/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 16.10.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.v2_0.exceptions;

import java.util.List;

import org.oscm.app.v2_0.data.LocalizedText;

/**
 * Thrown in case objects cannot not be retrieved, e.g. when a controller
 * requests details about a certain service instance which cannot be found in
 * the APP database.
 * 
 * @author kulle
 * 
 */
public class ObjectNotFoundException extends APPlatformException {

    private static final long serialVersionUID = -6225450026704508558L;

    /**
     * Constructs a new exception with the specified detail message. The cause
     * is not initialized.
     * 
     * @param message
     *            the detail message
     */
    public ObjectNotFoundException(String message) {
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
    public ObjectNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new exception with the specified localized text messages.
     * The cause is not initialized.
     * 
     * @param messages
     *            the localized text messages
     */
    public ObjectNotFoundException(List<LocalizedText> messages) {
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
    public ObjectNotFoundException(List<LocalizedText> messages, Throwable cause) {
        super(messages, cause);
    }
}
