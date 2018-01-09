/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.database;

import java.net.URL;

import org.oscm.setup.DatabaseVersionInfo;

/**
 * Test case for migrating the 'admin flag' from the platform user to the new
 * user role 'administrator'.
 * 
 * @author cheld
 * 
 */
public class SchemaUpgrade_02_00_25_to_02_00_26_IT extends
        SchemaUpgradeTestBase {

    public SchemaUpgrade_02_00_25_to_02_00_26_IT() {
        super(new DatabaseVersionInfo(2, 0, 25), new DatabaseVersionInfo(2, 0,
                26));
    }

    @Override
    protected URL getSetupDataset() {
        return getClass().getResource("/setup_02_00_25_to_02_00_26.xml");
    }

    @Override
    protected URL getExpectedDataset() {
        return getClass().getResource("/expected_02_00_25_to_02_00_26.xml");
    }
}
