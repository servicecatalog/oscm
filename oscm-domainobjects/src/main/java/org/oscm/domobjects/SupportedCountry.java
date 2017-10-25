/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.domobjects;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import org.oscm.domobjects.annotations.BusinessKey;

@BusinessKey(attributes = { "countryISOCode" })
@NamedQueries({
        @NamedQuery(name = "SupportedCountry.findByBusinessKey", query = "SELECT sc FROM SupportedCountry sc WHERE sc.dataContainer.countryISOCode = :countryISOCode"),
        @NamedQuery(name = "SupportedCountry.getAllCountryCodes", query = "SELECT sc.dataContainer.countryISOCode FROM SupportedCountry sc") })
@Entity
public class SupportedCountry extends
        DomainObjectWithVersioning<SupportedCountryData> {

    private static final long serialVersionUID = 846490744472571415L;

    /**
     * Constructs a new SupportedCountry domain object for the given country
     * code defined in ISO 3166.
     * 
     * @param countryCode
     */
    public SupportedCountry(String countryCode) {
        this();
        setCountryISOCode(countryCode);
    }

    /**
     * Default constructor.
     */
    public SupportedCountry() {
        this.dataContainer = new SupportedCountryData();
    }

    /**
     * Refer to {@link SupportedCountryData#getCountryISOCode()}
     */
    public String getCountryISOCode() {
        return dataContainer.getCountryISOCode();
    }

    /**
     * Refer to {@link SupportedCountryData#setCountryISOCode()}
     */
    public void setCountryISOCode(String countryISOCode) {
        dataContainer.setCountryISOCode(countryISOCode);
    }

    String toStringAttributes() {
        return String.format(", countryISOCode='%s'", getCountryISOCode());
    }

}
