/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2010-06-17                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.notification.intf;

import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.jws.WebParam;
import javax.jws.WebService;

import org.oscm.notification.vo.VONotification;
import org.oscm.types.enumtypes.UserRoleType;
import org.oscm.vo.VOOrganization;
import org.oscm.vo.VOOrganizationPaymentConfiguration;
import org.oscm.vo.VOParameter;
import org.oscm.vo.VOPaymentType;
import org.oscm.vo.VOService;
import org.oscm.vo.VOServicePaymentConfiguration;
import org.oscm.vo.VOSubscription;
import org.oscm.vo.VOTriggerProcess;
import org.oscm.vo.VOUsageLicense;
import org.oscm.vo.VOUser;
import org.oscm.vo.VOUserDetails;

/**
 * Interface defining the Web services which organizations must provide in order
 * to be able to receive notifications from the platform. Such Web services and
 * notifications can be used, for example, to integrate external process control
 * systems with the platform. The information on how to access the service must
 * be configured in the platform.
 */
@WebService(targetNamespace = "http://oscm.org/xsd")
public interface NotificationService {

    /**
     * Notifies the completion of a billing run for a certain customer and
     * provides the detailed billing data.
     * 
     * @param xmlBillingData
     *            the XML representation of the billing data
     */
    public void billingPerformed(
            @WebParam(name = "xmlBillingData") String xmlBillingData);

    /**
     * Notifies the addition of a supplier to the list of suppliers authorized
     * by a technology provider.
     * 
     * @param triggerProcess
     *            the platform's trigger process object for the action with the
     *            key required for later callbacks
     * @param supplierId
     *            the identifier of the supplier organization to add
     */
    public void onAddSupplier(
            @WebParam(name = "triggerProcess") VOTriggerProcess triggerProcess,
            @WebParam(name = "supplierId") String supplierId);

    /**
     * Notifies the removal of a supplier from the list of suppliers authorized
     * by a technology provider.
     * 
     * @param triggerProcess
     *            the platform's trigger process object for the action with the
     *            key required for later callbacks
     * @param supplierId
     *            the identifier of the supplier organization to remove
     */
    public void onRemoveSupplier(
            @WebParam(name = "triggerProcess") VOTriggerProcess triggerProcess,
            @WebParam(name = "supplierId") String supplierId);

    /**
     * Notifies the registration of a customer.
     * 
     * @param triggerProcess
     *            the platform's trigger process object for the action with the
     *            key required for later callbacks
     * @param organization
     *            the customer organization to create
     * @param user
     *            the first administrator to be created for the new organization
     * @param organizationProperties
     *            additional optional properties of the customer organization
     */
    public void onRegisterCustomer(
            @WebParam(name = "triggerProcess") VOTriggerProcess triggerProcess,
            @WebParam(name = "organization") VOOrganization organization,
            @WebParam(name = "user") VOUserDetails user,
            @WebParam(name = "organizationProperties") Properties organizationProperties);

    /**
     * Notifies a save operation for a supplier's or reseller's default payment
     * configuration for customers.
     * 
     * @param triggerProcess
     *            the platform's trigger process object for the action with the
     *            key required for later callbacks
     * @param defaultConfiguration
     *            the default payment configuration for customers to be saved
     *            for the supplier or reseller
     */
    public void onSaveDefaultPaymentConfiguration(
            @WebParam(name = "triggerProcess") VOTriggerProcess triggerProcess,
            @WebParam(name = "defaultConfiguration") Set<VOPaymentType> defaultConfiguration);

    /**
     * Notifies a save operation for a supplier's or reseller's
     * customer-specific payment configurations.
     * 
     * @param triggerProcess
     *            the platform's trigger process object for the action with the
     *            key required for later callbacks
     * @param customerConfiguration
     *            one customer-specific payment configuration to be saved for
     *            the supplier or reseller
     */
    public void onSaveCustomerPaymentConfiguration(
            @WebParam(name = "triggerProcess") VOTriggerProcess triggerProcess,
            @WebParam(name = "customerConfiguration") VOOrganizationPaymentConfiguration customerConfiguration);

    /**
     * Notifies the activation of a marketable service.
     * 
     * @param triggerProcess
     *            the platform's trigger process object for the action with the
     *            key required for later callbacks
     * @param product
     *            the service to activate
     */
    public void onActivateProduct(
            @WebParam(name = "triggerProcess") VOTriggerProcess triggerProcess,
            @WebParam(name = "product") VOService product);

    /**
     * Notifies the deactivation of a marketable service.
     * 
     * @param triggerProcess
     *            the platform's trigger process object for the action with the
     *            key required for later callbacks
     * @param product
     *            the service to deactivate
     */
    public void onDeactivateProduct(
            @WebParam(name = "triggerProcess") VOTriggerProcess triggerProcess,
            @WebParam(name = "product") VOService product);

    /**
     * Notifies a subscription to a service for the subscribing organization.
     * 
     * @param triggerProcess
     *            the platform's trigger process object for the action with the
     *            key required for later callbacks
     * @param subscription
     *            the subscription to create
     * @param product
     *            the service to subscribe to
     * @param users
     *            the users to be assigned to the subscription, including their
     *            service roles
     */
    public void onSubscribeToProduct(
            @WebParam(name = "triggerProcess") VOTriggerProcess triggerProcess,
            @WebParam(name = "subscription") VOSubscription subscription,
            @WebParam(name = "product") VOService product,
            @WebParam(name = "users") List<VOUsageLicense> users);

    /**
     * Notifies the termination of a subscription to a service for the
     * organization owning the subscription.
     * 
     * @param triggerProcess
     *            the platform's trigger process object for the action with the
     *            key required for later callbacks
     * @param subId
     *            the identifier of the subscription to terminate
     */
    public void onUnsubscribeFromProduct(
            @WebParam(name = "triggerProcess") VOTriggerProcess triggerProcess,
            @WebParam(name = "subId") String subId);

    /**
     * Notifies the modification of a subscription for the organization owning
     * the subscription.
     * 
     * @param triggerProcess
     *            the platform's trigger process object for the action with the
     *            key required for later callbacks
     * @param subscription
     *            the subscription to modify
     * @param modifiedParameters
     *            the modified parameters for the subscription
     */
    public void onModifySubscription(
            @WebParam(name = "triggerProcess") VOTriggerProcess triggerProcess,
            @WebParam(name = "subscription") VOSubscription subscription,
            @WebParam(name = "modifiedParameters") List<VOParameter> modifiedParameters);

    /**
     * Notifies an upgrade or downgrade of a subscription.
     * 
     * @param triggerProcess
     *            the platform's trigger process object for the action with the
     *            key required for later callbacks
     * @param current
     *            the subscription to upgrade or downgrade
     * @param newProduct
     *            the service to which the subscription is to be upgraded or
     *            downgraded
     */
    public void onUpgradeSubscription(
            @WebParam(name = "triggerProcess") VOTriggerProcess triggerProcess,
            @WebParam(name = "current") VOSubscription current,
            @WebParam(name = "newProduct") VOService newProduct);

    /**
     * Notifies the assignment or removal of users to/from a subscription.
     * 
     * @param triggerProcess
     *            the platform's trigger process object for the action with the
     *            key required for later callbacks
     * @param subscriptionId
     *            the identifier of the subscription for which user assignments
     *            are to be modified
     * @param usersToBeAdded
     *            the users to be added to the subscription, including their
     *            service roles
     * @param usersToBeRevoked
     *            the users to be removed from the subscription
     */
    public void onAddRevokeUser(
            @WebParam(name = "triggerProcess") VOTriggerProcess triggerProcess,
            @WebParam(name = "subscriptionId") String subscriptionId,
            @WebParam(name = "usersToBeAdded") List<VOUsageLicense> usersToBeAdded,
            @WebParam(name = "usersToBeRevoked") List<VOUser> usersToBeRevoked);

    /**
     * Notifies a save operation for a supplier's or reseller's service-specific
     * payment configurations.
     * 
     * @param triggerProcess
     *            the platform's trigger process object for the action with the
     *            key required for later callbacks
     * @param serviceConfiguration
     *            one service-specific payment configuration to be saved for the
     *            supplier or reseller
     */
    public void onSaveServicePaymentConfiguration(
            @WebParam(name = "triggerProcess") VOTriggerProcess triggerProcess,
            @WebParam(name = "serviceConfiguration") VOServicePaymentConfiguration serviceConfiguration);

    /**
     * Notifies a save operation for a supplier's or reseller's default payment
     * configuration for services.
     * 
     * @param triggerProcess
     *            the platform's trigger process object for the action with the
     *            key required for later callbacks
     * @param defaultConfiguration
     *            the default payment configuration for services to be saved for
     *            the supplier or reseller
     */
    public void onSaveServiceDefaultPaymentConfiguration(
            @WebParam(name = "triggerProcess") VOTriggerProcess triggerProcess,
            @WebParam(name = "defaultConfiguration") Set<VOPaymentType> defaultConfiguration);

    /**
     * Notifies a subscription to a service for the supplier, broker, or
     * reseller organization that provides the service.
     * 
     * @param triggerProcess
     *            the platform's trigger process object for the action with the
     *            key required for later callbacks
     * @param service
     *            the service to subscribe to
     * @param users
     *            the users to be assigned to the subscription, including their
     *            service roles
     * @param notification
     *            the notification with all the relevant data the supplier,
     *            broker, or reseller organization is entitled to read: <br>
     *            <code>subscription.subscriptionId</code>,
     *            <code>subscription.serviceInstanceId</code>,
     *            <code>billingContact.email</code>,
     *            <code>billingContact.companyName</code>,
     *            <code>billingContact.address</code>
     */
    public void onSubscriptionCreation(
            @WebParam(name = "triggerProcess") VOTriggerProcess triggerProcess,
            @WebParam(name = "service") VOService service,
            @WebParam(name = "users") List<VOUsageLicense> users,
            @WebParam(name = "notification") VONotification notification);

    /**
     * Notifies the modification of a subscription for the supplier, broker, or
     * reseller organization that provides the service.
     * 
     * @param triggerProcess
     *            the platform's trigger process object for the action with the
     *            key required for later callbacks
     * @param modifiedParameters
     *            the modified properties of the subscription
     * @param notification
     *            the notification with all the relevant data the supplier,
     *            broker, or reseller organization is entitled to read: <br>
     *            <code>subscription.subscriptionId</code>,
     *            <code>subscription.serviceInstanceId</code>,
     *            <code>subscription.serviceId</code>,
     *            <code>subscription.serviceKey</code>
     */
    public void onSubscriptionModification(
            @WebParam(name = "triggerProcess") VOTriggerProcess triggerProcess,
            @WebParam(name = "modifiedParameters") List<VOParameter> modifiedParameters,
            @WebParam(name = "notification") VONotification notification);

    /**
     * Notifies the termination of a subscription for the supplier, broker, or
     * reseller organization that provides the service.
     * 
     * @param triggerProcess
     *            the platform's trigger process object for the action with the
     *            key required for later callbacks
     * @param notification
     *            the notification with all the relevant data the supplier,
     *            broker, or reseller organization is entitled to read: <br>
     *            <code>subscription.subscriptionId</code>
     */
    public void onSubscriptionTermination(
            @WebParam(name = "triggerProcess") VOTriggerProcess triggerProcess,
            @WebParam(name = "notification") VONotification notification);

    /**
     * Notifies the registration of a user in one's own organization.
     * 
     * @param triggerProcess
     *            the platform's trigger process object for the action with the
     *            key required for later callbacks
     * @param user
     *            the user to be created
     * @param roles
     *            the roles to be assigned to the user
     * @param marketplaceId
     *            the ID of the marketplace from which to get customized texts
     */
    public void onRegisterUserInOwnOrganization(
            @WebParam(name = "triggerProcess") VOTriggerProcess triggerProcess,
            @WebParam(name = "user") VOUserDetails user,
            @WebParam(name = "roles") List<UserRoleType> roles,
            @WebParam(name = "marketplaceId") String marketplaceId);

    /**
     * Notifies the cancellation of a process-controlled action in the
     * <code>WAITING_FOR_APPROVAL</code> status.
     * 
     * @param actionKey
     *            the key of the platform's trigger process object for the
     *            cancelled action for later callbacks
     */
    public void onCancelAction(@WebParam(name = "actionKey") long actionKey);
}
