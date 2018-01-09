/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 10.12.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.database;

import java.net.URL;

import org.oscm.setup.DatabaseVersionInfo;

/**
 * @author goebel
 * 
 */
public class SchemaUpgrade_02_08_14_to_02_09_00_IT extends
        SchemaUpgradeTestBase {

    public SchemaUpgrade_02_08_14_to_02_09_00_IT() {
        super(new DatabaseVersionInfo(2, 8, 2),
                new DatabaseVersionInfo(2, 9, 0));
    }

    @Override
    protected URL getSetupDataset() {
        return getClass().getResource("/setup_02_08_14_to_02_09_00.xml");
    }

    @Override
    protected URL getExpectedDataset() {
        return getClass().getResource("/expected_02_08_14_to_02_09_00.xml");
    }

}