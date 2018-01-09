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
public class SchemaUpgrade_02_08_01_to_02_08_02_IT extends
        SchemaUpgradeTestBase {

    public SchemaUpgrade_02_08_01_to_02_08_02_IT() {
        super(new DatabaseVersionInfo(2, 8, 1),
                new DatabaseVersionInfo(2, 8, 2));
    }

    @Override
    protected URL getSetupDataset() {
        return getClass().getResource("/setup_02_08_01_to_02_08_02.xml");
    }

    @Override
    protected URL getExpectedDataset() {
        return getClass().getResource("/expected_02_08_01_to_02_08_02.xml");
    }

}
