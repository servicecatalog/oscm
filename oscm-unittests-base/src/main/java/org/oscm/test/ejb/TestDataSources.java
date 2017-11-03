/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.test.ejb;

import java.util.HashMap;
import java.util.Map;

import org.oscm.test.db.ClientTestDBPostGreSQL;
import org.oscm.test.db.ITestDB;

/**
 * Static configuration of data sources for unit testing.
 * 
 * @author hoffmann
 */
public class TestDataSources {

    private static Map<String, ITestDB> TEST_DATASOURCES = new HashMap<String, ITestDB>();
    private static Map<String, ITestDB> PRODUCTIVE_DATASOURCES = new HashMap<String, ITestDB>();

    static {
        final ITestDB saas = new ClientTestDBPostGreSQL(
                "../oscm-devruntime/javares/sql", "bssunittests",
                "bssunituser", "bssunituser");
        TEST_DATASOURCES.put("oscm-domainobjects", saas);

        final ITestDB app = new ClientTestDBPostGreSQL(
                "../oscm-app/resources/sql", "bssappunit",
                "bssappunituser", "bssappunituser");
        TEST_DATASOURCES.put("oscm-app", app);

        final ITestDB auditLog = new ClientTestDBPostGreSQL(
                "../oscm-devruntime/javares/sql", "bssunittests",
                "bssunituser", "bssunituser");
        TEST_DATASOURCES.put("oscm-auditlog", auditLog);

        final ITestDB saasProductive = new ClientTestDBPostGreSQL(
                "../oscm-devruntime/javares/sql", "bss", "bssuser",
                "bssuser", "localhost", 5432);
        PRODUCTIVE_DATASOURCES.put("oscm-domainobjects",
                saasProductive);
    }

    public static ITestDB get(String unitName) {
        final ITestDB db = TEST_DATASOURCES.get(unitName);
        if (db == null) {
            throw new AssertionError("No data source for persistence unit "
                    + unitName);
        }
        return db;
    }

    public static ITestDB get(String unitName, boolean productive) {
        ITestDB db;
        if (productive) {
            db = PRODUCTIVE_DATASOURCES.get(unitName);
        } else {
            db = TEST_DATASOURCES.get(unitName);
        }
        return db;
    }
}
