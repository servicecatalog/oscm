/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.subscriptionservice.local;

import java.util.List;

import javax.ejb.Local;
import javax.ejb.TransactionAttributeType;

import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.RoleDefinition;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.TriggerProcess;
import org.oscm.domobjects.UsageLicense;
import org.oscm.internal.intf.SubscriptionService;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.MandatoryCustomerUdaMissingException;
import org.oscm.internal.types.exception.MandatoryUdaMissingException;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.OperationPendingException;
import org.oscm.internal.types.exception.OrganizationAuthoritiesException;
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
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOSubscription;
import org.oscm.internal.vo.VOSubscriptionDetails;
import org.oscm.internal.vo.VOUsageLicense;
import org.oscm.internal.vo.VOUser;
import org.oscm.paginator.Pagination;
import org.oscm.paginator.PaginationFullTextFilter;
import org.oscm.types.exceptions.UserAlreadyAssignedException;
import org.oscm.types.exceptions.UserNotAssignedException;

/**
 * Local Interface of Subscription Management Service.
 */
@Local
public interface SubscriptionServiceLocal {

    /**
     * Continues the ADD_REVOKE_USER TriggerProcess
     * 
     * @see org.oscm.intf.SubscriptionService#addRevokeUser(String, Map<VOUser,
     *      Boolean>, List<VOUser>)
     * 
     * @param triggerProcess
     *            The trigger process which is continued.
     * @throws ObjectNotFoundException
     *             Thrown if either the subscription or one of the given users
     *             cannot be found
     * @throws ServiceParameterException
     *             Thrown in case the user count (of the subscription) is
     *             greater than the value of the product parameter NAMED_USER
     * @throws SubscriptionStateException
     *             Thrown in case the subscription state does not allow to call
     *             this method.
     * @throws TechnicalServiceNotAliveException
     *             Thrown in case the service for the operation cannot be
     *             reached.
     * @throws TechnicalServiceOperationException
     * @throws OperationNotPermittedException
     *             Thrown in case invalid service roles shall be set when the
     *             technical service defines service roles.
     * @throws ConcurrentModificationException
     *             Thrown if the object versions does not match.
     */
    public void addRevokeUserInt(TriggerProcess triggerProcess)
            throws ObjectNotFoundException, ServiceParameterException,
            SubscriptionStateException, TechnicalServiceNotAliveException,
            TechnicalServiceOperationException, OperationNotPermittedException,
            ConcurrentModificationException;

    /**
     * Adds a given User to the given subscription. If isAdmin is set, the user
     * gets administrative privileges.
     * 
     * <p>
     * NOTE: The application is not informed about the modification. No mail is
     * sent to the user to inform him about his new access.
     * </p>
     * 
     * @param subscription
     * @param user
     * @param serviceRole
     *            the role to set
     * @throws UserAlreadyAssignedException
     *             Thrown if the user is already assigned to the subscription
     */
    public UsageLicense addUserToSubscription(Subscription subscription,
            PlatformUser user, RoleDefinition serviceRole)
            throws UserAlreadyAssignedException;

    /**
     * Informs the product about the users to be created.
     * 
     * @param subscription
     *            The subscription the product of which will be informed to
     *            create the users.
     * @param usersToBeAdded
     *            The users the product should know.
     * @throws SubscriptionStateException
     *             Thrown in case the subscription state does not allow to call
     *             this method.
     * @throws TechnicalServiceNotAliveException
     *             TODO
     * @throws TechnicalServiceOperationException
     *             TODO
     */
    public void informProductAboutNewUsers(Subscription subscription,
            List<PlatformUser> usersToBeAdded)
            throws SubscriptionStateException,
            TechnicalServiceNotAliveException,
            TechnicalServiceOperationException;

    /**
     * Revokes a given User from a given subscription. If the user has been the
     * last user assigned for the subscription, an exception is thrown and the
     * user will not be revoked.
     * 
     * @param subscription
     * @param users
     *            The users to be revoked from the subscription.
     * @throws SubscriptionStateException
     *             Thrown in case the subscription state does not allow to call
     *             this method.
     * @throws TechnicalServiceNotAliveException
     *             Thrown in case the service for the operation cannot be
     *             reached.
     * @throws TechnicalServiceOperationException
     *             TODO
     */
    public void revokeUserFromSubscription(Subscription subscription,
            List<PlatformUser> users) throws SubscriptionStateException,
            TechnicalServiceNotAliveException,
            TechnicalServiceOperationException;

    /**
     * Revoke users from a given subscription without checking the status of
     * this subscription.
     * 
     * @param subscription
     * @param users
     *            The users to be revoked from the subscription.
     * @throws TechnicalServiceNotAliveException
     *             Thrown in case the service for the operation cannot be
     *             reached.
     * @throws TechnicalServiceOperationException
     *             TODO
     */
    public void revokeUserFromSubscriptionInt(Subscription subscription,
            List<PlatformUser> users) throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException;

    /**
     * Returns a list of active subscriptions a given user has a (non-revoked)
     * usage license for
     * 
     * @param user
     * @return
     */
    public List<Subscription> getSubscriptionsForUserInt(PlatformUser user);

    /**
     * Validate this subscription id whether has existed in the calling user's
     * organization, regardless of their status.
     * <p>
     * Required role: administrator of the user's organization
     * 
     * @param subscriptionId
     *            the subscription identifier
     * @return <code>true</code> if the subscription id has already been used,
     *         <code>false</code> if the subscription id doesn't exist in
     *         calling user's organization
     */

    public boolean validateSubscriptionIdForOrganization(String subscriptionId);

    /**
     * Returns the subscription object
     * 
     * @param subscriptionKey
     * @return
     * @throws ObjectNotFoundException
     */
    public Subscription loadSubscription(long subscriptionKey)
            throws ObjectNotFoundException;

    /**
     * Checks which subscriptions are activated for a longer time than permitted
     * in the corresponding product parameters. For all those found
     * subscriptions, the subscription will be set into state
     * {@link SubscriptionStatus#EXPIRED}.
     * 
     * @param currentTime
     *            The time the timer expired.
     * @return <code>true</code> in case the operation passed without problems,
     *         <code>false</code> as soon as one handling step failed.
     */
    public boolean expireOverdueSubscriptions(long currentTime);

    /**
     * Expires the given subscription, no matter if it is currently in use or
     * not. This method will be executed within a new transaction, the used
     * transaction attribute is {@link TransactionAttributeType#REQUIRES_NEW}.
     * 
     * @param subscriptionToExpire
     *            The subscription to expire.
     * @return <code>true</code> in case the expiration succeeded,
     *         <code>false</code> otherwise.
     */
    public boolean expireSubscription(Subscription subscriptionToExpire);

    /**
     * Checks if there are subscriptions in state
     * {@link SubscriptionStatus#PENDING} which have reached their specified
     * timeout and thus administrators of the technical product have to be
     * informed about that.
     * 
     * @param currentTime
     *            The time the timer expired.
     * @return <code>true</code> in case the operation passed without problems,
     *         <code>false</code> as soon as one handling step failed.
     */
    public boolean notifyAboutTimedoutSubscriptions(long currentTime);

    /**
     * Performs the concrete operations to modify the subscription, after a
     * required confirmation of a notification listener has been retrieved. If
     * none is required, it will be executed synchronously with the call to
     * {@link #modifySubscription(VOSubscription, List)}.
     * 
     * @param tp
     *            The trigger process containing the detail information for
     *            further processing.
     * @return The modified subscription.
     * @throws NonUniqueBusinessKeyException
     *             Thrown in case there is already a subscription with the given
     *             ID for the organization.
     * @throws ObjectNotFoundException
     *             Thrown in case there is no subscription corresponding to the
     *             given one.
     * @throws OperationNotPermittedException
     *             Thrown in case the user is not permitted to modify the
     *             subscription, or in case a parameter shall be changed that is
     *             not configurable or part of the subscription's service.
     * @throws ValidationException
     *             Thrown in case the given ID is not valid (empty or too long).
     * @throws SubscriptionMigrationException
     *             Thrown in case parameters have been changed but the technical
     *             service could not be notified about the parameter change.
     * @throws ConcurrentModificationException
     *             Thrown if the object versions does not match.
     * @throws TechnicalServiceNotAliveException
     *             Thrown in case the service for the operation cannot be
     *             reached.
     * @throws MandatoryUdaMissingException
     * @throws SubscriptionStateException
     *             Thrown in case that the modification is not allowed for
     *             current subscription state
     * @throws MandatoryCustomerUdaMissingException
     * 
     */
    public VOSubscriptionDetails modifySubscriptionInt(TriggerProcess tp)
            throws ObjectNotFoundException, NonUniqueBusinessKeyException,
            ValidationException, OperationNotPermittedException,
            SubscriptionMigrationException, ConcurrentModificationException,
            TechnicalServiceNotAliveException, MandatoryUdaMissingException,
            SubscriptionStateException, MandatoryCustomerUdaMissingException;

    /**
     * Performs the concrete operations to upgrade a subscription, after a
     * required confirmation of a notification listener has been retrieved. If
     * none is required, it will be executed synchronously with the call to
     * {@link #upgradeSubscription(VOSubscription, VOService)}.
     * 
     * @param tp
     *            The trigger process containing the detailed information on how
     *            to process the current task.
     * @return The upgraded subscription.
     * @throws ObjectNotFoundException
     *             Thrown in case the subscription or the target service cannot
     *             be found.
     * @throws OperationNotPermittedException
     *             Thrown in case the caller is not permitted to perform this
     *             operation.
     * @throws SubscriptionMigrationException
     *             Thrown in case the subscription could not be migrated, e.g.
     *             in case the current service is not compatible to the target
     *             service.
     * @throws PaymentInformationException
     *             Thrown in case the user has not yet specified payment
     *             information and tries to migrate to a service that is not
     *             free of charge.
     * 
     * @throws SubscriptionStateException
     *             Thrown in case the subscription state does not allow to call
     *             this method.
     * @throws ServiceChangedException
     *             Thrown in case the new service has been changed between
     *             viewing and updating.
     * @throws PriceModelException
     *             Thrown in case the target service has no price model defined.
     * @throws ConcurrentModificationException
     *             Thrown in case the subscription parameter does not match the
     *             current subscription.
     * @throws TechnicalServiceNotAliveException
     *             Thrown in case the service for the operation cannot be
     *             reached.
     * @throws MandatoryUdaMissingException
     * @throws ValidationException
     * @throws NonUniqueBusinessKeyException
     * @throws MandatoryCustomerUdaMissingException
     */
    public Subscription upgradeSubscriptionInt(TriggerProcess tp)
            throws ObjectNotFoundException, SubscriptionStateException,
            OperationNotPermittedException, ServiceChangedException,
            PriceModelException, PaymentInformationException,
            SubscriptionMigrationException, ConcurrentModificationException,
            TechnicalServiceNotAliveException, NonUniqueBusinessKeyException,
            ValidationException, MandatoryUdaMissingException,
            MandatoryCustomerUdaMissingException;

    /**
     * Performs the concrete operations to unsubscribe from a subscription,
     * after a required confirmation of a notification listener has been
     * retrieved. If none is required, it will be executed synchronously with
     * the call to {@link #unsubscribeFromService(String)}.
     * 
     * @param tp
     *            The trigger process providing the detailed information on how
     *            to handle the request.
     * @throws ObjectNotFoundException
     *             Thrown if the subscription cannot be found.
     * @throws SubscriptionStillActiveException
     *             Thrown in case there are still active sessions for the
     *             subscription.
     * @throws SubscriptionStateException
     *             Thrown in case the subscription state does not allow to call
     *             this method.
     * @throws TechnicalServiceNotAliveException
     *             Thrown in case the technical product cannot be reached.
     * @throws TechnicalServiceOperationException
     *             Thrown in case an exception occured in the technical product
     *             while handling the unsubscribing process.
     */
    public void unsubscribeFromServiceInt(TriggerProcess tp)
            throws ObjectNotFoundException, SubscriptionStateException,
            SubscriptionStillActiveException, TechnicalServiceNotAliveException,
            TechnicalServiceOperationException;

    /**
     * Performs the concrete operations to subscribe to a product, after a
     * required confirmation of a notification listener has been retrieved. If
     * none is required, it will be executed synchronously with the call to
     * {@link SubscriptionService#subscribeToService(VOSubscription, VOService, List)}
     * .
     * 
     * @param tp
     *            The trigger process containing the detail information on how
     *            to handle this request.
     * @return The created subscription, <code>null</code> in case the operation
     *         was suspended.
     * @throws ObjectNotFoundException
     *             Thrown if a given user or the service cannot be found.
     * @throws ValidationException
     *             Thrown in case the subscription data could not be validated.
     * @throws OperationNotPermittedException
     *             Thrown in case the user wants to subscribe to a service that
     *             is customer-specific but not for the calling user's
     *             organization.
     * @throws ServiceChangedException
     *             Thrown in case the service has been changed between viewing
     *             and subscribing.
     * @throws PriceModelException
     *             Thrown in case there is no price model defined for the
     *             specified service.
     * @throws PaymentInformationException
     *             Thrown in case the user wants to subscribe to a service that
     *             is not free of charge but has not yet specified payment
     *             information.
     * @throws NonUniqueBusinessKeyException
     *             Thrown if the subscription ID is not unique within the
     *             organization.
     * @throws TechnicalServiceNotAliveException
     *             Thrown in case the technical service cannot be reached.
     * @throws TechnicalServiceOperationException
     *             Thrown in case the operation failed at the technical service.
     * @throws ServiceParameterException
     *             Thrown in case the user count of the subscription is greater
     *             than the value of the <code>NAMED_USER</code> service
     *             parameter.
     * @throws SubscriptionAlreadyExistsException
     *             Thrown in case the a subscription exists for the specified
     *             service and the underlying technical service allows only one
     *             subscription per organization.
     * @throws MandatoryUdaMissingException
     * @throws ConcurrentModificationException
     * @throws MandatoryCustomerUdaMissingException
     * 
     */
    public Subscription subscribeToServiceInt(TriggerProcess tp)
            throws ObjectNotFoundException, ValidationException,
            OperationNotPermittedException, ServiceChangedException,
            PriceModelException, PaymentInformationException,
            NonUniqueBusinessKeyException, TechnicalServiceNotAliveException,
            TechnicalServiceOperationException, ServiceParameterException,
            SubscriptionAlreadyExistsException, ConcurrentModificationException,
            MandatoryUdaMissingException, SubscriptionStateException,
            MandatoryCustomerUdaMissingException;

    /**
     * Modifies the role/authority of an user in the context of the provided
     * subscription.
     * 
     * @param subscription
     *            the context {@link Subscription}
     * @param usr
     *            the {@link PlatformUser} to modify the role for
     * @param roleDef
     *            the {@link RoleDefinition} if defined by the technical product
     *            or <code>null</code> if default authorities will be used
     * @throws SubscriptionStateException
     *             Thrown in case the subscription is in a state where no roles
     *             can be modified
     * @throws UserNotAssignedException
     *             Thrown in case the user whose role should be changed is not
     *             yet assigned to the subscription
     * @throws TechnicalServiceNotAliveException
     *             Thrown in case of communication errors with the technical
     *             product
     * @throws TechnicalServiceOperationException
     *             Thrown in case of an error while processing on technical
     *             product side
     */
    void modifyUserRole(Subscription subscription, PlatformUser usr,
            RoleDefinition roleDef) throws SubscriptionStateException,
            UserNotAssignedException, TechnicalServiceNotAliveException,
            TechnicalServiceOperationException;

    /**
     * FIXME: Duplicated in local and remote interface
     * 
     * @see SubscriptionService#addRevokeUser(String subscriptionId,
     *      List<VOUsageLicense> usersToBeAdded, List<VOUser> usersToBeRevoked)
     */
    public boolean addRevokeUser(String subscriptionId,
            List<VOUsageLicense> usersToBeAdded, List<VOUser> usersToBeRevoked)
            throws ObjectNotFoundException, ServiceParameterException,
            SubscriptionStateException, TechnicalServiceNotAliveException,
            TechnicalServiceOperationException, OperationNotPermittedException,
            ConcurrentModificationException, OperationPendingException;

    /**
     * Returns a list of subscription for the services supplied by the calling
     * user's organization including SubscriptionStatus.EXPIRED subscriptions.
     * <p>
     * Required role: service manager of a supplier organization, broker manager
     * of a broker organization, or reseller manager of a reseller organization
     * 
     * @return the list of subscription
     * 
     * @throws OrganizationAuthoritiesException
     *             if the calling user's organization does not have the
     *             supplier, broker, or reseller role
     */
    public List<Subscription> getSubscriptionsForManagers()
            throws OrganizationAuthoritiesException;

    /**
     * Returns a list of subscriptions for the services supplied by the calling
     * user's organization including SubscriptionStatus.EXPIRED subscriptions.
     * The list contains the subscriptions in specified range, defined with an
     * offset and limit.
     * <p>
     * Required role: service manager of a supplier organization, broker manager
     * of a broker organization, or reseller manager of a reseller organization
     * 
     * @param pagination
     *            the parameters which describe the range of result data and the
     *            sort order
     * 
     * @return the list of subscriptions in a specified range
     * 
     * @throws OrganizationAuthoritiesException
     *             if the calling user's organization does not have the
     *             supplier, broker, or reseller role
     */
    public List<Subscription> getSubscriptionsForManagers(Pagination pagination)
            throws OrganizationAuthoritiesException;

    /**
     * Retrieves the subscriptions the calling user is assigned to. The list
     * includes subscriptions whose status is <code>ACTIVE</code>,
     * <code>PENDING</code>, <code>SUSPENDED</code>, or <code>EXPIRED</code>.
     * <p>
     * Required role: any user role in an organization Results are filtered by
     * the value given as parameter.
     *
     * @param pagination
     *            the parameters which describe the range of result data and the
     *            sort order
     *
     * @return the list of subscriptions
     */
    public List<Subscription> getSubscriptionsForCurrentUserWithFiltering(
            PaginationFullTextFilter pagination);

    /**
     * Retrieves the size of subscriptions the calling user is assigned to. The
     * list includes subscriptions whose status is <code>ACTIVE</code>,
     * <code>PENDING</code>, <code>SUSPENDED</code>, or <code>EXPIRED</code>.
     * <p>
     * Required role: any user role in an organization Results are filtered by
     * the value given as parameter.
     *
     * @param pagination
     *            the parameters which describe the range of result data and the
     *            sort order
     *
     * @return the list of subscriptions
     */
    public Integer getSubscriptionsSizeForCurrentUserWithFiltering(
            PaginationFullTextFilter pagination);

    /**
     * Returns the usage license referenced by subscription key and user.
     * 
     * @param user
     *            the {@link PlatformUser} to find the usage license for
     * 
     * @param subKey
     *            key referencing the subscription to find the usage license for
     * 
     * @return the usage license
     * 
     */
    public UsageLicense getSubscriptionUsageLicense(PlatformUser user,
            Long subKey);

    /**
     * Removes information about owner from the subscription.
     *
     * @param sub
     *            - subscription which should be modified
     */
    public void removeSubscriptionOwner(Subscription sub);

    /**
     * Method which returns subscription with details, but only assigned to
     * currently logged in user. Null otherwise.
     * 
     * @param key
     *            Subscription tkey
     * @return subscription with details, but only assigned to currently logged
     *         in user. Null otherwise.
     */
    public Subscription getMySubscriptionDetails(long key);
}
