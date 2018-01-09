/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.database;

import java.net.URL;

import org.oscm.setup.DatabaseVersionInfo;

public class SchemaUpgrade_02_00_50_to_02_00_51_IT extends
        SchemaUpgradeTestBase {

    public SchemaUpgrade_02_00_50_to_02_00_51_IT() {
        super(new DatabaseVersionInfo(2, 0, 50), new DatabaseVersionInfo(2, 0,
                51));
    }

    @Override
    protected URL getSetupDataset() {
        return getClass().getResource("/setup_02_00_50_to_02_00_51.xml");
    }

    @Override
    protected URL getExpectedDataset() {
        return getClass().getResource("/expected_02_00_50_to_02_00_51.xml");
    }
}
