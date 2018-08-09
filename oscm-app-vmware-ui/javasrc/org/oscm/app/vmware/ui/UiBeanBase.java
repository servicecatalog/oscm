/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2016-05-24
 *
 *******************************************************************************/

package org.oscm.app.vmware.ui;

import java.io.Serializable;
import java.util.HashMap;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import org.oscm.app.v2_0.APPlatformServiceFactory;
import org.oscm.app.v2_0.data.PasswordAuthentication;
import org.oscm.app.v2_0.data.ProvisioningSettings;
import org.oscm.app.v2_0.data.Setting;
import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.oscm.app.v2_0.intf.APPlatformService;
import org.oscm.app.vmware.business.Controller;
import org.oscm.app.vmware.business.VMPropertyHandler;
import org.oscm.app.vmware.i18n.Messages;
import org.oscm.app.vmware.remote.bes.Credentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for all UI beans.
 */
public abstract class UiBeanBase implements Serializable {

    private static final long serialVersionUID = 8234739272216026913L;

    private static final Logger logger = LoggerFactory
            .getLogger(UiBeanBase.class);

    protected APPlatformService platformService;
    protected VMPropertyHandler settings;
    protected String status;
    protected boolean isSSO;
    protected Credentials techprov;

    public UiBeanBase() {
        platformService = newPlatformService();
        settings = readControllerSettings();
        isSSO = settings.isSSO();
        techprov = settings.getTPUser();
    }

    public Logger getLogger() {
        return logger;
    }

    public VMPropertyHandler getSettings() {
        return settings;
    }

    private APPlatformService newPlatformService() {
        return APPlatformServiceFactory.getInstance();
    }

    protected VMPropertyHandler readControllerSettings() {
        HashMap<String, Setting> controllerSettings = getControllerSettings();
        ProvisioningSettings settings = new ProvisioningSettings(
                new HashMap<String, Setting>(), controllerSettings,
                Messages.DEFAULT_LOCALE);
        return new VMPropertyHandler(settings);
    }

    protected HashMap<String, Setting> getControllerSettings() {
        FacesContext facesContext = getContext();
        HttpSession session = (HttpSession) facesContext.getExternalContext()
                .getSession(false);
        Object userId = session.getAttribute("loggedInUserId");
        Object password = session.getAttribute("loggedInUserPassword");

        HashMap<String, Setting> controllerSettings = new HashMap<>();
        try {
            PasswordAuthentication tpUser = new PasswordAuthentication(
                    userId.toString(), password.toString());
            controllerSettings = platformService.getControllerSettings(
                    Controller.ID, tpUser);
        } catch (APPlatformException e1) {
            getLogger().error("UiBeanBase.getControllerSettings()", e1);
        }

        return controllerSettings;
    }

    protected FacesContext getContext() {
        return FacesContext.getCurrentInstance();
    }

    public boolean isReportingSupported() {
        String folder = settings
                .getControllerSetting(VMPropertyHandler.CTL_REPORT_FOLDER);
        return (folder != null && folder.length() > 0);
    }
}
