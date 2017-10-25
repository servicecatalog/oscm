/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: tokoda                                                      
 *                                                                              
 *  Creation Date: Oct 7, 2011                                                      
 *                                                                              
 *  Completion Time: Oct 7, 2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.operationslog;

import org.oscm.types.enumtypes.LogMessageIdentifier;

/**
 * The possible log types of the user operation log.
 * 
 * @author tokoda
 * 
 */
public enum UserOperationLogType {

    // ENUM_NAME(log message id, query name, item names of log)
    SUBSCR(LogMessageIdentifier.INFO_OPERATION_LOG_SUBSCR, "", new String[] {
            "op", "user", "subscription", "customer", "customer id", "service",
            "activation", "status", "deactivation", "marketplace",
            "payment type", "billing contact", "reference", "access", "url",
            "login path", "timeoutmailsent" }), //
    SUBSCR_USER(LogMessageIdentifier.INFO_OPERATION_LOG_SUBSCR_USER, "",
            new String[] { "op", "user", "subscription", "customer",
                    "customer id", "user id", "first name", "last name",
                    "email", "app user id", "app user role" }), //
    SUBSCR_PRICE(LogMessageIdentifier.INFO_OPERATION_LOG_SUBSCR_PRICE, "",
            new String[] { "op", "user", "subscription", "customer",
                    "customer id", "free", "one time fee", "period", "price",
                    "price per user", "currency" }), //
    SUBSCR_PRICE_EVENT(
            LogMessageIdentifier.INFO_OPERATION_LOG_SUBSCR_PRICE_EVENT, "",
            new String[] { "op", "user", "subscription", "customer",
                    "customer id", "event", "price", "currency" }), //
    SUBSCR_PRICE_EVENT_STEPPED(
            LogMessageIdentifier.INFO_OPERATION_LOG_SUBSCR_PRICE_EVENT_STEPPED,
            "",
            new String[] { "op", "user", "subscription", "customer",
                    "customer id", "event", "price", "currency", "upper limit" }), //
    SUBSCR_PRICE_PARAM(
            LogMessageIdentifier.INFO_OPERATION_LOG_SUBSCR_PRICE_PARAM, "",
            new String[] { "op", "user", "subscription", "customer",
                    "customer id", "parameter", "price", "price per user",
                    "currency" }), //
    SUBSCR_PRICE_PARAM_STEPPED(
            LogMessageIdentifier.INFO_OPERATION_LOG_SUBSCR_PRICE_PARAM_STEPPED,
            "", new String[] { "op", "user", "subscription", "customer",
                    "customer id", "parameter", "price", "currency",
                    "upperlimit" }), //
    SUBSCR_PRICE_OPTION(
            LogMessageIdentifier.INFO_OPERATION_LOG_SUBSCR_PRICE_OPTION, "",
            new String[] { "op", "user", "subscription", "customer",
                    "customer id", "parameter", "option", "price",
                    "price per user", "currency" }), //
    SUBSCR_PRICE_ROLE(
            LogMessageIdentifier.INFO_OPERATION_LOG_SUBSCR_PRICE_ROLE, "",
            new String[] { "op", "user", "subscription", "customer",
                    "customer id", "role", "price", "currency" }), //
    SUBSCR_PRICE_ROLE_PARAM(
            LogMessageIdentifier.INFO_OPERATION_LOG_SUBSCR_PRICE_ROLE_PARAM,
            "", new String[] { "op", "user", "subscription", "customer",
                    "customer id", "role", "parameter", "price", "currency" }), //
    SUBSCR_PRICE_ROLE_OPTION(
            LogMessageIdentifier.INFO_OPERATION_LOG_SUBSCR_PRICE_ROLE_OPTION,
            "", new String[] { "op", "user", "subscription", "customer",
                    "customer id", "role", "parameter", "option", "price",
                    "currency" }), //
    SUBSCR_UDA(LogMessageIdentifier.INFO_OPERATION_LOG_SUBSCR_UDA, "",
            new String[] { "op", "user", "subscription", "customer",
                    "customer id", "uda name", "uda value", "supplier",
                    "supplier id" }), //
    ORGAN(LogMessageIdentifier.INFO_OPERATION_LOG_ORGAN, "", new String[] {
            "op", "user", "organization", "id", "registration", "psp id",
            "email", "phone", "url", "country", "locale", "role", "address",
            "deregistration" }), //
    ORGAN_USER(LogMessageIdentifier.INFO_OPERATION_LOG_ORGAN_USER, "",
            new String[] { "op", "user", "organization", "id", "user id",
                    "first name", "last name", "email", "phone",
                    "realm user id", "locale", "status" }), //
    ORGAN_USER_ROLE(LogMessageIdentifier.INFO_OPERATION_LOG_ORGAN_USER_ROLE,
            "", new String[] { "op", "user", "organization", "id", "user id",
                    "role" }), //
    ORGAN_REF(LogMessageIdentifier.INFO_OPERATION_LOG_ORGAN_REF, "",
            new String[] { "op", "user", "organization", "id", "referencetype",
                    "organization", "id" }), //
    ORGAN_DISCOUNT(LogMessageIdentifier.INFO_OPERATION_LOG_ORGAN_DISCOUNT, "",
            new String[] { "op", "user", "organization", "id", "discount",
                    "start", "end" }), //
    ORGAN_VAT(LogMessageIdentifier.INFO_OPERATION_LOG_ORGAN_VAT, "",
            new String[] { "op", "user", "organization", "id", "referencetype",
                    "customer", "id", "vat" }), //
    ORGAN_UDA(LogMessageIdentifier.INFO_OPERATION_LOG_ORGAN_UDA, "",
            new String[] { "op", "user", "organization", "id", "referencetype",
                    "customer", "id", "uda name", "uda value" }), //
    ORGAN_BILL_CONTACT(
            LogMessageIdentifier.INFO_OPERATION_LOG_ORGAN_BILL_CONTACT, "",
            new String[] { "op", "user", "organization", "id", "name",
                    "company", "address", "email" }), //
    ORGAN_PAYMENT_INFO(
            LogMessageIdentifier.INFO_OPERATION_LOG_ORGAN_PAYMENT_INFO, "",
            new String[] { "op", "user", "organization", "id", "name",
                    "provider", "psp id", "payment type" }), //
    SERVICE(LogMessageIdentifier.INFO_OPERATION_LOG_SERVICE, "", new String[] {
            "op", "user", "service", "supplier", "id", "marketplace",
            "provisioning", "status", "deprovisioning", "technical service",
            "customer", "id" }), //
    SERVICE_PARAM(LogMessageIdentifier.INFO_OPERATION_LOG_SERVICE_PARAM, "",
            new String[] { "op", "user", "service", "supplier", "id",
                    "parameter", "value", "configurable" }), //
    SERVICE_OPTION(LogMessageIdentifier.INFO_OPERATION_LOG_SERVICE_OPTION, "",
            new String[] { "op", "user", "service", "supplier", "id",
                    "parameter", "option" }), //
    SERVICE_UPGRADE(LogMessageIdentifier.INFO_OPERATION_LOG_SERVICE_UPGRADE,
            "", new String[] { "op", "user", "service", "supplier", "id",
                    "upgrade to" }), //
    SERVICE_REVIEW(LogMessageIdentifier.INFO_OPERATION_LOG_SERVICE_REVIEW, "",
            new String[] { "op", "user", "service", "supplier", "id",
                    "user id", "rating", "title" }), //
    SERVICE_PRICE(LogMessageIdentifier.INFO_OPERATION_LOG_SERVICE_PRICE, "",
            new String[] { "op", "user", "service", "supplier", "id", "free",
                    "one time fee", "period", "price", "price per user",
                    "currency" }), //
    SERVICE_PRICE_EVENT(
            LogMessageIdentifier.INFO_OPERATION_LOG_SERVICE_PRICE_EVENT, "",
            new String[] { "op", "user", "service", "supplier", "id", "event",
                    "price", "currency" }), //
    SERVICE_PRICE_EVENT_STEPPED(
            LogMessageIdentifier.INFO_OPERATION_LOG_SERVICE_PRICE_EVENT_STEPPED,
            "", new String[] { "op", "user", "service", "supplier", "id",
                    "event", "price", "currency", "upper limit" }), //
    SERVICE_PRICE_PARAM(
            LogMessageIdentifier.INFO_OPERATION_LOG_SERVICE_PRICE_PARAM, "",
            new String[] { "op", "user", "service", "supplier", "id",
                    "parameter", "price", "price per user", "currency" }), //
    SERVICE_PRICE_PARAM_STEPPED(
            LogMessageIdentifier.INFO_OPERATION_LOG_SERVICE_PRICE_PARAM_STEPPED,
            "", new String[] { "op", "user", "service", "supplier", "id",
                    "parameter", "price", "currency", "upperlimit" }), //
    SERVICE_PRICE_OPTION(
            LogMessageIdentifier.INFO_OPERATION_LOG_SERVICE_PRICE_OPTION, "",
            new String[] { "op", "user", "service", "supplier", "id",
                    "parameter", "option", "price", "price per user",
                    "currency" }), //
    SERVICE_PRICE_ROLE(
            LogMessageIdentifier.INFO_OPERATION_LOG_SERVICE_PRICE_ROLE, "",
            new String[] { "op", "user", "service", "supplier", "id", "role",
                    "price", "currency" }), //
    SERVICE_PRICE_ROLE_PARAM(
            LogMessageIdentifier.INFO_OPERATION_LOG_SERVICE_PRICE_ROLE_PARAM,
            "", new String[] { "op", "user", "service", "supplier", "id",
                    "role", "parameter", "price", "currency" }), //
    SERVICE_PRICE_ROLE_OPTION(
            LogMessageIdentifier.INFO_OPERATION_LOG_SERVICE_PRICE_ROLE_OPTION,
            "", new String[] { "op", "user", "service", "supplier", "id",
                    "role", "parameter", "option", "price", "currency" }), //
    TSERVICE(LogMessageIdentifier.INFO_OPERATION_LOG_TSERVICE, "",
            new String[] { "op", "user", "service", "provider", "id", "build",
                    "url", "login path", "provisioning url", "type", "timeout",
                    "user", "access type", "only one subscription allowed",
                    "on behalf acting allowed" }), //
    TSERVICE_ROLE(LogMessageIdentifier.INFO_OPERATION_LOG_TSERVICE_ROLE, "",
            new String[] { "op", "user", "service", "provider", "id", "role" }), //
    TSERVICE_EVENT(LogMessageIdentifier.INFO_OPERATION_LOG_TSERVICE_EVENT, "",
            new String[] { "op", "user", "service", "provider", "id", "event",
                    "type" }), //
    TSERVICE_PARAM(LogMessageIdentifier.INFO_OPERATION_LOG_TSERVICE_PARAM, "",
            new String[] { "op", "user", "service", "provider", "id",
                    "parameter", "type", "min", "max", "default", "mandatory",
                    "configurable" }), //
    TSERVICE_OPERATION(
            LogMessageIdentifier.INFO_OPERATION_LOG_TSERVICE_OPERATION, "",
            new String[] { "op", "user", "service", "provider", "id", "op",
                    "url", "type" }), //
    MARKETPLACE(LogMessageIdentifier.INFO_OPERATION_LOG_MARKETPLACE, "",
            new String[] { "op", "user", "marketplace", "organization", "id",
                    "global" }), //
    MARKETPLACE_ENTRY(
            LogMessageIdentifier.INFO_OPERATION_LOG_MARKETPLACE_ENTRY, "",
            new String[] { "op", "user", "marketplace", "service", "supplier",
                    "id", "position", "visible for anonymous",
                    "edition visible in catalog" });

    LogMessageIdentifier logMessageIdentifier;
    String queryName;
    String[] itemNames;

    private UserOperationLogType(LogMessageIdentifier logMessageIdentifier,
            String queryName, String... itemNames) {
        this.logMessageIdentifier = logMessageIdentifier;
        this.queryName = queryName;
        this.itemNames = itemNames;
    }

    public LogMessageIdentifier getLogMessageIdentifier() {
        return logMessageIdentifier;
    }

    public String getQueryName() {
        return queryName;
    }

    public String[] getItemNames() {
        return itemNames;
    }
}
