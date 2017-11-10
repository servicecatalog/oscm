/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.ui.dialog.common.ldapsettings;

import org.oscm.string.Strings;
import org.oscm.internal.usermanagement.POLdapSetting;

public class LdapSetting {

    private String settingKey;
    private String settingValue;
    private boolean platformDefault;

    public LdapSetting(String settingKey, String settingValue,
            boolean platformDefault) {
        this.settingKey = settingKey;
        this.settingValue = settingValue;
        this.platformDefault = platformDefault;
    }

    public LdapSetting(POLdapSetting entry) {
        this.settingKey = entry.getSettingKey();
        this.settingValue = entry.getSettingValue();
        this.platformDefault = entry.isPlatformDefault();
    }

    public String getSettingKey() {
        return settingKey;
    }

    public void setSettingKey(String settingKey) {
        this.settingKey = settingKey;
    }

    public String getSettingValue() {
        return settingValue;
    }

    public void setSettingValue(String settingValue) {
        this.settingValue = settingValue;
    }

    public boolean isPlatformDefault() {
        return platformDefault;
    }

    public void setPlatformDefault(boolean platformDefault) {
        this.platformDefault = platformDefault;
    }

    public boolean equals(Object setting) {
        if (!(setting instanceof POLdapSetting)) {
            return false;
        }
        POLdapSetting otherSetting = (POLdapSetting) setting;
        return Strings
                .areStringsEqual(settingKey, otherSetting.getSettingKey());
    }

    public int hashCode() {
        if (settingKey == null) {
            return 0;
        }
        return settingKey.hashCode();
    }

}
