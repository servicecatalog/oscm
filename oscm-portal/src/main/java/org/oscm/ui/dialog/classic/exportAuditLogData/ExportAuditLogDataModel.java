/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2013-4-17                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.classic.exportAuditLogData;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;

import org.oscm.ui.model.AuditLogOperation;

/**
 * This model is used for exporting audit log
 * 
 * @author Qiu
 * 
 */
@ViewScoped
@ManagedBean(name="exportAuditLogDataModel")
public class ExportAuditLogDataModel {

    private String selectedGroup;

    private Date fromDate;

    private Date toDate;

    private byte[] auditLogData;

    private List<SelectItem> availableSelectGroups;

    private List<AuditLogOperation> availableOperations;

    private Map<String, String> operationGroups;

    private Map<String, String> operations;

    private boolean initialized;

    public Date getFromDate() {
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getToDate() {
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public byte[] getAuditLogData() {
        return auditLogData;
    }

    public void setAuditLogData(byte[] auditLogData) {
        this.auditLogData = auditLogData;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    public String getSelectedGroup() {
        return selectedGroup;
    }

    public void setSelectedGroup(String selectedGroup) {
        this.selectedGroup = selectedGroup;
    }

    public List<SelectItem> getAvailableSelectGroups() {
        return availableSelectGroups;
    }

    public void setAvailableSelectGroups(List<SelectItem> availableSelectGroups) {
        this.availableSelectGroups = availableSelectGroups;
    }

    public List<AuditLogOperation> getAvailableOperations() {
        return availableOperations;
    }

    public void setAvailableOperations(
            List<AuditLogOperation> availableOperations) {
        this.availableOperations = availableOperations;
    }

    public Map<String, String> getOperations() {
        return operations;
    }

    public void setOperations(Map<String, String> operations) {
        this.operations = operations;
    }

    public Map<String, String> getOperationGroups() {
        return operationGroups;
    }

    public void setOperationGroups(Map<String, String> operationGroups) {
        this.operationGroups = operationGroups;
    }

}
