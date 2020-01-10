/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 29.08.2016
 *
 *******************************************************************************/
package org.oscm.ui.dialog.classic.manageTenants;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.oscm.internal.tenant.POTenant;
import org.oscm.ui.beans.BaseModel;
import org.oscm.ui.profile.FieldData;

@ViewScoped
@ManagedBean
public class ManageTenantsModel extends BaseModel implements Serializable {

    private static final long serialVersionUID = 2478162552050148039L;
    
    private List<POTenant> tenants;
    private FieldData<String> tenantId;
    private FieldData<String> tenantName;
    private FieldData<String> tenantDescription;
    private String selectedTenantId;
    private boolean saveDisabled;
    private boolean deleteDisabled;
    private POTenant selectedTenant;
    
    private boolean clearExportAvailable;
    
    private boolean dirty;

    public List<POTenant> getTenants() {
        return tenants;
    }

    private List<String> dataTableHeaders = new ArrayList<>();

    public void setTenants(List<POTenant> tenants) {
        this.tenants = tenants;
    }

    public List<String> getDataTableHeaders() {
        return dataTableHeaders;
    }

    public void setDataTableHeaders(List<String> dataTableHeaders) {
        this.dataTableHeaders = dataTableHeaders;
    }

    public FieldData<String> getTenantId() {
        return tenantId;
    }

    public void setTenantId(FieldData<String> tenantId) {
        this.tenantId = tenantId;
    }

     
    public FieldData<String> getTenantDescription() {
        return tenantDescription;
    }

    public void setTenantDescription(FieldData<String> tenantDescription) {
        this.tenantDescription = tenantDescription;
    }

    public String getSelectedTenantId() {
        return selectedTenantId;
    }

    public void setSelectedTenantId(String selectedTenantId) {
        this.selectedTenantId = selectedTenantId;
    }

    public boolean isSaveDisabled() {
        return saveDisabled;
    }

    public void setSaveDisabled(boolean saveDisabled) {
        this.saveDisabled = saveDisabled;
    }

    public boolean isDeleteDisabled() {
        return deleteDisabled;
    }

    public void setDeleteDisabled(boolean deleteDisabled) {
        this.deleteDisabled = deleteDisabled;
    }

    public POTenant getSelectedTenant() {
        return selectedTenant;
    }

    public void setSelectedTenant(POTenant selectedTenant) {
        this.selectedTenant = selectedTenant;
    }

    public void setTenantName(FieldData<String> tenantName) {
        this.tenantName = tenantName;
    }
    
    public FieldData<String> getTenantName() {
        return tenantName;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }
    
   
}
