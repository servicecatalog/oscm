/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Dirk Bernsau                                                      
 *                                                                              
 *  Creation Date: Nov 30, 2011                                                      
 *                                                                              
 *  Completion Time: Nov 30, 2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;

import org.junit.Test;

import org.oscm.internal.vo.VOCatalogEntry;
import org.oscm.internal.vo.VOCustomerService;
import org.oscm.internal.vo.VOMarketplace;
import org.oscm.internal.vo.VOService;

public class CustomerServiceTest {

    @Test
    public void testOrganizationAttributes() {
        VOCustomerService voCs = new VOCustomerService();
        voCs.setOrganizationId("123456");
        voCs.setOrganizationName("MyOrg");
        Long organizationKey = new Long(123);
        voCs.setOrganizationKey(organizationKey);
        CustomerService cs = new CustomerService(voCs, new Service(
                new VOService()));
        assertEquals(
                voCs.getOrganizationName() + " (" + voCs.getOrganizationId()
                        + ")", cs.getOrganizationDisplayName());
        assertEquals(organizationKey, cs.getOrganizationKey());
        assertEquals(organizationKey, voCs.getOrganizationKey());
    }

    @Test
    public void testOrganizationDisplayName_Empty() {
        VOCustomerService voCs = new VOCustomerService();
        voCs.setOrganizationId("123456");
        voCs.setOrganizationName("");
        CustomerService cs = new CustomerService(voCs, new Service(
                new VOService()));
        assertEquals(voCs.getOrganizationId(), cs.getOrganizationDisplayName());
    }

    @Test
    public void testOrganizationDisplayName_Blank() {
        VOCustomerService voCs = new VOCustomerService();
        voCs.setOrganizationId("123456");
        voCs.setOrganizationName("    ");
        CustomerService cs = new CustomerService(voCs, new Service(
                new VOService()));
        assertEquals(voCs.getOrganizationId(), cs.getOrganizationDisplayName());
    }

    @Test
    public void testOrganizationDisplayName_OrgIdNull() {
        VOCustomerService voCs = new VOCustomerService();
        voCs.setOrganizationId(null);
        voCs.setOrganizationName("Org");
        CustomerService cs = new CustomerService(voCs, new Service(
                new VOService()));
        assertEquals(voCs.getOrganizationName(),
                cs.getOrganizationDisplayName());
    }

    @Test
    public void testOrganizationDisplayName_OrgIdEmpty() {
        VOCustomerService voCs = new VOCustomerService();
        voCs.setOrganizationId("");
        voCs.setOrganizationName("Org");
        CustomerService cs = new CustomerService(voCs, new Service(
                new VOService()));
        assertEquals(voCs.getOrganizationName(),
                cs.getOrganizationDisplayName());
    }

    @Test
    public void testOrganizationDisplayName_OrgIdBlank() {
        VOCustomerService voCs = new VOCustomerService();
        voCs.setOrganizationId("     ");
        voCs.setOrganizationName("Org");
        CustomerService cs = new CustomerService(voCs, new Service(
                new VOService()));
        assertEquals(voCs.getOrganizationName(),
                cs.getOrganizationDisplayName());
    }

    @Test
    public void testOrganizationDisplayName_Null() {
        VOCustomerService voCs = new VOCustomerService();
        voCs.setOrganizationId("123456");
        voCs.setOrganizationName(null);
        CustomerService cs = new CustomerService(voCs, new Service(
                new VOService()));
        assertEquals(voCs.getOrganizationId(), cs.getOrganizationDisplayName());
    }

    @Test
    public void testNoMarketplace() {
        VOCustomerService voCs = new VOCustomerService();
        voCs.setOrganizationId("123456");
        voCs.setOrganizationName(null);
        Service service = new Service(new VOService());
        service.setCatalogEntries(null);
        CustomerService cs = new CustomerService(voCs, service);
        assertTrue(cs.isNoMarketplaceAssigned());
        VOCatalogEntry ce = new VOCatalogEntry();
        service.setCatalogEntries(Collections.singletonList(ce));
        cs = new CustomerService(voCs, service);
        assertTrue(cs.isNoMarketplaceAssigned());
        ce.setMarketplace(new VOMarketplace());
        service.setCatalogEntries(Collections.singletonList(ce));
        cs = new CustomerService(voCs, service);
        assertFalse(cs.isNoMarketplaceAssigned());

    }

    @Test
    public void testHiddenFields() {
        VOService voSvc = new VOService();
        voSvc.setName("Name");
        voSvc.setServiceId("ID");
        CustomerService cs = new CustomerService(new VOCustomerService(),
                new Service(voSvc));
        assertNull(cs.getName());
        assertNull(cs.getServiceId());
    }
}
