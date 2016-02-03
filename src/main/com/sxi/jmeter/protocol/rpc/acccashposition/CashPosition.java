package com.sxi.jmeter.protocol.rpc.acccashposition;

import com.rabbitmq.client.*;
import id.co.tech.cakra.message.proto.olt.AccCashPosRequest;
import id.co.tech.cakra.message.proto.olt.AccCashPosResponse;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.testelement.property.TestElementProperty;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CashPosition extends AbstractCashPosition {

    private static final long serialVersionUID = 1L;
    private final static String HEADERS = "AMQPPublisher.Headers";

    private AccCashPosRequest accCashPosRequest;
    private transient String accCashPosConsumerTag;
    private transient CountDownLatch latch = new CountDownLatch(1);

    public boolean makeRequest() throws InterruptedException, IOException, TimeoutException {

        accCashPosRequest = AccCashPosRequest
                .newBuilder()
                .setUserId(getMobileUserId())
                .setSessionId(getSessionId())
                .setAccNo(getAccNo())
                .build();


        DefaultConsumer consumer = new DefaultConsumer(getChannel()) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                trace(new String(body));
                AccCashPosResponse response = AccCashPosResponse.parseFrom(body);
                result.setResponseMessage(response.toString());
                result.setResponseData(response.toString(), null);
                latch.countDown();
            }
        };


        result.sampleStart();

        trace("Starting basicConsume to ReplyTo Queue: " + getResponseQueue());
        accCashPosConsumerTag = getChannel().basicConsume(getResponseQueue(), true, consumer);

        new Thread(new CashPositionPublisher()).start();

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

    public Arguments getHeaders() {
        return (Arguments) getProperty(HEADERS).getObjectValue();
    }

    public void setHeaders(Arguments headers) {
        setProperty(new TestElementProperty(HEADERS, headers));
    }

    public void cleanup() {
        try {
            if (accCashPosConsumerTag != null && getChannel()!=null && getChannel().isOpen()) {
                getChannel().basicCancel(accCashPosConsumerTag);
            }
        } catch(IOException e) {
            trace("Couldn't safely cancel the sample " + accCashPosConsumerTag);
        }
    }


    class CashPositionPublisher implements Runnable {

        @Override
        public void run() {

            try {
                AMQP.BasicProperties props = MessageProperties.MINIMAL_BASIC
                        .builder()
                        .replyTo(getResponseQueue())
                        .build();

                trace("Publishing Cash Position request message to Queue:"+ getRequestQueue());

                result.setSamplerData(accCashPosRequest.toString());

                getChannel().basicPublish("", getRequestQueue(), props, accCashPosRequest.toByteArray());

            } catch (Exception e) {
                trace(e.getMessage());
            }

        }
    }

}