package com.sxi.jmeter.protocol.amqp.accountinfo;

import com.sxi.jmeter.protocol.amqp.login.AbstractLogin;
import org.apache.jmeter.testelement.ThreadListener;

public abstract class AbstractAccountInfo extends AbstractLogin implements ThreadListener {

    private final static String SESSION_ID = "Order.SessionId";

    protected String getTitle() {
        return this.getName();
    }

    public String getSessionId() {
        return getPropertyAsString(SESSION_ID);
    }

    public void setSessionId(String name) {
        setProperty(SESSION_ID, name);
    }

}
