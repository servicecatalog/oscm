/*******************************************************************************
 *                                                                              
7*  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: groch                                                 
 *                                                                              
 *  Creation Date: 07.03.2011                                                      
 *                                                                              
 *  Completion Time: 10.03.2011                                             
 *                                                                              
 *******************************************************************************/

package org.oscm.marketplace.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import javax.ejb.EJBException;

import org.junit.Test;

import org.oscm.domobjects.CatalogEntry;
import org.oscm.domobjects.LandingpageProduct;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.MarketplaceToOrganization;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.PublicLandingpage;
import org.oscm.domobjects.enums.PublishingAccess;
import org.oscm.test.data.Marketplaces;
import org.oscm.internal.types.exception.IllegalArgumentException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.PublishingToMarketplaceNotPermittedException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOCatalogEntry;
import org.oscm.internal.vo.VOMarketplace;
import org.oscm.internal.vo.VOService;

/**
 * Unit tests for the marketplace management. Only the method related to
 * "publish service" will be tested in this class.
 * 
 * @author groch
 * 
 */
public class MarketplaceServiceBeanPublishServiceIT extends MarketplaceServiceTestBase {

    @Test(expected = IllegalArgumentException.class)
    public void publishService_NullService() throws Exception {
        List<VOCatalogEntry> emptyList = Collections.emptyList();
        try {
            container.login(supplier1Key, ROLE_SERVICE_MANAGER);
            marketplaceService.publishService(null, emptyList);
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void publishService_NullMarketplaceIds() throws Exception {
        try {
            container.login(supplier1Key, ROLE_SERVICE_MANAGER);
            marketplaceService.publishService(new VOService(), null);
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    @Test(expected = ValidationException.class)
    public void publishService_EmptyMarketplaceIds() throws Exception {
        List<VOCatalogEntry> emptyList = Collections.emptyList();
        try {
            container.login(supplier1Key, ROLE_SERVICE_MANAGER);
            marketplaceService.publishService(new VOService(), emptyList);
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    @Test(expected = EJBException.class)
    public void publishService_TechnologyProvider() throws Exception {
        container.login(techProviderKey, ROLE_ORGANIZATION_ADMIN,
                ROLE_TECHNOLOGY_MANAGER);
        marketplaceService.publishService(new VOService(),
                Arrays.asList(new VOCatalogEntry()));
    }

    @Test(expected = ObjectNotFoundException.class)
    public void publishService_Customer() throws Exception {
        marketplaceService.publishService(new VOService(),
                Arrays.asList(new VOCatalogEntry()));
    }

    @Test
    public void publishService_visible() throws Exception {
        grantSupplier1AccessToGlobalMp();
        VOService voSvc1_3 = createUnpublishedService(supp1,
                p1_1.getTechnicalProduct(), "supp1_visibleProduct");

        VOMarketplace voMP = new VOMarketplace();
        voMP.setMarketplaceId(GLOBAL_MP_ID);

        VOCatalogEntry voCESvc1_3 = new VOCatalogEntry();
        voCESvc1_3.setMarketplace(voMP);
        voCESvc1_3.setAnonymousVisible(true);
        voCESvc1_3.setVisibleInCatalog(true);
        voCESvc1_3.setService(voSvc1_3);

        marketplaceService.publishService(voSvc1_3, Arrays.asList(voCESvc1_3));

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Product p = mgr.getReference(Product.class, p1_1.getKey());
                assertEquals(1, p.getCatalogEntries().size());
                CatalogEntry ce = p.getCatalogEntries().get(0);
                assertTrue(ce.isVisibleInCatalog());
                return null;
            }
        });
        assertNotInUpgradePath(voSvc1_3);
    }

    @Test
    public void publishService_republishSource() throws Exception {

        container.login(supplier2Key, ROLE_SERVICE_MANAGER);

        VOCatalogEntry ce = createCatalogEntry(voSvc2_1.getKey(), mpOpen);

        marketplaceService.publishService(voSvc2_1, Arrays.asList(ce));

        assertNotInUpgradePath(voSvc2_1);
        assertPartnerPriceModelsOfCatalogEntryRemoved(voSvc2_1,
                brokerRevenueShareModel2_1, resellerRevenueShareModel2_1);

    }

    @Test
    public void publishService_republishTarget() throws Exception {

        container.login(supplier2Key, ROLE_SERVICE_MANAGER);

        VOCatalogEntry ce = createCatalogEntry(voSvc2_2.getKey(), mpOpen);

        marketplaceService.publishService(voSvc2_2, Arrays.asList(ce));

        assertNotInUpgradePath(voSvc2_2);
        assertPartnerPriceModelsOfCatalogEntryRemoved(voSvc2_2,
                brokerRevenueShareModel2_2, resellerRevenueShareModel2_2);
    }

    @Test(expected = ValidationException.class)
    public void publishService_LocalAndGlobalMP() throws Exception {
        try {
            container.login(supplier1Key, ROLE_SERVICE_MANAGER);
            marketplaceService.publishService(new VOService(),
                    Arrays.asList(voCESvc1_1, voCESvc1_2));
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    @Test(expected = ValidationException.class)
    public void publishService_MultipleGlobalMPs() throws Exception {
        // currently, each service instance can only be published to exactly one
        // MP; this might change in the future
        container.login(supplier1Key, ROLE_SERVICE_MANAGER);
        final Marketplace mpglobal2 = runTX(new Callable<Marketplace>() {
            @Override
            public Marketplace call() throws Exception {
                return Marketplaces.createMarketplace(mpOwner, "GLOBAL_MP2",
                        false, mgr);
            }
        });

        VOCatalogEntry voCESvc1_3 = createCatalogEntry(p1_1.getKey(), mpglobal2);

        try {
            marketplaceService.publishService(new VOService(),
                    Arrays.asList(voCESvc1_2, voCESvc1_3));
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    @Test(expected = OperationNotPermittedException.class)
    public void publishService_LocalMP_SupplierNotOwnerOfService()
            throws Exception {
        marketplaceService.publishService(voSvc2_1, Arrays.asList(voCESvc2));
    }

    @Test
    public void publishService_LocalMP_alreadyPublishedOnGlobalMP()
            throws Exception {
        container.login(supplier1Key, ROLE_SERVICE_MANAGER);
        marketplaceService.publishService(voSvc1_2, Arrays.asList(voCESvc1_1));
        assertPartnerPriceModelsOfCatalogEntryExist(voSvc1_2);
    }

    @Test
    public void publishService_LocalMP_alreadyPublishedHere() throws Exception {
        container.login(supplier1Key, ROLE_SERVICE_MANAGER);
        marketplaceService.publishService(voSvc1_1, Arrays.asList(voCESvc1_1));
        assertPartnerPriceModelsOfCatalogEntryExist(voSvc1_1);
    }

    @Test
    public void publishService_LocalMP_initialPublishing() throws Exception {
        grantSupplier1AccessToGlobalMp();
        VOService voSvc1_3 = createUnpublishedService(supp1,
                p1_1.getTechnicalProduct(), "supp1_p3");

        VOCatalogEntry voCESvc1_3 = createCatalogEntry(voSvc1_3.getKey(),
                mpSupp1);

        marketplaceService.publishService(voSvc1_3, Arrays.asList(voCESvc1_3));
        assertNotInUpgradePath(voSvc1_3);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void publishService_GlobalMP_SupplierNotOwnerOfService()
            throws Exception {
        marketplaceService.publishService(voSvc2_1, Arrays.asList(voCESvc2));
    }

    @Test
    public void publishService_GlobalMP_alreadyPublishedOnLocalMP()
            throws Exception {
        grantSupplier1AccessToGlobalMp();
        marketplaceService.publishService(voSvc1_1, Arrays.asList(voCESvc1_2));
        assertPartnerPriceModelsOfCatalogEntryExist(voSvc1_1);
    }

    @Test(expected = PublishingToMarketplaceNotPermittedException.class)
    public void publishService_NotPermitted() throws Exception {
        container.login(supplier1Key, ROLE_SERVICE_MANAGER);
        VOCatalogEntry voCESvc1_3 = createCatalogEntry(voSvc1_1.getKey(),
                mpClosed);
        marketplaceService.publishService(voSvc1_1, Arrays.asList(voCESvc1_3));
    }

    @Test
    public void publishService_GlobalMP_alreadyPublishedHere() throws Exception {
        marketplaceService.publishService(voSvc1_2, Arrays.asList(voCESvc1_2));
        assertPartnerPriceModelsOfCatalogEntryExist(voSvc1_2);
    }

    @Test
    public void publishService_GlobalMP_initialPublishing() throws Exception {
        grantSupplier1AccessToGlobalMp();
        final VOService voSvc1_3 = createUnpublishedService(supp1,
                p1_1.getTechnicalProduct(), "supp1_p3");

        VOCatalogEntry voCESvc1_3 = createCatalogEntry(voSvc1_3.getKey(),
                mpGlobal);
        marketplaceService.publishService(voSvc1_3, Arrays.asList(voCESvc1_3));
    }

    @Test
    public void publishService_landingpageRepublish() throws Exception {
        container.login(supplier2Key, ROLE_SERVICE_MANAGER);
        // voSvc2_1/p2_1 is published on marketplace "mpGlobal" with ID
        // "GLOBAL_MP_ID"
        // add p2_1 to featured list of landingpage
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                mpGlobal = (Marketplace) mgr
                        .getReferenceByBusinessKey(mpGlobal);

                PublicLandingpage landingpage = mpGlobal.getPublicLandingpage();
                List<LandingpageProduct> featuredList = landingpage
                        .getLandingpageProducts();
                LandingpageProduct landingpageProduct = new LandingpageProduct();
                landingpageProduct.setLandingpage(landingpage);
                landingpageProduct.setPosition(1);
                landingpageProduct.setProduct(p2_1);
                featuredList.add(landingpageProduct);
                mgr.persist(mpGlobal);
                return null;
            }
        });

        // re-publish to marketplace "mpOpen" withID "OPEN_MP_ID"
        VOCatalogEntry ce = createCatalogEntry(voSvc2_1.getKey(), mpOpen);
        marketplaceService.publishService(voSvc2_1, Arrays.asList(ce));
        assertNotInUpgradePath(voSvc2_1);

        // verify that service was removed from featured list of landingpage of
        // mpGlobal
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                mpGlobal = (Marketplace) mgr
                        .getReferenceByBusinessKey(mpGlobal);
                PublicLandingpage landingpage = mpGlobal.getPublicLandingpage();
                assertNotNull(landingpage);

                // verify product must be removed
                assertEquals(0, landingpage.getLandingpageProducts().size());
                return null;
            }
        });
    }

    @Test
    public void publishService_landingpageUnpublish() throws Exception {
        container.login(supplier2Key, ROLE_SERVICE_MANAGER);

        // voSvc2_1/p2_1 is published on marketplace "mpGlobal" with ID
        // "GLOBAL_MP_ID"
        // add p2_1 to featured list of landingpage
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                mpGlobal = (Marketplace) mgr
                        .getReferenceByBusinessKey(mpGlobal);

                PublicLandingpage landingpage = mpGlobal.getPublicLandingpage();
                List<LandingpageProduct> featuredList = landingpage
                        .getLandingpageProducts();
                LandingpageProduct landingpageProduct = new LandingpageProduct();
                landingpageProduct.setLandingpage(landingpage);
                landingpageProduct.setPosition(1);
                landingpageProduct.setProduct(p2_1);
                featuredList.add(landingpageProduct);
                mgr.persist(mpGlobal);
                return null;
            }
        });

        // unpublish from marketplace "mpGlobal" with ID "GLOBAL_MP_ID"
        VOCatalogEntry ce = new VOCatalogEntry();
        ce.setMarketplace(null);
        ce.setAnonymousVisible(true);
        ce.setVisibleInCatalog(true);
        ce.setService(voSvc2_1);
        marketplaceService.publishService(voSvc2_1, Arrays.asList(ce));
        assertNotInUpgradePath(voSvc2_1);

        // verify that service was removed from featured list of landingpage
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                mpGlobal = (Marketplace) mgr
                        .getReferenceByBusinessKey(mpGlobal);
                PublicLandingpage landingpage = mpGlobal.getPublicLandingpage();
                assertNotNull(landingpage);

                // verify product must be removed
                assertEquals(0, landingpage.getLandingpageProducts().size());
                return null;
            }
        });
    }

    @Test
    public void publishService_unpublishSvcFromMarketplace_loggedInVisible_NotVisibleInCatalog()
            throws Exception {
        unpublish(false, false);
    }

    @Test
    public void publishService_unpublishSvcFromMarketplace_anonymousVisible_NotVisibleInCatalog()
            throws Exception {
        unpublish(true, false);
    }

    @Test
    public void publishService_unpublishSvcFromMarketplace_loggedInVisible_visibleInCatalog()
            throws Exception {
        unpublish(false, true);
    }

    @Test
    public void publishService_unpublishSvcFromMarketplace_anonymousVisible_visibleInCatalog()
            throws Exception {
        unpublish(true, true);
    }

    /**
     * Open marketplace test. No MarketplaceToOrganization reference in the
     * database.<br>
     * Expected:<br>
     * - publishing possible;<br>
     * - MarketplaceToOrganization reference with PUBLISHING_ACCESS_GRANTED;<br>
     * - 1 more catalog entry for the marketplace.<br>
     * 
     * @throws Exception
     */
    @Test
    public void publishService_OpenMP_SupplierPublishingNoRef()
            throws Exception {
        assertTrue(mpOpen.isOpen());

        // verify no reference exists before publishing
        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                Marketplace mp = (Marketplace) mgr.find(mpOpen);
                List<MarketplaceToOrganization> list = mp
                        .getMarketplaceToOrganizations();
                assertEquals(0, list.size());
                return null;
            }
        });

        VOService voSvc1_3 = createUnpublishedService(supp1,
                p1_1.getTechnicalProduct(), "supp1_p3");
        VOCatalogEntry voCESvc1_3 = createCatalogEntry(voSvc1_3.getKey(),
                mpOpen);

        final Integer cenum = runTX(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                mpOpen = (Marketplace) mgr.find(mpOpen);
                List<CatalogEntry> centries = mpOpen.getCatalogEntries();
                Integer cenum = Integer.valueOf(centries.size());
                return cenum;
            }
        });

        marketplaceService.publishService(voSvc1_3, Arrays.asList(voCESvc1_3));

        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                mpOpen = (Marketplace) mgr.find(mpOpen);
                List<MarketplaceToOrganization> list = mpOpen
                        .getMarketplaceToOrganizations();
                assertEquals(1, list.size());
                assertEquals(supp1.getOrganizationId(), list.get(0)
                        .getOrganization().getOrganizationId());
                assertEquals(PublishingAccess.PUBLISHING_ACCESS_GRANTED, list
                        .get(0).getPublishingAccess());
                List<CatalogEntry> centries = mpOpen.getCatalogEntries();
                assertEquals(cenum.intValue() + 1, centries.size());
                return null;
            }
        });
    }

    /**
     * Open marketplace test. 2 services published. MarketplaceToOrganization
     * reference exists after the first publish.<br>
     * Expected:<br>
     * - publishing possible;<br>
     * - Only 1 MarketplaceToOrganization reference with
     * PUBLISHING_ACCESS_GRANTED;<br>
     * - 2 more catalog entries for the marketplace.<br>
     * 
     * @throws Exception
     */
    @Test
    public void publishService_OpenMP_SupplierPublishingGranted()
            throws Exception {
        assertTrue(mpOpen.isOpen());

        // verify no reference exists before publishing
        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                Marketplace mp = (Marketplace) mgr.find(mpOpen);
                List<MarketplaceToOrganization> list = mp
                        .getMarketplaceToOrganizations();
                assertEquals(0, list.size());
                return null;
            }
        });

        VOService voSvc1_3 = createUnpublishedService(supp1,
                p1_1.getTechnicalProduct(), "supp1_p3");
        VOService voSvc1_4 = createUnpublishedService(supp1,
                p1_1.getTechnicalProduct(), "supp1_p4");

        VOCatalogEntry voCESvc1_3 = createCatalogEntry(voSvc1_3.getKey(),
                mpOpen);
        VOCatalogEntry voCESvc1_4 = createCatalogEntry(voSvc1_4.getKey(),
                mpOpen);

        final Integer cenum = runTX(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                mpOpen = (Marketplace) mgr.find(mpOpen);
                List<CatalogEntry> centries = mpOpen.getCatalogEntries();
                Integer cenum = Integer.valueOf(centries.size());
                return cenum;
            }
        });

        marketplaceService.publishService(voSvc1_3, Arrays.asList(voCESvc1_3));

        // verify reference exists after first publishing
        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                Marketplace mp = (Marketplace) mgr.find(mpOpen);
                List<MarketplaceToOrganization> list = mp
                        .getMarketplaceToOrganizations();
                assertEquals(1, list.size());
                return null;
            }
        });

        marketplaceService.publishService(voSvc1_4, Arrays.asList(voCESvc1_4));

        // verify only one ref but 2 catalog entries exist after 2nd publishing
        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                mpOpen = (Marketplace) mgr.find(mpOpen);
                List<MarketplaceToOrganization> list = mpOpen
                        .getMarketplaceToOrganizations();
                assertEquals(1, list.size());
                assertEquals(supp1.getOrganizationId(), list.get(0)
                        .getOrganization().getOrganizationId());
                assertEquals(PublishingAccess.PUBLISHING_ACCESS_GRANTED, list
                        .get(0).getPublishingAccess());
                List<CatalogEntry> centries = mpOpen.getCatalogEntries();
                assertEquals(cenum.intValue() + 2, centries.size());
                return null;
            }
        });
    }

    /**
     * Open marketplace test. Supplier banned for this marketplace.<br>
     * Expected:<br>
     * - publishing not possible;<br>
     * 
     * @throws Exception
     */
    @Test(expected = PublishingToMarketplaceNotPermittedException.class)
    public void publishService_OpenMP_SupplierPublishingBanned()
            throws Exception {
        assertTrue(mpOpen.isOpen());

        VOService voSvc = createUnpublishedService(supp1,
                p1_1.getTechnicalProduct(), "supp1_p5");
        VOCatalogEntry voCESvc = createCatalogEntry(voSvc.getKey(), mpOpen);

        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                MarketplaceToOrganization mto = new MarketplaceToOrganization(
                        mpOpen, supp1,
                        PublishingAccess.PUBLISHING_ACCESS_DENIED);
                mgr.persist(mto);
                mgr.flush();
                return null;
            }
        });

        marketplaceService.publishService(voSvc, Arrays.asList(voCESvc));
    }

    /**
     * Closed marketplace test. No MarketplaceToOrganization reference in the
     * database.<br>
     * Expected:<br>
     * - publishing not possible;<br>
     * 
     * @throws Exception
     */
    @Test(expected = PublishingToMarketplaceNotPermittedException.class)
    public void publishService_ClosedMP_SupplierPublishingNoRef()
            throws Exception {
        assertFalse(mpClosed.isOpen());

        VOService voSvc = createUnpublishedService(supp1,
                p1_1.getTechnicalProduct(), "supp1_p7");
        VOCatalogEntry voCESvc = createCatalogEntry(voSvc.getKey(), mpClosed);

        marketplaceService.publishService(voSvc, Arrays.asList(voCESvc));
    }

    /**
     * Closed marketplace test. MarketplaceToOrganization reference exists with
     * PUBLISHING_ACCESS_GRANTED (supplier already accepted).<br>
     * Expected:<br>
     * - publishing possible;<br>
     * - Only 1 MarketplaceToOrganization reference with
     * PUBLISHING_ACCESS_GRANTED;<br>
     * - 1 more catalog entry for the marketplace.<br>
     * 
     * @throws Exception
     */
    @Test
    public void publishService_ClosedMP_SupplierPublishingGranted()
            throws Exception {
        assertFalse(mpClosed.isOpen());

        VOService voSvc = createUnpublishedService(supp1,
                p1_1.getTechnicalProduct(), "supp1_p6");
        VOCatalogEntry voCESvc = createCatalogEntry(voSvc.getKey(), mpClosed);

        final Integer cenum = runTX(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                mpOpen = (Marketplace) mgr.find(mpOpen);
                List<CatalogEntry> centries = mpOpen.getCatalogEntries();
                Integer cenum = Integer.valueOf(centries.size());
                MarketplaceToOrganization mto = new MarketplaceToOrganization(
                        mpClosed, supp1,
                        PublishingAccess.PUBLISHING_ACCESS_GRANTED);
                mgr.persist(mto);
                mgr.flush();
                return cenum;
            }
        });

        marketplaceService.publishService(voSvc, Arrays.asList(voCESvc));

        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                mpClosed = (Marketplace) mgr.find(mpClosed);
                List<MarketplaceToOrganization> list = mpClosed
                        .getMarketplaceToOrganizations();
                assertEquals(1, list.size());
                assertEquals(supp1.getOrganizationId(), list.get(0)
                        .getOrganization().getOrganizationId());
                assertEquals(PublishingAccess.PUBLISHING_ACCESS_GRANTED, list
                        .get(0).getPublishingAccess());
                List<CatalogEntry> centries = mpClosed.getCatalogEntries();
                assertEquals(cenum.intValue() + 1, centries.size());
                return null;
            }
        });
    }

    /**
     * Closed marketplace test. MarketplaceToOrganization reference with
     * PUBLISHING_ACCESS_DENIED exists in the database.<br>
     * Expected:<br>
     * - publishing not possible;<br>
     * 
     * @throws Exception
     */
    @Test(expected = PublishingToMarketplaceNotPermittedException.class)
    public void publishService_ClosedMP_SupplierPublishingBanned()
            throws Exception {
        assertFalse(mpClosed.isOpen());

        VOService voSvc = createUnpublishedService(supp1,
                p1_1.getTechnicalProduct(), "supp1_p8");
        VOCatalogEntry voCESvc = createCatalogEntry(voSvc.getKey(), mpClosed);

        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                MarketplaceToOrganization mto = new MarketplaceToOrganization(
                        mpClosed, supp1,
                        PublishingAccess.PUBLISHING_ACCESS_DENIED);
                mgr.persist(mto);
                mgr.flush();
                return null;
            }
        });

        marketplaceService.publishService(voSvc, Arrays.asList(voCESvc));
    }

}
