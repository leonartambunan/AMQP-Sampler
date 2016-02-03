package com.sxi.jmeter.protocol.rpc.pinvalidation;

import com.google.protobuf.InvalidProtocolBufferException;
import com.rabbitmq.client.*;
import id.co.tech.cakra.message.proto.olt.PINValidationRequest;
import id.co.tech.cakra.message.proto.olt.PINValidationResponse;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.testelement.property.TestElementProperty;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class PinValidation extends AbstractPinValidation {

    private static final long serialVersionUID = 1L;
    private final static String HEADERS = "AMQPPublisher.Headers";
    private PINValidationRequest request;
    private transient String responseTag;
    private transient CountDownLatch latch = new CountDownLatch(1);

    public boolean makeRequest() throws TimeoutException, InterruptedException, IOException {

        request = PINValidationRequest
                .newBuilder()
                .setUserId(getMobileUserId())
                .setSessionId(getSessionId())
                .setPinValue(getPin())
                .build();

            DefaultConsumer consumer = new DefaultConsumer(getChannel()) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    trace(new String(body));
                    PINValidationResponse response = null;
                    try {response = PINValidationResponse.parseFrom(body);} catch (InvalidProtocolBufferException e) {trace(e.getMessage());}
                    result.setResponseMessage((response==null?"":response.toString()));
                    result.setResponseData((response==null?"":response.toString()), null);
                    latch.countDown();
                }
            };

            result.sampleStart();

            trace("Starting basicConsume to ReplyTo Queue: " + getResponseQueue());
            responseTag = getChannel().basicConsume(getResponseQueue(), true, consumer);

            new Thread(new PinValidationPublisher()).start();

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
            if (responseTag != null && getChannel()!=null && getChannel().isOpen()) {
                getChannel().basicCancel(responseTag);
            }
        } catch(IOException e) {
            trace("Couldn't safely cancel the sample " + responseTag);
        }
    }

    class PinValidationPublisher implements Runnable {

        @Override
        public void run() {

            try {
                AMQP.BasicProperties props = MessageProperties.MINIMAL_BASIC
                        .builder()
                        .replyTo(getResponseQueue())
                        .build();

                trace("Publishing PIN Validation Request to Queue:"+ getRequestQueue());
                result.setSamplerData(request.toString());

                getChannel().basicPublish("", getRequestQueue(), props, request.toByteArray());

            } catch (Exception e) {
                trace(e);
            }
        }
    }

    public Arguments getHeaders() {
        return (Arguments) getProperty(HEADERS).getObjectValue();
    }

    public void setHeaders(Arguments headers) {
        setProperty(new TestElementProperty(HEADERS, headers));
    }

}