/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2012-08-09                                                       
 *                                                                              
 *******************************************************************************/

package org.oscm.types.enumtypes;

/**
 * Specifies the organization role whose revenue share is to be taken into
 * account in the export of billing data.
 * 
 */
public enum BillingSharesResultType {
    /**
     * Broker.
     */
    BROKER,

    /**
     * Marketplace owner.
     */
    MARKETPLACE_OWNER,

    /**
     * Reseller.
     */
    RESELLER,

    /**
     * Supplier.
     */
    SUPPLIER
}
