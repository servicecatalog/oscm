/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: Nov 9, 2016                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.v2_0.data;

import java.io.Serializable;

/**
 * Structure class for controller settings, instance parameters, instance
 * attributes and customer attributes. The key is used as an identifier for the
 * setting. If the setting's value represents a password or some other sensitive
 * information it can be marked for encryption with the encryption flag.
 */
public class Setting implements Serializable {

    private static final long serialVersionUID = 1505614365313731825L;

    /**
     * The setting's key, used to identify the setting.
     */
    private String key;

    /**
     * The setting's value.
     */
    private String value;

    /**
     * Encryption flag, the value should be encrypted in log files, databases
     * and GUI.
     */
    private boolean encrypted;

    /**
     * Controller identifier.
     */
    private String controllerId;

    /**
     * Constructs a Setting object with all properties specified.
     * 
     * @param key
     *            setting's key
     * @param value
     *            setting's value
     * @param encrypted
     *            encryption flag
     * @param controllerId
     *            controller identifier
     */
    public Setting(String key, String value, boolean encrypted,
            String controllerId) {
        this.key = key;
        this.value = value;
        this.encrypted = encrypted;
        this.controllerId = controllerId;
    }

    /**
     * Constructs a Setting object with no controller specified.
     * 
     * @param key
     *            setting's key
     * @param value
     *            setting's value
     * @param encrypted
     *            encryption flag
     */
    public Setting(String key, String value, boolean encrypted) {
        this(key, value, encrypted, null);
    }

    /**
     * Constructs a Setting object, for non-encrypted values.
     * 
     * @param key
     *            setting's key
     * @param value
     *            setting's value
     */
    public Setting(String key, String value) {
        this(key, value, false);
    }

    /**
     * Returns the setting's key.
     * 
     * @return String representing the setting's key.
     */
    public String getKey() {
        return key;
    }

    /**
     * Sets the setting's key, used as identifier for the setting.
     * 
     * @param key
     *            the setting's key
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * Returns the setting's value.
     * 
     * @return setting's value
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the setting's value
     * 
     * @param value
     *            String representing the setting's value
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Returns the boolean value for the encryption flag.
     * 
     * @return false if the setting's value should not be encrypted, true if the
     *         setting's value should be encrypted.
     */
    public boolean isEncrypted() {
        return encrypted;
    }

    /**
     * Sets the boolean value for the encryption flag.
     * 
     * @param encrypted
     *            boolean value - true if the setting's value should be
     *            encrypted, false if the setting's value should not be
     *            encrypted.
     */
    public void setEncrypted(boolean encrypted) {
        this.encrypted = encrypted;
    }

    /**
     * Returns the controller identifier to which the setting belongs.
     * 
     * @return controller identifier
     */
    public String getControllerId() {
        return controllerId;
    }

    /**
     * Sets the controller identifier to which the setting belongs.
     * 
     * @param controllerId
     *            controller identifier
     */
    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }
}
