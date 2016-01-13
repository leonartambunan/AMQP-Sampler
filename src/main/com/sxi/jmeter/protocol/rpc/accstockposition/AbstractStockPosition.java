package com.sxi.jmeter.protocol.rpc.accstockposition;

import com.sxi.jmeter.protocol.base.AbstractRabbitSampler;

public abstract class AbstractStockPosition extends AbstractRabbitSampler {
    private final static String REQUEST_QUEUE = "StockPosition.RequestQueue";
    private final static String RESPONSE_QUEUE = "StockPosition.OrderResponseQueue";
    private final static String SESSION_ID = "StockPosition.SessionId";
    private final static String ACC_NO = "StockPosition.AccNo";
    private final static String STOCK_CODE = "StockPosition.StockCode";

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

    public String getStockCode() {return getPropertyAsString(STOCK_CODE);}

    public void setStockCode(String name) {
        setProperty(STOCK_CODE, name);
    }

    public String getResponseQueue() {
        return getPropertyAsString(RESPONSE_QUEUE);
    }

    public void setResponseQueue(String name) {
        setProperty(RESPONSE_QUEUE, name);
    }

    public String getRequestQueue() {
        return getPropertyAsString(REQUEST_QUEUE,"olt.acc_stock_pos_request-rpc");
    }

    public void setRequestQueue(String name) {
        setProperty(REQUEST_QUEUE, name);
    }
}
