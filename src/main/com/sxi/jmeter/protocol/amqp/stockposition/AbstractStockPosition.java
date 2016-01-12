package com.sxi.jmeter.protocol.amqp.stockposition;

import com.sxi.jmeter.protocol.amqp.login.AbstractLogin;
import org.apache.jmeter.testelement.ThreadListener;

public abstract class AbstractStockPosition extends AbstractLogin implements ThreadListener {

    private final static String SESSION_ID = "Order.SessionId";
    private final static String ACC_NO = "Order.AccNo";
    private final static String STOCK_CODE = "Order.StockCode";

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


}
