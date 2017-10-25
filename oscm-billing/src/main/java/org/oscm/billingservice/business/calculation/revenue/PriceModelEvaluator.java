/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 06.12.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.calculation.revenue;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.oscm.billingservice.business.calculation.revenue.model.PriceModelInput;
import org.oscm.billingservice.business.calculation.revenue.model.SubscriptionInput;
import org.oscm.billingservice.dao.BillingDataRetrievalServiceLocal;
import org.oscm.billingservice.service.model.BillingInput;
import org.oscm.domobjects.PriceModelHistory;
import org.oscm.domobjects.SubscriptionHistory;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;

/**
 * Auxiliary class to evaluate the price models of a subscription
 * 
 * @author baumann
 * 
 */
public class PriceModelEvaluator {

    private final BillingInput billingInput;
    private final BillingDataRetrievalServiceLocal bdr;
    private final SubscriptionInput subscriptionInput;
    private List<PriceModelInput> priceModels = new ArrayList<PriceModelInput>();

    public PriceModelEvaluator(BillingInput billingInput,
            BillingDataRetrievalServiceLocal bdr,
            SubscriptionInput subscriptionInput) {
        this.billingInput = billingInput;
        this.bdr = bdr;
        this.subscriptionInput = subscriptionInput;
    }

    public List<PriceModelInput> getPriceModels() {
        return priceModels;
    }

    public boolean subscriptionHasRelevantPriceModels() {
        return (priceModels.size() > 0);
    }

    /**
     * Evaluate the subscription histories to get the price models. The
     * subscription histories have already been filtered, so the only remaining
     * entries are: subscription activation, subscription upgrade(s),
     * subscription suspend, subscription expire and subscription deactivation.
     * The subscription history at the beginning of the list is the oldest
     * history. Its status may be an ACTIVE, PENDING_UPD, SUSPENDED,
     * SUSPENDED_UPD, DEACTIVATED or EXPIRED. If the list has more entries,
     * these entries all must have the ACTIVE or PENDING_UPD status, because a
     * suspended/resumed or expired/ upgraded subscription is split into
     * different subscriptions (one subscription that ends with the SUSPENDED-,
     * SUSPENDED_UPD- or EXPIRED- history entry and another one that starts with
     * the resumed/upgraded ACTIVE/PENDING_UPD history entry).
     */
    public void evaluatePriceModels() {
        for (int i = 0; i < subscriptionInput.getHistories().size(); i++) {
            SubscriptionHistory subHistory = subscriptionInput.getHistories()
                    .get(i);
            boolean isOldestSubHistory = (i == 0);
            boolean isYoungestSubHistory = (i == subscriptionInput
                    .getHistories().size() - 1);

            long priceModelKey = bdr
                    .loadPriceModelKeyForSubscriptionHistory(subHistory);
            PriceModelHistory pmHistory = bdr.loadOldestPriceModelHistory(
                    priceModelKey, billingInput.getBillingPeriodEnd());
            long adjustedBPStart = adjustBillingPeriodStart(pmHistory);
            long adjustedBPEnd = adjustBillingPeriodEnd(pmHistory);

            long priceModelStartTime;
            long priceModelEndTime;

            if (isOldestSubHistory && isYoungestSubHistory) {
                // Single history entry. If this entry is not ACTIVE or
                // PENDING_UPD, it's not relevant. However, this should
                // not occur here, because such subscription parts are
                // filtered in SubscriptionHistoryEvaluator.
                if (subHistory.getStatus().isActiveOrPendingUpd()) {
                    priceModelStartTime = Math.max(subHistory.getModdate()
                            .getTime(), adjustedBPStart);
                    priceModelEndTime = billingInput.getBillingPeriodEnd();

                    addPriceModelInput(priceModelKey, priceModelStartTime,
                            priceModelEndTime, pmHistory, true, true,
                            subscriptionInput.isResumedSubscription(),
                            adjustedBPEnd, subHistory);
                }

                break;
            }

            if (isOldestSubHistory) {
                if (subHistory.getStatus().isActiveOrPendingUpd()) {
                    // The subscription remains active until BP end
                    priceModelStartTime = Math.max(subHistory.getModdate()
                            .getTime(), adjustedBPStart);
                    priceModelEndTime = billingInput.getBillingPeriodEnd();

                    addPriceModelInput(priceModelKey, priceModelStartTime,
                            priceModelEndTime, pmHistory, true, false, false,
                            adjustedBPEnd, subHistory);
                } else {
                    // The subscription has been deactivated, was suspended or
                    // is expired. Normally the current subscription history and
                    // its predecessor refer to the same price model. However,
                    // if an async. upgrade is finished successfully, the
                    // subscription may change to SUSPENDED at the same time if
                    // the payment types of the new service have been removed.
                    i++;

                    SubscriptionHistory previousSubHistory = subscriptionInput
                            .getHistories().get(i);
                    boolean previousSubHistoryIsYoungest = (i == subscriptionInput
                            .getHistories().size() - 1);
                    long previousPriceModelKey = bdr
                            .loadPriceModelKeyForSubscriptionHistory(previousSubHistory);
                    PriceModelHistory previousPmHistory = bdr
                            .loadOldestPriceModelHistory(previousPriceModelKey,
                                    billingInput.getBillingPeriodEnd());
                    adjustedBPStart = adjustBillingPeriodStart(previousPmHistory);

                    long deactTime = subHistory.getModdate().getTime();
                    if (deactTime >= adjustedBPStart) {
                        priceModelStartTime = Math.max(previousSubHistory
                                .getModdate().getTime(), adjustedBPStart);
                        priceModelEndTime = deactTime;

                        addPriceModelInput(previousPriceModelKey,
                                priceModelStartTime, priceModelEndTime,
                                previousPmHistory, true,
                                previousSubHistoryIsYoungest,
                                subscriptionInput.isResumedSubscription()
                                        && previousSubHistoryIsYoungest,
                                adjustedBPEnd, previousSubHistory);
                    }
                }
            } else {
                // All subscription histories except the oldest one must be
                // ACTIVE or PENDING_UPD because all not relevant subscription
                // history entries have been filtered and a suspended/resumed or
                // an upgraded expired subscription was split into several
                // subscriptions.
                SubscriptionHistory subsequentSubHistory = subscriptionInput
                        .getHistories().get(i - 1);
                long subsequentModTime = subsequentSubHistory.getModdate()
                        .getTime();

                if (subsequentModTime >= adjustedBPStart) {
                    priceModelStartTime = Math.max(subHistory.getModdate()
                            .getTime(), adjustedBPStart);
                    priceModelEndTime = subsequentModTime;

                    addPriceModelInput(priceModelKey, priceModelStartTime,
                            priceModelEndTime, pmHistory, false,
                            isYoungestSubHistory,
                            subscriptionInput.isResumedSubscription()
                                    && isYoungestSubHistory, adjustedBPEnd,
                            subHistory);
                }
            }
        }
    }

    private void addPriceModelInput(long priceModelKey, long priceModelStart,
            long priceModelEnd, PriceModelHistory priceModelHistory,
            boolean oldestPriceModelOfPeriod,
            boolean youngestPriceModelOfPeriod, boolean isResumedPriceModel,
            long adjustedBillingPeriodEnd, SubscriptionHistory subHistory) {
        long freePeriodEnd = determineFreePeriodEnd(priceModelHistory);
        boolean isFreePriceModel = priceModelIsFree(priceModelEnd,
                freePeriodEnd);
        long pmStartAdjustedToFreePeriod = adjustPriceModelStartTimeToFreePeriod(
                priceModelStart, priceModelEnd, freePeriodEnd);
        boolean chargeOneTimeFee = isOneTimeFeeCharged(priceModelKey,
                isFreePriceModel, priceModelStart, priceModelEnd, freePeriodEnd);
        String productId = bdr
                .loadProductTemplateHistoryForSubscriptionHistory(subHistory,
                        billingInput.getBillingPeriodEnd()).getDataContainer()
                .getProductId();

        PriceModelInput pmInput = new PriceModelInput(priceModelKey,
                priceModelStart, priceModelEnd, pmStartAdjustedToFreePeriod,
                priceModelHistory, subscriptionInput.getDeactivationTime(),
                isResumedPriceModel, oldestPriceModelOfPeriod,
                youngestPriceModelOfPeriod, freePeriodEnd, isFreePriceModel,
                adjustedBillingPeriodEnd, chargeOneTimeFee, productId);

        priceModels.add(pmInput);
    }

    /**
     * Adjust the start of the billing period to the start of an overlapping
     * time unit for a per time unit price model
     */
    long adjustBillingPeriodStart(PriceModelHistory pmHistory) {
        if (pmHistory.getType() == org.oscm.internal.types.enumtypes.PriceModelType.PER_UNIT) {
            return PricingPeriodDateConverter
                    .getStartTime(billingInput.getBillingPeriodStart(),
                            pmHistory.getPeriod()).getTimeInMillis();
        } else {
            return billingInput.getBillingPeriodStart();
        }
    }

    /**
     * If the price model type is per time unit, adjust the end of the billing
     * period to the end of the last time unit, which is not overlapping the
     * billing period end.
     */
    long adjustBillingPeriodEnd(PriceModelHistory pmHistory) {
        if (pmHistory.getType() == org.oscm.internal.types.enumtypes.PriceModelType.PER_UNIT) {
            return PricingPeriodDateConverter.getStartTime(
                    billingInput.getBillingPeriodEnd(), pmHistory.getPeriod())
                    .getTimeInMillis();
        } else {
            return billingInput.getBillingPeriodEnd();
        }
    }

    /**
     * Determines the end of the free period of a price model, i.e. the point in
     * time, when the subscription payment starts
     * 
     * @param pmHistory
     *            a price model history entry
     * @return the end of the free period in milliseconds
     */
    long determineFreePeriodEnd(PriceModelHistory pmHistory) {

        final long priceModelStartTime = bdr.loadPriceModelStartDate(
                pmHistory.getObjKey()).getTime();
        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(priceModelStartTime);
        calendar.add(Calendar.DAY_OF_MONTH, pmHistory.getFreePeriod());
        return calendar.getTimeInMillis();
    }

    /**
     * Determines if the price model is completely free of charge in the current
     * billing period. In case of a per time unit price model, the time unit,
     * where the free period ends, is charged. However, if the free period ends
     * exactly at the end of the billing period or later, the corresponding time
     * unit is charged in the next billing period because it ends there. The one
     * time fee is also charged in the next billing period in this case, thus
     * the price model is completely free.
     * 
     * @return true if the price model is not charged.
     */
    boolean priceModelIsFree(long priceModelEnd, long freePeriodEnd) {
        return (freePeriodEnd > priceModelEnd || freePeriodEnd >= billingInput
                .getBillingPeriodEnd());
    }

    /**
     * Adjust the price model start time considering the free period.
     * 
     * @return <b>the end of the price model period</b>, if the free period is
     *         longer than the price model period; <br/>
     *         otherwise <b>max{price model start time, free period end}
     */
    long adjustPriceModelStartTimeToFreePeriod(long priceModelStart,
            long priceModelEnd, long freePeriodEnd) {
        if (freePeriodEnd >= priceModelEnd) {
            return priceModelEnd;
        } else {
            return Math.max(priceModelStart, freePeriodEnd);
        }
    }

    boolean isOneTimeFeeCharged(long priceModelKey, boolean isFreePriceModel,
            long priceModelStart, long priceModelEnd, long freePeriodEnd) {

        if (isFreePriceModel) {
            return false;
        }

        SubscriptionHistory firstSubHistoryBeforeFreePeriodEnd = bdr
                .loadPreviousSubscriptionHistoryForPriceModel(priceModelKey,
                        freePeriodEnd);

        SubscriptionStatus previousSubStatus = null;
        if (firstSubHistoryBeforeFreePeriodEnd != null) {
            previousSubStatus = firstSubHistoryBeforeFreePeriodEnd.getStatus();
        }

        if (previousSubStatus == null || previousSubStatus.isPending()
                || previousSubStatus.isSuspendedOrSuspendedUpd()) {
            // The subscription was not active before the price model free
            // period ended. Check if the subscription was activated at the
            // end of the free period or afterwards. If yes, charge the onetime
            // fee if the moddate of the determined active subscription history
            // is located in the price model period. Because a per unit price
            // model with a specific start date may be contained twice in
            // different billing periods in case of an overlapping time unit,
            // we also must ensure that the onetime fee is charged in the
            // billing period where the overlapping time unit starts!
            SubscriptionHistory nextActSubHistoryFromFreePeriodEnd = bdr
                    .loadNextActiveSubscriptionHistoryForPriceModel(
                            priceModelKey, freePeriodEnd);

            if (nextActSubHistoryFromFreePeriodEnd != null) {
                Date nextModDate = nextActSubHistoryFromFreePeriodEnd
                        .getModdate();
                return (dateFitsInPriceModel(nextModDate, priceModelStart,
                        priceModelEnd) && priceModelFitsInBillingPeriod(
                        priceModelStart, priceModelEnd));
            } else {
                return false;
            }
        } else if (previousSubStatus.isActiveOrPendingUpd()) {
            // The subscription was active before the price model free period
            // ended. Charge the onetime fee in the price model part that
            // contains the free period. A per unit price model with a
            // specific start date may be contained twice in different billing
            // periods in case of an overlapping time unit. If the free period
            // ends in such a time unit, the onetime fee must be charged in the
            // billing period where the free period ends.
            return (timeFitsInPriceModel(freePeriodEnd, priceModelStart,
                    priceModelEnd) && timeFitsInBillingPeriod(freePeriodEnd));
        } else {
            return false;
        }
    }

    boolean timeFitsInBillingPeriod(long time) {
        return (time >= billingInput.getBillingPeriodStart() && time <= billingInput
                .getBillingPeriodEnd());
    }

    boolean timeFitsInPriceModel(long time, long priceModelStart,
            long priceModelEnd) {
        return (time >= priceModelStart && time <= priceModelEnd);
    }

    boolean dateFitsInPriceModel(Date date, long priceModelStart,
            long priceModelEnd) {
        if (date != null) {
            long time = date.getTime();
            return timeFitsInPriceModel(time, priceModelStart, priceModelEnd);
        } else {
            return false;
        }
    }

    boolean priceModelFitsInBillingPeriod(long priceModelStart,
            long priceModelEnd) {
        return (priceModelStart >= billingInput.getBillingPeriodStart() && priceModelEnd <= billingInput
                .getBillingPeriodEnd());
    }

}
