/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2010-04-14                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.vo;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a payment configuration for an organization.
 */
public class VOOrganizationPaymentConfiguration implements Serializable {

    private static final long serialVersionUID = -6819992548846002557L;

    private VOOrganization organization;
    private Set<VOPaymentType> enabledPaymentTypes = new HashSet<VOPaymentType>();

    /**
     * Retrieves the organization for which the payment configuration is
     * defined.
     * 
     * @return the organization
     * 
     */
    public VOOrganization getOrganization() {
        return organization;
    }

    /**
     * Sets the organization for which the payment configuration is to be
     * defined.
     * 
     * @param organization
     *            the organization
     */
    public void setOrganization(VOOrganization organization) {
        this.organization = organization;
    }

    /**
     * Sets the payment types that are to be available for the organization.
     * 
     * @param enabledPaymentTypes
     *            the payment types
     */
    public void setEnabledPaymentTypes(Set<VOPaymentType> enabledPaymentTypes) {
        this.enabledPaymentTypes = enabledPaymentTypes;
    }

    /**
     * Retrieves the payment types that can be used by the organization.
     * 
     * @return the payment types
     */
    public Set<VOPaymentType> getEnabledPaymentTypes() {
        return enabledPaymentTypes;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }

        VOOrganizationPaymentConfiguration pType = (VOOrganizationPaymentConfiguration) obj;
        return this.getOrganization().getOrganizationId()
                .equals(pType.getOrganization().getOrganizationId());
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
