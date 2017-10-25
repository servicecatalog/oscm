/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.domobjects;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

/**
 * The class <code>OrganizationToCountry</code> stores a country code and the
 * organization that supports this code.
 * 
 * @author cheld, pock
 */
@Entity
public class OrganizationToCountry extends DomainObjectWithEmptyDataContainer {

    private static final long serialVersionUID = 4336379997821816252L;

    /**
     * The organization that supports this country code.
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Organization organization;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private SupportedCountry supportedCountry;

    /**
     * Default constructor
     */
    public OrganizationToCountry() {
        super();
    }

    /**
     * Setter for organization.
     * 
     * @param organization
     */
    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    /**
     * Getter for organization.
     * 
     * @return Organization.
     */
    public Organization getOrganization() {
        return organization;
    }

    public void setSupportedCountry(SupportedCountry supportedCountry) {
        this.supportedCountry = supportedCountry;
    }

    public SupportedCountry getSupportedCountry() {
        return supportedCountry;
    }

    /**
     * Returns the country code defined in ISO 3166.
     * 
     * @return String
     */
    public String getCode() {
        return getSupportedCountry().getCountryISOCode();
    }

}
