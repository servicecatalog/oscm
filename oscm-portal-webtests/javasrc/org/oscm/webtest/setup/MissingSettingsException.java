/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: Dirk Bernsau                                                      
 *                                                                              
 *  Creation Date: June 8, 2011                                                      
 *                                                                              
 *  Completion Time: June 8, 2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.webtest.setup;

/**
 * This exception is thrown when a web service task is called without valid
 * access settings.
 * 
 * @author Dirk Bernsau
 * 
 */
public class MissingSettingsException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public MissingSettingsException(String message) {
        super(message);
    }
}
