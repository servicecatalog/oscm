/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.ui.beans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.faces.application.FacesMessage;
import javax.security.auth.login.LoginException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.junit.Before;
import org.junit.Test;

import org.oscm.converter.ParameterEncoder;
import org.oscm.ui.common.ServiceAccess;
import org.oscm.ui.stubs.FacesContextStub;
import org.oscm.internal.intf.ConfigurationService;
import org.oscm.internal.intf.IdentityService;
import org.oscm.internal.types.enumtypes.UserAccountStatus;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.BulkUserImportException;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.MailOperationException;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.OrganizationRemovedException;
import org.oscm.internal.types.exception.SecurityCheckException;
import org.oscm.internal.types.exception.TechnicalServiceNotAliveException;
import org.oscm.internal.types.exception.TechnicalServiceOperationException;
import org.oscm.internal.types.exception.UserActiveException;
import org.oscm.internal.types.exception.UserDeletionConstraintException;
import org.oscm.internal.types.exception.UserModificationConstraintException;
import org.oscm.internal.types.exception.UserRoleAssignmentException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOUser;
import org.oscm.internal.vo.VOUserDetails;

/**
 * Test cases for ConfirmationBean
 */
public class ConfirmationBeanTest {

    private ConfirmationBean confirmationBean;
    private IdentityService idService;
    private SessionBean sessionBean;
    private HttpServletRequest request;
    private HttpSession session;
    private ConfigurationService cfgService;
    private ServiceAccess serviceAccess;

    private VOUser voUser;

    private String userId = "userA";
    private String orgId = "orgA";
    private Long serviceKey = Long.valueOf(12345);

    boolean addMessageOccured;

    @Before
    public void before() throws Exception {
        voUser = new VOUser();
        addMessageOccured = false;
        new FacesContextStub(Locale.ENGLISH) {
            @Override
            public void addMessage(String arg0, FacesMessage arg1) {
                addMessageOccured = true;
            }
        };
        confirmationBean = spy(new ConfirmationBean());
        idService = mock(IdentityService.class);

        doReturn(voUser).when(idService).getUser(any(VOUser.class));
        confirmationBean.idService = idService;
        sessionBean = new SessionBean();
        confirmationBean.setSessionBean(sessionBean);

        request = mock(HttpServletRequest.class);
        session = mock(HttpSession.class);
        doReturn(request).when(confirmationBean).getRequest();
        doReturn(session).when(request).getSession(anyBoolean());

        cfgService = mock(ConfigurationService.class);
        doReturn(cfgService).when(confirmationBean).getConfigurationService();

        serviceAccess = mock(ServiceAccess.class);
        doReturn(serviceAccess).when(session).getAttribute(
                eq(ServiceAccess.SESS_ATTR_SERVICE_ACCESS));
    }

    /**
     * Test the successful execution.
     */
    @Test
    public void initialize_success() {
        voUser.setStatus(UserAccountStatus.LOCKED_NOT_CONFIRMED);
        String encodedString = ParameterEncoder.encodeParameters(new String[] {
                userId, orgId, "MPID", String.valueOf(serviceKey) });
        confirmationBean.setEncodedParam(encodedString);

        confirmationBean.initialize();

        assertEquals(serviceKey, sessionBean.getSubscribeToServiceKey());
    }

    /**
     * Test the execution when the user account is already confirmed,.
     */
    @Test
    public void initialize_userStatus_active() {
        voUser.setStatus(UserAccountStatus.ACTIVE);

        String encodedString = ParameterEncoder.encodeParameters(new String[] {
                userId, orgId, "MPID", String.valueOf(serviceKey) });
        confirmationBean.setEncodedParam(encodedString);

        confirmationBean.initialize();
        assertTrue(addMessageOccured);
        assertTrue(confirmationBean.getShowError());
        assertTrue(confirmationBean.getShowButton());
    }

    /**
     * Test the execution with invalid encoded string.
     */
    @Test
    public void initialize_invalidEncParm_null() {
        confirmationBean.setEncodedParam(null);
        confirmationBean.initialize();
        assertTrue(addMessageOccured);
        assertTrue(confirmationBean.getShowError());
        assertFalse(confirmationBean.getShowButton());
    }

    /**
     * Test the execution with invalid encoded string.
     */
    @Test
    public void initialize_invalidEncParm_empty() {
        confirmationBean.setEncodedParam("");
        confirmationBean.initialize();
        assertTrue(addMessageOccured);
        assertTrue(confirmationBean.getShowError());
        assertFalse(confirmationBean.getShowButton());
    }

    /**
     * Test the execution with invalid encoded string.
     */
    @Test
    public void initialize_invalidEncParm_invalidDec() {
        confirmationBean.setEncodedParam("somestring");
        confirmationBean.initialize();
        assertTrue(addMessageOccured);
        assertTrue(confirmationBean.getShowError());
        assertFalse(confirmationBean.getShowButton());
    }

    /**
     * Check the behavior if it's not possible to send the registration
     * Confirmation mail.
     */
    @Test
    public void initialize_mailSendFailed() {
        confirmationBean.idService = new IdentityServiceStubExt();
        voUser.setUserId("mail");
        voUser.setStatus(UserAccountStatus.LOCKED_NOT_CONFIRMED);

        String encodedString = ParameterEncoder.encodeParameters(new String[] {
                userId, orgId, "MPID", String.valueOf(serviceKey) });
        confirmationBean.setEncodedParam(encodedString);

        confirmationBean.initialize();

        assertTrue(addMessageOccured);
        assertTrue(confirmationBean.getShowError());
        assertFalse(confirmationBean.getShowButton());
    }

    /**
     * Check the behavior if the user is locked.
     */
    @Test
    public void initialize_userLocked() {
        confirmationBean.idService = new IdentityServiceStubExt();
        voUser.setUserId("obj");
        voUser.setStatus(UserAccountStatus.LOCKED_NOT_CONFIRMED);

        String encodedString = ParameterEncoder.encodeParameters(new String[] {
                userId, orgId, "MPID", String.valueOf(serviceKey) });
        confirmationBean.setEncodedParam(encodedString);

        confirmationBean.initialize();

        assertTrue(addMessageOccured);
        assertTrue(confirmationBean.getShowError());
        assertFalse(confirmationBean.getShowButton());
    }

    /**
     * Test the successful execution. With the dummy postfix.
     */
    @Test
    public void initialize_b7856_success() {
        voUser.setStatus(UserAccountStatus.LOCKED_NOT_CONFIRMED);
        String encodedString = ParameterEncoder.encodeParameters(new String[] {
                userId, orgId, "MPID", String.valueOf(serviceKey) });
        encodedString += "&et";
        confirmationBean.setEncodedParam(encodedString);

        confirmationBean.initialize();

        assertEquals(serviceKey, sessionBean.getSubscribeToServiceKey());
    }

    /**
     * Test with a invalid service key.
     */
    @Test
    public void initialize_b7856_failed() {
        voUser.setStatus(UserAccountStatus.LOCKED_NOT_CONFIRMED);
        String encodedString = ParameterEncoder.encodeParameters(new String[] {
                userId, orgId, "MPID", "INVALID" });

        encodedString += "&et";
        confirmationBean.setEncodedParam(encodedString);

        confirmationBean.initialize();

        assertTrue(addMessageOccured);
        assertTrue(confirmationBean.getShowError());
        assertFalse(confirmationBean.getShowButton());

    }

    /**
     * Bug 10347: User should already be logged in at this point.
     */
    @Test
    public void initialize_SSOAutoLogin() throws Exception {
        // given
        voUser.setStatus(UserAccountStatus.LOCKED_NOT_CONFIRMED);
        String encodedString = ParameterEncoder.encodeParameters(new String[] {
                userId, orgId, "MPID", String.valueOf(serviceKey) });
        confirmationBean.setEncodedParam(encodedString);
        doReturn(Boolean.TRUE).when(confirmationBean).isServiceProvider();
        // when
        confirmationBean.initialize();
        // then
        assertEquals(serviceKey, sessionBean.getSubscribeToServiceKey());
        verify(confirmationBean, times(1)).loginUser(eq(voUser), anyString(),
                eq(request), eq(session));
    }

    @Test
    public void initialize_SSOAutoLogin_Exception() throws Exception {
        // given
        voUser.setStatus(UserAccountStatus.LOCKED_NOT_CONFIRMED);
        String encodedString = ParameterEncoder.encodeParameters(new String[] {
                userId, orgId, "MPID", String.valueOf(serviceKey) });
        confirmationBean.setEncodedParam(encodedString);
        doReturn(Boolean.TRUE).when(confirmationBean).isServiceProvider();
        doThrow(new LoginException("")).when(confirmationBean).loginUser(
                eq(voUser), anyString(), eq(request), eq(session));

        // when
        confirmationBean.initialize();

        // then
        assertEquals(serviceKey, sessionBean.getSubscribeToServiceKey());
        verify(confirmationBean, times(1)).addMessage(anyString(),
                eq(FacesMessage.SEVERITY_ERROR),
                eq(BaseBean.ERROR_USER_CONFIRMED_LOGIN_FAIL));
    }

    @Test
    public void isServiceProvider_True() {
        // given
        when(Boolean.valueOf(cfgService.isServiceProvider())).thenReturn(
                Boolean.TRUE);
        // when
        boolean result = confirmationBean.isServiceProvider();
        // then
        assertEquals(Boolean.TRUE, Boolean.valueOf(result));
    }

    @Test
    public void isServiceProvider_False() {
        // given
        when(Boolean.valueOf(cfgService.isServiceProvider())).thenReturn(
                Boolean.FALSE);
        // when
        boolean result = confirmationBean.isServiceProvider();
        // then
        assertEquals(Boolean.FALSE, Boolean.valueOf(result));
    }

    @Test
    public void loginUser() throws Exception {
        // given
        String password = "pwd123";

        // when
        confirmationBean.loginUser(voUser, password, request, session);

        // then
        verify(serviceAccess, times(1)).login(eq(voUser), eq(password),
                eq(request), any(HttpServletResponse.class));
    }

    // extended mock to throw exceptions
    private class IdentityServiceStubExt implements IdentityService {

        @Override
        public void changePassword(String oldPassword, String newPassword)
                throws SecurityCheckException, ValidationException {

        }

        @Override
        public void confirmAccount(VOUser user, String marketplaceId)
                throws OperationNotPermittedException, ObjectNotFoundException,
                MailOperationException {

            if (user.getUserId() == "mail")
                throw new MailOperationException();
            if (user.getUserId() == "obj")
                throw new ObjectNotFoundException();
        }

        @Override
        public VOUserDetails createUser(VOUserDetails user,
                List<UserRoleType> roles, String marketplaceId)
                throws NonUniqueBusinessKeyException, MailOperationException,
                ValidationException, UserRoleAssignmentException {
            return null;
        }

        @Override
        public void deleteUser(VOUser user, String marketplaceId)
                throws UserDeletionConstraintException,
                ObjectNotFoundException, ConcurrentModificationException {
        }

        @Override
        public VOUserDetails getCurrentUserDetails() {
            return null;
        }

        @Override
        public VOUserDetails getCurrentUserDetailsIfPresent() {
            return null;
        }

        @Override
        public VOUser getUser(VOUser user) throws ObjectNotFoundException,
                OperationNotPermittedException, OrganizationRemovedException {
            return voUser;
        }

        @Override
        public VOUserDetails getUserDetails(VOUser user)
                throws ObjectNotFoundException {
            return null;
        }

        @Override
        public List<VOUserDetails> getUsersForOrganization() {
            return null;
        }

        @Override
        public void importLdapUsers(List<VOUserDetails> users,
                String marketplaceId) throws NonUniqueBusinessKeyException,
                ValidationException, MailOperationException {

        }

        @Override
        public void lockUserAccount(VOUser user, UserAccountStatus newStatus,
                String marketplaceId) throws OperationNotPermittedException,
                ObjectNotFoundException, ConcurrentModificationException {

        }

        @Override
        public void notifyOnLoginAttempt(VOUser user, boolean attemptSuccessful)
                throws ObjectNotFoundException, SecurityCheckException,
                ValidationException {

        }

        @Override
        public void requestResetOfUserPassword(VOUser user, String marketplaceId)
                throws MailOperationException, ObjectNotFoundException,
                OperationNotPermittedException, UserActiveException,
                ConcurrentModificationException {

        }

        @Override
        public List<VOUserDetails> searchLdapUsers(String userIdPattern)
                throws ValidationException {
            return null;
        }

        @Override
        public void sendAccounts(String email, String marketplaceId)
                throws ValidationException, MailOperationException {

        }

        @Override
        public void unlockUserAccount(VOUser user, String marketplaceId)
                throws ObjectNotFoundException, OperationNotPermittedException,
                ConcurrentModificationException {

        }

        @Override
        public VOUserDetails updateUser(VOUserDetails user)
                throws OperationNotPermittedException, ObjectNotFoundException,
                ValidationException, NonUniqueBusinessKeyException,
                TechnicalServiceNotAliveException,
                TechnicalServiceOperationException,
                ConcurrentModificationException {
            return null;
        }

        @Override
        public List<UserRoleType> getAvailableUserRoles(VOUser user)
                throws ObjectNotFoundException, OperationNotPermittedException {
            return Collections.emptyList();
        }

        @Override
        public void grantUserRoles(VOUser user, List<UserRoleType> roles)
                throws ObjectNotFoundException, OperationNotPermittedException,
                UserRoleAssignmentException {
        }

        @Override
        public void revokeUserRoles(VOUser user, List<UserRoleType> roles)
                throws ObjectNotFoundException,
                UserModificationConstraintException, UserActiveException,
                OperationNotPermittedException, UserRoleAssignmentException {
        }

        @Override
        public void setUserRoles(VOUser user, List<UserRoleType> roles)
                throws ObjectNotFoundException, OperationNotPermittedException,
                UserModificationConstraintException,
                UserRoleAssignmentException, UserActiveException {

        }

        @Override
        public VOUserDetails createOnBehalfUser(String organizationId,
                String string) throws ObjectNotFoundException,
                OperationNotPermittedException, NonUniqueBusinessKeyException {
            return null;
        }

        @Override
        public void cleanUpCurrentUser() {

        }

        @Override
        public void refreshLdapUser() throws ValidationException {

        }

        @Override
        public void importUsersInOwnOrganization(byte[] csvData, String marketplaceId)
                throws BulkUserImportException, ObjectNotFoundException {
        }

        @Override
        public void importUsers(byte[] csvData,
                String organizationId, String marketplaceId)
                throws BulkUserImportException, ObjectNotFoundException {
        }

        @Override
        public boolean isCallerOrganizationAdmin() {
            return false;
        }

        @Override
        public boolean addRevokeUserUnitAssignment(String groupName,
                List<VOUser> usersToBeAdded, List<VOUser> usersToBeRevoked)
                throws ObjectNotFoundException, OperationNotPermittedException,
                MailOperationException, NonUniqueBusinessKeyException {
            return false;
        }

        @Override
        public boolean searchLdapUsersOverLimit(String userIdPattern)
                throws ValidationException {
            return false;
        }

        @Override
        public void grantUnitRole(VOUser user, UserRoleType role)
                throws ObjectNotFoundException, OperationNotPermittedException {
        }

        @Override
        public void revokeUnitRole(VOUser user, UserRoleType role)
                throws ObjectNotFoundException, OperationNotPermittedException {
        }
    }

}
