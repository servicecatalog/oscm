/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: cheld //TODO                                                      
 *                                                                              
 *  Creation Date: 20.06.2012                                                      
 *                                                                              
 *  Completion Time: <date> //TODO                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.common;

import javax.faces.application.FacesMessage;

import org.oscm.internal.components.response.Response;
import org.oscm.internal.components.response.ReturnCode;
import org.oscm.internal.components.response.ReturnType;

/**
 * Handler for response objects.
 * 
 * @author cheld
 * 
 */
public class ResponseHandler {

    /**
     * Adds the message contained in the response to the Faces context
     * 
     * @param response
     */
    public static void handle(Response response, String successMessageKey,
            Object... params) {
        ReturnCode returnCode = response.getMostSevereReturnCode();
        if (returnCode != null) {
            JSFUtils.addMessage(returnCode.getMember(),
                    mapToFaces(returnCode.getType()),
                    returnCode.getMessageKey(), returnCode.getMessageParam());
        } else {
            JSFUtils.addMessage(null, FacesMessage.SEVERITY_INFO,
                    successMessageKey, params);
        }
    }

    static FacesMessage.Severity mapToFaces(ReturnType type) {
        if (type == ReturnType.WARNING) {
            return FacesMessage.SEVERITY_WARN;
        }
        return FacesMessage.SEVERITY_INFO;
    }

}
