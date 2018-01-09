/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.database;

import java.net.URL;

import org.oscm.setup.DatabaseVersionInfo;

/**
 * Db tests for adding freeperiod column to pricemodel and pricemodelhistory.
 * 
 * @author brandstetter
 */
public class SchemaUpgrade_02_00_45_to_02_00_46_IT extends
        SchemaUpgradeTestBase {

    public SchemaUpgrade_02_00_45_to_02_00_46_IT() {
        super(new DatabaseVersionInfo(2, 0, 45), new DatabaseVersionInfo(2, 0,
                46));
    }

    @Override
    protected URL getSetupDataset() {
        return getClass().getResource("/setup_02_00_45_to_02_00_46.xml");
    }

    @Override
    protected URL getExpectedDataset() {
        return getClass().getResource("/expected_02_00_45_to_02_00_46.xml");
    }
}
