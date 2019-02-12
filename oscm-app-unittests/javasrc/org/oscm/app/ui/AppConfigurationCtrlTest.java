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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;

import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.naming.InitialContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.oscm.app.ui.appconfiguration.AppConfigurationCtrl;
import org.oscm.app.ui.appconfiguration.AppConfigurationModel;
import org.oscm.app.v2_0.service.APPConfigurationServiceBean;
import org.oscm.app.v2_0.service.APPTimerServiceBean;

/**
 * Test case for AppConfigurationCtrl
 * 
 * Mao
 */
public class AppConfigurationCtrlTest {

    protected static final String OUTCOME_SAMEPAGE = "refresh";
    protected static final String OUTCOME_SUCCESS = "success";

    private AppConfigurationCtrl ctrl;
    private AppConfigurationModel model;
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
        ctrl = new AppConfigurationCtrl() {
            @Override
            protected FacesContext getFacesContext() {
                return facesContext;
            }

            @Override
            protected InitialContext getInitialContext() {
                return initialContext;
            }

        };

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
    @Ignore("Not implemented")
    public void shouldInvokeCanPing_whenCanPingIsRequested_givenWMwareControllerId() {

    }

    @Test
    @Ignore("Not implemented")
    public void shouldThrowAnException_whenCanPingIsRequested_givenUnsupportedControllerId() {

    }

    @Test
    @Ignore("Not implemented")
    public void shouldInvokeCanPing_whenPingIsRequested_givenWMwareControllerId() {

    }

    @Test
    @Ignore("Not implemented")
    public void shouldThrowAnException_whenPingIsRequested_givenUnsupportedControllerId() {

    }
}
