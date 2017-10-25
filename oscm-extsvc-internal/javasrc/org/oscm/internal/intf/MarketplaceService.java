/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 2009-05-02                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.intf;

import java.util.List;
import java.util.Set;

import javax.ejb.Remote;
import javax.security.auth.login.LoginException;

import org.oscm.internal.cache.MarketplaceConfiguration;
import org.oscm.internal.types.exception.*;
import org.oscm.internal.vo.*;

/**
 * Remote interface of the marketplace management service.
 * 
 */
@Remote
public interface MarketplaceService {

    /**
     * Retrieves all marketplaces to which the calling user's organization can
     * publish services.
     * <p>
     * Required role: service manager of a supplier organization, reseller of a
     * reseller organization, or broker of a broker organization
     * 
     * @return the list of marketplaces
     */

    List<VOMarketplace> getMarketplacesForOrganization();

    /**
     * Retrieves the catalog entries from all marketplaces to which the
     * specified service has been published.
     * <p>
     * Required role: service manager of the supplier organization that owns the
     * service, or broker or reseller of an authorized broker or reseller
     * organization
     * 
     * @param service
     *            the service for which the catalog entries are to be retrieved
     * @return the catalog entries of the marketplaces the given service has
     *         been published to
     * @throws ObjectNotFoundException
     *             if the given service is not found by its key
     * @throws OperationNotPermittedException
     *             if the calling user's organization is neither the supplier of
     *             the service nor an authorized broker or reseller
     */

    List<VOCatalogEntry> getMarketplacesForService(VOService service)
            throws ObjectNotFoundException, OperationNotPermittedException;

    /**
     * Publishes a service to a marketplace or removes a service from a
     * marketplace. The marketplace to which the service is to be published is
     * specified by the given catalog entries. To remove a service from a
     * marketplace, pass a catalog entry whose marketplace is set to
     * <code>null</code>.
     * <p>
     * <b>Note:</b> At the moment, a service can be published only to a single
     * marketplace or not be published at all. A new publication overrides a
     * previous publication to a different marketplace.
     * <p>
     * Categories specified with a catalog entry are assigned to the service on
     * the marketplace. If the service is published to a different marketplace
     * or removed from a marketplace, the existing category assignments are
     * deleted.
     * <p>
     * Required role: service manager of the supplier organization that owns the
     * service, or broker or reseller of an authorized broker or reseller
     * organization
     * 
     * @param service
     *            the service to be published
     * @param entries
     *            the catalog entries referencing the marketplace to which the
     *            service is to be published. To remove a service from a
     *            marketplace, pass a catalog entry whose marketplace is set to
     *            <code>null</code>.
     * @return a value object with the updated service definition
     * @throws ObjectNotFoundException
     *             if the service or marketplace is not found
     * @throws ValidationException
     *             if one of the input parameters does not meet the constraints
     * @throws NonUniqueBusinessKeyException
     *             if there is already a catalog entry with the given identifier
     * @throws OperationNotPermittedException
     *             if the calling user's organization is neither the supplier of
     *             the service nor an authorized broker or reseller
     * @throws PublishingToMarketplaceNotPermittedException
     *             if the calling user's organization is not allowed to publish
     *             services on the given marketplace
     */

    VOServiceDetails publishService(VOService service,
            List<VOCatalogEntry> entries)
            throws ObjectNotFoundException, ValidationException,
            NonUniqueBusinessKeyException, OperationNotPermittedException;

    /**
     * Retrieves the marketplace for the specified subscription. If no
     * marketplace is associated with the subscription, the method returns
     * <code>null</code>.
     * <p>
     * Required role: any user role in an organization
     * 
     * @param subscriptionKey
     *            the numeric key of the subscription
     * @param locale
     *            the language to be used for the marketplace. Specify a
     *            language code as returned by <code>getLanguage()</code> of
     *            <code>java.util.Locale</code>, or <code>null</code> to use the
     *            language of the current user session.
     * @return the marketplace where the subscription was created, or
     *         <code>null</code> if the subscription is not associated with a
     *         marketplace
     * @throws ObjectNotFoundException
     *             if the subscription is not found
     */

    VOMarketplace getMarketplaceForSubscription(long subscriptionKey,
            String locale) throws ObjectNotFoundException;

    /**
     * Returns the marketplaces owned by the calling user's organization. If the
     * organization is not the owner of any marketplace, the list is empty.
     * <p>
     * Required role: marketplace manager of a marketplace owner organization or
     * operator of the platform operator organization
     * 
     * @return the list of marketplaces
     */

    List<VOMarketplace> getMarketplacesOwned();

    /**
     * Returns a list of the marketplaces defined on the platform. The platform
     * operator can add, change, and remove marketplace owners.
     * <p>
     * Required role: operator of the platform operator organization
     * 
     * @return the list of marketplaces
     */

    List<VOMarketplace> getMarketplacesForOperator();

    /**
     * Returns a list of the marketplaces that are accessible for the
     * organization of the calling user.
     * 
     * @return the list of marketplaces that are accessible for the organization
     *         of the calling user
     */
    public List<VOMarketplace> getAccessibleMarketplaces();

    /**
     * Modifies the name and/or owner of the given marketplace.
     * <p>
     * To set a new marketplace owner, specify the ID of the organization in the
     * <code>VOMarketplace</code> object. The organization is automatically
     * assigned the marketplace owner role. Its administrators are notified by
     * email and assigned the marketplace manager role. They can assign the
     * marketplace manager role to additional users as required.
     * <p>
     * Required role: marketplace manager of the marketplace owner organization
     * to change the name; operator of the platform operator organization to
     * change the marketplace owner
     * 
     * @param marketplace
     *            the value object specifying the marketplace and the data to be
     *            stored
     * @return a value object with the updated marketplace definition
     * @throws ObjectNotFoundException
     *             if the marketplace is not found by its ID
     * @throws OperationNotPermittedException
     *             if the calling user does not have the required permissions
     * @throws ConcurrentModificationException
     *             if the data stored for the given marketplace is changed by
     *             another user in the time between reading and writing it
     * @throws ValidationException
     *             if the marketplace ID in the value object is invalid
     * @throws UserRoleAssignmentException
     *             if a problem occurs in the user role assignment
     */

    VOMarketplace updateMarketplace(VOMarketplace marketplace)
            throws ObjectNotFoundException, OperationNotPermittedException,
            ConcurrentModificationException, ValidationException,
            UserRoleAssignmentException;

    /**
     * Creates a marketplace. The platform allows for creating any number of
     * marketplaces. A specific organization can be the owner of several
     * marketplaces.
     * <p>
     * The marketplace name in the <code>VOMarketplace</code> object is
     * mandatory and expected in English. It does not need to be unique.
     * <p>
     * If no marketplace owner organization is specified in the
     * <code>VOMarketplace</code> object, the calling user's organization is set
     * as the owner. The relevant organization is automatically assigned the
     * marketplace owner role. Its administrators are notified by email and
     * assigned the marketplace manager role. They can assign the marketplace
     * manager role to additional users as required.
     * <p>
     * A marketplace ID specified in the <code>VOMarketplace</code> object is
     * ignored. The method itself generates a unique ID for the new marketplace.
     * <p>
     * Required role: operator of the platform operator organization
     * 
     * @param marketplace
     *            the value object specifying the marketplace and the data to be
     *            stored
     * @return a value object with the stored marketplace definition
     * 
     * @throws OperationNotPermittedException
     *             if the calling user does not have the required permissions
     * @throws ObjectNotFoundException
     *             if the organization specified as the marketplace owner is not
     *             found
     * @throws ValidationException
     *             if no name is specified for the marketplace
     * @throws UserRoleAssignmentException
     *             if a problem occurs in the user role assignment
     * @throws MarketplaceValidationException
     *             if marketplace ID is invalid or already exists
     */

    VOMarketplace createMarketplace(VOMarketplace marketplace)
            throws OperationNotPermittedException, ObjectNotFoundException,
            ValidationException, UserRoleAssignmentException,
            MarketplaceValidationException;

    /**
     * Deletes a marketplace.
     * <p>
     * The deletion is rejected if active services are published on the
     * marketplace. When the last marketplace of the owning organization is
     * deleted, the marketplace owner role is automatically removed from the
     * organization, and the marketplace manager role is removed from the
     * organization's users.
     * <p>
     * Required role: operator of the platform operator organization
     * 
     * @param marketplaceId
     *            the ID of the marketplace
     * @throws ObjectNotFoundException
     *             if the marketplace is not found by its ID
     */

    void deleteMarketplace(String marketplaceId) throws ObjectNotFoundException;

    /**
     * Adds one or more organizations to the list of organizations that are
     * allowed to publish services on the given marketplace.
     * <p>
     * This method is required and executable only for marketplaces which are
     * not open to any supplier, broker, and reseller.
     * <p>
     * Required role: marketplace manager of the marketplace owner organization
     * 
     * @param organizationIds
     *            the IDs of the supplier, broker, and reseller organizations to
     *            be added to the marketplace
     * @param marketplaceId
     *            the ID of the marketplace
     * @throws ObjectNotFoundException
     *             if the marketplace or an organization is not found
     * @throws OperationNotPermittedException
     *             if the calling user does not have the required permissions
     * @throws OrganizationAuthorityException
     *             if the specified organizations do not have the supplier,
     *             broker, or reseller role
     * @throws OrganizationAlreadyExistsException
     *             if at least one of the specified organizations was already
     *             admitted to the marketplace
     * @throws MarketplaceAccessTypeUneligibleForOperationException
     *             if the marketplace is open to any supplier, broker, and
     *             reseller
     */

    void addOrganizationsToMarketplace(List<String> organizationIds,
            String marketplaceId)
            throws ObjectNotFoundException, OperationNotPermittedException,
            OrganizationAuthorityException, OrganizationAlreadyExistsException,
            MarketplaceAccessTypeUneligibleForOperationException;

    /**
     * Banishes one or more organizations from the given marketplace. This means
     * that the organizations are added to a black list, which prevents them
     * from publishing services to the marketplace.
     * <p>
     * This method is executable only for marketplaces which are open to any
     * supplier, broker, and reseller.
     * <p>
     * Required role: marketplace manager of the marketplace owner organization
     * 
     * @param organizationIds
     *            the IDs of the supplier, broker, and reseller organizations to
     *            be banned from the marketplace
     * @param marketplaceId
     *            the ID of the marketplace
     * @throws ObjectNotFoundException
     *             if the marketplace or an organization is not found
     * @throws OperationNotPermittedException
     *             if the calling user does not have the required permissions
     * @throws OrganizationAuthorityException
     *             if the specified organizations do not have the supplier,
     *             broker, or reseller role
     * @throws OrganizationAlreadyBannedException
     *             if at least one of the specified organizations is already
     *             banned from this marketplace
     * @throws MarketplaceAccessTypeUneligibleForOperationException
     *             if the marketplace is not open to any supplier, broker, and
     *             reseller
     */

    void banOrganizationsFromMarketplace(List<String> organizationIds,
            String marketplaceId)
            throws ObjectNotFoundException, OperationNotPermittedException,
            OrganizationAuthorityException, OrganizationAlreadyBannedException,
            MarketplaceAccessTypeUneligibleForOperationException;

    /**
     * Removes one or more organizations from the list of organizations that are
     * allowed to publish services on the given marketplace.
     * <p>
     * This method is required and executable only for marketplaces which are
     * not open to any supplier, broker, and reseller.
     * <p>
     * Required role: marketplace manager of the marketplace owner organization
     * 
     * @param organizationIds
     *            the IDs of the supplier, broker, and reseller organizations to
     *            be removed from the marketplace
     * @param marketplaceId
     *            the ID of the marketplace
     * @throws ObjectNotFoundException
     *             if the marketplace or an organization is not found
     * @throws OperationNotPermittedException
     *             if the calling user does not have the required permissions
     * @throws MarketplaceAccessTypeUneligibleForOperationException
     *             if the marketplace is not open to any supplier, broker, and
     *             reseller
     * @throws OrganizationAuthorityException
     *             if the specified organizations do not have the supplier,
     *             broker, or reseller role
     */

    void removeOrganizationsFromMarketplace(List<String> organizationIds,
            String marketplaceId)
            throws ObjectNotFoundException, OperationNotPermittedException,
            MarketplaceAccessTypeUneligibleForOperationException,
            OrganizationAuthorityException;

    /**
     * Removes one or more organizations from the black list for the given
     * marketplace. This means that the organizations are no longer banned from
     * the marketplace and can continue to publish services to it.
     * <p>
     * This method is executable only for marketplaces which are open to any
     * supplier, broker, and reseller.
     * <p>
     * Required role: marketplace manager of the marketplace owner organization
     * 
     * @param organizationIds
     *            the IDs of the supplier, broker, and reseller organizations to
     *            be removed from the black list for the marketplace
     * @param marketplaceId
     *            the ID of the marketplace
     * @throws ObjectNotFoundException
     *             if the marketplace or an organization is not found
     * @throws OperationNotPermittedException
     *             if the calling user does not have the required permissions
     * @throws MarketplaceAccessTypeUneligibleForOperationException
     *             if the marketplace is not open to any supplier, broker, and
     *             reseller
     * @throws OrganizationAuthorityException
     *             if the specified organizations do not have the supplier,
     *             broker, or reseller role
     */

    void liftBanOrganizationsFromMarketplace(List<String> organizationIds,
            String marketplaceId)
            throws ObjectNotFoundException, OperationNotPermittedException,
            MarketplaceAccessTypeUneligibleForOperationException,
            OrganizationAuthorityException;

    /**
     * Returns a list of all organizations which have been banned from the given
     * marketplace.
     * <p>
     * This method is executable only for marketplaces which are open to any
     * supplier, broker, and reseller.
     * <p>
     * Required role: marketplace manager of the marketplace owner organization
     * 
     * @param marketplaceId
     *            the ID of the marketplace
     * @return the list of supplier, broker, and reseller organizations
     * @throws ObjectNotFoundException
     *             if the marketplace is not found
     * @throws OperationNotPermittedException
     *             if the calling user does not have the required permissions
     * @throws MarketplaceAccessTypeUneligibleForOperationException
     *             if the marketplace is not open to any supplier, broker, and
     *             reseller
     */

    List<VOOrganization> getBannedOrganizationsForMarketplace(
            String marketplaceId)
            throws ObjectNotFoundException, OperationNotPermittedException,
            MarketplaceAccessTypeUneligibleForOperationException;

    /**
     * Returns a list of all organizations that are allowed to publish their
     * services on the given marketplace.
     * <p>
     * This method is required and executable only for marketplaces which are
     * not open to any supplier, broker, and reseller.
     * <p>
     * Required role: marketplace manager of the marketplace owner organization
     * 
     * @param marketplaceId
     *            the ID of the marketplace
     * @return the list of supplier, broker, and reseller organizations
     * @throws ObjectNotFoundException
     *             if the marketplace is not found
     * @throws OperationNotPermittedException
     *             if the calling user does not have the required permissions
     * @throws MarketplaceAccessTypeUneligibleForOperationException
     *             if the marketplace is open to any supplier, broker, and
     *             reseller
     */

    List<VOOrganization> getOrganizationsForMarketplace(String marketplaceId)
            throws ObjectNotFoundException, OperationNotPermittedException,
            MarketplaceAccessTypeUneligibleForOperationException;

    /**
     * Returns the definition of the marketplace with the specified ID. The
     * method implicitly checks whether the given marketplace ID is valid, i.e.
     * whether a marketplace with the given ID exists.
     * <p>
     * Required role: none
     * 
     * @param marketplaceId
     *            the ID of the marketplace
     * @throws ObjectNotFoundException
     *             if the marketplace is not found
     * 
     * @return a value object with the marketplace definition
     */

    VOMarketplace getMarketplaceById(String marketplaceId)
            throws ObjectNotFoundException;

    /**
     * Returns the URL of the style sheet (CSS) that defines the branding for
     * the specified marketplace.
     * <p>
     * Required role: none
     * 
     * @param marketplaceId
     *            the ID of the marketplace; must not be <code>null</code> or
     *            consist of blanks only
     * @return the URL of the branding style sheet, or <code>null</code> if the
     *         default white-label branding is used
     * @throws ObjectNotFoundException
     *             if the marketplace is not found
     */

    String getBrandingUrl(String marketplaceId) throws ObjectNotFoundException;

    /**
     * Stores the URL of the style sheet (CSS) that defines the branding for the
     * specified marketplace.
     * <p>
     * Required role: marketplace manager of the marketplace owner organization
     * 
     * @param marketplace
     *            the value object identifying the marketplace to save the URL
     *            for; must not be <code>null</code>
     * @param brandingUrl
     *            the URL of the branding style sheet
     * @throws ObjectNotFoundException
     *             if the marketplace is not found by its ID
     * @throws ValidationException
     *             if the specified URL is not a valid URL
     * @throws OperationNotPermittedException
     *             if the calling user does not have the required permissions
     * @throws ConcurrentModificationException
     *             if the data stored for the given marketplace is changed by
     *             another user in the time between reading and writing it
     */

    void saveBrandingUrl(VOMarketplace marketplace, String brandingUrl)
            throws ObjectNotFoundException, ValidationException,
            OperationNotPermittedException, ConcurrentModificationException;

    /**
     * Returns all organizations created in the system with information about
     * access to the given marketplace.
     *
     * @return collection of all organizations.
     * @throws ObjectNotFoundException
     */
    List<VOOrganization> getAllOrganizations(String marketplaceId)
            throws ObjectNotFoundException;

    /**
     * Method is used to restrict access to the given marketplace.
     *
     * @param marketplaceId
     * @param authorizedOrganizations
     *            - organizations to which access to marketplace should be
     *            granted
     * @param unauthorizedOrganizations
     *            - organizations which should not have access to marketplace
     *            any more
     * @throws OperationNotPermittedException
     * @throws ObjectNotFoundException
     * @throws NonUniqueBusinessKeyException
     * @throws TechnicalServiceOperationException
     * @throws TechnicalServiceNotAliveException
     */
    void closeMarketplace(String marketplaceId,
            Set<Long> authorizedOrganizations,
            Set<Long> unauthorizedOrganizations)
            throws OperationNotPermittedException, ObjectNotFoundException,
            NonUniqueBusinessKeyException, TechnicalServiceNotAliveException,
            TechnicalServiceOperationException;

    /**
     * This method is used to grant access to given marketplace to given
     * organization.
     *
     * @param voMarketplace
     * @param voOrganization
     * @throws ValidationException
     * @throws NonUniqueBusinessKeyException
     */
    void grantAccessToMarketPlaceToOrganization(VOMarketplace voMarketplace,
            VOOrganization voOrganization)
            throws ValidationException, NonUniqueBusinessKeyException;

    /**
     * Method is used to remove restrictions to the given marketplace.
     *
     * @param marketplaceId
     * @throws OperationNotPermittedException
     * @throws ObjectNotFoundException
     * @throws NonUniqueBusinessKeyException
     */
    void openMarketplace(String marketplaceId)
            throws OperationNotPermittedException, ObjectNotFoundException,
            NonUniqueBusinessKeyException;

    /**
     * Retrieves all restricted marketplaces to which the calling user's
     * organization have an access.
     * 
     * @return the list of marketplaces
     */
    List<VOMarketplace> getRestrictedMarketplaces();

    /**
     * Gives information if given organization has access to marketplace.
     *
     * @param marketplaceId
     * @param organizationId
     * @return true - if organization has access to marketplace, false -
     *         otherwise
     * @throws ObjectNotFoundException
     */
    boolean doesOrganizationHaveAccessMarketplace(String marketplaceId,
            String organizationId) throws LoginException;

    /**
     * Returns all organizations which have access to the marketplace with the
     * given id if the marketplace is restricted.
     * 
     * @param marketplaceId
     *            the marketplace id
     * @return the list of organizations or empty list if not restricted
     */
    List<VOOrganization> getAllOrganizationsWithAccessToMarketplace(
            String marketplaceId);

    /**
     * Gets the cached version of the marketplace with allowed organizations (if
     * it is restricted) for the marketplace with the given id.
     * 
     * @param marketplaceId
     *            the marketplace id
     * @return the configuration or null if marketplace id is invalid
     */
    MarketplaceConfiguration getCachedMarketplaceConfiguration(
            String marketplaceId);

    /**
     * Clears the cache from the configuration of the marketplace with the given
     * id.
     * 
     * @param marketplaceId
     *            the marketplace id
     */
    void clearCachedMarketplaceConfiguration(String marketplaceId);

    /**
     *
     * @param tenantKey
     *            - tanant technical key
     * @return list of marketplaces assigned to the given tenant
     * @throws ObjectNotFoundException
     */
    List<VOMarketplace> getAllMarketplacesForTenant(Long tenantKey)
            throws ObjectNotFoundException;

    String getTenantIdFromMarketplace(String marketplaceId)
            throws ObjectNotFoundException;

    /**
     * Gets all organizations with the supplier role that can publish on the
     * given marketplace.
     * 
     * @param marketplaceId
     *            the id of the marketplace
     * @return list of suppliers
     * @throws ObjectNotFoundException
     * @throws OperationNotPermittedException
     */
    public List<VOOrganization> getSuppliersForMarketplace(String marketplaceId)
            throws ObjectNotFoundException, OperationNotPermittedException;

    String getMarketplaceIdForKey(Long key) throws ObjectNotFoundException;
}
