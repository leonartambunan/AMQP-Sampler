package com.sxi.jmeter.protocol.rpc.accinfo;

import com.sxi.jmeter.protocol.rpc.login.AbstractLogin;
import org.apache.jmeter.testelement.ThreadListener;

public abstract class AbstractAccountInfo extends AbstractLogin implements ThreadListener {
    private final static String REQUEST_QUEUE = "AccountInfo.RequestQueue";
    private final static String RESPONSE_QUEUE = "AccountInfo.OrderResponseQueue";
    private final static String SESSION_ID = "AccountInfo.SessionId";

    protected String getTitle() {
        return this.getName();
    }

    public String getSessionId() {
        return getPropertyAsString(SESSION_ID);
    }

    public void setSessionId(String name) {
        setProperty(SESSION_ID, name);
    }

    public String getResponseQueue() {
        return getPropertyAsString(RESPONSE_QUEUE);
    }

    public void setResponseQueue(String name) {
        setProperty(RESPONSE_QUEUE, name);
    }

    public String getRequestQueue() {
        return getPropertyAsString(REQUEST_QUEUE,"olt.account_request-rpc");
    }

    public void setRequestQueue(String name) {
        setProperty(REQUEST_QUEUE, name);
    }
}
