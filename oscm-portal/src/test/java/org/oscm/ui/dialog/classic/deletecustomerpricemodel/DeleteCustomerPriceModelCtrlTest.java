/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 15.10.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.classic.deletecustomerpricemodel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.faces.model.SelectItem;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;

import org.oscm.ui.beans.BaseBean;
import org.oscm.ui.beans.SessionBean;
import org.oscm.ui.common.UiDelegate;
import org.oscm.internal.components.response.Response;
import org.oscm.internal.pricemodel.POCustomer;
import org.oscm.internal.pricemodel.POCustomerService;
import org.oscm.internal.pricemodel.PriceModelService;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.SaaSApplicationException;

/**
 * @author weiser
 * 
 */
@SuppressWarnings("boxing")
public class DeleteCustomerPriceModelCtrlTest {

    private DeleteCustomerPriceModelCtrl ctrl;
    private PriceModelService pms;
    private DeleteCustomerPriceModelModel model;
    private SessionBean sessionBean;

    @Captor
    ArgumentCaptor<List<POCustomerService>> ac;

    @Before
    public void setup() throws Exception {
        ctrl = new DeleteCustomerPriceModelCtrl();

        ctrl.ui = mock(UiDelegate.class);
        pms = mock(PriceModelService.class);
        model = new DeleteCustomerPriceModelModel();
        sessionBean = mock(SessionBean.class);

        when(ctrl.ui.findService(eq(PriceModelService.class))).thenReturn(pms);
        when(
                ctrl.ui.findBean(eq(DeleteCustomerPriceModelCtrl.DELETE_CUSTOMER_PRICE_MODEL_MODEL)))
                .thenReturn(model);
        when(ctrl.ui.findBean(eq(DeleteCustomerPriceModelCtrl.SESSION_BEAN)))
                .thenReturn(sessionBean);

        POCustomer c = createPOCustomer("customerid", "name");
        when(pms.getCustomers()).thenReturn(Arrays.asList(c));
        when(pms.getCustomerSpecificServices(anyString())).thenReturn(
                new ArrayList<POCustomerService>());
    }

    @Test
    public void getSessionBean() {
        SessionBean sb = ctrl.getSessionBean();

        assertSame(sessionBean, sb);
    }

    @Test
    public void getSessionBean_Initialized() {
        ctrl.sessionBean = mock(SessionBean.class);

        SessionBean sb = ctrl.getSessionBean();

        assertSame(ctrl.sessionBean, sb);
        verifyZeroInteractions(ctrl.ui);
    }

    @Test
    public void getModel() {
        DeleteCustomerPriceModelModel m = ctrl.getModel();

        assertSame(model, m);
    }

    @Test
    public void getModel_Initialized() {
        ctrl.model = mock(DeleteCustomerPriceModelModel.class);

        DeleteCustomerPriceModelModel m = ctrl.getModel();

        assertSame(ctrl.model, m);
        verifyZeroInteractions(ctrl.ui);
    }

    @Test
    public void getPriceModelService() {
        PriceModelService s = ctrl.getPriceModelService();

        assertSame(pms, s);
    }

    @Test
    public void getPriceModelService_Initialized() {
        ctrl.priceModelService = mock(PriceModelService.class);

        PriceModelService s = ctrl.getPriceModelService();

        assertSame(ctrl.priceModelService, s);
        verifyZeroInteractions(ctrl.ui);
    }

    @Test
    public void getInitialize() throws SaaSApplicationException {
        ctrl.getInitialize();

        verify(pms).getCustomers();
        // please select and one service
        assertEquals(2, model.getCustomers().size());
        assertTrue(model.isInitialized());
    }

    @Test
    public void getInitialize_PreSelection() throws Exception {
        String orgId = "orgid";
        when(sessionBean.getSelectedCustomerId()).thenReturn(orgId);

        ctrl.getInitialize();

        verify(pms).getCustomers();
        verify(pms).getCustomerSpecificServices(eq(orgId));
        // please select and one service
        assertEquals(2, model.getCustomers().size());
        assertTrue(model.isInitialized());
        assertEquals(orgId, model.getSelectedOrgId());
    }

    @Test
    public void getInitialize_Initialized() {
        model.setInitialized(true);

        ctrl.getInitialize();

        verifyZeroInteractions(pms);
        assertTrue(model.isInitialized());
    }

    @Test
    public void getLabel() {
        POCustomer c = createPOCustomer("customerid", "");

        assertEquals("customerid", ctrl.getLabel(c));
    }

    @Test
    public void getLabel_WithName() {
        POCustomer c = createPOCustomer("customerid", "name");

        assertEquals("name (customerid)", ctrl.getLabel(c));
    }

    @Test
    public void createCustomerSelectItems() {
        List<SelectItem> items = ctrl.createCustomerSelectItems(Arrays
                .asList(createPOCustomer("id", "name")));

        assertEquals(2, items.size());
        assertEquals("", items.get(0).getValue());
        assertEquals("id", items.get(1).getValue());
    }

    @Test
    public void createCustomerSelectItems_Empty() {
        List<SelectItem> items = ctrl
                .createCustomerSelectItems(new ArrayList<POCustomer>());

        assertEquals(1, items.size());
        assertEquals("", items.get(0).getValue());
    }

    @Test
    public void selectOrDeselectAllServices() {
        model.setAllSelected(true);
        model.setServices(createServices(false));

        ctrl.selectOrDeselectAllServices();

        verifySelection(model, true);
    }

    @Test
    public void selectOrDeselectAllServices_Unselect() {
        model.setServices(createServices(true));

        ctrl.selectOrDeselectAllServices();

        verifySelection(model, false);
    }

    @Test
    public void getOrgName_NotFound() {
        model.setCustomers(Arrays.asList(new SelectItem("1", "A"),
                new SelectItem("2", "B")));
        model.setSelectedOrgId("3");

        String orgName = ctrl.getOrgName();

        assertEquals("3", orgName);
    }

    @Test
    public void getOrgName() {
        model.setCustomers(Arrays.asList(new SelectItem("1", "A"),
                new SelectItem("2", "B")));
        model.setSelectedOrgId("1");

        String orgName = ctrl.getOrgName();

        assertEquals("A", orgName);
    }

    @Test
    public void delete() throws Exception {
        MockitoAnnotations.initMocks(this);
        model.setServices(createServices(true));

        String outcome = ctrl.delete();

        assertEquals(BaseBean.OUTCOME_SUCCESS, outcome);
        assertFalse(model.isInitialized());
        verify(pms).deleteCustomerSpecificServices(ac.capture());
        List<POCustomerService> list = ac.getValue();
        assertEquals(5, list.size());
        for (int i = 0; i < list.size(); i++) {
            POCustomerService cs = list.get(i);
            assertEquals(i, cs.getKey());
            assertEquals("cs" + i, cs.getId());
        }
        verify(ctrl.ui).handle(any(Response.class),
                eq(BaseBean.INFO_PRICEMODEL_FOR_CUSTOMER_DELETED), anyString());
    }

    @Test
    public void toCustomerService() {
        POCustomerService pocs = new POCustomerService();
        pocs.setId("someid");
        pocs.setKey(1234);
        pocs.setVersion(5);

        CustomerService cs = ctrl.toCustomerService(pocs);

        assertEquals(pocs.getId(), cs.getId());
        assertEquals(pocs.getKey(), cs.getKey());
        assertEquals(pocs.getVersion(), cs.getVersion());
        assertFalse(cs.isSelected());
    }

    @Test
    public void selectedOrgIdChanged() throws Exception {
        String id = "selectedOrgId";
        model.setSelectedOrgId(id);
        when(pms.getCustomerSpecificServices(anyString()))
                .thenReturn(
                        Arrays.asList(new POCustomerService(),
                                new POCustomerService()));

        ctrl.selectedOrgIdChanged();

        assertEquals(2, model.getServices().size());
        verify(sessionBean).setSelectedCustomerId(eq(id));
    }

    @Test
    public void selectedOrgIdChanged_Empty() throws Exception {
        model.setSelectedOrgId("");

        ctrl.selectedOrgIdChanged();

        assertEquals(0, model.getServices().size());
        verify(sessionBean).setSelectedCustomerId(eq((String) null));
    }

    @Test
    public void selectedOrgIdChanged_Error() throws Exception {
        ObjectNotFoundException e = new ObjectNotFoundException();
        when(pms.getCustomerSpecificServices(anyString())).thenThrow(e);
        model.setSelectedOrgId("id");

        ctrl.selectedOrgIdChanged();

        verify(ctrl.ui).handleException(same(e));
        verify(sessionBean).setSelectedCustomerId(eq((String) null));
    }

    @Test
    public void isDeleteDisabled() {
        List<CustomerService> list = createServices(false);
        list.get(2).setSelected(true);
        model.setServices(list);

        assertFalse(ctrl.isDeleteDisabled());
    }

    @Test
    public void isDeleteDisabled_NoSelection() {
        model.setServices(createServices(false));

        assertTrue(ctrl.isDeleteDisabled());
    }

    @Test
    public void isDeleteDisabled_NoServices() {
        assertTrue(ctrl.isDeleteDisabled());
    }

    @Test
    public void isSelectDisabled() {
        ctrl.getInitialize();

        assertFalse(ctrl.isSelectDisabled());
    }

    @Test
    public void isSelectDisabled_NoCustomers() throws SaaSApplicationException {
        when(pms.getCustomers()).thenReturn(new ArrayList<POCustomer>());

        ctrl.getInitialize();

        // we have only please select in the customer selection list
        assertTrue(ctrl.isSelectDisabled());
    }

    private static void verifySelection(DeleteCustomerPriceModelModel m,
            boolean selected) {
        List<CustomerService> services = m.getServices();
        for (CustomerService cs : services) {
            assertEquals(selected, cs.isSelected());
        }
    }

    private static List<CustomerService> createServices(boolean selected) {
        List<CustomerService> result = new ArrayList<CustomerService>();
        for (int i = 0; i < 5; i++) {
            CustomerService cs = new CustomerService();
            cs.setSelected(selected);
            cs.setId("cs" + i);
            cs.setKey(i);
            result.add(cs);
        }
        return result;
    }

    private static POCustomer createPOCustomer(String id, String name) {
        POCustomer c = new POCustomer();
        c.setId(id);
        c.setName(name);
        return c;
    }
}
