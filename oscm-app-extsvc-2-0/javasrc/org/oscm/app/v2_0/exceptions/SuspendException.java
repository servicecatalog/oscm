/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *       
 *  Creation Date: 2012-10-30                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.v2_0.exceptions;

import java.util.List;

import org.oscm.app.v2_0.data.LocalizedText;

/**
 * Exception a controller can throw to request APP to suspend the current
 * provisioning operation, for example, because an action failed at the
 * application side.
 * <p>
 * APP suspends its automatic polling for events. It sends an email to the
 * technology managers of the technology provider organization which is
 * responsible for the application. The email contains the detail message given
 * in the exception as well as a link for the technology managers to follow in
 * order to resume the operation after the problem has been sorted out.
 */
public class SuspendException extends APPlatformException {

    private static final long serialVersionUID = 7136077126869392797L;

    private int responseCode = 0;

    /**
     * Constructs a new exception with the specified detail message. The cause
     * is not initialized.
     * 
     * @param message
     *            the detail message
     */
    public SuspendException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified detail message. The cause
     * is not initialized.
     * 
     * @param message
     *            the detail message
     * @param responseCode
     *            the response code
     */
    public SuspendException(String message, int responseCode) {
        super(message);
        this.responseCode = responseCode;
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     * 
     * @param message
     *            the detail message
     * @param cause
     *            the cause
     */
    public SuspendException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new exception with the specified localized text messages.
     * The cause is not initialized.
     * 
     * @param messages
     *            the localized text messages
     */
    public SuspendException(List<LocalizedText> messages) {
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
    public SuspendException(List<LocalizedText> messages, Throwable cause) {
        super(messages, cause);
    }

    public int getResponseCode() {
        return responseCode;
    }

}
