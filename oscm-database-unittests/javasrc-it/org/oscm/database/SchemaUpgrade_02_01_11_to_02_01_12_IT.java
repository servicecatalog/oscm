/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.database;

import java.net.URL;

import org.oscm.setup.DatabaseVersionInfo;

public class SchemaUpgrade_02_01_11_to_02_01_12_IT extends
        SchemaUpgradeTestBase {

    public SchemaUpgrade_02_01_11_to_02_01_12_IT() {
        super(new DatabaseVersionInfo(2, 1, 11),
                new DatabaseVersionInfo(2, 1, 12));
    }

    @Override
    protected URL getSetupDataset() {
        return getClass().getResource("/setup_02_01_11_to_02_01_12.xml");
    }

    @Override
    protected URL getExpectedDataset() {
        return getClass().getResource("/expected_02_01_11_to_02_01_12.xml");
    }
}
