/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.database;

import java.net.URL;

import org.oscm.setup.DatabaseVersionInfo;


public class SchemaUpgrade_02_00_20_to_02_00_21_IT extends
        SchemaUpgradeTestBase {

    public SchemaUpgrade_02_00_20_to_02_00_21_IT() {
        super(new DatabaseVersionInfo(2, 0, 20), new DatabaseVersionInfo(2, 0,
                21));
    }

    @Override
    protected URL getSetupDataset() {
        return getClass().getResource("/setup_02_00_20_to_02_00_21.xml");
    }

    @Override
    protected URL getExpectedDataset() {
        return getClass().getResource("/expected_02_00_20_to_02_00_21.xml");
    }
}
