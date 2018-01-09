/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.app.business.exceptions;

public class ServiceInstanceInProcessingException extends Exception {

    private static final long serialVersionUID = -2891365531402403187L;

    public ServiceInstanceInProcessingException(String message, Object... args) {
        super(String.format(message, args));
    }
}
