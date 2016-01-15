package com.sxi.jmeter.protocol.rpc.orderinfo;

import com.rabbitmq.client.*;
import id.co.tech.cakra.message.proto.olt.OrderInfoRequest;
import id.co.tech.cakra.message.proto.olt.OrderInfoResponse;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.testelement.property.TestElementProperty;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
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

    public void makeRequest()  {

        correlationID = UUID.randomUUID().toString();

        orderInfoRequest = OrderInfoRequest
                .newBuilder()
                .setUserId(getMobileUserId())
                .setSessionId(getSessionId())
                .setAccNo(getAccNo())
                .build();

        try {

            initChannel();

            DefaultConsumer orderInfoConsumer = new DefaultConsumer(getChannel()) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {

                    trace("Message received <--");
                    trace("["+correlationID +"] <-> ["+properties.getCorrelationId()+"]");

                    trace(new String(body));

                    if (correlationID.equals(properties.getCorrelationId())) {
                        OrderInfoResponse response = null;
                        try {response = OrderInfoResponse.parseFrom(body); } catch(Exception e) {e.printStackTrace();}
                        //result.setResponseMessage(new String(body));
                        result.setResponseData(response==null?new String(body):response.toString(), null);
                        result.setResponseCodeOK();
                        result.setSuccessful(true);
                        latch.countDown();
                    } else {
                        trace("No correlation message");
                    }
                }
            };

            trace("Starting basicConsume to ReplyTo Queue: " + getResponseQueue());

            orderInfoConsumerTag = getChannel().basicConsume(getResponseQueue(), true, orderInfoConsumer);

            new Thread(new OrderInfoMessagePublisher()).start();

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
            trace("Couldn't safely cancel the orderInfoConsumerTag " + orderInfoConsumerTag+ " " +  e.getMessage());
        }

        super.cleanup();

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
                e.printStackTrace();
            }

        }
    }


}