package org.oscm.database;

import java.net.URL;

import org.oscm.setup.DatabaseVersionInfo;

/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 2016-05-17                                                      
 *                                                                              
 *******************************************************************************/

public class SchemaUpgrade_02_09_03_to_02_09_04_IT extends
        SchemaUpgradeTestBase {

    public SchemaUpgrade_02_09_03_to_02_09_04_IT() {
        super(new DatabaseVersionInfo(2, 9, 3),
                new DatabaseVersionInfo(2, 9, 4));
    }

    @Override
    protected URL getSetupDataset() {
        return getClass().getResource("/setup_02_09_03_to_02_09_04.xml");
    }

    @Override
    protected URL getExpectedDataset() {
        return getClass().getResource("/expected_02_09_03_to_02_09_04.xml");
    }

}