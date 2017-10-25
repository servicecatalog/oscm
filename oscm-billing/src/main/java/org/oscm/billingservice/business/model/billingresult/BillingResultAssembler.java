/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: Nov 30, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.model.billingresult;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.oscm.billingservice.business.calculation.revenue.model.EventCosts;
import org.oscm.billingservice.business.calculation.revenue.model.PriceModelInput;
import org.oscm.billingservice.business.calculation.revenue.model.UserAssignmentDetails;
import org.oscm.billingservice.business.calculation.revenue.model.UserAssignmentFactors;
import org.oscm.billingservice.dao.model.EventCount;
import org.oscm.billingservice.dao.model.OrganizationAddressData;
import org.oscm.billingservice.dao.model.ParameterRolePricingData;
import org.oscm.billingservice.dao.model.RolePricingDetails;
import org.oscm.billingservice.dao.model.SteppedPriceData;
import org.oscm.billingservice.dao.model.UdaBillingData;
import org.oscm.billingservice.dao.model.XParameterData;
import org.oscm.billingservice.dao.model.XParameterOption;
import org.oscm.billingservice.dao.model.XParameterPeriodEnumType;
import org.oscm.billingservice.dao.model.XParameterPeriodValue;
import org.oscm.billingservice.service.model.BillingInput;
import org.oscm.converter.BigDecimalComparator;
import org.oscm.converter.DateConverter;
import org.oscm.converter.PriceConverter;
import org.oscm.domobjects.BillingResult;
import org.oscm.domobjects.PriceModelData;
import org.oscm.domobjects.PriceModelHistory;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.types.exceptions.BillingRunFailed;
import org.oscm.internal.vo.VOLocalizedText;
import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;

/**
 * @author afschar
 * 
 */
public class BillingResultAssembler {

    private final ObjectFactory factory = new ObjectFactory();

    /**
     * Creates an initial form for the billing information, containing the
     * organization related data.
     * 
     * @param organizationKey
     *            The organization whose information are given in the final
     *            billing document.
     * @param subscriptionKey
     *            the subscription key
     * @param billingResult
     *            The billing result to get the key and the charging
     *            organization key from
     * @param startDate
     *            The date the billing will start at.
     * @param endDate
     *            The date the billing will end at.
     * @param storeResultXML
     *            Indicates whether to store or not the billing result XML. If
     *            <code>true</code> the events belonging to this billing run
     *            should be updated with a reference to the billing result XML
     *            or not.
     * @return The billing details element, used for further processing. @ *
     *         Thrown in case the organization related history data cannot be
     *         found.
     */
    public BillingDetailsType createBasicBillDocumentForOrganization(
            final OrganizationAddressData orgData,
            List<UdaBillingData> udasForCustomer, long organizationKey,
            long subscriptionKey, BillingResult billingResult, long startDate,
            long endDate, boolean storeResultXML) {
        final BillingDetailsType billingDetailsType = factory
                .createBillingDetailsType();
        billingDetailsType.setTimezone(DateConverter
                .getCurrentTimeZoneAsUTCString());
        if (storeResultXML) {
            billingDetailsType.setKey(Long.valueOf(billingResult.getKey()));
        }

        if (orgData == null) {
            // organization history data was deleted, what should not have
            // happened.
            throw new BillingRunFailed(
                    "Organization data has been deleted for organization key '"
                            + organizationKey + "'");
        }

        // adding the period information
        billingDetailsType.setPeriod(factory.createPeriodType());
        billingDetailsType.getPeriod().setStartDate(Long.valueOf(startDate));
        billingDetailsType.getPeriod().setStartDateIsoFormat(
                XMLGregorianCalendarImpl.parse(DateConverter
                        .convertLongToIso8601DateTimeFormat(startDate, TimeZone
                                .getTimeZone(DateConverter.TIMEZONE_ID_GMT))));
        billingDetailsType.getPeriod().setEndDate(Long.valueOf(endDate));
        billingDetailsType.getPeriod().setEndDateIsoFormat(
                XMLGregorianCalendarImpl.parse(DateConverter
                        .convertLongToIso8601DateTimeFormat(endDate, TimeZone
                                .getTimeZone(DateConverter.TIMEZONE_ID_GMT))));

        // adding the organization details
        billingDetailsType.setOrganizationDetails(factory
                .createOrganizationDetailsType());
        billingDetailsType.getOrganizationDetails()
                .setEmail(orgData.getEmail());
        billingDetailsType.getOrganizationDetails().setName(
                orgData.getOrganizationName());
        billingDetailsType.getOrganizationDetails().setId(
                orgData.getOrganizationId());
        billingDetailsType.getOrganizationDetails().setAddress(
                orgData.getAddress());
        billingDetailsType.getOrganizationDetails().setPaymenttype(
                orgData.getPaymentTypeId());

        if (udasForCustomer != null) {
            billingDetailsType.getOrganizationDetails().setUdas(
                    factory.createUdasType());
            for (UdaBillingData uda : udasForCustomer) {
                final UdaType udaType = factory.createUdaType();
                udaType.setId(uda.getIdentifier());
                udaType.setValue(uda.getValue());
                billingDetailsType.getOrganizationDetails().getUdas().getUda()
                        .add(udaType);
            }
        }
        billingDetailsType.setSubscriptions(factory.createSubscriptionsType());

        // adding the subscription parent node
        return billingDetailsType;
    }

    public PriceModelType initPriceModel(PriceModelHistory referenceHistory,
            long startTimeForPeriod, long endTimeForPeriod, String productId) {
        final PriceModelType priceModelType = factory.createPriceModelType();
        priceModelType.setId(String.valueOf(referenceHistory.getObjKey()));
        priceModelType.setServiceId(productId);
        priceModelType.setCalculationMode(PriceModelCalculationType
                .fromValue(referenceHistory.getType().name()));

        // add usage period information
        final PeriodType periodType = factory.createPeriodType();
        periodType.setStartDate(Long.valueOf(startTimeForPeriod));
        periodType.setStartDateIsoFormat(XMLGregorianCalendarImpl
                .parse(DateConverter.convertLongToIso8601DateTimeFormat(
                        startTimeForPeriod,
                        TimeZone.getTimeZone(DateConverter.TIMEZONE_ID_GMT))));
        periodType.setEndDate(Long.valueOf(endTimeForPeriod));
        periodType.setEndDateIsoFormat(XMLGregorianCalendarImpl
                .parse(DateConverter.convertLongToIso8601DateTimeFormat(
                        endTimeForPeriod,
                        TimeZone.getTimeZone(DateConverter.TIMEZONE_ID_GMT))));
        priceModelType.setUsagePeriod(periodType);
        return priceModelType;
    }

    public ParameterType initParameter(XParameterPeriodValue periodValue,
            PriceModelInput priceModelInput) {
        final ParameterType parameterType = factory.createParameterType();
        parameterType.setId(periodValue.getId());
        final TimeZone timeZone = TimeZone
                .getTimeZone(DateConverter.TIMEZONE_ID_GMT);

        long periodStart = periodValue.getStartTime();
        long periodEnd = usageEndTimeForBillingResult(periodStart,
                periodValue.getEndTime(), priceModelInput);

        final PeriodType periodType = factory.createPeriodType();
        periodType.setStartDate(Long.valueOf(periodStart));
        periodType.setStartDateIsoFormat(XMLGregorianCalendarImpl
                .parse(DateConverter.convertLongToIso8601DateTimeFormat(
                        periodStart, timeZone)));
        periodType.setEndDate(Long.valueOf(periodEnd));
        periodType.setEndDateIsoFormat(XMLGregorianCalendarImpl
                .parse(DateConverter.convertLongToIso8601DateTimeFormat(
                        periodEnd, timeZone)));
        parameterType.setParameterUsagePeriod(periodType);

        final ParameterValueType parameterValueType = factory
                .createParameterValueType();
        parameterValueType.setType(ParameterDataType.valueOf(periodValue
                .getValueType().name()));
        parameterValueType.setAmount(periodValue.getValue());
        parameterType.setParameterValue(parameterValueType);
        return parameterType;
    }

    public ParameterPeriodFeeType initParameterPeriodFee(
            XParameterPeriodValue periodValue, XParameterData parameterData) {
        if (periodValue.getValueType() != org.oscm.internal.types.enumtypes.ParameterValueType.ENUMERATION) {
            final ParameterPeriodFeeType parameterPeriodFeeType = factory
                    .createParameterPeriodFeeType();
            parameterPeriodFeeType.setBasePeriod(BasePeriodType
                    .valueOf(parameterData.getPeriod().name()));
            // only write base price if no stepped prices are
            // defined
            if (!periodValue.getSteppedPricesForParameter()
                    .areSteppedPricesDefined()) {
                parameterPeriodFeeType.setBasePrice(periodValue
                        .getPricePerSubscription());
            }
            parameterPeriodFeeType.setFactor(BigDecimal.valueOf(periodValue
                    .getPeriodFactor()));

            // only add the value factor if the entry does not
            // belong to an option
            if (!periodValue.isParameterOption()) {
                parameterPeriodFeeType.setValueFactor((float) periodValue
                        .getValueFactor());
            }
            parameterPeriodFeeType.setPrice(periodValue
                    .getTotalCostsForSubscription());
            if (periodValue.getSteppedPricesForParameter()
                    .areSteppedPricesDefined()) {
                final SteppedPricesType steppedPricesType = factory
                        .createSteppedPricesType();
                steppedPricesType.setAmount(periodValue
                        .getSteppedPricesForParameter().getNormalizedCost());

                for (SteppedPriceData data : periodValue
                        .getSteppedPricesForParameter().getPriceData()) {
                    final SteppedPriceType steppedPriceType = factory
                            .createSteppedPriceType();
                    steppedPriceType.setAdditionalPrice(data
                            .getAdditionalPrice());
                    steppedPriceType.setBasePrice(data.getBasePrice());
                    steppedPriceType.setFreeAmount(data.getFreeEntityCount());
                    steppedPriceType.setLimit(String.valueOf(data.getLimit()));
                    steppedPriceType.setStepAmount(data.getStepAmount());
                    steppedPriceType.setStepEntityCount(data
                            .getStepEntityCount());
                    steppedPricesType.getSteppedPrice().add(steppedPriceType);
                }
                parameterPeriodFeeType.setSteppedPrices(steppedPricesType);
            }
            return parameterPeriodFeeType;
        }
        return null;
    }

    public ParameterUserAssignmentCostsType initParameterUserAssignmentCosts(
            XParameterPeriodValue periodValue, XParameterData parameterData) {
        if (periodValue.getValueType() != org.oscm.internal.types.enumtypes.ParameterValueType.ENUMERATION) {
            final ParameterUserAssignmentCostsType parameterUserAssignmentCostsType = factory
                    .createParameterUserAssignmentCostsType();
            parameterUserAssignmentCostsType.setBasePeriod(BasePeriodType
                    .valueOf(parameterData.getPeriod().name()));
            parameterUserAssignmentCostsType.setBasePrice(periodValue
                    .getPricePerUser());
            parameterUserAssignmentCostsType.setFactor(BigDecimal
                    .valueOf(periodValue.getUserAssignmentFactor()));

            // only print value factor in case it is not for
            // a parameter option
            if (!periodValue.isParameterOption()) {
                parameterUserAssignmentCostsType
                        .setValueFactor((float) periodValue.getValueFactor());
            }
            parameterUserAssignmentCostsType.setPrice(periodValue
                    .getTotalCostsForUser());

            parameterUserAssignmentCostsType.setTotal(periodValue
                    .getTotalUserAssignmentCosts());

            if (periodValue.getRolePrices() != null
                    && !periodValue.getRolePrices()
                            .getAllRolePrices(periodValue.getKey()).isEmpty()) {
                final RoleCostsType roleCostsType = factory
                        .createRoleCostsType();
                roleCostsType.setTotal(periodValue.getRolePrices().getCosts());

                // add single role cost entries
                for (RolePricingDetails rolePrice : periodValue.getRolePrices()
                        .getAllRolePrices(periodValue.getKey())) {
                    final RoleCostType roleCostType = factory
                            .createRoleCostType();
                    roleCostType.setBasePrice(rolePrice.getPricePerUser());
                    roleCostType.setFactor(BigDecimal.valueOf(rolePrice
                            .getFactor()));
                    roleCostType.setId(rolePrice.getRoleId());
                    roleCostType.setPrice(rolePrice.getCost());
                    roleCostsType.getRoleCost().add(roleCostType);
                }
                parameterUserAssignmentCostsType.setRoleCosts(roleCostsType);
            }
            return parameterUserAssignmentCostsType;
        }
        return null;
    }

    public ParameterOptionsType initParameterOptions(
            XParameterPeriodValue periodValue, XParameterData parameterData) {
        if (periodValue.isParameterOption()) {
            final ParameterOptionsType parameterOptionsType = factory
                    .createParameterOptionsType();
            XParameterPeriodEnumType enumType = (XParameterPeriodEnumType) periodValue;
            XParameterOption parameterOption = enumType.getParameterOption();

            final ParameterOptionType parameterOptionType = factory
                    .createParameterOptionType();
            parameterOptionType.setId(parameterOption.getId());

            if (periodValue.getValueType() == org.oscm.internal.types.enumtypes.ParameterValueType.ENUMERATION) {
                final PeriodFeeType periodFeeType = factory
                        .createPeriodFeeType();
                periodFeeType.setBasePeriod(BasePeriodType
                        .valueOf(parameterData.getPeriod().name()));
                // only write base price if no stepped
                // prices are defined
                if (!periodValue.getSteppedPricesForParameter()
                        .areSteppedPricesDefined()) {
                    periodFeeType.setBasePrice(periodValue
                            .getPricePerSubscription());
                }
                periodFeeType.setFactor(BigDecimal.valueOf(periodValue
                        .getPeriodFactor()));
                periodFeeType.setPrice(periodValue
                        .getTotalCostsForSubscription());
                parameterOptionType.setPeriodFee(periodFeeType);
            }

            if (periodValue.getValueType() == org.oscm.internal.types.enumtypes.ParameterValueType.ENUMERATION) {
                final ParametersUserAssignmentCostsType parametersUserAssignmentCostsType = factory
                        .createParametersUserAssignmentCostsType();
                parametersUserAssignmentCostsType.setBasePeriod(BasePeriodType
                        .valueOf(parameterData.getPeriod().name()));
                parametersUserAssignmentCostsType.setBasePrice(periodValue
                        .getPricePerUser());
                parametersUserAssignmentCostsType.setFactor(BigDecimal
                        .valueOf(periodValue.getUserAssignmentFactor()));
                parametersUserAssignmentCostsType.setPrice(periodValue
                        .getTotalCostsForUser());
                parametersUserAssignmentCostsType.setTotal(periodValue
                        .getTotalUserAssignmentCosts());

                if (periodValue.getRolePrices() != null) {
                    parametersUserAssignmentCostsType.setRoleCosts(factory
                            .createRoleCostsType());
                    parametersUserAssignmentCostsType.getRoleCosts().setTotal(
                            periodValue.getRolePrices().getCosts());
                    for (RolePricingDetails rolePrice : periodValue
                            .getRolePrices().getAllRolePrices(
                                    periodValue.getKey())) {
                        final RoleCostType roleCostType = factory
                                .createRoleCostType();
                        roleCostType.setBasePrice(rolePrice.getPricePerUser());
                        roleCostType.setFactor(BigDecimal.valueOf(rolePrice
                                .getFactor()));
                        roleCostType.setId(rolePrice.getRoleId());
                        roleCostType.setPrice(rolePrice.getCost());
                        parametersUserAssignmentCostsType.getRoleCosts()
                                .getRoleCost().add(roleCostType);
                    }
                }
                parameterOptionType
                        .setUserAssignmentCosts(parametersUserAssignmentCostsType);
            }

            parameterOptionType.setOptionCosts(factory
                    .createNormalizedCostsType());
            parameterOptionType.getOptionCosts().setAmount(
                    enumType.getTotalCosts());

            parameterOptionsType.getOption().add(parameterOptionType);
            return parameterOptionsType;
        }
        return null;
    }

    public void addEvents(PriceModelType priceModelType, EventCosts eventCosts,
            LocalizerServiceLocal localizer) {
        if (eventCosts.getEventCountList().size() > 0) {
            priceModelType
                    .setGatheredEvents(factory.createGatheredEventsType());
            for (EventCount event : eventCosts.getEventCountList()) {
                final EventType eventType = factory.createEventType();
                priceModelType.getGatheredEvents().getEvent().add(eventType);
                eventType.setId(event.getEventIdentifier());
                final List<VOLocalizedText> localizedValues = localizer
                        .getLocalizedValues(event.getEventKey(),
                                LocalizedObjectTypes.EVENT_DESC);

                for (VOLocalizedText localizedText : localizedValues) {
                    final DescriptionType text = factory
                            .createDescriptionType();
                    text.setValue(localizedText.getText());
                    text.setLang(localizedText.getLocale());
                    eventType.getDescription().add(text);
                }

                if (event.getEventSteppedPrice() != null
                        && event.getEventSteppedPrice().size() > 0) {
                    final SteppedPricesType steppedPricesType = factory
                            .createSteppedPricesType();
                    for (SteppedPriceData stepPrice : event
                            .getEventSteppedPrice()) {
                        final SteppedPriceType steppedPriceType = factory
                                .createSteppedPriceType();
                        steppedPriceType
                                .setLimit(stepPrice.getLimit() == null ? "null"
                                        : String.valueOf(stepPrice.getLimit()));
                        steppedPriceType.setFreeAmount(stepPrice
                                .getFreeEntityCount());
                        steppedPriceType.setBasePrice(stepPrice.getBasePrice());
                        steppedPriceType.setAdditionalPrice(stepPrice
                                .getAdditionalPrice());
                        steppedPriceType.setStepAmount(stepPrice
                                .getStepAmount());
                        steppedPriceType.setStepEntityCount(stepPrice
                                .getStepEntityCount());
                        steppedPricesType.getSteppedPrice().add(
                                steppedPriceType);
                    }
                    steppedPricesType.setAmount(event
                            .getPriceForEventsWithEventIdentifier());
                    eventType.setSteppedPrices(steppedPricesType);
                } else {
                    eventType.setSingleCost(factory.createCostsType());
                    eventType.getSingleCost().setAmount(
                            event.getPriceForOneEvent());
                }
                eventType.setNumberOfOccurrence(factory
                        .createNumberOfOccurrenceType());
                eventType.getNumberOfOccurrence().setAmount(
                        event.getNumberOfOccurrences());
                eventType.setCostForEventType(factory
                        .createNormalizedCostsType());
                eventType.getCostForEventType().setAmount(
                        event.getPriceForEventsWithEventIdentifier());
                priceModelType.getGatheredEvents().setGatheredEventsCosts(
                        factory.createNormalizedCostsType());
                priceModelType.getGatheredEvents().getGatheredEventsCosts()
                        .setAmount(eventCosts.getNormalizedTotalCosts());
            }
        }
    }

    /**
     * Adds a UserAssignmentCostsByUser node for one user assignment with
     * userid, factor and price.
     * 
     * @param userAssigmentCostsType
     *            the parent UserAssignmentCosts node
     * @param userAssignmentsFactors
     *            the computed user assignment factors
     */
    public void addCostsPerUser(
            final UserAssignmentCostsType userAssigmentCostsType,
            final UserAssignmentFactors userAssignmentsFactors) {
        // the periods for each user of this subscription have been
        // determined. Now handle them according to the settings in the
        // price model
        final Set<Long> userKeys = userAssignmentsFactors.getUserKeys();
        for (long userKey : userKeys) {
            final UserAssignmentDetails user = userAssignmentsFactors
                    .getUserAssignmentDetails(Long.valueOf(userKey));
            final UserAssignmentCostsByUserType userAssignmentCostsByUserType = factory
                    .createUserAssignmentCostsByUserType();
            userAssignmentCostsByUserType.setUserId(user.getUserId());
            userAssignmentCostsByUserType.setFactor(BigDecimal.valueOf(user
                    .getUsageDetails().getFactor()));
            userAssigmentCostsType.getUserAssignmentCostsByUser().add(
                    userAssignmentCostsByUserType);
        }
    }

    public BigDecimal addRolesCosts(
            UserAssignmentCostsType userAssigmentCostsType,
            Map<Long, RolePricingDetails> roleCosts) {

        // prepare xml output result
        final List<ParameterRolePricingData> pricedRoles = new ArrayList<ParameterRolePricingData>();

        // Do not set scale for initial value because
        // math operation is followed afterwards
        BigDecimal totalRolesCosts = BigDecimal.ZERO;

        final Set<Long> rolesKeysSet = roleCosts.keySet();
        for (Long key : rolesKeysSet) {
            RolePricingDetails details = roleCosts.get(key);
            ParameterRolePricingData xmlData = details.getXmlReportData();

            pricedRoles.add(xmlData);
            totalRolesCosts = totalRolesCosts.add(details.getCost());
        }

        // normalize total role costs
        totalRolesCosts = totalRolesCosts.setScale(
                PriceConverter.NORMALIZED_PRICE_SCALING, RoundingMode.HALF_UP);

        if (!pricedRoles.isEmpty()) {
            final RoleCostsType roleCostsType = factory.createRoleCostsType();
            for (ParameterRolePricingData prpd : pricedRoles) {
                final RoleCostType roleCostType = factory.createRoleCostType();
                roleCostType.setId(prpd.getRoleId());
                roleCostType.setBasePrice(prpd.getBasePricePeriod());
                roleCostType.setFactor(BigDecimal.valueOf(prpd
                        .getFactorForPeriod()));
                roleCostType.setPrice(prpd.getTotalPricePeriod());
                roleCostsType.getRoleCost().add(roleCostType);
            }
            roleCostsType.setTotal(totalRolesCosts);
            userAssigmentCostsType.setRoleCosts(roleCostsType);
        }

        return totalRolesCosts;
    }

    public BigDecimal addOneTimeFee(PriceModelType priceModelType,
            PriceModelHistory referenceHistory) {

        BigDecimal oneTimeFee = referenceHistory.getOneTimeFee();
        if (!BigDecimalComparator.isZero(oneTimeFee)) {
            OneTimeFeeType oneTimeFeeType = factory.createOneTimeFeeType();
            oneTimeFeeType.setBaseAmount(oneTimeFee);
            oneTimeFeeType.setFactor(1);
            oneTimeFee = oneTimeFee.setScale(
                    PriceConverter.NORMALIZED_PRICE_SCALING,
                    RoundingMode.HALF_UP);
            oneTimeFeeType.setAmount(oneTimeFee);
            priceModelType.setOneTimeFee(oneTimeFeeType);
        } else {
            oneTimeFee = BigDecimal.ZERO.setScale(
                    PriceConverter.NORMALIZED_PRICE_SCALING,
                    RoundingMode.HALF_UP);
        }
        return oneTimeFee;
    }

    public PriceModelType initializePriceModelType(
            final BillingInput billingInput,
            final PriceModelInput priceModelInput,
            final PriceModelsType priceModelsType) {

        PriceModelHistory priceModelHistory = priceModelInput
                .getPriceModelHistory();

        long usageStart = priceModelInput.getPmStartAdjustedToFreePeriod();
        long usageEnd = usageEndTimeForBillingResult(usageStart,
                priceModelInput.getPriceModelPeriodEnd(), priceModelInput);
        String productId = priceModelInput.getProductId();

        final PriceModelType priceModelType = initPriceModel(priceModelHistory,
                usageStart, usageEnd, productId);
        priceModelsType.getPriceModel().add(priceModelType);

        final PriceModelData pmHistoryData = priceModelHistory
                .getDataContainer();
        PeriodFeeType periodFee = factory.createPeriodFeeType();
        periodFee.setBasePrice(pmHistoryData.getPricePerPeriod());
        periodFee.setBasePeriod(BasePeriodType.valueOf(pmHistoryData
                .getPeriod().name()));
        periodFee.setFactor(BigDecimal.valueOf(0D));
        periodFee.setPrice(BigDecimal.ZERO
                .setScale(PriceConverter.NORMALIZED_PRICE_SCALING));
        priceModelType.setPeriodFee(periodFee);

        priceModelType.setPriceModelCosts(factory.createDetailedCostsType());
        priceModelType.getPriceModelCosts().setGrossAmount(null);
        priceModelType.getPriceModelCosts().setCurrency(
                billingInput.getCurrencyIsoCode());

        return priceModelType;
    }

    public UserAssignmentCostsType initializeUserAssignmentCosts(
            final PriceModelType priceModelType) {
        UserAssignmentCostsType userAssignmentCosts = factory
                .createUserAssignmentCostsType();
        priceModelType.setUserAssignmentCosts(userAssignmentCosts);
        return userAssignmentCosts;
    }

    public void initializeOverallCostsType(final String currenceyIsoCode,
            final BillingDetailsType billingDetails) {

        billingDetails.setOverallCosts(factory.createOverallCostsType());
        billingDetails.getOverallCosts().setCurrency(currenceyIsoCode);
    }

    /**
     * Determine the usage end time of a price model or parameter, which is put
     * into the billing result
     * 
     * @param periodStart
     *            the start of the parameter- or price model-period
     * @param periodEnd
     *            the end of the parameter- or price model-period
     * @param adjustedBillingPeriodEnd
     *            the charging relevant end of the billing period, which depends
     *            on the price model type
     * @return
     */
    private long usageEndTimeForBillingResult(long periodStart, long periodEnd,
            PriceModelInput priceModelInput) {
        if (priceModelInput.isPerUnitPriceModel()) {
            long adjustedBillingPeriodEnd = priceModelInput
                    .getAdjustedBillingPeriodEnd();

            if (periodStart >= adjustedBillingPeriodEnd) {
                return periodStart;
            } else {
                return Math.min(adjustedBillingPeriodEnd, periodEnd);
            }
        } else {
            return periodEnd;
        }
    }
}
