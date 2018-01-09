/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: Dec 23, 2016                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.propertyimport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.oscm.db.TestDatabase;
import org.oscm.internal.types.enumtypes.ConfigurationKey;

/**
 * Integration test for PropertyMigrator
 * 
 * @author miethaner
 */
public class PropertyImportIT {

    private static TestDatabase testDatabase;
    private static String path;

    private class TestImport extends PropertyImport {

        public TestImport(String driverClass, String driverURL, String userName,
                String userPwd, String file, boolean overwrite,
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
    public void testMigration() throws Exception {
        TestImport migrator = new TestImport("java.lang.String", "url", "user",
                "password", path, true, "global");
        migrator.execute();

        Connection conn = testDatabase.getDBconnection();
        String query = "SELECT information_id FROM configurationsetting WHERE context_id = 'global'";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(query);

        List<String> keys = new ArrayList<>();
        while (rs.next()) {
            keys.add(rs.getString(1));
        }

        assertEquals(ConfigurationKey.values().length, keys.size());

        for (ConfigurationKey ck : ConfigurationKey.values()) {
            assertTrue(keys.contains(ck.getKeyName()));
        }
    }

    private static Properties getProperties() {
        Properties p = new Properties();
        p.put(ConfigurationKey.AUTH_MODE.name(), "INTERNAL");
        p.put(ConfigurationKey.BASE_URL.name(), "http://localhost:8180");
        p.put(ConfigurationKey.SSO_STS_URL.name(), "http://localhost:8680");

        p.put(ConfigurationKey.BASE_URL_HTTPS.name(), "http://localhost:8180");
        p.put(ConfigurationKey.LOG_FILE_PATH.name(), "../logs");
        p.put(ConfigurationKey.PSP_USAGE_ENABLED.name(), "false");
        p.put(ConfigurationKey.TAGGING_MAX_TAGS.name(), "20");
        p.put(ConfigurationKey.TAGGING_MIN_SCORE.name(), "1");

        p.put(ConfigurationKey.WS_TIMEOUT.name(), "180000");
        p.put(ConfigurationKey.SSO_DEFAULT_TENANT_ID.name(), "8f96dede");
        p.put(ConfigurationKey.SSO_IDP_SAML_ASSERTION_ISSUER_ID.name(),
                "default");
        p.put(ConfigurationKey.HIDDEN_UI_ELEMENTS.name(),
                "operator.manageBillingAdapters,techService.viewBillingAdapters");
        p.put(ConfigurationKey.SSO_SIGNING_KEYSTORE.name(), "./cacerts.jks");
        p.put(ConfigurationKey.SSO_SIGNING_KEYSTORE_PASS.name(), "changeit");
        p.put(ConfigurationKey.SSO_SIGNING_KEY_ALIAS.name(), "s1as");
        return p;
    }
}
