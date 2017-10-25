/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2012-02-27                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.types.exception.beans;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.oscm.internal.types.exception.DigitalSignatureValidationException;
import org.oscm.internal.types.exception.DigitalSignatureValidationException.ReasonEnum;

/**
 * Bean for JAX-WS exception serialization, specific for
 * {@link DigitalSignatureValidationException}.
 */
@XmlRootElement(name = "DigitalSignatureValidationExceptionBean")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class DigitalSignatureValidationExceptionBean extends
        ApplicationExceptionBean {

    private static final long serialVersionUID = -8340191524480039110L;

    private ReasonEnum reason;

    /**
     * Default constructor.
     */
    public DigitalSignatureValidationExceptionBean() {
        super();
    }

    /**
     * Instantiates a <code>DigitalSignatureValidationExceptionBean</code> based
     * on the specified <code>ApplicationExceptionBean</code> and sets the given
     * reason.
     * 
     * @param sup
     *            the <code>ApplicationExceptionBean</code> to use as the base
     * @param reason
     *            the reason for the exception
     */
    public DigitalSignatureValidationExceptionBean(
            ApplicationExceptionBean sup, ReasonEnum reason) {
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
