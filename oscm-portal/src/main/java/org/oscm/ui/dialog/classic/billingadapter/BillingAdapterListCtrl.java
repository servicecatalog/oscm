/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                                                                                                       
 *******************************************************************************/

package org.oscm.ui.dialog.classic.billingadapter;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import org.oscm.ui.beans.BaseBean;
import org.oscm.internal.billingadapter.BillingAdapterService;
import org.oscm.internal.billingadapter.POBillingAdapter;
import org.oscm.internal.components.response.Response;
import org.oscm.internal.types.exception.BillingAdapterConnectionException;
import org.oscm.internal.types.exception.BillingApplicationException;
import org.oscm.internal.types.exception.SaaSApplicationException;

/**
 * Class provides actions for View billing adapters
 *
 * @author BadziakP
 */
@ManagedBean
@ViewScoped
public class BillingAdapterListCtrl extends BaseBean {

    @ManagedProperty(value = "#{billingAdapterListModel}")
    private BillingAdapterListModel model;

    @EJB
    private BillingAdapterService billingAdapterService;

    /**
     * Method is used to initialize list of all billing adapters in model.
     * Default billing adapter is set as first list element.
     */
    public void getInitialize() {
        List<BillingAdapterWrapper> adapterWrappers = new ArrayList<>();
        List<POBillingAdapter> adapters = getBillingAdapters();
        for (POBillingAdapter adapter : adapters) {
            if (adapter.isDefaultAdapter()) {
                adapterWrappers.add(0, new BillingAdapterWrapper(adapter));
            } else {
                adapterWrappers.add(new BillingAdapterWrapper(adapter));
            }
        }
        getModel().setBillingAdapters(adapterWrappers);
    }

    /**
     * Method returns all billing adapters
     * 
     * @return List of all available billing adapters
     */
    private List<POBillingAdapter> getBillingAdapters() {
        Response response = getBillingAdapterService().getBillingAdapters();
        List<POBillingAdapter> adapters = response.getResultList(POBillingAdapter.class);
        return adapters;
    }

    public BillingAdapterListModel getModel() {
        return model;
    }

    public void setModel(BillingAdapterListModel model) {
        this.model = model;
    }

    public BillingAdapterService getBillingAdapterService() {
        return billingAdapterService;
    }

    public void setBillingAdapterService(BillingAdapterService billingAdapterService) {
        this.billingAdapterService = billingAdapterService;
    }

    /**
     * Method tests connection with billing system.
     * 
     * @return OUTCOME_SUCCESS - if connection can be established OUTCOME_ERROR
     *         - otherwise
     * @throws SaaSApplicationException
     */
    public String testConnection() throws SaaSApplicationException {
        if (getModel() == null) {
            return OUTCOME_ERROR;
        }
        POBillingAdapter adapter = model.getSelectedBillingAdapter();

        try {
            getBillingAdapterService().testConnection(adapter);
            ui.handle(INFO_BILLINGSYSTEM_CONNECTION_SUCCESS);

            return OUTCOME_SUCCESS;
        } catch (BillingApplicationException bae) {
            if (bae.getCause() instanceof BillingAdapterConnectionException) {
                ui.handleException(
                        (BillingAdapterConnectionException) bae.getCause());
            } else {
                ui.handleException(bae);
            }
            return OUTCOME_ERROR;
        }
    }
}
