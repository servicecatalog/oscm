/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *                                                                              
 *  Creation Date: Dec 10, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.calculation.revenue;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.oscm.billingservice.business.calculation.BigDecimals;
import org.oscm.billingservice.business.calculation.revenue.model.EventCosts;
import org.oscm.billingservice.business.calculation.revenue.model.UserAssignmentFactors;
import org.oscm.billingservice.dao.BillingDataRetrievalServiceLocal;
import org.oscm.billingservice.dao.model.EventCount;
import org.oscm.billingservice.dao.model.EventPricingData;
import org.oscm.billingservice.dao.model.ParameterRolePricingData;
import org.oscm.billingservice.dao.model.RolePricingDetails;
import org.oscm.billingservice.dao.model.SteppedPriceData;
import org.oscm.billingservice.dao.model.SteppedPriceDetail;
import org.oscm.billingservice.dao.model.VatRateDetails;
import org.oscm.billingservice.dao.model.XParameterData;
import org.oscm.billingservice.dao.model.XParameterPeriodValue;
import org.oscm.billingservice.service.model.BillingInput;
import org.oscm.converter.BigDecimalComparator;
import org.oscm.converter.PriceConverter;
import org.oscm.domobjects.ParameterHistory;
import org.oscm.domobjects.PriceModelHistory;
import org.oscm.domobjects.RoleDefinitionHistory;
import org.oscm.domobjects.SubscriptionHistory;
import org.oscm.domobjects.UsageLicenseHistory;
import org.oscm.types.exceptions.BillingRunFailed;
import org.oscm.internal.types.enumtypes.ParameterValueType;
import org.oscm.internal.types.enumtypes.PriceModelType;
import org.oscm.internal.types.enumtypes.PricingPeriod;
import org.oscm.internal.types.exception.IllegalArgumentException;

/**
 * @author afschar
 * 
 */
public abstract class CostCalculator {

    protected static final BigDecimal MILLISECONDS_PER_DAY = BigDecimal
            .valueOf(PricingPeriod.DAY.getMilliseconds());

    protected static final BigDecimal ZERO_NORMALIZED = BigDecimal.ZERO
            .setScale(PriceConverter.NORMALIZED_PRICE_SCALING);

    private final static CostCalculator PRO_RATA_CALCULATOR = new CostCalculatorProRata();

    private final static CostCalculator PER_UNIT_CALCULATOR = new CostCalculatorPerUnit();

    public static final CostCalculator get(PriceModelHistory priceModel) {
        if (priceModel.getType() == PriceModelType.PRO_RATA) {
            return PRO_RATA_CALCULATOR;
        } else if (priceModel.getType() == PriceModelType.PER_UNIT) {
            return PER_UNIT_CALCULATOR;
        } else {
            throw new IllegalArgumentException(
                    "Factor calculator for free of charge price model does not exist.");
        }
    }

    public abstract long determineStartTime(long startTimeForPeriod,
            long endTimeForPeriod, PricingPeriod period);

    public abstract long computeEndTimeForPaymentPreview(long endTimeForPeriod,
            long billingPeriodEnd, PricingPeriod period);

    public abstract double computeFactorForUsageTime(
            PricingPeriod pricingPeriod, BillingInput billingInput,
            long usagePeriodStart, long usagePeriodEnd);

    /**
     * Calculate factor of user and user role by iterating user license
     * histories.
     * 
     * @param ulHistList
     *            Usage license hisotry list for one subscription which are
     *            ordered by user key ASC and history version DESC
     * @param referencePMHistory
     *            Price model history object of the subscription
     * @param billingStart
     *            Start time of target billing period
     * @param billingEnd
     *            End time of target billing period
     * @param periodStart
     *            Start time of charged period (e.g. start time of subscription)
     * @param periodEnds
     *            End time of charged period (e.g. time when subscription was
     *            terminated)
     * @return User assignment factor, never null
     */
    public abstract UserAssignmentFactors computeUserAssignmentsFactors(
            List<UsageLicenseHistory> ulHistList,
            PriceModelHistory referencePMHistory, BillingInput billingInput,
            long periodStart, long periodEnd);

    public abstract long computeUserAssignmentStartTimeForParameters(
            PricingPeriod period, long paramValueEndTime,
            ParameterHistory paramHist, PriceModelHistory pmh,
            long paramValueStartTime);

    public abstract boolean isSuspendedAndResumedInSameTimeUnit(
            SubscriptionHistory current, SubscriptionHistory next,
            PriceModelHistory pm);

    public abstract void computeParameterPeriodFactor(BillingInput input,
            XParameterData parameterData, long startTimeForPeriod,
            long endTimeForPeriod);

    public abstract void computeParameterUserFactorAndRoleFactor(
            BillingDataRetrievalServiceLocal billingDao, BillingInput input,
            XParameterData parameterData, long startTimeForPeriod,
            long endTimeForPeriod);

    public abstract BigDecimal calculateParameterUserCosts(
            XParameterPeriodValue parameterPeriodValue,
            BigDecimal valueMultplier);

    /**
     * Calculates the sum for all gathered events of that kind for the
     * subscription in the given time frame.
     */
    public EventCosts calculateCostsForGatheredEventsInPeriod(
            Map<String, EventPricingData> eventPrices,
            List<EventCount> eventStatistics) {

        if (eventPrices == null || eventStatistics == null) {
            throw new IllegalArgumentException(
                    "Input parameters can not be null.");
        }

        EventCosts eventCosts = new EventCosts();

        BigDecimal priceForAllEvents = ZERO_NORMALIZED;
        List<EventCount> resultEventList = new ArrayList<EventCount>();

        for (EventCount entry : eventStatistics) {
            // now match the found event occurrences with the previously
            // obtained event_id-price list

            EventPricingData eventPricingData = eventPrices.get(entry
                    .getEventIdentifier());

            BigDecimal priceForOneEvent = ZERO_NORMALIZED;
            List<SteppedPriceData> eventSteppedPrice = null;
            if (eventPricingData != null) {
                BigDecimal price = eventPricingData.getPrice();
                if (price != null) {
                    priceForOneEvent = price;
                }
                eventSteppedPrice = eventPricingData.getEventSteppedPrice();
                entry.setEventKey(eventPricingData.getEventKey());

            }

            BigDecimal priceForEventsWithEventIdentifier = ZERO_NORMALIZED;

            /** Flag for stepped price cost calculation. */
            boolean isStepPriceDefined = false;

            if (eventSteppedPrice != null && eventSteppedPrice.size() > 0) {
                // there are stepped prices
                // define price for exact step
                isStepPriceDefined = true;
                BigDecimal numberOfOccurrences = BigDecimal.valueOf(entry
                        .getNumberOfOccurrences());
                SteppedPriceDetail steppedPriceDetail = new SteppedPriceDetail(
                        ZERO_NORMALIZED);
                steppedPriceDetail.setPriceData(eventSteppedPrice);
                SteppedPriceDetail stepDetail = calculateStepCost(
                        steppedPriceDetail, numberOfOccurrences);
                if (stepDetail != null && stepDetail.getCost() != null) {
                    priceForOneEvent = stepDetail.getCost();
                }
            }
            if (BigDecimalComparator.isZero(priceForOneEvent)) {
                // if the price is not known, the event is considered as not
                // relevant for the billing, so
                continue;
            }
            // add event price to total sum
            if (isStepPriceDefined) {
                // cost already defined on step definition and number of
                // parameter occurrences is included
                priceForEventsWithEventIdentifier = priceForOneEvent;
            } else {
                priceForEventsWithEventIdentifier = priceForOneEvent
                        .multiply(BigDecimal.valueOf(entry
                                .getNumberOfOccurrences()));
            }
            priceForAllEvents = priceForAllEvents
                    .add(priceForEventsWithEventIdentifier);

            entry.setPriceForOneEvent(priceForOneEvent);
            entry.setPriceForEventsWithEventIdentifier(priceForEventsWithEventIdentifier);
            entry.setEventSteppedPrice(eventSteppedPrice);

            resultEventList.add(entry);
        }

        eventCosts.setTotalCosts(priceForAllEvents);
        eventCosts.setEventCountList(resultEventList);

        return eventCosts;
    }

    /**
     * Define price for step. There are next relations for define stepped price:
     * Entity - price type - limit: PriceModel - pricePerUser - userNumber.
     * Event - price - eventNumber. Parameter - pricePerSubscription -
     * parameterValue.
     * 
     * @param steppedPriceList
     *            Details on stepped prices. The list has to be sorted DESC and
     *            NULL has to be last on data retrieval used: ORDER BY limit
     *            DESC NULLS LAST.
     * @param limit
     *            Value of step.
     * @return Price data for defined limit step. Can be null if there is no
     *         needed step.
     */
    public SteppedPriceDetail calculateStepCost(
            SteppedPriceDetail steppedPricesDetails, BigDecimal entityCount) {
        final List<SteppedPriceData> priceData = steppedPricesDetails
                .getPriceData();
        if (priceData == null || priceData.isEmpty()) {
            return steppedPricesDetails;
        }

        // create a new list with all step data
        final List<SteppedPriceData> steppedPrices = new ArrayList<SteppedPriceData>();
        BigDecimal costs = null;
        for (SteppedPriceData currentStep : priceData) {
            final BigDecimal stepEntityCount;
            if (currentStep.getLimit() == null
                    || currentStep.getLimit().doubleValue() >= entityCount
                            .doubleValue()) {
                // we have reached the final step
                if (costs == null) {
                    stepEntityCount = entityCount.subtract(new BigDecimal(
                            currentStep.getFreeEntityCount()));
                    costs = stepEntityCount
                            .multiply(currentStep.getBasePrice())
                            .add(currentStep.getAdditionalPrice())
                            .setScale(PriceConverter.NORMALIZED_PRICE_SCALING,
                                    RoundingMode.HALF_UP);
                } else {
                    stepEntityCount = BigDecimal.ZERO;
                }
            } else {
                // add the step in between
                stepEntityCount = new BigDecimal(String.valueOf(currentStep
                        .getLimit())).subtract(new BigDecimal(currentStep
                        .getFreeEntityCount()));
            }
            steppedPrices.add(new SteppedPriceData(currentStep
                    .getAdditionalPrice(), currentStep.getFreeEntityCount(),
                    currentStep.getLimit(), currentStep.getBasePrice(),
                    stepEntityCount.multiply(currentStep.getBasePrice())
                            .setScale(PriceConverter.NORMALIZED_PRICE_SCALING,
                                    RoundingMode.HALF_UP), stepEntityCount));
        }
        steppedPricesDetails.getPriceData().clear();
        steppedPricesDetails.getPriceData().addAll(steppedPrices);
        steppedPricesDetails.addCosts(costs == null ? BigDecimal.ZERO : costs);
        return steppedPricesDetails;
    }

    /**
     * Calculates the VAT details for the given VAT and cost data.
     * 
     * @param vatForCustomer
     *            The VAT details to be considered.
     * @return The VAT details containing the VAT amount and the total amount.
     */
    public static VatRateDetails calculateVATCosts(VatRateDetails vatForCustomer) {
        BigDecimal totalCosts = vatForCustomer.getNetCosts();
        BigDecimal vatRate = vatForCustomer.getEffectiveVatRateForCustomer();
        if (vatRate != null) {
            BigDecimal fixSum = BigDecimal.ONE;
            BigDecimal percentBaseFactor = new BigDecimal(100);
            BigDecimal factor = fixSum.add(vatRate.divide(percentBaseFactor));
            BigDecimal total = totalCosts.multiply(factor).setScale(
                    PriceConverter.NORMALIZED_PRICE_SCALING,
                    RoundingMode.HALF_UP);
            totalCosts = total;
        }
        vatForCustomer.setTotalCosts(totalCosts);
        return vatForCustomer;
    }

    /**
     * Reduce costs for discount amount.
     * 
     * @param costs
     *            Costs before discount.
     * @param discountValue
     *            Discount in percent.
     * @return Costs after discount.
     */
    public static BigDecimal calculateDiscountedCosts(BigDecimal costs,
            BigDecimal discountValue) {
        if (discountValue == null) {
            throw new IllegalArgumentException(
                    "Input parameter discountValue must not be null");
        }
        if (costs == null) {
            throw new IllegalArgumentException(
                    "Input parameter costs must not be null");
        }
        return costs.subtract(BigDecimals
                .calculatePercent(discountValue, costs));
    }

    /**
     * Calculate role related costs for price model.
     * 
     * @param rolePrices
     *            Map of TKEY of role and PricedProductRoleHistory object for
     *            getting role price.
     * @param factors
     *            Map of TKEY of role and factor for calculating cost of role.
     * @param roleDefinitions
     *            Role definitions for filling data for xml report.
     * @return Map<Long, RolePricingDetails> the first map attribute is TKEY of
     *         role definition, the second attribute is object with information
     *         of calculated cost for role and data for xml report.
     */
    public Map<Long, RolePricingDetails> calculateRoleRelatedCostsForPriceModel(
            Map<Long, RolePricingDetails> rolePrices,
            Map<Long, Double> factors,
            Map<Long, RoleDefinitionHistory> roleDefinitions) {
        if (rolePrices == null || factors == null) {
            throw new IllegalArgumentException("Input values can not be null.");
        }
        Map<Long, RolePricingDetails> resultCosts = calculateCostsForRoles(
                rolePrices, factors, roleDefinitions);
        return resultCosts;
    }

    private Map<Long, RolePricingDetails> calculateCostsForRoles(
            Map<Long, RolePricingDetails> rolePrices,
            Map<Long, Double> factors,
            Map<Long, RoleDefinitionHistory> roleDefinitions) {

        Map<Long, RolePricingDetails> resultCosts = new HashMap<Long, RolePricingDetails>();

        Set<Long> rolesKeysSet = rolePrices.keySet();
        Iterator<Long> iteratorRoles = rolesKeysSet.iterator();
        while (iteratorRoles.hasNext()) {
            Long roleKey = iteratorRoles.next();
            RolePricingDetails roleHistory = rolePrices.get(roleKey);
            Double factor = factors.get(roleKey);
            BigDecimal price = roleHistory.getPricePerUser();
            BigDecimal cost = ZERO_NORMALIZED;
            if (factor != null && price != null) {
                cost = price.multiply(BigDecimal.valueOf(factor.doubleValue()))
                        .setScale(PriceConverter.NORMALIZED_PRICE_SCALING,
                                RoundingMode.HALF_UP);
            }
            if (!BigDecimalComparator.isZero(cost)) {
                RolePricingDetails rolePricingDetails = new RolePricingDetails(
                        roleHistory.getPricedProductRoleHistory());
                rolePricingDetails.addCosts(cost);
                if (roleDefinitions != null) {
                    RoleDefinitionHistory roleDefinition = roleDefinitions
                            .get(roleKey);
                    if (roleDefinition != null && factor != null) {
                        ParameterRolePricingData xmlData = new ParameterRolePricingData();
                        xmlData.setRoleId(roleDefinition.getRoleId());
                        xmlData.setFactorForPeriod(factor.doubleValue());
                        xmlData.setBasePricePeriod(rolePrices
                                .get(roleKey)
                                .getPricePerUser()
                                .setScale(
                                        PriceConverter.NORMALIZED_PRICE_SCALING,
                                        RoundingMode.HALF_UP));
                        xmlData.setTotalPricePeriod(cost);
                        rolePricingDetails.setXmlReportData(xmlData);
                    }
                }
                resultCosts.put(roleKey, rolePricingDetails);
            }
        }
        return resultCosts;
    }

    /**
     * Determines the cost multiplier for the parameter depending on the value
     * type and value.
     * 
     * @param type
     *            Parameter type.
     * @param value
     *            Parameter value.
     * @return Calculated multiplier.
     * @throws BillingRunFailed
     */
    protected BigDecimal determineParameterValueMultiplier(
            ParameterValueType type, String value) throws BillingRunFailed {
        BigDecimal multiplier = BigDecimal.ZERO;
        if (value == null) {
            return multiplier;
        }
        // String type is not considered, multiplier remains 0 in this case
        try {
            switch (type) {
            case ENUMERATION:
                multiplier = BigDecimal.ONE;
                break;
            case BOOLEAN:
                Boolean tmp = Boolean.valueOf(value);
                if (tmp.booleanValue()) {
                    multiplier = BigDecimal.ONE;
                }
                break;
            case DURATION: // define multiplier for DURATION
                final BigDecimal msecDuration = new BigDecimal(value);
                multiplier = msecDuration.divide(MILLISECONDS_PER_DAY,
                        PriceConverter.NUMBER_OF_DECIMAL_PLACES,
                        RoundingMode.HALF_UP);
                break;
            case INTEGER:
            case LONG:
                multiplier = new BigDecimal(value);
                break;
            default:
                break;
            }
        } catch (NumberFormatException nfe) {
            throw new BillingRunFailed(nfe);
        }
        return multiplier;
    }

    /**
     * Get the duration of the time unit, in which the given time is located. A
     * daylight saving time switch over in the time unit is considered.
     * 
     * @param time
     *            a time in milliseconds
     * @param pricingPeriod
     *            the pricing period
     * @return the duration of the corresponding time unit
     */
    protected long timeUnitDuration(long time, PricingPeriod pricingPeriod) {
        Calendar timeUnitStart = PricingPeriodDateConverter.getStartTime(time,
                pricingPeriod);
        Calendar timeUnitEnd = PricingPeriodDateConverter
                .getStartTimeOfNextPeriod(time, pricingPeriod);
        long timeUnitDuration = timeUnitEnd.getTimeInMillis()
                - timeUnitStart.getTimeInMillis();
        return timeUnitDuration;
    }

    /**
     * Calculate a fractional factor for a usage in one time unit. The usage
     * period end time must be in the same time unit as the usage period start
     * time or it must be the start time of the following time unit.
     * 
     * @param startTime
     *            usage period start time
     * @param endTime
     *            usage period end time
     * @param pricingPeriod
     *            the type of the time unit
     */
    protected double computeFractionalFactorForTimeUnit(final long startTime,
            final long endTime, final PricingPeriod pricingPeriod) {

        long timeUnitDuration = timeUnitDuration(startTime, pricingPeriod);
        long duration = endTime - startTime;
        return (double) duration / (double) timeUnitDuration;
    }

    /**
     * Calculate a fractional factor for a usage period
     * 
     * @param startTime
     *            usage period start time
     * @param endTime
     *            usage period end time
     * @param pricingPeriod
     *            the type of the time unit
     * @return the fractional factor as <code>double</code>
     */
    protected double computeFractionalFactor(final long startTime,
            final long endTime, final PricingPeriod pricingPeriod) {
        if (startTime > endTime) {
            throw new BillingRunFailed(
                    "Start time for factor calculation is larger than end time");
        }

        if (startTime == endTime) {
            return 0D;
        }

        if (pricingPeriod == PricingPeriod.HOUR) {
            return (double) (endTime - startTime)
                    / (double) pricingPeriod.getMilliseconds();
        }

        double factor = 0;
        long start = startTime;
        long end = Math.min(PricingPeriodDateConverter
                .getStartTimeOfNextPeriodAsLong(startTime, pricingPeriod),
                endTime);
        factor += computeFractionalFactorForTimeUnit(start, end, pricingPeriod);

        while (end < endTime) {
            start = PricingPeriodDateConverter.getStartTimeOfNextPeriodAsLong(
                    start, pricingPeriod);

            long startOfNextTimeUnit = PricingPeriodDateConverter
                    .getStartTimeOfNextPeriodAsLong(start, pricingPeriod);
            if (endTime < startOfNextTimeUnit) {
                end = endTime;
                factor += computeFractionalFactorForTimeUnit(start, end,
                        pricingPeriod);
            } else {
                end = startOfNextTimeUnit;
                factor += 1.0D;
            }
        }

        return factor;
    }

}
