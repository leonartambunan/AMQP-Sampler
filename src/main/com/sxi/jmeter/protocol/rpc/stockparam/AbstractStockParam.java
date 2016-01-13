package com.sxi.jmeter.protocol.rpc.stockparam;

import com.sxi.jmeter.protocol.base.AbstractRabbitSampler;

public abstract class AbstractStockParam extends AbstractRabbitSampler {

    private final static String REQUEST_QUEUE = "StockParam.RequestQueue";
    private final static String RESPONSE_QUEUE = "StockParam.OrderResponseQueue";

    private final static String SESSION_ID = "StockParam.SessionId";
    private final static String MKT_ID = "StockParam.MktId";
    private final static String BOARD_CODE = "StockParam.BoardCode";

    protected String getTitle() {
        return this.getName();
    }

    public String getSessionId() {
        return getPropertyAsString(SESSION_ID);
    }

    public void setSessionId(String name) {
        setProperty(SESSION_ID, name);
    }

    public String getMktId() {return getPropertyAsString(MKT_ID);}

    public void setMktId(String name) {
        setProperty(MKT_ID, name);
    }

    public String getBoardCode() {return getPropertyAsString(BOARD_CODE);}

    public void setBoardCode(String name) {
        setProperty(BOARD_CODE, name);
    }

    public String getResponseQueue() {
        return getPropertyAsString(RESPONSE_QUEUE);
    }

    public void setResponseQueue(String name) {
        setProperty(RESPONSE_QUEUE, name);
    }

    public String getRequestQueue() {
        return getPropertyAsString(REQUEST_QUEUE,"olt.stock_param_request-rpc");
    }

    public void setRequestQueue(String name) {
        setProperty(REQUEST_QUEUE, name);
    }
}
