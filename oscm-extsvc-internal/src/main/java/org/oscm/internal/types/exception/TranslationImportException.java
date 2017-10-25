/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2009-03-20                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.types.exception;

import org.oscm.internal.types.exception.beans.ImportExceptionBean;
import org.oscm.internal.types.exception.beans.TranslationImportExceptionBean;

/**
 * Exception thrown when the import of a technical or marketable service
 * definition fails.
 * 
 */
public class TranslationImportException extends SaaSApplicationException {

    private static final long serialVersionUID = 3043864979786939426L;
    private TranslationImportExceptionBean bean = new TranslationImportExceptionBean();

    /**
     * Constructs a new exception with <code>null</code> as its detail message.
     * The cause is not initialized.
     */
    public TranslationImportException() {
        super();
    }

    /**
     * Constructs a new exception with a pre-defined message text and the
     * specified details.
     * 
     * @param details
     *            the detailed information
     */
    public TranslationImportException(String details) {
        super("Import Failed", new Object[] { details });
        bean.setDetails(details);
    }

    /**
     * Constructs a new exception with the specified detail message and bean for
     * JAX-WS exception serialization.
     * 
     * @param message
     *            the detail message
     * @param bean
     *            the bean for JAX-WS exception serialization
     */
    public TranslationImportException(String message,
            TranslationImportExceptionBean bean) {
        super(message, bean);
        this.bean = bean;
    }

    /**
     * Constructs a new exception with the specified reason set as its detail
     * message and appended to the message key.
     * 
     * @param reason
     *            the reason
     */
    public TranslationImportException(Reason reason) {
        super(String.valueOf(reason));
        setMessageKey(getMessageKey() + "." + reason);
    }

    /**
     * Constructs a new exception with the specified detail message, cause, and
     * bean for JAX-WS exception serialization.
     * 
     * @param message
     *            the detail message
     * @param bean
     *            the bean for JAX-WS exception serialization
     * @param cause
     *            the cause
     */
    public TranslationImportException(String message,
            TranslationImportExceptionBean bean, Throwable cause) {
        super(message, bean, cause);
        this.bean = bean;
    }

    /**
     * Returns the detailed information provided with this exception.
     * 
     * @return the detailed information
     */
    public String getDetails() {
        return bean.getDetails();
    }

    /* javadoc is copied from super class */
    @Override
    public ImportExceptionBean getFaultInfo() {
        return new ImportExceptionBean(super.getFaultInfo(), bean.getDetails());
    }

    /**
     * Enumeration of possible reasons for a {@link TranslationImportException}.
     * 
     */
    public enum Reason {

        /**
         * If the platform operator imports localized data with not all standard
         * languages.
         */
        MISSING_STANDARD_LANGUAGE,

        /**
         * If the platform operator imports localized data with more than one
         * language code.
         */
        MULTI_LANGUAGE_CODE_NOT_SUPPORTE,

        /**
         * If the platform operator imports localized data with error sheet
         * name.
         */
        SHEET_NAME_NOT_FOUND,

        /**
         * If the platform operator imports localized data missing key.
         * 
         */
        MISSING_KEY,

        /**
         * imported keys which was not found in properties file
         * 
         */
        KEY_NOT_FOUND,

        /**
         * formats of imported cells which were not text
         * 
         */
        CELL_NOT_TEXT;
    }

}
