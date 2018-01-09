/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: Zou Yang                                                     
 *                                                                              
 *  Creation Date: 12.06.2012                                                      
 *                                                                              
 *  Completion Time: 12.06.2012                                               
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

import org.oscm.domobjects.enums.ModificationType;
import org.oscm.test.ReflectiveClone;
import org.oscm.test.ReflectiveCompare;
import org.oscm.test.data.Organizations;
import org.oscm.types.enumtypes.UdaTargetType;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.UdaConfigurationType;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;

/**
 * 
 * Test of the UdaDefinition domain object.
 * 
 * @author Zou Yang
 * 
 */
public class UdaDefinitionIT extends DomainObjectTestBase {

    private List<DomainObjectWithVersioning<?>> domObjects = new ArrayList<DomainObjectWithVersioning<?>>();
    long key;

    private static final String ID_UDADEF = "test_UDADEF";

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

    private void doTestAdd() throws NonUniqueBusinessKeyException,
            ObjectNotFoundException {
        // Enter new UdaDefinition
        domObjects.clear();

        Organization provider = Organizations.createOrganization(mgr,
                OrganizationRoleType.TECHNOLOGY_PROVIDER);

        UdaDefinition def = new UdaDefinition();
        def.setOrganization(provider);
        def.setDefaultValue("defaultValue");
        def.setTargetType(UdaTargetType.CUSTOMER);
        def.setConfigurationType(UdaConfigurationType.SUPPLIER);
        def.setUdaId(ID_UDADEF);

        mgr.persist(def);
        domObjects.add((UdaDefinition) ReflectiveClone.clone(def));

        key = def.getKey();
    }

    private void doTestAddCheck() {
        UdaDefinition saved = mgr.find(UdaDefinition.class, key);
        UdaDefinition original = (UdaDefinition) domObjects.get(0);
        assertTrue(ReflectiveCompare.showDiffs(original, saved),
                ReflectiveCompare.compare(original, saved));

        // Load history objects and check them
        List<DomainHistoryObject<?>> histObjs = mgr.findHistory(saved);
        assertNotNull("History entry 'null' for UdaDefinition "
                + original.getUdaId());
        assertEquals("Only one history entry expected for UdaDefinition "
                + original.getUdaId(), 1, histObjs.size());
        UdaDefinitionHistory hist = (UdaDefinitionHistory) histObjs.get(0);
        assertEquals(ModificationType.ADD, hist.getModtype());
        assertEquals("Wrong modUser", "guest", hist.getModuser());
        assertEquals("Wrong objKey", original.getKey(), hist.getObjKey());
        assertEquals("Wrong organizationObjKey", original.getOrganizationKey(),
                hist.getOrganizationObjKey());

        // Make sure values are correct
        assertEquals(ID_UDADEF, saved.getUdaId());
        assertEquals(UdaTargetType.CUSTOMER, saved.getTargetType());
        assertEquals(UdaConfigurationType.SUPPLIER,
                saved.getConfigurationType());
        assertEquals("defaultValue", saved.getDefaultValue());
    }

    private void doTestModify() {
        domObjects.clear();
        UdaDefinition saved = new UdaDefinition();
        saved = mgr.find(UdaDefinition.class, key);
        saved.setDefaultValue("defaultValue_new");
        saved.setTargetType(UdaTargetType.CUSTOMER_SUBSCRIPTION);
        saved.setConfigurationType(UdaConfigurationType.USER_OPTION_MANDATORY);
        domObjects.add(saved);
    }

    private void doTestModifyCheck() {
        UdaDefinition saved = mgr.find(UdaDefinition.class, key);
        UdaDefinition original = (UdaDefinition) domObjects.get(0);
        assertFalse(ReflectiveCompare.showDiffs(original, saved),
                ReflectiveCompare.compare(original, saved));

        // Load history objects and check them
        List<DomainHistoryObject<?>> histObjs = mgr.findHistory(saved);
        assertNotNull("History entry 'null' for UdaDefinition "
                + original.getUdaId());
        assertEquals("Only one history entry expected for UdaDefinition "
                + original.getUdaId(), 2, histObjs.size());
        UdaDefinitionHistory hist = (UdaDefinitionHistory) histObjs.get(1);
        assertEquals(ModificationType.MODIFY, hist.getModtype());
        assertEquals("Wrong modUser", "guest", hist.getModuser());
        assertEquals("Wrong objKey", original.getKey(), hist.getObjKey());
        assertEquals("Wrong organizationObjKey", original.getOrganizationKey(),
                hist.getOrganizationObjKey());

        // Make sure values are correct
        assertEquals(ID_UDADEF, saved.getUdaId());
        assertEquals(UdaTargetType.CUSTOMER, saved.getTargetType());
        assertEquals(UdaConfigurationType.USER_OPTION_MANDATORY,
                saved.getConfigurationType());
        assertEquals("defaultValue_new", saved.getDefaultValue());
    }

    private void doTestDelete() {
        domObjects.clear();
        UdaDefinition saved = mgr.find(UdaDefinition.class, key);
        mgr.remove(saved);
        domObjects.add(saved);
    }

    private void doTestDeleteCheck() {
        UdaDefinition saved = mgr.find(UdaDefinition.class, key);
        UdaDefinition original = (UdaDefinition) domObjects.get(0);

        // Load history objects and check them
        List<DomainHistoryObject<?>> histObjs = mgr.findHistory(original);
        assertNotNull("History entry 'null' for UdaDefinition "
                + original.getUdaId());
        assertEquals("Only one history entry expected for UdaDefinition "
                + original.getUdaId(), 2, histObjs.size());
        UdaDefinitionHistory hist = (UdaDefinitionHistory) histObjs.get(1);
        assertEquals(ModificationType.DELETE, hist.getModtype());
        assertEquals("Wrong modUser", "guest", hist.getModuser());
        assertEquals("Wrong objKey", original.getKey(), hist.getObjKey());
        assertEquals("Wrong organizationObjKey", original.getOrganizationKey(),
                hist.getOrganizationObjKey());

        // Make sure values are correct
        assertNull("No UdaDefinition expected", saved);
    }
}
