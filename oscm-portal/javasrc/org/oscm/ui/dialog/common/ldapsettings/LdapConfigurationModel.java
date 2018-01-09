/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.ui.dialog.common.ldapsettings;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;
import javax.servlet.http.Part;

/**
 * Model class to contain the LDAP configuration settings to be displayed.
 * 
 * @author jaeger
 * 
 */
@ViewScoped
@ManagedBean(name="ldapConfigurationModel")
public class LdapConfigurationModel implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<LdapSetting> settings = new ArrayList<LdapSetting>();
    private List<SelectItem> organizations = new ArrayList<SelectItem>();
    private Part file;

    private boolean organizationSelectionSupported;
    private String organizationIdentifier;
    private boolean showIsPlatformSettingColumnVisible;
    private boolean showClearButtonVisible;

    public List<LdapSetting> getSettings() {
        return settings;
    }

    public void setSettings(List<LdapSetting> settings) {
        this.settings = settings;
    }

    public Part getFile() {
        return file;
    }

    public void setFile(Part file) {
        this.file = file;
    }

    public boolean isOrganizationSelectionSupported() {
        return organizationSelectionSupported;
    }

    public void setOrganizationSelectionSupported(
            boolean organizationSelectionSupported) {
        this.organizationSelectionSupported = organizationSelectionSupported;
    }

    public String getOrganizationIdentifier() {
        return organizationIdentifier;
    }

    public void setOrganizationIdentifier(String organizationIdentifier) {
        this.organizationIdentifier = organizationIdentifier;
    }

    public List<SelectItem> getOrganizations() {
        return organizations;
    }

    public void setOrganizations(List<SelectItem> organizations) {
        this.organizations = organizations;
    }

    public boolean isShowIsPlatformSettingColumnVisible() {
        return showIsPlatformSettingColumnVisible;
    }

    public void setShowIsPlatformSettingColumnVisible(
            boolean showIsPlatformSettingColumnVisible) {
        this.showIsPlatformSettingColumnVisible = showIsPlatformSettingColumnVisible;
    }

    public boolean isShowClearButtonVisible() {
        return showClearButtonVisible;
    }

    public void setShowClearButtonVisible(boolean showClearButtonVisible) {
        this.showClearButtonVisible = showClearButtonVisible;
    }

}
