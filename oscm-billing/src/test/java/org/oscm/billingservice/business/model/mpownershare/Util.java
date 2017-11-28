/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Sep 05, 2012                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.billingservice.business.model.mpownershare;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.oscm.converter.PriceConverter;

public class Util {

    public static BigDecimal createBigDecimal(double value) {
        if (value == 0) {
            return BigDecimal.ZERO
                    .setScale(PriceConverter.NORMALIZED_PRICE_SCALING);
        } else {
            return new BigDecimal(value).setScale(
                    PriceConverter.NORMALIZED_PRICE_SCALING,
                    PriceConverter.ROUNDING_MODE);
        }
    }

    public static String getBrokerId(int index) {
        return "broker_" + index;
    }

    public static String getResellerId(int index) {
        return "reseller_" + index;
    }

    public static String getSupplierId(int index) {
        return "supplier_" + index;
    }

    public static Organization createBrokerOrganization(int index) {
        Organization org = new Organization();
        org.setIdentifier(getBrokerId(index));
        org.setAmount(createBigDecimal(0));
        return org;
    }

    public static Organization createResellerOrganization(int index) {
        Organization org = new Organization();
        org.setIdentifier(getResellerId(index));
        org.setAmount(createBigDecimal(0));
        return org;
    }

    public static Organization createSupplierOrganization(int index) {
        Organization org = new Organization();
        org.setIdentifier(getSupplierId(index));
        org.setAmount(createBigDecimal(0));
        return org;
    }

    public static Broker createBroker(int index) {
        Broker broker = new Broker();
        OrganizationData organizationData = new OrganizationData();
        organizationData.setId(getBrokerId(index));
        broker.setOrganizationData(organizationData);
        return broker;
    }

    public static Reseller createReseller(int index) {
        Reseller reseller = new Reseller();
        OrganizationData organizationData = new OrganizationData();
        organizationData.setId(getResellerId(index));
        reseller.setOrganizationData(organizationData);
        return reseller;
    }

    public static Supplier createSupplier(int index) {
        Supplier supplier = new Supplier();
        OrganizationData organizationData = new OrganizationData();
        organizationData.setId(getSupplierId(index));
        supplier.setOrganizationData(organizationData);
        return supplier;
    }

    public static Service createServiceDirect(int index) {
        Service service = new Service();
        service.setModel(OfferingType.DIRECT);
        service.setSupplier(createSupplier(1));
        service.setId("serviceDirect" + index);

        RevenueShareDetails revenueShareDetails = service
                .getRevenueShareDetails();
        revenueShareDetails.setServiceRevenue(createBigDecimal(500));
        revenueShareDetails
                .setMarketplaceRevenueSharePercentage(createBigDecimal(15));
        revenueShareDetails.setOperatorRevenueSharePercentage(Util
                .createBigDecimal(0));
        return service;
    }

    public static Service createServiceReseller(int index) {
        Service service = new Service();
        service.setModel(OfferingType.RESELLER);
        service.setSupplier(createSupplier(1));
        service.setReseller(createReseller(1));
        service.setId("serviceReseller" + index);

        RevenueShareDetails revenueShareDetails = service
                .getRevenueShareDetails();
        revenueShareDetails.setServiceRevenue(createBigDecimal(500));
        revenueShareDetails
                .setMarketplaceRevenueSharePercentage(createBigDecimal(15));
        revenueShareDetails.setOperatorRevenueSharePercentage(Util
                .createBigDecimal(0));
        revenueShareDetails
                .setResellerRevenueSharePercentage(createBigDecimal(5));
        return service;
    }

    public static Service createServiceBroker(int index) {
        return createServiceBroker(index, createBigDecimal(500),
                createBigDecimal(15), createBigDecimal(5));
    }

    public static Service createServiceBroker(int index,
            BigDecimal serviceRevenue,
            BigDecimal marketplaceRevenueSharePercentage,
            BigDecimal brokerRevenueSharePercentage) {
        Service service = new Service();
        service.setModel(OfferingType.BROKER);
        service.setSupplier(createSupplier(1));
        service.setBroker(createBroker(1));
        service.setId("serviceBroker" + index);

        RevenueShareDetails revenueShareDetails = service
                .getRevenueShareDetails();
        revenueShareDetails.setServiceRevenue(serviceRevenue);
        revenueShareDetails
                .setMarketplaceRevenueSharePercentage(marketplaceRevenueSharePercentage);
        revenueShareDetails.setOperatorRevenueSharePercentage(Util
                .createBigDecimal(0));
        revenueShareDetails
                .setBrokerRevenueSharePercentage(brokerRevenueSharePercentage);
        return service;
    }

    public static RevenuesPerMarketplace createRevenuesPerMarketplace() {
        RevenuesPerMarketplace revenuesPerMarketplace = new RevenuesPerMarketplace();
        return revenuesPerMarketplace;
    }

    public static List<Service> createServices(int index) {
        return createServices(index, createBigDecimal(500),
                createBigDecimal(15), createBigDecimal(5));
    }

    public static List<Service> createServices(int index,
            BigDecimal serviceRevenue,
            BigDecimal marketplaceRevenueSharePercentage,
            BigDecimal brokerRevenueSharePercentage) {
        List<Service> services = new ArrayList<Service>();
        services.add(createServiceDirect(index));
        services.add(createServiceReseller(index));
        services.add(createServiceBroker(index, serviceRevenue,
                marketplaceRevenueSharePercentage, brokerRevenueSharePercentage));
        return services;
    }

    public static Marketplace createMarketplace(int index) {
        return createMarketplace(index, createBigDecimal(15));
    }

    public static Marketplace createNewMarketplace(int index) {
        return createNewMarketplace(index, createBigDecimal(15));
    }

    public static Marketplace createMarketplace(int index,
            BigDecimal marketplaceRevenueSharePercentage) {
        Marketplace marketplace = new Marketplace();
        marketplace.setId("marketplaceId_" + index);
        marketplace
                .setRevenueSharePercentage(marketplaceRevenueSharePercentage);
        marketplace.setRevenuesPerMarketplace(createRevenuesPerMarketplace());
        return marketplace;
    }

    public static Marketplace createNewMarketplace(int index,
            BigDecimal marketplaceRevenueSharePercentage) {
        Marketplace marketplace = new Marketplace();
        marketplace.setId("marketplaceId_" + index);
        marketplace
                .setRevenueSharePercentage(marketplaceRevenueSharePercentage);
        marketplace.setRevenuesPerMarketplace(createRevenuesPerMarketplace());
        return marketplace;
    }

    public static Marketplace setupMarketplace(Currency currency, int index) {
        List<Service> services = createServices(index);
        Marketplace marketplace = createNewMarketplace(index);
        for (Service service : services) {
            marketplace.addService(service);
            addSellersToSummary(service, marketplace, currency);
        }
        return marketplace;
    }

    private static void addSellersToSummary(Service service,
            Marketplace marketplace, Currency currency) {

        RevenuesPerMarketplace revenuePerMarketplace = marketplace
                .getRevenuesPerMarketplace();
        RevenuesOverAllMarketplaces revenuesOverAllMarketplaces = currency
                .getRevenuesOverAllMarketplaces();

        String orgId = service.getSupplier().getOrganizationData().getId();

        Organization orga = revenuePerMarketplace.getSuppliers()
                .getOrganization(orgId);
        if (orga == null) {
            Organization org = buildOrganizationForSupplier(orgId, service);
            revenuePerMarketplace.addOrUpdateSupplier(org);
            revenuesOverAllMarketplaces.addOrUpdateSupplier(org);
        }

        switch (service.getModel()) {
        case BROKER: {
            OrganizationData orgData = service.getBroker()
                    .getOrganizationData();
            String organizationId = service.getBroker().getOrganizationData()
                    .getId();
            Organization org = buildOrganization(organizationId, service);
            org.setName(orgData.getName());
            revenuePerMarketplace.addBroker(org);
            revenuesOverAllMarketplaces.addBroker(org);
            updateSupplierAmount(service, revenuePerMarketplace,
                    revenuesOverAllMarketplaces);
            break;
        }

        case RESELLER: {
            String organizationId = service.getReseller().getOrganizationData()
                    .getId();
            Organization org = buildOrganization(organizationId, service);
            revenuePerMarketplace.addReseller(org);
            revenuesOverAllMarketplaces.addReseller(org);
            updateSupplierAmount(service, revenuePerMarketplace,
                    revenuesOverAllMarketplaces);
            break;
        }
        case DIRECT: {
            String organizationId = service.getSupplier().getOrganizationData()
                    .getId();
            Organization org = buildOrganization(organizationId, service);
            revenuePerMarketplace.addOrUpdateSupplier(org);
            revenuesOverAllMarketplaces.addOrUpdateSupplier(org);
            break;
        }
        }
    }

    private static void updateSupplierAmount(Service service,
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

    private static Organization buildOrganization(String orgId, Service service) {
        Organization org = new Organization();
        org.setIdentifier(orgId);
        service.getRevenueShareDetails().calculate(service.getModel());
        org.setMarketplaceRevenue(service.getRevenueShareDetails()
                .getMarketplaceRevenue());
        org.setTotalAmount(service.getRevenueShareDetails().getServiceRevenue());
        switch (service.getModel()) {
        case BROKER: {
            org.setAmount(service.getRevenueShareDetails().getBrokerRevenue());
            break;
        }
        case RESELLER: {
            org.setAmount(service.getRevenueShareDetails().getResellerRevenue());
            break;
        }
        default:
            org.setAmount(service.getRevenueShareDetails()
                    .getAmountForSupplier());
            break;
        }
        return org;
    }

    private static Organization buildOrganizationForSupplier(String orgId,
            Service service) {
        Organization org = new Organization();
        org.setIdentifier(orgId);
        service.getRevenueShareDetails().calculate(service.getModel());
        org.setMarketplaceRevenue(service.getRevenueShareDetails()
                .getMarketplaceRevenue());
        org.setTotalAmount(service.getRevenueShareDetails().getServiceRevenue());
        org.setAmount(service.getRevenueShareDetails().getAmountForSupplier());
        return org;
    }

    public static RevenuesOverAllMarketplaces createRevenuesOverAllMarketplaces() {
        RevenuesOverAllMarketplaces revenuesOverAllMarketplaces = new RevenuesOverAllMarketplaces();
        return revenuesOverAllMarketplaces;
    }

    public static Currency createCurrency() {
        Currency currency = new Currency();
        currency.setId("currency_1");
        currency.setRevenuesOverAllMarketplaces(createRevenuesOverAllMarketplaces());
        return currency;
    }

}
