/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.database;

import java.net.URL;

import org.oscm.setup.DatabaseVersionInfo;

/**
 * Tests for migrating CatalogEntries
 * 
 * @author sven
 * 
 */
public class SchemaUpgrade_02_00_13_to_02_00_14_IT extends
        SchemaUpgradeTestBase {

    public SchemaUpgrade_02_00_13_to_02_00_14_IT() {
        super(new DatabaseVersionInfo(2, 0, 13), new DatabaseVersionInfo(2, 0,
                14));
    }

    @Override
    protected URL getSetupDataset() {
        return getClass().getResource("/setup_02_00_13_to_02_00_14.xml");
    }

    @Override
    protected URL getExpectedDataset() {
        return getClass().getResource("/expected_02_00_13_to_02_00_14.xml");
    }
}
