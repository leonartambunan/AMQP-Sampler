package com.sxi.jmeter.protocol.async.amendorder;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.MessageProperties;
import id.co.tech.cakra.message.proto.olt.AmendOLTOrderRequest;
import id.co.tech.cakra.message.proto.olt.OLTMessage;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.testelement.property.TestElementProperty;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class AmendOrder extends AbstractAmendOrder {

    private static final long serialVersionUID = 1L;
    private final static String HEADERS = "AMQPPublisher.Headers";

    OLTMessage request;
    private transient String listeningTag;
    private transient CountDownLatch latch = new CountDownLatch(1);
    private static String newOrderRef;

    public boolean makeRequest()  {

        newOrderRef = String.valueOf(System.currentTimeMillis());

        AmendOLTOrderRequest contentRequest = AmendOLTOrderRequest
                .newBuilder()
                .setNewCliOrderRef(newOrderRef)
                .setOldCliOrderRef(getOrderRef())
                .setInputBy(getMobileUserId())
                .setOrderID(getOrderId())
                .setNewOrdPeriod(getOrderPeriodAsDate().getTime())
                .setNewQty(Double.valueOf(getOrderQty()))
                .setNewPrice(Double.valueOf(getOrderPrice()))
                .build();

        request = OLTMessage
                .newBuilder()
                .setAmendOLTOrderRequest(contentRequest)
                .setType(OLTMessage.Type.AMEND_OLT_ORDER_REQUEST)
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

                    if (OLTMessage.Type.AMEND_OLT_ORDER_REJECT.equals(response.getType())) {
                        trace("Order Amendment Rejected");
                        trace(newOrderRef + " VS " + response.getAmendOLTOrderReject().getNewCliOrderRef());
                        if (newOrderRef.equals(response.getAmendOLTOrderReject().getNewCliOrderRef())) {
                            result.setResponseData(response.toString() + '\n' + response.getAmendOLTOrderReject().toString(), null);
                            result.setResponseCodeOK();
                            result.setSuccessful(true);
                            result.setResponseMessage(response.toString());
                            latch.countDown();
                        }
                    } else if (OLTMessage.Type.AMEND_OLT_ORDER_ACK.equals(response.getType())) {
                        trace("Order Amendment Ack");
                        trace(newOrderRef + " VS " + response.getAmendOLTOrderAck().getNewCliOrderRef());
                        if (newOrderRef.equals(response.getAmendOLTOrderAck().getNewCliOrderRef())) {
                            result.setResponseData(response.toString() + '\n' + response.getAmendOLTOrderAck().toString(), null);
                            result.setResponseCodeOK();
                            result.setSuccessful(true);
                            result.setResponseMessage(response.toString());
                            latch.countDown();
                        }
                    } else {
                        trace("What to do ? Calm, that msg is not your ask as amendment listener");
                    }
                }
            };

            String bindingQueueName = getChannel().queueDeclare().getQueue();

            trace("Listening to Exchange ["+ getResponseExchange() +"] with Routing Key ["+getRoutingKey()+ ']');

            getChannel().queueBind(bindingQueueName, getResponseExchange(),getRoutingKey());

            listeningTag = getChannel().basicConsume(bindingQueueName,true,consumer);

            new Thread(new AmendOrderPublisher()).start();

            latch.await();

//            boolean notZero = latch.await(Long.valueOf(getTimeout()), TimeUnit.MILLISECONDS);
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
            if (listeningTag != null && getChannel().isOpen()) {
                getChannel().basicCancel(listeningTag);
            }
        } catch(IOException e) {
            trace("Couldn't safely cancel the sample " + listeningTag + ' ' +  e.getMessage());
        }
        super.cleanup();
    }

    class AmendOrderPublisher implements Runnable {

        @Override
        public void run() {

            try {
                AMQP.BasicProperties props = MessageProperties.MINIMAL_BASIC
                        .builder()
                        .build();

                trace("Publishing Amend Order request message to Exchange:"+ getRequestQueue());

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