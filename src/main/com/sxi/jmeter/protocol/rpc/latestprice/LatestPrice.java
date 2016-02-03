package com.sxi.jmeter.protocol.rpc.latestprice;

import com.rabbitmq.client.*;
import com.tech.cakra.datafeed.server.df.message.proto.CurrentMessageRequest;
import com.tech.cakra.datafeed.server.df.message.proto.CurrentMessageResponse;
import com.tech.cakra.datafeed.server.df.message.proto.MIType;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.testelement.property.TestElementProperty;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class LatestPrice extends AbstractLatestPrice {

    private static final long serialVersionUID = 1L;
    private final static String HEADERS = "AMQPPublisher.Headers";
    private transient CurrentMessageRequest stockPriceRequest;
    private transient String stockPriceConsumeTag;

    private transient CountDownLatch latch = new CountDownLatch(1);

    public boolean makeRequest() throws TimeoutException, InterruptedException, IOException {

        stockPriceRequest = CurrentMessageRequest
                .newBuilder()
                .setBoardCode(getBoardCode())
                .setDataType(MIType.LATEST_PRICE)
                .addItemCode(getStockId())
                .build();


        DefaultConsumer consumer = new DefaultConsumer(getChannel()) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {

                trace("Message received");

                trace(new String(body));

                CurrentMessageResponse response = CurrentMessageResponse.parseFrom(body);

                result.setResponseMessage(response.toString());
                result.setResponseData(response.toString(), null);
                latch.countDown();

            }
        };

        result.sampleStart(); //STARTER

        trace("Starting basicConsume to ReplyTo Queue: " + getResponseQueue());
        stockPriceConsumeTag = getChannel().basicConsume(getResponseQueue(), true, consumer);

        new Thread(new LatestPriceMessagePublisher()).start();

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
            if (stockPriceConsumeTag != null && getChannel()!=null && getChannel().isOpen()) {
                getChannel().basicCancel(stockPriceConsumeTag);
            }
        } catch(IOException e) {
            trace("Couldn't safely cancel the sample " + stockPriceConsumeTag + ' ' +  e.getMessage());
        }
    }

    class LatestPriceMessagePublisher implements Runnable {

        @Override
        public void run() {

            try {
                AMQP.BasicProperties props = MessageProperties.MINIMAL_BASIC
                        .builder()
                        .replyTo(getResponseQueue())
                        .build();

                trace("Publishing Latest Stock Price request message to Queue:"+ getRequestQueue());

                result.setSamplerData(stockPriceRequest.toString());

                getChannel().basicPublish("", getRequestQueue(), props, stockPriceRequest.toByteArray());

            } catch (Exception e) {
                trace(e);
            }

        }
    }

}