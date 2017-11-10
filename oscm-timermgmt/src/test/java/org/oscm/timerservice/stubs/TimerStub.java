/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 18.06.2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.timerservice.stubs;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

import javax.ejb.EJBException;
import javax.ejb.NoSuchObjectLocalException;
import javax.ejb.ScheduleExpression;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerHandle;

/**
 * @author Mike J&auml;ger
 * 
 */
public class TimerStub implements Timer {

    Serializable info;
    long intervalDuration;
    Date execDate;
    Calendar cal;
    boolean isCanceled = false;
    ScheduleExpression scheduleExpression;

    public TimerStub() {

    }

    public TimerStub(long intervalDuration, Serializable info, Date execDate,
            boolean isCanceled) {
        this.intervalDuration = intervalDuration;
        this.info = info;
        this.execDate = execDate;
        this.isCanceled = isCanceled;
    }

    public TimerStub(ScheduleExpression scheduleExpression, TimerConfig config) {
        this.scheduleExpression = scheduleExpression;
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, 1);
        this.execDate = cal.getTime();
        this.info = config.getInfo();
    }

    public ScheduleExpression getScheduleExpression() {
        return scheduleExpression;
    }

    public void setScheduleExpression(ScheduleExpression scheduleExpression) {
        this.scheduleExpression = scheduleExpression;
    }

    public boolean isCanceled() {
        return isCanceled;
    }

    public void setCanceled(boolean isCanceled) {
        this.isCanceled = isCanceled;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.ejb.Timer#cancel()
     */
    @Override
    public void cancel() throws EJBException, IllegalStateException,
            NoSuchObjectLocalException {
        isCanceled = true;
    }

    public void setInfo(Serializable info) {
        this.info = info;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.ejb.Timer#getHandle()
     */
    @Override
    public TimerHandle getHandle() throws EJBException, IllegalStateException,
            NoSuchObjectLocalException {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.ejb.Timer#getInfo()
     */
    @Override
    public Serializable getInfo() throws EJBException, IllegalStateException,
            NoSuchObjectLocalException {
        return info;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.ejb.Timer#getNextTimeout()
     */
    @Override
    public Date getNextTimeout() throws EJBException, IllegalStateException,
            NoSuchObjectLocalException {

        return this.execDate;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.ejb.Timer#getTimeRemaining()
     */
    @Override
    public long getTimeRemaining() throws EJBException, IllegalStateException,
            NoSuchObjectLocalException {

        return 0;
    }

    public long getIntervalDuration() {
        return intervalDuration;
    }

    public Date getExecDate() {
        return execDate;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.ejb.Timer#getSchedule()
     */
    @Override
    public ScheduleExpression getSchedule() throws IllegalStateException,
            NoSuchObjectLocalException, EJBException {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.ejb.Timer#isCalendarTimer()
     */
    @Override
    public boolean isCalendarTimer() throws IllegalStateException,
            NoSuchObjectLocalException, EJBException {

        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.ejb.Timer#isPersistent()
     */
    @Override
    public boolean isPersistent() throws IllegalStateException,
            NoSuchObjectLocalException, EJBException {

        return false;
    }
}
