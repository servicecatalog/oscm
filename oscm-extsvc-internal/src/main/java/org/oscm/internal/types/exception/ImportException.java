/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2009-03-20                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.types.exception;

import org.oscm.internal.types.exception.beans.ImportExceptionBean;

/**
 * Exception thrown when the import of a technical or marketable service
 * definition fails.
 * 
 */
public class ImportException extends SaaSApplicationException {

    private static final long serialVersionUID = 3043864979786939426L;
    private ImportExceptionBean bean = new ImportExceptionBean();

    /**
     * Constructs a new exception with <code>null</code> as its detail message.
     * The cause is not initialized.
     */
    public ImportException() {
        super();
    }

    /**
     * Constructs a new exception with a pre-defined message text and the
     * specified details.
     * 
     * @param details
     *            the detailed information
     */
    public ImportException(String details) {
        super("Import Failed", new Object[] { details });
        bean.setDetails(details);
    }

    /**
     * Constructs a new exception with the specified detail message and bean for
     * JAX-WS exception serialization.
     * 
     * @param message
     *            the detail message
     * @param bean
     *            the bean for JAX-WS exception serialization
     */
    public ImportException(String message, ImportExceptionBean bean) {
        super(message, bean);
        this.bean = bean;
    }

    /**
     * Constructs a new exception with the specified detail message, cause, and
     * bean for JAX-WS exception serialization.
     * 
     * @param message
     *            the detail message
     * @param bean
     *            the bean for JAX-WS exception serialization
     * @param cause
     *            the cause
     */
    public ImportException(String message, ImportExceptionBean bean,
            Throwable cause) {
        super(message, bean, cause);
        this.bean = bean;
    }

    /**
     * Returns the detailed information provided with this exception.
     * 
     * @return the detailed information
     */
    public String getDetails() {
        return bean.getDetails();
    }

    /* javadoc is copied from super class */
    @Override
    public ImportExceptionBean getFaultInfo() {
        return new ImportExceptionBean(super.getFaultInfo(), bean.getDetails());
    }

}
