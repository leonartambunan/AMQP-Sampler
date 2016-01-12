package com.sxi.jmeter.protocol.amqp.cashposition;

import com.sxi.jmeter.protocol.amqp.login.AbstractLogin;
import org.apache.jmeter.testelement.ThreadListener;

public abstract class AbstractCashPosition extends AbstractLogin implements ThreadListener {

    private final static String SESSION_ID = "Order.SessionId";
    private final static String MKT_ID = "Order.MktId";
    private final static String BOARDCODE = "Order.BoardCode";

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

    public String getBoardcode() {return getPropertyAsString(BOARDCODE);}

    public void setBoardcode(String name) {
        setProperty(BOARDCODE, name);
    }


}
