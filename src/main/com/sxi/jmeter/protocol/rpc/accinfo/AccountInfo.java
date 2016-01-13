package com.sxi.jmeter.protocol.rpc.accinfo;

import com.rabbitmq.client.*;
import id.co.tech.cakra.message.proto.olt.AccountRequest;
import id.co.tech.cakra.message.proto.olt.AccountResponse;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;

public class AccountInfo extends AbstractAccountInfo {

    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggingManager.getLoggerForClass();
    private final static String HEADERS = "AMQPPublisher.Headers";
    private AccountRequest accountRequest;
    private transient String accountInfoConsumerTag;
    private transient CountDownLatch latch = new CountDownLatch(1);

    public void makeRequest()  {

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
                    result.setDataType(SampleResult.TEXT);
                    result.setResponseCodeOK();
                    result.setSuccessful(true);
                    latch.countDown();
                }
            };

            trace("Starting basicConsume to Response Queue: " + getResponseQueue());

            accountInfoConsumerTag = getChannel().basicConsume(getResponseQueue(), true, accountInfoConsumer);

            new Thread(new AccountInfoMessagePublisher()).start();

            latch.await();

        } catch (ShutdownSignalException e) {
            e.printStackTrace();
            trace(e.getMessage());
            result.setResponseCode("400");
            result.setResponseMessage(e.getMessage());
            interrupt();
        } catch (ConsumerCancelledException e) {
            e.printStackTrace();
            trace(e.getMessage());
            result.setResponseCode("300");
            result.setResponseMessage(e.getMessage());
            interrupt();
        } catch (InterruptedException e) {
            e.printStackTrace();
            trace(e.getMessage());
            result.setResponseCode("200");
            result.setResponseMessage(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            trace(e.getMessage());
            result.setResponseCode("100");
            result.setResponseMessage(e.getMessage());
        } catch (TimeoutException e) {
            e.printStackTrace();
            trace(e.getMessage());
            result.setResponseCode("600");
            result.setResponseMessage(e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            trace(e.getMessage());
            result.setResponseCode("700");
            result.setResponseMessage(e.getMessage());
        } catch (KeyManagementException e) {
            e.printStackTrace();
            trace(e.getMessage());
            result.setResponseCode("800");
            result.setResponseMessage(e.getMessage());
        }
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
            trace("Couldn't safely cancel the sample " + accountInfoConsumerTag+ " " +  e.getMessage());
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