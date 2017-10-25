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

import org.oscm.internal.types.exception.RegistrationException;
import org.oscm.internal.types.exception.RegistrationException.Reason;

/**
 * Bean for JAX-WS exception serialization, specific for
 * {@link RegistrationException}.
 * 
 */
@XmlRootElement(name = "RegistrationExceptionBean")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class RegistrationExceptionBean extends ApplicationExceptionBean {

    private static final long serialVersionUID = 5935101449972866353L;

    private Reason reason;

    /**
     * Default constructor.
     */
    public RegistrationExceptionBean() {
        super();
    }

    /**
     * Instantiates a <code>RegistrationExceptionBean</code> based on the
     * specified <code>ApplicationExceptionBean</code> and sets the given
     * reason.
     * 
     * @param sup
     *            the <code>ApplicationExceptionBean</code> to use as the base
     * @param reason
     *            the reason for the exception
     */
    public RegistrationExceptionBean(ApplicationExceptionBean sup, Reason reason) {
        super(sup);
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
