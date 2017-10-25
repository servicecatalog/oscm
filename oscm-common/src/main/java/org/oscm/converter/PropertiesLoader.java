/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.converter;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.internal.types.exception.SaaSSystemException;

/**
 * Utility class to populate Properties objects from input streams.
 * 
 * @author hoffmann
 */
public class PropertiesLoader {

    private static final Log4jLogger LOGGER = LoggerFactory
            .getLogger(PropertiesLoader.class);

    /**
     * Reads the properties from the given stream. The stream is closed in any
     * case.
     * 
     * @param in
     *            stream to read properties from
     * @return properties object
     */
    public static Properties loadProperties(final InputStream in) {
        final Properties properties = new Properties();
        try {
            try {
                properties.load(in);
            } finally {
                in.close();
            }
        } catch (IOException e) {
            LOGGER.logError(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.ERROR_READING_PROPERTIES);
        }
        return properties;
    }

    /**
     * Loads the resource with the given name from the classloader of the given
     * class.
     * 
     * @param clazz
     *            class loader to use
     * @param resource
     *            resource name
     * @return properties object
     */
    public static Properties load(final Class<?> clazz, final String resource) {
        Properties properties = new Properties();
        InputStream inputStream = null;
        try {
            inputStream = ResourceLoader.getResourceAsStream(clazz, resource);
            properties = loadProperties(inputStream);
        } catch (SaaSSystemException e) {
            // ignore
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    // ignore, wanted to close anyway
                }
            }

        }
        return properties;
    }

    /**
     * Loads the resource with the given name from the classloader of the given
     * class.
     * 
     * @param clazz
     *            class loader to use
     * @param resource
     *            resource name
     * @return ResourceBundle object
     * @throws IOException
     */
    public static ResourceBundle loadToBundle(final Class<?> clazz,
            final String resource) throws IOException {
        PropertyResourceBundle properties = null;
        InputStream in = null;
        try {
            in = ResourceLoader.getResourceAsStream(clazz, resource);
            properties = new PropertyResourceBundle(in);
        } finally {
            if (in != null) {
                in.close();
            }
        }
        return properties;
    }
}
