/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 15.10.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.classic.deletecustomerpricemodel;

import java.io.Serializable;

/**
 * @author weiser
 * 
 */
public class CustomerService implements Serializable {

    private static final long serialVersionUID = 9046640322623024771L;

    private boolean selected;
    private long key;
    private int version;
    private String id;

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public long getKey() {
        return key;
    }

    public void setKey(long key) {
        this.key = key;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
