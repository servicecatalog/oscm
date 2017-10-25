/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 01.08.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.calculation.revenue.model;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.oscm.domobjects.SubscriptionHistory;
import org.oscm.domobjects.UserGroupHistory;

/**
 * Immutable object to store information needed to bill a subscription.
 * 
 * @author kulle
 */
public class SubscriptionInput {

    private final String subscriptionId;
    private final String purchaseOrderNumber;
    private final long deactivationTime;
    private final boolean isResumedSubscription;
    private final boolean isUpgradedAfterExpiryOrSuspend;
    private final List<SubscriptionHistory> histories;

    private final UserGroupHistory userGroupHistory;

    private SubscriptionInput(Builder builder) {
        this.subscriptionId = builder.subscriptionId;
        this.userGroupHistory = builder.userGroupHistory;
        this.purchaseOrderNumber = builder.purchaseOrderNumber;
        this.deactivationTime = builder.deactivationTime;
        this.isResumedSubscription = builder.isResumedSubscription;
        this.isUpgradedAfterExpiryOrSuspend = builder.isUpgradedAfterExpiryOrSuspend;
        this.histories = builder.histories;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public String getPurchaseOrderNumber() {
        return purchaseOrderNumber;
    }

    public long getDeactivationTime() {
        return deactivationTime;
    }

    public boolean isResumedSubscription() {
        return isResumedSubscription;
    }

    public boolean isUpgradedAfterExpiryOrSuspend() {
        return isUpgradedAfterExpiryOrSuspend;
    }

    public UserGroupHistory getUserGroupHistory() {
        return userGroupHistory;
    }

    /**
     * @return List of all subscription history entries that are relevant for
     *         the given period (indicators for used price models). Ordered
     *         descending.
     */
    public List<SubscriptionHistory> getHistories() {
        return histories;
    }

    @Override
    public String toString() {
        final StringBuilder b = new StringBuilder();
        b.append("Subscription ID: ");
        b.append(subscriptionId);
        b.append("; deactivationTime: ");
        b.append(new Date(deactivationTime));
        b.append("; isResumedSubscription: ");
        b.append(isResumedSubscription);
        b.append("; isUpgradedAfterExpiration: ");
        b.append(isUpgradedAfterExpiryOrSuspend);

        return b.toString();
    }

    public static class Builder {

        private String subscriptionId;
        private String purchaseOrderNumber;
        private long deactivationTime = -1;
        public boolean isResumedSubscription = false;
        private boolean isUpgradedAfterExpiryOrSuspend = false;
        private List<SubscriptionHistory> histories = new LinkedList<SubscriptionHistory>();
        private UserGroupHistory userGroupHistory;

        public SubscriptionInput build() {
            return new SubscriptionInput(this);
        }

        public Builder setSubscriptionId(String subscriptionId) {
            this.subscriptionId = subscriptionId;
            return this;
        }

        public Builder setPurchaseOrderNumber(String purchaseOrderNumber) {
            this.purchaseOrderNumber = purchaseOrderNumber;
            return this;
        }

        public Builder setDeactivationTime(long deactivationTime) {
            this.deactivationTime = deactivationTime;
            return this;
        }

        public Builder setResumedSubscription(boolean isResumedSubscription) {
            this.isResumedSubscription = isResumedSubscription;
            return this;
        }

        public Builder setUpgradedAfterExpiryOrSuspend(
                boolean isUpgradedAfterExpiryOrSuspend) {
            this.isUpgradedAfterExpiryOrSuspend = isUpgradedAfterExpiryOrSuspend;
            return this;
        }

        public Builder setHistories(List<SubscriptionHistory> histories) {
            this.histories = histories;
            return this;
        }

        public Builder setUserGroupHistory(UserGroupHistory userGroupHistory) {
            this.userGroupHistory = userGroupHistory;
            return this;
        }

    }

}
