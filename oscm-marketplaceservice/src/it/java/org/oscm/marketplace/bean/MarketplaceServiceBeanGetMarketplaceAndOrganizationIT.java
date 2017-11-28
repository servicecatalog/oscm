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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Currency;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.ejb.EJBAccessException;
import javax.ejb.EJBException;

import org.junit.Test;

import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.MarketplaceToOrganization;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.PriceModel;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.SupportedCurrency;
import org.oscm.domobjects.enums.PublishingAccess;
import org.oscm.test.data.Marketplaces;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.Products;
import org.oscm.types.enumtypes.EmailType;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.PriceModelType;
import org.oscm.internal.types.enumtypes.PricingPeriod;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.types.exception.IllegalArgumentException;
import org.oscm.internal.types.exception.MarketplaceAccessTypeUneligibleForOperationException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.vo.VOCatalogEntry;
import org.oscm.internal.vo.VOMarketplace;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.internal.vo.VOService;

/**
 * Unit tests for the marketplace management. Only the "get" method will be
 * tested in this class.
 * 
 * @author groch
 * 
 */
public class MarketplaceServiceBeanGetMarketplaceAndOrganizationIT extends
        MarketplaceServiceTestBase {

    @Test(expected = EJBException.class)
    public void getMarketplacesForOrganization_TechnologyProvider()
            throws Exception {
        container.login(techProviderKey, ROLE_ORGANIZATION_ADMIN,
                ROLE_TECHNOLOGY_MANAGER);
        marketplaceService.getMarketplacesForOrganization();
    }

    @Test(expected = EJBException.class)
    public void getMarketplacesForOrganization_Customer() throws Exception {
        container.login(mpOwnerUserKey);
        marketplaceService.getMarketplacesForOrganization();
    }

    @Test
    public void getMarketplacesForOrganization() throws Exception {
        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                Marketplace mp = mgr.getReference(Marketplace.class,
                        mpGlobal.getKey());
                Marketplaces.grantPublishing(
                        mgr.getReference(Organization.class, supp1.getKey()),
                        mp, mgr, false);
                Marketplaces.grantPublishing(
                        mgr.getReference(Organization.class, supp2.getKey()),
                        mp, mgr, false);
                return null;
            }
        });
        container.login(supplier1Key, ROLE_SERVICE_MANAGER);
        assertGetMarketplaces();

        container.login(supplier2Key, ROLE_SERVICE_MANAGER);
        assertGetMarketplaces();
    }

    @Test
    public void getMarketplacesForOrganization_InitialSupplier()
            throws Exception {
        Long suppInitialUserKey = runTX(new Callable<Long>() {

            @Override
            public Long call() throws Exception {
                Organization org = Organizations.createOrganization(mgr,
                        OrganizationRoleType.SUPPLIER);
                PlatformUser user = Organizations.createUserForOrg(mgr, org,
                        true, "testGetMarketplaces_InitialSupplier");
                mgr.flush();
                return Long.valueOf(user.getKey());
            }
        });
        container.login(suppInitialUserKey.longValue(), ROLE_SERVICE_MANAGER);
        List<VOMarketplace> list = marketplaceService
                .getMarketplacesForOrganization();
        assertNotNull(list);
        assertEquals("Created user may at least publish to open mp", 1,
                list.size());
        assertEquals("Created user may at least publish to open mp",
                OPEN_MP_ID, list.get(0).getMarketplaceId());
    }

    @Test
    public void getMarketplacesForOrganization_noPublishingRightsForClosedMp()
            throws Exception {
        container.login(supplier3Key, ROLE_SERVICE_MANAGER);

        List<VOMarketplace> list = marketplaceService
                .getMarketplacesForOrganization();

        // expected result: contains only open mp
        assertNotNull("List of marketplaces expected - ", list);
        assertEquals("One marketplace expected - ", 1, list.size());
        assertEquals("Open marketplace expected - ", OPEN_MP_ID, list.get(0)
                .getMarketplaceId());
    }

    /*
     * Use case: supp3 was banned on previously open MP, blacklist ref remains
     * after switching MP to closed => this marketplace must not show up in the
     * returned list of marketplaces
     */
    @Test
    public void getMarketplacesForOrganization_supplierHadBeenBannedonClosedMpBefore()
            throws Exception {
        container.login(supplier3Key, ROLE_SERVICE_MANAGER);

        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                MarketplaceToOrganization mto = new MarketplaceToOrganization(
                        mpClosed, supp3,
                        PublishingAccess.PUBLISHING_ACCESS_DENIED);
                mgr.persist(mto);
                mgr.flush();

                return null;
            }
        });

        List<VOMarketplace> list = marketplaceService
                .getMarketplacesForOrganization();

        // expected result: contains only open mp
        assertNotNull("List of marketplaces expected - ", list);
        assertEquals("One marketplace expected - ", 1, list.size());
        assertEquals("Open marketplace expected - ", OPEN_MP_ID, list.get(0)
                .getMarketplaceId());
    }

    @Test
    public void getMarketplacesForOrganization_supplierHasPublishingRightsOnClosedMp()
            throws Exception {
        container.login(supplier3Key, ROLE_SERVICE_MANAGER);

        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                MarketplaceToOrganization mto = new MarketplaceToOrganization(
                        mpClosed, supp3,
                        PublishingAccess.PUBLISHING_ACCESS_GRANTED);
                mgr.persist(mto);
                mgr.flush();

                return null;
            }
        });

        List<VOMarketplace> list = marketplaceService
                .getMarketplacesForOrganization();

        // expected result: contains both open + closed mp
        assertNotNull("List of marketplaces expected - ", list);
        assertEquals("Two marketplace expected - ", 2, list.size());
        Set<String> mpIds = new HashSet<String>();
        for (VOMarketplace mp : list) {
            mpIds.add(mp.getMarketplaceId());
        }
        assertTrue("Open marketplace expected - ", mpIds.contains(OPEN_MP_ID));
        assertTrue("Closed marketplace expected - ",
                mpIds.contains(CLOSED_MP_ID));
    }

    /*
     * Use case: supp3 was banned on previously open MP, blacklist ref remains
     * after switching MP to closed => this marketplace must not show up in the
     * returned list of marketplaces
     */
    @Test
    public void getMarketplacesForOrganization_supplierIsBannedOnOpenMp()
            throws Exception {
        container.login(supplier3Key, ROLE_SERVICE_MANAGER);

        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                MarketplaceToOrganization mto = new MarketplaceToOrganization(
                        mpOpen, supp3,
                        PublishingAccess.PUBLISHING_ACCESS_DENIED);
                mgr.persist(mto);
                mgr.flush();

                return null;
            }
        });

        List<VOMarketplace> list = marketplaceService
                .getMarketplacesForOrganization();

        // expected result: contains no mp
        assertNotNull("List of marketplaces expected - ", list);
        assertEquals("One marketplace expected - ", 0, list.size());
    }

    /*
     * Use case: supp3 has previously published on this open MP, whitelist ref
     * exists => this marketplace must show up in the returned list of
     * marketplaces
     */
    @Test
    public void getMarketplacesForOrganization_supplierHasExplicitPublishingRightsOnOpenMp()
            throws Exception {
        container.login(supplier3Key, ROLE_SERVICE_MANAGER);

        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                MarketplaceToOrganization mto = new MarketplaceToOrganization(
                        mpOpen, supp3,
                        PublishingAccess.PUBLISHING_ACCESS_GRANTED);
                mgr.persist(mto);
                mgr.flush();

                return null;
            }
        });

        List<VOMarketplace> list = marketplaceService
                .getMarketplacesForOrganization();

        // expected result: contains only open mp
        assertNotNull("List of marketplaces expected - ", list);
        assertEquals("One marketplace expected - ", 1, list.size());
        assertEquals("Open marketplace expected - ", OPEN_MP_ID, list.get(0)
                .getMarketplaceId());
    }

    @Test(expected = EJBException.class)
    public void getMarketplacesForOperator_serviceManager() throws Exception {
        container.login(supplier1Key, ROLE_SERVICE_MANAGER);
        marketplaceService.getMarketplacesForOperator();
    }

    @Test(expected = EJBException.class)
    public void getMarketplacesForOperator_technologyManager() throws Exception {
        container.login(techProviderKey, ROLE_TECHNOLOGY_MANAGER);
        marketplaceService.getMarketplacesForOperator();
    }

    @Test(expected = EJBException.class)
    public void getMarketplacesForOperator_admin() throws Exception {
        container.login(supplier1Key, ROLE_ORGANIZATION_ADMIN);
        marketplaceService.getMarketplacesForOperator();
    }

    @Test
    public void getMarketplacesForOperator() throws Exception {
        container.login(platformOperatorUserKey, ROLE_PLATFORM_OPERATOR);
        List<VOMarketplace> availableMps = marketplaceService
                .getMarketplacesForOperator();
        // set of available marketplaces must contain the three global MPs
        // (supp3 has no local mp)
        assertEquals(
                "Result must return all marketplaces (both local and global)",
                3, availableMps.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void getMarketplacesForService_NullInput() throws Exception {
        try {
            container.login(supplier1Key, ROLE_SERVICE_MANAGER);
            marketplaceService.getMarketplacesForService(null);
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    @Test(expected = EJBException.class)
    public void getMarketplacesForService_TechnologyProvider() throws Exception {
        container.login(techProviderKey, ROLE_ORGANIZATION_ADMIN,
                ROLE_TECHNOLOGY_MANAGER);
        marketplaceService.getMarketplacesForService(new VOService());
    }

    @Test(expected = EJBException.class)
    public void getMarketplacesForService_Customer() throws Exception {
        container.login(mpOwnerUserKey);
        marketplaceService.getMarketplacesForService(new VOService());
    }

    @Test(expected = OperationNotPermittedException.class)
    public void getMarketplacesForService_SupplierNotOwnerOfService()
            throws Exception {
        try {
            marketplaceService.getMarketplacesForService(voSvc2_1);
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    @Test(expected = ObjectNotFoundException.class)
    public void getMarketplacesForService_SvcNotFound() throws Exception {
        VOService svc = new VOService();
        svc.setServiceId("UNKNOWN_SVC_ID");
        try {
            container.login(supplier1Key, ROLE_SERVICE_MANAGER);
            marketplaceService.getMarketplacesForService(svc);
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    @Test
    // for now, this cannot happen since we assume a service to be
    // published to exactly one MP; however in the future we'll allow a
    // service also to be "unpublished", i.e. not published to any MP
    public void getMarketplacesForService_publishedNowhere() throws Exception {
        container.login(supplier1Key, ROLE_SERVICE_MANAGER);
        VOService voSvc1_3 = createUnpublishedService(supp1,
                p1_1.getTechnicalProduct(), "supp1_p3");
        assertEquals(Collections.emptyList(),
                marketplaceService.getMarketplacesForService(voSvc1_3));
    }

    @Test
    public void getMarketplacesForService_publishedOnLocalMP() throws Exception {
        container.login(supplier1Key, ROLE_SERVICE_MANAGER);
        assertEquals(1, marketplaceService.getMarketplacesForService(voSvc1_1)
                .size());
    }

    @Test
    public void getMarketplacesForService_publishedOnGlobalMP()
            throws Exception {
        assertEquals(1, marketplaceService.getMarketplacesForService(voSvc1_2)
                .size());
        assertEquals(mpGlobal.getMarketplaceId(), marketplaceService
                .getMarketplacesForService(voSvc1_2).get(0).getMarketplace()
                .getMarketplaceId());
    }

    @Test
    public void getMarketplacesForService_publishedOnGlobalMP_customer_priceModel()
            throws Exception {

        // create a customer specific product with price model
        Product custProd = runTX(new Callable<Product>() {
            @Override
            public Product call() throws Exception {
                Product custProd = Products.createCustomerSpecifcProduct(mgr,
                        supp1, p1_2, ServiceStatus.ACTIVE);

                PriceModel pm = new PriceModel();
                pm.setType(PriceModelType.PRO_RATA);
                pm.setPeriod(PricingPeriod.DAY);
                pm.setPricePerPeriod(new BigDecimal(1));
                SupportedCurrency sc = new SupportedCurrency();
                sc.setCurrency(Currency.getInstance("EUR"));
                sc = (SupportedCurrency) mgr.find(sc);
                pm.setCurrency(sc);
                custProd.setPriceModel(pm);
                custProd.setStatus(ServiceStatus.ACTIVE);
                mgr.persist(custProd);
                return custProd;
            }
        });

        VOService voCustSvc = new VOService();
        voCustSvc.setKey(custProd.getKey());

        container.login(supplier1Key, ROLE_SERVICE_MANAGER);

        // getMarketplacesForService must return the template product
        List<VOCatalogEntry> list = marketplaceService
                .getMarketplacesForService(voCustSvc);
        assertEquals(1, list.size());
        assertEquals(voSvc1_2.getKey(), list.get(0).getService().getKey());
    }

    @Test(expected = ObjectNotFoundException.class)
    public void getMarketplaceForSubscription_SubscriptionDoesNotExist()
            throws Exception {
        getMarketplaceId(marketplaceService, -1);
    }

    @Test
    public void getMarketplaceForSubscription_TechnologyProviderMarketplaceSet()
            throws Exception {
        long subscriptionKey = createSubscription(LOCAL_MP_ID_SUPP1);
        assertEquals(LOCAL_MP_ID_SUPP1,
                getMarketplaceId(marketplaceService, subscriptionKey));
    }

    @Test
    public void getMarketplaceForSubscription_CustomerMarketplaceSet()
            throws Exception {
        long subscriptionKey = createSubscription(LOCAL_MP_ID_SUPP1);
        assertEquals(LOCAL_MP_ID_SUPP1,
                getMarketplaceId(marketplaceService, subscriptionKey));
    }

    @Test
    public void getMarketplaceForSubscription_SupplierMarketplaceSet()
            throws Exception {
        long subscriptionKey = createSubscription(LOCAL_MP_ID_SUPP1);
        assertEquals(LOCAL_MP_ID_SUPP1,
                getMarketplaceId(marketplaceService, subscriptionKey));
    }

    @Test
    public void getMarketplaceForSubscription_TechnologyProviderNoMarketplaceSet()
            throws Exception {
        long subscriptionKey = createSubscription(null);
        assertEquals(null,
                getMarketplaceId(marketplaceService, subscriptionKey));
    }

    @Test
    public void getMarketplaceForSubscription_CustomerNoMarketplaceSet()
            throws Exception {
        long subscriptionKey = createSubscription(null);
        assertEquals(null,
                getMarketplaceId(marketplaceService, subscriptionKey));
    }

    @Test
    public void getMarketplaceForSubscription_SupplierNoMarketplaceSet()
            throws Exception {
        long subscriptionKey = createSubscription(null);
        assertEquals(null,
                getMarketplaceId(marketplaceService, subscriptionKey));
    }

    @Test(expected = EJBAccessException.class)
    public void getMarketplacesOwned_Provider() throws Exception {
        createAndSetOrganizationWithGivenRole();
        container.login(techProviderKey);
        try {
            marketplaceService.getMarketplacesOwned();
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    @Test
    public void getMarketplacesOwned_Supplier() throws Exception {
        container.login(mpOwnerUserKey, ROLE_MARKETPLACE_OWNER);
        List<VOMarketplace> list = marketplaceService.getMarketplacesOwned();
        assertNotNull(list);
        assertEquals(1, list.size());
    }

    @Test
    public void getMarketplacesOwned_GlobalMarketplaceOwner() throws Exception {
        container.login(mpOwnerUserKey, ROLE_MARKETPLACE_OWNER);
        List<VOMarketplace> list = marketplaceService.getMarketplacesOwned();
        assertNotNull(list);
        assertEquals(1, list.size());
    }

    @Test(expected = OperationNotPermittedException.class)
    public void getOrganizationsForMarketplace_closedMp_WrongMarketplace()
            throws Exception {
        container.login(supplier1Key, ROLE_MARKETPLACE_OWNER);
        marketplaceService.getOrganizationsForMarketplace(LOCAL_MP_ID_SUPP1);
    }

    @Test(expected = MarketplaceAccessTypeUneligibleForOperationException.class)
    public void getOrganizationsForMarketplace_openMp() throws Exception {
        container.login(mpOwnerUserKey2, ROLE_MARKETPLACE_OWNER);
        marketplaceService.getOrganizationsForMarketplace(OPEN_MP_ID);
    }

    @Test
    public void getOrganizationsForMarketplace_closedMp_noSuppliersAccepted()
            throws Exception {
        container.login(mpOwnerUserKey2, ROLE_MARKETPLACE_OWNER);

        List<VOOrganization> list = marketplaceService
                .getOrganizationsForMarketplace(CLOSED_MP_ID);

        assertNotNull("List of suppliers expected - ", list);
        assertEquals("No supplier expected - ", 0, list.size());
    }

    /*
     * Use case: supp1 was banned on previously open MP, blacklist ref remains
     * after switching MP to closed => this should have no effect on the
     * returned list of accepted suppliers
     */
    @Test
    public void getOrganizationsForMarketplace_closedMp_supplierHadBeenBannedOnMpBefore()
            throws Exception {
        container.login(mpOwnerUserKey2, ROLE_MARKETPLACE_OWNER);

        runTX(new Callable<MarketplaceToOrganization>() {

            @Override
            public MarketplaceToOrganization call() throws Exception {
                MarketplaceToOrganization mto = new MarketplaceToOrganization(
                        mpClosed, supp1,
                        PublishingAccess.PUBLISHING_ACCESS_DENIED);
                mgr.persist(mto);
                mgr.flush();

                return mto;
            }
        });

        List<VOOrganization> list = marketplaceService
                .getOrganizationsForMarketplace(CLOSED_MP_ID);

        assertNotNull("List of suppliers expected - ", list);
        assertEquals("No supplier expected - ", 0, list.size());
    }

    @Test
    public void getOrganizationsForMarketplace_closedMp_supplierAccepted()
            throws Exception {
        container.login(mpOwnerUserKey2, ROLE_MARKETPLACE_OWNER);

        runTX(new Callable<MarketplaceToOrganization>() {

            @Override
            public MarketplaceToOrganization call() throws Exception {
                MarketplaceToOrganization mto = new MarketplaceToOrganization(
                        mpClosed, supp1,
                        PublishingAccess.PUBLISHING_ACCESS_GRANTED);
                mgr.persist(mto);
                mgr.flush();

                return mto;
            }
        });

        List<VOOrganization> list = marketplaceService
                .getOrganizationsForMarketplace(CLOSED_MP_ID);

        assertNotNull("List of suppliers expected - ", list);
        assertEquals("No supplier expected - ", 1, list.size());
    }

    @Test
    public void getOrganizationsForMarketplace_closedMp_MoreSuppliers()
            throws Exception {
        container.login(mpOwnerUserKey, ROLE_MARKETPLACE_OWNER);
        marketplaceService.addOrganizationsToMarketplace(
                Collections.singletonList(supp1.getOrganizationId()),
                GLOBAL_MP_ID);
        marketplaceService.addOrganizationsToMarketplace(
                Collections.singletonList(supp2.getOrganizationId()),
                GLOBAL_MP_ID);
        List<VOOrganization> list = marketplaceService
                .getOrganizationsForMarketplace(GLOBAL_MP_ID);
        assertNotNull("List of suppliers expected - ", list);
        assertEquals("Two suppliers expected", 2, list.size());

        mailCounter = 0;
        marketplaceService.removeOrganizationsFromMarketplace(
                Collections.singletonList(supp1.getOrganizationId()),
                GLOBAL_MP_ID);
        assertEquals(EmailType.MARKETPLACE_SUPPLIER_REMOVED, emailType2);
        assertTrue(publicAccessUrl
                .contains(org.oscm.types.constants.marketplace.Marketplace.MARKETPLACE_ROOT));
        assertNotNull(adminUrl);
        assertEquals(1, mailCounter);

        list = marketplaceService.getOrganizationsForMarketplace(GLOBAL_MP_ID);
        assertNotNull("List of suppliers expected - ", list);
        assertEquals("One supplier expected", 1, list.size());
        assertEquals("Supp2 expected", supp2.getOrganizationId(), list.get(0)
                .getOrganizationId());
    }

    @Test
    public void getMarketplaceById_ExistingMP() throws Exception {
        // for existing MPs, method should return the actual Mps
        VOMarketplace mp1 = marketplaceService.getMarketplaceById(GLOBAL_MP_ID);
        assertEquals(GLOBAL_MP_ID, mp1.getMarketplaceId());

        VOMarketplace mp2 = marketplaceService
                .getMarketplaceById(LOCAL_MP_ID_SUPP1);
        assertEquals(LOCAL_MP_ID_SUPP1, mp2.getMarketplaceId());
    }

    @Test(expected = ObjectNotFoundException.class)
    public void getMarketplaceById_NonExistingMP() throws Exception {
        // for non-existing MPs, method should throw corresponding exception
        marketplaceService.getMarketplaceById("myFantasyId");
    }

}
