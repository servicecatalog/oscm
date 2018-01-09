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
public class SchemaUpgrade_02_05_11_to_02_05_12_IT extends
        SchemaUpgradeTestBase {

    public SchemaUpgrade_02_05_11_to_02_05_12_IT() {
        super(new DatabaseVersionInfo(2, 5, 11), new DatabaseVersionInfo(2, 5,
                12));
    }

    @Override
    protected URL getSetupDataset() {
        return getClass().getResource("/setup_02_05_11_to_02_05_12.xml");
    }

    @Override
    protected URL getExpectedDataset() {
        return getClass().getResource("/expected_02_05_11_to_02_05_12.xml");
    }

}
