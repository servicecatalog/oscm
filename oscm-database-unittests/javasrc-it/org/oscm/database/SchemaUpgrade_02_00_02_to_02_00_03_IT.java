/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.database;

import java.net.URL;

import org.oscm.setup.DatabaseVersionInfo;

/**
 * Test case for migrating existing technical product licenses to price models.
 * 
 * @author weiser
 * 
 */
public class SchemaUpgrade_02_00_02_to_02_00_03_IT extends
        SchemaUpgradeTestBase {

    public SchemaUpgrade_02_00_02_to_02_00_03_IT() {
        super(new DatabaseVersionInfo(2, 0, 2),
                new DatabaseVersionInfo(2, 0, 3));
    }

    @Override
    protected URL getSetupDataset() {
        return getClass().getResource("/setup_02_00_02_to_02_00_03.xml");
    }

    @Override
    protected URL getExpectedDataset() {
        return getClass().getResource("/expected_02_00_02_to_02_00_03.xml");
    }
}
