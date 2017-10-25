/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 12.06.2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.accountservice.local;

import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.ejb.EJBException;
import javax.ejb.Local;
import javax.ejb.TransactionAttributeType;

import org.oscm.domobjects.Discount;
import org.oscm.domobjects.ImageResource;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationReference;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.TriggerProcess;
import org.oscm.internal.intf.AccountService;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.exception.AddMarketingPermissionException;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.DeletionConstraintException;
import org.oscm.internal.types.exception.DistinguishedNameException;
import org.oscm.internal.types.exception.ImageException;
import org.oscm.internal.types.exception.IncompatibleRolesException;
import org.oscm.internal.types.exception.MailOperationException;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.OrganizationAuthoritiesException;
import org.oscm.internal.types.exception.OrganizationAuthorityException;
import org.oscm.internal.types.exception.TechnicalServiceNotAliveException;
import org.oscm.internal.types.exception.TechnicalServiceOperationException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOImageResource;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.internal.vo.VOUserDetails;

/**
 * The local interface for the account service.
 * 
 * @author Mike J&auml;ger
 * 
 */
@Local
public interface AccountServiceLocal {

    /**
     * Scans for the existence of administrative users that have never confirmed
     * their account. If objects of that kind are found, the entire organization
     * object will be removed.
     * 
     * @param currentTime
     *            The time when the corresponding timer expired, used to
     *            determine the target objects.
     * @return <code>true</code> in case the invocation of the business logic
     *         passed without any problem, <code>false</code> otherwise.
     */
    public boolean removeOverdueOrganizations(long currentTime);

    /**
     * Removes the given organization by still considering the business
     * constraints. The only difference is that the method will be executed
     * within a new transaction, the used transaction attribute is
     * {@link TransactionAttributeType#REQUIRES_NEW}.
     * 
     * @param organization
     *            The organization to be deleted.
     * @throws DeletionConstraintException
     *             Thrown in case the specified organization cannot be deleted.
     * @throws ObjectNotFoundException
     *             Thrown in case the given organization object cannot be found.
     * @throws TechnicalServiceNotAliveException
     *             if the underlying technical service cannot be reached
     * @throws TechnicalServiceOperationException
     *             if a technical operation related to the subscription process
     *             fails
     */
    public void removeOverdueOrganization(Organization organization)
            throws DeletionConstraintException, ObjectNotFoundException,
            TechnicalServiceNotAliveException,
            TechnicalServiceOperationException;

    /**
     * A yet unknown Internet user registers as a new organization at the
     * platform.
     * 
     * <p>
     * By registering at the platform, the contractual relationship between the
     * organization and the platform operator is established.
     * </p>
     * 
     * <p>
     * In addition to the organization the specified user will be created as
     * well.
     * </p>
     * 
     * If the method is executed successfully a new organization is created. In
     * case the roles to be set are
     * {@link OrganizationRoleType#TECHNOLOGY_PROVIDER} as well as
     * {@link OrganizationRoleType#SUPPLIER}, the organization will also be
     * marked as supplier and technology provider for itself.
     * 
     * @param organization
     * @param imageResource
     *            the logo image of the organization, <code>null</code> if none
     *            available.
     * @param user
     *            The user that will be created as initial user for the
     *            organization.
     * @param organizationProperties
     *            Additional organization properties (e.g. remote LDAP
     *            properties) which will be stored in the OrganizationSetting
     *            table.
     * @param domicileCountry
     *            The country code in ISO 3166.
     * @param marketplaceId
     *            The context marketplace id
     * @param description
     *            the description in the current user locale
     * @param roles
     *            The roles that the new organization will be granted, possible
     *            values are {@link OrganizationRoleType#TECHNOLOGY_PROVIDER}
     *            and {@link OrganizationRoleType#SUPPLIER}.
     * @return The created organization.
     * @throws NonUniqueBusinessKeyException
     * @throws ValidationException
     * @throws MailOperationException
     *             Thrown if a password mail couldn't be sent.
     * @throws ObjectNotFoundException
     * @throws IncompatibleRolesException
     *             Thrown in case the roles that the new organization is to be
     *             granted are not compatible with each other.
     * @throws OrganizationAuthorityException
     *             Thrown in case the organization to be created is assigned an
     *             unauthorized role.
     */
    public Organization registerOrganization(Organization organization,
            ImageResource imageResource, VOUserDetails user,
            Properties organizationProperties, String domicileCountry,
            String marketplaceId, String description,
            OrganizationRoleType... roles)
            throws NonUniqueBusinessKeyException, ValidationException,
            MailOperationException, ObjectNotFoundException,
            IncompatibleRolesException, OrganizationAuthorityException;

    /**
     * Determines the organization belonging to the given organization
     * identifier and grants it the specified role, if it does not already have
     * it.
     * 
     * <p>
     * If the organization will be marked as
     * {@link OrganizationRoleType#TECHNOLOGY_PROVIDER} as well
     * {@link OrganizationRoleType#SUPPLIER} afterwards, it will also be
     * registered as supplier and technology provider of itself.
     * </p>
     * 
     * @param organizationId
     *            The identifier of the organization.
     * @param role
     *            The role to be granted.
     * @return The updated organization.
     * @throws ObjectNotFoundException
     *             Thrown in case an organization with the given identifier does
     *             not exist.
     * @throws AddMarketingPermissionException
     *             If the supplier could not be loaded or the organization does
     *             not have the OrganizationRoleType.SUPPLIER role. This should
     *             not happen here.
     * @throws IncompatibleRolesException
     *             Thrown in case the specified role is incompatible with the
     *             existing roles of the organization.
     * @throws NonUniqueBusinessKeyException
     */
    public Organization addOrganizationToRole(String organizationId,
            OrganizationRoleType role) throws ObjectNotFoundException,
            AddMarketingPermissionException, IncompatibleRolesException;

    /**
     * Getting list of organization to sending info mail about ending discount
     * in one week (seven days).
     * 
     * @param currentTimeMillis
     *            Current millisecond.
     * @return Organization list for sending notification.
     */
    public List<OrganizationReference> getOrganizationForDiscountEndNotificiation(
            long currentTimeMillis);

    /**
     * Sending notification mail about ending discount period.
     * 
     * @param currentTimeMillis
     *            Current time of mail sending in milliseconds.
     * @return true if sending mail was successful.
     * @throws MailOperationException
     *             On error mail sending.
     */
    public boolean sendDiscountEndNotificationMail(long currentTimeMillis)
            throws MailOperationException;

    /**
     * Checks if the distinguished name set on the provided organization is
     * already used by a different organization. If not the organization is
     * saved with the new value.
     * 
     * @param organization
     *            the organization with the new distinguished name.
     * @throws DistinguishedNameException
     *             in case the distinguished name is already used by a different
     *             organization.
     */
    public void checkDistinguishedName(Organization organization)
            throws DistinguishedNameException;

    /**
     * Performs the concrete operations to create the customer for the current
     * supplier, after a required confirmation of a notification listener has
     * been retrieved. If none is required, it will be executed synchronously
     * with the call to
     * {@link #registerCustomerForSupplier(VOOrganization, VOUserDetails, Properties)}
     * .
     * 
     * @param tp
     *            The trigger process containing the detail information for the
     *            handling of the request.
     * 
     * @return The created organization.
     * @throws OrganizationAuthoritiesException
     *             Thrown if the calling user does not belong to an organization
     *             having the role {@link OrganizationRoleType#SUPPLIER}.
     * @throws ValidationException
     *             Thrown if entered values (e. g. user ID) do not match the
     *             validation constraints
     * @throws NonUniqueBusinessKeyException
     *             Thrown if the generation of a unique organization ID fails
     * @throws MailOperationException
     *             Thrown if a password mail could not be sent.
     * @throws ObjectNotFoundException
     *             Thrown if domain object is not found
     * 
     */
    public VOOrganization registerKnownCustomerInt(TriggerProcess tp)
            throws OrganizationAuthoritiesException, ValidationException,
            NonUniqueBusinessKeyException, MailOperationException,
            ObjectNotFoundException;

    /**
     * Performs the concrete operations to save the customer or service payment
     * configurations for the current supplier, after a required confirmation of
     * a notification listener has been retrieved. If none is required, it will
     * be executed synchronously with the call to
     * {@link #savePaymentConfiguration(Set, List, Set, List)}
     * 
     * @param tp
     *            The trigger process containing the details on the action to be
     *            performed.
     * @throws ObjectNotFoundException
     *             Thrown in case the domain object for role supplier or the
     *             customer's organization or the service cannot be found.
     * @throws OperationNotPermittedException
     *             Thrown in case the the service is not owned by the calling
     *             supplier, is not template or the payment type is not enabled
     *             for the supplier.
     */
    public void savePaymentConfigurationInt(TriggerProcess tp)
            throws ObjectNotFoundException, OperationNotPermittedException;

    /**
     * Save or delete the images resource in/from the database
     * 
     * @param imageResource
     *            the image resource to store/delete
     * @param organizationKey
     *            the organizationKey key
     */
    public void processImage(ImageResource imageResource, long organizationKey)
            throws ValidationException;

    /**
     * Updates the current user data.
     * 
     * @param organization
     *            the domain object containing the data of the organization the
     *            current user belongs to, or <code>null</code> for users who
     *            are not allowed to modify organization data
     * @param user
     *            the value object containing the user data of the user to
     *            modify or <code>null</code> if the user shall not be modified
     * @param marketplaceId
     *            the marketplace context to get customized texts from - can be
     *            <code>null</code>
     * @throws ValidationException
     *             Thrown if the validation of the value objects failed
     * @throws TechnicalServiceOperationException
     *             Thrown if an error occurs on service side when modifying the
     *             user data
     * @throws TechnicalServiceNotAliveException
     *             Thrown if a service is to be notified about a user data
     *             change but is not reachable
     * @throws OperationNotPermittedException
     *             Thrown if a caller attempts to modify the user account
     *             details for another user
     * @throws NonUniqueBusinessKeyException
     *             Thrown if user with the given ID already exists in the
     *             organization.
     * @throws DistinguishedNameException
     *             Thrown if the provided distinguished name is already used by
     *             another organization.
     * @throws ConcurrentModificationException
     *             Thrown if the object versions does not match.
     */
    public void updateAccountInformation(Organization organization,
            VOUserDetails user, String marketplaceId)
            throws ValidationException, NonUniqueBusinessKeyException,
            OperationNotPermittedException, TechnicalServiceNotAliveException,
            TechnicalServiceOperationException, DistinguishedNameException,
            ConcurrentModificationException;

    /**
     * Updates the discount information for a client organization.
     * 
     * @param organization
     *            value object containing the new discount information for the
     *            organization.
     * @param discount
     *            discount object
     * @param discountVersion
     *            version of the discount object
     * @return VOOrganization updated organization value object
     * 
     * @throws ObjectNotFoundException
     *             Thrown if there is no such organization.
     * @throws ValidationException
     *             Thrown if constraints for discounts are not respected.
     * @throws OperationNotPermittedException
     *             Thrown if the calling organization is not registered as
     *             supplier for the organization to be updated.
     * @throws ConcurrentModificationException
     *             if the stored discount information is changed by another user
     *             in the time between reading and writing it
     * @throws EJBException
     *             Thrown if the user has no SERVICE_MANAGER role.
     */
    public Discount updateCustomerDiscount(Organization organization,
            Discount discount, Integer discountVersion)
            throws ObjectNotFoundException, ValidationException,
            OperationNotPermittedException, ConcurrentModificationException;

    /**
     * Returns a list of all users belonging to the specified organization that
     * have the administrator role.
     * 
     * @param organizationKey
     *            the key of the organization to get the administrators for
     * @return a list of users having the administrator role.
     */
    public List<PlatformUser> getOrganizationAdmins(long organizationKey);

    /**
     * FIXME: Duplicated in local and remote interface
     * <p>
     * 
     * @see AccountService#updateAccountInformation(VOOrganization,
     *      VOUserDetails, String, VOImageResource)
     */
    public void updateAccountInformation(VOOrganization organization,
            VOUserDetails user, String marketplaceId,
            VOImageResource imageResource) throws ValidationException,
            NonUniqueBusinessKeyException, OperationNotPermittedException,
            TechnicalServiceNotAliveException,
            TechnicalServiceOperationException, ObjectNotFoundException,
            DistinguishedNameException, ConcurrentModificationException,
            ImageException;

    /**
     * FIXME: Duplicated in local and remote interface
     * 
     * @see AccountService#getOrganizationDataFallback()
     */
    public VOOrganization getOrganizationDataFallback();

    /**
     * Returns true if the payment type with the given key is enabled for the
     * given service, otherwise false.
     * 
     * @param serviceKey
     *            the key of the service for which the payment type is checked.
     * @param paymentTypeKey
     *            the key of the payment type to be checked.
     * @throws ObjectNotFoundException
     *             Thrown if there is no such service.
     * @return true if the payment type with the given key is enabled for the
     *         given service, otherwise false.
     */
    public boolean isPaymentTypeEnabled(long serviceKey, long paymentTypeKey)
            throws ObjectNotFoundException;

    /**
     * Check is user number bigger than max value
     * 
     * @throws MailOperationException
     * @return true in case the operation passed without problem
     */
    public boolean checkUserNum() throws MailOperationException;

}
