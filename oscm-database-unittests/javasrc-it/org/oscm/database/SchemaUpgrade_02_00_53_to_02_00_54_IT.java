/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.database;

import java.net.URL;

import org.oscm.setup.DatabaseVersionInfo;

public class SchemaUpgrade_02_00_53_to_02_00_54_IT extends
        SchemaUpgradeTestBase {

    public SchemaUpgrade_02_00_53_to_02_00_54_IT() {
        super(new DatabaseVersionInfo(2, 0, 53), new DatabaseVersionInfo(2, 0,
                54));
    }

    @Override
    protected URL getSetupDataset() {
        return getClass().getResource("/setup_02_00_53_to_02_00_54.xml");
    }

    @Override
    protected URL getExpectedDataset() {
        return getClass().getResource("/expected_02_00_53_to_02_00_54.xml");
    }
}
