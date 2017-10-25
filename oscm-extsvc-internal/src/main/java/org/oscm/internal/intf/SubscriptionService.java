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
import java.util.UUID;

import javax.ejb.Remote;

import org.oscm.internal.types.enumtypes.OperationStatus;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.MailOperationException;
import org.oscm.internal.types.exception.MandatoryCustomerUdaMissingException;
import org.oscm.internal.types.exception.MandatoryUdaMissingException;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.OperationPendingException;
import org.oscm.internal.types.exception.OperationStateException;
import org.oscm.internal.types.exception.OrganizationAuthoritiesException;
import org.oscm.internal.types.exception.PaymentDataException;
import org.oscm.internal.types.exception.PaymentInformationException;
import org.oscm.internal.types.exception.PriceModelException;
import org.oscm.internal.types.exception.ServiceChangedException;
import org.oscm.internal.types.exception.ServiceParameterException;
import org.oscm.internal.types.exception.SubscriptionAlreadyExistsException;
import org.oscm.internal.types.exception.SubscriptionMigrationException;
import org.oscm.internal.types.exception.SubscriptionStateException;
import org.oscm.internal.types.exception.SubscriptionStillActiveException;
import org.oscm.internal.types.exception.TechnicalServiceNotAliveException;
import org.oscm.internal.types.exception.TechnicalServiceOperationException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOBillingContact;
import org.oscm.internal.vo.VOInstanceInfo;
import org.oscm.internal.vo.VOLocalizedText;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.internal.vo.VOParameter;
import org.oscm.internal.vo.VOPaymentInfo;
import org.oscm.internal.vo.VORoleDefinition;
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOServiceOperationParameter;
import org.oscm.internal.vo.VOServiceOperationParameterValues;
import org.oscm.internal.vo.VOSubscription;
import org.oscm.internal.vo.VOSubscriptionDetails;
import org.oscm.internal.vo.VOSubscriptionIdAndOrganizations;
import org.oscm.internal.vo.VOTechnicalServiceOperation;
import org.oscm.internal.vo.VOUda;
import org.oscm.internal.vo.VOUsageLicense;
import org.oscm.internal.vo.VOUser;
import org.oscm.internal.vo.VOUserSubscription;

/**
 * Remote interface of the subscription management service.
 */
@Remote
public interface SubscriptionService {

    /**
     * Creates a subscription to the given service for the calling user's
     * organization.
     * <p>
     * Usage licenses are created for the given users. The price model is copied
     * from the service and assigned directly to the subscription. In this way,
     * changes can be made to the price model which are specific to this
     * subscription only.
     * <p>
     * In the <code>VOSubscription</code> object, you can specify an activation
     * date and time for the subscription. It is ensured that users cannot
     * access subscriptions which have not been activated.
     * <p>
     * Required role: administrator of an organization
     * 
     * @param subscription
     *            a value object specifying the subscription identifier, which
     *            must be unique for the organization, and optionally a customer
     *            reference number
     * @param service
     *            the service to subscribe to
     * @param users
     *            the users to be assigned to the subscription, including their
     *            service roles
     * @param paymentInfo
     *            a value object specifying the payment information to be used
     *            for the subscription, or <code>null</code> if the service is
     *            free of charge. In the case when HIDE_PAYMENT_INFORMATION
     *            setting equals TRUE and the service is not free of charge,
     *            Invoice is used as default payment type
     * @param billingContact
     *            the billing contact to be assigned to the subscription, if the
     *            service is not free of charge. In the case when
     *            HIDE_PAYMENT_INFORMATION setting equals TRUE and the service
     *            is not free of charge, the organization address is used as
     *            billingContact
     * @param udas
     *            a list of custom attributes to set
     * @return the saved subscription, or <code>null</code> if the operation was
     *         suspended
     * @throws ObjectNotFoundException
     *             if a given user or the service is not found
     * @throws NonUniqueBusinessKeyException
     *             if the subscription identifier is not unique within the
     *             organization
     * @throws ValidationException
     *             if the validation of a value object fails or a custom
     *             attribute is invalid
     * @throws PaymentInformationException
     *             when trying to subscribe to a service that is not free of
     *             charge without specifying any payment information
     * @throws ServiceParameterException
     *             if the user count for the subscription is greater than the
     *             value of the <code>NAMED_USER</code> parameter of the
     *             underlying technical service
     * @throws ServiceChangedException
     *             if the service was changed between retrieving its details and
     *             subscribing to it
     * @throws PriceModelException
     *             if no price model is defined for the service
     * @throws TechnicalServiceNotAliveException
     *             if the underlying technical service cannot be reached
     * @throws TechnicalServiceOperationException
     *             if a technical operation related to the subscription process
     *             fails
     * @throws OperationNotPermittedException
     *             when trying to subscribe to a customer-specific service of
     *             another organization
     * @throws SubscriptionAlreadyExistsException
     *             if the calling user's organization already subscribed to the
     *             service and the underlying technical service allows for only
     *             one subscription per organization
     * @throws OperationPendingException
     *             if another request to create a subscription with the same
     *             identifier is pending
     * @throws MandatoryUdaMissingException
     *             if a mandatory subscription attribute is missing
     * @throws ConcurrentModificationException
     *             if an existing custom attribute is changed by another user in
     *             the time between reading and writing it
     * @throws SubscriptionStateException
     *             if the subscription status does not allow for the execution
     *             of this method
     * @throws MandatoryCustomerUdaMissingException
     *             if a mandatory customer attribute is missing
     */

    VOSubscription subscribeToService(VOSubscription subscription,
            VOService service, List<VOUsageLicense> users,
            VOPaymentInfo paymentInfo, VOBillingContact billingContact,
            List<VOUda> udas)
            throws ObjectNotFoundException, NonUniqueBusinessKeyException,
            ValidationException, PaymentInformationException,
            ServiceParameterException, ServiceChangedException,
            PriceModelException, TechnicalServiceNotAliveException,
            TechnicalServiceOperationException, OperationNotPermittedException,
            SubscriptionAlreadyExistsException, OperationPendingException,
            MandatoryUdaMissingException, ConcurrentModificationException,
            SubscriptionStateException, MandatoryCustomerUdaMissingException;

    /**
     * Assigns and/or removes users to/from a subscription.
     * <p>
     * If the operation fails for one of the users, the whole change is rolled
     * back, i.e. no user is added or removed. If users to be added are already
     * assigned to the subscription, only their roles are updated, if required.
     * Users who are included in both lists are added and removed in one and the
     * same transaction.
     * <p>
     * Required role: administrator of the organization that owns the
     * subscription
     * 
     * @param subscriptionId
     *            the subscription identifier
     * @param usersToBeAdded
     *            the users to be assigned to the subscription, including their
     *            service roles
     * @param usersToBeRevoked
     *            the users to be deassigned from the subscription
     * @return <code>true</code> if the operation was completed,
     *         <code>false</code> if it was suspended
     * @throws ObjectNotFoundException
     *             if the subscription or a given user is not found
     * @throws ServiceParameterException
     *             if the user count for the subscription is greater than the
     *             value of the <code>NAMED_USER</code> parameter of the
     *             underlying technical service
     * @throws SubscriptionStateException
     *             if the subscription status does not allow for the execution
     *             of this method
     * @throws TechnicalServiceNotAliveException
     *             if the underlying technical service cannot be reached
     * @throws TechnicalServiceOperationException
     *             if a technical operation related to the user assignment or
     *             removal fails
     * @throws OperationNotPermittedException
     *             if service roles to be set are not valid
     * @throws ConcurrentModificationException
     *             if data stored for the given subscription or users is changed
     *             by another user in the time between reading and writing it
     * @throws OperationPendingException
     *             if another conflicting request is pending
     */

    boolean addRevokeUser(String subscriptionId,
            List<VOUsageLicense> usersToBeAdded, List<VOUser> usersToBeRevoked)
            throws ObjectNotFoundException, ServiceParameterException,
            SubscriptionStateException, TechnicalServiceNotAliveException,
            TechnicalServiceOperationException, OperationNotPermittedException,
            ConcurrentModificationException, OperationPendingException;

    /**
     * Retrieves the subscriptions the specified user is assigned to. The list
     * includes subscriptions whose status is <code>ACTIVE</code>,
     * <code>PENDING</code>, <code>SUSPENDED</code>, or <code>EXPIRED</code>.
     * <p>
     * Required role: administrator of the user's organization
     * 
     * @param user
     *            the user for whom the list of subscriptions is to be retrieved
     * @return the list of subscriptions
     * @throws ObjectNotFoundException
     *             if the user is not found
     * @throws OperationNotPermittedException
     *             if the given user's organization differs from that of the
     *             caller
     */

    List<VOUserSubscription> getSubscriptionsForUser(VOUser user)
            throws ObjectNotFoundException, OperationNotPermittedException;

    /**
     * Retrieves the subscriptions the calling user is assigned to. The list
     * includes subscriptions whose status is <code>ACTIVE</code>,
     * <code>PENDING</code>, <code>SUSPENDED</code>, or <code>EXPIRED</code>.
     * <p>
     * Required role: any user role in an organization
     * 
     * @return the list of subscriptions
     */

    List<VOUserSubscription> getSubscriptionsForCurrentUser();

    /**
     * Retrieves all subscriptions of the calling user's organization,
     * regardless of their status.
     * <p>
     * Required role: administrator of the organization
     * 
     * @return the list of subscriptions
     */

    List<VOSubscription> getSubscriptionsForOrganization();

    /**
     * Retrieves the subscriptions of the calling user's organization that have
     * one of the specified statuses.
     * <p>
     * Required role: administrator of the organization
     * 
     * @param requiredStatus
     *            the statuses to use as a filter for the subscriptions. Only
     *            subscriptions that have one of these statuses are included in
     *            the list.
     * @return the list of subscriptions
     */

    List<VOSubscription> getSubscriptionsForOrganizationWithFilter(
            Set<SubscriptionStatus> requiredStatus);

    /**
     * Terminates the given subscription. Since bills may still be open for it,
     * the subscription is not deleted, but marked as <code>DEACTIVATED</code>.
     * <p>
     * Users assigned to the subscription are automatically removed from it. If
     * there are active sessions for the underlying service, the method fails
     * with an exception.
     * <p>
     * The organization administrators and the users assigned to the
     * subscription are informed by email about the termination.
     * <p>
     * Required role: administrator of the organization that owns the
     * subscription
     * 
     * @param subscriptionId
     *            the subscription identifier
     * @return <code>true</code> if the operation was completed,
     *         <code>false</code> if it was suspended
     * @throws ObjectNotFoundException
     *             if the subscription is not found
     * @throws SubscriptionStillActiveException
     *             if there are active sessions for the subscription
     * @throws SubscriptionStateException
     *             if the subscription status does not allow for the execution
     *             of this method
     * @throws TechnicalServiceNotAliveException
     *             if the underlying technical service cannot be reached
     * @throws TechnicalServiceOperationException
     *             if a technical operation related to the termination of the
     *             subscription fails
     * @throws OperationPendingException
     *             if another request to terminate the subscription is pending
     * @throws OperationNotPermittedException
     */

    boolean unsubscribeFromService(String subscriptionId)
            throws ObjectNotFoundException, SubscriptionStillActiveException,
            SubscriptionStateException, TechnicalServiceNotAliveException,
            TechnicalServiceOperationException, OperationPendingException,
            OperationNotPermittedException;

    /**
     * Returns detailed information on the given subscription of the calling
     * user's organization, including user and price model details.
     * <p>
     * Required role: administrator of the organization that owns the
     * subscription
     * 
     * @param subscriptionId
     *            the subscription identifier
     * @return the subscription details
     * @throws ObjectNotFoundException
     *             if the subscription is not found
     * @throws OperationNotPermittedException
     */

    VOSubscriptionDetails getSubscriptionDetails(String subscriptionId)
            throws ObjectNotFoundException, OperationNotPermittedException;

    /**
     * Returns detailed information on the given subscription of the calling
     * user's organization, including user and price model details.
     * <p>
     * Required role: administrator of the organization that owns the
     * subscription
     * 
     * @param subscriptionKey
     *            the subscription key
     * @return the subscription details
     * @throws ObjectNotFoundException
     *             if the subscription is not found
     * @throws OperationNotPermittedException
     */

    VOSubscriptionDetails getSubscriptionDetails(long subscriptionKey)
            throws ObjectNotFoundException, OperationNotPermittedException;

    /**
     * Returns the marketable services the given subscription can be upgraded or
     * downgraded to.
     * <p>
     * Required role: administrator of the organization that owns the
     * subscription
     * 
     * @param subscriptionId
     *            the subscription identifier
     * @return the list of services
     * @throws ObjectNotFoundException
     *             if the subscription is not found
     * @throws OperationNotPermittedException
     *             if the calling user's organization is not the owner of the
     *             subscription
     */

    List<VOService> getUpgradeOptions(String subscriptionId)
            throws ObjectNotFoundException, OperationNotPermittedException;

    /**
     * Returns the marketable services the given subscription can be upgraded or
     * downgraded to.
     * <p>
     * Required role: administrator of the organization that owns the
     * subscription
     * 
     * @param subscriptionKey
     *            the subscription key
     * @return the list of services
     * @throws ObjectNotFoundException
     *             if the subscription is not found
     * @throws OperationNotPermittedException
     *             if the calling user's organization is not the owner of the
     *             subscription
     */

    List<VOService> getUpgradeOptions(long subscriptionKey)
            throws ObjectNotFoundException, OperationNotPermittedException;

    /**
     * Upgrades or downgrades the given subscription to the specified marketable
     * service. Technically, this means subscribing to another marketable
     * service which is based on the same technical service as the
     * subscription's current service. The new service must be in the list of
     * compatible services of the subscription's current service.
     * <p>
     * Changing a subscription to a service that is not free of charge is
     * possible only if valid payment information exists for the organization.
     * <p>
     * Required role: administrator of the organization that owns the
     * subscription
     * 
     * @param subscription
     *            the subscription identifier
     * @param service
     *            the marketable service to which the subscription is to be
     *            upgraded or downgraded; must not be <code>null</code>
     * @param paymentInfo
     *            a value object specifying the payment information to be used
     *            for the subscription after the upgrade or downgrade. This
     *            information is mandatory when upgrading from a service that is
     *            free of charge to a chargeable one. When passing
     *            <code>null</code>, the existing payment information of the
     *            subscription remains unchanged and will again be used after
     *            the upgrade or downgrade. In the case when
     *            HIDE_PAYMENT_INFORMATION setting equals TRUE and the service
     *            is not free of charge, Invoice is used as default payment type
     * @param billingContact
     *            the billing contact to be assigned to the subscription, if the
     *            service is not free of charge. In the case when
     *            HIDE_PAYMENT_INFORMATION setting equals TRUE and the service
     *            is not free of charge, the organization address is used as
     *            billingContact
     * @param udas
     *            a list of custom attributes to set
     * @return the changed subscription, or <code>null</code> if the operation
     *         was suspended
     * @throws ObjectNotFoundException
     *             if the subscription or the new service is not found
     * @throws OperationNotPermittedException
     *             if the calling user's organization is not the owner of the
     *             subscription
     * @throws SubscriptionMigrationException
     *             if the upgrade or downgrade fails, for example, because the
     *             target service is not compatible with the current service
     * @throws PaymentInformationException
     *             when trying to upgrade or downgrade to a service that is not
     *             free of charge without specifying any payment information
     * @throws SubscriptionStateException
     *             if the subscription status does not allow for the execution
     *             of this method
     * @throws ServiceChangedException
     *             if the new service was changed between retrieving its details
     *             and subscribing to it
     * @throws PriceModelException
     *             if no price model is defined for the new service
     * @throws ConcurrentModificationException
     *             if a stored subscription parameter or custom attribute is
     *             changed by another user in the time between reading and
     *             writing it
     * @throws TechnicalServiceNotAliveException
     *             if the service to perform the technical operation cannot be
     *             reached
     * @throws OperationPendingException
     *             if another request to upgrade or downgrade the subscription
     *             is pending
     * @throws MandatoryUdaMissingException
     *             if a mandatory subscription attribute is missing
     * @throws NonUniqueBusinessKeyException
     *             if a custom attribute with the same definition and target
     *             entity already exists
     * @throws ValidationException
     *             if a custom attribute is invalid
     * @throws MandatoryCustomerUdaMissingException
     *             if a mandatory customer attribute is missing
     */

    VOSubscription upgradeSubscription(VOSubscription subscription,
            VOService service, VOPaymentInfo paymentInfo,
            VOBillingContact billingContact, List<VOUda> udas)
            throws ObjectNotFoundException, OperationNotPermittedException,
            SubscriptionMigrationException, PaymentInformationException,
            SubscriptionStateException, ServiceChangedException,
            PriceModelException, ConcurrentModificationException,
            TechnicalServiceNotAliveException, OperationPendingException,
            MandatoryUdaMissingException, NonUniqueBusinessKeyException,
            ValidationException, MandatoryCustomerUdaMissingException;

    /**
     * Modifies the given subscription. In order to rename the subscription, set
     * the new identifier in the <code>VOSubscription</code> object. You can
     * also change parameters and their options defined for the underlying
     * service.
     * <p>
     * Required role: administrator of the organization that owns the
     * subscription
     * 
     * @param subscription
     *            the value object identifying the subscription and specifying
     *            its new identifier, if required
     * @param parameters
     *            the parameters to modify
     * @param udas
     *            a list of custom attributes to set
     * @return the changed subscription
     * @throws NonUniqueBusinessKeyException
     *             if the new subscription identifier is not unique within the
     *             organization
     * @throws ObjectNotFoundException
     *             if the subscription is not found
     * @throws OperationNotPermittedException
     *             if the calling user's organization is not the owner of the
     *             subscription, or if a parameter is to be changed that is not
     *             configurable or part of the subscription's underlying service
     * @throws ValidationException
     *             if the given identifier is not valid (empty or too long) or
     *             if a custom attribute is invalid
     * @throws SubscriptionMigrationException
     *             if the underlying technical service cannot be notified about
     *             changes in parameters
     * @throws ConcurrentModificationException
     *             if a stored subscription parameter or custom attribute is
     *             changed by another user in the time between reading and
     *             writing it
     * @throws TechnicalServiceNotAliveException
     *             if the service to perform the technical operation cannot be
     *             reached
     * @throws OperationPendingException
     *             if another request to modify the subscription is pending
     * @throws MandatoryUdaMissingException
     *             if a mandatory subscription attribute is missing
     * @throws SubscriptionStateException
     *             if modification not allowed on current subscription state
     * @throws MandatoryCustomerUdaMissingException
     *             if a mandatory customer attribute is missing
     */

    VOSubscriptionDetails modifySubscription(VOSubscription subscription,
            List<VOParameter> parameters, List<VOUda> udas)
            throws NonUniqueBusinessKeyException, ObjectNotFoundException,
            OperationNotPermittedException, ValidationException,
            SubscriptionMigrationException, ConcurrentModificationException,
            TechnicalServiceNotAliveException, OperationPendingException,
            MandatoryUdaMissingException, SubscriptionStateException,
            MandatoryCustomerUdaMissingException;

    /**
     * Completes the subscription process for a subscription to a service with
     * asynchronous tenant provisioning. The instance ID of the relevant
     * application instance and the {@link SubscriptionStatus#ACTIVE} status are
     * set for the subscription. The application instance is notified about the
     * assigned users, and the users are notified about the subscription
     * activation.
     * <p>
     * Required role: technology manager of the technology provider organization
     * that provides the application and technical service
     * 
     * @param subscriptionId
     *            the subscription identifier as specified when the creation of
     *            the application instance was triggered
     * @param organizationId
     *            the customer identifier as specified when the creation of the
     *            application instance was triggered
     * @param instance
     *            the value object containing the information needed to access
     *            the application instance
     * @throws ObjectNotFoundException
     *             if the organization or subscription is not found
     * @throws SubscriptionStateException
     *             if the subscription status is not
     *             {@link SubscriptionStatus#PENDING}
     * @throws TechnicalServiceNotAliveException
     *             if the underlying technical service cannot be reached
     * @throws TechnicalServiceOperationException
     *             if a technical operation related to the subscription process
     *             fails
     * @throws OrganizationAuthoritiesException
     *             if the calling user's organization does not have the
     *             technology provider role
     * @throws OperationNotPermittedException
     *             if the calling user's organization is not the provider of the
     *             application and technical service
     * @throws ValidationException
     *             if the product instance ID already exist when complete
     *             subscription process
     */

    void completeAsyncSubscription(String subscriptionId, String organizationId,
            VOInstanceInfo instance) throws ObjectNotFoundException,
            SubscriptionStateException, TechnicalServiceNotAliveException,
            TechnicalServiceOperationException,
            OrganizationAuthoritiesException, OperationNotPermittedException,
            ValidationException;

    /**
     * Aborts the subscription process for a subscription to a service with
     * asynchronous tenant provisioning, if the application instance cannot be
     * provided. The subscription status is set to
     * {@link SubscriptionStatus#INVALID}. The administrators of the technology
     * provider organization are notified by email.
     * <p>
     * Required role: technology manager of the technology provider organization
     * that provides the application and technical service
     * 
     * @param subscriptionId
     *            the subscription identifier as specified when the creation of
     *            the application instance was triggered
     * @param organizationId
     *            the customer identifier as specified when the creation of the
     *            application instance was triggered
     * @param reason
     *            information on why the instance creation was aborted. The
     *            information can be provided for different locales.
     * @throws ObjectNotFoundException
     *             if the organization or subscription is not found
     * @throws SubscriptionStateException
     *             if the subscription status is not
     *             {@link SubscriptionStatus#PENDING}
     * @throws OrganizationAuthoritiesException
     *             if the calling user's organization does not have the
     *             technology provider role
     * @throws OperationNotPermittedException
     *             if the calling user's organization is not the provider of the
     *             application and technical service
     */

    void abortAsyncSubscription(String subscriptionId, String organizationId,
            List<VOLocalizedText> reason)
            throws ObjectNotFoundException, SubscriptionStateException,
            OrganizationAuthoritiesException, OperationNotPermittedException;

    /**
     * Aborts the subscription process for a subscription to a service with
     * asynchronous tenant provisioning, if the application instance cannot be
     * provided. The subscription status is set to
     * {@link SubscriptionStatus#INVALID}. The administrators of the technology
     * provider organization are notified by email.
     * <p>
     * Required role: technology manager of the technology provider organization
     * that provides the application and technical service
     * 
     * @param subscriptionUUID
     *            the subscription identifier as specified when the creation of
     *            the application instance was triggered
     * @throws ObjectNotFoundException
     *             if the organization or subscription is not found
     * @throws SubscriptionStateException
     *             if the subscription status is not
     *             {@link SubscriptionStatus#PENDING}
     * @throws OrganizationAuthoritiesException
     *             if the calling user's organization does not have the
     *             technology provider role
     * @throws OperationNotPermittedException
     *             if the calling user's organization is not the provider of the
     *             application and technical service
     */

    void abortAsyncSubscription(UUID subscriptionUUID)
            throws ObjectNotFoundException, SubscriptionStateException,
            OrganizationAuthoritiesException, OperationNotPermittedException;

    /**
     * Updates the progress information for a subscription to a service with
     * asynchronous tenant provisioning.
     * <p>
     * Required role: technology manager of the technology provider organization
     * that provides the application and technical service
     * 
     * @param subscriptionId
     *            the subscription identifier as specified when the creation of
     *            the application instance was triggered
     * @param organizationId
     *            the customer identifier as specified when the creation of the
     *            application instance was triggered
     * @param progress
     *            the progress information. The information can be provided for
     *            different locales.
     * @throws ObjectNotFoundException
     *             if the organization or subscription is not found
     * @throws SubscriptionStateException
     *             if the subscription status is not
     *             {@link SubscriptionStatus#PENDING}
     * @throws OrganizationAuthoritiesException
     *             if the calling user's organization does not have the
     *             technology provider role
     * @throws OperationNotPermittedException
     *             if the calling user's organization is not the provider of the
     *             application and technical service
     */

    void updateAsyncSubscriptionProgress(String subscriptionId,
            String organizationId, List<VOLocalizedText> progress)
            throws ObjectNotFoundException, SubscriptionStateException,
            OrganizationAuthoritiesException, OperationNotPermittedException;

    /**
     * Updates the progress information for a subscription to a service with
     * asynchronous tenant provisioning.
     * <p>
     * Required role: technology manager of the technology provider organization
     * that provides the application and technical service
     * 
     * @param subscriptionUUID
     *            the subscription identifier as specified when the creation of
     *            the application instance was triggered
     * @param progress
     *            the progress information.
     * @throws ObjectNotFoundException
     *             if the organization or subscription is not found
     * @throws SubscriptionStateException
     *             if the subscription status is not
     *             {@link SubscriptionStatus#PENDING}
     * @throws OrganizationAuthoritiesException
     *             if the calling user's organization does not have the
     *             technology provider role
     * @throws OperationNotPermittedException
     *             if the calling user's organization is not the provider of the
     *             application and technical service
     */

    void updateAsyncSubscriptionProgress(UUID subscriptionUUID, String progress)
            throws ObjectNotFoundException, SubscriptionStateException,
            OrganizationAuthoritiesException, OperationNotPermittedException;

    /**
     * Completes the subscription modification process with asynchronous tenant
     * provisioning. Calling this method the subscription is set in state
     * SubscriptionStatus.ACTIVE, and the new parameters will be from now used.
     * All customer administrators are informed about the successful update by
     * mail.
     * <p>
     * Required role: technology manager of the technology provider organization
     * that provides the application and technical service
     * 
     * @param subscriptionId
     *            the subscription identifier as specified when the creation of
     *            the application instance was triggered
     * @param organizationId
     *            the customer identifier as specified when the creation of the
     *            application instance was triggered
     * @param instance
     *            the value object containing the information needed to access
     *            the application instance
     * @throws ObjectNotFoundException
     *             if the organization or subscription is not found
     * @throws SubscriptionStateException
     *             if the subscription status is not
     *             {@link SubscriptionStatus#PENDING_UPD} or
     *             {@link SubscriptionStatus#SUSPENDED_UPD}
     * @throws TechnicalServiceNotAliveException
     *             if the underlying technical service cannot be reached
     * @throws TechnicalServiceOperationException
     *             if a technical operation related to the subscription process
     *             fails
     * @throws OrganizationAuthoritiesException
     *             if the calling user's organization does not have the
     *             technology provider role
     * @throws OperationNotPermittedException
     *             if the calling user's organization is not the provider of the
     *             application and technical service
     */

    void completeAsyncModifySubscription(String subscriptionId,
            String organizationId, VOInstanceInfo instance)
            throws ObjectNotFoundException, SubscriptionStateException,
            TechnicalServiceNotAliveException,
            TechnicalServiceOperationException,
            OrganizationAuthoritiesException, OperationNotPermittedException;

    /**
     * Completes the subscription modification process with asynchronous tenant
     * provisioning. Calling this method the subscription is set in state
     * SubscriptionStatus.ACTIVE, and the new parameters will be from now used.
     * All customer administrators are informed about the successful update by
     * mail.
     * <p>
     * Required role: technology manager of the technology provider organization
     * that provides the application and technical service
     * 
     * @param subscriptionUUID
     *            the subscription identifier as specified when the creation of
     *            the application instance was triggered
     * @param instance
     *            the value object containing the information needed to access
     *            the application instance
     * @throws ObjectNotFoundException
     *             if the organization or subscription is not found
     * @throws SubscriptionStateException
     *             if the subscription status is not
     *             {@link SubscriptionStatus#PENDING_UPD} or
     *             {@link SubscriptionStatus#SUSPENDED_UPD}
     * @throws TechnicalServiceNotAliveException
     *             if the underlying technical service cannot be reached
     * @throws TechnicalServiceOperationException
     *             if a technical operation related to the subscription process
     *             fails
     * @throws OrganizationAuthoritiesException
     *             if the calling user's organization does not have the
     *             technology provider role
     * @throws OperationNotPermittedException
     *             if the calling user's organization is not the provider of the
     *             application and technical service
     */

    void completeAsyncModifySubscription(UUID subscriptionUUID,
            VOInstanceInfo instance) throws ObjectNotFoundException,
            SubscriptionStateException, TechnicalServiceNotAliveException,
            TechnicalServiceOperationException,
            OrganizationAuthoritiesException, OperationNotPermittedException;

    /**
     * Completes the subscription upgrade process with asynchronous tenant
     * provisioning. Calling this method the subscription is set in state
     * SubscriptionStatus.ACTIVE, and the new parameters will be from now used.
     * All customer administrators are informed about the successful update by
     * mail.
     * <p>
     * Required role: technology manager of the technology provider organization
     * that provides the application and technical service
     * 
     * @param subscriptionId
     *            the subscription identifier as specified when the creation of
     *            the application instance was triggered
     * @param organizationId
     *            the customer identifier as specified when the creation of the
     *            application instance was triggered
     * @param instance
     *            the value object containing the information needed to access
     *            the application instance
     * @throws ObjectNotFoundException
     *             if the organization or subscription is not found
     * @throws SubscriptionStateException
     *             if the subscription status is not
     *             {@link SubscriptionStatus#PENDING_UPD} or
     *             {@link SubscriptionStatus#SUSPENDED_UPD}
     * @throws TechnicalServiceNotAliveException
     *             if the underlying technical service cannot be reached
     * @throws TechnicalServiceOperationException
     *             if a technical operation related to the subscription process
     *             fails
     * @throws OrganizationAuthoritiesException
     *             if the calling user's organization does not have the
     *             technology provider role
     * @throws OperationNotPermittedException
     *             if the calling user's organization is not the provider of the
     *             application and technical service
     */

    void completeAsyncUpgradeSubscription(String subscriptionId,
            String organizationId, VOInstanceInfo instance)
            throws ObjectNotFoundException, SubscriptionStateException,
            TechnicalServiceNotAliveException,
            TechnicalServiceOperationException,
            OrganizationAuthoritiesException, OperationNotPermittedException;

    /**
     * Aborts the subscription modification process with asynchronous tenant
     * provisioning. Calling this method the subscription is set in state
     * SubscriptionStatus.ACTIVE but the old parameters are used. All
     * administrators of the customer organization as well as the ones of the
     * technology provider organization get notified about the fail by mail.
     * <p>
     * Required role: technology manager of the technology provider organization
     * that provides the application and technical service
     * 
     * @param subscriptionId
     *            the subscription identifier as specified when the creation of
     *            the application instance was triggered
     * @param organizationId
     *            the customer identifier as specified when the creation of the
     *            application instance was triggered
     * @param reason
     *            information on why the instance creation was aborted. The
     *            information can be provided for different locales.
     * @throws ObjectNotFoundException
     *             if the organization or subscription is not found
     * @throws SubscriptionStateException
     *             if the subscription status is not
     *             {@link SubscriptionStatus#PENDING_UPD} or
     *             {@link SubscriptionStatus#SUSPENDED_UPD}
     * @throws OperationNotPermittedException
     *             if the calling user's organization is not the provider of the
     *             application and technical service
     * @throws OrganizationAuthoritiesException
     *             if the calling user's organization does not have the
     *             technology provider role
     * @throws OrganizationAuthoritiesException
     *             if the calling user's organization does not have the
     *             technology provider role
     */

    void abortAsyncModifySubscription(String subscriptionId,
            String organizationId, List<VOLocalizedText> reason)
            throws ObjectNotFoundException, SubscriptionStateException,
            OrganizationAuthoritiesException, OperationNotPermittedException;

    /**
     * Aborts the subscription modification process with asynchronous tenant
     * provisioning. Calling this method the subscription is set in state
     * SubscriptionStatus.ACTIVE but the old parameters are used. All
     * administrators of the customer organization as well as the ones of the
     * technology provider organization get notified about the fail by mail.
     * <p>
     * Required role: technology manager of the technology provider organization
     * that provides the application and technical service
     * 
     * @param subscriptionUUID
     *            the subscription identifier as specified when the creation of
     *            the application instance was triggered
     * @param reason
     *            information on why the instance creation was aborted.
     * @throws ObjectNotFoundException
     *             if the organization or subscription is not found
     * @throws SubscriptionStateException
     *             if the subscription status is not
     *             {@link SubscriptionStatus#PENDING_UPD} or
     *             {@link SubscriptionStatus#SUSPENDED_UPD}
     * @throws OperationNotPermittedException
     *             if the calling user's organization is not the provider of the
     *             application and technical service
     * @throws OrganizationAuthoritiesException
     *             if the calling user's organization does not have the
     *             technology provider role
     * @throws OrganizationAuthoritiesException
     *             if the calling user's organization does not have the
     *             technology provider role
     */

    void abortAsyncModifySubscription(UUID subscriptionUUID,
            String reason)
            throws ObjectNotFoundException, SubscriptionStateException,
            OrganizationAuthoritiesException, OperationNotPermittedException;

    /**
     * Aborts the subscription upgrade process with asynchronous tenant
     * provisioning. Calling this method the subscription is set in state
     * SubscriptionStatus.ACTIVE but the old parameters are used. All
     * administrators of the customer organization as well as the ones of the
     * technology provider organization get notified about the fail by mail.
     * <p>
     * Required role: technology manager of the technology provider organization
     * that provides the application and technical service
     * 
     * @param subscriptionId
     *            the subscription identifier as specified when the creation of
     *            the application instance was triggered
     * @param organizationId
     *            the customer identifier as specified when the creation of the
     *            application instance was triggered
     * @param reason
     *            information on why the instance creation was aborted. The
     *            information can be provided for different locale.
     * @throws ObjectNotFoundException
     *             if the organization or subscription is not found
     * @throws SubscriptionStateException
     *             if the subscription status is not
     *             {@link SubscriptionStatus#PENDING_UPD} or
     *             {@link SubscriptionStatus#SUSPENDED_UPD}
     * @throws OperationNotPermittedException
     *             if the calling user's organization is not the provider of the
     *             application and technical service
     * @throws OrganizationAuthoritiesException
     *             if the calling user's organization does not have the
     *             technology provider role
     * @throws OrganizationAuthoritiesException
     *             if the calling user's organization does not have the
     *             technology provider role
     */

    void abortAsyncUpgradeSubscription(String subscriptionId,
            String organizationId, List<VOLocalizedText> reason)
            throws ObjectNotFoundException, SubscriptionStateException,
            OrganizationAuthoritiesException, OperationNotPermittedException;

    /**
     * Returns the identifiers of all active and pending subscriptions to
     * services supplied by the calling user's organization.
     * <p>
     * Required role: service manager of a supplier organization, broker of a
     * broker organization, or reseller of a reseller organization
     * 
     * @return the list of subscription identifiers
     * @throws OrganizationAuthoritiesException
     *             if the calling user's organization does not have the
     *             supplier, broker, or reseller role
     */

    List<String> getSubscriptionIdentifiers()
            throws OrganizationAuthoritiesException;

    /**
     * Returns the customers which have an active or pending subscription with
     * the given identifier to a service supplied by the calling user's
     * organization.
     * <p>
     * Required role: service manager of a supplier organization, broker of a
     * broker organization, or reseller of a reseller organization
     * 
     * @param subscriptionId
     *            the subscription identifier
     * @return the list of customer organizations
     * @throws OrganizationAuthoritiesException
     *             if the calling user's organization does not have the
     *             supplier, broker, or reseller role
     */

    List<VOOrganization> getCustomersForSubscriptionId(String subscriptionId)
            throws OrganizationAuthoritiesException;

    /**
     * Returns a list of subscription/customer mappings for the services
     * supplied by the calling user's organization.
     * <p>
     * Required role: service manager of a supplier organization, broker of a
     * broker organization, or reseller of a reseller organization
     * 
     * @return the list of subscription identifiers mapped to the relevant
     *         customer organizations
     * @throws OrganizationAuthoritiesException
     *             if the calling user's organization does not have the
     *             supplier, broker, or reseller role
     */

    List<VOSubscriptionIdAndOrganizations> getCustomerSubscriptions()
            throws OrganizationAuthoritiesException;

    /**
     * Returns the details of a given customer's subscription to a service
     * supplied by the calling user's organization.
     * <p>
     * Required role: service manager of a supplier organization, broker of a
     * broker organization, or reseller of a reseller organization
     * 
     * @param organizationId
     *            the ID of the customer organization
     * @param subscriptionId
     *            the identifier of the subscription for which details are to be
     *            retrieved
     * @return the subscription details
     * @throws ObjectNotFoundException
     *             if the organization or subscription is not found
     * @throws OperationNotPermittedException
     *             if the given organization is not a customer of the calling
     *             user's organization
     */

    VOSubscriptionDetails getSubscriptionForCustomer(String organizationId,
            String subscriptionId)
            throws ObjectNotFoundException, OperationNotPermittedException;

    /**
     * Retrieves the service roles defined for the technical service on which
     * the given subscription is based. The service roles can be set for the
     * users assigned to the subscription.
     * <p>
     * Required role: administrator of the organization that owns the
     * subscription
     * 
     * @param subscriptionId
     *            the subscription identifier
     * @return the role definitions, or an empty list if no service roles are
     *         defined
     * @throws ObjectNotFoundException
     *             if the subscription is not found
     * @throws OperationNotPermittedException
     *             if the subscription is not owned by the calling user's
     *             organization
     */

    List<VORoleDefinition> getServiceRolesForSubscription(String subscriptionId)
            throws ObjectNotFoundException, OperationNotPermittedException;

    /**
     * Retrieves the service roles defined for the technical service on which
     * the given subscription is based. The service roles can be set for the
     * users assigned to the subscription.
     * <p>
     * Required role: administrator of the organization that owns the
     * subscription
     * 
     * @param subscriptionKey
     *            the subscription key
     * @return the role definitions, or an empty list if no service roles are
     *         defined
     * @throws ObjectNotFoundException
     *             if the subscription is not found
     * @throws OperationNotPermittedException
     *             if the subscription is not owned by the calling user's
     *             organization
     */

    List<VORoleDefinition> getServiceRolesForSubscription(long subscriptionKey)
            throws ObjectNotFoundException, OperationNotPermittedException;

    /**
     * Retrieves the service roles defined for the technical service on which
     * the given marketable service is based. The service roles can be set for
     * the users who are assigned to subscriptions to the marketable service.
     * <p>
     * Required role: administrator of an organization
     * 
     * @param service
     *            the marketable service
     * @return the role definitions, or an empty list if no service roles are
     *         defined
     * @throws ObjectNotFoundException
     *             if the service is not found
     * @throws OperationNotPermittedException
     *             if the service is not accessible by the calling user's
     *             organization
     */

    List<VORoleDefinition> getServiceRolesForService(VOService service)
            throws ObjectNotFoundException, OperationNotPermittedException;

    /**
     * Executes the specified service operation for the given subscription.
     * <p>
     * Before invoking the service operation, the method checks if:
     * <ul>
     * <li>the subscription belongs to the calling user's organization</li>
     * <li>the operation is defined for the subscription's underlying technical
     * service</li>
     * <li>the calling user is assigned to the subscription</li>
     * </ul>
     * <p>
     * Required role: any user role in an organization
     * 
     * @param subscription
     *            the subscription to execute the service operation for
     * @param operation
     *            the service operation to execute
     * @throws ObjectNotFoundException
     *             if the subscription or service operation is not found
     * @throws OperationNotPermittedException
     *             if the subscription or service operation is not accessible by
     *             the calling user's organization, or if the operation does not
     *             belong to the subscription's underlying technical service
     * @throws TechnicalServiceNotAliveException
     *             if the service to perform the technical operation cannot be
     *             reached
     * @throws TechnicalServiceOperationException
     *             if an exception occurs in the execution of the operation
     * @throws OrganizationAuthoritiesException
     *             if the calling user's organization does not have the customer
     *             role
     * @throws ConcurrentModificationException
     *             if data stored for the subscription or service operation is
     *             changed by another user in the time between reading it and
     *             executing the operation
     * @throws ValidationException
     *             in case for mandatory operation parameters no value is passed
     * @throws SubscriptionStateException
     *             Thrown in case the subscription state does not allow for
     *             executing the given service operation
     * @throws NonUniqueBusinessKeyException
     *             if the transaction id of operation service record already
     *             exists
     */

    void executeServiceOperation(VOSubscription subscription,
            VOTechnicalServiceOperation operation)
            throws ObjectNotFoundException, OperationNotPermittedException,
            TechnicalServiceNotAliveException,
            TechnicalServiceOperationException,
            OrganizationAuthoritiesException, ConcurrentModificationException,
            ValidationException, SubscriptionStateException,
            NonUniqueBusinessKeyException;

    /**
     * Returns a list of {@link VOServiceOperationParameter} that represent the
     * possible values for parameters that require to request their values form
     * the technical service.
     * 
     * @param subscription
     *            the subscription in which context parameter values are
     *            requested
     * @param operation
     *            the operation to get the parameter values for
     * @return the list of parameter values
     * @throws ObjectNotFoundException
     *             in case the subscription or operation wasn found
     * @throws OperationNotPermittedException
     *             in case the callers organization is not the owner of the
     *             subscription or the operation doesn't belong to the technical
     *             service the subscription is based on
     * @throws TechnicalServiceNotAliveException
     *             in case an error occurs while connection to the technical
     *             service.
     * @throws ConcurrentModificationException
     *             in case the operation or the subscription have been changed
     *             concurrently
     * @throws TechnicalServiceOperationException
     *             in case requesting parameter values is not supported
     */
    List<VOServiceOperationParameterValues> getServiceOperationParameterValues(
            VOSubscription subscription, VOTechnicalServiceOperation operation)
            throws ObjectNotFoundException, OperationNotPermittedException,
            TechnicalServiceNotAliveException, ConcurrentModificationException,
            TechnicalServiceOperationException;

    /**
     * Allows a supplier or reseller to terminate a customer subscription.
     * <p>
     * Required role: service manager of the supplier organization that owns the
     * underlying service, or reseller of an authorized reseller organization
     * 
     * @param subscription
     *            the subscription to terminate
     * @param reason
     *            information on why the subscription is terminated
     * @throws ObjectNotFoundException
     *             if the subscription is not found
     * @throws OrganizationAuthoritiesException
     *             if the calling user's organization is not the supplier or
     *             reseller for the subscription.
     * @throws TechnicalServiceNotAliveException
     *             if the underlying technical service cannot be reached
     * @throws TechnicalServiceOperationException
     *             if a technical operation related to the termination of the
     *             subscription fails
     * @throws ConcurrentModificationException
     *             if the stored subscription is changed by another user in the
     *             time between reading and terminating it
     * @throws SubscriptionStateException
     *             if the subscription status does not allow for the execution
     *             of this method
     */

    void terminateSubscription(VOSubscription subscription, String reason)
            throws ObjectNotFoundException, OrganizationAuthoritiesException,
            TechnicalServiceNotAliveException,
            TechnicalServiceOperationException, ConcurrentModificationException,
            SubscriptionStateException;

    /**
     * Checks if the calling user has any subscriptions, regardless of their
     * status.
     * <p>
     * Required role: none
     * 
     * @return <code>true</code> if the user has subscriptions,
     *         <code>false</code> otherwise
     */

    boolean hasCurrentUserSubscriptions();

    /**
     * Changes the payment information and/or the billing contact for a
     * subscription. The payment type specified in the payment information must
     * have been enabled by the supplier or reseller of the underlying service.
     * <p>
     * A subscription that has been suspended will be reactivated after its
     * payment information or billing contact have been changed successfully.
     * <p>
     * Required role: administrator of the organization that owns the
     * subscription
     * 
     * @param subscription
     *            the subscription to modify
     * @param billingContact
     *            the billing contact to be assigned to the subscription
     * @param paymentInfo
     *            the value object specifying the payment information to be used
     *            for the subscription
     * @return the updated subscription details
     * @throws ObjectNotFoundException
     *             if one of the specified entities is not found
     * @throws ConcurrentModificationException
     *             if the stored subscription, billing contact, or payment
     *             information is changed by another user in the time between
     *             reading and writing it
     * @throws OperationNotPermittedException
     *             if one of the specified entities is not owned by the calling
     *             user's organization
     * @throws PaymentInformationException
     *             if the payment information or billing contact is invalid
     * @throws SubscriptionStateException
     *             if the subscription status is not
     *             {@link SubscriptionStatus#ACTIVE},
     *             {@link SubscriptionStatus#PENDING}, or
     *             {@link SubscriptionStatus#SUSPENDED}
     * @throws PaymentDataException
     *             when trying to specify payment information for a subscription
     *             whose service is free of charge
     * @throws TechnicalServiceNotAliveException
     *             if the underlying technical service cannot be reached
     * @throws TechnicalServiceOperationException
     *             if a technical operation related to the activation of the
     *             subscription fails
     */

    VOSubscriptionDetails modifySubscriptionPaymentData(
            VOSubscription subscription, VOBillingContact billingContact,
            VOPaymentInfo paymentInfo) throws ObjectNotFoundException,
            ConcurrentModificationException, OperationNotPermittedException,
            PaymentInformationException, SubscriptionStateException,
            PaymentDataException, TechnicalServiceNotAliveException,
            TechnicalServiceOperationException;

    /**
     * Reports an issue on the given subscription to the supplier or reseller of
     * the underlying service. If the service is provided by a broker, the issue
     * is reported to the supplier organization.
     * <p>
     * An email with the given subject and text and with details of the
     * subscription is sent to the support email address of the supplier or
     * reseller organization. If the support email address is not defined, the
     * email is sent to the organization's standard email address.
     * <p>
     * Required role: administrator of the customer organization that owns the
     * subscription
     * 
     * @param subscriptionId
     *            the subscription identifier
     * @param subject
     *            the subject of the email
     * @param issueText
     *            the text of the email
     * @throws ObjectNotFoundException
     *             if the subscription is not found
     * @throws OperationNotPermittedException
     *             if the calling user's organization is not a customer of the
     *             supplier or reseller organization that owns the service
     *             underlying to the subscription
     * @throws MailOperationException
     *             if the email cannot be sent
     * @throws ValidationException
     *             if the subject or text of the email is too long
     */

    void reportIssue(String subscriptionId, String subject, String issueText)
            throws ObjectNotFoundException, OperationNotPermittedException,
            MailOperationException, ValidationException;

    /**
     * Returns a list of subscription/customer mappings for the services
     * supplied by the calling user's organization including
     * SubscriptionStatus.EXPIRED subscriptions.
     * <p>
     * Required role: service manager of a supplier organization, broker of a
     * broker organization, or reseller of a reseller organization
     * 
     * @return the list of subscription identifiers mapped to the relevant
     *         customer organizations
     * @throws OrganizationAuthoritiesException
     *             if the calling user's organization does not have the
     *             supplier, broker, or reseller role
     */
    List<VOSubscriptionIdAndOrganizations> getSubscriptionsForTerminate()
            throws OrganizationAuthoritiesException;

    /**
     * Update the access information for the given subscription.
     * <p>
     * 
     * @param subscriptionId
     *            - the subscription identifier
     * @param organizationId
     *            - the identifier
     * @param instanceInfo
     *            - the instance info
     * @throws ObjectNotFoundException
     *             in case the subscription wasn't found -
     * @throws SubscriptionStateException
     *             if the subscription is in an invalid state -
     * @throws OperationNotPermittedException
     *             in case the calling users organization is not the owner of
     *             the subscriptions underlying technical service -
     * @throws ValidationException
     *             in case provided access information is syntactically invalid
     */
    void updateAccessInformation(String subscriptionId, String organizationId,
            VOInstanceInfo instanceInfo)
            throws ObjectNotFoundException, SubscriptionStateException,
            OperationNotPermittedException, ValidationException;

    /**
     * Updates the progress information for asynchronous operation record.
     * <p>
     * Required role: technology manager of the technology provider organization
     * that provides the application and technical service
     * 
     * @param transactionId
     *            the operation record identifier as specified
     * @param status
     *            the operation status
     * @param progress
     *            the progress information. The information can be provided for
     *            different locales.
     * @throws OperationNotPermittedException
     *             if the calling user's organization is not the provider of the
     *             application and technical service
     * @throws OperationStateException
     *             if the operation record status does not allow for the
     *             execution of this method
     */
    void updateAsyncOperationProgress(String transactionId,
            OperationStatus status, List<VOLocalizedText> progress)
            throws OperationNotPermittedException, OperationStateException;

    /**
     * Update subscription status and notify users execution result for
     * asynchronous operation.
     * <p>
     * Required role: technology manager of the technology provider organization
     * that provides the application and technical service
     * 
     * @param subscriptionId
     *            the identifier of the subscription for which details are to be
     *            retrieved
     * @param organizationId
     *            the ID of the customer organization
     * @param instanceInfo
     *            the value object containing instance information
     * @throws ObjectNotFoundException
     *             if the organization or subscription is not found
     */
    void updateAsyncSubscriptionStatus(String subscriptionId,
            String organizationId, VOInstanceInfo instanceInfo)
            throws ObjectNotFoundException;

    /**
     * @param subscriptionKey
     *            - the subscription key
     * @return Subscription details
     * @throws ObjectNotFoundException
     */
    VOSubscriptionDetails getSubscriptionDetailsWithoutOwnerCheck(
            long subscriptionKey) throws ObjectNotFoundException;

    /**
     * @return true - if user doesn't need to provide billing contact and
     *         payment information in subscription process. false - otherwise
     */
    boolean isPaymentInfoHidden();

    /**
     * Delegate to unsubscribe from a service by the subscriptions technical
     * key.
     * 
     * @param key
     *            the subscriptions technical key
     * @return <code>true</code> if unsubscribe was executed, <code>false</code>
     *         in case of an existing suspending trigger
     * @throws ObjectNotFoundException
     * @throws SubscriptionStillActiveException
     * @throws SubscriptionStateException
     * @throws TechnicalServiceNotAliveException
     * @throws TechnicalServiceOperationException
     * @throws OperationPendingException
     * @throws OperationNotPermittedException
     */
    boolean unsubscribeFromService(Long key)
            throws ObjectNotFoundException, SubscriptionStillActiveException,
            SubscriptionStateException, TechnicalServiceNotAliveException,
            TechnicalServiceOperationException, OperationPendingException,
            OperationNotPermittedException;

    /**
     * Update number of provisioned VMs for given subscription. This operation
     * can be performed only by the Technical Manager who belongs to the
     * technology provider organization which is the owner of the subscribed
     * service.
     *
     * @param subscriptionId
     *            the identifier of the subscription for which details are to be
     *            retrieved
     * @param organizationId
     *            the ID of the customer organization
     * @param instanceInfo
     *            the value object containing instance information
     * @throws ObjectNotFoundException
     *             if the organization or subscription is not found
     */
    void notifySubscriptionAboutVmsNumber(String subscriptionId,
            String organizationId, VOInstanceInfo instanceInfo)
            throws ObjectNotFoundException, OperationNotPermittedException;

    /**
     * Method which completes the asynchronous provisioning for subscription
     * with given UUID.
     * 
     * @param subscriptionUUID
     *            Subscription UUID
     * @param instanceInfo
     *            VOInstanceInfo instanceInfo
     * @throws ValidationException
     * @throws OperationNotPermittedException
     * @throws OrganizationAuthoritiesException
     * @throws TechnicalServiceOperationException
     * @throws TechnicalServiceNotAliveException
     * @throws SubscriptionStateException
     * @throws ObjectNotFoundException
     */
    void completeAsyncSubscription(UUID subscriptionUUID,
            VOInstanceInfo instanceInfo) throws ObjectNotFoundException,
            SubscriptionStateException, TechnicalServiceNotAliveException,
            TechnicalServiceOperationException,
            OrganizationAuthoritiesException, OperationNotPermittedException,
            ValidationException;
    
    /**
     * Method which returns subscription with details.
     * 
     * @param uuid
     *            Subscription UUID
     * @return subscription with details.
     */
    public VOSubscription getSubscription(UUID uuid);
}
