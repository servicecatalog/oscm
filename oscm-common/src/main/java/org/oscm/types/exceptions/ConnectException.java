/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2013-7-29                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.types.exceptions;

import org.oscm.internal.types.exception.SaaSSystemException;

/**
 * @author Yu Yin
 */
public class ConnectException extends SaaSSystemException {

    private static final long serialVersionUID = -4048732073960231107L;
   
    public ConnectException(String message, Throwable cause) {
        super(message, cause);
    }

}
