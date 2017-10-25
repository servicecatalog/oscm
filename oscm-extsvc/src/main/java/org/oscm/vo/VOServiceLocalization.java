/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2010-07-29                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Container for the localized texts of a service.
 * 
 */
public class VOServiceLocalization implements Serializable {

    private static final long serialVersionUID = -2100805606029894462L;

    private List<VOLocalizedText> names = new ArrayList<VOLocalizedText>();

    private List<VOLocalizedText> descriptions = new ArrayList<VOLocalizedText>();

    private List<VOLocalizedText> shortDescriptions = new ArrayList<VOLocalizedText>();
    private List<VOLocalizedText> customTabNames = new ArrayList<>();

    /**
     * Returns the localized variants of the service's name.
     * 
     * @return the names for different locales
     */
    public List<VOLocalizedText> getNames() {
        return names;
    }

    /**
     * Sets the localized variants of the service's name.
     * 
     * @param names
     *            the names for different locales
     */
    public void setNames(List<VOLocalizedText> names) {
        this.names = names;
    }

    /**
     * Returns the localized variants of the service's description.
     * 
     * @return the descriptions for different locales
     */
    public List<VOLocalizedText> getDescriptions() {
        return descriptions;
    }

    /**
     * Sets the localized variants of the service's description.
     * 
     * @param descriptions
     *            the descriptions for different locales
     */
    public void setDescriptions(List<VOLocalizedText> descriptions) {
        this.descriptions = descriptions;
    }

    /**
     * Returns the localized variants of the service's short description.
     * 
     * @return the short descriptions for different locales
     */
    public List<VOLocalizedText> getShortDescriptions() {
        return shortDescriptions;
    }

    /**
     * Sets the localized variants of the service's short description.
     * 
     * @param shortDescriptions
     *            the short descriptions for different locales
     */
    public void setShortDescriptions(List<VOLocalizedText> shortDescriptions) {
        this.shortDescriptions = shortDescriptions;
    }

    public void setCustomTabNames(List<VOLocalizedText> customTabNames) {
        this.customTabNames = customTabNames;
    }

    public List<VOLocalizedText> getCustomTabNames() {
        return customTabNames;
    }
}
