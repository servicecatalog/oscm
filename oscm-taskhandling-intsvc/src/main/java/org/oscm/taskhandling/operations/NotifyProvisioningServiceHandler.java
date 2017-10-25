/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.taskhandling.operations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.RoleDefinition;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.UsageLicense;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.interceptor.DateFactory;
import org.oscm.taskhandling.local.TaskMessage;
import org.oscm.taskhandling.payloads.NotifyProvisioningServicePayload;
import org.oscm.taskhandling.payloads.SendMailPayload;
import org.oscm.taskhandling.payloads.TaskPayload;
import org.oscm.types.constants.Configuration;
import org.oscm.types.enumtypes.EmailType;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.types.enumtypes.ProvisioningType;
import org.oscm.types.exceptions.UserAlreadyAssignedException;
import org.oscm.validator.ADMValidator;
import org.oscm.internal.types.enumtypes.ConfigurationKey;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.types.exception.MailOperationException;
import org.oscm.internal.types.exception.TechnicalServiceNotAliveException;
import org.oscm.internal.types.exception.TechnicalServiceOperationException;
import org.oscm.provisioning.data.User;

/**
 * Asynchronous notification of ProvisioningService.
 * 
 * @author afschar
 * 
 */
public class NotifyProvisioningServiceHandler extends TaskHandler {

    public static final String KEY_PAIR_NAME = "Key pair name";
    public static final String AMAZONAWS_COM = "amazonaws.com";

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(NotifyProvisioningServiceHandler.class);
    private long delay = 10000;

    private NotifyProvisioningServicePayload payload;

    public NotifyProvisioningServiceHandler() {
        super();
    }

    NotifyProvisioningServiceHandler(long delay) {
        super();
        this.delay = delay;
    }

    @Override
    public void execute() throws Exception {
        Thread.sleep(delay);
        Subscription subscription = serviceFacade.getDataService()
                .getReference(Subscription.class, payload.getTkey());
        if (payload.isDeactivate()) {
            serviceFacade.getApplicationService().deactivateInstance(
                    subscription);
        } else {
            createAndAutoAssignUsers(subscription);
        }
    }

    private void createAndAutoAssignUsers(Subscription subscription)
            throws UserAlreadyAssignedException,
            TechnicalServiceNotAliveException,
            TechnicalServiceOperationException {

        if (isAutoAssignEnabled(subscription)) {

            PlatformUser owner = subscription.getOwner();
            RoleDefinition roleDef = getRoleDefToAssign(subscription);

            List<UsageLicense> usageLicenses = getUsageLicensesForCreation(
                    owner, roleDef);

            User[] createdUsers = serviceFacade.getApplicationService()
                    .createUsers(subscription, usageLicenses);

            serviceFacade.getSubscriptionService().addUserToSubscription(
                    subscription, owner, roleDef);

            mapApplicationUserIdToLicense(subscription.getUsageLicenses(),
                    createdUsers);

            List<UsageLicense> licenses = subscription.getUsageLicenses();
            sendToSubscriptionAddedMail(subscription, licenses);
        }
    }

    private List<UsageLicense> getUsageLicensesForCreation(PlatformUser owner,
            RoleDefinition roleDef) {
        List<UsageLicense> usageLicenses = new ArrayList<UsageLicense>();
        UsageLicense license = new UsageLicense();
        license.setRoleDefinition(roleDef);
        license.setAssignmentDate(DateFactory.getInstance()
                .getTransactionTime());
        license.setUser(owner);
        usageLicenses.add(license);
        return usageLicenses;
    }

    /**
     * Send the subscription created email to the users assigned to the
     * subscription. The mail also includes the access information.
     * 
     * @param newSub
     *            the created subscription to get the id and access info (url or
     *            descriptive text) from
     * @param addedUserLicenses
     *            the usage licenses of the users added to the subscription
     */
    private void sendToSubscriptionAddedMail(Subscription subscription,
            List<UsageLicense> usageLicenses) {

        if (subscription.getStatus() != SubscriptionStatus.ACTIVE) {
            return;
        }
        EmailType emailType = useAccessInfo(subscription) ? EmailType.SUBSCRIPTION_USER_ADDED_ACCESSTYPE_DIRECT
                : EmailType.SUBSCRIPTION_USER_ADDED;

        Long marketplaceKey = null;
        if (subscription.getMarketplace() != null) {
            marketplaceKey = Long.valueOf(subscription.getMarketplace()
                    .getKey());
        }

        SendMailPayload payload = new SendMailPayload();
        for (UsageLicense usageLicense : usageLicenses) {
            String accessInfo = getAccessInfo(subscription,
                    usageLicense.getUser());

            if (isUsableAWSAccessInfo(accessInfo)) {
                payload.addMailObjectForUser(usageLicense.getUser().getKey(),
                        EmailType.SUBSCRIPTION_USER_ADDED_ACCESSINFO,
                        new Object[] { subscription.getSubscriptionId(),
                                getPublicDNS(accessInfo),
                                getIPAddress(accessInfo),
                                getKeyPairName(accessInfo) }, marketplaceKey);
            } else {
                payload.addMailObjectForUser(usageLicense.getUser().getKey(),
                        emailType,
                        new Object[] { subscription.getSubscriptionId(),
                                accessInfo }, marketplaceKey);
            }
        }
        TaskMessage message = new TaskMessage(SendMailHandler.class, payload);
        serviceFacade.getTaskQueueService().sendAllMessages(
                Arrays.asList(message));
    }

    boolean isAWSAccessInfo(String accessInfo) {
        try {
            String subAccessInfo = accessInfo.substring(
                    accessInfo.indexOf(":"), accessInfo.indexOf(KEY_PAIR_NAME));
            return subAccessInfo.contains(AMAZONAWS_COM);
        } catch (StringIndexOutOfBoundsException e) {
            return false;
        }
    }

    boolean hasNullAccessInfo(String accessInfo) {
        if (getPublicDNS(accessInfo).isEmpty()
                || getIPAddress(accessInfo).isEmpty()
                || getKeyPairName(accessInfo).isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    boolean isUsableAWSAccessInfo(String accessInfo) {
        if(accessInfo == null || accessInfo.isEmpty()){
            return false;
        }else{
        return isAWSAccessInfo(accessInfo) && !hasNullAccessInfo(accessInfo);}
    }

    String getPublicDNS(String accessInfo) {
        try {
            String publicDNS = accessInfo.substring(0,
                    accessInfo.indexOf(KEY_PAIR_NAME));
            return publicDNS;
        } catch (StringIndexOutOfBoundsException e) {
            return "";
        }
    }

    String getIPAddress(String accessInfo) {
        try {
            String ipAddress = accessInfo
                    .substring(accessInfo.indexOf("-"), accessInfo.indexOf("."))
                    .substring(1).replace('-', '.');
            return ipAddress;
        } catch (StringIndexOutOfBoundsException e) {
            return "";
        }
    }

    String getKeyPairName(String accessInfo) {
        try {
            String keyPairName = accessInfo.substring(
                    accessInfo.indexOf(KEY_PAIR_NAME), accessInfo.length());
            return keyPairName;
        } catch (StringIndexOutOfBoundsException e) {
            return "";
        }
    }

    /**
     * Maps the application user id back to the usage license and sets the
     * affected usage license dirty
     * 
     * @param licenses
     *            the usage licenses to update
     * @param createdUsers
     *            the user array containing the application user id
     */
    private void mapApplicationUserIdToLicense(List<UsageLicense> licenses,
            User[] createdUsers) {
        if (createdUsers == null) {
            return;
        }
        String applicationUserId;
        for (UsageLicense license : licenses) {
            for (User createdUser : createdUsers) {
                applicationUserId = createdUser.getApplicationUserId();
                if (createdUser.getUserId().equals(
                        license.getUser().getUserId())
                        && applicationUserId != null) {
                    license.setApplicationUserId(applicationUserId);
                    break;
                }
            }
        }
    }

    String getAccessInfo(Subscription subscription, PlatformUser user) {
        String accessInfo = null;
        if (useAccessInfo(subscription)) {
            accessInfo = subscription.getAccessInfo();
            if (accessInfo == null) {
                accessInfo = serviceFacade
                        .getLocalizerService()
                        .getLocalizedTextFromDatabase(
                                user.getLocale(),
                                subscription.getProduct().getTechnicalProduct()
                                        .getKey(),
                                LocalizedObjectTypes.TEC_PRODUCT_LOGIN_ACCESS_DESC);
                if (accessInfo != null) {
                    accessInfo = accessInfo.replaceAll("<p>", "\n");
                    accessInfo = accessInfo.replaceAll("</p>", "\n");
                }
            }
        } else {
            accessInfo = getSubscriptionUrl(subscription);
        }
        if (accessInfo == null) {
            accessInfo = "";
        }
        return accessInfo;
    }

    /**
     * Gets the URL to access the subscription.
     * 
     * @param subscription
     *            the subscription for which we want to know the URL
     * @return the URL to access the subscription
     */
    String getSubscriptionUrl(Subscription subscription) {
        StringBuffer url = new StringBuffer();
        String baseUrl = serviceFacade.getConfigurationService().getBaseURL();
        String technicalProductBaseUrl = subscription.getProduct()
                .getTechnicalProduct().getBaseURL();

        if (ADMValidator.isHttpsScheme(technicalProductBaseUrl)) {
            baseUrl = serviceFacade
                    .getConfigurationService()
                    .getConfigurationSetting(ConfigurationKey.BASE_URL_HTTPS,
                            Configuration.GLOBAL_CONTEXT).getValue();
        }

        url.append(baseUrl);
        if (url.length() == 0 || url.charAt(url.length() - 1) != '/') {
            url.append('/');
        }
        url.append("opt/");
        url.append(Long.toHexString(subscription.getKey()));
        url.append('/');
        return url.toString();
    }

    private boolean useAccessInfo(Subscription subscription) {
        ServiceAccessType accessType = subscription.getProduct()
                .getTechnicalProduct().getAccessType();
        return accessType == ServiceAccessType.DIRECT
                || accessType == ServiceAccessType.USER;
    }

    /**
     * Log the exception and try to send a mail to the user that triggered the
     * operation
     */
    @Override
    public void handleError(Exception cause) throws Exception {
        logger.logWarn(Log4jLogger.SYSTEM_LOG, cause,
                LogMessageIdentifier.ERROR_NOTIFY_PROVISIONING_SERVICE_FAILED,
                payload.getInfo());

        if (!payload.isDeactivate()) {
            Subscription subscription = serviceFacade.getDataService()
                    .getReference(Subscription.class, payload.getTkey());
            informOwnerAndTechProviders(subscription,
                    EmailType.NOTIFY_PROVISIONING_SERVICE_FAILED);
        }
    }

    private void informOwnerAndTechProviders(Subscription subscription,
            EmailType type) throws MailOperationException {
        PlatformUser owner = subscription.getOwner();
        Object[] params = new Object[] { subscription.getSubscriptionId(),
                owner.getUserId() };

        // Inform the subscribing user
        serviceFacade.getCommunicationService().sendMail(owner, type, params,
                null);

        // Inform technology managers
        Organization techProvider = subscription.getProduct()
                .getTechnicalProduct().getOrganization();
        if (!techProvider.getOrganizationId().equals(
                owner.getOrganization().getOrganizationId())) {
            serviceFacade.getCommunicationService().sendMail(techProvider,
                    EmailType.NOTIFY_PROVISIONING_SERVICE_FAILED, params, null);
        }
    }

    @Override
    void setPayload(TaskPayload payload) {
        this.payload = (NotifyProvisioningServicePayload) payload;
    }

    private boolean isAutoAssignEnabled(Subscription subscription) {
        Product product = subscription.getProduct();
        return product.isAutoAssignUserEnabled().booleanValue()
                && ProvisioningType.ASYNCHRONOUS == product
                        .getTechnicalProduct().getProvisioningType();
    }

    private RoleDefinition getRoleDefToAssign(Subscription subscription) {
        List<RoleDefinition> roleDefinitions = subscription.getProduct()
                .getTechnicalProduct().getRoleDefinitions();

        if (roleDefinitions == null || roleDefinitions.isEmpty()) {
            return null;
        }
        return roleDefinitions.get(0);

    }
}
