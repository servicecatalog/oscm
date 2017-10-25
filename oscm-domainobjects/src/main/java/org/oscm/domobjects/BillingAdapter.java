/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                                                                                                                                                 
 *******************************************************************************/
package org.oscm.domobjects;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Formula;

import org.oscm.domobjects.annotations.BusinessKey;

/**
 * Holds all information for the registered billing adapter.
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {
        "billingIdentifier" }) )
@NamedQueries({
        @NamedQuery(name = "BillingAdapter.findByBusinessKey", query = "select ba from BillingAdapter ba where ba.dataContainer.billingIdentifier=:billingIdentifier"),
        @NamedQuery(name = "BillingAdapter.getAll", query = "select ba from BillingAdapter ba ORDER BY ba.key ASC"),
        @NamedQuery(name = "BillingAdapter.getDefaultAdapter", query = "select ba from BillingAdapter ba where ba.dataContainer.defaultAdapter=TRUE"),
        @NamedQuery(name = "BillingAdapter.getDefaultAdapterIdentifier", query = "select ba.dataContainer.billingIdentifier from BillingAdapter ba where ba.dataContainer.defaultAdapter=TRUE"),
        @NamedQuery(name = "BillingAdapter.isActive", query = "select tp.dataContainer.billingIdentifier from TechnicalProduct tp where tp.dataContainer.billingIdentifier=:billingIdentifier") })
@BusinessKey(attributes = { "billingIdentifier" })
public class BillingAdapter
        extends DomainObjectWithVersioning<BillingAdapterData> {

    private static final long serialVersionUID = 2943300954910863450L;
    @Formula("(exists(select * from technicalproduct as tp where tp.billingIdentifier=billingIdentifier))")
    private boolean active;

    public BillingAdapter() {
        super();
        dataContainer = new BillingAdapterData();
    }

    public String getBillingIdentifier() {
        return dataContainer.getBillingIdentifier();
    }

    public void setBillingIdentifier(String billingIdentifier) {
        dataContainer.setBillingIdentifier(billingIdentifier);
    }

    public String getName() {
        return dataContainer.getName();
    }

    public void setName(String name) {
        dataContainer.setName(name);
    }

    public String getConnectionProperties() {
        return dataContainer.getConnectionProperties();
    }

    public void setConnectionProperties(String connectionProperties) {
        dataContainer.setConnectionProperties(connectionProperties);
    }

    public boolean isDefaultAdapter() {
        return dataContainer.isDefaultAdapter();
    }

    public void setDefaultAdapter(boolean isDefaultAdapter) {
        dataContainer.setDefaultAdapter(isDefaultAdapter);
    }

    public boolean isActive() {
        return active;
    }
}
