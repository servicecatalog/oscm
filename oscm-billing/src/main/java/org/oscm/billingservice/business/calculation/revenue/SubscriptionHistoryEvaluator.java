/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 06.12.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.calculation.revenue;

import java.util.ArrayList;
import java.util.List;

import org.oscm.billingservice.business.calculation.revenue.model.SubscriptionInput;
import org.oscm.billingservice.dao.BillingDataRetrievalServiceLocal;
import org.oscm.billingservice.service.model.BillingInput;
import org.oscm.domobjects.PriceModelHistory;
import org.oscm.domobjects.SubscriptionHistory;
import org.oscm.domobjects.UserGroupHistory;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;

/**
 * Auxiliary class to evaluate the subscription history entries
 * 
 * @author Thomas Baumann
 * 
 */
public class SubscriptionHistoryEvaluator {

    private final BillingInput billingInput;
    private final BillingDataRetrievalServiceLocal bdr;
    private List<SubscriptionInput> subscriptions = new ArrayList<SubscriptionInput>();
    private String lastValidSubscriptionId = null;
    private UserGroupHistory lastValidGroupHistory = null;
    private String lastPurchaseOrderNumber = null;

    public SubscriptionHistoryEvaluator(BillingInput billingInput,
            BillingDataRetrievalServiceLocal bdr) {
        this.billingInput = billingInput;
        this.bdr = bdr;
    }

    /**
     * Billing always evaluates all subscription history entries up to 35 days
     * before billing period start and additionally the oldest history entry,
     * which was generated before these 35 days. Thus, if we have only the
     * deactivated history entry, this entry must be located more than 35 days
     * before the current billing period. Therefore the subscription will cause
     * no costs and should not be 'listed'.
     */
    boolean hasOnlyDeactivatedHistory() {
        return (billingInput.getSubscriptionHistoryEntries().size() == 1 && (billingInput
                .getSubscriptionHistoryEntries().get(0).getStatus() == SubscriptionStatus.DEACTIVATED
                || billingInput.getSubscriptionHistoryEntries().get(0)
                        .getStatus() == SubscriptionStatus.EXPIRED || billingInput
                .getSubscriptionHistoryEntries().get(0).getStatus()
                .isSuspendedOrSuspendedUpd()));
    }

    public List<SubscriptionInput> getSubscriptions() {
        return subscriptions;
    }

    /**
     * When a subscription is terminated, it is not physically deleted - so it
     * is renamed that the id can be used again. The last entry in the history
     * then contains the generated id that must not be displayed. So get the
     * last valid subscription id from the latest subscription history entry not
     * in deactivated state.
     * 
     * @param entriesForBillingRelevantPeriod
     *            The history elements for a certain subscription. Must contain
     *            at least one entry.
     * @return the last user given id of the subscription
     */
    String getLastValidSubscriptionId() {
        List<SubscriptionHistory> subscriptionHistories = billingInput
                .getSubscriptionHistoryEntries();
        String id = subscriptionHistories.get(0).getDataContainer()
                .getSubscriptionId();
        for (SubscriptionHistory hist : subscriptionHistories) {
            if (hist.getDataContainer().getStatus() != SubscriptionStatus.DEACTIVATED) {
                id = hist.getDataContainer().getSubscriptionId();
                break;
            }
        }
        return id;
    }

    UserGroupHistory getLastValidGroupHistory() {
        Long userGroupObjKey = null;
        List<SubscriptionHistory> subscriptionHistories = billingInput
                .getSubscriptionHistoryEntries();

        for (SubscriptionHistory hist : subscriptionHistories) {
            if (!SubscriptionStatus.DEACTIVATED.equals(hist.getStatus())) {
                userGroupObjKey = hist.getUserGroupObjKey();
                break;
            }
        }

        if (userGroupObjKey != null) {
            return bdr.getLastValidGroupHistory(userGroupObjKey.longValue(),
                    billingInput.getBillingPeriodEnd());
        } else {
            return null;
        }
    }

    /**
     * @return the purchase order number of the subscription, that is set at the
     *         end of the billing period
     */
    String getLastPurchaseOrderNumber() {
        List<SubscriptionHistory> subscriptionHistories = billingInput
                .getSubscriptionHistoryEntries();
        return subscriptionHistories.get(0).getDataContainer()
                .getPurchaseOrderNumber();
    }

    /**
     * Removes all subscription history entries that are irrelevant for billing.
     * These are history entries with status PENDING or INVALID, double history
     * entries that are generated at subscription activation time, history
     * entries that are generated because some subscription parameters like the
     * subscription ID are changed, history entries that are generated because a
     * user is assigned or deassigned to the subscription, history entries which
     * have been generated because the subscription was deleted or resume
     * history entries which have no corresponding suspend-entry because this
     * entry was filtered. State transitions from an inactive state like
     * EXPIRED, SUSPENDED or SUSPENDED_UPD to another inactive state like
     * EXPIRED, SUSPENDED, SUSPENDED_UPD or DEACTIVATED are also irrelevant
     * because the subscription still must not be billed.
     */
    List<SubscriptionHistory> filterIrrelevantSubscriptionHistories(
            List<SubscriptionHistory> subHistories) {
        List<SubscriptionHistory> entriesToRemove = new ArrayList<SubscriptionHistory>();
        SubscriptionHistory formerHistory = null;
        SubscriptionHistory currentHistory = null;

        for (int i = subHistories.size() - 1; i >= 0; i--) {
            currentHistory = subHistories.get(i);

            if (subHistoryIsIrrelevant(currentHistory, formerHistory)) {
                entriesToRemove.add(currentHistory);
            } else {
                formerHistory = currentHistory;
            }
        }

        List<SubscriptionHistory> filteredEntries = new ArrayList<SubscriptionHistory>();
        for (SubscriptionHistory subHistory : subHistories) {
            if (!entriesToRemove.contains(subHistory)) {
                filteredEntries.add(subHistory);
            }
        }

        return filteredEntries;
    }

    private boolean subHistoryIsIrrelevant(SubscriptionHistory history,
            SubscriptionHistory formerHistory) {
        if (history.getStatus() == SubscriptionStatus.PENDING
                || history.getStatus() == SubscriptionStatus.INVALID) {
            return true;
        } else if (formerHistory == null) {
            return false;
        } else if (history.getStatus() == formerHistory.getStatus()
                && history.getProductObjKey() == formerHistory
                        .getProductObjKey()) {
            return true;
        } else if (history.getStatus() == SubscriptionStatus.DEACTIVATED
                && (formerHistory.getStatus() == SubscriptionStatus.EXPIRED
                        || formerHistory.getStatus() == SubscriptionStatus.SUSPENDED || formerHistory
                        .getStatus() == SubscriptionStatus.SUSPENDED_UPD)) {
            return true;
        } else if (history.getStatus() == SubscriptionStatus.EXPIRED
                && formerHistory.getStatus() == SubscriptionStatus.SUSPENDED
                && history.getProductObjKey() == formerHistory
                        .getProductObjKey()) {
            return true;
        } else if (history.getStatus() == SubscriptionStatus.PENDING_UPD
                && formerHistory.getStatus() == SubscriptionStatus.ACTIVE
                && history.getProductObjKey() == formerHistory
                        .getProductObjKey()) {
            return true;
        } else if (history.getStatus() == SubscriptionStatus.ACTIVE
                && formerHistory.getStatus() == SubscriptionStatus.PENDING_UPD
                && history.getProductObjKey() == formerHistory
                        .getProductObjKey()) {
            return true;
        } else if (history.getStatus() == SubscriptionStatus.SUSPENDED
                && (formerHistory.getStatus() == SubscriptionStatus.SUSPENDED
                        || formerHistory.getStatus() == SubscriptionStatus.SUSPENDED_UPD || formerHistory
                        .getStatus() == SubscriptionStatus.EXPIRED)) {
            return true;
        } else if (history.getStatus() == SubscriptionStatus.SUSPENDED_UPD
                && formerHistory.getStatus() == SubscriptionStatus.SUSPENDED
                && history.getProductObjKey() == formerHistory
                        .getProductObjKey()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Remove suspended subscription history entries if the subscription history
     * has a per unit price model and the subscription was suspended and
     * activated in the same time unit. A suspended entry must not be removed if
     * there is a transition to suspended with a price model change. This may
     * occur e.g. if an asynchronous subscription is upgraded and the payment
     * types of the new service are removed during the upgrade.
     */
    List<SubscriptionHistory> filterSuspendedSubscriptionHistories(
            List<SubscriptionHistory> subHistories) {

        List<SubscriptionHistory> relevantHistories = new ArrayList<SubscriptionHistory>();
        int numberOfHistories = subHistories.size();
        SubscriptionHistory currentHistory = null;
        SubscriptionHistory nextHistory = null;
        SubscriptionHistory previousHistory;

        for (int i = 0; i < numberOfHistories; i++) {
            nextHistory = currentHistory;
            currentHistory = subHistories.get(i);
            if (i < numberOfHistories - 1) {
                previousHistory = subHistories.get(i + 1);
            } else {
                previousHistory = null;
            }

            if (nextHistory != null
                    && (previousHistory == null || (previousHistory
                            .getProductObjKey() == currentHistory
                            .getProductObjKey()))) {

                PriceModelHistory priceModel = bdr
                        .loadLatestPriceModelHistory(currentHistory);
                if (priceModel.getType() != org.oscm.internal.types.enumtypes.PriceModelType.FREE_OF_CHARGE
                        && CostCalculator
                                .get(priceModel)
                                .isSuspendedAndResumedInSameTimeUnit(
                                        currentHistory, nextHistory, priceModel)) {
                    continue;
                }
            }

            relevantHistories.add(currentHistory);
        }

        return relevantHistories;
    }

    /**
     * Filter all subscription history entries that are not relevant for
     * billing.
     */
    List<SubscriptionHistory> filterSubscriptionHistories() {
        List<SubscriptionHistory> subscriptionHistories = filterIrrelevantSubscriptionHistories(billingInput
                .getSubscriptionHistoryEntries());
        subscriptionHistories = filterSuspendedSubscriptionHistories(subscriptionHistories);
        // Filter also the resume entry if the corresponding suspend entry has
        // been removed by filterSuspendedSubscriptionHistories()
        return filterIrrelevantSubscriptionHistories(subscriptionHistories);
    }

    /**
     * Split the subscription histories because in case of a suspend/resume or
     * the upgrade of an expired subscription the subscription part before the
     * suspend/expiration and the subscription part after the resume/upgrade are
     * treated as two different subscriptions.
     */
    void splitHistories(List<SubscriptionHistory> subscriptionHistories) {
        List<SubscriptionHistory> tempHistories = new ArrayList<SubscriptionHistory>();
        Long deactivationTime = null;

        for (SubscriptionHistory subscriptionHistory : subscriptionHistories) {
            SubscriptionStatus subscriptionStatus = subscriptionHistory
                    .getStatus();
            if (subscriptionStatus == SubscriptionStatus.SUSPENDED
                    || subscriptionStatus == SubscriptionStatus.EXPIRED
                    || subscriptionStatus == SubscriptionStatus.SUSPENDED_UPD) {
                deactivationTime = Long.valueOf(subscriptionHistory
                        .getModdate().getTime());
                if (tempHistories.size() > 0) {
                    boolean subscriptionWasUpgraded = productHasChanged(
                            tempHistories.get(tempHistories.size() - 1),
                            subscriptionHistory);

                    // Save the resumed/upgraded subscription block
                    addSubscription(tempHistories, deactivationTime,
                            !subscriptionWasUpgraded, subscriptionWasUpgraded);
                    tempHistories.clear();
                }
            }

            tempHistories.add(subscriptionHistory);
        }

        // Beginning of the subscription. Don't save this part if it has a
        // single history, that is not ACTIVE or PENDING_UPD. This may occur
        // if a subscription was in EXPIRED, DEACTIVATED or SUSPENDED state
        // more than 35 days or if there was a transition from PENDING to
        // SUSPENDED.
        if (tempHistories.size() > 0
                && (tempHistories.size() > 1 || tempHistories.get(0)
                        .getStatus().isActiveOrPendingUpd())) {
            addSubscription(tempHistories, deactivationTime, false, false);
        }
    }

    private boolean productHasChanged(SubscriptionHistory subHistory1,
            SubscriptionHistory subHistory2) {
        return subHistory2.getProductObjKey() != subHistory1.getProductObjKey();
    }

    private SubscriptionInput addSubscription(
            List<SubscriptionHistory> subscriptionHistories,
            Long deactivationTime, boolean isResumedSubscription,
            boolean isUpgradedAfterExpiryOrSuspend) {

        SubscriptionInput.Builder subscrBuilder = new SubscriptionInput.Builder();
        subscrBuilder
                .setSubscriptionId(lastValidSubscriptionId)
                .setPurchaseOrderNumber(lastPurchaseOrderNumber)
                .setHistories(
                        new ArrayList<SubscriptionHistory>(
                                subscriptionHistories))
                .setResumedSubscription(isResumedSubscription)
                .setUpgradedAfterExpiryOrSuspend(isUpgradedAfterExpiryOrSuspend)
                .setUserGroupHistory(lastValidGroupHistory);
        if (deactivationTime != null) {
            subscrBuilder.setDeactivationTime(deactivationTime.longValue());
        }

        SubscriptionInput subscriptionInput = subscrBuilder.build();
        subscriptions.add(subscriptionInput);
        return subscriptionInput;
    }

    /**
     * Evaluates all subscription histories
     * 
     * @return <code>true</code> if relevant subscriptions for billing were
     *         found
     */
    public boolean evaluateHistories() {
        if (!hasOnlyDeactivatedHistory()) {
            lastValidSubscriptionId = getLastValidSubscriptionId();
            lastPurchaseOrderNumber = getLastPurchaseOrderNumber();
            lastValidGroupHistory = getLastValidGroupHistory();
            List<SubscriptionHistory> subscriptionHistories = filterSubscriptionHistories();
            splitHistories(subscriptionHistories);
            return (subscriptions.size() > 0);
        } else {
            return false;
        }
    }

}
