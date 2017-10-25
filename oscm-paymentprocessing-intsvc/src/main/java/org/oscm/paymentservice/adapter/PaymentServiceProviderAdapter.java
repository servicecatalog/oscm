/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 19.06.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.paymentservice.adapter;

import org.oscm.psp.intf.PaymentServiceProvider;

/**
 * @author weiser
 * 
 */
public interface PaymentServiceProviderAdapter extends PaymentServiceProvider {

    /**
     * Sets the reference to the web service , that implements the
     * version-appropriate PaymentServiceProvider.
     * 
     * @param pspInterface
     *            the reference to the payment service provider interface
     */
    public void setPaymentServiceProviderService(Object pspInterface);

}
