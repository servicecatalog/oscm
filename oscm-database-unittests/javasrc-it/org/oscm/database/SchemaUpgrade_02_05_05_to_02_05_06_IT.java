/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.database;

import java.net.URL;

import org.oscm.setup.DatabaseVersionInfo;

public class SchemaUpgrade_02_05_05_to_02_05_06_IT extends
        SchemaUpgradeTestBase {

    public SchemaUpgrade_02_05_05_to_02_05_06_IT() {
        super(new DatabaseVersionInfo(2, 5, 5),
                new DatabaseVersionInfo(2, 5, 6));
    }

    @Override
    protected URL getSetupDataset() {
        return getClass().getResource("/setup_02_05_05_to_02_05_06.xml");
    }

    @Override
    protected URL getExpectedDataset() {
        return getClass().getResource("/expected_02_05_05_to_02_05_06.xml");
    }
}
