/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.database;

import java.net.URL;

import org.oscm.setup.DatabaseVersionInfo;

public class SchemaUpgrade_02_03_12_to_02_04_00_IT extends
        SchemaUpgradeTestBase {

    public SchemaUpgrade_02_03_12_to_02_04_00_IT() {
        super(new DatabaseVersionInfo(2, 3, 12), new DatabaseVersionInfo(2, 4,
                0));
    }

    @Override
    protected URL getSetupDataset() {
        return getClass().getResource("/setup_02_03_12_to_02_04_00.xml");
    }

    @Override
    protected URL getExpectedDataset() {
        return getClass().getResource("/expected_02_03_12_to_02_04_00.xml");
    }

}
