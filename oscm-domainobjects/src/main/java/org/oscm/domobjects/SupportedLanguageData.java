/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2013-11-6                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * Data container of SupportedLanguage
 * 
 * @author Zou
 * 
 */
@Embeddable
public class SupportedLanguageData extends DomainDataContainer {

    private static final long serialVersionUID = 1L;

    @Column(nullable = false, updatable = false)
    private String languageISOCode;

    private boolean activeStatus;

    private boolean defaultStatus;

    public String getLanguageISOCode() {
        return languageISOCode;
    }

    public void setLanguageISOCode(String languageISOCode) {
        this.languageISOCode = languageISOCode;
    }

    public boolean getActiveStatus() {
        return activeStatus;
    }

    public void setActiveStatus(boolean activeStatus) {
        this.activeStatus = activeStatus;
    }

    public boolean getDefaultStatus() {
        return defaultStatus;
    }

    public void setDefaultStatus(boolean defaultStatus) {
        this.defaultStatus = defaultStatus;
    }

}
