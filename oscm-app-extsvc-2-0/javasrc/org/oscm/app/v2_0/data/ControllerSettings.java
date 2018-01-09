/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 2014-03-19
 *                                                                              
 *******************************************************************************/

package org.oscm.app.v2_0.data;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Provides the settings and values defined for an application-specific service
 * controller.
 * 
 */
public class ControllerSettings implements Serializable {

    public ControllerSettings(HashMap<String, Setting> configSettings) {
        this.configSettings = configSettings;
    }

    private static final long serialVersionUID = -812011951662194104L;

    private HashMap<String, Setting> configSettings;

    /**
     * Returns the user ID and password used for authentication of the service
     * controller.
     * 
     * @return the authentication data
     */
    public PasswordAuthentication getAuthentication() {
        return authentication;
    }

    /**
     * Sets the user ID and password to be used for authentication of the
     * service controller. The user must have the technology manager role and
     * belong to the organization which is responsible for the service
     * controller.
     * 
     * @param authentication
     *            the authentication data
     */
    public void setAuthentication(PasswordAuthentication authentication) {
        this.authentication = authentication;
    }

    private PasswordAuthentication authentication;

    /**
     * Returns a list of the controller configuration settings.
     * 
     * @return the configuration settings, consisting of a key and a value each
     */
    public HashMap<String, Setting> getConfigSettings() {
        return configSettings;
    }

    /**
     * Sets the controller configuration settings.
     * 
     * @param configSettings
     *            the configuration settings, consisting of a key and a value
     *            each
     */
    public void setConfigSettings(HashMap<String, Setting> configSettings) {
        this.configSettings = configSettings;
    }

}
