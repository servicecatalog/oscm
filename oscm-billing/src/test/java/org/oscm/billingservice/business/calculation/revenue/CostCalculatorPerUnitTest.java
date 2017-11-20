/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Dec 10, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.calculation.revenue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.oscm.billingservice.business.calculation.revenue.CostCalculatorPerUnit;
import org.oscm.billingservice.business.calculation.revenue.model.UserAssignment;
import org.oscm.billingservice.business.calculation.revenue.model.UserRoleAssignment;
import org.oscm.test.DateTimeHandling;
import org.oscm.internal.types.enumtypes.PricingPeriod;

/**
 * @author tokoda
 * 
 */
public class CostCalculatorPerUnitTest {

    private CostCalculatorPerUnit calculator;

    @Before
    public void setup() {
        calculator = new CostCalculatorPerUnit();
    }

    @Test
    public void computeEndTimeForPaymentPreview_InvokeInLastTimeSliceEndingWithBillingEnd() {
        // given
        long endTimeForPeriod = DateTimeHandling
                .calculateMillis("2012-01-31 23:59:59");
        long endTimeForBilling = DateTimeHandling
                .calculateMillis("2012-02-01 00:00:00");

        // when
        long endTime = new CostCalculatorPerUnit()
                .computeEndTimeForPaymentPreview(endTimeForPeriod,
                        endTimeForBilling, PricingPeriod.MONTH);

        // then
        assertEquals(DateTimeHandling.calculateMillis("2012-02-01 00:00:00"),
                endTime);
    }

    @Test
    public void computeEndTimeForPaymentPreview_InvokeInLastTimeSliceOverlappingBillingEnd() {
        // given
        long endTimeForPeriod = DateTimeHandling
                .calculateMillis("2012-02-13 00:00:00");
        long endTimeForBilling = DateTimeHandling
                .calculateMillis("2012-03-01 00:00:00");

        // when
        long endTime = new CostCalculatorPerUnit()
                .computeEndTimeForPaymentPreview(endTimeForPeriod,
                        endTimeForBilling, PricingPeriod.WEEK);

        // then
        assertEquals(DateTimeHandling.calculateMillis("2012-02-13 00:00:00"),
                endTime);
    }

    @Test
    public void computeEndTimeForPaymentPreview_InvokeBeforeLastTimeSliceOverlappingBillingEnd() {
        // given
        long endTimeForPeriod = DateTimeHandling
                .calculateMillis("2012-02-12 23:59:59");
        long endTimeForBilling = DateTimeHandling
                .calculateMillis("2012-03-01 00:00:00");

        // when
        long endTime = new CostCalculatorPerUnit()
                .computeEndTimeForPaymentPreview(endTimeForPeriod,
                        endTimeForBilling, PricingPeriod.WEEK);

        // then
        assertEquals(DateTimeHandling.calculateMillis("2012-02-13 00:00:00"),
                endTime);
    }

    @Test
    public void computeEndTimeForPaymentPreview_InvokeBeforeLastTimeSliceOverlappingBillingEnd2() {
        // given
        long endTimeForPeriod = DateTimeHandling
                .calculateMillis("2013-01-30 01:59:00");
        long endTimeForBilling = DateTimeHandling
                .calculateMillis("2013-02-01 00:00:00");

        // when
        long endTime = new CostCalculatorPerUnit()
                .computeEndTimeForPaymentPreview(endTimeForPeriod,
                        endTimeForBilling, PricingPeriod.WEEK);

        // then
        assertEquals(DateTimeHandling.calculateMillis("2013-02-01 00:00:00"),
                endTime);
    }

    @Test
    public void computeEndTimeForPaymentPreview_InvokeJustTimeSliceStart() {
        // given
        long endTimeForPeriod = DateTimeHandling
                .calculateMillis("2012-02-13 00:00:00");
        long endTimeForBilling = DateTimeHandling
                .calculateMillis("2012-03-01 00:00:00");

        // when
        long endTime = new CostCalculatorPerUnit()
                .computeEndTimeForPaymentPreview(endTimeForPeriod,
                        endTimeForBilling, PricingPeriod.DAY);

        // then
        assertEquals(DateTimeHandling.calculateMillis("2012-02-13 00:00:00"),
                endTime);
    }

    @Test
    public void computeEndTimeForPaymentPreview_InvokeInFirstTimeSlice() {
        // given
        long endTimeForPeriod = DateTimeHandling
                .calculateMillis("2012-01-15 00:00:00");
        long endTimeForBilling = DateTimeHandling
                .calculateMillis("2012-02-01 00:00:00");

        // when
        long endTime = new CostCalculatorPerUnit()
                .computeEndTimeForPaymentPreview(endTimeForPeriod,
                        endTimeForBilling, PricingPeriod.HOUR);

        // then
        assertEquals(DateTimeHandling.calculateMillis("2012-01-15 00:00:00"),
                endTime);
    }

    @Test
    public void isUserAssignmentInPeriod_BeforePeriodStart() {
        // given
        UserAssignment userAssignment = createUserAssignment(
                DateTimeHandling.calculateMillis("2013-04-04 23:59:58"),
                DateTimeHandling.calculateMillis("2013-04-04 23:59:59"));

        // when
        boolean result = calculator.isUserAssignmentInPeriod(userAssignment,
                DateTimeHandling.calculateMillis("2013-04-05 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-10 00:00:00"));

        // then
        assertFalse(result);
    }

    @Test
    public void isUserAssignmentInPeriod_OnPeriodStartTime() {
        // given
        UserAssignment userAssignment = createUserAssignment(
                DateTimeHandling.calculateMillis("2013-04-04 23:59:59"),
                DateTimeHandling.calculateMillis("2013-04-05 00:00:00"));

        // when
        boolean result = calculator.isUserAssignmentInPeriod(userAssignment,
                DateTimeHandling.calculateMillis("2013-04-05 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-10 00:00:00"));

        // then
        assertTrue(result);
    }

    @Test
    public void isUserAssignmentInPeriod_InPeriod() {
        // given
        UserAssignment userAssignment = createUserAssignment(
                DateTimeHandling.calculateMillis("2013-04-05 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-09 23:59:59"));

        // when
        boolean result = calculator.isUserAssignmentInPeriod(userAssignment,
                DateTimeHandling.calculateMillis("2013-04-05 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-10 00:00:00"));

        // then
        assertTrue(result);
    }

    @Test
    public void isUserAssignmentInPeriod_OnPeriodEndTime() {
        // given
        UserAssignment userAssignment = createUserAssignment(
                DateTimeHandling.calculateMillis("2013-04-10 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-10 00:00:01"));

        // when
        boolean result = calculator.isUserAssignmentInPeriod(userAssignment,
                DateTimeHandling.calculateMillis("2013-04-05 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-10 00:00:00"));

        // then
        assertFalse(result);
    }

    @Test
    public void isUserAssignmentInPeriod_AfterPeriodEnd() {
        // given
        UserAssignment userAssignment = createUserAssignment(
                DateTimeHandling.calculateMillis("2013-04-10 00:00:01"),
                DateTimeHandling.calculateMillis("2013-04-10 00:00:02"));

        // when
        boolean result = calculator.isUserAssignmentInPeriod(userAssignment,
                DateTimeHandling.calculateMillis("2013-04-05 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-10 00:00:00"));

        // then
        assertFalse(result);
    }

    @Test
    public void isUserRoleAssignmentInPeriod_BeforePeriodStart() {
        // given
        UserRoleAssignment userRoleAssignment = new UserRoleAssignment(
                Long.valueOf(10000),
                DateTimeHandling.calculateMillis("2013-04-04 23:59:58"),
                DateTimeHandling.calculateMillis("2013-04-04 23:59:59"));

        // when
        boolean result = calculator.isUserRoleAssignmentInPeriod(
                userRoleAssignment,
                DateTimeHandling.calculateMillis("2013-04-05 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-10 00:00:00"));

        // then
        assertFalse(result);
    }

    @Test
    public void isUserRoleAssignmentInPeriod_OnPeriodStartTime() {
        // given
        UserRoleAssignment userRoleAssignment = new UserRoleAssignment(
                Long.valueOf(10000),
                DateTimeHandling.calculateMillis("2013-04-04 23:59:59"),
                DateTimeHandling.calculateMillis("2013-04-05 00:00:00"));

        // when
        boolean result = calculator.isUserRoleAssignmentInPeriod(
                userRoleAssignment,
                DateTimeHandling.calculateMillis("2013-04-05 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-10 00:00:00"));

        // then
        assertTrue(result);
    }

    @Test
    public void isUserRoleAssignmentInPeriod_InPeriod() {
        // given
        UserRoleAssignment userRoleAssignment = new UserRoleAssignment(
                Long.valueOf(10000),
                DateTimeHandling.calculateMillis("2013-04-05 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-09 23:59:59"));

        // when
        boolean result = calculator.isUserRoleAssignmentInPeriod(
                userRoleAssignment,
                DateTimeHandling.calculateMillis("2013-04-05 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-10 00:00:00"));

        // then
        assertTrue(result);
    }

    @Test
    public void isUserRoleAssignmentInPeriod_OnPeriodEndTime() {
        // given
        UserRoleAssignment userRoleAssignment = new UserRoleAssignment(
                Long.valueOf(10000),
                DateTimeHandling.calculateMillis("2013-04-10 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-10 00:00:01"));

        // when
        boolean result = calculator.isUserRoleAssignmentInPeriod(
                userRoleAssignment,
                DateTimeHandling.calculateMillis("2013-04-05 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-10 00:00:00"));

        // then
        assertFalse(result);
    }

    @Test
    public void isUserRoleAssignmentInPeriod_AfterPeriodEnd() {
        // given
        UserRoleAssignment userRoleAssignment = new UserRoleAssignment(
                Long.valueOf(10000),
                DateTimeHandling.calculateMillis("2013-04-10 00:00:01"),
                DateTimeHandling.calculateMillis("2013-04-10 00:00:02"));

        // when
        boolean result = calculator.isUserRoleAssignmentInPeriod(
                userRoleAssignment,
                DateTimeHandling.calculateMillis("2013-04-05 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-10 00:00:00"));

        // then
        assertFalse(result);
    }

    @Test
    public void userAssignmentExistInPeriod_NoAssignment() {
        // given
        List<UserAssignment> userAssignments = new ArrayList<UserAssignment>();

        // when
        boolean result = calculator.userAssignmentExistInPeriod(
                userAssignments,
                DateTimeHandling.calculateMillis("2013-04-05 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-10 00:00:00"));

        // then
        assertFalse(result);
    }

    @Test
    public void userAssignmentExistInPeriod_NoAssignmentInPeriod() {
        // given
        List<UserAssignment> userAssignments = new ArrayList<UserAssignment>();
        userAssignments.add(createUserAssignment(
                DateTimeHandling.calculateMillis("2013-04-04 23:59:58"),
                DateTimeHandling.calculateMillis("2013-04-04 23:59:59")));

        // when
        boolean result = calculator.userAssignmentExistInPeriod(
                userAssignments,
                DateTimeHandling.calculateMillis("2013-04-05 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-10 00:00:00"));

        // then
        assertFalse(result);
    }

    @Test
    public void userAssignmentExistInPeriod_OneAssignmentInPeriod() {
        // given
        List<UserAssignment> userAssignments = new ArrayList<UserAssignment>();
        userAssignments.add(createUserAssignment(
                DateTimeHandling.calculateMillis("2013-04-10 00:00:01"),
                DateTimeHandling.calculateMillis("2013-04-10 00:00:02")));
        userAssignments.add(createUserAssignment(
                DateTimeHandling.calculateMillis("2013-04-05 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-10 00:00:00")));
        userAssignments.add(createUserAssignment(
                DateTimeHandling.calculateMillis("2013-04-04 23:59:58"),
                DateTimeHandling.calculateMillis("2013-04-04 23:59:59")));

        // when
        boolean result = calculator.userAssignmentExistInPeriod(
                userAssignments,
                DateTimeHandling.calculateMillis("2013-04-05 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-10 00:00:00"));

        // then
        assertTrue(result);
    }

    @Test
    public void findOldestUserAssignmentForPeriod_NoAssignment() {
        // given
        List<UserAssignment> userAssignments = new ArrayList<UserAssignment>();

        // when
        UserAssignment result = calculator.findOldestUserAssignmentForPeriod(
                userAssignments,
                DateTimeHandling.calculateMillis("2013-04-05 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-10 00:00:00"));

        // then
        assertNull(result);
    }

    @Test
    public void findOldestUserAssignmentForPeriod_NoAssignmentInPeriod() {
        // given
        List<UserAssignment> userAssignments = new ArrayList<UserAssignment>();
        userAssignments.add(createUserAssignment(
                DateTimeHandling.calculateMillis("2013-04-04 23:59:58"),
                DateTimeHandling.calculateMillis("2013-04-04 23:59:59")));

        // when
        UserAssignment result = calculator.findOldestUserAssignmentForPeriod(
                userAssignments,
                DateTimeHandling.calculateMillis("2013-04-05 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-10 00:00:00"));

        // then
        assertNull(result);
    }

    @Test
    public void findOldestUserAssignmentForPeriod_MultipleAssignmentInPeriod() {
        // given
        List<UserAssignment> userAssignments = new ArrayList<UserAssignment>();
        userAssignments.add(createUserAssignment(
                DateTimeHandling.calculateMillis("2013-04-10 00:00:01"),
                DateTimeHandling.calculateMillis("2013-04-10 00:00:02")));
        userAssignments.add(createUserAssignment(
                DateTimeHandling.calculateMillis("2013-04-08 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-09 00:00:00")));
        userAssignments.add(createUserAssignment(
                DateTimeHandling.calculateMillis("2013-04-06 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-07 00:00:00")));
        userAssignments.add(createUserAssignment(
                DateTimeHandling.calculateMillis("2013-04-04 23:59:58"),
                DateTimeHandling.calculateMillis("2013-04-04 23:59:59")));

        // when
        UserAssignment result = calculator.findOldestUserAssignmentForPeriod(
                userAssignments,
                DateTimeHandling.calculateMillis("2013-04-05 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-10 00:00:00"));

        // then
        assertEquals(DateTimeHandling.calculateMillis("2013-04-06 00:00:00"),
                result.getUsageStartTime());
        assertEquals(DateTimeHandling.calculateMillis("2013-04-07 00:00:00"),
                result.getUsageEndTime());
    }

    private UserAssignment createUserAssignment(long startTime, long endTime) {
        UserAssignment userAssignment = new UserAssignment();
        userAssignment.setUsageStartTime(startTime);
        userAssignment.setUsageEndTime(endTime);
        return userAssignment;
    }
}
