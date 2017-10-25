/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.auditlog.bean;

import static org.oscm.internal.types.enumtypes.ConfigurationKey.AUDIT_LOG_MAX_ENTRIES_RETRIEVED;

import java.util.List;

import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;
import javax.inject.Inject;

import org.oscm.auditlog.dao.AuditLogDao;
import org.oscm.auditlog.model.AuditLog;
import org.oscm.auditlog.model.AuditLogEntries;
import org.oscm.auditlog.model.AuditLogEntry;
import org.oscm.auditlog.util.AuditLogSerializer;
import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.domobjects.ConfigurationSetting;
import org.oscm.types.constants.Configuration;
import org.oscm.internal.types.exception.AuditLogTooManyRowsException;

@Stateless
public class AuditLogServiceBean {   
    @Inject
    AuditLogDao dao;

    @Inject
    Event<AuditLogEntries> logEvent;

    @EJB
    ConfigurationServiceLocal configService;
    
    @Asynchronous
    public void log(List<AuditLogEntry> logEntries) {
        if (logEntries != null) {
            logEvent.fire(new AuditLogEntries(logEntries));
        }
    }

    public byte[] loadAuditLogs(List<String> operationIds, long startTime,
            long endTime) throws AuditLogTooManyRowsException {
        long numberAuditLogs = dao.countAuditLogs(operationIds, startTime,
                endTime);
        if (numberAuditLogs > getMaxAuditlogs()) {
            throw new AuditLogTooManyRowsException();
        }
        List<AuditLog> auditLogs = dao.loadAuditLogs(operationIds, startTime, endTime);
        return createAuditLogSerializer().serialize(auditLogs);
    }

    long getMaxAuditlogs() {
        ConfigurationSetting setting = configService.getConfigurationSetting(
                AUDIT_LOG_MAX_ENTRIES_RETRIEVED, Configuration.GLOBAL_CONTEXT);
        return setting.getLongValue();
    }

    AuditLogSerializer createAuditLogSerializer() {
        return new AuditLogSerializer();
    }

    public void saveAuditLogEntries(
            @Observes(during = TransactionPhase.AFTER_SUCCESS) AuditLogEntries logData) {
        dao.saveAuditLog(logData.getAuditLogEntries());
    }
}
