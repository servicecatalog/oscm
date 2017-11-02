/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Dec 28, 2011                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.common;


/**
 * @author tokoda
 * 
 */
public class DisplayData {

    public static String getServiceName(String serviceName) {
        if (serviceName == null || serviceName.trim().length() == 0) {
            return JSFUtils.getText("service.name.undefined", null);
        }
        return serviceName;
    }
}
