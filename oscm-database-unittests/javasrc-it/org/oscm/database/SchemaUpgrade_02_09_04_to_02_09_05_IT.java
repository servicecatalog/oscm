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

public class SchemaUpgrade_02_09_04_to_02_09_05_IT extends
        SchemaUpgradeTestBase {

    public SchemaUpgrade_02_09_04_to_02_09_05_IT() {
        super(new DatabaseVersionInfo(2, 9, 4),
                new DatabaseVersionInfo(2, 9, 5));
    }

    @Override
    protected URL getSetupDataset() {
        return getClass().getResource("/setup_02_09_04_to_02_09_05.xml");
    }

    @Override
    protected URL getExpectedDataset() {
        return getClass().getResource("/expected_02_09_04_to_02_09_05.xml");
    }

}