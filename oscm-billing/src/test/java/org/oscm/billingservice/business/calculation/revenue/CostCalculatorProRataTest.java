/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 18.12.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.calculation.revenue;

import static org.junit.Assert.assertEquals;

import java.util.TimeZone;

import org.junit.Test;

import org.oscm.test.DateTimeHandling;
import org.oscm.internal.types.enumtypes.PricingPeriod;

/**
 * @author barzu
 */
public class CostCalculatorProRataTest {

    // Java double has a precision of 15 significant digits.
    private final static double ASSERT_DOUBLE_DELTA = 0.000000000000009D;

    @Test
    public void computeFactorForUsageTime_UsageInNextMonth() {
        // when
        double factor = new CostCalculatorProRata().computeFactorForUsageTime(
                PricingPeriod.MONTH, BillingInputFactory.newBillingInput(
                        "2012-01-26 00:00:00", "2012-02-26 00:00:00"),
                DateTimeHandling.calculateMillis("2012-02-24 00:00:00"),
                DateTimeHandling.calculateMillis("2012-02-25 00:00:00"));

        // then
        assertEquals(0.0344827586206897, factor, ASSERT_DOUBLE_DELTA);
    }

    @Test
    public void computeFactorForUsageTime_UsageInCurrentMonth() {
        // given
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+1"));

        // when
        double factor = new CostCalculatorProRata().computeFactorForUsageTime(
                PricingPeriod.MONTH, BillingInputFactory.newBillingInput(
                        "2012-01-26 00:00:00", "2012-02-26 00:00:00"),
                DateTimeHandling.calculateMillis("2012-01-27 00:00:00"),
                DateTimeHandling.calculateMillis("2012-01-28 00:00:00"));

        // then
        assertEquals(0.03225806451612903, factor, ASSERT_DOUBLE_DELTA);
    }

    @Test
    public void computeFactorForUsageTime_UsageInCurrentMonth2() {
        // when 29 days for February 2012
        double factor = new CostCalculatorProRata().computeFactorForUsageTime(
                PricingPeriod.MONTH, BillingInputFactory.newBillingInput(
                        "2012-02-01 00:00:00", "2012-03-01 00:00:00"),
                DateTimeHandling.calculateMillis("2012-02-07 00:00:00"),
                DateTimeHandling.calculateMillis("2012-02-08 00:00:00"));

        // then
        assertEquals(0.034482758620689655, factor, ASSERT_DOUBLE_DELTA);
    }

    @Test
    public void computeFactorForUsageTime_UsageInCurrentMonth3() {
        // when 28 days for February 2013
        double factor = new CostCalculatorProRata().computeFactorForUsageTime(
                PricingPeriod.MONTH, BillingInputFactory.newBillingInput(
                        "2013-02-01 00:00:00", "2013-03-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-02-07 00:00:00"),
                DateTimeHandling.calculateMillis("2013-02-08 00:00:00"));

        // then
        assertEquals(0.03571428571428571, factor, ASSERT_DOUBLE_DELTA);
    }

    @Test
    public void computeFactorForUsageTime_UsageExceedBillingPeriod() {
        double factor = new CostCalculatorProRata().computeFactorForUsageTime(
                PricingPeriod.MONTH, BillingInputFactory.newBillingInput(
                        "2013-02-01 00:00:00", "2013-03-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-01-30 00:00:00"),
                DateTimeHandling.calculateMillis("2013-03-02 00:00:00"));

        // then
        assertEquals(1, factor, 0);
    }

    public void computeEndTimeForPaymentPreview() {
        // given
        long endTimeForPeriod = DateTimeHandling
                .calculateMillis("2012-01-15 11:23:00");

        // when
        long endTime = new CostCalculatorProRata()
                .computeEndTimeForPaymentPreview(endTimeForPeriod, 0,
                        PricingPeriod.MONTH);

        // then
        assertEquals(endTimeForPeriod, endTime);
    }
}
