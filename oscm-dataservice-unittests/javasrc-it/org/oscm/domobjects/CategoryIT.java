/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: Dirk Bernsau                                                      
 *                                                                              
 *  Creation Date: 26.01.2011                                                      
 *                                                                              
 *  Completion Time: 27.01.2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.ejb.EJBException;
import javax.persistence.Query;

import org.junit.Test;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.test.ReflectiveClone;
import org.oscm.test.data.Categories;
import org.oscm.test.data.Marketplaces;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.PlatformUsers;
import org.oscm.test.data.Products;

/**
 * Tests for the domain object category.
 * 
 * @author Mani Afschar
 * 
 */
public class CategoryIT extends DomainObjectTestBase {

    private List<DomainObjectWithVersioning<?>> domObjects = new ArrayList<DomainObjectWithVersioning<?>>();

    /**
     * <b>Test case:</b> Add a new catalog entry<br>
     * <b>ExpectedResult:</b>
     * <ul>
     * <li>The catalog entry can be retrieved from DB and is identical to the
     * provided object</li>
     * <li>A history object is created for the catalog entry</li>
     * <li>The history object is referencing the correct product and
     * organization</li>
     * </ul>
     * 
     * @throws Exception
     */
    @Test
    public void testAdd() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                doTestAdd();
                return null;
            }
        });
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                doTestAddCheck();
                return null;
            }
        });
    }

    private void doTestAdd() throws Exception {
        Organization org = Organizations.createOrganization(mgr,
                OrganizationRoleType.MARKETPLACE_OWNER);
        assertNotNull("organisation expected", org);

        Marketplace m = Marketplaces.createGlobalMarketplace(org, "test_xyz",
                mgr);
        assertNotNull("marketplace expected", m);

        Category c = new Category();
        c.setCategoryId("category 1234");
        c.setMarketplace(m);

        mgr.persist(c);
        domObjects.add(c);

    }

    private void doTestAddCheck() {

        Category oldEntry = (Category) domObjects.get(0);
        assertNotNull("Old Category expected", oldEntry);
        Category entry = mgr.find(Category.class, oldEntry.getKey());
        assertNotNull("Category expected", entry);

        assertEquals(oldEntry.getCategoryId(), entry.getCategoryId());
        assertEquals(oldEntry.getMarketplace().getKey(), entry.getMarketplace()
                .getKey());
    }

    @Test
    public void testModify() throws Throwable {
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    doTestAdd();
                    return null;
                }
            });
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    doTestModify();
                    return null;
                }
            });
            runTX(new Callable<Void>() {
                @Override
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
        Category oldEntry = (Category) domObjects.get(0);
        assertNotNull("Old Category expected", oldEntry);
        Category entry = mgr.find(Category.class, oldEntry.getKey());
        assertNotNull("Category expected", entry);
        entry.setCategoryId("1234567890#new#");
        mgr.persist(entry);
        domObjects.clear();
        domObjects.add((Category) ReflectiveClone.clone(entry));
    }

    private void doTestModifyCheck() {
        Category oldEntry = (Category) domObjects.get(0);
        assertNotNull("Old Category expected", oldEntry);
        Category entry = mgr.find(Category.class, oldEntry.getKey());
        assertNotNull("Category expected", entry);
        assertEquals("Wrong position value", oldEntry.getCategoryId(),
                entry.getCategoryId());
    }

    @Test
    public void testDelete() throws Throwable {
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    doTestAdd();
                    return null;
                }
            });
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    doTestDelete();
                    return null;
                }
            });
            runTX(new Callable<Void>() {
                @Override
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
        Category oldEntry = (Category) domObjects.get(0);
        assertNotNull("Old Category expected", oldEntry);
        Category entry = mgr.find(Category.class, oldEntry.getKey());
        assertNotNull("Category expected", entry);
        domObjects.clear();
        domObjects.add((Category) ReflectiveClone.clone(entry));
        mgr.remove(entry);
    }

    private void doTestDeleteCheck() {
        Category oldEntry = (Category) domObjects.get(0);
        assertNotNull("Old Category expected", oldEntry);
        try {
            mgr.getReference(Category.class, oldEntry.getKey());
            throw new IllegalStateException("deleted category "
                    + oldEntry.getKey() + " still exists!");
        } catch (ObjectNotFoundException ex) {
            // expected
        }

    }

    @Test
    public void testQueryForAdminsThatMustBeNotified() throws Throwable {

        /**
         * The following test data is created:<br>
         * - Organization with one admin and a normal user<br>
         * - Product<br>
         * - One category assigned to the product
         */
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Organization org = Organizations.createOrganization(mgr,
                        OrganizationRoleType.MARKETPLACE_OWNER);
                PlatformUsers.createAdmin(mgr, "admin1", org);
                PlatformUsers.createUser(mgr, "no-admin-user", org);
                Marketplace marketplace = Marketplaces.createGlobalMarketplace(
                        org, "test_xyz", mgr);

                Product product = Products.createProduct(
                        org.getOrganizationId(), "newProd", "newTechProd", mgr);
                Category category = Categories.create(mgr, "cat1", marketplace);
                Categories.assignToProduct(mgr, category, product, marketplace);
                domObjects.add(category);

                return null;
            }

        });

        /**
         * The query result must only contain the admin
         */
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Category cat = (Category) domObjects.get(0);
                Query query = mgr
                        .createNamedQuery("Category.findAdminsOfServices");
                query.setParameter("categoryKey", Long.valueOf(cat.getKey()));
                @SuppressWarnings("unchecked")
                List<PlatformUser> admins = query.getResultList();
                assertEquals(1, admins.size());
                assertTrue(admins.get(0).isOrganizationAdmin());
                return null;
            }

        });
    }

    @Test
    public void testQueryForAdminsThatMustBeNotified_negative()
            throws Throwable {

        /**
         * The following test data is created:<br>
         * - Organization with one admin and a normal user<br>
         * - Product<br>
         * - One category *NOT* assigned to the product
         */
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Organization org = Organizations.createOrganization(mgr,
                        OrganizationRoleType.MARKETPLACE_OWNER);
                PlatformUsers.createAdmin(mgr, "admin1", org);
                PlatformUsers.createUser(mgr, "no-admin-user", org);
                Marketplace marketplace = Marketplaces.createGlobalMarketplace(
                        org, "test_xyz", mgr);

                Products.createProduct(org.getOrganizationId(), "newProd",
                        "newTechProd", mgr);
                Category category = Categories.create(mgr, "cat1", marketplace);
                domObjects.add(category);

                return null;
            }

        });

        /**
         * The query result must be empty
         */
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Category cat = (Category) domObjects.get(0);
                Query query = mgr
                        .createNamedQuery("Category.findAdminsOfServices");
                query.setParameter("categoryKey", Long.valueOf(cat.getKey()));
                @SuppressWarnings("unchecked")
                List<PlatformUser> admins = query.getResultList();
                assertEquals(0, admins.size());
                return null;
            }

        });

    }
}
