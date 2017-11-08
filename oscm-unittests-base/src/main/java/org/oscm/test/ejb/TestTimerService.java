/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.test.ejb;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import javax.ejb.EJBException;
import javax.ejb.ScheduleExpression;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;

public class TestTimerService implements TimerService {

    @Override
    public Timer createCalendarTimer(ScheduleExpression arg0)
            throws IllegalArgumentException, IllegalStateException,
            EJBException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Timer createCalendarTimer(ScheduleExpression arg0, TimerConfig arg1)
            throws IllegalArgumentException, IllegalStateException,
            EJBException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Timer createIntervalTimer(long arg0, long arg1, TimerConfig arg2)
            throws IllegalArgumentException, IllegalStateException,
            EJBException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Timer createIntervalTimer(Date arg0, long arg1, TimerConfig arg2)
            throws IllegalArgumentException, IllegalStateException,
            EJBException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Timer createSingleActionTimer(long arg0, TimerConfig arg1)
            throws IllegalArgumentException, IllegalStateException,
            EJBException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Timer createSingleActionTimer(Date arg0, TimerConfig arg1)
            throws IllegalArgumentException, IllegalStateException,
            EJBException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Timer createTimer(long arg0, Serializable arg1)
            throws IllegalArgumentException, IllegalStateException,
            EJBException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Timer createTimer(Date arg0, Serializable arg1)
            throws IllegalArgumentException, IllegalStateException,
            EJBException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Timer createTimer(long arg0, long arg1, Serializable arg2)
            throws IllegalArgumentException, IllegalStateException,
            EJBException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Timer createTimer(Date arg0, long arg1, Serializable arg2)
            throws IllegalArgumentException, IllegalStateException,
            EJBException {
        return null;
    }

    @Override
    public Collection<Timer> getTimers() throws IllegalStateException,
            EJBException {
        return Collections.emptyList();
    }

    @Override
    public Collection<Timer> getAllTimers() throws IllegalStateException, EJBException {
        return null;
    }

}
