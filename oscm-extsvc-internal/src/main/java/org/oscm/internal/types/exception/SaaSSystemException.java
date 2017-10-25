/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2009-02-04                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.internal.types.exception;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Base class for SaaS system exceptions. Each <code>SaaSSystemException</code>
 * instance gets an ID to identify it, for example, in log files.
 * 
 */
public class SaaSSystemException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private String id;
    private String causeStackTrace;

    /**
     * Constructs a new exception with <code>null</code> as its detail message.
     * The cause is not initialized.
     */
    public SaaSSystemException() {

    }

    /**
     * Constructs a new exception with the specified detail message. The cause
     * is not initialized.
     * 
     * @param arg0
     *            the detail message
     */
    public SaaSSystemException(String arg0) {
        super(arg0);
        genId();
    }

    /**
     * Constructs a new exception with the specified cause.
     * 
     * @param arg0
     *            the cause
     */
    public SaaSSystemException(Throwable arg0) {
        super(arg0);
        genId();
        this.setCauseStackTrace(arg0);
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     * 
     * @param arg0
     *            the detail message
     * @param arg1
     *            the cause
     */
    public SaaSSystemException(String arg0, Throwable arg1) {
        super(arg0, arg1);
        genId();
        this.setCauseStackTrace(arg1);
    }

    /**
     * Generates a unique exception ID. The current implementation is based on
     * the system time stamp and is therefore only unique per millisecond (and
     * for about 317 years)
     */
    private void genId() {
        setId(Long.valueOf(System.currentTimeMillis() % Long.MAX_VALUE)
                .toString());
    }

    /**
     * Keep stack trace of cause (for logging purpose)
     * 
     * @param arg0
     */
    private void setCauseStackTrace(Throwable arg0) {
        if (arg0 != null) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            arg0.printStackTrace(pw);
            setCauseStackTrace(sw.getBuffer().toString());
        }
    }

    /**
     * Sets the identifier for this exception. The ID is used for tracking the
     * information, for example, in log files.
     * 
     * @param id
     *            the exception identifier
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the identifier of this exception.
     * 
     * @return the exception identifier
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the causing stack trace for this exception.
     * 
     * @param causeStackTrace
     *            The stack trace of the causing exception
     */
    public void setCauseStackTrace(String causeStackTrace) {
        this.causeStackTrace = causeStackTrace;
    }

    /**
     * Retrieves the stack trace of the causing exception.
     * 
     * @return the stack trace of the causing exception
     */
    public String getCauseStackTrace() {
        return causeStackTrace;
    }

    /**
     * Returns the detail message of this exception, preceded by the exception
     * ID.
     * 
     * @return the detail message
     */
    @Override
    public String getMessage() {
        return "EXCEPTIONID " + getId() + ": " + super.getMessage();
    }

}
