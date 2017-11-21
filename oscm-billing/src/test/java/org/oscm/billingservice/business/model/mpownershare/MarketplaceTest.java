/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Sep 03, 2012                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.billingservice.business.model.mpownershare;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.math.BigDecimal;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.oscm.converter.PriceConverter;

public class MarketplaceTest {

    private Marketplace marketplace;
    private List<Service> services;
    private Marketplace newMarketplace;

    @Before
    public void setup() {
        services = Util.createServices(1);
        marketplace = Util.createMarketplace(1);
        newMarketplace = Util.createNewMarketplace(1);
    }

    @Test
    public void calculate_noServices() {
        // given see setup

        // when
        marketplace.calculate();

        // then no exception
    }

    @Test
    public void calculate_withServices() {
        // given see setup
        for (Service service : services) {
            newMarketplace.addService(service);
            addSellersToSummary(newMarketplace, service);
        }

        // when
        newMarketplace.calculate();

        // then

        // amount broker revenue for this marketplace
        List<Organization> mpOrgBroker = newMarketplace
                .getRevenuesPerMarketplace().getBrokers().getOrganization();
        assertNotNull(mpOrgBroker);
        assertEquals(1, mpOrgBroker.size());
        assertEquals(Util.createBigDecimal(25), mpOrgBroker.get(0).getAmount());

        // amount reseller revenue for this marketplace
        List<Organization> mpOrgReseller = newMarketplace
                .getRevenuesPerMarketplace().getResellers().getOrganization();
        assertNotNull(mpOrgReseller);
        assertEquals(1, mpOrgReseller.size());
        assertEquals(Util.createBigDecimal(25), mpOrgReseller.get(0)
                .getAmount());

        // amount supplier revenue for this marketplace
        List<Organization> mpOrgSupplier = newMarketplace
                .getRevenuesPerMarketplace().getSuppliers().getOrganization();
        assertNotNull(mpOrgSupplier);
        assertEquals(1, mpOrgSupplier.size());
        assertEquals(Util.createBigDecimal(1225), mpOrgSupplier.get(0)
                .getAmount());

        // amount marketplace owner revenue for this marketplace
        MarketplaceOwner marketplaceOwner = newMarketplace
                .getRevenuesPerMarketplace().getMarketplaceOwner();
        assertNotNull(marketplaceOwner);
        assertEquals(Util.createBigDecimal(225), marketplaceOwner.getAmount());
    }

    private void addSellersToSummary(Marketplace newMarketplace, Service service) {

        RevenuesPerMarketplace revenuePerMarketplace = newMarketplace
                .getRevenuesPerMarketplace();
        String orgId = service.getSupplier().getOrganizationData().getId();

        Organization orga = revenuePerMarketplace.getSuppliers()
                .getOrganization(orgId);
        if (orga == null) {
            Organization org = buildOrganizationForSupplier(orgId, service);
            revenuePerMarketplace.addOrUpdateSupplier(org);
        }

        switch (service.getModel()) {
        case BROKER: {
            String organizationId = service.getBroker().getOrganizationData()
                    .getId();
            Organization org = buildOrganization(organizationId, service);
            revenuePerMarketplace.addBroker(org);
            updateSupplierAmount(service, revenuePerMarketplace);
            break;
        }

        case RESELLER: {
            String organizationId = service.getReseller().getOrganizationData()
                    .getId();
            Organization org = buildOrganization(organizationId, service);
            revenuePerMarketplace.addReseller(org);
            updateSupplierAmount(service, revenuePerMarketplace);
            break;
        }
        case DIRECT: {
            String organizationId = service.getSupplier().getOrganizationData()
                    .getId();
            Organization org = buildOrganization(organizationId, service);
            revenuePerMarketplace.addOrUpdateSupplier(org);
            break;
        }
        }
    }

    private void updateSupplierAmount(Service service,
            RevenuesPerMarketplace revenuePerMarketplace) {

        String supplierId = service.getSupplier().getOrganizationData().getId();

        BigDecimal supplierAmount = service.getRevenueShareDetails()
                .getAmountForSupplier();

        BigDecimal amountPerMp = revenuePerMarketplace.getSuppliers()
                .getOrganization(supplierId).getAmount();
        revenuePerMarketplace.getSuppliers().getOrganization(supplierId)
                .setAmount(amountPerMp.add(supplierAmount));
    }

    private Organization buildOrganization(String orgId, Service service) {
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
        case DIRECT:
            org.setAmount(service.getRevenueShareDetails()
                    .getAmountForSupplier());
            break;
        }
        return org;
    }

    private Organization buildOrganizationForSupplier(String orgId,
            Service service) {
        Organization org = new Organization();
        org.setIdentifier(orgId);
        service.getRevenueShareDetails().calculate(service.getModel());
        org.setTotalAmount(BigDecimal.ZERO);
        org.setAmount(BigDecimal.ZERO);
        org.setMarketplaceRevenue(BigDecimal.ZERO);
        return org;
    }

    @Test
    public void calculate_marketplaceRevenueSharePercentage33() {
        // given
        Marketplace marketplaceX = Util.createNewMarketplace(1,
                BigDecimal.valueOf(33.3333));// marketplaceRevenueSharePercentage
        List<Service> servicesX = Util.createServices(1,
                BigDecimal.valueOf(100.000000),// serviceRevenue broker-service;
                                               // 500 for the other
                                               // services
                BigDecimal.valueOf(33.3333), // marketplaceRevenueSharePercentage
                // for the broker-service; 15% for the
                // other services
                BigDecimal.valueOf(33.3333));// brokerRevenueSharePercentage; 5%
        // for the other services
        for (Service service : servicesX) {
            marketplaceX.addService(service);
            addSellersToSummary(marketplaceX, service);
        }

        // when
        marketplaceX.calculate();

        // then
        BigDecimal total = new BigDecimal(0)
                .setScale(PriceConverter.NORMALIZED_PRICE_SCALING);
        // amount broker revenue for this marketplace
        List<Organization> mpOrgBroker = marketplaceX
                .getRevenuesPerMarketplace().getBrokers().getOrganization();
        assertNotNull(mpOrgBroker);
        assertEquals(1, mpOrgBroker.size());
        assertEquals(Util.createBigDecimal(33.33), // 33% from 100
                mpOrgBroker.get(0).getAmount());
        total = total.add(mpOrgBroker.get(0).getAmount());

        // amount reseller revenue for this marketplace
        List<Organization> mpOrgReseller = marketplaceX
                .getRevenuesPerMarketplace().getResellers().getOrganization();
        assertNotNull(mpOrgReseller);
        assertEquals(1, mpOrgReseller.size());
        assertEquals(Util.createBigDecimal(25), // 5% from 500
                mpOrgReseller.get(0).getAmount());
        total = total.add(mpOrgReseller.get(0).getAmount());

        // amount supplier revenue for this marketplace
        List<Organization> mpOrgSupplier = marketplaceX
                .getRevenuesPerMarketplace().getSuppliers().getOrganization();
        assertNotNull(mpOrgSupplier);
        assertEquals(1, mpOrgSupplier.size());
        // broker service: 33,33% from 100 for broker, 33,33% from 100 for mpo
        // = 100-33,33-33,33 = 33,4
        // reseller service: 5% from 500 for reseller, 15% from 500 for mpo
        // = 500-25-75 = 400
        // supplier service: 15% from 500 for mpo
        // = 500-75 = 425
        assertEquals(Util.createBigDecimal(858.34), mpOrgSupplier.get(0)
                .getAmount());
        total = total.add(mpOrgSupplier.get(0).getAmount());

        // amount marketplace owner revenue for this marketplace
        MarketplaceOwner marketplaceOwner = marketplaceX
                .getRevenuesPerMarketplace().getMarketplaceOwner();
        assertNotNull(marketplaceOwner);
        // broker service: 33,33% from 100= 33,33
        // reseller service: 15% from 500 = 75,00
        // supplier service: 15% from 500 = 75,00
        assertEquals(Util.createBigDecimal(183.33),
                marketplaceOwner.getAmount());
        total = total.add(marketplaceOwner.getAmount());
        // 500+500+100
        assertEquals(Util.createBigDecimal(1100), total);
    }
}
