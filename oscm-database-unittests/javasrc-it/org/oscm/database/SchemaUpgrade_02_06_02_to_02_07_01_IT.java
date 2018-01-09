/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.database;

import java.net.URL;

import org.oscm.setup.DatabaseVersionInfo;

/*******************************************************************************
 *                                                                              
 *  COPYRIGHT (C) 2014 FUJITSU Limited - ALL RIGHTS RESERVED.                  
 *                                                                                                                                 
 *  Creation Date: 2014-06-05                                                      
 *                                                                              
 *******************************************************************************/

/**
 * @author goebel
 * 
 */
public class SchemaUpgrade_02_06_02_to_02_07_01_IT extends
        SchemaUpgradeTestBase {

    public SchemaUpgrade_02_06_02_to_02_07_01_IT() {
        super(new DatabaseVersionInfo(2, 6, 2),
                new DatabaseVersionInfo(2, 7, 1));
    }

    @Override
    protected URL getSetupDataset() {
        return getClass().getResource("/setup_02_06_02_to_02_07_01.xml");
    }

    @Override
    protected URL getExpectedDataset() {
        return getClass().getResource("/expected_02_06_02_to_02_07_01.xml");
    }

}
