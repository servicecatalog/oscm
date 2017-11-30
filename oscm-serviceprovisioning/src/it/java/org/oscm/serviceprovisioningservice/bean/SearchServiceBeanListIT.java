/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.serviceprovisioningservice.bean;

import org.junit.*;
import org.oscm.accountservice.assembler.OrganizationAssembler;
import org.oscm.accountservice.local.AccountServiceLocal;
import org.oscm.accountservice.local.MarketingPermissionServiceLocal;
import org.oscm.categorizationService.local.CategorizationServiceLocal;
import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.converter.ParameterizedTypes;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.bean.HibernateIndexer;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.*;
import org.oscm.encrypter.AESEncrypter;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.i18nservice.local.ImageResourceServiceLocal;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.identityservice.local.LdapSettingsManagementServiceLocal;
import org.oscm.internal.intf.*;
import org.oscm.internal.types.enumtypes.*;
import org.oscm.internal.types.exception.DomainObjectException.ClassEnum;
import org.oscm.internal.types.exception.IllegalArgumentException;
import org.oscm.internal.types.exception.*;
import org.oscm.internal.vo.*;
import org.oscm.landingpageService.local.LandingpageServiceLocal;
import org.oscm.marketplaceservice.local.MarketplaceServiceLocal;
import org.oscm.provisioning.data.User;
import org.oscm.serviceprovisioningservice.local.ProductSearchResult;
import org.oscm.serviceprovisioningservice.local.SearchServiceLocal;
import org.oscm.subscriptionservice.bean.SubscriptionListServiceBean;
import org.oscm.subscriptionservice.bean.SubscriptionServiceBean;
import org.oscm.taskhandling.local.TaskQueueServiceLocal;
import org.oscm.tenantprovisioningservice.bean.TenantProvisioningServiceBean;
import org.oscm.tenantprovisioningservice.vo.TenantProvisioningResult;
import org.oscm.test.StaticEJBTestBase;
import org.oscm.test.TestDateFactory;
import org.oscm.test.data.*;
import org.oscm.test.ejb.FifoJMSQueue;
import org.oscm.test.stubs.*;
import org.oscm.triggerservice.local.TriggerMessage;
import org.oscm.triggerservice.local.TriggerProcessMessageData;
import org.oscm.types.constants.Configuration;
import org.oscm.usergroupservice.bean.UserGroupServiceLocalBean;

import javax.ejb.EJBException;
import javax.persistence.Query;
import java.io.File;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.Callable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
@Ignore
public class SearchServiceBeanListIT extends StaticEJBTestBase {

    private static final String FUJITSU = "FUJITSU";

    // will be used for search
    private static final String TAG1 = "cool";
    private static final String TAG2 = "wicked";
    private static final String TAG3 = "one-two";
    private static final String TAG4 = "fishing";
    private static final String CAT1 = "health";
    private static final String CAT2 = "computer";
    private static final String BLA = "bla";

    private static final String CAT1_DE = CAT1 + " deutsch";
    private static final String CAT2_DE = CAT2 + " deutsch";

    private static final String PM_DESC1 = "free";
    private static final String PM_DESC2 = "basic";
    private static final String PM_DESC3 = "flexible";

    private static final String BLANK = " ";
    private static final String THREE_BYTE_SPACE = "\u3000";
    private static final String COMMA = ",";
    private static final String PERIOD = ".";
    private static final String QMARK = "?";
    private static final String COLON = ":";

    private static final int SEARCHLIMIT_ONLYCOUNT = 0;
    private static final int SEARCHLIMIT_UNLIMITED = -1;

    // **************************************************
    // The following phrases (P) are constructed:
    // special characters appended mustn't make problems
    // P[0] : ""
    // P[1] : "cool"
    // P[2] : "wicked"
    // P[3] : "bla"
    // P[4] : "cool, bla."
    // P[5] : "cool,wicked"
    // P[6] : "cool one-two?"
    // P[7] : "cool: fishing"
    // P[8] : "wicked bla."
    // P[9] : "one-two: bla?"
    // P[10]: "one-two, fishing."
    // **************************************************

    private static final String[] PHRASE = new String[] { "", TAG1, TAG2, BLA,
            TAG1 + COMMA + BLANK + BLA + PERIOD, TAG1 + COMMA + TAG2,
            TAG1 + BLANK + TAG3 + QMARK, TAG1 + COLON + BLANK + TAG4,
            TAG2 + BLANK + BLA + PERIOD, TAG3 + COLON + BLANK + BLA + QMARK,
            TAG3 + COMMA + BLANK + TAG4 + PERIOD };

    // **************************************************
    // The following price model descriptions (PMD) are constructed:
    // PMD[0]: "free"
    // PMD[1]: "basic"
    // PMD[2]: "flexible"
    // PMD[3]: "free basic"
    // PMD[4]: "free flexible"
    // PMD[5]: "basic flexible"
    // **************************************************
    private static final String[] PM_DESCRIPTION = new String[] { PM_DESC1,
            PM_DESC2, PM_DESC3, PM_DESC1 + BLANK + PM_DESC2,
            PM_DESC1 + BLANK + PM_DESC3, PM_DESC2 + BLANK + PM_DESC3 };

    private static final String TEMP_INDEX_BASE_DIR = "tempIndexDir";

    private static final String[] ENABLED_PAYMENTTYPES = new String[] {
            PaymentType.CREDIT_CARD, PaymentType.INVOICE };

    private static String sysPropertyBaseDir = null;

    private static ServiceProvisioningService sps;
    private static SearchService search;
    private static SearchServiceLocal searchLocal;
    private static DataService ds;
    private static MarketplaceService mps;
    private static LocalizerServiceLocal localizer;
    private static SubscriptionService ss;
    private static CategorizationService cs;

    // mocked JMS queue for indexing requests
    private static FifoJMSQueue indexerQueue;
    private static HibernateIndexer irl;

    private static String providerOrgId;
    private static String supplierOrgId;
    private static String customerOrgId;

    private static long providerUserKey;
    private static long supplierUserKey;
    private static long customerUserKey;

    private static Organization provider;
    private static Organization supplier;
    private static Organization customer;
    private static Organization platformOperatorOrg;
    private static OrganizationReference orgRef;

    private static final List<VOService> allServices = new ArrayList<>();

    private static List<VOService> localPublicVisible;
    private static List<VOService> localLoggedInVisible;
    private static List<VOService> globalPublicVisible;
    private static List<VOService> globalLoggedInVisible;

    private static String TAGGING_MIN_SCORE_TEST = "1";
    private static int svcCounter = 0;

    private static long platformOperatorAdminKey;
    private static long platformOperatorUserKey;
    private static long unitAdminUserKey;
    private static long suspendedServiceKey;
    private static long suspendedCustServiceKey;

    private static List<PaymentType> paymentTypes;
    private final Map<String, VOPaymentInfo> voPaymentInfos = new HashMap<>();

    private static VOServiceDetails voSuspendedProduct;

    private static List<VOCategory> categories;
    private static List<VOService> servicesForPublicCatalog;

    private static UserGroupServiceLocalBean userGroupServiceLocalBean;
    private static long endUerKey;

    private static PlatformUser unitAdminUser;

    @BeforeClass
    public static void setupOnce() throws Exception {
        AESEncrypter.generateKey();
        TestDateFactory.restoreDefault();
        PERSISTENCE.clearEntityManagerFactoryCache();
        System.setProperty("hibernate.search.worker.jms.connection_factory",
                "jms/bss/indexerQueueFactory");
        System.setProperty("hibernate.search.worker.jms.queue",
                "jms/bss/indexerQueue");

        // store property base directory to be able to reset it later
        sysPropertyBaseDir = System
                .getProperty("hibernate.search.default.indexBase");

        // for all succeeding tests, put index into temp directory
        File indexBaseDir = new File(TEMP_INDEX_BASE_DIR);
        if (!indexBaseDir.exists()) {
            indexBaseDir.mkdir();
        }
        System.setProperty("hibernate.search.default.indexBase",
                TEMP_INDEX_BASE_DIR);

        enableHibernateSearchListeners(true);
        indexerQueue = createIndexerQueue();
        container.enableInterfaceMocking(true);
        container.login("1");
        container.addBean(new DataServiceBean());
        container.addBean(mock(ImageResourceServiceLocal.class));
        container.addBean(new TaskQueueServiceStub());
        container.addBean(new ConfigurationServiceStub() {

            @Override
            public ConfigurationSetting getConfigurationSetting(
                    ConfigurationKey informationId, String contextId) {
                if (ConfigurationKey.TAGGING_MIN_SCORE.equals(informationId)) {
                    return new ConfigurationSetting(
                            ConfigurationKey.TAGGING_MIN_SCORE,
                            Configuration.GLOBAL_CONTEXT,
                            TAGGING_MIN_SCORE_TEST);
                }
                return super.getConfigurationSetting(informationId, contextId);
            }

        });
        container.addBean(mock(LocalizerServiceLocal.class));
        localizer = container.get(LocalizerServiceLocal.class);
        container.addBean(new ServiceProvisioningServiceLocalizationBean());
        container.addBean(mock(CategorizationServiceLocal.class));
        cs = container.get(CategorizationService.class);
        container.addBean(new ApplicationServiceStub() {

            @Override
            public void validateCommunication(TechnicalProduct techProduct) {

            }

            @Override
            public User[] createUsers(Subscription subscription,
                    List<UsageLicense> usageLicenses) {
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
        container.addBean(new CommunicationServiceStub());
        container.addBean(new LdapAccessServiceStub());
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
                    user.setKey(platformOperatorAdminKey);
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
                    Subscription subscription) {
                TenantProvisioningResult result = new TenantProvisioningResult();
                result.setAsyncProvisioning(false);
                result.setProductInstanceId("productInstanceId");
                return result;
            }

        });
        container.addBean(mock(LandingpageServiceLocal.class));
        container.addBean(mock(SubscriptionListServiceBean.class));
        container.addBean(new SubscriptionServiceBean());
        ss = container.get(SubscriptionService.class);
        container.addBean(new PaymentServiceStub());
        container.addBean(mock(MarketingPermissionServiceLocal.class));
        container.addBean(mock(LdapSettingsManagementServiceLocal.class));
        userGroupServiceLocalBean = mock(UserGroupServiceLocalBean.class);
        container.addBean(userGroupServiceLocalBean);
        container.addBean(mock(AccountServiceLocal.class));
        container.addBean(new TagServiceBean());
        container.addBean(new ServiceProvisioningServiceBean());
        container.addBean(new SearchServiceInternalBean());
        container.addBean(new SearchServiceBean());
        container.addBean(mock(MarketplaceServiceLocal.class));
        container.addBean(mock(MarketplaceService.class));
        container.addBean(new HibernateIndexer());

        setUpDirServerStub(container.get(ConfigurationServiceLocal.class));
        sps = container.get(ServiceProvisioningService.class);
        search = container.get(SearchService.class);
        searchLocal = container.get(SearchServiceLocal.class);
        ds = container.get(DataService.class);

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

        mps = container.get(MarketplaceService.class);

        // create global marketplace + corresponding owner
        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                platformOperatorOrg = Organizations.createPlatformOperator(ds);
                Marketplaces.createMarketplace(platformOperatorOrg, FUJITSU,
                        false, ds);

                PlatformUser platformOperatorUser = Organizations
                        .createUserForOrg(ds, platformOperatorOrg, true,
                                "mpOwnerAdmin");
                platformOperatorAdminKey = platformOperatorUser.getKey();
                PlatformUsers.grantRoles(ds, platformOperatorUser,
                        UserRoleType.MARKETPLACE_OWNER);
                platformOperatorUser = Organizations.createUserForOrg(ds,
                        platformOperatorOrg, false, "mpOwnerUser");
                platformOperatorUserKey = platformOperatorUser.getKey();
                return null;
            }
        });

        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {

                unitAdminUser = Organizations.createUserForOrg(ds,
                        platformOperatorOrg, false, "unitAdmin");
                unitAdminUser = Organizations.createUserForOrg(ds,
                        platformOperatorOrg, false, "unitAdminUser");
                PlatformUsers.grantRoles(ds, unitAdminUser,
                        UserRoleType.UNIT_ADMINISTRATOR);
                unitAdminUserKey = unitAdminUser.getKey();
                return null;
            }
        });

        provider = runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization organization = Organizations.createOrganization(ds,
                        OrganizationRoleType.TECHNOLOGY_PROVIDER,
                        OrganizationRoleType.SUPPLIER);
                PlatformUser user = Organizations.createUserForOrg(ds,
                        organization, true, "admin");

                providerUserKey = user.getKey();
                return organization;
            }
        });
        providerOrgId = provider.getOrganizationId();

        supplier = runTX(new Callable<Organization>() {

            @Override
            public Organization call() throws Exception {
                Organization organization = Organizations.createOrganization(ds,
                        OrganizationRoleType.SUPPLIER);
                Marketplaces.ensureMarketplace(organization,
                        organization.getOrganizationId(), ds);
                PlatformUser user = Organizations.createUserForOrg(ds,
                        organization, true, "admin");
                supplierUserKey = user.getKey();
                PlatformUsers.grantRoles(ds, user,
                        UserRoleType.SERVICE_MANAGER);
                Organization provider = Organizations.findOrganization(ds,
                        providerOrgId);
                orgRef = Organizations
                        .createSupplierToTechnologyProviderReference(ds,
                                provider, organization);
                Marketplaces.grantPublishing(organization,
                        Marketplaces.findMarketplace(ds, FUJITSU), ds, false);
                PaymentTypes.enableForSupplier(platformOperatorOrg,
                        organization, ds, false, true, ENABLED_PAYMENTTYPES);
                return organization;
            }

        });
        supplierOrgId = supplier.getOrganizationId();

        customer = runTX(new Callable<Organization>() {

            @Override
            public Organization call() throws Exception {
                Organization supplier = Organizations.findOrganization(ds,
                        supplierOrgId);
                Organization organization = Organizations.createCustomer(ds,
                        supplier);
                PlatformUser user = Organizations.createUserForOrg(ds,
                        organization, true, "admin");
                customerUserKey = user.getKey();
                PlatformUser normalUser = PlatformUsers.createUser(ds, "userId",
                        organization);
                endUerKey = normalUser.getKey();
                return organization;
            }

        });

        customerOrgId = customer.getOrganizationId();
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                paymentTypes = new ArrayList<>();
                paymentTypes.add(findPaymentType(PaymentType.INVOICE, ds));
                return null;
            }
        });
        createAvailablePayment(customer);
        dataSetup();
        setupExpected();
    }

    @AfterClass
    public static void tearDownClass() {
        PERSISTENCE.clearEntityManagerFactoryCache();
        if (sysPropertyBaseDir != null && sysPropertyBaseDir.length() > 0) {
            System.setProperty("hibernate.search.default.indexBase",
                    sysPropertyBaseDir);
        } else {
            System.clearProperty("hibernate.search.default.indexBase");
        }
        // delete the created temp directory and all of its content
        File[] childDirs = new File(TEMP_INDEX_BASE_DIR).listFiles();
        for (int i = childDirs.length - 1; i >= 0; i--) {
            File[] files = childDirs[i].listFiles();
            for (int j = files.length - 1; j >= 0; j--) {
                files[j].delete();
            }
            childDirs[i].delete();
        }
        new File(TEMP_INDEX_BASE_DIR).delete();
    }

    @Before
    public void setup() {

    }

    @After
    public void cleanUp() {
        // if a user is logged in, log him out to prevent impact on other tests
        container.logout();
    }

    private static void dataSetup() throws Exception {
        // register technical products and add tags in different locale
        container.login(providerUserKey, ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        List<VOTechnicalService> tps = createTechnicalProducts();
        setLocaleCurrentUser("en", providerUserKey);
        System.out.println("------------- TAGS ----------------");
        VOTechnicalService tp1 = addTags(tps, 0,
                Arrays.asList(new String[] { TAG1, TAG2 }));
        System.out.println(
                tp1.getTechnicalServiceId() + " (en) : " + TAG1 + " " + TAG2);
        VOTechnicalService tp2 = addTags(tps, 1,
                Arrays.asList(new String[] { TAG1, TAG3 }));
        System.out.println(
                tp2.getTechnicalServiceId() + " (en) : " + TAG1 + " " + TAG3);
        VOTechnicalService tp3 = addTags(tps, 2,
                Arrays.asList(new String[] { TAG2 }));
        System.out.println(tp3.getTechnicalServiceId() + " (en) : " + TAG2);
        setLocaleCurrentUser("de", providerUserKey);
        VOTechnicalService tp4 = addTags(tps, 3,
                Arrays.asList(new String[] { TAG1, TAG3 }));
        System.out.println(
                tp4.getTechnicalServiceId() + " (de) : " + TAG1 + " " + TAG3);
        VOTechnicalService tp5 = addTags(tps, 4,
                Arrays.asList(new String[] { TAG2, TAG4 }));
        System.out.println(
                tp5.getTechnicalServiceId() + " (de) : " + TAG2 + " " + TAG4);
        container.logout();

        // create some services for all technical services
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Marketplace mp = Marketplaces.createMarketplace(supplier,
                        supplier.getOrganizationId(), false, ds);
                Marketplaces.grantPublishing(supplier, mp, ds, false);
                return null;
            }
        });

        setupServices(tp1);
        setupServices(tp2);
        setupServices(tp3);
        setupServices(tp4);
        setupServices(tp5);

        voSuspendedProduct = createProduct(tp1,
                "suspended" + tp1.getTechnicalServiceId(), new BigDecimal(3),
                "en");
        voSuspendedProduct = customizeAttributesAndPriceModel(
                voSuspendedProduct);
        publish(voSuspendedProduct, FUJITSU, true);
        suspendedServiceKey = voSuspendedProduct.getKey();
        VOServiceDetails voSuspendedCustProd = customizeAttributesAndPriceModelForCustomer(
                voSuspendedProduct, getOrganizationForOrgId(customerOrgId));
        suspendedCustServiceKey = voSuspendedCustProd.getKey();
        container.logout();

        container.login(platformOperatorAdminKey, ROLE_MARKETPLACE_OWNER);
        categories = runTX(new Callable<List<VOCategory>>() {
            @Override
            public List<VOCategory> call() throws Exception {
                final Marketplace m = Marketplaces.findMarketplace(ds, FUJITSU);
                final String mpId = m.getMarketplaceId();
                VOCategory category1 = new VOCategory();
                category1.setCategoryId(CAT1);
                category1.setName(CAT1 + " english");
                category1.setMarketplaceId(mpId);
                VOCategory category2 = new VOCategory();
                category2.setCategoryId(CAT2);
                category2.setName(CAT2 + " english");
                category2.setMarketplaceId(mpId);
                container.login(platformOperatorAdminKey,
                        ROLE_MARKETPLACE_OWNER);
                cs.saveCategories(Arrays.asList(category1, category2), null,
                        "en");
                final List<VOCategory> categories = cs.getCategories(mpId,
                        "de");
                categories.get(0).setName(CAT1 + " deutsch");
                categories.get(1).setName(CAT2 + " deutsch");
                cs.saveCategories(categories, null, "de");
                return categories;
            }
        });

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                servicesForPublicCatalog = sps
                        .getServicesForMarketplace(FUJITSU);
                assignCategoryToProduct(servicesForPublicCatalog.get(0),
                        categories.get(0));
                assignCategoryToProduct(servicesForPublicCatalog.get(1),
                        categories.get(0));
                assignCategoryToProduct(servicesForPublicCatalog.get(0),
                        categories.get(1));
                return null;
            }
        });
        container.logout();

        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        sps.activateService(voSuspendedCustProd);
        sps.activateService(voSuspendedProduct);
        container.logout();

        container.login(platformOperatorAdminKey, ROLE_MARKETPLACE_OWNER);
        sps.suspendService(voSuspendedCustProd, "some Reason");
        container.logout();
    }

    private static void setupExpected() throws Exception {
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        // must be 15 right now - 5 non-specific and 10 customer specific
        final String localMP = supplier.getOrganizationId();
        localLoggedInVisible = sps.getServicesForMarketplace(localMP);
        Assert.assertEquals(15, localLoggedInVisible.size());
        // must be 5 right now - 5 times a template and the customer specific
        // services are activated
        globalLoggedInVisible = sps.getServicesForMarketplace(FUJITSU);
        Assert.assertEquals(5, globalLoggedInVisible.size());
        container.logout();

        // must be 15 right now - 15 active non-specific ignoring customer
        // specific
        ListCriteria c = new ListCriteria();
        c.setLimit(50000);
        localPublicVisible = search.getServicesByCriteria(localMP, "en", c)
                .getServices();
        Assert.assertEquals(15, localPublicVisible.size());
        // must be 5 right now - 5 of the 10 services are not public
        globalPublicVisible = search.getServicesByCriteria(FUJITSU, "en", c)
                .getServices();

        Assert.assertEquals(5, globalPublicVisible.size());
    }

    private static void setupServices(VOTechnicalService tp) throws Exception {
        final String localMP = supplier.getOrganizationId();
        VOOrganization customer = getOrganizationForOrgId(customerOrgId);
        String tpId = tp.getTechnicalServiceId();
        VOServiceDetails voProduct1 = createProduct(tp,
                UUID.randomUUID().toString().substring(0, 4) + "_product1_"
                        + tpId,
                new BigDecimal(5), "en", "de");
        VOServiceDetails voProduct2 = createProduct(tp,
                UUID.randomUUID().toString().substring(0, 4) + "_product2_"
                        + tpId,
                new BigDecimal(3.7), "en", "ja");
        VOServiceDetails voProduct3 = createProduct(tp,
                UUID.randomUUID().toString().substring(0, 4) + "_product3_"
                        + tpId,
                new BigDecimal(1), "ja", "de");
        VOServiceDetails voProduct4 = createProduct(tp,
                UUID.randomUUID().toString().substring(0, 4) + "_product4_"
                        + tpId,
                new BigDecimal(4.1), "de");
        VOServiceDetails voProduct5 = createProduct(tp,
                UUID.randomUUID().toString().substring(0, 4) + "_product5_"
                        + tpId,
                new BigDecimal(4.2), "en");
        VOServiceDetails voProduct6 = createProduct(tp,
                UUID.randomUUID().toString().substring(0, 4) + "_product6_"
                        + tpId,
                null, "en", "de", "ja");
        VOServiceDetails voProduct7 = createProduct(tp,
                UUID.randomUUID().toString().substring(0, 4) + "_product7_"
                        + tpId,
                new BigDecimal(2), "de", "en");
        VOServiceDetails voProduct8 = createProduct(tp,
                UUID.randomUUID().toString().substring(0, 4) + "_product8_"
                        + tpId,
                new BigDecimal(1.5), "de");

        voProduct1 = customizeAttributesAndPriceModel(voProduct1);
        voProduct2 = customizeAttributesAndPriceModel(voProduct2);
        voProduct3 = customizeAttributesAndPriceModel(voProduct3);
        voProduct4 = customizeAttributesAndPriceModel(voProduct4);
        voProduct5 = customizeAttributesAndPriceModel(voProduct5);
        voProduct6 = customizeAttributesAndPriceModel(voProduct6);
        voProduct7 = customizeAttributesAndPriceModel(voProduct7);
        voProduct8 = customizeAttributesAndPriceModel(voProduct8);

        VOService temp = customizeAttributesAndPriceModelForCustomer(voProduct3,
                customer);
        allServices.add(temp);

        VOServiceDetails voProductCust4 = customizeAttributesAndPriceModelForCustomer(
                voProduct4, customer);
        allServices.add(voProductCust4);

        temp = customizeAttributesAndPriceModelForCustomer(voProduct5,
                customer);
        allServices.add(temp);

        VOServiceDetails voProductCust6 = customizeAttributesAndPriceModelForCustomer(
                voProduct6, customer);
        allServices.add(voProductCust6);

        temp = customizeAttributesAndPriceModelForCustomer(voProduct7,
                customer);
        allServices.add(temp);

        VOServiceDetails voProductCust8 = customizeAttributesAndPriceModelForCustomer(
                voProduct8, customer);
        allServices.add(voProductCust8);

        publish(voProduct1, localMP, true);
        publish(voProduct2, localMP, true);
        publish(voProduct3, localMP, true);
        publish(voProduct4, localMP, true);
        publish(voProduct5, localMP, true);
        publish(voProduct6, localMP, true);
        publish(voProduct7, FUJITSU, true);
        publish(voProduct8, FUJITSU, false);

        sps.activateService(voProduct2);
        sps.activateService(voProduct5);
        sps.activateService(voProduct6);
        sps.activateService(voProduct7);
        sps.activateService(voProduct8);
        sps.activateService(voProductCust4);
        sps.activateService(voProductCust6);
        sps.activateService(voProductCust8);
    }

    private static void assignCategoryToProduct(VOService service,
            VOCategory category)
            throws ObjectNotFoundException, NonUniqueBusinessKeyException {
        CategoryToCatalogEntry cc = new CategoryToCatalogEntry();
        final long catalogKey = ((Number) ds.createNativeQuery(
                "select tkey from catalogentry where product_tkey="
                        + service.getKey())
                .getSingleResult()).longValue();
        cc.setCatalogEntry(ds.getReference(CatalogEntry.class, catalogKey));
        cc.setCategory(ds.getReference(Category.class, category.getKey()));
        ds.persist(cc);
    }

    private static VOServiceDetails customizeAttributesAndPriceModel(
            VOServiceDetails voProd) throws Exception {
        VOServiceDetails result = sps.savePriceModel(voProd,
                getPriceModelWithDescription());
        customizeAttributes(result, null);
        svcCounter++;
        return result;
    }

    private static VOServiceDetails customizeAttributesAndPriceModelForCustomer(
            VOServiceDetails voProd, VOOrganization cust) throws Exception {
        VOServiceDetails result = sps.savePriceModelForCustomer(voProd,
                getPriceModelWithDescription(), cust);
        customizeAttributes(result, voProd);
        svcCounter++;
        return result;
    }

    private static void customizeAttributes(VOServiceDetails voProd,
            VOServiceDetails voProdTemplate) throws Exception {
        int phraseIndex;

        VOServiceLocalization loc = sps.getServiceLocalization(voProd);

        // in fact, we do only customize attributes for customer-specific
        // products; templates are left untouched (but do get printed out)

        if (voProdTemplate == null) {
            // first get locales (for which a name is defined)
            List<VOLocalizedText> locNames = loc.getNames();
            List<String> locales = new ArrayList<>();

            // enhance svc name by 0 or 1 tag, and collect available locales
            phraseIndex = svcCounter % 3 + 1;
            for (VOLocalizedText name : locNames) {
                locales.add(name.getLocale());
                name.setText(name.getText().split(BLANK)[0] + BLANK + "("
                        + svcCounter + ")" + BLANK + PHRASE[phraseIndex]);
            }

            Set<String> usedLocales = new HashSet<>();

            // set svc short description for all locales
            phraseIndex = svcCounter % 11;
            List<VOLocalizedText> locShortDescs = loc.getShortDescriptions();

            // in a 1st step, update existing short descriptions
            for (VOLocalizedText shDesc : locShortDescs) {
                shDesc.setText(PHRASE[phraseIndex]);
                usedLocales.add(shDesc.getLocale());
            }
            // in a 2nd step, add missing short desc for remaining locales
            for (String locale : locales) {
                if (!usedLocales.contains(locale)) {
                    locShortDescs.add(
                            new VOLocalizedText(locale, PHRASE[phraseIndex]));
                }
            }

            // set svc description for all locales
            usedLocales = new HashSet<>();

            phraseIndex = (svcCounter + 1) % 11;
            List<VOLocalizedText> locDescs = loc.getDescriptions();
            // in a 1st step, update existing descriptions
            for (VOLocalizedText desc : locDescs) {
                desc.setText(PHRASE[phraseIndex]);
                usedLocales.add(desc.getLocale());
            }
            // in a 2nd step, add missing descriptions for remaining locales
            for (String locale : locales) {
                if (!usedLocales.contains(locale)) {
                    locDescs.add(
                            new VOLocalizedText(locale, PHRASE[phraseIndex]));
                }
            }

            // finally persist localized resources
            sps.saveServiceLocalization(voProd, loc);
        }

        if (svcCounter % 14 == 6 || svcCounter % 14 == 13) {
            System.out.println("----------------");
            System.out.println(((svcCounter % 14 == 6) ? "anonymous (de,en)"
                    : "logged-in (de)")
                    + " | [SVC_NAME] "
                    + (loc.getNames().size() > 0
                            ? loc.getNames().get(0).getText() : "")
                    + " | [SVC_SHORT_DESC] "
                    + (loc.getShortDescriptions().size() > 0
                            ? loc.getShortDescriptions().get(0).getText() : "")
                    + " | [SVC_DESC] "
                    + (loc.getDescriptions().size() > 0
                            ? loc.getDescriptions().get(0).getText() : "")
                    + " | [PM_DESC(en)] "
                    + voProd.getPriceModel().getDescription() + voProd.getKey());
        }

    }

    private static VOPriceModel getPriceModelWithDescription() {
        VOPriceModel pm = new VOPriceModel();
        pm.setType(PriceModelType.PRO_RATA);
        pm.setDescription(PM_DESCRIPTION[svcCounter % 6]);
        pm.setCurrencyISOCode("EUR");
        pm.setPeriod(PricingPeriod.MONTH);
        return pm;
    }

    private static void publish(VOServiceDetails voProduct,
            String marketplaceId, boolean anonymous)
            throws ObjectNotFoundException, ValidationException,
            NonUniqueBusinessKeyException, OperationNotPermittedException {
        VOMarketplace voMp = new VOMarketplace();
        voMp.setMarketplaceId(marketplaceId);

        VOCatalogEntry ce = new VOCatalogEntry();
        ce.setService(voProduct);
        ce.setMarketplace(voMp);
        ce.setAnonymousVisible(anonymous);
        ce.setVisibleInCatalog(true);
        mps.publishService(voProduct, Arrays.asList(ce));
    }

    private static VOTechnicalService addTags(List<VOTechnicalService> tps,
            int index, List<String> tags) throws Exception {
        // save the tags in the users locale
        VOTechnicalService tp = tps.get(index);
        tp.setTags(tags);
        sps.saveTechnicalServiceLocalization(tp);
        tp = sps.getTechnicalServices(OrganizationRoleType.TECHNOLOGY_PROVIDER)
                .get(index);
        return tp;
    }

    /**
     * @param locale
     *            the locale to be used by the current user
     */
    private static void setLocaleCurrentUser(final String locale,
            final long userKey) throws Exception {
        // update the user profile to set the required locale
        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                PlatformUser user = ds.getReference(PlatformUser.class,
                        userKey);
                user.setLocale(locale);
                return null;
            }

        });
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

    private static VOServiceDetails createProduct(VOTechnicalService tp,
            String id, final BigDecimal rating, String... locales)
            throws Exception {
        VOServiceDetails service = new VOServiceDetails();
        service.setServiceId(id);

        try {
            createMarketingPermission(tp.getKey(), orgRef.getKey());
        } catch (NonUniqueBusinessKeyException e) {
            // ignore
        }

        service = sps.createService(tp, service, null);
        VOServiceLocalization loc = new VOServiceLocalization();
        ArrayList<VOLocalizedText> names = new ArrayList<>();
        for (String locale : locales) {
            names.add(new VOLocalizedText(locale, id + "_" + locale));
        }
        loc.setNames(names);
        sps.saveServiceLocalization(service, loc);
        if (rating != null) {
            final long key = service.getKey();
            runTX(new Callable<Void>() {

                @Override
                public Void call() throws Exception {
                    Product prod = ds.getReference(Product.class, key);
                    ProductFeedback pf = new ProductFeedback();
                    pf.setAverageRating(rating);
                    pf.setProduct(prod);
                    ds.persist(pf);
                    return null;
                }
            });
        }
        allServices.add(service);
        return service;
    }

    private static List<VOTechnicalService> createTechnicalProducts()
            throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Organization prov = ds.getReference(Organization.class,
                        provider.getKey());
                TechnicalProducts.createTechnicalProduct(ds, prov, "tp1_1",
                        false, ServiceAccessType.LOGIN);
                TechnicalProducts.createTechnicalProduct(ds, prov, "tp1_2",
                        false, ServiceAccessType.LOGIN);
                TechnicalProducts.createTechnicalProduct(ds, prov, "tp1_3",
                        false, ServiceAccessType.LOGIN);
                TechnicalProducts.createTechnicalProduct(ds, prov, "tp2_1",
                        false, ServiceAccessType.LOGIN);
                TechnicalProducts.createTechnicalProduct(ds, prov, "tp2_2",
                        false, ServiceAccessType.LOGIN);
                return null;
            }
        });
        return sps
                .getTechnicalServices(OrganizationRoleType.TECHNOLOGY_PROVIDER);
    }

    private static VOOrganization getOrganizationForOrgId(
            final String organizationId) throws Exception {
        return runTX(new Callable<VOOrganization>() {
            @Override
            public VOOrganization call() {
                Organization result = new Organization();
                result.setOrganizationId(organizationId);
                result = (Organization) ds.find(result);
                return OrganizationAssembler.toVOOrganization(result, false,
                        new LocalizerFacade(localizer, "en"));
            }
        });
    }

    /**
     * Gets a ListCriteria instance for category search.
     * 
     * @param offset
     *            Offset of the first result
     * @param limit
     *            Maximum number of results
     * @param filter
     *            Filter criteria
     * @param sorting
     *            Sorting criteria for the results
     * @return new ListCriteria instance
     */
    private static ListCriteria getCriteria(int offset, int limit,
            String filter, Sorting sorting) {
        ListCriteria crit = new ListCriteria();
        crit.setOffset(offset);
        crit.setLimit(limit);
        crit.setFilter(filter);
        crit.setSorting(sorting);
        return crit;
    }

    /**
     * Gets a ListCriteria instance for category search.
     * 
     * @param offset
     *            Offset of the first result
     * @param limit
     *            Maximum number of results
     * @param categoryId
     *            Category id
     * @param sorting
     *            Sorting criteria for the results
     * @return new ListCriteria instance
     */
    private static ListCriteria createCategoryCriteria(int offset, int limit,
            String categoryId, Sorting sorting) {
        ListCriteria crit = new ListCriteria();
        crit.setOffset(offset);
        crit.setLimit(limit);
        crit.setFilter(null);
        crit.setCategoryId(categoryId);
        crit.setSorting(sorting);
        return crit;
    }

    /**
     * Helper method to sort by provisioning date - as it is not added to the
     * value object, sorting must be done on domain object
     * 
     * @param asc
     *            ascending or descending order
     * @param list
     *            the list of services to sort
     * @return the list of keys in the sorted order
     * @throws Exception
     */
    private static List<Long> getServiceKeysByDate(final boolean asc,
            final List<VOService> list) throws Exception {
        return runTX(new Callable<List<Long>>() {

            @Override
            public List<Long> call() {
                Set<Long> keys = new HashSet<>();
                for (VOService voService : list) {
                    keys.add(Long.valueOf(voService.getKey()));
                }
                Query query = ds.createQuery("SELECT p.key FROM Product p "
                        + "WHERE p.key IN (:keys) "
                        + "ORDER BY p.dataContainer.provisioningDate "
                        + (asc ? "ASC" : "DESC"));
                query.setParameter("keys", keys);
                return ParameterizedTypes.list(query.getResultList(),
                        Long.class);
            }
        });
    }

    /**
     * Helper method to sort by the (localized) service name.
     * 
     * @param asc
     *            ascending or descending order
     * @param list
     *            the list of services to sort
     * @return the list of keys in the sorted order
     * @throws Exception
     */
    private static List<Long> getServiceKeysByName(final boolean asc,
            final List<VOService> list) {
        List<Long> resultList = new ArrayList<>();
        Comparator<VOService> comp = asc ? new ServiceNameComparator()
                : Collections.reverseOrder(new ServiceNameComparator());
        Collections.sort(list, comp);
        for (VOService svc : list) {
            resultList.add(new Long(svc.getKey()));
        }
        return resultList;
    }

    private static final class ServiceNameComparator
            implements Comparator<VOService> {

        @Override
        public int compare(VOService o1, VOService o2) {
            return getNameToDisplay(o1).compareTo(getNameToDisplay(o2));
        }

        private String getNameToDisplay(VOService svc) {
            if (svc.getName() == null || svc.getName().trim().length() == 0) {
                return svc.getServiceId();
            }
            return svc.getName();
        }

    }

    /**
     * Compares that the {@link VOService}s are in the same order in the list
     * (by key) as the {@link Long} values.
     * 
     * @param expected
     *            the expected key order
     * @param services
     *            the result list
     */
    private static void compare(List<Long> expected, List<VOService> services) {
        Assert.assertEquals(expected.size(), services.size());
        String error = "Expected key %s at position %s.";
        for (int index = 0; index < expected.size(); index++) {
            Long key = expected.get(index);
            Assert.assertEquals(
                    String.format(error, key, Integer.valueOf(index)), key,
                    Long.valueOf(services.get(index).getKey()));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSearchServices_NullMarketplaceId() throws Throwable {
        try {
            search.searchServices(null, "de", "wicked_de");
        } catch (EJBException e) {
            throw e.getCause();
        }
        Assert.fail();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSearchServices_EmptyMarketplaceId() throws Throwable {
        try {
            search.searchServices("", "de", "wicked_de");
        } catch (EJBException e) {
            throw e.getCause();
        }
        Assert.fail();
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testSearchServices_NonexistentMarketplaceId() throws Exception {
        search.searchServices("NotExistingMarketplaceId", "de", "wicked_de");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSearchServices_NullLocale() throws Throwable {
        try {
            search.searchServices(FUJITSU, null, "wicked_de");
        } catch (EJBException e) {
            throw e.getCause();
        }
        Assert.fail();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSearchServices_EmptyLocale() throws Throwable {
        try {
            search.searchServices(FUJITSU, "", "wicked_de");
        } catch (EJBException e) {
            throw e.getCause();
        }
        Assert.fail();
    }

    @Test
    public void testSearchServices_UnknownLocale() throws Exception {
        VOServiceListResult hits = search.searchServices(FUJITSU, "ede",
                "wicked_de");
        Assert.assertEquals(0, hits.getResultSize());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSearchServices_NullPhrase() throws Throwable {
        try {
            search.searchServices(FUJITSU, "de", null);
        } catch (EJBException e) {
            throw e.getCause();
        }
        Assert.fail();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSearchServices_EmptyPhrase() throws Throwable {
        try {
            search.searchServices(FUJITSU, "de", "");
        } catch (EJBException e) {
            throw e.getCause();
        }
        Assert.fail();
    }

    @Test
    public void testSearchServices_NonexistentPhrase() throws Exception {
        VOServiceListResult hits = search.searchServices(FUJITSU, "de",
                "Abracadabra");
        Assert.assertEquals(0, hits.getResultSize());
    }

    public void testSearchServices_UnallowedCharacter() throws Exception {
        VOServiceListResult hits = search.searchServices(FUJITSU, "de", "\\");
        // will be escaped, thus no exception (but no hits either of coz)
        Assert.assertEquals(0, hits.getResultSize());
    }

    @Test
    public void testSearchServices_Anonymous_SearchTermContainsOnlySpecialCharacters()
            throws Exception {
        // search without being logged in
        VOServiceListResult hits = search.searchServices(FUJITSU, "en",
                "-_!\"#$%&*+,/:;=<>?^`");
        Assert.assertEquals(0, hits.getResultSize());
    }

    @Test
    public void testSearchServices_Anonymous_SearchTermContainsReservedTerms()
            throws Exception {
        // search without being logged in, should throw no exception
        VOServiceListResult hits = search.searchServices(FUJITSU, "en",
                "AND OR NOT");
        Assert.assertEquals(0, hits.getResultSize());
    }

    // ////////////////////////////////////////////////////////////////////////
    // Anonomously visible services on global MP containing search phrases
    // SVC_NAME | TAGS | SVC_SHORT_DESC | SVC_DESCRIPTION | PM DESC (loc:en)
    // ////////////////////////////////////////////////////////////////////////
    // (6) cool | cool wicked | cool one-two | cool fishing | free
    // (20) bla | cool one-two | one-two bla | one-two fishing | flexible
    // (34) wicked | wicked | cool | wicked | free flexible
    // (48) cool | "" | cool bla | cool wicked | free
    // (62) bla | "" | cool fishing | wicked bla | flexible
    // ////////////////////////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////////////////////////
    // Anonomously visible services on global MP containing search phrases
    // SVC_NAME | TAGS | SVC_SHORT_DESC | SVC_DESCRIPTION | PM DESC (loc:de)
    // ////////////////////////////////////////////////////////////////////////
    // (6) cool | "" | cool one-two | cool fishing | ""
    // (20) bla | "" | one-two bla | one-two fishing| ""
    // (34) wicked | "" | cool | wicked | ""
    // (48) cool | cool one-two | cool bla | cool wicked | ""
    // (62) bla | wicked fishing | cool fishing | wicked bla | ""
    // ////////////////////////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////////////////////////
    // Logged-in visible services on global MP containing search phrases
    // SVC_NAME | TAGS | SVC_SHORT_DESC | SVC_DESCRIPTION | PM DESC (loc:en)
    // ////////////////////////////////////////////////////////////////////////
    // (13) "" | cool wicked | "" | "" | basic
    // (27) "" | cool one-two | "" | "" | free basic
    // (41) "" | wicked | "" | "" | basic flexible
    // (55) "" | "" | "" | "" | basic
    // (69) "" | "" | "" | "" | free basic
    // ////////////////////////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////////////////////////
    // Logged-in visible services on global MP containing search phrases
    // SVC_NAME | TAGS | SVC_SHORT_DESC | SVC_DESCRIPTION | PM DESC (loc:de)
    // ////////////////////////////////////////////////////////////////////////
    // (13) wicked | "" | wicked | bla | ""
    // (27) cool | "" | cool wicked | cool one-two | ""
    // (41) bla | "" | wicked bla | one-two bla | ""
    // (55) wicked | cool one-two | "" | cool | ""
    // (69) cool | wicked fishing | bla | cool bla | ""
    // ////////////////////////////////////////////////////////////////////////

    @Test
    public void testSearchServices_Anonymous_PhraseInAllFields()
            throws Exception {
        // search without being logged in
        VOServiceListResult hits = search.searchServices(FUJITSU, "en", TAG1);
        checkResultSet(hits, 6, 20, 34, 48, 62);

        // now perform same search in other locale
        hits = search.searchServices(FUJITSU, "de", TAG1);
        checkResultSet(hits, 6, 34, 48, 62);
        // (doesn't contain svc at index 20 since tag is not defined for de)
    }

    @Test
    public void testSearchServices_filterKeySetByInvisbleProductKeys()
            throws Exception {
        doReturn(Arrays.asList(Long.valueOf(allServices.get(27).getKey())))
                .when(userGroupServiceLocalBean)
                .getInvisibleProductKeysForUser(endUerKey);

        // search without being logged in
        VOServiceListResult hits = search.searchServices(FUJITSU, "en", TAG1);
        checkResultSet(hits, 6, 20, 34, 48, 62);

        // search while being logged in with organization admin role.
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        VOServiceListResult hitsLoggedIn = search.searchServices(FUJITSU, "en",
                TAG1);
        checkResultSet(hitsLoggedIn, 13, 27);
        container.logout();

        // search while being logged in without organization admin role.
        container.login(endUerKey, "");
        VOServiceListResult hitsWithoutAdmin = search.searchServices(FUJITSU,
                "en", TAG1);
        checkResultSet(hitsWithoutAdmin, 13);
    }

    @Test
    public void testSearchServices_LoggedIn_PhraseInAllFields()
            throws Exception {
        // search while being logged in
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        VOServiceListResult hits = search.searchServices(FUJITSU, "en", TAG1);
        checkResultSet(hits, 13, 27);

        // now perform same search in other locale
        hits = search.searchServices(FUJITSU, "de", TAG1);
        checkResultSet(hits, 13, 27, 55, 69);
    }

    @Test
    public void testSearchServices_Anonymous_PhraseInAllFields_TagNotDefinedInLocale()
            throws Exception {
        // search without being logged in
        VOServiceListResult hits = search.searchServices(FUJITSU, "de", TAG1);
        // result should be same as above except for svc(20) since the tag for
        // corresponding technical svc is only defined for English locale
        checkResultSet(hits, 6, 34, 48, 62);
    }

    public void testSearchServices_Anonymous_WildcardAll() throws Exception {
        // search without being logged in
        VOServiceListResult hits = search.searchServices(FUJITSU, "en", "*");
        // will be escaped, thus no exception (but no hits either of coz)
        Assert.assertEquals(0, hits.getResultSize());
    }

    @Test
    public void testSearchServices_Anonymous_WildcardAfterThirdLetter()
            throws Exception {
        // search without being logged in
        VOServiceListResult hits = search.searchServices(FUJITSU, "de",
                TAG4.substring(0, 3));
        Assert.assertEquals(3, hits.getResultSize());
    }

    //glassfish upgrade
    @Test
    @Ignore
    public void testSearchServices_Anonymous_CompoundWords() throws Exception {
        // search without being logged in
        // make sure a service containing compound word separated by delimiter
        // (e.g. 'one-two') is found by search for whole word (even without
        // delimiter) + also its parts
        VOServiceListResult hits = search.searchServices(FUJITSU, "de",
                "one-two");
        checkResultSet(hits, 6, 20, 48);

        hits = search.searchServices(FUJITSU, "de", "onetwo");
        checkResultSet(hits, 6, 20, 48);

        hits = search.searchServices(FUJITSU, "de", "one");
        checkResultSet(hits, 6, 20, 48);

        hits = search.searchServices(FUJITSU, "de", "two");
        checkResultSet(hits, 6, 20, 48);
    }

    @Test
    @Ignore
    public void testSearchServices_Anonymous_PhraseInTagOnly()
            throws Exception {
        // search without being logged in
        VOServiceListResult hits = search.searchServices(FUJITSU, "de", TAG3);
        checkResultSet(hits, 6, 20, 48);
    }

    @Test
    @Ignore
    public void testSearchServices_Anonymous_PhraseInSvcShortDescOnly()
            throws Exception {
        // search without being logged in
        VOServiceListResult hits = search.searchServices(FUJITSU, "en", TAG4);
        checkResultSet(hits, 6, 20, 62);
    }

    @Test
    @Ignore
    public void testSearchServices_Anonymous_PhraseInSvcDescOnly()
            throws Exception {
        // search without being logged in
        VOServiceListResult hits = search.searchServices(FUJITSU, "en", TAG4);
        checkResultSet(hits, 6, 20, 62);
    }

    @Test
    @Ignore
    public void testSearchServices_Anonymous_PhraseInPriceModelDescOnly()
            throws Exception {
        // search without being logged in
        VOServiceListResult hits = search.searchServices(FUJITSU, "en",
                PM_DESC3);
        checkResultSet(hits, 20, 34, 62);
    }

    @Test
    public void testSearchServices_LoggedIn_PhraseInSvcNameOnly()
            throws Exception {
        // search while being logged in
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        // setLocaleCurrentUser("de", customerUserKey);
        VOServiceListResult hits = search.searchServices(FUJITSU, "de", TAG2);
        checkResultSet(hits, 13, 41, 55, 69);
    }

    @Test
    public void testSearchServices_Wildcard_SvcName() throws InvalidPhraseException, ObjectNotFoundException {
        final VOServiceListResult hits = search.searchServices(FUJITSU, "de", "wic");
        final VOServiceListResult hits2 = search.searchServices(FUJITSU, "de", "wicked");
        assertTrue(hits.getResultSize() == hits2.getResultSize());
    }

    @Test
    public void testSearchServices_Wildcard_PriceModelDesc() throws InvalidPhraseException, ObjectNotFoundException {
        final VOServiceListResult hits = search.searchServices(FUJITSU, "de", "exi");
        final VOServiceListResult hits2 = search.searchServices(FUJITSU, "de", "flexible");
        assertTrue(hits.getResultSize() == hits2.getResultSize());
    }

    @Test
    public void testSearchServices_Wildcard_Description() throws InvalidPhraseException, ObjectNotFoundException {
        final VOServiceListResult hits = search.searchServices(FUJITSU, "de", "hing");
        final VOServiceListResult hits2 = search.searchServices(FUJITSU, "de", "fishing");
        assertTrue(hits.getResultSize() == hits2.getResultSize());
    }

    @Test
    public void testSearchServices_Wildcard_ShortDescription() throws InvalidPhraseException, ObjectNotFoundException {
        final VOServiceListResult hits = search.searchServices(FUJITSU, "de", "fis");
        final VOServiceListResult hits2 = search.searchServices(FUJITSU, "de", "fishing");
        assertTrue(hits.getResultSize() == hits2.getResultSize());
    }

    @Test
    public void testSearchServices_Wildcard_MultiplePhrases() throws InvalidPhraseException, ObjectNotFoundException {
        final VOServiceListResult hits = search.searchServices(FUJITSU, "de", "wic fis");
        final VOServiceListResult hits2 = search.searchServices(FUJITSU, "de", "wicked fishing");
        assertTrue(hits.getResultSize() == hits2.getResultSize());

        final VOServiceListResult hits3 = search.searchServices(FUJITSU, "de", "wic fis fis wic fis fis");
        final VOServiceListResult hits4 = search.searchServices(FUJITSU, "de", "wicked fishing fishing wicked fishing fishing");
        assertTrue(hits3.getResultSize() == hits4.getResultSize());
    }

    @Test
    @Ignore
    public void testSearchServices_Anonymous_PhraseOfMultipleWordsInOneAttribute()
            throws Exception {
        // search without being logged in
        VOServiceListResult hits = search.searchServices(FUJITSU, "en",
                TAG4 + BLANK + TAG1);
        checkResultSet(hits, 6, 20, 62);
    }

    @Test
    @Ignore
    public void testSearchServices_Anonymous_PhraseOfMultipleWordsInOneAttribute_ThreeByteSpace_en()
            throws Exception {
        // search without being logged in
        VOServiceListResult hits = search.searchServices(FUJITSU, "en",
                TAG4 + THREE_BYTE_SPACE + TAG1);
        checkResultSet(hits, 6, 20, 62);
    }

    @Test
    @Ignore
    public void testSearchServices_Anonymous_PhraseOfMultipleWordsOverMultipleAttributes()
            throws Exception {
        // search without being logged in
        VOServiceListResult hits = search.searchServices(FUJITSU, "en",
                PM_DESC3 + BLANK + TAG1 + BLANK + TAG4);
        checkResultSet(hits, 20, 62);
    }

    @Test
    @Ignore
    public void testSearchServices_Anonymous_PhraseOfMultipleWordsOverMultipleAttributes_MultipleConnectedWhiteSpaces()
            throws Exception {
        // search without being logged in
        VOServiceListResult hits = search.searchServices(FUJITSU, "en",
                PM_DESC3 + BLANK + BLANK + TAG1 + BLANK + BLANK + BLANK + TAG4);
        checkResultSet(hits, 20, 62);
    }

    @Test
    @Ignore
    public void testSearchServices_Anonymous_DefaultLocaleMechanism()
            throws Exception {
        VOServiceListResult hits = search.searchServices(FUJITSU, "de",
                PM_DESC3);
        // pm_desc for de locale is not defined, however for default locale en
        // p_desc are defined (Note however that mix of default locale and
        // current locale is not supported yet)
        checkResultSet(hits, 20, 34, 62);

        // now search in locale where nothing has been defined for
        hits = search.searchServices(FUJITSU, "ja",
                TAG1 + BLANK + TAG3 + BLANK + TAG4);
        // svc6 has words in short_desc + desc for default locale (en)
        checkResultSet(hits, 6);
    }

    @Test
    @Ignore
    public void testSearchServices_Anonymous_Stemming() throws Exception {
        // search without being logged in
        // originally, "fishing" was passed to the indexer
        // now check that "fishing" as well as its stem "fish" as well as the
        // plural "fishes" are found
        VOServiceListResult hits = search.searchServices(FUJITSU, "en",
                "fishing");
        checkResultSet(hits, 6, 20, 62);
        hits = search.searchServices(FUJITSU, "en", "fishes");
        checkResultSet(hits, 6, 20, 62);
        hits = search.searchServices(FUJITSU, "en", "fish");
        checkResultSet(hits, 6, 20, 62);
        // remark: the stemmer is pretty basic, since e.g. "fisher" is not found
    }

    @Test
    public void testSearchServices_Anonymous_HTMLFiltering() throws Exception {
        // search without being logged in
        // check that HTML tags in search term are not filtered
        VOServiceListResult hits = search.searchServices(FUJITSU, "en",
                "<b>" + TAG1 + "<b/>");
        Assert.assertEquals(0, hits.getResultSize());
    }

    @Test
    @Ignore
    public void testSearchServices_Anonymous_PhraseInSvcDescOnly_Deactivation()
            throws Exception {
        try {
            // deactivate svc which previously appeared in the search result
            container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
            VOService svcToBeModified = allServices.get(20);
            sps.deactivateService(svcToBeModified);
            container.logout();

            // search without being logged in
            VOServiceListResult hits = search.searchServices(FUJITSU, "en",
                    TAG4);
            checkResultSet(hits, 6, 62);
        } finally {
            container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
            VOService svcToBeModified = allServices.get(20);
            sps.activateService(svcToBeModified);
            container.logout();
        }
    }

    @Test
    @Ignore
    public void testSearchServices_Anonymous_CheckUpdateResultAfterModification()
            throws Exception {
        List<String> oldDescs = Collections.emptyList();
        // search without being logged in
        VOServiceListResult hits = search.searchServices(FUJITSU, "en", TAG4);
        checkResultSet(hits, 6, 20, 62);

        // modify svc [6] description such that it no longer contains TAG4
        try {
            oldDescs = modifySvcDescription(6, BLA, false);

            // search without being logged in
            hits = search.searchServices(FUJITSU, "en", TAG4);
            // result set must now no longer contain service [6]
            checkResultSet(hits, 20, 62);
        } finally {
            modifySvcDescription(6, oldDescs.get(0), false);
        }
    }

    @Test
    @Ignore
    public void testSearchServices_Anonymous_FurtherWordSeparators()
            throws Exception {
        List<String> oldDescs1 = Collections.emptyList();
        List<String> oldDescs2 = Collections.emptyList();
        List<String> oldDescs3 = Collections.emptyList();

        // search without being logged in
        VOServiceListResult hits = search.searchServices(FUJITSU, "en", TAG4);
        checkResultSet(hits, 6, 20, 62);

        // modify services [6,20,34] description such that it TAG4 is followed
        // by other separators than full-stop (svc 6 + 20 have TAG4 in svc
        // description only, 34 doesn't have TAG4 in any searchable attribute)
        try {
            oldDescs1 = modifySvcDescription(6, ".", true);
            oldDescs2 = modifySvcDescription(20, ",", true);
            oldDescs3 = modifySvcDescription(34, TAG4 + "-suffix", false);

            // search without being logged in
            hits = search.searchServices(FUJITSU, "en", TAG4);
            // result set must still contain services [6,20,62] + [34]
            checkResultSet(hits, 6, 20, 34, 62);
        } finally {
            modifySvcDescription(6, oldDescs1.get(0), false);
            modifySvcDescription(20, oldDescs2.get(0), false);
            modifySvcDescription(34, oldDescs3.get(0), false);
        }
    }

    private List<String> modifySvcDescription(int svcKey, String value,
            boolean append) throws Exception {
        container.login(supplierUserKey, ROLE_SERVICE_MANAGER);
        VOService svcToBeModified = sps
                .getServiceDetails(allServices.get(svcKey));
        svcToBeModified = sps.deactivateService(svcToBeModified);
        VOServiceLocalization loc = sps.getServiceLocalization(svcToBeModified);
        List<VOLocalizedText> locDescs = loc.getDescriptions();
        List<String> oldDescs = new ArrayList<>();
        for (VOLocalizedText desc : locDescs) {
            oldDescs.add(desc.getText());
            if (append) {
                desc.setText(desc.getText() + value);
            } else {
                desc.setText(value);
            }
        }
        sps.saveServiceLocalization(svcToBeModified, loc);
        svcToBeModified = sps.activateService(svcToBeModified);
        container.logout();
        return oldDescs;
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetServicesByCriteria_NullMarketplaceId() throws Throwable {
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        setLocaleCurrentUser("en", customerUserKey);
        try {
            search.getServicesByCriteria(null, "en", new ListCriteria());
        } catch (EJBException e) {
            throw e.getCause();
        }
        Assert.fail();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetServicesByCriteria_EmptyMarketplaceId()
            throws Throwable {
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        setLocaleCurrentUser("en", customerUserKey);
        try {
            search.getServicesByCriteria("", "en", new ListCriteria());
        } catch (EJBException e) {
            throw e.getCause();
        }
        Assert.fail();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetServicesByCriteria_NullLocale() throws Throwable {
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        setLocaleCurrentUser("en", customerUserKey);
        try {
            search.getServicesByCriteria(supplier.getOrganizationId(), null,
                    new ListCriteria());
        } catch (EJBException e) {
            throw e.getCause();
        }
        Assert.fail();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetServicesByCriteria_EmptyLocale() throws Throwable {
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        setLocaleCurrentUser("en", customerUserKey);
        try {
            search.getServicesByCriteria(supplier.getOrganizationId(), "",
                    new ListCriteria());
        } catch (EJBException e) {
            throw e.getCause();
        }
        Assert.fail();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetServicesByCriteria_NullCriterium() throws Throwable {
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        setLocaleCurrentUser("en", customerUserKey);
        try {
            search.getServicesByCriteria(supplier.getOrganizationId(), "en",
                    null);
        } catch (EJBException e) {
            throw e.getCause();
        }
        Assert.fail();
    }

    @Test
    public void testGetServicesByCriteria_Paging_TwoFromSecond()
            throws Exception {
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        ListCriteria crit = getCriteria(1, 2, null, null);
        VOServiceListResult servicesByCriteria = search.getServicesByCriteria(
                supplier.getOrganizationId(), "en", crit);
        List<VOService> products = servicesByCriteria.getServices();
        Assert.assertEquals(2, products.size());
        Assert.assertEquals(localLoggedInVisible.size(),
                servicesByCriteria.getResultSize());
    }

    @Test
    public void testGetServicesByCriteria_Paging_NegativeOffset()
            throws Exception {
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        // negative offset will be handled as beginning from first
        ListCriteria crit = getCriteria(-1, 2, null, null);
        VOServiceListResult servicesByCriteria = search.getServicesByCriteria(
                supplier.getOrganizationId(), "en", crit);
        List<VOService> products = servicesByCriteria.getServices();
        Assert.assertEquals(2, products.size());
        Assert.assertEquals(localLoggedInVisible.size(),
                servicesByCriteria.getResultSize());
    }

    @Test
    public void testGetServicesByCriteria_Paging_NegativeLimit()
            throws Exception {
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        // negative limit will be handled as all beginning from offset
        ListCriteria crit = getCriteria(0, -2, null, null);
        VOServiceListResult servicesByCriteria = search.getServicesByCriteria(
                supplier.getOrganizationId(), "en", crit);
        List<VOService> products = servicesByCriteria.getServices();
        int size = localLoggedInVisible.size();
        Assert.assertEquals(size, products.size());
        Assert.assertEquals(size, servicesByCriteria.getResultSize());
    }

    @Test
    public void testGetServicesByCriteria_Paging_ZeroLimit() throws Exception {
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        // zero limit will be handled as all beginning from offset
        ListCriteria crit = getCriteria(0, 0, null, null);
        VOServiceListResult servicesByCriteria = search.getServicesByCriteria(
                supplier.getOrganizationId(), "en", crit);
        List<VOService> products = servicesByCriteria.getServices();
        Assert.assertEquals(0, products.size());
        Assert.assertEquals(localLoggedInVisible.size(),
                servicesByCriteria.getResultSize());
    }

    @Test
    public void testGetServicesByCriteria_Paging_OffsetOutOfRange()
            throws Exception {
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        int size = localLoggedInVisible.size();
        int offset = size + 1;
        // negative limit will be handled as all beginning from offset
        ListCriteria crit = getCriteria(offset, -2, null, null);
        VOServiceListResult servicesByCriteria = search.getServicesByCriteria(
                supplier.getOrganizationId(), "en", crit);
        List<VOService> products = servicesByCriteria.getServices();
        Assert.assertEquals(0, products.size());
        Assert.assertEquals(size, servicesByCriteria.getResultSize());
    }

    @Test
    public void testGetServicesByCriteria_Paging_HighLimit() throws Exception {
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        int size = localLoggedInVisible.size();
        int limit = size + 100;
        // negative limit will be handled as all beginning from offset
        ListCriteria crit = getCriteria(0, limit, null, null);
        VOServiceListResult servicesByCriteria = search.getServicesByCriteria(
                supplier.getOrganizationId(), "en", crit);
        List<VOService> products = servicesByCriteria.getServices();
        Assert.assertEquals(size, products.size());
        Assert.assertEquals(size, servicesByCriteria.getResultSize());
    }

    @Test
    public void testGetServicesByCriteria_Paging_ScrollThroughResult()
            throws Exception {
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        Set<Long> readServiceKeys = new HashSet<>();
        final int size = localLoggedInVisible.size();
        for (int offset = 0; offset < size; offset++) {
            ListCriteria crit = getCriteria(offset, 1, null, null);
            VOServiceListResult servicesByCriteria = search
                    .getServicesByCriteria(supplier.getOrganizationId(), "en",
                            crit);
            List<VOService> products = servicesByCriteria.getServices();
            Assert.assertEquals(1, products.size());
            Long serviceKey = Long.valueOf(products.get(0).getKey());
            Assert.assertFalse(readServiceKeys.contains(serviceKey));
            readServiceKeys.add(serviceKey);
            Assert.assertEquals(size, servicesByCriteria.getResultSize());
        }
    }

    @Test
    public void testGetServicesByCriteria_Sorting_DateAsc() throws Exception {
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        List<Long> expected = getServiceKeysByDate(true, localLoggedInVisible);
        ListCriteria crit = getCriteria(-1, -1, null,
                Sorting.ACTIVATION_ASCENDING);
        VOServiceListResult servicesByCriteria = search.getServicesByCriteria(
                supplier.getOrganizationId(), "en", crit);
        compare(expected, servicesByCriteria.getServices());
    }

    @Test
    public void testGetServicesByCriteria_Sorting_DateDesc() throws Exception {
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        List<Long> expected = getServiceKeysByDate(false, localLoggedInVisible);
        ListCriteria crit = getCriteria(-1, -1, null,
                Sorting.ACTIVATION_DESCENDING);
        VOServiceListResult servicesByCriteria = search.getServicesByCriteria(
                supplier.getOrganizationId(), "en", crit);
        compare(expected, servicesByCriteria.getServices());
    }

    @Test
    public void testGetServicesByCriteria_Sorting_DateAscPaged()
            throws Exception {
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        List<Long> expected = getServiceKeysByDate(true, localLoggedInVisible);
        int size = localLoggedInVisible.size();
        for (int offset = 0; offset < size; offset++) {
            ListCriteria crit = getCriteria(offset, 1, null,
                    Sorting.ACTIVATION_ASCENDING);
            VOServiceListResult servicesByCriteria = search
                    .getServicesByCriteria(supplier.getOrganizationId(), "en",
                            crit);
            List<VOService> products = servicesByCriteria.getServices();
            Long serviceKey = Long.valueOf(products.get(0).getKey());
            Assert.assertEquals(expected.get(offset), serviceKey);
        }
    }

    @Test
    public void testGetServicesByCriteria_Sorting_DateDescPaged()
            throws Exception {
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        List<Long> expected = getServiceKeysByDate(false, localLoggedInVisible);
        int size = localLoggedInVisible.size();
        for (int offset = 0; offset < size; offset++) {
            ListCriteria crit = getCriteria(offset, 1, null,
                    Sorting.ACTIVATION_DESCENDING);
            VOServiceListResult servicesByCriteria = search
                    .getServicesByCriteria(supplier.getOrganizationId(), "en",
                            crit);
            List<VOService> products = servicesByCriteria.getServices();
            Long serviceKey = Long.valueOf(products.get(0).getKey());
            Assert.assertEquals(expected.get(offset), serviceKey);
        }
    }

    @Test
    public void testGetServicesByCriteria_Sorting_NameAsc() throws Exception {
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        List<Long> expected = getServiceKeysByName(true, localLoggedInVisible);
        ListCriteria crit = getCriteria(-1, -1, null, Sorting.NAME_ASCENDING);
        VOServiceListResult servicesByCriteria = search.getServicesByCriteria(
                supplier.getOrganizationId(), "en", crit);
        compare(expected, servicesByCriteria.getServices());
    }

    @Test
    public void testGetServicesByCriteria_Sorting_NameDesc() throws Exception {
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        List<Long> expected = getServiceKeysByName(false, localLoggedInVisible);
        ListCriteria crit = getCriteria(-1, -1, null, Sorting.NAME_DESCENDING);
        VOServiceListResult servicesByCriteria = search.getServicesByCriteria(
                supplier.getOrganizationId(), "en", crit);
        compare(expected, servicesByCriteria.getServices());
    }

    @Test
    public void testGetServicesByCriteria_Sorting_NameAscPaged()
            throws Exception {
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        List<Long> expected = getServiceKeysByName(true, localLoggedInVisible);
        int size = localLoggedInVisible.size();
        for (int offset = 0; offset < size; offset++) {
            ListCriteria crit = getCriteria(offset, 1, null,
                    Sorting.NAME_ASCENDING);
            VOServiceListResult servicesByCriteria = search
                    .getServicesByCriteria(supplier.getOrganizationId(), "en",
                            crit);
            List<VOService> products = servicesByCriteria.getServices();
            Long serviceKey = Long.valueOf(products.get(0).getKey());
            Assert.assertEquals(expected.get(offset), serviceKey);

        }
    }

    @Test
    public void testGetServicesByCriteria_Sorting_NameDescPaged()
            throws Exception {
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        List<Long> expected = getServiceKeysByName(false, localLoggedInVisible);
        int size = localLoggedInVisible.size();
        for (int offset = 0; offset < size; offset++) {
            ListCriteria crit = getCriteria(offset, 1, null,
                    Sorting.NAME_DESCENDING);
            VOServiceListResult servicesByCriteria = search
                    .getServicesByCriteria(supplier.getOrganizationId(), "en",
                            crit);
            List<VOService> products = servicesByCriteria.getServices();
            Long serviceKey = Long.valueOf(products.get(0).getKey());
            Assert.assertEquals(expected.get(offset), serviceKey);
        }
    }

    @Test
    public void testGetServicesByCriteria_Tagging_Null() throws Exception {
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        setLocaleCurrentUser("en", customerUserKey);
        ListCriteria crit = getCriteria(-1, -1, null, null);
        VOServiceListResult servicesByCriteria = search.getServicesByCriteria(
                supplier.getOrganizationId(), "en", crit);
        checkResultSet(servicesByCriteria, 1, 9, 11, 15, 23, 25, 29, 37, 39, 43,
                51, 53, 57, 65, 67);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testGetServicesByCriteria_Tagging_UnknownTag()
            throws Exception {
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        setLocaleCurrentUser("en", customerUserKey);
        ListCriteria crit = getCriteria(-1, -1, "NonExistingTag", null);
        VOServiceListResult servicesByCriteria = search.getServicesByCriteria(
                supplier.getOrganizationId(), "en", crit);
        servicesByCriteria.getServices();
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testGetServicesByCriteria_Tagging_PartialTag()
            throws Exception {
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        setLocaleCurrentUser("en", customerUserKey);
        String TAG1_partial = TAG1.substring(0, TAG1.length() - 1);
        ListCriteria crit = getCriteria(-1, -1, TAG1_partial, null);
        VOServiceListResult servicesByCriteria = search.getServicesByCriteria(
                supplier.getOrganizationId(), "en", crit);
        servicesByCriteria.getServices();
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testGetServicesByCriteria_Tagging_TwoTagsCombined()
            throws Exception {
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        setLocaleCurrentUser("en", customerUserKey);
        ListCriteria crit = getCriteria(-1, -1, TAG1.concat(" ").concat(TAG2),
                null);
        VOServiceListResult servicesByCriteria = search.getServicesByCriteria(
                supplier.getOrganizationId(), "en", crit);
        servicesByCriteria.getServices();
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testGetServicesByCriteria_Tagging_TagNotDefinedForUserLocale()
            throws Exception {
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        setLocaleCurrentUser("en", customerUserKey);
        ListCriteria crit = getCriteria(-1, -1, TAG4, null);
        VOServiceListResult servicesByCriteria = search.getServicesByCriteria(
                supplier.getOrganizationId(), "en", crit);
        servicesByCriteria.getServices();
    }

    @Test
    public void testGetServicesByCriteria_Tagging_TagDefinedForUserLocale()
            throws Exception {
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        setLocaleCurrentUser("en", customerUserKey);
        ListCriteria crit = getCriteria(-1, -1, TAG4, null);
        VOServiceListResult servicesByCriteria = search.getServicesByCriteria(
                supplier.getOrganizationId(), "de", crit);
        List<VOService> products = servicesByCriteria.getServices();
        Assert.assertEquals(3, products.size());
        checkResultSet(servicesByCriteria, 57, 65, 67);
    }

    @Test
    public void testGetServicesByCriteria_Tagging_DifferentTargetLocale()
            throws Exception {
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        setLocaleCurrentUser("en", customerUserKey);
        ListCriteria crit = getCriteria(-1, -1, "de," + TAG4, null);
        VOServiceListResult servicesByCriteria = search.getServicesByCriteria(
                supplier.getOrganizationId(), "en", crit);
        List<VOService> products = servicesByCriteria.getServices();
        Assert.assertEquals(3, products.size());
        checkResultSet(servicesByCriteria, 57, 65, 67);
    }

    @Test
    public void testGetServicesByCriteria_Tagging_TagInMultipleLocales_English()
            throws Exception {
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        setLocaleCurrentUser("en", customerUserKey);
        ListCriteria crit = getCriteria(-1, -1, TAG1, null);
        VOServiceListResult servicesByCriteria = search.getServicesByCriteria(
                supplier.getOrganizationId(), "en", crit);
        checkResultSet(servicesByCriteria, 1, 9, 11, 15, 23, 25);
    }

    @Test
    public void testGetServicesByCriteria_Tagging_TagInMultipleLocales_German()
            throws Exception {
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        setLocaleCurrentUser("en", customerUserKey);
        ListCriteria crit = getCriteria(-1, -1, TAG1, null);
        VOServiceListResult servicesByCriteria = search.getServicesByCriteria(
                supplier.getOrganizationId(), "de", crit);
        checkResultSet(servicesByCriteria, 43, 51, 53);
    }

    @Test
    public void testGetServicesByCriteria_Sorting_RatingDesc()
            throws Exception {
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        setLocaleCurrentUser("en", customerUserKey);
        ListCriteria crit = getCriteria(-1, -1, null,
                Sorting.RATING_DESCENDING);
        VOServiceListResult servicesByCriteria = search.getServicesByCriteria(
                supplier.getOrganizationId(), "en", crit);
        Assert.assertEquals(localLoggedInVisible.size(),
                servicesByCriteria.getResultSize());
        List<VOService> services = servicesByCriteria.getServices();

        // 4,1 rated ordered by name ascending
        List<Long> keys = getServiceKeysByName(true,
                getServices(localLoggedInVisible, 9, 23, 37, 51, 65));
        // 3,7 rated ordered by name ascending
        keys.addAll(getServiceKeysByName(true,
                getServices(localLoggedInVisible, 1, 15, 29, 43, 57)));
        // null rated ordered by name ascending
        keys.addAll(getServiceKeysByName(true,
                getServices(localLoggedInVisible, 11, 25, 39, 53, 67)));
        compare(keys, services);
    }

    @Test
    public void testGetServicesByCriteria_Sorting_RatingAsc() throws Exception {
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        setLocaleCurrentUser("en", customerUserKey);
        ListCriteria crit = getCriteria(-1, -1, null, Sorting.RATING_ASCENDING);
        VOServiceListResult servicesByCriteria = search.getServicesByCriteria(
                supplier.getOrganizationId(), "en", crit);
        Assert.assertEquals(localLoggedInVisible.size(),
                servicesByCriteria.getResultSize());
        List<VOService> services = servicesByCriteria.getServices();

        // null rated ordered by name ascending
        List<Long> keys = getServiceKeysByName(true,
                getServices(localLoggedInVisible, 11, 25, 39, 53, 67));
        // 3,7 rated ordered by name ascending
        keys.addAll(getServiceKeysByName(true,
                getServices(localLoggedInVisible, 1, 15, 29, 43, 57)));
        // 4,1 rated ordered by name ascending
        keys.addAll(getServiceKeysByName(true,
                getServices(localLoggedInVisible, 9, 23, 37, 51, 65)));
        compare(keys, services);
    }

    @Test
    public void testGetServicesByCriteria_Sorting_RatingDescPublic()
            throws Exception {
        container.logout();
        ListCriteria crit = getCriteria(-1, -1, null,
                Sorting.RATING_DESCENDING);
        VOServiceListResult servicesByCriteria = search.getServicesByCriteria(
                supplier.getOrganizationId(), "en", crit);
        Assert.assertEquals(localPublicVisible.size(),
                servicesByCriteria.getResultSize());
        List<VOService> services = servicesByCriteria.getServices();

        // 4,2 rated by name ascending
        List<Long> keys = getServiceKeysByName(true,
                getServices(localPublicVisible, 4, 18, 32, 46, 60));
        // 3,7 rated by name ascending
        keys.addAll(getServiceKeysByName(true,
                getServices(localPublicVisible, 1, 15, 29, 43, 57)));
        // null rated by name ascending
        keys.addAll(getServiceKeysByName(true,
                getServices(localPublicVisible, 5, 19, 33, 47, 61)));
        compare(keys, services);
    }

    @Test
    public void testGetServicesByCriteria_Sorting_RatingAscPublic()
            throws Exception {
        container.logout();
        ListCriteria crit = getCriteria(-1, -1, null, Sorting.RATING_ASCENDING);
        VOServiceListResult servicesByCriteria = search.getServicesByCriteria(
                supplier.getOrganizationId(), "en", crit);
        Assert.assertEquals(localPublicVisible.size(),
                servicesByCriteria.getResultSize());
        List<VOService> services = servicesByCriteria.getServices();

        // null rated by name ascending
        List<Long> keys = getServiceKeysByName(true,
                getServices(localPublicVisible, 5, 19, 33, 47, 61));
        // 3,7 rated ordered by name ascending
        keys.addAll(getServiceKeysByName(true,
                getServices(localPublicVisible, 1, 15, 29, 43, 57)));
        // 4,2 rated by name ascending
        keys.addAll(getServiceKeysByName(true,
                getServices(localPublicVisible, 4, 18, 32, 46, 60)));
        compare(keys, services);
    }

    @Test
    public void testGetServicesByCriteria_WithTemplateSubscriptionLocal()
            throws Exception {
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        setLocaleCurrentUser("en", customerUserKey);
        ListCriteria crit = getCriteria(-1, -1, null, null);
        final VOServiceListResult servicesByCriteriaBefore = search
                .getServicesByCriteria(supplier.getOrganizationId(), "en",
                        crit);
        Assert.assertFalse(resultContainsService(suspendedServiceKey,
                servicesByCriteriaBefore));
        Assert.assertFalse(resultContainsService(suspendedCustServiceKey,
                servicesByCriteriaBefore));

        // the key of the first not customer specific service
        long key = allServices.get(1).getKey();
        VOService subscribeTo = null;
        for (VOService svc : servicesByCriteriaBefore.getServices()) {
            if (key == svc.getKey()) {
                subscribeTo = svc;
                break;
            }
        }
        Assert.assertNotNull(subscribeTo);
        VOSubscription sub = new VOSubscription();
        sub.setSubscriptionId("subscriptionId");
        sub.setServiceKey(key);

        VOPaymentInfo pi = getPaymentInfo(supplier.getOrganizationId(),
                getOrganizationForCurrentUser());
        VOBillingContact bc = createBillingContact(
                getOrganizationForCurrentUser());
        ss.subscribeToService(sub, subscribeTo, null, pi, bc,
                new ArrayList<VOUda>());

        VOServiceListResult servicesByCriteriaAfter = search
                .getServicesByCriteria(supplier.getOrganizationId(), "en",
                        crit);
        Assert.assertEquals(servicesByCriteriaBefore.getResultSize(),
                servicesByCriteriaAfter.getResultSize());
    }

    @Test
    public void testGetServicesByCriteria_WithCustSpecSubscriptionGlobal()
            throws Exception {
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        setLocaleCurrentUser("en", customerUserKey);
        ListCriteria crit = getCriteria(-1, -1, null, null);
        final VOServiceListResult servicesByCriteriaBefore = search
                .getServicesByCriteria(FUJITSU, "en", crit);
        Assert.assertFalse(resultContainsService(suspendedServiceKey,
                servicesByCriteriaBefore));
        Assert.assertFalse(resultContainsService(suspendedCustServiceKey,
                servicesByCriteriaBefore));

        VOSubscription sub = new VOSubscription();
        sub.setSubscriptionId("subscriptionId2");
        VOService svc = servicesByCriteriaBefore.getServices().get(0);
        sub.setServiceKey(svc.getKey());

        VOPaymentInfo pi = getPaymentInfo(supplier.getOrganizationId(),
                getOrganizationForCurrentUser());
        VOBillingContact bc = createBillingContact(
                getOrganizationForCurrentUser());
        ss.subscribeToService(sub, svc, null, pi, bc, new ArrayList<VOUda>());

        VOServiceListResult servicesByCriteriaAfter = search
                .getServicesByCriteria(FUJITSU, "en", crit);
        Assert.assertEquals(servicesByCriteriaBefore.getResultSize(),
                servicesByCriteriaAfter.getResultSize());
    }

    @Test
    @Ignore
    public void testSearchServicesByCriteria_Categories() throws Exception {
        container.login(platformOperatorAdminKey, ROLE_ORGANIZATION_ADMIN);
        VOServiceListResult result = search.searchServices(FUJITSU, "de", CAT1);
        Assert.assertEquals(2, result.getResultSize());
        Assert.assertTrue(result.getServices().get(0).getName().endsWith(TAG1));
        Assert.assertTrue(result.getServices().get(1).getName().endsWith(TAG2));

        result = search.searchServices(FUJITSU, "de", CAT1 + " deutsch");

        Assert.assertEquals(result.getResultSize(), 2);
        Assert.assertTrue(result.getServices().get(0).getName().endsWith(TAG1));
        Assert.assertTrue(result.getServices().get(1).getName().endsWith(TAG2));
        result = search.searchServices(FUJITSU, "en", CAT1 + " deutsch");
        Assert.assertEquals(result.getResultSize(), 0);
        result = search.searchServices(FUJITSU, "de", CAT2 + " deutsch");
        Assert.assertEquals(result.getResultSize(), 1);
        Assert.assertTrue(result.getServices().get(0).getName().endsWith(TAG1));
    }

    @Test
    public void testGetServicesByCriteria_Categories_Cat1_Locale_de()
            throws Exception {
        container.login(platformOperatorAdminKey, ROLE_ORGANIZATION_ADMIN,
                ROLE_SERVICE_MANAGER);

        // find for category which is created in setup
        VOCategory category = getCategory(CAT1_DE);
        Assert.assertNotNull(category);

        ListCriteria listCriteria = createCategoryCriteria(0,
                SEARCHLIMIT_UNLIMITED, category.getCategoryId(), null);

        VOServiceListResult result = search.getServicesByCriteria(FUJITSU, "de",
                listCriteria);

        // validate if services are found
        Assert.assertEquals(2, result.getResultSize());
        List<VOService> expectedServices = new LinkedList<>();
        expectedServices.add(servicesForPublicCatalog.get(0));
        expectedServices.add(servicesForPublicCatalog.get(1));

        for (VOService resultService : result.getServices()) {
            boolean serviceFound = false;
            for (VOService expectedService : expectedServices) {
                if (resultService.getServiceId()
                        .equals(expectedService.getServiceId())) {
                    serviceFound = true;
                }
            }
            Assert.assertTrue(serviceFound);
        }
    }

    @Test
    public void testGetServicesByCriteria_Categories_Cat2_Locale_de()
            throws Exception {
        checkServicesByCriteria_Categories_Cat2("de");
    }

    @Test
    public void testGetServicesByCriteria_Categories_Cat2_Locale_en()
            throws Exception {
        checkServicesByCriteria_Categories_Cat2("en");
    }

    @Test
    public void testGetServicesByCriteria_Categories_InvalidCategoryId() {
        container.login(platformOperatorAdminKey, ROLE_ORGANIZATION_ADMIN);

        ListCriteria listCriteria = createCategoryCriteria(0,
                SEARCHLIMIT_UNLIMITED, "InvalidCategoryId", null);

        try {
            search.getServicesByCriteria(FUJITSU, "de", listCriteria);
            Assert.fail();
        } catch (ObjectNotFoundException e) {
            Assert.assertTrue(
                    e.getDomainObjectClassEnum() == ClassEnum.CATEGORY);
        }
    }

    @Test
    public void testGetServicesByCriteria_Categories_Count() throws Exception {
        VOCategory category = getCategory(CAT2_DE);
        ListCriteria listCriteria = createCategoryCriteria(0,
                SEARCHLIMIT_ONLYCOUNT, category.getCategoryId(), null);

        VOServiceListResult result = search.getServicesByCriteria(FUJITSU, "de",
                listCriteria);

        Assert.assertEquals(1, result.getResultSize());
    }

    @Test
    public void testGetServicesByCategory_Cat1_Locale_de() throws Exception {
        container.login(platformOperatorAdminKey, ROLE_ORGANIZATION_ADMIN,
                ROLE_SERVICE_MANAGER);

        // find for category which is created in setup
        VOCategory category = getCategory(CAT1_DE);
        Assert.assertNotNull(category);

        ProductSearchResult result = searchLocal.getServicesByCategory(FUJITSU,
                category.getCategoryId());

        // validate if services are found
        Assert.assertEquals(2, result.getResultSize());
        List<VOService> expectedServices = new LinkedList<>();
        expectedServices.add(servicesForPublicCatalog.get(0));
        expectedServices.add(servicesForPublicCatalog.get(1));

        for (Product resultService : result.getServices()) {
            boolean serviceFound = false;
            for (VOService expectedService : expectedServices) {
                if (resultService.getCleanProductId()
                        .equals(expectedService.getServiceId())) {
                    serviceFound = true;
                }
            }
            Assert.assertTrue(serviceFound);
        }
    }

    @Test
    public void testGetServicesByCategory_InvalidCategoryId() {
        container.login(platformOperatorAdminKey, ROLE_ORGANIZATION_ADMIN);

        try {
            searchLocal.getServicesByCategory(FUJITSU, "InvalidCategoryId");
            Assert.fail();
        } catch (ObjectNotFoundException e) {
            Assert.assertTrue(
                    e.getDomainObjectClassEnum() == ClassEnum.CATEGORY);
        }
    }

    @Test
    public void testGetServicesByCriteria_AsMPOwnerAdmin() throws Exception {
        container.login(platformOperatorAdminKey, ROLE_ORGANIZATION_ADMIN);
        ListCriteria crit = getCriteria(-1, -1, null, null);
        final VOServiceListResult result = search.getServicesByCriteria(FUJITSU,
                "en", crit);
        Assert.assertTrue(resultContainsService(suspendedServiceKey, result));
        Assert.assertFalse(
                resultContainsService(suspendedCustServiceKey, result));
    }

    @Test
    public void testGetServicesByCriteria_AsMPOwnerNonAdmin() throws Exception {
        container.login(platformOperatorUserKey);
        ListCriteria crit = getCriteria(-1, -1, null, null);
        final VOServiceListResult result = search.getServicesByCriteria(FUJITSU,
                "en", crit);
        Assert.assertFalse(resultContainsService(suspendedServiceKey, result));
        Assert.assertFalse(
                resultContainsService(suspendedCustServiceKey, result));
    }

    @Test
    public void testGetServicesByCriteria_AsUnitAdmin() throws Exception {
        // given
        container.login(unitAdminUserKey, ROLE_UNIT_ADMINISTRATOR);
        ListCriteria crit = getCriteria(-1, -1, null, null);

        // when
        search.getServicesByCriteria(FUJITSU, "en", crit);

        // then
        verify(userGroupServiceLocalBean, atLeastOnce())
                .getInvisibleProductKeysForUser(unitAdminUserKey);
    }

    /**
     * @param suspendedServiceKey2
     * @param result
     * @return
     */
    private static boolean resultContainsService(long serviceKey,
            VOServiceListResult result) {
        List<VOService> services = result.getServices();
        for (VOService svc : services) {
            if (svc.getKey() == serviceKey) {
                return true;
            }
        }
        return false;
    }

    /**
     * This checks that all given elements are contained in the given result
     * set. The expected elements are given by their indices referring to the
     * allServices list. <br>
     * Note: the given indices are zero-based.
     * 
     * @param result
     *            the actual result set of the query
     * @param indices
     *            the indices of the elements with respect to the allServices
     *            list to be expected in the result set
     * @return <code>true</code> if the given elements (and no more) are
     *         contained in the result list, and false otherwise.
     */
    private static void checkResultSet(VOServiceListResult result,
            int... indices) {
        Set<Long> svcKeys = new HashSet<>();
        // store keys of services contained in result set
        for (VOService svc : result.getServices()) {
            long key = svc.getKey();
            System.out.println("Result set contains: " + key);
            svcKeys.add(key);
        }
        // now compare with the key of the expected service from allServices
        for (int index : indices) {
            Assert.assertTrue(
                    "Expected service at index " + (index) + " " + allServices.get(index).getKey()
                            + " to be in the result set",
                    svcKeys.contains(
                            allServices.get(index).getKey()));
        }
        System.out.println("Result contains " + resultAsString(result));
        // make sure no additional services are expected to be in the result
        Assert.assertEquals("Result contains " + resultAsString(result),
                indices.length, result.getResultSize());
    }

    private static String resultAsString(VOServiceListResult result) {
        StringBuffer sb = new StringBuffer();
        for (Iterator<VOService> iterator = result.getServices()
                .iterator(); iterator.hasNext();) {
            VOService service = iterator.next();
            sb.append(service.getServiceId());
            if (iterator.hasNext()) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    /**
     * This checks that all given elements are contained in the given result set
     * and in the given order. The expected elements are given by their indices
     * referring to the allServices list. <br>
     * Note: the given indices are zero-based.
     * 
     * @param result
     *            the actual result set of the query
     * @param indices
     *            the indices of the elements with respect to the allServices
     *            list to be expected in the result set
     * @return <code>true</code> if the given elements (and no more) are
     *         contained in the result list and in the given order, and false
     *         otherwise.
     */
    private static void checkResultListOrdered(VOServiceListResult result,
            int... indices) {
        int pos = 0;
        for (int index : indices) {
            assertEquals(
                    "Expected service at index " + (index)
                            + " to be at position " + pos
                            + " in the result list. Service key ",
                    allServices.get(index).getKey(),
                    result.getServices().get(pos).getKey());
            pos++;
        }
        // make sure no additional services are expected to be in the result
        assertEquals(indices.length, result.getResultSize());
    }

    /**
     * Puts the services specified by the indices from the allServices list to
     * the result
     * 
     * @param indices
     *            the wanted service indices
     * @return the {@link VOService} list
     */
    private static List<VOService> getServices(List<VOService> visibleServices,
            int... indices) {
        List<VOService> result = new ArrayList<>();
        for (int index : indices) {
            // get the service key to the index
            VOService toGet = allServices.get(index);
            VOService found = null;
            // and now find the service with the correct name as the one in the
            // allServices is not reread after localization
            for (VOService svc : visibleServices) {
                if (svc.getKey() == toGet.getKey()) {
                    found = svc;
                    break;
                }
            }
            Assert.assertNotNull(found);
            result.add(found);
        }
        return result;
    }

    private Organization getOrganizationForCurrentUser() throws Exception {
        return runTX(new Callable<Organization>() {
            @Override
            public Organization call() {
                return ds.getCurrentUser().getOrganization();
            }
        });
    }

    private static void createAvailablePayment(final Organization org)
            throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Organization cust = ds.getReference(Organization.class,
                        org.getKey());
                OrganizationRole role = new OrganizationRole();
                role.setRoleName(OrganizationRoleType.CUSTOMER);
                role = (OrganizationRole) ds.getReferenceByBusinessKey(role);

                for (PaymentType type : paymentTypes) {
                    OrganizationRefToPaymentType apt = new OrganizationRefToPaymentType();
                    apt.setOrganizationReference(cust.getSources().get(0));
                    apt.setPaymentType(type);
                    apt.setOrganizationRole(role);
                    apt.setUsedAsDefault(false);
                    ds.persist(apt);
                }
                return null;
            }
        });
    }

    private VOPaymentInfo getPaymentInfo(final String supplierId,
            final Organization organization) throws Exception {
        if (!voPaymentInfos.containsKey(supplierId)) {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    VOPaymentInfo newPaymentInfo = PaymentInfos
                            .createVOPaymentInfo(organization, ds,
                                    paymentTypes.get(0));
                    voPaymentInfos.put(supplierId, newPaymentInfo);
                    return null;
                }
            });
        }
        return voPaymentInfos.get(supplierId);
    }

    private VOBillingContact createBillingContact(
            final Organization organization) throws Exception {
        return runTX(new Callable<VOBillingContact>() {
            @Override
            public VOBillingContact call() throws Exception {
                return PaymentInfos.createBillingContact(organization, ds);
            }
        });
    }

    private VOCategory getCategory(String categoryName) {
        final List<VOCategory> categories = cs.getCategories(FUJITSU, "de");

        VOCategory category = null;
        for (VOCategory voCategory : categories) {
            if (voCategory.getName().equals(categoryName)) {
                category = voCategory;
                break;
            }
        }
        return category;
    }

    /**
     * Test getServiceByCriteria with category and locale
     * 
     * @param locale
     *            Locale
     * @throws Exception
     *             ObjectNotFoundException, OperationNotPermittedException, ...
     */
    private void checkServicesByCriteria_Categories_Cat2(String locale)
            throws Exception {
        container.login(platformOperatorAdminKey, ROLE_ORGANIZATION_ADMIN);

        VOCategory category = getCategory(CAT2_DE);
        Assert.assertNotNull(category);

        ListCriteria listCriteria = createCategoryCriteria(0,
                SEARCHLIMIT_UNLIMITED, category.getCategoryId(),
                Sorting.NAME_DESCENDING);

        VOServiceListResult result = search.getServicesByCriteria(FUJITSU,
                locale, listCriteria);

        Assert.assertEquals(1, result.getResultSize());
        Assert.assertEquals(servicesForPublicCatalog.get(0).getServiceId(),
                result.getServices().get(0).getServiceId());
    }
}
