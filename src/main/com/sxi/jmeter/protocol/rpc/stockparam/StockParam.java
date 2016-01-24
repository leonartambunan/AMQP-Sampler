package com.sxi.jmeter.protocol.rpc.stockparam;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.MessageProperties;
import id.co.tech.cakra.message.proto.olt.StockParamRequest;
import id.co.tech.cakra.message.proto.olt.StockParamResponse;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.testelement.property.TestElementProperty;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class StockParam extends AbstractStockParam {

    private static final long serialVersionUID = 1L;
    private final static String HEADERS = "AMQPPublisher.Headers";
    private StockParamRequest stockParamRequest;
    private transient String stockParamConsumerTag;

    private transient CountDownLatch latch = new CountDownLatch(1);

    public boolean makeRequest()  {

        stockParamRequest = StockParamRequest
                .newBuilder()
                .setUserId(getMobileUserId())
                .setSessionId(getSessionId())
                .setMktId(getMktId())
                .setBoardcode(getBoardCode())
                .build();

        try {

            initChannel();

            DefaultConsumer consumer = new DefaultConsumer(getChannel()) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    trace(new String(body));
                    result.setResponseMessage(new String(body));

                    StockParamResponse response = StockParamResponse.parseFrom(body);

                    result.setResponseData(response.toString(), null);

                    if ("OK".equals(response.getStatus())) {
                        result.setResponseCodeOK();
                        result.setSuccessful(true);
                    }

                    latch.countDown();

                }
            };

            trace("Starting basicConsume to ReplyTo Queue: " + getResponseQueue());
            stockParamConsumerTag = getChannel().basicConsume(getResponseQueue(), true, consumer);

            new Thread(new StockParamMessagePublisher()).start();

            boolean noZero=latch.await(Long.valueOf(getTimeout()), TimeUnit.MILLISECONDS);
            if (!noZero) {
                throw new Exception("Time out");
            }
        } catch (Exception e) {
            e.printStackTrace();
            trace(e.getMessage());
            result.setResponseCode("400");
            result.setResponseMessage(e.getMessage());
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

class StockParamMessagePublisher implements Runnable {

    @Override
    public void run() {

        try {
            AMQP.BasicProperties props = MessageProperties.MINIMAL_BASIC
                    .builder()
                    .replyTo(getResponseQueue())
                    .build();

            trace("Publishing Stock Param request message to Queue:"+ getRequestQueue());
            result.setSamplerData(stockParamRequest.toString());
            getChannel().basicPublish("", getRequestQueue(), props, stockParamRequest.toByteArray());

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

}