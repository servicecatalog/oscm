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
 * @author cheld
 * 
 */
public class SchemaUpgrade_01_01_21_to_01_01_22_IT extends
        SchemaUpgradeTestBase {

    public SchemaUpgrade_01_01_21_to_01_01_22_IT() {
        super(new DatabaseVersionInfo(1, 1, 21), new DatabaseVersionInfo(1, 1,
                22));
    }

    @Override
    protected URL getSetupDataset() {
        return getClass().getResource("/setup_01_01_21_to_01_01_22.xml");
    }

    @Override
    protected URL getExpectedDataset() {
        return getClass().getResource("/expected_01_01_21_to_01_01_22.xml");
    }
}
