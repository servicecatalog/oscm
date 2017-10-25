/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2013-11-6                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.oscm.domobjects.annotations.BusinessKey;

/**
 * Domain object of SupportedLanguage
 * 
 * @author Zou
 * 
 */
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "languageISOCode" }))
@BusinessKey(attributes = { "languageISOCode" })
@NamedQueries({
        @NamedQuery(name = "SupportedLanguage.findAll", query = "SELECT sl FROM SupportedLanguage sl order by sl.dataContainer.defaultStatus desc, sl.dataContainer.languageISOCode "),
        @NamedQuery(name = "SupportedLanguage.findAllActive", query = "SELECT sl FROM SupportedLanguage sl WHERE sl.dataContainer.activeStatus = TRUE order by sl.dataContainer.defaultStatus desc, sl.dataContainer.languageISOCode"),
        @NamedQuery(name = "SupportedLanguage.findByBusinessKey", query = "SELECT sl FROM SupportedLanguage sl WHERE sl.dataContainer.languageISOCode = :languageISOCode"),
        @NamedQuery(name = "SupportedLanguage.findDefault", query = "SELECT sl.dataContainer.languageISOCode FROM SupportedLanguage sl WHERE sl.dataContainer.defaultStatus = TRUE") })
@Entity
public class SupportedLanguage extends
        DomainObjectWithVersioning<SupportedLanguageData> {

    private static final long serialVersionUID = 6373227371951477776L;

    /**
     * Default constructor.
     */
    public SupportedLanguage() {
        super();
        dataContainer = new SupportedLanguageData();
    }

    public String getLanguageISOCode() {
        return dataContainer.getLanguageISOCode();
    }

    public void setLanguageISOCode(String languageISOCode) {
        dataContainer.setLanguageISOCode(languageISOCode);
    }

    public boolean getActiveStatus() {
        return dataContainer.getActiveStatus();
    }

    public void setActiveStatus(boolean activeStatus) {
        dataContainer.setActiveStatus(activeStatus);
    }

    public boolean getDefaultStatus() {
        return dataContainer.getDefaultStatus();
    }

    public void setDefaultStatus(boolean defaultStatus) {
        dataContainer.setDefaultStatus(defaultStatus);
    }

}
