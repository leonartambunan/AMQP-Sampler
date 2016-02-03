package com.sxi.jmeter.protocol.rpc.getwatchlist;

import com.rabbitmq.client.*;
import id.co.tech.cakra.message.proto.olt.GetWatchListRequest;
import id.co.tech.cakra.message.proto.olt.GetWatchListResponse;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.testelement.property.TestElementProperty;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class GetWatchList extends AbstractGetWatchList {

    private static final long serialVersionUID = 1L;
    private final static String HEADERS = "AMQPPublisher.Headers";
    private GetWatchListRequest getWatchListRequest;
    private transient String watchListConsumerTag;
    private transient CountDownLatch latch = new CountDownLatch(1);

    public boolean makeRequest() throws IOException, InterruptedException, TimeoutException {

        getWatchListRequest = GetWatchListRequest
                .newBuilder()
                .setUserId(getMobileUserId())
                .setSessionId(getSessionId())
                .build();


            DefaultConsumer consumer = new DefaultConsumer(getChannel()) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {

                    trace("Message received <--");
                    trace(envelope.toString());

                    trace(new String(body));

                    //result.setResponseMessage(new String(body));

                    GetWatchListResponse response = GetWatchListResponse.parseFrom(body);

                    result.setResponseMessage(response.toString());
                    result.setResponseData(response.toString(), null);

//                    if ("OK".equals(response.getStatus())) {
//                        result.setResponseCodeOK();
//                        result.setSuccessful(true);
//                    }

                    latch.countDown();
                }
            };

        result.sampleStart();

            trace("Starting basicConsume to ReplyTo Queue: " + getResponseQueue());
            watchListConsumerTag = getChannel().basicConsume(getResponseQueue(), true, consumer);



            new Thread(new CashPositionPublisher()).start();

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

    public void cleanup() {

        try {
            if (watchListConsumerTag != null && getChannel()!=null && getChannel().isOpen()) {
                getChannel().basicCancel(watchListConsumerTag);
            }
        } catch(IOException e) {
            trace("Couldn't safely cancel the sample " + watchListConsumerTag+ ' ' +  e.getMessage());
        }
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
                trace(e);
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