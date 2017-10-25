/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Dec 14, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.oscm.billingservice.business.calculation.revenue.CostCalculator;
import org.oscm.billingservice.dao.BillingDataRetrievalServiceLocal;
import org.oscm.billingservice.service.model.BillingInput;
import org.oscm.billingservice.service.model.BillingInput.BillingContextType;
import org.oscm.billingservice.service.model.CustomerData;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.PriceModelHistory;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.SubscriptionHistory;
import org.oscm.domobjects.SupportedCurrency;
import org.oscm.domobjects.UserGroup;
import org.oscm.validation.Invariants;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.types.exception.ObjectNotFoundException;

/**
 * This class is a data provider for the exporting billing data by any period.
 * The operator can select any start date and any end date for the billing run,
 * e.g. from Jan to Dec of last year.
 * 
 * @author muenz
 * 
 */
public class DataProviderAnyPeriod extends DataProvider {
    private final long organizationKey;
    private final List<Long> unitKeys;

    /**
     * preview TRUE:<br>
     * customer: Account -> Reports -> Payment preview report<br>
     * called by: generatePaymentPreviewReport<br>
     * 
     * preview FALSE: <br>
     * operator: Account -> Billing data preview<br>
     * called by: generateBillingForAnyPeriod<br>
     */
    private final boolean preview;
    private final long initialPeriodStart;
    private final long initialPeriodEnd;

    public DataProviderAnyPeriod(BillingDataRetrievalServiceLocal bdr,
            long periodStart, long periodEnd, long organizationKey,
            List<Long> unitKeys, boolean preview, DataService dm) {
        Invariants.assertNotNull(bdr,
                "BillingDataRetrievalServiceLocal must not be null!");
        Invariants.assertTrue(periodStart > 0, "period start have to be set!");
        Invariants.assertTrue(periodEnd > 0, "period end have to be set!");
        Invariants.assertTrue(organizationKey > 0,
                "organization key have to be set!");
        this.initialPeriodStart = periodStart;
        this.initialPeriodEnd = periodEnd;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.organizationKey = organizationKey;
        this.unitKeys = unitKeys;
        this.preview = preview;
        this.billingInputList = loadBillingInputList(bdr, dm);
    }

    public DataProviderAnyPeriod(BillingDataRetrievalServiceLocal bdr,
            long periodStart, long periodEnd, long organizationKey,
            boolean preview) {
        this(bdr, periodStart, periodEnd, organizationKey, null, preview, null);
    }

    public long getInitialPeriodStart() {
        return initialPeriodStart;
    }

    public long getInitialPeriodEnd() {
        return initialPeriodEnd;
    }

    private long getMinimumStartDate(List<SubscriptionHistory> subs) {
        long min = Long.MAX_VALUE;
        for (SubscriptionHistory sub : subs) {
            if (sub.getModdate().getTime() < min) {
                min = sub.getModdate().getTime();
            }
        }
        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(min);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    private long getMaximumEndDate(List<SubscriptionHistory> subs) {
        long max = Long.MIN_VALUE;
        for (SubscriptionHistory sub : subs) {
            if (sub.getStatus() == SubscriptionStatus.DEACTIVATED
                    && sub.getModdate().getTime() > max) {
                max = sub.getModdate().getTime();
            }
        }
        return max == Long.MIN_VALUE ? Long.MAX_VALUE : max;
    }

    List<BillingInput> loadBillingInputList(
            BillingDataRetrievalServiceLocal bdr,
            DataService dm) {
        final CustomerData customerData = new CustomerData(
                loadSubscriptions(bdr));
        final List<BillingInput> result = new ArrayList<>();
        for (Long subscriptionKey : customerData.getSubscriptionKeys()) {
            processSubscriptionKey(bdr, customerData, result, subscriptionKey);
        }
        fillUserGroupData(result, dm);
        return result;
    }

    void fillUserGroupData(List<BillingInput> billingInputs,
            DataService dm) {
        for (BillingInput billingInput : billingInputs) {
            try {
                Subscription subscription = dm.getReference(Subscription.class,
                        billingInput.getSubscriptionKey());
                UserGroup unit = subscription.getUserGroup();
                if (unit != null) {
                    billingInput.setUserGroupKey(Long.valueOf(unit.getKey()));
                }
            } catch (ObjectNotFoundException ignored) {}
        }
    }

    /**
     * All subscriptions within the start and end period must be loaded.
     * However, a termination or upgrade of a subscription might have been
     * happened before and not yet billed, because the time unit and the billing
     * period are overlapping. So, in order to include those subscription we add
     * a little bit more than one month (a precise starting date is not
     * important, because the billing will filter all non relevant data anyway).
     */
    private List<SubscriptionHistory> loadSubscriptions(
            BillingDataRetrievalServiceLocal bdr) {
        if (unitKeys == null) {
            return bdr.loadSubscriptionsForCustomer(organizationKey,
                    periodStart, periodEnd, -1);
        } else {
            return bdr.loadSubscriptionsForCustomer(organizationKey, unitKeys,
                    periodStart, periodEnd, -1);
        }
    }

    private void processSubscriptionKey(BillingDataRetrievalServiceLocal bdr,
            CustomerData customerData, List<BillingInput> result,
            Long subscriptionKey) {

        final int subscriptionCutOffDay = customerData
                .determineCutOffDay(subscriptionKey.longValue());
        final List<SubscriptionHistory> subs = customerData
                .getSubscriptionHistoryEntries(subscriptionKey.longValue());

        final SupportedCurrency currency = bdr.loadCurrency(
                subscriptionKey.longValue(), periodEnd);
        final List<PriceModelHistory> priceModelHistories = bdr
                .loadPricemodelHistoriesForSubscriptionHistory(subs.get(0)
                        .getObjKey(), periodEnd);

        long currentStartDate = getMinimumStartDate(subs);
        if (currentStartDate < periodStart) {
            currentStartDate = periodStart;
        }
        long currentPeriodEnd = getMaximumEndDate(subs);
        if (currentPeriodEnd > periodEnd) {
            currentPeriodEnd = periodEnd;
        }

        while (currentStartDate < currentPeriodEnd) {
            currentStartDate = processPeriod(result, subscriptionKey,
                    subscriptionCutOffDay, subs, priceModelHistories, currency,
                    currentStartDate);
        }
    }

    private long processPeriod(List<BillingInput> result, Long subscriptionKey,
            final int subscriptionCutOffDay,
            final List<SubscriptionHistory> subs,
            final List<PriceModelHistory> priceModelHistories,
            final SupportedCurrency currency, long currentStartDate) {

        final Calendar cutOffDate = CutOffDayConverter
                .getBillingStartTimeForCutOffDay(currentStartDate,
                        subscriptionCutOffDay);
        final BillingInput.Builder bi = createBillingInput(currency,
                subscriptionKey, cutOffDate.getTimeInMillis());
        if (!preview) {
            bi.setInitialBillingPeriodStart(initialPeriodStart);
            bi.setInitialBillingPeriodEnd(initialPeriodEnd);
            bi.setBillingContext(BillingContextType.BILLING_FOR_ANY_PERIOD);
        }
        bi.setBillingPeriodStart(cutOffDate.getTimeInMillis());
        long billingPeriodEnd = CutOffDayConverter
                .getBillingEndTimeForCutOffDay(cutOffDate.getTimeInMillis())
                .getTimeInMillis();
        if (billingPeriodEnd > periodEnd) {
            billingPeriodEnd = periodEnd;
        }
        bi.setBillingPeriodEnd(billingPeriodEnd);
        bi.setSubscriptionHistoryEntries(getRelevantSubscriptions(subs,
                billingPeriodEnd));

        if ((!preview || cutOffDate.getTimeInMillis() > periodEnd)
                && cutOffDate.getTimeInMillis() > currentStartDate) {

            // Add the first period, which is less than a whole billing period.
            // Do it only in case of none preview, since preview does not take
            // this period into account
            if (cutOffDate.getTimeInMillis() > currentStartDate) {
                cutOffDate.add(Calendar.MONTH, -1);
            }
            final BillingInput.Builder bi2 = createBillingInput(currency,
                    subscriptionKey, cutOffDate.getTimeInMillis());
            if (!preview) {
                bi2.setInitialBillingPeriodStart(initialPeriodStart);
                bi2.setInitialBillingPeriodEnd(initialPeriodEnd);
                bi2.setBillingContext(BillingContextType.BILLING_FOR_ANY_PERIOD);
            }
            bi2.setBillingPeriodStart(currentStartDate);
            bi2.setBillingPeriodEnd(bi.getBillingPeriodStart() > periodEnd ? periodEnd
                    : bi.getBillingPeriodStart());
            bi2.setSubscriptionHistoryEntries(getRelevantSubscriptions(subs,
                    bi2.getBillingPeriodEnd()));
            long billingEndPeriod = determineEndTimeForPaymentPreview(
                    bi2.getBillingPeriodStart(), bi2.getBillingPeriodEnd(),
                    priceModelHistories);
            bi2.setBillingPeriodEnd(billingEndPeriod);
            result.add(bi2.build());
        }

        if (bi.getBillingPeriodStart() < periodEnd) {
            long billingEndPeriod = determineEndTimeForPaymentPreview(
                    bi.getBillingPeriodStart(), bi.getBillingPeriodEnd(),
                    priceModelHistories);
            bi.setBillingPeriodEnd(billingEndPeriod);
            result.add(bi.build());
        }
        return bi.getBillingPeriodEnd();
    }

    long determineEndTimeForPaymentPreview(long billingStartPeriod,
            long billingEndPeriod, List<PriceModelHistory> priceModelHistories) {
        if (billingEndPeriod >= periodEnd) {
            // the last period might be prolonged, test it and
            // set if if so
            final Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(billingStartPeriod);
            cal.add(Calendar.MONTH, 1);

            long paymentPreviewEndTime = 0;
            for (PriceModelHistory priceModelHistory : priceModelHistories) {
                if (!priceModelHistory.isChargeable()) {
                    paymentPreviewEndTime = billingEndPeriod;
                } else {
                    paymentPreviewEndTime = calculateEndTimeForPaymentPreview(
                            billingEndPeriod, cal, priceModelHistory);
                }
                if (paymentPreviewEndTime > billingEndPeriod) {
                    billingEndPeriod = paymentPreviewEndTime;
                }
            }
        }
        return billingEndPeriod;
    }

    long calculateEndTimeForPaymentPreview(long billingEndPeriod,
            final Calendar cal, PriceModelHistory priceModelHistory) {
        return CostCalculator.get(priceModelHistory)
                .computeEndTimeForPaymentPreview(billingEndPeriod,
                        cal.getTimeInMillis(), priceModelHistory.getPeriod());
    }

    private List<SubscriptionHistory> getRelevantSubscriptions(
            List<SubscriptionHistory> subs, long billingPeriodEnd) {
        final List<SubscriptionHistory> relevantSubs = new ArrayList<>();
        for (SubscriptionHistory sub : subs) {
            if (billingPeriodEnd > sub.getModdate().getTime()) {
                relevantSubs.add(sub);
            }
        }
        return relevantSubs;
    }

    private BillingInput.Builder createBillingInput(SupportedCurrency currency,
            Long subscriptionKey, long cutOffDate) {
        BillingInput.Builder builder = new BillingInput.Builder();
        if (currency != null) {
            builder.setCurrencyIsoCode(currency.getCurrencyISOCode());
        }
        builder.setSubscriptionKey(subscriptionKey.longValue());
        builder.setOrganizationKey(organizationKey);
        builder.setStoreBillingResult(false);
        builder.setCutOffDate(cutOffDate);
        return builder;
    }
}
