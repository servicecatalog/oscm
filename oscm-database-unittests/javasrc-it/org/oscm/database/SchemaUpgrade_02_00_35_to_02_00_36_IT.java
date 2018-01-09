/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.database;

import java.net.URL;

import org.oscm.setup.DatabaseVersionInfo;

/**
 * Db tests for report data migration
 * 
 * @author kulle
 */
public class SchemaUpgrade_02_00_35_to_02_00_36_IT extends
        SchemaUpgradeTestBase {

    public SchemaUpgrade_02_00_35_to_02_00_36_IT() {
        super(new DatabaseVersionInfo(2, 0, 35), new DatabaseVersionInfo(2, 0,
                36));
    }

    @Override
    protected URL getSetupDataset() {
        return getClass().getResource("/setup_02_00_35_to_02_00_36.xml");
    }

    @Override
    protected URL getExpectedDataset() {
        return getClass().getResource("/expected_02_00_35_to_02_00_36.xml");
    }
}
