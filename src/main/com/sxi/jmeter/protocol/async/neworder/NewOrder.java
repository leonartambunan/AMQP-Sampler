package com.sxi.jmeter.protocol.async.neworder;

import com.rabbitmq.client.*;
import id.co.tech.cakra.message.proto.olt.NewOLTOrder;
import id.co.tech.cakra.message.proto.olt.OLTMessage;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.property.TestElementProperty;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class NewOrder extends AbstractNewOrder {

    private static final long serialVersionUID = 1L;
    private final static String HEADERS = "AMQPPublisher.Headers";
    OLTMessage request;
    private transient String bindingQueueName;
    private transient String responseTag;
    private transient CountDownLatch latch = new CountDownLatch(1);
    private final String orderRef = String.valueOf(System.currentTimeMillis());

    public void makeRequest()  {

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

        try {

            initChannel();

            DefaultConsumer consumer = new DefaultConsumer(getChannel()) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {

                    trace("Message received <--");

                    trace(new String(body));

                    result.setResponseMessage(new String(body));

                    OLTMessage response = OLTMessage.parseFrom(body);

                    if (OLTMessage.Type.NEW_OLT_ORDER_REJECT.equals(response.getType())) {

                        if (orderRef.equals(response.getNewOLTOrderReject().getClOrderRef())) {
                            result.setResponseData(response.toString() + "\n" + response.getNewOLTOrderReject().toString(), null);
                            result.setDataType(SampleResult.TEXT);
                            result.setResponseCodeOK();
                            result.setSuccessful(true);
                            latch.countDown();
                        }

                    } else {

                        if (orderRef.equals(response.getNewOLTOrderAck().getClOrderRef())) {
                            result.setResponseData(response.toString() + "\n" + response.getNewOLTOrderAck().toString(), null);
                            result.setDataType(SampleResult.TEXT);
                            result.setResponseCodeOK();
                            result.setSuccessful(true);
                            latch.countDown();
                        }

                    }
                }
            };

            bindingQueueName = getChannel().queueDeclare().getQueue();

            trace("Listening to Queue ["+bindingQueueName +"] bind to exchange ["+ getResponseExchange()+"] with routing key ["+getRoutingKey()+"]");

            getChannel().queueBind(bindingQueueName, getResponseExchange(),getRoutingKey());

            responseTag = getChannel().basicConsume(bindingQueueName,true,consumer);

            new Thread(new NewOrderPublisher()).start();

            latch.await(Long.valueOf(getTimeout()),TimeUnit.MILLISECONDS);


//            if (timeout) {
//                result.setResponseMessage("Response Time out. Exceed "+getTimeout());
//            }

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
            if (responseTag != null && getChannel().isOpen()) {
                getChannel().basicCancel(responseTag);
            }
        } catch(IOException e) {
            trace("Couldn't safely cancel the sample " + responseTag);
        }
        super.cleanup();
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