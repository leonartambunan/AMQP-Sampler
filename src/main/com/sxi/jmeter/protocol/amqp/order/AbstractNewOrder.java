package com.sxi.jmeter.protocol.amqp.order;

import com.sxi.jmeter.protocol.amqp.login.AbstractLogin;
import org.apache.jmeter.testelement.ThreadListener;

public abstract class AbstractNewOrder extends AbstractLogin implements ThreadListener {

    private final static String STOCK_CODE = "Order.StockCode";
    private final static String STOCK_QTY = "Order.StockQty";

    protected String getTitle() {
        return this.getName();
    }

    public String getStockCode() {
        return getPropertyAsString(STOCK_CODE);
    }

    public void setStockCode(String name) {
        setProperty(STOCK_CODE, name);
    }

    public String getStockQty() {
        return getPropertyAsString(STOCK_QTY);
    }

    public void setStockQty(String name) {
        setProperty(STOCK_QTY, name);
    }


}
