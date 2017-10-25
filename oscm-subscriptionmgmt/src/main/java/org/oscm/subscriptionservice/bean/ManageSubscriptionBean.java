/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 01.09.2014      
 *  
 *  author cmin
 *                                                                              
 *******************************************************************************/
package org.oscm.subscriptionservice.bean;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.LocalBean;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.applicationservice.local.ApplicationServiceLocal;
import org.oscm.domobjects.OperationParameter;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.TechnicalProductOperation;
import org.oscm.domobjects.UsageLicense;
import org.oscm.domobjects.UserGroup;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.permission.PermissionCheck;
import org.oscm.string.Strings;
import org.oscm.subscriptionservice.auditlog.SubscriptionAuditLogCollector;
import org.oscm.types.enumtypes.EmailType;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.usergroupservice.bean.UserGroupServiceLocalBean;
import org.oscm.validation.ArgumentValidator;
import org.oscm.validator.BLValidator;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.types.enumtypes.UnitRoleType;
import org.oscm.internal.types.exception.IllegalArgumentException;
import org.oscm.internal.types.exception.MailOperationException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.TechnicalServiceNotAliveException;
import org.oscm.internal.types.exception.TechnicalServiceOperationException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.operation.data.OperationResult;

/**
 * No interface view bean implementation of subscriptions functionality.
 * 
 * @author cmin
 */
@LocalBean
public class ManageSubscriptionBean extends SubscriptionUtilBean {

    protected static final List<SubscriptionStatus> VISIBLE_SUBSCRIPTION_STATUS = Arrays
            .asList(SubscriptionStatus.ACTIVE, SubscriptionStatus.EXPIRED,
                    SubscriptionStatus.PENDING, SubscriptionStatus.SUSPENDED);

    private static final Log4jLogger LOG = LoggerFactory
            .getLogger(ManageSubscriptionBean.class);

    @EJB(beanInterface = ApplicationServiceLocal.class)
    protected ApplicationServiceLocal appManager;

    @EJB(beanInterface = LocalizerServiceLocal.class)
    protected LocalizerServiceLocal localizer;

    @EJB(beanInterface = UserGroupServiceLocalBean.class)
    UserGroupServiceLocalBean userGroupService = new UserGroupServiceLocalBean();

    @EJB
    protected SubscriptionAuditLogCollector audit;

    public OperationResult executeServiceOperation(Subscription subscription,
            TechnicalProductOperation operation,
            Map<String, String> parameters, String transactionid)
            throws OperationNotPermittedException,
            TechnicalServiceNotAliveException,
            TechnicalServiceOperationException, ValidationException {

        String userId = validateForServiceOperation(subscription, operation);
        validateMandatoryParametersAreSet(operation, parameters);

        OperationResult result = appManager.executeServiceOperation(userId,
                subscription, transactionid, operation, parameters);

        audit.executeService(dataManager, subscription, operation, parameters);

        return result;
    }

    /**
     * Checks that all mandatory parameters of the passed
     * {@link TechnicalProductOperation} are contained with a non empty value in
     * the passed {@link Map}.
     */
    void validateMandatoryParametersAreSet(TechnicalProductOperation tpo,
            Map<String, String> parameters) throws ValidationException {
        List<OperationParameter> list = tpo.getParameters();
        List<String> missingParams = new LinkedList<>();
        for (OperationParameter op : list) {
            if (!op.isMandatory()) {
                continue;
            }
            String value = parameters.get(op.getId());
            if (Strings.isEmpty(value)) {
                missingParams.add(op.getId());
            }
        }
        if (!missingParams.isEmpty()) {
            String member = missingParams.toString();
            member = member.substring(1, member.length() - 1);
            ValidationException ve = new ValidationException(
                    ValidationException.ReasonEnum.REQUIRED, member,
                    new Object[] { member });
            LOG.logWarn(Log4jLogger.SYSTEM_LOG, ve,
                    LogMessageIdentifier.WARN_VALIDATION_FAILED);
            throw ve;
        }
    }

    public Map<String, List<String>> getOperationParameterValues(
            Subscription subscription, TechnicalProductOperation operation)
            throws TechnicalServiceNotAliveException,
            TechnicalServiceOperationException, OperationNotPermittedException {
        String userId = validateForServiceOperation(subscription, operation);

        return appManager.getOperationParameterValues(userId, operation,
                subscription);
    }

    String validateForServiceOperation(Subscription subscription,
            TechnicalProductOperation operation)
            throws OperationNotPermittedException {
        ArgumentValidator.notNull("subscription", subscription);
        ArgumentValidator.notNull("operation", operation);

        validateServiceOperation(subscription, operation);

        return validateUsageLicence(subscription);
    }

    private void validateServiceOperation(Subscription sub,
            TechnicalProductOperation op) throws OperationNotPermittedException {
        if (sub.getProduct().getTechnicalProduct().getKey() != op
                .getTechnicalProduct().getKey()) {
            String message = "Service operation '%s' is not available for subscription '%s'.";
            OperationNotPermittedException onp = new OperationNotPermittedException(
                    String.format(message, op.getOperationId(),
                            sub.getSubscriptionId()));
            LOG.logWarn(Log4jLogger.SYSTEM_LOG, onp,
                    LogMessageIdentifier.WARN_SERVICE_OPERATION_NOT_AVAILABLE,
                    op.getOperationId(), sub.getSubscriptionId());
            sessionCtx.setRollbackOnly();
            throw onp;
        }
    }

    private String validateUsageLicence(Subscription sub)
            throws OperationNotPermittedException {
        PlatformUser user = dataManager.getCurrentUser();
        UsageLicense lic = sub.getUsageLicenseForUser(user);
        if (lic == null) {
            String message = "User '%s' is not assigned to subscription '%s'.";
            OperationNotPermittedException onp = new OperationNotPermittedException(
                    String.format(message, user.getUserId(),
                            sub.getSubscriptionId()));
            LOG.logWarn(
                    Log4jLogger.SYSTEM_LOG,
                    onp,
                    LogMessageIdentifier.WARN_USER_NOT_ASSINGED_TO_SUBSCRIPTION,
                    Long.toString(user.getKey()), Long.toString(sub.getKey()));
            throw onp;
        }
        String userId = lic.getApplicationUserId();
        if (Strings.isEmpty(userId)) {
            userId = user.getUserId();
        }
        return userId;
    }

    public void reportIssue(String subscriptionId, String subject,
            String issueText) throws IllegalArgumentException,
            ObjectNotFoundException, OperationNotPermittedException,
            MailOperationException, ValidationException {

        // validate parameters
        ArgumentValidator.notNull("subscriptionId", subscriptionId);
        ArgumentValidator.notNull("subject", subject);
        ArgumentValidator.notNull("issueText", issueText);

        Subscription sub = checkSubscriptionOwner(subscriptionId, 0);

        BLValidator.isSubjectOfSupportEmail("subject", subject, true);
        BLValidator.isContentOfSupportEmail("issueText", issueText, true);

        // check the current user is customer of subscription
        PlatformUser user = dataManager.getCurrentUser();
        Organization customer = user.getOrganization();

        PermissionCheck.owns(sub, customer, LOG);

        // Get the support email. If the product vendor is a broker, then the
        // support email is the supplier's email!
        Product product = sub.getProduct();
        Organization vendor = product.getVendor();
        if (vendor.hasRole(OrganizationRoleType.BROKER)) {
            vendor = product.getProductTemplate().getVendor();
        }

        String mailAddress = vendor.getSupportEmail();
        if (mailAddress == null || mailAddress.trim().isEmpty()) {
            mailAddress = vendor.getEmail();
        }

        Object[] params = getServiceTicketParameters(customer, sub, subject,
                issueText);

        // send mail
        commService.sendMail(mailAddress, EmailType.SUPPORT_ISSUE, params,
                sub.getMarketplace(), vendor.getLocale());

        audit.reportIssueOperation(dataManager, sub, subject);
    }

    private Object[] getServiceTicketParameters(Organization customer,
            Subscription sub, String subject, String issueText) {
        // get the parameters for mail content
        String customerName = customer.getName();
        if (customerName == null) {
            customerName = "-"; // NON-NLS-1$
        }
        String customerID = String.valueOf(customer.getOrganizationId());
        String subscriptionName = sub.getSubscriptionId();
        String subscriptionID = sub.getProductInstanceId();
        if (subscriptionID == null) {
            subscriptionID = "-"; // NON-NLS-1$
        }
        final LocalizerFacade facade = new LocalizerFacade(localizer,
                dataManager.getCurrentUser().getLocale());
        String marketplaceServiceName = facade.getText(sub.getProduct()
                .getTemplate().getKey(),
                LocalizedObjectTypes.PRODUCT_MARKETING_NAME);
        if (marketplaceServiceName == null) {
            marketplaceServiceName = ""; // NON-NLS-1$
        }
        String marketplaceServiceID = String.valueOf(sub.getProduct()
                .getTemplate().getProductId());
        String technicalServiceName = sub.getProduct().getTechnicalProduct()
                .getTechnicalProductId();
        String technicalServiceVersion = sub.getProduct().getTechnicalProduct()
                .getProvisioningVersion();
        if (technicalServiceVersion == null) {
            technicalServiceVersion = ""; // NON-NLS-1$
        }
        String technicalServiceBuildID = sub.getProduct().getTechnicalProduct()
                .getTechnicalProductBuildId();
        if (technicalServiceBuildID == null) {
            technicalServiceBuildID = "-"; // NON-NLS-1$
        }
        return new Object[] { subject, customerName, customerID,
                subscriptionName, subscriptionID, marketplaceServiceName,
                marketplaceServiceID, technicalServiceName,
                technicalServiceVersion, technicalServiceBuildID, issueText };
    }

    /**
     * Only ORGANIZATION_ADMIN and SUBSCRIPTION_MANAGER (must be the
     * subscription owner) have the right to proceed
     * 
     * @param subscriptionId
     *            the subscription to check the owner
     * @param subKey
     *            the subscription key, if not present 0.
     */
    protected Subscription checkSubscriptionOwner(String subscriptionId,
            long subKey) throws OperationNotPermittedException,
            ObjectNotFoundException {

        Subscription sub = loadSubscription(subscriptionId, subKey);
        PlatformUser currentUser = dataManager.getCurrentUser();
        List<UserGroup> administratedUserGroups = userGroupService
                .getUserGroupsForUserWithRole(currentUser.getKey(),
                        UnitRoleType.ADMINISTRATOR.getKey());
        PermissionCheck.owns(sub, currentUser, administratedUserGroups, LOG);
        return sub;
    }

    /**
     * Load the subscription object. If the key is set we use it to load the
     * object otherwise we use the subscription identifier with the current
     * organization as context.
     * 
     * @param subscriptionId
     * @param subKey
     * @return the subscription object
     * @throws ObjectNotFoundException
     */
    protected Subscription loadSubscription(String subscriptionId, long subKey)
            throws ObjectNotFoundException {
        Subscription result = null;

        if (subKey != 0) {
            result = dataManager.getReference(Subscription.class, subKey);
        } else {
            Subscription sub = new Subscription();
            String trimmedSubId = subscriptionId == null ? null
                    : subscriptionId.trim();
            sub.setSubscriptionId(trimmedSubId);
            sub.setOrganizationKey(dataManager.getCurrentUser()
                    .getOrganization().getKey());
            result = (Subscription) dataManager.getReferenceByBusinessKey(sub);
        }
        return result;
    }

    protected void suspend(Subscription subscription) {
        subscription.setStatus(SubscriptionStatus.SUSPENDED);
        try {
            appManager.deactivateInstance(subscription);
        } catch (TechnicalServiceNotAliveException
                | TechnicalServiceOperationException e) {
            LOG.logError(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.ERROR_DEACTIVATE_INSTANCE);
        }
    }
}
