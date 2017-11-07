/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 03.09.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.usermanagement;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.notNull;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.SessionContext;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.RoleAssignment;
import org.oscm.domobjects.RoleDefinition;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.UsageLicense;
import org.oscm.domobjects.UserGroup;
import org.oscm.domobjects.UserRole;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.identityservice.local.IdentityServiceLocal;
import org.oscm.identityservice.local.LdapSettingsManagementServiceLocal;
import org.oscm.internal.components.response.Response;
import org.oscm.internal.components.response.ReturnCode;
import org.oscm.internal.components.response.ReturnType;
import org.oscm.internal.types.enumtypes.Salutation;
import org.oscm.internal.types.enumtypes.SettingType;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.types.enumtypes.UnitRoleType;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.usergroupmgmt.POUserGroup;
import org.oscm.internal.vo.VOUsageLicense;
import org.oscm.internal.vo.VOUser;
import org.oscm.internal.vo.VOUserDetails;
import org.oscm.subscriptionservice.local.SubscriptionListServiceLocal;
import org.oscm.subscriptionservice.local.SubscriptionServiceLocal;
import org.oscm.subscriptionservice.local.SubscriptionWithRoles;
import org.oscm.usergroupservice.bean.UserGroupServiceLocalBean;

/**
 * @author weiser
 * 
 */
public class UserServiceBeanTest {

    private static final String ROLENAME = "rolename";
    private static final String GROUPNAME = "groupname";
    private static final long GROUPKEY = 11223l;
    private static final String TENANT_ID = "1";

    private UserServiceBean usb;

    private PlatformUser pu;
    private Set<SettingType> mappedAttributes;

    private List<POUserGroup> assginedUserGroups;
    private List<UserGroup> assginedUserGroupsInDB;

    @SuppressWarnings("unchecked")
    @Before
    public void setup() throws Exception {
        usb = new UserServiceBean();

        usb.isl = mock(IdentityServiceLocal.class);
        usb.ssl = mock(SubscriptionServiceLocal.class);
        usb.slsl = mock(SubscriptionListServiceLocal.class);
        usb.lsmsl = mock(LdapSettingsManagementServiceLocal.class);
        usb.lsl = mock(LocalizerServiceLocal.class);
        usb.ds = mock(DataService.class);
        usb.sc = mock(SessionContext.class);
        usb.dc = spy(new DataConverter());
        usb.userGroupService = mock(UserGroupServiceLocalBean.class);

        Organization org = new Organization();
        org.setOrganizationId("orgId");

        pu = new PlatformUser();
        pu.setKey(1234);
        pu.setOrganization(org);
        pu.setEmail("mail@host.cc");
        pu.setLocale("en");
        pu.setUserId("userId");
        RoleAssignment ra = new RoleAssignment();
        ra.setUser(pu);
        ra.setRole(new UserRole(UserRoleType.ORGANIZATION_ADMIN));
        pu.setAssignedRoles(Collections.singleton(ra));

        mappedAttributes = EnumSet.of(SettingType.LDAP_ATTR_ADDITIONAL_NAME);

        when(usb.isl.getOrganizationUsers()).thenReturn(Arrays.asList(pu));
        when(usb.isl.getPlatformUser(anyString(), anyBoolean())).thenReturn(pu);
        when(usb.isl.getPlatformUser(anyString(), anyString(), anyBoolean()))
                .thenReturn(pu);
        when(usb.isl.getAvailableUserRolesForUser(any(PlatformUser.class)))
                .thenReturn(new HashSet<UserRoleType>());

        when(usb.ds.getReference(eq(PlatformUser.class), eq(pu.getKey())))
                .thenReturn(pu);
        when(usb.ds.getCurrentUser()).thenReturn(pu);

        when(usb.lsmsl.getMappedAttributes()).thenReturn(mappedAttributes);

        when(
                usb.lsl.getLocalizedTextFromDatabase(anyString(), anyLong(),
                        eq(LocalizedObjectTypes.ROLE_DEF_NAME))).thenReturn(
                ROLENAME);

        when(
                usb.slsl.getSubcsriptionsWithRoles(any(Organization.class),
                        anySetOf(SubscriptionStatus.class))).thenReturn(
                new ArrayList<SubscriptionWithRoles>());
        when(
                usb.slsl.getSubscriptionAssignments(any(PlatformUser.class),
                        anySetOf(SubscriptionStatus.class))).thenReturn(
                new ArrayList<UsageLicense>());

        when(usb.isl.createUser(any(VOUserDetails.class), anyString()))
                .thenAnswer(new Answer<VOUserDetails>() {

                    @Override
                    public VOUserDetails answer(InvocationOnMock invocation)
                            throws Throwable {
                        // return the passed user details
                        VOUserDetails ud = (VOUserDetails) invocation
                                .getArguments()[0];
                        if (ud != null) {
                            ud.setKey(pu.getKey());
                        }
                        return ud;
                    }
                });
        when(
                usb.isl.createUserWithGroups(any(VOUserDetails.class),
                        anyListOf(UserRoleType.class), anyString(), anyMap()))
                .thenAnswer(new Answer<VOUserDetails>() {

                    @Override
                    public VOUserDetails answer(InvocationOnMock invocation)
                            throws Throwable {
                        // return the passed user details
                        VOUserDetails ud = (VOUserDetails) invocation
                                .getArguments()[0];
                        if (ud != null) {
                            ud.setKey(pu.getKey());
                        }
                        return ud;
                    }
                });

        when(
                Boolean.valueOf(usb.ssl.addRevokeUser(anyString(),
                        anyListOf(VOUsageLicense.class),
                        eq((List<VOUser>) null)))).thenReturn(Boolean.TRUE);
    }

    @Test
    public void getUsers() {
        List<POUser> result = usb.getUsers();

        verify(usb.isl, times(1)).getOrganizationUsers();
        verify(usb.dc, times(1)).toPOUser(eq(pu));
        assertEquals(1, result.size());
    }

    @Test
    public void getUserDetails() throws Exception {
        String userId = "userId";
        POUserDetails user = usb.getUserDetails(userId, TENANT_ID);

        verify(usb.isl, times(1)).getPlatformUser(eq(userId), anyString(),
                eq(true));
        verify(usb.isl, times(1)).getAvailableUserRolesForUser(eq(pu));
        verify(usb.lsmsl, times(1)).getMappedAttributes();
        verify(usb.dc, times(1)).toPOUserDetails(eq(pu),
                anySetOf(UserRoleType.class));
        assertSame(mappedAttributes, user.getMappedAttributes());
        assertNotNull(user);
    }

    @Test
    public void resetUserPassword() throws Exception {
        POUser user = createPOUser(pu);
        String marketplaceId = "marketplaceId";

        usb.resetUserPassword(user, marketplaceId);

        verify(usb.isl, times(1)).getPlatformUser(eq(user.getUserId()),
                eq(true));
        verify(usb.isl, times(1)).resetUserPassword(eq(pu), eq(marketplaceId));
        verifyZeroInteractions(usb.sc);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void resetUserPassword_Rollback() throws Exception {
        when(usb.isl.getPlatformUser(anyString(), anyBoolean())).thenThrow(
                new OperationNotPermittedException());
        POUser user = createPOUser(pu);
        String marketplaceId = "marketplaceId";

        try {
            usb.resetUserPassword(user, marketplaceId);
        } finally {
            verify(usb.sc, times(1)).setRollbackOnly();
        }
    }

    @Test
    public void deleteUser() throws Exception {
        POUser user = createPOUser(pu);
        String marketplaceId = "marketplaceId";
        when(
                usb.isl.getPlatformUser(eq(user.getUserId()), anyString(),
                        eq(true))).thenReturn(pu);

        usb.deleteUser(user, marketplaceId, TENANT_ID);

        verify(usb.isl, times(1)).getPlatformUser(eq(user.getUserId()),
                anyString(), eq(true));
        verify(usb.userGroupService, times(1))
                .addLogEntryWhenDeleteUser(eq(pu));
        verify(usb.isl, times(1)).deleteUser(eq(pu), eq(marketplaceId));
        verifyZeroInteractions(usb.sc);
    }

    @Test
    public void deleteUser_NotFound() throws Exception {
        when(usb.isl.getPlatformUser(anyString(), anyString(), anyBoolean()))
                .thenThrow(new ObjectNotFoundException());
        POUser user = createPOUser(pu);
        String marketplaceId = "marketplaceId";

        usb.deleteUser(user, marketplaceId, TENANT_ID);

        verify(usb.isl, times(1)).getPlatformUser(eq(user.getUserId()),
                anyString(), eq(true));
        verifyNoMoreInteractions(usb.isl);
        verifyZeroInteractions(usb.sc);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void deleteUser_Rollback() throws Exception {
        when(usb.isl.getPlatformUser(anyString(), anyString(), anyBoolean()))
                .thenThrow(new OperationNotPermittedException());
        POUser user = createPOUser(pu);
        String marketplaceId = "marketplaceId";

        try {
            usb.deleteUser(user, marketplaceId, TENANT_ID);
        } finally {
            verify(usb.sc, times(1)).setRollbackOnly();
        }
    }

    @Test
    public void saveUser() throws Exception {
        POUserDetails user = createPOUserDetails(pu);
        Set<UserRoleType> assignedRoles = new HashSet<UserRoleType>();
        assignedRoles.add(UserRoleType.SERVICE_MANAGER);
        user.setAssignedRoles(assignedRoles);

        usb.saveUser(user);

        verify(usb.isl, times(1)).verifyIdUniquenessAndLdapAttributes(eq(pu),
                any(PlatformUser.class));
        verify(usb.isl, times(1)).setUserRolesInt(eq(user.getAssignedRoles()),
                eq(pu));
        verify(usb.isl, times(1)).notifySubscriptionsAboutUserUpdate(eq(pu));
        verify(usb.isl, times(1)).sendUserUpdatedMail(eq(pu),
                any(PlatformUser.class));

        verify(usb.dc, times(2)).updatePlatformUser(eq(user),
                (PlatformUser) notNull());
        verifyZeroInteractions(usb.sc);
    }

    @Test
    public void saveUser_NoMailSet() throws Exception {
        POUserDetails user = createPOUserDetails(pu);
        pu.setEmail(null);

        usb.saveUser(user);

        verify(usb.isl, times(1)).verifyIdUniquenessAndLdapAttributes(eq(pu),
                any(PlatformUser.class));
        verify(usb.isl, times(1)).setUserRolesInt(eq(user.getAssignedRoles()),
                eq(pu));
        verify(usb.isl, times(1)).notifySubscriptionsAboutUserUpdate(eq(pu));
        verify(usb.isl, times(1)).sendUserUpdatedMail(eq(pu),
                eq((PlatformUser) null));

        verify(usb.dc, times(2)).updatePlatformUser(eq(user),
                any(PlatformUser.class));
        verifyZeroInteractions(usb.sc);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void saveUser_Rollback() throws Exception {
        when(usb.ds.getReference(eq(PlatformUser.class), eq(pu.getKey())))
                .thenThrow(new ObjectNotFoundException());
        POUserDetails user = createPOUserDetails(pu);

        try {
            usb.saveUser(user);
        } finally {
            verify(usb.sc, times(1)).setRollbackOnly();
        }
    }

    @Test
    public void saveUser_SendAdministratorNotifyMail() throws Exception {
        POUserDetails user = createPOUserDetails(pu);

        usb.saveUser(user);

        verify(usb.isl, times(1)).sendAdministratorNotifyMail(
                any(PlatformUser.class), eq(user.getUserId()));
    }

    @Test
    public void getNewUserData() {
        POUserAndSubscriptions uas = usb.getNewUserData();

        assertNotNull(uas);
        assertNotNull(uas.getAvailableRoles());
        assertNotNull(uas.getSubscriptions());
        // locale of calling user pre-selected
        assertEquals(pu.getLocale(), uas.getLocale());
        // pre-fetch of role names
        verify(usb.lsl).getLocalizedTextFromDatabase(anyString(),
                anyListOf(Long.class),
                eq(Arrays.asList(LocalizedObjectTypes.ROLE_DEF_NAME)));
        verify(usb.dc, times(1)).toPOUserAndSubscriptionsNew(
                anyListOf(SubscriptionWithRoles.class),
                anySetOf(UserRoleType.class), any(LocalizerFacade.class));
        verify(usb.slsl, times(1)).getSubcsriptionsWithRoles(
                any(Organization.class),
                eq(Subscription.ASSIGNABLE_SUBSCRIPTION_STATUS));
        verifyZeroInteractions(usb.sc);
    }

    @Test
    public void toRoleKeyList_Null() {
        List<Long> list = usb.toRoleKeyList(null);

        assertEquals(Collections.emptyList(), list);
    }

    @Test
    public void toRoleKeyList() {
        List<SubscriptionWithRoles> subs = initSubsWithRoles();

        List<Long> list = usb.toRoleKeyList(subs);

        HashSet<Long> keys = new HashSet<Long>(Arrays.asList(Long.valueOf(10),
                Long.valueOf(11), Long.valueOf(20), Long.valueOf(21)));
        assertEquals(new ArrayList<Long>(keys), list);
    }

    @Test
    public void createNewUser() throws Exception {
        POUserAndSubscriptions user = new POUserAndSubscriptions();
        user.setAssignedRoles(EnumSet.of(UserRoleType.ORGANIZATION_ADMIN));
        user.setSubscriptions(createSubscriptions());

        Response response = usb.createNewUser(user, null);

        assertNotNull(response);
        assertTrue(response.getReturnCodes().isEmpty());
        verifyZeroInteractions(usb.sc);
        verifyZeroInteractions(usb.ssl);
        verify(usb.isl, times(1)).createUser(any(VOUserDetails.class),
                anyString());

        verify(usb.isl, times(1)).sendMailToCreatedUser(anyString(), eq(true),
                any(Marketplace.class), eq(pu));
    }

    @Test
    public void createNewUser_CreateSuspended() throws Exception {
        when(usb.isl.createUser(any(VOUserDetails.class), anyString()))
                .thenReturn(null);
        POUserAndSubscriptions user = new POUserAndSubscriptions();
        user.setAssignedRoles(EnumSet.of(UserRoleType.ORGANIZATION_ADMIN));
        user.setSubscriptions(createSubscriptions());

        Response response = usb.createNewUser(user, null);

        assertNotNull(response);
        verifyZeroInteractions(usb.sc, usb.ssl);
        ReturnCode rc = response.getReturnCodes().get(0);
        assertEquals(ReturnType.INFO, rc.getType());
        verify(usb.isl, never()).sendMailToCreatedUser(anyString(),
                anyBoolean(), any(Marketplace.class), any(PlatformUser.class));
    }

    @Test
    public void createNewUser_AddRevokeSuspended() throws Exception {
        when(
                Boolean.valueOf(usb.ssl.addRevokeUser(anyString(),
                        anyListOf(VOUsageLicense.class),
                        anyListOf(VOUser.class)))).thenReturn(Boolean.FALSE);
        POUserAndSubscriptions user = new POUserAndSubscriptions();
        user.setAssignedRoles(EnumSet.of(UserRoleType.ORGANIZATION_ADMIN));
        user.setSubscriptions(createSubscriptions());

        Response response = usb.createNewUser(user, null);

        assertNotNull(response);
        verifyZeroInteractions(usb.sc);

        verify(usb.isl, times(1)).sendMailToCreatedUser(anyString(), eq(true),
                any(Marketplace.class), eq(pu));
    }

    @Test(expected = NonUniqueBusinessKeyException.class)
    public void createNewUser_NonUniqueId() throws Exception {
        when(usb.isl.createUser(any(VOUserDetails.class), anyString()))
                .thenThrow(new NonUniqueBusinessKeyException());
        POUserAndSubscriptions user = new POUserAndSubscriptions();
        user.setAssignedRoles(EnumSet.of(UserRoleType.ORGANIZATION_ADMIN));
        user.setSubscriptions(createSubscriptions());

        try {
            usb.createNewUser(user, null);
        } finally {
            verify(usb.sc, times(1)).setRollbackOnly();
        }
    }

    @Test
    public void getUserAndSubscriptionDetails() throws Exception {
        String userId = "userId";

        POUserAndSubscriptions result = usb.getUserAndSubscriptionDetails(
                userId, TENANT_ID);

        assertNotNull(result);
        verify(usb.lsmsl, times(1)).getMappedAttributes();
        verify(usb.dc, times(1)).toPOUserAndSubscriptionsExisting(eq(pu),
                anyListOf(SubscriptionWithRoles.class),
                anySetOf(UserRoleType.class), anyListOf(UsageLicense.class),
                any(LocalizerFacade.class), same(mappedAttributes));

        verify(usb.isl, times(1)).getPlatformUser(eq(userId), anyString(),
                eq(true));
        verify(usb.isl, times(1)).getAvailableUserRolesForUser(eq(pu));

        // pre-fetch of role names
        verify(usb.lsl).getLocalizedTextFromDatabase(anyString(),
                anyListOf(Long.class),
                eq(Arrays.asList(LocalizedObjectTypes.ROLE_DEF_NAME)));
    }

    @Test
    public void getAssignments() {
        VOUser u = new VOUser();
        List<POSubscription> subs = createSubscriptions();
        List<UsageLicense> assignments = createAssignments();

        Map<String, VOUsageLicense> result = usb.getAssignments(assignments,
                subs, u);

        verifyAssignment(result, u);
    }

    @Test
    public void getUnassignments() {
        List<POSubscription> subs = createSubscriptions();
        List<UsageLicense> assignments = createAssignments();

        Set<String> result = usb.getUnassignments(assignments, subs);

        assertEquals(new HashSet<String>(), result);
    }

    /*
     * @Test public void saveUserAndSubscriptionAssignment_updateUserGroups()
     * throws Exception { // given POUserAndSubscriptions user =
     * preparedForUpdateUserGroups(); List<UserGroup> userGroups =
     * POUserGroupAssembler .toUserGroups(assginedUserGroups);
     * doReturn(userGroups.get(0)).when(usb.ds).getReference(
     * eq(UserGroup.class), eq(1L));
     * doReturn(userGroups.get(1)).when(usb.ds).getReference(
     * eq(UserGroup.class), eq(2L)); // when Response response =
     * usb.saveUserAndSubscriptionAssignment(user, user.getAllGroups()); // then
     * verifyUserAndAssignmentUpdate(response, usb, false);
     * verify(usb.userGroupService, times(1)).assignUserToGroups(eq(pu),
     * eq(Arrays.asList(userGroups.get(0), userGroups.get(1))));
     * verify(usb.userGroupService, times(0)).revokeUserFromGroups(eq(pu),
     * eq(Arrays.asList(assginedUserGroupsInDB.get(1)))); }
     */

    @Test
    public void saveUserAndSubscriptionAssignment() throws Exception {
        POUserAndSubscriptions user = initUserWithAssignments(false, usb, pu);
        doReturn(Boolean.TRUE).when(usb.dc).isUserRoleUpdated(
                anySetOf(UserRoleType.class), any(PlatformUser.class));
        Response response = usb.saveUserAndSubscriptionAssignment(user, null);

        verifyUserAndAssignmentUpdate(response, usb, false);
        verify(usb.isl, times(1)).verifyIdUniquenessAndLdapAttributes(eq(pu),
                any(PlatformUser.class));
        verify(usb.isl, times(1)).setUserRolesInt(eq(user.getAssignedRoles()),
                eq(pu));
        verify(usb.isl, times(1)).notifySubscriptionsAboutUserUpdate(eq(pu));
        verify(usb.isl, times(1)).sendUserUpdatedMail(eq(pu),
                (PlatformUser) notNull());
    }

    @Test(expected = ObjectNotFoundException.class)
    public void saveUserAndSubscriptionAssignment_UserGroupNotExist()
            throws Exception {
        POUserGroup group = new POUserGroup();
        group.setKey(GROUPKEY);
        group.setGroupName(GROUPNAME);
        List<POUserGroup> groups = new ArrayList<POUserGroup>();
        groups.add(group);

        when(usb.ds.getReference(eq(UserGroup.class), eq(GROUPKEY))).thenThrow(
                new ObjectNotFoundException());

        POUserAndSubscriptions user = initUserWithAssignments(false, usb, pu);
        doReturn(Boolean.TRUE).when(usb.dc).isUserRoleUpdated(
                anySetOf(UserRoleType.class), any(PlatformUser.class));
        usb.saveUserAndSubscriptionAssignment(user, groups);
    }

    @Test
    public void saveUserAndSubscriptionAssignment_NoMailSet() throws Exception {
        POUserAndSubscriptions user = initUserWithAssignments(false, usb, pu);
        pu.setEmail(null);

        Response response = usb.saveUserAndSubscriptionAssignment(user, null);

        verifyUserAndAssignmentUpdate(response, usb, false);
        verify(usb.isl, times(1)).verifyIdUniquenessAndLdapAttributes(eq(pu),
                any(PlatformUser.class));
        verify(usb.isl, times(1)).setUserRolesInt(eq(user.getAssignedRoles()),
                eq(pu));
        verify(usb.isl, never()).notifySubscriptionsAboutUserUpdate(eq(pu));
        verify(usb.isl, times(1)).sendUserUpdatedMail(eq(pu),
                eq((PlatformUser) null));
    }

    @Test
    public void saveUserAndSubscriptionAssignment_Suspended() throws Exception {
        POUserAndSubscriptions user = initUserWithAssignments(true, usb, pu);

        Response response = usb.saveUserAndSubscriptionAssignment(user, null);

        verifyUserAndAssignmentUpdate(response, usb, true);
        verify(usb.isl, times(1)).verifyIdUniquenessAndLdapAttributes(eq(pu),
                any(PlatformUser.class));
        verify(usb.isl, times(1)).setUserRolesInt(eq(user.getAssignedRoles()),
                eq(pu));
        verify(usb.isl, never()).notifySubscriptionsAboutUserUpdate(eq(pu));
        verify(usb.isl, times(1)).sendUserUpdatedMail(eq(pu),
                any(PlatformUser.class));
    }

    @Test(expected = ObjectNotFoundException.class)
    public void saveUserAndSubscriptionAssignment_Rollback() throws Exception {
        when(usb.ds.getReference(eq(PlatformUser.class), eq(pu.getKey())))
                .thenThrow(new ObjectNotFoundException());
        POUserAndSubscriptions user = initUserWithAssignments(false, usb, pu);

        try {
            usb.saveUserAndSubscriptionAssignment(user, null);
        } finally {
            verify(usb.sc, times(1)).setRollbackOnly();
            verifyZeroInteractions(usb.dc, usb.isl, usb.isl, usb.lsl,
                    usb.lsmsl, usb.slsl, usb.ssl);
        }
    }

    @Test
    public void sameRole_NoRoleSelected() {
        assertTrue(usb.sameRole(new UsageLicense(), new POSubscription()));
    }

    @Test
    public void sameRole_DifferentRoleSelected() {
        UsageLicense lic = new UsageLicense();
        RoleDefinition rd = new RoleDefinition();
        rd.setRoleId("role1");
        lic.setRoleDefinition(rd);
        POSubscription sub = new POSubscription();
        POServiceRole sr = new POServiceRole();
        sr.setId("role2");
        sub.getUsageLicense().setPoServieRole(sr);

        assertFalse(usb.sameRole(lic, sub));
    }

    @Test
    public void sameRole() {
        UsageLicense lic = new UsageLicense();
        RoleDefinition rd = new RoleDefinition();
        rd.setRoleId("role1");
        lic.setRoleDefinition(rd);
        POSubscription sub = new POSubscription();
        POServiceRole sr = new POServiceRole();
        sr.setId("role1");
        sub.getUsageLicense().setPoServieRole(sr);

        assertTrue(usb.sameRole(new UsageLicense(), new POSubscription()));
    }

    private static void verifyUserAndAssignmentUpdate(Response response,
            UserServiceBean bean, boolean suspended) throws Exception {
        assertNotNull(response);
        if (suspended) {
            assertEquals(1, response.getReturnCodes().size());
            ReturnCode rc = response.getReturnCodes().get(0);
            assertEquals(ReturnType.INFO, rc.getType());
        } else {
            assertTrue(response.getReturnCodes().isEmpty());
        }
        verifyZeroInteractions(bean.sc);
    }

    private static void verifyAddRevokeCall(String subId,
            List<VOUsageLicense> lics, List<VOUser> users, PlatformUser pu) {
        if (subId.equals("sub3")) {
            // user revoked from subscription
            assertNull(lics);
            assertNotNull(users);
            assertEquals(1, users.size());
            assertEquals(pu.getUserId(), users.get(0).getUserId());
        } else if (subId.equals("sub2")) {
            // service role changed
            assertNull(users);
            assertNotNull(lics);
            assertEquals(1, lics.size());
            VOUsageLicense lic = lics.get(0);
            assertEquals(pu.getUserId(), lic.getUser().getUserId());
            assertEquals("role1", lic.getRoleDefinition().getRoleId());
        } else if (subId.equals("sub1")) {
            // user added to subscription
            assertNull(users);
            assertNotNull(lics);
            assertEquals(1, lics.size());
            VOUsageLicense lic = lics.get(0);
            assertEquals(pu.getUserId(), lic.getUser().getUserId());
            assertNull(lic.getRoleDefinition());
        } else {
            fail("Invalid call to addRevokeUser");
        }
    }

    private static POUserAndSubscriptions initUserWithAssignments(
            final boolean suspend, UserServiceBean bean, final PlatformUser pu)
            throws Exception {
        POUserAndSubscriptions user = new POUserAndSubscriptions();
        populateUserDetails(pu, user);
        user.setAssignedRoles(EnumSet.of(UserRoleType.ORGANIZATION_ADMIN));
        user.setSubscriptions(createSubscriptions());

        when(
                bean.slsl.getSubscriptionAssignments(any(PlatformUser.class),
                        anySetOf(SubscriptionStatus.class))).thenReturn(
                createAssignments());
        when(
                Boolean.valueOf(bean.ssl.addRevokeUser(anyString(),
                        anyListOf(VOUsageLicense.class),
                        anyListOf(VOUser.class)))).thenAnswer(
                new Answer<Boolean>() {

                    @Override
                    @SuppressWarnings("unchecked")
                    public Boolean answer(InvocationOnMock invocation)
                            throws Throwable {
                        String subId = (String) invocation.getArguments()[0];
                        List<VOUsageLicense> lics = (List<VOUsageLicense>) invocation
                                .getArguments()[1];
                        List<VOUser> users = (List<VOUser>) invocation
                                .getArguments()[2];
                        verifyAddRevokeCall(subId, lics, users, pu);
                        return Boolean.valueOf(!suspend);
                    }
                });
        return user;
    }

    private static void verifyAssignment(
            Map<String, VOUsageLicense> assignments, VOUser u) {
        assertEquals(2, assignments.size());

        VOUsageLicense lic = assignments.get("sub1");
        assertSame(u, lic.getUser());
        assertNull(lic.getRoleDefinition());

        lic = assignments.get("sub2");
        assertSame(u, lic.getUser());
        assertNotNull(lic.getRoleDefinition());
        assertEquals("role1", lic.getRoleDefinition().getRoleId());
    }

    private static List<UsageLicense> createAssignments() {
        List<UsageLicense> result = new ArrayList<UsageLicense>();

        Subscription sub = new Subscription();
        sub.setSubscriptionId("sub2");
        RoleDefinition rd = new RoleDefinition();
        rd.setRoleId("role2");
        UsageLicense lic = new UsageLicense();
        lic.setUser(new PlatformUser());
        lic.setSubscription(sub);
        lic.setRoleDefinition(rd);
        result.add(lic);

        sub = new Subscription();
        sub.setSubscriptionId("sub3");
        lic = new UsageLicense();
        lic.setUser(new PlatformUser());
        lic.setSubscription(sub);
        lic.setRoleDefinition(null);
        result.add(lic);

        return result;
    }

    private static List<POSubscription> createSubscriptions() {
        List<POSubscription> result = new ArrayList<POSubscription>();

        POSubscription sub = new POSubscription();
        sub.setId("sub1");
        sub.setKey(10);
        sub.setAssigned(true);
        result.add(sub);

        sub = new POSubscription();
        sub.setId("sub2");
        sub.setKey(20);
        sub.setAssigned(true);
        POServiceRole role = new POServiceRole();
        role.setId("role1");
        role.setKey(5);
        sub.getUsageLicense().setPoServieRole(role);
        result.add(sub);

        return result;
    }

    private static List<SubscriptionWithRoles> initSubsWithRoles() {
        List<SubscriptionWithRoles> result = new ArrayList<SubscriptionWithRoles>();

        SubscriptionWithRoles swr = new SubscriptionWithRoles();
        Subscription sub = new Subscription();
        swr.setSubscription(sub);
        result.add(swr);

        swr = new SubscriptionWithRoles();
        sub = new Subscription();
        swr.setSubscription(sub);

        RoleDefinition rd = new RoleDefinition();
        rd.setKey(10);
        swr.getRoles().add(rd);
        rd = new RoleDefinition();
        rd.setKey(11);
        swr.getRoles().add(rd);
        result.add(swr);

        swr = new SubscriptionWithRoles();
        sub = new Subscription();
        swr.setSubscription(sub);

        rd = new RoleDefinition();
        rd.setKey(10);
        swr.getRoles().add(rd);
        rd = new RoleDefinition();
        rd.setKey(11);
        swr.getRoles().add(rd);
        result.add(swr);

        swr = new SubscriptionWithRoles();
        sub = new Subscription();
        swr.setSubscription(sub);

        rd = new RoleDefinition();
        rd.setKey(20);
        swr.getRoles().add(rd);
        rd = new RoleDefinition();
        rd.setKey(21);
        swr.getRoles().add(rd);
        result.add(swr);

        return result;
    }

    private static POUser createPOUser(PlatformUser pu) {
        POUser user = new POUser();
        user.setUserId("userId");
        user.setKey(pu.getKey());
        user.setVersion(0);
        user.setEmail("mail@host.cc");
        user.setFirstName("firstName");
        user.setLastName("lastName");
        return user;
    }

    private static POUserDetails createPOUserDetails(PlatformUser pu) {
        POUserDetails user = new POUserDetails();
        populateUserDetails(pu, user);
        return user;
    }

    private static void populateUserDetails(PlatformUser pu, POUserDetails user) {
        user.setUserId("userId");
        user.setKey(pu.getKey());
        user.setVersion(0);
        user.setEmail("mail@host.cc");
        user.setFirstName("firstName");
        user.setLastName("lastName");
        user.setLocale("en");
        user.setSalutation(Salutation.MR);
    }

    private List<UserGroup> createAssginedGroupsInDB() {
        List<UserGroup> userGroupsInDB = new ArrayList<UserGroup>();
        UserGroup userGroupInDB = new UserGroup();
        userGroupInDB.setIsDefault(false);
        userGroupInDB.setKey(2);
        userGroupsInDB.add(userGroupInDB);

        userGroupInDB = new UserGroup();
        userGroupInDB.setIsDefault(false);
        userGroupInDB.setKey(3);
        userGroupsInDB.add(userGroupInDB);

        return userGroupsInDB;
    }

    private List<POUserGroup> createAssignedGroups() {
        List<POUserGroup> result = new ArrayList<POUserGroup>();
        POUserGroup userGroup = new POUserGroup();
        userGroup.setDefault(false);
        userGroup.setKey(1);
        userGroup.setGroupDescription("");
        userGroup.setGroupReferenceId("");
        userGroup.setGroupName("test1");
        userGroup.setSelectedRole(UnitRoleType.USER.name());
        result.add(userGroup);

        userGroup = new POUserGroup();
        userGroup.setDefault(false);
        userGroup.setKey(2);
        userGroup.setGroupDescription("");
        userGroup.setGroupReferenceId("");
        userGroup.setGroupName("test2");
        userGroup.setSelectedRole(UnitRoleType.USER.name());
        result.add(userGroup);

        return result;
    }

    private POUserAndSubscriptions preparedForUpdateUserGroups()
            throws Exception {
        assginedUserGroupsInDB = createAssginedGroupsInDB();
        assginedUserGroups = createAssignedGroups();
        POUserAndSubscriptions user = initUserWithAssignments(false, usb, pu);
        user.setGroupsToBeAssigned(assginedUserGroups);
        user.setAllGroups(assginedUserGroups);
        doReturn(Boolean.TRUE).when(usb.dc).isUserRoleUpdated(
                anySetOf(UserRoleType.class), any(PlatformUser.class));
        doReturn(assginedUserGroupsInDB).when(usb.userGroupService)
                .getUserGroupsForUserWithoutDefault(pu.getKey());

        return user;
    }

}
