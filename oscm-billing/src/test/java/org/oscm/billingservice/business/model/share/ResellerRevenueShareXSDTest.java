/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.billingservice.business.model.share;

import org.junit.Test;

public class ResellerRevenueShareXSDTest extends RevenueShareXSD {
    protected String getSchemaName() {
        return "ResellerRevenueShareResult";
    }

    @Test
    public void currencyWithoutRevenue() throws Exception {
        validateXml("currencyWithoutRevenue",
                expectedErrorMessage("ResellerRevenuePerSupplier"));
    }

    @Test
    public void serviceWithoutServiceRevenue() throws Exception {
        validateXml("serviceWithoutServiceRevenue",
                expectedErrorMessage("ServiceRevenue"));
    }

    @Test
    public void serviceWithoutSubscription() throws Exception {
        validateXml("serviceWithoutSubscription",
                expectedErrorMessage("Subscription"));
    }

    @Test
    public void supplierWithoutOrganizationData() throws Exception {
        validateXml("supplierWithoutOrganizationData",
                expectedErrorMessage("OrganizationData"));
    }

    @Test
    public void supplierWithoutServices() throws Exception {
        validateXml("supplierWithoutServices", expectedErrorMessage("Service"));
    }

    @Test
    public void supplierWithoutSupplierRevenue() throws Exception {
        validateXml("supplierWithoutSupplierRevenue",
                expectedErrorMessage("ResellerRevenuePerSupplier"));
    }

    @Test
    public void invalidFractionalDigitsForDecimal() throws Exception {
        validateXml("invalidFractionalDigitsForDecimal", "2");
    }

    @Test
    public void invalidSubscriptionWithoutPeriod() throws Exception {
        validateXml("subscriptionWithoutPeriodForReseller",
                expectedErrorMessage("Period"));
    }
}
