/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.ui.dialog.classic.partnerservice;

import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.oscm.ui.model.RoleSpecificPrice;
import org.oscm.internal.partnerservice.POPartnerServiceDetails;

@ViewScoped
@ManagedBean(name="partnerServiceViewModel")
public class PartnerServiceViewModel {

    private long selectedServiceKey;
    POPartnerServiceDetails service;
    private boolean disabled = true;
    List<RoleSpecificPrice> rolePrices;

    public POPartnerServiceDetails getPartnerServiceDetails() {
        return service;
    }

    public void setPartnerServiceDetails(POPartnerServiceDetails service) {
        this.service = service;
    }

    public long getSelectedServiceKey() {
        return selectedServiceKey;
    }

    public void setSelectedServiceKey(long selectedServiceKey) {
        this.selectedServiceKey = selectedServiceKey;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public List<RoleSpecificPrice> getRolePrices() {
        return rolePrices;
    }

    public void setRolePrices(List<RoleSpecificPrice> rolePrices) {
        this.rolePrices = rolePrices;
    }

}
