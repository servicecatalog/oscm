/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.subscriptionservice.bean;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.persistence.Query;

import org.junit.Test;
import org.oscm.accountservice.bean.MarketingPermissionServiceBean;
import org.oscm.applicationservice.bean.ApplicationServiceStub;
import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.converter.ParameterizedTypes;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.ImageResource;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationReference;
import org.oscm.domobjects.Parameter;
import org.oscm.domobjects.ParameterDefinition;
import org.oscm.domobjects.ParameterOption;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.PriceModel;
import org.oscm.domobjects.PricedEvent;
import org.oscm.domobjects.PricedOption;
import org.oscm.domobjects.PricedParameter;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.Session;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.SupportedCurrency;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.domobjects.TriggerDefinition;
import org.oscm.domobjects.TriggerProcess;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.domobjects.enums.OrganizationReferenceType;
import org.oscm.encrypter.AESEncrypter;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.i18nservice.bean.LocalizerServiceStub2;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.identityservice.bean.IdManagementStub;
import org.oscm.internal.intf.IdentityService;
import org.oscm.internal.intf.ServiceProvisioningService;
import org.oscm.internal.intf.SubscriptionService;
import org.oscm.internal.types.enumtypes.EventType;
import org.oscm.internal.types.enumtypes.ImageType;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ParameterType;
import org.oscm.internal.types.enumtypes.ParameterValueType;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.enumtypes.SessionType;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.vo.VOLocalizedText;
import org.oscm.internal.vo.VOSubscription;
import org.oscm.internal.vo.VOUda;
import org.oscm.serviceprovisioningservice.assembler.ProductAssembler;
import org.oscm.serviceprovisioningservice.bean.ServiceProvisioningServiceBean;
import org.oscm.serviceprovisioningservice.bean.TagServiceBean;
import org.oscm.sessionservice.bean.SessionManagementStub2;
import org.oscm.subscriptionservice.local.SubscriptionServiceLocal;
import org.oscm.taskhandling.local.TaskQueueServiceLocal;
import org.oscm.tenantprovisioningservice.bean.TenantProvisioningServiceBean;
import org.oscm.test.EJBTestBase;
import org.oscm.test.ReflectiveClone;
import org.oscm.test.data.Marketplaces;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.Products;
import org.oscm.test.data.Subscriptions;
import org.oscm.test.data.SupportedCountries;
import org.oscm.test.data.TechnicalProducts;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.CommunicationServiceStub;
import org.oscm.test.stubs.ConfigurationServiceStub;
import org.oscm.test.stubs.ImageResourceServiceStub;
import org.oscm.test.stubs.MarketplaceServiceStub;
import org.oscm.test.stubs.TriggerQueueServiceStub;
import org.oscm.triggerservice.local.TriggerMessage;
import org.oscm.triggerservice.local.TriggerProcessMessageData;
import org.oscm.types.enumtypes.ProvisioningType;

public class SubscriptionServiceBeanAllowOnBehalfActingIT extends EJBTestBase {

    protected DataService mgr;
    protected ApplicationServiceStub appMgmtStub;
    protected SubscriptionService subMgmt;
    protected SubscriptionServiceLocal subMgmtLocal;
    protected IdentityService idMgmt;
    protected ServiceProvisioningService servProv;
    protected LocalizerServiceLocal localizer;

    private Organization supplier;
    private Product testPrd;
    private Product testPrdOnbehalf;
    private Product testPrd2Onbehalf;
    private PlatformUser supplierAdminUser;
    private static int counter = 0;
    private TriggerDefinition td;

    @Override
    protected void setup(TestContainer container) throws Exception {
        AESEncrypter.generateKey();
        addBeansToTestContainer();

        mgr = container.get(DataService.class);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                createPaymentTypes(mgr);
                createOrganizationRoles(mgr);
                SupportedCountries.createSomeSupportedCountries(mgr);
                Organization operator = Organizations.createOrganization(mgr,
                        OrganizationRoleType.PLATFORM_OPERATOR);
                Marketplaces.createGlobalMarketplace(operator,
                        GLOBAL_MARKETPLACE_NAME, mgr);
                return null;
            }
        });

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                initMasterData();
                return null;
            }
        });
        container.login(supplierAdminUser.getKey(), ROLE_ORGANIZATION_ADMIN,
                ROLE_SERVICE_MANAGER);

        subMgmt = container.get(SubscriptionService.class);
        subMgmtLocal = container.get(SubscriptionServiceLocal.class);
        idMgmt = container.get(IdentityService.class);

        servProv = container.get(ServiceProvisioningService.class);
        localizer = container.get(LocalizerServiceLocal.class);
        ConfigurationServiceLocal cfg = container
                .get(ConfigurationServiceLocal.class);
        setUpDirServerStub(cfg);
        appMgmtStub.resetController();
    }

    private void addBeansToTestContainer() throws Exception {
        container.enableInterfaceMocking(true);

        container.addBean(new DataServiceBean());
        container.addBean(appMgmtStub = new ApplicationServiceStub());
        container.addBean(new SessionManagementStub2() {
            @Override
            public List<Session> getProductSessionsForSubscriptionTKey(
                    long subscriptionTKey) {
                Query query = mgr
                        .createNamedQuery("Session.findEntriesForSubscription");
                query.setParameter("subscriptionTKey",
                        Long.valueOf(subscriptionTKey));
                query.setParameter("sessionType", SessionType.SERVICE_SESSION);
                List<Session> activeSessions = ParameterizedTypes
                        .list(query.getResultList(), Session.class);
                return activeSessions;
            }
        });
        container.addBean(new IdManagementStub());
        container.addBean(new TenantProvisioningServiceBean());
        container.addBean(new CommunicationServiceStub());
        container.addBean(new LocalizerServiceStub2() {

            Map<String, List<VOLocalizedText>> map = new HashMap<>();

            @Override
            public void setLocalizedValues(long objectKey,
                    LocalizedObjectTypes objectType,
                    List<VOLocalizedText> values) {
                storeLocalizedResources(objectKey, objectType, values);
            }

            @Override
            public void storeLocalizedResources(long objectKey,
                    LocalizedObjectTypes objectType,
                    List<VOLocalizedText> values) {
                map.put(objectType + "_" + objectKey, values);
            }

            @Override
            public String getLocalizedTextFromDatabase(String localeString,
                    long objectKey, LocalizedObjectTypes objectType) {

                List<VOLocalizedText> list = map
                        .get(objectType + "_" + objectKey);
                if (list != null) {
                    for (VOLocalizedText localizedText : list) {
                        if (localeString.equals(localizedText.getLocale())) {
                            return localizedText.getText();
                        }
                    }
                }
                return "LocalizedTextFromDatabase";
            }

        });
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new ImageResourceServiceStub() {

            @Override
            public ImageResource read(long objectKey, ImageType imageType) {
                return null;
            }

        });
        container.addBean(mock(TaskQueueServiceLocal.class));
        container.addBean(new TriggerQueueServiceStub() {
            @Override
            public List<TriggerProcessMessageData> sendSuspendingMessages(
                    List<TriggerMessage> messageData) {
                TriggerProcess tp = new TriggerProcess();
                tp.setTriggerDefinition(td);
                tp.setUser(supplierAdminUser);
                TriggerProcessMessageData data = new TriggerProcessMessageData(
                        tp, null);
                return Collections.singletonList(data);
            }

            @Override
            public void sendAllNonSuspendingMessages(
                    List<TriggerMessage> messageData) {
            }
        });
        container.addBean(new TagServiceBean());
        container.addBean(new MarketingPermissionServiceBean());
        container.addBean(new MarketplaceServiceStub());
        container.addBean(new ServiceProvisioningServiceBean());
        container.addBean(new SubscriptionServiceBean());
        container.addBean(new TerminateSubscriptionBean());
        container.addBean(new ManageSubscriptionBean());
    }

    /**
     * Initialize test database with master data (products and price models)
     * 
     * @return The key of the created admin user.
     * 
     * @throws NonUniqueBusinessKeyException
     * @throws ObjectNotFoundException
     */
    private void initMasterData()
            throws NonUniqueBusinessKeyException, ObjectNotFoundException {
        // add currency
        SupportedCurrency sc = new SupportedCurrency();
        sc.setCurrency(Currency.getInstance("EUR"));
        mgr.persist(sc);

        // create technology provider and supplier organization
        supplier = Organizations.createOrganization(mgr,
                OrganizationRoleType.SUPPLIER,
                OrganizationRoleType.TECHNOLOGY_PROVIDER);
        supplierAdminUser = Organizations.createUserForOrg(mgr, supplier, true,
                "admin");

        // --- create products with allowing on behalf activated ---
        TechnicalProduct product1 = TechnicalProducts.createTechnicalProduct(
                mgr, supplier, "technialproduct1", false,
                ServiceAccessType.LOGIN);
        product1.setAllowingOnBehalfActing(true);
        prepareTechnicalProduct(product1);
        testPrdOnbehalf = createProductsFor(supplier, product1, 1).get(0);

        TechnicalProduct product2 = TechnicalProducts.createTechnicalProduct(
                mgr, supplier, "technialproduct2", false,
                ServiceAccessType.LOGIN);
        product2.setAllowingOnBehalfActing(true);
        prepareTechnicalProduct(product2);
        testPrd2Onbehalf = createProductsFor(supplier, product2, 1).get(0);
        // --- ---

        // --- create product with allowing on behalf deactivated ---
        TechnicalProduct product3 = TechnicalProducts.createTechnicalProduct(
                mgr, supplier, "technialproduct3", false,
                ServiceAccessType.LOGIN);
        product3.setAllowingOnBehalfActing(false);
        prepareTechnicalProduct(product3);
        testPrd = createProductsFor(supplier, product3, 1).get(0);

        TechnicalProduct product4 = TechnicalProducts.createTechnicalProduct(
                mgr, supplier, "technialproduct4", true,
                ServiceAccessType.LOGIN);
        product4.setAllowingOnBehalfActing(false);
        prepareTechnicalProduct(product4);
        createProductsFor(supplier, product4, 1).get(0);
        // --- ---
    }

    /**
     * Creates a parameter definition for the technical product and also an
     * option for it. Furthermore an event definition for the technical product
     * is created.
     * 
     * @param tProd
     *            The technical product to be updated.
     * @throws NonUniqueBusinessKeyException
     */
    private void prepareTechnicalProduct(TechnicalProduct tProd)
            throws NonUniqueBusinessKeyException {
        ParameterDefinition pd = TechnicalProducts.addParameterDefinition(
                ParameterValueType.INTEGER, "intParam",
                ParameterType.SERVICE_PARAMETER, tProd, mgr, null, null, true);
        ParameterOption option = new ParameterOption();
        option.setOptionId("OPT");
        option.setParameterDefinition(pd);
        List<ParameterOption> list = new ArrayList<>();
        list.add(option);
        pd.setOptionList(list);
        mgr.persist(option);
        TechnicalProducts.addEvent("eventId", EventType.SERVICE_EVENT, tProd,
                mgr);
    }

    private List<Product> createProductsFor(Organization supplier,
            TechnicalProduct tProd, int count)
            throws NonUniqueBusinessKeyException {

        List<Product> products = new ArrayList<>();
        Product prod;
        ParameterDefinition paramDef = tProd.getParameterDefinitions().get(0);
        for (int i = 1; i <= count; i++) {
            prod = Products.createProduct(supplier, tProd, (i % 2 != 1),
                    getProductId(tProd, counter++), null, mgr);

            Parameter param = new Parameter();
            param.setParameterDefinition(paramDef);
            param.setParameterSet(prod.getParameterSet());
            param.setValue("1");
            mgr.persist(param);

            PricedParameter pricedParam = new PricedParameter();
            pricedParam.setParameter(param);
            pricedParam.setPricePerUser(new BigDecimal(1));
            PriceModel priceModel = prod.getPriceModel();
            pricedParam.setPriceModel(priceModel);

            PricedOption option = new PricedOption();
            option.setPricedParameter(pricedParam);
            option.setPricePerUser(new BigDecimal(2));
            option.setParameterOptionKey(
                    paramDef.getOptionList().get(0).getKey());

            PricedEvent pEvent = new PricedEvent();
            pEvent.setEvent(tProd.getEvents().get(0));
            pEvent.setEventPrice(new BigDecimal(250));
            pEvent.setPriceModel(priceModel);

            mgr.persist(pricedParam);
            mgr.persist(option);
            mgr.persist(pEvent);

            products.add((Product) ReflectiveClone.clone(prod));
        }
        return products;
    }

    private String getProductId(TechnicalProduct tProd, int i) {
        String result = "Product" + i;
        if (tProd.getProvisioningType() == ProvisioningType.ASYNCHRONOUS) {
            result += tProd.getProvisioningType().name();
        }
        return result;
    }

    private OrganizationReference getOrganizationReference(
            Organization sourceOrganization, Organization targetOrganization) {
        for (OrganizationReference reference : targetOrganization
                .getSourcesForType(
                        OrganizationReferenceType.ON_BEHALF_ACTING)) {
            if (reference.getSource().getKey() == sourceOrganization.getKey()
                    && reference.getTargetKey() == targetOrganization
                            .getKey()) {
                return reference;
            }
        }
        return null;
    }

    @Test
    public void testCreateOneSubscription_AllowingOnbehalf() throws Exception {
        // subscribe
        String subId = Long.toString(System.currentTimeMillis());
        final VOSubscription sub = subMgmt.subscribeToService(
                Subscriptions.createVOSubscription(subId),
                ProductAssembler.toVOProduct(testPrdOnbehalf,
                        new LocalizerFacade(localizer, "en")),
                null, null, null, new ArrayList<VOUda>());

        // assert
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Subscription subscription = mgr.find(Subscription.class,
                        sub.getKey());
                Organization sourceOrganization = subscription.getProduct()
                        .getTechnicalProduct().getOrganization();
                Organization targetOrganization = subscription
                        .getOrganization();
                assertNotNull(getOrganizationReference(sourceOrganization,
                        targetOrganization));
                return null;
            }
        });
    }

    @Test
    public void testCreateTwoSubscriptions_AllowingOnbehalf() throws Exception {
        // create two subscriptions
        final String subscriptionId1 = Long
                .toString(System.currentTimeMillis());
        final String subscriptionId2 = Long
                .toString(System.currentTimeMillis() + 1);
        final VOSubscription sub = subMgmt.subscribeToService(
                Subscriptions.createVOSubscription(subscriptionId1),
                ProductAssembler.toVOProduct(testPrdOnbehalf,
                        new LocalizerFacade(localizer, "en")),
                null, null, null, new ArrayList<VOUda>());
        subMgmt.subscribeToService(
                Subscriptions.createVOSubscription(subscriptionId2),
                ProductAssembler.toVOProduct(testPrdOnbehalf,
                        new LocalizerFacade(localizer, "en")),
                null, null, null, new ArrayList<VOUda>());

        // assert
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                assertTrue(subMgmt.getSubscriptionIdentifiers()
                        .contains(subscriptionId1));
                assertTrue(subMgmt.getSubscriptionIdentifiers()
                        .contains(subscriptionId2));

                Subscription subscription = mgr.find(Subscription.class,
                        sub.getKey());
                Organization sourceOrganization = subscription.getProduct()
                        .getTechnicalProduct().getOrganization();
                Organization targetOrganization = subscription
                        .getOrganization();
                assertNotNull(getOrganizationReference(sourceOrganization,
                        targetOrganization));
                return null;
            }
        });
    }

    @Test
    public void testCreateOneSubscription() throws Exception {
        // subscribe
        String subId = Long.toString(System.currentTimeMillis());
        final VOSubscription sub = subMgmt.subscribeToService(
                Subscriptions.createVOSubscription(subId),
                ProductAssembler.toVOProduct(testPrd,
                        new LocalizerFacade(localizer, "en")),
                null, null, null, new ArrayList<VOUda>());

        // assert
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Subscription subscription = mgr.find(Subscription.class,
                        sub.getKey());
                Organization sourceOrganization = subscription.getProduct()
                        .getTechnicalProduct().getOrganization();
                Organization targetOrganization = subscription
                        .getOrganization();
                assertNull(getOrganizationReference(sourceOrganization,
                        targetOrganization));
                return null;
            }
        });
    }

    @Test
    public void testRemoveLastSubscription_AllowingOnbehalf() throws Exception {
        // subscribe and then remove subscription
        String subId = Long.toString(System.currentTimeMillis());
        final VOSubscription sub = subMgmt.subscribeToService(
                Subscriptions.createVOSubscription(subId),
                ProductAssembler.toVOProduct(testPrdOnbehalf,
                        new LocalizerFacade(localizer, "en")),
                null, null, null, new ArrayList<VOUda>());
        subMgmt.unsubscribeFromService(subId);

        // assert: on behalf reference must not be present
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Subscription subscription = mgr.find(Subscription.class,
                        sub.getKey());
                Organization sourceOrganization = subscription.getProduct()
                        .getTechnicalProduct().getOrganization();
                Organization targetOrganization = subscription
                        .getOrganization();
                assertNull(getOrganizationReference(sourceOrganization,
                        targetOrganization));
                return null;
            }
        });
    }

    @Test
    public void testRemoveSubscription_AllowingOnbehalf() throws Exception {
        // create 2 subscriptions, then remove one subscription
        String subId = Long.toString(System.currentTimeMillis());
        String subId2 = Long.toString(System.currentTimeMillis() + 1);
        final VOSubscription sub = subMgmt.subscribeToService(
                Subscriptions.createVOSubscription(subId),
                ProductAssembler.toVOProduct(testPrdOnbehalf,
                        new LocalizerFacade(localizer, "en")),
                null, null, null, new ArrayList<VOUda>());
        subMgmt.subscribeToService(Subscriptions.createVOSubscription(subId2),
                ProductAssembler.toVOProduct(testPrdOnbehalf,
                        new LocalizerFacade(localizer, "en")),
                null, null, null, new ArrayList<VOUda>());
        subMgmt.unsubscribeFromService(subId2);

        // assert: on behalf reference must not be present
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Subscription subscription = mgr.find(Subscription.class,
                        sub.getKey());
                Organization sourceOrganization = subscription.getProduct()
                        .getTechnicalProduct().getOrganization();
                Organization targetOrganization = subscription
                        .getOrganization();
                assertNotNull(getOrganizationReference(sourceOrganization,
                        targetOrganization));
                return null;
            }
        });
    }

    @Test
    public void testRemoveSubscription() throws Exception {
        // subscribe and then remove subscription
        String subId = Long.toString(System.currentTimeMillis());
        final VOSubscription sub = subMgmt.subscribeToService(
                Subscriptions.createVOSubscription(subId),
                ProductAssembler.toVOProduct(testPrd,
                        new LocalizerFacade(localizer, "en")),
                null, null, null, new ArrayList<VOUda>());
        subMgmt.unsubscribeFromService(subId);

        // assert: on behalf reference must not be present
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Subscription subscription = mgr.find(Subscription.class,
                        sub.getKey());
                Organization sourceOrganization = subscription.getProduct()
                        .getTechnicalProduct().getOrganization();
                Organization targetOrganization = subscription
                        .getOrganization();
                assertNull(getOrganizationReference(sourceOrganization,
                        targetOrganization));
                return null;
            }
        });
    }

    @Test
    public void testCreateTwoSubscriptionsBasedOnTwoTechnicalServices_OnBehalf()
            throws Exception {

        // create 2 subscriptions for two tech. prod.
        String subId1 = Long.toString(System.currentTimeMillis());
        String subId2 = Long.toString(System.currentTimeMillis() + 1);
        VOSubscription sub1 = subMgmt.subscribeToService(
                Subscriptions.createVOSubscription(subId1),
                ProductAssembler.toVOProduct(testPrdOnbehalf,
                        new LocalizerFacade(localizer, "en")),
                null, null, null, new ArrayList<VOUda>());
        final long sub1Key = sub1.getKey();

        subMgmt.subscribeToService(Subscriptions.createVOSubscription(subId2),
                ProductAssembler.toVOProduct(testPrd2Onbehalf,
                        new LocalizerFacade(localizer, "en")),
                null, null, null, new ArrayList<VOUda>());

        // assert: on behalf reference must be present
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Subscription subscription = mgr.find(Subscription.class,
                        sub1Key);
                Organization sourceOrganization = subscription.getProduct()
                        .getTechnicalProduct().getOrganization();
                Organization targetOrganization = subscription
                        .getOrganization();
                assertNotNull(getOrganizationReference(sourceOrganization,
                        targetOrganization));
                return null;
            }
        });
    }

    @Test
    public void testRemoveLastSubscripionFromSecondTechnicalService_OnBehalf()
            throws Exception {

        // one subscription for each of the 2 different technical services
        String subId1 = Long.toString(System.currentTimeMillis());
        String subId2 = Long.toString(System.currentTimeMillis() + 1);
        subMgmt.subscribeToService(Subscriptions.createVOSubscription(subId1),
                ProductAssembler.toVOProduct(testPrdOnbehalf,
                        new LocalizerFacade(localizer, "en")),
                null, null, null, new ArrayList<VOUda>());
        final VOSubscription sub2 = subMgmt.subscribeToService(
                Subscriptions.createVOSubscription(subId2),
                ProductAssembler.toVOProduct(testPrd2Onbehalf,
                        new LocalizerFacade(localizer, "en")),
                null, null, null, new ArrayList<VOUda>());
        subMgmt.unsubscribeFromService(subId1);

        // assert: on behalf reference must be present
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Subscription subscription = mgr.find(Subscription.class,
                        sub2.getKey());
                Organization sourceOrganization = subscription.getProduct()
                        .getTechnicalProduct().getOrganization();
                Organization targetOrganization = subscription
                        .getOrganization();
                assertNotNull(getOrganizationReference(sourceOrganization,
                        targetOrganization));
                return null;
            }
        });
    }

    /**
     * given: one subscription based on technical service with enabled on
     * behalf, second subscription based on technical service with disabled on
     * behalf.
     * 
     * @throws Exception
     */
    @Test
    public void testRemoveLastSubscripionFromTechnicalService_OnBehalf()
            throws Exception {

        String subId1 = Long.toString(System.currentTimeMillis());
        String subId2 = Long.toString(System.currentTimeMillis() + 1);
        subMgmt.subscribeToService(Subscriptions.createVOSubscription(subId1),
                ProductAssembler.toVOProduct(testPrdOnbehalf,
                        new LocalizerFacade(localizer, "en")),
                null, null, null, new ArrayList<VOUda>());
        final VOSubscription sub2 = subMgmt.subscribeToService(
                Subscriptions.createVOSubscription(subId2),
                ProductAssembler.toVOProduct(testPrd,
                        new LocalizerFacade(localizer, "en")),
                null, null, null, new ArrayList<VOUda>());
        subMgmt.unsubscribeFromService(subId1);

        // assert: on behalf reference must be present
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Subscription subscription = mgr.find(Subscription.class,
                        sub2.getKey());
                Organization sourceOrganization = subscription.getProduct()
                        .getTechnicalProduct().getOrganization();
                Organization targetOrganization = subscription
                        .getOrganization();
                assertNull(getOrganizationReference(sourceOrganization,
                        targetOrganization));
                return null;
            }
        });
    }

}
