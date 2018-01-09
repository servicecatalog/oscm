/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.database;

import java.net.URL;

import org.oscm.setup.DatabaseVersionInfo;

/**
 * Db tests for "define allowed payment types per marketable service"
 * 
 * @author brandstetter
 */
public class SchemaUpgrade_02_00_43_to_02_00_44_IT extends
        SchemaUpgradeTestBase {

    public SchemaUpgrade_02_00_43_to_02_00_44_IT() {
        super(new DatabaseVersionInfo(2, 0, 43), new DatabaseVersionInfo(2, 0,
                44));
    }

    @Override
    protected URL getSetupDataset() {
        return getClass().getResource("/setup_02_00_43_to_02_00_44.xml");
    }

    @Override
    protected URL getExpectedDataset() {
        return getClass().getResource("/expected_02_00_43_to_02_00_44.xml");
    }
}
