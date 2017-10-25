/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: G&uuml;nther Schmid                                                       
 *                                                                              
 *  Creation Date: 04.03.2009                                                      
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
public class UserNotAssignedException extends SaaSApplicationException {

    private static final long serialVersionUID = 1L;

    public UserNotAssignedException(String subid, String userid) {
        super("User '" + userid + "' is not yet assigned to subscription '"
                + subid + "' (or assignment is deactivated)");
    }

}
