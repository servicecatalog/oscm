/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2013-2-5                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.passwordrecovery;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;

import javax.ejb.EJBTransactionRolledbackException;
import javax.ejb.SessionContext;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;

import org.oscm.communicationservice.local.CommunicationServiceLocal;
import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.ConfigurationSetting;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.RoleAssignment;
import org.oscm.identityservice.local.IdentityServiceLocal;
import org.oscm.test.data.UserRoles;
import org.oscm.types.constants.Configuration;
import org.oscm.types.enumtypes.EmailType;
import org.oscm.internal.types.enumtypes.ConfigurationKey;
import org.oscm.internal.types.enumtypes.UserAccountStatus;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.IllegalArgumentException;
import org.oscm.internal.types.exception.MailOperationException;
import org.oscm.internal.types.exception.ObjectNotFoundException;

/**
 * Unit test for PasswordRecoveryServiceBean.
 * 
 * @author Yu
 * 
 */
@SuppressWarnings("boxing")
public class PasswordRecoveryServiceNoDBTest {

    private PasswordRecoveryServiceBean passwordRecoverybean;
    private final String userId = "userA";
    private final String marketplaceId = "mp";
    private PlatformUser pUser;
    private final String password_6letter = "123456";
    private final String password_4letter = "1234";

    private DataService dataServiceMock;
    private IdentityServiceLocal ids;

    @Captor
    ArgumentCaptor<String> urlCaptor;

    @Captor
    ArgumentCaptor<Object[]> stringCaptor;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        passwordRecoverybean = spy(new PasswordRecoveryServiceBean());
        pUser = new PlatformUser();
        pUser.setKey(1);
        pUser.setUserId(userId);

        Organization org = new Organization();
        org.setRemoteLdapActive(false);
        pUser.setOrganization(org);
        pUser.setStatus(UserAccountStatus.ACTIVE);

        dataServiceMock = mock(DataService.class);
        passwordRecoverybean.dm = dataServiceMock;

        passwordRecoverybean.configs = mock(ConfigurationServiceLocal.class);
        ConfigurationSetting setting = spy(new ConfigurationSetting());
        when(setting.getValue()).thenReturn("BaseURL");
        doReturn(
                new ConfigurationSetting(ConfigurationKey.BASE_URL,
                        Configuration.GLOBAL_CONTEXT, "BASE_URL")).when(
                passwordRecoverybean.configs)
                .getConfigurationSetting(eq(ConfigurationKey.BASE_URL),
                        eq(Configuration.GLOBAL_CONTEXT));
        ids = mock(IdentityServiceLocal.class);
        passwordRecoverybean.ids = ids;
        doReturn(pUser).when(passwordRecoverybean.ids).getPlatformUser(
                anyString(), eq(false));

        passwordRecoverybean.cs = mock(CommunicationServiceLocal.class);
        doNothing().when(passwordRecoverybean.cs).sendMail(
                any(PlatformUser.class), any(EmailType.class),
                any(Object[].class), any(Marketplace.class));

        passwordRecoverybean.sessionCtx = mock(SessionContext.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void startPasswordRecovery_NullInput() throws Exception {
        // when
        passwordRecoverybean.startPasswordRecovery(null, marketplaceId);

    }

    @Test
    public void startPasswordRecovery_NoUser() throws Exception {
        // given
        doThrow(new ObjectNotFoundException()).when(passwordRecoverybean.ids)
                .getPlatformUser(anyString(), eq(false));
        // when
        passwordRecoverybean.startPasswordRecovery(userId, marketplaceId);
        // then
        verify(passwordRecoverybean, never()).verifyUserPermission(
                any(PlatformUser.class), anyString());
    }

    @Test
    public void startPasswordRecovery_EJBTransactionFailed() throws Exception {
        // given
        doThrow(new EJBTransactionRolledbackException()).when(
                passwordRecoverybean.dm).flush();

        // when
        passwordRecoverybean.startPasswordRecovery(userId, marketplaceId);
        // then
        verify(passwordRecoverybean, never()).sendPasswordRecoveryMails(
                any(PlatformUser.class), any(EmailType.class), anyString(),
                any(Object[].class));
    }

    @Test
    public void startPasswordRecovery_NoPermission() throws Exception {
        // given
        doReturn(Boolean.FALSE).when(passwordRecoverybean)
                .verifyUserPermission(eq(pUser), eq(marketplaceId));
        // when
        passwordRecoverybean.startPasswordRecovery(userId, marketplaceId);
        // then
        verify(passwordRecoverybean, never()).sendPasswordRecoveryMails(
                any(PlatformUser.class), any(EmailType.class), anyString(),
                any(Object[].class));
    }

    @Test
    public void startPasswordRecovery_InvalidInterval() throws Exception {
        // given
        long currentTime = System.currentTimeMillis();
        pUser.setPasswordRecoveryStartDate(currentTime);
        // when
        passwordRecoverybean.startPasswordRecovery(userId, marketplaceId);
        // then
        verify(passwordRecoverybean, never()).sendPasswordRecoveryMails(
                any(PlatformUser.class), any(EmailType.class), anyString(),
                any(Object[].class));
    }

    @Test
    public void startPasswordRecovery_Ok() throws Exception {
        // when

        passwordRecoverybean.startPasswordRecovery(userId, marketplaceId);
        // then
        verify(passwordRecoverybean, times(1)).sendPasswordRecoveryMails(
                any(PlatformUser.class),
                eq(EmailType.RECOVERPASSWORD_CONFIRM_URL), urlCaptor.capture(),
                stringCaptor.capture());
        Object[] result = stringCaptor.getValue();
        assertEquals(true, result[0].toString().contains("token="));
        assertEquals(marketplaceId, urlCaptor.getValue());
    }

    @Test
    public void userPermissionCheck_LogginUser() throws Exception {
        // given
        doReturn(Boolean.TRUE).when(ids).isUserLoggedIn(pUser.getKey());
        // when
        passwordRecoverybean.verifyUserPermission(pUser, marketplaceId);
        // then
        verify(passwordRecoverybean, never()).sendPasswordRecoveryMails(
                any(PlatformUser.class), any(EmailType.class), anyString(),
                any(Object[].class));
        verify(passwordRecoverybean, never()).isAccountLocked(
                any(UserAccountStatus.class));
    }

    @Test
    public void userPermissionCheck_UserLock() throws Exception {

        // given
        pUser.setStatus(UserAccountStatus.LOCKED);
        // when
        passwordRecoverybean.verifyUserPermission(pUser, marketplaceId);
        // then
        verify(passwordRecoverybean, times(1)).sendPasswordRecoveryMails(
                eq(pUser), eq(EmailType.RECOVERPASSWORD_USER_LOCKED),
                urlCaptor.capture(), any(Object[].class));
        assertEquals(marketplaceId, urlCaptor.getValue());
    }

    @Test
    public void userPermissionCheck_LOCKED_FAILED_LOGIN_ATTEMPTS_B10139()
            throws Exception {
        // given
        pUser.setStatus(UserAccountStatus.LOCKED_FAILED_LOGIN_ATTEMPTS);
        // when
        passwordRecoverybean.verifyUserPermission(pUser, marketplaceId);
        // then
        verify(passwordRecoverybean, times(1)).sendPasswordRecoveryMails(
                eq(pUser), eq(EmailType.RECOVERPASSWORD_USER_LOCKED),
                urlCaptor.capture(), any(Object[].class));
        assertEquals(marketplaceId, urlCaptor.getValue());
    }

    @Test
    public void userPermissionCheck_LOCKED_NOT_CONFIRMED_B10139()
            throws Exception {
        // given
        pUser.setStatus(UserAccountStatus.LOCKED_NOT_CONFIRMED);
        // when
        passwordRecoverybean.verifyUserPermission(pUser, marketplaceId);
        // then
        verify(passwordRecoverybean, times(1)).sendPasswordRecoveryMails(
                eq(pUser), eq(EmailType.RECOVERPASSWORD_USER_LOCKED),
                urlCaptor.capture(), any(Object[].class));
        assertEquals(marketplaceId, urlCaptor.getValue());
    }

    @Test
    public void userPermissionCheck_UserRemote() throws Exception {
        // given
        pUser.getOrganization().setRemoteLdapActive(true);
        // when
        passwordRecoverybean.verifyUserPermission(pUser, marketplaceId);
        // then
        verify(passwordRecoverybean, times(1)).sendPasswordRecoveryMails(
                eq(pUser), eq(EmailType.RECOVERPASSWORD_FAILED_LDAP),
                urlCaptor.capture(), any(Object[].class));
        assertEquals(marketplaceId, urlCaptor.getValue());
    }

    @Test
    public void userPermissionCheck_NotPureUserWithMpId() throws Exception {
        // given
        pUser.setAssignedRoles(UserRoles.createRoleAssignments(pUser,
                UserRoleType.MARKETPLACE_OWNER));
        // when
        passwordRecoverybean.verifyUserPermission(pUser, marketplaceId);
        // then
        verify(passwordRecoverybean, times(1)).sendPasswordRecoveryMails(
                eq(pUser), eq(EmailType.RECOVERPASSWORD_MARKETPLACE_FAILED),
                urlCaptor.capture(), any(Object[].class));
        assertEquals(marketplaceId, urlCaptor.getValue());
    }

    @Test
    public void userPermissionCheck_PureUserWithoutMpId() throws Exception {
        // given
        pUser.setAssignedRoles(new HashSet<RoleAssignment>());
        // when
        passwordRecoverybean.verifyUserPermission(pUser, null);
        // then
        verify(passwordRecoverybean, times(1)).sendPasswordRecoveryMails(
                eq(pUser), eq(EmailType.RECOVERPASSWORD_CLASSICPORTAL_FAILED),
                urlCaptor.capture(), any(Object[].class));
        assertEquals(null, urlCaptor.getValue());
    }

    @Test
    public void confirmPasswordRecoveryLink_Ok() throws Exception {
        // given
        passwordRecoverybean.startPasswordRecovery(userId, marketplaceId);
        verify(passwordRecoverybean, times(1)).sendPasswordRecoveryMails(
                any(PlatformUser.class),
                eq(EmailType.RECOVERPASSWORD_CONFIRM_URL), urlCaptor.capture(),
                stringCaptor.capture());
        Object[] resultUrl = stringCaptor.getValue();
        String token = resultUrl[0].toString();
        token = token.substring(token.indexOf("token=") + 6);
        // when
        String result = passwordRecoverybean.confirmPasswordRecoveryLink(token,
                marketplaceId);
        // then
        assertEquals(userId, result);
    }

    @Test
    public void confirmPasswordRecoveryLink_InvalidLink() throws Exception {
        String result = passwordRecoverybean.confirmPasswordRecoveryLink(null,
                marketplaceId);
        assertEquals(null, result);

        result = passwordRecoverybean.confirmPasswordRecoveryLink("",
                marketplaceId);
        assertEquals(null, result);
    }

    @Test
    public void confirmPasswordRecoveryLink_NoUser() throws Exception {
        // given
        long currentTime = System.currentTimeMillis();
        PasswordRecoveryLink passwordRecoveryLink = new PasswordRecoveryLink(
                true, passwordRecoverybean.configs);
        String confirmationURL = passwordRecoveryLink
                .encodePasswordRecoveryLink(pUser, currentTime, marketplaceId);
        doThrow(new ObjectNotFoundException()).when(passwordRecoverybean.ids)
                .getPlatformUser(eq(userId), eq(false));
        confirmationURL = confirmationURL.substring(confirmationURL
                .indexOf("token=") + 6);
        // when
        String result = passwordRecoverybean.confirmPasswordRecoveryLink(
                confirmationURL, marketplaceId);
        // then
        assertEquals(null, result);
    }

    @Test
    public void confirmPasswordRecoveryLink_IncompleteConfirmationURL()
            throws Exception {
        // given
        long currentTime = System.currentTimeMillis();
        PasswordRecoveryLink passwordRecoveryLink = new PasswordRecoveryLink(
                true, passwordRecoverybean.configs);
        String confirmationURL = passwordRecoveryLink
                .encodePasswordRecoveryLink(pUser, currentTime, marketplaceId);
        System.out.println(confirmationURL);
        String confirmationURL1 = confirmationURL.substring(
                confirmationURL.indexOf("token=") + 6,
                confirmationURL.indexOf("token=") + 14);
        // when
        System.out.println(confirmationURL1);
        String result = passwordRecoverybean.confirmPasswordRecoveryLink(
                confirmationURL1, marketplaceId);
        // then
        assertEquals(null, result);

        String confirmationURL2 = confirmationURL.substring(
                confirmationURL.indexOf("token=") + 6,
                confirmationURL.indexOf("token=") + 7);
        System.out.println(confirmationURL2);
        // when
        result = passwordRecoverybean.confirmPasswordRecoveryLink(
                confirmationURL2, marketplaceId);
        // then
        assertEquals(null, result);
    }

    @Test
    public void completePasswordRecovery_Ok() throws Exception {
        // when
        boolean result = passwordRecoverybean.completePasswordRecovery(userId,
                password_6letter);
        // then
        assertEquals(true, result);
        assertEquals(0, pUser.getPasswordRecoveryStartDate());
        assertEquals(UserAccountStatus.ACTIVE, pUser.getStatus());
        verify(passwordRecoverybean, times(1)).sendPasswordRecoveryMails(
                eq(pUser), eq(EmailType.RECOVERPASSWORD_CONFIRM),
                urlCaptor.capture(), any(Object[].class));
        assertEquals(null, urlCaptor.getValue());
    }

    @Test
    public void completePasswordRecovery_NullInput() throws Exception {
        // when
        boolean result = passwordRecoverybean.completePasswordRecovery(null,
                password_6letter);
        // then
        assertEquals(false, result);
        verify(passwordRecoverybean, never()).sendPasswordRecoveryMails(
                eq(pUser), eq(EmailType.RECOVERPASSWORD_CONFIRM),
                urlCaptor.capture(), any(Object[].class));
    }

    @Test
    public void completePasswordRecovery_InvalidPasswordInput()
            throws Exception {
        // when
        boolean result = passwordRecoverybean.completePasswordRecovery(userId,
                password_4letter);
        // then
        assertEquals(false, result);
        verify(passwordRecoverybean, never()).sendPasswordRecoveryMails(
                eq(pUser), eq(EmailType.RECOVERPASSWORD_CONFIRM),
                urlCaptor.capture(), any(Object[].class));
    }

    @Test
    public void completePasswordRecovery_NoUser() throws Exception {
        // given
        doThrow(new ObjectNotFoundException()).when(passwordRecoverybean.ids)
                .getPlatformUser(anyString(), eq(false));
        // when
        boolean result = passwordRecoverybean.completePasswordRecovery(userId,
                password_6letter);
        // then
        assertEquals(false, result);
        verify(passwordRecoverybean, never()).sendPasswordRecoveryMails(
                eq(pUser), eq(EmailType.RECOVERPASSWORD_CONFIRM),
                urlCaptor.capture(), any(Object[].class));
    }

    @Test
    public void completePasswordRecovery_SendMailFailed() throws Exception {
        // given
        doThrow(new MailOperationException()).when(passwordRecoverybean)
                .sendPasswordRecoveryMails(any(PlatformUser.class),
                        any(EmailType.class), anyString(), any(Object[].class));
        // when
        boolean result = passwordRecoverybean.completePasswordRecovery(userId,
                password_6letter);
        // then

        verify(passwordRecoverybean.sessionCtx, times(1)).setRollbackOnly();
        assertEquals(false, result);
    }

    @Test
    public void completePasswordRecovery_EJBTransactionFailed()
            throws Exception {
        // given
        doThrow(new EJBTransactionRolledbackException()).when(
                passwordRecoverybean.dm).flush();
        // when
        boolean result = passwordRecoverybean.completePasswordRecovery(userId,
                password_6letter);
        // then
        verify(passwordRecoverybean, never()).sendPasswordRecoveryMails(
                any(PlatformUser.class), any(EmailType.class), anyString(),
                any(Object[].class));
        assertEquals(false, result);
    }

    @Test
    public void startPasswordRecovery_SendMailFailed() throws Exception {
        // given
        doThrow(new MailOperationException()).when(passwordRecoverybean)
                .sendPasswordRecoveryMails(any(PlatformUser.class),
                        any(EmailType.class), anyString(), any(Object[].class));
        // when
        passwordRecoverybean.startPasswordRecovery(userId, marketplaceId);
        // then
        verify(passwordRecoverybean.sessionCtx, times(1)).setRollbackOnly();
    }

    @Test
    public void startPasswordRecovery_Bug10115() throws Exception {
        // given
        pUser.setStatus(UserAccountStatus.PASSWORD_MUST_BE_CHANGED);
        // when
        passwordRecoverybean.startPasswordRecovery(userId, marketplaceId);
        // then
        assertEquals(UserAccountStatus.PASSWORD_MUST_BE_CHANGED,
                pUser.getStatus());
    }

    @Test
    public void completePasswordRecovery_Bug10115() throws Exception {
        // given
        pUser.setStatus(UserAccountStatus.PASSWORD_MUST_BE_CHANGED);
        // when
        boolean result = passwordRecoverybean.completePasswordRecovery(userId,
                password_6letter);
        // then
        assertEquals(true, result);
        assertEquals(UserAccountStatus.ACTIVE, pUser.getStatus());
    }

}
