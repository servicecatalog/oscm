/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.database;

import java.net.URL;

import org.oscm.setup.DatabaseVersionInfo;

/**
 * Db tests for setting the remote ldap flag on organizations if required
 * (existing organization settings as indicator).
 */
public class SchemaUpgrade_02_02_09_to_02_02_10_IT extends
        SchemaUpgradeTestBase {

    public SchemaUpgrade_02_02_09_to_02_02_10_IT() {
        super(new DatabaseVersionInfo(2, 2, 9), new DatabaseVersionInfo(2, 2,
                10));
    }

    @Override
    protected URL getSetupDataset() {
        return getClass().getResource("/setup_02_02_09_to_02_02_10.xml");
    }

    @Override
    protected URL getExpectedDataset() {
        return getClass().getResource("/expected_02_02_09_to_02_02_10.xml");
    }

}
