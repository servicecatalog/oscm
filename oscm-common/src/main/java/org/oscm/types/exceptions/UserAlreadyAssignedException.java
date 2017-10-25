/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: G&uuml;nther Schmid                                                       
 *                                                                              
 *  Creation Date: 18.02.2009                                                      
 *                                                                              
 *  Completion Time: 17.06.2009                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.types.exceptions;

import org.oscm.internal.types.exception.SaaSApplicationException;

/**
 * @author G&uuml;nther Schmid
 * 
 */
public class UserAlreadyAssignedException extends SaaSApplicationException {

    private static final long serialVersionUID = 1L;

    public UserAlreadyAssignedException(String subid, String userid) {
        super("User '" + userid + "' is already assigned to subscription '"
                + subid + "'");
    }
}
