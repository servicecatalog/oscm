/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 17.06.2010                                                      
 *                                                                              
 *******************************************************************************/

package internal;

import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.jws.WebService;

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
 * This is a stub implementation of the {@link NotificationService} as the Metro
 * jax-ws tools do not allow to generate WSDL files from the service interfaces.
 * 
 * <p>
 * <b>WARNING:</b> Do not use this class, all methods simply throw an
 * {@link UnsupportedOperationException}!
 * </p>
 * 
 * @author Mike J&auml;ger
 * 
 */
@WebService(serviceName = "NotificationService", targetNamespace = "http://oscm.org/xsd", portName = "StubServicePort", endpointInterface = "org.oscm.notification.intf.NotificationService")
public class NotificationServiceImpl implements NotificationService {

    @Override
    public void billingPerformed(String xmlBillingData) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onActivateProduct(VOTriggerProcess triggerProcess,
            VOService product) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onAddRevokeUser(VOTriggerProcess triggerProcess,
            String subscriptionId, List<VOUsageLicense> usersToBeAdded,
            List<VOUser> usersToBeRevoked) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onAddSupplier(VOTriggerProcess triggerProcess, String supplierId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onDeactivateProduct(VOTriggerProcess triggerProcess,
            VOService product) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onModifySubscription(VOTriggerProcess triggerProcess,
            VOSubscription subscription, List<VOParameter> modifiedParameters) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onRegisterCustomer(VOTriggerProcess triggerProcess,
            VOOrganization organization, VOUserDetails user,
            Properties organizationProperties) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onRemoveSupplier(VOTriggerProcess triggerProcess,
            String supplierId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onSaveDefaultPaymentConfiguration(
            VOTriggerProcess triggerProcess,
            Set<VOPaymentType> defaultConfiguration) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onSaveCustomerPaymentConfiguration(
            VOTriggerProcess triggerProcess,
            VOOrganizationPaymentConfiguration customerConfigurations) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onSubscribeToProduct(VOTriggerProcess triggerProcess,
            VOSubscription subscription, VOService product,
            List<VOUsageLicense> users) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onUnsubscribeFromProduct(VOTriggerProcess triggerProcess,
            String subId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onUpgradeSubscription(VOTriggerProcess triggerProcess,
            VOSubscription current, VOService newProduct) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onSaveServicePaymentConfiguration(
            VOTriggerProcess triggerProcess,
            VOServicePaymentConfiguration serviceConfiguration) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onSaveServiceDefaultPaymentConfiguration(
            VOTriggerProcess triggerProcess,
            Set<VOPaymentType> defaultConfiguration) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onSubscriptionCreation(VOTriggerProcess triggerProcess,
            VOService service, List<VOUsageLicense> users,
            VONotification notification) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onSubscriptionModification(VOTriggerProcess triggerProcess,
            List<VOParameter> parameters, VONotification notification) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onSubscriptionTermination(VOTriggerProcess triggerProcess,
            VONotification notification) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onRegisterUserInOwnOrganization(
            VOTriggerProcess triggerProcess, VOUserDetails user,
            List<UserRoleType> roles, String marketplaceId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onCancelAction(long actionKey) {
        throw new UnsupportedOperationException();
    }
}
