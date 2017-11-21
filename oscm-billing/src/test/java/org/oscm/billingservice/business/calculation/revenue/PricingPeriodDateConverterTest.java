/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Dec 18, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.calculation.revenue;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;

import org.junit.Test;

import org.oscm.billingservice.business.calculation.revenue.PricingPeriodDateConverter;
import org.oscm.internal.types.enumtypes.PricingPeriod;

/**
 * @author tokoda
 * 
 */
public class PricingPeriodDateConverterTest {

    private static final long BASE_TIME = createTime(2012, 2, 2, 1, 2, 0, 0)
            .getTimeInMillis();

    private static final long START_HOUR = createTime(2012, 2, 2, 1, 0, 0, 0)
            .getTimeInMillis();
    private static final long START_DAY = createTime(2012, 2, 2, 0, 0, 0, 0)
            .getTimeInMillis();
    private static final long START_WEEK = createTime(2012, 1, 30, 0, 0, 0, 0)
            .getTimeInMillis();
    private static final long START_MONTH = createTime(2012, 2, 1, 0, 0, 0, 0)
            .getTimeInMillis();

    private static final long START_OF_NEXT_HOUR = createTime(2012, 2, 2, 2, 0,
            0, 0).getTimeInMillis();
    private static final long START_OF_NEXT_DAY = createTime(2012, 2, 3, 0, 0,
            0, 0).getTimeInMillis();
    private static final long START_OF_NEXT_WEEK = createTime(2012, 2, 6, 0, 0,
            0, 0).getTimeInMillis();
    private static final long START_OF_NEXT_MONTH = createTime(2012, 3, 1, 0,
            0, 0, 0).getTimeInMillis();

    @Test
    public void getStartTime_HourPeriod() {
        // given
        // when
        Calendar result = PricingPeriodDateConverter.getStartTime(BASE_TIME,
                PricingPeriod.HOUR);

        // then
        assertEquals(START_HOUR, result.getTimeInMillis());
    }

    @Test
    public void getStartTime_DayPeriod() {
        // given
        // when
        Calendar result = PricingPeriodDateConverter.getStartTime(BASE_TIME,
                PricingPeriod.DAY);

        // then
        assertEquals(START_DAY, result.getTimeInMillis());
    }

    @Test
    public void getStartTime_WeekPeriod() {
        // given
        // when
        Calendar result = PricingPeriodDateConverter.getStartTime(BASE_TIME,
                PricingPeriod.WEEK);

        // then
        assertEquals(START_WEEK, result.getTimeInMillis());
    }

    @Test
    public void getStartTime_MonthPeriod() {
        // given
        // when
        Calendar result = PricingPeriodDateConverter.getStartTime(BASE_TIME,
                PricingPeriod.MONTH);

        // then
        assertEquals(START_MONTH, result.getTimeInMillis());
    }

    @Test
    public void getStartTimeOfNextPeriod_HourPeriod() {
        // given
        // when
        Calendar result = PricingPeriodDateConverter.getStartTimeOfNextPeriod(
                BASE_TIME, PricingPeriod.HOUR);

        // then
        assertEquals(START_OF_NEXT_HOUR, result.getTimeInMillis());
    }

    @Test
    public void getStartTimeOfNextPeriod_DayPeriod() {
        // given
        // when
        Calendar result = PricingPeriodDateConverter.getStartTimeOfNextPeriod(
                BASE_TIME, PricingPeriod.DAY);

        // then
        assertEquals(START_OF_NEXT_DAY, result.getTimeInMillis());
    }

    @Test
    public void getStartTimeOfNextPeriod_WeekPeriod() {
        // given
        // when
        Calendar result = PricingPeriodDateConverter.getStartTimeOfNextPeriod(
                BASE_TIME, PricingPeriod.WEEK);

        // then
        assertEquals(START_OF_NEXT_WEEK, result.getTimeInMillis());
    }

    @Test
    public void getStartTimeOfNextPeriod_MonthPeriod() {
        // given
        // when
        Calendar result = PricingPeriodDateConverter.getStartTimeOfNextPeriod(
                BASE_TIME, PricingPeriod.MONTH);

        // then
        assertEquals(START_OF_NEXT_MONTH, result.getTimeInMillis());
    }

    private static Calendar createTime(int year, int month, int dayOfMonth,
            int hourOfDay, int minute, int second, int millisec) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month - 1);
        cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, second);
        cal.set(Calendar.MILLISECOND, millisec);
        return cal;
    }
}
