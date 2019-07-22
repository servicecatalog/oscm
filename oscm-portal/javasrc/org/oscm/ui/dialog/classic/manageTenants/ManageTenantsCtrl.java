/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 29.08.2016
 *
 *******************************************************************************/
package org.oscm.ui.dialog.classic.manageTenants;

import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import org.oscm.internal.components.response.Response;
import org.oscm.internal.tenant.ManageTenantService;
import org.oscm.internal.tenant.POTenant;
import org.oscm.internal.types.exception.SaaSApplicationException;
import org.oscm.internal.types.exception.SaaSSystemException;
import org.oscm.ui.beans.BaseBean;
import org.oscm.ui.common.DataTableHandler;
import org.oscm.ui.profile.FieldData;

@ManagedBean
@ViewScoped
public class ManageTenantsCtrl extends BaseBean implements Serializable {
    
	private static final long serialVersionUID = 3995366775624605906L;

	@EJB
    private ManageTenantService manageTenantService;

    @ManagedProperty(value="#{manageTenantsModel}")
    private ManageTenantsModel model;

    @PostConstruct
    public void init() {
        if (model.getSelectedTenant() == null) {
            initWithoutSelection();
        }
    }

    public int getTenantsNumber() {
        return model.getTenants().size();
    }

    public ManageTenantsModel getModel() {
        return model;
    }

    public void setModel(ManageTenantsModel model) {
        this.model = model;
    }

    public List<String> getDataTableHeaders() {
        if (model.getDataTableHeaders() == null || model.getDataTableHeaders().isEmpty()) {
            try {
                model.setDataTableHeaders(DataTableHandler
                    .getTableHeaders(POTenant.class.getName()));
            } catch (Exception e) {
                throw new SaaSSystemException(e);
            }
        }
        return model.getDataTableHeaders();
    }

    private void initWithoutSelection() {
        model.setTenants(manageTenantService.getAllTenants());
        model.setTenantId(new FieldData<String>(null, true, false));
        model.setTenantName(new FieldData<String>(null, true, true));
        model.setTenantDescription(new FieldData<String>(null, true, false));
        model.setSaveDisabled(true);
        model.setDeleteDisabled(true);
        model.setImportDisabled(true);
    }

    public void setSelectedTenantId(String tenantId) {
        model.setSelectedTenantId(tenantId);
    }

    public void setSelectedTenant() {
        POTenant poTenant = getSelectedTenant();
        model.setClearExportAvailable(!manageTenantService.getTenantSettings(poTenant.getKey()).isEmpty());
        model.setSelectedTenant(poTenant);
        model.setTenantId(new FieldData<>(poTenant.getTenantId(), true, false));
        model.setTenantName(new FieldData<>(poTenant.getName(), false, true));
        model.setTenantDescription(new FieldData<>(poTenant.getDescription(), false, false));
        model.setSaveDisabled(false);
        model.setDeleteDisabled(false);
        model.setImportDisabled(false);
    }

    private POTenant getSelectedTenant() {
        POTenant poTenant = null;
        try {
            poTenant = getManageTenantService().getTenantByTenantId(model.getSelectedTenantId());
        } catch (SaaSApplicationException e) {
            ui.handleException(e);
        }
        return poTenant;
    }

    public String save() {
        try {
            if (model.getSelectedTenant() != null) {
                model.getSelectedTenant().setTenantId(model.getTenantId().getValue());
                model.getSelectedTenant().setName(model.getTenantName().getValue());
                model.getSelectedTenant().setDescription(model.getTenantDescription().getValue());
                manageTenantService.updateTenant(model.getSelectedTenant());
                model.setSelectedTenantId(model.getSelectedTenant().getTenantId());
                handleSuccessMessage(BaseBean.INFO_TENANT_SAVED, model.getTenantId().getValue());
            } else {
                POTenant poTenant = new POTenant();
                poTenant.setName(model.getTenantName().getValue());
                poTenant.setDescription(model.getTenantDescription().getValue());
                String generatedTenantId = manageTenantService.addTenant(poTenant);
                model.setSelectedTenantId(generatedTenantId);
                handleSuccessMessage(BaseBean.INFO_TENANT_ADDED, generatedTenantId);
            }
            model.setDirty(false);
            
        }  catch (SaaSApplicationException e) {
            ui.handleException(e);
        }
        refreshModel();
        return OUTCOME_SUCCESS;
    }

    public void handleSuccessMessage(String message, String tenantId) {
        ui.handle(new Response(), message, tenantId);
    }

    public String delete() {
        try {
            manageTenantService.removeTenant(model.getSelectedTenant());
            handleSuccessMessage(BaseBean.INFO_TENANT_DELETED, model.getSelectedTenantId());
            refreshModelAfterDelete();
            model.setDirty(false);
        } catch (SaaSApplicationException e) {
            ui.handleException(e);
        }
        return null;
    }

    private void refreshModel() {
        model.setTenants(manageTenantService.getAllTenants());
        for (POTenant poTenant : manageTenantService.getAllTenants()) {
            if (poTenant.getTenantId().equals(model.getSelectedTenantId())) {
                model.setSelectedTenant(poTenant);
                model.setClearExportAvailable(!manageTenantService.getTenantSettings(poTenant.getKey()).isEmpty());
                model.setTenantId(new FieldData<>(poTenant.getTenantId(), true, false));
                model.setDeleteDisabled(false);
                return;
            }
        }
    }

    private void refreshModelAfterDelete() {
        model.setSelectedTenant(null);
        model.setSelectedTenantId(null);
        model.setClearExportAvailable(false);
        initWithoutSelection();
    }

    public void addTenant() {
        model.setSelectedTenant(null);
        model.setSelectedTenantId(null);
        model.setClearExportAvailable(false);
        model.setTenantId(new FieldData<String>(null, true, false));
        model.setTenantName(new FieldData<String>(null, false, true));
        model.setTenantDescription(new FieldData<String>(null, false, false));
        model.setSaveDisabled(false);
        model.setDeleteDisabled(true);
        model.setImportDisabled(false);
    }

    
    public String clear() {
        try {
            manageTenantService.removeTenantSettings(model.getSelectedTenant().getKey());
            refreshModel();
        } catch (SaaSApplicationException e) {
            ui.handleException(e);
        }
        addMessage(null, FacesMessage.SEVERITY_INFO, INFO_IDP_SETTINGS_CLEAR);
        return OUTCOME_SUCCESS;
    }

    public void setManageTenantService(ManageTenantService manageTenantService) {
        this.manageTenantService = manageTenantService;
    }

    public ManageTenantService getManageTenantService() {
        return this.manageTenantService;
    }

}
