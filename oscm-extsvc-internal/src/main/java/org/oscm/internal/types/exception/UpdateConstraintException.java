/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2009-09-24                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.types.exception;

import org.oscm.internal.types.exception.beans.DomainObjectExceptionBean;

/**
 * Exception thrown when the update of an object fails due to the violation of
 * existing business rules.
 * 
 */
public class UpdateConstraintException extends DomainObjectException {

    private static final long serialVersionUID = 1519596702311530936L;

    /**
     * Constructs a new exception with <code>null</code> as its detail message.
     * The cause is not initialized.
     */
    public UpdateConstraintException() {
        super();
    }

    /**
     * Constructs a new exception with a pre-defined detail message that
     * includes the given parameter values.
     * 
     * @param classEnum
     *            a <code>classEnum</code> specifying the type of the object
     *            which cannot be updated; may be <code>null</code>
     * @param businessKey
     *            the business key of the object which cannot be updated
     */
    public UpdateConstraintException(ClassEnum classEnum, String businessKey) {
        super(generateMessage(classEnum, businessKey), classEnum, businessKey);
    }

    /**
     * Constructs a new exception with the specified detail message. The cause
     * is not initialized.
     * 
     * @param message
     *            the detail message
     */
    public UpdateConstraintException(String message) {
        super(message);
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
    public UpdateConstraintException(String message,
            DomainObjectExceptionBean bean) {
        super(message, bean);
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
    public UpdateConstraintException(String message,
            DomainObjectExceptionBean bean, Throwable cause) {
        super(message, bean, cause);
    }

    private static String generateMessage(ClassEnum classEnum,
            String businessKey) {
        return "Cannot update the object of class '" + classEnum.toString()
                + "' with the businessKey '" + escapeParam(businessKey) + "'";
    }

}
