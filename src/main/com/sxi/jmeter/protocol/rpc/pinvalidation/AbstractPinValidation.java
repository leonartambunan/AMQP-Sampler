package com.sxi.jmeter.protocol.rpc.pinvalidation;

import com.sxi.jmeter.protocol.base.AbstractRabbitSampler;

public abstract class AbstractPinValidation extends AbstractRabbitSampler {

    private final static String REQUEST_QUEUE = "Order.RequestQueue";
    private final static String RESPONSE_QUEUE = "Order.ResponseQueue";

    private final static String PIN = "Order.Pin";
    private final static String SESSION_ID = "Order.SessionId";

    protected String getTitle() {
        return this.getName();
    }

    public String getRequestQueue() {
        return getPropertyAsString(REQUEST_QUEUE,"oms.order_request-rpc");
    }

    public void setRequestQueue(String name) {
        setProperty(REQUEST_QUEUE, name);
    }

    public String getResponseQueue() {
        return getPropertyAsString(RESPONSE_QUEUE,"amq.rabbitmq.reply-to");
    }

    public void setResponseQueue(String name) {
        setProperty(RESPONSE_QUEUE, name);
    }

    public String getPin() {
        return getPropertyAsString(PIN);
    }

    public void setPin(String name) {
        setProperty(PIN, name);
    }

    public String getSessionId() {return getPropertyAsString(SESSION_ID);}

    public void setSessionId(String name) {
        setProperty(SESSION_ID, name);
    }
}
