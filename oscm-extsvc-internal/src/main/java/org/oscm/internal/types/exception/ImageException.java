/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2011-07-21                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.types.exception;

import javax.xml.bind.annotation.XmlType;

import org.oscm.internal.types.exception.beans.ImageExceptionBean;

/**
 * Exception thrown when the upload of an image fails.
 * 
 */
public class ImageException extends SaaSApplicationException {

    private static final long serialVersionUID = 51716811368055604L;
    private ImageExceptionBean bean = new ImageExceptionBean();

    /**
     * Constructs a new exception with <code>null</code> as its detail message.
     * The cause is not initialized.
     */
    public ImageException() {
        super();
    }

    /**
     * Constructs a new exception with the specified detail message. The cause
     * is not initialized.
     * 
     * @param message
     *            the detail message
     */
    public ImageException(String message) {
        super(message);
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
    public ImageException(String message, ImageExceptionBean bean) {
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
    public ImageException(String message, ImageExceptionBean bean,
            Throwable cause) {
        super(message, bean, cause);
        this.bean = bean;
    }

    /**
     * Constructs a new exception and appends the specified reason to the
     * message key.
     * 
     * @param reason
     *            the reason
     */
    public ImageException(Reason reason) {
        super();
        bean.setReason(reason);
        setMessageKey(getMessageKey() + "." + reason.name());
    }

    /**
     * Constructs a new exception with the given cause, and appends the
     * specified reason to the message key.
     * 
     * @param reason
     *            the reason
     * @param e
     *            the cause
     */
    public ImageException(Reason reason, Throwable e) {
        super(e);
        bean.setReason(reason);
        setMessageKey(getMessageKey() + "." + reason.name());
    }

    /**
     * Constructs a new exception with the given detail message, and appends the
     * specified reason to the message key.
     * 
     * @param message
     *            the detail message
     * @param reason
     *            the reason
     */
    public ImageException(String message, Reason reason) {
        super(message);
        bean.setReason(reason);
        setMessageKey(getMessageKey() + "." + reason.name());
    }

    /**
     * Constructs a new exception with the given detail message and cause, and
     * appends the specified reason to the message key.
     * 
     * @param message
     *            the detail message
     * @param reason
     *            the reason
     * @param e
     *            the cause
     */
    public ImageException(String message, Reason reason, Throwable e) {
        super(message + " Cause: " + e.toString(), e);
        bean.setReason(reason);
        setMessageKey(getMessageKey() + "." + reason.name());
    }

    /**
     * Returns the reason for this exception.
     * 
     * @return the reason
     */
    public Reason getReason() {
        return bean.getReason();
    }

    /* javadoc copied from super class */
    @Override
    public ImageExceptionBean getFaultInfo() {
        return new ImageExceptionBean(super.getFaultInfo(), bean.getReason());
    }

    /**
     * Enumeration of possible reasons for an {@link ImageException}.
     */
    @XmlType(name = "ImageException.Reason")
    public enum Reason {

        /**
         * A problem occurred in the data upload.
         */
        UPLOAD,

        /**
         * You tried to upload an image for an organization that does not have
         * the technology provider, supplier, broker, or reseller role.
         */
        NO_SELLER;

    }

}
