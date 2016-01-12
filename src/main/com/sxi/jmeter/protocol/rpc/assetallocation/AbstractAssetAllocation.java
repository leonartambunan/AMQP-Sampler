package com.sxi.jmeter.protocol.rpc.assetallocation;

import com.sxi.jmeter.protocol.rpc.login.AbstractLogin;
import org.apache.jmeter.testelement.ThreadListener;

public abstract class AbstractAssetAllocation extends AbstractLogin implements ThreadListener {
    private final static String REQUEST_QUEUE = "AssetAllocation.RequestQueue";
    private final static String RESPONSE_QUEUE = "AssetAllocation.ResponseQueue";
    private final static String SESSION_ID = "AssetAllocation.SessionId";
    private final static String REQUEST_TYPE = "AssetAllocation.RequestType";

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

    public String getResponseQueue() {
        return getPropertyAsString(RESPONSE_QUEUE);
    }

    public void setResponseQueue(String name) {
        setProperty(RESPONSE_QUEUE, name);
    }

    public String getRequestQueue() {
        return getPropertyAsString(REQUEST_QUEUE,"olt.asset_allocation_request-rpc");
    }

    public void setRequestQueue(String name) {
        setProperty(REQUEST_QUEUE, name);
    }
}
