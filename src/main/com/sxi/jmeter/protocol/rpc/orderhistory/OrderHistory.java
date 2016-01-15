package com.sxi.jmeter.protocol.rpc.orderhistory;

import com.rabbitmq.client.*;
import id.co.tech.cakra.message.proto.olt.OrderHistoryRequest;
import id.co.tech.cakra.message.proto.olt.OrderHistoryResponse;
import id.co.tech.cakra.message.proto.olt.OrderReleaseInfo;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class OrderHistory extends AbstractOrderHistory {

    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggingManager.getLoggerForClass();
    private final static String HEADERS = "AMQPPublisher.Headers";
    private OrderHistoryRequest orderHistoryRequest;
    private transient String orderHistoryConsumerTag;

    private transient CountDownLatch latch = new CountDownLatch(1);

    public void makeRequest()  {

        orderHistoryRequest = OrderHistoryRequest
                .newBuilder()
                .setUserId(getMobileUserId())
                .setSessionId(getSessionId())
                .setAccNo(getAccNo())
                .setStartDate(getStartDateAsLong())
                .setEndDate(getEndDateAsLong())
                .build();

        try {

            initChannel();

            DefaultConsumer consumer = new DefaultConsumer(getChannel()) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    trace(new String(body));

                    OrderHistoryResponse response = OrderHistoryResponse.parseFrom(body);

                    List<OrderReleaseInfo> list = response.getReleaseDirectInfoList();

                    StringBuilder sb = new StringBuilder();
                    sb.append(response.toString());

                    for (OrderReleaseInfo ori : list) {
                        sb.append(ori.toString());
                    }

                    result.setResponseMessage(new String(body));
                    result.setResponseData(sb.toString(), null);
                    result.setResponseCodeOK();
                    result.setSuccessful(true);

                    latch.countDown();
                }
            };

            trace("Starting basicConsume to ReplyTo Queue: " + getResponseQueue());
            orderHistoryConsumerTag = getChannel().basicConsume(getResponseQueue(), true, consumer);

            new Thread(new OrderHistoryMessagePublisher()).start();

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

    public Arguments getHeaders() {
        return (Arguments) getProperty(HEADERS).getObjectValue();
    }

    public void setHeaders(Arguments headers) {
        setProperty(new TestElementProperty(HEADERS, headers));
    }

    public void cleanup() {

        try {
            if (orderHistoryConsumerTag != null && getChannel()!=null && getChannel().isOpen()) {
                getChannel().basicCancel(orderHistoryConsumerTag);
            }
        } catch(IOException e) {
            trace("Couldn't safely cancel the sample " + orderHistoryConsumerTag);
        }
        super.cleanup();
    }


    class OrderHistoryMessagePublisher implements Runnable {

        @Override
        public void run() {

            try {
                AMQP.BasicProperties props = MessageProperties.MINIMAL_BASIC
                        .builder()
                        .replyTo(getResponseQueue())
                        .build();

                trace("Publishing Order History request message to Queue:"+ getRequestQueue());
                result.setSamplerData(orderHistoryRequest.toString());
                getChannel().basicPublish("", getRequestQueue(), props, orderHistoryRequest.toByteArray());

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }


}