/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: walker                                                  
 *                                                                              
 *  Creation Date: 16.05.2011                                                      
 *                                                                              
 *  Completion Time:  16.05.2011                                               
 *                                                                              
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.bean;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.oscm.accountservice.assembler.OrganizationAssembler;
import org.oscm.accountservice.local.AccountServiceLocal;
import org.oscm.accountservice.local.MarketingPermissionServiceLocal;
import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.*;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.i18nservice.local.ImageResourceServiceLocal;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.identityservice.local.LdapSettingsManagementServiceLocal;
import org.oscm.internal.intf.MarketplaceService;
import org.oscm.internal.intf.ServiceProvisioningService;
import org.oscm.internal.intf.SubscriptionService;
import org.oscm.internal.types.enumtypes.*;
import org.oscm.internal.types.exception.*;
import org.oscm.internal.vo.*;
import org.oscm.landingpageService.local.LandingpageServiceLocal;
import org.oscm.marketplaceservice.local.MarketplaceServiceLocal;
import org.oscm.provisioning.data.User;
import org.oscm.subscriptionservice.bean.SubscriptionListServiceBean;
import org.oscm.subscriptionservice.bean.SubscriptionServiceBean;
import org.oscm.taskhandling.local.TaskQueueServiceLocal;
import org.oscm.tenantprovisioningservice.bean.TenantProvisioningServiceBean;
import org.oscm.tenantprovisioningservice.vo.TenantProvisioningResult;
import org.oscm.test.StaticEJBTestBase;
import org.oscm.test.data.*;
import org.oscm.test.stubs.*;
import org.oscm.triggerservice.local.TriggerMessage;
import org.oscm.triggerservice.local.TriggerProcessMessageData;

import javax.ejb.EJBException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Callable;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

/**
 * Tests the retrieval of services
 * 
 */
@SuppressWarnings("boxing")
@Ignore
public class ServiceProvisioningServiceBeanGetServiceIT
        extends StaticEJBTestBase {

    private static ServiceProvisioningService sps;
    private static DataService ds;
    private static MarketplaceService mps;
    private static SubscriptionService ss;

    private static LocalizerServiceLocal localizer;
    private static Organization provider;
    private static String providerUserKey;
    private static String providerOrgId;

    private static Organization supplier1;
    private static Marketplace mpSupplier1;
    private static String supplierOrgId1;
    private static String supplierUserKey1;

    private static String supplierUserKey2;

    private static Organization customer;
    private static String customerUserKey;
    private static String customerOrgId;

    private static String customerUserKey2;

    private static Organization mpOwner;
    private static String mpOwnerUserKey;
    private static String mpOwnerAdminKey;

    private static List<VOServiceDetails> baseServices;
    private static List<VOServiceDetails> suspendedServices;
    private static HashSet<String> suspendedServiceIds;

    private static List<VOServiceDetails> serviceListSet1;
    private static final String FUJITSU = "FUJITSU";
    private static int tpCnt = 0;

    private static ServiceStatus newServiceStatus;
    private static VOServiceDetails voProduct;

    private static OrganizationReference orgRef;
    private static OrganizationReference orgRef2;

    @BeforeClass
    public static void setupOnce() throws Exception {

        baseServices = new ArrayList<>();
        suspendedServices = new ArrayList<>();
        suspendedServiceIds = new HashSet<>();

        container.enableInterfaceMocking(true);
        container.login("1");
        container.addBean(new DataServiceBean());
        container.addBean(mock(ImageResourceServiceLocal.class));
        container.addBean(new ConfigurationServiceStub() {
            @Override
            public ConfigurationSetting getConfigurationSetting(
                    ConfigurationKey informationId, String contextId) {
                if (ConfigurationKey.TAGGING_MIN_SCORE.equals(informationId)) {
                    return new ConfigurationSetting(
                            ConfigurationKey.TAGGING_MIN_SCORE, "global", "1");
                }
                return super.getConfigurationSetting(informationId, contextId);
            }

        });
        container.addBean(mock(LocalizerServiceLocal.class));
        localizer = container.get(LocalizerServiceLocal.class);
        container.addBean(new ApplicationServiceStub() {

            @Override
            public void validateCommunication(TechnicalProduct techProduct)
                    throws TechnicalServiceNotAliveException {

            }

            @Override
            public User[] createUsers(Subscription subscription,
                    List<UsageLicense> usageLicenses)
                    throws TechnicalServiceNotAliveException,
                    TechnicalServiceOperationException {
                List<User> users = new ArrayList<>();
                for (UsageLicense ul : usageLicenses) {
                    User user = new User();
                    user.setApplicationUserId(ul.getUser().getUserId());
                    user.setUserId(ul.getUser().getUserId());
                    users.add(user);
                }
                return users.toArray(new User[0]);
            }

            @Override
            public void saveAttributes(Subscription subscription)
                    throws TechnicalServiceNotAliveException,
                    TechnicalServiceOperationException {
            }

        });
        container.addBean(new SessionServiceStub());
        container.addBean(new IdentityServiceStub());
        container.addBean(mock(TaskQueueServiceLocal.class));
        container.addBean(new TriggerQueueServiceStub() {
            @Override
            public List<TriggerProcessMessageData> sendSuspendingMessages(
                    List<TriggerMessage> messageData) {
                List<TriggerProcessMessageData> result = new ArrayList<>();
                for (TriggerMessage m : messageData) {
                    TriggerProcess tp = new TriggerProcess();
                    PlatformUser user = new PlatformUser();
                    user.setKey(Long.valueOf(supplierUserKey1));
                    tp.setUser(user);
                    TriggerProcessMessageData data = new TriggerProcessMessageData(
                            tp, m);
                    result.add(data);
                }

                return result;
            }

        });
        container.addBean(new TenantProvisioningServiceBean() {

            @Override
            public TenantProvisioningResult createProductInstance(
                    Subscription subscription)
                    throws TechnicalServiceNotAliveException,
                    TechnicalServiceOperationException {
                TenantProvisioningResult result = new TenantProvisioningResult();
                result.setAsyncProvisioning(false);
                result.setProductInstanceId("productInstanceId");
                return result;
            }

        });

        container.addBean(new ServiceProvisioningServiceLocalizationBean());

        container.addBean(new CommunicationServiceStub());
        container.addBean(mock(LandingpageServiceLocal.class));
        container.addBean(mock(SubscriptionListServiceBean.class));
        container.addBean(new SubscriptionServiceBean());
        container.addBean(new LdapAccessServiceStub());
        container.addBean(new PaymentServiceStub());
        container.addBean(mock(MarketingPermissionServiceLocal.class));
        container.addBean(mock(LdapSettingsManagementServiceLocal.class));
        container.addBean(mock(AccountServiceLocal.class));
        container.addBean(new TagServiceBean());
        container.addBean(new CategorizationServiceStub() {
            @Override
            public boolean updateAssignedCategories(CatalogEntry catalogEntry,
                    List<VOCategory> categories) {
                return false;
            }
        });
        container.addBean(mock(MarketplaceServiceLocal.class));
        container.addBean(new MarketplaceServiceStub());
        container.addBean(new ServiceProvisioningServiceBean());
        container.addBean(mock(MarketplaceService.class));

        setUpDirServerStub(container.get(ConfigurationServiceLocal.class));
        sps = container.get(ServiceProvisioningService.class);
        ds = container.get(DataService.class);
        mps = container.get(MarketplaceService.class);
        ss = container.get(SubscriptionService.class);

        provider = runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization organization = Organizations.createOrganization(ds,
                        OrganizationRoleType.TECHNOLOGY_PROVIDER,
                        OrganizationRoleType.SUPPLIER);
                PlatformUser user = Organizations.createUserForOrg(ds,
                        organization, true, "admin");

                providerUserKey = String.valueOf(user.getKey());
                return organization;
            }
        });
        providerOrgId = provider.getOrganizationId();

        supplier1 = runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization organization = Organizations.createOrganization(ds,
                        OrganizationRoleType.SUPPLIER);
                PlatformUser user = Organizations.createUserForOrg(ds,
                        organization, true, "admin");
                supplierUserKey1 = String.valueOf(user.getKey());
                Organization provider = Organizations.findOrganization(ds,
                        providerOrgId);
                mpSupplier1 = Marketplaces.ensureMarketplace(organization,
                        organization.getOrganizationId(), ds);
                orgRef = Organizations
                        .createSupplierToTechnologyProviderReference(ds,
                                provider, organization);
                return organization;
            }
        });
        supplierOrgId1 = supplier1.getOrganizationId();

        runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization organization = Organizations.createOrganization(ds,
                        OrganizationRoleType.SUPPLIER);
                PlatformUser user = Organizations.createUserForOrg(ds,
                        organization, true, "admin");
                supplierUserKey2 = String.valueOf(user.getKey());
                Organization provider = Organizations.findOrganization(ds,
                        providerOrgId);
                orgRef2 = Organizations
                        .createSupplierToTechnologyProviderReference(ds,
                                provider, organization);
                return organization;
            }
        });

        customer = runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization supplier = Organizations.findOrganization(ds,
                        supplierOrgId1);
                Organization organization = Organizations.createCustomer(ds,
                        supplier);
                PlatformUser user = Organizations.createUserForOrg(ds,
                        organization, true, "admin");
                customerUserKey = String.valueOf(user.getKey());
                return organization;
            }
        });
        customerOrgId = customer.getOrganizationId();

        runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization supplier = Organizations.findOrganization(ds,
                        supplierOrgId1);
                Organization organization = Organizations.createCustomer(ds,
                        supplier);
                PlatformUser user = Organizations.createUserForOrg(ds,
                        organization, true, "admin");
                customerUserKey2 = String.valueOf(user.getKey());
                return organization;
            }
        });

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                mpOwner = Organizations.createOrganization(ds,
                        OrganizationRoleType.PLATFORM_OPERATOR);
                Marketplace mp = Marketplaces.createMarketplace(mpOwner,
                        FUJITSU, false, ds);

                Organization supplier = ds.getReference(Organization.class,
                        supplier1.getKey());
                PlatformUser user2 = ds.getReference(PlatformUser.class,
                        Long.parseLong(supplierUserKey2));
                Organization supplier2 = user2.getOrganization();
                Marketplaces.grantPublishing(supplier, mp, ds, false);
                Marketplaces.grantPublishing(supplier2, mp, ds, false);

                PlatformUser user = Organizations.createUserForOrg(ds, mpOwner,
                        true, "admin");
                PlatformUsers.grantRoles(ds, user,
                        UserRoleType.MARKETPLACE_OWNER);
                mpOwnerAdminKey = String.valueOf(user.getKey());

                user = Organizations.createUserForOrg(ds, mpOwner, false,
                        "user");
                mpOwnerUserKey = String.valueOf(user.getKey());

                Marketplace mp2 = Marketplaces.createMarketplace(mpOwner, "EST",
                        false, ds);
                Marketplaces.grantPublishing(supplier, mp2, ds, false);
                Marketplaces.grantPublishing(supplier2, mp2, ds, false);

                return null;
            }
        });

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                createSupportedCurrencies(ds);
                createPaymentTypes(ds);
                createOrganizationRoles(ds);
                SupportedCountries.createSomeSupportedCountries(ds);
                return null;
            }
        });

        setupData();

    }

    private static VOServiceDetails createProduct(
            VOTechnicalService technicalProduct, String id,
            ServiceProvisioningService serviceProvisioning) throws Exception {
        VOService product = new VOService();
        product.setServiceId(id);
        return serviceProvisioning.createService(technicalProduct, product,
                null);
    }

    private static VOOrganization getOrganizationForOrgId(
            final String organizationId) throws Exception {
        return runTX(new Callable<VOOrganization>() {
            @Override
            public VOOrganization call() throws Exception {
                LocalizerFacade facade = new LocalizerFacade(localizer, "en");
                Organization result = new Organization();
                result.setOrganizationId(organizationId);
                result = (Organization) ds.find(result);
                return OrganizationAssembler.toVOOrganization(result, false,
                        facade);
            }
        });
    }

    /**
     * Creates a technical product with the passed id.
     * 
     * @param tpName
     *            the id of the technical product to create
     * 
     * @return the vo of the newly created tp.
     */
    private static VOTechnicalService createTechnicalProduct(
            final String tpName, final boolean oneSubscription)
            throws Exception {
        container.login(providerUserKey, ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Organization prov = ds.getReference(Organization.class,
                        provider.getKey());
                TechnicalProduct tp = TechnicalProducts.createTechnicalProduct(
                        ds, prov, tpName, false, ServiceAccessType.LOGIN);
                tp.setOnlyOneSubscriptionAllowed(oneSubscription);
                return null;
            }
        });
        VOTechnicalService techProduct = sps
                .getTechnicalServices(OrganizationRoleType.TECHNOLOGY_PROVIDER)
                .get(tpCnt);
        tpCnt++;
        container.logout();
        return techProduct;
    }

    /**
     * Creates a single service which is based ion the passed technical service.
     * 
     * @param tp
     *            technical service the service should be based on.
     * @return the create service.
     */
    private static VOServiceDetails createSingleService(String supplierUserKey,
            VOTechnicalService tp) throws Exception {
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        VOServiceDetails voProduct = createProduct(tp, "single_product2_"
                + tp.getTechnicalServiceId() + "_" + supplierUserKey, sps);
        VOPriceModel priceModel = new VOPriceModel();
        voProduct = sps.savePriceModel(voProduct, priceModel);

        VOMarketplace voMarketplace = new VOMarketplace();
        voMarketplace.setMarketplaceId("FUJITSU");

        publishServiceToMarketplace(voProduct, voMarketplace, true, true);

        container.logout();
        return voProduct;
    }

    private static void publishServiceToMarketplace(VOService svc,
            VOMarketplace mp, boolean anonymousVisible,
            boolean visibleInCatalog)
            throws ObjectNotFoundException, NonUniqueBusinessKeyException,
            ValidationException, OperationNotPermittedException {
        VOCatalogEntry ce = new VOCatalogEntry();
        ce.setService(svc);
        ce.setMarketplace(mp);
        ce.setAnonymousVisible(anonymousVisible);
        ce.setVisibleInCatalog(visibleInCatalog);
        mps.publishService(svc, Arrays.asList(ce));
    }

    /**
     * Creates a set of services which have a different visibility in the mpl
     * context
     * 
     * @param tp
     *            the technical product the services are based on.
     * @return one service of the set of services which all are based on the
     *         same technical product.
     */
    private static List<VOServiceDetails> createServiceSet(
            VOTechnicalService tp) throws Exception {
        container.login(supplierUserKey1, ROLE_SERVICE_MANAGER);
        VOServiceDetails voProduct1 = createProduct(tp,
                "product1_" + tp.getTechnicalServiceId(), sps);
        VOServiceDetails voProduct2 = createProduct(tp,
                "product2_" + tp.getTechnicalServiceId(), sps);
        VOServiceDetails voProduct3 = createProduct(tp,
                "product3_" + tp.getTechnicalServiceId(), sps);
        VOServiceDetails voProduct4 = createProduct(tp,
                "product4_" + tp.getTechnicalServiceId(), sps);
        VOServiceDetails voProduct5 = createProduct(tp,
                "product5_" + tp.getTechnicalServiceId(), sps);
        VOServiceDetails voProduct6 = createProduct(tp,
                "product6_" + tp.getTechnicalServiceId(), sps);
        VOServiceDetails voProduct7 = createProduct(tp,
                "product7_" + tp.getTechnicalServiceId(), sps);
        VOServiceDetails voProduct8 = createProduct(tp,
                "product8_" + tp.getTechnicalServiceId(), sps);
        VOServiceDetails voProduct9 = createProduct(tp,
                "product9_" + tp.getTechnicalServiceId(), sps);
        VOServiceDetails voProduct10 = createProduct(tp,
                "product10_" + tp.getTechnicalServiceId(), sps);
        VOServiceDetails voProduct11 = createProduct(tp,
                "product11_" + tp.getTechnicalServiceId(), sps);
        VOServiceDetails voProduct12 = createProduct(tp,
                "product12_" + tp.getTechnicalServiceId(), sps);
        VOServiceDetails voProduct13 = createProduct(tp,
                "product13_" + tp.getTechnicalServiceId(), sps);
        VOServiceDetails voProduct14 = createProduct(tp,
                "product14_" + tp.getTechnicalServiceId(), sps);
        VOServiceDetails voProduct15 = createProduct(tp,
                "product15_" + tp.getTechnicalServiceId(), sps);
        VOServiceDetails voProductOBSOLETE = createProduct(tp,
                "productOBSOLETE_" + tp.getTechnicalServiceId(), sps);
        VOServiceDetails voProductDELETED = createProduct(tp,
                "productDELETED_" + tp.getTechnicalServiceId(), sps);
        VOServiceDetails voProductSUSPENDED = createProduct(tp,
                "productSUSPENDED_" + tp.getTechnicalServiceId(), sps);
        VOServiceDetails voProductOBSOLETE2 = createProduct(tp,
                "productOBSOLETE2_" + tp.getTechnicalServiceId(), sps);
        VOServiceDetails voProductDELETED2 = createProduct(tp,
                "productDELETED2_" + tp.getTechnicalServiceId(), sps);
        VOServiceDetails voProductSUSPENDED2 = createProduct(tp,
                "productSUSPENDED2_" + tp.getTechnicalServiceId(), sps);

        VOPriceModel priceModel = new VOPriceModel();
        sps.savePriceModel(voProduct1, priceModel);
        voProduct2 = sps.savePriceModel(voProduct2, priceModel);
        voProduct3 = sps.savePriceModel(voProduct3, priceModel);
        voProduct4 = sps.savePriceModel(voProduct4, priceModel);
        voProduct5 = sps.savePriceModel(voProduct5, priceModel);
        voProduct6 = sps.savePriceModel(voProduct6, priceModel);
        voProduct7 = sps.savePriceModel(voProduct7, priceModel);
        voProduct8 = sps.savePriceModel(voProduct8, priceModel);
        voProduct9 = sps.savePriceModel(voProduct9, priceModel);
        voProduct10 = sps.savePriceModel(voProduct10, priceModel);
        voProduct11 = sps.savePriceModel(voProduct11, priceModel);
        voProduct12 = sps.savePriceModel(voProduct12, priceModel);
        voProduct13 = sps.savePriceModel(voProduct13, priceModel);
        voProduct14 = sps.savePriceModel(voProduct14, priceModel);
        voProduct15 = sps.savePriceModel(voProduct15, priceModel);
        voProductOBSOLETE = sps.savePriceModel(voProductOBSOLETE, priceModel);
        voProductOBSOLETE2 = sps.savePriceModel(voProductOBSOLETE2, priceModel);
        voProductDELETED = sps.savePriceModel(voProductDELETED, priceModel);
        voProductDELETED2 = sps.savePriceModel(voProductDELETED2, priceModel);
        voProductSUSPENDED = sps.savePriceModel(voProductSUSPENDED, priceModel);
        voProductSUSPENDED2 = sps.savePriceModel(voProductSUSPENDED2,
                priceModel);

        VOOrganization org = getOrganizationForOrgId(customerOrgId);
        VOPriceModel pm = new VOPriceModel();
        sps.savePriceModelForCustomer(voProduct3, pm, org);
        VOServiceDetails voProductCust4 = sps
                .savePriceModelForCustomer(voProduct4, pm, org);
        VOServiceDetails voProductCust5 = sps
                .savePriceModelForCustomer(voProduct5, pm, org);
        VOServiceDetails voProductCust6 = sps
                .savePriceModelForCustomer(voProduct6, pm, org);
        VOServiceDetails voProductCust7 = sps
                .savePriceModelForCustomer(voProduct7, pm, org);
        VOServiceDetails voProductCust8 = sps
                .savePriceModelForCustomer(voProduct8, pm, org);
        VOServiceDetails voProductCust9 = sps
                .savePriceModelForCustomer(voProduct9, pm, org);
        VOServiceDetails voProductCust10 = sps
                .savePriceModelForCustomer(voProduct10, pm, org);
        VOServiceDetails voProductCust14 = sps
                .savePriceModelForCustomer(voProduct14, pm, org);
        VOServiceDetails voProductCust15 = sps
                .savePriceModelForCustomer(voProduct15, pm, org);
        VOServiceDetails voProductCustOBSOLETE = sps
                .savePriceModelForCustomer(voProductOBSOLETE, pm, org);
        VOServiceDetails voProductCustDELETED = sps
                .savePriceModelForCustomer(voProductDELETED, pm, org);
        VOServiceDetails voProductCustSUSPENDED = sps
                .savePriceModelForCustomer(voProductSUSPENDED, pm, org);

        VOMarketplace voMarketplaceLocal = new VOMarketplace();
        voMarketplaceLocal.setMarketplaceId(mpSupplier1.getMarketplaceId());
        publishServiceToMarketplace(voProduct2, voMarketplaceLocal, true, true);

        publishServiceToMarketplace(voProduct5, voMarketplaceLocal, true, true);

        publishServiceToMarketplace(voProduct6, voMarketplaceLocal, true, true);

        publishServiceToMarketplace(voProduct14, voMarketplaceLocal, true,
                true);

        VOMarketplace voMarketplace = new VOMarketplace();
        voMarketplace.setMarketplaceId("FUJITSU");

        publishServiceToMarketplace(voProduct7, voMarketplace, true, true);

        publishServiceToMarketplace(voProduct8, voMarketplace, false, true);

        publishServiceToMarketplace(voProduct9, voMarketplace, true, false);

        publishServiceToMarketplace(voProduct10, voMarketplace, false, false);

        publishServiceToMarketplace(voProductCust7, voMarketplace, true, true);

        VOMarketplace voMarketplace2 = new VOMarketplace();
        voMarketplace2.setMarketplaceId("EST");

        publishServiceToMarketplace(voProduct11, voMarketplace2, true, true);

        publishServiceToMarketplace(voProduct12, voMarketplace, true, true);

        publishServiceToMarketplace(voProduct13, voMarketplace, true, false);

        publishServiceToMarketplace(voProduct15, voMarketplace, true, true);

        publishServiceToMarketplace(voProductOBSOLETE, voMarketplace, true,
                true);

        publishServiceToMarketplace(voProductOBSOLETE2, voMarketplace, true,
                true);

        publishServiceToMarketplace(voProductCustOBSOLETE, voMarketplace, true,
                true);

        publishServiceToMarketplace(voProductDELETED, voMarketplace, true,
                true);

        publishServiceToMarketplace(voProductDELETED2, voMarketplace, true,
                true);

        publishServiceToMarketplace(voProductCustDELETED, voMarketplace, true,
                true);

        publishServiceToMarketplace(voProductSUSPENDED, voMarketplace, true,
                true);

        publishServiceToMarketplace(voProductSUSPENDED2, voMarketplace, true,
                true);

        publishServiceToMarketplace(voProductCustSUSPENDED, voMarketplace, true,
                true);

        sps.activateService(voProduct2);
        sps.activateService(voProduct5);
        sps.activateService(voProduct6);
        sps.activateService(voProduct7);
        sps.activateService(voProduct8);
        sps.activateService(voProduct10);
        sps.activateService(voProductCust4);
        sps.activateService(voProductCust6);
        VOService voActiveCustProduct7 = sps.activateService(voProductCust7);
        sps.activateService(voProductCust8);
        sps.activateService(voProductCust9);
        sps.activateService(voProductCust10);
        sps.activateService(voProduct11);
        VOService voActiveProduct13 = sps.activateService(voProduct13);
        sps.activateService(voProduct14);
        sps.activateService(voProduct15);
        setServiceStatus(ServiceStatus.OBSOLETE, voProductCustOBSOLETE);
        setServiceStatus(ServiceStatus.OBSOLETE, voProductOBSOLETE);
        setServiceStatus(ServiceStatus.OBSOLETE, voProductOBSOLETE2);
        sps.deleteService(voProductCustDELETED);
        sps.deleteService(voProductDELETED);
        sps.deleteService(voProductDELETED2);
        setServiceStatus(ServiceStatus.SUSPENDED, voProductCustSUSPENDED);
        setServiceStatus(ServiceStatus.SUSPENDED, voProductSUSPENDED);
        setServiceStatus(ServiceStatus.SUSPENDED, voProductSUSPENDED2);

        container.logout();

        suspendedServices.add(voProductSUSPENDED);
        suspendedServiceIds.add(voProductSUSPENDED.getServiceId());
        suspendedServices.add(voProductSUSPENDED2);
        suspendedServiceIds.add(voProductSUSPENDED2.getServiceId());
        suspendedServices.add(voProductCustSUSPENDED);
        suspendedServiceIds.add(voProductCustSUSPENDED.getServiceId());

        // Create a subscription for the public/active CSS
        VOSubscription voSubscriptionCustProduct7 = createSubscription(
                voActiveCustProduct7);

        // Create a subscription for the public/active service
        VOSubscription voSubscriptionProduct13 = createSubscription(
                voActiveProduct13);

        List<VOServiceDetails> list = new ArrayList<>();
        list.add(voProduct1); // [0] not active (no price model)

        list.add(voProduct2); // [1] active, price model defined

        list.add(voProduct3); // [2] not active, price model for CSS defined but
                              // not
                              // in vo

        list.add(voProduct4); // [3] not active, template for CSS
                              // (voProductCust4),
                              // CSS active,

        list.add(voProduct5); // [4] active, template CSS (voProductCust5), CSS
                              // not
                              // active

        list.add(voProduct6); // [5] active, template CSS (voProductCust6), CSS
                              // active

        list.add(voProduct7); // [6] active, template CSS (voProductCust7), CSS
                              // active, visible, in catalog

        list.add(voProduct8); // [7] active, template CSS (voProductCust8), CSS
                              // active, not visible, in catalog

        list.add(voProduct9); // [8] not active, template CSS (voProductCust9),
                              // CSS
                              // active, visible, not in catalog

        list.add(voProduct10); // [9] active, template CSS (voProductCust107),
                               // CSS
                               // active, not visible, not in catalog

        list.add(voProduct11); // [10] active, visible, in catalog, different
                               // MPL,

        list.add(voProductCust4); // [11] active, CSS for voProduct4

        list.add(voProductCust6); // [12] active, CSS for voProduct6

        list.add(voProductCust7); // [13] active, CSS for voProduct7, visible,
                                  // in
                                  // catalog

        list.add(voProductCust8); // [14] active, CSS for voProduct8, no catalog
                                  // entry

        list.add(voProductCust9); // [15] active, CSS for voProduct9

        list.add(voProductCust10); // [16] active, CSS for voProduct10

        list.add(voProduct12); // [17] not active , visible, in catalog,

        list.add(voProduct13); // [18] not active , visible, not in catalog,

        VOServiceDetails copyOfVoProduct7 = new VOServiceDetails();
        copyOfVoProduct7.setKey(
                getProductCopyKey(voSubscriptionCustProduct7).longValue());
        list.add(copyOfVoProduct7); // [19] subscription copy of cust product7
                                    // (should never be visible)

        VOServiceDetails copyOfVoProduct13 = new VOServiceDetails();
        copyOfVoProduct13
                .setKey(getProductCopyKey(voSubscriptionProduct13).longValue());
        list.add(copyOfVoProduct13); // [20] subscription copy of product 12
                                     // (should
                                     // never be visible)

        list.add(voProductCust5); // [21] not active, CSS for voProduct5,
                                  // voProduct5 active

        list.add(voProduct14); // [22] active but no CE, CSS exists
                               // (voProductCust14)
                               // which is not active

        list.add(voProductCust14); // [23] not active (voProductCust14), CSS for
                                   // voProduct14 which is active but not on the
                                   // MPL

        list.add(voProduct15); // [24] active with CE, CSS exists
                               // (voProductCust15) which is not active

        list.add(voProductCust15); // [25] not active (voProductCust15), CSS for
                                   // voProduct15 which is active and on the MPL

        return list;
    }

    private static VOSubscription createSubscription(VOService service)
            throws Exception {
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);

        VOSubscription voSubscription = new VOSubscription();
        voSubscription.setSubscriptionId("newSubscription" + service.getKey());
        voSubscription.setServiceKey(service.getKey());

        VOSubscription subscription = ss.subscribeToService(voSubscription,
                service, null, null, null, new ArrayList<VOUda>());

        container.logout();
        return subscription;
    }

    /**
     * Returns the key of the product which was created through a subbscription.
     */
    private static Long getProductCopyKey(final VOSubscription voSubscription)
            throws Exception {
        return runTX(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                Subscription subscription = ds.getReference(Subscription.class,
                        voSubscription.getKey());
                return Long.valueOf(subscription.getProduct().getKey());
            }
        });
    }

    /**
     * Init common test data.
     */
    private static void setupData() throws Exception {
        VOTechnicalService techProduct = createTechnicalProduct("tp11", false);
        VOTechnicalService techProduct2 = createTechnicalProduct("tp12", false);
        VOTechnicalService techProduct3 = createTechnicalProduct("tp13", false);
        VOTechnicalService techProduct4 = createTechnicalProduct("tp14", false);

        createMarketingPermission(techProduct.getKey(), orgRef.getKey());
        createMarketingPermission(techProduct2.getKey(), orgRef.getKey());
        createMarketingPermission(techProduct3.getKey(), orgRef.getKey());
        createMarketingPermission(techProduct4.getKey(), orgRef.getKey());

        // Service set based on tp11
        serviceListSet1 = createServiceSet(techProduct);
        baseServices.add(serviceListSet1.get(1));

        // Service set based on tp12
        baseServices.add(createServiceSet(techProduct2).get(1));

        // Single service (no related products)
        VOServiceDetails singleService1 = createSingleService(supplierUserKey1,
                techProduct3);
        container.login(supplierUserKey1, ROLE_SERVICE_MANAGER);
        sps.activateService(singleService1);
        container.logout();
        baseServices.add(singleService1);

        // Create a service which is based on the same technical product but
        // defined from a different supplier. this one should be not on the
        // result
        createMarketingPermission(techProduct.getKey(), orgRef2.getKey());
        VOServiceDetails singleService2 = createSingleService(supplierUserKey2,
                techProduct);
        container.login(supplierUserKey2, ROLE_SERVICE_MANAGER);
        sps.activateService(singleService2);
        container.logout();
        baseServices.add(singleService2);
        baseServices.add(createServiceSet(techProduct4).get(13));
    }

    private static void createMarketingPermission(final long tpKey,
            final long orgRefKey) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                TechnicalProduct technicalProduct = ds
                        .find(TechnicalProduct.class, tpKey);
                OrganizationReference reference = ds
                        .find(OrganizationReference.class, orgRefKey);

                MarketingPermission permission = new MarketingPermission();
                permission.setOrganizationReference(reference);
                permission.setTechnicalProduct(technicalProduct);
                ds.persist(permission);
                return null;
            }
        });
    }

    private static void setServiceStatus(ServiceStatus status,
            VOServiceDetails voServiceDetails) throws Exception {

        newServiceStatus = status;
        voProduct = voServiceDetails;

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {

                // BusinessKey(attributes={"productId", "supplierKey"})
                Product product = new Product();
                product.setProductId(voProduct.getServiceId());// productId
                product.setVendorKey(voProduct.getSellerKey());// organizationKey
                product = Product.class
                        .cast(ds.getReferenceByBusinessKey(product));
                product.setStatus(newServiceStatus);

                return null;
            }
        });
    }

    /**
     * good case (customer login)
     */
    @Test
    public void testGetRelatedServicesForMarketplace_ok() throws Exception {
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);

        List<VOService> products = sps.getRelatedServicesForMarketplace(
                baseServices.get(0), "FUJITSU", null);
        Assert.assertEquals(5, products.size());

        // product 1 - 6 not published
        Assert.assertEquals("product7_tp11", products.get(0).getServiceId());
        Assert.assertEquals("product8_tp11", products.get(1).getServiceId());

        // Product 9 is INACTIVE but is the template of a customer specific
        // product so it appears in the list
        Assert.assertEquals("product9_tp11", products.get(2).getServiceId());
        Assert.assertEquals("product10_tp11", products.get(3).getServiceId());
        // product 11 other MP
        // product 12 not active
        Assert.assertEquals("product13_tp11", products.get(4).getServiceId());
        // product 14 not published
        // product 15 customer specific price model, but no customer product
        // activation?

        // Different technical product same result
        products = sps.getRelatedServicesForMarketplace(baseServices.get(1),
                "FUJITSU", null);
        Assert.assertEquals(5, products.size());
        Assert.assertEquals("product7_tp12", products.get(0).getServiceId());
        Assert.assertEquals("product8_tp12", products.get(1).getServiceId());
        Assert.assertEquals("product9_tp12", products.get(2).getServiceId());
        Assert.assertEquals("product10_tp12", products.get(3).getServiceId());
        Assert.assertEquals("product13_tp12", products.get(4).getServiceId());
    }

    /**
     * good case (public services)
     */
    @Test
    public void testGetRelatedServicesForMarketplace_public() throws Exception {
        container.logout();
        List<VOService> products = sps.getRelatedServicesForMarketplace(
                baseServices.get(0), "FUJITSU", "EN");
        Assert.assertEquals(3, products.size());
        Assert.assertEquals("product7_tp11", products.get(0).getServiceId());
        Assert.assertEquals("product13_tp11", products.get(1).getServiceId());
        Assert.assertEquals("product15_tp11", products.get(2).getServiceId());

        // Different technical product same result
        products = sps.getRelatedServicesForMarketplace(baseServices.get(1),
                "FUJITSU", "EN");
        Assert.assertEquals(3, products.size());
        Assert.assertEquals("product7_tp12", products.get(0).getServiceId());
        Assert.assertEquals("product13_tp12", products.get(1).getServiceId());
        Assert.assertEquals("product15_tp12", products.get(2).getServiceId());
    }

    /**
     * public call with null as locale
     */
    @Test(expected = EJBException.class)
    public void testGetRelatedServicesForMarketplace_publicNoLocale()
            throws Exception {
        container.logout();
        sps.getRelatedServicesForMarketplace(baseServices.get(0), "FUJITSU",
                null);
    }

    /**
     * public call with a invalid locale
     */
    @Test
    public void testGetRelatedServicesForMarketplace_publicInvLocale()
            throws Exception {
        container.logout();
        List<VOService> products = sps.getRelatedServicesForMarketplace(
                baseServices.get(0), "FUJITSU", "INVALID");
        Assert.assertEquals(3, products.size());
        Assert.assertEquals("product7_tp11", products.get(0).getServiceId());
        Assert.assertEquals("product13_tp11", products.get(1).getServiceId());
        Assert.assertEquals("product15_tp11", products.get(2).getServiceId());
    }

    /**
     * The passed service doesn't exists
     */
    @Test(expected = ObjectNotFoundException.class)
    public void testGetRelatedServicesForMarketplace_serviceNotFound()
            throws Exception {
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        sps.getRelatedServicesForMarketplace(new VOService(), "FUJITSU", null);
    }

    /**
     * pass null as service
     */
    @Test(expected = EJBException.class)
    public void testGetRelatedServicesForMarketplace_null1() throws Exception {
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        sps.getRelatedServicesForMarketplace(null, "FUJITSU", null);
    }

    /**
     * pass null as mpl id
     */
    @Test(expected = EJBException.class)
    public void testGetRelatedServicesForMarketplace_null2() throws Exception {
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        sps.getRelatedServicesForMarketplace(new VOService(), null, null);
    }

    /**
     * voProduct5 pass null for all parameter
     * 
     */
    @Test(expected = EJBException.class)
    public void testGetRelatedServicesForMarketplace_null3() throws Exception {
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        sps.getRelatedServicesForMarketplace(null, null, null);
    }

    /**
     * Execute with unknown mpl
     */
    @Test
    public void testGetRelatedServicesForMarketplace_unknownMpl()
            throws Exception {
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        List<VOService> products = sps.getRelatedServicesForMarketplace(
                baseServices.get(0), "UNKNOWN", null);
        Assert.assertEquals(0, products.size());
    }

    /**
     * Check if there are no related services for the passed service.
     */
    @Test
    public void testGetRelatedServicesForMarketplace_noRelatedServices()
            throws Exception {
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        List<VOService> products = sps.getRelatedServicesForMarketplace(
                baseServices.get(2), "FUJITSU", null);
        Assert.assertEquals(0, products.size());
    }

    /**
     * Related products for a custumer specific product.
     */
    @Test
    public void testGetRelatedServicesForMarketplace_custSpecific()
            throws Exception {
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        List<VOService> products = sps.getRelatedServicesForMarketplace(
                baseServices.get(4), "FUJITSU", "EN");
        Assert.assertEquals(4, products.size());
        Assert.assertEquals("product8_tp14", products.get(0).getServiceId());
        Assert.assertEquals("product9_tp14", products.get(1).getServiceId());
        Assert.assertEquals("product10_tp14", products.get(2).getServiceId());
        Assert.assertEquals("product13_tp14", products.get(3).getServiceId());
    }

    /**
     * Test visibility of services if no user is logged in.
     */
    @Test
    public void testGetServiceFormarketplace_anonymousVisibility()
            throws Exception {
        container.logout();

        // Defined the services which should be returned by the service call
        boolean[] isVisible = new boolean[serviceListSet1.size()];
        Arrays.fill(isVisible, false);

        // Operation not permitted exception expected
        boolean[] onpExceptionExpected = new boolean[serviceListSet1.size()];
        Arrays.fill(onpExceptionExpected, false);

        long[] expectedServiceKey = new long[serviceListSet1.size()];
        Arrays.fill(expectedServiceKey, -1);

        // [0] voProduct1 not active (no price model)
        // [1] voProduct2 active, price model defined
        // [2] voProduct3 not active, price model for CSS defined but not in vo
        // [3] voProduct4 not active, template for CSS (voProductCust4),CSS
        // active,
        // [4] voProduct5 active, template CSS (voProductCust5), CSS not active
        // [5] voProduct6 active, template CSS (voProductCust5), CSS active
        // [6] voProduct7 active, template CSS (voProductCust7), CSS active,
        // visible, in catalog
        isVisible[6] = true;
        // [7] voProduct8 active, template CSS (voProductCust8), CSS active, not
        // visible, in catalog
        // [8] voProduct9 not active, template CSS (voProductCust9), CSS active,
        // visible, not in catalog
        // [9] voProduct10 active, template CSS (voProductCust10), CSS active,
        // not visible, not in catalog
        // [10] voProduct11 active, visible, in catalog, different MPL,
        // [11] voProductCust4 active, CSS for voProduct4
        onpExceptionExpected[11] = true; // anonymous to access to CSS
        // [12] voProductCust6 active, CSS for voProduct6
        onpExceptionExpected[12] = true; // anonymous to access to CSS
        // [13] voProductCust7 active, CSS for voProduct7, visible, in catalog
        onpExceptionExpected[13] = true; // anonymous to access to CSS
        // [14] voProductCust8 active, CSS for voProduct8,
        onpExceptionExpected[14] = true; // anonymous to access to CSS
        // [15] voProductCust9 active, CSS for voProduct9,
        onpExceptionExpected[15] = true; // anonymous to access to CSS
        // [16] voProductCust10 active, CSS for voProduct10,
        onpExceptionExpected[16] = true; // anonymous to access to CSS
        // [17] voProduct12 not active, visible, in catalog
        // [18] voProduct13 active, visible, not in catalog
        isVisible[18] = true;

        // [19] subscription copy of voCustProduct7 (should not be visible)
        onpExceptionExpected[19] = true; // access to product copy
        // [20] subscription copy of voProduct12 (should not be visible)
        onpExceptionExpected[20] = true; // access to product copy
        // [21] not active, CSS for voProduct5, voProduct5 active
        onpExceptionExpected[21] = true;
        // [22] active, CSS exists (voProductCust14) which is not active
        // [23] not active, CSS for voProduct14 which is active
        onpExceptionExpected[23] = true;
        // [24] active with CE, CSS exists (voProductCust15) which is not active
        isVisible[24] = true;
        // [25] not active (voProductCust15), CSS for voProduct15 which is
        // active and on the MPL
        onpExceptionExpected[25] = true;

        assertServiceSet(serviceListSet1, isVisible, onpExceptionExpected,
                expectedServiceKey);
    }

    /**
     * Checks if the passed service set against the three lists. Test for
     * "getServiceForMarketplace"
     * 
     * @param serviceSet
     *            the list of services to check.
     * @param isVisible
     *            must be the same length as serviceSet. Defines if the service
     *            will be returned by "getServiceForMarketplace".
     * @param onpExceptionExpected
     *            must be the same length as serviceSet. Defines if a operation
     *            not permitted exception is excepted.
     * @param expectedServiceKey
     *            must be the same length as serviceSet. Defines the excepted
     *            key if the service is visible.
     * @throws Exception
     */
    @SuppressWarnings("null")
    private void assertServiceSet(List<VOServiceDetails> serviceSet,
            boolean[] isVisible, boolean[] onpExceptionExpected,
            long[] expectedServiceKey) throws Exception {

        Assert.assertEquals(serviceSet.size(), isVisible.length);
        Assert.assertEquals(serviceSet.size(), onpExceptionExpected.length);
        Assert.assertEquals(serviceSet.size(), expectedServiceKey.length);

        for (int i = 0; i < isVisible.length; i++) {
            try {
                VOService product = sps.getServiceForMarketplace(
                        Long.valueOf(serviceSet.get(i).getKey()), "FUJITSU",
                        "EN");

                Assert.assertFalse(
                        "Operation not permitted exception expected for index "
                                + i,
                        onpExceptionExpected[i]);

                Assert.assertEquals(
                        "service with index " + i
                                + " should not have been returned",
                        isVisible[i], product != null);

                if (isVisible[i]) {
                    Assert.assertNotNull(product);
                    if (expectedServiceKey[i] < 0) {
                        // no cust specific services => same keys
                        Assert.assertEquals("Worng key for index " + i,
                                serviceSet.get(i).getKey(), product.getKey());
                    } else {
                        // CSP key expected
                        Assert.assertEquals("Worng key for index " + i,
                                expectedServiceKey[i], product.getKey());
                    }
                }
            } catch (OperationNotPermittedException onp) {
                if (!onpExceptionExpected[i]) {
                    Assert.fail(
                            "OperationNotPermittedException was not excpeted for index "
                                    + i);
                }
            }
        }
    }

    /**
     * Checking that the service is only returned for the correct marketplace.
     */
    @Test
    public void testGetServiceFormarketplace_wrongMarketplace()
            throws Exception {
        container.logout(); // run anonymously
        Long key = Long.valueOf(serviceListSet1.get(6).getKey());
        VOService product = sps.getServiceForMarketplace(key, "FUJITSU", "EN");
        assertNotNull(product);
        product = sps.getServiceForMarketplace(key, "EST", "EN");
        assertNull(product);
    }

    /**
     * Test getServicesForMarketPlace if a target customer is logged in.
     */
    @Test
    public void testGetServiceFormarketplace_targetCustomerVisibility()
            throws Exception {
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        // Defined the services which should be returned by the service call
        boolean[] isVisible = new boolean[serviceListSet1.size()];
        Arrays.fill(isVisible, false);

        // Operation not permitted exception expected
        boolean[] onpExceptionExpected = new boolean[serviceListSet1.size()];
        Arrays.fill(onpExceptionExpected, false);

        long[] expectedServiceKey = new long[serviceListSet1.size()];
        Arrays.fill(expectedServiceKey, -1);

        // [0] voProduct1 not active (no price model)
        // [1] voProduct2 active, price model defined
        // [2] voProduct3 not active, price model for CSS defined but not in vo
        // [3] voProduct4 not active, template for CSS (voProductCust4),CSS
        // active,
        // [4] voProduct5 active, template CSS (voProductCust5), CSS not active
        // [5] voProduct6 active, template CSS (voProductCust6), CSS active
        // [6] voProduct7 active, template CSS (voProductCust7), CSS active,
        // visible, in catalog
        isVisible[6] = true;
        // key of CSP expected
        expectedServiceKey[6] = serviceListSet1.get(13).getKey();

        // [7] voProduct8 active, template CSS (voProductCust8), CSS active, not
        // visible, in catalog
        isVisible[7] = true;
        expectedServiceKey[7] = serviceListSet1.get(14).getKey();

        // [8] voProduct9 not active, template CSS (voProductCust9), CSS active,
        // visible, not in catalog
        isVisible[8] = true;
        expectedServiceKey[8] = serviceListSet1.get(15).getKey();

        // [9] voProduct10 active, template CSS (voProductCust10), CSS active,
        // not visible, not in catalog
        isVisible[9] = true;
        expectedServiceKey[9] = serviceListSet1.get(16).getKey();

        // [10] voProduct11 active, visible, in catalog, different MPL,
        // [11] voProductCust4 active, CSS for voProduct4
        // [12] voProductCust6 active, CSS for voProduct6
        // [13] voProductCust7 active, CSS for voProduct7, visible, in catalog
        isVisible[13] = true;
        // [14] voProductCust8 active, CSS for voProduct8,
        isVisible[14] = true;
        // [15] voProductCust9 active, CSS for voProduct9,
        isVisible[15] = true;
        // [16] voProductCust10 active, CSS for voProduct10,
        isVisible[16] = true;
        // [17] voProduct12 not active, visible, in catalog
        // [18] voProduct13 active, visible, not in catalog
        isVisible[18] = true;
        // [19] subscription copy of voCustProduct7 (should not be visible)
        onpExceptionExpected[19] = true; // access to product copy
        // [20] subscription copy of voProduct12 (should not be visible)
        onpExceptionExpected[20] = true; // access to product copy
        // [21] not active, CSS for voProduct5, voProduct5 active
        // [22] active but no CE, CSS exists (voProductCust14) which is active
        // [23] not active, CSS for voProduct14 which is active
        // [24] active with CE, CSS exists (voProductCust15) which is not active
        // [25] not active (voProductCust15), CSS for voProduct15 which is
        // active and on the MPL

        assertServiceSet(serviceListSet1, isVisible, onpExceptionExpected,
                expectedServiceKey);
        container.logout();
    }

    /**
     * Test getServicesForMarketPlace if a non target customer is logged in.
     */
    @Test
    public void testGetServiceFormarketplace_nonTargetCustomerVisibility()
            throws Exception {
        container.login(customerUserKey2, ROLE_ORGANIZATION_ADMIN);
        // Defined the services which should be returned by the service call
        boolean[] isVisible = new boolean[serviceListSet1.size()];
        Arrays.fill(isVisible, false);

        // Operation not permitted exception expected
        boolean[] onpExceptionExpected = new boolean[serviceListSet1.size()];
        Arrays.fill(onpExceptionExpected, false);

        long[] expectedServiceKey = new long[serviceListSet1.size()];
        Arrays.fill(expectedServiceKey, -1);

        // [0] voProduct1 not active (no price model)
        // [1] voProduct2 active, price model defined
        // [2] voProduct3 not active, price model for CSS defined but not in vo
        // [3] voProduct4 not active, template for CSS (voProductCust4),CSS
        // active,
        // [4] voProduct5 active, template CSS (voProductCust5), CSS not active
        // [5] voProduct6 active, template CSS (voProductCust6), CSS active
        // [6] voProduct7 active, template CSS (voProductCust7), CSS active,
        // visible, in catalog
        isVisible[6] = true;
        // [7] voProduct8 active, template CSS (voProductCust8), CSS active, not
        // visible, in catalog
        isVisible[7] = true;
        // [8] voProduct9 not active, template CSS (voProductCust9), CSS active,
        // visible, not in catalog
        // [9] voProduct10 active, template CSS (voProductCust10), CSS active,
        // not visible, not in catalog
        isVisible[9] = true;
        // [10] voProduct11 active, visible, in catalog, different MPL,
        // [11] voProductCust4 active, CSS for voProduct4
        onpExceptionExpected[11] = true; // access to foreign CSP
        // [12] voProductCust6 active, CSS for voProduct6
        onpExceptionExpected[12] = true; // access to foreign CSP
        // [13] voProductCust7 active, CSS for voProduct7, visible, in catalog
        onpExceptionExpected[13] = true; // access to foreign CSP
        // [14] voProductCust8 active, CSS for voProduct8,
        onpExceptionExpected[14] = true; // access to foreign CSP
        // [15] voProductCust9 active, CSS for voProduct9,
        onpExceptionExpected[15] = true; // access to foreign CSP
        // [16] voProductCust10 active, CSS for voProduct10,
        onpExceptionExpected[16] = true; // access to foreign CSP
        // [17] voProduct12 not active, visible, in catalog
        // [18] voProduct13 active, visible, not in catalog
        isVisible[18] = true;
        // [19] subscription copy of voCustProduct7 (should not be visible)
        onpExceptionExpected[19] = true; // access to product copy
        // [20] subscription copy of voProduct12 (should not be visible)
        onpExceptionExpected[20] = true; // access to product copy
        // [21] not active, CSS for voProduct5, voProduct5 active
        onpExceptionExpected[21] = true; // access to foreign CSP
        // [22] active but no CE, CSS exists (voProductCust14) which is active
        // [23] not active, CSS for voProduct14 which is active
        onpExceptionExpected[23] = true; // access to foreign CSP
        // [24] active with CE, CSS exists (voProductCust15) which is not active
        isVisible[24] = true;
        // [25] not active (voProductCust15), CSS for voProduct15 which is
        // active and on the MPL
        onpExceptionExpected[25] = true; // access to foreign CSP

        assertServiceSet(serviceListSet1, isVisible, onpExceptionExpected,
                expectedServiceKey);
        container.logout();
    }

    @Test(expected = EJBException.class)
    public void testGetServiceFormarketplace_nullLocale_NoUser()
            throws Exception {
        container.logout();
        sps.getServiceForMarketplace(
                Long.valueOf(serviceListSet1.get(18).getKey()), "FUJITSU",
                null);
    }

    @Test
    public void testGetServiceFormarketplace_nullLocale_loggedinUser()
            throws Exception {
        container.login(customerUserKey2, ROLE_ORGANIZATION_ADMIN);
        sps.getServiceForMarketplace(
                Long.valueOf(serviceListSet1.get(18).getKey()), "FUJITSU",
                null);
    }

    @Test
    public void testGetServiceFormarketplace_hasOneSubscription_login_False()
            throws Exception {
        container.login(customerUserKey2, ROLE_ORGANIZATION_ADMIN);
        VOServiceEntry se = sps.getServiceForMarketplace(
                Long.valueOf(serviceListSet1.get(18).getKey()), "FUJITSU",
                null);
        Assert.assertFalse(se.isSubscriptionLimitReached());
    }

    @Test
    public void testGetServiceFormarketplace_hasOneSubscription_login_false()
            throws Exception {
        VOTechnicalService techProduct = createTechnicalProduct("tp_noonetime",
                false);
        createMarketingPermission(techProduct.getKey(), orgRef.getKey());
        VOServiceDetails oneTimeService = createSingleService(supplierUserKey1,
                techProduct);
        container.login(supplierUserKey1, ROLE_SERVICE_MANAGER);
        VOService activeService = sps.activateService(oneTimeService);
        container.logout();
        createSubscription(activeService);

        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        VOServiceEntry se = sps.getServiceForMarketplace(
                Long.valueOf(oneTimeService.getKey()), "FUJITSU", null);
        Assert.assertFalse(se.isSubscriptionLimitReached());
    }

    @Test
    public void testGetServiceFormarketplace_hasOneSubscription_login_true2()
            throws Exception {
        VOTechnicalService techProduct = createTechnicalProduct("tp_onetime1",
                true);
        createMarketingPermission(techProduct.getKey(), orgRef.getKey());
        VOServiceDetails oneTimeService = createSingleService(supplierUserKey1,
                techProduct);
        container.login(supplierUserKey1, ROLE_SERVICE_MANAGER);
        VOService activeService = sps.activateService(oneTimeService);
        container.logout();
        createSubscription(activeService);

        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        VOServiceEntry se = sps.getServiceForMarketplace(
                Long.valueOf(oneTimeService.getKey()), "FUJITSU", null);
        Assert.assertTrue(se.isSubscriptionLimitReached());
    }

    @Test
    public void testGetServiceFormarketplace_hasOneSubscription_login_true3()
            throws Exception {
        VOTechnicalService techProduct = createTechnicalProduct("tp_onetime2",
                true);
        createMarketingPermission(techProduct.getKey(), orgRef.getKey());
        VOServiceDetails oneTimeService = createSingleService(supplierUserKey1,
                techProduct);
        container.login(supplierUserKey1, ROLE_SERVICE_MANAGER);
        sps.activateService(oneTimeService);
        container.logout();

        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        VOServiceEntry se = sps.getServiceForMarketplace(
                Long.valueOf(oneTimeService.getKey()), "FUJITSU", null);
        Assert.assertFalse(se.isSubscriptionLimitReached());
    }

    @Test
    public void testGetServiceFormarketplace_hasOneSubscription_anonymous()
            throws Exception {
        container.logout();
        VOServiceEntry se = sps.getServiceForMarketplace(
                Long.valueOf(serviceListSet1.get(18).getKey()), "FUJITSU",
                "EN");
        Assert.assertFalse(se.isSubscriptionLimitReached());
    }

    /**
     * Login as MP owner admin. Must see suspended services.
     */
    @Test
    public void testGetRelatedServicesForMarketplace_MpAdmin()
            throws Exception {
        container.login(mpOwnerAdminKey, ROLE_PLATFORM_OPERATOR,
                ROLE_ORGANIZATION_ADMIN);

        List<VOService> products = sps.getRelatedServicesForMarketplace(
                baseServices.get(0), "FUJITSU", null);

        Assert.assertNotNull(products);
        Assert.assertTrue("The list of products must not be empty",
                products.size() > 0);

        HashSet<String> foundIds = new HashSet<>();
        for (VOService prod : products) {
            if (prod.getStatus() == ServiceStatus.SUSPENDED)
                foundIds.add(prod.getServiceId());
        }
        Assert.assertEquals(2, foundIds.size());
        Assert.assertTrue("wrong service id(s)",
                suspendedServiceIds.containsAll(foundIds));
    }

    /**
     * login as MP owner user. Must not see suspended services.
     */
    @Test
    public void testGetRelatedServicesForMarketplace_MpUser() throws Exception {
        container.login(mpOwnerUserKey, ROLE_PLATFORM_OPERATOR);

        List<VOService> products = sps.getRelatedServicesForMarketplace(
                baseServices.get(0), "FUJITSU", null);

        Assert.assertNotNull(products);
        Assert.assertTrue("The list of products must not be empty",
                products.size() > 0);

        for (VOService prod : products) {
            Assert.assertTrue("MP owner user must not see suspended services",
                    prod.getStatus() != ServiceStatus.SUSPENDED);
        }
    }

    /**
     * Login as MP owner admin. Must see suspended services.
     */
    @Test
    public void testGetServicesForMarketplace_MpAdmin() throws Exception {
        container.login(mpOwnerAdminKey, ROLE_PLATFORM_OPERATOR,
                ROLE_ORGANIZATION_ADMIN);

        List<VOService> products = sps.getServicesForMarketplace(FUJITSU);

        Assert.assertNotNull(products);
        Assert.assertTrue("The list of products must not be empty",
                products.size() > 0);

        HashSet<String> foundIds = new HashSet<>();
        for (VOService prod : products) {
            if (prod.getStatus() == ServiceStatus.SUSPENDED)
                foundIds.add(prod.getServiceId());
        }
        Assert.assertEquals(6, foundIds.size());
        Assert.assertTrue("wrong service id(s)",
                suspendedServiceIds.containsAll(foundIds));
    }

    /**
     * login as MP owner user. Must not see suspended services.
     */
    @Test
    public void testGetServicesForMarketplace_MpUser() throws Exception {
        container.login(mpOwnerUserKey, ROLE_PLATFORM_OPERATOR);

        List<VOService> products = sps.getServicesForMarketplace(FUJITSU);

        Assert.assertNotNull(products);
        Assert.assertTrue("The list of products must not be empty",
                products.size() > 0);

        for (VOService prod : products) {
            Assert.assertTrue("MP owner user must not see suspended services",
                    prod.getStatus() != ServiceStatus.SUSPENDED);
        }
    }

    /**
     * Login as MP owner admin. Must see suspended services.
     */
    @Test
    public void testGetServiceForMarketplace_MpAdmin() throws Exception {
        container.login(mpOwnerAdminKey, ROLE_PLATFORM_OPERATOR,
                ROLE_ORGANIZATION_ADMIN);

        VOServiceEntry entry = sps.getServiceForMarketplace(
                Long.valueOf(suspendedServices.get(0).getKey()), FUJITSU, null);
        Assert.assertEquals("productSUSPENDED_tp11", entry.getServiceId());

        entry = sps.getServiceForMarketplace(
                Long.valueOf(suspendedServices.get(1).getKey()), FUJITSU, null);
        Assert.assertEquals("productSUSPENDED2_tp11", entry.getServiceId());
    }

    /**
     * Login as MP owner admin. Must see suspended services. No access to
     * customer specific product.
     */
    @Test(expected = OperationNotPermittedException.class)
    public void testGetServiceForMarketplace_MpAdmin_ex() throws Exception {
        container.login(mpOwnerAdminKey, ROLE_PLATFORM_OPERATOR,
                ROLE_ORGANIZATION_ADMIN);

        sps.getServiceForMarketplace(
                Long.valueOf(suspendedServices.get(2).getKey()), FUJITSU, null);
    }

    /**
     * login as MP owner user. Must not see suspended services.
     */
    @Test
    public void testGetServiceForMarketplace_MpUser() throws Exception {
        container.login(mpOwnerUserKey, ROLE_PLATFORM_OPERATOR);

        VOServiceEntry entry = sps.getServiceForMarketplace(
                Long.valueOf(suspendedServices.get(0).getKey()), FUJITSU, null);
        Assert.assertNull(entry);

        entry = sps.getServiceForMarketplace(
                Long.valueOf(suspendedServices.get(1).getKey()), FUJITSU, null);
        Assert.assertNull(entry);
    }

    /**
     * login as MP owner user. Must not see suspended services. No access to
     * customer specific product.
     */
    @Test(expected = OperationNotPermittedException.class)
    public void testGetServiceForMarketplace_MpUser_ex() throws Exception {
        container.login(mpOwnerUserKey, ROLE_PLATFORM_OPERATOR);

        sps.getServiceForMarketplace(
                Long.valueOf(suspendedServices.get(2).getKey()), FUJITSU, null);
    }
}
