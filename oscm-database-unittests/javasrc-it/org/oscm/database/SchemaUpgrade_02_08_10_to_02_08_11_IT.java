/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.database;

import java.net.URL;

import org.oscm.setup.DatabaseVersionInfo;

public class SchemaUpgrade_02_08_10_to_02_08_11_IT extends
        SchemaUpgradeTestBase {

    public SchemaUpgrade_02_08_10_to_02_08_11_IT() {
        super(new DatabaseVersionInfo(2, 8, 10), new DatabaseVersionInfo(2, 8,
                11));
    }

    @Override
    protected URL getSetupDataset() {
        return getClass().getResource("/setup_02_08_10_to_02_08_11.xml");
    }

    @Override
    protected URL getExpectedDataset() {
        return getClass().getResource("/expected_02_08_10_to_02_08_11.xml");
    }

}
