/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Mike J&auml;ger                                                    
 *                                                                              
 *  Creation Date: 30.04.2009                                                      
 *                                                                              
 *  Completion Time: 17.06.2009                                             
 *                                                                              
 *******************************************************************************/

package org.oscm.i18nservice.local;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import javax.ejb.Local;

import org.oscm.domobjects.LocalizedBillingResource;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.enums.LocalizedBillingResourceType;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.domobjects.enums.LocalizedObjectTypes.InformationSource;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.vo.VOLocalizedText;

/**
 * Internal interface providing the functionality to retrieve localized
 * information.
 * 
 * @author Mike J&auml;ger
 * 
 */
@Local
public interface LocalizerServiceLocal {

    /**
     * @return the bes' default locale
     */
    public Locale getDefaultLocale();

    /**
     * Returns the localized resources from the database.
     * 
     * @param localeString
     *            The locale to be used.
     * @param objectKey
     *            The object key for the requested information.
     * @param objectTypes
     *            The types of the resources.
     * @return
     */
    public Map<LocalizedObjectTypes, String> getLocalizedTextFromDatabase(
            String localeString, long objectKey,
            List<LocalizedObjectTypes> objectTypes);

    /**
     * Returns the localized resource from the database.
     * 
     * @param localeString
     *            The locale to be used.
     * @param objectKey
     *            The object key for the requested information.
     * @param objectType
     *            The type of the resource.
     * @return - the localized value if present, or an empty string otherwise.
     */
    public String getLocalizedTextFromDatabase(String localeString,
            long objectKey, LocalizedObjectTypes objectType);

    /**
     * Returns the localized text for the given parameters.
     * 
     * @param type
     *            the localized object type (defining a property file), matching
     *            one of {@link LocalizedObjectTypes}.
     * @param shop
     *            the shop which may refer to customized localized text.
     * @param localeString
     *            the string representing the locale for which the text is
     *            desired
     * @param key
     *            the key for the desired text
     * @return The localized text.
     */
    public String getLocalizedTextFromBundle(LocalizedObjectTypes objectType,
            Marketplace shop, String localeString, String key);

    /**
     * Load all properties for the given key, type, locale triple from the
     * database.
     * 
     * @param objectKey
     *            the key of the domain object for which the properties are
     *            stored.
     * @param objectType
     *            The attribute type which contains the properties, matching one
     *            of {@link LocalizedObjectTypes}.
     * @param localeString
     *            the string representing the local for which the properties are
     *            desired.
     * @return a properties object with all properties of the properties file.
     */
    public Properties loadLocalizedPropertiesFromDatabase(long objectKey,
            LocalizedObjectTypes type, String localeString);

    /**
     * Loads the localized text for multiple domain objects.
     * 
     * @param localeString
     *            the string representing the local for which the properties are
     *            desired
     * @param objectKeys
     *            the keys of the domain objects for which the properties are
     *            stored
     * @param objectTypes
     *            the attribute types which contains the properties, matching
     *            one of {@link LocalizedObjectTypes}.
     * @return The localized text for all given domain objects.
     */
    public List<LocalizedDomainObject> getLocalizedTextFromDatabase(
            String localeString, List<Long> objectKeys,
            List<LocalizedObjectTypes> objectTypes);

    /**
     * Load all properties from a properties file for the given locale.
     * 
     * @param baseName
     *            the base name of the properties file
     * @param localeString
     *            the string representing the local for which the properties are
     *            desired.
     * @return a properties object with all properties of the properties file.
     */
    public Properties loadLocalizedPropertiesFromFile(String baseName,
            String localString);

    /**
     * Checks if all bundle files required by the localizer are present and
     * accessible. If they are not, an error will be logged.
     */
    public void checkExistenceOfBundleFiles();

    /**
     * Stores the given localized information in the database. An
     * {@link IllegalArgumentException} will be thrown in case the objectType
     * refers to an information source differenct than
     * {@link InformationSource#DATABASE}.
     * 
     * @param localeString
     *            The string of the locale the information is given in.
     * @param objectKey
     *            The technical key of the object the information belongs to.
     * @param objectType
     *            The type of the object the information belongs to.
     * @param value
     *            The localized information.
     * @return <code>true</code> in case the an object was updated,
     *         <code>false</code> in case it is a new creation.
     */
    public boolean storeLocalizedResource(String localeString, long objectKey,
            LocalizedObjectTypes objectType, String value);

    /**
     * Stores the given localized information in the database. An
     * {@link IllegalArgumentException} will be thrown in case the objectType
     * refers to an information source differenct than
     * {@link InformationSource#DATABASE}. An already existing resource will be
     * overwritten.
     * 
     * @param localeString
     *            The string of the locale the information is given in.
     * @param objectKey
     *            The technical key of the object the information belongs to.
     * @param objectType
     *            The type of the object the information belongs to.
     * @param value
     *            The localized information.
     * @return <code>true</code> in case the an object was updated,
     *         <code>false</code> in case it is a new creation.
     */
    public void storeLocalizedResources(long objectKey,
            LocalizedObjectTypes objectType, List<VOLocalizedText> values);

    /**
     * Returns a list containing all localized data for the given object key and
     * type.If the object is not found in db,it will search the property file if
     * the object type is
     * InformationSource.DATABASE_AND_RESOURCE_BUNDLE,otherwise search deeper by
     * parent keys.
     * 
     * @param objectKey
     *            The technical key of the object to retrieve the localized
     *            information for.
     * @param objectType
     *            The type of the object to retrieve the information for.
     * @return The localized data for the given object type.
     */
    public List<VOLocalizedText> getLocalizedValues(long objectKey,
            LocalizedObjectTypes objectType);

    /**
     * Sets the localized values for a certain field of an object.
     * 
     * @param key
     *            the object key
     * @param type
     *            the {@link LocalizedObjectTypes} identifying the field to
     *            localize
     * @param values
     *            a list of localized values
     * @throws ConcurrentModificationException
     *             Thrown if the object versions does not match.
     */
    public void setLocalizedValues(long key, LocalizedObjectTypes type,
            List<VOLocalizedText> values)
            throws ConcurrentModificationException;

    /**
     * Permanently removes the localization entries for an entry with the given
     * key and type.
     * 
     * @param objectKey
     *            the object key
     * @param objectType
     *            the type of the object
     */
    public void removeLocalizedValues(long objectKey,
            LocalizedObjectTypes objectType);

    /**
     * Permanently removes a localization entry for the given key, type and
     * locale.
     * 
     * @param objectKey
     *            the object key.
     * @param objectType
     *            the type of the object.
     * @param localeString
     *            the locale string of the localization entry to be removed.
     */
    public void removeLocalizedValue(long objectKey,
            LocalizedObjectTypes objectType, String localeString);
    
    /**
     * Get a localized billing resource
     * 
     * @param localeString
     *            the locale to be used.
     * @param objectID
     *            the object UUID
     * @param objectKey
     *            the object key
     * @return the localized billing resource if one was found for the specified
     *         locale or the default locale; otherwise <null>
     */
    public LocalizedBillingResource getLocalizedBillingResource(
            String localeString, UUID objectID,
            LocalizedBillingResourceType resourceType);

    /**
     * @param localeString - the locale to be used.
     * @param objectId - the object UUID
     * @return the localized price model resource if one was found for the specified
     *         locale or the default locale; otherwise <null>
     */
    public LocalizedBillingResource getLocalizedPriceModelResource(String localeString,
            UUID objectId);
}
