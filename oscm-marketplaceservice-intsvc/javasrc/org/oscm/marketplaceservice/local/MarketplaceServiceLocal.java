/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 18.07.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.marketplaceservice.local;

import java.math.BigDecimal;
import java.util.List;

import org.oscm.domobjects.CatalogEntry;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.RevenueShareModel;
import org.oscm.internal.resalepermissions.POResalePermissionDetails;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.exception.AddMarketingPermissionException;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.OrganizationAuthorityException;
import org.oscm.internal.types.exception.ServiceOperationException;
import org.oscm.internal.types.exception.ServiceStateException;
import org.oscm.internal.types.exception.UserRoleAssignmentException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOCategory;
import org.oscm.types.enumtypes.EmailType;

import javax.ejb.Local;

/**
 * Contains business logic performed with and on Marketplaces, that is not
 * accessible to the public API.
 * 
 * @author barzu
 */
@Local
public interface MarketplaceServiceLocal {

    /**
     * Returns a list of the marketplaces defined on the platform. The platform
     * operator can add, change, and remove marketplace owners.
     * <p>
     * Required role: operator of the platform operator organization
     * 
     * @return the list of marketplaces
     */
    List<Marketplace> getAllMarketplaces();

    List<Marketplace> getAllAccessibleMarketplacesForOrganization(
            long organizationKey);

    /**
     * Returns the list of the marketplaces where the supplier or the partner
     * can publish to.
     * <p>
     * Required role: supplier or partner (broker or reseller)
     * 
     * @return the list of marketplaces where a supplier or a partner can
     *         publish to
     */
    List<Marketplace> getMarketplacesForSupplier();

    /**
     * Creates the broker, reseller and marketplace price models with the
     * specified revenue shares and sets them into the specified marketplace.
     * 
     * <p>
     * Required role: operator of the platform operator organization
     * 
     * @param mp
     *            The marketplace to create and set the specified proce models
     *            for
     * @param brokerRevenueShare
     *            the revenue share value for the broker
     * @param resellerRevenueShare
     *            the revenue share value for the reseller
     * @param marketplaceRevenueShare
     *            the revenue share value for the marketplace
     */
    void createRevenueModels(Marketplace mp, BigDecimal brokerRevenueShare,
            BigDecimal resellerRevenueShare,
            BigDecimal marketplaceRevenueShare);

    /**
     * Updates the owner organization of the specified marketplace to
     * <code>newOwningOrganizationId</code>. If that organization is already the
     * owner of the marketplace, than it does nothing.
     * <p>
     * Required role: marketplace manager of the marketplace owner organization
     * to change the name; operator of the platform operator organization to
     * change the marketplace owner
     * 
     * @param marketplace
     *            the marketplace to update the owner for
     * @param newOwningOrganizationId
     *            the identifier of the new owning organization
     * @param forCreate
     *            specifies if the calling operation is the marketplace creation
     * @return <code>true</code> if the owning organization was updated,
     *         <code>false</code> if there was no need to update the owning
     *         organization
     * @throws OperationNotPermittedException
     *             if the calling user does not have the required permissions
     * @throws ObjectNotFoundException
     *             if the marketplace is not found by its ID
     * @throws AddMarketingPermissionException
     *             if an organization to be authorized as a supplier, reseller,
     *             or broker cannot be retrieved or does not have the required
     *             role
     */
    boolean updateOwningOrganization(Marketplace marketplace,
            final String newOwningOrganizationId, boolean forCreate)
            throws OperationNotPermittedException, ObjectNotFoundException;

    /**
     * Removes the {@link OrganizationRoleType#MARKETPLACE_OWNER} from the
     * specified organization. It does nothing if the organization does not have
     * that role.
     * 
     * @param organizaiton
     *            the organization to remove the role for
     */
    void removeOwnerRole(Organization organizaiton);

    /**
     * Removes all user roles of type MARKETPLACE_OWNER of users belonging to
     * the specified organization.
     * 
     * @param organizationId
     *            the identifier of the organization to remove the user role for
     * @throws ObjectNotFoundException
     */
    void removeUserRoles(String organizationId) throws ObjectNotFoundException;

    /**
     * @param marketplaceId
     *            the marketplace for which the revenue share should be loaded
     * @return the marketplace's revenue share
     * @throws ObjectNotFoundException
     *             thrown when no marketplace with the given id exists
     */
    RevenueShareModel loadMarketplaceRevenueShare(String marketplaceId)
            throws ObjectNotFoundException;

    /**
     * Saves the specified name for the <code>marketplace</code> as localized
     * resource in the current locale, only if it differs from the current
     * <code>marketplace</code> name.
     * 
     * @param marketplace
     *            the marketplace to update the name for
     * @param marketplaceName
     *            the new marketplace name
     * @throws OperationNotPermittedException
     *             if the new marketplace name needs to be saved and the current
     *             user is no marketplace owner.
     */
    void updateMarketplaceName(Marketplace marketplace, String marketplaceName)
            throws OperationNotPermittedException;

    /**
     * Sends an email regarding changes on a marketplace to all administrators
     * of the specified owning organization.
     * 
     * @param type
     *            the email template
     * @param marketplace
     *            the marketplace that was changed
     * @param organizationKey
     *            the key of the organization to inform the administrators for
     */
    void sendNotification(EmailType type, Marketplace marketplace,
            long organizationKey);

    /**
     * Sends an email regarding changes on a marketplace to the specified
     * administrators of its owning organization.
     * 
     * @param type
     *            the email template
     * @param marketplace
     *            the marketplace that was changed
     * @param admins
     *            a list of the owning organization administrators to be
     *            informed
     */
    void sendNotification(EmailType type, Marketplace marketplace,
            List<PlatformUser> admins);

    /**
     * Updates a revenue share model.
     * 
     * @param revenueShareModel
     *            the modified revenue share model
     * @param version
     *            the version of the modified revenue share model
     * @return
     * @throws ObjectNotFoundException
     *             thrown if the marketplace or the revenue share model cannot
     *             be loaded
     * @throws ValidationException
     *             thrown if the revenue share is not in [0,100] percent
     * @throws ConcurrentModificationException
     *             thrown if the revenue share model was updated in the meantime
     */
    RevenueShareModel updateRevenueShare(RevenueShareModel revenueShareModel,
            int version) throws ObjectNotFoundException, ValidationException,
            ConcurrentModificationException;

    /**
     * Persists the changes to the specified marketplace. The new name of the
     * marketplace will be saved as localized resource using the current locale.
     * If the owning organization changes, then the reference is also updated.
     * 
     * @param marketplace
     *            the marketplace to be persisted
     * @param marketplaceName
     *            the new name of the marketplace name.
     * @param owningOrganizationId
     *            the identifier of the new owning organization
     * @return <code>true</code> if the owning organization changed,
     *         <code>false</code> otherwise.
     * @throws ObjectNotFoundException
     * @throws ObjectNotFoundException
     *             if the marketplace is not found by its ID
     * @throws OperationNotPermittedException
     *             if the calling user does not have the required permissions
     * @throws ValidationException
     *             if the marketplace ID in the value object is invalid
     * @throws AddMarketingPermissionException
     *             if an organization to be authorized as a supplier, reseller,
     *             or broker cannot be retrieved or does not have the required
     *             role
     * @throws UserRoleAssignmentException
     *             if a problem occurs in the user role assignment
     */
    boolean updateMarketplace(Marketplace marketplace, String marketplaceName,
            String owningOrganizationId)
            throws ObjectNotFoundException, OperationNotPermittedException,
            ValidationException, UserRoleAssignmentException;

    /**
     * Ensures that the owning organization has the right to publish on the
     * specified marketplace if that organization is also a supplier.
     * 
     * @param mp
     *            the marketplace to ensure publishing rights for
     */
    void grantPublishingRights(Marketplace mp);

    /**
     * Retrieves the marketplace domain object for the specified marketplace
     * identifier.
     * 
     * @param marketplaceId
     *            the identifier of the marketplace to be retrieved
     * @return the marketplace domain object
     * @throws ObjectNotFoundException
     *             if the marketplace is not found by its identifier
     */
    Marketplace getMarketplace(String marketplaceId)
            throws ObjectNotFoundException;

    /**
     * Updates the specified <code>marketplace</code>. It is assumed the
     * attributes of <code>marketplace</code> were already set with the new
     * values, but not persisted yet. As the marketplace name and owning
     * organization are not part of a marketplace domain object, they are passed
     * separately. The new values of the marketplace and partner revenue shares
     * are contained by the <code>newMarketplace</code>. The on-load versions of
     * the revenue shares, relevant for concurrency check, are passed
     * separately.
     * 
     * @param marketplace
     *            the marketplace domain object containing the new attribute
     *            values.
     * @param newMarketplace
     *            a detached marketplace object containing the new values of the
     *            revenue shares
     * @param marketplaceName
     *            the new marketplace name
     * @param owningOrganizationId
     *            the new owner organization of the marketplace
     * @param marketplaceRevenueShareVersion
     *            the on-load version of the marketplace revenue share
     * @param resellerRevenueShareVersion
     *            the on-load version of the reseller revenue share
     * @param brokerRevenueShareVersion
     *            the on-load version of the broker revenue share
     * @return <code>true</code> if the marketplace owning organization has
     *         changed, <code>false</code> otherwise
     * @throws ObjectNotFoundException
     *             if the marketplace or any referenced object is not found by
     *             its ID
     * @throws OperationNotPermittedException
     *             if the calling user does not have the required permissions
     * @throws ConcurrentModificationException
     *             if the data stored for the given marketplace is changed by
     *             another user in the time between reading and writing it
     * @throws ValidationException
     *             if the marketplace ID in the value object is invalid
     * @throws AddMarketingPermissionException
     *             if an organization to be authorized as a supplier, reseller,
     *             or broker cannot be retrieved or does not have the required
     *             role
     * @throws UserRoleAssignmentException
     *             if a problem occurs in the user role assignment
     */
    boolean updateMarketplace(Marketplace marketplace,
            Marketplace newMarketplace, String marketplaceName,
            String owningOrganizationId, int marketplaceRevenueShareVersion,
            int resellerRevenueShareVersion, int brokerRevenueShareVersion)
            throws ObjectNotFoundException, ValidationException,
            ConcurrentModificationException, OperationNotPermittedException,
            AddMarketingPermissionException, UserRoleAssignmentException;

    /**
     * Persists the changes to the specified marketplace. The new name of the
     * marketplace will be saved as localized resource using the current locale.
     * If the owning organization changes, then the reference is also updated.
     * 
     * @param marketplaceId
     *            the id of the marketplace.
     * @param marketplaceVersion
     *            the version of the marketplace.
     * @param trackingCode
     *            the new trackingCode of the marketplace.
     * @throws ObjectNotFoundException
     *             if the marketplace is not found by its ID
     * @throws ConcurrentModificationException
     *             if the marketplace trackingcode is changed by another user in
     *             the time between reading and writing it
     */
    void updateMarketplaceTrackingCode(String marketplaceId,
            int marketplaceVersion, String trackingCode)
            throws ObjectNotFoundException, ConcurrentModificationException;

    /**
     * Extract the trackingCode from a given MarketplaceId
     * 
     * @param marketplaceId
     *            the marketplace Id to be persisted
     * @return
     * @throws ObjectNotFoundException
     *             if the marketplace is not found by its ID
     */
    String getTrackingCodeFromMarketplace(String marketplaceId)
            throws ObjectNotFoundException;

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
     * @param serviceKey
     *            the key of the service to be published
     * @param catalogEntry
     *            the catalog entry referencing the marketplace to which the
     *            service is to be published. To remove a service from a
     *            marketplace, pass a catalog entry whose marketplace is set to
     *            <code>null</code>.
     * @param categories
     *            the categories to be associated with the specified service.
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
     */
    Product publishService(long serviceKey, CatalogEntry catalogEntry,
            List<VOCategory> categories)
            throws ObjectNotFoundException, ValidationException,
            NonUniqueBusinessKeyException, OperationNotPermittedException;

    /**
     * Method does the same as method
     * {@link #publishService(long, CatalogEntry, List)}. In addition methods
     * grants and revokes permissions given as parameters.
     *
     * @param serviceKey
     *            the serviceKey of the service to be published
     * @param catalogEntry
     *            the catalog entry referencing the marketplace to which the
     *            service is to be published. To remove a service from a
     *            marketplace, pass a catalog entry whose marketplace is set to
     *            <code>null</code>.
     * @param categories
     *            the categories to be associated with the specified service.
     * @param permissionsToGrant
     *            A list of resale permissions, which should be granted. Each
     *            resale permission contains the related service template, the
     *            organization, which grants the resale permission, the
     *            organization, which receives the resale permission, and the
     *            type of the resale permission.
     * @param permissionsToRevoke
     *            A list of resale permissions, which should be revoked. Each
     *            resale permission contains the related service template, the
     *            organization, which grants the resale permission and the
     *            organization, which receives the resale permission.
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
     */
    Product publishServiceWithPermissions(long serviceKey,
            CatalogEntry catalogEntry, List<VOCategory> categories,
            List<POResalePermissionDetails> permissionsToGrant,
            List<POResalePermissionDetails> permissionsToRevoke)
            throws ObjectNotFoundException, NonUniqueBusinessKeyException,
            OperationNotPermittedException, ValidationException,
            OrganizationAuthorityException, ConcurrentModificationException,
            ServiceStateException, ServiceOperationException;

    Marketplace getMarketplaceForId(String marketplaceId)
            throws ObjectNotFoundException;

    /**
     * Returns all existing organizations.
     *
     * @return list of organizations
     */
    List<Organization> getAllOrganizations();

    /**
     * This method is used to close or open marketplace.
     *
     * @param marketplaceId
     *            - Id of marketplace of which access type has to be changed
     * @param isRestricted
     *            - true - close marketplace, false - open marketplace
     * @return - changed marketplace object
     * @throws ObjectNotFoundException
     * @throws NonUniqueBusinessKeyException
     */
    Marketplace updateMarketplaceAccessType(String marketplaceId,
            boolean isRestricted)
            throws ObjectNotFoundException, NonUniqueBusinessKeyException;

    /**
     * This method is used to grant access to given marketplace to given
     * organization.
     *
     * @param marketplace
     * @param organization
     * @throws NonUniqueBusinessKeyException
     */
    void grantAccessToMarketPlaceToOrganization(Marketplace marketplace,
            Organization organization) throws NonUniqueBusinessKeyException;

    /**
     * Removes all existing accesses to given marketplace.
     *
     * @param marketplaceKey
     */
    void removeMarketplaceAccesses(long marketplaceKey);

    /**
     * Remove access to marketplace for given organization.
     *
     * @param marketplaceKey
     * @param organizationKey
     * @throws ObjectNotFoundException
     */
    void removeMarketplaceAccess(long marketplaceKey, long organizationKey)
            throws ObjectNotFoundException;

    /**
     * Retrieves all marketplaces with restricted access which are accessible
     * for given organization.
     * 
     * @param orgKey
     *            key of the Organization
     * @return list of marketplaces
     */
    List<Marketplace> getMarketplacesForOrganizationWithRestrictedAccess(
            long orgKey);

    boolean doesAccessToMarketplaceExistForOrganization(long marketplaceKey,
            long organizationKey);

    /**
     * Retrieves organizations information in the context of managing access to
     * restricted marketplaces
     * 
     * @param marketplaceId
     *            id of the selected marketplace
     * @return list of objects containing: organization's key, organization's
     *         id, organization's name, restriction info (boolean), number of
     *         owned subscriptions on given marketplace
     * @throws ObjectNotFoundException
     */
    List<Object[]> getOrganizationsWithMarketplaceAccess(String marketplaceId)
            throws ObjectNotFoundException;

    /**
     * Retrieves all organizations with access to the marketplace with the given
     * key if the marketplace is restricted.
     * 
     * @param marketplaceKey
     *            the key of the marketplace
     * @return list of organizations or empty list if not restricted
     */
    List<Organization> getAllOrganizationsWithAccessToMarketplace(
            long marketplaceKey) throws ObjectNotFoundException;

    /**
     * Updates the tenant for marketplace.
     * 
     * @param marketplace
     *            the marketplace to update the tenant for
     * @param tenantId
     *            the identifier of the related tenant
     * @throws ObjectNotFoundException
     */
    void updateTenant(Marketplace marketplace, final String tenantId)
            throws ObjectNotFoundException;

    /**
     * Retrives all marketplaces assigned to the given tenant
     *
     * @param tenantKey
     *            - tenant technical key
     * @return the list of marketplaces or empty list if no marketplace can be
     *         found
     * @throws ObjectNotFoundException
     */
    List<Marketplace> getAllMarketplacesForTenant(Long tenantKey)
            throws ObjectNotFoundException;
}
