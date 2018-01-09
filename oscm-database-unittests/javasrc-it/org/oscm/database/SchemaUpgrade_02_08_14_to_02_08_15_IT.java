/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 01.02.16 10:00
 *
 ******************************************************************************/
package org.oscm.database;

import java.net.URL;

import org.oscm.setup.DatabaseVersionInfo;

public class SchemaUpgrade_02_08_14_to_02_08_15_IT extends
        SchemaUpgradeTestBase {

    public SchemaUpgrade_02_08_14_to_02_08_15_IT() {
        super(new DatabaseVersionInfo(2, 8, 14), new DatabaseVersionInfo(2, 8,
                15));
    }

    @Override
    protected URL getSetupDataset() {
        return getClass().getResource("/setup_02_08_14_to_02_08_15.xml");
    }

    @Override
    protected URL getExpectedDataset() {
        return getClass().getResource("/expected_02_08_14_to_02_08_15.xml");
    }

}
