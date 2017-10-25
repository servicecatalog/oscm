/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 10.12.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billing.application.bean;

import java.util.concurrent.Callable;

import org.oscm.billing.external.billing.service.BillingPluginService;
import org.oscm.domobjects.BillingAdapter;
import org.oscm.internal.types.exception.BillingApplicationException;

/**
 * @author baumann
 */
public class BillingPluginProxy extends BasicBillingProxy {

    BillingPluginService billingPluginService;

    public BillingPluginProxy(BillingAdapter billingAdapter) {
        super(billingAdapter);
    }

    BillingPluginService locateBillingPluginService()
            throws BillingApplicationException {
        if (billingPluginService == null) {
            billingPluginService = locateBillingAdapterService(BillingPluginService.class);
        }
        return billingPluginService;
    }

    public void testConnection() throws BillingApplicationException {

        Callable<Void> callable = new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                locateBillingPluginService().testConnection();
                return null;
            }
        };

        getAdapterResult(submitAdapterCall(callable));
    }

}
