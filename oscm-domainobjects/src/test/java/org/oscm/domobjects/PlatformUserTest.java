/********************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                             
 *  Author: stavreva
 *                                                                           
 *  Creation Date: 28.08.2013                                                    
 *                                                                             
 *  Completion Time:                              
 *                                                                             
 ********************************************************************************/
package org.oscm.domobjects;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import org.oscm.internal.types.enumtypes.UserRoleType;

/**
 * Tests for PlatformUser domain object
 * 
 * @author stavreva
 * 
 */
public class PlatformUserTest {

    @Test
    public void hasRole() {
        PlatformUser user = createUserWithRole(UserRoleType.ORGANIZATION_ADMIN);
        assertTrue(user.hasRole(UserRoleType.ORGANIZATION_ADMIN));
    }

    @Test
    public void hasManagerRole_ORGANIZATION_ADMIN() {
        PlatformUser user = createUserWithRole(UserRoleType.ORGANIZATION_ADMIN);
        assertFalse(user.hasManagerRole());
    }

    @Test
    public void hasManagerRole_SUBSCRIPTION_MANAGER() {
        PlatformUser user = createUserWithRole(UserRoleType.SUBSCRIPTION_MANAGER);
        assertFalse(user.hasManagerRole());
    }

    @Test
    public void hasManagerRole_PLATFORM_OPERATOR() {
        PlatformUser user = createUserWithRole(UserRoleType.PLATFORM_OPERATOR);
        assertTrue(user.hasManagerRole());
    }

    @Test
    public void hasManagerRole_MARKETPLACE_OWNER() {
        PlatformUser user = createUserWithRole(UserRoleType.MARKETPLACE_OWNER);
        assertTrue(user.hasManagerRole());
    }

    @Test
    public void hasManagerRole_TECHNOLOGY_MANAGER() {
        PlatformUser user = createUserWithRole(UserRoleType.TECHNOLOGY_MANAGER);
        assertTrue(user.hasManagerRole());
    }

    @Test
    public void hasManagerRole_SERVICE_MANAGER() {
        PlatformUser user = createUserWithRole(UserRoleType.SERVICE_MANAGER);
        assertTrue(user.hasManagerRole());
    }

    @Test
    public void hasManagerRole_BROKER_MANAGER() {
        PlatformUser user = createUserWithRole(UserRoleType.BROKER_MANAGER);
        assertTrue(user.hasManagerRole());
    }

    @Test
    public void hasManagerRole_RESELLER_MANAGER() {
        PlatformUser user = createUserWithRole(UserRoleType.RESELLER_MANAGER);
        assertTrue(user.hasManagerRole());
    }

    @Test
    public void hasSubscriptionOwnerRole_ORGANIZATION_ADMIN() {
        PlatformUser user = createUserWithRole(UserRoleType.ORGANIZATION_ADMIN);
        assertTrue(user.hasSubscriptionOwnerRole());
    }

    @Test
    public void hasSubscriptionOwnerRole_SUBSCRIPTION_MANAGER() {
        PlatformUser user = createUserWithRole(UserRoleType.SUBSCRIPTION_MANAGER);
        assertTrue(user.hasSubscriptionOwnerRole());
    }

    @Test
    public void hasSubscriptionOwnerRole_UNIT_ADMIN() {
        PlatformUser user = createUserWithRole(UserRoleType.UNIT_ADMINISTRATOR);
        assertTrue(user.hasSubscriptionOwnerRole());
    }

    @Test
    public void hasSubscriptionOwnerRole_SERVICE_MANAGER() {
        PlatformUser user = createUserWithRole(UserRoleType.SERVICE_MANAGER);
        assertFalse(user.hasSubscriptionOwnerRole());
    }

    @Test
    public void hasSubscriptionOwnerRole_TECHNOLOGY_MANAGER() {
        PlatformUser user = createUserWithRole(UserRoleType.TECHNOLOGY_MANAGER);
        assertFalse(user.hasSubscriptionOwnerRole());
    }

    @Test
    public void hasSubscriptionOwnerRole_BROKER_MANAGER() {
        PlatformUser user = createUserWithRole(UserRoleType.BROKER_MANAGER);
        assertFalse(user.hasSubscriptionOwnerRole());
    }

    @Test
    public void hasSubscriptionOwnerRole_RESELLER_MANAGER() {
        PlatformUser user = createUserWithRole(UserRoleType.RESELLER_MANAGER);
        assertFalse(user.hasSubscriptionOwnerRole());
    }

    @Test
    public void hasSubscriptionOwnerRole_MARKETPLACE_OWNER() {
        PlatformUser user = createUserWithRole(UserRoleType.MARKETPLACE_OWNER);
        assertFalse(user.hasSubscriptionOwnerRole());
    }

    @Test
    public void hasSubscriptionOwnerRole_PLATFORM_OPERATOR() {
        PlatformUser user = createUserWithRole(UserRoleType.PLATFORM_OPERATOR);
        assertFalse(user.hasSubscriptionOwnerRole());
    }

    @Test
    public void hasSubscriptionOwnerRole_PLATFORM_OPERATOR_and_ORGANIZATION_ADMIN() {
        PlatformUser user = createUserWithRole(UserRoleType.PLATFORM_OPERATOR,
                UserRoleType.ORGANIZATION_ADMIN);
        assertTrue(user.hasSubscriptionOwnerRole());
    }

    @Test
    public void hasSubscriptionOwnerRole_PLATFORM_OPERATOR_and_SUBSCRIPTION_MANAGER() {
        PlatformUser user = createUserWithRole(UserRoleType.PLATFORM_OPERATOR,
                UserRoleType.SUBSCRIPTION_MANAGER);
        assertTrue(user.hasSubscriptionOwnerRole());
    }

    @Test
    public void isOrganizationAdmin() {
        PlatformUser user = createUserWithRole(UserRoleType.ORGANIZATION_ADMIN);
        assertTrue(user.isOrganizationAdmin());
    }

    PlatformUser createUserWithRole(UserRoleType... roleTypes) {
        PlatformUser user = new PlatformUser();
        for (UserRoleType roleType : roleTypes) {
            RoleAssignment assignment = new RoleAssignment();
            assignment.setUser(user);
            assignment.setRole(new UserRole(roleType));
            user.getAssignedRoles().add(assignment);
        }
        return user;
    }

}
