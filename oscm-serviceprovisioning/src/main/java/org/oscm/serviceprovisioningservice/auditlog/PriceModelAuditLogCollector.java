/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 19.04.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.auditlog;

import static org.oscm.auditlog.AuditLogParameter.CURRENCY_CODE;
import static org.oscm.auditlog.AuditLogParameter.CUSTOMER_ID;
import static org.oscm.auditlog.AuditLogParameter.CUSTOMER_NAME;
import static org.oscm.auditlog.AuditLogParameter.DESCRIPTION;
import static org.oscm.auditlog.AuditLogParameter.LICENSE;
import static org.oscm.auditlog.AuditLogParameter.LOCALE;
import static org.oscm.auditlog.AuditLogParameter.PRICE;
import static org.oscm.auditlog.AuditLogParameter.TIMEUNIT;
import static org.oscm.auditlog.AuditLogParameter.USER_ROLE;

import java.math.BigDecimal;
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
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PriceModel;
import org.oscm.domobjects.PricedEvent;
import org.oscm.domobjects.PricedOption;
import org.oscm.domobjects.PricedParameter;
import org.oscm.domobjects.PricedProductRole;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.SteppedPrice;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.SupportedCurrency;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.interceptor.AuditLoggingEnabled;
import org.oscm.serviceprovisioningservice.auditlog.PriceModelAuditLogOperation.Operation;
import org.oscm.serviceprovisioningservice.auditlog.PriceModelAuditLogOperation.PriceModelType;
import org.oscm.validation.ArgumentValidator;
import org.oscm.internal.types.enumtypes.PricingPeriod;
import org.oscm.internal.types.enumtypes.ServiceType;
import org.oscm.internal.vo.VOLocalizedText;
import org.oscm.internal.vo.VOPriceModelLocalization;

@Stateless
@Interceptors(AuditLoggingEnabled.class)
public class PriceModelAuditLogCollector {

    @EJB(beanInterface = LocalizerServiceLocal.class)
    LocalizerServiceLocal localizer;

    private static int DESCRIPTION_LOCALIZED = 1;
    private static int LICENSE_LOCALIZED = 0;

    private void logEditEventPrice(DataService ds, PricedEvent pricedEvent) {

        BESAuditLogEntry logEntry = createLogEntry(ds,
                Operation.EDIT_EVENT_PRICE, pricedEvent.getPriceModel());
        logEntry.addPricedEvent(pricedEvent);
        logEntry.addAction(AuditLogAction.NONE);
        AuditLogData.add(logEntry);
    }

    public BESAuditLogEntry createLogEntry(DataService ds, Operation op,
            PriceModel priceModel) {

        Product product = priceModel.getProduct();
        Subscription subscription = product.getOwningSubscription();

        PriceModelAuditLogOperation operation = determineOperation(priceModel,
                op);
        BESAuditLogEntry logEntry = createAuditLogEntry(ds, operation, product,
                subscription);

        if (operation.getPriceModelType() == PriceModelType.SUBSCRIPTION) {
            logEntry.addCustomer(subscription.getOrganization());
        } else if (operation.getPriceModelType() == PriceModelType.CUSTOMER_SERVICE) {
            logEntry.addCustomer(product.getTargetCustomer());
        }
        return logEntry;
    }

    PriceModelAuditLogOperation determineOperation(PriceModel priceModel,
            Operation op) {

        PriceModelType priceModelType = determinePriceModelType(priceModel);
        return PriceModelAuditLogOperation.getOperation(op, priceModelType);
    }

    PriceModelType determinePriceModelType(PriceModel priceModel) {
        PriceModelType priceModelType = PriceModelType.SERVICE;

        if (ServiceType.isSubscription(priceModel.getProduct().getType())) {
            priceModelType = PriceModelType.SUBSCRIPTION;
        } else if (ServiceType.isCustomerTemplate(priceModel.getProduct()
                .getType())) {
            priceModelType = PriceModelType.CUSTOMER_SERVICE;
        }
        return priceModelType;
    }

    public void editEventPrice(DataService ds, PricedEvent pricedEvent,
            BigDecimal oldPrice) {

        boolean priceChanged = oldPrice != null
                && pricedEvent.getEventPrice().compareTo(oldPrice) != 0;
        if (priceChanged) {
            logEditEventPrice(ds, pricedEvent);
        }
    }

    public void removeEventPrice(DataService ds, long voPriceModelKey,
            PricedEvent pricedEvent) {

        // to avoid duplicate logging, already done in createEventPrice
        if (voPriceModelKey > 0) {
            pricedEvent.setEventPrice(BigDecimal.ZERO);
            logEditEventPrice(ds, pricedEvent);
        }
    }

    public void editEventSteppedPrice(DataService ds,
            SteppedPrice steppedPrice, BigDecimal oldPrice, Long oldLimit) {

        if (steppedPriceDataChanged(steppedPrice, oldPrice, oldLimit)) {
            logEventSteppedPrice(ds, steppedPrice, AuditLogAction.UPDATE);
        }
    }

    boolean steppedPriceDataChanged(SteppedPrice steppedPrice,
            BigDecimal oldPrice, Long oldLimit) {

        boolean priceChanged = oldPrice != null
                && steppedPrice.getPrice().compareTo(oldPrice) != 0;
        if (priceChanged) {
            return true;
        }

        boolean limitChanged = oldLimit != null
                && steppedPrice.getLimit() != null
                && steppedPrice.getLimit().compareTo(oldLimit) != 0;
        if (limitChanged) {
            return true;
        }

        return false;
    }

    public void removeEventSteppedPrice(DataService ds,
            SteppedPrice steppedPrice) {

        logEventSteppedPrice(ds, steppedPrice, AuditLogAction.DELETE);
    }

    public void insertEventSteppedPrice(DataService ds,
            SteppedPrice steppedPrice) {

        logEventSteppedPrice(ds, steppedPrice, AuditLogAction.INSERT);
    }

    private void logEventSteppedPrice(DataService ds,
            SteppedPrice steppedPrice, AuditLogAction action) {

        PricedEvent pricedEvent = steppedPrice.getPricedEvent();
        BESAuditLogEntry logEntry = createLogEntry(ds,
                Operation.EDIT_EVENT_PRICE, pricedEvent.getPriceModel());
        logEntry.addSteppedPricedEvent(steppedPrice);
        logEntry.addAction(action);
        AuditLogData.add(logEntry);
    }

    private void logParameterSubscriptionPrice(DataService ds,
            PricedParameter pricedParameter) {

        BESAuditLogEntry logEntry = createLogEntry(ds,
                Operation.EDIT_PARAMETER_SUBSCRIPTION_PRICE,
                pricedParameter.getPriceModel());
        logEntry.addParameterSubscriptionPrice(pricedParameter);
        logEntry.addAction(AuditLogAction.NONE);
        AuditLogData.add(logEntry);
    }

    public void editParameterSubscriptionPrice(DataService ds,
            PricedParameter pricedParameter, BigDecimal oldPrice) {

        boolean priceChanged = oldPrice != null
                && pricedParameter.getPricePerSubscription()
                        .compareTo(oldPrice) != 0;
        if (priceChanged) {
            logParameterSubscriptionPrice(ds, pricedParameter);
        }
    }

    public void removeParameterSubscriptionPrice(DataService ds,
            long voPriceModelKey, PricedParameter pricedParameter) {

        // to avoid duplicate logging, already done in createPricedParameter
        if (voPriceModelKey > 0) {
            pricedParameter.setPricePerSubscription(BigDecimal.ZERO);
            logParameterSubscriptionPrice(ds, pricedParameter);
        }
    }

    public void editParameterOptionSubscriptionPrice(DataService ds,
            PricedOption pricedOption, BigDecimal oldPrice) {

        boolean priceChanged = oldPrice != null
                && pricedOption.getPricePerSubscription().compareTo(oldPrice) != 0;
        if (priceChanged) {
            logParameterOptionSubscriptionPrice(ds, pricedOption);
        }
    }

    private void logParameterOptionSubscriptionPrice(DataService ds,
            PricedOption pricedOption) {

        BESAuditLogEntry logEntry = createLogEntry(ds,
                Operation.EDIT_PARAMETER_SUBSCRIPTION_PRICE, pricedOption
                        .getPricedParameter().getPriceModel());
        logEntry.addParameterOptionSubscriptionPrice(pricedOption,
                getOptionName(pricedOption));
        logEntry.addAction(AuditLogAction.NONE);
        AuditLogData.add(logEntry);
    }

    public void editParameterUserPrice(DataService ds,
            PricedParameter pricedParameter, BigDecimal oldPrice) {

        boolean priceChanged = oldPrice != null
                && pricedParameter.getPricePerUser().compareTo(oldPrice) != 0;
        if (priceChanged) {
            logEditParameterUserPrice(ds, pricedParameter);
        }
    }

    private void logEditParameterUserPrice(DataService ds,
            PricedParameter pricedParameter) {
        PriceModel priceModel = pricedParameter.getPriceModel();

        BESAuditLogEntry logEntry = createLogEntry(ds,
                Operation.EDIT_PARAMETER_USER_PRICE, priceModel);

        logEntry.addParameterUserPrice(pricedParameter);
        AuditLogData.add(logEntry);
    }

    public void editParameterOptionUserPrice(DataService ds,
            PricedOption pricedOption, BigDecimal oldPrice) {

        boolean priceChanged = oldPrice != null
                && pricedOption.getPricePerUser().compareTo(oldPrice) != 0;
        if (priceChanged) {
            logParameterOptionUserPrice(ds, pricedOption);
        }
    }

    private void logParameterOptionUserPrice(DataService ds,
            PricedOption pricedOption) {

        BESAuditLogEntry logEntry = createLogEntry(ds,
                Operation.EDIT_PARAMETER_USER_PRICE, pricedOption
                        .getPricedParameter().getPriceModel());
        logEntry.addParameterOptionUserPrice(pricedOption,
                getOptionName(pricedOption));
        AuditLogData.add(logEntry);
    }

    public void editParameterUserRolePrice(DataService ds,
            long voPriceModelKey, PricedProductRole pricedRole,
            BigDecimal oldPrice) {

        // Pricemodel for customer is a copy for pricemodel for service. For
        // initial creation, values greater than zero must be logged. For
        // update, only changed values are logged
        if (voPriceModelKey == 0) {
            oldPrice = BigDecimal.ZERO;
        }
        boolean pricePerUserChanged = oldPrice != null
                && pricedRole.getPricePerUser().compareTo(oldPrice) != 0;

        if (pricePerUserChanged) {
            logParameterUserRolePrice(ds, pricedRole);
        }
    }

    private void logParameterUserRolePrice(DataService ds,
            PricedProductRole pricedRole) {

        BESAuditLogEntry logEntry = createLogEntry(ds,
                Operation.EDIT_PARAMETER_USER_ROLE_PRICE, pricedRole
                        .getPricedParameter().getPriceModel());
        logEntry.addParameterUserRolePrice(pricedRole);
        AuditLogData.add(logEntry);
    }

    public void editParameterOptionUserRolePrice(DataService ds,
            PricedProductRole pricedRole, BigDecimal oldPrice) {

        // ignore zero and same values
        boolean pricePerUserChanged = oldPrice != null
                && pricedRole.getPricePerUser().compareTo(oldPrice) != 0;

        if (pricePerUserChanged) {
            logParameterOptionUserRolePrice(ds, pricedRole);
        }
    }

    private void logParameterOptionUserRolePrice(DataService ds,
            PricedProductRole pricedRole) {

        BESAuditLogEntry logEntry = createLogEntry(ds,
                Operation.EDIT_PARAMETER_USER_ROLE_PRICE, pricedRole
                        .getPricedOption().getPricedParameter().getPriceModel());
        logEntry.addParameterOptionUserRolePrice(pricedRole,
                getOptionName(pricedRole.getPricedOption()));
        AuditLogData.add(logEntry);
    }

    private String getOptionName(PricedOption pricedOption) {
        return localizer.getLocalizedTextFromDatabase("en",
                pricedOption.getParameterOptionKey(),
                LocalizedObjectTypes.OPTION_PARAMETER_DEF_DESC);
    }

    public void editParameterSteppedPrice(DataService ds,
            SteppedPrice steppedPrice, BigDecimal oldPrice, Long oldLimit) {

        if (steppedPriceDataChanged(steppedPrice, oldPrice, oldLimit)) {
            logParameterSteppedPrice(ds, steppedPrice, AuditLogAction.UPDATE);
        }
    }

    private void logParameterSteppedPrice(DataService ds,
            SteppedPrice steppedPrice, AuditLogAction action) {
        PriceModel priceModel = steppedPrice.getPricedParameter()
                .getPriceModel();

        BESAuditLogEntry logEntry = createLogEntry(ds,
                Operation.EDIT_PARAMETER_SUBSCRIPTION_PRICE, priceModel);

        logEntry.addParameterSteppedPrice(steppedPrice);
        logEntry.addAction(action);
        AuditLogData.add(logEntry);
    }

    public void removeParameterSteppedPrice(DataService ds,
            SteppedPrice steppedPrice) {

        logParameterSteppedPrice(ds, steppedPrice, AuditLogAction.DELETE);
    }

    public void insertParameterSteppedPrice(DataService ds,
            SteppedPrice steppedPrice) {

        logParameterSteppedPrice(ds, steppedPrice, AuditLogAction.INSERT);
    }

    public void editPriceModelTypeToChargeable(
            DataService ds,
            PriceModel priceModel,
            long voPriceModelKey,
            SupportedCurrency oldCurrencyCode,
            org.oscm.internal.types.enumtypes.PriceModelType oldPriceModelType,
            int oldFreePeriod, PricingPeriod oldPricingPeriod) {

        boolean priceModelTypeChanged = !priceModel.getType().equals(
                oldPriceModelType);
        boolean currencyChanged = false;
        if (changedFromFreeToChargeablePriceModel(priceModel, oldCurrencyCode)) {
            currencyChanged = true;
        }
        if (oldCurrencyCode != null
                && !priceModel.getCurrency().getCurrencyISOCode()
                        .equals(oldCurrencyCode.getCurrencyISOCode())) {
            currencyChanged = true;
        }

        boolean freePeriodChanged = !(priceModel.getFreePeriod() == oldFreePeriod);
        boolean pricingPeriodChanged = !priceModel.getPeriod().equals(
                oldPricingPeriod);

        if (voPriceModelKey == 0 || priceModelTypeChanged || currencyChanged
                || freePeriodChanged || pricingPeriodChanged) {
            BESAuditLogEntry logEntry = createLogEntry(ds,
                    Operation.EDIT_CHARGEABLE_PRICE_MODEL, priceModel);
            logEntry.addPriceModel(priceModel);
            AuditLogData.add(logEntry);
        }
    }

    /**
     * oldCurrencyCode is NULL if the pricemodel was free of charge
     */
    private boolean changedFromFreeToChargeablePriceModel(
            PriceModel priceModel, SupportedCurrency oldCurrencyCode) {
        return oldCurrencyCode == null && priceModel.getCurrency() != null;
    }

    public void editPriceModelTypeToFree(
            DataService ds,
            PriceModel priceModel,
            long voPriceModelKey,
            org.oscm.internal.types.enumtypes.PriceModelType oldPriceModelType) {

        boolean priceModelTypeChanged = !priceModel.getType().equals(
                oldPriceModelType);
        if (voPriceModelKey == 0 || priceModelTypeChanged) {
            BESAuditLogEntry logEntry = createLogEntry(ds,
                    Operation.EDIT_FREE_PRICE_MODEL, priceModel);
            AuditLogData.add(logEntry);
        }
    }

    public void editSubscriptionPrice(DataService ds, PriceModel priceModel,
            BigDecimal oldSubscriptionPrice, boolean isCreatePriceModel) {

        if (isCreatePriceModel) {
            oldSubscriptionPrice = BigDecimal.ZERO;
        }
        boolean subscriptionPriceChanged = oldSubscriptionPrice != null
                && priceModel.getPricePerPeriod().compareTo(
                        oldSubscriptionPrice) != 0;

        if (subscriptionPriceChanged) {
            BESAuditLogEntry logEntry = createLogEntry(ds,
                    Operation.EDIT_SUBSCRIPTION_PRICE, priceModel);
            logEntry.addSubscriptionPrice(priceModel);
            AuditLogData.add(logEntry);
        }
    }

    public void editOneTimeFee(DataService ds, PriceModel priceModel,
            BigDecimal oldOneTimeFee, boolean isCreatePriceModel) {
        if (isCreatePriceModel) {
            oldOneTimeFee = BigDecimal.ZERO;
        }
        boolean oneTimeFeeChanged = oldOneTimeFee != null
                && priceModel.getOneTimeFee().compareTo(oldOneTimeFee) != 0;

        if (oneTimeFeeChanged) {
            BESAuditLogEntry logEntry = createLogEntry(ds,
                    Operation.EDIT_ONETIME_FEE, priceModel);
            logEntry.addOneTimeFee(priceModel);
            AuditLogData.add(logEntry);
        }
    }

    public void editUserPrice(DataService ds, PriceModel priceModel,
            BigDecimal oldUserPrice, boolean isCreatePriceModel) {

        // Pricemodel for customer is a copy for pricemodel for service. For
        // initial creation, values greater than zero must be logged. For
        // update, only changed values are logged
        if (isCreatePriceModel) {
            oldUserPrice = BigDecimal.ZERO;
        }
        boolean userPriceChanged = oldUserPrice != null
                && priceModel.getPricePerUserAssignment().compareTo(
                        oldUserPrice) != 0;

        if (userPriceChanged) {
            BESAuditLogEntry logEntry = createLogEntry(ds,
                    Operation.EDIT_USER_PRICE, priceModel);
            logEntry.addUserPrice(priceModel);
            logEntry.addAction(AuditLogAction.NONE);
            AuditLogData.add(logEntry);
        }
    }

    public void editUserSteppedPrice(DataService ds, SteppedPrice steppedPrice,
            BigDecimal oldPrice, Long oldLimit) {

        if (steppedPriceDataChanged(steppedPrice, oldPrice, oldLimit)) {
            logEditUserSteppedPrice(ds, steppedPrice, AuditLogAction.UPDATE);
        }
    }

    private void logEditUserSteppedPrice(DataService ds,
            SteppedPrice steppedPrice, AuditLogAction action) {
        BESAuditLogEntry logEntry = createLogEntry(ds,
                Operation.EDIT_USER_PRICE, steppedPrice.getPriceModel());
        logEntry.addSteppedUserPrice(steppedPrice);
        logEntry.addAction(action);
        AuditLogData.add(logEntry);
    }

    public void removeUserSteppedPrice(DataService ds, SteppedPrice steppedPrice) {
        logEditUserSteppedPrice(ds, steppedPrice, AuditLogAction.DELETE);
    }

    public void insertUserSteppedPrice(DataService ds, SteppedPrice steppedPrice) {
        logEditUserSteppedPrice(ds, steppedPrice, AuditLogAction.INSERT);
    }

    public void deletePriceModel(DataService ds, PriceModel priceModel) {
        BESAuditLogEntry logEntry = createLogEntry(ds,
                Operation.DELETE_PRICE_MODEL, priceModel);
        AuditLogData.add(logEntry);
    }

    public void editServiceRolePrice(DataService ds, long voPriceModelKey,
            PriceModel priceModel, PricedProductRole updatedPricedProductRole,
            BigDecimal oldPricePerUser, Organization targetCustomer,
            Subscription subscription) {

        // Pricemodel for customer is a copy for pricemodel for service. For
        // initial creation, values greater than zero must be logged. For
        // update, only changed values are logged
        if (voPriceModelKey == 0) {
            oldPricePerUser = BigDecimal.ZERO;
        }
        boolean pricePerUserChanged = oldPricePerUser != null
                && updatedPricedProductRole.getPricePerUser().compareTo(
                        oldPricePerUser) != 0;

        if (pricePerUserChanged) {
            logEditServiceRolePrice(ds, priceModel, updatedPricedProductRole,
                    targetCustomer, subscription);
        }
    }

    private void logEditServiceRolePrice(DataService ds, PriceModel priceModel,
            PricedProductRole updatedPricedProductRole,
            Organization targetCustomer, Subscription subscription) {
        PriceModelAuditLogOperation operation = determineOperation(priceModel,
                Operation.EDIT_SERVICE_ROLE_PRICE);
        BESAuditLogEntry logEntry = createAuditLogEntry(ds, operation,
                priceModel.getProduct(), subscription);

        if (targetCustomer != null) {
            logEntry.addParameter(CUSTOMER_ID,
                    targetCustomer.getOrganizationId());
            logEntry.addParameter(CUSTOMER_NAME, targetCustomer.getName());
        }
        logEntry.addParameter(CURRENCY_CODE, priceModel.getCurrency()
                .getCurrencyISOCode());
        logEntry.addParameter(TIMEUNIT, priceModel.getPeriod().name());
        logEntry.addParameter(USER_ROLE, updatedPricedProductRole
                .getRoleDefinition().getRoleId());
        logEntry.addParameter(PRICE, updatedPricedProductRole.getPricePerUser()
                .toString());

        AuditLogData.add(logEntry);
    }

    public void localizePriceModel(DataService ds, Product product,
            Organization customer,
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

            addLogEntryForAuditLogData(ds, product, customer, locale,
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
            localizedValue.setShortDescription(localizedValue
                    .getShortDescription() == null ? "" : localizedValue
                    .getShortDescription());
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
     * 
     * @param ds
     *            the data service
     * @param product
     *            the product
     * @param customer
     *            the customer organization
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
    public void addLogEntryForAuditLogData(DataService ds, Product product,
            Organization customer, String locale,
            boolean isDescriptionLocalized, boolean isLicenseLocalized) {
        BESAuditLogEntry logEntry = createAuditLogEntry(
                ds,
                customer == null ? PriceModelAuditLogOperation.LOCALIZE_PRICE_MODEL_FOR_SERVICE
                        : PriceModelAuditLogOperation.LOCALIZE_PRICE_MODEL_FOR_CUSTOMER_SERVICE,
                product, null);
        if (customer != null) {
            logEntry.addParameter(CUSTOMER_ID, customer.getOrganizationId());
            logEntry.addParameter(CUSTOMER_NAME, customer.getName());
        }
        logEntry.addParameter(LOCALE, locale);
        logEntry.addParameter(DESCRIPTION, toYesOrNo(isDescriptionLocalized));
        logEntry.addParameter(LICENSE, toYesOrNo(isLicenseLocalized));
        AuditLogData.add(logEntry);
    }

    private String toYesOrNo(boolean parameter) {
        return parameter ? "YES" : "NO";
    }

    private BESAuditLogEntry createAuditLogEntry(DataService ds,
            PriceModelAuditLogOperation operation, Product product,
            Subscription subscription) {

        List<AuditLogParameter> parameters = operation.getParameters();

        BESAuditLogEntry logEntry = new BESAuditLogEntry(ds,
                operation.getOperationId(), operation.toString(),
                parameters.toArray(new AuditLogParameter[parameters.size()]));
        if (product != null) {
            logEntry.addProduct(product, localizer);
        }
        if (subscription != null) {
            logEntry.addSubscription(subscription);
        }
        return logEntry;
    }
}
