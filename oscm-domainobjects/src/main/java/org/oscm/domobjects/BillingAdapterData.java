/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                                                                                          
 *******************************************************************************/

package org.oscm.domobjects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * @author stavreva
 * 
 */
@Embeddable
public class BillingAdapterData extends DomainDataContainer {

    private static final long serialVersionUID = -3200195731711309039L;

    @Column(nullable = false)
    private String billingIdentifier;

    @Column(nullable = true)
    private String name;

    @Column(nullable = true)
    private String connectionProperties;

    @Column(nullable = false)
    private boolean defaultAdapter;

    public String getConnectionProperties() {
        return connectionProperties;
    }

    public void setConnectionProperties(String connectionProperties) {
        this.connectionProperties = connectionProperties;
    }

    public String getBillingIdentifier() {
        return billingIdentifier;
    }

    public void setBillingIdentifier(String billingIdentifier) {
        this.billingIdentifier = billingIdentifier;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isDefaultAdapter() {
        return defaultAdapter;
    }

    public void setDefaultAdapter(boolean defaultAdapter) {
        this.defaultAdapter = defaultAdapter;
    }

}
