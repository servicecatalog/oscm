/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2012-6-11                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.classic.manageudas;

/**
 * @author yuyin
 * 
 */
public class UdaDefinitionDetails {
    /**
     * The identifier of the custom attribute; must be unique for the target
     * type.
     */
    private String udaId;

    /**
     * The default value for the custom attribute
     */
    private String defaultValue;

    /**
     * Determine the UDA visible or not.
     */
    private boolean userOption;

    /**
     * Determine the UDA value is mandatory or not.
     */
    private boolean mandatory;

    /**
     * the numeric key for the UDA
     */
    private long key;

    /**
     * The version of the UDA
     */
    private int version;

    /**
     * Determine the UDA value is encrypted or not.
     */
    private boolean encrypted;

    /**
     * The custom user attribute name.
     */
    private String language;

    private String name;

    /**
     * Determine the controller the UDA is meant for.
     */
    private String controllerId;

    /**
     * @return the udaId
     */
    public String getUdaId() {
        return udaId;
    }

    /**
     * @param udaId
     *            the udaId to set
     */
    public void setUdaId(String udaId) {
        this.udaId = udaId;
    }

    /**
     * @return the defaultValue
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * @param defaultValue
     *            the defaultValue to set
     */
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * @return the userOption
     */
    public boolean isUserOption() {
        return userOption;
    }

    /**
     * @param userOption
     *            the userOption to set
     */
    public void setUserOption(boolean userOption) {
        this.userOption = userOption;
        this.mandatory = mandatory && userOption;
    }

    /**
     * @return the mandatory
     */
    public boolean isMandatory() {
        return mandatory;
    }

    /**
     * @param mandatory
     *            the mandatory to set
     */
    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    /**
     * @return the key
     */
    public long getKey() {
        return key;
    }

    /**
     * @param key
     *            the key to set
     */
    public void setKey(long key) {
        this.key = key;
    }

    /**
     * @return the version
     */
    public int getVersion() {
        return version;
    }

    /**
     * @param version
     *            the version to set
     */
    public void setVersion(int version) {
        this.version = version;
    }

    public boolean isEncrypted() {
        return encrypted;
    }

    public void setEncrypted(boolean encrypted) {
        this.encrypted = encrypted;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getControllerId() {
        return controllerId;
    }

    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }
}
