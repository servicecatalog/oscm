/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: Dirk Bernsau                                                      
 *                                                                              
 *  Creation Date: Sep 22, 2011                                                      
 *                                                                              
 *  Completion Time: Sep 22, 2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.webtest.setup;

/**
 * Custom ANT task revoking user roles from a user using the WS-API.
 * 
 * @author Dirk Bernsau
 */
public class UserRevokeRolesTask extends UserGrantRolesTask {

    @Override
    public void executeInternal() throws Exception {
        isRevoke = true;
        super.executeInternal();
    }
}
