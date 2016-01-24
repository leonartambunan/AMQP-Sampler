package com.sxi.jmeter.protocol.rpc.marketinfo;

import com.rabbitmq.client.*;
import com.rabbitmq.client.QueueingConsumer.Delivery;
import com.sxi.jmeter.protocol.rpc.constants.Trimegah;
import com.tech.cakra.datafeed.server.df.message.proto.MIMessage;
import id.co.tech.cakra.message.proto.olt.LogonRequest;
import id.co.tech.cakra.message.proto.olt.LogonResponse;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.Interruptible;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.testelement.property.TestElementProperty;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MarketInfo extends AbstractMarketInfo implements Interruptible, TestStateListener {

    private static final long serialVersionUID = 7480863561320459099L;

    private static final String RECEIVE_TIMEOUT = "AMQPConsumer.ReceiveTimeout";
    private static final String POSITIVE_LOGON_STATUS = "OK";

    private transient Channel channel;
    private transient QueueingConsumer loginConsumer;
    private transient String loginConsumerTag=null;
    private transient String marketInfoConsumerTag=null;
    private transient String marketInfoBindingQueueName;

    private LogonRequest logonRequest;

    private transient CountDownLatch scheduleLatch = new CountDownLatch(1);

    private transient CountDownLatch messageLatch = new CountDownLatch(1);

    protected static boolean SCHEDULE_STARTED = false;

    @Override
    public SampleResult sample(final Entry entry) {

        trace("sample()");

        try {
            logonRequest = LogonRequest
                    .newBuilder()
                    .setUserId(getMobileUserId())
                    .setPassword(getMobilePassword())
                    .setDeviceId(getMobileDeviceId())
                    .setDeviceType(getMobileType())
                    .setAppVersion(getMobileAppVersion())
                    .setIp(InetAddress.getLocalHost().getHostAddress())
                    .build();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        final SampleResult result = new SampleResult();
        result.setSampleLabel(getName());
        result.setSuccessful(false);
        result.setResponseCode("500");
        result.setSampleLabel(getTitle());
        result.setSamplerData(constructNiceString());
        result.setDataType(SampleResult.TEXT);

        result.sampleStart();

        result.samplePause();

        try {

            initChannel();

            if (loginConsumer == null) {
                trace("Creating rpc login consumer");
                loginConsumer = new QueueingConsumer(channel);
            }

            trace("Starting basicConsume to Login ReplyTo Queue:"+ getReplyToQueue());

            loginConsumerTag = channel.basicConsume(getReplyToQueue(), true, loginConsumer);

            Thread senderThread = new Thread(new LoginMessagePublisher());

            senderThread.start();

            Delivery loginDelivery;

            loginDelivery = loginConsumer.nextDelivery(getReceiveTimeoutAsInt());

            if(loginDelivery == null){
                throw new Exception("loginDelivery timed out");
            }

            LogonResponse logonResponse = LogonResponse.parseFrom(loginDelivery.getBody());

            System.out.println(logonResponse.toString());

            if (POSITIVE_LOGON_STATUS.equals(logonResponse.getStatus())) {

                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(getScheduleHour()));
                calendar.set(Calendar.MINUTE, Integer.parseInt(getScheduleMinute()));
                calendar.set(Calendar.SECOND, Integer.parseInt(getScheduleSecond()));

                calendar.add(Calendar.SECOND,Integer.parseInt(getScheduleDelay()));

                Date time = calendar.getTime();

                Timer timer = new Timer();

                ScheduledExecutionTask thread = new ScheduledExecutionTask(timer, scheduleLatch);
                timer.schedule(thread, time);

//                trace("Waiting for Schedule to start");
//                scheduleLatch.await();
//                trace("SCHEDULE STARTED");
//                SCHEDULE_STARTED = true;
//                result.sampleResume();

                marketInfoBindingQueueName = channel.queueDeclare().getQueue();

                channel.queueBind(marketInfoBindingQueueName,getMarketInfoExchange(),getRoutingKey());

                DefaultConsumer marketInfoConsumer = new DefaultConsumer(channel) {
                    @Override
                    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {

                        if (SCHEDULE_STARTED) {

                            trace("Message received with tag: [" + consumerTag + "] ");

                            result.setResponseMessage(new String(body));

                            try {
                                MIMessage response = MIMessage.parseFrom(body);
                                result.setResponseData(response.toString(), null);
                            } catch (Exception e) {
                                result.setResponseData(new String(body), null);
                            }

                            result.setResponseCodeOK();

                            result.setSuccessful(true);

                            messageLatch.countDown();

                            cleanup();

                        } else {
                            trace("Message received but schedule is not yet started");
                        }
                    }
                };

                trace("Subscribe to "+getMarketInfoExchange());

                marketInfoConsumerTag = channel.basicConsume(marketInfoBindingQueueName,true, marketInfoConsumer);

                trace("Waiting for Schedule to start");
                scheduleLatch.await();
                trace("Schedule started");
                SCHEDULE_STARTED = true;
                result.sampleResume();

                boolean reachZero = messageLatch.await(Long.valueOf(getTimeout()),TimeUnit.MILLISECONDS);

                if (!reachZero) {
//                    throw new Exception("Time out while waiting for market info message occurred");
                    return null;
                }

            } else {
                throw new Exception("Login Failed." + '\n' +logonResponse.toString(),null);
            }

        } catch (Exception e) {
            e.printStackTrace();
            loginConsumer = null;
            trace(e.getMessage());
            result.setResponseCode("400");
            result.setResponseMessage("Exception: "+e.getMessage());
            result.setResponseData("Exception: "+e.getMessage(),null);
        } finally {

            if (!SCHEDULE_STARTED) {
                result.sampleResume();
            }

            result.sampleEnd();
        }

        trace("sample() ended");

        cleanup();

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
            return Integer.parseInt(Trimegah.TIMEOUT);
        }
        return getPropertyAsInt(RECEIVE_TIMEOUT);
    }

    private final static String HEADERS = "AMQPPublisher.Headers";
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
            Thread.sleep(100 + Math.round(100*Math.random()));
        } catch (Exception e) {}

        try {
            if (loginConsumerTag != null) {
                channel.basicCancel(loginConsumerTag);
                loginConsumerTag=null;
            }

            if (marketInfoConsumerTag !=null) {
                channel.basicCancel(marketInfoConsumerTag);
                marketInfoConsumerTag=null;
            }

            if (marketInfoBindingQueueName != null) {
                channel.queueUnbind(marketInfoBindingQueueName,getMarketInfoExchange(),getRoutingKey());
                marketInfoBindingQueueName=null;
            }

        } catch(Exception e) {
            trace(e.getMessage());
        }

        super.cleanup();

    }

    protected boolean initChannel() throws IOException, NoSuchAlgorithmException, KeyManagementException, TimeoutException {
        boolean ret = super.initChannel();
        channel.basicQos(2);
        return ret;
    }

    private String constructNiceString() {

        return "---MQ SERVER---" +
                "\nIP \t:" + getHost() +
                "\nPort\t:" + getPort() +
                "\nUsername\t:" + getUsername() +
                "\nPassword\t:" + getPassword() +
                "\nVirtual Host\t:" + getVirtualHost() +
                "\n----------" + "\n---REQUEST---\n" +
                logonRequest.toString();

    }

    class LoginMessagePublisher implements Runnable {

        @Override
        public void run() {

            try {
                AMQP.BasicProperties props = MessageProperties.MINIMAL_BASIC
                        .builder()
                        .replyTo(getReplyToQueue())
                        .build();

                trace("Publishing login request message to Queue:"+getLoginQueue());

                channel.basicPublish("", getLoginQueue(), props, logonRequest.toByteArray());

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    class ScheduledExecutionTask extends TimerTask {

        Timer timer;
        CountDownLatch latch;

        public ScheduledExecutionTask(Timer timer,CountDownLatch latch) {
            this.timer=timer;
            this.latch=latch;
        }

        public void run() {

            latch.countDown();

            timer.cancel(); //Terminate the timer thread
        }
    }


}