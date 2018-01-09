/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.database;

import java.net.URL;

import org.oscm.setup.DatabaseVersionInfo;

/**
 * Db tests refactoring of localized resource => now designed as regular domain
 * objects.
 * 
 * @author Mike J&auml;ger
 */
public class SchemaUpgrade_02_00_32_to_02_00_33_IT extends
        SchemaUpgradeTestBase {

    public SchemaUpgrade_02_00_32_to_02_00_33_IT() {
        super(new DatabaseVersionInfo(2, 0, 32), new DatabaseVersionInfo(2, 0,
                33));
    }

    @Override
    protected URL getSetupDataset() {
        return getClass().getResource("/setup_02_00_32_to_02_00_33.xml");
    }

    @Override
    protected URL getExpectedDataset() {
        return getClass().getResource("/expected_02_00_32_to_02_00_33.xml");
    }
}
