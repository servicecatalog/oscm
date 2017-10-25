/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 10.05.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.dao;

import java.math.BigDecimal;
import java.util.List;

import javax.ejb.Local;

import org.oscm.domobjects.BillingResult;
import org.oscm.domobjects.BillingSharesResult;
import org.oscm.domobjects.MarketplaceHistory;
import org.oscm.domobjects.OrganizationHistory;
import org.oscm.domobjects.OrganizationRole;
import org.oscm.domobjects.ProductHistory;
import org.oscm.domobjects.SubscriptionHistory;
import org.oscm.internal.types.enumtypes.BillingSharesResultType;

/**
 * Internal service to retrieve data required by the billing service.
 * 
 * @author cheld
 */
@Local
public interface SharesDataRetrievalServiceLocal {

    /**
     * Loads all billing results which were created in the specified period and
     * whose subscription products were published on a marketplace with the
     * given marketplace key.
     * 
     * @param startOfMonth
     * @param endOfMonth
     * @param mpKey
     * @return the loaded billing results
     */
    public List<BillingResult> loadBillingResultsForMarketplace(
            Long mpKey, String isoCode, Long startOfMonth, Long endOfMonth);

    /**
     * Reads the revenue share from the marketplace which was valid in the given
     * period for the given marketplace id.
     * 
     * @param mpKey
     *            the key of the marketplace
     * @param endPeriod
     *            the end of the billing period
     * @return the revenue share for the given revenue share model type which
     *         was valid in the given period
     */
    public BigDecimal loadMarketplaceRevenueSharePercentage(long mpKey,
            long endPeriod);

    /**
     * Reads the operatorrevenue share from the catalog entry which was valid in
     * the given period for the given service.
     * 
     * @param serviceKey
     *            the key of the service
     * @param endPeriod
     *            the end of the billing period
     * @return the revenue share percentage for the operator which was valid in
     *         the given period
     */
    public BigDecimal loadOperatorRevenueSharePercentage(long serviceKey,
            long endPeriod);

    /**
     * Reads the broker revenue share from the catalog entry which was valid in
     * the given period for the given service.
     * 
     * @param serviceKey
     *            the key of the service
     * @param endPeriod
     *            the end of the billing period
     * @return the revenue share percentage for the broker which was valid in
     *         the given period
     */
    public BigDecimal loadBrokerRevenueSharePercentage(long serviceKey,
            long endPeriod);

    /**
     * Reads the reseller revenue share from the catalog entry which was valid
     * in the given period for the given service.
     * 
     * @param serviceKey
     *            the key of the service
     * @param endPeriod
     *            the end of the billing period
     * @return the revenue share percentage for the reseller which was valid in
     *         the given period
     */
    public BigDecimal loadResellerRevenueSharePercentage(long serviceKey,
            long endPeriod);

    /**
     * Loads the latest organization history instance.
     * 
     * @param organizationKey
     *            tKey of the organization
     * @return organization history
     * @throws ObjectNotFoundExceptionRevenueShareModelType
     *             if the organization was not found
     */
    public OrganizationHistory loadLastOrganizationHistory(Long organizationKey);

    /**
     * Loads a marketplace history with marketplace key and within a given
     * period.
     * 
     * @param mpKey
     *            the key of the marketplace
     * @param endPeriod
     *            the end of the billing period
     * @return
     */
    public MarketplaceHistory loadMarketplaceHistoryWithinPeriod(long mpKey,
            long endPeriod);

    /**
     * Loads the product that was published to the marketplace by the vendor
     * that corresponds to the given subscription key and price model key. In
     * other words, in case the vendor is a reseller, then the reseller copy is
     * returned (and not the product template of the supplier). In case the
     * vendor is a supplier, then the product template is returned.
     */
    public ProductHistory loadProductOfVendor(long subscriptionObjKey,
            Long priceModelKey, long endPeriod);

    /**
     * Loads all mpowner keys that existed in the given period.
     * 
     * @return list of organization keys
     */
    public List<Long> loadAllMpOwnerKeysWithinPeriod(long endPeriod);

    /**
     * Loads all supplier keys that existed in the given period. The list may
     * also contain suppliers that did not make a revenue.
     * 
     * @return list of organization keys
     */
    public List<Long> loadAllSupplierKeysWithinPeriod(long endPeriod);

    /**
     * Loads all broker keys that existed in the given period. The list may also
     * contain brokers that did not make a revenue.
     * 
     * @return list of organization keys
     */
    public List<Long> loadAllBrokerKeysWithinPeriod(long endPeriod);

    /**
     * Loads all reseller keys that existed in the given period. The list may
     * also contain resellers that did not make a revenue.
     * 
     * @return list of organization keys
     */
    public List<Long> loadAllResellerKeysWithinPeriod(long endPeriod);

    /**
     * Loads all billing results for products of the given broker.
     * 
     * 
     * @param brokerKey
     *            the organization key
     * @param startPeriod
     * @param endPeriod
     *            the end of the billing period
     * @return list of billing results
     */
    public List<BillingResult> loadBillingResultsForBroker(Long brokerKey,
            long startPeriod, long endPeriod);

    /**
     * Loads all billing results for products of the given supplier.
     * 
     * 
     * @param supplierKey
     *            the organization key
     * @param startOfLastMonth
     *            the start of the billing period
     * @param endOfLastMonth
     *            the end of the billing period
     * @return list of billing results
     */
    public List<BillingResult> loadBillingResultsForSupplier(Long supplierKey,
            long startPeriod, long endPeriod);

    /**
     * Loads all billing results for products of the given reseller.
     * 
     * 
     * @param resellerKey
     *            the organization key
     * @param startPeriod
     * @param endPeriod
     *            the end of the billing period
     * @return list of billing results
     */
    public List<BillingResult> loadBillingResultsForReseller(Long resellerKey,
            long startPeriod, long endPeriod);

    /**
     * Loads a subscription history with subscription key and within a given
     * period.
     * 
     * @param subscriptionKey
     *            subscription key
     * @param endPeriod
     *            end of period
     * @return a subscription history instance
     */
    public SubscriptionHistory loadSubscriptionHistoryWithinPeriod(
            long subscriptionKey, long endPeriod);

    /**
     * Loads a marketplace history with subscription key and within a given
     * period.
     * 
     * @param subscriptionKey
     *            subscription key
     * @param endPeriod
     *            end of period
     * @return a marketplace history instance
     */
    public MarketplaceHistory loadMarketplaceHistoryBySubscriptionKey(
            long subscriptionKey, long endPeriod);

    /**
     * Loads all organization roles of an organization which are set within a
     * given period
     * 
     * @param organizationKey
     *            organization key
     * @param endPeriod
     *            end period
     * @return list of organization roles
     */
    public List<OrganizationRole> loadOrganizationHistoryRoles(
            long organizationKey, long endPeriod);

    /**
     * Loads all supported currencies
     * 
     * @return list of ISO currency codes
     */
    public List<String> loadSupportedCurrencies();

    /**
     * Loads special supported Country ISO Code
     * 
     * @return ISO country codes
     */
    public String getSupportedCountryCode(Long orgKey);

    /**
     * Loads all marketplace IDs within a given period for the given marketplace
     * owner
     * 
     * @param mpOwnerKey
     *            the organization key of the marketplace owner
     * @param endPeriod
     *            end period
     * 
     * @return list of marketplace IDs
     */
    public List<Long> loadMarketplaceKeys(long mpOwnerKey, long endPeriod);

    /**
     * Loads supplier history with given productKey
     * 
     * @param serviceKey
     *            service key
     * @return supplier history
     */
    public OrganizationHistory loadSupplierHistoryOfProduct(long serviceKey);

    /**
     * Loads the billing share results for the given input parameters.
     * 
     * @param orgKey
     *            organization key
     * @param resultType
     *            the type of the billing share result
     * @param startPeriod
     *            start period
     * @param endPeriod
     *            end period
     * 
     * @return the list of the found billing share results
     */
    public List<BillingSharesResult> loadBillingSharesResultForOrganization(
            Long orgKey, BillingSharesResultType resultType, Long startPeriod,
            Long endPeriod);

    /**
     * Loads the billing share results for the given input parameters.
     * 
     * @param resultType
     *            the type of the billing share result
     * @param startPeriod
     *            start period
     * @param endPeriod
     *            end period
     * 
     * @return the list of the found billing share results
     */

    public List<BillingSharesResult> loadBillingSharesResult(
            BillingSharesResultType resultType, Long startPeriod, Long endPeriod);
}
