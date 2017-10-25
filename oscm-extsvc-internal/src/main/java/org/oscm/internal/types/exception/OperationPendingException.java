/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2012-02-27                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.types.exception;

import javax.xml.bind.annotation.XmlType;
import org.oscm.internal.types.exception.beans.OperationPendingExceptionBean;

/**
 * Exception thrown when trying to execute an action which conflicts with
 * another pending action. The possible conflicts and affected actions are
 * described in the <code>ReasonEnum</code>.
 */

public class OperationPendingException extends SaaSApplicationException {

    private static final long serialVersionUID = 5174926032591519223L;

    private OperationPendingExceptionBean bean = new OperationPendingExceptionBean();

    /**
     * Constructs a new exception with <code>null</code> as its detail message.
     * The cause is not initialized.
     */
    public OperationPendingException() {
        super();
    }

    /**
     * Constructs a new exception with the specified detail message. The cause
     * is not initialized.
     * 
     * @param message
     *            the detail message
     */
    public OperationPendingException(String message) {
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
    public OperationPendingException(String message,
            OperationPendingExceptionBean bean) {
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
    public OperationPendingException(String message,
            OperationPendingExceptionBean bean, Throwable cause) {
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
    public OperationPendingException(String message, ReasonEnum reason,
            Object[] params) {
        super(message, params);
        bean.setReason(reason);
        setMessageKey(getMessageKey() + "." + reason.toString());
    }

    /* javadoc copied from super class */
    @Override
    public OperationPendingExceptionBean getFaultInfo() {
        return new OperationPendingExceptionBean(super.getFaultInfo(),
                bean.getReason());
    }

    /**
     * Returns the reason for this exception.
     * 
     * @return the reason
     */
    public ReasonEnum getReason() {
        return bean.getReason();
    }

    /**
     * Enumeration of possible reasons for a {@link OperationPendingException}.
     * 
     */
    @XmlType(name = "OperationPendingException.ReasonEnum")
    public static enum ReasonEnum {

        /**
         * The service cannot be activated because another
         * <code>ACTIVATE_SERVICE</code> request with the same service key is
         * pending.
         * 
         */
        ACTIVATE_SERVICE,

        /**
         * The service cannot be deactivated because another
         * <code>DEACTIVATE_SERVICE</code> request with the same service key is
         * pending.
         */
        DEACTIVATE_SERVICE,
        /**
         * The users cannot be added or removed from the subscription because
         * another <code>ADD_REVOKE_USER</code> request is pending for the same
         * subscription and organization and at least one of the users to be
         * added or removed are identical.
         */
        ADD_REVOKE_USER,
        /**
         * The subscription cannot be modified because an
         * <code>UPDGRADE_SUBSCRIPTION</code> request with the same subscription
         * key is pending.
         */
        MODIFY_SUBSCRIPTION,
        /**
         * The customer cannot be registered by the supplier because another
         * <code>REGISTER_CUSTOMER_FOR_SUPPLIER</code> request with the same
         * initial administrator user ID or email address is pending.
         */
        REGISTER_CUSTOMER_FOR_SUPPLIER,
        /**
         * The payment configuration cannot be stored because another
         * <code>SAVE_PAYMENT_CONFIGURATION</code> request is pending for the
         * calling user's organization.
         */
        SAVE_PAYMENT_CONFIGURATION,
        /**
         * The subscription cannot be created because another
         * <code>SUBSCRIBE_TO_SERVICE</code> request with the same subscription
         * ID is pending for the calling user's organization.
         */
        SUBSCRIBE_TO_SERVICE,
        /**
         * The subscription cannot be terminated because another
         * <code>UNSUBSCRIBE_FROM_SERVICE</code> request with the same
         * subscription ID is pending for the calling user's organization.
         */
        UNSUBSCRIBE_FROM_SERVICE,

        /**
         * The subscription cannot be upgraded or downgraded because a
         * <code>MODIFY_SUBSCRIPTION</code> request with the same subscription
         * key is pending.
         */
        UPGRADE_SUBSCRIPTION,

        /**
         * The user cannot be created because another
         * <code>REGISTER_USER_IN_OWN_ORGANIZATION</code> request with the same
         * user ID is pending.
         */
        REGISTER_USER_IN_OWN_ORGANIZATION;
    }
}
