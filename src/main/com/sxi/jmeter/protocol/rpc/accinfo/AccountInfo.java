package com.sxi.jmeter.protocol.rpc.accinfo;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.MessageProperties;
import id.co.tech.cakra.message.proto.olt.AccountRequest;
import id.co.tech.cakra.message.proto.olt.AccountResponse;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.testelement.property.TestElementProperty;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class AccountInfo extends AbstractAccountInfo {

    private static final long serialVersionUID = 1L;
    private final static String HEADERS = "AMQPPublisher.Headers";
    private AccountRequest accountRequest;
    private transient String accountInfoConsumerTag;
    private transient CountDownLatch latch = new CountDownLatch(1);

    public boolean makeRequest()  {

        accountRequest = AccountRequest
                .newBuilder()
                .setUserId(getMobileUserId())
                .setSessionId(getSessionId())
                .build();

        try {

            initChannel();

            DefaultConsumer accountInfoConsumer = new DefaultConsumer(getChannel()) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    trace(new String(body));
                    AccountResponse response = AccountResponse.parseFrom(body);
                    result.setResponseMessage(response.toString());
                    result.setResponseData(response.toString(), null);
                    result.setResponseCodeOK();
                    result.setSuccessful(true);
                    latch.countDown();
                }
            };

            trace("Starting basicConsume to Response Queue: " + getResponseQueue());

            accountInfoConsumerTag = getChannel().basicConsume(getResponseQueue(), true, accountInfoConsumer);

            new Thread(new AccountInfoMessagePublisher()).start();

            boolean noZero = latch.await(Long.valueOf(getTimeout()), TimeUnit.MILLISECONDS);

            if (!noZero) {
                throw new Exception("Time out");
            }

        } catch (Exception e) {
            e.printStackTrace();
            trace(e.getMessage());
            result.setResponseCode("400");
            result.setResponseMessage(e.getMessage());
        }

        return true;
    }

    public Arguments getHeaders() {
        return (Arguments) getProperty(HEADERS).getObjectValue();
    }

    public void setHeaders(Arguments headers) {
        setProperty(new TestElementProperty(HEADERS, headers));
    }

    public void cleanup() {

        try {
            if (accountInfoConsumerTag != null && getChannel()!=null && getChannel().isOpen()) {
                getChannel().basicCancel(accountInfoConsumerTag);
            }
        } catch(IOException e) {
            trace("Couldn't safely cancel the sample " + accountInfoConsumerTag+ ' ' +  e.getMessage());
        }
        super.cleanup();
    }


    class AccountInfoMessagePublisher implements Runnable {

        @Override
        public void run() {

            try {
                AMQP.BasicProperties props = MessageProperties.MINIMAL_BASIC
                        .builder()
                        .replyTo(getResponseQueue())
                        .build();

                trace("Publishing account info request message to Queue:"+ getRequestQueue());

                result.setSamplerData(accountRequest.toString());

                getChannel().basicPublish("", getRequestQueue(), props, accountRequest.toByteArray());

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

}