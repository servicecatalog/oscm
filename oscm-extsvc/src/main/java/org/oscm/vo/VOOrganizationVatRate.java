/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2010-04-14                                               
 *                                                                              
 *******************************************************************************/

package org.oscm.vo;

/**
 * Represents a VAT rate defined for a specific organization.
 * 
 */
public class VOOrganizationVatRate extends VOVatRate {

    private static final long serialVersionUID = 7452412672504850066L;

    private VOOrganization organization;

    /**
     * Retrieves the organization for which the VAT rate is defined.
     * 
     * @return the organization
     * 
     */
    public VOOrganization getOrganization() {
        return organization;
    }

    /**
     * Sets the organization for which the VAT rate is to be defined.
     * 
     * @param organization
     *            the organization
     */
    public void setOrganization(VOOrganization organization) {
        this.organization = organization;
    }

}
