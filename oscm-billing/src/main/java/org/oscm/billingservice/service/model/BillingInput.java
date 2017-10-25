/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Dec 13, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.service.model;

import java.util.Date;
import java.util.List;

import org.oscm.domobjects.SubscriptionHistory;

/**
 * This class provides all information for a single billing run of one
 * subscription
 * 
 * @author muenz
 * 
 */
public class BillingInput {

    public enum BillingContextType {
        BILLING_FOR_ANY_PERIOD, OTHER
    }

    private final long initialBillingPeriodStart;
    private final long initialBillingPeriodEnd;
    private final long billingPeriodStart;
    private final long billingPeriodEnd;
    private final long cutOffDate;

    private Long userGroupKey;
    private final long organizationKey;
    private final long subscriptionKey;
    private final List<SubscriptionHistory> subscriptionHistoryEntries;
    private final String currencyIsoCode;
    private final boolean storeBillingResult;
    private final BillingContextType billingContext;

    private BillingInput(Builder builder) {
        this.initialBillingPeriodStart = builder.initialBillingPeriodStart;
        this.initialBillingPeriodEnd = builder.initialBillingPeriodEnd;
        this.billingPeriodStart = builder.billingPeriodStart;
        this.billingPeriodEnd = builder.billingPeriodEnd;
        this.cutOffDate = builder.cutOffDate;

        this.organizationKey = builder.organizationKey;
        this.subscriptionKey = builder.subscriptionKey;
        this.subscriptionHistoryEntries = builder.subscriptionHistoryEntries;
        this.currencyIsoCode = builder.currencyIsoCode;
        this.storeBillingResult = builder.storeBillingResult;
        this.billingContext = builder.billingContext;
    }

    public long getSubscriptionKey() {
        return subscriptionKey;
    }

    /**
     * Entries are sorted descending.
     */
    public List<SubscriptionHistory> getSubscriptionHistoryEntries() {
        return subscriptionHistoryEntries;
    }

    public long getBillingPeriodStart() {
        return billingPeriodStart;
    }

    public long getBillingPeriodEnd() {
        return billingPeriodEnd;
    }

    public long getOrganizationKey() {
        return organizationKey;
    }

    public String getCurrencyIsoCode() {
        return currencyIsoCode;
    }

    public boolean isStoreBillingResult() {
        return storeBillingResult;
    }

    public long getCutOffDate() {
        return cutOffDate;
    }

    public long getInitialBillingPeriodStart() {
        return initialBillingPeriodStart;
    }

    public long getInitialBillingPeriodEnd() {
        return initialBillingPeriodEnd;
    }

    public BillingContextType getBillingContext() {
        return billingContext;
    }

    public Long getUserGroupKey() {
        return userGroupKey;
    }

    public void setUserGroupKey(Long userGroupKey) {
        this.userGroupKey = userGroupKey;
    }

    @Override
    public String toString() {
        return "Range: " + new Date(billingPeriodStart) + " - "
                + new Date(billingPeriodEnd) + "; Currency: " + currencyIsoCode
                + "; Organization: " + organizationKey + "; Subscription: "
                + subscriptionKey + "; History entries: "
                + (subscriptionHistoryEntries == null ? 0
                        : subscriptionHistoryEntries.size());
    }

    public static class Builder {

        private long billingPeriodStart;
        private long billingPeriodEnd;
        private long cutOffDate;
        private long initialBillingPeriodStart;
        private long initialBillingPeriodEnd;

        private long organizationKey;
        private long subscriptionKey;
        private List<SubscriptionHistory> subscriptionHistoryEntries;
        private String currencyIsoCode;
        private boolean storeBillingResult;
        private BillingContextType billingContext = BillingContextType.OTHER;

        public BillingInput build() {
            return new BillingInput(this);
        }

        public Builder setSubscriptionHistoryEntries(
                List<SubscriptionHistory> subscriptionHistoryEntries) {
            this.subscriptionHistoryEntries = subscriptionHistoryEntries;
            return this;
        }

        public Builder setBillingPeriodStart(long periodStart) {
            this.billingPeriodStart = periodStart;
            return this;
        }

        public Builder setBillingPeriodEnd(long periodEnd) {
            this.billingPeriodEnd = periodEnd;
            return this;
        }

        public Builder setOrganizationKey(long organizationKey) {
            this.organizationKey = organizationKey;
            return this;
        }

        public Builder setCurrencyIsoCode(String currencyIsoCode) {
            this.currencyIsoCode = currencyIsoCode;
            return this;
        }

        public Builder setStoreBillingResult(boolean storeResult) {
            this.storeBillingResult = storeResult;
            return this;
        }

        public Builder setCutOffDate(long cutOffDate) {
            this.cutOffDate = cutOffDate;
            return this;
        }

        public Builder setInitialBillingPeriodStart(
                long initialBillingPeriodStart) {
            this.initialBillingPeriodStart = initialBillingPeriodStart;
            return this;
        }

        public Builder setInitialBillingPeriodEnd(long initialBillingPeriodEnd) {
            this.initialBillingPeriodEnd = initialBillingPeriodEnd;
            return this;
        }

        public Builder setBillingContext(BillingContextType billingContext) {
            this.billingContext = billingContext;
            return this;
        }

        public Builder setSubscriptionKey(long subscriptionKey) {
            this.subscriptionKey = subscriptionKey;
            return this;
        }

        public long getBillingPeriodStart() {
            return billingPeriodStart;
        }

        public long getBillingPeriodEnd() {
            return billingPeriodEnd;
        }

        public long getCutOffDate() {
            return cutOffDate;
        }

        public long getInitialBillingPeriodStart() {
            return initialBillingPeriodStart;
        }

        public long getInitialBillingPeriodEnd() {
            return initialBillingPeriodEnd;
        }

        public long getOrganizationKey() {
            return organizationKey;
        }

        public long getSubscriptionKey() {
            return subscriptionKey;
        }

        public List<SubscriptionHistory> getSubscriptionHistoryEntries() {
            return subscriptionHistoryEntries;
        }

        public String getCurrencyIsoCode() {
            return currencyIsoCode;
        }

        public boolean isStoreBillingResult() {
            return storeBillingResult;
        }

        public BillingContextType getBillingContext() {
            return billingContext;
        }

    }

}
