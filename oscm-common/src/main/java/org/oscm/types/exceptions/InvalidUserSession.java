/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Mike J&auml;ger                                                      
 *                                                                              
 *  Creation Date: 24.02.2009                                                      
 *                                                                              
 *  Completion Time: 17.06.2009                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.types.exceptions;

import org.oscm.internal.types.exception.SaaSSystemException;

/**
 * Represents all scenarios where a user cannot be found in the system although
 * he is the principal of the current transaction.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class InvalidUserSession extends SaaSSystemException {

    private static final long serialVersionUID = 3401685586321061777L;

    public InvalidUserSession(String message) {
        super(message);
    }

    public InvalidUserSession(String message, Throwable cause) {
        super(message, cause);
    }

}
