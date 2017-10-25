/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 26.01.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.types.enumtypes;

/**
 * Enumeration representing the possible status information for a payment
 * operation.
 * 
 * @author Mike J&auml;ger
 * 
 */
public enum PaymentProcessingStatus {

    /**
     * Indicates that the payment process was handled successfully.
     */
    SUCCESS,

    /**
     * Indicates that the payment process failed due to e.g. a communication
     * exception but that the payment process will be retried.
     */
    RETRY,

    /**
     * Indicates that the payment process failed on PSP side and that it will
     * not be retried.
     */
    FAILED_EXTERNAL,

    /**
     * Indicates taht the payment process failed due to internal problems, e.g.
     * problems parsing the billing result XML structure.
     */
    FAILED_INTERNAL;

}
