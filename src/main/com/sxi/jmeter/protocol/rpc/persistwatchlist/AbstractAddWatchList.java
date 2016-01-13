package com.sxi.jmeter.protocol.rpc.persistwatchlist;

import com.sxi.jmeter.protocol.base.AbstractRabbitSampler;

public abstract class AbstractAddWatchList extends AbstractRabbitSampler {

    private final static String REQUEST_QUEUE = "WatchList.RequestQueue";
    private final static String RESPONSE_QUEUE = "WatchList.ResponseQueue";
    private final static String SESSION_ID = "WatchList.SessionId";
    private final static String EXCHANGE_NAME = "WatchList.ExchangeName";
    private final static String BINDING_KEY = "WatchList.BindingKey";
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
        return getPropertyAsString(REQUEST_QUEUE,"olt.persist_watchlist_request-rpc");
    }

    public void setRequestQueue(String name) {
        setProperty(REQUEST_QUEUE, name);
    }

    public String getExchangeName() {
        return getPropertyAsString(EXCHANGE_NAME,"trade_summary-d");
    }

    public void setExchangeName(String name) {
        setProperty(EXCHANGE_NAME, name);
    }

    public String getBindingKey() {
        return getPropertyAsString(BINDING_KEY);
    }

    public void setBindingKey(String name) {
        setProperty(BINDING_KEY, name);
    }


}
