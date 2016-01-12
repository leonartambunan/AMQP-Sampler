package com.sxi.jmeter.protocol.rpc.getwatchlist;

import com.sxi.jmeter.protocol.rpc.login.AbstractLogin;
import org.apache.jmeter.testelement.ThreadListener;

public abstract class AbstractGetWatchList extends AbstractLogin implements ThreadListener {

    private final static String REQUEST_QUEUE = "WatchList.RequestQueue";
    private final static String RESPONSE_QUEUE = "WatchList.ResponseQueue";
    private final static String SESSION_ID = "WatchList.SessionId";

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
        return getPropertyAsString(REQUEST_QUEUE,"olt.get_watchlist_request-rpc");
    }

    public void setRequestQueue(String name) {
        setProperty(REQUEST_QUEUE, name);
    }



}
