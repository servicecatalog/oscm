/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.database;

import java.net.URL;

import org.oscm.setup.DatabaseVersionInfo;

public class SchemaUpgrade_02_02_15_to_02_02_16_IT extends
        SchemaUpgradeTestBase {

    public SchemaUpgrade_02_02_15_to_02_02_16_IT() {
        super(new DatabaseVersionInfo(2, 2, 15), new DatabaseVersionInfo(2, 2,
                16));
    }

    @Override
    protected URL getSetupDataset() {
        return getClass().getResource("/setup_02_02_15_to_02_02_16.xml");
    }

    @Override
    protected URL getExpectedDataset() {
        return getClass().getResource("/expected_02_02_15_to_02_02_16.xml");
    }

}
