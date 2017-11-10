/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Sep 12, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.reportingservice.business.data.supplierrevenueshare;

import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

import org.oscm.reportingservice.business.model.supplierrevenushare.RDORevenueShareDetail;

/**
 * Changes to fields (rename/delete) within report data objects must also be
 * reflected in the report template file.
 * <p>
 * The purpose of this test is to remind the developer to adapt report template
 * files.
 * 
 * @author tokoda
 * 
 */
public class RDORevenueShareDetailFieldTest {
    private RDORevenueShareDetail rdo;

    @Before
    public void setup() {
        rdo = new RDORevenueShareDetail();
    }

    @Test
    public void field_service() throws Exception {
        assertNotNull(rdo.getClass().getDeclaredField("service"));
        assertNotNull(rdo.getClass().getMethod("getService"));
        assertNotNull(rdo.getClass().getMethod("setService", String.class));
    }

    @Test
    public void field_marketplace() throws Exception {
        assertNotNull(rdo.getClass().getDeclaredField("marketplace"));
        assertNotNull(rdo.getClass().getMethod("getMarketplace"));
        assertNotNull(rdo.getClass().getMethod("setMarketplace", String.class));
    }

    @Test
    public void field_customer() throws Exception {
        assertNotNull(rdo.getClass().getDeclaredField("customer"));
        assertNotNull(rdo.getClass().getMethod("getCustomer"));
        assertNotNull(rdo.getClass().getMethod("setCustomer", String.class));
    }

    @Test
    public void field_revenue() throws Exception {
        assertNotNull(rdo.getClass().getDeclaredField("revenue"));
        assertNotNull(rdo.getClass().getMethod("getRevenue"));
        assertNotNull(rdo.getClass().getMethod("setRevenue", String.class));
    }

    @Test
    public void field_currency() throws Exception {
        assertNotNull(rdo.getClass().getDeclaredField("currency"));
        assertNotNull(rdo.getClass().getMethod("getCurrency"));
        assertNotNull(rdo.getClass().getMethod("setCurrency", String.class));
    }

    @Test
    public void field_partner() throws Exception {
        assertNotNull(rdo.getClass().getDeclaredField("partner"));
        assertNotNull(rdo.getClass().getMethod("getPartner"));
        assertNotNull(rdo.getClass().getMethod("setPartner", String.class));
    }

    @Test
    public void field_partnerRevenue() throws Exception {
        assertNotNull(rdo.getClass().getDeclaredField("partnerRevenue"));
        assertNotNull(rdo.getClass().getMethod("getPartnerRevenue"));
        assertNotNull(rdo.getClass().getMethod("setPartnerRevenue",
                String.class));
    }

    @Test
    public void field_partnerSharePercentage() throws Exception {
        assertNotNull(rdo.getClass().getDeclaredField("partnerSharePercentage"));
        assertNotNull(rdo.getClass().getMethod("getPartnerSharePercentage"));
        assertNotNull(rdo.getClass().getMethod("setPartnerSharePercentage",
                String.class));
    }

    @Test
    public void field_marketplaceRevenue() throws Exception {
        assertNotNull(rdo.getClass().getDeclaredField("marketplaceRevenue"));
        assertNotNull(rdo.getClass().getMethod("getMarketplaceRevenue"));
        assertNotNull(rdo.getClass().getMethod("setMarketplaceRevenue",
                String.class));
    }

    @Test
    public void field_marketplaceSharePercentage() throws Exception {
        assertNotNull(rdo.getClass().getDeclaredField(
                "marketplaceSharePercentage"));
        assertNotNull(rdo.getClass().getMethod("getMarketplaceSharePercentage"));
        assertNotNull(rdo.getClass().getMethod("setMarketplaceSharePercentage",
                String.class));
    }

    @Test
    public void field_operatorRevenuePercentage() throws Exception {
        assertNotNull(rdo.getClass().getDeclaredField(
                "operatorRevenuePercentage"));
        assertNotNull(rdo.getClass().getMethod("getOperatorRevenuePercentage"));
        assertNotNull(rdo.getClass().getMethod("setOperatorRevenuePercentage",
                String.class));
    }

    @Test
    public void field_operatorRevenue() throws Exception {
        assertNotNull(rdo.getClass().getDeclaredField("operatorRevenue"));
        assertNotNull(rdo.getClass().getMethod("getOperatorRevenue"));
        assertNotNull(rdo.getClass().getMethod("setOperatorRevenue",
                String.class));
    }

    @Test
    public void field_revenueMinusShares() throws Exception {
        assertNotNull(rdo.getClass().getDeclaredField("revenueMinusShares"));
        assertNotNull(rdo.getClass().getMethod("getRevenueMinusShares"));
        assertNotNull(rdo.getClass().getMethod("setRevenueMinusShares",
                String.class));
    }

}
