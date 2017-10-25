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

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import org.oscm.billing.external.context.ContextKey;
import org.oscm.billing.external.context.ContextValue;
import org.oscm.billing.external.pricemodel.service.PriceModel;
import org.oscm.billingadapterservice.bean.BillingAdapterDAO;
import org.oscm.domobjects.BillingAdapter;
import org.oscm.interceptor.ExceptionMapper;
import org.oscm.interceptor.InvocationDateContainer;
import org.oscm.internal.types.exception.BillingAdapterConnectionException;
import org.oscm.internal.types.exception.BillingApplicationException;

@Stateless
@LocalBean
@Interceptors({ InvocationDateContainer.class, ExceptionMapper.class })
public class PriceModelPluginBean {

    @EJB
    BillingAdapterDAO billingAdapterDAO;

    public PriceModel getPriceModel(String billingId, Set<Locale> locales,
            Map<ContextKey, ContextValue<?>> context)
            throws BillingApplicationException {

        PriceModel priceModel = newPriceModelPluginProxy(billingId)
                .getPriceModel(context, locales);

        return priceModel;
    }

    PriceModelPluginProxy newPriceModelPluginProxy(String billingId)
            throws BillingApplicationException {
        return new PriceModelPluginProxy(getBillingAdapter(billingId));
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