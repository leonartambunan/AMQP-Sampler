package com.sxi.jmeter.protocol.test;

import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.testelement.ThreadListener;

public abstract class AbstractTest extends AbstractSampler implements ThreadListener {


    private static final String SCHEDULE_HOUR = "AMQPSampler.ScheduleHour";
    private static final String SCHEDULE_MINUTE = "AMQPSampler.ScheduleMinute";
    private static final String SCHEDULE_SECOND = "AMQPSampler.ScheduleSecond";

    protected AbstractTest(){
    }



    protected String getTitle() {
        return this.getName();
    }

    public String getScheduleHour() {
        return getPropertyAsString(SCHEDULE_HOUR);
    }

    public void setScheduleHour(String hour) {
        setProperty(SCHEDULE_HOUR, hour);
    }

    public String getScheduleMinute() {
        return getPropertyAsString(SCHEDULE_MINUTE);
    }

    public void setScheduleMinute(String minute) {
        setProperty(SCHEDULE_MINUTE, minute);
    }

    public String getScheduleSecond() {
        return getPropertyAsString(SCHEDULE_SECOND);
    }

    public void setScheduleSecond(String second) {
        setProperty(SCHEDULE_SECOND, second);
    }


    @Override
    public void threadFinished() {
        trace("AbstractPreOpeningSampler.threadFinished called");
    }

    @Override
    public void threadStarted() {

    }

    public void trace(String s) {
        String tl = getTitle();
//        String tn = Thread.currentThread().getName();
//        String th = this.toString();
        System.out.println(tl + "\t- " + s);
    }
}
