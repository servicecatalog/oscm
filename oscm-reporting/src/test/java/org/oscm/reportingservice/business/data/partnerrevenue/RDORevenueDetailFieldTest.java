/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Sep 7, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.reportingservice.business.data.partnerrevenue;

import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.oscm.reportingservice.business.model.partnerrevenue.RDORevenueDetail;

/**
 * Checks if the RDOHeader has the field xxx which is used by the report
 * template. If you rename the field in the RDO you have to adapt the report
 * template as well!
 * 
 * @author kulle
 */
public class RDORevenueDetailFieldTest {

    private RDORevenueDetail rdo;

    @Before
    public void setup() {
        rdo = new RDORevenueDetail();
    }

    @Test
    public void field_vendor() throws Exception {
        assertNotNull(rdo.getClass().getDeclaredField("vendor"));
        assertNotNull(rdo.getClass().getMethod("getVendor"));
        assertNotNull(rdo.getClass().getMethod("setVendor", String.class));
    }

    @Test
    public void field_revenue() throws Exception {
        assertNotNull(rdo.getClass().getDeclaredField("revenue"));
        assertNotNull(rdo.getClass().getMethod("getRevenue"));
        assertNotNull(rdo.getClass().getMethod("setRevenue", String.class));
    }

    @Test
    public void field_amount() throws Exception {
        assertNotNull(rdo.getClass().getDeclaredField("amount"));
        assertNotNull(rdo.getClass().getMethod("getAmount"));
        assertNotNull(rdo.getClass().getMethod("setAmount", String.class));
    }

    @Test
    public void field_services() throws Exception {
        assertNotNull(rdo.getClass().getDeclaredField("services"));
        assertNotNull(rdo.getClass().getMethod("getServices"));
        assertNotNull(rdo.getClass().getMethod("setServices", List.class));
    }

    @Test
    public void field_purchasePrice() throws Exception {
        assertNotNull(rdo.getClass().getDeclaredField("purchasePrice"));
        assertNotNull(rdo.getClass().getMethod("getPurchasePrice"));
        assertNotNull(rdo.getClass()
                .getMethod("setPurchasePrice", String.class));
    }

    @Test
    public void field_currency() throws Exception {
        assertNotNull(rdo.getClass().getDeclaredField("currency"));
        assertNotNull(rdo.getClass().getMethod("getCurrency"));
        assertNotNull(rdo.getClass().getMethod("setCurrency", String.class));
    }
}
