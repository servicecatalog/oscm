/* 
 *  Copyright FUJITSU LIMITED 2017
 **
 * 
 */
package org.oscm.internal.types.exception;

import org.oscm.internal.types.exception.beans.BulkUserImportExceptionBean;

/**
 * Exception thrown when the bulk user import fails
 * 
 * @author cheld
 * 
 */
public class BulkUserImportException extends SaaSApplicationException {

    private static final long serialVersionUID = 4246678618351608402L;

    private BulkUserImportExceptionBean bean = new BulkUserImportExceptionBean();

    /**
     * Constructs a new exception with the given cause, reason, failed line, and
     * appends the specified reason to the message key.
     * 
     * @param reason
     *            the reason
     * @param e
     *            the cause
     * @param line
     *            the line on which the file parsing failed
     */
    public BulkUserImportException(Reason reason, Exception cause,
            int lineNumber) {
        super("Parsing failed at line " + lineNumber, cause, new Object[] { ""
                + lineNumber });
        bean.setReason(reason);
        setMessageKey(initMessageKey(reason));
    }

    /**
     * Constructs a new exception with the given cause, reason, and appends the
     * specified reason to the message key.
     * 
     * @param reason
     *            the reason
     * @param cause
     *            the cause
     */
    public BulkUserImportException(Reason reason, Exception cause) {
        super(reason.toString(), cause);
        bean.setReason(reason);
        setMessageKey(initMessageKey(reason));
    }

    /**
     * Constructs a new exception with <code>null</code> as its detail message.
     * The cause is not initialized.
     */
    public BulkUserImportException() {
        super();
    }

    /**
     * Constructs a new exception with the specified detail message. The cause
     * is not initialized.
     * 
     * @param message
     *            the detail message
     */
    public BulkUserImportException(String msg) {
        super(msg);
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
    public BulkUserImportException(String msg, BulkUserImportExceptionBean bean) {
        super(msg, bean);
        this.bean = bean;
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
    public BulkUserImportException(String msg,
            BulkUserImportExceptionBean bean, Throwable cause) {
        super(msg, bean, cause);
        this.bean = bean;
    }

    private String initMessageKey(Reason reason) {
        String enumName = reason.toString();
        return super.getMessageKey() + "." + enumName;
    }

    public static enum Reason {

        /**
         * Parsing the CSV data failed completely, e.g. a closing quote is
         * missing
         */
        PARSING_FAILED,

        /**
         * One line in the CSV data does not contain enough commas
         */
        WRONG_NUMBER_OF_FIELDS,

        /**
         * One line in the CSV data contains a salutation that could not be
         * recognized by the system
         */
        WRONG_SALUTATION,

        /**
         * One line in the CSV data contains a role that could not be recognized
         * by the system
         */
        WRONG_ROLE,

        /**
         * One line in the CSV data does not contain a user ID
         */
        MISSING_USERID,

        /**
         * One line in the CSV data does not contain a locale
         */
        MISSING_LOCALE,

        /**
         * User creation failed for unknown reason
         */
        USER_CREATION_FAILED,

        /**
         * The importing user must have an email set.
         */
        EMAIL_REQUIRED;

    }

    @Override
    public BulkUserImportExceptionBean getFaultInfo() {
        return new BulkUserImportExceptionBean(super.getFaultInfo(),
                bean.getReason());
    }

}
