/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 2014-2-25                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.ui.serviceinstance;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.naming.InitialContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.oscm.app.business.exceptions.BadResultException;
import org.oscm.app.business.exceptions.ServiceInstanceException;
import org.oscm.app.domain.InstanceOperation;
import org.oscm.app.domain.InstanceParameter;
import org.oscm.app.domain.ServiceInstance;
import org.oscm.app.ui.SessionConstants;
import org.oscm.app.v2_0.data.ServiceUser;
import org.oscm.app.v2_0.service.APPTimerServiceBean;
import org.oscm.app.v2_0.service.ServiceInstanceServiceBean;

/**
 * Unit test for ManageServiceInstanceCtrl
 * 
 * @author Mao
 * 
 */
public class ManageServiceInstanceCtrlTest {

    private static final String OUTCOME_SUCCESS = "success";
    private static final String OUTCOME_ERROR = "error";
    protected static final String OUTCOME_SAMEPAGE = "refresh";
    private static final String ENCRYPTED_PWD = "*********";
    private ManageServiceInstanceCtrl ctrl;
    private ManageServiceInstanceModel model;
    private ServiceInstanceServiceBean serviceInstanceService;
    private APPTimerServiceBean timerService;
    private FacesContext facesContext;
    private ExternalContext externalContext;
    private HttpSession httpSession;
    private HttpServletRequest request;
    private HttpSession session;
    private InitialContext initialContext;

    @Before
    public void setup() throws Exception {

        facesContext = mock(FacesContext.class);
        Application application = mock(Application.class);
        when(facesContext.getApplication()).thenReturn(application);
        when(application.getDefaultLocale()).thenReturn(Locale.FRANCE);

        externalContext = Mockito.mock(ExternalContext.class);
        httpSession = Mockito.mock(HttpSession.class);

        Mockito.when(facesContext.getExternalContext()).thenReturn(
                externalContext);

        Mockito.when(externalContext.getSession(Matchers.anyBoolean()))
                .thenReturn(httpSession);
        Mockito.when(httpSession.getAttribute(Matchers.anyString()))
                .thenReturn("mockUserId");

        request = mock(HttpServletRequest.class);
        session = mock(HttpSession.class);
        initialContext = mock(InitialContext.class);
        model = new ManageServiceInstanceModel();
        ctrl = new ManageServiceInstanceCtrl() {
            @Override
            protected FacesContext getFacesContext() {
                return facesContext;
            }

            @Override
            protected HttpServletRequest getRequest() {
                return request;
            }

            @Override
            protected InitialContext getInitialContext() {
                return initialContext;
            }

        };
        ctrl.model = model;
        serviceInstanceService = mock(ServiceInstanceServiceBean.class);
        timerService = mock(APPTimerServiceBean.class);
        doReturn(givenInstanceParameters()).when(serviceInstanceService)
                .getInstanceParameters(any(ServiceInstance.class), anyString());
        ctrl.serviceInstanceService = serviceInstanceService;
        ctrl.timerService = timerService;
        doReturn(session).when(request).getSession();
        doReturn("ess.ror").when(request).getParameter(
                eq(SessionConstants.SESSION_CTRL_ID));
    }

    @Test
    public void filterOperation_Failed() throws Exception {
        // given
        ServiceInstance serviceInstance = new ServiceInstance();
        serviceInstance.setRunWithTimer(false);
        serviceInstance.setControllerReady(true);

        // when
        boolean result = ctrl.filterOperation(InstanceOperation.RESUME,
                serviceInstance);

        // then
        assertEquals(Boolean.FALSE, Boolean.valueOf(result));
    }

    @Test
    public void filterOperation_Success() throws Exception {
        // given
        ServiceInstance serviceInstance = new ServiceInstance();
        serviceInstance.setRunWithTimer(true);
        serviceInstance.setControllerReady(false);

        // when
        boolean result = ctrl.filterOperation(InstanceOperation.SUSPEND,
                serviceInstance);

        // then
        assertEquals(Boolean.TRUE, Boolean.valueOf(result));
    }

    @Test
    public void getServiceInstanceService() throws Exception {
        ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);

        // given
        doReturn(null).when(initialContext).lookup(argument.capture());

        ctrl.serviceInstanceService = null;
        // when
        ctrl.getServiceInstanceService();

        // then
        verify(initialContext).lookup(anyString());
        assertEquals("java:comp/env/ejb/ServiceInstanceService",
                argument.getValue());

    }

    @Test
    public void getAPPTimerService() throws Exception {
        ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);

        // given
        doReturn(null).when(initialContext).lookup(argument.capture());

        ctrl.timerService = null;
        // when
        ctrl.getAPPTimerService();

        // then
        verify(initialContext).lookup(anyString());
        assertEquals("java:comp/env/ejb/APPTimerService", argument.getValue());
    }

    @Test
    public void getInitialize_ControllerIdEmpty() {
        // given
        doReturn("").when(request).getParameter(
                eq(SessionConstants.SESSION_CTRL_ID));
        doReturn(session).when(request).getSession();
        doReturn("uid").when(session).getAttribute(
                SessionConstants.SESSION_USER_ID);
        doReturn("en").when(session).getAttribute(
                SessionConstants.SESSION_USER_LOCALE);

        // when
        ctrl.getInitialize();

        // then
        verify(facesContext, times(1)).addMessage(anyString(),
                any(FacesMessage.class));
    }

    @Test
    public void getInitialize_ServiceInstanceException() throws Exception {
        // given
        doReturn(session).when(request).getSession();
        doReturn("1").when(session).getAttribute(
                SessionConstants.SESSION_CTRL_ID);
        doThrow(new ServiceInstanceException(new BadResultException(""))).when(
                serviceInstanceService).getInstancesForController(anyString());
        // when
        ctrl.getInitialize();

        // then
        verify(facesContext, times(1)).addMessage(anyString(),
                any(FacesMessage.class));

    }

    @Test
    public void getInitialize_Success() throws Exception {
        // given
        ctrl.setModel(null);
        List<ServiceInstance> serviceInstances = givenServiceInstances();
        EnumSet<InstanceOperation> operaions = EnumSet.of(
                InstanceOperation.DELETE, InstanceOperation.RESUME);
        doReturn(session).when(request).getSession();
        doReturn("1").when(session).getAttribute(
                SessionConstants.SESSION_CTRL_ID);
        doReturn(serviceInstances).when(serviceInstanceService)
                .getInstancesForController(anyString());
        doReturn(operaions).when(serviceInstanceService)
                .listOperationsForInstance(any(ServiceInstance.class));

        // when
        ctrl.getInitialize();

        // then
        verify(facesContext, times(0)).addMessage(anyString(),
                any(FacesMessage.class));
    }

    @Test
    public void getInitialize_Repeated() throws Exception {
        // when
        ctrl.getInitialize();
        ctrl.getInitialize();

        // then
        verify(ctrl.serviceInstanceService, times(1))
                .getInstancesForController(anyString());
        // when
        ctrl.getInitialize();

        // then
        verifyNoMoreInteractions(ctrl.serviceInstanceService);
        verify(facesContext, times(0)).addMessage(anyString(),
                any(FacesMessage.class));
    }

    @Test
    public void executeService_ServiceInstanceException() throws Exception {
        // given
        model.setToken(model.getToken());
        model.setServiceInstanceRows(givenServiceInstanceRows());
        model.setSelectedInstanceId("instanceId2");
        doReturn(session).when(request).getSession();
        doReturn("1").when(session).getAttribute(
                SessionConstants.SESSION_CTRL_ID);
        doThrow(new ServiceInstanceException(new BadResultException(""))).when(
                ctrl.serviceInstanceService).executeOperation(
                any(ServiceInstance.class), any(ServiceUser.class),
                any(InstanceOperation.class));
        doReturn(new ServiceInstance()).when(serviceInstanceService).find(
                any(ServiceInstance.class), anyString());

        // when
        String result = ctrl.executeService();

        // then
        assertEquals(OUTCOME_ERROR, result);
    }

    @Test
    public void executeService_Success() throws Exception {
        // given
        model.setToken(model.getToken());
        model.setInitialized(true);
        model.setServiceInstanceRows(givenServiceInstanceRows());
        model.setSelectedInstanceId("instanceId2");
        doReturn(session).when(request).getSession();
        doReturn("1").when(session).getAttribute(
                SessionConstants.SESSION_CTRL_ID);
        doReturn("uid").when(session).getAttribute(
                SessionConstants.SESSION_USER_ID);
        doReturn("en").when(session).getAttribute(
                SessionConstants.SESSION_USER_LOCALE);
        doNothing().when(ctrl.serviceInstanceService).executeOperation(
                any(ServiceInstance.class), any(ServiceUser.class),
                any(InstanceOperation.class));
        doReturn(new ServiceInstance()).when(serviceInstanceService).find(
                any(ServiceInstance.class), anyString());

        // when
        String result = ctrl.executeService();

        // then
        assertEquals(OUTCOME_SUCCESS, result);
        assertEquals(Boolean.TRUE, Boolean.valueOf(model.isInitialized()));
    }

    @Test
    public void executeService_AbortPending() throws Exception {
        // given
        model.setToken(model.getToken());
        model.setInitialized(true);
        model.setServiceInstanceRows(givenServiceInstanceRowsWithAbortPending());
        model.setSelectedInstanceId("instanceId1");
        doReturn(session).when(request).getSession();
        doReturn("1").when(session).getAttribute(
                SessionConstants.SESSION_CTRL_ID);
        doNothing().when(ctrl.serviceInstanceService).executeOperation(
                any(ServiceInstance.class), any(ServiceUser.class),
                any(InstanceOperation.class));
        doReturn("uid").when(session).getAttribute(
                SessionConstants.SESSION_USER_ID);
        doReturn("en").when(session).getAttribute(
                SessionConstants.SESSION_USER_LOCALE);
        ServiceInstance dbInstance = new ServiceInstance();
        dbInstance.setRunWithTimer(false);
        dbInstance.setControllerReady(false);
        doReturn(dbInstance).when(serviceInstanceService).find(
                any(ServiceInstance.class), anyString());

        // when
        String result = ctrl.executeService();

        // then
        assertEquals(OUTCOME_SUCCESS, result);
        assertEquals(Boolean.TRUE, Boolean.valueOf(model.isInitialized()));
        verify(facesContext, times(2)).addMessage(anyString(),
                any(FacesMessage.class));
    }

    @Test
    public void updateSelectedServiceInstanceRow_RowInvalid() {
        // given
        model.setServiceInstanceRows(givenServiceInstanceRows());
        model.setSelectedInstanceId("invalidId");

        // when
        ctrl.updateSelectedServiceInstanceRow();

        ServiceInstanceRow row = ctrl.model.getSelectedInstanceRow();
        assertEquals(null, row);
    }

    @Test
    public void updateSelectedServiceInstanceRow_RowExist() {
        // given
        model.setServiceInstanceRows(givenServiceInstanceRows());
        model.setSelectedInstanceId("instanceId1");

        // when
        ctrl.updateSelectedServiceInstanceRow();

        ServiceInstanceRow row = ctrl.model.getSelectedInstanceRow();
        assertEquals("instanceId1", row.getServiceInstance().getInstanceId());
    }

    @Test
    public void updateSelectedServiceInstanceRow_InitParameters() {
        // given
        model.setServiceInstanceRows(givenServiceInstanceRows());
        model.setSelectedInstanceId("instanceId1");

        // when
        ctrl.updateSelectedServiceInstanceRow();

        ServiceInstanceRow row = ctrl.model.getSelectedInstanceRow();
        assertEquals("instanceId1", row.getServiceInstance().getInstanceId());
        assertEquals("PARAM_A", model.getServiceInstanceRows().get(0)
                .getInstanceParameters().get(0).getParameterKey());
        assertEquals("PARAM_B", model.getServiceInstanceRows().get(0)
                .getInstanceParameters().get(1).getParameterKey());
    }

    @Test
    public void updateSelectedServiceInstanceRow_EncryptedParametersHidden()
            throws Exception {
        // given
        doReturn(givenEncryptedInstanceParameters()).when(
                serviceInstanceService).getInstanceParameters(
                any(ServiceInstance.class), anyString());
        model.setServiceInstanceRows(givenServiceInstanceRows());
        model.setSelectedInstanceId("instanceId2");

        // when
        ctrl.updateSelectedServiceInstanceRow();

        // then
        ServiceInstanceRow row = ctrl.model.getSelectedInstanceRow();
        assertEquals("instanceId2", row.getServiceInstance().getInstanceId());
        assertEquals("PARAM_A_PWD", model.getServiceInstanceRows().get(1)
                .getInstanceParameters().get(0).getParameterKey());
        assertEquals("PARAM_B", model.getServiceInstanceRows().get(1)
                .getInstanceParameters().get(1).getParameterKey());
        assertEquals("123", model.getServiceInstanceRows().get(1)
                .getInstanceParameters().get(1).getParameterValue());
    }

    @Test
    public void initTimer() throws Exception {
        // given
        model.setToken(model.getToken());
        doNothing().when(timerService).initTimers();
        doReturn(session).when(request).getSession();
        doReturn("uid").when(session).getAttribute(
                SessionConstants.SESSION_USER_ID);
        doReturn("en").when(session).getAttribute(
                SessionConstants.SESSION_USER_LOCALE);

        // when
        ctrl.initTimer();

        // then
        verify(timerService, times(1)).initTimers();
    }

    @Test
    public void getTimePattern() throws Exception {
        // given
        Calendar cal = givenCalendarTime();
        ctrl.getInitialize();
        model = ctrl.getModel();

        // when
        String pattern = model.getTimePattern();
        String actual = applyTimePattern(cal, pattern);

        // then
        assertEquals("2014-03-14 12:23:58 123 GMT", actual);
    }

    private String applyTimePattern(Calendar cal, String pattern) {
        DateFormat df = new SimpleDateFormat(pattern);
        df.setTimeZone(cal.getTimeZone());
        String actual = df.format(cal.getTime());
        return actual;
    }

    private Calendar givenCalendarTime() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal.set(Calendar.YEAR, 2014);
        cal.set(Calendar.MONTH, 2);
        cal.set(Calendar.DAY_OF_MONTH, 14);
        cal.set(Calendar.HOUR_OF_DAY, 12);
        cal.set(Calendar.MINUTE, 23);
        cal.set(Calendar.SECOND, 58);
        cal.set(Calendar.MILLISECOND, 123);
        return cal;
    }

    private List<ServiceInstance> givenServiceInstances() {
        List<ServiceInstance> instances = new ArrayList<ServiceInstance>();

        ServiceInstance instance1 = new ServiceInstance();
        instance1.setInstanceId("instanceId1");

        ServiceInstance instance2 = new ServiceInstance();
        instance2.setInstanceId("instanceId2");

        instances.add(instance1);
        instances.add(instance2);

        return instances;
    }

    private List<InstanceParameter> givenInstanceParameters() {
        List<InstanceParameter> params = new ArrayList<InstanceParameter>();

        InstanceParameter param1 = new InstanceParameter();
        param1.setParameterKey("PARAM_B");
        param1.setParameterValue("VALUE_B");

        InstanceParameter param2 = new InstanceParameter();
        param2.setParameterKey("PARAM_A");
        param2.setParameterValue("VALUE_A");

        params.add(param1);
        params.add(param2);

        return params;
    }

    private List<InstanceParameter> givenEncryptedInstanceParameters() {
        List<InstanceParameter> params = new ArrayList<InstanceParameter>();

        InstanceParameter param1 = new InstanceParameter();
        param1.setParameterKey("PARAM_A_PWD");
        param1.setParameterValue("ENCRYPTED_VALUE_B");

        InstanceParameter param2 = new InstanceParameter();
        param2.setParameterKey("PARAM_B");
        param2.setParameterValue("123");

        params.add(param1);
        params.add(param2);

        return params;
    }

    private List<ServiceInstanceRow> givenServiceInstanceRows() {
        List<ServiceInstanceRow> instanceRows = new ArrayList<ServiceInstanceRow>();

        ServiceInstance instance1 = new ServiceInstance();
        instance1.setInstanceId("instanceId1");
        instance1.setInstanceParameters(givenInstanceParameters());
        ServiceInstanceRow row1 = new ServiceInstanceRow(instance1,
                InstanceOperation.DELETE.name());

        ServiceInstance instance2 = new ServiceInstance();
        instance2.setInstanceId("instanceId2");
        instance2.setInstanceParameters(givenEncryptedInstanceParameters());
        ServiceInstanceRow row2 = new ServiceInstanceRow(instance2,
                InstanceOperation.SUSPEND.name());

        instanceRows.add(row1);
        instanceRows.add(row2);

        return instanceRows;
    }

    private List<ServiceInstanceRow> givenServiceInstanceRowsWithAbortPending() {
        List<ServiceInstanceRow> instanceRows = new ArrayList<ServiceInstanceRow>();

        ServiceInstance instance1 = new ServiceInstance();
        instance1.setInstanceId("instanceId1");
        instance1.setInstanceParameters(givenInstanceParameters());
        ServiceInstanceRow row1 = new ServiceInstanceRow(instance1,
                InstanceOperation.ABORT_PENDING.name());

        instanceRows.add(row1);

        return instanceRows;
    }

}
