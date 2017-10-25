/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017                                           
 *                                                                                                                                  
 *  Creation Date: 17.06.15 09:56
 *
 *******************************************************************************/

package org.oscm.triggerservice.validator;

import java.util.*;

import javax.persistence.Query;

import org.oscm.converter.ParameterizedTypes;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.TriggerProcess;
import org.oscm.domobjects.TriggerProcessIdentifier;
import org.oscm.triggerservice.bean.TriggerProcessIdentifiers;
import org.oscm.types.enumtypes.TriggerProcessIdentifierName;
import org.oscm.validation.ArgumentValidator;
import org.oscm.internal.types.enumtypes.TriggerType;
import org.oscm.internal.vo.*;

/**
 * Contains semantic validation for <code>TriggerProcess</code>
 * 
 * @author barzu
 */
public class TriggerProcessValidator {

    protected DataService ds;

    public TriggerProcessValidator(DataService ds) {
        this.ds = ds;
    }

    /**
     * Verifies if the specified service was already scheduled and is still
     * pending to be activated or de-activated. Throws an
     * <code>IllegalArgumentException</code> if the specified service is
     * <code>null</code> or its key is smaller than or equals <code>0</code>.
     * 
     * @param service
     *            The service to be checked.
     * @return <code>true</code> if the service was already scheduled and
     *         pending, otherwise <code>false</code>.
     */
    public boolean isActivateOrDeactivateServicePending(VOService service) {
        ArgumentValidator.notNull("service", service);
        TriggerProcessIdentifiers.validateObjectKey(service);
        Query query = ds
                .createNamedQuery("TriggerProcessIdentifier.isActivateDeactivateServicePending");
        query.setParameter("pendingStates",
                TriggerProcess.getUnfinishedStatus());
        query.setParameter("triggerTypes", Arrays.asList(
                TriggerType.ACTIVATE_SERVICE, TriggerType.DEACTIVATE_SERVICE));
        query.setParameter("serviceKeyName",
                TriggerProcessIdentifierName.SERVICE_KEY);
        query.setParameter("serviceKey", String.valueOf(service.getKey()));
        return ((Long) query.getSingleResult()).longValue() > 0;
    }

    /**
     * Returns the ones of the specified <code>users</code> that are already
     * scheduled by an user in the calling organization and are still pending to
     * be added to or revoked from the specified subscription. Throws
     * <code>IllegalArgumentException</code> if
     * <ul>
     * <li>the subscription identifier is <code>null</code>, or</li>
     * <li>any of the the users to be added or revoked is <code>null</code>, or</li>
     * <li>the key of any of the the users to be added or revoked is smaller
     * than or equals <code>0, or</code></li>
     * <li>the usage license of any of the users to add is <code>null</code>.</li>
     * </ul>
     * 
     * @param subscriptionId
     *            The subscription identifier
     * @param users
     *            The users to be checked
     * @return A list of user identifiers, which is empty if none of the
     *         specified users is already scheduled and still pending.
     */
    public List<TriggerProcessIdentifier> getPendingAddRevokeUsers(
            String subscriptionId, List<VOUsageLicense> usersToBeAdded,
            List<VOUser> usersToBeRevoked) {
        ArgumentValidator.notNull("subscriptionId", subscriptionId);
        Set<String> userIds = new HashSet<String>();
        if (usersToBeAdded != null) {
            for (VOUsageLicense license : usersToBeAdded) {
                ArgumentValidator.notNull("usersToBeAdded", license);
                ArgumentValidator.notNull("usersToBeAdded", license.getUser());
                TriggerProcessIdentifiers.validateObjectKey(license.getUser());
                userIds.add(license.getUser().getUserId());
            }
        }
        if (usersToBeRevoked != null) {
            for (VOUser user : usersToBeRevoked) {
                ArgumentValidator.notNull("usersToBeRevoked", user);
                TriggerProcessIdentifiers.validateObjectKey(user);
                userIds.add(user.getUserId());
            }
        }
        if (userIds.isEmpty()) {
            return new ArrayList<TriggerProcessIdentifier>();
        }
        Query query = ds
                .createNamedQuery("TriggerProcessIdentifier.getPendingAddRevokeUsers");
        query.setParameter("pendingStates",
                TriggerProcess.getUnfinishedStatus());
        query.setParameter("triggerType", TriggerType.ADD_REVOKE_USER);
        query.setParameter("orgKeyName",
                TriggerProcessIdentifierName.ORGANIZATION_KEY);
        query.setParameter("orgKey",
                String.valueOf(ds.getCurrentUser().getOrganization().getKey()));
        query.setParameter("subscriptionIdName",
                TriggerProcessIdentifierName.SUBSCRIPTION_ID);
        query.setParameter("subscriptionId", subscriptionId);
        query.setParameter("userNames", Arrays.asList(
                TriggerProcessIdentifierName.USER_TO_ADD,
                TriggerProcessIdentifierName.USER_TO_REVOKE));
        query.setParameter("users", userIds);
        return ParameterizedTypes.list(query.getResultList(),
                TriggerProcessIdentifier.class);
    }

    /**
     * Verifies if an organization with the specified initial administrator was
     * already scheduled and is still pending to be added as a customer for any
     * supplier. Throws an <code>IllegalArgumentException</code> if the
     * specified user is <code>null</code> or its key is smaller than or equals
     * <code>0</code>.
     * 
     * @param user
     *            The initial administrator of the customer.
     * @return <code>true</code> if the user identifier and the email address of
     *         the specified user equal those of the initial administrator of
     *         the pending organization, otherwise <code>false</code>.
     */
    public boolean isRegisterCustomerForSupplierPending(VOUserDetails user) {
        ArgumentValidator.notNull("user", user);
        Query query = ds
                .createNamedQuery("TriggerProcessIdentifier.isRegisterCustomerForSupplierPending");
        query.setParameter("pendingStates",
                TriggerProcess.getUnfinishedStatus());
        query.setParameter("triggerType",
                TriggerType.REGISTER_CUSTOMER_FOR_SUPPLIER);
        query.setParameter("userIdName", TriggerProcessIdentifierName.USER_ID);
        query.setParameter("userId", user.getUserId());
        query.setParameter("userEmailName",
                TriggerProcessIdentifierName.USER_EMAIL);
        query.setParameter("userEmail", user.getEMail());
        return ((Long) query.getSingleResult()).longValue() > 0;
    }

    /**
     * Verifies if there is already a pending payment configuration saving
     * operation for the organization the caller user belongs to.
     * 
     * @return <code>true</code> if there is such pending process, otherwise
     *         <code>false</code>.
     */
    public boolean isSavePaymentConfigurationPending() {
        Query query = ds
                .createNamedQuery("TriggerProcessIdentifier.isSavePaymentConfigurationPending");
        query.setParameter("pendingStates",
                TriggerProcess.getUnfinishedStatus());
        query.setParameter("triggerType",
                TriggerType.SAVE_PAYMENT_CONFIGURATION);
        query.setParameter("orgKeyName",
                TriggerProcessIdentifierName.ORGANIZATION_KEY);
        query.setParameter("orgKey",
                String.valueOf(ds.getCurrentUser().getOrganization().getKey()));
        return ((Long) query.getSingleResult()).longValue() > 0;
    }

    /**
     * Verifies if there is a pending modification or upgrade for the specified
     * subscription. Throws an <code>IllegalArgumentException</code> if the
     * subscription is <code>null</code> or its key is 0 or below.
     * 
     * @param subscription
     *            the subscription to be checked.
     * @return <code>true</code> if the there is such pending process, otherwise
     *         <code>false</code>.
     */
    public boolean isModifyOrUpgradeSubscriptionPending(
            VOSubscription subscription) {
        ArgumentValidator.notNull("subscription", subscription);
        TriggerProcessIdentifiers.validateObjectKey(subscription);

        return executeCheckForModifyOrUpgradeSubscriptionPending(Arrays.asList(
                TriggerType.MODIFY_SUBSCRIPTION,
                TriggerType.UPGRADE_SUBSCRIPTION), subscription.getKey());
    }

    /**
     * Verifies if there is a pending modification for the specified
     * subscription. Throws an <code>IllegalArgumentException</code> if the
     * subscriptionKey is <code>null</code>.
     * 
     * @param subscriptionKey
     *            the subscriptionKey to be checked.
     * @return <code>true</code> if the there is such pending process, otherwise
     *         <code>false</code>.
     */
    public boolean isModifySubscriptionPending(long subscriptionKey) {
        ArgumentValidator.notNull("subscriptionKey",
                String.valueOf(subscriptionKey));

        return executeCheckForModifyOrUpgradeSubscriptionPending(
                Arrays.asList(TriggerType.MODIFY_SUBSCRIPTION), subscriptionKey);
    }

    /**
     * Verifies if there is a pending upgrade for the specified subscription.
     * Throws an <code>IllegalArgumentException</code> if the subscriptionKey is
     * <code>null</code>.
     * 
     * @param subscriptionKey
     *            the subscriptionKey to be checked.
     * @return <code>true</code> if the there is such pending process, otherwise
     *         <code>false</code>.
     */
    public boolean isUpgradeSubscriptionPending(long subscriptionKey) {
        ArgumentValidator.notNull("subscriptionKey",
                String.valueOf(subscriptionKey));

        return executeCheckForModifyOrUpgradeSubscriptionPending(
                Arrays.asList(TriggerType.UPGRADE_SUBSCRIPTION),
                subscriptionKey);
    }

    /**
     * Verifies if there is a pending subscribe or un-subscribe process with the
     * specified subscription identifier. Throws an
     * <code>IllegalArgumentException</code> if the subscription identifier is
     * <code>null</code>.
     * 
     * @param subscriptionId
     *            the subscription identifier.
     * @return <code>true</code> if the there is such pending process, otherwise
     *         <code>false</code>.
     */
    public boolean isSubscribeOrUnsubscribeServicePending(String subscriptionId) {
        ArgumentValidator.notNull("subscriptionId", subscriptionId);
        Query query = ds
                .createNamedQuery("TriggerProcessIdentifier.isSubscribeOrUnsubscribeServicePending");
        query.setParameter("pendingStates",
                TriggerProcess.getUnfinishedStatus());
        query.setParameter("triggerTypes", Arrays.asList(
                TriggerType.SUBSCRIBE_TO_SERVICE,
                TriggerType.UNSUBSCRIBE_FROM_SERVICE));
        query.setParameter("orgKeyName",
                TriggerProcessIdentifierName.ORGANIZATION_KEY);
        query.setParameter("orgKey",
                String.valueOf(ds.getCurrentUser().getOrganization().getKey()));
        query.setParameter("subscriptionIdName",
                TriggerProcessIdentifierName.SUBSCRIPTION_ID);
        query.setParameter("subscriptionId", subscriptionId);
        return ((Long) query.getSingleResult()).longValue() > 0;
    }

    /**
     * Verifies if there is a pending user registration process with the
     * specified user identifier. Throws an
     * <code>IllegalArgumentException</code> if the subscription identifier is
     * <code>null</code>.
     * 
     * @param userId
     *            the user identifier to be registered.
     * @return <code>true</code> if the there is such pending process, otherwise
     *         <code>false</code>.
     */
    public boolean isRegisterOwnUserPending(String userId) {
        ArgumentValidator.notNull("userId", userId);
        ArgumentValidator.notEmptyString("userId", userId);

        Query query = ds
                .createNamedQuery("TriggerProcessIdentifier.isRegisterOwnUserPending");
        query.setParameter("pendingStates",
                TriggerProcess.getUnfinishedStatus());
        query.setParameter("triggerType", TriggerType.REGISTER_OWN_USER);
        query.setParameter("orgKeyName",
                TriggerProcessIdentifierName.ORGANIZATION_KEY);
        query.setParameter("orgKey",
                String.valueOf(ds.getCurrentUser().getOrganization().getKey()));
        query.setParameter("userIdName", TriggerProcessIdentifierName.USER_ID);
        query.setParameter("userId", userId);

        return ((Long) query.getSingleResult()).longValue() > 0;
    }

    private boolean executeCheckForModifyOrUpgradeSubscriptionPending(
            List<TriggerType> types, long subscriptionKey) {
        Query query = ds
                .createNamedQuery("TriggerProcessIdentifier.isModifyOrUpgradeSubscriptionPending");
        query.setParameter("pendingStates",
                TriggerProcess.getUnfinishedStatus());
        query.setParameter("triggerTypes", types);
        query.setParameter("subscriptionKeyName",
                TriggerProcessIdentifierName.SUBSCRIPTION_KEY);
        query.setParameter("subscriptionKey", String.valueOf(subscriptionKey));
        return ((Long) query.getSingleResult()).longValue() > 0;
    }
}
