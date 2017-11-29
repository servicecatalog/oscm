/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 18.12.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.calculation.revenue;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

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
 * @author farmaki
 * 
 */
public class CostCalculatorPerUnitParameterPeriodFactorTest {

    // Java double has a precision of 15 significant digits.
    // The factors don't have more than 3 digits on the left of the comma.
    private final static double ASSERT_FACTOR_DOUBLE_DELTA = 0.000000000009D;

    private final CostCalculatorPerUnit calculator = new CostCalculatorPerUnit();
    private List<XParameterPeriodValue> parameterPeriodValues;
    private XParameterData parameterData;
    private long startTimeForPeriod;
    private long endTimeForPeriod;
    private BillingInput.Builder billingInput;

    @Before
    public void setup() {
        parameterData = new XParameterData();
        parameterPeriodValues = new ArrayList<XParameterPeriodValue>();
        startTimeForPeriod = DateTimeHandling
                .calculateMillis("2012-12-01 00:00:00");
        endTimeForPeriod = DateTimeHandling
                .calculateMillis("2013-01-01 00:00:00");
        billingInput = new BillingInput.Builder();
        billingInput.setBillingPeriodStart(startTimeForPeriod);
        billingInput.setBillingPeriodEnd(endTimeForPeriod);
        System.out.println("Start time = " + (new Date(startTimeForPeriod))
                + "\nEnd time = " + (new Date(endTimeForPeriod)));
    }

    @Test
    public void computeParameterPeriodFactor_hour_beforePeriod()
            throws Exception {
        // given
        parameterData.setPeriod(PricingPeriod.HOUR);
        endTimeForPeriod = DateTimeHandling
                .calculateMillis("2012-12-02 11:59:59");
        addParameterIdData("parameter1", 1, "value",
                DateTimeHandling.calculateMillis("2012-12-02 00:00:00"),
                DateTimeHandling.calculateMillis("2012-12-02 11:59:59"));
        addParameterIdData("parameter1", 0, "value",
                DateTimeHandling.calculateMillis("2012-11-28 00:00:00"),
                DateTimeHandling.calculateMillis("2012-12-02 00:00:00"));

        // when
        calculator.computeParameterPeriodFactor(billingInput.build(),
                parameterData, startTimeForPeriod, endTimeForPeriod);

        // then
        XParameterIdData data = parameterData.getIdData().iterator().next();
        List<XParameterPeriodValue> result = data.getPeriodValues();

        assertEquals(2, result.size());
        assertEquals("key 1", 1, result.get(0).getKey().longValue());
        assertEquals(12, result.get(0).getPeriodFactor(), 0);

        assertEquals("key 0", 0, result.get(1).getKey().longValue());
        assertEquals(24, result.get(1).getPeriodFactor(), 0);
    }

    @Test
    public void computeParameterPeriodFactor_hour_inPeriod() throws Exception {
        // given
        parameterData.setPeriod(PricingPeriod.HOUR);
        endTimeForPeriod = DateTimeHandling
                .calculateMillis("2012-12-02 11:59:59");
        addParameterIdData("parameter1", 1, "value",
                DateTimeHandling.calculateMillis("2012-12-02 00:00:00"),
                DateTimeHandling.calculateMillis("2013-01-01 00:00:00"));
        addParameterIdData("parameter1", 0, "value",
                DateTimeHandling.calculateMillis("2012-12-01 12:00:00"),
                DateTimeHandling.calculateMillis("2012-12-02 00:00:00"));

        // when
        calculator.computeParameterPeriodFactor(billingInput.build(),
                parameterData, startTimeForPeriod, endTimeForPeriod);

        // then
        XParameterIdData data = parameterData.getIdData().iterator().next();
        List<XParameterPeriodValue> result = data.getPeriodValues();

        assertEquals(2, result.size());

        assertEquals("key 1", 1, result.get(0).getKey().longValue());
        assertEquals(12, result.get(0).getPeriodFactor(), 0);

        assertEquals("key 0", 0, result.get(1).getKey().longValue());
        assertEquals(12, result.get(1).getPeriodFactor(), 0);
    }

    @Test
    public void computeParameterPeriodFactor_hour_onlyBeforePeriod()
            throws Exception {
        // given
        parameterData.setPeriod(PricingPeriod.HOUR);
        endTimeForPeriod = DateTimeHandling
                .calculateMillis("2012-12-02 11:59:59");
        addParameterIdData("parameter1", 0, "value",
                DateTimeHandling.calculateMillis("2012-11-28 00:00:00"),
                DateTimeHandling.calculateMillis("2013-01-01 00:00:00"));

        // when
        calculator.computeParameterPeriodFactor(billingInput.build(),
                parameterData, startTimeForPeriod, endTimeForPeriod);

        // then
        XParameterIdData data = parameterData.getIdData().iterator().next();
        List<XParameterPeriodValue> result = data.getPeriodValues();

        assertEquals(1, result.size());
        assertEquals("key 0", 0, result.get(0).getKey().longValue());
        assertEquals(36D, result.get(0).getPeriodFactor(), 0);
    }

    @Test
    public void computeParameterPeriodFactor_day_inPeriod() throws Exception {
        // given
        parameterData.setPeriod(PricingPeriod.DAY);
        addParameterIdData("parameter1", 4, "value",
                DateTimeHandling.calculateMillis("2012-12-20 23:59:59"),
                DateTimeHandling.calculateMillis("2013-01-01 00:00:00"));
        addParameterIdData("parameter1", 3, "value",
                DateTimeHandling.calculateMillis("2012-12-15 04:00:00"),
                DateTimeHandling.calculateMillis("2012-12-20 23:59:59"));
        addParameterIdData("parameter1", 2, "value",
                DateTimeHandling.calculateMillis("2012-12-08 04:00:00"),
                DateTimeHandling.calculateMillis("2012-12-15 04:00:00"));
        addParameterIdData("parameter1", 1, "value",
                DateTimeHandling.calculateMillis("2012-12-08 02:00:00"),
                DateTimeHandling.calculateMillis("2012-12-08 04:00:00"));
        addParameterIdData("parameter1", 0, "value",
                DateTimeHandling.calculateMillis("2012-12-08 01:00:00"),
                DateTimeHandling.calculateMillis("2012-12-08 02:00:00"));

        // when
        calculator.computeParameterPeriodFactor(billingInput.build(),
                parameterData, startTimeForPeriod, endTimeForPeriod);

        // then
        XParameterIdData data = parameterData.getIdData().iterator().next();
        List<XParameterPeriodValue> result = data.getPeriodValues();

        assertEquals(5, result.size());
        assertEquals("key 4", 4, result.get(0).getKey().longValue());
        assertEquals(11.0000115740741D, result.get(0).getPeriodFactor(),
                ASSERT_FACTOR_DOUBLE_DELTA);

        assertEquals("key 3", 3, result.get(1).getKey().longValue());
        assertEquals(5.8333217592593D, result.get(1).getPeriodFactor(),
                ASSERT_FACTOR_DOUBLE_DELTA);

        assertEquals("key 2", 2, result.get(2).getKey().longValue());
        assertEquals(7, result.get(2).getPeriodFactor(), 0);

        assertEquals("key 1", 1, result.get(3).getKey().longValue());
        assertEquals(0.083333333333333D, result.get(3).getPeriodFactor(),
                ASSERT_FACTOR_DOUBLE_DELTA);

        assertEquals("key 0", 0, result.get(4).getKey().longValue());
        assertEquals(0.083333333333333D, result.get(4).getPeriodFactor(),
                ASSERT_FACTOR_DOUBLE_DELTA);
    }

    @Test
    public void computeParameterPeriodFactor_day_beforePeriod()
            throws Exception {
        // given
        parameterData.setPeriod(PricingPeriod.DAY);
        addParameterIdData("parameter1", 1, "value",
                DateTimeHandling.calculateMillis("2012-12-15 14:12:00"),
                DateTimeHandling.calculateMillis("2013-01-01 00:00:00"));
        addParameterIdData("parameter1", 0, "value",
                DateTimeHandling.calculateMillis("2012-11-28 01:00:00"),
                DateTimeHandling.calculateMillis("2012-12-15 14:12:00"));

        // when
        calculator.computeParameterPeriodFactor(billingInput.build(),
                parameterData, startTimeForPeriod, endTimeForPeriod);

        // then
        XParameterIdData data = parameterData.getIdData().iterator().next();
        List<XParameterPeriodValue> result = data.getPeriodValues();

        assertEquals(2, result.size());
        assertEquals("key 1", 1, result.get(0).getKey().longValue());
        assertEquals(16.4083333333333D, result.get(0).getPeriodFactor(),
                ASSERT_FACTOR_DOUBLE_DELTA);

        assertEquals("key 0", 0, result.get(1).getKey().longValue());
        assertEquals(14.5916666666667D, result.get(1).getPeriodFactor(),
                ASSERT_FACTOR_DOUBLE_DELTA);
    }

    @Test
    public void computeParameterPeriodFactor_day_onlyBeforePeriod()
            throws Exception {
        // given
        parameterData.setPeriod(PricingPeriod.DAY);
        addParameterIdData("parameter1", 0, "value",
                DateTimeHandling.calculateMillis("2012-11-15 14:12:00"),
                DateTimeHandling.calculateMillis("2013-01-01 00:00:00"));

        // when
        calculator.computeParameterPeriodFactor(billingInput.build(),
                parameterData, startTimeForPeriod, endTimeForPeriod);

        // then
        XParameterIdData data = parameterData.getIdData().iterator().next();
        List<XParameterPeriodValue> result = data.getPeriodValues();

        // then
        assertEquals(0, result.get(0).getKey().longValue());
        assertEquals(31D, result.get(0).getPeriodFactor(), 0);
    }

    @Test
    public void computeParameterPeriodFactor_week_inPeriod() throws Exception {
        // given
        parameterData.setPeriod(PricingPeriod.WEEK);
        addParameterIdData("parameter1", 3, "18",
                DateTimeHandling.calculateMillis("2012-12-18 23:59:59"),
                DateTimeHandling.calculateMillis("2013-01-01 00:00:00"));
        addParameterIdData("parameter1", 2, "11",
                DateTimeHandling.calculateMillis("2012-12-11 04:00:00"),
                DateTimeHandling.calculateMillis("2012-12-18 23:59:59"));
        addParameterIdData("parameter1", 1, "5",
                DateTimeHandling.calculateMillis("2012-12-05 04:00:00"),
                DateTimeHandling.calculateMillis("2012-12-11 04:00:00"));
        addParameterIdData("parameter1", 0, "4",
                DateTimeHandling.calculateMillis("2012-12-04 00:00:00"),
                DateTimeHandling.calculateMillis("2012-12-05 00:00:00"));

        // when
        calculator.computeParameterPeriodFactor(billingInput.build(),
                parameterData, startTimeForPeriod, endTimeForPeriod);

        // then
        XParameterIdData data = parameterData.getIdData().iterator().next();
        List<XParameterPeriodValue> result = data.getPeriodValues();

        assertEquals(4, result.size());
        assertEquals("key 3", 3, result.get(0).getKey().longValue());
        assertEquals(1.71428736772487D, result.get(0).getPeriodFactor(),
                ASSERT_FACTOR_DOUBLE_DELTA);

        assertEquals("key 2", 2, result.get(1).getKey().longValue());
        assertEquals(1.11904596560847D, result.get(1).getPeriodFactor(),
                ASSERT_FACTOR_DOUBLE_DELTA);

        assertEquals("key 1", 1, result.get(2).getKey().longValue());
        assertEquals(0.857142857142857D, result.get(2).getPeriodFactor(),
                ASSERT_FACTOR_DOUBLE_DELTA);

        assertEquals("key 0", 0, result.get(3).getKey().longValue());
        assertEquals(0.285714285714286D, result.get(3).getPeriodFactor(),
                ASSERT_FACTOR_DOUBLE_DELTA);
    }

    @Test
    public void computeParameterPeriodFactor_week_beforePeriod()
            throws Exception {
        // given
        parameterData.setPeriod(PricingPeriod.WEEK);
        addParameterIdData("parameter1", 2, "30",
                DateTimeHandling.calculateMillis("2012-12-30 00:00:00"),
                DateTimeHandling.calculateMillis("2013-01-01 00:00:00"));
        addParameterIdData("parameter1", 1, "11",
                DateTimeHandling.calculateMillis("2012-12-11 00:00:00"),
                DateTimeHandling.calculateMillis("2012-12-30 00:00:00"));
        addParameterIdData("parameter1", 0, "28",
                DateTimeHandling.calculateMillis("2012-11-28 00:00:00"),
                DateTimeHandling.calculateMillis("2012-12-11 00:00:00"));

        // when
        calculator.computeParameterPeriodFactor(billingInput.build(),
                parameterData, startTimeForPeriod, endTimeForPeriod);

        // then
        XParameterIdData data = parameterData.getIdData().iterator().next();
        List<XParameterPeriodValue> result = data.getPeriodValues();

        assertEquals(3, result.size());

        assertEquals("key 2", 2, result.get(0).getKey().longValue());
        assertEquals(0.142857142857143D, result.get(0).getPeriodFactor(),
                ASSERT_FACTOR_DOUBLE_DELTA);

        assertEquals("key 1", 1, result.get(1).getKey().longValue());
        assertEquals(2.71428571428571D, result.get(1).getPeriodFactor(),
                ASSERT_FACTOR_DOUBLE_DELTA);

        assertEquals("key 0", 0, result.get(2).getKey().longValue());
        assertEquals(2.14285714285714D, result.get(2).getPeriodFactor(),
                ASSERT_FACTOR_DOUBLE_DELTA);
    }

    @Test
    public void computeParameterPeriodFactor_week_lastValueInOverlappingWeek()
            throws Exception {
        // given
        parameterData.setPeriod(PricingPeriod.WEEK);
        addParameterIdData("parameter1", 22, "30",
                DateTimeHandling.calculateMillis("2012-12-31 07:00:00"),
                DateTimeHandling.calculateMillis("2013-01-20 13:00:00"));
        addParameterIdData("parameter1", 22, "11",
                DateTimeHandling.calculateMillis("2012-12-17 00:00:00"),
                DateTimeHandling.calculateMillis("2012-12-31 07:00:00"));
        addParameterIdData("parameter1", 22, "28",
                DateTimeHandling.calculateMillis("2012-12-02 08:13:00"),
                DateTimeHandling.calculateMillis("2012-12-17 00:00:00"));

        // when
        calculator.computeParameterPeriodFactor(billingInput.build(),
                parameterData, startTimeForPeriod, endTimeForPeriod);

        // then
        XParameterIdData data = parameterData.getIdData().iterator().next();
        List<XParameterPeriodValue> result = data.getPeriodValues();

        // Value "30" starts in overlapping week, thus this value is charged in
        // the next billing period
        assertEquals(2, result.size());

        assertEquals("Value 11", "11", result.get(0).getValue());
        assertEquals(2.0, result.get(0).getPeriodFactor(),
                ASSERT_FACTOR_DOUBLE_DELTA);

        assertEquals("Value 28", "28", result.get(1).getValue());
        assertEquals(3.0, result.get(1).getPeriodFactor(),
                ASSERT_FACTOR_DOUBLE_DELTA);
    }

    @Test
    public void computeParameterPeriodFactor_week_onlyBeforePeriod()
            throws Exception {
        // given
        parameterData.setPeriod(PricingPeriod.WEEK);
        addParameterIdData("parameter1", 0, "value",
                DateTimeHandling.calculateMillis("2012-11-15 00:00:00"),
                DateTimeHandling.calculateMillis("2013-01-28 00:00:00"));

        // when
        calculator.computeParameterPeriodFactor(billingInput.build(),
                parameterData, startTimeForPeriod, endTimeForPeriod);

        // then
        XParameterIdData data = parameterData.getIdData().iterator().next();
        List<XParameterPeriodValue> result = data.getPeriodValues();

        assertEquals(0, result.get(0).getKey().longValue());
        assertEquals(5D, result.get(0).getPeriodFactor(), 0);
    }

    @Test
    public void computeParameterPeriodFactor_week_onlyBeforePeriod_multipleChanges()
            throws Exception {
        // given
        parameterData.setPeriod(PricingPeriod.WEEK);
        addParameterIdData("parameter1", 2, "value",
                DateTimeHandling.calculateMillis("2012-11-29 00:00:00"),
                DateTimeHandling.calculateMillis("2013-01-01 00:00:00"));
        addParameterIdData("parameter1", 1, "value",
                DateTimeHandling.calculateMillis("2012-11-27 00:00:00"),
                DateTimeHandling.calculateMillis("2012-11-28 23:59:59"));
        addParameterIdData("parameter1", 0, "value",
                DateTimeHandling.calculateMillis("2012-11-15 00:00:00"),
                DateTimeHandling.calculateMillis("2012-11-26 23:59:59"));

        // when
        calculator.computeParameterPeriodFactor(billingInput.build(),
                parameterData, startTimeForPeriod, endTimeForPeriod);

        // then
        XParameterIdData data = parameterData.getIdData().iterator().next();
        List<XParameterPeriodValue> result = data.getPeriodValues();

        assertEquals(2, result.get(0).getKey().longValue());
        assertEquals(4.57142857142857D, result.get(0).getPeriodFactor(),
                ASSERT_FACTOR_DOUBLE_DELTA);

        assertEquals(1, result.get(1).getKey().longValue());
        assertEquals(0.285712632275132D, result.get(1).getPeriodFactor(),
                ASSERT_FACTOR_DOUBLE_DELTA);

        assertEquals(0, result.get(2).getKey().longValue());
        assertEquals(0.142855489417989D, result.get(2).getPeriodFactor(),
                ASSERT_FACTOR_DOUBLE_DELTA);
    }

    @Test
    public void computeParameterPeriodFactor_week_startAndTerminationInPeriod()
            throws Exception {
        // given
        parameterData.setPeriod(PricingPeriod.WEEK);
        addParameterIdData("parameter1", 1, "value",
                DateTimeHandling.calculateMillis("2012-12-18 00:00:00"),
                DateTimeHandling.calculateMillis("2012-12-26 00:00:00"));
        addParameterIdData("parameter1", 0, "value",
                DateTimeHandling.calculateMillis("2012-12-02 00:00:00"),
                DateTimeHandling.calculateMillis("2012-12-17 23:59:59"));

        // when
        calculator.computeParameterPeriodFactor(billingInput.build(),
                parameterData, startTimeForPeriod, endTimeForPeriod);

        // then
        XParameterIdData data = parameterData.getIdData().iterator().next();
        List<XParameterPeriodValue> result = data.getPeriodValues();

        assertEquals(1, result.get(0).getKey().longValue());
        assertEquals(1.85714285714286D, result.get(0).getPeriodFactor(),
                ASSERT_FACTOR_DOUBLE_DELTA);

        assertEquals(0, result.get(1).getKey().longValue());
        assertEquals(3.14285548941799D, result.get(1).getPeriodFactor(),
                ASSERT_FACTOR_DOUBLE_DELTA);
    }

    @Test
    public void computeParameterPeriodFactor_month_withinPeriodOnly()
            throws Exception {
        // given
        parameterData.setPeriod(PricingPeriod.MONTH);
        addParameterIdData("parameter1", 0, "value",
                DateTimeHandling.calculateMillis("2012-12-04 00:00:00"),
                DateTimeHandling.calculateMillis("2013-01-10 00:00:00"));

        // when
        calculator.computeParameterPeriodFactor(billingInput.build(),
                parameterData, startTimeForPeriod, endTimeForPeriod);

        // then
        XParameterIdData data = parameterData.getIdData().iterator().next();
        List<XParameterPeriodValue> result = data.getPeriodValues();

        assertEquals(1, result.size());

        assertEquals("key 0", 0, result.get(0).getKey().longValue());
        assertEquals(1D, result.get(0).getPeriodFactor(), 0);
    }

    @Test
    public void computeParameterPeriodFactor_month_startedInPeriod()
            throws Exception {
        // given
        parameterData.setPeriod(PricingPeriod.MONTH);
        addParameterIdData("parameter1", 1, "value",
                DateTimeHandling.calculateMillis("2012-12-12 23:59:59.999"),
                DateTimeHandling.calculateMillis("2013-01-01 00:00:00"));
        addParameterIdData("parameter1", 0, "value",
                DateTimeHandling.calculateMillis("2012-12-04 04:00:00"),
                DateTimeHandling.calculateMillis("2012-12-12 23:59:59.999"));

        // when
        calculator.computeParameterPeriodFactor(billingInput.build(),
                parameterData, startTimeForPeriod, endTimeForPeriod);

        // then
        XParameterIdData data = parameterData.getIdData().iterator().next();
        List<XParameterPeriodValue> result = data.getPeriodValues();

        assertEquals(2, result.size());

        assertEquals("key 1", 1, result.get(0).getKey().longValue());
        assertEquals(0.612903599163680D, result.get(0).getPeriodFactor(),
                ASSERT_FACTOR_DOUBLE_DELTA);

        assertEquals("key 0", 0, result.get(1).getKey().longValue());
        assertEquals(0.387096400836320D, result.get(1).getPeriodFactor(),
                ASSERT_FACTOR_DOUBLE_DELTA);
    }

    @Test
    public void computeParameterPeriodFactor_month_beforePeriod()
            throws Exception {
        // given
        parameterData.setPeriod(PricingPeriod.MONTH);
        addParameterIdData("parameter1", 2, "value",
                DateTimeHandling.calculateMillis("2012-12-12 23:59:59.999"),
                DateTimeHandling.calculateMillis("2013-01-01 00:00:00"));
        addParameterIdData("parameter1", 1, "value",
                DateTimeHandling.calculateMillis("2012-12-04 04:00:00"),
                DateTimeHandling.calculateMillis("2012-12-12 23:59:59.999"));
        addParameterIdData("parameter1", 0, "value",
                DateTimeHandling.calculateMillis("2012-11-28 14:10:00"),
                DateTimeHandling.calculateMillis("2012-12-04 04:00:00"));

        // when
        calculator.computeParameterPeriodFactor(billingInput.build(),
                parameterData, startTimeForPeriod, endTimeForPeriod);

        // then
        XParameterIdData data = parameterData.getIdData().iterator().next();
        List<XParameterPeriodValue> result = data.getPeriodValues();

        assertEquals(3, result.size());

        assertEquals("key 2", 2, result.get(0).getKey().longValue());
        assertEquals(0.612903599163680D, result.get(0).getPeriodFactor(),
                ASSERT_FACTOR_DOUBLE_DELTA);

        assertEquals("key 1", 1, result.get(1).getKey().longValue());
        assertEquals(0.284945863201912D, result.get(1).getPeriodFactor(),
                ASSERT_FACTOR_DOUBLE_DELTA);

        assertEquals("key 0", 0, result.get(2).getKey().longValue());
        assertEquals(0.102150537634409D, result.get(2).getPeriodFactor(),
                ASSERT_FACTOR_DOUBLE_DELTA);
    }

    @Test
    public void computeParameterPeriodFactor_month_onlyBeforePeriod()
            throws Exception {
        // given
        parameterData.setPeriod(PricingPeriod.MONTH);
        addParameterIdData("parameter1", 0, "value",
                DateTimeHandling.calculateMillis("2012-11-28 14:10:00"),
                DateTimeHandling.calculateMillis("2013-01-01 00:00:00"));

        // when
        calculator.computeParameterPeriodFactor(billingInput.build(),
                parameterData, startTimeForPeriod, endTimeForPeriod);

        // then
        XParameterIdData data = parameterData.getIdData().iterator().next();
        List<XParameterPeriodValue> result = data.getPeriodValues();

        assertEquals(1, result.size());
        assertEquals("key 0", 0, result.get(0).getKey().longValue());
        assertEquals(1, result.get(0).getPeriodFactor(), 0);
    }

    @Test
    public void computeParameterPeriodFactor_week_subscriptionEndOnTimeUnitEnd()
            throws Exception {
        // given
        startTimeForPeriod = DateTimeHandling
                .calculateMillis("2012-12-01 00:00:00");
        endTimeForPeriod = DateTimeHandling
                .calculateMillis("2012-12-03 00:00:00");

        parameterData.setPeriod(PricingPeriod.WEEK);
        addParameterIdData("parameter1", 0, "value",
                DateTimeHandling.calculateMillis("2012-12-01 00:00:00"),
                DateTimeHandling.calculateMillis("2012-12-03 00:00:00"));

        // when
        calculator.computeParameterPeriodFactor(billingInput.build(),
                parameterData, startTimeForPeriod, endTimeForPeriod);

        // then
        XParameterIdData data = parameterData.getIdData().iterator().next();
        List<XParameterPeriodValue> result = data.getPeriodValues();

        assertEquals(1, result.size());
        assertEquals("key 0", 0, result.get(0).getKey().longValue());
        assertEquals(2D, result.get(0).getPeriodFactor(), 0);
    }

    @Test
    public void computeParameterPeriodFactor_month_betweenFirstTimeUnitAndBeforeBillingStart()
            throws Exception {
        // given
        startTimeForPeriod = DateTimeHandling
                .calculateMillis("2012-12-02 00:00:00");
        endTimeForPeriod = DateTimeHandling
                .calculateMillis("2013-12-04 00:00:00");
        billingInput.setBillingPeriodStart(DateTimeHandling
                .calculateMillis("2012-12-15 00:00:00"));
        billingInput.setBillingPeriodEnd(DateTimeHandling
                .calculateMillis("2013-01-15 00:00:00"));

        parameterData.setPeriod(PricingPeriod.MONTH);
        addParameterIdData("parameter1", 0, "value",
                DateTimeHandling.calculateMillis("2012-12-02 00:00:00"),
                DateTimeHandling.calculateMillis("2012-12-04 00:00:00"));

        // when
        calculator.computeParameterPeriodFactor(billingInput.build(),
                parameterData, startTimeForPeriod, endTimeForPeriod);

        // then
        XParameterIdData data = parameterData.getIdData().iterator().next();
        List<XParameterPeriodValue> result = data.getPeriodValues();

        assertEquals(1, result.size());
        assertEquals("key 0", 0, result.get(0).getKey().longValue());
        assertEquals(1, result.get(0).getPeriodFactor(), 0);
    }

    // Bug 10133
    @Test
    public void computeParameterPeriodFactor_month_timeUnitOverlappingBillingEnd()
            throws Exception {
        // given
        startTimeForPeriod = DateTimeHandling
                .calculateMillis("2013-03-20 17:00:00");
        endTimeForPeriod = DateTimeHandling
                .calculateMillis("2013-03-20 17:10:00");
        billingInput.setBillingPeriodStart(DateTimeHandling
                .calculateMillis("2013-02-22 00:00:00"));
        billingInput.setBillingPeriodEnd(DateTimeHandling
                .calculateMillis("2013-03-22 00:00:00"));

        parameterData.setPeriod(PricingPeriod.MONTH);
        addParameterIdData("parameter1", 0, "value",
                DateTimeHandling.calculateMillis("2013-03-20 17:00:00"),
                DateTimeHandling.calculateMillis("2012-03-20 17:10:00"));

        // when
        calculator.computeParameterPeriodFactor(billingInput.build(),
                parameterData, startTimeForPeriod, endTimeForPeriod);

        // then
        assertEquals(0, parameterData.getIdData().size());
    }

    private void addParameterIdData(String id, long key, String value,
            long startTime, long endTime) {
        XParameterIdData idData = parameterData.getIdDataInstance(id,
                ParameterType.PLATFORM_PARAMETER, ParameterValueType.BOOLEAN);
        XParameterPeriodPrimitiveType type = new XParameterPeriodPrimitiveType(
                idData, null, null);
        type.setKey(Long.valueOf(key));
        type.setValue(value);
        type.setStartTime(startTime);
        type.setEndTime(endTime);
        parameterPeriodValues.add(type);
    }

}
