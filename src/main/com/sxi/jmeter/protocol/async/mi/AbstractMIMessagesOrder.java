package com.sxi.jmeter.protocol.async.mi;

import com.sxi.jmeter.protocol.base.AbstractNotificationSampler;

public abstract class AbstractMIMessagesOrder extends AbstractNotificationSampler {

    private final static String EXCHANGE = "Order.ExchangeName";

    private final static String ROUTING_KEY = "Order.RoutingKey";

    protected String getTitle() {
        return this.getName();
    }

    public String getRoutingKey() {
        return getPropertyAsString(ROUTING_KEY);
    }

    public void setRoutingKey(String name) {
        setProperty(ROUTING_KEY, name);
    }

    public String getExchange() {
        return getPropertyAsString(EXCHANGE);
    }

    public void setExchange(String name) {
        setProperty(EXCHANGE, name);
    }

}
