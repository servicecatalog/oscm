/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.database;

import java.net.URL;

import org.oscm.setup.DatabaseVersionInfo;

/**
 * Test for migrating organizations
 * 
 * @author cheld
 * 
 */
public class SchemaUpgrade_02_00_12_to_02_00_13_IT extends
        SchemaUpgradeTestBase {

    public SchemaUpgrade_02_00_12_to_02_00_13_IT() {
        super(new DatabaseVersionInfo(2, 0, 12), new DatabaseVersionInfo(2, 0,
                13));
    }

    @Override
    protected URL getSetupDataset() {
        return getClass().getResource("/setup_02_00_12_to_02_00_13.xml");
    }

    @Override
    protected URL getExpectedDataset() {
        return getClass().getResource("/expected_02_00_12_to_02_00_13.xml");
    }
}
