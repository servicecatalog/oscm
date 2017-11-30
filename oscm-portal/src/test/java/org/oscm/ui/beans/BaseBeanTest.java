/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.ui.beans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import javax.faces.context.ExternalContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.junit.Before;
import org.junit.Test;
import org.oscm.internal.intf.IdentityService;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.usermanagement.UserService;
import org.oscm.internal.vo.VOUserDetails;
import org.oscm.ui.common.Constants;
import org.oscm.ui.common.JSFUtils;
import org.oscm.ui.stubs.ExternalContextStub;
import org.oscm.ui.stubs.FacesContextStub;
import org.oscm.ui.stubs.HttpServletRequestStub;
import org.oscm.ui.stubs.HttpSessionStub;

public class BaseBeanTest {
    BaseBean ctrl;
    int callsToService;
    private IdentityService identityService;
    VOUserDetails voUser;
    VOUserDetails voUserInSession;
    ExternalContextStub externalContextStub;
    HttpServletResponse responseMock;

    @Before
    public void setUp() throws IOException {
        voUser = prepareVOUserDetail();
        voUserInSession = prepareSessionVOUserDetail();
        final HttpSession session = new HttpSessionStub(Locale.ENGLISH);
        session.setAttribute(Constants.SESS_ATTR_USER, voUserInSession);
        final HttpServletRequest request = new HttpServletRequestStub(
                Locale.ENGLISH) {
            @Override
            public HttpSession getSession() {
                return session;
            }
        };

        responseMock = mock(HttpServletResponse.class);
        OutputStream out = mock(ServletOutputStream.class);
        doReturn(out).when(responseMock).getOutputStream();

        externalContextStub = new ExternalContextStub(Locale.ENGLISH) {
            @Override
            public Object getRequest() {
                return request;
            }

            @Override
            public Object getResponse() {
                return responseMock;
            }

        };

        new FacesContextStub(Locale.ENGLISH) {
            @Override
            public ExternalContext getExternalContext() {
                return externalContextStub;
            }

            @Override
            public void responseComplete() {

            }
        };

        callsToService = 0;
        ctrl = new BaseBean() {
            @Override
            protected <T> T getService(final Class<T> clazz, Object service) {
                if (service == null) {
                    callsToService++;
                }
                return clazz.cast(service);
            }
        };
        identityService = mock(IdentityService.class);
        ctrl.idService = identityService;
        doReturn(voUser).when(identityService).getCurrentUserDetails();

    }

    @Test
    public void getUserService() {
        assertNull(ctrl.userService);

        UserService us = ctrl.getUserService();

        assertEquals(us, ctrl.userService);
        assertEquals(1, callsToService);
    }

    @Test
    public void getUserService_Initialized() {
        UserService us = mock(UserService.class);
        ctrl.userService = us;

        UserService read = ctrl.getUserService();

        assertEquals(us, read);
        assertEquals(0, callsToService);
    }

    @Test
    public void isCurrentUserRolesChanged_roleIncreased() {
        // when
        boolean result = ctrl.isCurrentUserRolesChanged();
        // then
        assertEquals(Boolean.TRUE, Boolean.valueOf(result));
    }

    @Test
    public void isCurrentUserRolesChanged_roleUnchanged() {
        // given
        voUserInSession.addUserRole(UserRoleType.SERVICE_MANAGER);
        // when
        boolean result = ctrl.isCurrentUserRolesChanged();
        // then
        assertEquals(Boolean.FALSE, Boolean.valueOf(result));

    }

    @Test
    public void isCurrentUserRolesChanged_roleModified() {
        // given
        voUserInSession.addUserRole(UserRoleType.TECHNOLOGY_MANAGER);
        // when
        boolean result = ctrl.isCurrentUserRolesChanged();
        // then
        assertEquals(Boolean.TRUE, Boolean.valueOf(result));

    }

    public VOUserDetails prepareVOUserDetail() {
        VOUserDetails voUser = prepareSessionVOUserDetail();
        voUser.addUserRole(UserRoleType.SERVICE_MANAGER);
        return voUser;
    }

    public VOUserDetails prepareSessionVOUserDetail() {
        VOUserDetails voCurrentUser = new VOUserDetails();
        voCurrentUser.setKey(1);
        Set<UserRoleType> currentUserRoles = new HashSet<UserRoleType>();
        currentUserRoles.add(UserRoleType.MARKETPLACE_OWNER);
        voCurrentUser.setUserRoles(currentUserRoles);
        return voCurrentUser;
    }

    @Test
    public void writeContentToResponse() throws Exception {
        byte[] content = "Some file content.\n".getBytes();
        String fileName = "fileName.xml";

        // when
        JSFUtils.writeContentToResponse(content, fileName, "Text");

        // then
        verify(responseMock, times(1)).setHeader(eq("Content-disposition"),
                eq("attachment; filename=\"" + fileName + "\""));
        verify(responseMock, times(1)).setContentLength(eq(content.length));
    }
}
