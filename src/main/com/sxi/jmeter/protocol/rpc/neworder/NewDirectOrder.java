package com.sxi.jmeter.protocol.rpc.neworder;

import com.google.protobuf.InvalidProtocolBufferException;
import com.rabbitmq.client.*;
import id.co.tech.cakra.message.proto.olt.NewOLTOrder;
import id.co.tech.cakra.message.proto.olt.NewOLTOrderAck;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.testelement.property.TestElementProperty;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class NewDirectOrder extends AbstractNewDirectOrder {

    private static final long serialVersionUID = 1L;
    private final static String HEADERS = "AMQPPublisher.Headers";
    private NewOLTOrder request;
    private transient String responseTag;
    private transient CountDownLatch latch = new CountDownLatch(1);

    public boolean makeRequest() throws IOException, InterruptedException, TimeoutException {

        request = NewOLTOrder
                .newBuilder()
                .setOrderTime(System.currentTimeMillis())
                .setBuySell(getBuySell())
                .setInputBy(getMobileUserId())
                .setClientCode(getClientCode())
                .setOrdQty(Double.valueOf(getOrderQty()))
                .setOrdPrice(Double.valueOf(getOrderPrice()))
                .setClOrderRef(String.valueOf(System.currentTimeMillis()))
                .setBoard(getBoard())
                .setStockCode(getStockCode())
                .setTimeInForce(getTimeInForce())
                .setInsvtType(getInvestorType())
                .setOrderTime(System.currentTimeMillis())
                .setOrderPeriod(Long.valueOf(getOrderPeriod()))
                .build();

        DefaultConsumer consumer = new DefaultConsumer(getChannel()) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                trace(new String(body));
                result.setResponseMessage(new String(body));
                NewOLTOrderAck response = null;
                try {response = NewOLTOrderAck.parseFrom(body);} catch (InvalidProtocolBufferException e) {trace(e.getMessage());}
                result.setResponseData((response==null?"":response.toString()), null);
                latch.countDown();
            }
        };

        result.sampleStart();

        trace("Starting basicConsume to ReplyTo Queue: " + getResponseQueue());
        responseTag = getChannel().basicConsume(getResponseQueue(), true, consumer);

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
            if (responseTag != null && getChannel().isOpen()) {
                getChannel().basicCancel(responseTag);
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
                        .replyTo(getResponseQueue())
                        .build();

                trace("Publishing New Order RPC Request to Queue:"+ getRequestQueue());
                result.setSamplerData(request.toString());

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