package com.sxi.jmeter.protocol.rpc.stockrecommendation;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.MessageProperties;
import id.co.tech.cakra.message.proto.olt.StockRecommendRequest;
import id.co.tech.cakra.message.proto.olt.StockRecommendResponse;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.testelement.property.TestElementProperty;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class StockRecommendation extends AbstractStockRecommendation {

    private static final long serialVersionUID = 1L;
    private final static String HEADERS = "AMQPPublisher.Headers";
    private StockRecommendRequest stockRecommendationRequest;
    private transient String stockParamConsumerTag;

    private transient CountDownLatch latch = new CountDownLatch(1);

    public boolean makeRequest()  {

        stockRecommendationRequest = StockRecommendRequest
                .newBuilder()
                .setUserId(getMobileUserId())
                .setSessionId(getSessionId())
                .setBoardCode(getBoardCode())
                .setStockCode(getStockId())
                .build();

        try {

            initChannel();

            DefaultConsumer consumer = new DefaultConsumer(getChannel()) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {

                    trace("Message received");

                    trace(new String(body));

                    result.setResponseMessage(new String(body));

                    StockRecommendResponse response = StockRecommendResponse.parseFrom(body);

                    result.setResponseData(response.toString(), null);

                    result.setResponseCodeOK();

                    result.setSuccessful(true);

                    latch.countDown();

                }
            };

            trace("Starting basicConsume to ReplyTo Queue: " + getResponseQueue());
            stockParamConsumerTag = getChannel().basicConsume(getResponseQueue(), true, consumer);

            new Thread(new StockRecommendationMessagePublisher()).start();

            latch.await();

//            boolean noZero = latch.await(Long.valueOf(getTimeout()), TimeUnit.MILLISECONDS);
//            if (!noZero) {
//                throw new Exception("Time out");
//            }
        } catch (Exception e) {
            trace(e.getMessage());
            result.setResponseCode("400");
            result.setResponseMessage("Exception:"+e.getMessage());
            result.setResponseData("Exception:"+e.getMessage(),null);
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
            if (stockParamConsumerTag != null && getChannel()!=null && getChannel().isOpen()) {
                getChannel().basicCancel(stockParamConsumerTag);
            }
        } catch(IOException e) {
            trace("Couldn't safely cancel the sample " + stockParamConsumerTag+ ' ' +  e.getMessage());
        }
        super.cleanup();
    }

class StockRecommendationMessagePublisher implements Runnable {

    @Override
    public void run() {

        try {
            AMQP.BasicProperties props = MessageProperties.MINIMAL_BASIC
                    .builder()
                    .replyTo(getResponseQueue())
                    .build();

            trace("Publishing Stock Recommendation request message to Queue:"+ getRequestQueue());
            trace(stockRecommendationRequest.toString());
            result.setSamplerData(stockRecommendationRequest.toString());
            getChannel().basicPublish("", getRequestQueue(), props, stockRecommendationRequest.toByteArray());

        } catch (Exception e) {
           trace(e.getMessage());
        }

    }
}

}