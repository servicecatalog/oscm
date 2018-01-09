/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.database;

import java.net.URL;

import org.oscm.setup.DatabaseVersionInfo;

/*******************************************************************************
 * 
 * COPYRIGHT (C) 2014 FUJITSU Limited - ALL RIGHTS RESERVED.
 * 
 * Creation Date: 2015-03-17
 * 
 *******************************************************************************/

public class SchemaUpgrade_02_08_02_to_02_08_03_IT extends
        SchemaUpgradeTestBase {

    public SchemaUpgrade_02_08_02_to_02_08_03_IT() {
        super(new DatabaseVersionInfo(2, 8, 2),
                new DatabaseVersionInfo(2, 8, 3));
    }

    @Override
    protected URL getSetupDataset() {
        return getClass().getResource("/setup_02_08_02_to_02_08_03.xml");
    }

    @Override
    protected URL getExpectedDataset() {
        return getClass().getResource("/expected_02_08_02_to_02_08_03.xml");
    }

}
