/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.converter;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.internal.types.exception.SaaSSystemException;

public class ResourceLoader {

    private static final Log4jLogger LOGGER = LoggerFactory
            .getLogger(ResourceLoader.class);

    /**
     * Returns an input stream for reading the resource with the given name from
     * the classloader of the given class.
     * 
     * @param clazz
     *            the class to use its classloader to find the resource
     * @param resource
     *            the name of the resource
     * @return the input stream for reading the resource
     * @throws SaaSSystemException
     *             if no classloader is found for the given class
     */
    public static InputStream getResourceAsStream(final Class<?> clazz,
            final String resource) {

        ClassLoader classLoader = getClassLoader(clazz);
        InputStream inputStream = getResourceAsStream(classLoader, resource);
        return inputStream;
    }

    private static ClassLoader getClassLoader(final Class<?> clazz) {
        ClassLoader classLoader = clazz.getClassLoader();
        assertClassLoader(clazz, classLoader);
        return classLoader;
    }

    private static void assertClassLoader(final Class<?> clazz,
            ClassLoader classLoader) {
        if (classLoader == null) {
            SaaSSystemException e = new SaaSSystemException(
                    "No classloader found for class " + clazz);
            LOGGER.logError(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.ERROR_CLASS_LOADER_NOT_FOUND,
                    clazz.getName());
            throw e;
        }
    }

    private static InputStream getResourceAsStream(
            final ClassLoader classLoader, final String resource) {

        InputStream inputStream = classLoader.getResourceAsStream(resource);
        assertInputStream(resource, inputStream);
        return inputStream;
    }

    private static void assertInputStream(final String resource,
            final InputStream inputStream) {
        if (inputStream == null) {
            SaaSSystemException e = new SaaSSystemException(
                    "Unable to find resource " + resource);
            LOGGER.logError(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.ERROR_FIND_RESOURCE_FAILED, resource);
            throw e;
        }
    }

    public static byte[] load(final Class<?> clazz, final String resource) {
        return load(getResourceAsStream(clazz, resource));
    }

    public static byte[] load(InputStream inputStream) {
        try {
            byte[] bytes = new byte[inputStream.available()];
            int len = inputStream.read(bytes);
            if (len > -1) {
                return bytes;
            }
            return new byte[0];
        } catch (IOException e) {
            SaaSSystemException se = new SaaSSystemException(
                    "Unable to read input stream: " + e.getMessage(), e);
            LOGGER.logError(Log4jLogger.SYSTEM_LOG, se,
                    LogMessageIdentifier.ERROR_FIND_INPUT_STREAM_FAILED);
            throw se;
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                SaaSSystemException se = new SaaSSystemException(
                        "Unable to close input stream: " + e.getMessage(), e);
                LOGGER.logError(Log4jLogger.SYSTEM_LOG, se,
                        LogMessageIdentifier.ERROR_CLOSE_INPUT_STREAM_FAILED);
                throw se;
            }
        }
    }

    /**
     * 
     * @param clazz
     *            The class to be used to load the resource's URL object
     * @param resourceName
     *            The name of the resource
     * @return an URL object of the provided resource string, or null if the
     *         resource could not be found
     */
    public static URL getResource(final Class<?> clazz, final String resourceName) {
        ClassLoader classLoader = getClassLoader(clazz);
        URL resourceUrl = classLoader.getResource(resourceName);
        return resourceUrl;
    }

}
