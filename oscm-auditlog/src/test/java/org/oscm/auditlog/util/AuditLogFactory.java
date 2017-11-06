/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.auditlog.util;

import java.util.ArrayList;
import java.util.List;

import org.oscm.auditlog.dao.DefaultAuditLogEntry;
import org.oscm.auditlog.model.AuditLog;
import org.oscm.auditlog.model.AuditLogEntry;

public class AuditLogFactory {
    public static List<AuditLogEntry> createAuditLogEntries(
            String operationName, int numberLogEntries) {
        List<AuditLogEntry> logEntries = new ArrayList<AuditLogEntry>();
        for (int i = 0; i < numberLogEntries; i++) {
            logEntries.add(createAuditLogEntry(operationName,
                    Integer.toString(i)));
        }
        return logEntries;
    }

    public static AuditLogEntry createAuditLogEntry(String operationName,
            String appendix) {
        String appendWith = appendix;
        if (appendix != null && appendix.trim().length() > 0) {
            appendWith = "_" + appendWith;
        }

        DefaultAuditLogEntry logEntry = new DefaultAuditLogEntry();
        logEntry.setLog("|parameter1=value1|parameter2=value2|parameter3=value3|");
        logEntry.setOperationId("30000");
        logEntry.setOperationName(operationName);
        logEntry.setOrganizationId("orgId" + appendWith);
        logEntry.setOrganizationName("orgName" + appendWith);
        logEntry.setUserId("userId" + appendWith);
        return logEntry;
    }

    public static AuditLogEntry createAuditLogEntry(String operationName) {
        return createAuditLogEntry(operationName, "");
    }

    public static List<AuditLog> createAuditLogs() {
        List<AuditLog> auditLogs = new ArrayList<AuditLog>();
        AuditLog auditLog = new AuditLog();
        auditLog.setOperationId("30000");
        auditLog.setOperationName("operationName1");
        auditLog.setCreationTime(162831600000L);
        auditLog.setLog("|parameter1=\"value1\"|parameter2=\"value2\"|parameter3=\"value3\"|");
        auditLog.setOrganizationId("342r7821g");
        auditLog.setOrganizationName("Google");
        auditLog.setUserId("Sergey Brin");
        auditLogs.add(auditLog);

        AuditLog auditLog2 = new AuditLog();
        auditLog2.setOperationId("30001");
        auditLog2.setOperationName("operationName2");
        auditLog2.setCreationTime(4236874628L);
        auditLog2.setLog("|parameter4=\"value4\"|parameter5=\"value5\"|");
        auditLog2.setOrganizationId("53498527");
        auditLog2.setOrganizationName("Facebook");
        auditLog2.setUserId("Mark Zuckerberg");
        auditLogs.add(auditLog2);
        
        AuditLog auditLog3 = new AuditLog();
        auditLog3.setOperationId("30002");
        auditLog3.setOperationName("operationName3");
        auditLog3.setCreationTime(4236874628L);
        auditLog3.setLog("|parameter4=\"value4\"|parameter5=\"value5\"|");
        auditLog3.setOrganizationId("53498527");
        auditLog3.setOrganizationName("\u30B5\u30FC\u30D3\u30B9\u63D0\u4F9B\u90E8\u9580");
        auditLog3.setUserId("Japanese");
        auditLogs.add(auditLog3);
        return auditLogs;
    }
}
