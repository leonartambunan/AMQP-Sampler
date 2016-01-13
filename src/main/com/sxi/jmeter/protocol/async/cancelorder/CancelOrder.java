package com.sxi.jmeter.protocol.async.cancelorder;

import com.google.protobuf.GeneratedMessage;
import com.rabbitmq.client.*;
import id.co.tech.cakra.message.proto.olt.CancelOLTOrderAck;
import id.co.tech.cakra.message.proto.olt.CancelOLTOrderReject;
import id.co.tech.cakra.message.proto.olt.CancelOLTOrderRequest;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.property.TestElementProperty;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CancelOrder extends AbstractCancelOrder {

    private static final long serialVersionUID = 1L;
    private final static String HEADERS = "AMQPPublisher.Headers";
    CancelOLTOrderRequest request;
    private transient String bindingQueueName;
    private transient String sendingTag;
    private transient CountDownLatch latch = new CountDownLatch(1);

    public void makeRequest()  {

        request = CancelOLTOrderRequest
                .newBuilder()
                .setNewCliOrderRef(""+System.currentTimeMillis())
                .setOldCliOrderRef(getOrderRef())
                .setInputBy("JMETER")
                .setOrderID(getOrderId())
                .build();
        try {

            initChannel();

            DefaultConsumer consumer = new DefaultConsumer(getChannel()) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    trace(new String(body));

                    GeneratedMessage response;
                    try {
                        response = CancelOLTOrderAck.parseFrom(body);
                    } catch (Exception e) {
                        response = CancelOLTOrderReject.parseFrom(body);
                    }

                    result.setResponseMessage(new String(body));
                    result.setResponseData(response.toString(), null);
                    result.setDataType(SampleResult.TEXT);
                    result.setResponseCodeOK();
                    result.setSuccessful(true);
                    latch.countDown();
                }
            };

            bindingQueueName = getChannel().queueDeclare().getQueue();

            trace("Listening to Exchange ["+bindingQueueName +"] with Routing Key ["+getMobileUserId()+"]");

            getChannel().queueBind(bindingQueueName, getResponseExchange(),getMobileUserId());

            sendingTag = getChannel().basicConsume(bindingQueueName,true,consumer);

            new Thread(new CancelOrderPublisher()).start();

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
            if (sendingTag != null && getChannel().isOpen()) {
                getChannel().basicCancel(sendingTag);
            }
        } catch(IOException e) {
            trace("Couldn't safely cancel the sample " + sendingTag +" " +e.getMessage());
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