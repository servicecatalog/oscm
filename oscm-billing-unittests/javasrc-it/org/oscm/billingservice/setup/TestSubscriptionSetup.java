/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 15.04.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.setup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.oscm.domobjects.Subscription;
import org.oscm.subscriptionservice.local.SubscriptionServiceLocal;
import org.oscm.test.DateTimeHandling;
import org.oscm.test.ejb.TestContainer;
import org.oscm.internal.intf.AccountService;
import org.oscm.internal.intf.EventService;
import org.oscm.internal.intf.ServiceProvisioningService;
import org.oscm.internal.intf.SubscriptionService;
import org.oscm.internal.types.enumtypes.PriceModelType;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.vo.VOBillingContact;
import org.oscm.internal.vo.VOGatheredEvent;
import org.oscm.internal.vo.VOInstanceInfo;
import org.oscm.internal.vo.VOLocalizedText;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.internal.vo.VOParameter;
import org.oscm.internal.vo.VOPaymentInfo;
import org.oscm.internal.vo.VOPriceModel;
import org.oscm.internal.vo.VORoleDefinition;
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOServiceDetails;
import org.oscm.internal.vo.VOSubscription;
import org.oscm.internal.vo.VOSubscriptionDetails;
import org.oscm.internal.vo.VOUda;
import org.oscm.internal.vo.VOUsageLicense;
import org.oscm.internal.vo.VOUser;

/**
 * Setup of subscriptions
 * 
 * @author baumann
 */
public class TestSubscriptionSetup {

    private final TestContainer container;
    private final SubscriptionService subscriptionService;
    private final SubscriptionServiceLocal subServiceLocal;
    private final AccountService accountService;
    private final EventService evMgmt;
    private final ServiceProvisioningService provisioningService;

    public TestSubscriptionSetup(TestContainer container) {
        this.container = container;
        subscriptionService = container.get(SubscriptionService.class);
        subServiceLocal = container.get(SubscriptionServiceLocal.class);
        accountService = container.get(AccountService.class);
        evMgmt = container.get(EventService.class);
        provisioningService = container.get(ServiceProvisioningService.class);
    }

    /**
     * Creates a subscription to the given service for the calling user's
     * organization. The Usage licenses are also created for the given users.
     */
    public VOSubscriptionDetails subscribeToService(
            VOSubscription subscription, VOService service, VOUser user,
            VORoleDefinition role) throws Exception {

        List<VOUsageLicense> users = null;
        if (user != null) {
            users = createUsageLicenceVOList(user, role);
        }

        VOBillingContact voBillingContact = accountService
                .saveBillingContact(newVOBillingContact());
        VOPaymentInfo voPaymentInfo = null;

        if (service.getPriceModel().getType() != PriceModelType.FREE_OF_CHARGE) {
            voPaymentInfo = accountService.getPaymentInfos().get(0);
        } else {
            voBillingContact = null;
        }

        VOSubscription createdSubscription = subscriptionService
                .subscribeToService(subscription, service, users,
                        voPaymentInfo, voBillingContact, new ArrayList<VOUda>());

        return subscriptionService.getSubscriptionDetails(createdSubscription
                .getSubscriptionId());
    }

    /**
     * Creates a subscription to the given service for the calling user's
     * organization. The Usage licenses are also created for the given users.
     */
    public VOSubscriptionDetails subscribeToService(String subscriptionId,
            long unitKey, VOService service, VOUser user, VORoleDefinition role)
            throws Exception {

        return subscribeToService(newVOSubscription(subscriptionId, unitKey), service,
                user, role);
    }

    public VOSubscriptionDetails subscribeToService(String subscriptionId,
            VOService service, VOUser user, VORoleDefinition role)
            throws Exception {

        return subscribeToService(subscriptionId, 0L, service, user, role);
    }

    public VOSubscriptionDetails subscribeToService(String subscriptionId,
            String purchaseOrderNumber, VOService service, VOUser user,
            VORoleDefinition role) throws Exception {
        return subscribeToService(
                newVOSubscription(subscriptionId, purchaseOrderNumber),
                service, user, role);
    }
    
    public VOSubscriptionDetails subscribeToService(long customerAdminKey,
            String subscriptionId, VOService service, VOUser user,
            VORoleDefinition role) throws Exception {
        return subscribeToService(customerAdminKey, subscriptionId, 0L,
                service, user, role);
    }

    public VOSubscriptionDetails subscribeToService(long customerAdminKey,
            String subscriptionId, long unitKey, VOService service,
            VOUser user, VORoleDefinition role) throws Exception {
        container.login(customerAdminKey,
                UserRoleType.ORGANIZATION_ADMIN.name());
        return subscribeToService(subscriptionId, unitKey, service, user, role);
    }

    public VOSubscriptionDetails subscribeToService(long customerAdminKey,
            String subscriptionId, String purchaseOrderNumber,
            VOService service, VOUser user, VORoleDefinition role)
            throws Exception {
        container.login(customerAdminKey,
                UserRoleType.ORGANIZATION_ADMIN.name());
        return subscribeToService(subscriptionId, purchaseOrderNumber, service,
                user, role);
    }

    /**
     * Terminates the given subscription. Since bills may still be open for it,
     * the subscription is not deleted, but marked as <code>DEACTIVATED</code>.
     * Users assigned to the subscription are automatically removed from it.
     */
    public void unsubscribeToService(String subscriptionId) throws Exception {
        subscriptionService.unsubscribeFromService(subscriptionId);
    }

    public void unsubscribeToService(long customerAdminKey,
            String subscriptionId) throws Exception {
        container.login(customerAdminKey,
                UserRoleType.ORGANIZATION_ADMIN.name());
        unsubscribeToService(subscriptionId);
    }

    /**
     * Allows a supplier or reseller to terminate a customer subscription.
     * 
     * @param subscription
     *            the subscription to be terminated
     * @param reason
     *            information on why the subscription is terminated
     */
    public void terminateSubscription(long supplierKey,
            VOSubscription voSubscription, String reason) throws Exception {
        container.login(supplierKey, UserRoleType.SERVICE_MANAGER.name());
        subscriptionService.terminateSubscription(voSubscription, reason);
    }

    public void expireSubscription(VOSubscription voSubscription,
            VOOrganization owningOrg) throws Exception {
        Subscription subscription = new Subscription();
        subscription.setSubscriptionId(voSubscription.getSubscriptionId());
        subscription.setOrganizationKey(owningOrg.getKey());
        subServiceLocal.expireSubscription(subscription);
    }

    public VOSubscriptionDetails upgradeSubscription(
            VOSubscriptionDetails subDetails, VOService service)
            throws Exception {

        VOBillingContact voBillingContact = null;
        VOPaymentInfo voPaymentInfo = null;
        if (service.getPriceModel().getType() != PriceModelType.FREE_OF_CHARGE) {
            voPaymentInfo = accountService.getPaymentInfos().get(0);
            voBillingContact = accountService.getBillingContacts().get(0);
        }

        VOSubscription subscription = subscriptionService.upgradeSubscription(
                subDetails, service, voPaymentInfo, voBillingContact,
                new ArrayList<VOUda>());

        return getSubscriptionDetails(subscription.getSubscriptionId());
    }

    public VOSubscriptionDetails upgradeSubscription(long customerAdminKey,
            VOSubscriptionDetails subDetails, VOService service)
            throws Exception {
        container.login(customerAdminKey,
                UserRoleType.ORGANIZATION_ADMIN.name());
        return upgradeSubscription(subDetails, service);
    }

    public VOSubscriptionDetails copyParametersAndUpgradeSubscription(
            long customerAdminKey, VOSubscriptionDetails subDetails,
            VOService service) throws Exception {
        container.login(customerAdminKey,
                UserRoleType.ORGANIZATION_ADMIN.name());
        copySubscriptionParameterValues(service, subDetails);
        return upgradeSubscription(subDetails, service);
    }

    public VOSubscriptionDetails getSubscriptionDetails(String subscriptionId)
            throws Exception {
        return subscriptionService.getSubscriptionDetails(subscriptionId);
    }

    public VOSubscriptionDetails getSubscriptionDetails(long customerAdminKey,
            String subscriptionId) throws Exception {
        container.login(customerAdminKey,
                UserRoleType.ORGANIZATION_ADMIN.name());
        return getSubscriptionDetails(subscriptionId);
    }

    /**
     * Modifies the given subscription. You can also change parameters and their
     * options defined for the underlying service.
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
     */
    public VOSubscriptionDetails modifySubscription(
            VOSubscription voSubscription, List<VOParameter> parameters)
            throws Exception {
        VOSubscriptionDetails subDetails = subscriptionService
                .modifySubscription(voSubscription, parameters,
                        new ArrayList<VOUda>());
        return subDetails;
    }

    public VOSubscriptionDetails modifySubscription(long customerAdminKey,
            VOSubscription voSubscription, List<VOParameter> parameters)
            throws Exception {
        container.login(customerAdminKey,
                UserRoleType.ORGANIZATION_ADMIN.name());
        return modifySubscription(voSubscription, parameters);
    }

    private VOParameter modifySubscriptionParameter(VOSubscriptionDetails sub,
            String parameterId, String newValue) {
        VOParameter parameter = VOServiceFactory.getParameter(
                sub.getSubscribedService(), parameterId);
        parameter.setValue(newValue);
        return parameter;
    }

    public VOSubscriptionDetails modifyParameter(String modificationTime,
            VOSubscriptionDetails subDetails, String parameterId,
            String newParameterValue) throws Exception {

        long modTime = DateTimeHandling.calculateMillis(modificationTime);
        return modifyParameterForSubscription(subDetails, modTime, parameterId,
                newParameterValue);
    }

    public VOSubscriptionDetails modifyParameterForSubscription(
            VOSubscriptionDetails subDetails, long usageModificationTime,
            String parameterId, String newParameterValue) throws Exception {
        BillingIntegrationTestBase
                .setDateFactoryInstance(usageModificationTime);
        VOParameter modifiedPar = modifySubscriptionParameter(subDetails,
                parameterId, newParameterValue);
        subDetails = modifySubscription(subDetails,
                Arrays.asList(new VOParameter[] { modifiedPar }));
        return subDetails;
    }

    /**
     * Copy all parameter values from the subscribed service to the new service
     */
    public void copySubscriptionParameterValues(VOService newService,
            VOSubscriptionDetails subDetails) {
        Map<String, VOParameter> serviceParameters = VOServiceFactory
                .getParameters(newService);
        Map<String, VOParameter> subscriptionParameters = VOServiceFactory
                .getParameters(subDetails.getSubscribedService());
        for (VOParameter serviceParameter : serviceParameters.values()) {
            VOParameter subscriptionParameter = subscriptionParameters
                    .get(serviceParameter.getParameterDefinition()
                            .getParameterId());
            if (subscriptionParameter != null) {
                serviceParameter.setValue(subscriptionParameter.getValue());
            }
        }
    }

    public void savePriceModelForSubscription(long supplierKey,
            VOSubscriptionDetails subDetails, VOPriceModel newSubPriceModel,
            VOOrganization customerOrg) throws Exception {
        container.login(supplierKey, UserRoleType.SERVICE_MANAGER.name());
        VOServiceDetails forSubscription = provisioningService
                .getServiceForSubscription(customerOrg,
                        subDetails.getSubscriptionId());
        provisioningService.savePriceModelForSubscription(forSubscription,
                newSubPriceModel);
    }

    public void recordEventForSubscription(VOSubscriptionDetails subscription,
            long occurrenceTime, String eventId, long multiplier)
            throws Exception {
        VOGatheredEvent evt = newVOGatheredEvent(eventId, System.nanoTime()
                + eventId, "anyUser", occurrenceTime, multiplier);
        evMgmt.recordEventForSubscription(subscription.getKey(), evt);
    }

    public void recordEventForSubscription(long techManagerKey,
            VOSubscriptionDetails subscription, long occurrenceTime,
            String eventId, long multiplier) throws Exception {
        container.login(techManagerKey, UserRoleType.TECHNOLOGY_MANAGER.name());
        recordEventForSubscription(subscription, occurrenceTime, eventId,
                multiplier);
    }

    public void recordEventForSubscription(long techManagerKey,
            VOSubscriptionDetails subscription, String occurrenceTime,
            String eventId, long multiplier) throws Exception {
        container.login(techManagerKey, UserRoleType.TECHNOLOGY_MANAGER.name());
        long millis = DateTimeHandling.calculateMillis(occurrenceTime);
        recordEventForSubscription(subscription, millis, eventId, multiplier);
    }

    /**
     * Updates the DateFactoryInstance to the provided modification time and
     * adds the user to the subscription.
     */
    public VOSubscriptionDetails addUser(String modificationTime,
            String subscriptionId, VOUser user, VORoleDefinition role)
            throws Exception {
        BillingIntegrationTestBase.setDateFactoryInstance(modificationTime);
        List<VOUsageLicense> usageLicenses = createUsageLicenceVOList(user,
                role);
        subscriptionService.addRevokeUser(subscriptionId, usageLicenses, null);
        return subscriptionService.getSubscriptionDetails(subscriptionId);
    }

    public VOSubscriptionDetails addUser(VOUser user, VORoleDefinition role,
            String subscriptionId) throws Exception {
        List<VOUsageLicense> usageLicenses = createUsageLicenceVOList(user,
                role);
        subscriptionService.addRevokeUser(subscriptionId, usageLicenses, null);
        return subscriptionService.getSubscriptionDetails(subscriptionId);
    }

    /**
     * Modify the role of an user, that is assigned to a subscription
     * 
     * @param license
     *            the usage license of the assigned user
     * @param newRole
     *            the new user role
     * @param subscriptionId
     *            the subscription id
     */
    public VOSubscriptionDetails modifyUserRole(VOUsageLicense license,
            VORoleDefinition newRole, String subscriptionId) throws Exception {
        license.setRoleDefinition(newRole);
        subscriptionService.addRevokeUser(subscriptionId,
                Arrays.asList(new VOUsageLicense[] { license }), null);
        return subscriptionService.getSubscriptionDetails(subscriptionId);
    }

    public VOSubscriptionDetails revokeUser(VOUser user, String subscriptionId)
            throws Exception {
        List<VOUser> revokeUsers = Arrays.asList(new VOUser[] { user });
        subscriptionService.addRevokeUser(subscriptionId, null, revokeUsers);
        return subscriptionService.getSubscriptionDetails(subscriptionId);
    }

    private VOSubscription newVOSubscription(String subscriptionId, long unitKey) {
        VOSubscription subscription = new VOSubscription();
        subscription.setSubscriptionId(subscriptionId);
        subscription.setUnitKey(unitKey);
        return subscription;
    }

    private VOSubscription newVOSubscription(String subscriptionId,
            String purchaseOrderNumber) {
        VOSubscription subscription = new VOSubscription();
        subscription.setSubscriptionId(subscriptionId);
        subscription.setPurchaseOrderNumber(purchaseOrderNumber);
        subscription.setUnitKey(0L);
        return subscription;
    }

    private VOUsageLicense newVOUsageLicense(VOUser user, VORoleDefinition role) {
        VOUsageLicense usageLicence = new VOUsageLicense();
        usageLicence.setUser(user);
        usageLicence.setRoleDefinition(role);
        return usageLicence;
    }

    private List<VOUsageLicense> createUsageLicenceVOList(VOUser user,
            VORoleDefinition role) {
        List<VOUsageLicense> users = new ArrayList<VOUsageLicense>();
        users.add(newVOUsageLicense(user, role));
        return users;
    }

    private VOBillingContact newVOBillingContact() {
        VOBillingContact voBillingContact = new VOBillingContact();
        voBillingContact.setAddress("str 123");
        voBillingContact.setCompanyName("fujitsu");
        voBillingContact.setEmail("test@mail.de");
        voBillingContact.setOrgAddressUsed(true);
        voBillingContact.setId("billingId" + TestBasicSetup.createUniqueKey());
        return voBillingContact;
    }

    private VOGatheredEvent newVOGatheredEvent(String eventId, String uniqueId,
            String actor, long occurrenceTime, long multiplier)
            throws Exception {
        VOGatheredEvent e = new VOGatheredEvent();
        e.setEventId(eventId);
        e.setActor(actor);
        e.setOccurrenceTime(occurrenceTime);
        e.setMultiplier(multiplier);
        e.setUniqueId(uniqueId);
        return e;
    }

    public VOSubscriptionDetails completeAsyncModifySubscription(
            long supplierAdminKey, VOUser customerAdmin,
            VOSubscriptionDetails subDetails) throws Exception {
        container.login(supplierAdminKey,
                UserRoleType.TECHNOLOGY_MANAGER.name());
        VOInstanceInfo instance = getDefaultInstance(
                subDetails.getSubscriptionId(), subDetails.getServiceBaseURL());

        subscriptionService.completeAsyncModifySubscription(
                subDetails.getSubscriptionId(),
                customerAdmin.getOrganizationId(), instance);

        return getSubscriptionDetails(customerAdmin.getKey(),
                subDetails.getSubscriptionId());
    }

    protected VOInstanceInfo getDefaultInstance(String subscriptionId,
            String baseUrl) {
        VOInstanceInfo instance = new VOInstanceInfo();
        instance.setInstanceId(subscriptionId);
        instance.setBaseUrl(baseUrl);
        instance.setAccessInfo("");
        instance.setLoginPath("/login");
        return instance;
    }

    public VOSubscriptionDetails completeAsyncSubscription(
            long supplierAdminKey, VOUser customerAdmin,
            VOSubscriptionDetails subDetails) throws Exception {
        container.login(supplierAdminKey,
                UserRoleType.TECHNOLOGY_MANAGER.name());
        VOInstanceInfo instance = getDefaultInstance(
                subDetails.getSubscriptionId(), subDetails.getServiceBaseURL());

        subscriptionService.completeAsyncSubscription(
                subDetails.getSubscriptionId(),
                customerAdmin.getOrganizationId(), instance);

        return getSubscriptionDetails(customerAdmin.getKey(),
                subDetails.getSubscriptionId());
    }

    public VOSubscriptionDetails completeAsyncUpgradeSubscription(
            long supplierAdminKey, VOUser customerAdmin,
            VOSubscriptionDetails subDetails) throws Exception {
        container.login(supplierAdminKey,
                UserRoleType.TECHNOLOGY_MANAGER.name());
        VOInstanceInfo instance = getDefaultInstance(
                subDetails.getSubscriptionId(), subDetails.getServiceBaseURL());

        subscriptionService.completeAsyncUpgradeSubscription(
                subDetails.getSubscriptionId(),
                customerAdmin.getOrganizationId(), instance);

        return getSubscriptionDetails(customerAdmin.getKey(),
                subDetails.getSubscriptionId());
    }

    public void abortAsyncModifySubscription(long supplierAdminKey,
            String subscriptionId, String organizationId) throws Exception {
        container.login(supplierAdminKey,
                UserRoleType.TECHNOLOGY_MANAGER.name());
        subscriptionService.abortAsyncModifySubscription(subscriptionId,
                organizationId, defaultReason());
    }

    public void abortAsyncSubscription(long supplierAdminKey,
            String subscriptionId, String organizationId) throws Exception {
        container.login(supplierAdminKey,
                UserRoleType.TECHNOLOGY_MANAGER.name());
        subscriptionService.abortAsyncSubscription(subscriptionId,
                organizationId, defaultReason());
    }

    public void abortAsyncUpgradeSubscription(long supplierAdminKey,
            String subscriptionId, String organizationId) throws Exception {
        container.login(supplierAdminKey,
                UserRoleType.TECHNOLOGY_MANAGER.name());
        subscriptionService.abortAsyncUpgradeSubscription(subscriptionId,
                organizationId, defaultReason());
    }

    protected List<VOLocalizedText> defaultReason() {
        List<VOLocalizedText> reason = new ArrayList<VOLocalizedText>();
        reason.add(new VOLocalizedText("en", "abort subcription for IT tests"));
        return reason;
    }

    public VOSubscriptionDetails modifySubscriptionPaymentData(
            long customerAdminKey, VOSubscription subscription,
            VOBillingContact billingContact, VOPaymentInfo paymentInfo)
            throws Exception {
        container.login(customerAdminKey,
                UserRoleType.ORGANIZATION_ADMIN.name());
        return subscriptionService.modifySubscriptionPaymentData(subscription,
                billingContact, paymentInfo);
    }

    public VOSubscriptionDetails modifySubscriptionPaymentData(
            long customerAdminKey, VOSubscription subscription,
            VOBillingContact billingContact) throws Exception {
        return modifySubscriptionPaymentData(customerAdminKey, subscription,
                billingContact, accountService.getPaymentInfos().get(0));
    }

}
