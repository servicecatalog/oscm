/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: May 28, 2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.calculation.revenue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.oscm.billingservice.business.calculation.revenue.CostCalculatorPerUnit;
import org.oscm.billingservice.business.calculation.revenue.model.UserAssignment;
import org.oscm.billingservice.business.calculation.revenue.model.UserRoleAssignment;
import org.oscm.test.DateTimeHandling;

/**
 * @author tokoda
 * 
 */
public class CostCalculatorPerUnitUserAssignmentHandling {

    private static final long USER_KEY = 10000;

    private static final long ROLE_KEY1 = 20000;
    private static final long ROLE_KEY2 = 20001;
    private static final long ROLE_KEY3 = 20002;

    private CostCalculatorPerUnit calculator;

    private List<UserAssignment> userAssignmentsForOneUser;

    @Before
    public void setup() {
        calculator = new CostCalculatorPerUnit();
        userAssignmentsForOneUser = new ArrayList<UserAssignment>();
    }

    @Test
    public void determineRoleAssignmentsWithFillingBlank_EmptyUserAssignment() {
        // given

        // when
        List<UserRoleAssignment> result = calculator
                .determineRoleAssignmentsWithFillingBlank(userAssignmentsForOneUser);

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    public void determineRoleAssignmentsWithFillingBlank_EmptyUserRoleAssignments() {
        // given
        createUserAssignmentWithRoles(USER_KEY,
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"), null);

        // when
        List<UserRoleAssignment> result = calculator
                .determineRoleAssignmentsWithFillingBlank(userAssignmentsForOneUser);

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    public void determineRoleAssignmentsWithFillingBlank_OneUserAssignmentWithRoles() {
        // given
        List<UserRoleAssignment> roleAssignments = new ArrayList<UserRoleAssignment>();
        roleAssignments.add(new UserRoleAssignment(Long.valueOf(ROLE_KEY2),
                DateTimeHandling.calculateMillis("2013-04-04 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-06 00:00:00")));
        roleAssignments.add(new UserRoleAssignment(Long.valueOf(ROLE_KEY1),
                DateTimeHandling.calculateMillis("2013-04-02 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-04 00:00:00")));

        createUserAssignmentWithRoles(USER_KEY,
                DateTimeHandling.calculateMillis("2013-04-02 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-06 00:00:00"),
                roleAssignments);

        // when
        List<UserRoleAssignment> result = calculator
                .determineRoleAssignmentsWithFillingBlank(userAssignmentsForOneUser);

        // then
        assertEquals(2, result.size());

        assertEquals(ROLE_KEY2, result.get(0).getRoleKey().longValue());
        assertEquals(DateTimeHandling.calculateMillis("2013-04-04 00:00:00"),
                result.get(0).getStartTime());
        assertEquals(DateTimeHandling.calculateMillis("2013-04-06 00:00:00"),
                result.get(0).getEndTime());

        assertEquals(ROLE_KEY1, result.get(1).getRoleKey().longValue());
        assertEquals(DateTimeHandling.calculateMillis("2013-04-02 00:00:00"),
                result.get(1).getStartTime());
        assertEquals(DateTimeHandling.calculateMillis("2013-04-04 00:00:00"),
                result.get(1).getEndTime());
    }

    @Test
    public void determineRoleAssignmentsWithFillingBlank_MultipleUserAssignments() {
        // given
        List<UserRoleAssignment> roleAssignments1 = new ArrayList<UserRoleAssignment>();
        roleAssignments1.add(new UserRoleAssignment(Long.valueOf(ROLE_KEY1),
                DateTimeHandling.calculateMillis("2013-04-08 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-09 00:00:00")));
        roleAssignments1.add(new UserRoleAssignment(Long.valueOf(ROLE_KEY3),
                DateTimeHandling.calculateMillis("2013-04-07 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-08 00:00:00")));

        createUserAssignmentWithRoles(USER_KEY,
                DateTimeHandling.calculateMillis("2013-04-07 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-09 00:00:00"),
                roleAssignments1);

        List<UserRoleAssignment> roleAssignments2 = new ArrayList<UserRoleAssignment>();
        roleAssignments2.add(new UserRoleAssignment(Long.valueOf(ROLE_KEY2),
                DateTimeHandling.calculateMillis("2013-04-05 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-06 00:00:00")));

        createUserAssignmentWithRoles(USER_KEY,
                DateTimeHandling.calculateMillis("2013-04-05 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-06 00:00:00"),
                roleAssignments2);

        List<UserRoleAssignment> roleAssignments3 = new ArrayList<UserRoleAssignment>();
        roleAssignments3.add(new UserRoleAssignment(Long.valueOf(ROLE_KEY2),
                DateTimeHandling.calculateMillis("2013-04-03 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-04 00:00:00")));
        roleAssignments3.add(new UserRoleAssignment(Long.valueOf(ROLE_KEY1),
                DateTimeHandling.calculateMillis("2013-04-02 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-03 00:00:00")));

        createUserAssignmentWithRoles(USER_KEY,
                DateTimeHandling.calculateMillis("2013-04-02 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-04 00:00:00"),
                roleAssignments3);

        // when
        List<UserRoleAssignment> result = calculator
                .determineRoleAssignmentsWithFillingBlank(userAssignmentsForOneUser);

        // then
        assertEquals(4, result.size());

        assertEquals(ROLE_KEY1, result.get(0).getRoleKey().longValue());
        assertEquals(DateTimeHandling.calculateMillis("2013-04-08 00:00:00"),
                result.get(0).getStartTime());
        assertEquals(DateTimeHandling.calculateMillis("2013-04-09 00:00:00"),
                result.get(0).getEndTime());

        assertEquals(ROLE_KEY3, result.get(1).getRoleKey().longValue());
        assertEquals(DateTimeHandling.calculateMillis("2013-04-07 00:00:00"),
                result.get(1).getStartTime());
        assertEquals(DateTimeHandling.calculateMillis("2013-04-08 00:00:00"),
                result.get(1).getEndTime());

        assertEquals(ROLE_KEY2, result.get(2).getRoleKey().longValue());
        assertEquals(DateTimeHandling.calculateMillis("2013-04-03 00:00:00"),
                result.get(2).getStartTime());
        assertEquals(DateTimeHandling.calculateMillis("2013-04-07 00:00:00"),
                result.get(2).getEndTime());

        assertEquals(ROLE_KEY1, result.get(3).getRoleKey().longValue());
        assertEquals(DateTimeHandling.calculateMillis("2013-04-02 00:00:00"),
                result.get(3).getStartTime());
        assertEquals(DateTimeHandling.calculateMillis("2013-04-03 00:00:00"),
                result.get(3).getEndTime());
    }

    private void createUserAssignmentWithRoles(long userKey, long startTime,
            long endTime, List<UserRoleAssignment> roles) {
        UserAssignment userAssignment = new UserAssignment();
        userAssignment.setUserKey(userKey);
        userAssignment.setUserId("ID");
        userAssignment.setUsageStartTime(startTime);
        userAssignment.setUsageEndTime(endTime);
        if (roles != null) {
            userAssignment.setRoleAssignments(roles);
        }
        userAssignmentsForOneUser.add(userAssignment);
    }
}
