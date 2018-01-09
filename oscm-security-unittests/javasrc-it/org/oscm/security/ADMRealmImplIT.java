/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: jaeger                                                       
 *                                                                              
 *  Creation Date: 08.11.2011                                                      
 *                                                                              
 *  Completion Time: 08.11.2011                                           
 *                                                                              
 *******************************************************************************/

package org.oscm.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.matches;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

import javax.security.auth.login.LoginException;
import javax.sql.DataSource;

import org.junit.Test;
import org.oscm.authorization.PasswordHash;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.domobjects.ConfigurationSetting;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.internal.types.enumtypes.ConfigurationKey;
import org.oscm.internal.types.enumtypes.UserAccountStatus;
import org.oscm.saml2.api.AssertionConsumerService;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.Organizations;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.ejb.TestDataSources;
import org.oscm.test.stubs.ConfigurationServiceStub;
import org.oscm.types.constants.Configuration;

public class ADMRealmImplIT extends EJBTestBase {

    private ADMRealmImpl realm;
    private DataSource dataSource;
    private DataServiceBean dm;
    private PlatformUser user;
    private UserQuery userQuery;
    private OrganizationSettingQuery organizationSettingQuery;
    private AssertionConsumerService acs;
    private static final String PWD = "password";
    private static final String ANY_KEY = "12345";
    private static final String WRONG_PASSWORD = "WrongPassword";
    private static final String WS_PASSWORD = "WSDummyPassword";
    private static final String REQUEST_ID = "ID_0123456789012345678901234567890123456789";
    private static final String SAML_RESPONSE = "SAMLResponse";
    private static final String TENANT_ID = "11111111";
    private static final String UI_PASSWORD = "UI" + REQUEST_ID + TENANT_ID
            + SAML_RESPONSE;
    public static final String tenantID = "";

    @Override
    protected void setup(TestContainer container) throws Exception {
        container.login(1);
        container.addBean(new ConfigurationServiceStub());
        dm = new DataServiceBean();
        container.addBean(dm);
        realm = spy(new ADMRealmImpl(mock(Logger.class)));

        dataSource = TestDataSources.get("oscm-domainobjects").getDataSource();

        user = runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                Organization org = Organizations.createOrganization(dm);
                dm.flush();
                PlatformUser user = Organizations.createUserForOrg(dm, org,
                        true, "some_username");
                user.setPasswordHash(PasswordHash
                        .calculateHash(user.getPasswordSalt(), PWD));
                return user;
            }
        });

        userQuery = spy(realm.getUserQuery(String.valueOf(user.getKey())));
        doReturn(userQuery).when(realm)
                .getUserQuery(String.valueOf(user.getKey()));

        organizationSettingQuery = spy(
                realm.getOrganizationSettingQuery(userQuery));
        doReturn(organizationSettingQuery).when(realm)
                .getOrganizationSettingQuery(any(UserQuery.class));
        doReturn(new Properties()).when(organizationSettingQuery)
                .getProperties();
        doNothing().when(organizationSettingQuery).execute();
        doReturn("").when(realm).findAndBind(any(Properties.class), anyString(),
                anyString(), anyString());
        acs = mock(AssertionConsumerService.class);
        doReturn(acs).when(realm).getAssertionConsumerService(
                any(AuthenticationModeQuery.class));
        doNothing().when(acs).validateResponse(anyString(), anyString(),
                eq(tenantID));
    }

    @Test
    public void authenticate_successful() throws Exception {
        realm.authenticateUser(String.valueOf(user.getKey()), PWD);
        validateUserSettings(0, UserAccountStatus.ACTIVE, 1);
    }

    @Test(expected = LoginException.class)
    public void authenticate_WrongUser() throws Exception {
        try {
            realm.authenticateUser("wrong", PWD);
        } catch (LoginException e) {
            assertEquals("Login failed for invalid user key 'wrong'",
                    e.getMessage());
            throw e;
        }
        validateUserSettings(1, UserAccountStatus.ACTIVE, 1);
    }

    @Test
    public void authenticate_unsuccessfulIncreaseCounter() throws Exception {
        try {
            realm.authenticateUser(String.valueOf(user.getKey()), "wrongPWD");
            fail("Authentication must have failed");
        } catch (LoginException e) {
            validateUserSettings(1, UserAccountStatus.ACTIVE, 1);
        }
    }

    @Test
    public void authenticate_userLocked() throws Exception {
        doReturn(UserAccountStatus.LOCKED.name()).when(userQuery).getStatus();

        try {
            realm.authenticateUser(String.valueOf(user.getKey()), "");
            fail("Authentication must have failed");
        } catch (LoginException e) {
            assertEquals(
                    "Login for user '" + user.getKey()
                            + "' failed as the user account is locked.",
                    e.getMessage());
            validateUserSettings(0, UserAccountStatus.ACTIVE, 1);
        }
    }

    @Test
    public void authenticate_lockingScenario() throws Exception {
        for (int i = 0; i < 3; i++) {
            try {
                realm.authenticateUser(String.valueOf(user.getKey()),
                        "wrongPWD");
                fail("Authentication must have failed");
            } catch (LoginException e) {
            }
        }
        validateUserSettings(3, UserAccountStatus.LOCKED_FAILED_LOGIN_ATTEMPTS,
                1);
    }

    @Test
    public void authenticate_lockingScenarioThenValidAttempt()
            throws Exception {
        for (int i = 0; i < 3; i++) {
            try {
                realm.authenticateUser(String.valueOf(user.getKey()),
                        "wrongPWD");
                fail("Authentication must have failed");
            } catch (LoginException e) {

            }
        }
        validateUserSettings(3, UserAccountStatus.LOCKED_FAILED_LOGIN_ATTEMPTS,
                1);
        try {
            realm.authenticateUser(String.valueOf(user.getKey()), PWD);
            fail("Authentication must have failed");
        } catch (LoginException e) {
            validateUserSettings(3,
                    UserAccountStatus.LOCKED_FAILED_LOGIN_ATTEMPTS, 1);
        }
    }

    @Test
    public void authenticate_lockingScenarioNonDefaultMaxAttempts()
            throws Exception {
        final int max_tries = 10;
        initConfigSetting(max_tries);
        for (int i = 0; i < max_tries; i++) {
            try {
                realm.authenticateUser(String.valueOf(user.getKey()),
                        "wrongPWD");
                fail("Authentication must have failed");
            } catch (LoginException e) {
                if (i != (max_tries - 1)) {
                    // ensure that state isn't set before reaching max count
                    validateUserSettings(i + 1, UserAccountStatus.ACTIVE, 1);
                }
            }
        }
        validateUserSettings(10, UserAccountStatus.LOCKED_FAILED_LOGIN_ATTEMPTS,
                1);
    }

    @Test
    public void authenticate_successfulResetCounter() throws Exception {
        try {
            realm.authenticateUser(String.valueOf(user.getKey()), "wrongPWD");
            fail("Authentication must have failed");
        } catch (LoginException e) {
            validateUserSettings(1, UserAccountStatus.ACTIVE, 1);
        }
        realm.authenticateUser(String.valueOf(user.getKey()), PWD);
        validateUserSettings(0, UserAccountStatus.ACTIVE, 1);
    }

    @Test
    public void authenticate_ldapManaged_noUserFoundInLdap() throws Exception {
        doReturn(Boolean.TRUE).when(userQuery).isRemoteLdapActive();
        doReturn(null).when(userQuery).getRealmUserId();

        try {
            realm.authenticateUser(String.valueOf(user.getKey()), "");
            fail("Authentication must have failed");
        } catch (LoginException e) {
            validateUserSettings(0, UserAccountStatus.ACTIVE, 1);
        }
    }

    @Test
    public void authenticate_ldapManaged_userLocked() throws Exception {
        doReturn(Boolean.TRUE).when(userQuery).isRemoteLdapActive();
        doReturn(UserAccountStatus.LOCKED.name()).when(userQuery).getStatus();

        try {
            realm.authenticateUser(String.valueOf(user.getKey()), "");
            fail("Authentication must have failed");
        } catch (LoginException e) {
            assertEquals(
                    "Login for user '" + user.getKey()
                            + "' failed as the user account is locked.",
                    e.getMessage());
            validateUserSettings(0, UserAccountStatus.ACTIVE, 1);
        }
    }

    @Test
    public void getGroupNames_GoodCase() throws Exception {
        List<String> list = Collections
                .list(realm.getGroupNames(String.valueOf(user.getKey())));

        assertTrue(list.contains(ADMRealmImpl.GROUP_USER));
        assertTrue(list.contains(ADMRealmImpl.GROUP_ADMIN));
    }

    @Test(expected = SQLException.class)
    public void getGroupNames_WrongUserKey() throws Exception {
        try {
            realm.getGroupNames("wrong");
        } catch (SQLException e) {
            assertEquals("PlatformUser with user key 'wrong' not found.",
                    e.getMessage());
            throw e;
        }
    }

    @Test
    public void handleUICaller() throws Exception {

        // when
        realm.handleUICaller(ANY_KEY, UI_PASSWORD,
                mock(AuthenticationModeQuery.class));

        // then
        verify(acs, times(1)).validateResponse(SAML_RESPONSE, REQUEST_ID,
                TENANT_ID);
    }

    @Test
    public void handleWebServiceCaller_positive() throws LoginException {

        // given
        long wsPasswordAge = System.currentTimeMillis() - 1;
        String wsPassword = "WS" + wsPasswordAge;

        // then
        realm.handleWebServiceCaller(ANY_KEY, wsPassword);
    }

    @Test(expected = LoginException.class)
    public void handleWebServiceCaller_negative1() throws LoginException {

        realm.handleWebServiceCaller(ANY_KEY, WRONG_PASSWORD);
    }

    @Test(expected = LoginException.class)
    public void handleWebServiceCaller_negative2() throws LoginException {

        // given
        long wsPasswordAge = 600000;
        String wsPassword = "WS" + wsPasswordAge;

        // then
        realm.handleWebServiceCaller(ANY_KEY, wsPassword);
    }

    @Test
    public void handleOperatorClientCaller_positive() throws Exception {
        // given
        doNothing().when(realm).handleLoginAttempt(matches("1000"),
                matches("admin123"), any(UserQuery.class));

        // when
        realm.handleOperatorClientCaller("1000", "admin123", userQuery);

        // then: nothing
    }

    @Test(expected = LoginException.class)
    public void handleOperatorClientCaller_wrongKey() throws Exception {
        // given

        // when
        realm.handleOperatorClientCaller(ANY_KEY, PWD, userQuery);

        // then: exception
    }

    @Test
    public void handleSSO_UICaller() throws Exception {

        // given
        AuthenticationModeQuery authModeQuery = mock(
                AuthenticationModeQuery.class);
        mockCallerHandlers();

        // when
        realm.handleSSOLogin(ANY_KEY, UI_PASSWORD, authModeQuery, userQuery);

        // then
        verify(realm, times(1)).handleUICaller(ANY_KEY, UI_PASSWORD,
                authModeQuery);
    }

    @Test
    public void handleSSO_WebServiceCaller() throws Exception {

        // given
        AuthenticationModeQuery authModeQuery = mock(
                AuthenticationModeQuery.class);
        mockCallerHandlers();

        // when
        realm.handleSSOLogin(ANY_KEY, WS_PASSWORD, authModeQuery, userQuery);

        // then
        verify(realm, times(1)).handleWebServiceCaller(ANY_KEY, WS_PASSWORD);
    }

    @Test
    public void handleSSO_OperatorClientCaller() throws Exception {

        // given
        AuthenticationModeQuery authModeQuery = mock(
                AuthenticationModeQuery.class);
        mockCallerHandlers();

        // when
        realm.handleSSOLogin(ANY_KEY, WRONG_PASSWORD, authModeQuery, userQuery);

        // then
        verify(realm, times(1)).handleOperatorClientCaller(ANY_KEY,
                WRONG_PASSWORD, userQuery);
    }

    private void mockCallerHandlers() throws Exception {
        doNothing().when(realm).handleUICaller(anyString(), anyString(),
                any(AuthenticationModeQuery.class));
        doNothing().when(realm).handleWebServiceCaller(anyString(),
                anyString());
        doNothing().when(realm).handleOperatorClientCaller(anyString(),
                anyString(), any(UserQuery.class));
    }

    private void initConfigSetting(final int max_tries) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ConfigurationSetting cs = new ConfigurationSetting(
                        ConfigurationKey.MAX_NUMBER_LOGIN_ATTEMPTS,
                        Configuration.GLOBAL_CONTEXT,
                        String.valueOf(max_tries));
                dm.persist(cs);
                return null;
            }
        });
    }

    private void validateUserSettings(final int counter,
            final UserAccountStatus status, final int version)
            throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                PlatformUser storedUser = dm.getReference(PlatformUser.class,
                        user.getKey());
                assertEquals(counter, storedUser.getFailedLoginCounter());
                assertEquals(status, storedUser.getStatus());
                assertEquals(version, storedUser.getVersion());
                return null;
            }
        });
    }

}
