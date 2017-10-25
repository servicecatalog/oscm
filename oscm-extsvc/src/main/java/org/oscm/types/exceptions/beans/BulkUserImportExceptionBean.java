/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2011-07-25                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.types.exceptions.beans;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.oscm.types.exceptions.BulkUserImportException;
import org.oscm.types.exceptions.BulkUserImportException.Reason;

/**
 * Bean for JAX-WS exception serialization, specific for
 * {@link BulkUserImportException}.
 */
@XmlRootElement(name = "BulkUserImportExceptionBean")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class BulkUserImportExceptionBean extends ApplicationExceptionBean {

    private static final long serialVersionUID = 6815370179859772857L;

    private Reason reason;

    /**
     * Default constructor.
     */
    public BulkUserImportExceptionBean() {
        super();
    }

    /**
     * Instantiates an <code>BulkUserImportExceptionBean</code> based on the
     * specified <code>ApplicationExceptionBean</code> and sets the given
     * reason.
     * 
     * @param sup
     *            the <code>ApplicationExceptionBean</code> to use as the base
     * @param reason
     *            the reason for the exception
     */
    public BulkUserImportExceptionBean(ApplicationExceptionBean sup,
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
