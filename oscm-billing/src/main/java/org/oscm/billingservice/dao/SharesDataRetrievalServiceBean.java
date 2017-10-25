/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Enes Sejfi                                               
 *                                                                              
 *  Creation Date: 10.05.2010                                                      
 *                                                                              
 *  Completion Time: 27.07.2011                                               
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.dao;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.converter.ParameterizedTypes;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.BillingResult;
import org.oscm.domobjects.BillingSharesResult;
import org.oscm.domobjects.MarketplaceHistory;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationHistory;
import org.oscm.domobjects.OrganizationRole;
import org.oscm.domobjects.ProductHistory;
import org.oscm.domobjects.RevenueShareModelHistory;
import org.oscm.domobjects.SubscriptionHistory;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.validation.Invariants;
import org.oscm.internal.types.enumtypes.BillingSharesResultType;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;

/**
 * Implementation of the share data retrieval service.
 */
@Local(SharesDataRetrievalServiceLocal.class)
@Stateless
public class SharesDataRetrievalServiceBean implements
        SharesDataRetrievalServiceLocal {

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(SharesDataRetrievalServiceBean.class);

    @EJB(beanInterface = DataService.class)
    protected DataService dm;

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public BigDecimal loadMarketplaceRevenueSharePercentage(long mpKey,
            long endPeriod) {
        Invariants.assertGreaterThan(mpKey, 0);
        Invariants.assertGreaterThan(endPeriod, 0);

        RevenueShareModelHistory revenueShareModelHistory = findMarketplaceRevenueShareWithinPeriod(
                mpKey, endPeriod);
        if (revenueShareModelHistory == null) {
            return null;
        }

        return revenueShareModelHistory.getDataContainer().getRevenueShare();
    }

    private RevenueShareModelHistory findMarketplaceRevenueShareWithinPeriod(
            long mpKey, long endPeriod) {
        Query query = dm
                .createNamedQuery("RevenueShareModelHistory.findMarketplaceRevenueSharePercentage");
        query.setParameter("mpKey", Long.valueOf(mpKey));
        query.setParameter("modDate", new Date(endPeriod));
        query.setMaxResults(1);

        try {
            return (RevenueShareModelHistory) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public BigDecimal loadOperatorRevenueSharePercentage(long serviceKey,
            long endPeriod) {
        Query query = dm
                .createNamedQuery("RevenueShareModelHistory.findOperatorRevenueSharePercentage");
        query.setParameter("productObjKey", Long.valueOf(serviceKey));
        query.setParameter("modDate", new Date(endPeriod));
        query.setMaxResults(1);

        BigDecimal percentage;
        try {
            RevenueShareModelHistory revenueShareModelHistory = (RevenueShareModelHistory) query
                    .getSingleResult();
            percentage = revenueShareModelHistory.getDataContainer()
                    .getRevenueShare();
        } catch (NoResultException e) {
            logger.logError(
                    Log4jLogger.SYSTEM_LOG,
                    e,
                    LogMessageIdentifier.ERROR_OPERATOR_REVENUE_SHARE_OF_SERVICE_NOT_FOUND,
                    Long.toString(serviceKey));
            throw e;
        }
        return percentage;
    }

    @Override
    public BigDecimal loadBrokerRevenueSharePercentage(long serviceKey,
            long endPeriod) {
        return loadRevenueSharePercentageForSeller(serviceKey, "Broker",
                endPeriod);
    }

    @Override
    public BigDecimal loadResellerRevenueSharePercentage(long serviceKey,
            long endPeriod) {
        return loadRevenueSharePercentageForSeller(serviceKey, "Reseller",
                endPeriod);
    }

    BigDecimal loadRevenueSharePercentageForSeller(long serviceKey,
            String sellerType, long endPeriod) {
        String queryName = null;
        if (sellerType.equals("Broker")) {
            queryName = "RevenueShareModelHistory.findBrokerRevenueSharePercentage";
        } else if (sellerType.equals("Reseller")) {
            queryName = "RevenueShareModelHistory.findResellerRevenueSharePercentage";
        }

        Query query = dm.createNamedQuery(queryName);
        query.setParameter("productObjKey", Long.valueOf(serviceKey));
        query.setParameter("modDate", new Date(endPeriod));
        query.setMaxResults(1);

        BigDecimal percentage;
        try {
            RevenueShareModelHistory revenueShareModelHistory = (RevenueShareModelHistory) query
                    .getSingleResult();
            percentage = revenueShareModelHistory.getDataContainer()
                    .getRevenueShare();
        } catch (NoResultException e) {
            percentage = null;
        }
        return percentage;
    }

    @Override
    public SubscriptionHistory loadSubscriptionHistoryWithinPeriod(
            long subscriptionKey, long endPeriod) {
        Query query = dm
                .createNamedQuery("SubscriptionHistory.findWithinPeriod");
        query.setParameter("subscriptionKey", Long.valueOf(subscriptionKey));
        query.setParameter("modDate", new Date(endPeriod));
        query.setMaxResults(2);

        List<SubscriptionHistory> histories = ParameterizedTypes.list(
                query.getResultList(), SubscriptionHistory.class);

        return getLastNotDeactivated(histories);
    }

    private SubscriptionHistory getLastNotDeactivated(
            List<SubscriptionHistory> results) {
        if (!results.isEmpty()) {
            final SubscriptionHistory last = results.get(0);
            if (last.getStatus() != SubscriptionStatus.DEACTIVATED) {
                return last;
            } else if (results.size() > 1) {
                return results.get(1);
            }
        }
        return null;
    }

    @Override
    public ProductHistory loadProductOfVendor(long subscriptionObjKey,
            Long priceModelKey, long endPeriod) {
        Query query = dm.createNamedQuery("ProductHistory.findProductOfVendor");
        query.setParameter("subscriptionObjKey",
                Long.valueOf(subscriptionObjKey));
        query.setParameter("modDate", new Date(endPeriod));
        query.setParameter("pmKey", priceModelKey);
        try {
            return (ProductHistory) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<BillingResult> loadBillingResultsForReseller(Long resellerKey,
            long startPeriod, long endPeriod) {
        return loadBillingResultsForSeller(resellerKey, startPeriod, endPeriod);
    }

    @SuppressWarnings("unchecked")
    private List<BillingResult> loadBillingResultsForSeller(Long sellerKey,
            long startPeriod, long endPeriod) {
        Query query = dm.createNamedQuery("BillingResult.findForSeller");
        query.setParameter("startTime", Long.valueOf(startPeriod));
        query.setParameter("endTime", Long.valueOf(endPeriod));
        query.setParameter("sellerKey", sellerKey);
        return query.getResultList();
    }

    @Override
    public List<BillingResult> loadBillingResultsForBroker(Long brokerKey,
            long startPeriod, long endPeriod) {
        return loadBillingResultsForSeller(brokerKey, startPeriod, endPeriod);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<BillingResult> loadBillingResultsForSupplier(Long supplierKey,
            long startPeriod, long endPeriod) {
        // bug 9594, splitted into two queries because of sql performance bug
        Query query = dm
                .createNamedQuery("BillingResult.findForSupplierWhenSupplierProduct");
        query.setParameter("startTime", Long.valueOf(startPeriod));
        query.setParameter("endTime", Long.valueOf(endPeriod));
        query.setParameter("supplierKey", supplierKey);
        List<BillingResult> result = query.getResultList();

        Query query2 = dm
                .createNamedQuery("BillingResult.findForSupplierWhenPartnerProduct");
        query2.setParameter("startTime", Long.valueOf(startPeriod));
        query2.setParameter("endTime", Long.valueOf(endPeriod));
        query2.setParameter("supplierKey", supplierKey);
        List<BillingResult> result2 = query2.getResultList();
        result.addAll(result2);
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Long> loadAllMpOwnerKeysWithinPeriod(long endPeriod) {
        Query query = dm
                .createNamedQuery("OrganizationHistory.findAllMarketplaceOwnerKeys");
        query.setParameter("modDate", new Date(endPeriod));
        return query.getResultList();
    }

    @Override
    public List<Long> loadAllResellerKeysWithinPeriod(long endPeriod) {
        return loadAllSellerKeysWithinPeriod(OrganizationRoleType.RESELLER,
                endPeriod);
    }

    @Override
    public List<Long> loadAllBrokerKeysWithinPeriod(long endPeriod) {
        return loadAllSellerKeysWithinPeriod(OrganizationRoleType.BROKER,
                endPeriod);
    }

    @Override
    public List<Long> loadAllSupplierKeysWithinPeriod(long endPeriod) {
        return loadAllSellerKeysWithinPeriod(OrganizationRoleType.SUPPLIER,
                endPeriod);
    }

    @SuppressWarnings("unchecked")
    private List<Long> loadAllSellerKeysWithinPeriod(OrganizationRoleType role,
            long endPeriod) {
        Query query = dm
                .createNamedQuery("OrganizationHistory.findAllOrganizationsWithRole");
        query.setParameter("modDate", new Date(endPeriod));
        query.setParameter("roleName", role);
        return query.getResultList();
    }

    @Override
    public OrganizationHistory loadLastOrganizationHistory(Long organizationKey) {
        Organization org = new Organization();
        org.setKey(organizationKey.longValue());
        return (OrganizationHistory) dm.findLastHistory(org);
    }

    @Override
    public String getSupportedCountryCode(Long orgKey) {
        return dm.find(Organization.class, orgKey).getDomicileCountryCode();
    }

    @Override
    public MarketplaceHistory loadMarketplaceHistoryWithinPeriod(long mpKey,
            long endPeriod) {
        Query query = dm
                .createNamedQuery("MarketplaceHistory.findWithinPeriod");
        query.setParameter("mpKey", Long.valueOf(mpKey));
        query.setParameter("modDate", new Date(endPeriod));
        query.setMaxResults(1);
        try {
            return (MarketplaceHistory) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }

    }

    @Override
    public MarketplaceHistory loadMarketplaceHistoryBySubscriptionKey(
            long subscriptionKey, long endPeriod) {
        Query query = dm
                .createNamedQuery("MarketplaceHistory.findBySubscriptionKey");
        query.setParameter("subscriptionKey", Long.valueOf(subscriptionKey));
        query.setParameter("modDate", new Date(endPeriod));
        query.setMaxResults(1);
        try {
            return (MarketplaceHistory) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<OrganizationRole> loadOrganizationHistoryRoles(
            long organizationKey, long endPeriod) {
        Query query = dm
                .createNamedQuery("OrganizationRole.findByOrganizationHistory");
        query.setParameter("organizationKey", Long.valueOf(organizationKey));
        query.setParameter("modDate", new Date(endPeriod));
        return query.getResultList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Long> loadMarketplaceKeys(long mpOwnerKey, long endPeriod) {
        Query query = dm
                .createNamedQuery("MarketplaceHistory.findMarketplaceKeys");
        query.setParameter("mpOwnerKey", Long.valueOf(mpOwnerKey));
        query.setParameter("modDate", new Date(endPeriod));
        return query.getResultList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> loadSupportedCurrencies() {
        return dm.createNamedQuery("SupportedCurrency.findAllCodes")
                .getResultList();
    }

    @Override
    public OrganizationHistory loadSupplierHistoryOfProduct(long serviceKey) {
        Query query = dm
                .createNamedQuery("OrganizationHistory.findSupplierOfPartnerProduct");
        query.setParameter("productKey", Long.valueOf(serviceKey));
        query.setMaxResults(1);
        try {
            return (OrganizationHistory) query.getSingleResult();
        } catch (NoResultException e) {
            return loadVendorHistoryOfProduct(serviceKey);
        }
    }

    private OrganizationHistory loadVendorHistoryOfProduct(long serviceKey) {
        Query query = dm
                .createNamedQuery("OrganizationHistory.findVendorOfProduct");
        query.setParameter("productKey", Long.valueOf(serviceKey));
        query.setMaxResults(1);
        try {
            return (OrganizationHistory) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<BillingSharesResult> loadBillingSharesResultForOrganization(
            Long orgKey, BillingSharesResultType resultType, Long startPeriod,
            Long endPeriod) {
        Query query = dm
                .createNamedQuery("BillingSharesResult.getSharesResultForOrganization");
        query.setParameter("orgKey", orgKey);
        query.setParameter("resultType", resultType);
        query.setParameter("fromDate", startPeriod);
        query.setParameter("toDate", endPeriod);
        return query.getResultList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<BillingSharesResult> loadBillingSharesResult(
            BillingSharesResultType resultType, Long startPeriod, Long endPeriod) {
        Query query = dm
                .createNamedQuery("BillingSharesResult.getSharesResult");
        query.setParameter("resultType", resultType);
        query.setParameter("fromDate", startPeriod);
        query.setParameter("toDate", endPeriod);
        return query.getResultList();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<BillingResult> loadBillingResultsForMarketplace(
            Long mpKey, String isoCode, Long startOfMonth, Long endOfMonth) {
        Query query = dm.createNamedQuery("BillingResult.findForPeriod");
        query.setParameter("startPeriod", startOfMonth);
        query.setParameter("endPeriod", endOfMonth);
        query.setParameter("mpKey", mpKey);
        query.setParameter("isoCode", isoCode);
        return query.getResultList();
    }
}
