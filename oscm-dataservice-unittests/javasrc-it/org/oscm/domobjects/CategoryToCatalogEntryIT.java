/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                         
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import javax.ejb.EJBException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.test.data.Marketplaces;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.Products;

/**
 * Tests for the domain object relationship category to catalogentry
 * 
 */
public class CategoryToCatalogEntryIT extends DomainObjectTestBase {

    /**
     * <b>Test case:</b> Add a new category to catalog entry<br>
     * <b>ExpectedResult:</b>
     * <ul>
     * <li>The dependant object can be retrieved from DB via catalogEntry and
     * via Category</li>
     * <li>Cascading delete works for deletion of category</li>
     * <li>Cascading delete works for deletion of catalogEntry</li>
     * <li>The history object is created when object is deleted</li>
     * </ul>
     * 
     * @throws Exception
     */

    Organization org;
    Category category;
    CatalogEntry ce;
    CatalogEntry oldEntry;
    CategoryToCatalogEntry oldCategoryToCatalogEntry;

    @Before
    public void setupData() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                setupCategory();
                setupCatalogEntry();
                return null;
            }
        });
    }

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

    private void setupCategory() throws NonUniqueBusinessKeyException,
            ObjectNotFoundException {

        org = Organizations.createOrganization(mgr,
                OrganizationRoleType.MARKETPLACE_OWNER);
        assertNotNull("organisation expected", org);

        Marketplace mp = Marketplaces.createGlobalMarketplace(org, "test_xyz",
                mgr);
        assertNotNull("marketplace expected", mp);

        category = new Category();
        category.setCategoryId("category 1234");
        category.setMarketplace(mp);

        mgr.persist(category);
    }

    private void setupCatalogEntry() throws NonUniqueBusinessKeyException,
            ObjectNotFoundException {
        Product p = Products.createProduct("supId", "prodId", "techProd", mgr);
        Marketplace mp = Marketplaces.ensureMarketplace(p.getVendor(), null,
                mgr);
        assertNotNull("Local marketplace expected", mp);

        ce = new CatalogEntry();
        ce.setProduct(p);
        ce.setMarketplace(mp);
        ce.setAnonymousVisible(true);

        mgr.persist(ce);

    }

    private void doTestAdd() throws Exception {

        category = mgr.find(Category.class, category.getKey());
        ce = mgr.find(CatalogEntry.class, ce.getKey());

        CategoryToCatalogEntry cce = new CategoryToCatalogEntry();
        cce.setCategory(category);
        cce.setCatalogEntry(ce);
        List<CategoryToCatalogEntry> cceList = Arrays.asList(cce);
        ce.setCategoryToCatalogEntry(cceList);

        mgr.persist(ce);
    }

    private void doTestAddCheck() {

        CatalogEntry entry = mgr.find(CatalogEntry.class, ce.getKey());
        assertNotNull("CatalogEntry expected", entry);

        assertNotNull(entry.getCategoryToCatalogEntry());
        List<CategoryToCatalogEntry> cceList = entry
                .getCategoryToCatalogEntry();
        Assert.assertEquals(1, cceList.size());

        CategoryToCatalogEntry cce = cceList.get(0);
        Assert.assertEquals(category, cce.getCategory());
        Assert.assertEquals(ce, cce.getCatalogEntry());

    }

    @Test
    public void testDelete() throws Throwable {
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    doTestDeletePrepare();
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

    private void doTestDeletePrepare() throws NonUniqueBusinessKeyException {
        category = mgr.find(Category.class, category.getKey());
        ce = mgr.find(CatalogEntry.class, ce.getKey());
        CategoryToCatalogEntry cce = new CategoryToCatalogEntry();
        cce.setCategory(category);
        cce.setCatalogEntry(ce);
        List<CategoryToCatalogEntry> cceList = Arrays.asList(cce);
        ce.setCategoryToCatalogEntry(cceList);

        mgr.persist(ce);

        CatalogEntry entry = mgr.find(CatalogEntry.class, ce.getKey());
        assertNotNull("CatalogEntry expected", entry);

        assertNotNull(entry.getCategoryToCatalogEntry());
        cceList = entry.getCategoryToCatalogEntry();
        Assert.assertEquals(1, cceList.size());

        cce = cceList.get(0);
        Assert.assertEquals(category, cce.getCategory());
        Assert.assertEquals(ce, cce.getCatalogEntry());
    }

    private void doTestDelete() {
        oldEntry = mgr.find(CatalogEntry.class, ce.getKey());
        assertNotNull("Old Entry expected", oldEntry);

        List<CategoryToCatalogEntry> list = oldEntry
                .getCategoryToCatalogEntry();
        assertNotNull(list);
        Assert.assertEquals(1, list.size());

        oldCategoryToCatalogEntry = mgr.find(CategoryToCatalogEntry.class, list
                .get(0).getKey());
        assertNotNull(oldCategoryToCatalogEntry);

        mgr.remove(oldEntry);
    }

    private void doTestDeleteCheck() {
        // catalogEntry must be deleted
        CatalogEntry catalogEntry = mgr.find(CatalogEntry.class,
                oldEntry.getKey());
        Assert.assertNull("CatalogEntry still available", catalogEntry);

        // check history of CatalogEntry
        List<DomainHistoryObject<?>> catalogEntryHistory = mgr
                .findHistory(oldEntry);
        Assert.assertNotNull("History entry 'null' for catalog entry",
                catalogEntryHistory);
        Assert.assertTrue("Two history entry expected for catalog entry",
                catalogEntryHistory.size() == 2);
        Assert.assertEquals(oldEntry.getKey(), catalogEntryHistory.get(0)
                .getObjKey());

        // categoryToCatalogEntry must be deleted
        CategoryToCatalogEntry categoryToCatalogEntry = mgr.find(
                CategoryToCatalogEntry.class,
                oldCategoryToCatalogEntry.getKey());
        Assert.assertNull("CategoryToCatalogEntry still available",
                categoryToCatalogEntry);

    }
}
