/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 05.09.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.classic.manageusers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.faces.application.FacesMessage;
import javax.servlet.http.HttpSession;

import org.junit.Before;
import org.junit.Test;

import org.oscm.ui.beans.ApplicationBean;
import org.oscm.ui.beans.BaseBean;
import org.oscm.ui.beans.SessionBean;
import org.oscm.ui.common.Constants;
import org.oscm.ui.common.UiDelegate;
import org.oscm.ui.delegates.ServiceLocator;
import org.oscm.ui.dialog.state.TableState;
import org.oscm.internal.components.response.Response;
import org.oscm.internal.intf.IdentityService;
import org.oscm.internal.types.enumtypes.Salutation;
import org.oscm.internal.types.enumtypes.SettingType;
import org.oscm.internal.types.enumtypes.UserAccountStatus;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.SaaSApplicationException;
import org.oscm.internal.types.exception.TechnicalServiceNotAliveException;
import org.oscm.internal.types.exception.TechnicalServiceOperationException;
import org.oscm.internal.usermanagement.POUser;
import org.oscm.internal.usermanagement.POUserDetails;
import org.oscm.internal.usermanagement.UserManagementService;
import org.oscm.internal.usermanagement.UserService;

/**
 * @author weiser
 * 
 */
@SuppressWarnings("boxing")
public class ManageUsersCtrlTest {

    private String messageKey;
    private static final String NAME = "name";
    private static final String MP_ID = "mpid";
    private static final String FIRSTNAME = "firstName";
    private static final String LASTNAME = "lastName";
    private ManageUsersCtrl ctrl;

    private ManageUsersModel model;
    private UserService us;
    private HttpSession session;

    private List<POUser> ul;
    private POUserDetails ud;
    private TableState ts;
    private SessionBean sessionBean;
    private boolean isNameSequenceReversed = false;
    private ServiceLocator sl;

    @Before
    public void setup() throws Exception {
        model = spy(new ManageUsersModel());
        model.setToken(model.getToken());

        ctrl = spy(new ManageUsersCtrl() {
            @Override
            public void addMessage(final String clientId,
                    final FacesMessage.Severity severity, final String key) {
                messageKey = key;
            }
        });

        ctrl.ui = mock(UiDelegate.class);
        sl = mock(ServiceLocator.class);
        us = mock(UserService.class);
        IdentityService is = mock(IdentityService.class);

        givenUsers();

        when(us.getUsers()).thenReturn(ul);
        when(us.getUserDetails(eq(ud.getUserId()), anyString())).thenReturn(ud);

        session = mock(HttpSession.class);
        when(ctrl.ui.getSession(anyBoolean())).thenReturn(session);

        when(ctrl.ui.findBean(eq(ManageUsersCtrl.MANAGE_USERS_MODEL)))
                .thenReturn(model);
        when(ctrl.ui.getText(anyString())).thenReturn(NAME);
        when(ctrl.ui.getMarketplaceId()).thenReturn(MP_ID);

        when(sl.findService(UserService.class)).thenReturn(us);
        when(sl.findService(IdentityService.class)).thenReturn(is);

        UserManagementService ums = mock(UserManagementService.class);
        when(Boolean.valueOf(ums.isOrganizationLDAPManaged())).thenReturn(
                Boolean.TRUE);
        when(sl.findService(UserManagementService.class)).thenReturn(ums);

        ts = mock(TableState.class);
        when(ctrl.ui.findBean(TableState.BEAN_NAME)).thenReturn(ts);

        ctrl.appBean = mock(ApplicationBean.class);
        when(Boolean.valueOf(ctrl.appBean.isInternalAuthMode())).thenReturn(
                Boolean.TRUE);
        sessionBean = new SessionBean() {
            private static final long serialVersionUID = -3460694623349548600L;

            @Override
            public boolean getNameSequenceReversed() {
                return isNameSequenceReversed;
            }
        };
        sessionBean.setSelectedUserId(ud.getUserId());
        sessionBean.setTenantID("1");
        ctrl.setSessionBean(sessionBean);
        ctrl.setServiceLocator(sl);
    }

    private List<POUser> givenUsers() {
        ul = new ArrayList<POUser>();
        ud = new POUserDetails();
        ud.setEmail("email");
        ud.setFirstName("firstName");
        ud.setKey(1234);
        ud.setLastName("lastName");
        ud.setLocale("en");
        ud.setSalutation(Salutation.MR);
        ud.setUserId("userId");
        ud.setVersion(5);
        ud.setStatus(UserAccountStatus.ACTIVE);
        ul.add(ud);
        return ul;
    }

    @Test
    public void getInitialize_Initialized() {
        model.setInitialized(true);
        reset(model); // for not counting method calls done so far

        String result = ctrl.getInitialize();

        assertEquals("", result);
        verify(model, times(1)).isInitialized();
        verifyZeroInteractions(sl, us);
    }

    @Test
    public void getInitialize() {
        sessionBean.setSelectedUserId(null);
        String result = ctrl.getInitialize();
        verify(model, times(1)).setResetPasswordButtonVisible(anyBoolean());
        assertEquals("", result);
        assertEquals(Boolean.TRUE, Boolean.valueOf(model.isInitialized()));
        assertSame(ul, model.getUsers());
        verifyFieldsNoSelection(model);
    }

    @Test
    public void getInitialize_ListOnly() {
        // initialize page and select user
        ctrl.getInitialize();
        ctrl.setSelectedUserId(ud.getUserId());
        ctrl.resetListOnly = true;
        List<POUser> list = Arrays.asList(new POUser());
        when(us.getUsers()).thenReturn(list);

        String result = ctrl.getInitialize();

        assertEquals("", result);
        assertEquals(Boolean.TRUE, Boolean.valueOf(model.isInitialized()));
        assertSame(list, model.getUsers());
        assertNotSame(ul, model.getUsers());
        verifyFieldsSelection(model, ud, false, false, false);
    }

    @Test
    public void getInitialize_Selection() {
        model.setSelectedUserId(ud.getUserId());

        String result = ctrl.getInitialize();

        assertEquals("", result);
        assertEquals(Boolean.TRUE, Boolean.valueOf(model.isInitialized()));
        assertSame(ul, model.getUsers());
        verifyFieldsSelection(model, ud, false, false, false);
    }

    @Test
    public void getInitialize_SelectionNoSalutation() {
        ud.setSalutation(null);
        model.setSelectedUserId(ud.getUserId());

        String result = ctrl.getInitialize();

        assertEquals("", result);
        assertEquals(Boolean.TRUE, Boolean.valueOf(model.isInitialized()));
        assertSame(ul, model.getUsers());
        verifyFieldsSelection(model, ud, false, false, false);
    }

    @Test
    public void getInitialize_SelectionLocked() {
        ud.setStatus(UserAccountStatus.LOCKED_FAILED_LOGIN_ATTEMPTS);
        model.setSelectedUserId(ud.getUserId());

        String result = ctrl.getInitialize();

        assertEquals("", result);
        assertEquals(Boolean.TRUE, Boolean.valueOf(model.isInitialized()));
        assertSame(ul, model.getUsers());
        assertEquals(Boolean.TRUE, Boolean.valueOf(model.isLocked()));
        verifyFieldsSelection(model, ud, true, true, true);
    }

    @Test
    public void getInitialize_SelectionMapped() {
        ud.setMappedAttributes(EnumSet.allOf(SettingType.class));
        model.setSelectedUserId(ud.getUserId());

        String result = ctrl.getInitialize();

        assertEquals("", result);
        assertEquals(Boolean.TRUE, Boolean.valueOf(model.isInitialized()));
        assertSame(ul, model.getUsers());
        verifyFieldsSelection(model, ud, true, false, false);
    }

    @Test
    public void getInitialize_SelectionNotFound() throws Exception {
        when(us.getUserDetails(eq(ud.getUserId()), anyString())).thenThrow(
                new ObjectNotFoundException());
        model.setSelectedUserId(ud.getUserId());

        String result = ctrl.getInitialize();

        assertEquals("", result);
        assertEquals(Boolean.TRUE, Boolean.valueOf(model.isInitialized()));
        assertEquals(null, sessionBean.getSelectedUserId());
        assertSame(ul, model.getUsers());
        verifyFieldsNoSelection(model);
        verify(ctrl.ui, times(1)).handleException(
                any(SaaSApplicationException.class));
    }

    @Test
    public void getInitialize_NotInternalAuthMode() throws Exception {
        // given
        model.setSelectedUserId(ud.getUserId());
        when(Boolean.valueOf(ctrl.appBean.isInternalAuthMode())).thenReturn(
                Boolean.FALSE);

        // when
        String result = ctrl.getInitialize();

        // then
        assertEquals("", result);
        assertEquals(Boolean.TRUE, Boolean.valueOf(model.isInitialized()));
        assertSame(ul, model.getUsers());
        verifyFieldsSelection(model, ud, false, false, false);
    }

    @Test
    public void setSelectedUserId() {
        ctrl.setSelectedUserId(ud.getUserId());

        verifyFieldsSelection(model, ud, false, false, false);
    }

    @Test
    public void setSelectedUserId_InvalidLocale() {
        // given
        ud.setLocale("de");

        // when
        ctrl.setSelectedUserId(ud.getUserId());

        // then
        verify(ctrl.appBean, times(1)).checkLocaleValidation("de");
    }

    @Test
    public void setSelectedUserId_SelectionNotFound() throws Exception {
        when(us.getUserDetails(eq(ud.getUserId()), anyString())).thenThrow(
                new ObjectNotFoundException());
        // given
        sessionBean.setSelectedUserId(null);
        ctrl.getInitialize();

        // when
        ctrl.setSelectedUserId(ud.getUserId());

        // then
        verifyFieldsNoSelection(model);
        verify(ctrl.ui, times(1)).handleException(
                any(SaaSApplicationException.class));
    }

    @Test
    public void initUserRoles_Empty() {
        List<UserRole> roles = ctrl.initUserRoles(ud);

        assertEquals(Boolean.TRUE, Boolean.valueOf(roles.isEmpty()));
    }

    @Test
    public void initUserRoles() {
        Set<UserRoleType> set = EnumSet.allOf(UserRoleType.class);
        ud.setAvailableRoles(set);

        List<UserRole> roles = ctrl.initUserRoles(ud);

        verifyUserRoles(set, roles, false);
    }

    @Test
    public void initUserRoles_AllSelected() {
        Set<UserRoleType> set = EnumSet.allOf(UserRoleType.class);
        ud.setAvailableRoles(set);
        ud.setAssignedRoles(set);

        List<UserRole> roles = ctrl.initUserRoles(ud);

        verifyUserRoles(set, roles, true);
    }

    @Test
    public void getSelectedUserRoles() {
        List<UserRole> list = new ArrayList<UserRole>();
        list.add(new UserRole(UserRoleType.BROKER_MANAGER, "name", true));
        list.add(new UserRole(UserRoleType.ORGANIZATION_ADMIN, "name", false));

        Set<UserRoleType> set = ctrl.getSelectedUserRoles(list);

        assertEquals(EnumSet.of(UserRoleType.BROKER_MANAGER), set);
    }

    @Test
    public void save() throws Exception {
        // initialize page and select user
        sessionBean.setSelectedUserId(null);
        ctrl.getInitialize();
        ctrl.setSelectedUserId(ud.getUserId());

        ctrl.save();

        verify(us, times(1)).saveUser(any(POUserDetails.class));
        assertEquals(Boolean.FALSE, Boolean.valueOf(model.isInitialized()));
        assertEquals(ud.getUserId(), sessionBean.getSelectedUserId());
        verify(ctrl.ui, times(1)).handle(any(Response.class),
                eq(BaseBean.INFO_USER_SAVED), eq(ud.getUserId()));
    }

    @Test
    public void save_Salutation() throws Exception {
        // initialize page and select user
        ctrl.getInitialize();
        ctrl.setSelectedUserId(ud.getUserId());
        model.getSalutation().setValue(Salutation.MR.name());

        ctrl.save();

        verify(us, times(1)).saveUser(any(POUserDetails.class));
        assertEquals(Boolean.FALSE, Boolean.valueOf(model.isInitialized()));
        verify(ctrl.ui, times(1)).handle(any(Response.class),
                eq(BaseBean.INFO_USER_SAVED), eq(ud.getUserId()));
    }

    @Test
    public void save_InvalidToken() throws Exception {
        model.setToken("anotherToken");
        ctrl.model = model;

        ctrl.save();

        verifyZeroInteractions(ctrl.ui, sl, us);
    }

    @Test
    public void save_SelfChangeId() throws Exception {
        final String newUserId = "newUserId";
        when(ctrl.ui.getMyUserId()).thenReturn(ud.getUserId());
        // initialize page and select user
        ctrl.getInitialize();
        ctrl.setSelectedUserId(ud.getUserId());
        model.getUserId().setValue(newUserId);

        ctrl.save();

        verify(us, times(1)).saveUser(any(POUserDetails.class));
        assertEquals(Boolean.FALSE, Boolean.valueOf(model.isInitialized()));
        assertEquals(newUserId, model.getSelectedUserId());
        verify(ctrl.ui, times(1)).handle(any(Response.class),
                eq(BaseBean.INFO_USER_SAVED_ITSELF), eq(newUserId));
        verify(ctrl.ui, times(1)).updateAndVerifyViewLocale();
        verify(session, times(1)).setAttribute(eq(Constants.SESS_ATTR_USER),
                any());
    }

    @Test(expected = ObjectNotFoundException.class)
    public void save_Negative() throws Exception {
        when(us.saveUser(any(POUserDetails.class))).thenThrow(
                new ObjectNotFoundException());
        ctrl.getInitialize();
        ctrl.setSelectedUserId(ud.getUserId());

        try {
            ctrl.save();
        } finally {
            assertEquals(Boolean.TRUE, Boolean.valueOf(model.isInitialized()));
        }
    }

    @Test
    public void save_Concurrent() throws Exception {
        when(us.saveUser(any(POUserDetails.class))).thenThrow(
                new ConcurrentModificationException());
        // initialize page and select user
        ctrl.getInitialize();
        ctrl.setSelectedUserId(ud.getUserId());

        try {
            ctrl.save();
        } finally {
            assertEquals(Boolean.TRUE, Boolean.valueOf(model.isInitialized()));
            assertEquals(Boolean.TRUE, Boolean.valueOf(ctrl.resetListOnly));
            verify(ctrl.ui, times(1)).handleException(
                    any(ConcurrentModificationException.class));
        }
    }

    @Test
    public void delete() throws Exception {
        Set<UserRoleType> set = EnumSet.allOf(UserRoleType.class);
        ud.setAvailableRoles(set);
        ud.setAssignedRoles(set);
        // initialize page and select user
        ctrl.getInitialize();
        ctrl.setSelectedUserId(ud.getUserId());

        ctrl.delete();

        verify(us, times(1)).deleteUser(any(POUser.class), eq(MP_ID),
                anyString());
        assertEquals(Boolean.FALSE, Boolean.valueOf(model.isInitialized()));
        assertNull(model.getSelectedUserId());
        verify(ctrl.ui, times(1)).handle(any(Response.class),
                eq(BaseBean.INFO_USER_DELETED), eq(ud.getUserId()));
        assertEquals(Boolean.TRUE, Boolean.valueOf(model.getRoles().isEmpty()));
        verify(ts).resetActivePages();
    }

    @Test
    public void delete_InvalidToken() throws Exception {
        model.setToken("anotherToken");
        ctrl.model = model;

        ctrl.delete();

        verifyZeroInteractions(ctrl.ui, sl, us);
    }

    @Test
    public void delete_TechnicalServiceNotAliveException() throws Exception {
        // give
        doReturn(new POUser()).when(ctrl).toPOUser(model);
        ctrl.model = model;
        doReturn(Boolean.TRUE).when(ctrl.model).isTokenValid();
        doThrow(new TechnicalServiceNotAliveException()).when(us).deleteUser(
                any(org.oscm.internal.usermanagement.POUser.class),
                anyString(), anyString());
        // when
        String result = ctrl.delete();
        // then

        assertEquals(BaseBean.ERROR_DELETE_USER_FROM_EXPIRED_SUBSCRIPTION,
                messageKey);
        assertEquals("", result);
    }

    @Test
    public void delete_TechnicalServiceOperationException() throws Exception {
        // give
        doReturn(new POUser()).when(ctrl).toPOUser(model);
        ctrl.model = model;
        doReturn(Boolean.TRUE).when(ctrl.model).isTokenValid();
        doThrow(new TechnicalServiceOperationException()).when(us).deleteUser(
                any(org.oscm.internal.usermanagement.POUser.class),
                anyString(), anyString());
        // when
        String result = ctrl.delete();
        // then

        assertEquals(BaseBean.ERROR_DELETE_USER_FROM_EXPIRED_SUBSCRIPTION,
                messageKey);
        assertEquals("", result);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void delete_Negative() throws Exception {
        when(us.deleteUser(any(POUser.class), anyString(), anyString()))
                .thenThrow(new OperationNotPermittedException());
        // initialize page and select user
        ctrl.getInitialize();
        ctrl.setSelectedUserId(ud.getUserId());

        try {
            ctrl.delete();
        } finally {
            assertEquals(Boolean.TRUE, Boolean.valueOf(model.isInitialized()));
        }
    }

    @Test
    public void resetPassword() throws Exception {
        // initialize page and select user
        ctrl.getInitialize();
        ctrl.setSelectedUserId(ud.getUserId());

        ctrl.resetPassword();

        verify(us, times(1)).resetUserPassword(any(POUser.class), eq(MP_ID));
        assertEquals(Boolean.FALSE, Boolean.valueOf(model.isInitialized()));
        verify(ctrl.ui, times(1)).handle(any(Response.class),
                eq(BaseBean.INFO_USER_PWD_RESET));
    }

    @Test
    public void resetPassword_InvalidToken() throws Exception {
        model.setToken("anotherToken");
        ctrl.model = model;

        ctrl.resetPassword();

        verifyZeroInteractions(ctrl.ui, sl, us);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void resetPassword_Negative() throws Exception {
        when(us.resetUserPassword(any(POUser.class), anyString())).thenThrow(
                new OperationNotPermittedException());
        // initialize page and select user
        ctrl.getInitialize();
        ctrl.setSelectedUserId(ud.getUserId());

        try {
            ctrl.resetPassword();
        } finally {
            assertEquals(Boolean.TRUE, Boolean.valueOf(model.isInitialized()));
        }
    }

    @Test
    public void isSaveDisabled_NoSelection() {
        ctrl.model = new ManageUsersModel();

        assertEquals(Boolean.TRUE, Boolean.valueOf(ctrl.isSaveDisabled()));
    }

    @Test
    public void isSaveDisabled_Locked() {
        ctrl.model = new ManageUsersModel();
        ctrl.model.setLocked(true);
        ctrl.model.setSelectedUserId("selectedUserId");

        assertEquals(Boolean.TRUE, Boolean.valueOf(ctrl.isSaveDisabled()));
    }

    @Test
    public void isSaveDisabled() {
        ctrl.model = new ManageUsersModel();
        ctrl.model.setSelectedUserId("selectedUserId");

        assertEquals(Boolean.FALSE, Boolean.valueOf(ctrl.isSaveDisabled()));
    }

    @Test
    public void isResetPasswordDisabled_NoSelection() {
        ctrl.model = new ManageUsersModel();

        assertEquals(Boolean.TRUE,
                Boolean.valueOf(ctrl.isResetPasswordDisabled()));
    }

    @Test
    public void isResetPasswordDisabled_SameUser() {
        String selectedUserId = "selectedUserId";
        when(ctrl.ui.getMyUserId()).thenReturn(selectedUserId);
        ctrl.model = new ManageUsersModel();
        ctrl.model.setSelectedUserId(selectedUserId);

        assertEquals(Boolean.TRUE,
                Boolean.valueOf(ctrl.isResetPasswordDisabled()));
    }

    @Test
    public void isResetPasswordDisabled() {
        ctrl.model = new ManageUsersModel();
        ctrl.model.setSelectedUserId("selectedUserId");

        assertEquals(Boolean.FALSE,
                Boolean.valueOf(ctrl.isResetPasswordDisabled()));
    }

    @Test
    public void isResetPasswordVisible_NoSelection() {
        ctrl.model = new ManageUsersModel();
        ctrl.model.setResetPasswordButtonVisible(false);

        assertEquals(Boolean.FALSE,
                Boolean.valueOf(ctrl.isResetPasswordVisible()));
    }

    @Test
    public void isResetPasswordVisible() {
        ctrl.model = new ManageUsersModel();
        ctrl.model.setResetPasswordButtonVisible(true);

        assertEquals(Boolean.TRUE,
                Boolean.valueOf(ctrl.isResetPasswordVisible()));
    }

    @Test
    public void isDeleteDisabled_NoSelection() {
        ctrl.model = new ManageUsersModel();

        assertEquals(Boolean.TRUE, Boolean.valueOf(ctrl.isDeleteDisabled()));
    }

    @Test
    public void isDeleteDisabled_SameUser() {
        String selectedUserId = "selectedUserId";
        when(ctrl.ui.getMyUserId()).thenReturn(selectedUserId);
        ctrl.model = new ManageUsersModel();
        ctrl.model.setSelectedUserId(selectedUserId);

        assertEquals(Boolean.TRUE, Boolean.valueOf(ctrl.isDeleteDisabled()));
    }

    @Test
    public void isDeleteDisabled() {
        ctrl.model = new ManageUsersModel();
        ctrl.model.setSelectedUserId("selectedUserId");

        assertEquals(Boolean.FALSE, Boolean.valueOf(ctrl.isDeleteDisabled()));
    }

    @Test
    public void isRolesDisabled_NoSelection() {
        sessionBean.setSelectedUserId(null);
        assertEquals(Boolean.TRUE, Boolean.valueOf(ctrl.isRolesDisabled()));
    }

    @Test
    public void isRolesDisabled_SelectionLocked() {
        model.setSelectedUserId("someid");
        model.setLocked(true);

        assertEquals(Boolean.TRUE, Boolean.valueOf(ctrl.isRolesDisabled()));
    }

    @Test
    public void isRolesDisabled() {
        model.setSelectedUserId("someid");

        assertEquals(Boolean.FALSE, Boolean.valueOf(ctrl.isRolesDisabled()));
    }

    @Test
    public void getSelectedUserId_NotExisting() {
        String id = "notExisting";
        ctrl.setSelectedUserId(id);

        String selId = ctrl.getSelectedUserId();

        assertSame(null, selId);
    }

    @Test
    public void getSelectedUserId_Existing() {
        // given
        String id = ud.getUserId();

        // when
        model.setSelectedUserId(id);

        String selId = ctrl.getSelectedUserId();

        assertSame(id, selId);
    }

    @Test
    public void getModel() {

        ManageUsersModel m = ctrl.getModel();

        assertSame("userId", m.getSelectedUserId());
    }

    @Test
    public void updateSelectedUser() {
        // given
        model.setSelectedUserId(ud.getUserId());
        // when
        ctrl.updateSelectedUser(model, ud);
        // then
        verifyFieldsSelection(model, ud, false, false, false);
    }

    @Test
    public void updateSelectedUser_LDAP() {
        // given
        model.setSelectedUserId(ud.getUserId());
        Set<SettingType> mappedAttributes = new HashSet<SettingType>();
        mappedAttributes.add(SettingType.LDAP_ATTR_EMAIL);
        mappedAttributes.add(SettingType.LDAP_ATTR_FIRST_NAME);
        mappedAttributes.add(SettingType.LDAP_ATTR_LAST_NAME);
        mappedAttributes.add(SettingType.LDAP_ATTR_LOCALE);
        mappedAttributes.add(SettingType.LDAP_ATTR_UID);
        ud.setMappedAttributes(mappedAttributes);
        // when
        ctrl.updateSelectedUser(model, ud);
        // then
        verifyFieldsSelection(model, ud, true, false, false);
    }

    @Test
    public void updateSelectedUser_Locked() {
        // given
        model.setSelectedUserId(ud.getUserId());
        ud.setStatus(UserAccountStatus.LOCKED_FAILED_LOGIN_ATTEMPTS);
        // when
        ctrl.updateSelectedUser(model, ud);
        // then
        verifyFieldsSelection(model, ud, true, true, true);
    }

    @Test
    public void updateSelectedUser_NotInternalAuthMode_CurrentAdminUser() {
        // given
        model.setSelectedUserId(ud.getUserId());
        when(ctrl.ui.getMyUserId()).thenReturn(ud.getUserId());
        when(Boolean.valueOf(ctrl.appBean.isInternalAuthMode())).thenReturn(
                Boolean.FALSE);
        // when
        ctrl.updateSelectedUser(model, ud);
        // then
        assertEquals(Boolean.FALSE,
                Boolean.valueOf(model.getSalutation().isReadOnly()));
        assertEquals(Boolean.FALSE,
                Boolean.valueOf(model.getEmail().isReadOnly()));
        assertEquals(Boolean.FALSE,
                Boolean.valueOf(model.getFirstName().isReadOnly()));
        assertEquals(Boolean.FALSE,
                Boolean.valueOf(model.getLastName().isReadOnly()));
        assertEquals(Boolean.FALSE,
                Boolean.valueOf(model.getLocale().isReadOnly()));
        assertEquals(Boolean.TRUE,
                Boolean.valueOf(model.getUserId().isReadOnly()));
    }

    @Test
    public void updateSelectedUser_NotInternalAuthMode_OtherUser() {
        // given
        model.setSelectedUserId(ud.getUserId());
        when(Boolean.valueOf(ctrl.appBean.isInternalAuthMode())).thenReturn(
                Boolean.FALSE);
        // when
        ctrl.updateSelectedUser(model, ud);
        // then
        verifyFieldsSelection(model, ud, false, false, false);
    }

    @Test
    public void getDataTableHeaders() {
        // when
        List<String> headers = ctrl.getDataTableHeaders();

        // then
        assertEquals(5, headers.size());
        assertEquals("userId", headers.get(0));
    }

    @Test
    public void getDataTableHeaders_reverseNameSequence() {
        // when
        this.isNameSequenceReversed = true;
        List<String> headers = ctrl.getDataTableHeaders();

        // then
        assertEquals(5, headers.size());
        assertEquals("userId", headers.get(0));
        assertEquals(LASTNAME, headers.get(1));
        assertEquals(FIRSTNAME, headers.get(2));
    }

    @Test
    public void getUserDetailsAndCheckLocaleValidation_Exception()
            throws Exception {
        // given
        String selectedUserId = "selectedUserId";
        when(us.getUserDetails(eq(selectedUserId), anyString())).thenThrow(
                new SaaSApplicationException());

        // when
        ctrl.getUserDetailsAndValidateLocale(selectedUserId);

        // then
        verify(ctrl.ui, times(1)).handleException(
                any(SaaSApplicationException.class));
    }

    @Test
    public void getUserDetailsAndCheckLocaleValidation_UserDetailsIsNull()
            throws Exception {
        // given
        String selectedUserId = "selectedUserId";
        when(us.getUserDetails(eq(selectedUserId), anyString())).thenReturn(
                null);

        // when
        ctrl.getUserDetailsAndValidateLocale(selectedUserId);

        // then
        verify(ctrl.appBean, never()).checkLocaleValidation(anyString());
    }

    @Test
    public void getUserDetailsAndCheckLocaleValidation_UserLocaleIsNull()
            throws Exception {
        // given
        String selectedUserId = "selectedUserId";
        POUserDetails result = new POUserDetails();
        result.setLocale(null);
        when(us.getUserDetails(eq(selectedUserId), anyString())).thenReturn(
                result);

        // when
        ctrl.getUserDetailsAndValidateLocale(selectedUserId);

        // then
        verify(ctrl.appBean, never()).checkLocaleValidation(anyString());
    }

    @Test
    public void getUserDetailsAndCheckLocaleValidation() throws Exception {
        // given
        String selectedUserId = "selectedUserId";
        String locale = "en";
        POUserDetails result = new POUserDetails();
        result.setLocale(locale);
        when(us.getUserDetails(eq(selectedUserId), anyString())).thenReturn(
                result);

        // when
        ctrl.getUserDetailsAndValidateLocale(selectedUserId);

        // then
        verify(ctrl.appBean, times(1)).checkLocaleValidation(eq(locale));
    }

    private void verifyUserRoles(Set<UserRoleType> set, List<UserRole> roles,
            boolean selected) {
        for (UserRole r : roles) {
            assertEquals(NAME, r.getName());
            assertEquals(selected, r.isSelected());
            assertEquals(Boolean.TRUE, Boolean.valueOf(set.remove(r.getType())));
        }
        assertEquals(Boolean.TRUE, Boolean.valueOf(set.isEmpty()));
    }

    private static void verifyFieldsSelection(ManageUsersModel m,
            POUserDetails ud, boolean readOnly, boolean salReadOnly,
            boolean localReadOnly) {
        assertEquals(salReadOnly, m.getSalutation().isReadOnly());
        assertEquals(readOnly, m.getEmail().isReadOnly());
        assertEquals(readOnly, m.getFirstName().isReadOnly());
        assertEquals(readOnly, m.getLastName().isReadOnly());
        assertEquals(localReadOnly, m.getLocale().isReadOnly());
        assertEquals(readOnly, m.getUserId().isReadOnly());

        assertEquals(ud.getEmail(), m.getEmail().getValue());
        assertEquals(ud.getFirstName(), m.getFirstName().getValue());
        assertEquals(ud.getLastName(), m.getLastName().getValue());
        assertEquals(ud.getLocale(), m.getLocale().getValue());
        if (ud.getSalutation() != null) {
            assertEquals(ud.getSalutation().name(), m.getSalutation()
                    .getValue());
        } else {
            assertNull(m.getSalutation().getValue());
        }
        assertEquals(ud.getUserId(), m.getUserId().getValue());
        assertEquals(ud.getUserId(), m.getSelectedUserId());
        assertEquals(ud.getKey(), m.getKey());
        assertEquals(ud.getVersion(), m.getVersion());

        verifyRequiredAndRendered(m);
    }

    private static void verifyFieldsNoSelection(ManageUsersModel m) {
        assertEquals(Boolean.TRUE, Boolean.valueOf(m.getEmail().isReadOnly()));
        assertEquals(Boolean.TRUE,
                Boolean.valueOf(m.getFirstName().isReadOnly()));
        assertEquals(Boolean.TRUE,
                Boolean.valueOf(m.getLastName().isReadOnly()));
        assertEquals(Boolean.TRUE, Boolean.valueOf(m.getLocale().isReadOnly()));
        assertEquals(Boolean.TRUE,
                Boolean.valueOf(m.getSalutation().isReadOnly()));
        assertEquals(Boolean.TRUE, Boolean.valueOf(m.getUserId().isReadOnly()));

        assertNull(m.getEmail().getValue());
        assertNull(m.getFirstName().getValue());
        assertNull(m.getLastName().getValue());
        assertNull(m.getLocale().getValue());
        assertNull(m.getSalutation().getValue());
        assertNull(m.getUserId().getValue());
        assertNull(m.getSelectedUserId());
        assertEquals(0, m.getKey());
        assertEquals(0, m.getVersion());

        verifyRequiredAndRendered(m);
    }

    private static void verifyRequiredAndRendered(ManageUsersModel m) {
        assertEquals(Boolean.TRUE, Boolean.valueOf(m.getEmail().isRequired()));
        assertEquals(Boolean.TRUE, Boolean.valueOf(m.getLocale().isRequired()));
        assertEquals(Boolean.TRUE, Boolean.valueOf(m.getUserId().isRequired()));

        assertEquals(Boolean.FALSE,
                Boolean.valueOf(m.getFirstName().isRequired()));
        assertEquals(Boolean.FALSE,
                Boolean.valueOf(m.getLastName().isRequired()));
        assertEquals(Boolean.FALSE,
                Boolean.valueOf(m.getSalutation().isRequired()));

        assertEquals(Boolean.TRUE, Boolean.valueOf(m.getEmail().isRendered()));
        assertEquals(Boolean.TRUE, Boolean.valueOf(m.getLocale().isRendered()));
        assertEquals(Boolean.TRUE, Boolean.valueOf(m.getUserId().isRendered()));
        assertEquals(Boolean.TRUE,
                Boolean.valueOf(m.getFirstName().isRendered()));
        assertEquals(Boolean.TRUE,
                Boolean.valueOf(m.getLastName().isRendered()));
        assertEquals(Boolean.TRUE,
                Boolean.valueOf(m.getSalutation().isRendered()));
    }
}
