/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Sep 19, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.reportingservice.business.data.supplierrevenueshare;

import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.oscm.reportingservice.business.model.supplierrevenushare.RDOSupplierRevenueShareReports;

/**
 * @author tokoda
 * 
 */
public class RDOSupplierRevenueShareReportsFieldTest {
    private RDOSupplierRevenueShareReports rdo;

    @Before
    public void setup() {
        rdo = new RDOSupplierRevenueShareReports();
    }

    @Test
    public void field_totalAmount() throws Exception {
        assertNotNull(rdo.getClass().getDeclaredField("reports"));
        assertNotNull(rdo.getClass().getMethod("getReports"));
        assertNotNull(rdo.getClass().getMethod("setReports", List.class));
    }
}
