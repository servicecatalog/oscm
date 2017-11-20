/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 18.12.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.calculation.revenue;

import static org.oscm.test.BigDecimalAsserts.checkEquals;
import static org.oscm.test.Numbers.BD20;
import static org.oscm.test.Numbers.BD200;
import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import org.oscm.billingservice.business.calculation.revenue.model.BillingItemCosts;
import org.oscm.billingservice.business.calculation.revenue.model.ParameterCosts;
import org.oscm.billingservice.dao.model.RolePricingData;
import org.oscm.billingservice.dao.model.RolePricingDetails;
import org.oscm.billingservice.dao.model.XParameterData;
import org.oscm.billingservice.dao.model.XParameterIdData;
import org.oscm.billingservice.dao.model.XParameterOption;
import org.oscm.billingservice.dao.model.XParameterPeriodEnumType;
import org.oscm.billingservice.dao.model.XParameterPeriodPrimitiveType;
import org.oscm.billingservice.dao.model.XParameterPeriodValue;
import org.oscm.billingservice.service.model.BillingInput;
import org.oscm.domobjects.PricedProductRoleHistory;
import org.oscm.test.BigDecimalAsserts;
import org.oscm.types.exceptions.BillingRunFailed;
import org.oscm.internal.types.enumtypes.ParameterType;
import org.oscm.internal.types.enumtypes.ParameterValueType;
import org.oscm.internal.types.enumtypes.PricingPeriod;

/**
 * @author kulle
 * 
 */
public class CostCalculatorProRataParameterTest {

    private XParameterData parameterData;
    private CostCalculatorProRata calculator;
    private BillingInput.Builder bi;
    private RevenueCalculatorBean revenueBean;

    @Before
    public void setup() {
        parameterData = new XParameterData();
        parameterData.setPeriod(PricingPeriod.DAY);

        bi = new BillingInput.Builder();
        calculator = new CostCalculatorProRata();

        revenueBean = new RevenueCalculatorBean();
    }

    @Test
    public void testGetParameterCostsNoElement() throws Exception {
        ParameterCosts parameterCosts = revenueBean.calculateAllParameterCosts(
                calculator, parameterData);

        checkEquals(0, parameterCosts.getNormalizedOverallCosts());
        assertEquals(0, parameterCosts.getBilledItems().size());
    }

    /**
     * Calculate parameter costs with fractions.
     * 
     * @throws Exception
     */
    @Test
    public void testGetParameterCosts_fraction() throws Exception {
        // given
        addParameterPricingData(ParameterValueType.BOOLEAN, "true", null,
                new BigDecimal("100.5"), 0.5, BigDecimal.ZERO, 0,
                "parameterIdentifier");
        BillingInput billingInput = bi.build();
        calculator.computeParameterPeriodFactor(billingInput, parameterData,
                bi.getBillingPeriodStart(), 0);

        // when
        ParameterCosts parameterCosts = revenueBean.calculateAllParameterCosts(
                calculator, parameterData);

        // then
        checkEquals("50.25", parameterCosts.getNormalizedOverallCosts());
    }

    /**
     * Calculate parameter costs with fractions.
     * 
     * @throws Exception
     */
    @Test
    public void testGetParameterCosts_fraction2() throws Exception {
        // given
        addParameterPricingData(ParameterValueType.BOOLEAN, "true", null,
                new BigDecimal("100.0"), 1, new BigDecimal("10.5"), 0.5,
                "parameterIdentifier");
        BillingInput billingInput = bi.build();
        calculator.computeParameterPeriodFactor(billingInput, parameterData,
                bi.getBillingPeriodStart(), 0);

        // when
        ParameterCosts parameterCosts = revenueBean.calculateAllParameterCosts(
                calculator, parameterData);

        // then
        checkEquals("105.25", parameterCosts.getNormalizedOverallCosts());
    }

    @Test
    public void testGetParameterCostsNoFactor() throws Exception {
        addParameterPricingData(ParameterValueType.ENUMERATION, null,
                "option1", 0, 0, 160, 0, "parameterIdentifier");
        BillingInput billingInput = bi.build();
        calculator.computeParameterPeriodFactor(billingInput, parameterData,
                bi.getBillingPeriodStart(), 0);

        ParameterCosts parameterCosts = revenueBean.calculateAllParameterCosts(
                calculator, parameterData);

        assertEquals(1, parameterCosts.getBilledItems().size());
        checkEquals(0, parameterCosts.getBilledItems().get(0)
                .getBillingItemCosts());
        assertEquals("parameterIdentifier", parameterCosts.getBilledItems()
                .get(0).getBillingItem().getId());
        checkEquals(0, parameterCosts.getNormalizedOverallCosts());
    }

    @Test(expected = BillingRunFailed.class)
    public void testGetParameterCostsIntNoNumber() throws Exception {
        addParameterPricingData(ParameterValueType.INTEGER, "2df23", null, 0,
                0, 150, 0.5D, "parameterIdentifier");
        revenueBean.calculateAllParameterCosts(calculator, parameterData);
    }

    @Test
    public void testGetParameterCostsIntParamPeriodPriceFactorGreater0()
            throws Exception {
        addParameterPricingData(ParameterValueType.INTEGER, "1", null, 0, 0,
                150, 0.5D, "parameterIdentifier");
        BillingInput billingInput = bi.build();
        calculator.computeParameterPeriodFactor(billingInput, parameterData,
                bi.getBillingPeriodStart(), 0);

        ParameterCosts parameterCosts = revenueBean.calculateAllParameterCosts(
                calculator, parameterData);

        assertEquals(1, parameterCosts.getBilledItems().size());
        checkEquals(new BigDecimal("75.0"), parameterCosts.getBilledItems()
                .get(0).getBillingItemCosts());
        assertEquals("parameterIdentifier", parameterCosts.getBilledItems()
                .get(0).getBillingItem().getId());
        checkEquals(75, parameterCosts.getNormalizedOverallCosts());
    }

    @Test
    public void testGetParameterCostsIntParamPeriodPriceFactorGreater0RoundDown()
            throws Exception {
        // period * price is 0.318, so rounding it is 0. nevertheless price has
        // to consider the parameter value and is expected to be 32
        addParameterPricingData(ParameterValueType.INTEGER, "100", null, 0, 0,
                10, 0.031851155D, "parameterIdentifier");
        BillingInput billingInput = bi.build();
        calculator.computeParameterPeriodFactor(billingInput, parameterData,
                bi.getBillingPeriodStart(), 0);

        ParameterCosts parameterCosts = revenueBean.calculateAllParameterCosts(
                calculator, parameterData);

        assertEquals(1, parameterCosts.getBilledItems().size());
        checkEquals(new BigDecimal("31.85"), parameterCosts.getBilledItems()
                .get(0).getBillingItemCosts());
        assertEquals("parameterIdentifier", parameterCosts.getBilledItems()
                .get(0).getBillingItem().getId());
        checkEquals("31.85", parameterCosts.getNormalizedOverallCosts());
    }

    @Test
    public void testGetParameterCostsIntParamPeriodPriceFactorGreater0RoundUp()
            throws Exception {
        // period * price is 0.651, so rounding it is 1. nevertheless price has
        // to consider the parameter value and is expected to be 65
        addParameterPricingData(ParameterValueType.INTEGER, "100", null, 0, 0,
                10, 0.065184489D, "parameterIdentifier");
        BillingInput billingInput = bi.build();
        calculator.computeParameterPeriodFactor(billingInput, parameterData,
                bi.getBillingPeriodStart(), 0);

        ParameterCosts parameterCosts = revenueBean.calculateAllParameterCosts(
                calculator, parameterData);

        assertEquals(1, parameterCosts.getBilledItems().size());
        checkEquals(new BigDecimal("65.18"), parameterCosts.getBilledItems()
                .get(0).getBillingItemCosts());
        assertEquals("parameterIdentifier", parameterCosts.getBilledItems()
                .get(0).getBillingItem().getId());
        checkEquals("65.18", parameterCosts.getNormalizedOverallCosts());
    }

    @Test
    public void testGetParameterCostsIntParamPeriodPriceFactorEquals0()
            throws Exception {
        addParameterPricingData(ParameterValueType.INTEGER, "0", null, 0, 0,
                150, 0.5D, "parameterIdentifier");
        ParameterCosts parameterCosts = revenueBean.calculateAllParameterCosts(
                calculator, parameterData);

        assertEquals(1, parameterCosts.getBilledItems().size());
        checkEquals(0, parameterCosts.getBilledItems().get(0)
                .getBillingItemCosts());
        assertEquals("parameterIdentifier", parameterCosts.getBilledItems()
                .get(0).getBillingItem().getId());
        checkEquals(0, parameterCosts.getNormalizedOverallCosts());
    }

    @Test
    public void testGetParameterCostsLongParamPeriodPriceFactorNotEquals0()
            throws Exception {
        addParameterPricingData(ParameterValueType.LONG, "25123456541512142",
                null, 0, 0, 150, 0.5D, "parameterIdentifier");
        BillingInput billingInput = bi.build();
        calculator.computeParameterPeriodFactor(billingInput, parameterData,
                bi.getBillingPeriodStart(), 0);

        ParameterCosts parameterCosts = revenueBean.calculateAllParameterCosts(
                calculator, parameterData);

        assertEquals(1, parameterCosts.getBilledItems().size());
        checkEquals(new BigDecimal("1884259240613410650.0"), parameterCosts
                .getBilledItems().get(0).getBillingItemCosts());
        assertEquals("parameterIdentifier", parameterCosts.getBilledItems()
                .get(0).getBillingItem().getId());
        checkEquals(new BigDecimal("1884259240613410650.0"),
                parameterCosts.getNormalizedOverallCosts());
    }

    @Test
    public void testGetParameterCostsLongParamPeriodPriceFactorEquals0()
            throws Exception {
        addParameterPricingData(ParameterValueType.LONG, "0", null, 0, 0, 150,
                0.5D, "parameterIdentifier");
        ParameterCosts parameterCosts = revenueBean.calculateAllParameterCosts(
                calculator, parameterData);

        assertEquals(1, parameterCosts.getBilledItems().size());
        checkEquals(0, parameterCosts.getBilledItems().get(0)
                .getBillingItemCosts());
        assertEquals("parameterIdentifier", parameterCosts.getBilledItems()
                .get(0).getBillingItem().getId());
        checkEquals("0.0", parameterCosts.getNormalizedOverallCosts());
    }

    @Test
    public void testGetParameterCostsBooleanParamPeriodPriceTrue()
            throws Exception {
        addParameterPricingData(ParameterValueType.BOOLEAN, "true", null, 0, 0,
                150, 0.5D, "parameterIdentifier");
        BillingInput billingInput = bi.build();
        calculator.computeParameterPeriodFactor(billingInput, parameterData,
                bi.getBillingPeriodStart(), 0);

        ParameterCosts parameterCosts = revenueBean.calculateAllParameterCosts(
                calculator, parameterData);

        assertEquals(1, parameterCosts.getBilledItems().size());
        checkEquals(new BigDecimal("75.0"), parameterCosts.getBilledItems()
                .get(0).getBillingItemCosts());
        assertEquals("parameterIdentifier", parameterCosts.getBilledItems()
                .get(0).getBillingItem().getId());
        checkEquals("75.0", parameterCosts.getNormalizedOverallCosts());
    }

    @Test
    public void testGetParameterCostsBooleanParamNullValue() throws Exception {
        addParameterPricingData(ParameterValueType.BOOLEAN, null, null, 0, 0,
                150, 0.5D, "parameterIdentifier");
        ParameterCosts parameterCosts = revenueBean.calculateAllParameterCosts(
                calculator, parameterData);

        assertEquals(1, parameterCosts.getBilledItems().size());
        checkEquals("0.0", parameterCosts.getBilledItems().get(0)
                .getBillingItemCosts());
        assertEquals("parameterIdentifier", parameterCosts.getBilledItems()
                .get(0).getBillingItem().getId());
        checkEquals("0.0", parameterCosts.getNormalizedOverallCosts());
    }

    @Test
    public void testGetParameterCostsBooleanParamPeriodPriceFalse()
            throws Exception {
        addParameterPricingData(ParameterValueType.BOOLEAN, "false", null, 0,
                0, 150, 0.5D, "parameterIdentifier");
        ParameterCosts parameterCosts = revenueBean.calculateAllParameterCosts(
                calculator, parameterData);

        assertEquals(1, parameterCosts.getBilledItems().size());
        checkEquals(0, parameterCosts.getBilledItems().get(0)
                .getBillingItemCosts());
        assertEquals("parameterIdentifier", parameterCosts.getBilledItems()
                .get(0).getBillingItem().getId());
        checkEquals(0, parameterCosts.getNormalizedOverallCosts());
    }

    @Test
    public void testGetParameterCostsDurationParamPeriodPriceValue0()
            throws Exception {
        addParameterPricingData(ParameterValueType.DURATION, "0", null, 0, 0,
                150, 0.5D, "parameterIdentifier");
        ParameterCosts parameterCosts = revenueBean.calculateAllParameterCosts(
                calculator, parameterData);

        assertEquals(1, parameterCosts.getBilledItems().size());
        checkEquals(0, parameterCosts.getBilledItems().get(0)
                .getBillingItemCosts());
        assertEquals("parameterIdentifier", parameterCosts.getBilledItems()
                .get(0).getBillingItem().getId());
        checkEquals(0, parameterCosts.getNormalizedOverallCosts());
    }

    @Test
    public void testGetParameterCostsStringParamPeriodPriceValueGreater0()
            throws Exception {
        addParameterPricingData(ParameterValueType.DURATION, "43200000", null,
                0, 0, 160, 0.5D, "parameterIdentifier");
        BillingInput billingInput = bi.build();
        calculator.computeParameterPeriodFactor(billingInput, parameterData,
                bi.getBillingPeriodStart(), 0);

        ParameterCosts parameterCosts = revenueBean.calculateAllParameterCosts(
                calculator, parameterData);

        assertEquals(1, parameterCosts.getBilledItems().size());
        checkEquals(40, parameterCosts.getBilledItems().get(0)
                .getBillingItemCosts());
        assertEquals("parameterIdentifier", parameterCosts.getBilledItems()
                .get(0).getBillingItem().getId());
        checkEquals(40, parameterCosts.getNormalizedOverallCosts());
    }

    @Test
    public void testGetParameterCostsEnumParamPeriodPriceNoCosts()
            throws Exception {
        addParameterPricingData(ParameterValueType.ENUMERATION, null,
                "option1", 0, 0, 160, 0.5D, "parameterIdentifier");
        BillingInput billingInput = bi.build();
        calculator.computeParameterPeriodFactor(billingInput, parameterData,
                bi.getBillingPeriodStart(), 0);

        ParameterCosts parameterCosts = revenueBean.calculateAllParameterCosts(
                calculator, parameterData);

        assertEquals(1, parameterCosts.getBilledItems().size());
        checkEquals("80.0", parameterCosts.getBilledItems().get(0)
                .getBillingItemCosts());
        assertEquals("parameterIdentifier", parameterCosts.getBilledItems()
                .get(0).getBillingItem().getId());
        checkEquals("80.0", parameterCosts.getNormalizedOverallCosts());
    }

    @Test
    public void testGetParameterCostsEnumParamPeriodPriceCostsGreater0()
            throws Exception {
        addParameterPricingData(ParameterValueType.ENUMERATION, null,
                "option1", 0, 0, 160, 0.5D, "parameterIdentifier");
        BillingInput billingInput = bi.build();
        calculator.computeParameterPeriodFactor(billingInput, parameterData,
                bi.getBillingPeriodStart(), 0);

        ParameterCosts parameterCosts = revenueBean.calculateAllParameterCosts(
                calculator, parameterData);

        assertEquals(1, parameterCosts.getBilledItems().size());
        checkEquals("80.0", parameterCosts.getBilledItems().get(0)
                .getBillingItemCosts());
        assertEquals("parameterIdentifier", parameterCosts.getBilledItems()
                .get(0).getBillingItem().getId());
        checkEquals("80.0", parameterCosts.getNormalizedOverallCosts());
    }

    @Test
    public void testGetParameterCostsIntParamUserPriceFactorGreater0()
            throws Exception {
        addParameterPricingData(ParameterValueType.INTEGER, "4", null, 84,
                0.5D, 0, 0, "parameterIdentifier");
        ParameterCosts parameterCosts = revenueBean.calculateAllParameterCosts(
                calculator, parameterData);

        assertEquals(1, parameterCosts.getBilledItems().size());
        checkEquals("168.0", parameterCosts.getBilledItems().get(0)
                .getBillingItemCosts());
        assertEquals("parameterIdentifier", parameterCosts.getBilledItems()
                .get(0).getBillingItem().getId());
        checkEquals("168.0", parameterCosts.getNormalizedOverallCosts());
    }

    @Test
    public void testGetParameterCostsIntParamUserPriceFactorGreater0RoundDown()
            throws Exception {
        addParameterPricingData(ParameterValueType.INTEGER, "10", null, 100,
                0.002D, 0, 0, "parameterIdentifier");
        ParameterCosts parameterCosts = revenueBean.calculateAllParameterCosts(
                calculator, parameterData);

        assertEquals(1, parameterCosts.getBilledItems().size());
        checkEquals("2.00", parameterCosts.getBilledItems().get(0)
                .getBillingItemCosts());
        assertEquals("parameterIdentifier", parameterCosts.getBilledItems()
                .get(0).getBillingItem().getId());
        checkEquals("2.00", parameterCosts.getNormalizedOverallCosts());
    }

    @Test
    public void testGetParameterCostsIntParamUserPriceFactorGreater0RoundUp()
            throws Exception {
        addParameterPricingData(ParameterValueType.INTEGER, "10", null, 100,
                0.006D, 0, 0, "parameterIdentifier");
        ParameterCosts parameterCosts = revenueBean.calculateAllParameterCosts(
                calculator, parameterData);

        assertEquals(1, parameterCosts.getBilledItems().size());
        checkEquals("6.00", parameterCosts.getBilledItems().get(0)
                .getBillingItemCosts());
        assertEquals("parameterIdentifier", parameterCosts.getBilledItems()
                .get(0).getBillingItem().getId());
        checkEquals("6.00", parameterCosts.getNormalizedOverallCosts());
    }

    @Test
    public void testGetParameterCostsIntParamUsersPriceFactorEquals0()
            throws Exception {
        addParameterPricingData(ParameterValueType.INTEGER, "0", null, 160,
                0.4D, 0, 0, "parameterIdentifier");
        ParameterCosts parameterCosts = revenueBean.calculateAllParameterCosts(
                calculator, parameterData);

        assertEquals(1, parameterCosts.getBilledItems().size());
        checkEquals(0, parameterCosts.getBilledItems().get(0)
                .getBillingItemCosts());
        assertEquals("parameterIdentifier", parameterCosts.getBilledItems()
                .get(0).getBillingItem().getId());
        checkEquals(0, parameterCosts.getNormalizedOverallCosts());
    }

    @Test
    public void testGetParameterCostsLongParamUsersPriceFactorNotEquals0()
            throws Exception {
        addParameterPricingData(ParameterValueType.LONG, "25123456541512142",
                null, 150, 0.5D, 0, 0, "parameterIdentifier");
        ParameterCosts parameterCosts = revenueBean.calculateAllParameterCosts(
                calculator, parameterData);

        assertEquals(1, parameterCosts.getBilledItems().size());
        checkEquals("1884259240613410650.0", parameterCosts.getBilledItems()
                .get(0).getBillingItemCosts());
        assertEquals("parameterIdentifier", parameterCosts.getBilledItems()
                .get(0).getBillingItem().getId());
        checkEquals("1884259240613410650.0",
                parameterCosts.getNormalizedOverallCosts());
    }

    @Test
    public void testGetParameterCostsLongParamUsersPriceFactorEquals0()
            throws Exception {
        addParameterPricingData(ParameterValueType.LONG, "0", null, 150, 0.5D,
                0, 0, "parameterIdentifier");
        ParameterCosts parameterCosts = revenueBean.calculateAllParameterCosts(
                calculator, parameterData);

        assertEquals(1, parameterCosts.getBilledItems().size());
        checkEquals(0, parameterCosts.getBilledItems().get(0)
                .getBillingItemCosts());
        assertEquals("parameterIdentifier", parameterCosts.getBilledItems()
                .get(0).getBillingItem().getId());
        checkEquals(0, parameterCosts.getNormalizedOverallCosts());
    }

    @Test
    public void testGetParameterCostsBooleanParamUsersPriceTrue()
            throws Exception {
        addParameterPricingData(ParameterValueType.BOOLEAN, "true", null, 750,
                0.5D, 0, 0, "parameterIdentifier");
        ParameterCosts parameterCosts = revenueBean.calculateAllParameterCosts(
                calculator, parameterData);

        assertEquals(1, parameterCosts.getBilledItems().size());
        checkEquals("375.0", parameterCosts.getBilledItems().get(0)
                .getBillingItemCosts());
        assertEquals("parameterIdentifier", parameterCosts.getBilledItems()
                .get(0).getBillingItem().getId());
        checkEquals("375.0", parameterCosts.getNormalizedOverallCosts());
    }

    @Test
    public void testGetParameterCostsBooleanParamUsersPriceFalse()
            throws Exception {
        addParameterPricingData(ParameterValueType.BOOLEAN, "false", null, 150,
                0.5D, 0, 0, "parameterIdentifier");
        ParameterCosts parameterCosts = revenueBean.calculateAllParameterCosts(
                calculator, parameterData);

        assertEquals(1, parameterCosts.getBilledItems().size());
        checkEquals(0, parameterCosts.getBilledItems().get(0)
                .getBillingItemCosts());
        assertEquals("parameterIdentifier", parameterCosts.getBilledItems()
                .get(0).getBillingItem().getId());
        checkEquals(0, parameterCosts.getNormalizedOverallCosts());
    }

    @Test
    public void testGetParameterCostsDurationParamUsersPriceValue0()
            throws Exception {
        addParameterPricingData(ParameterValueType.DURATION, "0", null, 150,
                0.5D, 0, 0, "parameterIdentifier");
        ParameterCosts parameterCosts = revenueBean.calculateAllParameterCosts(
                calculator, parameterData);

        assertEquals(1, parameterCosts.getBilledItems().size());
        checkEquals(0, parameterCosts.getBilledItems().get(0)
                .getBillingItemCosts());
        assertEquals("parameterIdentifier", parameterCosts.getBilledItems()
                .get(0).getBillingItem().getId());
        checkEquals(0, parameterCosts.getNormalizedOverallCosts());
    }

    @Test
    public void testGetParameterCostsStringParamUsersPriceValueGreater0()
            throws Exception {
        addParameterPricingData(ParameterValueType.DURATION, "43200000", null,
                840, 0.5D, 0, 0, "parameterIdentifier");
        ParameterCosts parameterCosts = revenueBean.calculateAllParameterCosts(
                calculator, parameterData);

        assertEquals(1, parameterCosts.getBilledItems().size());
        checkEquals(210, parameterCosts.getBilledItems().get(0)
                .getBillingItemCosts());
        assertEquals("parameterIdentifier", parameterCosts.getBilledItems()
                .get(0).getBillingItem().getId());
        checkEquals(210, parameterCosts.getNormalizedOverallCosts());
    }

    @Test
    public void testGetParameterCostsEnumParamUsersPriceNoCosts()
            throws Exception {
        addParameterPricingData(ParameterValueType.ENUMERATION, null,
                "option1", 160, 0.5D, 0, 0, "parameterIdentifier");
        ParameterCosts parameterCosts = revenueBean.calculateAllParameterCosts(
                calculator, parameterData);

        assertEquals(1, parameterCosts.getBilledItems().size());
        checkEquals("80.0", parameterCosts.getBilledItems().get(0)
                .getBillingItemCosts());
        assertEquals("parameterIdentifier", parameterCosts.getBilledItems()
                .get(0).getBillingItem().getId());
        checkEquals("80.0", parameterCosts.getNormalizedOverallCosts());
    }

    @Test
    public void testGetParameterCostsEnumParamUsersPriceCostsGreater0()
            throws Exception {
        addParameterPricingData(ParameterValueType.ENUMERATION, null,
                "option1", 444, 0.5D, 0, 0, "parameterIdentifier");
        ParameterCosts parameterCosts = revenueBean.calculateAllParameterCosts(
                calculator, parameterData);

        assertEquals(1, parameterCosts.getBilledItems().size());
        checkEquals("222.0", parameterCosts.getBilledItems().get(0)
                .getBillingItemCosts());
        assertEquals("parameterIdentifier", parameterCosts.getBilledItems()
                .get(0).getBillingItem().getId());
        checkEquals("222.0", parameterCosts.getNormalizedOverallCosts());
    }

    @Test
    public void testGetParameterCostsIntParamUsersAndPeriodPriceFactorGreater0()
            throws Exception {
        addParameterPricingData(ParameterValueType.INTEGER, "4", null, 84,
                0.5D, 120, 0.4D, "parameterIdentifier");
        BillingInput billingInput = bi.build();
        calculator.computeParameterPeriodFactor(billingInput, parameterData,
                bi.getBillingPeriodStart(), 0);

        ParameterCosts parameterCosts = revenueBean.calculateAllParameterCosts(
                calculator, parameterData);

        assertEquals(1, parameterCosts.getBilledItems().size());
        checkEquals("360.00", parameterCosts.getBilledItems().get(0)
                .getBillingItemCosts());
        assertEquals("parameterIdentifier", parameterCosts.getBilledItems()
                .get(0).getBillingItem().getId());
        checkEquals("360.00", parameterCosts.getNormalizedOverallCosts());
    }

    @Test
    public void testGetParameterCostsIntParamUsersAndPeriodPriceFactorEquals0()
            throws Exception {
        addParameterPricingData(ParameterValueType.INTEGER, "0", null, 160,
                0.4D, 44, 0.1D, "parameterIdentifier");
        ParameterCosts parameterCosts = revenueBean.calculateAllParameterCosts(
                calculator, parameterData);

        assertEquals(1, parameterCosts.getBilledItems().size());
        checkEquals(0, parameterCosts.getBilledItems().get(0)
                .getBillingItemCosts());
        assertEquals("parameterIdentifier", parameterCosts.getBilledItems()
                .get(0).getBillingItem().getId());
        checkEquals(0, parameterCosts.getNormalizedOverallCosts());
    }

    @Test
    public void testGetParameterCostsLongParamUsersAndPeriodPriceFactorNotEquals0()
            throws Exception {
        addParameterPricingData(ParameterValueType.LONG, "25123456541512142",
                null, 150, 0.5D, 80, 0.0125D, "parameterIdentifier");
        BillingInput billingInput = bi.build();
        calculator.computeParameterPeriodFactor(billingInput, parameterData,
                bi.getBillingPeriodStart(), 0);

        ParameterCosts parameterCosts = revenueBean.calculateAllParameterCosts(
                calculator, parameterData);

        assertEquals(1, parameterCosts.getBilledItems().size());
        checkEquals("1909382697154922792.00", parameterCosts.getBilledItems()
                .get(0).getBillingItemCosts());
        assertEquals("parameterIdentifier", parameterCosts.getBilledItems()
                .get(0).getBillingItem().getId());
        checkEquals("1909382697154922792.00",
                parameterCosts.getNormalizedOverallCosts());
    }

    @Test
    public void testGetParameterCostsLongParamUsersAndPeriodPriceFactorEquals0()
            throws Exception {
        addParameterPricingData(ParameterValueType.LONG, "0", null, 150, 0.5D,
                44, 0.75D, "parameterIdentifier");
        ParameterCosts parameterCosts = revenueBean.calculateAllParameterCosts(
                calculator, parameterData);

        assertEquals(1, parameterCosts.getBilledItems().size());
        checkEquals(0, parameterCosts.getBilledItems().get(0)
                .getBillingItemCosts());
        assertEquals("parameterIdentifier", parameterCosts.getBilledItems()
                .get(0).getBillingItem().getId());
        checkEquals(0, parameterCosts.getNormalizedOverallCosts());
    }

    @Test
    public void testGetParameterCostsBooleanParamUsersAndPeriodPriceTrue()
            throws Exception {
        addParameterPricingData(ParameterValueType.BOOLEAN, "true", null, 750,
                0.5D, 42, 0.5D, "parameterIdentifier");
        BillingInput billingInput = bi.build();
        calculator.computeParameterPeriodFactor(billingInput, parameterData,
                bi.getBillingPeriodStart(), 0);

        ParameterCosts parameterCosts = revenueBean.calculateAllParameterCosts(
                calculator, parameterData);

        assertEquals(1, parameterCosts.getBilledItems().size());
        checkEquals("396.0", parameterCosts.getBilledItems().get(0)
                .getBillingItemCosts());
        assertEquals("parameterIdentifier", parameterCosts.getBilledItems()
                .get(0).getBillingItem().getId());
        checkEquals("396.0", parameterCosts.getNormalizedOverallCosts());
    }

    @Test
    public void testGetParameterCostsBooleanParamUsersAndPeriodPriceFalse()
            throws Exception {
        addParameterPricingData(ParameterValueType.BOOLEAN, "false", null, 150,
                0.5D, 1000, 0.001D, "parameterIdentifier");
        ParameterCosts parameterCosts = revenueBean.calculateAllParameterCosts(
                calculator, parameterData);

        assertEquals(1, parameterCosts.getBilledItems().size());
        checkEquals(0, parameterCosts.getBilledItems().get(0)
                .getBillingItemCosts());
        assertEquals("parameterIdentifier", parameterCosts.getBilledItems()
                .get(0).getBillingItem().getId());
        checkEquals(0, parameterCosts.getNormalizedOverallCosts());
    }

    @Test
    public void testGetParameterCostsDurationParamUsersAndPeriodPriceValue0()
            throws Exception {
        addParameterPricingData(ParameterValueType.DURATION, "0", null, 150,
                0.5D, 144, 2.5D, "parameterIdentifier");
        ParameterCosts parameterCosts = revenueBean.calculateAllParameterCosts(
                calculator, parameterData);

        assertEquals(1, parameterCosts.getBilledItems().size());
        checkEquals(0, parameterCosts.getBilledItems().get(0)
                .getBillingItemCosts());
        assertEquals("parameterIdentifier", parameterCosts.getBilledItems()
                .get(0).getBillingItem().getId());
        checkEquals(0, parameterCosts.getNormalizedOverallCosts());
    }

    @Test
    public void testGetParameterCostsStringParamUsersAndPeriodPriceValueGreater0()
            throws Exception {
        addParameterPricingData(ParameterValueType.DURATION, "43200000", null,
                840, 0.5D, 70000, 18, "parameterIdentifier");
        BillingInput billingInput = bi.build();
        calculator.computeParameterPeriodFactor(billingInput, parameterData,
                bi.getBillingPeriodStart(), 0);

        ParameterCosts parameterCosts = revenueBean.calculateAllParameterCosts(
                calculator, parameterData);

        assertEquals(1, parameterCosts.getBilledItems().size());
        checkEquals("630210.0", parameterCosts.getBilledItems().get(0)
                .getBillingItemCosts());
        assertEquals("parameterIdentifier", parameterCosts.getBilledItems()
                .get(0).getBillingItem().getId());
        checkEquals("630210.0", parameterCosts.getNormalizedOverallCosts());
    }

    @Test
    public void testGetParameterCostsEnumParamUsersAndPeriodPriceNoValue()
            throws Exception {
        addParameterPricingData(ParameterValueType.ENUMERATION, null,
                "option1", 160, 0.5D, 7, 2, "parameterIdentifier");
        BillingInput billingInput = bi.build();
        calculator.computeParameterPeriodFactor(billingInput, parameterData,
                bi.getBillingPeriodStart(), 0);

        ParameterCosts parameterCosts = revenueBean.calculateAllParameterCosts(
                calculator, parameterData);

        assertEquals(1, parameterCosts.getBilledItems().size());
        checkEquals("94.0", parameterCosts.getBilledItems().get(0)
                .getBillingItemCosts());
        assertEquals("parameterIdentifier", parameterCosts.getBilledItems()
                .get(0).getBillingItem().getId());
        checkEquals("94.0", parameterCosts.getNormalizedOverallCosts());
    }

    @Test
    public void testGetParameterCostsEnumParamUsersAndPeriodPriceCostsGreater0()
            throws Exception {

        // given
        addParameterPricingData(ParameterValueType.ENUMERATION, null,
                "option1", 444, 0.5D, 48, 3.25D, "parameterIdentifier");
        BillingInput billingInput = bi.build();
        calculator.computeParameterPeriodFactor(billingInput, parameterData,
                bi.getBillingPeriodStart(), 0);

        // when
        ParameterCosts parameterCosts = revenueBean.calculateAllParameterCosts(
                calculator, parameterData);

        // then
        assertEquals(1, parameterCosts.getBilledItems().size());
        checkEquals("378.0", parameterCosts.getBilledItems().get(0)
                .getBillingItemCosts());
        assertEquals("parameterIdentifier", parameterCosts.getBilledItems()
                .get(0).getBillingItem().getId());
        checkEquals("378.0", parameterCosts.getNormalizedOverallCosts());
    }

    @Test
    public void testGetParameterCostsMultipleEntries() throws Exception {
        // given
        addParameterPricingData(ParameterValueType.ENUMERATION, null,
                "option1", 100, 0.5D, 480, 3.25D, "parameterIdentifier1");
        addParameterPricingData(ParameterValueType.DURATION, "43200000", null,
                840, 0.5D, 700, 1.8, "parameterIdentifier2");
        addParameterPricingData(ParameterValueType.BOOLEAN, "true", null, 0, 0,
                150, 0.5D, "parameterIdentifier3");
        addParameterPricingData(ParameterValueType.BOOLEAN, "false", null, 0,
                0, 150, 0.5D, "parameterIdentifier4");
        addParameterPricingData(ParameterValueType.LONG, "2", null, 150, 0.5D,
                0, 0, "parameterIdentifier5");
        addParameterPricingData(ParameterValueType.INTEGER, "4", null, 84,
                0.5D, 0, 0, "parameterIdentifier6");
        BillingInput billingInput = bi.build();
        calculator.computeParameterPeriodFactor(billingInput, parameterData,
                bi.getBillingPeriodStart(), 0);

        // when
        ParameterCosts parameterCosts = revenueBean.calculateAllParameterCosts(
                calculator, parameterData);

        // then
        assertEquals(6, parameterCosts.getBilledItems().size());
        Map<String, BillingItemCosts<XParameterPeriodValue>> billingItemsPerParamId = new HashMap<String, BillingItemCosts<XParameterPeriodValue>>();
        for (BillingItemCosts<XParameterPeriodValue> bi : parameterCosts
                .getBilledItems()) {
            billingItemsPerParamId.put(bi.getBillingItem().getId(), bi);
        }
        String[] expectedCosts = new String[] { "1610.0", "840.00", "75.0",
                "0.0", "150.0", "168.0", "2843.00" };
        for (int i = 0; i < billingItemsPerParamId.keySet().size(); i++) {
            checkEquals(
                    expectedCosts[i],
                    billingItemsPerParamId.remove(
                            "parameterIdentifier" + (i + 1))
                            .getBillingItemCosts());
        }
    }

    @Test
    public void calculateCostsForRoles_NullInput() {
        // when
        BigDecimal roleCosts = revenueBean.calculateParameterUserRoleCosts(
                null, null);

        // then
        BigDecimalAsserts.checkEquals(BigDecimal.ZERO, roleCosts);
    }

    @Test
    public void calculateCostsForRoles_NoRolePrices() {
        // when
        BigDecimal roleCosts = revenueBean.calculateParameterUserRoleCosts(
                new RolePricingData(), BigDecimal.ONE);

        // then
        BigDecimalAsserts.checkEquals(BigDecimal.ZERO, roleCosts);
    }

    @Test
    public void calculateCostsForRoles_OneRolePrice() {
        // when
        RolePricingData rolePricingData = new RolePricingData();
        Map<Long, RolePricingDetails> roleDetails = new HashMap<Long, RolePricingDetails>();
        RolePricingDetails rolePricingDetails = new RolePricingDetails(
                rolePricingData);
        PricedProductRoleHistory pprh = new PricedProductRoleHistory();
        pprh.setPricePerUser(BD20);
        rolePricingDetails.setPricedProductRoleHistory(pprh);
        rolePricingDetails.setFactor(1);
        roleDetails.put(Long.valueOf(1), rolePricingDetails);
        rolePricingData.addRolePricesForContainerKey(Long.valueOf(1),
                roleDetails);

        // when
        revenueBean.calculateParameterUserRoleCosts(rolePricingData,
                BigDecimal.ONE);

        // then
        BigDecimalAsserts.checkEquals(new BigDecimal("20.00"),
                rolePricingDetails.getCost());
        BigDecimalAsserts.checkEquals(new BigDecimal("20.00"),
                rolePricingData.getCosts());
    }

    @Test
    public void calculateCostsForRoles_TwoRolePrices() {
        // given
        RolePricingData rolePricingData = new RolePricingData();
        Map<Long, RolePricingDetails> roleDetails = new HashMap<Long, RolePricingDetails>();
        RolePricingDetails rolePricingDetails = new RolePricingDetails(
                rolePricingData);
        PricedProductRoleHistory pprh = new PricedProductRoleHistory();
        pprh.setPricePerUser(BD20);
        rolePricingDetails.setPricedProductRoleHistory(pprh);
        rolePricingDetails.setFactor(1);
        roleDetails.put(Long.valueOf(1), rolePricingDetails);
        RolePricingDetails rolePricingDetails2 = new RolePricingDetails(
                rolePricingData);
        PricedProductRoleHistory pprh2 = new PricedProductRoleHistory();
        pprh2.setPricePerUser(BD200);
        rolePricingDetails2.setPricedProductRoleHistory(pprh2);
        rolePricingDetails2.setFactor(0.5);
        roleDetails.put(Long.valueOf(2), rolePricingDetails2);
        rolePricingData.addRolePricesForContainerKey(Long.valueOf(1),
                roleDetails);

        // when
        revenueBean.calculateParameterUserRoleCosts(rolePricingData,
                BigDecimal.ONE);

        // then
        BigDecimalAsserts.checkEquals(new BigDecimal("20.00"),
                rolePricingDetails.getCost());
        BigDecimalAsserts.checkEquals(new BigDecimal("100.00"),
                rolePricingDetails2.getCost());
        BigDecimalAsserts.checkEquals(new BigDecimal("120.00"),
                rolePricingData.getCosts());
    }

    /**
     * Initializes the global ParameterPricingData object with the given
     * settings and adds it to the global parameter list.
     */
    private void addParameterPricingData(ParameterValueType valueType,
            String value, String optionId, long baseUserPrice,
            double userFactor, long basePeriodPrice, double periodFactor,
            String parameterId) {
        addParameterPricingData(valueType, value, optionId,
                BigDecimal.valueOf(baseUserPrice), userFactor,
                BigDecimal.valueOf(basePeriodPrice), periodFactor, parameterId);
    }

    private void addParameterPricingData(ParameterValueType valueType,
            String value, String optionId, BigDecimal baseUserPrice,
            double userFactor, BigDecimal basePeriodPrice, double periodFactor,
            String parameterId) {
        XParameterIdData idData = parameterData.getIdDataInstance(parameterId,
                ParameterType.PLATFORM_PARAMETER, valueType);
        XParameterPeriodValue periodValue = null;
        if (valueType != ParameterValueType.ENUMERATION) {
            XParameterPeriodPrimitiveType ppd = new XParameterPeriodPrimitiveType(
                    idData, null, null);
            ppd.setValue(value);
            periodValue = ppd;
        } else {
            XParameterPeriodEnumType ppd = new XParameterPeriodEnumType(idData);
            XParameterOption option = new XParameterOption(ppd);
            option.setId(optionId);
            ppd.setParameterOption(option);
            periodValue = ppd;
        }
        periodValue.setPricePerUser(baseUserPrice);
        periodValue.setUserAssignmentFactor(userFactor);
        periodValue.setPricePerSubscription(basePeriodPrice);
        periodValue.setEndTime((long) (periodFactor * 86400000));
    }

}
