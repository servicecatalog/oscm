/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 06.09.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.calculation.revenue;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.oscm.billingservice.business.calculation.revenue.model.EventCosts;
import org.oscm.billingservice.business.calculation.revenue.model.PriceModelInput;
import org.oscm.billingservice.business.model.billingresult.BillingResultAssembler;
import org.oscm.billingservice.business.model.billingresult.PriceModelType;
import org.oscm.billingservice.dao.BillingDataRetrievalServiceLocal;
import org.oscm.billingservice.dao.model.EventCount;
import org.oscm.billingservice.dao.model.EventPricingData;
import org.oscm.billingservice.service.model.BillingInput;
import org.oscm.domobjects.BillingResult;
import org.oscm.i18nservice.local.LocalizerServiceLocal;

/**
 * @author kulle
 * 
 */
public class EventCalculator {

    private final BillingResultAssembler assembler = new BillingResultAssembler();
    private final LocalizerServiceLocal localizer;
    private final BillingDataRetrievalServiceLocal bdr;

    public EventCalculator(BillingDataRetrievalServiceLocal bdr,
            LocalizerServiceLocal localizer) {
        this.bdr = bdr;
        this.localizer = localizer;
    }

    public BigDecimal calculateEventCosts(final BillingInput billingInput,
            final PriceModelInput priceModelInput, final BillingResult result,
            final PriceModelType priceModelType) {

        EventCosts eventCosts = computeEventRevenue(billingInput,
                priceModelInput);
        assembler.addEvents(priceModelType, eventCosts, localizer);
        updateGatheredEvents(billingInput, priceModelInput, result);
        return eventCosts.getNormalizedTotalCosts();
    }

    private EventCosts computeEventRevenue(final BillingInput billingInput,
            final PriceModelInput priceModelInput) {

        final Map<String, EventPricingData> eventPrices = bdr.loadEventPricing(
                priceModelInput.getPriceModelKey(),
                priceModelInput.getPriceModelPeriodEnd());

        long adjStartTime = adjustStartTimeForPerUnitPriceModel(billingInput,
                priceModelInput);
        long adjEndTime = adjustEndTimeForPerUnitPriceModel(billingInput,
                priceModelInput);
        final List<EventCount> eventStatistics = bdr.loadEventStatistics(
                billingInput.getSubscriptionKey(), adjStartTime, adjEndTime);

        CostCalculator calculator = priceModelInput.getCostCalculator();
        return calculator.calculateCostsForGatheredEventsInPeriod(eventPrices,
                eventStatistics);
    }

    /**
     * Used to compute event costs. The start date of a price model must be
     * moved to the billing period start in case of an overlapping unit. The
     * start of the unit would result in reading to much event history data.
     */
    private long adjustStartTimeForPerUnitPriceModel(
            final BillingInput billingInput,
            final PriceModelInput priceModelInput) {

        long adjustedStartTime = priceModelInput
                .getPmStartAdjustedToFreePeriod();
        if (priceModelInput.isPerUnitPriceModel()) {
            if (priceModelInput.isYoungestPriceModelOfPeriod()
                    && !priceModelInput.isResumedPriceModel()) {
                if (priceModelInput.getFreePeriodEnd() > billingInput
                        .getBillingPeriodStart()) {
                    // free period end is smaller than price model period
                    // end checked by billPriceModel method
                    adjustedStartTime = priceModelInput.getFreePeriodEnd();
                } else {
                    adjustedStartTime = billingInput.getBillingPeriodStart();
                }
            }
        }
        return adjustedStartTime;
    }

    /**
     * Used to compute event costs. The end date of a price model must be moved
     * to the billing period end in case of an overlapping unit. The end of the
     * unit will result in reading to less event history data.
     */
    private long adjustEndTimeForPerUnitPriceModel(
            final BillingInput billingInput,
            final PriceModelInput priceModelInput) {

        long adjustetEndTime = priceModelInput.getPriceModelPeriodEnd();
        if (priceModelInput.getPriceModelHistory().getDataContainer().getType() == org.oscm.internal.types.enumtypes.PriceModelType.PER_UNIT) {
            if (priceModelInput.isOldestPriceModelOfPeriod()
                    && priceModelInput.getDeactivationTime() == -1) {
                adjustetEndTime = billingInput.getBillingPeriodEnd();
            }
        }
        return adjustetEndTime;
    }

    private void updateGatheredEvents(final BillingInput billingInput,
            final PriceModelInput priceModelInput, final BillingResult result) {

        if (billingInput.isStoreBillingResult()) {
            // for all events considered in the previous step, create a
            // reference to the billing result object
            bdr.updateEvent(priceModelInput.getPmStartAdjustedToFreePeriod(),
                    priceModelInput.getPriceModelPeriodEnd(),
                    billingInput.getSubscriptionKey(), result);
        }
    }

}
