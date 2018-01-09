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
 *  Creation Date: Sep 17, 2014                                                      
 *                                                                              
 *******************************************************************************/

/**
 * @author zhaoh.fnst
 * 
 */
public class SchemaUpgrade_02_07_01_to_02_08_01_IT extends
        SchemaUpgradeTestBase {

    public SchemaUpgrade_02_07_01_to_02_08_01_IT() {
        super(new DatabaseVersionInfo(2, 7, 1),
                new DatabaseVersionInfo(2, 8, 1));
    }

    @Override
    protected URL getSetupDataset() {
        return getClass().getResource("/setup_02_07_01_to_02_08_01.xml");
    }

    @Override
    protected URL getExpectedDataset() {
        return getClass().getResource("/expected_02_07_01_to_02_08_01.xml");
    }

}
