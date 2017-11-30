/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Aug 7, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.classic.managerevenueshare;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.faces.event.ValueChangeEvent;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import org.oscm.ui.beans.SessionBean;
import org.oscm.ui.common.UiDelegate;
import org.oscm.internal.components.response.Response;
import org.oscm.internal.pricing.POMarketplacePricing;
import org.oscm.internal.pricing.POOrganization;
import org.oscm.internal.pricing.POPartnerPriceModel;
import org.oscm.internal.pricing.PORevenueShare;
import org.oscm.internal.pricing.POServiceForPricing;
import org.oscm.internal.pricing.POServicePricing;
import org.oscm.internal.pricing.PricingService;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.ServiceOperationException;
import org.oscm.internal.types.exception.ServiceStateException;
import org.oscm.internal.types.exception.ValidationException;

/**
 * @author tokoda
 * 
 */
public class ManageRevenueShareCtrlTest {

    private static final long TEMPLATE_SERVICE_KEY = 11111L;
    private static final String TEMPLATE_SERVICE_ID = "service1";
    private static final String VENDER_ID = "vender";

    private static final long BROKER_PARTNER_SERVICE_KEY = 22222L;
    private static final long BROKER_REVENUESHARE_KEY = 33333L;
    private static final BigDecimal BROKER_REVENUESHARE_VALUE = BigDecimal.ONE;

    private static final long RESELLER_PARTNER_SERVICE_KEY = 44444L;
    private static final long RESELLER_REVENUESHARE_KEY = 55555L;
    private static final BigDecimal RESELLER_REVENUESHARE_VALUE = BigDecimal.TEN;

    ManageRevenueShareCtrl ctrl = new ManageRevenueShareCtrl();;
    private SessionBean sessionBean = new SessionBean();

    private List<POServicePricing> storedValue;

    @Before
    public void setup() {
        UiDelegate ui = mock(UiDelegate.class);
        ctrl.model = new ManageRevenueShareModel();
        when(ui.findBean("sessionBean")).thenReturn(sessionBean);
        ctrl.ui = ui;

        ctrl.pricingService = mock(PricingService.class);
    }

    @Test
    public void getInitializePage_TemplateServiceNotExists() {
        // given
        mockGetTemplateServicesWithNoTemplateService();

        // when
        ctrl.getInitializePage();

        // then
        ManageRevenueShareModel model = ctrl.getModel();
        assertNotNull(model);
        assertEquals(0, model.getTemplates().size());
        assertEquals(0, model.getSelectedTemplateKey());
    }

    @Test
    public void getInitializePage_TemplateServiceNotSelected() {
        // given
        spyIntializePricingForSelectedTemplate();
        mockGetTemplateServicesWithTemplateServices();

        // when
        ctrl.getInitializePage();

        // then
        ManageRevenueShareModel model = ctrl.getModel();
        assertNotNull(model);
        assertEquals(0, model.getSelectedTemplateKey());
        assertEquals(1, model.getTemplates().size());
        verify(ctrl, never()).intializePricingForSelectedTemplate(anyLong());
    }

    @Test
    public void getInitializePage_TemplateServiceAlreadySelected() {
        // given
        spyIntializePricingForSelectedTemplate();
        mockGetTemplateServicesWithTemplateServices();

        ManageRevenueShareModel model = new ManageRevenueShareModel();
        model.setSelectedTemplateKey(TEMPLATE_SERVICE_KEY);
        ctrl.setModel(model);

        // when
        ctrl.getInitializePage();

        // then
        assertNotNull(model);
        assertEquals(TEMPLATE_SERVICE_KEY, model.getSelectedTemplateKey());
        assertEquals(1, model.getTemplates().size());
        verify(ctrl, times(1)).intializePricingForSelectedTemplate(
                TEMPLATE_SERVICE_KEY);
    }

    @Test
    public void intializePricingForSelectedTemplate_ServiceOperationException()
            throws Exception {
        // given
        ServiceOperationException sex = new ServiceOperationException();
        when(
                ctrl.pricingService
                        .getMarketplacePricingForService(any(POServiceForPricing.class)))
                .thenThrow(sex);

        ManageRevenueShareModel model = new ManageRevenueShareModel();
        model.setSelectedTemplateKey(TEMPLATE_SERVICE_KEY);
        ctrl.setModel(model);

        // when
        ctrl.intializePricingForSelectedTemplate(0);

        // then
        assertEquals(TEMPLATE_SERVICE_KEY, model.getSelectedTemplateKey());
        verify(ctrl.ui, times(1)).handleException(sex);
    }

    @Test
    public void intializePricingForSelectedTemplate_ObjectNotFoundException()
            throws Exception {
        // given
        ObjectNotFoundException oex = new ObjectNotFoundException();
        when(
                ctrl.pricingService
                        .getMarketplacePricingForService(any(POServiceForPricing.class)))
                .thenThrow(oex);

        ManageRevenueShareModel model = new ManageRevenueShareModel();
        model.setSelectedTemplateKey(TEMPLATE_SERVICE_KEY);
        ctrl.setModel(model);

        // when
        ctrl.intializePricingForSelectedTemplate(0);

        // then
        assertEquals(0, model.getSelectedTemplateKey());
        verify(ctrl.ui, times(1)).handleException(oex);
    }

    @Test
    public void intializePricingForSelectedTemplate_ServiceStateException()
            throws Exception {
        // given
        ServiceStateException sex = new ServiceStateException();
        when(
                ctrl.pricingService
                        .getMarketplacePricingForService(any(POServiceForPricing.class)))
                .thenThrow(sex);

        ManageRevenueShareModel model = new ManageRevenueShareModel();
        model.setSelectedTemplateKey(TEMPLATE_SERVICE_KEY);
        ctrl.setModel(model);

        // when
        ctrl.intializePricingForSelectedTemplate(0);

        // then
        assertEquals(0, model.getSelectedTemplateKey());
        verify(ctrl.ui, times(1)).handleException(sex);
    }

    @Test
    public void intializePricingForSelectedTemplate_NoPartnerServices()
            throws Exception {
        // given
        mockGetMarketplacePricingForService();
        mockGetPartnerRevenueShareForService();
        mockGetPartnerServicesWithRevenueShareForTemplate(new ArrayList<POServicePricing>());

        ManageRevenueShareModel model = new ManageRevenueShareModel();
        ctrl.setModel(model);

        // when
        ctrl.intializePricingForSelectedTemplate(TEMPLATE_SERVICE_KEY);

        // then
        assertNotNull(model.getPricingOfMarketplaceForSelectedTemplate());
        assertNotNull(model.getPartnerPriceModelForSelectedTemplate());
        assertEquals(0, model.getBrokerServicePricings().size());
        assertEquals(0, model.getResellerServicePricings().size());
    }

    @Test
    public void intializePricingForSelectedTemplate_WithBrokerService()
            throws Exception {
        // given
        mockGetMarketplacePricingForService();
        mockGetPartnerRevenueShareForService();
        mockGetPartnerServicesWithRevenueShareForTemplate(createPartnerServicePricings(
                true, false));

        ManageRevenueShareModel model = new ManageRevenueShareModel();
        ctrl.setModel(model);

        // when
        ctrl.intializePricingForSelectedTemplate(TEMPLATE_SERVICE_KEY);

        // then
        assertNotNull(model.getPricingOfMarketplaceForSelectedTemplate());
        assertNotNull(model.getPartnerPriceModelForSelectedTemplate());
        assertEquals(1, model.getBrokerServicePricings().size());
        assertEquals(0, model.getResellerServicePricings().size());
    }

    @Test
    public void intializePricingForSelectedTemplate_WithResellerService()
            throws Exception {
        // given
        mockGetMarketplacePricingForService();
        mockGetPartnerRevenueShareForService();
        mockGetPartnerServicesWithRevenueShareForTemplate(createPartnerServicePricings(
                false, true));

        ManageRevenueShareModel model = new ManageRevenueShareModel();
        ctrl.setModel(model);

        // when
        ctrl.intializePricingForSelectedTemplate(TEMPLATE_SERVICE_KEY);

        // then
        assertNotNull(model.getPricingOfMarketplaceForSelectedTemplate());
        assertNotNull(model.getPartnerPriceModelForSelectedTemplate());
        assertEquals(0, model.getBrokerServicePricings().size());
        assertEquals(1, model.getResellerServicePricings().size());
    }

    @Test
    public void intializePricingForSelectedTemplate_WithPartnerServices()
            throws Exception {
        // given
        mockGetMarketplacePricingForService();
        mockGetPartnerRevenueShareForService();
        mockGetPartnerServicesWithRevenueShareForTemplate(createPartnerServicePricings(
                true, true));

        ManageRevenueShareModel model = new ManageRevenueShareModel();
        ctrl.setModel(model);

        // when
        ctrl.intializePricingForSelectedTemplate(TEMPLATE_SERVICE_KEY);

        // then
        assertNotNull(model.getPricingOfMarketplaceForSelectedTemplate());
        assertNotNull(model.getPartnerPriceModelForSelectedTemplate());
        assertEquals(1, model.getBrokerServicePricings().size());
        assertEquals(1, model.getResellerServicePricings().size());
    }

    @Test
    public void templateChanged_DifferentKey() {
        // given
        ValueChangeEvent eventMock = getMockValueChangeEventForTemplate(2);

        ManageRevenueShareModel model = new ManageRevenueShareModel();
        model.setSelectedTemplateKey(1);
        ctrl.setModel(model);

        // when
        ctrl.templateChanged(eventMock);

        // then
        assertEquals(2, model.getSelectedTemplateKey());
    }

    @Test
    public void templateChanged_SameKey() {
        // given
        ValueChangeEvent eventMock = getMockValueChangeEventForTemplate(1);

        ManageRevenueShareModel model = new ManageRevenueShareModel();
        model.setSelectedTemplateKey(1);
        ctrl.setModel(model);

        // when
        ctrl.templateChanged(eventMock);

        // then
        assertEquals(1, model.getSelectedTemplateKey());
    }

    @Test
    public void save_SaaSApplicationException() throws Exception {
        // give
        ValidationException ve = new ValidationException();
        when(
                ctrl.pricingService
                        .savePartnerRevenueSharesForServices(anyListOf(POServicePricing.class)))
                .thenThrow(ve);

        ManageRevenueShareModel model = new ManageRevenueShareModel();
        model.setSelectedTemplateKey(TEMPLATE_SERVICE_KEY);
        ctrl.setModel(model);

        // when
        String result = ctrl.save(new ArrayList<POServicePricing>());

        // then
        assertNull(result);
        assertEquals(TEMPLATE_SERVICE_KEY, model.getSelectedTemplateKey());
        verify(ctrl.ui, never()).handle(any(Response.class),
                eq(ManageRevenueShareCtrl.INFO_SAVED));
        verify(ctrl.ui, times(1)).handleException(ve);
    }

    @Test
    public void save_ConcurrentModificationException() throws Exception {
        // give
        ConcurrentModificationException cex = new ConcurrentModificationException();
        when(
                ctrl.pricingService
                        .savePartnerRevenueSharesForServices(anyListOf(POServicePricing.class)))
                .thenThrow(cex);

        ManageRevenueShareModel model = new ManageRevenueShareModel();
        model.setSelectedTemplateKey(TEMPLATE_SERVICE_KEY);
        ctrl.setModel(model);

        // when
        String result = ctrl.save(new ArrayList<POServicePricing>());

        // then
        assertEquals(ManageRevenueShareCtrl.CONCURRENT_MODIFICATION_ERROR,
                result);
        assertEquals(TEMPLATE_SERVICE_KEY, model.getSelectedTemplateKey());
        verify(ctrl.ui, never()).handle(any(Response.class),
                eq(ManageRevenueShareCtrl.INFO_SAVED));
        verify(ctrl.ui, times(1)).handleException(cex);
    }

    @Test
    public void save_ObjectNotFoundException() throws Exception {
        // give
        ObjectNotFoundException oex = new ObjectNotFoundException();
        when(
                ctrl.pricingService
                        .savePartnerRevenueSharesForServices(anyListOf(POServicePricing.class)))
                .thenThrow(oex);

        ManageRevenueShareModel model = new ManageRevenueShareModel();
        model.setSelectedTemplateKey(TEMPLATE_SERVICE_KEY);
        ctrl.setModel(model);

        // when
        String result = ctrl.save(new ArrayList<POServicePricing>());

        // then
        assertNull(result);
        assertEquals(0, model.getSelectedTemplateKey());
        verify(ctrl.ui, never()).handle(any(Response.class),
                eq(ManageRevenueShareCtrl.INFO_SAVED));
        verify(ctrl.ui, times(1)).handleException(oex);
    }

    @Test
    public void save_ServiceStateException() throws Exception {
        // give
        ServiceStateException sex = new ServiceStateException();
        when(
                ctrl.pricingService
                        .savePartnerRevenueSharesForServices(anyListOf(POServicePricing.class)))
                .thenThrow(sex);

        ManageRevenueShareModel model = new ManageRevenueShareModel();
        model.setSelectedTemplateKey(TEMPLATE_SERVICE_KEY);
        ctrl.setModel(model);

        // when
        String result = ctrl.save(new ArrayList<POServicePricing>());

        // then
        assertNull(result);
        assertEquals(0, model.getSelectedTemplateKey());
        verify(ctrl.ui, never()).handle(any(Response.class),
                eq(ManageRevenueShareCtrl.INFO_SAVED));
        verify(ctrl.ui, times(1)).handleException(sex);
    }

    @Test
    public void save_isNotPublished() throws Exception {
        // given
        mockSavePartnerRevenueSharesForServices();

        ManageRevenueShareModel model = new ManageRevenueShareModel();
        model.setSelectedTemplateKey(TEMPLATE_SERVICE_KEY);
        ctrl.setModel(model);

        List<POServicePricing> partnerServicePricings = createPartnerServicePricings(
                true, true);

        // when
        String result = ctrl.save(partnerServicePricings);

        assertNull(result);
        verify(ctrl.pricingService, times(1))
                .savePartnerRevenueSharesForServices(partnerServicePricings);
        verify(ctrl.ui, times(1)).handle(any(Response.class),
                eq(ManageRevenueShareCtrl.INFO_SAVED));
    }

    @Test
    public void saveForBroker() throws Exception {
        // given
        mockSavePartnerRevenueSharesForServices();

        POServiceForPricing poService = new POServiceForPricing();
        poService.setKey(TEMPLATE_SERVICE_KEY);
        POPartnerPriceModel poPartnerPriceModel = new POPartnerPriceModel();
        POServicePricing servicePricing = new POServicePricing();
        servicePricing.setServiceForPricing(poService);
        servicePricing.setPartnerPriceModel(poPartnerPriceModel);

        List<POServicePricing> brokerServicePricings = createPartnerServicePricings(
                true, false);

        ManageRevenueShareModel model = new ManageRevenueShareModel();
        model.setSelectedTemplateKey(TEMPLATE_SERVICE_KEY);
        model.setPricingOfMarketplaceForSelectedTemplate(new POMarketplacePricing());
        model.setPartnerPriceModelForSelectedTemplate(poPartnerPriceModel);
        model.setResellerServicePricings(brokerServicePricings);
        ctrl.setModel(model);

        // when
        String result = ctrl.saveForReseller();

        // then
        assertNull(result);
        verify(ctrl.pricingService, times(1))
                .savePartnerRevenueSharesForServices(
                        anyListOf(POServicePricing.class));
        assertEquals(2, storedValue.size());
        POServicePricing templateServiceArgument = storedValue.get(0);
        assertEquals(TEMPLATE_SERVICE_KEY, templateServiceArgument
                .getServiceForPricing().getKey());
        POServicePricing partnerServiceArgument = storedValue.get(1);
        assertEquals(BROKER_PARTNER_SERVICE_KEY, partnerServiceArgument
                .getServiceForPricing().getKey());
        verify(ctrl.ui, times(1)).handle(any(Response.class),
                eq(ManageRevenueShareCtrl.INFO_SAVED));
    }

    @Test
    public void saveForReseller() throws Exception {
        // given
        mockSavePartnerRevenueSharesForServices();

        POServiceForPricing poService = new POServiceForPricing();
        poService.setKey(TEMPLATE_SERVICE_KEY);
        POPartnerPriceModel poPartnerPriceModel = new POPartnerPriceModel();
        POServicePricing servicePricing = new POServicePricing();
        servicePricing.setServiceForPricing(poService);
        servicePricing.setPartnerPriceModel(poPartnerPriceModel);

        List<POServicePricing> resellerServicePricings = createPartnerServicePricings(
                false, true);

        ManageRevenueShareModel model = new ManageRevenueShareModel();
        model.setSelectedTemplateKey(TEMPLATE_SERVICE_KEY);
        model.setPricingOfMarketplaceForSelectedTemplate(new POMarketplacePricing());
        model.setPartnerPriceModelForSelectedTemplate(poPartnerPriceModel);
        model.setResellerServicePricings(resellerServicePricings);
        ctrl.setModel(model);

        // when
        String result = ctrl.saveForReseller();

        // then
        assertNull(result);
        verify(ctrl.pricingService, times(1))
                .savePartnerRevenueSharesForServices(
                        anyListOf(POServicePricing.class));
        assertEquals(2, storedValue.size());
        POServicePricing templateServiceArgument = storedValue.get(0);
        assertEquals(TEMPLATE_SERVICE_KEY, templateServiceArgument
                .getServiceForPricing().getKey());
        POServicePricing partnerServiceArgument = storedValue.get(1);
        assertEquals(RESELLER_PARTNER_SERVICE_KEY, partnerServiceArgument
                .getServiceForPricing().getKey());
        verify(ctrl.ui, times(1)).handle(any(Response.class),
                eq(ManageRevenueShareCtrl.INFO_SAVED));
    }

    private void mockGetTemplateServicesWithNoTemplateService() {
        Response response = mock(Response.class);
        when(response.getResultList(POServiceForPricing.class)).thenReturn(
                new ArrayList<POServiceForPricing>());
        when(ctrl.pricingService.getTemplateServices()).thenReturn(response);
    }

    private void mockGetTemplateServicesWithTemplateServices() {
        List<POServiceForPricing> templates = createTemplateServiceList();
        Response response = mock(Response.class);
        when(response.getResultList(POServiceForPricing.class)).thenReturn(
                templates);
        when(ctrl.pricingService.getTemplateServices()).thenReturn(response);
    }

    private List<POServiceForPricing> createTemplateServiceList() {
        POOrganization venderForTemplate = new POOrganization();
        venderForTemplate.setOrganizationId(VENDER_ID);

        POServiceForPricing templateService = new POServiceForPricing();
        templateService.setKey(TEMPLATE_SERVICE_KEY);
        templateService.setServiceId(TEMPLATE_SERVICE_ID);
        templateService.setVendor(venderForTemplate);

        List<POServiceForPricing> templates = new ArrayList<POServiceForPricing>();
        templates.add(templateService);
        return templates;
    }

    private void mockGetMarketplacePricingForService() throws Exception {
        POMarketplacePricing marketplacePricing = new POMarketplacePricing();

        Response response = mock(Response.class);
        when(response.getResult(POMarketplacePricing.class)).thenReturn(
                marketplacePricing);

        POServiceForPricing service = new POServiceForPricing();
        service.setKey(TEMPLATE_SERVICE_KEY);
        when(ctrl.pricingService.getMarketplacePricingForService(service))
                .thenReturn(response);
    }

    private void mockGetPartnerRevenueShareForService() throws Exception {
        POPartnerPriceModel partnerPriceModel = new POPartnerPriceModel();

        Response response = mock(Response.class);
        when(response.getResult(POPartnerPriceModel.class)).thenReturn(
                partnerPriceModel);

        POServiceForPricing service = new POServiceForPricing(
                TEMPLATE_SERVICE_KEY, 0);
        when(
                ctrl.pricingService
                        .getPartnerRevenueShareForAllStatesService(service))
                .thenReturn(response);
    }

    private void mockGetPartnerServicesWithRevenueShareForTemplate(
            List<POServicePricing> servicePricings) throws Exception {

        Response response = mock(Response.class);
        when(response.getResultList(POServicePricing.class)).thenReturn(
                servicePricings);

        POServiceForPricing service = new POServiceForPricing(
                TEMPLATE_SERVICE_KEY, 0);
        when(
                ctrl.pricingService
                        .getPartnerServicesWithRevenueShareForTemplate(service))
                .thenReturn(response);
    }

    private void mockSavePartnerRevenueSharesForServices() throws Exception {

        when(
                ctrl.pricingService
                        .savePartnerRevenueSharesForServices(anyListOf(POServicePricing.class)))
                .thenAnswer(new Answer<Response>() {

                    @SuppressWarnings("unchecked")
                    public Response answer(InvocationOnMock invocation) {
                        Object obj = invocation.getArguments()[0];
                        storedValue = (List<POServicePricing>) obj;
                        return new Response();
                    }
                });
    }

    private List<POServicePricing> createPartnerServicePricings(boolean broker,
            boolean reseller) {
        List<POServicePricing> partnerServicePricings = new ArrayList<POServicePricing>();
        if (broker) {
            POServiceForPricing brokerService = new POServiceForPricing();
            brokerService.setKey(BROKER_PARTNER_SERVICE_KEY);

            POPartnerPriceModel brokerPriceModel = new POPartnerPriceModel();
            PORevenueShare brokerRevenueShare = new PORevenueShare();
            brokerRevenueShare.setKey(BROKER_REVENUESHARE_KEY);
            brokerRevenueShare.setRevenueShare(BROKER_REVENUESHARE_VALUE);
            brokerPriceModel.setRevenueShareBrokerModel(brokerRevenueShare);

            POServicePricing brokerServicePricing = new POServicePricing();
            brokerServicePricing.setServiceForPricing(brokerService);
            brokerServicePricing.setPartnerPriceModel(brokerPriceModel);
            partnerServicePricings.add(brokerServicePricing);
        }

        if (reseller) {
            POServiceForPricing resellerService = new POServiceForPricing();
            resellerService.setKey(RESELLER_PARTNER_SERVICE_KEY);

            POPartnerPriceModel resellerPriceModel = new POPartnerPriceModel();
            PORevenueShare resellerRevenueShare = new PORevenueShare();
            resellerRevenueShare.setKey(RESELLER_REVENUESHARE_KEY);
            resellerRevenueShare.setRevenueShare(RESELLER_REVENUESHARE_VALUE);
            resellerPriceModel
                    .setRevenueShareResellerModel(resellerRevenueShare);

            POServicePricing resellerServicePricing = new POServicePricing();
            resellerServicePricing.setServiceForPricing(resellerService);
            resellerServicePricing.setPartnerPriceModel(resellerPriceModel);
            partnerServicePricings.add(resellerServicePricing);
        }

        return partnerServicePricings;
    }

    private ValueChangeEvent getMockValueChangeEventForTemplate(
            long selectedServiceKey) {
        ValueChangeEvent eventMock = mock(ValueChangeEvent.class);
        when(eventMock.getNewValue()).thenReturn(
                Long.valueOf(selectedServiceKey));
        return eventMock;
    }

    private void spyIntializePricingForSelectedTemplate() {
        ctrl = spy(ctrl);
        doNothing().when(ctrl).intializePricingForSelectedTemplate(anyLong());
    }
}
