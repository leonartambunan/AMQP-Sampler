package com.sxi.jmeter.protocol.async.redeemorder;

import com.sxi.jmeter.protocol.base.AbstractRabbitSampler;

public abstract class AbstractRedeemOrder extends AbstractRabbitSampler {

    private final static String REQUEST_QUEUE = "Order.RequestQueue";
    private final static String RESPONSE_EXCHANGE = "Order.ResponseExchange";
    private final static String ROUTING_KEY = "Order.RoutingKey";

    private final static String ACC_NO = "Order.AccNo";
    private final static String SESSION_ID = "Order.SessionId";
    private final static String CIF = "Order.CIF";
    private final static String ORDER_REF = "Order.OrderRef";
    private final static String PRODUCT_CODE = "Order.ProductCode";
    private final static String PRODUCT_ID = "Order.ProductId";
    private final static String UNIT_VALUE = "Order.UnitValue";
    private final static String AMOUNT_VALUE = "Order.AmountValue";

    protected String getTitle() {
        return this.getName();
    }

    public String getAmountValue() {
        return getPropertyAsString(AMOUNT_VALUE);
    }

    public void setAmountValue(String name) {
        setProperty(AMOUNT_VALUE, name);
    }

    public String getUnitValue() {
        return getPropertyAsString(UNIT_VALUE);
    }

    public void setUnitValue(String name) {
        setProperty(UNIT_VALUE, name);
    }
    public String getAccNo() {
        return getPropertyAsString(ACC_NO);
    }

    public void setAccNo(String name) {
        setProperty(ACC_NO, name);
    }

    public String getRoutingKey() {
        return getPropertyAsString(ROUTING_KEY);
    }

    public void setRoutingKey(String name) {
        setProperty(ROUTING_KEY, name);
    }

    public String getRequestQueue() {
        return getPropertyAsString(REQUEST_QUEUE,"olt.mf_subscribe_order");
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

    public String getStockCode() {
        return getPropertyAsString(ACC_NO);
    }

    public void setStockCode(String name) {
        setProperty(ACC_NO, name);
    }

    public String getSessionId() {
        return getPropertyAsString(SESSION_ID);
    }

    public void setSessionId(String name) {
        setProperty(SESSION_ID, name);
    }

    public String getCif() {return getPropertyAsString(CIF);}

    public void setCif(String name) {
        setProperty(CIF, name);
    }

    public String getOrderRef() {return getPropertyAsString(ORDER_REF);}

    public void setOrderRef(String name) {
        setProperty(ORDER_REF, name);
    }

    public String getProductCode() {return getPropertyAsString(PRODUCT_CODE);}

    public void setProductCode(String name) {
        setProperty(PRODUCT_CODE, name);
    }

    public String getProductId() {return getPropertyAsString(PRODUCT_ID);}

    public void setProductId(String name) {
        setProperty(PRODUCT_ID, name);
    }

}