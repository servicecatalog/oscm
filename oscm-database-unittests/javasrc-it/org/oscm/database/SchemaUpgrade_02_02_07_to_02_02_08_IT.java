/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.database;

import java.net.URL;

import org.oscm.setup.DatabaseVersionInfo;

/**
 * Db tests for adding modificationType column to parameterdefinition and
 * parameterdefinitionhistory. Modify modificationType column default value and
 * not null.
 * 
 * @author ManQ
 * 
 */
public class SchemaUpgrade_02_02_07_to_02_02_08_IT extends
        SchemaUpgradeTestBase {

    public SchemaUpgrade_02_02_07_to_02_02_08_IT() {
        super(new DatabaseVersionInfo(2, 2, 7),
                new DatabaseVersionInfo(2, 2, 8));
    }

    @Override
    protected URL getSetupDataset() {
        return getClass().getResource("/setup_02_02_07_to_02_02_08.xml");
    }

    @Override
    protected URL getExpectedDataset() {
        return getClass().getResource("/expected_02_02_07_to_02_02_08.xml");
    }

}
