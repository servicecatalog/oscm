/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Mike J&auml;ger                                                      
 *                                                                              
 *  Creation Date: 14.07.2009                                                      
 *                                                                              
 *  Completion Time: 04.08.2009                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.types.exceptions;

/**
 * Exception to represent all failures that occur when performing a billing run.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class BillingRunFailed extends RuntimeException {

    private static final long serialVersionUID = -4611120908058977931L;

    public BillingRunFailed(String message) {
        super(message);
    }

    public BillingRunFailed(Exception cause) {
        super(cause);
    }

    public BillingRunFailed(String message, Exception cause) {
        super(message, cause);
    }
}
