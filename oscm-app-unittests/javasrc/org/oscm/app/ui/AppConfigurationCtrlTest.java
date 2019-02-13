/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2018
 *
 *  Sample controller implementation for the 
 *  BSS Asynchronous Provisioning Proxy (APP)
 *
 *  Creation Date: 06.09.2012                                                      
 *
 *******************************************************************************/
package org.oscm.app.ui;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.*;
import org.oscm.app.ui.appconfiguration.AppConfigurationCtrl;
import org.oscm.app.ui.appconfiguration.AppConfigurationModel;
import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.oscm.app.v2_0.intf.APPlatformController;
import org.oscm.app.v2_0.service.APPConfigurationServiceBean;
import org.oscm.app.v2_0.service.APPTimerServiceBean;
import org.oscm.test.ejb.TestNamingContextFactoryBuilder;
import org.oscm.types.constants.Configuration;

import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.spi.NamingManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

/**
 * Test case for AppConfigurationCtrl
 * <p>
 * Mao
 */
public class AppConfigurationCtrlTest {

    protected static final String OUTCOME_SAMEPAGE = "refresh";
    protected static final String OUTCOME_SUCCESS = "success";

    @Spy
    private AppConfigurationCtrl ctrl;
    private AppConfigurationModel model;
    private APPlatformController serviceController;
    private FacesContext facesContext;
    private ExternalContext externalContext;
    private HttpSession httpSession;
    private HttpServletRequest request;
    private HttpSession session;

    private InitialContext initialContext;
    private APPConfigurationServiceBean appConfigService;
    private APPTimerServiceBean appTimerService;
    private final HashMap<String, String> items = new HashMap<String, String>();

    @SuppressWarnings("unchecked")
    @Before
    public void setup() throws Exception {

        facesContext = mock(FacesContext.class);
        externalContext = Mockito.mock(ExternalContext.class);
        httpSession = Mockito.mock(HttpSession.class);
        request = Mockito.mock(HttpServletRequest.class);

        Mockito.when(facesContext.getExternalContext()).thenReturn(
                externalContext);

        Mockito.when(externalContext.getSession(Matchers.anyBoolean()))
                .thenReturn(httpSession);
        Mockito.when(externalContext.getRequest()).thenReturn(request);
        Mockito.when(httpSession.getAttribute(Matchers.anyString()))
                .thenReturn("mockUserId");
        doReturn(session).when(request).getSession();
        session = mock(HttpSession.class);

        Application application = mock(Application.class);
        when(facesContext.getApplication()).thenReturn(application);
        when(application.getDefaultLocale()).thenReturn(Locale.FRANCE);

        initialContext = mock(InitialContext.class);
        model = new AppConfigurationModel();
        ctrl = spy(new AppConfigurationCtrl() {
            @Override
            protected FacesContext getFacesContext() {
                return facesContext;
            }

            @Override
            protected InitialContext getInitialContext() {
                return initialContext;
            }

        });

        ctrl.setModel(model);
        appConfigService = mock(APPConfigurationServiceBean.class);
        when(ctrl.getAppConfigService()).thenReturn(appConfigService);
        appTimerService = mock(APPTimerServiceBean.class);
        when(ctrl.getAPPTimerService()).thenReturn(appTimerService);

        items.clear();
        items.put("key1", "value1");
        items.put("key2", "value2");
        doNothing().when(appConfigService).storeControllerOrganizations(
                any(HashMap.class));

        MockitoAnnotations.initMocks(this);
        serviceController = mock(APPlatformController.class);
    }

    @Test
    public void getAppConfigService() throws Exception {
        ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);

        // given
        doReturn(null).when(initialContext).lookup(argument.capture());

        // when
        ctrl.getAppConfigService();

        // then
        verify(initialContext).lookup(anyString());
        assertEquals("java:comp/env/ejb/APPConfigurationService",
                argument.getValue());

    }

    @Test
    public void getInitialize_Success() throws Exception {
        // given
        doReturn(items).when(appConfigService).getControllerOrganizations();

        // when
        ctrl.getInitialize();

        // then
        verify(facesContext, times(0)).addMessage(anyString(),
                any(FacesMessage.class));

    }

    @Test
    public void save_TokenInvalid() throws Exception {
        // when
        String result = ctrl.save();

        // then
        assertEquals(OUTCOME_SAMEPAGE, result);
    }

    @Test
    public void save_Add_ControllerIdExist() throws Exception {
        // given
        model.setToken(model.getToken());
        model.setNewControllerId("key1");
        model.setItems(items);

        // when
        String result = ctrl.save();

        // then
        assertEquals(OUTCOME_SUCCESS, result);
    }

    @Test
    public void save_Add_Success() throws Exception {
        // given
        model.setToken(model.getToken());
        model.setNewControllerId("key3");
        model.setNewOrganizationId("Value3");
        model.setItems(items);
        model.setControllerIds(new HashSet<String>(items.keySet()));
        // when
        String result = ctrl.save();

        // then
        verify(facesContext, times(1)).addMessage(anyString(),
                any(FacesMessage.class));
        assertEquals(OUTCOME_SUCCESS, result);
        assertEquals(Boolean.FALSE, Boolean.valueOf(model.isInitialized()));
    }

    @Test
    public void save_Modify_ValueMondatory() throws Exception {
        // given
        items.put("key5", "");
        model.setToken(model.getToken());
        model.setItems(items);
        model.setControllerIds(new HashSet<String>(items.keySet()));
        // when
        String result = ctrl.save();

        // then
        verify(facesContext, times(1)).addMessage(anyString(),
                any(FacesMessage.class));
        assertEquals(OUTCOME_SUCCESS, result);
    }

    @Test
    public void deleteController_TokenInvalid() throws Exception {
        // when
        String result = ctrl.deleteController();

        // then
        assertEquals(OUTCOME_SAMEPAGE, result);
    }

    @Test
    public void deleteController_Success() throws Exception {
        // given
        items.put("key6", "value6");
        model.setToken(model.getToken());
        model.setItems(items);
        model.setKeys(new ArrayList<String>());
        model.setSelectedControllerId("key6");

        // when
        String result = ctrl.deleteController();

        // then
        assertEquals("", result);
        assertEquals(Boolean.FALSE, Boolean.valueOf(model.isDirty()));
        assertEquals(Boolean.FALSE, Boolean.valueOf(model.isInitialized()));
    }

    @Test
    public void getLoggedInUserId() {
        // when
        String userId = ctrl.getLoggedInUserId();
        // then
        assertEquals("mockUserId", userId);
    }

    @Test
    public void getLoggedInUserId_Null() {
        // given
        Mockito.when(externalContext.getSession(Matchers.anyBoolean()))
                .thenReturn(null);
        // when
        String userId = ctrl.getLoggedInUserId();
        // then
        assertNull(userId);
    }

    @Test
    public void restart_tokenInvalid() {
        // given
        model.setToken(model.getToken() + "invalid");

        // when
        String result = ctrl.restart();

        // then
        assertEquals(OUTCOME_SAMEPAGE, result);
    }

    @Test
    public void restart_Failed() {
        // given
        model.setToken(model.getToken());
        model.setRestartRequired(true);
        doReturn(Boolean.FALSE).when(appTimerService).restart(anyBoolean());
        doReturn(session).when(request).getSession();
        doReturn("uid").when(session).getAttribute(
                SessionConstants.SESSION_USER_ID);
        doReturn("en").when(session).getAttribute(
                SessionConstants.SESSION_USER_LOCALE);

        // when
        String result = ctrl.restart();

        // then
        assertEquals(OUTCOME_SUCCESS, result);
        assertEquals(Boolean.TRUE, Boolean.valueOf(model.isRestartRequired()));
    }

    @Test
    public void restart() {
        // given
        model.setToken(model.getToken());
        model.setRestartRequired(true);
        doReturn(Boolean.TRUE).when(appTimerService).restart(anyBoolean());
        doReturn(session).when(request).getSession();
        doReturn("uid").when(session).getAttribute(
                SessionConstants.SESSION_USER_ID);
        doReturn("en").when(session).getAttribute(
                SessionConstants.SESSION_USER_LOCALE);

        // when
        String result = ctrl.restart();

        // then
        assertEquals(OUTCOME_SUCCESS, result);
        assertEquals(Boolean.FALSE, Boolean.valueOf(model.isRestartRequired()));
    }

    @Test
    public void shouldInvokeCanPingImplementation_whenCanPingIsRequested_givenWMWareControllerId() {
        setServiceControllerContext(Configuration.VMWARE_CONTROLLER_ID);

        try {
            ctrl.invokeCanPing(Configuration.VMWARE_CONTROLLER_ID);
        } catch (APPlatformException e) {
            fail("An exception has occurred: " + e.getMessage());
        }

        verify(serviceController, atLeastOnce()).canPing();
    }

    @Test
    public void shouldShowError_whenCanPingIsRequested_givenUnsupportedControllerId() {
        String controllerId = "unsupported.controller.id";
        setServiceControllerContext(controllerId);

        try {
            ctrl.invokeCanPing(controllerId);
        } catch (APPlatformException e) {
            fail("An exception has occurred: " + e.getMessage());
        }

        verify(serviceController, Mockito.never()).canPing();
        verify(ctrl, atLeastOnce()).addError(anyString());
    }

    @Test
    public void shouldPassPing_whenPingIsRequested_givenWMWareControllerId() {
        setServiceControllerContext(Configuration.VMWARE_CONTROLLER_ID);
        try {
            when(serviceController.ping(anyString())).thenReturn(true);

            ctrl.invokePing(Configuration.VMWARE_CONTROLLER_ID);

            verify(serviceController, atLeastOnce()).ping(Configuration.VMWARE_CONTROLLER_ID);
        } catch (APPlatformException e) {
            fail("An exception has occurred: " + e.getMessage());

        }
        verify(ctrl, atLeastOnce()).addMessage(anyString());
    }

    @Test
    public void shouldFailPing_whenPingIsRequested_givenWMWareControllerId() {
        setServiceControllerContext(Configuration.VMWARE_CONTROLLER_ID);
        try {
            when(serviceController.ping(anyString())).thenReturn(false);

            ctrl.invokePing(Configuration.VMWARE_CONTROLLER_ID);

            verify(serviceController, atLeastOnce()).ping(anyString());
        } catch (APPlatformException e) {
            fail("An exception has occurred: " + e.getMessage());

        }
        verify(ctrl, never()).addMessage(anyString());
        verify(ctrl, atLeastOnce()).addError("app.message.error.controller.unreachable");
    }

    @Test
    public void shouldShowError_whenPingIsRequested_givenUnsupportedControllerId() {
        String controllerId = "unsupported.controller.id";
        setServiceControllerContext(controllerId);

        try {
            ctrl.invokePing(controllerId);

            verify(serviceController, never()).ping(controllerId);
        } catch (APPlatformException e) {
            fail("An exception has occurred: " + e.getMessage());

        }
        verify(ctrl, atLeastOnce()).addError(anyString());
    }


    private void setServiceControllerContext(String controllerId) {
        doReturn(session).when(request).getSession();
        doReturn("uid").when(session).getAttribute(
                SessionConstants.SESSION_USER_ID);
        doReturn("en").when(session).getAttribute(
                SessionConstants.SESSION_USER_LOCALE);

        try {
            if (!NamingManager.hasInitialContextFactoryBuilder()) {
                NamingManager
                        .setInitialContextFactoryBuilder(new TestNamingContextFactoryBuilder());
            }
            InitialContext context = new InitialContext();
            context.bind(APPlatformController.JNDI_PREFIX + controllerId,
                    serviceController);
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }
}
