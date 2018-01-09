/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.database;

import java.net.URL;

import org.oscm.setup.DatabaseVersionInfo;

/**
 * Test case for global unique user ids
 * 
 * @author walker
 * 
 */
public class SchemaUpgrade_02_00_21_to_02_00_22_IT extends
        SchemaUpgradeTestBase {

    public SchemaUpgrade_02_00_21_to_02_00_22_IT() {
        super(new DatabaseVersionInfo(2, 0, 21), new DatabaseVersionInfo(2, 0,
                22));
    }

    @Override
    protected URL getSetupDataset() {
        return getClass().getResource("/setup_02_00_21_to_02_00_22.xml");
    }

    @Override
    protected URL getExpectedDataset() {
        return getClass().getResource("/expected_02_00_21_to_02_00_22.xml");
    }
}
