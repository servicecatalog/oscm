/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Sep 12, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.reportingservice.business.data.supplierrevenueshare;

import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.oscm.reportingservice.business.model.supplierrevenushare.RDOSupplierRevenueShareCurrency;

/**
 * @author tokoda
 * 
 */
public class RDOSupplierRevenueShareCurrencyFieldTest {
    private RDOSupplierRevenueShareCurrency rdo;

    @Before
    public void setup() {
        rdo = new RDOSupplierRevenueShareCurrency();
    }

    @Test
    public void field_brokerTotalShareAmount() throws Exception {
        assertNotNull(rdo.getClass().getDeclaredField("brokerTotalShareAmount"));
        assertNotNull(rdo.getClass().getMethod("getBrokerTotalShareAmount"));
        assertNotNull(rdo.getClass().getMethod("setBrokerTotalShareAmount",
                String.class));
    }

    @Test
    public void field_resellerTotalShareAmount() throws Exception {
        assertNotNull(rdo.getClass().getDeclaredField(
                "resellerTotalShareAmount"));
        assertNotNull(rdo.getClass().getMethod("getResellerTotalShareAmount"));
        assertNotNull(rdo.getClass().getMethod("setResellerTotalShareAmount",
                String.class));
    }

    @Test
    public void field_totalRemainingRevenue() throws Exception {
        assertNotNull(rdo.getClass().getDeclaredField("totalRemainingRevenue"));
        assertNotNull(rdo.getClass().getMethod("getTotalRemainingRevenue"));
        assertNotNull(rdo.getClass().getMethod("setTotalRemainingRevenue",
                String.class));
    }

    @Test
    public void field_directTotalRemainingRevenue() throws Exception {
        assertNotNull(rdo.getClass().getDeclaredField(
                "directTotalRemainingRevenue"));
        assertNotNull(rdo.getClass()
                .getMethod("getDirectTotalRemainingRevenue"));
        assertNotNull(rdo.getClass().getMethod(
                "setDirectTotalRemainingRevenue", String.class));
    }

    @Test
    public void field_directProvisionToOperator() throws Exception {
        assertNotNull(rdo.getClass().getDeclaredField(
                "directProvisionToOperator"));
        assertNotNull(rdo.getClass().getMethod("getDirectProvisionToOperator"));
        assertNotNull(rdo.getClass().getMethod("setDirectProvisionToOperator",
                String.class));
    }

    @Test
    public void field_brokerProvisionToOperator() throws Exception {
        assertNotNull(rdo.getClass().getDeclaredField(
                "brokerProvisionToOperator"));
        assertNotNull(rdo.getClass().getMethod("getBrokerProvisionToOperator"));
        assertNotNull(rdo.getClass().getMethod("setBrokerProvisionToOperator",
                String.class));
    }

    @Test
    public void field_brokerTotalRemainingRevenue() throws Exception {
        assertNotNull(rdo.getClass().getDeclaredField(
                "brokerTotalRemainingRevenue"));
        assertNotNull(rdo.getClass()
                .getMethod("getBrokerTotalRemainingRevenue"));
        assertNotNull(rdo.getClass().getMethod(
                "setBrokerTotalRemainingRevenue", String.class));
    }

    @Test
    public void field_resellerProvisionToOperator() throws Exception {
        assertNotNull(rdo.getClass().getDeclaredField(
                "resellerProvisionToOperator"));
        assertNotNull(rdo.getClass()
                .getMethod("getResellerProvisionToOperator"));
        assertNotNull(rdo.getClass().getMethod(
                "setResellerProvisionToOperator", String.class));
    }

    @Test
    public void field_resellerTotalRemainingRevenue() throws Exception {
        assertNotNull(rdo.getClass().getDeclaredField(
                "resellerTotalRemainingRevenue"));
        assertNotNull(rdo.getClass().getMethod(
                "getResellerTotalRemainingRevenue"));
        assertNotNull(rdo.getClass().getMethod(
                "setResellerTotalRemainingRevenue", String.class));
    }

    @Test
    public void field_currency() throws Exception {
        assertNotNull(rdo.getClass().getDeclaredField("currency"));
        assertNotNull(rdo.getClass().getMethod("getCurrency"));
        assertNotNull(rdo.getClass().getMethod("setCurrency", String.class));
    }

    @Test
    public void field_totalRevenue() throws Exception {
        assertNotNull(rdo.getClass().getDeclaredField("totalRevenue"));
        assertNotNull(rdo.getClass().getMethod("getTotalRevenue"));
        assertNotNull(rdo.getClass().getMethod("setTotalRevenue", String.class));
    }

    @Test
    public void field_directTotalRevenue() throws Exception {
        assertNotNull(rdo.getClass().getDeclaredField("directTotalRevenue"));
        assertNotNull(rdo.getClass().getMethod("getDirectTotalRevenue"));
        assertNotNull(rdo.getClass().getMethod("setDirectTotalRevenue",
                String.class));
    }

    @Test
    public void field_brokerTotalRevenue() throws Exception {
        assertNotNull(rdo.getClass().getDeclaredField("brokerTotalRevenue"));
        assertNotNull(rdo.getClass().getMethod("getBrokerTotalRevenue"));
        assertNotNull(rdo.getClass().getMethod("setBrokerTotalRevenue",
                String.class));
    }

    @Test
    public void field_brokerPercentageRevenue() throws Exception {
        assertNotNull(rdo.getClass()
                .getDeclaredField("brokerPercentageRevenue"));
        assertNotNull(rdo.getClass().getMethod("getBrokerPercentageRevenue"));
        assertNotNull(rdo.getClass().getMethod("setBrokerPercentageRevenue",
                String.class));
    }

    @Test
    public void field_resellerTotalRevenue() throws Exception {
        assertNotNull(rdo.getClass().getDeclaredField("resellerTotalRevenue"));
        assertNotNull(rdo.getClass().getMethod("getResellerTotalRevenue"));
        assertNotNull(rdo.getClass().getMethod("setResellerTotalRevenue",
                String.class));
    }

    @Test
    public void field_resellerPercentageRevenue() throws Exception {
        assertNotNull(rdo.getClass().getDeclaredField(
                "resellerPercentageRevenue"));
        assertNotNull(rdo.getClass().getMethod("getResellerPercentageRevenue"));
        assertNotNull(rdo.getClass().getMethod("setResellerPercentageRevenue",
                String.class));
    }

    @Test
    public void field_directProvisionToMarketplaceOwner() throws Exception {
        assertNotNull(rdo.getClass().getDeclaredField(
                "directProvisionToMarketplaceOwner"));
        assertNotNull(rdo.getClass().getMethod(
                "getDirectProvisionToMarketplaceOwner"));
        assertNotNull(rdo.getClass().getMethod(
                "setDirectProvisionToMarketplaceOwner", String.class));
    }

    @Test
    public void field_brokerProvisionToMarketplaceOwner() throws Exception {
        assertNotNull(rdo.getClass().getDeclaredField(
                "brokerProvisionToMarketplaceOwner"));
        assertNotNull(rdo.getClass().getMethod(
                "getBrokerProvisionToMarketplaceOwner"));
        assertNotNull(rdo.getClass().getMethod(
                "setBrokerProvisionToMarketplaceOwner", String.class));
    }

    @Test
    public void field_resellerProvisionToMarketplaceOwner() throws Exception {
        assertNotNull(rdo.getClass().getDeclaredField(
                "resellerProvisionToMarketplaceOwner"));
        assertNotNull(rdo.getClass().getMethod(
                "getResellerProvisionToMarketplaceOwner"));
        assertNotNull(rdo.getClass().getMethod(
                "setResellerProvisionToMarketplaceOwner", String.class));
    }

    @Test
    public void field_directRevenueSummaries() throws Exception {
        assertNotNull(rdo.getClass().getDeclaredField("directRevenueSummaries"));
        assertNotNull(rdo.getClass().getMethod("getDirectRevenueSummaries"));
        assertNotNull(rdo.getClass().getMethod("setDirectRevenueSummaries",
                List.class));
    }

    @Test
    public void field_brokerRevenueSummaries() throws Exception {
        assertNotNull(rdo.getClass().getDeclaredField("brokerRevenueSummaries"));
        assertNotNull(rdo.getClass().getMethod("getBrokerRevenueSummaries"));
        assertNotNull(rdo.getClass().getMethod("setBrokerRevenueSummaries",
                List.class));
    }

    @Test
    public void field_resellerRevenueSummaries() throws Exception {
        assertNotNull(rdo.getClass().getDeclaredField(
                "resellerRevenueSummaries"));
        assertNotNull(rdo.getClass().getMethod("getResellerRevenueSummaries"));
        assertNotNull(rdo.getClass().getMethod("setResellerRevenueSummaries",
                List.class));
    }

    @Test
    public void field_directRevenueShareDetails() throws Exception {
        assertNotNull(rdo.getClass().getDeclaredField(
                "directRevenueShareDetails"));
        assertNotNull(rdo.getClass().getMethod("getDirectRevenueShareDetails"));
        assertNotNull(rdo.getClass().getMethod("setDirectRevenueShareDetails",
                List.class));
    }

    @Test
    public void field_brokerRevenueShareDetails() throws Exception {
        assertNotNull(rdo.getClass().getDeclaredField(
                "brokerRevenueShareDetails"));
        assertNotNull(rdo.getClass().getMethod("getBrokerRevenueShareDetails"));
        assertNotNull(rdo.getClass().getMethod("setBrokerRevenueShareDetails",
                List.class));
    }

    @Test
    public void field_resellerRevenueShareDetails() throws Exception {
        assertNotNull(rdo.getClass().getDeclaredField(
                "resellerRevenueShareDetails"));
        assertNotNull(rdo.getClass()
                .getMethod("getResellerRevenueShareDetails"));
        assertNotNull(rdo.getClass().getMethod(
                "setResellerRevenueShareDetails", List.class));
    }
}
