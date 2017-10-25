/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 18.07.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.pricing;

import java.util.List;

import javax.ejb.Remote;

import org.oscm.internal.components.response.Response;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.SaaSApplicationException;
import org.oscm.internal.types.exception.ServiceOperationException;
import org.oscm.internal.types.exception.ServiceStateException;
import org.oscm.internal.types.exception.ValidationException;

/**
 * Contains viewing and modifying of the price models for the partner shared
 * revenue model.
 * 
 * @author barzu
 */
@Remote
public interface PricingService {

    /**
     * Returns the marketplace revenue share of a marketplace.
     * 
     * <p>
     * Required role: marketplace owner, platform operator, supplier, broker, or
     * reseller
     * 
     * @param marketplaceId
     *            a String representing the id of the marketplace
     * @return A Response object whose list of results contains a
     *         POMarketplacePriceModel
     * @throws ObjectNotFoundException
     *             if no marketplace with the given identified is found.
     */
    public Response getMarketplaceRevenueShares(String marketplaceId)
            throws ObjectNotFoundException;

    /**
     * Returns the partner revenue shares of a marketplace.
     * 
     * <p>
     * Required role: marketplace owner, platform operator, supplier, broker, or
     * reseller
     * 
     * @param marketplaceId
     *            a String representing the id of the marketplace
     * @return A Response object whose list of results contains a
     *         POPartnerPriceModel
     * @throws ObjectNotFoundException
     *             if the marketplace is not found by its identifier
     */
    public Response getPartnerRevenueSharesForMarketplace(String marketplaceId)
            throws ObjectNotFoundException;

    /**
     * Returns the partner revenue shares of a service.
     * 
     * <p>
     * Required role: marketplace owner, platform operator, supplier, broker, or
     * reseller
     * 
     * @param service
     *            a POServiceForPricing
     * @throws SaaSApplicationException
     * @return A Response object whose list of results contains a
     *         POPartnerPriceModel
     * @throws ObjectNotFoundException
     *             if the product with the given key is not found
     * @throws OperationNotPermittedException
     *             if the caller organization has the role SUPPLIER, BROKER,
     *             RESELLER and is not the owner of the given product
     * @throws ServiceOperationException
     *             if the service is a subscription-specific copy, or if the
     *             service is a template which is not assigned to any
     *             marketplace and the catalog entry has no price models
     * @throws ServiceStateException
     *             if the service has the state of DELETED, SUSPENDED, OBSOLETE
     */
    public Response getPartnerRevenueShareForService(POServiceForPricing service)
            throws ObjectNotFoundException, OperationNotPermittedException,
            ServiceOperationException, ServiceStateException;

    /**
     * Returns the partner revenue shares of a service in all states.
     * 
     * <p>
     * Required role: marketplace owner, platform operator, supplier, broker, or
     * reseller
     * 
     * @param service
     *            a POServiceForPricing
     * @return A Response object whose list of results contains a
     *         POPartnerPriceModel
     * @throws ObjectNotFoundException
     *             if the product with the given key is not found
     * @throws OperationNotPermittedException
     * @throws ServiceOperationException
     */
    Response getPartnerRevenueShareForAllStatesService(
            POServiceForPricing service) throws ObjectNotFoundException,
            OperationNotPermittedException, ServiceOperationException,
            ServiceStateException;

    /**
     * Retrieves the operator revenue share for a product, fetched from the
     * catalog entry. For convenience, also the default operator revenue share
     * for the vendor organization is added to the response.
     * 
     * <p>
     * Required role: platform operator, supplier, broker, or reseller
     * 
     * @param serviceKey
     *            the key of the service to retrieve the operator revenue share
     *            for.
     * @return A response containing operator revenue share as
     *         {@link POOperatorPriceModel}.
     * @throws ObjectNotFoundException
     *             if specified service is not found
     * @throws OperationNotPermittedException
     *             if the caller organization has no PLATFORM_OPERATOR, no
     *             MARKETPLACE_OWNER role and is not the owner of the given
     *             product.
     */
    public Response getOperatorRevenueShare(long serviceKey)
            throws ObjectNotFoundException, OperationNotPermittedException;

    /**
     * Updates the operator revenue share for the specified service.
     * <p>
     * Required role: platform operator
     * 
     * @param serviceKey
     *            the key of the service to update the operator revenue share
     *            for.
     * @param revenueShare
     *            object containing the new value of the operator revenue share
     *            for the specified service.
     * @return an empty Response object.
     * 
     * @throws ObjectNotFoundException
     *             if no service could be found with the specified key.
     * @throws ValidationException
     *             if the specified revenue share is not a valid percent value.
     * @throws ServiceOperationException
     *             if the specified product does is not a TEMPLATE product.
     * @throws ConcurrentModificationException
     *             if the same revenue share was modified by another user
     *             between the loading and saving time.
     */
    public Response saveOperatorRevenueShare(long serviceKey,
            PORevenueShare revenueShare) throws ObjectNotFoundException,
            ValidationException, ServiceOperationException,
            ConcurrentModificationException;

    /**
     * Updates the partner revenue shares for a list of services.
     * <p>
     * Required role: platform operator
     * 
     * @param pricingsList
     *            A List<POServicePricing> containing a list of services to be
     *            updated and their respective partner revenue shares.
     * @throws SaaSApplicationException
     * @return
     * @throws ObjectNotFoundException
     *             if any of the specified services are not found
     * @throws NonUniqueBusinessKeyException
     *             if any of the specified revenue shares already exist in the
     *             database with the same business key
     * @throws ValidationException
     *             if any of the specified revenue shares is missing or is not a
     *             valid percent between <code>1</code> and <code>100</code>
     * @throws ServiceOperationException
     *             if any of the specified services are neither a template not a
     *             partner template
     * @throws ConcurrentModificationException
     *             if any of the specified revenue shares were modified by
     *             another session between the loading and saving time
     */
    public Response savePartnerRevenueSharesForServices(
            List<POServicePricing> pricingsList)
            throws ObjectNotFoundException, NonUniqueBusinessKeyException,
            ValidationException, ServiceOperationException,
            ConcurrentModificationException, ServiceStateException;

    /**
     * Retrieves the pricing (containing a marketplace PO and all revenue
     * shares) of a marketplace.
     * <p>
     * Required role: supplier, broker, or reseller
     * 
     * if successful, return the List of marketplace pricings with one element
     * (List<POMarketplacePricing>) in Result
     * 
     * @param marketplaceId
     *            the id of the marketplace for which to retrieve the pricing
     * @return a Response object containing the POMarketplacePricing
     * @throws ObjectNotFoundException
     *             if no marketplace could be found in the database with the
     *             specified identifier
     */
    public Response getPricingForMarketplace(String marketplaceId)
            throws ObjectNotFoundException;

    /**
     * Retrieves the pricing (containing a marketplace PO and all revenue
     * shares) of a marketplace for a specific service.
     * <p>
     * Required role: supplier, broker, or reseller
     * 
     * @param service
     *            a POServiceForPricing for which to retrieve the partner
     *            revenue shares
     * @return Response object containing the POMarketplacePricing, or Response
     *         object having no values when the service has no catalog entry or
     *         no marketplace.
     * @throws ObjectNotFoundException
     *             if the specified product is not found by its key
     * @throws ServiceOperationException
     *             if the service is not a template or a partner template
     */
    public Response getMarketplacePricingForService(POServiceForPricing service)
            throws ObjectNotFoundException, ServiceOperationException,
            ServiceStateException;

    /**
     * Retrieves the services that are no service copies and that have the
     * status ACTIVE or INACTIVE.
     * <p>
     * Required role: platform operator
     * 
     * if successful return the List of services(List<POServiceForPricing>) in
     * Result
     * 
     * @return Response object containing the POServiceForPricing
     */
    public Response getTemplateServices();

    /**
     * Retrieves the list of pricings of all partners for a service.
     * <p>
     * Required role: platform operator
     * 
     * @param service
     *            a POServiceForPricing for which to retrieve the partner
     *            revenue shares
     * @return Response object containing the List of service pricings
     *         (List<POServicePricing>) in Result
     * @throws ObjectNotFoundException
     *             if the specified product with the given key is not found
     * @throws ServiceOperationException
     *             if the service is not a template
     */
    public Response getPartnerServicesWithRevenueShareForTemplate(
            POServiceForPricing service) throws ObjectNotFoundException,
            ServiceOperationException;

}
