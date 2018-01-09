/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.database;

import java.net.URL;

import org.oscm.setup.DatabaseVersionInfo;

public class SchemaUpgrade_02_02_21_to_02_03_00_IT extends
        SchemaUpgradeTestBase {

    public SchemaUpgrade_02_02_21_to_02_03_00_IT() {
        super(new DatabaseVersionInfo(2, 2, 21), new DatabaseVersionInfo(2, 3,
                0));
    }

    protected URL getSetupDataset() {
        return getClass().getResource("/setup_02_02_21_to_02_03_00.xml");
    }

    protected URL getExpectedDataset() {
        return getClass().getResource("/expected_02_02_21_to_02_03_00.xml");
    }

}
