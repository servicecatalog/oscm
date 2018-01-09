/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.billingservice.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.oscm.billingservice.service.model.CustomerData;
import org.oscm.domobjects.SubscriptionHistory;

/**
 * Test cases for class <code>BillingData</code>
 * 
 * @author cheld
 * 
 */
public class CustomerDataTest {

    CustomerData data;
    List<SubscriptionHistory> subscriptionHistoryEntries = new ArrayList<SubscriptionHistory>();

    @Before
    public void before() {
        // History entry for subscription 1
        SubscriptionHistory sh1 = new SubscriptionHistory();
        sh1.setKey(1234);
        sh1.setObjKey(1);
        sh1.setCutOffDay(1);
        sh1.setModdate(new Date(1000));

        // History entry for subscription 2
        SubscriptionHistory sh2 = new SubscriptionHistory();
        sh2.setKey(32456);
        sh2.setObjKey(2);
        sh2.setCutOffDay(2);
        sh2.setModdate(new Date(2000));

        // History entry for subscription 1
        SubscriptionHistory sh3 = new SubscriptionHistory();
        sh3.setKey(9678);
        sh3.setObjKey(1);
        sh3.setCutOffDay(3);
        sh3.setModdate(new Date(3000));

        // History entry for subscription 3
        SubscriptionHistory sh4 = new SubscriptionHistory();
        sh4.setKey(3001);
        sh4.setObjKey(3);
        sh4.setCutOffDay(4);
        sh4.setModdate(new Date(4000));

        // History entry for subscription 3
        SubscriptionHistory sh5 = new SubscriptionHistory();
        sh5.setKey(3002);
        sh5.setObjKey(3);
        sh5.setCutOffDay(5);
        sh5.setModdate(new Date(5000));

        // History entry for subscription 3
        SubscriptionHistory sh6 = new SubscriptionHistory();
        sh6.setKey(3003);
        sh6.setObjKey(3);
        sh6.setCutOffDay(6);
        sh6.setModdate(new Date(6000));

        subscriptionHistoryEntries.add(sh1);
        subscriptionHistoryEntries.add(sh2);
        subscriptionHistoryEntries.add(sh3);
        subscriptionHistoryEntries.add(sh4);
        subscriptionHistoryEntries.add(sh6);
        subscriptionHistoryEntries.add(sh5);
        data = new CustomerData(subscriptionHistoryEntries);
    }

    /**
     * Extract unique subscription keys
     */
    @Test
    public void getSubscriptionKeys() {
        Collection<Long> subscriptionKeys = data.getSubscriptionKeys();
        assertEquals(3, subscriptionKeys.size());
        assertTrue(subscriptionKeys.contains(Long.valueOf(1)));
        assertTrue(subscriptionKeys.contains(Long.valueOf(2)));
    }

    /**
     * No error must occur one empty list
     */
    @Test
    public void getSubscriptionKeys_empty() {
        @SuppressWarnings("unchecked")
        CustomerData data = new CustomerData(Collections.EMPTY_LIST);
        assertTrue(data.getSubscriptionKeys().isEmpty());
    }

    /**
     * Extract sublist of SubscriptionHistory objects for the given subscription
     * key
     */
    @Test
    public void getSubscriptionHistoryEntries() {
        List<SubscriptionHistory> subList = data
                .getSubscriptionHistoryEntries(1);
        assertEquals(2, subList.size());
        List<SubscriptionHistory> subList2 = data
                .getSubscriptionHistoryEntries(2);
        assertEquals(1, subList2.size());
    }

    /**
     * Sorting must be kept
     */
    @Test
    public void getSubscriptionHistoryEntries_keepSorting() {
        List<SubscriptionHistory> subList = data
                .getSubscriptionHistoryEntries(1);
        SubscriptionHistory sh1 = subList.get(0);
        SubscriptionHistory sh2 = subList.get(1);
        assertEquals(1234, sh1.getKey());
        assertEquals(9678, sh2.getKey());
    }

    /**
     * No error must occur one empty list
     */
    @Test
    public void getSubscriptionHistoryEntries_empty() {
        @SuppressWarnings("unchecked")
        CustomerData data = new CustomerData(Collections.EMPTY_LIST);
        assertTrue(data.getSubscriptionHistoryEntries(0).isEmpty());
    }

    /**
     * Iterator over SubscriptionHistory objects by subscription
     */
    public void iterator() {
        Iterator<List<SubscriptionHistory>> i = data.iterator();
        List<SubscriptionHistory> subList = i.next();
        assertEquals(2, subList.size());
        List<SubscriptionHistory> subList2 = i.next();
        assertEquals(1, subList2.size());
    }

    @Test
    public void iterator_empty() {
        @SuppressWarnings("unchecked")
        CustomerData data = new CustomerData(Collections.EMPTY_LIST);
        assertFalse(data.iterator().hasNext());
    }

    @Test
    public void determineCutOffDay_empty() {
        @SuppressWarnings("unchecked")
        CustomerData billingInputWithoutData = new CustomerData(
                Collections.EMPTY_LIST);
        assertEquals(0, billingInputWithoutData.determineCutOffDay(3));
    }

    @Test
    public void determineCutOffDay() {
        CustomerData data = new CustomerData(subscriptionHistoryEntries);
        assertEquals(6, data.determineCutOffDay(3));
    }

}
