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
import org.oscm.internal.intf.MarketplaceService;

/**
 * @author tokoda
 * 
 */
public class MarketplaceServiceWSTest {

    private MarketplaceServiceWS serviceWS;
    private MarketplaceService serviceMock;
    private HttpServletRequest requestMock;

    @Before
    public void setup() {
        serviceMock = mock(MarketplaceService.class);
        serviceWS = new MarketplaceServiceWS();
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
    public void testLogInjectionForMarketplaceServiceWS() throws Exception {

        serviceWS.getMarketplacesForOrganization();
        serviceWS.getMarketplacesForService(null);
        serviceWS.publishService(null, null);
        serviceWS.getMarketplaceForSubscription(0, null);
        serviceWS.getMarketplacesOwned();
        serviceWS.getMarketplacesForOperator();
        serviceWS.updateMarketplace(null);
        serviceWS.createMarketplace(null);
        serviceWS.deleteMarketplace(null);
        serviceWS.addOrganizationsToMarketplace(null, null);
        serviceWS.banOrganizationsFromMarketplace(null, null);
        serviceWS.removeOrganizationsFromMarketplace(null, null);
        serviceWS.liftBanOrganizationsFromMarketplace(null, null);
        serviceWS.getMarketplaceById(null);
        serviceWS.getOrganizationsForMarketplace(null);
        serviceWS.getBrandingUrl(null);
        serviceWS.saveBrandingUrl(null, null);
        serviceWS.getAccessibleMarketplaces();

        verify(requestMock, times(18)).getRemoteAddr();

        verify(serviceMock, times(1)).getAccessibleMarketplaces();
        verify(serviceMock, times(1)).getMarketplacesForOrganization();
        verify(serviceMock, times(1)).getMarketplacesForService(null);
        verify(serviceMock, times(1)).publishService(null, null);
        verify(serviceMock, times(1)).getMarketplaceForSubscription(0, null);
        verify(serviceMock, times(1)).getMarketplacesOwned();
        verify(serviceMock, times(1)).getMarketplacesForOperator();
        verify(serviceMock, times(1)).updateMarketplace(null);
        verify(serviceMock, times(1)).createMarketplace(null);
        verify(serviceMock, times(1)).deleteMarketplace(null);
        verify(serviceMock, times(1)).addOrganizationsToMarketplace(null, null);
        verify(serviceMock, times(1)).banOrganizationsFromMarketplace(null,
                null);
        verify(serviceMock, times(1)).removeOrganizationsFromMarketplace(null,
                null);
        verify(serviceMock, times(1)).liftBanOrganizationsFromMarketplace(null,
                null);
        verify(serviceMock, times(1)).getMarketplaceById(null);
        verify(serviceMock, times(1)).getOrganizationsForMarketplace(null);
        verify(serviceMock, times(1)).getBrandingUrl(null);
        verify(serviceMock, times(1)).saveBrandingUrl(null, null);
    }

}
