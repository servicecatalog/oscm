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

public class SchemaUpgrade_02_09_13_to_02_10_01_IT
        extends SchemaUpgradeTestBase {

    public SchemaUpgrade_02_09_13_to_02_10_01_IT() {
        super(new DatabaseVersionInfo(2, 9, 13),
                new DatabaseVersionInfo(2, 10, 01));
    }

    @Override
    protected URL getSetupDataset() {
        return getClass().getResource("/setup_02_09_13_to_02_10_01.xml");
    }

    @Override
    protected URL getExpectedDataset() {
        return getClass().getResource("/expected_02_09_13_to_02_10_01.xml");
    }

}