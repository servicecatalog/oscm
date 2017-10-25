/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 09.04.2009                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.types.enumtypes;

/**
 * The Email Type describes the different email content types which can be sent
 * from the server
 * 
 * @author pock
 */
public enum EmailType {
    ORGANIZATION_PAYMENT_INFO,

    ORGANIZATION_UPDATED,

    ORGANIZATION_DISCOUNT_ADDED,

    ORGANIZATION_DISCOUNT_DELETED,

    ORGANIZATION_DISCOUNT_UPDATED,

    ORGANIZATION_DISCOUNT_ENDING,

    /** Parameter: product name, reason */
    REVIEW_REMOVED_BY_MARKETPLACE_ADMIN,

    SUBSCRIPTION_ACTIVATED,

    SUBSCRIPTION_PARAMETER_MODIFIED,

    SUBSCRIPTION_PARAMETER_MODIFY_ABORT,

    SUBSCRIPTION_CREATED,

    SUBSCRIPTION_CREATED_ON_BEHALF_ACTING,

    SUBSCRIPTION_USER_ADDED,

    SUBSCRIPTION_USER_ADDED_ACCESSINFO,

    SUBSCRIPTION_USER_ADDED_ACCESSTYPE_DIRECT,

    SUBSCRIPTION_DELETE,

    SUBSCRIPTION_DELETED,

    SUBSCRIPTION_DELETED_ON_BEHALF_ACTING,

    SUBSCRIPTION_MIGRATED,

    SUBSCRIPTION_ACCESS_GRANTED,

    SUBSCRIPTION_USER_REMOVED,

    SUBSCRIPTION_INVALIDATED,

    SUBSCRIPTION_ACCESSINFO_CHANGED,

    SUBSCRIPTION_TIMEDOUT,

    SUBSCRIPTION_TERMINATED_BY_SUPPLIER,

    /** Parameters: technical product id, instance id */
    SUBSCRIPTION_TERMINATE_TECHNICAL_SERVICE_ERROR,

    USER_ACCOUNTS,

    USER_CONFIRM,

    USER_CONFIRM_ACKNOWLEDGE,

    USER_CREATED,

    USER_CREATED_SAML_SP,

    USER_CREATED_WITH_MARKETPLACE,

    USER_CREATED_WITH_MARKETPLACE_SAML_SP,

    USER_UPDATED,

    USER_UPDATED_WITH_NOROLE,

    USER_DELETED,

    USER_IMPORTED,

    USER_IMPORTED_WITH_MARKETPLACE,

    USER_LOCKED,

    USER_PASSWORD_RESET,

    USER_UNLOCKED,

    USER_UPDATE_FOR_SUBSCRIPTION_FAILED,

    USER_NUM_EXCEEDED,

    /**
     * Service was suspended by the marketplace owner - reason and mail of
     * marketplace owner as parameters
     */
    SERVICE_SUSPENDED,

    /**
     * Email type for notifying administrators in case the marketplace owner
     * role was assigned to their organization.
     */
    MARKETPLACE_OWNER_ASSIGNED,

    /**
     * Email type for notifying administrators in case the supplier was added to
     * the marketplace.
     */
    MARKETPLACE_SUPPLIER_ASSIGNED,

    /**
     * Email type for notifying administrators in case the supplier was added to
     * the marketplace and the supplier is the marketplace owner of the assigned
     * marketplace.
     */
    MARKETPLACE_SUPPLIER_ASSIGNED_OWNED,

    /**
     * Email type for notifying administrators in case the supplier was removed
     * to the marketplace.
     */
    MARKETPLACE_SUPPLIER_REMOVED,

    /**
     * Email type for notifying supplier in case he was (temporarily) banned
     * from publishing to the marketplace.
     */
    MARKETPLACE_SUPPLIER_BANNED,

    /**
     * Email type for notifying supplier in case he was banned and is now
     * allowed again to publish to the marketplace.
     */
    MARKETPLACE_SUPPLIER_LIFTED_BAN,

    /**
     * Email type for notifying (customer/technology providers) in case the
     * notification of the provisioning service has failed.
     */
    NOTIFY_PROVISIONING_SERVICE_FAILED,

    /**
     * Email type for notifying administrators in case a used category was
     * removed.
     */
    CATEGORY_REMOVED,

    /**
     * Email type for sending support issues to supplier's support
     */
    SUPPORT_ISSUE,

    /**
     * Email type for notifying user in case password recovery mail was sent.
     */
    RECOVERPASSWORD_CONFIRM_URL,

    /**
     * Email type for notifying user in case it is an LDAP user.
     */
    RECOVERPASSWORD_FAILED_LDAP,

    /**
     * Email type for notifying pure user in case of recover password from
     * administration portal.
     */
    RECOVERPASSWORD_CLASSICPORTAL_FAILED,

    /**
     * Email type for notifying manager in case of recover password from
     * marketplace.
     */
    RECOVERPASSWORD_MARKETPLACE_FAILED,

    /**
     * Email type for notifying user in case of password change.
     */
    RECOVERPASSWORD_CONFIRM,

    /**
     * Email type for notifying user in case his/her account was locked.
     */
    RECOVERPASSWORD_USER_LOCKED,

    /**
     * Email type for notifying administrator in case of successful completion
     * of bulk user import.
     */
    BULK_USER_IMPORT_SUCCESS,

    /**
     * Email type for notifying administrator in case of bulk user import
     * errors. Bulk user import was completed, but import of some users failed.
     */
    BULK_USER_IMPORT_SOME_ERRORS,

    /**
     * Email type for complete failure of bulk user import. No users were
     * imported.
     */
    BULK_USER_IMPORT_FAILURE,

    /**
     * Email type for notifying users that the name of the user group to which
     * they belong was changed.
     */
    USER_GROUP_UPDATED,

    /**
     * Email type for notifying users that the user group to which they belonged
     * was deleted.
     */
    USER_GROUP_DELETED,

    /**
     * Email type for notifying users that they were assigned to the user group.
     */
    GROUP_USER_ASSIGNED,

    /**
     * Email type for notifying users that were removed from the user group.
     */
    GROUP_USER_REVOKED,

    /**
     * Email type for notifying users that technical service instance of
     * subscription is not found.
     */
    SUBSCRIPTION_INSTANCE_NOT_FOUND,

    /**
     * Email type for notyfing platform operator about failed billing.
     */
    BILLING_FAILED
}
