package com.sxi.jmeter.protocol.amqp.assetallocation;

import com.sxi.jmeter.protocol.amqp.login.AbstractLogin;
import org.apache.jmeter.testelement.ThreadListener;

public abstract class AbstractAssetAllocation extends AbstractLogin implements ThreadListener {

    private final static String SESSION_ID = "Order.SessionId";
    private final static String REQUEST_TYPE = "Order.RequestType";

    protected String getTitle() {
        return this.getName();
    }

    public String getSessionId() {
        return getPropertyAsString(SESSION_ID);
    }

    public void setSessionId(String name) {
        setProperty(SESSION_ID, name);
    }

    public String getRequestType() {return getPropertyAsString(REQUEST_TYPE);}

    public void setRequestType(String name) {
        setProperty(REQUEST_TYPE, name);
    }


}
