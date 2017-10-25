/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2009-09-21                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.types.exception;

import org.oscm.internal.types.exception.beans.ApplicationExceptionBean;
import org.oscm.internal.types.exception.beans.ServiceCompatibilityExceptionBean;

/**
 * Exception thrown when the upgrade and downgrade options (compatibility) of a
 * marketable service cannot be defined due to constraint violations, or when a
 * marketable service cannot be imported because it is incompatible with the
 * given technical service.
 * 
 */
public class ServiceCompatibilityException extends SaaSApplicationException {

    private static final long serialVersionUID = -7763743033914488493L;

    private final ServiceCompatibilityExceptionBean bean = new ServiceCompatibilityExceptionBean();

    /**
     * Constructs a new exception with <code>null</code> as its detail message.
     * The cause is not initialized.
     */
    public ServiceCompatibilityException() {

    }

    /**
     * Constructs a new exception with the specified detail message. The cause
     * is not initialized.
     * 
     * @param message
     *            the detail message
     */
    public ServiceCompatibilityException(String message) {
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
    public ServiceCompatibilityException(String message,
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
    public ServiceCompatibilityException(String message,
            ApplicationExceptionBean bean, Throwable cause) {
        super(message, bean, cause);
    }

    /**
     * Constructs a new exception with the specified reason set as its detail
     * message and appended to the message key.
     * 
     * @param reason
     *            the reason
     */
    public ServiceCompatibilityException(Reason reason) {
        super(reason.toString());
        setMessageKey(getMessageKey() + "." + reason.toString());
        bean.setReason(reason);
    }

    /**
     * Constructs a new exception with the given detail message, and appends the
     * specified reason to the message key.
     * 
     * @param message
     *            the detail message
     * @param reason
     *            the reason
     */
    public ServiceCompatibilityException(String message, Reason reason) {
        super(message);
        setMessageKey(getMessageKey() + "." + reason.toString());
        bean.setReason(reason);
    }

    /**
     * Constructs a new exception with the given detail message and message
     * parameters, and appends the specified reason to the message key.
     * 
     * @param message
     *            the detail message
     * @param params
     *            the message parameters
     * @param reason
     *            the reason
     */
    public ServiceCompatibilityException(String message, Object[] params,
            Reason reason) {
        super(message, params);
        setMessageKey(getMessageKey() + "." + reason.toString());
        bean.setReason(reason);
    }

    /**
     * Returns the reason for this exception.
     * 
     * @return the reason
     */
    public Reason getReason() {
        return bean.getReason();
    }

    /**
     * Enumeration of possible reasons for a
     * {@link ServiceCompatibilityException}.
     */
    public enum Reason {
        /**
         * The marketable services are not based on the same technical service.
         */
        TECH_SERVICE,

        /**
         * The price models of the services do not match.
         */
        CURRENCY,

        /**
         * The services are not published on the same marketplace.
         */
        MARKETPLACE,

        /**
         * The parameters of the technical service and the marketable service do
         * not match.
         */
        INVALID_PARAMETER,

        /**
         * The parameter options of the technical service and the marketable
         * service do not match.
         */
        INVALID_PARAMETER_OPTION,

        /**
         * The events of the technical service and the marketable service do not
         * match.
         */
        INVALID_EVENT,

        /**
         * The service roles of the technical service and the marketable service
         * do not match.
         */
        INVALID_PRICED_ROLE
    }
}
