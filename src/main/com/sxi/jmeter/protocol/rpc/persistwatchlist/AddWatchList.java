package com.sxi.jmeter.protocol.rpc.persistwatchlist;

import com.rabbitmq.client.*;
import id.co.tech.cakra.message.proto.olt.ExchangeKey;
import id.co.tech.cakra.message.proto.olt.PersistWatchListRequest;
import id.co.tech.cakra.message.proto.olt.PersistWatchListResponse;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;

public class AddWatchList extends AbstractAddWatchList {

    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggingManager.getLoggerForClass();
    private final static String HEADERS = "AMQPPublisher.Headers";
    private PersistWatchListRequest persistWatchListRequest;
    private transient String watchListConsumerTag;
    private transient CountDownLatch latch = new CountDownLatch(1);

    public void makeRequest()  {

        ExchangeKey key = ExchangeKey.newBuilder()
                .setExchangeName(getExchangeName())
                .setBindingKey(0,getBindingKey()) //TODO ERROR
                .build();

        persistWatchListRequest = PersistWatchListRequest
                .newBuilder()
                .setUserId(getMobileUserId())
                .setSessionId(getSessionId())
                .setWatchList(key)
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
                    result.setDataType(SampleResult.TEXT);
                    result.setResponseCodeOK();
                    result.setSuccessful(true);
                    latch.countDown();
                }
            };

            trace("Starting basicConsume to ReplyTo Queue: " + getResponseQueue());
            watchListConsumerTag = getChannel().basicConsume(getResponseQueue(), true, consumer);

            new Thread(new AddWatchListPublisher()).start();

            latch.await();

        } catch (ShutdownSignalException e) {
            e.printStackTrace();
            trace(e.getMessage());
            result.setResponseCode("400");
            result.setResponseMessage(e.getMessage());
            interrupt();
        } catch (ConsumerCancelledException e) {
            e.printStackTrace();
            trace(e.getMessage());
            result.setResponseCode("300");
            result.setResponseMessage(e.getMessage());
            interrupt();
        } catch (InterruptedException e) {
            e.printStackTrace();
            trace(e.getMessage());
            result.setResponseCode("200");
            result.setResponseMessage(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            trace(e.getMessage());
            result.setResponseCode("100");
            result.setResponseMessage(e.getMessage());
        } catch (TimeoutException e) {
            e.printStackTrace();
            trace(e.getMessage());
            result.setResponseCode("600");
            result.setResponseMessage(e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            trace(e.getMessage());
            result.setResponseCode("700");
            result.setResponseMessage(e.getMessage());
        } catch (KeyManagementException e) {
            e.printStackTrace();
            trace(e.getMessage());
            result.setResponseCode("800");
            result.setResponseMessage(e.getMessage());
        }
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