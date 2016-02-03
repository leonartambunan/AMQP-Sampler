package com.sxi.jmeter.protocol.rpc.orderinfo;

import com.rabbitmq.client.*;
import id.co.tech.cakra.message.proto.olt.OrderInfoRequest;
import id.co.tech.cakra.message.proto.olt.OrderInfoResponse;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.testelement.property.TestElementProperty;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class OrderInfo extends AbstractOrderInfo {

    private static final long serialVersionUID = 1L;

    private OrderInfoRequest orderInfoRequest;

    private transient String orderInfoConsumerTag;

    private transient CountDownLatch latch = new CountDownLatch(1);

    private static String correlationID;

    public boolean makeRequest() throws TimeoutException, InterruptedException, IOException {

        correlationID = UUID.randomUUID().toString();

        orderInfoRequest = OrderInfoRequest
                .newBuilder()
                .setUserId(getMobileUserId())
                .setSessionId(getSessionId())
                .setAccNo(getAccNo())
                .build();


        DefaultConsumer orderInfoConsumer = new DefaultConsumer(getChannel()) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {

                trace("Message received <--");

                trace('[' +correlationID +"] <-> ["+properties.getCorrelationId()+ ']');

                trace(new String(body));

                if (correlationID.equals(properties.getCorrelationId())) {

                    OrderInfoResponse response = null;

                    try {response = OrderInfoResponse.parseFrom(body); } catch(Exception e) {
                        trace(e);
                    }
                    result.setResponseMessage(new String(body));
                    result.setResponseData((response==null?new String(body):response.toString()), null);
                    latch.countDown();

                } else {
                    trace("No correlation message");
                }
            }
        };

        result.sampleStart(); //STARTER

        trace("Starting basicConsume to ReplyTo Queue: " + getResponseQueue());

        orderInfoConsumerTag = getChannel().basicConsume(getResponseQueue(), true, orderInfoConsumer);

        new Thread(new OrderInfoMessagePublisher()).start();

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

    private final static String HEADERS = "AMQPPublisher.Headers";
    public Arguments getHeaders() {
        return (Arguments) getProperty(HEADERS).getObjectValue();
    }
    public void setHeaders(Arguments headers) {
        setProperty(new TestElementProperty(HEADERS, headers));
    }

    public void cleanup() {

        try {
            if (orderInfoConsumerTag != null && getChannel()!=null && getChannel().isOpen()) {
                getChannel().basicCancel(orderInfoConsumerTag);
            }
        } catch(IOException e) {
            trace("Couldn't safely cancel the orderInfoConsumerTag " + orderInfoConsumerTag+ ' ' +  e.getMessage());
        }


    }


    class OrderInfoMessagePublisher implements Runnable {

        @Override
        public void run() {

            try {
                AMQP.BasicProperties props = MessageProperties.MINIMAL_BASIC
                        .builder()
                        .replyTo(getResponseQueue())
                        .correlationId(correlationID)
                        .build();

                trace("Publishing order info request message to Queue:"+ getRequestQueue());

                result.setSamplerData(orderInfoRequest.toString());

                getChannel().basicPublish("", getRequestQueue(), props, orderInfoRequest.toByteArray());

            } catch (Exception e) {
                trace(e);
            }

        }
    }


}