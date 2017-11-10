/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Sep 7, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.reportingservice.business.data.partnerrevenue;

import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

import org.oscm.reportingservice.business.model.partnerrevenue.RDORevenueDetailService;

/**
 * Checks if the RDOHeader has the field xxx which is used by the report
 * template. If you rename the field in the RDO you have to adapt the report
 * template as well!
 * 
 * @author kulle
 */
public class RDORevenueDetailServiceFieldTest {

    private RDORevenueDetailService rdo;

    @Before
    public void setup() {
        rdo = new RDORevenueDetailService();
    }

    @Test
    public void field_revenue() throws Exception {
        assertNotNull(rdo.getClass().getDeclaredField("revenue"));
        assertNotNull(rdo.getClass().getMethod("getRevenue"));
        assertNotNull(rdo.getClass().getMethod("setRevenue", String.class));
    }

    @Test
    public void field_service() throws Exception {
        assertNotNull(rdo.getClass().getDeclaredField("service"));
        assertNotNull(rdo.getClass().getMethod("getService"));
        assertNotNull(rdo.getClass().getMethod("setService", String.class));
    }

    @Test
    public void field_revenueShare() throws Exception {
        assertNotNull(rdo.getClass().getDeclaredField("revenueShare"));
        assertNotNull(rdo.getClass().getMethod("getRevenueShare"));
        assertNotNull(rdo.getClass().getMethod("setRevenueShare", String.class));
    }

    @Test
    public void field_currency() throws Exception {
        assertNotNull(rdo.getClass().getDeclaredField("currency"));
        assertNotNull(rdo.getClass().getMethod("getCurrency"));
        assertNotNull(rdo.getClass().getMethod("setCurrency", String.class));
    }

    @Test
    public void field_vendor() throws Exception {
        assertNotNull(rdo.getClass().getDeclaredField("vendor"));
        assertNotNull(rdo.getClass().getMethod("getVendor"));
        assertNotNull(rdo.getClass().getMethod("setVendor", String.class));
    }

    @Test
    public void field_amount() throws Exception {
        assertNotNull(rdo.getClass().getDeclaredField("amount"));
        assertNotNull(rdo.getClass().getMethod("getAmount"));
        assertNotNull(rdo.getClass().getMethod("setAmount", String.class));
    }

    @Test
    public void field_purchasePrice() throws Exception {
        assertNotNull(rdo.getClass().getDeclaredField("purchasePrice"));
        assertNotNull(rdo.getClass().getMethod("getPurchasePrice"));
        assertNotNull(rdo.getClass()
                .getMethod("setPurchasePrice", String.class));
    }

}
