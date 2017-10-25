/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2013-11-12                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.converter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.internal.types.exception.SaaSSystemException;

/**
 * Converter of properties
 * 
 * @author Gao
 * 
 */
public class PropertiesConverter {

    private static final Log4jLogger LOGGER = LoggerFactory
            .getLogger(PropertiesConverter.class);

    /**
     * Writes the properties into a string.
     * 
     * @param props
     *            the properties to be written.
     * @return the string representing the properties.
     */
    public static String propertiesToString(Properties props) {
        if (props == null) {
            return null;
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            props.store(outputStream, "");
            // Property files are always encoded in ISO-8859-1:
            return outputStream.toString("ISO-8859-1");
        } catch (IOException e) {
            SaaSSystemException se = new SaaSSystemException(
                    "Storing properties failed!", e);
            LOGGER.logError(Log4jLogger.SYSTEM_LOG, se,
                    LogMessageIdentifier.ERROR_STORE_PROPERTIES_FAILED);
            throw se;
        }
    }

    /**
     * Writes the properties into a string, ignore keys when value ="".
     * 
     * @param props
     *            the properties to be written.
     * @return the string representing the properties.
     */
    public static String propertiesToStringIgnoreEmptyKeys(Properties props) {
        return propertiesToString(removeEmptyValue(props));
    }

    /**
     * remove the empty value of properties
     * 
     * @param properties
     *            the properties to be removed empty value.
     * @return the properties after removing empty value.
     */
    public static Properties removeEmptyValue(Properties properties) {
        if (properties == null) {
            return properties;
        }
        Enumeration<Object> keys = properties.keys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement().toString();
            if (properties.getProperty(key) == null
                    || properties.getProperty(key).trim().length() < 1) {
                properties.remove(key);
            }
        }
        return properties;
    }

    /**
     * count the non-empty value of properties
     * 
     * @param properties
     *            the properties to be count non-empty value.
     * @return the number of non-empty value of properties.
     */
    public static int countNonEmptyValue(Properties properties) {
        int numOfNotEmptyValue = 0;
        if (properties == null) {
            return numOfNotEmptyValue;
        }
        Enumeration<Object> keys = properties.keys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement().toString();
            if (properties.getProperty(key) != null
                    && properties.getProperty(key).trim().length() > 0) {
                numOfNotEmptyValue++;
            }
        }
        return numOfNotEmptyValue;
    }

}
