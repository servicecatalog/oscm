/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 10.05.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.dao;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.ejb.Local;
import javax.xml.validation.Schema;

import org.oscm.billingservice.business.calculation.revenue.model.PriceModelInput;
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
import org.oscm.billingservice.service.model.BillingInput;
import org.oscm.domobjects.BillingContactHistory;
import org.oscm.domobjects.BillingResult;
import org.oscm.domobjects.PriceModelHistory;
import org.oscm.domobjects.ProductHistory;
import org.oscm.domobjects.RoleDefinitionHistory;
import org.oscm.domobjects.SubscriptionHistory;
import org.oscm.domobjects.SupportedCurrency;
import org.oscm.domobjects.UsageLicenseHistory;
import org.oscm.domobjects.UserGroupHistory;
import org.oscm.types.exceptions.BillingRunFailed;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;

/**
 * Internal service to retrieve data required by the billing service.
 *
 * @author Mike J&auml;ger
 *
 */
@Local
public interface BillingDataRetrievalServiceLocal {

    /**
     * Determines the address data of the organization by accessing the history
     * data.
     *
     * @param organizationKey
     *            The technical key of the organization.
     * @param subscriptionKey
     *            the technical key of the subscription to get billing contact
     *            and payment type from
     * @return The organzation's billing address. <code>null</code> in case the
     *         organization is not found.
     */
    public OrganizationAddressData loadOrganizationBillingDataFromHistory(
            long organizationKey, long subscriptionKey);

    /**
     * Determines the list of UsageLicense objects that belong to the
     * subscription (and thus product). The list will be ordered by userobjkey
     * ASC, objVersion DESC.
     *
     * @param subscriptionKey
     *            the technical key of the subscription to get usage license
     *            from
     * @param startTimeForPeriod
     *            The start time of the period.
     * @param endTimeForPeriod
     *            The end time of the period.
     * @return The usage license data of the subscription for billing
     */
    public List<UsageLicenseHistory> loadUsageLicenses(long subscriptionKey,
            long startTimeForPeriod, long endTimeForPeriod);

    /**
     * Determines the parameter cost relevant information for the given period.
     */
    public XParameterData loadParameterData(final BillingInput billingInput,
            final PriceModelInput priceModelInput);

    /**
     * Getting discount value in percent for organization in period.
     *
     * @param organizationKey
     *            Organization key.
     * @param billingStart
     *            Start of billing period.
     * @param billingEnd
     *            End of billing period.
     * @param supplierKey
     *            the key of the supplier that defines the discount
     * @return Percents of discount.
     */
    public BigDecimal loadDiscountValue(long organizationKey,
            long billingStart, long billingEnd, long supplierKey);

    /**
     * Determines the priced events for the given price model that were valid
     * prior to the given end date. Events that had been deleted prior to the
     * end date will be ignored.
     *
     * @param priceModelKey
     *            The technical key of the price model to check.
     * @param endTimeForPeriod
     *            The end time of the billing period to be considered.
     * @return A map containing the event identifier as key and the event
     *         pricing data: price as value and stepped price list.
     */
    public Map<String, EventPricingData> loadEventPricing(long priceModelKey,
            long endTimeForPeriod);

    /**
     * Reads the gathered events for the given period for the given subscription
     * and provides the information on how often they occurred.
     *
     * @param subscriptionKey
     *            The key of the subscription the events will be collected for.
     * @param startTime
     *            The start time of the billing relevant period.
     * @param endTime
     *            The end time of the billing relevant period.
     * @return A list of elements giving the event identifier and the number of
     *         the corresponding event occurrences.
     */
    public List<EventCount> loadEventStatistics(long subscriptionKey,
            long startTime, long endTime);

    /**
     * Determines the role definitions that correspond to the specified price
     * model key.
     *
     * @param priceModelKey
     *            The technical key of the price model.
     * @param periodEndTime
     *            The period end time, before which the roles had to exist.
     * @return Map of role key and definitions that existed for the price model
     *         prior to the period end time.
     */
    public Map<Long, RoleDefinitionHistory> loadRoleDefinitionsForPriceModel(
            long priceModelKey, long periodEndTime);

    /**
     * Determines the role related prices for a price model identified by its
     * key attribute that were valid at the specified end of the period.
     *
     * @param priceModelKey
     *            The technical key of the price model to retrieve the role
     *            prices for.
     * @param periodEndTime
     *            The time at which the prices were valid.
     * @return A map containing the technical key of the role definition as key
     *         and the priced role data container is value. Never null.
     */
    public Map<Long, RolePricingDetails> loadRoleRelatedCostsForPriceModel(
            long priceModelKey, long periodEndTime);

    /**
     * Determines the role related prices for the parameters of a specific price
     * model identified by its key attribute that were valid at the specified
     * end of the period.
     *
     * @param priceModelKey
     *            The technical key of the price model to retrieve the parameter
     *            related role prices for.
     * @param periodEndTime
     *            The time at which the prices were valid.
     * @return The role pricing information for all priced parameters related to
     *         this price model.
     */
    public RolePricingData loadRoleRelatedCostsForParameters(
            long priceModelKey, long periodEndTime);

    /**
     * Determines the role related prices for the options of a parameters of a
     * specific price model identified by its key attribute that were valid at
     * the specified end of the period.
     *
     * @param priceModelKey
     *            The technical key of the price model to retrieve the parameter
     *            related role prices for.
     * @param periodEndTime
     *            The time at which the prices were valid.
     * @return The role pricing information for all options of priced parameters
     *         related to this price model.
     */
    public ParameterOptionRolePricingData loadRoleRelatedCostsForOptions(
            long priceModelKey, long periodEndTime);

    /**
     * Getting stepped prices for price model.
     *
     * @param priceModelKey
     *            Key of price model history object.
     * @param periodEndTime
     *            End of billing period.
     * @return List of stepped prices for needed price model till needed time
     *         period.
     */
    public List<SteppedPriceData> loadSteppedPricesForPriceModel(
            long priceModelKey, long periodEndTime);

    /**
     * Getting stepped prices for event.
     *
     * @param eventKey
     *            Key of price model history object.
     * @param periodEndTime
     *            End of billing period.
     * @return List of stepped prices for needed price model till needed time
     *         period.
     */
    public List<SteppedPriceData> loadSteppedPricesForEvent(long eventKey,
            long periodEndTime);

    /**
     * Retrieves the UDAs for the specified customer, that are currently stored.
     *
     * @param customerKey
     *            The key of the customer to retrieve the UDAs for.
     * @param chargingOrgKey
     *            The key of the organization in charge - either supplier or
     *            reseller
     *
     * @return The UDAs for the customer.
     */
    public List<UdaBillingData> loadUdasForCustomer(long customerKey,
            long chargingOrgKey);

    /**
     * Retrieves the UDAs for the specified subscription, that are currently
     * stored.
     *
     * @param subscriptionKey
     *            The key of the subscription to retrieve the UDAs for.
     * @param chargingOrkKey
     *            The key of the organization in charge - either supplier or
     *            reseller
     *
     * @return The UDAs for the customer.
     */
    public List<UdaBillingData> loadUdasForSubscription(long subscriptionKey,
            long chargingOrkKey);

    /**
     * Determines the VAT rate detail information for the provided customer.
     * Only the last valid entries prior to the end date are considered.
     *
     * @param customerKey
     *            The technical key of the customer organization.
     * @param endDate
     *            The end date of the billing period.
     * @param supplierKey
     *            the key of the supplier
     * @return The VAT rate details containing the supplier's default VAT rate,
     *         the VAT rate for the customer country and the customer specific
     *         VAT.
     */
    public VatRateDetails loadVATForCustomer(long customerKey, long endDate,
            long supplierKey);

    /**
     * Loads the billing data for the given time period. The billing data
     * contains the the subscription history objects for all subscriptions.
     *
     * @param organizationKey
     *            The technical key of the organization
     * @param startDate
     *            the start date of the billing period in milliseconds
     * @param endDate
     *            the end date of the billing period in milliseconds
     * @param cutOffDay
     *            the cutOffDay of the subscription, -1 will ignore the
     *            cutOffDay and return all subscriptions within the time unit
     * @return BillingInput
     */
    public List<SubscriptionHistory> loadSubscriptionsForCustomer(
            long organizationKey, long startDate, long endDate, int cutOffDay);

    /**
     * Loads the billing data for the given time period. The billing data
     * contains the the subscription history objects for all subscriptions.
     *
     * @param organizationKey
     *            The technical key of the organization
     * @param unitKeys
     *            the keys of organizational units to which the subscriptions
     *            belong
     * @param startDate
     *            the start date of the billing period in milliseconds
     * @param endDate
     *            the end date of the billing period in milliseconds
     * @param cutOffDay
     *            the cutOffDay of the subscription, -1 will ignore the
     *            cutOffDay and return all subscriptions within the time unit
     * @return BillingInput
     */
    public List<SubscriptionHistory> loadSubscriptionsForCustomer(
            long organizationKey, List<Long> unitKeys, long startDate,
            long endDate, int cutOffDay);

    /**
     * Loads the currency for the given subscription key.
     *
     * @param subscriptionKey
     *            The technical key of the subscription
     * @param endDate
     *            The end date of the billing period.
     * @return SupportedCurrency
     */
    public SupportedCurrency loadCurrency(long subscriptionKey, long endDate);

    /**
     * Loads the BillingContactHistory for the given subscription key.
     *
     * @param subscriptionKey
     *            The technical key of the subscription
     * @return BillingContactHistory
     */
    public BillingContactHistory loadBillingContact(long subscriptionKey);

    /**
     * Reads the key of the supplier of the product that is associated with the
     * subscription.
     *
     * @param subscriptionKey
     *            the key of the subscription to get the supplier key for
     * @return the key of the supplier of the subscriptions product
     */
    public long loadSupplierKeyForSubscription(long subscriptionKey);

    /**
     * Reads the key of the charging organization that offered the product that
     * is associated with the subscription. The charging organization is the
     * supplier if the seller is either the supplier himself or a broker, or the
     * reseller if the service was offered by a reseller.
     *
     * @param subscriptionKey
     *            the key of the subscription to get the supplier key for
     * @return the key of the supplier of the subscriptions product
     */
    public long loadChargingOrgKeyForSubscription(long subscriptionKey);

    /**
     * Reads the key of the vendor of the product that is associated with the
     * subscription.
     *
     * @param subscriptionKey
     *            the key of the subscription to get the vendor key for
     * @return the key of the vendor of the subscriptions product
     */
    public long loadVendorKeyForSubscription(long subscriptionKey);

    /**
     * Reads the roles of the vendor of the specified subscription.
     *
     * @param subscriptionKey
     *            the key of the subscription to get the vendor roles for
     * @return a list of organization roles
     */
    public List<OrganizationRoleType> loadVendorRolesForSubscription(
            long subscriptionKey);

    /**
     * Load the payment type id of the last payment info assigned to the
     * subscription.
     *
     * @param subscriptionKey
     *            the key of the subscription to get the payment type id for
     * @return the payment type id
     */
    public String loadPaymentTypeIdForSubscription(long subscriptionKey);

    public SupportedCurrency loadSupportedCurrency(String currencyISOcode)
            throws ObjectNotFoundException;

    public void updateEvent(long startTimeForPeriod, long endTimeForPeriod,
            long subscriptionKey, BillingResult result);

    /**
     * Retrieves the <code>PriceModelHistory</code> entries for the specified
     * price model, that have a modification date strictly younger than the
     * specified <code>endTimeForPeriod</code>. The returned List is sorted
     * descending by version and modification date.
     *
     * @param priceModelKeyForSubscription
     *            the key of the price model to get the history entries for.
     * @param endTimeForPeriod
     *            the modification date strict upper limit.
     * @return a list of <code>PriceModelHistory</code> as described.
     */
    public List<PriceModelHistory> loadPriceModelHistories(
            long priceModelKeyForSubscription, long endTimeForPeriod);

    /**
     * Evaluates the product reference for the given subscription history
     * element, and determines the technical key of the corresponding price
     * model.
     *
     * @param subscriptionHistory
     *            The subscription history element to determine the price model
     *            key for.
     * @return The price model key.
     * @throws BillingRunFailed
     *             Thrown in case the product history data is missing.
     */
    public long loadPriceModelKeyForSubscriptionHistory(
            SubscriptionHistory subHist) throws BillingRunFailed;

    /**
     * Evaluates the product reference for the given subscription history
     * element, and determines the last history object of the corresponding
     * price model for the subscription history.
     *
     * @param subriptionKey
     *            The subscription history element to determine the price model
     *            key for.
     * @param endPeriod
     *            period end date
     * @return All price model histories for the subscription history.
     */
    public List<PriceModelHistory> loadPricemodelHistoriesForSubscriptionHistory(
            long subscriptionKey, long endPeriod);

    /**
     * @return the latest price model history for the given subscription
     *         history. The subscription's modification date is crucial to
     *         determine the right price model history.
     */
    public PriceModelHistory loadLatestPriceModelHistory(
            SubscriptionHistory history);

    /**
     * Determines the start date of a price model
     *
     * @param priceModelKeyForSubscription
     *            the key of the price model to get the start date for
     * @return the price model start date
     */
    public Date loadPriceModelStartDate(long priceModelKeyForSubscription);

    /**
     * Persists the given billing result and flushes it into the DB.
     *
     * @throws NonUniqueBusinessKeyException
     *             thrown in case a billing result with the same key already
     *             exists in the database
     */
    public void persistBillingResult(BillingResult result)
            throws NonUniqueBusinessKeyException;

    /**
     * Removes a billing result from the database, e.g. has to be called when
     * the billing result should not be persist at all or in case an error
     * happened and an initial billing result was already stored.
     */
    public void removeBillingResult(BillingResult result);

    /**
     * @return a schema object containing the relevant billing schema files. The
     *         files are read from the file system or jar content.
     */
    public Schema loadSchemaFiles();

    /**
     * Retrieves the oldest price model history object which is strictly younger
     * (<) than the modDate parameter.
     */
    public PriceModelHistory loadOldestPriceModelHistory(long priceModelKey,
            long modDate);

    /**
     * Determines the subscription history, which has a product with the given
     * price model key and is located directly before the given time stamp.
     */
    public SubscriptionHistory loadPreviousSubscriptionHistoryForPriceModel(
            final long priceModelKey, final long timeMillis);

    /**
     * Determines the next active subscription history, which has a product with
     * the given price model key and is located at or after the given time
     * stamp.
     */
    public SubscriptionHistory loadNextActiveSubscriptionHistoryForPriceModel(
            final long priceModelKey, final long timeMillis);

    /**
     * Determines the last history of the corresponding product template for a
     * given subscription history before a given deadline, whose modification
     * type is not 'DELETE'.
     *
     * @param subscriptionHistory
     *            a subscription history
     * @param endDate
     *            the maximum modDate of the history
     * @return the last product template history
     * @throws BillingRunFailed
     *             if the product template history cannot be found
     */
    public ProductHistory loadProductTemplateHistoryForSubscriptionHistory(
            SubscriptionHistory subscriptionHistory, long endDate)
            throws BillingRunFailed;

    /**
     * Determines the subscriptions that have to be billed
     *
     * @param effectiveBillingEndDate
     *            the end of the last billing period, that may be calculated in
     *            the current billing run
     * @param cutoffBillingEndDate
     *            the maximum end date of the last billed period
     * @param cutoffDeactivationDate
     *            the minimum deactivation date of the subscription
     * @return a list of subscription data sorted by subscription keys
     */
    public List<BillingSubscriptionData> getSubscriptionsForBilling(
            long effectiveBillingEndDate, long cutoffBillingEndDate,
            long cutoffDeactivationDate);

    /**
     * Loads the relevant subscription histories for the given subscriptions and
     * the given billing period. The subscription histories are sorted by
     * subscription keys in ascending order. For each subscription, the
     * histories are sorted in descending order by object version and moddate.
     *
     * @param subscriptionKeys
     *            a list of subscription keys
     * @param startDate
     *            the start of the billing period
     * @param endDate
     *            the end of the billing period
     * @return a sorted list of subscription histories
     */
    public List<SubscriptionHistory> loadSubscriptionHistoriesForBillingPeriod(
            List<Long> subscriptionKeys, long startDate, long endDate);

    /**
     * Update the billing subscription status in the database
     *
     * @param subscriptionKey
     *            a subscription key
     * @param endOfLastBilledPeriod
     *            the end of the last billed period
     * @throws NonUniqueBusinessKeyException
     *             thrown in case a billing subscription status with the same
     *             key already exists in the database
     */
    public void updateBillingSubscriptionStatus(long subscriptionKey,
            long endOfLastBilledPeriod) throws NonUniqueBusinessKeyException;

    /**
     * Gets the last valid UserGroupHistory record for a specified UserGroup
     * with its key and specified billing period end. It is the last
     * modification record in the history before the specified billing period
     * end.
     *
     * @param groupKey
     *            the key of the UserGroup
     * @param endOfBillingPeriod
     *            end of the billing period
     * @return the last valid UserGroupHistory record
     */
    public UserGroupHistory getLastValidGroupHistory(long groupKey,
            long endOfBillingPeriod);
}
