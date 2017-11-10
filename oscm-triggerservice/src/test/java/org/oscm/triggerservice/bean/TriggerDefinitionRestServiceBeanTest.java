/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: Jun 9, 2016                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.triggerservice.bean;

import javax.persistence.Query;

import org.junit.Test;
import org.mockito.Mockito;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.TriggerDefinition;

/**
 * Unit test for TriggerDefinitionRestServiceBean
 * 
 * @author miethaner
 */
public class TriggerDefinitionRestServiceBeanTest {

    @Test
    public void testGetTriggerDefinition() {

        TriggerDefinition definition = new TriggerDefinition();
        definition.setKey(123L);

        DataService dm = Mockito.mock(DataService.class);
        Query q = Mockito.mock(Query.class);

        Mockito.when(
                dm.createNamedQuery("TriggerDefinition.getDefinitionByResourceId"))
                .thenReturn(q);
        Mockito.when(q.getSingleResult()).thenReturn(definition);

    }

}
