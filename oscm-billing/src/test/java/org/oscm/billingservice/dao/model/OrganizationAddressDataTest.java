/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Mike J&auml;ger                                                      
 *                                                                              
 *  Creation Date: 10.05.2010                                                      
 *                                                                              
 *  Completion Time: 10.05.2010                                             
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.dao.model;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.oscm.billingservice.dao.model.OrganizationAddressData;

/**
 * @author Mike J&auml;ger
 * 
 */
public class OrganizationAddressDataTest {

    private OrganizationAddressData orgData;

    @Before
    public void setUp() {
        orgData = new OrganizationAddressData("address", "orgName", "email",
                "organizationId");
    }

    @Test
    public void testGetName() throws Exception {
        Assert.assertEquals("orgName", orgData.getOrganizationName());
    }

    @Test
    public void testGetAddress() throws Exception {
        Assert.assertEquals("address", orgData.getAddress());
    }

    @Test
    public void testGetCompleteAddress() throws Exception {
        Assert.assertEquals("orgName\naddress", orgData.getCompleteAddress());
    }

    @Test
    public void testGetEmail() throws Exception {
        Assert.assertEquals("email", orgData.getEmail());
    }

    @Test
    public void testGetOrganizationId() throws Exception {
        Assert.assertEquals("organizationId", orgData.getOrganizationId());
    }
}
