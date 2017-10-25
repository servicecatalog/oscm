/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Mike J&auml;ger                                                      
 *                                                                              
 *  Creation Date: 22.04.2009                                                      
 *                                                                              
 *  Completion Time: 17.06.2009                                             
 *                                                                              
 *******************************************************************************/

package org.oscm.logging;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import org.oscm.types.enumtypes.LogMessageIdentifier;

/**
 * This utility class provides the logging functionality to be used by the
 * entire ADM UE application.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class Log4jLogger {

    private static final String LOG_MESSAGE_RESOURCE_BASENAME = "LogMessages";

    private static final String SYSTEM_LOG_CATEGORY_SUFFIX = "sys";
    private static final String ACCESS_LOG_CATEGORY_SUFFIX = "acc";
    private static final String AUDIT_LOG_CATEGORY_SUFFIX = "audit";
    private static final String REVERSEPROXY_LOG_CATEGORY_SUFFIX = "prx";

    /**
     * Indicates that the log information should be written to the system.log
     * file.
     */
    public static final int SYSTEM_LOG = 1;

    /**
     * Indicates that the log information should be written to the access.log
     * file.
     */
    public static final int ACCESS_LOG = 2;

    /**
     * Indicates that the log information should be written to the audit.log
     * file.
     */
    public static final int AUDIT_LOG = 4;

    /**
     * Indicates that the log information should be written to the
     * reverseproxy.log file.
     */
    public static final int PROXY_LOG = 8;

    protected Locale locale;

    Logger systemLogger;
    Logger accessLogger;
    Logger auditLogger;
    Logger proxyLogger;

    /**
     * Initializes the logger to write to the console, so that even in case the
     * configuration does not contain correct settings the log information is
     * not lost.
     */
    Log4jLogger(Class<?> category, Locale locale) {
        this.locale = locale;
        systemLogger = Logger.getLogger(SYSTEM_LOG_CATEGORY_SUFFIX + "."
                + category.getName());
        systemLogger.setAdditivity(false);
        accessLogger = Logger.getLogger(ACCESS_LOG_CATEGORY_SUFFIX + "."
                + category.getName());
        accessLogger.setAdditivity(false);
        auditLogger = Logger.getLogger(AUDIT_LOG_CATEGORY_SUFFIX + "."
                + category.getName());
        auditLogger.setAdditivity(false);
        proxyLogger = Logger.getLogger(REVERSEPROXY_LOG_CATEGORY_SUFFIX + "."
                + category.getName());
        proxyLogger.setAdditivity(false);
    }

    /**
     * Logs an information with log type {@link Level#INFO}. The information is
     * written to the log files specified.
     * 
     * @param logTargets
     *            The target log files to be used.
     * @param identifier
     *            The message identifier to be logged.
     * @param params
     *            The parameters for the place holders of the message
     */
    public void logInfo(int logTargets, LogMessageIdentifier identifier,
            String... params) {
        String message = getLogMessageText(identifier, params);
        if (logToSystemLog(logTargets)) {
            systemLogger.info(message);
        }
        if (logToAccessLog(logTargets)) {
            accessLogger.info(message);
        }
        if (logToAuditLog(logTargets)) {
            auditLogger.info(message);
        }
        if (logToReverseProxyLog(logTargets)) {
            proxyLogger.info(message);
        }
    }

    /**
     * Logs an information with log type {@link Level#DEBUG}.
     * 
     * @param message
     *            The message to be logged.
     * @param logTargets
     *            The target log files to be used.
     */
    public void logDebug(String message, int logTargets) {
        String msgWithId = addMsgIdToMsg(LogMessageIdentifier.DEBUG, message);
        if (logToSystemLog(logTargets)) {
            systemLogger.debug(msgWithId);
        }
        if (logToAccessLog(logTargets)) {
            accessLogger.debug(msgWithId);
        }
        if (logToAuditLog(logTargets)) {
            auditLogger.debug(msgWithId);
        }
        if (logToReverseProxyLog(logTargets)) {
            proxyLogger.debug(msgWithId);
        }
    }

    /**
     * Logs information with log type {@link Level#DEBUG}. The information is
     * written to the system.log file.
     * 
     * @param message
     *            The message to be logged.
     */
    public void logDebug(String message) {
        String msgWithId = addMsgIdToMsg(LogMessageIdentifier.DEBUG, message);
        systemLogger.debug(msgWithId);
    }

    /**
     * Logs an information with log type {@link Level#WARN}. The information is
     * written to the log files specified.
     * 
     * @param logTargets
     *            The target log files to be used.
     * @param identifier
     *            The message identifier to be logged.
     * @param params
     *            The parameters for the place holders of the message
     */
    public void logWarn(int logTargets, LogMessageIdentifier identifier,
            String... params) {
        String message = getLogMessageText(identifier, params);
        if (logToSystemLog(logTargets)) {
            systemLogger.warn(message);
        }
        if (logToAccessLog(logTargets)) {
            accessLogger.warn(message);
        }
        if (logToAuditLog(logTargets)) {
            auditLogger.warn(message);
        }
        if (logToReverseProxyLog(logTargets)) {
            proxyLogger.warn(message);
        }
    }

    /**
     * Logs information with log type {@link Level#ERROR}. The information is
     * written to the system.log file.
     * 
     * @param identifier
     *            The message identifier to be logged.
     * @param params
     *            The parameters for the place holders of the message
     */
    public void logError(LogMessageIdentifier identifier, String... params) {
        String message = getLogMessageText(identifier, params);
        systemLogger.error(message);
    }

    /**
     * Logs an information with log type {@link Level#WARN}. The information is
     * written to the log files specified.
     * 
     * @param logTargets
     *            The target log files to be used.
     * @param e
     *            Throwable to be listed in the log message.
     * @param identifier
     *            The message identifier to be logged.
     * @param params
     *            The parameters for the place holders of the message
     */
    public void logWarn(int logTargets, Throwable e,
            LogMessageIdentifier identifier, String... params) {
        String message = getLogMessageText(identifier, params);
        if (logToSystemLog(logTargets)) {
            systemLogger.warn(message, e);
        }
        if (logToAccessLog(logTargets)) {
            accessLogger.warn(message);
        }
        if (logToAuditLog(logTargets)) {
            auditLogger.warn(message);
        }
        if (logToReverseProxyLog(logTargets)) {
            proxyLogger.warn(message);
        }
    }

    /**
     * Logs an information with log type {@link Level#ERROR}. The information is
     * written to the log files specified.
     * 
     * @param logTargets
     *            The target log files to be used.
     * @param e
     *            Throwable to be listed in the log message.
     * @param identifier
     *            The message identifier to be logged.
     * @param params
     *            The parameters for the place holders of the message
     */
    public void logError(int logTargets, Throwable e,
            LogMessageIdentifier identifier, String... params) {
        String message = getLogMessageText(identifier, params);
        if (logToSystemLog(logTargets)) {
            systemLogger.error(message, e);
        }
        if (logToAccessLog(logTargets)) {
            accessLogger.error(message, e);
        }
        if (logToAuditLog(logTargets)) {
            auditLogger.error(message, e);
        }
        if (logToReverseProxyLog(logTargets)) {
            proxyLogger.error(message, e);
        }
    }

    private boolean logToAuditLog(int logTargets) {
        return (AUDIT_LOG & logTargets) > 0;
    }

    private boolean logToAccessLog(int logTargets) {
        return (ACCESS_LOG & logTargets) > 0;
    }

    private boolean logToSystemLog(int logTargets) {
        return (SYSTEM_LOG & logTargets) > 0;
    }

    private boolean logToReverseProxyLog(int logTargets) {
        return (PROXY_LOG & logTargets) > 0;
    }

    /**
     * Returns whether debug logging is enabled or not.
     * 
     * @return <code>true</code> in case debug logging is enabled,
     *         <code>false</code> otherwise.
     */
    public boolean isDebugLoggingEnabled() {
        return systemLogger.isDebugEnabled();
    }

    protected String getLogMessageText(LogMessageIdentifier identifier,
            String... params) {
        String text;
        try {
            String msgId = identifier.getMsgId();
            ResourceBundle bundle = getResourceBundle();
            text = bundle.getString(msgId);
            if (params != null) {
                MessageFormat mf = new MessageFormat(text, bundle.getLocale());
                params = escapeParams(params);
                text = mf.format(params, new StringBuffer(), null).toString();
            }
            text = addMsgIdToMsg(identifier, text);
        } catch (MissingResourceException e) {
            text = "?? key '" + identifier.name() + "' not found ??";
        }

        return text;
    }

    private String[] escapeParams(String... params) {
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                if (params[i] != null) {
                    params[i] = params[i].replaceAll("\n", "\\\\n");
                }
            }
        }
        return params;
    }

    private ResourceBundle getResourceBundle() {
        ResourceBundle bundleForLog;
        try {
            bundleForLog = ResourceBundle.getBundle(
                    LOG_MESSAGE_RESOURCE_BASENAME, locale);
        } catch (MissingResourceException e) {
            bundleForLog = ResourceBundle
                    .getBundle(LOG_MESSAGE_RESOURCE_BASENAME);
        }
        return bundleForLog;
    }

    private String addMsgIdToMsg(LogMessageIdentifier identifier, String msg) {
        return identifier.getMsgId() + ": " + msg;
    }

}
