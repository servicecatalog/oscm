/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.identityservice.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.ejb.EJBException;
import javax.naming.NamingException;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.oscm.communicationservice.data.SendMailStatus;
import org.oscm.communicationservice.local.CommunicationServiceLocal;
import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.converter.ParameterEncoder;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.ConfigurationSetting;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationReference;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.TriggerProcess;
import org.oscm.domobjects.enums.OrganizationReferenceType;
import org.oscm.encrypter.AESEncrypter;
import org.oscm.identityservice.local.ILdapResultMapper;
import org.oscm.identityservice.local.IdentityServiceLocal;
import org.oscm.identityservice.local.LdapAccessServiceLocal;
import org.oscm.identityservice.local.LdapConnector;
import org.oscm.identityservice.local.LdapSettingsManagementServiceLocal;
import org.oscm.internal.types.enumtypes.ConfigurationKey;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.SettingType;
import org.oscm.internal.types.enumtypes.UserAccountStatus;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.MailOperationException;
import org.oscm.internal.types.exception.UnsupportedOperationException;
import org.oscm.internal.vo.VOUserDetails;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.Marketplaces;
import org.oscm.test.data.OrganizationReferences;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.PlatformUsers;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.ConfigurationServiceStub;
import org.oscm.triggerservice.local.TriggerMessage;
import org.oscm.triggerservice.local.TriggerProcessMessageData;
import org.oscm.triggerservice.local.TriggerQueueServiceLocal;
import org.oscm.types.enumtypes.EmailType;

public class IdentityServiceBeanLdapWithDbIT extends EJBTestBase {

    private static final String ORGANIZATION_ID = "orgId";
    private static final String MP_ID = "mpId";

    private Organization supplier;
    private Organization customer;
    private DataService ds;
    private IdentityServiceBean idMgmt;
    private IdentityServiceLocal idMgmtLocal;
    private PlatformUser customerAdmin;
    private LdapAccessServiceLocal ldapService;
    private CommunicationServiceLocal cm;

    @Captor
    ArgumentCaptor<Object[]> ac;
    private VOUserDetails userToReturnByLdap;
    private LdapSettingsManagementServiceLocal ldapSettingsMock;
    private Set<SettingType> mappedLdapSettings = new HashSet<>();
    private Properties ldapOrgSettingsResolved = new Properties();

    private LdapConnector connectorMock;

    @SuppressWarnings("unchecked")
    @Override
    protected void setup(TestContainer container) throws Exception {
        AESEncrypter.generateKey();
        container.enableInterfaceMocking(true);
        MockitoAnnotations.initMocks(this);
        TriggerQueueServiceLocal triggerQS = mock(
                TriggerQueueServiceLocal.class);
        ConfigurationServiceLocal cs = mock(ConfigurationServiceLocal.class);
        ldapService = mock(LdapAccessServiceLocal.class);
        cm = mock(CommunicationServiceLocal.class);
        ldapSettingsMock = mock(LdapSettingsManagementServiceLocal.class);
        container.addBean(ldapSettingsMock);
        container.addBean(cm);
        container.addBean(cs);
        container.addBean(ldapService);
        container.addBean(triggerQS);
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new DataServiceBean());
        idMgmt = spy(new IdentityServiceBean());

        connectorMock = mock(LdapConnector.class);
        doReturn(connectorMock).when(idMgmt)
                .getLdapConnector(ldapOrgSettingsResolved);

        when(connectorMock.getAttrMap())
                .thenAnswer(new Answer<Map<SettingType, String>>() {

                    @Override
                    public Map<SettingType, String> answer(
                            InvocationOnMock invocation) throws Throwable {
                        final HashMap<SettingType, String> settingsMap = new HashMap<>();
                        settingsMap.put(SettingType.LDAP_ATTR_EMAIL, "");
                        return settingsMap;
                    }
                });
        container.addBean(idMgmt);

        doReturn(new SendMailStatus<PlatformUser>()).when(cm).sendMail(
                any(EmailType.class), any(Object[].class),
                any(Marketplace.class), any(PlatformUser[].class));

        doAnswer(new Answer<List<VOUserDetails>>() {
            @Override
            public List<VOUserDetails> answer(InvocationOnMock invocation)
                    throws Throwable {
                return Collections.singletonList(userToReturnByLdap);
            }
        }).when(ldapService).search(any(Properties.class), anyString(),
                anyString(), any(ILdapResultMapper.class), anyBoolean());

        doAnswer(new Answer<Set<SettingType>>() {
            @Override
            public Set<SettingType> answer(InvocationOnMock invocation)
                    throws Throwable {
                return mappedLdapSettings;
            }
        }).when(ldapSettingsMock).getMappedAttributes();
        doAnswer(new Answer<Properties>() {
            @Override
            public Properties answer(InvocationOnMock invocation)
                    throws Throwable {
                return ldapOrgSettingsResolved;
            }
        }).when(ldapSettingsMock).getOrganizationSettingsResolved(anyString());

        ArrayList<TriggerProcessMessageData> triggerResult = new ArrayList<>();
        triggerResult.add(new TriggerProcessMessageData(new TriggerProcess(),
                new TriggerMessage()));
        doReturn(triggerResult).when(triggerQS)
                .sendSuspendingMessages(anyListOf(TriggerMessage.class));

        when(cs.getConfigurationSetting(any(ConfigurationKey.class),
                anyString())).thenReturn(new ConfigurationSetting());

        ds = container.get(DataService.class);
        idMgmtLocal = container.get(IdentityServiceLocal.class);

        supplier = Organizations.createOrganization(ds,
                OrganizationRoleType.SUPPLIER);

        customer = Organizations.createCustomer(ds, supplier, true);
        customerAdmin = Organizations.createUserForOrg(ds, customer, true,
                "user1");
        Marketplaces.createGlobalMarketplace(supplier, MP_ID, ds);
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
    }

    /**
     * Test the creation of a org admin if ldap is active and the user name of
     * the desired username of the admin is not unique.
     * 
     * @throws Exception
     */
    @Test
    public void testCreateOrganizationAdmin_ldapNonUniqueId() throws Exception {
        final VOUserDetails user1 = initTestUser();
        user1.setRealmUserId(user1.getUserId());
        userToReturnByLdap = user1;

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                idMgmt.importLdapUsers(Collections.singletonList(user1), MP_ID);
                return null;
            }
        });

        final VOUserDetails user2 = initTestUser();
        user2.setRealmUserId(user2.getUserId());
        assertEquals(user1.getUserId(), user2.getUserId());
        userToReturnByLdap = user2;

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Organization org = ds.getReference(Organization.class,
                        customer.getKey());
                Marketplace mp = new Marketplace();
                mp.setMarketplaceId(MP_ID);
                idMgmtLocal.createOrganizationAdmin(user2, org, "newPassword",
                        Long.valueOf(12345),
                        (Marketplace) ds.getReferenceByBusinessKey(mp));
                return null;
            }
        });

        // import notification and confirm
        verifyMailParameters(3, user2.getEMail());
        verify(connectorMock, times(1))
                .ensureAllMandatoryLdapPropertiesPresent();
    }

    /**
     * There the creation of an org admin. The user name of the admin is not
     * unique as well as the email address as user id
     * 
     * @throws Exception
     */
    @Test
    public void testCreateOrganizationAdmin_ldapNonUniqueIdAndEmail()
            throws Exception {
        final VOUserDetails user1 = initTestUser();
        user1.setRealmUserId(user1.getUserId());
        userToReturnByLdap = user1;
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                idMgmt.importLdapUsers(Collections.singletonList(user1), MP_ID);
                return null;
            }
        });

        final VOUserDetails user11 = initTestUser();
        user11.setUserId(user1.getEMail());
        userToReturnByLdap = user11;
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                idMgmt.importLdapUsers(Collections.singletonList(user11),
                        MP_ID);
                return null;
            }
        });

        final VOUserDetails user2 = initTestUser();
        final String userid = user2.getUserId();
        user2.setRealmUserId(userid);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Organization org = ds.getReference(Organization.class,
                        customer.getKey());
                Marketplace mp = new Marketplace();
                mp.setMarketplaceId(MP_ID);
                idMgmtLocal.createOrganizationAdmin(user2, org, "newPassword",
                        Long.valueOf(12345),
                        (Marketplace) ds.getReferenceByBusinessKey(mp));
                return null;
            }
        });

        // import notification and confirm
        verifyMailParameters(4, userid + "@" + user2.getOrganizationId());
    }

    @Test
    public void testSyncUserWithLdap() throws Exception {
        addLdapOrganizationSetting();
        final VOUserDetails user1 = initTestUser();
        user1.setEMail("peter.pock@est.fujitsu.de");
        user1.setRealmUserId(user1.getUserId());
        userToReturnByLdap = user1;
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                idMgmt.importLdapUsers(Collections.singletonList(user1), MP_ID);
                return null;
            }
        });

        // a notify call synchronizes the user data with the LDAP system data,
        // so the mail address should be updated
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                userToReturnByLdap = retrieveUser(user1.getUserId());
                userToReturnByLdap.setEMail("peter.pock@est.fujitsu.com");
                idMgmt.notifyOnLoginAttempt(userToReturnByLdap, true);
                return null;
            }
        });

        verify(connectorMock, times(2))
                .ensureAllMandatoryLdapPropertiesPresent();
        assertEquals("peter.pock@est.fujitsu.com",
                retrieveUser(user1.getUserId()).getEMail());
    }

    @SuppressWarnings("unchecked")
    public void testSyncUserWithLdapNamingException() throws Exception {
        addLdapOrganizationSetting();
        VOUserDetails user1 = initTestUser();
        user1.setEMail("peter.pock@est.fujitsu.de");
        user1.setRealmUserId(user1.getUserId());
        userToReturnByLdap = user1;
        idMgmt.importLdapUsers(Collections.singletonList(user1), MP_ID);
        user1 = retrieveUser(user1.getUserId());

        doThrow(new NamingException()).when(ldapService).search(
                any(Properties.class), anyString(), anyString(),
                any(ILdapResultMapper.class), anyBoolean());
        try {
            idMgmt.notifyOnLoginAttempt(user1, true);
            fail();
        } catch (EJBException ex) {
            verify(connectorMock, times(1))
                    .ensureAllMandatoryLdapPropertiesPresent();
        }
    }

    @Test(expected = UnsupportedOperationException.class)
    public void changePassword_LDAPUsed() throws Exception {
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    idMgmt.changePassword("bla", "blabla");
                    return null;
                }
            });
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    @Test(expected = UnsupportedOperationException.class)
    public void requestResetOfUserPassword_LDAPUsed() throws Exception {
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    idMgmt.requestResetOfUserPassword(
                            idMgmt.getCurrentUserDetails(), null);
                    return null;
                }
            });
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    @Test(expected = UnsupportedOperationException.class)
    public void createUser_LDAPUsed() throws Exception {
        try {
            final VOUserDetails userToCreate = new VOUserDetails();
            userToCreate.setUserId("newUser");
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    idMgmt.createUser(userToCreate, Collections.singletonList(
                            UserRoleType.ORGANIZATION_ADMIN), null);
                    return null;
                }
            });
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    @Test(expected = UnsupportedOperationException.class)
    public void updateUser_LDAPUsedMappedAttributeViolation() throws Exception {
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    final VOUserDetails currentUserDetails = idMgmt
                            .getCurrentUserDetails();
                    mappedLdapSettings.add(SettingType.LDAP_ATTR_FIRST_NAME);
                    currentUserDetails.setFirstName("another first name");
                    idMgmt.updateUser(currentUserDetails);
                    return null;
                }
            });
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    @Test
    public void updateUser_LDAPUsedMappedAttributeNoViolation()
            throws Exception {
        mappedLdapSettings.add(SettingType.LDAP_ATTR_FIRST_NAME);
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                VOUserDetails currentUserDetails = idMgmt
                        .getCurrentUserDetails();
                idMgmt.updateUser(currentUserDetails);
                return null;
            }
        });
    }

    @Test
    public void deleteUser_LDAPUsed() throws Exception {
        doNothing().when(idMgmt).deletePlatformUser(any(PlatformUser.class),
                anyBoolean(), anyBoolean(), any(Marketplace.class));
        final VOUserDetails currentUserDetails = runTX(
                new Callable<VOUserDetails>() {
                    @Override
                    public VOUserDetails call() throws Exception {
                        VOUserDetails user = idMgmt.getCurrentUserDetails();
                        idMgmt.deleteUser(user, null);
                        return user;
                    }
                });
        ArgumentCaptor<PlatformUser> user = ArgumentCaptor
                .forClass(PlatformUser.class);
        verify(idMgmt, times(1)).deletePlatformUser(user.capture(), eq(false),
                eq(false), (Marketplace) eq(null));
        assertEquals(currentUserDetails.getKey(), user.getValue().getKey());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void lockUserAccount_LDAPUsed() throws Exception {
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    idMgmt.lockUserAccount(idMgmt.getCurrentUserDetails(),
                            UserAccountStatus.LOCKED, null);
                    return null;
                }
            });
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    @Test(expected = UnsupportedOperationException.class)
    public void unlockUserAccount_LDAPUsed() throws Exception {
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    idMgmt.unlockUserAccount(idMgmt.getCurrentUserDetails(),
                            null);
                    return null;
                }
            });
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    @Test(expected = UnsupportedOperationException.class)
    public void createOnBehalfUser_LDAPUsed() throws Exception {
        try {
            PlatformUser tpAdmin = runTX(new Callable<PlatformUser>() {
                @Override
                public PlatformUser call() throws Exception {
                    Organization tp = Organizations.createOrganization(ds,
                            OrganizationRoleType.TECHNOLOGY_PROVIDER);
                    PlatformUser tpAdmin = PlatformUsers.createAdmin(ds,
                            "tpAdmin", tp);
                    PlatformUsers.grantRoles(ds, tpAdmin,
                            UserRoleType.TECHNOLOGY_MANAGER);
                    OrganizationReference ref = OrganizationReferences
                            .addReference(tp,
                                    ds.getReference(Organization.class,
                                            customer.getKey()),
                                    OrganizationReferenceType.ON_BEHALF_ACTING);
                    ds.persist(ref);
                    return tpAdmin;
                }
            });
            container.login(tpAdmin.getKey(), ROLE_TECHNOLOGY_MANAGER);
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    idMgmt.createOnBehalfUser(
                            customerAdmin.getOrganization().getOrganizationId(),
                            "newPWD");
                    return null;
                }
            });
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    private void addLdapOrganizationSetting() throws Exception {
        ldapOrgSettingsResolved.setProperty(SettingType.LDAP_URL.name(),
                "ldap://estinfra1.lan.est.fujitsu.de:389");
        ldapOrgSettingsResolved.setProperty(SettingType.LDAP_BASE_DN.name(),
                "ou=people,dc=est,dc=fujitsu,dc=de");
        ldapOrgSettingsResolved.setProperty(SettingType.LDAP_ATTR_EMAIL.name(),
                "scalixEmailAddress");
    }

    /**
     * Retrieves the user from the system.
     * 
     * @param userId
     *            The user to be retrieved. The business key field(s) must be
     *            set.
     * @return The value object reflecting the current values.
     */
    private VOUserDetails retrieveUser(String userId) throws Exception {
        List<VOUserDetails> users = runTX(new Callable<List<VOUserDetails>>() {
            @Override
            public List<VOUserDetails> call() throws Exception {
                return idMgmt.getUsersForOrganization();
            }
        });
        for (VOUserDetails ud : users) {
            if (ud.getUserId().equals(userId)) {
                return ud;
            }
        }
        return null;
    }

    private void verifyMailParameters(int expectedMailCount,
            final String expectedUserId) throws MailOperationException,
            UnsupportedEncodingException, Exception {
        verify(cm, Mockito.times(expectedMailCount)).sendMail(
                any(PlatformUser.class), any(EmailType.class), ac.capture(),
                any(Marketplace.class));

        Object[] latestParams = ac.getValue();
        assertEquals(1, latestParams.length);
        String encodedParam = (String) latestParams[0];

        // b7856: check if the dummy postfix exist
        assertTrue(encodedParam.endsWith("&et"));
        encodedParam = encodedParam.substring(0, encodedParam.indexOf("&et"));
        encodedParam = encodedParam.substring(encodedParam.indexOf("enc=") + 4);

        encodedParam = URLDecoder.decode(encodedParam, "UTF-8");
        String[] decodedParam = ParameterEncoder.decodeParameters(encodedParam);
        assertEquals(4, decodedParam.length);
        assertEquals(customer.getOrganizationId(), decodedParam[0]);

        // Here we expect the newly created user id
        assertEquals(expectedUserId, decodedParam[1]);
        assertEquals(MP_ID, decodedParam[2]);
        assertEquals("12345", decodedParam[3]);

        PlatformUser createdUser = runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                PlatformUser p = new PlatformUser();
                p.setUserId(expectedUserId);
                PlatformUser createdUser = ds.find(p);
                load(createdUser);
                load(createdUser.getOrganization());
                return createdUser;
            }
        });

        assertNotNull(createdUser);
        assertEquals(expectedUserId, createdUser.getUserId());
        assertEquals(customer.getOrganizationId(),
                createdUser.getOrganization().getOrganizationId());

        assertTrue(createdUser.isOrganizationAdmin());
        assertEquals(UserAccountStatus.ACTIVE, createdUser.getStatus());
    }

    private VOUserDetails initTestUser() {
        VOUserDetails user = new VOUserDetails();
        user.setOrganizationId(ORGANIZATION_ID);
        user.setUserId(ORGANIZATION_ID + "usera");
        user.setEMail("someMail@somehost.com");
        user.setFirstName("Harald");
        user.setLastName("Wilhelm");
        user.setLocale(Locale.ENGLISH.toString());
        return user;
    }
}
