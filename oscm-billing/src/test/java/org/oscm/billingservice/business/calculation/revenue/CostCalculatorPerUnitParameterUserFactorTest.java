/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: May 27, 2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.calculation.revenue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.LinkedList;

import org.junit.Before;
import org.junit.Test;

import org.oscm.billingservice.business.calculation.revenue.model.TimeSlice;
import org.oscm.billingservice.business.calculation.revenue.model.UserAssignment;
import org.oscm.billingservice.dao.model.XParameterData;
import org.oscm.billingservice.dao.model.XParameterIdData;
import org.oscm.billingservice.dao.model.XParameterPeriodPrimitiveType;
import org.oscm.billingservice.dao.model.XParameterPeriodValue;
import org.oscm.test.DateTimeHandling;
import org.oscm.internal.types.enumtypes.ParameterType;
import org.oscm.internal.types.enumtypes.ParameterValueType;
import org.oscm.internal.types.enumtypes.PricingPeriod;

/**
 * @author tokoda
 * 
 */
public class CostCalculatorPerUnitParameterUserFactorTest {

    private static final long PERIOD_VALUE_KEY1 = 10000;
    private static final long PERIOD_VALUE_KEY2 = 10001;

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
                ParameterType.PLATFORM_PARAMETER, ParameterValueType.BOOLEAN);

        periodValues = new LinkedList<XParameterPeriodValue>();
        userAssignments = new ArrayList<UserAssignment>();

    }

    @Test
    public void computeParameterUserFactorForOneTimeSliceAndOneUser_EmptyInputs() {
        // given

        // when
        calculator.computeParameterUserFactorForOneTimeSliceAndOneUser(
                timeSlice, periodValues, userAssignments);

        // then
        assertTrue(periodValues.isEmpty());

    }

    @Test
    public void computeParameterUserFactorForOneTimeSliceAndOneUser_EmptyUserAssignments() {
        // given
        addParameterPeriodValue(PERIOD_VALUE_KEY1, "A",
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-08 00:00:00"));

        // when
        calculator.computeParameterUserFactorForOneTimeSliceAndOneUser(
                timeSlice, periodValues, userAssignments);

        // then
        assertEquals(1, periodValues.size());
        assertEquals(PERIOD_VALUE_KEY1, periodValues.get(0).getKey()
                .longValue());
        assertEquals(0, periodValues.get(0).getUserAssignmentFactor(), 0.000001);

        assertNull(periodValues.get(0).getRolePrices());

    }

    @Test
    public void computeParameterUserFactorForOneTimeSliceAndOneUser_UserExt_ParNot() {
        // given
        addParameterPeriodValue(PERIOD_VALUE_KEY1, "A",
                DateTimeHandling.calculateMillis("2013-03-31 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"));

        addUserAssignment(20000,
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-08 00:00:00"));

        // when
        calculator.computeParameterUserFactorForOneTimeSliceAndOneUser(
                timeSlice, periodValues, userAssignments);

        // then
        assertEquals(1, periodValues.size());
        assertEquals(PERIOD_VALUE_KEY1, periodValues.get(0).getKey()
                .longValue());
        assertEquals(1, periodValues.get(0).getUserAssignmentFactor(), 0.000001);

        assertNull(periodValues.get(0).getRolePrices());

    }

    @Test
    public void computeParameterUserFactorForOneTimeSliceAndOneUser_OneUserAssignmentForOnePeriodValue() {
        // given
        addParameterPeriodValue(PERIOD_VALUE_KEY1, "A",
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-08 00:00:00"));

        addUserAssignment(20000,
                DateTimeHandling.calculateMillis("2013-04-01 01:00:00"),
                DateTimeHandling.calculateMillis("2013-04-01 02:00:00"));

        // when
        calculator.computeParameterUserFactorForOneTimeSliceAndOneUser(
                timeSlice, periodValues, userAssignments);

        // then
        assertEquals(1, periodValues.size());
        assertEquals(PERIOD_VALUE_KEY1, periodValues.get(0).getKey()
                .longValue());
        assertEquals(1, periodValues.get(0).getUserAssignmentFactor(), 0.000001);
    }

    @Test
    public void computeParameterUserFactorForOneTimeSliceAndOneUser_TwoUserAssignmentsForOnePeriodValue() {
        // given
        addParameterPeriodValue(PERIOD_VALUE_KEY1, "A",
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-08 00:00:00"));

        addUserAssignment(20000,
                DateTimeHandling.calculateMillis("2013-04-02 01:00:00"),
                DateTimeHandling.calculateMillis("2013-04-02 02:00:00"));

        addUserAssignment(20000,
                DateTimeHandling.calculateMillis("2013-04-01 01:00:00"),
                DateTimeHandling.calculateMillis("2013-04-01 02:00:00"));

        // when
        calculator.computeParameterUserFactorForOneTimeSliceAndOneUser(
                timeSlice, periodValues, userAssignments);

        // then
        assertEquals(1, periodValues.size());
        assertEquals(PERIOD_VALUE_KEY1, periodValues.get(0).getKey()
                .longValue());
        assertEquals(1, periodValues.get(0).getUserAssignmentFactor(), 0.000001);
    }

    @Test
    public void computeParameterUserFactorForOneTimeSliceAndOneUser_OneUserAssignmentForTwoPeriodValues() {
        // given
        addParameterPeriodValue(PERIOD_VALUE_KEY2, "B",
                DateTimeHandling.calculateMillis("2013-04-04 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-08 00:00:00"));

        addParameterPeriodValue(PERIOD_VALUE_KEY1, "A",
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-04 00:00:00"));

        addUserAssignment(20000,
                DateTimeHandling.calculateMillis("2013-04-02 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-07 00:00:00"));

        // when
        calculator.computeParameterUserFactorForOneTimeSliceAndOneUser(
                timeSlice, periodValues, userAssignments);

        // then
        assertEquals(2, periodValues.size());
        assertEquals(PERIOD_VALUE_KEY2, periodValues.get(0).getKey()
                .longValue());
        assertEquals(0.57142857, periodValues.get(0).getUserAssignmentFactor(),
                0.000001);
        assertEquals(PERIOD_VALUE_KEY1, periodValues.get(1).getKey()
                .longValue());
        assertEquals(0.42857142, periodValues.get(1).getUserAssignmentFactor(),
                0.000001);
    }

    @Test
    public void computeParameterUserFactorForOneTimeSliceAndOneUser_FirstPeriodValueHasNoUserAssignment() {
        // given
        addParameterPeriodValue(PERIOD_VALUE_KEY2, "B",
                DateTimeHandling.calculateMillis("2013-04-04 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-08 00:00:00"));

        addParameterPeriodValue(PERIOD_VALUE_KEY1, "A",
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-04 00:00:00"));

        addUserAssignment(20000,
                DateTimeHandling.calculateMillis("2013-04-05 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-06 00:00:00"));

        // when
        calculator.computeParameterUserFactorForOneTimeSliceAndOneUser(
                timeSlice, periodValues, userAssignments);

        // then
        assertEquals(2, periodValues.size());
        assertEquals(PERIOD_VALUE_KEY2, periodValues.get(0).getKey()
                .longValue());
        assertEquals(1, periodValues.get(0).getUserAssignmentFactor(), 0.000001);
        assertEquals(PERIOD_VALUE_KEY1, periodValues.get(1).getKey()
                .longValue());
        assertEquals(0, periodValues.get(1).getUserAssignmentFactor(), 0.000001);
    }

    @Test
    public void computeParameterUserFactorForOneTimeSliceAndOneUser_SecondPeriodValueHasNoUserAssignment() {
        // given
        addParameterPeriodValue(10001, "B",
                DateTimeHandling.calculateMillis("2013-04-04 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-08 00:00:00"));

        addParameterPeriodValue(PERIOD_VALUE_KEY1, "A",
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-04 00:00:00"));

        addUserAssignment(20000,
                DateTimeHandling.calculateMillis("2013-04-02 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-03 00:00:00"));

        // when
        calculator.computeParameterUserFactorForOneTimeSliceAndOneUser(
                timeSlice, periodValues, userAssignments);

        // then
        assertEquals(2, periodValues.size());
        assertEquals(10001, periodValues.get(0).getKey().longValue());
        assertEquals(0, periodValues.get(0).getUserAssignmentFactor(), 0.000001);
        assertEquals(PERIOD_VALUE_KEY1, periodValues.get(1).getKey()
                .longValue());
        assertEquals(1, periodValues.get(1).getUserAssignmentFactor(), 0.000001);
    }

    @Test
    public void computeParameterUserFactorForOneTimeSliceAndOneUser_TwoUserAssignmentsForTwoPeriodValues() {
        // given
        addParameterPeriodValue(PERIOD_VALUE_KEY2, "B",
                DateTimeHandling.calculateMillis("2013-04-04 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-08 00:00:00"));

        addParameterPeriodValue(PERIOD_VALUE_KEY1, "A",
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-04 00:00:00"));

        addUserAssignment(20000,
                DateTimeHandling.calculateMillis("2013-04-06 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-07 00:00:00"));

        addUserAssignment(20000,
                DateTimeHandling.calculateMillis("2013-04-02 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-03 00:00:00"));

        // when
        calculator.computeParameterUserFactorForOneTimeSliceAndOneUser(
                timeSlice, periodValues, userAssignments);

        // then
        assertEquals(2, periodValues.size());

        // From 2013-04-06 00:00:00 to 2013-04-08 00:00:00
        assertEquals(PERIOD_VALUE_KEY2, periodValues.get(0).getKey()
                .longValue());
        assertEquals(0.28571428, periodValues.get(0).getUserAssignmentFactor(),
                0.000001);

        // From 2013-04-01 00:00:00 to 2013-04-06 00:00:00
        assertEquals(PERIOD_VALUE_KEY1, periodValues.get(1).getKey()
                .longValue());
        assertEquals(0.71428571, periodValues.get(1).getUserAssignmentFactor(),
                0.000001);
    }

    private void addParameterPeriodValue(long key, String value,
            long startTime, long endTime) {
        XParameterPeriodPrimitiveType periodValue = new XParameterPeriodPrimitiveType(
                idData, null, null);
        periodValue.setKey(Long.valueOf(key));
        periodValue.setValue(value);
        periodValue.setStartTime(startTime);
        periodValue.setEndTime(endTime);
        periodValues.add(periodValue);
    }

    private void addUserAssignment(long key, long startTime, long endTime) {
        UserAssignment userAssignment = new UserAssignment();
        userAssignment.setUserKey(key);
        userAssignment.setUsageStartTime(startTime);
        userAssignment.setUsageEndTime(endTime);
        userAssignments.add(userAssignment);
    }
}
