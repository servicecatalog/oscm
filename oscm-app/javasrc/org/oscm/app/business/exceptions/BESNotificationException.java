/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 13.09.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.business.exceptions;

/**
 * Internal exception to simplify the control flow for problems which are based
 * on BES connection or notification problems.
 * 
 * @author soehnges
 */
public class BESNotificationException extends Exception {
    private static final long serialVersionUID = -4920101108646166876L;

    public BESNotificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
