package com.sxi.jmeter.protocol.async.subscribeorder;

import com.sxi.jmeter.protocol.rpc.login.AbstractLogin;
import org.apache.jmeter.testelement.ThreadListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class AbstractSubscribeOrder extends AbstractLogin implements ThreadListener {

    private final static String REQUEST_EXCHANGE_NAME = "Order.RequestExchangeName";
    private final static String RESPONSE_EXCHANGE_NAME = "Order.ResponseExchangeName";
    private final static String ROUTING_KEY = "Order.RoutingKey";

    private final static String ACC_NO = "Order.AccNo";
    private final static String SESSION_ID = "Order.SessionId";
    private final static String CIF = "Order.CIF";
    private final static String ORDER_REF = "Order.OrderRef";
    private final static String PRODUCT_CODE = "Order.ProductCode";
    private final static String PRODUCT_ID = "Order.ProductId";

    protected String getTitle() {
        return this.getName();
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

    public String getRequestExchangeName() {
        return getPropertyAsString(REQUEST_EXCHANGE_NAME,"olt.mf_subscribe_order");
    }

    public void setRequestExchangeName(String name) {
        setProperty(REQUEST_EXCHANGE_NAME, name);
    }

    public String getResponseExchangeName() {
        return getPropertyAsString(RESPONSE_EXCHANGE_NAME,"olt.order_reply");
    }

    public void setResponseExchangeName(String name) {
        setProperty(RESPONSE_EXCHANGE_NAME, name);
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
