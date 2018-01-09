/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.database;

import java.net.URL;

import org.oscm.setup.DatabaseVersionInfo;

/**
 * Db tests for report data migration
 * 
 * @author flori
 */
public class SchemaUpgrade_02_00_38_to_02_00_39_IT extends
        SchemaUpgradeTestBase {

    public SchemaUpgrade_02_00_38_to_02_00_39_IT() {
        super(new DatabaseVersionInfo(2, 0, 38), new DatabaseVersionInfo(2, 0,
                39));
    }

    @Override
    protected URL getSetupDataset() {
        return getClass().getResource("/setup_02_00_38_to_02_00_39.xml");
    }

    @Override
    protected URL getExpectedDataset() {
        return getClass().getResource("/expected_02_00_38_to_02_00_39.xml");
    }
}
