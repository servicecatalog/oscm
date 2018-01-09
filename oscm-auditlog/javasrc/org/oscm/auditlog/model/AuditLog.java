/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.auditlog.model;

import javax.persistence.*;

@NamedQueries({
        @NamedQuery(name = "AuditLog.findByDateRange", query = "SELECT o FROM AuditLog o  WHERE creationTime >= :startTime AND creationTime < :endTime ORDER BY creationTime"),
        @NamedQuery(name = "AuditLog.countByDateRange", query = "SELECT COUNT(o) FROM AuditLog o WHERE creationTime >= :startTime AND creationTime < :endTime"),
        @NamedQuery(name = "AuditLog.countByOperationAndDateRange", query = "SELECT COUNT(o) FROM AuditLog o WHERE o.operationId in (:operationIds) AND creationTime >= :startTime AND creationTime < :endTime"),
        @NamedQuery(name = "AuditLog.findByOperationAndDateRange", query = "SELECT o FROM AuditLog o  WHERE o.operationId in (:operationIds) AND creationTime >= :startTime AND creationTime < :endTime ORDER BY creationTime") })
@Entity
public class AuditLog implements AuditLogEntry {
    @Id
    @Column(name = "tkey")
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "auditlog_seq")
    @TableGenerator(table = "hibernate_sequences", name = "auditlog_seq", allocationSize = 100, valueColumnName = "sequence_next_hi_value")
    private long key;

    @Column(nullable = false)
    private String operationId;

    @Column(nullable = false)
    private String operationName;

    @Column(nullable = false)
    private String log;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String organizationId;

    @Column(nullable = false)
    private String organizationName;

    @Column(nullable = false)
    private long creationTime;

    public AuditLog() {
    }

    public long getKey() {
        return key;
    }

    public void setKey(long key) {
        this.key = key;
    }

    @Override
    public String getOperationId() {
        return operationId;
    }

    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }

    @Override
    public String getOperationName() {
        return operationName;
    }

    public void setLog(String log) {
        this.log = log;
    }

    @Override
    public String getLog() {
        return log;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String getUserId() {
        return userId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    @Override
    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    @Override
    public String getOrganizationName() {
        return organizationName;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }
}
