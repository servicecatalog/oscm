/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 2014-04-02                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.iaas.exceptions;

import java.util.List;

public class MissingParameterException extends IaasException {

    private static final long serialVersionUID = -8179680895627735723L;

    public MissingParameterException(String action, List<String> parameters) {
        super(getMessage(action, parameters));
    }

    @Override
    public boolean isBusyMessage() {
        return false;
    }

    static String getMessage(String action, List<String> parameters) {
        String params = "";
        for (String p : parameters) {
            params += " " + p;
        }
        String message = "Action: " + action + ", Empty parameters: " + params;
        return message;
    }

    @Override
    public boolean isIllegalState() {
        return false;
    }
}
