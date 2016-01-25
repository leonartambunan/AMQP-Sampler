package com.sxi.jmeter.protocol.rpc.accstockposition;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.MessageProperties;
import id.co.tech.cakra.message.proto.olt.AccStockPosInfo;
import id.co.tech.cakra.message.proto.olt.AccStockPosRequest;
import id.co.tech.cakra.message.proto.olt.AccStockPosResponse;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.testelement.property.TestElementProperty;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class StockPosition extends AbstractStockPosition{

    private static final long serialVersionUID = 1L;
    private final static String HEADERS = "AMQPPublisher.Headers";
    private AccStockPosRequest accStockPosRequest;
    private transient String accStockPositionConsumerTag;
    private transient CountDownLatch latch = new CountDownLatch(1);


    public boolean makeRequest()  {

        accStockPosRequest = AccStockPosRequest
                .newBuilder()
                .setUserId(getMobileUserId())
                .setSessionId(getSessionId())
                .setAccNo(getAccNo())
                .setStockCode(getStockCode())
                .build();

        try {

            initChannel();

            DefaultConsumer consumer = new DefaultConsumer(getChannel()) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {

                    trace("Message received <--");

                    trace(new String(body));

                    result.setResponseMessage(new String(body));

                    AccStockPosResponse response = AccStockPosResponse.parseFrom(body);

                    StringBuilder sb = new StringBuilder(10).append(response.toString());
                    for (AccStockPosInfo info : response.getAccStockPosInfoList()) {
                        sb.append(info.toString());
                    }

                    result.setResponseData(sb.toString(), null);
                    result.setResponseCodeOK();
                    result.setSuccessful(true);
                    latch.countDown();
                }
            };


            trace("Starting basicConsume to ReplyTo Queue: " + getResponseQueue());
            accStockPositionConsumerTag = getChannel().basicConsume(getResponseQueue(), true, consumer);

            new Thread(new StockPositionMessagePublisher()).start();

            latch.await();

//            boolean noZero=latch.await(Long.valueOf(getTimeout()), TimeUnit.MILLISECONDS);
//            if (!noZero) {
//                throw new Exception("Time out");
//            }
        } catch (Exception e) {
            trace(e.getMessage());
            result.setResponseCode("400");
            result.setResponseMessage(e.getMessage());
            result.setResponseData(e.getMessage(),null);
        }

        return true;
    }

    public void cleanup() {

        try {
            if (accStockPositionConsumerTag != null && getChannel()!=null && getChannel().isOpen()) {
                getChannel().basicCancel(accStockPositionConsumerTag);
            }
        } catch(IOException e) {
            trace("Couldn't safely cancel the sample " + accStockPositionConsumerTag);
        }
        super.cleanup();
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