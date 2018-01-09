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
import org.oscm.app.v2_0.intf.ControllerAccess;

public class OpenStackControllerAccess implements ControllerAccess {

    private static final long serialVersionUID = 8912611326487987648L;

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
        return result;
    }
}
