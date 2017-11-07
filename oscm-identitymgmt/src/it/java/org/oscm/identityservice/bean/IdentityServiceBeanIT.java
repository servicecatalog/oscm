/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *  Creation Date: 12.02.2009                                                      
 *
 *******************************************************************************/

package org.oscm.identityservice.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.oscm.test.matchers.BesMatchers.isPersisted;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.Callable;

import javax.ejb.EJBException;
import javax.persistence.Query;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.oscm.communicationservice.data.SendMailStatus;
import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.converter.ParameterEncoder;
import org.oscm.converter.ParameterizedTypes;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.ConfigurationSetting;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.OnBehalfUserReference;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationSetting;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.RoleAssignment;
import org.oscm.domobjects.Session;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.UsageLicense;
import org.oscm.domobjects.UserGroup;
import org.oscm.domobjects.UserGroupToUser;
import org.oscm.encrypter.AESEncrypter;
import org.oscm.identityservice.assembler.UserDataAssembler;
import org.oscm.identityservice.ldap.LdapAccessStub;
import org.oscm.identityservice.local.IdentityServiceLocal;
import org.oscm.identityservice.local.LdapSettingsManagementServiceLocal;
import org.oscm.internal.intf.IdentityService;
import org.oscm.internal.types.enumtypes.ConfigurationKey;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.SettingType;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.types.enumtypes.UserAccountStatus;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.MailOperationException;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.OperationPendingException;
import org.oscm.internal.types.exception.OrganizationRemovedException;
import org.oscm.internal.types.exception.SecurityCheckException;
import org.oscm.internal.types.exception.UserActiveException;
import org.oscm.internal.types.exception.UserDeletionConstraintException;
import org.oscm.internal.types.exception.UserRoleAssignmentException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.types.exception.ValidationException.ReasonEnum;
import org.oscm.internal.vo.VOUser;
import org.oscm.internal.vo.VOUserDetails;
import org.oscm.reviewservice.bean.ReviewServiceLocalBean;
import org.oscm.sessionservice.bean.SessionManagementStub;
import org.oscm.subscriptionservice.local.SubscriptionServiceLocal;
import org.oscm.taskhandling.local.TaskMessage;
import org.oscm.taskhandling.operations.UpdateUserHandler;
import org.oscm.taskhandling.payloads.UpdateUserPayload;
import org.oscm.test.EJBTestBase;
import org.oscm.test.MailDetails;
import org.oscm.test.data.Marketplaces;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.PlatformUsers;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.CommunicationServiceStub;
import org.oscm.test.stubs.ConfigurationServiceStub;
import org.oscm.test.stubs.TaskQueueServiceStub;
import org.oscm.test.stubs.TriggerQueueServiceStub;
import org.oscm.types.constants.Configuration;
import org.oscm.types.enumtypes.EmailType;
import org.oscm.usergroupservice.bean.UserGroupServiceLocalBean;
import org.oscm.usergroupservice.dao.UserGroupDao;
import org.oscm.usergroupservice.dao.UserGroupUsersDao;

@SuppressWarnings("boxing")
public class IdentityServiceBeanIT extends EJBTestBase {

    private static final String MP_ID = "TEST";
    private static final String USER_KEY_EXISTING = "1";
    private static final String USER_ID_EXISTING = "admin";
    private static final String USER_ID_EXISTING_SP_ADMIN = "supplier_admin";
    private static final String USER_ID_EXISTING_SP = "supplier_user";
    private static final String USER_ID_EXISTING_OP_ADMIN = "operator_admin";
    private static final String USER_ID_EXISTING_OP_USER = "operator_user";
    private static final String USER_ID_EXISTING_2 = "admin2";
    private static final String ORGANIZATION_ID = "BMW";
    private static final String ORGANIZATION_ID_2 = "EST";
    private static final String BASE_URL = "BASE_URL";
    private static final String BASE_URL_WITH_SLASH = "BASE_URL/";
    private static final String BASE_URL_WITH_TWO_SLASHES = "BASE_URL//";
    private String INSERT_UNIT_ROLE_SQL = "INSERT INTO unituserrole (tkey, version, rolename) VALUES (?, ?, ?)";

    private IdentityService idMgmt;
    private IdentityServiceLocal idMgmtLocal;
    private ConfigurationServiceLocal cfg;
    private ReviewServiceLocalBean reviewServiceMock;
    private DataService mgr;
    private LdapAccessStub ldapAccessStub;
    private Organization organization = null;
    private Organization po = null;
    private String userKey;
    private String userKey_2;
    private String operatorKey;
    private Long operator_user_key;

    private int mailCounter;
    private EmailType mailType;
    private Object[] receivedParams = null;

    private boolean throwMailOperationFailed = false;
    private EmailType exceptionCausingEmailType = null;

    private final List<Session> sessionList = new ArrayList<>();

    private String initialPassword;

    private String encodedParam;

    private List<TaskMessage> messagesOfTaskQueue = new ArrayList<>();

    private final List<MailDetails<PlatformUser>> sendedMails = new LinkedList<>();
    private LdapSettingsManagementServiceLocal ldapSettingsMock;
    private final Properties ldapOrgSettingsResolved = new Properties();
    private SubscriptionServiceLocal subSvcMock;

    @Override
    public void setup(TestContainer container) throws Exception {
        AESEncrypter.generateKey();
        container.enableInterfaceMocking(true);
        container.login(USER_KEY_EXISTING);
        container.addBean(new DataServiceBean());
        reviewServiceMock = mock(ReviewServiceLocalBean.class);
        container.addBean(reviewServiceMock);
        ldapSettingsMock = mock(LdapSettingsManagementServiceLocal.class);
        container.addBean(ldapSettingsMock);
        container.addBean(new UserGroupDao());
        container.addBean(new UserGroupUsersDao());
        container.addBean(new UserGroupServiceLocalBean());
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new TriggerQueueServiceStub());
        container.addBean(new CommunicationServiceStub() {
            @Override
            public SendMailStatus<PlatformUser> sendMail(EmailType type,
                    Object[] params, Marketplace marketplace,
                    PlatformUser... recipients) {

                SendMailStatus<PlatformUser> mailStatus = new SendMailStatus<>();
                for (PlatformUser recipient : recipients) {
                    try {
                        sendMail(recipient, type, params, marketplace);
                        mailStatus.addMailStatus(recipient);
                    } catch (MailOperationException e) {
                        mailStatus.addMailStatus(recipient, e);
                    }
                }
                return mailStatus;
            }

            @Override
            public void sendMail(PlatformUser recipient, EmailType type,
                    Object[] params, Marketplace marketplace)
                    throws MailOperationException {
                mailCounter++;
                mailType = type;
                receivedParams = params;
                // reset member
                encodedParam = null;

                if (params != null && params.length > 1) {
                    initialPassword = String.valueOf(params[1]);
                }
                if (params != null && params.length == 1) {
                    String param = String.valueOf(params[0]);
                    if (param.contains("/confirm.jsf?") && param
                            .indexOf("/confirm.jsf?") < param.indexOf("enc=")) {
                        encodedParam = param
                                .substring(param.indexOf("enc=") + 4);
                    }
                }
                if (throwMailOperationFailed) {
                    throw new MailOperationException("Test");
                }
                if (exceptionCausingEmailType == EmailType.USER_CONFIRM) {
                    throw new MailOperationException("Test");
                }

                sendedMails.add(new MailDetails<>(recipient, type, params));
            }

            @Override
            public String getMarketplaceUrl(String marketplaceId) {
                return "?mId=" + MP_ID;
            }
        });
        container.addBean(new SessionManagementStub() {
            @Override
            public List<Session> getSessionsForUserKey(long platformUserKey) {
                return sessionList;
            }
        });
        subSvcMock = mock(SubscriptionServiceLocal.class);
        container.addBean(subSvcMock);
        container.addBean(ldapAccessStub = new LdapAccessStub());
        container.addBean(new TaskQueueServiceStub() {

            @Override
            public void sendAllMessages(List<TaskMessage> messages) {
                messagesOfTaskQueue.addAll(messages);
            }
        });
        container.addBean(new IdentityServiceBean());

        mgr = container.get(DataService.class);
        idMgmt = container.get(IdentityService.class);
        idMgmtLocal = container.get(IdentityServiceLocal.class);
        cfg = container.get(ConfigurationServiceLocal.class);

        setUpDirServerStub(cfg);
        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                createPaymentTypes(mgr);
                createOrganizationRoles(mgr);
                createUserRoles(mgr);
                po = Organizations.createOrganization(mgr,
                        OrganizationRoleType.PLATFORM_OPERATOR,
                        OrganizationRoleType.TECHNOLOGY_PROVIDER,
                        OrganizationRoleType.SUPPLIER,
                        OrganizationRoleType.MARKETPLACE_OWNER);
                Marketplaces.createGlobalMarketplace(po, MP_ID, mgr);
                mgr.flush();
                PlatformUser pt_user = new PlatformUser();
                pt_user.setUserId(USER_ID_EXISTING_OP_ADMIN);
                pt_user.setEmail("someMail@somehost.de");
                pt_user.setOrganization(po);
                pt_user.setStatus(UserAccountStatus.ACTIVE);
                pt_user.setLocale(Locale.ENGLISH.toString());
                po.addPlatformUser(pt_user);
                mgr.persist(pt_user);
                PlatformUsers.grantAdminRole(mgr, pt_user);
                PlatformUsers.grantRoles(mgr, pt_user,
                        UserRoleType.MARKETPLACE_OWNER);
                operatorKey = String.valueOf(pt_user.getKey());

                PlatformUser n_user = new PlatformUser();
                n_user.setUserId(USER_ID_EXISTING_OP_USER);
                n_user.setEmail("someMail@somehost.de");
                n_user.setOrganization(po);
                n_user.setStatus(UserAccountStatus.ACTIVE);
                n_user.setLocale(Locale.ENGLISH.toString());
                po.addPlatformUser(n_user);
                mgr.persist(n_user);
                operator_user_key = Long.valueOf(n_user.getKey());
                return null;
            }
        });
        String userName = setupUsers();
        doAnswer(new Answer<Properties>() {
            @Override
            public Properties answer(InvocationOnMock invocation) {
                return ldapOrgSettingsResolved;
            }
        }).when(ldapSettingsMock).getOrganizationSettingsResolved(anyString());
        container.login(userName, ROLE_ORGANIZATION_ADMIN);
        initUnitRoles();
    }

    private void initUnitRoles() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                insertUnitRole(Long.valueOf(1L), Long.valueOf(0L),
                        "ADMINISTRATOR");
                insertUnitRole(Long.valueOf(2L), Long.valueOf(0L), "USER");
                return null;
            }
        });
    }

    private void insertUnitRole(Long key, Long version, String roleName) {
        Query query = mgr.createNativeQuery(INSERT_UNIT_ROLE_SQL);
        query.setParameter(1, key);
        query.setParameter(2, version);
        query.setParameter(3, roleName);
        query.executeUpdate();
    }

    public String setupUsers() throws Exception {
        return runTX(new Callable<String>() {
            @Override
            public String call() throws Exception {
                Organization cust = new Organization();
                cust.setOrganizationId(ORGANIZATION_ID);
                cust.setName("The organization");
                cust.setAddress(
                        "my address is a very long string, which is stored in the database \n with line delimiters\n.");
                cust.setEmail("organization@organization.com");
                cust.setPhone("012345/678");
                cust.setLocale(Locale.ENGLISH.toString());
                cust.setCutOffDay(1);
                mgr.persist(cust);
                Organizations.grantOrganizationRoles(mgr, cust,
                        OrganizationRoleType.CUSTOMER);
                organization = cust;
                PlatformUser user = new PlatformUser();
                user.setUserId(USER_ID_EXISTING);
                user.setEmail("someMail@somehost.de");
                user.setOrganization(cust);
                user.setStatus(UserAccountStatus.ACTIVE);
                user.setLocale(Locale.ENGLISH.toString());
                cust.addPlatformUser(user);
                mgr.persist(user);
                PlatformUsers.grantAdminRole(mgr, user);
                userKey = String.valueOf(user.getKey());

                UserGroup defaultGroup = new UserGroup();
                defaultGroup.setOrganization(organization);
                defaultGroup.setIsDefault(true);
                defaultGroup.setName("default");
                mgr.persist(defaultGroup);

                cust = new Organization();
                cust.setOrganizationId(ORGANIZATION_ID_2);
                cust.setName("The organization");
                cust.setAddress(
                        "my address is a very long string, which is stored in the database \n with line delimiters\n.");
                cust.setEmail("organization@organization.com");
                cust.setPhone("012345/678");
                cust.setLocale(Locale.ENGLISH.toString());
                cust.setCutOffDay(1);
                mgr.persist(cust);
                Organizations.grantOrganizationRoles(mgr, cust,
                        OrganizationRoleType.CUSTOMER);
                user = new PlatformUser();
                user.setUserId(USER_ID_EXISTING_2);
                user.setEmail("someMail@somehost.de");
                user.setOrganization(cust);
                user.setStatus(UserAccountStatus.ACTIVE);
                user.setLocale(Locale.ENGLISH.toString());
                cust.addPlatformUser(user);
                mgr.persist(user);
                userKey_2 = String.valueOf(user.getKey());

                return userKey;
            }
        });
    }

    // Test section

    @Test
    public void testUpdateUser_DifferentEmail() throws Exception {
        sendedMails.clear();
        try {
            final String oldEmail = "admin@organization.com";
            final String newEmail = "enes.sejfi@est.fujitsu.com";

            modifyUserData(oldEmail, newEmail);

            assertEquals(2, sendedMails.size());
            checkEmail(0, newEmail);
            checkEmail(1, oldEmail);

        } finally {
            sendedMails.clear();
        }
    }

    @Test
    public void testUpdateUser_SameEmail() throws Exception {
        sendedMails.clear();
        try {
            final String mail = "admin@organization.com";
            modifyUserData(mail, mail);
            assertEquals(1, sendedMails.size());
            checkEmail(0, mail);
        } finally {
            sendedMails.clear();
        }
    }

    private void modifyUserData(final String oldEmail, final String newEmail)
            throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                PlatformUser existingUser = new PlatformUser();
                existingUser.setEmail(oldEmail);
                existingUser.setLocale(Locale.GERMAN.toString());
                existingUser.setOrganization(mgr.getReference(
                        Organization.class, organization.getKey()));
                existingUser.setUserId("g");

                VOUserDetails user = new VOUserDetails();
                user.setUserId("g");
                user.setEMail(newEmail);
                user.setLocale(Locale.GERMAN.toString());
                user.setOrganizationId(organization.getOrganizationId());

                idMgmtLocal.modifyUserData(existingUser, user, false, true);
                return null;
            }
        });
    }

    private void checkEmail(int index, String expectedEmail) {
        assertEquals(expectedEmail,
                sendedMails.get(index).getInstance().getEmail());

        assertEquals(EmailType.USER_UPDATED,
                sendedMails.get(index).getEmailType());

        assertNull(sendedMails.get(index).getParams());
    }

    @Test
    public void testGetUsersForOrganization() {
        List<VOUserDetails> users = idMgmt.getUsersForOrganization();
        Assert.assertEquals(1, users.size());
    }

    /**
     * On behalf off user must not be visible and filtered from the result list.
     *
     * @throws Exception
     */
    @Test
    public void testGetUsersForOrganization_onBehalfeOf() throws Exception {

        // the setup creates an organization with one normal user ('admin')
        // when creating a on-behalf user
        createSlaveUser();

        // then the result size must still be one (instead of two)
        List<VOUserDetails> users = idMgmt.getUsersForOrganization();
        assertEquals(1, users.size());
        assertEquals("admin", users.get(0).getUserId()); // not on-behalf-off
    }

    private void createSlaveUser() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                PlatformUser master = mgr.getReference(PlatformUser.class,
                        Long.parseLong(userKey));
                PlatformUser slave = PlatformUsers.createUser(mgr,
                        "on-behalf-off", organization);
                OnBehalfUserReference ref = new OnBehalfUserReference();
                ref.setSlaveUser(slave);
                ref.setMasterUser(master);
                ref.setSlaveUser(slave);
                mgr.persist(ref);
                return null;
            }
        });
    }

    @Test
    public void testAddPlatformUser() throws Exception {
        VOUserDetails userToCreate = createTestUser();
        VOUserDetails createdUser = idMgmt.createUser(userToCreate,
                new ArrayList<UserRoleType>(), MP_ID);

        assertThat(createdUser, isPersisted());
        assertEquals(UserAccountStatus.PASSWORD_MUST_BE_CHANGED,
                createdUser.getStatus());
        assertEquals(userToCreate.getUserId(), createdUser.getUserId());
        assertTrue(isUserPartOfOrganization(userToCreate.getUserId()));
    }

    @Test(expected = NonUniqueBusinessKeyException.class)
    public void createUser_userAlreadyExists() throws Exception {
        // given
        VOUserDetails userToCreate = createTestUser();
        idMgmt.createUser(userToCreate, new ArrayList<UserRoleType>(), MP_ID);

        // when create same user again
        idMgmt.createUser(userToCreate, new ArrayList<UserRoleType>(), MP_ID);
    }

    public boolean isUserPartOfOrganization(String userIdToVerify) {
        boolean userPartOfOrganization = false;
        List<VOUserDetails> users = idMgmt.getUsersForOrganization();
        for (VOUser userDetails : users) {
            String userId = userDetails.getUserId();
            if (userId != null && userId.equals(userIdToVerify)) {
                userPartOfOrganization = true;
            }
        }
        return userPartOfOrganization;
    }

    @Test
    public void createUserWithGroups_noneUserGroups() throws Exception {
        // given
        VOUserDetails userToCreate = createTestUser();
        idMgmtLocal.createUserWithGroups(userToCreate,
                new ArrayList<UserRoleType>(), MP_ID, null);

        final VOUserDetails savedUser = retrieveUser(userToCreate);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                PlatformUser user = mgr.getReference(PlatformUser.class,
                        savedUser.getKey());
                assertEquals(0, user.getUserGroupToUsers().size());
                return null;
            }
        });
    }

    /*
     * public void createUserWithGroups() throws Exception { // given
     * VOUserDetails userToCreate = createTestUser(); UserGroup userGroup =
     * runTX(new Callable<UserGroup>() {
     * 
     * @Override public UserGroup call() throws Exception { UserGroup group =
     * new UserGroup(); group.setOrganization(organization);
     * group.setIsDefault(false); group.setName("group"); mgr.persist(group);
     * return group; } });
     * 
     * // mock DataService mgr = spy(container.get(DataServiceBean.class));
     * doReturn(givenUser(123L, "userId",
     * organization)).when(mgr).getCurrentUser(); container.addBean(mgr);
     * UnitUserRole unitUserRole = new UnitUserRole(); unitUserRole.setKey(1);
     * unitUserRole.setRoleName(UnitRoleType.ADMINISTRATOR); UserGroup group =
     * new UserGroup(); group.setKey(userGroup.getKey());
     * group.setName(userGroup.getName());
     * 
     * Map<Long, UnitUserRole> groupsWithRoles = new HashMap<>();
     * groupsWithRoles.put(group.getKey(), unitUserRole);
     * 
     * idMgmtLocal.createUserWithGroups(userToCreate, new
     * ArrayList<UserRoleType>(), MP_ID, groupsWithRoles); final VOUserDetails
     * savedUser = retrieveUser(userToCreate);
     * 
     * runTX(new Callable<Void>() {
     * 
     * @Override public Void call() throws Exception {
     * 
     * PlatformUser user = mgr.getReference(PlatformUser.class,
     * savedUser.getKey()); UserGroupToUser userGroupToUser =
     * user.getUserGroupToUsers() .get(0); assertEquals(1,
     * user.getUserGroupToUsers().size());
     * 
     * UserGroup userGroup = userGroupToUser.getUserGroup();
     * 
     * Organization org = mgr.getReference(Organization.class,
     * userGroup.getOrganization_tkey()); assertEquals(organization.getKey(),
     * org.getKey());
     * 
     * assertEquals(userGroupToUser.getUnitRoleAssignments().get(0).
     * getUnitUserRole().getRoleName(), UnitRoleType.ADMINISTRATOR); return
     * null; } });
     * 
     * // restore original DataService container.addBean(new DataServiceBean());
     * }
     */

    @Test(expected = OperationNotPermittedException.class)
    public void addRevokeUserUnitAssignment_revokeFromDefaultGroup()
            throws Exception {
        // given
        PlatformUser revokerUser = runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                return mgr.getReference(PlatformUser.class,
                        Long.valueOf(userKey).longValue());
            }
        });
        VOUser voRevokeUser = new VOUser();
        voRevokeUser.setUserId(revokerUser.getUserId());

        idMgmt.addRevokeUserUnitAssignment("default", new ArrayList<VOUser>(),
                Collections.singletonList(voRevokeUser));
    }

    @Test(expected = OperationNotPermittedException.class)
    public void addRevokeUserUnitAssignment_assignToDefaultGroup()
            throws Exception {
        // given
        PlatformUser assignUser = runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                return mgr.getReference(PlatformUser.class,
                        Long.valueOf(userKey).longValue());
            }
        });
        VOUser voAssignUser = new VOUser();
        voAssignUser.setUserId(assignUser.getUserId());

        idMgmt.addRevokeUserUnitAssignment("default",
                Collections.singletonList(voAssignUser),
                new ArrayList<VOUser>());
    }

    @Test(expected = OperationNotPermittedException.class)
    public void addRevokeUserUnitAssignment_assignBehalfUser()
            throws Exception {
        // given
        PlatformUser assignUser = runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                PlatformUser user = mgr.getReference(PlatformUser.class,
                        Long.valueOf(userKey).longValue());
                user.setMaster(new OnBehalfUserReference());
                mgr.refresh(user);
                return user;
            }
        });
        VOUser voAssignUser = new VOUser();
        voAssignUser.setUserId(assignUser.getUserId());

        idMgmt.addRevokeUserUnitAssignment("default",
                Collections.singletonList(voAssignUser),
                new ArrayList<VOUser>());
    }

    @Test
    public void addRevokeUserUnitAssignment() throws Exception {
        // given
        final List<PlatformUser> users = runTX(
                new Callable<List<PlatformUser>>() {
                    @Override
                    public List<PlatformUser> call() throws Exception {
                        List<PlatformUser> users = new ArrayList<>();
                        PlatformUser user1 = mgr.getReference(
                                PlatformUser.class,
                                Long.valueOf(userKey).longValue());
                        PlatformUser user2 = new PlatformUser();
                        user2.setUserId("user2");
                        user2.setEmail("someMail@somehost.de");
                        user2.setOrganization(organization);
                        user2.setStatus(UserAccountStatus.ACTIVE);
                        user2.setLocale(Locale.ENGLISH.toString());
                        organization.addPlatformUser(user2);
                        mgr.persist(user2);
                        users.add(user1);
                        users.add(user2);
                        return users;
                    }
                });

        final UserGroup userGroup = runTX(new Callable<UserGroup>() {
            @Override
            public UserGroup call() throws Exception {
                UserGroup group = new UserGroup();
                group.setOrganization(organization);
                group.setIsDefault(false);
                group.setName("group");
                mgr.persist(group);

                UserGroupToUser userGroupToUser = new UserGroupToUser();
                userGroupToUser.setUserGroup(group);
                userGroupToUser.setPlatformuser(users.get(1));
                mgr.persist(userGroupToUser);

                mgr.flush();
                mgr.refresh(group);
                assertEquals(1, group.getUserGroupToUsers().size());
                assertEquals("user2", group.getUserGroupToUsers().get(0)
                        .getPlatformuser().getUserId());
                return group;
            }
        });

        VOUser voAssignUser = new VOUser();
        voAssignUser.setUserId(users.get(0).getUserId());
        VOUser voRevokeUser = new VOUser();
        voRevokeUser.setUserId(users.get(1).getUserId());

        // when
        boolean result = idMgmt.addRevokeUserUnitAssignment(userGroup.getName(),
                Collections.singletonList(voAssignUser),
                Collections.singletonList(voRevokeUser));

        // then
        assertEquals(Boolean.TRUE, Boolean.valueOf(result));
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                UserGroup group = mgr.getReference(UserGroup.class,
                        userGroup.getKey());
                assertEquals(1, group.getUserGroupToUsers().size());
                assertEquals("admin", group.getUserGroupToUsers().get(0)
                        .getPlatformuser().getUserId());
                return null;
            }
        });
    }

    @Test
    public void testAddPlatformUser_WithMarketplace() throws Exception {
        VOUserDetails user = createTestUser();
        idMgmt.createUser(user, Collections.<UserRoleType> emptyList(), MP_ID);
        Assert.assertTrue(
                Arrays.toString(receivedParams).contains("?mId=" + MP_ID));

        assertTrue(isUserPartOfOrganization(user.getUserId()));
        VOUser updatedUser = retrieveUser(user);
        Assert.assertEquals(UserAccountStatus.PASSWORD_MUST_BE_CHANGED,
                updatedUser.getStatus());
    }

    @Test(expected = NonUniqueBusinessKeyException.class)
    public void testAddPlatformUserSaasNonUniqueBusinessKey() throws Exception {
        final VOUserDetails user = createUserAndReRetrieveIt();
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                idMgmt.createUser(user, Collections.<UserRoleType> emptyList(),
                        null);
                return null;
            }
        });
    }

    @Test(expected = MailOperationException.class)
    public void testAddPlatformUserMailOperationFailed() throws Exception {
        VOUserDetails user = createTestUser();
        throwMailOperationFailed = true;
        idMgmt.createUser(user, new ArrayList<UserRoleType>(), MP_ID);
    }

    @Test
    public void testGetUsersForOrganizationTwoUsers() throws Exception {
        VOUserDetails user = createTestUser();
        idMgmt.createUser(user, Collections.<UserRoleType> emptyList(), MP_ID);

        List<VOUserDetails> users = idMgmt.getUsersForOrganization();
        Assert.assertEquals(2, users.size());
    }

    @Test
    public void testDeletePlatformUser() throws Exception {
        VOUserDetails user = createTestUser();
        idMgmt.createUser(user, Collections.<UserRoleType> emptyList(), MP_ID);
        List<VOUserDetails> users = idMgmt.getUsersForOrganization();
        for (VOUserDetails userDetails : users) {
            String userId = userDetails.getUserId();
            if (userId != null && userId.equals(user.getUserId())) {
                user = userDetails;
            }
        }
        idMgmt.deleteUser(user, null);

        boolean userDeleted = true;
        users = idMgmt.getUsersForOrganization();
        for (VOUser userDetails : users) {
            String userId = userDetails.getUserId();
            if (userId != null && userId.equals(user.getUserId())) {
                userDeleted = false;
            }
        }

        Assert.assertTrue(userDeleted);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                PlatformUser user = mgr.getCurrentUser();
                idMgmtLocal.deletePlatformUser(user, null);
                verify(reviewServiceMock).deleteReviewsOfUser(user, false);
                user = mgr.find(PlatformUser.class, user.getKey());
                Assert.assertNull(user);
                return null;
            }
        });
    }

    @Test
    public void testDeletePlatformUserWithExistingRoles() throws Exception {
        final VOUserDetails user = createTestUser();

        List<UserRoleType> roles = new LinkedList<>();
        roles.add(UserRoleType.ORGANIZATION_ADMIN);

        idMgmt.createUser(user, roles, MP_ID);

        VOUserDetails user2 = null;
        List<VOUserDetails> users = idMgmt.getUsersForOrganization();
        for (VOUserDetails userDetails : users) {
            String userId = userDetails.getUserId();
            if (userId != null && userId.equals(user.getUserId())) {
                user2 = userDetails;
            }
        }
        assertNotNull(user2);
        assertEquals(user.getUserId(), user2.getUserId());

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                PlatformUser pUser = new PlatformUser();
                pUser.setUserId(user.getUserId());

                PlatformUser dbUser = (PlatformUser) mgr
                        .getReferenceByBusinessKey(pUser);
                assertEquals(user.getUserId(), dbUser.getUserId());

                List<RoleAssignment> userRoles = getRoleAssignmentByUserKey(
                        dbUser.getKey());
                assertEquals(1, userRoles.size());

                idMgmtLocal.deletePlatformUser(dbUser, null);
                mgr.flush();

                userRoles = getRoleAssignmentByUserKey(dbUser.getKey());
                assertEquals(0, userRoles.size());

                return null;
            }
        });
    }

    @Test(expected = UserDeletionConstraintException.class)
    public void testDeletePlatformUserLastAdmin() throws Exception {
        VOUserDetails details = new VOUserDetails();
        details.setOrganizationId(ORGANIZATION_ID);
        details.setUserId(USER_ID_EXISTING);
        details = retrieveUser(details);

        // as it's the only user and thus also last admin of the subscription,
        // delete it
        idMgmt.deleteUser(details, null);
    }

    @Test
    public void testDeletePlatformUserSendEmail() throws Exception {
        VOUserDetails user = createTestUser();
        idMgmt.createUser(user, Collections.<UserRoleType> emptyList(), MP_ID);
        user = retrieveUser(user);

        int mailCount = mailCounter;
        idMgmt.deleteUser(user, null);
        Assert.assertEquals(mailCount + 1, mailCounter);
        Assert.assertEquals(EmailType.USER_DELETED, mailType);
    }

    @Test
    public void testDeletePlatformUserNoEmailSendAfterException()
            throws Exception {
        VOUserDetails details = new VOUserDetails();
        details.setOrganizationId(ORGANIZATION_ID);
        details.setUserId(USER_ID_EXISTING);
        details = retrieveUser(details);

        // as it's the only user and thus also last admin of the subscription,
        // delete it
        int mailCount = mailCounter;
        try {
            idMgmt.deleteUser(details, null);
            Assert.fail("Last admin user for organization must not be removed");
        } catch (UserDeletionConstraintException e) {
            Assert.assertEquals(mailCount, mailCounter); // no mail must be send
        }
    }

    @Test
    public void testDeletePlatformUserSendEmailError() throws Exception {
        VOUserDetails user = createTestUser();
        idMgmt.createUser(user, Collections.<UserRoleType> emptyList(), MP_ID);
        user = retrieveUser(user);

        throwMailOperationFailed = true;
        int mailCount = mailCounter;
        idMgmt.deleteUser(user, null);
        Assert.assertEquals(mailCount + 1, mailCounter);
        Assert.assertNull(retrieveUser(user));
    }

    @Test(expected = UserDeletionConstraintException.class)
    public void testDeletePlatformUserHasActiveSubscription() throws Exception {
        VOUserDetails user = createTestUser();
        setupSubscriptionServiceToReturnUsageLicense();
        idMgmt.createUser(user,
                Collections.singletonList(UserRoleType.ORGANIZATION_ADMIN),
                MP_ID);
        user = retrieveUser(user);

        idMgmt.deleteUser(user, null);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testConcurrentDeletePlatformUser() throws Exception {
        VOUserDetails user = createTestUser();
        idMgmt.createUser(user, Collections.<UserRoleType> emptyList(), MP_ID);
        List<VOUserDetails> users = idMgmt.getUsersForOrganization();
        for (VOUserDetails userDetails : users) {
            String userId = userDetails.getUserId();
            if (userId != null && userId.equals(user.getUserId())) {
                user = userDetails;
            }
        }

        user.setAddress("another address");
        idMgmt.updateUser(user);
        idMgmt.deleteUser(user, null);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void testDeleteOtherPlatformUserNotPermitted() throws Exception {
        VOUser other_user = new VOUser();
        other_user.setKey(Long.parseLong(userKey_2));
        idMgmt.deleteUser(other_user, null);
    }

    @Test
    public void testUpdateUser() throws Exception {
        setupSubscriptionServiceToReturnUsageLicense();
        messagesOfTaskQueue = new ArrayList<>();
        List<VOUserDetails> users = idMgmt.getUsersForOrganization();
        VOUserDetails user = users.get(0);
        String oldValue = user.getEMail();
        user.setEMail("new_" + oldValue);
        user.setUserId("withSubscription");

        VOUserDetails temp = idMgmt.updateUser(user);
        Assert.assertEquals(user.getEMail(), temp.getEMail());
        Assert.assertEquals(user.getUserId(), temp.getUserId());

        Assert.assertEquals(1, messagesOfTaskQueue.size());
        TaskMessage message = messagesOfTaskQueue.get(0);
        Assert.assertEquals(UpdateUserHandler.class, message.getHandlerClass());
        UpdateUserPayload payload = (UpdateUserPayload) message.getPayload();
        Assert.assertEquals(11111, payload.getSubscriptionKey());
        Assert.assertEquals(22222, payload.getUsageLicenseKey());
        Assert.assertEquals(0, message.getExecTime());
        Assert.assertEquals(0, message.getRetry());
        Assert.assertEquals(0, message.getNumberOfAttempt());

        users = idMgmt.getUsersForOrganization();
        VOUserDetails userDetails = users.get(0);
        Assert.assertEquals(user.getEMail(), userDetails.getEMail());
        Assert.assertEquals(user.getUserId(), userDetails.getUserId());
    }

    @Test(expected = NonUniqueBusinessKeyException.class)
    public void testUpdateUserExistingId() throws Exception {
        List<VOUserDetails> users = idMgmt.getUsersForOrganization();
        VOUserDetails user = users.get(0);
        VOUserDetails other = createUserAndReRetrieveIt();
        user.setUserId(other.getUserId());
        idMgmt.updateUser(user);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void testUpdateOtherUserAccountNotPermitted() throws Exception {
        VOUserDetails other_user = new VOUserDetails();
        other_user.setKey(Long.parseLong(userKey_2));
        idMgmt.updateUser(other_user);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testConcurrentUpdateUser() throws Exception {
        messagesOfTaskQueue = new ArrayList<>();
        List<VOUserDetails> users = idMgmt.getUsersForOrganization();
        VOUserDetails user = users.get(0);
        user.setEMail("new_" + user.getEMail());
        idMgmt.updateUser(user);
        idMgmt.updateUser(user);
    }

    /**
     * Checks the successful execution.
     */
    @Test
    public void testCreateOrganizationAdmin_success() throws Exception {
        final VOUserDetails user = createTestUser();
        int mailCnt = mailCounter;

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Organization org = mgr.getReference(Organization.class,
                        organization.getKey());
                Marketplace mp = new Marketplace();
                mp.setMarketplaceId(MP_ID);
                idMgmtLocal.createOrganizationAdmin(user, org, "newPassword",
                        Long.valueOf(12345),
                        (Marketplace) mgr.getReferenceByBusinessKey(mp));

                return null;
            }
        });

        Assert.assertEquals(mailCnt + 1, mailCounter);

        Assert.assertNotNull(encodedParam);

        // b7856: check if the dummy postfix exist
        Assert.assertTrue(encodedParam.endsWith("&et"));
        encodedParam = encodedParam.substring(0, encodedParam.indexOf("&et"));

        String[] decodedParam = ParameterEncoder.decodeParameters(encodedParam);
        Assert.assertEquals(4, decodedParam.length);
        Assert.assertEquals(ORGANIZATION_ID, decodedParam[0]);
        Assert.assertEquals(ORGANIZATION_ID + "usera", decodedParam[1]);
        Assert.assertEquals(MP_ID, decodedParam[2]);
        Assert.assertEquals("12345", decodedParam[3]);

        VOUserDetails resultUser = retrieveUser(user);

        Assert.assertTrue(resultUser.hasAdminRole());
        Assert.assertEquals(UserAccountStatus.LOCKED_NOT_CONFIRMED,
                resultUser.getStatus());
    }

    /**
     * Checks the behavior in case the mail could not be sent.
     */
    @Test(expected = MailOperationException.class)
    public void testCreateOrganizationAdmin_mailSendingFails()
            throws Exception {
        final VOUserDetails user = createTestUser();
        exceptionCausingEmailType = EmailType.USER_CONFIRM;

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Organization org = mgr.getReference(Organization.class,
                        organization.getKey());
                Marketplace mp = new Marketplace();
                mp.setMarketplaceId(MP_ID);
                idMgmtLocal.createOrganizationAdmin(user, org, "newPassword",
                        Long.valueOf(12345),
                        (Marketplace) mgr.getReferenceByBusinessKey(mp));

                return null;
            }
        });
    }

    /**
     * Checks method call with passed orgId=null
     */
    @Test(expected = EJBException.class)
    public void testCreateOrganizationAdmin_NoOrgId() throws Exception {
        VOUserDetails user = createTestUser();
        idMgmtLocal.createOrganizationAdmin(user, null, "newPassword",
                Long.valueOf(12345), null);
    }

    /**
     * Checks the method call with passed user = null
     */
    @Test(expected = EJBException.class)
    public void testCreateOrganizationAdmin_user() throws Exception {
        idMgmtLocal.createOrganizationAdmin(null, organization, "newPassword",
                Long.valueOf(12345), null);
    }

    /**
     * Checks the method call with serviceid = null
     */
    @Test
    public void testCreateOrganizationAdmin_NoServiceId() throws Exception {
        final VOUserDetails user = createTestUser();
        // Should work as well
        int mailCnt = mailCounter;

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Organization org = mgr.getReference(Organization.class,
                        organization.getKey());
                Marketplace mp = new Marketplace();
                mp.setMarketplaceId(MP_ID);
                idMgmtLocal.createOrganizationAdmin(user, org, "newPassword",
                        null, (Marketplace) mgr.getReferenceByBusinessKey(mp));
                return null;
            }
        });

        Assert.assertEquals(mailCnt + 1, mailCounter);

        Assert.assertNotNull(encodedParam);

        // b7856: check if the dummy postfix exist
        Assert.assertTrue(encodedParam.endsWith("&et"));
        encodedParam = encodedParam.substring(0, encodedParam.indexOf("&et"));

        String[] decodedParam = ParameterEncoder.decodeParameters(encodedParam);
        Assert.assertEquals(3, decodedParam.length);
        Assert.assertEquals(ORGANIZATION_ID, decodedParam[0]);
        Assert.assertEquals(ORGANIZATION_ID + "usera", decodedParam[1]);
        Assert.assertEquals(MP_ID, decodedParam[2]);
        // no service ID!

        VOUserDetails resultUser = retrieveUser(user);

        Assert.assertTrue(resultUser.hasAdminRole());
        Assert.assertEquals(UserAccountStatus.LOCKED_NOT_CONFIRMED,
                resultUser.getStatus());
    }

    /**
     * Checks the call with passed password = null
     */
    @Test
    public void testCreateOrganizationAdmin_NoPasswd() throws Exception {
        final VOUserDetails user = createTestUser();
        // Should work as well (passwd should be auto created and the user needs
        // to
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Organization org = mgr.getReference(Organization.class,
                        organization.getKey());
                Marketplace mp = new Marketplace();
                mp.setMarketplaceId(MP_ID);
                idMgmtLocal.createOrganizationAdmin(user, org, null,
                        Long.valueOf(12345),
                        (Marketplace) mgr.getReferenceByBusinessKey(mp));
                return null;
            }
        });

        VOUserDetails resultUser = retrieveUser(user);
        Assert.assertTrue(resultUser.hasAdminRole());
        Assert.assertEquals(UserAccountStatus.PASSWORD_MUST_BE_CHANGED,
                resultUser.getStatus());
    }

    /**
     * Checks the method call with password = null and service id = null
     */
    @Test
    public void testCreateOrganizationAdmin_NoPasswdNoServiceId()
            throws Exception {
        final VOUserDetails user = createTestUser();
        // Should work as well (passwd should be auto created and the user needs
        // to)
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Organization org = mgr.getReference(Organization.class,
                        organization.getKey());
                Marketplace mp = new Marketplace();
                mp.setMarketplaceId(MP_ID);
                idMgmtLocal.createOrganizationAdmin(user, org, null, null,
                        (Marketplace) mgr.getReferenceByBusinessKey(mp));
                return null;
            }
        });

        VOUserDetails resultUser = retrieveUser(user);

        Assert.assertTrue(resultUser.hasAdminRole());
        Assert.assertEquals(UserAccountStatus.PASSWORD_MUST_BE_CHANGED,
                resultUser.getStatus());
    }

    @Test
    public void testConfirmAccount() throws Exception {
        final VOUserDetails user = createTestUser();

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Organization org = mgr.getReference(Organization.class,
                        organization.getKey());
                Marketplace mp = new Marketplace();
                mp.setMarketplaceId(MP_ID);
                idMgmtLocal.createOrganizationAdmin(user, org, "newPassword",
                        Long.valueOf(12345),
                        (Marketplace) mgr.getReferenceByBusinessKey(mp));
                return null;
            }
        });

        VOUserDetails savedUser = retrieveUser(user);
        idMgmt.confirmAccount(savedUser, null);

        VOUserDetails resultUser = retrieveUser(savedUser);

        Assert.assertTrue(resultUser.hasAdminRole());
        Assert.assertEquals(UserAccountStatus.ACTIVE, resultUser.getStatus());
    }

    @Test
    public void testcreateOrganizationAdmin_userRoles() throws Exception {
        final VOUserDetails user = createTestUser();
        user.addUserRole(UserRoleType.SERVICE_MANAGER);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Organization org = mgr.getReference(Organization.class,
                        organization.getKey());
                Marketplace mp = new Marketplace();
                mp.setMarketplaceId(MP_ID);
                idMgmtLocal.createOrganizationAdmin(user, org, "newPassword",
                        Long.valueOf(12345),
                        (Marketplace) mgr.getReferenceByBusinessKey(mp));
                return null;
            }
        });

        VOUserDetails savedUser = retrieveUser(user);
        assertTrue(savedUser.getUserRoles()
                .contains(UserRoleType.ORGANIZATION_ADMIN));
        assertTrue(savedUser.getUserRoles()
                .contains(UserRoleType.SERVICE_MANAGER));

    }

    @Test
    public void testcreateOrganizationAdmin_grantOrganizationAdminRole()
            throws Exception {
        final VOUserDetails user = createTestUser();
        user.addUserRole(UserRoleType.SERVICE_MANAGER);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Organization org = mgr.getReference(Organization.class,
                        organization.getKey());
                Marketplace mp = new Marketplace();
                mp.setMarketplaceId(MP_ID);
                idMgmtLocal.createOrganizationAdmin(user, org, "newPassword",
                        Long.valueOf(12345),
                        (Marketplace) mgr.getReferenceByBusinessKey(mp));
                return null;
            }
        });

        VOUserDetails savedUser = retrieveUser(user);
        assertTrue(savedUser.getUserRoles()
                .contains(UserRoleType.ORGANIZATION_ADMIN));
        assertTrue(savedUser.getUserRoles()
                .contains(UserRoleType.SERVICE_MANAGER));

    }

    @Test
    public void testcreateOrganizationAdmin_containsOrganizationAdminRole()
            throws Exception {
        final VOUserDetails user = createTestUser();
        user.addUserRole(UserRoleType.SERVICE_MANAGER);
        user.addUserRole(UserRoleType.ORGANIZATION_ADMIN);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Organization org = mgr.getReference(Organization.class,
                        organization.getKey());
                Marketplace mp = new Marketplace();
                mp.setMarketplaceId(MP_ID);
                idMgmtLocal.createOrganizationAdmin(user, org, "newPassword",
                        Long.valueOf(12345),
                        (Marketplace) mgr.getReferenceByBusinessKey(mp));
                return null;
            }
        });

        VOUserDetails savedUser = retrieveUser(user);
        assertTrue(savedUser.getUserRoles()
                .contains(UserRoleType.ORGANIZATION_ADMIN));
        assertTrue(savedUser.getUserRoles()
                .contains(UserRoleType.SERVICE_MANAGER));

    }

    @Test
    public void testConfirmAccount_WithMarketplace() throws Exception {
        final Marketplace marketplace = new Marketplace();
        marketplace.setMarketplaceId(MP_ID);
        final VOUserDetails user = createTestUser();

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Organization org = mgr.getReference(Organization.class,
                        organization.getKey());
                idMgmtLocal.createOrganizationAdmin(user, org, "newPassword",
                        Long.valueOf(12345), marketplace);
                return null;
            }
        });

        VOUserDetails savedUser = retrieveUser(user);
        idMgmt.confirmAccount(savedUser, MP_ID);
        Assert.assertTrue(
                Arrays.toString(receivedParams).contains("?mId=" + MP_ID));

        VOUserDetails resultUser = retrieveUser(savedUser);

        Assert.assertTrue(resultUser.hasAdminRole());
        Assert.assertEquals(UserAccountStatus.ACTIVE, resultUser.getStatus());
    }

    @Test(expected = MailOperationException.class)
    public void testConfirmAccountMailOperationFailed() throws Exception {
        final VOUserDetails user = createTestUser();
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Organization org = mgr.getReference(Organization.class,
                        organization.getKey());
                Marketplace mp = new Marketplace();
                mp.setMarketplaceId(MP_ID);
                idMgmtLocal.createOrganizationAdmin(user, org, "newPassword",
                        Long.valueOf(12345),
                        (Marketplace) mgr.getReferenceByBusinessKey(mp));
                return null;
            }
        });

        VOUserDetails storedUser = retrieveUser(user);

        throwMailOperationFailed = true;
        idMgmt.confirmAccount(storedUser, null);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void testConfirmAccountMailWrongStatus() throws Exception {
        final VOUserDetails user = createTestUser();

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Organization org = mgr.getReference(Organization.class,
                        organization.getKey());
                Marketplace mp = new Marketplace();
                mp.setMarketplaceId(MP_ID);
                idMgmtLocal.createOrganizationAdmin(user, org, null, null,
                        (Marketplace) mgr.getReferenceByBusinessKey(mp));
                return null;
            }
        });

        VOUserDetails savedUser = retrieveUser(user);
        final long userKey = savedUser.getKey();
        user.setKey(userKey);

        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                PlatformUser user = mgr.find(PlatformUser.class, userKey);
                user.setStatus(UserAccountStatus.LOCKED);
                return null;
            }
        });
        idMgmt.confirmAccount(user, null);
    }

    @Test
    public void testCreateUserRoleRelationCustAdmin() throws Exception {
        final VOUser user = createUserAndReRetrieveIt();
        idMgmt.grantUserRoles(user,
                Collections.singletonList(UserRoleType.ORGANIZATION_ADMIN));

        PlatformUser resultUser = runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() {
                PlatformUser resultUser = mgr.find(PlatformUser.class,
                        user.getKey());
                load(resultUser);
                return resultUser;
            }
        });

        Assert.assertTrue(resultUser.isOrganizationAdmin());

    }

    @Test
    public void testGetUserRolesForAdmin() throws Exception {
        final VOUser user = createUserAndReRetrieveIt();
        idMgmt.grantUserRoles(user,
                Collections.singletonList(UserRoleType.ORGANIZATION_ADMIN));
        List<UserRoleType> avRoles = idMgmt.getAvailableUserRoles(user);
        Assert.assertEquals("only admin role and subMgr possible", 2,
                avRoles.size());
        Assert.assertTrue(avRoles.contains(UserRoleType.ORGANIZATION_ADMIN));
        Assert.assertTrue(avRoles.contains(UserRoleType.SUBSCRIPTION_MANAGER));
    }

    @Test
    public void testGetUserRolesForPlatformOperator() throws Exception {

        container.login(this.operatorKey, ROLE_ORGANIZATION_ADMIN);

        VOUserDetails oUser = new VOUserDetails();
        oUser.setUserId(USER_ID_EXISTING_OP_ADMIN);
        oUser.setOrganizationId(po.getOrganizationId());

        final VOUser voUser = idMgmt.getUser(oUser);

        idMgmt.grantUserRoles(voUser,
                Collections.singletonList(UserRoleType.PLATFORM_OPERATOR));

        List<UserRoleType> avRoles = idMgmt.getAvailableUserRoles(voUser);
        Assert.assertEquals("Wrong roles for operator", 6, avRoles.size());
        Assert.assertTrue(avRoles.contains(UserRoleType.PLATFORM_OPERATOR));
        Assert.assertTrue(avRoles.contains(UserRoleType.ORGANIZATION_ADMIN));
        Assert.assertTrue(avRoles.contains(UserRoleType.MARKETPLACE_OWNER));
        Assert.assertTrue(avRoles.contains(UserRoleType.SUBSCRIPTION_MANAGER));
    }

    @Test
    public void testGetUserRolesForSupplier() throws Exception {
        // Create supplier for later registration
        final String supplier_orgId = "mySupplierOrgId";
        String pt_userKey = createUserForOrganizationRole(supplier_orgId,
                OrganizationRoleType.SUPPLIER);
        container.login(pt_userKey, ROLE_ORGANIZATION_ADMIN);

        VOUserDetails user = new VOUserDetails();
        user.setUserId(USER_ID_EXISTING_SP);
        user.setOrganizationId(supplier_orgId);

        final VOUser voUser = idMgmt.getUser(user);

        idMgmt.grantUserRoles(voUser,
                Collections.singletonList(UserRoleType.SERVICE_MANAGER));

        List<UserRoleType> avRoles = idMgmt.getAvailableUserRoles(voUser);
        Assert.assertEquals("only roles service manager and admin are possible",
                3, avRoles.size());
        Assert.assertTrue(avRoles.contains(UserRoleType.SERVICE_MANAGER));
        Assert.assertTrue(avRoles.contains(UserRoleType.ORGANIZATION_ADMIN));
        Assert.assertTrue(avRoles.contains(UserRoleType.SUBSCRIPTION_MANAGER));
    }

    @Test
    public void testGetUserRolesForUser() throws Exception {
        // Create supplier for later registration
        final String supplier_orgId = "mySupplierOrgId";
        String pt_userKey = createUserForOrganizationRole(supplier_orgId,
                OrganizationRoleType.CUSTOMER);
        container.login(pt_userKey, ROLE_ORGANIZATION_ADMIN);

        VOUserDetails user = new VOUserDetails();
        user.setUserId(USER_ID_EXISTING_SP);
        user.setOrganizationId(supplier_orgId);

        final VOUser voUser = idMgmt.getUser(user);

        List<UserRoleType> avRoles = idMgmt.getAvailableUserRoles(voUser);
        Assert.assertEquals("only role admin is possible", 2, avRoles.size());
        Assert.assertTrue(avRoles.contains(UserRoleType.ORGANIZATION_ADMIN));
        Assert.assertTrue(avRoles.contains(UserRoleType.SUBSCRIPTION_MANAGER));
    }

    @Test
    public void testGetUserRolesForTechProvider() throws Exception {
        // Create techology provider for later registration
        final String supplier_orgId = "myTechServOrgId";
        String pt_userKey = createUserForOrganizationRole(supplier_orgId,
                OrganizationRoleType.TECHNOLOGY_PROVIDER);
        container.login(pt_userKey, ROLE_ORGANIZATION_ADMIN);

        VOUserDetails user = new VOUserDetails();
        user.setUserId(USER_ID_EXISTING_SP);
        user.setOrganizationId(supplier_orgId);

        final VOUser voUser = idMgmt.getUser(user);

        idMgmt.grantUserRoles(voUser,
                Collections.singletonList(UserRoleType.TECHNOLOGY_MANAGER));

        List<UserRoleType> avRoles = idMgmt.getAvailableUserRoles(voUser);
        Assert.assertEquals("only roles tech manager and admin are possible", 3,
                avRoles.size());
        Assert.assertTrue(avRoles.contains(UserRoleType.TECHNOLOGY_MANAGER));
        Assert.assertTrue(avRoles.contains(UserRoleType.ORGANIZATION_ADMIN));
        Assert.assertTrue(avRoles.contains(UserRoleType.SUBSCRIPTION_MANAGER));
    }

    @Test
    public void testGetUserRolesForBroker() throws Exception {
        // Create techology provider for later registration
        final String supplier_orgId = "myTechServOrgId";
        String pt_userKey = createUserForOrganizationRole(supplier_orgId,
                OrganizationRoleType.BROKER);
        container.login(pt_userKey, ROLE_ORGANIZATION_ADMIN);

        VOUserDetails user = new VOUserDetails();
        user.setUserId(USER_ID_EXISTING_SP);
        user.setOrganizationId(supplier_orgId);

        final VOUser voUser = idMgmt.getUser(user);

        idMgmt.grantUserRoles(voUser,
                Collections.singletonList(UserRoleType.BROKER_MANAGER));

        List<UserRoleType> avRoles = idMgmt.getAvailableUserRoles(voUser);
        Assert.assertEquals("only roles broker and admin are possible", 3,
                avRoles.size());
        Assert.assertTrue(avRoles.contains(UserRoleType.BROKER_MANAGER));
        Assert.assertTrue(avRoles.contains(UserRoleType.ORGANIZATION_ADMIN));
        Assert.assertTrue(avRoles.contains(UserRoleType.SUBSCRIPTION_MANAGER));

    }

    @Test
    public void testGetUserRolesForReseller() throws Exception {
        // Create techology provider for later registration
        final String supplier_orgId = "myTechServOrgId";
        String pt_userKey = createUserForOrganizationRole(supplier_orgId,
                OrganizationRoleType.RESELLER);
        container.login(pt_userKey, ROLE_ORGANIZATION_ADMIN);

        VOUserDetails user = new VOUserDetails();
        user.setUserId(USER_ID_EXISTING_SP);
        user.setOrganizationId(supplier_orgId);

        final VOUser voUser = idMgmt.getUser(user);

        idMgmt.grantUserRoles(voUser,
                Collections.singletonList(UserRoleType.RESELLER_MANAGER));

        List<UserRoleType> avRoles = idMgmt.getAvailableUserRoles(voUser);
        Assert.assertEquals("only roles reseller and admin are possible", 3,
                avRoles.size());
        Assert.assertTrue(avRoles.contains(UserRoleType.RESELLER_MANAGER));
        Assert.assertTrue(avRoles.contains(UserRoleType.ORGANIZATION_ADMIN));
        Assert.assertTrue(avRoles.contains(UserRoleType.SUBSCRIPTION_MANAGER));

    }

    @Test
    public void testGetUserRolesForTechProviderSupplier() throws Exception {
        // Create techology provider & supplier for later registration
        final String supplier_orgId = "mySupplierTechServOrgId";
        String pt_userKey = createUserForOrganizationRole(supplier_orgId,
                OrganizationRoleType.SUPPLIER,
                OrganizationRoleType.TECHNOLOGY_PROVIDER);
        container.login(pt_userKey, ROLE_ORGANIZATION_ADMIN);

        VOUserDetails user = new VOUserDetails();
        user.setUserId(USER_ID_EXISTING_SP);
        user.setOrganizationId(supplier_orgId);

        final VOUser voUser = idMgmt.getUser(user);

        List<UserRoleType> roleList = new ArrayList<>();
        roleList.add(UserRoleType.TECHNOLOGY_MANAGER);
        roleList.add(UserRoleType.SERVICE_MANAGER);
        idMgmt.grantUserRoles(voUser, roleList);

        List<UserRoleType> avRoles = idMgmt.getAvailableUserRoles(voUser);
        Assert.assertEquals(
                "only roles service, tech manager and admin are possible", 4,
                avRoles.size());
        Assert.assertTrue(avRoles.contains(UserRoleType.SERVICE_MANAGER));
        Assert.assertTrue(avRoles.contains(UserRoleType.TECHNOLOGY_MANAGER));
        Assert.assertTrue(avRoles.contains(UserRoleType.ORGANIZATION_ADMIN));
        Assert.assertTrue(avRoles.contains(UserRoleType.SUBSCRIPTION_MANAGER));
    }

    @Test
    public void testAssignRevokeServiceManagerRoleForSupplier()
            throws Exception {
        // Create supplier for later registration
        final String supplier_orgId = "mySupplierOrgId";
        String pt_userKey = createUserForOrganizationRole(supplier_orgId,
                OrganizationRoleType.SUPPLIER);
        container.login(pt_userKey, ROLE_ORGANIZATION_ADMIN);

        VOUserDetails user = new VOUserDetails();
        user.setUserId(USER_ID_EXISTING_SP);
        user.setOrganizationId(supplier_orgId);

        final VOUser voUser = idMgmt.getUser(user);

        idMgmt.grantUserRoles(voUser,
                Collections.singletonList(UserRoleType.SERVICE_MANAGER));

        final PlatformUser pUser = new PlatformUser();
        pUser.setUserId(voUser.getUserId());
        PlatformUser resultUser = runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() {
                PlatformUser resultUser = mgr.find(PlatformUser.class,
                        voUser.getKey());
                load(resultUser);
                return resultUser;
            }
        });
        Assert.assertTrue(resultUser.hasRole(UserRoleType.SERVICE_MANAGER));
        // making revoke
        idMgmt.revokeUserRoles(voUser,
                Collections.singletonList(UserRoleType.SERVICE_MANAGER));

        resultUser = runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() {
                PlatformUser resultUser = mgr.find(PlatformUser.class,
                        voUser.getKey());
                load(resultUser);
                return resultUser;
            }
        });

        Assert.assertFalse(resultUser.hasRole(UserRoleType.SERVICE_MANAGER));

    }

    @Test
    public void testAssignRevokeTechServiceManagerRoleForTechProvider()
            throws Exception {
        // Create techology provider for later registration
        final String supplier_orgId = "myTechServOrgId";
        String pt_userKey = createUserForOrganizationRole(supplier_orgId,
                OrganizationRoleType.TECHNOLOGY_PROVIDER);
        container.login(pt_userKey, ROLE_ORGANIZATION_ADMIN);

        VOUserDetails user = new VOUserDetails();
        user.setUserId(USER_ID_EXISTING_SP);
        user.setOrganizationId(supplier_orgId);

        final VOUser voUser = idMgmt.getUser(user);

        idMgmt.grantUserRoles(voUser,
                Collections.singletonList(UserRoleType.TECHNOLOGY_MANAGER));

        final PlatformUser pUser = new PlatformUser();
        pUser.setUserId(voUser.getUserId());
        PlatformUser resultUser = runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() {
                PlatformUser resultUser = mgr.find(PlatformUser.class,
                        voUser.getKey());
                load(resultUser);
                return resultUser;
            }
        });

        Assert.assertTrue(resultUser.hasRole(UserRoleType.TECHNOLOGY_MANAGER));

        // making revoke

        idMgmt.revokeUserRoles(voUser,
                Collections.singletonList(UserRoleType.TECHNOLOGY_MANAGER));

        pUser.setUserId(voUser.getUserId());
        resultUser = runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() {
                PlatformUser resultUser = mgr.find(PlatformUser.class,
                        voUser.getKey());
                load(resultUser);
                return resultUser;
            }
        });

        Assert.assertFalse(resultUser.hasRole(UserRoleType.TECHNOLOGY_MANAGER));

    }

    @Test(expected = UserRoleAssignmentException.class)
    public void testAssignServiceManagerRoleForTechProvider() throws Exception {
        // Create supplier for later registration
        final String supplier_orgId = "mySupplierOrgId";
        String pt_userKey = createUserForOrganizationRole(supplier_orgId,
                OrganizationRoleType.SUPPLIER);
        container.login(pt_userKey, ROLE_ORGANIZATION_ADMIN);

        VOUserDetails user = new VOUserDetails();
        user.setUserId(USER_ID_EXISTING_SP);
        user.setOrganizationId(supplier_orgId);

        final VOUser voUser = idMgmt.getUser(user);

        idMgmt.grantUserRoles(voUser,
                Collections.singletonList(UserRoleType.TECHNOLOGY_MANAGER));

    }

    @Test(expected = UserRoleAssignmentException.class)
    public void testAssignOperatorRoleForTechProvider() throws Exception {
        // Create supplier for later registration
        final String supplier_orgId = "mySupplierOrgId";
        String pt_userKey = createUserForOrganizationRole(supplier_orgId,
                OrganizationRoleType.SUPPLIER);
        container.login(pt_userKey, ROLE_ORGANIZATION_ADMIN);

        VOUserDetails user = new VOUserDetails();
        user.setUserId(USER_ID_EXISTING_SP);
        user.setOrganizationId(supplier_orgId);

        final VOUser voUser = idMgmt.getUser(user);

        idMgmt.grantUserRoles(voUser,
                Collections.singletonList(UserRoleType.PLATFORM_OPERATOR));

    }

    @Test(expected = UserRoleAssignmentException.class)
    public void testAssignServiceManagerRoleForSupplier() throws Exception {
        // Create techology provider for later registration
        final String supplier_orgId = "myTechServOrgId";
        String pt_userKey = createUserForOrganizationRole(supplier_orgId,
                OrganizationRoleType.TECHNOLOGY_PROVIDER);
        container.login(pt_userKey, ROLE_ORGANIZATION_ADMIN);

        VOUserDetails user = new VOUserDetails();
        user.setUserId(USER_ID_EXISTING_SP);
        user.setOrganizationId(supplier_orgId);

        final VOUser voUser = idMgmt.getUser(user);

        idMgmt.grantUserRoles(voUser,
                Collections.singletonList(UserRoleType.SERVICE_MANAGER));

    }

    /*
     * Test-Objective: assign marketplace owner user role to supplier and
     * marketplace owner organization role is allowed
     */
    @Test
    public void testAssignMarketplaceOwnerRoleForSupplierWithMp()
            throws Exception {
        // Create technology provider for later registration
        final String supplier_orgId = "myTechServOrgId";
        String pt_userKey = createUserForOrganizationRole(supplier_orgId,
                OrganizationRoleType.SUPPLIER,
                OrganizationRoleType.MARKETPLACE_OWNER);
        container.login(pt_userKey, ROLE_ORGANIZATION_ADMIN);

        VOUserDetails user = new VOUserDetails();
        user.setUserId(USER_ID_EXISTING_SP);
        user.setOrganizationId(supplier_orgId);

        final VOUser voUser = idMgmt.getUser(user);

        idMgmt.grantUserRoles(voUser,
                Collections.singletonList(UserRoleType.MARKETPLACE_OWNER));

        PlatformUser resultUser = runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() {
                PlatformUser resultUser = mgr.find(PlatformUser.class,
                        voUser.getKey());
                load(resultUser);
                return resultUser;
            }
        });
        Assert.assertTrue(resultUser.hasRole(UserRoleType.MARKETPLACE_OWNER));

    }

    /*
     * Test-Objective: ensure that assign marketplace owner role to technology
     * provider is NOT allowed
     */
    @Test(expected = UserRoleAssignmentException.class)
    public void testAssignMarketplaceOwnerRoleForTechProv() throws Exception {
        // Create technology provider for later registration
        final String supplier_orgId = "myTechServOrgId";
        String pt_userKey = createUserForOrganizationRole(supplier_orgId,
                OrganizationRoleType.TECHNOLOGY_PROVIDER);
        container.login(pt_userKey, ROLE_ORGANIZATION_ADMIN);

        VOUserDetails user = new VOUserDetails();
        user.setUserId(USER_ID_EXISTING_SP);
        user.setOrganizationId(supplier_orgId);

        final VOUser voUser = idMgmt.getUser(user);

        idMgmt.grantUserRoles(voUser,
                Collections.singletonList(UserRoleType.MARKETPLACE_OWNER));
        // above statement should raise UserRoleAssignmentException
    }

    @Test
    public void testAssignRevokeManagerRolesForTechProviderOperator()
            throws Exception {

        container.login(operatorKey, ROLE_ORGANIZATION_ADMIN);

        VOUserDetails user = new VOUserDetails();
        user.setUserId(USER_ID_EXISTING_OP_USER);
        user.setOrganizationId(po.getOrganizationId());

        final VOUser voUser = idMgmt.getUser(user);

        List<UserRoleType> roleList = new ArrayList<>();
        roleList.add(UserRoleType.TECHNOLOGY_MANAGER);
        roleList.add(UserRoleType.SERVICE_MANAGER);
        idMgmt.grantUserRoles(voUser, roleList);

        final PlatformUser pUser = new PlatformUser();
        pUser.setUserId(voUser.getUserId());
        PlatformUser resultUser = null;
        resultUser = runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() {
                PlatformUser resultUser = mgr.find(PlatformUser.class,
                        voUser.getKey());
                load(resultUser);
                return resultUser;
            }
        });

        Assert.assertTrue(resultUser.hasRole(UserRoleType.TECHNOLOGY_MANAGER));
        Assert.assertTrue(resultUser.hasRole(UserRoleType.SERVICE_MANAGER));

        // making revoke
        idMgmt.revokeUserRoles(voUser, roleList);

        pUser.setUserId(voUser.getUserId());
        resultUser = runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() {
                PlatformUser resultUser = mgr.find(PlatformUser.class,
                        voUser.getKey());
                load(resultUser);
                return resultUser;
            }
        });

        Assert.assertFalse(resultUser.hasRole(UserRoleType.TECHNOLOGY_MANAGER));
        Assert.assertFalse(resultUser.hasRole(UserRoleType.SERVICE_MANAGER));
    }

    @Test
    public void testSetUserRolesManagerRolesForTechProviderOperator()
            throws Exception {

        container.login(operatorKey, ROLE_ORGANIZATION_ADMIN);

        VOUserDetails user = new VOUserDetails();
        user.setUserId(USER_ID_EXISTING_OP_USER);
        user.setOrganizationId(po.getOrganizationId());

        final VOUser voUser = idMgmt.getUser(user);

        List<UserRoleType> roleList = new ArrayList<>();
        roleList.add(UserRoleType.ORGANIZATION_ADMIN);
        roleList.add(UserRoleType.TECHNOLOGY_MANAGER);
        roleList.add(UserRoleType.SERVICE_MANAGER);
        idMgmt.grantUserRoles(voUser, roleList);

        final PlatformUser pUser = new PlatformUser();
        pUser.setUserId(voUser.getUserId());
        PlatformUser resultUser = null;
        resultUser = runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() {
                PlatformUser resultUser = mgr.find(PlatformUser.class,
                        voUser.getKey());
                load(resultUser);
                return resultUser;
            }
        });

        Assert.assertTrue(resultUser.hasRole(UserRoleType.TECHNOLOGY_MANAGER));
        Assert.assertTrue(resultUser.hasRole(UserRoleType.SERVICE_MANAGER));
        Assert.assertTrue(resultUser.hasRole(UserRoleType.ORGANIZATION_ADMIN));

        idMgmt.setUserRoles(voUser,
                Collections.singletonList(UserRoleType.TECHNOLOGY_MANAGER));

        resultUser = null;
        resultUser = runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() {
                PlatformUser resultUser = mgr.find(PlatformUser.class,
                        voUser.getKey());
                load(resultUser);
                return resultUser;
            }
        });

        Assert.assertTrue(resultUser.hasRole(UserRoleType.TECHNOLOGY_MANAGER));
        Assert.assertFalse(resultUser.hasRole(UserRoleType.SERVICE_MANAGER));
        Assert.assertFalse(resultUser.hasRole(UserRoleType.ORGANIZATION_ADMIN));
    }

    @Test
    public void testSetUserRolesEmptyList() throws Exception {

        container.login(operatorKey, ROLE_ORGANIZATION_ADMIN);

        VOUserDetails user = new VOUserDetails();
        user.setUserId(USER_ID_EXISTING_OP_USER);
        user.setOrganizationId(po.getOrganizationId());

        final VOUser voUser = idMgmt.getUser(user);

        List<UserRoleType> roleList = new ArrayList<>();
        idMgmt.setUserRoles(voUser, roleList);

        final PlatformUser pUser = new PlatformUser();
        pUser.setUserId(voUser.getUserId());
        PlatformUser resultUser = null;
        resultUser = runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() {
                PlatformUser resultUser = mgr.find(PlatformUser.class,
                        voUser.getKey());
                load(resultUser);
                return resultUser;
            }
        });

        Assert.assertTrue(resultUser.getAssignedRoles().isEmpty());

    }

    @Test
    public void testAssignRevokeManagerRolesForTechProviderSupplier()
            throws Exception {
        // Create techology provider & supplier for later registration
        final String supplier_orgId = "mySupplierTechServOrgId";
        String pt_userKey = createUserForOrganizationRole(supplier_orgId,
                OrganizationRoleType.SUPPLIER,
                OrganizationRoleType.TECHNOLOGY_PROVIDER);

        container.login(pt_userKey, ROLE_ORGANIZATION_ADMIN);

        VOUserDetails user = new VOUserDetails();
        user.setUserId(USER_ID_EXISTING_SP);
        user.setOrganizationId(supplier_orgId);

        final VOUser voUser = idMgmt.getUser(user);

        List<UserRoleType> userRoles = new ArrayList<>();
        userRoles.add(UserRoleType.TECHNOLOGY_MANAGER);
        userRoles.add(UserRoleType.SERVICE_MANAGER);
        idMgmt.grantUserRoles(voUser, userRoles);

        final PlatformUser pUser = new PlatformUser();
        pUser.setUserId(voUser.getUserId());
        PlatformUser resultUser = null;
        resultUser = runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() {
                PlatformUser resultUser = mgr.find(PlatformUser.class,
                        voUser.getKey());
                load(resultUser);
                return resultUser;
            }
        });

        Assert.assertTrue(resultUser.hasRole(UserRoleType.TECHNOLOGY_MANAGER));
        Assert.assertTrue(resultUser.hasRole(UserRoleType.SERVICE_MANAGER));

        // making revoke

        idMgmt.revokeUserRoles(voUser, userRoles);

        pUser.setUserId(voUser.getUserId());
        resultUser = runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() {
                PlatformUser resultUser = mgr.find(PlatformUser.class,
                        voUser.getKey());
                load(resultUser);
                return resultUser;
            }
        });

        Assert.assertFalse(resultUser.hasRole(UserRoleType.TECHNOLOGY_MANAGER));
        Assert.assertFalse(resultUser.hasRole(UserRoleType.SERVICE_MANAGER));
    }

    @Test(expected = OperationNotPermittedException.class)
    public void testCreateUserRoleRelationNotPermitted() throws Exception {
        VOUser user = new VOUser();
        user.setUserId(USER_ID_EXISTING_2);
        idMgmt.grantUserRoles(user,
                Collections.singletonList(UserRoleType.ORGANIZATION_ADMIN));
    }

    /**
     * Grant admin role for user that has this role already. No exception must
     * be thrown.
     *
     * @throws Exception
     */
    @Test
    public void testCreateUserRoleRelationTwice() throws Exception {
        final VOUser user = createUserAndReRetrieveIt();
        idMgmt.grantUserRoles(user,
                Collections.singletonList(UserRoleType.ORGANIZATION_ADMIN));
        idMgmt.grantUserRoles(user,
                Collections.singletonList(UserRoleType.ORGANIZATION_ADMIN));

    }

    @Test
    public void testDeleteUserRoleRelationCustAdmin() throws Exception {
        final VOUser user = createUserAndReRetrieveIt();
        idMgmt.grantUserRoles(user,
                Collections.singletonList(UserRoleType.ORGANIZATION_ADMIN));
        idMgmt.revokeUserRoles(user,
                Collections.singletonList(UserRoleType.ORGANIZATION_ADMIN));
        idMgmt.revokeUserRoles(user,
                Collections.singletonList(UserRoleType.ORGANIZATION_ADMIN));

    }

    @Test(expected = OperationNotPermittedException.class)
    public void testDeleteUserRoleRelationNotPermitted() throws Exception {
        VOUser other_user = new VOUser();
        other_user.setKey(Long.parseLong(userKey_2));
        idMgmt.revokeUserRoles(other_user,
                Collections.singletonList(UserRoleType.ORGANIZATION_ADMIN));
    }

    @Test
    public void testDeleteUserRoleRelationCustAdminConcurrently()
            throws Exception {

        final VOUser admin2 = createUserAndReRetrieveIt();
        idMgmt.grantUserRoles(admin2,
                Collections.singletonList(UserRoleType.ORGANIZATION_ADMIN));

        Session admin2Session = new Session();
        sessionList.add(admin2Session);

        try {
            idMgmt.revokeUserRoles(admin2,
                    Collections.singletonList(UserRoleType.ORGANIZATION_ADMIN));
            fail("UserActiveException expected");
        } catch (UserActiveException e) {
            sessionList.clear();
            idMgmt.revokeUserRoles(admin2,
                    Collections.singletonList(UserRoleType.ORGANIZATION_ADMIN));
        }

    }

    @Test
    public void testDeleteUserRoleRelationCustAdminConcurrently_EmptyList()
            throws Exception {

        final VOUser admin2 = createUserAndReRetrieveIt();
        idMgmt.grantUserRoles(admin2,
                Collections.singletonList(UserRoleType.ORGANIZATION_ADMIN));

        Session admin2Session = new Session();
        sessionList.add(admin2Session);

        List<UserRoleType> emptyList = Collections.emptyList();
        idMgmt.revokeUserRoles(admin2, emptyList);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testCreateUserRoleRelationCustAdminForNonExistingUser()
            throws Exception {
        // given
        VOUser userNotPersistent = createTestUser();

        // when
        idMgmt.grantUserRoles(userNotPersistent,
                Collections.singletonList(UserRoleType.ORGANIZATION_ADMIN));
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testDeletePlatformUserNonExisting() throws Exception {
        // given
        VOUser user = createUserAndReRetrieveIt();
        idMgmt.deleteUser(user, null);

        // when
        idMgmt.deleteUser(user, null);
    }

    @Test
    public void testModifyUserDataAsTheUserItself() throws Exception {
        VOUserDetails user = createUserAndReRetrieveIt();
        String oldMail = user.getEMail();
        String newMail = "new_" + oldMail;
        user.setEMail(newMail);

        // user has been created, so use this one for login
        container.login(String.valueOf(user.getKey()), ROLE_ORGANIZATION_ADMIN);
        idMgmt.updateUser(user);

        VOUserDetails updatedUser = retrieveUser(user);
        Assert.assertEquals(newMail, updatedUser.getEMail());
    }

    @Test(expected = OperationNotPermittedException.class)
    public void testModifyUserDataAsNonAdminForOtherUser() throws Exception {
        // given
        VOUserDetails user = createUserAndReRetrieveIt();
        String nonAdminKey = String.valueOf(user.getKey());
        String oldMail = user.getEMail();
        String newMail = "new_" + oldMail;
        user.setEMail(newMail);

        user.setUserId("modificationVictim");
        idMgmt.createUser(user, Collections.<UserRoleType> emptyList(), MP_ID);
        user = retrieveUser(user);

        container.login(nonAdminKey);

        // when
        idMgmt.updateUser(user);
    }

    @Test
    public void testLockUserAccount() throws Exception {
        VOUserDetails user = createUserAndReRetrieveIt();

        idMgmt.lockUserAccount(user, UserAccountStatus.LOCKED, null);

        user = retrieveUser(user);
        Assert.assertEquals(UserAccountStatus.LOCKED, user.getStatus());
    }

    @Test(expected = OperationNotPermittedException.class)
    public void testLockUserAccountWithStatusActive() throws Exception {
        VOUserDetails user = createUserAndReRetrieveIt();

        idMgmt.lockUserAccount(user, UserAccountStatus.ACTIVE, null);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void testLockUserAccountWithStatusLockedFailedLoginAttemps()
            throws Exception {
        VOUserDetails user = createUserAndReRetrieveIt();

        idMgmt.lockUserAccount(user,
                UserAccountStatus.LOCKED_FAILED_LOGIN_ATTEMPTS, null);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void testLockUserAccountWithStatusLocked() throws Exception {
        VOUserDetails user = createUserAndReRetrieveIt();
        try {
            idMgmt.lockUserAccount(user, UserAccountStatus.LOCKED, null);
        } catch (OperationNotPermittedException e) {
            Assert.fail("Locking failed.");
        }

        user = retrieveUser(user);
        idMgmt.lockUserAccount(user,
                UserAccountStatus.LOCKED_FAILED_LOGIN_ATTEMPTS, null);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testConcurrentLockUserAccount() throws Exception {
        VOUserDetails user = createUserAndReRetrieveIt();
        idMgmt.lockUserAccount(user, UserAccountStatus.LOCKED, null);
        idMgmt.lockUserAccount(user, UserAccountStatus.LOCKED, null);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void testLockOtherUserAccountNotPermitted() throws Exception {
        VOUser other_user = new VOUser();
        other_user.setKey(Long.parseLong(userKey_2));
        idMgmt.lockUserAccount(other_user, UserAccountStatus.LOCKED, null);
    }

    @Test
    public void testUnlockUserAccount() throws Exception {
        VOUserDetails user = createUserAndReRetrieveIt();
        idMgmt.lockUserAccount(user, UserAccountStatus.LOCKED, null);

        user = retrieveUser(user);
        idMgmt.unlockUserAccount(user, null);

        user = retrieveUser(user);
        Assert.assertEquals(UserAccountStatus.ACTIVE, user.getStatus());
    }

    @Test(expected = OperationNotPermittedException.class)
    public void testUnlockUserAccountWrongStatus() throws Exception {
        VOUserDetails user = createUserAndReRetrieveIt();
        final long userKey = user.getKey();

        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                PlatformUser user = mgr.find(PlatformUser.class, userKey);
                user.setStatus(UserAccountStatus.PASSWORD_MUST_BE_CHANGED);
                return null;
            }
        });

        idMgmt.unlockUserAccount(user, null);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testConcurrentUnlockUserAccount() throws Exception {
        VOUserDetails user = createUserAndReRetrieveIt();
        idMgmt.lockUserAccount(user, UserAccountStatus.LOCKED, null);

        user = retrieveUser(user);
        idMgmt.unlockUserAccount(user, null);
        idMgmt.unlockUserAccount(user, null);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void testUnlockOtherUserAccountNotPermitted() throws Exception {
        VOUser other_user = new VOUser();
        other_user.setKey(Long.parseLong(userKey_2));
        idMgmt.unlockUserAccount(other_user, null);
    }

    @Test
    public void testSetUserAccountStatusMailOperationFailed() throws Exception {

        container.login(this.operatorKey, ROLE_ORGANIZATION_ADMIN);

        VOUserDetails oUser = new VOUserDetails();
        oUser.setUserId(USER_ID_EXISTING_OP_ADMIN);
        oUser.setOrganizationId(po.getOrganizationId());

        final VOUser voUser = idMgmt.getUser(oUser);

        idMgmt.grantUserRoles(voUser,
                Collections.singletonList(UserRoleType.PLATFORM_OPERATOR));

        container.login(this.operatorKey, ROLE_PLATFORM_OPERATOR);

        throwMailOperationFailed = true;

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {

                PlatformUser user = mgr.find(PlatformUser.class,
                        operator_user_key);
                idMgmtLocal.setUserAccountStatus(user,
                        UserAccountStatus.ACTIVE);

                user = mgr.find(PlatformUser.class, operator_user_key);
                Assert.assertEquals(UserAccountStatus.ACTIVE, user.getStatus());
                return null;
            }
        });

    }

    @Test
    public void testGetPlatformUser() throws Exception {
        VOUserDetails user = createTestUser();
        idMgmt.createUser(user, Collections.<UserRoleType> emptyList(), MP_ID);

        VOUser userDetails = idMgmt.getUserDetails(user);
        Assert.assertEquals(user.getUserId(), userDetails.getUserId());
        Assert.assertEquals(user.getOrganizationId(),
                userDetails.getOrganizationId());
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testGetPlatformUserNoMatch()
            throws ObjectNotFoundException, OperationNotPermittedException {
        VOUser user = createTestUser();
        idMgmt.getUserDetails(user);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void testGetPlatformUserNotPermitted()
            throws ObjectNotFoundException, OperationNotPermittedException {
        VOUser user = new VOUser();
        user.setUserId(USER_ID_EXISTING_2);
        idMgmt.getUserDetails(user);
    }

    @Test
    public void testGetPlatformUserLocal() throws Exception {
        final VOUserDetails user = createTestUser();
        idMgmt.createUser(user, Collections.<UserRoleType> emptyList(), MP_ID);

        PlatformUser userDetails = runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                return idMgmtLocal.getPlatformUser(user.getUserId(), true);
            }
        });

        Assert.assertEquals(user.getUserId(), userDetails.getUserId());
        Assert.assertEquals(user.getOrganizationId(),
                userDetails.getOrganization().getOrganizationId());
        Assert.assertEquals(user.getFirstName(), userDetails.getFirstName());
        Assert.assertEquals(user.getLastName(), userDetails.getLastName());
        Assert.assertEquals(user.getAdditionalName(),
                userDetails.getAdditionalName());
        Assert.assertEquals(user.getSalutation(), userDetails.getSalutation());
        Assert.assertEquals(user.getAddress(), userDetails.getAddress());
        Assert.assertEquals(user.getEMail(), userDetails.getEmail());
        Assert.assertEquals(user.getPhone(), userDetails.getPhone());
        Assert.assertEquals(user.getLocale(), userDetails.getLocale());
    }

    @Test(expected = OperationNotPermittedException.class)
    public void testGetPlatformUserLocalNotPermitted() throws Exception {
        final VOUser user = new VOUser();
        user.setUserId(USER_ID_EXISTING_2);
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                idMgmtLocal.getPlatformUser(user.getUserId(), true);
                return null;
            }
        });

        idMgmt.getUserDetails(user);
    }

    @Test
    public void testGetPlatformUserLocalIgnoreNotPermitted() throws Exception {
        final VOUser user = new VOUser();
        user.setUserId(USER_ID_EXISTING_2);

        PlatformUser userDetails = runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                PlatformUser userDetails = idMgmtLocal
                        .getPlatformUser(user.getUserId(), false);
                load(userDetails.getOrganization());
                return userDetails;
            }
        });

        Assert.assertEquals(USER_ID_EXISTING_2, userDetails.getUserId());
        Assert.assertEquals(ORGANIZATION_ID_2,
                userDetails.getOrganization().getOrganizationId());
        Assert.assertEquals("someMail@somehost.de", userDetails.getEmail());
        Assert.assertEquals(Locale.ENGLISH.toString(), userDetails.getLocale());
    }

    @Test
    public void testNotifyLoginAttemptOneWrong() throws Exception {
        VOUser user = createUserAndReRetrieveIt();
        idMgmt.notifyOnLoginAttempt(user, false);
        PlatformUser pUser = getDOUser(user.getKey());
        Assert.assertEquals(1, pUser.getFailedLoginCounter());
    }

    @Test
    public void testNotifyLoginAttemptWrongAndReset() throws Exception {
        VOUser user = createUserAndReRetrieveIt();
        idMgmt.notifyOnLoginAttempt(user, false);
        idMgmt.notifyOnLoginAttempt(user, true);
        PlatformUser pUser = getDOUser(user.getKey());
        Assert.assertEquals(0, pUser.getFailedLoginCounter());
    }

    @Test
    public void testNotifyLoginAttemptWrongThreeTimes() throws Exception {
        VOUser user = createUserAndReRetrieveIt();
        idMgmt.notifyOnLoginAttempt(user, false);
        idMgmt.notifyOnLoginAttempt(user, false);
        idMgmt.notifyOnLoginAttempt(user, false);
        PlatformUser pUser = getDOUser(user.getKey());
        Assert.assertEquals(3, pUser.getFailedLoginCounter());
        Assert.assertEquals(UserAccountStatus.LOCKED_FAILED_LOGIN_ATTEMPTS,
                pUser.getStatus());
    }

    @Test(expected = SecurityCheckException.class)
    public void testNotifyLoginAttemptWrongThreeTimesReset() throws Exception {
        // given
        VOUser user = createUserAndReRetrieveIt();
        idMgmt.notifyOnLoginAttempt(user, false);
        idMgmt.notifyOnLoginAttempt(user, false);
        idMgmt.notifyOnLoginAttempt(user, false);

        // when
        idMgmt.notifyOnLoginAttempt(user, true);

    }

    @Test
    public void testNotifyLoginAttemptWrongThreeTimesThenUnlock()
            throws Exception {
        VOUserDetails user = createUserAndReRetrieveIt();
        idMgmt.notifyOnLoginAttempt(user, false);
        idMgmt.notifyOnLoginAttempt(user, false);
        idMgmt.notifyOnLoginAttempt(user, false);

        user = retrieveUser(user);
        idMgmt.unlockUserAccount(user, null);
        PlatformUser pUser = getDOUser(user.getKey());
        Assert.assertEquals(UserAccountStatus.ACTIVE, pUser.getStatus());
    }

    @Test
    public void testNotifyLoginAttemptWrongThreeTimesThenRequestNewPwd()
            throws Exception {
        VOUser user = createUserAndReRetrieveIt();
        idMgmt.notifyOnLoginAttempt(user, false);
        idMgmt.notifyOnLoginAttempt(user, false);
        idMgmt.notifyOnLoginAttempt(user, false);
        user = retrieveUser(user);
        idMgmt.requestResetOfUserPassword(user, null);
        PlatformUser pUser = getDOUser(user.getKey());
        Assert.assertEquals(0, pUser.getFailedLoginCounter());
        Assert.assertEquals(UserAccountStatus.PASSWORD_MUST_BE_CHANGED,
                pUser.getStatus());
    }

    @Test(expected = MailOperationException.class)
    public void testRequestResetOfUserPasswordMailOperationFailed()
            throws Exception {
        VOUser user = createUserAndReRetrieveIt();
        throwMailOperationFailed = true;
        idMgmt.requestResetOfUserPassword(user, null);
    }

    @Test(expected = UserActiveException.class)
    public void testRequestResetOfUserPasswordActiveSession() throws Exception {
        VOUser user = createUserAndReRetrieveIt();
        sessionList.add(new Session());
        idMgmt.requestResetOfUserPassword(user, null);
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testConcurrentRequestResetOfUserPassword() throws Exception {
        messagesOfTaskQueue = new ArrayList<>();
        VOUserDetails user = createUserAndReRetrieveIt();
        user.setAddress("Somewhere Else");
        idMgmt.updateUser(user);
        idMgmt.requestResetOfUserPassword(user, null);
    }

    @Test
    public void testResetPasswordForUser() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                PlatformUser pUser = mgr.find(PlatformUser.class,
                        Long.parseLong(userKey));
                pUser.setFailedLoginCounter(1);
                pUser.setStatus(UserAccountStatus.LOCKED_FAILED_LOGIN_ATTEMPTS);
                return null;
            }
        });
        PlatformUser modifiedUser = runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                PlatformUser pUser = mgr.find(PlatformUser.class,
                        Long.parseLong(userKey));
                idMgmtLocal.resetPasswordForUser(pUser, null);
                return pUser;
            }
        });

        Assert.assertEquals(0, modifiedUser.getFailedLoginCounter());
        Assert.assertEquals(UserAccountStatus.PASSWORD_MUST_BE_CHANGED,
                modifiedUser.getStatus());
    }

    @Test
    public void testChangePassword() throws Exception {
        VOUser user = createUserAndReRetrieveIt();
        container.login(String.valueOf(user.getKey()));
        idMgmt.changePassword(initialPassword, "secret");
    }

    @Test
    public void testChangePassword_Short() throws Exception {
        VOUser user = createUserAndReRetrieveIt();
        container.login(String.valueOf(user.getKey()));
        try {
            idMgmt.changePassword(initialPassword, "short");
        } catch (ValidationException e) {
            assertEquals("Wrong reason", ReasonEnum.MIN_LENGTH, e.getReason());
        }
    }

    @Test
    public void testChangePassword_BlanksOnly() throws Exception {
        VOUser user = createUserAndReRetrieveIt();
        container.login(String.valueOf(user.getKey()));
        idMgmt.changePassword(initialPassword, "      ");
    }

    @Test
    public void testChangePasswordSecurityCheckFailed() throws Exception {
        VOUser user = createUserAndReRetrieveIt();
        container.login(String.valueOf(user.getKey()));
        ldapAccessStub.setThrowNamingException(true);
        try {
            idMgmt.changePassword("secret", "secret");
            Assert.fail("Change password must fail");
        } catch (SecurityCheckException e) {
            Assert.assertEquals(e.getMessageKey(), "error.changePassword");
        }
        try {
            idMgmt.changePassword("secret", "secret");
        } catch (SecurityCheckException e) {
            Assert.assertEquals(e.getMessageKey(), "error.changePassword");
        }
        try {
            idMgmt.changePassword("secret", "secret");
        } catch (SecurityCheckException e) {
            Assert.assertEquals(e.getMessageKey(), "error.changePassword");
        }
        user = idMgmt.getUser(user);
        Assert.assertEquals(UserAccountStatus.LOCKED_FAILED_LOGIN_ATTEMPTS,
                user.getStatus());
    }

    @Test
    public void testModifyUserDataChangingAdminSettings() throws Exception {
        VOUserDetails user = createUserAndReRetrieveIt();
        user.addUserRole(UserRoleType.ORGANIZATION_ADMIN);

        container.login(String.valueOf(user.getKey()), ROLE_ORGANIZATION_ADMIN);
        idMgmt.updateUser(user);

        VOUserDetails userUpdated = retrieveUser(user);

        Assert.assertEquals(false, userUpdated.hasAdminRole());
    }

    @Test
    public void testGetOverdueOrganizationAdminsNoHit() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                List<PlatformUser> overdueOrganizationAdmins = idMgmtLocal
                        .getOverdueOrganizationAdmins(
                                System.currentTimeMillis());
                Assert.assertEquals(
                        "No overdue admin exists, so none may be found", 0,
                        overdueOrganizationAdmins.size());
                return null;
            }
        });
    }

    @Test
    public void testGetOverdueOrganizationAdminsOneHit() throws Exception {
        // create an admin user with the status "not confirmed"
        final VOUserDetails testUser = createTestUser();
        testUser.setUserId("overdueOrganizationAdmin1");
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Organization org = mgr.getReference(Organization.class,
                        organization.getKey());
                Marketplace mp = new Marketplace();
                mp.setMarketplaceId(MP_ID);
                idMgmtLocal.createOrganizationAdmin(testUser, org, "secret",
                        Long.valueOf(12345),
                        (Marketplace) mgr.getReferenceByBusinessKey(mp));

                List<PlatformUser> overdueOrganizationAdmins = idMgmtLocal
                        .getOverdueOrganizationAdmins(
                                System.currentTimeMillis() + 2000L);
                Assert.assertEquals("Overdue organization admin not found", 1,
                        overdueOrganizationAdmins.size());
                return null;
            }
        });
    }

    @Test
    public void testGetOverdueOrganizationAdminsTwoHits() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                // load organization
                Organization org = mgr.getReference(Organization.class,
                        organization.getKey());

                // create another admin user with the status "not confirmed"
                VOUserDetails testUser = createTestUser();
                testUser.setUserId("overdueOrganizationAdmin1");
                Marketplace mp = new Marketplace();
                mp.setMarketplaceId(MP_ID);
                mp = (Marketplace) mgr.getReferenceByBusinessKey(mp);
                idMgmtLocal.createOrganizationAdmin(testUser, org, "secret",
                        Long.valueOf(12345), mp);
                testUser.setUserId("overdueOrganizationAdmin2");
                idMgmtLocal.createOrganizationAdmin(testUser, org, "secret",
                        Long.valueOf(12345), mp);

                List<PlatformUser> overdueOrganizationAdmins = idMgmtLocal
                        .getOverdueOrganizationAdmins(
                                System.currentTimeMillis() + 3000L);
                Assert.assertEquals("Overdue organization admin not found", 2,
                        overdueOrganizationAdmins.size());
                return null;
            }
        });
    }

    @Test
    public void testGetUser() throws Exception {
        VOUserDetails testUser = createTestUser();
        testUser.setUserId("overdueOrganizationAdmin1");
        testUser.setOrganizationId(organization.getOrganizationId());
        idMgmt.createUser(testUser, Collections.<UserRoleType> emptyList(),
                MP_ID);
        VOUser storedUser = idMgmt.getUser(testUser);
        Assert.assertNotNull("User was not found", storedUser);
        Assert.assertTrue("Key for the user is not set",
                storedUser.getKey() != 0);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void testGetUserOperationNotPermitted() throws Exception {
        VOUserDetails user = createUserAndReRetrieveIt();
        final long userKey = user.getKey();

        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                PlatformUser user = mgr.find(PlatformUser.class, userKey);
                user.getOrganization().setDeregistrationDate(
                        Long.valueOf(System.currentTimeMillis()));
                return null;
            }
        });

        idMgmt.getUser(user);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testGetUserNotExisting() throws Exception {
        // given
        VOUser user = new VOUser();
        user.setUserId("overdueOrganizationAdmin3");
        user.setOrganizationId(organization.getOrganizationId());

        // when
        idMgmt.getUser(user);
    }

    @Test
    public void testGetUserOfOtherOrganization() throws Exception {
        VOUser user = new VOUser();
        user.setUserId(USER_ID_EXISTING_2);
        VOUser storedUser = idMgmt.getUser(user);
        Assert.assertNotNull("User was not found", storedUser);
        Assert.assertTrue("Key for the user is not set",
                storedUser.getKey() != 0);
    }

    @Test(expected = OrganizationRemovedException.class)
    public void testGetUserForRemovedOrganization() throws Exception {
        // setup the following scenario: organization with a user in state
        // LOCKED_NOT_CONFIRMED, then delete user and organization
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                doSetupOrganizationUserRemovedScenario("usera",
                        "organizationtoremove1", true);
                return null;
            }
        });

        // now check if the getUser call ends up with the correct exception:
        // OrganizationRemovedException

        VOUser user = new VOUser();
        user.setUserId("usera");
        user.setOrganizationId("organizationtoremove1");
        idMgmt.getUser(user);
    }

    @Test
    public void testGetUserForRemovedOrganizationAndNonRemovedOrganization()
            throws Exception {
        // setup the following scenario: organization with a user in state
        // LOCKED_NOT_CONFIRMED, then delete user and organization
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                doSetupOrganizationUserRemovedScenario("userb1",
                        "organizationtoremove2", true);
                doSetupOrganizationUserRemovedScenario("userb2",
                        "organizationtoremove3", false);
                return null;
            }
        });

        // now check if the getUser call ends up with the correct exception:
        // OrganizationRemovedException
        try {
            VOUser user = new VOUser();
            user.setUserId("userb1");
            user.setOrganizationId("organizationtoremove2");
            idMgmt.getUser(user);
            Assert.fail("The user has been deleted, the call must fail");
        } catch (OrganizationRemovedException e) {
            // expected
        }
        VOUser user = new VOUser();
        user.setUserId("userb2");
        user.setOrganizationId("organizationtoremove3");
        VOUser storedUser = idMgmt.getUser(user);
        Assert.assertNotNull("User was not found", storedUser);
        Assert.assertTrue("Key for the user is not set",
                storedUser.getKey() != 0);
    }

    @Test
    public void testImportLdapUsers() throws Exception {
        addLdapOrganizationSetting();

        List<VOUserDetails> list = idMgmt.searchLdapUsers("poc*");
        Assert.assertEquals(1, list.size());

        Assert.assertEquals("pock", list.get(0).getRealmUserId());

        idMgmt.importLdapUsers(list, null);

        VOUserDetails user;
        user = idMgmt.getUserDetails(list.get(0));
        Assert.assertEquals("pock", user.getUserId());
        Assert.assertEquals("peter.pock@est.fujitsu.com", user.getEMail());

        list = idMgmt.searchLdapUsers("po*");
        Assert.assertEquals(2, list.size());

        removeOrganizationSettings();
    }

    @Test
    public void testImportLdapUsers_WithMarketplace() throws Exception {
        addLdapOrganizationSetting();

        List<VOUserDetails> list = idMgmt.searchLdapUsers("poc*");
        Assert.assertEquals(1, list.size());

        Assert.assertEquals("pock", list.get(0).getRealmUserId());

        idMgmt.importLdapUsers(list, MP_ID);
        Assert.assertTrue(
                Arrays.toString(receivedParams).contains("?mId=" + MP_ID));

        VOUserDetails user;
        user = idMgmt.getUserDetails(list.get(0));
        Assert.assertEquals("pock", user.getUserId());
        Assert.assertEquals("peter.pock@est.fujitsu.com", user.getEMail());

        list = idMgmt.searchLdapUsers("po*");
        Assert.assertEquals(2, list.size());

        removeOrganizationSettings();
    }

    @Test(expected = ValidationException.class)
    public void testImportLdapUsersNotFound() throws Exception {
        addLdapOrganizationSetting();
        List<VOUserDetails> list = new ArrayList<>();
        VOUserDetails user = new VOUserDetails();
        user.setUserId("");
        list.add(user);
        idMgmt.importLdapUsers(list, null);
    }

    @Test(expected = ValidationException.class)
    public void testImportLdapUsersNotUnique() throws Exception {
        addLdapOrganizationSetting();
        List<VOUserDetails> list = new ArrayList<>();
        VOUserDetails user = new VOUserDetails();
        user.setUserId("po*");
        list.add(user);
        idMgmt.importLdapUsers(list, null);
    }

    @Test(expected = ValidationException.class)
    public void testImportLdapUsersNamingException() throws Exception {
        addLdapOrganizationSetting();
        List<VOUserDetails> list = new ArrayList<>();
        VOUserDetails user = new VOUserDetails();
        user.setUserId("pock");
        list.add(user);
        ldapAccessStub.setThrowNamingException(true);
        idMgmt.importLdapUsers(list, null);
    }

    @Test
    public void testSearchLdapUsers() throws Exception {
        addLdapOrganizationSetting();

        List<VOUserDetails> list = idMgmt.searchLdapUsers("poc*");
        Assert.assertEquals(1, list.size());

        Assert.assertEquals("pock", list.get(0).getRealmUserId());
    }

    @Test(expected = ValidationException.class)
    public void testSearchLdapUsersNamingException() throws Exception {
        addLdapOrganizationSetting();
        ldapAccessStub.setThrowNamingException(true);
        idMgmt.searchLdapUsers("poc*");
    }

    @Test
    public void searchLdapUsersOverLimit() throws Exception {
        addLdapOrganizationSetting();
        cfg.setConfigurationSetting(
                new ConfigurationSetting(ConfigurationKey.LDAP_SEARCH_LIMIT,
                        Configuration.GLOBAL_CONTEXT, "1"));

        boolean flag = idMgmt.searchLdapUsersOverLimit("poc*");
        Assert.assertFalse(flag);
    }

    @Test(expected = ValidationException.class)
    public void searchLdapUsersOverLimitNamingException() throws Exception {
        addLdapOrganizationSetting();
        ldapAccessStub.setThrowNamingException(true);
        idMgmt.searchLdapUsersOverLimit("poc*");
    }

    @Test
    public void testSendAccounts()
            throws ValidationException, MailOperationException {
        idMgmt.sendAccounts("someMail@somehost.de", null);
        Assert.assertEquals(1, mailCounter);
        Assert.assertFalse(receivedParams[0].toString().contains("&mId="));
    }

    @Test
    public void testSendAccounts_WithMarketplace()
            throws ValidationException, MailOperationException {
        idMgmt.sendAccounts("someMail@somehost.de", "MP_ID");
        Assert.assertEquals(1, mailCounter);
        Assert.assertTrue(receivedParams[0].toString().contains("&mId=MP_ID"));
    }

    @Test
    public void testSendAccounts_BaseUrl()
            throws ValidationException, MailOperationException {
        // given a base url without a slash
        cfg.setConfigurationSetting(
                new ConfigurationSetting(ConfigurationKey.BASE_URL,
                        Configuration.GLOBAL_CONTEXT, BASE_URL_WITH_SLASH));

        // when
        idMgmt.sendAccounts("someMail@somehost.de", "MP_ID");

        // verify that the e-mail contains the base url appended with ?oId
        // without a slash
        Assert.assertEquals(1, mailCounter);
        Assert.assertTrue(
                receivedParams[0].toString().contains(BASE_URL + "?oId"));
    }

    @Test
    public void testSendAccounts_BaseUrlWithSlash()
            throws ValidationException, MailOperationException {
        // given a base url with a slash
        cfg.setConfigurationSetting(
                new ConfigurationSetting(ConfigurationKey.BASE_URL,
                        Configuration.GLOBAL_CONTEXT, BASE_URL_WITH_SLASH));

        // when
        idMgmt.sendAccounts("someMail@somehost.de", "MP_ID");

        // verify that the e-mail contains the base url appended with ?oId
        // without a slash
        Assert.assertEquals(1, mailCounter);
        Assert.assertTrue(
                receivedParams[0].toString().contains(BASE_URL + "?oId"));
    }

    @Test
    public void testSendAccounts_BaseUrlWithTwoSlashes()
            throws ValidationException, MailOperationException {
        // given a base url with a slash
        cfg.setConfigurationSetting(new ConfigurationSetting(
                ConfigurationKey.BASE_URL, Configuration.GLOBAL_CONTEXT,
                BASE_URL_WITH_TWO_SLASHES));

        // when
        idMgmt.sendAccounts("someMail@somehost.de", "MP_ID");

        // verify that the e-mail contains the base url appended with ?oId
        // without a slash
        Assert.assertEquals(1, mailCounter);
        Assert.assertTrue(
                receivedParams[0].toString().contains(BASE_URL + "?oId"));
    }

    @Test
    public void testGetUserDetails() {
        VOUserDetails user = idMgmt.getCurrentUserDetails();
        Assert.assertEquals("admin", user.getUserId());
    }

    @Test
    public void testGetUserDetailsIfPresent() {
        VOUserDetails user = idMgmt.getCurrentUserDetailsIfPresent();
        Assert.assertEquals("admin", user.getUserId());
    }

    @Test
    public void testGetUserDetailsIfPresentNegative() {
        container.logout();
        VOUserDetails user = idMgmt.getCurrentUserDetailsIfPresent();
        Assert.assertNull("No user details expected", user);
    }

    @Test
    public void testModifyUserDataLocalAdmin1() throws Exception {
        List<VOUserDetails> users = idMgmt.getUsersForOrganization();
        final VOUserDetails user = users.get(0);
        testModifyAdmin(true, user, user);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void testModifyUserDataLocalAdmin2() throws Exception {
        List<VOUserDetails> users = idMgmt.getUsersForOrganization();
        final VOUserDetails user = users.get(0);
        VOUserDetails toModify = createUserAndReRetrieveIt();
        // must fail because admin wants to modify a different users account
        // although only modifying own account is allowed
        testModifyAdmin(true, user, toModify);
    }

    @Test
    public void testModifyUserDataLocalAdmin3() throws Exception {
        List<VOUserDetails> users = idMgmt.getUsersForOrganization();
        final VOUserDetails user = users.get(0);
        testModifyAdmin(false, user, user);
    }

    @Test
    public void testModifyUserDataLocalNonAdmin1() throws Exception {
        VOUserDetails user = createUserAndReRetrieveIt();
        container.login(String.valueOf(user.getKey()));
        testModifyAdmin(true, user, user);
    }

    @Test
    public void testModifyUserDataLocalNonAdmin2() throws Exception {
        VOUserDetails user = createUserAndReRetrieveIt();
        container.login(String.valueOf(user.getKey()));
        testModifyAdmin(false, user, user);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void testModifyUserDataLocalNonAdmin3() throws Exception {
        List<VOUserDetails> users = idMgmt.getUsersForOrganization();
        final VOUserDetails toModify = users.get(0);
        VOUserDetails user = createUserAndReRetrieveIt();
        container.login(String.valueOf(user.getKey()));
        // must fail because non admin wants to modify admin account
        testModifyAdmin(true, user, toModify);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void testModifyUserDataLocalNonAdmin4() throws Exception {
        List<VOUserDetails> users = idMgmt.getUsersForOrganization();
        final VOUserDetails toModify = users.get(0);
        VOUserDetails user = createUserAndReRetrieveIt();
        container.login(String.valueOf(user.getKey()));
        // must fail because non admin wants to modify admin account
        testModifyAdmin(false, user, toModify);
    }

    private void testModifyAdmin(final boolean modifyOwnUser,
            final VOUserDetails user, final VOUserDetails toModify)
            throws Exception {
        final String mail = "new_" + user.getEMail();
        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                PlatformUser tempUser1 = mgr.getReference(PlatformUser.class,
                        user.getKey());
                PlatformUser tempUser2 = mgr.getReference(PlatformUser.class,
                        toModify.getKey());
                VOUserDetails userData = UserDataAssembler
                        .toVOUserDetails(tempUser2);
                userData.setEMail(mail);
                idMgmtLocal.modifyUserData(tempUser1, userData, modifyOwnUser,
                        true);
                return null;
            }
        });
        String savedMail = runTX(new Callable<String>() {

            @Override
            public String call() throws Exception {
                PlatformUser tempUser2 = mgr.getReference(PlatformUser.class,
                        toModify.getKey());
                return tempUser2.getEmail();
            }
        });
        Assert.assertEquals(mail, savedMail);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // internal helper methods

    /**
     * Creates a organization and a platformuser for that organization, sets the
     * user in state locked_not_confirmed, then removes both of the entries. In
     * the end, the history entries will be there for further testing.
     */
    private void doSetupOrganizationUserRemovedScenario(String userId,
            String organizationId, boolean removeOrganizationAndUser)
            throws NonUniqueBusinessKeyException {
        Organization cust = new Organization();
        cust.setOrganizationId(organizationId);
        cust.setName("The organization");
        cust.setAddress(
                "my address is a very long string, which is stored in the database \n with line delimiters\n.");
        cust.setEmail("organization@organization.com");
        cust.setPhone("012345/678");
        cust.setCutOffDay(1);
        mgr.persist(cust);

        PlatformUser user = new PlatformUser();
        user.setUserId(userId);
        user.setEmail("someMail@somehost.de");
        user.setOrganization(cust);
        user.setStatus(UserAccountStatus.LOCKED_NOT_CONFIRMED);
        user.setLocale(Locale.ENGLISH.toString());
        cust.addPlatformUser(user);
        mgr.persist(user);

        if (removeOrganizationAndUser) {
            mgr.remove(user);
            mgr.remove(cust);
        }
    }

    private String createUserForOrganizationRole(final String supplierOrgId,
            final OrganizationRoleType... orgs) throws Exception {
        return runTX(new Callable<String>() {
            @Override
            public String call() throws Exception {
                Organization cust = Organizations.createOrganization(mgr, orgs);
                Organizations.supportAllCountries(mgr, cust);
                cust.setOrganizationId(supplierOrgId);
                mgr.flush();
                PlatformUser pt_user = new PlatformUser();
                pt_user.setUserId(USER_ID_EXISTING_SP_ADMIN);
                pt_user.setEmail("someMail@somehost.de");
                pt_user.setOrganization(cust);
                pt_user.setStatus(UserAccountStatus.ACTIVE);
                pt_user.setLocale(Locale.ENGLISH.toString());
                cust.addPlatformUser(pt_user);
                mgr.persist(pt_user);
                PlatformUsers.grantAdminRole(mgr, pt_user);

                PlatformUser n_user = new PlatformUser();
                n_user.setUserId(USER_ID_EXISTING_SP);
                n_user.setEmail("someMail@somehost.de");
                n_user.setOrganization(cust);
                n_user.setStatus(UserAccountStatus.ACTIVE);
                n_user.setLocale(Locale.ENGLISH.toString());
                cust.addPlatformUser(n_user);
                mgr.persist(n_user);

                return String.valueOf(pt_user.getKey());
            }
        });
    }

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
     * Inits a user object, creates it and retrieves it from the system, so that
     * the internal id values is set.
     *
     * @return The value object matching the current domain object.
     * @throws NonUniqueBusinessKeyException
     *             Thrown in case an entry with the same business key does
     *             already exist.
     * @throws MailOperationException
     * @throws ValidationException
     * @throws UserRoleAssignmentException
     * @throws OperationPendingException
     */
    private VOUserDetails createUserAndReRetrieveIt()
            throws NonUniqueBusinessKeyException, MailOperationException,
            ValidationException, UserRoleAssignmentException,
            OperationPendingException {
        VOUserDetails user = createTestUser();
        idMgmt.createUser(user, Collections.<UserRoleType> emptyList(), MP_ID);

        return retrieveUser(user);
    }

    /**
     * Retrieves the user from the system.
     *
     * @param user
     *            The user to be retrieved. The business key field(s) must be
     *            set.
     * @return The value object reflecting the current values.
     */
    private VOUserDetails retrieveUser(VOUser user) {
        VOUserDetails resultUser = null;
        List<VOUserDetails> users = idMgmt.getUsersForOrganization();
        for (VOUserDetails ud : users) {
            if (ud.getUserId().equals(user.getUserId()) && ud
                    .getOrganizationId().equals(user.getOrganizationId())) {
                resultUser = ud;
            }
        }
        return resultUser;
    }

    private PlatformUser getDOUser(final long key) throws Exception {
        return runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                return mgr.getReference(PlatformUser.class, key);
            }
        });
    }

    private void addLdapOrganizationSetting() {
        ldapOrgSettingsResolved.setProperty(SettingType.LDAP_URL.name(),
                "ldap://estinfra1.lan.est.fujitsu.de:389");
        ldapOrgSettingsResolved.setProperty(
                SettingType.LDAP_CONTEXT_FACTORY.name(),
                "com.sun.jndi.ldap.LdapCtxFactory");
        ldapOrgSettingsResolved.setProperty(SettingType.LDAP_BASE_DN.name(),
                "ou=people,dc=est,dc=fujitsu,dc=de");
        ldapOrgSettingsResolved.setProperty(SettingType.LDAP_ATTR_EMAIL.name(),
                "scalixEmailAddress");
        ldapOrgSettingsResolved.setProperty(SettingType.LDAP_ATTR_UID.name(),
                "uid");
        ldapOrgSettingsResolved.setProperty(
                SettingType.LDAP_ATTR_FIRST_NAME.name(), "givenName");
        ldapOrgSettingsResolved.setProperty(
                SettingType.LDAP_ATTR_ADDITIONAL_NAME.name(), "addName");
        ldapOrgSettingsResolved
                .setProperty(SettingType.LDAP_ATTR_LAST_NAME.name(), "sn");
    }

    private void removeOrganizationSettings() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                Organization o = Organizations.findOrganization(mgr,
                        organization.getOrganizationId());
                for (OrganizationSetting os : o.getOrganizationSettings()) {
                    mgr.remove(os);
                }
                return null;
            }
        });
    }

    private List<RoleAssignment> getRoleAssignmentByUserKey(long key) {
        return ParameterizedTypes.list(mgr
                .createQuery(
                        "SELECT o FROM RoleAssignment o WHERE o.user.key = :userKey")
                .setParameter("userKey", Long.valueOf(key)).getResultList(),
                RoleAssignment.class);
    }

    private void setupSubscriptionServiceToReturnUsageLicense() {
        when(subSvcMock.getSubscriptionsForUserInt(any(PlatformUser.class)))
                .thenAnswer(new Answer<List<Subscription>>() {

                    @Override
                    public List<Subscription> answer(
                            InvocationOnMock invocation) throws Throwable {
                        UsageLicense usageLicense = new UsageLicense();
                        usageLicense.setKey(22222);
                        usageLicense.setUser(
                                (PlatformUser) invocation.getArguments()[0]);

                        Subscription subscription = new Subscription();
                        subscription.setKey(11111);
                        subscription.setSubscriptionId("s1");
                        subscription.setStatus(SubscriptionStatus.ACTIVE);
                        subscription.setUsageLicenses(
                                Collections.singletonList(usageLicense));
                        return Collections.singletonList(subscription);
                    }
                });
    }

}
