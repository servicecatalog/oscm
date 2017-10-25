/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 10.12.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.pricemodel.external;

import java.util.Locale;
import java.util.UUID;

import javax.ejb.Remote;

import org.oscm.billing.external.pricemodel.service.PriceModel;
import org.oscm.billing.external.pricemodel.service.PriceModelContent;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.internal.vo.VOServiceDetails;
import org.oscm.internal.vo.VOSubscriptionDetails;

/**
 * External price model service for the CT-MG UI.
 * 
 * @author stavreva
 * 
 */
@Remote
public interface ExternalPriceModelService {

    /**
     * Retrieves the external price model from cache.
     * 
     * @param locale
     *            locale in which the price model tag is requested
     * @param priceModelId
     *            the external price model id (from billing adapter)
     * @return external price model teg
     * @throws ExternalPriceModelException
     */
    public String getCachedPriceModelTag(Locale locale, UUID priceModelId)
            throws ExternalPriceModelException;

    /**
     * Retrieves the external price model from cache.
     * 
     * @param locale
     *            locale in which the price model tag is requested
     * @param priceModelId
     *            the external price model id (from billing adapter)
     * @return external price model
     * @throws ExternalPriceModelException
     */
    public PriceModelContent getCachedPriceModel(Locale locale,
            UUID priceModelId) throws ExternalPriceModelException;

    /**
     * 
     * Updates cache with given external price model
     * 
     * @param externalPriceModel
     *            external price model to be updated
     * @throws ExternalPriceModelException
     */
    public void updateCache(PriceModel externalPriceModel)
            throws ExternalPriceModelException;

    /**
     * Retrieves the external price model form related billing adapter based on
     * selected service
     * 
     * @param service
     *            selected service
     * @return external price model
     * @throws ExternalPriceModelException
     */
    public PriceModel getExternalPriceModelForService(VOServiceDetails service)
            throws ExternalPriceModelException;

    /**
     * Retrieves the external price model from related billing adapter based on
     * selected service and customer
     * 
     * @param service
     *            selected service
     * @param customer
     *            selected customer
     * @return external price model
     * @throws ExternalPriceModelException
     */
    public PriceModel getExternalPriceModelForCustomer(
            VOServiceDetails service, VOOrganization customer)
            throws ExternalPriceModelException;

    /**
     * Retrieves the external price model from related billing adapter based on
     * selected service and subscription
     * 
     * @param subscription
     *            selected subscription
     * @return external price model
     * @throws ExternalPriceModelException
     */

    public PriceModel getExternalPriceModelForSubscription(
            VOSubscriptionDetails subscription)
            throws ExternalPriceModelException;
}