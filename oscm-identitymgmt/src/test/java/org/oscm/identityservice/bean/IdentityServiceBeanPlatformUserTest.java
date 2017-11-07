/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Jun 28, 2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.identityservice.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import org.oscm.communicationservice.local.CommunicationServiceLocal;
import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.DomainObject;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.UserRole;
import org.oscm.identityservice.control.SendMailControl;
import org.oscm.string.Strings;
import org.oscm.taskhandling.local.TaskMessage;
import org.oscm.taskhandling.local.TaskQueueServiceLocal;
import org.oscm.taskhandling.payloads.ImportUserPayload;
import org.oscm.taskhandling.payloads.ImportUserPayload.UserDefinition;
import org.oscm.test.data.Organizations;
import org.oscm.usergroupservice.bean.UserGroupServiceLocalBean;
import org.oscm.internal.types.enumtypes.UserAccountStatus;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.BulkUserImportException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.vo.VOUserDetails;

/**
 * @author farmaki
 * 
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class IdentityServiceBeanPlatformUserTest {

    private IdentityServiceBean idSrv;
    private CommunicationServiceLocal cm;
    private ConfigurationServiceLocal cs;
    private TaskQueueServiceLocal tqs;

    private PlatformUser pUser;

    private UserGroupServiceLocalBean userGroupServiceLocalBean;

    @Before
    public void setup() throws Exception {
        SendMailControl.clear();
        idSrv = spy(new IdentityServiceBean());

        DataService dm = mock(DataService.class);
        idSrv.dm = dm;
        when(dm.getCurrentUser()).thenAnswer(new Answer<PlatformUser>() {
            @Override
            public PlatformUser answer(InvocationOnMock invocation)
                    throws Throwable {
                return pUser;
            }
        });

        cm = mock(CommunicationServiceLocal.class);
        idSrv.cm = cm;

        cs = mock(ConfigurationServiceLocal.class);
        idSrv.cs = cs;

        tqs = mock(TaskQueueServiceLocal.class);
        idSrv.tqs = tqs;

        pUser = new PlatformUser();
        Organization org = new Organization();
        org.setOrganizationId("orgId");
        pUser.setOrganization(org);
        pUser.setKey(1234);
        pUser.setUserId("userid");
        pUser.setEmail("user@gmail.com");

        when(dm.find(any(DomainObject.class))).thenAnswer(
                new Answer<DomainObject<?>>() {

                    @Override
                    public DomainObject<?> answer(InvocationOnMock invocation)
                            throws Throwable {
                        Object template = invocation.getArguments()[0];
                        if (template instanceof Marketplace) {
                            return (Marketplace) template;
                        } else if (template instanceof UserRole) {
                            return (UserRole) template;
                        }
                        return null;
                    }
                });
        userGroupServiceLocalBean = mock(UserGroupServiceLocalBean.class);
        idSrv.userGroupService = userGroupServiceLocalBean;
    }

    @After
    public void tearDown() {
        SendMailControl.clear();
    }

    @Test
    public void addPlatformUser_SAML_SP() throws Exception {
        // Given a SAML_SP authentication mode and
        // a user account status PASSWORD_MUST_BE_CHANGED
        doReturn(Boolean.TRUE).when(cs).isServiceProvider();

        Organization org = Organizations.createOrganization("org");
        VOUserDetails userDetails = getVOUserDetails(org, "user1");
        userDetails.addUserRole(UserRoleType.MARKETPLACE_OWNER);

        // when
        pUser = idSrv.addPlatformUser(userDetails, org, "password",
                UserAccountStatus.PASSWORD_MUST_BE_CHANGED, true, true,
                new Marketplace(), false);

        // then the user account status is ACTIVE and the password is not set
        assertEquals(UserAccountStatus.ACTIVE, pUser.getStatus());
        assertNull(pUser.getPasswordHash());
        assertEquals(0, pUser.getPasswordSalt());
    }

    @Test
    public void addPlatformUser_SAML_SP_LockedNotConfirmed() throws Exception {
        // Given a SAML_SP authentication mode
        // a user account status LOCKED_NOT_CONFIRMED
        doReturn(Boolean.TRUE).when(cs).isServiceProvider();

        Organization org = Organizations.createOrganization("org");
        VOUserDetails userDetails = getVOUserDetails(org, "user1");
        userDetails.addUserRole(UserRoleType.MARKETPLACE_OWNER);

        // when
        pUser = idSrv.addPlatformUser(userDetails, org, "password",
                UserAccountStatus.LOCKED_NOT_CONFIRMED, true, true,
                new Marketplace(), false);

        // then the user account status is the same and the password is not set
        assertEquals(UserAccountStatus.LOCKED_NOT_CONFIRMED, pUser.getStatus());
        assertNull(pUser.getPasswordHash());
        assertEquals(0, pUser.getPasswordSalt());
    }

    @Test
    public void addPlatformUser_INTERNAL() throws Exception {
        // Given an INTERNAL authentication mode
        doReturn(Boolean.FALSE).when(cs).isServiceProvider();

        Organization org = Organizations.createOrganization("org");
        VOUserDetails userDetails = getVOUserDetails(org, "user1");
        userDetails.addUserRole(UserRoleType.MARKETPLACE_OWNER);

        // when
        pUser = idSrv.addPlatformUser(userDetails, org, "password",
                UserAccountStatus.PASSWORD_MUST_BE_CHANGED, true, true,
                new Marketplace(), false);

        // then the user account status is the same and the password is set
        assertEquals(UserAccountStatus.PASSWORD_MUST_BE_CHANGED,
                pUser.getStatus());

        assertNotNull(pUser.getPasswordHash());
    }

    @Test
    public void addPlatformUser_MailOrder_Bug11130() throws Exception {
        // Given an INTERNAL authentication mode
        doReturn(Boolean.FALSE).when(cs).isServiceProvider();

        Organization org = Organizations.createOrganization("org");
        VOUserDetails userDetails = getVOUserDetails(org, "user1");
        userDetails.addUserRole(UserRoleType.MARKETPLACE_OWNER);
        InOrder inOrder = inOrder(idSrv, idSrv.userGroupService);

        // when
        pUser = idSrv.addPlatformUser(userDetails, org, "password",
                UserAccountStatus.PASSWORD_MUST_BE_CHANGED, true, true,
                new Marketplace(), false);

        // then the user account status is the same and the password is set
        assertEquals(UserAccountStatus.PASSWORD_MUST_BE_CHANGED,
                pUser.getStatus());

        assertNotNull(pUser.getPasswordHash());

        inOrder.verify(idSrv).sendMailToCreatedUser(anyString(), anyBoolean(),
                any(Marketplace.class), any(PlatformUser.class));
    }

    @Test
    public void addPlatformUser_INTERNAL_RemoteLDAP() throws Exception {
        // Given an INTERNAL authentication mode
        doReturn(Boolean.FALSE).when(cs).isServiceProvider();

        Organization org = Organizations.createOrganization("org");
        VOUserDetails userDetails = getVOUserDetails(org, "user1");
        userDetails.addUserRole(UserRoleType.MARKETPLACE_OWNER);

        // when
        pUser = idSrv.addPlatformUser(userDetails, org, "password",
                UserAccountStatus.PASSWORD_MUST_BE_CHANGED, true, false,
                new Marketplace(), false);

        // then the user account status is the same and the password is not set
        assertEquals(UserAccountStatus.PASSWORD_MUST_BE_CHANGED,
                pUser.getStatus());

        assertNull(pUser.getPasswordHash());
    }

    @Test
    public void addPlatformUser_INTERNAL_LocalLDAP() throws Exception {
        // Given an INTERNAL authentication mode
        doReturn(Boolean.FALSE).when(cs).isServiceProvider();

        Organization org = Organizations.createOrganization("org");
        VOUserDetails userDetails = getVOUserDetails(org, "user1");
        userDetails.addUserRole(UserRoleType.MARKETPLACE_OWNER);

        // when
        pUser = idSrv.addPlatformUser(userDetails, org, "password",
                UserAccountStatus.PASSWORD_MUST_BE_CHANGED, true, true,
                new Marketplace(), false);

        // then the user account status is the same and the password is set
        assertEquals(UserAccountStatus.PASSWORD_MUST_BE_CHANGED,
                pUser.getStatus());

        assertNotNull(pUser.getPasswordHash());
    }

    private VOUserDetails getVOUserDetails(Organization org, String userId) {
        VOUserDetails user = new VOUserDetails();
        user.setEMail(org.getEmail());
        user.setOrganizationId(org.getOrganizationId());
        user.setLocale(org.getLocale());
        user.setUserId(userId);
        return user;
    }

    /**
     * Import users with CSV data
     */
    @Test
    public void importUsers() throws Exception {

        // given
        byte[] csv = bytes("user1,user1@org.com,en,MR,John,Doe,ORGANIZATION_ADMIN");

        // when importing
        idSrv.importUsers(csv, pUser.getOrganization(), null);

        // then parsed users must be sent to task queue
        List<UserDefinition> parsedUsers = captureFromTaskQueue();
        assertEquals(1, parsedUsers.size());
        assertEquals("user1", parsedUsers.get(0).getUserDetails().getUserId());
    }

    /**
     * Import users with CSV data. The CSV contains two user rows
     */
    @Test
    public void importUsers_twoUsers() throws Exception {

        // given
        byte[] csv = bytes("user1,user1@org.com,en,MR,John,Doe,ORGANIZATION_ADMIN \n user2,user1@org.com,en,MR,John,Doe,ORGANIZATION_ADMIN");

        // when importing
        idSrv.importUsers(csv, pUser.getOrganization(), null);

        // then parsed users must be sent to task queue
        List<UserDefinition> parsedUsers = captureFromTaskQueue();
        assertEquals(2, parsedUsers.size());
        assertEquals("user1", parsedUsers.get(0).getUserDetails().getUserId());
        assertEquals("user2", parsedUsers.get(1).getUserDetails().getUserId());
    }

    private List<UserDefinition> captureFromTaskQueue() {
        ArgumentCaptor<List> argument = ArgumentCaptor.forClass(List.class);
        verify(tqs, times(1)).sendAllMessages(argument.capture());
        TaskMessage asyncTask = (TaskMessage) argument.getValue().get(0);
        ImportUserPayload payload = (ImportUserPayload) asyncTask.getPayload();
        return payload.getUsersToBeImported();
    }

    /**
     * This test checks if exception is thrown if invalid CSV data is given
     * 
     * @throws Exception
     */
    @Test(expected = BulkUserImportException.class)
    public void importUsers_invalidFormat() throws Exception {
        byte[] invalidCsv = bytes("user1,user1@org.com,en,MR,John,");
        idSrv.importUsers(invalidCsv, pUser.getOrganization(), null);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void importUsers_invalidMarketplaceId() throws Exception {
        // given
        doThrow(new ObjectNotFoundException()).when(idSrv.dm)
                .getReferenceByBusinessKey(any(Marketplace.class));
        byte[] csv = bytes("user1,user1@org.com,en,MR,John,Doe,ORGANIZATION_ADMIN");
        // when
        idSrv.importUsers(csv, pUser.getOrganization(), "testMarketplaceId");
    }

    /**
     * The executing user must have an email. Otherwise, the report of the
     * import cannot be sent by mail to him.
     * 
     * @throws Exception
     */
    @Test(expected = BulkUserImportException.class)
    public void importUsers_noEmail() throws Exception {

        // given the executing user without an email
        pUser.setEmail(null);

        // when importing, then exception is thrown
        byte[] invalidCsv = bytes("user1,user1@org.com,en,MR,John,Doe,ORGANIZATION_ADMIN");
        idSrv.importUsers(invalidCsv, pUser.getOrganization(), null);
    }

    private byte[] bytes(String value) {
        return Strings.toBytes(value);
    }

    @Test
    public void importUser_assignDefaultGroup() throws Exception {

        // given
        VOUserDetails user = new VOUserDetails();
        user.setOrganizationId("organizationId");
        doReturn(new PlatformUser()).when(idSrv).addPlatformUser(
                any(VOUserDetails.class), any(Organization.class),
                any(String.class),
                eq(UserAccountStatus.PASSWORD_MUST_BE_CHANGED), eq(true),
                eq(true), any(Marketplace.class), eq(true));

        // when
        idSrv.importUser(user, "marketplace");
        // then
        verify(idSrv, times(1)).addPlatformUser(any(VOUserDetails.class),
                any(Organization.class), any(String.class),
                eq(UserAccountStatus.PASSWORD_MUST_BE_CHANGED), eq(true),
                eq(true), any(Marketplace.class), eq(true));
    }
}
