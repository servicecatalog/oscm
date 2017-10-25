/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Mar 13, 2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.subscriptionservice.bean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.SessionContext;
import javax.interceptor.Interceptors;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.accountservice.dataaccess.UdaAccess;
import org.oscm.communicationservice.local.CommunicationServiceLocal;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.domobjects.Uda;
import org.oscm.domobjects.UsageLicense;
import org.oscm.domobjects.UserGroup;
import org.oscm.identityservice.local.IdentityServiceLocal;
import org.oscm.interceptor.ExceptionMapper;
import org.oscm.subscriptionservice.dao.ModifiedEntityDao;
import org.oscm.subscriptionservice.dao.OrganizationDao;
import org.oscm.subscriptionservice.dao.SubscriptionDao;
import org.oscm.taskhandling.local.TaskMessage;
import org.oscm.taskhandling.local.TaskQueueServiceLocal;
import org.oscm.taskhandling.operations.SendMailHandler;
import org.oscm.taskhandling.payloads.SendMailPayload;
import org.oscm.types.enumtypes.EmailType;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.types.exception.MailOperationException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;

/**
 * No interface view bean implementation for util method
 * 
 * @author Zhou
 */
@Interceptors({ ExceptionMapper.class })
public class SubscriptionUtilBean {

    private static final Log4jLogger LOG = LoggerFactory
            .getLogger(ManageSubscriptionBean.class);

    @EJB
    protected IdentityServiceLocal idManager;

    @EJB
    protected DataService dataManager;

    @EJB
    protected TaskQueueServiceLocal tqs;

    @EJB
    protected CommunicationServiceLocal commService;

    @Resource
    protected SessionContext sessionCtx;

    protected List<PlatformUser> getCustomerAdminsAndSubscriptionOwner(
            Subscription subscription) {
        List<PlatformUser> users = getOrganizationDao().getOrganizationAdmins(
                subscription.getOrganizationKey());
        for (int i = 0; i < users.size(); i++) {
            PlatformUser user = users.get(i);
            if (user.isOnBehalfUser()) {
                users.remove(i);
            }
        }

        PlatformUser owner = subscription.getOwner();
        if (owner != null && !users.contains(owner) && !owner.isOnBehalfUser()) {
            users.add(owner);
        }
        return users;
    }

    /**
     * The receivers for mail is sent to:
     * 
     * - administrators of the technology provider organization;<br/>
     * - administrators of the customer organization;<br/>
     * - subscription owner if it is not already administrator.
     */
    List<PlatformUser> getCustomerAndTechnicalProductAdminForSubscription(
            Subscription subscription) {

        List<PlatformUser> users = getCustomerAdminsAndSubscriptionOwner(subscription);
        for (PlatformUser orgAdmin : subscription.getProduct()
                .getTechnicalProduct().getOrganization()
                .getOrganizationAdmins()) {
            if (!users.contains(orgAdmin)) {
                users.add(orgAdmin);
            }
        }
        return users;
    }

    UdaAccess getUdaAccess() {
        return new UdaAccess(dataManager, sessionCtx);
    }

    List<Uda> getExistingUdas(Subscription subscription) {
        List<Uda> existingUdas = getUdaAccess().getExistingUdas(
                subscription.getOrganization().getKey(),
                subscription.getKey(),
                subscription.getProduct().getSupplierOrResellerTemplate()
                        .getVendor());
        return existingUdas;
    }

    void setSubscriptionOwner(Subscription subscriptionToModify,
            String ownerId, boolean validateOrganization)
            throws ObjectNotFoundException, OperationNotPermittedException {
        if (ownerId != null && ownerId.length() != 0) {
            PlatformUser owner = idManager.getPlatformUser(ownerId, dataManager
                    .getCurrentUser().getTenantId(), validateOrganization);
            subscriptionToModify.setOwner(owner);
        } else {
            // delete the owner of subscription
            subscriptionToModify.setOwner(null);
        }
    }

    void setSubscriptionUnit(Subscription subscriptionToModify, String unitKey)
            throws ObjectNotFoundException {
        if (unitKey != null && unitKey.trim().length() != 0) {
            UserGroup unit = dataManager.getReference(UserGroup.class, Long
                    .valueOf(unitKey).longValue());
            subscriptionToModify.setUserGroup(unit);
        } else {
            subscriptionToModify.setUserGroup(null);
        }
    }

    /**
     * Verifies that the current authorized organization has the role
     * TECHNOLOGY_PROVIDER and is the owner of the technical product on which
     * the given subscription is based.
     * 
     * @param subscription
     *            The subscription defining the technical product
     * @throws OperationNotPermittedException
     *             Thrown in case the current user's organization does not own
     *             the technical product on which the given subscription is
     *             based
     */
    void validateTechnoloyProvider(Subscription subscription)
            throws OperationNotPermittedException {
        Organization tp = dataManager.getCurrentUser().getOrganization();
        if (!tp.getTechnicalProducts().contains(
                subscription.getProduct().getTechnicalProduct())) {
            OperationNotPermittedException onp = new OperationNotPermittedException();
            LOG.logWarn(
                    Log4jLogger.SYSTEM_LOG | Log4jLogger.AUDIT_LOG,
                    onp,
                    LogMessageIdentifier.WARN_ORGANIZATION_OWN_NO_TECHNICAL_PRODUCT_OF_SUBSCRIPTION,
                    subscription.getSubscriptionId());
            throw onp;
        }
    }

    /**
     * Tries to read the subscription with the given id in the context of the
     * organization with the given id.
     * 
     * @param subscriptionId
     *            the subscription id
     * @param organizationId
     *            the organization id
     * @return the subscription
     * @throws ObjectNotFoundException
     *             in case the organization or the subscription wasn't found
     */
    Subscription findSubscription(String subscriptionId, String organizationId)
            throws ObjectNotFoundException {
        Organization organization = new Organization();
        organization.setOrganizationId(organizationId);
        organization = (Organization) dataManager
                .getReferenceByBusinessKey(organization);

        Subscription subscription = new Subscription();
        subscription.setSubscriptionId(subscriptionId);
        subscription.setOrganization(organization);
        subscription = (Subscription) dataManager
                .getReferenceByBusinessKey(subscription);
        return subscription;
    }

    /**
     * Only removes all {@link UsageLicense}s for a subscription and sends the
     * user removed mail. Only use this method when terminating a subscription
     * (where users would be deleted with the instance deletion).
     * 
     * @param subscription
     *            the subscription to delete the {@link UsageLicense}s for.
     */
    protected void removeUsageLicenses(Subscription subscription) {
        // as the user gets no mail when being added to a pending
        // subscription, the notification about the removal will also
        // not be sent. The same for direct access...
        boolean needToSendMails = !(subscription.getStatus() != SubscriptionStatus.ACTIVE || subscription
                .getProduct().getTechnicalProduct().getAccessType() == ServiceAccessType.DIRECT);
        List<UsageLicense> usageLicenses = new ArrayList<UsageLicense>(
                subscription.getUsageLicenses());
        for (UsageLicense lic : usageLicenses) {
            subscription.revokeUser(lic.getUser());
            dataManager.remove(lic);
        }

        if (needToSendMails) {
            sendUserRemoveMailMessages(subscription, usageLicenses);
        }
    }

    private void sendUserRemoveMailMessages(Subscription subscription,
            List<UsageLicense> usageLicenses) {
        Long marketplaceKey = null;
        if (subscription.getMarketplace() != null) {
            marketplaceKey = subscription.getMarketplace()
                    .getKey();
        }

        SendMailPayload payload = new SendMailPayload();
        String subscriptionId = subscription.getSubscriptionId();

        for (UsageLicense license : usageLicenses) {
            payload.addMailObjectForUser(license.getUser().getKey(),
                    EmailType.SUBSCRIPTION_USER_REMOVED,
                    new Object[] { subscriptionId }, marketplaceKey);
        }
        TaskMessage message = new TaskMessage(SendMailHandler.class, payload);
        tqs.sendAllMessages(Arrays.asList(message));
    }

    protected void sendTechnicalServiceErrorMail(Subscription subscription) {
        TechnicalProduct tp = subscription.getProduct().getTechnicalProduct();
        List<PlatformUser> admins = tp.getOrganization()
                .getOrganizationAdmins();
        for (PlatformUser admin : admins) {
            try {
                commService
                        .sendMail(
                                admin,
                                EmailType.SUBSCRIPTION_TERMINATE_TECHNICAL_SERVICE_ERROR,
                                new Object[] { tp.getTechnicalProductId(),
                                        subscription.getProductInstanceId() },
                                subscription.getMarketplace());
            } catch (MailOperationException e) {
                // only log the exception and proceed
                LOG.logWarn(Log4jLogger.SYSTEM_LOG, e,
                        LogMessageIdentifier.WARN_SENDING_ERROR_MAIL_FAILED);
            }
        }
    }

    void deleteModifiedEntityForSubscription(Subscription subscription) {
        getModifiedEntityDao()
                .deleteModifiedEntityForSubscription(subscription);
        dataManager.flush();
    }

    public SubscriptionDao getSubscriptionDao() {
        return new SubscriptionDao(dataManager);
    }

    public OrganizationDao getOrganizationDao() {
        return new OrganizationDao(dataManager);
    }

    public ModifiedEntityDao getModifiedEntityDao() {
        return new ModifiedEntityDao(dataManager);
    };
}
