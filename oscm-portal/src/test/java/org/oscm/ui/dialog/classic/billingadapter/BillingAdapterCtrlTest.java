/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 4 maj 2016                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.classic.billingadapter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;

import org.junit.Before;
import org.junit.Test;
import org.oscm.internal.billingadapter.BillingAdapterService;
import org.oscm.internal.billingadapter.ConnectionPropertyItem;
import org.oscm.internal.billingadapter.POBillingAdapter;
import org.oscm.internal.components.response.Response;
import org.oscm.internal.types.exception.*;
import org.oscm.ui.common.UiDelegate;

/**
 * @author BadziakP
 *
 */
public class BillingAdapterCtrlTest {

    private BillingAdapterCtrl bean;
    private BillingAdapterModel model;
    private BillingAdapterService billingAdapterService;
    private UiDelegate ui;

    @Before
    public void setup() throws Exception {
        bean = spy(new BillingAdapterCtrl() {
            @Override
            protected void addMessage(String clientId, FacesMessage.Severity severity, String key, Object[] params) {

            }
            @Override
            protected void addMessage(String clientId, FacesMessage.Severity severity, String key) {

            }
            @Override
            protected void addMessage(String clientId, FacesMessage.Severity severity, String key, String param) {

            }
        });
        model = spy(new BillingAdapterModel());
        model.addBillingAdapter(createBillingAdapter());
        bean.setModel(model);
        billingAdapterService = mock(BillingAdapterService.class);
        doReturn(billingAdapterService).when(bean).getBillingAdapterService();
        ui = mock(UiDelegate.class);
        bean.setUiDelegate(ui);

        doNothing().when(ui).handle(any(String.class));
    }

    protected void decorateInBillingAdapters() {
        List<POBillingAdapter> billingAdapters = new ArrayList<>();
        billingAdapters.add(createBillingAdapter());
        doReturn(billingAdapters).when(bean).getBillingAdapters();
    }

    @Test
    public void addBillingAdapterTest() throws SaaSApplicationException {
        // given
        doNothing().when(model).addBillingAdapter(any(POBillingAdapter.class));
        doReturn(1).when(bean).reinitializeAdapters();
        // when
        bean.addBillingAdapter();
        // then
        verify(bean, times(1)).setDisabledAddBtn(true);
        verify(model, times(1)).setSelectedIndex(1);
    }

    @Test
    public void testConnectionTest() throws SaaSApplicationException {
        doReturn(new Response()).when(billingAdapterService).testConnection(any(POBillingAdapter.class));
        bean.testConnection();
        doThrow(new BillingApplicationException()).when(billingAdapterService).testConnection(any(POBillingAdapter.class));
        bean.testConnection();
        BillingApplicationException wrapper = mock(BillingApplicationException.class);
        when(wrapper.getCause()).thenReturn(new BillingAdapterConnectionException());
        doThrow(wrapper).when(billingAdapterService).testConnection(any(POBillingAdapter.class));
        bean.testConnection();
    }

    @Test
    public void saveTest()
            throws SaaSApplicationException {
        // given
        doReturn(new Response()).when(billingAdapterService)
                .saveBillingAdapter(any(POBillingAdapter.class));
        doReturn(createBillingAdapter()).when(bean)
                .getBillingAdapter(any(String.class));
        decorateInBillingAdapters();

        // when
        String result = bean.save();

        // then
        assertEquals(result, "success");
        verify(bean, times(1)).setDisabledAddBtn(false);
    }

    @Test
    public void saveTest1()
            throws SaaSApplicationException {
        // given
        doThrow(new ObjectNotFoundException()).when(billingAdapterService)
                .saveBillingAdapter(any(POBillingAdapter.class));
        POBillingAdapter billingAdapter = createBillingAdapter();
        doReturn(billingAdapter).when(bean)
                .getBillingAdapter(any(String.class));
        decorateInBillingAdapters();

        // when
        String result = bean.save();

        // then
        assertEquals(result, "error");
        verify(bean, times(1)).setDisabledAddBtn(false);
        billingAdapter.getConnectionProperties().add(model.getSelectedBillingAdapter().getConnectionProperties().get(0));
        result = bean.save();
        assertEquals(result, "error");
    }

    @Test
    public void setDefaultAdapterTest()
            throws SaaSApplicationException {
        // given
        doReturn(new Response()).when(billingAdapterService)
                .setDefaultBillingAdapter(any(POBillingAdapter.class));
        decorateInBillingAdapters();

        // when
        String result = bean.setDefaultAdapter();

        // then
        assertEquals(result, "success");
        verify(bean, times(1)).setDisabledAddBtn(false);
    }

    @Test
    public void setDefaultAdapterTestExc()
            throws SaaSApplicationException {
        // given
        doThrow(new ObjectNotFoundException()).when(billingAdapterService)
                .setDefaultBillingAdapter(any(POBillingAdapter.class));
        decorateInBillingAdapters();

        // when
        String result = bean.setDefaultAdapter();

        // then
        assertEquals(result, "error");
        verify(bean, times(1)).setDisabledAddBtn(false);
    }

    @Test
    public void deleteBillingAdapterTest() throws SaaSApplicationException {
        // given
        doReturn(new Response()).when(billingAdapterService)
                .deleteAdapter(any(POBillingAdapter.class));
        decorateInBillingAdapters();

        // when
        String result = bean.deleteAdapter();

        // then
        assertEquals(result, "success");
        verify(bean, times(1)).setDisabledAddBtn(false);
    }

    @Test
    public void deleteBillingAdapterTestExc() throws SaaSApplicationException {
        // given
        doThrow(new DeletionConstraintException()).when(billingAdapterService)
                .deleteAdapter(any(POBillingAdapter.class));
        doNothing().when(bean).updateAdapter(anyString());
        decorateInBillingAdapters();

        // when
        String result = bean.deleteAdapter();

        // then
        assertEquals(result, "error");
        verify(bean, times(1)).updateAdapter(anyString());
    }

    @Test
    public void deleteBillingAdapterTestExc2() throws SaaSApplicationException {
        // given
        doThrow(new ObjectNotFoundException()).when(billingAdapterService)
                .deleteAdapter(any(POBillingAdapter.class));
        doNothing().when(bean).updateAdapter(anyString());
        decorateInBillingAdapters();

        // when
        String result = bean.deleteAdapter();

        // then
        assertEquals(result, "error");
    }

    @Test
    public void validateDuplicatedIds() throws SaaSApplicationException {
        FacesContext fc = mock(FacesContext.class);
        UIComponent component = mock(UIInput.class);
        doReturn(createBillingAdapter()).when(bean)
                .getBillingAdapter(any(String.class));

        bean.validateDuplicatedId(fc, component, "BILLING_ADAPTER1");
    }

    @Test(expected = ObjectNotFoundException.class)
    public void udpateAdapterNull() throws SaaSApplicationException {
        assertNotNull(bean.getBillingAdapterService());
        doReturn(new Response()).when(billingAdapterService)
                .getBillingAdapters();
        List<POBillingAdapter> billingAdapters = bean.getBillingAdapters();
        assertEquals(0, billingAdapters.size());

        doReturn(new Response()).when(billingAdapterService)
                .getBillingAdapter(anyString());
        bean.updateAdapter("adapter");
    }

    private POBillingAdapter createBillingAdapter() {
        POBillingAdapter adapter = new POBillingAdapter();
        adapter.setBillingIdentifier("BILLING_ADAPTER1");
        adapter.setName("BILLING_ADAPTER1");
        adapter.setNativeBilling(false);
        adapter.setDefaultAdapter(false);
        Set<ConnectionPropertyItem> connectionProperties = new HashSet<>();
        ConnectionPropertyItem connectionPropertyItem = new ConnectionPropertyItem(
                "Key1", "Value1");
        connectionProperties.add(connectionPropertyItem);
        adapter.setConnectionProperties(connectionProperties);
        return adapter;
    }
}
