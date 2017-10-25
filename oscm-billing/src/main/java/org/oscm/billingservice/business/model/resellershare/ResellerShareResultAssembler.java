/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.billingservice.business.model.resellershare;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.oscm.billingservice.business.XmlSearch;
import org.oscm.billingservice.dao.BillingDataRetrievalServiceLocal;
import org.oscm.billingservice.dao.SharesDataRetrievalServiceLocal;
import org.oscm.domobjects.BillingResult;
import org.oscm.domobjects.OrganizationHistory;
import org.oscm.domobjects.ProductHistory;
import org.oscm.domobjects.SubscriptionHistory;
import org.oscm.validation.Invariants;

public class ResellerShareResultAssembler {

    private BillingResult currentBillingResult;
    private long periodEndTime;
    private Long pmKey;
    private XmlSearch xmlSearch;
    ResellerRevenueShareResult result;
    SharesDataRetrievalServiceLocal sharesRetrievalService;
    BillingDataRetrievalServiceLocal billingRetrievalService;

    public ResellerShareResultAssembler(
            SharesDataRetrievalServiceLocal sharesRetrievalService,
            BillingDataRetrievalServiceLocal billingRetrievalService) {
        this.sharesRetrievalService = sharesRetrievalService;
        this.billingRetrievalService = billingRetrievalService;
    }

    public ResellerRevenueShareResult build(Long resellerKey,
            long periodStartTime, long periodEndTime)
            throws DatatypeConfigurationException {
        Invariants.assertNotNull(resellerKey);

        result = new ResellerRevenueShareResult();
        setOrganizationData(resellerKey);
        setResellerData(resellerKey);
        setPeriod(periodStartTime, periodEndTime);

        List<BillingResult> billingResults = sharesRetrievalService
                .loadBillingResultsForReseller(resellerKey, periodStartTime,
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

    private void setOrganizationData(Long orgKey) {
        OrganizationHistory org = sharesRetrievalService
                .loadLastOrganizationHistory(orgKey);
        String countryIsoCode = sharesRetrievalService
                .getSupportedCountryCode(orgKey);
        OrganizationData orgData = new OrganizationData();
        orgData.setAddress(org.getAddress());
        orgData.setEmail(org.getEmail());
        orgData.setKey(BigInteger.valueOf(orgKey.longValue()));
        orgData.setName(org.getOrganizationName());
        orgData.setId(org.getOrganizationId());
        orgData.setCountryIsoCode(countryIsoCode);
        result.setOrganizationData(orgData);
    }

    private void setResellerData(Long resellerKey) {
        OrganizationHistory reseller = sharesRetrievalService
                .loadLastOrganizationHistory(resellerKey);
        result.setOrganizationKey(BigInteger.valueOf(reseller.getObjKey()));
        result.setOrganizationId(reseller.getOrganizationId());
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

        // start date
        period.setStartDate(BigInteger.valueOf(periodStartTime));
        gc.setTimeInMillis(periodStartTime);
        period.setStartDateIsoFormat(df.newXMLGregorianCalendar(gc).normalize());

        // end date
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

        addSupplier(currency);
    }

    private void addSupplier(Currency currency)
            throws DatatypeConfigurationException {
        long subscriptionKey = currentBillingResult.getSubscriptionKey()
                .longValue();
        long supplierKey = billingRetrievalService
                .loadSupplierKeyForSubscription(subscriptionKey);
        Supplier supplier = currency.getSupplierByKey(supplierKey);
        if (supplier == null) {
            supplier = buildSupplier(supplierKey);
            currency.addSupplier(supplier);
        }

        addService(supplier, subscriptionKey);
    }

    private Supplier buildSupplier(long supplierKey) {
        Supplier supplier = new Supplier();
        OrganizationHistory organizationHistory = sharesRetrievalService
                .loadLastOrganizationHistory(Long.valueOf(supplierKey));
        String countryIsoCode = sharesRetrievalService
                .getSupportedCountryCode(Long.valueOf(supplierKey));
        supplier.setOrganizationData(buildSupplierData(supplierKey,
                organizationHistory, countryIsoCode));
        return supplier;
    }

    private OrganizationData buildSupplierData(long supplierKey,
            OrganizationHistory organization, String countryIsoCode) {
        OrganizationData organizationData = new OrganizationData();
        organizationData.setId(organization.getOrganizationId());
        organizationData.setKey(BigInteger.valueOf(supplierKey));
        organizationData.setName(organization.getOrganizationName());
        organizationData.setEmail(organization.getEmail());
        organizationData.setAddress(organization.getAddress());
        organizationData.setCountryIsoCode(countryIsoCode);
        return organizationData;
    }

    private void addService(Supplier supplier, long subscriptionKey)
            throws DatatypeConfigurationException {
        Set<Long> priceModelKeys = xmlSearch.findPriceModelKeys();
        for (Iterator<Long> iterator = priceModelKeys.iterator(); iterator
                .hasNext();) {
            pmKey = iterator.next();

            ProductHistory productHistory = sharesRetrievalService
                    .loadProductOfVendor(subscriptionKey, pmKey, periodEndTime);
            Service service = supplier.getServiceByKey(productHistory
                    .getObjKey());
            if (service == null) {
                service = buildService(productHistory);
                supplier.addService(service);
            }

            addSubscription(service, subscriptionKey);
            setServiceRevenue(service.getServiceRevenue());
            addServiceCustomerRevenue(service, subscriptionKey);
        }
    }

    Service buildService(ProductHistory product) {
        Service service = new Service();
        service.setKey(BigInteger.valueOf(product.getObjKey()));
        service.setId(product.getCleanProductId());
        service.setTemplateKey(BigInteger.valueOf(product.getTemplateObjKey()
                .longValue()));
        setRevenueSharePercentage(service);
        return service;
    }

    private void setRevenueSharePercentage(Service service) {
        BigDecimal revenueSharePercentage = sharesRetrievalService
                .loadResellerRevenueSharePercentage(service.getKey()
                        .longValue(), periodEndTime);
        Invariants.assertNotNull(revenueSharePercentage);
        service.getServiceRevenue().setResellerRevenueSharePercentage(
                revenueSharePercentage);
    }

    private void setServiceRevenue(ServiceRevenue serviceRevenue) {
        BigDecimal totalAmount = serviceRevenue.getTotalAmount();
        totalAmount = totalAmount.add(currentBillingResult.getNetAmount());
        serviceRevenue.setTotalAmount(totalAmount);
    }

    private void addServiceCustomerRevenue(Service service, long subscriptionKey) {

        OrganizationHistory customer = getCustomerBySubscription(subscriptionKey);

        ServiceCustomerRevenue serviceCustomerRevenue = new ServiceCustomerRevenue();
        serviceCustomerRevenue.setTotalAmount(currentBillingResult
                .getNetAmount());
        BigDecimal revenueSharePercentage = sharesRetrievalService
                .loadResellerRevenueSharePercentage(service.getKey()
                        .longValue(), periodEndTime);
        Invariants.assertNotNull(revenueSharePercentage);
        serviceCustomerRevenue
                .setResellerRevenueSharePercentage(revenueSharePercentage);
        serviceCustomerRevenue.setCustomerId(customer.getOrganizationId());
        serviceCustomerRevenue.setCustomerName(customer.getOrganizationName());
        serviceCustomerRevenue.calculate();
        service.getServiceRevenue().addServiceCustomerRevenue(
                serviceCustomerRevenue);
    }

    private void addSubscription(Service service, long subscriptionKey)
            throws DatatypeConfigurationException {
        Subscription subscription = service
                .getSubscriptionByKey(subscriptionKey);
        Invariants.assertNull(subscription, "Duplicate subscription key "
                + subscriptionKey);
        subscription = buildSubscription(subscriptionKey);
        service.addSubscription(subscription);
    }

    private Subscription buildSubscription(long subscriptionKey)
            throws DatatypeConfigurationException {
        SubscriptionHistory subscriptionHistory = sharesRetrievalService
                .loadSubscriptionHistoryWithinPeriod(subscriptionKey,
                        periodEndTime);

        Subscription subscription = new Subscription();
        subscription.setKey(BigInteger.valueOf(subscriptionKey));
        subscription.setId(subscriptionHistory.getDataContainer()
                .getSubscriptionId());
        subscription.setBillingKey(BigInteger.valueOf(currentBillingResult
                .getKey()));
        subscription.setRevenue(currentBillingResult.getNetAmount());
        Period period = preparePeriod(
                this.currentBillingResult.getPeriodStartTime(),
                this.currentBillingResult.getPeriodEndTime());
        subscription.setPeriod(period);
        return subscription;
    }

    private OrganizationHistory getCustomerBySubscription(long subscriptionKey) {
        SubscriptionHistory subscriptionHistory = sharesRetrievalService
                .loadSubscriptionHistoryWithinPeriod(subscriptionKey,
                        periodEndTime);
        OrganizationHistory customer = sharesRetrievalService
                .loadLastOrganizationHistory(Long.valueOf(subscriptionHistory
                        .getOrganizationObjKey()));
        return customer;

    }
}
