/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Mike J&auml;ger                                                     
 *                                                                              
 *  Creation Date: 23.04.2009                                                      
 *                                                                              
 *  Completion Time: 17.06.2009                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.logging;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

import org.apache.log4j.Appender;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.RollingFileAppender;

/**
 * Provides functionality to obtain logger objects and to modify their settings,
 * e.g. the log level.
 * 
 * @author Mike J&auml;ger
 * 
 */
public class LoggerFactory {

    public static final String INFO_LOG_LEVEL = "INFO";
    public static final String DEBUG_LOG_LEVEL = "DEBUG";
    public static final String WARN_LOG_LEVEL = "WARN";
    public static final String ERROR_LOG_LEVEL = "ERROR";

    private static HashMap<Class<?>, Log4jLogger> managedLoggers = new HashMap<Class<?>, Log4jLogger>();

    private static final int MAX_BACKUP_INDEX = 5;
    private static final String MAX_FILE_SIZE = "10MB";

    private static String logLevel;
    private static String logFilePath;
    private static String logConfigPath;
    private static RollingFileAppender systemLogAppender;
    private static RollingFileAppender accessLogAppender;
    private static RollingFileAppender auditLogAppender;
    private static RollingFileAppender reverseProxyLogAppender;

    private final static String systemLogAppenderName = "besSystemLogAppender";
    private final static String accessLogAppenderName = "besAccessLogAppender";
    private final static String auditLogAppenderName = "besAuditLogAppender";
    private final static String reverseProxyLogAppenderName = "reverseProxyAppender";

    private static ConsoleAppender consoleAppender;
    private static boolean switchedToFileAppender = false;

    public static Log4jLogger getLogger(Class<?> category) {
        return getLogger(category, Locale.getDefault());
    }

    public static Log4jLogger getLogger(Class<?> category, Locale locale) {
        synchronized (managedLoggers) {
            Log4jLogger logger = new Log4jLogger(category, locale);
            if (switchedToFileAppender) {
                setFileAppendersForLogger(logger);
            } else {
                setConsoleAppenderForLogger(logger);
            }
            if (!managedLoggers.containsKey(category)) {
                managedLoggers.put(category, logger);
            }
            return logger;
        }
    }

    /**
     * Changes the initial ConsoleAppender to a RollingFileAppender using the
     * current configuration settings.
     * 
     * @param logFilePath
     *            The path to the log files.
     * @param logConfigFile
     *            The path to the log4j configuration file.
     * @param logLevel
     *            The log level to be used.
     */
    public static void activateRollingFileAppender(String logFilePath,
            String logConfigFile, String logLevel) {
        synchronized (managedLoggers) {
            try {
                LoggerFactory.logLevel = logLevel;
                LoggerFactory.logFilePath = logFilePath;
                LoggerFactory.logConfigPath = logConfigFile;

                initAppenders();

                Iterator<Class<?>> iterator = managedLoggers.keySet()
                        .iterator();
                while (iterator.hasNext()) {
                    Class<?> loggerName = iterator.next();
                    Log4jLogger logger = managedLoggers.get(loggerName);

                    // initialize the loggers
                    setFileAppendersForLogger(logger);

                }
                switchedToFileAppender = true;
            } catch (IOException e) {
                System.err.println("Log file could not be created!");
            }
        }
    }

    private static void initAppenders() throws IOException {
        systemLogAppender = new RollingFileAppender(getLayout(), logFilePath
                + File.separatorChar + "system.log");
        accessLogAppender = new RollingFileAppender(getLayout(), logFilePath
                + File.separatorChar + "access.log");
        auditLogAppender = new RollingFileAppender(getLayout(), logFilePath
                + File.separatorChar + "audit.log");
        reverseProxyLogAppender = new RollingFileAppender(getLayout(),
                logFilePath + File.separatorChar + "reverseproxy.log");

        // setting the max backup index and file size
        systemLogAppender.setMaxBackupIndex(MAX_BACKUP_INDEX);
        systemLogAppender.setMaxFileSize(MAX_FILE_SIZE);
        systemLogAppender.setName(systemLogAppenderName);

        accessLogAppender.setMaxBackupIndex(MAX_BACKUP_INDEX);
        accessLogAppender.setMaxFileSize(MAX_FILE_SIZE);
        accessLogAppender.setName(accessLogAppenderName);

        auditLogAppender.setMaxBackupIndex(MAX_BACKUP_INDEX);
        auditLogAppender.setMaxFileSize(MAX_FILE_SIZE);
        auditLogAppender.setName(auditLogAppenderName);

        reverseProxyLogAppender.setMaxBackupIndex(MAX_BACKUP_INDEX);
        reverseProxyLogAppender.setMaxFileSize(MAX_FILE_SIZE);
        reverseProxyLogAppender.setName(reverseProxyLogAppenderName);
    }

    private static void setFileAppendersForLogger(Log4jLogger logger) {
        Level level = determineLogLevel(logLevel);

        changeFileAppenderIfNew(logger.systemLogger, systemLogAppenderName,
                systemLogAppender);

        changeFileAppenderIfNew(logger.accessLogger, accessLogAppenderName,
                accessLogAppender);

        changeFileAppenderIfNew(logger.auditLogger, auditLogAppenderName,
                auditLogAppender);

        changeFileAppenderIfNew(logger.proxyLogger,
                reverseProxyLogAppenderName, reverseProxyLogAppender);

        setLogLevel(logger, level);
    }

    private static void changeFileAppenderIfNew(Logger logger,
            String fileAppenderName, RollingFileAppender newFileAppender) {
        Appender existingFileAppender = logger.getAppender(fileAppenderName);
        if (existingFileAppender == null) {
            logger.removeAppender(consoleAppender);
            logger.addAppender(newFileAppender);
        } else if (existingFileAppender != newFileAppender) {
            logger.removeAppender(existingFileAppender);
            logger.addAppender(newFileAppender);
        }
        return;
    }

    private static void setConsoleAppenderForLogger(Log4jLogger logger) {
        if (consoleAppender == null) {
            consoleAppender = new ConsoleAppender(getLayout());
            consoleAppender.setName("OSCM console appender");
        }
        Level level = determineLogLevel(logLevel);

        logger.systemLogger.removeAllAppenders();
        logger.systemLogger.addAppender(consoleAppender);

        logger.accessLogger.removeAllAppenders();
        logger.accessLogger.addAppender(consoleAppender);

        logger.auditLogger.removeAllAppenders();
        logger.auditLogger.addAppender(consoleAppender);

        logger.proxyLogger.removeAllAppenders();
        logger.proxyLogger.addAppender(consoleAppender);

        setLogLevel(logger, level);
    }

    /**
     * Sets the log level for the given logger. If there is no property file to
     * be used, the log level will be set to the value as given in the level
     * parameter.
     * 
     * @param logger
     *            The logger to be modified.
     * @param level
     *            The log level to be set if no property file can be found.
     */
    private static void setLogLevel(Log4jLogger logger, Level level) {
        if (logConfigPath != null && new File(logConfigPath).exists()) {
            PropertyConfigurator.configureAndWatch(logConfigPath, 60000);
        } else {
            logger.systemLogger.setLevel(level);
            logger.auditLogger.setLevel(level);

            // used INFO log level as default for the reverse proxy logger
            logger.proxyLogger.setLevel(Level.INFO);

            // all access operations will be logged with info level
            logger.accessLogger.setLevel(Level.INFO);
        }
    }

    /**
     * Determines the log level represented by the configuration setting. If the
     * stored value does not match any of the supported log levels, the default
     * log level INFO will be used.
     * 
     * @param logLevel
     *            The log level information read from the configuration
     *            settings.
     * @return The log4j compliant log level to be used.
     */
    private static Level determineLogLevel(String logLevel) {
        Level level = Level.INFO;
        if (DEBUG_LOG_LEVEL.equals(logLevel)) {
            level = Level.DEBUG;
        } else if (WARN_LOG_LEVEL.equals(logLevel)) {
            level = Level.WARN;
        } else if (ERROR_LOG_LEVEL.equals(logLevel)) {
            level = Level.ERROR;
        } else if (INFO_LOG_LEVEL.equals(logLevel)) {
            level = Level.INFO;
        }
        return level;
    }

    /**
     * Returns the log message layout according to the product requirements.
     * 
     * @return The message layout.
     */
    private static Layout getLayout() {
        Layout layout = new PatternLayout(
                "%d{MM/dd/yyyy_HH:mm:ss.SSS} FSP_INTS-BSS: %p: ThreadID %t: %c{1}: %m%n");
        return layout;
    }

}
