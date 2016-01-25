package com.sxi.jmeter.protocol.rpc.persistwatchlist;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.MessageProperties;
import id.co.tech.cakra.message.proto.olt.ExchangeKey;
import id.co.tech.cakra.message.proto.olt.PersistWatchListRequest;
import id.co.tech.cakra.message.proto.olt.PersistWatchListResponse;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.testelement.property.TestElementProperty;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class AddWatchList extends AbstractAddWatchList {

    private static final long serialVersionUID = 1L;
    private final static String HEADERS = "AMQPPublisher.Headers";
    private PersistWatchListRequest persistWatchListRequest;
    private transient String watchListConsumerTag;
    private transient CountDownLatch latch = new CountDownLatch(1);

    public boolean makeRequest()  {

        List<String> routingKeys = new ArrayList<>(1);
        routingKeys.add(getBindingKey());

        ExchangeKey watchList = ExchangeKey
                .newBuilder()
                .setExchangeName(getExchangeName())
                .addAllBindingKey(routingKeys)
                .build();

        persistWatchListRequest = PersistWatchListRequest
                .newBuilder()
                .setUserId(getMobileUserId())
                .setSessionId(getSessionId())
                .setWatchList(watchList)
                .build();

        try {

            initChannel();

            DefaultConsumer consumer = new DefaultConsumer(getChannel()) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    trace(new String(body));
                    PersistWatchListResponse response = PersistWatchListResponse.parseFrom(body);
                    result.setResponseMessage(new String(body));
                    result.setResponseData(response.toString(), null);
                    result.setResponseCodeOK();
                    result.setSuccessful(true);
                    latch.countDown();
                }
            };

            trace("Starting basicConsume to ReplyTo Queue: " + getResponseQueue());
            watchListConsumerTag = getChannel().basicConsume(getResponseQueue(), true, consumer);

            new Thread(new AddWatchListPublisher()).start();

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
            if (watchListConsumerTag != null && getChannel()!=null && getChannel().isOpen()) {
                getChannel().basicCancel(watchListConsumerTag);
            }
        } catch(IOException e) {
            trace("Couldn't safely cancel the sample " + watchListConsumerTag);
        }
        super.cleanup();
    }

    class AddWatchListPublisher implements Runnable {

        @Override
        public void run() {

            try {
                AMQP.BasicProperties props = MessageProperties.MINIMAL_BASIC
                        .builder()
                        .replyTo(getResponseQueue())
                        .build();

                trace("Publishing Add Watch List request message to Queue:"+ getRequestQueue());
                result.setSamplerData(persistWatchListRequest.toString());
                getChannel().basicPublish("", getRequestQueue(), props, persistWatchListRequest.toByteArray());

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }


}