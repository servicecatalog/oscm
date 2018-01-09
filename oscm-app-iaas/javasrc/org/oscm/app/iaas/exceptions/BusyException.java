/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Creation Date: 2014-01-14                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.iaas.exceptions;

public class BusyException extends IaasException {

    public BusyException(String message) {
        super(message);
    }

    private static final long serialVersionUID = 8938016862668098677L;

    @Override
    public boolean isBusyMessage() {
        return true;
    }

    @Override
    public boolean isIllegalState() {
        return false;
    }
}
