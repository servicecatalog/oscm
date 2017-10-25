/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2009-07-02                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.types.exceptions;

import javax.xml.ws.WebFault;

import org.oscm.types.enumtypes.ParameterType;
import org.oscm.types.exceptions.beans.ApplicationExceptionBean;

/**
 * Exception thrown when the constraints of service parameters are violated. For
 * example, the <code>NAMED_USER</code> service parameter is set to 5, but an
 * administrator tries to add a 6th user.
 * 
 */
@WebFault(name = "ServiceParameterException", targetNamespace = "http://oscm.org/xsd")
public class ServiceParameterException extends SaaSApplicationException {

    private static final long serialVersionUID = 1432453406331664837L;

    /**
     * Constructs a new exception with <code>null</code> as its detail message.
     * The cause is not initialized.
     */
    public ServiceParameterException() {
        super();
    }

    /**
     * Constructs a new exception with the specified detail message. The cause
     * is not initialized.
     * 
     * @param message
     *            the detail message
     */
    public ServiceParameterException(String message) {
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
    public ServiceParameterException(String message,
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
    public ServiceParameterException(String message,
            ApplicationExceptionBean bean, Throwable cause) {
        super(message, bean, cause);
    }

    /**
     * Constructs a new exception with the given detail message and message
     * parameters, and appends the specified parameter type and ID to the
     * message key.
     * 
     * @param message
     *            the detail message
     * @param type
     *            the parameter type (platform parameter or service parameter)
     * @param id
     *            the parameter identifier
     * @param params
     *            the message parameters
     */
    public ServiceParameterException(String message, ParameterType type,
            String id, Object[] params) {
        super(message, params);
        String enumName = type.toString();
        enumName = enumName.substring(enumName.lastIndexOf(".") + 1);
        setMessageKey(getMessageKey() + "." + enumName + "." + id);
    }
}
