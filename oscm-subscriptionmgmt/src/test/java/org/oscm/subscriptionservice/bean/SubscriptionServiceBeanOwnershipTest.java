/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 05.08.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.subscriptionservice.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.DomainObject;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.RoleAssignment;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.UserRole;
import org.oscm.identityservice.local.IdentityServiceLocal;
import org.oscm.usergroupservice.bean.UserGroupServiceLocalBean;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOSubscription;
import org.oscm.internal.vo.VOUsageLicense;
import org.oscm.internal.vo.VOUser;

/**
 * @author iversen
 * 
 */
public class SubscriptionServiceBeanOwnershipTest {
    private SubscriptionServiceBean bean;
    private ManageSubscriptionBean manageBean;
    private IdentityServiceLocal idManager;
    private ModifyAndUpgradeSubscriptionBean modUpgBean;
    private PlatformUser user;
    private Organization org;
    private final String organizationId = "ORG1";
    private Subscription sub;
    private final String subscriptionID = "SUBID";
    private final String userId = "USERID";
    private VOSubscription voSub;
    private ConfigurationServiceLocal cfgService;

    @Before
    public void setup() {

        bean = spy(new SubscriptionServiceBean());
        manageBean = spy(new ManageSubscriptionBean());
        modUpgBean = spy(new ModifyAndUpgradeSubscriptionBean());
        bean.stateValidator = new ValidateSubscriptionStateBean();
        DataService dsMock = mock(DataService.class);
        bean.dataManager = dsMock;
        idManager = mock(IdentityServiceLocal.class);

        cfgService = mock(ConfigurationServiceLocal.class);
        bean.cfgService = cfgService;

        bean.manageBean = manageBean;
        manageBean.dataManager = dsMock;

        bean.modUpgBean = modUpgBean;
        bean.modUpgBean.idManager = idManager;
        modUpgBean.dataManager = dsMock;

        user = new PlatformUser();
        org = new Organization();
        org.setOrganizationId(organizationId);
        user.setOrganization(org);
        org.getPlatformUsers().add(user);
        sub = new Subscription();
        sub.setSubscriptionId(subscriptionID);
        sub.setOrganization(org);
        sub.setOrganizationKey(org.getKey());
        voSub = new VOSubscription();
        voSub.setSubscriptionId(subscriptionID);

        manageBean.userGroupService = mock(UserGroupServiceLocalBean.class);
        doReturn(Collections.EMPTY_LIST).when(manageBean.userGroupService)
                .getUserGroupsForUserWithRole(anyLong(), anyLong());
        doReturn(true).when(cfgService).isPaymentInfoAvailable();

    }

    @Test
    public void checkSubscriptionOwner_Admin() throws Exception {
        // given
        user.setAssignedRoles(newRoleAssignment(user,
                UserRoleType.ORGANIZATION_ADMIN));
        when(bean.dataManager.getCurrentUser()).thenReturn(user);
        doReturn(sub).when(bean.dataManager).getReferenceByBusinessKey(
                any(DomainObject.class));

        // when
        manageBean.checkSubscriptionOwner(sub.getSubscriptionId(), 0);
    }

    @Test
    public void checkSubscriptionOwner_SubscriptionManager_Owner()
            throws Exception {
        // given
        user.setAssignedRoles(newRoleAssignment(user,
                UserRoleType.SUBSCRIPTION_MANAGER));
        sub.setOwner(user);
        when(bean.dataManager.getCurrentUser()).thenReturn(user);
        doReturn(sub).when(bean.dataManager).getReferenceByBusinessKey(
                any(DomainObject.class));

        // when
        manageBean.checkSubscriptionOwner(subscriptionID, 0);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void checkSubscriptionOwner_SubscriptionManager_NotOwner()
            throws Exception {
        // given
        user.setAssignedRoles(newRoleAssignment(user,
                UserRoleType.SUBSCRIPTION_MANAGER));
        when(manageBean.dataManager.getCurrentUser()).thenReturn(user);
        doReturn(sub).when(manageBean.dataManager).getReferenceByBusinessKey(
                any(DomainObject.class));

        // when
        manageBean.checkSubscriptionOwner(sub.getSubscriptionId(), 0);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void checkSubscriptionOwner_SubscriptionManager_ObjectNotFound()
            throws Exception {
        // given
        createSubscriptionManagerAsOwner();
        mockSubscriptionNotFound();

        // when
        manageBean.checkSubscriptionOwner(sub.getSubscriptionId(), 0);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void getServiceRolesForSubscription_NotOwner() throws Exception {
        // given
        createSubscriptionManagerNotOwner();
        mockUserAndSubscription();
        // when
        bean.getServiceRolesForSubscription(subscriptionID);
    }

    @Test(expected = NullPointerException.class)
    public void getServiceRolesForSubscription_Owner() throws Exception {
        // given
        createSubscriptionManagerAsOwner();
        mockUserAndSubscription();

        // when
        bean.getServiceRolesForSubscription(subscriptionID);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void getServiceRolesForSubscription_ObjectNotFound()
            throws Exception {
        // given
        createSubscriptionManagerAsOwner();
        mockSubscriptionNotFound();

        // when
        bean.getServiceRolesForSubscription(subscriptionID);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void getSubscriptionDetails_NotOwner() throws Exception {
        // given
        createSubscriptionManagerNotOwner();
        mockUserAndSubscription();
        // when
        bean.getSubscriptionDetails(subscriptionID);
    }

    @Test(expected = NullPointerException.class)
    public void getSubscriptionDetails_Owner() throws Exception {
        // given
        createSubscriptionManagerAsOwner();
        mockUserAndSubscription();

        // when
        bean.getSubscriptionDetails(subscriptionID);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void getSubscriptionDetails_ObjectNotFound() throws Exception {
        // given
        createSubscriptionManagerAsOwner();
        mockSubscriptionNotFound();

        // when
        bean.getSubscriptionDetails(subscriptionID);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void upgradeSubscription_NotOwner() throws Exception {
        // given
        createSubscriptionManagerNotOwner();
        mockUserAndSubscription();

        // when
        bean.upgradeSubscription(voSub, new VOService(), null, null, null);
    }

    @Test(expected = NullPointerException.class)
    public void upgradeSubscription_Owner() throws Exception {
        // given
        createSubscriptionManagerAsOwner();
        mockUserAndSubscription();

        // when
        bean.upgradeSubscription(voSub, new VOService(), null, null, null);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void upgradeSubscription_ObjectNotFound() throws Exception {
        // given
        createSubscriptionManagerAsOwner();
        mockSubscriptionNotFound();

        // when
        bean.upgradeSubscription(voSub, new VOService(), null, null, null);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void unsubscribeFromService_NotOwner() throws Exception {
        // given
        createSubscriptionManagerNotOwner();
        mockUserAndSubscription();

        // when
        bean.unsubscribeFromService(subscriptionID);
    }

    @Test(expected = NullPointerException.class)
    public void unsubscribeFromService_Owner() throws Exception {
        // given
        createSubscriptionManagerAsOwner();
        mockUserAndSubscription();

        // when
        bean.unsubscribeFromService(subscriptionID);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void unsubscribeFromService_ObjectNotFound() throws Exception {
        // given
        createSubscriptionManagerAsOwner();
        mockSubscriptionNotFound();

        // when
        bean.unsubscribeFromService(subscriptionID);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void getUpgradeOptions_NotOwner() throws Exception {
        // given
        createSubscriptionManagerNotOwner();
        mockUserAndSubscription();

        // when
        bean.getUpgradeOptions(subscriptionID);
    }

    @Test(expected = NullPointerException.class)
    public void getUpgradeOptions_Owner() throws Exception {
        // given
        createSubscriptionManagerAsOwner();
        mockUserAndSubscription();

        // when
        bean.getUpgradeOptions(subscriptionID);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void getUpgradeOptions_ObjectNotFound() throws Exception {
        // given
        createSubscriptionManagerAsOwner();
        mockSubscriptionNotFound();

        // when
        bean.getUpgradeOptions(subscriptionID);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void addRevokeUser_NotOwner() throws Exception {
        // given
        createSubscriptionManagerNotOwner();
        mockUserAndSubscription();

        // when
        bean.addRevokeUser(subscriptionID, null, null);
    }

    @Test(expected = NullPointerException.class)
    public void addRevokeUser_Owner() throws Exception {
        // given
        createSubscriptionManagerAsOwner();
        mockUserAndSubscription();

        // when
        bean.addRevokeUser(subscriptionID, new ArrayList<VOUsageLicense>(),
                new ArrayList<VOUser>());

    }

    @Test(expected = ObjectNotFoundException.class)
    public void addRevokeUser_ObjectNotFound() throws Exception {
        // given
        createSubscriptionManagerAsOwner();
        mockSubscriptionNotFound();

        // when
        bean.addRevokeUser(subscriptionID, new ArrayList<VOUsageLicense>(),
                new ArrayList<VOUser>());

    }

    @Test
    public void addRevokeUser_AddedUserNotExist() throws Exception {
        // given
        createSubscriptionManagerAsOwner();
        mockUserAndSubscription();
        doThrow(new ObjectNotFoundException()).when(bean.dataManager)
                .getReference(eq(PlatformUser.class), anyLong());

        // when
        try {
            bean.addRevokeUser(subscriptionID, givenVOUsageLicense(),
                    new ArrayList<VOUser>());
            fail();
        } catch (ObjectNotFoundException e) {
            // then
            assertEquals(Boolean.TRUE,
                    Boolean.valueOf(e.getMessage().contains(userId)));
        }
    }

    @Test
    public void addRevokeUser_RevokedUserNotExist() throws Exception {
        // given
        createSubscriptionManagerAsOwner();
        mockUserAndSubscription();
        doThrow(new ObjectNotFoundException()).when(bean.dataManager)
                .getReference(eq(PlatformUser.class), anyLong());

        // when
        try {
            bean.addRevokeUser(subscriptionID, new ArrayList<VOUsageLicense>(),
                    givenVOUser());
            fail();
        } catch (ObjectNotFoundException e) {
            // then
            assertEquals(Boolean.TRUE,
                    Boolean.valueOf(e.getMessage().contains(userId)));
        }
    }

    @Test(expected = OperationNotPermittedException.class)
    public void modifySubscriptionPaymentData_NotOwner() throws Exception {
        // given
        createSubscriptionManagerNotOwner();
        mockUserAndSubscription();

        // when
        bean.modifySubscriptionPaymentData(voSub, null, null);
    }

    @Test(expected = NullPointerException.class)
    public void modifySubscriptionPaymentData_Owner() throws Exception {
        // given
        createSubscriptionManagerAsOwner();
        mockUserAndSubscription();

        // when
        bean.modifySubscriptionPaymentData(voSub, null, null);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void modifySubscriptionPaymentData_ObjectNotFound() throws Exception {
        // given
        createSubscriptionManagerAsOwner();
        mockSubscriptionNotFound();

        // when
        bean.modifySubscriptionPaymentData(voSub, null, null);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void modifySubscription_NotOwner() throws Exception {
        // given
        createSubscriptionManagerNotOwner();
        mockUserAndSubscription();

        // when
        bean.modifySubscription(voSub, null, null);
    }

    @Test(expected = NullPointerException.class)
    public void modifySubscription_Owner() throws Exception {
        // given
        createSubscriptionManagerAsOwner();
        mockUserAndSubscription();

        // when
        bean.modifySubscription(voSub, null, null);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void modifySubscriptiona_ObjectNotFound() throws Exception {
        // given
        createSubscriptionManagerAsOwner();
        mockSubscriptionNotFound();

        // when
        bean.modifySubscription(voSub, null, null);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void reportIssue_NotOwner() throws Exception {
        // given
        createSubscriptionManagerNotOwner();
        mockUserAndSubscription();

        // when
        bean.reportIssue(subscriptionID, "", "");
    }

    @Test(expected = ValidationException.class)
    public void reportIssue_Owner() throws Exception {
        // given
        createSubscriptionManagerAsOwner();
        mockUserAndSubscription();

        // when
        bean.reportIssue(subscriptionID, "", "");
    }

    @Test(expected = ObjectNotFoundException.class)
    public void reportIssue_ObjectNotFound() throws Exception {
        // given
        createSubscriptionManagerAsOwner();
        mockSubscriptionNotFound();

        // when
        bean.reportIssue(subscriptionID, "", "");
    }

    @Test
    public void setSubscriptionOwner_setOwner() throws Exception {
        // given
        PlatformUser user = givenSubscriptionOwner();
        Subscription subscriptionToModify = givenSubscription(null);
        doReturn(user).when(modUpgBean.dataManager).getCurrentUser();
        doReturn(user).when(bean.modUpgBean.idManager).getPlatformUser(
                eq(user.getUserId()), anyString(), eq(true));

        // when
        bean.modUpgBean.setSubscriptionOwner(subscriptionToModify,
                user.getUserId(), true);

        // then
        assertEquals(user, subscriptionToModify.getOwner());
    }

    @Test
    public void setSubscriptionOwner_resetOwner() throws Exception {
        // given
        PlatformUser user = givenSubscriptionOwner();
        Subscription subscriptionToModify = givenSubscription(user);
        doReturn(user).when(idManager).getPlatformUser(user.getUserId(), true);

        // when
        bean.modUpgBean.setSubscriptionOwner(subscriptionToModify, null, true);

        // then
        assertNull(subscriptionToModify.getOwner());
    }

    @Test
    public void setSubscriptionOwner_ownerSame() throws Exception {
        // given
        PlatformUser user = givenSubscriptionOwner();
        Subscription subscriptionToModify = givenSubscription(user);
        doReturn(user).when(modUpgBean.dataManager).getCurrentUser();
        doReturn(user).when(idManager).getPlatformUser(eq(user.getUserId()),
                anyString(), eq(true));

        // when
        bean.modUpgBean.setSubscriptionOwner(subscriptionToModify,
                user.getUserId(), true);

        // then
        assertEquals(user, subscriptionToModify.getOwner());
    }

    @Test
    public void setSubscriptionOwner_ownerSameNull() throws Exception {
        // given
        Subscription subscriptionToModify = givenSubscription(null);
        doReturn(user).when(idManager).getPlatformUser(user.getUserId(), true);

        // when
        bean.modUpgBean.setSubscriptionOwner(subscriptionToModify, null, true);

        // then
        assertNull(subscriptionToModify.getOwner());
    }

    private Subscription givenSubscription(PlatformUser owner) {
        Subscription sub = new Subscription();
        sub.setOwner(owner);
        return sub;
    }

    private PlatformUser givenSubscriptionOwner() {
        PlatformUser user = new PlatformUser();
        user.setUserId("userId");
        return user;
    }

    private void createSubscriptionManagerAsOwner() {
        user.setAssignedRoles(newRoleAssignment(user,
                UserRoleType.SUBSCRIPTION_MANAGER));
        sub.setOwner(user);
    }

    private void createSubscriptionManagerNotOwner() {
        user.setAssignedRoles(newRoleAssignment(user,
                UserRoleType.SUBSCRIPTION_MANAGER));
    }

    private void mockUserAndSubscription() throws Exception {
        when(bean.dataManager.getCurrentUser()).thenReturn(user);
        doReturn(sub).when(bean.dataManager).getReferenceByBusinessKey(
                any(DomainObject.class));
    }

    private void mockSubscriptionNotFound() throws ObjectNotFoundException {
        ObjectNotFoundException e = new ObjectNotFoundException();
        when(bean.dataManager.getCurrentUser()).thenReturn(user);
        doThrow(e).when(bean.dataManager).getReferenceByBusinessKey(
                any(DomainObject.class));
    }

    private Set<RoleAssignment> newRoleAssignment(PlatformUser user,
            UserRoleType roleType) {
        Set<RoleAssignment> roles = new HashSet<RoleAssignment>();
        RoleAssignment ra = new RoleAssignment();
        ra.setKey(1L);
        ra.setUser(user);
        ra.setRole(new UserRole(roleType));
        roles.add(ra);
        return roles;
    }

    private List<VOUsageLicense> givenVOUsageLicense() {
        VOUsageLicense voUsageLicense = new VOUsageLicense();
        VOUser voUser = new VOUser();
        voUser.setKey(1000L);
        voUser.setUserId(userId);
        voUsageLicense.setUser(voUser);
        List<VOUsageLicense> usersTobeAdded = new ArrayList<VOUsageLicense>();
        usersTobeAdded.add(voUsageLicense);
        return usersTobeAdded;
    }

    private List<VOUser> givenVOUser() {
        VOUser voUser = new VOUser();
        voUser.setKey(1000L);
        voUser.setUserId(userId);
        List<VOUser> voUsers = new ArrayList<VOUser>();
        voUsers.add(voUser);
        return voUsers;
    }
}
