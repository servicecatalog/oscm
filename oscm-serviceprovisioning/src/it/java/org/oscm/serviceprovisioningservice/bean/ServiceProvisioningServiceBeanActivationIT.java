/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Soehnges                                             
 *                                                                              
 *  Creation Date: 17.05.2011                                                      
 *                                                                              
 *  Completion Time: 17.05.2011                                              
 *                                                                              
 *******************************************************************************/
package org.oscm.serviceprovisioningservice.bean;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.oscm.accountservice.assembler.OrganizationAssembler;
import org.oscm.accountservice.local.AccountServiceLocal;
import org.oscm.accountservice.local.MarketingPermissionServiceLocal;
import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.*;
import org.oscm.domobjects.enums.BillingAdapterIdentifier;
import org.oscm.domobjects.enums.OrganizationReferenceType;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.i18nservice.local.ImageResourceServiceLocal;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.identityservice.local.IdentityServiceLocal;
import org.oscm.identityservice.local.LdapSettingsManagementServiceLocal;
import org.oscm.internal.intf.MarketplaceService;
import org.oscm.internal.intf.ServiceProvisioningService;
import org.oscm.internal.intf.TagService;
import org.oscm.internal.intf.TriggerService;
import org.oscm.internal.types.enumtypes.*;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.types.exception.*;
import org.oscm.internal.types.exception.TechnicalServiceNotAliveException.Reason;
import org.oscm.internal.vo.*;
import org.oscm.landingpageService.local.LandingpageServiceLocal;
import org.oscm.marketplace.assembler.MarketplaceAssembler;
import org.oscm.marketplaceservice.local.MarketplaceServiceLocal;
import org.oscm.serviceprovisioningservice.assembler.ProductAssembler;
import org.oscm.subscriptionservice.local.SubscriptionServiceLocal;
import org.oscm.tenantprovisioningservice.bean.TenantProvisioningServiceBean;
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.*;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.*;
import org.oscm.triggerservice.bean.TriggerQueueServiceBean;
import org.oscm.triggerservice.bean.TriggerServiceBean;
import org.oscm.triggerservice.local.TriggerMessage;
import org.oscm.triggerservice.local.TriggerProcessMessageData;
import org.oscm.triggerservice.local.TriggerServiceLocal;
import org.oscm.types.constants.Configuration;

import javax.ejb.EJBAccessException;
import javax.ejb.EJBException;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.Callable;

import static org.mockito.Mockito.mock;
@Ignore
public class ServiceProvisioningServiceBeanActivationIT extends EJBTestBase {

    private static final String EXAMPLE_ENTERPRISE = "EXAMPLE Enterprise";
    private static final String EXAMPLE_PROFESSIONAL = "EXAMPLE Professional";
    private static final String EXAMPLE_STARTER = "EXAMPLE Starter";
    private static final String EXAMPLE_TRIAL = "EXAMPLE Trial";

    private ServiceProvisioningService svcProv;
    private DataService mgr;
    private LocalizerServiceLocal localizer;
    private MarketplaceService mpProv;
    private TagService tagService;
    private TriggerService triggerService;
    private TriggerServiceLocal triggerServiceLocal;

    private final Map<String, VOService> COMPARE_VALUES = new HashMap<String, VOService>();

    private String providerOrgId;
    private String supplierOrgId;
    private String customerOrgId;

    private String operatorUserKey;
    private String providerUserKey;
    private String supplierUserKey;

    protected boolean appNotAlive;

    private Organization provider;
    private Organization supplier;
    private Organization customer;
    private OrganizationReference orgRef;

    private VOMarketplace localMarketplace;
    private VOMarketplace globalMarketplace;
    private VOMarketplace globalMarketplace2;

    private static final String EUR = "EUR";

    private boolean calledTriggerQueueServiceForSuspendingNtfx = false;

    @Override
    public void setup(TestContainer container) throws Exception {

        container.enableInterfaceMocking(true);
        container.login("1");
        container.addBean(new DataServiceBean());
        container.addBean(new TriggerQueueServiceStub());
        container.addBean(new ConfigurationServiceStub());
        container.addBean(mock(LocalizerServiceLocal.class));
        container.addBean(new ServiceProvisioningServiceLocalizationBean());
        container.addBean(mock(ImageResourceServiceLocal.class));
        container.addBean(new ApplicationServiceStub() {
            @Override
            public void validateCommunication(TechnicalProduct techProduct)
                    throws TechnicalServiceNotAliveException {
                if (appNotAlive) {
                    throw new TechnicalServiceNotAliveException(
                            Reason.CONNECTION_REFUSED);
                }
            }
        });
        container.addBean(new SessionServiceStub() {
            @Override
            public boolean hasTechnicalProductActiveSessions(
                    long technicalProductKey) {
                return false;
            }
        });
        container.addBean(mock(SubscriptionServiceLocal.class));
        container.addBean(new CommunicationServiceStub());
        container.addBean(new LdapAccessServiceStub());
        container.addBean(new TaskQueueServiceStub());
        container.addBean(mock(LdapSettingsManagementServiceLocal.class));
        container.addBean(mock(IdentityServiceLocal.class));
        container.addBean(new PaymentServiceStub() {
            @Override
            public void deregisterPaymentInPSPSystem(PaymentInfo payment) {
            }
        });
        container.addBean(new TriggerQueueServiceBean() {
            @Override
            public List<TriggerProcessMessageData> sendSuspendingMessages(
                    List<TriggerMessage> messageData) {
                calledTriggerQueueServiceForSuspendingNtfx = true;

                return super.sendSuspendingMessages(messageData);
            }
        });
        container.addBean(mock(MarketingPermissionServiceLocal.class));
        container.addBean(mock(LdapSettingsManagementServiceLocal.class));
        container.addBean(mock(AccountServiceLocal.class));

        container.addBean(new TenantProvisioningServiceBean() {
            @Override
            public void deleteProductInstance(Subscription subscription) {
            }
        });
        container.addBean(new TagServiceBean());
        container.addBean(new MarketplaceServiceStub());
        container.addBean(mock(LandingpageServiceLocal.class));
        container.addBean(new ServiceProvisioningServiceBean());
        container.addBean(new CategorizationServiceStub() {
            @Override
            public boolean updateAssignedCategories(CatalogEntry catalogEntry,
                    List<VOCategory> categories) {
                return false;
            }
        });
        container.addBean(mock(MarketplaceServiceLocal.class));
        container.addBean(mock(MarketplaceService.class));
        container.addBean(new TriggerServiceBean());

        ConfigurationServiceLocal cfg = container
                .get(ConfigurationServiceLocal.class);
        setUpDirServerStub(cfg);

        cfg.setConfigurationSetting(new ConfigurationSetting(
                ConfigurationKey.TAGGING_MIN_SCORE,
                Configuration.GLOBAL_CONTEXT, "1"));

        svcProv = container.get(ServiceProvisioningService.class);
        mgr = container.get(DataService.class);
        localizer = container.get(LocalizerServiceLocal.class);
        mpProv = container.get(MarketplaceService.class);
        tagService = container.get(TagService.class);
        triggerService = container.get(TriggerService.class);
        triggerServiceLocal = container.get(TriggerServiceLocal.class);

        COMPARE_VALUES.put(EXAMPLE_TRIAL, getFreeProduct(EXAMPLE_TRIAL));
        COMPARE_VALUES.put(EXAMPLE_STARTER, getFreeProduct(EXAMPLE_STARTER));
        COMPARE_VALUES.put(EXAMPLE_PROFESSIONAL,
                getEnterprise(EXAMPLE_PROFESSIONAL));
        COMPARE_VALUES.put(EXAMPLE_ENTERPRISE,
                getEnterprise(EXAMPLE_ENTERPRISE));

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                SupportedCurrency sc = new SupportedCurrency();
                sc.setCurrency(Currency.getInstance(EUR));
                mgr.persist(sc);
                createPaymentTypes(mgr);
                createOrganizationRoles(mgr);
                SupportedCountries.createSomeSupportedCountries(mgr);
                BillingAdapters.createBillingAdapter(mgr,
                        BillingAdapterIdentifier.NATIVE_BILLING.toString(),
                        true);
                return null;
            }
        });

        provider = runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization organization = Organizations.createOrganization(
                        mgr, OrganizationRoleType.TECHNOLOGY_PROVIDER,
                        OrganizationRoleType.SUPPLIER);
                Marketplaces.ensureMarketplace(organization,
                        organization.getOrganizationId(), mgr);
                PlatformUser user = Organizations.createUserForOrg(mgr,
                        organization, true, "admin");

                providerUserKey = String.valueOf(user.getKey());
                return organization;
            }
        });
        providerOrgId = provider.getOrganizationId();

        supplier = runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization organization = Organizations.createOrganization(
                        mgr, OrganizationRoleType.SUPPLIER);
                Marketplaces.ensureMarketplace(organization,
                        organization.getOrganizationId(), mgr);
                PlatformUser user = Organizations.createUserForOrg(mgr,
                        organization, true, "admin");
                supplierUserKey = String.valueOf(user.getKey());
                Organization provider = Organizations.findOrganization(mgr,
                        providerOrgId);
                orgRef = Organizations
                        .createSupplierToTechnologyProviderReference(mgr,
                                provider, organization);
                return organization;
            }
        });
        supplierOrgId = supplier.getOrganizationId();

        customer = runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization supplier = Organizations.findOrganization(mgr,
                        supplierOrgId);
                Organization organization = Organizations.createCustomer(mgr,
                        supplier);
                Organizations
                        .createUserForOrg(mgr, organization, true, "admin");
                return organization;
            }
        });
        customerOrgId = customer.getOrganizationId();
        container.login(providerUserKey, ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);

        // create marketplaces
        final Marketplace domLocalMarketplace = runTX(new Callable<Marketplace>() {
            @Override
            public Marketplace call() throws Exception {
                Marketplace mp = Marketplaces.createMarketplace(provider,
                        "LOCAL", false, mgr);
                Marketplaces
                        .grantPublishing(
                                mgr.getReference(Organization.class,
                                        provider.getKey()), mp, mgr, false);
                return mp;
            }
        });

        final Marketplace domGlobalMarketplace = runTX(new Callable<Marketplace>() {
            @Override
            public Marketplace call() throws Exception {
                Marketplace mp = Marketplaces.createMarketplace(provider,
                        "FUJITSU_1", false, mgr);
                Marketplaces
                        .grantPublishing(
                                mgr.getReference(Organization.class,
                                        provider.getKey()), mp, mgr, false);
                return mp;
            }
        });

        final Marketplace domGlobalMarketplace2 = runTX(new Callable<Marketplace>() {
            @Override
            public Marketplace call() throws Exception {
                Marketplace mp = Marketplaces.createMarketplace(provider,
                        "FUJITSU_2", false, mgr);
                Marketplaces
                        .grantPublishing(
                                mgr.getReference(Organization.class,
                                        provider.getKey()), mp, mgr, false);
                return mp;
            }
        });

        final LocalizerFacade facade = new LocalizerFacade(localizer, "en");
        localMarketplace = runTX(new Callable<VOMarketplace>() {
            @Override
            public VOMarketplace call() {
                return MarketplaceAssembler.toVOMarketplace(
                        domLocalMarketplace, facade);
            }
        });
        globalMarketplace = runTX(new Callable<VOMarketplace>() {
            @Override
            public VOMarketplace call() {
                return MarketplaceAssembler.toVOMarketplace(
                        domGlobalMarketplace, facade);
            }
        });
        globalMarketplace2 = runTX(new Callable<VOMarketplace>() {
            @Override
            public VOMarketplace call() {
                return MarketplaceAssembler.toVOMarketplace(
                        domGlobalMarketplace2, facade);
            }
        });
    }

    private void createOperator() throws Exception {
        runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization organization = Organizations.createOrganization(
                        mgr, OrganizationRoleType.PLATFORM_OPERATOR);
                PlatformUser user = Organizations.createUserForOrg(mgr,
                        organization, true, "admin");

                operatorUserKey = String.valueOf(user.getKey());
                return organization;
            }
        });
    }

    private static VOService getEnterprise(String id) {
        VOService product = new VOService();
        product.setServiceId(id);
        product.setName("");
        product.setDescription("");
        VOPriceModel priceModel = new VOPriceModel();
        priceModel.setDescription("");
        priceModel.setType(PriceModelType.PRO_RATA);
        priceModel.setPeriod(PricingPeriod.MONTH);
        priceModel.setPricePerPeriod(BigDecimal.valueOf(100));
        product.setPriceModel(priceModel);
        return product;
    }

    private static VOService getFreeProduct(String id) {
        VOService product = new VOService();
        product.setServiceId(id);
        product.setName("");
        product.setDescription("");
        VOPriceModel priceModel = new VOPriceModel();
        priceModel.setDescription("");
        priceModel.setType(PriceModelType.FREE_OF_CHARGE);
        product.setPriceModel(priceModel);
        return product;
    }

    private VOTechnicalService createTechnicalProduct(
            ServiceProvisioningService serviceProvisioning) throws Exception {
        VOTechnicalService voTechnicalService = createTechnicalProduct(
                serviceProvisioning, TECHNICAL_SERVICES_XML);
        return voTechnicalService;
    }

    private VOTechnicalService createTechnicalProduct(
            ServiceProvisioningService serviceProvisioning, String xml)
            throws Exception {
        String rc = serviceProvisioning.importTechnicalServices(xml
                .getBytes("UTF-8"));
        Assert.assertEquals("", rc);

        List<VOTechnicalService> technicalServices = serviceProvisioning
                .getTechnicalServices(OrganizationRoleType.TECHNOLOGY_PROVIDER);
        OrganizationReference orgRef2 = createOrgRef(provider.getKey());
        for (VOTechnicalService voTechnicalService : technicalServices) {
            createMarketingPermission(voTechnicalService.getKey(),
                    orgRef2.getKey());
            createMarketingPermission(voTechnicalService.getKey(),
                    orgRef.getKey());
        }

        return technicalServices.get(0);
    }

    private static VOServiceDetails createProduct(
            VOTechnicalService technicalProduct, String id,
            ServiceProvisioningService serviceProvisioning) throws Exception {
        VOService product = new VOService();
        product.setServiceId(id);
        return serviceProvisioning.createService(technicalProduct, product,
                null);
    }

    private VOPriceModel createPriceModel() {
        VOPriceModel priceModel = new VOPriceModel();
        return priceModel;
    }

    private VOOrganization getOrganizationForOrgId(final String organizationId)
            throws Exception {
        return runTX(new Callable<VOOrganization>() {
            @Override
            public VOOrganization call() {
                Organization result = new Organization();
                result.setOrganizationId(organizationId);
                result = (Organization) mgr.find(result);
                return OrganizationAssembler.toVOOrganization(result, false,
                        new LocalizerFacade(localizer, "en"));
            }
        });
    }

    private String createSubscription(final VOOrganization cust,
            final SubscriptionStatus status, final VOService product,
            final String subId, final VORoleDefinition role) throws Exception {
        return runTX(new Callable<String>() {

            @Override
            public String call() throws Exception {
                Organization customer = Organizations.findOrganization(mgr,
                        cust.getOrganizationId());
                Subscription subscription = Subscriptions.createSubscription(
                        mgr, customer.getOrganizationId(),
                        product.getServiceId(), subId, supplier);
                subscription.setStatus(status);
                if (role != null) {
                    RoleDefinition r = mgr.getReference(RoleDefinition.class,
                            role.getKey());
                    List<PlatformUser> users = customer.getPlatformUsers();
                    for (PlatformUser u : users) {
                        subscription.addUser(u, r);
                    }
                }
                return subscription.getSubscriptionId();
            }
        });
    }

    private VOServiceDetails publishServiceToGlobalMarketplace(
            VOServiceDetails product) throws ObjectNotFoundException,
            ValidationException, NonUniqueBusinessKeyException,
            OperationNotPermittedException {
        VOCatalogEntry entry = new VOCatalogEntry();
        entry.setMarketplace(globalMarketplace);
        return mpProv.publishService(product, Arrays.asList(entry));
    }

    private void createMarketingPermission(final long tpKey,
            final long orgRefKey) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                TechnicalProduct technicalProduct = mgr.find(
                        TechnicalProduct.class, tpKey);
                OrganizationReference reference = mgr.find(
                        OrganizationReference.class, orgRefKey);

                MarketingPermission permission = new MarketingPermission();
                permission.setOrganizationReference(reference);
                permission.setTechnicalProduct(technicalProduct);
                mgr.persist(permission);
                return null;
            }
        });
    }

    private OrganizationReference createOrgRef(final long orgKey)
            throws Exception {
        return runTX(new Callable<OrganizationReference>() {
            @Override
            public OrganizationReference call() throws Exception {
                Organization organization = mgr
                        .find(Organization.class, orgKey);
                OrganizationReference orgRef = new OrganizationReference(
                        organization,
                        organization,
                        OrganizationReferenceType.TECHNOLOGY_PROVIDER_TO_SUPPLIER);
                mgr.persist(orgRef);
                return orgRef;
            }
        });
    }

    @Test
    public void testActivate() throws Exception {
        VOTechnicalService techProduct = createTechnicalProduct(svcProv);
        VOServiceDetails product = createProduct(techProduct, "product",
                svcProv);

        VOPriceModel priceModel = createPriceModel();
        product = svcProv.savePriceModel(product, priceModel);

        product = publishServiceToGlobalMarketplace(product);

        Assert.assertEquals(ServiceStatus.INACTIVE, product.getStatus());
        VOService activatedProduct = svcProv.activateService(product);
        Assert.assertEquals(ServiceStatus.ACTIVE, activatedProduct.getStatus());
    }

    @Test
    public void testActivateAssertTPInvocation() throws Exception {
        VOTechnicalService techProduct = createTechnicalProduct(svcProv);
        VOServiceDetails product = createProduct(techProduct, "product",
                svcProv);

        VOPriceModel priceModel = createPriceModel();
        svcProv.savePriceModel(product, priceModel);

        product = publishServiceToGlobalMarketplace(product);

        Assert.assertEquals(ServiceStatus.INACTIVE, product.getStatus());
        svcProv.activateService(product);
        Assert.assertTrue(calledTriggerQueueServiceForSuspendingNtfx);
    }

    @Test
    public void testActivateInActiveState() throws Exception {
        VOTechnicalService techProduct = createTechnicalProduct(svcProv);
        VOServiceDetails product = createProduct(techProduct, "product",
                svcProv);

        VOPriceModel priceModel = createPriceModel();
        svcProv.savePriceModel(product, priceModel);

        product = publishServiceToGlobalMarketplace(product);

        svcProv.activateService(product);

        product = svcProv.getServiceDetails(product);
        Assert.assertEquals(ServiceStatus.ACTIVE, product.getStatus());
        svcProv.activateService(product);
        product = svcProv.getServiceDetails(product);
        Assert.assertEquals(ServiceStatus.ACTIVE, product.getStatus());
    }

    @Test(expected = ServiceOperationException.class)
    public void testActivateWithoutPriceModel() throws Exception {
        VOTechnicalService techProduct = createTechnicalProduct(svcProv);
        VOServiceDetails product = createProduct(techProduct, "product",
                svcProv);

        Assert.assertEquals(ServiceStatus.INACTIVE, product.getStatus());
        svcProv.activateService(product);
    }

    @Test(expected = TechnicalServiceNotAliveException.class)
    public void testActivateProductTechProdNotAlive() throws Exception {
        VOTechnicalService tp = createTechnicalProduct(svcProv);
        VOServiceDetails product = createProduct(tp, "test1", svcProv);

        VOPriceModel priceModel = createPriceModel();
        product = svcProv.savePriceModel(product, priceModel);
        publishServiceToGlobalMarketplace(product);

        appNotAlive = true;
        svcProv.activateService(product);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void testActivateProductNotOwner() throws Exception {
        VOTechnicalService tp = createTechnicalProduct(svcProv);
        VOServiceDetails product = createProduct(tp, "test1", svcProv);
        VOPriceModel priceModel = createPriceModel();
        product = svcProv.savePriceModel(product, priceModel);
        publishServiceToGlobalMarketplace(product);

        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        svcProv.activateService(product);
    }

    /**
     * When a marketplace gets deleted, the catalog entry is orphaned and the
     * service can therefore not be activated.
     */
    @Test(expected = ServiceNotPublishedException.class)
    public void testActivateProductNoMarketplace() throws Exception {
        VOTechnicalService tp = createTechnicalProduct(svcProv);
        VOServiceDetails product = createProduct(tp, "test1", svcProv);
        VOPriceModel priceModel = createPriceModel();
        product = svcProv.savePriceModel(product, priceModel);

        publishServiceToGlobalMarketplace(product);

        createOperator();
        container.login(operatorUserKey, ROLE_PLATFORM_OPERATOR);
        mpProv.deleteMarketplace(globalMarketplace.getMarketplaceId());
        container.login(providerUserKey, ROLE_SERVICE_MANAGER);

        svcProv.activateService(product);
    }

    @Test(expected = ServiceStateException.class)
    public void testActivateProduct_Deleted() throws Exception {
        VOTechnicalService tp = createTechnicalProduct(svcProv);
        VOServiceDetails product = createProduct(tp, "test1", svcProv);
        VOPriceModel priceModel = createPriceModel();
        product = svcProv.savePriceModel(product, priceModel);
        svcProv.deleteService(product);
        svcProv.activateService(product);
    }

    @Test(expected = ServiceStateException.class)
    public void testActivateProduct_Obsolete() throws Exception {
        VOTechnicalService tp = createTechnicalProduct(svcProv);
        VOServiceDetails product = createProduct(tp, "test1", svcProv);
        VOPriceModel priceModel = createPriceModel();
        product = svcProv.savePriceModel(product, priceModel);
        product = publishServiceToGlobalMarketplace(product);
        product = setProductStatus(ServiceStatus.OBSOLETE, product.getKey());
        svcProv.activateService(product);
    }

    @Test(expected = ServiceStateException.class)
    public void testActivateProduct_Suspended() throws Exception {
        VOTechnicalService tp = createTechnicalProduct(svcProv);
        VOServiceDetails product = createProduct(tp, "test1", svcProv);
        VOPriceModel priceModel = createPriceModel();
        product = svcProv.savePriceModel(product, priceModel);
        product = publishServiceToGlobalMarketplace(product);
        product = setProductStatus(ServiceStatus.SUSPENDED, product.getKey());
        svcProv.activateService(product);
    }

    @Test(expected = ServiceOperationException.class)
    public void testActivateProductOfSubscription() throws Exception {
        VOTechnicalService tp = createTechnicalProduct(svcProv);
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        VOServiceDetails product = createProduct(tp, "test", svcProv);
        VOPriceModel priceModel = createPriceModel();
        product = svcProv.savePriceModel(product, priceModel);
        VOOrganization customer = getOrganizationForOrgId(customerOrgId);
        String subId = createSubscription(customer, SubscriptionStatus.ACTIVE,
                product, "testSub", null);
        product = svcProv.getServiceForSubscription(customer, subId);
        svcProv.activateService(product);
    }

    @Test
    public void testDeactivate() throws Exception {
        VOTechnicalService techProduct = createTechnicalProduct(svcProv);
        VOServiceDetails product = createProduct(techProduct, "product",
                svcProv);

        VOPriceModel priceModel = createPriceModel();
        product = svcProv.savePriceModel(product, priceModel);

        product = publishServiceToGlobalMarketplace(product);

        svcProv.activateService(product);
        product = svcProv.getServiceDetails(product);
        Assert.assertEquals(ServiceStatus.ACTIVE, product.getStatus());

        VOService deactivatedProduct = svcProv.deactivateService(product);
        Assert.assertEquals(ServiceStatus.INACTIVE,
                deactivatedProduct.getStatus());
    }

    @Test
    public void testDeactivateInDeactivatedState() throws Exception {
        VOTechnicalService techProduct = createTechnicalProduct(svcProv);
        VOServiceDetails product = createProduct(techProduct, "product",
                svcProv);

        Assert.assertEquals(ServiceStatus.INACTIVE, product.getStatus());
        svcProv.deactivateService(product);
        product = svcProv.getServiceDetails(product);
        Assert.assertEquals(ServiceStatus.INACTIVE, product.getStatus());
    }

    @Test(expected = OperationNotPermittedException.class)
    public void testDeactivateProductNotOwner() throws Exception {
        VOTechnicalService tp = createTechnicalProduct(svcProv);
        VOServiceDetails product = createProduct(tp, "test1", svcProv);
        VOPriceModel priceModel = createPriceModel();
        product = svcProv.savePriceModel(product, priceModel);
        product = publishServiceToGlobalMarketplace(product);
        svcProv.activateService(product);
        product = svcProv.getServiceDetails(product);

        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        svcProv.deactivateService(product);
    }

    @Test(expected = ServiceStateException.class)
    public void testDeactivateProduct_Deleted() throws Exception {
        VOTechnicalService tp = createTechnicalProduct(svcProv);
        VOServiceDetails product = createProduct(tp, "test1", svcProv);
        VOPriceModel priceModel = createPriceModel();
        product = svcProv.savePriceModel(product, priceModel);
        svcProv.deleteService(product);
        svcProv.deactivateService(product);
    }

    @Test(expected = ServiceStateException.class)
    public void testDeactivateProduct_Obsolete() throws Exception {
        VOTechnicalService tp = createTechnicalProduct(svcProv);
        VOServiceDetails product = createProduct(tp, "test1", svcProv);
        VOPriceModel priceModel = createPriceModel();
        product = svcProv.savePriceModel(product, priceModel);
        product = setProductStatus(ServiceStatus.OBSOLETE, product.getKey());
        svcProv.deactivateService(product);
    }

    @Test(expected = ServiceStateException.class)
    public void testDeactivateProduct_Suspended() throws Exception {
        VOTechnicalService tp = createTechnicalProduct(svcProv);
        VOServiceDetails product = createProduct(tp, "test1", svcProv);
        VOPriceModel priceModel = createPriceModel();
        product = svcProv.savePriceModel(product, priceModel);
        product = setProductStatus(ServiceStatus.SUSPENDED, product.getKey());
        svcProv.deactivateService(product);
    }

    @Test(expected = ServiceOperationException.class)
    public void testDeactivateProductOfSubscription() throws Exception {
        VOTechnicalService tp = createTechnicalProduct(svcProv);
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        VOServiceDetails product = createProduct(tp, "test", svcProv);
        VOPriceModel priceModel = createPriceModel();
        product = svcProv.savePriceModel(product, priceModel);
        VOOrganization customer = getOrganizationForOrgId(customerOrgId);
        String subId = createSubscription(customer, SubscriptionStatus.ACTIVE,
                product, "testSub", null);
        product = svcProv.getServiceForSubscription(customer, subId);
        svcProv.deactivateService(product);
    }

    /**
     * Creates a number of products and catalog entries and prepares an
     * activation structure
     */
    private List<VOServiceActivation> createServiceActivations(
            VOTechnicalService techProduct, VOMarketplace marketplace, int count)
            throws Exception {
        List<VOServiceActivation> list = new ArrayList<VOServiceActivation>();
        String marketplaceId = "null";
        if (marketplace != null) {
            marketplaceId = marketplace.getMarketplaceId();
        }
        for (int i = 0; i < count; i++) {
            // Create product
            VOServiceDetails product = createProduct(techProduct, "product_"
                    + marketplaceId + "_" + techProduct.getTechnicalServiceId()
                    + "_" + i, svcProv);
            VOCatalogEntry entry = new VOCatalogEntry();
            entry.setMarketplace(marketplace);

            List<VOCatalogEntry> entries = new ArrayList<VOCatalogEntry>();
            entries.add(entry);
            product = mpProv.publishService(product, entries);

            VOPriceModel priceModel1 = createPriceModel();
            product = svcProv.savePriceModel(product, priceModel1);

            // And convert to activation entity
            VOServiceActivation activation = new VOServiceActivation();
            activation.setActive(false);
            activation.setService(product);
            activation.setCatalogEntries(entries);
            list.add(activation);
        }

        return list;
    }

    /**
     * Creates a number of customer specific products and prepares an activation
     * structure
     */
    private List<VOServiceActivation> createCustomerSpecificServiceActivations(
            VOTechnicalService techProduct, VOMarketplace marketplace, int count)
            throws Exception {
        List<VOServiceActivation> list = new ArrayList<VOServiceActivation>();
        String marketplaceId = "null";
        if (marketplace != null) {
            marketplaceId = marketplace.getMarketplaceId();
        }
        LocalizerFacade facade = mock(LocalizerFacade.class);
        for (int i = 0; i < count; i++) {
            VOServiceDetails product = createProduct(techProduct, "product_"
                    + marketplaceId + "_" + techProduct.getTechnicalServiceId()
                    + "_" + i, svcProv);
            VOCatalogEntry entry = new VOCatalogEntry();
            entry.setMarketplace(marketplace);

            List<VOCatalogEntry> entries = new ArrayList<VOCatalogEntry>();
            entries.add(entry);
            product = mpProv.publishService(product, entries);

            VOOrganization cust = OrganizationAssembler.toVOOrganization(
                    supplier, false, facade);
            VOPriceModel priceModel = createPriceModel();
            // Create customer specific service
            VOServiceDetails specificProduct = svcProv
                    .savePriceModelForCustomer(product, priceModel, cust);

            // And convert to activation entity
            entries.clear();
            VOServiceActivation activation = new VOServiceActivation();
            activation.setActive(false);
            activation.setService(specificProduct);
            activation.setCatalogEntries(entries);
            list.add(activation);

        }
        return list;
    }

    /**
     * Creates a number of products and catalog entries and prepares an
     * acitvation structure
     */
    private void adjustActivationWithoutCatalog(
            List<VOServiceActivation> activations, int index, boolean active) {
        VOServiceActivation activation = activations.get(index);
        activation.setActive(active);
    }

    /**
     * Creates a number of products and catalog entries and prepares an
     * acitvation structure
     */
    private void adjustActivation(List<VOServiceActivation> activations,
            int index, boolean active, boolean visible) {
        VOServiceActivation activation = activations.get(index);
        activation.setActive(active);
        activation.getCatalogEntries().get(0).setVisibleInCatalog(visible);
    }

    @Test
    public void testListActivate_CustomerSpecificServiceActivate()
            throws Exception {
        List<VOServiceActivation> list = prepareSerivce();
        // Adjust settings
        adjustActivationWithoutCatalog(list, 0, true);

        // Set new activation states
        svcProv.setActivationStates(list);

        // Validate result
        VOServiceDetails sd = svcProv.getServiceDetails(list.get(0)
                .getService());
        List<VOCatalogEntry> ce = list.get(0).getCatalogEntries();
        Assert.assertEquals(ServiceStatus.ACTIVE, sd.getStatus());
        Assert.assertEquals(0, ce.size());
    }

    @Test
    public void testListActivateGlobal() throws Exception {
        // Create services
        VOTechnicalService techProduct = createTechnicalProduct(svcProv);
        List<VOServiceActivation> list = createServiceActivations(techProduct,
                globalMarketplace, 2);

        // Adjust settings
        adjustActivation(list, 0, true, true);
        adjustActivation(list, 1, false, false);

        // Set new activation states
        svcProv.setActivationStates(list);

        // Validate result
        VOServiceDetails sd1 = svcProv.getServiceDetails(list.get(0)
                .getService());
        VOServiceDetails sd2 = svcProv.getServiceDetails(list.get(1)
                .getService());
        VOCatalogEntry ce1 = mpProv.getMarketplacesForService(sd1).get(0);
        VOCatalogEntry ce2 = mpProv.getMarketplacesForService(sd2).get(0);

        Assert.assertEquals(ServiceStatus.ACTIVE, sd1.getStatus());
        Assert.assertEquals(ServiceStatus.INACTIVE, sd2.getStatus());

        Assert.assertTrue(ce1.isVisibleInCatalog());
        Assert.assertFalse(ce2.isVisibleInCatalog());
    }

    @Test(expected = ServiceNotPublishedException.class)
    public void testListActivate_NoMarketplaceActivate() throws Exception {
        // Create services
        VOTechnicalService techProduct = createTechnicalProduct(svcProv);
        List<VOServiceActivation> list = createServiceActivations(techProduct,
                null, 1);

        // Adjust settings
        adjustActivation(list, 0, true, true);

        // Set new activation states
        svcProv.setActivationStates(list);

    }

    /**
     * Bug 9923 - marketplace publishing changed concurrently before activating
     */
    @Test(expected = ConcurrentModificationException.class)
    public void testListActivate_ConcurrentMarketplaceChange() throws Exception {
        // Create services
        VOTechnicalService techProduct = createTechnicalProduct(svcProv);
        final List<VOServiceActivation> list = createServiceActivations(
                techProduct, globalMarketplace, 1);

        // Adjust settings
        adjustActivation(list, 0, true, true);

        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                Marketplace mp = mgr.getReference(Marketplace.class,
                        globalMarketplace2.getKey());
                long svcKey = list.get(0).getService().getKey();
                Product p = mgr.getReference(Product.class, svcKey);
                p.getCatalogEntries().get(0).setMarketplace(mp);
                return null;
            }
        });

        // Set new activation states
        svcProv.setActivationStates(list);

    }

    @Test
    public void testListActivate_NoMarketplaceDeactivate() throws Exception {
        // Create services
        VOTechnicalService techProduct = createTechnicalProduct(svcProv);
        List<VOServiceActivation> list = createServiceActivations(techProduct,
                null, 1);

        // Adjust settings
        adjustActivation(list, 0, false, true);

        // Set new activation states
        svcProv.setActivationStates(list);

        // Validate result
        VOServiceDetails sd1 = svcProv.getServiceDetails(list.get(0)
                .getService());
        VOCatalogEntry ce1 = mpProv.getMarketplacesForService(sd1).get(0);

        Assert.assertEquals(ServiceStatus.INACTIVE, sd1.getStatus());

        Assert.assertTrue(ce1.isVisibleInCatalog());

    }

    @Test(expected = ServiceStateException.class)
    public void testListActivate_DeactivateProductDeleted() throws Exception {
        // Create services
        VOTechnicalService techProduct = createTechnicalProduct(svcProv);
        List<VOServiceActivation> list = createServiceActivations(techProduct,
                null, 1);

        // Adjust settings
        adjustActivation(list, 0, false, true);

        svcProv.deleteService(list.get(0).getService());
        // Set new activation states
        svcProv.setActivationStates(list);
    }

    @Test
    public void testListActivateLocalGlobal() throws Exception {
        // Create services
        VOTechnicalService techProduct = createTechnicalProduct(svcProv);
        List<VOServiceActivation> list = createServiceActivations(techProduct,
                globalMarketplace, 2);
        list.addAll(createServiceActivations(techProduct, localMarketplace, 2));

        // Adjust settings
        adjustActivation(list, 0, true, false);
        adjustActivation(list, 1, true, true);
        adjustActivation(list, 2, true, true);
        adjustActivation(list, 3, false, false);

        // Set new activation states
        svcProv.setActivationStates(list);

        // Validate result
        VOServiceDetails sd1 = svcProv.getServiceDetails(list.get(0)
                .getService());
        VOServiceDetails sd2 = svcProv.getServiceDetails(list.get(1)
                .getService());
        VOServiceDetails sd3 = svcProv.getServiceDetails(list.get(2)
                .getService());
        VOServiceDetails sd4 = svcProv.getServiceDetails(list.get(3)
                .getService());
        VOCatalogEntry ce1 = mpProv.getMarketplacesForService(sd1).get(0);
        VOCatalogEntry ce2 = mpProv.getMarketplacesForService(sd2).get(0);
        VOCatalogEntry ce3 = mpProv.getMarketplacesForService(sd3).get(0);
        VOCatalogEntry ce4 = mpProv.getMarketplacesForService(sd4).get(0);

        Assert.assertEquals(ServiceStatus.ACTIVE, sd1.getStatus());
        Assert.assertEquals(ServiceStatus.ACTIVE, sd2.getStatus());
        Assert.assertEquals(ServiceStatus.ACTIVE, sd3.getStatus());
        Assert.assertEquals(ServiceStatus.INACTIVE, sd4.getStatus());

        Assert.assertFalse(ce1.isVisibleInCatalog());
        Assert.assertTrue(ce2.isVisibleInCatalog());
        Assert.assertTrue(ce3.isVisibleInCatalog());
        Assert.assertFalse(ce4.isVisibleInCatalog());
    }

    @Test
    public void testListActivateUpdated() throws Exception {
        // Create services
        VOTechnicalService techProduct = createTechnicalProduct(svcProv);
        List<VOServiceActivation> list = createServiceActivations(techProduct,
                globalMarketplace, 2);
        list.addAll(createServiceActivations(techProduct, localMarketplace, 2));

        // Adjust settings
        adjustActivation(list, 0, true, true);
        adjustActivation(list, 1, true, true);
        adjustActivation(list, 2, true, true);
        adjustActivation(list, 3, false, false);

        // Set new activation states
        List<VOService> services = svcProv.setActivationStates(list);
        for (int i = 0; i < list.size(); i++) {
            list.get(i).setService(services.get(i));
        }

        // Validate result
        VOServiceDetails sd1 = svcProv.getServiceDetails(list.get(0)
                .getService());
        VOServiceDetails sd2 = svcProv.getServiceDetails(list.get(1)
                .getService());
        VOServiceDetails sd3 = svcProv.getServiceDetails(list.get(2)
                .getService());
        VOServiceDetails sd4 = svcProv.getServiceDetails(list.get(3)
                .getService());
        VOCatalogEntry ce1 = mpProv.getMarketplacesForService(sd1).get(0);
        VOCatalogEntry ce2 = mpProv.getMarketplacesForService(sd2).get(0);
        VOCatalogEntry ce3 = mpProv.getMarketplacesForService(sd3).get(0);
        VOCatalogEntry ce4 = mpProv.getMarketplacesForService(sd4).get(0);

        Assert.assertEquals(ServiceStatus.ACTIVE, sd1.getStatus());
        Assert.assertEquals(ServiceStatus.ACTIVE, sd2.getStatus());
        Assert.assertEquals(ServiceStatus.ACTIVE, sd3.getStatus());
        Assert.assertEquals(ServiceStatus.INACTIVE, sd4.getStatus());

        Assert.assertTrue(ce1.isVisibleInCatalog());
        Assert.assertTrue(ce2.isVisibleInCatalog());
        Assert.assertTrue(ce3.isVisibleInCatalog());
        Assert.assertFalse(ce4.isVisibleInCatalog());

        // Now modify some of them
        List<VOServiceActivation> updateList = new ArrayList<VOServiceActivation>();
        updateList.add(list.get(0)); // 1st global
        updateList.add(list.get(1)); // 2nd global
        updateList.add(list.get(2)); // 1st local
        updateList.add(list.get(3)); // 1st local

        adjustActivation(updateList, 0, true, true);
        adjustActivation(updateList, 1, false, true);
        adjustActivation(updateList, 2, true, false);
        adjustActivation(updateList, 3, true, true);

        // Set new activation states
        svcProv.setActivationStates(updateList);

        // Validate result
        sd1 = svcProv.getServiceDetails(list.get(0).getService());
        sd2 = svcProv.getServiceDetails(list.get(1).getService());
        sd3 = svcProv.getServiceDetails(list.get(2).getService());
        sd4 = svcProv.getServiceDetails(list.get(3).getService());
        ce1 = mpProv.getMarketplacesForService(sd1).get(0);
        ce2 = mpProv.getMarketplacesForService(sd2).get(0);
        ce3 = mpProv.getMarketplacesForService(sd3).get(0);
        ce4 = mpProv.getMarketplacesForService(sd4).get(0);

        Assert.assertEquals(ServiceStatus.ACTIVE, sd1.getStatus());
        Assert.assertEquals(ServiceStatus.INACTIVE, sd2.getStatus()); // changed!
        Assert.assertEquals(ServiceStatus.ACTIVE, sd3.getStatus());
        Assert.assertEquals(ServiceStatus.ACTIVE, sd4.getStatus());

        Assert.assertTrue(ce1.isVisibleInCatalog());
        Assert.assertTrue(ce2.isVisibleInCatalog());
        Assert.assertFalse(ce3.isVisibleInCatalog()); // changed!
        Assert.assertTrue(ce4.isVisibleInCatalog());

    }

    @Test
    public void testListActivateCheckTagCache() throws Exception {
        // Create services
        VOTechnicalService techProduct = createTechnicalProduct(svcProv);

        // Create one product (not yet activated)
        List<VOServiceActivation> list = createServiceActivations(techProduct,
                globalMarketplace, 1);

        // Define some tags
        List<String> tags = new ArrayList<String>();
        tags.add("tag1");
        tags.add("tag2");
        tags.add("tag3");
        techProduct.setTags(tags);
        deleteEmptyTp_keyRecord(techProduct);
        svcProv.saveTechnicalServiceLocalization(techProduct);

        // Check that tag cloud is empty (service is not yet visible in
        // marketplace)
        List<VOTag> tagList = tagService.getTagsForMarketplace("en",
                globalMarketplace.getMarketplaceId());
        Assert.assertTrue(tagList.isEmpty());

        // Now activate the product
        adjustActivation(list, 0, true, true);

        // Set new activation states
        svcProv.setActivationStates(list);

        // Tag cloud must not longer be empty!
        tagList = tagService.getTagsForMarketplace("en",
                globalMarketplace.getMarketplaceId());
        Assert.assertEquals(tags.size(), tagList.size());
        Assert.assertFalse(tagList.isEmpty());
    }

    @Test(expected = ServiceOperationException.class)
    public void testListActivateVisibilityCheck1() throws Exception {
        // Create services
        VOTechnicalService techProduct = createTechnicalProduct(svcProv);
        List<VOServiceActivation> list = createServiceActivations(techProduct,
                globalMarketplace, 2);
        list.addAll(createServiceActivations(techProduct, localMarketplace, 2));
        list.addAll(createServiceActivations(techProduct, globalMarketplace2, 2));

        // Adjust settings
        adjustActivation(list, 0, true, false); // GLO/ACT/HID
        adjustActivation(list, 1, false, true); // GLO/INA/VIS
        adjustActivation(list, 2, true, false); // LOC/ACT/HID
        adjustActivation(list, 3, false, false);// LOC/INA/HID
        adjustActivation(list, 4, true, false); // GLO2/ACT/HID
        adjustActivation(list, 5, false, false);// GLO2/INA/HID

        try {
            // => problem no global service is visible (but one is active!)
            svcProv.setActivationStates(list);

        } catch (ServiceOperationException e) {
            Assert.assertTrue(e.getMessageKey().contains(
                    ServiceOperationException.Reason.NO_VISIBLE_ACTIVE_SERVICE
                            .toString()));

            // Validate result (must be unchanged!)
            VOServiceDetails sd1 = svcProv.getServiceDetails(list.get(0)
                    .getService());
            VOServiceDetails sd2 = svcProv.getServiceDetails(list.get(1)
                    .getService());
            VOServiceDetails sd3 = svcProv.getServiceDetails(list.get(2)
                    .getService());
            VOServiceDetails sd4 = svcProv.getServiceDetails(list.get(3)
                    .getService());
            VOServiceDetails sd5 = svcProv.getServiceDetails(list.get(2)
                    .getService());
            VOServiceDetails sd6 = svcProv.getServiceDetails(list.get(3)
                    .getService());

            VOCatalogEntry ce1 = mpProv.getMarketplacesForService(sd1).get(0);
            VOCatalogEntry ce2 = mpProv.getMarketplacesForService(sd2).get(0);
            VOCatalogEntry ce3 = mpProv.getMarketplacesForService(sd3).get(0);
            VOCatalogEntry ce4 = mpProv.getMarketplacesForService(sd4).get(0);
            VOCatalogEntry ce5 = mpProv.getMarketplacesForService(sd5).get(0);
            VOCatalogEntry ce6 = mpProv.getMarketplacesForService(sd6).get(0);

            Assert.assertEquals(ServiceStatus.INACTIVE, sd1.getStatus());
            Assert.assertEquals(ServiceStatus.INACTIVE, sd2.getStatus());
            Assert.assertEquals(ServiceStatus.INACTIVE, sd3.getStatus());
            Assert.assertEquals(ServiceStatus.INACTIVE, sd4.getStatus());
            Assert.assertEquals(ServiceStatus.INACTIVE, sd5.getStatus());
            Assert.assertEquals(ServiceStatus.INACTIVE, sd6.getStatus());

            Assert.assertTrue(ce1.isVisibleInCatalog());
            Assert.assertTrue(ce2.isVisibleInCatalog());
            Assert.assertTrue(ce3.isVisibleInCatalog());
            Assert.assertTrue(ce4.isVisibleInCatalog());
            Assert.assertTrue(ce5.isVisibleInCatalog());
            Assert.assertTrue(ce6.isVisibleInCatalog());

            throw e;
        }
    }

    @Test(expected = ServiceOperationException.class)
    public void testListActivateVisibilityCheck2() throws Exception {
        // Create services
        createTechnicalProduct(svcProv);
        VOTechnicalService techProduct1 = svcProv.getTechnicalServices(
                OrganizationRoleType.TECHNOLOGY_PROVIDER).get(0);
        VOTechnicalService techProduct2 = svcProv.getTechnicalServices(
                OrganizationRoleType.TECHNOLOGY_PROVIDER).get(1);

        List<VOServiceActivation> list = createServiceActivations(techProduct1,
                globalMarketplace, 4);
        list.addAll(createServiceActivations(techProduct1, localMarketplace, 2));

        list.addAll(createServiceActivations(techProduct2, globalMarketplace, 2));
        list.addAll(createServiceActivations(techProduct1, globalMarketplace2,
                2));

        // Adjust settings
        adjustActivation(list, 0, true, false); // T1/GLO/ACT/HID
        adjustActivation(list, 1, false, true); // T1/GLO/INA/VIS
        adjustActivation(list, 2, true, true); // T1/GLO/ACT/VIS
        adjustActivation(list, 3, true, true); // T1/GLO/ACT/VIS
        adjustActivation(list, 4, false, false); // T1/LOC/INA/HID
        adjustActivation(list, 5, true, false); // T1/LOC/ACT/HID

        adjustActivation(list, 6, true, false); // T2/GLO/ACT/HID
        adjustActivation(list, 7, false, true); // T2/GLO/INA/VIS

        adjustActivation(list, 8, true, false); // T2/GLO/ACT/HID
        adjustActivation(list, 9, false, true); // T2/GLO/INA/VIS

        try {
            // => problem no global service is visible (but one is active!)
            svcProv.setActivationStates(list);

        } catch (ServiceOperationException e) {
            Assert.assertTrue(e.getMessageKey().contains(
                    ServiceOperationException.Reason.NO_VISIBLE_ACTIVE_SERVICE
                            .toString()));

            // Validate result (must be unchanged!)
            VOServiceDetails sd1 = svcProv.getServiceDetails(list.get(0)
                    .getService());
            VOServiceDetails sd2 = svcProv.getServiceDetails(list.get(1)
                    .getService());
            VOServiceDetails sd3 = svcProv.getServiceDetails(list.get(2)
                    .getService());
            VOServiceDetails sd4 = svcProv.getServiceDetails(list.get(3)
                    .getService());
            VOServiceDetails sd5 = svcProv.getServiceDetails(list.get(4)
                    .getService());
            VOServiceDetails sd6 = svcProv.getServiceDetails(list.get(5)
                    .getService());
            VOServiceDetails sd7 = svcProv.getServiceDetails(list.get(6)
                    .getService());
            VOServiceDetails sd8 = svcProv.getServiceDetails(list.get(7)
                    .getService());
            VOServiceDetails sd9 = svcProv.getServiceDetails(list.get(8)
                    .getService());
            VOServiceDetails sd10 = svcProv.getServiceDetails(list.get(9)
                    .getService());
            VOCatalogEntry ce1 = mpProv.getMarketplacesForService(sd1).get(0);
            VOCatalogEntry ce2 = mpProv.getMarketplacesForService(sd2).get(0);
            VOCatalogEntry ce3 = mpProv.getMarketplacesForService(sd3).get(0);
            VOCatalogEntry ce4 = mpProv.getMarketplacesForService(sd4).get(0);
            VOCatalogEntry ce5 = mpProv.getMarketplacesForService(sd5).get(0);
            VOCatalogEntry ce6 = mpProv.getMarketplacesForService(sd6).get(0);
            VOCatalogEntry ce7 = mpProv.getMarketplacesForService(sd7).get(0);
            VOCatalogEntry ce8 = mpProv.getMarketplacesForService(sd8).get(0);
            VOCatalogEntry ce9 = mpProv.getMarketplacesForService(sd7).get(0);
            VOCatalogEntry ce10 = mpProv.getMarketplacesForService(sd8).get(0);

            Assert.assertEquals(ServiceStatus.INACTIVE, sd1.getStatus());
            Assert.assertEquals(ServiceStatus.INACTIVE, sd2.getStatus());
            Assert.assertEquals(ServiceStatus.INACTIVE, sd3.getStatus());
            Assert.assertEquals(ServiceStatus.INACTIVE, sd4.getStatus());
            Assert.assertEquals(ServiceStatus.INACTIVE, sd5.getStatus());
            Assert.assertEquals(ServiceStatus.INACTIVE, sd6.getStatus());
            Assert.assertEquals(ServiceStatus.INACTIVE, sd7.getStatus());
            Assert.assertEquals(ServiceStatus.INACTIVE, sd8.getStatus());
            Assert.assertEquals(ServiceStatus.INACTIVE, sd9.getStatus());
            Assert.assertEquals(ServiceStatus.INACTIVE, sd10.getStatus());

            Assert.assertTrue(ce1.isVisibleInCatalog());
            Assert.assertTrue(ce2.isVisibleInCatalog());
            Assert.assertTrue(ce3.isVisibleInCatalog());
            Assert.assertTrue(ce4.isVisibleInCatalog());
            Assert.assertTrue(ce5.isVisibleInCatalog());
            Assert.assertTrue(ce6.isVisibleInCatalog());
            Assert.assertTrue(ce7.isVisibleInCatalog());
            Assert.assertTrue(ce8.isVisibleInCatalog());
            Assert.assertTrue(ce9.isVisibleInCatalog());
            Assert.assertTrue(ce10.isVisibleInCatalog());

            throw e;
        }
    }

    private List<VOServiceActivation> prepareListActivateVisibilityCheck3()
            throws Exception {
        // Create services
        createTechnicalProduct(svcProv);
        VOTechnicalService techProduct1 = svcProv.getTechnicalServices(
                OrganizationRoleType.TECHNOLOGY_PROVIDER).get(0);
        VOTechnicalService techProduct2 = svcProv.getTechnicalServices(
                OrganizationRoleType.TECHNOLOGY_PROVIDER).get(1);

        List<VOServiceActivation> list = createServiceActivations(techProduct1,
                globalMarketplace, 4);
        list.addAll(createServiceActivations(techProduct1, localMarketplace, 2));

        list.addAll(createServiceActivations(techProduct2, globalMarketplace, 2));

        // Adjust settings
        adjustActivation(list, 0, true, false); // T1/GLO/ACT/HID
        adjustActivation(list, 1, false, true); // T1/GLO/INA/VIS
        adjustActivation(list, 2, true, true); // T1/GLO/ACT/VIS
        adjustActivation(list, 3, true, true); // T1/GLO/ACT/VIS
        adjustActivation(list, 4, false, false); // T1/LOC/INA/HID
        adjustActivation(list, 5, true, true); // T1/LOC/ACT/HID

        adjustActivation(list, 6, true, true); // T2/GLO/ACT/VIS
        adjustActivation(list, 7, false, true); // T2/GLO/INA/VIS

        // This is consistent!
        List<VOService> services = svcProv.setActivationStates(list);
        for (int i = 0; i < list.size(); i++) {
            list.get(i).setService(services.get(i));
        }

        return list;
    }

    private void doTestListActivateVisibilityCheck3(
            List<VOServiceActivation> list) throws Exception {
        // Now change it inconsistently
        List<VOServiceActivation> updateList = new ArrayList<VOServiceActivation>();
        adjustActivation(list, 3, true, false); // T1/GLO/ACT/HID (still ok)
        adjustActivation(list, 5, false, false); // T1/LOC/ACT/HID (unimportant)
        adjustActivation(list, 6, true, false); // T2/GLO/ACT/HID (not ok!)
        updateList.add(list.get(3));
        updateList.add(list.get(5));
        updateList.add(list.get(6));

        try {
            // => problem no global service is visible (but one is active!)
            svcProv.setActivationStates(updateList);

        } catch (ServiceOperationException e) {
            Assert.assertTrue(e.getMessageKey().contains(
                    ServiceOperationException.Reason.NO_VISIBLE_ACTIVE_SERVICE
                            .toString()));

            // Validate result (must be unchanged!)
            VOServiceDetails sd1 = svcProv.getServiceDetails(list.get(0)
                    .getService());
            VOServiceDetails sd2 = svcProv.getServiceDetails(list.get(1)
                    .getService());
            VOServiceDetails sd3 = svcProv.getServiceDetails(list.get(2)
                    .getService());
            VOServiceDetails sd4 = svcProv.getServiceDetails(list.get(3)
                    .getService());
            VOServiceDetails sd5 = svcProv.getServiceDetails(list.get(4)
                    .getService());
            VOServiceDetails sd6 = svcProv.getServiceDetails(list.get(5)
                    .getService());
            VOServiceDetails sd7 = svcProv.getServiceDetails(list.get(6)
                    .getService());
            VOServiceDetails sd8 = svcProv.getServiceDetails(list.get(7)
                    .getService());
            VOCatalogEntry ce1 = mpProv.getMarketplacesForService(sd1).get(0);
            VOCatalogEntry ce2 = mpProv.getMarketplacesForService(sd2).get(0);
            VOCatalogEntry ce3 = mpProv.getMarketplacesForService(sd3).get(0);
            VOCatalogEntry ce4 = mpProv.getMarketplacesForService(sd4).get(0);
            VOCatalogEntry ce5 = mpProv.getMarketplacesForService(sd5).get(0);
            VOCatalogEntry ce6 = mpProv.getMarketplacesForService(sd6).get(0);
            VOCatalogEntry ce7 = mpProv.getMarketplacesForService(sd7).get(0);
            VOCatalogEntry ce8 = mpProv.getMarketplacesForService(sd8).get(0);

            Assert.assertEquals(ServiceStatus.ACTIVE, sd1.getStatus());
            Assert.assertEquals(ServiceStatus.INACTIVE, sd2.getStatus());
            Assert.assertEquals(ServiceStatus.ACTIVE, sd3.getStatus());
            Assert.assertEquals(ServiceStatus.ACTIVE, sd4.getStatus());
            Assert.assertEquals(ServiceStatus.INACTIVE, sd5.getStatus());
            Assert.assertEquals(ServiceStatus.ACTIVE, sd6.getStatus());
            Assert.assertEquals(ServiceStatus.ACTIVE, sd7.getStatus());
            Assert.assertEquals(ServiceStatus.INACTIVE, sd8.getStatus());

            Assert.assertFalse(ce1.isVisibleInCatalog());
            Assert.assertTrue(ce2.isVisibleInCatalog());
            Assert.assertTrue(ce3.isVisibleInCatalog());
            Assert.assertTrue(ce4.isVisibleInCatalog());
            Assert.assertFalse(ce5.isVisibleInCatalog());
            Assert.assertTrue(ce6.isVisibleInCatalog());
            Assert.assertTrue(ce7.isVisibleInCatalog());
            Assert.assertTrue(ce8.isVisibleInCatalog());

            throw e;
        }
    }

    @Test(expected = ServiceOperationException.class)
    public void testListActivateVisibilityCheck3() throws Exception {
        List<VOServiceActivation> list = prepareListActivateVisibilityCheck3();
        doTestListActivateVisibilityCheck3(list);
    }

    private void setupTrigger() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                TriggerDefinition def = new TriggerDefinition();
                def.setOrganization(mgr.getCurrentUser().getOrganization());
                def.setType(TriggerType.ACTIVATE_SERVICE);
                def.setTargetType(TriggerTargetType.WEB_SERVICE);
                def.setTarget("http");
                def.setSuspendProcess(true);
                def.setName(def.getType().name());
                mgr.persist(def);

                def = new TriggerDefinition();
                def.setOrganization(mgr.getCurrentUser().getOrganization());
                def.setType(TriggerType.DEACTIVATE_SERVICE);
                def.setTargetType(TriggerTargetType.WEB_SERVICE);
                def.setTarget("http");
                def.setSuspendProcess(true);
                def.setName(def.getType().name());
                mgr.persist(def);

                mgr.flush();
                return null;
            }
        });

    }

    private void applyAllTriggers() throws Exception {
        container.login(providerUserKey, ROLE_ORGANIZATION_ADMIN,
                ROLE_SERVICE_MANAGER);
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                List<VOTriggerProcess> triggers = triggerService
                        .getAllActions();
                for (VOTriggerProcess trProc : triggers) {
                    triggerServiceLocal.setStatus(trProc.getKey(),
                            TriggerProcessStatus.WAITING_FOR_APPROVAL);
                }

                mgr.flush();
                return null;
            }
        });
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                List<VOTriggerProcess> triggers = triggerService
                        .getAllActions();
                for (VOTriggerProcess trProc : triggers) {
                    triggerService.approveAction(trProc.getKey());
                }

                mgr.flush();
                return null;
            }
        });

    }

    @Test(expected = ServiceOperationException.class)
    public void testListActivateVisibilityCheck1Triggered() throws Exception {
        // Setup trigger
        setupTrigger();
        // Test result must be identical with and without trigger
        testListActivateVisibilityCheck1();
    }

    @Test(expected = ServiceOperationException.class)
    public void testListActivateVisibilityCheck2Triggered() throws Exception {
        // Setup trigger
        setupTrigger();
        // Test result must be identical with and without trigger
        testListActivateVisibilityCheck2();
    }

    @Test
    public void testListActivate_CustomerSpecificServiceActivateTriggered()
            throws Exception {

        List<VOServiceActivation> list = prepareSerivce();
        // Adjust settings
        adjustActivationWithoutCatalog(list, 0, true);

        // Setup trigger
        setupTrigger();
        // Set new activation states
        svcProv.setActivationStates(list);

        // Validate result
        VOServiceDetails sd = svcProv.getServiceDetails(list.get(0)
                .getService());
        List<VOCatalogEntry> ce = list.get(0).getCatalogEntries();
        Assert.assertEquals(ServiceStatus.INACTIVE, sd.getStatus());
        Assert.assertEquals(0, ce.size());
    }

    private List<VOServiceActivation> prepareSerivce() throws Exception {
        // Prepare services and price model
        VOTechnicalService techProduct = createTechnicalProduct(svcProv);
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        List<VOServiceActivation> list = createCustomerSpecificServiceActivations(
                techProduct, null, 1);

        return list;
    }

    @Test(expected = ServiceOperationException.class)
    public void testListActivateVisibilityCheck3Triggered() throws Exception {
        List<VOServiceActivation> list = prepareListActivateVisibilityCheck3();
        // Setup trigger
        setupTrigger();
        // Test result must be identical with and without trigger
        doTestListActivateVisibilityCheck3(list);
    }

    @Test(expected = ServiceOperationException.class)
    public void testListActivateVisibilityCheck_multipleGlobalMplsTriggered()
            throws Exception {
        // Setup trigger
        setupTrigger();
        // Test result must be identical with and without trigger
        testListActivateVisibilityCheck_multipleGlobalMpls();
    }

    @Test(expected = ServiceOperationException.class)
    public void testListActivateVisibilityCheck_multipleGlobalMpls()
            throws Exception {
        // Create services
        VOTechnicalService techProduct = createTechnicalProduct(svcProv);
        List<VOServiceActivation> list = createServiceActivations(techProduct,
                globalMarketplace, 2);
        list.addAll(createServiceActivations(techProduct, globalMarketplace2, 2));

        // Adjust settings
        adjustActivation(list, 0, true, true); // GLO/ACT/VIS
        adjustActivation(list, 1, false, false);// GLO/INA/NID
        adjustActivation(list, 2, true, false); // GLO2/ACT/HID <= this is not
                                                // valid
        adjustActivation(list, 3, false, false);// GLO2/INA/HID

        try {
            // => problem: there is no service visible on the second global
            // marketplace
            svcProv.setActivationStates(list);

        } catch (ServiceOperationException e) {
            Assert.assertTrue(e.getMessageKey().contains(
                    ServiceOperationException.Reason.NO_VISIBLE_ACTIVE_SERVICE
                            .toString()));

            // Validate result (must be unchanged!)
            VOServiceDetails sd1 = svcProv.getServiceDetails(list.get(0)
                    .getService());
            VOServiceDetails sd2 = svcProv.getServiceDetails(list.get(1)
                    .getService());
            VOServiceDetails sd3 = svcProv.getServiceDetails(list.get(2)
                    .getService());
            VOServiceDetails sd4 = svcProv.getServiceDetails(list.get(3)
                    .getService());
            VOServiceDetails sd5 = svcProv.getServiceDetails(list.get(2)
                    .getService());
            VOServiceDetails sd6 = svcProv.getServiceDetails(list.get(3)
                    .getService());

            VOCatalogEntry ce1 = mpProv.getMarketplacesForService(sd1).get(0);
            VOCatalogEntry ce2 = mpProv.getMarketplacesForService(sd2).get(0);
            VOCatalogEntry ce3 = mpProv.getMarketplacesForService(sd3).get(0);
            VOCatalogEntry ce4 = mpProv.getMarketplacesForService(sd4).get(0);
            VOCatalogEntry ce5 = mpProv.getMarketplacesForService(sd5).get(0);
            VOCatalogEntry ce6 = mpProv.getMarketplacesForService(sd6).get(0);

            Assert.assertEquals(ServiceStatus.INACTIVE, sd1.getStatus());
            Assert.assertEquals(ServiceStatus.INACTIVE, sd2.getStatus());
            Assert.assertEquals(ServiceStatus.INACTIVE, sd3.getStatus());
            Assert.assertEquals(ServiceStatus.INACTIVE, sd4.getStatus());
            Assert.assertEquals(ServiceStatus.INACTIVE, sd5.getStatus());
            Assert.assertEquals(ServiceStatus.INACTIVE, sd6.getStatus());

            Assert.assertTrue(ce1.isVisibleInCatalog());
            Assert.assertTrue(ce2.isVisibleInCatalog());
            Assert.assertTrue(ce3.isVisibleInCatalog());
            Assert.assertTrue(ce4.isVisibleInCatalog());
            Assert.assertTrue(ce5.isVisibleInCatalog());
            Assert.assertTrue(ce6.isVisibleInCatalog());

            throw e;
        }
    }

    @Test
    public void testListActivateVisibilityCheckTriggered() throws Exception {
        List<VOServiceActivation> list = prepareListActivateVisibilityCheck3();
        // Setup trigger
        setupTrigger();

        // Now test a "good" case with triggers
        List<VOServiceActivation> updateList = new ArrayList<VOServiceActivation>();
        adjustActivation(list, 0, true, false); // T1/GLO/ACT/VIS
        adjustActivation(list, 1, true, true); // T1/GLO/ACT/VIS
        adjustActivation(list, 2, false, true); // T1/GLO/INA/VIS
        adjustActivation(list, 3, true, true); // T1/GLO/ACT/HID
        updateList.add(list.get(0));
        updateList.add(list.get(1));
        updateList.add(list.get(2));
        updateList.add(list.get(3));

        // Change triggered
        svcProv.setActivationStates(updateList);

        // Validate result (must be unchanged!)
        VOServiceDetails sd1 = svcProv.getServiceDetails(list.get(0)
                .getService());
        VOServiceDetails sd2 = svcProv.getServiceDetails(list.get(1)
                .getService());
        VOServiceDetails sd3 = svcProv.getServiceDetails(list.get(2)
                .getService());
        VOServiceDetails sd4 = svcProv.getServiceDetails(list.get(3)
                .getService());
        VOServiceDetails sd5 = svcProv.getServiceDetails(list.get(4)
                .getService());
        VOServiceDetails sd6 = svcProv.getServiceDetails(list.get(5)
                .getService());
        VOServiceDetails sd7 = svcProv.getServiceDetails(list.get(6)
                .getService());
        VOServiceDetails sd8 = svcProv.getServiceDetails(list.get(7)
                .getService());
        VOCatalogEntry ce1 = mpProv.getMarketplacesForService(sd1).get(0);
        VOCatalogEntry ce2 = mpProv.getMarketplacesForService(sd2).get(0);
        VOCatalogEntry ce3 = mpProv.getMarketplacesForService(sd3).get(0);
        VOCatalogEntry ce4 = mpProv.getMarketplacesForService(sd4).get(0);
        VOCatalogEntry ce5 = mpProv.getMarketplacesForService(sd5).get(0);
        VOCatalogEntry ce6 = mpProv.getMarketplacesForService(sd6).get(0);
        VOCatalogEntry ce7 = mpProv.getMarketplacesForService(sd7).get(0);
        VOCatalogEntry ce8 = mpProv.getMarketplacesForService(sd8).get(0);

        Assert.assertEquals(ServiceStatus.ACTIVE, sd1.getStatus());
        Assert.assertEquals(ServiceStatus.INACTIVE, sd2.getStatus());
        Assert.assertEquals(ServiceStatus.ACTIVE, sd3.getStatus());
        Assert.assertEquals(ServiceStatus.ACTIVE, sd4.getStatus());
        Assert.assertEquals(ServiceStatus.INACTIVE, sd5.getStatus());
        Assert.assertEquals(ServiceStatus.ACTIVE, sd6.getStatus());
        Assert.assertEquals(ServiceStatus.ACTIVE, sd7.getStatus());
        Assert.assertEquals(ServiceStatus.INACTIVE, sd8.getStatus());

        Assert.assertFalse(ce1.isVisibleInCatalog());
        Assert.assertTrue(ce2.isVisibleInCatalog());
        Assert.assertTrue(ce3.isVisibleInCatalog());
        Assert.assertTrue(ce4.isVisibleInCatalog());
        Assert.assertFalse(ce5.isVisibleInCatalog());
        Assert.assertTrue(ce6.isVisibleInCatalog());
        Assert.assertTrue(ce7.isVisibleInCatalog());
        Assert.assertTrue(ce8.isVisibleInCatalog());

        // Now we apply the queued triggers
        applyAllTriggers();

        // Revalidate
        sd1 = svcProv.getServiceDetails(list.get(0).getService());
        sd2 = svcProv.getServiceDetails(list.get(1).getService());
        sd3 = svcProv.getServiceDetails(list.get(2).getService());
        sd4 = svcProv.getServiceDetails(list.get(3).getService());
        sd5 = svcProv.getServiceDetails(list.get(4).getService());
        sd6 = svcProv.getServiceDetails(list.get(5).getService());
        sd7 = svcProv.getServiceDetails(list.get(6).getService());
        sd8 = svcProv.getServiceDetails(list.get(7).getService());
        ce1 = mpProv.getMarketplacesForService(sd1).get(0);
        ce2 = mpProv.getMarketplacesForService(sd2).get(0);
        ce3 = mpProv.getMarketplacesForService(sd3).get(0);
        ce4 = mpProv.getMarketplacesForService(sd4).get(0);
        ce5 = mpProv.getMarketplacesForService(sd5).get(0);
        ce6 = mpProv.getMarketplacesForService(sd6).get(0);
        ce7 = mpProv.getMarketplacesForService(sd7).get(0);
        ce8 = mpProv.getMarketplacesForService(sd8).get(0);

        Assert.assertEquals(ServiceStatus.ACTIVE, sd1.getStatus());
        Assert.assertEquals(ServiceStatus.ACTIVE, sd2.getStatus());
        Assert.assertEquals(ServiceStatus.INACTIVE, sd3.getStatus());
        Assert.assertEquals(ServiceStatus.ACTIVE, sd4.getStatus());
        Assert.assertEquals(ServiceStatus.INACTIVE, sd5.getStatus());
        Assert.assertEquals(ServiceStatus.ACTIVE, sd6.getStatus());
        Assert.assertEquals(ServiceStatus.ACTIVE, sd7.getStatus());
        Assert.assertEquals(ServiceStatus.INACTIVE, sd8.getStatus());

        Assert.assertFalse(ce1.isVisibleInCatalog());
        Assert.assertTrue(ce2.isVisibleInCatalog());
        Assert.assertTrue(ce3.isVisibleInCatalog());
        Assert.assertTrue(ce4.isVisibleInCatalog());
        Assert.assertFalse(ce5.isVisibleInCatalog());
        Assert.assertTrue(ce6.isVisibleInCatalog());
        Assert.assertTrue(ce7.isVisibleInCatalog());
        Assert.assertTrue(ce8.isVisibleInCatalog());

    }

    @Test(expected = ServiceOperationException.class)
    public void testActivateVisibilityCheckTriggered() throws Exception {
        // Create services
        VOTechnicalService techProduct = createTechnicalProduct(svcProv);
        List<VOServiceActivation> list = createServiceActivations(techProduct,
                globalMarketplace, 2);

        // Adjust initial settings
        adjustActivation(list, 0, false, false); // GLO/INA/HID
        adjustActivation(list, 1, false, true); // GLO/INA/VIS
        svcProv.setActivationStates(list);

        // Setup trigger
        setupTrigger();

        try {
            // activate first service => problem: no visible service exists!
            svcProv.activateService(list.get(0).getService());

        } catch (ServiceOperationException e) {
            Assert.assertTrue(e.getMessageKey().contains(
                    ServiceOperationException.Reason.NO_VISIBLE_ACTIVE_SERVICE
                            .toString()));

            // Validate result (must be unchanged!)
            VOServiceDetails sd1 = svcProv.getServiceDetails(list.get(0)
                    .getService());
            VOServiceDetails sd2 = svcProv.getServiceDetails(list.get(1)
                    .getService());
            VOCatalogEntry ce1 = mpProv.getMarketplacesForService(sd1).get(0);
            VOCatalogEntry ce2 = mpProv.getMarketplacesForService(sd2).get(0);

            Assert.assertEquals(ServiceStatus.INACTIVE, sd1.getStatus());
            Assert.assertEquals(ServiceStatus.INACTIVE, sd2.getStatus());

            Assert.assertFalse(ce1.isVisibleInCatalog());
            Assert.assertTrue(ce2.isVisibleInCatalog());

            throw e;
        }
    }

    @Test(expected = ServiceOperationException.class)
    public void testDeactivateVisibilityCheckTriggered() throws Exception {
        // Create services
        VOTechnicalService techProduct = createTechnicalProduct(svcProv);
        List<VOServiceActivation> list = createServiceActivations(techProduct,
                globalMarketplace, 2);

        // Adjust initial settings
        adjustActivation(list, 0, true, false); // GLO/ACT/HID
        adjustActivation(list, 1, false, true); // GLO/ACT/VIS
        List<VOService> services = svcProv.setActivationStates(list);
        for (int i = 0; i < list.size(); i++) {
            list.get(i).setService(services.get(i));
        }

        // Setup trigger
        setupTrigger();

        try {
            // deactivate second service => problem: no visible service exists!
            svcProv.deactivateService(list.get(1).getService());

        } catch (ServiceOperationException e) {
            Assert.assertTrue(e.getMessageKey().contains(
                    ServiceOperationException.Reason.NO_VISIBLE_ACTIVE_SERVICE
                            .toString()));

            // Validate result (must be unchanged!)
            VOServiceDetails sd1 = svcProv.getServiceDetails(list.get(0)
                    .getService());
            VOServiceDetails sd2 = svcProv.getServiceDetails(list.get(1)
                    .getService());
            VOCatalogEntry ce1 = mpProv.getMarketplacesForService(sd1).get(0);
            VOCatalogEntry ce2 = mpProv.getMarketplacesForService(sd2).get(0);

            Assert.assertEquals(ServiceStatus.ACTIVE, sd1.getStatus());
            Assert.assertEquals(ServiceStatus.ACTIVE, sd2.getStatus());

            Assert.assertFalse(ce1.isVisibleInCatalog());
            Assert.assertTrue(ce2.isVisibleInCatalog());

            throw e;
        }
    }

    /**
     * Prepare multiple suppliers
     */
    private List<VOServiceActivation> prepareListActivateMultipleSuppliers()
            throws Exception {
        // Create technical service
        final VOTechnicalService techProduct = createTechnicalProduct(svcProv);

        // Create services for supplier A
        List<VOServiceActivation> list = createServiceActivations(techProduct,
                globalMarketplace, 2);
        adjustActivation(list, 0, true, false); // ACT / NOTVIS
        adjustActivation(list, 1, true, true); // ACT / VIS
        svcProv.setActivationStates(list);

        // Create services for supplier B (use same technical service)
        String supplierUserKey2 = runTX(new Callable<String>() {
            @Override
            public String call() throws Exception {
                // add 2nd supplier
                Organization organization = Organizations.createOrganization(
                        mgr, OrganizationRoleType.SUPPLIER);
                PlatformUser user = Organizations.createUserForOrg(mgr,
                        organization, true, "admin2");
                String supplierUserKey = String.valueOf(user.getKey());
                Organization provider = Organizations.findOrganization(mgr,
                        providerOrgId);
                OrganizationReference reference = Organizations
                        .createSupplierToTechnologyProviderReference(mgr,
                                provider, organization);

                TechnicalProduct technicalProduct = mgr.find(
                        TechnicalProduct.class, techProduct.getKey());
                MarketingPermission permission = new MarketingPermission();
                permission.setOrganizationReference(reference);
                permission.setTechnicalProduct(technicalProduct);
                mgr.persist(permission);

                Marketplaces.grantPublishing(
                        organization,
                        Marketplaces.findMarketplace(mgr,
                                globalMarketplace.getMarketplaceId()), mgr,
                        false);
                return supplierUserKey;
            }
        });

        container.login(supplierUserKey2, ROLE_SERVICE_MANAGER);
        list = createServiceActivations(techProduct, globalMarketplace, 2);
        adjustActivation(list, 0, true, false); // ACT / NOTVIS
        adjustActivation(list, 1, true, true); // ACT / VIS
        List<VOService> services = svcProv.setActivationStates(list);
        for (int i = 0; i < list.size(); i++) {
            list.get(i).setService(services.get(i));
        }
        return list;
    }

    @Test(expected = ServiceOperationException.class)
    public void testListActivateMultipleSuppliers() throws Exception {
        // Prepare environment
        List<VOServiceActivation> list = prepareListActivateMultipleSuppliers();

        // Now supplier 2 deactivates => error (although other supplier has one
        // service active)
        List<VOServiceActivation> updateList = new ArrayList<VOServiceActivation>();
        updateList.add(list.get(1));
        adjustActivation(updateList, 0, true, false);

        try {
            // Set new activation states
            svcProv.setActivationStates(updateList);
        } catch (ServiceOperationException e) {
            Assert.assertTrue(e.getMessageKey().contains(
                    ServiceOperationException.Reason.NO_VISIBLE_ACTIVE_SERVICE
                            .toString()));
            throw e;
        }
    }

    @Test(expected = ServiceOperationException.class)
    public void testListActivateMultipleSuppliersTriggered() throws Exception {
        // Prepare environment
        List<VOServiceActivation> list = prepareListActivateMultipleSuppliers();

        // Setup trigger
        setupTrigger();

        // Now supplier 2 deactivates => error (although other supplier has one
        // service active)
        List<VOServiceActivation> updateList = new ArrayList<VOServiceActivation>();
        updateList.add(list.get(1));
        adjustActivation(updateList, 0, true, false);

        try {
            // Set new activation states
            svcProv.setActivationStates(updateList);
        } catch (ServiceOperationException e) {
            Assert.assertTrue(e.getMessageKey().contains(
                    ServiceOperationException.Reason.NO_VISIBLE_ACTIVE_SERVICE
                            .toString()));
            throw e;
        }
    }

    @Test(expected = EJBAccessException.class)
    public void suspendService_NotAuthorized() throws Exception {
        // requires administrator role
        container.login(providerUserKey, ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        try {
            svcProv.suspendService(null, null);
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    @Test(expected = EJBAccessException.class)
    public void resumeService_NotAuthorized() throws Exception {
        // requires administrator role
        container.login(providerUserKey, ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        try {
            svcProv.resumeService(null);
        } catch (EJBException e) {
            throw e.getCausedByException();
        }
    }

    private VOServiceDetails setProductStatus(final ServiceStatus status,
            final long key) throws Exception {
        return runTX(new Callable<VOServiceDetails>() {

            @Override
            public VOServiceDetails call() throws Exception {
                Product prod = mgr.getReference(Product.class, key);
                prod.setStatus(status);

                mgr.flush();

                return ProductAssembler.toVOProductDetails(prod, null,
                        new LinkedList<Event>(), false, new LocalizerFacade(
                                localizer, "en"));
            }
        });
    }

    protected void deleteEmptyTp_keyRecord(VOTechnicalService techProd)
            throws Exception {
        List<VOEventDefinition> events = techProd.getEventDefinitions();
        for (Iterator<VOEventDefinition> eventIterator = events.iterator(); eventIterator
                .hasNext();) {
            final VOEventDefinition voEventDef = eventIterator.next();

            Event e = runTX(new Callable<Event>() {
                @Override
                public Event call() throws Exception {
                    return mgr.find(Event.class, voEventDef.getKey());
                }
            });
            if (null == e.getTechnicalProduct_tkey()) {
                eventIterator.remove();
            }
        }
        List<VOParameterDefinition> paramDefinitions = techProd
                .getParameterDefinitions();
        for (Iterator<VOParameterDefinition> paramDefIterator = paramDefinitions
                .iterator(); paramDefIterator.hasNext();) {
            final VOParameterDefinition voParamDef = paramDefIterator.next();

            ParameterDefinition pdf = runTX(new Callable<ParameterDefinition>() {
                @Override
                public ParameterDefinition call() throws Exception {
                    return mgr.find(ParameterDefinition.class,
                            voParamDef.getKey());
                }
            });
            if (null == pdf.getTechnicalProduct_tkey()) {
                paramDefIterator.remove();
            }
        }
    }

}
