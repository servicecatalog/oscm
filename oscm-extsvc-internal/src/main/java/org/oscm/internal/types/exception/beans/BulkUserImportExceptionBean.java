/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2014-04-02                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.types.exception.beans;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.oscm.internal.types.exception.BulkUserImportException;
import org.oscm.internal.types.exception.BulkUserImportException.Reason;

/**
 * Bean for JAX-WS exception serialization, specific for
 * {@link BulkUserImportException}.
 * 
 */
@XmlRootElement(name = "BulkUserImportExceptionBean")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class BulkUserImportExceptionBean extends ApplicationExceptionBean {

    private static final long serialVersionUID = 6180688833602863439L;

    private Reason reason;

    /**
     * Default constructor.
     */
    public BulkUserImportExceptionBean() {
        super();
    }

    /**
     * Instantiates a <code>BulkUserImportExceptionBean</code> based on the
     * specified <code>ApplicationExceptionBean</code> and sets the given reason
     * and member field name.
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
