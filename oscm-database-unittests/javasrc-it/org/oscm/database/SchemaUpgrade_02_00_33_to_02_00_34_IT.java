/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.database;

import java.net.URL;

import org.oscm.setup.DatabaseVersionInfo;

/**
 * Db tests
 * 
 * @author Enes Sejfi
 */
public class SchemaUpgrade_02_00_33_to_02_00_34_IT extends
        SchemaUpgradeTestBase {

    public SchemaUpgrade_02_00_33_to_02_00_34_IT() {
        super(new DatabaseVersionInfo(2, 0, 33), new DatabaseVersionInfo(2, 0,
                34));
    }

    @Override
    protected URL getSetupDataset() {
        return getClass().getResource("/setup_02_00_33_to_02_00_34.xml");
    }

    @Override
    protected URL getExpectedDataset() {
        return getClass().getResource("/expected_02_00_33_to_02_00_34.xml");
    }
}
