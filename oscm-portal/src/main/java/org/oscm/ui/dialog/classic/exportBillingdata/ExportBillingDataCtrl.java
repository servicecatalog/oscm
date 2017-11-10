/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.ui.dialog.classic.exportBillingdata;

import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.faces.validator.ValidatorException;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.ui.common.JSFUtils;
import org.oscm.ui.common.SelectItemBuilder;
import org.oscm.ui.common.UiDelegate;
import org.oscm.ui.delegates.ServiceLocator;
import org.oscm.ui.validator.DateFromToValidator;
import org.oscm.internal.billingdataexport.ExportBillingDataService;
import org.oscm.internal.billingdataexport.POBillingDataExport;
import org.oscm.internal.billingdataexport.POOrganization;
import org.oscm.internal.billingdataexport.PORevenueShareExport;
import org.oscm.internal.components.response.Response;
import org.oscm.internal.types.enumtypes.BillingSharesResultType;
import org.oscm.internal.types.exception.OrganizationAuthoritiesException;
import org.oscm.internal.types.exception.SaaSApplicationException;

public class ExportBillingDataCtrl implements Serializable {

    private static final long serialVersionUID = 8707293755759589418L;

    private static final String SUCCESS = "success";
    protected static final String ERROR_SHOW_BILLING_DATA = "operator.showBillingData.error";
    private static final Log4jLogger logger = LoggerFactory
            .getLogger(ExportBillingDataCtrl.class);

    ExportBillingDataService exportBillingDataService;

    ExportBillingDataModel model;

    UiDelegate ui = new UiDelegate();
    DateFromToValidator validator = new DateFromToValidator();

    /**
     * initializer method called by <adm:initialize />
     * 
     * @return empty string (due to value jsf binding )
     * 
     *         workaround: to be refactored under jsf 2.0
     * 
     */
    public String getInitializeExportBillingData() {
        initializeModel();
        return "";
    }

    /**
     * indicates whether the billingtypeselector step should be displayed.
     * broker , marketplaceOwner and customer do not show it
     * 
     * @return
     */
    public boolean isShowBillingTypeSelectStep() {
        if (model.getBillingSharesResultTypes() == null)
            return false;
        if (model.getBillingSharesResultTypes().contains(
                BillingSharesResultType.RESELLER))
            return true;
        if (model.getBillingSharesResultTypes().contains(
                BillingSharesResultType.SUPPLIER))
            return true;
        if (model.isPlatformOperator())
            return false;
        if (model.getBillingSharesResultTypes().contains(
                BillingSharesResultType.BROKER))
            return false;
        else
            return false;
    }

    public boolean isShowSharesExport() {
        if (model.getSelectedBillingDataType() == null)
            return false;
        if (model.getSelectedBillingDataType().equals(
                BillingDataType.RevenueShare))
            return true;
        else
            return false;
    }

    /**
     * indicates whether the customer table step should be displayed.
     * 
     * @return
     */
    public boolean isShowCustomerSelectStep() {
        if (model.getSelectedBillingDataType() == null)
            return false;
        if (model.getSelectedBillingDataType().equals(
                BillingDataType.RevenueShare))
            return false;

        if (model.getBillingSharesResultTypes().contains(
                BillingSharesResultType.RESELLER))
            return true;
        if (model.getBillingSharesResultTypes().contains(
                BillingSharesResultType.SUPPLIER))
            return true;
        return false;
    }

    ExportBillingDataService getExportBillingDataService() {
        if (exportBillingDataService == null) {
            exportBillingDataService = new ServiceLocator()
                    .findService(ExportBillingDataService.class);
        }
        return exportBillingDataService;
    }

    void initializeModel() {
        if (model.isInitialized() == false) {
            List<BillingSharesResultType> resultTypes = getExportBillingDataService()
                    .getBillingShareResultTypes();

            model.setBillingSharesResultTypes(resultTypes);
            initializeCurrentUser();
            initializeBillingDataTypeOptions();
            initializeSelectedBillingDataType();

            initializeSharesResultTypeOptions();
            initializeSelectedSharesResultType();

            model.setInitialized(true);
        }
    }

    void initializeCurrentUser() {
        model.setPlatformOperator(getExportBillingDataService()
                .isPlatformOperator());
        model.setSupplierOrReseller(getExportBillingDataService()
                .isSupplierOrReseller());
    }

    private void initializeSelectedBillingDataType() {
        if (model.getBillingSharesResultTypes().contains(
                BillingSharesResultType.BROKER)
                || model.isPlatformOperator()) {
            model.setSelectedBillingDataType(BillingDataType.RevenueShare);
        }
        if (isOnlyMarketplaceOwner()) {
            model.setSelectedBillingDataType(BillingDataType.RevenueShare);
        }

    }

    private boolean isOnlyMarketplaceOwner() {
        if (model.getBillingSharesResultTypes().size() == 1
                && model.getBillingSharesResultTypes().contains(
                        BillingSharesResultType.MARKETPLACE_OWNER))
            return true;
        else
            return false;
    }

    void initSelectableOrganizations() {

        List<POOrganization> organizations = getExportBillingDataService()
                .getCustomers();
        List<Customer> customerOrgs = new ArrayList<Customer>();
        if (organizations != null) {
            for (POOrganization po : organizations) {
                Customer customer = new Customer(po);
                customerOrgs.add(customer);
            }
        }
        model.setCustomers(customerOrgs);
    }

    /**
     * selectable options switching different dialog types only for controlling
     * view options.
     */
    void initializeBillingDataTypeOptions() {
        List<BillingDataType> billingDataTypes = null;
        if (model.isSupplierOrReseller()) {
            billingDataTypes = Arrays.asList(BillingDataType.values());
        } else {
            billingDataTypes = Arrays.asList(BillingDataType.RevenueShare);
        }
        List<SelectItem> billingDataTypeOptions = new SelectItemBuilder(ui)
                .buildSelectItems(billingDataTypes, "BillingDataType");
        model.setBillingDataTypeOptions(billingDataTypeOptions);
    }

    void initializeSharesResultTypeOptions() {
        List<SelectItem> resultTypeOptions = new SelectItemBuilder(ui)
                .buildSelectItems(model.getBillingSharesResultTypes(),
                        "BillingSharesResultType");
        model.setSharesResultTypeOptions(resultTypeOptions);
    }

    void initializeSelectedSharesResultType() {
        if (model.getSharesResultTypeOptions().size() == 1) {
            model.setSelectedSharesResultType((BillingSharesResultType) model
                    .getSharesResultTypeOptions().get(0).getValue());
        }
    }

    /**
     * value change listener for marketplace chooser
     * 
     * @param event
     */
    public void billingTypeChanged(ValueChangeEvent event) {
        BillingDataType selectedBillingType = (BillingDataType) event
                .getNewValue();
        model.setSelectedBillingDataType(selectedBillingType);
        model.setFromDate(null);
        model.setToDate(null);
        model.setSelectedSharesResultType(null);
        if (BillingDataType.CustomerBillingData.equals(model
                .getSelectedBillingDataType())) {
            initSelectableOrganizations();
        }
    }

    /**
     * action method for export button
     * 
     * @return null: stay on same page
     */
    public String getSharesData() {

        PORevenueShareExport poBillingDataExport = new PORevenueShareExport();
        poBillingDataExport.setFrom(model.getFromDate());
        poBillingDataExport.setTo(model.getToDate());
        poBillingDataExport.setRevenueShareType(model
                .getSelectedSharesResultType());

        try {
            model.setBillingData(null);
            Response response = getExportBillingDataService()
                    .exportRevenueShares(poBillingDataExport);
            model.setBillingData(response.getResult(byte[].class));
            return SUCCESS;
        } catch (SaaSApplicationException e) {
            ui.handleException(e);
            // reset as data export doesn't modify any data
            ui.resetDirty();
            return null;
        }
    }

    public void setModel(ExportBillingDataModel model) {
        this.model = model;
    }

    public ExportBillingDataModel getModel() {
        return model;
    }

    /**
     * Retrieves the billing data with the specified start and end date and the
     * for the specified list of customers. Temporarily saves the data in the
     * bean for showing it later.
     * 
     * @return the logical outcome
     * @throws OrganizationAuthoritiesException
     */
    public String getCustomerBillingData() {
        POBillingDataExport poBillingDataExport = new POBillingDataExport();
        poBillingDataExport.setFrom(model.getFromDate());
        poBillingDataExport.setTo(model.getToDate());
        poBillingDataExport.setOrganizationIds(model
                .getSelectedOrganizationIds());
        try {
            model.setBillingData(null);
            Response response = getExportBillingDataService()
                    .exportBillingData(poBillingDataExport);
            model.setBillingData(response.getResult(byte[].class));
        } catch (SaaSApplicationException e) {
            ui.handleException(e);
            // reset as data export doesn't modify any data
            ui.resetDirty();
        }
        return null;
    }

    public String showBillingData() throws IOException {

        if (model.getBillingData() == null) {
            JSFUtils.addMessage(null, FacesMessage.SEVERITY_ERROR,
                    ERROR_SHOW_BILLING_DATA, null);
            logger.logError(LogMessageIdentifier.ERROR_EXECUTE_SHOW_BILLING_DATA_WITH_NULL_DATA);
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd");
        String filename = sdf.format(Calendar.getInstance().getTime())
                + "_BillingData.xml";
        String contentType = "text/xml";
        JSFUtils.writeContentToResponse(model.getBillingData(), filename,
                contentType);

        return "success";
    }

    public void validateFromAndToDate(final FacesContext context,
            final UIComponent toValidate, final Object value) {
        model.setFailedDateComponentId("");
        validator.setToDate(getModel().getToDate());
        validator.setFromDate(getModel().getFromDate());
        String clientId = toValidate.getClientId(context);
        try {
            validator.validate(context, toValidate, value);
        } catch (ValidatorException ex) {
            context.addMessage(
                    clientId,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, ex
                            .getLocalizedMessage(), null));
            model.setFailedDateComponentId(clientId);
        }

    }

    /**
     * Checks if billing data has been read.
     * 
     * @return <code>true</code> if billing data is available otherwise
     *         <code>false</code>.
     */
    public boolean isDataAvailable() {
        return model.getBillingData() != null;
    }

    public boolean isCustomerExportButtonDisabled() {
        if (model.getFromDate() == null || model.getToDate() == null
                || model.getAnyCustomerSelected().equals("0")) {
            return true;
        }
        return model.getFromDate().after(model.getToDate());
    }

    public boolean isSharesExportButtonDisabled() {
        if (model.getFromDate() == null || model.getToDate() == null
                || model.getSelectedSharesResultType() == null) {
            return true;
        }
        return model.getFromDate().after(model.getToDate());
    }

    /*
     * value change listener for log chooser
     */
    public void processOrgRoleChange(ValueChangeEvent event) {
        BillingSharesResultType role = (BillingSharesResultType) event
                .getNewValue();
        model.setSelectedSharesResultType(role);
    }
}
