/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2014-08-26                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billing.external.pricemodel.service;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.ejb.Remote;

import org.oscm.billing.external.context.ContextKey;
import org.oscm.billing.external.context.ContextValue;
import org.oscm.billing.external.exception.BillingException;

/**
 * Interface for retrieving price models from external billing systems and
 * returning them to Catalog Manager.
 * <p>
 * The <code>getPriceModel</code> method is invoked by Catalog Manager when a
 * supplier imports a price model for a service, customer, or subscription. The
 * billing adapter must provide the appropriate price model depending on the
 * context.
 * 
 */
@Remote
public interface PriceModelPluginService {

    /**
     * Retrieves a price model for the given context and locales from the
     * external billing system.
     * 
     * @param context
     *            the context specifying the service or subscription for which
     *            the price model is to be returned
     * @param locales
     *            the locales for which the price model is to be returned. If
     *            the set is <code>null</code> or empty, or if any of the given
     *            locales are not supported, the price model is returned for the
     *            default locale.
     * @return a price model object with the localized content and an optional
     *         tag, or <code>null</code> if the context is empty or if the price
     *         model is not found.
     * @throws BillingException
     */
    public PriceModel getPriceModel(Map<ContextKey, ContextValue<?>> context,
            Set<Locale> locales) throws BillingException;
}
