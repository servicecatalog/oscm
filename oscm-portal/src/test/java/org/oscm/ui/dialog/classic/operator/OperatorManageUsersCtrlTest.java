/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Jun 1, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.classic.operator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.oscm.test.matchers.JavaMatchers.hasItems;
import static org.oscm.test.matchers.JavaMatchers.hasNoItems;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.faces.application.FacesMessage;

import org.junit.Before;
import org.junit.Test;

import org.oscm.internal.intf.AccountService;
import org.oscm.internal.intf.ConfigurationService;
import org.oscm.internal.intf.IdentityService;
import org.oscm.internal.intf.MarketplaceService;
import org.oscm.internal.intf.OperatorService;
import org.oscm.internal.types.enumtypes.ConfigurationKey;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.UserAccountStatus;
import org.oscm.internal.types.exception.MailOperationException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.OrganizationAuthoritiesException;
import org.oscm.internal.usermanagement.POUserAndOrganization;
import org.oscm.internal.usermanagement.UserManagementService;
import org.oscm.internal.vo.VOConfigurationSetting;
import org.oscm.internal.vo.VOMarketplace;
import org.oscm.internal.vo.VOUser;
import org.oscm.internal.vo.VOUserDetails;
import org.oscm.types.constants.Configuration;
import org.oscm.ui.beans.ApplicationBean;
import org.oscm.ui.beans.BaseBean;
import org.oscm.ui.beans.MarketplaceBean;
import org.oscm.ui.common.UiDelegate;
import org.oscm.ui.delegates.ServiceLocator;
import org.oscm.ui.model.Marketplace;

/**
 * @author ZhouMin
 *
 */
public class OperatorManageUsersCtrlTest {

    private VOUser user;
    private ServiceLocator sl;
    private OperatorManageUsersCtrl bean;
    private final ApplicationBean appBean = mock(ApplicationBean.class);
    private final UserManagementService userManageService = mock(UserManagementService.class);
    private final IdentityService idService = mock(IdentityService.class);
    private final AccountService accountingService = mock(AccountService.class);
    private final ConfigurationService configurationService = mock(ConfigurationService.class);
    private final MarketplaceService marketplaceService = mock(MarketplaceService.class);
    private MarketplaceBean marketplaceBean;
    private UiDelegate ui;
    private final OperatorService operatorService = mock(OperatorService.class);

    @Before
    public void setup() throws Exception {
        bean = new OperatorManageUsersCtrl() {

            private static final long serialVersionUID = -9126265695343363133L;

            @Override
            protected IdentityService getIdService() {
                return idService;
            }

            @Override
            protected OperatorService getOperatorService() {
                return operatorService;
            }

            @Override
            protected void addMessage(final String clientId,
                    final FacesMessage.Severity severity, final String key) {
            }

            @Override
            protected AccountService getAccountingService() {
                return accountingService;
            }

            @Override
            protected ConfigurationService getConfigurationService() {
                return configurationService;
            }

            @Override
            protected MarketplaceService getMarketplaceService() {
                return marketplaceService;
            }
        };

        bean.model = new OperatorManageUsersModel();

        bean.getModel().setUserId("userId");

        ui = mock(UiDelegate.class);
        bean.ui = ui;

        marketplaceBean = mock(MarketplaceBean.class);
        when(bean.ui.findBean("marketplaceBean")).thenReturn(marketplaceBean);

        sl = mock(ServiceLocator.class);
        bean.setServiceLocator(sl);
        user = new VOUser();
        user.setOrganizationId("organizationId");
        user.setUserId("userId");

        when(idService.getUser(any(VOUser.class))).thenReturn(user);
        when(bean.ui.findBean(eq(OperatorManageUsersCtrl.APPLICATION_BEAN)))
                .thenReturn(appBean);
        when(sl.findService(UserManagementService.class)).thenReturn(
                userManageService);
        when(bean.getApplicationBean()).thenReturn(appBean);
        when(Boolean.valueOf(appBean.isInternalAuthMode())).thenReturn(
                Boolean.TRUE);
        when(
                Boolean.valueOf(userManageService
                        .isOrganizationLDAPManaged(user.getOrganizationId())))
                .thenReturn(Boolean.FALSE);
        when(Long.valueOf(accountingService.countRegisteredUsers()))
                .thenReturn(Long.valueOf(10));

        doReturn(getConfigurationSetting()).when(configurationService)
                .getVOConfigurationSetting(any(ConfigurationKey.class),
                        anyString());
    }

    @Test
    public void getInitialize() throws Exception {
        // given
        currentUser(OrganizationRoleType.PLATFORM_OPERATOR);
        doReturn(mockVOMarketplaces(123, 1234)).when(marketplaceService).getMarketplacesForOperator();
        bean.model.setInitialized(false);

        // when
        bean.getInitialize();

        // then
        verify(marketplaceService, times(1)).getMarketplacesForOperator();
        assertThat(bean.model.getMarketplaces(), hasItems(2));
    }

    @Test
    public void getInitialize_initialized() throws Exception {
        // given
        currentUser(OrganizationRoleType.PLATFORM_OPERATOR);

        // when
        bean.getInitialize();

        // then
        verify(marketplaceService, times(1)).getMarketplacesForOperator();
    }

    @Test
    public void reinitUser() throws Exception {
        // when
        OperatorManageUsersModel model = mock(OperatorManageUsersModel.class);
        when(model.getUserId()).thenReturn("userID");
        bean.setModel(model);
        bean.reinitUser();
        // then
        verify(model, times(2)).setUser(any(VOUser.class));
    }

    @Test
    public void reinitUser_NotInternalAuthMode() throws Exception {
        // given
        when(Boolean.valueOf(appBean.isInternalAuthMode())).thenReturn(
                Boolean.FALSE);
        // when
        bean.reinitUser();
        // then
        assertEquals(Boolean.valueOf(false),
                Boolean.valueOf(bean.isCheckResetPasswordSupported()));
    }

    @Test
    public void reinitUser_UserIdNull() throws Exception {
        // given
        OperatorManageUsersModel model = mock(OperatorManageUsersModel.class);
        doReturn(null).when(model).getUserId();
        bean.setModel(model);
        // when
        bean.reinitUser();
        // then
        verify(sl, never()).findService(UserManagementService.class);
        verify(bean.appBean, never()).isInternalAuthMode();
        verify(model, times(1)).setUser(any(VOUser.class));
    }

    @Test
    public void isResetPasswordSupported() throws Exception {
        // when
        bean.setInternalAuthMode(true);
        bean.isCheckResetPasswordSupported();
        // then
        assertEquals(Boolean.valueOf(true),
                Boolean.valueOf(bean.isCheckResetPasswordSupported()));
    }

    @Test
    public void userListSize() {
        //given
        bean = spy(bean);
        final List<VOUserDetails> usersList = new ArrayList<>();
        usersList.add(0, new VOUserDetails());
        usersList.add(1, new VOUserDetails());
        doReturn(usersList).when(bean).getUsersList();
        //then
        assertTrue(bean.getUsersListSize() == 2);
    }

    @Test
    public void getSelectedMarketplace_null() {
        //given
        OperatorManageUsersModel model = mock(OperatorManageUsersModel.class);
        doReturn(null).when(model).getUserId();
        bean.setModel(model);
        doReturn("0").when(model).getSelectedMarketplace();
        //when
        final String selectedMarketplace = bean.getSelectedMarketplace();
        //then
        assertTrue(selectedMarketplace == null);
    }

    @Test
    public void getSelectedMarketplace() {
        //given
        OperatorManageUsersModel model = mock(OperatorManageUsersModel.class);
        doReturn(null).when(model).getUserId();
        bean.setModel(model);
        doReturn("someID").when(model).getSelectedMarketplace();
        //when
        final String selectedMarketplace = bean.getSelectedMarketplace();
        //then
        assertTrue(selectedMarketplace == "someID");
    }

    @Test
    public void isResetPasswordSupported_SAML() throws Exception {
        //given
        when(appBean.isInternalAuthMode()).thenReturn(false);
        // when
        bean.isCheckResetPasswordSupported();
        // then
        assertEquals(Boolean.valueOf(false),
                Boolean.valueOf(bean.isCheckResetPasswordSupported()));
    }

    @Test
    public void isResetPasswordSupported_userIdChanged() throws Exception {
        // given
        bean.setInternalAuthMode(true);
        bean.getModel().setUserId("userId1");
        // when
        bean.isCheckResetPasswordSupported();
        // then
        assertEquals(Boolean.valueOf(true),
                Boolean.valueOf(bean.isCheckResetPasswordSupported()));
    }

    /**
     * Test the retrieving of available marketplaces. This operation is allowed
     * only to the PLATFORM_OPERATOR. All other organization roles will recieve
     * empty list.
     */
    @Test
    public void getSelectableMarketplaces() {
        // given
        currentUser(OrganizationRoleType.PLATFORM_OPERATOR);
        doReturn(mockVOMarketplaces(123, 1234)).when(marketplaceService).getMarketplacesForOperator();

        // when
        List<Marketplace> result = bean.getSelectableMarketplaces();

        // than
        verify(marketplaceService, times(1)).getMarketplacesForOperator();
        assertThat(result, hasItems(2));
    }

    /**
     * Test the retrieving of available marketplaces. This operation is allowed
     * only to the PLATFORM_OPERATOR. All other organization roles will recieve
     * empty list.
     */
    @Test
    public void getSelectableMarketplaces_Not_Platform_Operator() {
        // given not authorized for this operation
        currentUser(OrganizationRoleType.BROKER);
        availableMarketplaces(123, 1234);

        // when
        List<Marketplace> result = bean.getSelectableMarketplaces();

        // than
        verify(marketplaceBean, times(0)).getMarketplacesForOperator();
        assertThat(result, hasNoItems());
    }

    @Test
    public void getSelectableMarketplaces_No_Marketplaces() {
        // given no existing marketplaces
        currentUser(OrganizationRoleType.PLATFORM_OPERATOR);
        doReturn(mockVOMarketplaces()).when(marketplaceService).getMarketplacesForOperator();

        // when
        List<Marketplace> result = bean.getSelectableMarketplaces();

        // than
        verify(marketplaceService, times(1)).getMarketplacesForOperator();
        assertThat(result, hasNoItems());
    }

    @Test
    public void resetPasswordForUser_userIdNull()
            throws ObjectNotFoundException, OperationNotPermittedException,
            MailOperationException, OrganizationAuthoritiesException {
        // given
        bean.getModel().setUser(null);

        // when
        String restult = bean.resetPasswordForUser();

        // then
        verify(operatorService, times(0)).resetPasswordForUser(
                bean.getModel().getUserId());
        assertEquals(BaseBean.OUTCOME_ERROR, restult);
    }

    @Test
    public void resetPasswordForUser_ldapUser() throws ObjectNotFoundException,
            OperationNotPermittedException, MailOperationException,
            OrganizationAuthoritiesException {
        // given
        prepareLdapUser();

        // when
        String restult = bean.resetPasswordForUser();

        // then
        verify(operatorService, times(0)).resetPasswordForUser(
                bean.getModel().getUserId());
        assertEquals(BaseBean.OUTCOME_ERROR, restult);
    }

    @Test
    public void resetPasswordForUser() throws ObjectNotFoundException,
            OperationNotPermittedException, MailOperationException,
            OrganizationAuthoritiesException {
        // given
        VOUser voUser = new VOUser();
        voUser.setUserId("userId");
        bean.getModel().setUser(voUser);

        // when
        String restult = bean.resetPasswordForUser();

        // then
        verify(operatorService, times(1)).resetPasswordForUser("userId");
        assertEquals(BaseBean.OUTCOME_SUCCESS, restult);
    }

    @Test
    public void getMaxRegisteredUsersCount_configurationSetting() {
        // when
        long isExceed = bean.getMaxRegisteredUsersCount();

        // then
        assertEquals(5, isExceed);
    }

    @Test
    public void isExceedMaxNumberOfUsers_true() {
        // given
        bean.model.setNumberOfRegisteredUsers(15);
        bean.model.setMaxNumberOfRegisteredUsers(10);

        // when
        boolean isExceed = bean.isExceedMaxNumberOfUsers();

        // then
        assertEquals(Boolean.TRUE, Boolean.valueOf(isExceed));
    }

    @Test
    public void isExceedMaxNumberOfUsers_false() {
        // given
        bean.model.setNumberOfRegisteredUsers(5);
        bean.model.setMaxNumberOfRegisteredUsers(10);

        // when
        boolean isExceed = bean.isExceedMaxNumberOfUsers();

        // then
        assertEquals(Boolean.FALSE, Boolean.valueOf(isExceed));
    }

    @Test
    public void importUsers_tokenInvalid() {
        // given
        bean.model.resetToken();
        bean.model.setToken(bean.model.getToken());
        bean.model.resetToken();
        //when
        String result = bean.importUsers();
        // then
        assertEquals(BaseBean.OUTCOME_SUCCESS, result);
        verify(bean.ui, times(0)).handle(anyString(), anyString());
    }

    @Test
    public void updateSelectedUserTest() throws Exception {
        //given
        String id = "someUser";
        bean.setSelectedUserKey(id);
        VOUser mockUser = mock(VOUser.class);
        bean.model.setUser(mockUser);
        when(mockUser.getUserId()).thenReturn(id);
        //when
        bean.updateSelectedUser();
        //then
        assertTrue(bean.model.getUser().getUserId().equals(id));
    }

    @Test
    public void getUsersList() throws OrganizationAuthoritiesException {
        // given
        VOUserDetails user1 = new VOUserDetails();
        user1.setUserId("user1ID");
        user1.setEMail("user1Email");
        user1.setOrganizationName("user1OrgName");
        user1.setOrganizationId("user1OrgID");

        VOUserDetails user2 = new VOUserDetails();
        user2.setUserId("user2ID");
        user2.setEMail("user2Email");
        user2.setOrganizationName("user2OrgName");
        user2.setOrganizationId("user2OrgID");

        List<VOUserDetails> usersListVO = new ArrayList<>();
        usersListVO.add(user1);
        usersListVO.add(user2);

        when(operatorService.getUsers()).thenReturn(usersListVO);
        // when
        final List<POUserAndOrganization> resultList = bean.getUsersList();
        // then
        boolean hasFirst = false;
        boolean hasSecond = false;
        for (POUserAndOrganization obj : resultList) {
            if (obj.getUserId().equals("user1ID")
                    && obj.getEmail().equals("user1Email")
                    && obj.getOrganizationId().equals("user1OrgID")
                    && obj.getOrganizationName().equals("user1OrgName")) {
                hasFirst = true;
            }
            if (obj.getUserId().equals("user2ID")
                    && obj.getEmail().equals("user2Email")
                    && obj.getOrganizationId().equals("user2OrgID")
                    && obj.getOrganizationName().equals("user2OrgName")) {
                hasSecond = true;
            }
        }
        assertTrue(hasFirst && hasSecond);
    }

    @Test
    public void getDataTableHeaders() {
        //given
        List<String> expectedHeaders = new ArrayList<>();
        expectedHeaders.add("userId");
        expectedHeaders.add("email");
        expectedHeaders.add("organizationName");
        expectedHeaders.add("organizationId");
        //when
        final List<String> dataTableHeaders = bean.getDataTableHeaders();
        //then
        for (String header : expectedHeaders) {
            assertTrue(dataTableHeaders.contains(header));
        }
    }

    @Test
    public void lockUser() throws Exception {
        //given
        VOUser voUser = mock(VOUser.class);
        OperatorManageUsersModel mockModel = mock(OperatorManageUsersModel.class);
        bean.setModel(mockModel);
        doReturn(voUser).when(mockModel).getUser();
        //when
        bean.lockUser();
        //then
        verify(operatorService, times(1)).setUserAccountStatus(any(VOUser.class), any(UserAccountStatus.class));
    }

    @Test
    public void unlockUser() throws Exception {
        //given
        VOUser voUser = mock(VOUser.class);
        OperatorManageUsersModel mockModel = mock(OperatorManageUsersModel.class);
        bean.setModel(mockModel);
        doReturn(voUser).when(mockModel).getUser();
        //when
        bean.unlockUser();
        //then
        verify(operatorService, times(1)).setUserAccountStatus(any(VOUser.class), any(UserAccountStatus.class));
    }

    private void prepareLdapUser() throws ObjectNotFoundException {

        VOUser voUser = new VOUser();
        voUser.setOrganizationId("organizationId");
        bean.getModel().setUser(voUser);

        when(sl.findService(UserManagementService.class)).thenReturn(
                userManageService);
        when(
                Boolean.valueOf(userManageService
                        .isOrganizationLDAPManaged(user.getOrganizationId())))
                .thenReturn(Boolean.TRUE);
    }

    private void currentUser(OrganizationRoleType platformOperator) {
        VOUserDetails userDetails = new VOUserDetails();
        userDetails.setUserRoles(OrganizationRoleType
                .correspondingUserRoles(Arrays.asList(platformOperator)));

        when(ui.getUserFromSessionWithoutException()).thenReturn(userDetails);
    }

    private void availableMarketplaces(long... mIds) {
        ArrayList<Marketplace> marketplaces = new ArrayList<Marketplace>();
        for (int i = 0; i < mIds.length; i++) {
            Marketplace mp = new Marketplace();
            long mKey = mIds[i];
            mp.setKey(mKey);
            mp.setVersion(i + 1);
            mp.setMarketplaceId(String.valueOf(mKey) + "_id");
            mp.setName(String.valueOf(mKey) + "_name");
            mp.setOwningOrganizationId(String.valueOf(mKey) + "_org");

            marketplaces.add(mp);
        }

        if (marketplaces.isEmpty()) {
            when(marketplaceBean.getMarketplacesForOperator()).thenReturn(null);
        } else {
            when(marketplaceBean.getMarketplacesForOperator()).thenReturn(
                    marketplaces);
        }
    }

    private List<VOMarketplace> mockVOMarketplaces(long... mIds) {
        ArrayList<VOMarketplace> marketplaces = new ArrayList<>();
        for (int i = 0; i < mIds.length; i++) {
            VOMarketplace mp = new VOMarketplace();
            long mKey = mIds[i];
            mp.setKey(mKey);
            mp.setVersion(i + 1);
            mp.setMarketplaceId(String.valueOf(mKey) + "_id");
            mp.setName(String.valueOf(mKey) + "_name");
            mp.setOwningOrganizationId(String.valueOf(mKey) + "_org");
            marketplaces.add(mp);
        }
        if (marketplaces.isEmpty()) {
            return Collections.emptyList();
        } else {
            return marketplaces;
        }
    }

    private VOConfigurationSetting getConfigurationSetting() {
        VOConfigurationSetting configurationSetting = new VOConfigurationSetting();
        configurationSetting
                .setInformationId(ConfigurationKey.MAX_NUMBER_ALLOWED_USERS);
        configurationSetting.setContextId(Configuration.GLOBAL_CONTEXT);
        configurationSetting.setValue("5");
        configurationSetting.setKey(1000L);
        return configurationSetting;
    }
}
