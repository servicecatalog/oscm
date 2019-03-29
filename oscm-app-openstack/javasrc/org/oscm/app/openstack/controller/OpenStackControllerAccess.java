/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 03.07.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.openstack.controller;

import java.util.LinkedList;
import java.util.List;

import org.oscm.app.openstack.i18n.Messages;
import org.oscm.app.v2_0.APPlatformServiceFactory;
import org.oscm.app.v2_0.data.ControllerSettings;
import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.oscm.app.v2_0.intf.ControllerAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenStackControllerAccess implements ControllerAccess {
    private Logger LOGGER = LoggerFactory
            .getLogger(OpenStackControllerAccess.class);
    private static final long serialVersionUID = 8912611326487987648L;
    private ControllerSettings settings;

    @Override
    public String getControllerId() {
        return OpenStackController.ID;
    }

    @Override
    public String getMessage(String locale, String key, Object... arguments) {
        return Messages.get(locale, key, arguments);
    }

    @Override
    public List<String> getControllerParameterKeys() {
        LinkedList<String> result = new LinkedList<>();
        result.add(PropertyHandler.API_USER_NAME);
        result.add(PropertyHandler.API_USER_PWD);
        result.add(PropertyHandler.KEYSTONE_API_URL);
        result.add(PropertyHandler.TENANT_ID);
        result.add(PropertyHandler.DOMAIN_NAME);
        result.add(PropertyHandler.TEMPLATE_BASE_URL);
        result.add(PropertyHandler.TIMER_INTERVAL);
        return result;
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
