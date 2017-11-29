/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.billingservice.business.model.share;

import org.junit.Test;

public class SupplierRevenueShareXSDTest extends RevenueShareXSD {
    protected String getSchemaName() {
        return "SupplierRevenueShareResult";
    }

    @Test
    public void currencyWithoutMarketplace() throws Exception {
        validateXml("currencyWithoutMarketplace",
                expectedErrorMessage("Marketplace"));
    }

    @Test
    public void marketplaceWithoutMarketplaceOwner() throws Exception {
        validateXml("marketplaceWithoutMarketplaceOwner",
                expectedErrorMessage("MarketplaceOwner"));
    }

    @Test
    public void marketplaceWithoutOrganizationData() throws Exception {
        validateXml("marketplaceWithoutOrganizationData",
                expectedErrorMessage("OrganizationData"));
    }

    @Test
    public void marketplaceWithoutServices() throws Exception {
        validateXml("marketplaceWithoutServices",
                expectedErrorMessage("Service"));
    }

    @Test
    public void marketplaceWithoutMarketplaceRevenue() throws Exception {
        validateXml("marketplaceWithoutMarketplaceRevenue",
                expectedErrorMessage("RevenuePerMarketplace"));
    }

    @Test
    public void serviceWithoutSubscription() throws Exception {
        validateXml("serviceWithoutSubscription", null);
    }

    @Test
    public void serviceWithoutRevenueShareDetails() throws Exception {
        validateXml("serviceWithoutRevenueShareDetails",
                expectedErrorMessage("RevenueShareDetails"));
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
    public void serviceWithoutReseller() throws Exception {
        validateXml("serviceWithoutReseller", null);
    }

    @Test
    public void serviceWithoutSubscriptionsRevenue() throws Exception {
        validateXml("serviceWithoutSubscriptionsRevenue", null);
    }

    @Test
    public void resellerWithoutOrganizationData() throws Exception {
        validateXml("resellerWithoutOrganizationData",
                expectedErrorMessage("OrganizationData"));
    }

    @Test
    public void invalidFractionalDigitsForDecimal() throws Exception {
        validateXml("invalidFractionalDigitsForDecimal", "2");
    }

    @Test
    public void invalidSubscriptionWithoutPeriod() throws Exception {
        validateXml("subscriptionWithoutPeriodForSupplier",
                expectedErrorMessage("Period"));
    }
    
    @Test
    public void customerWithoutName() throws Exception {
        validateXml("customerWithoutName",
                "customerName");
    }
    

    @Test
    public void customerWithoutId() throws Exception {
        validateXml("customerWithoutId",
                "customerId");
    }

}
