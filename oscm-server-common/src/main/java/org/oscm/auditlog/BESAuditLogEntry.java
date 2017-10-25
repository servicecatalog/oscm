/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.auditlog;

import static org.oscm.auditlog.AuditLogParameter.ACTION_NAME;
import static org.oscm.auditlog.AuditLogParameter.CALCULATION_MODE;
import static org.oscm.auditlog.AuditLogParameter.CURRENCY_CODE;
import static org.oscm.auditlog.AuditLogParameter.CUSTOMER_ID;
import static org.oscm.auditlog.AuditLogParameter.CUSTOMER_NAME;
import static org.oscm.auditlog.AuditLogParameter.EVENT_NAME;
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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.oscm.auditlog.model.AuditLogAction;
import org.oscm.auditlog.model.AuditLogEntry;
import org.oscm.converter.TimeStampUtil;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.PriceModel;
import org.oscm.domobjects.PricedEvent;
import org.oscm.domobjects.PricedOption;
import org.oscm.domobjects.PricedParameter;
import org.oscm.domobjects.PricedProductRole;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.SteppedPrice;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.internal.types.enumtypes.PriceModelType;
import org.oscm.internal.vo.VOServiceDetails;

public class BESAuditLogEntry implements AuditLogEntry {
    private final String operationId;
    private final String operationName;
    private String log = "";
    private final String userId;
    private final String userLocale;
    private final String organizationId;
    private final String organizationName;
    private final Map<AuditLogParameter, String> logParameters = new HashMap<AuditLogParameter, String>();
    ResourceBundle resourceBundle;
    private static final String AUDITLOG_MESSAGE_RESOURCE_NAME = "AuditLogMessages";

    public BESAuditLogEntry(DataService ds, String operationId,
            String operationName, AuditLogParameter... parameters) {
        this.operationId = operationId;
        this.operationName = operationName;
        PlatformUser user = ds.getCurrentUser();
        this.userId = user.getUserId();
        this.userLocale = user.getLocale();
        this.organizationId = user.getOrganization().getOrganizationId();
        this.organizationName = user.getOrganization().getName();
        resourceBundle = ResourceBundle.getBundle(
                AUDITLOG_MESSAGE_RESOURCE_NAME, Locale.ENGLISH);
        setParameterTemplate(parameters);
    }

    private void setParameterTemplate(AuditLogParameter... parameters) {
        String log = "";
        for (AuditLogParameter param : parameters) {
            String parameterName = resourceBundle.getString(param.name());
            log += "|" + parameterName;
        }
        if (log.length() > 0) {
            log += "|";
        }
        setLog(log);
    }

    public void addParameter(AuditLogParameter parameter, String value) {
        logParameters.put(parameter, value);
        String parameterName = resourceBundle.getString(parameter.name());
        if (log.contains("|" + parameterName + "|")
                && !log.contains("|" + parameterName + "=")) {
            log = log.replace("|" + parameterName + "|", "|" + parameterName
                    + "=\"" + escapeValueIfNeeded(value) + "\"" + "|");
        }
    }

    public void addParameter(String parameterName, String value) {
        if (parameterName != null && !parameterName.isEmpty())
            log += (parameterName + "=\"" + escapeValueIfNeeded(value) + "\"" + "|");
    }

    public void removeParameter(AuditLogParameter parameter) {
        logParameters.remove(parameter);
        String parameterName = resourceBundle.getString(parameter.name());
        if (log.contains("|" + parameterName + "|")
                && !log.contains("|" + parameterName + "=")) {
            log = log.replace("|" + parameterName + "|", "|");
        }
    }

    String escapeValueIfNeeded(String value) {
        String newValue = "";
        if (value != null && value.trim().length() > 0) {
            newValue = value.replace("\"", "\\\"");
        }
        return newValue;
    }

    public Map<AuditLogParameter, String> getLogParameters() {
        return logParameters;
    }

    @Override
    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }

    @Override
    public String getOperationId() {
        return escapeValueIfNeeded(operationId);
    }

    @Override
    public String getOperationName() {
        return escapeValueIfNeeded(operationName);
    }

    @Override
    public String getUserId() {
        return escapeValueIfNeeded(userId);
    }

    @Override
    public String getOrganizationId() {
        return escapeValueIfNeeded(organizationId);
    }

    @Override
    public String getOrganizationName() {
        return escapeValueIfNeeded(organizationName);
    }

    public void addSubscription(Subscription subscription) {
        this.addParameter(SUBSCRIPTION_NAME, subscription.getSubscriptionId());
    }

    public void addServiceDetail(VOServiceDetails service) {
        this.addParameter(SERVICE_ID, service.getServiceId());
        this.addParameter(SERVICE_NAME, service.getName());

    }

    public void addProduct(Product product, LocalizerServiceLocal localizer) {
        this.addParameter(SERVICE_ID, TimeStampUtil
                .removeTimestampFromId(getRootTemplateProduct(product)
                        .getProductId()));

        LocalizerFacade facade = getLocalizerFacade(localizer);
        String nameForCustomer = facade.getText(product.getTemplateOrSelf()
                .getKey(), LocalizedObjectTypes.PRODUCT_MARKETING_NAME);

        this.addParameter(SERVICE_NAME, nameForCustomer);
    }

    public void addCustomer(Organization customer) {
        this.addParameter(CUSTOMER_ID, customer.getOrganizationId());
        this.addParameter(CUSTOMER_NAME, customer.getName());
    }

    public void addPricedEvent(PricedEvent pricedEvent) {
        this.addParameter(CURRENCY_CODE, pricedEvent.getPriceModel()
                .getCurrency().getCurrencyISOCode());
        this.addParameter(EVENT_NAME, pricedEvent.getEvent()
                .getEventIdentifier());
        this.addParameter(RANGE, "1-ANY");
        this.addParameter(PRICE, pricedEvent.getEventPrice().toString());
    }

    public void addSteppedPricedEvent(SteppedPrice steppedPrice) {
        this.addParameter(CURRENCY_CODE, steppedPrice.getPricedEvent()
                .getPriceModel().getCurrency().getCurrencyISOCode());
        this.addParameter(EVENT_NAME, steppedPrice.getPricedEvent().getEvent()
                .getEventIdentifier());
        if (steppedPrice.getLimit() == null) {
            this.addParameter(RANGE, "ANY ABOVE");
        } else {
            this.addParameter(RANGE, steppedPrice.getLimit().toString());
        }
        this.addParameter(PRICE, steppedPrice.getPrice().toString());
    }

    public void addParameterSubscriptionPrice(PricedParameter pricedParameter) {
        this.addParameter(CURRENCY_CODE, pricedParameter.getPriceModel()
                .getCurrency().getCurrencyISOCode());
        this.addParameter(TIMEUNIT, pricedParameter.getPriceModel().getPeriod()
                .name());
        this.addParameter(PARAMETER_NAME, pricedParameter.getParameter()
                .getParameterDefinition().getParameterId());
        this.addParameter(RANGE, "1-ANY");
        this.removeParameter(OPTION_NAME);
        this.addParameter(PRICE, pricedParameter.getPricePerSubscription()
                .toString());
    }

    public void addParameterUserPrice(PricedParameter pricedParameter) {
        this.addParameter(CURRENCY_CODE, pricedParameter.getPriceModel()
                .getCurrency().getCurrencyISOCode());
        this.addParameter(TIMEUNIT, pricedParameter.getPriceModel().getPeriod()
                .name());
        this.addParameter(PARAMETER_NAME, pricedParameter.getParameter()
                .getParameterDefinition().getParameterId());
        this.removeParameter(OPTION_NAME);
        this.addParameter(PRICE, pricedParameter.getPricePerUser().toString());
    }

    public void addParameterUserRolePrice(PricedProductRole pricedRole) {
        this.addParameter(CURRENCY_CODE, pricedRole.getPricedParameter()
                .getPriceModel().getCurrency().getCurrencyISOCode());
        this.addParameter(TIMEUNIT, pricedRole.getPricedParameter()
                .getPriceModel().getPeriod().name());
        this.addParameter(USER_ROLE, pricedRole.getRoleDefinition().getRoleId());
        this.addParameter(PARAMETER_NAME, pricedRole.getPricedParameter()
                .getParameter().getParameterDefinition().getParameterId());
        this.removeParameter(OPTION_NAME);
        this.addParameter(PRICE, pricedRole.getPricePerUser().toString());
    }

    public void addParameterOptionUserRolePrice(PricedProductRole pricedRole,
            String optionName) {
        this.addParameter(CURRENCY_CODE, pricedRole.getPricedOption()
                .getPricedParameter().getPriceModel().getCurrency()
                .getCurrencyISOCode());
        this.addParameter(TIMEUNIT, pricedRole.getPricedOption()
                .getPricedParameter().getPriceModel().getPeriod().name());
        this.addParameter(USER_ROLE, pricedRole.getRoleDefinition().getRoleId());
        this.addParameter(PARAMETER_NAME, pricedRole.getPricedOption()
                .getPricedParameter().getParameter().getParameterDefinition()
                .getParameterId());
        this.addParameter(OPTION_NAME, optionName);
        this.addParameter(PRICE, pricedRole.getPricePerUser().toString());
    }

    public void addParameterOptionSubscriptionPrice(PricedOption pricedOption,
            String optionName) {
        this.addParameter(CURRENCY_CODE, pricedOption.getPricedParameter()
                .getPriceModel().getCurrency().getCurrencyISOCode());
        this.addParameter(TIMEUNIT, pricedOption.getPricedParameter()
                .getPriceModel().getPeriod().name());
        this.addParameter(PARAMETER_NAME, pricedOption.getPricedParameter()
                .getParameter().getParameterDefinition().getParameterId());
        this.addParameter(RANGE, "1-ANY");
        this.addParameter(OPTION_NAME, optionName);
        this.addParameter(PRICE, pricedOption.getPricePerSubscription()
                .toString());
    }

    public void addParameterOptionUserPrice(PricedOption pricedOption,
            String optionName) {
        this.addParameter(CURRENCY_CODE, pricedOption.getPricedParameter()
                .getPriceModel().getCurrency().getCurrencyISOCode());
        this.addParameter(TIMEUNIT, pricedOption.getPricedParameter()
                .getPriceModel().getPeriod().name());
        this.addParameter(PARAMETER_NAME, pricedOption.getPricedParameter()
                .getParameter().getParameterDefinition().getParameterId());
        this.addParameter(OPTION_NAME, optionName);
        this.addParameter(PRICE, pricedOption.getPricePerUser().toString());
    }

    public void addParameterSteppedPrice(SteppedPrice steppedPrice) {
        this.addParameter(CURRENCY_CODE, steppedPrice.getPricedParameter()
                .getPriceModel().getCurrency().getCurrencyISOCode());
        this.addParameter(TIMEUNIT, steppedPrice.getPricedParameter()
                .getPriceModel().getPeriod().name());
        this.addParameter(PARAMETER_NAME, steppedPrice.getPricedParameter()
                .getParameter().getParameterDefinition().getParameterId());
        if (steppedPrice.getLimit() == null) {
            this.addParameter(RANGE, "ANY ABOVE");
        } else {
            this.addParameter(RANGE, steppedPrice.getLimit().toString());
        }
        this.removeParameter(OPTION_NAME);
        this.addParameter(PRICE, steppedPrice.getPrice().toString());
    }

    public void addPriceModel(PriceModel priceModel) {
        if (priceModel.getType() != PriceModelType.FREE_OF_CHARGE) {
            this.addParameter(CURRENCY_CODE, priceModel.getCurrency()
                    .getCurrencyISOCode());
            this.addParameter(TIMEUNIT, priceModel.getPeriod().name());
            this.addParameter(CALCULATION_MODE, priceModel.getType().name());
            if (priceModel.getFreePeriod() > 0) {
                this.addParameter(TRIAL_PERIOD, "ON");
            } else {
                this.addParameter(TRIAL_PERIOD, "OFF");
            }
            this.addParameter(AuditLogParameter.DAYS_OF_TRIAL, String
                    .valueOf(priceModel.getDataContainer().getFreePeriod()));
        }
    }

    public void addSubscriptionPrice(PriceModel priceModel) {
        this.addParameter(CURRENCY_CODE, priceModel.getCurrency()
                .getCurrencyISOCode());
        this.addParameter(TIMEUNIT, priceModel.getPeriod().name());
        this.addParameter(ONE_TIME_FEE, priceModel.getOneTimeFee().toString());
        this.addParameter(RECURRING_CHARGE, priceModel.getPricePerPeriod()
                .toString());
    }

    public void addOneTimeFee(PriceModel priceModel) {
        this.addParameter(CURRENCY_CODE, priceModel.getCurrency()
                .getCurrencyISOCode());
        this.addParameter(TIMEUNIT, priceModel.getPeriod().name());
        this.addParameter(ONE_TIME_FEE, priceModel.getOneTimeFee().toString());
    }

    public void addUserPrice(PriceModel priceModel) {
        this.addParameter(CURRENCY_CODE, priceModel.getCurrency()
                .getCurrencyISOCode());
        this.addParameter(TIMEUNIT, priceModel.getPeriod().name());
        this.addParameter(RANGE, "1-ANY");
        this.addParameter(RECURRING_CHARGE, priceModel
                .getPricePerUserAssignment().toString());
    }

    public void addSteppedUserPrice(SteppedPrice steppedPrice) {
        this.addParameter(CURRENCY_CODE, steppedPrice.getPriceModel()
                .getCurrency().getCurrencyISOCode());
        this.addParameter(TIMEUNIT, steppedPrice.getPriceModel().getPeriod()
                .name());
        if (steppedPrice.getLimit() == null) {
            this.addParameter(RANGE, "ANY ABOVE");
        } else {
            this.addParameter(RANGE, steppedPrice.getLimit().toString());
        }
        this.addParameter(RECURRING_CHARGE, steppedPrice.getPrice().toString());
    }

    public void addAction(AuditLogAction action) {
        if (AuditLogAction.NONE.equals(action)) {
            removeParameter(ACTION_NAME);
        } else {
            addParameter(ACTION_NAME, action.name());
        }

    }

    Product getRootTemplateProduct(Product product) {
        if (product.getTemplate() == null) {
            return product;
        } else if (!product.getTemplate().equals(product)) {
            return getRootTemplateProduct(product.getTemplate());
        }
        return product;
    }

    LocalizerFacade getLocalizerFacade(LocalizerServiceLocal localizer) {
        return new LocalizerFacade(localizer, userLocale);
    }
}
