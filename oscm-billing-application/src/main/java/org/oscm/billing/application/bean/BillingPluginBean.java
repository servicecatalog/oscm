/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                                                                                        
 *******************************************************************************/
package org.oscm.billing.application.bean;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import org.oscm.billingadapterservice.bean.BillingAdapterDAO;
import org.oscm.domobjects.BillingAdapter;
import org.oscm.interceptor.ExceptionMapper;
import org.oscm.interceptor.InvocationDateContainer;
import org.oscm.internal.types.exception.BillingAdapterConnectionException;
import org.oscm.internal.types.exception.BillingApplicationException;

@Stateless
@LocalBean
@Interceptors({ InvocationDateContainer.class, ExceptionMapper.class })
public class BillingPluginBean {

    @EJB
    BillingAdapterDAO billingAdapterDAO;

    /**
     * Test the connection to the external billing system via the billing
     * adapter
     * 
     * @param billingId
     *            the billing adapter identifier
     * @throws BillingApplicationException
     */
    public void testConnection(String billingId)
            throws BillingApplicationException {
        newBillingPluginProxy(billingId).testConnection();
    }

    /**
     * Test the connection to the external billing system via the billing
     * adapter
     * 
     * @param billingAdapter
     *            a BillingAdapter object containing the connection properties
     * @throws BillingApplicationException
     */
    public void testConnection(BillingAdapter billingAdapter)
            throws BillingApplicationException {
        newBillingPluginProxy(billingAdapter).testConnection();
    }

    BillingPluginProxy newBillingPluginProxy(String billingId)
            throws BillingApplicationException {
        return new BillingPluginProxy(getBillingAdapter(billingId));
    }

    BillingPluginProxy newBillingPluginProxy(BillingAdapter billingAdapter) {
        return new BillingPluginProxy(billingAdapter);
    }

    private BillingAdapter getBillingAdapter(String billingId)
            throws BillingApplicationException {
        BillingAdapter billingAdapter = new BillingAdapter();
        billingAdapter.setBillingIdentifier(billingId);
        billingAdapter = billingAdapterDAO.get(billingAdapter);
        if (billingAdapter == null) {
            throw new BillingApplicationException("Unknown Billing Adapter",
                    new BillingAdapterConnectionException(
                            "BillingAdapter for ID " + billingId
                                    + " doesn't exist."));
        }
        return billingAdapter;
    }
}
