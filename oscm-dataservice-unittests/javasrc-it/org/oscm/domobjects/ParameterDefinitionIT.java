/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 2012-7-31                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.ejb.EJBException;

import org.junit.Test;

import org.oscm.test.ReflectiveCompare;
import org.oscm.internal.types.enumtypes.ParameterModificationType;
import org.oscm.internal.types.enumtypes.ParameterType;
import org.oscm.internal.types.enumtypes.ParameterValueType;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;

/**
 * @author ManQ
 * 
 */
public class ParameterDefinitionIT extends DomainObjectTestBase {

    private List<DomainObjectWithVersioning<?>> domObjects = new ArrayList<DomainObjectWithVersioning<?>>();
    long key;

    private static final String ID_PDDEF = "test_PDDEF";

    @Test
    public void add() throws Throwable {
        try {
            runTX(new Callable<Void>() {
                public Void call() throws Exception {
                    doTestAdd();
                    return null;
                }
            });
            runTX(new Callable<Void>() {
                public Void call() throws Exception {
                    doTestAddCheck();
                    return null;
                }
            });
        } catch (EJBException e) {
            throw e.getCause();
        }
    }

    @Test
    public void modify() throws Throwable {
        try {
            runTX(new Callable<Void>() {
                public Void call() throws Exception {
                    doTestAdd();
                    return null;
                }
            });
            runTX(new Callable<Void>() {
                public Void call() throws Exception {
                    doTestModify();
                    return null;
                }
            });
            runTX(new Callable<Void>() {
                public Void call() throws Exception {
                    doTestModifyCheck();
                    return null;
                }
            });
        } catch (EJBException e) {
            throw e.getCause();
        }
    }

    @Test
    public void delete() throws Throwable {
        try {
            runTX(new Callable<Void>() {
                public Void call() throws Exception {
                    doTestAdd();
                    return null;
                }
            });
            runTX(new Callable<Void>() {
                public Void call() throws Exception {
                    doTestDelete();
                    return null;
                }
            });
            runTX(new Callable<Void>() {
                public Void call() throws Exception {
                    doTestDeleteCheck();
                    return null;
                }
            });
        } catch (EJBException e) {
            throw e.getCause();
        }
    }

    private void doTestAdd() throws NonUniqueBusinessKeyException {
        // Enter new ParameterDefinition
        domObjects.clear();

        ParameterDefinition def = new ParameterDefinition();
        def.setParameterId(ID_PDDEF);
        def.setParameterType(ParameterType.PLATFORM_PARAMETER);
        def.setConfigurable(true);
        def.setDefaultValue("defaultValue");
        def.setModificationType(ParameterModificationType.STANDARD);
        def.setValueType(ParameterValueType.STRING);
        mgr.persist(def);
        domObjects.add(def);
        key = def.getKey();
    }

    private void doTestAddCheck() {
        ParameterDefinition saved = mgr.find(ParameterDefinition.class, key);
        ParameterDefinition original = (ParameterDefinition) domObjects.get(0);
        assertTrue(ReflectiveCompare.showDiffs(original, saved),
                ReflectiveCompare.compare(original, saved));

        // Load history objects and check them
        List<DomainHistoryObject<?>> histObjs = mgr.findHistory(saved);
        assertNotNull("History entry 'null' for ParameterDefinition "
                + original);
        assertEquals("Only one history entry expected for ParameterDefinition "
                + original.getKey(), 1, histObjs.size());
        ParameterDefinitionHistory hist = (ParameterDefinitionHistory) histObjs
                .get(0);
        assertEquals(ParameterModificationType.STANDARD,
                hist.getModificationType());
        assertEquals(ParameterType.PLATFORM_PARAMETER, hist.getParameterType());
        assertEquals(original.getDataContainer().getModificationType(), hist
                .getDataContainer().getModificationType());
        // Make sure values are correct
        assertEquals(ID_PDDEF, saved.getParameterId());
    }

    private void doTestModify() {
        ParameterDefinition saved = new ParameterDefinition();
        saved = mgr.find(ParameterDefinition.class, key);
        saved.setDefaultValue("defaultValue_new");
        saved.setModificationType(ParameterModificationType.ONE_TIME);
    }

    private void doTestModifyCheck() {
        ParameterDefinition saved = mgr.find(ParameterDefinition.class, key);
        ParameterDefinition original = (ParameterDefinition) domObjects.get(0);
        assertFalse(ReflectiveCompare.showDiffs(original, saved),
                ReflectiveCompare.compare(original, saved));

        // Load history objects and check them
        List<DomainHistoryObject<?>> histObjs = mgr.findHistory(saved);
        assertNotNull("History entry 'null' for ParameterDefinition "
                + original.getKey());
        assertEquals("Only one history entry expected for UdaDefinition "
                + original.getKey(), 2, histObjs.size());
        ParameterDefinitionHistory hist = (ParameterDefinitionHistory) histObjs
                .get(1);
        assertEquals(ParameterModificationType.ONE_TIME,
                hist.getModificationType());
        assertEquals(ParameterType.PLATFORM_PARAMETER, hist.getParameterType());
        assertFalse(original.getDataContainer().getModificationType()
                .equals(hist.getDataContainer().getModificationType()));
        // Make sure values are correct
        assertEquals(ID_PDDEF, saved.getParameterId());
        assertEquals(ParameterModificationType.ONE_TIME,
                saved.getModificationType());
        assertEquals("defaultValue_new", saved.getDefaultValue());
    }

    private void doTestDelete() {
        domObjects.clear();
        ParameterDefinition saved = mgr.find(ParameterDefinition.class, key);
        mgr.remove(saved);
        domObjects.add(saved);
    }

    private void doTestDeleteCheck() {
        ParameterDefinition saved = mgr.find(ParameterDefinition.class, key);
        ParameterDefinition original = (ParameterDefinition) domObjects.get(0);

        // Load history objects and check them
        List<DomainHistoryObject<?>> histObjs = mgr.findHistory(original);
        assertNotNull("History entry 'null' for ParameterDefinition "
                + original);
        assertEquals("Only one history entry expected for ParameterDefinition "
                + original.getKey(), 2, histObjs.size());
        ParameterDefinitionHistory hist = (ParameterDefinitionHistory) histObjs
                .get(1);
        assertEquals(ParameterModificationType.STANDARD,
                hist.getModificationType());
        assertEquals(ParameterType.PLATFORM_PARAMETER, hist.getParameterType());
        assertEquals(original.getDataContainer().getModificationType(), hist
                .getDataContainer().getModificationType());
        // Make sure values are correct
        assertNull("No ParameterDefinition expected", saved);
    }

}
