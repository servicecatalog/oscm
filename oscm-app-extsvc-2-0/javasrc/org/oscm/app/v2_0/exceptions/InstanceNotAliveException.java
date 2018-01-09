/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 19.12.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.v2_0.exceptions;

import java.util.List;

import org.oscm.app.v2_0.data.LocalizedText;

public class InstanceNotAliveException extends APPlatformException {

    private static final long serialVersionUID = 8272506497979966309L;

    /**
     * Constructs a new exception with the specified detail message. The cause
     * is not initialized.
     * 
     * @param message
     *            the detail message
     */
    public InstanceNotAliveException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     * 
     * @param message
     *            the detail message
     * @param cause
     *            the cause
     */
    public InstanceNotAliveException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new exception with the specified localized text messages.
     * The cause is not initialized.
     * 
     * @param messages
     *            the localized text messages
     */
    public InstanceNotAliveException(List<LocalizedText> messages) {
        super(messages);
    }

    /**
     * Constructs a new exception with the specified localized text messages and
     * cause.
     * 
     * @param messages
     *            the localized text messages
     * @param cause
     *            the cause
     */
    public InstanceNotAliveException(List<LocalizedText> messages,
            Throwable cause) {
        super(messages, cause);
    }
}
