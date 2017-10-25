/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Mike J&auml;ger                                                      
 *                                                                              
 *  Creation Date: 10.05.2010                                                      
 *                                                                              
 *  Completion Time: 27.07.2011                                               
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.dao;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.billingservice.business.calculation.revenue.CostCalculator;
import org.oscm.billingservice.business.calculation.revenue.model.PriceModelInput;
import org.oscm.billingservice.business.calculation.revenue.model.UserAssignmentFactors;
import org.oscm.billingservice.dao.model.BillingSubscriptionData;
import org.oscm.billingservice.dao.model.EventCount;
import org.oscm.billingservice.dao.model.EventPricingData;
import org.oscm.billingservice.dao.model.OrganizationAddressData;
import org.oscm.billingservice.dao.model.ParameterOptionRolePricingData;
import org.oscm.billingservice.dao.model.RolePricingData;
import org.oscm.billingservice.dao.model.RolePricingDetails;
import org.oscm.billingservice.dao.model.SteppedPriceData;
import org.oscm.billingservice.dao.model.UdaBillingData;
import org.oscm.billingservice.dao.model.VatRateDetails;
import org.oscm.billingservice.dao.model.XParameterData;
import org.oscm.billingservice.dao.model.XParameterIdData;
import org.oscm.billingservice.dao.model.XParameterOption;
import org.oscm.billingservice.dao.model.XParameterPeriodEnumType;
import org.oscm.billingservice.dao.model.XParameterPeriodPrimitiveType;
import org.oscm.billingservice.dao.model.XParameterPeriodValue;
import org.oscm.billingservice.service.model.BillingInput;
import org.oscm.converter.ParameterizedTypes;
import org.oscm.converter.ResourceLoader;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.BillingContactHistory;
import org.oscm.domobjects.BillingResult;
import org.oscm.domobjects.BillingSubscriptionStatus;
import org.oscm.domobjects.DiscountHistory;
import org.oscm.domobjects.DomainHistoryObject;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationHistory;
import org.oscm.domobjects.ParameterDefinitionHistory;
import org.oscm.domobjects.ParameterHistory;
import org.oscm.domobjects.ParameterOptionHistory;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.PriceModelHistory;
import org.oscm.domobjects.PricedEventData;
import org.oscm.domobjects.PricedEventHistory;
import org.oscm.domobjects.PricedOptionHistory;
import org.oscm.domobjects.PricedParameterHistory;
import org.oscm.domobjects.PricedProductRoleHistory;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.ProductHistory;
import org.oscm.domobjects.RoleDefinitionHistory;
import org.oscm.domobjects.SteppedPriceHistory;
import org.oscm.domobjects.SubscriptionHistory;
import org.oscm.domobjects.SupportedCurrency;
import org.oscm.domobjects.UdaHistory;
import org.oscm.domobjects.UsageLicenseHistory;
import org.oscm.domobjects.UserGroupHistory;
import org.oscm.domobjects.VatRateHistory;
import org.oscm.domobjects.enums.ModificationType;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.types.enumtypes.UdaTargetType;
import org.oscm.types.exceptions.BillingRunFailed;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ParameterValueType;
import org.oscm.internal.types.enumtypes.PricingPeriod;
import org.oscm.internal.types.enumtypes.ServiceType;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;

/**
 * Implementation of the billing data retrieval service.
 *
 * @author Mike J&auml;ger
 *
 */
@Local(BillingDataRetrievalServiceLocal.class)
@Stateless
public class BillingDataRetrievalServiceBean implements
        BillingDataRetrievalServiceLocal {

    private static final Log4jLogger LOGGER = LoggerFactory
            .getLogger(BillingDataRetrievalServiceBean.class);

    @EJB(beanInterface = DataService.class)
    DataService dm;

    /**
     * @see BillingDataRetrievalServiceLocal#getOrganizationBillingAddressFromHistory(long)
     */
    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public OrganizationAddressData loadOrganizationBillingDataFromHistory(
            long organizationKey, long subscriptionKey) {
        Organization org = new Organization();
        org.setKey(organizationKey);
        OrganizationHistory orgHistory = (OrganizationHistory) dm
                .findLastHistory(org);
        if (orgHistory == null) {
            return null;
        }
        String organizationId = orgHistory.getOrganizationId();
        String name = orgHistory.getOrganizationName();
        String address = orgHistory.getAddress();
        String email = orgHistory.getEmail();
        String paymentTypeId = loadPaymentTypeIdForSubscription(subscriptionKey);

        BillingContactHistory billingContactHistory = loadBillingContact(subscriptionKey);
        if (billingContactHistory != null
                && !billingContactHistory.isOrgAddressUsed()) {
            name = billingContactHistory.getCompanyName();
            address = billingContactHistory.getAddress();
            email = billingContactHistory.getEmail();
        }
        name = name == null ? "" : name;
        OrganizationAddressData data = new OrganizationAddressData(address == null ? "" : address,
                name == null ? "" : name, email == null ? "" : email, organizationId);
        data.setPaymentTypeId(paymentTypeId);
        return data;
    }

    /**
     * @see BillingDataRetrievalServiceLocal#loadDiscountValue(long, long, long,
     *      long)
     */
    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public BigDecimal loadDiscountValue(long organizationKey,
            long billingStart, long pBillingEnd, long supplierKey) {

        BigDecimal discountValue = null;
        DiscountHistory discountHistory = null;

        // get all possible discount for the period
        List<DiscountHistory> discountList = findDiscounts(organizationKey,
                billingStart, pBillingEnd, supplierKey);

        if (discountList.size() > 0) {
            // take the last version of discount
            discountHistory = discountList.get(0);

            // check the discount was not deleted
            // it was deleted, when last history row has modification type
            // DELETE
            ModificationType modType = discountHistory.getModtype();
            if (modType == ModificationType.DELETE) {
                discountHistory = null;
            }
        }

        if (discountHistory != null) {
            discountValue = discountHistory.getValue();
        }

        return discountValue;
    }

    List<DiscountHistory> findDiscounts(long organizationKey,
            long billingStart, long pBillingEnd, long supplierKey) {

        // billing period end is first millisecond of next month.
        // has to reduce to last millisecond to one millisecond
        final long billingEnd = pBillingEnd - 1;

        Query query = dm
                .createNamedQuery("DiscountHistory.findForOrganizationAndPeriod");
        query.setParameter("orgKey", Long.valueOf(organizationKey));
        query.setParameter("bS", Long.valueOf(billingStart));
        query.setParameter("bE", Long.valueOf(billingEnd));
        query.setParameter("supplierKey", Long.valueOf(supplierKey));

        return ParameterizedTypes.list(query.getResultList(),
                DiscountHistory.class);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public XParameterData loadParameterData(final BillingInput billingInput,
            final PriceModelInput priceModelInput) {

        long startTime = priceModelInput.getPmStartAdjustedToFreePeriod();
        long priceModelKeyForSubscription = priceModelInput.getPriceModelKey();
        long endTime = priceModelInput.getPriceModelPeriodEnd();
        PricingPeriod period = priceModelInput.getPriceModelHistory()
                .getDataContainer().getPeriod();
        long subscriptionKey = billingInput.getSubscriptionKey();

        // load parameter relevant history data
        List<Object[]> resultList = findParameterData(
                priceModelKeyForSubscription, startTime, endTime);

        // assemble parameter data to object structure
        final XParameterData result = new XParameterData();
        result.setPeriod(period);
        long currentEndTime = endTime;
        XParameterIdData oldIdData = null;
        String oldParamValue = null;
        XParameterPeriodValue periodValue = null;
        for (Object[] entries : resultList) {
            PricedParameterHistory pph = (PricedParameterHistory) entries[0];
            ParameterHistory paramHist = (ParameterHistory) entries[1];
            ParameterDefinitionHistory pdh = (ParameterDefinitionHistory) entries[2];
            PriceModelHistory pmh = (PriceModelHistory) entries[3];

            String currentParamValue = paramHist.getValue();
            RolePricingData roleCostsForParameter = loadRoleRelatedCostsForParameters(
                    priceModelKeyForSubscription, endTime);
            ParameterOptionRolePricingData roleCostsForOptions = loadRoleRelatedCostsForOptions(
                    priceModelKeyForSubscription, endTime);

            List<SteppedPriceData> steppedPrices = getSteppedPricesForParameter(
                    pph.getObjKey(), endTime);

            XParameterIdData idData = result.getIdDataInstance(
                    pdh.getParameterId(), pdh.getParameterType(),
                    pdh.getValueType());

            boolean newParam = idData != oldIdData;
            boolean paramValueChanged = (currentParamValue == null && oldParamValue != null)
                    || (currentParamValue != null && !currentParamValue
                            .equals(oldParamValue));
            oldIdData = idData;
            currentEndTime = setEndTimeForParameterValue(endTime,
                    currentEndTime, periodValue, newParam, paramValueChanged,
                    paramHist);
            if (currentParamValue != null) {
                long paramValueStartTime = Math.max(startTime, paramHist
                        .getModdate().getTime());

                long userAssignmentValueStartTime = CostCalculator.get(pmh)
                        .computeUserAssignmentStartTimeForParameters(period,
                                currentEndTime, paramHist, pmh,
                                paramValueStartTime);
                final List<UsageLicenseHistory> ulHistList = loadUsageLicenses(
                        subscriptionKey, paramValueStartTime, currentEndTime);
                UserAssignmentFactors userFactors = CostCalculator.get(pmh)
                        .computeUserAssignmentsFactors(ulHistList, pmh,
                                billingInput, userAssignmentValueStartTime,
                                currentEndTime);
                periodValue = handleParameterPeriodValue(endTime,
                        currentEndTime, roleCostsForParameter,
                        roleCostsForOptions, periodValue, pph, paramHist, pdh,
                        idData, newParam, paramValueChanged,
                        paramValueStartTime, userFactors, steppedPrices);
            } else {
                periodValue = null;
            }

            oldParamValue = currentParamValue;
        }

        return result;
    }

    /**
     * @return list of parameter data, may be empty if no results were found.
     *         Never null.
     */
    List<Object[]> findParameterData(long priceModelKeyForSubscription,
            long startTime, long endTime) {
        Query query = dm
                .createNamedQuery("PricedParameterHistory.findParameterDataForPriceModelAndPeriod");
        query.setParameter("pmKey", Long.valueOf(priceModelKeyForSubscription));
        query.setParameter("startTime", new Date(startTime));
        query.setParameter("endTime", new Date(endTime));
        return ParameterizedTypes.list(query.getResultList(), Object[].class);
    }

    /**
     * Getting stepped prices for parameter.
     *
     * @param parameterKey
     *            Key of price model history object.
     * @param periodEndTime
     *            End of billing period.
     * @return List of stepped prices for needed price model till needed time
     *         period. May be empty, but is never null
     */
    List<SteppedPriceData> getSteppedPricesForParameter(long parameterKey,
            long periodEndTime) {
        final List<SteppedPriceData> result = new ArrayList<SteppedPriceData>();
        List<SteppedPriceHistory> steppedPrices = findSteppedPricesForParameter(
                parameterKey, periodEndTime);
        for (SteppedPriceHistory steppedPrice : steppedPrices) {
            result.add(new SteppedPriceData(steppedPrice));
        }
        return result;
    }

    /**
     *
     * @return list of stepped price history objects, may be empty but never
     *         null
     */
    List<SteppedPriceHistory> findSteppedPricesForParameter(long parameterKey,
            long periodEndTime) {
        Query query = dm
                .createNamedQuery("SteppedPriceHistory.getForParameterKeyAndEndDate");
        query.setParameter("prmtrObjKey", Long.valueOf(parameterKey));
        query.setParameter("modDate", new Timestamp(periodEndTime));
        return ParameterizedTypes.list(query.getResultList(),
                SteppedPriceHistory.class);
    }

    private XParameterPeriodValue handleParameterPeriodValue(long endTime,
            long currentEndTime, RolePricingData roleCostsForParameter,
            ParameterOptionRolePricingData roleCostsForOptions,
            XParameterPeriodValue periodValue, PricedParameterHistory pph,
            ParameterHistory paramHist, ParameterDefinitionHistory pdh,
            XParameterIdData idData, boolean newParam,
            boolean paramValueChanged, long paramValueStartTime,
            UserAssignmentFactors userFactors,
            List<SteppedPriceData> steppedPrices) throws BillingRunFailed {
        if (paramValueChanged || newParam) {
            periodValue = createPeriodValueData(endTime, currentEndTime,
                    roleCostsForParameter, roleCostsForOptions, pph, paramHist,
                    pdh, idData, userFactors, paramValueStartTime,
                    steppedPrices);
        } else {
            updatePeriodValue(periodValue, paramValueStartTime, userFactors);
        }
        return periodValue;
    }

    private void updatePeriodValue(XParameterPeriodValue periodValue,
            long paramValueStartTime, UserAssignmentFactors userFactors) {
        periodValue.setStartTime(paramValueStartTime);
        updateRoleFactors(periodValue.getRolePrices(), userFactors,
                periodValue.getKey());
        periodValue.setUserAssignmentFactor(userFactors.getBasicFactor());
    }

    private long setEndTimeForParameterValue(long endTime, long currentEndTime,
            XParameterPeriodValue lastPeriodValue, boolean newParam,
            boolean paramValueChanged, ParameterHistory paramHist) {
        // be careful when changing the evaluation order, has definitelly impact
        // on the billing results
        if (paramHist.getModtype() == ModificationType.DELETE) {
            return paramHist.getModdate().getTime();
        } else if (newParam) {
            if (paramHist.getValue() == null) {
                return paramHist.getModdate().getTime();
            }
            return endTime;
        } else if (lastPeriodValue == null) {
            return currentEndTime;
        } else if (paramValueChanged) {
            if (paramHist.getValue() == null) {
                return paramHist.getModdate().getTime();
            }
            return lastPeriodValue.getStartTime();
        }
        return currentEndTime;
    }

    /**
     * Factory method to create period value data. The newly created object is
     * automatically added to the XParameterIdData object.
     *
     * @return parameter period value data object, never null
     */
    private XParameterPeriodValue createPeriodValueData(long endTime,
            long currentEndTime, RolePricingData roleCostsForParameter,
            ParameterOptionRolePricingData roleCostsForOptions,
            PricedParameterHistory pph, ParameterHistory paramHist,
            ParameterDefinitionHistory pdh, XParameterIdData idData,
            UserAssignmentFactors userFactors, long paramValueStartTime,
            List<SteppedPriceData> steppedPrices) throws BillingRunFailed {
        // add period value data
        XParameterPeriodValue periodValue = null;
        if (pdh.getValueType() == ParameterValueType.ENUMERATION) {
            List<Object[]> options = findOptionsForParameter(endTime, pph,
                    paramHist);
            if (options.size() != 1) {
                throwBillingRunFailed(
                        String.format("Option '%s' not found!",
                                paramHist.getValue()),
                        LogMessageIdentifier.ERROR_OPTION_NOT_FOUND,
                        paramHist.getValue());
            }
            PricedOptionHistory poh = (PricedOptionHistory) options.get(0)[0];
            XParameterPeriodEnumType enumPeriodValue = new XParameterPeriodEnumType(
                    idData);
            enumPeriodValue.setPricePerSubscription(poh
                    .getPricePerSubscription());
            enumPeriodValue.setPricePerUser(poh.getPricePerUser());
            XParameterOption parameterOption = new XParameterOption(
                    enumPeriodValue);
            ParameterOptionHistory optionHistory = (ParameterOptionHistory) options
                    .get(0)[1];
            parameterOption.setId(optionHistory.getOptionId());
            parameterOption.setRolePrices(roleCostsForOptions
                    .getRolePricingDataForPricedParameterKey(Long.valueOf(pph
                            .getObjKey())));
            enumPeriodValue.setParameterOption(parameterOption);
            enumPeriodValue.setKey(Long.valueOf(poh.getObjKey()));
            updateRoleFactors(enumPeriodValue.getRolePrices(), userFactors,
                    enumPeriodValue.getKey());
            periodValue = enumPeriodValue;
        } else {
            XParameterPeriodPrimitiveType primitivePeriodValue = new XParameterPeriodPrimitiveType(
                    idData, roleCostsForParameter, steppedPrices);
            primitivePeriodValue.setValue(paramHist.getValue());
            primitivePeriodValue.setPricePerSubscription(pph
                    .getPricePerSubscription());
            primitivePeriodValue.setPricePerUser(pph.getPricePerUser());
            primitivePeriodValue.setKey(Long.valueOf(pph.getObjKey()));
            updateRoleFactors(primitivePeriodValue.getRolePrices(),
                    userFactors, primitivePeriodValue.getKey());
            periodValue = primitivePeriodValue;
        }
        periodValue.setStartTime(paramValueStartTime);
        periodValue.setEndTime(currentEndTime);
        periodValue.setUserAssignmentFactor(userFactors.getBasicFactor());
        return periodValue;
    }

    private void throwBillingRunFailed(final String message,
            final LogMessageIdentifier logMessageIdentifier,
            final String... params) throws BillingRunFailed {
        throw billingRunFailed(message, logMessageIdentifier, params);
    }

    private BillingRunFailed billingRunFailed(final String message,
            final LogMessageIdentifier logMessageIdentifier,
            final String... params) throws BillingRunFailed {
        BillingRunFailed brf = new BillingRunFailed(message);
        brf.fillInStackTrace();
        LOGGER.logError(Log4jLogger.SYSTEM_LOG, brf, logMessageIdentifier,
                params);
        return brf;
    }

    /**
     *
     * @return list of parameter options, may be empty but is never null
     */
    List<Object[]> findOptionsForParameter(long endTime,
            PricedParameterHistory pph, ParameterHistory paramHist) {
        Query optionQuery = dm
                .createNamedQuery("PricedOptionHistory.findOptionsForParameter");
        optionQuery.setParameter("endTimeForPeriod", new Date(endTime));
        optionQuery.setParameter("pricedparameterObjKey",
                Long.valueOf(pph.getObjKey()));
        optionQuery.setParameter("optionId", paramHist.getValue());
        return ParameterizedTypes.list(optionQuery.getResultList(),
                Object[].class);
    }

    /**
     * Sets the role cost factors.
     *
     * @param roleCosts
     *            The role cost data with factors uninitialized.
     * @param userFactors
     *            The factors for the roles.
     * @param refObjKey
     *            The key of the parameter or option.
     */
    private void updateRoleFactors(RolePricingData roleCosts,
            UserAssignmentFactors userFactors, Long refObjKey) {

        if (roleCosts == null || userFactors == null
                || userFactors.getRoleFactors().isEmpty()) {
            return;
        }

        Map<Long, RolePricingDetails> rolePrices = roleCosts
                .getRolePricesForContainerKey(refObjKey);
        Set<Long> roleDefKeys = rolePrices.keySet();
        for (Long roleDefKey : roleDefKeys) {
            RolePricingDetails rolePricingDetails = rolePrices.get(roleDefKey);
            Double factor = userFactors.getRoleFactors().get(roleDefKey);
            if (factor != null) {
                rolePricingDetails.setFactor(factor.doubleValue());
            }
        }
    }

    /**
     * @see BillingDataRetrievalServiceLocal#loadEventPricing(long, long)
     */
    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public Map<String, EventPricingData> loadEventPricing(long priceModelKey,
            long endTimeForPeriod) {
        // obtain all priced event history objects for the given price model
        // (identified by price model key).
        // Take only events with modification date only less or equal of end
        // period time
        Iterable<Object[]> pricedEvents = findEventsByPriceModelKeyBeforePeriodEn(
                priceModelKey, endTimeForPeriod);

        // create an event-id / EventPricingData map
        Map<String, EventPricingData> eventPrices = new HashMap<String, EventPricingData>();
        String eventIdToIgnore = null;
        for (Object[] entry : pricedEvents) {
            PricedEventHistory history = (PricedEventHistory) entry[0];
            PricedEventData historyData = history.getDataContainer();
            long eventKey = history.getEventObjKey();
            String eventId = (String) entry[1];

            // only consider the entry when the modification date of the priced
            // event is lower than the end date of the interval (if modified
            // later, it must not affect this period)
            // furthermore the modification type for that last entry must not be
            // ModificationType.DELETE. If it was, ignore the event in general
            if (eventIdToIgnore == null || !eventIdToIgnore.equals(eventId)) {
                if (history.getModdate().getTime() < endTimeForPeriod) {
                    if (history.getModtype() == ModificationType.DELETE) {
                        eventIdToIgnore = eventId;
                        continue;
                    }
                    if (!eventPrices.containsKey(eventId)) {
                        // get stepped prices for event
                        List<SteppedPriceData> eventSteppedPrices = loadSteppedPricesForEvent(
                                history.getObjKey(), endTimeForPeriod);

                        EventPricingData eventPricingData = new EventPricingData();
                        eventPricingData.setPrice(historyData.getEventPrice());
                        eventPricingData
                                .setEventSteppedPrice(eventSteppedPrices);
                        // Set the event key.
                        eventPricingData.setEventKey(eventKey);
                        eventPrices.put(eventId, eventPricingData);
                    }
                }
            }
        }
        return eventPrices;
    }

    /**
     * Obtain all priced event history objects for the given price model
     * (identified by price model key). Take only events with modification date
     * only less or equal of end period time
     */
    Iterable<Object[]> findEventsByPriceModelKeyBeforePeriodEn(
            long priceModelKey, long endTimeForPeriod) {
        Query query = dm
                .createNamedQuery("PricedEventHistory.findEventsByPriceModelKeyBeforePeriodEnd");
        query.setParameter("priceModelKey", Long.valueOf(priceModelKey));
        final Date endTimeForPeriodTimeStamp = new Date(endTimeForPeriod);
        query.setParameter("modDate", endTimeForPeriodTimeStamp);
        return ParameterizedTypes.iterable(query.getResultList(),
                Object[].class);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public List<EventCount> loadEventStatistics(long subscriptionKey,
            long startTime, long endTime) {
        List<Object[]> gatheredEvents = findEventStatistics(subscriptionKey,
                startTime, endTime);
        List<EventCount> result = new ArrayList<EventCount>();
        for (Object[] eventStat : gatheredEvents) {
            EventCount evtCount = new EventCount();
            evtCount.setEventIdentifier((String) eventStat[0]);
            evtCount.setNumberOfOccurrences(((Long) eventStat[1]).longValue());
            result.add(evtCount);
        }
        return result;
    }

    List<Object[]> findEventStatistics(long subscriptionKey, long startTime,
            long endTime) {
        Query queryForGatheredEvents = dm
                .createNamedQuery("GatheredEvent.getEventsForSubAndPeriod");
        queryForGatheredEvents.setParameter("startTime",
                Long.valueOf(startTime));
        queryForGatheredEvents.setParameter("endTime", Long.valueOf(endTime));
        queryForGatheredEvents.setParameter("subscriptionKey",
                Long.valueOf(subscriptionKey));
        return ParameterizedTypes.list(queryForGatheredEvents.getResultList(),
                Object[].class);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public Map<Long, RoleDefinitionHistory> loadRoleDefinitionsForPriceModel(
            long priceModelKey, long periodEndTime) {
        List<RoleDefinitionHistory> tempResult = findRoleDefinitionsForPriceModel(
                priceModelKey, periodEndTime);

        Map<Long, RoleDefinitionHistory> result = new HashMap<Long, RoleDefinitionHistory>();
        Set<Long> containedRoleKeys = new HashSet<Long>();

        for (RoleDefinitionHistory currentHist : tempResult) {
            if (containedRoleKeys.add(Long.valueOf(currentHist.getObjKey()))) {
                result.put(Long.valueOf(currentHist.getObjKey()), currentHist);
            }
        }

        return result;
    }

    List<RoleDefinitionHistory> findRoleDefinitionsForPriceModel(
            long priceModelKey, long periodEndTime) {
        Query query = dm
                .createNamedQuery("RoleDefinitionHistory.getRolesForPriceModelKey");
        query.setParameter("pmKey", Long.valueOf(priceModelKey));
        query.setParameter("modDate", new Date(periodEndTime));
        return ParameterizedTypes.list(query.getResultList(),
                RoleDefinitionHistory.class);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public Map<Long, RolePricingDetails> loadRoleRelatedCostsForPriceModel(
            long priceModelKey, long periodEndTime) {

        List<PricedProductRoleHistory> rolePrices = findRoleRelatedCostsForPriceModel(
                priceModelKey, periodEndTime);

        final Map<Long, RolePricingDetails> result = new HashMap<Long, RolePricingDetails>();
        for (PricedProductRoleHistory rolePrice : rolePrices) {
            if (!result.containsKey(Long.valueOf(rolePrice
                    .getRoleDefinitionObjKey()))) {
                RolePricingDetails returnObject = new RolePricingDetails();
                returnObject.setPricedProductRoleHistory(rolePrice);
                result.put(Long.valueOf(rolePrice.getRoleDefinitionObjKey()),
                        returnObject);
            }
        }
        return result;
    }

    List<PricedProductRoleHistory> findRoleRelatedCostsForPriceModel(
            long priceModelKey, long periodEndTime) {
        Query query = dm
                .createNamedQuery("PricedProductRoleHistory.getForPMKeyAndEndDate");
        query.setParameter("pmObjKey", Long.valueOf(priceModelKey));
        query.setParameter("modDate", new Date(periodEndTime));
        return ParameterizedTypes.list(query.getResultList(),
                PricedProductRoleHistory.class);
    }

    /**
     * @return role pricing data, never null
     */
    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public RolePricingData loadRoleRelatedCostsForParameters(
            long priceModelKey, long periodEndTime) {

        List<Object[]> pricedRoles = findRoleRelatedCostsForParameters(
                priceModelKey, periodEndTime);

        final RolePricingData result = new RolePricingData();
        for (Object[] entry : pricedRoles) {
            PricedProductRoleHistory pricedRole = (PricedProductRoleHistory) entry[0];
            String roleId = (String) entry[1];
            Long currentParamKey = pricedRole.getPricedParameterObjKey();
            // if nothing is stored, initialize with empty list
            if (!result.getContainerKeys().contains(currentParamKey)) {
                result.addRolePricesForContainerKey(currentParamKey,
                        new HashMap<Long, RolePricingDetails>());
            }
            Map<Long, RolePricingDetails> currentRolePrices = result
                    .getRolePricesForContainerKey(currentParamKey);
            if (!currentRolePrices.keySet().contains(
                    Long.valueOf(pricedRole.getRoleDefinitionObjKey()))) {
                RolePricingDetails returnObject = new RolePricingDetails(result);
                returnObject.setPricedProductRoleHistory(pricedRole);
                returnObject.setRoleId(roleId);
                currentRolePrices.put(
                        Long.valueOf(pricedRole.getRoleDefinitionObjKey()),
                        returnObject);
            }
        }
        return result;
    }

    List<Object[]> findRoleRelatedCostsForParameters(long priceModelKey,
            long periodEndTime) {
        Query query = dm
                .createNamedQuery("PricedProductRoleHistory.getForParameterAndEndDate");
        query.setParameter("pmKey", Long.valueOf(priceModelKey));
        query.setParameter("endDate", new Date(periodEndTime));
        return ParameterizedTypes.list(query.getResultList(), Object[].class);
    }

    /**
     * @return parameter option role price data, never null
     */
    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public ParameterOptionRolePricingData loadRoleRelatedCostsForOptions(
            long priceModelKey, long periodEndTime) {

        List<Object[]> pricedRoles = findRoleRelatedCostsForOptions(
                priceModelKey, periodEndTime);

        final ParameterOptionRolePricingData result = new ParameterOptionRolePricingData();
        for (Object[] entry : pricedRoles) {
            Long currentPricedParamKey = (Long) entry[0];
            PricedProductRoleHistory rolePriceDetails = (PricedProductRoleHistory) entry[1];
            String roleId = (String) entry[2];
            // if nothing is stored, initialize with empty list
            if (!result.getPricedParameterKeys()
                    .contains(currentPricedParamKey)) {
                result.addRolePricingDataForPricedParameterKey(
                        currentPricedParamKey, new RolePricingData());
            }

            RolePricingData currentParamOptionPrices = result
                    .getRolePricingDataForPricedParameterKey(currentPricedParamKey);
            Long currentOptionKey = rolePriceDetails.getPricedOptionObjKey();

            // the option related role prices for the parameter must be
            // enhanced, so check if an entry for the current option is already
            // contained
            if (!currentParamOptionPrices.getContainerKeys().contains(
                    currentOptionKey)) {
                // if not, set an empty map
                currentParamOptionPrices.addRolePricesForContainerKey(
                        currentOptionKey,
                        new HashMap<Long, RolePricingDetails>());
            }
            // and add the current option entry
            Map<Long, RolePricingDetails> currentRolePrices = currentParamOptionPrices
                    .getRolePricesForContainerKey(currentOptionKey);
            if (!currentRolePrices.keySet().contains(
                    Long.valueOf(rolePriceDetails.getRoleDefinitionObjKey()))) {
                RolePricingDetails returnObject = new RolePricingDetails(
                        result.getRolePricingDataForPricedParameterKey(currentPricedParamKey));
                returnObject.setPricedProductRoleHistory(rolePriceDetails);
                returnObject.setRoleId(roleId);
                currentRolePrices.put(Long.valueOf(rolePriceDetails
                        .getRoleDefinitionObjKey()), returnObject);
            }
        }

        return result;
    }

    List<Object[]> findRoleRelatedCostsForOptions(long priceModelKey,
            long periodEndTime) {
        Query query = dm
                .createNamedQuery("PricedProductRoleHistory.getForParameterOptionAndEndDate");
        query.setParameter("pmObjKey", Long.valueOf(priceModelKey));
        query.setParameter("modDate", new Date(periodEndTime));
        return ParameterizedTypes.list(query.getResultList(), Object[].class);
    }

    /**
     * Load the list of UsageLicense objects that belong to the subscription and
     * thus the product. The list will be ordered by userobjkey ASC, objVersion
     * DESC.
     *
     * @return list of usage licenses, may be empty but never null.
     */
    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public List<UsageLicenseHistory> loadUsageLicenses(long subscriptionKey,
            long startTimeForPeriod, long endTimeForPeriod) {

        Query query = dm
                .createNamedQuery("UsageLicenseHistory.getForSubKey_VersionDESC");
        Date startDate = new Date(startTimeForPeriod);
        query.setParameter("startTimeAsDate", startDate);
        query.setParameter("subscriptionKey", Long.valueOf(subscriptionKey));
        Date endDate = new Date(endTimeForPeriod);
        query.setParameter("endTimeAsDate", endDate);

        final List<UsageLicenseHistory> usageLicenseHistoryElements = ParameterizedTypes
                .list(query.getResultList(), UsageLicenseHistory.class);

        setUserIdsForUsageLicenses(usageLicenseHistoryElements);

        return usageLicenseHistoryElements;
    }

    private void setUserIdsForUsageLicenses(
            List<UsageLicenseHistory> usageLicenses) {
        final List<UsageLicenseHistory> usageLicenseHistoryElements = new ArrayList<UsageLicenseHistory>();
        for (UsageLicenseHistory ulHist : usageLicenses) {

            String userId = String.valueOf(ulHist.getUserObjKey());
            try {
                userId = dm.getReference(PlatformUser.class,
                        ulHist.getUserObjKey()).getUserId();
            } catch (ObjectNotFoundException e) {
                // ignore this - maybe user has been deleted in the
                // meantime so we just use the initially used user key
            }
            ulHist.getDataContainer().setApplicationUserId(userId);
            usageLicenseHistoryElements.add(ulHist);
        }
    }

    /**
     * @see BillingDataRetrievalServiceLocal#loadSteppedPricesForPriceModel(long,
     *      long)
     */
    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public List<SteppedPriceData> loadSteppedPricesForPriceModel(
            long priceModelKey, long periodEndTime) {
        List<SteppedPriceHistory> steppedPrices = findSteppedPricesForPriceModel(
                priceModelKey, periodEndTime);

        List<SteppedPriceData> result = new ArrayList<SteppedPriceData>();
        for (SteppedPriceHistory steppedPrice : steppedPrices) {
            result.add(new SteppedPriceData(steppedPrice));
        }
        return result;
    }

    List<SteppedPriceHistory> findSteppedPricesForPriceModel(
            long priceModelKey, long periodEndTime) {
        Query query = dm
                .createNamedQuery("SteppedPriceHistory.getForPMKeyAndEndDate");
        query.setParameter("pmObjKey", Long.valueOf(priceModelKey));
        query.setParameter("modDate", new Date(periodEndTime));
        return ParameterizedTypes.list(query.getResultList(),
                SteppedPriceHistory.class);
    }

    /**
     * @see BillingDataRetrievalServiceLocal#loadSteppedPricesForEvent(long,
     *      long)
     */
    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public List<SteppedPriceData> loadSteppedPricesForEvent(long eventKey,
            long periodEndTime) {
        List<SteppedPriceHistory> steppedPrices = findSteppedPricesForEvent(
                eventKey, periodEndTime);

        List<SteppedPriceData> result = new ArrayList<SteppedPriceData>();
        for (SteppedPriceHistory steppedPrice : steppedPrices) {
            result.add(new SteppedPriceData(steppedPrice));
        }
        return result;
    }

    List<SteppedPriceHistory> findSteppedPricesForEvent(long eventKey,
            long periodEndTime) {
        Query query = dm
                .createNamedQuery("SteppedPriceHistory.getForEventKeyAndEndDate");
        query.setParameter("evntObjKey", Long.valueOf(eventKey));
        query.setParameter("modDate", new Date(periodEndTime));
        return ParameterizedTypes.list(query.getResultList(),
                SteppedPriceHistory.class);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public List<UdaBillingData> loadUdasForCustomer(long customerKey,
            long chargingOrkKey) {

        List<Object[]> udas = findUdaBillingDataForOrg(customerKey,
                chargingOrkKey);
        return getUdaBillingDataFromResultList(udas);
    }

    List<Object[]> findUdaBillingDataForOrg(long customerKey,
            long chargingOrkKey) {
        Query query = dm.createNamedQuery("UdaHistory.findForOrg");
        query.setParameter("ignoredModType", ModificationType.DELETE);
        query.setParameter("targetObjectKey", Long.valueOf(customerKey));
        query.setParameter("udaTargetType", UdaTargetType.CUSTOMER);
        query.setParameter("organizationObjKey", Long.valueOf(chargingOrkKey));
        return ParameterizedTypes.list(query.getResultList(), Object[].class);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public List<UdaBillingData> loadUdasForSubscription(long subscriptionKey,
            long chargingOrgKey) {

        List<Object[]> udas = findUdaBillingDataForSubscription(
                subscriptionKey, chargingOrgKey);
        return getUdaBillingDataFromResultList(udas);
    }

    List<Object[]> findUdaBillingDataForSubscription(long subscriptionKey,
            long chargingOrgKey) {
        Query query = dm.createNamedQuery("UdaHistory.findForSub");
        query.setParameter("ignoredModType", ModificationType.DELETE);
        query.setParameter("targetObjectKey", Long.valueOf(subscriptionKey));
        query.setParameter("modTypeDeleted", ModificationType.DELETE);
        query.setParameter("udaTargetType", UdaTargetType.CUSTOMER_SUBSCRIPTION);
        query.setParameter("organizationObjKey", Long.valueOf(chargingOrgKey));
        return ParameterizedTypes.list(query.getResultList(), Object[].class);
    }

    /**
     * Reads the results and return a list of UdaBillingData objects
     * representing its content.
     *
     * @param query
     *            The query object. Must not be <code>null</code>.
     */
    private List<UdaBillingData> getUdaBillingDataFromResultList(
            List<Object[]> udas) {
        List<UdaBillingData> result = new ArrayList<UdaBillingData>();
        for (Object[] objects : udas) {
            UdaHistory udh = (UdaHistory) objects[0];
            String udaId = (String) objects[1];
            result.add(new UdaBillingData(udh.getDataContainer().getUdaValue(),
                    udaId));
        }
        return result;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public VatRateDetails loadVATForCustomer(long customerKey, long endDate,
            long supplierKey) {
        VatRateDetails vatDetails = new VatRateDetails();

        List<VatRateHistory> vatRates = findVATForCustomer(customerKey,
                endDate, supplierKey);
        vatDetails = new VatRateDetails();

        for (VatRateHistory entry : vatRates) {
            if (entry.getModtype() == ModificationType.DELETE) {
                continue;
            }
            if (entry.getTargetCountryObjKey() == null
                    && entry.getTargetOrganizationObjKey() == null) {
                vatDetails
                        .setDefaultVatRate(entry.getDataContainer().getRate());
            } else if (entry.getTargetOrganizationObjKey() == null) {
                vatDetails
                        .setCountryVatRate(entry.getDataContainer().getRate());
            } else {
                vatDetails.setCustomerVatRate(entry.getDataContainer()
                        .getRate());
            }
        }

        return vatDetails;
    }

    List<VatRateHistory> findVATForCustomer(long customerKey, long endDate,
            long supplierKey) {
        Query query = dm
                .createNamedQuery("VatRateHistory.findForCustomerAndSupplier");
        query.setParameter("customerKey", Long.valueOf(customerKey));
        query.setParameter("supplierKey", Long.valueOf(supplierKey));
        query.setParameter("endDate", new Date(endDate));
        return ParameterizedTypes.list(query.getResultList(),
                VatRateHistory.class);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public List<SubscriptionHistory> loadSubscriptionsForCustomer(
            long organizationKey, long startDate, long endDate, int cutOffDay) {
        Query query = dm
                .createNamedQuery("SubscriptionHistory.getSubscriptionsForOrganization_VersionDesc");
        query.setParameter("organizationKey", Long.valueOf(organizationKey));
        query.setParameter("startDate", new Date(startDate));
        query.setParameter("endDate", new Date(endDate));
        query.setParameter("cutOffDay", Integer.valueOf(cutOffDay));
        query.setParameter("external", true);
        @SuppressWarnings("unchecked")
        List<SubscriptionHistory> result = query.getResultList();
        return result;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public List<SubscriptionHistory> loadSubscriptionsForCustomer(
            long organizationKey, List<Long> unitKeys, long startDate,
            long endDate, int cutOffDay) {
        if (unitKeys == null || unitKeys.isEmpty()) {
            return new ArrayList<SubscriptionHistory>();
        }
        Query query = dm
                .createNamedQuery("SubscriptionHistory.getSubscriptionsForOrganizationAndUnits_VersionDesc");
        query.setParameter("organizationKey", Long.valueOf(organizationKey));
        query.setParameter("units", unitKeys);
        query.setParameter("startDate", new Date(startDate));
        query.setParameter("endDate", new Date(endDate));
        query.setParameter("cutOffDay", Integer.valueOf(cutOffDay));
        query.setParameter("external", true);
        @SuppressWarnings("unchecked")
        List<SubscriptionHistory> result = query.getResultList();
        return result;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public List<SubscriptionHistory> loadSubscriptionHistoriesForBillingPeriod(
            List<Long> subscriptionKeys, long startDate, long endDate) {
        Query query = dm
                .createNamedQuery("SubscriptionHistory.getHistoriesForSubscriptionsAndBillingPeriod");
        query.setParameter("startDate", new Date(startDate));
        query.setParameter("endDate", new Date(endDate));
        query.setParameter("subscriptionKeys", subscriptionKeys);
        query.setParameter("external", true);
        return ParameterizedTypes.list(query.getResultList(),
                SubscriptionHistory.class);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public SupportedCurrency loadCurrency(long subscriptionKey, long endDate) {
        Query query = dm.createNamedQuery("SubscriptionHistory.findCurrency");
        query.setParameter("subscriptionKey", Long.valueOf(subscriptionKey));
        query.setParameter("endDate", new Date(endDate));
        @SuppressWarnings("unchecked")
        List<SupportedCurrency> result = query.getResultList();
        if (result.isEmpty()) {
            return null;
        }
        return result.get(0);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public BillingContactHistory loadBillingContact(long subscriptionKey) {
        Query query = dm
                .createNamedQuery("SubscriptionHistory.findBillingContact");
        query.setParameter("subscriptionKey", Long.valueOf(subscriptionKey));
        @SuppressWarnings("unchecked")
        List<BillingContactHistory> result = query.getResultList();
        if (result.isEmpty()) {
            return null;
        }
        return result.get(0);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public long loadChargingOrgKeyForSubscription(long subscriptionKey) {
        List<OrganizationRoleType> roles = loadVendorRolesForSubscription(subscriptionKey);
        long result;
        if (roles.contains(OrganizationRoleType.BROKER)) {
            result = loadSupplierKeyForSubscription(subscriptionKey);
        } else {
            result = loadVendorKeyForSubscription(subscriptionKey);
        }
        return result;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public long loadVendorKeyForSubscription(long subscriptionKey) {
        Query query = dm.createNamedQuery("SubscriptionHistory.getVendorKey");
        query.setParameter("subscriptionKey", Long.valueOf(subscriptionKey));
        Long result = (Long) query.getSingleResult();
        return result.longValue();
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public long loadSupplierKeyForSubscription(long subscriptionKey) {
        Query query = dm.createNamedQuery("SubscriptionHistory.getSupplierKey");
        query.setParameter("subscriptionKey", Long.valueOf(subscriptionKey));
        query.setParameter("productType", ServiceType.PARTNER_SUBSCRIPTION);
        long result;
        try {
            result = ((Long) query.getSingleResult()).longValue();
        } catch (NoResultException e) {
            // subscription product is not of type PARTNER_TEMPLATE
            result = loadVendorKeyForSubscription(subscriptionKey);
        }
        return result;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public List<OrganizationRoleType> loadVendorRolesForSubscription(
            long subscriptionKey) {
        Query query = dm
                .createNamedQuery("SubscriptionHistory.getVendorRoleNames");
        query.setParameter("subscriptionKey", Long.valueOf(subscriptionKey));
        @SuppressWarnings({ "unchecked" })
        List<OrganizationRoleType> result = query.getResultList();
        return result;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public String loadPaymentTypeIdForSubscription(long subscriptionKey) {
        Query query = dm
                .createNamedQuery("SubscriptionHistory.getPaymentTypeId");
        query.setParameter("subscriptionKey", Long.valueOf(subscriptionKey));
        String result;
        try {
            result = (String) query.getSingleResult();
        } catch (NoResultException e) {
            result = "";
        }
        return result;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public SupportedCurrency loadSupportedCurrency(String currencyISOcode)
            throws ObjectNotFoundException {
        SupportedCurrency currency = new SupportedCurrency(currencyISOcode);
        return (SupportedCurrency) dm.getReferenceByBusinessKey(currency);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void updateEvent(long startTimeForPeriod, long endTimeForPeriod,
            long subscriptionKey, BillingResult result) {

        final Query eventUpdateQuery = dm
                .createNamedQuery("GatheredEvent.setResultReferenceForEventsForSubAndPeriod");
        eventUpdateQuery.setParameter("startTime",
                Long.valueOf(startTimeForPeriod));
        eventUpdateQuery
                .setParameter("endTime", Long.valueOf(endTimeForPeriod));
        eventUpdateQuery.setParameter("subscriptionKey",
                Long.valueOf(subscriptionKey));
        eventUpdateQuery.setParameter("billingResult", result);
        eventUpdateQuery.executeUpdate();
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    @SuppressWarnings("unchecked")
    public List<PriceModelHistory> loadPriceModelHistories(
            long priceModelKeyForSubscription, long endTimeForPeriod) {

        Query query = dm
                .createNamedQuery("PriceModelHistory.findByKeyDescVersion");
        query.setParameter("objKey", Long.valueOf(priceModelKeyForSubscription));
        query.setParameter("modDate", new Date(endTimeForPeriod));
        return query.getResultList();
    }

    /**
     * Using the product key, determine the product related history entries. As
     * the price model must not change for a product (a new product is created
     * in this case), use the price model information related to the most
     * current history entry for the product.
     */
    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public long loadPriceModelKeyForSubscriptionHistory(
            SubscriptionHistory subscriptionHistory) throws BillingRunFailed {

        Product productTemplate = new Product();
        productTemplate.setKey(subscriptionHistory.getProductObjKey());
        List<DomainHistoryObject<?>> productHistories = dm
                .findHistory(productTemplate);
        int size = productHistories.size();
        if (size == 0) {
            throwBillingRunFailed(
                    "History data is missing for product with key '"
                            + subscriptionHistory.getProductObjKey() + "'.",
                    LogMessageIdentifier.ERROR_MISSING_HISTORYDATA_FOR_PRODUCT,
                    String.valueOf(subscriptionHistory.getProductObjKey()));

        }
        ProductHistory productHistory = (ProductHistory) productHistories
                .get(size - 1);
        return productHistory.getPriceModelObjKey().longValue();
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public List<PriceModelHistory> loadPricemodelHistoriesForSubscriptionHistory(
            long subscriptionKey, long endPeriod) {

        Query query = dm
                .createNamedQuery("PriceModelHistory.findBySubscriptionHistory");
        query.setParameter("subcriptionObjKey", Long.valueOf(subscriptionKey));
        query.setParameter("modDate", new Date(endPeriod));
        return ParameterizedTypes.list(query.getResultList(),
                PriceModelHistory.class);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public PriceModelHistory loadLatestPriceModelHistory(
            SubscriptionHistory history) {

        Query query = dm
                .createNamedQuery("PriceModelHistory.findLatestBySubscriptionHistory");
        query.setParameter("prdObjKey",
                Long.valueOf(history.getProductObjKey()));
        query.setParameter("modDate", history.getModdate());
        return (PriceModelHistory) query.getSingleResult();
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public Date loadPriceModelStartDate(long priceModelKeyForSubscription) {
        TypedQuery<PriceModelHistory> query = dm.createNamedQuery(
                "PriceModelHistory.findByObjectAndProvisioningCompleted",
                PriceModelHistory.class);
        query.setParameter("objKey", Long.valueOf(priceModelKeyForSubscription));
        query.setParameter("provisioningCompleted", Boolean.TRUE);

        List<PriceModelHistory> resultList = query.getResultList();
        if (resultList.isEmpty()) {
            throw new BillingRunFailed(
                    "History data is missing for price model with key "
                            + priceModelKeyForSubscription);
        }

        return resultList.get(0).getModdate();
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void persistBillingResult(BillingResult result)
            throws NonUniqueBusinessKeyException {
        dm.persist(result);
        dm.flush();
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void removeBillingResult(BillingResult result) {
        dm.remove(result);
    }

    @Override
    public Schema loadSchemaFiles() {
        try (InputStream brStream = ResourceLoader.getResourceAsStream(
                BillingDataRetrievalServiceBean.class, "BillingResult.xsd");
                InputStream localeStream = ResourceLoader.getResourceAsStream(
                        BillingDataRetrievalServiceBean.class, "Locale.xsd")) {

            URL billingResultUri = ResourceLoader.getResource(
                    BillingDataRetrievalServiceBean.class, "BillingResult.xsd");
            URL localeUri = ResourceLoader.getResource(
                    BillingDataRetrievalServiceBean.class, "Locale.xsd");
            SchemaFactory schemaFactory = SchemaFactory
                    .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            StreamSource[] sourceDocuments = new StreamSource[] {
                    new StreamSource(localeStream, localeUri.getPath()),
                    new StreamSource(brStream, billingResultUri.getPath()) };
            return schemaFactory.newSchema(sourceDocuments);
        } catch (SAXException | IOException e) {
            throw new BillingRunFailed("Schema files couldn't be loaded", e);
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public PriceModelHistory loadOldestPriceModelHistory(long priceModelKey,
            long modDate) {

        List<PriceModelHistory> resultList = loadPriceModelHistories(
                priceModelKey, modDate);
        if (resultList.isEmpty()) {
            throw new BillingRunFailed(
                    "History data is missing for price model with key "
                            + priceModelKey + " before " + new Date(modDate));
        }
        return resultList.get(0);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public SubscriptionHistory loadPreviousSubscriptionHistoryForPriceModel(
            final long priceModelKey, final long timeMillis) {
        TypedQuery<SubscriptionHistory> query = dm.createNamedQuery(
                "SubscriptionHistory.findPreviousForPriceModel",
                SubscriptionHistory.class);
        query.setParameter("priceModelKey", Long.valueOf(priceModelKey));
        query.setParameter("modDate", new Date(timeMillis));

        List<SubscriptionHistory> resultList = query.getResultList();
        if (resultList.size() > 0) {
            return resultList.get(0);
        } else {
            return null;
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public SubscriptionHistory loadNextActiveSubscriptionHistoryForPriceModel(
            final long priceModelKey, final long timeMillis) {
        TypedQuery<SubscriptionHistory> query = dm.createNamedQuery(
                "SubscriptionHistory.findNextForPriceModelAndState",
                SubscriptionHistory.class);
        query.setParameter("priceModelKey", Long.valueOf(priceModelKey));
        query.setParameter("modDate", new Date(timeMillis));
        query.setParameter("subscriptionStates", EnumSet.of(
                SubscriptionStatus.ACTIVE, SubscriptionStatus.PENDING_UPD));

        List<SubscriptionHistory> resultList = query.getResultList();
        if (resultList.size() > 0) {
            return resultList.get(0);
        } else {
            return null;
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public ProductHistory loadProductTemplateHistoryForSubscriptionHistory(
            SubscriptionHistory subscriptionHistory, long endDate)
            throws BillingRunFailed {

        ProductHistory productHistory = findProductHistory(
                subscriptionHistory.getProductObjKey(), endDate);
        Long templateKey = (productHistory != null) ? productHistory
                .getTemplateObjKey() : null;
        while (templateKey != null) {
            productHistory = findProductHistory(templateKey.longValue(),
                    endDate);
            templateKey = (productHistory != null) ? productHistory
                    .getTemplateObjKey() : null;
        }

        if (productHistory != null) {
            return productHistory;
        } else {
            throw billingRunFailed(
                    "History of product template not found for subscription history with key '"
                            + subscriptionHistory.getKey() + "'.",
                    LogMessageIdentifier.ERROR_HISTORYDATA_OF_PRODUCT_TEMPLATE_NOT_FOUND,
                    String.valueOf(subscriptionHistory.getKey()));
        }
    }

    private ProductHistory findProductHistory(long productKey, long endDate) {

        Query query = dm
                .createNamedQuery("ProductHistory.findByObjectDateAndModTypeDesc");
        query.setParameter("objKey", Long.valueOf(productKey));
        query.setParameter("endDate", new Date(endDate));
        query.setParameter("modTypes", EnumSet.of(ModificationType.DELETE));
        query.setMaxResults(1);
        List<?> qryresult = query.getResultList();
        if (qryresult.isEmpty()) {
            return null;
        } else {
            return (ProductHistory) qryresult.get(0);
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public List<BillingSubscriptionData> getSubscriptionsForBilling(
            long effectiveBillingEndDate, long cutoffBillingEndDate,
            long cutoffDeactivationDate) {

        final String queryString = "SELECT s.objkey, s.activationdate, s.cutoffday, bs.endoflastbilledPeriod "
                + "FROM subscriptionhistory s LEFT JOIN billingsubscriptionstatus bs ON (s.objkey=bs.subscriptionkey) "
                + "WHERE (s.objversion = (SELECT MAX(objversion) FROM subscriptionhistory sh WHERE sh.objkey=s.objkey)) "
                + "AND (s.activationdate IS NOT NULL) AND (s.activationdate <= :effectiveBillingEndDate) AND (s.external<> :external) "
                + "AND ((bs IS NULL) OR ((bs.endOfLastBilledPeriod <= :cutoffBillingEndDate) "
                + "AND ((s.deactivationdate IS NULL) OR (s.deactivationdate >= :cutoffDeactivationDate)))) "
                + "ORDER by s.tkey ASC";

        Query query = dm.createNativeQuery(queryString);
        query.setParameter("effectiveBillingEndDate",
                Long.valueOf(effectiveBillingEndDate));
        query.setParameter("cutoffBillingEndDate",
                Long.valueOf(cutoffBillingEndDate));
        query.setParameter("cutoffDeactivationDate",
                Long.valueOf(cutoffDeactivationDate));
        query.setParameter("external", true);

        List<Object[]> result = ParameterizedTypes.list(query.getResultList(),
                Object[].class);
        List<BillingSubscriptionData> billingSubData = new ArrayList<BillingSubscriptionData>();
        for (Object[] resultElement : result) {
            BillingSubscriptionData subData = new BillingSubscriptionData();
            subData.setSubscriptionKey(Long.parseLong(resultElement[0]
                    .toString()));
            subData.setActivationDate(Long.parseLong(resultElement[1]
                    .toString()));
            subData.setCutOffDay(Integer.parseInt(resultElement[2].toString()));
            if (resultElement[3] != null) {
                subData.setEndOfLastBilledPeriod(Long.valueOf(resultElement[3]
                        .toString()));
            }
            billingSubData.add(subData);
        }

        return billingSubData;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void updateBillingSubscriptionStatus(long subscriptionKey,
            long endOfLastBilledPeriod) throws NonUniqueBusinessKeyException {

        BillingSubscriptionStatus billingSubStatus = new BillingSubscriptionStatus();
        billingSubStatus.setSubscriptionKey(subscriptionKey);
        billingSubStatus = (BillingSubscriptionStatus) dm
                .find(billingSubStatus);

        if (billingSubStatus != null) {
            billingSubStatus.setEndOfLastBilledPeriod(Math.max(
                    billingSubStatus.getEndOfLastBilledPeriod(),
                    endOfLastBilledPeriod));
        } else {
            billingSubStatus = new BillingSubscriptionStatus();
            billingSubStatus.setSubscriptionKey(subscriptionKey);
            billingSubStatus.setEndOfLastBilledPeriod(endOfLastBilledPeriod);
            dm.persist(billingSubStatus);
            dm.flush();
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public UserGroupHistory getLastValidGroupHistory(long groupKey,
            long endOfBillingPeriod) {
        Query query = dm
                .createNamedQuery("UserGroupHistory.findLastValidForEndPeriod");
        query.setParameter("objKey", Long.valueOf(groupKey));
        query.setParameter("endDate", new Date(endOfBillingPeriod));
        query.setMaxResults(1);
        List<?> qryresult = query.getResultList();
        if (qryresult.size() == 0) {
            return null;
        } else {
            return (UserGroupHistory) qryresult.get(0);
        }
    }
}
