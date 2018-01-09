/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: may 12 2015
 *
 *******************************************************************************/

package org.oscm.app.v2_0.exceptions;

import java.util.List;

import org.oscm.app.v2_0.data.LocalizedText;

public class InstanceExistsException extends APPlatformException {

    private static final long serialVersionUID = 5696569816753547107L;

    /**
     * Constructs a new exception with the specified detail message. The cause
     * is not initialized.
     * 
     * @param message
     *            the detail message
     */
    public InstanceExistsException(String message) {
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
    public InstanceExistsException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new exception with the specified localized text messages.
     * The cause is not initialized.
     * 
     * @param messages
     *            the localized text messages
     */
    public InstanceExistsException(List<LocalizedText> messages) {
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
    public InstanceExistsException(List<LocalizedText> messages, Throwable cause) {
        super(messages, cause);
    }
}
