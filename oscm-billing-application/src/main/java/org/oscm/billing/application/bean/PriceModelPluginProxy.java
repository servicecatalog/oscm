/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 10.12.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billing.application.bean;

import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import org.oscm.billing.external.context.ContextKey;
import org.oscm.billing.external.context.ContextValue;
import org.oscm.billing.external.pricemodel.service.PriceModel;
import org.oscm.billing.external.pricemodel.service.PriceModelPluginService;
import org.oscm.domobjects.BillingAdapter;
import org.oscm.internal.types.exception.BillingApplicationException;

/**
 * @author baumann
 */
public class PriceModelPluginProxy extends BasicBillingProxy {

    PriceModelPluginService priceModelPluginService;

    public PriceModelPluginProxy(BillingAdapter billingAdapter) {
        super(billingAdapter);
    }

    PriceModelPluginService locatePriceModelPluginService()
            throws BillingApplicationException {
        if (priceModelPluginService == null) {
            priceModelPluginService = locateBillingAdapterService(PriceModelPluginService.class);
        }
        return priceModelPluginService;
    }

    public PriceModel getPriceModel(
            final Map<ContextKey, ContextValue<?>> context,
            final Set<Locale> locales) throws BillingApplicationException {

        Callable<PriceModel> callable = new Callable<PriceModel>() {
            @Override
            public PriceModel call() throws Exception {
                return locatePriceModelPluginService().getPriceModel(context,
                        locales);
            }
        };

        return getAdapterResult(submitAdapterCall(callable));
    }

}