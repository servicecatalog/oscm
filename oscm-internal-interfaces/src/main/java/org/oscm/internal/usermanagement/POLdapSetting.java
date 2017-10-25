/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.internal.usermanagement;

import java.io.Serializable;

public class POLdapSetting implements Serializable {

    private static final long serialVersionUID = 1L;

    private String settingKey;
    private String settingValue;
    private boolean platformDefault;

    public POLdapSetting(String settingKey, String settingValue,
            boolean platformDefault) {
        this.settingKey = settingKey;
        this.settingValue = settingValue;
        this.platformDefault = platformDefault;
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
        return areStringsEqual(settingKey, otherSetting.getSettingKey());
    }

    public int hashCode() {
        if (settingKey == null) {
            return 0;
        }
        return settingKey.hashCode();
    }

    private boolean areStringsEqual(String s1, String s2) {
        if (s1 == null) {
            return s2 == null;
        }
        return s1.equals(s2);
    }

}
