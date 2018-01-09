/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: Feb 15, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.junit.Assert;

import org.junit.Test;

import org.oscm.domobjects.enums.ModificationType;
import org.oscm.test.ReflectiveClone;
import org.oscm.types.enumtypes.TriggerProcessIdentifierName;
import org.oscm.internal.types.enumtypes.TriggerType;

/**
 * Tests for the TriggerProcessIdentifier domain object.
 * 
 * @author barzu
 */
public class TriggerProcessIdentifierIT extends DomainObjectTestBase {

    private List<TriggerProcessIdentifier> objList = new ArrayList<TriggerProcessIdentifier>();

    private void verify(ModificationType modType) throws Exception {
        verify(modType, objList, TriggerProcessIdentifier.class);
    }

    @Test
    public void testAdd() throws Exception {
        final TriggerProcess clone = createTriggerProcessWithParamIdentifiers();
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                TriggerProcess triggerProcess = mgr.getReference(
                        TriggerProcess.class, clone.getKey());
                TriggerProcessIdentifier identifier = triggerProcess
                        .getTriggerProcessIdentifiers().get(0);
                Assert.assertEquals(TEST_MAIL_ADDRESS, identifier.getValue());
                return null;
            }
        });
        verify(ModificationType.ADD);
    }

    @Test
    public void testModify() throws Exception {
        final TriggerProcess clone = createTriggerProcessWithParamIdentifiers();
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                TriggerProcess triggerProcess = mgr.getReference(
                        TriggerProcess.class, clone.getKey());

                TriggerProcessIdentifier identifier = triggerProcess
                        .getTriggerProcessIdentifiers().get(0);
                identifier
                        .setName(TriggerProcessIdentifierName.USER_TO_ADD);
                identifier.setValue("user1");
                objList.remove(0);
                objList.add((TriggerProcessIdentifier) ReflectiveClone
                        .clone(identifier));

                Assert.assertEquals(clone.getKey(), identifier
                        .getTriggerProcess().getKey());
                return null;
            }
        });
        verify(ModificationType.MODIFY);
    }

    @Test
    public void testDelete() throws Exception {
        createTriggerProcessWithParamIdentifiers();
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                mgr.remove(mgr.getReference(
                        TriggerProcessIdentifier.class, objList.get(0)
                                .getKey()));
                return null;
            }
        });
        verify(ModificationType.DELETE);
    }

    private TriggerProcess createTriggerProcessWithParamIdentifiers()
            throws Exception {
        return runTX(new Callable<TriggerProcess>() {
            public TriggerProcess call() throws Exception {
                TriggerProcess triggerProcess = TriggerProcessIT
                        .createTriggerProcess(mgr,
                                TriggerType.REGISTER_CUSTOMER_FOR_SUPPLIER,
                                "http://localhost", true);

                TriggerProcessIdentifier identifier = triggerProcess
                        .addTriggerProcessIdentifier(
                                TriggerProcessIdentifierName.USER_EMAIL,
                                TEST_MAIL_ADDRESS);

                mgr.flush();
                objList.add((TriggerProcessIdentifier) ReflectiveClone
                        .clone(identifier));

                return (TriggerProcess) ReflectiveClone.clone(triggerProcess);
            }
        });
    }
}
