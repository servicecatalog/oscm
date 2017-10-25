/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Jul 16, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.service;

import java.util.List;

import javax.ejb.Remote;

import org.oscm.internal.components.response.Response;
import org.oscm.internal.resalepermissions.POResalePermissionDetails;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.OrganizationAuthorityException;
import org.oscm.internal.types.exception.PublishingToMarketplaceNotPermittedException;
import org.oscm.internal.types.exception.ServiceOperationException;
import org.oscm.internal.types.exception.ServiceStateException;
import org.oscm.internal.types.exception.ValidationException;

@Remote
public interface PublishService {

    /**
     * Returns the details of the service specified by the given key.
     * <p>
     * Roles allowed: manager of a supplier, broker or reseller organization.
     * 
     * @param serviceKey
     *            the key of the service to return
     * @return A <code>Response</code> object containing the service details as
     *         <code>POServiceForPublish</code> and the partner price model
     *         details as <code>POPartnerPriceModel</code>.
     * @throws ObjectNotFoundException
     *             if the service itself, its catalog entry or the partner price
     *             model details could not be found in the database
     * @throws OperationNotPermittedException
     *             if the calling user's organization is neither the supplier of
     *             the service nor an authorized broker or reseller
     * @throws ServiceOperationException
     *             if the service is a subscription-specific copy, or if the
     *             service is a template which is not assigned to any
     *             marketplace and the catalog entry has no price models
     * @throws ServiceStateException
     *             if the service has the state of DELETED, SUSPENDED, OBSOLETE
     */
    public Response getServiceDetails(long serviceKey)
            throws ObjectNotFoundException, OperationNotPermittedException,
            ServiceOperationException, ServiceStateException;

    /**
     * Updates the publishing options of a service and, if the caller belongs to
     * a supplier organization, updates its resale permissions.
     * <p>
     * Roles allowed: manager of a supplier, broker or reseller organization.
     * 
     * @param service
     *            the service to be updated
     * @param permissionsToGrant
     *            the list of all resellers and brokers to grant resale
     *            permissions for, in case the caller is a supplier
     * @param permissionsToRevoke
     *            the list of all resellers and brokers to revoke the resale
     *            permissions for, in case the caller is a supplier
     * @return An empty <code>Response</code> object
     * @throws ValidationException
     *             if either the specified service or any of the resale
     *             permissions are not valid
     * @throws ObjectNotFoundException
     *             if either the specified service, the resale copies or an
     *             organization is not found
     * @throws NonUniqueBusinessKeyException
     *             if a service copy or a new catalog entry cannot be created,
     *             because an entity with the same key already exists in the
     *             database
     * @throws OperationNotPermittedException
     *             if a service template doesn't belong to the corresponding
     *             grantor
     * @throws ServiceOperationException
     *             if a service template is no template or if a service template
     *             has no valid price model defined for itself or if a service
     *             template is not assigned to a marketplace
     * @throws ConcurrentModificationException
     *             if the same resale permission is granted by another user at
     *             the same time
     * @throws OrganizationAuthorityException
     *             if a grantee organization has neither the BROKER nor the
     *             RESELLER role
     * @throws ServiceStateException
     *             if a service template is not in status INACTIVE or ACTIVE
     */
    public Response updateAndPublishService(POServiceForPublish service,
            List<POResalePermissionDetails> permissionsToGrant,
            List<POResalePermissionDetails> permissionsToRevoke)
            throws ValidationException, ObjectNotFoundException,
            NonUniqueBusinessKeyException,
            PublishingToMarketplaceNotPermittedException,
            OperationNotPermittedException, ServiceOperationException,
            ConcurrentModificationException, OrganizationAuthorityException,
            ServiceStateException;

    /**
     * Retrieves the categories localized as specified and the marketplace and
     * partner revenue shares for the specified marketplace.
     * 
     * @param marketplaceId
     *            the identifier of the marketplace
     * @param locale
     *            the locale key to retrieve the localized category names with
     * @return a <code>Response</code> object containing a list of
     *         <code>VOCategory</code>, a <code>POMarketplacePriceModel</code>
     *         and a <code>POPartnerPriceModel</code>
     * @throws ObjectNotFoundException
     *             if the marketplace is not found by its identifier
     */
    public Response getCategoriesAndRvenueShare(String marketplaceId,
            String locale) throws ObjectNotFoundException;

    /**
     * Returns the brokers that have resale permissions for the specified
     * service.
     * 
     * @param serviceKey
     *            the service key to retrieve the brokers for
     * @return a <code>Response</code> object containing a list of
     *         <code>POPartner</code> objects.
     */
    public Response getBrokers(long serviceKey);

    /**
     * Returns the resellers that have resale permissions for the specified
     * service.
     * 
     * @param serviceKey
     *            the service key to retrieve the resellers for
     * @return a <code>Response</code> object containing a list of
     *         <code>POPartner</code> objects.
     */
    public Response getResellers(long serviceKey);

    /**
     * Retrieves the template services of the seller organization the calling
     * user belongs to.
     * <p>
     * Required roles: manager of a supplier, reseller or broker organization.
     * 
     * @return a <code>Response</code> holding a list of
     *         <code>POServiceDetails</code> objects
     */
    public Response getTemplateServices();
}
