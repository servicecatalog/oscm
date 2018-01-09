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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import org.junit.Test;

import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.enums.ModificationType;
import org.oscm.test.ReflectiveClone;
import org.oscm.test.data.Organizations;
import org.oscm.types.enumtypes.TriggerProcessIdentifierName;
import org.oscm.types.enumtypes.TriggerProcessParameterName;
import org.oscm.internal.types.enumtypes.TriggerProcessStatus;
import org.oscm.internal.types.enumtypes.TriggerTargetType;
import org.oscm.internal.types.enumtypes.TriggerType;

/**
 * Test of the TriggerProcess domain object.
 * 
 * @author pock
 * 
 */
public class TriggerProcessIT extends DomainObjectTestBase {

    private List<TriggerProcess> objList = new ArrayList<TriggerProcess>();

    private void verify(ModificationType modType) throws Exception {
        verify(modType, objList, TriggerProcess.class);
    }

    public static TriggerProcess createTriggerProcess(DataService mgr,
            TriggerType type, String target, boolean suspendProcess)
            throws Exception {
        Organization org = Organizations.createOrganization(mgr);
        PlatformUser user = Organizations.createUserForOrg(mgr, org, true,
                "userId");

        TriggerDefinition triggerDefinition = new TriggerDefinition();
        triggerDefinition.setOrganization(org);
        triggerDefinition.setType(type);
        triggerDefinition.setTargetType(TriggerTargetType.WEB_SERVICE);
        triggerDefinition.setTarget(target);
        triggerDefinition.setSuspendProcess(suspendProcess);
        triggerDefinition.setName("testTrigger");
        mgr.persist(triggerDefinition);

        TriggerProcess triggerProcess = new TriggerProcess();
        triggerProcess.setState(TriggerProcessStatus.INITIAL);
        triggerProcess.setTriggerDefinition(triggerDefinition);
        triggerProcess.setUser(user);
        mgr.persist(triggerProcess);

        return triggerProcess;
    }

    @Test
    public void testAdd() throws Exception {
        objList.add(createTriggerProcess());
        verify(ModificationType.ADD);
    }

    @Test
    public void testModify() throws Exception {
        objList.add(createTriggerProcess());
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                TriggerProcess triggerProcess = mgr.getReference(
                        TriggerProcess.class, objList.get(0).getKey());
                triggerProcess.setState(TriggerProcessStatus.APPROVED);
                objList.remove(0);
                objList.add((TriggerProcess) ReflectiveClone
                        .clone(triggerProcess));
                return null;
            }
        });
        verify(ModificationType.MODIFY);
    }

    @Test
    public void testDelete() throws Exception {
        objList.add(createTriggerProcess());
        runTX(new Callable<Void>() {
            public Void call() throws Exception {
                mgr.remove(mgr.getReference(TriggerProcess.class, objList
                        .get(0).getKey()));
                return null;
            }
        });
        verify(ModificationType.DELETE);
    }

    private TriggerProcess createTriggerProcess() throws Exception {
        return runTX(new Callable<TriggerProcess>() {
            public TriggerProcess call() throws Exception {
                TriggerProcess triggerProcess = createTriggerProcess(mgr,
                        TriggerType.ACTIVATE_SERVICE, "http://localhost", true);
                return (TriggerProcess) ReflectiveClone.clone(triggerProcess);
            }
        });
    }

    @Test
    public void testGetModificationDataForAttributeNameNoHit() throws Exception {
        TriggerProcess tp = runTX(new Callable<TriggerProcess>() {
            public TriggerProcess call() throws Exception {
                return createTriggerProcess();
            }
        });
        TriggerProcessParameter result = tp
                .getParamValueForName(TriggerProcessParameterName.OBJECT_ID);
        assertNull(result);
    }

    @Test
    public void testGetParamValueForName() throws Exception {
        TriggerProcess tp = runTX(new Callable<TriggerProcess>() {
            public TriggerProcess call() throws Exception {
                TriggerProcess tp = createTriggerProcess();
                tp.addTriggerProcessParameter(
                        TriggerProcessParameterName.OBJECT_ID, "storedValue");
                TriggerProcessParameter param = new TriggerProcessParameter();
                param.setTriggerProcess(tp);
                param.setName(TriggerProcessParameterName.OBJECT_ID);
                param.setValue("storedValue");
                tp.setTriggerProcessParameters(Collections.singletonList(param));
                return tp;
            }
        });
        TriggerProcessParameter result = tp
                .getParamValueForName(TriggerProcessParameterName.OBJECT_ID);
        assertNotNull(result);
        assertEquals("storedValue", result.getValue(String.class));
    }

    @Test
    public void testSetTriggerProcessIdentifiers() throws Exception {
        TriggerProcess tp = new TriggerProcess();
        TriggerProcessIdentifier identifier = new TriggerProcessIdentifier();
        identifier.setName(TriggerProcessIdentifierName.USER_EMAIL);
        identifier.setValue(TEST_MAIL_ADDRESS);
        tp.setTriggerProcessIdentifiers(Collections.singletonList(identifier));

        List<TriggerProcessIdentifier> identifiers = tp
                .getTriggerProcessIdentifiers();
        assertEquals(1, identifiers.size());
        assertEquals(TriggerProcessIdentifierName.USER_EMAIL, identifiers
                .get(0).getName());
        assertEquals(TEST_MAIL_ADDRESS, identifiers.get(0).getValue());
        assertEquals(tp, identifiers.get(0).getTriggerProcess());
    }

    @Test
    public void testAddTriggerProcessIdentifier() throws Exception {
        TriggerProcess tp = new TriggerProcess();
        tp.addTriggerProcessIdentifier(TriggerProcessIdentifierName.USER_EMAIL,
                TEST_MAIL_ADDRESS);

        List<TriggerProcessIdentifier> identifiers = tp
                .getTriggerProcessIdentifiers();
        assertEquals(1, identifiers.size());
        assertEquals(TriggerProcessIdentifierName.USER_EMAIL, identifiers
                .get(0).getName());
        assertEquals(TEST_MAIL_ADDRESS, identifiers.get(0).getValue());
        assertEquals(tp, identifiers.get(0).getTriggerProcess());
    }

    @Test
    public void testGetParamIdentifierValuesForName() throws Exception {
        TriggerProcess tp = new TriggerProcess();
        tp.addTriggerProcessIdentifier(TriggerProcessIdentifierName.USER_EMAIL,
                TEST_MAIL_ADDRESS);
        tp.addTriggerProcessIdentifier(TriggerProcessIdentifierName.USER_EMAIL,
                TEST_MAIL_ADDRESS + ".com");

        List<TriggerProcessIdentifier> identifiers = tp
                .getIdentifierValuesForName(TriggerProcessIdentifierName.USER_EMAIL);
        assertNotNull(identifiers);
        assertEquals(2, identifiers.size());
        assertEquals(TEST_MAIL_ADDRESS, identifiers.get(0).getValue());
        assertEquals(TEST_MAIL_ADDRESS + ".com", identifiers.get(1).getValue());
    }

}
