/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 02.04.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.iaas.data;

/**
 * Represents access information for a virtual server or resource.
 */
public class AccessInformation {

    private String ip;
    private String initialPassword;

    public AccessInformation(String ip, String password) {
        this.ip = ip;
        this.initialPassword = password;
    }

    /**
     * @return the ip
     */
    public String getIP() {
        return ip;
    }

    /**
     * @return the initialPassword
     */
    public String getInitialPassword() {
        return initialPassword;
    }
}
