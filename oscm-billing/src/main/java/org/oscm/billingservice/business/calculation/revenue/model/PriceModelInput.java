/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 01.08.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.calculation.revenue.model;

import java.util.Date;

import org.oscm.billingservice.business.calculation.revenue.CostCalculator;
import org.oscm.domobjects.PriceModelHistory;
import org.oscm.internal.types.enumtypes.PriceModelType;
import org.oscm.internal.types.enumtypes.PricingPeriod;

/**
 * Object to store information needed to bill a price model.
 * 
 * @author kulle
 */
public class PriceModelInput {

    private final long priceModelKey;
    private final long priceModelPeriodStart;
    private final long priceModelPeriodEnd;
    private final long pmStartAdjustedToFreePeriod;
    private final PriceModelHistory priceModelHistory;
    private final long deactivationTime;
    private final boolean isResumedPriceModel;
    private final boolean oldestPriceModelOfPeriod;
    private final boolean youngestPriceModelOfPeriod;
    private final long freePeriodEnd;
    private final boolean isFreePriceModel;
    private final long adjustedBillingPeriodEnd;
    private final boolean chargeOneTimeFee;
    private final String productId;

    public PriceModelInput(long priceModelKey, long priceModelStart,
            long priceModelEnd, long pmStartAdjustedToFreePeriod,
            PriceModelHistory priceModelHistory, long deactivationTime,
            boolean isResumedPriceModel, boolean oldestPriceModelOfPeriod,
            boolean youngestPriceModelOfPeriod, long freePeriodEnd,
            boolean isFreePriceModel, long adjustedBillingPeriodEnd,
            boolean chargeOneTimeFee, String productId) {
        this.priceModelKey = priceModelKey;
        this.priceModelPeriodStart = priceModelStart;
        this.priceModelPeriodEnd = priceModelEnd;
        this.priceModelHistory = priceModelHistory;
        this.deactivationTime = deactivationTime;
        this.isResumedPriceModel = isResumedPriceModel;
        this.oldestPriceModelOfPeriod = oldestPriceModelOfPeriod;
        this.youngestPriceModelOfPeriod = youngestPriceModelOfPeriod;
        this.freePeriodEnd = freePeriodEnd;
        this.isFreePriceModel = isFreePriceModel;
        this.pmStartAdjustedToFreePeriod = pmStartAdjustedToFreePeriod;
        this.adjustedBillingPeriodEnd = adjustedBillingPeriodEnd;
        this.chargeOneTimeFee = chargeOneTimeFee;
        this.productId = productId;
    }

    /**
     * @return The technical key for the price model that was in use in the
     *         given period
     */
    public long getPriceModelKey() {
        return priceModelKey;
    }

    /**
     * @return <b>max{price model usage start time, adjusted-bp-start-time}</b>,
     *         where the adjusted-bp-start-time is either: <br />
     *         <b>1.</b> The start time of the billing period (which is never
     *         adjusted) in case of a 'PRO RATA' price model or <br />
     *         <b>2.</b> The start time of the time unit of the billing period
     *         start in case of 'PER UNIT' price model<br />
     */
    public long getPriceModelPeriodStart() {
        return priceModelPeriodStart;
    }

    /**
     * @return <b>the price model end time</b>, if it ends before the billing
     *         period end <br/>
     *         otherwise <b>the end of the billing period</b>
     */
    public long getPriceModelPeriodEnd() {
        return priceModelPeriodEnd;
    }

    public long getDeactivationTime() {
        return deactivationTime;
    }

    /**
     * @return true, if this price model is the oldest price model of the
     *         billing period.
     */
    public boolean isOldestPriceModelOfPeriod() {
        return oldestPriceModelOfPeriod;
    }

    /**
     * @return true, if this price model is the youngest price model of the
     *         period which is currently billed.
     */
    public boolean isYoungestPriceModelOfPeriod() {
        return youngestPriceModelOfPeriod;
    }

    public boolean isResumedPriceModel() {
        return isResumedPriceModel;
    }

    public long getFreePeriodEnd() {
        return freePeriodEnd;
    }

    public boolean isFreePriceModel() {
        return isFreePriceModel;
    }

    public PriceModelHistory getPriceModelHistory() {
        return priceModelHistory;
    }

    public PriceModelType getPriceModelType() {
        return priceModelHistory.getType();
    }

    public PricingPeriod getPricingPeriod() {
        return priceModelHistory.getPeriod();
    }

    public CostCalculator getCostCalculator() {
        return CostCalculator.get(priceModelHistory);
    }

    public boolean isPerUnitPriceModel() {
        return getPriceModelType() == PriceModelType.PER_UNIT;
    }

    public boolean isChargeablePriceModel() {
        return priceModelHistory.isChargeable();
    }

    public long getPmStartAdjustedToFreePeriod() {
        return pmStartAdjustedToFreePeriod;
    }

    /**
     * @return <b>the billing period end time</b> for a pro rata or free of
     *         charge price model. <br>
     *         <b> the end of the last time unit, that doesn't overlap the
     *         billing period end</b> for a per time unit price model
     */
    public long getAdjustedBillingPeriodEnd() {
        return adjustedBillingPeriodEnd;
    }

    public boolean isOneTimeFeeCharged() {
        return chargeOneTimeFee;
    }

    public String getProductId() {
        return productId;
    }

    @Override
    public String toString() {
        final StringBuilder b = new StringBuilder();
        b.append("Range: ");
        b.append(new Date(priceModelPeriodStart));
        b.append(" - ");
        b.append(new Date(priceModelPeriodEnd));
        b.append("; PriceModelKey: ");
        b.append(priceModelKey);
        b.append("; deactivationTime: ");
        b.append(new Date(deactivationTime));
        b.append("; oldestPriceModelOfPeriod: ");
        b.append(oldestPriceModelOfPeriod);
        b.append("; youngestPriceModelOfPeriod: ");
        b.append(youngestPriceModelOfPeriod);
        b.append("; isFirstPriceModelAfterResume: ");
        b.append(isResumedPriceModel);
        b.append("; End of free period: ");
        b.append(new Date(freePeriodEnd));
        b.append("; isFreePriceModel: ");
        b.append(isFreePriceModel);
        b.append("; pmStartAdjustedToFreePeriod: ");
        b.append(new Date(pmStartAdjustedToFreePeriod));
        b.append("; adjustedBillingPeriodEnd: ");
        b.append(new Date(adjustedBillingPeriodEnd));
        b.append("; charge onetime fee: ");
        b.append(chargeOneTimeFee);
        b.append("; product id: ");
        b.append(productId);
        return b.toString();
    }

}
