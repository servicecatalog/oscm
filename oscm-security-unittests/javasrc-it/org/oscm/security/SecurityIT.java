/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: pock                                                     
 *                                                                              
 *  Creation Date: 14.01.2010                                             
 *                                                                              
 *******************************************************************************/
package org.oscm.security;

import static org.oscm.test.Numbers.L10;
import static org.oscm.test.Numbers.L20;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.security.auth.login.LoginException;
import javax.sql.DataSource;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import org.oscm.test.BaseAdmUmTest;
import org.oscm.test.db.ITestDB;
import org.oscm.test.ejb.TestDataSources;
import org.oscm.internal.types.enumtypes.SettingType;
import org.oscm.internal.types.enumtypes.UserRoleType;

public class SecurityIT extends BaseAdmUmTest {

    private static final String WRONG_USER_KEY = "WRONG_USER_KEY";

    private static ITestDB TESTDB;
    private String exceptedLdapUserId = "";
    private boolean findAndBindWasCalled = false;

    private class TestADMRealmImpl extends ADMRealmImpl {

        private static final String REMOTE_DN = "REMOTE_DN";

        private String userKey;

        public TestADMRealmImpl(String userKey) {
            super(Logger.getLogger(SecurityIT.class.toString()));
            this.userKey = userKey;
        }

        @Override
        protected DataSource getDataSource() {
            return TESTDB.getDataSource();
        }

        @Override
        protected boolean bindAsUser(Properties ldapProps, String bindDN,
                String password) {
            if (!LDAP_PASSWORD.equals(password)) {
                return false;
            }
            if (ldapProps != null) {
                Object url = ldapProps.get(Context.PROVIDER_URL);
                if (LDAP_REMOTE_URL.equals(url)) {
                    return REMOTE_DN.equals(bindDN);
                } else if (CFG_LDAP_URL.equals(url)) {
                    return userKey.equals(bindDN);
                }
            }
            return false;
        }

        @Override
        protected String findAndBind(Properties ldapProperties, String baseDN,
                String searchFilter, String password) throws LoginException {
            findAndBindWasCalled = true;

            if (!LDAP_PASSWORD.equals(password)) {
                throw new LoginException("invalid password");
            }
            assertTrue(searchFilter.contains(exceptedLdapUserId));
            return "dnOfUser";
        }

        @Override
        protected String userSearch(Properties ldapProps, String baseDN,
                String filter) {
            if (ldapProps != null) {
                Object url = ldapProps.get(Context.PROVIDER_URL);
                if (LDAP_REMOTE_URL.equals(url)) {
                    return REMOTE_DN;
                } else if (CFG_LDAP_URL.equals(url)) {
                    if (!WRONG_USER_KEY.equals(userKey)) {
                        return userKey;
                    }
                }
            }
            return "";
        }

    };

    private final static String LDAP_ATTR_UID = "uid";
    private final static String LDAP_REMOTE_URL = "ldap://estinfra1.lan.est.fujitsu.de:389";
    private final static String LDAP_PASSWORD = "secret";
    private final static String CFG_LOCAL = "local";
    private final static String CFG_LDAP_BASE_DN = "ou=system";
    private final static String CFG_LDAP_URL = "ldap://localhost:10389";
    private final static String CFG_LDAP_CONTEXT_FACTORY = "com.sun.jndi.ldap.LdapCtxFactory";

    // the calculated hash for password "secret"
    private final static String PASSWORD_HASH = "66ac2a856661bd7db5a2215e3a54b1e7c94de5ef3c099295da4a344112e644de";

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        TESTDB = TestDataSources.get("oscm-domainobjects");
        TESTDB.initialize();

        String[] ins = {
                "INSERT INTO Organization (TKEY, VERSION, REGISTRATIONDATE, ORGANIZATIONID, REMOTELDAPACTIVE, CUTOFFDAY) VALUES (10, 0, 0, 'oid1', true, 1)",
                "INSERT INTO Organization (TKEY, VERSION, REGISTRATIONDATE, ORGANIZATIONID, CUTOFFDAY) VALUES (20, 0, 0, 'oid2', 1)",
                "INSERT INTO Organization (TKEY, VERSION, REGISTRATIONDATE, ORGANIZATIONID, CUTOFFDAY) VALUES (30, 0, 0, 'oid3', 1)",
                "INSERT INTO PlatformUser (TKEY, VERSION, ORGANIZATIONKEY, FAILEDLOGINCOUNTER, CREATIONDATE, STATUS, USERID, LOCALE, REALMUSERID) VALUES (10, 0, 10, 0, 0, 'ACTIVE', 'uid1', 'en', 'uid1')",
                "INSERT INTO PlatformUser (TKEY, VERSION, ORGANIZATIONKEY, FAILEDLOGINCOUNTER, CREATIONDATE, STATUS, USERID, LOCALE, REALMUSERID) VALUES (20, 0, 10, 0, 0, 'ACTIVE', 'uid2', 'en', 'ldapuid2')",
                "INSERT INTO PlatformUser (TKEY, VERSION, ORGANIZATIONKEY, FAILEDLOGINCOUNTER, CREATIONDATE, STATUS, USERID, LOCALE, PASSWORDSALT, PASSWORDHASH) VALUES (30, 0, 20, 0, 0, 'ACTIVE', 'uid3', 'en', 0, decode('"
                        + PASSWORD_HASH + "', 'hex'))",
                "INSERT INTO PlatformUser (TKEY, VERSION, ORGANIZATIONKEY, FAILEDLOGINCOUNTER, CREATIONDATE, STATUS, USERID, LOCALE, PASSWORDSALT, PASSWORDHASH) VALUES (40, 0, 30, 0, 0, 'ACTIVE', 'uid4', 'en', 0, decode('"
                        + PASSWORD_HASH + "', 'hex'))",
                "INSERT INTO UserRole (TKEY, ROLENAME, VERSION) VALUES (1, 'ORGANIZATION_ADMIN', 0)",
                "INSERT INTO UserRole (TKEY, ROLENAME, VERSION) VALUES (2, 'SERVICE_MANAGER', 0)",
                "INSERT INTO RoleAssignment (TKEY, VERSION, USER_TKEY,USERROLE_TKEY) VALUES (1, 0, 20, 1)",
                "INSERT INTO RoleAssignment (TKEY, VERSION, USER_TKEY,USERROLE_TKEY) VALUES (2, 0, 20, 2)",
                "INSERT INTO OrganizationSetting (TKEY, VERSION, ORGANIZATION_TKEY, SETTINGTYPE, SETTINGVALUE) VALUES (10, 0, 10, '"
                        + SettingType.LDAP_BASE_DN
                        + "', '"
                        + CFG_LDAP_BASE_DN
                        + "')",
                "INSERT INTO OrganizationSetting (TKEY, VERSION, ORGANIZATION_TKEY, SETTINGTYPE, SETTINGVALUE) VALUES (11, 0, 10, '"
                        + SettingType.LDAP_ATTR_UID
                        + "', '"
                        + LDAP_ATTR_UID
                        + "')",
                "INSERT INTO OrganizationSetting (TKEY, VERSION, ORGANIZATION_TKEY, SETTINGTYPE, SETTINGVALUE) VALUES (12, 0, 10, '"
                        + SettingType.LDAP_URL
                        + "', '"
                        + LDAP_REMOTE_URL
                        + "')",
                "INSERT INTO OrganizationSetting (TKEY, VERSION, ORGANIZATION_TKEY, SETTINGTYPE, SETTINGVALUE) VALUES (13, 0, 10, '"
                        + SettingType.LDAP_CONTEXT_FACTORY
                        + "', '"
                        + CFG_LDAP_CONTEXT_FACTORY + "')",
                "INSERT INTO OrganizationSetting (TKEY, VERSION, ORGANIZATION_TKEY, SETTINGTYPE, SETTINGVALUE) VALUES (14, 0, 10, '"
                        + SettingType.LDAP_CREDENTIALS + "', 'secret')",
                "INSERT INTO OrganizationSetting (TKEY, VERSION, ORGANIZATION_TKEY, SETTINGTYPE, SETTINGVALUE) VALUES (15, 0, 10, '"
                        + SettingType.LDAP_PRINCIPAL
                        + "', 'uid=admin,ou=system')",
                "INSERT INTO OrganizationSetting (TKEY, VERSION, ORGANIZATION_TKEY, SETTINGTYPE, SETTINGVALUE) VALUES (16, 0, 10, '"
                        + SettingType.LDAP_ATTR_REFERRAL + "', 'ignore')", };

        for (String stmt : ins) {
            execSQL(stmt);
        }

        System.setProperty("bss.nodename", CFG_LOCAL);
    }

    private static void execSQL(String statement) throws SQLException {
        Connection c = null;
        PreparedStatement p = null;

        try {
            c = TESTDB.getDataSource().getConnection();
            p = c.prepareStatement(statement);
            p.execute();
        } finally {
            if (p != null) {
                try {
                    p.close();
                } catch (SQLException e) {
                    // ignore
                }
            }
            if (c != null) {
                try {
                    c.close();
                } catch (SQLException e) {
                    // ignore
                }
            }
        }

    }

    @Test(expected = SQLException.class)
    public void AbstractQuery() throws SQLException {
        AbstractQuery query = new AbstractQuery(TESTDB.getDataSource()) {

            @Override
            protected String getStatement() {
                return "SELECT * FROM ";
            }

            @Override
            protected void mapResult(ResultSet rs) throws SQLException {
            }

            @Override
            protected void setParameters(PreparedStatement p)
                    throws SQLException {
            }

        };
        query.execute();
    }

    @Test
    public void testPasswordHash() throws Exception {
        AbstractQuery query = new AbstractQuery(TESTDB.getDataSource()) {

            @Override
            protected String getStatement() {
                return "SELECT PASSWORDHASH FROM PlatformUser WHERE TKEY = 30";
            }

            @Override
            protected void mapResult(ResultSet rs) throws SQLException {
                byte[] hash = rs.getBytes(1);
                assertEquals("0x" + PASSWORD_HASH,
                        String.format("0x%x", new BigInteger(hash)));
            }

            @Override
            protected void setParameters(PreparedStatement p)
                    throws SQLException {
            }

        };
        query.execute();
    }

    @Test
    public void testUserQuery() throws Exception {
        UserQuery userQuery;
        userQuery = new UserQuery(TESTDB.getDataSource(), "10");
        userQuery.execute();
        assertEquals("uid1", userQuery.getUserId());
        assertEquals(L10, userQuery.getOrgKey());
        assertTrue(userQuery.isRemoteLdapActive());
        assertEquals("uid1", userQuery.getRealmUserId());

        userQuery = new UserQuery(TESTDB.getDataSource(), "20");
        userQuery.execute();
        assertEquals("uid2", userQuery.getUserId());
        assertEquals("ldapuid2", userQuery.getRealmUserId());

        userQuery = new UserQuery(TESTDB.getDataSource(), "30");
        userQuery.execute();
        assertEquals("uid3", userQuery.getUserId());
        assertFalse(userQuery.isRemoteLdapActive());
        assertEquals(null, userQuery.getRealmUserId());
    }

    @Test
    public void testRoleQuery() throws Exception {
        RoleQuery roleQuery = new RoleQuery(TESTDB.getDataSource(), "10");
        roleQuery.execute();
        assertTrue(roleQuery.getRoleNames().isEmpty());

        roleQuery = new RoleQuery(TESTDB.getDataSource(), "20");
        roleQuery.execute();
        assertTrue(roleQuery.getRoleNames().contains(
                UserRoleType.ORGANIZATION_ADMIN.name()));
        assertTrue(roleQuery.getRoleNames().contains(
                UserRoleType.SERVICE_MANAGER.name()));
    }

    @Test
    public void testOrganizationSettingQuery() throws SQLException {
        OrganizationSettingQuery organizationSettingQuery;
        organizationSettingQuery = new OrganizationSettingQuery(
                TESTDB.getDataSource(), L10);
        organizationSettingQuery.execute();

        assertEquals(LDAP_ATTR_UID, organizationSettingQuery.getAttrUid());
        assertEquals(CFG_LDAP_BASE_DN, organizationSettingQuery.getBaseDN());
        assertEquals(LDAP_REMOTE_URL, organizationSettingQuery.getProperties()
                .get(Context.PROVIDER_URL));
    }

    @Test
    public void testMissingOrganizationSetting() throws SQLException {
        OrganizationSettingQuery organizationSettingQuery;
        organizationSettingQuery = new OrganizationSettingQuery(
                TESTDB.getDataSource(), L20);
        organizationSettingQuery.execute();

        assertNull(CFG_LDAP_BASE_DN, organizationSettingQuery.getBaseDN());
    }

    @Test
    public void testGetGroupNamesUser() throws Exception {
        String userKey = "10";
        ADMRealmImpl realmImpl = new TestADMRealmImpl(userKey);
        List<String> groups = Collections
                .list(realmImpl.getGroupNames(userKey));
        assertEquals(Arrays.asList(ADMRealmImpl.GROUPLIST_USER).size(), groups.size());
        assertEquals(Arrays.asList(ADMRealmImpl.GROUPLIST_USER).get(0), groups);
    }

    @Test(expected = SQLException.class)
    public void testGetGroupNamesUserStringUserKey() throws Exception {
        String userKey = "CLIENTCERT";
        ADMRealmImpl realmImpl = new TestADMRealmImpl(userKey);
        Collections.list(realmImpl.getGroupNames(null));
    }

    @Test
    public void testGetGroupNamesAdmin() throws Exception {
        String userKey = "20";
        ADMRealmImpl realmImpl = new TestADMRealmImpl(userKey);
        List<String> groups = Collections
                .list(realmImpl.getGroupNames(userKey));
        assertTrue(groups.contains(UserRoleType.ORGANIZATION_ADMIN.name()));
        assertTrue(groups.contains(UserRoleType.SERVICE_MANAGER.name()));
    }

    @Test(expected = SQLException.class)
    public void testGetGroupNamesUserMissing() throws Exception {
        String userKey = "50";
        ADMRealmImpl realmImpl = new TestADMRealmImpl(userKey);
        realmImpl.getGroupNames(userKey);
    }

    @Test
    public void testLoginRemoteUser() throws Exception {
        String userKey = "10";
        ADMRealmImpl realmImpl = new TestADMRealmImpl(userKey);
        List<String> groups = realmImpl.authenticateUser(userKey, LDAP_PASSWORD);
        assertTrue(ADMRealmImpl.GROUPLIST_USER.containsAll(groups));
    }

//    @Test
//    public void testLoginRemoteAdmin() throws Exception {
//        ADMRealmImpl realmImpl = new TestADMRealmImpl(userKey);
//        String[] groups = realmImpl.authenticateUser(userKey, LDAP_PASSWORD);
//        assertTrue(Arrays.asList(groups).contains(
//                UserRoleType.ORGANIZATION_ADMIN.name()));
//    }

    @Test
    public void testLoginLocalUser() throws Exception {
        String userKey = "30";
        ADMRealmImpl realmImpl = new TestADMRealmImpl(userKey);
        List<String> groups = realmImpl.authenticateUser(userKey, LDAP_PASSWORD);
        assertTrue(ADMRealmImpl.GROUPLIST_USER.containsAll(groups));
    }

    @Test
    public void testLoginLocalUserWrongPassword() {
        String userKey = "30";
        ADMRealmImpl realmImpl = new TestADMRealmImpl(userKey);
        try {
            realmImpl.authenticateUser(userKey, "wrong");
            fail();
        } catch (LoginException e) {
            assertTrue(e.getMessage().startsWith(
                    "Password verification failed for user '"));
        }
    }

    @Test
    public void testLoginUserMissing() {
        String userKey = "50";
        ADMRealmImpl realmImpl = new TestADMRealmImpl(userKey);
        try {
            realmImpl.authenticateUser(userKey, LDAP_PASSWORD);
            fail();
        } catch (LoginException e) {
            assertEquals("PlatformUser '50' not found.", e.getMessage());
        }
    }

    @Test
    public void testLoginWrongUserKey() {
        String userKey = WRONG_USER_KEY;
        ADMRealmImpl realmImpl = new TestADMRealmImpl(userKey);
        try {
            realmImpl.authenticateUser(userKey, LDAP_PASSWORD);
            fail();
        } catch (LoginException e) {
            assertTrue(e.getMessage().startsWith(
                    "Login failed for invalid user key '"));
        }
    }

    /**
     * Test the method in case the a LDAP system is active.
     */
    @Test
    public void testAuthenticateUserLDAP() throws LoginException {
        ADMRealmImpl realmImpl = new TestADMRealmImpl("20");
        // the BES userid is "uid2"
        exceptedLdapUserId = "ldapuid2";
        findAndBindWasCalled = false;
        realmImpl.authenticateUser("20", LDAP_PASSWORD);
        assertTrue(findAndBindWasCalled);
    }

    /**
     * Test the method for a non LDAP login.
     */
    @Test
    public void testAuthenticateUserRegular() throws LoginException {
        ADMRealmImpl realmImpl = new TestADMRealmImpl("40");
        findAndBindWasCalled = false;
        realmImpl.authenticateUser("40", LDAP_PASSWORD);
        assertFalse(findAndBindWasCalled);
    }

    /**
     * Helper class for simulating glassfish login module
     */
//    private class TestADMLoginModule extends ADMLoginModule {
//        public TestADMLoginModule(ADMRealm testRealm, String testPwd) {
//            this._currentRealm = testRealm;
//            this._password = testPwd;
//        }
//    };
//
//    /**
//     * Test the glassfish authentication module
//     */
//    @Test(expected = LoginException.class)
//    public void testGlassfishLoginModuleNoRealm() throws LoginException {
//        // Invoke login without realm
//        ADMLoginModule module = new TestADMLoginModule(null, null);
//        module.authenticateUser();
//    }

//    /**
//     * Test the glassfish authentication module
//     */
//    @Test(expected = LoginException.class)
//    public void testGlassfishLoginModuleNoPassword() throws Exception {
//        // Invoke login without password
//        ADMRealmImpl testRealm = new ADMRealmImpl(null);
//        testRealm.initialize(new Properties());
//        ADMLoginModule module = new TestADMLoginModule(testRealm, null);
//        module.authenticateUser();
//    }
//
//    /**
//     * Test the glassfish authentication module
//     */
//    @Test
//    public void testGlassfishRealmAuthType() throws Exception {
//        ADMRealm testRealm = new ADMRealm();
//        assertEquals("Federation", testRealm.getAuthType());
//    }

    /**
     * Test the glassfish authentication module
//     */
//    @Test
//    public void testGlassfishGroupNames() throws Exception {
//        ADMRealm testRealm = new ADMRealm();
//        List<String> groups = Collections.list(testRealm.getGroupNames());
//        assertEquals(Arrays.asList(ADMRealmImpl.GROUPLIST_ADMIN), groups);
//    }
}
