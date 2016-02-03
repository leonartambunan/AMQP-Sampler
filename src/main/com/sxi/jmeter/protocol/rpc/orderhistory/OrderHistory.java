package com.sxi.jmeter.protocol.rpc.orderhistory;

import com.rabbitmq.client.*;
import id.co.tech.cakra.message.proto.olt.OrderHistoryRequest;
import id.co.tech.cakra.message.proto.olt.OrderHistoryResponse;
import id.co.tech.cakra.message.proto.olt.OrderReleaseInfo;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.testelement.property.TestElementProperty;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class OrderHistory extends AbstractOrderHistory {

    private static final long serialVersionUID = 1L;
    private final static String HEADERS = "AMQPPublisher.Headers";
    private OrderHistoryRequest orderHistoryRequest;
    private transient String orderHistoryConsumerTag;

    private transient CountDownLatch latch = new CountDownLatch(1);

    public boolean makeRequest() throws IOException, InterruptedException, TimeoutException {

        orderHistoryRequest = OrderHistoryRequest
                .newBuilder()
                .setUserId(getMobileUserId())
                .setSessionId(getSessionId())
                .setAccNo(getAccNo())
                .setStartDate(getStartDateAsLong())
                .setEndDate(getEndDateAsLong())
                .build();

            DefaultConsumer consumer = new DefaultConsumer(getChannel()) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    trace(new String(body));

                    OrderHistoryResponse response = OrderHistoryResponse.parseFrom(body);

                    List<OrderReleaseInfo> list = response.getReleaseDirectInfoList();

                    StringBuilder sb = new StringBuilder(10);
                    sb.append(response.toString());

                    for (OrderReleaseInfo ori : list) {
                        sb.append(ori.toString());
                    }

                    result.setResponseMessage(new String(body));
                    result.setResponseData(sb.toString(), null);

                    latch.countDown();
                }
            };

            result.sampleStart();

            trace("Starting basicConsume to ReplyTo Queue: " + getResponseQueue());
            orderHistoryConsumerTag = getChannel().basicConsume(getResponseQueue(), true, consumer);

            new Thread(new OrderHistoryMessagePublisher()).start();

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
            if (orderHistoryConsumerTag != null && getChannel()!=null && getChannel().isOpen()) {
                getChannel().basicCancel(orderHistoryConsumerTag);
            }
        } catch(IOException e) {
            trace("Couldn't safely cancel the sample " + orderHistoryConsumerTag);
        }
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
                trace(e);
            }

        }
    }


}