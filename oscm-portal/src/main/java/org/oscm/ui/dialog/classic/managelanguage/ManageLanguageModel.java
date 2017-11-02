/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2013-11-6                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.classic.managelanguage;

import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.oscm.ui.beans.BaseModel;
import org.oscm.internal.operatorservice.POLocalizedData;
import org.oscm.internal.operatorservice.POSupportedLanguage;

/**
 * @author zou
 * 
 */
@ViewScoped
@ManagedBean(name="manageLanguageModel")
public class ManageLanguageModel extends BaseModel {

    private static final long serialVersionUID = -8163458290982112372L;

    private POLocalizedData localizedData;

    private boolean initialized;

    // store the language list showing on page
    private List<POSupportedLanguage> languages;

    // the selected Language on page
    private String selectedLanguageCode;

    // the default Language
    private String defaultLanguageCode;

    // for the new language code input text
    private String newISOCode;

    public ManageLanguageModel() {
        super();
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    public String getDefaultLanguageCode() {
        return defaultLanguageCode;
    }

    public void setDefaultLanguageCode() {
        for (POSupportedLanguage poSupportedLanguage : languages) {
            if (poSupportedLanguage.isDefaultLanguageStatus()) {
                this.defaultLanguageCode = poSupportedLanguage
                        .getLanguageISOCode();
                return;
            }
        }
    }

    public List<POSupportedLanguage> getLanguages() {
        return languages;
    }

    public void setLanguages(List<POSupportedLanguage> languages) {
        this.languages = languages;
    }

    public String getSelectedLanguageCode() {
        return selectedLanguageCode;
    }

    public void setSelectedLanguageCode(String selectedLanguageCode) {
        this.selectedLanguageCode = selectedLanguageCode;
    }

    public String getNewISOCode() {
        return newISOCode;
    }

    public void setNewISOCode(String newISOCode) {
        this.newISOCode = newISOCode == null ? "" : newISOCode.toLowerCase();
    }

    /**
     * @return the localizedData
     */
    public POLocalizedData getLocalizedData() {
        return localizedData;
    }

    /**
     * @param localizedData
     *            the localizedData to set
     */
    public void setLocalizedData(POLocalizedData localizedData) {
        this.localizedData = localizedData;
    }
}
