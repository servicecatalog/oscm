/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Mar 28, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.identityservice.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.contains;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.oscm.types.constants.marketplace.Marketplace.MARKETPLACE_ROOT;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.Query;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.oscm.communicationservice.local.CommunicationServiceLocal;
import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.converter.ParameterizedTypes;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.ConfigurationSetting;
import org.oscm.domobjects.DomainObject;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationSetting;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.RoleAssignment;
import org.oscm.domobjects.Tenant;
import org.oscm.domobjects.TriggerProcess;
import org.oscm.domobjects.UserRole;
import org.oscm.encrypter.AESEncrypter;
import org.oscm.identityservice.control.SendMailControl;
import org.oscm.internal.types.enumtypes.ConfigurationKey;
import org.oscm.internal.types.enumtypes.SettingType;
import org.oscm.internal.types.enumtypes.UserAccountStatus;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.vo.VOUser;
import org.oscm.internal.vo.VOUserDetails;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.UserRoles;
import org.oscm.triggerservice.local.TriggerMessage;
import org.oscm.triggerservice.local.TriggerProcessMessageData;
import org.oscm.triggerservice.local.TriggerQueueServiceLocal;
import org.oscm.types.constants.Configuration;
import org.oscm.types.enumtypes.EmailType;
import org.oscm.usergroupservice.bean.UserGroupServiceLocalBean;

/**
 * Tests the correct generation of the context URL of Emails sent by this
 * service.
 * 
 * @author barzu
 */
public class IdentityServiceBeanMailSendingTest {

    private IdentityServiceBean idSrv;
    private CommunicationServiceLocal cm;
    private ConfigurationServiceLocal cs;
    private UserGroupServiceLocalBean userGroupServiceLocalBean;

    private PlatformUser pUser;
    private final List<RoleAssignment> persistedRoleAssignments = new ArrayList<>();

    private static final String BASE_URL = "BASE_URL";
    private static final String BASE_URL_WITH_SLASH = "BASE_URL/";
    private static final String BASE_URL_WITH_TWO_SLASHES = "BASE_URL//";

    @Before
    public void setup() throws Exception {
        AESEncrypter.generateKey();
        persistedRoleAssignments.clear();
        SendMailControl.clear();
        idSrv = spy(new IdentityServiceBean());

        DataService dm = mock(DataService.class);
        idSrv.dm = dm;
        pUser = new PlatformUser();
        Organization org = new Organization();
        org.setOrganizationId("orgId");
        pUser.setOrganization(org);
        pUser.setKey(1234);
        pUser.setUserId("userid");
        doReturn(pUser).when(dm).getReference(eq(PlatformUser.class),
                anyLong());
        doReturn(pUser).when(idSrv.dm).getCurrentUser();

        when(dm.find(any(DomainObject.class)))
                .thenAnswer(new Answer<DomainObject<?>>() {

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

        doReturn(null).when(idSrv).loadUser(anyString(), any(Tenant.class));
        Query triggerQuery = mock(Query.class);
        when(triggerQuery.getSingleResult()).thenReturn(Long.valueOf(0));
        when(dm.createNamedQuery(contains("TriggerProcessIdentifier")))
                .thenReturn(triggerQuery);

        cm = mock(CommunicationServiceLocal.class);
        idSrv.cm = cm;

        cs = mock(ConfigurationServiceLocal.class);
        idSrv.cs = cs;
        ConfigurationSetting setting = new ConfigurationSetting(
                ConfigurationKey.BASE_URL, Configuration.GLOBAL_CONTEXT,
                BASE_URL);

        doReturn(setting).when(cs).getConfigurationSetting(
                any(ConfigurationKey.class), anyString());
        doReturn(BASE_URL).when(cs).getBaseURL();
        doReturn("baseUrl").when(cm).getBaseUrl();
        doReturn("baseUrl").when(cm).getBaseUrlWithTenant(anyString());
        doReturn("marketplaceUrl").when(cm).getMarketplaceUrl(anyString());
        TriggerQueueServiceLocal triggerQS = mock(
                TriggerQueueServiceLocal.class);
        idSrv.triggerQS = triggerQS;
        doReturn(Collections.singletonList(new TriggerProcessMessageData(
                new TriggerProcess(), new TriggerMessage()))).when(triggerQS)
                        .sendSuspendingMessages(ParameterizedTypes
                                .list(anyList(), TriggerMessage.class));

        userGroupServiceLocalBean = mock(UserGroupServiceLocalBean.class);
        idSrv.userGroupService = userGroupServiceLocalBean;
    }

    @After
    public void tearDown() {
        SendMailControl.clear();
    }

    @Test
    public void confirmAccount_PublicMP() throws Exception {
        pUser.setStatus(UserAccountStatus.LOCKED_NOT_CONFIRMED);
        idSrv.confirmAccount(new VOUser(), "1");

        verify(cm, times(1)).getMarketplaceUrl(anyString());
    }

    @Test
    public void confirmAccount_AdminPortal() throws Exception {
        pUser.setStatus(UserAccountStatus.LOCKED_NOT_CONFIRMED);
        pUser.setAssignedRoles(UserRoles.createRoleAssignments(pUser,
                UserRoleType.MARKETPLACE_OWNER));
        idSrv.confirmAccount(new VOUser(), "1");

        verify(cm, times(1)).getBaseUrl();
    }

    /**
     * Test that on account confirmation user id and user key are passed to be
     * contained in the mail (Bug 9168)
     */
    @Test
    public void confirmAccount_MailParams() throws Exception {
        // given
        pUser.setStatus(UserAccountStatus.LOCKED_NOT_CONFIRMED);

        // when
        idSrv.confirmAccount(new VOUser(), "1");

        // then
        verifyMailParameters();
    }

    /**
     * Test that on account confirmation user status is set to
     */
    @Test
    public void confirmAccount_UserStatus() throws Exception {
        // given
        pUser.setStatus(UserAccountStatus.LOCKED_NOT_CONFIRMED);

        // when
        idSrv.confirmAccount(new VOUser(), "1");

        // then
        assertEquals(UserAccountStatus.ACTIVE, pUser.getStatus());
    }

    @Test
    public void createOrganizationAdmin_PublicMP() throws Exception {
        setupDsForRefreshingUserRoles();
        Organization org = Organizations.createOrganization("fdaskj");

        // pwd not auto generated -> test pwd confirmation mail
        idSrv.createOrganizationAdmin(getVOUserDetails(org, "user1"), org,
                "abc", Long.valueOf(123L), new Marketplace());

        // verify '/marketplace' is contained by pwd confirmation mail URL
        verify(cm, times(0)).getMarketplaceUrl(anyString());
        verify(cm, times(1)).sendMail(any(PlatformUser.class),
                eq(EmailType.USER_CONFIRM),
                argThat(getArrayContainsStringMatcher(MARKETPLACE_ROOT)),
                any(Marketplace.class));
    }

    /**
     * Bug9301: Confirmation URL have to be encoded by using URLEncoder (e.g.
     * '+' => %2B)
     */
    @Test
    public void createOrganizationAdmin_PublicMPWithJapaneseID()
            throws Exception {
        setupDsForRefreshingUserRoles();
        Organization org = Organizations.createOrganization("fdaskj");

        // pwd not auto generated -> test pwd confirmation mail
        idSrv.createOrganizationAdmin(getVOUserDetails(org, "富士次郎"), org, "abc",
                Long.valueOf(123L), new Marketplace());

        // verify '/marketplace' is contained by pwd confirmation mail URL
        verify(cm, times(0)).getMarketplaceUrl(anyString());
        verify(cm, times(1)).sendMail(any(PlatformUser.class),
                eq(EmailType.USER_CONFIRM),
                argThat(getArrayContainsStringMatcher(
                        "ZmRhc2tqJuWvjOWjq%2BasoemDjiYxMjMm")),
                any(Marketplace.class));
    }

    @Test
    public void createOrganizationAdmin_AdminPortal() throws Exception {
        setupDsForRefreshingUserRoles();
        Organization org = Organizations.createOrganization("fdaskj");
        VOUserDetails userDetails = getVOUserDetails(org, "user1");
        userDetails.addUserRole(UserRoleType.MARKETPLACE_OWNER);

        // pwd not auto generated -> test pwd confirmation mail
        idSrv.createOrganizationAdmin(userDetails, org, "abc",
                Long.valueOf(123L), new Marketplace());

        // verify no '/marketplace' in pwd confirmation mail URL
        verify(cm, times(0)).getMarketplaceUrl(anyString());
        verify(cm, times(1)).sendMail(any(PlatformUser.class),
                eq(EmailType.USER_CONFIRM),
                argThat(getArrayNotContainsStringMatcher(MARKETPLACE_ROOT)),
                any(Marketplace.class));
    }

    @Test
    public void createOrganizationAdmin_BaseUrl() throws Exception {
        // given
        setupDsForRefreshingUserRoles();
        Organization org = Organizations.createOrganization("fdaskj");
        VOUserDetails userDetails = getVOUserDetails(org, "user1");
        userDetails.addUserRole(UserRoleType.MARKETPLACE_OWNER);

        // when pwd not auto generated
        idSrv.createOrganizationAdmin(userDetails, org, "abc",
                Long.valueOf(123L), new Marketplace());

        // verify that the trailing slash of the base url is cut in the
        // confirmation mail
        verify(cm, times(1)).sendMail(any(PlatformUser.class),
                eq(EmailType.USER_CONFIRM),
                argThat(getArrayContainsStringMatcher(
                        "BASE_URL/public/confirm.jsf")),
                any(Marketplace.class));
    }

    @Test
    public void createOrganizationAdmin_BaseUrlWithSlash() throws Exception {
        // given a base url with slash
        setupDsForRefreshingUserRoles();
        ConfigurationSetting setting = new ConfigurationSetting(
                ConfigurationKey.BASE_URL, Configuration.GLOBAL_CONTEXT,
                BASE_URL_WITH_SLASH);

        doReturn(setting).when(cs).getConfigurationSetting(
                any(ConfigurationKey.class), anyString());

        Organization org = Organizations.createOrganization("fdaskj");
        VOUserDetails userDetails = getVOUserDetails(org, "user1");
        userDetails.addUserRole(UserRoleType.MARKETPLACE_OWNER);

        // when pwd not auto generated
        idSrv.createOrganizationAdmin(userDetails, org, "abc",
                Long.valueOf(123L), new Marketplace());

        // verify that the trailing slash of the base url is cut in the
        // confirmation mail
        verify(cm, times(1)).sendMail(any(PlatformUser.class),
                eq(EmailType.USER_CONFIRM),
                argThat(getArrayContainsStringMatcher(
                        "BASE_URL/public/confirm.jsf")),
                any(Marketplace.class));
    }

    @Test
    public void createOrganizationAdmin_BaseUrlWithTwoSlashes()
            throws Exception {
        // given a base url with two slashes
        setupDsForRefreshingUserRoles();
        ConfigurationSetting setting = new ConfigurationSetting(
                ConfigurationKey.BASE_URL, Configuration.GLOBAL_CONTEXT,
                BASE_URL_WITH_TWO_SLASHES);

        doReturn(setting).when(cs).getConfigurationSetting(
                any(ConfigurationKey.class), anyString());

        Organization org = Organizations.createOrganization("fdaskj");
        VOUserDetails userDetails = getVOUserDetails(org, "user1");
        userDetails.addUserRole(UserRoleType.MARKETPLACE_OWNER);

        // when pwd not auto generated
        idSrv.createOrganizationAdmin(userDetails, org, "abc",
                Long.valueOf(123L), new Marketplace());

        // verify that the trailing slash of the base url is cut in the
        // confirmation mail
        verify(cm, times(1)).sendMail(any(PlatformUser.class),
                eq(EmailType.USER_CONFIRM),
                argThat(getArrayContainsStringMatcher(
                        "BASE_URL/public/confirm.jsf")),
                any(Marketplace.class));
    }

    @Test
    public void createOrganizationAdmin_AutoGenPwd_PublicMP() throws Exception {
        setupDsForRefreshingUserRoles();
        Organization org = Organizations.createOrganization("fdaskj");

        // null password -> auto-generated -> skips pwd confirmation mail
        idSrv.createOrganizationAdmin(getVOUserDetails(org, "user1"), org, null,
                Long.valueOf(123L), new Marketplace());

        // verify no '/marketplace' in user add mail URL
        verify(cm, times(1)).getMarketplaceUrl(anyString());
    }

    @Test
    public void createOrganizationAdmin_AutoGenPwd_AdminPortal()
            throws Exception {
        setupDsForRefreshingUserRoles();
        Organization org = Organizations.createOrganization("fdaskj");
        // non-customer role
        VOUserDetails userDetails = getVOUserDetails(org, "user1");
        userDetails.addUserRole(UserRoleType.MARKETPLACE_OWNER);

        // null password -> auto-generated -> skips pwd confirmation mail
        idSrv.createOrganizationAdmin(userDetails, org, null,
                Long.valueOf(123L), new Marketplace());

        // verify no '/marketplace' in user add mail URL
        verify(cm, times(1)).getBaseUrl();
    }

    @Test
    public void createOrganizationAdmin_RemoteLdapActive_PublicMP()
            throws Exception {
        setupDsForRefreshingUserRoles();
        Organization org = Organizations.createOrganization("fdaskj");
        // LDAP active
        OrganizationSetting orgSetting = new OrganizationSetting();
        orgSetting.setSettingType(SettingType.LDAP_URL);
        org.setOrganizationSettings(Collections.singletonList(orgSetting));

        // null password -> auto-generated -> skips pwd confirmation mail
        idSrv.createOrganizationAdmin(getVOUserDetails(org, "user1"), org, null,
                Long.valueOf(123L), new Marketplace());

        // verify no '/marketplace' in user add mail URL
        verify(cm, times(1)).getMarketplaceUrl(anyString());
    }

    @Test
    public void createOrganizationAdmin_RemoteLdapActive_AdminPortal()
            throws Exception {
        setupDsForRefreshingUserRoles();
        Organization org = Organizations.createOrganization("fdaskj");
        // LDAP active
        OrganizationSetting orgSetting = new OrganizationSetting();
        orgSetting.setSettingType(SettingType.LDAP_URL);
        org.setOrganizationSettings(Collections.singletonList(orgSetting));
        // non-customer role
        VOUserDetails userDetails = getVOUserDetails(org, "user1");
        userDetails.addUserRole(UserRoleType.TECHNOLOGY_MANAGER);

        // null password -> auto-generated -> skips confirmation mail
        idSrv.createOrganizationAdmin(userDetails, org, null,
                Long.valueOf(123L), new Marketplace());

        // verify no '/marketplace' is contained by user add mail URL
        verify(cm, times(1)).getBaseUrl();
    }

    @Test
    public void createUser_AdminPortal_NoRole() throws Exception {
        setupIgnoreRoleCheck();

        idSrv.createUser(
                getVOUserDetails(Organizations.createOrganization("orgId"),
                        "userId"),
                new ArrayList<UserRoleType>(), "abc");

        // since NO manager, mail should NOT contain administration portal URL
        verify(idSrv.cm, times(1)).getMarketplaceUrl(anyString());
    }

    @Test
    public void createUser_AdminPortal_ORGANIZATION_ADMIN() throws Exception {
        setupIgnoreRoleCheck();

        idSrv.createUser(
                getVOUserDetails(Organizations.createOrganization("orgId"),
                        "userId"),
                Collections.singletonList(UserRoleType.ORGANIZATION_ADMIN),
                "abc");

        // since NO manager, mail should NOT contain administration portal URL
        verify(idSrv.cm, times(1)).getMarketplaceUrl(anyString());
    }

    @Test
    public void createUser_AdminPortal_MARKETPLACE_OWNER() throws Exception {
        setupIgnoreRoleCheck();

        idSrv.createUser(
                getVOUserDetails(Organizations.createOrganization("orgId"),
                        "userId"),
                Collections.singletonList(UserRoleType.MARKETPLACE_OWNER),
                "abc");

        // since manager, mail should contain administration portal URL
        verify(idSrv.cm, times(1)).getBaseUrl();
    }

    @Test
    public void createUser_AdminPortal_TECHNOLOGY_MANAGER() throws Exception {
        setupIgnoreRoleCheck();

        idSrv.createUser(
                getVOUserDetails(Organizations.createOrganization("orgId"),
                        "userId"),
                Collections.singletonList(UserRoleType.TECHNOLOGY_MANAGER),
                "abc");

        // since manager, mail should contain administration portal URL
        verify(idSrv.cm, times(1)).getBaseUrl();
    }

    @Test
    public void createUser_AdminPortal_SERVICE_MANAGER() throws Exception {
        setupIgnoreRoleCheck();

        idSrv.createUser(
                getVOUserDetails(Organizations.createOrganization("orgId"),
                        "userId"),
                Collections.singletonList(UserRoleType.SERVICE_MANAGER), "abc");

        // since manager, mail should contain administration portal URL
        verify(idSrv.cm, times(1)).getBaseUrl();
    }

    @Test
    public void createUser_AdminPortal_PLATFORM_OPERATOR() throws Exception {
        setupIgnoreRoleCheck();

        idSrv.createUser(
                getVOUserDetails(Organizations.createOrganization("orgId"),
                        "userId"),
                Collections.singletonList(UserRoleType.PLATFORM_OPERATOR),
                "abc");

        // since manager, mail should contain administration portal URL
        verify(idSrv.cm, times(1)).getBaseUrl();
    }

    /**
     * Test cases for bug #9225.
     * 
     * @throws Exception
     */
    @Test
    public void sendMailToCreatedUser_NoManagerRole() throws Exception {
        // Given a user with no manager role and a marketplace with no
        // marketplaceId set (bes classic portal)
        Marketplace marketplace = new Marketplace();

        // when sending a mail to the created user
        idSrv.sendMailToCreatedUser("secret", true, marketplace, pUser);

        // assert that the the mail
        // should contain the context URL
        verify(idSrv.cm, times(0)).getBaseUrl();
        verify(idSrv.cm, times(1)).getMarketplaceUrl(anyString());

        // verify that mail parameters are correct
        ArgumentCaptor<Object[]> ac = ArgumentCaptor.forClass(Object[].class);
        verify(cm, times(1)).sendMail(eq(pUser), eq(EmailType.USER_CREATED),
                ac.capture(), any(Marketplace.class));
        Object[] value = ac.getValue();
        assertEquals(pUser.getUserId(), value[0]);
        assertEquals("secret", value[1]);
        assertEquals("marketplaceUrl", value[2]);
        assertEquals(String.valueOf(pUser.getKey()), value[3]);

    }

    @Test
    public void sendMailToCreatedUser_ManagerRole() throws Exception {
        // Given a user with the service manager role
        addRole(pUser, UserRoleType.SERVICE_MANAGER);

        // and a marketplace with no marketplaceId set (bes classic portal)
        Marketplace marketplace = new Marketplace();

        // when sending a mail to the created user
        idSrv.sendMailToCreatedUser("secret", true, marketplace, pUser);

        // assert that the the mail, since user is manager,
        // should contain the context URL
        verify(idSrv.cm, times(1)).getBaseUrl();
        verify(idSrv.cm, times(0)).getMarketplaceUrl(anyString());

        // verify that mail parameters are correct
        ArgumentCaptor<Object[]> ac = ArgumentCaptor.forClass(Object[].class);
        verify(cm, times(1)).sendMail(eq(pUser), eq(EmailType.USER_CREATED),
                ac.capture(), any(Marketplace.class));
        Object[] value = ac.getValue();
        assertEquals(pUser.getUserId(), value[0]);
        assertEquals("secret", value[1]);
        assertEquals("baseUrl", value[2]);
        assertEquals(String.valueOf(pUser.getKey()), value[3]);

    }

    @Test
    public void sendMailToCreatedUser_LDAP_ManagerRole_GivenMID_B9806()
            throws Exception {
        // Given a user with the service manager role
        addRole(pUser, UserRoleType.SERVICE_MANAGER);

        Marketplace marketplace = new Marketplace();
        marketplace.setMarketplaceId("test");

        // when sending a mail to the created user
        idSrv.sendMailToCreatedUser("secret", false, marketplace, pUser);

        // Both URLs must be contained
        verify(idSrv.cm, times(1)).getBaseUrl();
        verify(idSrv.cm, times(1)).getMarketplaceUrl(eq("test"));
    }

    @Test
    public void sendMailToCreatedUser_LDAP_ManagerRole_NoGivenMID_B9806()
            throws Exception {
        // Given a user with the service manager role
        addRole(pUser, UserRoleType.SERVICE_MANAGER);

        Marketplace marketplace = new Marketplace();
        marketplace.setMarketplaceId(null);

        // when sending a mail to the created user
        idSrv.sendMailToCreatedUser("secret", false, marketplace, pUser);

        // Only administration portal URL is contained
        verify(idSrv.cm, times(1)).getBaseUrl();
        verify(idSrv.cm, times(0)).getMarketplaceUrl((String) Mockito.isNull());
    }

    @Test
    public void sendMailToCreatedUser_LDAP_NoManagerRole_GivenMID_B9806()
            throws Exception {
        // Given a user with the service manager role
        addRole(pUser, UserRoleType.ORGANIZATION_ADMIN);

        Marketplace marketplace = new Marketplace();
        marketplace.setMarketplaceId("test");

        // when sending a mail to the created user
        idSrv.sendMailToCreatedUser("secret", false, marketplace, pUser);

        // Only administration portal URL
        verify(idSrv.cm, times(0)).getBaseUrl();
        verify(idSrv.cm, times(1)).getMarketplaceUrl(eq("test"));
    }

    @Test
    public void sendMailToCreatedUser_MarketplaceIdSet() throws Exception {
        // given a user with the service manager role
        addRole(pUser, UserRoleType.SERVICE_MANAGER);

        // and a marketplace with a marketplaceId set (marketplace portal)
        Marketplace marketplace = new Marketplace();
        marketplace.setMarketplaceId("myMarketplaceId");
        idSrv.sendMailToCreatedUser("secret", true, marketplace, pUser);

        // assert that mail should contain
        // the administration portal URL and the context URL
        verify(idSrv.cm, times(1)).getBaseUrl();
        verify(idSrv.cm, times(1)).getMarketplaceUrl(anyString());

        // verify that mail parameters are correct
        ArgumentCaptor<Object[]> ac = ArgumentCaptor.forClass(Object[].class);
        verify(cm, times(1)).sendMail(eq(pUser),
                eq(EmailType.USER_CREATED_WITH_MARKETPLACE), ac.capture(),
                any(Marketplace.class));
        Object[] value = ac.getValue();
        assertEquals(pUser.getUserId(), value[0]);
        assertEquals("secret", value[1]);
        assertEquals("baseUrl", value[2]);
        assertEquals("marketplaceUrl", value[3]);
        assertEquals(String.valueOf(pUser.getKey()), value[4]);

    }

    @Test
    public void sendMailToCreatedUser_SwitchedOff() throws Exception {
        SendMailControl.setSendMail(Boolean.FALSE);
        Marketplace mp = new Marketplace();

        idSrv.sendMailToCreatedUser("secret", true, mp, pUser);

        verifyZeroInteractions(idSrv.cm);
        assertEquals("secret", SendMailControl.getPassword());
        assertSame(mp, SendMailControl.getMarketplace());
    }

    @Test
    public void sendMailToCreatedUser_NoManagerRole_SAML_SP() throws Exception {
        // given a SAML_SP authentication mode and a user with no manager role
        doReturn(Boolean.TRUE).when(cs).isServiceProvider();

        // when sending a mail to the created user
        idSrv.sendMailToCreatedUser("secret", true, new Marketplace(), pUser);

        // then verify that mail parameters are correct
        verify(idSrv.cm, times(0)).getBaseUrl();
        verify(idSrv.cm, times(1)).getMarketplaceUrl(anyString());

        ArgumentCaptor<Object[]> ac = ArgumentCaptor.forClass(Object[].class);
        verify(cm, times(1)).sendMail(eq(pUser),
                eq(EmailType.USER_CREATED_SAML_SP), ac.capture(),
                any(Marketplace.class));
        Object[] value = ac.getValue();
        assertEquals(pUser.getUserId(), value[0]);
        assertEquals("marketplaceUrl", value[1]);
    }

    @Test
    public void sendMailToCreatedUser_ManagerRole_SAML_SP() throws Exception {
        // given a SAML_SP authentication mode and a user with manager role
        doReturn(Boolean.TRUE).when(cs).isServiceProvider();

        addRole(pUser, UserRoleType.SERVICE_MANAGER);

        // when sending a mail to the created user
        idSrv.sendMailToCreatedUser("secret", true, new Marketplace(), pUser);

        // then verify that mail parameters are correct
        verify(idSrv.cm, times(1)).getBaseUrlWithTenant(anyString());
        verify(idSrv.cm, times(0)).getMarketplaceUrl(anyString());

        ArgumentCaptor<Object[]> ac = ArgumentCaptor.forClass(Object[].class);
        verify(cm, times(1)).sendMail(eq(pUser),
                eq(EmailType.USER_CREATED_SAML_SP), ac.capture(),
                any(Marketplace.class));
        Object[] value = ac.getValue();
        assertEquals(pUser.getUserId(), value[0]);
    }

    @Test
    public void sendMailToCreatedUser_MarketplaceIdSet_SAML_SP()
            throws Exception {
        // given a SAML_SP authentication mode and a marketplace with the
        // markeplace id set

        doReturn(Boolean.TRUE).when(cs).isServiceProvider();

        addRole(pUser, UserRoleType.SERVICE_MANAGER);

        Marketplace marketplace = new Marketplace();
        marketplace.setMarketplaceId("myMarketplaceId");

        // when sending a mail to the created user
        idSrv.sendMailToCreatedUser("secret", true, marketplace, pUser);

        // then verify that mail parameters are correct
        verify(idSrv.cm, times(1)).getBaseUrlWithTenant(anyString());
        verify(idSrv.cm, times(1)).getMarketplaceUrl(anyString());

        ArgumentCaptor<Object[]> ac = ArgumentCaptor.forClass(Object[].class);
        verify(cm, times(1)).sendMail(eq(pUser),
                eq(EmailType.USER_CREATED_WITH_MARKETPLACE_SAML_SP),
                ac.capture(), any(Marketplace.class));
        Object[] value = ac.getValue();
        assertEquals(pUser.getUserId(), value[0]);
        assertEquals("baseUrl", value[1]);
        assertEquals("marketplaceUrl", value[2]);
    }

    private static void addRole(PlatformUser pu, UserRoleType role) {
        RoleAssignment ra = new RoleAssignment();
        ra.setUser(pu);
        ra.setRole(new UserRole(role));
        pu.getAssignedRoles().add(ra);

    }

    private void setupIgnoreRoleCheck() throws Exception {
        doNothing().when(idSrv).checkRoleConstrains(any(PlatformUser.class),
                any(UserRoleType.class));
        setupDsForRefreshingUserRoles();
    }

    private ArgumentMatcher<Object[]> getArrayContainsStringMatcher(
            final String s) {
        return new ArgumentMatcher<Object[]>() {

            @Override
            public boolean matches(Object argument) {
                return ((String) ((Object[]) argument)[0]).contains(s);
            }
        };
    }

    private ArgumentMatcher<Object[]> getArrayNotContainsStringMatcher(
            final String s) {
        return new ArgumentMatcher<Object[]>() {

            @Override
            public boolean matches(Object argument) {
                return !((String) ((Object[]) argument)[0]).contains(s);
            }
        };
    }

    private VOUserDetails getVOUserDetails(Organization org, String userId) {
        VOUserDetails user = new VOUserDetails();
        user.setEMail(org.getEmail());
        user.setOrganizationId(org.getOrganizationId());
        user.setLocale(org.getLocale());
        user.setUserId(userId);
        return user;
    }

    private void verifyMailParameters() throws Exception {
        ArgumentCaptor<Object[]> ac = ArgumentCaptor.forClass(Object[].class);
        verify(cm, times(1)).sendMail(eq(pUser),
                eq(EmailType.USER_CONFIRM_ACKNOWLEDGE), ac.capture(),
                any(Marketplace.class));
        Object[] value = ac.getValue();
        assertEquals(pUser.getUserId(), value[1]);
        assertEquals(String.valueOf(pUser.getKey()), value[2]);
    }

    private void setupDsForRefreshingUserRoles() throws Exception {
        doAnswer(new Answer<Void>() {

            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                Object object = invocation.getArguments()[0];
                if (object instanceof RoleAssignment) {
                    persistedRoleAssignments.add((RoleAssignment) object);
                }
                return null;
            }
        }).when(idSrv.dm).persist(any(RoleAssignment.class));

        doAnswer(new Answer<Void>() {

            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                Object object = invocation.getArguments()[0];
                if (object instanceof PlatformUser) {
                    ((PlatformUser) object).getAssignedRoles()
                            .addAll(persistedRoleAssignments);
                }
                return null;
            }
        }).when(idSrv.dm).refresh(any(PlatformUser.class));
    }
}
