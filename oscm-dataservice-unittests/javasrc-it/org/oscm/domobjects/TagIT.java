/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: Oliver Soehnges                                                      
 *                                                                              
 *  Creation Date: 29.04.2011                                                      
 *                                                                              
 *  Completion Time: n/a                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.ejb.EJBException;

import org.junit.Test;

import org.oscm.test.ReflectiveClone;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.TechnicalProducts;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;

/**
 * Tests for the domain object representing tags.
 * 
 * @author soehnges
 * 
 */
public class TagIT extends DomainObjectTestBase {

    private List<DomainObjectWithVersioning<?>> domObjects = new ArrayList<DomainObjectWithVersioning<?>>();

    /**
     * <b>Test case:</b> Add a new tag entry<br>
     * <b>ExpectedResult:</b>
     * <ul>
     * <li>The tag entry can be retrieved from DB and is identical to the
     * provided object</li>
     * <li>A history object is created for the tag entry</li>
     * </ul>
     * 
     * @throws Exception
     */
    @Test
    public void testAdd() throws Exception {
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
    }

    private void doTestAdd() throws Exception {
        Organization supplier = Organizations.createOrganization(mgr,
                OrganizationRoleType.TECHNOLOGY_PROVIDER,
                OrganizationRoleType.SUPPLIER);
        TechnicalProduct p = TechnicalProducts.createTechnicalProduct(mgr,
                supplier, "tec_pro", false, ServiceAccessType.DIRECT);

        Tag tg = new Tag("en", "storage");
        mgr.persist(tg);

        TechnicalProductTag tpt = new TechnicalProductTag();
        tpt.setTechnicalProduct(p);
        tpt.setTag(tg);
        mgr.persist(tpt);

        domObjects.clear();
        domObjects.add((Tag) ReflectiveClone.clone(tg));
        domObjects.add((TechnicalProductTag) ReflectiveClone.clone(tpt));
        domObjects.add((TechnicalProduct) ReflectiveClone.clone(p));
    }

    private void doTestAddCheck() {
        Tag oldEntry = (Tag) domObjects.get(0);
        assertNotNull("Old Tag expected", oldEntry);

        TechnicalProductTag oldTagRef = (TechnicalProductTag) domObjects.get(1);
        assertNotNull("Old Tag Relation expected", oldTagRef);

        TechnicalProduct oldProduct = (TechnicalProduct) domObjects.get(2);
        assertNotNull("Old Product expected", oldProduct);

        Tag entry = mgr.find(Tag.class, oldEntry.getKey());
        assertNotNull("Tag expected", entry);
        assertEquals("Wrong locale", oldEntry.getLocale(), entry.getLocale());
        assertEquals("Wrong tag value", oldEntry.getValue(), entry.getValue());

        TechnicalProductTag relentry = mgr.find(TechnicalProductTag.class,
                oldTagRef.getKey());
        assertNotNull("Tag relation expected", entry);
        assertEquals("Wrong tag", oldTagRef.getTag(), relentry.getTag());
        assertEquals("Wrong tech prod", oldTagRef.getTechnicalProduct()
                .getTechnicalProductId(), relentry.getTechnicalProduct()
                .getTechnicalProductId());
    }

    @Test
    public void testModify() throws Throwable {
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

    private void doTestModify() throws NonUniqueBusinessKeyException {
        Tag oldEntry = (Tag) domObjects.get(0);
        assertNotNull("Old Tag expected", oldEntry);
        Tag entry = mgr.find(Tag.class, oldEntry.getKey());
        assertNotNull("Tag expected", entry);
        entry.setValue("huge storage");
        mgr.persist(entry);
        domObjects.clear();
        domObjects.add((Tag) ReflectiveClone.clone(entry));
    }

    private void doTestModifyCheck() {
        Tag oldEntry = (Tag) domObjects.get(0);
        assertNotNull("Old Tag expected", oldEntry);
        Tag entry = mgr.find(Tag.class, oldEntry.getKey());
        assertNotNull("Tag expected", entry);
        assertEquals("Wrong value", oldEntry.getValue(), entry.getValue());
    }

    @Test
    public void testDelete() throws Throwable {
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

    private void doTestDelete() {
        Tag oldEntry = (Tag) domObjects.get(0);
        assertNotNull("Old Tag expected", oldEntry);
        TechnicalProductTag oldTagRef = (TechnicalProductTag) domObjects.get(1);
        assertNotNull("Old Tag ref expected", oldTagRef);

        TechnicalProductTag refentry = mgr.find(TechnicalProductTag.class,
                oldTagRef.getKey());
        mgr.remove(refentry);

        Tag entry = mgr.find(Tag.class, oldEntry.getKey());
        assertNotNull("Tag expected", entry);
        domObjects.clear();
        domObjects.add((Tag) ReflectiveClone.clone(entry));
        mgr.remove(entry);
    }

    private void doTestDeleteCheck() {
        Tag oldEntry = (Tag) domObjects.get(0);
        assertNotNull("Old Tag expected", oldEntry);
        Tag entry = mgr.find(Tag.class, oldEntry.getKey());
        assertNull("Tag still available", entry);

    }

}
