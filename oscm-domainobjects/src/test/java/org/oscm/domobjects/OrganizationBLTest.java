/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Mike J&auml;ger                                                      
 *                                                                              
 *  Creation Date: 11.03.2011                                                      
 *                                                                              
 *  Completion Time: 14.03.2011                                                  
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.oscm.domobjects.enums.OrganizationReferenceType;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;

/**
 * Tests for the business logic in the organization domain object class.
 * 
 * @author Mike J&auml;ger
 */
public class OrganizationBLTest {

    private Organization testOrg;
    private Organization definingOrg;
    private Organization secondDefOrg;

    @Before
    public void setUp() throws Exception {
        testOrg = new Organization();

        definingOrg = new Organization();
        definingOrg.setOrganizationId("definer");

        secondDefOrg = new Organization();
        secondDefOrg.setOrganizationId("definer2");
    }

    @Test
    public void testUrl() {
        String url = "http://www.fujitsu.com";
        testOrg.setUrl(url);

        assertEquals(url, testOrg.getUrl());
    }

    @Test
    public void testPhone() {
        String phone = "1234";
        testOrg.setPhone(phone);

        assertEquals(phone, testOrg.getPhone());
    }

    @Test
    public void getPaymentTypes_NullDefiningOrgId() throws Exception {
        List<OrganizationRefToPaymentType> result = testOrg
                .getPaymentTypes(null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void getPaymentTypes_NoHits() throws Exception {
        List<OrganizationRefToPaymentType> result = testOrg
                .getPaymentTypes(null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void getPaymentTypes_Hits() throws Exception {
        ArrayList<OrganizationReference> expectedResult = new ArrayList<OrganizationReference>();
        OrganizationReference ortpt = initOrgToPaymentRef(definingOrg);
        OrganizationReference ortpt2 = initOrgToPaymentRef(definingOrg);
        expectedResult.add(ortpt);
        expectedResult.add(ortpt2);
        testOrg.setSources(expectedResult);
        List<OrganizationRefToPaymentType> result = testOrg
                .getPaymentTypes(definingOrg.getOrganizationId());
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    public void getPaymentTypes_FilteredHits() throws Exception {
        ArrayList<OrganizationReference> expectedResult = new ArrayList<OrganizationReference>();
        OrganizationReference ortpt = initOrgToPaymentRef(definingOrg);
        OrganizationReference ortpt2 = initOrgToPaymentRef(definingOrg);
        OrganizationReference ortpt3 = initOrgToPaymentRef(secondDefOrg);
        expectedResult.add(ortpt);
        expectedResult.add(ortpt2);
        expectedResult.add(ortpt3);
        testOrg.setSources(expectedResult);
        List<OrganizationRefToPaymentType> result = testOrg
                .getPaymentTypes(definingOrg.getOrganizationId());
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    private OrganizationReference initOrgToPaymentRef(Organization defOrg) {
        OrganizationReference orgRef = new OrganizationReference(defOrg,
                testOrg, OrganizationReferenceType.SUPPLIER_TO_CUSTOMER);
        OrganizationRefToPaymentType ortpt = new OrganizationRefToPaymentType();
        ortpt.setOrganizationReference(orgRef);
        orgRef.getPaymentTypes().add(ortpt);
        return orgRef;
    }

    @Test
    public void hasAtLeastOneRole() {
        // given
        Organization org = new Organization();
        OrganizationRole roleSupplier = new OrganizationRole();
        roleSupplier.setRoleName(OrganizationRoleType.SUPPLIER);
        OrganizationRole roleTech = new OrganizationRole();
        roleTech.setRoleName(OrganizationRoleType.TECHNOLOGY_PROVIDER);

        OrganizationToRole orgToRoleSupplier = new OrganizationToRole();
        orgToRoleSupplier.setOrganization(org);
        orgToRoleSupplier.setOrganizationRole(roleSupplier);

        OrganizationToRole orgToRoleTech = new OrganizationToRole();
        orgToRoleTech.setOrganization(org);
        orgToRoleTech.setOrganizationRole(roleTech);

        org.setGrantedRoles(new HashSet<OrganizationToRole>(Arrays.asList(
                orgToRoleSupplier, orgToRoleTech)));

        // when
        assertTrue(org.hasAtLeastOneRole(OrganizationRoleType.SUPPLIER,
                OrganizationRoleType.BROKER, OrganizationRoleType.RESELLER));
        assertFalse(org.hasAtLeastOneRole(OrganizationRoleType.CUSTOMER,
                OrganizationRoleType.PLATFORM_OPERATOR));
    }
}
