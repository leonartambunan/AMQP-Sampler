package com.sxi.jmeter.protocol.rpc.pinvalidation;

import com.google.protobuf.InvalidProtocolBufferException;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.MessageProperties;
import id.co.tech.cakra.message.proto.olt.PINValidationRequest;
import id.co.tech.cakra.message.proto.olt.PINValidationResponse;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.testelement.property.TestElementProperty;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class PinValidation extends AbstractPinValidation {

    private static final long serialVersionUID = 1L;
    private final static String HEADERS = "AMQPPublisher.Headers";
    private PINValidationRequest request;
    private transient String responseTag;
    private transient CountDownLatch latch = new CountDownLatch(1);

    public boolean makeRequest()  {

        request = PINValidationRequest
                .newBuilder()
                .setUserId(getMobileUserId())
                .setSessionId(getSessionId())
                .setPinValue(getPin())
                .build();
        try {

            initChannel();

            DefaultConsumer consumer = new DefaultConsumer(getChannel()) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    trace(new String(body));
                    result.setResponseMessage(new String(body));
                    PINValidationResponse response = null;
                    try {response = PINValidationResponse.parseFrom(body);} catch (InvalidProtocolBufferException e) {trace(e.getMessage());}
                    result.setResponseData((response==null?"":response.toString()), null);
                    result.setResponseCodeOK();
                    result.setSuccessful(true);
                    latch.countDown();
                }
            };

            trace("Starting basicConsume to ReplyTo Queue: " + getResponseQueue());
            responseTag = getChannel().basicConsume(getResponseQueue(), true, consumer);

            new Thread(new PinValidationPublisher()).start();

            latch.await();

//            boolean noZero=latch.await(Long.valueOf(getTimeout()),TimeUnit.MILLISECONDS);
//            if (!noZero) {
//                throw new Exception("Time out");
//            }
        } catch (Exception e) {
            e.printStackTrace();
            trace(e.getMessage());
            result.setResponseCode("400");
            result.setResponseMessage(e.getMessage());
            result.setResponseData(e.getMessage(),null);
        }

        return true;
    }

    public void cleanup() {

        try {
            if (responseTag != null && getChannel().isOpen()) {
                getChannel().basicCancel(responseTag);
            }
        } catch(IOException e) {
            trace("Couldn't safely cancel the sample " + responseTag);
        }
        super.cleanup();
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
                e.printStackTrace();
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