/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 24.04.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.beans;

import static org.oscm.internal.types.enumtypes.SettingType.LDAP_ATTR_FIRST_NAME;
import static org.oscm.internal.types.enumtypes.SettingType.LDAP_ATTR_LAST_NAME;
import static org.oscm.internal.types.enumtypes.SettingType.LDAP_ATTR_LOCALE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;

import org.oscm.types.constants.marketplace.Marketplace;
import org.oscm.ui.dialog.state.TableState;
import org.oscm.ui.model.User;
import org.oscm.ui.model.UserRole;
import org.oscm.ui.stubs.FacesContextStub;
import org.oscm.internal.intf.ConfigurationService;
import org.oscm.internal.intf.IdentityService;
import org.oscm.internal.types.enumtypes.ConfigurationKey;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.types.exception.ValidationException.ReasonEnum;
import org.oscm.internal.vo.VOConfigurationSetting;
import org.oscm.internal.vo.VOUserDetails;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author weiser
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class UserImportBeanTest {

    private static final String UNIT = "%";
    private static final String MARKETPLACE_ID = "mpid";

    private UserImportBean bean;
    private User user;

    private UserRole userRole0, userRole1;

    @Mock
    private IdentityService identityService;
    private ConfigurationService configurationService;

    @Mock
    private TableState ts;

    @Spy
    private BaseBean bb = new BaseBean();

    @Before
    public void setup() throws Exception {
        new FacesContextStub(null);

        bean = spy(new UserImportBean());
        user = new User(new VOUserDetails());
        user.setFirstName("firstName");
        user.setLastName("lastName");
        user.setLocale("en");

        List<UserRoleType> availableUserRoles = new ArrayList<>();
        availableUserRoles.add(UserRoleType.BROKER_MANAGER);
        availableUserRoles.add(UserRoleType.RESELLER_MANAGER);

        doReturn(availableUserRoles).when(identityService)
                .getAvailableUserRoles(any(VOUserDetails.class));
        configurationService = mock(ConfigurationService.class);

        doReturn(new ArrayList<VOUserDetails>()).when(identityService)
                .searchLdapUsers(any(String.class));

        VOConfigurationSetting confSetting = new VOConfigurationSetting();
        confSetting.setValue("1");
        doReturn(confSetting).when(configurationService)
                .getVOConfigurationSetting(any(ConfigurationKey.class),
                        any(String.class));

        List<UserRole> userRoles = new ArrayList<>();
        userRole0 = new UserRole();
        userRole0.setUserRoleType(UserRoleType.BROKER_MANAGER);
        userRoles.add(userRole0);

        userRole1 = new UserRole();
        userRole1.setUserRoleType(UserRoleType.MARKETPLACE_OWNER);
        userRoles.add(userRole1);

        bean.setUserRolesForNewUser(userRoles);

        doReturn(user).when(bb).getUserFromSession();

        bean.setIdentityService(identityService);
        bean.setBaseBean(bb);
        bean.setConfigurationService(configurationService);

        doNothing().when(bb).addMessage(anyString(), any(Severity.class),
                anyString(), any(Object[].class));
        doReturn(MARKETPLACE_ID).when(bb).getMarketplaceId();

        bean.setTableState(ts);
    }

    /**
     * We have uid, user id and mail always visible.
     */
    @Test
    public void getColumnWidth() {
        String expected = String.valueOf(UserImportBean.BASE_WIDTH / 3) + UNIT;
        assertEquals(expected, bean.getColumnWidth());
    }

    @Test
    public void getColumnWidth_FirstName() {
        user.setRemoteLdapAttributes(Arrays.asList(LDAP_ATTR_FIRST_NAME));

        String expected = String.valueOf(UserImportBean.BASE_WIDTH / 4) + UNIT;
        assertEquals(expected, bean.getColumnWidth());
    }

    @Test
    public void getColumnWidth_LastName() {
        user.setRemoteLdapAttributes(Arrays.asList(LDAP_ATTR_LAST_NAME));

        String expected = String.valueOf(UserImportBean.BASE_WIDTH / 4) + UNIT;
        assertEquals(expected, bean.getColumnWidth());
    }

    @Test
    public void getColumnWidth_Locale() {
        user.setRemoteLdapAttributes(Arrays.asList(LDAP_ATTR_LOCALE));

        String expected = String.valueOf(UserImportBean.BASE_WIDTH / 4) + UNIT;
        assertEquals(expected, bean.getColumnWidth());
    }

    @Test
    public void getColumnWidth_All() {
        user.setRemoteLdapAttributes(Arrays.asList(LDAP_ATTR_LOCALE,
                LDAP_ATTR_LAST_NAME, LDAP_ATTR_FIRST_NAME));

        String expected = String.valueOf(UserImportBean.BASE_WIDTH / 6) + UNIT;
        assertEquals(expected, bean.getColumnWidth());
    }

    @Test
    public void isImportDisabled() {
        bean.setUsers(Arrays.asList(user));

        assertTrue(bean.isImportDisabled());
    }

    @Test
    public void isImportDisabled_ListNull() {
        assertTrue(bean.isImportDisabled());
    }

    @Test
    public void isImportDisabled_UserSelected() {
        user.setSelected(true);
        bean.setUsers(Arrays.asList(user));

        assertFalse(bean.isImportDisabled());
    }

    @Test
    public void isImportDisabled_EmptyList() {
        bean.setUsers(new ArrayList<User>());

        assertTrue(bean.isImportDisabled());
    }

    /**
     * Bug 9177 - show a more specific error message on import error with user
     * with leading/trailing blanks on email.
     */
    @Test
    public void importUsers_MessageOnInvalidMail() throws Exception {
        final String email = " mail@mail.de ";

        // given
        givenClassicPortalUserAndRoleSelection();
        
        willThrow(
                new ValidationException(ReasonEnum.EMAIL, "email",
                        new Object[] { email })).given(identityService)
                .importLdapUsers(anyListOf(VOUserDetails.class), anyString());

        // when
        bean.importUsers();
        // then
        verify(bb, times(1)).addMessage(eq((String) null),
                eq(FacesMessage.SEVERITY_ERROR),
                eq(UserImportBean.ERROR_IMPORT_EMAIL), eq(email));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void importUsers() throws Exception {
        // given
        givenClassicPortalUserAndRoleSelection();

        // when
        bean.importUsers();

        // then
        ArgumentCaptor<List> argument = ArgumentCaptor.forClass(List.class);

        verify(identityService, times(1)).importLdapUsers(argument.capture(),
                anyString());

        List<VOUserDetails> list = argument.getValue();
        VOUserDetails details = list.get(0);

        // ensure import the correct user
        assertEquals(details.getFirstName(), user.getFirstName());
        assertEquals(details.getLastName(), user.getLastName());
        assertEquals(details.getLocale(), user.getLocale());
        // ensure user with correct role
        Set<UserRoleType> roles = details.getUserRoles();
        assertEquals(1, roles.size());
        assertEquals(roles.iterator().next(), UserRoleType.BROKER_MANAGER);
        verify(ts).resetActivePages();
    }

    @Test
    public void searchUsers_NotOverLimit() throws Exception {
        // given
        doReturn(false).when(identityService).searchLdapUsersOverLimit(
                any(String.class));

        // when
        String result = bean.searchUsers();

        // then
        assertEquals(BaseBean.OUTCOME_SUCCESS, result);
    }

    @Test
    public void searchUsers_OverLimit() throws Exception {
        // given
        doReturn(true).when(identityService).searchLdapUsersOverLimit(
                any(String.class));

        // when
        String result = bean.searchUsers();

        // then
        verify(bb, times(1)).addMessage(anyString(),
                eq(FacesMessage.SEVERITY_ERROR),
                eq(BaseBean.ERROR_USER_LDAP_SEARCH_LIMIT_EXCEEDED),
                any(Object[].class));
        assertEquals(BaseBean.OUTCOME_SUCCESS, result);
    }

    @Test
    public void getMarketplaceIdIfContextsSet_Marketplace() throws Exception {
        // given
        givenMarketplaceUserAndRoleSelection();

        ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);

        // when
        bean.importUsers();

        // then
        verify(identityService, times(1)).importLdapUsers(
                anyListOf(VOUserDetails.class), argument.capture());
        verify(bean, times(1)).getMarketplaceIdForUserImport();
        assertEquals(MARKETPLACE_ID, argument.getValue());
    }

    @Test
    public void getMarketplaceIdIfContextsSet_ClassicPortal() throws Exception {

        // given
        givenClassicPortalUserAndRoleSelection();

        ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);

        // when
        bean.importUsers();

        // then
        verify(identityService, times(1)).importLdapUsers(
                anyListOf(VOUserDetails.class), argument.capture());
        verify(bean, times(1)).getMarketplaceIdForUserImport();
        assertEquals(null, argument.getValue());
    }

    private void givenContext(String context) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getServletPath()).thenReturn(context + "/user/import.jsf");
        when(bb.getRequest()).thenReturn(request);
    }
    
    private void givenMarketplaceUserAndRoleSelection() throws ValidationException {
        givenContext(Marketplace.MARKETPLACE_ROOT);
        givenUserAndRoleSelection();
    }
    
    private void givenClassicPortalUserAndRoleSelection() throws ValidationException {
        givenContext("");
        givenUserAndRoleSelection();
    }

    private void givenUserAndRoleSelection() throws ValidationException {
        user.setSelected(true);
        bean.setUsers(Arrays.asList(user));
        userRole0.setSelected(true);
        userRole1.setSelected(false);
        doReturn(BaseBean.OUTCOME_SUCCESS).when(bean).searchUsers();
    }
}
