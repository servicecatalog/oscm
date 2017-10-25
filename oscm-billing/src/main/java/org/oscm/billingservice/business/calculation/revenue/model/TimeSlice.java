/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 07.01.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.calculation.revenue.model;

import java.util.Calendar;
import java.util.Date;

import org.oscm.internal.types.enumtypes.PricingPeriod;

/**
 * @author kulle
 * 
 */
public class TimeSlice {

    private final PricingPeriod period;
    private final long start;
    private final long end;

    private boolean lastSlice = false;
    private boolean firstSlice = false;

    public TimeSlice(long start, long end, PricingPeriod period) {
        this.start = start;
        this.end = end;
        this.period = period;
    }

    /**
     * Operation is based on java util calendar in order to support daylight
     * saving times.
     */
    public TimeSlice previous() {
        long newStart = start;
        long newEnd = end;
        Calendar temp = Calendar.getInstance();
        temp.setTimeInMillis(start);
        int field = 0;
        switch (period) {
        case HOUR:
            field = Calendar.HOUR_OF_DAY;
            break;
        case DAY:
            field = Calendar.DAY_OF_MONTH;
            break;
        case WEEK:
            field = Calendar.WEEK_OF_YEAR;
            break;
        case MONTH:
            field = Calendar.MONTH;
            break;
        default:
            break;
        }
        temp.add(field, -1);
        newStart = temp.getTimeInMillis();
        temp.setTimeInMillis(end);
        temp.add(field, -1);
        newEnd = temp.getTimeInMillis();
        return new TimeSlice(newStart, newEnd, period);
    }

    @Override
    public String toString() {
        return "start: " + new Date(start) + ", end: " + new Date(end)
                + ", period: " + period;
    }

    public PricingPeriod getPeriod() {
        return period;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }

    public long getStartOfNextSlice() {
        return end + 1;
    }

    public Calendar getStartOfNextSliceAsCalendar() {
        Calendar result = Calendar.getInstance();
        result.setTimeInMillis(end + 1);
        return result;
    }

    public void setLastSlice(boolean lastSlice) {
        this.lastSlice = lastSlice;
    }

    public void setFirstSlice(boolean firstSlice) {
        this.firstSlice = firstSlice;
    }

    public boolean isLastButNotFirst() {
        return lastSlice && !firstSlice;
    }

    public boolean isFirstButNotLast() {
        return firstSlice && !lastSlice;
    }

    public boolean isFirstAndLast() {
        return firstSlice && lastSlice;
    }

    public Calendar getStartAsCalendar() {
        Calendar result = Calendar.getInstance();
        result.setTimeInMillis(start);
        return result;
    }

}
