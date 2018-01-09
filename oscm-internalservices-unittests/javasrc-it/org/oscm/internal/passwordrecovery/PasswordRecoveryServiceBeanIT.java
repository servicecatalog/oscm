/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 2013-2-5                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.passwordrecovery;

import static org.junit.Assert.assertEquals;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;

import org.junit.After;
import org.junit.Test;

import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.converter.ParameterEncoder;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.ConfigurationSetting;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Session;
import org.oscm.identityservice.bean.IdentityServiceBean;
import org.oscm.sessionservice.bean.SessionManagementStub;
import org.oscm.test.EJBTestBase;
import org.oscm.test.MailDetails;
import org.oscm.test.data.Marketplaces;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.PlatformUsers;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.CommunicationServiceStub;
import org.oscm.test.stubs.ConfigurationServiceStub;
import org.oscm.types.constants.Configuration;
import org.oscm.types.enumtypes.EmailType;
import org.oscm.internal.types.enumtypes.ConfigurationKey;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.UserAccountStatus;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.MailOperationException;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;

/**
 * Container-based unit test for the password recovery service.
 * 
 * @author Mao
 */
@SuppressWarnings("boxing")
public class PasswordRecoveryServiceBeanIT extends EJBTestBase {

    private static final String MP_ID = "TEST";
    private static final String MP_ID2 = "fd454b47";
    private static final String ORGANIZATION_ID = "SUPPLIER";
    private static final String ORGANIZATION_ID_2 = "CUSTOMER";
    private static final String USER_ID = "Admin";
    private static final String USER_ID_2 = "Admin2";
    private static final String BASE_URL_WITH_SLASH = "BASE_URL//";
    private Organization po = null;

    private final List<Session> sessionList = new ArrayList<Session>();

    private DataService mgr;
    private PasswordRecoveryService pwdRecoveryMgmt;
    private ConfigurationServiceLocal cfg;
    private final List<MailDetails<PlatformUser>> sendedMails = new LinkedList<MailDetails<PlatformUser>>();

    @Override
    protected void setup(TestContainer container) throws Exception {
        container.enableInterfaceMocking(true);
        container.addBean(new DataServiceBean());
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new CommunicationServiceStub() {
            @Override
            public void sendMail(PlatformUser recipient, EmailType type,
                    Object[] params, Marketplace marketplace)
                    throws MailOperationException {
                sendedMails.add(new MailDetails<PlatformUser>(recipient, type,
                        params));
            }
        });
        container.addBean(new SessionManagementStub() {
            @Override
            public List<Session> getSessionsForUserKey(long platformUserKey) {
                return sessionList;
            }
        });
        container.addBean(new IdentityServiceBean());
        container.addBean(new PasswordRecoveryServiceBean());

        mgr = container.get(DataService.class);
        pwdRecoveryMgmt = container.get(PasswordRecoveryService.class);
        cfg = container.get(ConfigurationServiceLocal.class);
        setUpDirServerStub(cfg);

        createMarketplace();
        setupUsers();
    }

    @After
    public void cleanUpSendedMail() {
        sendedMails.clear();
    }

    @Test
    public void startPasswordRecovery_ValidInterval() throws Exception {
        cfg.setConfigurationSetting(new ConfigurationSetting(
                ConfigurationKey.BASE_URL, Configuration.GLOBAL_CONTEXT,
                BASE_URL_WITH_SLASH));
        pwdRecoveryMgmt.startPasswordRecovery(USER_ID, null);
        pwdRecoveryMgmt.startPasswordRecovery(USER_ID_2, MP_ID);

        assertEquals(2, sendedMails.size());
        verifyPasswordRecoveryMails(0, 1, EmailType.RECOVERPASSWORD_CONFIRM_URL);
        verifyPasswordRecoveryMails(1, 1, EmailType.RECOVERPASSWORD_CONFIRM_URL);
    }

    @Test
    public void confirmPasswordRecoveryLink_ValidLink() throws Exception {
        pwdRecoveryMgmt.startPasswordRecovery(USER_ID, null);
        assertEquals(
                USER_ID,
                pwdRecoveryMgmt.confirmPasswordRecoveryLink(
                        getConfirmUrl(USER_ID, null), null));
    }

    @Test
    public void confirmPasswordRecoveryLink_ValidLink_Marketplace()
            throws Exception {
        pwdRecoveryMgmt.startPasswordRecovery(USER_ID_2, MP_ID2);
        assertEquals(
                USER_ID_2,
                pwdRecoveryMgmt.confirmPasswordRecoveryLink(
                        getConfirmUrl(USER_ID_2, MP_ID2), MP_ID2));
    }

    @Test
    public void completePasswordRecovery_Ok() throws Exception {
        boolean changePwd = pwdRecoveryMgmt.completePasswordRecovery(USER_ID,
                "newPassword");
        assertEquals(0, getPlatformUser(USER_ID).getPasswordRecoveryStartDate());
        assertEquals(UserAccountStatus.ACTIVE, getPlatformUser(USER_ID)
                .getStatus());
        assertEquals(1, sendedMails.size());
        assertEquals(true, changePwd);
    }

    /**
     * Encode the token.
     * 
     * @param userId
     * @return
     * @throws Exception
     */
    private String getConfirmUrl(final String userId, final String marketplaceId)
            throws Exception {
        return runTX(new Callable<String>() {
            @Override
            public String call() throws Exception {
                StringBuffer token = new StringBuffer();
                PlatformUser pUser = new PlatformUser();
                pUser.setUserId(userId);
                pUser = (PlatformUser) mgr.find(pUser);
                String[] urlParam = new String[3];
                urlParam[0] = pUser.getUserId();
                urlParam[1] = Long.toString(pUser
                        .getPasswordRecoveryStartDate());
                urlParam[2] = (marketplaceId == null) ? "" : marketplaceId;
                token.append(URLEncoder.encode(
                        ParameterEncoder.encodeParameters(urlParam), "UTF-8"));
                token.append("&et");
                return token.toString();
            }
        });
    }

    /**
     * verify if the passwordRecoveryStartDate or User status has been changed
     */
    private PlatformUser getPlatformUser(final String userId) throws Exception {
        return runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                PlatformUser pUser = new PlatformUser();
                pUser.setUserId(userId);
                pUser = (PlatformUser) mgr.find(pUser);
                return pUser;
            }
        });
    }

    /**
     * verify if the correct mail has been send
     */
    private void verifyPasswordRecoveryMails(int index, int paramsCount,
            EmailType mailType) {
        assertEquals(mailType, sendedMails.get(index).getEmailType());
        assertEquals(paramsCount, sendedMails.get(index).getParams().length);
    }

    /**
     * create marketplace with id: MP_ID
     * 
     * @throws Exception
     */
    private void createMarketplace() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                // create marketplace
                createOrganizationRoles(mgr);
                po = Organizations.createOrganization(mgr,
                        OrganizationRoleType.PLATFORM_OPERATOR,
                        OrganizationRoleType.MARKETPLACE_OWNER);
                Marketplaces.createGlobalMarketplace(po, MP_ID, mgr);
                mgr.flush();
                return null;
            }
        });
    }

    /**
     * create organizations and users for test
     * 
     * @return
     * @throws Exception
     */
    private Void setupUsers() throws Exception {
        return runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                // create supplier
                Organization org = new Organization();
                org = createOrganization(ORGANIZATION_ID);
                long validTime = System.currentTimeMillis() - 300020;
                Organizations.grantOrganizationRoles(mgr, org,
                        OrganizationRoleType.SUPPLIER);
                // create service manager
                PlatformUser user = new PlatformUser();
                user = createPlatformUser(USER_ID, org,
                        UserAccountStatus.ACTIVE, false, validTime);
                org.addPlatformUser(user);
                PlatformUsers.grantAdminRole(mgr, user);
                PlatformUsers.grantRoles(mgr, user,
                        UserRoleType.SERVICE_MANAGER);

                // create customer
                org = createOrganization(ORGANIZATION_ID_2);
                Organizations.grantOrganizationRoles(mgr, org,
                        OrganizationRoleType.CUSTOMER);
                // create simple user
                user = createPlatformUser(USER_ID_2, org,
                        UserAccountStatus.ACTIVE, false, 0);
                org.addPlatformUser(user);
                return null;
            }
        });
    }

    /**
     * create and persist organization
     * 
     * @param organizationId
     * @return
     * @throws NonUniqueBusinessKeyException
     */
    private Organization createOrganization(String organizationId)
            throws NonUniqueBusinessKeyException {
        Organization org = new Organization();
        org.setOrganizationId(organizationId);
        org.setName("The supplier organization");
        org.setAddress("my address is a very long string, which is stored in the database \n with line delimiters\n.");
        org.setEmail("organization@organization.com");
        org.setPhone("012345/678");
        org.setLocale(Locale.ENGLISH.toString());
        org.setCutOffDay(1);
        mgr.persist(org);
        return org;
    }

    /**
     * create and persist platformUser
     * 
     * @param userId
     * @param org
     * @param status
     * @param ldapActive
     * @param validTime
     * @return
     * @throws NonUniqueBusinessKeyException
     */
    private PlatformUser createPlatformUser(String userId, Organization org,
            UserAccountStatus status, boolean ldapActive, long validTime)
            throws NonUniqueBusinessKeyException {
        PlatformUser user = new PlatformUser();
        user.setUserId(userId);
        user.setEmail("someMail@somehost.de");
        user.setOrganization(org);
        user.setStatus(status);
        user.setLocale(Locale.ENGLISH.toString());
        user.getOrganization().setRemoteLdapActive(ldapActive);
        user.setPasswordRecoveryStartDate(validTime);
        mgr.persist(user);
        return user;
    }

}
