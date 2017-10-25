/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 2012-08-06                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.types.enumtypes;

/**
 * Specifies the type of a resale permission for a service.
 */
public enum OfferingType {

    /**
     * The service is offered by a broker.
     */
    BROKER,

    /**
     * The service is offered by a reseller.
     */
    RESELLER,

    /**
     * The service is offered directly by the supplier.
     */
    DIRECT;

}
