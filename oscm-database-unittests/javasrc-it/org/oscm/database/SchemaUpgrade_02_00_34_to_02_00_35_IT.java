/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.database;

import java.net.URL;

import org.oscm.setup.DatabaseVersionInfo;

/**
 * Db tests
 * 
 * @author tokoda
 */
public class SchemaUpgrade_02_00_34_to_02_00_35_IT extends
        SchemaUpgradeTestBase {

    public SchemaUpgrade_02_00_34_to_02_00_35_IT() {
        super(new DatabaseVersionInfo(2, 0, 34), new DatabaseVersionInfo(2, 0,
                35));
    }

    @Override
    protected URL getSetupDataset() {
        return getClass().getResource("/setup_02_00_34_to_02_00_35.xml");
    }

    @Override
    protected URL getExpectedDataset() {
        return getClass().getResource("/expected_02_00_34_to_02_00_35.xml");
    }
}
