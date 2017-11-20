/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Dec 13, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.service;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.oscm.billingservice.dao.BillingDataRetrievalServiceLocal;
import org.oscm.billingservice.service.model.BillingInput;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.PriceModelHistory;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.SubscriptionHistory;
import org.oscm.domobjects.SupportedCurrency;
import org.oscm.internal.types.enumtypes.PriceModelType;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.test.DateAsserts;
import org.oscm.test.DateTimeHandling;

/**
 * @author muenz
 * 
 */
public class PaymentPreviewProviderTest {

    private static final String CURRENCY_CODE = "EUR";

    private static final long ORGANIZATION_KEY = 1000l;

    DataProviderAnyPeriod provider;

    BillingDataRetrievalServiceLocal bdrMock;

    private DataService dm;

    @Before
    public void setup() throws ObjectNotFoundException {
        bdrMock = mock(BillingDataRetrievalServiceLocal.class);
        dm = mock(DataService.class);
        when(dm.getReference(eq(Subscription.class), anyLong()))
                .thenReturn(new Subscription());
    }

    @Test
    public void testBillingAnyPeriodProvider_invariantsCheck() {

        // bdr not set
        try {
            long VALID = 10;
            new DataProviderAnyPeriod(null, 0, 0, VALID, true);
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof NullPointerException);
        }

        // organization key not set
        try {
            long INVALID = 0;
            new DataProviderAnyPeriod(bdrMock, 1, 2, INVALID, true);
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
        }

    }

    /**
     * Tests the constructor. Should set start, end of period, period
     * calculator, organization key and load billing input list.
     */
    @Test
    public void constructor_ShouldLoadBillingInputListAndSetSomeValues() {
        // given
        DateTimeHandling.defineInvocationTime("2012-12-14 11:00:00");

        when(
                bdrMock.loadSubscriptionsForCustomer(anyLong(), anyLong(),
                        anyLong(), anyInt())).thenReturn(
                new ArrayList<SubscriptionHistory>());
        when(bdrMock.loadCurrency(anyLong(), anyLong())).thenReturn(
                new SupportedCurrency(CURRENCY_CODE));

        // when
        provider = new DataProviderAnyPeriod(bdrMock, 1, 2, ORGANIZATION_KEY,
                true);

        // then
        assertTrue(provider.getPeriodStart() > 0);
        assertTrue(provider.getPeriodEnd() > 0);
        verify(bdrMock).loadSubscriptionsForCustomer(eq(ORGANIZATION_KEY),
                anyLong(), eq(provider.getPeriodEnd()),
                anyInt());
        assertNotNull(provider.getBillingInput());
        assertEquals(0, provider.getBillingInput().size());
    }

    @Test
    public void getEnd_ShouldReturnTheInvocationTime() {
        // given
        long end = DateTimeHandling.calculateMillis("2012-12-14 11:00:00");
        provider = new DataProviderAnyPeriod(bdrMock, 1, end, ORGANIZATION_KEY,
                true);

        // when
        end = provider.getPeriodEnd();

        // then
        assertEquals(DateTimeHandling.calculateMillis("2012-12-14 11:00:00"),
                end);
    }

    @Test
    public void getStart_ShouldReturnTheInvocationTimeMinusOneMonthAtZeroOclock() {
        // given
        long start = DateTimeHandling
                .defineInvocationTime("2012-12-14 11:00:00");
        provider = new DataProviderAnyPeriod(bdrMock, start, start + 2,
                ORGANIZATION_KEY, true);

        // when
        long start2 = provider.getPeriodStart();

        // then
        assertEquals(start, start2);
    }

    /**
     * Tests whether the list of {@link BillingInput} is filled correctly.
     */
    @SuppressWarnings("boxing")
    @Test
    public void getBillingInputList_CheckBillingInputCorrectlyFilled() {
        // given
        long subscriptionKey = 1l;
        long now = DateTimeHandling.defineInvocationTime("2012-12-14 11:00:00");
        List<SubscriptionHistory> subscriptionHistoryEntries = new ArrayList<>();
        SubscriptionHistory subscriptionHistory1 = createSubscriptionHistory(
                createSubscription(subscriptionKey), 3,
                DateTimeHandling.calculateMillis("2012-12-11 15:00:00"));
        subscriptionHistoryEntries.add(subscriptionHistory1);
        subscriptionHistoryEntries.add(createSubscriptionHistory(
                createSubscription(1l), 5,
                DateTimeHandling.calculateMillis("2012-12-14 08:00:00")));
        when(
                bdrMock.loadSubscriptionsForCustomer(anyLong(), anyLong(),
                        anyLong(), anyInt())).thenReturn(
                subscriptionHistoryEntries);
        when(bdrMock.loadCurrency(anyLong(), anyLong())).thenReturn(
                new SupportedCurrency(CURRENCY_CODE));

        List<PriceModelHistory> priceModelHistories = new LinkedList<>();
        PriceModelHistory priceModelHistory = new PriceModelHistory();
        priceModelHistory.setType(PriceModelType.PRO_RATA);
        priceModelHistories.add(priceModelHistory);
        when(
                bdrMock.loadPricemodelHistoriesForSubscriptionHistory(
                        subscriptionHistory1.getObjKey(), now)).thenReturn(
                priceModelHistories);

        // must be constructed after mock definitions
        provider = new DataProviderAnyPeriod(bdrMock, 1, now, ORGANIZATION_KEY,
                null, true, dm);

        // when
        List<BillingInput> billingInputs = provider.getBillingInput();

        // then - expected are the following:
        // * subscription key == 1
        // * one billing input
        // * subscription period start day must be cutoff day 5 (latest)
        // * subscription period end must be invocation time
        // * organization key must be filled
        // * currency code must be CURRENCY_CODE
        // * store flag must be false
        assertEquals(1, billingInputs.size());
        BillingInput bi = billingInputs.get(0);
        assertEquals(bi.isStoreBillingResult(), false);
        assertEquals(subscriptionKey, bi.getSubscriptionKey());
        assertEquals(subscriptionHistoryEntries,
                bi.getSubscriptionHistoryEntries());
        assertEquals(ORGANIZATION_KEY, bi.getOrganizationKey());
        assertEquals(CURRENCY_CODE, bi.getCurrencyIsoCode());
        DateAsserts.assertDates(now, bi.getBillingPeriodEnd());
        DateAsserts.assertDates(subscriptionHistory1.getModdate().getTime(),
                bi.getBillingPeriodStart());

    }

    private Subscription createSubscription(long key) {
        Subscription sub = new Subscription();
        sub.setKey(key);
        return sub;
    }

    private SubscriptionHistory createSubscriptionHistory(
            Subscription subscription, int cutOffDay, long modificationDate) {
        SubscriptionHistory sh = new SubscriptionHistory(subscription);
        sh.setCutOffDay(cutOffDay);
        sh.setModdate(new Date(modificationDate));
        return sh;
    }
}
