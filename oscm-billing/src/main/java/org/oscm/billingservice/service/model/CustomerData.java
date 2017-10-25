/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.billingservice.service.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import org.oscm.domobjects.SubscriptionHistory;

/**
 * The class <code>BillingData</code> contains the list of SubscriptionHistory
 * entries that are relevant for billing.
 * 
 * @author cheld
 * 
 */
public class CustomerData {

    private List<Long> subscriptionKeys;

    private List<SubscriptionHistory> subscriptionHistoryEntries = new ArrayList<SubscriptionHistory>();

    /**
     * Constructs a new BillingData object. This object contains all the
     * SubscriptionHistory objects that should be part of the billing.
     * 
     * @param subscriptionHistoryEntries
     *            List of SubscriptionHistory objects to be included.
     */
    public CustomerData(List<SubscriptionHistory> subscriptionHistoryEntries) {
        this.subscriptionHistoryEntries = subscriptionHistoryEntries;
        determineSubscriptionKeys();
    }

    void determineSubscriptionKeys() {
        Set<Long> uniqueKeys = new LinkedHashSet<Long>();
        for (SubscriptionHistory subscriptionHistory : subscriptionHistoryEntries) {
            uniqueKeys.add(Long.valueOf(subscriptionHistory.getObjKey()));
        }
        subscriptionKeys = new ArrayList<Long>(uniqueKeys);
    }

    /**
     * Returns the list of unique subscription keys that are related by this
     * billing data.
     * 
     * @return Unsorted list of subscription keys.
     */
    public List<Long> getSubscriptionKeys() {
        return subscriptionKeys;
    }

    /**
     * Returns all SubscriptionHistory objects that have been created for the
     * given subscription and are included in this billing data.
     * 
     * @param subscriptionKey
     *            Key of the subscription
     * @return List of SubscriptionHistory objects
     */
    public List<SubscriptionHistory> getSubscriptionHistoryEntries(
            long subscriptionKey) {
        List<SubscriptionHistory> sublist = new ArrayList<SubscriptionHistory>();
        for (SubscriptionHistory subscriptionHistory : subscriptionHistoryEntries) {
            if (subscriptionHistory.getObjKey() == subscriptionKey) {
                sublist.add(subscriptionHistory);
            }
        }
        return sublist;
    }

    /**
     * Returns the an iterator over the subscriptions.
     * 
     * @return Iterator
     */
    public Iterator<List<SubscriptionHistory>> iterator() {
        final Iterator<Long> i = getSubscriptionKeys().iterator();
        return new Iterator<List<SubscriptionHistory>>() {

            public boolean hasNext() {
                return i.hasNext();
            }

            public List<SubscriptionHistory> next() {
                return getSubscriptionHistoryEntries(i.next().longValue());
            }

            public void remove() {
                throw new NotImplementedException();
            }
        };
    }

    /**
     * Determine the oldest entry (modDate) from the SubscriptionHistory list
     * and return the cut-off day of the corresponding subscription. (The list
     * is sorted by other criteria from the database).
     * 
     * @return cutOffDay
     */
    public int determineCutOffDay(long subscriptionKey) {
        int cutOffDay = 0;
        Date latestEntry = new Date(Long.MIN_VALUE);
        for (SubscriptionHistory sh : getSubscriptionHistoryEntries(subscriptionKey)) {
            if (sh.getModdate().after(latestEntry)) {
                latestEntry = sh.getModdate();
                cutOffDay = sh.getCutOffDay();
            }
        }
        return cutOffDay;
    }
}
