/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Sep 04, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.beans;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Collections;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.junit.Before;
import org.junit.Test;

import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.vo.VOUserDetails;

public class GotoMarketplaceBeanPartnerTest {

    private GotoMarketplaceBean gotoBean;
    private HttpSession sessionMock;

    @Before
    public void setup() {
        gotoBean = spy(new GotoMarketplaceBean());

        FacesContext fcContextMock = mock(FacesContext.class);
        when(gotoBean.getFacesContext()).thenReturn(fcContextMock);

        // mock the ExternalContext's getRequest() method
        ExternalContext externalContextMock = mock(ExternalContext.class);
        when(fcContextMock.getExternalContext())
                .thenReturn(externalContextMock);

        HttpServletRequest httpServletRequestMock = mock(HttpServletRequest.class);
        when(externalContextMock.getRequest()).thenReturn(
                httpServletRequestMock);

        // mock the HttpServletRequest's getSession() method
        sessionMock = mock(HttpSession.class);
        when(httpServletRequestMock.getSession()).thenReturn(sessionMock);

    }

    @Test
    public void isLoggedInAndVendorManager_AsBroker() {
        // given a broker manager user
        when(sessionMock.getAttribute(anyString())).thenReturn(
                givenManagerUser(UserRoleType.BROKER_MANAGER));

        // then assert that the user is logged-in as service manager or partner
        assertTrue(gotoBean.isLoggedInAndVendorManager());

    }

    @Test
    public void isLoggedInAndVendorManager_Reseller() {
        // given a reseller manager user
        when(sessionMock.getAttribute(anyString())).thenReturn(
                givenManagerUser(UserRoleType.RESELLER_MANAGER));

        // then assert that the user is logged-in as service manager or partner
        assertTrue(gotoBean.isLoggedInAndVendorManager());

    }

    @Test
    public void isLoggedInAndVendorManager_AsServiceManager() {
        // given a service manager user
        when(sessionMock.getAttribute(anyString())).thenReturn(
                givenManagerUser(UserRoleType.SERVICE_MANAGER));

        // then assert that the user is logged-in as service manager or partner
        assertTrue(gotoBean.isLoggedInAndVendorManager());

    }

    @Test
    public void isLoggedInAndVendorManager_AsNonServiceManager() {
        // given a non service manager user
        when(sessionMock.getAttribute(anyString())).thenReturn(
                givenManagerUser(UserRoleType.TECHNOLOGY_MANAGER));

        // then assert that the user is not logged-in as service manager
        // or partner
        assertFalse(gotoBean.isLoggedInAndVendorManager());

    }

    @Test
    public void isLoggedInAndVendorManager_AsUserWithNoRoles() {
        // given a user who does not have any roles assigned
        when(sessionMock.getAttribute(anyString())).thenReturn(
                new VOUserDetails());

        // then assert that the user is not logged-in as service manager
        // or partner
        assertFalse(gotoBean.isLoggedInAndVendorManager());

    }

    VOUserDetails givenManagerUser(UserRoleType roleType) {
        VOUserDetails userDetails = new VOUserDetails();
        userDetails.setUserRoles(Collections.singleton(roleType));
        return userDetails;

    }
}
