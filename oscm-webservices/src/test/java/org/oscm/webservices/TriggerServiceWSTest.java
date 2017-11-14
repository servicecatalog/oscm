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

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.PlatformUser;
import org.oscm.webservices.logger.WebServiceLogger;
import org.oscm.types.enumtypes.TriggerType;

/**
 * @author tokoda
 * 
 */
public class TriggerServiceWSTest {
    private TriggerServiceWS serviceWS;
    private org.oscm.internal.intf.TriggerService serviceMock;

    @Before
    public void setup() throws Exception {
        serviceMock = mock(org.oscm.internal.intf.TriggerService.class);
        serviceWS = new TriggerServiceWS();
        serviceWS.delegate = serviceMock;
        serviceWS.WS_LOGGER = mock(WebServiceLogger.class);
        PlatformUser user = mock(PlatformUser.class);
        DataService ds = mock(DataService.class);
        when(ds.getCurrentUser()).thenReturn(user);
        serviceWS.ds = ds;
    }

    @Test
    public void testLogInjectionForTriggerServiceWS() throws Exception {

        serviceWS.approveAction(0);
        serviceWS.cancelActions(null, null);
        serviceWS.deleteActions(null);
        serviceWS.getAllActions();
        serviceWS.getAllActionsForOrganization();
        serviceWS.getAllDefinitions();
        serviceWS.rejectAction(0, null);
        serviceWS.updateActionParameters(0, null);

        verify(serviceMock, times(1)).approveAction(0);
        verify(serviceMock, times(1)).cancelActions(null, null);
        verify(serviceMock, times(1)).deleteActions(null);
        verify(serviceMock, times(1)).getAllActions();
        verify(serviceMock, times(1)).getAllActionsForOrganization();
        verify(serviceMock, times(1)).getAllDefinitions();
        verify(serviceMock, times(1)).rejectAction(0, null);
        verify(serviceMock, times(1)).updateActionParameters(0, null);
    }

    @Test
    public void getAllDefinitions() {
        // given
        given(serviceWS.delegate.getAllDefinitions()).willReturn(
                getTriggerDefinitionFromVariousBesVersion());

        // when
        List<org.oscm.vo.VOTriggerDefinition> result = serviceWS
                .getAllDefinitions();

        // then
        assertEquals(2, result.size());
        assertEquals(TriggerType.ACTIVATE_SERVICE, result.get(0).getType());
        assertEquals(TriggerType.SUBSCRIPTION_CREATION, result.get(1).getType());
    }

    private List<org.oscm.internal.vo.VOTriggerDefinition> getTriggerDefinitionFromVariousBesVersion() {
        List<org.oscm.internal.vo.VOTriggerDefinition> result = new LinkedList<org.oscm.internal.vo.VOTriggerDefinition>();

        org.oscm.internal.vo.VOTriggerDefinition def1 = new org.oscm.internal.vo.VOTriggerDefinition();
        def1.setType(org.oscm.internal.types.enumtypes.TriggerType.ACTIVATE_SERVICE); // 1.1
        result.add(def1);

        org.oscm.internal.vo.VOTriggerDefinition def2 = new org.oscm.internal.vo.VOTriggerDefinition();
        def2.setType(org.oscm.internal.types.enumtypes.TriggerType.SUBSCRIPTION_CREATION); // 1.4
        result.add(def2);
        return result;
    }
}
