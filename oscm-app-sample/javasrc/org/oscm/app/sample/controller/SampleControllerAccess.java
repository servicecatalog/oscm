/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: Jan 25, 2017                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.sample.controller;

import java.util.LinkedList;
import java.util.List;

import org.oscm.app.v2_0.i18n.Messages;
import org.oscm.app.v2_0.intf.ControllerAccess;

public class SampleControllerAccess implements ControllerAccess {

    private static final long serialVersionUID = -4783887274347693642L;

    @Override
    public String getControllerId() {
        return SampleController.ID;
    }

    @Override
    public String getMessage(String locale, String key, Object... args) {
        return Messages.get(locale, key, args);
    }

    @Override
    public List<String> getControllerParameterKeys() {
        LinkedList<String> result = new LinkedList<>();
        return result;
    }
}
