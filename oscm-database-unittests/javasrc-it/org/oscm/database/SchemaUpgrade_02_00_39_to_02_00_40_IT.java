/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.database;

import java.net.URL;

import org.oscm.setup.DatabaseVersionInfo;

/**
 * Db tests for relation marketplace to organization
 * 
 * @author weiser
 */
public class SchemaUpgrade_02_00_39_to_02_00_40_IT extends
        SchemaUpgradeTestBase {

    public SchemaUpgrade_02_00_39_to_02_00_40_IT() {
        super(new DatabaseVersionInfo(2, 0, 39), new DatabaseVersionInfo(2, 0,
                40));
    }

    @Override
    protected URL getSetupDataset() {
        return getClass().getResource("/setup_02_00_39_to_02_00_40.xml");
    }

    @Override
    protected URL getExpectedDataset() {
        return getClass().getResource("/expected_02_00_39_to_02_00_40.xml");
    }
}
