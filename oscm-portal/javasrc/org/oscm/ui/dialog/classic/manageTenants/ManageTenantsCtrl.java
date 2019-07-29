/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Creation Date: 29.08.2016
 *
 *******************************************************************************/
package org.oscm.ui.dialog.classic.manageTenants;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
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

    @ManagedProperty(value = "#{manageTenantsModel}")
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
        if (model.getDataTableHeaders() == null
                || model.getDataTableHeaders().isEmpty()) {
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
        model.setTenants(manageTenantService.getAllTenantsWithDefaultTenant());
        model.setTenantId(new FieldData<String>(null, true, false));
        model.setTenantName(new FieldData<String>(null, true, true));
        model.setTenantDescription(new FieldData<String>(null, true, false));
        model.setSaveDisabled(true);
        model.setDeleteDisabled(true);
    }

    public void setSelectedTenantId(String tenantId) {
        model.setSelectedTenantId(tenantId);
    }

    public void setSelectedTenant() {
        POTenant poTenant = getSelectedTenant();
        model.setSelectedTenant(poTenant);
        model.setTenantId(new FieldData<>(poTenant.getTenantId(), true, false));
        model.setTenantName(new FieldData<>(poTenant.getName(),
                !isDefault(poTenant), true));
        model.setTenantDescription(new FieldData<>(poTenant.getDescription(),
                !isDefault(poTenant), false));
        model.setSaveDisabled(isDefault(poTenant));
        model.setDeleteDisabled(isDefault(poTenant));
    }

    
    private POTenant getSelectedTenant() {
        POTenant defaultTenant = getDefaultTenant();
        if (!defaultTenant.getTenantId().equals(model.getSelectedTenantId())) {
            try {
                return getManageTenantService()
                        .getTenantByTenantId(model.getSelectedTenantId());
            } catch (SaaSApplicationException e) {
                ui.handleException(e);
            }
        }
        return defaultTenant;
    }

    public String save() {
        try {
            if (model.getSelectedTenant() != null) {

                model.getSelectedTenant()
                        .setTenantId(model.getTenantId().getValue());

                if (!isDefault(model.getSelectedTenant())) {
                    model.getSelectedTenant()
                            .setName(model.getTenantName().getValue());
                    model.getSelectedTenant().setDescription(
                            model.getTenantDescription().getValue());

                    manageTenantService.updateTenant(model.getSelectedTenant());
                    model.setSelectedTenantId(
                            model.getSelectedTenant().getTenantId());
                }

                handleSuccessMessage(BaseBean.INFO_TENANT_SAVED,
                        model.getTenantId().getValue());
            } else {
                POTenant poTenant = new POTenant();
                poTenant.setName(model.getTenantName().getValue());
                poTenant.setDescription(
                        model.getTenantDescription().getValue());
                String generatedTenantId = manageTenantService
                        .addTenant(poTenant);
                model.setSelectedTenantId(generatedTenantId);
                handleSuccessMessage(BaseBean.INFO_TENANT_ADDED,
                        generatedTenantId);
            }
            model.setDirty(false);

        } catch (SaaSApplicationException e) {
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
            handleSuccessMessage(BaseBean.INFO_TENANT_DELETED,
                    model.getSelectedTenantId());
            refreshModelAfterDelete();
            model.setDirty(false);
        } catch (SaaSApplicationException e) {
            ui.handleException(e);
        }
        return null;
    }

    private void refreshModel() {
        model.setTenants(manageTenantService.getAllTenantsWithDefaultTenant());
        for (POTenant poTenant : model.getTenants()) {
            if (poTenant.getTenantId().equals(model.getSelectedTenantId())) {
                model.setSelectedTenant(poTenant);
                model.setTenantId(
                        new FieldData<>(poTenant.getTenantId(), true, false));
                model.setDeleteDisabled(false);
                return;
            }
        }
    }

    private void refreshModelAfterDelete() {
        model.setSelectedTenant(null);
        model.setSelectedTenantId(null);
        initWithoutSelection();
    }

    public void addTenant() {
        model.setSelectedTenant(null);
        model.setSelectedTenantId(null);
        model.setTenantId(new FieldData<String>(null, true, false));
        model.setTenantName(new FieldData<String>(null, false, true));
        model.setTenantDescription(new FieldData<String>(null, false, false));
        model.setSaveDisabled(false);
        model.setDeleteDisabled(true);
    }

    public void setManageTenantService(
            ManageTenantService manageTenantService) {
        this.manageTenantService = manageTenantService;
    }

    public ManageTenantService getManageTenantService() {
        return this.manageTenantService;
    }

    public String exportSettingsTemplate() throws IOException {
        final String selectedTenantId = model.getSelectedTenantId();

        String tenantId = Value.of(selectedTenantId)
                .ifNotGivenReturn("default");

        Properties properties = generateTenantSettingsTemplate(tenantId);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            final String fName = String.format("tenant-%s.properties",
                    tenantId);
            properties.store(baos, null);
            writeSettings(baos.toByteArray(), fName);
        } finally {
            baos.close();
        }

        return OUTCOME_SUCCESS;
    }

    public void writeSettings(byte[] content, String fileName)
            throws IOException {
        super.writeContentToResponse(content, fileName, "text/x-java-properties");
    }

    public Properties generateTenantSettingsTemplate(String tenantId) throws IOException {

        Properties prop = new Properties();
        HttpURLConnection con = null;
        try {
            con = getConnection();
            prop.load(con.getInputStream());
            prop.put("oidc.provider", tenantId);
            return prop;
        
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }
      
    }
    
    boolean isDefault(POTenant poTenant) {
        return "default".equals(poTenant.getTenantId());
    }
    
    POTenant getDefaultTenant() {
        for (POTenant t : model.getTenants())
            if (isDefault(t))
                return t;

        throw new RuntimeException("Default Tenant missing");
    }

    
    HttpURLConnection getConnection() throws IOException {
        final String url = "https://raw.githubusercontent.com/servicecatalog/oscm-identity/master/config/tenants/tenant-default.properties";
        return (HttpURLConnection) new URL(url).openConnection();
    }

    static class Value<T> {
        private T value;

        static <T> Value<T> of(T value) {
            return new Value<T>(value);
        }

        Value(T value) {
            this.value = value;
        }

        T ifNotGivenReturn(T otherValue) {
            return (value != null) ? value : otherValue;
        }
    }

}
