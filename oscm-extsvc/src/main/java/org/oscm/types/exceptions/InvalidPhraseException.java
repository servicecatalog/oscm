/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2011-07-28                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.types.exceptions;

import javax.xml.ws.WebFault;

import org.oscm.types.exceptions.beans.ApplicationExceptionBean;

/**
 * Exception thrown when the search engine fails to parse the query generated
 * for a search phrase.
 */
@WebFault(name = "InvalidPhraseException", targetNamespace = "http://oscm.org/xsd")
public class InvalidPhraseException extends SaaSApplicationException {

    private static final long serialVersionUID = -8175173207154691192L;

    /**
     * Constructs a new exception with <code>null</code> as its detail message.
     * The cause is not initialized.
     */
    public InvalidPhraseException() {

    }

    /**
     * Constructs a new exception with the specified cause and message
     * parameter.
     * 
     * @param cause
     *            the cause
     * @param phrase
     *            the search phrase for which the exception is thrown and which
     *            is to be set as a message parameter
     * 
     * 
     */
    public InvalidPhraseException(Throwable cause, String phrase) {
        super(cause, new Object[] { phrase });
    }

    /**
     * Constructs a new exception with the specified detail message. The cause
     * is not initialized.
     * 
     * @param message
     *            the detail message
     */
    public InvalidPhraseException(String message) {
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
    public InvalidPhraseException(String message, ApplicationExceptionBean bean) {
        super(message, bean);
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
    public InvalidPhraseException(String message,
            ApplicationExceptionBean bean, Throwable cause) {
        super(message, bean, cause);
    }

}
