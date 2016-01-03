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
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

public class Login extends AbstractLoginSampler implements Interruptible, TestStateListener {

    private static final long serialVersionUID = 7480863561320459099L;
    private static final Logger log = LoggingManager.getLoggerForClass();
    private static final String RECEIVE_TIMEOUT = "AMQPConsumer.ReceiveTimeout";
    private final static String HEADERS = "AMQPPublisher.Headers";
    private static final Object SUCCESSFUL_LOGIN = "Success";

    private transient Channel channel;
    private transient QueueingConsumer consumer;
    private transient String consumerTag;

    @Override
    public SampleResult sample(Entry entry) {

        SampleResult result = new SampleResult();
        result.setSampleLabel(getName());
        result.setSuccessful(false);
        result.setResponseCode("500");

        result.setSamplerData("User:"+getMobileUserid()+", Password:"+getMobilePassword()+",DeviceId:"+getMobileDeviceid()+",DeviceType:"+getMobileType()+",AppVersion:"+getMobileAppVersion());

        trace("Trimegah RPC Login sample()");

        try {

            initChannel();

            if (consumer == null) {
                log.info("Creating rpc consumer");
                consumer = new QueueingConsumer(channel);
            }

            log.info("Starting basic consumer from Queue: "+getReplytoQueue());
            consumerTag = channel.basicConsume(getReplytoQueue(), true, consumer);

        } catch (Exception ex) {
            log.error("Failed to initialize channel", ex);
            result.setResponseMessage(ex.toString());
            return result;
        }

        result.setSampleLabel(getTitle());

        result.sampleStart();


        Thread senderThread = new Thread(new MessageSender());

        senderThread.start();

        Delivery delivery;

        try {

            delivery = consumer.nextDelivery(getReceiveTimeoutAsInt());

            if(delivery == null){
                result.setResponseMessage("timed out");
                return result;
            }

            LogonResponse logonResponse = LogonResponse.parseFrom(delivery.getBody());

            //TODO How the authenticated connection passed to next request.
            //TODO Success string of logon status

            if (SUCCESSFUL_LOGIN.equals(logonResponse.getStatus())) {

                if (!"".equals(getAuthenticatedConnectionVarName())) {
                    saveConnectionToJMeterVariable();
                }

                result.setResponseMessage(logonResponse.getStatus());

                result.setResponseData(logonResponse.toString(), null);

                result.setDataType(SampleResult.TEXT);

                result.setResponseCodeOK();

                result.setSuccessful(true);

            }

        } catch (ShutdownSignalException e) {
            consumer = null;
            consumerTag = null;
            log.warn("AMQP RPC Login consumer failed to consume", e);
            result.setResponseCode("400");
            result.setResponseMessage(e.getMessage());
            interrupt();
        } catch (ConsumerCancelledException e) {
            consumer = null;
            consumerTag = null;
            log.warn("Trimegah RPC Login consumer failed to consume", e);
            result.setResponseCode("300");
            result.setResponseMessage(e.getMessage());
            interrupt();
        } catch (InterruptedException e) {
            consumer = null;
            consumerTag = null;
            log.info("Interrupted while attempting to consume RPC Login ");
            result.setResponseCode("200");
            result.setResponseMessage(e.getMessage());
        } catch (IOException e) {
            consumer = null;
            consumerTag = null;
            log.warn("Trimegah RPC Login consumer failed to consume", e);
            result.setResponseCode("100");
            result.setResponseMessage(e.getMessage());
        } finally {
            result.sampleEnd();
        }

        trace("Trimegah RPC Login.sample ended");

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

//    public String getPurgeQueue() {
//        return getPropertyAsString(PURGE_QUEUE);
//    }

//    public void setPurgeQueue(String content) {
//        setProperty(PURGE_QUEUE, content);
//    }

//    public void setPurgeQueue(Boolean purgeQueue) {
//        setProperty(PURGE_QUEUE, purgeQueue.toString());
//    }

//    public boolean purgeQueue(){
//        return Boolean.parseBoolean(getPurgeQueue());
//    }

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

    class MessageSender implements Runnable {

        @Override
        public void run() {

            try {
                AMQP.BasicProperties props = MessageProperties.MINIMAL_BASIC
                        .builder()
                        .replyTo(getReplytoQueue())
                        .build();


                LogonRequest logonReq = LogonRequest
                        .newBuilder()
                        .setUserId(getMobileUserid())
                        .setPassword(getMobilePassword())
                        .setDeviceId(getMobileDeviceid())
                        .setDeviceType(getMobileType())
                        .setAppVersion(getMobileAppVersion())
                        .setIp(InetAddress.getLocalHost().getHostAddress())
                        .build();

                log.info("Publishing message to Queue:"+getServerQueue());

                channel.basicPublish("", getServerQueue(), props, logonReq.toByteArray());

                //TODO how about ack ? Is it a mandatory ?

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}