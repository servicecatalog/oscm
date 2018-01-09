/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.database;

import java.net.URL;

import org.oscm.setup.DatabaseVersionInfo;

public class SchemaUpgrade_02_00_52_to_02_00_53_IT extends
        SchemaUpgradeTestBase {

    public SchemaUpgrade_02_00_52_to_02_00_53_IT() {
        super(new DatabaseVersionInfo(2, 0, 52), new DatabaseVersionInfo(2, 0,
                53));
    }

    @Override
    protected URL getSetupDataset() {
        return getClass().getResource("/setup_02_00_52_to_02_00_53.xml");
    }

    @Override
    protected URL getExpectedDataset() {
        return getClass().getResource("/expected_02_00_52_to_02_00_53.xml");
    }
}
