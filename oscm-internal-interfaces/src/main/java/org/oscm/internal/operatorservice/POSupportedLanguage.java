/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2013-11-7
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.operatorservice;

import org.oscm.internal.base.BasePO;

/**
 * Represents SupportedLanguage for manage language.
 * 
 * @author chenmin
 */
public class POSupportedLanguage extends BasePO {

    private static final long serialVersionUID = 2103823966943099962L;

    /**
     * The identifier.
     */
    private String languageISOCode;

    /**
     * language name to represent
     */
    private String languageName;

    /**
     * active Status for language, if language is active, the language could be
     * used for user
     */
    private boolean active = false;

    /**
     * default Status for language, if language is default, the language will be
     * used when the selected locale is invalid.
     */
    private boolean defaultLanguageStatus = false;

    public String getLanguageISOCode() {
        return languageISOCode;
    }

    public void setLanguageISOCode(String languageISOCode) {
        this.languageISOCode = languageISOCode;
    }

    public String getLanguageName() {
        return languageName;
    }

    public void setLanguageName(String languageName) {
        this.languageName = languageName;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isDefaultLanguageStatus() {
        return defaultLanguageStatus;
    }

    public void setDefaultLanguageStatus(boolean defaultLanguageStatus) {
        this.defaultLanguageStatus = defaultLanguageStatus;
    }

}
