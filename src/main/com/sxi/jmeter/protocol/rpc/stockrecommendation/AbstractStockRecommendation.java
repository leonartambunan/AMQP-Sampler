package com.sxi.jmeter.protocol.rpc.stockrecommendation;

import com.sxi.jmeter.protocol.base.AbstractRabbitSampler;

public abstract class AbstractStockRecommendation extends AbstractRabbitSampler {

    private final static String REQUEST_QUEUE = "StockRecommendation.RequestQueue";
    private final static String RESPONSE_QUEUE = "StockRecommendation.OrderResponseQueue";

    private final static String SESSION_ID = "StockRecommendation.SessionId";
    private final static String STOCK_ID = "StockRecommendation.StockID";
    private final static String BOARD_CODE = "StockRecommendation.BoardCode";


    protected String getTitle() {
        return this.getName();
    }

    public String getSessionId() {
        return getPropertyAsString(SESSION_ID);
    }

    public void setSessionId(String name) {
        setProperty(SESSION_ID, name);
    }

    public String getStockId() {return getPropertyAsString(STOCK_ID);}

    public void setStockId(String name) {
        setProperty(STOCK_ID, name);
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
