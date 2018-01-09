/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 2009-05-02                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.intf;

import java.util.List;

import javax.ejb.Remote;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import org.oscm.types.exceptions.ConcurrentModificationException;
import org.oscm.types.exceptions.MarketplaceAccessTypeUneligibleForOperationException;
import org.oscm.types.exceptions.MarketplaceValidationException;
import org.oscm.types.exceptions.NonUniqueBusinessKeyException;
import org.oscm.types.exceptions.ObjectNotFoundException;
import org.oscm.types.exceptions.OperationNotPermittedException;
import org.oscm.types.exceptions.OrganizationAlreadyBannedException;
import org.oscm.types.exceptions.OrganizationAlreadyExistsException;
import org.oscm.types.exceptions.OrganizationAuthorityException;
import org.oscm.types.exceptions.PublishingToMarketplaceNotPermittedException;
import org.oscm.types.exceptions.UserRoleAssignmentException;
import org.oscm.types.exceptions.ValidationException;
import org.oscm.vo.VOCatalogEntry;
import org.oscm.vo.VOMarketplace;
import org.oscm.vo.VOOrganization;
import org.oscm.vo.VOService;
import org.oscm.vo.VOServiceDetails;

/**
 * Remote interface of the marketplace management service.
 * 
 */
@Remote
@WebService(targetNamespace = "http://oscm.org/xsd")
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
    @WebMethod
    public List<VOMarketplace> getMarketplacesForOrganization();

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
    @WebMethod
    public List<VOCatalogEntry> getMarketplacesForService(
            @WebParam(name = "service") VOService service)
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
    @WebMethod
    public VOServiceDetails publishService(
            @WebParam(name = "service") VOService service,
            @WebParam(name = "entries") List<VOCatalogEntry> entries)
            throws ObjectNotFoundException, ValidationException,
            NonUniqueBusinessKeyException, OperationNotPermittedException,
            PublishingToMarketplaceNotPermittedException;

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
    @WebMethod
    public VOMarketplace getMarketplaceForSubscription(
            @WebParam(name = "subscriptionKey") long subscriptionKey,
            @WebParam(name = "locale") String locale)
            throws ObjectNotFoundException;

    /**
     * Returns the marketplaces owned by the calling user's organization. If the
     * organization is not the owner of any marketplace, the list is empty.
     * <p>
     * Required role: marketplace manager of a marketplace owner organization or
     * operator of the platform operator organization
     * 
     * @return the list of marketplaces
     */
    @WebMethod
    public List<VOMarketplace> getMarketplacesOwned();

    /**
     * Returns a list of the marketplaces defined on the platform. The platform
     * operator can add, change, and remove marketplace owners.
     * <p>
     * Required role: operator of the platform operator organization
     * 
     * @return the list of marketplaces
     */
    @WebMethod
    public List<VOMarketplace> getMarketplacesForOperator();

    /**
     * Returns a list of all marketplaces that can be accessed by the
     * organization of the calling user.
     * 
     * @return a list of all marketplaces that can be accessed by the
     *         organization of the calling user
     */
    @WebMethod
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
    @WebMethod
    public VOMarketplace updateMarketplace(
            @WebParam(name = "marketplace") VOMarketplace marketplace)
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
    @WebMethod
    public VOMarketplace createMarketplace(
            @WebParam(name = "marketplace") VOMarketplace marketplace)
            throws OperationNotPermittedException, ObjectNotFoundException,
            ValidationException, UserRoleAssignmentException, MarketplaceValidationException;

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
    @WebMethod
    public void deleteMarketplace(
            @WebParam(name = "marketplaceId") String marketplaceId)
            throws ObjectNotFoundException;

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
    @WebMethod
    public void addOrganizationsToMarketplace(
            @WebParam(name = "organizationIds") List<String> organizationIds,
            @WebParam(name = "marketplaceId") String marketplaceId)
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
    @WebMethod
    public void banOrganizationsFromMarketplace(
            @WebParam(name = "organizationIds") List<String> organizationIds,
            @WebParam(name = "marketplaceId") String marketplaceId)
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
    @WebMethod
    public void removeOrganizationsFromMarketplace(
            @WebParam(name = "organizationIds") List<String> organizationIds,
            @WebParam(name = "marketplaceId") String marketplaceId)
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
    @WebMethod
    public void liftBanOrganizationsFromMarketplace(
            @WebParam(name = "organizationIds") List<String> organizationIds,
            @WebParam(name = "marketplaceId") String marketplaceId)
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
    @WebMethod
    public List<VOOrganization> getBannedOrganizationsForMarketplace(
            @WebParam(name = "marketplaceId") String marketplaceId)
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
    @WebMethod
    public List<VOOrganization> getOrganizationsForMarketplace(
            @WebParam(name = "marketplaceId") String marketplaceId)
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
    @WebMethod
    public VOMarketplace getMarketplaceById(
            @WebParam(name = "marketplaceId") String marketplaceId)
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
    @WebMethod
    public String getBrandingUrl(
            @WebParam(name = "marketplaceId") String marketplaceId)
            throws ObjectNotFoundException;

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
    @WebMethod
    public void saveBrandingUrl(
            @WebParam(name = "marketplace") VOMarketplace marketplace,
            @WebParam(name = "brandingUrl") String brandingUrl)
            throws ObjectNotFoundException, ValidationException,
            OperationNotPermittedException, ConcurrentModificationException;

}
