/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.billingservice.business.model.share;

import org.junit.Test;

public class MPOwnerRevenueShareXSDTest extends RevenueShareXSD {
    protected String getSchemaName() {
        return "MPOwnerRevenueShareResult";
    }

    @Test
    public void currencyWithoutMarketplace() throws Exception {
        validateXml("currencyWithoutMarketplace",
                expectedErrorMessage("Marketplace"));
    }

    @Test
    public void marketplaceWithoutServices() throws Exception {
        validateXml("marketplaceWithoutServices",
                expectedErrorMessage("Service"));
    }

    @Test
    public void serviceWithoutSupplier() throws Exception {
        validateXml("serviceWithoutSupplier", expectedErrorMessage("Supplier"));
    }

    @Test
    public void supplierWithoutOrganizationData() throws Exception {
        validateXml("supplierWithoutOrganizationData",
                expectedErrorMessage("OrganizationData"));
    }

    @Test
    public void serviceWithoutRevenueShareDetails() throws Exception {
        validateXml("serviceWithoutRevenueShareDetails",
                expectedErrorMessage("RevenueShareDetails"));
    }

    @Test
    public void serviceWithoutReseller() throws Exception {
        validateXml("serviceWithoutReseller", null);
    }

    @Test
    public void resellerWithoutOrganizationData() throws Exception {
        validateXml("resellerWithoutOrganizationData",
                expectedErrorMessage("OrganizationData"));
    }

    @Test
    public void serviceWithoutBroker() throws Exception {
        validateXml("serviceWithoutBroker", null);
    }

    @Test
    public void brokerWithoutOrganizationData() throws Exception {
        validateXml("brokerWithoutOrganizationData",
                expectedErrorMessage("OrganizationData"));
    }

    @Test
    public void marketplaceWithoutRevenuesPerMarketplace() throws Exception {
        validateXml("marketplaceWithoutRevenuesPerMarketplace",
                expectedErrorMessage("RevenuesPerMarketplace"));
    }

    @Test
    public void marketplaceWithoutBroker() throws Exception {
        validateXml("marketplaceWithoutBroker", expectedErrorMessage("Brokers"));
    }

    @Test
    public void marketplaceBrokerWithoutOrganization() throws Exception {
        validateXml("marketplaceBrokerWithoutOrganization",
                expectedErrorMessage("Organization"));
    }

    @Test
    public void marketplaceWithoutReseller() throws Exception {
        validateXml("marketplaceWithoutReseller",
                expectedErrorMessage("Resellers"));
    }

    @Test
    public void marketplaceResellerWithoutOrganization() throws Exception {
        validateXml("marketplaceResellerWithoutOrganization",
                expectedErrorMessage("Organization"));
    }

    @Test
    public void marketplaceWithoutSupplier() throws Exception {
        validateXml("marketplaceWithoutSupplier",
                expectedErrorMessage("Suppliers"));
    }

    @Test
    public void marketplaceSupplierWithoutOrganization() throws Exception {
        validateXml("marketplaceSupplierWithoutOrganization",
                expectedErrorMessage("Organization"));
    }

    @Test
    public void marketplaceWithoutMarketplaceOwner() throws Exception {
        validateXml("marketplaceBalanceWithoutMarketplaceOwner",
                expectedErrorMessage("MarketplaceOwner"));
    }

    @Test
    public void currencyWithoutMarketplacesRevenue() throws Exception {
        validateXml("currencyWithoutMarketplacesRevenue",
                expectedErrorMessage("RevenuesOverAllMarketplaces"));
    }

    @Test
    public void marketplacesRevenueWithoutBroker() throws Exception {
        validateXml("marketplacesRevenueWithoutBroker",
                expectedErrorMessage("Brokers"));
    }

    @Test
    public void marketplacesRevenueBrokerWithoutOrganization() throws Exception {
        validateXml("marketplacesRevenueBrokerWithoutOrganization",
                expectedErrorMessage("Organization"));
    }

    @Test
    public void marketplacesRevenueWithoutReseller() throws Exception {
        validateXml("marketplacesRevenueWithoutReseller",
                expectedErrorMessage("Resellers"));
    }

    @Test
    public void marketplacesRevenueResellerWithoutOrganization()
            throws Exception {
        validateXml("marketplacesRevenueResellerWithoutOrganization",
                expectedErrorMessage("Organization"));
    }

    @Test
    public void marketplacesRevenueWithoutSupplier() throws Exception {
        validateXml("marketplacesRevenueWithoutSupplier",
                expectedErrorMessage("Suppliers"));
    }

    @Test
    public void marketplacesRevenueSupplierWithoutOrganization()
            throws Exception {
        validateXml("marketplacesRevenueSupplierWithoutOrganization",
                expectedErrorMessage("Organization"));
    }

    @Test
    public void marketplacesRevenueWithoutMarketplaceOwner() throws Exception {
        validateXml("marketplacesRevenueWithoutMarketplaceOwner",
                expectedErrorMessage("MarketplaceOwner"));
    }
}
