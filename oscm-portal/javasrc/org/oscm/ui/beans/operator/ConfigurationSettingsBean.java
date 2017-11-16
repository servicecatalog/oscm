/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: tokoda                                                     
 *                                                                              
 *  Creation Date: 31.01.2011                                                      
 *                                                                              
 *  Completion Time: <date>                                              
 *                                                                              
 *******************************************************************************/
package org.oscm.ui.beans.operator;

import java.io.Serializable;
import java.text.Collator;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import org.oscm.internal.types.exception.DuplicateTenantIdException;
import org.oscm.types.constants.Configuration;
import org.oscm.ui.beans.ApplicationBean;
import org.oscm.ui.beans.BaseBean;
import org.oscm.ui.beans.MenuBean;
import org.oscm.ui.common.ExceptionHandler;
import org.oscm.internal.types.enumtypes.ConfigurationKey;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.OrganizationAuthoritiesException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOConfigurationSetting;

/**
 * Bean for managing configuration settings.
 * 
 * @author tokoda
 * 
 */
@ViewScoped
@ManagedBean(name = "configurationSettingsBean")
public class ConfigurationSettingsBean extends BaseOperatorBean implements
        Serializable {

    private static final long serialVersionUID = 1111031201991623753L;

    private List<VOConfigurationSetting> configurationSettings;

    // The SOP starting prefix to check if a configuration setting is an SOP
    // setting
    private static final String SOP_STARTING_PREFIX = "SOP_";
    // The SSO starting prefix to check if a configuration setting is an SSO
    // setting
    private static final String SSO_STARTING_PREFIX = "SSO_";

    // String value of the authentication mode key
    private static final String AUTH_MODE = "AUTH_MODE";
    // Auth mode key value when running in SSO mode
    private static final String AUTH_MODE_SAML_SP = "SAML_SP";

    // The id of the SOP_ORGANIZATION_IDENTIFIER SOP configuration setting.
    private static final String SOP_ORGANIZATION_IDENTIFIER = "SOP_ORGANIZATION_IDENTIFIER";

    @ManagedProperty(value="#{appBean}")
    private ApplicationBean appBean = null;
    @ManagedProperty(value="#{menuBean}")
    private MenuBean menuBean = null;

    /**
     * By default, configuration keys are sorted alphabetical.
     */
    public class DefaultSortingOfConfigurationSettings implements
            Comparator<VOConfigurationSetting> {

        Collator collator = Collator.getInstance();

        public int compare(VOConfigurationSetting configurationKey1,
                VOConfigurationSetting configurationKey2) {
            return collator.compare(configurationKey1.getInformationId()
                    .getKeyName(), configurationKey2.getInformationId()
                    .getKeyName());
        }

    }

    public List<VOConfigurationSetting> getConfigurationSettings() {

        if (configurationSettings == null) {
            try {
                configurationSettings = getOperatorService()
                        .getConfigurationSettings();
                fillAllConfigurationKeys();
                // Filter the SOP configuration settings, if necessary.
                filterSSOConfigurationSettings();
                filterSOPConfigurationSettings();
                filterHiddenConfigurationSettings();
                Collections.sort(configurationSettings,
                        new DefaultSortingOfConfigurationSettings());
            } catch (OrganizationAuthoritiesException e) {
                ExceptionHandler.execute(e);
            }
        }

        return configurationSettings;
    }

    private void fillAllConfigurationKeys() {

        ConfigurationKey[] allConfigurationKeys = ConfigurationKey.values();

        for (ConfigurationKey configurationKey : allConfigurationKeys) {
            if (!isConfigurationKeyContained(configurationKey)) {
                VOConfigurationSetting voSetting = new VOConfigurationSetting(
                        configurationKey, Configuration.GLOBAL_CONTEXT, "");
                voSetting.setVersion(-1); //
                configurationSettings.add(voSetting);
            }
        }
    }

    private boolean isConfigurationKeyContained(ConfigurationKey key) {
        for (VOConfigurationSetting configuration : configurationSettings) {
            if (key.equals(configuration.getInformationId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Filters the SOP configuration settings, only in the case the
     * SOP_ORGANIZATION_IDENTIFIER is not present or set. If it is set, the SOP
     * configuration settings are displayed normally.
     */
    private void filterSOPConfigurationSettings() {
        if (existsSopOrgSetting()) {
            return; // do not filter
        }
        for (Iterator<VOConfigurationSetting> i = configurationSettings
                .iterator(); i.hasNext();) {
            VOConfigurationSetting setting = i.next();
            if (isSopSetting(setting)) {
                i.remove();
            }
        }
    }

    /**
     * Filters the SSO configuration settings, only in the case when
     * the application is not running in SSO mode.
     */
    private void filterSSOConfigurationSettings() {
        if (isSamlMode()) {
            return; // do not filter
        }
        for (Iterator<VOConfigurationSetting> i = configurationSettings
                .iterator(); i.hasNext();) {
            VOConfigurationSetting setting = i.next();
            if (isSsoSetting(setting)) {
                i.remove();
            }
        }
    }

    /**
     * Filters the hidden configuration settings.
     */
    private void filterHiddenConfigurationSettings() {
        for (Iterator<VOConfigurationSetting> i = configurationSettings
                .iterator(); i.hasNext();) {
            VOConfigurationSetting setting = i.next();
            if (isHiddenSetting(setting)) {
                i.remove();
            }
        }
    }

    /**
     * returns true if a VOConfigurationSetting is an SOP configuration setting
     * and false otherwise.
     * 
     * @param setting
     *            the VOConfigurationSetting to check.
     * @return a boolean indicating if a configuration setting is an SOP
     *         setting.
     */
    private boolean isSopSetting(VOConfigurationSetting setting) {
        if (setting.getInformationId().getKeyName()
                .startsWith(SOP_STARTING_PREFIX)) {
            return true;
        }
        return false;
    }

    /**
     * returns true if a VOConfigurationSetting is an SSO configuration setting
     * and false otherwise. Settings required in both INTERNAL and SAML_SP modes
     * are NOT treated as SSO settings.
     *
     * @param setting the VOConfigurationSetting to check.
     * @return a boolean indicating if a configuration setting is an SSO
     * setting.
     */
    private boolean isSsoSetting(VOConfigurationSetting setting) {
        final List<String> excludedSettings = Arrays.asList("SSO_SIGNING_KEY_ALIAS", "SSO_SIGNING_KEYSTORE_PASS", "SSO_SIGNING_KEYSTORE");
        final String settingName = setting.getInformationId().getKeyName();

        if (excludedSettings.contains(settingName)) {
            return false;
        }
        if (settingName.startsWith(SSO_STARTING_PREFIX)) {
            return true;
        }
        return false;
    }

    /**
     * returns true if a VOConfigurationSetting is a hidden configuration setting
     * and false otherwise.
     *
     * @param setting the VOConfigurationSetting to check.
     * @return a boolean indicating if a configuration setting is a hidden
     * setting.
     */
    private boolean isHiddenSetting(VOConfigurationSetting setting) {
        final List<String> hiddenSettings = Arrays.asList("LOG_FILE_PATH"
                , "LOG_CONFIG_FILE"
                , "KEY_FILE_PATH"
                , "BASE_URL"
                , "HIDE_PAYMENT_INFORMATION");
        final String settingName = setting.getInformationId().getKeyName();

        if (hiddenSettings.contains(settingName)) {
            return true;
        }
        return false;
    }

    /**
     * Returns true if there exists a configuration setting with key:
     * SOP_ORGANIZATION_IDENTIFIER and has a set value and false if this setting
     * is absent.
     * 
     * @return a boolean indicating if the SOP_ORGANIZATION_IDENTIFIER is
     *         present and set.
     */
    private boolean existsSopOrgSetting() {
        for (VOConfigurationSetting setting : configurationSettings) {
            if (setting.getInformationId().getKeyName()
                    .equals(SOP_ORGANIZATION_IDENTIFIER)
                    && setting.getValue().trim().length() > 0) {
                return true;
            }
        }
        return false;
    }

    private boolean isSamlMode() {
        for (VOConfigurationSetting setting : configurationSettings) {
            if (setting.getInformationId().getKeyName()
                    .equals(AUTH_MODE)
                    && setting.getValue().equals(AUTH_MODE_SAML_SP)) {
                return true;
            }
        }
        return false;
    }

    public String save() throws OrganizationAuthoritiesException,
            ValidationException, ConcurrentModificationException, DuplicateTenantIdException {

        getOperatorService().saveConfigurationSettings(configurationSettings);
        addMessage(null, FacesMessage.SEVERITY_INFO, INFO_CONFIGURATION_SAVED);
        configurationSettings = null;
        getAppBean().reset();
        getMenuBean().resetMenuVisibility();

        return BaseBean.OUTCOME_SUCCESS;
    }

    public ApplicationBean getAppBean() {
        return appBean;
    }

    public void setAppBean(ApplicationBean appBean) {
        this.appBean = appBean;
    }

    public MenuBean getMenuBean() {
        return menuBean;
    }

    public void setMenuBean(MenuBean menuBean) {
        this.menuBean = menuBean;
    }

}
