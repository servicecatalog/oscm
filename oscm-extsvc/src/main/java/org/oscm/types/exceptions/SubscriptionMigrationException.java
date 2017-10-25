/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2009-06-12                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.types.exceptions;

import javax.xml.bind.annotation.XmlType;
import javax.xml.ws.WebFault;

import org.oscm.types.exceptions.beans.SubscriptionMigrationExceptionBean;

/**
 * Exception thrown when a problem occurs in upgrading or downgrading a
 * subscription.
 */
@WebFault(name = "SubscriptionMigrationException", targetNamespace = "http://oscm.org/xsd")
public class SubscriptionMigrationException extends SaaSApplicationException {

    private static final long serialVersionUID = -4611798608627949699L;

    private SubscriptionMigrationExceptionBean bean = new SubscriptionMigrationExceptionBean();

    /**
     * Constructs a new exception with <code>null</code> as its detail message.
     * The cause is not initialized.
     */
    public SubscriptionMigrationException() {
        super();
    }

    /**
     * Constructs a new exception with the specified detail message. The cause
     * is not initialized.
     * 
     * @param message
     *            the detail message
     */
    public SubscriptionMigrationException(String message) {
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
    public SubscriptionMigrationException(String message,
            SubscriptionMigrationExceptionBean bean) {
        super(message, bean);
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
    public SubscriptionMigrationException(String message,
            SubscriptionMigrationExceptionBean bean, Throwable cause) {
        super(message, bean, cause);
        this.bean = bean;
    }

    /**
     * Constructs a new exception with the given detail message and message
     * parameters, and appends the specified reason to the message key.
     * 
     * @param message
     *            the detail message
     * @param reason
     *            the reason
     * @param params
     *            the message parameters
     */
    public SubscriptionMigrationException(String message, Reason reason,
            Object[] params) {
        super(message, params);
        bean.setReason(reason);
        setMessageKey(getMessageKey() + "." + reason.toString());
    }

    /* javadoc copied from super class */
    @Override
    public SubscriptionMigrationExceptionBean getFaultInfo() {
        return new SubscriptionMigrationExceptionBean(super.getFaultInfo(),
                bean.getReason());
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
     * {@link SubscriptionMigrationException}.
     */
    @XmlType(name = "SubscriptionMigrationException.Reason")
    public enum Reason {

        /**
         * The current service is not compatible to the specified target
         * service.
         */
        INCOMPATIBLE_SERVICES,

        /**
         * A parameter check or update failed.
         */
        PARAMETER,

        /**
         * The constraints for the <code>PERIOD</code> parameter are violated.
         */
        PARAMETER_PERIOD,

        /**
         * The constraints for the <code>NAMED_USER</code> parameter are
         * violated.
         */
        PARAMETER_USERS,

        /**
         * A parameter cannot be updated to the specified value.
         */
        INCOMPATIBLE_PARAMETER;
    }

}
