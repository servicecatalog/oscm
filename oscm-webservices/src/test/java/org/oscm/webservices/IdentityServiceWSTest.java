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
import org.oscm.internal.intf.IdentityService;

/**
 * @author tokoda
 * 
 */
public class IdentityServiceWSTest {

    private IdentityServiceWS serviceWS;
    private IdentityService serviceMock;
    private HttpServletRequest requestMock;

    @Before
    public void setup() {
        serviceMock = mock(IdentityService.class);
        serviceWS = new IdentityServiceWS();
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
    public void testLogInjectionForIdentityServiceWS() throws Exception {

        serviceWS.createUser(null, null, null);
        serviceWS.changePassword(null, null);
        serviceWS.confirmAccount(null, null);
        serviceWS.grantUserRoles(null, null);
        serviceWS.deleteUser(null, null);
        serviceWS.revokeUserRoles(null, null);
        serviceWS.getCurrentUserDetails();
        serviceWS.getCurrentUserDetailsIfPresent();
        serviceWS.getUser(null);
        serviceWS.getUserDetails(null);
        serviceWS.getUsersForOrganization();
        serviceWS.importLdapUsers(null, null);
        serviceWS.lockUserAccount(null, null, null);
        serviceWS.updateUser(null);
        serviceWS.notifyOnLoginAttempt(null, true);
        serviceWS.requestResetOfUserPassword(null, null);
        serviceWS.searchLdapUsers(null);
        serviceWS.addRevokeUserUnitAssignment(null, null, null);
        serviceWS.addRevokeUserGroupAssignment(null, null, null);
        serviceWS.sendAccounts(null, null);
        serviceWS.unlockUserAccount(null, null);
        serviceWS.getAvailableUserRoles(null);
        serviceWS.setUserRoles(null, null);
        serviceWS.createOnBehalfUser(null, null);
        serviceWS.cleanUpCurrentUser();

        verify(requestMock, times(25)).getRemoteAddr();

        verify(serviceMock, times(1)).createUser(null, null, null);
        verify(serviceMock, times(1)).changePassword(null, null);
        verify(serviceMock, times(1)).confirmAccount(null, null);
        verify(serviceMock, times(1)).grantUserRoles(null, null);
        verify(serviceMock, times(1)).deleteUser(null, null);
        verify(serviceMock, times(1)).revokeUserRoles(null, null);
        verify(serviceMock, times(1)).getCurrentUserDetails();
        verify(serviceMock, times(1)).getCurrentUserDetailsIfPresent();
        verify(serviceMock, times(1)).getUser(null);
        verify(serviceMock, times(1)).getUserDetails(null);
        verify(serviceMock, times(1)).getUsersForOrganization();
        verify(serviceMock, times(1)).importLdapUsers(null, null);
        verify(serviceMock, times(1)).lockUserAccount(null, null, null);
        verify(serviceMock, times(1)).updateUser(null);
        verify(serviceMock, times(1)).notifyOnLoginAttempt(null, true);
        verify(serviceMock, times(1)).requestResetOfUserPassword(null, null);
        verify(serviceMock, times(1)).searchLdapUsers(null);
        verify(serviceMock, times(2)).addRevokeUserUnitAssignment(null, null,
                null);
        verify(serviceMock, times(1)).sendAccounts(null, null);
        verify(serviceMock, times(1)).unlockUserAccount(null, null);
        verify(serviceMock, times(1)).getAvailableUserRoles(null);
        verify(serviceMock, times(1)).setUserRoles(null, null);
        verify(serviceMock, times(1)).createOnBehalfUser(null, null);
        verify(serviceMock, times(1)).cleanUpCurrentUser();

    }

}
