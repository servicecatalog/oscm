/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 12.02.2014                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.integrationhelper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Reader property file to get properties from specified file for accessing the
 * target Web service.
 */
public class PropertyFileReader {
    private static Log logger = LogFactory.getLog(PropertyFileReader.class);

    /**
     * Get properties from specified file for accessing the target Web service.
     * 
     * @param fileName
     *            name of the specified file.
     * @return Properties that read from specified file.
     */
    public static Properties getPropertiesFromFile(String fileName) {
        logger.debug("Try to read file: " + fileName);
        if (fileName == null) {
            logger.debug("Error: file name null");
            return null;
        }

        final InputStream in = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(fileName);
        if (in == null) {
            logger.debug("Error: input stream null");
            return null;
        }

        Properties props = new Properties();
        try {
            props.load(in);
        } catch (IOException e) {
            logger.error("Error: Reading properties failed!", e);
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                logger.error("Error: Can not close InputStream!", e);
                e.printStackTrace();
            }
        }
        logger.debug("Properties read.");
        return props;
    }
}
