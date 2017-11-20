/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 27.08.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.calculation.revenue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import org.oscm.billingservice.business.calculation.revenue.model.PriceModelInput;
import org.oscm.billingservice.business.model.billingresult.BillingDetailsType;
import org.oscm.billingservice.business.model.billingresult.PriceModelsType;
import org.oscm.billingservice.dao.BillingDataRetrievalServiceBean;
import org.oscm.billingservice.dao.model.OrganizationAddressData;
import org.oscm.billingservice.dao.model.VatRateDetails;
import org.oscm.billingservice.dao.model.XParameterData;
import org.oscm.billingservice.dao.model.XParameterIdData;
import org.oscm.billingservice.dao.model.XParameterPeriodPrimitiveType;
import org.oscm.billingservice.service.model.BillingInput;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.BillingResult;
import org.oscm.domobjects.PriceModelHistory;
import org.oscm.domobjects.ProductHistory;
import org.oscm.domobjects.SubscriptionHistory;
import org.oscm.domobjects.UsageLicenseHistory;
import org.oscm.domobjects.UserGroupHistory;
import org.oscm.test.DateTimeHandling;
import org.oscm.test.ReflectiveAccess;
import org.oscm.types.exceptions.BillingRunFailed;
import org.oscm.internal.types.enumtypes.ParameterType;
import org.oscm.internal.types.enumtypes.ParameterValueType;
import org.oscm.internal.types.enumtypes.PriceModelType;
import org.oscm.internal.types.enumtypes.PricingPeriod;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;

/**
 * @author weiser
 * 
 */
public class CreateBillingDataForOrganizationTest {

    // Java double has a precision of 15 significant digits.
    private final static double ASSERT_DOUBLE_DELTA = 0.000000000000009D;

    private RevenueCalculatorBean bb;
    private PriceModelHistory pmh;
    private ProductHistory ph;
    private DataService dm;

    @Before
    public void setup() throws Exception {
        bb = spy(new RevenueCalculatorBean());
        bb.bdr = mock(BillingDataRetrievalServiceBean.class);
        dm = mock(DataService.class);
        ReflectiveAccess.set(bb.bdr, "dm", dm);

        doReturn(new UserGroupHistory()).when(bb.bdr).getLastValidGroupHistory(
                anyLong(), anyLong());
        when(
                bb.bdr.loadOrganizationBillingDataFromHistory(anyLong(),
                        anyLong())).thenReturn(
                new OrganizationAddressData("a", "n", "e", "id"));
        when(bb.bdr.loadVATForCustomer(anyLong(), anyLong(), anyLong()))
                .thenReturn(new VatRateDetails());

        doReturn(Long.valueOf(1)).when(bb.bdr)
                .loadPriceModelKeyForSubscriptionHistory(
                        any(SubscriptionHistory.class));

        pmh = new PriceModelHistory();
        pmh.setType(PriceModelType.PRO_RATA);
        pmh.setPeriod(PricingPeriod.WEEK);
        pmh.setFreePeriod(0);
        pmh.setModdate(new Date());

        doReturn(pmh).when(bb.bdr).loadOldestPriceModelHistory(anyLong(),
                anyLong());
        when(bb.bdr.loadLatestPriceModelHistory(any(SubscriptionHistory.class)))
                .thenReturn(pmh);

        doReturn(new Date(0)).when(bb.bdr).loadPriceModelStartDate(anyLong());

        ph = new ProductHistory();
        ph.getDataContainer().setProductId("AService");
        when(
                bb.bdr.loadProductTemplateHistoryForSubscriptionHistory(
                        any(SubscriptionHistory.class), anyLong())).thenReturn(
                ph);
    }

    @Test
    public void createBillingDataForOrganization_EmptyList() throws Exception {
        // given
        // when
        BillingDetailsType doc = createBillingDataForOrganization(1, 1, 10, 20,
                0, new BillingResult(), true,
                new ArrayList<SubscriptionHistory>(), "EUR");

        // then
        assertNull(doc);
        verifyZeroInteractions(bb.bdr, dm);
    }

    private BillingDetailsType createBillingDataForOrganization(
            long organizationKey, long subscriptionKey, long startOfPeriod,
            long endOfPeriod, long cutOffDate, BillingResult billingResult,
            boolean storeResultXML, List<SubscriptionHistory> subHistEntries,
            String currency) throws BillingRunFailed {
        BillingInput.Builder billingInput = new BillingInput.Builder();
        billingInput.setOrganizationKey(organizationKey);
        billingInput.setSubscriptionKey(subscriptionKey);
        billingInput.setBillingPeriodStart(startOfPeriod);
        billingInput.setBillingPeriodEnd(endOfPeriod);
        billingInput.setCutOffDate(cutOffDate);
        billingInput.setStoreBillingResult(storeResultXML);
        billingInput.setSubscriptionHistoryEntries(subHistEntries);
        billingInput.setCurrencyIsoCode(currency);

        return bb.createBillingDataForOrganization(billingInput.build(),
                billingResult);
    }

    @Test
    public void createBillingDataForOrganization_ProRataDeactivatedBeforePeriod()
            throws Exception {
        // given
        long billingPeriodStart = DateTimeHandling
                .calculateMillis("2013-03-01 00:00:00");
        long billingPeriodEnd = DateTimeHandling
                .calculateMillis("2013-04-01 00:00:00");

        SubscriptionHistory sh = new SubscriptionHistory();
        sh.setModdate(DateTimeHandling.calculateDate("2013-02-28 23:59:59"));
        sh.setObjKey(1);
        sh.setOrganizationObjKey(1);
        sh.getDataContainer().setStatus(SubscriptionStatus.DEACTIVATED);

        // when
        BillingDetailsType doc = createBillingDataForOrganization(1, 1,
                billingPeriodStart, billingPeriodEnd, 0, new BillingResult(),
                true, Arrays.asList(sh), "EUR");

        // then
        assertNull(doc);
    }

    @Test
    public void createBillingDataForOrganization_FreeOfChargeDeactivatedBeforePeriod()
            throws Exception {
        // given
        pmh.setType(PriceModelType.FREE_OF_CHARGE);

        long billingPeriodStart = DateTimeHandling
                .calculateMillis("2013-03-01 00:00:00");
        long billingPeriodEnd = DateTimeHandling
                .calculateMillis("2013-04-01 00:00:00");

        SubscriptionHistory sh = new SubscriptionHistory();
        sh.setModdate(DateTimeHandling.calculateDate("2013-02-28 23:59:59"));
        sh.setObjKey(1);
        sh.setOrganizationObjKey(1);
        sh.getDataContainer().setStatus(SubscriptionStatus.DEACTIVATED);

        // when
        BillingDetailsType doc = createBillingDataForOrganization(1, 1,
                billingPeriodStart, billingPeriodEnd, 0, new BillingResult(),
                true, Arrays.asList(sh), "EUR");

        // then
        assertNull(doc);
    }

    @Test
    public void createBillingDataForOrganization_PerUnitDeactivatedBeforePeriod()
            throws Exception {
        // given
        pmh.setType(PriceModelType.PER_UNIT);

        long billingPeriodStart = DateTimeHandling
                .calculateMillis("2013-03-01 00:00:00");
        long billingPeriodEnd = DateTimeHandling
                .calculateMillis("2013-04-01 00:00:00");

        SubscriptionHistory sh = new SubscriptionHistory();
        sh.setModdate(DateTimeHandling.calculateDate("2013-02-24 23:59:59"));
        sh.setObjKey(1);
        sh.setOrganizationObjKey(1);
        sh.getDataContainer().setStatus(SubscriptionStatus.DEACTIVATED);

        // when
        BillingDetailsType doc = createBillingDataForOrganization(1, 1,
                billingPeriodStart, billingPeriodEnd, 0, new BillingResult(),
                true, Arrays.asList(sh), "EUR");

        // then
        assertNull(doc);
    }

    @Test
    public void createBillingDataForOrganization_PerUnitDeactivatedBetweenTimeUnitStartAndBillingStart()
            throws Exception {

        // given
        pmh.setType(PriceModelType.PER_UNIT);

        doAnswer(new Answer<BigDecimal>() {
            @Override
            public BigDecimal answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments();
                PriceModelsType pmType = (PriceModelsType) args[2];
                pmType.getPriceModel()
                        .add(new org.oscm.billingservice.business.model.billingresult.PriceModelType());
                return CostCalculator.ZERO_NORMALIZED;
            }
        }).when(bb).billPriceModel(any(BillingInput.class),
                any(PriceModelInput.class), any(PriceModelsType.class),
                any(BillingResult.class));

        long billingPeriodStart = DateTimeHandling
                .calculateMillis("2013-03-01 00:00:00");
        long billingPeriodEnd = DateTimeHandling
                .calculateMillis("2013-04-01 00:00:00");

        SubscriptionHistory sh1 = new SubscriptionHistory();
        sh1.setModdate(DateTimeHandling.calculateDate("2013-02-15 03:00:00"));
        sh1.setObjKey(1);
        sh1.setOrganizationObjKey(1);
        sh1.getDataContainer().setActivationDate(
                Long.valueOf(DateTimeHandling
                        .calculateMillis("2013-02-15 03:00:00")));
        sh1.getDataContainer().setStatus(SubscriptionStatus.ACTIVE);
        sh1.setUserGroupObjKey(Long.valueOf(100L));

        SubscriptionHistory sh2 = new SubscriptionHistory();
        sh2.setModdate(DateTimeHandling.calculateDate("2013-02-27 13:11:00"));
        sh2.setObjKey(1);
        sh2.setOrganizationObjKey(1);
        sh2.getDataContainer().setActivationDate(
                Long.valueOf(DateTimeHandling
                        .calculateMillis("2013-02-15 03:00:00")));
        sh2.getDataContainer().setStatus(SubscriptionStatus.DEACTIVATED);
        sh2.setUserGroupObjKey(Long.valueOf(200L));

        // when
        BillingDetailsType doc = createBillingDataForOrganization(1, 1,
                billingPeriodStart, billingPeriodEnd, 0, new BillingResult(),
                true, Arrays.asList(sh2, sh1), "EUR");

        // then
        assertNotNull(doc);
        assertEquals(1, doc.getSubscriptions().getSubscription().size());
        assertEquals(0.0, doc.getOverallCosts().getGrossAmount().doubleValue(),
                0);
    }

    @Test
    public void createBillingDataForOrganization_DeactivatedInPeriod()
            throws Exception {
        // given
        long billingPeriodStart = DateTimeHandling
                .calculateMillis("2013-03-01 00:00:00");
        long billingPeriodEnd = DateTimeHandling
                .calculateMillis("2013-04-01 00:00:00");

        SubscriptionHistory sh1 = new SubscriptionHistory();
        sh1.setModdate(DateTimeHandling.calculateDate("2013-02-15 03:00:00"));
        sh1.setObjKey(1);
        sh1.setOrganizationObjKey(1);
        sh1.getDataContainer().setActivationDate(
                Long.valueOf(DateTimeHandling
                        .calculateMillis("2013-02-15 03:00:00")));
        sh1.getDataContainer().setStatus(SubscriptionStatus.ACTIVE);
        sh1.setUserGroupObjKey(Long.valueOf(100L));

        SubscriptionHistory sh2 = new SubscriptionHistory();
        sh2.setModdate(DateTimeHandling.calculateDate("2013-03-15 13:00:00"));
        sh2.setObjKey(1);
        sh2.setOrganizationObjKey(1);
        sh2.getDataContainer().setActivationDate(
                Long.valueOf(DateTimeHandling
                        .calculateMillis("2013-02-15 03:00:00")));
        sh2.getDataContainer().setStatus(SubscriptionStatus.DEACTIVATED);
        sh2.setUserGroupObjKey(Long.valueOf(200L));

        // when
        BillingDetailsType doc = createBillingDataForOrganization(1, 1,
                billingPeriodStart, billingPeriodEnd, 0, new BillingResult(),
                true, Arrays.asList(sh1, sh2), "EUR");

        // then
        assertNotNull(doc);
        assertEquals(1, doc.getSubscriptions().getSubscription().size());
        assertEquals(0.0, doc.getOverallCosts().getGrossAmount().doubleValue(),
                0);
    }

    /**
     * Calculates the factors for period fee, user assignment and parameter
     * assignment.
     * 
     * Note: The cut off day is the 26, we calculate from 22.2. until 26.2. For
     * pro rata calculation the month has 31 days starting 26.1. until 26.2. All
     * factors must be calculated on that base, not on 29 days which the
     * February 2012 has!
     */
    @Test
    public void createBillingDataForOrganization_CuttOffDay26()
            throws Exception {
        // given
        SubscriptionHistory sh1 = new SubscriptionHistory();
        sh1.setModdate(new Date(DateTimeHandling
                .calculateMillis("2012-02-24 00:00:00")));
        sh1.setObjKey(1);
        sh1.setCutOffDay(26);
        sh1.setOrganizationObjKey(1);
        sh1.getDataContainer().setActivationDate(
                Long.valueOf(sh1.getModdate().getTime()));
        sh1.getDataContainer().setStatus(SubscriptionStatus.ACTIVE);
        sh1.setUserGroupObjKey(Long.valueOf(100L));

        pmh.setPeriod(PricingPeriod.MONTH);
        UsageLicenseHistory ul = new UsageLicenseHistory();
        ul.setModdate(new Date(DateTimeHandling
                .calculateMillis("2012-02-25 00:00:00")));
        when(bb.bdr.loadUsageLicenses(anyLong(), anyLong(), anyLong()))
                .thenReturn(Arrays.asList(ul));
        XParameterData pd = new XParameterData();
        pd.getIdDataInstance("ert", ParameterType.SERVICE_PARAMETER,
                ParameterValueType.INTEGER);
        XParameterIdData id = pd.getIdData().iterator().next();
        XParameterPeriodPrimitiveType v = new XParameterPeriodPrimitiveType(id,
                null, null);
        v.setValue("1");
        v.setStartTime(DateTimeHandling.calculateMillis("2012-02-25 12:00:00"));
        v.setEndTime(DateTimeHandling.calculateMillis("2012-02-26 00:00:00"));
        id.getPeriodValues().add(v);
        pd.setPeriod(PricingPeriod.MONTH);
        when(
                bb.bdr.loadParameterData(any(BillingInput.class),
                        any(PriceModelInput.class))).thenReturn(pd);

        // when
        BillingDetailsType doc = createBillingDataForOrganization(1, 1,
                DateTimeHandling.calculateMillis("2012-02-22 00:00:00"),
                DateTimeHandling.calculateMillis("2012-02-26 00:00:00"),
                DateTimeHandling.calculateMillis("2012-01-26 00:00:00"),
                new BillingResult(), true, Arrays.asList(sh1), "EUR");

        // then
        assertNotNull(doc);
        assertEquals(1, doc.getSubscriptions().getSubscription().size());
        org.oscm.billingservice.business.model.billingresult.PriceModelType pm = doc
                .getSubscriptions().getSubscription().get(0).getPriceModels()
                .getPriceModel().get(0);
        assertEquals(0.06896551724137931, pm.getPeriodFee().getFactor()
                .doubleValue(), ASSERT_DOUBLE_DELTA);
        assertEquals(0.0344827586206897, pm.getUserAssignmentCosts()
                .getFactor().doubleValue(), ASSERT_DOUBLE_DELTA);
        assertEquals(0.0172413793103448,
                pm.getParameters().getParameter().get(0).getPeriodFee()
                        .getFactor().doubleValue(), ASSERT_DOUBLE_DELTA);
        assertEquals(0.0, doc.getOverallCosts().getGrossAmount().doubleValue(),
                0);
    }

    /* Bug10095 */
    @Test
    public void createBillingDataForOrganization_SubscriptionBeforeBillingStartTime()
            throws Exception {
        // given

        SubscriptionHistory sh1 = new SubscriptionHistory();
        sh1.setModdate(new Date(DateTimeHandling
                .calculateMillis("2012-01-25 00:00:00")));
        sh1.setObjKey(1);
        sh1.setCutOffDay(26);
        sh1.setOrganizationObjKey(1);
        sh1.getDataContainer().setActivationDate(
                Long.valueOf(sh1.getModdate().getTime()));
        sh1.getDataContainer().setStatus(SubscriptionStatus.DEACTIVATED);
        sh1.setUserGroupObjKey(Long.valueOf(100L));

        SubscriptionHistory sh2 = new SubscriptionHistory();
        sh2.setModdate(new Date(DateTimeHandling
                .calculateMillis("2012-01-24 00:00:00")));
        sh2.setObjKey(1);
        sh2.setCutOffDay(26);
        sh2.setOrganizationObjKey(1);
        sh2.getDataContainer().setActivationDate(
                Long.valueOf(sh2.getModdate().getTime()));
        sh2.getDataContainer().setStatus(SubscriptionStatus.ACTIVE);
        sh2.setUserGroupObjKey(Long.valueOf(200L));

        pmh.setType(PriceModelType.PER_UNIT);
        pmh.setPeriod(PricingPeriod.MONTH);
        pmh.setModdate(new Date());

        XParameterData pd = new XParameterData();
        pd.getIdDataInstance("ert", ParameterType.SERVICE_PARAMETER,
                ParameterValueType.INTEGER);
        XParameterIdData id = pd.getIdData().iterator().next();
        XParameterPeriodPrimitiveType v = new XParameterPeriodPrimitiveType(id,
                null, null);
        v.setValue("1");
        v.setStartTime(DateTimeHandling.calculateMillis("2012-01-24 00:00:00"));
        v.setEndTime(DateTimeHandling.calculateMillis("2012-01-25 00:00:00"));
        id.getPeriodValues().add(v);
        pd.setPeriod(PricingPeriod.MONTH);
        when(
                bb.bdr.loadParameterData(any(BillingInput.class),
                        any(PriceModelInput.class))).thenReturn(pd);

        // when
        BillingDetailsType doc = createBillingDataForOrganization(1, 1,
                DateTimeHandling.calculateMillis("2012-01-26 00:00:00"),
                DateTimeHandling.calculateMillis("2012-02-26 00:00:00"),
                DateTimeHandling.calculateMillis("2012-01-26 00:00:00"),
                new BillingResult(), true, Arrays.asList(sh1, sh2), "EUR");

        // then
        assertNotNull(doc);
        assertEquals(1, doc.getSubscriptions().getSubscription().size());
        org.oscm.billingservice.business.model.billingresult.PriceModelType pm = doc
                .getSubscriptions().getSubscription().get(0).getPriceModels()
                .getPriceModel().get(0);
        assertEquals(BigDecimal.valueOf(1.0), pm.getPeriodFee().getFactor());
        assertEquals(1.0322D, pm.getParameters().getParameter().get(0)
                .getPeriodFee().getFactor().doubleValue(), 0.001);
    }
}
