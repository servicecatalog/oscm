/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Mar 30, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.identityservice.bean;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationRole;
import org.oscm.domobjects.OrganizationToRole;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.RoleAssignment;
import org.oscm.domobjects.UserRole;
import org.oscm.test.data.UserRoles;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.SaaSSystemException;
import org.oscm.internal.types.exception.UserRoleAssignmentException;

/**
 * Unit tests for the user role management functionality of
 * {@link IdentityServiceBean}.
 * 
 * @author barzu
 */
public class IdentityServiceBeanUserRolesTest {

    private IdentityServiceBean idSrv;
    private PlatformUser user;

    @Before
    public void setup() throws Exception {
        idSrv = Mockito.spy(new IdentityServiceBean());

        user = new PlatformUser();
        Set<RoleAssignment> grantedRoles = new HashSet<RoleAssignment>();
        RoleAssignment assignedRole = new RoleAssignment();
        UserRole role = new UserRole();
        role.setRoleName(UserRoleType.ORGANIZATION_ADMIN);
        assignedRole.setRole(role);
        grantedRoles.add(assignedRole);
        user.setAssignedRoles(grantedRoles);
        idSrv.dm = mock(DataService.class);
        when(idSrv.dm.getCurrentUser()).thenReturn(user);
    }

    @Test
    public void grantUserRolesLocal() throws Exception {
        doNothing().when(idSrv).checkRoleConstrains(any(PlatformUser.class),
                any(UserRoleType.class));
        doNothing().when(idSrv).grantRole(any(PlatformUser.class),
                any(UserRoleType.class));
        idSrv.grantUserRoles(user, Arrays.asList(
                UserRoleType.MARKETPLACE_OWNER, UserRoleType.SERVICE_MANAGER));

        verify(idSrv, times(1)).checkRoleConstrains(eq(user),
                eq(UserRoleType.MARKETPLACE_OWNER));
        verify(idSrv, times(1)).grantRole(eq(user),
                eq(UserRoleType.MARKETPLACE_OWNER));

        verify(idSrv, times(1)).checkRoleConstrains(eq(user),
                eq(UserRoleType.SERVICE_MANAGER));
        verify(idSrv, times(1)).grantRole(eq(user),
                eq(UserRoleType.SERVICE_MANAGER));
    }

    @Test
    public void grantUserRolesLocal_AlreadyHasRole() throws Exception {
        doNothing().when(idSrv).checkRoleConstrains(any(PlatformUser.class),
                any(UserRoleType.class));
        doNothing().when(idSrv).grantRole(any(PlatformUser.class),
                any(UserRoleType.class));
        user.setAssignedRoles(UserRoles.createRoleAssignments(user,
                UserRoleType.MARKETPLACE_OWNER));

        idSrv.grantUserRoles(user, Arrays.asList(
                UserRoleType.MARKETPLACE_OWNER, UserRoleType.SERVICE_MANAGER));

        verify(idSrv, times(0)).checkRoleConstrains(eq(user),
                eq(UserRoleType.MARKETPLACE_OWNER));
        verify(idSrv, times(0)).grantRole(eq(user),
                eq(UserRoleType.MARKETPLACE_OWNER));

        verify(idSrv, times(1)).checkRoleConstrains(eq(user),
                eq(UserRoleType.SERVICE_MANAGER));
        verify(idSrv, times(1)).grantRole(eq(user),
                eq(UserRoleType.SERVICE_MANAGER));
    }

    @Test
    public void grantRole() throws Exception {
        UserRole role = new UserRole(UserRoleType.MARKETPLACE_OWNER);
        doReturn(role).when(idSrv.dm).find(any(UserRole.class));
        idSrv.grantRole(user, UserRoleType.MARKETPLACE_OWNER);

        verify(idSrv.dm, times(1))
                .persist(
                        argThat(isRoleAssignment(user,
                                UserRoleType.MARKETPLACE_OWNER)));
    }

    @Test(expected = SaaSSystemException.class)
    public void grantRole_RoleNotFound() throws Exception {
        doReturn(null).when(idSrv.dm).find(any(UserRole.class));
        idSrv.grantRole(user, UserRoleType.MARKETPLACE_OWNER);
    }

    @Test
    public void checkRoleConstrains_PLATFORM_OPERATOR() throws Exception {
        user.setOrganization(getOrganizationWithRoles(OrganizationRoleType.PLATFORM_OPERATOR));
        idSrv.checkRoleConstrains(user, UserRoleType.PLATFORM_OPERATOR);
    }

    @Test(expected = UserRoleAssignmentException.class)
    public void checkRoleConstrains_No_PLATFORM_OPERATOR() throws Exception {
        user.setOrganization(getOrganizationWithRoles());
        idSrv.checkRoleConstrains(user, UserRoleType.PLATFORM_OPERATOR);
    }

    @Test
    public void checkRoleConstrains_TECHNOLOGY_MANAGER() throws Exception {
        user.setOrganization(getOrganizationWithRoles(OrganizationRoleType.TECHNOLOGY_PROVIDER));
        idSrv.checkRoleConstrains(user, UserRoleType.TECHNOLOGY_MANAGER);
    }

    @Test(expected = UserRoleAssignmentException.class)
    public void checkRoleConstrains_TECHNOLOGY_MANAGER_PO_Failure()
            throws Exception {
        user.setOrganization(getOrganizationWithRoles(OrganizationRoleType.PLATFORM_OPERATOR));
        idSrv.checkRoleConstrains(user, UserRoleType.TECHNOLOGY_MANAGER);
    }

    @Test
    public void checkRoleConstrains_TECHNOLOGY_MANAGER_PO() throws Exception {
        user.setOrganization(getOrganizationWithRoles(OrganizationRoleType.TECHNOLOGY_PROVIDER));
        idSrv.checkRoleConstrains(user, UserRoleType.TECHNOLOGY_MANAGER);
    }

    @Test(expected = UserRoleAssignmentException.class)
    public void checkRoleConstrains_No_TECHNOLOGY_MANAGER() throws Exception {
        user.setOrganization(getOrganizationWithRoles());
        idSrv.checkRoleConstrains(user, UserRoleType.TECHNOLOGY_MANAGER);
    }

    @Test
    public void checkRoleConstrains_MinimumUser() throws Exception {
        user.setOrganization(getOrganizationWithRoles(OrganizationRoleType.CUSTOMER));
        idSrv.checkRoleConstrains(user, UserRoleType.ORGANIZATION_ADMIN);
    }

    @Test
    public void checkRoleConstrains_SERVICE_MANAGER() throws Exception {
        user.setOrganization(getOrganizationWithRoles(OrganizationRoleType.SUPPLIER));
        idSrv.checkRoleConstrains(user, UserRoleType.SERVICE_MANAGER);
    }

    @Test(expected = UserRoleAssignmentException.class)
    public void checkRoleConstrains_SERVICE_MANAGER_PO_Failure()
            throws Exception {
        user.setOrganization(getOrganizationWithRoles(OrganizationRoleType.PLATFORM_OPERATOR));
        idSrv.checkRoleConstrains(user, UserRoleType.SERVICE_MANAGER);
    }

    @Test
    public void checkRoleConstrains_SERVICE_MANAGER_PO() throws Exception {
        user.setOrganization(getOrganizationWithRoles(OrganizationRoleType.SUPPLIER));
        idSrv.checkRoleConstrains(user, UserRoleType.SERVICE_MANAGER);
    }

    @Test(expected = UserRoleAssignmentException.class)
    public void checkRoleConstrains_No_SERVICE_MANAGER() throws Exception {
        user.setOrganization(getOrganizationWithRoles());
        idSrv.checkRoleConstrains(user, UserRoleType.SERVICE_MANAGER);
    }

    @Test
    public void checkRoleConstrains_MARKETPLACE_OWNER() throws Exception {
        user.setOrganization(getOrganizationWithRoles(OrganizationRoleType.MARKETPLACE_OWNER));
        idSrv.checkRoleConstrains(user, UserRoleType.MARKETPLACE_OWNER);
    }

    @Test(expected = UserRoleAssignmentException.class)
    public void checkRoleConstrains_MARKETPLACE_OWNER_PO_Failure()
            throws Exception {
        user.setOrganization(getOrganizationWithRoles(OrganizationRoleType.PLATFORM_OPERATOR));
        idSrv.checkRoleConstrains(user, UserRoleType.MARKETPLACE_OWNER);
    }

    @Test
    public void checkRoleConstrains_MARKETPLACE_OWNER_PO() throws Exception {
        user.setOrganization(getOrganizationWithRoles(OrganizationRoleType.MARKETPLACE_OWNER));
        idSrv.checkRoleConstrains(user, UserRoleType.MARKETPLACE_OWNER);
    }

    @Test(expected = UserRoleAssignmentException.class)
    public void checkRoleConstrains_No_MARKETPLACE_OWNER() throws Exception {
        user.setOrganization(getOrganizationWithRoles());
        idSrv.checkRoleConstrains(user, UserRoleType.MARKETPLACE_OWNER);
    }

    @Test
    public void checkRoleConstrains_BROKER_MANAGER() throws Exception {
        user.setOrganization(getOrganizationWithRoles(OrganizationRoleType.BROKER));
        idSrv.checkRoleConstrains(user, UserRoleType.BROKER_MANAGER);
    }

    @Test(expected = UserRoleAssignmentException.class)
    public void checkRoleConstrains_BROKER_MANAGER_PO_Failure()
            throws Exception {
        user.setOrganization(getOrganizationWithRoles(OrganizationRoleType.PLATFORM_OPERATOR));
        idSrv.checkRoleConstrains(user, UserRoleType.BROKER_MANAGER);
    }

    @Test
    public void checkRoleConstrains_BROKER_MANAGER_PO() throws Exception {
        user.setOrganization(getOrganizationWithRoles(OrganizationRoleType.BROKER));
        idSrv.checkRoleConstrains(user, UserRoleType.BROKER_MANAGER);
    }

    @Test(expected = UserRoleAssignmentException.class)
    public void checkRoleConstrains_No_BROKER_MANAGER() throws Exception {
        user.setOrganization(getOrganizationWithRoles());
        idSrv.checkRoleConstrains(user, UserRoleType.BROKER_MANAGER);
    }

    @Test
    public void checkRoleConstrains_RESELLER_MANAGER() throws Exception {
        user.setOrganization(getOrganizationWithRoles(OrganizationRoleType.RESELLER));
        idSrv.checkRoleConstrains(user, UserRoleType.RESELLER_MANAGER);
    }

    @Test(expected = UserRoleAssignmentException.class)
    public void checkRoleConstrains_RESELLER_MANAGER_PO_Failure()
            throws Exception {
        user.setOrganization(getOrganizationWithRoles(OrganizationRoleType.PLATFORM_OPERATOR));
        idSrv.checkRoleConstrains(user, UserRoleType.RESELLER_MANAGER);
    }

    @Test
    public void checkRoleConstrains_RESELLER_MANAGER_PO() throws Exception {
        user.setOrganization(getOrganizationWithRoles(OrganizationRoleType.RESELLER));
        idSrv.checkRoleConstrains(user, UserRoleType.RESELLER_MANAGER);
    }

    @Test(expected = UserRoleAssignmentException.class)
    public void checkRoleConstrains_No_RESELLER_MANAGER() throws Exception {
        user.setOrganization(getOrganizationWithRoles());
        idSrv.checkRoleConstrains(user, UserRoleType.RESELLER_MANAGER);
    }

    @Test
    public void isAllowedUserRole_SUBSCRIPTION_MANAGER() {
        // given
        Organization org = getOrganizationWithRoles(OrganizationRoleType.CUSTOMER);
        // when
        Boolean result = Boolean.valueOf(idSrv.isAllowedUserRole(org,
                UserRoleType.SUBSCRIPTION_MANAGER));
        // then
        assertEquals(Boolean.TRUE, result);
    }

    @Test
    public void isAllowedUserRole_ORGANIZATION_ADMIN() {
        // given
        Organization org = getOrganizationWithRoles(OrganizationRoleType.CUSTOMER);
        // when
        Boolean result = Boolean.valueOf(idSrv.isAllowedUserRole(org,
                UserRoleType.ORGANIZATION_ADMIN));
        // then
        assertEquals(Boolean.TRUE, result);
    }

    @Test
    public void isAllowedUserRole_False() {
        // given
        Organization org = getOrganizationWithRoles(OrganizationRoleType.RESELLER);
        // when
        Boolean result = Boolean.valueOf(idSrv.isAllowedUserRole(org,
                UserRoleType.BROKER_MANAGER));
        // then
        assertEquals(Boolean.FALSE, result);
    }

    private Organization getOrganizationWithRoles(OrganizationRoleType... roles) {
        Organization org = new Organization();
        Set<OrganizationToRole> orgToRoles = new HashSet<OrganizationToRole>();
        for (OrganizationRoleType role : roles) {
            OrganizationToRole org2Role = new OrganizationToRole();
            org2Role.setOrganization(org);
            org2Role.setOrganizationRole(new OrganizationRole(role));
            orgToRoles.add(org2Role);
        }
        org.setGrantedRoles(orgToRoles);
        return org;
    }

    private ArgumentMatcher<RoleAssignment> isRoleAssignment(
            final PlatformUser u, final UserRoleType role) {
        return new ArgumentMatcher<RoleAssignment>() {
            @Override
            public boolean matches(Object argument) {
                RoleAssignment assignment = (RoleAssignment) argument;
                return u.equals(assignment.getUser())
                        && role == assignment.getRole().getRoleName();
            }
        };
    }

}
