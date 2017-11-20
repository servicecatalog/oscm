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

import org.oscm.billingservice.business.calculation.revenue.model.TimeSlice;
import org.oscm.test.DateTimeHandling;
import org.oscm.internal.types.enumtypes.PricingPeriod;
import org.oscm.internal.types.exception.IllegalArgumentException;

/**
 * @author baumann
 */
public class CostCalculatorPerUnitComputeTimeSliceFactorTest {

    // Java double has a precision of 15 significant digits.
    // The time slice factors are all lower or equal 1D.
    private final static double ASSERT_DOUBLE_DELTA = 0.000000000000009D;

    private CostCalculatorPerUnit calculator;

    @Before
    public void setup() {
        calculator = new CostCalculatorPerUnit();
    }

    @Test(expected = NullPointerException.class)
    public void computeFactorForTimeSlice_NullPricingPeriod() {
        // given
        long usagePeriodStart = DateTimeHandling
                .calculateMillis("2013-09-02 06:00:00");
        long usagePeriodEnd = DateTimeHandling
                .calculateMillis("2013-09-03 12:00:00");

        // when
        calculator.computeFactorForTimeSlice(null, usagePeriodStart,
                usagePeriodEnd, true, true);

        // then a NullPointerException is expected.
    }

    @Test(expected = IllegalArgumentException.class)
    public void computeFactorForTimeSlice_usageStartAfterEnd() {
        // given
        TimeSlice timeSlice = newTimeSlice("2013-09-02 00:00:00",
                PricingPeriod.WEEK);
        long usagePeriodStart = DateTimeHandling
                .calculateMillis("2013-09-04 06:00:00");
        long usagePeriodEnd = DateTimeHandling
                .calculateMillis("2013-09-03 12:00:00");

        // when
        calculator.computeFactorForTimeSlice(timeSlice, usagePeriodStart,
                usagePeriodEnd, true, true);

        // then an IllegalArgumentException is expected.
    }

    @Test
    public void computeFactorForTimeSlice_hour_adjustStartEnd() {
        // given
        TimeSlice timeSlice = newTimeSlice("2013-05-29 08:00:00",
                PricingPeriod.HOUR);
        long usagePeriodStart = DateTimeHandling
                .calculateMillis("2013-05-29 08:10:00");
        long usagePeriodEnd = DateTimeHandling
                .calculateMillis("2013-05-29 08:40:00");

        // when
        double factor = calculator.computeFactorForTimeSlice(timeSlice,
                usagePeriodStart, usagePeriodEnd, true, true);

        // then
        assertEquals(1D, factor, 0);
    }

    @Test
    public void computeFactorForTimeSlice_hour_adjustStart() {
        // given
        TimeSlice timeSlice = newTimeSlice("2013-05-29 08:00:00",
                PricingPeriod.HOUR);
        long usagePeriodStart = DateTimeHandling
                .calculateMillis("2013-05-29 08:10:00");
        long usagePeriodEnd = DateTimeHandling
                .calculateMillis("2013-05-29 08:40:00");

        // when
        double factor = calculator.computeFactorForTimeSlice(timeSlice,
                usagePeriodStart, usagePeriodEnd, true, false);

        // then
        assertEquals(0.6666666666666667D, factor, ASSERT_DOUBLE_DELTA);
    }

    @Test
    public void computeFactorForTimeSlice_hour_adjustEnd() {
        // given
        TimeSlice timeSlice = newTimeSlice("2013-05-29 08:00:00",
                PricingPeriod.HOUR);
        long usagePeriodStart = DateTimeHandling
                .calculateMillis("2013-05-29 08:10:00");
        long usagePeriodEnd = DateTimeHandling
                .calculateMillis("2013-05-29 08:40:00");

        // when
        double factor = calculator.computeFactorForTimeSlice(timeSlice,
                usagePeriodStart, usagePeriodEnd, false, true);

        // then
        assertEquals(0.8333333333333333D, factor, ASSERT_DOUBLE_DELTA);
    }

    @Test
    public void computeFactorForTimeSlice_hour_noAdjust() {
        // given
        TimeSlice timeSlice = newTimeSlice("2013-05-29 08:00:00",
                PricingPeriod.HOUR);
        long usagePeriodStart = DateTimeHandling
                .calculateMillis("2013-05-29 08:10:00");
        long usagePeriodEnd = DateTimeHandling
                .calculateMillis("2013-05-29 08:40:00");

        // when
        double factor = calculator.computeFactorForTimeSlice(timeSlice,
                usagePeriodStart, usagePeriodEnd, false, false);

        // then
        assertEquals(0.5D, factor, ASSERT_DOUBLE_DELTA);
    }

    @Test
    public void computeFactorForTimeSlice_day_adjustStart() {
        // given
        TimeSlice timeSlice = newTimeSlice("2013-05-29 00:00:00",
                PricingPeriod.DAY);
        long usagePeriodStart = DateTimeHandling
                .calculateMillis("2013-05-29 06:00:00");
        long usagePeriodEnd = DateTimeHandling
                .calculateMillis("2013-05-29 12:00:00");

        // when
        double factor = calculator.computeFactorForTimeSlice(timeSlice,
                usagePeriodStart, usagePeriodEnd, true, false);

        // then
        assertEquals(0.5D, factor, ASSERT_DOUBLE_DELTA);
    }

    @Test
    public void computeFactorForTimeSlice_day_adjustEnd() {
        // given
        TimeSlice timeSlice = newTimeSlice("2013-05-29 00:00:00",
                PricingPeriod.DAY);
        long usagePeriodStart = DateTimeHandling
                .calculateMillis("2013-05-29 06:00:00");
        long usagePeriodEnd = DateTimeHandling
                .calculateMillis("2013-05-29 12:00:00");

        // when
        double factor = calculator.computeFactorForTimeSlice(timeSlice,
                usagePeriodStart, usagePeriodEnd, false, true);

        // then
        assertEquals(0.75D, factor, ASSERT_DOUBLE_DELTA);
    }

    @Test
    public void computeFactorForTimeSlice_day_summertime_adjustStartEnd() {
        // given
        TimeSlice timeSlice = newTimeSlice("2014-03-30 00:00:00",
                PricingPeriod.DAY);
        long usagePeriodStart = DateTimeHandling
                .calculateMillis("2014-03-30 01:00:00");
        long usagePeriodEnd = DateTimeHandling
                .calculateMillis("2014-03-30 11:00:00");

        // when
        double factor = calculator.computeFactorForTimeSlice(timeSlice,
                usagePeriodStart, usagePeriodEnd, true, true);

        // then
        assertEquals(1D, factor, 0);
    }

    @Test
    public void computeFactorForTimeSlice_day_summertime_noAdjust() {
        // given
        TimeSlice timeSlice = newTimeSlice("2014-03-30 00:00:00",
                PricingPeriod.DAY);
        long usagePeriodStart = DateTimeHandling
                .calculateMillis("2014-03-30 01:00:00");
        long usagePeriodEnd = DateTimeHandling
                .calculateMillis("2014-03-30 11:00:00");

        // when
        double factor = calculator.computeFactorForTimeSlice(timeSlice,
                usagePeriodStart, usagePeriodEnd, false, false);

        if (DateTimeHandling.isDayLightSaving(usagePeriodStart, usagePeriodEnd)) {
            // then
            assertEquals(0.391304347826087D, factor, ASSERT_DOUBLE_DELTA);
        } else {
            // then
            assertEquals(0.4166666666666667D, factor, ASSERT_DOUBLE_DELTA);
        }
    }

    @Test
    public void computeFactorForTimeSlice_day_wintertime_adjustStartEnd() {
        // given
        TimeSlice timeSlice = newTimeSlice("2014-10-26 00:00:00",
                PricingPeriod.DAY);
        long usagePeriodStart = DateTimeHandling
                .calculateMillis("2014-10-26 01:00:00");
        long usagePeriodEnd = DateTimeHandling
                .calculateMillis("2014-10-26 12:00:00");

        // when
        double factor = calculator.computeFactorForTimeSlice(timeSlice,
                usagePeriodStart, usagePeriodEnd, true, true);

        // then
        assertEquals(1D, factor, 0);
    }

    @Test
    public void computeFactorForTimeSlice_day_wintertime_noAdjust() {
        // given
        TimeSlice timeSlice = newTimeSlice("2014-10-26 00:00:00",
                PricingPeriod.DAY);
        long usagePeriodStart = DateTimeHandling
                .calculateMillis("2014-10-26 01:00:00");
        long usagePeriodEnd = DateTimeHandling
                .calculateMillis("2014-10-26 12:00:00");

        // when
        double factor = calculator.computeFactorForTimeSlice(timeSlice,
                usagePeriodStart, usagePeriodEnd, false, false);

        if (DateTimeHandling.isDayLightSaving(usagePeriodStart, usagePeriodEnd)) {
            // then
            assertEquals(0.48D, factor, ASSERT_DOUBLE_DELTA);
        } else {
            // then
            assertEquals(0.4583333333333333D, factor, ASSERT_DOUBLE_DELTA);
        }

    }

    @Test
    public void computeFactorForTimeSlice_week_adjustStartEnd() {
        // given
        TimeSlice timeSlice = newTimeSlice("2013-09-02 00:00:00",
                PricingPeriod.WEEK);
        long usagePeriodStart = DateTimeHandling
                .calculateMillis("2013-09-02 06:00:00");
        long usagePeriodEnd = DateTimeHandling
                .calculateMillis("2013-09-03 12:00:00");

        // when
        double factor = calculator.computeFactorForTimeSlice(timeSlice,
                usagePeriodStart, usagePeriodEnd, true, true);

        // then
        assertEquals(1D, factor, 0);
    }

    @Test
    public void computeFactorForTimeSlice_week_adjustStart() {
        // given
        TimeSlice timeSlice = newTimeSlice("2013-09-02 00:00:00",
                PricingPeriod.WEEK);
        long usagePeriodStart = DateTimeHandling
                .calculateMillis("2013-09-02 06:00:00");
        long usagePeriodEnd = DateTimeHandling
                .calculateMillis("2013-09-03 12:00:00");

        // when
        double factor = calculator.computeFactorForTimeSlice(timeSlice,
                usagePeriodStart, usagePeriodEnd, true, false);

        // then
        assertEquals(0.2142857142857143D, factor, ASSERT_DOUBLE_DELTA);
    }

    @Test
    public void computeFactorForTimeSlice_week_adjustEnd() {
        // given
        TimeSlice timeSlice = newTimeSlice("2013-09-02 00:00:00",
                PricingPeriod.WEEK);
        long usagePeriodStart = DateTimeHandling
                .calculateMillis("2013-09-02 06:00:00");
        long usagePeriodEnd = DateTimeHandling
                .calculateMillis("2013-09-03 12:00:00");

        // when
        double factor = calculator.computeFactorForTimeSlice(timeSlice,
                usagePeriodStart, usagePeriodEnd, false, true);

        // then
        assertEquals(0.9642857142857143D, factor, ASSERT_DOUBLE_DELTA);
    }

    @Test
    public void computeFactorForTimeSlice_week_noAdjust() {
        // given
        TimeSlice timeSlice = newTimeSlice("2013-09-02 00:00:00",
                PricingPeriod.WEEK);
        long usagePeriodStart = DateTimeHandling
                .calculateMillis("2013-09-02 06:00:00");
        long usagePeriodEnd = DateTimeHandling
                .calculateMillis("2013-09-03 12:00:00");

        // when
        double factor = calculator.computeFactorForTimeSlice(timeSlice,
                usagePeriodStart, usagePeriodEnd, false, false);

        // then
        assertEquals(0.1785714285714286D, factor, ASSERT_DOUBLE_DELTA);
    }

    @Test
    public void computeFactorForTimeSlice_week_summertime_adjustStartEnd() {
        // given
        TimeSlice timeSlice = newTimeSlice("2014-03-24 00:00:00",
                PricingPeriod.WEEK);
        long usagePeriodStart = DateTimeHandling
                .calculateMillis("2014-03-26 00:00:00");
        long usagePeriodEnd = DateTimeHandling
                .calculateMillis("2014-03-31 12:00:00");

        // when
        double factor = calculator.computeFactorForTimeSlice(timeSlice,
                usagePeriodStart, usagePeriodEnd, true, true);

        // then
        assertEquals(1D, factor, 0);
    }

    @Test
    public void computeFactorForTimeSlice_week_summertime_noAdjust() {
        // given
        TimeSlice timeSlice = newTimeSlice("2014-03-24 00:00:00",
                PricingPeriod.WEEK);
        long usagePeriodStart = DateTimeHandling
                .calculateMillis("2014-03-26 00:00:00");
        long usagePeriodEnd = DateTimeHandling
                .calculateMillis("2014-03-30 12:00:00");

        // when
        double factor = calculator.computeFactorForTimeSlice(timeSlice,
                usagePeriodStart, usagePeriodEnd, false, false);

        if (DateTimeHandling.isDayLightSaving(usagePeriodStart, usagePeriodEnd)) {
            // then
            assertEquals(0.6407185628742515D, factor, ASSERT_DOUBLE_DELTA);
        } else {
            // then
            assertEquals(0.6428571428571429D, factor, ASSERT_DOUBLE_DELTA);
        }

    }

    @Test
    public void computeFactorForTimeSlice_week_wintertime_adjustStartEnd() {
        // given
        TimeSlice timeSlice = newTimeSlice("2014-10-20 00:00:00",
                PricingPeriod.WEEK);
        long usagePeriodStart = DateTimeHandling
                .calculateMillis("2014-10-22 12:00:00");
        long usagePeriodEnd = DateTimeHandling
                .calculateMillis("2014-10-26 12:00:00");

        // when
        double factor = calculator.computeFactorForTimeSlice(timeSlice,
                usagePeriodStart, usagePeriodEnd, true, true);

        // then
        assertEquals(1D, factor, 0);
    }

    @Test
    public void computeFactorForTimeSlice_week_wintertime_noAdjust() {
        // given
        TimeSlice timeSlice = newTimeSlice("2014-10-20 00:00:00",
                PricingPeriod.WEEK);
        long usagePeriodStart = DateTimeHandling
                .calculateMillis("2014-10-22 12:00:00");
        long usagePeriodEnd = DateTimeHandling
                .calculateMillis("2014-10-26 12:00:00");

        // when
        double factor = calculator.computeFactorForTimeSlice(timeSlice,
                usagePeriodStart, usagePeriodEnd, false, false);

        if (DateTimeHandling.isDayLightSaving(usagePeriodStart, usagePeriodEnd)) {
            // then
            assertEquals(0.5739644970414201D, factor, ASSERT_DOUBLE_DELTA);
        } else {
            // then
            assertEquals(0.5714285714285714D, factor, ASSERT_DOUBLE_DELTA);
        }

    }

    @Test
    public void computeFactorForTimeSlice_month_summertime_adjustStartEnd() {
        // given
        TimeSlice timeSlice = newTimeSlice("2014-03-01 00:00:00",
                PricingPeriod.MONTH);
        long usagePeriodStart = DateTimeHandling
                .calculateMillis("2014-03-10 00:00:00");
        long usagePeriodEnd = DateTimeHandling
                .calculateMillis("2014-03-20 12:00:00");

        // when
        double factor = calculator.computeFactorForTimeSlice(timeSlice,
                usagePeriodStart, usagePeriodEnd, true, true);

        // then
        assertEquals(1D, factor, 0);
    }

    @Test
    public void computeFactorForTimeSlice_month_summertime_noAdjust() {
        // given
        TimeSlice timeSlice = newTimeSlice("2014-03-01 00:00:00",
                PricingPeriod.MONTH);
        long usagePeriodStart = DateTimeHandling
                .calculateMillis("2014-03-10 00:00:00");
        long usagePeriodEnd = DateTimeHandling
                .calculateMillis("2014-03-31 12:00:00");

        // when
        double factor = calculator.computeFactorForTimeSlice(timeSlice,
                usagePeriodStart, usagePeriodEnd, false, false);

        if (DateTimeHandling.isDayLightSaving(usagePeriodStart, usagePeriodEnd)) {
            // then
            assertEquals(0.693135935397039D, factor, ASSERT_DOUBLE_DELTA);
        } else {
            // then
            assertEquals(0.6935483870967742D, factor, ASSERT_DOUBLE_DELTA);
        }
    }

    @Test
    public void computeFactorForTimeSlice_month_adjustStart() {
        // given
        TimeSlice timeSlice = newTimeSlice("2014-05-01 00:00:00",
                PricingPeriod.MONTH);
        long usagePeriodStart = DateTimeHandling
                .calculateMillis("2014-05-10 00:00:00");
        long usagePeriodEnd = DateTimeHandling
                .calculateMillis("2014-05-20 12:00:00");

        // when
        double factor = calculator.computeFactorForTimeSlice(timeSlice,
                usagePeriodStart, usagePeriodEnd, true, false);

        // then
        assertEquals(0.6290322580645161D, factor, ASSERT_DOUBLE_DELTA);
    }

    @Test
    public void computeFactorForTimeSlice_month_adjustEnd() {
        // given
        TimeSlice timeSlice = newTimeSlice("2014-05-01 00:00:00",
                PricingPeriod.MONTH);
        long usagePeriodStart = DateTimeHandling
                .calculateMillis("2014-05-10 00:00:00");
        long usagePeriodEnd = DateTimeHandling
                .calculateMillis("2014-05-20 12:00:00");

        // when
        double factor = calculator.computeFactorForTimeSlice(timeSlice,
                usagePeriodStart, usagePeriodEnd, false, true);

        // then
        assertEquals(0.7096774193548387D, factor, ASSERT_DOUBLE_DELTA);
    }

    @Test
    public void computeFactorForTimeSlice_month_wintertime_adjustStartEnd() {
        // given
        TimeSlice timeSlice = newTimeSlice("2014-10-01 00:00:00",
                PricingPeriod.MONTH);
        long usagePeriodStart = DateTimeHandling
                .calculateMillis("2014-10-10 00:00:00");
        long usagePeriodEnd = DateTimeHandling
                .calculateMillis("2014-10-28 12:00:00");

        // when
        double factor = calculator.computeFactorForTimeSlice(timeSlice,
                usagePeriodStart, usagePeriodEnd, true, true);

        // then
        assertEquals(1D, factor, 0);
    }

    @Test
    public void computeFactorForTimeSlice_month_wintertime_noAdjust() {
        // given
        TimeSlice timeSlice = newTimeSlice("2014-10-01 00:00:00",
                PricingPeriod.MONTH);
        long usagePeriodStart = DateTimeHandling
                .calculateMillis("2014-10-10 00:00:00");
        long usagePeriodEnd = DateTimeHandling
                .calculateMillis("2014-10-28 12:00:00");

        // when
        double factor = calculator.computeFactorForTimeSlice(timeSlice,
                usagePeriodStart, usagePeriodEnd, false, false);

        if (DateTimeHandling.isDayLightSaving(usagePeriodStart, usagePeriodEnd)) {
            // then
            assertEquals(0.5973154362416107D, factor, ASSERT_DOUBLE_DELTA);
        } else {
            // then
            assertEquals(0.5967741935483871D, factor, ASSERT_DOUBLE_DELTA);
        }
    }

    private TimeSlice newTimeSlice(String date, PricingPeriod period) {
        long startTime = PricingPeriodDateConverter.getStartTime(
                DateTimeHandling.calculateMillis(date), period)
                .getTimeInMillis();
        long endTime = PricingPeriodDateConverter.getStartTimeOfNextPeriod(
                startTime, period).getTimeInMillis() - 1;
        return new TimeSlice(startTime, endTime, period);
    }

}
