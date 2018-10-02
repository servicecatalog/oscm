/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 2016-07-29
 *
 *******************************************************************************/
package org.oscm.app.azure.exception;

public class AzureClientException extends RuntimeException {

    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = -5369307665782818572L;

    /**
     * @param message
     * @param cause
     */
    public AzureClientException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message
     */
    public AzureClientException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public AzureClientException(Throwable cause) {
        super(cause);
    }
}
