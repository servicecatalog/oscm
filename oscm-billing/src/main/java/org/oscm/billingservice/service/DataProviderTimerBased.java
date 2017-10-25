/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: May 28, 2015                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.oscm.billingservice.dao.BillingDataRetrievalServiceLocal;
import org.oscm.billingservice.dao.model.BillingSubscriptionData;
import org.oscm.billingservice.service.model.BillingInput;
import org.oscm.billingservice.service.model.BillingPeriodData;
import org.oscm.billingservice.service.model.BillingSubscriptionChunk;
import org.oscm.billingservice.service.model.BillingSubscriptionHistoryData;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.SubscriptionHistory;
import org.oscm.domobjects.SupportedCurrency;
import org.oscm.domobjects.UserGroup;
import org.oscm.internal.types.exception.ObjectNotFoundException;

/**
 * This class is a data provider for the timer-triggered and operator-triggered
 * billing run.
 * 
 * @author baumann
 * 
 */
class DataProviderTimerBased {

    private static final int SUBSCRIPTION_CHUNK_SIZE = 1000;
    private static final int DEACTIVATION_THRESHOLD = 65;
    private static final boolean STORE_RESULT = true;

    private final ThreadLocal<Calendar> calendar = new ThreadLocal<>();

    private final BillingDataRetrievalServiceLocal bdr;
    private final long periodRevenueSharesStart;
    private final long periodRevenueSharesEnd;
    private long effectiveBillingEndDate;

    public DataProviderTimerBased(long billingInvocationTime,
            long billingOffset, BillingDataRetrievalServiceLocal bdr) {
        this.bdr = bdr;
        this.effectiveBillingEndDate = calculateEffectiveBillingEndDate(
                billingInvocationTime, billingOffset);
        this.periodRevenueSharesEnd = calculateRevenueSharesPeriodEnd(
                billingInvocationTime, billingOffset);
        this.periodRevenueSharesStart = addMonths(periodRevenueSharesEnd, -1);
    }

    private Calendar getCalendar() {
        Calendar cal = calendar.get();
        if (cal == null) {
            cal = Calendar.getInstance();
            calendar.set(cal);
        }
        return cal;
    }

    public long getPeriodRevenueSharesEnd() {
        return periodRevenueSharesEnd;
    }

    public long getPeriodRevenueSharesStart() {
        return periodRevenueSharesStart;
    }

    public long getEffectiveBillingEndDate() {
        return effectiveBillingEndDate;
    }

    /**
     * Calculates the end of the last period, that may be calculated in the
     * current billing run, based on the billing invocation time and the billing
     * offset. The period end is always on a cut-off day, no period end for 29,
     * 30, 31 days of month.
     * 
     * Examples: <br/>
     * 
     * 15.12.2012 02:30:0500, offset 2 days in ms <br/>
     * 13.12.2012 00:00:00:0000 period end
     * 
     * 31.01.2012 02:30:0500, offset 1 day in ms <br/>
     * 28.01.2012 00:00:00:0000 period end
     * 
     * @param billingInvocationTime
     *            - in milliseconds
     * @param billingOffset
     *            - in milliseconds
     * @return the effective billing enddate in milliseconds (start of day
     *         00:00:00:0000)
     */
    private long calculateEffectiveBillingEndDate(long billingInvocationTime,
            long billingOffset) {
        Calendar cal = getCalendar();
        cal.setTimeInMillis(subtractBillingOffset(billingInvocationTime,
                billingOffset));
        cal.set(Calendar.DAY_OF_MONTH,
                normalizeCutOffDay(cal.get(Calendar.DAY_OF_MONTH)));
        return cal.getTimeInMillis();
    }

    /**
     * Subtract billing offset from billing invocation time and set hour,
     * minute, second and millisecond to 0.
     */
    long subtractBillingOffset(long billingInvocationTime, long billingOffset) {
        Calendar cal = getCalendar();
        cal.setTimeInMillis(billingInvocationTime - billingOffset);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    /**
     * Cut-off day value range [1-28]. For 29,30,31 the value will be set to
     * max. 28.
     *
     * @param cutOffDay
     * @return normalized cutOffDay
     */
    private int normalizeCutOffDay(int cutOffDay) {
        if (cutOffDay > 28)
            return 28;
        return cutOffDay;
    }

    /**
     * Calculates the revenue shares period end, base on invocation time and
     * time offset. The period end is always on first day of the month.<br/>
     *
     * @param billingInvocationTime
     *            - in milliseconds
     * @param billingOffset
     *            - in milliseconds
     * @return end billing period in milliseconds (start of day 00:00:00:0000)
     */
    private long calculateRevenueSharesPeriodEnd(long billingInvocationTime,
            long billingOffset) {
        Calendar cal = getCalendar();
        cal.setTimeInMillis(subtractBillingOffset(billingInvocationTime,
                billingOffset));
        cal.set(Calendar.DAY_OF_MONTH, 1);
        return cal.getTimeInMillis();
    }

    private long addMonths(long millis, int months) {
        Calendar cal = getCalendar();
        cal.setTimeInMillis(millis);
        cal.add(Calendar.MONTH, months);
        return cal.getTimeInMillis();
    }

    private long addDays(long millis, int days) {
        Calendar cal = getCalendar();
        cal.setTimeInMillis(millis);
        cal.add(Calendar.DAY_OF_MONTH, days);
        return cal.getTimeInMillis();
    }

    /**
     * Determines all subscriptions that are relevant for the current billing
     * run and the corresponding billing periods, that must be calculated. For a
     * given billing period, the affected subscriptions are summarized in
     * subscription chunks and stored in a list of BillingPeriodData objects.
     */
    public List<BillingPeriodData> loadBillingData() {

        long cutoffBillingEndDate = addMonths(effectiveBillingEndDate, -1);
        long cutoffDeactivationDate = addDays(effectiveBillingEndDate,
                -DEACTIVATION_THRESHOLD);

        List<BillingSubscriptionData> subscriptionList = bdr
                .getSubscriptionsForBilling(effectiveBillingEndDate,
                        cutoffBillingEndDate, cutoffDeactivationDate);

        Map<Long, List<Long>> billingPeriods = determineBillingPeriods(subscriptionList);
        return sortBillingPeriods(billingPeriods);
    }

    /**
     * Determine the billing periods, that must be processed in the current
     * billing run and store the affected subscription ID's for the billing
     * periods.
     *
     * @param subscriptionList
     *            a list of data of all subscriptions, that must be processed in
     *            the current billing run
     * @return a map with the billing period end dates as keys and a set of
     *         subscription keys as values
     */
    Map<Long, List<Long>> determineBillingPeriods(
            List<BillingSubscriptionData> subscriptionList) {

        Map<Long, List<Long>> billingPeriodMap = new HashMap<>();

        for (BillingSubscriptionData billingSubData : subscriptionList) {
            long startOfBillingPeriod;
            if (billingSubData.getEndOfLastBilledPeriod() != null) {
                startOfBillingPeriod = billingSubData
                        .getEndOfLastBilledPeriod().longValue();
            } else {
                startOfBillingPeriod = calculateStartOfFirstBillingPeriod(
                        billingSubData.getCutOffDay(),
                        billingSubData.getActivationDate());
            }
            long endOfBillingPeriod = addMonths(startOfBillingPeriod, 1);

            while (endOfBillingPeriod <= effectiveBillingEndDate) {
                addToBillingPeriodMap(billingPeriodMap,
                        Long.valueOf(endOfBillingPeriod),
                        Long.valueOf(billingSubData.getSubscriptionKey()));
                endOfBillingPeriod = addMonths(endOfBillingPeriod, 1);
            }
        }

        return billingPeriodMap;
    }

    private long calculateStartOfFirstBillingPeriod(int cutOffDay,
            long activationDate) {

        Calendar cal = getCalendar();
        cal.setTimeInMillis(activationDate);
        int activationDay = cal.get(Calendar.DAY_OF_MONTH);

        cal.set(Calendar.DAY_OF_MONTH, cutOffDay);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        if (cutOffDay > activationDay) {
            cal.add(Calendar.MONTH, -1);
        }

        return cal.getTimeInMillis();
    }

    private void addToBillingPeriodMap(Map<Long, List<Long>> billingPeriodMap,
            Long endOfBillingPeriod, Long subscriptionKey) {
        List<Long> subscriptionKeys = billingPeriodMap.get(endOfBillingPeriod);
        if (subscriptionKeys != null) {
            subscriptionKeys.add(subscriptionKey);
        } else {
            subscriptionKeys = new ArrayList<>();
            subscriptionKeys.add(subscriptionKey);
            billingPeriodMap.put(endOfBillingPeriod, subscriptionKeys);
        }
    }

    /**
     * Sort the billing periods. The oldest billing period should be processed
     * first. If the number of subscription keys for a billing period exceeds
     * the maximum SUBSCRIPTION_CHUNK_SIZE, split the subscriptions in several
     * chunks. The result is stored in a list of BillingPeriodData objects,
     * which contain beginning and end of a billing period and a list of the
     * affected subscription keys.
     *
     * @param billingPeriodMap
     *            a map with the billing period end dates as keys and a set of
     *            subscription keys as values
     */
    List<BillingPeriodData> sortBillingPeriods(
            Map<Long, List<Long>> billingPeriodMap) {
        List<BillingPeriodData> billingPeriodData = new ArrayList<>();

        List<Long> billingEndDateList = new ArrayList<>(
                billingPeriodMap.keySet());
        Collections.sort(billingEndDateList);

        for (Long billingEndDate : billingEndDateList) {
            // The subscription key list is already sorted because the billing
            // data retrieval service returns the subscriptions in ascending
            // order!
            List<Long> subscriptionKeys = billingPeriodMap.get(billingEndDate);
            long billingStartDate = addMonths(billingEndDate.longValue(), -1);

            // Split the subscription key list into chunks
            int fromIndex = 0;
            do {
                int toIndex = Math.min(
                        (fromIndex + getSubscriptionChunkSize()),
                        subscriptionKeys.size());
                List<Long> subscriptionKeyChunk = subscriptionKeys.subList(
                        fromIndex, toIndex);
                billingPeriodData.add(new BillingPeriodData(billingStartDate,
                        billingEndDate.longValue(), subscriptionKeyChunk));
                fromIndex = toIndex;
            } while (fromIndex < subscriptionKeys.size());
        }

        return billingPeriodData;
    }

    int getSubscriptionChunkSize() {
        return SUBSCRIPTION_CHUNK_SIZE;
    }

    /**
     * Get the relevant subscription history records for a given billing period
     * and list of subscription keys. Create a BillingInput object for each
     * affected subscription, which contains the histories.
     *
     * @param billingPeriodData
     *            a BillingPeriodData object
     * @return a subscription chunk object containing the beginning and end of
     *         the billing period and a list of BillingInput objects
     */
    public BillingSubscriptionChunk getSubscriptionHistories(
            BillingPeriodData billingPeriodData, DataService ds) {

        long billingPeriodStart = billingPeriodData.getBillingPeriodStart();
        long billingPeriodEnd = billingPeriodData.getBillingPeriodEnd();

        BillingSubscriptionChunk subscriptionChunk = new BillingSubscriptionChunk(
                billingPeriodStart, billingPeriodEnd);

        List<SubscriptionHistory> subHistories = bdr
                .loadSubscriptionHistoriesForBillingPeriod(
                        billingPeriodData.getSubscriptionKeys(),
                        billingPeriodStart, billingPeriodEnd);

        BillingSubscriptionHistoryData subHistoryData = new BillingSubscriptionHistoryData(
                subHistories);
        for (Long subscriptionKey : subHistoryData.getSubscriptionKeys()) {
            BillingInput billingInput = createBillingInput(subscriptionKey,
                    billingPeriodStart, billingPeriodEnd,
                    subHistoryData
                            .getSubscriptionHistoryEntries(subscriptionKey), ds);
            subscriptionChunk.addBillingInput(billingInput);
        }

        return subscriptionChunk;
    }

    BillingInput createBillingInput(Long subscriptionKey,
            long billingPeriodStart, long billingPeriodEnd,
            List<SubscriptionHistory> subHistories, DataService dm) {
        BillingInput.Builder biBuilder = new BillingInput.Builder();
        biBuilder.setStoreBillingResult(STORE_RESULT);
        biBuilder.setSubscriptionKey(subscriptionKey.longValue());
        biBuilder.setBillingPeriodStart(billingPeriodStart);
        biBuilder.setBillingPeriodEnd(billingPeriodEnd);
        biBuilder.setCutOffDate(billingPeriodStart);
        biBuilder.setOrganizationKey(
                subHistories.get(0).getOrganizationObjKey());
        biBuilder.setSubscriptionHistoryEntries(subHistories);

        SupportedCurrency currency = bdr
                .loadCurrency(subscriptionKey.longValue(), billingPeriodEnd);
        if (currency != null) {
            biBuilder.setCurrencyIsoCode(currency.getCurrencyISOCode());
        }

        return fillUserGroupData(biBuilder.build(), dm);
    }

    BillingInput fillUserGroupData(BillingInput billingInput, DataService dm) {
        try {
            Subscription subscription = dm.getReference(Subscription.class,
                    billingInput.getSubscriptionKey());
            UserGroup unit = subscription.getUserGroup();
            if (unit != null) {
                billingInput.setUserGroupKey(Long.valueOf(unit.getKey()));
            }
        } catch (ObjectNotFoundException ignored) {
        }
        return billingInput;
    }

}
