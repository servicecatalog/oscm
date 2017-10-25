/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 19.04.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.subscriptionservice.auditlog;

import java.util.BitSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import org.oscm.auditlog.AuditLogData;
import org.oscm.auditlog.AuditLogParameter;
import org.oscm.auditlog.BESAuditLogEntry;
import org.oscm.auditlog.model.AuditLogAction;
import org.oscm.auditlog.model.AuditLogEntry;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.BillingContact;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.Parameter;
import org.oscm.domobjects.PaymentInfo;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.RoleDefinition;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.TechnicalProductOperation;
import org.oscm.domobjects.UsageLicense;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.interceptor.AuditLoggingEnabled;
import org.oscm.types.enumtypes.UdaTargetType;
import org.oscm.validation.ArgumentValidator;
import org.oscm.internal.types.enumtypes.ParameterValueType;
import org.oscm.internal.vo.VOLocalizedText;
import org.oscm.internal.vo.VOPriceModelLocalization;

@Stateless
@Interceptors(AuditLoggingEnabled.class)
public class SubscriptionAuditLogCollector {

    @EJB(beanInterface = LocalizerServiceLocal.class)
    LocalizerServiceLocal localizer;

    private static int DESCRIPTION_LOCALIZED = 1;
    private static int LICENSE_LOCALIZED = 0;

    public void editSubscriptionAttributeByServiceManager(DataService ds,
            Subscription subscription, String parameterName,
            String parameterValue) {

        BESAuditLogEntry logEntry = createAuditLogEntry(
                ds,
                SubscriptionAuditLogOperation.EDIT_SUBSCRIPTION_ATTRIBUTE_BY_SERVICE_MANAGER,
                subscription);

        logEntry.addParameter(AuditLogParameter.ATTRIBUTE_NAME, parameterName);
        logEntry.addParameter(AuditLogParameter.ATTRIBUTE_VALUE, parameterValue);

        AuditLogData.add(logEntry);
    }

    public void reportIssueOperation(DataService ds, Subscription subscription,
            String subject) {

        BESAuditLogEntry logEntry = createAuditLogEntry(ds,
                SubscriptionAuditLogOperation.SUBSCRIPTION_REPORT_ISSUE,
                subscription);

        logEntry.addParameter(AuditLogParameter.SUBSCRIPTION_ISSUE_SUBJECT,
                subject);

        AuditLogData.add(logEntry);
    }

    public void subscribeToService(DataService ds, Subscription subscription) {

        AuditLogEntry logEntry = createAuditLogEntry(ds,
                SubscriptionAuditLogOperation.SUBSCRIBE_SERVICE, subscription);

        AuditLogData.add(logEntry);
    }

    public void unsubscribeFromService(DataService ds, Subscription subscription) {
        AuditLogEntry logEntry = createAuditLogEntry(ds,
                SubscriptionAuditLogOperation.UNSUBSCRIBE_FROM_SERVICE,
                subscription);
        AuditLogData.add(logEntry);
    }

    public void editPaymentType(DataService ds, Subscription subscription,
            PaymentInfo paymentInfo) {

        AuditLogAction action = AuditLogData.determineAction(
                subscription.getPaymentInfo(), paymentInfo);

        if (AuditLogAction.NONE.equals(action)) {
            return;
        }

        BESAuditLogEntry logEntry = createAuditLogEntry(ds,
                SubscriptionAuditLogOperation.EDIT_SUBSCRIPTION_PAYMENT_TYPE,
                subscription);

        PaymentInfo logPaymentInfo = paymentInfo;

        // log the existing payment information in case of delete action
        if (AuditLogAction.DELETE.equals(action)) {
            logPaymentInfo = subscription.getPaymentInfo();
        }

        logEntry.addParameter(AuditLogParameter.PAYMENT_NAME,
                logPaymentInfo.getPaymentInfoId());
        logEntry.addParameter(AuditLogParameter.PAYMENT_TYPE, logPaymentInfo
                .getPaymentType().getPaymentTypeId());

        AuditLogData.add(logEntry);

    }

    public void terminateSubscription(DataService ds,
            Subscription subscription, String oldSubscId, String reason) {
        // its not possible to use the existing Subscription and replace the
        // subscritpionId, because this
        // will cause a new HistoryEntry with the wrong ID and so it's not
        // possible to reuse this SubscriptionID
        Subscription tempSubscription = new Subscription();
        tempSubscription.setSubscriptionId(oldSubscId);
        tempSubscription.setProduct(subscription.getProduct());

        BESAuditLogEntry logEntry = createAuditLogEntry(ds,
                SubscriptionAuditLogOperation.TERMINATE_SUBSCRIPTION,
                tempSubscription);
        logEntry.addParameter(AuditLogParameter.REASON, reason);
        AuditLogData.add(logEntry);

    }

    public void executeService(DataService ds, Subscription subscription,
            TechnicalProductOperation op, Map<String, String> parameters) {
        BESAuditLogEntry logEntry = createAuditLogEntry(ds,
                SubscriptionAuditLogOperation.EXECUTE_SERVICE_OPERATION,
                subscription);
        logEntry.addParameter(AuditLogParameter.SERVICE_OPERATION,
                op.getOperationId());
        if (parameters != null) {
            for (Entry<String, String> parameter : parameters.entrySet()) {
                logEntry.addParameter(parameter.getKey(), parameter.getValue());
            }
        }
        AuditLogData.add(logEntry);
    }

    public void editBillingAddress(DataService ds, Subscription subscription,
            BillingContact billingContact) {

        AuditLogAction action = AuditLogData.determineAction(
                subscription.getBillingContact(), billingContact);

        if (AuditLogAction.NONE.equals(action)) {
            return;
        }

        BESAuditLogEntry logEntry = createAuditLogEntry(
                ds,
                SubscriptionAuditLogOperation.EDIT_SUBSCRIPTION_BILLING_ADDRESS,
                subscription);

        BillingContact logBillingContact = billingContact;

        // log the existing billing contact in case of delete action
        if (AuditLogAction.DELETE.equals(action)) {
            logBillingContact = subscription.getBillingContact();
        }

        logEntry.addParameter(AuditLogParameter.ADDRESS_NAME,
                logBillingContact.getBillingContactId());

        StringBuilder addressDetails = new StringBuilder();
        final String separator = "|";
        addressDetails.append(logBillingContact.getCompanyName())
                .append(separator).append(logBillingContact.getEmail())
                .append(separator).append(logBillingContact.getAddress());
        logEntry.addParameter(AuditLogParameter.ADDRESS_DETAILS,
                addressDetails.toString());

        AuditLogData.add(logEntry);

    }

    public void upDowngradeSubscription(DataService ds,
            Subscription subscription, Product initialProduct,
            Product dbTargetProduct) {

        BESAuditLogEntry logEntry = createAuditLogEntry(ds,
                SubscriptionAuditLogOperation.UP_DOWNGRADE_SUBSCRIPTION,
                subscription, initialProduct);

        logEntry.addParameter(AuditLogParameter.NEW_SERVICE_ID,
                dbTargetProduct.getProductId());

        LocalizerFacade facade = getLocalizerFacade(localizer, ds
                .getCurrentUser().getLocale());
        String nameForCustomer = facade.getText(dbTargetProduct
                .getTemplateOrSelf().getKey(),
                LocalizedObjectTypes.PRODUCT_MARKETING_NAME);

        logEntry.addParameter(AuditLogParameter.NEW_SERVICE_NAME,
                nameForCustomer);

        AuditLogData.add(logEntry);
    }

    public void viewSubscription(DataService ds, Subscription subscription,
            Organization customer) {

        BESAuditLogEntry logEntry = createAuditLogEntry(ds,
                SubscriptionAuditLogOperation.VIEW_SUBSCRIPTION, subscription);

        logEntry.addCustomer(customer);
        AuditLogData.add(logEntry);
    }

    public void assignUserRoleForService(DataService ds,
            Subscription subscription, PlatformUser usr, RoleDefinition roleDef) {
        if (roleDef == null) {
            return;
        }
        BESAuditLogEntry logEntry = createAuditLogEntry(ds,
                SubscriptionAuditLogOperation.ASSIGN_USERROLE_FOR_SERVICE,
                subscription);

        logEntry.addParameter(AuditLogParameter.TARGET_USER, usr.getUserId());
        logEntry.addParameter(AuditLogParameter.USER_ROLE, roleDef.getRoleId());
        AuditLogData.add(logEntry);
    }

    public void deassignUserRoleForService(DataService ds,
            Subscription subscription, PlatformUser usr, RoleDefinition roleDef) {
        if (roleDef == null) {
            return;
        }
        BESAuditLogEntry logEntry = createAuditLogEntry(ds,
                SubscriptionAuditLogOperation.DEASSIGN_USERROLE_FOR_SERVICE,
                subscription);

        logEntry.addParameter(AuditLogParameter.TARGET_USER, usr.getUserId());
        logEntry.addParameter(AuditLogParameter.USER_ROLE, roleDef.getRoleId());
        AuditLogData.add(logEntry);
    }

    public void assignUserToSubscription(DataService ds,
            Subscription subscription, UsageLicense usageLicense) {
        if (usageLicense == null) {
            return;
        }
        BESAuditLogEntry logEntry = createAuditLogEntry(ds,
                SubscriptionAuditLogOperation.ASSIGN_USER_TO_SUBSCRIPTION,
                subscription);

        logEntry.addParameter(AuditLogParameter.TARGET_USER, usageLicense
                .getUser().getUserId());
        AuditLogData.add(logEntry);
    }

    public void deassignUserFromSubscription(DataService ds,
            Subscription subscription, List<UsageLicense> usageLicenses) {

        if (usageLicenses == null || usageLicenses.isEmpty()) {
            return;
        }
        BESAuditLogEntry logEntry = createAuditLogEntry(ds,
                SubscriptionAuditLogOperation.DEASSIGN_USER_FROM_SUBSCRIPTION,
                subscription);

        String usrs = "";
        for (UsageLicense usageLicense : usageLicenses) {
            usrs += usageLicense.getUser().getUserId() + ",";
        }
        usrs = usrs.substring(0, usrs.length() - 1);
        logEntry.addParameter(AuditLogParameter.TARGET_USER, usrs);

        AuditLogData.add(logEntry);
    }

    public void editSubscriptionParameterConfiguration(DataService ds,
            Product product, List<Parameter> modifiedParameters) {

        if (modifiedParameters.isEmpty()) {
            return;
        }

        Subscription subscription = product.getOwningSubscription();

        for (Parameter parameter : modifiedParameters) {
            BESAuditLogEntry logEntry = createAuditLogEntry(
                    ds,
                    SubscriptionAuditLogOperation.EDIT_SUBSCRIPTION_PARAMETER_CONFIGURATION,
                    subscription);
            String parameterValue;
            logEntry.addParameter(AuditLogParameter.PARAMETER_NAME, parameter
                    .getParameterDefinition().getParameterId());
            if (parameter.getParameterDefinition().getValueType()
                    .equals(ParameterValueType.BOOLEAN)) {
                if (parameter.getBooleanValue()) {
                    parameterValue = "ON";
                } else {
                    parameterValue = "OFF";
                }

            } else if (parameter.getParameterDefinition().getValueType()
                    .equals(ParameterValueType.ENUMERATION)) {
                parameterValue = localizer.getLocalizedTextFromDatabase("en",
                        parameter.getParameterOption(parameter.getValue())
                                .getKey(),
                        LocalizedObjectTypes.OPTION_PARAMETER_DEF_DESC);
            } else {
                parameterValue = parameter.getValue();
            }
            logEntry.addParameter(AuditLogParameter.PARAMETER_VALUE,
                    parameterValue);
            AuditLogData.add(logEntry);
        }

    }

    public void editSubscriptionOwner(DataService ds,
            Subscription subscription, PlatformUser oldOwner) {

        PlatformUser newOwner = subscription.getOwner();

        if (oldOwner == null && newOwner == null) {
            return;
        }

        if (oldOwner != null && newOwner != null) {
            if (oldOwner.getUserId().equals(newOwner.getUserId())) {
                return;
            }
        }

        BESAuditLogEntry logEntry = createAuditLogEntry(ds,
                SubscriptionAuditLogOperation.EDIT_SUBSCRIPTION_OWNER,
                subscription);

        if (newOwner != null) {
            logEntry.addParameter(AuditLogParameter.SUBSCRIPTION_OWNER,
                    newOwner.getUserId());
        } else {
            logEntry.addParameter(AuditLogParameter.SUBSCRIPTION_OWNER,
                    "NO_OWNER");
        }
        AuditLogData.add(logEntry);

    }

    public void editSubscriptionAndCustomerAttributeByCustomer(DataService ds,
            Organization customer, Subscription subscription,
            String parameterName, String parameterValue, String targetType) {
        BESAuditLogEntry logEntry = null;
        if (UdaTargetType.CUSTOMER.toString().equals(targetType)) {
            logEntry = createAuditLogEntry(
                    ds,
                    SubscriptionAuditLogOperation.EDIT_CUSTOMER_ATTRIBUTE_BY_CUSTOMER,
                    customer);
        } else {
            logEntry = createAuditLogEntry(
                    ds,
                    SubscriptionAuditLogOperation.EDIT_SUBSCRIPTION_ATTRIBUTE_BY_CUSTOMER,
                    subscription);
        }
        logEntry.addParameter(AuditLogParameter.ATTRIBUTE_NAME, parameterName);
        logEntry.addParameter(AuditLogParameter.ATTRIBUTE_VALUE, parameterValue);

        AuditLogData.add(logEntry);
    }

    public void localizePriceModel(DataService ds, Subscription subscription,
            VOPriceModelLocalization localizationOriginal,
            VOPriceModelLocalization localizationToBeUpdate) {
        ArgumentValidator.notNull("localizationToBeUpdate",
                localizationToBeUpdate);
        ArgumentValidator.notNull("localizationOriginal", localizationOriginal);
        Map<String, LocalizedAuditLogEntryParameters> mapToBeUpdate = prepareLocalizationMap(localizationToBeUpdate);
        Map<String, LocalizedAuditLogEntryParameters> mapOriginal = prepareLocalizationMap(localizationOriginal);
        Map<String, BitSet> localizationMap = collectLocalizationStates(
                mapToBeUpdate, mapOriginal);
        for (String locale : localizationMap.keySet()) {
            boolean descriptionLocalized = isDescriptionLocalized(
                    localizationMap, locale);
            boolean licenseLocalized = isLicenseLocalized(localizationMap,
                    locale);

            addLogEntryForAuditLogData(ds, subscription, locale,
                    descriptionLocalized, licenseLocalized);
        }

    }

    Map<String, LocalizedAuditLogEntryParameters> prepareLocalizationMap(
            VOPriceModelLocalization localization) {
        Map<String, LocalizedAuditLogEntryParameters> map = new LinkedHashMap<String, LocalizedAuditLogEntryParameters>();

        // collect all locale, description and license, and add them
        // to hashMap(locale,description license)
        for (VOLocalizedText localizedText : localization.getDescriptions()) {
            LocalizedAuditLogEntryParameters auditLogEntryData = new LocalizedAuditLogEntryParameters();
            auditLogEntryData.setDescription(localizedText.getText());
            map.put(localizedText.getLocale(), auditLogEntryData);
        }

        for (VOLocalizedText localizedText : localization.getLicenses()) {
            LocalizedAuditLogEntryParameters auditLogEntryData = map
                    .get(localizedText.getLocale());
            if (auditLogEntryData != null) {
                auditLogEntryData.setLicense(localizedText.getText());
            } else {
                LocalizedAuditLogEntryParameters newAuditLogEntryData = new LocalizedAuditLogEntryParameters();
                newAuditLogEntryData.setLicense(localizedText.getText());
                map.put(localizedText.getLocale(), newAuditLogEntryData);
            }
        }
        return map;
    }

    Map<String, BitSet> collectLocalizationStates(
            Map<String, LocalizedAuditLogEntryParameters> mapToBeUpdate,
            Map<String, LocalizedAuditLogEntryParameters> mapStored) {
        Map<String, BitSet> localeMap = new LinkedHashMap<String, BitSet>();
        Iterator<Entry<String, LocalizedAuditLogEntryParameters>> iter = mapToBeUpdate
                .entrySet().iterator();
        while (iter.hasNext()) {
            Entry<String, LocalizedAuditLogEntryParameters> entry = iter.next();
            BitSet newLocalized = new BitSet(2);
            LocalizedAuditLogEntryParameters storedLocalizedValue = mapStored
                    .get(entry.getKey());
            LocalizedAuditLogEntryParameters newLocalizedValue = entry
                    .getValue();
            // replace null parameter to empty string
            storedLocalizedValue = validateLocalizedParameter(storedLocalizedValue);
            newLocalizedValue = validateLocalizedParameter(newLocalizedValue);
            // do not log if localized text does not change
            if (storedLocalizedValue.equals(newLocalizedValue)) {
                continue;
            }
            newLocalized.set(
                    DESCRIPTION_LOCALIZED,
                    !newLocalizedValue.getDescription().equals(
                            storedLocalizedValue.getDescription()));
            newLocalized.set(LICENSE_LOCALIZED, !newLocalizedValue.getLicense()
                    .equals(storedLocalizedValue.getLicense()));
            localeMap.put(entry.getKey(), newLocalized);

        }
        return localeMap;
    }

    private LocalizedAuditLogEntryParameters validateLocalizedParameter(
            LocalizedAuditLogEntryParameters localizedValue) {
        if (localizedValue != null) {
            localizedValue
                    .setDescription(localizedValue.getDescription() == null ? ""
                            : localizedValue.getDescription());
            localizedValue.setLicense(localizedValue.getLicense() == null ? ""
                    : localizedValue.getLicense());
        } else {
            localizedValue = new LocalizedAuditLogEntryParameters();
        }
        return localizedValue;
    }

    private boolean isDescriptionLocalized(Map<String, BitSet> localeMap,
            String locale) {
        return localeMap.get(locale) != null
                && localeMap.get(locale).get(DESCRIPTION_LOCALIZED);
    }

    private boolean isLicenseLocalized(Map<String, BitSet> localeMap,
            String locale) {
        return localeMap.get(locale) != null
                && localeMap.get(locale).get(LICENSE_LOCALIZED);
    }

    /**
     * Construct
     * #SubscriptionAuditLogOperation.LOCALIZE_PRICE_MODEL_FOR_SUBSCRIPTION
     * audit log entry
     * 
     * @param ds
     *            the data service
     * @param subscription
     *            the subscription
     * @param locale
     *            the locale
     * @param isDescriptionLocalized
     *            "YES" indicates description localized for <code>locale</code>
     *            "NO" indicates description not localized for
     *            <code>locale</code>
     * @param isLicenseLocalized
     *            <code>true</code> indicates license localized for
     *            <code>locale</code>, <code>false</code> indicates license not
     *            localized for <code>locale</code>
     */
    public void addLogEntryForAuditLogData(DataService ds,
            Subscription subscription, String locale,
            boolean isDescriptionLocalized, boolean isLicenseLocalized) {
        BESAuditLogEntry logEntry = createAuditLogEntry(
                ds,
                SubscriptionAuditLogOperation.LOCALIZE_PRICE_MODEL_FOR_SUBSCRIPTION,
                subscription);
        logEntry.addCustomer(subscription.getOrganization());
        logEntry.addParameter(AuditLogParameter.LOCALE, locale);
        logEntry.addParameter(AuditLogParameter.DESCRIPTION,
                toYesOrNo(isDescriptionLocalized));
        logEntry.addParameter(AuditLogParameter.LICENSE,
                toYesOrNo(isLicenseLocalized));
        AuditLogData.add(logEntry);
    }

    private String toYesOrNo(boolean parameter) {
        return parameter ? "YES" : "NO";
    }

    private BESAuditLogEntry createAuditLogEntry(DataService ds,
            SubscriptionAuditLogOperation operation, Subscription subscription) {
        BESAuditLogEntry logEntry = new BESAuditLogEntry(ds,
                operation.getOperationId(), operation.toString(),
                operation.getParameters());
        logEntry.addProduct(subscription.getProduct(), localizer);
        logEntry.addSubscription(subscription);
        return logEntry;
    }

    private BESAuditLogEntry createAuditLogEntry(DataService ds,
            SubscriptionAuditLogOperation operation, Subscription subscription,
            Product initialProduct) {
        BESAuditLogEntry logEntry = new BESAuditLogEntry(ds,
                operation.getOperationId(), operation.toString(),
                operation.getParameters());

        logEntry.addProduct(initialProduct, localizer);
        logEntry.addSubscription(subscription);
        return logEntry;
    }

    private BESAuditLogEntry createAuditLogEntry(DataService ds,
            SubscriptionAuditLogOperation operation, Organization customer) {
        BESAuditLogEntry logEntry = new BESAuditLogEntry(ds,
                operation.getOperationId(), operation.toString(),
                operation.getParameters());
        logEntry.addCustomer(customer);
        return logEntry;
    }

    LocalizerFacade getLocalizerFacade(LocalizerServiceLocal localizer,
            String userLocale) {
        return new LocalizerFacade(localizer, userLocale);
    }
}
