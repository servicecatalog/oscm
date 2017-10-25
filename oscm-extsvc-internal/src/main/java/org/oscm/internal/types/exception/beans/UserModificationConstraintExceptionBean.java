/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2011-12-15                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.types.exception.beans;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.oscm.internal.types.exception.UserModificationConstraintException;
import org.oscm.internal.types.exception.UserModificationConstraintException.Reason;

/**
 * Bean for JAX-WS exception serialization, specific for
 * {@link UserModificationConstraintException}.
 * 
 */
@XmlRootElement(name = "UserModificationConstraintExceptionBean")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class UserModificationConstraintExceptionBean extends
        ApplicationExceptionBean {

    private static final long serialVersionUID = 6511939447367374475L;

    private Reason reason;

    /**
     * Default constructor.
     */
    public UserModificationConstraintExceptionBean() {
        super();
    }

    /**
     * Instantiates a <code>UserModificationConstraintExceptionBean</code> based
     * on the specified <code>ApplicationExceptionBean</code> and sets the given
     * reason.
     * 
     * @param bean
     *            the <code>ApplicationExceptionBean</code> to use as the base
     * @param reason
     *            the reason for the exception
     */
    public UserModificationConstraintExceptionBean(
            ApplicationExceptionBean bean, Reason reason) {
        super(bean);
        setReason(reason);
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
}
