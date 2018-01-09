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
public class SchemaUpgrade_02_00_15_to_02_00_16_IT extends
        SchemaUpgradeTestBase {

    public SchemaUpgrade_02_00_15_to_02_00_16_IT() {
        super(new DatabaseVersionInfo(2, 0, 15), new DatabaseVersionInfo(2, 0,
                16));
    }

    @Override
    protected URL getSetupDataset() {
        return getClass().getResource("/setup_02_00_15_to_02_00_16.xml");
    }

    @Override
    protected URL getExpectedDataset() {
        return getClass().getResource("/expected_02_00_15_to_02_00_16.xml");
    }
}
