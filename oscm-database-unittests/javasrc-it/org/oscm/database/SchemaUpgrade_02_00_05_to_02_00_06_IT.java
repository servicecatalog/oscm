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
 * @author groch
 * 
 */
public class SchemaUpgrade_02_00_05_to_02_00_06_IT extends
        SchemaUpgradeTestBase {

    public SchemaUpgrade_02_00_05_to_02_00_06_IT() {
        super(new DatabaseVersionInfo(2, 0, 5),
                new DatabaseVersionInfo(2, 0, 6));
    }

    @Override
    protected URL getSetupDataset() {
        return getClass().getResource("/setup_02_00_05_to_02_00_06.xml");
    }

    @Override
    protected URL getExpectedDataset() {
        return getClass().getResource("/expected_02_00_05_to_02_00_06.xml");
    }
}
