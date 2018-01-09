/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2016-05-24
 *
 *******************************************************************************/

package org.oscm.app.vmware.service;

import java.util.ArrayList;
import java.util.List;

import org.oscm.app.v2_0.APPlatformServiceFactory;
import org.oscm.app.v2_0.data.ControllerSettings;
import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.oscm.app.v2_0.intf.ControllerAccess;
import org.oscm.app.vmware.business.Controller;
import org.oscm.app.vmware.i18n.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements the controller access for VMware controller.
 */
public class VMwareControllerAccess implements ControllerAccess {

    private static final long serialVersionUID = -7227238594317311419L;
    private Logger LOGGER = LoggerFactory
            .getLogger(VMwareControllerAccess.class);
    private ControllerSettings settings;

    @Override
    public String getControllerId() {
        return Controller.ID;
    }

    @Override
    public String getMessage(String locale, String key, Object... arguments) {
        return Messages.get(locale, key, arguments);
    }

    @Override
    public List<String> getControllerParameterKeys() {
        // not needed here because common configuration UI is not used
        return new ArrayList<>();
    }

    public ControllerSettings getSettings() {
        if (settings == null) {
            try {
                APPlatformServiceFactory.getInstance()
                        .requestControllerSettings(getControllerId());
                LOGGER.debug(
                        "Settings were NULL. Requested from APP and got {}",
                        settings);
            } catch (APPlatformException e) {
                LOGGER.error(
                        "Error while ControllerAcces was requesting controller setting from APP",
                        e);
            }
        }
        return settings;
    }

    public void storeSettings(ControllerSettings settings) {
        this.settings = settings;
    }
}
