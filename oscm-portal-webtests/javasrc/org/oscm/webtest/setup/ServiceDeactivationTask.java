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

import org.oscm.internal.types.exception.SaaSApplicationException;

/**
 * Custom ANT task deactivating the service specified by service IDs.
 * 
 * @author Dirk Bernsau
 * 
 */
public class ServiceDeactivationTask extends ServiceActivationTask {

    @Override
    public void setServiceIds(String value) {
        super.setServiceIds(value);
    }

    @Override
    public void executeInternal() throws BuildException,
            SaaSApplicationException {
        active = false;
        super.executeInternal();
    }
}
