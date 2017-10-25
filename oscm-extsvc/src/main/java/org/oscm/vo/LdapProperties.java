/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2012-01-17                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.vo;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Represents a collection of properties that consist of a key and a value.
 * Both, the key and the value, must be of the <code>String</code> type.
 */
public class LdapProperties implements Serializable {

    private static final long serialVersionUID = 7704491822913101030L;
    private Set<Setting> settings = new HashSet<Setting>();

    /**
     * Default constructor.
     */
    public LdapProperties() {
    }

    /**
     * Instantiates a new collection of properties.
     * 
     * @param properties
     *            the properties to include in the set. Each property must
     *            consist of a key and a value of the <code>String</code> type.
     */
    public LdapProperties(Properties properties) {
        if (properties != null) {
            for (Map.Entry<Object, Object> propertiesEntry : properties
                    .entrySet()) {
                settings.add(new Setting((String) propertiesEntry.getKey(),
                        (String) propertiesEntry.getValue()));
            }
        }
    }

    /**
     * Retrieves the value of the property with the given key.
     * 
     * @param key
     *            the key of the property whose value is to be returned
     * @return the value, or <code>null</code> if the property with the given
     *         key is not found in the collection
     */
    public String getProperty(String key) {
        Setting setting = getSetting(key);
        return (setting == null) ? null : setting.getValue();
    }

    private Setting getSetting(String key) {
        for (Setting entry : settings) {
            if (entry.getKey().equals(key)) {
                return entry;
            }
        }
        return null;
    }

    /**
     * Returns the value of the property with the given key or a given default
     * value.
     * 
     * @param key
     *            the key of the property whose value is to be returned
     * @param defaultValue
     *            the string to return if the property is not found or its value
     *            is <code>null</code>
     * @return the value of the property, or the given default value if the
     *         property is not found in the collection or its value is
     *         <code>null</code>
     */
    public String getProperty(String key, String defaultValue) {
        String val = getProperty(key);
        return (val == null) ? defaultValue : val;
    }

    /**
     * Sets the specified value for the property with the given key.
     * 
     * @param key
     *            the key of the property whose value is to be set
     * @param value
     *            the value
     * @return the property's previous value if the key already exists, or
     *         <code>null</code> if the key is new
     */
    public String setProperty(String key, String value) {
        Setting previousEntry = getSetting(key);
        if (previousEntry != null) {
            settings.remove(previousEntry);
        }
        settings.add(new Setting(key, value));
        return previousEntry == null ? null : previousEntry.getValue();
    }

    /**
     * Returns the properties in the collection as
     * <code>java.util.Properties</code>.
     * 
     * @return the converted properties
     */
    public Properties asProperties() {
        Properties properties = new Properties();
        for (Setting entry : settings) {
            properties.setProperty(entry.getKey(), entry.getValue());
        }
        return properties;
    }

    /**
     * Returns the properties in the collection as a set of key-value pairs.
     * 
     * @return the <code>java.util.Set</code> of key-value pairs
     */
    public Set<Setting> getSettings() {
        return settings;
    }

    /**
     * Sets the properties in the collection from the given set of key-value
     * pairs.
     * 
     * @param entries
     *            the <code>java.util.Set</code> of key-value pairs
     */
    public void setSettings(Set<Setting> entries) {
        this.settings = entries;
    }

    /**
     * Returns the given <code>java.util.Properties</code> as a collection of
     * properties.
     * 
     * @param properties
     *            the properties to convert
     * @return the collection of properties
     */
    public static LdapProperties get(Properties properties) {
        if (properties == null) {
            return null;
        } else {
            return new LdapProperties(properties);
        }
    }

}
