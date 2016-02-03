package com.sxi.jmeter.protocol.async.neworder;

import com.rabbitmq.client.*;
import id.co.tech.cakra.message.proto.olt.NewOLTOrder;
import id.co.tech.cakra.message.proto.olt.OLTMessage;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.testelement.property.TestElementProperty;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class NewOrder extends AbstractNewOrder {

    private static final long serialVersionUID = 1L;
    private final static String HEADERS = "AMQPPublisher.Headers";
    private transient OLTMessage request;
    private transient String responseTag;
    private transient CountDownLatch latch = new CountDownLatch(1);
    private transient String orderRef = null;
    private transient String bindingQueueName=null;

    public boolean makeRequest() throws InterruptedException, IOException, TimeoutException {

        orderRef = String.valueOf(System.currentTimeMillis());

        NewOLTOrder contentRequest = NewOLTOrder
                .newBuilder()
                .setOrderTime(System.currentTimeMillis())
                .setBuySell(getBuySell())
                .setInputBy(getMobileUserId())
                .setClientCode(getClientCode())
                .setOrdQty(Double.valueOf(getOrderQty()))
                .setOrdPrice(Double.valueOf(getOrderPrice()))
                .setClOrderRef(orderRef)
                .setBoard(getBoard())
                .setStockCode(getStockCode())
                .setTimeInForce(getTimeInForce())
                .setInsvtType(getInvestorType())
                .setOrderTime(System.currentTimeMillis())
                .build();

        request = OLTMessage.newBuilder()
                .setSessionId(getSessionId())
                .setNewOLTOrder(contentRequest)
                .setType(OLTMessage.Type.NEW_OLT_ORDER)
                .build();

        result.setSamplerData(request.toString());

        DefaultConsumer consumer = new DefaultConsumer(getChannel()) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {

                trace("Message received <--");

                OLTMessage response = OLTMessage.parseFrom(body);

                trace(response.toString());

                trace("Type:" + response.getType());

                if (OLTMessage.Type.NEW_OLT_ORDER_REJECT.equals(response.getType())) {
                    trace("Order Rejected");
                    trace(orderRef + " VS " + response.getNewOLTOrderExchangeUpdate().getClOrderRef());
                    if (orderRef.equals(response.getNewOLTOrderReject().getClOrderRef())) {
                        result.setResponseData(response.toString() + '\n' + response.getNewOLTOrderReject().toString(), null);
                        //result.setResponseCodeOK();
                        //result.setSuccessful(true);
                        result.setResponseMessage(response.toString());
                        latch.countDown();
                    }
                } else if (OLTMessage.Type.NEW_OLT_ORDER_EXCHANGE_UPDATE.equals(response.getType())) {
                    trace("Got Update from Exchange");
                    trace(orderRef + " VS " + response.getNewOLTOrderExchangeUpdate().getClOrderRef());
                    if (orderRef.equals(response.getNewOLTOrderExchangeUpdate().getClOrderRef())) {
                        result.setResponseData(response.toString() + '\n' + response.getNewOLTOrderExchangeUpdate().toString(), null);
//                            result.setResponseCodeOK();
//                            result.setSuccessful(true);
                        result.setResponseMessage(response.toString());
                        latch.countDown();
                    }
                } else if (OLTMessage.Type.NEW_OLT_ORDER_ACK.equals(response.getType())) {
                    if (orderRef.equals(response.getNewOLTOrderAck().getClOrderRef())) {
                        trace("Patient. You have to wait");
                    }
                } else {
                    trace("What to do ? Calm, this msg is not your task as new order listener");
                }
            }
        };

        result.sampleStart();

        bindingQueueName = getChannel().queueDeclare().getQueue();

        trace("Listening to Queue [" + bindingQueueName + "] bind to exchange [" + getResponseExchange() + "] with routing key [" + getRoutingKey() + ']');

        getChannel().queueBind(bindingQueueName, getResponseExchange(), getRoutingKey());

        responseTag = getChannel().basicConsume(bindingQueueName, true, consumer);

        new Thread(new NewOrderPublisher()).start();

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

    public void cleanup() {

        try {
            if (responseTag != null && getChannel()!=null && getChannel().isOpen()) {

                getChannel().basicCancel(responseTag);

                if (bindingQueueName!=null) getChannel().queueUnbind(bindingQueueName,getResponseExchange(),getRoutingKey());

            }
        } catch(IOException e) {
            trace("Couldn't safely cancel the sample " + responseTag);
        }
    }

    class NewOrderPublisher implements Runnable {

        @Override
        public void run() {

            try {
                AMQP.BasicProperties props = MessageProperties.MINIMAL_BASIC
                        .builder()
                        .build();

                trace("Publishing New Order request message to Queue:"+ getRequestQueue());

                getChannel().basicPublish("", getRequestQueue(), props, request.toByteArray());

            } catch (Exception e) {
                trace(e);
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