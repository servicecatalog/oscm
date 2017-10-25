/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 9.01.2015                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.billingadapter;

import java.io.Serializable;

/**
 * @author farmaki
 * 
 */
public class ConnectionPropertyItem implements Serializable, Comparable<ConnectionPropertyItem> {

    private static final long serialVersionUID = 3436197273846120192L;

    private String key;
    private String value;

    // TODO move to global position
    private static final String JNDI_NAME = "JNDI_NAME";

    public ConnectionPropertyItem(String key, String value) {
        this.key = key;
        this.value = (value != null) ? value : "";
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setValue(String value) {
        this.value = value;

    }

    public String getDisplayName() {
        if (getKey() != null && getKey().trim().length() != 0) {
            return key;
        } else {
            return "";
        }
    }

    public void setDisplayName(String displayName) {
        setKey(displayName);
    }

    public boolean isJndiName() {
        if (getKey() != null && getKey().trim().length() != 0) {
            return JNDI_NAME.equals(getKey());
        }

        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ConnectionPropertyItem that = (ConnectionPropertyItem) o;

        return !(key != null ? !key.equals(that.key) : that.key != null);

    }

    @Override
    public int hashCode() {
        return key != null ? key.hashCode() : 0;
    }

    @Override
    public int compareTo(ConnectionPropertyItem o2) {
        if (o2 == null) return -1;
        if (this.getKey() == null && o2.getKey() == null) return 0;
        if (this.getKey() == null && o2.getKey() != null) return 1;
        if (this.getKey() != null && o2.getKey() == null) return -1;
        if (this.isJndiName() && !o2.isJndiName()) return -1;
        if (!this.isJndiName() && o2.isJndiName()) return 1;
        if (this.isJndiName() && o2.isJndiName()) return 0;
        return this.getKey().compareTo(o2.getKey());
    }
}
