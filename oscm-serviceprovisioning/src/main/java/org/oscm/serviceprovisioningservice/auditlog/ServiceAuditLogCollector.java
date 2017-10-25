/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2013-5-10                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.auditlog;

import java.util.BitSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import org.oscm.auditlog.AuditLogData;
import org.oscm.auditlog.AuditLogParameter;
import org.oscm.auditlog.BESAuditLogEntry;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.interceptor.AuditLoggingEnabled;
import org.oscm.validation.ArgumentValidator;
import org.oscm.internal.types.enumtypes.OfferingType;
import org.oscm.internal.types.exception.IllegalArgumentException;
import org.oscm.internal.vo.VOLocalizedText;
import org.oscm.internal.vo.VOServiceLocalization;

/**
 * @author Mao
 * 
 */
@Stateless
@Interceptors(AuditLoggingEnabled.class)
public class ServiceAuditLogCollector {

    @EJB(beanInterface = LocalizerServiceLocal.class)
    LocalizerServiceLocal localizer;

    private static int DESCRIPTION_LOCALIZED = 1;
    private static int SHORTDESCRIPTION_LOCALIZED = 0;

    public void activeOrDeactiveService(DataService ds, Product product,
            String marketPlaceId, String marketPlaceName, boolean isActive,
            boolean inCatalog) {
        BESAuditLogEntry logEntry = createAuditLogEntry(ds,
                ServiceAuditLogOperation.ACTIVATE_DEACTIVATE_SERVICE, product);
        logEntry.addParameter(AuditLogParameter.MARKETPLACE_ID, marketPlaceId);
        logEntry.addParameter(AuditLogParameter.MARKETPLACE_NAME,
                marketPlaceName);
        logEntry.addParameter(AuditLogParameter.ACTIVATION, toOnOrOff(isActive));
        logEntry.addParameter(AuditLogParameter.INCATALOG, toOnOrOff(inCatalog));
        AuditLogData.add(logEntry);
    }

    public void updateServiceParameters(DataService ds, Product product,
            String parameterName, String parameterValue, boolean isConfigurable) {
        if (product.getKey() == 0) {
            String msg = String.format("Parameter %s must not be zero.",
                    "Servicekey");
            throw new IllegalArgumentException(msg);
        }
        BESAuditLogEntry logEntry = createAuditLogEntry(ds,
                ServiceAuditLogOperation.UPDATE_SERVICE_PARAMETERS, product);
        logEntry.addParameter(AuditLogParameter.PARAMETER_NAME, parameterName);
        logEntry.addParameter(AuditLogParameter.PARAMETER_VALUE, parameterValue);
        logEntry.addParameter(AuditLogParameter.USEROPTION,
                toOnOrOff(isConfigurable));
        AuditLogData.add(logEntry);
    }

    public void defineUpDownGradeOptions(DataService ds, Product product,
            Product targetProduct, String upDownGrade) {
        BESAuditLogEntry logEntry = createAuditLogEntry(ds,
                ServiceAuditLogOperation.DEFINE_UPGRADE_DOWNGRADE_SERVICE,
                product);
        logEntry.addParameter(AuditLogParameter.TARGET_SERVICE_ID,
                targetProduct.getTemplateOrSelf().getProductId());

        LocalizerFacade facade = getLocalizerFacade(localizer, ds
                .getCurrentUser().getLocale());
        String nameForCustomer = facade.getText(product.getTemplateOrSelf()
                .getKey(), LocalizedObjectTypes.PRODUCT_MARKETING_NAME);

        logEntry.addParameter(AuditLogParameter.TARGET_SERVICE_NAME,
                nameForCustomer);
        logEntry.addParameter(AuditLogParameter.UPDOWNGRADE, upDownGrade);
        AuditLogData.add(logEntry);
    }

    public void localizeService(DataService ds, Product product,
            VOServiceLocalization localizationOriginal,
            VOServiceLocalization localizationToBeUpdate) {
        ArgumentValidator.notNull("localizationToBeUpdate",
                localizationToBeUpdate);
        ArgumentValidator.notNull("localizationOriginal", localizationOriginal);
        Map<String, LocalizedAuditLogEntryParameters> mapToBeUpdate = prepareLocalizationMap(localizationToBeUpdate);
        Map<String, LocalizedAuditLogEntryParameters> mapOriginal = prepareLocalizationMap(localizationOriginal);
        Map<String, BitSet> localizationMap = collectLocalizationStates(
                mapToBeUpdate, mapOriginal);
        for (String locale : localizationMap.keySet()) {
            boolean shortDescriptionLocalized = isShortDescriptionLocalized(
                    localizationMap, locale);
            boolean descriptionLocalized = isDescriptionLocalized(
                    localizationMap, locale);

            addLogEntryForAuditLogData(ds, product, locale,
                    descriptionLocalized, shortDescriptionLocalized);
        }
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
            newLocalized.set(
                    SHORTDESCRIPTION_LOCALIZED,
                    !newLocalizedValue.getShortDescription().equals(
                            storedLocalizedValue.getShortDescription()));
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
            localizedValue.setShortDescription(localizedValue
                    .getShortDescription() == null ? "" : localizedValue
                    .getShortDescription());
        } else {
            localizedValue = new LocalizedAuditLogEntryParameters();
        }
        return localizedValue;
    }

    Map<String, LocalizedAuditLogEntryParameters> prepareLocalizationMap(
            VOServiceLocalization localization) {
        Map<String, LocalizedAuditLogEntryParameters> map = new LinkedHashMap<String, LocalizedAuditLogEntryParameters>();

        // collect all locale, description and short description, and add them
        // to hashMap(locale,description short description)
        for (VOLocalizedText localizedText : localization.getDescriptions()) {
            LocalizedAuditLogEntryParameters auditLogEntryData = new LocalizedAuditLogEntryParameters();
            auditLogEntryData.setDescription(localizedText.getText());
            map.put(localizedText.getLocale(), auditLogEntryData);
        }

        for (VOLocalizedText localizedText : localization
                .getShortDescriptions()) {
            LocalizedAuditLogEntryParameters auditLogEntryData = map
                    .get(localizedText.getLocale());
            if (auditLogEntryData != null) {
                auditLogEntryData.setShortDescription(localizedText.getText());
            } else {
                LocalizedAuditLogEntryParameters newAuditLogEntryData = new LocalizedAuditLogEntryParameters();
                newAuditLogEntryData.setShortDescription(localizedText
                        .getText());
                map.put(localizedText.getLocale(), newAuditLogEntryData);
            }
        }
        return map;
    }

    private boolean isShortDescriptionLocalized(Map<String, BitSet> localeMap,
            String locale) {
        return localeMap.get(locale) != null
                && localeMap.get(locale).get(SHORTDESCRIPTION_LOCALIZED);
    }

    private boolean isDescriptionLocalized(Map<String, BitSet> localeMap,
            String locale) {
        return localeMap.get(locale) != null
                && localeMap.get(locale).get(DESCRIPTION_LOCALIZED);
    }

    public void deleteService(DataService ds, Product product) {
        BESAuditLogEntry logEntry = createAuditLogEntry(ds,
                ServiceAuditLogOperation.DELETE_SERVICE, product);
        AuditLogData.add(logEntry);
    }

    public void copyService(DataService ds, Product product, String copyId,
            String copyName) {
        BESAuditLogEntry logEntry = createAuditLogEntry(ds,
                ServiceAuditLogOperation.COPY_SERVICE, product);
        logEntry.addParameter(AuditLogParameter.COPY_SERVICE_ID, copyId);
        logEntry.addParameter(AuditLogParameter.COPY_SERVICE_NAME, copyName);
        AuditLogData.add(logEntry);
    }

    public void updateService(DataService ds, Product product,
            boolean isShortDescriptionChanged, boolean isDescriptionChanged,
            boolean isCustomTabNameChanged, String locale) {
        BESAuditLogEntry logEntry = createAuditLogEntry(ds,
                ServiceAuditLogOperation.UPDATE_SERVICE, product);
        logEntry.addParameter(AuditLogParameter.DESCRIPTION,
                toYesOrNo(isDescriptionChanged));
        logEntry.addParameter(AuditLogParameter.SHORT_DESCRIPTION,
                toYesOrNo(isShortDescriptionChanged));
        logEntry.addParameter(AuditLogParameter.LOCALE, locale);
        logEntry.addParameter(AuditLogParameter.AUTO_ASSIGN_USER,
                toYesOrNo(product.isAutoAssignUserEnabled().booleanValue()));
        logEntry.addParameter(AuditLogParameter.CUSTOM_TAB_NAME,
                toYesOrNo(isCustomTabNameChanged));
        AuditLogData.add(logEntry);
    }

    public void defineService(DataService ds, Product product,
            String technicalServiceId, String shortDescription,
            String description, String locale) {
        BESAuditLogEntry logEntry = createAuditLogEntry(ds,
                ServiceAuditLogOperation.DEFINE_SERVICE, product);
        logEntry.addParameter(AuditLogParameter.TECHSERVICE_NAME,
                technicalServiceId);
        logEntry.addParameter(AuditLogParameter.DESCRIPTION,
                toYesOrNo(description != null && !description.isEmpty()));
        logEntry.addParameter(
                AuditLogParameter.SHORT_DESCRIPTION,
                toYesOrNo(shortDescription != null
                        && !shortDescription.isEmpty()));
        logEntry.addParameter(AuditLogParameter.LOCALE, locale);
        logEntry.addParameter(AuditLogParameter.AUTO_ASSIGN_USER,
                toYesOrNo(product.isAutoAssignUserEnabled().booleanValue()));
        AuditLogData.add(logEntry);
    }

    public void assignResellerBroker(DataService ds, Product product,
            String orginizationId, OfferingType offeringType, boolean isAssign) {

        ServiceAuditLogOperation operator = null;
        AuditLogParameter auditlogParameter = null;
        if (OfferingType.BROKER.equals(offeringType)) {
            operator = isAssign ? ServiceAuditLogOperation.ASSIGN_SERVICE_BROKERS
                    : ServiceAuditLogOperation.DEASSIGN_SERVICE_BROKER;
            auditlogParameter = AuditLogParameter.BROKER_ID;
        } else if (OfferingType.RESELLER.equals(offeringType)) {
            operator = isAssign ? ServiceAuditLogOperation.ASSIGN_SERVICE_RESELLERS
                    : ServiceAuditLogOperation.DEASSIGN_SERVICE_RESELLER;
            auditlogParameter = AuditLogParameter.RESELLER_ID;
        }

        BESAuditLogEntry logEntryBroker = createAuditLogEntry(ds, operator,
                product);

        logEntryBroker.addParameter(auditlogParameter, orginizationId);

        AuditLogData.add(logEntryBroker);
    }

    /**
     * Construct #ServiceAuditLogOperation.LOCALIZE_SERVICE audit log entry
     * 
     * @param ds
     *            the data service
     * @param product
     *            the product which has been modified
     * @param locale
     *            the locale
     * @param isDescriptionLocalized
     *            "YES" indicates description localized for <code>locale</code>
     *            "NO" indicates description not localized for
     *            <code>locale</code>
     * @param isShortDescriptionLocalized
     *            <code>true</code> indicates short description localized for
     *            <code>locale</code>, <code>false</code> indicates short
     *            description not localized for <code>locale</code>
     */
    private void addLogEntryForAuditLogData(DataService ds, Product product,
            String locale, boolean isDescriptionLocalized,
            boolean isShortDescriptionLocalized) {
        BESAuditLogEntry logEntry = createAuditLogEntry(ds,
                ServiceAuditLogOperation.LOCALIZE_SERVICE, product);
        logEntry.addParameter(AuditLogParameter.LOCALE, locale);
        logEntry.addParameter(AuditLogParameter.DESCRIPTION,
                toYesOrNo(isDescriptionLocalized));
        logEntry.addParameter(AuditLogParameter.SHORT_DESCRIPTION,
                toYesOrNo(isShortDescriptionLocalized));
        AuditLogData.add(logEntry);
    }

    private String toOnOrOff(boolean parameter) {
        return parameter ? "ON" : "OFF";
    }

    private String toYesOrNo(boolean parameter) {
        return parameter ? "YES" : "NO";
    }

    private BESAuditLogEntry createAuditLogEntry(DataService ds,
            ServiceAuditLogOperation operation, Product product) {
        BESAuditLogEntry logEntry = new BESAuditLogEntry(ds,
                operation.getOperationId(), operation.toString(),
                operation.getParameters());

        logEntry.addProduct(product, localizer);
        return logEntry;
    }

    LocalizerFacade getLocalizerFacade(LocalizerServiceLocal localizer,
            String userLocale) {
        return new LocalizerFacade(localizer, userLocale);
    }
}
