/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.database;

import java.net.URL;

import org.oscm.setup.DatabaseVersionInfo;

/**
 * Db tests for migration of configrationsetting
 * TIMER_INTERVAL_SUBSCRIPTION_EXPIRATION value.
 * 
 * @author tokoda
 */
public class SchemaUpgrade_02_00_46_to_02_00_47_IT extends
        SchemaUpgradeTestBase {

    public SchemaUpgrade_02_00_46_to_02_00_47_IT() {
        super(new DatabaseVersionInfo(2, 0, 46), new DatabaseVersionInfo(2, 0,
                47));
    }

    @Override
    protected URL getSetupDataset() {
        return getClass().getResource("/setup_02_00_46_to_02_00_47.xml");
    }

    @Override
    protected URL getExpectedDataset() {
        return getClass().getResource("/expected_02_00_46_to_02_00_47.xml");
    }
}
