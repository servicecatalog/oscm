/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2009-02-24                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.vo;

import org.oscm.internal.vo.BaseVO;

/**
 * Represents a billing contact of an organization.
 * 
 */
public class VOBillingContact extends BaseVO {

    private static final long serialVersionUID = -8472582588793736813L;

    private String email;
    private String companyName;
    private String address;
    private boolean orgAddressUsed;
    private String id;

    /**
     * Retrieves the email address to which invoices are sent.
     * 
     * @return the email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email address to which invoices are sent.
     * 
     * @param email
     *            the email address
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Retrieves the name of the organization.
     * 
     * @return the organization name
     */
    public String getCompanyName() {
        return companyName;
    }

    /**
     * Sets the name of the organization.
     * 
     * @param companyName
     *            the organization name
     */
    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    /**
     * Retrieves the address of the organization.
     * 
     * @return the address
     */
    public String getAddress() {
        return address;
    }

    /**
     * Sets the address of the organization.
     * 
     * @param address
     *            the address
     */
    public void setAddress(String address) {
    	if (address != null && !address.trim().equals("")) {
    		this.address = address.replaceAll("\u200C", "\n");
    	} else {
    		this.address = address;
    	}
    }

    /**
     * Returns information on whether invoices are sent to the organization
     * address or to a user-defined address.
     * 
     * @return <code>true</code> if the organization address is used,
     *         <code>false</code> otherwise
     */
    public boolean isOrgAddressUsed() {
        return orgAddressUsed;
    }

    /**
     * Specifies whether invoices are sent to the organization address or to a
     * user-defined address.
     * 
     * @param orgAddressUsed
     *            <code>true</code> if the organization address is to be used,
     *            <code>false</code> otherwise
     */
    public void setOrgAddressUsed(boolean orgAddressUsed) {
        this.orgAddressUsed = orgAddressUsed;
    }

    /**
     * Specifies the identifier of the billing contact.
     * 
     * @param id
     *            the identifier
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the identifier of the billing contact.
     * 
     * @return the identifier
     */
    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VOBillingContact)) return false;

        VOBillingContact that = (VOBillingContact) o;

        if (orgAddressUsed != that.orgAddressUsed) return false;
        if (address != null ? !address.equals(that.address) : that.address != null) return false;
        if (companyName != null ? !companyName.equals(that.companyName) : that.companyName != null) return false;
        if (email != null ? !email.equals(that.email) : that.email != null) return false;
        if (!id.equals(that.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = email != null ? email.hashCode() : 0;
        result = 31 * result + (companyName != null ? companyName.hashCode() : 0);
        result = 31 * result + (address != null ? address.hashCode() : 0);
        result = 31 * result + (orgAddressUsed ? 1 : 0);
        result = 31 * result + id.hashCode();
        return result;
    }
}
