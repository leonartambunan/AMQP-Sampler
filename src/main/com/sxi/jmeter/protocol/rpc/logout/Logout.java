package com.sxi.jmeter.protocol.rpc.logout;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.MessageProperties;
import id.co.tech.cakra.message.proto.olt.LogonResponse;
import id.co.tech.cakra.message.proto.olt.LogoutRequest;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.testelement.property.TestElementProperty;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class Logout extends AbstractLogout {

    private static final long serialVersionUID = 1L;
    private final static String HEADERS = "AMQPPublisher.Headers";
    private LogoutRequest logoutRequest;
    private transient String logoutTag;
    private transient CountDownLatch latch = new CountDownLatch(1);

    public boolean makeRequest()  {

        logoutRequest = LogoutRequest
                .newBuilder()
                .setUserId(getMobileUserId())
                .setSessionId(getSessionId())
                .setReason(getLogoutReason())
                .build();

        try {

            initChannel();

            DefaultConsumer consumer = new DefaultConsumer(getChannel()) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    trace(new String(body));
                    LogonResponse response = LogonResponse.parseFrom(body);
                    result.setResponseMessage(new String(body));
                    result.setResponseData(response.toString(), null);
                    result.setResponseCodeOK();
                    result.setSuccessful(true);
                    latch.countDown();
                }
            };

            trace("Starting basicConsume to Logout Response Queue: " + getResponseQueue());

            logoutTag = getChannel().basicConsume(getResponseQueue(), true, consumer);

            new Thread(new LogoutMessagePublisher()).start();

            boolean noZero=latch.await(Long.valueOf(getTimeout()), TimeUnit.MILLISECONDS);
            if (!noZero) {
                throw new Exception("Time out");
            }
        } catch (Exception e) {
            e.printStackTrace();
            trace("400 " + e.getMessage());
            result.setResponseCode("400");
            result.setResponseMessage(e.getMessage());
            result.setResponseData(e.getMessage(),null);
        }

        terminateConnection();

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
            if (logoutTag != null  && getChannel()!=null && getChannel().isOpen()) {
                getChannel().basicCancel(logoutTag);
            }
        } catch(IOException e) {
            trace("Couldn't safely cancel the sample " + logoutTag+ ' ' +  e.getMessage());
        }

        super.cleanup();

    }

    class LogoutMessagePublisher implements Runnable {

        @Override
        public void run() {

            try {
                AMQP.BasicProperties props = MessageProperties.MINIMAL_BASIC
                        .builder()
                        .replyTo(getResponseQueue())
                        .build();

                trace("Publishing Logout request message to Queue:"+ getRequestQueue());

                getChannel().basicPublish("", getRequestQueue(), props, logoutRequest.toByteArray());

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}