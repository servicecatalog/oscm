/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: May 29, 2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.calculation.revenue;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import org.oscm.billingservice.business.calculation.revenue.model.UserAssignmentFactors;
import org.oscm.billingservice.dao.BillingDataRetrievalServiceBean;
import org.oscm.billingservice.dao.BillingDataRetrievalServiceLocal;
import org.oscm.billingservice.dao.model.RolePricingData;
import org.oscm.billingservice.dao.model.RolePricingDetails;
import org.oscm.billingservice.dao.model.XParameterData;
import org.oscm.billingservice.dao.model.XParameterIdData;
import org.oscm.billingservice.dao.model.XParameterOption;
import org.oscm.billingservice.dao.model.XParameterPeriodEnumType;
import org.oscm.billingservice.dao.model.XParameterPeriodPrimitiveType;
import org.oscm.billingservice.dao.model.XParameterPeriodValue;
import org.oscm.billingservice.service.model.BillingInput;
import org.oscm.domobjects.UsageLicenseHistory;
import org.oscm.domobjects.enums.ModificationType;
import org.oscm.test.DateTimeHandling;
import org.oscm.internal.types.enumtypes.ParameterType;
import org.oscm.internal.types.enumtypes.ParameterValueType;
import org.oscm.internal.types.enumtypes.PricingPeriod;

/**
 * @author tokoda
 * 
 */
public class CostcalculatorPerUnitParameterUserFactorAndRoleFactorTest {

    private static final long SUBSCRIPTION_KEY = 10000;

    private static final long USER_KEY1 = 20000;
    private static final String USER_ID1 = "USER1";

    private static final long USER_KEY2 = 20001;
    private static final String USER_ID2 = "USER2";

    private static final long ROLE_KEY1 = 30000;
    private static final long ROLE_KEY2 = 30001;

    private static final String PARAMETER_ID1 = "PARAM1";
    private static final String PARAMETER_ID2 = "PARAM2";

    private static final long PERIOD_VALUE_KEY1 = 90000;
    private static final long PERIOD_VALUE_KEY2 = 90001;
    private static final long PERIOD_VALUE_KEY3 = 90002;
    private static final long PERIOD_VALUE_KEY4 = 90003;

    private CostCalculatorPerUnit calculator;

    private XParameterData parameterData;

    private BillingDataRetrievalServiceLocal billingDao;

    private BillingInput.Builder billingInput;

    private List<UsageLicenseHistory> ulHistories;

    @Before
    public void setup() {
        calculator = new CostCalculatorPerUnit();
        parameterData = new XParameterData();

        ulHistories = new ArrayList<UsageLicenseHistory>();
        billingDao = mock(BillingDataRetrievalServiceBean.class);
        when(
                billingDao.loadUsageLicenses(eq(SUBSCRIPTION_KEY), anyLong(),
                        anyLong())).thenReturn(ulHistories);

        billingInput = new BillingInput.Builder();
        billingInput.setSubscriptionKey(SUBSCRIPTION_KEY);
        billingInput.setBillingPeriodStart(DateTimeHandling
                .calculateMillis("2013-05-01 00:00:00"));
        billingInput.setBillingPeriodEnd(DateTimeHandling
                .calculateMillis("2013-06-01 00:00:00"));
    }

    @Test
    public void computeParameterUserFactorAndRoleFactor_EmptyParameterId() {
        // given
        long startTimeForPeriod = DateTimeHandling
                .calculateMillis("2013-05-01 00:00:00");
        long endTimeForPeriod = DateTimeHandling
                .calculateMillis("2013-06-01 00:00:00");
        parameterData.setPeriod(PricingPeriod.WEEK);

        // when
        calculator.computeParameterUserFactorAndRoleFactor(billingDao,
                billingInput.build(), parameterData, startTimeForPeriod,
                endTimeForPeriod);

        // then
        assertEquals(0, parameterData.getIdData().size());
    }

    @Test
    public void computeParameterUserFactorAndRoleFactor_EmptyUser() {
        // given
        long startTimeForPeriod = DateTimeHandling
                .calculateMillis("2013-05-01 00:00:00");
        long endTimeForPeriod = DateTimeHandling
                .calculateMillis("2013-06-01 00:00:00");

        parameterData.setPeriod(PricingPeriod.WEEK);

        XParameterIdData idData1 = parameterData.getIdDataInstance(
                PARAMETER_ID1, ParameterType.PLATFORM_PARAMETER,
                ParameterValueType.INTEGER);
        addPeriodValueTo(idData1, PERIOD_VALUE_KEY1, "1",
                DateTimeHandling.calculateMillis("2013-05-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-06-01 00:00:00"));

        // when
        calculator.computeParameterUserFactorAndRoleFactor(billingDao,
                billingInput.build(), parameterData, startTimeForPeriod,
                endTimeForPeriod);

        // then
        assertEquals(1, parameterData.getIdData().size());
        XParameterIdData resultIdData1 = parameterData.getIdDataInstance(
                PARAMETER_ID1, ParameterType.PLATFORM_PARAMETER,
                ParameterValueType.INTEGER);
        assertEquals(1, resultIdData1.getPeriodValues().size());

        XParameterPeriodValue resultPeriodValue1 = resultIdData1
                .getPeriodValues().get(0);
        assertEquals(PERIOD_VALUE_KEY1, resultPeriodValue1.getKey().longValue());
        assertEquals(0, resultPeriodValue1.getUserAssignmentFactor(), 0.00001);
        assertEquals(
                0,
                roleFactorOf(ROLE_KEY1, PERIOD_VALUE_KEY1,
                        resultPeriodValue1.getRolePrices()), 0.000001);
    }

    @Test
    public void computeParameterUserFactorAndRoleFactor_MultipleParameterIdsAndMultipleUsers() {
        // given
        long startTimeForPeriod = DateTimeHandling
                .calculateMillis("2013-05-01 00:00:00");
        long endTimeForPeriod = DateTimeHandling
                .calculateMillis("2013-06-01 00:00:00");

        parameterData.setPeriod(PricingPeriod.WEEK);

        XParameterIdData idData1 = parameterData.getIdDataInstance(
                PARAMETER_ID1, ParameterType.PLATFORM_PARAMETER,
                ParameterValueType.INTEGER);
        addPeriodValueTo(idData1, PERIOD_VALUE_KEY2, "2",
                DateTimeHandling.calculateMillis("2013-05-09 00:00:00"),
                DateTimeHandling.calculateMillis("2013-06-01 00:00:00"));
        addPeriodValueTo(idData1, PERIOD_VALUE_KEY1, "1",
                DateTimeHandling.calculateMillis("2013-05-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-05-09 00:00:00"));

        XParameterIdData idData2 = parameterData.getIdDataInstance(
                PARAMETER_ID2, ParameterType.PLATFORM_PARAMETER,
                ParameterValueType.INTEGER);
        addPeriodValueTo(idData2, PERIOD_VALUE_KEY4, "4",
                DateTimeHandling.calculateMillis("2013-05-28 00:00:00"),
                DateTimeHandling.calculateMillis("2013-06-01 00:00:00"));
        addPeriodValueTo(idData2, PERIOD_VALUE_KEY3, "3",
                DateTimeHandling.calculateMillis("2013-05-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-05-28 00:00:00"));

        createUsageLicenseHistory(ModificationType.DELETE,
                DateTimeHandling.calculateDate("2013-05-21 00:00:00"),
                USER_KEY1, USER_ID1, ROLE_KEY1);
        createUsageLicenseHistory(ModificationType.ADD,
                DateTimeHandling.calculateDate("2013-05-16 00:00:00"),
                USER_KEY1, USER_ID1, ROLE_KEY1);
        createUsageLicenseHistory(ModificationType.DELETE,
                DateTimeHandling.calculateDate("2013-05-03 00:00:00"),
                USER_KEY1, USER_ID1, ROLE_KEY1);
        createUsageLicenseHistory(ModificationType.ADD,
                DateTimeHandling.calculateDate("2013-05-01 00:00:00"),
                USER_KEY1, USER_ID1, ROLE_KEY1);
        createUsageLicenseHistory(ModificationType.MODIFY,
                DateTimeHandling.calculateDate("2013-05-23 00:00:00"),
                USER_KEY2, USER_ID2, ROLE_KEY1);
        createUsageLicenseHistory(ModificationType.MODIFY,
                DateTimeHandling.calculateDate("2013-05-15 00:00:00"),
                USER_KEY2, USER_ID2, ROLE_KEY2);
        createUsageLicenseHistory(ModificationType.ADD,
                DateTimeHandling.calculateDate("2013-05-07 00:00:00"),
                USER_KEY2, USER_ID2, ROLE_KEY1);

        // when
        calculator.computeParameterUserFactorAndRoleFactor(billingDao,
                billingInput.build(), parameterData, startTimeForPeriod,
                endTimeForPeriod);

        // then
        assertEquals(2, parameterData.getIdData().size());

        XParameterIdData resultIdData1 = parameterData.getIdDataInstance(
                PARAMETER_ID1, ParameterType.PLATFORM_PARAMETER,
                ParameterValueType.INTEGER);
        assertEquals(2, resultIdData1.getPeriodValues().size());

        XParameterPeriodValue resultPeriodValue2 = resultIdData1
                .getPeriodValues().get(0);
        assertEquals(PERIOD_VALUE_KEY2, resultPeriodValue2.getKey().longValue());
        assertEquals(4.57142857, resultPeriodValue2.getUserAssignmentFactor(),
                0.00001);
        assertEquals(
                3.42857142,
                roleFactorOf(ROLE_KEY1, PERIOD_VALUE_KEY2,
                        resultPeriodValue2.getRolePrices()), 0.000001);
        assertEquals(
                1.14285714,
                roleFactorOf(ROLE_KEY2, PERIOD_VALUE_KEY2,
                        resultPeriodValue2.getRolePrices()), 0.000001);

        XParameterPeriodValue resultPeriodValue1 = resultIdData1
                .getPeriodValues().get(1);
        assertEquals(PERIOD_VALUE_KEY1, resultPeriodValue1.getKey().longValue());
        assertEquals(1.42857142, resultPeriodValue1.getUserAssignmentFactor(),
                0.00001);
        assertEquals(
                1.42857142,
                roleFactorOf(ROLE_KEY1, PERIOD_VALUE_KEY1,
                        resultPeriodValue1.getRolePrices()), 0.000001);
        assertEquals(
                0,
                roleFactorOf(ROLE_KEY2, PERIOD_VALUE_KEY1,
                        resultPeriodValue1.getRolePrices()), 0.000001);

        XParameterIdData resultIdData2 = parameterData.getIdDataInstance(
                PARAMETER_ID2, ParameterType.PLATFORM_PARAMETER,
                ParameterValueType.INTEGER);
        assertEquals(2, resultIdData2.getPeriodValues().size());

        XParameterPeriodValue resultPeriodValue4 = resultIdData2
                .getPeriodValues().get(0);
        assertEquals(PERIOD_VALUE_KEY4, resultPeriodValue4.getKey().longValue());
        assertEquals(0, resultPeriodValue4.getUserAssignmentFactor(), 0.00001);
        assertEquals(
                0,
                roleFactorOf(ROLE_KEY1, PERIOD_VALUE_KEY4,
                        resultPeriodValue4.getRolePrices()), 0.000001);
        assertEquals(
                0,
                roleFactorOf(ROLE_KEY2, PERIOD_VALUE_KEY4,
                        resultPeriodValue4.getRolePrices()), 0.000001);

        XParameterPeriodValue resultPeriodValue3 = resultIdData2
                .getPeriodValues().get(1);
        assertEquals(PERIOD_VALUE_KEY3, resultPeriodValue3.getKey().longValue());
        assertEquals(6, resultPeriodValue3.getUserAssignmentFactor(), 0.00001);
        assertEquals(
                4.85714285,
                roleFactorOf(ROLE_KEY1, PERIOD_VALUE_KEY3,
                        resultPeriodValue3.getRolePrices()), 0.000001);
        assertEquals(
                1.14285714,
                roleFactorOf(ROLE_KEY2, PERIOD_VALUE_KEY3,
                        resultPeriodValue3.getRolePrices()), 0.000001);

    }

    @Test
    public void computeParameterUserFactorAndRoleFactor_BeginingOfFirstTimeSliceBeforeBillingPeriodStart() {
        // given
        long startTimeForPeriod = DateTimeHandling
                .calculateMillis("2013-04-30 00:00:00");
        long endTimeForPeriod = DateTimeHandling
                .calculateMillis("2013-04-31 00:00:00");

        parameterData.setPeriod(PricingPeriod.WEEK);

        XParameterIdData idData1 = parameterData.getIdDataInstance(
                PARAMETER_ID1, ParameterType.PLATFORM_PARAMETER,
                ParameterValueType.INTEGER);
        addPeriodValueTo(idData1, PERIOD_VALUE_KEY1, "1",
                DateTimeHandling.calculateMillis("2013-04-30 00:00:00"),
                DateTimeHandling.calculateMillis("2013-04-31 00:00:00"));

        createUsageLicenseHistory(ModificationType.MODIFY,
                DateTimeHandling.calculateDate("2013-04-30 12:00:00"),
                USER_KEY1, USER_ID1, ROLE_KEY2);
        createUsageLicenseHistory(ModificationType.ADD,
                DateTimeHandling.calculateDate("2013-04-30 00:00:00"),
                USER_KEY1, USER_ID1, ROLE_KEY1);

        // when
        calculator.computeParameterUserFactorAndRoleFactor(billingDao,
                billingInput.build(), parameterData, startTimeForPeriod,
                endTimeForPeriod);

        // then
        assertEquals(1, parameterData.getIdData().size());

        XParameterIdData resultIdData1 = parameterData.getIdDataInstance(
                PARAMETER_ID1, ParameterType.PLATFORM_PARAMETER,
                ParameterValueType.INTEGER);
        assertEquals(1, resultIdData1.getPeriodValues().size());

        XParameterPeriodValue resultPeriodValue1 = resultIdData1
                .getPeriodValues().get(0);
        assertEquals(PERIOD_VALUE_KEY1, resultPeriodValue1.getKey().longValue());
        assertEquals(1, resultPeriodValue1.getUserAssignmentFactor(), 0.00001);
        assertEquals(
                0.21428571,
                roleFactorOf(ROLE_KEY1, PERIOD_VALUE_KEY1,
                        resultPeriodValue1.getRolePrices()), 0.000001);
        assertEquals(
                0.78571428,
                roleFactorOf(ROLE_KEY2, PERIOD_VALUE_KEY1,
                        resultPeriodValue1.getRolePrices()), 0.000001);
    }

    @Test
    public void computeParameterUserFactorAndRoleFactor_UserAssignDeassign() {
        // given
        long startTimeForPeriod = DateTimeHandling
                .calculateMillis("2013-05-09 20:00:00");
        long endTimeForPeriod = DateTimeHandling
                .calculateMillis("2013-05-12 20:00:00");

        parameterData.setPeriod(PricingPeriod.HOUR);

        XParameterIdData idData1 = parameterData.getIdDataInstance(
                PARAMETER_ID1, ParameterType.PLATFORM_PARAMETER,
                ParameterValueType.INTEGER);
        addPeriodValueTo(idData1, PERIOD_VALUE_KEY1, "1",
                DateTimeHandling.calculateMillis("2013-05-09 20:00:00"),
                DateTimeHandling.calculateMillis("2013-05-12 20:00:00"));

        createUsageLicenseHistory(ModificationType.DELETE,
                DateTimeHandling.calculateDate("2013-05-12 11:59:59"),
                USER_KEY1, USER_ID1, ROLE_KEY1);
        createUsageLicenseHistory(ModificationType.ADD,
                DateTimeHandling.calculateDate("2013-05-12 10:00:00"),
                USER_KEY1, USER_ID1, ROLE_KEY1);
        createUsageLicenseHistory(ModificationType.DELETE,
                DateTimeHandling.calculateDate("2013-05-10 10:59:59"),
                USER_KEY1, USER_ID1, ROLE_KEY1);
        createUsageLicenseHistory(ModificationType.ADD,
                DateTimeHandling.calculateDate("2013-05-10 10:00:00"),
                USER_KEY1, USER_ID1, ROLE_KEY1);

        // when
        calculator.computeParameterUserFactorAndRoleFactor(billingDao,
                billingInput.build(), parameterData, startTimeForPeriod,
                endTimeForPeriod);

        // then
        assertEquals(1, parameterData.getIdData().size());

        XParameterIdData resultIdData1 = parameterData.getIdDataInstance(
                PARAMETER_ID1, ParameterType.PLATFORM_PARAMETER,
                ParameterValueType.INTEGER);
        assertEquals(1, resultIdData1.getPeriodValues().size());

        XParameterPeriodValue resultPeriodValue1 = resultIdData1
                .getPeriodValues().get(0);
        assertEquals(PERIOD_VALUE_KEY1, resultPeriodValue1.getKey().longValue());
        assertEquals(3, resultPeriodValue1.getUserAssignmentFactor(), 0);
        assertEquals(
                3,
                roleFactorOf(ROLE_KEY1, PERIOD_VALUE_KEY1,
                        resultPeriodValue1.getRolePrices()), 0);
    }

    @Test
    public void updateRoleFactorForPeriodValue_rolePriceNotDefined() {
        // given
        XParameterIdData idData = new XParameterIdData(null, null, null, null);
        XParameterPeriodEnumType periodValue = new XParameterPeriodEnumType(
                idData);
        XParameterOption option = new XParameterOption(periodValue);
        option.setRolePrices(null);
        periodValue.setParameterOption(option);
        UserAssignmentFactors factors = new UserAssignmentFactors();
        factors.addRoleFactor(new Long(0), 1.0);
        calculator = spy(new CostCalculatorPerUnit());

        // when
        calculator.updateRoleFactorForPeriodValue(periodValue, factors);

        // then
        verify(calculator, times(1)).parameterRolePriceNotDefined(periodValue);
    }

    private void addPeriodValueTo(XParameterIdData idData, long periodValueKey,
            String parameterValue, long startTime, long endTime) {
        XParameterPeriodPrimitiveType type = new XParameterPeriodPrimitiveType(
                idData, createPricingDataForRoleKeys(periodValueKey), null);
        type.setKey(Long.valueOf(periodValueKey));
        type.setValue(parameterValue);
        type.setStartTime(startTime);
        type.setEndTime(endTime);
    }

    private void createUsageLicenseHistory(ModificationType modtype,
            Date moddate, long userKey, String userId, long roleKey) {
        UsageLicenseHistory history = new UsageLicenseHistory();
        history.setModtype(modtype);
        history.setModdate(moddate);
        history.setUserObjKey(userKey);
        history.getDataContainer().setApplicationUserId(userId);
        history.setRoleDefinitionObjKey(Long.valueOf(roleKey));
        ulHistories.add(history);
    }

    private RolePricingData createPricingDataForRoleKeys(long periodValueKey) {
        Map<Long, RolePricingDetails> rolePricingDetails = new HashMap<Long, RolePricingDetails>();
        rolePricingDetails.put(Long.valueOf(ROLE_KEY1),
                new RolePricingDetails());
        rolePricingDetails.put(Long.valueOf(ROLE_KEY2),
                new RolePricingDetails());

        RolePricingData pricingData = new RolePricingData();
        pricingData.addRolePricesForContainerKey(Long.valueOf(periodValueKey),
                rolePricingDetails);
        return pricingData;
    }

    private double roleFactorOf(long roleKey, long periodValueKey,
            RolePricingData rolePricingData) {
        Map<Long, RolePricingDetails> rolePrices = rolePricingData
                .getRolePricesForContainerKey(Long.valueOf(periodValueKey));
        RolePricingDetails pricingDetails = rolePrices.get(Long
                .valueOf(roleKey));
        return pricingDetails.getFactor();
    }
}
