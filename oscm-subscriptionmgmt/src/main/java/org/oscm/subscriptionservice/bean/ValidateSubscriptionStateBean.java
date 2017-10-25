/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 2014-02-03      
 *  
 *  author goebel
 *                                                                              
 *******************************************************************************/
package org.oscm.subscriptionservice.bean;

import java.util.EnumSet;

import javax.annotation.Resource;
import javax.ejb.LocalBean;
import javax.ejb.SessionContext;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.domobjects.PriceModel;
import org.oscm.domobjects.Subscription;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.types.exception.SubscriptionStateException;

/**
 * No interface view bean implementation for validing subscription states
 * 
 * @author goebel
 */
@LocalBean
public class ValidateSubscriptionStateBean {

    private static final Log4jLogger LOG = LoggerFactory
            .getLogger(ValidateSubscriptionStateBean.class);

    @Resource
    public SessionContext sessionCtx;

    void checkAddRevokeUserAllowed(Subscription sub)
            throws SubscriptionStateException {
        EnumSet<SubscriptionStatus> forbiddenStates = EnumSet.of(
                SubscriptionStatus.EXPIRED, SubscriptionStatus.INVALID,
                SubscriptionStatus.DEACTIVATED, SubscriptionStatus.SUSPENDED,
                SubscriptionStatus.SUSPENDED_UPD,
                SubscriptionStatus.PENDING_UPD);
        checkOperationAllowed(sub, forbiddenStates);
    }

    void checkModifyAllowedForUpdating(Subscription dbSubscription)
            throws SubscriptionStateException {
        EnumSet<SubscriptionStatus> forbiddenStates = EnumSet.of(
                SubscriptionStatus.EXPIRED, SubscriptionStatus.INVALID,
                SubscriptionStatus.DEACTIVATED, SubscriptionStatus.SUSPENDED,
                SubscriptionStatus.SUSPENDED_UPD, SubscriptionStatus.PENDING,
                SubscriptionStatus.PENDING_UPD);
        checkOperationAllowed(dbSubscription, forbiddenStates);
    }

    void checkModifyAllowedForUpgrading(Subscription dbSubscription)
            throws SubscriptionStateException {
        EnumSet<SubscriptionStatus> forbiddenStates = EnumSet.of(
                SubscriptionStatus.INVALID, SubscriptionStatus.DEACTIVATED,
                SubscriptionStatus.PENDING, SubscriptionStatus.PENDING_UPD,
                SubscriptionStatus.SUSPENDED_UPD);
        checkOperationAllowed(dbSubscription, forbiddenStates);
    }

    void checkUnsubscribingAllowed(Subscription dbSubscription)
            throws SubscriptionStateException {
        EnumSet<SubscriptionStatus> forbiddenStates = EnumSet.of(
                SubscriptionStatus.INVALID, SubscriptionStatus.DEACTIVATED);
        checkOperationAllowed(dbSubscription, forbiddenStates);
    }

    void checkModifyUserRoleAllowed(Subscription dbSubscription)
            throws SubscriptionStateException {
        EnumSet<SubscriptionStatus> forbiddenStates = EnumSet.of(
                SubscriptionStatus.EXPIRED, SubscriptionStatus.INVALID,
                SubscriptionStatus.DEACTIVATED, SubscriptionStatus.SUSPENDED,
                SubscriptionStatus.SUSPENDED_UPD,
                SubscriptionStatus.PENDING_UPD);

        checkOperationAllowed(dbSubscription, forbiddenStates);
    }

    void checkInformProductAboutNewUsers(Subscription dbSubscription)
            throws SubscriptionStateException {
        EnumSet<SubscriptionStatus> forbiddenStates = EnumSet.of(
                SubscriptionStatus.INVALID, SubscriptionStatus.DEACTIVATED,
                SubscriptionStatus.SUSPENDED, SubscriptionStatus.PENDING_UPD,
                SubscriptionStatus.SUSPENDED_UPD);

        checkOperationAllowed(dbSubscription, forbiddenStates);
    }

    void checkAbortAllowedForUpgrading(Subscription dbSubscription)
            throws SubscriptionStateException {
        EnumSet<SubscriptionStatus> forbiddenStates = EnumSet.of(
                SubscriptionStatus.ACTIVE, SubscriptionStatus.INVALID,
                SubscriptionStatus.DEACTIVATED, SubscriptionStatus.PENDING,
                SubscriptionStatus.SUSPENDED);
        checkOperationAllowed(dbSubscription, forbiddenStates);
        checkUpgradingProcess(dbSubscription);
    }

    void checkAbortAllowedForModifying(Subscription dbSubscription)
            throws SubscriptionStateException {
        EnumSet<SubscriptionStatus> forbiddenStates = EnumSet.of(
                SubscriptionStatus.ACTIVE, SubscriptionStatus.EXPIRED,
                SubscriptionStatus.INVALID, SubscriptionStatus.DEACTIVATED,
                SubscriptionStatus.PENDING, SubscriptionStatus.SUSPENDED);
        checkOperationAllowed(dbSubscription, forbiddenStates);
        checkUpdatingProcess(dbSubscription);
    }

    void checkExecuteServiceOperationAllowed(Subscription sub)
            throws SubscriptionStateException {
        EnumSet<SubscriptionStatus> forbiddenStates = EnumSet.of(
                SubscriptionStatus.INVALID, SubscriptionStatus.DEACTIVATED,
                SubscriptionStatus.PENDING);
        checkOperationAllowed(sub, forbiddenStates);
    }

    private void checkOperationAllowed(Subscription sub,
            EnumSet<SubscriptionStatus> set) throws SubscriptionStateException {
        try {
            if (set.contains(sub.getStatus())) {
                SubscriptionStateException sse = new SubscriptionStateException(
                        SubscriptionStateException.Reason.SUBSCRIPTION_INVALID_STATE,
                        null, new Object[] { sub.getStatus() });

                LOG.logWarn(Log4jLogger.SYSTEM_LOG, sse,
                        LogMessageIdentifier.WARN_SUBSCRIPTION_STATE_INVALID,
                        sub.getStatus().name());
                throw sse;
            }
        } catch (SubscriptionStateException e) {
            sessionCtx.setRollbackOnly();
            throw e;
        }
    }

    void checkCompleteUpgradeAllowed(Subscription sub)
            throws SubscriptionStateException {
        try {
            if (!sub.getStatus().canCompleteUpgrade()) {
                SubscriptionStateException sse = new SubscriptionStateException(
                        SubscriptionStateException.Reason.ONLY_UPD, null,
                        new Object[] { sub.getStatus() });

                LOG.logWarn(Log4jLogger.SYSTEM_LOG, sse,
                        LogMessageIdentifier.WARN_SUBSCRIPTION_STATE_INVALID,
                        sub.getStatus().name());
                throw sse;
            }
            checkUpgradingProcess(sub);
        } catch (SubscriptionStateException e) {
            sessionCtx.setRollbackOnly();
            throw e;
        }

    }

    public void checkCompleteModifyAllowed(Subscription sub)
            throws SubscriptionStateException {
        try {
            if (!sub.getStatus().canCompleteModify()) {
                SubscriptionStateException sse = new SubscriptionStateException(
                        SubscriptionStateException.Reason.ONLY_UPD, null,
                        new Object[] { sub.getStatus() });

                LOG.logWarn(Log4jLogger.SYSTEM_LOG, sse,
                        LogMessageIdentifier.WARN_SUBSCRIPTION_STATE_INVALID,
                        sub.getStatus().name());
                throw sse;
            }
            checkUpdatingProcess(sub);
        } catch (SubscriptionStateException e) {
            sessionCtx.setRollbackOnly();
            throw e;
        }
    }

    private void checkUpdatingProcess(Subscription sub)
            throws SubscriptionStateException {
        PriceModel pm = sub.getAsyncTempProduct().getPriceModel();
        if (!pm.isProvisioningCompleted()) {
            SubscriptionStateException sse = new SubscriptionStateException(
                    SubscriptionStateException.Reason.NOT_IN_UPDATING,
                    null, null);

            LOG.logWarn(Log4jLogger.SYSTEM_LOG, sse,
                    LogMessageIdentifier.WARN_SUBSCRIPTION_NOT_IN_UPDATING);
            throw sse;
        }
    }

    private void checkUpgradingProcess(Subscription sub)
            throws SubscriptionStateException {
        PriceModel pm = sub.getAsyncTempProduct().getPriceModel();
        if (pm.isProvisioningCompleted()) {
            SubscriptionStateException sse = new SubscriptionStateException(
                    SubscriptionStateException.Reason.NOT_IN_UPGRADING,
                    null, null);

            LOG.logWarn(Log4jLogger.SYSTEM_LOG, sse,
                    LogMessageIdentifier.WARN_SUBSCRIPTION_NOT_IN_UPGRADING);
            throw sse;
        }
    }

}
