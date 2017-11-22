/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: pravi                                                    
 *                                                                              
 *  Creation Date: Sep 4, 2009                                                      
 *                                                                              
 *  Completion Time: Sep 4, 2009                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.subscriptionservice.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Currency;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.junit.Assert;
import org.junit.Test;
import org.oscm.accountservice.bean.MarketingPermissionServiceBean;
import org.oscm.applicationservice.bean.ApplicationServiceStub;
import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.converter.BigDecimalComparator;
import org.oscm.converter.ParameterizedTypes;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.DomainHistoryObject;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationRefToPaymentType;
import org.oscm.domobjects.OrganizationReference;
import org.oscm.domobjects.OrganizationRole;
import org.oscm.domobjects.ParameterSet;
import org.oscm.domobjects.PaymentType;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.PriceModel;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.SubscriptionHistory;
import org.oscm.domobjects.SupportedCurrency;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.domobjects.TriggerProcess;
import org.oscm.domobjects.UsageLicense;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.domobjects.enums.ModificationType;
import org.oscm.domobjects.enums.OrganizationReferenceType;
import org.oscm.encrypter.AESEncrypter;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.i18nservice.bean.LocalizerServiceBean;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.identityservice.assembler.UserDataAssembler;
import org.oscm.identityservice.bean.IdManagementStub;
import org.oscm.internal.intf.IdentityService;
import org.oscm.internal.intf.ServiceProvisioningService;
import org.oscm.internal.intf.SubscriptionService;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.PriceModelType;
import org.oscm.internal.types.enumtypes.PricingPeriod;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.types.enumtypes.ServiceType;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.types.enumtypes.UserAccountStatus;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.TechnicalServiceNotAliveException;
import org.oscm.internal.types.exception.TechnicalServiceOperationException;
import org.oscm.internal.vo.VOBillingContact;
import org.oscm.internal.vo.VOPaymentInfo;
import org.oscm.internal.vo.VOPriceModel;
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOSubscription;
import org.oscm.internal.vo.VOSubscriptionDetails;
import org.oscm.internal.vo.VOUda;
import org.oscm.internal.vo.VOUser;
import org.oscm.internal.vo.VOUserSubscription;
import org.oscm.provisioning.data.User;
import org.oscm.serviceprovisioningservice.assembler.ProductAssembler;
import org.oscm.serviceprovisioningservice.bean.ServiceProvisioningServiceBean;
import org.oscm.serviceprovisioningservice.bean.TagServiceBean;
import org.oscm.sessionservice.bean.SessionManagementStub2;
import org.oscm.subscriptionservice.local.SubscriptionServiceLocal;
import org.oscm.taskhandling.local.TaskQueueServiceLocal;
import org.oscm.tenantprovisioningservice.bean.TenantProvisioningServiceBean;
import org.oscm.test.EJBTestBase;
import org.oscm.test.ReflectiveClone;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.PaymentInfos;
import org.oscm.test.data.PaymentTypes;
import org.oscm.test.data.PlatformUsers;
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

/**
 * @author pravi
 * 
 */
public class SubscriptionServiceBeanDirectLoginIT extends EJBTestBase {

    private static final String[] ENABLED_PAYMENTTYPES = new String[] {
            PaymentType.CREDIT_CARD, PaymentType.DIRECT_DEBIT,
            PaymentType.INVOICE };

    protected DataService mgr;
    protected ApplicationServiceStub appMgmtDirectLogin;
    protected SubscriptionService subMgmt;
    protected SubscriptionServiceLocal subMgmtLocal;
    protected IdentityService idMgmt;
    protected ServiceProvisioningService servProv;
    private LocalizerServiceLocal localizer;

    private final List<Product> testProducts = new ArrayList<>();
    private final List<Organization> testOrganizations = new ArrayList<>();
    private Map<String, VOPaymentInfo> voPaymentInfos = new HashMap<>();
    private Organization supplier;
    private final Map<Organization, ArrayList<PlatformUser>> testUsers = new HashMap<>();
    private String currentLocale = "en";
    private List<PaymentType> paymentTypes;

    @Override
    public void setup(TestContainer container) throws Exception {
        AESEncrypter.generateKey();
        container.enableInterfaceMocking(true);

        container.login("1");
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new DataServiceBean());
        container.addBean(appMgmtDirectLogin = new ApplicationServiceStub() {

            @Override
            public User[] createUsers(Subscription subscription,
                    List<UsageLicense> usageLicenses)
                    throws TechnicalServiceNotAliveException,
                    TechnicalServiceOperationException {
                if (subscription.getProduct().getTechnicalProduct()
                        .getAccessType() == ServiceAccessType.DIRECT) {
                    return null;
                } else {
                    return super.createUsers(subscription, usageLicenses);
                }
            }

            @Override
            public void deleteUsers(Subscription subscription,
                    List<UsageLicense> licenses)
                    throws TechnicalServiceNotAliveException,
                    TechnicalServiceOperationException {
                if (subscription.getProduct().getTechnicalProduct()
                        .getAccessType() == ServiceAccessType.DIRECT) {
                    return;
                } else {
                    super.deleteUsers(subscription, licenses);
                }
            }

        });
        container.addBean(new SessionManagementStub2());
        container.addBean(new IdManagementStub());
        container.addBean(new TenantProvisioningServiceBean());
        container.addBean(new CommunicationServiceStub());
        container.addBean(new LocalizerServiceBean());
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new ImageResourceServiceStub());
        container.addBean(mock(TaskQueueServiceLocal.class));

        container.addBean(new TagServiceBean());
        container.addBean(new MarketingPermissionServiceBean());
        container.addBean(new MarketplaceServiceStub());
        container.addBean(new ServiceProvisioningServiceBean());
        container.addBean(new SubscriptionListServiceBean());
        container.addBean(new SubscriptionServiceBean());
        container.addBean(new TerminateSubscriptionBean());
        container.addBean(new ManageSubscriptionBean());

        localizer = container.get(LocalizerServiceLocal.class);
        mgr = container.get(DataService.class);
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                paymentTypes = createPaymentTypes(mgr);
                createOrganizationRoles(mgr);
                SupportedCountries.createSomeSupportedCountries(mgr);
                return null;
            }
        });
        Long userKey = runTX(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                return initMasterData();
            }
        });
        container.addBean(new TriggerQueueServiceStub() {
            @Override
            public List<TriggerProcessMessageData> sendSuspendingMessages(
                    List<TriggerMessage> messageData) {
                TriggerProcess tp = new TriggerProcess();
                tp.setTriggerDefinition(null);
                tp.setUser(testUsers.get(testOrganizations.get(0)).get(1));
                TriggerProcessMessageData data = new TriggerProcessMessageData(
                        tp, null);
                return Collections.singletonList(data);
            }

            @Override
            public void sendAllNonSuspendingMessages(
                    List<TriggerMessage> messageData) {
            }
        });
        createAvailablePayment(testOrganizations.get(0));
        container.login(String.valueOf(userKey), ROLE_ORGANIZATION_ADMIN);
        subMgmt = container.get(SubscriptionService.class);
        subMgmtLocal = container.get(SubscriptionServiceLocal.class);
        idMgmt = container.get(IdentityService.class);
        servProv = container.get(ServiceProvisioningService.class);
        ConfigurationServiceLocal cfg = container
                .get(ConfigurationServiceLocal.class);
        setUpDirServerStub(cfg);
    }

    /**
     * Initialize test database with master data (products and price models)
     * 
     * @throws NonUniqueBusinessKeyException
     * @throws ObjectNotFoundException
     */
    private Long initMasterData() throws Exception {
        Long initialCustomerAdminKey = null;

        Organization organization = Organizations.createOrganization(mgr,
                OrganizationRoleType.SUPPLIER,
                OrganizationRoleType.TECHNOLOGY_PROVIDER);
        createTechnicalProducts(organization);
        supplier = organization;

        Organization cust = null;
        for (int i = 1; i <= 2; i++) {
            cust = Organizations.createOrganization(mgr,
                    OrganizationRoleType.CUSTOMER);
            OrganizationReference supplierToCustomer = new OrganizationReference(
                    organization, cust,
                    OrganizationReferenceType.SUPPLIER_TO_CUSTOMER);

            List<OrganizationReference> sources = cust.getSources();
            if (sources == null) {
                sources = new LinkedList<>();
            }
            sources.add(supplierToCustomer);
            cust.setSources(sources);
            mgr.persist(supplierToCustomer);

            Organization addCust = (Organization) ReflectiveClone.clone(cust);
            testOrganizations.add(addCust);
            ArrayList<PlatformUser> userlist = new ArrayList<>();
            testUsers.put(addCust, userlist);

            PlatformUser admin = createAdmin(cust);
            if (initialCustomerAdminKey == null) {
                initialCustomerAdminKey = Long.valueOf(admin.getKey());
            }
            userlist.add((PlatformUser) ReflectiveClone.clone(admin));
            for (int j = 1; j <= 2; j++) {
                PlatformUser usr1 = createNonAdminPlatformUser(cust, j);
                mgr.persist(usr1);
                userlist.add((PlatformUser) ReflectiveClone.clone(usr1));
            }
        }
        createAdmin(organization);
        return initialCustomerAdminKey;
    }

    /**
     * @param organization
     * @throws NonUniqueBusinessKeyException
     * @throws ObjectNotFoundException
     */
    private void createTechnicalProducts(Organization organization)
            throws NonUniqueBusinessKeyException, ObjectNotFoundException {
        // Technical product with synchronous provisioning and access info
        TechnicalProduct tProd = TechnicalProducts.createTechnicalProduct(mgr,
                organization, "TPWithAccessInfo_ID", false,
                ServiceAccessType.DIRECT);
        mgr.persist(getSupportedCurrency());
        addProducts(organization, tProd, 4, testProducts);
        String accessInfoSynProv = "access information for direct login product with synchronous Provisioning"
                + tProd.getKey();
        localizer.storeLocalizedResource(currentLocale, tProd.getKey(),
                LocalizedObjectTypes.TEC_PRODUCT_LOGIN_ACCESS_DESC,
                accessInfoSynProv);

        // Technical product with synchronous provisioning and without access
        // info
        tProd = TechnicalProducts.createTechnicalProduct(mgr, organization,
                "TPWithoutAccessInfo_ID", false, ServiceAccessType.DIRECT);
        addProducts(organization, tProd, 4, testProducts);
    }

    /**
     * @param cust
     * @param ii
     * @return
     */
    private PlatformUser createNonAdminPlatformUser(Organization cust, int ii) {
        PlatformUser usr1 = new PlatformUser();
        usr1.setAdditionalName("name" + ii);
        usr1.setAddress("Address" + ii);
        usr1.setCreationDate(GregorianCalendar.getInstance().getTime());
        usr1.setEmail("EMail" + ii);
        usr1.setFirstName("FirstName" + ii);
        // create system wide unique userId
        usr1.setUserId("usr" + ii + "_" + cust.getOrganizationId());
        usr1.setLastName("LastName" + ii);
        usr1.setPhone("111111/111111");
        usr1.setStatus(UserAccountStatus.ACTIVE);
        usr1.setOrganization(cust);
        usr1.setLocale(currentLocale);
        return usr1;
    }

    private PlatformUser createAdmin(Organization cust) throws Exception {
        PlatformUser admin = new PlatformUser();
        admin.setAdditionalName("AddName Admin");
        admin.setAddress("Address");
        admin.setCreationDate(GregorianCalendar.getInstance().getTime());
        admin.setEmail("admin@organization.com");
        admin.setFirstName("FirstName");
        // create system wide unique userId
        admin.setUserId("admin_" + cust.getOrganizationId());
        admin.setLastName("LastName");
        admin.setPhone("111111/111111");
        admin.setStatus(UserAccountStatus.ACTIVE);
        admin.setOrganization(cust);
        admin.setLocale("en");
        mgr.persist(admin);
        PlatformUsers.grantAdminRole(mgr, admin);
        mgr.flush();
        return admin;
    }

    private void addProducts(Organization supplier, TechnicalProduct tProd,
            int max, List<Product> products)
            throws NonUniqueBusinessKeyException, ObjectNotFoundException {
        Product prod;
        for (int i = 1; i < max; i++) {
            prod = new Product();
            prod.setVendor(supplier);
            prod.setProductId(getProductId(tProd, i));
            prod.setTechnicalProduct(tProd);
            prod.setProvisioningDate(System.currentTimeMillis());
            prod.setStatus(ServiceStatus.ACTIVE);
            prod.setAutoAssignUserEnabled(Boolean.FALSE);
            PriceModel pm = new PriceModel();
            if (i % 2 != 1) {
                pm.setType(PriceModelType.PRO_RATA);
                pm.setPeriod(PricingPeriod.DAY);
                pm.setPricePerPeriod(new BigDecimal(1));
                pm.setPricePerUserAssignment(new BigDecimal(100));

                SupportedCurrency sc = getSupportedCurrency();

                sc = (SupportedCurrency) mgr.find(sc);
                pm.setCurrency(sc);
                prod.setPriceModel(pm);
            }
            prod.setPriceModel(pm);
            ParameterSet paramSet = new ParameterSet();
            prod.setParameterSet(paramSet);
            prod.setType(ServiceType.TEMPLATE);
            mgr.persist(prod);
            PaymentTypes.enableForProduct(prod, mgr, ENABLED_PAYMENTTYPES);
            products.add((Product) ReflectiveClone.clone(prod));
        }
    }

    /**
     * @return
     */
    private SupportedCurrency getSupportedCurrency() {
        SupportedCurrency sc = new SupportedCurrency();
        sc.setCurrency(Currency.getInstance("EUR"));
        return sc;
    }

    private String getProductId(TechnicalProduct tProd, int i) {
        return "Product" + tProd.getKey() + i;
    }

    private Subscription loadSubscription(final String subscriptionId,
            final long organizationKey) throws ObjectNotFoundException {
        Subscription qry = new Subscription();
        qry.setSubscriptionId(subscriptionId);
        qry.setOrganizationKey(organizationKey);
        return (Subscription) mgr.getReferenceByBusinessKey(qry);
    }

    @Test
    public void testProductAccessType() throws Throwable {
        assertNotNull(subMgmt);
        for (int ii = 0; ii < testProducts.size(); ii++) {
            assertEquals(ServiceAccessType.DIRECT,
                    testProducts.get(ii).getTechnicalProduct().getAccessType());
        }
    }

    @Test
    public void testSubscribeToProduct() throws Throwable {
        assertNotNull(subMgmt);
        VOService product = getProductToSubscribe(testProducts.get(0).getKey());

        VOUser[] admins = new VOUser[1];
        admins[0] = UserDataAssembler
                .toVOUser(testUsers.get(testOrganizations.get(0)).get(1));
        final String subscriptionID = "testSubscriptionForDirectLogin";
        subMgmt.subscribeToService(
                Subscriptions.createVOSubscription(subscriptionID), product,
                getUsersToAdd(admins, null), null, null,
                new ArrayList<VOUda>());
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    checkSubscribeToProduct(subscriptionID, testProducts.get(0),
                            SubscriptionStatus.ACTIVE);
                    return null;
                }
            });
        } catch (Exception e) {
            throw e.getCause();
        }
    }

    private void checkSubscribeToProduct(String subscriptionId, Product product,
            SubscriptionStatus status) {
        // load generated subscription object
        Organization theOrganization = testOrganizations.get(0);
        Subscription qryObj = new Subscription();
        qryObj.setOrganizationKey(theOrganization.getKey());
        qryObj.setSubscriptionId(subscriptionId);
        Subscription subscription = (Subscription) mgr.find(qryObj);

        assertNotNull("Could not load subscription 'testSubscribeToProduct'",
                subscription);
        // check subscription attributes
        assertEquals(
                new SimpleDateFormat("yyyy-MM-dd")
                        .format(GregorianCalendar.getInstance().getTime()),
                new SimpleDateFormat("yyyy-MM-dd")
                        .format(subscription.getCreationDate()));
        // check product
        assertNotNull("No product assigned to subscription",
                subscription.getProduct());
        assertTrue("Wrong ProductId in subscription", subscription.getProduct()
                .getProductId().startsWith(product.getProductId() + "#"));

        assertNotNull("No priceModel assigned to subscription",
                subscription.getPriceModel());
        assertEquals(status, subscription.getStatus());
        assertNotNull(subscription.getProductInstanceId());
        int idx = 1;
        for (UsageLicense lic : subscription.getUsageLicenses()) {
            assertEquals(
                    new SimpleDateFormat("yyyy-MM-dd")
                            .format(GregorianCalendar.getInstance().getTime()),
                    new SimpleDateFormat("yyyy-MM-dd")
                            .format(new Date(lic.getAssignmentDate())));
            assertNotNull("User entry is null for license " + lic.getKey(),
                    lic.getUser());
            assertEquals(
                    "user seems to be wrong (userId)", testUsers
                            .get(testOrganizations.get(0)).get(idx).getUserId(),
                    lic.getUser().getUserId());

            // also check the usage license history entries
            List<DomainHistoryObject<?>> ulhs = mgr.findHistory(lic);
            assertEquals(
                    "Wrong number of history entries found for the usage license",
                    1, ulhs.size());
            assertEquals(
                    "Wrong mod type for found usage license history object",
                    ModificationType.ADD, ulhs.get(0).getModtype());

            idx++;
        }
        assertEquals(
                new SimpleDateFormat("yyyy-MM-dd")
                        .format(GregorianCalendar.getInstance().getTime()),
                new SimpleDateFormat("yyyy-MM-dd")
                        .format(subscription.getActivationDate()));
    }

    @Test
    public void testSubscriptionAccessInfo() throws Throwable {
        VOService product = getProductToSubscribe(testProducts.get(1).getKey());

        VOUser[] admins = new VOUser[1];
        Organization OrganizationID = testOrganizations.get(0);
        admins[0] = UserDataAssembler
                .toVOUser(testUsers.get(OrganizationID).get(1));

        final String subscriptionId = "testAccessInfoForDirectLogin";
        VOSubscription voSubscription = Subscriptions
                .createVOSubscription(subscriptionId);
        VOPaymentInfo voPaymentInfo = getPaymentInfo(
                supplier.getOrganizationId(), testOrganizations.get(0));
        VOBillingContact bc = createBillingContact(testOrganizations.get(0));
        subMgmt.subscribeToService(voSubscription, product,
                getUsersToAdd(admins, null), voPaymentInfo, bc,
                new ArrayList<VOUda>());
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    doTestAccessInfoForDirectLogin(subscriptionId);
                    return null;
                }
            });
        } catch (Exception e) {
            throw e;
        }
    }

    private void doTestAccessInfoForDirectLogin(final String subscriptionId)
            throws Exception {
        Subscription subscription = loadSubscription(subscriptionId,
                testOrganizations.get(0).getKey());
        assertNull(subscription.getAccessInfo());
        String alternativeAccessInfo = localizer.getLocalizedTextFromDatabase(
                currentLocale,
                subscription.getProduct().getTechnicalProduct().getKey(),
                LocalizedObjectTypes.TEC_PRODUCT_LOGIN_ACCESS_DESC);
        assertNotNull(alternativeAccessInfo);
    }

    @Test
    public void testSubscriptionEmptyAccessInfo() throws Throwable {
        VOService product = getProductToSubscribe(testProducts.get(1).getKey());

        VOUser[] admins = new VOUser[1];
        Organization OrganizationID = testOrganizations.get(0);
        admins[0] = UserDataAssembler
                .toVOUser(testUsers.get(OrganizationID).get(1));

        final String subscriptionId = "testEmptyAccessInfoForDirectLogin";
        VOSubscription voSubscription = Subscriptions
                .createVOSubscription(subscriptionId);
        VOPaymentInfo voPaymentInfo = getPaymentInfo(
                supplier.getOrganizationId(), testOrganizations.get(0));
        VOBillingContact bc = createBillingContact(testOrganizations.get(0));
        subMgmt.subscribeToService(voSubscription, product,
                getUsersToAdd(admins, null), voPaymentInfo, bc,
                new ArrayList<VOUda>());
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    doTestEmptyAccessInfoForDirectLogin(subscriptionId);
                    return null;
                }
            });
        } catch (Exception e) {
            throw e;
        }
    }

    private void doTestEmptyAccessInfoForDirectLogin(
            final String subscriptionId) throws Exception {
        Subscription subscription = loadSubscription(subscriptionId,
                testOrganizations.get(0).getKey());
        assertNull(subscription.getAccessInfo());
        String alternativeAccessInfo = localizer.getLocalizedTextFromDatabase(
                currentLocale,
                subscription.getProduct().getTechnicalProduct().getKey(),
                LocalizedObjectTypes.TEC_PRODUCT_LOGIN_ACCESS_DESC);
        assertNotNull(alternativeAccessInfo);
    }

    @Test
    public void testPricePerUserAssignment() throws Throwable {
        assertNotNull(subMgmt);
        VOService product = getProductToSubscribe(testProducts.get(1).getKey());
        VOUser[] users = new VOUser[1];
        VOUser[] admins = new VOUser[1];
        admins[0] = UserDataAssembler
                .toVOUser(testUsers.get(testOrganizations.get(0)).get(1));
        users[0] = UserDataAssembler
                .toVOUser(testUsers.get(testOrganizations.get(0)).get(2));
        VOSubscription voSubscription = Subscriptions
                .createVOSubscription("testSubscriptionForDirectLogin");
        VOPaymentInfo voPaymentInfo = getPaymentInfo(
                supplier.getOrganizationId(), testOrganizations.get(0));
        VOBillingContact bc = createBillingContact(testOrganizations.get(0));
        subMgmt.subscribeToService(voSubscription, product,
                getUsersToAdd(admins, users), voPaymentInfo, bc,
                new ArrayList<VOUda>());

        // Now retrieve data and check results
        String voId = "testSubscriptionForDirectLogin";
        VOSubscriptionDetails voDetails = subMgmt.getSubscriptionDetails(voId);
        assertNotNull(voDetails);
        assertEquals(SubscriptionStatus.ACTIVE, voDetails.getStatus());
        assertTrue(voDetails.getPriceModel().isChargeable());

        assert testProducts.get(1).getPriceModel().isChargeable();
        PriceModel orgPM = testProducts.get(1).getPriceModel();
        VOPriceModel actPM = voDetails.getPriceModel();
        assertEquals(orgPM.getPeriod(), actPM.getPeriod());
        assertEquals(orgPM.getPricePerPeriod(), actPM.getPricePerPeriod());
        Assert.assertTrue(
                BigDecimalComparator.isZero(actPM.getPricePerUserAssignment()));
    }

    @Test
    public void testUserManagementForDirectLogin() throws Throwable {
        VOService product = getProductToSubscribe(testProducts.get(1).getKey());

        VOUser[] admins = new VOUser[1];
        Organization OrganizationID = testOrganizations.get(0);
        admins[0] = UserDataAssembler
                .toVOUser(testUsers.get(OrganizationID).get(1));

        final String subscriptionId = "testUMForDirectLogin";
        VOSubscription voSubscription = Subscriptions
                .createVOSubscription(subscriptionId);
        VOPaymentInfo voPaymentInfo = getPaymentInfo(
                supplier.getOrganizationId(), testOrganizations.get(0));
        VOBillingContact bc = createBillingContact(testOrganizations.get(0));

        subMgmt.subscribeToService(voSubscription, product,
                getUsersToAdd(admins, null), voPaymentInfo, bc,
                new ArrayList<VOUda>());

        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    doTestUserManagementForDirectLogin(subscriptionId);
                    return null;
                }
            });
        } catch (Exception e) {
            throw e;
        }

    }

    private void doTestUserManagementForDirectLogin(String subscriptionId)
            throws Exception {
        Subscription subscription = loadSubscription(subscriptionId,
                testOrganizations.get(0).getKey());
        User[] users = appMgmtDirectLogin
                .createUsersForSubscription(subscription);
        assertNull(users);
        appMgmtDirectLogin.deleteUsers(subscription,
                subscription.getUsageLicenses());

    }

    @Test
    public void testSubscribeToProductNoUsers() throws Throwable {
        assertNotNull(subMgmt);
        VOService product = getProductToSubscribe(testProducts.get(0).getKey());
        subMgmt.subscribeToService(
                Subscriptions.createVOSubscription("unwantedProduct"), product,
                getUsersToAdd(null, null), null, null, new ArrayList<VOUda>());
    }

    @Test
    public void testGetActiveSubscriptionsForCurrentUser() throws Throwable {
        VOService product = getProductToSubscribe(testProducts.get(1).getKey());
        VOUser[] admins = new VOUser[2];
        admins[0] = UserDataAssembler
                .toVOUser(testUsers.get(testOrganizations.get(0)).get(0));
        admins[1] = UserDataAssembler
                .toVOUser(testUsers.get(testOrganizations.get(0)).get(1));
        String subscriptionId1 = "getActiveSubscriptionsForCurrentUser1";
        VOPaymentInfo voPaymentInfo1 = getPaymentInfo(
                supplier.getOrganizationId(), testOrganizations.get(0));
        VOBillingContact bc = createBillingContact(testOrganizations.get(0));

        subMgmt.subscribeToService(
                Subscriptions.createVOSubscription(subscriptionId1), product,
                getUsersToAdd(admins, null), voPaymentInfo1, bc,
                new ArrayList<VOUda>());
        String subscriptionId2 = "getActiveSubscriptionsForCurrentUser2";
        VOPaymentInfo voPaymentInfo2 = getPaymentInfo(
                supplier.getOrganizationId(), testOrganizations.get(0));
        subMgmt.subscribeToService(
                Subscriptions.createVOSubscription(subscriptionId2), product,
                getUsersToAdd(admins, null), voPaymentInfo2, bc,
                new ArrayList<VOUda>());
        container.login(admins[0].getKey(), ROLE_ORGANIZATION_ADMIN);
        List<VOUserSubscription> subList = subMgmt
                .getSubscriptionsForCurrentUser();
        assertEquals("Number of found subscriptions", 2, subList.size());
    }

    @Test
    public void testUnsubscribeFromProductOK() throws Throwable {
        VOService product = getProductToSubscribe(testProducts.get(1).getKey());
        VOUser[] admins = new VOUser[1];
        admins[0] = UserDataAssembler
                .toVOUser(testUsers.get(testOrganizations.get(0)).get(1));
        String subscriptionId = "testUnsubscribeFromProductOK";
        VOPaymentInfo voPaymentInfo = getPaymentInfo(
                supplier.getOrganizationId(), testOrganizations.get(0));
        VOBillingContact bc = createBillingContact(testOrganizations.get(0));

        subMgmt.subscribeToService(
                Subscriptions.createVOSubscription(subscriptionId), product,
                getUsersToAdd(admins, null), voPaymentInfo, bc,
                new ArrayList<VOUda>());
        final VOSubscriptionDetails sub = subMgmt
                .getSubscriptionDetails(subscriptionId);
        assertNotNull(sub);

        subMgmt.unsubscribeFromService(subscriptionId);
        // check results
        String newSubId = runTX(new Callable<String>() {
            @Override
            public String call() throws Exception {
                Subscription subscription = new Subscription();
                subscription.setKey(sub.getKey());
                List<SubscriptionHistory> hist = ParameterizedTypes.list(
                        mgr.findHistory(subscription),
                        SubscriptionHistory.class);
                return hist.get(hist.size() - 1).getDataContainer()
                        .getSubscriptionId();
            }
        });

        final VOSubscriptionDetails renamedSub = subMgmt
                .getSubscriptionDetails(newSubId);
        assertNotNull(renamedSub);
        assertEquals(SubscriptionStatus.DEACTIVATED, renamedSub.getStatus());
        assertEquals(
                new SimpleDateFormat("yyyy-MM-dd")
                        .format(GregorianCalendar.getInstance().getTime()),
                new SimpleDateFormat("yyyy-MM-dd")
                        .format(renamedSub.getDeactivationDate()));
        Assert.assertEquals("Licenses were not removed!", 0,
                renamedSub.getUsageLicenses().size());
        assertEquals("Wrong number of users added to the product", 0,
                appMgmtDirectLogin.addedUsers.size());
        assertEquals("Wrong number of users removed from the product", 0,
                appMgmtDirectLogin.deletedUsers.size());
    }

    @Test
    public void testGetSubscriptionsForOrganization() throws Throwable {
        createAvailablePayment(testOrganizations.get(1));
        long userKey = testUsers.get(testOrganizations.get(0)).get(0).getKey();
        container.login(String.valueOf(userKey), ROLE_ORGANIZATION_ADMIN);

        VOService product = getProductToSubscribe(testProducts.get(1).getKey());
        VOUser[] admins = new VOUser[1];
        admins[0] = UserDataAssembler
                .toVOUser(testUsers.get(testOrganizations.get(0)).get(2));

        final String subscriptionId1 = "testGetSubscriptionsForOrganization1";
        VOPaymentInfo voPaymentInfo1 = getPaymentInfo(
                supplier.getOrganizationId(), testOrganizations.get(0));
        VOBillingContact bc = createBillingContact(testOrganizations.get(0));

        subMgmt.subscribeToService(
                Subscriptions.createVOSubscription(subscriptionId1), product,
                getUsersToAdd(admins, null), voPaymentInfo1, bc,
                new ArrayList<VOUda>());
        // create a new one
        String subscriptionId2 = "testGetSubscriptionsForOrganization2";
        VOPaymentInfo voPaymentInfo2 = getPaymentInfo(
                supplier.getOrganizationId(), testOrganizations.get(0));
        subMgmt.subscribeToService(
                Subscriptions.createVOSubscription(subscriptionId2), product,
                getUsersToAdd(admins, null), voPaymentInfo2, bc,
                new ArrayList<VOUda>());
        List<VOSubscription> subList = subMgmt
                .getSubscriptionsForOrganization();
        assertEquals("Number of found subscriptions", 2, subList.size());
        final long subKey1;
        if (subscriptionId1.equals(subList.get(0).getSubscriptionId())) {
            subKey1 = subList.get(0).getKey();
            assertEquals(subscriptionId2, subList.get(1).getSubscriptionId());
        } else if (subscriptionId1.equals(subList.get(1).getSubscriptionId())) {
            subKey1 = subList.get(1).getKey();
            assertEquals(subscriptionId2, subList.get(0).getSubscriptionId());
        } else {
            subKey1 = 0;
            fail("Wrong subscription Id's");
        }
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                assertEquals(subscriptionId1, subMgmtLocal
                        .loadSubscription(subKey1).getSubscriptionId());
                return null;
            }
        });
    }

    private VOService getProductToSubscribe(final long key) throws Exception {
        return runTX(new Callable<VOService>() {

            @Override
            public VOService call() throws Exception {
                Product product = mgr.getReference(Product.class, key);
                return ProductAssembler.toVOProduct(product,
                        new LocalizerFacade(localizer, "en"));
            }
        });
    }

    private void createAvailablePayment(final Organization org)
            throws Exception {
        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                OrganizationRole role = new OrganizationRole();
                role.setRoleName(OrganizationRoleType.CUSTOMER);
                role = (OrganizationRole) mgr.getReferenceByBusinessKey(role);

                for (PaymentType type : paymentTypes) {
                    OrganizationRefToPaymentType apt = new OrganizationRefToPaymentType();
                    apt.setOrganizationReference(org.getSources().get(0));
                    apt.setPaymentType(type);
                    apt.setOrganizationRole(role);
                    apt.setUsedAsDefault(false);
                    mgr.persist(apt);
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
                            .createVOPaymentInfo(organization, mgr,
                                    paymentTypes.get(0));
                    voPaymentInfos.put(supplierId, newPaymentInfo);
                    return null;
                }
            });
        }
        return voPaymentInfos.get(supplierId);
    }

    /**
     * @param organization
     * @return
     */
    private VOBillingContact createBillingContact(
            final Organization organization) throws Exception {
        return runTX(new Callable<VOBillingContact>() {

            @Override
            public VOBillingContact call() throws Exception {
                return PaymentInfos.createBillingContact(organization, mgr);
            }
        });
    }

}
