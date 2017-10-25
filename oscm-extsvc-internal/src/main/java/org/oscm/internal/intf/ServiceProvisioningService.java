/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 05.02.2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.intf;

import java.util.List;

import javax.ejb.Remote;

import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.types.exception.BillingAdapterNotFoundException;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.CurrencyException;
import org.oscm.internal.types.exception.DeletionConstraintException;
import org.oscm.internal.types.exception.ImportException;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.OperationPendingException;
import org.oscm.internal.types.exception.OrganizationAuthoritiesException;
import org.oscm.internal.types.exception.PaymentInformationException;
import org.oscm.internal.types.exception.PriceModelException;
import org.oscm.internal.types.exception.ServiceCompatibilityException;
import org.oscm.internal.types.exception.ServiceNotPublishedException;
import org.oscm.internal.types.exception.ServiceOperationException;
import org.oscm.internal.types.exception.ServiceStateException;
import org.oscm.internal.types.exception.SubscriptionStateException;
import org.oscm.internal.types.exception.TechnicalServiceActiveException;
import org.oscm.internal.types.exception.TechnicalServiceMultiSubscriptions;
import org.oscm.internal.types.exception.TechnicalServiceNotAliveException;
import org.oscm.internal.types.exception.UnchangeableAllowingOnBehalfActingException;
import org.oscm.internal.types.exception.UpdateConstraintException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOCompatibleService;
import org.oscm.internal.vo.VOCustomerService;
import org.oscm.internal.vo.VOImageResource;
import org.oscm.internal.vo.VOLocalizedText;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.internal.vo.VOPriceModel;
import org.oscm.internal.vo.VOPriceModelLocalization;
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOServiceActivation;
import org.oscm.internal.vo.VOServiceDetails;
import org.oscm.internal.vo.VOServiceEntry;
import org.oscm.internal.vo.VOServiceLocalization;
import org.oscm.internal.vo.VOSubscriptionDetails;
import org.oscm.internal.vo.VOTechnicalService;

/**
 * Remote interface for defining and provisioning services.
 * 
 */
@Remote
public interface ServiceProvisioningService {

    /**
     * Retrieves a list of the marketable services provided by the calling
     * user's organization.
     * <p>
     * Required role: service manager of a supplier organization, broker of a
     * broker organization, or reseller of a reseller organization
     * 
     * @return the list of services
     */

    public List<VOService> getSuppliedServices();

    /**
     * Retrieves the technical services which are visible to the calling user's
     * organization according to the given organization role.
     * <p>
     * <ul>
     * <li>If the organization role is {@link OrganizationRoleType#SUPPLIER},
     * the method returns all technical services for which the calling user's
     * organization has been appointed as a supplier by a technology provider.
     * <li>If the organization role is
     * {@link OrganizationRoleType#TECHNOLOGY_PROVIDER}, all technical services
     * provided by the calling user's organization are returned.
     * </ul>
     * <p>
     * An empty list is returned for other organization roles or if the calling
     * user's organization does not have the specified role.
     * <p>
     * Required role: service manager of a supplier organization to retrieve the
     * technical services for the supplier role; technology manager of a
     * technology provider organization to retrieve the technical services for
     * the technology provider role
     * 
     * @param role
     *            an <code>OrganizationRoleType</code> object specifying the
     *            organization role for which the list is to be returned
     * 
     * @return the list of technical services
     * @throws OrganizationAuthoritiesException
     *             if the calling user's organization does not have the
     *             specified role
     */

    public List<VOTechnicalService> getTechnicalServices(
            OrganizationRoleType role) throws OrganizationAuthoritiesException;

    /**
     * Validates whether the correct version of the provisioning service is
     * running for the application underlying to the given technical service.
     * <p>
     * Required role: any user role in a supplier or technology provider
     * organization
     * 
     * @param technicalService
     *            the technical service to validate
     * 
     * @throws ObjectNotFoundException
     *             if the technical service is not found
     * @throws OperationNotPermittedException
     *             if the calling user's organization is not allowed to access
     *             the technical service
     * @throws TechnicalServiceNotAliveException
     *             if the provisioning service does not respond correctly
     */

    public void validateTechnicalServiceCommunication(
            VOTechnicalService technicalService)
            throws ObjectNotFoundException, OperationNotPermittedException,
            TechnicalServiceNotAliveException;

    /**
     * Deletes the given technical service, if it is not referenced by any
     * marketable service.
     * <p>
     * Required role: technology manager of the technology provider organization
     * that owns the service
     * 
     * @param technicalService
     *            the technical service to delete
     * @throws OrganizationAuthoritiesException
     *             if the calling user's organization does not have the
     *             technology provider role
     * @throws ObjectNotFoundException
     *             if the technical service is not found
     * @throws DeletionConstraintException
     *             if the technical service is referenced by a marketable
     *             service that cannot be removed
     * @throws ConcurrentModificationException
     *             if the stored technical service is changed by another user in
     *             the time between reading and deleting it
     */

    public void deleteTechnicalService(VOTechnicalService technicalService)
            throws OrganizationAuthoritiesException, ObjectNotFoundException,
            DeletionConstraintException, ConcurrentModificationException;

    /**
     * Creates a marketable service based on the given technical service. The
     * parameters of the new marketable service are obtained from the technical
     * service and from the specified value object. Note that the license
     * information is ignored.
     * <p>
     * Required role: service manager of a supplier organization
     * 
     * @param technicalService
     *            the technical service the marketable service is to be based on
     * @param service
     *            the value object specifying the marketable service to create
     * @param imageResource
     *            optionally an image for the new service, or <code>null</code>
     *            if no image is to be added
     * @return a value object with the definition of the new marketable service
     * 
     * @throws OrganizationAuthoritiesException
     *             if the calling user's organization does not have the supplier
     *             role
     * @throws ObjectNotFoundException
     *             if the technical service or a parameter definition is not
     *             found
     * @throws OperationNotPermittedException
     *             if the calling user does not have the required permissions
     * @throws ValidationException
     *             if the validation of the value object for the new service
     *             fails
     * @throws NonUniqueBusinessKeyException
     *             if a marketable service with the given identifier is already
     *             supplied by the calling user's organization
     * @throws ConcurrentModificationException
     *             if the stored technical service is changed by another user in
     *             the time between reading it and creating the marketable
     *             service
     */

    public VOServiceDetails createService(VOTechnicalService technicalService,
            VOService service, VOImageResource imageResource)
            throws OrganizationAuthoritiesException, ObjectNotFoundException,
            OperationNotPermittedException, ValidationException,
            NonUniqueBusinessKeyException, ConcurrentModificationException;

    /**
     * Updates the definition of the given marketable service.
     * <p>
     * The parameters and parameter values for the service are obtained from the
     * value object. Note that the license information is ignored.
     * <p>
     * The changes are valid for all new subscriptions. Existing subscriptions
     * are not affected.
     * <p>
     * Required role: service manager of the supplier organization that owns the
     * service
     * 
     * @param service
     *            the value object specifying the new parameters and values to
     *            be set for the service
     * @param imageResource
     *            optionally a new image for the service, or <code>null</code>
     *            if no image is to be set
     * @return a value object with the updated service definition
     * @throws ObjectNotFoundException
     *             if the service is not found
     * @throws OrganizationAuthoritiesException
     *             if the calling user's organization does not have the supplier
     *             role
     * @throws OperationNotPermittedException
     *             if the calling user does not have the permissions required to
     *             execute the method with the given parameters and values
     * @throws ValidationException
     *             if the validation of the value object for the service fails,
     *             for example, because the given identifier is too long
     * @throws NonUniqueBusinessKeyException
     *             if the service identifier is not unique
     * @throws ServiceStateException
     *             if the service status is not {@link ServiceStatus#INACTIVE}
     * @throws ConcurrentModificationException
     *             if the stored service is changed by another user in the time
     *             between reading and writing it
     */

    public VOServiceDetails updateService(VOServiceDetails service,
            VOImageResource imageResource) throws ObjectNotFoundException,
            OrganizationAuthoritiesException, OperationNotPermittedException,
            ValidationException, NonUniqueBusinessKeyException,
            ServiceStateException, ConcurrentModificationException;

    /**
     * Retrieves a customer-specific variant of a marketable service. The
     * service returned is a copy of the specified marketable service, which is
     * valid for the given customer only. Such a copy is created when a supplier
     * defines a customer-specific price model for a marketable service.
     * <p>
     * Required role: service manager of the supplier organization that owns the
     * services
     * 
     * @param customer
     *            the customer to retrieve the marketable service for
     * @param service
     *            the marketable service on which the customer-specific variant
     *            is based
     * @return a value object with the details of the customer-specific service
     *         or <code>null</code> if none exists
     * @throws OperationNotPermittedException
     *             if the calling user does not have the required permissions
     * @throws ObjectNotFoundException
     *             if the given service or customer is not found
     */

    public VOServiceDetails getServiceForCustomer(VOOrganization customer,
            VOService service) throws OperationNotPermittedException,
            ObjectNotFoundException;

    /**
     * Retrieves a subscription-specific variant of a marketable service. The
     * service returned is a copy of the specified marketable service, which is
     * valid for the given customer and subscription only. Such a copy is
     * created when a customer subscribes to a marketable service.
     * <p>
     * Required role: service manager of the supplier organization that owns the
     * services
     * 
     * @param customer
     *            the customer the subscription belongs to
     * @param subscriptionId
     *            the identifier of the subscription
     * @return a value object with the details of the subscription-specific
     *         service, or <code>null</code> if none exists
     * @throws OrganizationAuthoritiesException
     *             if the calling user's organization does not have the supplier
     *             role
     * @throws ObjectNotFoundException
     *             if the customer is not found
     */

    public VOServiceDetails getServiceForSubscription(VOOrganization customer,
            String subscriptionId) throws OrganizationAuthoritiesException,
            ObjectNotFoundException;

    /**
     * Returns detailed information on the given marketable service.
     * <p>
     * Required role: service manager of the supplier organization that owns the
     * service, or broker or reseller of an authorized broker or reseller
     * organization
     * 
     * @param service
     *            the service to retrieve detailed information for
     * @return a value object with the service details
     * @throws ObjectNotFoundException
     *             if the service is not found
     * @throws OperationNotPermittedException
     *             if the calling user does not have the required permissions
     */

    public VOServiceDetails getServiceDetails(VOService service)
            throws ObjectNotFoundException, OperationNotPermittedException;

    /**
     * Returns the supplier, broker, or reseller organization that provides a
     * marketable service.
     * <p>
     * Required role: none
     * 
     * @param serviceKey
     *            the numeric key of the service to get the seller for
     * @param locale
     *            the language in which the information on the seller is to be
     *            returned. Specify a language code as returned by
     *            <code>getLanguage()</code> of <code>java.util.Locale</code>.
     * @return the seller organization for the given service
     * @throws ObjectNotFoundException
     *             if the service is not found
     */

    public VOOrganization getServiceSeller(long serviceKey, String locale)
            throws ObjectNotFoundException;

    /**
     * Creates or updates the price model for the given marketable service.
     * <p>
     * None of the parameters must be <code>null</code>. Setting
     * <code>null</code> for the price model will not reset the price model of
     * the service.
     * <p>
     * Required role: service manager of the supplier organization that owns the
     * service
     * 
     * @param service
     *            the value object with the details of the marketable service
     *            for which the price model is to be defined
     * @param priceModel
     *            the value object defining the price model
     * @return a value object with the service definition including the new
     *         price model details
     * @throws ObjectNotFoundException
     *             if the service is not found
     * @throws OperationNotPermittedException
     *             if the calling user does not have the required permissions
     * @throws CurrencyException
     *             if the currency specified in the price model is not supported
     * @throws ValidationException
     *             if a price set in the price model or for an event is
     *             negative, or if no billing period is set for a chargeable
     *             price model
     * @throws ServiceStateException
     *             if the service status is not {@link ServiceStatus#INACTIVE}
     * @throws PriceModelException
     *             when trying to change the currency of the price model in a
     *             way which is not allowed
     * @throws ConcurrentModificationException
     *             if the stored service or price model is changed by another
     *             user in the time between reading and writing it
     */

    public VOServiceDetails savePriceModel(VOServiceDetails service,
            VOPriceModel priceModel) throws ObjectNotFoundException,
            OperationNotPermittedException, CurrencyException,
            ValidationException, ServiceStateException, PriceModelException,
            ConcurrentModificationException;

    /**
     * Creates or updates a customer-specific price model for the specified
     * marketable service.
     * <p>
     * Required role: service manager of the supplier organization that owns the
     * service
     * 
     * @param service
     *            the value object with the details of the marketable service
     *            for which a customer-specific price model is to be created, or
     *            the customer-specific variant of the marketable service for
     *            which the price model is to be updated
     * @param priceModel
     *            the value object defining the price model
     * @param customer
     *            the customer for which the price model is to be defined
     * @return a value object with the service definition including the new
     *         price model details
     * @throws OrganizationAuthoritiesException
     *             if the calling user's organization does not have the supplier
     *             role
     * @throws ObjectNotFoundException
     *             if the service or customer is not found
     * @throws OperationNotPermittedException
     *             if the calling user does not have the required permissions
     * @throws CurrencyException
     *             if the currency specified in the price model is not supported
     * @throws ValidationException
     *             if a price set in the price model or for an event is
     *             negative, or if no billing period is set for a chargeable
     *             price model
     * @throws ServiceStateException
     *             if the service status is not {@link ServiceStatus#INACTIVE}
     *             when trying to update an existing price model
     * @throws PriceModelException
     *             when trying to change the currency of the price model in a
     *             way which is not allowed
     * @throws ServiceOperationException
     *             if a service and price model for the customer already exist
     *             and the given service is not the customer-specific variant
     *             but the general one
     * @throws ConcurrentModificationException
     *             if the stored service or price model is changed by another
     *             user in the time between reading and writing it
     */

    public VOServiceDetails savePriceModelForCustomer(VOServiceDetails service,
            VOPriceModel priceModel, VOOrganization customer)
            throws OrganizationAuthoritiesException, ObjectNotFoundException,
            OperationNotPermittedException, CurrencyException,
            ValidationException, ServiceStateException, PriceModelException,
            ServiceOperationException, ConcurrentModificationException;

    /**
     * Creates or updates a subscription-specific price model for the specified
     * marketable service. This is possible for services published directly by
     * the supplier or by an authorized broker.
     * <p>
     * Required role: service manager of the supplier organization that owns the
     * original service
     * 
     * @param service
     *            the value object specifying the subscription-specific variant
     *            of the original marketable service for which the price model
     *            is to be created or updated
     * @param priceModel
     *            the value object defining the price model
     * @return a value object with the service definition including the new
     *         price model details
     * @throws OrganizationAuthoritiesException
     *             if the calling user's organization does not have the supplier
     *             role
     * @throws ObjectNotFoundException
     *             if the service or subscription is not found
     * @throws OperationNotPermittedException
     *             if the calling user does not have the required permissions
     * @throws CurrencyException
     *             if the currency specified in the price model is not supported
     * @throws ValidationException
     *             if a price set in the price model or for an event is
     *             negative, or if no billing period is set for a chargeable
     *             price model
     * @throws ConcurrentModificationException
     *             if the stored service or price model is changed by another
     *             user in the time between reading and writing it
     * @throws SubscriptionStateException
     *             if the subscription status does not allow for the execution
     *             of this method
     * @throws PaymentInformationException
     *             if the price model cannot be defined as chargeable because no
     *             payment information is available for the customer owning the
     *             subscription
     * @throws PriceModelException
     *             when trying to change the 'chargeable' property or the
     *             currency of the price model in a way which is not allowed
     */

    public VOServiceDetails savePriceModelForSubscription(
            VOServiceDetails service, VOPriceModel priceModel)
            throws OrganizationAuthoritiesException, ObjectNotFoundException,
            OperationNotPermittedException, CurrencyException,
            ValidationException, ConcurrentModificationException,
            SubscriptionStateException, PaymentInformationException,
            PriceModelException;

    /**
     * Returns all marketable services supplied by the calling user's
     * organization to which the specified service can be upgraded or
     * downgraded.
     * <p>
     * Required role: service manager of the supplier organization that owns the
     * services
     * 
     * @param service
     *            the service for which the compatible services are to be
     *            retrieved
     * @return the list of compatible services
     * @throws OrganizationAuthoritiesException
     *             if the calling user's organization does not have the supplier
     *             role
     * @throws ObjectNotFoundException
     *             if the specified service is not found
     * @throws OperationNotPermittedException
     *             if the calling user does not have the required permissions
     */

    public List<VOService> getCompatibleServices(VOService service)
            throws OrganizationAuthoritiesException, ObjectNotFoundException,
            OperationNotPermittedException;

    /**
     * Registers a list of marketable services to which the specified source
     * service can be upgraded or downgraded later.
     * <p>
     * Required role: service manager of the supplier organization that owns the
     * services
     * 
     * @param service
     *            the service for which the compatible services are to be set
     * @param compatibleServices
     *            the list of services to be registered as compatible services
     * @throws OrganizationAuthoritiesException
     *             if the calling user's organization does not have the supplier
     *             role
     * @throws ObjectNotFoundException
     *             if one of the given services is not found
     * @throws OperationNotPermittedException
     *             if the calling user does not have the required permissions
     * @throws ServiceCompatibilityException
     *             if not all of the specified services are based on the same
     *             technical service
     * @throws ServiceStateException
     *             if the status of the source service is not
     *             {@link ServiceStatus#INACTIVE}
     * @throws ConcurrentModificationException
     *             if data stored for the services is changed by another user in
     *             the time between reading and writing it
     */

    public void setCompatibleServices(VOService service,
            List<VOService> compatibleServices)
            throws OrganizationAuthoritiesException, ObjectNotFoundException,
            OperationNotPermittedException, ServiceCompatibilityException,
            ServiceStateException, ConcurrentModificationException;

    /**
     * Deletes the specified marketable service, if there are no subscriptions
     * and resale permissions for it.
     * <p>
     * Required role: service manager of the supplier organization that owns the
     * service
     * 
     * @param service
     *            the marketable service to delete
     * @throws OrganizationAuthoritiesException
     *             if the calling user's organization does not have the supplier
     *             role
     * @throws OperationNotPermittedException
     *             if the calling user's organization is not the owner of the
     *             service
     * @throws ObjectNotFoundException
     *             if the service is not found
     * @throws ServiceOperationException
     *             if the service is used by a subscription or if a resale
     *             permission exists for it
     * @throws ServiceStateException
     *             if the service status is not {@link ServiceStatus#INACTIVE}
     * @throws ConcurrentModificationException
     *             if the stored service is changed by another user in the time
     *             between reading and deleting it
     */

    public void deleteService(VOService service)
            throws OrganizationAuthoritiesException,
            OperationNotPermittedException, ObjectNotFoundException,
            ServiceOperationException, ServiceStateException,
            ConcurrentModificationException;

    /**
     * Checks if the specified marketable service has a status which allows for
     * its deletion.
     * <p>
     * Required role: service manager of the supplier organization that owns the
     * service
     * 
     * @param service
     *            the marketable service to check
     * @return <code>true</code> if the service can be deleted, for example,
     *         because it is inactive; <code>false</code> otherwise
     * @throws OperationNotPermittedException
     *             if the calling user does not have the required permissions
     * @throws ObjectNotFoundException
     *             if the service is not found
     * @throws ConcurrentModificationException
     *             if the stored service is changed by another user in the time
     *             between reading it and checking its status
     */

    public boolean statusAllowsDeletion(VOService service)
            throws OperationNotPermittedException, ObjectNotFoundException,
            ConcurrentModificationException;

    /**
     * Imports technical services from an XML file into the database.
     * <p>
     * Required role: technology manager of a technology provider organization
     * 
     * @param xml
     *            The XML file containing the definition of the technical
     *            services
     * @return the result message created by the XML parsing process
     * @throws ImportException
     *             if the import failed
     * @throws OperationNotPermittedException
     *             if the calling user does not have the required permissions
     * @throws TechnicalServiceActiveException
     *             if a technical service to be imported already exists and is
     *             in use
     * @throws UpdateConstraintException
     *             if the import tries to change the license information or a
     *             parameter value type or to remove an event or a parameter
     *             definition for an existing technical service which is already
     *             used by a marketable service
     * @throws TechnicalServiceMultiSubscriptions
     *             if multiple subscriptions already exist for a technical
     *             service to be imported, but the updated definition does not
     *             allow for multiple subscriptions
     * @throws UnchangeableAllowingOnBehalfActingException
     *             when trying to change the <code>allowingOnBehalfActing</code>
     *             setting for an existing technical service which already has
     *             subscriptions
     * @throws BillingAdapterNotFoundException
     *             when trying to import technical service with billing
     *             identifier which is not registered in the system
     */

    public String importTechnicalServices(byte[] xml) throws ImportException,
            OperationNotPermittedException, TechnicalServiceActiveException,
            UpdateConstraintException, TechnicalServiceMultiSubscriptions,
            UnchangeableAllowingOnBehalfActingException,
            BillingAdapterNotFoundException;

    /**
     * Activates a marketable service so that it becomes available to potential
     * customers. The service status is set to {@link ServiceStatus#ACTIVE}.
     * <p>
     * Before you can activate a service, a price model must have been defined
     * for it. An active service cannot be modified. Therefore, it must be
     * deactivated explicitly for any update operation.
     * <p>
     * Required role: service manager of the supplier organization that owns the
     * service, or broker or reseller of an authorized broker or reseller
     * organization
     * 
     * @param service
     *            the service to activate
     * @return the activated service, or <code>null</code> if the operation was
     *         suspended
     * @throws ObjectNotFoundException
     *             if the service is not found
     * @throws ServiceStateException
     *             if the service has a status in which it cannot be activated
     * @throws OrganizationAuthoritiesException
     *             if the calling user's organization does not have the
     *             supplier, broker, or reseller role
     * @throws OperationNotPermittedException
     *             if the calling user's organization is neither the supplier of
     *             the service nor an authorized broker or reseller
     * @throws ServiceOperationException
     *             if no price model is defined for the service
     * @throws TechnicalServiceNotAliveException
     *             if the provisioning service of the underlying application
     *             does not respond correctly
     * @throws ServiceNotPublishedException
     *             if the service is currently not published on any marketplace
     * @throws OperationPendingException
     *             if another conflicting request is pending
     * @throws ConcurrentModificationException
     *             if the service has been published to a different marketplace
     *             concurrently
     * 
     * @see ServiceStatus
     * @see #deactivateService(VOService)
     * 
     */

    public VOService activateService(VOService service)
            throws ObjectNotFoundException, ServiceStateException,
            OrganizationAuthoritiesException, OperationNotPermittedException,
            ServiceOperationException, TechnicalServiceNotAliveException,
            ServiceNotPublishedException, OperationPendingException,
            ConcurrentModificationException;

    /**
     * Deactivates a marketable service. The service status is set to
     * {@link ServiceStatus#INACTIVE}.
     * <p>
     * A service must be deactivated, for example, to modify its definition. To
     * make the service visible to customers again, you must re-activate it
     * explicitly.
     * <p>
     * Required role: service manager of the supplier organization that owns the
     * service, or broker or reseller of an authorized broker or reseller
     * organization
     * 
     * @return the deactivated service, or <code>null</code> if the operation
     *         was suspended
     * 
     * @see ServiceStatus
     * @see #activateService(VOService)
     * 
     * @param service
     *            the service to deactivate
     * @throws ObjectNotFoundException
     *             if the service is not found
     * @throws ServiceStateException
     *             if the service has a status in which it cannot be deactivated
     * @throws OrganizationAuthoritiesException
     *             if the calling user's organization does not have the
     *             supplier, broker, or reseller role
     * @throws OperationNotPermittedException
     *             if the calling user's organization is neither the supplier of
     *             the service nor an authorized broker or reseller
     * @throws ServiceOperationException
     *             if the service is used by a subscription, i.e. the service is
     *             a subscription-specific copy of a marketable service
     * @throws OperationPendingException
     *             if another conflicting request is pending
     * @throws ConcurrentModificationException
     *             if the service has been published to a different marketplace
     *             concurrently
     */

    public VOService deactivateService(VOService service)
            throws ObjectNotFoundException, ServiceStateException,
            OrganizationAuthoritiesException, OperationNotPermittedException,
            ServiceOperationException, OperationPendingException,
            ConcurrentModificationException;

    /**
     * Updates the activation statuses for a given list of marketable services,
     * including the services' visibility on their marketplace. For every
     * service which has been activated, the status is set to
     * {@link ServiceStatus#ACTIVE}; otherwise, the status is set to
     * {@link ServiceStatus#INACTIVE}. The visibility on a marketplace can be
     * controlled for every service by setting the "visible in catalog" flag
     * accordingly at the corresponding catalog entry.
     * <p>
     * Before you can activate a service, a price model must have been defined
     * for it. An active service cannot be modified. Therefore, it must be
     * deactivated explicitly before an update and reactivated afterwards.
     * <p>
     * Required role: service manager of the supplier organization that owns the
     * services, or broker or reseller of an authorized broker or reseller
     * organization
     * 
     * @see ServiceStatus
     * @see #activateService(VOService)
     * @see #deactivateService(VOService)
     * 
     * @param activations
     *            the list of <code>VOServiceActivation</code> objects
     *            specifying the services for which to update the activation
     *            status
     * @return the list of activated or deactivated services. Services for which
     *         the activation or deactivation has been suspended are not
     *         included.
     * 
     * @throws ObjectNotFoundException
     *             if a service is not found
     * @throws ServiceStateException
     *             if a service has a status in which it cannot be activated
     * @throws OrganizationAuthoritiesException
     *             if the calling user's organization does not have the
     *             supplier, broker, or reseller role
     * @throws OperationNotPermittedException
     *             if the calling user's organization is neither the supplier of
     *             a service in the list nor an authorized broker or reseller
     * @throws ServiceOperationException
     *             if no price model is defined for a service
     * @throws ServiceNotPublishedException
     *             if the service is currently not published on any marketplace
     * @throws TechnicalServiceNotAliveException
     *             if the provisioning service of the application underlying to
     *             a service does not respond correctly
     * @throws OperationPendingException
     *             if another conflicting request is pending
     * @throws ConcurrentModificationException
     *             if the service has been published to a different marketplace
     *             concurrently
     */

    public List<VOService> setActivationStates(
            List<VOServiceActivation> activations)
            throws ObjectNotFoundException, ServiceStateException,
            OrganizationAuthoritiesException, OperationNotPermittedException,
            ServiceOperationException, TechnicalServiceNotAliveException,
            ServiceNotPublishedException, OperationPendingException,
            ConcurrentModificationException;

    /**
     * Returns the localized texts for a marketable service.
     * <p>
     * Required role: none
     * 
     * @param service
     *            the service to get the localized texts for
     * @return a <code>VOServiceLocalization</code> object with the localized
     *         texts
     * @throws ObjectNotFoundException
     *             if the service is not found
     * @throws OperationNotPermittedException
     *             if the calling user does not have read access to the service
     */

    public VOServiceLocalization getServiceLocalization(VOService service)
            throws ObjectNotFoundException, OperationNotPermittedException;

    /**
     * Saves the localized texts for a marketable service.
     * <p>
     * Required role: service manager of the supplier organization that owns the
     * service, or reseller of an authorized reseller organization
     * 
     * @param service
     *            the service for which the localized texts are to be saved
     * @param localization
     *            a <code>VOServiceLocalization</code> object with the localized
     *            texts to save
     * @throws ObjectNotFoundException
     *             if the service is not found
     * @throws OperationNotPermittedException
     *             if the calling user does not have write access to the service
     * @throws ValidationException
     *             if one of the localized texts is longer than 100 bytes
     * @throws ConcurrentModificationException
     *             if the stored service or texts are changed by another user in
     *             the time between reading and writing them
     */

    public void saveServiceLocalization(VOService service,
            VOServiceLocalization localization) throws ObjectNotFoundException,
            OperationNotPermittedException, ValidationException,
            ConcurrentModificationException;

    /**
     * Returns the localized texts for a price model.
     * <p>
     * Required role: none
     * 
     * @param priceModel
     *            the price model to get the localized texts for
     * @return a <code>VOPriceModelLocalization</code> object with the localized
     *         texts
     * @throws ObjectNotFoundException
     *             if the price model is not found
     * @throws OperationNotPermittedException
     *             if the calling user does not have read access to the price
     *             model
     */

    public VOPriceModelLocalization getPriceModelLocalization(
            VOPriceModel priceModel) throws ObjectNotFoundException,
            OperationNotPermittedException;

    /**
     * Returns the localized license texts for the technical service on which
     * the specified marketable service is based. The texts can then be used as
     * templates for defining the localized license texts of a price model.
     * <p>
     * Required role: none
     * 
     * @param service
     *            the marketable service for whose technical service the license
     *            texts are to be returned
     * @throws ObjectNotFoundException
     *             if the service is not found
     * @throws OperationNotPermittedException
     *             if the calling user does not have read access to the service
     * @return a list of the localized license texts defined for the technical
     *         service
     */

    public List<VOLocalizedText> getPriceModelLicenseTemplateLocalization(
            VOServiceDetails service) throws ObjectNotFoundException,
            OperationNotPermittedException;

    /**
     * Saves the localized texts for a price model.
     * <p>
     * Required role: service manager of the supplier organization that owns the
     * price model
     * 
     * @param priceModel
     *            the price model for which the localized texts are to be saved
     * @param localization
     *            a <code>VOPriceModelLocalization</code> object with the
     *            localized texts to save
     * @throws ObjectNotFoundException
     *             if the price model is not found
     * @throws OperationNotPermittedException
     *             if the calling user does not have write access to the price
     *             model
     * @throws ConcurrentModificationException
     *             if the stored price model or texts are changed by another
     *             user in the time between reading and writing them
     */

    public void savePriceModelLocalization(VOPriceModel priceModel,
            VOPriceModelLocalization localization)
            throws ObjectNotFoundException, OperationNotPermittedException,
            ConcurrentModificationException;

    /**
     * Retrieves a list of the customer-specific services which are defined for
     * the given customer and supplied by the calling user's organization.
     * Customer-specific services are copies of defined marketable services.
     * Such a copy is created when a supplier defines a customer-specific price
     * model for a marketable service.
     * <p>
     * Required role: service manager of a supplier organization
     * 
     * @param customer
     *            the customer to get the customer-specific services for
     * @return the list of services
     * @throws ObjectNotFoundException
     *             if the customer organization is not found
     * @throws OperationNotPermittedException
     *             if the specified organization is not a customer of the
     *             supplier organization the calling user is a member of
     */

    public List<VOService> getServicesForCustomer(VOOrganization customer)
            throws ObjectNotFoundException, OperationNotPermittedException;

    /**
     * Returns all currencies which are currently supported.
     * <p>
     * Required role: none
     * 
     * @return the supported currencies
     */

    public List<String> getSupportedCurrencies();

    /**
     * Loads the image of the specified service.
     * <p>
     * Required role: none
     * 
     * @param serviceKey
     *            the numeric key of the service
     * @return a <code>VOImageResource</code> object, or <code>null</code> if no
     *         image is found
     */

    public VOImageResource loadImage(Long serviceKey);

    /**
     * Loads the image of the given service which the specified supplier has
     * defined for the marketplace associated with the service.
     * <p>
     * Required role: none
     * 
     * @param serviceId
     *            the ID of the service
     * @param supplierId
     *            the ID of the supplier
     * @return a <code>VOImageResource</code> object, or <code>null</code> if no
     *         image is found
     * @throws ObjectNotFoundException
     *             if the supplier is not found
     */

    public VOImageResource loadImageForSupplier(String serviceId,
            String supplierId) throws ObjectNotFoundException;

    /**
     * Exports the definition of the specified technical services to a byte
     * array in XML format.
     * <p>
     * Required role: technology manager of the technology provider organization
     * that owns the services
     * 
     * @param technicalServices
     *            the list of services to export
     * @return the XML data
     * @throws OrganizationAuthoritiesException
     *             if the calling user's organization does not have the
     *             technology provider role
     * @throws ObjectNotFoundException
     *             if one of the technical services is not found
     * @throws OperationNotPermittedException
     *             if the calling user's organization is not the provider of all
     *             the technical services
     */

    public byte[] exportTechnicalServices(
            List<VOTechnicalService> technicalServices)
            throws OrganizationAuthoritiesException, ObjectNotFoundException,
            OperationNotPermittedException;

    /**
     * Creates a technical service without any events, parameters, roles,
     * operations, or attributes.
     * <p>
     * Required role: technology manager of a technology provider organization
     * 
     * @param technicalService
     *            the value object specifying the technical service to create
     * @return a value object with the definition of the new technical service
     * @throws OrganizationAuthoritiesException
     *             if the calling user's organization does not have the
     *             technology provider role
     * @throws ValidationException
     *             if the validation of the value object for the new service
     *             fails
     * @throws NonUniqueBusinessKeyException
     *             if a technical service with the given identifier and version
     *             is already provided by the calling user's organization
     */

    public VOTechnicalService createTechnicalService(
            VOTechnicalService technicalService)
            throws OrganizationAuthoritiesException, ValidationException,
            NonUniqueBusinessKeyException;

    /**
     * Saves the localizable attributes of the given technical service as well
     * as its events, tags, parameters, and options in the locale that is
     * configured for the calling user.
     * <p>
     * Required role: technology manager of the technology provider organization
     * that owns the service
     * 
     * @param technicalService
     *            the value object with the definition of the technical service
     * @throws OrganizationAuthoritiesException
     *             if the calling user's organization does not have the
     *             technology provider role
     * @throws ObjectNotFoundException
     *             if the technical service or one of the related entities is
     *             not found
     * @throws OperationNotPermittedException
     *             if the calling user's organization is not the provider of the
     *             technical service
     * @throws UpdateConstraintException
     *             if the license information is to be changed, but the
     *             technical service is in use, i.e. marketable services based
     *             on it exist
     * @throws ValidationException
     *             if a role definition or operation name is too long
     */

    public void saveTechnicalServiceLocalization(
            VOTechnicalService technicalService)
            throws OrganizationAuthoritiesException, ObjectNotFoundException,
            OperationNotPermittedException, UpdateConstraintException,
            ValidationException;

    /**
     * Returns a list of all customer-specific services provided by the calling
     * user's organization. Customer-specific services are copies of defined
     * marketable services. Such a copy is created when a supplier defines a
     * customer-specific price model for a marketable service.
     * <p>
     * Required role: service manager of a supplier organization, broker of a
     * broker organization, or reseller of a reseller organization
     * 
     * @return the list of customer-specific services
     * @throws OrganizationAuthoritiesException
     *             if the calling user's organization does not have the
     *             supplier, broker, or reseller role
     */

    public List<VOCustomerService> getAllCustomerSpecificServices()
            throws OrganizationAuthoritiesException;

    /**
     * Copies the specified marketable service and saves the copy with the given
     * service ID. An existing price model and localized resources are included
     * in the copy. The original service remains unchanged.
     * <p>
     * Required role: service manager of the supplier organization that owns the
     * service
     * 
     * @param service
     *            the service to copy
     * @param serviceId
     *            The ID for the new service
     * @return a value object with the definition of the new service
     * @throws ObjectNotFoundException
     *             if the service to copy is not found
     * @throws OrganizationAuthoritiesException
     *             if the calling user's organization does not have the supplier
     *             role
     * @throws OperationNotPermittedException
     *             if the calling user is not allowed to access the service to
     *             be copied
     * @throws ServiceStateException
     *             if the service status is not {@link ServiceStatus#ACTIVE} or
     *             {@link ServiceStatus#INACTIVE}
     * @throws ConcurrentModificationException
     *             if the stored service is changed by another user in the time
     *             between reading it and creating the copy
     * @throws NonUniqueBusinessKeyException
     *             if a service with the specified ID already exists
     * @throws ValidationException
     *             if the service ID is invalid
     */

    public VOServiceDetails copyService(VOService service, String serviceId)
            throws ObjectNotFoundException, OrganizationAuthoritiesException,
            OperationNotPermittedException, ServiceStateException,
            ConcurrentModificationException, NonUniqueBusinessKeyException,
            ValidationException;

    /**
     * Retrieves the marketable services the calling user's organization can
     * subscribe to in the context of the given marketplace.
     * <p>
     * Required role: none
     * 
     * @param marketplaceId
     *            the ID of the marketplace
     * 
     * @return the list of services
     */

    public List<VOService> getServicesForMarketplace(String marketplaceId);

    /**
     * Retrieves the marketable services which are based on the same technical
     * service as the specified service and available to the calling user on the
     * given marketplace. The result list depends on the services' status and on
     * whether the calling user is logged in or performs an anonymous access.
     * <p>
     * Required role: none
     * 
     * @param service
     *            the service for which the related services are to be retrieved
     * @param marketplaceId
     *            the ID of the marketplace to which the services have been
     *            published
     * @param locale
     *            the language in which to retrieve the service details. This
     *            can be <code>null</code>, if the calling user is logged in and
     *            his current locale is to be used. For anonymous access, the
     *            locale is mandatory. Specify a language code as returned by
     *            <code>getLanguage()</code> of <code>java.util.Locale</code>.
     * @return the list of services
     * @throws ObjectNotFoundException
     *             if the specified service is not found
     */

    public List<VOService> getRelatedServicesForMarketplace(VOService service,
            String marketplaceId, String locale) throws ObjectNotFoundException;

    /**
     * Retrieves the marketable service which is identified by the specified key
     * and visible on the given marketplace.
     * <p>
     * Depending on the calling user, the checks described below are performed.
     * If one of the conditions is not fulfilled, the method returns
     * <code>null</code>.
     * <p>
     * If the calling user is an anonymous user who is not logged in, the method
     * returns the requested service if:
     * <ul>
     * <li>the service is a not a customer-specific service</li>
     * <li>the service is active</li>
     * <li>the service is visible on the specified marketplace</li>
     * <li>the service is visible for anonymous users</li>
     * </ul>
     * <p>
     * If the calling user is logged in and requests a customer-specific service
     * which has been created for his customer organization, the method returns
     * the service if:
     * <ul>
     * <li>the service is active</li>
     * <li>the marketable service from which the customer-specific service was
     * copied is published on the specified marketplace</li>
     * </ul>
     * <p>
     * If the calling user is logged in and requests a service which is not a
     * customer-specific copy for his customer organization, the method returns
     * the service if:
     * <ul>
     * <li>the service has been published on the given marketplace</li>
     * <li>requested service is active</li>
     * <li>no active customer-specific copy exists for it. If an active
     * customer-specific copy of the service exists, this is returned instead.</li>
     * </ul>
     * <p>
     * Required role: none
     * 
     * @param serviceKey
     *            the numeric key of the service to be retrieved
     * @param marketplaceId
     *            the ID of the marketplace on which the requested service has
     *            been published
     * @param locale
     *            the language in which to retrieve the service details. This
     *            can be <code>null</code>, if the calling user is logged in and
     *            his current locale is to be used. For anonymous access, the
     *            locale is mandatory. Specify a language code as returned by
     *            <code>getLanguage()</code> of <code>java.util.Locale</code>.
     * @return the service which is visible on the specified marketplace. The
     *         <code>isSubscriptionLimitReached</code> property is set if the
     *         method is called by a logged in user. The property is set to
     *         <code>true</code> if a subscription exists for the service and
     *         the calling user's organization, and the underlying technical
     *         service allows for one subscription only.
     * @throws ObjectNotFoundException
     *             if the service is not found
     * @throws OperationNotPermittedException
     *             if the specified service is a copy for a subscription, if
     *             anonymous access to a customer-specific service is attempted,
     *             or if the calling user is logged in but tries to access a
     *             customer-specific service created for an organization other
     *             than his own
     */

    public VOServiceEntry getServiceForMarketplace(Long serviceKey,
            String marketplaceId, String locale)
            throws ObjectNotFoundException, OperationNotPermittedException;

    /**
     * Returns the instance IDs for the subscriptions to the marketable services
     * which are offered by the specified organizations and based on technical
     * services provided by the calling user's organization.
     * <p>
     * Required role: technology manager of a technology provider organization
     * 
     * @param organizationIds
     *            the IDs of the organizations offering the services
     * @return the list of instance IDs
     */

    public List<String> getInstanceIdsForSellers(List<String> organizationIds);

    /**
     * Deactivates the specified service on its associated marketplace so that
     * it is no longer available for subscription. This may be necessary if the
     * service violates existing regulations (e.g. its description contains law
     * or license violations). The service status is set to
     * {@link ServiceStatus#SUSPENDED}.
     * <p>
     * Required role: marketplace manager of the marketplace owner organization
     * 
     * @param service
     *            the service to deactivate
     * @param reason
     *            the reason why the service is deactivated
     * @return the updated service
     * @throws ObjectNotFoundException
     *             if the service is not found
     * @throws OperationNotPermittedException
     *             if the service is not published on a marketplace owned by the
     *             calling user's organization, or if the service is a
     *             subscription-specific copy of a marketable service
     * @throws ServiceStateException
     *             if the service status is not {@link ServiceStatus#ACTIVE}
     */

    public VOService suspendService(VOService service, String reason)
            throws ObjectNotFoundException, OperationNotPermittedException,
            ServiceStateException;

    /**
     * Reactivates a deactivated service on its associated marketplace so that
     * it is available again for subscription. The service status is set to
     * {@link ServiceStatus#ACTIVE}.
     * <p>
     * Required role: marketplace manager of the marketplace owner organization
     * 
     * @param service
     *            the service to reactivate
     * @return the updated service
     * @throws ObjectNotFoundException
     *             if the service is not found
     * @throws OperationNotPermittedException
     *             if the service is not published on a marketplace owned by the
     *             calling user's organization, or if the service is a
     *             subscription-specific copy of a marketable service
     * @throws ServiceStateException
     *             if the service status is not {@link ServiceStatus#SUSPENDED}
     */

    public VOService resumeService(VOService service)
            throws ObjectNotFoundException, OperationNotPermittedException,
            ServiceStateException;

    /**
     * Checks if the specified service is part of an upgrade path. This is the
     * case if a subscription to the given service can be upgraded or downgraded
     * to another service, or if a subscription to another service can be
     * upgraded or downgraded to the given service.
     * <p>
     * Required role: service manager of the supplier organization that owns the
     * service
     * 
     * @param service
     *            the marketable service to check
     * @return <code>true</code> if the service is part of an upgrade path,
     *         <code>false</code> otherwise
     * @throws ObjectNotFoundException
     *             if the service is not found
     * @throws OperationNotPermittedException
     *             if the service is not owned by the calling user's
     *             organization, or if the service is a subscription-specific
     *             copy of a marketable service
     */

    public boolean isPartOfUpgradePath(VOService service)
            throws ObjectNotFoundException, OperationNotPermittedException;

    /**
     * Returns the marketable services which are already included in or can be
     * added to the list of services to which a subscription to the given
     * service can be upgraded or downgraded. For the services which are already
     * included in the list, <code>VOCompatibleService#isCompatible()</code>
     * returns <code>true</code>. Otherwise, the method returns
     * <code>false</code>.
     * <p>
     * Required role: service manager of the supplier organization that owns the
     * service
     * 
     * @param service
     *            the service to get the compatible services for
     * @return the list of actually or potentially compatible services
     * @throws ObjectNotFoundException
     *             if the service is not found
     * @throws OperationNotPermittedException
     *             if the service is not owned by the calling user's
     *             organization
     */

    public List<VOCompatibleService> getPotentialCompatibleServices(
            VOService service) throws ObjectNotFoundException,
            OperationNotPermittedException;

    /**
     * Returns the supplier, broker, or reseller organization that provides a
     * marketable service. If the organization description is empty for de or
     * jp, then return the en description.
     * <p>
     * Required role: none
     * 
     * @param serviceKey
     *            the numeric key of the service to get the seller organization
     *            for
     * @param locale
     *            the language in which the seller information is to be
     *            returned. Specify a language code as returned by
     *            <code>getLanguage()</code> of <code>java.util.Locale</code>.
     * @return the seller organization for the given service
     * @throws ObjectNotFoundException
     *             if the service is not found
     */
    VOOrganization getServiceSellerFallback(long serviceKey, String locale)
            throws ObjectNotFoundException;

    /**
     * Validates if subscription can be processed.
     * 
     * @param service
     *            details
     * @return subscription details (stored in the database)
     * @throws OperationNotPermittedException
     *             if the calling user's organization is not allowed to access
     *             the subscription
     * @throws SubscriptionStateException
     *             if the subscription is EXPIRED, INVALID or DEACTIVATED
     * @throws ObjectNotFoundException
     *             if the product is not found
     */
    VOSubscriptionDetails validateSubscription(VOService service)
            throws OperationNotPermittedException, SubscriptionStateException,
            ObjectNotFoundException;

    /**
     * Returns a list of all customer-specific services provided by the calling
     * user's organization. Customer-specific services are copies of defined
     * marketable services. Such a copy is created when a supplier defines a
     * customer-specific price model for a marketable service.
     * 
     * @return the list of customer-specific services
     * @throws ObjectNotFoundException
     * @throws OperationNotPermittedException
     */

    public List<VOCustomerService> getServiceCustomerTemplates(VOService service)
            throws ObjectNotFoundException, OperationNotPermittedException;

    /**
     * Delegate to delete a service by its key.
     * 
     * @param key
     *            the technical key
     * @throws ConcurrentModificationException
     * @throws ServiceStateException
     * @throws ServiceOperationException
     * @throws OperationNotPermittedException
     * @throws OrganizationAuthoritiesException
     * @throws ObjectNotFoundException
     */
    public void deleteService(Long key) throws ObjectNotFoundException,
            OrganizationAuthoritiesException, OperationNotPermittedException,
            ServiceOperationException, ServiceStateException,
            ConcurrentModificationException;

    /**
     * Delegate to delete a technical service by its key.
     * 
     * @param key
     *            the technical key
     * @throws OperationNotPermittedException
     * @throws ObjectNotFoundException
     * @throws ConcurrentModificationException
     * @throws OrganizationAuthoritiesException
     * @throws DeletionConstraintException
     */
    public void deleteTechnicalService(Long key)
            throws ObjectNotFoundException, OperationNotPermittedException,
            DeletionConstraintException, OrganizationAuthoritiesException,
            ConcurrentModificationException;

}
