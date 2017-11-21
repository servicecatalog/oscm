/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.billingservice.business.model.share;

import org.junit.Test;

public class BrokerRevenueShareXSDTest extends RevenueShareXSD {
    protected String getSchemaName() {
        return "BrokerRevenueShareResult";
    }

    @Test
    public void currencyWithoutRevenue() throws Exception {
        validateXml("currencyWithoutRevenue",
                expectedErrorMessage("BrokerRevenue"));
    }

    @Test
    public void currencyWithoutSupplier() throws Exception {
        validateXml("currencyWithoutSupplier", expectedErrorMessage("Supplier"));
    }

    @Test
    public void invalidFractionalDigitsForDecimal() throws Exception {
        validateXml("invalidFractionalDigitsForDecimal",
                "2");
    }

    @Test
    public void supplierWithoutServices() throws Exception {
        validateXml("supplierWithoutServices", expectedErrorMessage("Service"));
    }

    @Test
    public void supplierWithoutSupplierRevenue() throws Exception {
        validateXml("supplierWithoutSupplierRevenue",
                expectedErrorMessage("BrokerRevenuePerSupplier"));
    }

    @Test
    public void supplierWithoutOrganizationData() throws Exception {
        validateXml("supplierWithoutOrganizationData",
                expectedErrorMessage("OrganizationData"));
    }
}
