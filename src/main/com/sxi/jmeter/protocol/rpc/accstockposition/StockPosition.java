package com.sxi.jmeter.protocol.rpc.accstockposition;

import com.rabbitmq.client.*;
import id.co.tech.cakra.message.proto.olt.AccStockPosInfo;
import id.co.tech.cakra.message.proto.olt.AccStockPosRequest;
import id.co.tech.cakra.message.proto.olt.AccStockPosResponse;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.testelement.property.TestElementProperty;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class StockPosition extends AbstractStockPosition{

    private static final long serialVersionUID = 1L;
    private final static String HEADERS = "AMQPPublisher.Headers";
    private AccStockPosRequest accStockPosRequest;
    private transient String accStockPositionConsumerTag;
    private transient CountDownLatch latch = new CountDownLatch(1);


    public boolean makeRequest() throws TimeoutException, InterruptedException, IOException {

        accStockPosRequest = AccStockPosRequest
                .newBuilder()
                .setUserId(getMobileUserId())
                .setSessionId(getSessionId())
                .setAccNo(getAccNo())
                .setStockCode(getStockCode())
                .build();


        DefaultConsumer consumer = new DefaultConsumer(getChannel()) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {

                trace("Message received <--");

                trace(new String(body));

                //result.setResponseMessage(new String(body));

                AccStockPosResponse response = AccStockPosResponse.parseFrom(body);

//                    StringBuilder sb = new StringBuilder(10).append(response.toString());
//                    for (AccStockPosInfo info : response.getAccStockPosInfoList()) {
//                        sb.append(info.toString());
//                    }

                result.setResponseMessage(response.toString());
                result.setResponseData(response.toString(), null);
//                    result.setResponseCodeOK();
//                    result.setSuccessful(true);
                latch.countDown();
            }
        };

        result.sampleStart();

        trace("Starting basicConsume to ReplyTo Queue: " + getResponseQueue());
        accStockPositionConsumerTag = getChannel().basicConsume(getResponseQueue(), true, consumer);

        new Thread(new StockPositionMessagePublisher()).start();

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

    /**
     * Called by parent class
     */
    public void cleanup() {

        try {
            if (accStockPositionConsumerTag != null && getChannel()!=null && getChannel().isOpen()) {
                getChannel().basicCancel(accStockPositionConsumerTag);
            }
        } catch(IOException e) {
            trace("Couldn't safely cancel the sample " + accStockPositionConsumerTag);
        }
    }

    public Arguments getHeaders() {
        return (Arguments) getProperty(HEADERS).getObjectValue();
    }

    public void setHeaders(Arguments headers) {
        setProperty(new TestElementProperty(HEADERS, headers));
    }

    class StockPositionMessagePublisher implements Runnable {

        @Override
        public void run() {

            try {
                AMQP.BasicProperties props = MessageProperties.MINIMAL_BASIC
                        .builder()
                        .replyTo(getResponseQueue())
                        .build();

                trace("Publishing Stock Position request message to Queue:"+ getRequestQueue());
                result.setSamplerData(accStockPosRequest.toString());



                getChannel().basicPublish("", getRequestQueue(), props, accStockPosRequest.toByteArray());

            } catch (Exception e) {
                trace(e.getMessage());
            }

        }
    }

}