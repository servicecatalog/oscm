/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2011-10-07                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.psp.data;

import java.io.Serializable;

/**
 * Provides the key and value of a configuration setting for a payment service
 * provider (PSP).
 */
public class Property implements Serializable {
    private static final long serialVersionUID = 8183236536813648423L;
    private String key;
    private String value;

    /**
     * Default constructor.
     */
    public Property() {
        super();
    }

    /**
     * Constructs a configuration setting with the given key and value.
     * 
     * @param key
     *            the key of the configuration setting
     * @param value
     *            the value of the configuration setting
     */
    public Property(String key, String value) {
        super();
        this.key = key;
        this.value = value;
    }

    /**
     * Returns the key of the configuration setting.
     * 
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * Sets the key of the configuration setting.
     * 
     * @param key
     *            the key
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * Returns the value of the configuration setting.
     * 
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value of the configuration setting.
     * 
     * @param value
     *            the value
     */
    public void setValue(String value) {
        this.value = value;
    }

}
