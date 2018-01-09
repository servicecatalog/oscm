/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 01.10.2014                                                      
 *                                                                              
 *******************************************************************************/

/**
 * @author stavreva
 *
 */

package org.oscm.app.v2_0.exceptions;

import java.util.List;

import org.oscm.app.v2_0.data.LocalizedText;

/**
 * Exception APP can throw if the controller is not initialized. It can happen
 * at container restart. The APP is initialized and the APP timer runs requiring
 * a controller instance. The controller is not initialized yet.
 */
public class ControllerLookupException extends APPlatformException {

    private static final long serialVersionUID = 1841813273474547148L;

    /**
     * Constructs a new exception with the specified detail message. The cause
     * is not initialized.
     * 
     * @param message
     *            the detail message
     */
    public ControllerLookupException(String message) {
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
    public ControllerLookupException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new exception with the specified localized text messages.
     * The cause is not initialized.
     * 
     * @param messages
     *            the localized text messages
     */
    public ControllerLookupException(List<LocalizedText> messages) {
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
    public ControllerLookupException(List<LocalizedText> messages,
            Throwable cause) {
        super(messages, cause);
    }
}
