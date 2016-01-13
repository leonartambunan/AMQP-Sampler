package com.sxi.jmeter.protocol.base;

import com.google.protobuf.InvalidProtocolBufferException;
import com.rabbitmq.client.*;
import com.sxi.jmeter.protocol.rpc.constants.Trimegah;
import id.co.tech.cakra.message.proto.olt.LogonRequest;
import id.co.tech.cakra.message.proto.olt.LogonResponse;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.Interruptible;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.testelement.ThreadListener;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

public abstract class AbstractRabbitSampler extends AbstractSampler implements ThreadListener, Interruptible, TestStateListener {

    public static final String DEFAULT_LOGON_REPLY_TO_QUEUE = "amq.rabbitmq.reply-to";
    public static final String DEFAULT_LOGON_REQUEST_QUEUE = "olt.logon_request-rpc";

    protected static final String VIRTUAL_HOST = "AMQPSampler.VirtualHost";
    protected static final String HOST = "AMQPSampler.Host";
    protected static final String PORT = "AMQPSampler.Port";
    protected static final String SSL = "AMQPSampler.SSL";
    protected static final String USERNAME = "AMQPSampler.Username";
    protected static final String PASSWORD = "AMQPSampler.Password";
    private static final String TIMEOUT = "AMQPSampler.Timeout";
    private static final String AUTHENTICATED_CONNECTION_VAR_NAME= "AMQPSampler.AuthenticatedConnectionVariableName";

    private final static String LOGON_REQUEST_QUEUE = "AMQPSampler.ServerQueue";
    private final static String LOGON_REPLY_TO_QUEUE = "AMQPSampler.ReplyToQueue";

    private final static String MOBILE_DEVICE_ID = "Mobile.DeviceId";
    private final static String MOBILE_USER_ID = "Mobile.UserId";
    private final static String MOBILE_PASSWORD = "Mobile.Password";
    private final static String MOBILE_TYPE = "Mobile.Type";
    private final static String MOBILE_APP_VERSION = "Mobile.AppVersion";

    private transient ConnectionFactory factory;
    private transient Connection connection;
    private transient Channel channel;

    public transient QueueingConsumer loginConsumer;
    public transient String loginConsumerTag;

    public LogonRequest logonRequest;

    public static final String SUCCESSFUL_LOGIN = "OK";
    public SampleResult result = new SampleResult();
    public static final Logger log = LoggingManager.getLoggerForClass();

    protected AbstractRabbitSampler(){
        factory = new ConnectionFactory();
        factory.setRequestedHeartbeat(Trimegah.HEARTBEAT);
    }

    @Override
    public SampleResult sample(Entry entry) {

        trace("sample() started");

        result = new SampleResult();
        result.setSampleLabel(getName());
        result.setSuccessful(false);
        result.setResponseCode("500");

        result.setSampleLabel(getTitle());

        try {

            if (!restoreConnection()) {
                createFreshAMQPConnection();
            }

        } catch (Exception e) {
            e.printStackTrace();
            trace(e.getMessage());
            result.setResponseCode("400");
            result.setResponseMessage(e.getMessage());
            interrupt();
        }

        result.sampleStart();

        makeRequest();

        result.sampleEnd();

        trace("sample() ended");

        return result;
    }

    protected boolean initChannel() throws IOException, NoSuchAlgorithmException, KeyManagementException, TimeoutException {

        trace("initChannel() factory "+(factory==null?"null":"not null")+", connection "+(connection==null?"null":"not null")+ ", channel "+(channel==null?"null":"not null"));

        if(channel != null && !channel.isOpen()){
            trace("Channel " + channel.getChannelNumber() + " closed unexpectedly: " + channel.getCloseReason());
            channel = null;
        }

        trace(channel==null?"channel is null, we are going to create one for you":"channel is not null");

        if(channel == null) {
            factory.setPort(getPortAsInt());
            factory.setHost(getHost());
            factory.setUsername(getUsername());
            factory.setPassword(getPassword());
            connection = factory.newConnection();
            channel  = connection.createChannel();
            channel.basicQos(2);
        }

        saveConnectionToJMeterVariable();

        return true;
    }

    protected LogonResponse login() throws ShutdownSignalException, ConsumerCancelledException, InterruptedException, InvalidProtocolBufferException {

        trace("login()");

        LogonResponse response = LogonResponse.getDefaultInstance();

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

        try {

            initChannel();

            if (loginConsumer == null) {
                trace("Creating rpc login consumer");
                loginConsumer = new QueueingConsumer(channel);
            }

            trace("Starting basicConsume to ReplyTo Queue: " + getLogonReplyToQueue());
            loginConsumerTag = channel.basicConsume(getLogonReplyToQueue(), true, loginConsumer);

            new Thread(new LoginMessagePublisher()).start();

        } catch (Exception ex) {
            trace("Failed to initialize channel");
            loginConsumerTag=null;
            loginConsumer=null;
        }

        QueueingConsumer.Delivery delivery = loginConsumer.nextDelivery(Long.valueOf(getTimeout()));

        if (delivery != null) {

            trace("Response to login request received <-- ");
            trace(new String(delivery.getBody()));
            response = LogonResponse.parseFrom(delivery.getBody());

        }

        return response;
    }

    protected String getTitle() {
        return this.getName();
    }

    public String getTimeout() {
        return getPropertyAsString(TIMEOUT, Trimegah.TIMEOUT);
    }

    public void setTimeout(String s) {
        setProperty(TIMEOUT, s);
    }

    public String getVirtualHost() {
        return getPropertyAsString(VIRTUAL_HOST,"/");
    }

    public void setVirtualHost(String name) {
        setProperty(VIRTUAL_HOST, name);
    }

    public String getHost() {
        return getPropertyAsString(HOST,"172.16.1.76");
    }

    public void setHost(String name) {
        setProperty(HOST, name);
    }

    public String getPort() {
        return getPropertyAsString(PORT,"5672");
    }

    public void setPort(String name) {
        setProperty(PORT, name);
    }

    protected int getPortAsInt() {
        if (getPropertyAsInt(PORT) < 1) {
            return Integer.parseInt(Trimegah.PORT);
        }
        return getPropertyAsInt(PORT);
    }

    public void setConnectionSSL(Boolean value) {
        setProperty(SSL, value.toString());
    }

    public boolean isConnectionSSL() {
        return getPropertyAsBoolean(SSL);
    }

    public void setAuthenticatedConnectionVarName(String name) {
        setProperty(AUTHENTICATED_CONNECTION_VAR_NAME, name);
    }

    public String getAuthenticatedConnectionVarName() {
        return getPropertyAsString(AUTHENTICATED_CONNECTION_VAR_NAME,"AUTH_CON");
    }

    public String getUsername() {
        return getPropertyAsString(USERNAME,"guest");
    }

    public void setUsername(String name) {
        setProperty(USERNAME, name);
    }

    public String getPassword() {
        return getPropertyAsString(PASSWORD,"guest");
    }

    public void setPassword(String name) {
        setProperty(PASSWORD, name);
    }

    public String getMobileDeviceId() {
        return getPropertyAsString(MOBILE_DEVICE_ID,"a18632d8bde899bc");
    }

    public void setMobileDeviceId(String deviceId) {
        setProperty(MOBILE_DEVICE_ID, deviceId);
    }

    public String getLogonRequestQueue() {
        return getPropertyAsString(LOGON_REQUEST_QUEUE, DEFAULT_LOGON_REQUEST_QUEUE);
    }

    public void setLogonRequestQueue(String queue) {
        setProperty(LOGON_REQUEST_QUEUE, queue);
    }

    public String getLogonReplyToQueue() {
        return getPropertyAsString(LOGON_REPLY_TO_QUEUE, DEFAULT_LOGON_REPLY_TO_QUEUE);
    }

    public void setLogonReplyToQueue(String queue) {
        setProperty(LOGON_REPLY_TO_QUEUE, queue);
    }

    public String getMobileAppVersion() {
        return getPropertyAsString(MOBILE_APP_VERSION,"3.0");
    }

    public void setMobileAppVersion(String appVersion) {
        setProperty(MOBILE_APP_VERSION, appVersion);
    }

    public String getMobilePassword() {
        return getPropertyAsString(MOBILE_PASSWORD);
    }

    public void setMobilePassword(String password) {
        setProperty(MOBILE_PASSWORD, password);
    }

    public String getMobileType() {
        return getPropertyAsString(MOBILE_TYPE,"ANDROID");
    }

    public void setMobileUserId(String userId) {
        setProperty(MOBILE_USER_ID, userId);
    }

    public String getMobileUserId() {
        return getPropertyAsString(MOBILE_USER_ID);
    }

    public void setMobileType(String mobileType) {
        setProperty(MOBILE_TYPE, mobileType);
    }

    public Channel getChannel() {
        return channel;
    }

    protected void cleanup() {

        trace("cleanup()");

        //Close connection only if the VAR NAME AUTHENTICATED CONNECTION is not set
        //if ("".equals(getAuthenticatedConnectionVarName().trim())) {
        try {
            if (connection != null && connection.isOpen())
                connection.close();
        } catch (IOException e) {
            e.printStackTrace();
            trace("Failed to close connection");
        }
        //} else {
        //saveConnectionToJMeterVariable();
        //}
    }

    @Override
    public void threadFinished() {
        trace("threadFinished()");
        cleanup();
    }

    @Override
    public void threadStarted() {

    }

    protected boolean restoreConnection() {

        trace("restoreConnection()");

        boolean restored = false;

        if (!"".equals(getAuthenticatedConnectionVarName().trim())) {

            trace("Trying to reuse Rabbit Connection from VAR [" + getAuthenticatedConnectionVarName() + "]");

            JMeterContext jmetercontext = JMeterContextService.getContext();
            JMeterVariables vars = jmetercontext.getVariables();

            if (vars == null) return false;

            Object a = vars.getObject(getAuthenticatedConnectionVarName());

            if (a != null) {
                connection = (Connection) a;
                try {

                    channel = connection.createChannel();

                    restored = true;

                    trace("Connection is "+(connection.isOpen()?"":"not")+" open");
                    trace("CONNECTION RESTORED");

                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        }

        return restored;

    }


    protected void createFreshAMQPConnection() throws KeyManagementException, TimeoutException, NoSuchAlgorithmException, IOException, InterruptedException {

        trace("createFreshAMQPConnection()");

        initChannel();

        if (loginConsumerTag == null) {
            trace("Creating rpc login consumer");
            loginConsumer = new QueueingConsumer(channel);
        }

        trace("Starting basicConsume to Login ReplyTo Queue:"+ getLogonReplyToQueue());

        loginConsumerTag = channel.basicConsume(getLogonReplyToQueue(), true, loginConsumer);

        new Thread(new LoginMessagePublisher()).start();

        QueueingConsumer.Delivery loginDelivery;

        loginDelivery = loginConsumer.nextDelivery(Long.valueOf(getTimeout()));

        LogonResponse logonResponse = LogonResponse.parseFrom(loginDelivery.getBody());

        trace(logonResponse.toString());

    }

    class LoginMessagePublisher implements Runnable {

        @Override
        public void run() {

            try {
                AMQP.BasicProperties props = MessageProperties.MINIMAL_BASIC
                        .builder()
                        .replyTo(getLogonReplyToQueue())
                        .build();

                trace("Publishing Login request message to Queue: ["+ getLogonRequestQueue()+"]");

                channel.basicPublish("", getLogonRequestQueue(), props, logonRequest.toByteArray());

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public String constructNiceString() {
        StringBuilder stringBuffer = new StringBuilder();
        stringBuffer.append("---MQ SERVER---")
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

        return stringBuffer.toString();

    }

    protected void saveConnectionToJMeterVariable() {
        trace("saveConnectionToJMeterVariable() started");

        if (!"".equals(getAuthenticatedConnectionVarName())) {

            if (connection!=null && connection.isOpen()) {

                JMeterContext jMeterContext = JMeterContextService.getContext();

                JMeterVariables vars = jMeterContext.getVariables();

                if (vars == null) vars = new JMeterVariables();

                vars.putObject(getAuthenticatedConnectionVarName(), connection);

                jMeterContext.setVariables(vars);

                trace("connection saved to " + getAuthenticatedConnectionVarName());

            }
        }

        trace("saveConnectionToJMeterVariable() ended");
    }


    @Override
    public boolean interrupt() {
        testEnded();
        return true;
    }

    @Override
    public void testEnded() {
        trace("testEnded()");
        cleanup();
    }

    @Override
    public void testEnded(String arg0) {

        trace("testEnded(String arg0)");
    }

    @Override
    public void testStarted() {

    }

    @Override
    public void testStarted(String arg0) {

    }

    public void trace(String s) {
        String tl = getTitle();
//        String tn = Thread.currentThread().getName();
//        String th = this.toString();
        log.info(tl + "\t- " + s);
    }


    public abstract void makeRequest();

}
