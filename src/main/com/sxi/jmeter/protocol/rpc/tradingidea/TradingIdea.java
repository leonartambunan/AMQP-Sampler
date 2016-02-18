package com.sxi.jmeter.protocol.rpc.tradingidea;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.MessageProperties;
import id.co.tech.cakra.message.proto.olt.TradingIdeaRequest;
import id.co.tech.cakra.message.proto.olt.TradingIdeaResponse;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.testelement.property.TestElementProperty;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class TradingIdea extends AbstractTradingIdea {

    private static final long serialVersionUID = 1L;
    private final static String HEADERS = "AMQPPublisher.Headers";
    private TradingIdeaRequest request;
    private transient String consumerTag;

    private transient CountDownLatch latch = new CountDownLatch(1);

    public boolean makeRequest() throws TimeoutException, InterruptedException, IOException {

        request = TradingIdeaRequest
                .newBuilder()
                .setUserId(getMobileUserId())
                .setSessionId(getSessionId())
                .setBoardCode(getBoardCode())
                .setIdeaCode("*")
                .setProductId(1)
                .build();


        DefaultConsumer consumer = new DefaultConsumer(getChannel()) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                trace(new String(body));

                TradingIdeaResponse response = TradingIdeaResponse.parseFrom(body);
                result.setResponseMessage(response.toString());
                result.setResponseData(response.toString(), null);

                latch.countDown();

            }
        };

        result.sampleStart();

        trace("Starting basicConsume to ReplyTo Queue: " + getResponseQueue());
        consumerTag = getChannel().basicConsume(getResponseQueue(), true, consumer);

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
            if (consumerTag != null && getChannel()!=null && getChannel().isOpen()) {
                getChannel().basicCancel(consumerTag);
            }
        } catch(IOException e) {
            trace("Couldn't safely cancel the sample " + consumerTag+ ' ' +  e.getMessage());
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

                trace("Publishing Trading Idea request message to Queue:"+ getRequestQueue());
                result.setSamplerData(request.toString());

                getChannel().basicPublish("", getRequestQueue(), props, request.toByteArray());

            } catch (Exception e) {
                trace(e);
            }

        }
    }

}