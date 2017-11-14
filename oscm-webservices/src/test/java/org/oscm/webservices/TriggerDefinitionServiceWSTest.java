/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Brandstetter                                                     
 *                                                                              
 *******************************************************************************/

package org.oscm.webservices;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.Principal;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.PlatformUser;
import org.oscm.types.enumtypes.TriggerType;
import org.oscm.vo.VOTriggerDefinition;

public class TriggerDefinitionServiceWSTest {

    private TriggerDefinitionServiceWS serviceWS;
    private org.oscm.internal.intf.TriggerDefinitionService serviceMock;
    private HttpServletRequest requestMock;

    @Before
    public void setup() {
        serviceMock = mock(org.oscm.internal.intf.TriggerDefinitionService.class);
        serviceWS = new TriggerDefinitionServiceWS();
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
    public void testLogInjectionForTriggerDefinitionServiceWS()
            throws Exception {
        serviceWS.createTriggerDefinition(null);
        verify(serviceMock, times(1)).createTriggerDefinition(null);

        serviceWS.deleteTriggerDefinition(0);
        verify(serviceMock, times(1)).deleteTriggerDefinition(0);

        serviceWS.getTriggerDefinitions();
        verify(serviceMock, times(1)).getTriggerDefinitions();

        serviceWS.getTriggerTypes();
        verify(serviceMock, times(1)).getTriggerTypes();

        serviceWS.updateTriggerDefinition(null);
        verify(serviceMock, times(1)).updateTriggerDefinition(null);

        verify(requestMock, times(5)).getRemoteAddr();
    }

    @Test
    public void getTriggerTypes() {
        // given
        given(serviceWS.delegate.getTriggerTypes()).willReturn(
                getTriggerTypeFromVariousBesVersion());

        // when
        List<TriggerType> result = serviceWS.getTriggerTypes();

        // then
        assertEquals(2, result.size());
        assertEquals(TriggerType.ACTIVATE_SERVICE, result.get(0));
        assertEquals(TriggerType.SUBSCRIPTION_CREATION, result.get(1));
    }

    private List<org.oscm.internal.types.enumtypes.TriggerType> getTriggerTypeFromVariousBesVersion() {
        List<org.oscm.internal.types.enumtypes.TriggerType> result = new LinkedList<org.oscm.internal.types.enumtypes.TriggerType>();
        result.add(org.oscm.internal.types.enumtypes.TriggerType.ACTIVATE_SERVICE); // 1.3
        result.add(org.oscm.internal.types.enumtypes.TriggerType.SUBSCRIPTION_CREATION); // 1.4
        return result;
    }

    @Test
    public void getTriggerDefinitions() {
        // given
        given(serviceWS.delegate.getTriggerDefinitions()).willReturn(
                triggerDefinitionsFromVariousBesVersion());

        // when
        List<VOTriggerDefinition> result = serviceWS.getTriggerDefinitions();

        // then
        assertEquals(2, result.size());
        assertEquals(TriggerType.ACTIVATE_SERVICE, result.get(0).getType());
        assertEquals(TriggerType.SUBSCRIPTION_CREATION, result.get(1).getType());
    }

    private List<org.oscm.internal.vo.VOTriggerDefinition> triggerDefinitionsFromVariousBesVersion() {
        List<org.oscm.internal.vo.VOTriggerDefinition> result = new LinkedList<org.oscm.internal.vo.VOTriggerDefinition>();

        org.oscm.internal.vo.VOTriggerDefinition def1 = new org.oscm.internal.vo.VOTriggerDefinition();
        def1.setType(org.oscm.internal.types.enumtypes.TriggerType.ACTIVATE_SERVICE); // 1.3
        result.add(def1);

        org.oscm.internal.vo.VOTriggerDefinition def2 = new org.oscm.internal.vo.VOTriggerDefinition();
        def2.setType(org.oscm.internal.types.enumtypes.TriggerType.SUBSCRIPTION_CREATION); // 1.4
        result.add(def2);
        return result;
    }
}
