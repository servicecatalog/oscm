/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2010-10-05                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.types.exception;

import javax.ejb.ApplicationException;

import org.oscm.internal.types.exception.beans.ApplicationExceptionBean;
import org.oscm.internal.vo.BaseVO;

/**
 * Exception thrown when an update operation fails due to an outdated value
 * object.
 * 
 */
@ApplicationException(rollback = true)
public class ConcurrentModificationException extends SaaSApplicationException {

    private static final long serialVersionUID = 7041213479558732651L;

    /**
     * Constructs a new exception with <code>null</code> as its detail message.
     * The cause is not initialized.
     */
    public ConcurrentModificationException() {
    }

    /**
     * Constructs a new exception with the specified detail message. The cause
     * is not initialized.
     * 
     * @param message
     *            the detail message
     */
    public ConcurrentModificationException(String message) {
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
    public ConcurrentModificationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new exception with a pre-defined detail message that
     * includes the given parameter values.
     * 
     * @param type
     *            a string specifying the type of the object for which the
     *            exception is thrown
     * @param version
     *            a string specifying the version of the object for which the
     *            exception is thrown
     * 
     */
    public ConcurrentModificationException(String type, int version) {
        super("Outdated " + type + " in version " + version);
    }

    /**
     * Constructs a new exception with a pre-defined detail message that
     * includes the simple name and version obtained from the given value
     * object.
     * 
     * @param vo
     *            the value object for which the exception is thrown
     */
    public ConcurrentModificationException(BaseVO vo) {
        this(vo.getClass().getSimpleName(), vo.getVersion());
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
    public ConcurrentModificationException(String message,
            ApplicationExceptionBean bean) {
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
    public ConcurrentModificationException(String message,
            ApplicationExceptionBean bean, Throwable cause) {
        super(message, bean, cause);
    }

}
