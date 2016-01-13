package com.sxi.jmeter.protocol.async.subscribeorder;

import com.google.protobuf.GeneratedMessage;
import com.rabbitmq.client.*;
import id.co.tech.cakra.message.proto.olt.*;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;

public class SubscribeOrder extends AbstractSubscribeOrder {

    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggingManager.getLoggerForClass();
    private final static String HEADERS = "AMQPPublisher.Headers";

    MFSubscribeOrder request;
    private transient String bindingQueueName;
    private transient String sendingTag;
    private transient CountDownLatch latch = new CountDownLatch(1);
    public void makeRequest()  {

        request = MFSubscribeOrder
                .newBuilder()
                .setAccNo(getAccNo())
                .setSessionId(getSessionId())
                .setCif(getCif())
                .setClnOrderReff(getOrderRef())
                .setOrderDate(System.currentTimeMillis())
                .setProductCode(getProductCode())
                .setProductId(getProductId())
                .build();
        try {

            initChannel();

            DefaultConsumer consumer = new DefaultConsumer(getChannel()) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    trace(new String(body));
                    MFUpdateOrder response = MFUpdateOrder.parseFrom(body);
                    result.setResponseMessage(new String(body));
                    result.setResponseData(response.toString(), null);
                    result.setDataType(SampleResult.TEXT);
                    result.setResponseCodeOK();
                    result.setSuccessful(true);
                    latch.countDown();
                }
            };

            bindingQueueName = getChannel().queueDeclare().getQueue();

            trace("Listening to Exchange ["+getResponseExchangeName() +"] with Routing Key ["+getRoutingKey()+"]");

            getChannel().queueBind(bindingQueueName,getResponseExchangeName(),getRoutingKey());

            sendingTag = getChannel().basicConsume(bindingQueueName,true,consumer);

            new Thread(new AmendOrderPublisher()).start();

            latch.await();

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
            trace("Couldn't safely cancel the sample " + sendingTag+ " " +  e.getMessage());
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

                trace("Publishing Subscribe Order request message to Exchange:"+ getRequestExchangeName());

                getChannel().basicPublish(getRequestExchangeName(), getRoutingKey(), props, request.toByteArray());

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