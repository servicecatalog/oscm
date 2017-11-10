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

import org.oscm.reportingservice.business.model.partnerrevenue.RDOPartnerReport;

/**
 * Checks if the RDOHeader has the field xxx which is used by the report
 * template. If you rename the field in the RDO you have to adapt the report
 * template as well!
 * 
 * @author kulle
 */
public class RDOPartnerReportFieldTest {

    private RDOPartnerReport rdo;

    @Before
    public void setup() {
        rdo = new RDOPartnerReport();
    }

    @Test
    public void field_fromDate() throws Exception {
        assertNotNull(rdo.getClass().getDeclaredField("periodStart"));
        assertNotNull(rdo.getClass().getMethod("getPeriodStart"));
        assertNotNull(rdo.getClass().getMethod("setPeriodStart", String.class));
    }

    @Test
    public void field_toDate() throws Exception {
        assertNotNull(rdo.getClass().getDeclaredField("periodEnd"));
        assertNotNull(rdo.getClass().getMethod("getPeriodEnd"));
        assertNotNull(rdo.getClass().getMethod("setPeriodEnd", String.class));
    }

    @Test
    public void field_address() throws Exception {
        assertNotNull(rdo.getClass().getDeclaredField("address"));
        assertNotNull(rdo.getClass().getMethod("getAddress"));
        assertNotNull(rdo.getClass().getMethod("setAddress", String.class));
    }

    @Test
    public void field_vendor() throws Exception {
        assertNotNull(rdo.getClass().getDeclaredField("vendor"));
        assertNotNull(rdo.getClass().getMethod("getVendor"));
        assertNotNull(rdo.getClass().getMethod("setVendor", String.class));
    }

    @Test
    public void field_vendorType() throws Exception {
        assertNotNull(rdo.getClass().getDeclaredField("vendorType"));
        assertNotNull(rdo.getClass().getMethod("getVendorType"));
        assertNotNull(rdo.getClass().getMethod("setVendorType", String.class));
    }

    @Test
    public void field_countryName() throws Exception {
        assertNotNull(rdo.getClass().getDeclaredField("countryName"));
        assertNotNull(rdo.getClass().getMethod("getCountryName"));
        assertNotNull(rdo.getClass().getMethod("setCountryName", String.class));
    }

}
