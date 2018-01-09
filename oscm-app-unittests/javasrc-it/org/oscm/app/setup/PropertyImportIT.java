/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: Dec 23, 2016                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.setup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.oscm.app.db.TestDatabase;
import org.oscm.app.domain.PlatformConfigurationKey;

/**
 * Integration test for PropertyMigrator
 * 
 * @author miethaner
 */
public class PropertyImportIT {

    private static TestDatabase testDatabase;
    private static String path;

    private class TestMigrator extends PropertyImport {

        public TestMigrator(String driverClass, String driverURL,
                String userName, String userPwd, String file, boolean overwrite,
                String contextId) {
            super(driverClass, driverURL, userName, userPwd, file, overwrite,
                    contextId);
        }

        @Override
        protected Connection getConnetion() throws SQLException {
            return testDatabase.getDBconnection();
        }
    }

    @BeforeClass
    public static void initDb() throws Exception {
        testDatabase = new TestDatabase();
        testDatabase.initDatabase();

        File tempFile = File.createTempFile("temp", ".properties");
        tempFile.deleteOnExit();
        path = tempFile.getAbsolutePath();
        FileOutputStream fos = null;
        Properties p = getProperties();
        try {
            fos = new FileOutputStream(tempFile);
            p.store(fos, "No comment");
        } finally {
            if (fos != null) {
                fos.close();
            }
        }
    }

    @Before
    public void setUp() throws Exception {
        testDatabase.insertData(
                getClass().getResource("/setup_PropertyMigrator.xml"));
    }

    @After
    public void tearDown() throws Exception {
        testDatabase.clean();
    }

    @AfterClass
    public static void cleanupDB() throws Exception {
        testDatabase.updateDBSchemaToLatestVersion();
        testDatabase.close();
    }

    @Test
    public void testMigrationProxy() throws Exception {
        TestMigrator migrator = new TestMigrator("java.lang.String", "url",
                "user", "password", path, true, "PROXY");
        migrator.execute();

        Connection conn = testDatabase.getDBconnection();
        String query = "SELECT settingkey, settingvalue FROM configurationsetting WHERE controllerid = 'PROXY'";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(query);

        Map<String, String> settings = new HashMap<>();
        while (rs.next()) {
            settings.put(rs.getString(1), rs.getString(2));
        }

        assertEquals(PlatformConfigurationKey.values().length, settings.size());

        for (PlatformConfigurationKey ck : PlatformConfigurationKey.values()) {
            assertTrue(settings.containsKey(ck.name()));
        }

        assertTrue(settings.get(PlatformConfigurationKey.BSS_USER_PWD.name())
                .startsWith("_crypt:"));
    }

    @Test
    public void testMigrationController() throws Exception {

        Properties p = new Properties();
        // p.put("CONTROLLER_ID", "ess.sample");
        p.put("TEST1", "test");
        p.put("TEST3", "test");

        File tempFile = File.createTempFile("tempController", ".properties");
        tempFile.deleteOnExit();
        String pathCP = tempFile.getAbsolutePath();
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(tempFile);
            p.store(fos, "No comment");
        } finally {
            if (fos != null) {
                fos.close();
            }
        }

        TestMigrator migrator = new TestMigrator("java.lang.String", "url",
                "user", "password", pathCP, true, "ess.sample");
        migrator.execute();

        Connection conn = testDatabase.getDBconnection();
        String query = "SELECT settingkey, settingvalue FROM configurationsetting WHERE controllerid = 'ess.sample'";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(query);

        Map<String, String> settings = new HashMap<>();
        while (rs.next()) {
            settings.put(rs.getString(1), rs.getString(2));
        }

        assertEquals(3, settings.size());
    }

    private static Properties getProperties() {
        Properties p = new Properties();
        p.put(PlatformConfigurationKey.APP_BASE_URL.name(),
                "http://www.fujitsu.com");
        p.put(PlatformConfigurationKey.APP_ADMIN_MAIL_ADDRESS.name(),
                "here@there.com");
        p.put(PlatformConfigurationKey.APP_TIMER_INTERVAL.name(), "15000");
        p.put(PlatformConfigurationKey.APP_TIMER_REFRESH_SUBSCRIPTIONS.name(), "86400000");
        p.put(PlatformConfigurationKey.BSS_WEBSERVICE_URL.name(),
                "http://www.fujitsu.com/{service}/BASIC?wsdl");
        p.put(PlatformConfigurationKey.BSS_USER_KEY.name(), "1000");
        p.put(PlatformConfigurationKey.BSS_USER_PWD.name(), "_crypt:admin123");
        p.put(PlatformConfigurationKey.BSS_AUTH_MODE.name(), "INTERNAL");
        p.put(PlatformConfigurationKey.BSS_WEBSERVICE_WSDL_URL.name(),
                "http://www.fujitsu.com/{service}/oscm/BASIC?wsdl");
        p.put(PlatformConfigurationKey.BSS_STS_WEBSERVICE_WSDL_URL.name(),
                "http://www.fujitsu.com/{service}/oscm/STS?wsdl");
        p.put(PlatformConfigurationKey.APP_KEY_PATH.name(), "./key");
        p.put(PlatformConfigurationKey.APP_TRUSTSTORE.name(), "./cacert.jsk");
        p.put(PlatformConfigurationKey.APP_TRUSTSTORE_PASSWORD.name(),
                "changeit");
        p.put(PlatformConfigurationKey.APP_TRUSTSTORE_BSS_ALIAS.name(),
                "bes-s1as");
        return p;
    }
}
