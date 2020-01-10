/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2019
 *
 *  Creation Date: 22.07.19 11:58
 *
 ******************************************************************************/
package org.oscm.database;

import java.net.URL;

import org.oscm.setup.DatabaseVersionInfo;

public class SchemaUpgrade_02_10_01_to_02_10_02_IT
        extends SchemaUpgradeTestBase {

    public SchemaUpgrade_02_10_01_to_02_10_02_IT() {
        super(new DatabaseVersionInfo(2, 10, 01),
                new DatabaseVersionInfo(2, 10, 02));
    }

    @Override
    protected URL getSetupDataset() {
        return getClass().getResource("/setup_02_10_01_to_02_10_02.xml");
    }

    @Override
    protected URL getExpectedDataset() {
        return getClass().getResource("/expected_02_10_01_to_02_10_02.xml");
    }

}