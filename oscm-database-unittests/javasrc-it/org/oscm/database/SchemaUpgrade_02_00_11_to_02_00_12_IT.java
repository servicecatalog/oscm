/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.database;

import java.net.URL;

import org.oscm.setup.DatabaseVersionInfo;

/**
 * Tests for migrating vat rates
 * 
 * @author sven
 * 
 */
public class SchemaUpgrade_02_00_11_to_02_00_12_IT extends
        SchemaUpgradeTestBase {

    public SchemaUpgrade_02_00_11_to_02_00_12_IT() {
        super(new DatabaseVersionInfo(2, 0, 11), new DatabaseVersionInfo(2, 0,
                12));
    }

    @Override
    protected URL getSetupDataset() {
        return getClass().getResource("/setup_02_00_11_to_02_00_12.xml");
    }

    @Override
    protected URL getExpectedDataset() {
        return getClass().getResource("/expected_02_00_11_to_02_00_12.xml");
    }
}
