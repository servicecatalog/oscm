/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2014-04-02                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.types.exceptions;

import javax.xml.bind.annotation.XmlType;
import javax.xml.ws.WebFault;

import org.oscm.types.exceptions.beans.BulkUserImportExceptionBean;

/**
 * Exception thrown when a bulk import of users from a CSV file fails.
 * 
 */
@WebFault(name = "BulkUserImportException", targetNamespace = "http://oscm.org/xsd")
public class BulkUserImportException extends SaaSApplicationException {

    private static final long serialVersionUID = -8338319299979928686L;

    private BulkUserImportExceptionBean bean = new BulkUserImportExceptionBean();

    /**
     * Constructs a new exception with the specified reason, cause, and line
     * number in the CSV file where the problem occurred.
     * 
     * @param reason
     *            the reason
     * @param cause
     *            the cause
     * @param lineNumber
     *            the line number in the CSV file
     */
    public BulkUserImportException(Reason reason, Exception cause,
            int lineNumber) {
        super("Parsing failed at line " + lineNumber, cause, new Object[] { ""
                + lineNumber });
        bean.setReason(reason);
        setMessageKey(initMessageKey(reason));
    }

    /**
     * Constructs a new exception with the specified reason and cause.
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
     * @param msg
     *            the detail message
     */
    public BulkUserImportException(String msg) {
        super(msg);
    }

    /**
     * Constructs a new exception with the specified detail message and bean for
     * JAX-WS exception serialization.
     * 
     * @param msg
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
     * @param msg
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

    /**
     * Enumeration of possible reasons for a {@link BulkUserImportException}.
     */
    @XmlType(name = "BulkUserImportException.Reason")
    public static enum Reason {

        /**
         * Parsing the CSV data failed as a whole, for example, because a
         * closing quote is missing
         */
        PARSING_FAILED,

        /**
         * One line in the CSV data does not contain enough commas.
         */
        WRONG_NUMBER_OF_FIELDS,

        /**
         * One line in the CSV data contains a salutation that could not be
         * recognized.
         */
        WRONG_SALUTATION,

        /**
         * One line in the CSV data contains a role that could not be
         * recognized.
         */
        WRONG_ROLE,

        /**
         * One line in the CSV data does not contain a user ID.
         */
        MISSING_USERID,

        /**
         * One line in the CSV data does not contain a locale.
         */
        MISSING_LOCALE,

        /**
         * The user creation failed for unknown reasons.
         */
        USER_CREATION_FAILED,

        /**
         * The user to import must have an email address set.
         */
        EMAIL_REQUIRED;

    }

    @Override
    public BulkUserImportExceptionBean getFaultInfo() {
        return new BulkUserImportExceptionBean(super.getFaultInfo(),
                bean.getReason());
    }
}
