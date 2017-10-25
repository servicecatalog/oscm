/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2009-02-23                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.types.exception;

import org.oscm.internal.types.exception.beans.DomainObjectExceptionBean;

/**
 * Exception thrown when a specific domain object cannot be found although its
 * existence is presumed.
 * 
 */
public class ObjectNotFoundException extends DomainObjectException {

    private static final long serialVersionUID = -2956710498872473618L;

    /**
     * Constructs a new exception with <code>null</code> as its detail message.
     * The cause is not initialized.
     */
    public ObjectNotFoundException() {
        super();
    }

    /**
     * Constructs a new exception with a pre-defined detail message that
     * includes the given parameter values.
     * 
     * @param classEnum
     *            a <code>classEnum</code> specifying the type of the object
     *            which cannot be found; may be <code>null</code>
     * @param businessKey
     *            the business key of the object which cannot be found
     */
    public ObjectNotFoundException(ClassEnum classEnum, String businessKey) {
        super(generateMessage(classEnum, businessKey), classEnum, businessKey);
    }

    private static String generateMessage(ClassEnum classEnum,
            String businessKey) {
        if (classEnum == null) {
            return "Could not find object key '" + escapeParam(businessKey)
                    + "'";
        }
        return "Could not find object of type '" + classEnum.toString()
                + "' with business key '" + escapeParam(businessKey) + "'";
    }

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
     * Constructs a new exception with the specified detail message and bean for
     * JAX-WS exception serialization.
     * 
     * @param message
     *            the detail message
     * @param bean
     *            the bean for JAX-WS exception serialization
     */
    public ObjectNotFoundException(String message,
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
    public ObjectNotFoundException(String message,
            DomainObjectExceptionBean bean, Throwable cause) {
        super(message, bean, cause);
    }
}
