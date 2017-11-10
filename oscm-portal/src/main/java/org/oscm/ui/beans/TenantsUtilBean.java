/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2017
 *
 *  Creation Date: 21.09.16 09:00
 *
 ******************************************************************************/

package org.oscm.ui.beans;

import java.util.List;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

import org.oscm.internal.tenant.ManageTenantService;
import org.oscm.internal.tenant.POTenant;

/**
 * Authored by dawidch
 */
@ManagedBean
@RequestScoped
public class TenantsUtilBean {

    @EJB
    private ManageTenantService manageTenantService;

    public ManageTenantService getManageTenantService() {
        return manageTenantService;
    }

    public void setManageTenantService(ManageTenantService manageTenantService) {
        this.manageTenantService = manageTenantService;
    }

    public List<POTenant> getSuggestionsForTenants(String tenantId) {

        tenantId = tenantId.replaceAll("\\p{C}", "");
        String pattern = tenantId + "%";

        List<POTenant> tenants = manageTenantService.getTenantsByIdPattern(pattern);

        return tenants;
    }
}
