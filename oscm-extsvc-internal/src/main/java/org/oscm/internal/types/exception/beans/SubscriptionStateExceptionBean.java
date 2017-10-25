/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2010-12-16                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.types.exception.beans;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.oscm.internal.types.exception.SubscriptionStateException;
import org.oscm.internal.types.exception.SubscriptionStateException.Reason;

/**
 * Bean for JAX-WS exception serialization, specific for
 * {@link SubscriptionStateException}.
 * 
 */
@XmlRootElement(name = "SubscriptionStateExceptionBean")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class SubscriptionStateExceptionBean extends ApplicationExceptionBean {

    private static final long serialVersionUID = -759267182359748670L;

    private Reason reason;
    private String member;

    /**
     * Default constructor.
     */
    public SubscriptionStateExceptionBean() {
        super();
    }

    /**
     * Instantiates a <code>SubscriptionStateExceptionBean</code> based on the
     * specified <code>ApplicationExceptionBean</code> and sets the given reason
     * and member field name.
     * 
     * @param sup
     *            the <code>ApplicationExceptionBean</code> to use as the base
     * @param reason
     *            the reason for the exception
     * @param member
     *            the name of the member field that could not be validated
     */
    public SubscriptionStateExceptionBean(ApplicationExceptionBean sup,
            Reason reason, String member) {
        super(sup);
        setReason(reason);
        setMember(member);
    }

    /**
     * Returns the reason for the exception.
     * 
     * @return the reason
     */
    public Reason getReason() {
        return reason;
    }

    /**
     * Sets the reason for the exception.
     * 
     * @param reason
     *            the reason
     */
    public void setReason(Reason reason) {
        this.reason = reason;
    }

    /**
     * Returns the name of the member field that could not be validated.
     * 
     * @return the field name
     */
    public String getMember() {
        return member;
    }

    /**
     * Sets the name of the member field that could not be validated.
     * 
     * @param member
     *            the field name
     */
    public void setMember(String member) {
        this.member = member;
    }
}
