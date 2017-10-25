/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2009-02-10                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.types.exceptions;

import javax.xml.ws.WebFault;

import org.oscm.types.exceptions.beans.DomainObjectExceptionBean;

/**
 * Exception thrown when an object cannot be persisted because another one with
 * the same key already exists.
 * 
 */
@WebFault(name = "NonUniqueBusinessKeyException", targetNamespace = "http://oscm.org/xsd")
public class NonUniqueBusinessKeyException extends DomainObjectException {

    private static final long serialVersionUID = -2296742757368870462L;

    /**
     * Constructs a new exception with <code>null</code> as its detail message.
     * The cause and the <code>classEnum</code> specifying the type of the
     * object that cannot be persisted are not initialized.
     */
    public NonUniqueBusinessKeyException() {
        super();
    }

    /**
     * Constructs a new exception with a pre-defined detail message that
     * includes the given parameter values.
     * 
     * @param classEnum
     *            a <code>classEnum</code> specifying the type of the object
     *            which cannot be persisted
     * @param businessKey
     *            the business key of the object which cannot be persisted
     */
    public NonUniqueBusinessKeyException(ClassEnum classEnum, String businessKey) {
        super(generateMessage(classEnum, businessKey), classEnum, businessKey);
    }

    /**
     * Constructs a new exception with the specified detail message. The cause
     * and the <code>classEnum</code> specifying the type of the object that
     * cannot be persisted are not initialized.
     * 
     * @param message
     *            the detail message
     */
    public NonUniqueBusinessKeyException(String message) {
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
    public NonUniqueBusinessKeyException(String message,
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
    public NonUniqueBusinessKeyException(String message,
            DomainObjectExceptionBean bean, Throwable cause) {
        super(message, bean, cause);
    }

    private static String generateMessage(ClassEnum classEnum,
            String businessKey) {
        return "Object of class '" + classEnum.toString()
                + "' already exists with unique business key value'"
                + escapeParam(businessKey) + "'";
    }

}
