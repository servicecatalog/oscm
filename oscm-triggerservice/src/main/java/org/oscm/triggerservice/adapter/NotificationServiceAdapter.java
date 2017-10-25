/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2015年2月4日                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.triggerservice.adapter;

import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.dataservice.local.DataService;
import org.oscm.notification.intf.NotificationService;
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
 * @author gaowenxin
 * 
 */
public class NotificationServiceAdapter implements INotificationServiceAdapter {

    DataService ds;
    NotificationService svc;

    @Override
    public void billingPerformed(String xmlBillingData) {
        svc.billingPerformed(xmlBillingData);
    }

    @Override
    public void onAddSupplier(VOTriggerProcess triggerProcess, String supplierId) {
        svc.onAddSupplier(triggerProcess, supplierId);
    }

    @Override
    public void onRemoveSupplier(VOTriggerProcess triggerProcess,
            String supplierId) {
        svc.onRemoveSupplier(triggerProcess, supplierId);
    }

    @Override
    public void onRegisterCustomer(VOTriggerProcess triggerProcess,
            VOOrganization organization, VOUserDetails user,
            Properties organizationProperties) {
        svc.onRegisterCustomer(triggerProcess, organization, user,
                organizationProperties);
    }

    @Override
    public void onSaveDefaultPaymentConfiguration(
            VOTriggerProcess triggerProcess,
            Set<VOPaymentType> defaultConfiguration) {
        svc.onSaveDefaultPaymentConfiguration(triggerProcess,
                defaultConfiguration);
    }

    @Override
    public void onSaveCustomerPaymentConfiguration(
            VOTriggerProcess triggerProcess,
            VOOrganizationPaymentConfiguration customerConfiguration) {
        svc.onSaveCustomerPaymentConfiguration(triggerProcess,
                customerConfiguration);
    }

    @Override
    public void onActivateProduct(VOTriggerProcess triggerProcess,
            VOService product) {
        svc.onActivateProduct(triggerProcess, product);
    }

    @Override
    public void onDeactivateProduct(VOTriggerProcess triggerProcess,
            VOService product) {
        svc.onDeactivateProduct(triggerProcess, product);
    }

    @Override
    public void onSubscribeToProduct(VOTriggerProcess triggerProcess,
            VOSubscription subscription, VOService product,
            List<VOUsageLicense> users) {
        svc.onSubscribeToProduct(triggerProcess, subscription, product, users);
    }

    @Override
    public void onUnsubscribeFromProduct(VOTriggerProcess triggerProcess,
            String subId) {
        svc.onUnsubscribeFromProduct(triggerProcess, subId);
    }

    @Override
    public void onModifySubscription(VOTriggerProcess triggerProcess,
            VOSubscription subscription, List<VOParameter> modifiedParameters) {
        svc.onModifySubscription(triggerProcess, subscription,
                modifiedParameters);
    }

    @Override
    public void onUpgradeSubscription(VOTriggerProcess triggerProcess,
            VOSubscription current, VOService newProduct) {
        svc.onUpgradeSubscription(triggerProcess, current, newProduct);
    }

    @Override
    public void onAddRevokeUser(VOTriggerProcess triggerProcess,
            String subscriptionId, List<VOUsageLicense> usersToBeAdded,
            List<VOUser> usersToBeRevoked) {
        svc.onAddRevokeUser(triggerProcess, subscriptionId, usersToBeAdded,
                usersToBeRevoked);
    }

    @Override
    public void onSaveServicePaymentConfiguration(
            VOTriggerProcess triggerProcess,
            VOServicePaymentConfiguration serviceConfiguration) {
        svc.onSaveServicePaymentConfiguration(triggerProcess,
                serviceConfiguration);
    }

    @Override
    public void onSaveServiceDefaultPaymentConfiguration(
            VOTriggerProcess triggerProcess,
            Set<VOPaymentType> defaultConfiguration) {
        svc.onSaveServiceDefaultPaymentConfiguration(triggerProcess,
                defaultConfiguration);
    }

    @Override
    public void onSubscriptionCreation(VOTriggerProcess triggerProcess,
            VOService service, List<VOUsageLicense> users,
            VONotification notification) {
        svc.onSubscriptionCreation(triggerProcess, service, users, notification);
    }

    @Override
    public void onSubscriptionModification(VOTriggerProcess triggerProcess,
            List<VOParameter> modifiedParameters, VONotification notification) {
        svc.onSubscriptionModification(triggerProcess, modifiedParameters,
                notification);
    }

    @Override
    public void onSubscriptionTermination(VOTriggerProcess triggerProcess,
            VONotification notification) {
        svc.onSubscriptionTermination(triggerProcess, notification);
    }

    @Override
    public void onRegisterUserInOwnOrganization(
            VOTriggerProcess triggerProcess, VOUserDetails user,
            List<UserRoleType> roles, String marketplaceId) {
        svc.onRegisterUserInOwnOrganization(triggerProcess, user, roles,
                marketplaceId);
    }

    @Override
    public void setNotificationService(Object notificationService) {
        svc = NotificationService.class.cast(notificationService);
    }

    @Override
    public void setConfigurationService(
            ConfigurationServiceLocal configurationService) {
    }

    @Override
    public void setDataService(DataService dataService) {
    }

    @Override
    public void onCancelAction(long actionKey) {
        svc.onCancelAction(actionKey);
    }

}
