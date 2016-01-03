package com.sxi.jmeter.protocol.amqp;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.QueueingConsumer.Delivery;
import com.rabbitmq.client.ShutdownSignalException;
import id.co.tech.cakra.message.proto.olt.LogonResponse;
import id.co.tech.cakra.message.proto.olt.OrderInfoResponse;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.Interruptible;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeoutException;

public class PreOpening extends AbstractPreOpeningSampler implements Interruptible, TestStateListener {

    private static final long serialVersionUID = 7480863561320459099L;

    private static final Logger log = LoggingManager.getLoggerForClass();

    private static final String RECEIVE_TIMEOUT = "AMQPConsumer.ReceiveTimeout";

    //    private final static String MESSAGE_ROUTING_KEY = "AMQPPublisher.MessageRoutingKey";
    private final static String HEADERS = "AMQPPublisher.Headers";
    private static final String POSITIVE_LOGON_STATUS = "Success";

    private transient Channel channel;
    private transient QueueingConsumer loginConsumer;
    private transient QueueingConsumer orderStatusConsumer;
    private transient String loginConsumerTag;
    private boolean preOpeningProcessStarted;

    @Override
    public SampleResult sample(Entry entry) {

        SampleResult result = new SampleResult();
        result.setSampleLabel(getName());
        result.setSuccessful(false);
        result.setResponseCode("500");

        result.setSamplerData("User:"+getMobileUserid()+", Password:"+getMobilePassword()+",DeviceId:"+ getMobileDeviceId()+",DeviceType:"+getMobileType()+",AppVersion:"+getMobileAppVersion());

        trace("Trimegah RPC Market Info sample()");

        try {

            initChannel();

            if (loginConsumer == null) {
                log.info("Creating rpc login Consumer");
                loginConsumer = new QueueingConsumer(channel);
            }

            log.info("Starting basic login Consumer.. Queue:"+ getLoginQueue());

            loginConsumerTag = channel.basicConsume(getLoginQueue(), true, loginConsumer);


        } catch (Exception ex) {
            log.error("Failed to initialize channel", ex);
            result.setResponseMessage(ex.toString());
            return result;
        }

        result.setSampleLabel(getTitle());


        Delivery loginDelivery;
        Delivery orderResponseDelivery;

        try {

            loginDelivery = loginConsumer.nextDelivery(getReceiveTimeoutAsInt());

            if(loginDelivery == null){
                result.setResponseMessage("loginDelivery timed out");
                return result;
            }

            LogonResponse logonResponse = LogonResponse.parseFrom(loginDelivery.getBody());

            if (POSITIVE_LOGON_STATUS.equals(logonResponse.getStatus())) {

                //TODO SEND ORDER REQUEST HERE

                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(getScheduleHour()));
                calendar.set(Calendar.MINUTE, Integer.parseInt(getScheduleMinute()));
                calendar.set(Calendar.SECOND, Integer.parseInt(getScheduleSecond()));
                Date time = calendar.getTime();

                Timer timer = new Timer();
                ScheduledExecutionTask thread = new ScheduledExecutionTask(timer);
                timer.schedule(thread, time);

                for(;;) {
                    Thread.sleep(50);
                    if (preOpeningProcessStarted) {
                        result.sampleStart();
                        break;
                    }
                }


                orderStatusConsumer = new QueueingConsumer(channel);

                log.info("Starting basic order Info .. Queue:"+getOrderResponseQueue());

                channel.basicConsume(getOrderResponseQueue(),true,orderStatusConsumer);

                orderResponseDelivery = orderStatusConsumer.nextDelivery(getReceiveTimeoutAsInt());

                if(orderResponseDelivery == null){
                    result.setResponseMessage("Order timed out");
                    return result;
                }

                OrderInfoResponse stockTradeInfo= OrderInfoResponse.parseFrom(orderResponseDelivery.getBody());

                result.setResponseMessage(stockTradeInfo.toString());

                result.setResponseData(stockTradeInfo.toString(), null);

                result.setDataType(SampleResult.TEXT);

                result.setResponseCodeOK();

                result.setSuccessful(true);

            }

        } catch (ShutdownSignalException e) {
            e.printStackTrace();
            loginConsumer = null;
            loginConsumerTag = null;
            log.warn("AMQP RPC Market Info loginConsumer failed to consume", e);
            result.setResponseCode("400");
            result.setResponseMessage(e.getMessage());
            interrupt();
        } catch (ConsumerCancelledException e) {
            e.printStackTrace();
            loginConsumer = null;
            loginConsumerTag = null;
            log.warn("AMQP RPC Market Info loginConsumer failed to consume", e);
            result.setResponseCode("300");
            result.setResponseMessage(e.getMessage());
            interrupt();
        } catch (InterruptedException e) {
            e.printStackTrace();
            loginConsumer = null;
            loginConsumerTag = null;
            log.info("interuppted while attempting to consume RPC Market Info ");
            result.setResponseCode("200");
            result.setResponseMessage(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            loginConsumer = null;
            loginConsumerTag = null;
            log.warn("AMQP RPC Market Info loginConsumer failed to consume", e);
            result.setResponseCode("100");
            result.setResponseMessage(e.getMessage());
        } finally {
            result.sampleEnd();
        }

        trace("AMQP Market Info sample ended");

        return result;
    }

    @Override
    protected Channel getChannel() {
        return channel;
    }

    @Override
    protected void setChannel(Channel channel) {
        this.channel = channel;
    }

    protected int getReceiveTimeoutAsInt() {
        if (getPropertyAsInt(RECEIVE_TIMEOUT) < 1) {
            return DEFAULT_TIMEOUT;
        }
        return getPropertyAsInt(RECEIVE_TIMEOUT);
    }

//    public String getReceiveTimeout() {
//        return getPropertyAsString(RECEIVE_TIMEOUT, DEFAULT_TIMEOUT_STRING);
//    }
//
//    public void setReceiveTimeout(String s) {
//        setProperty(RECEIVE_TIMEOUT, s);
//    }

    public Arguments getHeaders() {
        return (Arguments) getProperty(HEADERS).getObjectValue();
    }

    public void setHeaders(Arguments headers) {
        setProperty(new TestElementProperty(HEADERS, headers));
    }

    @Override
    public boolean interrupt() {
        testEnded();
        return true;
    }

    @Override
    public void testEnded() {

    }

    @Override
    public void testEnded(String arg0) {

    }

    @Override
    public void testStarted() {

    }

    @Override
    public void testStarted(String arg0) {

    }

    public void cleanup() {

        try {
            if (loginConsumerTag != null) {
                channel.basicCancel(loginConsumerTag);
            }
        } catch(IOException e) {
            log.error("Couldn't safely cancel the sample " + loginConsumerTag, e);
        }

        super.cleanup();

    }

    protected boolean initChannel() throws IOException, NoSuchAlgorithmException, KeyManagementException, TimeoutException {
        boolean ret = super.initChannel();
        channel.basicQos(2);
        return ret;
    }

    private void trace(String s) {
        String tl = getTitle();
        String tn = Thread.currentThread().getName();
        String th = this.toString();
        log.debug(tn + " " + tl + " " + s + " " + th);
    }

    class ScheduledExecutionTask extends TimerTask {
        Timer timer;
        public ScheduledExecutionTask(Timer timer) {
            this.timer=timer;
        }

        public void run() {
            System.out.format("Preopening Process starts");
            preOpeningProcessStarted = true;
            timer.cancel(); //Terminate the timer thread
        }
    }

}