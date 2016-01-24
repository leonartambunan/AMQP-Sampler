package com.sxi.jmeter.protocol.rpc.assetallocation;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.MessageProperties;
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

public class AssetAllocation extends AbstractAssetAllocation implements Interruptible, TestStateListener {

    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggingManager.getLoggerForClass();
    private final static String HEADERS = "AMQPPublisher.Headers";
    private AssetAllocationRequest assetAllocationRequest;
    private transient String assetAllocationConsumerTag;
    private transient CountDownLatch latch = new CountDownLatch(1);

    public boolean makeRequest()  {

        assetAllocationRequest = AssetAllocationRequest
                .newBuilder()
                .setUserId(getMobileUserId())
                .setSessionId(getSessionId())
                .setRequestType(getRequestTypeAsInt())
                .build();

        try {

            initChannel();

            DefaultConsumer consumer = new DefaultConsumer(getChannel()) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    trace(new String(body));
                    AssetAllocationResponse response = AssetAllocationResponse.parseFrom(body);
                    result.setResponseMessage(new String(body));
                    result.setResponseData(response.toString(), null);
                    result.setResponseCodeOK();
                    result.setSuccessful(true);
                    latch.countDown();
                }
            };


            trace("Starting basicConsume to ReplyTo Queue: " + getResponseQueue());
            assetAllocationConsumerTag = getChannel().basicConsume(getResponseQueue(), true, consumer);

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
    public Arguments getHeaders() {
        return (Arguments) getProperty(HEADERS).getObjectValue();
    }

    public void setHeaders(Arguments headers) {
        setProperty(new TestElementProperty(HEADERS, headers));
    }

    @Override
    public boolean interrupt() {
        testEnded();
        return true;
    }

    @Override
    public void testEnded() {

    }

    @Override
    public void testEnded(String arg0) {

    }

    @Override
    public void testStarted() {

    }

    @Override
    public void testStarted(String arg0) {

    }
    public void cleanup() {

        try {
            if (assetAllocationConsumerTag != null && getChannel()!=null && getChannel().isOpen()) {
                getChannel().basicCancel(assetAllocationConsumerTag);
            }
        } catch(IOException e) {
            trace("Couldn't safely cancel the sample " + assetAllocationConsumerTag );
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

                trace("Publishing Asset Allocation request message to Queue:"+ getRequestQueue());
                result.setSamplerData(assetAllocationRequest.toString());
                getChannel().basicPublish("", getRequestQueue(), props, assetAllocationRequest.toByteArray());

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }


}