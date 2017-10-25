/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 20.01.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.dbtask;

import java.sql.PreparedStatement;

/**
 * Inserts the platform operator user data and history with the calculated
 * password hash.
 * 
 * @author weiser
 * 
 */
public class CreateOperatorUser extends DatabaseUpgradeTask {

    private static final byte[] PWD_HASH = new byte[] { 1, 111, 107, 121, -13,
            -49, -67, -7, -62, 101, -21, -5, 80, -122, -97, 72, -100, 12, -21,
            -103, 40, 66, -84, 23, 90, 107, -45, 105, 84, 112, 36, -49 };
    private static final String INSERT_USER = "INSERT INTO \"platformuser\" (\"tkey\", \"version\", \"creationdate\", \"locale\", \"failedlogincounter\", \"organizationadmin\", \"status\" , \"passwordsalt\", \"passwordhash\", \"userid\", \"organizationkey\") VALUES (1000, 0, date_part('epoch', now())*1000, 'en', 0, true, 'ACTIVE', 0, ?, 'administrator', 1);";
    private static final String INSERT_USER_HISTORY = "INSERT INTO \"platformuserhistory\" (\"tkey\", \"moddate\", \"modtype\", \"moduser\", \"objkey\", \"objversion\", \"creationdate\", \"locale\", \"failedlogincounter\", \"organizationadmin\", \"status\" , \"passwordsalt\", \"passwordhash\", \"userid\", \"organizationobjkey\") VALUES (1, now(), 'ADD', 'ANONYMOUS', 1, 0, date_part('epoch', now())*1000, 'en', 0, true, 'ACTIVE', 0, ?, 'administrator', 1);";

    @Override
    public void execute() throws Exception {
        PreparedStatement stmt = getPreparedStatement(INSERT_USER);
        stmt.setBytes(1, PWD_HASH);
        stmt.execute();
        stmt = getPreparedStatement(INSERT_USER_HISTORY);
        stmt.setBytes(1, PWD_HASH);
        stmt.execute();
    }

}
