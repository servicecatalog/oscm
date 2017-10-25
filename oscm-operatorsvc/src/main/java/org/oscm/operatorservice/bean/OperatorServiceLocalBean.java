/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 11.11.2013      
 *  
 *  author cmin
 *                                                                              
 *******************************************************************************/

package org.oscm.operatorservice.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.SessionContext;
import javax.interceptor.Interceptors;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.converter.ParameterizedTypes;
import org.oscm.converter.PropertiesConverter;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.DomainObject;
import org.oscm.domobjects.Event;
import org.oscm.domobjects.ParameterDefinition;
import org.oscm.domobjects.PaymentType;
import org.oscm.domobjects.Report;
import org.oscm.domobjects.SupportedLanguage;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.interceptor.ExceptionMapper;
import org.oscm.interceptor.InvocationDateContainer;
import org.oscm.operatorservice.validator.PropertiesImportValidator;
import org.oscm.types.enumtypes.LocalizedDataType;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.types.enumtypes.StandardLanguage;
import org.oscm.validation.ArgumentValidator;
import org.oscm.internal.types.enumtypes.EventType;
import org.oscm.internal.types.enumtypes.ParameterType;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.PropertiesImportException;
import org.oscm.internal.types.exception.SaaSSystemException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.types.exception.ValidationException.ReasonEnum;

/**
 * No interface view bean implementation of the operator related BSS internal
 * functionality.
 * 
 * @author cmin
 */
@RolesAllowed("PLATFORM_OPERATOR")
@Interceptors({ InvocationDateContainer.class, ExceptionMapper.class })
@LocalBean
public class OperatorServiceLocalBean {

    private static Log4jLogger logger = LoggerFactory
            .getLogger(OperatorServiceLocalBean.class);

    @EJB(beanInterface = DataService.class)
    DataService dm;

    @EJB(beanInterface = LocalizerServiceLocal.class)
    LocalizerServiceLocal localizer;

    @Resource
    protected SessionContext sessionCtx;

    private static final String PROPERTY_TYPE_MAIL = "Mail";
    private static final String PROPERTY_TYPE_MESSAGE = "Message";
    private static final String PROPERTY_TYPE_PLATFORMOBJECT = "Platform objects";

    /**
     * Save message properties into DB
     * 
     * @param messagePropertiesMap
     *            - map of properties contains default language properties and
     *            imported language properties
     * @param languageCode
     *            - language code get from excel file
     * @param propertiesType
     *            - type of current properties
     * @throws OperationNotPermittedException
     *             - there is no default language code
     * @throws PropertiesImportException
     *             - exception while import properties
     * @throws ObjectNotFoundException
     *             - if there is no default language in system
     * 
     */
    @RolesAllowed({ "PLATFORM_OPERATOR" })
    public void saveProperties(Map<String, Properties> messagePropertiesMap,
            String languageCode, LocalizedDataType dataType)
            throws OperationNotPermittedException, PropertiesImportException,
            ObjectNotFoundException {
        PropertiesImportValidator validator = new PropertiesImportValidator(dm);
        validator.checkLanguageCodeNotNull(languageCode);
        validator.checkLanguageSupported(languageCode);
        if (dataType.equals(LocalizedDataType.MessageProperties)) {
            storePropertiesToDB(messagePropertiesMap,
                    LocalizedObjectTypes.MESSAGE_PROPERTIES);
        } else if (dataType.equals(LocalizedDataType.MailProperties)) {
            storePropertiesToDB(messagePropertiesMap,
                    LocalizedObjectTypes.MAIL_PROPERTIES);
        } else if (dataType.equals(LocalizedDataType.PlatformObjects)) {
            storeLocalizedNamesToDB(messagePropertiesMap);
        }
    }

    /**
     * Load properties for user interface labels:marketplace user
     * interface,("blue") administration portal and including messages
     * 
     * @param languageCode
     *            - which language's properties need to be loaded
     * @return - properties map: key is the language code, value is properties
     *         object
     * @throws ObjectNotFoundException
     */
    @RolesAllowed({ "PLATFORM_OPERATOR" })
    public Map<String, Properties> loadMessageProperties(String languageCode)
            throws ObjectNotFoundException {
        Map<String, Properties> propertiesMap = loadStandardLanguageProperties(PROPERTY_TYPE_MESSAGE);
        if (languageCode != null && !propertiesMap.containsKey(languageCode)) {
            Properties props = loadMessagePropertiesFromDB(languageCode);
            propertiesMap.put(languageCode, props);
        }
        return propertiesMap;
    }

    /**
     * Load properties and mail properties
     * 
     * @param languageCode
     *            - which language's properties need to be loaded
     * @return - properties map: key is the language code, value is properties
     *         object
     * @throws ObjectNotFoundException
     */
    @RolesAllowed({ "PLATFORM_OPERATOR" })
    public Properties loadPropertiesFromDB(String languageCode) {
        ArgumentValidator.notNull("Language code", languageCode);
        Properties props = loadMessagePropertiesFromDB(languageCode);
        Properties mailProps = loadMailPropertiesFromFileAndDB(languageCode);
        props.putAll(mailProps);
        return props;
    }

    /**
     * Load properties for mail texts
     * 
     * @param languageCode
     *            - which language's mail texts need to be loaded
     * @return - properties map: key is the language code, value is properties
     *         object
     * @throws ObjectNotFoundException
     */
    @RolesAllowed({ "PLATFORM_OPERATOR" })
    public Map<String, Properties> loadMailProperties(String languageCode)
            throws ObjectNotFoundException {
        Map<String, Properties> propertiesMap = loadStandardLanguageProperties(PROPERTY_TYPE_MAIL);
        if (languageCode != null && !propertiesMap.containsKey(languageCode)) {
            Properties mailProps = loadMailPropertiesFromDB(languageCode);
            propertiesMap.put(languageCode, mailProps);
        }
        return propertiesMap;
    }

    private Map<String, Properties> loadStandardLanguageProperties(
            String propType) {
        Map<String, Properties> propertiesMap = new HashMap<String, Properties>();
        Properties standardLanguageProps = new Properties();
        for (StandardLanguage standardLanguage : StandardLanguage.values()) {

            if (PROPERTY_TYPE_MESSAGE.equals(propType)) {
                standardLanguageProps = loadMessagePropertiesFromDB(standardLanguage
                        .toString());
                propertiesMap.put(standardLanguage.toString(),
                        standardLanguageProps);
            } else if (PROPERTY_TYPE_MAIL.equals(propType)) {
                propertiesMap
                        .putAll(loadMailPropertiesMapFromFileAndDB(standardLanguage
                                .toString()));
            } else if (PROPERTY_TYPE_PLATFORMOBJECT.equals(propType)) {
                propertiesMap
                        .putAll(loadPlatformObjectsMapFromFileAndDB(standardLanguage
                                .toString()));
            }

        }
        return propertiesMap;
    }

    /**
     * Load properties for platform objects names
     * 
     * @param languageCode
     *            - which language's platform objects names need to be loaded
     * @return - properties map: key is the language code, value is properties
     *         object
     * @throws ObjectNotFoundException
     */
    @RolesAllowed({ "PLATFORM_OPERATOR" })
    public Map<String, Properties> loadPlatformObjects(String languageCode)
            throws ObjectNotFoundException {
        Map<String, Properties> propertiesMap = loadStandardLanguageProperties(PROPERTY_TYPE_PLATFORMOBJECT);
        if (languageCode != null && !propertiesMap.containsKey(languageCode)) {
            Properties props = loadPlatformObjectsFromDB(languageCode);
            propertiesMap.put(languageCode, props);
        }
        return propertiesMap;
    }

    /**
     * check the imported properties are all translated
     * 
     * @param propertiesMap
     *            map contains default language properties and imported language
     *            properties
     * @param defaultLanguageCode
     *            language code of default language
     * @param importLanguageCode
     *            language code of imported language
     * @return
     * @throws PropertiesImportException
     *             - exception while import properties
     * @throws ObjectNotFoundException
     *             - exception if there is no default language in system
     */
    @RolesAllowed({ "PLATFORM_OPERATOR" })
    public String checkAreAllItemsTranslated(
            List<Map<String, Properties>> propertiesMaps,
            String importLanguageCode) throws PropertiesImportException,
            ObjectNotFoundException {
        String defaultLanguageCode = getDefaultLanguage()
                + StandardLanguage.COLUMN_HEADING_SUFFIX;
        for (Map<String, Properties> propertiesMap : propertiesMaps) {
            Properties defaultLanguageProperties = propertiesMap
                    .get(defaultLanguageCode);
            Properties importLanguageProperties = propertiesMap
                    .get(importLanguageCode);
            Set<Object> keys = defaultLanguageProperties.keySet();
            for (Object key : keys) {
                String defaultPropertyValue = defaultLanguageProperties
                        .get(key.toString()).toString().trim();
                if (defaultPropertyValue.length() == 0) {
                    continue;
                }
                if (importLanguageProperties.containsKey(key)) {
                    Object importPropertyValue = importLanguageProperties
                            .get(key.toString());
                    if (importPropertyValue != null
                            && importPropertyValue.toString().length() > 0) {
                        continue;
                    }
                }
                PropertiesImportException propertiesImportException = new PropertiesImportException(
                        PropertiesImportException.Reason.TRANSLATIONS_MISSING);
                logger.logWarn(
                        Log4jLogger.SYSTEM_LOG,
                        propertiesImportException,
                        LogMessageIdentifier.WARN_TRANSLATIONS_MISSING_FOR_IMPORT_PROPERTIES);
                return propertiesImportException.getMessageKey();
            }
        }

        return null;
    }

    /**
     * get active language list/all supported language list
     * 
     * @param isOnlyActive
     *            if true, only get all active supported languages, if false,
     *            get all supported languages
     * @return
     */
    public List<SupportedLanguage> getLanguages(boolean isOnlyActive) {
        Query q = null;
        if (isOnlyActive) {
            q = dm.createNamedQuery("SupportedLanguage.findAllActive");
        } else {
            q = dm.createNamedQuery("SupportedLanguage.findAll");
        }
        return ParameterizedTypes.list(q.getResultList(),
                SupportedLanguage.class);
    }

    public String getDefaultLanguage() throws ObjectNotFoundException {
        Query q = dm.createNamedQuery("SupportedLanguage.findDefault");
        List<String> list = ParameterizedTypes.list(q.getResultList(),
                String.class);
        if (list == null || list.size() < 1) {
            ObjectNotFoundException e = new ObjectNotFoundException(
                    "Default supported language cannot be found.");
            logger.logError(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.ERROR_DEFAULT_LANGUAGE_NOT_FOUND,
                    new String[] {});
            throw e;
        } else if (list.size() > 1) {
            ObjectNotFoundException e = new ObjectNotFoundException(
                    "Two or more default supported languages have been found.");
            logger.logError(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.ERROR_DEFAULT_LANGUAGE_MORE_THAN_ONE,
                    new String[] {});
            throw e;
        }
        return list.get(0);
    }

    @RolesAllowed({ "PLATFORM_OPERATOR" })
    public void saveLanguages(List<SupportedLanguage> languages)
            throws IllegalArgumentException, ValidationException {
        ArgumentValidator.notNull("Language list", languages);

        for (SupportedLanguage sl : languages) {
            ArgumentValidator.notNull("Language", sl);
            ArgumentValidator.notNull("Language ISO code",
                    sl.getLanguageISOCode());
            if (!checkLocale(sl.getLanguageISOCode())) {
                ValidationException ve = new ValidationException(
                        ReasonEnum.INVALID_LANGUAGE_ISOCODE, "languageISOCode",
                        new Object[] { sl.getLanguageISOCode() });
                logger.logWarn(Log4jLogger.SYSTEM_LOG, ve,
                        LogMessageIdentifier.ERROR_LANGUAGE_ISOCODE_INVALID,
                        new String[] { sl.getLanguageISOCode() });
                throw ve;
            }
            try {
                if (sl.getKey() < 1) {
                    dm.persist(sl);
                } else {
                    SupportedLanguage storedObj = (SupportedLanguage) dm
                            .getReferenceByBusinessKey(sl);
                    doModify(storedObj, sl);
                }
            } catch (NonUniqueBusinessKeyException ex) {
                ValidationException ve = new ValidationException(
                        ReasonEnum.LANGUAGE_ISOCODE_EXISTED, "languageISOCode",
                        new Object[] { sl.getLanguageISOCode() });
                logger.logWarn(Log4jLogger.SYSTEM_LOG, ve,
                        LogMessageIdentifier.ERROR_LANGUAGE_ISOCODE_EXISTED,
                        new String[] { sl.getLanguageISOCode() });
                throw ve;
            } catch (ObjectNotFoundException e) {
                ValidationException ve = new ValidationException(
                        ReasonEnum.LANGUAGE_ISOCODE_NOT_FOUND,
                        "languageISOCode",
                        new Object[] { sl.getLanguageISOCode() });
                logger.logWarn(Log4jLogger.SYSTEM_LOG, ve,
                        LogMessageIdentifier.ERROR_LANGUAGE_ISOCODE_NOT_FOUND,
                        new String[] { sl.getLanguageISOCode() });
                throw ve;
            }
        }
        dm.flush();
    }

    boolean doModify(SupportedLanguage domObj, SupportedLanguage sl) {
        boolean isModified = false;
        if (domObj.getDefaultStatus() != sl.getDefaultStatus()) {
            domObj.setDefaultStatus(sl.getDefaultStatus());
            isModified = true;
        }
        if (domObj.getActiveStatus() != sl.getActiveStatus()) {
            domObj.setActiveStatus(sl.getActiveStatus());
            isModified = true;
        }
        return isModified;
    }

    private boolean checkLocale(String languageISOCode) {
        String[] locales = Locale.getISOLanguages();
        for (String l : locales) {
            if (l.equalsIgnoreCase(languageISOCode)) {
                return true;
            }
        }
        return false;
    }

    private void storePropertiesToDB(Map<String, Properties> propertiesMap,
            LocalizedObjectTypes type) {
        if (propertiesMap != null) {
            for (String localeString : propertiesMap.keySet()) {
                if (!StandardLanguage.isStandardLanguage(localeString,
                        StandardLanguage.COLUMN_HEADING_SUFFIX)) {
                    localizer
                            .storeLocalizedResource(
                                    localeString,
                                    0L,
                                    type,
                                    PropertiesConverter
                                            .propertiesToStringIgnoreEmptyKeys(propertiesMap
                                                    .get(localeString)));
                }
            }
        }
    }

    private void storeLocalizedNamesToDB(Map<String, Properties> namesMap) {
        if (namesMap != null) {
            for (String localeString : namesMap.keySet()) {
                if (!StandardLanguage.isStandardLanguage(localeString,
                        StandardLanguage.COLUMN_HEADING_SUFFIX)) {
                    Properties targetProperties = namesMap.get(localeString);
                    for (Object objectKey : targetProperties.keySet()) {
                        if (objectKey == null
                                || objectKey.toString().indexOf(".") == -1) {
                            continue;
                        }

                        String[] keys = ((String) objectKey).split("\\.");
                        String objectType = keys[0];
                        LocalizedObjectTypes localizedObjectType = getLocalizedObjectType(objectType);
                        String objectName = keys[1];
                        DomainObject<?> object = null;
                        if (localizedObjectType
                                .equals(LocalizedObjectTypes.PARAMETER_DEF_DESC)) {
                            object = getPlatformParamDefinitionWithId(objectName);
                        } else if (localizedObjectType
                                .equals(LocalizedObjectTypes.EVENT_DESC)) {
                            object = getPlatformEventWithId(objectName);
                        } else if (localizedObjectType
                                .equals(LocalizedObjectTypes.REPORT_DESC)) {
                            object = getReportWithId(objectName);
                        } else if (localizedObjectType
                                .equals(LocalizedObjectTypes.PAYMENT_TYPE_NAME)) {
                            object = getPaymentTypeWithId(objectName);
                        }

                        if (object == null) {
                            continue;
                        }

                        if (targetProperties.get(objectKey) != null
                                && targetProperties.get(objectKey).toString()
                                        .length() > 0) {
                            localizer.storeLocalizedResource(localeString,
                                    object.getKey(), localizedObjectType,
                                    targetProperties.get(objectKey).toString());
                        } else {
                            localizer.removeLocalizedValue(object.getKey(),
                                    localizedObjectType, localeString);
                        }
                    }
                }
            }
        }
    }

    private Event getPlatformEventWithId(String id) {
        Query query = dm.createNamedQuery("Event.getPlatformEvent");
        query.setParameter("eventType", EventType.PLATFORM_EVENT);
        query.setParameter("eventIdentifier", id);

        Event event = null;
        try {
            event = (Event) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        } catch (NonUniqueResultException e) {
            throw new SaaSSystemException("Non unique platform event '" + id
                    + "'!", e);
        }
        return event;
    }

    private ParameterDefinition getPlatformParamDefinitionWithId(String id) {
        Query query = dm
                .createNamedQuery("ParameterDefinition.getPlatformParameterDefinition");
        query.setParameter("parameterType", ParameterType.PLATFORM_PARAMETER);
        query.setParameter("parameterId", id);

        ParameterDefinition paramDef = null;
        try {
            paramDef = (ParameterDefinition) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        } catch (NonUniqueResultException e) {
            SaaSSystemException se = new SaaSSystemException(e);
            logger.logError(Log4jLogger.SYSTEM_LOG, se,
                    LogMessageIdentifier.ERROR_NOT_UNIQUE_PLATFORM_PARAMETER,
                    id);
            throw se;
        }
        return paramDef;
    }

    private Report getReportWithId(String id) {
        Query query = dm.createNamedQuery("Report.findByBusinessKey");
        query.setParameter("reportName", id);

        Report report = null;
        try {
            report = (Report) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        } catch (NonUniqueResultException e) {
            throw new SaaSSystemException("Non unique platform event '" + id
                    + "'!", e);
        }
        return report;
    }

    private PaymentType getPaymentTypeWithId(String id) {
        Query query = dm.createNamedQuery("PaymentType.findByBusinessKey");
        query.setParameter("paymentTypeId", id);

        PaymentType paymentType = null;
        try {
            paymentType = (PaymentType) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        } catch (NonUniqueResultException e) {
            throw new SaaSSystemException("Non unique platform event '" + id
                    + "'!", e);
        }
        return paymentType;
    }

    private Properties loadMessagePropertiesFromDB(String languageCode) {
        Properties props = new Properties();
        props.putAll(localizer.loadLocalizedPropertiesFromDatabase(0L,
                LocalizedObjectTypes.MESSAGE_PROPERTIES, languageCode));
        return props;
    }

    private Map<String, Properties> loadMailPropertiesMapFromFileAndDB(
            String languageCode) {
        Map<String, Properties> mailPropsMap = new HashMap<String, Properties>();
        Properties propertiesInFile = localizer
                .loadLocalizedPropertiesFromFile(
                        LocalizedObjectTypes.MAIL_CONTENT.getSourceLocation(),
                        languageCode);
        if (propertiesInFile != null) {
            mailPropsMap.put(languageCode
                    + StandardLanguage.COLUMN_HEADING_SUFFIX, propertiesInFile);
        }
        Properties propertiesInDB = localizer
                .loadLocalizedPropertiesFromDatabase(0L,
                        LocalizedObjectTypes.MAIL_PROPERTIES, languageCode);
        mailPropsMap.put(languageCode, propertiesInDB);
        return mailPropsMap;
    }

    private Map<String, Properties> loadPlatformObjectsMapFromFileAndDB(
            String languageCode) {
        Map<String, Properties> propsMap = new HashMap<String, Properties>();
        Properties propsInFile = loadPlatformObjectsFromFile(languageCode);
        propsMap.put(languageCode + StandardLanguage.COLUMN_HEADING_SUFFIX,
                propsInFile);
        Properties propsInDB = loadPlatformObjectsFromDB(languageCode);
        propsMap.put(languageCode, propsInDB);
        return propsMap;
    }

    private Properties loadPlatformObjectsFromDB(String languageCode) {
        Properties properties = new Properties();
        for (LocalizedObjectTypes objectTypes : getLocalizedObjectTypes()) {
            Query query = dm.createQuery(getQueryStringForDB(objectTypes));
            query.setParameter("locale", languageCode);
            List<Object[]> result = ParameterizedTypes.list(
                    query.getResultList(), Object[].class);
            for (Object[] entry : result) {
                String id = entry[0].toString();
                String name = entry[1].toString();
                properties.put(objectTypes.toString() + "." + id, name);
            }
        }
        return properties;
    }

    @RolesAllowed({ "PLATFORM_OPERATOR" })
    public Properties loadPlatformObjectsFromFile(String languageCode) {
        Properties properties = new Properties();
        for (LocalizedObjectTypes objectType : getLocalizedObjectTypes()) {
            Properties propertiesFromFile = localizer
                    .loadLocalizedPropertiesFromFile(
                            objectType.getSourceLocation(), languageCode);
            Query query = dm.createQuery(getQueryStringForFile(objectType));
            List<Object[]> result = ParameterizedTypes.list(
                    query.getResultList(), Object[].class);
            Map<String, String> key2IdMap = new HashMap<String, String>();
            for (Object[] entry : result) {
                String key = entry[0].toString();
                String id = entry[1].toString();
                key2IdMap.put(objectType.toString() + "." + key,
                        objectType.toString() + "." + id);
            }
            if (propertiesFromFile != null) {
                for (String key : key2IdMap.keySet()) {
                    String id = key2IdMap.get(key);
                    String value = propertiesFromFile.getProperty(key);
                    if (value != null)
                        properties.put(id, value);
                }
            }
        }
        return properties;
    }

    private Properties loadMailPropertiesFromFileAndDB(String languageCode) {
        Properties mailProps = new Properties();
        Properties propertiesInFile = localizer
                .loadLocalizedPropertiesFromFile(
                        LocalizedObjectTypes.MAIL_CONTENT.getSourceLocation(),
                        languageCode);
        if (propertiesInFile != null) {
            mailProps.putAll(propertiesInFile);
        }
        mailProps.putAll(localizer.loadLocalizedPropertiesFromDatabase(0L,
                LocalizedObjectTypes.MAIL_PROPERTIES, languageCode));
        return mailProps;
    }

    private Properties loadMailPropertiesFromDB(String languageCode) {
        Properties mailProps = new Properties();
        mailProps.putAll(localizer.loadLocalizedPropertiesFromDatabase(0L,
                LocalizedObjectTypes.MAIL_PROPERTIES, languageCode));
        return mailProps;
    }

    private LocalizedObjectTypes getLocalizedObjectType(String objectType) {
        for (LocalizedObjectTypes objectTypes : getLocalizedObjectTypes()) {
            if (objectTypes.toString().equals(objectType)) {
                return objectTypes;
            }
        }
        return null;
    }

    String getQueryStringForFile(LocalizedObjectTypes objectType) {
        if (LocalizedObjectTypes.EVENT_DESC.equals(objectType)) {
            return "select e.key, e.dataContainer.eventIdentifier from Event e where e.dataContainer.eventType = org.oscm.internal.types.enumtypes.EventType.PLATFORM_EVENT order by e.key";
        } else if (LocalizedObjectTypes.PARAMETER_DEF_DESC.equals(objectType)) {
            return "select pd.key, pd.dataContainer.parameterId  from ParameterDefinition pd where pd.dataContainer.parameterType = org.oscm.internal.types.enumtypes.ParameterType.PLATFORM_PARAMETER";
        } else if (LocalizedObjectTypes.PAYMENT_TYPE_NAME.equals(objectType)) {
            return "select pt.key, pt.dataContainer.paymentTypeId from PaymentType pt where pt.key<4";
        } else if (LocalizedObjectTypes.REPORT_DESC.equals(objectType)) {
            return "select r.key, r.dataContainer.reportName from Report r";
        }
        return null;
    }

    String getQueryStringForDB(LocalizedObjectTypes objectType) {
        if (LocalizedObjectTypes.EVENT_DESC.equals(objectType)) {
            return "select e.dataContainer.eventIdentifier,lr.value from Event e,LocalizedResource lr where lr.objectKey=e.key and e.dataContainer.eventType = org.oscm.internal.types.enumtypes.EventType.PLATFORM_EVENT and lr.objectType=org.oscm.domobjects.enums.LocalizedObjectTypes.EVENT_DESC and lr.locale = :locale";
        } else if (LocalizedObjectTypes.PARAMETER_DEF_DESC.equals(objectType)) {
            return "select pd.dataContainer.parameterId ,lr.value from ParameterDefinition pd,LocalizedResource lr where lr.objectKey=pd.key and pd.dataContainer.parameterType = org.oscm.internal.types.enumtypes.ParameterType.PLATFORM_PARAMETER and lr.objectType=org.oscm.domobjects.enums.LocalizedObjectTypes.PARAMETER_DEF_DESC and lr.locale = :locale";
        } else if (LocalizedObjectTypes.PAYMENT_TYPE_NAME.equals(objectType)) {
            return "select pt.dataContainer.paymentTypeId ,lr.value from PaymentType pt,LocalizedResource lr where lr.objectKey=pt.key and pt.key<4 and lr.objectType=org.oscm.domobjects.enums.LocalizedObjectTypes.PAYMENT_TYPE_NAME and lr.locale = :locale";
        } else if (LocalizedObjectTypes.REPORT_DESC.equals(objectType)) {
            return "select r.dataContainer.reportName,lr.value from Report r, LocalizedResource lr where lr.objectKey=r.key and lr.objectType=org.oscm.domobjects.enums.LocalizedObjectTypes.REPORT_DESC and lr.locale = :locale";
        }
        return null;
    }

    private List<LocalizedObjectTypes> getLocalizedObjectTypes() {
        List<LocalizedObjectTypes> objectTypes = new ArrayList<LocalizedObjectTypes>();
        objectTypes.add(LocalizedObjectTypes.PARAMETER_DEF_DESC);
        objectTypes.add(LocalizedObjectTypes.EVENT_DESC);
        objectTypes.add(LocalizedObjectTypes.PAYMENT_TYPE_NAME);
        objectTypes.add(LocalizedObjectTypes.REPORT_DESC);
        return objectTypes;
    }
}
