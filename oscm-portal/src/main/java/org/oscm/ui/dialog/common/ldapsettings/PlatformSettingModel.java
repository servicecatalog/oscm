/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.ui.dialog.common.ldapsettings;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

@ViewScoped
@ManagedBean(name="platformSettingsModel")
public class PlatformSettingModel implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<LdapSetting> platformSettings = new ArrayList<LdapSetting>();

    public List<LdapSetting> getPlatformSettings() {
        return platformSettings;
    }

    public void setPlatformSettings(List<LdapSetting> platformSettings) {
        this.platformSettings = platformSettings;
    }

}
