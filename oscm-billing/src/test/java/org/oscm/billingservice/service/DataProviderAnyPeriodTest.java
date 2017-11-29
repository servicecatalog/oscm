/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Dec 17, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.service;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import java.util.*;

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
import org.oscm.test.DateTimeHandling;
import org.oscm.types.exceptions.BillingRunFailed;

/**
 * Tests the functionality of the provider.
 * 
 * @author muenz
 * 
 */
public class DataProviderAnyPeriodTest {

    private static final String CURRENCY_CODE = "EUR";

    private static final long ORGANIZATION_KEY = 1000l;

    DataProviderAnyPeriod provider;

    BillingDataRetrievalServiceLocal bdrMock;

    private DataService dm;

    @Before
    public void setup() throws ObjectNotFoundException {
        bdrMock = mock(BillingDataRetrievalServiceLocal.class);
        when(bdrMock.loadCurrency(anyLong(), anyLong()))
                .thenReturn(new SupportedCurrency(CURRENCY_CODE));
        dm = mock(DataService.class);
        when(dm.getReference(eq(Subscription.class), anyLong()))
                .thenReturn(new Subscription());
    }

    @Test
    public void loadBillingInputList_invariantsCheck() {

        // bdr not set
        long VALID = 10;
        try {
            provider = new DataProviderAnyPeriod(null, VALID, VALID, VALID,
                    false);
        } catch (Exception e) {
            assertTrue(e instanceof NullPointerException);
        }

        // period start not set
        long INVALID = 0;
        try {
            provider = new DataProviderAnyPeriod(bdrMock, INVALID, VALID,
                    VALID, false);
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
        }

        // period end not set
        try {
            provider = new DataProviderAnyPeriod(bdrMock, VALID, INVALID,
                    VALID, false);
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
        }

        // organization key not set
        try {
            provider = new DataProviderAnyPeriod(bdrMock, VALID, VALID,
                    INVALID, false);
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
        }

    }

    /**
     * Tests the constructor behavior of the provider. Period start, end and
     * invocation time must be set. The customer data should also be loaded in
     * constructor.
     * 
     * @throws BillingRunFailed
     */
    @Test
    public void loadBillingInputList() {
        // given
        when(
                bdrMock.loadSubscriptionsForCustomer(anyLong(), anyLong(),
                        anyLong(), anyInt())).thenReturn(
                new ArrayList<SubscriptionHistory>());

        long periodStart = DateTimeHandling
                .calculateMillis("2012-02-15 00:00:00");
        long periodEnd = DateTimeHandling
                .calculateMillis("2012-03-31 23:59:59");
        long organizationKey = 1000l;

        // when
        provider = new DataProviderAnyPeriod(bdrMock, periodStart, periodEnd,
                organizationKey, false);

        // then
        verify(bdrMock).loadSubscriptionsForCustomer(eq(ORGANIZATION_KEY),
                eq(DateTimeHandling.calculateMillis("2012-02-15 00:00:00")),
                eq(periodEnd), anyInt());

        assertEquals(0, provider.getBillingInput().size());
        assertEquals(periodStart, provider.getPeriodStart());
        assertEquals(periodEnd, provider.getPeriodEnd());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void loadBillingInputListUnits() {
        // given
        when(
                bdrMock.loadSubscriptionsForCustomer(anyLong(),
                        any(List.class), anyLong(), anyLong(), anyInt()))
                .thenReturn(new ArrayList<SubscriptionHistory>());

        long periodStart = DateTimeHandling
                .calculateMillis("2012-02-15 00:00:00");
        long periodEnd = DateTimeHandling
                .calculateMillis("2012-03-31 23:59:59");
        long organizationKey = 1000l;
        List<Long> unitKeys = Arrays.asList(Long.valueOf(100L),
                Long.valueOf(200L));

        // when
        provider = new DataProviderAnyPeriod(bdrMock, periodStart, periodEnd,
                organizationKey, unitKeys, false, null);

        // then
        verify(bdrMock).loadSubscriptionsForCustomer(eq(organizationKey),
                eq(unitKeys),
                eq(DateTimeHandling.calculateMillis("2012-02-15 00:00:00")),
                eq(periodEnd), anyInt());

        assertEquals(0, provider.getBillingInput().size());
        assertEquals(periodStart, provider.getPeriodStart());
        assertEquals(periodEnd, provider.getPeriodEnd());
    }

    /**
     * Tests the lazy loading of billing input data. The billing inputs will be
     * checked for correct values and whether the
     * {@link BillingDataRetrievalServiceLocal} interface is called.
     * 
     * @throws BillingRunFailed
     */
    @Test
    public void loadBillingInputList_inputOutputCheck() {
        // given
        long subscriptionKey = 1l;
        int historyEntryCount = 5;
        long periodStart = System.currentTimeMillis() - 100000L;
        long periodEnd = periodStart + 200000L;

        when(
                bdrMock.loadSubscriptionsForCustomer(anyLong(), anyLong(),
                        anyLong(), anyInt())).thenReturn(
                createSubscriptionWithHistory(subscriptionKey,
                        historyEntryCount));

        List<PriceModelHistory> givenPriceModels = givenPriceModelHistories(PriceModelType.PRO_RATA);
        when(
                bdrMock.loadPricemodelHistoriesForSubscriptionHistory(
                        anyLong(), anyLong())).thenReturn(givenPriceModels);

        // when
        provider = new DataProviderAnyPeriod(bdrMock, periodStart, periodEnd,
                ORGANIZATION_KEY, null, false, dm);

        // then
        verify(bdrMock).loadCurrency(eq(subscriptionKey), eq(periodEnd));
        assertEquals(1, provider.getBillingInput().size());
        BillingInput bi = provider.getBillingInput().get(0);
        assertEquals(subscriptionKey, bi.getSubscriptionKey());
        assertEquals(ORGANIZATION_KEY, bi.getOrganizationKey());
        assertEquals(CURRENCY_CODE, bi.getCurrencyIsoCode());
        String s = String
                .valueOf(bdrMock.loadSubscriptionsForCustomer(0, 0, 0, 0).get(0)
                        .getModdate().getTime());
        s = s.substring(0, s.length() - 3) + "000";
        assertEquals(s, String.valueOf(bi.getBillingPeriodStart()));
        assertEquals(periodEnd, bi.getBillingPeriodEnd());
        assertNotNull(bi.getSubscriptionHistoryEntries());
        assertEquals(historyEntryCount, bi.getSubscriptionHistoryEntries()
                .size());
    }

    private List<PriceModelHistory> givenPriceModelHistories(PriceModelType type) {
        List<PriceModelHistory> priceModelHistories = new LinkedList<>();
        PriceModelHistory pmh = new PriceModelHistory();
        pmh.setType(type);
        priceModelHistories.add(pmh);
        return priceModelHistories;
    }

    /**
     * Tests the lazy loading of billing input data. The billing inputs will be
     * checked for correct values and whether the
     * {@link BillingDataRetrievalServiceLocal} interface is called.
     * 
     * @throws BillingRunFailed
     */
    @Test
    public void loadBillingInputList_longPeriod() {
        // given
        final long periodStart = DateTimeHandling
                .calculateMillis("2012-02-15 00:00:00");
        final long periodEnd = DateTimeHandling
                .calculateMillis("2013-01-31 23:59:59");
        final List<SubscriptionHistory> subHistoryEntries = new ArrayList<>();
        subHistoryEntries.add(create(1, "2012-01-05 00:00:00", 6));
        subHistoryEntries.add(create(2, "2012-07-15 03:00:00", 15));
        subHistoryEntries.add(create(3, "2012-01-06 06:00:00", 4));
        subHistoryEntries.add(create(4, "2012-01-15 06:00:00", 7));
        subHistoryEntries.add(create(5, "2012-01-15 06:00:00", 18));
        when(
                bdrMock.loadSubscriptionsForCustomer(anyLong(), anyLong(),
                        anyLong(), anyInt())).thenReturn(subHistoryEntries);
        List<PriceModelHistory> givenPriceModels = givenPriceModelHistories(PriceModelType.PRO_RATA);
        when(
                bdrMock.loadPricemodelHistoriesForSubscriptionHistory(
                        anyLong(), anyLong())).thenReturn(givenPriceModels);

        // when
        provider = new DataProviderAnyPeriod(bdrMock, periodStart, periodEnd,
                ORGANIZATION_KEY, null, false, dm);

        // then
        assertEquals(56, provider.getBillingInput().size());
        validate(0, 1, 6, 3, periodStart, periodEnd);
        validate(12, 2, 15, 8,
                DateTimeHandling.calculateMillis("2012-07-15 03:00:00"),
                periodEnd);
        validate(19, 3, 4, 3, periodStart, periodEnd);
        validate(31, 4, 7, 3, periodStart, periodEnd);
        validate(43, 5, 18, 2, periodStart, periodEnd);
    }

    /**
     * Tests the lazy loading of billing input data. The billing inputs will be
     * checked for correct values and whether the
     * {@link BillingDataRetrievalServiceLocal} interface is called.
     * 
     * @throws BillingRunFailed
     */
    @Test
    public void loadBillingInputList_asPreview() {
        // given
        final long periodStart = DateTimeHandling
                .calculateMillis("2012-03-01 00:00:00");
        final long periodEnd = DateTimeHandling
                .calculateMillis("2012-04-01 00:00:00");
        final List<SubscriptionHistory> subHistoryEntries = new ArrayList<>();
        subHistoryEntries.add(create(1, "2012-01-05 00:00:00", 6));
        subHistoryEntries.add(create(2, "2012-07-15 03:00:00", 15));
        subHistoryEntries.add(create(3, "2012-01-06 06:00:00", 4));
        subHistoryEntries.add(create(4, "2012-01-15 06:00:00", 7));
        subHistoryEntries.add(create(5, "2012-01-15 06:00:00", 18));
        when(
                bdrMock.loadSubscriptionsForCustomer(anyLong(), anyLong(),
                        anyLong(), anyInt())).thenReturn(subHistoryEntries);

        List<PriceModelHistory> givenPriceModels = givenPriceModelHistories(PriceModelType.PRO_RATA);
        when(
                bdrMock.loadPricemodelHistoriesForSubscriptionHistory(
                        anyLong(), anyLong())).thenReturn(givenPriceModels);

        // when
        provider = new DataProviderAnyPeriod(bdrMock, periodStart, periodEnd,
                ORGANIZATION_KEY, null, true, dm);

        // then
        assertEquals(4, provider.getBillingInput().size());
        validatePreview(0, 1, 6);
        validatePreview(1, 3, 4);
        validatePreview(2, 4, 7);
        validatePreview(3, 5, 18);
    }

    private void validate(int offset, long subscriptionKey, int cutOffDay,
            int month, long periodStart, long periodEnd) {
        // if cutOffDay is bigger than day of billing period start, the first
        // period ends within the same month, otherwise it ends in the next
        // month, e.g. billing period start at 15.2., cut off day on the 18. The
        // first period goes from 15.2. until the 18.2. With cut off day 6 it
        // goes from 15.2. until 6.3.
        BillingInput bi = provider.getBillingInput().get(offset);
        assertEquals(ORGANIZATION_KEY, bi.getOrganizationKey());
        assertEquals(CURRENCY_CODE, bi.getCurrencyIsoCode());
        assertEquals(subscriptionKey, bi.getSubscriptionKey());
        assertEquals(periodStart, bi.getBillingPeriodStart());
        assertEquals(
                DateTimeHandling.calculateMillis("2012-" + month + "-"
                        + cutOffDay + " 00:00:00"), bi.getBillingPeriodEnd());
        assertEquals(1, bi.getSubscriptionHistoryEntries().size());

        bi = provider.getBillingInput().get(offset + 3);
        assertEquals(ORGANIZATION_KEY, bi.getOrganizationKey());
        assertEquals(CURRENCY_CODE, bi.getCurrencyIsoCode());
        assertEquals(subscriptionKey, bi.getSubscriptionKey());
        assertEquals(
                DateTimeHandling.calculateMillis("2012-" + (month + 2) + "-"
                        + cutOffDay + " 00:00:00"), bi.getBillingPeriodStart());
        assertEquals(
                DateTimeHandling.calculateMillis("2012-" + (month + 3) + "-"
                        + cutOffDay + " 00:00:00"), bi.getBillingPeriodEnd());
        assertEquals(1, bi.getSubscriptionHistoryEntries().size());

        bi = provider.getBillingInput().get(offset + 11 - month + 3);
        assertEquals(ORGANIZATION_KEY, bi.getOrganizationKey());
        assertEquals(CURRENCY_CODE, bi.getCurrencyIsoCode());
        assertEquals(subscriptionKey, bi.getSubscriptionKey());
        assertEquals(
                DateTimeHandling.calculateMillis("2013-01-" + cutOffDay
                        + " 00:00:00"), bi.getBillingPeriodStart());
        assertEquals(1, bi.getSubscriptionHistoryEntries().size());
        assertEquals(periodEnd, bi.getBillingPeriodEnd());
    }

    private void validatePreview(int offset, long subscriptionKey, int cutOffDay) {
        BillingInput bi = provider.getBillingInput().get(offset);
        assertEquals(ORGANIZATION_KEY, bi.getOrganizationKey());
        assertEquals(CURRENCY_CODE, bi.getCurrencyIsoCode());
        assertEquals(subscriptionKey, bi.getSubscriptionKey());
        assertEquals(
                DateTimeHandling.calculateMillis("2012-03-" + cutOffDay
                        + " 00:00:00"), bi.getBillingPeriodStart());
        assertEquals(DateTimeHandling.calculateMillis("2012-04-01 00:00:00"),
                bi.getBillingPeriodEnd());
        assertEquals(1, bi.getSubscriptionHistoryEntries().size());
    }

    private List<SubscriptionHistory> createSubscriptionWithHistory(
            long subscriptionKey, int historyEntryCount) {
        Random random = new Random();
        long modTime1 = System.currentTimeMillis() - random.nextInt(1000);
        List<SubscriptionHistory> subHistoryEntries = new ArrayList<>();

        // subscription
        Subscription sub = new Subscription();
        sub.setCutOffDay(6);
        sub.setKey(subscriptionKey);

        // subscription history for subscription
        for (int i = 0; i < historyEntryCount; i++) {
            SubscriptionHistory sh = create(subscriptionKey, modTime1, 6);
            subHistoryEntries.add(sh);
        }
        return subHistoryEntries;
    }

    private SubscriptionHistory create(long key, String modificationDate,
            int cutOffDay) {
        return create(key, DateTimeHandling.calculateMillis(modificationDate),
                cutOffDay);
    }

    private SubscriptionHistory create(long key, long modificationDate,
            int cutOffDay) {
        final Subscription subscription = new Subscription();
        subscription.setCutOffDay(cutOffDay);
        subscription.setKey(key);
        final SubscriptionHistory sh = new SubscriptionHistory(subscription);
        sh.setCutOffDay(subscription.getCutOffDay());
        sh.setModdate(new Date(modificationDate));
        return sh;
    }

    @Test
    public void determineEndTimeForPaymentPreview_priceModelIsFreeOfCharge() {
        // given
        long givenEndPeriod = 1000;
        DataProviderAnyPeriod dataProvider = new DataProviderAnyPeriod(bdrMock,
                1, givenEndPeriod, 1, true);
        List<PriceModelHistory> priceModelHistories = givenPriceModelHistories(PriceModelType.FREE_OF_CHARGE);

        // when
        long billingEndPeriod = dataProvider.determineEndTimeForPaymentPreview(
                0, givenEndPeriod, priceModelHistories);

        // then
        assertEquals(givenEndPeriod, billingEndPeriod);
    }

    @Test
    public void determineEndTimeForPaymentPreview_priceModelIsChargeable() {
        // given
        long initialEndPeriod = 1000;
        DataProviderAnyPeriod dataProvider = spy(new DataProviderAnyPeriod(
                bdrMock, 1, initialEndPeriod, 1, true));
        List<PriceModelHistory> priceModelHistories = givenPriceModelHistories(PriceModelType.PER_UNIT);
        long expectedEndPeriod = mockCalculateEndTimeForChargeablePriceModels(dataProvider);

        // when
        long billingEndPeriod = dataProvider.determineEndTimeForPaymentPreview(
                0, initialEndPeriod, priceModelHistories);

        // then
        assertEquals(expectedEndPeriod, billingEndPeriod);
    }

    private long mockCalculateEndTimeForChargeablePriceModels(
            DataProviderAnyPeriod dataProvider) {
        long endPeriod = 4632786;
        doReturn(Long.valueOf(endPeriod)).when(dataProvider)
                .calculateEndTimeForPaymentPreview(anyLong(),
                        any(Calendar.class), any(PriceModelHistory.class));
        return endPeriod;
    }

    @Test
    public void determineEndTimeForPaymentPreview() {
        // given
        long initialEndPeriod = 1000;
        DataProviderAnyPeriod dataProvider = spy(new DataProviderAnyPeriod(
                bdrMock, 1, initialEndPeriod, 1, true));
        List<PriceModelHistory> priceModelHistories = givenChargeableAndFreePriceModels();
        long expectedEndPeriod = mockCalculateEndTimeForChargeablePriceModels(dataProvider);

        // when
        long billingEndPeriod = dataProvider.determineEndTimeForPaymentPreview(
                0, initialEndPeriod, priceModelHistories);

        // then
        assertEquals(billingEndPeriod, expectedEndPeriod);
    }

    private List<PriceModelHistory> givenChargeableAndFreePriceModels() {
        List<PriceModelHistory> priceModelHistories = new LinkedList<>();
        PriceModelHistory pmh = new PriceModelHistory();
        pmh.setType(PriceModelType.FREE_OF_CHARGE);
        priceModelHistories.add(pmh);

        PriceModelHistory pmh2 = new PriceModelHistory();
        pmh2.setType(PriceModelType.PER_UNIT);
        priceModelHistories.add(pmh2);
        return priceModelHistories;
    }
}
