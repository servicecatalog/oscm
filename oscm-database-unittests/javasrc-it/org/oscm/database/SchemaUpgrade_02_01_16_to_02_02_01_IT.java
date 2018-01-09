/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.database;

import java.net.URL;

import org.oscm.setup.DatabaseVersionInfo;

public class SchemaUpgrade_02_01_16_to_02_02_01_IT extends
        SchemaUpgradeTestBase {

    public SchemaUpgrade_02_01_16_to_02_02_01_IT() {
        super(new DatabaseVersionInfo(2, 1, 16), new DatabaseVersionInfo(2, 2,
                01));
    }

    @Override
    protected URL getSetupDataset() {
        return getClass().getResource("/setup_02_01_16_to_02_02_01.xml");
    }

    @Override
    protected URL getExpectedDataset() {
        return getClass().getResource("/expected_02_01_16_to_02_02_01.xml");
    }
}
