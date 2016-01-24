package com.sxi.jmeter.protocol.rpc.getwatchlist;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.MessageProperties;
import id.co.tech.cakra.message.proto.olt.GetWatchListRequest;
import id.co.tech.cakra.message.proto.olt.GetWatchListResponse;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.testelement.property.TestElementProperty;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class GetWatchList extends AbstractGetWatchList {

    private static final long serialVersionUID = 1L;
    private final static String HEADERS = "AMQPPublisher.Headers";
    private GetWatchListRequest getWatchListRequest;
    private transient String watchListConsumerTag;
    private transient CountDownLatch latch = new CountDownLatch(1);

    public boolean makeRequest()  {

        getWatchListRequest = GetWatchListRequest
                .newBuilder()
                .setUserId(getMobileUserId())
                .setSessionId(getSessionId())
                .build();

        try {

            initChannel();

            DefaultConsumer consumer = new DefaultConsumer(getChannel()) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {

                    trace("Message received <--");
                    trace(envelope.toString());

                    trace(new String(body));

                    result.setResponseMessage(new String(body));

                    GetWatchListResponse response = GetWatchListResponse.parseFrom(body);

                    result.setResponseData(response.toString(), null);

                    if ("OK".equals(response.getStatus())) {
                        result.setResponseCodeOK();
                        result.setSuccessful(true);
                    }

                    latch.countDown();
                }
            };

            trace("Starting basicConsume to ReplyTo Queue: " + getResponseQueue());
            watchListConsumerTag = getChannel().basicConsume(getResponseQueue(), true, consumer);

            new Thread(new CashPositionPublisher()).start();

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

    public void cleanup() {

        try {
            if (watchListConsumerTag != null && getChannel()!=null && getChannel().isOpen()) {
                getChannel().basicCancel(watchListConsumerTag);
            }
        } catch(IOException e) {
            trace("Couldn't safely cancel the sample " + watchListConsumerTag+ ' ' +  e.getMessage());
        }
        super.cleanup();
    }



    class CashPositionPublisher implements Runnable {

        @Override
        public void run() {

            try {
                AMQP.BasicProperties props = MessageProperties.MINIMAL_BASIC
                        .builder()
                        .replyTo(getResponseQueue())
                        .build();

                trace("Publishing Get Watch List request message to Queue:"+ getRequestQueue());
                result.setSamplerData(getWatchListRequest.toString());
                getChannel().basicPublish("", getRequestQueue(), props, getWatchListRequest.toByteArray());

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public Arguments getHeaders() {
        return (Arguments) getProperty(HEADERS).getObjectValue();
    }

    public void setHeaders(Arguments headers) {
        setProperty(new TestElementProperty(HEADERS, headers));
    }
}