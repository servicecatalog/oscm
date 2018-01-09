/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 04.10.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.mp.updateuser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.servlet.http.HttpSession;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.oscm.internal.components.response.Response;
import org.oscm.internal.components.response.ReturnCode;
import org.oscm.internal.components.response.ReturnType;
import org.oscm.internal.intf.IdentityService;
import org.oscm.internal.types.constants.HiddenUIConstants;
import org.oscm.internal.types.enumtypes.Salutation;
import org.oscm.internal.types.enumtypes.SettingType;
import org.oscm.internal.types.enumtypes.UserAccountStatus;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.TechnicalServiceNotAliveException;
import org.oscm.internal.types.exception.TechnicalServiceOperationException;
import org.oscm.internal.usergroupmgmt.POUserGroup;
import org.oscm.internal.usergroupmgmt.UserGroupService;
import org.oscm.internal.usermanagement.POServiceRole;
import org.oscm.internal.usermanagement.POSubscription;
import org.oscm.internal.usermanagement.POUser;
import org.oscm.internal.usermanagement.POUserAndSubscriptions;
import org.oscm.internal.usermanagement.POUserDetails;
import org.oscm.internal.usermanagement.UserService;
import org.oscm.internal.vo.VOUserDetails;
import org.oscm.paginator.Pagination;
import org.oscm.ui.beans.ApplicationBean;
import org.oscm.ui.beans.BaseBean;
import org.oscm.ui.beans.SessionBean;
import org.oscm.ui.common.Constants;
import org.oscm.ui.common.UiDelegate;
import org.oscm.ui.dialog.mp.createuser.Subscription;
import org.oscm.ui.dialog.mp.createuser.UserGroup;
import org.oscm.ui.dialog.state.TableState;
import org.oscm.ui.model.User;

/**
 * @author weiser
 */
@RunWith(MockitoJUnitRunner.class)
public class UpdateUserCtrlTest {

    private static final String MY_USER_ID = "myUserId";
    private static final String MP_ID = "mpid";
    private static final String NAME = "name";

    private UpdateUserCtrl ctrl = new UpdateUserCtrl();
    private UpdateUserModel model = new UpdateUserModel();
    private UserSubscriptionsLazyDataModel lazyDataModel = new UserSubscriptionsLazyDataModel();

    @Mock
    private UserService us;
    @Mock
    private UserGroupService userGroupService;
    @Mock
    private HttpSession session;
    private POUserAndSubscriptions uas;
    private List<POSubscription> subs = prepareSubscriptions();
    @Mock
    private ApplicationBean applicationBean;
    @Mock
    private TableState ts;
    private User user;
    private Response response;
    private SessionBean sessionBean = new SessionBean();

    @Before
    public void setup() throws Exception {
        ctrl = spy(ctrl);
        model = spy(model);
        lazyDataModel = spy(lazyDataModel);

        ctrl.setUserService(us);
        ctrl.setUserGroupService(userGroupService);
        ctrl.setUi(mock(UiDelegate.class));
        ctrl.setIdentityService(mock(IdentityService.class));
        ctrl.setModel(model);
        ctrl.setUserSubscriptionsLazyDataModel(lazyDataModel);
        ctrl.setAppBean(applicationBean);

        lazyDataModel.setModel(model);

        uas = new POUserAndSubscriptions();
        uas.setLocale("en");
        uas.setAvailableRoles(EnumSet.of(UserRoleType.ORGANIZATION_ADMIN));
        uas.setSubscriptions(subs);
        uas.setKey(1234);
        uas.setVersion(7);

        when(ctrl.getUi().getMarketplaceId()).thenReturn(MP_ID);
        when(ctrl.getUi().getText(anyString())).thenReturn(NAME);
        when(ctrl.getUi().getMyUserId()).thenReturn(MY_USER_ID);

        when(us.getUserAndSubscriptionDetails(anyString(), anyString()))
                .thenReturn(uas);
        response = new Response();
        when(
                us.saveUserAndSubscriptionAssignment(
                        any(POUserAndSubscriptions.class),
                        anyListOf(POUserGroup.class))).thenReturn(response);

        when(
                us.getUserAssignableSubscriptions(any(Pagination.class),
                        anyString())).thenReturn(subs);
        when(
                us.getUserAssignableSubscriptionsNumber(any(Pagination.class),
                        anyString(), anyString())).thenReturn(
                (long) subs.size());

        when(
                Boolean.valueOf(applicationBean
                        .isUIElementHidden(eq(HiddenUIConstants.PANEL_USER_LIST_SUBSCRIPTIONS))))
                .thenReturn(Boolean.FALSE);

        when(userGroupService.getGroupListForOrganizationWithoutDefault())
                .thenReturn(preparePOUserGroups(3));

        when(userGroupService.getUserGroupsForUserWithoutDefault(1234))
                .thenReturn(preparePOUserGroups(3));

        when(
                userGroupService
                        .getUserGroupListForUserWithRolesWithoutDefault(
                                "userId", 1234)).thenReturn(
                preparePOUserGroups(1));

        when(ctrl.getUi().getSession(anyBoolean())).thenReturn(session);
        ctrl.setTs(ts);
        user = new User(new VOUserDetails());
        model.setUser(user);
        model.setSubscriptions(lazyDataModel.toSubscriptionList(subs));

        Map<String, Subscription> allSubs = new HashMap<>();

        for (Subscription sub : model.getSubscriptions()) {
            allSubs.put(sub.getId(), sub);
        }

        model.setAllSubscriptions(allSubs);
        // when(ctrl.getAllSubscriptions(anyListOf(Subscription.class))).thenReturn(prepareSubscriptions());
        doNothing().when(ctrl).addMessage(any(FacesMessage.Severity.class),
                anyString());
        sessionBean.setTenantID("1");
        ctrl.setSessionBean(sessionBean);
    }

    @Test
    public void isSubTableRendered_NoSubs() throws Exception {
        when(
                us.getUserAssignableSubscriptionsNumber(any(Pagination.class),
                        anyString(), anyString())).thenReturn(0L);
        assertFalse(ctrl.isSubTableRendered());
    }

    @Test
    public void isSubTableRendered_ErrorOnRead() {
        model.setErrorOnRead(true);

        assertFalse(ctrl.isSubTableRendered());
    }

    @Test
    public void isSubTableRendered_TableHidden() {
        when(
                Boolean.valueOf(applicationBean
                        .isUIElementHidden(eq(HiddenUIConstants.PANEL_USER_LIST_SUBSCRIPTIONS))))
                .thenReturn(Boolean.TRUE);

        assertFalse(ctrl.isSubTableRendered());
    }

    @Test
    public void isSubTableRendered() {
        ctrl.init(uas);
        model.setAssignableSubscriptionsNumber(subs.size());
        assertTrue(ctrl.isSubTableRendered());
    }

    @Test
    public void isRoleColumnRendered_NoSubs() {
        model.setSubscriptions(Collections.<Subscription> emptyList());
        assertFalse(ctrl.isRoleColumnRendered());
    }

    @Test
    public void isRoleColumnRendered_NoRoles() {
        model.setSubscriptions(Arrays.asList(new Subscription()));

        assertFalse(ctrl.isRoleColumnRendered());
    }

    @Test
    public void isRoleColumnRendered_Cached() {
        ctrl.rolesColumnVisible = Boolean.TRUE;

        assertTrue(ctrl.isRoleColumnRendered());

        verifyZeroInteractions(ctrl.getUi());
    }

    @Test
    public void isRoleColumnRendered() {
        ctrl.init(uas);
        assertTrue(ctrl.isRoleColumnRendered());
    }

    @Test
    public void initUserGroups() {
        // given
        model.getUser().setUserId("userId");
        model.setKey(1234);

        // when
        List<UserGroup> result = ctrl.initUserGroups();

        // then
        assertEquals(3, result.size());
        assertEquals(Boolean.TRUE, Boolean.valueOf(result.get(0).isSelected()));
        verify(userGroupService, times(1))
                .getGroupListForOrganizationWithoutDefault();
        verify(userGroupService, times(1))
                .getUserGroupListForUserWithRolesWithoutDefault(anyString(),
                        anyLong());
        assertGroupsSorted(result);
    }

    private void assertGroupsSorted(List<UserGroup> groups) {
        UserGroup prev = null;
        for (UserGroup group : groups) {
            if (prev == null) {
                prev = group;
            } else {
                if (!prev.isSelected() && group.isSelected()) {
                    assertFalse(true);
                }
            }
        }
        assertTrue(true);
    }

    @Test
    public void createUserName() {
        uas.setFirstName("Hans");
        uas.setLastName("Wurst");
        uas.setUserId("1a2b3c4d");

        String userName = ctrl.createUserName(uas);

        assertEquals("Hans Wurst", userName);
    }

    @Test
    public void createUserName_OnlyId() {
        String userId = "1a2b3c4d";
        uas.setUserId(userId);

        String userName = ctrl.createUserName(uas);

        assertEquals(userId, userName);
    }

    @Test
    public void createUserName_FirstName() {
        uas.setFirstName("Hans");
        uas.setUserId("1a2b3c4d");

        String userName = ctrl.createUserName(uas);

        assertEquals("Hans", userName);
    }

    @Test
    public void createUserName_LastName() {
        uas.setFirstName("");
        uas.setLastName("Wurst");
        uas.setUserId("1a2b3c4d");

        String userName = ctrl.createUserName(uas);

        assertEquals("Wurst", userName);
    }

    @Test
    public void createUserName_ReverseOrder() {
        when(Boolean.valueOf(ctrl.getUi().isNameSequenceReversed()))
                .thenReturn(Boolean.TRUE);
        uas.setFirstName("Hans");
        uas.setLastName("Wurst");
        uas.setUserId("1a2b3c4d");

        String userName = ctrl.createUserName(uas);

        assertEquals("Wurst Hans", userName);
    }

    @Test
    public void toPOUser() {
        model.setKey(1234);
        user.setUserId("userid");
        model.setVersion(7);

        POUser u = ctrl.toPOUser();

        assertEquals(1234, u.getKey());
        assertEquals("userid", u.getUserId());
        assertEquals(7, u.getVersion());
    }

    @Test
    public void getDeleteMsgForUser() {
        String userName = "user name";
        model.setUserName(userName);

        ctrl.getDeleteMsgForUser();

        verify(ctrl.getUi()).getText(eq("marketplace.account.deleteMsg"),
                eq(userName));
    }

    @Test
    public void isResetPwdRendered_LdapNotManagedAndInternalMode() {
        model.setLdapManaged(false);
        givenAuthMode(true);
        boolean result = ctrl.isResetPwdRendered();
        assertEquals(Boolean.TRUE, Boolean.valueOf(result));
    }

    @Test
    public void isResetPwdRendered_LdapNotManagedAndExternalMode() {
        model.setLdapManaged(false);
        givenAuthMode(false);
        boolean result = ctrl.isResetPwdRendered();
        assertEquals(Boolean.FALSE, Boolean.valueOf(result));
    }

    @Test
    public void isResetPwdRendered_LdapManagedAndInternalMode() {
        model.setLdapManaged(true);
        givenAuthMode(true);
        boolean result = ctrl.isResetPwdRendered();
        assertEquals(Boolean.FALSE, Boolean.valueOf(result));
    }

    @Test
    public void isResetPwdRendered_LdapManagedAndExternalMode() {
        model.setLdapManaged(true);
        givenAuthMode(false);
        boolean result = ctrl.isResetPwdRendered();
        assertEquals(Boolean.FALSE, Boolean.valueOf(result));
    }

    @Test
    public void isDeleteDisabled() {
        user.setUserId("userId");

        assertFalse(ctrl.isDeleteDisabled());
    }

    @Test
    public void isDeleteDisabled_SelfEdit() {
        user.setUserId(MY_USER_ID);

        assertTrue(ctrl.isDeleteDisabled());
    }

    @Test
    public void isDeleteDisabled_ErrorOnRead() {
        model.setErrorOnRead(true);

        assertTrue(ctrl.isDeleteDisabled());
    }

    @Test
    public void isResetPwdDisabled() {
        user.setUserId("userId");

        assertFalse(ctrl.isResetPwdDisabled());
    }

    @Test
    public void isResetPwdDisabled_SelfEdit() {
        user.setUserId(MY_USER_ID);

        assertTrue(ctrl.isResetPwdDisabled());
    }

    @Test
    public void isResetPwdDisabled_ErrorOnRead() {
        model.setErrorOnRead(true);

        assertTrue(ctrl.isResetPwdDisabled());
    }

    @Test
    public void isSaveDisabled() {
        assertFalse(ctrl.isSaveDisabled());
    }

    @Test
    public void isSaveDisabled_Locked() {
        model.setLocked(true);

        assertTrue(ctrl.isSaveDisabled());
    }

    @Test
    public void isSaveDisabled_ErrorOnRead() {
        model.setErrorOnRead(true);

        assertTrue(ctrl.isSaveDisabled());
    }

    @Test
    public void isRolesDisabled() {
        assertFalse(ctrl.isRolesDisabled());
    }

    @Test
    public void isRolesDisabled_Locked() {
        model.setLocked(true);

        assertTrue(ctrl.isRolesDisabled());
    }

    @Test
    public void isRolesDisabled_ErrorOnRead() {
        model.setErrorOnRead(true);

        assertTrue(ctrl.isRolesDisabled());
    }

    @Test
    public void init_LdapMapped() {
        uas.setMappedAttributes(EnumSet.allOf(SettingType.class));

        ctrl.init(uas);

        assertEquals(Boolean.TRUE,
                Boolean.valueOf(model.getEmail().isReadOnly()));
        assertEquals(Boolean.TRUE,
                Boolean.valueOf(model.getFirstName().isReadOnly()));
        assertEquals(Boolean.TRUE,
                Boolean.valueOf(model.getLastName().isReadOnly()));
        assertEquals(Boolean.FALSE,
                Boolean.valueOf(model.getLocale().isReadOnly()));
        assertEquals(Boolean.TRUE,
                Boolean.valueOf(model.getUserId().isReadOnly()));
        assertEquals(Boolean.TRUE, Boolean.valueOf(model.isLdapManaged()));
    }

    @Test
    public void init_Locked() {
        uas.setStatus(UserAccountStatus.LOCKED_FAILED_LOGIN_ATTEMPTS);
        uas.setSalutation(Salutation.MR);

        ctrl.init(uas);

        assertTrue(model.getEmail().isReadOnly());
        assertTrue(model.getFirstName().isReadOnly());
        assertTrue(model.getLastName().isReadOnly());
        assertTrue(model.getLocale().isReadOnly());
        assertTrue(model.getUserId().isReadOnly());
        assertTrue(model.getSalutation().isReadOnly());
        assertTrue(model.isLocked());
    }

    @Test
    public void init_SetNotContainsUIDAndUnlocked() {
        // given
        uas.setStatus(UserAccountStatus.ACTIVE);
        uas.setMappedAttributes(new HashSet<SettingType>());
        givenAuthMode(true);

        // when
        ctrl.init(uas);
        boolean result = model.getUserId().isReadOnly();

        // then
        assertEquals(Boolean.FALSE, Boolean.valueOf(result));
    }

    @Test
    public void init_NotInternalAuthMode_CurrentAdminUser() {
        // given
        uas.setStatus(UserAccountStatus.ACTIVE);
        when(ctrl.getUi().getMyUserId()).thenReturn(uas.getUserId());
        givenAuthMode(false);

        // when
        ctrl.init(uas);
        boolean result = model.getUserId().isReadOnly();

        // then
        assertEquals(Boolean.TRUE, Boolean.valueOf(result));
    }

    @Test
    public void init_NotInternalAuthMode_OtherUser() {
        // given
        uas.setStatus(UserAccountStatus.ACTIVE);
        givenAuthMode(false);

        // when
        ctrl.init(uas);
        boolean result = model.getUserId().isReadOnly();

        // then
        assertEquals(Boolean.FALSE, Boolean.valueOf(result));
    }

    @Test
    public void init_localeCheck() {
        // given
        uas.setLocale("en");

        // when
        ctrl.init(uas);

        // then
        verify(applicationBean, times(1)).checkLocaleValidation("en");
    }

    @Test
    public void delete() throws Exception {
        String userId = "selectedUserId";
        model.setToken(model.getToken());
        user.setUserId(userId);

        String outcome = ctrl.delete();

        assertEquals(BaseBean.OUTCOME_SUCCESS, outcome);
        verify(ctrl.getUi()).handle(any(Response.class),
                eq(BaseBean.INFO_USER_DELETED), eq(userId));
        ArgumentCaptor<POUser> ac = ArgumentCaptor.forClass(POUser.class);
        verify(us).deleteUser(ac.capture(), eq(MP_ID), anyString());
        assertEquals(userId, ac.getValue().getUserId());
        verify(ts).resetActivePages();
    }

    @Test
    public void delete_InvalidToken() throws Exception {

        String outcome = ctrl.delete();

        assertEquals(null, outcome);
        verify(ctrl.getUi(), never()).handle(any(Response.class), anyString(),
                anyString());
        verify(us, never()).deleteUser(any(POUser.class), anyString(),
                anyString());
    }

    @Test(expected = OperationNotPermittedException.class)
    public void delete_Error() throws Exception {
        when(us.deleteUser(any(POUser.class), anyString(), anyString()))
                .thenThrow(new OperationNotPermittedException());
        model.setToken(model.getToken());

        ctrl.delete();
    }

    @Test
    public void delete_TechnicalServiceNotAliveException() throws Exception {
        // give
        doReturn(Boolean.TRUE).when(model).isTokenValid();
        doThrow(new TechnicalServiceNotAliveException()).when(us).deleteUser(
                any(org.oscm.internal.usermanagement.POUser.class),
                anyString(), anyString());
        // when
        String result = ctrl.delete();

        // then
        assertEquals("", result);
    }

    @Test
    public void delete_TechnicalServiceOperationException() throws Exception {
        // give
        doReturn(Boolean.TRUE).when(model).isTokenValid();
        doThrow(new TechnicalServiceOperationException()).when(us).deleteUser(
                any(org.oscm.internal.usermanagement.POUser.class),
                anyString(), anyString());
        // when
        String result = ctrl.delete();
        // then

        assertEquals("", result);
    }

    @Test
    public void resetPwd() throws Exception {
        String userId = "selectedUserId";
        model.setToken(model.getToken());
        user.setUserId(userId);

        String outcome = ctrl.resetPwd();

        assertEquals(BaseBean.OUTCOME_SUCCESS, outcome);
        verify(ctrl.getUi()).handle(any(Response.class),
                eq(BaseBean.INFO_USER_PWD_RESET));
        ArgumentCaptor<POUser> ac = ArgumentCaptor.forClass(POUser.class);
        verify(us).resetUserPassword(ac.capture(), eq(MP_ID));
        assertEquals(userId, ac.getValue().getUserId());
    }

    @Test
    public void resetPwd_InvalidToken() throws Exception {

        String outcome = ctrl.resetPwd();

        assertEquals(BaseBean.OUTCOME_RESET_PWD + "?userId="
                + model.getUser().getUserId(), outcome);
        verify(ctrl.getUi(), never()).handle(any(Response.class), anyString());
        verify(us, never()).resetUserPassword(any(POUser.class), anyString());
    }

    @Test(expected = OperationNotPermittedException.class)
    public void resetPwd_Error() throws Exception {
        when(us.resetUserPassword(any(POUser.class), anyString())).thenThrow(
                new OperationNotPermittedException());
        model.setToken(model.getToken());

        ctrl.resetPwd();
    }

    @Test
    public void save() throws Exception {
        doReturn(user.getUserId()).when(ctrl).getSelectedUserId();
        doReturn(Boolean.FALSE).when(ctrl).isCurrentUserRolesChanged();
        ctrl.init(uas);
        updateData(model);

        String outcome = ctrl.save();

        assertEquals(BaseBean.OUTCOME_SUCCESS, outcome);
        verify(ctrl.getUi()).handle(response, BaseBean.INFO_USER_SAVED,
                model.getUserId().getValue());
        ArgumentCaptor<POUserAndSubscriptions> ac = ArgumentCaptor
                .forClass(POUserAndSubscriptions.class);
        verify(us).saveUserAndSubscriptionAssignment(ac.capture(),
                anyListOf(POUserGroup.class));
        verifyDataUpdate(ac.getValue());
    }

    @Test(expected = ObjectNotFoundException.class)
    public void save_Error() throws Exception {
        ctrl.init(uas);
        when(
                us.saveUserAndSubscriptionAssignment(
                        any(POUserAndSubscriptions.class),
                        anyListOf(POUserGroup.class))).thenThrow(
                new ObjectNotFoundException());
        model.setToken(model.getToken());

        ctrl.save();
        assertTrue(model.getSubscriptions().isEmpty());
        verify(model).setSubscriptions(any(List.class));
    }

    @Test
    public void save_Suspended() throws Exception {
        ctrl.init(uas);
        Response resp = new Response();
        resp.getReturnCodes().add(new ReturnCode(ReturnType.INFO, "key"));
        String newId = "newuserid";
        when(
                us.saveUserAndSubscriptionAssignment(
                        any(POUserAndSubscriptions.class),
                        anyListOf(POUserGroup.class))).thenReturn(resp);
        model.getSalutation().setValue(Salutation.MR.name());
        model.getUserId().setValue(newId);
        model.setToken(model.getToken());

        String outcome = ctrl.save();

        assertEquals(BaseBean.OUTCOME_PENDING, outcome);
        assertEquals(newId, model.getUser().getUserId());
        verify(ctrl.getUi()).handle(same(resp), eq(BaseBean.INFO_USER_SAVED),
                eq(newId));
    }

    @Test
    public void save_SelfEdit() throws Exception {
        ctrl.init(uas);
        model.getSalutation().setValue(Salutation.MR.name());
        user.setUserId(MY_USER_ID);
        model.getRoles().get(0).setSelected(true);
        model.setToken(model.getToken());
        model.getUserId().setValue(MY_USER_ID);

        String outcome = ctrl.save();

        assertEquals(BaseBean.OUTCOME_SUCCESS, outcome);
        verify(ctrl.getUi()).handle(any(Response.class),
                eq(BaseBean.INFO_USER_SAVED_ITSELF), eq(MY_USER_ID));
        verify(session, times(1)).setAttribute(eq(Constants.SESS_ATTR_USER),
                any());
        verify(ctrl.getUi()).updateAndVerifyViewLocale();
    }

    @Test
    public void save_InvalidToken() throws Exception {

        String outcome = ctrl.save();

        assertEquals(null, outcome);
        verify(ctrl.getUi(), never()).handle(any(Response.class), anyString());
        verify(us, never()).saveUser(any(POUserDetails.class));
    }

    @Test
    public void getSelectedUserGroups() {
        List<UserGroup> groups = new ArrayList<UserGroup>();
        groups.add(prepareUserGroup(true));
        groups.add(prepareUserGroup(false));
        List<POUserGroup> poUserGroups = ctrl.getSelectedUserGroups(groups);

        assertEquals(1, poUserGroups.size());
    }

    @Test
    public void getAllUserGroupsWithoutDefault() {
        List<UserGroup> groups = new ArrayList<UserGroup>();
        groups.add(prepareUserGroup(true));
        groups.add(prepareUserGroup(false));
        List<POUserGroup> poUserGroups = ctrl.getAllUserGroups(groups);

        assertEquals(2, poUserGroups.size());
    }

    private UserGroup prepareUserGroup(boolean selected) {
        UserGroup group = new UserGroup();
        group.setSelected(selected);
        POUserGroup poUserGroup = new POUserGroup();
        poUserGroup.setDefault(false);
        group.setPoUserGroup(poUserGroup);
        return group;
    }

    private List<POUserGroup> preparePOUserGroups(int num) {
        List<POUserGroup> userGroups = new ArrayList<POUserGroup>();
        for (int i = 0; i < num; i++) {
            POUserGroup userGroup = new POUserGroup();
            userGroup.setKey(i);
            userGroup.setGroupName("groupName");
            userGroup.setGroupDescription("description");
            userGroup.setGroupReferenceId("reference id" + i);
            userGroup.setSelectedRole("ADMINISTRATOR");
            userGroup.setDefault(false);
            userGroups.add(userGroup);
        }
        return userGroups;
    }

    private static List<POSubscription> prepareSubscriptions() {
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
        role.setName("role1name");
        sub.getRoles().add(role);
        role = new POServiceRole();
        role.setId("role2");
        role.setName("role2name");
        role.setKey(15);
        sub.getRoles().add(role);
        sub.getUsageLicense().setKey(111);
        sub.getUsageLicense().setVersion(1);
        sub.getUsageLicense().setPoServieRole(role);
        result.add(sub);

        sub = new POSubscription();
        sub.setId("sub3");
        sub.setKey(30);
        result.add(sub);

        return result;
    }

    private static void updateData(UpdateUserModel m) {
        m.getEmail().setValue("email@provider.com");
        m.getFirstName().setValue("Hans");
        m.getLastName().setValue("Wurst");
        m.getLocale().setValue("de");
        m.getRoles().get(0).setSelected(true);
        m.getSalutation().setValue(Salutation.MR.name());
        m.getUserId().setValue("userid");
        m.getAllSubscriptions().get("sub1").setSelected(false);
        m.getAllSubscriptions().get("sub2").setSelectedRole("5:role1");
        m.getAllSubscriptions().get("sub3").setSelected(true);
        m.setToken(m.getToken());
    }

    private static void verifyDataUpdate(POUserAndSubscriptions user) {
        assertEquals(1234, user.getKey());
        assertEquals(7, user.getVersion());
        assertEquals(Salutation.MR, user.getSalutation());
        assertEquals("email@provider.com", user.getEmail());
        assertEquals("Hans", user.getFirstName());
        assertEquals("Wurst", user.getLastName());
        assertEquals("de", user.getLocale());
        assertEquals("userid", user.getUserId());
        assertEquals(EnumSet.of(UserRoleType.ORGANIZATION_ADMIN),
                user.getAssignedRoles());

        List<POSubscription> subs = user.getSubscriptions();
        assertEquals(3, subs.size());
        POSubscription sub = subs.get(0);
        assertEquals("sub2", sub.getId());
        POServiceRole role = sub.getUsageLicense().getPoServieRole();
        assertNotNull(role);
        assertEquals("role1", role.getId());
        assertEquals(5, role.getKey());
        assertEquals(1, sub.getUsageLicense().getVersion());
        assertEquals(111, sub.getUsageLicense().getKey());

        sub = subs.get(2);
        assertEquals("sub1", sub.getId());
        assertNull(sub.getUsageLicense().getPoServieRole());
    }

    private void givenAuthMode(boolean isInternalAuthMode) {
        when(Boolean.valueOf(applicationBean.isInternalAuthMode())).thenReturn(
                Boolean.valueOf(isInternalAuthMode));
    }
}
