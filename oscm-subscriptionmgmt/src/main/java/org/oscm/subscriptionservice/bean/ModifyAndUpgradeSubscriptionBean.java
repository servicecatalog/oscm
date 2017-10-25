/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Mar 13, 2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.subscriptionservice.bean;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.interceptor.Interceptors;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;

import org.oscm.domobjects.BillingContact;
import org.oscm.domobjects.ModifiedEntity;
import org.oscm.domobjects.ModifiedUda;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PaymentInfo;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.Uda;
import org.oscm.domobjects.enums.ModifiedEntityType;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.interceptor.ExceptionMapper;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.types.exception.MailOperationException;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.PaymentInformationException;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.types.enumtypes.EmailType;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.types.enumtypes.UdaTargetType;
import org.oscm.validation.PaymentDataValidator;

/**
 * No interface view bean implementation for asynchronously modify subscription
 * 
 * @author Zhou
 */
@LocalBean
@Interceptors({ ExceptionMapper.class })
public class ModifyAndUpgradeSubscriptionBean extends SubscriptionUtilBean {

    private static final Log4jLogger LOG = LoggerFactory
            .getLogger(ManageSubscriptionBean.class);

    @EJB
    protected LocalizerServiceLocal localizer;

    Subscription updateSubscriptionAttributesForAsyncUpgrade(
            Subscription subscription)
            throws ObjectNotFoundException, NumberFormatException {
        List<ModifiedEntity> modifiedEntities = retrieveModifiedEntities(
                subscription);

        String paymentInfoKey = getModifiedEntityValueByType(modifiedEntities,
                ModifiedEntityType.SUBSCRIPTION_PAYMENTINFO);
        if (paymentInfoKey != null) {
            try {
                PaymentInfo paymentInfo = dataManager.getReference(
                        PaymentInfo.class,
                        Long.valueOf(paymentInfoKey).longValue());
                subscription.setPaymentInfo(paymentInfo);
            } catch (ObjectNotFoundException e) {
                LOG.logWarn(Log4jLogger.SYSTEM_LOG, e,
                        LogMessageIdentifier.WARN_PAYMENT_INFO_NOT_EXIST);
                subscription.setPaymentInfo(null);
            }
        }
        String bcKey = getModifiedEntityValueByType(modifiedEntities,
                ModifiedEntityType.SUBSCRIPTION_BILLINGCONTACT);
        if (bcKey != null) {
            try {
                BillingContact bc = dataManager.getReference(
                        BillingContact.class, Long.valueOf(bcKey).longValue());
                subscription.setBillingContact(bc);
            } catch (ObjectNotFoundException e) {
                LOG.logWarn(Log4jLogger.SYSTEM_LOG, e,
                        LogMessageIdentifier.WARN_BILLING_CONTACT_NOT_EXIST);
                subscription.setBillingContact(null);
            }
        }
        updateUdasForAsyncSubscription(subscription);
        removeModifiedEntities(modifiedEntities);
        return subscription;
    }

    Subscription updateSubscriptionAttributesForAsyncUpdate(
            Subscription subscription) throws ObjectNotFoundException,
            NumberFormatException, OperationNotPermittedException {
        List<ModifiedEntity> modifiedEntities = retrieveModifiedEntities(
                subscription);

        String subscriptionId = getModifiedEntityValueByType(modifiedEntities,
                ModifiedEntityType.SUBSCRIPTION_SUBSCRIPTIONID);
        String purchaseNumber = getModifiedEntityValueByType(modifiedEntities,
                ModifiedEntityType.SUBSCRIPTION_PURCHASEORDERNUMBER);
        String ownerId = getModifiedEntityValueByType(modifiedEntities,
                ModifiedEntityType.SUBSCRIPTION_OWNERID);
        String unitKey = getModifiedEntityValueByType(modifiedEntities,
                ModifiedEntityType.SUBSCRIPTION_UNIT);
        if (subscriptionId != null) {
            subscription.setSubscriptionId(subscriptionId);
        }
        subscription.setPurchaseOrderNumber(purchaseNumber);
        if (unitKey != null && Long.valueOf(unitKey).longValue() != 0L) {
            setSubscriptionUnit(subscription, unitKey);
        }
        setSubscriptionOwner(subscription, ownerId, false);
        updateUdasForAsyncSubscription(subscription);

        removeModifiedEntities(modifiedEntities);
        return subscription;
    }

    void setStatusForModifyComplete(Subscription sub) {
        final SubscriptionStatus currentState = sub.getStatus();
        sub.setStatus(currentState.getNextForCompleteModify());
    }

    void setStatusForUpgradeComplete(Subscription sub) {
        final SubscriptionStatus currentState = sub.getStatus();

        boolean paymentValidOrFree = isPaymentValidOrFree(sub);

        SubscriptionStatus nextState = currentState
                .getNextForCompleteUpgrade(paymentValidOrFree);
        sub.setStatus(nextState);
    }

    boolean isPaymentValidOrFree(Subscription subscription) {
        boolean validPaymentDataOrFree = true;
        if (subscription.getPriceModel().isChargeable()) {
            PaymentInfo paymentInfo = subscription.getPaymentInfo();
            BillingContact billingContact = subscription.getBillingContact();
            if (paymentInfo == null) {
                LOG.logError(
                        LogMessageIdentifier.ERROR_ACTIVATE_SUBSCRIPTION_FAILED_NO_PAYMENT_INFORMATION);
                validPaymentDataOrFree = false;
            } else if (billingContact == null) {
                LOG.logError(
                        LogMessageIdentifier.ERROR_ACTIVATE_SUBSCRIPTION_FAILED_NO_BILLING_CONTACT);
                validPaymentDataOrFree = false;
            } else {
                // get the parameters for the payment validation
                Organization customer = subscription.getOrganization();
                try {
                    validatePaymentInfo(subscription, paymentInfo, customer);
                } catch (PaymentInformationException e) {
                    LOG.logError(Log4jLogger.SYSTEM_LOG, e,
                            LogMessageIdentifier.ERROR_ACTIVATE_SUBSCRIPTION_FAILED_AS_NO_VALID_PAYMENT_ASSIGNED);
                    validPaymentDataOrFree = false;
                }
            }
        }
        return validPaymentDataOrFree;
    }

    void validatePaymentInfo(Subscription subscription, PaymentInfo paymentInfo,
            Organization customer) throws PaymentInformationException {
        PaymentDataValidator.validatePaymentTypeSupportedBySupplier(customer,
                subscription.getProduct(), paymentInfo.getPaymentType());
        PaymentDataValidator.validatePaymentInfoDataForUsage(paymentInfo);
    }

    protected void sendConfirmUpgradationEmail(Subscription subscription,
            String oldServiceId, String newServiceId) {
        sendConfirmUpgradationEmail(subscription, oldServiceId, newServiceId,
                "");
    }

    protected void sendConfirmUpgradationEmail(Subscription subscription,
            String oldServiceId, String newServiceId, String accessInfo) {

        List<PlatformUser> users = getCustomerAdminsAndSubscriptionOwner(
                subscription);
        try {
            for (PlatformUser user : users) {
                commService.sendMail(user, EmailType.SUBSCRIPTION_MIGRATED,
                        new Object[] { subscription.getSubscriptionId(),
                                oldServiceId, newServiceId, accessInfo },
                        subscription.getMarketplace());
            }
        } catch (MailOperationException e) {
            LOG.logWarn(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.WARN_SUBSCRIPTION_MIGRATION_CONFIRMING_FAILED);
        }
    }

    /**
     * For async modifying subscription, the subscription id may be changed,
     * when the provisioning service call
     * completeAsyncModifySubscription/abortAsyncModifySubscription, the passed
     * parameter subscriptionId is modified ID, and can not be found in
     * subscription table. If not found in subscription table, try to get
     * subscription key in modifiedentity.
     * 
     * @param subscriptionId
     *            the subscription id
     * @param organizationId
     *            the organization id
     * @return the subscription
     * @throws ObjectNotFoundException
     *             in case the organization or the subscription wasn't found
     */
    Subscription findSubscriptionForAsyncCallBack(String subscriptionId,
            String organizationId) throws ObjectNotFoundException {
        Subscription subscription = null;
        try {
            subscription = findSubscription(subscriptionId, organizationId);
        } catch (ObjectNotFoundException e) {
            Long result = null;
            try {
                result = getSubscriptionDao().findSubscriptionForAsyncCallBack(
                        subscriptionId, organizationId);
            } catch (NoResultException ex) {
                LOG.logError(Log4jLogger.SYSTEM_LOG, ex,
                        LogMessageIdentifier.ERROR_SUBSCRIPTIONID_NOT_EXIST_IN_MODIFIEDENTITY,
                        subscriptionId, organizationId);
                throw e;
            } catch (NonUniqueResultException se) {
                LOG.logError(Log4jLogger.SYSTEM_LOG, se,
                        LogMessageIdentifier.ERROR_SUBSCRIPTIONID_NOT_UNIQUE_IN_MODIFIEDENTITY,
                        subscriptionId, organizationId);
                throw e;
            }

            subscription = dataManager.getReference(Subscription.class,
                    result.longValue());
        }
        return subscription;
    }

    void storeModifiedEntity(long targetObjectkey, ModifiedEntityType type,
            String value) throws NonUniqueBusinessKeyException {
        ModifiedEntity entity = new ModifiedEntity();
        entity.setTargetObjectType(type);
        entity.setTargetObjectKey(targetObjectkey);
        entity.setValue(value);
        dataManager.persist(entity);
    }

    void storeModifiedUda(long targetObjectkey, ModifiedEntityType type,
            String value, long subscriptionKey, boolean encrypted)
            throws NonUniqueBusinessKeyException {
        ModifiedUda modifiedUda = new ModifiedUda();
        modifiedUda.setTargetObjectType(type);
        modifiedUda.setTargetObjectKey(targetObjectkey);
        modifiedUda.setSubscriptionKey(subscriptionKey);
        modifiedUda.setEncrypted(encrypted);
        modifiedUda.setValue(value);
        dataManager.persist(modifiedUda);
    }

    private List<ModifiedEntity> retrieveModifiedEntities(
            Subscription subscription) {
        return getModifiedEntityDao().retrieveModifiedEntities(subscription);
    }

    private void removeModifiedEntities(List<ModifiedEntity> modifiedEntities) {
        for (ModifiedEntity entity : modifiedEntities) {
            dataManager.remove(entity);
        }
    }

    private String getModifiedEntityValueByType(
            List<ModifiedEntity> modifiedEntities, ModifiedEntityType type) {
        for (ModifiedEntity entity : modifiedEntities) {
            if (entity.getTargetObjectType().equals(type)) {
                return entity.getValue();
            }
        }
        return null;
    }

    private void updateUdasForAsyncSubscription(Subscription subscription)
            throws ObjectNotFoundException {
        List<Uda> existingUdas = getExistingUdas(subscription);
        for (Uda uda : existingUdas) {
            if (uda.getUdaDefinition()
                    .getTargetType() == UdaTargetType.CUSTOMER_SUBSCRIPTION) {
                ModifiedUda modifiedUda = new ModifiedUda();
                modifiedUda.setTargetObjectKey(uda.getKey());
                modifiedUda.setTargetObjectType(ModifiedEntityType.UDA_VALUE);
                modifiedUda.setSubscriptionKey(subscription.getKey());
                modifiedUda = (ModifiedUda) dataManager
                        .getReferenceByBusinessKey(modifiedUda);
                uda.setUdaValue(modifiedUda.getValue());
                dataManager.remove(modifiedUda);
            }
        }
    }
}
