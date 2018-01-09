/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 03.09.2012
 *
 *******************************************************************************/

package org.oscm.database;

import java.net.URL;

import org.oscm.setup.DatabaseVersionInfo;

public class SchemaUpgrade_02_08_06_to_02_08_07_IT extends
        SchemaUpgradeTestBase {

    public SchemaUpgrade_02_08_06_to_02_08_07_IT() {
        super(new DatabaseVersionInfo(2, 8, 6),
                new DatabaseVersionInfo(2, 8, 7));
    }

    @Override
    protected URL getSetupDataset() {
        return getClass().getResource("/setup_02_08_06_to_02_08_07.xml");
    }

    @Override
    protected URL getExpectedDataset() {
        return getClass().getResource("/expected_02_08_06_to_02_08_07.xml");
    }

}
