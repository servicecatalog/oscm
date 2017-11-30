/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 04.03.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.classic.manageoperatorrevenueshare;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import org.junit.Before;
import org.junit.Test;

import org.oscm.ui.delegates.ServiceLocator;
import org.oscm.ui.stubs.UiDelegateStub;
import org.oscm.internal.components.POService;
import org.oscm.internal.components.ServiceSelector;
import org.oscm.internal.components.response.Response;
import org.oscm.internal.pricing.POOperatorPriceModel;
import org.oscm.internal.pricing.PORevenueShare;
import org.oscm.internal.pricing.PricingService;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.ServiceOperationException;
import org.oscm.internal.types.exception.ValidationException;

/**
 * Unit tests for ManageOperatorRevenueShareCtrl.
 * 
 * @author barzu
 */
public class ManageOperatorRevenueShareCtrlTest {

    private ManageOperatorRevenueShareCtrl ctrl;
    private PricingService pricingService;
    private UiDelegateStub ui;
    private List<POService> services;

    @Before
    public void setup() {
        ctrl = spy(new ManageOperatorRevenueShareCtrl());
        ctrl.setModel(new ManageOperatorRevenueShareModel());
        ctrl.sl = mock(ServiceLocator.class);
        ui = new UiDelegateStub();
        ctrl.ui = ui;

        ServiceSelector serviceSelector = mock(ServiceSelector.class);
        doReturn(serviceSelector).when(ctrl.sl).findService(
                eq(ServiceSelector.class));

        services = new ArrayList<POService>();
        Response response = new Response(services);
        doReturn(response).when(serviceSelector).getTemplateServices();

        pricingService = mock(PricingService.class);
        doReturn(pricingService).when(ctrl.sl)
                .findService(PricingService.class);
    }

    @Test
    public void getInitializePage() {
        // given
        services.add(new POService());
        services.add(new POService());

        // when
        String returnValue = ctrl.getInitializePage();

        // then
        assertEquals("", returnValue);
        assertNull(ctrl.getModel().getOperatorRevenueShare());
        assertEquals(2, ctrl.getModel().getTemplates().size());
    }

    @Test
    public void initializeModel_serviceNotSelected() {
        // given
        services.add(new POService());
        services.add(new POService());

        // when
        ctrl.initializeModel();

        assertNull(ctrl.getModel().getOperatorRevenueShare());
        assertEquals(2, ctrl.getModel().getTemplates().size());
    }

    @Test
    public void initializeModel_serviceSelected() throws Exception {
        // given
        services.add(new POService());
        services.add(new POService());
        ctrl.getModel().setSelectedTemplateKey(101L);
        POOperatorPriceModel pm = new POOperatorPriceModel();
        pm.setRevenueShare(new PORevenueShare());
        doReturn(new Response(pm)).when(pricingService)
                .getOperatorRevenueShare(anyLong());

        // when
        ctrl.initializeModel();

        assertNotNull(ctrl.getModel().getOperatorRevenueShare());
        assertEquals(2, ctrl.getModel().getTemplates().size());
    }

    @Test
    public void intializeRevenueShares() throws Exception {
        // given
        POOperatorPriceModel pm = new POOperatorPriceModel();
        pm.setRevenueShare(new PORevenueShare());
        pm.getRevenueShare().setRevenueShare(BigDecimal.valueOf(1));
        pm.setDefaultRevenueShare(new PORevenueShare());
        pm.getDefaultRevenueShare().setRevenueShare(BigDecimal.valueOf(2));
        doReturn(new Response(pm)).when(pricingService)
                .getOperatorRevenueShare(anyLong());

        // when
        ctrl.intializeRevenueShares(101L);

        // then
        assertNotNull(ctrl.getModel().getOperatorRevenueShare());
        assertEquals(BigDecimal.valueOf(1), ctrl.getModel()
                .getOperatorRevenueShare().getRevenueShare());
        assertNotNull(ctrl.getModel().getDefaultOperatorRevenueShare());
        assertEquals(BigDecimal.valueOf(2), ctrl.getModel()
                .getDefaultOperatorRevenueShare().getRevenueShare());
    }

    @Test
    public void intializeRevenueShares_ObjectNotFoundException()
            throws Exception {
        // given
        ctrl.getModel().setSelectedTemplateKey(101L);
        doThrow(new ObjectNotFoundException()).when(pricingService)
                .getOperatorRevenueShare(anyLong());

        // when
        ctrl.intializeRevenueShares(101L);

        // then
        assertEquals(0L, ctrl.getModel().getSelectedTemplateKey());
        verify(ctrl, times(1)).resetModel();
        assertTrue(ctrl.ui.hasErrors());
    }

    @Test
    public void resetModel() {
        // given
        ctrl.getModel().setOperatorRevenueShare(new PORevenueShare());
        ctrl.getModel().setDefaultOperatorRevenueShare(new PORevenueShare());

        // when
        ctrl.resetModel();

        // then
        assertNull(ctrl.getModel().getOperatorRevenueShare());
        assertNull(ctrl.getModel().getDefaultOperatorRevenueShare());
        assertTrue(ui.isResetDirtyCalled());
    }

    @Test
    public void save() throws Exception {
        // given
        ctrl.getModel().setSelectedTemplateKey(101L);
        PORevenueShare po = new PORevenueShare();
        ctrl.getModel().setOperatorRevenueShare(po);
        doReturn(new Response()).when(pricingService).saveOperatorRevenueShare(
                eq(101L), eq(po));

        // when
        ctrl.save();

        // then
        assertFalse(ui.hasErrors());
        verify(pricingService, times(1)).saveOperatorRevenueShare(eq(101L),
                eq(po));
    }

    @Test
    public void save_ObjectNotFoundException() throws Exception {
        // given
        ctrl.getModel().setSelectedTemplateKey(101L);
        doThrow(new ObjectNotFoundException()).when(pricingService)
                .saveOperatorRevenueShare(eq(101L), any(PORevenueShare.class));

        // when
        ctrl.save();

        // then
        assertEquals(0L, ctrl.getModel().getSelectedTemplateKey());
        assertTrue(ui.hasErrors());
    }

    @Test
    public void save_ValidationException() throws Exception {
        // given
        ctrl.getModel().setSelectedTemplateKey(101L);
        doThrow(new ValidationException()).when(pricingService)
                .saveOperatorRevenueShare(eq(101L), any(PORevenueShare.class));

        // when
        ctrl.save();

        // then
        assertEquals(101L, ctrl.getModel().getSelectedTemplateKey());
        assertTrue(ui.hasErrors());
    }

    @Test
    public void save_ServiceOperationException() throws Exception {
        // given
        ctrl.getModel().setSelectedTemplateKey(101L);
        doThrow(new ServiceOperationException()).when(pricingService)
                .saveOperatorRevenueShare(eq(101L), any(PORevenueShare.class));

        // when
        ctrl.save();

        // then
        assertEquals(101L, ctrl.getModel().getSelectedTemplateKey());
        assertTrue(ui.hasErrors());
    }

    @Test
    public void save_ConcurrentModificationException() throws Exception {
        // given
        ctrl.getModel().setSelectedTemplateKey(101L);
        doThrow(new ConcurrentModificationException()).when(pricingService)
                .saveOperatorRevenueShare(eq(101L), any(PORevenueShare.class));

        // when
        ctrl.save();

        // then
        assertEquals(101L, ctrl.getModel().getSelectedTemplateKey());
        assertTrue(ui.hasErrors());
    }

    @Test
    public void initTemplateServiceSelector() {
        // given
        POService service = new POService();
        service.setKey(101L);
        service.setServiceId("serviceId");
        service.setVendorOrganizationId("oId");
        services.add(service);

        // when
        ctrl.initTemplateServiceSelector();

        // then
        List<SelectItem> items = ctrl.getModel().getTemplates();
        assertEquals(1, items.size());
        assertEquals(Long.valueOf(101L), items.get(0).getValue());
        assertEquals("serviceId(oId)", items.get(0).getLabel());
    }

    @Test
    public void templateChanged() {
        // given
        ValueChangeEvent event = new ValueChangeEvent(mock(UIComponent.class),
                Long.valueOf(0L), Long.valueOf(101L));
        ctrl.setModel(spy(new ManageOperatorRevenueShareModel()));

        // when
        ctrl.templateChanged(event);

        // then
        assertEquals(101L, ctrl.getModel().getSelectedTemplateKey());
        verify(ctrl.getModel(), times(1)).setSelectedTemplateKey(eq(101L));
    }

    @Test
    public void templateChanged_toSameValue() {
        // given
        ValueChangeEvent event = new ValueChangeEvent(mock(UIComponent.class),
                Long.valueOf(101L), Long.valueOf(101L));
        ManageOperatorRevenueShareModel model = new ManageOperatorRevenueShareModel();
        model.setSelectedTemplateKey(101L);
        ctrl.setModel(spy(model));

        // when
        ctrl.templateChanged(event);

        // then
        assertEquals(101L, ctrl.getModel().getSelectedTemplateKey());
        verify(ctrl.getModel(), times(0)).setSelectedTemplateKey(eq(101L));
    }

    @Test
    public void setModel() {
        // given
        ManageOperatorRevenueShareModel model = new ManageOperatorRevenueShareModel();

        // when
        ctrl.setModel(model);

        // then
        assertEquals(model, ctrl.getModel());
    }

}
