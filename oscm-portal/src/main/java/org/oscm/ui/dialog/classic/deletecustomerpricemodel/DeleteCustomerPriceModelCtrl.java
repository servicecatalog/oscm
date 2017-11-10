/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 15.10.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.classic.deletecustomerpricemodel;

import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;

import org.oscm.string.Strings;
import org.oscm.ui.beans.BaseBean;
import org.oscm.ui.beans.SessionBean;
import org.oscm.ui.common.SelectItemBuilder;
import org.oscm.ui.common.UiDelegate;
import org.oscm.internal.components.response.Response;
import org.oscm.internal.pricemodel.POCustomer;
import org.oscm.internal.pricemodel.POCustomerService;
import org.oscm.internal.pricemodel.PriceModelService;
import org.oscm.internal.types.exception.SaaSApplicationException;

/**
 * @author weiser
 * 
 */
public class DeleteCustomerPriceModelCtrl {

    public static final String DELETE_CUSTOMER_PRICE_MODEL_MODEL = "deleteCustomerPriceModelModel";
    public static final String SESSION_BEAN = "sessionBean";

    UiDelegate ui = new UiDelegate();

    transient DeleteCustomerPriceModelModel model;
    transient PriceModelService priceModelService;
    transient SessionBean sessionBean;

    public String getInitialize() {

        DeleteCustomerPriceModelModel m = getModel();
        if (!m.isInitialized()) {
            List<POCustomer> customers = new ArrayList<POCustomer>();
            try {
                customers = getPriceModelService().getCustomers();
                List<SelectItem> items = createCustomerSelectItems(customers);
                m.setCustomers(items);
                m.setInitialized(true);
                m.setAllSelected(false);
                String orgId = getSessionBean().getSelectedCustomerId();
                if (!Strings.isEmpty(orgId)) {
                    m.setSelectedOrgId(orgId);
                    selectedOrgIdChanged();
                }
            } catch (SaaSApplicationException e) {
                ui.handleException(e);
            }
        }

        return "";
    }

    public String delete() throws SaaSApplicationException {

        DeleteCustomerPriceModelModel m = getModel();
        List<CustomerService> services = m.getServices();
        List<POCustomerService> toDelete = new ArrayList<POCustomerService>();
        for (CustomerService cs : services) {
            if (cs.isSelected()) {
                POCustomerService pocs = new POCustomerService();
                pocs.setId(cs.getId());
                pocs.setKey(cs.getKey());
                pocs.setVersion(cs.getVersion());
                toDelete.add(pocs);
            }
        }
        Response response = getPriceModelService()
                .deleteCustomerSpecificServices(toDelete);
        ui.handle(response, BaseBean.INFO_PRICEMODEL_FOR_CUSTOMER_DELETED,
                getOrgName());
        m.setInitialized(false);

        return BaseBean.OUTCOME_SUCCESS;
    }

    public void selectOrDeselectAllServices() {

        DeleteCustomerPriceModelModel m = getModel();
        boolean sel = m.isAllSelected();
        List<CustomerService> services = m.getServices();
        for (CustomerService cs : services) {
            cs.setSelected(sel);
        }

    }

    public void selectedOrgIdChanged() {

        DeleteCustomerPriceModelModel m = getModel();
        String selectedOrgId = m.getSelectedOrgId();
        SessionBean sb = getSessionBean();
        try {
            sb.setSelectedCustomerId(null);
            if (Strings.isEmpty(selectedOrgId)) {
                m.getServices().clear();
            } else {
                List<POCustomerService> services = getPriceModelService()
                        .getCustomerSpecificServices(selectedOrgId);
                List<CustomerService> list = new ArrayList<CustomerService>();
                for (POCustomerService pocs : services) {
                    list.add(toCustomerService(pocs));
                }
                m.setServices(list);
                sb.setSelectedCustomerId(selectedOrgId);
            }
        } catch (SaaSApplicationException e) {
            ui.handleException(e);
            m.setSelectedOrgId(null);
        }

    }

    public boolean isDeleteDisabled() {

        List<CustomerService> list = getModel().getServices();
        boolean selected = false;
        for (int i = 0; i < list.size() && !selected; i++) {
            selected = list.get(i).isSelected();
        }

        return !selected;
    }

    public boolean isSelectDisabled() {

        List<SelectItem> customers = getModel().getCustomers();
        boolean result = customers.size() <= 1;

        return result;
    }

    CustomerService toCustomerService(POCustomerService pocs) {

        CustomerService cs = new CustomerService();
        cs.setId(pocs.getId());
        cs.setKey(pocs.getKey());
        cs.setVersion(pocs.getVersion());

        return cs;
    }

    String getOrgName() {

        DeleteCustomerPriceModelModel m = getModel();
        String orgId = m.getSelectedOrgId();
        String result = orgId;
        List<SelectItem> customers = m.getCustomers();
        for (SelectItem si : customers) {
            if (si.getValue().equals(orgId)) {
                result = si.getLabel();
                break;
            }
        }

        return result;
    }

    DeleteCustomerPriceModelModel getModel() {

        if (model == null) {
            model = ui.findBean(DELETE_CUSTOMER_PRICE_MODEL_MODEL);
        }

        return model;
    }

    PriceModelService getPriceModelService() {

        if (priceModelService == null) {
            priceModelService = ui.findService(PriceModelService.class);
        }

        return priceModelService;
    }

    SessionBean getSessionBean() {

        if (sessionBean == null) {
            sessionBean = ui.findBean(SESSION_BEAN);
        }

        return sessionBean;
    }

    String getLabel(POCustomer c) {

        String result;
        if (Strings.isEmpty(c.getName())) {
            result = c.getId();
        } else {
            result = String.format("%s (%s)", c.getName().trim(), c.getId());
        }

        return result;
    }

    List<SelectItem> createCustomerSelectItems(List<POCustomer> customers) {

        List<SelectItem> items = new ArrayList<SelectItem>();
        SelectItemBuilder sib = new SelectItemBuilder(ui);
        items.add(sib.pleaseSelect(""));
        for (POCustomer c : customers) {
            items.add(new SelectItem(c.getId(), getLabel(c)));
        }

        return items;
    }

}
