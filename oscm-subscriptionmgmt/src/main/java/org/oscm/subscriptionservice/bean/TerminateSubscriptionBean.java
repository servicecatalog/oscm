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

import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.interceptor.Interceptors;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.applicationservice.local.ApplicationServiceLocal;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.Parameter;
import org.oscm.domobjects.ParameterSet;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.Session;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.interceptor.DateFactory;
import org.oscm.interceptor.ExceptionMapper;
import org.oscm.sessionservice.local.SessionServiceLocal;
import org.oscm.subscriptionservice.auditlog.SubscriptionAuditLogCollector;
import org.oscm.subscriptionservice.local.SubscriptionServiceLocal;
import org.oscm.types.enumtypes.EmailType;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.types.enumtypes.PlatformParameterIdentifiers;
import org.oscm.validation.ArgumentValidator;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ParameterType;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.types.enumtypes.ServiceType;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.types.exception.MailOperationException;
import org.oscm.internal.types.exception.OrganizationAuthoritiesException;
import org.oscm.internal.types.exception.SaaSApplicationException;

/**
 * No interface view bean implementation of terminate and expire subscriptions
 * functionality.
 * 
 * @author cmin
 */
@LocalBean
@Interceptors({ ExceptionMapper.class })
public class TerminateSubscriptionBean extends SubscriptionUtilBean {

    private static final Log4jLogger LOG = LoggerFactory
            .getLogger(TerminateSubscriptionBean.class);

    @EJB(beanInterface = ApplicationServiceLocal.class)
    protected ApplicationServiceLocal appManager;

    @EJB(beanInterface = SessionServiceLocal.class)
    protected SessionServiceLocal prodSessionMgmt;

    @EJB(beanInterface = LocalizerServiceLocal.class)
    LocalizerServiceLocal localizer;

    @EJB
    protected SubscriptionAuditLogCollector audit;

    public boolean expireOverdueSubscriptions(long currentTime) {
        List<Subscription> activeSubs = getSubscriptionDao()
                .getActiveSubscriptions();
        boolean handledSuccessfully = true;

        for (Subscription sub : activeSubs) {
            Long permittedUsagePeriod = null;
            ParameterSet parameterSet = sub.getParameterSet();
            if (parameterSet != null) {
                List<Parameter> parameters = parameterSet.getParameters();
                for (Parameter param : parameters) {
                    if (param.getParameterDefinition().getParameterType() == ParameterType.PLATFORM_PARAMETER
                            && PlatformParameterIdentifiers.PERIOD.equals(param
                                    .getParameterDefinition().getParameterId())
                            && param.getValue() != null) {
                        permittedUsagePeriod = Long.valueOf(param
                                .getLongValue());
                        break;
                    }
                }

                if (permittedUsagePeriod != null) {
                    // so the subscription has a limited period, check if it
                    // still can be used
                    long usedTime = currentTime
                            - sub.getActivationDate().longValue();
                    if (usedTime > permittedUsagePeriod.longValue()) {
                        // subscription has already been used too long, so
                        // expire it

                        // call has to be made by calling into the container
                        // again, so that the new transactional behaviour is
                        // considered.
                        boolean outcome = prepareForNewTranscation(
                                SubscriptionServiceLocal.class)
                                .expireSubscription(sub);
                        handledSuccessfully = handledSuccessfully & outcome;

                    }
                }
            }
        }
        return handledSuccessfully;
    }

    private <T> T prepareForNewTranscation(Class<T> businessIntf) {
        DateFactory.getInstance().takeCurrentTime();
        return sessionCtx.getBusinessObject(businessIntf);
    }

    public boolean expireSubscription(Subscription subscriptionToExpire) {
        boolean outcome = true;
        try {
            Subscription sub = (Subscription) dataManager
                    .find(subscriptionToExpire);
            sub.setStatus(SubscriptionStatus.EXPIRED);
            dataManager.flush();

            // BE08022 Deactivate the instance. If this invocation fails, do not
            // perform a rollback
            try {
                appManager.deactivateInstance(sub);
            } catch (SaaSApplicationException e) {
                LOG.logError(Log4jLogger.SYSTEM_LOG, e,
                        LogMessageIdentifier.ERROR_STOP_APPLICATION_FAILED,
                        Long.toString(sub.getKey()));
            }
        } catch (Exception e) {
            outcome = false;
            LOG.logError(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.ERROR_EXPIRE_SUBSCRIPTION_FAILED,
                    Long.toString(subscriptionToExpire.getKey()));
        }
        return outcome;
    }

    public void terminateSubscription(Subscription subscription, String reason)
            throws OrganizationAuthoritiesException {

        ArgumentValidator.notNull("subscription", subscription);

        // for null reason value set empty string. It's needed for e-mail text.
        if (reason == null) {
            reason = "";
        }

        // check if the caller is a subscription vendor (supplier or reseller)
        Organization currentOrg = dataManager.getCurrentUser()
                .getOrganization();
        long vendorKey;
        if (subscription.getProduct().getType() == ServiceType.PARTNER_SUBSCRIPTION
                && currentOrg.getGrantedRoleTypes().contains(
                        OrganizationRoleType.SUPPLIER)) {
            vendorKey = subscription.getProduct().getTemplate().getTemplate()
                    .getVendor().getKey();
        } else {
            vendorKey = subscription.getProduct().getVendor().getKey();
        }
        if (currentOrg.getKey() != vendorKey) {
            sessionCtx.setRollbackOnly();
            OrganizationAuthoritiesException e = new OrganizationAuthoritiesException(
                    "Caller is not a supplier");
            LOG.logWarn(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.WARN_ORG_AUTHORITIES_NO_SUPPLIER);
            throw e;
        }

        // delete user sessions
        List<Session> activeSessions = prodSessionMgmt
                .getProductSessionsForSubscriptionTKey(subscription.getKey());
        for (Session session : activeSessions) {
            dataManager.remove(session);
            dataManager.flush();
        }

        // Revoke all users from the subscription
        removeUsageLicenses(subscription);

        // Update referenced product status to DELETED
        final Product product = subscription.getProduct();
        product.setStatus(ServiceStatus.DELETED);
        final Product asyncTempProduct = subscription.getAsyncTempProduct();
        if (asyncTempProduct != null) {
            subscription.setAsyncTempProduct(null);
            dataManager.remove(asyncTempProduct);
        }
        deleteModifiedEntityForSubscription(subscription);

        // Delete instance
        try {
            appManager.deleteInstance(subscription);
        } catch (SaaSApplicationException e) {
            LOG.logError(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.ERROR_TERMINATE_SUBSCRIPTION_FAILED,
                    Long.toString(subscription.getKey()));

            sendTechnicalServiceErrorMail(subscription);
        }

        // set DEACTIVATED status for the subscription
        long timeInMillis = DateFactory.getInstance().getTransactionTime();
        subscription.setDeactivationDate(Long.valueOf(timeInMillis));
        subscription.setStatus(SubscriptionStatus.DEACTIVATED);

        // rename the subscription as last step because id is still used for
        // exceptions and mails rename to allow the reuse of the id
        final String oldSubscId = subscription.getSubscriptionId();
        subscription.setSubscriptionId(String.valueOf(System
                .currentTimeMillis()));

        // Bug 11917: Remove user group from terminated subscription
        subscription.setUserGroup(null);
        dataManager.flush();

        // send mail notification to customer administrator
        List<PlatformUser> users = getCustomerAdminsAndSubscriptionOwner(subscription);

        try {
            String r = reason;
            if (reason.trim().length() > 0) {
                String localeString = dataManager.getCurrentUser().getLocale();
                String text = localizer.getLocalizedTextFromBundle(
                        LocalizedObjectTypes.MAIL_CONTENT, null, localeString,
                        "SUBSCRIPTION_TERMINATED_BY_SUPPLIER_REASON");
                MessageFormat mf = new MessageFormat(text, new Locale(
                        localeString));
                r = mf.format(new Object[] { reason }, new StringBuffer(), null)
                        .toString();
            }
            for (PlatformUser user : users) {
                commService.sendMail(user,
                        EmailType.SUBSCRIPTION_TERMINATED_BY_SUPPLIER,
                        new Object[] { oldSubscId, r },
                        subscription.getMarketplace());
            }
        } catch (MailOperationException e) {
            LOG.logWarn(
                    Log4jLogger.SYSTEM_LOG,
                    e,
                    LogMessageIdentifier.WARN_SUBSCRIPTION_TERMINATION_CONFIRMING_FAILED);
        }
        audit.terminateSubscription(dataManager, subscription, oldSubscId,
                reason);
    }
}
