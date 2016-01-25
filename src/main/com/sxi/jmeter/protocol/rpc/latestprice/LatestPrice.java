package com.sxi.jmeter.protocol.rpc.latestprice;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.MessageProperties;
import com.tech.cakra.datafeed.server.df.message.proto.CurrentMessageRequest;
import com.tech.cakra.datafeed.server.df.message.proto.CurrentMessageResponse;
import com.tech.cakra.datafeed.server.df.message.proto.MIType;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.testelement.property.TestElementProperty;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class LatestPrice extends AbstractLatestPrice {

    private static final long serialVersionUID = 1L;
    private final static String HEADERS = "AMQPPublisher.Headers";
    private CurrentMessageRequest stockPriceRequest;
    private transient String stockPriceConsumeTag;

    private transient CountDownLatch latch = new CountDownLatch(1);

    public boolean makeRequest()  {

        stockPriceRequest = CurrentMessageRequest
                .newBuilder()
                .setBoardCode(getBoardCode())
                .setDataType(MIType.LATEST_PRICE)
                .addItemCode(getStockId())
                .build();

        try {

            initChannel();

            DefaultConsumer consumer = new DefaultConsumer(getChannel()) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {

                    trace("Message received");

                    trace(new String(body));

                    result.setResponseMessage(new String(body));

                    CurrentMessageResponse response = CurrentMessageResponse.parseFrom(body);

                    result.setResponseData(response.toString(), null);
                        result.setResponseCodeOK();
                        result.setSuccessful(true);

                    latch.countDown();

                }
            };

            trace("Starting basicConsume to ReplyTo Queue: " + getResponseQueue());
            stockPriceConsumeTag = getChannel().basicConsume(getResponseQueue(), true, consumer);

            new Thread(new LatestPriceMessagePublisher()).start();

            latch.await();

//            boolean noZero=latch.await(Long.valueOf(getTimeout()), TimeUnit.MILLISECONDS);
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
        super.cleanup();
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
            e.printStackTrace();
        }

    }
}

}