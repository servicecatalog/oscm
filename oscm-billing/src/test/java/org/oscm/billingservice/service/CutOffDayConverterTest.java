/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Jan 24, 2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.service;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import org.oscm.billingservice.service.CutOffDayConverter;
import org.oscm.test.DateTimeHandling;

/**
 * @author tokoda
 * 
 */
public class CutOffDayConverterTest {

    @Test
    public void getBillingStartTimeForCutOffDay_DayOfMonthJustCutOffDay() {
        // given
        long invocationTime = DateTimeHandling
                .calculateMillis("2012-02-28 00:00:00");
        int cutOffDay = 28;

        // when
        long billingStartTime = CutOffDayConverter
                .getBillingStartTimeForCutOffDay(invocationTime, cutOffDay)
                .getTimeInMillis();

        // then
        assertEquals(DateTimeHandling.calculateMillis("2012-02-28 00:00:00"),
                billingStartTime);
    }

    @Test
    public void getBillingStartTimeForCutOffDay_DayOfMonthLessThanCutOffDay() {
        // given
        long invocationTime = DateTimeHandling
                .calculateMillis("2012-02-01 00:00:00");
        int cutOffDay = 15;

        // when
        long billingStartTime = CutOffDayConverter
                .getBillingStartTimeForCutOffDay(invocationTime, cutOffDay)
                .getTimeInMillis();

        // then
        assertEquals(DateTimeHandling.calculateMillis("2012-02-15 00:00:00"),
                billingStartTime);
    }

    @Test
    public void getBillingStartTimeForCutOffDay_DayOfMonthMoreThanCutOffDay() {
        // given
        long invocationTime = DateTimeHandling
                .calculateMillis("2012-02-28 00:00:00");
        int cutOffDay = 1;

        // when
        long billingStartTime = CutOffDayConverter
                .getBillingStartTimeForCutOffDay(invocationTime, cutOffDay)
                .getTimeInMillis();

        // then
        assertEquals(DateTimeHandling.calculateMillis("2012-03-01 00:00:00"),
                billingStartTime);
    }

    @Test
    public void getBillingEndTimeForCutOffDay_DayOfMonthJustCutOffDay() {
        // given
        long invocationTime = DateTimeHandling
                .calculateMillis("2012-02-28 00:00:00");

        // when
        long billingEndTime = CutOffDayConverter.getBillingEndTimeForCutOffDay(
                invocationTime).getTimeInMillis();

        // then
        assertEquals(DateTimeHandling.calculateMillis("2012-03-28 00:00:00"),
                billingEndTime);
    }

    @Test
    public void getBillingEndTimeForCutOffDay_DayOfMonthLessThanCutOffDay() {
        // given
        long invocationTime = DateTimeHandling
                .calculateMillis("2012-02-01 00:00:00");

        // when
        long billingEndTime = CutOffDayConverter.getBillingEndTimeForCutOffDay(
                invocationTime).getTimeInMillis();

        // then
        assertEquals(DateTimeHandling.calculateMillis("2012-03-01 00:00:00"),
                billingEndTime);
    }

    @Test
    public void getBillingEndTimeForCutOffDay_DayOfMonthMoreThanCutOffDay() {
        // given
        long invocationTime = DateTimeHandling
                .calculateMillis("2012-02-28 00:00:00");

        // when
        long billingEndTime = CutOffDayConverter.getBillingEndTimeForCutOffDay(
                invocationTime).getTimeInMillis();

        // then
        assertEquals(DateTimeHandling.calculateMillis("2012-03-28 00:00:00"),
                billingEndTime);
    }

}
