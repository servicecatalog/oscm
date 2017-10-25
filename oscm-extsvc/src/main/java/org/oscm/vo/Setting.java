/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2012-01-17                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.vo;

import java.io.Serializable;

/**
 * Represents a property setting, mapping a <code>String</code> value to a
 * <code>String</code> key.
 */
public class Setting implements Serializable {

    private static final long serialVersionUID = -1771654544229001811L;
    private String key;
    private String value;

    /**
     * Default constructor.
     */
    public Setting() {
    }

    /**
     * Constructs a setting with the given key and value.
     * 
     * @param key
     *            the key of the setting
     * @param value
     *            the value of the setting
     */
    public Setting(String key, String value) {
        this.key = key;
        this.value = value;
    }

    /**
     * Returns the key of the setting.
     * 
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * Sets the key of the setting.
     * 
     * @param key
     *            the key
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * Returns the value of the setting.
     * 
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value of the setting.
     * 
     * @param value
     *            the value
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Returns a hash code value for the object.
     * 
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return key == null ? 0 : key.hashCode();
    }

    /**
     * Indicates whether the given object is equal to this one.
     * 
     * @param obj
     *            the reference object with which to compare
     * @return <code>true</code> if this object is the same as the
     *         <code>obj</code> argument; <code>false</code> otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        // returns also false if obj is null
        if (!(obj instanceof Setting)) {
            return false;
        }

        Setting setting = (Setting) obj;
        return key.equals(setting.key);
    }

}
