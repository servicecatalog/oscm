/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Sep 10, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.reportingservice.business.data.partnerrevenue;

import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.oscm.reportingservice.business.model.partnerrevenue.RDOCurrency;

/**
 * @author kulle
 * 
 */
public class RDOCurrencyFieldTest {

    private RDOCurrency rdo;

    @Before
    public void setup() {
        rdo = new RDOCurrency();
    }

    @Test
    public void field_totalAmount() throws Exception {
        assertNotNull(rdo.getClass().getDeclaredField("totalAmount"));
        assertNotNull(rdo.getClass().getMethod("getTotalAmount"));
        assertNotNull(rdo.getClass().getMethod("setTotalAmount", String.class));
    }

    @Test
    public void field_totalRevenue() throws Exception {
        assertNotNull(rdo.getClass().getDeclaredField("totalRevenue"));
        assertNotNull(rdo.getClass().getMethod("getTotalRevenue"));
        assertNotNull(rdo.getClass().getMethod("setTotalRevenue", String.class));
    }

    @Test
    public void field_brokerRevenue() throws Exception {
        assertNotNull(rdo.getClass().getDeclaredField("brokerRevenue"));
        assertNotNull(rdo.getClass().getMethod("getBrokerRevenue"));
        assertNotNull(rdo.getClass()
                .getMethod("setBrokerRevenue", String.class));
    }

    @Test
    public void field_remainingAmount() throws Exception {
        assertNotNull(rdo.getClass().getDeclaredField("remainingAmount"));
        assertNotNull(rdo.getClass().getMethod("getRemainingAmount"));
        assertNotNull(rdo.getClass().getMethod("setRemainingAmount",
                String.class));
    }

    @Test
    public void field_revenueDetails() throws Exception {
        assertNotNull(rdo.getClass().getDeclaredField("revenueDetails"));
        assertNotNull(rdo.getClass().getMethod("getRevenueDetails"));
        assertNotNull(rdo.getClass().getMethod("setRevenueDetails", List.class));
    }

    @Test
    public void field_currency() throws Exception {
        assertNotNull(rdo.getClass().getDeclaredField("currency"));
        assertNotNull(rdo.getClass().getMethod("getCurrency"));
        assertNotNull(rdo.getClass().getMethod("setCurrency", String.class));
    }

}
