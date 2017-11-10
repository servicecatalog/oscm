/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 2014年9月10日                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.triggerservice.bean;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import javax.persistence.Query;

import org.junit.Before;
import org.junit.Test;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.TriggerDefinition;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.vo.VOTriggerDefinition;

/**
 * Unit test for TriggerDefinitionServiceBean
 * 
 * @author gaowenxin
 * 
 */
public class TriggerDefinitionServiceBeanTest {

    private TriggerDefinitionServiceBean triggerDefinitionService;
    private VOTriggerDefinition voTriggerDefinition;
    private TriggerDefinition triggerDefinition;
    private PlatformUser currentUser;
    private Organization organization;
    private Query query;
    private DataService dm;

    @Before
    public void setup() throws Exception {
        triggerDefinitionService = new TriggerDefinitionServiceBean();
        dm = mock(DataService.class);
        triggerDefinitionService.dm = dm;
        query = mock(Query.class);
        doReturn(query).when(dm).createNamedQuery(
                eq("TriggerProcess.getAllForTriggerDefinition"));
        prepareCurrentUser();
        prepareTriggerDefinition();
    }

    @Test
    public void deleteTriggerDefinition_OK() throws Exception {
        // given
        prepareVOTriggerDefinition(1);
        // when
        triggerDefinitionService.deleteTriggerDefinition(voTriggerDefinition);
        // then

    }

    @Test(expected = ConcurrentModificationException.class)
    public void deleteTriggerDefinition_ConcurrentModificationException()
            throws Exception {
        // given
        prepareVOTriggerDefinition(-1);
        // when
        try {
            triggerDefinitionService
                    .deleteTriggerDefinition(voTriggerDefinition);
            // then
            fail();
        } catch (Exception e) {
            assertTrue(e.getMessage().contains(
                    "Outdated VOTriggerDefinition in version"));
            throw e;
        }

    }

    private VOTriggerDefinition prepareVOTriggerDefinition(int version) {
        voTriggerDefinition = new VOTriggerDefinition();
        voTriggerDefinition.setKey(1000l);
        voTriggerDefinition.setVersion(version);
        return voTriggerDefinition;
    }

    private TriggerDefinition prepareTriggerDefinition() throws Exception {
        triggerDefinition = new TriggerDefinition();
        triggerDefinition.setOrganization(organization);
        triggerDefinition.setKey(1000l);
        doReturn(triggerDefinition).when(dm).getReference(
                eq(TriggerDefinition.class), eq(1000l));
        return triggerDefinition;
    }

    private PlatformUser prepareCurrentUser() throws Exception {
        organization = new Organization();
        organization.setKey(10l);
        currentUser = new PlatformUser();
        currentUser.setOrganization(organization);
        currentUser.setKey(100l);
        doReturn(currentUser).when(dm).getCurrentUser();
        return currentUser;
    }
}
