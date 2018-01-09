/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *       
 *  Creation Date: 2012-09-27
 *                                                                              
 *******************************************************************************/

package org.oscm.app.v2_0.exceptions;

import java.util.List;

import org.oscm.app.v2_0.data.LocalizedText;

/**
 * Exception thrown when loading of a controller configuration fails due to
 * missing or invalid configuration settings.
 */
public class ConfigurationException extends APPlatformException {

    private static final long serialVersionUID = 2804755466599378233L;

    private String affectedKey;

    /**
     * Constructs a new exception with the specified detail message. The cause
     * is not initialized.
     * 
     * @param message
     *            the detail message
     */
    public ConfigurationException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified localized text messages.
     * The cause is not initialized.
     * 
     * @param messages
     *            the localized text messages
     */
    public ConfigurationException(List<LocalizedText> messages) {
        super(messages);
    }

    /**
     * Constructs a new exception with the specified detail message and
     * parameter.
     * 
     * @param message
     *            the detail message
     * @param affectedConfigurationKey
     *            the key of the missing or invalid configuration setting
     */
    public ConfigurationException(String message,
            String affectedConfigurationKey) {
        this(message);
        affectedKey = affectedConfigurationKey;
    }

    /**
     * Returns the key of the configuration setting which is missing or invalid
     * 
     * @return the key
     */
    public String getAffectedKey() {
        return affectedKey;
    }
}
