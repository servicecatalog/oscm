/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.billingservice.business.model.suppliershare;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.oscm.billingservice.business.XmlSearch;
import org.oscm.billingservice.business.calculation.share.DiscountCalculator;
import org.oscm.billingservice.dao.SharesDataRetrievalServiceLocal;
import org.oscm.domobjects.BillingResult;
import org.oscm.domobjects.MarketplaceHistory;
import org.oscm.domobjects.OrganizationHistory;
import org.oscm.domobjects.OrganizationRole;
import org.oscm.domobjects.ProductHistory;
import org.oscm.domobjects.SubscriptionHistory;
import org.oscm.validation.Invariants;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ServiceType;

public class SupplierShareResultAssembler {

    private long periodEndTime;
    private Long pmKey;
    BillingResult currentBillingResult;
    XmlSearch xmlSearch;
    SupplierRevenueShareResult result;
    SharesDataRetrievalServiceLocal billingRetrievalService;

    public SupplierShareResultAssembler(
            SharesDataRetrievalServiceLocal billingRetrievalService) {
        this.billingRetrievalService = billingRetrievalService;
    }

    /**
     * Iterates over all billing results.<br />
     * 
     * Shares are extracted price model by price model and are based on net
     * values.
     * 
     * @param supplierKey
     *            valid key of a supplier organization
     * @param periodStartTime
     *            start of a calendar month is expected
     * @param periodEndTime
     *            end of a calendar month is expected
     */
    public SupplierRevenueShareResult build(Long supplierKey,
            long periodStartTime, long periodEndTime)
            throws DatatypeConfigurationException {

        Invariants.assertNotNull(supplierKey);

        result = new SupplierRevenueShareResult();
        OrganizationHistory org = billingRetrievalService
                .loadLastOrganizationHistory(supplierKey);
        result.setOrganizationData(buildOrganizationData(org));
        setSupplierData(supplierKey);
        setPeriod(periodStartTime, periodEndTime);

        List<BillingResult> billingResults = billingRetrievalService
                .loadBillingResultsForSupplier(supplierKey, periodStartTime,
                        periodEndTime);
        for (BillingResult billingResult : billingResults) {
            currentBillingResult = billingResult;
            xmlSearch = newXmlSearch(currentBillingResult);
            addCurrency();
        }
        return result;
    }

    XmlSearch newXmlSearch(BillingResult billingResult) {
        return new XmlSearch(billingResult);
    }

    private void setSupplierData(Long supplierKey) {
        OrganizationHistory supplier = billingRetrievalService
                .loadLastOrganizationHistory(supplierKey);
        result.setOrganizationKey(BigInteger.valueOf(supplier.getObjKey()));
        result.setOrganizationId(supplier.getOrganizationId());
    }

    void setPeriod(long periodStartTime, long periodEndTime)
            throws DatatypeConfigurationException {

        this.periodEndTime = periodEndTime;
        Period period = preparePeriod(periodStartTime, periodEndTime);
        result.setPeriod(period);
    }

    private Period preparePeriod(long periodStartTime, long periodEndTime)
            throws DatatypeConfigurationException {

        Period period = new Period();

        DatatypeFactory df = DatatypeFactory.newInstance();
        GregorianCalendar gc = new GregorianCalendar();

        period.setStartDate(BigInteger.valueOf(periodStartTime));
        gc.setTimeInMillis(periodStartTime);
        period.setStartDateIsoFormat(df.newXMLGregorianCalendar(gc).normalize());

        period.setEndDate(BigInteger.valueOf(periodEndTime));
        gc.setTimeInMillis(periodEndTime);
        period.setEndDateIsoFormat(df.newXMLGregorianCalendar(gc).normalize());

        return period;
    }

    private void addCurrency() throws DatatypeConfigurationException {
        String currencyCode = currentBillingResult.getCurrencyCode();
        Currency currency = result.getCurrencyByCode(currencyCode);
        if (currency == null) {
            currency = new Currency(currencyCode);
            result.addCurrency(currency);
        }

        addMarketplace(currency);
    }

    private void addMarketplace(Currency currency)
            throws DatatypeConfigurationException {
        long subscriptionKey = currentBillingResult.getSubscriptionKey()
                .longValue();
        MarketplaceHistory marketplaceHistory = billingRetrievalService
                .loadMarketplaceHistoryBySubscriptionKey(subscriptionKey,
                        periodEndTime);
        Marketplace marketplace = currency.getMarketplace(marketplaceHistory
                .getObjKey());
        if (marketplace == null) {
            marketplace = buildMarketplace(marketplaceHistory);
            currency.addMarketplace(marketplace);
        }

        addService(marketplace, subscriptionKey);
    }

    Marketplace buildMarketplace(MarketplaceHistory marketplaceHistory) {
        Marketplace marketplace = new Marketplace();
        marketplace.setKey(BigInteger.valueOf(marketplaceHistory.getObjKey()));
        marketplace.setId(marketplaceHistory.getDataContainer()
                .getMarketplaceId());
        OrganizationHistory organizationHistory = loadLastOrganizationHistory(marketplaceHistory
                .getOrganizationObjKey());
        marketplace
                .setMarketplaceOwner(buildMarketplaceOwnerData(organizationHistory));
        marketplace
                .setRevenueSharePercentage(loadMarketplaceRevenueSharePercentage(marketplaceHistory
                        .getObjKey()));
        return marketplace;
    }

    private OrganizationHistory loadLastOrganizationHistory(long organizationKey) {
        return billingRetrievalService.loadLastOrganizationHistory(Long
                .valueOf(organizationKey));
    }

    private MarketplaceOwner buildMarketplaceOwnerData(
            OrganizationHistory organization) {
        MarketplaceOwner marketplaceOwner = new MarketplaceOwner();
        marketplaceOwner
                .setOrganizationData(buildOrganizationData(organization));
        return marketplaceOwner;
    }

    private OrganizationData buildOrganizationData(
            OrganizationHistory orgHistory) {
        OrganizationData organizationData = new OrganizationData();
        organizationData.setId(orgHistory.getOrganizationId());
        organizationData.setKey(BigInteger.valueOf(orgHistory.getObjKey()));
        organizationData.setName(orgHistory.getOrganizationName());
        organizationData.setEmail(orgHistory.getEmail());
        organizationData.setAddress(orgHistory.getAddress());
        String countryIsoCode = billingRetrievalService
                .getSupportedCountryCode(Long.valueOf(orgHistory.getObjKey()));
        organizationData.setCountryIsoCode(countryIsoCode);
        return organizationData;
    }

    private BigDecimal loadMarketplaceRevenueSharePercentage(long mpKey) {
        return billingRetrievalService.loadMarketplaceRevenueSharePercentage(
                mpKey, periodEndTime);
    }

    private void addService(Marketplace marketplace, long subscriptionKey)
            throws DatatypeConfigurationException {

        Set<Long> priceModelKeys = xmlSearch.findPriceModelKeys();
        for (Iterator<Long> iterator = priceModelKeys.iterator(); iterator
                .hasNext();) {
            pmKey = iterator.next();
            ProductHistory prd = billingRetrievalService.loadProductOfVendor(
                    subscriptionKey, pmKey, periodEndTime);
            Service service = marketplace.getServiceByKey(prd.getObjKey());
            if (service == null) {
                service = buildService(marketplace, prd);
                marketplace.addService(service);
            }
            addSubscription(service, subscriptionKey);
            addServiceCustomerRevenue(service, subscriptionKey);
        }
    }

    Service buildService(Marketplace marketplace, ProductHistory vendorProduct) {
        Service service = new Service();
        service.setKey(BigInteger.valueOf(vendorProduct.getObjKey()));
        service.setId(vendorProduct.getCleanProductId());
        service.getRevenueShareDetails().setMarketplaceRevenueSharePercentage(
                marketplace.getRevenueSharePercentage());
        service.getRevenueShareDetails().setOperatorRevenueSharePercentage(
                getOperatorRevenueShareForVendorProduct(vendorProduct));

        List<OrganizationRole> orgRoles = billingRetrievalService
                .loadOrganizationHistoryRoles(vendorProduct.getVendorObjKey(),
                        periodEndTime);
        if (hasOrganizationRole(orgRoles, OrganizationRoleType.BROKER)) {
            service.setModel(OrganizationRoleType.BROKER.name());
            addBrokerData(service, vendorProduct.getVendorObjKey());
            setBrokerRevenueSharePercentage(service);

        } else if (hasOrganizationRole(orgRoles, OrganizationRoleType.RESELLER)) {
            service.setModel(OrganizationRoleType.RESELLER.name());
            addResellerData(service, vendorProduct.getVendorObjKey());
            setResellerRevenueSharePercentage(service);
        } else if (hasOrganizationRole(orgRoles, OrganizationRoleType.SUPPLIER)) {
            service.setModel(OfferingType.DIRECT.name());
        }

        if (!service.getModel().equals(OfferingType.DIRECT.name())) {
            service.setTemplateKey(BigInteger.valueOf(vendorProduct
                    .getTemplateObjKey().longValue()));
        }
        return service;
    }

    private BigDecimal getOperatorRevenueShareForVendorProduct(
            ProductHistory vendorProduct) {

        long templateKey;
        if (vendorProduct.getDataContainer().getType() == ServiceType.TEMPLATE) {
            templateKey = vendorProduct.getObjKey();
        } else {
            templateKey = vendorProduct.getTemplateObjKey().longValue();
        }
        return billingRetrievalService.loadOperatorRevenueSharePercentage(
                templateKey, periodEndTime);
    }

    private boolean hasOrganizationRole(
            List<OrganizationRole> organizationRoles,
            OrganizationRoleType roleToBeFound) {
        for (OrganizationRole organizationRole : organizationRoles) {
            if (organizationRole.getRoleName() == roleToBeFound) {
                return true;
            }
        }
        return false;
    }

    private void addBrokerData(Service service, long vendorKey) {
        Broker broker = new Broker();
        OrganizationHistory organization = loadLastOrganizationHistory(vendorKey);
        broker.setOrganizationData(buildOrganizationData(organization));
        service.setBroker(broker);
    }

    private void setBrokerRevenueSharePercentage(Service service) {
        BigDecimal revenueSharePercentage = billingRetrievalService
                .loadBrokerRevenueSharePercentage(service.getKey().longValue(),
                        periodEndTime);
        service.getRevenueShareDetails().setBrokerRevenueSharePercentage(
                revenueSharePercentage);
    }

    private void addResellerData(Service service, long vendorKey) {
        Reseller reseller = new Reseller();
        OrganizationHistory organization = loadLastOrganizationHistory(vendorKey);
        reseller.setOrganizationData(buildOrganizationData(organization));
        service.setReseller(reseller);
    }

    private void setResellerRevenueSharePercentage(Service service) {
        BigDecimal revenueSharePercentage = billingRetrievalService
                .loadResellerRevenueSharePercentage(service.getKey()
                        .longValue(), periodEndTime);
        service.getRevenueShareDetails().setResellerRevenueSharePercentage(
                revenueSharePercentage);
    }

    private void addSubscription(Service service, long subscriptionKey)
            throws DatatypeConfigurationException {
        BigDecimal serviceRevenue = DiscountCalculator.calculateServiceRevenue(
                xmlSearch, pmKey);
        if (service.getModel().equals(OfferingType.RESELLER.name())) {
            SubscriptionsRevenue revenue = service
                    .retrieveSubscriptionsRevenue();
            revenue.sumUp(serviceRevenue);
        } else {
            Subscription subscription = service
                    .getSubscriptionByKey(subscriptionKey);
            if (subscription != null) {
                // Billing result contains multiple price models for the same
                // service.This may occur if a subscription is upgraded to a new
                // service and downgraded again to the original service in the
                // same billing period.
                subscription.sumUpRevenue(serviceRevenue);
            } else {
                subscription = buildSubscription(subscriptionKey,
                        serviceRevenue);
                service.addSubscription(subscription);
            }
        }
    }

    private Subscription buildSubscription(long subscriptionKey,
            BigDecimal serviceRevenue) throws DatatypeConfigurationException {

        SubscriptionHistory subscriptionHistory = billingRetrievalService
                .loadSubscriptionHistoryWithinPeriod(subscriptionKey,
                        periodEndTime);

        Subscription subscription = new Subscription();
        subscription.setKey(BigInteger.valueOf(subscriptionKey));
        subscription.setId(subscriptionHistory.getDataContainer()
                .getSubscriptionId());
        subscription.setBillingKey(BigInteger.valueOf(currentBillingResult
                .getKey()));
        subscription.setRevenue(serviceRevenue);
        Period period = preparePeriod(
                this.currentBillingResult.getPeriodStartTime(),
                this.currentBillingResult.getPeriodEndTime());
        subscription.setPeriod(period);
        return subscription;
    }

    private void addServiceCustomerRevenue(Service service, long subscriptionKey) {
        OrganizationHistory customer = getCustomerBySubscription(subscriptionKey);
        CustomerRevenueShareDetails customerRevenue = new CustomerRevenueShareDetails();
        customerRevenue.setServiceRevenue(DiscountCalculator
                .calculateServiceRevenue(xmlSearch, pmKey));
        customerRevenue.setCustomerId(customer.getOrganizationId());
        customerRevenue.setCustomerName(customer.getOrganizationName());

        BigDecimal marketplaceRevenueSharePercentage = service
                .getRevenueShareDetails()
                .getMarketplaceRevenueSharePercentage();
        BigDecimal operatorSharePercentage = service.getRevenueShareDetails()
                .getOperatorRevenueSharePercentage();

        customerRevenue.calculate(marketplaceRevenueSharePercentage,
                operatorSharePercentage,
                getBrokerRevenueSharePercentage(service),
                getResellerRevenueSharePercentage(service));

        service.getRevenueShareDetails().addCustomerRevenueShareDetails(
                customerRevenue);
    }

    private OrganizationHistory getCustomerBySubscription(long subscriptionKey) {
        SubscriptionHistory subscriptionHistory = billingRetrievalService
                .loadSubscriptionHistoryWithinPeriod(subscriptionKey,
                        periodEndTime);
        OrganizationHistory customer = billingRetrievalService
                .loadLastOrganizationHistory(Long.valueOf(subscriptionHistory
                        .getOrganizationObjKey()));
        return customer;

    }

    private BigDecimal getBrokerRevenueSharePercentage(Service service) {
        BigDecimal brokerRevenueSharePercentage = null;
        if (service.getBroker() != null) {
            brokerRevenueSharePercentage = billingRetrievalService
                    .loadBrokerRevenueSharePercentage(service.getKey()
                            .longValue(), periodEndTime);
        }
        return brokerRevenueSharePercentage;
    }

    private BigDecimal getResellerRevenueSharePercentage(Service service) {
        BigDecimal resellerRevenueSharePercentage = null;
        if (service.getReseller() != null) {
            resellerRevenueSharePercentage = billingRetrievalService
                    .loadResellerRevenueSharePercentage(service.getKey()
                            .longValue(), periodEndTime);
        }
        return resellerRevenueSharePercentage;
    }

}
