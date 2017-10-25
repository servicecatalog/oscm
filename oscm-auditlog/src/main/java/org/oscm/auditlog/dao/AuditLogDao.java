/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.auditlog.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.oscm.auditlog.model.AuditLog;
import org.oscm.auditlog.model.AuditLogEntry;

public class AuditLogDao {
    @PersistenceContext(unitName = "oscm-auditlog")
    EntityManager em;
    int batchSize = 100;

    public void saveAuditLog(List<AuditLogEntry> logEntries) {
        for (int i = 0; i < logEntries.size(); ++i) {
            AuditLog auditLog = createAuditLog(logEntries.get(i));
            persist(auditLog);

            if (i > 0 && i % batchSize == 0) {
                flushAndClear();
            }
        }
        flushAndClear();
    }

    AuditLog createAuditLog(AuditLogEntry logEntry) {
        AuditLog auditLog = new AuditLog();
        auditLog.setLog(logEntry.getLog());
        auditLog.setOperationId(logEntry.getOperationId());
        auditLog.setOperationName(logEntry.getOperationName());
        auditLog.setOrganizationId(logEntry.getOrganizationId());
        auditLog.setOrganizationName(logEntry.getOrganizationName());
        auditLog.setUserId(logEntry.getUserId());
        auditLog.setCreationTime(System.currentTimeMillis());
        return auditLog;
    }

    void persist(AuditLog auditLog) {
        em.persist(auditLog);
    }

    void flushAndClear() {
        em.flush();
        em.clear();
    }

    /**
     * count the number of auditLogs by operationIds and date range, if
     * operationIds is null or empty, count the number of AuditLogs only by date
     * range
     * 
     * @param operationIds
     * @param startTime
     * @param endTime
     * @return
     */
    public long countAuditLogs(List<String> operationIds, long startTime,
            long endTime) {
        TypedQuery<Number> query;
        if (operationIds == null || operationIds.isEmpty()) {
            query = em.createNamedQuery("AuditLog.countByDateRange",
                    Number.class);
        } else {
            query = em.createNamedQuery(
                    "AuditLog.countByOperationAndDateRange", Number.class);
            query.setParameter("operationIds", operationIds);
        }
        query.setParameter("startTime", Long.valueOf(startTime));
        query.setParameter("endTime", Long.valueOf(endTime));
        return query.getSingleResult().longValue();
    }

    /**
     * load auditLogs by operationIds and date range, if operationIds is null or
     * empty, load auditLogs only by date range
     * 
     * @param operationIds
     * @param startTime
     * @param endTime
     * @return
     */
    public List<AuditLog> loadAuditLogs(List<String> operationIds,
            long startTime, long endTime) {
        TypedQuery<AuditLog> query;
        if (operationIds == null || operationIds.isEmpty()) {
            query = em.createNamedQuery("AuditLog.findByDateRange",
                    AuditLog.class);
        } else {
            query = em.createNamedQuery("AuditLog.findByOperationAndDateRange",
                    AuditLog.class);
            query.setParameter("operationIds", operationIds);
        }
        query.setParameter("startTime", Long.valueOf(startTime));
        query.setParameter("endTime", Long.valueOf(endTime));
        return query.getResultList();
    }
}
