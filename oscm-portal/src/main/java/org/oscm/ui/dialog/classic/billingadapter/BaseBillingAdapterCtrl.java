/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 28.10.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.classic.billingadapter;

import java.util.List;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import org.apache.commons.lang3.StringUtils;

import org.oscm.internal.billingadapter.BillingAdapterService;
import org.oscm.internal.billingadapter.POBaseBillingAdapter;
import org.oscm.internal.billingadapter.POBillingAdapter;
import org.oscm.internal.components.response.Response;

/**
 * @author stavreva
 * 
 */
@ManagedBean
@ViewScoped
public class BaseBillingAdapterCtrl {

    @ManagedProperty(value="#{baseBillingAdapterModel}")
    BaseBillingAdapterModel model;

    @EJB
    BillingAdapterService billingAdapterService;

    public BaseBillingAdapterModel getModel() {
        return model;
    }

    public void setModel(BaseBillingAdapterModel model) {
        this.model = model;
    }

    public List<POBaseBillingAdapter> getBillingAdapters() {
        Response response = getBillingAdapterService().getBaseBillingAdapters();
        return response.getResultList(POBaseBillingAdapter.class);
    }
    
    public String getDefaultBillingIdentifier() {
        List<POBillingAdapter> adapters = billingAdapterService
                .getBillingAdapters().getResultList(POBillingAdapter.class);
        
        for(POBillingAdapter adapter : adapters) {
            if(adapter.isDefaultAdapter()) {
                return adapter.getBillingIdentifier();
            }
        }
        
        return StringUtils.EMPTY;
    }

    private BillingAdapterService getBillingAdapterService() {
        return billingAdapterService;
    }

    public void setBillingAdapterService(BillingAdapterService billingAdapterService) {
        this.billingAdapterService = billingAdapterService;
    }
}
