/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.billingservice.business.model.brokershare;

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

public class BrokerShareResultAssembler {

    private BillingResult currentBillingResult;
    private long periodEndTime;
    private XmlSearch xmlSearch;
    private Long pmKey;
    BrokerRevenueShareResult result;
    SharesDataRetrievalServiceLocal sharesRetrievalService;
    BillingDataRetrievalServiceLocal billingRetrievalService;

    public BrokerShareResultAssembler(
            SharesDataRetrievalServiceLocal sharesRetrievalService,
            BillingDataRetrievalServiceLocal billingRetrievalService) {
        this.sharesRetrievalService = sharesRetrievalService;
        this.billingRetrievalService = billingRetrievalService;
    }

    public BrokerRevenueShareResult build(Long brokerKey, long periodStartTime,
            long periodEndTime) throws DatatypeConfigurationException {
        Invariants.assertNotNull(brokerKey);

        result = new BrokerRevenueShareResult();
        setOrganizationData(brokerKey);
        setBrokerData(brokerKey);
        setPeriod(periodStartTime, periodEndTime);

        List<BillingResult> billingResults = sharesRetrievalService
                .loadBillingResultsForBroker(brokerKey, periodStartTime,
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

    private void setBrokerData(Long brokerKey) {
        OrganizationHistory broker = sharesRetrievalService
                .loadLastOrganizationHistory(brokerKey);
        result.setOrganizationKey(BigInteger.valueOf(brokerKey.longValue()));
        result.setOrganizationId(broker.getOrganizationId());
    }

    void setPeriod(long periodStartTime, long periodEndTime)
            throws DatatypeConfigurationException {
        this.periodEndTime = periodEndTime;

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

        result.setPeriod(period);
    }

    private void addCurrency() {
        String currencyCode = currentBillingResult.getCurrencyCode();
        Currency currency = result.getCurrencyByCode(currencyCode);
        if (currency == null) {
            currency = new Currency(currencyCode);
            result.addCurrency(currency);
        }

        addSupplier(currency);
    }

    private void addSupplier(Currency currency) {
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

    private void addService(Supplier supplier, long subscriptionKey) {
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
                .loadBrokerRevenueSharePercentage(service.getKey().longValue(),
                        periodEndTime);
        Invariants.assertNotNull(revenueSharePercentage);
        service.getServiceRevenue().setBrokerRevenueSharePercentage(
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
                .loadBrokerRevenueSharePercentage(service.getKey().longValue(),
                        periodEndTime);
        Invariants.assertNotNull(revenueSharePercentage);
        serviceCustomerRevenue
                .setBrokerRevenueSharePercentage(revenueSharePercentage);
        serviceCustomerRevenue.setCustomerId(customer.getOrganizationId());
        serviceCustomerRevenue.setCustomerName(customer.getOrganizationName());
        serviceCustomerRevenue.calculate();
        service.getServiceRevenue().addServiceCustomerRevenue(
                serviceCustomerRevenue);
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
