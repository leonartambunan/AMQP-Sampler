package com.sxi.jmeter.protocol.async.amendorder;

import com.sxi.jmeter.protocol.base.AbstractRabbitSampler;
import com.sxi.jmeter.protocol.rpc.login.AbstractLogin;
import org.apache.jmeter.testelement.ThreadListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class AbstractAmendOrder extends AbstractRabbitSampler  {

    private final static String REQUEST_QUEUE = "Order.RequestExchangeName";
    private final static String RESPONSE_EXCHANGE = "Order.ResponseExchangeName";
    private final static String ROUTING_KEY = "Order.RoutingKey";

    private final static String STOCK_CODE = "Order.StockCode";
    private final static String ORDER_ID = "Order.OrderID";
    private final static String ORDER_QTY = "Order.StockQty";
    private final static String ORDER_PRICE = "Order.Price";
    private final static String BOARD = "Order.Board";
    private final static String INVESTOR_TYPE = "Order.InvType";
    private final static String BUY_SELL = "Order.BuySell";
    private final static String CLIENT_CODE = "Order.ClientCode";
    private final static String TIME_IN_FORCE = "Order.TimeInForce";
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

    public String getStockCode() {
        return getPropertyAsString(STOCK_CODE);
    }

    public void setStockCode(String name) {
        setProperty(STOCK_CODE, name);
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

    public String getBoard() {return getPropertyAsString(BOARD);}

    public void setBoard(String name) {
        setProperty(BOARD, name);
    }


    public String getInvestorType() {return getPropertyAsString(INVESTOR_TYPE);}

    public void setInvestorType(String name) {
        setProperty(INVESTOR_TYPE, name);
    }


    public String getTimeInForce() {return getPropertyAsString(TIME_IN_FORCE);}

    public void setTimeInForce(String name) {
        setProperty(TIME_IN_FORCE, name);
    }


    public String getBuySell() {return getPropertyAsString(BUY_SELL);}

    public void setBuySell(String name) {
        setProperty(BUY_SELL, name);
    }


    public String getClientCode() {return getPropertyAsString(CLIENT_CODE);}

    public void setClientCode(String name) {
        setProperty(CLIENT_CODE, name);
    }

    public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
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
