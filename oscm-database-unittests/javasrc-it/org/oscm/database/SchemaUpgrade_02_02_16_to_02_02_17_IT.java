/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.database;

import java.net.URL;

import org.oscm.setup.DatabaseVersionInfo;

public class SchemaUpgrade_02_02_16_to_02_02_17_IT extends
        SchemaUpgradeTestBase {

    public SchemaUpgrade_02_02_16_to_02_02_17_IT() {
        super(new DatabaseVersionInfo(2, 2, 16), new DatabaseVersionInfo(2, 2,
                17));
    }

    @Override
    protected URL getSetupDataset() {
        return getClass().getResource("/setup_02_02_16_to_02_02_17.xml");
    }

    @Override
    protected URL getExpectedDataset() {
        return getClass().getResource("/expected_02_02_16_to_02_02_17.xml");
    }

}
