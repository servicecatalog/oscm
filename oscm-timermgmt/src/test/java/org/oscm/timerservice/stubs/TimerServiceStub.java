/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 19.01.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.timerservice.stubs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.ejb.EJBException;
import javax.ejb.ScheduleExpression;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;

/**
 * @author Mike J&auml;ger
 * 
 */
public class TimerServiceStub implements TimerService {

    private List<Timer> timers = new ArrayList<Timer>();

    /*
     * (non-Javadoc)
     * 
     * @see javax.ejb.TimerService#createTimer(java.util.Date,
     * java.io.Serializable)
     */
    @Override
    public Timer createTimer(Date arg0, Serializable arg1)
            throws IllegalArgumentException, IllegalStateException,
            EJBException {
        timers.add(new TimerStub(0, arg1, arg0, false));
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.ejb.TimerService#createTimer(long, java.io.Serializable)
     */
    @Override
    public Timer createTimer(long arg0, Serializable arg1)
            throws IllegalArgumentException, IllegalStateException,
            EJBException {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.ejb.TimerService#createTimer(java.util.Date, long,
     * java.io.Serializable)
     */
    @Override
    public Timer createTimer(Date arg0, long arg1, Serializable arg2)
            throws IllegalArgumentException, IllegalStateException,
            EJBException {
        timers.add(new TimerStub(arg1, arg2, arg0, false));
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.ejb.TimerService#createTimer(long, long, java.io.Serializable)
     */
    @Override
    public Timer createTimer(long arg0, long arg1, Serializable arg2)
            throws IllegalArgumentException, IllegalStateException,
            EJBException {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.ejb.TimerService#getTimers()
     */
    @Override
    public Collection<Timer> getTimers() throws IllegalStateException,
            EJBException {
        return timers;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * javax.ejb.TimerService#createCalendarTimer(javax.ejb.ScheduleExpression)
     */
    @Override
    public Timer createCalendarTimer(ScheduleExpression arg0)
            throws IllegalArgumentException, IllegalStateException,
            EJBException {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * javax.ejb.TimerService#createCalendarTimer(javax.ejb.ScheduleExpression,
     * javax.ejb.TimerConfig)
     */
    @Override
    public Timer createCalendarTimer(ScheduleExpression arg0, TimerConfig arg1)
            throws IllegalArgumentException, IllegalStateException,
            EJBException {
        TimerStub timer = new TimerStub(arg0, arg1);
        timers.add(timer);
        return timer;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.ejb.TimerService#createIntervalTimer(long, long,
     * javax.ejb.TimerConfig)
     */
    @Override
    public Timer createIntervalTimer(long arg0, long arg1, TimerConfig arg2)
            throws IllegalArgumentException, IllegalStateException,
            EJBException {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.ejb.TimerService#createIntervalTimer(java.util.Date, long,
     * javax.ejb.TimerConfig)
     */
    @Override
    public Timer createIntervalTimer(Date arg0, long arg1, TimerConfig arg2)
            throws IllegalArgumentException, IllegalStateException,
            EJBException {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.ejb.TimerService#createSingleActionTimer(long,
     * javax.ejb.TimerConfig)
     */
    @Override
    public Timer createSingleActionTimer(long arg0, TimerConfig arg1)
            throws IllegalArgumentException, IllegalStateException,
            EJBException {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.ejb.TimerService#createSingleActionTimer(java.util.Date,
     * javax.ejb.TimerConfig)
     */
    @Override
    public Timer createSingleActionTimer(Date arg0, TimerConfig arg1)
            throws IllegalArgumentException, IllegalStateException,
            EJBException {

        return null;
    }

    @Override
    public Collection<Timer> getAllTimers()
            throws IllegalStateException, EJBException {
        // TODO Auto-generated method stub
        return null;
    }

}
