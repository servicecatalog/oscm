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

import org.oscm.reportingservice.business.model.supplierrevenushare.RDOSupplierRevenueShareReport;

/**
 * @author tokoda
 * 
 */
public class RDOSupplierRevenueShareReportFieldTest {

    private RDOSupplierRevenueShareReport rdo;

    @Before
    public void setup() {
        rdo = new RDOSupplierRevenueShareReport();
    }

    @Test
    public void field_supplier() throws Exception {
        assertNotNull(rdo.getClass().getDeclaredField("supplier"));
        assertNotNull(rdo.getClass().getMethod("getSupplier"));
        assertNotNull(rdo.getClass().getMethod("setSupplier", String.class));
    }

    @Test
    public void field_periodStart() throws Exception {
        assertNotNull(rdo.getClass().getDeclaredField("periodStart"));
        assertNotNull(rdo.getClass().getMethod("getPeriodStart"));
        assertNotNull(rdo.getClass().getMethod("setPeriodStart", String.class));
    }

    @Test
    public void field_periodEnd() throws Exception {
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
    public void field_country() throws Exception {
        assertNotNull(rdo.getClass().getDeclaredField("country"));
        assertNotNull(rdo.getClass().getMethod("getCountry"));
        assertNotNull(rdo.getClass().getMethod("setCountry", String.class));
    }

    @Test
    public void field_currencies() throws Exception {
        assertNotNull(rdo.getClass().getDeclaredField("currencies"));
        assertNotNull(rdo.getClass().getMethod("getCurrencies"));
        assertNotNull(rdo.getClass().getMethod("setCurrencies", List.class));
    }

}
