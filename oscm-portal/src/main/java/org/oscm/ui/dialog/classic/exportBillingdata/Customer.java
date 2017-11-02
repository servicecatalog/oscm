/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.ui.dialog.classic.exportBillingdata;

import java.io.Serializable;

import org.oscm.internal.billingdataexport.POOrganization;

public class Customer implements Serializable {

    private static final long serialVersionUID = 975772576073977003L;

    POOrganization customer;
    boolean selected = false;

    Customer(POOrganization customer) {
        super();
        this.customer = customer;
    }

    public Customer() {
    }

    public POOrganization getCustomer() {
        return customer;
    }

    public void setCustomer(POOrganization customer) {
        this.customer = customer;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getOrganizationId() {
        return customer.getOrganizationId();
    }

    public void setOrganizationId(String organizationId) {
        customer.setOrganizationId(organizationId);
    }

    public String getOrganizationName() {
        return customer.getOrganizationName();
    }

    public void setOrganizationName(String organizationName) {
        customer.setOrganizationName(organizationName);
    }

    public String getOrganizationAddress() {
        return customer.getOrganizationAddress();
    }

    public void setOrganizationAddress(String organizationAddress) {
        customer.setOrganizationAddress(organizationAddress);
    }

}
