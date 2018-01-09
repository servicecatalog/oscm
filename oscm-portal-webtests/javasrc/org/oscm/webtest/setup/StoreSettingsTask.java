/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: Dirk Bernsau
 *                                                                              
 *  Creation Date: May 23, 2011                                                      
 *                                                                              
 *  Completion Time: June 6, 2011
 *                                                                              
 *******************************************************************************/

package org.oscm.webtest.setup;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * Custom ANT task storing settings for WS-API calls in a project related
 * manner.
 * 
 * @author Dirk Bernsau
 * 
 */
public class StoreSettingsTask extends Task {

    private String userkey;
    private String password;

    @Override
    public void execute() throws BuildException {
        ServiceFactory.create(getProject(), userkey, password);
    }

    public void setUserkey(String userkey) {
        this.userkey = userkey;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
