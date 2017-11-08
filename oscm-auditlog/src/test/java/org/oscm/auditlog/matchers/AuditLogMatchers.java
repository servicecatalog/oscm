/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.auditlog.matchers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import org.oscm.auditlog.model.AuditLog;
import org.oscm.auditlog.model.AuditLogEntry;
import org.oscm.auditlog.util.AuditLogSerializer;

/**
 * Hamcrest matchers for the audit logging.
 * 
 * @author Enes Sejfi
 */
@SuppressWarnings("unchecked")
public class AuditLogMatchers {

    public static Matcher<List<AuditLogEntry>> isSameAs(
            final List<AuditLog> auditLogs) {
        return new BaseMatcher<List<AuditLogEntry>>() {
            private int errorPosition;

            @Override
            public boolean matches(Object object) {
                List<AuditLogEntry> auditLogEntries = (List<AuditLogEntry>) object;

                assertEquals(auditLogEntries.size(), auditLogs.size());
                for (int i = 0; i < auditLogEntries.size(); i++) {
                    errorPosition = i;
                    compareAuditLogEntry(auditLogEntries.get(i),
                            auditLogs.get(i));
                }
                return true;
            }

            @Override
            public void describeTo(Description description) {
                description
                        .appendText("AuditLogEntry is not equal with AuditLog at position "
                                + errorPosition);
            }
        };
    }

    public static void compareAuditLogEntry(AuditLogEntry auditLogEntry,
            AuditLog dbAuditLog) {
        assertEquals(auditLogEntry.getLog(), dbAuditLog.getLog());
        assertEquals(auditLogEntry.getOperationName(),
                dbAuditLog.getOperationName());
        assertEquals(auditLogEntry.getOrganizationId(),
                dbAuditLog.getOrganizationId());
        assertEquals(auditLogEntry.getOrganizationName(),
                dbAuditLog.getOrganizationName());
        assertEquals(auditLogEntry.getUserId(), dbAuditLog.getUserId());
    }

    public static Matcher<List<AuditLog>> containCreationTimes(
            final String operationId, final long[] creationTimes) {
        return new BaseMatcher<List<AuditLog>>() {
            private int errorPosition;

            @Override
            public boolean matches(Object object) {
                List<AuditLog> auditLogs = (List<AuditLog>) object;

                assertEquals(creationTimes.length, auditLogs.size());
                for (int i = 0; i < auditLogs.size(); i++) {
                    assertEquals(creationTimes[i], auditLogs.get(i)
                            .getCreationTime());
                }
                for (AuditLog auditLog : auditLogs) {
                    compareAuditLog(auditLog, operationId);
                }

                return true;
            }

            @Override
            public void describeTo(Description description) {
                description
                        .appendText("AuditLogEntry is not equal with AuditLog at position "
                                + errorPosition);
            }
        };
    }
    
    
    public static Matcher<List<AuditLog>> sortedCreationTimes() {
        return new BaseMatcher<List<AuditLog>>() {

            @Override
            public boolean matches(Object object) {
                List<AuditLog> auditLogs = (List<AuditLog>) object;

                for (int i = 0; i < auditLogs.size()-1; i++) {
                    assertTrue(auditLogs.get(i).getCreationTime()<=auditLogs.get(i+1).getCreationTime());
                }

                return true;
            }

            @Override
            public void describeTo(Description description) {
                description
                        .appendText("AuditLogEntry List not sorted on creation time.");
            }
        };
    }

    public static Matcher<List<AuditLog>> notSortedCreationTimes() {
        return new BaseMatcher<List<AuditLog>>() {

            @Override
            public boolean matches(Object object) {
                List<AuditLog> auditLogs = (List<AuditLog>) object;

                for (int i = 0; i < auditLogs.size()-1; i++) {
                    if (auditLogs.get(i).getCreationTime()>auditLogs.get(i+1).getCreationTime()) {
                    	return true;
                    }
                }

                return false;
            }

            @Override
            public void describeTo(Description description) {
                description
                        .appendText("AuditLogEntry List sorted on creation time.");
            }
        };
    }
    public static void compareAuditLog(AuditLog dbAuditLog,
            String expectedOperationId) {
        assertTrue(dbAuditLog.getKey() > 0);
        assertEquals(expectedOperationId, dbAuditLog.getOperationId());
        assertEquals("orgId", dbAuditLog.getOrganizationId());
        assertEquals("orgName", dbAuditLog.getOrganizationName());
        assertEquals("userId", dbAuditLog.getUserId());
        assertEquals("abc", dbAuditLog.getLog());
    }

    public static Matcher<byte[]> isSerializedCorrectly(
            final List<AuditLog> auditLogs) {
        return new BaseMatcher<byte[]>() {
            private int errorPosition;

            @Override
            public boolean matches(Object object) {
                try {
                    String result = new String((byte[]) object, "UTF-8");
                    String[] lines = getLines(result);
                    assertEquals(auditLogs.size(), lines.length);

                    for (int i = 0; i < auditLogs.size(); i++) {
                        errorPosition = i;
                        String expected = serialize(auditLogs.get(i));
                        String actual = lines[i];
                        assertEquals(expected, actual);
                    }
                } catch (Exception e) {
                }
                return true;
            }

            @Override
            public void describeTo(Description description) {
                description
                        .appendText("AuditLog is not equal with serialized AuditLog at position "
                                + errorPosition);
            }
        };
    }

    public static Matcher<String> isCorrectTimeStampFormat() {
        return new BaseMatcher<String>() {

            @Override
            public boolean matches(Object object) {
                String string = (String) object;
                assertTrue(string
                        .matches("[0-9]{2,}/[0-9]{2,}/[0-9]{4,}_[0-9]{2,}:[0-9]{2,}:[0-9]{2,}\\.[0-9]{3,}"));
                return true;
            }

            @Override
            public void describeTo(Description description) {
                description
                        .appendText("Timestamp format is wrong. MM/dd/YYYY_hh:mm:ss.SSS expected");
            }
        };
    }

    private static String[] getLines(String txt) {
        return txt.split(System.getProperty("line.separator"));
    }

    private static String serialize(AuditLog auditLog) {
        return getCreationTimeAsStr(auditLog.getCreationTime()) + " "
                + AuditLogSerializer.COMPONENT_NAME + ": "
                + AuditLogSerializer.LOG_LEVEL + ": "
                + auditLog.getOperationId() + ": "
                + getMessageText(auditLog.getOperationId()) + ", "
                + AuditLogSerializer.USER_ID + "="
                + addDoubleQuotesForString(auditLog.getUserId()) + "|"
                + AuditLogSerializer.ORGANIZATION_ID + "="
                + addDoubleQuotesForString(auditLog.getOrganizationId()) + "|"
                + AuditLogSerializer.ORGANIZATION_NAME + "="
                + addDoubleQuotesForString(auditLog.getOrganizationName())
                + auditLog.getLog();
    }

    private static String getCreationTimeAsStr(long creationTime) {
        return AuditLogSerializer.DATE_FORMATTER.get().format(
                new Date(creationTime));
    }

    private static String getMessageText(String messageId) {
        ResourceBundle resourceBundle = ResourceBundle.getBundle(
                AuditLogSerializer.AUDITLOG_MESSAGE_RESOURCE_NAME,
                Locale.ENGLISH);
        return resourceBundle.getString(messageId);
    }

    private static String addDoubleQuotesForString(String come) {
        return (char) 34 + come + (char) 34;
    }
}
