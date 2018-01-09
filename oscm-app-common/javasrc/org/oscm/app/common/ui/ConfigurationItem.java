/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 27.05.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.common.ui;

/**
 * Value object for configuration settings.
 */
public class ConfigurationItem {
    // Local constants
    private final static String PASSWORD_SUFFIX = "_PWD";
    private final static String PASSWORD_SUFFIX_PASS = "_PASS";

    private boolean isDirty;
    private boolean readOnly;
    private String displayName;
    private String key;
    private String value;
    private String tooltip;

    public ConfigurationItem(String key, String value) {
        this.key = key;
        this.value = (value != null) ? value : "";
        setDirty(false);
    }

    public boolean isDirty() {
        return isDirty;
    }

    public void setDirty(boolean isDirty) {
        this.isDirty = isDirty;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean value) {
        readOnly = value;
    }

    public boolean isPasswordField() {
        return key.endsWith(PASSWORD_SUFFIX)
                || key.endsWith(PASSWORD_SUFFIX_PASS);
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
        this.isDirty = true;
    }

    public String getDisplayName() {
        return (displayName != null) ? displayName : getKey();
    }

    public void setDisplayName(String value) {
        this.displayName = value;
    }

    public String getTooltip() {
        return (tooltip != null) ? tooltip : "";
    }

    public void setTooltip(String value) {
        this.tooltip = value;
    }

}
