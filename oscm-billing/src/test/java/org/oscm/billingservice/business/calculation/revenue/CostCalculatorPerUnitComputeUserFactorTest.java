/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Apr 30, 2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.calculation.revenue;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.oscm.billingservice.business.calculation.revenue.CostCalculatorPerUnit;
import org.oscm.billingservice.business.calculation.revenue.model.UserAssignment;
import org.oscm.billingservice.business.calculation.revenue.model.UserAssignmentDetails;
import org.oscm.billingservice.business.calculation.revenue.model.UserAssignmentFactors;
import org.oscm.billingservice.service.model.BillingInput;
import org.oscm.test.DateTimeHandling;
import org.oscm.internal.types.enumtypes.PricingPeriod;

/**
 * @author tokoda
 * 
 */
public class CostCalculatorPerUnitComputeUserFactorTest {

    private static final long USER_KEY = 10000;
    private static final String USER_ID = "userA";
    private static final String USER_ID_CHANGED = "userA2";

    private CostCalculatorPerUnit calculator;
    private UserAssignmentFactors result;
    private List<UserAssignment> userAssignments;

    @Before
    public void setup() {
        calculator = new CostCalculatorPerUnit();
        result = new UserAssignmentFactors();
        userAssignments = new ArrayList<UserAssignment>();
    }

    @Test
    public void computeUserFactorForOneUser_NoAssignment() {
        // given
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2013-04-01 00:00:00", "2013-05-01 00:00:00");

        // when
        calculator.computeUserFactorAndRoleFactorForOneUser(result,
                userAssignments, PricingPeriod.WEEK, billingInput);

        // then
        assertEquals(0, result.getNumberOfUsers());
        assertEquals(0, result.getBasicFactor(), 0.0);
    }

    @Test
    public void computeUserFactorForOneUser_OneAssignment() {
        // given
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2013-04-01 00:00:00", "2013-05-01 00:00:00");

        createUserAssignment(USER_KEY, USER_ID,
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-01 00:00:01"));

        // when
        calculator.computeUserFactorAndRoleFactorForOneUser(result,
                userAssignments, PricingPeriod.MONTH, billingInput);

        // then
        assertEquals(1, result.getNumberOfUsers());
        assertEquals(1, result.getBasicFactor(), 0.0);

        UserAssignmentDetails detail = result.getUserAssignmentDetails(Long
                .valueOf(USER_KEY));
        assertEquals(USER_ID, detail.getUserId());
        assertEquals(1, detail.getUsageDetails().getFactor(), 0.0);
    }

    @Test
    public void computeUserFactorForOneUser_MultipleAssignmentsInOneSlice() {
        // given
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2013-04-01 00:00:00", "2013-05-01 00:00:00");

        createUserAssignment(USER_KEY, USER_ID,
                DateTimeHandling.calculateMillis("2013-04-30 23:59:59"),
                DateTimeHandling.calculateMillis("2013-05-01 00:00:00"));

        createUserAssignment(USER_KEY, USER_ID,
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-01 00:00:01"));

        // when
        calculator.computeUserFactorAndRoleFactorForOneUser(result,
                userAssignments, PricingPeriod.MONTH, billingInput);

        // then
        assertEquals(1, result.getNumberOfUsers());
        assertEquals(1, result.getBasicFactor(), 0.0);

        UserAssignmentDetails detail = result.getUserAssignmentDetails(Long
                .valueOf(USER_KEY));
        assertEquals(USER_ID, detail.getUserId());
        assertEquals(1, detail.getUsageDetails().getFactor(), 0.0);
    }

    @Test
    public void computeUserFactorForOneUser_MultipleAssignmentsHavingEmptySlices() {
        // given
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2013-04-01 00:00:00", "2013-05-01 00:00:00");

        createUserAssignment(USER_KEY, USER_ID,
                DateTimeHandling.calculateMillis("2013-04-28 23:59:59"),
                DateTimeHandling.calculateMillis("2013-04-29 00:00:00"));

        createUserAssignment(USER_KEY, USER_ID,
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-01 00:00:01"));

        // when
        calculator.computeUserFactorAndRoleFactorForOneUser(result,
                userAssignments, PricingPeriod.WEEK, billingInput);

        // then
        assertEquals(1, result.getNumberOfUsers());
        assertEquals(2, result.getBasicFactor(), 0.0);

        UserAssignmentDetails detail = result.getUserAssignmentDetails(Long
                .valueOf(USER_KEY));
        assertEquals(USER_ID, detail.getUserId());
        assertEquals(2, detail.getUsageDetails().getFactor(), 0.0);
    }

    @Test
    public void computeUserFactorForOneUser_MultipleAssignmentsAcrossSlices() {
        // given
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2013-04-01 00:00:00", "2013-05-01 00:00:00");

        createUserAssignment(USER_KEY, USER_ID,
                DateTimeHandling.calculateMillis("2013-04-14 23:59:59"),
                DateTimeHandling.calculateMillis("2013-04-15 00:00:00"));

        createUserAssignment(USER_KEY, USER_ID,
                DateTimeHandling.calculateMillis("2013-04-07 23:59:59"),
                DateTimeHandling.calculateMillis("2013-04-08 00:00:00"));

        // when
        calculator.computeUserFactorAndRoleFactorForOneUser(result,
                userAssignments, PricingPeriod.WEEK, billingInput);

        // then
        assertEquals(1, result.getNumberOfUsers());
        assertEquals(3, result.getBasicFactor(), 0.0);

        UserAssignmentDetails detail = result.getUserAssignmentDetails(Long
                .valueOf(USER_KEY));
        assertEquals(USER_ID, detail.getUserId());
        assertEquals(3, detail.getUsageDetails().getFactor(), 0.0);
    }

    @Test
    public void computeUserFactorForOneUser_UserIdChanged() {
        // given
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2013-04-01 00:00:00", "2013-05-01 00:00:00");

        createUserAssignment(USER_KEY, USER_ID_CHANGED,
                DateTimeHandling.calculateMillis("2013-04-11 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-11 00:00:01"));

        createUserAssignment(USER_KEY, USER_ID,
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-01 00:00:01"));

        // when
        calculator.computeUserFactorAndRoleFactorForOneUser(result,
                userAssignments, PricingPeriod.WEEK, billingInput);

        // then
        assertEquals(1, result.getNumberOfUsers());
        assertEquals(2, result.getBasicFactor(), 0.0);

        UserAssignmentDetails detail = result.getUserAssignmentDetails(Long
                .valueOf(USER_KEY));
        assertEquals(USER_ID_CHANGED, detail.getUserId());
        assertEquals(2, detail.getUsageDetails().getFactor(), 0.0);
    }

    private void createUserAssignment(long userKey, String userId,
            long usageStartTime, long usageEndTime) {
        UserAssignment ua = new UserAssignment();
        ua.setUserKey(userKey);
        ua.setUserId(userId);
        ua.setUsageStartTime(usageStartTime);
        ua.setUsageEndTime(usageEndTime);
        userAssignments.add(ua);
    }
}
