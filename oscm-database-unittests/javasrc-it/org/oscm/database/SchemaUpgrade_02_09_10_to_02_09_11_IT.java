/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 12.05.17 14:58
 *
 ******************************************************************************/
package org.oscm.database;

import java.net.URL;

import org.oscm.setup.DatabaseVersionInfo;

public class SchemaUpgrade_02_09_10_to_02_09_11_IT
        extends SchemaUpgradeTestBase {

    public SchemaUpgrade_02_09_10_to_02_09_11_IT() {
        super(new DatabaseVersionInfo(2, 9, 10),
                new DatabaseVersionInfo(2, 9, 11));
    }

    @Override
    protected URL getSetupDataset() {
        return getClass().getResource("/setup_02_09_10_to_02_09_11.xml");
    }

    @Override
    protected URL getExpectedDataset() {
        return getClass().getResource("/expected_02_09_10_to_02_09_11.xml");
    }

}