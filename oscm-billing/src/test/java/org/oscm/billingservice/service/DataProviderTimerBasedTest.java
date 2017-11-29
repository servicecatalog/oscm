/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: June 2, 2015                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.service;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import org.oscm.billingservice.dao.BillingDataRetrievalServiceLocal;
import org.oscm.billingservice.dao.model.BillingSubscriptionData;
import org.oscm.billingservice.service.model.BillingInput;
import org.oscm.billingservice.service.model.BillingPeriodData;
import org.oscm.billingservice.service.model.BillingSubscriptionChunk;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.SubscriptionHistory;
import org.oscm.domobjects.SupportedCurrency;
import org.oscm.test.DateTimeHandling;
import org.oscm.internal.types.exception.ObjectNotFoundException;

/**
 * @author baumann
 */
public class DataProviderTimerBasedTest {

    private static final long DAY_IN_MILLIS = 24 * 3600 * 1000;
    private static final String CURRENCY_CODE = "EUR";

    @Mock
    private DataService dm;

    DataProviderTimerBased provider;
    BillingDataRetrievalServiceLocal bdrMock;

    @Before
    public void setup() {
        bdrMock = mock(BillingDataRetrievalServiceLocal.class);
        when(bdrMock.loadCurrency(anyLong(), anyLong())).thenReturn(
                new SupportedCurrency(CURRENCY_CODE));
    }

    @Test
    public void calculateRevenueSharesPeriodStart() {
        // given invocation time and time offset
        long invocationTime = DateTimeHandling
                .calculateMillis("2012-12-05 01:30:00");
        long timeOffset = DAY_IN_MILLIS;

        // when
        provider = new DataProviderTimerBased(invocationTime, timeOffset,
                bdrMock);

        // then expected is first day of the previous month
        assertEquals(DateTimeHandling.calculateMillis("2012-11-01 00:00:00"),
                provider.getPeriodRevenueSharesStart());
    }

    @Test
    public void calculateRevenueSharesPeriodEnd_inMonthFromInvocationTime() {

        // given is invocation time and offset to stay in the same month
        long invocationTime = DateTimeHandling
                .calculateMillis("2012-12-05 01:30:00");
        long timeOffset = DAY_IN_MILLIS;

        // when
        provider = new DataProviderTimerBased(invocationTime, timeOffset,
                bdrMock);

        // then expected is the beginning of the month
        assertEquals(DateTimeHandling.calculateMillis("2012-12-01 00:00:00"),
                provider.getPeriodRevenueSharesEnd());
    }

    @Test
    public void calculateRevenueSharesPeriodEnd_inPreviousMonthFromInvocationTime() {

        // given is invocation time and offset to go to previous month
        long invocationTime = DateTimeHandling
                .calculateMillis("2012-12-01 01:30:00");
        long timeOffset = DAY_IN_MILLIS;

        // when
        provider = new DataProviderTimerBased(invocationTime, timeOffset,
                bdrMock);

        // then expected is the beginning of the previous month
        assertEquals(DateTimeHandling.calculateMillis("2012-11-01 00:00:00"),
                provider.getPeriodRevenueSharesEnd());
    }

    @Test
    public void calculateRevenueSharesPeriodEnd_inPreviousYearFromInvocationTime() {

        // given is invocation time and offset to go to previous year
        long invocationTime = DateTimeHandling
                .calculateMillis("2012-01-01 01:30:00");
        long timeOffset = DAY_IN_MILLIS;

        // when
        provider = new DataProviderTimerBased(invocationTime, timeOffset,
                bdrMock);

        // then expected is the beginning of the month in previous year
        assertEquals(DateTimeHandling.calculateMillis("2011-12-01 00:00:00"),
                provider.getPeriodRevenueSharesEnd());
    }

    @Test
    public void calculateEffectiveBillingEndDate() {

        // given invocation time and time offset
        long invocationTime = DateTimeHandling
                .calculateMillis("2012-12-05 01:30:00");
        long timeOffset = DAY_IN_MILLIS;

        // when
        provider = new DataProviderTimerBased(invocationTime, timeOffset,
                bdrMock);

        // then expected is first day of the previous month
        assertEquals(DateTimeHandling.calculateMillis("2012-12-04 00:00:00"),
                provider.getEffectiveBillingEndDate());
    }

    @Test
    public void calculateEffectiveBillingEndDate_notCutOffDay() {

        // given invocation time and time offset
        long invocationTime = DateTimeHandling
                .calculateMillis("2012-12-30 01:30:00");
        long timeOffset = DAY_IN_MILLIS;

        // when
        provider = new DataProviderTimerBased(invocationTime, timeOffset,
                bdrMock);

        // then expected is the last possible cutOffDay - 28th
        assertEquals(DateTimeHandling.calculateMillis("2012-12-28 00:00:00"),
                provider.getEffectiveBillingEndDate());
    }

    /**
     * Load the billing data for several subscriptions with and without a
     * billing subscription status. Check if the correct billing periods are
     * determined, if they are sorted and if the correct subscription keys are
     * associated.
     */
    @Test
    public void loadBillingData() {
        // given
        long billingInvocationTime = DateTimeHandling
                .calculateMillis("2015-06-03 02:30:00");
        long billingOffset = 2 * DAY_IN_MILLIS;

        provider = spy(new DataProviderTimerBased(billingInvocationTime,
                billingOffset, bdrMock));

        List<BillingSubscriptionData> subscriptionList = new ArrayList<BillingSubscriptionData>();
        addBillingSubscriptionData(subscriptionList, 1,
                DateTimeHandling.calculateMillis("2015-03-10 10:30:00"), 1,
                Long.valueOf(DateTimeHandling
                        .calculateMillis("2015-05-01 00:00:00")));
        addBillingSubscriptionData(subscriptionList, 2,
                DateTimeHandling.calculateMillis("2015-05-20 09:00:00"), 5,
                null);
        addBillingSubscriptionData(subscriptionList, 3,
                DateTimeHandling.calculateMillis("2015-05-20 09:00:00"), 24,
                null);
        addBillingSubscriptionData(subscriptionList, 4,
                DateTimeHandling.calculateMillis("2015-05-20 09:00:00"), 1,
                null);
        addBillingSubscriptionData(subscriptionList, 5,
                DateTimeHandling.calculateMillis("2014-12-02 07:00:00"), 24,
                null);
        addBillingSubscriptionData(subscriptionList, 6,
                DateTimeHandling.calculateMillis("2015-01-05 10:00:00"), 1,
                Long.valueOf(DateTimeHandling
                        .calculateMillis("2015-03-01 00:00:00")));

        when(
                bdrMock.getSubscriptionsForBilling(anyLong(), anyLong(),
                        anyLong())).thenReturn(subscriptionList);

        // when
        List<BillingPeriodData> billingPeriodList = provider.loadBillingData();

        // then
        assertEquals("Wrong size of billing period list", 9,
                billingPeriodList.size());

        checkBililngPeriodOrder(billingPeriodList);
        checkSubscriptionsOrder(billingPeriodList);

        checkBillingPeriodData(billingPeriodList.get(0),
                DateTimeHandling.calculateMillis("2014-11-24 00:00:00"),
                DateTimeHandling.calculateMillis("2014-12-24 00:00:00"),
                Arrays.asList(Long.valueOf(5)));
        checkBillingPeriodData(billingPeriodList.get(1),
                DateTimeHandling.calculateMillis("2014-12-24 00:00:00"),
                DateTimeHandling.calculateMillis("2015-01-24 00:00:00"),
                Arrays.asList(Long.valueOf(5)));
        checkBillingPeriodData(billingPeriodList.get(2),
                DateTimeHandling.calculateMillis("2015-01-24 00:00:00"),
                DateTimeHandling.calculateMillis("2015-02-24 00:00:00"),
                Arrays.asList(Long.valueOf(5)));
        checkBillingPeriodData(billingPeriodList.get(3),
                DateTimeHandling.calculateMillis("2015-02-24 00:00:00"),
                DateTimeHandling.calculateMillis("2015-03-24 00:00:00"),
                Arrays.asList(Long.valueOf(5)));
        checkBillingPeriodData(billingPeriodList.get(4),
                DateTimeHandling.calculateMillis("2015-03-01 00:00:00"),
                DateTimeHandling.calculateMillis("2015-04-01 00:00:00"),
                Arrays.asList(Long.valueOf(6)));
        checkBillingPeriodData(billingPeriodList.get(5),
                DateTimeHandling.calculateMillis("2015-03-24 00:00:00"),
                DateTimeHandling.calculateMillis("2015-04-24 00:00:00"),
                Arrays.asList(Long.valueOf(5)));
        checkBillingPeriodData(billingPeriodList.get(6),
                DateTimeHandling.calculateMillis("2015-04-01 00:00:00"),
                DateTimeHandling.calculateMillis("2015-05-01 00:00:00"),
                Arrays.asList(Long.valueOf(6)));
        checkBillingPeriodData(billingPeriodList.get(7),
                DateTimeHandling.calculateMillis("2015-04-24 00:00:00"),
                DateTimeHandling.calculateMillis("2015-05-24 00:00:00"),
                Arrays.asList(Long.valueOf(3), Long.valueOf(5)));
        checkBillingPeriodData(
                billingPeriodList.get(8),
                DateTimeHandling.calculateMillis("2015-05-01 00:00:00"),
                DateTimeHandling.calculateMillis("2015-06-01 00:00:00"),
                Arrays.asList(Long.valueOf(1), Long.valueOf(4), Long.valueOf(6)));
    }

    /**
     * Load the billing data for several subscriptions with and without a
     * billing subscription status. Check if the correct billing periods are
     * determined, if they are sorted and if the correct subscription keys are
     * associated. Use a small subscription chunk size.
     */
    @Test
    public void loadBillingData_chunk() {
        // given
        long billingInvocationTime = DateTimeHandling
                .calculateMillis("2015-06-03 02:30:00");
        long billingOffset = 2 * DAY_IN_MILLIS;

        provider = spy(new DataProviderTimerBased(billingInvocationTime,
                billingOffset, bdrMock));

        doReturn(Integer.valueOf(3)).when(provider).getSubscriptionChunkSize();

        List<BillingSubscriptionData> subscriptionList = new ArrayList<BillingSubscriptionData>();
        addBillingSubscriptionData(subscriptionList, 1,
                DateTimeHandling.calculateMillis("2015-03-10 10:30:00"), 1,
                Long.valueOf(DateTimeHandling
                        .calculateMillis("2015-05-01 00:00:00")));
        addBillingSubscriptionData(subscriptionList, 2,
                DateTimeHandling.calculateMillis("2015-05-20 09:00:00"), 5,
                null);
        addBillingSubscriptionData(subscriptionList, 3,
                DateTimeHandling.calculateMillis("2015-05-20 09:00:00"), 24,
                null);
        addBillingSubscriptionData(subscriptionList, 4,
                DateTimeHandling.calculateMillis("2015-05-20 09:00:00"), 1,
                null);
        addBillingSubscriptionData(subscriptionList, 5,
                DateTimeHandling.calculateMillis("2014-12-02 07:00:00"), 24,
                null);
        addBillingSubscriptionData(subscriptionList, 6,
                DateTimeHandling.calculateMillis("2015-01-05 10:00:00"), 1,
                Long.valueOf(DateTimeHandling
                        .calculateMillis("2015-03-01 00:00:00")));
        addBillingSubscriptionData(subscriptionList, 7,
                DateTimeHandling.calculateMillis("2015-04-10 10:30:00"), 1,
                Long.valueOf(DateTimeHandling
                        .calculateMillis("2015-05-01 00:00:00")));
        addBillingSubscriptionData(subscriptionList, 8,
                DateTimeHandling.calculateMillis("2015-04-15 10:30:00"), 1,
                Long.valueOf(DateTimeHandling
                        .calculateMillis("2015-05-01 00:00:00")));

        when(
                bdrMock.getSubscriptionsForBilling(anyLong(), anyLong(),
                        anyLong())).thenReturn(subscriptionList);

        // when
        List<BillingPeriodData> billingPeriodList = provider.loadBillingData();

        // then
        assertEquals("Wrong size of billing period list", 10,
                billingPeriodList.size());

        checkBililngPeriodOrder(billingPeriodList);
        checkSubscriptionsOrder(billingPeriodList);

        checkBillingPeriodData(billingPeriodList.get(0),
                DateTimeHandling.calculateMillis("2014-11-24 00:00:00"),
                DateTimeHandling.calculateMillis("2014-12-24 00:00:00"),
                Arrays.asList(Long.valueOf(5)));
        checkBillingPeriodData(billingPeriodList.get(1),
                DateTimeHandling.calculateMillis("2014-12-24 00:00:00"),
                DateTimeHandling.calculateMillis("2015-01-24 00:00:00"),
                Arrays.asList(Long.valueOf(5)));
        checkBillingPeriodData(billingPeriodList.get(2),
                DateTimeHandling.calculateMillis("2015-01-24 00:00:00"),
                DateTimeHandling.calculateMillis("2015-02-24 00:00:00"),
                Arrays.asList(Long.valueOf(5)));
        checkBillingPeriodData(billingPeriodList.get(3),
                DateTimeHandling.calculateMillis("2015-02-24 00:00:00"),
                DateTimeHandling.calculateMillis("2015-03-24 00:00:00"),
                Arrays.asList(Long.valueOf(5)));
        checkBillingPeriodData(billingPeriodList.get(4),
                DateTimeHandling.calculateMillis("2015-03-01 00:00:00"),
                DateTimeHandling.calculateMillis("2015-04-01 00:00:00"),
                Arrays.asList(Long.valueOf(6)));
        checkBillingPeriodData(billingPeriodList.get(5),
                DateTimeHandling.calculateMillis("2015-03-24 00:00:00"),
                DateTimeHandling.calculateMillis("2015-04-24 00:00:00"),
                Arrays.asList(Long.valueOf(5)));
        checkBillingPeriodData(billingPeriodList.get(6),
                DateTimeHandling.calculateMillis("2015-04-01 00:00:00"),
                DateTimeHandling.calculateMillis("2015-05-01 00:00:00"),
                Arrays.asList(Long.valueOf(6)));
        checkBillingPeriodData(billingPeriodList.get(7),
                DateTimeHandling.calculateMillis("2015-04-24 00:00:00"),
                DateTimeHandling.calculateMillis("2015-05-24 00:00:00"),
                Arrays.asList(Long.valueOf(3), Long.valueOf(5)));
        checkBillingPeriodData(
                billingPeriodList.get(8),
                DateTimeHandling.calculateMillis("2015-05-01 00:00:00"),
                DateTimeHandling.calculateMillis("2015-06-01 00:00:00"),
                Arrays.asList(Long.valueOf(1), Long.valueOf(4), Long.valueOf(6)));
        checkBillingPeriodData(billingPeriodList.get(9),
                DateTimeHandling.calculateMillis("2015-05-01 00:00:00"),
                DateTimeHandling.calculateMillis("2015-06-01 00:00:00"),
                Arrays.asList(Long.valueOf(7), Long.valueOf(8)));
    }

    /**
     * Loads the billing data and checks if the correct billing periods are
     * determined, if they are sorted and if the correct subscription keys are
     * associated.
     * 
     * chunk size......................: 3<br>
     * subscription data entries size..: 15<br>
     */
    @Test
    public void loadBillingData_smallChunk() {
        // given
        long billingInvocationTime = DateTimeHandling
                .calculateMillis("2015-06-03 02:30:00");
        long billingOffset = 2 * DAY_IN_MILLIS;

        provider = spy(new DataProviderTimerBased(billingInvocationTime,
                billingOffset, bdrMock));

        doReturn(Integer.valueOf(3)).when(provider).getSubscriptionChunkSize();

        List<BillingSubscriptionData> subscriptionList = new ArrayList<BillingSubscriptionData>();

        for (int i = 1; i < 11; i++) {
            addBillingSubscriptionData(subscriptionList, i,
                    DateTimeHandling.calculateMillis("2015-04-15 10:30:00"), 1,
                    Long.valueOf(DateTimeHandling
                            .calculateMillis("2015-05-01 00:00:00")));
        }

        addBillingSubscriptionData(subscriptionList, 11,
                DateTimeHandling.calculateMillis("2015-04-15 10:30:00"), 1,
                Long.valueOf(DateTimeHandling
                        .calculateMillis("2015-05-01 00:00:01")));

        for (int i = 12; i < 16; i++) {
            addBillingSubscriptionData(subscriptionList, i,
                    DateTimeHandling.calculateMillis("2015-03-15 10:30:00"),
                    15, Long.valueOf(DateTimeHandling
                            .calculateMillis("2015-04-15 00:00:00")));
        }

        when(
                bdrMock.getSubscriptionsForBilling(anyLong(), anyLong(),
                        anyLong())).thenReturn(subscriptionList);

        // when
        List<BillingPeriodData> billingPeriodList = provider.loadBillingData();

        // then
        assertEquals("Wrong size of billing period list", 6,
                billingPeriodList.size());

        checkBililngPeriodOrder(billingPeriodList);
        checkSubscriptionsOrder(billingPeriodList);

        checkBillingPeriodData(
                billingPeriodList.get(0),
                DateTimeHandling.calculateMillis("2015-04-15 00:00:00"),
                DateTimeHandling.calculateMillis("2015-05-15 00:00:00"),
                Arrays.asList(Long.valueOf(12), Long.valueOf(13),
                        Long.valueOf(14)));

        checkBillingPeriodData(billingPeriodList.get(1),
                DateTimeHandling.calculateMillis("2015-04-15 00:00:00"),
                DateTimeHandling.calculateMillis("2015-05-15 00:00:00"),
                Arrays.asList(Long.valueOf(15)));

        checkBillingPeriodData(
                billingPeriodList.get(2),
                DateTimeHandling.calculateMillis("2015-05-01 00:00:00"),
                DateTimeHandling.calculateMillis("2015-06-01 00:00:00"),
                Arrays.asList(Long.valueOf(1), Long.valueOf(2), Long.valueOf(3)));

        checkBillingPeriodData(
                billingPeriodList.get(3),
                DateTimeHandling.calculateMillis("2015-05-01 00:00:00"),
                DateTimeHandling.calculateMillis("2015-06-01 00:00:00"),
                Arrays.asList(Long.valueOf(4), Long.valueOf(5), Long.valueOf(6)));

        checkBillingPeriodData(
                billingPeriodList.get(4),
                DateTimeHandling.calculateMillis("2015-05-01 00:00:00"),
                DateTimeHandling.calculateMillis("2015-06-01 00:00:00"),
                Arrays.asList(Long.valueOf(7), Long.valueOf(8), Long.valueOf(9)));

        checkBillingPeriodData(billingPeriodList.get(5),
                DateTimeHandling.calculateMillis("2015-05-01 00:00:00"),
                DateTimeHandling.calculateMillis("2015-06-01 00:00:00"),
                Arrays.asList(Long.valueOf(10)));

    }

    /**
     * Loads the billing data and checks if the correct billing periods are
     * determined, if they are sorted and if the correct subscription keys are
     * associated.
     * 
     * chunk size......................: 10<br>
     * subscription data entries size..: 24<br>
     */
    @Test
    public void loadBillingData_mediumChunk() {
        // given
        long billingInvocationTime = DateTimeHandling
                .calculateMillis("2015-06-03 02:30:00");
        long billingOffset = 2 * DAY_IN_MILLIS;

        provider = spy(new DataProviderTimerBased(billingInvocationTime,
                billingOffset, bdrMock));

        doReturn(Integer.valueOf(10)).when(provider).getSubscriptionChunkSize();

        List<BillingSubscriptionData> subscriptionList = new ArrayList<BillingSubscriptionData>();

        for (int i = 1; i < 25; i++) {
            addBillingSubscriptionData(subscriptionList, i,
                    DateTimeHandling.calculateMillis("2015-04-15 10:30:00"), 1,
                    Long.valueOf(DateTimeHandling
                            .calculateMillis("2015-05-01 00:00:00")));
        }

        when(
                bdrMock.getSubscriptionsForBilling(anyLong(), anyLong(),
                        anyLong())).thenReturn(subscriptionList);

        // when
        List<BillingPeriodData> billingPeriodList = provider.loadBillingData();

        // then
        assertEquals("Wrong size of billing period list", 3,
                billingPeriodList.size());

        checkBililngPeriodOrder(billingPeriodList);
        checkSubscriptionsOrder(billingPeriodList);

        checkBillingPeriodData(billingPeriodList.get(0),
                DateTimeHandling.calculateMillis("2015-05-01 00:00:00"),
                DateTimeHandling.calculateMillis("2015-06-01 00:00:00"),
                Arrays.asList(Long.valueOf(1), Long.valueOf(2),
                        Long.valueOf(3), Long.valueOf(4), Long.valueOf(5),
                        Long.valueOf(6), Long.valueOf(7), Long.valueOf(8),
                        Long.valueOf(9), Long.valueOf(10)));
        checkBillingPeriodData(billingPeriodList.get(1),
                DateTimeHandling.calculateMillis("2015-05-01 00:00:00"),
                DateTimeHandling.calculateMillis("2015-06-01 00:00:00"),
                Arrays.asList(Long.valueOf(11), Long.valueOf(12),
                        Long.valueOf(13), Long.valueOf(14), Long.valueOf(15),
                        Long.valueOf(16), Long.valueOf(17), Long.valueOf(18),
                        Long.valueOf(19), Long.valueOf(20)));
        checkBillingPeriodData(
                billingPeriodList.get(2),
                DateTimeHandling.calculateMillis("2015-05-01 00:00:00"),
                DateTimeHandling.calculateMillis("2015-06-01 00:00:00"),
                Arrays.asList(Long.valueOf(21), Long.valueOf(22),
                        Long.valueOf(23), Long.valueOf(24)));

    }

    /**
     * Loads the billing data and checks if the correct billing periods are
     * determined, if they are sorted and if the correct subscription keys are
     * associated.
     * 
     * chunk size......................: 20<br>
     * subscription data entries size..: 44<br>
     */
    @Test
    public void loadBillingData_largeChunk() {

        // given
        long billingInvocationTime = DateTimeHandling
                .calculateMillis("2015-06-03 02:30:00");
        long billingOffset = 2 * DAY_IN_MILLIS;

        provider = spy(new DataProviderTimerBased(billingInvocationTime,
                billingOffset, bdrMock));

        doReturn(Integer.valueOf(20)).when(provider).getSubscriptionChunkSize();

        List<BillingSubscriptionData> subscriptionList = new ArrayList<BillingSubscriptionData>();

        for (int i = 1; i < 25; i++) {
            addBillingSubscriptionData(subscriptionList, i,
                    DateTimeHandling.calculateMillis("2015-04-15 10:30:00"), 1,
                    Long.valueOf(DateTimeHandling
                            .calculateMillis("2015-05-01 00:00:00")));
        }

        for (int i = 2500; i < 2520; i++) {
            addBillingSubscriptionData(subscriptionList, i,
                    DateTimeHandling.calculateMillis("2015-04-15 10:30:00"), 1,
                    null);
        }

        when(
                bdrMock.getSubscriptionsForBilling(anyLong(), anyLong(),
                        anyLong())).thenReturn(subscriptionList);

        // when
        List<BillingPeriodData> billingPeriodList = provider.loadBillingData();

        // then
        assertEquals("Wrong size of billing period list", 4,
                billingPeriodList.size());

        checkBililngPeriodOrder(billingPeriodList);
        checkSubscriptionsOrder(billingPeriodList);

        checkBillingPeriodData(
                billingPeriodList.get(0),
                DateTimeHandling.calculateMillis("2015-04-01 00:00:00"),
                DateTimeHandling.calculateMillis("2015-05-01 00:00:00"),
                Arrays.asList(Long.valueOf(2500), Long.valueOf(2501),
                        Long.valueOf(2502), Long.valueOf(2503),
                        Long.valueOf(2504), Long.valueOf(2505),
                        Long.valueOf(2506), Long.valueOf(2507),
                        Long.valueOf(2508), Long.valueOf(2509),
                        Long.valueOf(2510), Long.valueOf(2511),
                        Long.valueOf(2512), Long.valueOf(2513),
                        Long.valueOf(2514), Long.valueOf(2515),
                        Long.valueOf(2516), Long.valueOf(2517),
                        Long.valueOf(2518), Long.valueOf(2519)));
        checkBillingPeriodData(billingPeriodList.get(1),
                DateTimeHandling.calculateMillis("2015-05-01 00:00:00"),
                DateTimeHandling.calculateMillis("2015-06-01 00:00:00"),
                Arrays.asList(Long.valueOf(1), Long.valueOf(2),
                        Long.valueOf(3), Long.valueOf(4), Long.valueOf(5),
                        Long.valueOf(6), Long.valueOf(7), Long.valueOf(8),
                        Long.valueOf(9), Long.valueOf(10), Long.valueOf(11),
                        Long.valueOf(12), Long.valueOf(13), Long.valueOf(14),
                        Long.valueOf(15), Long.valueOf(16), Long.valueOf(17),
                        Long.valueOf(18), Long.valueOf(19), Long.valueOf(20)));
        checkBillingPeriodData(billingPeriodList.get(2),
                DateTimeHandling.calculateMillis("2015-05-01 00:00:00"),
                DateTimeHandling.calculateMillis("2015-06-01 00:00:00"),
                Arrays.asList(Long.valueOf(21), Long.valueOf(22),
                        Long.valueOf(23), Long.valueOf(24), Long.valueOf(2500),
                        Long.valueOf(2501), Long.valueOf(2502),
                        Long.valueOf(2503), Long.valueOf(2504),
                        Long.valueOf(2505), Long.valueOf(2506),
                        Long.valueOf(2507), Long.valueOf(2508),
                        Long.valueOf(2509), Long.valueOf(2510),
                        Long.valueOf(2511), Long.valueOf(2512),
                        Long.valueOf(2513), Long.valueOf(2514),
                        Long.valueOf(2515)));
        checkBillingPeriodData(
                billingPeriodList.get(3),
                DateTimeHandling.calculateMillis("2015-05-01 00:00:00"),
                DateTimeHandling.calculateMillis("2015-06-01 00:00:00"),
                Arrays.asList(Long.valueOf(2516), Long.valueOf(2517),
                        Long.valueOf(2518), Long.valueOf(2519)));

    }

    /**
     * Loads the subscription histories and checks if the correct billing inputs
     * are created
     */
    @Test
    public void testGetSubscriptionHistories() {

        // given
        long billingInvocationTime = DateTimeHandling
                .calculateMillis("2015-06-03 02:30:00");
        long billingOffset = 2 * DAY_IN_MILLIS;

        long billingPeriodStart = DateTimeHandling
                .calculateMillis("2015-05-13 00:00:00");
        long billingPeriodEnd = DateTimeHandling
                .calculateMillis("2015-06-13 00:00:00");

        List<Long> subscriptionKeys = Arrays.asList(Long.valueOf(12009),
                Long.valueOf(21111), Long.valueOf(3009));

        BillingPeriodData billingPeriodData = new BillingPeriodData(
                billingPeriodStart, billingPeriodEnd, subscriptionKeys);

        List<SubscriptionHistory> subscriptionHistoryList = new ArrayList<SubscriptionHistory>();

        addSubscriptionHistory(subscriptionHistoryList, 12009,
                DateTimeHandling.calculateMillis("2015-02-15 10:30:00"));
        addSubscriptionHistory(subscriptionHistoryList, 21111,
                DateTimeHandling.calculateMillis("2015-04-18 10:30:00"));
        addSubscriptionHistory(subscriptionHistoryList, 3009,
                DateTimeHandling.calculateMillis("2015-03-03 10:30:00"));
        addSubscriptionHistory(subscriptionHistoryList, 3009,
                DateTimeHandling.calculateMillis("2015-03-03 10:30:00"));
        addSubscriptionHistory(subscriptionHistoryList, 3009,
                DateTimeHandling.calculateMillis("2015-03-03 10:30:00"));

        provider = spy(new DataProviderTimerBased(billingInvocationTime,
                billingOffset, bdrMock));

        doReturn(subscriptionHistoryList).when(bdrMock)
                .loadSubscriptionHistoriesForBillingPeriod(subscriptionKeys,
                        billingPeriodStart, billingPeriodEnd);

        try {
            dm = mock(DataService.class);
            doThrow(new ObjectNotFoundException()).when(dm)
                    .getReference(Mockito.eq(Subscription.class), anyLong());
        } catch (ObjectNotFoundException e) {
            e.printStackTrace();
        }

        // when
        BillingSubscriptionChunk subscriptionHistories = provider
                .getSubscriptionHistories(billingPeriodData, dm);

        // then
        checkBillingInput(subscriptionHistories.getBillingInputList().get(0),
                billingPeriodStart, billingPeriodEnd, 3009, 3);
        checkBillingInput(subscriptionHistories.getBillingInputList().get(1),
                billingPeriodStart, billingPeriodEnd, 12009, 1);
        checkBillingInput(subscriptionHistories.getBillingInputList().get(2),
                billingPeriodStart, billingPeriodEnd, 21111, 1);
    }

    private void addBillingSubscriptionData(
            List<BillingSubscriptionData> subscriptionList,
            long subscriptionKey, long activationDate, int cutOffDay,
            Long endOfLastBilledPeriod) {
        subscriptionList.add(createBillingSubscriptionData(subscriptionKey,
                activationDate, cutOffDay, endOfLastBilledPeriod));
    }

    private BillingSubscriptionData createBillingSubscriptionData(
            long subscriptionKey, long activationDate, int cutOffDay,
            Long endOfLastBilledPeriod) {
        BillingSubscriptionData billingSubscriptionData = new BillingSubscriptionData();
        billingSubscriptionData.setSubscriptionKey(subscriptionKey);
        billingSubscriptionData.setActivationDate(activationDate);
        billingSubscriptionData.setCutOffDay(cutOffDay);
        billingSubscriptionData.setEndOfLastBilledPeriod(endOfLastBilledPeriod);
        return billingSubscriptionData;
    }

    private void addSubscriptionHistory(
            List<SubscriptionHistory> subscriptionHistoryList,
            long subscriptionKey, long activationDate) {

        subscriptionHistoryList.add(createSubscriptionHistory(subscriptionKey,
                activationDate));
    }

    private SubscriptionHistory createSubscriptionHistory(long subscriptionKey,
            long activationDate) {

        Subscription subscription = new Subscription();
        subscription.setKey(subscriptionKey);
        subscription.setActivationDate(activationDate);

        return new SubscriptionHistory(subscription);
    }

    private void checkBillingPeriodData(BillingPeriodData billingPeriodData,
            long billingPeriodStart, long billingPeriodEnd,
            List<Long> subscriptionKeys) {
        assertEquals("Wrong billing period start", billingPeriodStart,
                billingPeriodData.getBillingPeriodStart());
        assertEquals("Wrong billing period end", billingPeriodEnd,
                billingPeriodData.getBillingPeriodEnd());
        assertEquals("Wrong subscriptionList", subscriptionKeys,
                billingPeriodData.getSubscriptionKeys());
    }

    private void checkBillingInput(BillingInput billingInput,
            long billingPeriodStart, long billingPeriodEnd,
            long subscriptionKey, int numOfHistoryEntries) {
        assertEquals("Wrong billing period start", billingPeriodStart,
                billingInput.getBillingPeriodStart());
        assertEquals("Wrong billing period end", billingPeriodEnd,
                billingInput.getBillingPeriodEnd());
        assertEquals("Wrong subscription key", subscriptionKey,
                billingInput.getSubscriptionKey());
        assertEquals("Wrong subscription history entries", numOfHistoryEntries,
                billingInput.getSubscriptionHistoryEntries().size());
        System.out.println(billingInput.getCutOffDate());
    }

    private void checkBililngPeriodOrder(List<BillingPeriodData> billingPeriods) {

        long previousBillinPeriodStart = 0;

        for (BillingPeriodData billingPeriod : billingPeriods) {

            long billingPeriodStart = billingPeriod.getBillingPeriodStart();

            assertTrue("Wrong billing period start order",
                    billingPeriodStart >= previousBillinPeriodStart);
            previousBillinPeriodStart = billingPeriodStart;
        }
    }

    private void checkSubscriptionsOrder(List<BillingPeriodData> billingPeriods) {

        for (BillingPeriodData billingPeriod : billingPeriods) {

            List<Long> subscriptionKeys = billingPeriod.getSubscriptionKeys();

            Long previouskey = 0L;
            for (Long key : subscriptionKeys) {
                assertTrue("Wrong billing period subscriptions order",
                        key > previouskey);
                previouskey = key;
            }
        }
    }

}
