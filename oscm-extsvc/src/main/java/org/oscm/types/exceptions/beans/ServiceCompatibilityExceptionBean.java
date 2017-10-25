/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2012-02-22                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.types.exceptions.beans;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.oscm.types.exceptions.ServiceCompatibilityException;
import org.oscm.types.exceptions.ServiceCompatibilityException.Reason;

/**
 * Bean for JAX-WS exception serialization, specific for
 * {@link ServiceCompatibilityException}.
 */
@XmlRootElement(name = "ServiceCompatibilityExceptionBean")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class ServiceCompatibilityExceptionBean extends ApplicationExceptionBean {

    private static final long serialVersionUID = 397354781229340191L;

    private Reason reason;

    /**
     * Default constructor.
     */
    public ServiceCompatibilityExceptionBean() {
        super();
    }

    /**
     * Instantiates a <code>ServiceCompatibilityExceptionBean</code> based on
     * the specified <code>ApplicationExceptionBean</code> and sets the given
     * reason.
     * 
     * @param sup
     *            the <code>ApplicationExceptionBean</code> to use as the base
     * @param reason
     *            the reason for the exception
     */
    public ServiceCompatibilityExceptionBean(ApplicationExceptionBean sup,
            Reason reason) {
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
