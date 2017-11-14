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
import org.oscm.internal.intf.AccountService;

/**
 * @author tokoda
 */
public class AccountServiceWSTest {

    private AccountServiceWS serviceWS;
    private AccountService serviceMock;
    private HttpServletRequest requestMock;

    @Before
    public void setup() {
        serviceMock = mock(AccountService.class);
        serviceWS = new AccountServiceWS();
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
    public void testLogInjectionForAccountServiceWS() throws Exception {
        serviceWS.deregisterOrganization();
        serviceWS.getAvailablePaymentTypesForOrganization();
        serviceWS.getAvailablePaymentTypesFromOrganization(null);
        serviceWS.getBillingContacts();
        serviceWS.getCustomerPaymentConfiguration();
        serviceWS.getMyCustomers();
        serviceWS.getDefaultPaymentConfiguration();
        serviceWS.getOrganizationData();
        serviceWS.getOrganizationId(0);
        serviceWS.registerKnownCustomer(null, null, null, null);
        serviceWS.registerCustomer(null, null, null, null, null, null);
        serviceWS.saveBillingContact(null);
        serviceWS.deleteBillingContact(null);
        serviceWS.savePaymentConfiguration(null, null, null, null);
        serviceWS.savePaymentInfo(null);
        serviceWS.updateAccountInformation(null, null, null, null);
        serviceWS.updateCustomerDiscount(null);
        serviceWS.getUdaTargetTypes();
        serviceWS.getUdaDefinitions();
        serviceWS.getUdas(null, 0);
        serviceWS.saveUdaDefinitions(null, null);
        serviceWS.saveUdas(null);
        serviceWS.getSupportedCountryCodes();
        serviceWS.loadImageOfOrganization(0);
        serviceWS.getSeller(null, null);
        serviceWS.deletePaymentInfo(null);
        serviceWS.getPaymentInfos();
        serviceWS.getAvailablePaymentTypes();
        serviceWS.getDefaultServicePaymentConfiguration();
        serviceWS.getServicePaymentConfiguration();
        serviceWS.addSuppliersForTechnicalService(null, null);
        serviceWS.removeSuppliersFromTechnicalService(null, null);
        serviceWS.getSuppliersForTechnicalService(null);

        verify(requestMock, times(33)).getRemoteAddr();

        verify(serviceMock, times(1)).deregisterOrganization();
        verify(serviceMock, times(1)).getAvailablePaymentTypesForOrganization();
        verify(serviceMock, times(1)).getAvailablePaymentTypesFromOrganization(
                null);
        verify(serviceMock, times(1)).getBillingContacts();
        verify(serviceMock, times(1)).getCustomerPaymentConfiguration();
        verify(serviceMock, times(1)).getMyCustomers();
        verify(serviceMock, times(1)).getDefaultPaymentConfiguration();
        verify(serviceMock, times(1)).getOrganizationData();
        verify(serviceMock, times(1)).getOrganizationId(0);
        verify(serviceMock, times(1)).registerKnownCustomer(null, null, null,
                null);
        verify(serviceMock, times(1)).registerCustomer(null, null, null, null,
                null, null);
        verify(serviceMock, times(1)).saveBillingContact(null);
        verify(serviceMock, times(1)).deleteBillingContact(null);
        verify(serviceMock, times(1)).savePaymentConfiguration(null, null,
                null, null);
        verify(serviceMock, times(1)).savePaymentInfo(null);
        verify(serviceMock, times(1)).updateAccountInformation(null, null,
                null, null);
        verify(serviceMock, times(1)).updateCustomerDiscount(null);
        verify(serviceMock, times(1)).getUdaTargetTypes();
        verify(serviceMock, times(1)).getUdaDefinitions();
        verify(serviceMock, times(1)).getUdas(null, 0, true);
        verify(serviceMock, times(1)).saveUdaDefinitions(null, null);
        verify(serviceMock, times(1)).saveUdas(null);
        verify(serviceMock, times(1)).getSupportedCountryCodes();
        verify(serviceMock, times(1)).loadImageOfOrganization(0);
        verify(serviceMock, times(1)).getSeller(null, null);
        verify(serviceMock, times(1)).deletePaymentInfo(null);
        verify(serviceMock, times(1)).getPaymentInfos();
        verify(serviceMock, times(1)).getAvailablePaymentTypes();
        verify(serviceMock, times(1)).getDefaultServicePaymentConfiguration();
        verify(serviceMock, times(1)).getServicePaymentConfiguration();
        verify(serviceMock, times(1)).addSuppliersForTechnicalService(null,
                null);
        verify(serviceMock, times(1)).removeSuppliersFromTechnicalService(null,
                null);
        verify(serviceMock, times(1)).getSuppliersForTechnicalService(null);
    }

}
