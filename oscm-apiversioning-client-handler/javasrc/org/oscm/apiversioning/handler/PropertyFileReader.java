/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: Jan 13, 2015                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.apiversioning.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Property file reader class responsible for getting properties from a
 * specified property file. The file should contains the properties the client
 * needs to access a web service.
 */
public class PropertyFileReader {
    private static Log logger = LogFactory.getLog(PropertyFileReader.class);
    /**
     * Get properties from specified file for accessing the target Web service.
     * 
     * @param fileName
     *            name of the specified property file.
     * @return Properties read from the specified file.
     */
    public static Properties getPropertiesFromFile(String fileName) {
        if (fileName == null) {
            return null;
        }

        final InputStream in = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(fileName);
        if (in == null) {
            return null;
        }

        Properties props = new Properties();
        try {
            props.load(in);
        } catch (IOException e) {
            logger.error("Error loading properties.", e);
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                logger.error("Error closing stream.", e);
            }
        }
        return props;
    }
}
