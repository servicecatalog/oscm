/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: tokoda                                                     
 *                                                                              
 *  Creation Date: Sep 9, 2011                                                      
 *                                                                              
 *  Completion Time: Sep 9, 2011                                                 
 *                                                                              
 *******************************************************************************/

package org.oscm.webservices;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.PlatformUser;
import org.oscm.internal.intf.ServiceProvisioningService;
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOTechnicalService;

/**
 * @author tokoda
 * 
 */
public class ServiceProvisioningServiceWSTest {

    private ServiceProvisioningServiceWS serviceWS;
    private ServiceProvisioningService serviceMock;
    private HttpServletRequest requestMock;

    @Before
    public void setup() {
        serviceMock = mock(ServiceProvisioningService.class);
        serviceWS = new ServiceProvisioningServiceWS();
        serviceWS.wsContext = createWebServiceContextMock("127.0.0.1", "99999");
        serviceWS.delegate = serviceMock;
        PlatformUser user = mock(PlatformUser.class);
        DataService ds = mock(DataService.class);
        Mockito.when(ds.getCurrentUser()).thenReturn(user);
        serviceWS.ds = ds;
    }

    private WebServiceContext createWebServiceContextMock(String expectedIP,
            String expectedUser) {
        requestMock = mock(HttpServletRequest.class);
        when(requestMock.getRemoteAddr()).thenReturn(expectedIP);

        Principal principalMock = mock(Principal.class);
        when(principalMock.getName()).thenReturn(expectedUser);

        MessageContext msgContextMock = mock(MessageContext.class);
        when(msgContextMock.get(anyString())).thenReturn(requestMock);

        WebServiceContext wsContextMock = mock(WebServiceContext.class);
        when(wsContextMock.getUserPrincipal()).thenReturn(principalMock);
        when(wsContextMock.getMessageContext()).thenReturn(msgContextMock);

        return wsContextMock;
    }

    @Test
    public void testLogInjectionForServiceProvisioningServiceWS()
            throws Exception {

        serviceWS.activateService(null);
        serviceWS.createService(null, null, null);
        serviceWS.createTechnicalService(null);
        serviceWS.deactivateService(null);
        serviceWS.deleteService(null);
        serviceWS.deleteTechnicalService(null);
        serviceWS.exportTechnicalServices(null);
        serviceWS.getAllCustomerSpecificServices();
        serviceWS.getCompatibleServices(null);
        serviceWS.getServicesForCustomer(null);
        serviceWS.getPriceModelLocalization(null);
        serviceWS.getServiceDetails(null);
        serviceWS.getServiceForCustomer(null, null);
        serviceWS.getServiceLocalization(null);
        serviceWS.getServiceForSubscription(null, null);
        serviceWS.getSupportedCurrencies();
        serviceWS.getTechnicalServices(null);
        serviceWS.importTechnicalServices(null);
        serviceWS.loadImage(null);
        serviceWS.savePriceModel(null, null);
        serviceWS.savePriceModelForCustomer(null, null, null);
        serviceWS.savePriceModelForSubscription(null, null);
        serviceWS.savePriceModelLocalization(null, null);
        serviceWS.saveServiceLocalization(null, null);
        serviceWS.saveTechnicalServiceLocalization(null);
        serviceWS.setCompatibleServices(null, null);
        serviceWS.updateService(null, null);
        serviceWS.validateTechnicalServiceCommunication(null);
        serviceWS.copyService(null, null);
        serviceWS.getSuppliedServices();
        serviceWS.getServicesForMarketplace(null);
        serviceWS.loadImageForSupplier(null, null);
        serviceWS.getPriceModelLicenseTemplateLocalization(null);
        serviceWS.setActivationStates(null);
        serviceWS.getRelatedServicesForMarketplace(null, null, null);
        serviceWS.getServiceForMarketplace(null, null, null);
        serviceWS.getServiceSeller(0, null);
        serviceWS.getInstanceIdsForSellers(null);
        serviceWS.suspendService(null, null);
        serviceWS.resumeService(null);
        serviceWS.statusAllowsDeletion(null);
        serviceWS.isPartOfUpgradePath(null);
        serviceWS.getPotentialCompatibleServices(null);
        verify(requestMock, times(43)).getRemoteAddr();

        verify(serviceMock, times(1)).activateService(null);
        verify(serviceMock, times(1)).createService(null, null, null);
        verify(serviceMock, times(1)).createTechnicalService(null);
        verify(serviceMock, times(1)).deactivateService(null);
        verify(serviceMock, times(1)).deleteService((VOService) null);
        verify(serviceMock, times(1)).deleteTechnicalService(
                (VOTechnicalService) null);
        verify(serviceMock, times(1)).exportTechnicalServices(null);
        verify(serviceMock, times(1)).getAllCustomerSpecificServices();
        verify(serviceMock, times(1)).getCompatibleServices(null);
        verify(serviceMock, times(1)).getServicesForCustomer(null);
        verify(serviceMock, times(1)).getPriceModelLocalization(null);
        verify(serviceMock, times(1)).getServiceDetails(null);
        verify(serviceMock, times(1)).getServiceForCustomer(null, null);
        verify(serviceMock, times(1)).getServiceLocalization(null);
        verify(serviceMock, times(1)).getServiceForSubscription(null, null);
        verify(serviceMock, times(1)).getSupportedCurrencies();
        verify(serviceMock, times(1)).getTechnicalServices(null);
        verify(serviceMock, times(1)).importTechnicalServices(null);
        verify(serviceMock, times(1)).loadImage(null);
        verify(serviceMock, times(1)).savePriceModel(null, null);
        verify(serviceMock, times(1)).savePriceModelForCustomer(null, null,
                null);
        verify(serviceMock, times(1)).savePriceModelForSubscription(null, null);
        verify(serviceMock, times(1)).savePriceModelLocalization(null, null);
        verify(serviceMock, times(1)).saveServiceLocalization(null, null);
        verify(serviceMock, times(1)).saveTechnicalServiceLocalization(null);
        verify(serviceMock, times(1)).setCompatibleServices(null, null);
        verify(serviceMock, times(1)).updateService(null, null);
        verify(serviceMock, times(1)).validateTechnicalServiceCommunication(
                null);
        verify(serviceMock, times(1)).copyService(null, null);
        verify(serviceMock, times(1)).getSuppliedServices();
        verify(serviceMock, times(1)).getServicesForMarketplace(null);
        verify(serviceMock, times(1)).loadImageForSupplier(null, null);
        verify(serviceMock, times(1)).getPriceModelLicenseTemplateLocalization(
                null);
        verify(serviceMock, times(1)).setActivationStates(null);
        verify(serviceMock, times(1)).getRelatedServicesForMarketplace(null,
                null, null);
        verify(serviceMock, times(1))
                .getServiceForMarketplace(null, null, null);
        verify(serviceMock, times(1)).getServiceSeller(0, null);
        verify(serviceMock, times(1)).getInstanceIdsForSellers(null);
        verify(serviceMock, times(1)).suspendService(null, null);
        verify(serviceMock, times(1)).resumeService(null);
        verify(serviceMock, times(1)).statusAllowsDeletion(null);
        verify(serviceMock, times(1)).isPartOfUpgradePath(null);
        verify(serviceMock, times(1)).getPotentialCompatibleServices(null);
    }

    @Test
    public void logInjectionForServiceProvisioningServiceWS() throws Exception {

        serviceWS.createService(null, null, null);

        serviceWS.updateService(null, null);

        verify(requestMock, times(2)).getRemoteAddr();

        verify(serviceMock, times(1)).createService(null, null, null);

        verify(serviceMock, times(1)).updateService(null, null);

        verify(serviceMock, never()).getServiceDetails(null);
    }
}
