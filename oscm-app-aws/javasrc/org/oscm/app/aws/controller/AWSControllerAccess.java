/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 30.05.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.aws.controller;

import java.util.LinkedList;
import java.util.List;

import org.oscm.app.aws.i18n.Messages;
import org.oscm.app.v2_0.intf.ControllerAccess;

public class AWSControllerAccess implements ControllerAccess {

    private static final long serialVersionUID = -5454221403279778113L;

    @Override
    public String getControllerId() {
        return AWSController.ID;
    }

    @Override
    public String getMessage(String locale, String key, Object... args) {
        return Messages.get(locale, key, args);
    }

    @Override
    public List<String> getControllerParameterKeys() {
        LinkedList<String> result = new LinkedList<>();
        result.add(PropertyHandler.ACCESS_KEY_ID_PWD);
        result.add(PropertyHandler.SECRET_KEY_PWD);
        return result;
    }

}
