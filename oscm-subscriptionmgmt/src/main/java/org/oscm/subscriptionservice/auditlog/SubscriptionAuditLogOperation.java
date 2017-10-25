/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.subscriptionservice.auditlog;

import org.oscm.auditlog.AuditLogParameter;

public enum SubscriptionAuditLogOperation {
    SUBSCRIBE_SERVICE("30000", AuditLogParameter.SERVICE_ID,
            AuditLogParameter.SERVICE_NAME, AuditLogParameter.SUBSCRIPTION_NAME),

    ASSIGN_USER_TO_SUBSCRIPTION("30001", AuditLogParameter.SERVICE_ID,
            AuditLogParameter.SERVICE_NAME,
            AuditLogParameter.SUBSCRIPTION_NAME, AuditLogParameter.TARGET_USER),

    DEASSIGN_USER_FROM_SUBSCRIPTION("30002", AuditLogParameter.SERVICE_ID,
            AuditLogParameter.SERVICE_NAME,
            AuditLogParameter.SUBSCRIPTION_NAME, AuditLogParameter.TARGET_USER),

    ASSIGN_USERROLE_FOR_SERVICE("30003", AuditLogParameter.SERVICE_ID,
            AuditLogParameter.SERVICE_NAME,
            AuditLogParameter.SUBSCRIPTION_NAME, AuditLogParameter.TARGET_USER,
            AuditLogParameter.USER_ROLE),

    DEASSIGN_USERROLE_FOR_SERVICE("30004", AuditLogParameter.SERVICE_ID,
            AuditLogParameter.SERVICE_NAME,
            AuditLogParameter.SUBSCRIPTION_NAME, AuditLogParameter.TARGET_USER,
            AuditLogParameter.USER_ROLE),

    EDIT_SUBSCRIPTION_PARAMETER_CONFIGURATION("30005",
            AuditLogParameter.SERVICE_ID, AuditLogParameter.SERVICE_NAME,
            AuditLogParameter.SUBSCRIPTION_NAME,
            AuditLogParameter.PARAMETER_NAME, AuditLogParameter.PARAMETER_VALUE),

    EDIT_SUBSCRIPTION_BILLING_ADDRESS("30006", AuditLogParameter.SERVICE_ID,
            AuditLogParameter.SERVICE_NAME,
            AuditLogParameter.SUBSCRIPTION_NAME,
            AuditLogParameter.ADDRESS_NAME, AuditLogParameter.ADDRESS_DETAILS),

    EDIT_SUBSCRIPTION_PAYMENT_TYPE("30007", AuditLogParameter.SERVICE_ID,
            AuditLogParameter.SERVICE_NAME,
            AuditLogParameter.SUBSCRIPTION_NAME,
            AuditLogParameter.PAYMENT_NAME, AuditLogParameter.PAYMENT_TYPE),

    UP_DOWNGRADE_SUBSCRIPTION("30008", AuditLogParameter.SERVICE_ID,
            AuditLogParameter.SERVICE_NAME,
            AuditLogParameter.SUBSCRIPTION_NAME,
            AuditLogParameter.NEW_SERVICE_ID,
            AuditLogParameter.NEW_SERVICE_NAME),

    EXECUTE_SERVICE_OPERATION("30009", AuditLogParameter.SERVICE_ID,
            AuditLogParameter.SERVICE_NAME,
            AuditLogParameter.SUBSCRIPTION_NAME,
            AuditLogParameter.SERVICE_OPERATION),

    TERMINATE_SUBSCRIPTION("30010", AuditLogParameter.SERVICE_ID,
            AuditLogParameter.SERVICE_NAME,
            AuditLogParameter.SUBSCRIPTION_NAME, AuditLogParameter.REASON),

    SUBSCRIPTION_REPORT_ISSUE("30011", AuditLogParameter.SERVICE_ID,
            AuditLogParameter.SERVICE_NAME,
            AuditLogParameter.SUBSCRIPTION_NAME,
            AuditLogParameter.SUBSCRIPTION_ISSUE_SUBJECT),

    VIEW_SUBSCRIPTION("30012", AuditLogParameter.SERVICE_ID,
            AuditLogParameter.SERVICE_NAME,
            AuditLogParameter.SUBSCRIPTION_NAME, AuditLogParameter.CUSTOMER_ID,
            AuditLogParameter.CUSTOMER_NAME),

    LOCALIZE_PRICE_MODEL_FOR_SUBSCRIPTION("30013",
            AuditLogParameter.SERVICE_ID, AuditLogParameter.SERVICE_NAME,
            AuditLogParameter.SUBSCRIPTION_NAME, AuditLogParameter.CUSTOMER_ID,
            AuditLogParameter.CUSTOMER_NAME, AuditLogParameter.LOCALE,
            AuditLogParameter.DESCRIPTION, AuditLogParameter.LICENSE),

    EDIT_SUBSCRIPTION_ATTRIBUTE_BY_SERVICE_MANAGER("30014",
            AuditLogParameter.SERVICE_ID, AuditLogParameter.SERVICE_NAME,
            AuditLogParameter.SUBSCRIPTION_NAME,
            AuditLogParameter.ATTRIBUTE_NAME, AuditLogParameter.ATTRIBUTE_VALUE),

    UNSUBSCRIBE_FROM_SERVICE("30015", AuditLogParameter.SERVICE_ID,
            AuditLogParameter.SERVICE_NAME, AuditLogParameter.SUBSCRIPTION_NAME),

    EDIT_SUBSCRIPTION_OWNER("30016", AuditLogParameter.SERVICE_ID,
            AuditLogParameter.SERVICE_NAME,
            AuditLogParameter.SUBSCRIPTION_NAME,
            AuditLogParameter.SUBSCRIPTION_OWNER),

    EDIT_SUBSCRIPTION_ATTRIBUTE_BY_CUSTOMER("30017",
            AuditLogParameter.SERVICE_ID, AuditLogParameter.SERVICE_NAME,
            AuditLogParameter.SUBSCRIPTION_NAME,
            AuditLogParameter.ATTRIBUTE_NAME, AuditLogParameter.ATTRIBUTE_VALUE),

    EDIT_CUSTOMER_ATTRIBUTE_BY_CUSTOMER("30018", AuditLogParameter.CUSTOMER_ID,
            AuditLogParameter.CUSTOMER_NAME, AuditLogParameter.ATTRIBUTE_NAME,
            AuditLogParameter.ATTRIBUTE_VALUE);

    private String operationId;
    private AuditLogParameter[] parameters;

    private SubscriptionAuditLogOperation(String operationId,
            AuditLogParameter... parameters) {
        this.operationId = operationId;
        this.parameters = parameters;
    }

    public String getOperationId() {
        return operationId;
    }

    public AuditLogParameter[] getParameters() {
        return parameters;
    }
}
