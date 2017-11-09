/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 28.10.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.classic.billingadapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;

import org.oscm.internal.billingadapter.BillingAdapterService;
import org.oscm.internal.billingadapter.ConnectionPropertyItem;
import org.oscm.internal.billingadapter.POBillingAdapter;
import org.oscm.internal.components.response.Response;
import org.oscm.internal.types.exception.*;
import org.oscm.internal.types.exception.DomainObjectException.ClassEnum;
import org.oscm.ui.beans.BaseBean;
import org.oscm.ui.common.UiDelegate;

/**
 * @author stavreva
 * 
 */
@ManagedBean
@ViewScoped
public class BillingAdapterCtrl extends BaseBean {

    private boolean disabledAddBtn;

    @ManagedProperty(value = "#{billingAdapterModel}")
    private BillingAdapterModel model;

    @EJB
    private BillingAdapterService billingAdapterService;

    public BillingAdapterModel getModel() {
        return model;
    }

    public void setModel(BillingAdapterModel model) {
        this.model = model;
    }
    
    public void getInitialize() {
        if (!model.isInitialized()) {
            int index = reinitializeAdapters();
            setSelectedPanel(index);
            model.setInitialized(true);
        }
    }

    public void initializeAdapters() {

        int index = reinitializeAdapters();

        setSelectedPanel(index);

    }

    public int reinitializeAdapters() {
        int defaultIndex = 0;
        List<BillingAdapterWrapper> adapterWrappers = new ArrayList<>();
        List<POBillingAdapter> adapters = getBillingAdapters();
        for (int i = 0; i < adapters.size(); i++) {

            POBillingAdapter adapter = adapters.get(i);
            adapterWrappers.add(new BillingAdapterWrapper(adapter));
            if (adapter.isDefaultAdapter()) {
                defaultIndex = i;
            }
            model.setBillingAdapters(adapterWrappers);
        }
        return defaultIndex;
    }

    private POBillingAdapter createNewAdapter() {
        POBillingAdapter adapter = new POBillingAdapter();

        Set<ConnectionPropertyItem> properties = new TreeSet<>();
        properties.add(new ConnectionPropertyItem("JNDI_NAME", null));
        adapter.setConnectionProperties(properties);

        return adapter;
    }

    void updateAdapter(String billingIdentifier)
            throws SaaSApplicationException {
        POBillingAdapter adapter = getBillingAdapter(billingIdentifier);

        if (adapter == null) {
            throw new ObjectNotFoundException(ClassEnum.BILLING_ADAPTER,
                    billingIdentifier);
        }
        model.getBillingAdapters().set(model.getSelectedIndex(),
                new BillingAdapterWrapper(adapter));
    }

    public POBillingAdapter getBillingAdapter(String billingIdentifier) {
        Response response = getBillingAdapterService()
                .getBillingAdapter(billingIdentifier);
        return response.getResult(POBillingAdapter.class);
    }

    public List<POBillingAdapter> getBillingAdapters() {
        Response response = getBillingAdapterService().getBillingAdapters();
        return response.getResultList(POBillingAdapter.class);
    }

    public BillingAdapterService getBillingAdapterService() {
        return billingAdapterService;
    }

    public String save() {
        setDisabledAddBtn(false);
        if (model == null) {
            return OUTCOME_ERROR;
        }

        String retVal = OUTCOME_SUCCESS;
        POBillingAdapter selectedBillingAdapter = getSelectedBillingAdapter();
        List<ConnectionPropertyItem> connectionProperties = model.getSelectedBillingAdapter().getConnectionProperties();
        selectedBillingAdapter.getConnectionProperties().clear();
        for (ConnectionPropertyItem connectionProperty : connectionProperties) {
            boolean exists = selectedBillingAdapter.getConnectionProperties().add(connectionProperty);
            if (!exists) {
                ui.handleException(new SaaSApplicationException());
                return OUTCOME_ERROR;
            }
        }
        try {
            getBillingAdapterService()
                    .saveBillingAdapter(selectedBillingAdapter);

            updateAdapter(selectedBillingAdapter.getBillingIdentifier());
            ui.handle(INFO_ADAPTER_SAVED);

        } catch (SaaSApplicationException e) {
            ui.handleException(e);
            retVal = OUTCOME_ERROR;
        }
        model.setInitialized(false);
        getInitialize();
        return retVal;
    }

    public String setDefaultAdapter() throws SaaSApplicationException {
        setDisabledAddBtn(false);
        model = getModel();

        if (model == null) {
            return OUTCOME_ERROR;
        }

        String retVal = OUTCOME_SUCCESS;

        int selectedIndex = model.getSelectedIndex();

        try {
            getBillingAdapterService().setDefaultBillingAdapter(
                    model.getBillingAdapters().get(selectedIndex).getAdapter());
            model.setInitialized(false);
            getInitialize();
            ui.handle(INFO_ADAPTER_SAVED);
        } catch (ObjectNotFoundException | NonUniqueBusinessKeyException
                | DuplicateAdapterException e) {
            ui.handleException(e);
            retVal = OUTCOME_ERROR;
        }
        initializeAdapters();
        return retVal;
    }

    public String deleteAdapter() throws SaaSApplicationException {
        setDisabledAddBtn(false);
        if (model == null) {
            return OUTCOME_ERROR;
        }

        String retVal = OUTCOME_SUCCESS;

        POBillingAdapter adapter = getSelectedBillingAdapter();
        try {
            getBillingAdapterService().deleteAdapter(adapter);
            model.setInitialized(false);
            getInitialize();
            ui.handle(INFO_ADAPTER_DELETED);
        } catch (DeletionConstraintException e) {
            ui.handleException(e);
            updateAdapter(adapter.getBillingIdentifier());
            retVal = OUTCOME_ERROR;
        }

        catch (ObjectNotFoundException e) {
            ui.handleException(e);
            retVal = OUTCOME_ERROR;
        }
        initializeAdapters();
        return retVal;
    }

    public void testConnection() throws SaaSApplicationException {

        POBillingAdapter adapter = getSelectedBillingAdapter();

        try {
            getBillingAdapterService().testConnection(adapter);
            ui.handle(INFO_BILLINGSYSTEM_CONNECTION_SUCCESS);

        } catch (BillingApplicationException bae) {
            if (bae.getCause() instanceof BillingAdapterConnectionException) {
                ui.handleException(
                        (BillingAdapterConnectionException) bae.getCause());
            } else {
                ui.handleException(bae);
            }
        }
    }

    public String addBillingAdapter() throws SaaSApplicationException {
        if (model == null) {
            return OUTCOME_ERROR;
        }
        initializeAdapters();

        // add a new billing adapter.
        model.addBillingAdapter(createNewAdapter());

        // Set the selected panel to the last one.
        int lastIndex = model.getBillingAdapters().size() - 1;
        setSelectedPanel(lastIndex);
        setDisabledAddBtn(true);
        return "";
    }

    /**
     * @return the disabled
     */
    public boolean isDisabledAddBtn() {
        return disabledAddBtn;
    }

    /**
     * @param disabled
     *            the disabled to set
     */
    public void setDisabledAddBtn(boolean disabled) {
        this.disabledAddBtn = disabled;
    }

    private POBillingAdapter getSelectedBillingAdapter() {
        int selectedIndex = model.getSelectedIndex();
        return model.getBillingAdapters().get(selectedIndex).getAdapter();
    }

    private void setSelectedPanel(int index) {
        String selectedPanel = model.getPanelBarItemPrefix() + ":" + index + ":"
                + model.getPanelBarItemSufix();
        model.setSelectedIndex(index);
        model.setSelectedPanel(selectedPanel);
    }

    public void setBillingAdapterService(
            BillingAdapterService billingAdapterService) {
        this.billingAdapterService = billingAdapterService;
    }

    public void setUiDelegate(UiDelegate ui) {
        this.ui = ui;
    }

    public void validateDuplicatedId(final FacesContext context,
            final UIComponent component, final Object value) {

        String billingId = value.toString();
        POBillingAdapter billingAdapter = getBillingAdapter(billingId);

        if (billingAdapter.getBillingIdentifier() != null && billingId.equals(billingAdapter.getBillingIdentifier())) {
            ((UIInput) component).setValid(false);
            addMessage(component.getClientId(context),
                    FacesMessage.SEVERITY_ERROR,
                    ERROR_BILLING_ID_ALREADY_EXISTS,
                    new Object[] { billingId });
        }
    }
}
