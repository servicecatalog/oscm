/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2010-05-22                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.vo;

/**
 * Represents a country-specific VAT rate.
 * 
 */
public class VOCountryVatRate extends VOVatRate {

    private static final long serialVersionUID = -3693411330647037996L;

    /**
     * The country code.
     */
    private String country;

    /**
     * Sets the country code for this VAT rate.
     * 
     * @param country
     *            the country code in ISO 3166 format
     */
    public void setCountry(String country) {
        this.country = country;
    }

    /**
     * Returns the country code of this VAT rate.
     * 
     * @return the country code in ISO 3166 format
     */
    public String getCountry() {
        return country;
    }

}
