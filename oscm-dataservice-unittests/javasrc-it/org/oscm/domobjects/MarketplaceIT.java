/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: Dirk Bernsau                                                      
 *                                                                              
 *  Creation Date: Mar 8, 2011                                                      
 *                                                                              
 *  Completion Time: Mar 8, 2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.domobjects;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.ejb.EJBException;
import javax.persistence.Query;

import org.junit.Assert;
import org.junit.Test;
import org.oscm.converter.ParameterizedTypes;
import org.oscm.domobjects.enums.ModificationType;
import org.oscm.domobjects.enums.RevenueShareModelType;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.test.ReflectiveClone;
import org.oscm.test.ReflectiveCompare;
import org.oscm.test.data.Marketplaces;
import org.oscm.test.data.Organizations;

/**
 * Tests of the marketplace-related domain objects.
 * 
 * @author Dirk Bernsau
 */
public class MarketplaceIT extends DomainObjectTestBase {

    private List<DomainObjectWithHistory<?>> domObjects = new ArrayList<>();
    private static final String ID_MP1 = "test_MP1_global";
    private static final String BRANDING_URL1 = "http://www.fujitsu.com";
    private static final String BRANDING_URL2 = "http://en.wikipedia.org";
    private static final String TRACKING_CODE1 = "<SCRIPT>alert(document.location.protocol)</SCRIPT>";
    private static final String TRACKING_CODE2 = "<SCRIPT></SCRIPT>";

    /**
     * Tests the creation of a Marketplace object and compare the persisted
     * object with the original and the history.
     * 
     * @throws Throwable
     */
    @Test
    public void testAdd() throws Throwable {
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

    protected void doTestModifyCheck() {
        Marketplace saved = new Marketplace();
        saved.setMarketplaceId(ID_MP1);
        saved = (Marketplace) mgr.find(saved);

        List<DomainHistoryObject<?>> histObjs = mgr.findHistory(saved);
        Assert.assertNotNull("History 'null' for marketplace", histObjs);
        Assert.assertTrue("History entry expected for marketplace ",
                histObjs.size() == 3);
        MarketplaceHistory hist = (MarketplaceHistory) histObjs.get(2);

        Assert.assertEquals(ModificationType.MODIFY, hist.getModtype());

        Assert.assertFalse(saved.isTaggingEnabled());
        Assert.assertFalse(saved.isReviewEnabled());
        Assert.assertFalse(saved.isCategoriesEnabled());

        Assert.assertFalse(hist.isTaggingEnabled());
        Assert.assertFalse(hist.isReviewEnabled());
        Assert.assertFalse(hist.isCategoriesEnabled());

        assertEquals(BRANDING_URL2, saved.getBrandingUrl());
        assertEquals(BRANDING_URL2, hist.getBrandingUrl());

        assertEquals(TRACKING_CODE2, saved.getTrackingCode());
        assertEquals(TRACKING_CODE2, hist.getTrackingCode());
    }

    private void doTestModify() {
        domObjects.clear();
        Marketplace saved = new Marketplace();
        saved.setMarketplaceId(ID_MP1);
        saved = (Marketplace) mgr.find(saved);
        saved.setTaggingEnabled(false);
        saved.setReviewEnabled(false);
        saved.setCategoriesEnabled(false);
        saved.setBrandingUrl(BRANDING_URL2);
        saved.setTrackingCode(TRACKING_CODE2);
        domObjects.add(saved);
    }

    private void doTestAdd() throws NonUniqueBusinessKeyException,
            ObjectNotFoundException {
        domObjects.clear();
        Organization provider = Organizations.createOrganization(mgr,
                OrganizationRoleType.TECHNOLOGY_PROVIDER);

        Marketplace mp = Marketplaces.createMarketplace(provider, ID_MP1, true,
                mgr);
        mp.setTaggingEnabled(true);
        mp.setReviewEnabled(true);
        mp.setCategoriesEnabled(true);

        mp.setTrackingCode(TRACKING_CODE1);

        mp.setBrandingUrl(BRANDING_URL1);
        createRevenueModels(mp);

        mp.setCatalogEntries(new ArrayList<CatalogEntry>());
        mgr.persist(mp);

        domObjects.add(provider);
        domObjects.add(mp);
    }

    private void doTestAddCheck() {
        Marketplace saved = new Marketplace();
        saved.setMarketplaceId(ID_MP1);
        saved = (Marketplace) mgr.find(saved);
        Marketplace original = (Marketplace) domObjects.get(1);
        Assert.assertTrue(ReflectiveCompare.showDiffs(original, saved),
                ReflectiveCompare.compare(original, saved));
        Assert.assertTrue(original.isOpen() == saved.isOpen());
        Assert.assertNotNull("Missing list of catalog entries",
                saved.getCatalogEntries());
        Assert.assertTrue("List of catalog entries is not empty", saved
                .getCatalogEntries().isEmpty());

        RevenueShareModel priceModel = saved.getPriceModel();
        RevenueShareModel brokerPriceModel = saved.getBrokerPriceModel();
        RevenueShareModel resellerPriceModel = saved.getResellerPriceModel();

        Assert.assertNotNull("Price model missing", priceModel);
        Assert.assertNotNull("Broker price model missing", brokerPriceModel);
        Assert.assertNotNull("Reseller price model missing", resellerPriceModel);

        List<DomainHistoryObject<?>> histObjs = mgr.findHistory(saved);
        Assert.assertNotNull("History 'null' for marketplace", histObjs);
        Assert.assertTrue("History entry expected for marketplace ",
                histObjs.size() == 2);
        MarketplaceHistory hist = (MarketplaceHistory) histObjs.get(0);
        Assert.assertEquals(ModificationType.ADD, hist.getModtype());
        Assert.assertEquals("Wrong modUser", "guest", hist.getModuser());
        Assert.assertEquals("Wrong objKey", original.getKey(), hist.getObjKey());
        Assert.assertEquals("Wrong organizationObjKey", domObjects.get(0)
                .getKey(), hist.getOrganizationObjKey());

        Assert.assertEquals("Price model key mismatch",
                hist.getPriceModelObjKey(), priceModel.getKey());
        Assert.assertEquals("Broker price model key mismatch",
                hist.getBrokerPriceModelObjKey(), brokerPriceModel.getKey());
        Assert.assertEquals("Reseller price model key mismatch",
                hist.getResellerPriceModelObjKey(), resellerPriceModel.getKey());

        Assert.assertTrue(saved.isTaggingEnabled());
        Assert.assertTrue(saved.isReviewEnabled());
        Assert.assertTrue(saved.isCategoriesEnabled());

        Assert.assertTrue(hist.isTaggingEnabled());
        Assert.assertTrue(hist.isReviewEnabled());
        Assert.assertTrue(hist.isCategoriesEnabled());

        assertEquals(BRANDING_URL1, saved.getBrandingUrl());
        assertEquals(BRANDING_URL1, hist.getBrandingUrl());

        assertEquals(TRACKING_CODE1, saved.getTrackingCode());
        assertEquals(TRACKING_CODE1, hist.getTrackingCode());
    }

    /**
     * <b>Test case:</b> Check that at least one marketplace is defined in the
     * system and can be retrieved using a named query.<br>
     * 
     * @throws Throwable
     */
    @Test
    public void testOneGlobalExists() throws Throwable {
        try {
            runTX(new Callable<Void>() {
                public Void call() throws Exception {
                    Marketplace marketplace = Marketplaces
                            .findOneGlobalMarketplace(mgr);
                    Assert.assertNotNull("No marketplace found", marketplace);
                    Assert.assertNotNull("No owner set",
                            marketplace.getOrganization());
                    Assert.assertNotNull("Missing mp list", marketplace
                            .getOrganization().getMarketplaces());
                    Assert.assertTrue("object relations incomplete",
                            marketplace.getOrganization().getMarketplaces()
                                    .size() > 0);
                    return null;
                }
            });
        } catch (EJBException e) {
            throw e.getCause();
        }
    }

    @Test
    public void testGetGlobalByOwner() throws Throwable {
        try {
            runTX(new Callable<Void>() {
                public Void call() throws Exception {
                    Marketplace marketplace = Marketplaces
                            .findOneGlobalMarketplace(mgr);
                    Assert.assertNotNull("No marketplace found", marketplace);
                    Assert.assertNotNull("No owner set",
                            marketplace.getOrganization());

                    Query query = mgr
                            .createNamedQuery("Marketplace.getByOwner");
                    String oId = marketplace.getOrganization()
                            .getOrganizationId();
                    query.setParameter("organizationId", oId);
                    List<Marketplace> result = ParameterizedTypes.list(
                            query.getResultList(), Marketplace.class);
                    Assert.assertNotNull("No marketplace found", result);
                    Assert.assertTrue("No marketplace found", result.size() > 0);
                    for (Marketplace mp : result) {
                        Assert.assertNotNull("No owner set",
                                mp.getOrganization());
                        Assert.assertEquals("Wrong owner - ", oId, mp
                                .getOrganization().getOrganizationId());
                    }
                    return null;
                }
            });
        } catch (EJBException e) {
            throw e.getCause();
        }
    }

    @Test
    public void testDelete() throws Throwable {
        try {
            runTX(new Callable<Void>() {
                public Void call() throws Exception {
                    doTestDeletePrepare();
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

    private void doTestDeletePrepare() throws NonUniqueBusinessKeyException,
            ObjectNotFoundException {

        domObjects.clear();
        Organization supplier = Organizations.createOrganization(mgr,
                OrganizationRoleType.SUPPLIER);

        Marketplace mp = Marketplaces.ensureMarketplace(supplier,
                supplier.getOrganizationId(), mgr);
        mp.setPriceModel(unproxyEntity(mp.getPriceModel()));
        mp.setBrokerPriceModel(unproxyEntity(mp.getBrokerPriceModel()));
        mp.setResellerPriceModel(unproxyEntity(mp.getResellerPriceModel()));
        domObjects.add((Marketplace) ReflectiveClone.clone(mp));

        CatalogEntry entry = new CatalogEntry();
        entry.setMarketplace(mp);
        mgr.persist(entry);
        domObjects.add((CatalogEntry) ReflectiveClone.clone(entry));
    }

    private void doTestDelete() {
        Marketplace oldMP = (Marketplace) domObjects.get(0);
        assertNotNull("Old Marketplace expected", oldMP);
        Marketplace mp = mgr.find(Marketplace.class, oldMP.getKey());
        assertNotNull("Marketplace expected", mp);
        mp.getOrganization().getMarketplaces().remove(mp);
        for (CatalogEntry e : mp.getCatalogEntries()) {
            e.setMarketplace(null);
        }
        mgr.remove(mp);
    }

    private void doTestDeleteCheck() {
        CatalogEntry oldEntry = (CatalogEntry) domObjects.get(1);
        assertNotNull("Old CatalogEntry expected", oldEntry);
        CatalogEntry entry = mgr.find(CatalogEntry.class, oldEntry.getKey());
        assertNotNull("CatalogEntry expected", entry);
        assertNull("No marketplace expected", entry.getMarketplace());
        List<DomainHistoryObject<?>> histObjs = mgr.findHistory(oldEntry);
        Assert.assertNotNull("History entry 'null' for catalog entry", histObjs);
        Assert.assertFalse("History entry empty for catalog entry",
                histObjs.isEmpty());
        Assert.assertTrue("Two history entry expected for catalog entry",
                histObjs.size() == 2);
        DomainHistoryObject<?> hist = histObjs.get(0);
        Assert.assertEquals(ModificationType.ADD, hist.getModtype());
        Assert.assertEquals("modUser", "guest", hist.getModuser());

        CatalogEntryHistory ceHist = (CatalogEntryHistory) histObjs.get(1);
        Assert.assertEquals(ModificationType.MODIFY, ceHist.getModtype());
        Assert.assertEquals("modUser", "guest", ceHist.getModuser());
        Assert.assertEquals(null, ceHist.getMarketplaceObjKey());

        // Check deletion of revenue share models
        // (CascadeType.REMOVE, should also be deleted)
        Marketplace oldMP = (Marketplace) domObjects.get(0);
        RevenueShareModel deletedPriceModel = mgr.find(RevenueShareModel.class,
                oldMP.getPriceModel().getKey());

        RevenueShareModel deletedBrokerPriceModel = mgr.find(
                RevenueShareModel.class, oldMP.getBrokerPriceModel().getKey());

        RevenueShareModel deletedResellerPriceModel = mgr
                .find(RevenueShareModel.class, oldMP.getResellerPriceModel()
                        .getKey());
        Assert.assertNull("PriceModel not deleted", deletedPriceModel);
        Assert.assertNull("PriceModel not deleted", deletedBrokerPriceModel);
        Assert.assertNull("PriceModel not deleted", deletedResellerPriceModel);

        // Check that the revenue share model keys still exist in the
        // marketplace history
        // table.
        List<DomainHistoryObject<?>> mpHistObjs = mgr.findHistory(oldMP);
        Assert.assertNotNull("History 'null' for marketplace", mpHistObjs);
        Assert.assertTrue("History entry expected for marketplace ",
                histObjs.size() == 2);
        MarketplaceHistory mpHist = (MarketplaceHistory) mpHistObjs.get(1);

        Assert.assertEquals(ModificationType.DELETE, mpHist.getModtype());
        Assert.assertEquals("Price model key mismatch",
                mpHist.getPriceModelObjKey(), oldMP.getPriceModel().getKey());
        Assert.assertEquals("Broker price model key mismatch", mpHist
                .getBrokerPriceModelObjKey(), oldMP.getBrokerPriceModel()
                .getKey());
        Assert.assertEquals("Reseller price model key mismatch", mpHist
                .getResellerPriceModelObjKey(), oldMP.getResellerPriceModel()
                .getKey());

    }

    @Override
    protected void dataSetup() throws Exception {
        super.dataSetup();
        createOrganizationRoles(mgr);
        Organization operator = Organizations.createOrganization(mgr,
                OrganizationRoleType.PLATFORM_OPERATOR);
        Marketplaces.createGlobalMarketplace(operator, GLOBAL_MARKETPLACE_NAME,
                mgr);
    }

    private static RevenueShareModel createRevenueModel(
            RevenueShareModelType type) {
        RevenueShareModel revenueShareModel = new RevenueShareModel();
        revenueShareModel.setRevenueShare(BigDecimal.ZERO);
        revenueShareModel.setRevenueShareModelType(type);
        return revenueShareModel;
    }

    private void createRevenueModels(Marketplace mp)
            throws NonUniqueBusinessKeyException {
        RevenueShareModel priceModel = createRevenueModel(RevenueShareModelType.MARKETPLACE_REVENUE_SHARE);
        RevenueShareModel brokerPriceModel = createRevenueModel(RevenueShareModelType.BROKER_REVENUE_SHARE);
        RevenueShareModel resellerPriceModel = createRevenueModel(RevenueShareModelType.RESELLER_REVENUE_SHARE);
        mgr.persist(priceModel);
        mgr.persist(brokerPriceModel);
        mgr.persist(resellerPriceModel);

        mp.setResellerPriceModel(priceModel);
        mp.setBrokerPriceModel(brokerPriceModel);
        mp.setResellerPriceModel(resellerPriceModel);
    }

}
