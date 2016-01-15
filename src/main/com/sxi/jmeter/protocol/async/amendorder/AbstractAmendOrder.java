package com.sxi.jmeter.protocol.async.amendorder;

import com.sxi.jmeter.protocol.base.AbstractRabbitSampler;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class AbstractAmendOrder extends AbstractRabbitSampler  {

    private final static String REQUEST_QUEUE = "Order.RequestExchangeName";
    private final static String RESPONSE_EXCHANGE = "Order.ResponseExchangeName";
    private final static String ROUTING_KEY = "Order.RoutingKey";
    private final static String SESSION_ID = "Order.SessionId";

    private final static String ORDER_ID = "Order.OrderID";
    private final static String ORDER_REF = "Order.OrderRef";
    private final static String ORDER_QTY = "Order.StockQty";
    private final static String ORDER_PRICE = "Order.Price";
    private final static String ORDER_PERIOD = "Order.OrderPeriod";

    protected String getTitle() {
        return this.getName();
    }

    public String getRoutingKey() {
        return getPropertyAsString(ROUTING_KEY);
    }

    public void setRoutingKey(String name) {
        setProperty(ROUTING_KEY, name);
    }

    public String getSessionId() {
        return getPropertyAsString(SESSION_ID);
    }

    public void setSessionId(String name) {
        setProperty(SESSION_ID, name);
    }

    public String getOrderRef() {
        return getPropertyAsString(ORDER_REF);
    }

    public void setOrderRef(String name) {
        setProperty(ORDER_REF, name);
    }

    public String getRequestQueue() {
        return getPropertyAsString(REQUEST_QUEUE,"olt.amend_olt_order_request");
    }

    public void setRequestQueue(String name) {
        setProperty(REQUEST_QUEUE, name);
    }

    public String getResponseExchange() {
        return getPropertyAsString(RESPONSE_EXCHANGE,"olt.order_reply");
    }

    public void setResponseExchange(String name) {
        setProperty(RESPONSE_EXCHANGE, name);
    }

    public String getOrderId() {
        return getPropertyAsString(ORDER_ID);
    }

    public void setOrderId(String name) {
        setProperty(ORDER_ID, name);
    }

    public String getOrderQty() {return getPropertyAsString(ORDER_QTY);}

    public void setOrderQty(String name) {
        setProperty(ORDER_QTY, name);
    }

    public String getOrderPrice() {return getPropertyAsString(ORDER_PRICE);}

    public void setOrderPrice(String name) {
        setProperty(ORDER_PRICE, name);
    }

    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    public Date getOrderPeriodAsDate() {
        try {
            return sdf.parse(getPropertyAsString(ORDER_PERIOD));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getOrderPeriod() {return getPropertyAsString(ORDER_PERIOD);}

    public void setOrderPeriod(String name) {
        setProperty(ORDER_PERIOD, name);
    }



}
