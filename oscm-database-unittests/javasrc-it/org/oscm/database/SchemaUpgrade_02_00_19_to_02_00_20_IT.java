/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.database;

import java.net.URL;

import org.oscm.setup.DatabaseVersionInfo;

/**
 * Test case for migrating existing organizations. Entries for
 * OrganizationToCountry and OrganizationToCountryHistory are added for each
 * Organization.
 * 
 * @author farmaki
 * 
 */
public class SchemaUpgrade_02_00_19_to_02_00_20_IT extends
        SchemaUpgradeTestBase {

    public SchemaUpgrade_02_00_19_to_02_00_20_IT() {
        super(new DatabaseVersionInfo(2, 0, 19), new DatabaseVersionInfo(2, 0,
                20));
    }

    @Override
    protected URL getSetupDataset() {
        return getClass().getResource("/setup_02_00_19_to_02_00_20.xml");
    }

    @Override
    protected URL getExpectedDataset() {
        return getClass().getResource("/expected_02_00_19_to_02_00_20.xml");
    }
}
