/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 07.01.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.calculation.revenue;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;

import org.junit.Test;

import org.oscm.billingservice.business.calculation.revenue.model.TimeSlice;
import org.oscm.internal.types.enumtypes.PricingPeriod;

/**
 * @author kulle
 * 
 */
public class TimeSliceTest {

    TimeSlice slice;

    @Test
    public void previous_hour() {
        // given
        long start = createTime(2012, 12, 1, 0, 0, 0, 0);
        long end = createTime(2012, 12, 1, 0, 59, 59, 999);
        slice = new TimeSlice(start, end, PricingPeriod.HOUR);

        // when
        slice = slice.previous();

        // then
        assertEquals(createTime(2012, 11, 30, 23, 0, 0, 0), slice.getStart());
        assertEquals(createTime(2012, 11, 30, 23, 59, 59, 999), slice.getEnd());
    }

    @Test
    public void previous_day() {
        // given
        long start = createTime(2012, 12, 1, 0, 0, 0, 0);
        long end = createTime(2012, 12, 1, 23, 59, 59, 999);
        slice = new TimeSlice(start, end, PricingPeriod.DAY);

        // when
        slice = slice.previous();

        // then
        assertEquals(createTime(2012, 11, 30, 0, 0, 0, 0), slice.getStart());
        assertEquals(createTime(2012, 11, 30, 23, 59, 59, 999), slice.getEnd());
    }

    @Test
    public void previous_week() {
        // given
        long start = createTime(2012, 11, 26, 0, 0, 0, 0);
        long end = createTime(2012, 12, 2, 23, 59, 59, 999);
        slice = new TimeSlice(start, end, PricingPeriod.WEEK);

        // when
        slice = slice.previous();

        // then
        assertEquals(createTime(2012, 11, 19, 0, 0, 0, 0), slice.getStart());
        assertEquals(createTime(2012, 11, 25, 23, 59, 59, 999), slice.getEnd());
    }

    @Test
    public void previous_month() {
        // given
        long start = createTime(2012, 12, 1, 0, 0, 0, 0);
        long end = createTime(2012, 12, 31, 23, 59, 59, 999);
        slice = new TimeSlice(start, end, PricingPeriod.MONTH);

        // when
        slice = slice.previous();

        // then
        assertEquals(createTime(2012, 11, 1, 0, 0, 0, 0), slice.getStart());
        assertEquals(createTime(2012, 11, 30, 23, 59, 59, 999), slice.getEnd());
    }

    private long createTime(int year, int month, int dayOfMonth, int hourOfDay,
            int minute, int second, int millisec) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month - 1);
        cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, second);
        cal.set(Calendar.MILLISECOND, millisec);
        return cal.getTimeInMillis();
    }

}
