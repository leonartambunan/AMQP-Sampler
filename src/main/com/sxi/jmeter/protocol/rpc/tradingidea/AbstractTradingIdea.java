package com.sxi.jmeter.protocol.rpc.tradingidea;

import com.sxi.jmeter.protocol.base.AbstractRabbitSampler;

public abstract class AbstractTradingIdea extends AbstractRabbitSampler {

    private final static String REQUEST_QUEUE = "TradingIdea.RequestQueue";
    private final static String RESPONSE_QUEUE = "TradingIdea.OrderResponseQueue";

    private final static String SESSION_ID = "TradingIdea.SessionId";
    private final static String BOARD_CODE = "TradingIdea.BoardCode";

    protected String getTitle() {
        return this.getName();
    }

    public String getSessionId() {
        return getPropertyAsString(SESSION_ID);
    }

    public void setSessionId(String name) {
        setProperty(SESSION_ID, name);
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
        return getPropertyAsString(REQUEST_QUEUE,"news.trading_idea_request-rpc");
    }

    public void setRequestQueue(String name) {
        setProperty(REQUEST_QUEUE, name);
    }
}
