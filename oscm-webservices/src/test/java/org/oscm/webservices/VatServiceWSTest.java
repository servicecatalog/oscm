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
import org.oscm.internal.intf.VatService;

/**
 * @author tokoda
 * 
 */
public class VatServiceWSTest {

    private VatServiceWS serviceWS;
    private VatService serviceMock;
    private HttpServletRequest requestMock;

    @Before
    public void setup() {
        serviceMock = mock(VatService.class);
        serviceWS = new VatServiceWS();
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
    public void testLogInjectionForVatServiceWS() throws Exception {

        serviceWS.getCountryVats();
        serviceWS.getDefaultVat();
        serviceWS.getOrganizationVats();
        serviceWS.getVatSupport();
        serviceWS.saveAllVats(null, null, null);
        serviceWS.saveCountryVats(null);
        serviceWS.saveDefaultVat(null);
        serviceWS.saveOrganizationVats(null);

        verify(requestMock, times(8)).getRemoteAddr();

        verify(serviceMock, times(1)).getCountryVats();
        verify(serviceMock, times(1)).getDefaultVat();
        verify(serviceMock, times(1)).getOrganizationVats();
        verify(serviceMock, times(1)).getVatSupport();
        verify(serviceMock, times(1)).saveAllVats(null, null, null);
        verify(serviceMock, times(1)).saveCountryVats(null);
        verify(serviceMock, times(1)).saveDefaultVat(null);
        verify(serviceMock, times(1)).saveOrganizationVats(null);
    }

}
