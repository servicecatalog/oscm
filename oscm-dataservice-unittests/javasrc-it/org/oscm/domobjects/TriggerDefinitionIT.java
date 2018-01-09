/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: Peter Pock                                         
 *                                                                              
 *  Creation Date: 15.06.2010                                                      
 *                                                                              
 *  Completion Time: 15.06.2010                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.junit.Test;

import org.oscm.domobjects.enums.ModificationType;
import org.oscm.test.ReflectiveClone;
import org.oscm.internal.types.enumtypes.TriggerTargetType;
import org.oscm.internal.types.enumtypes.TriggerType;

/**
 * Test of the TriggerDefinition domain object.
 * 
 * @author pock
 * 
 */
public class TriggerDefinitionIT extends DomainObjectTestBase {

    private List<TriggerDefinition> objList = new ArrayList<TriggerDefinition>();

    private void verify(ModificationType modType) throws Exception {
        verify(modType, objList, TriggerDefinition.class);
    }

    @Test
    public void testAdd() throws Exception {
        objList.add(createTriggerDefinition());
        verify(ModificationType.ADD);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidSuspendProcess() throws Exception {
        objList.add(createTriggerDefinition());
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                TriggerDefinition triggerDefinition = mgr.getReference(
                        TriggerDefinition.class, objList.get(0).getKey());
                triggerDefinition.setType(TriggerType.START_BILLING_RUN);
                triggerDefinition.setSuspendProcess(true);
                return null;
            }
        });
    }

    @Test
    public void testModify() throws Exception {
        objList.add(createTriggerDefinition());
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                TriggerDefinition triggerDefinition = mgr.getReference(
                        TriggerDefinition.class, objList.get(0).getKey());
                triggerDefinition.setType(TriggerType.DEACTIVATE_SERVICE);
                triggerDefinition.setTarget("http://localhost:8080");
                triggerDefinition.setSuspendProcess(true);
                objList.remove(0);
                objList.add((TriggerDefinition) ReflectiveClone
                        .clone(triggerDefinition));
                return null;
            }
        });
        verify(ModificationType.MODIFY);
    }

    @Test
    public void testDelete() throws Exception {
        objList.add(createTriggerDefinition());
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                mgr.remove(mgr.getReference(TriggerDefinition.class, objList
                        .get(0).getKey()));
                return null;
            }
        });
        verify(ModificationType.DELETE);
    }

    private TriggerDefinition createTriggerDefinition() throws Exception {
        return runTX(new Callable<TriggerDefinition>() {
            public TriggerDefinition call() throws Exception {
                TriggerDefinition triggerDefinition = new TriggerDefinition();
                triggerDefinition.setType(TriggerType.ACTIVATE_SERVICE);
                triggerDefinition.setTargetType(TriggerTargetType.WEB_SERVICE);
                triggerDefinition.setTarget("http://localhost");
                triggerDefinition.setSuspendProcess(false);
                triggerDefinition.setName("testTrigger");
                mgr.persist(triggerDefinition);

                return (TriggerDefinition) ReflectiveClone
                        .clone(triggerDefinition);
            }
        });
    }

}
