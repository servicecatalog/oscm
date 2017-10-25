/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.billingservice.business.model.mpownershare;

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
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.BillingResult;
import org.oscm.domobjects.MarketplaceHistory;
import org.oscm.domobjects.OrganizationHistory;
import org.oscm.domobjects.OrganizationRole;
import org.oscm.domobjects.ProductHistory;
import org.oscm.validation.Invariants;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ServiceType;

public class MpOwnerShareResultAssembler {

    BillingResult currentBillingResult;
    MarketplaceOwnerRevenueShareResult result;
    SharesDataRetrievalServiceLocal billingRetrievalService;
    XmlSearch xmlSearch;
    DataService ds;
    long periodStartTime;
    long periodEndTime;
    private Long pmKey;

    public MpOwnerShareResultAssembler(
            SharesDataRetrievalServiceLocal billingRetrievalService,
            DataService ds) {

        this.billingRetrievalService = billingRetrievalService;
        this.ds = ds;
    }

    public MarketplaceOwnerRevenueShareResult build(Long mpOwnerKey,
            long periodStartTime, long periodEndTime)
            throws DatatypeConfigurationException {

        Invariants.assertNotNull(mpOwnerKey);
        result = new MarketplaceOwnerRevenueShareResult();
        setMarketpleOwnerData(mpOwnerKey);
        setPeriod(periodStartTime, periodEndTime);

        List<String> currencies = billingRetrievalService
                .loadSupportedCurrencies();
        List<Long> marketplaceKeys = billingRetrievalService
                .loadMarketplaceKeys(mpOwnerKey.longValue(), periodEndTime);
        for (String currencyCode : currencies) {
            for (Long marketplaceKey : marketplaceKeys) {
                List<BillingResult> billingResults = billingRetrievalService
                        .loadBillingResultsForMarketplace(marketplaceKey,
                                currencyCode, Long.valueOf(periodStartTime),
                                Long.valueOf(periodEndTime));
                for (BillingResult br : billingResults) {
                    currentBillingResult = br;
                    xmlSearch = newXmlSearch(currentBillingResult);
                    build(currencyCode, marketplaceKey);
                }
            }
        }
        return result;
    }

    void setMarketpleOwnerData(Long mpOwnerKey) {
        OrganizationHistory mpOwner = billingRetrievalService
                .loadLastOrganizationHistory(mpOwnerKey);
        result.setOrganizationData(buildOrganizationData(mpOwner));
        result.setOrganizationKey(BigInteger.valueOf(mpOwner.getObjKey()));
        result.setOrganizationId(mpOwner.getOrganizationId());
    }

    void setPeriod(long periodStartTime, long periodEndTime)
            throws DatatypeConfigurationException {

        this.periodStartTime = periodStartTime;
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

    XmlSearch newXmlSearch(BillingResult billingResult) {
        return new XmlSearch(billingResult);
    }

    @SuppressWarnings("boxing")
    private void build(String currencyCode, Long marketplaceKey) {
        MarketplaceHistory mp = billingRetrievalService
                .loadMarketplaceHistoryWithinPeriod(marketplaceKey.longValue(),
                        periodEndTime);
        Currency currency = addCurrency(currencyCode);
        Marketplace marketplace = addMarketplace(mp, currency);

        Set<Long> priceModelKeys = xmlSearch.findPriceModelKeys();
        for (Iterator<Long> iterator = priceModelKeys.iterator(); iterator
                .hasNext();) {
            pmKey = iterator.next();
            ProductHistory prd = billingRetrievalService.loadProductOfVendor(
                    currentBillingResult.getSubscriptionKey(), pmKey,
                    periodEndTime);
            Service service = addOrGetService(prd, marketplace);
            updateServiceRevenueShareDetails(service, marketplace);
            addSellersToRevenueSummary(service, marketplace, currency);
        }
    }

    Currency addCurrency(String currencyCode) {
        Currency currency = result.getCurrencyByCode(currencyCode);
        if (currency == null) {
            currency = new Currency(currencyCode);
            result.addCurrency(currency);
        }
        return currency;
    }

    Marketplace addMarketplace(MarketplaceHistory mp, Currency currency) {
        Marketplace marketplace = currency.getMarketplace(mp.getObjKey());
        if (marketplace == null) {
            marketplace = buildMarketplace(mp);
            currency.addMarketplace(marketplace);
        }
        return marketplace;
    }

    private Marketplace buildMarketplace(MarketplaceHistory mp) {
        Marketplace marketplace = new Marketplace();
        marketplace.setKey(BigInteger.valueOf(mp.getObjKey()));
        marketplace.setId(mp.getDataContainer().getMarketplaceId());
        BigDecimal mpRevenuePercentage = loadMarketplaceRevenueSharePercentage(mp
                .getObjKey());
        marketplace.setRevenueSharePercentage(mpRevenuePercentage);
        return marketplace;
    }

    Service addOrGetService(ProductHistory productHistory,
            Marketplace marketplace) {
        Service service = marketplace.getServiceByKey(productHistory
                .getObjKey());
        if (service == null) {
            service = buildService(productHistory);
            marketplace.addService(service);
        }
        return service;
    }

    Service buildService(ProductHistory product) {
        Service service = new Service();
        service.setKey(BigInteger.valueOf(product.getObjKey()));
        service.setId(product.getCleanProductId());
        addSupplierData(service);

        service.getRevenueShareDetails().setOperatorRevenueSharePercentage(
                getOperatorRevenueShareForVendorProduct(product));

        List<OrganizationRole> orgRoles = billingRetrievalService
                .loadOrganizationHistoryRoles(product.getVendorObjKey(),
                        periodEndTime);
        if (hasOrganizationRole(orgRoles, OrganizationRoleType.BROKER)) {
            service.setModel(OfferingType.BROKER);
            service.setTemplateKey(BigInteger.valueOf(product
                    .getTemplateObjKey().longValue()));
            addBrokerData(service, product.getVendorObjKey());
        } else if (hasOrganizationRole(orgRoles, OrganizationRoleType.RESELLER)) {
            service.setModel(OfferingType.RESELLER);
            service.setTemplateKey(BigInteger.valueOf(product
                    .getTemplateObjKey().longValue()));
            addResellerData(service, product.getVendorObjKey());
        } else if (hasOrganizationRole(orgRoles, OrganizationRoleType.SUPPLIER)) {
            service.setModel(OfferingType.DIRECT);
        }

        return service;
    }

    private void addSupplierData(Service service) {
        Supplier supplier = new Supplier();
        OrganizationHistory organization = billingRetrievalService
                .loadSupplierHistoryOfProduct(service.getKey().longValue());

        supplier.setOrganizationData(buildOrganizationData(organization));
        service.setSupplier(supplier);
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

    private void addResellerData(Service service, long vendorKey) {
        Reseller reseller = new Reseller();
        OrganizationHistory organization = loadLastOrganizationHistory(vendorKey);
        reseller.setOrganizationData(buildOrganizationData(organization));
        service.setReseller(reseller);
    }

    private OrganizationHistory loadLastOrganizationHistory(long organizationKey) {
        return billingRetrievalService.loadLastOrganizationHistory(Long
                .valueOf(organizationKey));
    }

    private BigDecimal loadMarketplaceRevenueSharePercentage(long mpKey) {
        return billingRetrievalService.loadMarketplaceRevenueSharePercentage(
                mpKey, periodEndTime);
    }

    OrganizationData buildOrganizationData(OrganizationHistory organization) {
        String countryIsoCode = billingRetrievalService
                .getSupportedCountryCode(Long.valueOf(organization.getObjKey()));
        OrganizationData organizationData = new OrganizationData();
        organizationData.setId(organization.getOrganizationId());
        organizationData.setKey(BigInteger.valueOf(organization.getObjKey()));
        organizationData.setName(organization.getOrganizationName());
        organizationData.setEmail(organization.getEmail());
        organizationData.setAddress(organization.getAddress());
        organizationData.setCountryIsoCode(countryIsoCode);
        return organizationData;
    }

    void updateServiceRevenueShareDetails(Service service,
            Marketplace marketplace) {

        RevenueShareDetails revenueShareDetails = service
                .getRevenueShareDetails();
        revenueShareDetails.setMarketplaceRevenueSharePercentage(marketplace
                .getRevenueSharePercentage());
        if (service.getModel() == OfferingType.BROKER) {
            setBrokerRevenueSharePercentage(service);
        } else if (service.getModel() == OfferingType.RESELLER) {
            setResellerRevenueSharePercentage(service);
        }
        BigDecimal netAmountAfterDiscount = DiscountCalculator
                .calculateServiceRevenue(xmlSearch, pmKey);

        Invariants.assertNotNull(netAmountAfterDiscount);
        BigDecimal serviceRevenue = revenueShareDetails.getServiceRevenue();
        if (serviceRevenue == null) {
            serviceRevenue = BigDecimal.ZERO;
        }
        revenueShareDetails.setServiceRevenue(serviceRevenue
                .add(netAmountAfterDiscount));
        revenueShareDetails.calculate(service.getModel());
    }

    private void setBrokerRevenueSharePercentage(Service service) {
        BigDecimal revenueSharePercentage = billingRetrievalService
                .loadBrokerRevenueSharePercentage(service.getKey().longValue(),
                        periodEndTime);
        service.getRevenueShareDetails().setBrokerRevenueSharePercentage(
                revenueSharePercentage);
    }

    private void setResellerRevenueSharePercentage(Service service) {
        BigDecimal revenueSharePercentage = billingRetrievalService
                .loadResellerRevenueSharePercentage(service.getKey()
                        .longValue(), periodEndTime);
        service.getRevenueShareDetails().setResellerRevenueSharePercentage(
                revenueSharePercentage);
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

    void addSellersToRevenueSummary(Service service, Marketplace marketplace,
            Currency currency) {

        RevenuesPerMarketplace revenuesPerMarketplace = marketplace
                .getRevenuesPerMarketplace();
        RevenuesOverAllMarketplaces revenuesOverAllMarketplaces = currency
                .getRevenuesOverAllMarketplaces();
        createOrganization(service, revenuesPerMarketplace,
                revenuesOverAllMarketplaces);

        switch (service.getModel()) {
        case BROKER: {
            Organization org = buildOrganization(service.getBroker()
                    .getOrganizationData(), service);
            revenuesPerMarketplace.addBroker(org);
            revenuesOverAllMarketplaces.addBroker(org);
            updateSupplierAmount(service, revenuesPerMarketplace,
                    revenuesOverAllMarketplaces);
            break;
        }
        case RESELLER: {
            Organization org = buildOrganization(service.getReseller()
                    .getOrganizationData(), service);
            revenuesPerMarketplace.addReseller(org);
            revenuesOverAllMarketplaces.addReseller(org);
            updateSupplierAmount(service, revenuesPerMarketplace,
                    revenuesOverAllMarketplaces);
            break;
        }
        case DIRECT: {
            Organization org = buildOrganization(service.getSupplier()
                    .getOrganizationData(), service);
            revenuesPerMarketplace.addOrUpdateSupplier(org);
            revenuesOverAllMarketplaces.addOrUpdateSupplier(org);
            break;
        }
        }
    }

    /**
     * Creates an organization if does not yet exists and adds it to the
     * appropriate lists in the 'revenues per marketplace list' and 'revenues
     * over all marketplaces list'.
     */
    private void createOrganization(Service service,
            RevenuesPerMarketplace revenuesPerMarketplace,
            RevenuesOverAllMarketplaces revenuesOverAllMarketplaces) {
        OrganizationData supplierOrg = service.getSupplier()
                .getOrganizationData();
        Organization orga = revenuesPerMarketplace.getSuppliers()
                .getOrganization(supplierOrg.getId());
        if (orga == null) {
            Organization org = buildOrganizationForSupplier(supplierOrg);
            revenuesPerMarketplace.addOrUpdateSupplier(org);
            revenuesOverAllMarketplaces.addOrUpdateSupplier(org);
        }
    }

    private void updateSupplierAmount(Service service,
            RevenuesPerMarketplace revenuePerMarketplace,
            RevenuesOverAllMarketplaces revenuesOverAllMarketplaces) {

        String supplierId = service.getSupplier().getOrganizationData().getId();
        BigDecimal supplierAmount = service.getRevenueShareDetails()
                .getAmountForSupplier();

        BigDecimal amountPerMp = revenuePerMarketplace.getSuppliers()
                .getOrganization(supplierId).getAmount();
        revenuePerMarketplace.getSuppliers().getOrganization(supplierId)
                .setAmount(amountPerMp.add(supplierAmount));
        BigDecimal amountAllMp = revenuesOverAllMarketplaces.getSuppliers()
                .getOrganization(supplierId).getAmount();
        revenuesOverAllMarketplaces.getSuppliers().getOrganization(supplierId)
                .setAmount(amountAllMp.add(supplierAmount));
    }

    private Organization buildOrganization(OrganizationData orgData,
            Service service) {

        RevenueShareDetails shareDetails = service.getRevenueShareDetails();
        RevenueShareDetails tmp = new RevenueShareDetails();
        tmp.setMarketplaceRevenueSharePercentage(shareDetails
                .getMarketplaceRevenueSharePercentage());
        tmp.setOperatorRevenueSharePercentage(shareDetails
                .getOperatorRevenueSharePercentage());
        tmp.setServiceRevenue(DiscountCalculator.calculateServiceRevenue(
                xmlSearch, pmKey));
        tmp.setResellerRevenueSharePercentage(shareDetails
                .getResellerRevenueSharePercentage());
        tmp.setBrokerRevenueSharePercentage(shareDetails
                .getBrokerRevenueSharePercentage());
        tmp.calculate(service.getModel());

        Organization org = new Organization();
        org.setIdentifier(orgData.getId());
        org.setName(orgData.getName());
        org.setMarketplaceRevenue(tmp.getMarketplaceRevenue());
        org.setTotalAmount(tmp.getServiceRevenue());

        switch (service.getModel()) {
        case BROKER: {
            org.setAmount(tmp.getBrokerRevenue());
            break;
        }
        case RESELLER: {
            org.setAmount(tmp.getResellerRevenue());
            break;
        }
        case DIRECT:
            org.setAmount(tmp.getAmountForSupplier());
            break;
        }

        return org;
    }

    private Organization buildOrganizationForSupplier(OrganizationData orgData) {
        Organization org = new Organization();
        org.setIdentifier(orgData.getId());
        org.setName(orgData.getName());
        org.setTotalAmount(BigDecimal.ZERO);
        org.setAmount(BigDecimal.ZERO);
        org.setMarketplaceRevenue(BigDecimal.ZERO);
        return org;
    }
}
