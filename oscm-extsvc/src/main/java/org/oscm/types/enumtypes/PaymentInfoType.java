/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2009-01-21                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.types.enumtypes;

/**
 * Specifies the available payment types. The operator defines the payment types
 * available to a supplier or reseller organization. The supplier or reseller in
 * turn specifies the available payment types for each of his customers.
 * 
 */
public enum PaymentInfoType {

    /**
     * Payments are collected by direct debit.
     */
    DIRECT_DEBIT,

    /**
     * Costs are charged to a credit card.
     */
    CREDIT_CARD,

    /**
     * The customer pays on receipt of an invoice.
     */
    INVOICE;
}
