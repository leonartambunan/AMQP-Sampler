package com.sxi.jmeter.protocol.amqp;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.testelement.ThreadListener;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

public abstract class AbstractPreOpeningSampler extends AbstractSampler implements ThreadListener {

    public static final String DEFAULT_LOGIN_QUEUE = "olt.logon_request-rpc";
    public static final String DEFAULT_REPLY_TO_QUEUE = "amq.rabbitmq.reply-to";
    public static final String DEFAULT_ORDER_REQUEST_QUEUE = "olt.order_request";
    public static final String DEFAULT_ORDER_RESPONSE_QUEUE = "olt.order_response";

    public static final int DEFAULT_PORT = 5672;

    public static final String DEFAULT_PORT_STRING = Integer.toString(DEFAULT_PORT);

    public static final int DEFAULT_TIMEOUT = 60000;
    public static final String DEFAULT_TIMEOUT_STRING = Integer.toString(DEFAULT_TIMEOUT);

    private static final Logger log = LoggingManager.getLoggerForClass();

    protected static final String VIRTUAL_HOST = "AMQPSampler.VirtualHost";
    protected static final String HOST = "AMQPSampler.Host";
    protected static final String PORT = "AMQPSampler.Port";
    protected static final String SSL = "AMQPSampler.SSL";
    protected static final String USERNAME = "AMQPSampler.Username";
    protected static final String PASSWORD = "AMQPSampler.Password";
    private static final String TIMEOUT = "AMQPSampler.Timeout";
    private static final String SCHEDULE_HOUR = "AMQPSampler.ScheduleHour";
    private static final String SCHEDULE_MINUTE = "AMQPSampler.ScheduleMinute";
    private static final String SCHEDULE_SECOND = "AMQPSampler.ScheduleSecond";

    private static final int DEFAULT_HEARTBEAT = 1;

    private final static String LOGIN_QUEUE = "AMQPSampler.ServerQueue";
    private final static String LOGIN_REPLY_TO_QUEUE = "AMQPSampler.ReplyToQueue";
    private final static String ORDER_REQUEST_QUEUE = "AMQPSampler.OrderRequestQueue";
    private final static String ORDER_RESPONSE_QUEUE = "AMQPSampler.OrderResponseQueue";
    private static final String ROUTING_KEY = "AMQPSampler.RoutingKey";
    private static final String STOCK_ID = "AMQPSampler.StockId";
    private static final String STOCK_AMOUNT = "AMQPSampler.StockAmount";

    private final static String MOBILE_DEVICE_ID = "Mobile.DeviceId";
    private final static String MOBILE_USER_ID = "Mobile.UserId";
    private final static String MOBILE_PASSWORD = "Mobile.Password";
    private final static String MOBILE_TYPE = "Mobile.Type";
    private final static String MOBILE_APP_VERSION = "Mobile.AppVersion";

    private transient ConnectionFactory factory;
    private transient Connection connection;

    protected AbstractPreOpeningSampler(){
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

    public String getLoginQueue() {
        return getPropertyAsString(LOGIN_QUEUE, DEFAULT_LOGIN_QUEUE);
    }

    public void setLoginQueue(String queue) {
        setProperty(LOGIN_QUEUE, queue);
    }

    public String getLoginReplyToQueue() {
        return getPropertyAsString(LOGIN_REPLY_TO_QUEUE, DEFAULT_REPLY_TO_QUEUE);
    }

    public void setLoginReplyToQueue(String queue) {
        setProperty(LOGIN_REPLY_TO_QUEUE, queue);
    }

    public String getOrderResponseQueue() {
        return getPropertyAsString(ORDER_RESPONSE_QUEUE, DEFAULT_ORDER_RESPONSE_QUEUE);
    }

    public void setOrderResponseQueue(String queue) {
        setProperty(ORDER_RESPONSE_QUEUE, queue);
    }

    public String getOrderRequestQueue() {
        return getPropertyAsString(ORDER_REQUEST_QUEUE, DEFAULT_ORDER_REQUEST_QUEUE);
    }

    public void setOrderRequestQueue(String queue) {
        setProperty(ORDER_REQUEST_QUEUE, queue);
    }

    public String getRoutingKey() {
        return getPropertyAsString(ROUTING_KEY);
    }

    public void setRoutingKey(String key) {
        setProperty(ROUTING_KEY, key);
    }

    public String getStockId() {
        return getPropertyAsString(STOCK_ID);
    }

    public void setStockId(String id) {
        setProperty(STOCK_ID, id);
    }

    public String getStockAmount() {
        return getPropertyAsString(STOCK_AMOUNT);
    }

    public void setStockAmount(String amount) {
        setProperty(STOCK_AMOUNT, amount);
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
        setProperty(MOBILE_USER_ID, userid);
    }

    public String getMobileUserid() {
        return getPropertyAsString(MOBILE_USER_ID);
    }

    public void setMobileType(String mobileType) {
        setProperty(MOBILE_TYPE, mobileType);
    }

    public String getScheduleHour() {
        return getPropertyAsString(SCHEDULE_HOUR);
    }

    public void setScheduleHour(String hour) {
        setProperty(SCHEDULE_HOUR, hour);
    }

    public String getScheduleMinute() {
        return getPropertyAsString(SCHEDULE_MINUTE);
    }

    public void setScheduleMinute(String minute) {
        setProperty(SCHEDULE_MINUTE, minute);
    }

    public String getScheduleSecond() {
        return getPropertyAsString(SCHEDULE_SECOND);
    }

    public void setScheduleSecond(String second) {
        setProperty(SCHEDULE_SECOND, second);
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

}
