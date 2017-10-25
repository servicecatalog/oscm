/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 03.06.2015                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.service.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.oscm.domobjects.SubscriptionHistory;

/**
 * The class <code>BillingSubscriptionHistoryData</code> contains the list of
 * SubscriptionHistory entries that are relevant for a chunk of subscriptions.
 * 
 * @author baumann
 * 
 */
public class BillingSubscriptionHistoryData {

    private List<SubscriptionHistory> subscriptionHistoryEntries;
    private Map<Long, SubscriptionIndices> subscriptionIndexMap;

    /**
     * Create a new BillingSubscriptionHistoryData object
     * 
     * @param subscriptionHistoryEntries
     *            a list of subscription histories, which is sorted by
     *            subscription keys.
     */
    public BillingSubscriptionHistoryData(
            List<SubscriptionHistory> subscriptionHistoryEntries) {
        this.subscriptionHistoryEntries = subscriptionHistoryEntries;
        subscriptionIndexMap = new HashMap<Long, SubscriptionIndices>();
        if (subscriptionHistoryEntries.size() > 0) {
            determineSubscriptions();
        }
    }

    void determineSubscriptions() {
        int startIndex = 0;
        Long subscriptionKey = Long.valueOf(subscriptionHistoryEntries.get(0)
                .getObjKey());

        for (int i = 1; i < subscriptionHistoryEntries.size(); i++) {
            SubscriptionHistory subscriptionHistory = subscriptionHistoryEntries
                    .get(i);
            if (subscriptionHistory.getObjKey() != subscriptionKey.longValue()) {
                subscriptionIndexMap.put(subscriptionKey,
                        new SubscriptionIndices(startIndex, i));
                startIndex = i;
                subscriptionKey = Long.valueOf(subscriptionHistory.getObjKey());
            }
        }
        subscriptionIndexMap.put(subscriptionKey, new SubscriptionIndices(
                startIndex, subscriptionHistoryEntries.size()));
    }

    /**
     * Returns the list of unique subscription keys that are related by this
     * billing data.
     * 
     * @return Sorted list of subscription keys.
     */
    public List<Long> getSubscriptionKeys() {
        List<Long> subscriptionKeyList = new ArrayList<Long>(
                subscriptionIndexMap.keySet());
        Collections.sort(subscriptionKeyList);
        return subscriptionKeyList;
    }

    /**
     * Returns all SubscriptionHistory objects for a given subscription
     * 
     * @param subscriptionKey
     *            Key of the subscription
     * @return List of SubscriptionHistory objects
     */
    public List<SubscriptionHistory> getSubscriptionHistoryEntries(
            Long subscriptionKey) {
        SubscriptionIndices subIndices = subscriptionIndexMap
                .get(subscriptionKey);
        return subscriptionHistoryEntries.subList(subIndices.getStartIndex(),
                subIndices.getEndIndex());
    }

    private class SubscriptionIndices {
        private int startIndex; // start index in the subscription history list
        private int endIndex; // end index in the subscription history list
                              // (exclusive)

        public SubscriptionIndices(int startIndex, int endIndex) {
            this.startIndex = startIndex;
            this.endIndex = endIndex;
        }

        public int getStartIndex() {
            return startIndex;
        }

        public int getEndIndex() {
            return endIndex;
        }
    }

}
