/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.database;

import java.net.URL;

import org.oscm.setup.DatabaseVersionInfo;

public class SchemaUpgrade_02_03_02_to_02_03_03_IT extends
        SchemaUpgradeTestBase {

    public SchemaUpgrade_02_03_02_to_02_03_03_IT() {
        super(new DatabaseVersionInfo(2, 3, 02), new DatabaseVersionInfo(2, 3,
                03));
    }

    @Override
    protected URL getSetupDataset() {
        return getClass().getResource("/setup_02_03_02_to_02_03_03.xml");
    }

    @Override
    protected URL getExpectedDataset() {
        return getClass().getResource("/expected_02_03_02_to_02_03_03.xml");
    }

}
