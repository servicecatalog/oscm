/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Dec 10, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.calculation.revenue;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import org.oscm.billingservice.service.model.BillingInput;
import org.oscm.test.DateTimeHandling;
import org.oscm.internal.types.enumtypes.PricingPeriod;
import org.oscm.internal.types.exception.IllegalArgumentException;

/**
 * @author tokoda
 * 
 */
public class CostCalculatorPerUnitComputeFactorTest {

    private CostCalculatorPerUnit calculator;

    @Before
    public void setup() {
        calculator = new CostCalculatorPerUnit();
    }

    @Test(expected = NullPointerException.class)
    public void computeFactor_NullPricingPeriodType() {
        // given
        long startTimeUsage = DateTimeHandling
                .calculateMillis("2012-01-01 00:00:00");
        long endTimeUsage = DateTimeHandling
                .calculateMillis("2012-01-31 23:59:59");
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-01-01 00:00:00", "2012-02-01 00:00:00");

        // when
        calculator.computeFactor(null, billingInput, startTimeUsage,
                endTimeUsage, true, true);

        // then a NullPointerException is expected.
    }

    @Test
    public void computeFactor_UsageTimeOutsideOfBillingPeriod() {
        // given
        long startTimeUsage = 0;
        long endTimeUsage = 0;
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-01-01 00:00:00", "2012-02-01 00:00:00");

        // when
        double factor = calculator.computeFactor(PricingPeriod.HOUR,
                billingInput, startTimeUsage, endTimeUsage, true, true);

        // then
        assertEquals(0D, factor, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void computeFactor_OnlyStartTime() {
        // given
        long startTimeUsage = DateTimeHandling
                .calculateMillis("2012-01-01 01:00:00");
        long endTimeUsage = 0;
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-01-01 00:00:00", "2012-02-01 00:00:00");

        // when
        calculator.computeFactor(PricingPeriod.HOUR, billingInput,
                startTimeUsage, endTimeUsage, true, true);

        // then an IllegalArgumentException is thrown
    }

    @Test
    public void computeFactor_OnlyEndTime() {
        // given
        long startTimeUsage = 0;
        long endTimeUsage = DateTimeHandling
                .calculateMillis("2012-01-31 22:59:59");
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-01-01 00:00:00", "2012-02-01 00:00:00");

        // when
        double factor = calculator.computeFactor(PricingPeriod.HOUR,
                billingInput, startTimeUsage, endTimeUsage, true, true);

        // then
        assertEquals(743, factor, 0);
    }

    @Test
    public void computeFactor_OneHourInNextBillingPeriod() {
        long startTimeUsage = DateTimeHandling
                .calculateMillis("2012-01-31 23:00:00");
        long endTimeUsage = DateTimeHandling
                .calculateMillis("2012-02-01 01:00:00");
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-01-01 00:00:00", "2012-02-01 00:00:00");

        // when
        double factor = calculator.computeFactor(PricingPeriod.HOUR,
                billingInput, startTimeUsage, endTimeUsage, true, true);

        // then
        assertEquals(1, factor, 0);

    }

    @Test
    public void computeFactor_TwoHoursInFirstBillingPeriod() {
        long startTimeUsage = DateTimeHandling
                .calculateMillis("2012-01-31 22:00:00");
        long endTimeUsage = DateTimeHandling
                .calculateMillis("2012-01-31 23:59:59");
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-01-01 00:00:00", "2012-02-01 00:00:00");

        // when
        double factor = calculator.computeFactor(PricingPeriod.HOUR,
                billingInput, startTimeUsage, endTimeUsage, true, true);

        // then
        assertEquals(2, factor, 0);

    }

    @Test(expected = NullPointerException.class)
    public void computeFactor_OutOfTheBillingPeriod() {
        // given
        long startTimeUsage = DateTimeHandling
                .calculateMillis("2012-01-01 00:00:00");
        long endTimeUsage = DateTimeHandling
                .calculateMillis("2012-01-02 23:59:59");
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-01-03 00:00:00", "2012-02-01 00:00:00");

        // when
        calculator.computeFactor(null, billingInput, startTimeUsage,
                endTimeUsage, true, true);

        // then a NullPointerException is expected.
    }

    @Test
    public void computeFactor_OverTheBillingPeriodForHour() {
        // given
        long startTimeUsage = DateTimeHandling
                .calculateMillis("2011-12-31 23:59:59");
        long endTimeUsage = DateTimeHandling
                .calculateMillis("2012-02-01 00:00:00");
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-01-01 00:00:00", "2012-02-01 00:00:00");

        // when
        double factor = calculator.computeFactor(PricingPeriod.HOUR,
                billingInput, startTimeUsage, endTimeUsage, true, true);

        // then
        assertEquals(744, factor, 0);
    }

    @Test
    public void computeFactor_WholeBillingPeriodForHour() {
        // given
        long startTimeUsage = DateTimeHandling
                .calculateMillis("2012-01-01 00:00:00");
        long endTimeUsage = DateTimeHandling
                .calculateMillis("2012-01-31 23:59:59");
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-01-01 00:00:00", "2012-02-01 00:00:00");

        // when
        double factor = calculator.computeFactor(PricingPeriod.HOUR,
                billingInput, startTimeUsage, endTimeUsage, true, true);

        // then
        assertEquals(744, factor, 0);
    }

    @Test
    public void computeFactor_JustOneUsageForHour() {
        // given
        long startTimeUsage = DateTimeHandling
                .calculateMillis("2012-01-01 01:00:00");
        long endTimeUsage = DateTimeHandling
                .calculateMillis("2012-01-01 01:59:59");
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-01-01 00:00:00", "2012-02-01 00:00:00");

        // when
        double factor = calculator.computeFactor(PricingPeriod.HOUR,
                billingInput, startTimeUsage, endTimeUsage, true, true);

        // then
        assertEquals(1, factor, 0);
    }

    @Test
    public void computeFactor_SmallUsageForHour() {
        // given
        long startTimeUsage = DateTimeHandling
                .calculateMillis("2012-01-31 23:59:58");
        long endTimeUsage = DateTimeHandling
                .calculateMillis("2012-01-31 23:59:59");
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-01-01 00:00:00", "2012-02-01 00:00:00");

        // when
        double factor = calculator.computeFactor(PricingPeriod.HOUR,
                billingInput, startTimeUsage, endTimeUsage, true, true);

        // then
        assertEquals(1, factor, 0);
    }

    @Test
    public void computeFactor_UsageTimeZeroMilliseconds() {
        // given
        long startTimeUsage = DateTimeHandling
                .calculateMillis("2012-01-31 23:59:59");
        long endTimeUsage = DateTimeHandling
                .calculateMillis("2012-01-31 23:59:59");
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-01-01 00:00:00", "2012-02-01 00:00:00");

        // when
        double factor = calculator.computeFactor(PricingPeriod.HOUR,
                billingInput, startTimeUsage, endTimeUsage, true, true);

        // then
        assertEquals(1, factor, 0);
    }

    @Test
    public void computeFactor_OverOneUsageForHour() {
        // given
        long startTimeUsage = DateTimeHandling
                .calculateMillis("2012-01-01 00:59:59");
        long endTimeUsage = DateTimeHandling
                .calculateMillis("2012-01-01 02:00:00");
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-01-01 00:00:00", "2012-02-01 00:00:00");

        // when
        double factor = calculator.computeFactor(PricingPeriod.HOUR,
                billingInput, startTimeUsage, endTimeUsage, true, true);

        // then
        assertEquals(3, factor, 0);
    }

    @Test
    public void computeFactor_SummerTimeHour() {
        // given the day when switching to summer time
        long startTimeUsage = DateTimeHandling
                .calculateMillis("2012-03-25 00:00:00");
        long endTimeUsage = DateTimeHandling
                .calculateMillis("2012-03-25 23:59:59");
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-03-01 00:00:00", "2012-04-01 00:00:00");

        // when
        double factor = calculator.computeFactor(PricingPeriod.HOUR,
                billingInput, startTimeUsage, endTimeUsage, true, true);

        if (DateTimeHandling.isDayLightSaving(startTimeUsage, endTimeUsage)) {
            // then day has 23 hours
            assertEquals(23, factor, 0);
        } else {
            // then day has 24 hours
            assertEquals(24, factor, 0);
        }

    }

    @Test
    public void computeFactor_WinterTimeHour() {
        // given the day when switching to winter time
        long startTimeUsage = DateTimeHandling
                .calculateMillis("2012-10-28 00:00:00");
        long endTimeUsage = DateTimeHandling
                .calculateMillis("2012-10-28 23:59:59");
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-10-01 00:00:00", "2012-11-01 00:00:00");

        // when
        double factor = calculator.computeFactor(PricingPeriod.HOUR,
                billingInput, startTimeUsage, endTimeUsage, true, true);

        if (DateTimeHandling.isDayLightSaving(startTimeUsage, endTimeUsage)) {
            // then day has 25 hours
            assertEquals(25, factor, 0);
        } else {
            // then day has 24 hours
            assertEquals(24, factor, 0);
        }
    }

    @Test
    public void computeFactor_SummerTimeDay() {
        // given the day when switching to summer time
        long startTimeUsage = DateTimeHandling
                .calculateMillis("2012-03-22 00:00:00");
        long endTimeUsage = DateTimeHandling
                .calculateMillis("2012-03-31 23:59:59");
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-03-01 00:00:00", "2012-04-01 00:00:00");

        // when
        double factor = calculator.computeFactor(PricingPeriod.DAY,
                billingInput, startTimeUsage, endTimeUsage, true, true);

        // then
        assertEquals(10, factor, 0);
    }

    @Test
    public void computeFactor_WinterTimeDay() {
        // given the day when switching to winter time
        long startTimeUsage = DateTimeHandling
                .calculateMillis("2012-10-21 00:00:00");
        long endTimeUsage = DateTimeHandling
                .calculateMillis("2012-10-30 23:59:59");
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-10-01 00:00:00", "2012-11-01 00:00:00");

        // when
        double factor = calculator.computeFactor(PricingPeriod.DAY,
                billingInput, startTimeUsage, endTimeUsage, true, true);

        // then
        assertEquals(10, factor, 0);
    }

    @Test
    public void computeFactor_SummerTimeWeek() {
        // given the day when switching to summer time
        long startTimeUsage = DateTimeHandling
                .calculateMillis("2012-03-13 00:00:00");
        long endTimeUsage = DateTimeHandling
                .calculateMillis("2012-03-25 23:59:59");
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-03-01 00:00:00", "2012-04-01 00:00:00");

        // when
        double factor = calculator.computeFactor(PricingPeriod.WEEK,
                billingInput, startTimeUsage, endTimeUsage, true, true);

        // then
        assertEquals(2, factor, 0);
    }

    @Test
    public void computeFactor_WinterTimeWeek() {
        // given the day when switching to winter time
        long startTimeUsage = DateTimeHandling
                .calculateMillis("2012-10-17 00:00:00");
        long endTimeUsage = DateTimeHandling
                .calculateMillis("2012-10-27 23:59:59");
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-10-01 00:00:00", "2012-11-01 00:00:00");

        // when
        double factor = calculator.computeFactor(PricingPeriod.WEEK,
                billingInput, startTimeUsage, endTimeUsage, true, true);

        // then
        assertEquals(2, factor, 0);
    }

    @Test
    public void computeFactor_SummerTimeMonth() {
        // given the day when switching to summer time
        long startTimeUsage = DateTimeHandling
                .calculateMillis("2012-01-13 00:00:00");
        long endTimeUsage = DateTimeHandling
                .calculateMillis("2012-04-25 23:59:59");
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-01-01 00:00:00", "2012-05-01 00:00:00");

        // when
        double factor = calculator.computeFactor(PricingPeriod.MONTH,
                billingInput, startTimeUsage, endTimeUsage, true, true);

        // then
        assertEquals(4, factor, 0);
    }

    @Test
    public void computeFactor_WinterTimeMonth() {
        // given the day when switching to winter time
        long startTimeUsage = DateTimeHandling
                .calculateMillis("2012-09-03 00:00:00");
        long endTimeUsage = DateTimeHandling
                .calculateMillis("2012-11-08 23:59:59");
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-09-01 00:00:00", "2012-12-01 00:00:00");

        // when
        double factor = calculator.computeFactor(PricingPeriod.MONTH,
                billingInput, startTimeUsage, endTimeUsage, true, true);

        // then
        assertEquals(3, factor, 0);
    }

    @Test
    public void computeFactor_FebruaryOfLeapYear() {
        // given February of leap year with 29 days
        long startTimeUsage = DateTimeHandling
                .calculateMillis("2012-02-01 00:00:00");
        long endTimeUsage = DateTimeHandling
                .calculateMillis("2012-02-29 23:59:59");
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-02-01 00:00:00", "2012-03-01 00:00:00");

        // when
        double factor = calculator.computeFactor(PricingPeriod.DAY,
                billingInput, startTimeUsage, endTimeUsage, true, true);

        // then factor is 29 (days)
        assertEquals(29, factor, 0);
    }

    @Test
    public void computeFactor_TwoMonthsOfLeapYear() {
        // given two months of leap year (with 29 days for February)
        long startTimeUsage = DateTimeHandling
                .calculateMillis("2012-02-01 00:00:00");
        long endTimeUsage = DateTimeHandling
                .calculateMillis("2012-03-01 23:59:59");
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-02-01 00:00:00", "2012-03-02 00:00:00");

        // when
        double factor = calculator.computeFactor(PricingPeriod.MONTH,
                billingInput, startTimeUsage, endTimeUsage, true, true);

        // then factor is 1 months
        assertEquals(1, factor, 0);
    }

    @Test
    public void computeFactor_OverTheBillingPeriodForDay() {
        // given
        long startTimeUsage = DateTimeHandling
                .calculateMillis("2011-12-31 23:59:59");
        long endTimeUsage = DateTimeHandling
                .calculateMillis("2012-02-01 00:00:00");
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-01-01 00:00:00", "2012-02-01 00:00:00");

        // when
        double factor = calculator.computeFactor(PricingPeriod.DAY,
                billingInput, startTimeUsage, endTimeUsage, true, true);

        // then
        assertEquals(31, factor, 0);
    }

    @Test
    public void computeFactor_JustOneUsageForDay() {
        // given
        long startTimeUsage = DateTimeHandling
                .calculateMillis("2012-02-29 00:00:00");
        long endTimeUsage = DateTimeHandling
                .calculateMillis("2012-02-29 23:59:59");
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-02-01 00:00:00", "2012-03-01 00:00:00");

        // when
        double factor = calculator.computeFactor(PricingPeriod.DAY,
                billingInput, startTimeUsage, endTimeUsage, true, true);

        // then
        assertEquals(1, factor, 0);
    }

    @Test
    public void computeFactor_SmallUsageForDay() {
        // given
        long startTimeUsage = DateTimeHandling
                .calculateMillis("2012-02-29 23:59:58");
        long endTimeUsage = DateTimeHandling
                .calculateMillis("2012-02-29 23:59:59");
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-02-01 00:00:00", "2012-03-01 00:00:00");

        // when
        double factor = calculator.computeFactor(PricingPeriod.DAY,
                billingInput, startTimeUsage, endTimeUsage, true, true);

        // then
        assertEquals(1, factor, 0);
    }

    @Test
    public void computeFactor_OverOneUsageForDay() {
        // given
        long startTimeUsage = DateTimeHandling
                .calculateMillis("2012-02-01 23:59:59");
        long endTimeUsage = DateTimeHandling
                .calculateMillis("2012-02-03 00:00:00");
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-02-01 00:00:00", "2012-03-01 00:00:00");

        // when
        double factor = calculator.computeFactor(PricingPeriod.DAY,
                billingInput, startTimeUsage, endTimeUsage, true, true);

        // then
        assertEquals(3, factor, 0);
    }

    @Test
    public void computeFactor_OverTheBillingPeriodForWeek() {
        // given
        long startTimeUsage = DateTimeHandling
                .calculateMillis("2012-01-15 23:59:59");
        long endTimeUsage = DateTimeHandling
                .calculateMillis("2012-03-15 00:00:00");
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-02-01 00:00:00", "2012-03-01 00:00:00");

        // when
        double factor = calculator.computeFactor(PricingPeriod.WEEK,
                billingInput, startTimeUsage, endTimeUsage, true, true);

        // then
        assertEquals(4, factor, 0);
    }

    @Test
    public void computeFactor_WholeBillingPeriodForWeek() {
        // given
        long startTimeUsage = DateTimeHandling
                .calculateMillis("2012-02-01 00:00:00");
        long endTimeUsage = DateTimeHandling
                .calculateMillis("2012-02-29 23:59:59");
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-02-01 00:00:00", "2012-03-01 00:00:00");

        // when
        double factor = calculator.computeFactor(PricingPeriod.WEEK,
                billingInput, startTimeUsage, endTimeUsage, true, true);

        // then
        assertEquals(4, factor, 0);
    }

    @Test
    public void computeFactor_WholeBillingPeriodForWeek2() {
        // given
        long startTimeUsage = DateTimeHandling
                .calculateMillis("2012-02-01 00:00:00");
        long endTimeUsage = DateTimeHandling
                .calculateMillis("2012-02-29 23:59:59");
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-02-01 00:00:00", "2012-03-01 00:00:00");

        // when
        double factor = calculator.computeFactor(PricingPeriod.WEEK,
                billingInput, startTimeUsage, endTimeUsage, true, true);

        // then
        assertEquals(4, factor, 0);
    }

    @Test
    public void computeFactor_JustOneUsageForWeek() {
        // given
        long startTimeUsage = DateTimeHandling
                .calculateMillis("2012-02-06 00:00:00");
        long endTimeUsage = DateTimeHandling
                .calculateMillis("2012-02-12 23:59:59");
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-02-01 00:00:00", "2012-03-01 00:00:00");

        // when
        double factor = calculator.computeFactor(PricingPeriod.WEEK,
                billingInput, startTimeUsage, endTimeUsage, true, true);

        // then
        assertEquals(1, factor, 0);
    }

    @Test
    public void computeFactor_SmallUsageForWeek() {
        // given
        long startTimeUsage = DateTimeHandling
                .calculateMillis("2012-02-12 23:59:58");
        long endTimeUsage = DateTimeHandling
                .calculateMillis("2012-02-12 23:59:59");
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-02-01 00:00:00", "2012-03-01 00:00:00");

        // when
        double factor = calculator.computeFactor(PricingPeriod.WEEK,
                billingInput, startTimeUsage, endTimeUsage, true, true);

        // then
        assertEquals(1, factor, 0);
    }

    @Test
    public void computeFactor_OverUsageForWeek() {
        // given
        long startTimeUsage = DateTimeHandling
                .calculateMillis("2012-02-12 23:59:59");
        long endTimeUsage = DateTimeHandling
                .calculateMillis("2012-02-20 00:00:00");
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-02-01 00:00:00", "2012-03-01 00:00:00");

        // when
        double factor = calculator.computeFactor(PricingPeriod.WEEK,
                billingInput, startTimeUsage, endTimeUsage, true, true);

        // then
        assertEquals(3, factor, 0);
    }

    @Test
    public void computeFactor_FirstWeekIncludingEndOfLastBilling() {
        // given
        long startTimeUsage = DateTimeHandling
                .calculateMillis("2012-01-31 23:59:58");
        long endTimeUsage = DateTimeHandling
                .calculateMillis("2012-01-31 23:59:59");
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-02-01 00:00:00", "2012-03-01 00:00:00");

        // when
        double factor = calculator.computeFactor(PricingPeriod.WEEK,
                billingInput, startTimeUsage, endTimeUsage, true, true);

        // then
        assertEquals(1, factor, 0);
    }

    @Test
    public void computeFactor_LastWeekIncludingEndOfBilling() {
        // given
        long startTimeUsage = DateTimeHandling
                .calculateMillis("2012-02-27 00:00:00");
        long endTimeUsage = DateTimeHandling
                .calculateMillis("2012-02-29 23:59:59");
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-02-01 00:00:00", "2012-03-01 00:00:00");

        // when
        double factor = calculator.computeFactor(PricingPeriod.WEEK,
                billingInput, startTimeUsage, endTimeUsage, true, true);

        // then
        assertEquals(0, factor, 0);
    }

    @Test
    public void computeFactor_OverTheBillingPeriodForMonth() {
        // given
        long startTimeUsage = DateTimeHandling
                .calculateMillis("2011-12-31 23:59:59");
        long endTimeUsage = DateTimeHandling
                .calculateMillis("2012-02-01 00:00:00");
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-01-01 00:00:00", "2012-02-01 00:00:00");

        // when
        double factor = calculator.computeFactor(PricingPeriod.MONTH,
                billingInput, startTimeUsage, endTimeUsage, true, true);

        // then
        assertEquals(1, factor, 0);
    }

    @Test
    public void computeFactor_WholeBillingPeriodForMonth() {
        // given
        long startTimeUsage = DateTimeHandling
                .calculateMillis("2012-01-01 00:00:00");
        long endTimeUsage = DateTimeHandling
                .calculateMillis("2012-01-31 23:59:59");
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-01-01 00:00:00", "2012-02-01 00:00:00");

        // when
        double factor = calculator.computeFactor(PricingPeriod.MONTH,
                billingInput, startTimeUsage, endTimeUsage, true, true);

        // then
        assertEquals(1, factor, 0);
    }

    @Test
    public void computeFactor_JustOneUsageForMonth() {
        // given
        long startTimeUsage = DateTimeHandling
                .calculateMillis("2012-02-01 01:00:00");
        long endTimeUsage = DateTimeHandling
                .calculateMillis("2012-02-29 23:59:59");
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-01-01 00:00:00", "2012-04-01 00:00:00");

        // when
        double factor = calculator.computeFactor(PricingPeriod.MONTH,
                billingInput, startTimeUsage, endTimeUsage, true, true);

        // then
        assertEquals(1, factor, 0);
    }

    @Test
    public void computeFactor_ForTwoMonthIn2Years() {
        // given
        long startTimeUsage = DateTimeHandling
                .calculateMillis("2012-12-01 00:00:00");
        long endTimeUsage = DateTimeHandling
                .calculateMillis("2013-02-23 23:59:59");
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-12-01 00:00:00", "2013-02-24 00:00:00");

        // when
        double factor = calculator.computeFactor(PricingPeriod.MONTH,
                billingInput, startTimeUsage, endTimeUsage, true, true);

        // then
        assertEquals(2, factor, 0);
    }

    @Test
    public void computeFactor_ForTwoYears() {
        // given
        long startTimeUsage = DateTimeHandling
                .calculateMillis("2012-12-01 00:00:00");
        long endTimeUsage = DateTimeHandling
                .calculateMillis("2014-02-23 23:59:59");
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-12-01 00:00:00", "2014-02-24 00:00:00");

        // when
        double factor = calculator.computeFactor(PricingPeriod.MONTH,
                billingInput, startTimeUsage, endTimeUsage, true, true);

        // then
        assertEquals(14, factor, 0);
    }

    @Test
    public void computeFactor_SmallUsageForMonth() {
        // given
        long startTimeUsage = DateTimeHandling
                .calculateMillis("2012-01-31 23:59:58");
        long endTimeUsage = DateTimeHandling
                .calculateMillis("2012-01-31 23:59:59");
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-01-01 00:00:00", "2012-02-01 00:00:00");

        // when
        double factor = calculator.computeFactor(PricingPeriod.MONTH,
                billingInput, startTimeUsage, endTimeUsage, true, true);

        // then
        assertEquals(1, factor, 0);
    }

    @Test
    public void computeFactor_OverOneUsageForMonth() {
        // given
        long startTimeUsage = DateTimeHandling
                .calculateMillis("2012-01-01 23:59:59");
        long endTimeUsage = DateTimeHandling
                .calculateMillis("2012-03-01 00:00:00");
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-01-01 00:00:00", "2012-04-01 00:00:00");

        // when
        double factor = calculator.computeFactor(PricingPeriod.MONTH,
                billingInput, startTimeUsage, endTimeUsage, true, true);

        // then
        assertEquals(3, factor, 0);
    }

    @Test
    public void computeFactor_BillingPeriodForOneYear() {
        // given
        long startTimeUsage = DateTimeHandling
                .calculateMillis("2012-01-01 23:59:59");
        long endTimeUsage = DateTimeHandling
                .calculateMillis("2013-02-01 00:00:00");
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-01-01 00:00:00", "2013-01-01 00:00:00");

        // when
        double factor = calculator.computeFactor(PricingPeriod.MONTH,
                billingInput, startTimeUsage, endTimeUsage, true, true);

        // then
        assertEquals(12, factor, 0);
    }

    @Test
    public void computeFactor_NotExtendsPeriodHour() {
        // given
        long startTimeUsage = DateTimeHandling
                .calculateMillis("2012-02-01 02:20:00");
        long endTimeUsage = DateTimeHandling
                .calculateMillis("2012-02-01 03:50:00");
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-02-01 00:00:00", "2012-03-01 00:00:00");

        // when
        double factor = calculator.computeFactor(PricingPeriod.HOUR,
                billingInput, startTimeUsage, endTimeUsage, false, false);

        // then (2:20 ~ 3:50) / 1 hour
        assertEquals(1.50000, factor, 0);
    }

    @Test
    public void computeFactor_ExtendsOnlyPeriodStartHour() {
        // given
        long startTimeUsage = DateTimeHandling
                .calculateMillis("2012-02-01 02:20:00");
        long endTimeUsage = DateTimeHandling
                .calculateMillis("2012-02-01 03:50:00");
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-02-01 00:00:00", "2012-03-01 00:00:00");

        // when
        double factor = calculator.computeFactor(PricingPeriod.HOUR,
                billingInput, startTimeUsage, endTimeUsage, true, false);

        // then (2:00 ~ 3:50) / 1 hour
        assertEquals(1.83333333333333333, factor, 0);
    }

    @Test
    public void computeFactor_ExtendsOnlyPeriodEndHour() {
        // given
        long startTimeUsage = DateTimeHandling
                .calculateMillis("2012-02-01 02:20:00");
        long endTimeUsage = DateTimeHandling
                .calculateMillis("2012-02-01 03:50:00");
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-02-01 00:00:00", "2012-03-01 00:00:00");

        // when
        double factor = calculator.computeFactor(PricingPeriod.HOUR,
                billingInput, startTimeUsage, endTimeUsage, false, true);

        // then (2:20 ~ 4:00) / 1 hour
        assertEquals(1.66666666666666667, factor, 0);
    }

    @Test
    public void computeFactor_NotExtendsPeriodDay() {
        // given
        long startTimeUsage = DateTimeHandling
                .calculateMillis("2012-02-03 02:20:00");
        long endTimeUsage = DateTimeHandling
                .calculateMillis("2012-02-04 03:50:00");
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-02-01 00:00:00", "2012-03-01 00:00:00");

        // when
        double factor = calculator.computeFactor(PricingPeriod.DAY,
                billingInput, startTimeUsage, endTimeUsage, false, false);

        // then (03 Feb 2012 2:20 ~ 04 Feb 2012 3:50) / 1 day
        assertEquals(1.0625, factor, 0);
    }

    @Test
    public void computeFactor_ExtendsOnlyPeriodStartDay() {
        // given
        long startTimeUsage = DateTimeHandling
                .calculateMillis("2012-02-03 02:20:00");
        long endTimeUsage = DateTimeHandling
                .calculateMillis("2012-02-04 03:50:00");
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-02-01 00:00:00", "2012-03-01 00:00:00");

        // when
        double factor = calculator.computeFactor(PricingPeriod.DAY,
                billingInput, startTimeUsage, endTimeUsage, true, false);

        // then (03 Feb 2012 0:00 ~ 04 Feb 2012 3:50) / 1 day
        assertEquals(1.15972222222222222, factor, 0);
    }

    @Test
    public void computeFactor_ExtendsOnlyPeriodEndDay() {
        // given
        long startTimeUsage = DateTimeHandling
                .calculateMillis("2012-02-03 02:20:00");
        long endTimeUsage = DateTimeHandling
                .calculateMillis("2012-02-04 03:50:00");
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-02-01 00:00:00", "2012-03-01 00:00:00");

        // when
        double factor = calculator.computeFactor(PricingPeriod.DAY,
                billingInput, startTimeUsage, endTimeUsage, false, true);

        // then (03 Feb 2012 2:20 ~ 05 Feb 2012 0:00) / 1 day
        assertEquals(1.90277777777777778, factor, 0);
    }

    @Test
    public void computeFactor_NotExtendsPeriodWeek() {
        // given
        long startTimeUsage = DateTimeHandling
                .calculateMillis("2012-02-07 02:20:00");
        long endTimeUsage = DateTimeHandling
                .calculateMillis("2012-02-015 03:50:00");
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-02-01 00:00:00", "2012-03-01 00:00:00");

        // when
        double factor = calculator.computeFactor(PricingPeriod.WEEK,
                billingInput, startTimeUsage, endTimeUsage, false, false);

        // then (1 week + 1 day + 1 hour + 30 min) / 1 week
        assertEquals(1.1517857142857144, factor, 0);
    }

    @Test
    public void computeFactor_ExtendsOnlyPeriodStartWeek() {
        // given
        long startTimeUsage = DateTimeHandling
                .calculateMillis("2012-02-07 02:20:00");
        long endTimeUsage = DateTimeHandling
                .calculateMillis("2012-02-15 03:50:00");
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-02-01 00:00:00", "2012-03-01 00:00:00");

        // when
        double factor = calculator.computeFactor(PricingPeriod.WEEK,
                billingInput, startTimeUsage, endTimeUsage, true, false);

        // then 1 week + (13 Feb 2012 0:00 ~ 15 Feb 2012 3:50) / 1 week
        assertEquals(1.30853174603174603, factor, 0);
    }

    @Test
    public void computeFactor_ExtendsOnlyPeriodEndWeek() {
        // given
        long startTimeUsage = DateTimeHandling
                .calculateMillis("2012-02-07 02:20:00");
        long endTimeUsage = DateTimeHandling
                .calculateMillis("2012-02-15 03:50:00");
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-02-01 00:00:00", "2012-03-01 00:00:00");

        // when
        double factor = calculator.computeFactor(PricingPeriod.WEEK,
                billingInput, startTimeUsage, endTimeUsage, false, true);

        // then (07 Feb 2012 2:20 ~ 13 Feb 2012 0:00) / 1 week + 1 week
        assertEquals(1.8432539682539684, factor, 0);
    }

    @Test
    public void computeFactor_NotExtendsPeriodMonth() {
        // given
        long startTimeUsage = DateTimeHandling
                .calculateMillis("2012-01-07 02:20:00");
        long endTimeUsage = DateTimeHandling
                .calculateMillis("2012-02-15 03:50:00");
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-01-01 00:00:00", "2012-03-01 00:00:00");

        // when
        double factor = calculator.computeFactor(PricingPeriod.MONTH,
                billingInput, startTimeUsage, endTimeUsage, false, false);

        // then (07.01.2012 2:20 ~ 01.02.2012 0:00) / Jan 2012 + (01.02.2012
        // 0:00 ~ 15.02.2012 3:50) / Feb 2012
        assertEquals(1.291581695711284, factor, 0);
    }

    @Test
    public void computeFactor_ExtendsOnlyPeriodStartMonth() {
        // given
        long startTimeUsage = DateTimeHandling
                .calculateMillis("2012-01-07 02:20:00");
        long endTimeUsage = DateTimeHandling
                .calculateMillis("2012-02-15 03:50:00");
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-01-01 00:00:00", "2012-03-01 00:00:00");

        // when
        double factor = calculator.computeFactor(PricingPeriod.MONTH,
                billingInput, startTimeUsage, endTimeUsage, true, false);

        // then 1 month + (01.02.2012 0:00 ~ 15.02.2012 3:50) / Feb 2012
        assertEquals(1.48826628352490421, factor, 0);
    }

    @Test
    public void computeFactor_NotExtendsPeriodAndStartEndInOneTimeUnit() {
        // given
        long startTimeUsage = DateTimeHandling
                .calculateMillis("2012-01-07 02:20:00");
        long endTimeUsage = DateTimeHandling
                .calculateMillis("2012-01-08 02:20:00");
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-01-01 00:00:00", "2012-02-01 00:00:00");

        // when
        double factor = calculator.computeFactor(PricingPeriod.MONTH,
                billingInput, startTimeUsage, endTimeUsage, false, false);

        // then (07.01.2012 2:20 ~ 08.01.2012 2:20) / Jan 2012
        assertEquals(0.03225806451612903, factor, 0);
    }

    @Test
    public void computeFactor_ExtendsOnlyPeriodEndMonth() {
        // given
        long startTimeUsage = DateTimeHandling
                .calculateMillis("2012-01-07 02:20:00");
        long endTimeUsage = DateTimeHandling
                .calculateMillis("2012-02-15 03:50:00");
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-01-01 00:00:00", "2012-03-01 00:00:00");

        // when
        double factor = calculator.computeFactor(PricingPeriod.MONTH,
                billingInput, startTimeUsage, endTimeUsage, false, true);

        // then (07.01.2012 2:20 ~ 01.02.2012 0:00) / Jan 2012 + 1 month
        assertEquals(1.80331541218637993, factor, 0);
    }

    @Test
    public void computeFactor_ExtendsOnlyPeriodStartInOneTimeUnit() {
        // given
        long startTimeUsage = DateTimeHandling
                .calculateMillis("2012-01-07 00:00:00");
        long endTimeUsage = DateTimeHandling
                .calculateMillis("2012-01-15 00:00:00");
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-01-01 00:00:00", "2012-02-01 00:00:00");

        // when
        double factor = calculator.computeFactor(PricingPeriod.MONTH,
                billingInput, startTimeUsage, endTimeUsage, true, false);

        // then (01.01.2012 0:00 ~ 01.15.2012 0:00) / Jan 2012
        assertEquals(0.45161290, factor, 0.0000001);
    }

    @Test
    public void computeFactor_ExtendsOnlyPeriodEndInOneTimeUnit() {
        // given
        long startTimeUsage = DateTimeHandling
                .calculateMillis("2012-01-07 00:00:00");
        long endTimeUsage = DateTimeHandling
                .calculateMillis("2012-01-15 00:00:00");
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-01-01 00:00:00", "2012-02-01 00:00:00");

        // when
        double factor = calculator.computeFactor(PricingPeriod.MONTH,
                billingInput, startTimeUsage, endTimeUsage, false, true);

        // then (07.01.2012 0:00 ~ 01.02.2012 0:00) / Jan 2012
        assertEquals(0.80645161, factor, 0.0000001);
    }

    @Test
    public void computeFactor_Days() {
        // given
        long startTimeUsage = DateTimeHandling
                .calculateMillis("2012-11-16 03:59:59");
        long endTimeUsage = DateTimeHandling
                .calculateMillis("2012-12-31 18:30:00");
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-12-01 00:00:00", "2013-01-01 00:00:00");

        // when
        double factor = calculator.computeFactor(PricingPeriod.DAY,
                billingInput, startTimeUsage, endTimeUsage, true, true);

        // then
        assertEquals(31, factor, 0);
    }

    @Test
    public void computeFactorForUsageTime() {
        // given
        long startTimeUsage = DateTimeHandling
                .calculateMillis("2012-01-10 00:00:00");
        long endTimeUsage = DateTimeHandling
                .calculateMillis("2012-01-20 00:00:00");
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2012-01-01 00:00:00", "2012-02-01 00:00:00");

        // when
        double factor = calculator
                .computeFactorForUsageTime(PricingPeriod.MONTH, billingInput,
                        startTimeUsage, endTimeUsage);

        // then
        assertEquals(1, factor, 0);
    }

}
