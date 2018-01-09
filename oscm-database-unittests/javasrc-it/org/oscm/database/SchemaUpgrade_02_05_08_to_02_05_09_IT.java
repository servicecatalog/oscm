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
 *  Creation Date: Mar 21, 2014                                                      
 *                                                                              
 *******************************************************************************/

/**
 * @author qiu
 * 
 */
public class SchemaUpgrade_02_05_08_to_02_05_09_IT extends
        SchemaUpgradeTestBase {

    public SchemaUpgrade_02_05_08_to_02_05_09_IT() {
        super(new DatabaseVersionInfo(2, 5, 8),
                new DatabaseVersionInfo(2, 5, 9));
    }

    @Override
    protected URL getSetupDataset() {
        return getClass().getResource("/setup_02_05_08_to_02_05_09.xml");
    }

    @Override
    protected URL getExpectedDataset() {
        return getClass().getResource("/expected_02_05_08_to_02_05_09.xml");
    }

}
