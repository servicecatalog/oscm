/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2013-11-6                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.types.exception;

/**
 * @author Yuyin
 * 
 */
public class PropertiesImportException extends SaaSApplicationException {

    /**
     * 
     */
    private static final long serialVersionUID = 5195512106301620532L;

    /**
     * Constructs a new exception with <code>null</code> as its detail message.
     * The cause is not initialized.
     */
    public PropertiesImportException() {

    }

    /**
     * Constructs a new exception with the specified detail message. The cause
     * is not initialized.
     * 
     * @param message
     *            the detail message
     */
    public PropertiesImportException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified reason set as its detail
     * message and appended to the message key.
     * 
     * @param reason
     *            the reason
     */
    public PropertiesImportException(Reason reason) {
        super(String.valueOf(reason));
        setMessageKey(getMessageKey() + "." + reason);
    }

    /**
     * Enumeration of possible reasons for a {@link PropertiesImportException}.
     * 
     */
    public enum Reason {
        /**
         * If the platform operator imports localized data for a language, which
         * is not (yet) supported the system.
         */
        LANGUAGE_NOT_SUPPORTED,

        /**
         * If the platform operator imports localized data for no language (no
         * language code).
         */
        NONE_LANGUAGE_CODE,

        /**
         * If the platform operator imports localized data for a language and
         * not all localizable items are translated the system.
         */
        TRANSLATIONS_MISSING;
    }
}
