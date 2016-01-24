package com.sxi.jmeter.protocol.test;

import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.Interruptible;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestStateListener;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

public class Test extends AbstractTest implements Interruptible, TestStateListener {

    private static final long serialVersionUID = 7480863561320459099L;

    private transient CountDownLatch latch = new CountDownLatch(1);

    @Override
    public SampleResult sample(Entry entry) {

        SampleResult result = new SampleResult();
        result.setSampleLabel(getName());
        result.setSuccessful(false);
        result.setResponseCode("500");
        result.setSampleLabel(getTitle());
        result.setDataType(SampleResult.TEXT);
        result.sampleStart();
        result.samplePause();

        trace("Test sample() method started at "+ new Date());

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(getScheduleHour()));
        calendar.set(Calendar.MINUTE, Integer.parseInt(getScheduleMinute()));
        calendar.set(Calendar.SECOND, Integer.parseInt(getScheduleSecond()));

        Date time = calendar.getTime();

        Timer timer = new Timer();

        ScheduledExecutionTask thread = new ScheduledExecutionTask(timer, latch);
        timer.schedule(thread, time);

        try {

            latch.await();

            result.sampleResume();

            Thread.sleep(1000);

            result.setResponseMessage("REACHED");
            result.setResponseData("REACHED", null);
            result.setResponseCodeOK();
            result.setSuccessful(true);

        } catch (Exception e) {
            e.printStackTrace();
            trace(e.getMessage());
            result.setResponseCode("400");
            result.setResponseMessage(e.getMessage());
        } finally {
            if (!result.isSuccessful()) {
                result.sampleResume();
            }

            result.sampleEnd();
        }

        trace("Test sample() method ended");

        return result;
    }


    @Override
    public boolean interrupt() {
        testEnded();
        return true;
    }

    @Override
    public void testEnded() {

    }

    @Override
    public void testEnded(String arg0) {

    }

    @Override
    public void testStarted() {

    }

    @Override
    public void testStarted(String arg0) {

    }

    class ScheduledExecutionTask extends TimerTask {
        Timer timer;
        CountDownLatch latch;
        public ScheduledExecutionTask(Timer timer, CountDownLatch latch) {

            this.timer=timer;
            this.latch = latch;
        }

        public void run() {
            System.out.format("Test process starts");
            latch.countDown();
            timer.cancel(); //Terminate the timer thread
        }
    }

}