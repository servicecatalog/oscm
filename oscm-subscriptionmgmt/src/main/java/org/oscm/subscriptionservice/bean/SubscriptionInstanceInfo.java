/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2014-02-14                                                       
 *                                                                              
 *******************************************************************************/
package org.oscm.subscriptionservice.bean;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.validator.BLValidator;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.exception.TechnicalServiceOperationException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOInstanceInfo;

/**
 * Helper for validation and update of instance information.
 * 
 * @author goebel
 */
class SubscriptionInstanceInfo {
    private static final Log4jLogger LOG = LoggerFactory
            .getLogger(ManageSubscriptionBean.class);

    private LocalizerServiceLocal localizer;
    private Subscription subscription;
    
    SubscriptionInstanceInfo(LocalizerServiceLocal localizer,
            Subscription subscription) {
        this.localizer = localizer;
        this.subscription = subscription;
    }

    /**
     * Validates and updates the given instance information on the subscription.
     * <p>
     * {@link #validateInstanceInfo(String, String, VOInstanceInfo)}.
     */
    void validateAndUpdateInstanceInfo(VOInstanceInfo instance)
            throws ValidationException {
        validateInstanceInfo(instance);
        subscription.setProductInstanceId(instance.getInstanceId());
        subscription.setAccessInfo(instance.getAccessInfo());
        subscription.setBaseURL(instance.getBaseUrl());
        subscription.setLoginPath(instance.getLoginPath());
    }

    /**
     * Validates and updates the instance information returned by a completion
     * callback from asynchronous provisioning instance.
     * <p>
     * {@link #validateInstanceInfo(String, String, VOInstanceInfo)}
     * 
     * @param info
     *            - the instance info
     * @throws TechnicalServiceOperationException
     * 
     */
    void validateAndUpdateInstanceInfoForCompletion(VOInstanceInfo info)
            throws TechnicalServiceOperationException {
        try {
            validateInstanceInfo(info);
            subscription.setProductInstanceId(info.getInstanceId());
            subscription.setAccessInfo(info.getAccessInfo());
            subscription.setBaseURL(info.getBaseUrl());
            subscription.setLoginPath(info.getLoginPath());
        } catch (ValidationException e) {
            String message = String.format("%s validation of field %s failed",
                    e.getReason().name(), e.getMember());
            TechnicalServiceOperationException ex = new TechnicalServiceOperationException(
                    "Service createInstance() returned invalid data.",
                    new Object[] { subscription.getSubscriptionId(), message },
                    e);
            LOG.logWarn(Log4jLogger.SYSTEM_LOG, ex,
                    LogMessageIdentifier.WARN_TECH_SERVICE_VALIDATION_FAILED,
                    subscription.getSubscriptionId(), e.getReason().name(),
                    e.getMember());
            throw ex;
        }
    }

    void validateInstanceInfo(VOInstanceInfo info) throws ValidationException {
        BLValidator.isDescription("instanceId", info.getInstanceId(), true);
        BLValidator.isAccessinfo("accessInfo", info.getAccessInfo(),
                isAccessInfoMandatory(info));
        BLValidator.isUrl("baseUrl", info.getBaseUrl(),
                isBaseUrlMandatory(info));
        BLValidator.isDescription("loginPath", info.getLoginPath(),
                isLoginPathMandatory(info));
    }

    private boolean isBaseUrlMandatory(VOInstanceInfo info) {
        ServiceAccessType accessType = subscription.getProduct()
                .getTechnicalProduct().getAccessType();
        return (accessType == ServiceAccessType.LOGIN)
                && (info.getLoginPath() != null)
                && (info.getLoginPath().trim().length() != 0);
    }

    private boolean isLoginPathMandatory(VOInstanceInfo info) {
        ServiceAccessType accessType = subscription.getProduct()
                .getTechnicalProduct().getAccessType();
        return (accessType == ServiceAccessType.LOGIN)
                && (info.getBaseUrl() != null)
                && (info.getBaseUrl().trim().length() != 0);
    }

    private boolean isAccessInfoMandatory(VOInstanceInfo info) {
        ServiceAccessType accessType = subscription.getProduct()
                .getTechnicalProduct().getAccessType();
        
        if (isAccessTypeDirectOrUser(accessType) && noAccessInfoGiven(info)) {
            return noAccessInfoFromTechnicalServiceGiven();
        }
        return false;
    }

    private boolean noAccessInfoFromTechnicalServiceGiven() {
        String accessInfo = localizer.getLocalizedTextFromDatabase("en",
                subscription.getProduct().getTechnicalProduct().getKey(),
                LocalizedObjectTypes.TEC_PRODUCT_LOGIN_ACCESS_DESC);
        return accessInfo == null || accessInfo.trim().length() <= 0;
    }

    private boolean noAccessInfoGiven(VOInstanceInfo info) {
        return info.getAccessInfo() == null
                || info.getAccessInfo().trim().length() <= 0;
    }

    private boolean isAccessTypeDirectOrUser(ServiceAccessType accessType) {
        return accessType == ServiceAccessType.DIRECT
                || accessType == ServiceAccessType.USER;
    }
}
