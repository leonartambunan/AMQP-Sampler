package com.sxi.jmeter.protocol.rpc.assetallocation;

import com.sxi.jmeter.protocol.base.AbstractRabbitSampler;

public abstract class AbstractAssetAllocation extends AbstractRabbitSampler {
    private final static String REQUEST_QUEUE = "AssetAllocation.RequestQueue";
    private final static String RESPONSE_QUEUE = "AssetAllocation.ResponseQueue";
    private final static String SESSION_ID = "AssetAllocation.SessionId";
    private final static String REQUEST_TYPE = "AssetAllocation.RequestType";
    private final static String ACC_NO = "AssetAllocation.AccNo";

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

    public int getRequestTypeAsInt() {return Integer.valueOf(getPropertyAsString(REQUEST_TYPE));}

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
        return getPropertyAsString(REQUEST_QUEUE,"olt.asset_alloc_request-rpc");
    }

    public void setRequestQueue(String name) {
        setProperty(REQUEST_QUEUE, name);
    }
}
