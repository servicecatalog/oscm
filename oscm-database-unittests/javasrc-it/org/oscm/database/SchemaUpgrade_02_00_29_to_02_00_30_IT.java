/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.database;

import java.net.URL;

import org.oscm.setup.DatabaseVersionInfo;

/**
 * Test case for the removal of the technical product version attribute.
 * 
 * @author Sven Kulle
 */
public class SchemaUpgrade_02_00_29_to_02_00_30_IT extends
        SchemaUpgradeTestBase {

    public SchemaUpgrade_02_00_29_to_02_00_30_IT() {
        super(new DatabaseVersionInfo(2, 0, 29), new DatabaseVersionInfo(2, 0,
                30));
    }

    @Override
    protected URL getSetupDataset() {
        return getClass().getResource("/setup_02_00_29_to_02_00_30.xml");
    }

    @Override
    protected URL getExpectedDataset() {
        return getClass().getResource("/expected_02_00_29_to_02_00_30.xml");
    }
}
