/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 05.06.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.types.exception.beans;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.oscm.internal.types.exception.SAMLRedirectURLException.ReasonEnum;

/**
 * @author roderus
 *
 */
/**
 * Bean for JAX-WS exception serialization, specific for
 * {@link SAMLRedirectURLExceptionBean}.
 */
@XmlRootElement(name = "SAMLRedirectURLExceptionBean")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class SAMLRedirectURLExceptionBean extends ApplicationExceptionBean {

    private static final long serialVersionUID = -8340191524480039110L;

    private ReasonEnum reason;

    /**
     * Default constructor.
     */
    public SAMLRedirectURLExceptionBean() {
        super();
    }

    /**
     * Instantiates a <code>SAMLRedirectURLExceptionBean</code> based on the
     * specified <code>ApplicationExceptionBean</code> and sets the given
     * reason.
     * 
     * @param sup
     *            the <code>ApplicationExceptionBean</code> to use as the base
     * @param reason
     *            the reason for the exception
     */
    public SAMLRedirectURLExceptionBean(ApplicationExceptionBean sup,
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
