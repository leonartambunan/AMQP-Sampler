package com.sxi.jmeter.protocol.amqp.orderhistory;

import com.sxi.jmeter.protocol.amqp.login.AbstractLogin;
import org.apache.jmeter.testelement.ThreadListener;

public abstract class AbstractOrderHistory extends AbstractLogin implements ThreadListener {

    private final static String SESSION_ID = "Order.SessionId";
    private final static String ACC_NO = "Order.AccNo";
    private final static String START_DATE = "Order.StartDate";
    private final static String END_DATE = "Order.EndDate";

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

    public String getStartDate() {return getPropertyAsString(START_DATE);}

    public void setStartDate(String name) {
        setProperty(START_DATE, name);
    }

    public String getEndDate() {return getPropertyAsString(END_DATE);}

    public void setEndDate(String name) {
        setProperty(END_DATE, name);
    }


}
