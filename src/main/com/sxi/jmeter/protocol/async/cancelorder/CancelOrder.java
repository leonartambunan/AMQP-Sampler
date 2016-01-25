package com.sxi.jmeter.protocol.async.cancelorder;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.MessageProperties;
import id.co.tech.cakra.message.proto.olt.CancelOLTOrderRequest;
import id.co.tech.cakra.message.proto.olt.OLTMessage;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.testelement.property.TestElementProperty;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class CancelOrder extends AbstractCancelOrder {

    private static final long serialVersionUID = 1L;
    private final static String HEADERS = "AMQPPublisher.Headers";
    OLTMessage request;
    private transient String responseTag;
    private transient CountDownLatch latch = new CountDownLatch(1);
    private static String newOrderRef;

    public boolean makeRequest()  {

        newOrderRef = String.valueOf(System.currentTimeMillis());

        CancelOLTOrderRequest contentRequest = CancelOLTOrderRequest
                .newBuilder()
                .setOldCliOrderRef(getOrderRef())
                .setOrderID(getOrderId())
                .setNewCliOrderRef(newOrderRef)
                .setInputBy(getMobileUserId())
                .build();

        request = OLTMessage
                .newBuilder()
                .setCancelOLTOrderRequest(contentRequest)
                .setType(OLTMessage.Type.CANCEL_OLT_ORDER_REQUEST)
                .setSessionId(getSessionId())
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

                    if (OLTMessage.Type.CANCEL_OLT_ORDER_REJECT.equals(response.getType())) {
                        trace("Order Cancellation Rejected");
                        trace(newOrderRef + " VS " + response.getCancelOLTOrderReject().getNewCliOrderRef());
                        if (newOrderRef.equals(response.getCancelOLTOrderReject().getNewCliOrderRef())) {
                            result.setResponseData(response.toString() + '\n' + response.getCancelOLTOrderReject().toString(), null);
                            result.setResponseCodeOK();
                            result.setSuccessful(true);
                            result.setResponseMessage(response.toString());
                            latch.countDown();
                        }
                    } else if (OLTMessage.Type.CANCEL_OLT_ORDER_ACK.equals(response.getType())) {
                        trace("Order Cancellation Ack");
                        trace(newOrderRef + " VS " + response.getCancelOLTOrderAck().getNewCliOrderRef());
                        if (newOrderRef.equals(response.getCancelOLTOrderAck().getNewCliOrderRef())) {
                            result.setResponseData(response.toString() + '\n' + response.getCancelOLTOrderAck().toString(), null);
                            result.setResponseCodeOK();
                            result.setSuccessful(true);
                            result.setResponseMessage(response.toString());
                            latch.countDown();
                        }
                    } else {
                        trace("What to do ? Calm, that msg is not your task as Cancellation listener");
                    }
                }
            };

            String bindingQueueName = getChannel().queueDeclare().getQueue();

            trace("Listening to Queue ["+ bindingQueueName +"] bind to exchange ["+ getResponseExchange()+"] with routing key ["+getRoutingKey()+ ']');

            getChannel().queueBind(bindingQueueName, getResponseExchange(),getRoutingKey());

            responseTag = getChannel().basicConsume(bindingQueueName,true,consumer);

            new Thread(new CancelOrderPublisher()).start();

            latch.await();

//            boolean notZero = latch.await(Long.valueOf(getTimeout()),TimeUnit.MILLISECONDS);
//
//            if (!notZero) {
//                throw new Exception("Time out");
//            }

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
            if (responseTag != null && getChannel().isOpen()) {
                getChannel().basicCancel(responseTag);
            }
        } catch(IOException e) {
            trace("Couldn't safely cancel the sample " + responseTag + ' ' +e.getMessage());
        }
        super.cleanup();
    }

    class CancelOrderPublisher implements Runnable {

        @Override
        public void run() {

            try {
                AMQP.BasicProperties props = MessageProperties.MINIMAL_BASIC
                        .builder()
                        .build();

                trace("Publishing Cancel Order request message to Queue :"+ getRequestQueue());
                getChannel().basicPublish("",getRequestQueue(),props, request.toByteArray());

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