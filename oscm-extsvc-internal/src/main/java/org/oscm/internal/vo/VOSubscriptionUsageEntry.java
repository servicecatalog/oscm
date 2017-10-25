/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *  Creation Date: 25.04.17 08:30
 *
 ******************************************************************************/

package org.oscm.internal.vo;

import java.io.Serializable;

/**
 * Authored by dawidch
 */
public class VOSubscriptionUsageEntry implements Serializable {

    private String customerOrgId;
    private String customerOrgName;
    private String subscriptionName;
    private String marketableServiceName;
    private String technicalServiceName;
    private String supplierOrganizationId;
    private String supplierOrganizationName;
    private String numberOfusers;
    private String numberOfVMs;

    public VOSubscriptionUsageEntry(String customerOrgId, String customerOrgName, String subscriptionName, String marketableServiceName, String technicalServiceName, String supplierOrganizationId, String supplierOrganizationName, String numberOfusers, String numberOfVMs) {
        this.customerOrgId = customerOrgId;
        this.customerOrgName = customerOrgName;
        this.subscriptionName = subscriptionName;
        this.marketableServiceName = marketableServiceName;
        this.technicalServiceName = technicalServiceName;
        this.supplierOrganizationId = supplierOrganizationId;
        this.supplierOrganizationName = supplierOrganizationName;
        this.numberOfusers = numberOfusers;
        this.numberOfVMs = numberOfVMs;
    }

    public String getCustomerOrgId() {
        return customerOrgId;
    }

    public String getCustomerOrgName() {
        return customerOrgName;
    }

    public String getSubscriptionName() {
        return subscriptionName;
    }

    public String getMarketableServiceName() {
        return marketableServiceName;
    }

    public String getTechnicalServiceName() {
        return technicalServiceName;
    }

    public String getSupplierOrganizationId() {
        return supplierOrganizationId;
    }

    public String getSupplierOrganizationName() {
        return supplierOrganizationName;
    }

    public String getNumberOfusers() {
        return numberOfusers;
    }

    public String getNumberOfVMs() {
        return numberOfVMs;
    }
}
