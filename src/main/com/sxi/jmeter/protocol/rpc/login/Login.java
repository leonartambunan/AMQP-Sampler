package com.sxi.jmeter.protocol.rpc.login;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.MessageProperties;
import id.co.tech.cakra.message.proto.olt.LogonRequest;
import id.co.tech.cakra.message.proto.olt.LogonResponse;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.testelement.property.TestElementProperty;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Login extends AbstractLogin {

    private static final long serialVersionUID = 1L;
    private transient String loginConsumerTag=null;
    private transient LogonRequest logonRequest;
    private transient CountDownLatch latch = new CountDownLatch(1);

    public boolean makeRequest() throws IOException, InterruptedException, TimeoutException {
        trace("login()");



        try {
            logonRequest = LogonRequest
                    .newBuilder()
                    .setUserId(getMobileUserId())
                    .setPassword(getMobilePassword())
                    .setDeviceId(getMobileDeviceId())
                    .setDeviceType(getMobileType())
                    .setAppVersion(getMobileAppVersion())
                    .setIp(InetAddress.getLocalHost().getHostAddress())
                    .build();
        } catch (UnknownHostException e) {
            throw new IOException(e.getMessage());
        }

        DefaultConsumer consumer = new DefaultConsumer(getChannel()) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {

                trace("Message received");
                trace(new String(body));

                LogonResponse response = LogonResponse.parseFrom(body);

                result.setResponseMessage(response.toString());
                result.setResponseData(response.toString(), null);

                latch.countDown();

            }
        };

        trace("Starting basicConsume to ReplyTo Queue: " + getLogonReplyToQueue());

        result.sampleStart(); //STARTER

        loginConsumerTag = getChannel().basicConsume(getLogonReplyToQueue(), true, consumer);

        new Thread(new LoginMessagePublisher()).start();

        if (Integer.valueOf(getTimeout()) == 0) {
            latch.await();
        } else {
            boolean notZero = latch.await(Long.valueOf(getTimeout()), TimeUnit.MILLISECONDS);

            if (!notZero) {
                throw new TimeoutException("Time out");
            }
        }

        return true;
    }

    public void cleanup() {

        try {
            if (loginConsumerTag != null && getChannel()!=null && getChannel().isOpen()) {
                getChannel().basicCancel(loginConsumerTag);
            }
        } catch(IOException e) {
            trace("Couldn't safely cancel the sample " + loginConsumerTag + ' ' +  e.getMessage());
        }
    }

    class LoginMessagePublisher implements Runnable {

        @Override
        public void run() {

            try {
                AMQP.BasicProperties props = MessageProperties.MINIMAL_BASIC
                        .builder()
                        .replyTo(getLogonReplyToQueue())
                        .build();

                trace("Publishing Login request message to Queue: ["+ getLogonRequestQueue()+ ']');

                result.setSamplerData(logonRequest.toString());

                getChannel().basicPublish("", getLogonRequestQueue(), props, logonRequest.toByteArray());

            } catch (Exception e) {
                trace(e);
            }
        }
    }

    private final static String HEADERS = "AMQPPublisher.Headers";

    public Arguments getHeaders() {
        return (Arguments) getProperty(HEADERS).getObjectValue();
    }

    public void setHeaders(Arguments headers) {
        setProperty(new TestElementProperty(HEADERS, headers));
    }

}