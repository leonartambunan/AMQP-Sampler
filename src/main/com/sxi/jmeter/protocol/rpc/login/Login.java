package com.sxi.jmeter.protocol.rpc.login;

import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.ShutdownSignalException;
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

public class Login extends AbstractLogin implements Interruptible, TestStateListener {

    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggingManager.getLoggerForClass();
    private final static String HEADERS = "AMQPPublisher.Headers";


    @Override
    public SampleResult sample(Entry entry) {

        trace("Trimegah Login sample()");

        SampleResult result = new SampleResult();
        result.setSampleLabel(getName());
        result.setSuccessful(false);
        result.setResponseCode("500");

        result.setSampleLabel(getTitle());

        result.sampleStart();

        try {

            LogonResponse logonResponse = login();

            result.setSamplerData(constructNiceString());

            if (SUCCESSFUL_LOGIN.equals(logonResponse.getStatus())) {
                result.setDataType(SampleResult.TEXT);
                result.setResponseCodeOK();
                result.setSuccessful(true);
            }

            result.setResponseData(logonResponse.toString(), null);
            result.setResponseMessage(logonResponse.toString());

            //TODO How the authenticated connection passed to next request.
            //TODO Success string of logon status


        } catch (ShutdownSignalException e) {
            e.printStackTrace();
            log.warn(e.getMessage());
            result.setResponseCode("400");
            result.setResponseMessage(e.getMessage());
            interrupt();
        } catch (ConsumerCancelledException e) {
            e.printStackTrace();
            log.warn(e.getMessage());
            result.setResponseCode("300");
            result.setResponseMessage(e.getMessage());
            interrupt();
        } catch (InterruptedException e) {
            e.printStackTrace();
            log.warn(e.getMessage());
            result.setResponseCode("200");
            result.setResponseMessage(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            log.warn(e.getMessage());
            result.setResponseCode("100");
            result.setResponseMessage(e.getMessage());
        } finally {
            result.sampleEnd();
        }

        trace("RPC Login sample() method ended");

        return result;
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

//    public void cleanup() {
//
//        try {
//            if (loginConsumerTag != null) {
//                channel.basicCancel(loginConsumerTag);
//            }
//        } catch(IOException e) {
//            log.error("Couldn't safely cancel the sample " + loginConsumerTag, e);
//        }
//
//        super.cleanup();
//
//    }

//    protected boolean initChannel() throws IOException, NoSuchAlgorithmException, KeyManagementException, TimeoutException {
//        boolean ret = super.initChannel();
//        channel.basicQos(2);
//        return ret;
//    }


    private void trace(String s) {
        String tl = getTitle();
        String tn = Thread.currentThread().getName();
        String th = this.toString();
        log.debug(tn + " " + tl + " " + s + " " + th);
    }

//    public void createFreshAMQPConnection() throws Exception {
//
//        initChannel();
//
//        if (loginConsumerTag == null) {
//            log.info("Creating rpc login consumer");
//            consumer = new QueueingConsumer(channel);
//        }
//
//        log.info("Starting basicConsume to Login ReplyTo Queue:"+ getLogonReplyToQueue());
//
//        loginConsumerTag = channel.basicConsume(getLogonReplyToQueue(), true, consumer);
//
//        new Thread(new LoginMessagePublisher()).start();
//
//        Delivery loginDelivery;
//
//        loginDelivery = consumer.nextDelivery(getReceiveTimeoutAsInt());
//
//        LogonResponse logonResponse = LogonResponse.parseFrom(loginDelivery.getBody());
//
//        log.info(logonResponse.toString());
//
//    }


}