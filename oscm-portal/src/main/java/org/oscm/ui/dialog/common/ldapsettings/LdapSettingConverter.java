/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.ui.dialog.common.ldapsettings;

import java.util.Collection;
import java.util.Properties;

import org.oscm.internal.usermanagement.POLdapSetting;

public class LdapSettingConverter {

    /**
     * Converts the server settings to the model representation and adds them to
     * the collection.
     * 
     * @param modelSettings
     *            The model settings to add the converted settings to.
     * @param serverSettings
     *            The server settings to convert to model representation.
     */
    void addToModel(Collection<LdapSetting> modelSettings,
            Collection<POLdapSetting> serverSettings) {
        for (POLdapSetting serverSetting : serverSettings) {
            modelSettings.add(new LdapSetting(serverSetting));
        }
    }

    /**
     * Converts the provided settings to a property structure.
     * 
     * @param modelSettings
     *            The settings to convert.
     * @param keepValueForPlatformDefault
     *            keep the value of the settings even if they are platform
     *            settings
     */
    Properties toProperties(Collection<LdapSetting> modelSettings,
            boolean keepValueForPlatformDefault) {
        Properties properties = new Properties();
        for (LdapSetting ldapSetting : modelSettings) {
            if (ldapSetting.isPlatformDefault() && !keepValueForPlatformDefault) {
                properties.setProperty(ldapSetting.getSettingKey(), "");
            } else {
                properties.setProperty(ldapSetting.getSettingKey(),
                        ldapSetting.getSettingValue());
            }
        }
        return properties;
    }

}
