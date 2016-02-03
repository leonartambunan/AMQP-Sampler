package com.sxi.jmeter.protocol.rpc.stockparam;

import com.rabbitmq.client.*;
import id.co.tech.cakra.message.proto.olt.StockParamRequest;
import id.co.tech.cakra.message.proto.olt.StockParamResponse;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.testelement.property.TestElementProperty;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class StockParam extends AbstractStockParam {

    private static final long serialVersionUID = 1L;
    private final static String HEADERS = "AMQPPublisher.Headers";
    private StockParamRequest stockParamRequest;
    private transient String stockParamConsumerTag;

    private transient CountDownLatch latch = new CountDownLatch(1);

    public boolean makeRequest() throws TimeoutException, InterruptedException, IOException {

        stockParamRequest = StockParamRequest
                .newBuilder()
                .setUserId(getMobileUserId())
                .setSessionId(getSessionId())
                .setMktId(getMktId())
                .setBoardcode(getBoardCode())
                .build();


        DefaultConsumer consumer = new DefaultConsumer(getChannel()) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                trace(new String(body));

                StockParamResponse response = StockParamResponse.parseFrom(body);
                result.setResponseMessage(response.toString());
                result.setResponseData(response.toString(), null);

                latch.countDown();

            }
        };

        result.sampleStart();

        trace("Starting basicConsume to ReplyTo Queue: " + getResponseQueue());
        stockParamConsumerTag = getChannel().basicConsume(getResponseQueue(), true, consumer);

        new Thread(new StockParamMessagePublisher()).start();

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
            if (stockParamConsumerTag != null && getChannel()!=null && getChannel().isOpen()) {
                getChannel().basicCancel(stockParamConsumerTag);
            }
        } catch(IOException e) {
            trace("Couldn't safely cancel the sample " + stockParamConsumerTag+ ' ' +  e.getMessage());
        }
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
                trace(e);
            }

        }
    }

}