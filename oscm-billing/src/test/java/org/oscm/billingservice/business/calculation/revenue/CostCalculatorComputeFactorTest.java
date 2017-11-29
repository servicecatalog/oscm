/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 11.11.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.calculation.revenue;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import org.oscm.test.DateTimeHandling;
import org.oscm.internal.types.enumtypes.PricingPeriod;

/**
 * @author baumann
 */
public class CostCalculatorComputeFactorTest {

    private CostCalculator calculator = new CostCalculatorProRata();

    // Java double has a precision of 15 significant digits.
    private final static double ASSERT_DOUBLE_DELTA = 0.000000000000009D;

    @Test
    public void computeFractionalFactor_month() {
        // when
        double factor = calculator.computeFractionalFactor(
                DateTimeHandling.calculateMillis("2013-01-17 00:00:00"),
                DateTimeHandling.calculateMillis("2013-02-15 00:00:00"),
                PricingPeriod.MONTH);

        // then
        assertEquals(0.9838709677419355D, factor, ASSERT_DOUBLE_DELTA);
    }

    @Test
    public void computeFractionalFactor_month_summertime() {
        // when
        double factor = calculator.computeFractionalFactor(
                DateTimeHandling.calculateMillis("2013-03-16 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-16 00:00:00"),
                PricingPeriod.MONTH);

        if (DateTimeHandling.isDayLightSaving(
                DateTimeHandling.calculateMillis("2013-03-16 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-16 00:00:00"))) {
            // then
            assertEquals(1.015477792732167D, factor, ASSERT_DOUBLE_DELTA);
        } else {
            // then
            assertEquals(1.0161290322580645D, factor, ASSERT_DOUBLE_DELTA);
        }
    }

    @Test
    public void computeFractionalFactor_month_wintertime() {
        // when
        double factor = calculator.computeFractionalFactor(
                DateTimeHandling.calculateMillis("2013-10-16 00:00:00"),
                DateTimeHandling.calculateMillis("2013-11-16 00:00:00"),
                PricingPeriod.MONTH);

        if (DateTimeHandling.isDayLightSaving(
                DateTimeHandling.calculateMillis("2013-10-16 00:00:00"),
                DateTimeHandling.calculateMillis("2013-11-16 00:00:00"))) {
            // then
            assertEquals(1.016778523489933D, factor, ASSERT_DOUBLE_DELTA);
        } else {
            // then
            assertEquals(1.0161290322580645D, factor, ASSERT_DOUBLE_DELTA);
        }
    }

    @Test
    public void computeFractionalFactor_week() {
        // when
        double factor = calculator.computeFractionalFactor(
                DateTimeHandling.calculateMillis("2013-01-03 00:00:00"),
                DateTimeHandling.calculateMillis("2013-01-28 00:00:00"),
                PricingPeriod.WEEK);

        // then
        assertEquals(3.571428571428571D, factor, ASSERT_DOUBLE_DELTA);
    }

    @Test
    public void computeFractionalFactor_week_summertime() {
        // when
        double factor = calculator.computeFractionalFactor(
                DateTimeHandling.calculateMillis("2013-03-26 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-04 12:00:00"),
                PricingPeriod.WEEK);

        if (DateTimeHandling.isDayLightSaving(
                DateTimeHandling.calculateMillis("2013-03-26 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-04 12:00:00"))) {
            // then
            assertEquals(1.356287425149701D, factor, ASSERT_DOUBLE_DELTA);
        } else {
            // then
            assertEquals(1.3571428571428572D, factor, ASSERT_DOUBLE_DELTA);
        }
    }

    @Test
    public void computeFractionalFactor_week_wintertime() {
        // when
        double factor = calculator.computeFractionalFactor(
                DateTimeHandling.calculateMillis("2013-10-26 00:00:00"),
                DateTimeHandling.calculateMillis("2013-11-14 12:00:00"),
                PricingPeriod.WEEK);

        if (DateTimeHandling.isDayLightSaving(
                DateTimeHandling.calculateMillis("2013-10-26 00:00:00"),
                DateTimeHandling.calculateMillis("2013-11-14 12:00:00"))) {
            // then
            assertEquals(2.789940828402367D, factor, ASSERT_DOUBLE_DELTA);
        } else {
            // then
            assertEquals(2.7857142857142856D, factor, ASSERT_DOUBLE_DELTA);
        }
    }

    @Test
    public void computeFractionalFactor_day() {
        // when
        double factor = calculator.computeFractionalFactor(
                DateTimeHandling.calculateMillis("2013-02-07 00:00:00"),
                DateTimeHandling.calculateMillis("2013-02-21 00:00:00"),
                PricingPeriod.DAY);

        // then
        assertEquals(14.0D, factor, ASSERT_DOUBLE_DELTA);
    }

    @Test
    public void computeFractionalFactor_day_summertime() {
        // when
        double factor = calculator.computeFractionalFactor(
                DateTimeHandling.calculateMillis("2013-03-25 12:00:00"),
                DateTimeHandling.calculateMillis("2013-04-02 00:00:00"),
                PricingPeriod.DAY);

        // then
        assertEquals(7.5D, factor, ASSERT_DOUBLE_DELTA);
    }

    @Test
    public void computeFractionalFactor_day_wintertime() {
        // when
        double factor = calculator.computeFractionalFactor(
                DateTimeHandling.calculateMillis("2013-10-25 12:00:00"),
                DateTimeHandling.calculateMillis("2013-10-27 10:00:00"),
                PricingPeriod.DAY);
        if (DateTimeHandling.isDayLightSaving(
                DateTimeHandling.calculateMillis("2013-10-25 12:00:00"),
                DateTimeHandling.calculateMillis("2013-10-27 10:00:00"))) {
            // then
            assertEquals(1.94D, factor, ASSERT_DOUBLE_DELTA);
        } else {
            // then
            assertEquals(1.9166666666666667D, factor, ASSERT_DOUBLE_DELTA);
        }
    }

    @Test
    public void computeFractionalFactor_hour() {
        // when
        double factor = calculator.computeFractionalFactor(
                DateTimeHandling.calculateMillis("2013-02-07 10:30:00"),
                DateTimeHandling.calculateMillis("2013-02-08 03:00:00"),
                PricingPeriod.HOUR);

        // then
        assertEquals(16.5D, factor, ASSERT_DOUBLE_DELTA);
    }

    @Test
    public void computeFractionalFactor_hour_summertime() {
        // when
        double factor = calculator.computeFractionalFactor(
                DateTimeHandling.calculateMillis("2013-03-30 10:30:00"),
                DateTimeHandling.calculateMillis("2013-04-01 22:00:00"),
                PricingPeriod.HOUR);

        if (DateTimeHandling.isDayLightSaving(
                DateTimeHandling.calculateMillis("2013-03-30 10:30:00"),
                DateTimeHandling.calculateMillis("2013-04-01 22:00:00"))) {
            // then day has 23 hours
            assertEquals(58.5D, factor, ASSERT_DOUBLE_DELTA);
        } else {
            // then
            assertEquals(59.5D, factor, ASSERT_DOUBLE_DELTA);
        }
    }

    @Test
    public void computeFractionalFactor_hour_wintertime() {
        // when
        double factor = calculator.computeFractionalFactor(
                DateTimeHandling.calculateMillis("2013-10-27 01:30:00"),
                DateTimeHandling.calculateMillis("2013-10-27 22:00:00"),
                PricingPeriod.HOUR);

        if (DateTimeHandling.isDayLightSaving(
                DateTimeHandling.calculateMillis("2013-10-27 01:30:00"),
                DateTimeHandling.calculateMillis("2013-10-27 22:00:00"))) {
            // then day has 23 hours
            assertEquals(21.5D, factor, ASSERT_DOUBLE_DELTA);
        } else {
            // then
            assertEquals(20.5D, factor, ASSERT_DOUBLE_DELTA);
        }
    }

}
