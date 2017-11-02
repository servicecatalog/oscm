/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.ui.model;

import org.oscm.internal.vo.VOOrganization;

/**
 * @author weiser
 * 
 */
public class CustomerPaymentTypes extends PaymentTypes {

    private final VOOrganization customer;

    public CustomerPaymentTypes(VOOrganization org) {
        customer = org;
    }

    public VOOrganization getCustomer() {
        return customer;
    }

    public String getOrganizationId() {
        return customer.getOrganizationId();
    }

    public String getName() {
        return customer.getName();
    }

    public String getAddress() {
        return customer.getAddress();
    }

    protected PaymentTypes newInstance() {
        return new CustomerPaymentTypes(getCustomer());
    }

}
