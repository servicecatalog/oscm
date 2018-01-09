/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.database;

import java.net.URL;

import org.oscm.setup.DatabaseVersionInfo;

/**
 * @author Enes Sejfi
 */
public class SchemaUpgrade_02_00_30_to_02_00_31_IT extends
        SchemaUpgradeTestBase {

    public SchemaUpgrade_02_00_30_to_02_00_31_IT() {
        super(new DatabaseVersionInfo(2, 0, 30), new DatabaseVersionInfo(2, 0,
                31));
    }

    @Override
    protected URL getSetupDataset() {
        return getClass().getResource("/setup_02_00_30_to_02_00_31.xml");
    }

    @Override
    protected URL getExpectedDataset() {
        return getClass().getResource("/expected_02_00_30_to_02_00_31.xml");
    }
}
