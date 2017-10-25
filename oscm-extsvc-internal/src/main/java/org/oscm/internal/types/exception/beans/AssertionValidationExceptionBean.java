/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 24.05.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.types.exception.beans;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.oscm.internal.types.exception.AssertionValidationException;
import org.oscm.internal.types.exception.AssertionValidationException.ReasonEnum;

/**
 * Bean for JAX-WS exception serialization, specific for
 * {@link AssertionValidationException}.
 */
@XmlRootElement(name = "AssertionValidationExceptionBean")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class AssertionValidationExceptionBean extends ApplicationExceptionBean {

    private static final long serialVersionUID = 2364945945950942284L;

    private ReasonEnum reason;

    /**
     * Default constructor.
     */
    public AssertionValidationExceptionBean() {
        super();
    }

    /**
     * Instantiates an <code>AssertionValidationExceptionBean</code> based on
     * the specified <code>ApplicationExceptionBean</code> and sets the given
     * reason.
     * 
     * @param sup
     *            the <code>ApplicationExceptionBean</code> to use as the base
     * @param reason
     *            the reason for the exception
     */
    public AssertionValidationExceptionBean(ApplicationExceptionBean sup,
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
