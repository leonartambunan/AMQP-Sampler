package com.sxi.jmeter.protocol.rpc.logout;

import com.sxi.jmeter.protocol.base.AbstractRabbitSampler;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

public abstract class AbstractLogout extends AbstractRabbitSampler {

    private final static String REQUEST_QUEUE = "Logout.RequestQueue";
    private final static String RESPONSE_QUEUE = "Logout.ResponseQueue";
    private final static String SESSION_ID = "Mobile.SessionId";
    private final static String LOGOUT_REASON = "Logout.Reason";

    private static final Logger log = LoggingManager.getLoggerForClass();

    protected String getTitle() {
        return this.getName();
    }

    public String getSessionId() {
        return getPropertyAsString(SESSION_ID);
    }

    public void setSessionId(String name) {
        setProperty(SESSION_ID, name);
    }

  public String getLogoutReason() {
        return getPropertyAsString(LOGOUT_REASON);
    }

    public void setLogoutReason(String name) {
        setProperty(LOGOUT_REASON, name);
    }

    public String getResponseQueue() {
        return getPropertyAsString(RESPONSE_QUEUE);
    }

    public void setResponseQueue(String name) {
        setProperty(RESPONSE_QUEUE, name);
    }

    public String getRequestQueue() {
        return getPropertyAsString(REQUEST_QUEUE,"olt.logout_request-rpc");
    }

    public void setRequestQueue(String name) {
        setProperty(REQUEST_QUEUE, name);
    }

    @Override
    public void threadFinished() {
        trace("threadFinished() called");
        cleanup();
    }

    @Override
    public void threadStarted() {

    }

}
