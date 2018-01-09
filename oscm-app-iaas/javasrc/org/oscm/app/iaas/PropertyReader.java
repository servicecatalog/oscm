/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 2013-12-16                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.app.iaas;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.oscm.app.v2_0.data.Setting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles access to multi-dimensional service parameters.
 * <p>
 * Additional LServers can be provisioned and configured by using a certain
 * prefix for the property keys (e.g. LSERVER_#_).
 */
class PropertyReader {
    private static final Logger logger = LoggerFactory
            .getLogger(PropertyReader.class);

    // Local vars
    private String prefix;
    private Map<String, Setting> props;
    private Properties configProps = null;

    /**
     * Constructor
     * 
     * @param sourceProps
     *            the property object to take the settings from
     * @param index
     *            the index of the underlying entity
     */
    PropertyReader(Map<String, Setting> props, String prefix) {
        this.props = props;
        this.prefix = (prefix != null) ? prefix : "";

        // read and parse configuration line (if existing)
        String configLine = getProperty("CONFIG");
        if (configLine != null) {
            configProps = new Properties();
            // Split into key-value pairs (and support
            // "\" escaping of delimiters ";" and "=")
            String regex = "(\\w+)=((?:\\\\.|[^;])*)";
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(configLine);
            while (m.find()) {
                // replace escaped characters
                String value = m.group(2).replaceAll("\\\\;", ";");
                value = value.replaceAll("\\\\=", "=");
                configProps.put(m.group(1), value);
            }
        }
    }

    /**
     * Reads the requested property from the available parameters. If no value
     * can be found, NULL will be returned.
     * 
     * @param key
     *            the key to retrieve the setting for
     * @return the parameter value corresponding to the provided key
     */
    public String getProperty(String key) {
        Setting setting = props.get(this.prefix + key);
        String value = setting != null ? setting.getValue() : null;
        if (value == null && configProps != null) {
            return configProps.getProperty(key);
        }
        return value;
    }

    /**
     * Reads the requested property from the available parameters. If no value
     * can be found, a RuntimeException will be thrown.
     * 
     * @param key
     *            the key to retrieve the setting for
     * @return the parameter value corresponding to the provided key
     */
    public String getValidatedProperty(String key) {
        String value = getProperty(key);
        if (value == null) {
            String message = String.format("No value set for property '%s'",
                    this.prefix + key);
            logger.error(message);
            throw new RuntimeException(message);
        }
        return value;
    }

    /**
     * Updates the requested property from the available parameters.
     * 
     * @param key
     *            the key to update the setting for
     * @param value
     *            the value to update
     */
    public void setProperty(String key, String value) {
        // NULL values are not supported by APP!!! (use empty instead)
        if (value == null) {
            logger.warn("PropertyReader.setProperty: NULL value given for property "
                    + key + "! Using EMPTY value instead.");
            value = "";
        }
        props.put(this.prefix + key, new Setting(this.prefix + key, value));
    }

    /**
     * Removes the requested property from the available parameters.
     * 
     * @param key
     *            the key of the entry to clear
     */
    public void clearProperty(String key) {
        props.remove(this.prefix + key);
    }

    /**
     * Returns a list of property keys which match to the given key prefix.
     * 
     * @param keyprefix
     *            the prefix of all requested keys
     * @return a list with all defined property keys
     */
    public List<String> getPropertyKeys(String keyprefix) {
        String realprefix = this.prefix
                + ((keyprefix != null) ? keyprefix : "");
        List<String> result = new ArrayList<>();
        for (String key : props.keySet()) {
            if (key.startsWith(realprefix)) {
                result.add(key.substring(this.prefix.length()));
            }
        }
        return result;
    }
}
