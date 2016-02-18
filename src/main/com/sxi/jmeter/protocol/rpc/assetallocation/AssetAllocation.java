package com.sxi.jmeter.protocol.rpc.assetallocation;

import com.rabbitmq.client.*;
import id.co.tech.cakra.message.proto.olt.AssetAllocationRequest;
import id.co.tech.cakra.message.proto.olt.AssetAllocationResponse;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.samplers.Interruptible;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class AssetAllocation extends AbstractAssetAllocation implements Interruptible, TestStateListener {

    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggingManager.getLoggerForClass();
    private final static String HEADERS = "AMQPPublisher.Headers";
    private AssetAllocationRequest assetAllocationRequest;
    private transient String assetAllocationConsumerTag;
    private transient CountDownLatch latch = new CountDownLatch(1);

    public boolean makeRequest() throws IOException, InterruptedException, TimeoutException {

        assetAllocationRequest = AssetAllocationRequest
                .newBuilder()
                .setUserId(getMobileUserId())
                .setSessionId(getSessionId())
                .setRequestType(getRequestTypeAsInt())
                .setAccNo(getAccNo())
                .build();

        DefaultConsumer consumer = new DefaultConsumer(getChannel()) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                trace(new String(body));
                AssetAllocationResponse response = AssetAllocationResponse.parseFrom(body);
                result.setResponseMessage(response.toString());
                result.setResponseData(response.toString(), null);
                latch.countDown();
            }
        };

        result.sampleStart();

        trace("Starting basicConsume to ReplyTo Queue: " + getResponseQueue());
        assetAllocationConsumerTag = getChannel().basicConsume(getResponseQueue(), true, consumer);

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
    public Arguments getHeaders() {
        return (Arguments) getProperty(HEADERS).getObjectValue();
    }

    public void setHeaders(Arguments headers) {
        setProperty(new TestElementProperty(HEADERS, headers));
    }

    public void cleanup() {

        try {
            if (assetAllocationConsumerTag != null && getChannel()!=null && getChannel().isOpen()) {
                getChannel().basicCancel(assetAllocationConsumerTag);
            }
        } catch(IOException e) {
            trace("Couldn't safely cancel the sample " + assetAllocationConsumerTag );
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

                trace("Publishing Asset Allocation request message to Queue:"+ getRequestQueue());
                result.setSamplerData(assetAllocationRequest.toString());

                getChannel().basicPublish("", getRequestQueue(), props, assetAllocationRequest.toByteArray());

            } catch (Exception e) {
                trace(e);
            }

        }
    }


}