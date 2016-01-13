package com.sxi.jmeter.protocol.rpc.orderhistory;

import com.sxi.jmeter.protocol.base.AbstractRabbitSampler;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class AbstractOrderHistory extends AbstractRabbitSampler {
    private final static String REQUEST_QUEUE = "OrderHistory.RequestQueue";
    private final static String RESPONSE_QUEUE = "OrderHistory.ResponseQueue";
    private final static String SESSION_ID = "OrderHistory.SessionId";
    private final static String ACC_NO = "OrderHistory.AccNo";
    private final static String START_DATE = "OrderHistory.StartDate";
    private final static String END_DATE = "OrderHistory.EndDate";

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

    SimpleDateFormat sdf  = new SimpleDateFormat("yyyy-MM-dd");

    public long getStartDateAsLong() {

        Date date = null;
        try {
            date = sdf.parse(getPropertyAsString(START_DATE));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (date!=null) {
            return date.getTime();
        }

        return 0L;

    }

    public long getEndDateAsLong() {

        Date date = null;
        try {
            date = sdf.parse(getPropertyAsString(END_DATE));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (date!=null) {
            return date.getTime();
        }

        return 0L;

    }

    public void setStartDate(String name) {
        setProperty(START_DATE, name);
    }

    public String getEndDate() {return getPropertyAsString(END_DATE);}

    public void setEndDate(String name) {
        setProperty(END_DATE, name);
    }

    public String getResponseQueue() {
        return getPropertyAsString(RESPONSE_QUEUE);
    }

    public void setResponseQueue(String name) {
        setProperty(RESPONSE_QUEUE, name);
    }

    public String getRequestQueue() {
        return getPropertyAsString(REQUEST_QUEUE,"olt.order_his_request-rpc");
    }

    public void setRequestQueue(String name) {
        setProperty(REQUEST_QUEUE, name);
    }
}
