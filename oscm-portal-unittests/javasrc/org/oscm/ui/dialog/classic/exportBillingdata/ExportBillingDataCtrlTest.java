/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2018
 *******************************************************************************/

package org.oscm.ui.dialog.classic.exportBillingdata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.faces.validator.ValidatorException;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import org.oscm.ui.common.SelectItemBuilder;
import org.oscm.ui.common.UiDelegate;
import org.oscm.ui.stubs.FacesContextStub;
import org.oscm.ui.stubs.UiDelegateStub;
import org.oscm.ui.validator.DateFromToValidator;
import org.oscm.internal.billingdataexport.ExportBillingDataService;
import org.oscm.internal.billingdataexport.POBillingDataExport;
import org.oscm.internal.billingdataexport.POOrganization;
import org.oscm.internal.billingdataexport.PORevenueShareExport;
import org.oscm.internal.components.response.Response;
import org.oscm.internal.types.enumtypes.BillingSharesResultType;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exceptions.NoBilingSharesDataAvailableException;

public class ExportBillingDataCtrlTest {

    private ExportBillingDataCtrl ctrl;
    private ExportBillingDataModel model;

    private UiDelegateStub webContainer;

    @BeforeClass
    public static void setupClass() {
        new FacesContextStub(Locale.ENGLISH) {
            public void addMessage(String arg0, FacesMessage arg1) {
                // the implementation of the stub throws an
                // OperationNotSupportedException
            }
        };
    }

    void setupMocks() {
        webContainer = new UiDelegateStub() {

            @SuppressWarnings("unchecked")
            @Override
            public <T> T findBean(String beanName) {
                if ("exportBillingDataModel".equals(beanName)) {
                    return (T) new ExportBillingDataModel();
                }
                return null;
            }
        };
        ctrl.ui = webContainer;
        ctrl.validator = mock(DateFromToValidator.class);
    }

    @Before
    public void setup() {
        ctrl = new ExportBillingDataCtrl();
        model = new ExportBillingDataModel();
        ctrl.model = model;
        setupMocks();
    }

    @Test
    public void showBillingDataStep_NOT_forPlatformOperator() {
        // given
        model.setPlatformOperator(true);
        // when
        boolean result = ctrl.isShowBillingTypeSelectStep();
        // then
        assertFalse(result);
    }

    @Test
    public void showBillingDataStep_forPlatformOperator() {
        // given
        model.setPlatformOperator(true);
        List<BillingSharesResultType> billingSharesResultTypes = new ArrayList<BillingSharesResultType>();
        billingSharesResultTypes.add(BillingSharesResultType.MARKETPLACE_OWNER);
        model.setBillingSharesResultTypes(billingSharesResultTypes);
        // when
        boolean result = ctrl.isShowBillingTypeSelectStep();
        // then
        assertFalse(result);
    }

    @Test
    public void showBillingDataStep_forSupplier() {
        // given
        model.setBillingSharesResultTypes(Arrays
                .asList(BillingSharesResultType.SUPPLIER));
        // when
        boolean result = ctrl.isShowBillingTypeSelectStep();
        // then
        assertTrue(result);
    }

    @Test
    public void showBillingDataStep_NOTforBroker() {
        // given
        model.setBillingSharesResultTypes(Arrays
                .asList(BillingSharesResultType.BROKER));
        // when
        boolean result = ctrl.isShowBillingTypeSelectStep();
        // then
        assertFalse(result);
    }

    @Test
    public void showBillingDataStep_forReseller() {
        // given
        model.setBillingSharesResultTypes(Arrays
                .asList(BillingSharesResultType.RESELLER));
        // when
        boolean result = ctrl.isShowBillingTypeSelectStep();
        // then
        assertTrue(result);
    }

    @Test
    public void showSharesExport_notInitialized() {
        // given
        model.setSelectedBillingDataType(null);
        // when
        boolean result = ctrl.isShowSharesExport();
        // then
        assertFalse(result);
    }

    @Test
    public void showSharesExport_RevenueSharesSelected() {
        // given
        model.setSelectedBillingDataType(BillingDataType.RevenueShare);
        // when
        boolean result = ctrl.isShowSharesExport();
        // then
        assertTrue(result);
    }

    @Test
    public void showSharesExport_CustomerBillingSelected() {
        // given
        model.setSelectedBillingDataType(BillingDataType.CustomerBillingData);
        // when
        boolean result = ctrl.isShowSharesExport();
        // then
        assertFalse(result);
    }

    @Test
    public void showCustomerSelectStep_Null() {
        // given
        model.setSelectedBillingDataType(null);
        // when
        boolean result = ctrl.isShowCustomerSelectStep();
        // then
        assertFalse(result);
    }

    @Test
    public void showCustomerSelectStep_RevenueShareSelected() {
        // given
        model.setSelectedBillingDataType(BillingDataType.RevenueShare);
        // when
        boolean result = ctrl.isShowCustomerSelectStep();
        // then
        assertFalse(result);
    }

    @Test
    public void showCustomerSelectStep_VisibleForReseller() {
        // given
        model.setSelectedBillingDataType(BillingDataType.CustomerBillingData);
        List<BillingSharesResultType> orgRoles = new ArrayList<BillingSharesResultType>();
        orgRoles.add(BillingSharesResultType.RESELLER);
        model.setBillingSharesResultTypes(orgRoles);

        // when
        boolean result = ctrl.isShowCustomerSelectStep();
        // then
        assertTrue(result);
    }

    @Test
    public void showCustomerSelectStep_VisibleForSupplier() {
        // given
        model.setSelectedBillingDataType(BillingDataType.CustomerBillingData);
        List<BillingSharesResultType> orgRoles = new ArrayList<BillingSharesResultType>();
        orgRoles.add(BillingSharesResultType.SUPPLIER);
        model.setBillingSharesResultTypes(orgRoles);

        // when
        boolean result = ctrl.isShowCustomerSelectStep();
        // then
        assertTrue(result);
    }

    @Test
    public void showCustomerSelectStep_NotVisibleForBroker() {
        // given
        List<BillingSharesResultType> orgRoles = new ArrayList<BillingSharesResultType>();
        orgRoles.add(BillingSharesResultType.BROKER);
        model.setBillingSharesResultTypes(orgRoles);

        // when
        boolean result = ctrl.isShowCustomerSelectStep();
        // then
        assertFalse(result);
    }

    @Test
    public void sharesExportButtonDisabled_allMandatoryFieldsSet() {
        // given
        model.setFromDate(new Date());
        model.setToDate(new Date());
        model.setSelectedSharesResultType(BillingSharesResultType.BROKER);
        // when
        boolean result = ctrl.isSharesExportButtonDisabled();
        // then
        assertFalse(result);
    }

    @Test
    public void sharesExportButtonDisabled_MandatoryFieldsMissing() {
        // given
        model.setFromDate(new Date());
        model.setToDate(new Date());
        // when
        boolean result = ctrl.isSharesExportButtonDisabled();
        // then
        assertTrue(result);
    }

    @Test
    public void isCustomerExportButtonDisabled_allMandatoryFieldsSet() {
        // given
        model.setFromDate(new Date());
        model.setToDate(new Date());
        model.setAnyCustomerSelected("1");
        // when
        boolean result = ctrl.isCustomerExportButtonDisabled();
        // then
        assertFalse(result);
    }

    @Test
    public void isCustomerExportButtonDisabled_missingMandatoryFields() {
        // given
        model.setFromDate(new Date());
        model.setToDate(new Date());
        // when
        boolean result = ctrl.isCustomerExportButtonDisabled();
        // then
        assertTrue(result);
    }

    @Test
    public void getSharesData() throws Exception {
        // given
        Response r = new Response();
        r.setResults(new ArrayList<Object>());
        ctrl.exportBillingDataService = Mockito
                .mock(ExportBillingDataService.class);
        when(
                ctrl.exportBillingDataService
                        .exportRevenueShares(any(PORevenueShareExport.class)))
                .thenReturn(r);

        // when
        ctrl.getSharesData();

        // then
        verify(ctrl.exportBillingDataService, times(1)).exportRevenueShares(
                any(PORevenueShareExport.class));
        assertFalse(webContainer.hasErrors());
    }

    @Test
    public void getSharesData_Error() throws Exception {
        // given
        ctrl.model.setBillingData(new byte[] { 1, 2, 3 });
        ctrl.exportBillingDataService = mock(ExportBillingDataService.class);
        doThrow(new OperationNotPermittedException()).when(
                ctrl.exportBillingDataService).exportRevenueShares(
                any(PORevenueShareExport.class));

        // when
        ctrl.getSharesData();

        // then
        verify(ctrl.exportBillingDataService, times(1)).exportRevenueShares(
                any(PORevenueShareExport.class));
        assertTrue(webContainer.isResetDirtyCalled());
        assertTrue(webContainer.hasErrors());
        assertNull(ctrl.model.getBillingData());
    }

    // getCustomerBillingData
    @Test
    public void getCustomerBillingData() throws Exception {
        // given
        ctrl.model.setCustomers(new ArrayList<Customer>());
        Response r = new Response();
        r.setResults(new ArrayList<Object>());
        ctrl.exportBillingDataService = Mockito
                .mock(ExportBillingDataService.class);
        when(
                ctrl.exportBillingDataService
                        .exportBillingData(any(POBillingDataExport.class)))
                .thenReturn(r);

        // when
        ctrl.getCustomerBillingData();

        // then
        verify(ctrl.exportBillingDataService, times(1)).exportBillingData(
                any(POBillingDataExport.class));
        assertFalse(webContainer.hasErrors());

    }

    @Test
    public void getCustomerBillingData_Error() throws Exception {
        // given
        ctrl.model.setBillingData(new byte[] { 1, 2, 3 });
        ctrl.model.setCustomers(new ArrayList<Customer>());
        ctrl.exportBillingDataService = mock(ExportBillingDataService.class);
        doThrow(new NoBilingSharesDataAvailableException()).when(
                ctrl.exportBillingDataService).exportBillingData(
                any(POBillingDataExport.class));

        // when
        String outcome = ctrl.getCustomerBillingData();

        // then
        verify(ctrl.exportBillingDataService, times(1)).exportBillingData(
                any(POBillingDataExport.class));
        assertTrue(webContainer.isResetDirtyCalled());
        assertTrue(webContainer.hasErrors());
        assertTrue(outcome == null);
        assertNull(ctrl.model.getBillingData());
    }

    @Test
    public void initSelectableOrganizations() {
        // given
        model.setCustomers(null);
        List<POOrganization> testOrgs = new ArrayList<POOrganization>();
        POOrganization testOrg = new POOrganization();
        testOrgs.add(testOrg);
        ctrl.exportBillingDataService = Mockito
                .mock(ExportBillingDataService.class);
        when(ctrl.exportBillingDataService.getCustomers()).thenReturn(testOrgs);

        // when
        ctrl.initSelectableOrganizations();

        // then
        assertFalse(model.getCustomers() == null);
    }

    @Test
    public void initializeModel() {
        // given
        ctrl.model = new ExportBillingDataModel();
        ctrl.model.initialized = false;

        List<BillingSharesResultType> testResults = new ArrayList<BillingSharesResultType>();
        ctrl.exportBillingDataService = Mockito
                .mock(ExportBillingDataService.class);
        when(ctrl.exportBillingDataService.getBillingShareResultTypes())
                .thenReturn(testResults);

        // when
        ctrl.initializeModel();

        // then
        assertTrue(ctrl.model.initialized);
        assertFalse(ctrl.model == null);
    }

    @Test
    public void initializeModel_OnlyOperatorRole() {
        // given
        ctrl.model = new ExportBillingDataModel();
        ctrl.model.initialized = false;
        ctrl.model.setSupplierOrReseller(false);

        List<BillingSharesResultType> testResults = new ArrayList<BillingSharesResultType>();
        ctrl.exportBillingDataService = Mockito
                .mock(ExportBillingDataService.class);
        when(ctrl.exportBillingDataService.getBillingShareResultTypes())
                .thenReturn(testResults);

        List<BillingDataType> billingDataTypes = Arrays.asList(BillingDataType.RevenueShare);
        List<SelectItem> selectItems = new SelectItemBuilder(new UiDelegate())
                .buildSelectItems(billingDataTypes, "BillingDataType");

        SelectItemBuilder select = Mockito.mock(SelectItemBuilder.class);
        when(select.buildSelectItems(anyListOf(BillingDataType.class), anyString()))
                .thenReturn(selectItems);

        // when
        ctrl.initializeModel();

        // then
        assertFalse(ctrl.model.getBillingDataTypeOptions().isEmpty());
    }

    @Test
    public void billingTypeChanged() {
        // given
        UIInput input = mock(UIInput.class);
        ValueChangeEvent e = new ValueChangeEvent(input, null,
                BillingDataType.RevenueShare);

        // when
        ctrl.billingTypeChanged(e);

        // then
        assertEquals(BillingDataType.RevenueShare,
                model.getSelectedBillingDataType());
        assertNull(model.getFromDate());
        assertNull(model.getToDate());
        assertNull(model.getSelectedSharesResultType());
    }

    @Test
    public void billingTypeChanged_bug10285() {
        // given
        UIInput input = mock(UIInput.class);
        ValueChangeEvent e = new ValueChangeEvent(input, null, null);

        // when
        ctrl.billingTypeChanged(e);

        // then
        assertEquals(null, model.getSelectedBillingDataType());
    }

    @Test
    public void validateFromAndToDate() {
        // given
        UIComponent dateComponent = mock(UIComponent.class);
        FacesContext facesContext = mock(FacesContext.class);
        model.setFailedDateComponentId(null);
        doReturn("toDate").when(dateComponent).getClientId(facesContext);
        doThrow(new ValidatorException(new FacesMessage("failed"))).when(
                ctrl.validator).validate(any(FacesContext.class),
                any(UIComponent.class), any(Date.class));

        // when
        ctrl.validateFromAndToDate(facesContext, dateComponent, new Date());

        // then
        assertEquals("toDate", ctrl.model.getFailedDateComponentId());
        verify(facesContext, times(1)).addMessage(anyString(),
                any(FacesMessage.class));

    }

    @Test
    public void processOrgRoleChange() {
        // given
        ValueChangeEvent event = mock(ValueChangeEvent.class);
        model.setSelectedSharesResultType(null);
        doReturn(BillingSharesResultType.SUPPLIER).when(event).getNewValue();

        // when
        ctrl.processOrgRoleChange(event);

        assertNotNull(model.getSelectedSharesResultType());

    }

}
