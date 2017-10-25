/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.triggerservice.bean;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.TriggerProcessIdentifier;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.types.enumtypes.TriggerProcessIdentifierName;
import org.oscm.validation.ArgumentValidator;
import org.oscm.internal.types.enumtypes.TriggerType;
import org.oscm.internal.types.exception.IllegalArgumentException;
import org.oscm.internal.vo.BaseVO;
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOSubscription;
import org.oscm.internal.vo.VOUsageLicense;
import org.oscm.internal.vo.VOUser;
import org.oscm.internal.vo.VOUserDetails;

/**
 * Class determine trigger process related identifiers.
 */
public class TriggerProcessIdentifiers {

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(TriggerProcessIdentifiers.class);

    /**
     * Determines the identifying values for the trigger related operations. The
     * result will contain the calling organization's key and the service key,
     * which is appropriate for trigger types
     * {@link TriggerType#ACTIVATE_SERVICE} and
     * {@link TriggerType#DEACTIVATE_SERVICE}. If another trigger type is used
     * or any of the parameters is <code>null</code>, an IllegalArgument
     * exception is thrown. This also happens if the service has a key of 0 or
     * below.
     * 
     * @param ds
     *            The data service.
     * @param triggerType
     *            The trigger type identifying the current operation.
     * @param service
     *            The service the operation is based on.
     * 
     * @return The list of identifying values.
     */
    public static List<TriggerProcessIdentifier> createDeactivateService(
            DataService ds, TriggerType triggerType, VOService service) {

        ArgumentValidator.notNull("ds", ds);
        ArgumentValidator.notNull("triggerType", triggerType);
        ArgumentValidator.notNull("service", service);
        validateTriggerType(triggerType, TriggerType.ACTIVATE_SERVICE,
                TriggerType.DEACTIVATE_SERVICE);
        validateObjectKey(service);
        List<TriggerProcessIdentifier> result = initResult(ds);
        result.add(new TriggerProcessIdentifier(
                TriggerProcessIdentifierName.SERVICE_KEY, String
                        .valueOf(service.getKey())));

        return result;
    }

    /**
     * Creates a new result list for process identifiers and already adds the
     * key of the calling organization.
     * 
     * @param ds
     *            The data service used to retrieve the current caller.
     * 
     * @return The initialized identifier list.
     */
    private static List<TriggerProcessIdentifier> initResult(DataService ds) {
        List<TriggerProcessIdentifier> result = new ArrayList<TriggerProcessIdentifier>();
        result.add(new TriggerProcessIdentifier(
                TriggerProcessIdentifierName.ORGANIZATION_KEY,
                String.valueOf(ds.getCurrentUser().getOrganization().getKey())));
        return result;
    }

    /**
     * Validates that the key of the specified object is set. If it is not
     * greater than 0, it is not persisted and an IllegalArgument exception is
     * thrown.
     * 
     * @param obj
     *            The object to validate the key for.
     */
    public static void validateObjectKey(BaseVO obj) {
        if (!(obj.getKey() > 0)) {
            IllegalArgumentException iae = new IllegalArgumentException();
            logger.logError(Log4jLogger.SYSTEM_LOG, iae,
                    LogMessageIdentifier.ERROR_PASSED_ENTITY_NOT_PERSISTED, obj
                            .getClass().getSimpleName());
            throw iae;
        }
    }

    /**
     * Determines the identifying values for the trigger related operations. The
     * result will contain the calling organization's key and the subscription
     * identifier, which is appropriate for the trigger types
     * {@link TriggerType#SUBSCRIBE_TO_SERVICE} and
     * {@link TriggerType#UNSUBSCRIBE_FROM_SERVICE}. If another trigger type is
     * used or any of the parameters is <code>null</code>, an IllegalArgument
     * exception is thrown.
     * 
     * @param ds
     *            The data service.
     * @param triggerType
     *            The trigger type identifying the current operation.
     * @param subscriptionIdentifier
     *            The identifier of the subscription.
     * 
     * @return The list of identifying values.
     */
    public static List<TriggerProcessIdentifier> createUnsubscribeFromService(
            DataService ds, TriggerType triggerType,
            String subscriptionIdentifier) {

        ArgumentValidator.notNull("ds", ds);
        ArgumentValidator.notNull("triggerType", triggerType);
        ArgumentValidator.notNull("subscriptionIdentifier",
                subscriptionIdentifier);
        validateTriggerType(triggerType, TriggerType.SUBSCRIBE_TO_SERVICE,
                TriggerType.UNSUBSCRIBE_FROM_SERVICE);
        List<TriggerProcessIdentifier> result = initResult(ds);
        result.add(new TriggerProcessIdentifier(
                TriggerProcessIdentifierName.SUBSCRIPTION_ID,
                subscriptionIdentifier));

        return result;
    }

    /**
     * Determines the identifying values for the trigger related operations. The
     * result will contain the calling organization's key and the subscription,
     * which is appropriate for the trigger types
     * {@link TriggerType#UPGRADE_SUBSCRIPTION} and
     * {@link TriggerType#MODIFY_SUBSCRIPTION}. If another trigger type is used
     * or any of the parameters is <code>null</code>, an IllegalArgument
     * exception is thrown. This also happens if the subscription has a key of 0
     * or below.
     * 
     * @param ds
     *            The data service.
     * @param triggerType
     *            The trigger type identifying the current operation.
     * @param subscription
     *            The corresponding subscription.
     * 
     * @return The list of identifying values.
     */
    public static List<TriggerProcessIdentifier> createUpgradeSubscription(
            DataService ds, TriggerType triggerType, VOSubscription subscription) {

        ArgumentValidator.notNull("ds", ds);
        ArgumentValidator.notNull("triggerType", triggerType);
        ArgumentValidator.notNull("subscription", subscription);
        validateTriggerType(triggerType, TriggerType.MODIFY_SUBSCRIPTION,
                TriggerType.UPGRADE_SUBSCRIPTION);
        validateObjectKey(subscription);
        List<TriggerProcessIdentifier> result = initResult(ds);
        result.add(new TriggerProcessIdentifier(
                TriggerProcessIdentifierName.SUBSCRIPTION_KEY, String
                        .valueOf(subscription.getKey())));

        return result;
    }

    private static void validateTriggerType(TriggerType passedTriggerType,
            TriggerType... allowedTriggerTypes) {
        for (TriggerType type : allowedTriggerTypes) {
            if (type == passedTriggerType) {
                return;
            }
        }
        IllegalArgumentException iae = new IllegalArgumentException();
        logger.logError(Log4jLogger.SYSTEM_LOG, iae,
                LogMessageIdentifier.ERROR_INVALID_TRIGGER_TYPE_FOR_CONTEXT,
                passedTriggerType.name());
        throw iae;
    }

    /**
     * Determines the identifying values for the trigger related operations. The
     * result will contain the calling organization's key and the user's
     * identifier and email, which is appropriate for the trigger type
     * {@link TriggerType#REGISTER_CUSTOMER_FOR_SUPPLIER}. If another trigger
     * type is used or any of the parameters is <code>null</code>, an
     * IllegalArgument exception is thrown.
     * 
     * @param ds
     *            The data service.
     * @param triggerType
     *            The trigger type identifying the current operation.
     * @param user
     *            The corresponding user.
     * 
     * @return The list of identifying values.
     */
    public static List<TriggerProcessIdentifier> createRegisterCustomerForSupplier(
            DataService ds, TriggerType triggerType, VOUserDetails user) {

        ArgumentValidator.notNull("ds", ds);
        ArgumentValidator.notNull("triggerType", triggerType);
        ArgumentValidator.notNull("user", user);
        validateTriggerType(triggerType,
                TriggerType.REGISTER_CUSTOMER_FOR_SUPPLIER);
        List<TriggerProcessIdentifier> result = initResult(ds);
        result.add(new TriggerProcessIdentifier(
                TriggerProcessIdentifierName.USER_ID, user.getUserId()));
        result.add(new TriggerProcessIdentifier(
                TriggerProcessIdentifierName.USER_EMAIL, user.getEMail()));

        return result;
    }

    /**
     * Determines the identifying values for the trigger related operations. The
     * result will contain the calling organization's key and the user's
     * identifier, which is appropriate for the trigger type
     * {@link TriggerType#REGISTER_OWN_USER}. If another trigger type is used or
     * any of the parameters is <code>null</code>, an IllegalArgument exception
     * is thrown.
     * 
     * @param ds
     *            The data service.
     * @param triggerType
     *            The trigger type identifying the current operation.
     * @param user
     *            The corresponding user.
     * 
     * @return The list of identifying values.
     */
    public static List<TriggerProcessIdentifier> createRegisterOwnUser(
            DataService ds, TriggerType triggerType, VOUserDetails user) {

        ArgumentValidator.notNull("ds", ds);
        ArgumentValidator.notNull("triggerType", triggerType);
        ArgumentValidator.notNull("user", user);
        validateTriggerType(triggerType, TriggerType.REGISTER_OWN_USER);
        List<TriggerProcessIdentifier> result = initResult(ds);
        result.add(new TriggerProcessIdentifier(
                TriggerProcessIdentifierName.USER_ID, user.getUserId()));

        return result;
    }

    /**
     * Determines the identifying values for the trigger related operations. The
     * result will contain the calling organization's key and the subscription
     * id as well as the user identifiers, which is appropriate for the trigger
     * type {@link TriggerType#ADD_REVOKE_USER}. If another trigger type is used
     * or any of the parameters is <code>null</code>, an IllegalArgument
     * exception is thrown. This also happens if any of the referenced user
     * objects has a key of 0 or below.
     * 
     * @param ds
     *            The data service.
     * @param triggerType
     *            The trigger type identifying the current operation.
     * @param subscriptionIdentifier
     *            The identifier of the affected subscription.
     * @param usersToAdd
     *            The usage licenses for the users to be added.
     * @param usersToRemove
     *            The users to be removed.
     * 
     * @return The list of identifying values.
     */
    public static List<TriggerProcessIdentifier> createAddRevokeUser(
            DataService ds, TriggerType triggerType,
            String subscriptionIdentifier, List<VOUsageLicense> usersToAdd,
            List<VOUser> usersToRemove) {
        ArgumentValidator.notNull("ds", ds);
        ArgumentValidator.notNull("triggerType", triggerType);
        ArgumentValidator.notNull("subscriptionIdentifier",
                subscriptionIdentifier);
        validateTriggerType(triggerType, TriggerType.ADD_REVOKE_USER);
        Set<String> idsForUsersToRevoke = getUserIds(usersToRemove);
        Set<String> idsForUsersToAdd = getUserIdsForLicenses(usersToAdd);
        List<TriggerProcessIdentifier> result = initResult(ds);
        result.add(new TriggerProcessIdentifier(
                TriggerProcessIdentifierName.SUBSCRIPTION_ID,
                subscriptionIdentifier));
        for (String idUserToAdd : idsForUsersToAdd) {
            result.add(new TriggerProcessIdentifier(
                    TriggerProcessIdentifierName.USER_TO_ADD, idUserToAdd));
        }
        for (String idUserToRevoke : idsForUsersToRevoke) {
            result.add(new TriggerProcessIdentifier(
                    TriggerProcessIdentifierName.USER_TO_REVOKE, idUserToRevoke));
        }

        return result;
    }

    /**
     * Determines the identifiers of the given users and returns them in a set.
     * Throws an IllegalArgument exception in case a user is specified with a
     * key <= 0.
     * 
     * @param users
     *            The users to check.
     * @return A set of user identifiers corresponding to the provided users.
     */
    private static Set<String> getUserIds(List<VOUser> users) {
        Set<String> result = new HashSet<String>();
        if (users == null) {
            return result;
        }
        for (VOUser user : users) {
            validateObjectKey(user);
            result.add(user.getUserId());
        }
        return result;
    }

    /**
     * Determines the identifiers of the given users and returns them in a set.
     * Throws an IllegalArgument exception in case a user is specified with a
     * key <= 0.
     * 
     * @param uls
     *            The users to check.
     * @return A set of user identifiers corresponding to the provided users.
     */
    private static Set<String> getUserIdsForLicenses(List<VOUsageLicense> uls) {
        Set<String> result = new HashSet<String>();
        if (uls == null) {
            return result;
        }
        for (VOUsageLicense ul : uls) {
            ArgumentValidator.notNull("usersToAdd.user", ul.getUser());
            validateObjectKey(ul.getUser());
            result.add(ul.getUser().getUserId());
        }
        return result;
    }

    /**
     * Determines the identifying values for the trigger related operations. The
     * result will contain the calling organization's key, which is appropriate
     * for trigger type {@link TriggerType#SAVE_PAYMENT_CONFIGURATION}. If
     * another trigger type is used or the parameters is <code>null</code>, an
     * IllegalArgument exception is thrown.
     * 
     * @param ds
     *            The data service.
     * @param triggerType
     *            The trigger type identifying the current operation.
     * 
     * @return The list of identifying values.
     */
    public static List<TriggerProcessIdentifier> createSavePaymentConfiguration(
            DataService ds, TriggerType triggerType) {

        ArgumentValidator.notNull("ds", ds);
        ArgumentValidator.notNull("triggerType", triggerType);
        validateTriggerType(triggerType, TriggerType.SAVE_PAYMENT_CONFIGURATION);
        List<TriggerProcessIdentifier> result = initResult(ds);

        return result;
    }

}
