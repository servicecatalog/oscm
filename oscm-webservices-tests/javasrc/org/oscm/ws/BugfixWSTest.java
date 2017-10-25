/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 15.04.2011                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ws;

import static org.junit.Assert.fail;
import static org.junit.Assert.assertTrue;

import javax.xml.ws.soap.SOAPFaultException;

import org.junit.Before;
import org.junit.Test;

import org.oscm.ws.base.ServiceFactory;
import org.oscm.ws.base.WebserviceTestBase;
import org.oscm.vo.VOImageResource;
import org.oscm.vo.VOServiceDetails;
import org.oscm.intf.ServiceProvisioningService;

public class BugfixWSTest {

    @Before
    public void setUp() throws Exception {
        WebserviceTestBase.getMailReader().deleteMails();
        WebserviceTestBase.getOperator().addCurrency("EUR");
    }

    @Test
    public void testBug7461() throws Exception {

        ServiceProvisioningService serviceProvisioningSrv = ServiceFactory
                .getDefault().getServiceProvisioningService();

        VOServiceDetails sd = new VOServiceDetails();
        VOImageResource ir = new VOImageResource();
        try {
            serviceProvisioningSrv.updateService(sd, ir);
            fail("Call must not succeed!");
        } catch (Exception e) {
            if (e instanceof SOAPFaultException) {
                assertTrue(e.getMessage().contains("Unauthorized"));
            }
        }
    }

}
