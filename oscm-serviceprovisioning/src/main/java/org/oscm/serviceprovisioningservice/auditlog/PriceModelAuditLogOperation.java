/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.auditlog;

import static org.oscm.auditlog.AuditLogParameter.ACTION_NAME;
import static org.oscm.auditlog.AuditLogParameter.CALCULATION_MODE;
import static org.oscm.auditlog.AuditLogParameter.CURRENCY_CODE;
import static org.oscm.auditlog.AuditLogParameter.CUSTOMER_ID;
import static org.oscm.auditlog.AuditLogParameter.CUSTOMER_NAME;
import static org.oscm.auditlog.AuditLogParameter.DAYS_OF_TRIAL;
import static org.oscm.auditlog.AuditLogParameter.DESCRIPTION;
import static org.oscm.auditlog.AuditLogParameter.EVENT_NAME;
import static org.oscm.auditlog.AuditLogParameter.LICENSE;
import static org.oscm.auditlog.AuditLogParameter.LOCALE;
import static org.oscm.auditlog.AuditLogParameter.ONE_TIME_FEE;
import static org.oscm.auditlog.AuditLogParameter.OPTION_NAME;
import static org.oscm.auditlog.AuditLogParameter.PARAMETER_NAME;
import static org.oscm.auditlog.AuditLogParameter.PRICE;
import static org.oscm.auditlog.AuditLogParameter.RANGE;
import static org.oscm.auditlog.AuditLogParameter.RECURRING_CHARGE;
import static org.oscm.auditlog.AuditLogParameter.SERVICE_ID;
import static org.oscm.auditlog.AuditLogParameter.SERVICE_NAME;
import static org.oscm.auditlog.AuditLogParameter.SUBSCRIPTION_NAME;
import static org.oscm.auditlog.AuditLogParameter.TIMEUNIT;
import static org.oscm.auditlog.AuditLogParameter.TRIAL_PERIOD;
import static org.oscm.auditlog.AuditLogParameter.USER_ROLE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.oscm.auditlog.AuditLogParameter;

public enum PriceModelAuditLogOperation {

    EDIT_EVENT_PRICE_FOR_SUBSCRIPTION("30030", CURRENCY_CODE, EVENT_NAME,
            RANGE, ACTION_NAME, PRICE),

    EDIT_EVENT_PRICE_FOR_CUSTOMER_SERVICE("30031", CURRENCY_CODE, EVENT_NAME,
            RANGE, ACTION_NAME, PRICE),

    EDIT_EVENT_PRICE_FOR_SERVICE("30032", CURRENCY_CODE, EVENT_NAME, RANGE,
            ACTION_NAME, PRICE),

    EDIT_PARAMETER_SUBSCRIPTION_PRICE_FOR_SUBSCRIPTION("30033", CURRENCY_CODE,
            PARAMETER_NAME, RANGE, OPTION_NAME, ACTION_NAME, PRICE),

    EDIT_PARAMETER_SUBSCRIPTION_PRICE_FOR_CUSTOMER_SERVICE("30034",
            CURRENCY_CODE, PARAMETER_NAME, RANGE, OPTION_NAME, ACTION_NAME,
            PRICE),

    EDIT_PARAMETER_SUBSCRIPTION_PRICE_FOR_SERVICE("30035", CURRENCY_CODE,
            PARAMETER_NAME, RANGE, OPTION_NAME, ACTION_NAME, PRICE),

    EDIT_PARAMETER_USER_PRICE_FOR_SUBSCRIPTION("30042", CURRENCY_CODE,
            PARAMETER_NAME,  OPTION_NAME, PRICE),

    EDIT_PARAMETER_USER_PRICE_FOR_CUSTOMER_SERVICE("30043", CURRENCY_CODE,
            PARAMETER_NAME,  OPTION_NAME, PRICE),

    EDIT_PARAMETER_USER_PRICE_FOR_SERVICE("30044", CURRENCY_CODE,
            PARAMETER_NAME, OPTION_NAME, PRICE),

    EDIT_PARAMETER_USER_ROLE_PRICE_FOR_SUBSCRIPTION("30048", CURRENCY_CODE,
            TIMEUNIT, USER_ROLE, PARAMETER_NAME,  OPTION_NAME, PRICE),

    EDIT_PARAMETER_USER_ROLE_PRICE_FOR_CUSTOMER_SERVICE("30049", CURRENCY_CODE,
            TIMEUNIT, USER_ROLE, PARAMETER_NAME, OPTION_NAME, PRICE),

    EDIT_PARAMETER_USER_ROLE_PRICE_FOR_SERVICE("30050", CURRENCY_CODE,
            TIMEUNIT, USER_ROLE, PARAMETER_NAME, OPTION_NAME, PRICE),

    EDIT_CHARGEABLE_PRICE_MODEL_FOR_SERVICE("30051", CURRENCY_CODE, TIMEUNIT,
            CALCULATION_MODE, TRIAL_PERIOD, DAYS_OF_TRIAL),

    EDIT_CHARGEABLE_PRICE_MODEL_FOR_CUSTOMER_SERVICE("30052", CURRENCY_CODE,
            TIMEUNIT, CALCULATION_MODE, TRIAL_PERIOD, DAYS_OF_TRIAL),

    EDIT_SUBSCRIPTION_PRICE_FOR_SERVICE("30054", CURRENCY_CODE, TIMEUNIT,
            ONE_TIME_FEE, RECURRING_CHARGE),

    EDIT_SUBSCRIPTION_PRICE_FOR_CUSTOMER_SERVICE("30055", CURRENCY_CODE,
            TIMEUNIT, ONE_TIME_FEE, RECURRING_CHARGE),

    EDIT_SUBSCRIPTION_PRICE_FOR_SUBSCRIPTION("30056", CURRENCY_CODE, TIMEUNIT,
            ONE_TIME_FEE, RECURRING_CHARGE),

    EDIT_SERVICE_ROLE_PRICE_FOR_SERVICE("30057", CURRENCY_CODE, TIMEUNIT,
            USER_ROLE, PRICE),

    EDIT_SERVICE_ROLE_PRICE_FOR_CUSTOMER_SERVICE("30058", CURRENCY_CODE,
            TIMEUNIT, USER_ROLE, PRICE),

    EDIT_SERVICE_ROLE_PRICE_FOR_SUBSCRIPTION("30059", CURRENCY_CODE, TIMEUNIT,
            USER_ROLE, PRICE),

    EDIT_FREE_PRICE_MODEL_FOR_CUSTOMER_SERVICE("30061"),

    EDIT_FREE_PRICE_MODEL_FOR_SERVICE("30062"),

    EDIT_USER_PRICE_FOR_SUBSCRIPTION("30063", CURRENCY_CODE, TIMEUNIT, RANGE,
            ACTION_NAME, RECURRING_CHARGE),

    EDIT_USER_PRICE_FOR_CUSTOMER_SERVICE("30064", CURRENCY_CODE, TIMEUNIT,
            RANGE, ACTION_NAME, RECURRING_CHARGE),

    EDIT_USER_PRICE_FOR_SERVICE("30065", CURRENCY_CODE, TIMEUNIT, RANGE,
            ACTION_NAME, RECURRING_CHARGE),

    DELETE_PRICE_MODEL_FOR_CUSTOMER_SERVICE("30069"),

    EDIT_ONETIME_FEE_FOR_SERVICE("30070", CURRENCY_CODE, TIMEUNIT, ONE_TIME_FEE),

    EDIT_ONETIME_FEE_FOR_CUSTOMER_SERVICE("30071", CURRENCY_CODE, TIMEUNIT,
            ONE_TIME_FEE),

    LOCALIZE_PRICE_MODEL_FOR_SERVICE("30072", LOCALE, DESCRIPTION, LICENSE),

    LOCALIZE_PRICE_MODEL_FOR_CUSTOMER_SERVICE("30073", LOCALE, DESCRIPTION,
            LICENSE);

    public enum PriceModelType {
        SERVICE, CUSTOMER_SERVICE, SUBSCRIPTION;
    }

    public enum Operation {
        EDIT_EVENT_PRICE,

        EDIT_PARAMETER_SUBSCRIPTION_PRICE,

        EDIT_PARAMETER_USER_PRICE,

        EDIT_PARAMETER_USER_ROLE_PRICE,

        EDIT_CHARGEABLE_PRICE_MODEL,

        EDIT_FREE_PRICE_MODEL,

        EDIT_SUBSCRIPTION_PRICE,

        EDIT_ONETIME_FEE,

        EDIT_USER_PRICE,

        EDIT_SERVICE_ROLE_PRICE,

        DELETE_PRICE_MODEL,

        LOCALIZE_PRICE_MODEL,

    };

    private String operationId;
    private List<AuditLogParameter> parameters = new ArrayList<AuditLogParameter>();
    private PriceModelType priceModelType;
    private Operation priceModelOperation;

    private PriceModelAuditLogOperation(String operationId) {
        String[] s = name().split("FOR");

        this.priceModelOperation = Operation.valueOf(s[0].substring(0,
                s[0].length() - 1));

        this.priceModelType = PriceModelType.valueOf(s[1].substring(1,
                s[1].length()));

        this.operationId = operationId;

        this.parameters.add(SERVICE_ID);
        this.parameters.add(SERVICE_NAME);

        if (priceModelType == PriceModelType.SUBSCRIPTION
                || priceModelType == PriceModelType.CUSTOMER_SERVICE) {
            this.parameters.add(CUSTOMER_ID);
            this.parameters.add(CUSTOMER_NAME);
        }

        if (priceModelType == PriceModelType.SUBSCRIPTION) {
            this.parameters.add(SUBSCRIPTION_NAME);
        }

    }

    private PriceModelAuditLogOperation(String operationId,
            AuditLogParameter... parameters) {

        this(operationId);

        this.parameters.addAll(Arrays.asList(parameters));

    }

    public String getOperationId() {
        return operationId;
    }

    PriceModelType getPriceModelType() {
        return priceModelType;
    }

    public List<AuditLogParameter> getParameters() {
        return parameters;
    }

    Operation getPriceModelOperation() {
        return priceModelOperation;
    }

    static PriceModelAuditLogOperation getOperation(Operation op,
            PriceModelType type) {
        for (PriceModelAuditLogOperation e : PriceModelAuditLogOperation
                .values()) {
            if (e.getPriceModelType() == type
                    && op == e.getPriceModelOperation()) {
                return e;
            }
        }

        return null;
    }
}
