/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: Jan 13, 2015                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.apiversioning.handler;

import java.util.Properties;

/**
 * This class is responsible for getting the service version information.
 */
public class ApiVersionInfo {

    private Properties props = null;

    public ApiVersionInfo(String fileName) {
        if (fileName == null || fileName.trim().length() == 0) {
            return;
        }

        props = PropertyFileReader.getPropertiesFromFile(fileName);
        if (props == null) {
            return;
        }
    }

    String getProperty(String property) {
        String value = props.getProperty(property).trim();
        return value;
    }
}
