package com.sxi.jmeter.protocol.rpc.login;

import id.co.tech.cakra.message.proto.olt.LogonResponse;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.property.TestElementProperty;

public class Login extends AbstractLogin {

    private static final long serialVersionUID = 1L;

    @Override
    public SampleResult sample(Entry entry) {

        trace("sample()");

        result.setSampleLabel(getName());
        result.setSuccessful(false);
        result.setResponseCode("500");
        result.setSampleLabel(getTitle());
        result.setDataType(SampleResult.TEXT);

        result.sampleStart();

        try {

            LogonResponse logonResponse = login();

            result.setSamplerData(constructNiceString());

            if (SUCCESSFUL_LOGIN.equals(logonResponse.getStatus())) {
                result.setResponseCodeOK();
                result.setSuccessful(true);
            }

            result.setResponseData(logonResponse.toString(), null);
            result.setResponseMessage(logonResponse.toString());

        } catch (Exception e) {
            e.printStackTrace();
            trace(e.getMessage());
            result.setResponseCode("400");
            result.setResponseMessage(e.getMessage());
            result.setResponseData(e.getMessage(),null);
        } finally {
            result.sampleEnd();
        }

        trace("sample() ended");

        return result;
    }

    public boolean makeRequest() {
        return true;
    }

    private final static String HEADERS = "AMQPPublisher.Headers";

    public Arguments getHeaders() {
        return (Arguments) getProperty(HEADERS).getObjectValue();
    }

    public void setHeaders(Arguments headers) {
        setProperty(new TestElementProperty(HEADERS, headers));
    }

}