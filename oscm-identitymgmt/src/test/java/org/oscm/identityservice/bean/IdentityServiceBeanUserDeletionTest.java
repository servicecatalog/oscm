/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2013-3-1                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.identityservice.bean;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.ejb.SessionContext;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;

import org.oscm.communicationservice.local.CommunicationServiceLocal;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Session;
import org.oscm.domobjects.Subscription;
import org.oscm.reviewservice.bean.ReviewServiceLocalBean;
import org.oscm.sessionservice.local.SessionServiceLocal;
import org.oscm.subscriptionservice.local.SubscriptionServiceLocal;
import org.oscm.types.enumtypes.EmailType;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.IllegalArgumentException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.UserDeletionConstraintException;
import org.oscm.internal.vo.VOUserDetails;

/**
 * The unit test for identityServiceBean.deleteUser
 * 
 * @author Gao
 * 
 */
public class IdentityServiceBeanUserDeletionTest {

    private static final String ORGANIZATION_ID = "ORG_ID";
    private static final String ORGANIZATION_NAME = "ORG_NANE";
    private IdentityServiceBean idMgmt;
    private DataService dm;
    private SubscriptionServiceLocal sm;
    private SessionServiceLocal sessionService;
    private SessionContext sessionCtx;
    private Principal principal;
    private ReviewServiceLocalBean rvs;
    private CommunicationServiceLocal cm;
    private VOUserDetails user;
    private PlatformUser userToBeDeleted;
    private PlatformUser currentUser;
    private Organization org;
    private List<Session> userSessions;
    @Captor
    private ArgumentCaptor<Object[]> ac;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);

        idMgmt = new IdentityServiceBean();

        dm = mock(DataService.class);
        idMgmt.dm = dm;

        sm = mock(SubscriptionServiceLocal.class);
        idMgmt.sm = sm;

        sessionService = mock(SessionServiceLocal.class);
        idMgmt.sessionService = sessionService;

        rvs = mock(ReviewServiceLocalBean.class);
        idMgmt.rvs = rvs;

        principal = mock(Principal.class);
        sessionCtx = mock(SessionContext.class);
        doReturn(principal).when(sessionCtx).getCallerPrincipal();

        idMgmt.sessionCtx = sessionCtx;

        cm = mock(CommunicationServiceLocal.class);
        idMgmt.cm = cm;

        setupUsers();

        doReturn(new ArrayList<Subscription>()).when(sm)
                .getSubscriptionsForUserInt(userToBeDeleted);
        doReturn("Test").when(principal).getName();
        userSessions = new ArrayList<Session>();
        doReturn(userSessions).when(sessionService).getSessionsForUserKey(
                user.getKey());
    }

    @Test(expected = IllegalArgumentException.class)
    public void deleteUser_ArgumentIsNull() throws Exception {
        // given
        user = null;
        // when
        idMgmt.deleteUser(user, null);
        // then
        verify(dm, never()).getReference(PlatformUser.class, anyLong());
    }

    @Test(expected = ObjectNotFoundException.class)
    public void deleteUser_ObjectNotFoundException() throws Exception {
        // given
        doThrow(new ObjectNotFoundException()).when(dm).getReference(
                PlatformUser.class, user.getKey());
        // when
        idMgmt.deleteUser(user, null);
        // then
        verify(dm, times(1))
                .getReference(PlatformUser.class, eq(user.getKey()));

    }

    @Test(expected = ConcurrentModificationException.class)
    public void deleteUser_ConcurrentModificationException() throws Exception {
        // given
        user.setVersion(-1);
        // when
        idMgmt.deleteUser(user, null);
        // then
        verify(dm, times(1))
                .getReference(PlatformUser.class, eq(user.getKey()));
    }

    @Test(expected = OperationNotPermittedException.class)
    public void deleteUser_OperationNotPermittedException() throws Exception {
        // given
        user = createTestUser();
        userToBeDeleted.setOrganization(new Organization());
        currentUser.setOrganization(new Organization());
        // when
        idMgmt.deleteUser(user, null);
        // then
        verify(dm, times(1))
                .getReference(PlatformUser.class, eq(user.getKey()));
    }

    @Test(expected = UserDeletionConstraintException.class)
    public void deleteUser_UserDeletionConstraintException_ActiveSubscription()
            throws Exception {
        // given
        Subscription subscription = new Subscription();
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        doReturn(Arrays.asList(subscription)).when(sm)
                .getSubscriptionsForUserInt(userToBeDeleted);
        // when
        try {
            idMgmt.deleteUser(user, null);
        } catch (UserDeletionConstraintException e) {
            // then
            assertEquals(
                    Boolean.TRUE,
                    Boolean.valueOf(e
                            .getMessageKey()
                            .contains(
                                    String.valueOf(UserDeletionConstraintException.Reason.HAS_ACTIVE_SUBSCRIPTIONS))));
            throw e;
        }
    }

    @Test(expected = UserDeletionConstraintException.class)
    public void deleteUser_UserDeletionConstraintException_IsUserLoggedIn()
            throws Exception {
        // given
        userSessions.add(new Session());
        // when
        try {
            idMgmt.deleteUser(user, null);
        } catch (UserDeletionConstraintException e) {
            // then
            assertEquals(
                    Boolean.TRUE,
                    Boolean.valueOf(e
                            .getMessageKey()
                            .contains(
                                    String.valueOf(UserDeletionConstraintException.Reason.IS_USER_LOGGED_IN))));
            throw e;
        }
    }

    @Test(expected = UserDeletionConstraintException.class)
    public void deleteUser_UserDeletionConstraintException_SelfDelete()
            throws Exception {
        // given
        doReturn(String.valueOf(user.getKey())).when(principal).getName();
        // when
        try {
            idMgmt.deleteUser(user, null);
        } catch (UserDeletionConstraintException e) {
            // then
            assertEquals(
                    Boolean.TRUE,
                    Boolean.valueOf(e
                            .getMessageKey()
                            .contains(
                                    String.valueOf(UserDeletionConstraintException.Reason.FORBIDDEN_SELF_DELETION))));
            throw e;
        }
    }

    @Test
    public void deleteUser_OK() throws Exception {
        // given
        doNothing().when(rvs).deleteReviewsOfUser(userToBeDeleted, false);
        doNothing().when(cm).sendMail(userToBeDeleted, EmailType.USER_DELETED,
                new Object[] { org.getName() }, null);
        // when
        idMgmt.deleteUser(user, null);
        // then
        verify(dm, times(1)).remove(eq(userToBeDeleted));
        verify(cm, times(1)).sendMail(eq(userToBeDeleted),
                eq(EmailType.USER_DELETED), ac.capture(),
                any(Marketplace.class));
        assertEquals(ORGANIZATION_NAME, ac.getValue()[0].toString());

    }

    @Test
    public void deletePlatformUser() throws Exception {
        // given
        Subscription subscription = new Subscription();
        subscription.setStatus(SubscriptionStatus.EXPIRED);
        doReturn(Arrays.asList(subscription)).when(sm)
                .getSubscriptionsForUserInt(userToBeDeleted);
        // when
        idMgmt.deletePlatformUser(userToBeDeleted, false, false,
                new Marketplace());
        // then
        verify(idMgmt.sm, times(1)).revokeUserFromSubscriptionInt(subscription,
                Arrays.asList(userToBeDeleted));
    }

    /**
     * create user for test
     * 
     * @return
     */
    private VOUserDetails createTestUser() {
        VOUserDetails user = new VOUserDetails();
        user.setOrganizationId(ORGANIZATION_ID);
        user.setUserId(ORGANIZATION_ID + "usera");
        user.setEMail("someMail@somehost.com");
        user.setFirstName("Harald");
        user.setLastName("Wilhelm");
        user.setLocale(Locale.ENGLISH.toString());
        return user;
    }

    /**
     * create users and organization for test
     * 
     * @throws Exception
     */
    private void setupUsers() throws Exception {
        user = createTestUser();
        userToBeDeleted = new PlatformUser();
        doReturn(userToBeDeleted).when(dm).getReference(PlatformUser.class,
                user.getKey());
        currentUser = new PlatformUser();
        doReturn(currentUser).when(dm).getCurrentUser();
        org = new Organization();
        org.setName(ORGANIZATION_NAME);
        userToBeDeleted.setOrganization(org);
        currentUser.setOrganization(org);
    }

}
