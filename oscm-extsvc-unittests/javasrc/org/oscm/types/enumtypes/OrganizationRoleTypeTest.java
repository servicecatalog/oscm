/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: cheld                                                    
 *                                                                              
 *  Creation Date: 10.05.2011                                                      
 *                                                                              
 *  Completion Time: 10.05.2011                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.types.enumtypes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.junit.Test;

/**
 * Test cases for class <code>OrganizationRoleType</code>
 * 
 * @author cheld
 * 
 */
public class OrganizationRoleTypeTest {

    /**
     * By default users of a PLATFORM_OPERATOR organization have role
     * PLATFORM_OPERATOR
     */
    @Test
    public void correspondingUserRole_PLATFORM_OPERATOR() {
        assertEquals(UserRoleType.PLATFORM_OPERATOR,
                OrganizationRoleType.PLATFORM_OPERATOR.correspondingUserRole());
    }

    /**
     * By default users of a MARKETPLACE_OWNER organization have role
     * MARKETPLACE_OWNER
     */
    @Test
    public void correspondingUserRole_MARKETPLACE_OWNER() {
        assertEquals(UserRoleType.MARKETPLACE_OWNER,
                OrganizationRoleType.MARKETPLACE_OWNER.correspondingUserRole());
    }

    /**
     * By default users of a TECHNOLOGY_PROVIDER organization have role
     * TECHNOLOGY_MANAGER
     */
    @Test
    public void correspondingUserRole_TECHNOLOGY_PROVIDER() {
        assertEquals(UserRoleType.TECHNOLOGY_MANAGER,
                OrganizationRoleType.TECHNOLOGY_PROVIDER
                        .correspondingUserRole());
    }

    /**
     * By default users of a SUPPLIER organization have role SERVICE_MANAGER
     */
    @Test
    public void correspondingUserRole_SUPPLIER() {
        assertEquals(UserRoleType.SERVICE_MANAGER,
                OrganizationRoleType.SUPPLIER.correspondingUserRole());
    }

    /**
     * The organization role customer is not mapped to user role, because a
     * customer has no special user rights.
     */
    @Test
    public void correspondingUserRole_CUSTOMER() {
        assertNull(OrganizationRoleType.CUSTOMER.correspondingUserRole());
    }

    /**
     * By default users of a BROKER organization have role BROKER_MANAGER
     */
    @Test
    public void correspondingUserRole_BROKER() {
        assertEquals(UserRoleType.BROKER_MANAGER,
                OrganizationRoleType.BROKER.correspondingUserRole());
    }

    /**
     * By default users of a RESELLER organization have role RESELLER_MANAGER
     */
    @Test
    public void correspondingUserRole_RESELLER() {
        assertEquals(UserRoleType.RESELLER_MANAGER,
                OrganizationRoleType.RESELLER.correspondingUserRole());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void correspondingUserRoles_empty() {
        assertTrue(OrganizationRoleType.correspondingUserRoles(
                Collections.EMPTY_LIST).isEmpty());
    }

    /**
     * Organization role customer is not mapped to a user role and must be
     * skipped
     */
    @Test
    public void correspondingUserRoles_skipCUSTOMER() {
        // given two org roles. one is customer
        Collection<OrganizationRoleType> givenOrganizationRoles = Arrays
                .asList(new OrganizationRoleType[] {
                        OrganizationRoleType.CUSTOMER,
                        OrganizationRoleType.SUPPLIER });

        // when mapped to user roles
        Set<UserRoleType> userRoles = OrganizationRoleType
                .correspondingUserRoles(givenOrganizationRoles);

        // then customer must be skipped
        assertEquals(1, userRoles.size());
        assertTrue(userRoles.contains(UserRoleType.SERVICE_MANAGER));
    }

    @Test
    public void correspondingOrgRoleForUserRole_PLATFORM_OPERATOR() {
        assertEquals(
                OrganizationRoleType.PLATFORM_OPERATOR,
                OrganizationRoleType
                        .correspondingOrgRoleForUserRole(UserRoleType.PLATFORM_OPERATOR));
    }

    @Test
    public void correspondingOrgRoleForUserRole_TECHNOLOGY_MANAGER() {
        assertEquals(
                OrganizationRoleType.TECHNOLOGY_PROVIDER,

                OrganizationRoleType
                        .correspondingOrgRoleForUserRole(UserRoleType.TECHNOLOGY_MANAGER));
    }

    @Test
    public void correspondingOrgRoleForUserRole_SERVICE_MANAGER() {
        assertEquals(
                OrganizationRoleType.SUPPLIER,
                OrganizationRoleType
                        .correspondingOrgRoleForUserRole(UserRoleType.SERVICE_MANAGER));
    }

    @Test
    public void correspondingOrgRoleForUserRole_MARKETPLACE_OWNER() {
        assertEquals(
                OrganizationRoleType.MARKETPLACE_OWNER,

                OrganizationRoleType
                        .correspondingOrgRoleForUserRole(UserRoleType.MARKETPLACE_OWNER));
    }

    @Test
    public void correspondingOrgRoleForUserRole_BROKER_MANAGER() {
        assertEquals(
                OrganizationRoleType.BROKER,
                OrganizationRoleType
                        .correspondingOrgRoleForUserRole(UserRoleType.BROKER_MANAGER));
    }

    @Test
    public void correspondingOrgRoleForUserRole_RESELLER_MANAGER() {
        assertEquals(
                OrganizationRoleType.RESELLER,
                OrganizationRoleType
                        .correspondingOrgRoleForUserRole(UserRoleType.RESELLER_MANAGER));
    }

    /**
     * The user role ORGANIZATION_ADMIN is not connected to any organization
     * role. It's allowed to be assigned to any user, it's handled by
     * IdentityService.
     */
    @Test
    public void correspondingOrgRoleForUserRole_ORGANIZATION_ADMIN() {
        assertEquals(
                null,
                OrganizationRoleType
                        .correspondingOrgRoleForUserRole(UserRoleType.ORGANIZATION_ADMIN));
    }

}
