/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Sep 17, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.reportingservice.business.data.partnerrevenue;

import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.oscm.reportingservice.business.model.partnerrevenue.RDOPartnerReports;

/**
 * @author kulle
 * 
 */
public class RDOPartnerReportsFieldTest {

    private RDOPartnerReports rdo;

    @Before
    public void setup() {
        rdo = new RDOPartnerReports();
    }

    @Test
    public void field_totalAmount() throws Exception {
        assertNotNull(rdo.getClass().getDeclaredField("reports"));
        assertNotNull(rdo.getClass().getMethod("getReports"));
        assertNotNull(rdo.getClass().getMethod("setReports", List.class));
    }
}
