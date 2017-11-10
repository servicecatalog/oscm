/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.ui.dialog.common.ldapsettings;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import org.oscm.ui.beans.BaseBean;
import org.oscm.internal.usermanagement.UserManagementService;

@ViewScoped
@ManagedBean(name="platformSettingsCtrl")
public class PlatformSettingCtrl extends BaseBean {

    @ManagedProperty(value="#{platformSettingsModel}")
    transient PlatformSettingModel model;
    transient UserManagementService ums;

    public String getInitialize() {
        if (model == null) {
            model = ui.findBean("platformSettingsModel");
        }
        initModelData();
        return "";
    }

    void initModelData() {

        getModel().getPlatformSettings().clear();
        getSettingConverter().addToModel(model.getPlatformSettings(),
                getUserManagementService().getPlatformSettings());

    }

    protected UserManagementService getUserManagementService() {
        if (ums == null) {
            ums = sl.findService(UserManagementService.class);
        }
        return ums;
    }

    public String exportSettings() throws IOException {

        Properties props = getSettingConverter().toProperties(
                model.getPlatformSettings(), false);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            props.store(baos, null);
            writeSettings(baos.toByteArray());
        } finally {
            baos.close();
        }

        return OUTCOME_SUCCESS;
    }

    void writeSettings(byte[] content) throws IOException {
        super.writeContentToResponse(content, "platformSettings.properties",
                "text/plain");
    }

    LdapSettingConverter getSettingConverter() {
        return new LdapSettingConverter();
    }

    public PlatformSettingModel getModel() {
        return model;
    }

    public void setModel(PlatformSettingModel model) {
        this.model = model;
    }

    public boolean isPlatformSettingsDefined() {
        return !model.getPlatformSettings().isEmpty();
    }

}
