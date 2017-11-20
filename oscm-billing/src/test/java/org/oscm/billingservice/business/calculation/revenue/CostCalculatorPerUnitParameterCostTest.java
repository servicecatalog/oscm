/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 18.12.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.calculation.revenue;

import static org.oscm.test.BigDecimalAsserts.checkEquals;
import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;

import org.oscm.billingservice.business.calculation.revenue.model.ParameterCosts;
import org.oscm.billingservice.dao.model.XParameterData;
import org.oscm.billingservice.dao.model.XParameterIdData;
import org.oscm.billingservice.dao.model.XParameterOption;
import org.oscm.billingservice.dao.model.XParameterPeriodEnumType;
import org.oscm.billingservice.dao.model.XParameterPeriodPrimitiveType;
import org.oscm.billingservice.dao.model.XParameterPeriodValue;
import org.oscm.billingservice.service.model.BillingInput;
import org.oscm.internal.types.enumtypes.ParameterType;
import org.oscm.internal.types.enumtypes.ParameterValueType;
import org.oscm.internal.types.enumtypes.PricingPeriod;

/**
 * @author kulle
 * 
 */
public class CostCalculatorPerUnitParameterCostTest {

    private XParameterData parameterData;
    private BillingInput.Builder builder;
    private CostCalculatorPerUnit calculator;
    private RevenueCalculatorBean revenueBean;

    @Before
    public void setup() {
        parameterData = new XParameterData();
        builder = new BillingInput.Builder();
        calculator = new CostCalculatorPerUnit();
        revenueBean = new RevenueCalculatorBean();
    }

    @Test
    public void getParameterCosts_long_week_beforeSlice_BeforePeriod()
            throws Exception {
        // given (billing period: dec 2012)
        builder.setBillingPeriodStart(1354320000000L);
        builder.setBillingPeriodEnd(1356998400000L);
        parameterData.setPeriod(PricingPeriod.WEEK);
        addParameterPricingData("param1", ParameterValueType.LONG, "1",
                1353542400000L, 1356998400000L, null, 0, 0, 150, 0D);
        calculator.computeParameterPeriodFactor(builder.build(), parameterData,
                builder.getBillingPeriodStart(), 1356998400000L);

        // when
        ParameterCosts parameterCosts = revenueBean.calculateAllParameterCosts(
                calculator, parameterData);

        // then
        assertEquals(1, parameterCosts.getBilledItems().size());
        checkEquals("750.00", parameterCosts.getBilledItems().get(0)
                .getBillingItemCosts());
        assertEquals("param1", parameterCosts.getBilledItems().get(0)
                .getBillingItem().getId());
        checkEquals("750.00", parameterCosts.getNormalizedOverallCosts());
    }

    @Test
    public void getParameterCosts_long_week_beforePeriod() throws Exception {
        // given (billing period: dec 2012)
        builder.setBillingPeriodStart(1354320000000L);
        builder.setBillingPeriodEnd(1356998400000L);
        parameterData.setPeriod(PricingPeriod.WEEK);
        addParameterPricingData("param1", ParameterValueType.LONG, "1",
                1354147200000L, 1356998400000L, null, 0, 0, 150, 0D);
        calculator.computeParameterPeriodFactor(builder.build(), parameterData,
                builder.getBillingPeriodStart(), 1356998400000L);

        // when
        ParameterCosts parameterCosts = revenueBean.calculateAllParameterCosts(
                calculator, parameterData);

        // then
        assertEquals(1, parameterCosts.getBilledItems().size());
        checkEquals("750.0", parameterCosts.getBilledItems().get(0)
                .getBillingItemCosts());
        assertEquals("param1", parameterCosts.getBilledItems().get(0)
                .getBillingItem().getId());
        checkEquals("750.0", parameterCosts.getNormalizedOverallCosts());
    }

    @Test
    public void getParameterCosts_long_week_inFirstSlice_inPeriod()
            throws Exception {
        // given (billing period: dec 2012)
        builder.setBillingPeriodStart(1354320000000L);
        builder.setBillingPeriodEnd(1356998400000L);
        parameterData.setPeriod(PricingPeriod.WEEK);
        addParameterPricingData("param1", ParameterValueType.LONG, "1",
                1354406400000L, 1356998400000L, null, 0, 0, 150, 0D);
        calculator.computeParameterPeriodFactor(builder.build(), parameterData,
                builder.getBillingPeriodStart(), 1356998400000L);

        // when
        ParameterCosts parameterCosts = revenueBean.calculateAllParameterCosts(
                calculator, parameterData);

        // then
        assertEquals(1, parameterCosts.getBilledItems().size());
        checkEquals("750.0", parameterCosts.getBilledItems().get(0)
                .getBillingItemCosts());
        assertEquals("param1", parameterCosts.getBilledItems().get(0)
                .getBillingItem().getId());
        checkEquals("750.0", parameterCosts.getNormalizedOverallCosts());
    }

    private void addParameterPricingData(String parameterId,
            ParameterValueType valueType, String paramValue,
            long paramStartTime, long paramEndTime, String optionId,
            long pricePerUser, double userAssignmentFactor,
            long pricePerSubscription, double periodFactor) {
        addParameterPricingData(valueType, paramValue, paramStartTime,
                paramEndTime, optionId, BigDecimal.valueOf(pricePerUser),
                userAssignmentFactor, BigDecimal.valueOf(pricePerSubscription),
                periodFactor, parameterId);
    }

    private void addParameterPricingData(ParameterValueType valueType,
            String value, long startTime, long endTime, String optionId,
            BigDecimal pricePerUser, double userAssignmentFactor,
            BigDecimal pricePerSubscription, double periodFactor,
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
        periodValue.setPricePerUser(pricePerUser);
        periodValue.setUserAssignmentFactor(userAssignmentFactor);
        periodValue.setPricePerSubscription(pricePerSubscription);
        periodValue.setPeriodFactor(periodFactor);
        periodValue.setStartTime(startTime);
        periodValue.setEndTime(endTime);
    }

}
