/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: May 21, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.calculation.revenue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import org.oscm.billingservice.business.model.billingresult.BasePeriodType;
import org.oscm.billingservice.business.model.billingresult.BillingDetailsType;
import org.oscm.billingservice.business.model.billingresult.DetailedCostsType;
import org.oscm.billingservice.business.model.billingresult.ObjectFactory;
import org.oscm.billingservice.business.model.billingresult.OrganizationDetailsType;
import org.oscm.billingservice.business.model.billingresult.OrganizationalUnitType;
import org.oscm.billingservice.business.model.billingresult.OverallCostsType;
import org.oscm.billingservice.business.model.billingresult.PeriodFeeType;
import org.oscm.billingservice.business.model.billingresult.PriceModelType;
import org.oscm.billingservice.business.model.billingresult.PriceModelsType;
import org.oscm.billingservice.business.model.billingresult.SubscriptionType;
import org.oscm.billingservice.dao.BillingDataRetrievalServiceBean;
import org.oscm.billingservice.service.model.BillingInput;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.BillingResult;
import org.oscm.domobjects.DomainObject;
import org.oscm.domobjects.SubscriptionHistory;
import org.oscm.test.ReflectiveAccess;
import org.oscm.types.exceptions.BillingRunFailed;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;

public class PerformBillingRunForSubscriptionTest {

    private RevenueCalculatorBean bb;
    private DataService dm;
    private Query queryMock;
    private List<DomainObject<?>> persistArguments;

    @Before
    public void setup() {
        bb = spy(new RevenueCalculatorBean());
        bb.bdr = spy(new BillingDataRetrievalServiceBean());
        dm = mock(DataService.class);
        ReflectiveAccess.set(bb.bdr, "dm", dm);
        queryMock = mock(Query.class);
        doReturn(queryMock).when(dm).createNamedQuery(anyString());

        persistArguments = new ArrayList<DomainObject<?>>();
        try {
            doAnswer(new Answer<Void>() {
                @Override
                public Void answer(InvocationOnMock invocation) {
                    Object[] args = invocation.getArguments();
                    persistArguments.add((DomainObject<?>) args[0]);
                    return null;
                }
            }).when(dm).persist(any(DomainObject.class));
        } catch (NonUniqueBusinessKeyException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void noSubscriptions_persist() throws Exception {
        // given
        doReturn(createDocument(false)).when(bb)
                .createBillingDataForOrganization(any(BillingInput.class),
                        any(BillingResult.class));
        doReturn(Long.valueOf(1)).when(queryMock).getSingleResult();

        // when
        BillingResult result = performBillingRunForSubscription(1L,
                new ArrayList<SubscriptionHistory>(), 1L, 1L, 3L, "EUR", true);

        // then
        ArgumentCaptor<BillingResult> removeCaptor = ArgumentCaptor
                .forClass(BillingResult.class);
        verify(dm, times(2)).persist(any(DomainObject.class));
        verify(dm, times(1)).remove(removeCaptor.capture());
        assertSame(result, persistArguments.get(0));
        assertSame(result, removeCaptor.getValue());
    }

    private BillingResult performBillingRunForSubscription(
            long subscriptionKey,
            List<SubscriptionHistory> subscriptionHistoryEntries,
            long organizationKey, long startOfLastMonth, long endOfLastMonth,
            String currencyISOcode, boolean storeResultXML)
            throws ObjectNotFoundException, NonUniqueBusinessKeyException,
            BillingRunFailed {
        BillingInput.Builder billingInput = new BillingInput.Builder();
        billingInput.setCurrencyIsoCode(currencyISOcode);
        billingInput.setOrganizationKey(organizationKey);
        billingInput.setBillingPeriodEnd(endOfLastMonth);
        billingInput.setBillingPeriodStart(startOfLastMonth);
        billingInput.setStoreBillingResult(storeResultXML);
        billingInput.setSubscriptionHistoryEntries(subscriptionHistoryEntries);
        billingInput.setSubscriptionKey(subscriptionKey);
        return bb.performBillingRunForSubscription(billingInput.build());
    }

    @Test
    public void withSubscriptions_persist() throws Exception {
        // given
        doReturn(createDocument(true)).when(bb)
                .createBillingDataForOrganization(any(BillingInput.class),
                        any(BillingResult.class));
        doReturn(Long.valueOf(1)).when(queryMock).getSingleResult();

        // when
        BillingResult result = performBillingRunForSubscription(1L,
                new ArrayList<SubscriptionHistory>(), 1L, 1L, 3L, "EUR", true);

        // then
        verify(dm, times(2)).persist(any(DomainObject.class));
        assertSame(result, persistArguments.get(0));
        verify(dm, never()).remove(any(DomainObject.class));
    }

    @Test
    public void noSubscriptions() throws Exception {
        // given
        doReturn(createDocument(false)).when(bb)
                .createBillingDataForOrganization(any(BillingInput.class),
                        any(BillingResult.class));
        doReturn(Long.valueOf(1)).when(queryMock).getSingleResult();

        // when
        BillingResult result = performBillingRunForSubscription(1L,
                new ArrayList<SubscriptionHistory>(), 1L, 1L, 3L, "EUR", false);

        // then
        assertEquals("", result.getResultXML());
        verify(bb.bdr, never()).persistBillingResult(any(BillingResult.class));
        verify(bb.bdr, never()).removeBillingResult(any(BillingResult.class));
    }

    @Test
    public void withSubscriptions() throws Exception {
        // given
        doReturn(createDocument(true)).when(bb)
                .createBillingDataForOrganization(any(BillingInput.class),
                        any(BillingResult.class));
        doReturn(Long.valueOf(1)).when(queryMock).getSingleResult();

        // when
        performBillingRunForSubscription(1L,
                new ArrayList<SubscriptionHistory>(), 1L, 1L, 3L, "EUR", false);

        // then
        verify(bb.bdr, never()).persistBillingResult(any(BillingResult.class));
        verify(bb.bdr, never()).removeBillingResult(any(BillingResult.class));
    }

    @Test
    public void withSubscriptions_DeactivatedBeforePeriod() throws Exception {
        // given - createBillingDataForOrganization will return null by default
        // which is done when subscription was deactivated before period start
        doReturn(Long.valueOf(1)).when(queryMock).getSingleResult();

        // when
        BillingResult result = performBillingRunForSubscription(1L,
                new ArrayList<SubscriptionHistory>(), 1L, 1L, 3L, "EUR", true);

        // then
        assertEquals("", result.getResultXML());
        ArgumentCaptor<BillingResult> persistCaptor = ArgumentCaptor
                .forClass(BillingResult.class);
        ArgumentCaptor<BillingResult> removeCaptor = ArgumentCaptor
                .forClass(BillingResult.class);
        verify(bb.bdr, times(1)).persistBillingResult(persistCaptor.capture());
        verify(bb.bdr, times(1)).removeBillingResult(removeCaptor.capture());
        assertSame(result, persistCaptor.getValue());
        assertSame(result, removeCaptor.getValue());
    }

    @Test
    public void withSubscriptions_DeactivatedBeforePeriodNoPersist()
            throws Exception {
        // given - createBillingDataForOrganization will return null by default
        // which is done when subscription was deactivated before period start
        doReturn(Long.valueOf(1)).when(queryMock).getSingleResult();

        // when
        BillingResult result = performBillingRunForSubscription(1L,
                new ArrayList<SubscriptionHistory>(), 1L, 1L, 3L, "EUR", false);

        // then
        assertEquals("", result.getResultXML());
        verify(dm, never()).persist(any(DomainObject.class));
        verify(dm, never()).remove(any(DomainObject.class));
    }

    private static BillingDetailsType createDocument(boolean subscriptions) {
        final ObjectFactory factory = new ObjectFactory();
        final BillingDetailsType billingDetails = factory
                .createBillingDetailsType();
        billingDetails.setSubscriptions(factory.createSubscriptionsType());
        billingDetails.setTimezone("UTC");
        if (subscriptions) {
            billingDetails.setPeriod(factory.createPeriodType());

            OrganizationDetailsType organizationDetailsType = factory
                    .createOrganizationDetailsType();
            organizationDetailsType.setAddress("address");
            organizationDetailsType.setEmail("email");
            organizationDetailsType.setName("name");
            organizationDetailsType.setPaymenttype("paymentType");
            billingDetails.setOrganizationDetails(organizationDetailsType);

            SubscriptionType subscriptionType = factory
                    .createSubscriptionType();
            subscriptionType.setId("id");

            OrganizationalUnitType orgUnitType = factory
                    .createOrganizationalUnitType();
            orgUnitType.setName("UnitName");
            subscriptionType.setOrganizationalUnit(orgUnitType);
            PriceModelsType createPriceModelsType = factory
                    .createPriceModelsType();
            subscriptionType.setPriceModels(createPriceModelsType);
            billingDetails.getSubscriptions().getSubscription()
                    .add(subscriptionType);

            PriceModelType pm = factory.createPriceModelType();
            pm.setId("id");
            pm.setUsagePeriod(factory.createPeriodType());
            PeriodFeeType periodFeeType = factory.createPeriodFeeType();
            periodFeeType.setBasePeriod(BasePeriodType.DAY);
            periodFeeType.setBasePrice(BigDecimal.ONE);
            periodFeeType.setFactor(BigDecimal.ONE);
            periodFeeType.setPrice(BigDecimal.ONE);
            pm.setPeriodFee(periodFeeType);
            createPriceModelsType.getPriceModel().add(pm);

            DetailedCostsType createDetailedCostsType = factory
                    .createDetailedCostsType();
            createDetailedCostsType.setAmount(BigDecimal.ONE);
            createDetailedCostsType.setCurrency("EUR");
            createDetailedCostsType.setGrossAmount(BigDecimal.ONE);
            pm.setPriceModelCosts(createDetailedCostsType);

            OverallCostsType createOverallCostsType = factory
                    .createOverallCostsType();
            createOverallCostsType.setCurrency("EUR");
            createOverallCostsType.setGrossAmount(BigDecimal.ONE);
            createOverallCostsType.setNetAmount(BigDecimal.ONE);
            billingDetails.setOverallCosts(createOverallCostsType);
        }
        return billingDetails;
    }

}
