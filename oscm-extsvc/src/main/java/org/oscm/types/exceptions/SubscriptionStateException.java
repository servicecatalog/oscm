/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2009-08-18                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.types.exceptions;

import java.util.Arrays;

import javax.xml.bind.annotation.XmlType;
import javax.xml.ws.WebFault;

import org.oscm.types.enumtypes.SubscriptionStatus;
import org.oscm.types.exceptions.beans.SubscriptionStateExceptionBean;

/**
 * Exception thrown when an operation cannot be performed due to the current
 * status of a subscription.
 * 
 */
@WebFault(name = "SubscriptionStateException", targetNamespace = "http://oscm.org/xsd")
public class SubscriptionStateException extends SaaSApplicationException {

    private static final long serialVersionUID = 9069691369666911176L;

    private SubscriptionStateExceptionBean bean = new SubscriptionStateExceptionBean();

    /**
     * Constructs a new exception with <code>null</code> as its detail message.
     * The cause is not initialized.
     */
    public SubscriptionStateException() {
        super();
    }

    /**
     * Constructs a new exception with the specified detail message. The cause
     * is not initialized.
     * 
     * @param message
     *            the detail message
     */
    public SubscriptionStateException(String message) {
        super(message);
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
    public SubscriptionStateException(String message, Reason reason) {
        super(message);
        bean.setReason(reason);
        setMessageKey(initMessageKey());
    }

    /**
     * Constructs a new exception with a generated detail message and the given
     * message parameters, and appends the specified reason to the message key.
     * The generated detail message depends on the specified reason, the
     * subscription member field that contains invalid data, and the message
     * parameters.
     * 
     * @param reason
     *            the reason
     * @param member
     *            the subscription member field that could not be validated
     * @param params
     *            the message parameters
     * 
     */
    public SubscriptionStateException(Reason reason, String member,
            Object[] params) {
        super(getMessage(reason, member, params), params);
        bean.setReason(reason);
        bean.setMember(member);
        setMessageKey(initMessageKey());
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
    public SubscriptionStateException(String message,
            SubscriptionStateExceptionBean bean) {
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
    public SubscriptionStateException(String message,
            SubscriptionStateExceptionBean bean, Throwable cause) {
        super(message, bean, cause);
        this.bean = bean;
    }

    private static String getMessage(Reason reason, String member,
            Object[] params) {
        final String memberstr;
        if (member == null) {
            memberstr = "";
        } else {
            memberstr = " for member " + member;
        }
        final String paramsstr;
        if (params == null) {
            paramsstr = "";
        } else {
            paramsstr = escapeParam(String.format(" (parameters=%s)",
                    Arrays.asList(params)));
        }
        return String.format("Validation failed%s with reason %s%s.",
                memberstr, reason, paramsstr);
    }

    private String initMessageKey() {
        Reason reason = bean.getReason();
        String enumName = reason.toString();
        return super.getMessageKey() + "." + enumName;
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
     * Returns the name of the member field that could not be validated.
     * 
     * @return the field name
     */
    public String getMember() {
        return bean.getMember();
    }

    /* javadoc copied from super class */
    @Override
    public SubscriptionStateExceptionBean getFaultInfo() {
        return new SubscriptionStateExceptionBean(super.getFaultInfo(),
                bean.getReason(), bean.getMember());
    }

    /**
     * Enumeration of possible reasons for a {@link SubscriptionStateException}.
     * 
     */
    @XmlType(name = "SubscriptionStateException.Reason")
    public enum Reason {

        /**
         * The operation to be performed is not allowed for invalid
         * subscriptions.
         */
        INVALID,

        /**
         * The operation to be performed is not allowed for pending
         * subscriptions.
         */
        PENDING,

        /**
         * The operation to be performed is only allowed for pending
         * subscriptions.
         */
        ONLY_PENDING,

        /**
         * The operation to be performed is only allowed for active
         * subscriptions.
         */
        ONLY_ACTIVE,

        /**
         * The operation to be performed is only allowed for subscriptions with
         * pending update, in state {@link SubscriptionStatus#SUSPENDED_UPD} or
         * {@link SubscriptionStatus#PENDING_UPD}.
         */
        ONLY_UPD,

        /**
         * The price model cannot be changed if the subscription is deactivated,
         * invalid, or expired.
         */
        SUBSCRIPTION_STATE_CHANGED,

        /**
         * The operation to be performed is not allowed with the subscription's
         * current status.
         */
        SUBSCRIPTION_INVALID_STATE,

        /**
         * The operation to be performed is only allowed for subscriptions which
         * are currently being modified in asynchronous mode.
         */
        NOT_IN_UPDATING,

        /**
         * The operation to be performed is only allowed for subscriptions which
         * are currently being upgraded or downgraded in asynchronous mode.
         */
        NOT_IN_UPGRADING;
    }
}
