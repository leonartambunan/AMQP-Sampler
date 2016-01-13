package com.sxi.jmeter.protocol.rpc.preopening;

import com.rabbitmq.client.*;
import com.rabbitmq.client.QueueingConsumer.Delivery;
import com.sxi.jmeter.protocol.rpc.constants.Trimegah;
import id.co.tech.cakra.message.proto.olt.LogonRequest;
import id.co.tech.cakra.message.proto.olt.LogonResponse;
import id.co.tech.cakra.message.proto.olt.NewOLTOrder;
import id.co.tech.cakra.message.proto.olt.OrderInfoResponse;
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
import java.util.concurrent.TimeoutException;

public class PreOpening extends AbstractPreOpening implements Interruptible, TestStateListener {

    private static final long serialVersionUID = 7480863561320459099L;

    private static final String RECEIVE_TIMEOUT = "AMQPConsumer.ReceiveTimeout";
    private final static String HEADERS = "AMQPPublisher.Headers";
    private static final String POSITIVE_LOGON_STATUS = "OK";
    private transient Channel channel;
    private transient QueueingConsumer loginConsumer;
    private transient QueueingConsumer orderStatusConsumer;
    private transient String loginConsumerTag;
    public LogonRequest logonRequest;
    public NewOLTOrder newOLTOrder;

    private transient CountDownLatch latch = new CountDownLatch(1);

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


            newOLTOrder = NewOLTOrder
                    .newBuilder()
                    .setOrderTime(System.currentTimeMillis())
                    .setBuySell(getBuySell())
                    .setInputBy("JMETER")
                    .setClientCode(getClientCode())
                    .setOrdQty(Double.parseDouble(getStockAmount()))
                    .setOrdPrice(Double.valueOf(getOrderPrice()))
                    .setClOrderRef("ST"+ System.currentTimeMillis())
                    .setBoard(getBoard())
                    .setTimeInForce(getTimeInForce())
                    .setInsvtType(getInvestorType())
                    .setStockCode(getStockCode())
                    .build();

        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        SampleResult result = new SampleResult();
        result.setSampleLabel(getName());
        result.setSuccessful(false);
        result.setResponseCode("500");
        result.setSampleLabel(getTitle());
        result.setSamplerData(constructNiceString());

        trace("PreOpening sample() method started ");

        try {

            initChannel();

            if (loginConsumer == null) {
                trace("Creating rpc login Consumer");
                loginConsumer = new QueueingConsumer(channel);
            }

            trace("Starting basicConsume to Login ReplyTo Queue:"+ getLoginReplyToQueue());

            loginConsumerTag = channel.basicConsume(getLoginReplyToQueue(), true, loginConsumer);


        } catch (Exception ex) {
            ex.printStackTrace();
            trace("Failed to initialize channel");
            result.setResponseMessage(ex.toString());
            return result;
        }

        Thread senderThread = new Thread(new LoginMessagePublisher());
        senderThread.start();

        Delivery loginDelivery;
        Delivery orderResponseDelivery;

        try {

            loginDelivery = loginConsumer.nextDelivery(getReceiveTimeoutAsInt());

            if(loginDelivery == null){
                result.setResponseMessage("Login Delivery timed out");
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
                ScheduledExecutionTask thread = new ScheduledExecutionTask(timer,latch);
                timer.schedule(thread, time);

                latch.await();

                result.sampleStart();

                orderStatusConsumer = new QueueingConsumer(channel);

                trace("Starting basicConsume to Order Queue:"+getOrderResponseQueue());

                channel.basicConsume(getOrderResponseQueue(),true,orderStatusConsumer);

                new Thread(new NewOrderMessagePublisher()).start();

                orderResponseDelivery = orderStatusConsumer.nextDelivery(getReceiveTimeoutAsInt());

                if(orderResponseDelivery == null){
                    result.setResponseMessage("Order response timed out");
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
            trace(e.getMessage());
            result.setResponseCode("400");
            result.setResponseMessage(e.getMessage());
            interrupt();
        } catch (ConsumerCancelledException e) {
            e.printStackTrace();
            loginConsumer = null;
            loginConsumerTag = null;
            trace(e.getMessage());
            result.setResponseCode("300");
            result.setResponseMessage(e.getMessage());
            interrupt();
        } catch (InterruptedException e) {
            e.printStackTrace();
            loginConsumer = null;
            loginConsumerTag = null;
            trace(e.getMessage());
            result.setResponseCode("200");
            result.setResponseMessage(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            loginConsumer = null;
            loginConsumerTag = null;
            trace(e.getMessage());
            result.setResponseCode("100");
            result.setResponseMessage(e.getMessage());
        } finally {
            result.sampleEnd();
        }

        trace("PreOpening sample() method ended");

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

        } catch(IOException e) {
            trace("Couldn't safely cancel the sample " + loginConsumerTag);
        }

        super.cleanup();

    }

    protected boolean initChannel() throws IOException, NoSuchAlgorithmException, KeyManagementException, TimeoutException {
        boolean ret = super.initChannel();
        channel.basicQos(2);
        return ret;
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
                        .replyTo(getLoginReplyToQueue())
                        .build();

                trace("Publishing login request message to Queue:"+getLoginQueue());

                channel.basicPublish("", getLoginQueue(), props, logonRequest.toByteArray());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class NewOrderMessagePublisher implements Runnable {

        @Override
        public void run() {

            try {
                AMQP.BasicProperties props = MessageProperties.MINIMAL_BASIC
                        .builder()
                        .replyTo(getOrderResponseQueue())
                        .build();

                trace("Publishing new neworder request message to Queue:"+getOrderRequestQueue());

                channel.basicPublish("", getOrderRequestQueue(), props, newOLTOrder.toByteArray());

                //TODO how about ack ? Is it a mandatory ?

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class ScheduledExecutionTask extends TimerTask {
        Timer timer;
        CountDownLatch latch;
        public ScheduledExecutionTask(Timer timer, CountDownLatch latch) {

            this.timer=timer;
            this.latch = latch;
        }

        public void run() {
            System.out.format("PreOpening process starts");
            latch.countDown();
            timer.cancel(); //Terminate the timer thread
        }
    }

}