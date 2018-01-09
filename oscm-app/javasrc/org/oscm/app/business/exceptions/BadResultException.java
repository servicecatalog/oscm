/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *       
 *  Creation Date: 2012-08-21                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.business.exceptions;

import org.oscm.provisioning.data.BaseResult;

/**
 * Exception used to simplify the control flow for nested errors that need to be
 * reported as <code>BaseResult</code> objects. A <code>BaseResult</code> object
 * provides the basic data returned to the platform upon calls to a provisioning
 * service. This basic data consists in a return code and a status message.
 */
public class BadResultException extends Exception {

    private static final long serialVersionUID = -2087997298487791753L;

    /**
     * Constructs a new exception with the specified detail message and
     * parameters.
     * 
     * @param message
     *            the detail message
     * @param args
     *            the message parameters
     * 
     */
    public BadResultException(String message, Object... args) {
        super(String.format(message, args));
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     * 
     * @param message
     *            the detail message
     * @param cause
     *            the cause
     */
    public BadResultException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Returns a <code>BaseResult</code> object with the return code and status
     * message for an operation.
     * 
     * @param type
     *            the type of <code>BaseResult</code> to return; this can be an
     *            <code>AbstractBaseResult</code>, an
     *            <code>InstanceResult</code>, or a <code>UserResult</code>
     * @return a <code>BaseResult</code> object
     */
    public <T extends BaseResult> T getResult(Class<T> type) {
        try {
            T result = type.newInstance();
            result.setRc(1);
            result.setDesc(getMessage());
            return result;
        } catch (InstantiationException e) {
            // Must not happen with BaseResults
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            // Must not happen with BaseResults
            throw new RuntimeException(e);
        }
    }

}
