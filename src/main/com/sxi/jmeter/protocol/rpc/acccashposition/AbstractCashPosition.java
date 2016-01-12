package com.sxi.jmeter.protocol.rpc.acccashposition;

import com.sxi.jmeter.protocol.rpc.login.AbstractLogin;
import org.apache.jmeter.testelement.ThreadListener;

public abstract class AbstractCashPosition extends AbstractLogin implements ThreadListener {
    private final static String REQUEST_QUEUE = "CashPosition.RequestQueue";
    private final static String RESPONSE_QUEUE = "CashPosition.OrderResponseQueue";
    private final static String SESSION_ID = "CashPosition.SessionId";
    private final static String ACC_NO = "CashPosition.AccNo";

    protected String getTitle() {
        return this.getName();
    }

    public String getSessionId() {
        return getPropertyAsString(SESSION_ID);
    }

    public void setSessionId(String name) {
        setProperty(SESSION_ID, name);
    }

    public String getAccNo() {
        return getPropertyAsString(ACC_NO);
    }

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
        return getPropertyAsString(REQUEST_QUEUE,"olt.acc_cash_pos_request-rpc");
    }

    public void setRequestQueue(String name) {
        setProperty(REQUEST_QUEUE, name);
    }


}
