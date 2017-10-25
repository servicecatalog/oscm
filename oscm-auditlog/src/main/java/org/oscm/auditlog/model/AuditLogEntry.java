/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.auditlog.model;

public interface AuditLogEntry {
    public String getOperationId();

    public String getOperationName();

    public String getLog();

    public String getUserId();

    public String getOrganizationId();

    public String getOrganizationName();
}
