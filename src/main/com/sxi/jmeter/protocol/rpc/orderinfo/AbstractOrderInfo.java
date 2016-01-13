package com.sxi.jmeter.protocol.rpc.orderinfo;

import com.sxi.jmeter.protocol.base.AbstractRabbitSampler;

public abstract class AbstractOrderInfo extends AbstractRabbitSampler {

    private final static String REQUEST_QUEUE = "OrderInfo.RequestQueue";
    private final static String RESPONSE_QUEUE = "OrderInfo.ResponseQueue";

    private final static String SESSION_ID = "OrderInfo.SessionId";
    private final static String ACC_NO = "OrderInfo.AccNo";

    protected String getTitle() {
        return this.getName();
    }

    public String getResponseQueue() {
        return getPropertyAsString(RESPONSE_QUEUE);
    }

    public void setResponseQueue(String name) {
        setProperty(RESPONSE_QUEUE, name);
    }

    public String getRequestQueue() {
        return getPropertyAsString(REQUEST_QUEUE,"olt.order_request-rpc");
    }

    public void setRequestQueue(String name) {
        setProperty(REQUEST_QUEUE, name);
    }

    public String getSessionId() {
        return getPropertyAsString(SESSION_ID);
    }

    public void setSessionId(String name) {
        setProperty(SESSION_ID, name);
    }

    public String getAccNo() {return getPropertyAsString(ACC_NO);}

    public void setAccNo(String name) {
        setProperty(ACC_NO, name);
    }



}
