/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: May 14, 2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.calculation.revenue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.oscm.billingservice.business.calculation.revenue.model.TimeSlice;
import org.oscm.billingservice.dao.model.XParameterData;
import org.oscm.billingservice.dao.model.XParameterIdData;
import org.oscm.billingservice.dao.model.XParameterPeriodPrimitiveType;
import org.oscm.billingservice.dao.model.XParameterPeriodValue;
import org.oscm.billingservice.service.model.BillingInput;
import org.oscm.test.DateTimeHandling;
import org.oscm.internal.types.enumtypes.ParameterType;
import org.oscm.internal.types.enumtypes.ParameterValueType;
import org.oscm.internal.types.enumtypes.PricingPeriod;

/**
 * @author tokoda
 * 
 */
public class CostCalculatorPerUnitTimeSliceTest {

    private static final String VALUE_1 = "VALUE_1";
    private static final String VALUE_2 = "VALUE_2";
    private static final String VALUE_3 = "VALUE_3";

    private CostCalculatorPerUnit calculator;

    private TimeSlice timeSlice;

    private XParameterIdData parentOfPeriodValue;

    private List<XParameterPeriodValue> periodValues;

    private BillingInput.Builder input;

    @Before
    public void setup() {
        calculator = new CostCalculatorPerUnit();
        timeSlice = new TimeSlice(
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-05-01 00:00:00"),
                PricingPeriod.MONTH);
        parentOfPeriodValue = createParemeterIdData("PID");
        periodValues = new ArrayList<>();
        input = new BillingInput.Builder();
    }

    private XParameterIdData createParemeterIdData(String parameterId) {
        XParameterData data = new XParameterData();
        return data.getIdDataInstance(parameterId,
                ParameterType.PLATFORM_PARAMETER, ParameterValueType.BOOLEAN);
    }

    @Test(expected = NullPointerException.class)
    public void retrieveParametersForTimeSlice_NullPeriodValues() {
        // given
        // when
        calculator.retrieveParametersForTimeSlice(null, timeSlice);
        // then
    }

    @Test
    public void retrieveParametersForTimeSlice_EmptyPeriodValues() {
        // given
        // when
        LinkedList<XParameterPeriodValue> result = calculator
                .retrieveParametersForTimeSlice(periodValues, timeSlice);
        // then
        assertTrue(result.isEmpty());
    }

    @Test
    public void retrieveParametersForTimeSlice_PeriodValueBeforeTimeSlice() {
        // given
        createPriodValue(
                DateTimeHandling.calculateMillis("2013-03-31 23:59:58"),
                DateTimeHandling.calculateMillis("2013-03-31 23:59:59"),
                VALUE_1);

        // when
        LinkedList<XParameterPeriodValue> result = calculator
                .retrieveParametersForTimeSlice(periodValues, timeSlice);
        // then
        assertEquals(1, result.size());
        assertEquals(VALUE_1, result.get(0).getValue());
    }

    @Test
    public void retrieveParametersForTimeSlice_PeriodValueOnTimeSliceStart() {
        // given
        createPriodValue(
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                VALUE_1);

        // when
        LinkedList<XParameterPeriodValue> result = calculator
                .retrieveParametersForTimeSlice(periodValues, timeSlice);
        // then
        assertEquals(1, result.size());
        assertEquals(VALUE_1, result.get(0).getValue());
    }

    @Test
    public void retrieveParametersForTimeSlice_PeriodValueOnTimeSliceEnd() {
        // given
        createPriodValue(
                DateTimeHandling.calculateMillis("2013-05-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-05-01 00:00:01"),
                VALUE_1);

        // when
        LinkedList<XParameterPeriodValue> result = calculator
                .retrieveParametersForTimeSlice(periodValues, timeSlice);
        // then
        assertEquals(1, result.size());
        assertEquals(VALUE_1, result.get(0).getValue());
    }

    @Test
    public void retrieveParametersForTimeSlice_PeriodValueAfterTimeSlice() {
        // given
        createPriodValue(
                DateTimeHandling.calculateMillis("2013-05-01 00:00:01"),
                DateTimeHandling.calculateMillis("2013-05-01 00:00:02"),
                VALUE_1);

        // when
        LinkedList<XParameterPeriodValue> result = calculator
                .retrieveParametersForTimeSlice(periodValues, timeSlice);
        // then
        assertTrue(result.isEmpty());
    }

    @Test
    public void retrieveParametersForTimeSlice_PeriodValuesBeforeAndWithinTimeSlice() {
        // given
        createPriodValue(
                DateTimeHandling.calculateMillis("2013-03-31 23:59:59"),
                DateTimeHandling.calculateMillis("2013-04-01 00:00:01"),
                VALUE_2);

        createPriodValue(
                DateTimeHandling.calculateMillis("2013-03-31 23:59:58"),
                DateTimeHandling.calculateMillis("2013-03-31 23:59:59"),
                VALUE_1);

        // when
        LinkedList<XParameterPeriodValue> result = calculator
                .retrieveParametersForTimeSlice(periodValues, timeSlice);
        // then
        assertEquals(1, result.size());
        assertEquals(VALUE_2, result.get(0).getValue());
    }

    @Test
    public void retrieveParametersForTimeSlice_PeriodValuesOnTimeSliceStartAndWithinTimeSlice() {
        // given
        createPriodValue(
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-01 00:00:01"),
                VALUE_2);

        createPriodValue(
                DateTimeHandling.calculateMillis("2013-03-31 23:59:59"),
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                VALUE_1);

        // when
        LinkedList<XParameterPeriodValue> result = calculator
                .retrieveParametersForTimeSlice(periodValues, timeSlice);
        // then
        assertEquals(2, result.size());
        assertEquals(VALUE_2, result.get(0).getValue());
        assertEquals(VALUE_1, result.get(1).getValue());
    }

    @Test
    public void retrieveParametersForTimeSlice_PeriodValuesWithinTimeSliceAndOnTimeSliceEnd() {
        // given
        createPriodValue(
                DateTimeHandling.calculateMillis("2013-05-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-05-01 00:00:01"),
                VALUE_2);

        createPriodValue(
                DateTimeHandling.calculateMillis("2013-04-30 23:59:59"),
                DateTimeHandling.calculateMillis("2013-05-01 00:00:00"),
                VALUE_1);

        // when
        LinkedList<XParameterPeriodValue> result = calculator
                .retrieveParametersForTimeSlice(periodValues, timeSlice);
        // then
        assertEquals(2, result.size());
        assertEquals(VALUE_2, result.get(0).getValue());
        assertEquals(VALUE_1, result.get(1).getValue());
    }

    @Test
    public void retrieveParametersForTimeSlice_PeriodValuesWithinAndAfterTimeSlice() {
        // given
        createPriodValue(
                DateTimeHandling.calculateMillis("2013-05-01 00:00:01"),
                DateTimeHandling.calculateMillis("2013-05-01 00:00:02"),
                VALUE_2);

        createPriodValue(
                DateTimeHandling.calculateMillis("2013-05-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-05-01 00:00:01"),
                VALUE_1);

        // when
        LinkedList<XParameterPeriodValue> result = calculator
                .retrieveParametersForTimeSlice(periodValues, timeSlice);
        // then
        assertEquals(1, result.size());
        assertEquals(VALUE_1, result.get(0).getValue());
    }

    @Test
    public void retrieveParametersForTimeSlice_MultiplePeriodValues() {
        // given
        createPriodValue(
                DateTimeHandling.calculateMillis("2013-05-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-05-01 00:00:01"),
                VALUE_3);

        createPriodValue(
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-05-01 00:00:00"),
                VALUE_2);

        createPriodValue(
                DateTimeHandling.calculateMillis("2013-03-31 23:59:59"),
                DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                VALUE_1);

        // when
        LinkedList<XParameterPeriodValue> result = calculator
                .retrieveParametersForTimeSlice(periodValues, timeSlice);
        // then
        assertEquals(3, result.size());
        assertEquals(VALUE_3, result.get(0).getValue());
        assertEquals(DateTimeHandling.calculateMillis("2013-05-01 00:00:00"),
                result.get(0).getStartTime());
        assertEquals(DateTimeHandling.calculateMillis("2013-05-01 00:00:01"),
                result.get(0).getEndTime());

        assertEquals(VALUE_2, result.get(1).getValue());
        assertEquals(DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                result.get(1).getStartTime());
        assertEquals(DateTimeHandling.calculateMillis("2013-05-01 00:00:00"),
                result.get(1).getEndTime());

        assertEquals(VALUE_1, result.get(2).getValue());
        assertEquals(DateTimeHandling.calculateMillis("2013-03-31 23:59:59"),
                result.get(2).getStartTime());
        assertEquals(DateTimeHandling.calculateMillis("2013-04-01 00:00:00"),
                result.get(2).getEndTime());
    }

    private void createPriodValue(long startTime, long endTime, String value) {
        XParameterPeriodPrimitiveType periodValue = new XParameterPeriodPrimitiveType(
                parentOfPeriodValue, null, null);
        periodValue.setStartTime(startTime);
        periodValue.setEndTime(endTime);
        periodValue.setValue(value);
        periodValues.add(periodValue);
    }

    @Test
    public void relevantTimeSlices_Month() {
        // given
        input.setBillingPeriodStart(DateTimeHandling
                .calculateMillis("2013-01-01 00:00:00"));
        input.setBillingPeriodEnd(DateTimeHandling
                .calculateMillis("2013-02-01 00:00:00"));

        // when
        List<TimeSlice> slices = calculator.relevantTimeSlices(input.build(),
                DateTimeHandling.calculateMillis("2013-01-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-02-01 00:00:00"),
                PricingPeriod.MONTH);

        // then
        assertEquals(1, slices.size());
        assertEquals(DateTimeHandling.calculateMillis("2013-01-01 00:00:00"),
                slices.get(0).getStart());
        assertEquals(
                DateTimeHandling.calculateMillis("2013-02-01 00:00:00") - 1,
                slices.get(0).getEnd());
    }

    @Test
    public void relevantTimeSlices_Week() {
        // given
        input.setBillingPeriodStart(DateTimeHandling
                .calculateMillis("2013-01-01 00:00:00"));
        input.setBillingPeriodEnd(DateTimeHandling
                .calculateMillis("2013-02-01 00:00:00"));

        // when
        List<TimeSlice> slices = calculator.relevantTimeSlices(input.build(),
                DateTimeHandling.calculateMillis("2013-01-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-02-01 00:00:00"),
                PricingPeriod.WEEK);

        // then
        assertEquals(4, slices.size());
        assertEquals(DateTimeHandling.calculateMillis("2013-01-21 00:00:00"),
                slices.get(0).getStart());
        assertEquals(
                DateTimeHandling.calculateMillis("2013-01-28 00:00:00") - 1,
                slices.get(0).getEnd());
        assertEquals(DateTimeHandling.calculateMillis("2012-12-31 00:00:00"),
                slices.get(3).getStart());
        assertEquals(
                DateTimeHandling.calculateMillis("2013-01-07 00:00:00") - 1,
                slices.get(3).getEnd());
    }

    @Test
    public void relevantTimeSlices_Day() {
        // given
        input.setBillingPeriodStart(DateTimeHandling
                .calculateMillis("2013-01-01 00:00:00"));
        input.setBillingPeriodEnd(DateTimeHandling
                .calculateMillis("2013-02-01 00:00:00"));

        // when
        List<TimeSlice> slices = calculator.relevantTimeSlices(input.build(),
                DateTimeHandling.calculateMillis("2013-01-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-02-01 00:00:00"),
                PricingPeriod.DAY);

        // then
        assertEquals(31, slices.size());
        assertEquals(DateTimeHandling.calculateMillis("2013-01-31 00:00:00"),
                slices.get(0).getStart());
        assertEquals(
                DateTimeHandling.calculateMillis("2013-02-01 00:00:00") - 1,
                slices.get(0).getEnd());
        assertEquals(DateTimeHandling.calculateMillis("2013-01-01 00:00:00"),
                slices.get(30).getStart());
        assertEquals(
                DateTimeHandling.calculateMillis("2013-01-02 00:00:00") - 1,
                slices.get(30).getEnd());
    }

    @Test
    public void relevantTimeSlices_Hour() {
        // given
        input.setBillingPeriodStart(DateTimeHandling
                .calculateMillis("2013-01-01 00:00:00"));
        input.setBillingPeriodEnd(DateTimeHandling
                .calculateMillis("2013-02-01 00:00:00"));

        // when
        List<TimeSlice> slices = calculator.relevantTimeSlices(input.build(),
                DateTimeHandling.calculateMillis("2013-01-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-02-01 00:00:00"),
                PricingPeriod.HOUR);

        // then
        assertEquals(744, slices.size());
        assertEquals(DateTimeHandling.calculateMillis("2013-01-31 23:00:00"),
                slices.get(0).getStart());
        assertEquals(
                DateTimeHandling.calculateMillis("2013-02-01 00:00:00") - 1,
                slices.get(0).getEnd());
        assertEquals(DateTimeHandling.calculateMillis("2013-01-01 00:00:00"),
                slices.get(743).getStart());
        assertEquals(
                DateTimeHandling.calculateMillis("2013-01-01 01:00:00") - 1,
                slices.get(743).getEnd());
    }

    @Test
    public void relevantTimeSlices_UsageLongerThanOneTimeSlice() {
        // given
        input.setBillingPeriodStart(DateTimeHandling
                .calculateMillis("2013-01-01 00:00:00"));
        input.setBillingPeriodEnd(DateTimeHandling
                .calculateMillis("2013-02-01 00:00:00"));

        // when
        List<TimeSlice> slices = calculator.relevantTimeSlices(input.build(),
                DateTimeHandling.calculateMillis("2013-01-06 23:59:59"),
                DateTimeHandling.calculateMillis("2013-01-14 00:00:00"),
                PricingPeriod.WEEK);

        // then
        assertEquals(3, slices.size());
        assertEquals(DateTimeHandling.calculateMillis("2013-01-14 00:00:00"),
                slices.get(0).getStart());
        assertEquals(
                DateTimeHandling.calculateMillis("2013-01-21 00:00:00") - 1,
                slices.get(0).getEnd());
        assertEquals(DateTimeHandling.calculateMillis("2013-01-07 00:00:00"),
                slices.get(1).getStart());
        assertEquals(
                DateTimeHandling.calculateMillis("2013-01-14 00:00:00") - 1,
                slices.get(1).getEnd());
        assertEquals(DateTimeHandling.calculateMillis("2012-12-31 00:00:00"),
                slices.get(2).getStart());
        assertEquals(
                DateTimeHandling.calculateMillis("2013-01-07 00:00:00") - 1,
                slices.get(2).getEnd());
    }

    @Test
    public void relevantTimeSlices_UsageJustOneTimeSlice() {
        // given
        input.setBillingPeriodStart(DateTimeHandling
                .calculateMillis("2013-01-01 00:00:00"));
        input.setBillingPeriodEnd(DateTimeHandling
                .calculateMillis("2013-02-01 00:00:00"));

        // when
        List<TimeSlice> slices = calculator.relevantTimeSlices(input.build(),
                DateTimeHandling.calculateMillis("2013-01-07 00:00:00"),
                DateTimeHandling.calculateMillis("2013-01-13 23:59:59"),
                PricingPeriod.WEEK);

        // then
        assertEquals(1, slices.size());
        assertEquals(DateTimeHandling.calculateMillis("2013-01-07 00:00:00"),
                slices.get(0).getStart());
        assertEquals(
                DateTimeHandling.calculateMillis("2013-01-14 00:00:00") - 1,
                slices.get(0).getEnd());
    }
}
