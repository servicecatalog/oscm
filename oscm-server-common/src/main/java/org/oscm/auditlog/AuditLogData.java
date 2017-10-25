/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 19.04.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.auditlog;

import java.util.ArrayList;
import java.util.List;

import org.oscm.auditlog.model.AuditLogAction;
import org.oscm.auditlog.model.AuditLogEntry;
import org.oscm.domobjects.DomainObject;

public class AuditLogData {

    static final ThreadLocal<List<AuditLogEntry>> auditLogData = new ThreadLocal<List<AuditLogEntry>>();

    public static void add(AuditLogEntry entry) {
        List<AuditLogEntry> logData = auditLogData.get();
        if (logData == null) {
            logData = new ArrayList<AuditLogEntry>();
        }
        if (entry != null && !logData.contains(entry)) {
            logData.add(entry);
        }
        auditLogData.set(logData);
    }

    public static void clear() {
        auditLogData.remove();
    }

    public static List<AuditLogEntry> get() {
        return auditLogData.get();
    }

    public static AuditLogAction determineAction(DomainObject<?> oldObject,
            DomainObject<?> newObject) {

        if ((oldObject == null) && (newObject == null)) {
            return AuditLogAction.NONE;
        }
        if ((oldObject == null) && (newObject != null)) {
            return AuditLogAction.INSERT;
        }
        if ((oldObject != null) && (newObject == null)) {
            return AuditLogAction.DELETE;
        }
        if ((oldObject != null) && (newObject != null)) {
            if (oldObject.getKey() != newObject.getKey()) {
                return AuditLogAction.UPDATE;
            }
        }
        return AuditLogAction.NONE;
    }
}
