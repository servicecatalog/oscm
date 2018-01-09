/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.database;

import java.net.URL;

import org.oscm.setup.DatabaseVersionInfo;

/**
 * Db tests for deleting the sessions in the session table. Needed for deleting
 * all sessions with SingleNode in the nodename. In the cluster environment each
 * nodename is the name of the server instance.
 * 
 * @author Stavreva
 * 
 */
public class SchemaUpgrade_02_02_06_to_02_02_07_IT extends
        SchemaUpgradeTestBase {

    public SchemaUpgrade_02_02_06_to_02_02_07_IT() {
        super(new DatabaseVersionInfo(2, 2, 6),
                new DatabaseVersionInfo(2, 2, 7));
    }

    @Override
    protected URL getSetupDataset() {
        return getClass().getResource("/setup_02_02_06_to_02_02_07.xml");
    }

    @Override
    protected URL getExpectedDataset() {
        return getClass().getResource("/expected_02_02_06_to_02_02_07.xml");
    }

}
