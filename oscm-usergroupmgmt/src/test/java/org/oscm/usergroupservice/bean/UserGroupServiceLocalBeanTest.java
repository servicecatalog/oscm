/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2014-6-27                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.usergroupservice.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.SessionContext;
import javax.persistence.Query;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.oscm.communicationservice.data.SendMailStatus;
import org.oscm.communicationservice.local.CommunicationServiceLocal;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.DomainObject;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.RoleAssignment;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.UnitRoleAssignment;
import org.oscm.domobjects.UnitUserRole;
import org.oscm.domobjects.UserGroup;
import org.oscm.domobjects.UserGroupToInvisibleProduct;
import org.oscm.domobjects.UserGroupToUser;
import org.oscm.domobjects.UserRole;
import org.oscm.internal.intf.IdentityService;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.types.enumtypes.UnitRoleType;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.DeletingUnitWithSubscriptionsNotPermittedException;
import org.oscm.internal.types.exception.IllegalArgumentException;
import org.oscm.internal.types.exception.MailOperationException;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.UserRoleAssignmentException;
import org.oscm.internal.vo.VOUser;
import org.oscm.paginator.Pagination;
import org.oscm.subscriptionservice.local.SubscriptionListServiceLocal;
import org.oscm.taskhandling.local.TaskQueueServiceLocal;
import org.oscm.test.stubs.QueryStub;
import org.oscm.types.enumtypes.EmailType;
import org.oscm.usergroupservice.auditlog.UserGroupAuditLogCollector;
import org.oscm.usergroupservice.dao.UserGroupDao;

/**
 * Unit tests for the user group management.
 * 
 * @author fangzhongwei
 * 
 */
public class UserGroupServiceLocalBeanTest {
    private UserGroupServiceLocalBean userGroupService;
    private PlatformUser user;
    private PlatformUser platformUser;
    private Organization org;
    private Organization organization;
    private UserGroup group;
    private Product product;
    private Map<UserGroup, UnitUserRole> groupsWithRoles;
    private UnitUserRole unitUserRole;
    private static final long groupKey = 1000L;
    private static final String USER_ID = "userId";
    private static final String TESTGROUPNAME = "test";
    private static final String MARKETPLACEID = "marketplaceId";
    private static final long productKey = 12345L;
    private static final long invProductKey = 54321L;
    private static final long existingInvProductKey = 2L;

    @Before
    public void setup() throws Exception {
        userGroupService = new UserGroupServiceLocalBean();
        user = new PlatformUser();
        platformUser = new PlatformUser();
        org = new Organization();
        organization = new Organization();
        product = new Product();
        product.setKey(1000L);

        initUserGroupData();

        org.setOrganizationId("orgId1");
        org.setKey(1L);
        user.setKey(1L);
        user.setUserId(USER_ID);
        user.setOrganization(org);

        organization.setOrganizationId("orgId2");
        org.setKey(2L);
        group.setOrganization(org);
        platformUser.setOrganization(organization);

        groupsWithRoles = new HashMap<UserGroup, UnitUserRole>();
        unitUserRole = new UnitUserRole();
        unitUserRole.setKey(1L);
        unitUserRole.setRoleName(UnitRoleType.ADMINISTRATOR);

        userGroupService.setDm(mock(DataService.class));
        userGroupService.audit = mock(UserGroupAuditLogCollector.class);
        userGroupService.setUserGroupDao(mock(UserGroupDao.class));
        userGroupService.setCs(mock(CommunicationServiceLocal.class));
        userGroupService.setSessionCtx(mock(SessionContext.class));
        IdentityService identityService = mock(IdentityService.class);
        userGroupService.setIs(identityService);
        userGroupService.slsl = mock(SubscriptionListServiceLocal.class);
        userGroupService.tqs = mock(TaskQueueServiceLocal.class);

        when(userGroupService.getDm().find(any(DomainObject.class)))
                .thenAnswer(new Answer<DomainObject<?>>() {

                    @Override
                    public DomainObject<?> answer(InvocationOnMock invocation) {
                        Object template = invocation.getArguments()[0];
                        if (template instanceof UserGroup) {
                            return group;
                        } else if (template instanceof PlatformUser) {
                            return user;
                        }
                        return null;
                    }
                });
        doReturn(user).when(userGroupService.getDm()).find(
                any(PlatformUser.class));
        when(
                userGroupService.getDm().getReference(UserGroup.class,
                        group.getKey())).thenReturn(group);
        when(userGroupService.getDm().getCurrentUser()).thenReturn(user);

        doNothing().when(userGroupService.getDm()).persist(
                any(DomainObject.class));
        doReturn(new SendMailStatus<PlatformUser>()).when(
                userGroupService.getCs()).sendMail(any(EmailType.class),
                any(Object[].class), any(Marketplace.class),
                any(PlatformUser[].class));
    }

    @Test
    public void testGrantUserRolesWithHandleUnitAdminRole()
            throws OperationNotPermittedException, ObjectNotFoundException {
        // given
        PlatformUser user = prepareUserWithRoleForTest(UserRoleType.SUBSCRIPTION_MANAGER);

        List<UnitRoleType> roleTypes = Collections
                .singletonList(UnitRoleType.ADMINISTRATOR);
        UserGroupToUser userGroupToUser = new UserGroupToUser();

        userGroupToUser.setUserGroup(group);
        group.setOrganization(org);

        List<UserGroup> groups = new ArrayList<>(10);
        groups.add(group);
        Map<UserGroup, UnitRoleType> allUserAssignments = new HashMap<>();
        allUserAssignments.put(group, UnitRoleType.ADMINISTRATOR);

        doReturn(user).when(userGroupService.getDm())
                .getReferenceByBusinessKey(any(DomainObject.class));

        doReturn(user).when(userGroupService.getDm()).find(
                any(PlatformUser.class));

        doReturn(userGroupToUser).when(userGroupService.getUserGroupDao())
                .getUserGroupAssignment(group, user);
        userGroupService = spy(userGroupService);
        doReturn(allUserAssignments).when(userGroupService)
                .getUserGroupsForUserWithRoles(anyString());

        // when
        userGroupService.grantUserRolesWithHandleUnitAdminRole(user, roleTypes,
                group);

        // then
        verify(userGroupService.getIs(), times(1)).grantUnitRole(
                any(VOUser.class), any(UserRoleType.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void createUserGroup_nullUserGroup() throws ObjectNotFoundException,
            NonUniqueBusinessKeyException, MailOperationException,
            OperationNotPermittedException {
        // when
        userGroupService.createUserGroup(null, null, null, null);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void createUserGroup_nullPlatformUser()
            throws ObjectNotFoundException, OperationNotPermittedException,
            MailOperationException, NonUniqueBusinessKeyException {
        // given
        when(userGroupService.getDm().find(user)).thenReturn(null);
        product.setStatus(ServiceStatus.ACTIVE);
        when(userGroupService.getDm().getReference(Product.class, 1L))
                .thenReturn(product);
        // when
        userGroupService.createUserGroup(group, null, null, MARKETPLACEID);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void createUserGroup_defaultUserGroup()
            throws ObjectNotFoundException, NonUniqueBusinessKeyException,
            MailOperationException, OperationNotPermittedException {
        // given
        group.setIsDefault(true);

        // when
        userGroupService.createUserGroup(group, null, null, MARKETPLACEID);
    }

    @Test
    public void createUserGroup() throws ObjectNotFoundException,
            OperationNotPermittedException, MailOperationException,
            NonUniqueBusinessKeyException {
        // given
        group.setIsDefault(false);
        product.setStatus(ServiceStatus.ACTIVE);
        when(userGroupService.getDm().getReference(Product.class, 1L))
                .thenReturn(product);

        // when
        UserGroup result = userGroupService.createUserGroup(group, null, null,
                MARKETPLACEID);

        // then
        assertEquals(result.getKey(), group.getKey());
        verify(userGroupService.getDm(), times(1)).flush();
        verify(userGroupService.getDm(), times(3)).persist(
                any(DomainObject.class));
    }

    @Test(expected = OperationNotPermittedException.class)
    public void createUserGroup_ServiceNotActive()
            throws ObjectNotFoundException, NonUniqueBusinessKeyException,
            MailOperationException, OperationNotPermittedException {
        // given
        List<Product> visProds = givenVisibleProducts();
        visProds.get(0).setStatus(ServiceStatus.INACTIVE);
        group.setIsDefault(false);

        // when
        userGroupService.createUserGroup(group, visProds, null, MARKETPLACEID);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void updateUserGroup_GroupNotExists()
            throws ObjectNotFoundException, OperationNotPermittedException,
            MailOperationException, NonUniqueBusinessKeyException,
            UserRoleAssignmentException {
        // given
        List<Product> visProds = givenVisibleProducts();
        List<Product> invisProds = givenInvisibleProducts();
        group.setIsDefault(false);
        when(
                userGroupService.getDm().getReference(eq(UserGroup.class),
                        eq(group.getKey()))).thenThrow(
                new ObjectNotFoundException());

        // when
        userGroupService.updateUserGroup(group, visProds, invisProds,
                MARKETPLACEID, givenPlatformUserMap(),
                new ArrayList<PlatformUser>(), givenPlatformUserMap());
    }

    @Test(expected = OperationNotPermittedException.class)
    public void updateUserGroup_ServiceNotActive()
            throws ObjectNotFoundException, NonUniqueBusinessKeyException,
            MailOperationException, OperationNotPermittedException,
            UserRoleAssignmentException {
        // given
        List<Product> visProds = givenVisibleProducts();
        List<Product> invisProds = givenInvisibleProducts();
        visProds.get(0).setStatus(ServiceStatus.INACTIVE);

        // when
        userGroupService.updateUserGroup(group, visProds, invisProds,
                MARKETPLACEID, givenPlatformUserMap(),
                new ArrayList<PlatformUser>(), givenPlatformUserMap());
    }

    @Test(expected = IllegalArgumentException.class)
    public void deleteUserGroup_nullUserGroup()
            throws OperationNotPermittedException, MailOperationException,
            ObjectNotFoundException,
            DeletingUnitWithSubscriptionsNotPermittedException {
        // when
        userGroupService.deleteUserGroup((UserGroup) null);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void deleteUserGroup_defaultUserGroup()
            throws OperationNotPermittedException, MailOperationException,
            ObjectNotFoundException,
            DeletingUnitWithSubscriptionsNotPermittedException {
        // given
        group.setIsDefault(true);

        // when
        userGroupService.deleteUserGroup(group);
    }

    @Test
    public void deleteUserGroup() throws OperationNotPermittedException,
            MailOperationException, ObjectNotFoundException,
            DeletingUnitWithSubscriptionsNotPermittedException {
        // given
        group.setIsDefault(false);

        // when
        boolean result = userGroupService.deleteUserGroup(group);

        // then
        assertEquals(Boolean.FALSE, Boolean.valueOf(result));
        verify(userGroupService.getDm(), times(1)).remove(
                any(DomainObject.class));
    }

    @Test
    public void bug12139_untieSubscriptionsFromUserGroup()
            throws ObjectNotFoundException, OperationNotPermittedException,
            MailOperationException,
            DeletingUnitWithSubscriptionsNotPermittedException {
        // given
        Subscription sub = mock(Subscription.class);
        List<Subscription> list = new ArrayList<Subscription>();
        list.add(sub);
        group.setSubscriptions(list);
        // when
        userGroupService.deleteUserGroup(group);
        // then
        assertTrue(sub.getUserGroup() == null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateUserGroup_nullUserGroup() throws ObjectNotFoundException,
            NonUniqueBusinessKeyException, MailOperationException,
            OperationNotPermittedException, UserRoleAssignmentException {
        // when
        userGroupService.updateUserGroup(null, null, null, MARKETPLACEID,
                givenPlatformUserMap(), givenPlatformUserList(),
                givenPlatformUserMap());
    }

    @Test(expected = OperationNotPermittedException.class)
    public void updateUserGroup_defaultUserGroup()
            throws ObjectNotFoundException, OperationNotPermittedException,
            MailOperationException, NonUniqueBusinessKeyException,
            UserRoleAssignmentException {
        // given
        UserGroup userGroup = new UserGroup();
        userGroup.setIsDefault(true);
        userGroup.setName("Name");
        product.setStatus(ServiceStatus.ACTIVE);
        when(userGroupService.getDm().getReference(eq(Product.class), eq(1L)))
                .thenReturn(product);
        when(
                userGroupService.getDm().getReference(UserGroup.class,
                        group.getKey())).thenReturn(userGroup);

        // when
        userGroupService.updateUserGroup(group, null, null, MARKETPLACEID,
                givenPlatformUserMap(), givenPlatformUserList(),
                givenPlatformUserMap());

        // then
        verify(userGroupService.getCs(), never()).sendMail(
                any(EmailType.class), any(Object[].class),
                any(Marketplace.class), any(PlatformUser[].class));
    }

    @Test
    public void updateUserGroup_WithOldGroupNameForAssigning()
            throws ObjectNotFoundException, OperationNotPermittedException,
            MailOperationException, NonUniqueBusinessKeyException,
            UserRoleAssignmentException {
        // given
        UserGroup userGroup = givenNewUserGroup();
        doReturn(givenMarketplace()).when(userGroupService.getDm())
                .getReferenceByBusinessKey(any(Marketplace.class));
        userGroup.setName("group");
        UserGroupToUser ugtu = new UserGroupToUser();
        ugtu.setUserGroup(userGroup);
        // when

        userGroupService.setUserGroupDao(mock(UserGroupDao.class));

        when(
                userGroupService.getDm().getReference(PlatformUser.class,
                        user.getKey())).thenReturn(user);
        when(
                userGroupService.getUserGroupDao().getUserGroupAssignment(
                        any(UserGroup.class), any(PlatformUser.class)))
                .thenReturn(ugtu);

        when(
                userGroupService.slsl
                        .getSubscriptionsForOwner(any(PlatformUser.class)))
                .thenReturn(Collections.EMPTY_LIST);

        doReturn(new UnitRoleAssignment()).when(userGroupService.getDm())
                .getReferenceByBusinessKey(any(UnitRoleAssignment.class));

        userGroupService.updateUserGroup(userGroup, new ArrayList<Product>(),
                new ArrayList<Product>(), MARKETPLACEID,
                Collections.singletonMap(user, "USER"),
                new ArrayList<PlatformUser>(),
                new HashMap<PlatformUser, String>());

        // then
        verify(userGroupService.getCs(), times(1)).sendMail(
                EmailType.GROUP_USER_ASSIGNED, new Object[] { "group" }, null,
                user);
        verify(userGroupService.getCs(), times(0)).sendMail(
                EmailType.USER_GROUP_UPDATED, new Object[] { "group" }, null,
                user);
    }

    @Test
    public void updateUserGroup_WithOldGroupNameForRevoking() throws Exception {
        // given
        UserGroup userGroup = givenNewUserGroup();
        doReturn(givenMarketplace()).when(userGroupService.getDm())
                .getReferenceByBusinessKey(any(Marketplace.class));
        userGroup.setName("group");
        // when
        userGroupService.updateUserGroup(userGroup, new ArrayList<Product>(),
                new ArrayList<Product>(), MARKETPLACEID,
                new HashMap<PlatformUser, String>(),
                Collections.singletonList(user),
                new HashMap<PlatformUser, String>());

        // then
        verify(userGroupService.getCs(), times(1)).sendMail(
                EmailType.GROUP_USER_REVOKED, new Object[] { TESTGROUPNAME },
                null, user);
        verify(userGroupService.getCs(), times(0)).sendMail(
                EmailType.USER_GROUP_UPDATED, new Object[] { TESTGROUPNAME },
                null, user);
    }

    @Test
    public void updateUserGroup() throws Exception {
        // given
        List<Product> visProds = givenVisibleProducts();
        List<Product> invisProds = givenInvisibleProducts();

        UserGroup userGroup = givenNewUserGroup();
        UserGroupToUser ugtu = new UserGroupToUser();
        ugtu.setUserGroup(userGroup);

        when(
                userGroupService.getDm().getReference(eq(Product.class),
                        eq(productKey))).thenReturn(visProds.get(0));
        when(
                userGroupService.getDm().getReference(eq(Product.class),
                        eq(invProductKey))).thenReturn(invisProds.get(0));
        when(
                userGroupService.getDm().getReference(PlatformUser.class,
                        user.getKey())).thenReturn(user);
        when(
                userGroupService.getUserGroupDao().getUserGroupAssignment(
                        any(UserGroup.class), any(PlatformUser.class)))
                .thenReturn(ugtu);

        Product existingInvisibleProduct = userGroup.getInvisibleProducts()
                .get(0);
        when(
                userGroupService.getDm().getReference(eq(Product.class),
                        eq(existingInvProductKey))).thenReturn(
                existingInvisibleProduct);
        doReturn(givenMarketplace()).when(userGroupService.getDm())
                .getReferenceByBusinessKey(any(Marketplace.class));
        doReturn(new UnitRoleAssignment()).when(userGroupService.getDm())
                .getReferenceByBusinessKey(any(UnitRoleAssignment.class));

        when(
                userGroupService.slsl
                        .getSubscriptionsForOwner(any(PlatformUser.class)))
                .thenReturn(Collections.EMPTY_LIST);
        // when
        UserGroup result = userGroupService.updateUserGroup(userGroup,
                visProds, invisProds, MARKETPLACEID, givenPlatformUserMap(),
                new ArrayList<PlatformUser>(),
                new HashMap<PlatformUser, String>());

        // then
        assertEquals(group.getKey(), result.getKey());
        verify(userGroupService.getDm(), times(4)).flush();
        verify(userGroupService.getDm(), times(2)).refresh(
                any(DomainObject.class));
        verify(userGroupService.getDm(), times(3)).persist(
                any(DomainObject.class));
    }

    @Test
    public void getUserGroupDetails() throws Exception {
        // given
        group.setIsDefault(false);

        // when
        userGroupService.getUserGroupDetails(groupKey);

        // then
        verify(userGroupService.getUserGroupDao(), times(1))
                .getUserGroupDetails(eq(groupKey));
    }

    @Test
    public void getInvisibleProductKeysForUser_FromDefaultGroup()
            throws Exception {
        // given
        doReturn(user).when(userGroupService.getDm()).getReference(
                eq(PlatformUser.class), eq(user.getKey()));
        group.setIsDefault(true);
        user.getOrganization().getUserGroups().add(group);

        // when
        userGroupService.getInvisibleProductKeysForUser(user.getKey());

        // then
        verify(userGroupService.getUserGroupDao(), times(1))
                .getInvisibleProductKeysForGroup(eq(group.getKey()));
    }

    @Test
    public void getUserGroupsForOrganization() throws Exception {
        // given
        group.setIsDefault(false);

        // when
        userGroupService.getUserGroupsForOrganization();

        // then
        verify(userGroupService.getUserGroupDao(), times(1))
                .getUserGroupsForOrganization();
    }

    @Test
    public void getUserGroupsForOrganizationWithoutDefault() throws Exception {
        // given
        group.setIsDefault(false);

        // when
        userGroupService.getUserGroupsForOrganizationWithoutDefault();

        // then
        verify(userGroupService.getUserGroupDao(), times(1))
                .getUserGroupsForOrganizationWithoutDefault();
    }

    @Test
    public void getUserGroupsForUserWithoutDefault() throws Exception {
        // given
        group.setIsDefault(false);

        // when
        userGroupService.getUserGroupsForUserWithoutDefault(1L);

        // then
        verify(userGroupService.getUserGroupDao(), times(1))
                .getUserGroupsForUserWithoutDefault(eq(1L));
    }

    @Test
    public void getUserGroupsForUser() throws Exception {
        // given
        group.setIsDefault(false);

        // when
        userGroupService.getUserGroupsForUser("userId");

        // then
        verify(userGroupService.getUserGroupDao(), times(1))
                .getUserGroupsForUser("userId");
    }

    @Test
    public void getUserGroupsForUserWithRoles() throws Exception {
        // given
        group.setIsDefault(false);

        // when
        userGroupService.getUserGroupsForUserWithRoles("userId");

        // then
        verify(userGroupService.getUserGroupDao(), times(1))
                .getUserGroupsForUser(anyString());
    }

    @Test
    public void assignUsersToGroup_OK() throws Exception {
        // given
        group.setIsDefault(false);
        group.setOrganization(org);

        doReturn(user).when(userGroupService.getDm()).find(
                any(PlatformUser.class));
        doReturn(user).when(userGroupService.getDm()).getReference(
                PlatformUser.class, 1L);

        UserGroupToUser ugu = new UserGroupToUser();
        ugu.setUserGroup(group);
        ugu.setPlatformuser(user);

        doReturn(ugu).when(userGroupService.getUserGroupDao())
                .getUserGroupAssignment(any(UserGroup.class),
                        any(PlatformUser.class));

        // when
        userGroupService.assignUsersToGroup(group,
                Collections.singletonList(user));

        // then
        verify(userGroupService.getDm(), times(2)).flush();
        verify(userGroupService.getDm(), times(2)).persist(
                any(DomainObject.class));
    }

    @Test(expected = ObjectNotFoundException.class)
    public void assignUsersToGroup_nullPlatformUser() throws Exception {
        // given
        group.setIsDefault(false);
        group.setOrganization(org);
        when(userGroupService.getDm().find(user)).thenReturn(null);

        // when
        userGroupService.assignUsersToGroup(group,
                Collections.singletonList(user));
    }

    @Test
    public void revokeUsersFromGroup_OK() throws Exception {
        // given
        group.setIsDefault(false);
        group.setOrganization(org);
        // when
        userGroupService.revokeUsersFromGroup(group,
                Collections.singletonList(user));

        // then
        verify(userGroupService.getDm(), times(1)).flush();
    }

    @Test(expected = OperationNotPermittedException.class)
    public void revokeUsersFromGroup_defaultGroup() throws Exception {
        // given
        group.setIsDefault(true);

        // when
        userGroupService.revokeUsersFromGroup(group,
                Collections.singletonList(user));
    }

    @Test(expected = OperationNotPermittedException.class)
    public void revokeUsersFromGroup_groupNotBelongToOrg() throws Exception {
        // given
        group.setIsDefault(false);
        group.setOrganization(organization);
        when(userGroupService.getDm().find(any(UserGroup.class))).thenReturn(
                null);

        // when
        userGroupService.revokeUsersFromGroup(group,
                Collections.singletonList(user));
    }

    @Test(expected = OperationNotPermittedException.class)
    public void revokeUsersFromGroup_userNotBelongToOrg() throws Exception {
        // given
        group.setIsDefault(false);
        group.setOrganization(org);
        doReturn(platformUser).when(userGroupService.getDm()).find(
                any(PlatformUser.class));
        when(userGroupService.getDm().find(any(DomainObject.class)))
                .thenAnswer(new Answer<DomainObject<?>>() {

                    @Override
                    public DomainObject<?> answer(InvocationOnMock invocation) {
                        Object template = invocation.getArguments()[0];
                        if (template instanceof UserGroup) {
                            return group;
                        } else if (template instanceof PlatformUser) {
                            return platformUser;
                        }
                        return null;
                    }
                });

        // when
        userGroupService.revokeUsersFromGroup(group,
                Collections.singletonList(user));
    }

    @Test
    public void assignUserToGroups_OK() throws Exception {
        // given
        group.setIsDefault(false);
        groupsWithRoles.clear();
        groupsWithRoles.put(group, unitUserRole);
        // when
        userGroupService.assignUserToGroups(user, groupsWithRoles);

        // then
        verify(userGroupService.getDm(), times(1)).flush();
    }

    @Test
    public void assignUserToGroups_onlyDefaultGroup() throws Exception {
        // given
        group.setIsDefault(true);
        groupsWithRoles.clear();
        groupsWithRoles.put(group, unitUserRole);
        // when
        userGroupService.assignUserToGroups(user, groupsWithRoles);

        // then
        verify(userGroupService.getDm(), times(1)).flush();
        verify(userGroupService.getCs(), times(0)).sendMail(
                EmailType.GROUP_USER_ASSIGNED, new Object[] { TESTGROUPNAME },
                null, user);
    }

    @Test
    public void assignUserToGroups_TwoGroupsIncludingDefaultGroup()
            throws Exception {
        // given
        group.setIsDefault(true);
        group.setKey(5L);
        UserGroup newUserGroup = givenNewUserGroup();
        groupsWithRoles.clear();
        groupsWithRoles.put(group, unitUserRole);
        groupsWithRoles.put(newUserGroup, unitUserRole);

        // when
        userGroupService.assignUserToGroups(user, groupsWithRoles);

        // then
        verify(userGroupService.getDm(), times(1)).flush();
        verify(userGroupService.getTqs(), times(1)).sendAllMessages(
                any(List.class));

    }

    @Test
    public void assignUserToGroups_oneNoneDefaultGroup() throws Exception {
        // given
        UserGroup newUserGroup = givenNewUserGroup();
        groupsWithRoles.clear();
        group.setKey(657656L);
        groupsWithRoles.put(group, unitUserRole);
        groupsWithRoles.put(newUserGroup, unitUserRole);
        // when
        userGroupService.assignUserToGroups(user, groupsWithRoles);

        // then

        verify(userGroupService.getDm(), times(1)).flush();
        verify(userGroupService.getTqs(), times(1)).sendAllMessages(
                any(List.class));
    }

    @Test
    public void revokeUserFromGroups_OK() throws Exception {
        // given
        group.setIsDefault(false);
        group.setOrganization(org);
        // when
        userGroupService.revokeUserFromGroups(user,
                Collections.singletonList(group));

        // then
        verify(userGroupService.getDm(), times(1)).flush();
    }

    @Test(expected = OperationNotPermittedException.class)
    public void revokeUserFromGroups_defaultGroup() throws Exception {
        // given
        group.setIsDefault(true);

        // when
        userGroupService.revokeUserFromGroups(user,
                Collections.singletonList(group));
    }

    @Test(expected = OperationNotPermittedException.class)
    public void revokeUserFromGroups_groupNotBelongToOrg() throws Exception {
        // given
        group.setIsDefault(false);
        group.setOrganization(organization);
        when(userGroupService.getDm().find(any(PlatformUser.class)))
                .thenAnswer(new Answer<DomainObject<?>>() {

                    @Override
                    public DomainObject<?> answer(InvocationOnMock invocation) {
                        Object template = invocation.getArguments()[0];
                        if (template instanceof UserGroup) {
                            return null;
                        } else if (template instanceof PlatformUser) {
                            return platformUser;
                        }
                        return null;
                    }
                });

        // when
        userGroupService.revokeUserFromGroups(user,
                Collections.singletonList(group));
    }

    @Test(expected = OperationNotPermittedException.class)
    public void revokeUserFromGroups_userNotBelongToOrg() throws Exception {
        // given
        group.setIsDefault(false);
        doReturn(platformUser).when(userGroupService.getDm()).find(
                any(PlatformUser.class));
        when(userGroupService.getDm().find(any(DomainObject.class)))
                .thenAnswer(new Answer<DomainObject<?>>() {

                    @Override
                    public DomainObject<?> answer(InvocationOnMock invocation) {
                        Object template = invocation.getArguments()[0];
                        if (template instanceof UserGroup) {
                            return group;
                        } else if (template instanceof PlatformUser) {
                            return platformUser;
                        }
                        return null;
                    }
                });

        // when
        userGroupService.revokeUserFromGroups(user,
                Collections.singletonList(group));
    }

    @Test
    public void getUserCountForGroup() throws Exception {
        // when
        userGroupService
                .getUserCountForGroup(group.getKey(), group.isDefault());

        // then
        verify(userGroupService.getUserGroupDao(), times(1))
                .getUserCountForGroup(eq(group.getKey()));
    }

    @Test
    public void getUserCountForGroup_defaultGroup() throws Exception {
        // given
        UserGroup userGroup = new UserGroup();
        userGroup.setIsDefault(true);
        userGroup.setKey(10L);
        doReturn(userGroup).when(userGroupService.getDm()).getReference(
                eq(UserGroup.class), eq(10L));
        // when
        userGroupService.getUserCountForGroup(userGroup.getKey(),
                userGroup.isDefault());

        // then
        verify(userGroupService.getUserGroupDao(), times(1))
                .getUserCountForDefaultGroup(anyString());
    }

    @Test
    public void getAssignedUserIdsForGroup() throws Exception {
        // when
        userGroupService.getAssignedUserIdsForUserGroup(group.getKey());

        // then
        verify(userGroupService.getUserGroupDao(), times(1))
                .getAssignedUserIdsForGroup(eq(group.getKey()));
    }

    @Test
    public void getOrganizationalUnitsNullParam()
            throws OperationNotPermittedException {
        // given
        List<UserGroup> users = spy(new ArrayList<UserGroup>());
        users.add(new UserGroup());
        doReturn(users).when(userGroupService.getUserGroupDao())
                .getUserGroupsForOrganization();
        // when
        List<UserGroup> result = userGroupService.getOrganizationalUnits(null);

        // then
        assertEquals(users.size(), result.size());
    }

    @Test
    public void getOrganizationalUnits() throws OperationNotPermittedException {
        // given
        int limit = 1;
        int offset = 0;

        Pagination pagination = new Pagination(offset, limit);
        List<UserGroup> users = spy(new ArrayList<UserGroup>());

        users.add(new UserGroup());
        users.add(new UserGroup());

        doReturn(users.subList(0, 1)).when(userGroupService.getUserGroupDao())
                .getUserGroupsForOrganization(pagination);
        // when
        List<UserGroup> result = userGroupService
                .getOrganizationalUnits(pagination);

        // then
        assertEquals(1, result.size());
    }

    @Test
    public void createUnit() throws OperationNotPermittedException,
            NonUniqueBusinessKeyException {

        // given
        final String unitName = "TestUnit";
        final String unitDesc = "UnitDesc";
        final String unitRefId = "refId";

        // when
        UserGroup group = userGroupService.createUserGroup(unitName, unitDesc,
                unitRefId);

        // then
        verify(userGroupService.getDm(), times(1))
                .persist(any(UserGroup.class));
        verify(userGroupService.getDm(), times(1)).flush();

        assertEquals(unitName, group.getName());
        assertEquals(unitDesc, group.getDescription());
        assertEquals(unitRefId, group.getReferenceId());
    }

    @Test(expected = IllegalArgumentException.class)
    public void createUnitNullParams() throws OperationNotPermittedException,
            NonUniqueBusinessKeyException {
        // when
        userGroupService.createUserGroup(null, null, null);

        // then exception
    }

    private void initUserGroupData() {
        group = new UserGroup();
        group.setKey(1L);
        group.setName("GROUPNAME");

        List<UserGroupToInvisibleProduct> userGroupToInvisibleProducts = new ArrayList<>();
        UserGroupToInvisibleProduct userGroupToInvisibleProduct = new UserGroupToInvisibleProduct();
        userGroupToInvisibleProducts.add(userGroupToInvisibleProduct);
        group.setUserGroupToInvisibleProducts(userGroupToInvisibleProducts);

        List<UserGroupToUser> userGroupToUsers = new ArrayList<>();
        UserGroupToUser userGroupToUser = new UserGroupToUser();
        userGroupToUsers.add(userGroupToUser);
        group.setUserGroupToUsers(userGroupToUsers);

        PlatformUser user = new PlatformUser();
        user.setKey(1L);
        user.setUserId("UserId");

        userGroupToUser.setPlatformuser(user);

        Product product = new Product();
        product.setKey(1L);

        userGroupToInvisibleProduct.setProduct(product);

        group.setIsDefault(false);
        group.setName(TESTGROUPNAME);
    }

    private UserGroup givenNewUserGroup() {
        UserGroup userGroup = new UserGroup();
        userGroup.setIsDefault(false);
        userGroup.setName("newUserGroup");
        userGroup.setKey(1L);

        List<UserGroupToInvisibleProduct> userGroupToInvisibleProducts = new ArrayList<>();
        UserGroupToInvisibleProduct userGroupToInvisibleProduct = new UserGroupToInvisibleProduct();
        userGroupToInvisibleProducts.add(userGroupToInvisibleProduct);
        userGroup.setUserGroupToInvisibleProducts(userGroupToInvisibleProducts);

        Product product = new Product();
        product.setKey(existingInvProductKey);
        product.setStatus(ServiceStatus.ACTIVE);
        userGroupToInvisibleProduct.setProduct(product);

        return userGroup;
    }

    private List<Product> givenVisibleProducts() {
        List<Product> prods = new ArrayList<>();
        Product prod = new Product();
        prod.setKey(productKey);
        prod.setStatus(ServiceStatus.ACTIVE);
        prods.add(prod);

        return prods;
    }

    private List<PlatformUser> givenPlatformUserList() {
        List<PlatformUser> users = new ArrayList<>();
        PlatformUser user = new PlatformUser();
        user.setKey(12345L);
        users.add(user);

        return users;
    }

    private Map<PlatformUser, String> givenPlatformUserMap() {
        Map<PlatformUser, String> users = new HashMap<PlatformUser, String>();
        users.put(user, UnitRoleType.USER.name());
        return users;
    }

    private List<Product> givenInvisibleProducts() {
        List<Product> prods = new ArrayList<>();
        Product prod = new Product();
        prod.setStatus(ServiceStatus.ACTIVE);
        prod.setKey(invProductKey);
        prods.add(prod);
        return prods;
    }

    private Marketplace givenMarketplace() {
        return new Marketplace();
    }

    @Test
    public void validateNotUnitWithSubscriptions_NoSubscriptionAssigned()
            throws Exception {
        // given
        doReturn(Boolean.valueOf(false)).when(
                userGroupService.getUserGroupDao())
                .isNotTerminatedSubscriptionAssignedToUnit(1L);
        // when
        userGroupService.validateNotUnitWithSubscriptions(group);
    }

    @Test(expected = DeletingUnitWithSubscriptionsNotPermittedException.class)
    public void validateNotUnitWithSubscriptions_SubscriptionAssigned()
            throws Exception {
        // given
        doReturn(Boolean.valueOf(true))
                .when(userGroupService.getUserGroupDao())
                .isNotTerminatedSubscriptionAssignedToUnit(1L);
        // when
        userGroupService.validateNotUnitWithSubscriptions(group);
    }

    @Test
    public void grantUserRoles() throws ObjectNotFoundException,
            OperationNotPermittedException {
        // given
        PlatformUser user = prepareUserWithRoleForTest(UserRoleType.ORGANIZATION_ADMIN);
        List<UnitRoleType> roleTypes = Collections
                .singletonList(UnitRoleType.ADMINISTRATOR);
        UserGroupToUser userGroupToUser = new UserGroupToUser();

        userGroupToUser.setUserGroup(group);
        group.setOrganization(org);

        doReturn(user).when(userGroupService.getDm()).find(
                any(PlatformUser.class));

        doReturn(user).when(userGroupService.getDm())
                .getReferenceByBusinessKey(any(DomainObject.class));

        doReturn(userGroupToUser).when(userGroupService.getUserGroupDao())
                .getUserGroupAssignment(group, user);

        // when
        userGroupService.grantUserRoles(user, roleTypes, group);

        // then
        verify(userGroupService.getDm(), times(1)).getReferenceByBusinessKey(
                any(DomainObject.class));
        verify(userGroupService.getUserGroupDao(), times(1))
                .getUserGroupAssignment(group, user);
    }

    @Test(expected = IllegalArgumentException.class)
    public void grantUserRolesNullParams()
            throws OperationNotPermittedException,
            NonUniqueBusinessKeyException, ObjectNotFoundException {
        // when
        userGroupService.grantUserRoles(null, null, null);

        // then exception
    }

    @Test
    public void revokeUserRoles() throws ObjectNotFoundException,
            OperationNotPermittedException {
        // given
        PlatformUser user = mock(PlatformUser.class);
        List<UnitRoleType> roleTypes = Collections
                .singletonList(UnitRoleType.ADMINISTRATOR);
        UserGroupToUser userGroupToUser = new UserGroupToUser();
        RoleAssignment roleAssignment = mock(RoleAssignment.class);

        userGroupToUser.setUserGroup(this.group);
        group.setOrganization(org);

        doReturn(user).when(userGroupService.getDm())
                .getReferenceByBusinessKey(user);

        doReturn(userGroupToUser).when(userGroupService.getUserGroupDao())
                .getUserGroupAssignment(group, user);

        doReturn(roleAssignment).when(userGroupService.getDm())
                .getReferenceByBusinessKey(roleAssignment);

        // when
        userGroupService.revokeUserRoles(user, roleTypes, group);

        // then
        verify(userGroupService.getDm(), times(1)).getReferenceByBusinessKey(
                user);
        verify(userGroupService.getUserGroupDao(), times(1))
                .getUserGroupAssignment(group, user);
    }

    @Test(expected = IllegalArgumentException.class)
    public void revokeUserRolesNullParams()
            throws OperationNotPermittedException,
            NonUniqueBusinessKeyException, ObjectNotFoundException {
        // when
        userGroupService.revokeUserRoles(null, null, null);

        // then exception
    }

    @Test
    public void deleteUserGroupWithName() throws ObjectNotFoundException,
            OperationNotPermittedException,
            DeletingUnitWithSubscriptionsNotPermittedException,
            MailOperationException {

        // when
        doReturn(user).when(userGroupService.getDm()).getCurrentUser();
        doReturn(group).when(userGroupService.getDm())
                .getReferenceByBusinessKey(any(DomainObject.class));

        userGroupService.deleteUserGroup("testGroup");

        // then
        verify(userGroupService.getDm(), times(1)).remove(group);
        verify(userGroupService.getDm(), times(1)).getReferenceByBusinessKey(
                any(DomainObject.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void deleteUserGroupWithNameNullParam()
            throws ObjectNotFoundException,
            DeletingUnitWithSubscriptionsNotPermittedException,
            MailOperationException, OperationNotPermittedException {
        // when
        userGroupService.deleteUserGroup((String) null);

        // then exception
    }

    @Test
    public void handleRemovingCurrentUserFromGroup_NotUnitAdmin() {
        // given
        PlatformUser user = prepareUserWithRoleForTest(UserRoleType.SUBSCRIPTION_MANAGER);
        when(userGroupService.getDm().getCurrentUser()).thenReturn(user);

        // when
        boolean returnValueToCheck = userGroupService
                .handleRemovingCurrentUserFromGroup();
        // then
        assertFalse(returnValueToCheck);
    }

    @Test
    public void handleRemovingCurrentUserFromGroup_UnitAdmin() {
        // given
        PlatformUser user = prepareUnitAdminWithGroups();
        when(userGroupService.getDm().getCurrentUser()).thenReturn(user);

        // when
        boolean returnValueToCheck = userGroupService
                .handleRemovingCurrentUserFromGroup();

        // then
        assertTrue(returnValueToCheck);
    }

    @Test
    public void deleteUserGroup_HandleUnitAdminRole()
            throws ObjectNotFoundException, OperationNotPermittedException,
            MailOperationException,
            DeletingUnitWithSubscriptionsNotPermittedException {
        // given
        PlatformUser user = prepareUnitAdminWithGroups();
        when(userGroupService.getDm().getCurrentUser()).thenReturn(user);

        // when
        userGroupService.deleteUserGroup(user.getUserGroupToUsers().get(0)
                .getUserGroup());

        // then
        assertTrue(!user.getAssignedRoles().contains(
                UserRoleType.UNIT_ADMINISTRATOR));
    }

    @Test
    public void getAccessibleServices() {
        // given
        Pagination pagination = new Pagination(0, 0);
        // when
        userGroupService.getAccessibleServices(String.valueOf(group.getKey()),
                pagination, MARKETPLACEID);
        // then
        verify(userGroupService.getUserGroupDao(), times(1))
                .getAccessibleServices(String.valueOf(group.getKey()),
                        pagination, MARKETPLACEID);
    }

    @Test
    public void getVisibleServices() {
        // given
        Pagination pagination = new Pagination(0, 0);
        // when
        userGroupService.getVisibleServices(String.valueOf(group.getKey()),
                pagination, MARKETPLACEID);
        // then
        verify(userGroupService.getUserGroupDao(), times(1))
                .getVisibleServices(String.valueOf(group.getKey()), pagination,
                        MARKETPLACEID);
    }

    private PlatformUser prepareUserWithRoleForTest(UserRoleType userRoleType) {
        PlatformUser user = new PlatformUser();
        RoleAssignment roleAssignment = new RoleAssignment();
        roleAssignment.setKey(1);
        roleAssignment.setUser(user);
        UserRole userRole = new UserRole();
        userRole.setKey(1);
        userRole.setRoleName(userRoleType);
        roleAssignment.setRole(userRole);
        Set<RoleAssignment> grantedRoles = new HashSet<RoleAssignment>();
        grantedRoles.add(roleAssignment);
        user.setAssignedRoles(grantedRoles);
        user.setOrganization(org);
        return user;
    }

    private PlatformUser prepareUnitAdminWithGroups() {
        PlatformUser user = prepareUserWithRoleForTest(UserRoleType.UNIT_ADMINISTRATOR);
        List<UserGroup> userGroups = new ArrayList<UserGroup>();
        UserGroup userGroup = new UserGroup();
        userGroup.setKey(1);

        UserGroupToUser userUserGroupToUser = new UserGroupToUser();
        userUserGroupToUser.setKey(1);
        userUserGroupToUser.setPlatformuser(user);
        userUserGroupToUser.setUserGroup(userGroup);
        UnitRoleAssignment unitRoleAssignment = new UnitRoleAssignment();
        unitRoleAssignment.setKey(1);
        UnitUserRole unitUserRole = new UnitUserRole();
        unitUserRole.setKey(1);
        unitUserRole.setRoleName(UnitRoleType.ADMINISTRATOR);
        unitRoleAssignment.setUnitUserRole(unitUserRole);
        userUserGroupToUser.setUnitRoleAssignments(Arrays
                .asList(unitRoleAssignment));
        userGroup.setUserGroupToUsers(Arrays.asList(userUserGroupToUser));
        userGroups.add(userGroup);
        user.setUserGroupToUsers(Arrays.asList(userUserGroupToUser));
        user.setOrganization(organization);
        return user;
    }
}
