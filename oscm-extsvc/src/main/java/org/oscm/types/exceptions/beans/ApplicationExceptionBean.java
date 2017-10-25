/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2010-12-16                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.types.exceptions.beans;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Bean for JAX-WS exception serialization.
 * 
 */
@XmlRootElement(name = "ApplicationExceptionBean")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class ApplicationExceptionBean implements Serializable {

    private static final long serialVersionUID = -7191314116652891671L;

    /**
     * Default constructor.
     */
    public ApplicationExceptionBean() {
    }

    /**
     * Instantiates an <code>ApplicationExceptionBean</code> based on the
     * specified template. The exception ID, causing stack trace, message key,
     * and message parameters are copied from the template.
     * 
     * @param template
     *            the <code>ApplicationExceptionBean</code> to use as the
     *            template
     * 
     */
    public ApplicationExceptionBean(ApplicationExceptionBean template) {
        setId(template.getId());
        setCauseStackTrace(template.getCauseStackTrace());
        setMessageKey(template.getMessageKey());
        setMessageParams(template.getMessageParams());
    }

    private String messageKey;
    private String[] messageParams;
    private String id;
    private String causeStackTrace;

    /**
     * Returns the message key set for the exception. This is the key used for
     * the resource bundle message files.
     * 
     * @return the message key
     */
    public String getMessageKey() {
        return messageKey;
    }

    /**
     * Returns the message parameters set for the exception.
     * 
     * @return the message parameters
     */
    public String[] getMessageParams() {
        return messageParams;
    }

    /**
     * Returns the identifier of the exception.
     * 
     * @return the exception identifier
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the message key for the exception. This is the key used for the
     * resource bundle message files.
     * 
     * @param messageKey
     *            the message key
     */
    public void setMessageKey(String messageKey) {
        this.messageKey = messageKey;
    }

    /**
     * Sets the message parameters for the exception.
     * 
     * @param messageParams
     *            the message parameters
     */
    public void setMessageParams(String[] messageParams) {
        this.messageParams = messageParams;
    }

    /**
     * Sets the identifier of the exception.
     * 
     * @param id
     *            the message identifier
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the causing stack trace set for the exception.
     * 
     * @return the stack trace
     */
    public String getCauseStackTrace() {
        return causeStackTrace;
    }

    /**
     * Sets the causing stack trace for the exception.
     * 
     * @param causeStackTrace
     *            the stack trace
     */
    public void setCauseStackTrace(String causeStackTrace) {
        this.causeStackTrace = causeStackTrace;
    }

}
