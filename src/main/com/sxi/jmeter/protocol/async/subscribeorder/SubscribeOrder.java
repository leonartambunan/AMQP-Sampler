package com.sxi.jmeter.protocol.async.subscribeorder;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.MessageProperties;
import id.co.tech.cakra.message.proto.olt.MFSubscribeOrder;
import id.co.tech.cakra.message.proto.olt.OLTMessage;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.testelement.property.TestElementProperty;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class SubscribeOrder extends AbstractSubscribeOrder {

    private static final long serialVersionUID = 1L;
    private final static String HEADERS = "AMQPPublisher.Headers";

    OLTMessage request;
    private transient String sendingTag;
    private transient CountDownLatch latch = new CountDownLatch(1);
    private static String orderRef = null;

    public boolean makeRequest()  {

        orderRef = String.valueOf(System.currentTimeMillis());

        MFSubscribeOrder contentRequest = MFSubscribeOrder
                .newBuilder()
                .setAccNo(getAccNo())
                .setSessionId(getSessionId())
                .setCif(getCif())
                .setClnOrderReff(getOrderRef())
                .setOrderDate(System.currentTimeMillis())
                .setProductCode(getProductCode())
                .setProductId(getProductId())
                .setUserId(getMobileUserId())
                .build();

        request = OLTMessage.newBuilder()
                .setSessionId(getSessionId())
                .setMFSubscribeOrder(contentRequest)
                .setType(OLTMessage.Type.MF_SUBSCRIBE_ORDER)
                .build();

        result.setSamplerData(request.toString());

        try {

            initChannel();

            DefaultConsumer consumer = new DefaultConsumer(getChannel()) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {

                    trace("Message received <--");

                    OLTMessage response = OLTMessage.parseFrom(body);

                    trace(response.toString());

                    trace("Type:" + response.getType());

                    if (OLTMessage.Type.MF_UPDATE_ORDER.equals(response.getType())) {
                        trace("MF Order Update");
                        trace(orderRef + " VS " + response.getMFUpdateOrder().getClnOrderReff());
                        if (orderRef.equals(response.getMFUpdateOrder().getClnOrderReff())) {
                            result.setResponseData(response.toString() + '\n' + response.getMFUpdateOrder().toString(), null);
                            result.setResponseCodeOK();
                            result.setSuccessful(true);
                            result.setResponseMessage(response.toString());
                            latch.countDown();
                        }

                    } else {
                        trace("What to do ? Calm, this msg is not your task as new order listener");
                    }

                }
            };

            String bindingQueueName = getChannel().queueDeclare().getQueue();

            trace("Listening to Exchange ["+ getResponseExchange() +"] with Routing Key ["+getRoutingKey()+ ']');

            getChannel().queueBind(bindingQueueName, getResponseExchange(),getRoutingKey());

            sendingTag = getChannel().basicConsume(bindingQueueName,true,consumer);

            new Thread(new MFSubscribeOrderPublisher()).start();

            boolean notZero = latch.await(Long.valueOf(getTimeout()), TimeUnit.MILLISECONDS);

            if (!notZero) {
                throw new Exception("Time out");
            }

        } catch (Exception e) {
            e.printStackTrace();
            trace(e.getMessage());
            result.setResponseCode("400");
            result.setResponseMessage(e.getMessage());
            result.setResponseData(e.getMessage(),null);
        }

        return true;
    }

    public void cleanup() {

        try {
            if (sendingTag != null && getChannel().isOpen()) {
                getChannel().basicCancel(sendingTag);
            }
        } catch(IOException e) {
            trace("Couldn't safely cancel the sample " + sendingTag+ ' ' +  e.getMessage());
        }
        super.cleanup();
    }

    class MFSubscribeOrderPublisher implements Runnable {

        @Override
        public void run() {

            try {
                AMQP.BasicProperties props = MessageProperties.MINIMAL_BASIC
                        .builder()
                        .build();

                trace("Publishing Subscribe Order request message to Exchange:"+ getRequestQueue());

                getChannel().basicPublish("", getRequestQueue(), props, request.toByteArray());

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