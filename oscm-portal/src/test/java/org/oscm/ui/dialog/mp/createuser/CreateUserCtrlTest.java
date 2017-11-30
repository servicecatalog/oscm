/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 26.09.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ui.dialog.mp.createuser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;

import javax.faces.application.FacesMessage;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.oscm.internal.components.response.Response;
import org.oscm.internal.components.response.ReturnCode;
import org.oscm.internal.components.response.ReturnType;
import org.oscm.internal.types.constants.HiddenUIConstants;
import org.oscm.internal.types.enumtypes.Salutation;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.MailOperationException;
import org.oscm.internal.usergroupmgmt.UserGroupService;
import org.oscm.internal.usermanagement.POUserAndSubscriptions;
import org.oscm.internal.usermanagement.UserService;
import org.oscm.ui.beans.ApplicationBean;
import org.oscm.ui.beans.BaseBean;
import org.oscm.ui.beans.SessionBean;
import org.oscm.ui.common.UiDelegate;
import org.oscm.ui.dialog.state.TableState;
import org.oscm.ui.stubs.FacesContextStub;
import org.oscm.ui.stubs.UIViewRootStub;

/**
 * @author weiser
 * 
 */
public class CreateUserCtrlTest {

    private static final String MP_ID = "mpid";
    private static final String NAME = "name";

    private CreateUserCtrl ctrl;

    private CreateUserModel model;
    private UserService us;
    private UserGroupService userGroupService;
    private POUserAndSubscriptions uas;
    private ApplicationBean applicationBean;
    private TableState ts;

    private List<FacesMessage> facesMessages = new ArrayList<FacesMessage>();

    @Before
    public void setup() throws Exception {

        FacesContextStub contextStub = new FacesContextStub(Locale.ENGLISH) {
            @Override
            public void addMessage(String arg0, FacesMessage arg1) {
                facesMessages.add(arg1);
            }
        };

        UIViewRootStub vrStub = new UIViewRootStub() {
            @Override
            public Locale getLocale() {
                return Locale.ENGLISH;
            };
        };
        contextStub.setViewRoot(vrStub);

        userGroupService = mock(UserGroupService.class);
        us = mock(UserService.class);
        ctrl = new CreateUserCtrl();
        ctrl.setUserService(us);
        SessionBean mock = mock(SessionBean.class);
        doReturn("tenantId").when(mock).getTenantID();
        ctrl.setSessionBean(mock);
        ctrl.setUserGroupService(userGroupService);

        model = new CreateUserModel();
        applicationBean = mock(ApplicationBean.class);
        ctrl.ui = mock(UiDelegate.class);

        uas = new POUserAndSubscriptions();
        uas.setLocale("en");
        uas.setAvailableRoles(EnumSet.of(UserRoleType.ORGANIZATION_ADMIN));

        when(ctrl.ui.getMarketplaceId()).thenReturn(MP_ID);
        ctrl.setModel(model);
        ctrl.setApplicationBean(applicationBean);
        when(ctrl.ui.getText(anyString())).thenReturn(NAME);

        when(us.getNewUserData()).thenReturn(uas);
        when(us.createNewUser(any(POUserAndSubscriptions.class), anyString()))
                .thenReturn(new Response());

        when(
                Boolean.valueOf(applicationBean
                        .isUIElementHidden(eq(HiddenUIConstants.PANEL_USER_LIST_SUBSCRIPTIONS))))
                .thenReturn(Boolean.FALSE);

        ts = mock(TableState.class);
        ctrl.setTableState(ts);
    }

    @Test
    public void init() {
        ctrl.init();

        verifyInitialization(model);
    }

    @Test
    public void create() throws Exception {
        ctrl.init();
        setData(ctrl.getModel());

        String outcome = ctrl.create();

        assertEquals(BaseBean.OUTCOME_SHOW_DETAILS, outcome);
        verify(ctrl.ui, times(1)).handle(any(Response.class),
                eq(BaseBean.INFO_USER_CREATED),
                eq(model.getUserId().getValue()));
        ArgumentCaptor<POUserAndSubscriptions> ac = ArgumentCaptor
                .forClass(POUserAndSubscriptions.class);
        verify(us, times(1)).createNewUser(ac.capture(), eq(MP_ID));
        verifyPassedValue(ac.getValue());
        verify(ts).resetActivePages();
    }

    @Test
    public void create_MailOperationException() throws Exception {
        ctrl.init();
        // given
        when(us.createNewUser(any(POUserAndSubscriptions.class), anyString()))
                .thenThrow(new MailOperationException());
        setData(ctrl.getModel());
        when(Boolean.valueOf(applicationBean.isInternalAuthMode())).thenReturn(
                Boolean.TRUE);

        // when
        String outcome = ctrl.create();

        // then
        assertEquals(BaseBean.OUTCOME_ERROR, outcome);
        verify(ctrl.ui, times(1)).handleError((String) isNull(),
                eq(BaseBean.ERROR_USER_CREATE_MAIL));
    }

    @Test
    public void create_MailOperationException_NotInternal() throws Exception {
        ctrl.init();
        // given
        when(us.createNewUser(any(POUserAndSubscriptions.class), anyString()))
                .thenThrow(new MailOperationException());
        setData(ctrl.getModel());
        when(Boolean.valueOf(applicationBean.isInternalAuthMode())).thenReturn(
                Boolean.FALSE);

        // when
        String outcome = ctrl.create();

        // then
        assertEquals(BaseBean.OUTCOME_ERROR, outcome);
        verify(ctrl.ui, times(1)).handleError((String) isNull(),
                eq(BaseBean.ERROR_USER_CREATE_MAIL_NOT_INTERNAL));
    }

    @Test
    public void create_Pending() throws Exception {
        ctrl.init();
        Response r = new Response();
        r.getReturnCodes().add(new ReturnCode(ReturnType.INFO, "messageKey"));
        when(us.createNewUser(any(POUserAndSubscriptions.class), anyString()))
                .thenReturn(r);
        setData(ctrl.getModel());

        String outcome = ctrl.create();

        assertEquals(BaseBean.OUTCOME_PENDING, outcome);
        verify(ctrl.ui, times(1)).handle(any(Response.class),
                eq(BaseBean.INFO_USER_CREATED),
                eq(model.getUserId().getValue()));
        ArgumentCaptor<POUserAndSubscriptions> ac = ArgumentCaptor
                .forClass(POUserAndSubscriptions.class);
        verify(us, times(1)).createNewUser(ac.capture(), eq(MP_ID));
        verifyPassedValue(ac.getValue());
    }

    private static void verifyPassedValue(POUserAndSubscriptions uas) {
        assertEquals("email", uas.getEmail());
        assertEquals("firstname", uas.getFirstName());
        assertEquals("lastname", uas.getLastName());
        assertEquals("ja", uas.getLocale());
        assertEquals("userid", uas.getUserId());
        assertEquals(Salutation.MR, uas.getSalutation());
    }

    private static void setData(CreateUserModel m) {
        m.getEmail().setValue("email");
        m.getFirstName().setValue("firstname");
        m.getLastName().setValue("lastname");
        m.getLocale().setValue("ja");
        m.getUserId().setValue("userid");
        m.getSalutation().setValue("MR");
    }

    private static void verifyInitialization(CreateUserModel model) {
        assertEquals("en", model.getLocale().getValue());

        assertNull(model.getFirstName().getValue());
        assertNull(model.getLastName().getValue());
        assertNull(model.getSalutation().getValue());
        assertNull(model.getUserId().getValue());
    }

}
