/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.database;

import java.net.URL;

import org.oscm.setup.DatabaseVersionInfo;

/**
 * Db tests for new user role: "marketplace owner"
 * 
 * @author brandstetter
 */
public class SchemaUpgrade_02_00_41_to_02_00_42_IT extends
        SchemaUpgradeTestBase {

    public SchemaUpgrade_02_00_41_to_02_00_42_IT() {
        super(new DatabaseVersionInfo(2, 0, 41), new DatabaseVersionInfo(2, 0,
                42));
    }

    @Override
    protected URL getSetupDataset() {
        return getClass().getResource("/setup_02_00_41_to_02_00_42.xml");
    }

    @Override
    protected URL getExpectedDataset() {
        return getClass().getResource("/expected_02_00_41_to_02_00_42.xml");
    }
}
