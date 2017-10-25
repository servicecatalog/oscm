/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2014-12-10                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billing.external.pricemodel.service;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.oscm.billing.external.context.ContextKey;
import org.oscm.billing.external.context.ContextValue;

/**
 * Represents the localized content and tag of a price model defined in an
 * external billing system.
 */
public class PriceModel implements Serializable {

    private static final long serialVersionUID = 630996128479608150L;

    private UUID id;
    private Map<ContextKey, ContextValue<?>> context;
    private Map<Locale, PriceModelContent> localizedPriceModelContent = new HashMap<Locale, PriceModelContent>();

    /**
     * Constructs a price model.
     * 
     * @param id
     *            the unique identifier of the price model.
     * 
     */
    public PriceModel(UUID id) {
        this.id = id;
    }

    /**
     * Returns the unique identifier of the price model.
     * 
     * @return the identifier
     */
    public UUID getId() {
        return id;
    }

    /**
     * Sets the unique identifier of the price model.
     * 
     * @param id
     *            the identifier
     */
    public void setId(UUID id) {
        this.id = id;
    }

    /**
     * Returns the context of the price model.
     * <p>
     * The context provides details of the element for which the price model is
     * defined.
     * <p>
     * For service price models, the context consists of:
     * <ul>
     * <li><code>SERVICE_ID</code>
     * <li><code>SERVICE_NAME</code>
     * <li><code>SERVICE_PARAMETERS</code>
     * </ul>
     * For a customer price model, the context consists of:
     * <ul>
     * <li><code>SERVICE_ID</code>
     * <li><code>SERVICE_NAME</code>
     * <li><code>SERVICE_PARAMETERS</code>
     * <li><code>CUSTOMER_ID</code>
     * <li><code>CUSTOMER_NAME</code>
     * </ul>
     * For a subscription price model, the context consists of:
     * <ul>
     * <li><code>SUBSCRIPTION_ID</code>
     * <li><code>TENANT_ID</code> - only required for pushing price models
     * </ul>
     * 
     * @return the context, consisting of the required keys and values
     */
    public Map<ContextKey, ContextValue<?>> getContext() {
        return context;
    }

    /**
     * Sets the context of the price model.
     * <p>
     * The context provides details of the element for which the price model is
     * defined.
     * <p>
     * For service price models, the context consists of:
     * <ul>
     * <li><code>SERVICE_ID</code>
     * <li><code>SERVICE_NAME</code>
     * <li><code>SERVICE_PARAMETERS</code>
     * </ul>
     * For a customer price model, the context consists of:
     * <ul>
     * <li><code>SERVICE_ID</code>
     * <li><code>SERVICE_NAME</code>
     * <li><code>SERVICE_PARAMETERS</code>
     * <li><code>CUSTOMER_ID</code>
     * <li><code>CUSTOMER_NAME</code>
     * </ul>
     * For a subscription price model, the context consists of:
     * <ul>
     * <li><code>SUBSCRIPTION_ID</code>
     * <li><code>TENANT_ID</code> - only required for pushing price models
     * </ul>
     * 
     * @param context
     *            the context, consisting of the required keys and values
     */
    public void setContext(Map<ContextKey, ContextValue<?>> context) {
        this.context = context;
    }

    /**
     * Adds or updates the content of the price model for the given locale.
     * 
     * @param locale
     *            the locale
     * @param content
     *            the content
     * @throws NullPointerException
     *             if the specified locale or content is <code>null</code>
     */
    public void put(Locale locale, PriceModelContent content) {
        localizedPriceModelContent.put(locale, content);
    }

    /**
     * Retrieves the locales for the price model.
     * 
     * @return a set of locales
     */
    public Set<Locale> getLocales() {
        return localizedPriceModelContent.keySet();
    }

    /**
     * Retrieves the content of the price model for the given locale.
     * 
     * @param locale
     *            the locale
     * @return the content, or <code>null</code> if no content is available for
     *         the specified locale
     */
    public PriceModelContent get(Locale locale) {
        return localizedPriceModelContent.get(locale);
    }
}
