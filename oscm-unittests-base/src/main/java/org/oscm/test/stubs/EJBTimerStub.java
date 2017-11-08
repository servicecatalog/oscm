/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 17.08.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.test.stubs;

import java.io.Serializable;
import java.util.Date;

import javax.ejb.EJBException;
import javax.ejb.NoSuchObjectLocalException;
import javax.ejb.ScheduleExpression;
import javax.ejb.Timer;
import javax.ejb.TimerHandle;

public class EJBTimerStub implements Timer {

    @Override
    public void cancel() throws IllegalStateException,
            NoSuchObjectLocalException, EJBException {
        throw new UnsupportedOperationException();
    }

    @Override
    public TimerHandle getHandle() throws IllegalStateException,
            NoSuchObjectLocalException, EJBException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Serializable getInfo() throws IllegalStateException,
            NoSuchObjectLocalException, EJBException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Date getNextTimeout() throws IllegalStateException,
            NoSuchObjectLocalException, EJBException {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getTimeRemaining() throws IllegalStateException,
            NoSuchObjectLocalException, EJBException {
        throw new UnsupportedOperationException();
    }

    @Override
    public ScheduleExpression getSchedule() throws IllegalStateException,
            NoSuchObjectLocalException, EJBException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isCalendarTimer() throws IllegalStateException,
            NoSuchObjectLocalException, EJBException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isPersistent() throws IllegalStateException,
            NoSuchObjectLocalException, EJBException {
        throw new UnsupportedOperationException();
    }

}
