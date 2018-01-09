/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: Mike J&auml;ger                                                      
 *                                                                              
 *  Creation Date: 30.04.2009                                                      
 *                                                                              
 *  Completion Time: 17.06.2009                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.i18nservice.bean;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.Spliterator;
import java.util.UUID;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.Spliterator;
import java.util.UUID;

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.Query;

import org.oscm.converter.LocaleHandler;
import org.oscm.converter.ParameterizedTypes;
import org.oscm.converter.PropertiesLoader;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.LocalizedBillingResource;
import org.oscm.domobjects.LocalizedResource;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.PriceModel;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.enums.LocalizedBillingResourceType;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.domobjects.enums.LocalizedObjectTypes.InformationSource;
import org.oscm.i18nservice.local.LocalizedDomainObject;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.SaaSSystemException;
import org.oscm.internal.vo.VOLocalizedText;
import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.types.enumtypes.StandardLanguage;

/**
 * Bean implementation to provide the localized information for the given
 * locale.
 * 
 * @author Mike J&auml;ger
 * 
 */
@Stateless
@Local(LocalizerServiceLocal.class)
public class LocalizerServiceBean implements LocalizerServiceLocal {

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(LocalizerServiceBean.class);
    private static final Locale defaultLocale = Locale.ENGLISH;

    @EJB(beanInterface = DataService.class)
    private DataService dm;

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public String getLocalizedTextFromBundle(LocalizedObjectTypes objectType,
            Marketplace marketplace, String localeString, String key) {

        String result = null;

        if (objectType.getSource() == InformationSource.RESOURCE_BUNDLE) {
            result = getLocalizedTextFromResourceBundle(
                    objectType.getSourceLocation(), marketplace, localeString,
                    key);
        } else if (objectType.getSource() == InformationSource.DATABASE_AND_RESOURCE_BUNDLE) {
            result = getLocalizedTextFromResourceBundleForPlatformObjects(
                    objectType.getSourceLocation(), localeString, key);
        } else {
            logger.logWarn(Log4jLogger.SYSTEM_LOG,
                    LogMessageIdentifier.WARN_NON_SUPPORTED_LOCALE,
                    String.valueOf(objectType.getSource()));
        }

        return result;
    }

    /**
     * Returns the localized text from a resource bundle (which is defined by a
     * property file and a marketplace attribute).
     * 
     * @param baseName
     *            the baseName name of the property file
     * @param marketplace
     *            the shop domain object which may hold customized properties
     * @param localeString
     *            the string representing the locale for which the text is
     *            desired
     * @param key
     *            the key for the desired text
     * @return the localized text.
     */
    private String getLocalizedTextFromResourceBundle(String baseName,
            Marketplace marketplace, String localeString, String key) {

        Locale locale = LocaleHandler.getLocaleFromString(localeString);

        String result = null;
        while (result == null && locale.getLanguage().length() > 0) {
            // first we try to get the text from the customized messages
            if (marketplace != null) {
                result = getLocalizedTextFromDB(marketplace.getKey(),
                        LocalizedObjectTypes.SHOP_MESSAGE_PROPERTIES, locale,
                        key);
            }
            if (result == null) {
                result = getLocalizedTextFromDB(0L,
                        LocalizedObjectTypes.MESSAGE_PROPERTIES, locale, key);
            }
            if (result == null) {
                result = getLocalizedTextFromDB(0L,
                        LocalizedObjectTypes.MAIL_PROPERTIES, locale, key);
            }
            // next we try to get it from the current bundle (and not from one
            // of its parents)
            if (result == null) {
                try {
                    ResourceBundle bundle = ResourceBundle.getBundle(baseName,
                            locale);
                    if (bundle.getLocale().equals(locale)) {
                        result = (String) ((PropertyResourceBundle) bundle)
                                .handleGetObject(key);
                    }
                } catch (MissingResourceException e) {
                    logger.logWarn(Log4jLogger.SYSTEM_LOG,
                            LogMessageIdentifier.WARN_MISSING_BUNDLE, baseName,
                            String.valueOf(locale), key);
                }
            }
            // if we still didn't find a text we use the parent locale
            if (result == null) {
                locale = getParentLocale(locale);
            }
        }

        if (result == null) {
            result = key;
        }

        return result;
    }

    private String getLocalizedTextFromResourceBundleForPlatformObjects(
            String baseName, String localeString, String key) {

        Locale locale = LocaleHandler.getLocaleFromString(localeString);

        String result = null;
        while (result == null && locale.getLanguage().length() > 0) {
            try {
                ResourceBundle bundle = ResourceBundle.getBundle(baseName,
                        locale);
                if (bundle.getLocale().equals(locale)) {
                    result = (String) ((PropertyResourceBundle) bundle)
                            .handleGetObject(key);
                }
            } catch (MissingResourceException e) {
                logger.logWarn(Log4jLogger.SYSTEM_LOG,
                        LogMessageIdentifier.WARN_MISSING_BUNDLE, baseName,
                        String.valueOf(locale), key);
            }
            // if we still didn't find a text we use the parent locale
            if (result == null) {
                locale = getParentLocale(locale);
            }
        }

        if (result == null) {
            result = "";
        }

        return result;
    }

    private String getLocalizedTextFromDB(long objectKey,
            LocalizedObjectTypes type, Locale locale, String propertyKey) {
        Properties props = loadLocalizedPropertiesFromDatabase(objectKey, type,
                locale.toString());
        return props.getProperty(propertyKey);
    }

    /**
     * Construct a parent locale for the given locale in the following order (:
     * 
     * language, country, variant
     * 
     * language, country
     * 
     * language
     * 
     * defaultLanguage
     * 
     * @param locale
     *            The start locale.
     * @return The parent locale.
     */
    private Locale getParentLocale(Locale locale) {
        if (locale.getVariant().length() == 0) {
            if (locale.getCountry().length() == 0) {
                if (locale.getLanguage().equals(defaultLocale.getLanguage())) {
                    return new Locale("");
                }
                return new Locale(defaultLocale.getLanguage());
            } else {
                return new Locale(locale.getLanguage());
            }
        } else {
            return new Locale(locale.getLanguage(), locale.getCountry());
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public Map<LocalizedObjectTypes, String> getLocalizedTextFromDatabase(
            String localeString, long objectKey,
            List<LocalizedObjectTypes> objectTypes) {

        Locale locale = LocaleHandler.getLocaleFromString(localeString);
        KeysForOneObject keysForObjects = getKeysForObjects(objectKey,
                objectTypes);

        return getResourcesFromDatabase(
                objectTypes, Collections.singletonList(locale.getLanguage()),
                Collections.singletonList(keysForObjects)).get(0)
                .getLocalizedResources();
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public String getLocalizedTextFromDatabase(String localeString,
            long objectKey, LocalizedObjectTypes objectType) {

        String result = null;
        Locale locale = LocaleHandler.getLocaleFromString(localeString);
        LocalizedResource resource = null;
        KeysForOneObject keysForObjects = getKeysForObjects(objectKey,
                Collections.singletonList(objectType));

        while (resource == null && locale.getLanguage().length() > 0) {
            resource = getResourceFromDatabase(objectType, locale,
                    keysForObjects);
            locale = getParentLocale(locale);
        }

        if (resource == null) {
            // resource not found
            logger.logDebug("Localized information of type '" + objectType
                    + "' of object with key '" + objectKey
                    + "' could not be found.", Log4jLogger.SYSTEM_LOG);
            result = objectType.getDefaultValue();
            if (result == null) {
                result = "";
            }
        } else {
            result = resource.getValue();
        }

        return result;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public List<LocalizedDomainObject> getLocalizedTextFromDatabase(
            String localeString, List<Long> objectKeys,
            List<LocalizedObjectTypes> objectTypes) {

        if (objectKeys.isEmpty()) {
            return Collections.emptyList();
        }

        // fallback keys that must be loaded as well
        List<KeysForOneObject> keysForAllObjects = new ArrayList<>();
        for (Long objectKey : objectKeys) {
            KeysForOneObject keysForOneObject = getKeysForObjects(
                    objectKey.longValue(), objectTypes);
            keysForAllObjects.add(keysForOneObject);
        }

        // fallback languages must be loaded as well
        List<String> locales = new ArrayList<>();
        Locale locale = LocaleHandler.getLocaleFromString(localeString);
        while (locale.getLanguage().length() > 0) {
            locales.add(locale.getLanguage());
            locale = getParentLocale(locale);
        }
        Collections.reverse(locales);

        return getResourcesFromDatabase(
                objectTypes, locales, keysForAllObjects);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public Properties loadLocalizedPropertiesFromDatabase(long objectKey,
            LocalizedObjectTypes objectType, String localeString) {
        Properties properties = new Properties();
        LocalizedResource template = new LocalizedResource(localeString,
                objectKey, objectType);
        LocalizedResource resource = (LocalizedResource) dm.find(template);
        if (resource != null && resource.getValue().length() > 0) {
            try {
                // Property files are always encoded in ISO-8859-1:
                final InputStream inputStream = new ByteArrayInputStream(
                        resource.getValue().getBytes("ISO-8859-1"));
                properties.load(inputStream);
                inputStream.close();
            } catch (IOException e) {
                logger.logError(Log4jLogger.SYSTEM_LOG, e,
                        LogMessageIdentifier.ERROR_OBJECT_ENCODING_FAILED);
            }
        }

        return properties;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public Properties loadLocalizedPropertiesFromFile(String baseName,
            String localeString) {
        Properties properties = new Properties();
        // only standard languages have mail.properties file
        if (!LocaleHandler.isStandardLanguage(new Locale(localeString))) {
            return properties;
        }
        final String resource = baseName + "_" + localeString
                + ".properties";
        properties = PropertiesLoader
                .load(LocalizerServiceBean.class, resource);
        return properties;
    }

    @Override
    public void checkExistenceOfBundleFiles() {

        for (LocalizedObjectTypes type : LocalizedObjectTypes.values()) {
            if (type.getSource() == InformationSource.RESOURCE_BUNDLE) {
                // get bundle name and verify that it can be loaded in the
                // default locale
                String baseBundleName = type.getSourceLocation();
                try {
                    ResourceBundle bundle = ResourceBundle.getBundle(
                            baseBundleName, defaultLocale);
                    if (!bundle.getLocale().getLanguage()
                            .equals(defaultLocale.getLanguage())) {
                        logger.logError(
                                LogMessageIdentifier.ERROR_REQUIRED_BUNDLE_NOT_FOUND,
                                baseBundleName, String.valueOf(defaultLocale));
                    }
                } catch (MissingResourceException e) {
                    logger.logError(
                            Log4jLogger.SYSTEM_LOG,
                            e,
                            LogMessageIdentifier.ERROR_REQUIRED_BUNDLE_NOT_FOUND,
                            baseBundleName, String.valueOf(defaultLocale));
                }
            }
        }

    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void storeLocalizedResources(long objectKey,
            LocalizedObjectTypes objectType, List<VOLocalizedText> values) {
        if (values != null) {
            for (final VOLocalizedText v : values) {
                storeLocalizedResource(v.getLocale(), objectKey, objectType,
                        v.getText());
            }
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public boolean storeLocalizedResource(String localeString, long objectKey,
            LocalizedObjectTypes objectType, String value) {

        boolean isUpdate = false;

        if (!objectType.getSource().canBeModified()) {
            IllegalArgumentException iae = new IllegalArgumentException(
                    "Only localized information in the database can be modified");
            logger.logError(Log4jLogger.SYSTEM_LOG, iae,
                    LogMessageIdentifier.ERROR_LOCALIZE_RESOURCE_FAILED,
                    String.valueOf(objectType.getSource()));
            throw iae;
        }

        LocalizedResource template = new LocalizedResource(localeString,
                objectKey, objectType);
        LocalizedResource storedResource = (LocalizedResource) dm
                .find(template);

        if (storedResource == null) {
            LocalizedResource resourceToPersist = new LocalizedResource();
            resourceToPersist.setLocale(localeString);
            resourceToPersist.setObjectKey(objectKey);
            resourceToPersist.setObjectType(objectType);
            resourceToPersist.setValue(value);
            try {
                dm.persist(resourceToPersist);
            } catch (NonUniqueBusinessKeyException e) {
                SaaSSystemException sse = new SaaSSystemException(
                        "Localized Resource could not be persisted although prior check was performed, "
                                + resourceToPersist, e);
                logger.logError(
                        Log4jLogger.SYSTEM_LOG,
                        sse,
                        LogMessageIdentifier.ERROR_PERSIST_LOCALIZED_RESOURCE_FAILED_PRIOR_CHECK_PERFORMED,
                        String.valueOf(resourceToPersist));
                throw sse;
            }
        } else {
            isUpdate = true;
            storedResource.setValue(value);
            // TODO dm.merge(storedResource);
        }

        return isUpdate;
    }

    /**
     * Returns the keys the database has to be queried for in order to return
     * the required localized information. Is only relevant in relation to some
     * copied elements, as it internally determines the parent and queries for
     * both, the current object key and its parent's key. If the object is not
     * copied, only the current key will be returned.
     * 
     * @param objectKey
     *            The key of the current object.
     * @param objectTypes
     *            The type of the requested resource.
     * 
     * 
     * @return A list of keys to query for. At most two keys will be returned.
     */
    private KeysForOneObject getKeysForObjects(long objectKey,
            List<LocalizedObjectTypes> objectTypes) {

        KeysForOneObject result = new KeysForOneObject();
        result.setPrimaryObjKey(Long.valueOf(objectKey));

        // the object is either product related or price model related!
        boolean prRelated = false;
        boolean pmRelated = false;
        for (LocalizedObjectTypes lot : objectTypes) {
            if (!prRelated && lot.isProductRelated()) {
                prRelated = true;
                break;
            } else if (!pmRelated && lot.isPriceModelRelated()) {
                pmRelated = true;
                break;
            }
        }

        Product prod = null;
        if (prRelated) {
            // determine the product
            prod = dm.find(Product.class, objectKey);
        } else if (pmRelated) {
            PriceModel pm = dm.find(PriceModel.class, objectKey);
            if (pm != null) {
                // occurs in case the price model is not persisted or has been
                // removed in the meantime
                prod = pm.getProduct();
            }
        }

        // read the template if there is any, and store its key as well
        if (prod != null) {
            Product template = prod.getTemplate();
            if (template != null) {
                if (prRelated) {
                    result.getFallBackObjKeys().add(
                            Long.valueOf(template.getKey()));
                } else if (pmRelated) {
                    if (template.getPriceModel() != null) {
                        result.getFallBackObjKeys()
                                .add(Long.valueOf(template.getPriceModel()
                                        .getKey()));
                    }
                }
            }
        }

        return result;
    }

    /**
     * Queries the db to get the localized strings for the given object types.
     * If two matches found for an object type, depending on the keysForObjetcs,
     * always the one, matching the object key is used.
     * 
     * @param keysForObjects
     *            The key of the current object.
     * @param objectTypes
     *            The object types the localized resources have to be retrieved
     *            for.
     * @param locales
     *            The locale the information has to be retrieved for.
     * @return
     */
    private List<LocalizedDomainObject> getResourcesFromDatabase(
            List<LocalizedObjectTypes> objectTypes, List<String> locales,
            List<KeysForOneObject> keysForObjects) {

        // create list of all object keys
        List<Long> keysForAllObjects = new ArrayList<>();
        for (KeysForOneObject keysForOneObject : keysForObjects) {
            keysForAllObjects.addAll(keysForOneObject.getAllObjKeys());
        }

        // query
        Query query = dm.createNamedQuery("LocalizedResource.getAll");
        query.setParameter("locales", locales);
        query.setParameter("objectTypes", objectTypes);
        query.setParameter("objectKeys", keysForAllObjects);
        List<LocalizedResource> queryResult = ParameterizedTypes.list(
                query.getResultList(), LocalizedResource.class);

        // build result map:
        List<LocalizedDomainObject> result = new ArrayList<>();
        for (KeysForOneObject keysForOneObject : keysForObjects) {
            Map<LocalizedObjectTypes, String> resultOfOneObject = localizeOneDomainObject(
                    objectTypes,
                    keysForOneObject,
                    searchForOneDomainObject(locales, keysForOneObject,
                            queryResult));
            result.add(new LocalizedDomainObject(keysForOneObject
                    .getPrimaryObjKey().longValue(), resultOfOneObject));
        }

        return result;
    }

    /**
     * Returns all localized resources for a given domain object.
     */
    private List<LocalizedResource> searchForOneDomainObject(
            List<String> locales, KeysForOneObject keysForOneObject,
            List<LocalizedResource> queryResult) {
        List<LocalizedResource> result = new ArrayList<>();
        for (String locale : locales) {
            for (Long key : keysForOneObject.getAllObjKeys()) {
                result.addAll(search(key, locale, queryResult));
            }
        }
        return result;
    }

    /**
     * Search in the query result all localized resources for a given key and
     * locale
     */
    private List<LocalizedResource> search(Long key, String locale,
            List<LocalizedResource> queryResult) {
        List<LocalizedResource> result = new ArrayList<>();
        for (LocalizedResource localizedResource : queryResult) {
            if (localizedResource.getObjectKey() == key.longValue()
                    && localizedResource.getLocale().equals(locale)) {
                result.add(localizedResource);
            }
        }
        return result;
    }

    private Map<LocalizedObjectTypes, String> localizeOneDomainObject(
            List<LocalizedObjectTypes> objectTypes,
            KeysForOneObject keysForOneObject,
            List<LocalizedResource> queryResultList) {
        // 1) put query result into a map, replace parent object key if needed
        Map<LocalizedObjectTypes, LocalizedResource> resultQueryMap = new HashMap<>();
        for (LocalizedResource lr : queryResultList) {
            if (!resultQueryMap.containsKey(lr.getObjectType())) {
                resultQueryMap.put(lr.getObjectType(), lr);
            } else { // key already existent
                if (lr.getObjectKey() == keysForOneObject.getPrimaryObjKey()
                        .longValue()) { // replace
                    resultQueryMap.put(lr.getObjectType(), lr);
                }
            }
        }
        // 2) put object types and localized text to the result map
        Map<LocalizedObjectTypes, String> resultMap = new HashMap<>();
        for (LocalizedObjectTypes lot : objectTypes) {
            if (resultQueryMap.containsKey(lot)) {
                resultMap.put(lot, resultQueryMap.get(lot).getValue());
            } else { // resource not found, use default value if possible
                logger.logDebug("Localized information of type '" + lot
                        + "' of object with key '"
                        + keysForOneObject.getPrimaryObjKey().longValue()
                        + "' could not be found.", Log4jLogger.SYSTEM_LOG);
                String defaultValue = lot.getDefaultValue();
                if (defaultValue != null) {
                    resultMap.put(lot, defaultValue);
                } else {
                    resultMap.put(lot, "");
                }
            }
        }
        return resultMap;
    }

    LocalizedResource find(String locale, long objKey,
            List<LocalizedResource> resources) {
        for (LocalizedResource localizedResource : resources) {
            if (localizedResource.getLocale().equals(locale)
                    && localizedResource.getObjectKey() == objKey) {
                return localizedResource;
            }
        }
        return null;
    }

    /**
     * Depending on the number of key values passed queries for the localized
     * string matching the given resource. If two matches are found, always the
     * one matching the given key, not it's parent key, is returned.
     *
     * @param objectType
     *            The object type the localized resource has to be retrieved
     *            for.
     * @param locale
     *            The locale the information has to be retrieved for.
     * @param keysForObjects
     *            The key values the resource must have assigned.
     * @return The localized resource.
     */
    private LocalizedResource getResourceFromDatabase(
            LocalizedObjectTypes objectType, Locale locale,
            KeysForOneObject keysForObjects) {

        LocalizedResource resource = null;
        if (keysForObjects.getAllObjKeys().size() == 1) {
            LocalizedResource template = new LocalizedResource(
                    locale.toString(), keysForObjects.getPrimaryObjKey()
                            .longValue(), objectType);
            resource = (LocalizedResource) dm.find(template);
        } else {
            Query query = dm
                    .createNamedQuery("LocalizedResource.getForCurrAndParentKey");
            query.setParameter("locale", locale.getLanguage());
            query.setParameter("objectType", objectType);
            query.setParameter("objectKeyChild",
                    keysForObjects.getPrimaryObjKey());
            query.setParameter("objectKeyParent", keysForObjects
                    .getAllObjKeys().get(1));

            List<LocalizedResource> resultList = ParameterizedTypes.list(
                    query.getResultList(), LocalizedResource.class);
            if (resultList.size() == 1) {
                resource = resultList.get(0);
            } else if (resultList.size() == 2) {
                if (resultList.get(0).getObjectKey() == keysForObjects
                        .getPrimaryObjKey().longValue()) {
                    resource = resultList.get(0);
                } else {
                    resource = resultList.get(1);
                }
            }
        }

        return resource;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public List<VOLocalizedText> getLocalizedValues(long objectKey,
            LocalizedObjectTypes objectType) {
        List<VOLocalizedText> results = new ArrayList<>();
        Query query = dm
                .createNamedQuery("LocalizedResource.getAllTextsWithLocale");
        query.setParameter("objectKey", Long.valueOf(objectKey));
        query.setParameter("objectType", objectType);
        for (LocalizedResource resource : ParameterizedTypes.iterable(
                query.getResultList(), LocalizedResource.class)) {
            results.add(new VOLocalizedText(resource.getLocale(), resource
                    .getValue(), resource.getVersion()));
        }
        if (objectType.getSource() == InformationSource.DATABASE_AND_RESOURCE_BUNDLE) {
            return getLocalizedValuesFromBundle(objectKey, objectType, results);
        }
        if (results.isEmpty()) {
            List<Long> keysForObjects = getKeysForObjects(objectKey,
                    Collections.singletonList(objectType)).getAllObjKeys();
            if (keysForObjects.size() > 1
                    && objectKey != keysForObjects.get(1).longValue()) {
                // read the localized values from the template
                results = getLocalizedValues(keysForObjects.get(1).longValue(),
                        objectType);
            }
        }
        return results;
    }

    private List<VOLocalizedText> getLocalizedValuesFromBundle(long objectKey,
            LocalizedObjectTypes objectType, List<VOLocalizedText> results) {

        Map<String, String> resultMap = new HashMap<>();
        for (VOLocalizedText result : results) {
            resultMap.put(result.getLocale(), result.getText());
        }
        for (StandardLanguage standardLanguage : StandardLanguage.values()) {
            if (resultMap.get(standardLanguage.toString()) == null) {
                String text = getLocalizedTextFromBundle(objectType, null,
                        standardLanguage.toString(),
                        generatePropertiesKey(objectType, objectKey));
                if (text != null) {
                    results.add(new VOLocalizedText(
                            standardLanguage.toString(), text));
                }
            }
        }

        return results;
    }

    private String generatePropertiesKey(LocalizedObjectTypes objectType,
            long objectKey) {
        return objectType.toString() + "." + objectKey;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void setLocalizedValues(long objectKey,
            LocalizedObjectTypes objectType, List<VOLocalizedText> values)
            throws ConcurrentModificationException {
        if (values == null) {
            return;
        }
        for (VOLocalizedText text : values) {
            store(objectKey, objectType, text);
        }
    }

    private void store(long objectKey, LocalizedObjectTypes objectType,
            VOLocalizedText text) throws ConcurrentModificationException {
        LocalizedResource template = new LocalizedResource(text.getLocale(),
                objectKey, objectType);
        LocalizedResource storedResource = (LocalizedResource) dm
                .find(template);
        if (text.getText() == null || text.getText().length() == 0) {
            if (storedResource != null) {
                verifyVersion(storedResource, text);
                dm.remove(storedResource);
            }
        } else if (storedResource == null) {
            LocalizedResource resourceToPersist = new LocalizedResource();
            resourceToPersist.setLocale(text.getLocale());
            resourceToPersist.setObjectKey(objectKey);
            resourceToPersist.setObjectType(objectType);
            resourceToPersist.setValue(text.getText());
            try {
                dm.persist(resourceToPersist);
            } catch (NonUniqueBusinessKeyException e) {
                SaaSSystemException sse = new SaaSSystemException(
                        "Localized Resource could not be persisted although prior check was performed, "
                                + resourceToPersist, e);
                logger.logError(
                        Log4jLogger.SYSTEM_LOG,
                        sse,
                        LogMessageIdentifier.ERROR_PERSIST_LOCALIZED_RESOURCE_FAILED_PRIOR_CHECK_PERFORMED,
                        String.valueOf(resourceToPersist));
                throw sse;
            }
        } else {
            verifyVersion(storedResource, text);
            storedResource.setValue(text.getText());
            // TODO dm.merge(storedResource);
        }
    }

    private void verifyVersion(LocalizedResource toBeUpdated,
            VOLocalizedText template) throws ConcurrentModificationException {
        if (toBeUpdated.getVersion() > template.getVersion()
                && !toBeUpdated.getValue().equals(template.getText())) {
            ConcurrentModificationException cme = new ConcurrentModificationException(
                    VOLocalizedText.class.getName(), template.getVersion());
            logger.logWarn(Log4jLogger.SYSTEM_LOG, cme,
                    LogMessageIdentifier.WARN_CONCURRENT_MODIFICATION,
                    VOLocalizedText.class.getName(),
                    String.valueOf(template.getVersion()));
            throw cme;
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void removeLocalizedValues(long objectKey,
            LocalizedObjectTypes objectType) {

        Query query = dm
                .createNamedQuery("LocalizedResource.deleteForObjectAndType");
        query.setParameter("objectKey", Long.valueOf(objectKey));
        query.setParameter("objectType", objectType);
        query.executeUpdate();

    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public void removeLocalizedValue(long objectKey,
            LocalizedObjectTypes objectType, String localeString) {

        Query query = dm
                .createNamedQuery("LocalizedResource.deleteForObjectAndTypeAndLocale");
        query.setParameter("objectKey", Long.valueOf(objectKey));
        query.setParameter("objectType", objectType);
        query.setParameter("locale", localeString);
        query.executeUpdate();

    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public Locale getDefaultLocale() {
        return defaultLocale;
    }

    /**
     * Container for all domain object keys that must be localized. Some domain
     * objects have fallback rules for translation (e.g. in case no localization
     * is found for a product, then the localization from product template is
     * used). This container stores the key of the domain object to be
     * translated, as well as all keys that must be used as fallback.
     */
    private static class KeysForOneObject {

        Long primaryObjKey;

        List<Long> fallBackObjKeys = new ArrayList<>();

        public Long getPrimaryObjKey() {
            return primaryObjKey;
        }

        public void setPrimaryObjKey(Long primaryObjKey) {
            this.primaryObjKey = primaryObjKey;
        }

        public List<Long> getFallBackObjKeys() {
            return fallBackObjKeys;
        }

        public List<Long> getAllObjKeys() {
            return new AbstractList<Long>() {

                @Override
                public Long get(int index) {
                    if (index == 0) {
                        return primaryObjKey;
                    }
                    return fallBackObjKeys.get(index - 1);
                }

                @Override
                public Spliterator<Long> spliterator() {
                    return super.spliterator();
                }

                @Override
                public int size() {
                    return fallBackObjKeys.size() + 1;
                }

            };
        }

    }

    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public LocalizedBillingResource getLocalizedBillingResource(
            String localeString, UUID objectId,
            LocalizedBillingResourceType resourceType) {

        Locale locale = LocaleHandler.getLocaleFromString(localeString);
        LocalizedBillingResource billingResource = getLocalizedBillingResourceFromDatabase(
                objectId, locale.getLanguage(), resourceType);
        if (billingResource == null) {
            billingResource = getLocalizedBillingResourceFromDatabase(objectId,
                    defaultLocale.getLanguage(), resourceType);
        }

        if (billingResource != null) {
            return billingResource;
        } else {
            logger.logDebug("Localized billing resource of type '"
                    + resourceType.name() + "' of object with id '" + objectId
                    + "' could not be found.", Log4jLogger.SYSTEM_LOG);
            return null;
        }
    }
    
    
    @Override
    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public LocalizedBillingResource getLocalizedPriceModelResource(
            String localeString, UUID objectId) {

        Locale locale = LocaleHandler.getLocaleFromString(localeString);
        LocalizedBillingResource billingResource = getLocalizedPriceModelResourceFromDatabase(
                objectId, locale.getLanguage());
        if (billingResource == null) {
            billingResource = getLocalizedPriceModelResourceFromDatabase(objectId,
                    defaultLocale.getLanguage());
        }

        if (billingResource != null) {
            return billingResource;
        } else {
            logger.logDebug("Localized price model of object with id '" + objectId
                    + "' could not be found.", Log4jLogger.SYSTEM_LOG);
            return null;
        }
    }
    
    protected LocalizedBillingResource getLocalizedBillingResourceFromDatabase(
            UUID objectId, String locale,
            LocalizedBillingResourceType resourceType) {

        // query
        Query query = dm
                .createNamedQuery("LocalizedBillingResource.findByBusinessKey");
        query.setParameter("objectId", objectId);
        query.setParameter("locale", locale);
        query.setParameter("resourceType", resourceType);
        List<LocalizedBillingResource> queryResult = ParameterizedTypes.list(
                query.getResultList(), LocalizedBillingResource.class);

        if (!queryResult.isEmpty()) {
            return queryResult.get(0);
        } else {
            return null;
        }
    }

    
    protected LocalizedBillingResource getLocalizedPriceModelResourceFromDatabase(
            UUID objectId, String locale) {

        // query
        Query query = dm
                .createNamedQuery("LocalizedBillingResource.findPriceModelByBusinessKey");
        query.setParameter("objectId", objectId);
        query.setParameter("locale", locale);
        List<LocalizedBillingResource> queryResult = ParameterizedTypes.list(
                query.getResultList(), LocalizedBillingResource.class);

        if (!queryResult.isEmpty()) {
            return queryResult.get(0);
        } else {
            return null;
        }
    }
}
