/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.database;

import java.net.URL;

import org.oscm.setup.DatabaseVersionInfo;

/**
 * Db tests for on-behalf-of feature.
 * 
 * @author Sven Kulle
 */
public class SchemaUpgrade_02_00_31_to_02_00_32_IT extends
        SchemaUpgradeTestBase {

    public SchemaUpgrade_02_00_31_to_02_00_32_IT() {
        super(new DatabaseVersionInfo(2, 0, 31), new DatabaseVersionInfo(2, 0,
                32));
    }

    @Override
    protected URL getSetupDataset() {
        return getClass().getResource("/setup_02_00_31_to_02_00_32.xml");
    }

    @Override
    protected URL getExpectedDataset() {
        return getClass().getResource("/expected_02_00_31_to_02_00_32.xml");
    }
}
