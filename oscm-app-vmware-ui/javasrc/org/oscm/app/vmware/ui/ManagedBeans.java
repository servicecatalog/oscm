/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2016-05-24
 *
 *******************************************************************************/

package org.oscm.app.vmware.ui;

import java.util.HashMap;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import org.oscm.app.v2_0.data.PasswordAuthentication;
import org.oscm.app.v2_0.data.ProvisioningSettings;
import org.oscm.app.v2_0.data.Setting;
import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.oscm.app.v2_0.exceptions.AuthenticationException;
import org.oscm.app.v2_0.exceptions.ConfigurationException;
import org.oscm.app.v2_0.intf.APPlatformService;
import org.oscm.app.vmware.business.Controller;
import org.oscm.app.vmware.business.VMPropertyHandler;
import org.oscm.app.vmware.i18n.Messages;

/**
 * @author kulle
 * 
 */
public class ManagedBeans {

    public VMPropertyHandler loadControllerSettings(
            APPlatformService platformService) throws AuthenticationException,
            ConfigurationException, APPlatformException {

        HashMap<String, Setting> controllerSettings = getControllerSettings(platformService);

        ProvisioningSettings settings = new ProvisioningSettings(
                new HashMap<String, Setting>(), controllerSettings,
                Messages.DEFAULT_LOCALE);
        return new VMPropertyHandler(settings);
    }

    private HashMap<String, Setting> getControllerSettings(
            APPlatformService platformService) throws AuthenticationException,
            ConfigurationException, APPlatformException {

        FacesContext facesContext = getContext();
        HttpSession session = (HttpSession) facesContext.getExternalContext()
                .getSession(false);

        Object userId = session.getAttribute("loggedInUserId");
        Object password = session.getAttribute("loggedInUserPassword");

        PasswordAuthentication tpUser = new PasswordAuthentication(
                userId.toString(), password.toString());

        return platformService.getControllerSettings(Controller.ID, tpUser);
    }

    public FacesContext getContext() {
        return FacesContext.getCurrentInstance();
    }

    public String getDefaultLanguage() {
        return FacesContext.getCurrentInstance().getApplication()
                .getDefaultLocale().getLanguage();
    }

}
