/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.billingservice.business.calculation.revenue;

import java.util.List;

import org.oscm.billingservice.service.model.BillingInput;
import org.oscm.domobjects.SubscriptionHistory;
import org.oscm.test.DateTimeHandling;

public class BillingInputFactory {

    public static BillingInput newBillingInput(String cutoffDate, String endDate) {
        return newBillingInput(DateTimeHandling.calculateMillis(cutoffDate),
                DateTimeHandling.calculateMillis(endDate));
    }

    public static BillingInput newBillingInput(long cutoffDate, long endDate) {
        return newBillingInput(cutoffDate, endDate, 0);
    }

    public static BillingInput newBillingInput(long cutoffDate, long endDate,
            long subscriptionKey) {
        BillingInput.Builder builder = new BillingInput.Builder();
        builder.setSubscriptionKey(subscriptionKey);
        builder.setCutOffDate(cutoffDate);
        builder.setBillingPeriodStart(cutoffDate);
        builder.setBillingPeriodEnd(endDate);
        return builder.build();
    }

    public static BillingInput newBillingInput(long billingPeriodStart) {
        BillingInput.Builder builder = new BillingInput.Builder();
        builder.setBillingPeriodStart(billingPeriodStart);
        return builder.build();
    }

    public static BillingInput newBillingInput(
            List<SubscriptionHistory> subscriptionHistoryEntries) {
        BillingInput.Builder builder = new BillingInput.Builder();
        builder.setSubscriptionHistoryEntries(subscriptionHistoryEntries);
        return builder.build();
    }

    public static BillingInput newBillingInput(String cutoffDate,
            String endDate, String currencyIsoCode) {
        return newBillingInput(DateTimeHandling.calculateMillis(cutoffDate),
                DateTimeHandling.calculateMillis(endDate), 0, currencyIsoCode);
    }

    public static BillingInput newBillingInput(long cutoffDate, long endDate,
            long subscriptionKey, String currencyIsoCode) {
        BillingInput.Builder builder = new BillingInput.Builder();
        builder.setSubscriptionKey(subscriptionKey);
        builder.setCutOffDate(cutoffDate);
        builder.setBillingPeriodStart(cutoffDate);
        builder.setBillingPeriodEnd(endDate);
        builder.setCurrencyIsoCode(currencyIsoCode);
        return builder.build();
    }

}
