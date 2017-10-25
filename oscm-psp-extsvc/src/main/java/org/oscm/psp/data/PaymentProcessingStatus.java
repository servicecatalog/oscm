/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2010-01-26                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.psp.data;

/**
 * Specifies the statuses of payment processing.
 * 
 */
public enum PaymentProcessingStatus {

    /**
     * The payment processing was completed successfully.
     */
    SUCCESS,

    /**
     * The payment processing failed (e.g. due to a communication problem), but
     * will be retried later.
     */
    RETRY,

    /**
     * The payment processing failed on the PSP side and will not be retried.
     */
    FAILED_EXTERNAL,

    /**
     * The payment processing failed due to an internal problem in the platform,
     * for example, a problem in parsing the billing result XML structure.
     */
    FAILED_INTERNAL;

}
