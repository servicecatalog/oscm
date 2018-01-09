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
 * @author cmin
 * 
 */
public class SchemaUpgrade_02_05_07_to_02_05_08_IT extends
        SchemaUpgradeTestBase {

    public SchemaUpgrade_02_05_07_to_02_05_08_IT() {
        super(new DatabaseVersionInfo(2, 5, 7),
                new DatabaseVersionInfo(2, 5, 8));
    }

    @Override
    protected URL getSetupDataset() {
        return getClass().getResource("/setup_02_05_07_to_02_05_08.xml");
    }

    @Override
    protected URL getExpectedDataset() {
        return getClass().getResource("/expected_02_05_07_to_02_05_08.xml");
    }
}
