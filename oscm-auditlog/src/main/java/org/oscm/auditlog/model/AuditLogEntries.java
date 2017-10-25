/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.auditlog.model;

import java.util.List;

public class AuditLogEntries {
    private List<AuditLogEntry> auditLogEntries;

    public AuditLogEntries(List<AuditLogEntry> auditLogEntries) {
        this.auditLogEntries = auditLogEntries;
    }

    public List<AuditLogEntry> getAuditLogEntries() {
        return auditLogEntries;
    }
}
