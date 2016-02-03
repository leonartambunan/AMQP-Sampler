package com.sxi.jmeter.protocol.rpc.preopening;

import com.rabbitmq.client.*;
import com.rabbitmq.client.QueueingConsumer.Delivery;
import com.sxi.jmeter.protocol.rpc.constants.Trimegah;
import id.co.tech.cakra.message.proto.olt.*;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.Interruptible;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.testelement.property.TestElementProperty;

import java.io.IOException;
import java.net.InetAddress;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class PreOpening extends AbstractPreOpening implements Interruptible, TestStateListener {

    private static final long serialVersionUID = 7480863561320459099L;

    private static final String RECEIVE_TIMEOUT = "AMQPConsumer.ReceiveTimeout";
    private final static String HEADERS = "AMQPPublisher.Headers";
    private static final String POSITIVE_LOGON_STATUS = "OK";
    private transient Channel channel;
    private transient QueueingConsumer loginConsumer;
    private LogonRequest logonRequest;
    private PINValidationRequest pinRequest;
    private AccountRequest accountRequest;
    private static String orderRef = null;
    private static String sessionId = null;
    protected static boolean SCHEDULE_STARTED = false;
    private transient CountDownLatch marketOpenLatch = new CountDownLatch(1);
    private transient CountDownLatch messageLatch  = new CountDownLatch(1);
    private String INVESTOR_TYPE = "I";
    private String ACC_NO = "";
    private String USER_ID = "";

    @Override
    public SampleResult sample(Entry entry) {

        orderRef = String.valueOf(System.currentTimeMillis());
        USER_ID = getMobileUserId();

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
        } catch (Exception e) {
            trace(e.toString());
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

        trace("sample() method started ");

        try {

            initChannel();

            if (loginConsumer == null) {
                trace("Creating rpc login Consumer");
                loginConsumer = new QueueingConsumer(channel);
            }

            trace("Starting basicConsume to Login ReplyTo Queue:"+ getLoginReplyToQueue());

            String loginConsumerTag = channel.basicConsume(getLoginReplyToQueue(), true, loginConsumer);

            Thread senderThread = new Thread(new LoginMessagePublisher());
            senderThread.start();

            Delivery loginDelivery = loginConsumer.nextDelivery(getReceiveTimeoutAsInt());

            if(loginDelivery == null){
                throw new Exception("Login Delivery timed out");
            }

            LogonResponse logonResponse = LogonResponse.parseFrom(loginDelivery.getBody());

            //System.out.println(logonResponse.toString());

            channel.basicCancel(loginConsumerTag);

            if (POSITIVE_LOGON_STATUS.equals(logonResponse.getStatus())) {

                /*================================ <get account info> =====================================*/
                sessionId = logonResponse.getSessionId();

                accountRequest = AccountRequest
                        .newBuilder()
                        .setUserId(getMobileUserId())
                        .setSessionId(sessionId)
                        .build();

                QueueingConsumer accountInfoConsumer = new QueueingConsumer(channel);

                String accInfoConsumerTag = channel.basicConsume(getAccInfoResponseQueue(), true, accountInfoConsumer);

                Thread accInfoSenderThread = new Thread(new AccountInfoMessagePublisher());
                accInfoSenderThread.start();

                Delivery accInfoDelivery = accountInfoConsumer.nextDelivery(getReceiveTimeoutAsInt());

                if(accInfoDelivery == null){
                    throw new Exception("Acc Info Delivery time-out");
                }

                AccountResponse accountResponse = AccountResponse.parseFrom(accInfoDelivery.getBody());

                trace(accountResponse.toString());

                if (accountResponse.getAccountInfoCount()>0) {
                    INVESTOR_TYPE = accountResponse.getAccountInfo(0).getInvtype();
                    ACC_NO = accountResponse.getAccountInfo(0).getAccno();

                }

                channel.basicCancel(accInfoConsumerTag);

                //================================ </get account info> =======================================================

                //================================ <Pin Validation> =======================================================
                pinRequest = PINValidationRequest
                        .newBuilder()
                        .setUserId(getMobileUserId())
                        .setSessionId(sessionId)
                        .setPinValue(getMobilePin())
                        .build();

                QueueingConsumer pinValidationConsumer = new QueueingConsumer(channel);

                String pinValidationConsumerTag = channel.basicConsume(getPinValidationResponseQueue(), true, pinValidationConsumer);

                Thread pinValidationSenderThread = new Thread(new PinValidationMessagePublisher());
                pinValidationSenderThread.start();

                Delivery pinValidationDelivery = pinValidationConsumer.nextDelivery(getReceiveTimeoutAsInt());

                if(pinValidationDelivery == null){
                    throw new Exception("Pin Validation Delivery time-out");
                }

                PINValidationResponse pinValidationResponse = PINValidationResponse.parseFrom(pinValidationDelivery.getBody());

                trace("Response: "+pinValidationResponse.toString());

                channel.basicCancel(pinValidationConsumerTag);

                /*================================ </Pin Validation> ========================================*/


                /*====================<listen to new order response exchange>======================================*/
                DefaultConsumer orderStatusConsumer = new DefaultConsumer(getChannel()) {
                    @Override
                    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {

                        trace("Message received <--");

                        if (SCHEDULE_STARTED) {

                            OLTMessage response = OLTMessage.parseFrom(body);

                            trace(response.toString());

                            trace("Type:" + response.getType());

                            if (OLTMessage.Type.NEW_OLT_ORDER_REJECT.equals(response.getType())) {
                                trace("Order Rejected");
                                trace(orderRef + " VS " + response.getNewOLTOrderReject().getClOrderRef());
                                if (orderRef.equals(response.getNewOLTOrderReject().getClOrderRef())) {
                                    result.setResponseCodeOK();
                                    result.setSuccessful(true);
                                    result.setResponseMessage(response.toString());
                                    messageLatch.countDown();
                                }
                            } else if (OLTMessage.Type.NEW_OLT_ORDER_EXCHANGE_UPDATE.equals(response.getType())) {
                                trace("Got Update from Exchange");
                                trace(orderRef + " VS " + response.getNewOLTOrderExchangeUpdate().getClOrderRef());
                                if (orderRef.equals(response.getNewOLTOrderExchangeUpdate().getClOrderRef())) {
                                    result.setResponseData(response.toString() + '\n' + response.getNewOLTOrderExchangeUpdate().toString(), null);
                                    result.setResponseCodeOK();
                                    result.setSuccessful(true);
                                    result.setResponseMessage(response.toString());
                                    messageLatch.countDown();
                                }

                            } else if (OLTMessage.Type.NEW_OLT_ORDER_ACK.equals(response.getType())) {

                                if (orderRef.equals(response.getNewOLTOrderAck().getClOrderRef())) {
                                    trace("Patient. You have to wait");
                                }

                            } else {
                                trace("What to do ? Calm, this msg is not your task as new order listener");
                            }
                        } else {
                            trace("Message received while market is not opened");
                        }
                    }
                };

                String bindingQueueName = channel.queueDeclare().getQueue();
                trace("Listening to Queue ["+ bindingQueueName +"] bind to exchange ["+ getOrderResponseQueue()+"] with routing key ["+getRoutingKey()+ ']');
                channel.queueBind(bindingQueueName, getOrderResponseQueue(),getRoutingKey());
                channel.basicConsume(bindingQueueName,true,orderStatusConsumer);
                /*==================== </listen to new order response exchange>=================================*/

                /*====================<send new order>=================================*/
                new Thread(new NewOrderMessagePublisher()).start();
                /*====================</send new order>=================================*/

                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(getScheduleHour()));
                calendar.set(Calendar.MINUTE, Integer.parseInt(getScheduleMinute()));
                calendar.set(Calendar.SECOND, Integer.parseInt(getScheduleSecond()));
                Date time = calendar.getTime();

                Timer timer = new Timer();
                ScheduledExecutionTask thread = new ScheduledExecutionTask(timer, marketOpenLatch);
                timer.schedule(thread, time);

                trace("Waiting for schedule to start");
                marketOpenLatch.await();
                trace("Schedule started");
                SCHEDULE_STARTED = true;
                result.sampleResume();

                boolean reachZero = messageLatch.await(Long.valueOf(getTimeout()), TimeUnit.MILLISECONDS);

                if (!reachZero) {
                    //throw new Exception("Time out while waiting for market info message occurred");
                    return null;
                }

            } else {
                throw new Exception("Login Failed.\n"+logonResponse.toString(),null);
            }

        } catch (Exception e) {
            loginConsumer = null;
            trace(e);
            result.setResponseCode("100");
            result.setResponseMessage("Exception: "+e);
            result.setResponseData("Exception: "+e,null);
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


        super.cleanup();

    }

    protected boolean initChannel() throws IOException, NoSuchAlgorithmException, KeyManagementException, TimeoutException {
        boolean ret = super.initChannel();
        channel.basicQos(2);
        return ret;
    }


    private String constructNiceString() {

        return "---MQ SERVER---" +
                "\nIP \t:" +
                getHost() +
                "\nPort\t:" +
                getPort() +
                "\nUsername\t:" +
                getUsername() +
                "\nPassword\t:" +
                getPassword() +
                "\nVirtual Host\t:" +
                getVirtualHost() +
                "\n----------" +
                "\n---REQUEST---\n" +
                logonRequest.toString();

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
                trace("Request: "+logonRequest.toString());
                channel.basicPublish("", getLoginQueue(), props, logonRequest.toByteArray());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class AccountInfoMessagePublisher implements Runnable {

        @Override
        public void run() {

            try {
                AMQP.BasicProperties props = MessageProperties.MINIMAL_BASIC
                        .builder()
                        .replyTo(getAccInfoResponseQueue())
                        .build();

                trace("Publishing acc info request message to Queue:"+getAccInfoResponseQueue());

                trace("Request: "+accountRequest.toString());

                channel.basicPublish("", getAccInfoRequestQueue(), props, accountRequest.toByteArray());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class PinValidationMessagePublisher implements Runnable {

        @Override
        public void run() {

            try {
                AMQP.BasicProperties props = MessageProperties.MINIMAL_BASIC
                        .builder()
                        .replyTo(getPinValidationResponseQueue())
                        .build();

                trace("Publishing PIN validation request message to Queue:"+getPinValidationRequestQueue());
                trace("Request: "+pinRequest.toString());

                channel.basicPublish("", getPinValidationRequestQueue(), props, pinRequest.toByteArray());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class NewOrderMessagePublisher implements Runnable {

        @Override
        public void run() {

            NewOLTOrder contentRequest = NewOLTOrder
                    .newBuilder()
                    .setOrderTime(System.currentTimeMillis())
                    .setBuySell(getBuySell())
                    .setInputBy(USER_ID)
                    .setClientCode(ACC_NO)
                    .setOrdQty(Double.valueOf(getStockAmount()))
                    .setOrdPrice(Double.valueOf(getOrderPrice()))
                    .setClOrderRef(orderRef)
                    .setBoard(getBoard())
                    .setStockCode(getStockCode())
                    .setTimeInForce(getTimeInForce())
                    .setInsvtType(INVESTOR_TYPE)
                    .setOrderTime(System.currentTimeMillis())
                    .build();

            OLTMessage newOrderRequest = OLTMessage.newBuilder()
                    .setSessionId(sessionId)
                    .setNewOLTOrder(contentRequest)
                    .setType(OLTMessage.Type.NEW_OLT_ORDER)
                    .build();

            try {

                AMQP.BasicProperties props = MessageProperties.MINIMAL_BASIC
                        .builder()
                        .replyTo(getOrderResponseQueue())
                        .build();

                trace("Publishing new order request message to Queue:"+getOrderRequestQueue());
                trace("Request: "+ newOrderRequest.toString());
                trace("Subject to be replied to "+getOrderResponseQueue());

                channel.basicPublish("", getOrderRequestQueue(), props, newOrderRequest.toByteArray());

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