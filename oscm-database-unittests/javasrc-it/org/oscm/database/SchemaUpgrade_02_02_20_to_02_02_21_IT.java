/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.database;

import java.net.URL;

import org.oscm.setup.DatabaseVersionInfo;

public class SchemaUpgrade_02_02_20_to_02_02_21_IT extends
        SchemaUpgradeTestBase {

    public SchemaUpgrade_02_02_20_to_02_02_21_IT() {
        super(new DatabaseVersionInfo(2, 2, 20), new DatabaseVersionInfo(2, 2,
                21));
    }

    protected URL getSetupDataset() {
        return getClass().getResource("/setup_02_02_20_to_02_02_21.xml");
    }

    protected URL getExpectedDataset() {
        return getClass().getResource("/expected_02_02_20_to_02_02_21.xml");
    }

}
