/*******************************************************************************
 *                                                                              
7*  Copyright FUJITSU LIMITED 2018
 *                                                                              
 *  Author: groch                                                 
 *                                                                              
 *  Creation Date: 07.03.2011                                                      
 *                                                                              
 *  Completion Time: 10.03.2011                                             
 *                                                                              
 *******************************************************************************/

package org.oscm.marketplace.bean;

import static org.oscm.internal.types.enumtypes.OrganizationRoleType.SUPPLIER;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import org.oscm.accountservice.bean.AccountServiceBean;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.*;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.domobjects.enums.PublishingAccess;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.i18nservice.bean.LocalizerServiceBean;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.internal.intf.CategorizationService;
import org.oscm.internal.intf.MarketplaceService;
import org.oscm.internal.intf.ServiceProvisioningService;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.*;
import org.oscm.internal.vo.VOCatalogEntry;
import org.oscm.internal.vo.VOMarketplace;
import org.oscm.internal.vo.VOService;
import org.oscm.marketplace.assembler.MarketplaceAssembler;
import org.oscm.marketplace.cache.MarketplaceCacheServiceBean;
import org.oscm.serviceprovisioningservice.assembler.ProductAssembler;
import org.oscm.serviceprovisioningservice.bean.ServiceProvisioningServiceBean;
import org.oscm.test.BaseAdmUmTest;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.Marketplaces;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.PlatformUsers;
import org.oscm.test.data.Products;
import org.oscm.test.data.Subscriptions;
import org.oscm.test.data.SupportedCountries;
import org.oscm.test.data.TechnicalProducts;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.CommunicationServiceStub;
import org.oscm.test.stubs.ConfigurationServiceStub;
import org.oscm.test.stubs.TriggerQueueServiceStub;
import org.oscm.types.enumtypes.EmailType;

/**
 * For the management of marketplace unit tests. Since there are many methods
 * need to be tested, we decided to disperse all the unit tests into four
 * classes to avoid the coupling factor. This class only contains the setup
 * method and the necessary protected methods which are needed by the unit
 * tests.
 * 
 * @author Heng.Sun
 * 
 */
public class MarketplaceServiceTestBase extends EJBTestBase {

    protected static final String GLOBAL_MP_ID = "GLOBAL_MP";
    protected static final String OPEN_MP_ID = "OPEN_MP";
    protected static final String CLOSED_MP_ID = "CLOSED_MP";
    protected Marketplace mpGlobal;
    protected Marketplace mpSupp1;
    protected Marketplace mpOpen;
    protected Marketplace mpClosed;
    protected Marketplace mpGlobal1;

    protected DataService mgr;

    protected Organization mpOwner;
    protected Organization mpOwner2;
    protected Organization supp1;
    protected Organization supp2;
    protected Organization supp3;

    protected Organization broker;
    protected Organization reseller;
    protected Organization supplier4;
    protected Organization mplOwner;
    protected Organization platformOperatorOrg;

    protected MarketplaceService marketplaceService;
    protected ServiceProvisioningService provisioningService;
    protected LocalizerFacade localizerFacade;
    protected LocalizerServiceLocal localizer;
    protected CategorizationService categorizationService;

    protected Product p1_1;
    protected Product p1_2;
    protected Product p2_1;
    protected Product p2_2;

    protected VOService voSvc1_1;
    protected VOService voSvc1_2;
    protected VOService voSvc2_1;
    protected VOService voSvc2_2;

    protected CatalogEntry ce_p1_1;
    protected CatalogEntry ce_p1_2;
    protected CatalogEntry ce_p2_1;
    protected CatalogEntry ce_p2_2;

    protected RevenueShareModel brokerRevenueShareModel1_1;
    protected RevenueShareModel resellerRevenueShareModel1_1;
    protected RevenueShareModel brokerRevenueShareModel1_2;
    protected RevenueShareModel resellerRevenueShareModel1_2;
    protected RevenueShareModel brokerRevenueShareModel2_1;
    protected RevenueShareModel resellerRevenueShareModel2_1;
    protected RevenueShareModel brokerRevenueShareModel2_2;
    protected RevenueShareModel resellerRevenueShareModel2_2;

    protected VOCatalogEntry voCESvc1_1;
    protected VOCatalogEntry voCESvc1_2;
    protected VOCatalogEntry voCESvc2;

    protected String LOCAL_MP_ID_SUPP1;

    protected long mpOwnerUserKey;
    protected long mpOwnerUserKey2;
    protected long supplier1Key;
    protected long supplier2Key;
    protected long supplier3Key;
    protected long techProviderKey;
    protected long platformOperatorUserKey;
    protected String owningOrganizationId;

    protected VOMarketplace voMarketplace;

    protected static boolean organizationsCreated = false;
    protected static Organization myTechnologyProvider;
    protected static Organization myCustomer;

    protected EmailType emailType1 = null;
    protected EmailType emailType2 = null;
    protected int mailCounter = 0;
    protected PlatformUser emailRecipient1;
    protected PlatformUser emailRecipient2;
    protected PlatformUser admin1;
    protected PlatformUser admin2;
    protected PlatformUser userToVerify;
    protected String publicAccessUrl;
    protected String adminUrl;

    protected static PlatformUser platformOperatorUser;
    protected static List<PaymentType> paymentTypeList;

    @Override
    protected void setup(final TestContainer container) throws Exception {
        container.enableInterfaceMocking(true);
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new DataServiceBean());
        container.addBean(new LocalizerServiceBean());

        container.addBean(new CommunicationServiceStub() {
            @Override
            public void sendMail(PlatformUser recipient, EmailType type,
                    Object[] params, Marketplace marketplace) {
                if (emailType1 == null)
                    emailType1 = type;
                else
                    emailType2 = type;
                mailCounter++;
                if (emailRecipient1 == null)
                    emailRecipient1 = recipient;
                else
                    emailRecipient2 = recipient;
                publicAccessUrl = (String) params[1];
                if (params.length > 2) {
                    adminUrl = (String) params[2];
                }
            }

            @Override
            public String getMarketplaceUrl(String marketplaceId)
                    throws MailOperationException {
                return "myBaseUrl" + (marketplaceId == null
                        || marketplaceId.trim().length() == 0 ? ""
                                : "/marketplace/index.jsf?mId="
                                        + marketplaceId);
            }

            @Override
            public String getBaseUrl() {
                return "myBaseUrl";
            }

        });
        container.addBean(new TriggerQueueServiceStub());
        container.addBean(new ServiceProvisioningServiceBean());
        container.addBean(new AccountServiceBean());
        container.addBean(new CategorizationServiceBean());

        container.addBean(new MarketplaceCacheServiceBean());
        container.addBean(new LandingpageServiceBeanLocal());
        container.addBean(new MarketplaceServiceLocalBean());
        container.addBean(new MarketplaceServiceBean());

        categorizationService = container.get(CategorizationService.class);
        provisioningService = container.get(ServiceProvisioningService.class);
        marketplaceService = container.get(MarketplaceService.class);
        mgr = container.get(DataService.class);

        // get localizer facade
        this.localizer = container.get(LocalizerServiceLocal.class);
        localizerFacade = new LocalizerFacade(localizer, "en");

        // create marketplace + corresponding owner
        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                mpOwner = Organizations.createOrganization(mgr,
                        OrganizationRoleType.MARKETPLACE_OWNER,
                        OrganizationRoleType.PLATFORM_OPERATOR,
                        OrganizationRoleType.TECHNOLOGY_PROVIDER,
                        OrganizationRoleType.SUPPLIER);
                PlatformUser createUserForOrg = Organizations
                        .createUserForOrg(mgr, mpOwner, true, "admin");
                mpOwnerUserKey = createUserForOrg.getKey();
                PlatformUsers.grantRoles(mgr, createUserForOrg,
                        UserRoleType.PLATFORM_OPERATOR);
                PlatformUsers.grantRoles(mgr, createUserForOrg,
                        UserRoleType.MARKETPLACE_OWNER);

                return null;
            }
        });

        mpGlobal = runTX(new Callable<Marketplace>() {
            @Override
            public Marketplace call() throws Exception {
                return Marketplaces.createMarketplace(mpOwner, GLOBAL_MP_ID,
                        false, mgr);
            }
        });

        // create marketplace + corresponding owner
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                mpOwner2 = Organizations.createOrganization(mgr,
                        OrganizationRoleType.MARKETPLACE_OWNER,
                        OrganizationRoleType.PLATFORM_OPERATOR,
                        OrganizationRoleType.TECHNOLOGY_PROVIDER);
                PlatformUser createUserForOrg = Organizations
                        .createUserForOrg(mgr, mpOwner2, true, "admin");
                mpOwnerUserKey2 = createUserForOrg.getKey();
                PlatformUsers.grantRoles(mgr, createUserForOrg,
                        UserRoleType.PLATFORM_OPERATOR);
                PlatformUsers.grantRoles(mgr, createUserForOrg,
                        UserRoleType.MARKETPLACE_OWNER);

                return null;
            }
        });

        mpOpen = runTX(new Callable<Marketplace>() {
            @Override
            public Marketplace call() throws Exception {
                return Marketplaces.createMarketplace(mpOwner2, OPEN_MP_ID,
                        true, mgr);
            }
        });

        mpClosed = runTX(new Callable<Marketplace>() {
            @Override
            public Marketplace call() throws Exception {
                return Marketplaces.createMarketplace(mpOwner2, CLOSED_MP_ID,
                        false, mgr);
            }
        });

        // // create 3 suppliers (with their local marketplaces)
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                supp1 = Organizations.createOrganization(mgr,
                        OrganizationRoleType.SUPPLIER);
                mpSupp1 = mpGlobal;
                LOCAL_MP_ID_SUPP1 = mpSupp1.getMarketplaceId();

                PlatformUser createUserForOrg = Organizations
                        .createUserForOrg(mgr, supp1, true, "admin");
                supplier1Key = createUserForOrg.getKey();

                supp2 = Organizations.createOrganization(mgr,
                        OrganizationRoleType.SUPPLIER);

                createUserForOrg = Organizations.createUserForOrg(mgr, supp2,
                        true, "userS2");
                supplier2Key = createUserForOrg.getKey();

                // supp3 has no local mp!
                supp3 = Organizations.createOrganization(mgr,
                        OrganizationRoleType.SUPPLIER);

                createUserForOrg = Organizations.createUserForOrg(mgr, supp3,
                        false, "userS3");
                supplier3Key = createUserForOrg.getKey();
                return null;
            }
        });

        if (!organizationsCreated) {
            createAndSetOrganizationWithGivenRole();
        }
        createPlatformOperator();

        // create products
        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                TechnicalProduct tProd1 = TechnicalProducts
                        .createTechnicalProduct(mgr, supp1, "tp1", false,
                                ServiceAccessType.LOGIN);
                // supp1 offers 2 services
                p1_1 = Products.createProduct(supp1, tProd1, false, "supp1_p1",
                        "priceModelId1", mgr);
                mgr.persist(p1_1);

                p1_2 = Products.createProduct(supp1, tProd1, false, "supp1_p2",
                        "priceModelId1", mgr);
                mgr.persist(p1_2);

                // supp2 offers 1 service
                TechnicalProduct tProd2 = TechnicalProducts
                        .createTechnicalProduct(mgr, supp1, "tp2", false,
                                ServiceAccessType.LOGIN);
                p2_1 = Products.createProduct(supp2, tProd2, false, "supp2_p1",
                        "priceModelId2", mgr);
                mgr.persist(p2_1);

                p2_2 = Products.createProduct(supp2, tProd2, false, "supp2_p2",
                        "priceModelId2", mgr);
                mgr.persist(p2_2);

                ProductReference pr = new ProductReference(p2_1, p2_2);
                p2_1.getAllCompatibleProducts().add(pr);
                p2_2.getCompatibleProductsTarget().add(pr);
                mgr.persist(pr);

                // supp3 offers no service at all

                return null;
            }
        });

        // create corresponding catalog entries
        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {

                brokerRevenueShareModel1_1 = mpGlobal.getBrokerPriceModel()
                        .copy();
                mgr.persist(brokerRevenueShareModel1_1);
                resellerRevenueShareModel1_1 = mpGlobal.getResellerPriceModel()
                        .copy();
                mgr.persist(resellerRevenueShareModel1_1);

                ce_p1_1 = ((Product) mgr.find(p1_1)).getCatalogEntries().get(0);
                ce_p1_1.setAnonymousVisible(true);
                ce_p1_1.setVisibleInCatalog(true);
                ce_p1_1.setMarketplace(mpSupp1);
                ce_p1_1.setBrokerPriceModel(brokerRevenueShareModel1_1);
                ce_p1_1.setResellerPriceModel(resellerRevenueShareModel1_1);
                // just for completion, actually redundant
                mgr.persist(ce_p1_1);

                brokerRevenueShareModel1_2 = mpGlobal.getBrokerPriceModel()
                        .copy();
                mgr.persist(brokerRevenueShareModel1_2);
                resellerRevenueShareModel1_2 = mpGlobal.getResellerPriceModel()
                        .copy();
                mgr.persist(resellerRevenueShareModel1_2);

                ce_p1_2 = ((Product) mgr.find(p1_2)).getCatalogEntries().get(0);
                ce_p1_2.setAnonymousVisible(true);
                ce_p1_2.setVisibleInCatalog(true);
                ce_p1_2.setMarketplace(mpGlobal);
                ce_p1_2.setBrokerPriceModel(brokerRevenueShareModel1_2);
                ce_p1_2.setResellerPriceModel(resellerRevenueShareModel1_2);
                mgr.persist(ce_p1_2);

                brokerRevenueShareModel2_1 = mpGlobal.getBrokerPriceModel()
                        .copy();
                mgr.persist(brokerRevenueShareModel2_1);
                resellerRevenueShareModel2_1 = mpGlobal.getResellerPriceModel()
                        .copy();
                mgr.persist(resellerRevenueShareModel2_1);

                ce_p2_1 = ((Product) mgr.find(p2_1)).getCatalogEntries().get(0);
                ce_p2_1.setAnonymousVisible(true);
                ce_p2_1.setVisibleInCatalog(true);
                ce_p2_1.setMarketplace(mpGlobal);
                ce_p2_1.setBrokerPriceModel(brokerRevenueShareModel2_1);
                ce_p2_1.setResellerPriceModel(resellerRevenueShareModel2_1);
                mgr.persist(ce_p2_1);

                brokerRevenueShareModel2_2 = mpGlobal.getBrokerPriceModel()
                        .copy();
                mgr.persist(brokerRevenueShareModel2_2);
                resellerRevenueShareModel2_2 = mpGlobal.getResellerPriceModel()
                        .copy();
                mgr.persist(resellerRevenueShareModel2_2);

                ce_p2_2 = ((Product) mgr.find(p2_2)).getCatalogEntries().get(0);
                ce_p2_2.setAnonymousVisible(true);
                ce_p2_2.setVisibleInCatalog(true);
                ce_p2_2.setMarketplace(mpGlobal);
                ce_p2_2.setBrokerPriceModel(brokerRevenueShareModel2_2);
                ce_p2_2.setResellerPriceModel(resellerRevenueShareModel2_2);
                mgr.persist(ce_p2_2);

                return null;
            }
        });

        // setup supplier and adminUsers
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                supplier4 = Organizations.createOrganization(mgr, SUPPLIER);
                admin1 = Organizations.createUserForOrg(mgr, supplier4, true,
                        "admin1");
                admin2 = Organizations.createUserForOrg(mgr, supplier4, true,
                        "admin2");
                Organizations.createUserForOrg(mgr, supplier4, false, "user");
                return null;
            }
        });

        // create service voSvc1_1 based on p1_1
        voSvc1_1 = new VOService();
        voSvc1_1.setKey(p1_1.getKey());
        voSvc1_1.setServiceId(p1_1.getProductId());
        // create corresponding voCatalogEntry
        voCESvc1_1 = new VOCatalogEntry();
        voCESvc1_1.setKey(ce_p1_1.getKey());
        voCESvc1_1.setAnonymousVisible(ce_p1_1.isAnonymousVisible());
        voCESvc1_1.setVisibleInCatalog(ce_p1_1.isVisibleInCatalog());
        VOMarketplace voMP1 = new VOMarketplace();
        voMP1.setMarketplaceId(LOCAL_MP_ID_SUPP1);
        voCESvc1_1.setMarketplace(voMP1);
        // publish on local MP of supp1

        container.login(supplier1Key, ROLE_ORGANIZATION_ADMIN,
                ROLE_SERVICE_MANAGER);

        marketplaceService.publishService(voSvc1_1, Arrays.asList(voCESvc1_1));

        // create service voSvc1_2 based on p1_2
        voSvc1_2 = new VOService();
        voSvc1_2.setKey(p1_2.getKey());
        voSvc1_2.setServiceId(p1_2.getProductId());
        // create corresponding voCatalogEntry
        voCESvc1_2 = new VOCatalogEntry();
        voCESvc1_2.setKey(ce_p1_2.getKey());
        voCESvc1_2.setAnonymousVisible(ce_p1_2.isAnonymousVisible());
        voCESvc1_2.setVisibleInCatalog(ce_p1_2.isVisibleInCatalog());
        VOMarketplace voMP2 = new VOMarketplace();
        voMP2.setMarketplaceId(GLOBAL_MP_ID);
        voCESvc1_2.setMarketplace(voMP2);
        // publish to MP

        marketplaceService.publishService(voSvc1_2, Arrays.asList(voCESvc1_2));

        // create service voSvc2 based on p2
        // to any MP
        voSvc2_1 = new VOService();
        voSvc2_1.setKey(p2_1.getKey());
        voSvc2_1.setServiceId(p2_1.getProductId());
        // create corresponding voCatalogEntry
        voCESvc2 = new VOCatalogEntry();
        voCESvc2.setKey(ce_p2_1.getKey());
        voCESvc2.setAnonymousVisible(ce_p2_1.isAnonymousVisible());
        voCESvc2.setVisibleInCatalog(ce_p2_1.isVisibleInCatalog());
        voCESvc2.setMarketplace(voMP2);
        // publish to MP

        voSvc2_2 = new VOService();
        voSvc2_2.setKey(p2_2.getKey());
        voSvc2_2.setServiceId(p2_2.getProductId());

        container.login(supplier2Key, ROLE_ORGANIZATION_ADMIN,
                ROLE_SERVICE_MANAGER);

        marketplaceService.publishService(voSvc2_1, Arrays.asList(voCESvc2));

        container.login(supplier1Key, ROLE_ORGANIZATION_ADMIN,
                ROLE_SERVICE_MANAGER);

        resetEmailNotificationResults();
    }

    protected void createAndSetOrganizationWithGivenRole() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                if (paymentTypeList == null || paymentTypeList.isEmpty()) {
                    paymentTypeList = BaseAdmUmTest.createPaymentTypes(mgr);
                }
                BaseAdmUmTest.createSupportedCurrencies(mgr);
                SupportedCountries.setupAllCountries(mgr);
                mgr.flush();

                myTechnologyProvider = Organizations.createOrganization(mgr,
                        OrganizationRoleType.TECHNOLOGY_PROVIDER);

                PlatformUser createUserForOrg = Organizations.createUserForOrg(
                        mgr, myTechnologyProvider, false, "userTechProvider");
                techProviderKey = createUserForOrg.getKey();

                myCustomer = Organizations.createOrganization(mgr,
                        OrganizationRoleType.CUSTOMER);

                return null;
            }
        });
        organizationsCreated = true;
    }

    protected void createPlatformOperator() throws Exception {
        if (platformOperatorOrg == null) {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {

                    platformOperatorOrg = Organizations.createOrganization(mgr,
                            OrganizationRoleType.PLATFORM_OPERATOR);

                    platformOperatorUser = Organizations.createUserForOrg(mgr,
                            platformOperatorOrg, true, "Administrator");
                    PlatformUsers.grantRoles(mgr, platformOperatorUser,
                            UserRoleType.PLATFORM_OPERATOR);

                    platformOperatorUserKey = platformOperatorUser.getKey();
                    System.out.println("## platformOperatorUserKey="
                            + platformOperatorUserKey);

                    return null;
                }
            });

        }
    }

    protected VOService createUnpublishedService(final Organization supp,
            final TechnicalProduct techProd, final String productId)
            throws Exception {
        // first create dom obj
        final Product myProd = runTX(new Callable<Product>() {

            @Override
            public Product call() throws Exception {

                Product p = Products.createProduct(supp, techProd, false,
                        productId, "priceModelId1", mgr);
                mgr.persist(p);
                mgr.flush();
                return p;
            }
        });

        // now create service based on dom obj, but do not explicitly publish
        // it to any MP (remove corresponding catalog entry)
        VOService mySvcVo = new VOService();
        mySvcVo.setKey(myProd.getKey());
        mySvcVo.setServiceId(myProd.getProductId());
        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                Product p = mgr.getReference(Product.class, myProd.getKey());
                mgr.remove(p.getCatalogEntries().get(0));
                p.setCatalogEntries(new ArrayList<CatalogEntry>());
                mgr.persist(p);
                return null;
            }
        });
        return mySvcVo;
    }

    protected VOCatalogEntry createCatalogEntry(final long pKey,
            final Marketplace mp) throws Exception {

        final VOCatalogEntry voCE = new VOCatalogEntry();
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                voCE.setMarketplace(MarketplaceAssembler.toVOMarketplace(
                        mgr.getReference(Marketplace.class, mp.getKey()),
                        localizerFacade));
                voCE.setService(ProductAssembler.toVOProduct(
                        mgr.find(Product.class, pKey), localizerFacade));
                return null;
            }
        });
        return voCE;
    }

    protected void assertGetMarketplaces() throws Exception {
        List<VOMarketplace> availableMps = marketplaceService
                .getMarketplacesForOrganization();
        assertEquals("Result must contain global and open marketplace", 2,
                availableMps.size());
        Set<String> marketplaceIds = new HashSet<>();
        for (VOMarketplace voMarketplace : availableMps) {
            marketplaceIds.add(voMarketplace.getMarketplaceId());
        }
        assertTrue(marketplaceIds.contains(GLOBAL_MP_ID));
        assertTrue(marketplaceIds.contains(OPEN_MP_ID));
    }

    protected void unpublish(final boolean isAnonymousVisible,
            final boolean isVisibleInCatalog)
            throws ObjectNotFoundException, ValidationException,
            NonUniqueBusinessKeyException, OperationNotPermittedException,
            UpdateConstraintException, Exception {
        VOCatalogEntry voCESvc1_Updated = new VOCatalogEntry();
        voCESvc1_Updated.setMarketplace(null);
        voCESvc1_Updated.setAnonymousVisible(isAnonymousVisible);
        voCESvc1_Updated.setVisibleInCatalog(isVisibleInCatalog);
        voCESvc1_Updated.setService(voSvc1_2);

        marketplaceService.publishService(voSvc1_2,
                Arrays.asList(voCESvc1_Updated));

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Product p = mgr.getReference(Product.class, p1_2.getKey());
                assertEquals(1, p.getCatalogEntries().size());
                CatalogEntry ce = p.getCatalogEntries().get(0);
                assertEquals(null, ce.getMarketplace());
                assertEquals(Boolean.valueOf(isAnonymousVisible),
                        Boolean.valueOf(ce.isAnonymousVisible()));
                assertEquals(Boolean.valueOf(isVisibleInCatalog),
                        Boolean.valueOf(ce.isVisibleInCatalog()));
                assertNull(ce.getBrokerPriceModel());
                assertNull(ce.getResellerPriceModel());
                return null;
            }
        });
    }

    protected long createSubscription(final String marketplaceId)
            throws Exception {
        createAndSetOrganizationWithGivenRole();
        return runTX(new Callable<Long>() {

            @Override
            public Long call() throws Exception {
                return new Long(Subscriptions.createSubscription(mgr,
                        myCustomer.getOrganizationId(), p1_1.getProductId(),
                        "mySubId", marketplaceId, supp1, 1).getKey());
            }
        }).longValue();
    }

    protected static String getMarketplaceId(MarketplaceService service,
            long subscriptionKey) throws ObjectNotFoundException {
        VOMarketplace mpl = service
                .getMarketplaceForSubscription(subscriptionKey, null);
        return (mpl != null) ? mpl.getMarketplaceId() : null;
    }

    protected VOMarketplace buildMarketplace(String name, String marketplaceId,
            Organization ownerOrganization) {
        VOMarketplace marketplace = new VOMarketplace();
        marketplace.setName(name);
        marketplace.setMarketplaceId(marketplaceId);
        if (ownerOrganization != null)
            marketplace.setOwningOrganizationId(
                    ownerOrganization.getOrganizationId());
        return marketplace;
    }

    protected void checkDefaultLandingPage(final long mpKey) throws Exception {
        // check landing page
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Marketplace marketplace = mgr.getReference(Marketplace.class,
                        mpKey);
                PublicLandingpage landingpage = marketplace
                        .getPublicLandingpage();

                // verify default settings
                assertEquals(PublicLandingpage.DEFAULT_NUMBERSERVICES,
                        landingpage.getNumberServices());
                assertEquals(PublicLandingpage.DEFAULT_FILLINCRITERION,
                        landingpage.getFillinCriterion());
                assertEquals(0, landingpage.getLandingpageProducts().size());
                return null;
            }
        });
    }

    /*
     * 2 Emails must be issued the reciepients must be different the sequence
     * does not matter
     */
    protected void verifyEmaiRecipients() {
        // verify that 2 mails have been issued
        assertEquals("number of notificationMails ", 2, mailCounter);
        // verify that admin1 and admin2 have been emailed
        assertEquals("type of email 1 ", EmailType.MARKETPLACE_OWNER_ASSIGNED,
                emailType1);
        assertEquals("type of email 2", EmailType.MARKETPLACE_OWNER_ASSIGNED,
                emailType2);
        // verify the recipients of the emails
        assertFalse(emailRecipient1.equals(emailRecipient2));
        assertTrue(admin1.equals(emailRecipient1)
                || admin1.equals(emailRecipient2));
        assertTrue(admin2.equals(emailRecipient1)
                || admin2.equals(emailRecipient2));
    }

    /**
     * @return
     * @throws OperationNotPermittedException
     * @throws ObjectNotFoundException
     * @throws ValidationException
     * @throws UserRoleAssignmentException
     * @throws Exception
     */
    protected VOMarketplace givenMarketplaceWithEnterpriseLandingpage()
            throws OperationNotPermittedException, ObjectNotFoundException,
            ValidationException, UserRoleAssignmentException, Exception {
        final VOMarketplace voMarketplace = marketplaceService
                .createMarketplace(
                        buildMarketplace("TEST_MP_NAME", "TEST_MP_ID", null));
        assertNotNull(voMarketplace);

        // Switch to enterprise landingpage
        switchLandingpage(voMarketplace.getMarketplaceId());

        final VOMarketplace enterpriseMarketplace = marketplaceService
                .getMarketplaceById(voMarketplace.getMarketplaceId());
        return enterpriseMarketplace;
    }

    protected Void switchLandingpage(final String marketplaceId)
            throws Exception {
        return runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Marketplace marketplace = (Marketplace) mgr
                        .find(new Marketplace(marketplaceId));

                // remove old landing page
                marketplace.setPublicLandingpage(null);
                mgr.remove(marketplace.getPublicLandingpage());

                // create new landing page
                EnterpriseLandingpage newLandingPage = new EnterpriseLandingpage();
                newLandingPage.setMarketplace(marketplace);
                mgr.persist(newLandingPage);
                marketplace.setEnterpiseLandingpage(newLandingPage);
                return null;
            }
        });
    }

    protected Organization getSupp1WithinTransaction() throws Exception {
        Organization updatedOrg = runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization org = Organizations.findOrganization(mgr,
                        supp1.getOrganizationId());
                load(org);
                return org;
            }
        });
        return updatedOrg;
    }

    protected void resetEmailNotificationResults() throws Exception {
        this.emailType1 = null;
        this.emailType2 = null;
        this.emailRecipient1 = null;
        this.emailRecipient2 = null;
        this.mailCounter = 0;
    }

    protected boolean hasSupp1MarketplaceOwnerRole() throws Exception {
        Organization supplierWithMp = getSupp1WithinTransaction();
        return supplierWithMp.hasRole(OrganizationRoleType.MARKETPLACE_OWNER);
    }

    protected void createLocalizedResourceEntries(final long marketplaceKey)
            throws Exception {

        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {

                localizer.storeLocalizedResource("de_DE", marketplaceKey,
                        LocalizedObjectTypes.SHOP_MESSAGE_PROPERTIES, "p=123");

                localizer.storeLocalizedResource("de_DE", marketplaceKey,
                        LocalizedObjectTypes.MARKETPLACE_STAGE, "testStage");

                return null;
            }
        });

    }

    protected Marketplace setPublishedServicesToInactive() throws Exception {
        return runTX(new Callable<Marketplace>() {
            @Override
            public Marketplace call() throws Exception {
                Marketplace mp = Marketplaces.findMarketplace(mgr,
                        LOCAL_MP_ID_SUPP1);
                // mp.getCatalogEntries() set inactive
                for (CatalogEntry catalogEntry : mp.getCatalogEntries()) {
                    if (catalogEntry.getProduct().getStatus()
                            .equals(ServiceStatus.ACTIVE)) {
                        catalogEntry.getProduct()
                                .setStatus(ServiceStatus.INACTIVE);
                        mgr.persist(catalogEntry.getProduct());
                    }
                }
                mp.getOrganization().getOrganizationId();
                return mp;
            }
        });
    }

    protected void grantSupplier1AccessToGlobalMp() throws Exception {
        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                Marketplace mp = mgr.getReference(Marketplace.class,
                        mpGlobal.getKey());
                Marketplaces.grantPublishing(
                        mgr.getReference(Organization.class, supp1.getKey()),
                        mp, mgr, false);
                return null;
            }
        });
    }

    /**
     * Asserts that the given product has neither incoming nor outgoing product
     * references.
     */
    protected void assertNotInUpgradePath(final VOService svc)
            throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Product p = mgr.getReference(Product.class, svc.getKey());
                assertNotNull(p);
                assertTrue(p.getCompatibleProducts().isEmpty());
                assertTrue(p.getCompatibleProductsTarget().isEmpty());
                return null;
            }
        });
        assertFalse(
                "Service " + svc.getServiceId() + " is part of an upgrade path",
                provisioningService.isPartOfUpgradePath(svc));
    }

    /**
     * Assert that the catalog entry of the given product has niether broker
     * revenue share nor reseller revenue share.
     */
    protected void assertPartnerPriceModelsOfCatalogEntryRemoved(
            final VOService svc, final RevenueShareModel brokerRevenueShare,
            final RevenueShareModel resellerRevenueShare) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Product p = mgr.getReference(Product.class, svc.getKey());
                CatalogEntry ce = p.getCatalogEntries().get(0);
                assertNull(ce.getBrokerPriceModel());
                assertNull(ce.getResellerPriceModel());
                try {
                    mgr.getReference(RevenueShareModel.class,
                            brokerRevenueShare.getKey());
                    fail();
                } catch (ObjectNotFoundException e) {
                    // correct
                }
                try {
                    mgr.getReference(RevenueShareModel.class,
                            resellerRevenueShare.getKey());
                    fail();
                } catch (ObjectNotFoundException e) {
                    // correct
                }
                return null;
            }
        });
    }

    /**
     * Assert that the catalog entry of the given product has both of broker
     * revenue share and reseller revenue share.
     */
    protected void assertPartnerPriceModelsOfCatalogEntryExist(
            final VOService svc) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Product p = mgr.getReference(Product.class, svc.getKey());
                CatalogEntry ce = p.getCatalogEntries().get(0);
                assertNotNull(ce.getBrokerPriceModel());
                assertNotNull(ce.getResellerPriceModel());
                return null;
            }
        });
    }

    protected Organization givenBroker() throws Exception {
        return runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                return Organizations.createOrganization(mgr,
                        OrganizationRoleType.BROKER);
            }
        });
    }

    protected Organization givenReseller() throws Exception {
        return runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                return Organizations.createOrganization(mgr,
                        OrganizationRoleType.RESELLER);
            }
        });
    }

    protected void createRelationWithPublishingAccess(
            final Marketplace marketplace, final Organization org)
            throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                MarketplaceToOrganization mto = new MarketplaceToOrganization(
                        marketplace, org,
                        PublishingAccess.PUBLISHING_ACCESS_GRANTED);
                mgr.persist(mto);
                mgr.flush();
                return null;
            }
        });
    }

    protected MarketplaceToOrganization findMarketplaceToOrganization(
            final Marketplace marketplace, final Organization org)
            throws Exception {
        MarketplaceToOrganization mto = runTX(
                new Callable<MarketplaceToOrganization>() {
                    @Override
                    public MarketplaceToOrganization call() throws Exception {
                        MarketplaceToOrganization mto = new MarketplaceToOrganization(
                                marketplace, org);
                        return (MarketplaceToOrganization) mgr.find(mto);
                    }
                });
        return mto;
    }

    protected boolean hasUserRole(PlatformUser marketplaceOwner) {
        Set<RoleAssignment> roles = marketplaceOwner.getAssignedRoles();
        if (roles != null && roles.size() > 0)
            for (RoleAssignment assignedRole : roles) {
                if (assignedRole.getRole().getRoleName()
                        .equals(UserRoleType.MARKETPLACE_OWNER)) {
                    return true;
                }
            }
        return false;
    }

}
