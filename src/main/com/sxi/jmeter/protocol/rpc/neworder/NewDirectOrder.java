package com.sxi.jmeter.protocol.rpc.neworder;

import com.google.protobuf.InvalidProtocolBufferException;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.MessageProperties;
import id.co.tech.cakra.message.proto.olt.NewOLTOrder;
import id.co.tech.cakra.message.proto.olt.NewOLTOrderAck;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class NewDirectOrder extends AbstractNewDirectOrder {

    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggingManager.getLoggerForClass();
    private final static String HEADERS = "AMQPPublisher.Headers";
    NewOLTOrder request;
    private transient String responseTag;
    private transient CountDownLatch latch = new CountDownLatch(1);

    public void makeRequest()  {

        request = NewOLTOrder
                .newBuilder()
                .setOrderTime(System.currentTimeMillis())
                .setBuySell(getBuySell())
                .setInputBy(getMobileUserId())
                .setClientCode(getClientCode())
                .setOrdQty(Double.valueOf(getOrderQty()))
                .setOrdPrice(Double.valueOf(getOrderPrice()))
                .setClOrderRef(""+System.currentTimeMillis())
                .setBoard(getBoard())
                .setStockCode(getStockCode())
                .setTimeInForce(getTimeInForce())
                .setInsvtType(getInvestorType())
                .setOrderTime(System.currentTimeMillis())
                .setOrderPeriod(Long.valueOf(getOrderPeriod()))
                .build();
        try {

            initChannel();

            DefaultConsumer consumer = new DefaultConsumer(getChannel()) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    trace(new String(body));
                    result.setResponseMessage(new String(body));
                    NewOLTOrderAck response = null;
                    try {response = NewOLTOrderAck.parseFrom(body);} catch (InvalidProtocolBufferException e) {trace(e.getMessage());}
                    result.setResponseData((response==null?"":response.toString()), null);
                    result.setResponseCodeOK();
                    result.setSuccessful(true);
                    latch.countDown();
                }
            };

            trace("Starting basicConsume to ReplyTo Queue: " + getResponseQueue());
            responseTag = getChannel().basicConsume(getResponseQueue(), true, consumer);

            new Thread(new NewOrderPublisher()).start();

            latch.await(Long.valueOf(getTimeout()),TimeUnit.MILLISECONDS);

        } catch (Exception e) {
            e.printStackTrace();
            trace(e.getMessage());
            result.setResponseCode("400");
            result.setResponseMessage(e.getMessage());
            interrupt();
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
                        .replyTo(getResponseQueue())
                        .build();

                trace("Publishing New Order RPC Request to Queue:"+ getRequestQueue());
                result.setSamplerData(request.toString());
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