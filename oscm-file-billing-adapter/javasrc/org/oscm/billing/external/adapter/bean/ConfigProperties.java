/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 12.01.2015                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billing.external.adapter.bean;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.oscm.billing.external.exception.BillingException;

/**
 * Configuration properties of FileBillingAdapter
 */
public class ConfigProperties {
    Properties configProperties = null;
    String adapterId = null;

    private final static String PROPERTY_FILE = "billingApplication.properties";

    public ConfigProperties(String adapterId) {
        this.adapterId = adapterId;
    }

    /**
     * Get the config property specified by key
     */
    public String getConfigProperty(String key) throws BillingException {
        String value = (String) getConfigProperties().get(key);
        if (value == null || value.trim().length() == 0) {
            throw new BillingException("Configsetting: " + key + " is missing");
        }
        return value;
    }

    /**
     * Get the config properties
     */
    Properties getConfigProperties() throws BillingException {
        if (configProperties == null) {
            loadProperties();
        }
        return configProperties;
    }

    /**
     * Set the config properties
     */
    void setConfigProperties(Properties configProperties) {
        this.configProperties = configProperties;
    }

    /**
     * Load the config properties from PROPERTY_FILE
     */
    void loadProperties() throws BillingException {
        ClassLoader classLoader = this.getClass().getClassLoader();
        try (InputStream in = classLoader.getResourceAsStream(PROPERTY_FILE);) {
            if (in != null) {
                Properties props = new Properties();
                props.load(in);
                setConfigProperties(props);
            }
        } catch (IOException e) {
            setConfigProperties(null);
            throw new BillingException("Configsettings are missing");
        }
    }
}
