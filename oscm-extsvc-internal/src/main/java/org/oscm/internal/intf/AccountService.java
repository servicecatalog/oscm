/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2009-02-05                                                       
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.intf;

import java.util.List;
import java.util.Set;

import javax.ejb.EJBException;
import javax.ejb.Remote;

import org.oscm.internal.types.enumtypes.PerformanceHint;
import org.oscm.internal.types.exception.AddMarketingPermissionException;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.DeletionConstraintException;
import org.oscm.internal.types.exception.DistinguishedNameException;
import org.oscm.internal.types.exception.ImageException;
import org.oscm.internal.types.exception.MailOperationException;
import org.oscm.internal.types.exception.MandatoryUdaMissingException;
import org.oscm.internal.types.exception.MarketingPermissionNotFoundException;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.OperationPendingException;
import org.oscm.internal.types.exception.OrganizationAuthoritiesException;
import org.oscm.internal.types.exception.PaymentDataException;
import org.oscm.internal.types.exception.PaymentDeregistrationException;
import org.oscm.internal.types.exception.RegistrationException;
import org.oscm.internal.types.exception.ServiceParameterException;
import org.oscm.internal.types.exception.SubscriptionStateException;
import org.oscm.internal.types.exception.TechnicalServiceNotAliveException;
import org.oscm.internal.types.exception.TechnicalServiceOperationException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.LdapProperties;
import org.oscm.internal.vo.VOBillingContact;
import org.oscm.internal.vo.VOImageResource;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.internal.vo.VOOrganizationPaymentConfiguration;
import org.oscm.internal.vo.VOPaymentInfo;
import org.oscm.internal.vo.VOPaymentType;
import org.oscm.internal.vo.VOServicePaymentConfiguration;
import org.oscm.internal.vo.VOTechnicalService;
import org.oscm.internal.vo.VOUda;
import org.oscm.internal.vo.VOUdaDefinition;
import org.oscm.internal.vo.VOUserDetails;

/**
 * Remote interface of the account management service.
 */
@Remote
public interface AccountService {

    /**
     * Loads the image of an organization. An image can be associated with
     * organizations having one of the following roles: technology provider,
     * supplier, broker, reseller.
     * <p>
     * Required role: none
     * 
     * @param organizationKey
     *            the numeric key of the organization
     * @return a <code>VOImageResource</code> object, or <code>null</code> if no
     *         image is found
     */

    public VOImageResource loadImageOfOrganization(long organizationKey);

    /**
     * Marks the organization of the calling user as deleted. Only a customer
     * organization can be deregistered, if it does not have any active
     * subscriptions. All users of the organization are deleted.
     * <p>
     * Required role: administrator of the organization
     * 
     * @throws DeletionConstraintException
     *             if the organization has active subscriptions, or has other
     *             roles beside the customer role
     * @throws TechnicalServiceNotAliveException
     *             if the underlying technical service cannot be reached
     * @throws TechnicalServiceOperationException
     *             if a technical operation related to the subscription process
     *             fails
     */
    public void deregisterOrganization() throws DeletionConstraintException,
            TechnicalServiceNotAliveException,
            TechnicalServiceOperationException;

    /**
     * Retrieves the data for the organization of the calling user.
     * <p>
     * Required role: administrator of the organization
     * 
     * @return the organization data
     */

    public VOOrganization getOrganizationData();

    /**
     * Retrieves the data for the organization of the calling user with en
     * description if other local description is empty.
     * <p>
     * Required role: administrator of the organization
     * 
     * @return the organization data
     */
    public VOOrganization getOrganizationDataFallback();

    /**
     * Returns the organization ID for a given subscription key. With this
     * method, you can perform a login without knowing the organization ID, for
     * example, if a subscription key is known from a URL.
     * <p>
     * Required role: any user role in an organization
     * 
     * @param subscriptionKey
     *            the numeric key of the subscription
     * 
     * @return the organization ID
     * @throws ObjectNotFoundException
     * @throws ServiceParameterException
     * @throws OperationNotPermittedException
     * @throws SubscriptionStateException
     *             if the subscription was suspended (e.g. due to invalid
     *             payment information)
     */

    public String getOrganizationId(long subscriptionKey)
            throws ObjectNotFoundException, ServiceParameterException,
            OperationNotPermittedException, SubscriptionStateException;

    String getLocalizedAttributeName(long key, String locale);

    /**
     * Registers a new customer organization for the calling user who is not yet
     * known to the platform. The registration establishes the contractual
     * relationship between the new organization and the platform operator.
     * <p>
     * If the method is successful, a new organization with the given
     * administrator and payment information is created. The organization is
     * assigned the
     * {@link org.oscm.internal.types.enumtypes.OrganizationRoleType#CUSTOMER}
     * role.
     * <p>
     * Required role: none
     * 
     * @param organization
     *            the value object containing the data of the new organization
     * @param admin
     *            the value object containing the data of the new organization's
     *            initial administrator
     * @param password
     *            the password of the organization administrator, or
     *            <code>null</code>
     * @param serviceKey
     *            the numeric key of the service to which the organization is to
     *            subscribe after the registration, or <code>null</code>
     * @param marketplaceId
     *            the ID of the marketplace from which to get customized texts
     * @param sellerId
     *            optionally, the ID of a supplier, broker, or reseller
     *            organization for which the new customer is to be registered
     * 
     * @return the registered organization
     * @throws NonUniqueBusinessKeyException
     *             if the organization ID is not unique
     * @throws ValidationException
     *             if the validation of a value object fails
     * @throws ValidationException
     *             if the organization data contain a seller ID which does not
     *             exist in the database
     * @throws ObjectNotFoundException
     *             if a domain object is not found
     * @throws MailOperationException
     *             if the mail with the initial password cannot be sent to the
     *             organization's administrator
     * @throws RegistrationException
     *             if the registration process fails
     */

    public VOOrganization registerCustomer(VOOrganization organization,
            VOUserDetails admin, String password, Long serviceKey,
            String marketplaceId, String sellerId)
            throws NonUniqueBusinessKeyException, ValidationException,
            ObjectNotFoundException, MailOperationException,
            RegistrationException;

    /**
     * Creates a new customer for the supplier, broker, or reseller organization
     * the calling user is a member of. The calling user's organization is set
     * as the supplier, broker, or reseller for the new organization. The new
     * organization is assigned the
     * {@link org.oscm.internal.types.enumtypes.OrganizationRoleType#CUSTOMER}
     * role and an organization ID is generated.
     * <p>
     * Required role: service manager of a supplier organization, reseller of a
     * reseller organization, or broker of a broker organization
     * 
     * @param organization
     *            the value object containing the data of the new customer
     *            organization
     * @param user
     *            the value object containing the data of the new organization's
     *            initial administrator
     * @param organizationProperties
     *            additional optional organization properties (e.g. remote LDAP
     *            properties) that are to be stored in the
     *            <code>OrganizationSetting</code> table, or <code>null</code>
     *            if there are no such data
     * @param marketplaceId
     *            the ID of the marketplace for which the customer is to be
     *            registered
     * @return the registered organization
     * @throws OrganizationAuthoritiesException
     *             if the calling user's organization does not have the
     *             supplier, broker, or reseller role
     * @throws ValidationException
     *             if specified values (e.g. user key) do not match the
     *             validation constraints
     * @throws NonUniqueBusinessKeyException
     *             if the generation of a unique organization ID fails
     * @throws MailOperationException
     *             if the mail with the initial password cannot be sent to the
     *             new organization's administrator
     * @throws ObjectNotFoundException
     *             if a domain object is not found
     * @throws OperationPendingException
     *             if another request to register a customer with the same
     *             administrator ID or administrator email address is pending
     */

    public VOOrganization registerKnownCustomer(VOOrganization organization,
            VOUserDetails user, LdapProperties organizationProperties,
            String marketplaceId) throws OrganizationAuthoritiesException,
            ValidationException, NonUniqueBusinessKeyException,
            MailOperationException, ObjectNotFoundException,
            OperationPendingException;

    /**
     * Updates the data of the organization to which the calling user belongs,
     * or the data of the given user.
     * <p>
     * Required role: any user role in an organization to update one's own data;
     * administrator of the organization to update the data of other users or of
     * the organization.
     * 
     * @param organization
     *            the value object containing the new organization data, or
     *            <code>null</code> if no user is to be modified or if the
     *            calling user is not allowed to modify organization data
     * @param user
     *            the value object containing the data of a user to modify, or
     *            <code>null</code> if no user is to be modified
     * @param marketplaceId
     *            optionally, the ID of the marketplace from which to get
     *            customized texts. By default, the standard texts of the
     *            platform are used.
     * @param imageResource
     *            the new image for the organization, or <code>null</code> if
     *            the existing image is to remain unchanged. If the existing
     *            image is to be deleted, <code>imageResource.getBuffer()</code>
     *            must return <code>null</code>.
     * @throws ValidationException
     *             if the validation of a value object fails
     * @throws NonUniqueBusinessKeyException
     *             if a user with the given key already exists in the
     *             organization
     * @throws OperationNotPermittedException
     *             if the caller attempts to modify the user account details for
     *             another user but is not allowed to do so
     * @throws TechnicalServiceNotAliveException
     *             if a service is to be notified about changes in user data but
     *             is not reachable
     * @throws TechnicalServiceOperationException
     *             if an error occurs in a service when modifying user data
     * @throws ObjectNotFoundException
     *             if the given user or organization is not found
     * @throws DistinguishedNameException
     *             if the provided distinguished name is already used by another
     *             organization
     * @throws ConcurrentModificationException
     *             if the data stored for the given user or organization is
     *             changed by another user in the time between reading and
     *             writing it
     * @throws ImageException
     *             if the <code>imageResource</code> parameter is not
     *             <code>null</code> and the organization to be changed does not
     *             have the technology provider, supplier, broker, or reseller
     *             role
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
     * Returns the registered customers of the supplier, broker, or reseller
     * organization the calling user is a member of.
     * <p>
     * Required role: service manager of a supplier organization, broker or a
     * broker organization, or reseller of a reseller organization
     * 
     * @return the customers of the organization
     * @throws OrganizationAuthoritiesException
     *             if the calling user's organization does not have the
     *             supplier, broker, or reseller role
     */

    public List<VOOrganization> getMyCustomers()
            throws OrganizationAuthoritiesException;

    /**
     * Returns the suppliers who are authorized to offer marketable services
     * based on the specified technical service.
     * <p>
     * Required role: technology manager of the technology provider organization
     * that owns the technical service
     * 
     * @param technicalService
     *            the technical service for which the suppliers are to be listed
     * @return the supplier organizations
     * @throws OperationNotPermittedException
     *             if the calling user's organization is not the owner of the
     *             technical service
     * @throws ObjectNotFoundException
     *             if the technical service is not found
     */

    public List<VOOrganization> getMyCustomersOptimization()
            throws OrganizationAuthoritiesException;

    public List<VOOrganization> getSuppliersForTechnicalService(
            VOTechnicalService technicalService)
            throws ObjectNotFoundException, OperationNotPermittedException;

    /**
     * Adds the suppliers with the given IDs to the list of suppliers who are
     * authorized to offer marketable services based on the specified technical
     * service.
     * <p>
     * Required role: technology manager of the technology provider organization
     * that owns the technical service
     * 
     * @param technicalService
     *            the technical service for which the suppliers are to be added
     * @param organizationIds
     *            the IDs of the supplier organizations to be added
     * @throws ObjectNotFoundException
     *             if the technical service is not found
     * @throws OperationNotPermittedException
     *             if the calling user's organization is not the owner of the
     *             technical service
     * @throws AddMarketingPermissionException
     *             if one of the specified organizations cannot be retrieved or
     *             does not have the supplier role
     */

    public void addSuppliersForTechnicalService(
            VOTechnicalService technicalService, List<String> organizationIds)
            throws ObjectNotFoundException, OperationNotPermittedException,
            AddMarketingPermissionException;

    /**
     * Removes the suppliers with the given IDs from the list of suppliers who
     * are authorized to offer marketable services based on the specified
     * technical service.
     * <p>
     * Required role: technology manager of the technology provider organization
     * that owns the technical service
     * 
     * @param technicalService
     *            the technical service for which the suppliers are to be
     *            removed
     * @param organizationIds
     *            the IDs of the supplier organizations to be removed
     * @throws OrganizationAuthoritiesException
     *             if the calling user's organization does not have the
     *             technology provider role
     * @throws ObjectNotFoundException
     *             if the technical service is not found
     * @throws OperationNotPermittedException
     *             if the calling user's organization is not the owner of the
     *             technical service
     * @throws MarketingPermissionNotFoundException
     *             if one of the specified suppliers does not exist in the list
     *             of suppliers for the technical service
     */

    public void removeSuppliersFromTechnicalService(
            VOTechnicalService technicalService, List<String> organizationIds)
            throws OrganizationAuthoritiesException, ObjectNotFoundException,
            OperationNotPermittedException,
            MarketingPermissionNotFoundException;

    /**
     * Stores a payment information object of a type that is not handled by a
     * payment service provider (PSP) for the customer organization the calling
     * user is a member of.
     * <p>
     * Required role: administrator of the organization
     * 
     * @param paymentInfo
     *            the payment information to be saved
     * @return the saved payment information
     * @throws ObjectNotFoundException
     *             if the specified payment type is not found
     * @throws PaymentDeregistrationException
     *             if the payment type is changed from a PSP-managed type to a
     *             supplier/reseller-managed type and the deregistration of the
     *             existing information fails on the PSP side
     * @throws NonUniqueBusinessKeyException
     *             if a payment information object with the given ID already
     *             exists
     * @throws ConcurrentModificationException
     *             if the stored payment information is changed by another user
     *             in the time between reading and writing it
     * @throws ValidationException
     *             if the specified payment information ID is not valid
     * @throws OperationNotPermittedException
     *             when trying to access the payment information of another
     *             organization
     * @throws PaymentDataException
     *             if the payment type or ID is missing
     */

    public VOPaymentInfo savePaymentInfo(VOPaymentInfo paymentInfo)
            throws ObjectNotFoundException, PaymentDeregistrationException,
            NonUniqueBusinessKeyException, ConcurrentModificationException,
            ValidationException, OperationNotPermittedException,
            PaymentDataException;

    /**
     * Returns the list of payment types that are available to the supplier or
     * reseller organization the calling user is a member of. The payment types
     * a supplier or reseller can offer to his customers have to be enabled by
     * the operator. In case the list is empty, no payment types have been
     * enabled.
     * <p>
     * Required role: service manager of a supplier organization or reseller of
     * a reseller organization
     * 
     * @return the list of enabled payment types
     */

    Set<VOPaymentType> getAvailablePaymentTypesForOrganization();

    /**
     * Returns the list of payment types for which the customer organization of
     * the calling user can define payment information.
     * <p>
     * Required role: administrator of the customer organization
     * 
     * @return the list of payment types
     */

    Set<VOPaymentType> getAvailablePaymentTypes();

    /**
     * Returns the list of payment types which are available to the calling
     * user's organization for the specified service. If no payment types have
     * been enabled for the service or the customer by the relevant supplier or
     * reseller, the list is empty.
     * <p>
     * Required role: any user role in a customer organization
     * 
     * @param serviceKey
     *            the numeric key of the service
     * @return the list of payment types
     * @throws OrganizationAuthoritiesException
     *             if the calling user's organization does not have the customer
     *             role
     * @throws ObjectNotFoundException
     *             if the service is not found
     */

    public Set<VOPaymentType> getAvailablePaymentTypesFromOrganization(
            Long serviceKey) throws OrganizationAuthoritiesException,
            ObjectNotFoundException;

    /**
     * Retrieves the billing contacts of the calling user's organization.
     * <p>
     * Required role: administrator of the organization
     * 
     * @return the billing contacts
     */

    public List<VOBillingContact> getBillingContacts();

    /**
     * Stores the specified billing contact for the calling user's organization.
     * <p>
     * Required role: administrator of the organization
     * 
     * @param billingContact
     *            the value object with the information to be stored
     * @return the value object with the stored billing contact information
     * @throws ConcurrentModificationException
     *             if the stored billing contact is changed by another user in
     *             the time between reading and writing it
     * @throws ValidationException
     *             if the company name or address is too long
     * @throws NonUniqueBusinessKeyException
     *             if the specified ID is not unique
     * @throws OperationNotPermittedException
     *             if the specified billing contact is not owned by the calling
     *             user's organization
     */

    public VOBillingContact saveBillingContact(VOBillingContact billingContact)
            throws ConcurrentModificationException, ValidationException,
            NonUniqueBusinessKeyException, OperationNotPermittedException;

    /**
     * Deletes a billing contact for the calling user's organization.
     * <p>
     * Required role: administrator of the organization
     * 
     * @param billingContact
     *            the billing contact to be deleted
     * @throws ObjectNotFoundException
     *             if the billing contact is not found
     * @throws ConcurrentModificationException
     *             if the stored billing contact is changed by another user in
     *             the time between reading and deleting it
     * @throws OperationNotPermittedException
     *             if the specified billing contact is not owned by the calling
     *             user's organization
     */

    public void deleteBillingContact(VOBillingContact billingContact)
            throws ObjectNotFoundException, ConcurrentModificationException,
            OperationNotPermittedException;

    /**
     * Retrieves the payment configurations for all customers of the supplier or
     * reseller organization the calling user is a member of.
     * <p>
     * Required role: service manager of a supplier organization or reseller of
     * a reseller organization
     * 
     * @return the payment configurations for all customers
     */

    public List<VOOrganizationPaymentConfiguration> getCustomerPaymentConfiguration();

    /**
     * Retrieves the payment types that are enabled by default for new customers
     * of the supplier or reseller organization the calling user is a member of.
     * <p>
     * Required role: service manager of a supplier organization or reseller of
     * a reseller organization
     * 
     * @return the default payment types for customers
     */

    public Set<VOPaymentType> getDefaultPaymentConfiguration();

    /**
     * Saves payment configurations for the supplier or reseller organization
     * the calling user is a member of. The default configurations for the
     * organization's customers and services must be set. Optionally, you can
     * also specify customer-specific or service-specific payment
     * configurations.
     * <p>
     * The payment types specified in the <code>defaultConfiguration</code> and
     * <code>defaultServiceConfiguration</code> parameters are set as the
     * default for the customers or services of the calling user's organization.
     * The payment types given in
     * <code>VOOrganizationPaymentConfiguration.enabledPaymentTypes</code> or
     * <code>VOServicePaymentConfiguration.enabledPaymentTypes</code> are
     * enabled for the relevant customer or service.
     * <p>
     * Required role: service manager of a supplier organization or reseller of
     * a reseller organization
     * 
     * @param defaultConfiguration
     *            the default payment configuration for customers, i.e. the set
     *            of enabled payment types
     * @param customerConfigurations
     *            the customer-specific payment configurations, or
     *            <code>null</code> if no such configurations are to be saved
     * @param defaultServiceConfiguration
     *            the default payment configuration for services, i.e. the set
     *            of enabled payment types
     * @param serviceConfigurations
     *            the service-specific payment configurations, or
     *            <code>null</code> if no such configurations are to be saved
     * @return <code>false</code> if the method execution was suspended,
     *         <code>true</code> otherwise
     * @throws ObjectNotFoundException
     *             if one of the referenced objects is not found
     * @throws OperationNotPermittedException
     *             if the calling user's organization is not the supplier or
     *             reseller for one of the given services
     * @throws OperationPendingException
     *             if another request to save a payment configuration for the
     *             calling user's organization is pending
     */

    public boolean savePaymentConfiguration(
            Set<VOPaymentType> defaultConfiguration,
            List<VOOrganizationPaymentConfiguration> customerConfigurations,
            Set<VOPaymentType> defaultServiceConfiguration,
            List<VOServicePaymentConfiguration> serviceConfigurations)
            throws ObjectNotFoundException, OperationNotPermittedException,
            OperationPendingException;

    /**
     * Updates the discount information for the specified customer of the
     * supplier organization the calling user is a member of.
     * <p>
     * Required role: service manager of the supplier organization
     * 
     * @param organization
     *            the value object containing the new discount information.
     * @return the updated organization data
     * 
     * @throws ObjectNotFoundException
     *             if the specified customer organization is not found
     * @throws ValidationException
     *             if constraints for discounts are not respected
     * @throws OperationNotPermittedException
     *             if the calling user's organization is not registered as a
     *             supplier for the customer organization to be updated, or if
     *             any organization data other than the discount is to be
     *             modified
     * @throws ConcurrentModificationException
     *             if the stored discount information is changed by another user
     *             in the time between reading and writing it
     * @throws EJBException
     *             if the calling user does not have the service manager role
     */

    public VOOrganization updateCustomerDiscount(VOOrganization organization)
            throws ObjectNotFoundException, ValidationException,
            OperationNotPermittedException, ConcurrentModificationException;

    /**
     * Returns the target types for custom attributes that are available to the
     * calling user's organization. A target type represents a type of entity
     * for which custom attributes can be defined, for example,
     * <code>CUSTOMER</code>.
     * <p>
     * Required role: any user role in an organization
     * 
     * @return the set of target types
     */

    public Set<String> getUdaTargetTypes();

    /**
     * Saves or deletes definitions of custom attributes. New attribute
     * definitions in the <code>udaDefinitionsToSave</code> list are created,
     * existing ones are updated. Attribute definitions specified in the
     * <code>udaDefinitionsToDelete</code> list are deleted, including all their
     * instances.
     * <p>
     * Required role: any user role in a supplier organization
     * 
     * @param udaDefinitionsToSave
     *            the list of custom attribute definitions to save
     * @param udaDefinitionsToDelete
     *            the list of custom attribute definitions to delete
     * @throws ValidationException
     *             if specified values (e.g. attribute ID) do not match the
     *             validation constraints
     * @throws OrganizationAuthoritiesException
     *             if the calling user's organization does not have the role
     *             required to define custom attributes for the specified target
     *             type
     * @throws NonUniqueBusinessKeyException
     *             if a custom attribute with the specified ID and target type
     *             already exists for the organization
     * @throws ObjectNotFoundException
     *             if one of the specified existing attributes is not found by
     *             its ID
     * @throws ConcurrentModificationException
     *             if the stored attribute definitions are changed by another
     *             user in the time between reading and writing or deleting them
     * @throws OperationNotPermittedException
     *             if the attribute definitions to be modified are not owned by
     *             the calling user's organization
     */

    public void saveUdaDefinitions(List<VOUdaDefinition> udaDefinitionsToSave,
            List<VOUdaDefinition> udaDefinitionsToDelete)
            throws ValidationException, OrganizationAuthoritiesException,
            NonUniqueBusinessKeyException, ObjectNotFoundException,
            ConcurrentModificationException, OperationNotPermittedException;

    /**
     * Retrieves the custom attributes owned by the calling user's organization.
     * <p>
     * Required role: any user role in an organization
     * 
     * @return the list of custom attribute definitions
     */

    public List<VOUdaDefinition> getUdaDefinitions();

    /**
     * Retrieves the custom attributes with the given target type for the entity
     * with the specified ID.
     * <p>
     * Required role: any user role in a supplier organization
     * 
     * @param targetType
     *            the target type (e.g. <code>CUSTOMER</code>) defined in the
     *            custom attribute
     * @param targetObjectKey
     *            the numeric key of the entity (e.g. customer or subscription)
     *            for which the custom attribute is to be retrieved
     * @param checkSeller
     *            boolean flag which determines if the calling organization
     *            should be checked if it is a seller for the specified customer
     *            with the targetObjectKey
     * @return the list of custom attributes
     * @throws ValidationException
     *             if the given target type is not valid
     * @throws OrganizationAuthoritiesException
     *             if the calling user's organization does not have the role
     *             required for handling custom attributes with the specified
     *             target type
     * @throws OperationNotPermittedException
     *             if the calling user's organization is not permitted to access
     *             the target object (e.g. the organization is not a supplier of
     *             the customer organization with the given ID)
     * @throws ObjectNotFoundException
     *             if the target object is not found
     */

    public List<VOUda> getUdas(String targetType, long targetObjectKey,
            boolean checkSeller) throws ValidationException,
            OrganizationAuthoritiesException, ObjectNotFoundException,
            OperationNotPermittedException;

    /**
     * Saves the specified list of custom attributes. For each attribute in the
     * list, the definition ({@link VOUdaDefinition}) must be set and is
     * validated. If the value of an attribute is set to <code>null</code>, it
     * is deleted if it exists (unless it is mandatory) or ignored if it does
     * not exist.
     * <p>
     * Required role: any user role in an organization
     * 
     * @param udas
     *            the list of attributes to save
     * @throws ValidationException
     *             if specified values (e.g. attribute values) do not match the
     *             validation constraints
     * @throws ConcurrentModificationException
     *             if the stored attributes or attribute definitions are changed
     *             by another user in the time between reading and writing them
     * @throws OperationNotPermittedException
     *             if the calling user's organization is not the owner of the
     *             custom attributes or is not permitted to access the target
     *             objects
     * @throws ObjectNotFoundException
     *             if one of the specified attributes, its definition, or a
     *             target object is not found
     * @throws NonUniqueBusinessKeyException
     *             if an attribute with the same definition already exists for
     *             the target object
     * @throws MandatoryUdaMissingException
     *             when trying to delete a mandatory attribute
     * @throws OrganizationAuthoritiesException
     *             if the calling user's organization does not have the role
     *             required for handling custom attributes with the specified
     *             target type
     */

    public void saveUdas(List<VOUda> udas) throws ValidationException,
            ObjectNotFoundException, OperationNotPermittedException,
            ConcurrentModificationException, NonUniqueBusinessKeyException,
            MandatoryUdaMissingException, OrganizationAuthoritiesException;

    /**
     * Returns the countries that are supported by the platform. Users can
     * select their organization's country from this list.
     * <p>
     * Required role: any user role in an organization
     * 
     * @return a list of ISO 3166 country codes
     */

    public List<String> getSupportedCountryCodes();

    /**
     * Retrieves information on the specified supplier, broker, or reseller
     * organization.
     * <p>
     * Required role: any user role in an organization
     * 
     * @param sellerId
     *            the ID of the supplier, broker, or reseller organization
     * @param locale
     *            the language in which to retrieve the organization
     *            information. Specify a language code as returned by
     *            <code>getLanguage()</code> of <code>java.util.Locale</code>.
     * @return the organization information
     * @throws ObjectNotFoundException
     *             if the organization is not found or does not have the
     *             supplier, broker, or reseller role
     */

    public VOOrganization getSeller(String sellerId, String locale)
            throws ObjectNotFoundException;

    /**
     * Retrieves information for the specified customer organization.
     * <p>
     * Required role: service manager of a supplier organization, broker or a
     * broker organization, or reseller of a reseller organization
     * 
     * @param organization
     *            object the organization object with key or organization id of
     *            the customer organization
     * @param locale
     *            the language in which to retrieve the organization
     *            information. Specify a language code as returned by
     *            <code>getLanguage()</code> of <code>java.util.Locale</code>.
     * @return the organization information
     * @throws ObjectNotFoundException
     *             if the organization is not found.
     */

    public VOOrganization getMyCustomer(VOOrganization org, String locale)
            throws ObjectNotFoundException;

    /**
     * Deletes the specified payment information for the calling user's
     * organization.
     * <p>
     * Required role: administrator of the organization
     * 
     * @param paymentInfo
     *            the payment information to delete
     * @throws ObjectNotFoundException
     *             if the payment information is not found
     * @throws OperationNotPermittedException
     *             if the calling user's organization is not the owner of the
     *             payment information
     * @throws ConcurrentModificationException
     *             if the stored payment information is changed by another user
     *             in the time between reading and writing it
     * @throws PaymentDeregistrationException
     */

    public void deletePaymentInfo(VOPaymentInfo paymentInfo)
            throws ObjectNotFoundException, OperationNotPermittedException,
            ConcurrentModificationException, PaymentDeregistrationException;

    /**
     * Retrieves the payment information for the calling user's organization.
     * <p>
     * Required role: administrator of the organization or subscription manager
     * 
     * @return the payment information
     */

    public List<VOPaymentInfo> getPaymentInfos();

    /**
     * Retrieves the payment information for the calling user's organization.
     * <p>
     * Required role: administrator of the organization
     * 
     * @return the payment information
     */

    public List<VOPaymentInfo> getPaymentInfosForOrgAdmin();

    /**
     * Retrieves the payment types that are enabled by default for new services
     * provided by the organization the calling user is a member of.
     * <p>
     * Required role: service manager of a supplier organization or reseller of
     * a reseller organization
     * 
     * @return the default payment types for services
     */

    public Set<VOPaymentType> getDefaultServicePaymentConfiguration();

    /**
     * Retrieves the payment configurations for all services provided by the
     * organization the calling user is a member of.
     * <p>
     * Required role: service manager of a supplier organization or reseller of
     * a reseller organization
     * 
     * @return the payment configurations for all services
     */

    public List<VOServicePaymentConfiguration> getServicePaymentConfiguration();

    /**
     * Retrieves the payment configurations for all services provided by the
     * organization the calling user is a member of.
     * <p>
     * Required role: service manager of a supplier organization or reseller of
     * a reseller organization
     * 
     * @param performanceHint
     *            for optimized access
     * @return the payment configurations for all services
     */

    public List<VOServicePaymentConfiguration> getServicePaymentConfiguration(
            PerformanceHint performanceHint);

    /**
     * Retrieves the custom attributes which have been defined by the given
     * supplier organization and are presented to customers when they subscribe
     * to a service.
     * <p>
     * Required role: any user role in an organization
     * 
     * @param supplierId
     *            the ID of the supplier organization whose custom attributes
     *            are to be listed
     * @return the list of custom attribute definitions
     * @throws ObjectNotFoundException
     *             if the supplier organization is not found
     */

    public List<VOUdaDefinition> getUdaDefinitionsForCustomer(String supplierId)
            throws ObjectNotFoundException;

    /**
     * Retrieves the custom attributes with the given target type for the entity
     * with the specified ID, which have been defined by the given supplier
     * organization and are presented to customers when they subscribe to a
     * service.
     * 
     * <p>
     * Required role: any user role in an organization
     * 
     * @param targetType
     *            the target type (e.g. <code>CUSTOMER</code>) defined in the
     *            custom attribute
     * @param targetObjectKey
     *            the numeric key of the entity (e.g. customer or subscription)
     *            for which the custom attribute is to be retrieved
     * @param supplierId
     *            the ID of the supplier organization whose custom attributes
     *            are to be listed
     * @return the list of custom attributes
     * @throws ValidationException
     *             if the given target type is not valid
     * @throws OrganizationAuthoritiesException
     *             if the calling user's organization does not have the role
     *             required for handling custom attributes with the specified
     *             target type
     * @throws ObjectNotFoundException
     *             if the target object or the supplier organization is not
     *             found
     * @throws OperationNotPermittedException
     *             if the calling user's organization is not permitted to access
     *             the target object
     */

    public List<VOUda> getUdasForCustomer(String targetType,
            long targetObjectKey, String supplierId)
            throws ValidationException, OrganizationAuthoritiesException,
            ObjectNotFoundException, OperationNotPermittedException;

    /**
     * Retrieves current amount of registered users
     * 
     * @return the number of currently registered users
     * 
     */
    public long countRegisteredUsers();
}
