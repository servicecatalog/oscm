/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 01.03.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.classic.manageoperatorrevenueshare;

import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;

import org.oscm.internal.pricing.PORevenueShare;

/**
 * @author barzu
 */
@ManagedBean
@ViewScoped
public class ManageOperatorRevenueShareModel {

    private List<SelectItem> templates = new ArrayList<SelectItem>();
    private long selectedTemplateKey;

    private PORevenueShare operatorRevenueShare;
    private PORevenueShare defaultOperatorRevenueShare;

    public List<SelectItem> getTemplates() {
        return templates;
    }

    public void setTemplates(List<SelectItem> templates) {
        this.templates = templates;
    }

    public long getSelectedTemplateKey() {
        return selectedTemplateKey;
    }

    public void setSelectedTemplateKey(long selectedTemplateKey) {
        this.selectedTemplateKey = selectedTemplateKey;
    }

    public boolean isServiceSelected() {
        return selectedTemplateKey != 0;
    }

    public boolean isSaveDisabled() {
        return !isServiceSelected();
    }

    public PORevenueShare getOperatorRevenueShare() {
        return operatorRevenueShare;
    }

    public void setOperatorRevenueShare(PORevenueShare operatorRevenueShare) {
        this.operatorRevenueShare = operatorRevenueShare;
    }

    public PORevenueShare getDefaultOperatorRevenueShare() {
        return defaultOperatorRevenueShare;
    }

    public void setDefaultOperatorRevenueShare(
            PORevenueShare defaultOperatorRevenueShare) {
        this.defaultOperatorRevenueShare = defaultOperatorRevenueShare;
    }

}
