/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.database;

import java.net.URL;

import org.oscm.setup.DatabaseVersionInfo;

public class SchemaUpgrade_02_03_06_to_02_03_07_IT extends
        SchemaUpgradeTestBase {

    public SchemaUpgrade_02_03_06_to_02_03_07_IT() {
        super(new DatabaseVersionInfo(2, 3, 06), new DatabaseVersionInfo(2, 3,
                07));
    }

    @Override
    protected URL getSetupDataset() {
        return getClass().getResource("/setup_02_03_06_to_02_03_07.xml");
    }

    @Override
    protected URL getExpectedDataset() {
        return getClass().getResource("/expected_02_03_06_to_02_03_07.xml");
    }

}
