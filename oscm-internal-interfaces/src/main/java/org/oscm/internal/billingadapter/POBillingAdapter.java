/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 11.11.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.billingadapter;

import java.util.Set;

/**
 * Presentation object for the billing adapter with all information.
 * 
 * @author stavreva
 * 
 */
public class POBillingAdapter extends POBaseBillingAdapter {

    private static final long serialVersionUID = 1810616235653834269L;

    private Set<ConnectionPropertyItem> connectionProperties;

    private boolean defaultAdapter;

    private boolean active;

    private boolean nativeBilling;

    public Set<ConnectionPropertyItem> getConnectionProperties() {
        return connectionProperties;
    }

    public void setConnectionProperties(
            Set<ConnectionPropertyItem> connectionProperties) {
        this.connectionProperties = connectionProperties;
    }

    public boolean isDefaultAdapter() {
        return defaultAdapter;
    }

    public void setDefaultAdapter(boolean defaultAdapter) {
        this.defaultAdapter = defaultAdapter;
    }

    public boolean isNativeBilling() {
        return nativeBilling;
    }

    public void setNativeBilling(boolean nativeBilling) {
        this.nativeBilling = nativeBilling;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
