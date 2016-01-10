package com.sxi.jmeter.protocol.amqp.login;

import com.google.protobuf.InvalidProtocolBufferException;
import com.rabbitmq.client.*;
import com.sxi.jmeter.protocol.amqp.constants.Trimegah;
import id.co.tech.cakra.message.proto.olt.LogonRequest;
import id.co.tech.cakra.message.proto.olt.LogonResponse;
import org.apache.jmeter.samplers.AbstractSampler;
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

public abstract class AbstractLogin extends AbstractSampler implements ThreadListener {

    public static final String DEFAULT_LOGON_REPLY_TO_QUEUE = "amq.rabbitmq.reply-to";
    public static final String DEFAULT_LOGON_REQUEST_QUEUE = "olt.logon_request-rpc";

    public static final Logger log = LoggingManager.getLoggerForClass();

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
    protected transient Connection connection;
    private transient Channel channel;

    private transient QueueingConsumer loginConsumer;
    private transient String loginConsumerTag;

    public LogonRequest logonRequest;

    public static final String SUCCESSFUL_LOGIN = "OK";

    protected AbstractLogin(){
        factory = new ConnectionFactory();
        factory.setRequestedHeartbeat(Trimegah.HEARTBEAT);
    }

    private boolean initChannel() throws IOException, NoSuchAlgorithmException, KeyManagementException, TimeoutException {

        if(channel != null && !channel.isOpen()){
            log.warn("channel " + channel.getChannelNumber() + " closed unexpectedly: ", channel.getCloseReason());
            channel = null;
        }

        log.info(channel==null?"channel is null, we are going to create one for you":"channel is not null");

        if(channel == null) {
            factory.setPort(getPortAsInt());
            factory.setHost(getHost());
            factory.setUsername(getUsername());
            factory.setPassword(getPassword());

            connection = factory.newConnection();

            channel  = connection.createChannel();
            channel.basicQos(2);


        }

        return true;
    }

    protected LogonResponse login() throws ShutdownSignalException, ConsumerCancelledException, InterruptedException, InvalidProtocolBufferException {

        LogonResponse result = LogonResponse.getDefaultInstance();

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
                log.info("Creating rpc login consumer");
                loginConsumer = new QueueingConsumer(channel);
            }

            log.info("Starting basicConsume to ReplyTo Queue: " + getLogonReplyToQueue());
            loginConsumerTag = channel.basicConsume(getLogonReplyToQueue(), true, loginConsumer);

            new Thread(new LoginMessagePublisher()).start();

        } catch (Exception ex) {
            log.error("Failed to initialize channel", ex);
            loginConsumerTag=null;
            loginConsumer=null;
        }

        QueueingConsumer.Delivery delivery = loginConsumer.nextDelivery(Long.valueOf(getTimeout()));

        if (delivery != null) {


            LogonResponse logonResponse = LogonResponse.parseFrom(delivery.getBody());

            System.out.println(logonResponse.toString());

            result = logonResponse;

            //TODO How the authenticated connection passed to next request.
            //TODO Success string of logon status


        }

        return result;
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
        return getPropertyAsString(VIRTUAL_HOST);
    }

    public void setVirtualHost(String name) {
        setProperty(VIRTUAL_HOST, name);
    }

    public String getHost() {
        return getPropertyAsString(HOST);
    }

    public void setHost(String name) {
        setProperty(HOST, name);
    }

    public String getPort() {
        return getPropertyAsString(PORT);
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
        return getPropertyAsString(AUTHENTICATED_CONNECTION_VAR_NAME,"");
    }

    public String getUsername() {
        return getPropertyAsString(USERNAME);
    }

    public void setUsername(String name) {
        setProperty(USERNAME, name);
    }

    public String getPassword() {
        return getPropertyAsString(PASSWORD);
    }

    public void setPassword(String name) {
        setProperty(PASSWORD, name);
    }

    public String getMobileDeviceId() {
        return getPropertyAsString(MOBILE_DEVICE_ID,"ANDROID");
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

    protected int getTimeoutAsInt() {
        if (getPropertyAsInt(TIMEOUT) < 1) {
            return Integer.valueOf(Trimegah.TIMEOUT);
        }
        return getPropertyAsInt(TIMEOUT);
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
        return getPropertyAsString(MOBILE_TYPE);
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

    protected void cleanup() {
        //Close connection only if the VAR NAME AUTHENTICATED CONNECTION is not set
        if ("".equals(getAuthenticatedConnectionVarName().trim())) {
            try {
                if (connection != null && connection.isOpen())
                    connection.close();
            } catch (IOException e) {
                log.error("Failed to close connection", e);
            }
        } else {
            saveConnectionToJMeterVariable();
        }
    }

    @Override
    public void threadFinished() {
        log.info("LoginSampler.threadFinished called");
        cleanup();
    }

    @Override
    public void threadStarted() {

    }

    protected boolean restoreConnection() {

        boolean restored = false;

        log.info("Reuse Connection from VAR "+getAuthenticatedConnectionVarName());

        JMeterContext jmetercontext = JMeterContextService.getContext();
        JMeterVariables vars = jmetercontext.getVariables();

        if (vars==null) return false;

        Object a = vars.getObject(getAuthenticatedConnectionVarName());

        if (a!=null) {
            connection = (Connection) a;
            try {
                channel = connection.createChannel();

            } catch (IOException e) {
                e.printStackTrace();
            }


            log.info("Connection recovered from JMeter Context");

            restored = true;
        }

        return restored;

    }


    protected void createFreshAMQPConnection() throws KeyManagementException, TimeoutException, NoSuchAlgorithmException, IOException, InterruptedException {

        initChannel();

        if (loginConsumerTag == null) {
            log.info("Creating rpc login consumer");
            loginConsumer = new QueueingConsumer(channel);
        }

        log.info("Starting basicConsume to Login ReplyTo Queue:"+ getLogonReplyToQueue());

        loginConsumerTag = channel.basicConsume(getLogonReplyToQueue(), true, loginConsumer);

        new Thread(new LoginMessagePublisher()).start();

        QueueingConsumer.Delivery loginDelivery;

        loginDelivery = loginConsumer.nextDelivery(Long.valueOf(getTimeout()));

        LogonResponse logonResponse = LogonResponse.parseFrom(loginDelivery.getBody());

        log.info(logonResponse.toString());

    }



    class LoginMessagePublisher implements Runnable {

        @Override
        public void run() {

            try {
                AMQP.BasicProperties props = MessageProperties.MINIMAL_BASIC
                        .builder()
                        .replyTo(getLogonReplyToQueue())
                        .build();

                log.info("Publishing login request message to Queue:"+ getLogonRequestQueue());

                channel.basicPublish("", getLogonRequestQueue(), props, logonRequest.toByteArray());

                //TODO how about ack ? Is it a mandatory ?

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

        JMeterContext jMeterContext = JMeterContextService.getContext();
        JMeterVariables vars = jMeterContext.getVariables();

        if (vars==null) vars = new JMeterVariables();

        vars.putObject(getAuthenticatedConnectionVarName(), connection);

        jMeterContext.setVariables(vars);

    }



}
