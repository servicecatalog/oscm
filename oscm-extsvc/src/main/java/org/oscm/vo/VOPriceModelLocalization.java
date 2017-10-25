/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2010-07-09                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Container for the localized texts of a price model.
 */
public class VOPriceModelLocalization implements Serializable {

    private static final long serialVersionUID = -2100805606029894462L;

    private List<VOLocalizedText> descriptions = new ArrayList<VOLocalizedText>();

    private List<VOLocalizedText> licenses = new ArrayList<VOLocalizedText>();

    /**
     * Returns the localized variants of the price model's description.
     * 
     * @return the descriptions for different locales
     */
    public List<VOLocalizedText> getDescriptions() {
        return descriptions;
    }

    /**
     * Sets the localized variants of the price model's description.
     * 
     * @param descriptions
     *            the descriptions for different locales
     */
    public void setDescriptions(List<VOLocalizedText> descriptions) {
        this.descriptions = descriptions;
    }

    /**
     * Sets the localized variants of the price model's license agreement.
     * 
     * @param licenses
     *            the license agreements for different locales
     */
    public void setLicenses(List<VOLocalizedText> licenses) {
        this.licenses = licenses;
    }

    /**
     * Returns the localized variants of the price model's license agreement.
     * 
     * @return the license agreements for different locales
     */
    public List<VOLocalizedText> getLicenses() {
        return licenses;
    }

}
