package org.oscm.database;

import java.net.URL;

import org.oscm.setup.DatabaseVersionInfo;

/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 2016-03-03                                                      
 *                                                                              
 *******************************************************************************/

public class SchemaUpgrade_02_09_00_to_02_09_01_IT extends
        SchemaUpgradeTestBase {

    public SchemaUpgrade_02_09_00_to_02_09_01_IT() {
        super(new DatabaseVersionInfo(2, 9, 0),
                new DatabaseVersionInfo(2, 9, 1));
    }

    @Override
    protected URL getSetupDataset() {
        return getClass().getResource("/setup_02_09_00_to_02_09_01.xml");
    }

    @Override
    protected URL getExpectedDataset() {
        return getClass().getResource("/expected_02_09_00_to_02_09_01.xml");
    }

}