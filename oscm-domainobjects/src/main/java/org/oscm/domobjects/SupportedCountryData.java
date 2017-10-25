/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.domobjects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class SupportedCountryData extends DomainDataContainer {

    private static final long serialVersionUID = -1276282860062717819L;

    /**
     * The ISO code of the currency according to the ISO 3166 standard.
     */
    @Column(nullable = false)
    private String countryISOCode;

    /**
     * Returns the ISO 3166 country code
     * 
     * @return String
     */
    public String getCountryISOCode() {
        return countryISOCode;
    }

    /**
     * Sets the ISO 3166 country code.
     * 
     * @param countryISOCode
     *            ISO 3166 country code to be set
     */
    public void setCountryISOCode(String countryISOCode) {
        this.countryISOCode = countryISOCode;
    }

}
