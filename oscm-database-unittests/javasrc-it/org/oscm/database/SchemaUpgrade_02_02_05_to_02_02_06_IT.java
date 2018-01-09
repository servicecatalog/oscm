/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.database;

import java.net.URL;

import org.oscm.setup.DatabaseVersionInfo;

/**
 * Db tests for adding modificationType column to parameterdefinition and parameterdefinitionhistory.
 * Modify modificationType column default value and not null.
 * @author ManQ
 *
 */
public class SchemaUpgrade_02_02_05_to_02_02_06_IT extends
        SchemaUpgradeTestBase {

    public SchemaUpgrade_02_02_05_to_02_02_06_IT() {
        super(new DatabaseVersionInfo(2, 2, 5),
                new DatabaseVersionInfo(2, 2, 6));
    }

    @Override
    protected URL getSetupDataset() {
        return getClass().getResource("/setup_02_02_05_to_02_02_06.xml");
    }

    @Override
    protected URL getExpectedDataset() {
        return getClass().getResource("/expected_02_02_05_to_02_02_06.xml");
    }

}
