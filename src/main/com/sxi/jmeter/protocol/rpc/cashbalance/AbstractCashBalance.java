package com.sxi.jmeter.protocol.rpc.cashbalance;

import com.sxi.jmeter.protocol.base.AbstractRabbitSampler;

public abstract class AbstractCashBalance extends AbstractRabbitSampler {

    private final static String REQUEST_QUEUE = "CashBalance.RequestQueue";
    private final static String RESPONSE_QUEUE = "CashBalance.OrderResponseQueue";

    private final static String SESSION_ID = "CashBalance.SessionId";
    private final static String ACC_NO = "CashBalance.AccNo";

    protected String getTitle() {
        return this.getName();
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

    public String getResponseQueue() {
        return getPropertyAsString(RESPONSE_QUEUE);
    }

    public void setResponseQueue(String name) {
        setProperty(RESPONSE_QUEUE, name);
    }

    public String getRequestQueue() {
        return getPropertyAsString(REQUEST_QUEUE,"olt.CASH_BALANCE_REQUEST-rpc".toLowerCase());
    }

    public void setRequestQueue(String name) {
        setProperty(REQUEST_QUEUE, name);
    }
}
