/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.database;

import java.net.URL;

import org.oscm.setup.DatabaseVersionInfo;

/**
 * Tests for migrating shops. Creation of global and local marketplace.
 * 
 * @author cheld
 * 
 */
public class SchemaUpgrade_02_00_08_to_02_00_09_IT extends
        SchemaUpgradeTestBase {

    public SchemaUpgrade_02_00_08_to_02_00_09_IT() {
        super(new DatabaseVersionInfo(2, 0, 8),
                new DatabaseVersionInfo(2, 0, 9));
    }

    @Override
    protected URL getSetupDataset() {
        return getClass().getResource("/setup_02_00_08_to_02_00_09.xml");
    }

    @Override
    protected URL getExpectedDataset() {
        return getClass().getResource("/expected_02_00_08_to_02_00_09.xml");
    }
}
