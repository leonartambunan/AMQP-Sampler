package com.sxi.jmeter.protocol.amqp;

import com.rabbitmq.client.*;
import com.rabbitmq.client.QueueingConsumer.Delivery;
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
import java.util.concurrent.TimeoutException;

public class Login extends AbstractLoginSampler implements Interruptible, TestStateListener {

    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggingManager.getLoggerForClass();
    private static final String RECEIVE_TIMEOUT = "AMQPConsumer.ReceiveTimeout";
    private final static String HEADERS = "AMQPPublisher.Headers";
    private static final Object SUCCESSFUL_LOGIN = "OK";

    private transient Channel channel;
    private transient QueueingConsumer consumer;
    private transient String consumerTag;

    public LogonRequest logonRequest;


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



        SampleResult result = new SampleResult();
        result.setSampleLabel(getName());
        result.setSuccessful(false);
        result.setResponseCode("500");

        result.setSamplerData(constructNiceString());

        trace("Trimegah Login sample()");

        try {

            initChannel();

            if (consumer == null) {
                log.info("Creating rpc login consumer");
                consumer = new QueueingConsumer(channel);
            }

            log.info("Starting basicConsume to ReplyTo Queue: "+ getReplyToQueue());
            consumerTag = channel.basicConsume(getReplyToQueue(), true, consumer);

        } catch (Exception ex) {
            log.error("Failed to initialize channel", ex);
            result.setResponseMessage(ex.toString());
            return result;
        }

        result.setSampleLabel(getTitle());

        result.sampleStart();

        Thread senderThread = new Thread(new LoginMessagePublisher());

        senderThread.start();

        Delivery delivery;

        try {

            delivery = consumer.nextDelivery(getReceiveTimeoutAsInt());

            if(delivery == null){
                result.setResponseMessage("Login delivery timed out");
                return result;
            }

            LogonResponse logonResponse = LogonResponse.parseFrom(delivery.getBody());

            System.out.println(logonResponse.toString());

            result.setResponseData(logonResponse.toString(), null);
            result.setResponseMessage(logonResponse.toString());

            //TODO How the authenticated connection passed to next request.
            //TODO Success string of logon status

            if (SUCCESSFUL_LOGIN.equals(logonResponse.getStatus())) {

                if (!"".equals(getAuthenticatedConnectionVarName())) {
                    saveConnectionToJMeterVariable();
                }

                result.setDataType(SampleResult.TEXT);
                result.setResponseCodeOK();
                result.setSuccessful(true);

            }

        } catch (ShutdownSignalException e) {
            e.printStackTrace();
            consumer = null;
            consumerTag = null;
            log.warn(e.getMessage());
            result.setResponseCode("400");
            result.setResponseMessage(e.getMessage());
            interrupt();
        } catch (ConsumerCancelledException e) {
            e.printStackTrace();
            consumer = null;
            consumerTag = null;
            log.warn(e.getMessage());
            result.setResponseCode("300");
            result.setResponseMessage(e.getMessage());
            interrupt();
        } catch (InterruptedException e) {
            e.printStackTrace();
            consumer = null;
            consumerTag = null;
            log.warn(e.getMessage());
            result.setResponseCode("200");
            result.setResponseMessage(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            consumer = null;
            consumerTag = null;
            log.warn(e.getMessage());
            result.setResponseCode("100");
            result.setResponseMessage(e.getMessage());
        } finally {
            result.sampleEnd();
        }

        trace("RPC Login sample() method ended");

        return result;
    }

    private String constructNiceString() {
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
            if (consumerTag != null) {
                channel.basicCancel(consumerTag);
            }
        } catch(IOException e) {
            log.error("Couldn't safely cancel the sample " + consumerTag, e);
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

    class LoginMessagePublisher implements Runnable {

        @Override
        public void run() {

            try {
                AMQP.BasicProperties props = MessageProperties.MINIMAL_BASIC
                        .builder()
                        .replyTo(getReplyToQueue())
                        .build();

                log.info("Publishing login request message to Queue:"+getServerQueue());

                channel.basicPublish("", getServerQueue(), props, logonRequest.toByteArray());

                //TODO how about ack ? Is it a mandatory ?

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}