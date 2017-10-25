/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 10.05.2010                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.dao.model;

/**
 * Data object to represent the organization address data for an organization.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class OrganizationAddressData {

    private String organizationId;
    private String address;
    private String organizationName;
    private String email;
    private String paymentTypeId;

    public OrganizationAddressData(String address, String organizationName,
            String email, String organizationId) {
        this.address = address;
        this.organizationName = organizationName;
        this.email = email;
        this.organizationId = organizationId;
    }

    /**
     * Returns the address of the organization.
     * 
     * @return The address.
     */
    public String getAddress() {
        return address;
    }

    /**
     * Returns the name of the organization.
     * 
     * @return The name.
     */
    public String getOrganizationName() {
        return organizationName;
    }

    /**
     * Returns the organization name and the address data as one address entry
     * to be used as contact information.
     * 
     * @return The complete address.
     */
    public String getCompleteAddress() {
        return organizationName + "\n" + address;
    }

    /**
     * Returns the email of the customer that should be used for sending bills.
     * 
     * @return The customer's email.
     */
    public String getEmail() {
        return email;
    }

    public void setPaymentTypeId(String paymentTypeId) {
        this.paymentTypeId = paymentTypeId;
    }

    public String getPaymentTypeId() {
        return paymentTypeId;
    }

    public String getOrganizationId() {
        return organizationId;
    }
}
