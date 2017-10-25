/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2012-02-27                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.types.exceptions.beans;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.oscm.types.exceptions.OperationPendingException;
import org.oscm.types.exceptions.OperationPendingException.ReasonEnum;

/**
 * Bean for JAX-WS exception serialization, specific for
 * {@link OperationPendingException}.
 */
@XmlRootElement(name = "OperationPendingExceptionBean")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class OperationPendingExceptionBean extends ApplicationExceptionBean {

    private static final long serialVersionUID = -8340191524480039110L;

    private ReasonEnum reason;

    /**
     * Default constructor.
     */
    public OperationPendingExceptionBean() {
        super();
    }

    /**
     * Instantiates an <code>OperationPendingExceptionBean</code> based on the
     * specified <code>ApplicationExceptionBean</code> and sets the given
     * reason.
     * 
     * @param sup
     *            the <code>ApplicationExceptionBean</code> to use as the base
     * @param reason
     *            the reason for the exception
     */
    public OperationPendingExceptionBean(ApplicationExceptionBean sup,
            ReasonEnum reason) {
        super(sup);
        setReason(reason);
    }

    /**
     * Returns the reason for the exception.
     * 
     * @return the reason
     */
    public ReasonEnum getReason() {
        return reason;
    }

    /**
     * Sets the reason for the exception.
     * 
     * @param reason
     *            the reason
     */
    public void setReason(ReasonEnum reason) {
        this.reason = reason;
    }

}
