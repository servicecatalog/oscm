/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 2014-01-13                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.iaas.exceptions;

/**
 * Exception is thrown when firewall policies cannot be resolved.
 */
public class PolicyConfigurationException extends IaasException {

    private static final long serialVersionUID = -1889247329375336756L;

    public PolicyConfigurationException(String message) {
        super(message);
    }

    @Override
    public boolean isBusyMessage() {
        return false;
    }

    @Override
    public boolean isIllegalState() {
        return false;
    }

}
