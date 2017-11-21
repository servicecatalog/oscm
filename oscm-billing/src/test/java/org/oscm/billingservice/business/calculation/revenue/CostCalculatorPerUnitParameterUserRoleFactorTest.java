/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 08.04.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.calculation.revenue;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import org.oscm.billingservice.business.calculation.revenue.model.TimeSlice;
import org.oscm.billingservice.business.calculation.revenue.model.UserAssignment;
import org.oscm.billingservice.business.calculation.revenue.model.UserRoleAssignment;
import org.oscm.billingservice.dao.model.RolePricingData;
import org.oscm.billingservice.dao.model.RolePricingDetails;
import org.oscm.billingservice.dao.model.XParameterData;
import org.oscm.billingservice.dao.model.XParameterIdData;
import org.oscm.billingservice.dao.model.XParameterPeriodPrimitiveType;
import org.oscm.billingservice.dao.model.XParameterPeriodValue;
import org.oscm.test.DateTimeHandling;
import org.oscm.internal.types.enumtypes.ParameterType;
import org.oscm.internal.types.enumtypes.ParameterValueType;
import org.oscm.internal.types.enumtypes.PricingPeriod;

/**
 * @author kulle
 * 
 */
public class CostCalculatorPerUnitParameterUserRoleFactorTest {

    private static final long PERIOD_VALUE_KEY1 = 10000;
    private static final long PERIOD_VALUE_KEY2 = 10001;
    private static final long PERIOD_VALUE_KEY3 = 10002;

    private static final long USER_KEY = 20000;

    private static final long ROLE_KEY1 = 30000;
    private static final long ROLE_KEY2 = 30001;
    private static final long ROLE_KEY3 = 30002;

    private CostCalculatorPerUnit calculator;
    private TimeSlice timeSlice;
    private XParameterIdData idData;
    private LinkedList<XParameterPeriodValue> periodValues;
    private ArrayList<UserAssignment> userAssignments;

    @Before
    public void setup() {
        calculator = new CostCalculatorPerUnit();
        timeSlice = new TimeSlice(
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-08 00:00:00") - 1,
                PricingPeriod.WEEK);

        XParameterData pData = new XParameterData();
        idData = pData.getIdDataInstance("ID",
                ParameterType.PLATFORM_PARAMETER, ParameterValueType.INTEGER);

        periodValues = new LinkedList<XParameterPeriodValue>();
        userAssignments = new ArrayList<UserAssignment>();

    }

    @Test
    public void computeParameterUserFactorForOneTimeSliceAndOneUser_OneRolesForOneUserAssignmentForOnePeriodValue() {
        // given
        addParameterPeriodValue(PERIOD_VALUE_KEY1, "A",
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-08 00:00:00"));

        List<UserRoleAssignment> roleAssignments = new ArrayList<UserRoleAssignment>();
        roleAssignments.add(new UserRoleAssignment(Long.valueOf(ROLE_KEY1),
                DateTimeHandling.calculateMillis("2013-04-01 01:00:00"),
                DateTimeHandling.calculateMillis("2013-04-01 02:00:00")));

        addUserAssignmentWithRole(USER_KEY,
                DateTimeHandling.calculateMillis("2013-04-01 01:00:00"),
                DateTimeHandling.calculateMillis("2013-04-01 02:00:00"),
                roleAssignments);

        // when
        calculator.computeParameterUserFactorForOneTimeSliceAndOneUser(
                timeSlice, periodValues, userAssignments);

        // then
        assertEquals(1, periodValues.size());
        assertEquals(PERIOD_VALUE_KEY1, periodValues.get(0).getKey()
                .longValue());
        assertEquals(1, periodValues.get(0).getUserAssignmentFactor(), 0.000001);

        RolePricingData rolePricingData = periodValues.get(0).getRolePrices();
        assertEquals(1,
                roleFactorOf(ROLE_KEY1, PERIOD_VALUE_KEY1, rolePricingData),
                0.000001);
    }

    @Test
    public void computeParameterUserFactorForOneTimeSliceAndOneUser_TwoRolesForOneUserAssignmentForOnePeriodValue() {
        // given
        addParameterPeriodValue(PERIOD_VALUE_KEY1, "A",
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-08 00:00:00"));

        List<UserRoleAssignment> roleAssignments = new ArrayList<UserRoleAssignment>();
        roleAssignments.add(new UserRoleAssignment(Long.valueOf(ROLE_KEY2),
                DateTimeHandling.calculateMillis("2013-04-04 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-05 00:00:00")));
        roleAssignments.add(new UserRoleAssignment(Long.valueOf(ROLE_KEY1),
                DateTimeHandling.calculateMillis("2013-04-03 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-04 00:00:00")));

        addUserAssignmentWithRole(USER_KEY,
                DateTimeHandling.calculateMillis("2013-04-03 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-05 00:00:00"),
                roleAssignments);

        // when
        calculator.computeParameterUserFactorForOneTimeSliceAndOneUser(
                timeSlice, periodValues, userAssignments);

        // then
        assertEquals(1, periodValues.size());
        assertEquals(10000, periodValues.get(0).getKey().longValue());
        assertEquals(1, periodValues.get(0).getUserAssignmentFactor(), 0.000001);

        RolePricingData rolePricingData = periodValues.get(0).getRolePrices();
        // From 2013-04-01 00:00:00 to 2013-04-04 00:00:00
        assertEquals(0.42857142,
                roleFactorOf(ROLE_KEY1, PERIOD_VALUE_KEY1, rolePricingData),
                0.000001);
        // From 2013-04-04 00:00:00 to 2013-04-08 00:00:00
        assertEquals(0.57142857,
                roleFactorOf(ROLE_KEY2, PERIOD_VALUE_KEY1, rolePricingData),
                0.000001);
    }

    @Test
    public void computeParameterUserFactorForOneTimeSliceAndOneUser_OneRoleForEachUserAssignmentForOnePeriodValue() {
        // given
        addParameterPeriodValue(PERIOD_VALUE_KEY1, "A",
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-08 00:00:00"));

        List<UserRoleAssignment> roleAssignments1 = new ArrayList<UserRoleAssignment>();
        roleAssignments1.add(new UserRoleAssignment(Long.valueOf(ROLE_KEY2),
                DateTimeHandling.calculateMillis("2013-04-04 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-05 00:00:00")));

        addUserAssignmentWithRole(USER_KEY,
                DateTimeHandling.calculateMillis("2013-04-04 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-05 00:00:00"),
                roleAssignments1);

        List<UserRoleAssignment> roleAssignments2 = new ArrayList<UserRoleAssignment>();
        roleAssignments1.add(new UserRoleAssignment(Long.valueOf(ROLE_KEY1),
                DateTimeHandling.calculateMillis("2013-04-02 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-03 00:00:00")));

        addUserAssignmentWithRole(USER_KEY,
                DateTimeHandling.calculateMillis("2013-04-02 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-03 00:00:00"),
                roleAssignments2);

        // when
        calculator.computeParameterUserFactorForOneTimeSliceAndOneUser(
                timeSlice, periodValues, userAssignments);

        // then
        assertEquals(1, periodValues.size());
        assertEquals(10000, periodValues.get(0).getKey().longValue());
        assertEquals(1, periodValues.get(0).getUserAssignmentFactor(), 0.000001);

        RolePricingData rolePricingData = periodValues.get(0).getRolePrices();
        // From 2013-04-01 00:00:00 to 2013-04-04 00:00:00
        assertEquals(0.42857142,
                roleFactorOf(ROLE_KEY1, PERIOD_VALUE_KEY1, rolePricingData),
                0.000001);
        // From 2013-04-04 00:00:00 to 2013-04-08 00:00:00
        assertEquals(0.57142857,
                roleFactorOf(ROLE_KEY2, PERIOD_VALUE_KEY1, rolePricingData),
                0.000001);
    }

    @Test
    public void computeParameterUserFactorForOneTimeSliceAndOneUser_TwoRolesForEachUserAssignmentForOnePeriodValue() {
        // given
        addParameterPeriodValue(PERIOD_VALUE_KEY1, "A",
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-08 00:00:00"));

        List<UserRoleAssignment> roleAssignments1 = new ArrayList<UserRoleAssignment>();
        roleAssignments1.add(new UserRoleAssignment(Long.valueOf(ROLE_KEY3),
                DateTimeHandling.calculateMillis("2013-04-06 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-07 00:00:00")));
        roleAssignments1.add(new UserRoleAssignment(Long.valueOf(ROLE_KEY2),
                DateTimeHandling.calculateMillis("2013-04-05 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-06 00:00:00")));

        addUserAssignmentWithRole(USER_KEY,
                DateTimeHandling.calculateMillis("2013-04-05 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-07 00:00:00"),
                roleAssignments1);

        List<UserRoleAssignment> roleAssignments2 = new ArrayList<UserRoleAssignment>();
        roleAssignments1.add(new UserRoleAssignment(Long.valueOf(ROLE_KEY2),
                DateTimeHandling.calculateMillis("2013-04-03 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-04 00:00:00")));
        roleAssignments1.add(new UserRoleAssignment(Long.valueOf(ROLE_KEY1),
                DateTimeHandling.calculateMillis("2013-04-02 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-03 00:00:00")));

        addUserAssignmentWithRole(USER_KEY,
                DateTimeHandling.calculateMillis("2013-04-02 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-04 00:00:00"),
                roleAssignments2);

        // when
        calculator.computeParameterUserFactorForOneTimeSliceAndOneUser(
                timeSlice, periodValues, userAssignments);

        // then
        assertEquals(1, periodValues.size());
        assertEquals(10000, periodValues.get(0).getKey().longValue());
        assertEquals(1, periodValues.get(0).getUserAssignmentFactor(), 0.000001);

        RolePricingData rolePricingData = periodValues.get(0).getRolePrices();
        // From 2013-04-01 00:00:00 to 2013-04-03 00:00:00
        assertEquals(0.28571428,
                roleFactorOf(ROLE_KEY1, PERIOD_VALUE_KEY1, rolePricingData),
                0.000001);
        // From 2013-04-03 00:00:00 to 2013-04-06 00:00:00
        assertEquals(0.42857142,
                roleFactorOf(ROLE_KEY2, PERIOD_VALUE_KEY1, rolePricingData),
                0.000001);
        // From 2013-04-06 00:00:00 to 2013-04-08 00:00:00
        assertEquals(0.28571428,
                roleFactorOf(ROLE_KEY3, PERIOD_VALUE_KEY1, rolePricingData),
                0.000001);
    }

    @Test
    public void computeParameterUserFactorForOneTimeSliceAndOneUser_OneRolesForOneUserAssignmentForTwoPeriodValues() {
        // given
        addParameterPeriodValue(PERIOD_VALUE_KEY2, "B",
                DateTimeHandling.calculateMillis("2013-04-04 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-08 00:00:00"));

        addParameterPeriodValue(PERIOD_VALUE_KEY1, "A",
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-04 00:00:00"));

        List<UserRoleAssignment> roleAssignments = new ArrayList<UserRoleAssignment>();
        roleAssignments.add(new UserRoleAssignment(Long.valueOf(ROLE_KEY1),
                DateTimeHandling.calculateMillis("2013-04-03 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-05 00:00:00")));

        addUserAssignmentWithRole(USER_KEY,
                DateTimeHandling.calculateMillis("2013-04-03 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-05 00:00:00"),
                roleAssignments);

        // when
        calculator.computeParameterUserFactorForOneTimeSliceAndOneUser(
                timeSlice, periodValues, userAssignments);

        // then
        assertEquals(2, periodValues.size());
        // From 2013-04-04 00:00:00 to 2013-04-08 00:00:00
        assertEquals(PERIOD_VALUE_KEY2, periodValues.get(0).getKey()
                .longValue());
        assertEquals(0.57142857, periodValues.get(0).getUserAssignmentFactor(),
                0.000001);
        RolePricingData rolePricingData1 = periodValues.get(0).getRolePrices();
        assertEquals(0.57142857,
                roleFactorOf(ROLE_KEY1, PERIOD_VALUE_KEY2, rolePricingData1),
                0.000001);

        // From 2013-04-01 00:00:00 to 2013-04-04 00:00:00
        assertEquals(PERIOD_VALUE_KEY1, periodValues.get(1).getKey()
                .longValue());
        assertEquals(0.42857142, periodValues.get(1).getUserAssignmentFactor(),
                0.000001);
        RolePricingData rolePricingData2 = periodValues.get(1).getRolePrices();
        assertEquals(0.42857142,
                roleFactorOf(ROLE_KEY1, PERIOD_VALUE_KEY1, rolePricingData2),
                0.000001);

    }

    @Test
    public void computeParameterUserFactorForOneTimeSliceAndOneUser_TwoRolesForOneUserAssignmentForOnlyOneOfPeriodValues() {
        // given
        addParameterPeriodValue(PERIOD_VALUE_KEY3, "C",
                DateTimeHandling.calculateMillis("2013-04-06 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-08 00:00:00"));

        addParameterPeriodValue(PERIOD_VALUE_KEY2, "B",
                DateTimeHandling.calculateMillis("2013-04-02 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-06 00:00:00"));

        addParameterPeriodValue(PERIOD_VALUE_KEY1, "A",
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-02 00:00:00"));

        List<UserRoleAssignment> roleAssignments = new ArrayList<UserRoleAssignment>();
        roleAssignments.add(new UserRoleAssignment(Long.valueOf(ROLE_KEY2),
                DateTimeHandling.calculateMillis("2013-04-04 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-05 00:00:00")));
        roleAssignments.add(new UserRoleAssignment(Long.valueOf(ROLE_KEY1),
                DateTimeHandling.calculateMillis("2013-04-03 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-04 00:00:00")));

        addUserAssignmentWithRole(USER_KEY,
                DateTimeHandling.calculateMillis("2013-04-03 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-05 00:00:00"),
                roleAssignments);
        // when
        calculator.computeParameterUserFactorForOneTimeSliceAndOneUser(
                timeSlice, periodValues, userAssignments);

        // then
        assertEquals(3, periodValues.size());

        XParameterPeriodValue periodValue1 = periodValues.get(0);
        assertEquals(PERIOD_VALUE_KEY3, periodValue1.getKey().longValue());
        assertEquals(0, periodValue1.getUserAssignmentFactor(), 0.000001);
        RolePricingData rolePricingData1 = periodValue1.getRolePrices();
        assertEquals(0,
                roleFactorOf(ROLE_KEY1, PERIOD_VALUE_KEY3, rolePricingData1),
                0.000001);
        assertEquals(0,
                roleFactorOf(ROLE_KEY2, PERIOD_VALUE_KEY3, rolePricingData1),
                0.000001);

        XParameterPeriodValue periodValue2 = periodValues.get(1);
        assertEquals(PERIOD_VALUE_KEY2, periodValue2.getKey().longValue());
        assertEquals(1, periodValue2.getUserAssignmentFactor(), 0.000001);
        RolePricingData rolePricingData2 = periodValue2.getRolePrices();
        // From 2013-04-01 00:00:00 to 2013-04-04 00:00:00
        assertEquals(0.42857142,
                roleFactorOf(ROLE_KEY1, PERIOD_VALUE_KEY2, rolePricingData2),
                0.000001);
        // From 2013-04-04 00:00:00 to 2013-04-08 00:00:00
        assertEquals(0.57142857,
                roleFactorOf(ROLE_KEY2, PERIOD_VALUE_KEY2, rolePricingData2),
                0.000001);

        XParameterPeriodValue periodValue3 = periodValues.get(2);
        assertEquals(PERIOD_VALUE_KEY1, periodValue3.getKey().longValue());
        assertEquals(0, periodValue3.getUserAssignmentFactor(), 0.000001);
        RolePricingData rolePricingData3 = periodValue3.getRolePrices();
        assertEquals(0,
                roleFactorOf(ROLE_KEY1, PERIOD_VALUE_KEY1, rolePricingData3),
                0.000001);
        assertEquals(0,
                roleFactorOf(ROLE_KEY2, PERIOD_VALUE_KEY1, rolePricingData3),
                0.000001);

    }

    @Test
    public void computeParameterUserFactorForOneTimeSliceAndOneUser_MultipleRolesAndUserAssignmentsForMultiplePeriodValues() {
        // given
        addParameterPeriodValue(PERIOD_VALUE_KEY3, "C",
                DateTimeHandling.calculateMillis("2013-04-06 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-08 00:00:00"));

        addParameterPeriodValue(PERIOD_VALUE_KEY2, "B",
                DateTimeHandling.calculateMillis("2013-04-03 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-06 00:00:00"));

        addParameterPeriodValue(PERIOD_VALUE_KEY1, "A",
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-03 00:00:00"));

        List<UserRoleAssignment> roleAssignments1 = new ArrayList<UserRoleAssignment>();
        roleAssignments1.add(new UserRoleAssignment(Long.valueOf(ROLE_KEY1),
                DateTimeHandling.calculateMillis("2013-04-07 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-07 12:00:00")));
        addUserAssignmentWithRole(USER_KEY,
                DateTimeHandling.calculateMillis("2013-04-07 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-07 12:00:00"),
                roleAssignments1);

        List<UserRoleAssignment> roleAssignments2 = new ArrayList<UserRoleAssignment>();
        roleAssignments1.add(new UserRoleAssignment(Long.valueOf(ROLE_KEY3),
                DateTimeHandling.calculateMillis("2013-04-05 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-05 12:00:00")));
        addUserAssignmentWithRole(USER_KEY,
                DateTimeHandling.calculateMillis("2013-04-05 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-05 12:00:00"),
                roleAssignments2);

        List<UserRoleAssignment> roleAssignments3 = new ArrayList<UserRoleAssignment>();
        roleAssignments1.add(new UserRoleAssignment(Long.valueOf(ROLE_KEY2),
                DateTimeHandling.calculateMillis("2013-04-02 12:00:00"),
                DateTimeHandling.calculateMillis("2013-04-04 00:00:00")));
        roleAssignments1.add(new UserRoleAssignment(Long.valueOf(ROLE_KEY1),
                DateTimeHandling.calculateMillis("2013-04-02 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-02 12:00:00")));
        addUserAssignmentWithRole(USER_KEY,
                DateTimeHandling.calculateMillis("2013-04-02 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-04 00:00:00"),
                roleAssignments3);

        // when
        calculator.computeParameterUserFactorForOneTimeSliceAndOneUser(
                timeSlice, periodValues, userAssignments);

        // then
        assertEquals(3, periodValues.size());

        XParameterPeriodValue periodValue1 = periodValues.get(0);
        assertEquals(PERIOD_VALUE_KEY3, periodValue1.getKey().longValue());
        // From 2013-04-07 00:00:00 to 2013-04-08 00:00:00
        assertEquals(0.14285714, periodValue1.getUserAssignmentFactor(),
                0.000001);
        RolePricingData rolePricingData1 = periodValue1.getRolePrices();
        // From 2013-04-07 00:00:00 to 2013-04-08 00:00:00
        assertEquals(0.14285714,
                roleFactorOf(ROLE_KEY1, PERIOD_VALUE_KEY3, rolePricingData1),
                0.000001);
        assertEquals(0,
                roleFactorOf(ROLE_KEY2, PERIOD_VALUE_KEY3, rolePricingData1),
                0.000001);
        assertEquals(0,
                roleFactorOf(ROLE_KEY3, PERIOD_VALUE_KEY3, rolePricingData1),
                0.000001);

        XParameterPeriodValue periodValue2 = periodValues.get(1);
        assertEquals(PERIOD_VALUE_KEY2, periodValue2.getKey().longValue());
        // From 2013-04-03 00:00:00 to 2013-04-07 00:00:00
        assertEquals(0.57142857, periodValue2.getUserAssignmentFactor(),
                0.000001);
        RolePricingData rolePricingData2 = periodValue2.getRolePrices();
        assertEquals(0,
                roleFactorOf(ROLE_KEY1, PERIOD_VALUE_KEY2, rolePricingData2),
                0.000001);
        // From 2013-04-03 00:00:00 to 2013-04-05 00:00:00
        assertEquals(0.28571428,
                roleFactorOf(ROLE_KEY2, PERIOD_VALUE_KEY2, rolePricingData2),
                0.000001);
        // From 2013-04-05 00:00:00 to 2013-04-07 00:00:00
        assertEquals(0.28571428,
                roleFactorOf(ROLE_KEY3, PERIOD_VALUE_KEY2, rolePricingData2),
                0.000001);

        XParameterPeriodValue periodValue3 = periodValues.get(2);
        assertEquals(PERIOD_VALUE_KEY1, periodValue3.getKey().longValue());
        // From 2013-04-01 00:00:00 to 2013-04-03 00:00:00
        assertEquals(0.28571428, periodValue3.getUserAssignmentFactor(),
                0.000001);
        RolePricingData rolePricingData3 = periodValue3.getRolePrices();
        // From 2013-04-01 00:00:00 to 2013-04-02 12:00:00
        assertEquals(0.21428571,
                roleFactorOf(ROLE_KEY1, PERIOD_VALUE_KEY1, rolePricingData3),
                0.000001);
        // From 2013-04-02 12:00:00 to 2013-04-03 00:00:00
        assertEquals(0.07142857,
                roleFactorOf(ROLE_KEY2, PERIOD_VALUE_KEY1, rolePricingData3),
                0.000001);
        assertEquals(0,
                roleFactorOf(ROLE_KEY3, PERIOD_VALUE_KEY1, rolePricingData3),
                0.000001);

    }

    private double roleFactorOf(long roleKey, long periodValueKey,
            RolePricingData rolePricingData) {
        Map<Long, RolePricingDetails> rolePrices = rolePricingData
                .getRolePricesForContainerKey(Long.valueOf(periodValueKey));
        RolePricingDetails pricingDetails = rolePrices.get(Long
                .valueOf(roleKey));
        return pricingDetails.getFactor();
    }

    private void addParameterPeriodValue(long periodValueKey,
            String parameterValue, long startTime, long endTime) {
        XParameterPeriodPrimitiveType periodValue = new XParameterPeriodPrimitiveType(
                idData, createPricingDataForRoleKeys(periodValueKey), null);
        periodValue.setKey(Long.valueOf(periodValueKey));
        periodValue.setValue(parameterValue);
        periodValue.setStartTime(startTime);
        periodValue.setEndTime(endTime);
        periodValues.add(periodValue);
    }

    private void addUserAssignmentWithRole(long userKey, long startTime,
            long endTime, List<UserRoleAssignment> roles) {
        UserAssignment userAssignment = new UserAssignment();
        userAssignment.setUserKey(userKey);
        userAssignment.setUsageStartTime(startTime);
        userAssignment.setUsageEndTime(endTime);
        if (roles != null) {
            userAssignment.setRoleAssignments(roles);
        }
        userAssignments.add(userAssignment);
    }

    private RolePricingData createPricingDataForRoleKeys(long periodValueKey) {
        Map<Long, RolePricingDetails> rolePricingDetails = new HashMap<Long, RolePricingDetails>();
        rolePricingDetails.put(Long.valueOf(ROLE_KEY1),
                new RolePricingDetails());
        rolePricingDetails.put(Long.valueOf(ROLE_KEY2),
                new RolePricingDetails());
        rolePricingDetails.put(Long.valueOf(ROLE_KEY3),
                new RolePricingDetails());

        RolePricingData pricingData = new RolePricingData();
        pricingData.addRolePricesForContainerKey(Long.valueOf(periodValueKey),
                rolePricingDetails);
        return pricingData;
    }

}
