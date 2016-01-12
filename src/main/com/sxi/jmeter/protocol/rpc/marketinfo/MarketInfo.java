package com.sxi.jmeter.protocol.rpc.marketinfo;

import com.rabbitmq.client.*;
import com.rabbitmq.client.QueueingConsumer.Delivery;
import com.sxi.jmeter.protocol.rpc.constants.Trimegah;
import id.co.tech.cakra.message.proto.olt.LogonRequest;
import id.co.tech.cakra.message.proto.olt.LogonResponse;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.Interruptible;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

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

    private static final Logger log = LoggingManager.getLoggerForClass();

    private static final String RECEIVE_TIMEOUT = "AMQPConsumer.ReceiveTimeout";

    private final static String HEADERS = "AMQPPublisher.Headers";
    private static final String POSITIVE_LOGON_STATUS = "OK";

    private transient Channel channel;
    private transient QueueingConsumer loginConsumer;
    private transient DefaultConsumer marketInfoConsumer;
    private transient String loginConsumerTag;
    private transient String marketInfoBindingQueueName;

    private LogonRequest logonRequest;

    private transient CountDownLatch scheduleLatch = new CountDownLatch(1);
    private transient CountDownLatch messageLatch = new CountDownLatch(1);

    @Override
    public SampleResult sample(Entry entry) {

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

        trace("Trimegah Market Info sample()");

        try {

            initChannel();

            if (loginConsumer == null) {
                log.info("Creating rpc login consumer");
                loginConsumer = new QueueingConsumer(channel);
            }

            log.info("Starting basicConsume to Login ReplyTo Queue:"+ getReplyToQueue());

            loginConsumerTag = channel.basicConsume(getReplyToQueue(), true, loginConsumer);

        } catch (Exception ex) {
            ex.printStackTrace();
            log.error("Failed to initialize channel", ex);
            result.setResponseMessage(ex.toString());
            result.setResponseData(ex.getLocalizedMessage(),null);
            return result;
        }

        Thread senderThread = new Thread(new LoginMessagePublisher());

        senderThread.start();

        Delivery loginDelivery;

        try {

            loginDelivery = loginConsumer.nextDelivery(getReceiveTimeoutAsInt());

            if(loginDelivery == null){
                result.setResponseMessage("loginDelivery timed out");
                return result;
            }

            LogonResponse logonResponse = LogonResponse.parseFrom(loginDelivery.getBody());

            System.out.println(logonResponse.toString());

            if (POSITIVE_LOGON_STATUS.equals(logonResponse.getStatus())) {

                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(getScheduleHour()));
                calendar.set(Calendar.MINUTE, Integer.parseInt(getScheduleMinute()));
                calendar.set(Calendar.SECOND, Integer.parseInt(getScheduleSecond()));
                Date time = calendar.getTime();

                Timer timer = new Timer();
                ScheduledExecutionTask thread = new ScheduledExecutionTask(timer, scheduleLatch);
                timer.schedule(thread, time);

                log.info("WAITING FOR SCHEDULE TO START");

                scheduleLatch.await();

                //START LISTEN TO MARKET INFO

                marketInfoBindingQueueName = channel.queueDeclare().getQueue();

                channel.queueBind(marketInfoBindingQueueName,getMarketInfoExchange(),getRoutingKey());

                result.sampleStart();

                marketInfoConsumer = new DefaultConsumer(channel) {
                    @Override
                    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                        log.info(new String(body));

//                        Builder msgBuilder = DFMessage.newBuilder();
//                        msgBuilder.mergeFrom(body);
//                        DFMessage data = msgBuilder.build();
//                        TradeDetailData tradeDetailData = data.getTradeDetailData();

                        result.setResponseMessage(new String(body));

                        result.setResponseData(new String(body), null);

                        result.setDataType(SampleResult.BINARY);

                        result.setResponseCodeOK();

                        result.setSuccessful(true);

                        messageLatch.countDown();
                    }
                };

                channel.basicConsume(marketInfoBindingQueueName,true,marketInfoConsumer);

                boolean reachZero = messageLatch.await(Long.valueOf(getTimeout()),TimeUnit.MILLISECONDS);

                if (!reachZero) {
                    result.setResponseMessage("Time out occurred");
                    result.setResponseData("Time out occurred", null);
                    result.setDataType(SampleResult.TEXT);
                }
            } else {
                result.setResponseData("Login Failed",null);
                result.setResponseMessage("Login Failed");
            }

        } catch (ShutdownSignalException e) {
            e.printStackTrace();
            loginConsumer = null;
            loginConsumerTag = null;
            log.warn(e.getMessage());
            result.setResponseCode("400");
            result.setResponseMessage(e.getMessage());
            interrupt();
        } catch (ConsumerCancelledException e) {
            e.printStackTrace();
            loginConsumer = null;
            loginConsumerTag = null;
            log.warn(e.getMessage());
            result.setResponseCode("300");
            result.setResponseMessage(e.getMessage());
            interrupt();
        } catch (InterruptedException e) {
            e.printStackTrace();
            loginConsumer = null;
            loginConsumerTag = null;
            log.info(e.getMessage());
            result.setResponseCode("200");
            result.setResponseMessage(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            loginConsumer = null;
            loginConsumerTag = null;
            log.warn(e.getMessage());
            result.setResponseCode("100");
            result.setResponseMessage(e.getLocalizedMessage());
        } finally {
            result.sampleEnd();
        }

        trace("Market Info sample() method ended");

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

            if (marketInfoBindingQueueName != null) {
                channel.queueUnbind(marketInfoBindingQueueName,getMarketInfoExchange(),getRoutingKey());
            }

        } catch(IOException e) {
            log.warn(e.getMessage());
            e.printStackTrace();
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

    private String constructNiceString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("---MQ SERVER---")
                .append("\nIP \t:")
                .append(getHost())
                .append("\nPort\t:")
                .append(getPort())
                .append("\nUsername\t:")
                .append(getUsername())
                .append("\nPassword\t:")
                .append(getPassword())
                .append("\nVirtual Host\t:")
                .append(getVirtualHost())
                .append("\n----------")
                .append("\n---REQUEST---\n")
                .append(logonRequest.toString());

        return stringBuilder.toString();

    }

    class LoginMessagePublisher implements Runnable {

        @Override
        public void run() {

            try {
                AMQP.BasicProperties props = MessageProperties.MINIMAL_BASIC
                        .builder()
                        .replyTo(getReplyToQueue())
                        .build();

                log.info("Publishing login request message to Queue:"+getLoginQueue());

                channel.basicPublish("", getLoginQueue(), props, logonRequest.toByteArray());

                //TODO how about ack ? Is it a mandatory ?

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

            System.out.format("Market Info Process started at "+new Date());

            latch.countDown();

            timer.cancel(); //Terminate the timer thread
        }
    }


}