package com.sxi.jmeter.protocol.async.bookingorder;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.tech.cakra.datafeed.server.df.message.proto.MIMessage;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.testelement.property.TestElementProperty;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class BookingOrder extends AbstractBookingOrder {

    private static final long serialVersionUID = 1L;
    private final static String HEADERS = "AMQPPublisher.Headers";

    private transient String responseTag;
    private transient CountDownLatch latch = new CountDownLatch(1);

    public boolean makeRequest()  {

        try {

            initChannel();

            DefaultConsumer consumer = new DefaultConsumer(getChannel()) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {

                    trace("Message received <--");

                    try {
                        MIMessage response = MIMessage.parseFrom(body);
                        trace(response.toString());

                        result.setStampAndTime(response.getSendingTime(),System.currentTimeMillis()-response.getSendingTime());

                        result.setResponseData(response.toString(),null);

                        result.setSuccessful(true);
                        result.setResponseCodeOK();

                        latch.countDown();

                    } catch(Exception e) {

                        trace(e.toString());
                    }


                }
            };

            String bindingQueueName = getChannel().queueDeclare().getQueue();

            trace("Listening to Queue ["+ bindingQueueName +"] bind to exchange ["+ getExchange()+"] with routing key ["+getRoutingKey()+ ']');

            getChannel().queueBind(bindingQueueName, getExchange(),getRoutingKey());

            responseTag = getChannel().basicConsume(bindingQueueName,true,consumer);

            boolean notZero = latch.await(Long.valueOf(getTimeout()),TimeUnit.MILLISECONDS);

            if (!notZero) {
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
            trace(e.getMessage());
            result.setResponseCode("400");
            result.setResponseMessage(e.getMessage());
            result.setResponseData(e.getMessage(),null);
        }

        cleanup();

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
        super.cleanup();
    }


    public Arguments getHeaders() {
        return (Arguments) getProperty(HEADERS).getObjectValue();
    }

    public void setHeaders(Arguments headers) {
        setProperty(new TestElementProperty(HEADERS, headers));
    }

}