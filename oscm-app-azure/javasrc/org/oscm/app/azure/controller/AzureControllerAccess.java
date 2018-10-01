/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2018-10-01
 *
 *******************************************************************************/
package org.oscm.app.azure.controller;

import org.oscm.app.azure.i18n.Messages;
import org.oscm.app.v2_0.intf.ControllerAccess;

import java.util.Collections;
import java.util.List;

public class AzureControllerAccess implements ControllerAccess {

    private static final long serialVersionUID = 9031101868925333431L;

    @Override
    public String getControllerId() {
        return AzureController.ID;
    }

    @Override
    public String getMessage(String locale, String key, Object... arguments) {
        return Messages.get(locale, key, arguments);
    }

    @Override
    public List<String> getControllerParameterKeys() {
        return Collections.emptyList();
    }
}
