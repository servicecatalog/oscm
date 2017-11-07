/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2013-02-26                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.internal.types.enumtypes;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Unit test for the UserRoleType enum.
 * 
 * @author goebel
 */
public class UserRoleTypeTest {

    /**
     * PLATFORM_OPERATOR is a manager role, granting access to the
     * administration portal.
     */
    @Test
    public void isManagerRole_PLATFORM_OPERATOR() {
        assertTrue(UserRoleType.PLATFORM_OPERATOR.isManagerRole());
    }

    /**
     * ORGANIZATION_ADMIN is no manager role.
     */
    @Test
    public void isManagerRole_ORGANIZATION_ADMIN() {
        assertFalse(UserRoleType.ORGANIZATION_ADMIN.isManagerRole());
    }

    @Test
    public void isManagerRole_SUBSCRIPTION_MANAGER() {
        assertFalse(UserRoleType.SUBSCRIPTION_MANAGER.isManagerRole());
    }

    @Test
    public void isManagerRole_SERVICE_MANAGER() {
        assertTrue(UserRoleType.SERVICE_MANAGER.isManagerRole());
    }

    @Test
    public void isManagerRole_TECHNOLOGY_MANAGER() {
        assertTrue(UserRoleType.TECHNOLOGY_MANAGER.isManagerRole());
    }

    @Test
    public void isManagerRole_BROKER_MANAGER() {
        assertTrue(UserRoleType.BROKER_MANAGER.isManagerRole());
    }

    @Test
    public void isManagerRole_RESELLER_MANAGER() {
        assertTrue(UserRoleType.RESELLER_MANAGER.isManagerRole());
    }

    @Test
    public void isManagerRole_MARKETPLACE_OWNER() {
        assertTrue(UserRoleType.MARKETPLACE_OWNER.isManagerRole());
    }

    @Test
    public void isOrganizationSpecificRole_PLATFORM_OPERATOR() {
        assertTrue(UserRoleType.PLATFORM_OPERATOR.isOrganizationSpecificRole());
    }

    @Test
    public void isOrganizationSpecificRole_ORGANIZATION_ADMIN() {
        assertFalse(UserRoleType.ORGANIZATION_ADMIN
                .isOrganizationSpecificRole());
    }

    @Test
    public void isOrganizationSpecificRole_SUBSCRIPTION_MANAGER() {
        assertFalse(UserRoleType.SUBSCRIPTION_MANAGER
                .isOrganizationSpecificRole());
    }

    @Test
    public void isOrganizationSpecificRole_SERVICE_MANAGER() {
        assertTrue(UserRoleType.SERVICE_MANAGER.isOrganizationSpecificRole());
    }

    @Test
    public void isOrganizationSpecificRole_TECHNOLOGY_MANAGER() {
        assertTrue(UserRoleType.TECHNOLOGY_MANAGER.isOrganizationSpecificRole());
    }

    @Test
    public void isOrganizationSpecificRole_BROKER_MANAGER() {
        assertTrue(UserRoleType.BROKER_MANAGER.isOrganizationSpecificRole());
    }

    @Test
    public void isOrganizationSpecificRole_RESELLER_MANAGER() {
        assertTrue(UserRoleType.RESELLER_MANAGER.isOrganizationSpecificRole());
    }

    @Test
    public void isOrganizationSpecificRole_MARKETPLACE_OWNER() {
        assertTrue(UserRoleType.MARKETPLACE_OWNER.isOrganizationSpecificRole());
    }

}
