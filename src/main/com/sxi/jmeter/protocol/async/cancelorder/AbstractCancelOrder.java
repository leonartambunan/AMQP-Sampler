package com.sxi.jmeter.protocol.async.cancelorder;

import com.sxi.jmeter.protocol.base.AbstractRabbitSampler;

public abstract class AbstractCancelOrder extends AbstractRabbitSampler {

    private final static String REQUEST_QUEUE = "Order.RequestExchangeName";
    private final static String RESPONSE_EXCHANGE = "Order.ResponseExchangeName";
    private final static String ROUTING_KEY = "Order.RoutingKey";
    private final static String SESSION_ID = "Order.SessionId";

    private final static String ORDER_REF = "Order.RefNo";
    private final static String ORDER_ID = "Order.Id";

    protected String getTitle() {
        return this.getName();
    }

    public String getSessionId() {
        return getPropertyAsString(SESSION_ID);
    }

    public void setSessionId(String name) {
        setProperty(SESSION_ID, name);
    }

    public String getRoutingKey() {
        return getPropertyAsString(ROUTING_KEY);
    }

    public void setRoutingKey(String name) {
        setProperty(ROUTING_KEY, name);
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

    public String getOrderRef() {
        return getPropertyAsString(ORDER_REF);
    }

    public void setOrderRef(String name) {
        setProperty(ORDER_REF, name);
    }

    public String getOrderId() {return getPropertyAsString(ORDER_ID);}

    public void setOrderId(String name) {
        setProperty(ORDER_ID, name);
    }


}
