/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *       
 *  Creation Date: 2009-02-04                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.types.exceptions;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.ws.WebFault;

import org.oscm.types.exceptions.beans.ApplicationExceptionBean;

/**
 * Base class for SaaS application exceptions. Each
 * <code>SaaSApplicationException</code> instance gets an ID to identify it, for
 * example, in log files. In contrast to <code>SaaSSystemExceptions</code>, SaaS
 * application exceptions do not inherit from <code>RuntimeException</code> and
 * are thus checked exceptions.
 * 
 */
@WebFault(name = "SaaSApplicationException", targetNamespace = "http://oscm.org/xsd")
public class SaaSApplicationException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * The prefix for enumerations used in string representations of message
     * parameters.
     */
    public static final String ENUM_PREFIX = "enum.";

    /**
     * The prefix for exceptions used in string representations of message
     * parameters.
     */
    public static final String MESSAGE_PREFIX = "ex.";

    // bean reference for JAX-WS compliance
    private ApplicationExceptionBean bean;

    /**
     * Constructs a new exception with <code>null</code> as its detail message.
     * The cause is not initialized.
     */
    public SaaSApplicationException() {
        this.init();
    }

    /**
     * Constructs a new exception with the specified cause.
     * 
     * @param cause
     *            the cause
     */
    public SaaSApplicationException(Throwable cause) {
        super(cause);
        init();
        this.setCauseStackTrace(cause);
    }

    /**
     * Constructs a new exception with the specified detail message. The cause
     * is not initialized.
     * 
     * @param message
     *            the detail message
     */
    public SaaSApplicationException(String message) {
        super(message);
        init();
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
    public SaaSApplicationException(String message,
            ApplicationExceptionBean bean) {
        super(message);
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
    public SaaSApplicationException(String message,
            ApplicationExceptionBean bean, Throwable cause) {
        super(message, cause);
        this.bean = bean;
    }

    /**
     * Constructs a new exception with the specified message parameters.
     * 
     * @param params
     *            the message parameters
     */
    public SaaSApplicationException(Object[] params) {
        super();
        init(params);
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     * 
     * @param message
     *            the detail message
     * @param cause
     *            the cause
     */
    public SaaSApplicationException(String message, Throwable cause) {
        super(message, cause);
        init();
        this.setCauseStackTrace(cause);
    }

    /**
     * Constructs a new exception with the specified detail message and message
     * parameters.
     * 
     * @param message
     *            the detail message
     * @param params
     *            the message parameters
     */
    public SaaSApplicationException(String message, Object[] params) {
        super(message);
        init(params);
    }

    /**
     * Constructs a new exception with the specified cause and message
     * parameters.
     * 
     * @param cause
     *            the cause
     * @param params
     *            the message parameters
     */
    public SaaSApplicationException(Throwable cause, Object[] params) {
        super(cause);
        init(params);
    }

    /**
     * Constructs a new exception with the specified detail message, cause, and
     * message parameters.
     * 
     * @param message
     *            the detail message
     * @param cause
     *            the cause
     * @param params
     *            the message parameters
     */
    public SaaSApplicationException(String message, Throwable cause,
            Object[] params) {
        super(message, cause);
        init(params);
    }

    /**
     * Returns the exception bean for JAX-WS compliance. All the variable
     * information that is to be received at the client must be passed in the
     * member fields of the bean, not in the exception.
     * 
     * @return the bean for JAX-WS exception serialization
     */
    public ApplicationExceptionBean getFaultInfo() {
        return bean;
    }

    /**
     * Initializes an exception instance (like {@link #init(Object[])}) without
     * setting any parameters.
     */
    private void init() {
        init(null);
    }

    /**
     * Initializes an exception instance with the specified parameters,
     * initializes the message key, and generates a unique exception ID. The ID
     * is based on the system time stamp and thus only unique per millisecond.
     * 
     * @param parameters
     *            the message parameters to set
     */
    private void init(Object[] parameters) {
        this.bean = new ApplicationExceptionBean();
        setId(Long.valueOf(System.currentTimeMillis() % Long.MAX_VALUE)
                .toString());
        String className = getClass().getName();
        bean.setMessageKey(MESSAGE_PREFIX
                + className.substring(className.lastIndexOf(".") + 1));
        bean.setMessageParams(toStringArray(parameters));
    }

    protected static String[] escapeParams(String... params) {
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                params[i] = escapeParam(params[i]);
            }
        }
        return params;
    }

    protected static String escapeParam(String param) {
        if (param != null) {
            param = param.replaceAll("\n", "\\\\n");
        }
        return param;
    }

    /**
     * Keeps the stack trace of the cause (for logging purposes).
     * 
     * @param arg0
     *            the cause
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
    public final void setId(String id) {
        bean.setId(id);
    }

    /**
     * Returns the identifier of this exception.
     * 
     * @return the exception identifier
     */
    public final String getId() {
        return bean.getId();
    }

    /**
     * Sets the causing stack trace for this exception.
     * 
     * @param causeStackTrace
     *            the stack trace of the causing exception
     */
    public final void setCauseStackTrace(String causeStackTrace) {
        bean.setCauseStackTrace(causeStackTrace);
    }

    /**
     * Retrieves the stack trace of the causing exception.
     * 
     * @return the stack trace of the causing exception
     */
    public final String getCauseStackTrace() {
        return bean.getCauseStackTrace();
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

    /**
     * Returns the message parameters to be shown to the user when the exception
     * is thrown.
     * 
     * @return the message parameters as strings
     */
    public final String[] getMessageParams() {
        return bean.getMessageParams();
    }

    /**
     * Sets the message parameters to be shown to the user when the exception is
     * thrown.
     * 
     * @param messageParams
     *            the message parameters as strings
     */
    public final void setMessageParams(String[] messageParams) {
        bean.setMessageParams(messageParams);
    }

    /**
     * Converts the message parameters into their string representation.
     * Enumerations are listed with the prefix 'enum.'.
     * 
     * @param params
     *            the message parameter objects to be transformed
     * @return the message parameters as strings
     */
    private static String[] toStringArray(Object[] params) {
        List<String> result = new ArrayList<String>();
        if (params != null) {
            for (Object p : params) {
                String entry = (p == null) ? null : String.valueOf(p);
                if (p instanceof Enum<?>) {
                    entry = ENUM_PREFIX + p.getClass().getSimpleName() + "."
                            + p;
                }
                result.add(entry);
            }
        }
        return result.toArray(new String[result.size()]);
    }

    /**
     * Returns the message key for this exception. This is the key used for the
     * resource bundle message files.
     * 
     * @return the message key
     */
    public final String getMessageKey() {
        return bean.getMessageKey();
    }

    /**
     * Sets the message key for this exception. This is the key used for the
     * resource bundle message files.
     * 
     * @param messageKey
     *            the message key
     */
    public final void setMessageKey(String messageKey) {
        bean.setMessageKey(messageKey);
    }

}
