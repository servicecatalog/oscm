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
import org.oscm.internal.intf.SubscriptionService;

/**
 * @author tokoda
 * 
 */
public class SubscriptionServiceWSTest {

    private SubscriptionServiceWS serviceWS;
    private SubscriptionService serviceMock;
    private HttpServletRequest requestMock;

    @Before
    public void setup() {
        serviceMock = mock(SubscriptionService.class);
        serviceWS = new SubscriptionServiceWS();
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
    public void testLogInjectionForSubscriptionServiceWS() throws Exception {

        serviceWS.abortAsyncSubscription(null, null, null);
        serviceWS.addRevokeUser(null, null, null);
        serviceWS.completeAsyncSubscription(null, null, null);
        serviceWS.getSubscriptionsForCurrentUser();
        serviceWS.getSubscriptionsForUser(null);
        serviceWS.getCustomersForSubscriptionId(null);
        serviceWS.getCustomerSubscriptions();
        serviceWS.getServiceRolesForSubscription(null);
        serviceWS.getSubscriptionDetails(null);
        serviceWS.getSubscriptionForCustomer(null, null);
        serviceWS.getSubscriptionIdentifiers();
        serviceWS.getSubscriptionsForOrganization();
        serviceWS.getUpgradeOptions(null);
        serviceWS.modifySubscription(null, null, null);
        serviceWS.subscribeToService(null, null, null, null, null, null);
        serviceWS.unsubscribeFromService(null);
        serviceWS.upgradeSubscription(null, null, null, null, null);
        serviceWS.getServiceRolesForService(null);
        serviceWS.updateAsyncSubscriptionProgress(null, null, null);
        serviceWS.executeServiceOperation(null, null);
        serviceWS.terminateSubscription(null, null);
        serviceWS.hasCurrentUserSubscriptions();
        serviceWS.modifySubscriptionPaymentData(null, null, null);
        serviceWS.getSubscriptionsForOrganizationWithFilter(null);
        serviceWS.reportIssue(null, null, null);
        serviceWS.getServiceOperationParameterValues(null, null);
        serviceWS.updateAsyncOperationProgress(null, null, null);
        serviceWS.updateAsyncSubscriptionStatus(null, null, null);

        verify(requestMock, times(28)).getRemoteAddr();

        verify(serviceMock, times(1)).abortAsyncSubscription(null, null, null);
        verify(serviceMock, times(1)).addRevokeUser(null, null, null);
        verify(serviceMock, times(1)).completeAsyncSubscription(null, null,
                null);
        verify(serviceMock, times(1)).getSubscriptionsForCurrentUser();
        verify(serviceMock, times(1)).getSubscriptionsForUser(null);
        verify(serviceMock, times(1)).getCustomersForSubscriptionId(null);
        verify(serviceMock, times(1)).getCustomerSubscriptions();
        verify(serviceMock, times(1)).getServiceRolesForSubscription(null);
        verify(serviceMock, times(1)).getSubscriptionDetails(null);
        verify(serviceMock, times(1)).getSubscriptionForCustomer(null, null);
        verify(serviceMock, times(1)).getSubscriptionIdentifiers();
        verify(serviceMock, times(1)).getSubscriptionsForOrganization();
        verify(serviceMock, times(1)).getUpgradeOptions(null);
        verify(serviceMock, times(1)).modifySubscription(null, null, null);
        verify(serviceMock, times(1)).subscribeToService(null, null, null,
                null, null, null);
        verify(serviceMock, times(1)).unsubscribeFromService((String) null);
        verify(serviceMock, times(1)).upgradeSubscription(null, null, null,
                null, null);
        verify(serviceMock, times(1)).getServiceRolesForService(null);
        verify(serviceMock, times(1)).updateAsyncSubscriptionProgress(null,
                null, null);
        verify(serviceMock, times(1)).executeServiceOperation(null, null);
        verify(serviceMock, times(1)).terminateSubscription(null, null);
        verify(serviceMock, times(1)).hasCurrentUserSubscriptions();
        verify(serviceMock, times(1)).modifySubscriptionPaymentData(null, null,
                null);
        verify(serviceMock, times(1))
                .getSubscriptionsForOrganizationWithFilter(null);
        verify(serviceMock, times(1)).reportIssue(null, null, null);
        verify(serviceMock, times(1)).getServiceOperationParameterValues(null,
                null);
        verify(serviceMock, times(1)).updateAsyncOperationProgress(null, null,
                null);
    }

}
