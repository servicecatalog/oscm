/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Jul 8, 2015                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.service.model;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.spy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import org.oscm.domobjects.SubscriptionHistory;
import org.oscm.domobjects.enums.ModificationType;
import org.oscm.test.DateTimeHandling;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;

/**
 * Tests BillingSubscriptionHistoryData functionality
 */
public class BillingSubscriptionHistoryDataTest {

    private BillingSubscriptionHistoryData historyData;

    @Test
    public void testCreation_withSubscriptionHistoriesEmpty() {

        // given
        List<SubscriptionHistory> subscriptionHistories = new ArrayList<>();

        // when
        historyData = spy(new BillingSubscriptionHistoryData(
                subscriptionHistories));

        // then
        assertEquals(true, historyData.getSubscriptionKeys().isEmpty());
    }

    @Test(expected = NullPointerException.class)
    public void testCreation_withSubscriptionHistoriesNull() {

        // given
        List<SubscriptionHistory> subscriptionHistories = null;

        // when
        historyData = spy(new BillingSubscriptionHistoryData(
                subscriptionHistories));

        // then
        // expected=NullPointerException.class
    }

    @Test
    public void testCreation_withSubscriptionHistoriesNotEmpty() {

        // given
        List<SubscriptionHistory> subscriptionHistories = new ArrayList<>();
        addSubscriptionHistory(subscriptionHistories, 1L, 2000L,
                SubscriptionStatus.ACTIVE,
                DateTimeHandling.calculateMillis("2015-02-15 10:30:00"),
                DateTimeHandling.calculateMillis("2015-02-15 10:30:00"));
        addSubscriptionHistory(subscriptionHistories, 2L, 2000L,
                SubscriptionStatus.ACTIVE,
                DateTimeHandling.calculateMillis("2015-02-15 10:30:00"),
                DateTimeHandling.calculateMillis("2015-02-20 12:00:00"));
        addSubscriptionHistory(subscriptionHistories, 3L, 2000L,
                SubscriptionStatus.ACTIVE,
                DateTimeHandling.calculateMillis("2015-02-15 10:30:00"),
                DateTimeHandling.calculateMillis("2015-02-20 12:00:00"));

        // when
        historyData = spy(new BillingSubscriptionHistoryData(
                subscriptionHistories));

        // then
        assertEquals(false, historyData.getSubscriptionKeys().isEmpty());
        assertEquals(1, historyData.getSubscriptionKeys().size());
        assertEquals(false, historyData.getSubscriptionHistoryEntries(2000L)
                .isEmpty());
        assertEquals(3, historyData.getSubscriptionHistoryEntries(2000L).size());
    }

    @Test
    public void testGetSubscriptionKeys() {

        // given
        List<SubscriptionHistory> subscriptionHistories = new ArrayList<>();
        addSubscriptionHistory(subscriptionHistories, 1L, 2000L,
                SubscriptionStatus.ACTIVE,
                DateTimeHandling.calculateMillis("2015-02-15 10:30:00"),
                DateTimeHandling.calculateMillis("2015-02-15 10:30:00"));
        addSubscriptionHistory(subscriptionHistories, 2L, 2000L,
                SubscriptionStatus.ACTIVE,
                DateTimeHandling.calculateMillis("2015-02-15 10:30:00"),
                DateTimeHandling.calculateMillis("2015-02-20 12:00:00"));
        addSubscriptionHistory(subscriptionHistories, 3L, 2000L,
                SubscriptionStatus.ACTIVE,
                DateTimeHandling.calculateMillis("2015-02-15 10:30:00"),
                DateTimeHandling.calculateMillis("2015-02-20 12:00:00"));
        addSubscriptionHistory(subscriptionHistories, 4L, 2001L,
                SubscriptionStatus.ACTIVE,
                DateTimeHandling.calculateMillis("2015-02-15 10:30:00"),
                DateTimeHandling.calculateMillis("2015-02-15 10:30:00"));
        addSubscriptionHistory(subscriptionHistories, 5L, 2001L,
                SubscriptionStatus.ACTIVE,
                DateTimeHandling.calculateMillis("2015-02-15 10:30:00"),
                DateTimeHandling.calculateMillis("2015-02-20 12:00:00"));
        addSubscriptionHistory(subscriptionHistories, 6L, 2002L,
                SubscriptionStatus.ACTIVE,
                DateTimeHandling.calculateMillis("2015-02-15 10:30:00"),
                DateTimeHandling.calculateMillis("2015-02-15 10:30:00"));
        addSubscriptionHistory(subscriptionHistories, 7L, 2002L,
                SubscriptionStatus.ACTIVE,
                DateTimeHandling.calculateMillis("2015-02-15 10:30:00"),
                DateTimeHandling.calculateMillis("2015-02-15 10:30:00"));
        addSubscriptionHistory(subscriptionHistories, 8L, 2003L,
                SubscriptionStatus.ACTIVE,
                DateTimeHandling.calculateMillis("2015-02-15 10:30:00"),
                DateTimeHandling.calculateMillis("2015-02-15 10:30:00"));
        addSubscriptionHistory(subscriptionHistories, 9L, 2003L,
                SubscriptionStatus.ACTIVE,
                DateTimeHandling.calculateMillis("2015-02-15 10:30:00"),
                DateTimeHandling.calculateMillis("2015-02-20 12:00:00"));

        historyData = spy(new BillingSubscriptionHistoryData(
                subscriptionHistories));

        // when
        List<Long> subscriptionKeys = historyData.getSubscriptionKeys();

        // then
        assertEquals(false, subscriptionKeys.isEmpty());
        assertEquals(Arrays.asList(2000L, 2001L, 2002L, 2003L),
                subscriptionKeys);
    }

    @Test
    public void testGetSubscriptionEntries() {

        // given
        List<SubscriptionHistory> subscriptionHistories = new ArrayList<>();
        addSubscriptionHistory(subscriptionHistories, 1L, 2000L,
                SubscriptionStatus.ACTIVE,
                DateTimeHandling.calculateMillis("2015-02-15 10:30:00"),
                DateTimeHandling.calculateMillis("2015-02-15 10:30:00"));
        addSubscriptionHistory(subscriptionHistories, 2L, 2000L,
                SubscriptionStatus.ACTIVE,
                DateTimeHandling.calculateMillis("2015-02-15 10:30:00"),
                DateTimeHandling.calculateMillis("2015-02-20 12:00:00"));
        addSubscriptionHistory(subscriptionHistories, 3L, 2000L,
                SubscriptionStatus.ACTIVE,
                DateTimeHandling.calculateMillis("2015-02-15 10:30:00"),
                DateTimeHandling.calculateMillis("2015-02-20 12:00:00"));
        addSubscriptionHistory(subscriptionHistories, 4L, 2001L,
                SubscriptionStatus.ACTIVE,
                DateTimeHandling.calculateMillis("2015-02-15 10:30:00"),
                DateTimeHandling.calculateMillis("2015-02-15 10:30:00"));
        addSubscriptionHistory(subscriptionHistories, 5L, 2001L,
                SubscriptionStatus.ACTIVE,
                DateTimeHandling.calculateMillis("2015-02-15 10:30:00"),
                DateTimeHandling.calculateMillis("2015-02-20 12:00:00"));
        addSubscriptionHistory(subscriptionHistories, 6L, 2002L,
                SubscriptionStatus.ACTIVE,
                DateTimeHandling.calculateMillis("2015-02-15 10:30:00"),
                DateTimeHandling.calculateMillis("2015-02-15 10:30:00"));
        addSubscriptionHistory(subscriptionHistories, 7L, 2002L,
                SubscriptionStatus.ACTIVE,
                DateTimeHandling.calculateMillis("2015-02-15 10:30:00"),
                DateTimeHandling.calculateMillis("2015-02-15 10:30:00"));
        addSubscriptionHistory(subscriptionHistories, 8L, 2003L,
                SubscriptionStatus.ACTIVE,
                DateTimeHandling.calculateMillis("2015-02-15 10:30:00"),
                DateTimeHandling.calculateMillis("2015-02-15 10:30:00"));
        addSubscriptionHistory(subscriptionHistories, 9L, 2003L,
                SubscriptionStatus.ACTIVE,
                DateTimeHandling.calculateMillis("2015-02-15 10:30:00"),
                DateTimeHandling.calculateMillis("2015-02-20 12:00:00"));

        historyData = spy(new BillingSubscriptionHistoryData(
                subscriptionHistories));

        // when
        List<SubscriptionHistory> entries = historyData
                .getSubscriptionHistoryEntries(Long.valueOf(2001L));

        // then
        assertEquals(false, entries.isEmpty());
        assertEquals(2, entries.size());
        assertEquals(4, entries.get(0).getKey());
        assertEquals(5, entries.get(1).getKey());
    }

    private void addSubscriptionHistory(
            List<SubscriptionHistory> subscriptionHistoryList, long historyKey,
            long subObjKey, SubscriptionStatus status, long activationDate,
            long modDate) {

        subscriptionHistoryList.add(createSubscriptionHistory(historyKey,
                subObjKey, status, activationDate, modDate));
    }

    private SubscriptionHistory createSubscriptionHistory(long historyKey,
            long subObjKey, SubscriptionStatus status, long activationDate,
            long modDate) {

        SubscriptionHistory subHistory = new SubscriptionHistory();
        subHistory.setKey(historyKey);
        subHistory.setObjKey(subObjKey);
        subHistory.getDataContainer().setActivationDate(
                Long.valueOf(activationDate));
        subHistory.setModdate(new Date(modDate));

        if (modDate != activationDate) {
            subHistory.setModtype(ModificationType.ADD);
        } else {
            subHistory.setModtype(ModificationType.MODIFY);
        }

        return subHistory;
    }

}
