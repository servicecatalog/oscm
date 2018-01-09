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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.ejb.EJBException;
import javax.persistence.Query;

import org.junit.Assert;
import org.junit.Test;

import org.oscm.domobjects.enums.ModificationType;
import org.oscm.domobjects.enums.RevenueShareModelType;
import org.oscm.test.ReflectiveClone;
import org.oscm.test.data.Marketplaces;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.Products;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;

/**
 * Tests for the domain object representing catalog entries.
 * 
 * @author Dirk Bernsau
 * 
 */
public class CatalogEntryIT extends DomainObjectTestBase {

    private List<DomainObjectWithHistory<?>> domObjects = new ArrayList<DomainObjectWithHistory<?>>();

    private Marketplace mp;

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

        Product p = Products.createProduct("supId", "prodId", "techProd", mgr);
        mp = Marketplaces.ensureMarketplace(p.getVendor(), null, mgr);
        assertNotNull("Local marketplace expected", mp);

        CatalogEntry ce = new CatalogEntry();
        ce.setProduct(p);
        ce.setMarketplace(mp);
        ce.setAnonymousVisible(true);

        createRevenueModels(ce);

        mgr.persist(ce);

        domObjects.clear();
        domObjects.add((CatalogEntry) ReflectiveClone.clone(ce));
        domObjects.add((Product) ReflectiveClone.clone(p));
        domObjects.add((Organization) ReflectiveClone.clone(p.getVendor()));
    }

    private void doTestAddCheck() {

        CatalogEntry oldEntry = (CatalogEntry) domObjects.get(0);
        assertNotNull("Old CatalogEntry expected", oldEntry);
        CatalogEntry entry = mgr.find(CatalogEntry.class, oldEntry.getKey());
        assertNotNull("CatalogEntry expected", entry);
        assertNotNull("toStringAttributes() does not return any value",
                entry.toStringAttributes());
        assertTrue(entry.isAnonymousVisible());

        List<DomainHistoryObject<?>> histObjs = mgr.findHistory(entry);
        Assert.assertNotNull("History entry 'null' for catalog entry", histObjs);
        Assert.assertFalse("History entry empty for catalog entry",
                histObjs.isEmpty());
        Assert.assertTrue("One history entry expected for catalog entry",
                histObjs.size() == 1);
        DomainHistoryObject<?> hist = histObjs.get(0);
        Assert.assertEquals(ModificationType.ADD, hist.getModtype());
        Assert.assertEquals("modUser", "guest", hist.getModuser());

        CatalogEntryHistory entryHistory = (CatalogEntryHistory) hist;

        Product productReloaded = entry.getProduct();
        Assert.assertNotNull("Reloaded product missing", productReloaded);
        Product oldProduct = (Product) domObjects.get(1);
        Assert.assertNotNull("Old product missing", oldProduct);

        Assert.assertEquals("Product key mismatch ",
                Long.valueOf(oldProduct.getKey()),
                entryHistory.getProductObjKey());

        Marketplace mpReloaded = entry.getMarketplace();
        Assert.assertNotNull("Reloaded marketplace missing", mpReloaded);
        Marketplace oldMp = mp;
        Assert.assertNotNull("Old marketplace missing", oldMp);
        Assert.assertEquals("Marketplace key mismatch ",
                Long.valueOf(oldMp.getKey()), Long.valueOf(mpReloaded.getKey()));
        Assert.assertEquals("Marketplace key mismatch ",
                entryHistory.getMarketplaceObjKey(),
                Long.valueOf(mpReloaded.getKey()));

        RevenueShareModel brokerPriceModel = entry.getBrokerPriceModel();
        Assert.assertNotNull("Broker price model missing", brokerPriceModel);

        Assert.assertEquals("Broker price model key mismatch",
                entryHistory.getBrokerPriceModelObjKey(),
                Long.valueOf(brokerPriceModel.getKey()));

        RevenueShareModel resellerPriceModel = entry.getResellerPriceModel();
        Assert.assertNotNull("Reseller price model missing", resellerPriceModel);

        Assert.assertEquals("Reseller price model key mismatch",
                entryHistory.getResellerPriceModelObjKey(),
                Long.valueOf(resellerPriceModel.getKey()));

        RevenueShareModel operatorPriceModel = entry.getOperatorPriceModel();
        Assert.assertNotNull("Operator price model missing", operatorPriceModel);

        Assert.assertEquals("Operator price model key mismatch",
                entryHistory.getOperatorPriceModelObjKey(),
                Long.valueOf(operatorPriceModel.getKey()));
    }

    @Test
    public void testModify() throws Throwable {
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    doTestModifyPrepare();
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

    private void doTestModifyPrepare() throws NonUniqueBusinessKeyException {
        CatalogEntry entry = new CatalogEntry();
        mgr.persist(entry);
        domObjects.clear();
        domObjects.add((CatalogEntry) ReflectiveClone.clone(entry));
    }

    private void doTestModify() throws Exception {
        CatalogEntry oldEntry = (CatalogEntry) domObjects.get(0);
        assertNotNull("Old CatalogEntry expected", oldEntry);
        CatalogEntry entry = mgr.find(CatalogEntry.class, oldEntry.getKey());
        assertNotNull("CatalogEntry expected", entry);
        entry.setVisibleInCatalog(!entry.isVisibleInCatalog());
        domObjects.clear();
        domObjects.add((CatalogEntry) ReflectiveClone.clone(entry));
    }

    private void doTestModifyCheck() {
        CatalogEntry oldEntry = (CatalogEntry) domObjects.get(0);
        assertNotNull("Old CatalogEntry expected", oldEntry);
        CatalogEntry entry = mgr.find(CatalogEntry.class, oldEntry.getKey());
        assertNotNull("CatalogEntry expected", entry);

        List<DomainHistoryObject<?>> histObjs = mgr.findHistory(entry);
        Assert.assertNotNull("History entry 'null' for catalog entry", histObjs);
        Assert.assertEquals("One history entry expected for catalog entry", 2,
                histObjs.size());
        DomainHistoryObject<?> hist = histObjs.get(0);
        Assert.assertEquals(ModificationType.ADD, hist.getModtype());
        Assert.assertEquals("modUser", "guest", hist.getModuser());

        hist = histObjs.get(1);
        Assert.assertEquals(ModificationType.MODIFY, hist.getModtype());
        Assert.assertEquals("modUser", "guest", hist.getModuser());

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
        CatalogEntry entry = new CatalogEntry();
        createRevenueModels(entry);
        mgr.persist(entry);
        domObjects.clear();
        domObjects.add((CatalogEntry) ReflectiveClone.clone(entry));
    }

    private void doTestDelete() {
        CatalogEntry oldEntry = (CatalogEntry) domObjects.get(0);
        assertNotNull("Old CatalogEntry expected", oldEntry);
        CatalogEntry entry = mgr.find(CatalogEntry.class, oldEntry.getKey());
        assertNotNull("CatalogEntry expected", entry);
        domObjects.clear();
        domObjects.add((CatalogEntry) ReflectiveClone.clone(entry));
        mgr.remove(entry);
    }

    private void doTestDeleteCheck() {
        CatalogEntry oldEntry = (CatalogEntry) domObjects.get(0);
        assertNotNull("Old CatalogEntry expected", oldEntry);

        CatalogEntry entry = mgr.find(CatalogEntry.class, oldEntry.getKey());
        assertNull("CatalogEntry still available", entry);

        RevenueShareModel brokerPriceModel = mgr.find(RevenueShareModel.class,
                oldEntry.getBrokerPriceModel().getKey());
        assertNull("Broker price model still available", brokerPriceModel);

        RevenueShareModel resellerPriceModel = mgr.find(
                RevenueShareModel.class, oldEntry.getResellerPriceModel()
                        .getKey());
        assertNull("Broker price model still available", resellerPriceModel);

        RevenueShareModel operatorPriceModel = mgr.find(
                RevenueShareModel.class, oldEntry.getOperatorPriceModel()
                        .getKey());
        assertNull("Operator pricer model still available", operatorPriceModel);

        List<DomainHistoryObject<?>> histObjs = mgr.findHistory(oldEntry);
        Assert.assertNotNull("History entry 'null' for catalog entry", histObjs);
        Assert.assertFalse("History entry empty for catalog entry",
                histObjs.isEmpty());
        Assert.assertTrue("Two history entries expected for catalog entry",
                histObjs.size() == 2);
        DomainHistoryObject<?> hist = histObjs.get(0);
        Assert.assertEquals(ModificationType.ADD, hist.getModtype());
        Assert.assertEquals("modUser", "guest", hist.getModuser());

        hist = histObjs.get(1);
        Assert.assertEquals(ModificationType.DELETE, hist.getModtype());
        Assert.assertEquals("modUser", "guest", hist.getModuser());

        List<DomainHistoryObject<?>> revShmHistObjs = mgr.findHistory(oldEntry
                .getBrokerPriceModel());
        Assert.assertNotNull("History entry 'null' for broker price model ",
                revShmHistObjs);
        Assert.assertEquals(
                "Exactly 2 history entries expected for broker price model ",
                2, revShmHistObjs.size());
        Assert.assertEquals("Wrong object key in history", oldEntry
                .getBrokerPriceModel().getKey(), revShmHistObjs.get(1)
                .getObjKey());
        Assert.assertEquals(ModificationType.DELETE, revShmHistObjs.get(1)
                .getModtype());

        revShmHistObjs = mgr.findHistory(oldEntry.getResellerPriceModel());
        Assert.assertNotNull("History entry 'null' for reseller price model ",
                revShmHistObjs);
        Assert.assertEquals(
                "Exactly 2 history entries expected for reseller price model ",
                2, revShmHistObjs.size());
        Assert.assertEquals("Wrong object key in history", oldEntry
                .getResellerPriceModel().getKey(), revShmHistObjs.get(1)
                .getObjKey());
        Assert.assertEquals(ModificationType.DELETE, revShmHistObjs.get(1)
                .getModtype());

        revShmHistObjs = mgr.findHistory(oldEntry.getOperatorPriceModel());
        Assert.assertNotNull("History entry 'null' for operator price model ",
                revShmHistObjs);
        Assert.assertEquals(
                "Exactly 2 history entries expected for operator price model ",
                2, revShmHistObjs.size());
        Assert.assertEquals("Wrong object key in history", oldEntry
                .getOperatorPriceModel().getKey(), revShmHistObjs.get(1)
                .getObjKey());
        Assert.assertEquals(ModificationType.DELETE, revShmHistObjs.get(1)
                .getModtype());
    }

    /**
     * Test the correct persistence of the "visible in catalog" flag.
     */
    @Test
    public void testVisibelInCatalog() throws Throwable {
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    CatalogEntry ce = createTestCatalogEntry("sup1", "prod1.1",
                            "tech1");
                    ce = createTestCatalogEntry("sup2", "prod2.5", "tech2");
                    assertFalse(ce.isVisibleInCatalog());
                    domObjects.clear();
                    domObjects.add(ce);
                    return null;
                }
            });

            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    CatalogEntry oldEntry = (CatalogEntry) domObjects.get(0);
                    CatalogEntry entry = mgr.find(CatalogEntry.class,
                            oldEntry.getKey());
                    entry.setVisibleInCatalog(true);
                    domObjects.clear();
                    domObjects.add(entry);
                    return null;
                }
            });

            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    CatalogEntry oldEntry = (CatalogEntry) domObjects.get(0);
                    CatalogEntry entry = mgr.find(CatalogEntry.class,
                            oldEntry.getKey());

                    assertTrue(oldEntry.isVisibleInCatalog() == entry
                            .isVisibleInCatalog());

                    List<DomainHistoryObject<?>> histObjs = mgr
                            .findHistory(oldEntry);
                    Assert.assertNotNull(
                            "History entry 'null' for catalog entry", histObjs);
                    Assert.assertFalse("History entry empty for catalog entry",
                            histObjs.isEmpty());
                    Assert.assertTrue(
                            "One history entry expected for catalog entry",
                            histObjs.size() == 2);
                    DomainHistoryObject<?> hist = histObjs.get(0);
                    Assert.assertEquals(ModificationType.ADD, hist.getModtype());
                    Assert.assertEquals("modUser", "guest", hist.getModuser());
                    Assert.assertFalse("Invalid value for visibility flag",
                            ((CatalogEntryData) hist.getDataContainer())
                                    .isVisibleInCatalog());

                    hist = histObjs.get(1);
                    Assert.assertEquals(ModificationType.MODIFY,
                            hist.getModtype());
                    Assert.assertEquals("modUser", "guest", hist.getModuser());
                    Assert.assertTrue("Invalid value for visibility flag",
                            ((CatalogEntryData) hist.getDataContainer())
                                    .isVisibleInCatalog());

                    return null;
                }
            });
        } catch (EJBException e) {
            throw e.getCause();
        }
    }

    private static RevenueShareModel createRevenueModel(
            RevenueShareModelType type) {
        RevenueShareModel m = new RevenueShareModel();
        m.setRevenueShare(BigDecimal.ZERO);
        m.setRevenueShareModelType(type);
        return m;
    }

    private void createRevenueModels(CatalogEntry ce)
            throws NonUniqueBusinessKeyException {
        RevenueShareModel brokerPriceModel = createRevenueModel(RevenueShareModelType.BROKER_REVENUE_SHARE);
        RevenueShareModel resellerPriceModel = createRevenueModel(RevenueShareModelType.RESELLER_REVENUE_SHARE);
        RevenueShareModel operatorPriceModel = createRevenueModel(RevenueShareModelType.OPERATOR_REVENUE_SHARE);
        mgr.persist(brokerPriceModel);
        mgr.persist(resellerPriceModel);
        mgr.persist(operatorPriceModel);

        ce.setBrokerPriceModel(brokerPriceModel);
        ce.setResellerPriceModel(resellerPriceModel);
        ce.setOperatorPriceModel(operatorPriceModel);
    }

    private CatalogEntry createTestCatalogEntry(String supplierId,
            String productId, String techProductId)
            throws NonUniqueBusinessKeyException, ObjectNotFoundException {
        Product p = Products.createProduct(supplierId, productId,
                techProductId, mgr);
        mp = Marketplaces.ensureMarketplace(p.getVendor(),
                "" + System.currentTimeMillis(), mgr);

        CatalogEntry ce = new CatalogEntry();
        ce.setProduct(p);
        ce.setMarketplace(mp);
        mgr.persist(ce);
        return ce;
    }

    /**
     * Test the named query returning all active services (including customer
     * specific ones) that are somehow related to a marketplace.
     */
    @Test
    public void testCountActiveServices() throws Throwable {
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    domObjects.clear();
                    CatalogEntry ce = createTestCatalogEntry("sup1", "prod1.1",
                            "tech1");
                    ce.getProduct().setStatus(ServiceStatus.ACTIVE);
                    Marketplace marketplace = ce.getMarketplace();
                    ce = createTestCatalogEntry("sup1", "prod1.2", "tech1");
                    domObjects.add((CatalogEntry) ReflectiveClone.clone(ce));
                    ce = createTestCatalogEntry("sup2", "prod2.1", "tech1");
                    ce.getProduct().setStatus(ServiceStatus.ACTIVE);
                    // publish this ce on the other marketplace to be able to
                    // test the query
                    ce.setMarketplace(marketplace);
                    mgr.persist(ce);
                    return null;
                }
            });
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    Organization customer = Organizations
                            .createOrganization(mgr);
                    CatalogEntry ce = (CatalogEntry) domObjects.get(0);
                    Product p = ce.getProduct();
                    Product p1 = p.copyForCustomer(customer);
                    p1.setStatus(ServiceStatus.ACTIVE);
                    mgr.persist(p1);
                    Thread.sleep(100); // time stamp related
                    mgr.persist(p.copyForCustomer(customer));
                    domObjects.clear();
                    domObjects.add(ce.getMarketplace());
                    return null;
                }
            });
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    Marketplace mp = (Marketplace) domObjects.get(0);
                    mp.getMarketplaceId();
                    Query query = mgr
                            .createNamedQuery("CatalogEntry.countActiveServices");
                    query.setParameter("marketplace", mp);
                    Object result = query.getSingleResult();
                    long res = 0;
                    if (result instanceof Number) {
                        res = ((Number) result).intValue();
                    }
                    assertEquals("Three active services expected", 3, res);
                    query = mgr
                            .createNamedQuery("CatalogEntry.countActiveServicesByVendor");
                    query.setParameter("marketplace", mp);
                    // filter by owner of MP
                    query.setParameter("vendor", mp.getOrganization());
                    result = query.getSingleResult();
                    res = 0;
                    if (result instanceof Number) {
                        res = ((Number) result).intValue();
                    }
                    assertEquals("Two active services expected", 2, res);
                    return null;
                }
            });
        } catch (EJBException e) {
            throw e.getCause();
        }

    }
}
