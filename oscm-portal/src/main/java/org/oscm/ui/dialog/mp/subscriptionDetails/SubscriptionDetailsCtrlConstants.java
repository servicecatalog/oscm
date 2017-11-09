/*
 * ******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *   Creation Date: 04.12.14 14:16
 *
 * ******************************************************************************
 */

package org.oscm.ui.dialog.mp.subscriptionDetails;

import org.oscm.internal.types.enumtypes.SubscriptionStatus;

/**
 * Created by ChojnackiD on 2014-12-04.
 */
public class SubscriptionDetailsCtrlConstants {
    public static final String OUTCOME_PREVIOUS = "previous";
    public static final String ERROR_USER_NOTASSIGNEDTOSUBSCRIPTION = "error.user.NotAssignedToSubscription";
    public static final String ASSIGN_USERS_MODAL_TITLE = "marketplace.subscription.assign.user.modal.title";
    public static final String EDIT_ROLES_MODAL_TITLE = "marketplace.subscription.edit.roles.modal.title";
    public static final String ERROR_SERVICE_INVALID_KEY = "error.service.invalidKey";
    public static final String ERROR_SUBSCRIPTION_LICENSE = "error.subscription.license";
    public static final String ERROR_SUBSCRIPTION_NOT_ACCESSIBLE = "error.subscription.notAccessible";
    public static final String ERROR_INVALID_CONFIGURED_PARAMETERS = "error.externalTool.validate";
    public static final String ERROR_EXTERNAL_TOOL_COMMUNICATION = "error.externalTool.communicate";
    public static final String ERROR_TO_PROCEED_SELECT_UNIT="error.subscription.unitHasToBeSelected";
    public static final String INFO_DIRECT_SUBSCRIPTION_CREATED_DISABLE_INFO = "info.direct.subscription.created.disable.info";
    public static final String INFO_MAX_USERS_REACHED = "info.subscriptions.maximumNumberOfUsersReached";
    public static final String INFO_NO_MORE_USERS = "info.subscriptions.noMoreUsersForAssignment";
    public static final String INFO_PAYMENT_INFO_SAVED = "info.paymentInfo.saved";
    public static final String INFO_SUBSCRIPTION_CREATED = "info.subscription.created";
    public static final String INFO_SUBSCRIPTION_ASYNC_CREATED = "info.subscription.async.created";
    public static final String INFO_SUBSCRIPTION_DELETED = "info.subscription.deleted";
    public static final String INFO_SUBSCRIPTION_UPGRADED = "info.subscription.upgraded";
    public static final String INFO_SUBSCRIPTION_ASYNC_UPGRADED = "info.subscription.async.upgraded";
    public static final String INFO_SUBSCRIPTION_ROLE_UPDATED = "info.subscriptions.role.updated.success";
    public static final String INFO_SUBSCRIPTION_SAVED = "info.subscription.saved";
    public static final String INFO_SUBSCRIPTION_ASYNC_SAVED = "info.subscription.async.saved";
    public static final String SUBSCRIPTION_STATE_WARNING = "subscription.stateWarning";
    public static final String INFO_SUBSCRIPTION_USER_ASSIGNED = "info.subscriptions.userAssigned.success";
    public static final String INFO_SUBSCRIPTION_USER_DEASSIGNED = "info.subscriptions.userDeassigned.success";
    public static final String OUTCOME_DEASSIGNED_USER_OR_ERROR = "deassignedUserOrError";
    public static final String OUTCOME_ENTER_PAYMENT = "enterPayment";
    public static final String OUTCOME_MODIFICATION_ERROR = "concurrentModificationError";
    public static final String OUTCOME_ENTER_SERVICE_CONFIGURATION = "enterServiceConfiguration";
    public static final String OUTCOME_SERVICE_UNSUBSCRIBE = "serviceUnsubscribe";
    public static final String OUTCOME_SERVICE_UPGRADE = "serviceUpgrade";
    public static final String OUTCOME_SHOW_DETAILS = "showDetails";
    public static final String OUTCOME_SUBSCRIPTION_NOT_AVAILABLE = "subscriptionNotAccessible";
    public static final String OUTCOME_SUBSCRIPTION_NEED_APPROVAL = "subscriptionNeedApproval";
    public static final String OUTCOME_SHOW_SERVICE_LIST = "showServiceList";
    public static final String OUTCOME_SUCCESS = "success";
    public static final String OUTCOME_ERROR = "error";
    public static final String VALIDATION_ERROR = "validationError";
    public static final String OUTCOME_PROCESS = "process";
    public static final String OUTCOME_SHOW_DETAILS_4_CREATION = "showDetailsCreation";
    public static final String REQUEST_PARAM_USER_TO_DEASSIGN = "userToDeassign";
    public static final String SELECT_OWNERS_MODAL_TITLE = "marketplace.subscription.select.owner.modal.title";
    public static final String SUBSCRIPTION_CONFIRMATION_PAGE = "subscription.page2";
    public static final String SUBSCRIPTION_USER_DEASSIGN_MSG_KEY = "subscription.users.deassign.message";
    public static final String VIEW_ID_ASSIGN_USERS_POPUP = "/marketplace/subscriptions/assignUsersInclude.xhtml";
    public static final String VIEW_ID_EDIT_ROLES_POPUP = "/marketplace/subscriptions/editRolesInclude.xhtml";
    public static final String VIEW_ID_SELECT_OWNERS_POPUP = "/marketplace/subscriptions/selectOwnersInclude.xhtml";
    public static final String MESSAGE_NO_PAYMENT_TYPE_AVAILABLE = "organization.payment.noAvailablePaymentForNonAdmin";
    public static final String MESSAGE_NO_PAYMENT_TYPE_ENABLED = "organization.payment.noEnabledPayment";
    public static final String ERROR_SERVICE_NOTFOUND = "error.service.notFound";
    public static final String ERROR_SERVICE_INACCESSIBLE = "error.service.inaccessible";
    public static final String ERROR_SERVICE_CHANGED = "error.service.changed";
    public static final String ERROR_SUBSCRIPTION_REPEATSTEPS = "error.subscription.repeatsteps";
    public static final String STATUS_PREFIX = SubscriptionStatus.class
            .getSimpleName() + ".";

    public static final String SUBSCRIPTION_NAME_ALREADY_EXISTS = "ex.NonUniqueBusinessKeyException.SUBSCRIPTION";
    public static final String SUBSCRIPTIONDETAILS_VIEWID = "/marketplace/account/subscriptionDetails.xhtml";
}
