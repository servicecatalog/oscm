/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: May 2, 2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.calculation.revenue;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;

import org.oscm.billingservice.business.calculation.revenue.CostCalculatorPerUnit;
import org.oscm.billingservice.business.calculation.revenue.model.UserAssignment;
import org.oscm.billingservice.business.calculation.revenue.model.UserAssignmentFactors;
import org.oscm.billingservice.business.calculation.revenue.model.UserRoleAssignment;
import org.oscm.billingservice.service.model.BillingInput;
import org.oscm.test.DateTimeHandling;
import org.oscm.internal.types.enumtypes.PricingPeriod;

/**
 * @author tokoda
 * 
 */
public class CostCalculatorPerUnitComputeUserRoleFactorTest {

    private static final long USER_KEY = 10000;
    private static final String USER_ID = "userA";

    private static final long ROLE_KEY = 11111;
    private static final long ROLE_KEY2 = 22222;
    private static final long ROLE_KEY3 = 33333;

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
    public void computeUserRoleFactorForOneUser_NoAssignment() {
        // given
        PricingPeriod period = PricingPeriod.MONTH;
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2013-04-01 00:00:00", "2013-05-01 00:00:00");

        // when
        calculator.computeUserFactorAndRoleFactorForOneUser(result,
                userAssignments, period, billingInput);

        // then
        assertEquals(0, result.getNumberOfUsers());
        assertEquals(0, result.getBasicFactor(), 0.0);

        assertEquals(0, result.getRoleFactors().size());
    }

    @Test
    public void computeUserRoleFactorForOneUser_NoRoleAssignment() {
        // given
        PricingPeriod period = PricingPeriod.MONTH;
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2013-04-01 00:00:00", "2013-05-01 00:00:00");

        createUserAssignment(USER_KEY, USER_ID,
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-01 00:00:01"),
                new ArrayList<UserRoleAssignment>());

        // when
        calculator.computeUserFactorAndRoleFactorForOneUser(result,
                userAssignments, period, billingInput);

        // then
        assertEquals(1, result.getNumberOfUsers());
        assertEquals(1, result.getBasicFactor(), 0.0);

        assertEquals(0, result.getRoleFactors().size());
    }

    @Test
    public void computeUserRoleFactorForOneUser_OneRoleAssignment() {
        // given
        PricingPeriod period = PricingPeriod.MONTH;
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2013-04-01 00:00:00", "2013-05-01 00:00:00");

        List<UserRoleAssignment> roleAssignments = new ArrayList<UserRoleAssignment>();
        UserRoleAssignment roleAssignment1 = createUserRoleAssignment(
                Long.valueOf(ROLE_KEY),
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-01 00:00:01"), true,
                true);
        roleAssignments.add(roleAssignment1);

        createUserAssignment(USER_KEY, USER_ID,
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-01 00:00:01"),
                roleAssignments);

        // when
        calculator.computeUserFactorAndRoleFactorForOneUser(result,
                userAssignments, period, billingInput);

        // then
        assertEquals(1, result.getNumberOfUsers());
        assertEquals(1, result.getBasicFactor(), 0.0);

        assertEquals(1, result.getRoleFactors().size());
        assertEquals(1, result.getRoleFactors().get(Long.valueOf(ROLE_KEY))
                .doubleValue(), 0.0);
    }

    @Test
    public void computeUserRoleFactorForOneUser_MultipleRoleAssignments() {
        // given
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+1"));
        
        PricingPeriod period = PricingPeriod.WEEK;
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2013-04-01 00:00:00", "2013-05-01 00:00:00");

        List<UserRoleAssignment> roleAssignments = new ArrayList<UserRoleAssignment>();
        UserRoleAssignment roleAssignment1 = createUserRoleAssignment(
                Long.valueOf(ROLE_KEY),
                DateTimeHandling.calculateMillis("2013-04-25 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-27 00:00:00"), false,
                true);
        roleAssignments.add(roleAssignment1);

        UserRoleAssignment roleAssignment2 = createUserRoleAssignment(
                Long.valueOf(ROLE_KEY2),
                DateTimeHandling.calculateMillis("2013-04-10 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-25 00:00:00"), false,
                false);
        roleAssignments.add(roleAssignment2);

        UserRoleAssignment roleAssignment3 = createUserRoleAssignment(
                Long.valueOf(ROLE_KEY3),
                DateTimeHandling.calculateMillis("2013-04-09 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-10 00:00:00"), true,
                false);
        roleAssignments.add(roleAssignment3);

        createUserAssignment(USER_KEY, USER_ID,
                DateTimeHandling.calculateMillis("2013-04-9 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-27 00:00:01"),
                roleAssignments);

        // when
        calculator.computeUserFactorAndRoleFactorForOneUser(result,
                userAssignments, period, billingInput);
        // then
        assertEquals(1, result.getNumberOfUsers());
        assertEquals(3, result.getBasicFactor(), 0.0);

        assertEquals(3, result.getRoleFactors().size());
        // From '25-04-2013 00:00:00' to '29-04-2013 00:00:00'
        assertEquals(0.57142857,
                result.getRoleFactors().get(Long.valueOf(ROLE_KEY))
                        .doubleValue(), 0.0000001);
        // From '10-04-2013 00:00:00' to '25-04-2013 00:00:00'
        assertEquals(2.14285714,
                result.getRoleFactors().get(Long.valueOf(ROLE_KEY2))
                        .doubleValue(), 0.0000001);
        // From '08-04-2013 00:00:00' to '10-04-2013 00:00:00'
        assertEquals(0.28571428,
                result.getRoleFactors().get(Long.valueOf(ROLE_KEY3))
                        .doubleValue(), 0.0000001);
    }

    @Test
    public void computeUserRoleFactorForOneUser_MultipleUserAssignmentsWithSameRole() {
        // given
        PricingPeriod period = PricingPeriod.MONTH;
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2013-04-01 00:00:00", "2013-05-01 00:00:00");

        List<UserRoleAssignment> roleAssignments1 = new ArrayList<UserRoleAssignment>();
        UserRoleAssignment roleAssignment1 = createUserRoleAssignment(
                Long.valueOf(ROLE_KEY),
                DateTimeHandling.calculateMillis("2013-04-05 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-30 00:00:00"), true,
                true);
        roleAssignments1.add(roleAssignment1);

        createUserAssignment(USER_KEY, USER_ID,
                DateTimeHandling.calculateMillis("2013-04-05 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-30 00:00:00"),
                roleAssignments1);

        List<UserRoleAssignment> roleAssignments2 = new ArrayList<UserRoleAssignment>();
        UserRoleAssignment roleAssignment2 = createUserRoleAssignment(
                Long.valueOf(ROLE_KEY),
                DateTimeHandling.calculateMillis("2013-04-02 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-03 00:00:00"), true,
                true);
        roleAssignments2.add(roleAssignment2);

        createUserAssignment(USER_KEY, USER_ID,
                DateTimeHandling.calculateMillis("2013-04-02 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-03 00:00:00"),
                roleAssignments2);

        // when
        calculator.computeUserFactorAndRoleFactorForOneUser(result,
                userAssignments, period, billingInput);

        // then
        assertEquals(1, result.getNumberOfUsers());
        assertEquals(1, result.getBasicFactor(), 0.0);

        assertEquals(1, result.getRoleFactors().size());
        assertEquals(1, result.getRoleFactors().get(Long.valueOf(ROLE_KEY))
                .doubleValue(), 0.0000001);
    }

    @Test
    public void computeUserRoleFactorForOneUser_MultipleUserAssignmentsWithDifferentRoles() {
        // given
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+1"));
        
        PricingPeriod period = PricingPeriod.MONTH;
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2013-04-01 00:00:00", "2013-05-01 00:00:00");

        List<UserRoleAssignment> roleAssignments1 = new ArrayList<UserRoleAssignment>();
        UserRoleAssignment roleAssignment1 = createUserRoleAssignment(
                Long.valueOf(ROLE_KEY),
                DateTimeHandling.calculateMillis("2013-04-05 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-30 00:00:00"), true,
                true);
        roleAssignments1.add(roleAssignment1);

        createUserAssignment(USER_KEY, USER_ID,
                DateTimeHandling.calculateMillis("2013-04-05 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-30 00:00:00"),
                roleAssignments1);

        List<UserRoleAssignment> roleAssignments2 = new ArrayList<UserRoleAssignment>();
        UserRoleAssignment roleAssignment2 = createUserRoleAssignment(
                Long.valueOf(ROLE_KEY2),
                DateTimeHandling.calculateMillis("2013-04-02 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-03 00:00:00"), true,
                true);
        roleAssignments2.add(roleAssignment2);

        createUserAssignment(USER_KEY, USER_ID,
                DateTimeHandling.calculateMillis("2013-04-02 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-03 00:00:00"),
                roleAssignments2);

        // when
        calculator.computeUserFactorAndRoleFactorForOneUser(result,
                userAssignments, period, billingInput);

        // then
        assertEquals(1, result.getNumberOfUsers());
        assertEquals(1, result.getBasicFactor(), 0.0);

        assertEquals(2, result.getRoleFactors().size());
        // From '05-04-2013 00:00:00' to '01-05-2013 00:00:00'
        assertEquals(0.86666666,
                result.getRoleFactors().get(Long.valueOf(ROLE_KEY))
                        .doubleValue(), 0.0000001);
        // From '01-04-2013 00:00:00' to '05-04-2013 00:00:00'
        assertEquals(0.13333333,
                result.getRoleFactors().get(Long.valueOf(ROLE_KEY2))
                        .doubleValue(), 0.0000001);

    }

    @Test
    public void computeUserRoleFactorForOneUser_MultipleUserAssignmentsInDifferentTimeSlices() {
        // given
        PricingPeriod period = PricingPeriod.WEEK;
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2013-04-01 00:00:00", "2013-05-01 00:00:00");

        List<UserRoleAssignment> roleAssignments1 = new ArrayList<UserRoleAssignment>();
        UserRoleAssignment roleAssignment1 = createUserRoleAssignment(
                Long.valueOf(ROLE_KEY),
                DateTimeHandling.calculateMillis("2013-04-09 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-12 00:00:00"), true,
                true);
        roleAssignments1.add(roleAssignment1);

        createUserAssignment(USER_KEY, USER_ID,
                DateTimeHandling.calculateMillis("2013-04-09 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-12 00:00:00"),
                roleAssignments1);

        List<UserRoleAssignment> roleAssignments2 = new ArrayList<UserRoleAssignment>();
        UserRoleAssignment roleAssignment2 = createUserRoleAssignment(
                Long.valueOf(ROLE_KEY2),
                DateTimeHandling.calculateMillis("2013-04-02 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-03 00:00:00"), true,
                true);
        roleAssignments2.add(roleAssignment2);

        createUserAssignment(USER_KEY, USER_ID,
                DateTimeHandling.calculateMillis("2013-04-02 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-03 00:00:00"),
                roleAssignments2);

        // when
        calculator.computeUserFactorAndRoleFactorForOneUser(result,
                userAssignments, period, billingInput);

        // then
        assertEquals(1, result.getNumberOfUsers());
        assertEquals(2, result.getBasicFactor(), 0.0);

        assertEquals(2, result.getRoleFactors().size());
        assertEquals(1, result.getRoleFactors().get(Long.valueOf(ROLE_KEY))
                .doubleValue(), 0.0000001);
        assertEquals(1, result.getRoleFactors().get(Long.valueOf(ROLE_KEY2))
                .doubleValue(), 0.0000001);

    }

    @Test
    public void computeUserRoleFactorForOneUser_EmptyTimeSliceBetweenMultipleUserAssignments() {
        // given
        PricingPeriod period = PricingPeriod.WEEK;
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2013-04-01 00:00:00", "2013-05-01 00:00:00");

        List<UserRoleAssignment> roleAssignments1 = new ArrayList<UserRoleAssignment>();
        UserRoleAssignment roleAssignment1 = createUserRoleAssignment(
                Long.valueOf(ROLE_KEY),
                DateTimeHandling.calculateMillis("2013-04-16 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-18 00:00:00"), true,
                true);
        roleAssignments1.add(roleAssignment1);

        createUserAssignment(USER_KEY, USER_ID,
                DateTimeHandling.calculateMillis("2013-04-16 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-18 00:00:00"),
                roleAssignments1);

        List<UserRoleAssignment> roleAssignments2 = new ArrayList<UserRoleAssignment>();
        UserRoleAssignment roleAssignment2 = createUserRoleAssignment(
                Long.valueOf(ROLE_KEY2),
                DateTimeHandling.calculateMillis("2013-04-02 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-03 00:00:00"), true,
                true);
        roleAssignments2.add(roleAssignment2);

        createUserAssignment(USER_KEY, USER_ID,
                DateTimeHandling.calculateMillis("2013-04-02 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-03 00:00:00"),
                roleAssignments2);

        // when
        calculator.computeUserFactorAndRoleFactorForOneUser(result,
                userAssignments, period, billingInput);

        // then
        assertEquals(1, result.getNumberOfUsers());
        assertEquals(2, result.getBasicFactor(), 0.0);

        assertEquals(2, result.getRoleFactors().size());
        assertEquals(1, result.getRoleFactors().get(Long.valueOf(ROLE_KEY))
                .doubleValue(), 0.0000001);
        assertEquals(1, result.getRoleFactors().get(Long.valueOf(ROLE_KEY2))
                .doubleValue(), 0.0000001);

    }

    @Test
    public void computeUserRoleFactorForOneUser_AssignmentsAcrossingMultipleTimeSlices() {
        // given
        PricingPeriod period = PricingPeriod.WEEK;
        BillingInput billingInput = BillingInputFactory.newBillingInput(
                "2013-04-01 00:00:00", "2013-05-01 00:00:00");

        List<UserRoleAssignment> roleAssignments1 = new ArrayList<UserRoleAssignment>();
        UserRoleAssignment roleAssignment1 = createUserRoleAssignment(
                Long.valueOf(ROLE_KEY),
                DateTimeHandling.calculateMillis("2013-04-18 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-23 00:00:00"), true,
                true);
        roleAssignments1.add(roleAssignment1);

        createUserAssignment(USER_KEY, USER_ID,
                DateTimeHandling.calculateMillis("2013-04-18 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-23 00:00:00"),
                roleAssignments1);

        List<UserRoleAssignment> roleAssignments2 = new ArrayList<UserRoleAssignment>();
        UserRoleAssignment roleAssignment2 = createUserRoleAssignment(
                Long.valueOf(ROLE_KEY2),
                DateTimeHandling.calculateMillis("2013-04-04 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-16 00:00:00"), true,
                true);
        roleAssignments2.add(roleAssignment2);

        createUserAssignment(USER_KEY, USER_ID,
                DateTimeHandling.calculateMillis("2013-04-04 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-16 00:00:00"),
                roleAssignments2);

        List<UserRoleAssignment> roleAssignments3 = new ArrayList<UserRoleAssignment>();
        UserRoleAssignment roleAssignment3 = createUserRoleAssignment(
                Long.valueOf(ROLE_KEY3),
                DateTimeHandling.calculateMillis("2013-04-02 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-03 00:00:00"), true,
                true);
        roleAssignments3.add(roleAssignment3);

        createUserAssignment(USER_KEY, USER_ID,
                DateTimeHandling.calculateMillis("2013-04-02 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-03 00:00:00"),
                roleAssignments3);

        // when
        calculator.computeUserFactorAndRoleFactorForOneUser(result,
                userAssignments, period, billingInput);

        // then
        assertEquals(1, result.getNumberOfUsers());
        assertEquals(4, result.getBasicFactor(), 0.0);

        assertEquals(3, result.getRoleFactors().size());
        // From '18-04-2013 00:00:00' to '29-04-2013 00:00:00'
        assertEquals(1.57142857,
                result.getRoleFactors().get(Long.valueOf(ROLE_KEY))
                        .doubleValue(), 0.0000001);
        // From '04-04-2013 00:00:00' to '18-04-2013 00:00:00'
        assertEquals(2, result.getRoleFactors().get(Long.valueOf(ROLE_KEY2))
                .doubleValue(), 0.0000001);
        // From '01-04-2013 00:00:00' to '04-04-2013 00:00:00'
        assertEquals(0.42857142,
                result.getRoleFactors().get(Long.valueOf(ROLE_KEY3))
                        .doubleValue(), 0.0000001);

    }

    private void createUserAssignment(long userKey, String userId,
            long usageStartTime, long usageEndTime,
            List<UserRoleAssignment> roleAssignments) {
        UserAssignment ua = new UserAssignment();
        ua.setUserKey(userKey);
        ua.setUserId(userId);
        ua.setUsageStartTime(usageStartTime);
        ua.setUsageEndTime(usageEndTime);
        ua.setRoleAssignments(roleAssignments);
        userAssignments.add(ua);
    }

    private UserRoleAssignment createUserRoleAssignment(Long roleKey,
            long startTime, long endTime, boolean isFirstRoleAssignment,
            boolean isLastRoleAssignment) {
        UserRoleAssignment role = new UserRoleAssignment(roleKey, startTime,
                endTime);
        role.setFirstRoleAssignment(isFirstRoleAssignment);
        role.setLastRoleAssignment(isLastRoleAssignment);
        return role;
    }
}
