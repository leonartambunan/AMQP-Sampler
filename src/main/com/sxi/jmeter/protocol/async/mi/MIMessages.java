package com.sxi.jmeter.protocol.async.mi;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.tech.cakra.datafeed.server.df.message.proto.MIMessage;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.property.TestElementProperty;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MIMessages extends AbstractMIMessagesOrder {

    private static final long serialVersionUID = 1L;
    private final static String HEADERS = "AMQPPublisher.Headers";

    private transient String responseTag=null;
    private transient String bindingQueueName=null;
    private transient CountDownLatch latch = new CountDownLatch(1);
    //private SampleResult sampleResult = null;

    public void listen() throws IOException, InterruptedException, TimeoutException {

        sampleResult.setSampleLabel(getName());
        sampleResult.setSuccessful(true);
        sampleResult.setResponseCodeOK();
        sampleResult.setDataType(SampleResult.TEXT);
        sampleResult.setSampleLabel(getTitle());
        sampleResult.setSamplerData("Listen to "+getExchange());

        sampleResult.sampleStart();

        DefaultConsumer consumer = new DefaultConsumer(getChannel()) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {

                trace("Message received");

                try {

                    MIMessage response = MIMessage.parseFrom(body);

                    trace(response.toString());

                    sampleResult.sampleEnd();

//                    sampleResult = SampleResult.createTestSample(response.getSendingTime(),System.currentTimeMillis()-response.getSendingTime());
//                    sampleResult.setTimeStamp(response.getSendingTime());
                    sampleResult.setResponseMessage(response.toString());
                    sampleResult.setResponseData(response.toString(),null);
                    sampleResult.setSuccessful(true);
                    sampleResult.setResponseCodeOK();

                    latch.countDown();

                } catch(Exception e) {

                    trace(e.toString());
                }
            }
        };


        bindingQueueName = getChannel().queueDeclare().getQueue();

        trace("Listening to Queue ["+ bindingQueueName +"] bind to exchange ["+ getExchange()+"] with routing key ["+getRoutingKey()+ ']');

        getChannel().queueBind(bindingQueueName, getExchange(),getRoutingKey());

        responseTag = getChannel().basicConsume(bindingQueueName,true,consumer);

        boolean notZero = latch.await(Long.valueOf(getTimeout()),TimeUnit.MILLISECONDS);

        if (!notZero) {
            throw new TimeoutException("Timeout");
        }
    }

    /**
     * Called by parent class
     */
    public void cleanup() {

        try {
            if (responseTag != null && getChannel()!=null && getChannel().isOpen()) {

                getChannel().basicCancel(responseTag);

                if (bindingQueueName!=null) getChannel().queueUnbind(bindingQueueName,getExchange(),getRoutingKey());
            }
        } catch(IOException e) {
            trace(e);
        }
    }


    public Arguments getHeaders() {
        return (Arguments) getProperty(HEADERS).getObjectValue();
    }

    public void setHeaders(Arguments headers) {
        setProperty(new TestElementProperty(HEADERS, headers));
    }

}