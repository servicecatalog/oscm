/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.auditlog.dao;

import org.oscm.auditlog.model.AuditLogEntry;

public class DefaultAuditLogEntry implements AuditLogEntry {
    private String operationId;
    private String operationName;
    private String log;
    private String userId;
    private String organizationId;
    private String organizationName;

    public DefaultAuditLogEntry() {
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
}
