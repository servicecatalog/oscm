/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.auditlog.util;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.TimeZone;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.auditlog.model.AuditLog;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.internal.types.exception.SaaSSystemException;

public class AuditLogSerializer {
    private static final Log4jLogger logger = LoggerFactory
            .getLogger(AuditLogSerializer.class);

    private ResourceBundle resourceBundle;

    public static ThreadLocal<DateFormat> DATE_FORMATTER = new ThreadLocal<DateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            SimpleDateFormat format = new SimpleDateFormat(
                    "MM/dd/YYYY_HH:mm:ss.SSS");
            format.setTimeZone(TimeZone.getDefault());
            return format;
        }
    };

    public static final String AUDITLOG_MESSAGE_RESOURCE_NAME = "AuditLogMessages";
    public static final String USER_ID = "userId";
    public static final String COMPONENT_NAME = "FSP_SW/OSCM_CTMG-BSS";
    public static final String LOG_LEVEL = "INFO";
    public static final String ORGANIZATION_ID = "orgId";
    public static final String ORGANIZATION_NAME = "orgName";
    static ThreadLocal<MessageFormat> LOG_PATTERN = new ThreadLocal<MessageFormat>() {
        @Override
        protected MessageFormat initialValue() {
            return new MessageFormat("{0} " + COMPONENT_NAME + ": " + LOG_LEVEL
                    + ": {1}: {2}, " + USER_ID + "={3}|" + ORGANIZATION_ID
                    + "={4}|" + ORGANIZATION_NAME + "={5}{6}");
        }
    };

    public AuditLogSerializer() {
        resourceBundle = ResourceBundle.getBundle(
                AUDITLOG_MESSAGE_RESOURCE_NAME, Locale.ENGLISH);
    }

    public byte[] serialize(List<AuditLog> auditLogs) {
        StringBuffer result = new StringBuffer();
        for (AuditLog auditLog : auditLogs) {
            Object[] formatArguments = new Object[7];
            formatArguments[0] = getCreationTimeAsStr(auditLog
                    .getCreationTime());
            formatArguments[1] = auditLog.getOperationId();
            formatArguments[2] = getMessageText(auditLog.getOperationId());
            formatArguments[3] = addDoubleQuotesForString(
            		auditLog.getUserId());
            formatArguments[4] = addDoubleQuotesForString(
            		auditLog.getOrganizationId());
            formatArguments[5] = addDoubleQuotesForString(
            		auditLog.getOrganizationName());
            formatArguments[6] = auditLog.getLog();

            result.append(LOG_PATTERN.get().format(formatArguments));
            result.append(System.getProperty("line.separator"));
        }
        try {
            return result.toString().getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            SaaSSystemException sse = new SaaSSystemException(
                    "Unexpected encode exception for loggging", e);
            logger.logError(Log4jLogger.SYSTEM_LOG, sse,
                    LogMessageIdentifier.ERROR_NOT_SUPPORTED_ENCODING_UTF8);
            throw sse;
        }
    }

    String getCreationTimeAsStr(long creationTime) {
        return DATE_FORMATTER.get().format(new Date(creationTime));
    }
    
    private String addDoubleQuotesForString(String come){
    	return (char)34+come+(char)34;
    }
    
    private String getMessageText(String messageId) {
        return resourceBundle.getString(messageId);
    }
}
