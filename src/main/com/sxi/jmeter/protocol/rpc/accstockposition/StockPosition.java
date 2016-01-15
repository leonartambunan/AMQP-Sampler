package com.sxi.jmeter.protocol.rpc.accstockposition;

import com.rabbitmq.client.*;
import id.co.tech.cakra.message.proto.olt.AccStockPosInfo;
import id.co.tech.cakra.message.proto.olt.AccStockPosRequest;
import id.co.tech.cakra.message.proto.olt.AccStockPosResponse;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class StockPosition extends AbstractStockPosition{

    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggingManager.getLoggerForClass();
    private final static String HEADERS = "AMQPPublisher.Headers";
    private AccStockPosRequest accStockPosRequest;
    private transient String accStockPositionConsumerTag;
    private transient CountDownLatch latch = new CountDownLatch(1);


    public void makeRequest()  {

        accStockPosRequest = AccStockPosRequest
                .newBuilder()
                .setUserId(getMobileUserId())
                .setSessionId(getSessionId())
                .setAccNo(getAccNo())
                .setStockCode(getStockCode())
                .build();

        try {

            initChannel();

            DefaultConsumer consumer = new DefaultConsumer(getChannel()) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    trace(new String(body));
                    result.setResponseMessage(new String(body));

                    AccStockPosResponse response = AccStockPosResponse.parseFrom(body);

                    StringBuilder sb = new StringBuilder().append(response.toString());
                    for (AccStockPosInfo info : response.getAccStockPosInfoList()) {
                        sb.append(info.toString());
                    }

                    result.setResponseData(sb.toString(), null);
                    result.setResponseCodeOK();
                    result.setSuccessful(true);
                    latch.countDown();
                }
            };


            trace("Starting basicConsume to ReplyTo Queue: " + getResponseQueue());
            accStockPositionConsumerTag = getChannel().basicConsume(getResponseQueue(), true, consumer);

            new Thread(new StockPositionMessagePublisher()).start();

            latch.await(Long.valueOf(getTimeout()), TimeUnit.MILLISECONDS);

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

    public void cleanup() {

        try {
            if (accStockPositionConsumerTag != null && getChannel()!=null && getChannel().isOpen()) {
                getChannel().basicCancel(accStockPositionConsumerTag);
            }
        } catch(IOException e) {
            trace("Couldn't safely cancel the sample " + accStockPositionConsumerTag);
        }
        super.cleanup();
    }

    public Arguments getHeaders() {
        return (Arguments) getProperty(HEADERS).getObjectValue();
    }

    public void setHeaders(Arguments headers) {
        setProperty(new TestElementProperty(HEADERS, headers));
    }




    class StockPositionMessagePublisher implements Runnable {

        @Override
        public void run() {

            try {
                AMQP.BasicProperties props = MessageProperties.MINIMAL_BASIC
                        .builder()
                        .replyTo(getResponseQueue())
                        .build();

                trace("Publishing Stock Position request message to Queue:"+ getRequestQueue());
                result.setSamplerData(accStockPosRequest.toString());
                getChannel().basicPublish("", getRequestQueue(), props, accStockPosRequest.toByteArray());

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

}