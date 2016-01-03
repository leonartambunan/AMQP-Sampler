package com.sxi.jmeter.protocol.amqp;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.testelement.ThreadListener;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

public abstract class AbstractLoginSampler extends AbstractSampler implements ThreadListener {

    public static final String DEFAULT_REPLY_QUEUE = "amq.rabbitmq.reply-to";
    public static final String DEFAULT_SERVER_QUEUE = "olt.logon_request-rpc";

    public static final int DEFAULT_PORT = 5672;
    public static final String DEFAULT_PORT_STRING = Integer.toString(DEFAULT_PORT);

    public static final int DEFAULT_TIMEOUT = 90000;
    public static final String DEFAULT_TIMEOUT_STRING = Integer.toString(DEFAULT_TIMEOUT);

    private static final Logger log = LoggingManager.getLoggerForClass();

    protected static final String VIRTUAL_HOST = "AMQPSampler.VirtualHost";
    protected static final String HOST = "AMQPSampler.Host";
    protected static final String PORT = "AMQPSampler.Port";
    protected static final String SSL = "AMQPSampler.SSL";
    protected static final String USERNAME = "AMQPSampler.Username";
    protected static final String PASSWORD = "AMQPSampler.Password";
    private static final String TIMEOUT = "AMQPSampler.Timeout";
    private static final String AUTHENTICATED_CONNECTION_VAR_NAME= "AMQPSampler.AuthenticatedConnectionVariableName";

    private static final int DEFAULT_HEARTBEAT = 1;

    private final static String LOGIN_QUEUE = "AMQPSampler.ServerQueue";
    private final static String REPLY_TO_QUEUE = "AMQPSampler.ReplyToQueue";

    private final static String MOBILE_DEVICEID = "Mobile.DeviceId";
    private final static String MOBILE_USERID = "Mobile.UserId";
    private final static String MOBILE_PASSWORD = "Mobile.Password";
    private final static String MOBILE_TYPE = "Mobile.Type";
    private final static String MOBILE_APP_VERSION = "Mobile.AppVersion";

    private transient ConnectionFactory factory;
    private transient Connection connection;

    protected AbstractLoginSampler(){
        factory = new ConnectionFactory();
        factory.setRequestedHeartbeat(DEFAULT_HEARTBEAT);
    }

    protected boolean initChannel() throws IOException, NoSuchAlgorithmException, KeyManagementException, TimeoutException {

        Channel channel = getChannel();

        if(channel != null && !channel.isOpen()){
            log.warn("channel " + channel.getChannelNumber() + " closed unexpectedly: ", channel.getCloseReason());
            channel = null;
        }

        log.info(channel==null?"channel is null":"channel is not null");

        if(channel == null) {

            factory.setPort(getPortAsInt());
            factory.setHost(getHost());
            factory.setUsername(getUsername());
            factory.setPassword(getPassword());

            connection = factory.newConnection();

            Channel ch = connection.createChannel();

            setChannel(ch);

        }

        return true;
    }

    protected abstract Channel getChannel();
    protected abstract void setChannel(Channel channel);

    protected String getTitle() {
        return this.getName();
    }

    public String getTimeout() {
        return getPropertyAsString(TIMEOUT, DEFAULT_TIMEOUT_STRING);
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
            return DEFAULT_PORT;
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

    public String getMobileDeviceid() {
        return getPropertyAsString(MOBILE_DEVICEID,"ANDROID");
    }

    public void setMobileDeviceid(String deviceId) {
        setProperty(MOBILE_DEVICEID, deviceId);
    }

    public String getServerQueue() {
        return getPropertyAsString(LOGIN_QUEUE,DEFAULT_SERVER_QUEUE);
    }

    public void setServerQueue(String queue) {
        setProperty(LOGIN_QUEUE, queue);
    }

    public String getReplytoQueue() {
        return getPropertyAsString(REPLY_TO_QUEUE,DEFAULT_REPLY_QUEUE);
    }

    public void setReplytoQueue(String queue) {
        setProperty(REPLY_TO_QUEUE, queue);
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

    public void setMobileUserid(String userid) {
        setProperty(MOBILE_USERID, userid);
    }

    public String getMobileUserid() {
        return getPropertyAsString(MOBILE_USERID);
    }

    public void setMobileType(String mobileType) {
        setProperty(MOBILE_TYPE, mobileType);
    }

    protected void cleanup() {
        try {
            //getChannel().close();   // closing the connection will close the channel if it's still open
            if(connection != null && connection.isOpen())
                connection.close();
        } catch (IOException e) {
            log.error("Failed to close connection", e);
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

    protected void saveConnectionToJMeterVariable() {
        org.apache.jmeter.threads.JMeterContext jMeterContext = org.apache.jmeter.threads.JMeterContextService.getContext();
        JMeterVariables vars = jMeterContext.getVariables();
        vars.putObject(getAuthenticatedConnectionVarName(), connection);
        jMeterContext.setVariables(vars);

    }


}