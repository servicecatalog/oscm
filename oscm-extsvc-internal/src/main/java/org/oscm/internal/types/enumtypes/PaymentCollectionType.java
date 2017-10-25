/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2009-01-21                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.types.enumtypes;

/**
 * Specifies the way payment collection is performed.
 * 
 */
public enum PaymentCollectionType {

    /**
     * The supplier or reseller is responsible for creating invoices and
     * charging the customers for the costs.
     */
    ORGANIZATION,

    /**
     * Payment related tasks are managed by a payment service provider (PSP).
     */
    PAYMENT_SERVICE_PROVIDER;
}
