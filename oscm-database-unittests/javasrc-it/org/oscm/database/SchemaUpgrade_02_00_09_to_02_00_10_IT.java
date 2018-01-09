/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.database;

import java.net.URL;

import org.oscm.setup.DatabaseVersionInfo;

/**
 * Migrate OrganizationReference. A OrganizationReference is created between
 * PlatformOperator and Supplier and between Supplier and Customer.
 * 
 * @author cheld
 * 
 */
public class SchemaUpgrade_02_00_09_to_02_00_10_IT extends
        SchemaUpgradeTestBase {

    public SchemaUpgrade_02_00_09_to_02_00_10_IT() {
        super(new DatabaseVersionInfo(2, 0, 9), new DatabaseVersionInfo(2, 0,
                10));
    }

    @Override
    protected URL getSetupDataset() {
        return getClass().getResource("/setup_02_00_09_to_02_00_10.xml");
    }

    @Override
    protected URL getExpectedDataset() {
        return getClass().getResource("/expected_02_00_09_to_02_00_10.xml");
    }
}
