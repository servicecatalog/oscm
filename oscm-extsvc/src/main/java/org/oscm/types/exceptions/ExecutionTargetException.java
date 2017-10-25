/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2010-07-30                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.types.exceptions;

import javax.xml.ws.WebFault;

import org.oscm.intf.TriggerService;
import org.oscm.types.exceptions.beans.ApplicationExceptionBean;

/**
 * Wrapper exception for {@link SaaSSystemException}s that occur in generic
 * execution methods like {@link TriggerService#approveAction(long)}. Such
 * generic executions dispatch to arbitrary API methods and therefore cannot
 * specify a particular exception signature. The original exception is stored as
 * the causing exception.
 * 
 */
@WebFault(name = "ExecutionTargetException", targetNamespace = "http://oscm.org/xsd")
public class ExecutionTargetException extends SaaSApplicationException {

    private static final long serialVersionUID = 4841719254118612514L;

    /**
     * Constructs a new exception with <code>null</code> as its detail message.
     * The cause is not initialized.
     */
    public ExecutionTargetException() {
        super();
    }

    /**
     * Constructs a new exception with the specified detail message. The cause
     * is not initialized.
     * 
     * @param message
     *            the detail message
     */
    public ExecutionTargetException(String message) {
        super(message);
    }

    /**
     * Creates a wrapper for the given application exception.
     * 
     * @param appException
     *            the application exception to wrap
     */
    public ExecutionTargetException(SaaSApplicationException appException) {
        super(appException, appException.getMessageParams());
        setMessageKey(appException.getMessageKey());
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
    public ExecutionTargetException(String message,
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
    public ExecutionTargetException(String message,
            ApplicationExceptionBean bean, Throwable cause) {
        super(message, bean, cause);
    }

}
