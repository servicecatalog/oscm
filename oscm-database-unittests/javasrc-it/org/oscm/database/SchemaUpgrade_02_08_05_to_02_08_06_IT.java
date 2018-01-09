/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.database;

import java.net.URL;

import org.oscm.setup.DatabaseVersionInfo;

/*******************************************************************************
 * 
 * COPYRIGHT (C) 2015 FUJITSU Limited - ALL RIGHTS RESERVED.
 * 
 * Creation Date: 2015-06-08
 * 
 *******************************************************************************/

public class SchemaUpgrade_02_08_05_to_02_08_06_IT extends
        SchemaUpgradeTestBase {

    public SchemaUpgrade_02_08_05_to_02_08_06_IT() {
        super(new DatabaseVersionInfo(2, 8, 5),
                new DatabaseVersionInfo(2, 8, 6));
    }

    @Override
    protected URL getSetupDataset() {
        return getClass().getResource("/setup_02_08_05_to_02_08_06.xml");
    }

    @Override
    protected URL getExpectedDataset() {
        return getClass().getResource("/expected_02_08_05_to_02_08_06.xml");
    }

}
