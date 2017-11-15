/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.subscriptionservice.bean;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Callable;

import javax.ejb.EJBException;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.junit.Assert;
import org.junit.Test;
import org.oscm.accountservice.bean.MarketingPermissionServiceBean;
import org.oscm.applicationservice.bean.ApplicationServiceStub;
import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.converter.ParameterizedTypes;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.*;
import org.oscm.domobjects.enums.BillingAdapterIdentifier;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.domobjects.enums.ModificationType;
import org.oscm.domobjects.enums.OrganizationReferenceType;
import org.oscm.encrypter.AESEncrypter;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.i18nservice.bean.LocalizerServiceStub2;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.identityservice.assembler.UserDataAssembler;
import org.oscm.identityservice.bean.IdManagementStub;
import org.oscm.interceptor.DateFactory;
import org.oscm.internal.intf.IdentityService;
import org.oscm.internal.intf.MarketplaceService;
import org.oscm.internal.intf.ServiceProvisioningService;
import org.oscm.internal.intf.SubscriptionService;
import org.oscm.internal.types.enumtypes.*;
import org.oscm.internal.types.exception.*;
import org.oscm.internal.types.exception.ConcurrentModificationException;
import org.oscm.internal.vo.*;
import org.oscm.marketplace.bean.LandingpageServiceBeanLocal;
import org.oscm.marketplace.bean.MarketplaceServiceBean;
import org.oscm.marketplace.bean.MarketplaceServiceLocalBean;
import org.oscm.serviceprovisioningservice.assembler.ProductAssembler;
import org.oscm.serviceprovisioningservice.assembler.RoleAssembler;
import org.oscm.serviceprovisioningservice.bean.ServiceProvisioningServiceBean;
import org.oscm.serviceprovisioningservice.bean.ServiceProvisioningServiceLocalizationBean;
import org.oscm.serviceprovisioningservice.bean.TagServiceBean;
import org.oscm.sessionservice.bean.SessionManagementStub2;
import org.oscm.subscriptionservice.assembler.SubscriptionAssembler;
import org.oscm.subscriptionservice.dao.SubscriptionHistoryDao;
import org.oscm.subscriptionservice.dao.UsageLicenseDao;
import org.oscm.subscriptionservice.local.SubscriptionServiceLocal;
import org.oscm.taskhandling.local.TaskMessage;
import org.oscm.taskhandling.local.TaskQueueServiceLocal;
import org.oscm.taskhandling.operations.NotifyProvisioningServiceHandler;
import org.oscm.taskhandling.operations.SendMailHandler;
import org.oscm.taskhandling.payloads.NotifyProvisioningServicePayload;
import org.oscm.taskhandling.payloads.SendMailPayload;
import org.oscm.tenantprovisioningservice.bean.TenantProvisioningServiceBean;
import org.oscm.test.BaseAdmUmTest;
import org.oscm.test.EJBTestBase;
import org.oscm.test.ReflectiveClone;
import org.oscm.test.data.*;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.*;
import org.oscm.triggerservice.local.TriggerMessage;
import org.oscm.triggerservice.local.TriggerProcessMessageData;
import org.oscm.types.constants.Configuration;
import org.oscm.types.enumtypes.EmailType;
import org.oscm.types.enumtypes.PlatformParameterIdentifiers;
import org.oscm.types.enumtypes.ProvisioningType;
import org.oscm.types.enumtypes.TriggerProcessParameterName;
import org.oscm.types.exceptions.UserAlreadyAssignedException;
import org.oscm.types.exceptions.UserNotAssignedException;

public class SubscriptionServiceBeanIT extends EJBTestBase {

    private static final String LOGIN_PATH = "/login.jsp";
    private static final String BASE_URL_SERVICE_HTTP = "http://localhost:8080/example-service";
    private static final String BASE_URL_SERVICE_HTTPS = "https://localhost:8181/example-service";

    private static final String BASE_URL_BES_HTTP = "http://localhost:8080/oscm-portal";
    private static final String BASE_URL_BES_HTTPS = "https://localhost:8181/oscm-portal";
    private static final String TOO_LONG_URL = "http://localhost:8080/oscm-portal/"
            + "organizationorganizationorganizationorganizationorganizationorganizationorganizationorganizationorganization"
            + "organizationorganizationorganizationorganizationorganizationorganizationorganizationorganizationorganization"
            + "/payment.jsf";

    private static String UNIT1 = "Unit1";
    private static String UNIT2 = "Unit2";
    private static long UNIT_NON_EXISTING = 999999999999999999L;
    private static String UNIT_OTHER_ORG = "UnitFromOtherOrg";
    private static final String parameterValueConstant = "2000";
    private static final String SUBSCRIPTION_ID = "subId";
    protected DataService mgr;
    protected ApplicationServiceStub appMgmtStub;
    protected SubscriptionService subMgmt;
    protected SubscriptionServiceLocal subMgmtLocal;
    protected IdentityService idMgmt;
    protected ServiceProvisioningService servProv;
    protected LocalizerServiceLocal localizer;
    private MarketplaceService mpSvc;
    protected TaskQueueServiceLocal tqs;
    private List<PaymentType> paymentTypes;
    private final Map<String, VOPaymentInfo> voPaymentInfos = new HashMap<>();

    private final List<Product> testProducts = new ArrayList<>();
    private final List<Product> asyncTestProducts = new ArrayList<>();
    private final List<Organization> testOrganizations = new ArrayList<>();
    private final Map<Organization, List<PlatformUser>> testUsers = new HashMap<>();

    private PlatformUser supplierUser;

    private List<TaskMessage> messagesOfTaskQueue = new ArrayList<>();

    private boolean isTriggerQueueService_sendSuspendingMessageCalled = false;
    private boolean isTriggerQueueService_sendAllNonSuspendingMessageCalled = false;
    private boolean isSendAllMessagesCalled = false;
    private List<TriggerType> usedTriggersTypes;
    private TriggerDefinition td;
    private Organization org;
    private Organization tpAndSupplier;
    private String supplierOrgId;

    private String customerUserKey;

    private boolean isCorrectSubscriptionIdForMail = false;

    private EmailType mailType = null;
    private Object[] receivedParams = null;

    private List<SendMailPayload> receivedSendMailPayload = new ArrayList<>();
    private org.oscm.domobjects.Marketplace mp;

    @Override
    public void setup(TestContainer container) throws Exception {
        AESEncrypter.generateKey();
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
        container.addBean(new CommunicationServiceStub() {
            @Override
            public void sendMail(PlatformUser recipient, EmailType type,
                    Object[] params,
                    org.oscm.domobjects.Marketplace marketplace) {

                isCorrectSubscriptionIdForMail = params[0]
                        .equals(SUBSCRIPTION_ID);

                mailType = type;
                receivedParams = params;
            }
        });
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
        container.addBean(new UsageLicenseDao(mgr));
        container.addBean(new ConfigurationServiceStub());
        container.addBean(new ImageResourceServiceStub() {

            @Override
            public ImageResource read(long objectKey, ImageType imageType) {
                return null;
            }

        });
        container.addBean(new TaskQueueServiceStub() {

            @Override
            public void sendAllMessages(List<TaskMessage> messages) {
                messagesOfTaskQueue.addAll(messages);

                for (TaskMessage message : messages) {
                    if (message
                            .getHandlerClass() == NotifyProvisioningServiceHandler.class) {
                        NotifyProvisioningServicePayload payload = (NotifyProvisioningServicePayload) message
                                .getPayload();
                        try {
                            Subscription sub = mgr.getReference(
                                    Subscription.class, payload.getTkey());
                            if (payload.isDeactivate()) {
                                appMgmtStub.deactivateInstance(sub);
                            }
                            appMgmtStub.createUsers(sub,
                                    sub.getUsageLicenses());

                            SendMailPayload sendMailPayload = new SendMailPayload();
                            for (UsageLicense usageLicense : sub
                                    .getUsageLicenses()) {

                                String accessInfo = getAccessInfoMock(sub);
                                sendMailPayload.addMailObjectForUser(
                                        usageLicense.getUser().getKey(),
                                        EmailType.SUBSCRIPTION_USER_ADDED,
                                        new Object[] { sub.getSubscriptionId(),
                                                accessInfo },
                                        Long.valueOf(1L));
                            }

                            TaskMessage sendMailMessage = new TaskMessage(
                                    SendMailHandler.class, sendMailPayload);
                            this.sendAllMessages(
                                    Arrays.asList(sendMailMessage));
                            receivedSendMailPayload.add(sendMailPayload);
                            isSendAllMessagesCalled = true;

                        } catch (ObjectNotFoundException
                                | TechnicalServiceNotAliveException
                                | TechnicalServiceOperationException e) {
                            e.printStackTrace();
                        }
                    }
                    if (message.getHandlerClass() == SendMailHandler.class) {
                        SendMailPayload payload = (SendMailPayload) message
                                .getPayload();
                        receivedSendMailPayload.add(payload);
                        isSendAllMessagesCalled = true;
                    }
                }
            }
        });
        container.addBean(new TriggerQueueServiceStub() {
            @Override
            public List<TriggerProcessMessageData> sendSuspendingMessages(
                    List<TriggerMessage> messageData) {
                for (TriggerMessage msg : messageData) {
                    usedTriggersTypes.add(msg.getTriggerType());
                }
                isTriggerQueueService_sendSuspendingMessageCalled = true;
                TriggerProcess tp = new TriggerProcess();
                tp.setTriggerDefinition(td);
                tp.setUser(testUsers.get(testOrganizations.get(0)).get(0));
                TriggerProcessMessageData data = new TriggerProcessMessageData(
                        tp, null);
                return Collections.singletonList(data);
            }

            @Override
            public void sendAllNonSuspendingMessages(
                    List<TriggerMessage> messageData) {
                for (TriggerMessage msg : messageData) {
                    usedTriggersTypes.add(msg.getTriggerType());
                }
                isTriggerQueueService_sendAllNonSuspendingMessageCalled = true;
            }
        });
        container.addBean(new TagServiceBean());
        container.addBean(new MarketingPermissionServiceBean());
        container.addBean(new MarketplaceServiceStub());
        container.addBean(new LandingpageServiceBeanLocal());
        container.addBean(new ServiceProvisioningServiceLocalizationBean());
        container.addBean(new ServiceProvisioningServiceBean());
        container.addBean(new AccountServiceStub() {
            @Override
            public boolean isPaymentTypeEnabled(long serviceKey,
                    long paymentTypeKey) {
                return true;
            }
        });
        container.addBean(new CategorizationServiceStub() {
            @Override
            public boolean updateAssignedCategories(CatalogEntry catalogEntry,
                    List<VOCategory> categories) {
                return false;
            }
        });
        container.addBean(new MarketplaceServiceLocalBean());
        container.addBean(new MarketplaceServiceBean());
        container.addBean(new SubscriptionListServiceBean());
        container.addBean(new SubscriptionServiceBean());
        container.addBean(new TerminateSubscriptionBean());
        container.addBean(new ManageSubscriptionBean());
        container.addBean(new ValidateSubscriptionStateBean());
        container.addBean(new ModifyAndUpgradeSubscriptionBean());
        container.addBean(new UsageLicenseDao(mgr));
        mgr = container.get(DataService.class);

        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                paymentTypes = createPaymentTypes(mgr);
                createOrganizationRoles(mgr);
                SupportedCountries.createSomeSupportedCountries(mgr);
                Organization operator = Organizations.createOrganization(mgr,
                        OrganizationRoleType.PLATFORM_OPERATOR);
                operator.setOrganizationId(
                        OrganizationRoleType.PLATFORM_OPERATOR.name());
                Marketplaces.createGlobalMarketplace(operator,
                        GLOBAL_MARKETPLACE_NAME, mgr);
                BillingAdapters.createBillingAdapter(mgr,
                        BillingAdapterIdentifier.NATIVE_BILLING.toString(),
                        true);
                return null;
            }
        });
        Long userKey = runTX(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                return initMasterData();
            }
        });
        customerUserKey = String.valueOf(userKey);
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);

        subMgmt = container.get(SubscriptionService.class);
        subMgmtLocal = container.get(SubscriptionServiceLocal.class);
        idMgmt = container.get(IdentityService.class);
        servProv = container.get(ServiceProvisioningService.class);
        localizer = container.get(LocalizerServiceLocal.class);
        mpSvc = container.get(MarketplaceService.class);
        tqs = container.get(TaskQueueServiceLocal.class);

        ConfigurationServiceLocal cfg = container
                .get(ConfigurationServiceLocal.class);
        setUpDirServerStub(cfg);
        cfg.setConfigurationSetting(
                new ConfigurationSetting(ConfigurationKey.BASE_URL,
                        Configuration.GLOBAL_CONTEXT, BASE_URL_BES_HTTP));
        cfg.setConfigurationSetting(
                new ConfigurationSetting(ConfigurationKey.BASE_URL_HTTPS,
                        Configuration.GLOBAL_CONTEXT, BASE_URL_BES_HTTPS));

        appMgmtStub.resetController();

        // this testcase is only applicable for the platform login
        testProducts.get(0).getTechnicalProduct()
                .setAccessType(ServiceAccessType.LOGIN);

        usedTriggersTypes = new LinkedList<>();
    }

    String getAccessInfoMock(Subscription subscription) {
        String accessInfo = null;
        if (useAccessInfo(subscription)) {
            accessInfo = subscription.getAccessInfo();
        } else {
            accessInfo = BASE_URL_BES_HTTP + "/opt/"
                    + Long.toHexString(subscription.getKey()) + "/";
        }
        if (accessInfo == null) {
            accessInfo = "";
        }
        return accessInfo;
    }

    private boolean useAccessInfo(Subscription subscription) {
        ServiceAccessType accessType = subscription.getProduct()
                .getTechnicalProduct().getAccessType();
        return accessType == ServiceAccessType.DIRECT
                || accessType == ServiceAccessType.USER;
    }

    /**
     * Test for termination subscription by supplier.
     */
    @Test(expected = ObjectNotFoundException.class)
    public void testTerminateSubscriptionSubscriptionNotFound()
            throws Throwable {
        final String reason = "";

        final VOSubscription subscription = new VOSubscription();

        container.login(String.valueOf(supplierUser.getKey()),
                ROLE_SERVICE_MANAGER);

        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                subMgmt.terminateSubscription(subscription, reason);
                return null;
            }
        });
    }

    /**
     * Test for termination subscription by supplier.
     */
    @Test(expected = ObjectNotFoundException.class)
    public void testTerminateSubscriptionSubscriptionNotFoundNullReason()
            throws Throwable {
        final String reason = null;

        final VOSubscription subscription = new VOSubscription();

        container.login(String.valueOf(supplierUser.getKey()),
                ROLE_SERVICE_MANAGER);

        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                subMgmt.terminateSubscription(subscription, reason);
                return null;
            }
        });
    }

    /**
     * Test for termination subscription by supplier.
     */
    @Test(expected = javax.ejb.EJBException.class)
    public void testTerminateSubscriptionOrganizationAuthoritiesException()
            throws Throwable {
        final String reason = "";

        // create subscription
        final String subscriptionId = "testSubscribeToProduct";

        final VOSubscription subscription = runTX(
                new Callable<VOSubscription>() {
                    @Override
                    public VOSubscription call() throws Exception {
                        VOService product = getProductToSubscribe(
                                testProducts.get(0).getKey());
                        VOUser[] users = new VOUser[2];
                        VOUser[] admins = new VOUser[1];
                        setUsers(users, admins);
                        VOSubscription createdSubscr = subMgmt
                                .subscribeToService(
                                        Subscriptions.createVOSubscription(
                                                subscriptionId),
                                        product, getUsersToAdd(admins, users),
                                        null, null, new ArrayList<VOUda>());

                        return createdSubscr;
                    }
                });

        subMgmt.terminateSubscription(subscription, reason);
    }

    /**
     * Test for termination by not authorized user.
     */
    @Test(expected = OrganizationAuthoritiesException.class)
    public void testTerminateSubscription_NotAuthorizedUser() throws Throwable {
        final String reason = "";

        // create subscription
        final String subscriptionId = "testSubscribeToProduct";

        final VOSubscription subscription = runTX(
                new Callable<VOSubscription>() {
                    @Override
                    public VOSubscription call() throws Exception {
                        VOService product = getProductToSubscribe(
                                testProducts.get(0).getKey());
                        VOUser[] users = new VOUser[2];
                        VOUser[] admins = new VOUser[1];
                        setUsers(users, admins);
                        VOSubscription createdSubscr = subMgmt
                                .subscribeToService(
                                        Subscriptions.createVOSubscription(
                                                subscriptionId),
                                        product, getUsersToAdd(admins, users),
                                        null, null, new ArrayList<VOUda>());
                        return createdSubscr;
                    }
                });

        container.login(customerUserKey, ROLE_SERVICE_MANAGER);
        subMgmt.terminateSubscription(subscription, reason);
    }

    /**
     * Test for termination subscription by supplier.
     */
    @Test
    public void testTerminateSubscription() throws Throwable {
        final String reason = "";

        // create subscription
        final String subscriptionId = "testSubscribeToProduct";

        UserGroup unit1 = getUnit(UNIT1, testOrganizations.get(0));
        final VOSubscription newSub = Subscriptions
                .createVOSubscription(subscriptionId);
        newSub.setUnitKey(unit1.getKey());

        final VOSubscription subscription = runTX(
                new Callable<VOSubscription>() {
                    @Override
                    public VOSubscription call() throws Exception {
                        VOService product = getProductToSubscribe(
                                testProducts.get(0).getKey());
                        VOUser[] users = new VOUser[2];
                        VOUser[] admins = new VOUser[1];
                        setUsers(users, admins);
                        VOSubscription createdSubscr = subMgmt
                                .subscribeToService(newSub, product,
                                        getUsersToAdd(admins, users), null,
                                        null, new ArrayList<VOUda>());

                        return createdSubscr;
                    }
                });

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Session session = new Session();
                session.setNodeName("nodeName");
                session.setPlatformUserId("platformUserId");
                session.setSessionId("1");
                session.setPlatformUserKey(testUsers
                        .get(testOrganizations.get(0)).get(1).getKey());
                session.setSessionType(SessionType.SERVICE_SESSION);
                session.setSubscriptionTKey(
                        Long.valueOf(subscription.getKey()));
                mgr.persist(session);
                mgr.flush();

                return null;
            }
        });

        // login as supplier
        container.login(String.valueOf(supplierUser.getKey()),
                ROLE_SERVICE_MANAGER);
        VOSubscriptionDetails sub = subMgmt.getSubscriptionForCustomer(
                testOrganizations.get(0).getOrganizationId(), subscriptionId);
        subMgmt.terminateSubscription(sub, reason);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                // check subscription status
                Subscription subscriptionAfter = mgr.getReference(
                        Subscription.class, subscription.getKey());
                assertEquals("Check subscription status",
                        subscriptionAfter.getStatus(),
                        SubscriptionStatus.DEACTIVATED);
                assertNull("Unit reference must be deleted",
                        subscriptionAfter.getUserGroup());

                // check no more user license
                List<UsageLicense> licenses = subscriptionAfter
                        .getUsageLicenses();
                assertEquals("Check number of usage license", 0,
                        licenses.size());

                // check product is deleted
                Product product = subscriptionAfter.getProduct();
                assertEquals("Check service status", ServiceStatus.DELETED,
                        product.getStatus());

                // check no active sessions
                Query query = mgr.createQuery("SELECT s FROM Session s");
                List<Session> activeSessions = new ArrayList<>();
                for (Session ses : ParameterizedTypes
                        .iterable(query.getResultList(), Session.class)) {
                    activeSessions.add(ses);
                }
                assertEquals("Check number of sessions", 0,
                        activeSessions.size());

                return null;
            }
        });

    }

    @Test(expected = ConcurrentModificationException.class)
    public void testTerminateSubscription_Concurrent() throws Throwable {
        final String reason = "";

        // create subscription
        final String subscriptionId = "testConcurrent";

        final VOSubscription subscription = runTX(
                new Callable<VOSubscription>() {
                    @Override
                    public VOSubscription call() throws Exception {
                        VOService product = getProductToSubscribe(
                                testProducts.get(0).getKey());
                        VOUser[] users = new VOUser[2];
                        VOUser[] admins = new VOUser[1];
                        setUsers(users, admins);
                        VOSubscription createdSubscr = subMgmt
                                .subscribeToService(
                                        Subscriptions.createVOSubscription(
                                                subscriptionId),
                                        product, getUsersToAdd(admins, users),
                                        null, null, new ArrayList<VOUda>());

                        return createdSubscr;
                    }
                });

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Subscription sub = mgr.getReference(Subscription.class,
                        subscription.getKey());
                sub.setSubscriptionId("renamed");
                return null;
            }
        });

        // login as supplier
        container.login(String.valueOf(supplierUser.getKey()),
                ROLE_SERVICE_MANAGER);
        subMgmt.terminateSubscription(subscription, reason);
    }

    /**
     * Test for getting subscription details for supplier. Exception is
     * expected. No such subscription.
     */
    @Test(expected = ObjectNotFoundException.class)
    public void testGetSubscriptionForCustomerObjectNotFound()
            throws Throwable {
        container.login(supplierUser.getKey(), ROLE_SERVICE_MANAGER);
        String organizationId = "";
        String subscriptionId = "";

        subMgmt.getSubscriptionForCustomer(organizationId, subscriptionId);
    }

    /**
     * Test for getting subscription details for supplier. Exception is expected
     * - user which call this method is not a supplier of the subscription
     * product.
     */
    @Test(expected = OperationNotPermittedException.class)
    public void testGetSubscriptionForCustomerOperationIsNotPermitted()
            throws Throwable {
        String subscriptionId = "testSubscribeToProduct";

        VOService product = getProductToSubscribe(testProducts.get(0).getKey());
        VOUser[] users = new VOUser[2];
        VOUser[] admins = new VOUser[1];
        setUsers(users, admins);
        subMgmt.subscribeToService(
                Subscriptions.createVOSubscription(subscriptionId), product,
                getUsersToAdd(admins, users), null, null,
                new ArrayList<VOUda>());

        String customerId = runTX(new Callable<String>() {
            @Override
            public String call() {
                return mgr.getCurrentUser().getOrganization()
                        .getOrganizationId();
            }
        });

        container.login(customerUserKey, ROLE_SERVICE_MANAGER);
        subMgmt.getSubscriptionForCustomer(customerId, subscriptionId);
    }

    /**
     * Test for getting subscription details for supplier.
     */
    @Test
    public void testGetSubscriptionForCustomer() throws Throwable {
        String subscriptionId = "testSubscribeToProduct";

        VOService product = getProductToSubscribe(testProducts.get(0).getKey());
        String productId = product.getServiceId();

        VOUser[] users = new VOUser[2];
        VOUser[] admins = new VOUser[1];
        setUsers(users, admins);

        subMgmt.subscribeToService(
                Subscriptions.createVOSubscription(subscriptionId), product,
                getUsersToAdd(admins, users), null, null,
                new ArrayList<VOUda>());

        String customerId = runTX(new Callable<String>() {
            @Override
            public String call() {
                return mgr.getCurrentUser().getOrganization()
                        .getOrganizationId();
            }
        });

        PlatformUser oldUser = runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() {
                return mgr.getCurrentUser();
            }
        });

        // login as new user SUPPLIER
        container.login(String.valueOf(supplierUser.getKey()),
                ROLE_RESELLER_MANAGER);

        VOSubscriptionDetails voSubscriptionDetails = subMgmt
                .getSubscriptionForCustomer(customerId, subscriptionId);

        assertEquals(subscriptionId, voSubscriptionDetails.getSubscriptionId());
        assertEquals(productId, voSubscriptionDetails.getServiceId());

        // login as old user
        container.login(String.valueOf(oldUser.getKey()),
                ROLE_ORGANIZATION_ADMIN);
    }

    @Test
    public void testSubscribeToProduct() throws Throwable {
        assertNotNull(subMgmt);
        VOService product = getProductToSubscribe(testProducts.get(0).getKey());
        VOUser[] users = new VOUser[2];
        VOUser[] admins = new VOUser[1];
        setUsers(users, admins);
        VOSubscription sub = Subscriptions
                .createVOSubscription("testSubscribeToProduct");
        sub.setPurchaseOrderNumber(TOO_LONG_NAME);
        messagesOfTaskQueue = new ArrayList<>();
        VOSubscription newSub = subMgmt.subscribeToService(sub, product,
                getUsersToAdd(admins, users), null, null,
                new ArrayList<VOUda>());

        // Now check results
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() {
                    checkSubscribeToProduct(false, "testSubscribeToProduct",
                            testProducts.get(0), SubscriptionStatus.ACTIVE, 3,
                            TOO_LONG_NAME, 1);
                    return null;
                }
            });
        } catch (Exception e) {
            if (e.getCause() != null) {
                throw e.getCause();
            }
            throw e;
        }
        assertNotNull(newSub);
        assertTrue(newSub.getKey() > 0);
        assertNull(newSub.getUnitName());
        assertTrue(isTriggerQueueService_sendSuspendingMessageCalled);
        assertTrue(isMessageSend(TriggerType.SUBSCRIBE_TO_SERVICE));
        assertTrue(isMessageSend(TriggerType.SUBSCRIPTION_CREATION));

        assertEquals(2, messagesOfTaskQueue.size());
        assertEquals(2, receivedSendMailPayload.size());
        assertEquals(3, receivedSendMailPayload.get(0).getMailObjects().size());
        assertEquals(1, receivedSendMailPayload.get(1).getMailObjects().size());
        // Check if the URL in the mail contains the correct base URL
        assertTrue(((String) receivedSendMailPayload.get(0).getMailObjects()
                .get(0).getParams()[1]).startsWith(BASE_URL_BES_HTTP));
    }

    @Test
    public void testSubscribeToProductUnitAssign() throws Throwable {
        assertNotNull(subMgmt);

        Organization customerOrg = runTX(new Callable<Organization>() {
            @Override
            public Organization call() {
                return testOrganizations.get(0);
            }
        });

        UserGroup unit1 = getUnit(UNIT1, customerOrg);

        VOService product = getProductToSubscribe(testProducts.get(0).getKey());
        VOUser[] admins = new VOUser[1];
        admins[0] = UserDataAssembler
                .toVOUser(testUsers.get(testOrganizations.get(0)).get(1));
        VOSubscription sub = Subscriptions
                .createVOSubscription("testSubscribeToProduct");
        sub.setPurchaseOrderNumber(TOO_LONG_NAME);
        sub.setUnitKey(unit1.getKey());

        VOSubscription newSub = subMgmt.subscribeToService(sub, product,
                getUsersToAdd(admins, null), null, null,
                new ArrayList<VOUda>());

        // Now check results
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() {
                    checkSubscribeToProduct(false, "testSubscribeToProduct",
                            testProducts.get(0), SubscriptionStatus.ACTIVE, 1,
                            TOO_LONG_NAME, 1);
                    return null;
                }
            });
        } catch (Exception e) {
            if (e.getCause() != null) {
                throw e.getCause();
            }
            throw e;
        }
        assertNotNull(newSub);
        assertTrue(newSub.getKey() > 0);
        assertEquals(unit1.getKey(), newSub.getUnitKey());
    }

    @Test
    public void testSubscribeToProductUnitAssignSubManager() throws Throwable {
        assertNotNull(subMgmt);

        Organization customerOrg = runTX(new Callable<Organization>() {
            @Override
            public Organization call() {
                return testOrganizations.get(0);
            }
        });

        UserGroup unit1 = getUnit(UNIT1, customerOrg);

        VOService product = getProductToSubscribe(testProducts.get(0).getKey());
        VOUser[] admins = new VOUser[1];
        admins[0] = UserDataAssembler
                .toVOUser(testUsers.get(testOrganizations.get(0)).get(1));
        VOSubscription sub = Subscriptions
                .createVOSubscription("testSubscribeToProduct");
        sub.setPurchaseOrderNumber(TOO_LONG_NAME);
        sub.setUnitKey(unit1.getKey());

        container.login(testUsers.get(testOrganizations.get(0)).get(2).getKey(),
                ROLE_SUBSCRIPTION_MANAGER);

        VOSubscription newSub = subMgmt.subscribeToService(sub, product,
                getUsersToAdd(admins, null), null, null,
                new ArrayList<VOUda>());

        // Now check results
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() {
                    checkSubscribeToProduct(false, "testSubscribeToProduct",
                            testProducts.get(0), SubscriptionStatus.ACTIVE, 1,
                            TOO_LONG_NAME, 1);
                    return null;
                }
            });
        } catch (Exception e) {
            if (e.getCause() != null) {
                throw e.getCause();
            }
            throw e;
        }
        assertNotNull(newSub);
        assertTrue(newSub.getKey() > 0);
        assertEquals(0L, newSub.getUnitKey());
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testSubscribeToProductUnitAssignNonExisting() throws Throwable {
        assertNotNull(subMgmt);
        VOService product = getProductToSubscribe(testProducts.get(0).getKey());
        VOUser[] admins = new VOUser[1];
        admins[0] = UserDataAssembler
                .toVOUser(testUsers.get(testOrganizations.get(0)).get(1));
        VOSubscription sub = Subscriptions
                .createVOSubscription("testSubscribeToProduct");
        sub.setPurchaseOrderNumber(TOO_LONG_NAME);
        sub.setUnitKey(UNIT_NON_EXISTING);
        subMgmt.subscribeToService(sub, product, getUsersToAdd(admins, null),
                null, null, new ArrayList<VOUda>());
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testSubscribeToProductUnitAssignOtherOrg() throws Throwable {
        assertNotNull(subMgmt);

        UserGroup unitOtherOrg = getUnit(UNIT_OTHER_ORG, tpAndSupplier);

        VOService product = getProductToSubscribe(testProducts.get(0).getKey());
        VOUser[] admins = new VOUser[1];
        admins[0] = UserDataAssembler
                .toVOUser(testUsers.get(testOrganizations.get(0)).get(1));
        VOSubscription sub = Subscriptions
                .createVOSubscription("testSubscribeToProduct");
        sub.setPurchaseOrderNumber(TOO_LONG_NAME);

        sub.setUnitKey(unitOtherOrg.getKey());
        subMgmt.subscribeToService(sub, product, getUsersToAdd(admins, null),
                null, null, new ArrayList<VOUda>());
    }

    @Test
    public void testSubscribeToProduct_NewCustomerCopyEnabledPaymentTypes()
            throws Throwable {
        VOService product = getProductToSubscribe(testProducts.get(1).getKey());
        VOUser[] users = new VOUser[2];
        VOUser[] admins = new VOUser[1];
        setUsers(users, admins);
        final VOPaymentInfo voPaymentInfo = getPaymentInfo(
                tpAndSupplier.getOrganizationId(), testOrganizations.get(0));

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                // remove supplier-customer-reference
                Organization sup = mgr.getReference(Organization.class,
                        tpAndSupplier.getKey());
                List<OrganizationReference> targets = sup.getTargets();
                for (OrganizationReference ref : targets) {
                    if (ref.getTargetKey() == testOrganizations.get(0).getKey()
                            && ref.getReferenceType() == OrganizationReferenceType.SUPPLIER_TO_CUSTOMER) {
                        mgr.remove(ref);
                        break;
                    }
                }
                // create reference between operator and supplier
                Organization po = new Organization();
                po.setOrganizationId(
                        OrganizationRoleType.PLATFORM_OPERATOR.name());
                po = Organization.class.cast(mgr.getReferenceByBusinessKey(po));
                OrganizationReference ref = new OrganizationReference(po, sup,
                        OrganizationReferenceType.PLATFORM_OPERATOR_TO_SUPPLIER);
                // create default payment
                PaymentType pt = mgr.getReference(PaymentType.class,
                        paymentTypes.get(0).getKey());
                mgr.persist(ref);
                OrganizationRefToPaymentType o = new OrganizationRefToPaymentType();
                o.setOrganizationReference(ref);
                OrganizationRole role = new OrganizationRole(
                        OrganizationRoleType.SUPPLIER);
                role = OrganizationRole.class
                        .cast(mgr.getReferenceByBusinessKey(role));
                o.setOrganizationRole(role);
                o.setPaymentType(pt);
                o.setUsedAsDefault(true);
                mgr.persist(o);
                ref.getPaymentTypes().add(o);
                return null;
            }
        });
        VOBillingContact bc = createBillingContact(testOrganizations.get(0));

        VOSubscription newSub = subMgmt.subscribeToService(
                Subscriptions.createVOSubscription("testSubscribeToProduct"),
                product, getUsersToAdd(admins, users), voPaymentInfo, bc,
                new ArrayList<VOUda>());
        // Now check results
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    checkSubscribeToProduct(false, "testSubscribeToProduct",
                            testProducts.get(1), SubscriptionStatus.ACTIVE, 3,
                            null, 1);

                    // check that default payment types have been copied and
                    // relation exists
                    Organization cust = mgr.getReference(Organization.class,
                            testOrganizations.get(0).getKey());
                    List<OrganizationRefToPaymentType> types = cust
                            .getPaymentTypes(false,
                                    OrganizationRoleType.CUSTOMER,
                                    tpAndSupplier.getOrganizationId());
                    assertNotNull(types);
                    assertEquals(1, types.size());
                    assertEquals(paymentTypes.get(0).getPaymentTypeId(),
                            types.get(0).getPaymentType().getPaymentTypeId());
                    return null;
                }
            });
        } catch (Exception e) {
            if (e.getCause() != null) {
                throw e.getCause();
            }
            throw e;
        }
        assertNotNull(newSub);
        assertTrue(newSub.getKey() > 0);
        assertTrue(isTriggerQueueService_sendSuspendingMessageCalled);
        assertTrue(isMessageSend(TriggerType.SUBSCRIBE_TO_SERVICE));
    }

    @Test
    public void testSubscribeToProductidWithBlanks() throws Throwable {
        assertNotNull(subMgmt);
        VOService product = getProductToSubscribe(testProducts.get(0).getKey());
        VOUser[] users = new VOUser[2];
        VOUser[] admins = new VOUser[1];
        setUsers(users, admins);
        VOSubscription newSub = subMgmt.subscribeToService(
                Subscriptions.createVOSubscription("testSubscribeTo Product"),
                product, getUsersToAdd(admins, users), null, null,
                new ArrayList<VOUda>());

        // Now check results
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() {
                    checkSubscribeToProduct(false, "testSubscribeTo Product",
                            testProducts.get(0), SubscriptionStatus.ACTIVE, 3,
                            null, 1);
                    return null;
                }
            });
        } catch (Exception e) {
            if (e.getCause() != null) {
                throw e.getCause();
            }
            throw e;
        }
        assertNotNull(newSub);
        assertTrue(newSub.getKey() > 0);
        assertTrue(isTriggerQueueService_sendSuspendingMessageCalled);
        assertTrue(isMessageSend(TriggerType.SUBSCRIBE_TO_SERVICE));
    }

    @Test
    public void testSubscribeAgainToProduct() throws Throwable {
        assertNotNull(subMgmt);
        VOService product = getProductToSubscribe(testProducts.get(0).getKey());
        VOUser[] users = new VOUser[2];
        VOUser[] admins = new VOUser[1];
        setUsers(users, admins);
        VOSubscription newSub = subMgmt.subscribeToService(
                Subscriptions.createVOSubscription("testSubscribeTo Product"),
                product, getUsersToAdd(admins, users), null, null,
                new ArrayList<VOUda>());

        VOSubscription newSub2 = subMgmt.subscribeToService(
                Subscriptions.createVOSubscription("testSubscribeTo Product2"),
                product, getUsersToAdd(admins, users), null, null,
                new ArrayList<VOUda>());

        // Now check results
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() {
                    checkSubscribeToProduct(false, "testSubscribeTo Product",
                            testProducts.get(0), SubscriptionStatus.ACTIVE, 3,
                            null, 1);
                    checkSubscribeToProduct(false, "testSubscribeTo Product2",
                            testProducts.get(0), SubscriptionStatus.ACTIVE, 3,
                            null, 1);
                    return null;
                }
            });
        } catch (Exception e) {
            if (e.getCause() != null) {
                throw e.getCause();
            }
            throw e;
        }
        assertNotNull(newSub);
        assertTrue(newSub.getKey() > 0);
        assertNotNull(newSub2);
        assertTrue(newSub2.getKey() > 0);
        assertTrue(isTriggerQueueService_sendSuspendingMessageCalled);
        assertTrue(isMessageSend(TriggerType.SUBSCRIBE_TO_SERVICE));
    }

    @Test(expected = PaymentInformationException.class)
    public void testSubscribeToProductChargeableNoPaymentTypeEnabled()
            throws Throwable {
        assertNotNull(subMgmt);
        VOService product = getProductToSubscribe(testProducts.get(1).getKey());
        VOUser[] users = new VOUser[2];
        VOUser[] admins = new VOUser[1];
        setUsers(users, admins);
        final VOPaymentInfo voPaymentInfo = getPaymentInfo(
                tpAndSupplier.getOrganizationId(), testOrganizations.get(0));

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                PaymentInfo pi = mgr.getReference(PaymentInfo.class,
                        voPaymentInfo.getKey());

                // Change the pt of the pi
                PaymentType pt = mgr.getReference(PaymentType.class,
                        paymentTypes.get(1).getKey());
                pi.setPaymentType(pt);
                return null;
            }
        });
        VOBillingContact bc = createBillingContact(testOrganizations.get(0));
        // skip the fixed validation of bug 10503
        voPaymentInfo.setVersion(1);

        subMgmt.subscribeToService(
                Subscriptions.createVOSubscription("testSubscribeToProduct"),
                product, getUsersToAdd(admins, users), voPaymentInfo, bc,
                new ArrayList<VOUda>());
    }

    @Test(expected = PaymentInformationException.class)
    public void testSubscribeToProductChargeableNoPaymentTypeForProduct()
            throws Throwable {
        assertNotNull(subMgmt);
        VOService product = getProductToSubscribe(testProducts.get(1).getKey());
        VOUser[] users = new VOUser[2];
        VOUser[] admins = new VOUser[1];
        setUsers(users, admins);
        final VOPaymentInfo voPaymentInfo = getPaymentInfo(
                tpAndSupplier.getOrganizationId(), testOrganizations.get(0));

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Product product = asyncTestProducts.get(1);
                product = mgr.getReference(Product.class, product.getKey());
                for (ProductToPaymentType ptpt : product.getPaymentTypes()) {
                    mgr.remove(ptpt);
                }
                return null;
            }
        });
        VOBillingContact bc = createBillingContact(testOrganizations.get(0));

        subMgmt.subscribeToService(
                Subscriptions.createVOSubscription("testSubscribeToProduct"),
                product, getUsersToAdd(admins, users), voPaymentInfo, bc,
                new ArrayList<VOUda>());
    }

    private void assertProvisioningCompleted(Subscription subscription) {

        boolean isCompleted = subscription.getPriceModel()
                .isProvisioningCompleted();
        assertTrue("Unexpected provisioning status for subscription pricemodel",
                isCompleted);

    }

    private void assertProvisioningNotCompleted(Subscription subscription) {
        boolean isCompleted = subscription.getPriceModel()
                .isProvisioningCompleted();
        assertFalse(
                "Unexpected provisioning status for subscription pricemodel",
                isCompleted);

    }

    private void assertProvisioningNotCompleted(String id, long orgKey)
            throws Exception {
        Subscription sub = loadSubscription(id, orgKey);
        assertProvisioningNotCompleted(sub);
    }

    /**
     * Checks several subscription attributes.
     * 
     * @param checkNotActive
     *            If <code>true</code> check that product instance id and
     *            activation date are not set and added users not provided to
     *            the application. Else check if these values are correctly set.
     * @param subscriptionId
     *            The id of the subscription to check.
     * @param product
     *            The product to compare.
     * @param status
     *            The expected status.
     * @param usersAdded
     *            Number of users added to the subscription.
     * @param pon
     *            optional - if null, the subscription id will be used as PON
     * @param userStartIndex
     *            TODO
     */
    private void checkSubscribeToProduct(boolean checkNotActive,
            String subscriptionId, Product product, SubscriptionStatus status,
            int usersAdded, String pon, int userStartIndex) {
        if (pon == null) {
            pon = subscriptionId;
        }
        // load generated subscription object
        Organization theOrganization = testOrganizations.get(0);
        Subscription subscription = null;
        if (checkNotActive) {
            subscription = findInvalidSubscription(subscriptionId,
                    theOrganization);
        } else {
            Subscription qryObj = new Subscription();
            qryObj.setOrganizationKey(theOrganization.getKey());
            qryObj.setSubscriptionId(subscriptionId);
            subscription = (Subscription) mgr.find(qryObj);
        }

        assertNotNull("Could not load subscription 'testSubscribeToProduct'",
                subscription);
        // check subscription attributes
        assertEquals(
                new SimpleDateFormat("yyyy-MM-dd")
                        .format(GregorianCalendar.getInstance().getTime()),
                new SimpleDateFormat("yyyy-MM-dd")
                        .format(subscription.getCreationDate()));
        assertEquals(pon, subscription.getPurchaseOrderNumber());
        // check product
        assertNotNull("No product assigned to subscription",
                subscription.getProduct());
        assertTrue("Wrong ProductId in subscription", subscription.getProduct()
                .getProductId().startsWith(product.getProductId() + "#"));
        // check pricemodel of subscription
        assertNotNull("No priceModel assigned to subscription",
                subscription.getPriceModel());

        // pricemodel should be the same as the one assigned to the product
        assertEquals(
                "PriceModel of subscription is not the same instance as for the product",
                product.getPriceModel().getPricePerPeriod(),
                subscription.getPriceModel().getPricePerPeriod());
        assertEquals(
                "PriceModel of subscription is not the same instance as for the product",
                product.getPriceModel().getPricePerUserAssignment(),
                subscription.getPriceModel().getPricePerUserAssignment());
        // Check Users/UsageLicenses
        // should contain 3 entries

        assertEquals("Wrong number of UsageLicenses", usersAdded,
                subscription.getUsageLicenses().size());
        // sort by Key
        Collections.sort(subscription.getUsageLicenses(),
                new Comparator<UsageLicense>() {
                    @Override
                    public int compare(UsageLicense b1, UsageLicense b2) {
                        if (b1 == null || b1.getUser() == null)
                            return -1;
                        if (b2 == null || b2.getUser() == null)
                            return 1;
                        // return -1 if b1.isbn > b2.isbn, 1 if b1.isbn <
                        // b2.isbn, 0 otherwise
                        return b1.getUser().getUserId()
                                .compareTo(b2.getUser().getUserId());
                    }
                });
        assertEquals(status, subscription.getStatus());
        if (status == SubscriptionStatus.INVALID) {
            assertTrue(
                    "Wrong subscription id" + subscription.getSubscriptionId(),
                    subscription.getSubscriptionId()
                            .startsWith(subscriptionId + "#"));
        } else {
            assertEquals(subscriptionId, subscription.getSubscriptionId());
            if (status == SubscriptionStatus.PENDING) {
                assertProvisioningNotCompleted(subscription);
            }
        }

        if (checkNotActive) {
            assertEquals(
                    "Product has been informed about creation of all users", 0,
                    appMgmtStub.addedUsers.size());
            assertNull(subscription.getActivationDate());
            assertNull(subscription.getProductInstanceId());
        } else {
            assertNotNull(subscription.getProductInstanceId());
            int idx = userStartIndex;

            assertEquals(
                    "Product has not been informed about creation of all users",
                    subscription.getUsageLicenses().size(),
                    appMgmtStub.addedUsers.size());
            for (UsageLicense lic : subscription.getUsageLicenses()) {
                PlatformUser platformUser = appMgmtStub.addedUsers
                        .get(lic.getUser().getUserId());
                assertNotNull(platformUser);

                assertEquals(
                        new SimpleDateFormat("yyyy-MM-dd").format(
                                GregorianCalendar.getInstance().getTime()),
                        new SimpleDateFormat("yyyy-MM-dd")
                                .format(new Long(lic.getAssignmentDate())));
                assertNotNull("User entry is null for license " + lic.getKey(),
                        lic.getUser());
                assertEquals("user seems to be wrong (userId)", testUsers
                        .get(testOrganizations.get(0)).get(idx).getUserId(),
                        lic.getUser().getUserId());

                // also check the usage license history entries
                List<DomainHistoryObject<?>> ulhs = mgr.findHistory(lic);
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

            assertProvisioningCompleted(subscription);
        }
    }

    private Subscription findInvalidSubscription(String subscriptionId,
            Organization theOrganization) {
        theOrganization = (Organization) mgr.find(theOrganization);
        List<Subscription> subscriptions = theOrganization.getSubscriptions();
        for (Subscription sub : subscriptions) {
            if (sub.getSubscriptionId().startsWith(subscriptionId)) {
                return sub;
            }
        }
        return null;
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testSubscribeToProductUnknownProduct() throws Throwable {
        assertNotNull(subMgmt);
        VOService product = new VOService();
        product.setServiceId("thisproductdoesntexist");
        VOUser[] users = new VOUser[2];
        VOUser[] admins = new VOUser[1];
        setUsers(users, admins);
        subMgmt.subscribeToService(
                Subscriptions.createVOSubscription("testSubscribeToProduct"),
                product, getUsersToAdd(admins, users), null, null,
                new ArrayList<VOUda>());
    }

    @Test
    public void testSubscribeToProductNoUsers() throws Throwable {
        assertNotNull(subMgmt);
        VOService product = getProductToSubscribe(testProducts.get(0).getKey());
        subMgmt.subscribeToService(
                Subscriptions.createVOSubscription("testSubscribeToProduct2"),
                product, null, null, null, new ArrayList<VOUda>());
    }

    @Test(expected = ValidationException.class)
    public void testSubscribeToProductIdToLong() throws Throwable {
        assertNotNull(subMgmt);
        VOService product = getProductToSubscribe(testProducts.get(0).getKey());
        subMgmt.subscribeToService(
                Subscriptions.createVOSubscription(
                        "12345678901234567890" + "12345678901234567890_tolong"),
                product, null, null, null, new ArrayList<VOUda>());
    }

    @Test(expected = ValidationException.class)
    public void testSubscribeToProductPonToLong() throws Throwable {
        assertNotNull(subMgmt);
        VOService product = getProductToSubscribe(testProducts.get(0).getKey());
        VOSubscription sub = Subscriptions.createVOSubscription("test");
        sub.setPurchaseOrderNumber(TOO_LONG_DESCRIPTION);
        subMgmt.subscribeToService(sub, product, null, null, null,
                new ArrayList<VOUda>());
    }

    @Test(expected = SubscriptionAlreadyExistsException.class)
    public void testSubscribeToProductMultipleSubscriptions() throws Throwable {
        assertNotNull(subMgmt);

        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                // Fetch the technical product and set the flag for only one
                // subscription allowed.
                Product product = (Product) mgr.find(testProducts.get(0));
                TechnicalProduct techProd = product.getTechnicalProduct();
                techProd.setOnlyOneSubscriptionAllowed(true);

                return null;
            }
        });

        VOService product = getProductToSubscribe(testProducts.get(0).getKey());
        // Create two subscriptions.
        VOSubscription sub1 = Subscriptions.createVOSubscription("test1");

        VOSubscription sub2 = Subscriptions.createVOSubscription("test2");

        // First subscription should be successful.
        subMgmt.subscribeToService(sub1, product, null, null, null,
                new ArrayList<VOUda>());

        // Trying to subscribe a second time to the same service should throw an
        // exception since the "only one subscription allowed" flag for the
        // technical product is set.
        subMgmt.subscribeToService(sub2, product, null, null, null,
                new ArrayList<VOUda>());
    }

    @Test
    public void testGetSubscriptionDetails() throws Throwable {
        assertNotNull(subMgmt);
        createAvailablePayment(testOrganizations.get(0));
        VOService product = getProductToSubscribe(testProducts.get(1).getKey());
        VOUser[] users = new VOUser[2];
        VOUser[] admins = new VOUser[1];
        setUsers(users, admins);
        VOPaymentInfo voPaymentInfo = getPaymentInfo(
                tpAndSupplier.getOrganizationId(), testOrganizations.get(0));
        VOBillingContact bc = createBillingContact(testOrganizations.get(0));
        subMgmt.subscribeToService(
                Subscriptions
                        .createVOSubscription("testGetSubscriptionDetails"),
                product, getUsersToAdd(admins, users), voPaymentInfo, bc,
                new ArrayList<VOUda>());
        // Now retrieve data and check results
        String voId = "testGetSubscriptionDetails";
        VOSubscriptionDetails voDetails = subMgmt.getSubscriptionDetails(voId);
        assertNotNull(voDetails);
        assertEquals(voDetails.getServiceId(),
                testProducts.get(1).getProductId());
        assertEquals(
                new SimpleDateFormat("yyyy-MM-dd")
                        .format(GregorianCalendar.getInstance().getTime()),
                new SimpleDateFormat("yyyy-MM-dd")
                        .format(voDetails.getActivationDate()));
        assertEquals(
                new SimpleDateFormat("yyyy-MM-dd")
                        .format(GregorianCalendar.getInstance().getTime()),
                new SimpleDateFormat("yyyy-MM-dd")
                        .format(voDetails.getCreationDate()));
        assertNull(voDetails.getDeactivationDate());
        assertEquals(SubscriptionStatus.ACTIVE, voDetails.getStatus());
        assertTrue(voDetails.getPriceModel().isChargeable());
        assert testProducts.get(1).getPriceModel().isChargeable();
        PriceModel orgPM = testProducts.get(1).getPriceModel();
        VOPriceModel actPM = voDetails.getPriceModel();
        assertEquals(orgPM.getPeriod(), actPM.getPeriod());
        assertEquals(orgPM.getPricePerPeriod(), actPM.getPricePerPeriod());

        assertTrue(voDetails.getUsageLicenses().size() == 3);
    }

    /*
     * test case for the upgradeSubscription() BUG which didn't create a product
     * copy. As consequence getProducts() doesn't return the complete product
     * list
     */
    @Test
    public void testUpgrade() throws Throwable {
        createAvailablePayment(testOrganizations.get(0));
        VOService product = getProductToSubscribe(
                testProducts.get(10).getKey());
        VOUser[] users = new VOUser[2];
        VOUser[] admins = new VOUser[1];
        setUsers(users, admins);
        VOSubscription sub = Subscriptions.createVOSubscription("testUpgrade");
        subMgmt.subscribeToService(sub, product, getUsersToAdd(admins, users),
                null, null, new ArrayList<VOUda>());

        List<VOService> products = servProv
                .getServicesForMarketplace(supplierOrgId);
        int numOfProducts = products.size();

        sub = subMgmt.getSubscriptionDetails(sub.getSubscriptionId());
        products = subMgmt.getUpgradeOptions(sub.getSubscriptionId());
        assertEquals(2, products.size());

        VOPaymentInfo voPaymentInfo = getPaymentInfo(
                tpAndSupplier.getOrganizationId(), testOrganizations.get(0));
        VOBillingContact bc = createBillingContact(testOrganizations.get(0));

        VOSubscription upgradedSubscription = subMgmt.upgradeSubscription(sub,
                products.get(0), voPaymentInfo, bc, new ArrayList<VOUda>());

        assertNotNull(upgradedSubscription);
        assertEquals(0L, upgradedSubscription.getUnitKey());
        assertNull(upgradedSubscription.getUnitName());
        assertEquals(numOfProducts,
                servProv.getServicesForMarketplace(supplierOrgId).size());
        assertEquals(products.get(0).getServiceId(),
                upgradedSubscription.getServiceId());
        assertTrue(isTriggerQueueService_sendSuspendingMessageCalled);
        assertTrue(isMessageSend(TriggerType.UPGRADE_SUBSCRIPTION));

        assertEquals(EmailType.SUBSCRIPTION_MIGRATED, mailType);
        assertEquals(receivedParams[0], sub.getSubscriptionId());
        assertEquals(receivedParams[1], product.getServiceId());
        assertEquals(receivedParams[2], products.get(0).getServiceId());
    }

    @Test
    public void testUpgradeSubscriptionOwnerAndUnitIgnored() throws Throwable {
        createAvailablePayment(testOrganizations.get(0));
        VOService product = getProductToSubscribe(
                testProducts.get(10).getKey());
        VOUser[] users = new VOUser[2];
        VOUser[] admins = new VOUser[1];
        setUsers(users, admins);
        VOSubscription sub = Subscriptions.createVOSubscription("testUpgrade");

        UserGroup unit1 = getUnit(UNIT1, testOrganizations.get(0));
        UserGroup unit2 = getUnit(UNIT2, testOrganizations.get(0));

        sub.setUnitKey(unit1.getKey());
        subMgmt.subscribeToService(sub, product, getUsersToAdd(admins, users),
                null, null, new ArrayList<VOUda>());

        List<VOService> products = servProv
                .getServicesForMarketplace(supplierOrgId);

        sub = subMgmt.getSubscriptionDetails(sub.getSubscriptionId());
        products = subMgmt.getUpgradeOptions(sub.getSubscriptionId());
        assertEquals(2, products.size());
        String ownerIdAfterSubscribe = sub.getOwnerId();

        VOPaymentInfo voPaymentInfo = getPaymentInfo(
                tpAndSupplier.getOrganizationId(), testOrganizations.get(0));
        VOBillingContact bc = createBillingContact(testOrganizations.get(0));

        sub.setUnitKey(unit2.getKey());
        sub.setOwnerId("user1");
        VOSubscription upgradedSubscription = subMgmt.upgradeSubscription(sub,
                products.get(0), voPaymentInfo, bc, new ArrayList<VOUda>());

        assertNotNull(upgradedSubscription);
        assertEquals(unit1.getKey(), upgradedSubscription.getUnitKey());
        assertEquals(unit1.getName(), upgradedSubscription.getUnitName());
        assertEquals(ownerIdAfterSubscribe, upgradedSubscription.getOwnerId());
    }

    private UserGroup getUnit(final String unitNamePrefix,
            final Organization org) throws Exception {
        UserGroup unit = runTX(new Callable<UserGroup>() {
            @Override
            public UserGroup call() throws ObjectNotFoundException {
                UserGroup unit = new UserGroup();
                unit.setName(unitNamePrefix + org.getOrganizationId());
                unit.setOrganization_tkey(org.getKey());
                unit = (UserGroup) mgr.getReferenceByBusinessKey(unit);
                return unit;
            }
        });
        return unit;
    }

    private void setUsers(VOUser[] users, VOUser[] admins) {
        admins[0] = UserDataAssembler
                .toVOUser(testUsers.get(testOrganizations.get(0)).get(1));
        users[0] = UserDataAssembler
                .toVOUser(testUsers.get(testOrganizations.get(0)).get(2));
        users[1] = UserDataAssembler
                .toVOUser(testUsers.get(testOrganizations.get(0)).get(3));
    }

    /*
     * BugId 8022 Testcase for the upgradeSubscription() BUG which didn't create
     * a product copy. Before the subscription up-/downgrade, the subscription
     * expires. After the up-/downgrade, the subscription must be active again!
     */
    @Test
    public void testBE08022_UpgradeExpiredSubscription() throws Throwable {
        createAvailablePayment(testOrganizations.get(0));
        VOService product = getProductToSubscribe(
                testProducts.get(10).getKey());
        VOUser[] users = new VOUser[2];
        VOUser[] admins = new VOUser[1];
        setUsers(users, admins);
        VOSubscription sub = Subscriptions.createVOSubscription("testUpgrade");
        subMgmt.subscribeToService(sub, product, getUsersToAdd(admins, users),
                null, null, new ArrayList<VOUda>());

        List<VOService> products = servProv
                .getServicesForMarketplace(supplierOrgId);
        int numOfProducts = products.size();

        sub = subMgmt.getSubscriptionDetails(sub.getSubscriptionId());
        products = subMgmt.getUpgradeOptions(sub.getSubscriptionId());
        assertEquals(2, products.size());

        VOPaymentInfo voPaymentInfo = getPaymentInfo(
                tpAndSupplier.getOrganizationId(), testOrganizations.get(0));
        VOBillingContact bc = createBillingContact(testOrganizations.get(0));

        // First expire the subscription, the up-/downgrade it
        Subscription subDomObj = runTX(new Callable<Subscription>() {
            @Override
            public Subscription call() {
                return getSubscription("testUpgrade",
                        testOrganizations.get(0).getKey());
            }
        });
        subMgmtLocal.expireSubscription(subDomObj);

        sub = subMgmt.getSubscriptionDetails(sub.getSubscriptionId());
        assertEquals(SubscriptionStatus.EXPIRED, sub.getStatus());

        VOSubscription upgradedSubscription = subMgmt.upgradeSubscription(sub,
                products.get(0), voPaymentInfo, bc, new ArrayList<VOUda>());

        // The up-/downgraded susbscription must be activated again!
        assertNotNull(upgradedSubscription);
        assertEquals(SubscriptionStatus.ACTIVE,
                upgradedSubscription.getStatus());

        assertEquals(numOfProducts,
                servProv.getServicesForMarketplace(supplierOrgId).size());
        assertEquals(products.get(0).getServiceId(),
                upgradedSubscription.getServiceId());
        assertTrue(isTriggerQueueService_sendSuspendingMessageCalled);
        assertTrue(isMessageSend(TriggerType.UPGRADE_SUBSCRIPTION));

        assertEquals(EmailType.SUBSCRIPTION_MIGRATED, mailType);
        assertEquals(receivedParams[0], sub.getSubscriptionId());
        assertEquals(receivedParams[1], product.getServiceId());
        assertEquals(receivedParams[2], products.get(0).getServiceId());
    }

    /*
     * BugId 8022 Modify an expired subscription
     */
    @Test(expected = SubscriptionStateException.class)
    public void testBE08022_ModifyExpiredSubscription() throws Throwable {
        createAvailablePayment(testOrganizations.get(0));
        VOService product = getProductToSubscribe(
                testProducts.get(10).getKey());
        VOUser[] users = new VOUser[2];
        VOUser[] admins = new VOUser[1];
        setUsers(users, admins);
        VOSubscription sub = Subscriptions.createVOSubscription("testUpgrade");
        subMgmt.subscribeToService(sub, product, getUsersToAdd(admins, users),
                null, null, new ArrayList<VOUda>());

        sub = subMgmt.getSubscriptionDetails(sub.getSubscriptionId());

        Subscription subDomObj = runTX(new Callable<Subscription>() {
            @Override
            public Subscription call() {
                Subscription sub = getSubscription("testUpgrade",
                        testOrganizations.get(0).getKey());
                load(sub.getProduct());
                return sub;
            }
        });

        // Create a configurable parameter for the subscription product
        final String productID = subDomObj.getProduct().getProductId();
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                doSetParameterSetForProduct(productID, true, "1000");
                return null;
            }
        });

        // Expire the subscription
        subMgmtLocal.expireSubscription(subDomObj);

        VOSubscriptionDetails subDetails = subMgmt
                .getSubscriptionDetails(sub.getSubscriptionId());
        assertEquals(SubscriptionStatus.EXPIRED, subDetails.getStatus());

        List<VOParameter> parameters = subDetails.getSubscribedService()
                .getParameters();

        List<VOParameter> modifiedParameters = new ArrayList<>();

        for (VOParameter parameter : parameters) {
            if (parameter.isConfigurable()) {
                parameter.setValue("8813");
                modifiedParameters.add(parameter);
                break;
            }
        }

        subMgmt.modifySubscription(subDetails, modifiedParameters,
                new ArrayList<VOUda>());
    }

    // BugId 7463
    @Test(expected = ObjectNotFoundException.class)
    public void testUpgradeSubscription_EmptyParams() throws Exception {
        VOSubscription subs = new VOSubscription();
        VOService serv = new VOService();
        subMgmt.upgradeSubscription(subs, serv, null, null,
                new ArrayList<VOUda>());
    }

    @Test(expected = ServiceChangedException.class)
    public void testUpgradeConcurrencyProblemProduct() throws Throwable {
        createAvailablePayment(testOrganizations.get(0));
        VOService product = getProductToSubscribe(
                testProducts.get(10).getKey());
        VOUser[] users = new VOUser[2];
        VOUser[] admins = new VOUser[1];
        setUsers(users, admins);
        VOSubscription sub = Subscriptions.createVOSubscription("testUpgrade");
        subMgmt.subscribeToService(sub, product, getUsersToAdd(admins, users),
                null, null, new ArrayList<VOUda>());

        List<VOService> products = servProv
                .getServicesForMarketplace(supplierOrgId);

        sub = subMgmt.getSubscriptionDetails(sub.getSubscriptionId());
        products = subMgmt.getUpgradeOptions(sub.getSubscriptionId());
        VOService targetService = products.get(0);
        targetService.setVersion(targetService.getVersion() - 1);
        subMgmt.upgradeSubscription(sub, targetService, null, null,
                new ArrayList<VOUda>());
    }

    @Test(expected = ServiceChangedException.class)
    public void testUpgradeConcurrencyProblemProductParameter()
            throws Throwable {
        createAvailablePayment(testOrganizations.get(0));
        VOService product = getProductToSubscribe(
                testProducts.get(10).getKey());
        VOUser[] users = new VOUser[2];
        VOUser[] admins = new VOUser[1];
        setUsers(users, admins);
        VOSubscription sub = Subscriptions.createVOSubscription("testUpgrade");
        subMgmt.subscribeToService(sub, product, getUsersToAdd(admins, users),
                null, null, new ArrayList<VOUda>());

        List<VOService> products = servProv
                .getServicesForMarketplace(supplierOrgId);

        sub = subMgmt.getSubscriptionDetails(sub.getSubscriptionId());
        products = subMgmt.getUpgradeOptions(sub.getSubscriptionId());
        VOService targetService = products.get(0);
        List<VOParameter> parameters = targetService.getParameters();
        VOParameter voParameter = parameters.get(0);
        voParameter.setVersion(voParameter.getVersion() - 1);
        subMgmt.upgradeSubscription(sub, targetService, null, null,
                new ArrayList<VOUda>());
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testUpgradeConcurrencyProblemSubscription() throws Throwable {
        createAvailablePayment(testOrganizations.get(0));
        VOService product = getProductToSubscribe(
                testProducts.get(10).getKey());
        VOUser[] users = new VOUser[2];
        VOUser[] admins = new VOUser[1];
        setUsers(users, admins);
        VOSubscription sub = Subscriptions.createVOSubscription("testUpgrade");
        VOPaymentInfo voPaymentInfo = getPaymentInfo(
                tpAndSupplier.getOrganizationId(), testOrganizations.get(0));
        VOBillingContact bc = createBillingContact(testOrganizations.get(0));

        subMgmt.subscribeToService(sub, product, getUsersToAdd(admins, users),
                voPaymentInfo, bc, new ArrayList<VOUda>());

        List<VOService> products = servProv
                .getServicesForMarketplace(supplierOrgId);

        sub = subMgmt.getSubscriptionDetails(sub.getSubscriptionId());
        products = subMgmt.getUpgradeOptions(sub.getSubscriptionId());
        sub.setVersion(sub.getVersion() - 1);
        subMgmt.upgradeSubscription(sub, products.get(0), voPaymentInfo, bc,
                new ArrayList<VOUda>());
    }

    @Test
    public void testUpgradeSubscriptionInt() throws Exception {
        final TriggerProcess tp = new TriggerProcess();
        final Subscription sub = createSubscription();
        final Product product = sub.getProduct().getTemplate();

        final Product targetProduct = runTX(new Callable<Product>() {
            @Override
            public Product call() throws Exception {
                Product newProduct = Products.createProduct(org,
                        product.getTechnicalProduct(), false, "newProduct",
                        null, mgr);

                ProductReference prodRef = new ProductReference(product,
                        newProduct);
                mgr.persist(prodRef);

                return newProduct;
            }
        });

        final VOPaymentInfo voPaymentInfo = getPaymentInfo(
                tpAndSupplier.getOrganizationId(), testOrganizations.get(0));
        final LocalizerFacade facade = new LocalizerFacade(localizer, "en");
        final VOBillingContact bc = createBillingContact(
                testOrganizations.get(0));
        final VOSubscription voSub = SubscriptionAssembler.toVOSubscription(sub,
                facade);

        VOService voNewProduct = runTX(new Callable<VOService>() {

            @Override
            public VOService call() {
                Product t = (Product) mgr.find(targetProduct);
                VOService voNewPrd = ProductAssembler.toVOProduct(t, facade);
                tp.addTriggerProcessParameter(
                        TriggerProcessParameterName.SUBSCRIPTION, voSub);
                tp.addTriggerProcessParameter(
                        TriggerProcessParameterName.PRODUCT, voNewPrd);
                tp.addTriggerProcessParameter(
                        TriggerProcessParameterName.PAYMENTINFO, voPaymentInfo);
                tp.addTriggerProcessParameter(
                        TriggerProcessParameterName.BILLING_CONTACT, bc);
                tp.addTriggerProcessParameter(TriggerProcessParameterName.UDAS,
                        new ArrayList<VOUda>());
                return voNewPrd;
            }
        });

        PlatformUser user = runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                Organization organization = mgr.getReference(Organization.class,
                        sub.getOrganization().getKey());
                return organization.getPlatformUsers().get(0);
            }
        });

        container.login(user.getKey(), ROLE_ORGANIZATION_ADMIN);

        Subscription upgradedSub = runTX(new Callable<Subscription>() {
            @Override
            public Subscription call() throws Exception {
                return subMgmtLocal.upgradeSubscriptionInt(tp);
            }
        });
        assertNotNull(upgradedSub);
        assertTrue(upgradedSub.getProduct().getProductId()
                .startsWith(voNewProduct.getServiceId()));
        assertNull(upgradedSub.getProduct().getConfiguratorUrl());
        assertTrue(isCorrectSubscriptionIdForMail);
        assertEquals(EmailType.SUBSCRIPTION_MIGRATED, mailType);
        assertTrue(isTriggerQueueService_sendAllNonSuspendingMessageCalled);
        assertTrue(isMessageSend(TriggerType.UPGRADE_SUBSCRIPTION));
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testUpgradeSubscriptionIntObjectNotFound() throws Exception {
        final TriggerProcess tp = new TriggerProcess();
        tp.addTriggerProcessParameter(TriggerProcessParameterName.SUBSCRIPTION,
                new VOSubscription());
        tp.addTriggerProcessParameter(TriggerProcessParameterName.PRODUCT,
                new VOService());
        tp.addTriggerProcessParameter(TriggerProcessParameterName.PAYMENTINFO,
                new VOPaymentInfo());
        tp.addTriggerProcessParameter(
                TriggerProcessParameterName.BILLING_CONTACT,
                new VOBillingContact());
        tp.addTriggerProcessParameter(TriggerProcessParameterName.UDAS,
                new ArrayList<VOUda>());

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                subMgmtLocal.upgradeSubscriptionInt(tp);
                return null;
            }
        });
    }

    /*
     * test case for the upgradeSubscription() BUG which didn't create a product
     * copy. As consequence getProducts() doesn't return the complete product
     * list
     */
    @Test(expected = PaymentInformationException.class)
    public void testUpgradeInvalidPaymentInfo() throws Throwable {
        createAvailablePayment(testOrganizations.get(0));
        VOService product = getProductToSubscribe(
                testProducts.get(10).getKey());
        VOUser[] users = new VOUser[2];
        VOUser[] admins = new VOUser[1];
        setUsers(users, admins);
        VOSubscription sub = Subscriptions.createVOSubscription("testUpgrade");
        subMgmt.subscribeToService(sub, product, getUsersToAdd(admins, users),
                null, null, new ArrayList<VOUda>());

        List<VOService> products = servProv
                .getServicesForMarketplace(supplierOrgId);
        int numOfProducts = products.size();

        sub = subMgmt.getSubscriptionDetails(sub.getSubscriptionId());
        products = subMgmt.getUpgradeOptions(sub.getSubscriptionId());
        assertEquals(2, products.size());

        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                Organization cust = mgr.getReference(Organization.class,
                        testOrganizations.get(0).getKey());

                List<OrganizationRefToPaymentType> paymentTypes = cust
                        .getPaymentTypes(cust.getSources().get(0).getSource()
                                .getOrganizationId());
                for (OrganizationRefToPaymentType orgToPt : paymentTypes) {
                    mgr.remove(orgToPt);
                }
                return null;
            }
        });

        subMgmt.upgradeSubscription(sub, products.get(0), null, null,
                new ArrayList<VOUda>());

        assertEquals(numOfProducts,
                servProv.getServicesForMarketplace(supplierOrgId).size());
    }

    /**
     * Test updateSubscription. Simulate Application Management subscription.
     * 
     * @throws Throwable
     */
    @Test(expected = SubscriptionMigrationException.class)
    public void testUpgradeApplicationManagementExceptionWithParamInException()
            throws Throwable {
        createAvailablePayment(testOrganizations.get(0));
        VOService product = getProductToSubscribe(
                testProducts.get(10).getKey());
        VOUser[] users = new VOUser[2];
        VOUser[] admins = new VOUser[1];
        setUsers(users, admins);
        VOSubscription sub = Subscriptions.createVOSubscription("testUpgrade");
        VOPaymentInfo voPaymentInfo = getPaymentInfo(
                tpAndSupplier.getOrganizationId(), testOrganizations.get(0));
        VOBillingContact bc = createBillingContact(testOrganizations.get(0));
        subMgmt.subscribeToService(sub, product, getUsersToAdd(admins, users),
                voPaymentInfo, bc, new ArrayList<VOUda>());

        List<VOService> products = servProv
                .getServicesForMarketplace(supplierOrgId);

        sub = subMgmt.getSubscriptionDetails(sub.getSubscriptionId());
        products = subMgmt.getUpgradeOptions(sub.getSubscriptionId());
        assertEquals(2, products.size());

        appMgmtStub.throwSaaSApplicationException = true;
        appMgmtStub.throwSaaSApplicationExceptionWithParam = true;

        subMgmt.upgradeSubscription(sub, products.get(0), voPaymentInfo, bc,
                new ArrayList<VOUda>());
    }

    /**
     * Test updateSubscription. Simulate Application Management subscription.
     * 
     * @throws Throwable
     */
    @Test(expected = SubscriptionMigrationException.class)
    public void testUpgradeApplicationManagementExceptionWithoutParamInException()
            throws Throwable {
        createAvailablePayment(testOrganizations.get(0));
        VOService product = getProductToSubscribe(
                testProducts.get(10).getKey());
        VOUser[] users = new VOUser[2];
        VOUser[] admins = new VOUser[1];
        setUsers(users, admins);
        VOSubscription sub = Subscriptions.createVOSubscription("testUpgrade");
        VOPaymentInfo voPaymentInfo = getPaymentInfo(
                tpAndSupplier.getOrganizationId(), testOrganizations.get(0));
        VOBillingContact bc = createBillingContact(testOrganizations.get(0));
        subMgmt.subscribeToService(sub, product, getUsersToAdd(admins, users),
                voPaymentInfo, bc, new ArrayList<VOUda>());

        List<VOService> products = servProv
                .getServicesForMarketplace(supplierOrgId);

        sub = subMgmt.getSubscriptionDetails(sub.getSubscriptionId());
        products = subMgmt.getUpgradeOptions(sub.getSubscriptionId());
        assertEquals(2, products.size());

        appMgmtStub.throwSaaSApplicationException = true;
        appMgmtStub.throwSaaSApplicationExceptionWithParam = false;

        subMgmt.upgradeSubscription(sub, products.get(0), voPaymentInfo, bc,
                new ArrayList<VOUda>());
    }

    @Test
    public void testAddRevokeUserOK() throws Throwable {
        VOService product = getProductToSubscribe(testProducts.get(2).getKey());
        VOUser[] users = new VOUser[2];
        VOUser[] admins = new VOUser[1];
        setUsers(users, admins);
        String subscriptionId = "testAddRevokeUserOK";
        subMgmt.subscribeToService(
                Subscriptions.createVOSubscription(subscriptionId), product,
                getUsersToAdd(admins, users), null, null,
                new ArrayList<VOUda>());
        // Now: Add 2 new users (one with admin privileges), revoke old admin
        // and one "normal" user
        appMgmtStub.addedUsers.clear();
        appMgmtStub.deletedUsers.clear();
        List<VOUsageLicense> addUsers = new ArrayList<>();
        final List<VOUser> revokeUsers = new ArrayList<>();
        addUsers.add(
                mapUserToRole(testUsers.get(testOrganizations.get(0)).get(4)));
        addUsers.add(
                mapUserToRole(testUsers.get(testOrganizations.get(0)).get(5)));
        revokeUsers.add((UserDataAssembler
                .toVOUser(testUsers.get(testOrganizations.get(0)).get(1))));
        revokeUsers.add((UserDataAssembler
                .toVOUser(testUsers.get(testOrganizations.get(0)).get(3))));
        messagesOfTaskQueue = new ArrayList<>();
        boolean wasExecuted = subMgmt.addRevokeUser(subscriptionId, addUsers,
                revokeUsers);
        // Now check results, needs TX-context to use DataManager
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() {
                    checkAddRevokeUserOK(revokeUsers);
                    return null;
                }
            });
        } catch (Exception e) {
            throw e.getCause();
        }
        assertTrue(wasExecuted);
        assertEquals("Product has not been informed about the added users", 2,
                appMgmtStub.addedUsers.size());
        assertEquals("Product has not been informed about the removed users", 2,
                appMgmtStub.deletedUsers.size());
        assertTrue(isTriggerQueueService_sendSuspendingMessageCalled);
        assertTrue(isMessageSend(TriggerType.ADD_REVOKE_USER));
        assertEquals(2, messagesOfTaskQueue.size());
        assertEquals(4, receivedSendMailPayload.size());
    }

    @Test
    public void testAddRevokeUserPendingSubscription_BUG_10170_9998()
            throws Throwable {
        // given
        String subscriptionId = givenPendingSubscriptionWithThreeUsers();

        // when
        boolean wasExecuted = revokeTwoUsers(subscriptionId);

        // then
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() {
                    assertOnlyOneUserAssigned();
                    return null;
                }
            });
        } catch (Exception e) {
            throw e.getCause();
        }
        assertTrue(wasExecuted);
    }

    private boolean revokeTwoUsers(String subscriptionId)
            throws ObjectNotFoundException, ServiceParameterException,
            SubscriptionStateException, TechnicalServiceNotAliveException,
            TechnicalServiceOperationException, OperationNotPermittedException,
            ConcurrentModificationException, OperationPendingException {
        List<VOUsageLicense> addUsers = new ArrayList<>();
        List<VOUser> revokeUsers = new ArrayList<>();

        revokeUsers.add((UserDataAssembler
                .toVOUser(testUsers.get(testOrganizations.get(0)).get(1))));
        revokeUsers.add((UserDataAssembler
                .toVOUser(testUsers.get(testOrganizations.get(0)).get(3))));
        boolean wasExecuted = subMgmt.addRevokeUser(subscriptionId, addUsers,
                revokeUsers);
        return wasExecuted;
    }

    private String givenPendingSubscriptionWithThreeUsers() throws Exception,
            ObjectNotFoundException, NonUniqueBusinessKeyException,
            ValidationException, PaymentInformationException,
            ServiceParameterException, ServiceChangedException,
            PriceModelException, TechnicalServiceNotAliveException,
            TechnicalServiceOperationException, OperationNotPermittedException,
            SubscriptionAlreadyExistsException, OperationPendingException,
            MandatoryUdaMissingException, ConcurrentModificationException {
        VOService asyncProduct = getProductToSubscribe(
                asyncTestProducts.get(0).getKey());
        VOUser[] users = new VOUser[2];
        VOUser[] admins = new VOUser[1];
        setUsers(users, admins);
        String subscriptionId = "testAddRevokeUserPendingSubscription";
        subMgmt.subscribeToService(
                Subscriptions.createVOSubscription(subscriptionId),
                asyncProduct, getUsersToAdd(admins, users), null, null,
                new ArrayList<VOUda>());
        return subscriptionId;
    }

    private void assertOnlyOneUserAssigned() {
        // load generated subscription object
        Organization theOrganization = testOrganizations.get(0);
        Subscription qryObj = new Subscription();
        qryObj.setOrganizationKey(theOrganization.getKey());
        qryObj.setSubscriptionId("testAddRevokeUserPendingSubscription");
        Subscription subscription = (Subscription) mgr.find(qryObj);
        assertNotNull(
                "Could not load subscription 'testAddRevokeUserPendingSubscription'",
                subscription);

        assertEquals("Number of UsageLicenses", 1,
                subscription.getUsageLicenses().size());
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testConcurrentAddRevokeUser() throws Throwable {
        VOService product = getProductToSubscribe(testProducts.get(2).getKey());
        VOUser[] users = new VOUser[2];
        VOUser[] admins = new VOUser[1];
        setUsers(users, admins);
        String subscriptionId = "testAddRevokeUserOK";
        subMgmt.subscribeToService(
                Subscriptions.createVOSubscription(subscriptionId), product,
                getUsersToAdd(admins, users), null, null,
                new ArrayList<VOUda>());
        // Now: Add a new users
        appMgmtStub.addedUsers.clear();
        appMgmtStub.deletedUsers.clear();
        List<VOUsageLicense> addUsers = new ArrayList<>();
        final List<VOUser> revokeUsers = new ArrayList<>();
        addUsers.add(
                mapUserToRole(testUsers.get(testOrganizations.get(0)).get(1)));
        subMgmt.addRevokeUser(subscriptionId, addUsers, revokeUsers);

        // Add the same usage (outdated) license again
        subMgmt.addRevokeUser(subscriptionId, addUsers, revokeUsers);
    }

    private void checkAddRevokeUserOK(List<VOUser> revokedUsers) {
        // load generated subscription object
        Organization theOrganization = testOrganizations.get(0);
        Subscription qryObj = new Subscription();
        qryObj.setOrganizationKey(theOrganization.getKey());
        qryObj.setSubscriptionId("testAddRevokeUserOK");
        Subscription subscription = (Subscription) mgr.find(qryObj);
        assertNotNull("Could not load subscription 'testAddRevokeUserOK'",
                subscription);
        // only check for usageLicenses
        // we should have 5 usageLicense-Entries
        assertEquals("Number of UsageLicenses", 3,
                subscription.getUsageLicenses().size());
        // sort by UserId
        Collections.sort(subscription.getUsageLicenses(),
                new Comparator<UsageLicense>() {
                    @Override
                    public int compare(UsageLicense b1, UsageLicense b2) {
                        if (b1 == null || b1.getUser() == null)
                            return -1;
                        if (b2 == null || b2.getUser() == null)
                            return 1;
                        // return -1 if b1.isbn > b2.isbn, 1 if b1.isbn <
                        // b2.isbn, 0 otherwise
                        return b1.getUser().getUserId()
                                .compareTo(b2.getUser().getUserId());
                    }
                });
        // now check entries
        // No. 1) "usr2" still active
        UsageLicense lic = subscription.getUsageLicenses().get(0);
        List<DomainHistoryObject<?>> hist = mgr.findHistory(lic);
        assertUsageLicenseHistory(hist);
        // No. 3) "usr4" newly assigned
        lic = subscription.getUsageLicenses().get(1);
        hist = mgr.findHistory(lic);
        assertUsageLicenseHistory(hist);
        // No. 4) "usr5" newly assigned
        lic = subscription.getUsageLicenses().get(2);
        hist = mgr.findHistory(lic);
        assertUsageLicenseHistory(hist);

        // check for deleted users, history entries must reflect deletion
        Query query = mgr.createQuery(
                "SELECT ul FROM UsageLicenseHistory ul WHERE subscriptionObjKey = :subKey ORDER BY USEROBJKEY ASC, MODDATE DESC");
        query.setParameter("subKey", Long.valueOf(subscription.getKey()));
        for (UsageLicenseHistory ulHist : ParameterizedTypes
                .iterable(query.getResultList(), UsageLicenseHistory.class)) {
            for (VOUser user : revokedUsers) {
                if (user.getKey() == ulHist.getUserObjKey()
                        && ulHist.getObjVersion() == 2) {
                    // No. 0) "usr1" is revoked, has been admin
                    hist = mgr.findHistory(lic);
                    assertEquals("Wrong history entry", ModificationType.DELETE,
                            ulHist.getModtype());
                }
            }
        }
    }

    @Test(expected = ServiceParameterException.class)
    public void testAddRevokeUserWithParam() throws Throwable {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Product product = (Product) mgr.find(testProducts.get(3));
                Products.addPlatformParameter(product,
                        PlatformParameterIdentifiers.NAMED_USER, true,
                        Long.valueOf(3), mgr);
                return null;
            }
        });
        createAvailablePayment(testOrganizations.get(0));
        VOSubscription subscription = Subscriptions
                .createVOSubscription(commonSubscribeToProduct());

        List<VOUsageLicense> addUsers = new ArrayList<>();
        addUsers.add(
                mapUserToRole(testUsers.get(testOrganizations.get(0)).get(4)));
        addUsers.add(
                mapUserToRole(testUsers.get(testOrganizations.get(0)).get(5)));
        subMgmt.addRevokeUser(subscription.getSubscriptionId(), addUsers, null);
    }

    @Test
    public void testAddRevokeUserTestSuspensionByTrigger() throws Throwable {
        createAvailablePayment(testOrganizations.get(0));
        VOSubscription subscription = Subscriptions
                .createVOSubscription(commonSubscribeToProduct());

        List<VOUsageLicense> addUsers = new ArrayList<>();
        addUsers.add(
                mapUserToRole(testUsers.get(testOrganizations.get(0)).get(4)));
        addUsers.add(
                mapUserToRole(testUsers.get(testOrganizations.get(0)).get(5)));
        td = new TriggerDefinition();
        td.setType(TriggerType.ADD_REVOKE_USER);
        isTriggerQueueService_sendAllNonSuspendingMessageCalled = false;
        isTriggerQueueService_sendSuspendingMessageCalled = false;
        boolean result = subMgmt.addRevokeUser(subscription.getSubscriptionId(),
                addUsers, null);
        assertTrue("suspending method was not called",
                isTriggerQueueService_sendSuspendingMessageCalled);
        assertFalse(
                "addition of users was processed, although it should not have been",
                isTriggerQueueService_sendAllNonSuspendingMessageCalled);
        assertFalse("Execution was not suspended", result);
    }

    @Test
    public void testAddRevokeUserUserAssignedTwice() throws Throwable {
        VOService product = getProductToSubscribe(testProducts.get(4).getKey());
        VOUser[] users = new VOUser[2];
        VOUser[] admins = new VOUser[1];
        setUsers(users, admins);
        String subscriptionId = "testAddRevokeUserUserAssignedTwice";
        subMgmt.subscribeToService(
                Subscriptions.createVOSubscription(subscriptionId), product,
                getUsersToAdd(admins, users), null, null,
                new ArrayList<VOUda>());
        // Now: Add 2 new users (one with changed admin privileges)
        List<VOUsageLicense> addUsers = new ArrayList<>();
        addUsers.add(
                mapUserToRole(testUsers.get(testOrganizations.get(0)).get(2)));
        addUsers.add(
                mapUserToRole(testUsers.get(testOrganizations.get(0)).get(3)));

        // Add existing users again:
        addUsers.clear();
        final VOSubscriptionDetails details = subMgmt
                .getSubscriptionDetails(subscriptionId);
        addUsers.addAll(details.getUsageLicenses());
        subMgmt.addRevokeUser(subscriptionId, addUsers, null);
        // Now check results, needs TX-context to use DataManager
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() {
                    checkAddRevokeUserUserAssignedTwice();
                    return null;
                }
            });
        } catch (Exception e) {
            throw e.getCause();
        }

    }

    private void checkAddRevokeUserUserAssignedTwice() {
        // load generated subscription object
        Organization theOrganization = testOrganizations.get(0);
        Subscription qryObj = new Subscription();
        qryObj.setOrganizationKey(theOrganization.getKey());
        qryObj.setSubscriptionId("testAddRevokeUserUserAssignedTwice");
        Subscription subscription = (Subscription) mgr.find(qryObj);
        assertNotNull(
                "Could not load subscription 'testAddRevokeUserUserAssignedTwice'",
                subscription);
        // only check for usageLicenses
        // we should have 3 usageLicense-Entries
        assertEquals("Number of UsageLicenses", 3,
                subscription.getUsageLicenses().size());
        // sort by UserId
        Collections.sort(subscription.getUsageLicenses(),
                new Comparator<UsageLicense>() {
                    @Override
                    public int compare(UsageLicense b1, UsageLicense b2) {
                        if (b1 == null || b1.getUser() == null)
                            return -1;
                        if (b2 == null || b2.getUser() == null)
                            return 1;
                        // return -1 if b1.isbn > b2.isbn, 1 if b1.isbn <
                        // b2.isbn, 0 otherwise
                        return b1.getUser().getUserId()
                                .compareTo(b2.getUser().getUserId());
                    }
                });
        // now check entries
        // No. 0) "usr1" is unchanged
        UsageLicense lic = subscription.getUsageLicenses().get(0);
        List<DomainHistoryObject<?>> hist = mgr.findHistory(lic);
        assertUsageLicenseHistory(hist);
        // No. 1) "usr2" still active as normal user
        lic = subscription.getUsageLicenses().get(1);
        hist = mgr.findHistory(lic);
        assertEquals("Wrong history entry", ModificationType.MODIFY,
                hist.get(hist.size() - 1).getModtype());
        // No. 2) "usr3" still active, but not changed, so the add entry is the
        // last one
        lic = subscription.getUsageLicenses().get(2);
        hist = mgr.findHistory(lic);
        assertEquals("Wrong history entry", ModificationType.ADD,
                hist.get(hist.size() - 1).getModtype());
    }

    @Test
    public void testAddRevokeUserUserToBeAssignedNotFound() throws Throwable {
        VOService product = getProductToSubscribe(testProducts.get(4).getKey());
        VOUser[] users = new VOUser[2];
        VOUser[] admins = new VOUser[1];
        setUsers(users, admins);
        String subscriptionId = "testAddRevokeUserUserNotFound";
        subMgmt.subscribeToService(
                Subscriptions.createVOSubscription(subscriptionId), product,
                getUsersToAdd(admins, users), null, null,
                new ArrayList<VOUda>());
        // Now: Add 2 new users (one with changed admin privileges)
        List<VOUsageLicense> addUsers = new ArrayList<>();
        addUsers.add(
                mapUserToRole(testUsers.get(testOrganizations.get(0)).get(5)));
        PlatformUser unexistingUser = (PlatformUser) ReflectiveClone
                .clone(testUsers.get(testOrganizations.get(0)).get(2));
        unexistingUser.setKey(10999998);
        addUsers.add(mapUserToRole(unexistingUser));
        try {
            subMgmt.addRevokeUser(subscriptionId, addUsers, null);
            fail("expected ObjectNotFound !");
        } catch (ObjectNotFoundException e) {

        }
        // Now check results (Rollback !!!), needs TX-context to use DataManager
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() {
                    checkAddRevokeUserUserToBeAssignedNotFound();
                    return null;
                }
            });
        } catch (Exception e) {
            throw e.getCause();
        }

    }

    private void checkAddRevokeUserUserToBeAssignedNotFound() {
        // load generated subscription object
        Organization theOrganization = testOrganizations.get(0);
        Subscription qryObj = new Subscription();
        qryObj.setOrganizationKey(theOrganization.getKey());
        qryObj.setSubscriptionId("testAddRevokeUserUserNotFound");
        Subscription subscription = (Subscription) mgr.find(qryObj);
        assertNotNull(
                "Could not load subscription 'testAddRevokeUserUserNotFound'",
                subscription);
        // only check for usageLicenses
        // we should have 3 usageLicense-Entries
        assertEquals("Number of UsageLicenses", 3,
                subscription.getUsageLicenses().size());
    }

    @Test
    public void testAddRevokeUserUserToBeRevokedNotFound() throws Throwable {
        VOService product = getProductToSubscribe(testProducts.get(4).getKey());
        VOUser[] users = new VOUser[2];
        VOUser[] admins = new VOUser[1];
        setUsers(users, admins);
        String subscriptionId = "testAddRevokeUserUserToBeRevokedNotFound";
        subMgmt.subscribeToService(
                Subscriptions.createVOSubscription(subscriptionId), product,
                getUsersToAdd(admins, users), null, null,
                new ArrayList<VOUda>());
        // Now: Add 2 new users
        List<VOUsageLicense> addUsers = new ArrayList<>();
        addUsers.add(
                mapUserToRole(testUsers.get(testOrganizations.get(0)).get(4)));
        addUsers.add(
                mapUserToRole(testUsers.get(testOrganizations.get(0)).get(5)));
        // and revoke unexisting user
        List<VOUser> revokeUsers = new ArrayList<>();
        PlatformUser unexistingUser = (PlatformUser) ReflectiveClone
                .clone(testUsers.get(testOrganizations.get(0)).get(2));
        unexistingUser.setKey(10999998);
        VOUser voUser = UserDataAssembler.toVOUser(unexistingUser);
        revokeUsers.add(voUser);
        try {
            subMgmt.addRevokeUser(subscriptionId, addUsers, revokeUsers);
            fail("expected ObjectNotFound !");
        } catch (ObjectNotFoundException e) {

        }
        // Now check results (Rollback !!!), needs TX-context to use DataManager
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() {
                    checkAddRevokeUserUserToBeRevokedNotFound();
                    return null;
                }
            });
        } catch (Exception e) {
            throw e.getCause();
        }
    }

    private void checkAddRevokeUserUserToBeRevokedNotFound() {
        // load generated subscription object
        Organization theOrganization = testOrganizations.get(0);
        Subscription qryObj = new Subscription();
        qryObj.setOrganizationKey(theOrganization.getKey());
        qryObj.setSubscriptionId("testAddRevokeUserUserToBeRevokedNotFound");
        Subscription subscription = (Subscription) mgr.find(qryObj);
        assertNotNull(
                "Could not load subscription 'testAddRevokeUserUserToBeRevokedNotFound'",
                subscription);
        // only check for usageLicenses
        // we should have 3 usageLicense-Entries
        assertEquals("Number of UsageLicenses", 3,
                subscription.getUsageLicenses().size());
    }

    /**
     * All users can be removed from subscription
     * 
     * @throws Throwable
     */
    @Test
    public void testAddRevokeUserRevokeLastUser() throws Throwable {
        VOService product = getProductToSubscribe(testProducts.get(4).getKey());
        VOUser[] users = new VOUser[2];
        VOUser[] admins = new VOUser[1];
        setUsers(users, admins);
        String subscriptionId = "testAddRevokeUserRevokeLastUser";
        subMgmt.subscribeToService(
                Subscriptions.createVOSubscription(subscriptionId), product,
                getUsersToAdd(admins, users), null, null,
                new ArrayList<VOUda>());
        // Revoke all users
        List<VOUser> revokeUsers = new ArrayList<>();
        revokeUsers.add(admins[0]);
        revokeUsers.add(users[0]);
        revokeUsers.add(users[1]);

        subMgmt.addRevokeUser(subscriptionId, null, revokeUsers);

        // Now check results (Rollback !!!), needs TX-context to use DataManager
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() {
                    checkAddRevokeUserRevokeLastUser();
                    return null;
                }
            });
        } catch (Exception e) {
            throw e.getCause();
        }
    }

    private void checkAddRevokeUserRevokeLastUser() {
        // load generated subscription object
        Organization theOrganization = testOrganizations.get(0);
        Subscription qryObj = new Subscription();
        qryObj.setOrganizationKey(theOrganization.getKey());
        qryObj.setSubscriptionId("testAddRevokeUserRevokeLastUser");
        Subscription subscription = (Subscription) mgr.find(qryObj);
        assertNotNull(
                "Could not load subscription 'testAddRevokeUserRevokeLastUser'",
                subscription);
        // only check for usageLicenses
        // we should have 0 usageLicense-Entries
        assertEquals("Number of UsageLicenses", 0,
                subscription.getUsageLicenses().size());
    }

    @Test
    public void testHasCurrentUserSubscriptions_noSubscriptions() {
        // current user is organization admin
        assertFalse(subMgmt.hasCurrentUserSubscriptions());
    }

    @Test
    public void testHasCurrentUserSubscriptions_activeSubscription()
            throws Throwable {
        // current user is organization admin
        VOService product = getProductToSubscribe(testProducts.get(2).getKey());
        VOUser[] admins = new VOUser[2];
        admins[0] = UserDataAssembler
                .toVOUser(testUsers.get(testOrganizations.get(0)).get(0));
        admins[1] = UserDataAssembler
                .toVOUser(testUsers.get(testOrganizations.get(0)).get(1));
        String subscriptionId1 = "getActiveSubscriptionsForCurrentUser1";
        subMgmt.subscribeToService(
                Subscriptions.createVOSubscription(subscriptionId1), product,
                getUsersToAdd(admins, null), null, null,
                new ArrayList<VOUda>());
        assertTrue(subMgmt.hasCurrentUserSubscriptions());
    }

    @Test
    public void testHasCurrentUserSubscriptions_expiredSubscription()
            throws Throwable {
        // current user is organization admin
        VOService product = getProductToSubscribe(testProducts.get(2).getKey());
        VOUser[] admins = new VOUser[2];
        admins[0] = UserDataAssembler
                .toVOUser(testUsers.get(testOrganizations.get(0)).get(0));
        admins[1] = UserDataAssembler
                .toVOUser(testUsers.get(testOrganizations.get(0)).get(1));
        String subscriptionId1 = "getActiveSubscriptionsForCurrentUser1";
        final VOSubscription sub1 = subMgmt.subscribeToService(
                Subscriptions.createVOSubscription(subscriptionId1), product,
                getUsersToAdd(admins, null), null, null,
                new ArrayList<VOUda>());
        // set status to invalid
        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                Subscription subscription = mgr.getReference(Subscription.class,
                        sub1.getKey());
                subscription.setStatus(SubscriptionStatus.EXPIRED);
                return null;
            }
        });
        assertTrue(subMgmt.hasCurrentUserSubscriptions());
    }

    @Test
    public void testHasCurrentUserSubscriptions_pendingSubscription()
            throws Throwable {
        // current user is organization admin
        VOService product = getProductToSubscribe(testProducts.get(2).getKey());
        VOUser[] admins = new VOUser[2];
        admins[0] = UserDataAssembler
                .toVOUser(testUsers.get(testOrganizations.get(0)).get(0));
        admins[1] = UserDataAssembler
                .toVOUser(testUsers.get(testOrganizations.get(0)).get(1));
        String subscriptionId1 = "getActiveSubscriptionsForCurrentUser1";
        final VOSubscription sub1 = subMgmt.subscribeToService(
                Subscriptions.createVOSubscription(subscriptionId1), product,
                getUsersToAdd(admins, null), null, null,
                new ArrayList<VOUda>());
        // set status to invalid
        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                Subscription subscription = mgr.getReference(Subscription.class,
                        sub1.getKey());
                subscription.setStatus(SubscriptionStatus.PENDING);
                return null;
            }
        });
        assertTrue(subMgmt.hasCurrentUserSubscriptions());
    }

    @Test
    public void testHasCurrentUserSubscriptions_suspendedSubscription()
            throws Throwable {
        // current user is organization admin
        VOService product = getProductToSubscribe(testProducts.get(2).getKey());
        VOUser[] admins = new VOUser[2];
        admins[0] = UserDataAssembler
                .toVOUser(testUsers.get(testOrganizations.get(0)).get(0));
        admins[1] = UserDataAssembler
                .toVOUser(testUsers.get(testOrganizations.get(0)).get(1));
        String subscriptionId1 = "getActiveSubscriptionsForCurrentUser1";
        final VOSubscription sub1 = subMgmt.subscribeToService(
                Subscriptions.createVOSubscription(subscriptionId1), product,
                getUsersToAdd(admins, null), null, null,
                new ArrayList<VOUda>());
        // set status to invalid
        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                Subscription subscription = mgr.getReference(Subscription.class,
                        sub1.getKey());
                subscription.setStatus(SubscriptionStatus.SUSPENDED);
                return null;
            }
        });
        assertTrue(subMgmt.hasCurrentUserSubscriptions());
    }

    @Test
    public void testHasCurrentUserSubscriptions_invalidSubscription()
            throws Throwable {
        // current user is organization admin
        VOService product = getProductToSubscribe(testProducts.get(2).getKey());
        VOUser[] admins = new VOUser[2];
        admins[0] = UserDataAssembler
                .toVOUser(testUsers.get(testOrganizations.get(0)).get(0));
        admins[1] = UserDataAssembler
                .toVOUser(testUsers.get(testOrganizations.get(0)).get(1));
        String subscriptionId1 = "getActiveSubscriptionsForCurrentUser1";
        final VOSubscription sub1 = subMgmt.subscribeToService(
                Subscriptions.createVOSubscription(subscriptionId1), product,
                getUsersToAdd(admins, null), null, null,
                new ArrayList<VOUda>());
        // set status to invalid
        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                Subscription subscription = mgr.getReference(Subscription.class,
                        sub1.getKey());
                subscription.setStatus(SubscriptionStatus.INVALID);
                return null;
            }
        });
        assertFalse(subMgmt.hasCurrentUserSubscriptions());
    }

    @Test
    public void testHasCurrentUserSubscriptions_deactivatedSubscriptions()
            throws Throwable {
        // current user is organization admin, should not have any subscriptions
        // from previous tests !
        VOService product = getProductToSubscribe(testProducts.get(2).getKey());
        VOUser[] admins = new VOUser[2];
        admins[0] = UserDataAssembler
                .toVOUser(testUsers.get(testOrganizations.get(0)).get(0));
        admins[1] = UserDataAssembler
                .toVOUser(testUsers.get(testOrganizations.get(0)).get(1));
        String subscriptionId1 = "getActiveSubscriptionsForCurrentUser1";
        subMgmt.subscribeToService(
                Subscriptions.createVOSubscription(subscriptionId1), product,
                getUsersToAdd(admins, null), null, null,
                new ArrayList<VOUda>());
        // set status to deactivated
        subMgmt.unsubscribeFromService(subscriptionId1);
        assertFalse(subMgmt.hasCurrentUserSubscriptions());
    }

    @Test
    public void testHasCurrentUserSubscriptions_mixedSubscriptionStatus()
            throws Throwable {
        // current user is organization admin, should not have any subscriptions
        // from previous tests !
        VOService product = getProductToSubscribe(testProducts.get(2).getKey());
        VOUser[] admins = new VOUser[2];
        admins[0] = UserDataAssembler
                .toVOUser(testUsers.get(testOrganizations.get(0)).get(0));
        admins[1] = UserDataAssembler
                .toVOUser(testUsers.get(testOrganizations.get(0)).get(1));
        String subscriptionId1 = "getActiveSubscriptionsForCurrentUser1";
        subMgmt.subscribeToService(
                Subscriptions.createVOSubscription(subscriptionId1), product,
                getUsersToAdd(admins, null), null, null,
                new ArrayList<VOUda>());
        // set status to deactivated
        subMgmt.unsubscribeFromService(subscriptionId1);
        String subscriptionId2 = "getActiveSubscriptionsForCurrentUser2";
        subMgmt.subscribeToService(
                Subscriptions.createVOSubscription(subscriptionId2), product,
                getUsersToAdd(admins, null), null, null,
                new ArrayList<VOUda>());
        assertTrue(subMgmt.hasCurrentUserSubscriptions());
    }

    @Test(expected = EJBException.class)
    public void testHasCurrentUserSubscriptions_anonymousUser() {
        container.login("anonymous");
        subMgmt.hasCurrentUserSubscriptions();
    }

    @Test
    public void testGetActiveSubscriptionsForCurrentUser() throws Throwable {
        // current user is organization admin, should not have any subscriptions
        // from previous tests !
        VOService product = getProductToSubscribe(testProducts.get(2).getKey());
        VOUser[] admins = new VOUser[2];
        admins[0] = UserDataAssembler
                .toVOUser(testUsers.get(testOrganizations.get(0)).get(0));
        admins[1] = UserDataAssembler
                .toVOUser(testUsers.get(testOrganizations.get(0)).get(1));
        String subscriptionId1 = "getActiveSubscriptionsForCurrentUser1";
        subMgmt.subscribeToService(
                Subscriptions.createVOSubscription(subscriptionId1), product,
                getUsersToAdd(admins, null), null, null,
                new ArrayList<VOUda>());
        String subscriptionId2 = "getActiveSubscriptionsForCurrentUser2";
        subMgmt.subscribeToService(
                Subscriptions.createVOSubscription(subscriptionId2), product,
                getUsersToAdd(admins, null), null, null,
                new ArrayList<VOUda>());
        List<VOUserSubscription> subList = subMgmt
                .getSubscriptionsForCurrentUser();
        assertEquals("Number of found subscriptions", 2, subList.size());
        if (subscriptionId1.equals(subList.get(0).getSubscriptionId())) {
            assertEquals(subscriptionId2, subList.get(1).getSubscriptionId());
        } else if (subscriptionId1.equals(subList.get(1).getSubscriptionId())) {
            assertEquals(subscriptionId2, subList.get(0).getSubscriptionId());
        } else {
            fail("Wrong subscription Id's");
        }
    }

    @Test
    public void testGetActiveSubscriptionsForUser() throws Throwable {
        VOService product = getProductToSubscribe(testProducts.get(2).getKey());
        VOUser[] admins = new VOUser[1];
        admins[0] = UserDataAssembler
                .toVOUser(testUsers.get(testOrganizations.get(0)).get(5));
        String subscriptionId1 = "testGetActiveSubscriptionsForUser1";
        subMgmt.subscribeToService(
                Subscriptions.createVOSubscription(subscriptionId1), product,
                getUsersToAdd(admins, null), null, null,
                new ArrayList<VOUda>());
        String subscriptionId2 = "testGetActiveSubscriptionsForUser2";
        subMgmt.subscribeToService(
                Subscriptions.createVOSubscription(subscriptionId2), product,
                getUsersToAdd(admins, null), null, null,
                new ArrayList<VOUda>());
        List<VOUserSubscription> subList = subMgmt
                .getSubscriptionsForUser(admins[0]);
        assertEquals("Number of found subscriptions", 2, subList.size());
        if (subscriptionId1.equals(subList.get(0).getSubscriptionId())) {
            assertEquals(subscriptionId2, subList.get(1).getSubscriptionId());
        } else if (subscriptionId1.equals(subList.get(1).getSubscriptionId())) {
            assertEquals(subscriptionId2, subList.get(0).getSubscriptionId());
        } else {
            fail("Wrong subscription Id's");
        }
    }

    @Test(expected = OperationNotPermittedException.class)
    public void testGetActiveSubscriptionsForUserNotPermitted()
            throws Throwable {
        VOUser otherUser = UserDataAssembler
                .toVOUser(testUsers.get(testOrganizations.get(1)).get(5));
        subMgmt.getSubscriptionsForUser(otherUser);
    }

    @Test
    public void testUnsubscribeFromProductOK() throws Throwable {
        createAvailablePayment(testOrganizations.get(0));
        VOService product = getProductToSubscribe(testProducts.get(1).getKey());
        VOUser[] users = new VOUser[2];
        VOUser[] admins = new VOUser[1];
        setUsers(users, admins);
        String subscriptionId = "testUnsubscribeFromProductOK";
        appMgmtStub.addedUsers.clear();
        appMgmtStub.deletedUsers.clear();

        VOPaymentInfo voPaymentInfo = getPaymentInfo(
                tpAndSupplier.getOrganizationId(), testOrganizations.get(0));
        VOBillingContact bc = createBillingContact(testOrganizations.get(0));

        UserGroup unit1 = getUnit(UNIT1, testOrganizations.get(0));
        VOSubscription newSub = Subscriptions
                .createVOSubscription(subscriptionId);
        newSub.setUnitKey(unit1.getKey());

        subMgmt.subscribeToService(newSub, product,
                getUsersToAdd(admins, users), voPaymentInfo, bc,
                new ArrayList<VOUda>());

        final VOSubscriptionDetails sub = subMgmt
                .getSubscriptionDetails(subscriptionId);
        assertNotNull(sub);

        // now unsubscribe
        boolean wasExecuted = subMgmt.unsubscribeFromService(subscriptionId);
        String newSubId = runTX(new Callable<String>() {
            @Override
            public String call() {
                Subscription subscription = new Subscription();
                subscription.setKey(sub.getKey());
                List<DomainHistoryObject<?>> hist = mgr
                        .findHistory(subscription);
                return ((SubscriptionData) hist.get(hist.size() - 1)
                        .getDataContainer()).getSubscriptionId();
            }
        });

        final VOSubscriptionDetails renamedSub = subMgmt
                .getSubscriptionDetails(newSubId);
        assertNotNull(renamedSub);
        // check results
        assertEquals(SubscriptionStatus.DEACTIVATED, renamedSub.getStatus());
        assertEquals(
                new SimpleDateFormat("yyyy-MM-dd")
                        .format(GregorianCalendar.getInstance().getTime()),
                new SimpleDateFormat("yyyy-MM-dd")
                        .format(renamedSub.getDeactivationDate()));
        assertEquals("Wrong number of users added to the product", 3,
                appMgmtStub.addedUsers.size());
        assertEquals("No users must be removed from the service instance", 0,
                appMgmtStub.deletedUsers.size());
        assertTrue("Product has not been deleted after unsubscription",
                appMgmtStub.isProductDeleted);
        assertEquals("Licenses were not removed!", 0,
                renamedSub.getUsageLicenses().size());
        assertTrue(isTriggerQueueService_sendSuspendingMessageCalled);
        assertTrue(isMessageSend(TriggerType.UNSUBSCRIBE_FROM_SERVICE));
        assertTrue(wasExecuted);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testUnsubscribeFromProductIntObjectNotFound() throws Exception {
        final TriggerProcess tp = new TriggerProcess();
        String subId = "subId";
        tp.addTriggerProcessParameter(TriggerProcessParameterName.SUBSCRIPTION,
                subId);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                subMgmtLocal.unsubscribeFromServiceInt(tp);
                return null;
            }
        });
    }

    @Test
    public void testUnsubscribeFromProductInt() throws Exception {
        final TriggerProcess tp = new TriggerProcess();
        final Subscription sub = createSubscription();
        String subId = sub.getSubscriptionId();
        tp.addTriggerProcessParameter(TriggerProcessParameterName.SUBSCRIPTION,
                subId);

        PlatformUser user = runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                Organization organization = mgr.getReference(Organization.class,
                        sub.getOrganization().getKey());
                return organization.getPlatformUsers().get(0);
            }
        });
        container.login(user.getKey(), ROLE_ORGANIZATION_ADMIN);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                subMgmtLocal.unsubscribeFromServiceInt(tp);
                return null;
            }
        });
        assertTrue(isTriggerQueueService_sendAllNonSuspendingMessageCalled);
        assertTrue(isMessageSend(TriggerType.UNSUBSCRIBE_FROM_SERVICE));
        assertTrue(isCorrectSubscriptionIdForMail);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testUnsubscribeFromProductNotFound() throws Throwable {
        VOService product = new VOService();
        product.setServiceId(testProducts.get(4).getProductId());
        VOUser[] users = new VOUser[2];
        VOUser[] admins = new VOUser[1];
        setUsers(users, admins);
        String subscriptionId = "testUnsubscribeFromProductNotFound";
        subMgmt.subscribeToService(
                Subscriptions.createVOSubscription(subscriptionId), product,
                getUsersToAdd(admins, users), null, null,
                new ArrayList<VOUda>());
        // now unsubscribe
        String voSubId = "unknown";
        subMgmt.unsubscribeFromService(voSubId);
    }

    @Test
    public void testGetSubscriptionsForOrganization() throws Throwable {
        // Switch to organization 1
        long userKey = testUsers.get(testOrganizations.get(0)).get(0).getKey();
        container.login(String.valueOf(userKey), ROLE_ORGANIZATION_ADMIN);

        // subscribe two times
        VOService product = getProductToSubscribe(
                testProducts.get(12).getKey());
        VOUser[] admins = new VOUser[1];
        admins[0] = UserDataAssembler
                .toVOUser(testUsers.get(testOrganizations.get(0)).get(5));
        final String subscriptionId1 = "testGetSubscriptionsForOrganization1";
        subMgmt.subscribeToService(
                Subscriptions.createVOSubscription(subscriptionId1), product,
                getUsersToAdd(admins, null), null, null,
                new ArrayList<VOUda>());
        String subscriptionId2 = "testGetSubscriptionsForOrganization2";
        subMgmt.subscribeToService(
                Subscriptions.createVOSubscription(subscriptionId2), product,
                getUsersToAdd(admins, null), null, null,
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

    @Test
    public void testGetSubscriptionsForOrganizationWithFilter()
            throws Throwable {
        // Switch to organization 1
        long userKey = testUsers.get(testOrganizations.get(0)).get(0).getKey();
        container.login(String.valueOf(userKey), ROLE_ORGANIZATION_ADMIN,
                ROLE_SERVICE_MANAGER, ROLE_TECHNOLOGY_MANAGER);

        // given two subscriptions
        VOService product = getProductToSubscribe(
                testProducts.get(12).getKey());
        VOUser[] admins = new VOUser[1];
        admins[0] = UserDataAssembler
                .toVOUser(testUsers.get(testOrganizations.get(0)).get(5));
        final String subscriptionId1 = "testGetSubscriptionsForOrganization1";
        subMgmt.subscribeToService(
                Subscriptions.createVOSubscription(subscriptionId1), product,
                getUsersToAdd(admins, null), null, null,
                new ArrayList<VOUda>());
        String subscriptionId2 = "testGetSubscriptionsForOrganization2";
        final VOSubscription sub2 = subMgmt.subscribeToService(
                Subscriptions.createVOSubscription(subscriptionId2), product,
                getUsersToAdd(admins, null), null, null,
                new ArrayList<VOUda>());

        // when terminating one
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Subscription sub = mgr.find(Subscription.class, sub2.getKey());
                sub.setStatus(SubscriptionStatus.DEACTIVATED);
                mgr.persist(sub);
                return null;
            }
        });

        // then only one active is found
        List<VOSubscription> subList = subMgmt
                .getSubscriptionsForOrganizationWithFilter(
                        EnumSet.of(SubscriptionStatus.ACTIVE));
        assertEquals("Number of found subscriptions", 1, subList.size());
        assertEquals(subscriptionId1, subList.get(0).getSubscriptionId());
    }

    @Test
    public void testAddUserToSubscriptionOK() throws Throwable {
        VOService product = getProductToSubscribe(testProducts.get(4).getKey());
        VOUser[] users = new VOUser[1];
        VOUser[] admins = new VOUser[1];
        admins[0] = UserDataAssembler
                .toVOUser(testUsers.get(testOrganizations.get(0)).get(1));
        users[0] = UserDataAssembler
                .toVOUser(testUsers.get(testOrganizations.get(0)).get(2));
        String subscriptionId = "testAddUserToSubscriptionOK";
        subMgmt.subscribeToService(
                Subscriptions.createVOSubscription(subscriptionId), product,
                getUsersToAdd(admins, users), null, null,
                new ArrayList<VOUda>());
        // do the work (local interface, thus needs tx boundary)
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    doTestAddUserToSubscriptionOK();
                    return null;
                }
            });
        } catch (Exception e) {
            throw e.getCause();
        }
        // check results
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    checkAddUserToSubscriptionOK();
                    return null;
                }
            });
        } catch (Exception e) {
            throw e.getCause();
        }
    }

    private void doTestAddUserToSubscriptionOK() throws Exception {
        // Get subscription
        Subscription sub = loadSubscription("testAddUserToSubscriptionOK",
                testOrganizations.get(0).getKey());
        PlatformUser usr = testUsers.get(testOrganizations.get(0)).get(3);
        // call method
        subMgmtLocal.addUserToSubscription(sub, usr, null);
    }

    private void checkAddUserToSubscriptionOK() throws Exception {
        // Get subscription
        Subscription sub = loadSubscription("testAddUserToSubscriptionOK",
                testOrganizations.get(0).getKey());
        assertEquals("Number of assigned users", 3,
                sub.getUsageLicenses().size());
    }

    @Test(expected = UserAlreadyAssignedException.class)
    public void testAddUserToSubscriptionAlreadyAssigned() throws Throwable {
        createAvailablePayment(testOrganizations.get(0));
        VOService product = getProductToSubscribe(testProducts.get(1).getKey());
        VOUser[] users = new VOUser[1];
        VOUser[] admins = new VOUser[1];
        admins[0] = UserDataAssembler
                .toVOUser(testUsers.get(testOrganizations.get(0)).get(1));
        users[0] = UserDataAssembler
                .toVOUser(testUsers.get(testOrganizations.get(0)).get(2));
        String subscriptionId = "testAddUserToSubscriptionAlreadyAssigned";
        VOPaymentInfo voPaymentInfo = getPaymentInfo(
                tpAndSupplier.getOrganizationId(), testOrganizations.get(0));
        VOBillingContact bc = createBillingContact(testOrganizations.get(0));
        subMgmt.subscribeToService(
                Subscriptions.createVOSubscription(subscriptionId), product,
                getUsersToAdd(admins, users), voPaymentInfo, bc,
                new ArrayList<VOUda>());
        // do the work (local interface, thus needs tx boundary)
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    doTestAddUserToSubscriptionAlreadyAssigned();
                    return null;
                }
            });
        } catch (Exception e) {
            throw e;
        }
    }

    private void doTestAddUserToSubscriptionAlreadyAssigned() throws Exception {
        // Get subscription
        Subscription sub = loadSubscription(
                "testAddUserToSubscriptionAlreadyAssigned",
                testOrganizations.get(0).getKey());
        PlatformUser usr = testUsers.get(testOrganizations.get(0)).get(2);
        // call method
        subMgmtLocal.addUserToSubscription(sub, usr, null);
        // as this is a local method the transaction will not be rolled back,
        // thus we do not have to check this !
    }

    @Test
    public void testRevokeUserFromSubscriptionOK() throws Throwable {
        createAvailablePayment(testOrganizations.get(0));
        VOService product = getProductToSubscribe(testProducts.get(1).getKey());
        VOUser[] users = new VOUser[1];
        VOUser[] admins = new VOUser[1];
        admins[0] = UserDataAssembler
                .toVOUser(testUsers.get(testOrganizations.get(0)).get(1));
        users[0] = UserDataAssembler
                .toVOUser(testUsers.get(testOrganizations.get(0)).get(2));
        String subscriptionId = "testRevokeUserFromSubscriptionOK";
        VOPaymentInfo voPaymentInfo = getPaymentInfo(
                tpAndSupplier.getOrganizationId(), testOrganizations.get(0));
        VOBillingContact bc = createBillingContact(testOrganizations.get(0));
        subMgmt.subscribeToService(
                Subscriptions.createVOSubscription(subscriptionId), product,
                getUsersToAdd(admins, users), voPaymentInfo, bc,
                new ArrayList<VOUda>());
        // do the work (local interface, thus needs tx boundary)
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    doTestRevokeUserToSubscriptionOK();
                    return null;
                }
            });
        } catch (Exception e) {
            throw e.getCause();
        }
        // check results
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    checkRevokeUserToSubscriptionOK();
                    return null;
                }
            });
        } catch (Exception e) {
            throw e.getCause();
        }
    }

    private void doTestRevokeUserToSubscriptionOK() throws Exception {
        // Get subscription
        Subscription sub = loadSubscription("testRevokeUserFromSubscriptionOK",
                testOrganizations.get(0).getKey());
        PlatformUser usr = testUsers.get(testOrganizations.get(0)).get(1);
        // call method
        List<PlatformUser> users = new ArrayList<>();
        users.add(usr);
        subMgmtLocal.revokeUserFromSubscription(sub, users);
    }

    private void checkRevokeUserToSubscriptionOK() throws Exception {
        // Get subscription
        Subscription sub = loadSubscription("testRevokeUserFromSubscriptionOK",
                testOrganizations.get(0).getKey());
        // still all users available
        // but with one of status revoked
        assertEquals("Number of assigned users", 1,
                sub.getUsageLicenses().size());
    }

    @Test
    public void testGrantAdminRoleOK() throws Throwable {
        createAvailablePayment(testOrganizations.get(0));
        VOService product = getProductToSubscribe(testProducts.get(1).getKey());
        VOUser[] users = new VOUser[1];
        VOUser[] admins = new VOUser[1];
        admins[0] = UserDataAssembler
                .toVOUser(testUsers.get(testOrganizations.get(0)).get(1));
        users[0] = UserDataAssembler
                .toVOUser(testUsers.get(testOrganizations.get(0)).get(2));
        String subscriptionId = "testGrantAdminRoleOK";
        VOPaymentInfo voPaymentInfo = getPaymentInfo(
                tpAndSupplier.getOrganizationId(), testOrganizations.get(0));
        VOBillingContact bc = createBillingContact(testOrganizations.get(0));
        subMgmt.subscribeToService(
                Subscriptions.createVOSubscription(subscriptionId), product,
                getUsersToAdd(admins, users), voPaymentInfo, bc,
                new ArrayList<VOUda>());
        // do the work (local interface, thus needs tx boundary)
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    doTestGrantAdminRoleOK();
                    return null;
                }
            });
        } catch (Exception e) {
            throw e.getCause();
        }
        // check results
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    checkGrantAdminRoleOK();
                    return null;
                }
            });
        } catch (Exception e) {
            throw e.getCause();
        }
    }

    private void doTestGrantAdminRoleOK() throws Exception {
        // Get subscription
        Subscription sub = loadSubscription("testGrantAdminRoleOK",
                testOrganizations.get(0).getKey());
        PlatformUser usr = testUsers.get(testOrganizations.get(0)).get(2);
        // call method
        subMgmtLocal.modifyUserRole(sub, usr, null);
    }

    private void checkGrantAdminRoleOK() throws Exception {
        // Get subscription
        Subscription sub = loadSubscription("testGrantAdminRoleOK",
                testOrganizations.get(0).getKey());
        assertEquals("Number of assigned users", 2,
                sub.getUsageLicenses().size());
    }

    @Test(expected = UserNotAssignedException.class)
    public void testGrantAdminRoleNotAssigned() throws Throwable {
        createAvailablePayment(testOrganizations.get(0));
        VOService product = getProductToSubscribe(testProducts.get(1).getKey());
        VOUser[] users = new VOUser[1];
        VOUser[] admins = new VOUser[1];
        admins[0] = UserDataAssembler
                .toVOUser(testUsers.get(testOrganizations.get(0)).get(1));
        users[0] = UserDataAssembler
                .toVOUser(testUsers.get(testOrganizations.get(0)).get(2));
        String subscriptionId = "testGrantAdminRoleNotAssigned";
        VOPaymentInfo voPaymentInfo = getPaymentInfo(
                tpAndSupplier.getOrganizationId(), testOrganizations.get(0));
        VOBillingContact bc = createBillingContact(testOrganizations.get(0));
        subMgmt.subscribeToService(
                Subscriptions.createVOSubscription(subscriptionId), product,
                getUsersToAdd(admins, users), voPaymentInfo, bc,
                new ArrayList<VOUda>());
        // do the work (local interface, thus needs tx boundary)
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    doTestGrantAdminRoleNotAssigned();
                    return null;
                }
            });
        } catch (Exception e) {
            throw e;
        }
    }

    private void doTestGrantAdminRoleNotAssigned() throws Exception {
        // Get subscription
        Subscription sub = loadSubscription("testGrantAdminRoleNotAssigned",
                testOrganizations.get(0).getKey());
        PlatformUser usr = testUsers.get(testOrganizations.get(0)).get(3);
        // call method
        subMgmtLocal.modifyUserRole(sub, usr, null);
        // as this is a local method the transaction will not be rolled back,
        // thus we do not have to check this !
    }

    @Test
    public void testRevokeAdminRoleOK() throws Throwable {
        createAvailablePayment(testOrganizations.get(0));
        VOService product = getProductToSubscribe(testProducts.get(1).getKey());
        VOUser[] users = new VOUser[1];
        VOUser[] admins = new VOUser[1];
        admins[0] = UserDataAssembler
                .toVOUser(testUsers.get(testOrganizations.get(0)).get(1));
        users[0] = UserDataAssembler
                .toVOUser(testUsers.get(testOrganizations.get(0)).get(2));
        String subscriptionId = "testRevokeAdminRoleOK";
        VOPaymentInfo voPaymentInfo = getPaymentInfo(
                tpAndSupplier.getOrganizationId(), testOrganizations.get(0));
        VOBillingContact bc = createBillingContact(testOrganizations.get(0));
        subMgmt.subscribeToService(
                Subscriptions.createVOSubscription(subscriptionId), product,
                getUsersToAdd(admins, users), voPaymentInfo, bc,
                new ArrayList<VOUda>());
        // do the work (local interface, thus needs tx boundary)
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    doTestRevokeAdminRoleOK();
                    return null;
                }
            });
        } catch (Exception e) {
            throw e.getCause();
        }
        // check results
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    checkRevokeAdminRoleOK();
                    return null;
                }
            });
        } catch (Exception e) {
            throw e.getCause();
        }
    }

    private void doTestRevokeAdminRoleOK() throws Exception {
        // Get subscription
        Subscription sub = loadSubscription("testRevokeAdminRoleOK",
                testOrganizations.get(0).getKey());
        PlatformUser usr = testUsers.get(testOrganizations.get(0)).get(1);
        // call method
        subMgmtLocal.modifyUserRole(sub, usr, null);
    }

    private void checkRevokeAdminRoleOK() throws Exception {
        // Get subscription
        Subscription sub = loadSubscription("testRevokeAdminRoleOK",
                testOrganizations.get(0).getKey());
        assertEquals("Number of assigned users", 2,
                sub.getUsageLicenses().size());
    }

    @Test(expected = UserNotAssignedException.class)
    public void testRevokeAdminRoleNotAssigned() throws Throwable {
        createAvailablePayment(testOrganizations.get(0));
        VOService product = getProductToSubscribe(testProducts.get(1).getKey());
        VOUser[] users = new VOUser[1];
        VOUser[] admins = new VOUser[1];
        admins[0] = UserDataAssembler
                .toVOUser(testUsers.get(testOrganizations.get(0)).get(1));
        users[0] = UserDataAssembler
                .toVOUser(testUsers.get(testOrganizations.get(0)).get(2));
        String subscriptionId = "testRevokeAdminRoleNotAssigned";
        VOPaymentInfo voPaymentInfo = getPaymentInfo(
                tpAndSupplier.getOrganizationId(), testOrganizations.get(0));
        VOBillingContact bc = createBillingContact(testOrganizations.get(0));
        subMgmt.subscribeToService(
                Subscriptions.createVOSubscription(subscriptionId), product,
                getUsersToAdd(admins, users), voPaymentInfo, bc,
                new ArrayList<VOUda>());
        // do the work (local interface, thus needs tx boundary)
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    doTestRevokeAdminRoleNotAssigned();
                    return null;
                }
            });
        } catch (Exception e) {
            throw e;
        }
    }

    private void doTestRevokeAdminRoleNotAssigned() throws Exception {
        // Get subscription
        Subscription sub = loadSubscription("testRevokeAdminRoleNotAssigned",
                testOrganizations.get(0).getKey());
        PlatformUser usr = testUsers.get(testOrganizations.get(0)).get(3);
        // call method
        subMgmtLocal.modifyUserRole(sub, usr, null);
        // as this is a local method the transaction will not be rolled back,
        // thus we do not have to check this !
    }

    /**
     * Initialize test database with master data (products and price models)
     * 
     * @return The key of the created admin user.
     * 
     * @throws NonUniqueBusinessKeyException
     * @throws ObjectNotFoundException
     */
    private Long initMasterData()
            throws NonUniqueBusinessKeyException, ObjectNotFoundException {
        Long initialCustomerAdminKey = null;
        tpAndSupplier = Organizations.createOrganization(mgr,
                OrganizationRoleType.SUPPLIER,
                OrganizationRoleType.TECHNOLOGY_PROVIDER);
        mp = Marketplaces.ensureMarketplace(tpAndSupplier,
                tpAndSupplier.getOrganizationId(), mgr);
        Marketplaces.grantPublishing(tpAndSupplier, mp, mgr, false);
        supplierOrgId = tpAndSupplier.getOrganizationId();
        // insert some products
        TechnicalProduct tProd = TechnicalProducts.createTechnicalProduct(mgr,
                tpAndSupplier, "TP_ID", false, ServiceAccessType.LOGIN);
        prepareTechnicalProduct(tProd);
        SupportedCurrency sc = new SupportedCurrency();
        sc.setCurrency(Currency.getInstance("EUR"));
        mgr.persist(sc);
        addProducts(tpAndSupplier, tProd, 13, testProducts, mp);
        ProductReference pref;
        pref = new ProductReference(testProducts.get(10), testProducts.get(11));
        mgr.persist(pref);
        pref = new ProductReference(testProducts.get(10), testProducts.get(12));
        mgr.persist(pref);
        tProd = TechnicalProducts.createTechnicalProduct(mgr, tpAndSupplier,
                "TP_ID_ASYNC", true, ServiceAccessType.LOGIN);
        prepareTechnicalProduct(tProd);
        addProducts(tpAndSupplier, tProd, 2, asyncTestProducts, mp);

        // insert some organizations with users
        Organization cust = null;
        for (int i = 1; i <= 2; i++) {
            cust = Organizations.createOrganization(mgr,
                    OrganizationRoleType.CUSTOMER);
            OrganizationReference ref = new OrganizationReference(tpAndSupplier,
                    cust, OrganizationReferenceType.SUPPLIER_TO_CUSTOMER);
            mgr.persist(ref);
            testOrganizations.add(cust);
            ArrayList<PlatformUser> userlist = new ArrayList<>();
            testUsers.put(cust, userlist);
            // add a organization admin
            PlatformUser admin = Organizations.createUserForOrg(mgr, cust, true,
                    "admin");
            if (initialCustomerAdminKey == null) {
                initialCustomerAdminKey = Long.valueOf(admin.getKey());
            }
            userlist.add((PlatformUser) ReflectiveClone.clone(admin));
            // add some users for the organization
            for (int j = 1; j <= 5; j++) {
                PlatformUser user = Organizations.createUserForOrg(mgr, cust,
                        false, "user" + j);
                userlist.add((PlatformUser) ReflectiveClone.clone(user));
            }

            // create units
            UserGroup unit1 = new UserGroup();
            unit1.setName(UNIT1 + cust.getOrganizationId());
            unit1.setOrganization_tkey(cust.getKey());
            unit1.setOrganization(cust);
            mgr.persist(unit1);

            UserGroup unit2 = new UserGroup();
            unit2.setName(UNIT2 + cust.getOrganizationId());
            unit2.setOrganization_tkey(cust.getKey());
            unit2.setOrganization(cust);
            mgr.persist(unit2);
        }
        // organization admin of the technical product's owner for notification
        supplierUser = Organizations.createUserForOrg(mgr, tpAndSupplier, true,
                "admin");
        UserGroup unit_supplier = new UserGroup();
        unit_supplier
                .setName(UNIT_OTHER_ORG + tpAndSupplier.getOrganizationId());
        unit_supplier.setOrganization_tkey(tpAndSupplier.getKey());
        unit_supplier.setOrganization(tpAndSupplier);
        mgr.persist(unit_supplier);
        return initialCustomerAdminKey;
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

    private void addProducts(Organization supplier, TechnicalProduct tProd,
            int count, List<Product> products,
            org.oscm.domobjects.Marketplace mp)
            throws NonUniqueBusinessKeyException, ObjectNotFoundException {
        Product prod;
        ParameterDefinition paramDef = tProd.getParameterDefinitions().get(0);
        for (int i = 1; i <= count; i++) {
            prod = Products.createProduct(supplier, tProd, (i % 2 != 1),
                    getProductId(tProd, i), null, mp, mgr);

            PaymentTypes.enableForProduct(prod, mgr, PaymentType.INVOICE,
                    PaymentType.CREDIT_CARD, PaymentType.DIRECT_DEBIT);

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
    }

    private String getProductId(TechnicalProduct tProd, int i) {
        String result = "Product" + i;
        if (tProd.getProvisioningType() == ProvisioningType.ASYNCHRONOUS) {
            result += tProd.getProvisioningType().name();
        }
        return result;
    }

    @Test
    public void testExpireOverdueSubscriptionsForTime() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                doSetParameterSetForProduct("Product1", false,
                        parameterValueConstant);
                return null;
            }
        });
        VOService product = findProduct(testProducts.get(0).getKey());
        VOUser[] users = new VOUser[2];
        VOUser[] admins = new VOUser[1];
        setUsers(users, admins);
        subMgmt.subscribeToService(
                Subscriptions.createVOSubscription("expirationTest1"), product,
                getUsersToAdd(admins, users), null, null,
                new ArrayList<VOUda>());

        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                long initialTransactionTime = DateFactory.getInstance()
                        .getTransactionTime();
                subMgmtLocal.expireOverdueSubscriptions(
                        System.currentTimeMillis() + 2000);
                assertFalse("Transaction time not set",
                        initialTransactionTime == DateFactory.getInstance()
                                .getTransactionTime());
                return null;
            }
        });
        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                doCheckSubscriptionStatus("expirationTest1",
                        testOrganizations.get(0).getKey(),
                        SubscriptionStatus.EXPIRED);
                return null;
            }

        });
    }

    @Test
    public void testExpireSubscription() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                doSetParameterSetForProduct("Product2", false,
                        parameterValueConstant);
                return null;
            }
        });
        createAvailablePayment(testOrganizations.get(0));
        VOService product = findProduct(testProducts.get(1).getKey());
        VOUser[] users = new VOUser[2];
        VOUser[] admins = new VOUser[1];
        setUsers(users, admins);
        VOPaymentInfo voPaymentInfo = getPaymentInfo(
                tpAndSupplier.getOrganizationId(), testOrganizations.get(0));
        VOBillingContact bc = createBillingContact(testOrganizations.get(0));
        subMgmt.subscribeToService(
                Subscriptions.createVOSubscription("expirationTest2"), product,
                getUsersToAdd(admins, users), voPaymentInfo, bc,
                new ArrayList<VOUda>());
        Subscription sub = runTX(new Callable<Subscription>() {
            @Override
            public Subscription call() {
                return getSubscription("expirationTest2",
                        testOrganizations.get(0).getKey());
            }
        });

        assertNotNull("Subscription not found", sub);
        subMgmtLocal.expireSubscription(sub);

        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                doCheckSubscriptionStatus("expirationTest2",
                        testOrganizations.get(0).getKey(),
                        SubscriptionStatus.EXPIRED);
                return null;
            }
        });

        // BE08022 Now the application service is not deleted when the
        // subscription expires, because the subscription may be be
        // upgraded afterwards, what results in a reactivation
        // of the application service
        assertFalse("Product has been deleted when the subscription expired",
                appMgmtStub.isProductDeleted);
    }

    @Test
    public void testExpireOverdueSubscriptionsForTimeOneOutOfTwo()
            throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                doSetParameterSetForProduct("Product1", false, null);
                doSetParameterSetForProduct("Product3", false,
                        parameterValueConstant);
                return null;
            }
        });
        VOService product = findProduct(testProducts.get(0).getKey());
        VOUser[] users = new VOUser[2];
        VOUser[] admins = new VOUser[1];
        setUsers(users, admins);
        subMgmt.subscribeToService(
                Subscriptions.createVOSubscription("expirationTest3"), product,
                getUsersToAdd(admins, users), null, null,
                new ArrayList<VOUda>());
        product = findProduct(testProducts.get(2).getKey());
        subMgmt.subscribeToService(
                Subscriptions.createVOSubscription("expirationTest4"), product,
                getUsersToAdd(admins, users), null, null,
                new ArrayList<VOUda>());

        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                doChangeActivationDateForSubscription("expirationTest4",
                        testOrganizations.get(0).getKey());
                return null;
            }
        });

        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                subMgmtLocal.expireOverdueSubscriptions(
                        System.currentTimeMillis() + 4000);
                return null;
            }
        });
        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                doCheckSubscriptionStatus("expirationTest4",
                        testOrganizations.get(0).getKey(),
                        SubscriptionStatus.EXPIRED);
                doCheckSubscriptionStatus("expirationTest3",
                        testOrganizations.get(0).getKey(),
                        SubscriptionStatus.ACTIVE);
                return null;
            }

        });
    }

    @Test
    public void testExpireOverdueSubscriptionAppStopFails() throws Exception {
        appMgmtStub.throwProductOperationFailed = true;
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                doSetParameterSetForProduct("Product2", false,
                        parameterValueConstant);
                return null;
            }
        });
        createAvailablePayment(testOrganizations.get(0));
        VOService product = findProduct(testProducts.get(1).getKey());
        VOUser[] users = new VOUser[2];
        VOUser[] admins = new VOUser[1];
        setUsers(users, admins);
        VOPaymentInfo voPaymentInfo = getPaymentInfo(
                tpAndSupplier.getOrganizationId(), testOrganizations.get(0));
        VOBillingContact bc = createBillingContact(testOrganizations.get(0));
        subMgmt.subscribeToService(
                Subscriptions.createVOSubscription("expirationTest5"), product,
                getUsersToAdd(admins, users), voPaymentInfo, bc,
                new ArrayList<VOUda>());
        final Subscription sub = runTX(new Callable<Subscription>() {
            @Override
            public Subscription call() {
                return getSubscription("expirationTest5",
                        testOrganizations.get(0).getKey());
            }
        });

        assertNotNull("Subscription not found", sub);
        // in bad case, execute the expiration call in a transaction as it would
        // happen in the container
        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                subMgmtLocal.expireSubscription(sub);
                return null;
            }
        });

        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                doCheckSubscriptionStatus("expirationTest5",
                        testOrganizations.get(0).getKey(),
                        SubscriptionStatus.EXPIRED);
                return null;
            }
        });
    }

    private void doChangeActivationDateForSubscription(String subscriptionId,
            long organizationKey) {
        Subscription subscription = getSubscription(subscriptionId,
                organizationKey);
        subscription.setActivationDate(Long.valueOf(
                subscription.getActivationDate().longValue() - 3600000L));
    }

    private Subscription getSubscription(String subscriptionId,
            long organizationKey) {
        Subscription template = new Subscription();
        template.setOrganizationKey(organizationKey);
        template.setSubscriptionId(subscriptionId);
        return (Subscription) mgr.find(template);
    }

    private void doCheckSubscriptionStatus(String subscriptionId,
            long organizationKey, SubscriptionStatus desiredStatus) {
        Subscription sub = getSubscription(subscriptionId, organizationKey);
        assertNotNull("Subscription not found", sub);
        assertEquals("Subscription has wrong state", desiredStatus,
                sub.getStatus());

        List<DomainHistoryObject<?>> history = mgr.findHistory(sub);
        SubscriptionHistory domainHistoryObject = (SubscriptionHistory) history
                .get(history.size() - 1);
        assertEquals(
                "Wrong user for expiration", String.valueOf(testUsers
                        .get(testOrganizations.get(0)).get(0).getKey()),
                domainHistoryObject.getModuser());
        assertEquals("Wrong status after expiration", desiredStatus,
                domainHistoryObject.getDataContainer().getStatus());
        assertEquals("Wrong type for expiration", ModificationType.MODIFY,
                domainHistoryObject.getModtype());
    }

    private void doSetParameterSetForProduct(String productId,
            boolean isConfigurable, String parameterValue)
            throws NonUniqueBusinessKeyException {
        Product prod = new Product();
        prod.setVendor(tpAndSupplier);
        prod.setProductId(productId);
        prod = (Product) mgr.find(prod);
        assertNotNull("Required product could not be found.", prod);

        // Get/create Parameter definition
        Query query = mgr.createNamedQuery(
                "ParameterDefinition.getPlatformParameterDefinition");
        query.setParameter("parameterType", ParameterType.PLATFORM_PARAMETER);
        query.setParameter("parameterId", PlatformParameterIdentifiers.PERIOD);

        ParameterDefinition paramDef = null;
        try {
            paramDef = (ParameterDefinition) query.getSingleResult();
        } catch (NoResultException e) {
            paramDef = new ParameterDefinition();
            paramDef.setParameterType(ParameterType.PLATFORM_PARAMETER);
            paramDef.setParameterId(PlatformParameterIdentifiers.PERIOD);
            paramDef.setValueType(ParameterValueType.DURATION);
            mgr.persist(paramDef);
        }

        // Create Parameterset and parameters
        ParameterSet params = prod.getParameterSet();
        if (params == null) {
            params = new ParameterSet();
            prod.setParameterSet(params);
            mgr.flush();
        }

        Parameter period = new Parameter();
        period.setParameterDefinition(paramDef);
        period.setValue(parameterValue);
        period.setParameterSet(params);

        period.setConfigurable(isConfigurable);

        mgr.persist(period);
    }

    private Subscription loadSubscription(final String subscriptionId,
            final long organizationKey) throws ObjectNotFoundException {
        Subscription qry = new Subscription();
        qry.setSubscriptionId(subscriptionId);
        qry.setOrganizationKey(organizationKey);
        Subscription result = (Subscription) mgr.getReferenceByBusinessKey(qry);
        return result;
    }

    @Test
    public void testCompletion() throws Throwable {
        final String id = "testCompletion";
        subscribeAsync(id, 0, false);
        String orgId = testOrganizations.get(0).getOrganizationId();

        // login as technology provider
        container.login(String.valueOf(supplierUser.getKey()),
                ROLE_TECHNOLOGY_MANAGER);
        VOInstanceInfo instanceInfo = new VOInstanceInfo();
        instanceInfo.setInstanceId("completionProductInstanceId");
        instanceInfo.setAccessInfo(null);
        ConfigurationServiceLocal cfg = container
                .get(ConfigurationServiceLocal.class);
        final String baseUrl = cfg
                .getConfigurationSetting(ConfigurationKey.BASE_URL,
                        Configuration.GLOBAL_CONTEXT)
                .getValue();

        receivedSendMailPayload = new ArrayList<>();
        subMgmt.completeAsyncSubscription(id, orgId, instanceInfo);
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() {
                    checkSubscribeToProduct(false, id, asyncTestProducts.get(0),
                            SubscriptionStatus.ACTIVE, 3, null, 1);
                    String param1 = (String) receivedSendMailPayload.get(0)
                            .getMailObjects().get(0).getParams()[1];
                    assertNotNull(param1);
                    assertTrue(param1.startsWith(baseUrl));
                    return null;
                }
            });
        } catch (Exception e) {
            throw e.getCause();
        }
    }

    @Test
    public void testCompletionFreeService_AndRetest_B7842() throws Throwable {
        final String id = "testCompletion";
        subscribeAsync(id, 0, true);
        String orgId = testOrganizations.get(0).getOrganizationId();

        // login as technology provider
        container.login(String.valueOf(supplierUser.getKey()),
                ROLE_TECHNOLOGY_MANAGER);
        VOInstanceInfo instanceInfo = new VOInstanceInfo();
        instanceInfo.setInstanceId("completionProductInstanceId");

        // get just created subscription for getting correct subscription key
        final Subscription subscription = runTX(new Callable<Subscription>() {
            @Override
            public Subscription call() {
                Subscription returnSubscription = getSubscription(id,
                        testOrganizations.get(0).getKey());
                load(returnSubscription.getProduct());
                return returnSubscription;
            }
        });

        final String accessUrl = BASE_URL_BES_HTTP + "/opt/"
                + Long.toHexString(subscription.getKey()) + "/";

        final String accessInfo = "http://www.google.de";
        instanceInfo.setAccessInfo(accessInfo);
        receivedSendMailPayload = new ArrayList<>();
        subMgmt.completeAsyncSubscription(id, orgId, instanceInfo);
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() {
                    checkSubscribeToProduct(false, id, asyncTestProducts.get(0),
                            SubscriptionStatus.ACTIVE, 3, null, 1);
                    return null;
                }
            });
        } catch (Exception e) {
            throw e.getCause();
        }
        String param1 = (String) receivedSendMailPayload.get(0).getMailObjects()
                .get(0).getParams()[1];
        assertEquals(accessUrl, param1);
    }

    @Test
    public void testAbort() throws Throwable {
        final String id = "testAbort";
        subscribeAsync(id, 0, false);
        String orgId = testOrganizations.get(0).getOrganizationId();

        // login as technology provider
        container.login(String.valueOf(supplierUser.getKey()),
                ROLE_TECHNOLOGY_MANAGER);
        subMgmt.abortAsyncSubscription(id, orgId, null);
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() {
                    checkSubscribeToProduct(true, id, asyncTestProducts.get(0),
                            SubscriptionStatus.INVALID, 3, null, 1);
                    return null;
                }
            });
        } catch (Exception e) {
            throw e.getCause();
        }
    }

    @Test
    public void testCompletionWithInvalidPaymentInfo() throws Throwable {
        final String id = "testCompletion";
        createAvailablePayment(testOrganizations.get(0));

        subscribeAsync(id, 1, false);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                // Change the payment type of the PI to one which is not used
                // for the supplier
                Organization org = mgr.getReference(Organization.class,
                        testOrganizations.get(1).getKey());
                PaymentInfo pi = mgr.getReference(PaymentInfo.class,
                        voPaymentInfos.get(tpAndSupplier.getOrganizationId())
                                .getKey());
                OrganizationRole role = new OrganizationRole();
                role.setRoleName(OrganizationRoleType.CUSTOMER);
                role = (OrganizationRole) mgr.getReferenceByBusinessKey(role);
                OrganizationRefToPaymentType apt = new OrganizationRefToPaymentType();
                apt.setOrganizationReference(org.getSources().get(0));
                PaymentType paymentType = new PaymentType();
                paymentType
                        .setCollectionType(PaymentCollectionType.ORGANIZATION);
                paymentType.setPaymentTypeId("newPaymentType");
                PSP template = new PSP();
                template.setIdentifier("pspIdentifier");
                template = (PSP) mgr.getReferenceByBusinessKey(template);
                paymentType.setPsp(template);
                mgr.persist(paymentType);
                apt.setPaymentType(paymentType);
                apt.setOrganizationRole(role);
                apt.setUsedAsDefault(false);
                mgr.persist(apt);
                pi.setOrganization(apt.getAffectedOrganization());
                pi.setPaymentType(paymentType);
                return null;
            }
        });

        String orgId = testOrganizations.get(0).getOrganizationId();

        // login as technology provider
        container.login(String.valueOf(supplierUser.getKey()),
                ROLE_TECHNOLOGY_MANAGER);
        VOInstanceInfo instanceInfo = new VOInstanceInfo();
        instanceInfo.setInstanceId("completionProductInstanceId");
        subMgmt.completeAsyncSubscription(id, orgId, instanceInfo);
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() {
                    checkSubscribeToProduct(false, id, asyncTestProducts.get(1),
                            SubscriptionStatus.SUSPENDED, 3, null, 1);
                    return null;
                }
            });
        } catch (Exception e) {
            throw e.getCause();
        }
        assertTrue(appMgmtStub.deactivated);
        assertFalse(appMgmtStub.activated);
    }

    @Test
    public void testCompletionWithPaymentTypeForProduct() throws Throwable {
        final String id = "testCompletion2";
        createAvailablePayment(testOrganizations.get(0));

        subscribeAsync(id, 1, false);
        // service has all payment types by default => subscription should be
        // active directly

        String orgId = testOrganizations.get(0).getOrganizationId();

        // login as technology provider
        container.login(String.valueOf(supplierUser.getKey()),
                ROLE_TECHNOLOGY_MANAGER);
        VOInstanceInfo instanceInfo = new VOInstanceInfo();
        instanceInfo.setInstanceId("completionProductInstanceId");
        subMgmt.completeAsyncSubscription(id, orgId, instanceInfo);
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() {
                    checkSubscribeToProduct(false, id, asyncTestProducts.get(1),
                            SubscriptionStatus.ACTIVE, 3, null, 1);
                    return null;
                }
            });
        } catch (Exception e) {
            throw e.getCause();
        }
        assertFalse(appMgmtStub.activated);
        assertFalse(appMgmtStub.deactivated);
    }

    @Test
    public void testCompletionWithWrongPaymentTypeForProduct()
            throws Throwable {
        final String id = "testCompletion3";
        createAvailablePayment(testOrganizations.get(0));

        subscribeAsync(id, 1, false);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Product product = asyncTestProducts.get(1);
                product = mgr.getReference(Product.class, product.getKey());
                for (ProductToPaymentType ptpt : product.getPaymentTypes()) {
                    mgr.remove(ptpt);
                }
                return null;
            }
        });

        String orgId = testOrganizations.get(0).getOrganizationId();

        // login as technology provider
        container.login(String.valueOf(supplierUser.getKey()),
                ROLE_TECHNOLOGY_MANAGER);
        VOInstanceInfo instanceInfo = new VOInstanceInfo();
        instanceInfo.setInstanceId("completionProductInstanceId");
        subMgmt.completeAsyncSubscription(id, orgId, instanceInfo);
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() {
                    checkSubscribeToProduct(false, id, asyncTestProducts.get(1),
                            SubscriptionStatus.SUSPENDED, 3, null, 1);
                    return null;
                }
            });
        } catch (Exception e) {
            throw e.getCause();
        }
        assertTrue(appMgmtStub.deactivated);
        assertFalse(appMgmtStub.activated);
    }

    @Test
    public void testCompletionWithAddingUsers() throws Throwable {
        final String id = "testCompletionWithAddingUsers";
        subscribeAsync(id, 0, false);
        List<VOUsageLicense> addUsers = new ArrayList<>();
        addUsers.add(
                mapUserToRole(testUsers.get(testOrganizations.get(0)).get(4)));
        addUsers.add(
                mapUserToRole(testUsers.get(testOrganizations.get(0)).get(5)));
        List<VOUser> removedUsers = new ArrayList<>();
        subMgmt.addRevokeUser(id, addUsers, removedUsers);

        // login as technology provider
        container.login(String.valueOf(supplierUser.getKey()),
                ROLE_TECHNOLOGY_MANAGER);
        VOInstanceInfo instanceInfo = new VOInstanceInfo();
        instanceInfo.setInstanceId("completionProductInstanceId");
        String orgId = testOrganizations.get(0).getOrganizationId();
        subMgmt.completeAsyncSubscription(id, orgId, instanceInfo);
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() {
                    checkSubscribeToProduct(false, id, asyncTestProducts.get(0),
                            SubscriptionStatus.ACTIVE, 5, null, 1);
                    return null;
                }
            });
        } catch (Exception e) {
            throw e.getCause();
        }
    }

    @Test(expected = ValidationException.class)
    public void testCompleteAsyncSubscription_ReturnedInstanceIdNull()
            throws Throwable {
        final String id = "testCompletion";
        subscribeAsync(id, 0, false);
        String orgId = testOrganizations.get(0).getOrganizationId();
        // login as technology provider
        container.login(String.valueOf(supplierUser.getKey()),
                ROLE_TECHNOLOGY_MANAGER);
        try {
            subMgmt.completeAsyncSubscription(id, orgId,
                    createInstanceInfo(null, null, null, null));
        } catch (TechnicalServiceOperationException e) {
            throw e.getCause();
        }
    }

    @Test(expected = ValidationException.class)
    public void testCompleteAsyncSubscription_ReturnedInstanceIdToLong()
            throws Throwable {
        final String id = "testCompletion";
        subscribeAsync(id, 0, false);
        String orgId = testOrganizations.get(0).getOrganizationId();
        // login as technology provider
        container.login(String.valueOf(supplierUser.getKey()),
                ROLE_TECHNOLOGY_MANAGER);
        try {
            subMgmt.completeAsyncSubscription(id, orgId, createInstanceInfo(
                    null, null, BaseAdmUmTest.TOO_LONG_DESCRIPTION, null));
        } catch (TechnicalServiceOperationException e) {
            throw e.getCause();
        }
    }

    @Test(expected = ValidationException.class)
    public void testCompleteAsyncSubscription_ReturnedBaseUrlInvalid()
            throws Throwable {
        final String id = "testCompletion";
        subscribeAsync(id, 0, false);
        String orgId = testOrganizations.get(0).getOrganizationId();
        // login as technology provider
        container.login(String.valueOf(supplierUser.getKey()),
                ROLE_TECHNOLOGY_MANAGER);
        try {
            subMgmt.completeAsyncSubscription(id, orgId, createInstanceInfo(
                    null, "some invalid url", "id", LOGIN_PATH));
        } catch (TechnicalServiceOperationException e) {
            throw e.getCause();
        }
    }

    @Test(expected = ValidationException.class)
    public void testCompleteAsyncSubscription_ReturnedBaseUrlToLong()
            throws Throwable {
        final String id = "testCompletion";
        subscribeAsync(id, 0, false);
        String orgId = testOrganizations.get(0).getOrganizationId();
        // login as technology provider
        container.login(String.valueOf(supplierUser.getKey()),
                ROLE_TECHNOLOGY_MANAGER);
        try {
            subMgmt.completeAsyncSubscription(id, orgId,
                    createInstanceInfo(null, TOO_LONG_URL, "id", LOGIN_PATH));
        } catch (TechnicalServiceOperationException e) {
            throw e.getCause();
        }
    }

    @Test
    public void testCompleteAsyncSubscription() throws Throwable {
        final String id = "testCompletion";
        subscribeAsync(id, 0, false);
        String orgId = testOrganizations.get(0).getOrganizationId();
        // login as technology provider
        container.login(String.valueOf(supplierUser.getKey()),
                ROLE_TECHNOLOGY_MANAGER);
        try {
            subMgmt.completeAsyncSubscription(id, orgId, createInstanceInfo(
                    "access info", BASE_URL_SERVICE_HTTP, "id", LOGIN_PATH));
        } catch (TechnicalServiceOperationException e) {
            throw e.getCause();
        }
    }

    @Test(expected = ValidationException.class)
    public void testCompleteAsyncSubscription_ReturnedAccessInfoToLong()
            throws Throwable {
        final String id = "testCompletion";
        subscribeAsync(id, 0, false);
        String orgId = testOrganizations.get(0).getOrganizationId();
        // login as technology provider
        container.login(String.valueOf(supplierUser.getKey()),
                ROLE_TECHNOLOGY_MANAGER);
        try {
            subMgmt.completeAsyncSubscription(id, orgId,
                    createInstanceInfo(
                            BaseAdmUmTest.TOO_LONG_DESCRIPTION
                                    + BaseAdmUmTest.TOO_LONG_DESCRIPTION
                                    + BaseAdmUmTest.TOO_LONG_DESCRIPTION
                                    + BaseAdmUmTest.TOO_LONG_DESCRIPTION
                                    + BaseAdmUmTest.TOO_LONG_DESCRIPTION
                                    + BaseAdmUmTest.TOO_LONG_DESCRIPTION
                                    + BaseAdmUmTest.TOO_LONG_DESCRIPTION
                                    + BaseAdmUmTest.TOO_LONG_DESCRIPTION
                                    + BaseAdmUmTest.TOO_LONG_DESCRIPTION
                                    + BaseAdmUmTest.TOO_LONG_DESCRIPTION
                                    + BaseAdmUmTest.TOO_LONG_DESCRIPTION
                                    + BaseAdmUmTest.TOO_LONG_DESCRIPTION
                                    + BaseAdmUmTest.TOO_LONG_DESCRIPTION
                                    + BaseAdmUmTest.TOO_LONG_DESCRIPTION
                                    + BaseAdmUmTest.TOO_LONG_DESCRIPTION
                                    + BaseAdmUmTest.TOO_LONG_DESCRIPTION + "1",
                            null, "id", null));
        } catch (TechnicalServiceOperationException e) {
            throw e.getCause();
        }
    }

    @Test(expected = ValidationException.class)
    public void testCompleteAsyncSubscription_ReturnedLoginPathToLong()
            throws Throwable {
        final String id = "testCompletion";
        subscribeAsync(id, 0, false);
        String orgId = testOrganizations.get(0).getOrganizationId();
        // login as technology provider
        container.login(String.valueOf(supplierUser.getKey()),
                ROLE_TECHNOLOGY_MANAGER);
        try {
            subMgmt.completeAsyncSubscription(id, orgId,
                    createInstanceInfo(null, BASE_URL_SERVICE_HTTP, "id",
                            BaseAdmUmTest.TOO_LONG_DESCRIPTION));
        } catch (TechnicalServiceOperationException e) {
            throw e.getCause();
        }
    }

    @Test(expected = ValidationException.class)
    public void testCompleteAsyncSubscription_ReturnedLoginPathWithoutBaseUrl()
            throws Throwable {
        final String id = "testCompletion";
        subscribeAsync(id, 0, false);
        String orgId = testOrganizations.get(0).getOrganizationId();
        // login as technology provider
        container.login(String.valueOf(supplierUser.getKey()),
                ROLE_TECHNOLOGY_MANAGER);
        try {
            subMgmt.completeAsyncSubscription(id, orgId,
                    createInstanceInfo(null, null, "id", LOGIN_PATH));
        } catch (TechnicalServiceOperationException e) {
            throw e.getCause();
        }
    }

    @Test(expected = ValidationException.class)
    public void testCompleteAsyncSubscription_ReturnedBaseUrlWithoutLoginPath()
            throws Throwable {
        final String id = "testCompletion";
        subscribeAsync(id, 0, false);
        String orgId = testOrganizations.get(0).getOrganizationId();
        // login as technology provider
        container.login(String.valueOf(supplierUser.getKey()),
                ROLE_TECHNOLOGY_MANAGER);
        try {
            subMgmt.completeAsyncSubscription(id, orgId, createInstanceInfo(
                    null, BASE_URL_SERVICE_HTTP, "id", null));
        } catch (TechnicalServiceOperationException e) {
            throw e.getCause();
        }
    }

    @Test
    public void testAbortWithAddingUsers() throws Throwable {
        final String id = "testAbortWithAddingUsers";
        subscribeAsync(id, 0, false);
        List<VOUsageLicense> addUsers = new ArrayList<>();
        addUsers.add(
                mapUserToRole(testUsers.get(testOrganizations.get(0)).get(4)));
        addUsers.add(
                mapUserToRole(testUsers.get(testOrganizations.get(0)).get(5)));
        String orgId = testOrganizations.get(0).getOrganizationId();
        List<VOUser> removedUsers = new ArrayList<>();
        subMgmt.addRevokeUser(id, addUsers, removedUsers);

        // login as technology provider
        container.login(String.valueOf(supplierUser.getKey()),
                ROLE_TECHNOLOGY_MANAGER);
        subMgmt.abortAsyncSubscription(id, orgId, null);
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() {
                    checkSubscribeToProduct(true, id, asyncTestProducts.get(0),
                            SubscriptionStatus.INVALID, 5, null, 1);
                    return null;
                }
            });
        } catch (Exception e) {
            throw e.getCause();
        }

    }

    @Test
    public void testUpdateAsyncSubscriptionProgress() throws Throwable {
        final String id = "updateProgress";
        subscribeAsync(id, 0, false);
        String orgId = testOrganizations.get(0).getOrganizationId();

        // login as technology provider
        container.login(String.valueOf(supplierUser.getKey()),
                ROLE_TECHNOLOGY_MANAGER);
        List<VOLocalizedText> list = new ArrayList<>();
        VOLocalizedText en = new VOLocalizedText();
        en.setLocale(Locale.ENGLISH.toString());
        en.setText("Text_" + Locale.ENGLISH.toString());
        list.add(en);
        VOLocalizedText de = new VOLocalizedText();
        de.setLocale(Locale.GERMAN.toString());
        de.setText("Text_" + Locale.GERMAN.toString());
        list.add(de);
        subMgmt.updateAsyncSubscriptionProgress(id, orgId, list);
        VOInstanceInfo instanceInfo = new VOInstanceInfo();
        instanceInfo.setInstanceId("updateProgressInstanceId");
        subMgmt.completeAsyncSubscription(id, orgId, instanceInfo);

        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        assertEquals(en.getText(),
                subMgmt.getSubscriptionDetails(id).getProvisioningProgress());
    }

    @Test
    public void testUpdateAsyncSubscriptionProgressListNull() throws Throwable {
        final String id = "updateProgress";
        subscribeAsync(id, 0, false);
        String orgId = testOrganizations.get(0).getOrganizationId();

        // login as technology provider
        container.login(String.valueOf(supplierUser.getKey()),
                ROLE_TECHNOLOGY_MANAGER);
        subMgmt.updateAsyncSubscriptionProgress(id, orgId, null);
        VOInstanceInfo instanceInfo = new VOInstanceInfo();
        instanceInfo.setInstanceId("updateProgressInstanceId");
        subMgmt.completeAsyncSubscription(id, orgId, instanceInfo);

        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        assertEquals("LocalizedTextFromDatabase",
                subMgmt.getSubscriptionDetails(id).getProvisioningProgress());
    }

    @Test(expected = javax.ejb.EJBException.class)
    public void testUpdateAsyncSubscriptionProgressNotPermitted()
            throws Throwable {
        final String id = "updateProgress";
        container.login(String.valueOf(supplierUser.getKey()),
                ROLE_TECHNOLOGY_MANAGER);
        subscribeAsync(id, 0, false);
        String orgId = testOrganizations.get(0).getOrganizationId();

        subMgmt.updateAsyncSubscriptionProgress(id, orgId,
                new ArrayList<VOLocalizedText>());
    }

    @Test(expected = SubscriptionStateException.class)
    public void testUpdateAsyncSubscriptionProgressInvalidState()
            throws Throwable {
        final String id = "updateProgress";
        subscribeAsync(id, 0, false);
        String orgId = testOrganizations.get(0).getOrganizationId();

        // login as technology provider
        container.login(String.valueOf(supplierUser.getKey()),
                ROLE_TECHNOLOGY_MANAGER);
        VOInstanceInfo instanceInfo = new VOInstanceInfo();
        instanceInfo.setInstanceId("updateProgressInstanceId");
        subMgmt.completeAsyncSubscription(id, orgId, instanceInfo);

        subMgmt.updateAsyncSubscriptionProgress(id, orgId, null);
    }

    private void subscribeAsync(final String id, final int indexInList,
            boolean noPaymentInfo) throws Throwable {
        assertNotNull(subMgmt);
        appMgmtStub.addedUsers.clear();
        appMgmtStub.deletedUsers.clear();
        VOService product = getProductToSubscribe(
                asyncTestProducts.get(indexInList).getKey());
        VOUser[] users = new VOUser[2];
        VOUser[] admins = new VOUser[1];
        setUsers(users, admins);
        VOPaymentInfo voPaymentInfo = getPaymentInfo(
                tpAndSupplier.getOrganizationId(), testOrganizations.get(0));
        VOBillingContact bc = createBillingContact(testOrganizations.get(0));
        VOSubscription subscription = subMgmt.subscribeToService(
                Subscriptions.createVOSubscription(id), product,
                getUsersToAdd(admins, users),
                noPaymentInfo ? null : voPaymentInfo, bc,
                new ArrayList<VOUda>());

        assertEquals("Subscription must be PENDING", subscription.getStatus(),
                SubscriptionStatus.PENDING);

        final long orgKey = testOrganizations.get(0).getKey();

        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {

                    assertProvisioningNotCompleted(id, orgKey);

                    checkSubscribeToProduct(true, id,
                            asyncTestProducts.get(indexInList),
                            SubscriptionStatus.PENDING, 3, null, 1);
                    return null;
                }

            });
        } catch (Exception e) {
            throw e.getCause();
        }
    }

    @Test
    public void testUnsubscribePending() throws Throwable {
        final String id = "testUnsubscribePending";
        subscribeAsync(id, 0, false);

        subMgmt.unsubscribeFromService(id);
    }

    @Test
    public void testUpgradePending() throws Throwable {
        final String id = "testUpgradePending";
        subscribeAsync(id, 0, false);
        VOSubscription subscription = new VOSubscription();
        subscription.setSubscriptionId(id);
        VOService product = new VOService();
        try {
            subMgmt.upgradeSubscription(subscription, product, null, null,
                    new ArrayList<VOUda>());
            fail("No SubscriptionStateException thrown");
        } catch (SubscriptionStateException e) {
            assertInvalidStateException(e, SubscriptionStatus.PENDING);
        }
    }

    @Test
    public void testUpgradeInvalid() throws Throwable {
        final String id = "testUpgradeInvalid";
        subscribeAsync(id, 0, false);
        String orgId = testOrganizations.get(0).getOrganizationId();

        // login as technology provider
        container.login(String.valueOf(supplierUser.getKey()),
                ROLE_TECHNOLOGY_MANAGER);
        subMgmt.abortAsyncSubscription(id, orgId, null);

        // login as customer
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        VOSubscription subscription = new VOSubscription();
        String inactiveId = runTX(new Callable<String>() {

            @Override
            public String call() {
                Subscription sub = findInvalidSubscription(id,
                        testOrganizations.get(0));
                return sub.getSubscriptionId();
            }
        });
        subscription.setSubscriptionId(inactiveId);
        VOService product = new VOService();
        try {
            subMgmt.upgradeSubscription(subscription, product, null, null,
                    new ArrayList<VOUda>());
            fail("No SubscriptionStateException thrown");
        } catch (SubscriptionStateException e) {
            assertInvalidStateException(e, SubscriptionStatus.INVALID);
        }
    }

    @Test
    public void testGrantAdminRoleInvalid() throws Throwable {
        final String id = "testGrantAdminRoleInvalid";
        subscribeAsync(id, 0, false);
        String orgId = testOrganizations.get(0).getOrganizationId();

        // login as technology provider
        container.login(String.valueOf(supplierUser.getKey()),
                ROLE_TECHNOLOGY_MANAGER);
        subMgmt.abortAsyncSubscription(id, orgId, null);
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    Subscription loadSubscription = findInvalidSubscription(id,
                            testOrganizations.get(0));
                    subMgmtLocal.modifyUserRole(loadSubscription,
                            testUsers.get(testOrganizations.get(0)).get(0),
                            null);
                    fail("No SubscriptionStateException thrown");
                    return null;
                }
            });
        } catch (SubscriptionStateException e) {
            assertInvalidStateException(e, SubscriptionStatus.INVALID);
        }
    }

    @Test
    public void testInformProductAboutNewUsersInvalid() throws Throwable {
        final String id = "testInformProductAboutNewUsersInvalid";
        subscribeAsync(id, 0, false);
        String orgId = testOrganizations.get(0).getOrganizationId();

        // login as technology provider
        container.login(String.valueOf(supplierUser.getKey()),
                ROLE_TECHNOLOGY_MANAGER);
        subMgmt.abortAsyncSubscription(id, orgId, null);
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    Subscription loadSubscription = findInvalidSubscription(id,
                            testOrganizations.get(0));
                    subMgmtLocal.informProductAboutNewUsers(loadSubscription,
                            testOrganizations.get(0).getPlatformUsers());
                    fail("No SubscriptionStateException thrown");
                    return null;
                }
            });
        } catch (SubscriptionStateException e) {
            assertInvalidStateException(e, SubscriptionStatus.INVALID);
        }
    }

    @Test
    public void testRevokeAdminRoleInvalid() throws Throwable {
        final String id = "testRevokeAdminRoleInvalid";
        subscribeAsync(id, 0, false);
        String orgId = testOrganizations.get(0).getOrganizationId();

        // login as technology provider
        container.login(String.valueOf(supplierUser.getKey()),
                ROLE_TECHNOLOGY_MANAGER);
        subMgmt.abortAsyncSubscription(id, orgId, null);
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    Subscription loadSubscription = findInvalidSubscription(id,
                            testOrganizations.get(0));
                    subMgmtLocal.modifyUserRole(loadSubscription,
                            testUsers.get(testOrganizations.get(0)).get(0),
                            null);
                    fail("No SubscriptionStateException thrown");
                    return null;
                }
            });
        } catch (SubscriptionStateException e) {
            assertInvalidStateException(e, SubscriptionStatus.INVALID);
        }
    }

    @Test
    public void testGrantAdminRolePending() throws Throwable {
        appMgmtStub.userModificationCallReceived = false;
        appMgmtStub.addedUsers.clear();
        appMgmtStub.deletedUsers.clear();
        final String id = "testGrantAdminRolePending";
        subscribeAsync(id, 0, false);
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Subscription loadSubscription = loadSubscription(id,
                        testOrganizations.get(0).getKey());
                subMgmtLocal.modifyUserRole(loadSubscription,
                        testUsers.get(testOrganizations.get(0)).get(2), null);
                return null;
            }
        });
        checkUsersAddedButNotToApllication(id);
    }

    @Test
    public void testInformProductAboutNewUsersPending() throws Throwable {
        appMgmtStub.userModificationCallReceived = false;
        appMgmtStub.addedUsers.clear();
        appMgmtStub.deletedUsers.clear();
        final String id = "testInformProductAboutNewUsersPending";
        subscribeAsync(id, 0, false);
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Subscription loadSubscription = loadSubscription(id,
                        testOrganizations.get(0).getKey());
                subMgmtLocal.informProductAboutNewUsers(loadSubscription,
                        testOrganizations.get(0).getPlatformUsers());
                return null;
            }
        });
        checkUsersAddedButNotToApllication(id);
    }

    @Test
    public void testRevokeAdminRolePending() throws Throwable {
        appMgmtStub.userModificationCallReceived = false;
        appMgmtStub.addedUsers.clear();
        appMgmtStub.deletedUsers.clear();
        final String id = "testRevokeAdminRolePending";
        subscribeAsync(id, 0, false);
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Subscription loadSubscription = loadSubscription(id,
                        testOrganizations.get(0).getKey());
                subMgmtLocal.modifyUserRole(loadSubscription,
                        testUsers.get(testOrganizations.get(0)).get(2), null);
                return null;
            }
        });
        checkUsersAddedButNotToApllication(id);
    }

    protected void checkUsersAddedButNotToApllication(final String id)
            throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Subscription subscription = loadSubscription(id,
                        testOrganizations.get(0).getKey());
                // should contain 3 entries

                assertEquals("Wrong number of UsageLicenses", 3,
                        subscription.getUsageLicenses().size());

                assertEquals("User added to application", 0,
                        appMgmtStub.addedUsers.size());
                assertEquals("User removed from application", 0,
                        appMgmtStub.deletedUsers.size());
                assertFalse("User modification call received by application",
                        appMgmtStub.userModificationCallReceived);
                return null;
            }
        });
    }

    @Test
    public void testNotifyAboutTimedoutSubscriptions() throws Throwable {
        final String id = "testNotifyAboutTimedoutSubscriptions";
        subscribeAsync(id, 0, false);
        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                subMgmtLocal.notifyAboutTimedoutSubscriptions(
                        System.currentTimeMillis());
                return null;
            }
        });
        Subscription sub = runTX(new Callable<Subscription>() {
            @Override
            public Subscription call() throws Exception {
                Subscription subscription = loadSubscription(id,
                        testOrganizations.get(0).getKey());
                return subscription;
            }
        });
        assertTrue("No timeout mail sent", sub.isTimeoutMailSent());

    }

    @Test
    public void testGetUpgradeOptionsThroughTemplate() throws Exception {
        final String subId = "testGetUpgradeOptionsDirect";
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                final Product prod1 = testProducts.get(0);
                final Product prod2 = testProducts.get(1);
                ProductReference reference = new ProductReference(prod1, prod2);
                mgr.persist(reference);
                return null;
            }
        });
        VOService product = findProduct(testProducts.get(0).getKey());
        VOUser[] users = new VOUser[1];
        VOUser[] admins = new VOUser[1];
        admins[0] = UserDataAssembler
                .toVOUser(testUsers.get(testOrganizations.get(0)).get(1));
        users[0] = UserDataAssembler
                .toVOUser(testUsers.get(testOrganizations.get(0)).get(2));
        subMgmt.subscribeToService(Subscriptions.createVOSubscription(subId),
                product, getUsersToAdd(admins, users), null, null,
                new ArrayList<VOUda>());
        String id = runTX(new Callable<String>() {
            @Override
            public String call() throws Exception {
                Subscription sub = loadSubscription(subId,
                        testOrganizations.get(0).getKey());
                return sub.getSubscriptionId();
            }
        });
        List<VOService> upgradeOptions = subMgmt.getUpgradeOptions(id);
        assertEquals(1, upgradeOptions.size());
        assertEquals(testProducts.get(1).getKey(),
                upgradeOptions.get(0).getKey());
    }

    @Test
    public void testGetUpgradeOptionsThroughTemplateToCustom()
            throws Exception {
        final String subId = "UpgradeOptionsThroughTemplateToCustom";
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                final Product prod1 = testProducts.get(0);
                final Product prod2 = testProducts.get(1);
                ProductReference reference = new ProductReference(prod1, prod2);
                mgr.persist(reference);
                return null;
            }
        });
        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                testProducts.set(1,
                        mgr.find(Product.class, testProducts.get(1).getKey()));
                testProducts.set(2,
                        mgr.find(Product.class, testProducts.get(2).getKey()));
                final Product prod2 = testProducts.get(1);
                Product productCust = testProducts.get(2);
                productCust.setTemplate(prod2);
                productCust.setTargetCustomer(testOrganizations.get(0));
                return null;
            }
        });
        VOService product = findProduct(testProducts.get(0).getKey());
        VOUser[] users = new VOUser[1];
        VOUser[] admins = new VOUser[1];
        admins[0] = UserDataAssembler
                .toVOUser(testUsers.get(testOrganizations.get(0)).get(1));
        users[0] = UserDataAssembler
                .toVOUser(testUsers.get(testOrganizations.get(0)).get(2));
        subMgmt.subscribeToService(Subscriptions.createVOSubscription(subId),
                product, getUsersToAdd(admins, users), null, null,
                new ArrayList<VOUda>());
        String id = runTX(new Callable<String>() {
            @Override
            public String call() throws Exception {
                Subscription sub = loadSubscription(subId,
                        testOrganizations.get(0).getKey());
                return sub.getSubscriptionId();
            }
        });
        List<VOService> upgradeOptions = subMgmt.getUpgradeOptions(id);
        assertEquals(1, upgradeOptions.size());
        assertEquals(testProducts.get(2).getKey(),
                upgradeOptions.get(0).getKey());
    }

    private VOService findProduct(long key) {
        VOService result = null;
        List<VOService> products = servProv
                .getServicesForMarketplace(mp.getMarketplaceId());
        for (VOService product : products) {
            if (product.getKey() == key) {
                result = product;
                break;
            }
        }
        assertNotNull("Product '" + key + "' not found", result);
        return result;
    }

    @Test(expected = ServiceChangedException.class)
    public void testSubscribeToProductInactive() throws Exception {
        String subId = "testSubscribeToProductInactive";
        VOService product = findProduct(testProducts.get(0).getKey());
        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                final Product prod = mgr.find(Product.class,
                        testProducts.get(0).getKey());
                prod.setStatus(ServiceStatus.INACTIVE);
                return null;
            }
        });
        VOUser[] users = new VOUser[1];
        VOUser[] admins = new VOUser[1];
        admins[0] = UserDataAssembler
                .toVOUser(testUsers.get(testOrganizations.get(0)).get(1));
        users[0] = UserDataAssembler
                .toVOUser(testUsers.get(testOrganizations.get(0)).get(2));
        subMgmt.subscribeToService(Subscriptions.createVOSubscription(subId),
                product, getUsersToAdd(admins, users), null, null,
                new ArrayList<VOUda>());
    }

    @Test(expected = ServiceChangedException.class)
    public void testSubscribeToProductObsolete() throws Exception {
        String subId = "testSubscribeToProductInactive";
        VOService product = findProduct(testProducts.get(0).getKey());
        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                final Product prod = mgr.find(Product.class,
                        testProducts.get(0).getKey());
                prod.setStatus(ServiceStatus.OBSOLETE);
                return null;
            }
        });
        VOUser[] users = new VOUser[1];
        VOUser[] admins = new VOUser[1];
        admins[0] = UserDataAssembler
                .toVOUser(testUsers.get(testOrganizations.get(0)).get(1));
        users[0] = UserDataAssembler
                .toVOUser(testUsers.get(testOrganizations.get(0)).get(2));
        subMgmt.subscribeToService(Subscriptions.createVOSubscription(subId),
                product, getUsersToAdd(admins, users), null, null,
                new ArrayList<VOUda>());
    }

    @Test(expected = ServiceChangedException.class)
    public void testSubscribeToProductSuspended() throws Exception {
        String subId = "testSubscribeToProductInactive";
        VOService product = findProduct(testProducts.get(0).getKey());
        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                final Product prod = mgr.find(Product.class,
                        testProducts.get(0).getKey());
                prod.setStatus(ServiceStatus.SUSPENDED);
                return null;
            }
        });
        VOUser[] users = new VOUser[1];
        VOUser[] admins = new VOUser[1];
        admins[0] = UserDataAssembler
                .toVOUser(testUsers.get(testOrganizations.get(0)).get(1));
        users[0] = UserDataAssembler
                .toVOUser(testUsers.get(testOrganizations.get(0)).get(2));
        subMgmt.subscribeToService(Subscriptions.createVOSubscription(subId),
                product, getUsersToAdd(admins, users), null, null,
                new ArrayList<VOUda>());
    }

    @Test
    public void testGetUpgradeOptionsInactive() throws Exception {
        final String subId = "testGetUpgradeOptionsInactive";
        VOSubscription sub = prepareSubForGetUpgradeOptions(subId,
                ServiceStatus.INACTIVE);
        List<VOService> upgradeOptions = subMgmt
                .getUpgradeOptions(sub.getSubscriptionId());
        assertTrue(upgradeOptions.isEmpty());
    }

    @Test
    public void testGetUpgradeOptionsObsolete() throws Exception {
        final String subId = "testGetUpgradeOptionsObsolete";
        VOSubscription sub = prepareSubForGetUpgradeOptions(subId,
                ServiceStatus.OBSOLETE);
        List<VOService> upgradeOptions = subMgmt
                .getUpgradeOptions(sub.getSubscriptionId());
        assertTrue(upgradeOptions.isEmpty());
    }

    @Test
    public void testGetUpgradeOptionsSuspended() throws Exception {
        final String subId = "testGetUpgradeOptionsSuspended";
        VOSubscription sub = prepareSubForGetUpgradeOptions(subId,
                ServiceStatus.SUSPENDED);
        List<VOService> upgradeOptions = subMgmt
                .getUpgradeOptions(sub.getSubscriptionId());
        assertTrue(upgradeOptions.isEmpty());
    }

    @Test(expected = ServiceChangedException.class)
    public void testUpgradeSubscriptionInactive() throws Exception {
        final String subId = "testGetUpgradeOptionsInactive";
        createAvailablePayment(testOrganizations.get(0));
        // we have to use the id of the template because the products id is
        // replaced by the one of the template
        VOService product = findProduct(testProducts.get(1).getKey());
        VOUser[] users = new VOUser[1];
        VOUser[] admins = new VOUser[1];
        admins[0] = UserDataAssembler
                .toVOUser(testUsers.get(testOrganizations.get(0)).get(1));
        users[0] = UserDataAssembler
                .toVOUser(testUsers.get(testOrganizations.get(0)).get(2));
        VOPaymentInfo voPaymentInfo = getPaymentInfo(
                tpAndSupplier.getOrganizationId(), testOrganizations.get(0));
        VOBillingContact bc = createBillingContact(testOrganizations.get(0));
        VOSubscription sub = subMgmt.subscribeToService(
                Subscriptions.createVOSubscription(subId), product,
                getUsersToAdd(admins, users), voPaymentInfo, bc,
                new ArrayList<VOUda>());
        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                Product prod = mgr.find(Product.class,
                        testProducts.get(1).getKey());
                prod.setStatus(ServiceStatus.INACTIVE);
                return null;
            }
        });
        subMgmt.upgradeSubscription(sub, product, null, null,
                new ArrayList<VOUda>());
    }

    @Test(expected = ServiceChangedException.class)
    public void testUpgradeSubscriptionObsolete() throws Exception {
        final String subId = "testGetUpgradeOptionsInactive";
        createAvailablePayment(testOrganizations.get(0));
        // we have to use the id of the template because the products id is
        // replaced by the one of the template
        VOService product = findProduct(testProducts.get(1).getKey());
        VOUser[] users = new VOUser[1];
        VOUser[] admins = new VOUser[1];
        admins[0] = UserDataAssembler
                .toVOUser(testUsers.get(testOrganizations.get(0)).get(1));
        users[0] = UserDataAssembler
                .toVOUser(testUsers.get(testOrganizations.get(0)).get(2));
        VOPaymentInfo voPaymentInfo = getPaymentInfo(
                tpAndSupplier.getOrganizationId(), testOrganizations.get(0));
        VOBillingContact bc = createBillingContact(testOrganizations.get(0));
        VOSubscription sub = subMgmt.subscribeToService(
                Subscriptions.createVOSubscription(subId), product,
                getUsersToAdd(admins, users), voPaymentInfo, bc,
                new ArrayList<VOUda>());
        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                Product prod = mgr.find(Product.class,
                        testProducts.get(1).getKey());
                prod.setStatus(ServiceStatus.OBSOLETE);
                return null;
            }
        });
        subMgmt.upgradeSubscription(sub, product, null, null,
                new ArrayList<VOUda>());
    }

    @Test(expected = ServiceChangedException.class)
    public void testUpgradeSubscriptionSuspended() throws Exception {
        final String subId = "testGetUpgradeOptionsInactive";
        createAvailablePayment(testOrganizations.get(0));
        // we have to use the id of the template because the products id is
        // replaced by the one of the template
        VOService product = findProduct(testProducts.get(1).getKey());
        VOUser[] users = new VOUser[1];
        VOUser[] admins = new VOUser[1];
        admins[0] = UserDataAssembler
                .toVOUser(testUsers.get(testOrganizations.get(0)).get(1));
        users[0] = UserDataAssembler
                .toVOUser(testUsers.get(testOrganizations.get(0)).get(2));
        VOPaymentInfo voPaymentInfo = getPaymentInfo(
                tpAndSupplier.getOrganizationId(), testOrganizations.get(0));
        VOBillingContact bc = createBillingContact(testOrganizations.get(0));
        VOSubscription sub = subMgmt.subscribeToService(
                Subscriptions.createVOSubscription(subId), product,
                getUsersToAdd(admins, users), voPaymentInfo, bc,
                new ArrayList<VOUda>());
        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                Product prod = mgr.find(Product.class,
                        testProducts.get(1).getKey());
                prod.setStatus(ServiceStatus.SUSPENDED);
                return null;
            }
        });
        subMgmt.upgradeSubscription(sub, product, null, null,
                new ArrayList<VOUda>());
    }

    @Test(expected = ValidationException.class)
    public void testModifySubscriptionIdTooLong() throws Exception {
        // given
        final VOSubscription subscription = Subscriptions
                .createVOSubscription(TOO_LONG_ID);
        long key = 1;
        subscription.setKey(key);// prevent validateObjectKey error
        final SubscriptionServiceBean sBean = spy(
                new SubscriptionServiceBean());
        sBean.dataManager = mgr;
        ManageSubscriptionBean manageBean = spy(new ManageSubscriptionBean());
        sBean.manageBean = manageBean;
        doReturn(null).when(manageBean)
                .checkSubscriptionOwner(subscription.getSubscriptionId(), key);

        // when
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                sBean.modifySubscription(subscription, null,
                        new ArrayList<VOUda>());
                return null;
            }
        });
    }

    @Test(expected = ValidationException.class)
    public void testModifySubscriptionPonTooLong() throws Exception {
        // given
        final VOSubscription subscription = Subscriptions
                .createVOSubscription("test");
        long key = 1;
        subscription.setPurchaseOrderNumber(TOO_LONG_DESCRIPTION);
        subscription.setKey(key);// prevent validateObjectKey error
        final SubscriptionServiceBean sBean = spy(
                new SubscriptionServiceBean());
        ManageSubscriptionBean manageBean = spy(new ManageSubscriptionBean());
        sBean.manageBean = manageBean;
        sBean.dataManager = mgr;
        doReturn(null).when(manageBean)
                .checkSubscriptionOwner(subscription.getSubscriptionId(), key);

        // when
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                sBean.modifySubscription(subscription, null,
                        new ArrayList<VOUda>());
                return null;
            }
        });
    }

    @Test
    public void testModifySubscriptionNewPon() throws Throwable {
        final String id = prepareSubscriptionForModification(0L);

        // Now check results
        VOSubscription subToModify;
        try {
            subToModify = runTX(new Callable<VOSubscription>() {
                @Override
                public VOSubscription call() {
                    checkSubscribeToProduct(false, id, testProducts.get(3),
                            SubscriptionStatus.ACTIVE, 3, null, 1);
                    Subscription subscription = getSubscription(id,
                            testOrganizations.get(0).getKey());
                    return SubscriptionAssembler.toVOSubscription(subscription,
                            new LocalizerFacade(new LocalizerServiceStub2(),
                                    "en"));
                }
            });
        } catch (Exception e) {
            throw e.getCause();
        }
        final String pon = "new purchase order number";
        subToModify.setPurchaseOrderNumber(pon);
        subMgmt.modifySubscription(subToModify, null, new ArrayList<VOUda>());
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() {
                    Subscription sub = getSubscription(id,
                            testOrganizations.get(0).getKey());
                    assertEquals(pon, sub.getPurchaseOrderNumber());
                    return null;
                }
            });
        } catch (Exception e) {
            throw e.getCause();
        }
        assertTrue(isTriggerQueueService_sendSuspendingMessageCalled);
        assertTrue(isMessageSend(TriggerType.MODIFY_SUBSCRIPTION));
    }

    private String prepareSubscriptionForModification(long unitKey)
            throws Exception, ObjectNotFoundException,
            NonUniqueBusinessKeyException, ValidationException,
            PaymentInformationException, ServiceParameterException,
            ServiceChangedException, PriceModelException,
            ServiceOperationException, TechnicalServiceNotAliveException,
            TechnicalServiceOperationException, OperationNotPermittedException {
        createAvailablePayment(testOrganizations.get(0));
        VOService product = getProductToSubscribe(testProducts.get(3).getKey());
        VOUser[] users = new VOUser[2];
        VOUser[] admins = new VOUser[1];
        setUsers(users, admins);
        final String id = "testModifyPon";
        VOPaymentInfo voPaymentInfo = getPaymentInfo(
                tpAndSupplier.getOrganizationId(), testOrganizations.get(0));
        VOBillingContact bc = createBillingContact(testOrganizations.get(0));
        VOSubscription voSub = Subscriptions.createVOSubscription(id);
        voSub.setUnitKey(unitKey);
        subMgmt.subscribeToService(voSub, product, getUsersToAdd(admins, users),
                voPaymentInfo, bc, new ArrayList<VOUda>());
        return id;
    }

    @Test
    public void testModifySubscriptionNewParamValue() throws Throwable {
        handleNamedUserParamSettings(null, null, "32", Long.valueOf(3));
    }

    // refers to bug 6156
    @Test
    public void testModifySubscriptionNewParamValueNullNamedUsers()
            throws Throwable {
        handleNamedUserParamSettings(null, "5000", null, null);
    }

    @Test
    public void testModifySubscriptionNewParamValueNullPeriod()
            throws Throwable {
        handleNamedUserParamSettings("4", null, null, null);
    }

    /**
     * Creates a product and subscription, setting the specified value for the
     * platform parameter {@link PlatformParameterIdentifiers#NAMED_USER}.
     * 
     * @param newValueNamedUsers
     *            The new value for the parameter for the named users parameter.
     * @param newValuePeriod
     *            The new value for the period parameter.
     * @param newValueConcurrentUser
     *            The new value for the concurrent user parameter.
     * @param oldValue
     *            The initialization value for the parameter.
     */
    private void handleNamedUserParamSettings(String newValueNamedUsers,
            String newValuePeriod, String newValueConcurrentUser,
            final Long oldValue) throws Exception, ObjectNotFoundException,
            NonUniqueBusinessKeyException, OperationNotPermittedException,
            ValidationException, SubscriptionMigrationException {
        createAvailablePayment(testOrganizations.get(0));
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Product product = (Product) mgr.find(testProducts.get(3));
                Products.addPlatformParameter(product,
                        PlatformParameterIdentifiers.NAMED_USER, true, oldValue,
                        mgr);
                Products.addPlatformParameter(product,
                        PlatformParameterIdentifiers.CONCURRENT_USER, true,
                        oldValue, mgr);
                Products.addPlatformParameter(product,
                        PlatformParameterIdentifiers.PERIOD, true, oldValue,
                        mgr);
                return null;
            }
        });
        VOSubscription subscription = Subscriptions
                .createVOSubscription(commonSubscribeToProduct());

        VOParameter param1 = getParameterById(subscription,
                PlatformParameterIdentifiers.CONCURRENT_USER);
        assertNotNull("Required param not found", param1);
        param1.setValue(newValueConcurrentUser);

        VOParameter param2 = getParameterById(subscription,
                PlatformParameterIdentifiers.NAMED_USER);
        assertNotNull("Required param not found", param2);
        param2.setValue(newValueNamedUsers);

        VOParameter param3 = getParameterById(subscription,
                PlatformParameterIdentifiers.PERIOD);
        assertNotNull("Required param not found", param3);
        param3.setValue(newValuePeriod);

        List<VOParameter> params = new ArrayList<>();
        params.add(param1);
        params.add(param2);
        params.add(param3);
        subMgmt.modifySubscription(
                subMgmt.getSubscriptionDetails(
                        subscription.getSubscriptionId()),
                params, new ArrayList<VOUda>());

        List<VOParameter> parameters = subMgmt
                .getSubscriptionDetails(subscription.getSubscriptionId())
                .getSubscribedService().getParameters();
        boolean historyEntryFound = false;
        for (VOParameter currentParam : parameters) {
            if (currentParam.getParameterDefinition().getParameterId()
                    .equals(PlatformParameterIdentifiers.NAMED_USER)) {
                assertEquals(newValueNamedUsers, currentParam.getValue());
                historyEntryFound = true;
            }
        }
        assertTrue("Parameter does not exist", historyEntryFound);
    }

    @Test(expected = SubscriptionMigrationException.class)
    public void testModifySubscriptionInvalidParamValue() throws Throwable {
        createAvailablePayment(testOrganizations.get(0));
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Product product = (Product) mgr.find(testProducts.get(3));
                Products.addPlatformParameter(product,
                        PlatformParameterIdentifiers.NAMED_USER, true,
                        Long.valueOf(3), mgr);
                return null;
            }
        });
        VOSubscription subscription = Subscriptions
                .createVOSubscription(commonSubscribeToProduct());

        VOParameter param = getParameterById(subscription,
                PlatformParameterIdentifiers.NAMED_USER);
        param.setValue("2");
        List<VOParameter> params = new ArrayList<>();
        params.add(param);
        subMgmt.modifySubscription(
                subMgmt.getSubscriptionDetails(
                        subscription.getSubscriptionId()),
                params, new ArrayList<VOUda>());
    }

    @Test(expected = OperationNotPermittedException.class)
    public void testModifySubscriptionInvalidOrganization() throws Throwable {
        createAvailablePayment(testOrganizations.get(0));
        final VOSubscription subscription = subMgmt
                .getSubscriptionDetails(commonSubscribeToProduct());

        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                Subscription sub = mgr.find(Subscription.class,
                        subscription.getKey());
                sub.setOrganization(mgr.find(Organization.class,
                        testOrganizations.get(1).getKey()));
                return null;
            }
        });

        subMgmt.modifySubscription(subscription, null, new ArrayList<VOUda>());
    }

    /**
     * Test subscription modification. block if
     * (!subscriptionToModify.getSubscriptionId().equals(subscriptionId)) {
     * 
     * @throws Throwable
     */
    @Test
    public void testModifySubscriptionDifferentID() throws Throwable {
        final String id = prepareSubscriptionForModification(0L);

        // Now check results
        VOSubscription subToModify;
        try {
            subToModify = runTX(new Callable<VOSubscription>() {
                @Override
                public VOSubscription call() {
                    checkSubscribeToProduct(false, id, testProducts.get(3),
                            SubscriptionStatus.ACTIVE, 3, null, 1);
                    Subscription subscription = getSubscription(id,
                            testOrganizations.get(0).getKey());
                    return SubscriptionAssembler.toVOSubscription(subscription,
                            new LocalizerFacade(new LocalizerServiceStub2(),
                                    "en"));
                }
            });
        } catch (Exception e) {
            throw e.getCause();
        }
        // try to set another ID, covering block for testing business key unique
        subToModify.setSubscriptionId(UUID.randomUUID().toString());
        subMgmt.modifySubscription(subToModify, null, new ArrayList<VOUda>());

    }

    @Test(expected = ConcurrentModificationException.class)
    public void testModifySubscriptionDifferentIDConcurrent() throws Throwable {
        final String id = prepareSubscriptionForModification(0L);
        String value = UUID.randomUUID().toString();

        // Now check results
        VOSubscription subToModify;
        subToModify = runTX(new Callable<VOSubscription>() {
            @Override
            public VOSubscription call() {
                checkSubscribeToProduct(false, id, testProducts.get(3),
                        SubscriptionStatus.ACTIVE, 3, null, 1);
                Subscription subscription = getSubscription(id,
                        testOrganizations.get(0).getKey());
                return SubscriptionAssembler.toVOSubscription(subscription,
                        new LocalizerFacade(new LocalizerServiceStub2(), "en"));
            }
        });
        // try to set another ID, covering block for testing business key unique
        String subId = value;
        subToModify.setSubscriptionId(subId);
        VOSubscriptionDetails modifySubscription = subMgmt
                .modifySubscription(subToModify, null, new ArrayList<VOUda>());
        assertEquals(subId, modifySubscription.getSubscriptionId());
        assertEquals(subToModify.getVersion() + 1,
                modifySubscription.getVersion());

        // now decrease the version and ensure that the similar call fails,
        // simulating concurrent access for lost update scenarios
        subId = subId + "enh";
        subToModify.setVersion(subToModify.getVersion() - 1);
        subToModify.setSubscriptionId(subId);
        subMgmt.modifySubscription(subToModify, null, new ArrayList<VOUda>());
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testModifySubscriptionDifferentConcurrentModifiedParam()
            throws Throwable {
        final String id = prepareSubscriptionForModification(0L);
        String value = UUID.randomUUID().toString();

        // Now check results
        VOSubscriptionDetails subToModify;
        subToModify = runTX(new Callable<VOSubscriptionDetails>() {
            @Override
            public VOSubscriptionDetails call() {
                checkSubscribeToProduct(false, id, testProducts.get(3),
                        SubscriptionStatus.ACTIVE, 3, null, 1);
                Subscription subscription = getSubscription(id,
                        testOrganizations.get(0).getKey());
                subscription.getProduct().getParameterSet().getParameters()
                        .get(0).setConfigurable(true);
                return SubscriptionAssembler.toVOSubscriptionDetails(
                        subscription,
                        new LocalizerFacade(new LocalizerServiceStub2(), "en"));
            }
        });
        // try to set another ID, covering block for testing business key unique
        String subId = value;
        subToModify.setSubscriptionId(subId);
        VOSubscriptionDetails modifySubscription = subMgmt
                .modifySubscription(subToModify, null, new ArrayList<VOUda>());
        assertEquals(subId, modifySubscription.getSubscriptionId());
        assertEquals(subToModify.getVersion() + 1,
                modifySubscription.getVersion());

        // now decrease the version and ensure that the similar call fails,
        // simulating concurrent access for lost update scenarios
        VOParameter voParameter = modifySubscription.getSubscribedService()
                .getParameters().get(0);
        voParameter.setVersion(voParameter.getVersion() - 1);
        List<VOParameter> params = new ArrayList<>();
        params.add(voParameter);
        subMgmt.modifySubscription(modifySubscription, params,
                new ArrayList<VOUda>());
    }

    /**
     * Test for private method checkPlatformParameterConstraints. Parameter has
     * null value.
     * 
     * @throws Throwable
     */
    @Test
    public void testModifySubscriptioncheckIfValidParametersAreModifiedNullValue()
            throws Throwable {
        createAvailablePayment(testOrganizations.get(0));
        baseTestModifyWithParameters(true, parameterValueConstant, null, true);
    }

    /**
     * Test for private method checkPlatformParameterConstraints. New parameter
     * value is the same value.
     * 
     * @throws Exception
     */
    @Test
    public void testModifySubscriptioncheckIfValidParametersAreModifiedTheSameValue()
            throws Throwable {
        createAvailablePayment(testOrganizations.get(0));
        baseTestModifyWithParameters(true, parameterValueConstant, Boolean.TRUE,
                true);
    }

    /**
     * Test for private method checkPlatformParameterConstraints. New parameter
     * value is different from old one.
     * 
     * @throws Exception
     */
    @Test
    public void testModifySubscriptioncheckIfValidParametersAreModifiedNewValue()
            throws Throwable {
        createAvailablePayment(testOrganizations.get(0));
        baseTestModifyWithParameters(true, parameterValueConstant,
                Boolean.FALSE, true);
    }

    /**
     * Test for private method checkPlatformParameterConstraints. New parameter
     * value is different from old one. Parameter check failed.
     * 
     * @throws Exception
     */
    @Test(expected = SubscriptionMigrationException.class)
    public void testModifySubscriptioncheckIfValidParametersAreModifiedOldNullValueNewValue()
            throws Throwable {
        createAvailablePayment(testOrganizations.get(0));
        baseTestModifyWithParameters(true, null, Boolean.FALSE, true);
    }

    @Test(expected = ValidationException.class)
    public void testModifySubscriptionIntValidationFailed() throws Exception {
        final TriggerProcess tp = new TriggerProcess();
        tp.addTriggerProcessParameter(TriggerProcessParameterName.SUBSCRIPTION,
                new VOSubscription());
        tp.addTriggerProcessParameter(TriggerProcessParameterName.PARAMETERS,
                new ArrayList<VOParameter>());
        tp.addTriggerProcessParameter(TriggerProcessParameterName.UDAS,
                new ArrayList<VOUda>());

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                subMgmtLocal.modifySubscriptionInt(tp);
                return null;
            }
        });
    }

    @Test
    public void testModifySubscriptionTechnicalServiceNotAliveException()
            throws Throwable {

        // data setup
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Scenario.setup(container, false);
                return null;
            }
        });
        container.login(Scenario.getCustomerAdminUser().getKey(),
                ROLE_ORGANIZATION_ADMIN);

        VOSubscriptionDetails vo = subMgmt.getSubscriptionDetails(
                Scenario.getSubscription().getSubscriptionId());
        vo.getSubscribedService().getParameters().get(0).setValue("2");

        try {
            appMgmtStub.throwTechnicalServiceNotAliveExceptionCustomer = true;
            subMgmt.modifySubscription(vo,
                    vo.getSubscribedService().getParameters(),
                    new ArrayList<VOUda>());
            Assert.fail("The modifySubscription call must fail.");
        } catch (TechnicalServiceNotAliveException e) {
            assertEquals("ex.TechnicalServiceNotAliveException.CUSTOMER",
                    e.getMessageKey());
        }
    }

    @Test
    public void testModifySubscriptionInt() throws Exception {
        final Subscription sub = createSubscription();
        final TriggerProcess tp = new TriggerProcess();
        VOSubscriptionDetails voSub = SubscriptionAssembler
                .toVOSubscriptionDetails(sub,
                        new LocalizerFacade(localizer, "en"));

        tp.addTriggerProcessParameter(TriggerProcessParameterName.SUBSCRIPTION,
                voSub);
        tp.addTriggerProcessParameter(TriggerProcessParameterName.PARAMETERS,
                new ArrayList<VOParameter>());
        tp.addTriggerProcessParameter(TriggerProcessParameterName.UDAS,
                new ArrayList<VOUda>());

        PlatformUser user = runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                Organization organization = mgr.getReference(Organization.class,
                        sub.getOrganization().getKey());
                return organization.getPlatformUsers().get(0);
            }
        });
        container.login(user.getKey(), ROLE_ORGANIZATION_ADMIN);

        final VOSubscriptionDetails voSubDetails = runTX(
                new Callable<VOSubscriptionDetails>() {
                    @Override
                    public VOSubscriptionDetails call() throws Exception {
                        return subMgmtLocal.modifySubscriptionInt(tp);
                    }
                });
        assertTrue(isTriggerQueueService_sendAllNonSuspendingMessageCalled);
        assertTrue(isMessageSend(TriggerType.MODIFY_SUBSCRIPTION));
        assertTrue(isMessageSend(TriggerType.SUBSCRIPTION_MODIFICATION));

        Product subTemplate = runTX(new Callable<Product>() {
            @Override
            public Product call() throws Exception {
                return mgr.getReference(Product.class,
                        voSubDetails.getServiceKey());
            }
        });
        assertNotNull(subTemplate.getConfiguratorUrl());

        Product subCopy = runTX(new Callable<Product>() {
            @Override
            public Product call() throws Exception {
                Subscription subscription = mgr.getReference(Subscription.class,
                        voSubDetails.getKey());
                Product subCopy = subscription.getProduct();
                subCopy.getConfiguratorUrl();
                return subCopy;
            }
        });
        assertNull(subCopy.getConfiguratorUrl());
    }

    @Test(expected = SubscriptionStateException.class)
    public void testModifyPendingSubscriptionIntChangeID_Bug_9998()
            throws Throwable {
        // given
        String subscriptionId = givenPendingSubscriptionWithThreeUsers();

        // when
        modifySubscriptionIDInt(subscriptionId);
    }

    @Test
    public void testModifySubscription_changeOwner() throws Exception {
        final String id = prepareSubscriptionForModification(0L);

        VOSubscriptionDetails subToModify;
        subToModify = runTX(new Callable<VOSubscriptionDetails>() {
            @Override
            public VOSubscriptionDetails call() {
                checkSubscribeToProduct(false, id, testProducts.get(3),
                        SubscriptionStatus.ACTIVE, 3, null, 1);
                Subscription subscription = getSubscription(id,
                        testOrganizations.get(0).getKey());
                subscription.getProduct().getParameterSet().getParameters()
                        .get(0).setConfigurable(true);
                return SubscriptionAssembler.toVOSubscriptionDetails(
                        subscription,
                        new LocalizerFacade(new LocalizerServiceStub2(), "en"));
            }
        });

        final PlatformUser customerAdmin = runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call()
                    throws ObjectNotFoundException, NumberFormatException {
                return mgr.getReference(PlatformUser.class,
                        Long.valueOf(customerUserKey).longValue());
            }
        });

        final long subKey = subToModify.getKey();
        PlatformUser subOwnerBefore = runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call()
                    throws ObjectNotFoundException, NumberFormatException {
                Subscription sub = mgr.getReference(Subscription.class, subKey);
                return unproxyEntity(sub.getOwner());
            }
        });

        assertEquals(customerAdmin, subOwnerBefore);

        PlatformUser subManager = runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws ObjectNotFoundException,
                    NumberFormatException, NonUniqueBusinessKeyException {
                final Organization customer = mgr.getReference(
                        Organization.class,
                        customerAdmin.getOrganization().getKey());
                PlatformUser user = Organizations.createUserForOrg(mgr,
                        customer, false, "subMgr", "en");
                PlatformUsers.grantRoles(mgr, user,
                        UserRoleType.SUBSCRIPTION_MANAGER);
                return user;
            }
        });

        subToModify.setOwnerId(subManager.getUserId());
        subMgmt.modifySubscription(subToModify, null, new ArrayList<VOUda>());

        PlatformUser subOwnerAfter = runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call()
                    throws ObjectNotFoundException, NumberFormatException {
                Subscription sub = mgr.getReference(Subscription.class, subKey);
                return unproxyEntity(sub.getOwner());
            }
        });

        assertEquals(subManager, subOwnerAfter);

    }

    @Test(expected = OperationNotPermittedException.class)
    public void testModifySubscription_changeOwner_notOwnerRole()
            throws Exception {
        final String id = prepareSubscriptionForModification(0L);

        VOSubscriptionDetails subToModify;
        subToModify = runTX(new Callable<VOSubscriptionDetails>() {
            @Override
            public VOSubscriptionDetails call() {
                checkSubscribeToProduct(false, id, testProducts.get(3),
                        SubscriptionStatus.ACTIVE, 3, null, 1);
                Subscription subscription = getSubscription(id,
                        testOrganizations.get(0).getKey());
                subscription.getProduct().getParameterSet().getParameters()
                        .get(0).setConfigurable(true);
                return SubscriptionAssembler.toVOSubscriptionDetails(
                        subscription,
                        new LocalizerFacade(new LocalizerServiceStub2(), "en"));
            }
        });

        final PlatformUser customerAdmin = runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call()
                    throws ObjectNotFoundException, NumberFormatException {
                return mgr.getReference(PlatformUser.class,
                        Long.valueOf(customerUserKey).longValue());
            }
        });

        final long subKey = subToModify.getKey();
        PlatformUser subOwnerBefore = runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call()
                    throws ObjectNotFoundException, NumberFormatException {
                Subscription sub = mgr.getReference(Subscription.class, subKey);
                return unproxyEntity(sub.getOwner());
            }
        });

        assertEquals(customerAdmin, subOwnerBefore);

        PlatformUser user = runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws ObjectNotFoundException,
                    NumberFormatException, NonUniqueBusinessKeyException {
                final Organization customer = mgr.getReference(
                        Organization.class,
                        customerAdmin.getOrganization().getKey());
                PlatformUser user = Organizations.createUserForOrg(mgr,
                        customer, false, "mpOwner", "en");
                PlatformUsers.grantRoles(mgr, user,
                        UserRoleType.MARKETPLACE_OWNER);
                return user;
            }
        });

        subToModify.setOwnerId(user.getUserId());
        subMgmt.modifySubscription(subToModify, null, new ArrayList<VOUda>());
    }

    @Test
    public void testModifySubscriptionUnitChange() throws Exception {

        Organization customerOrg = runTX(new Callable<Organization>() {
            @Override
            public Organization call() {
                return testOrganizations.get(0);
            }
        });

        UserGroup unit1 = getUnit(UNIT1, customerOrg);
        UserGroup unit2 = getUnit(UNIT2, customerOrg);

        final String id = prepareSubscriptionForModification(unit1.getKey());

        VOSubscriptionDetails subToModify = runTX(
                new Callable<VOSubscriptionDetails>() {
                    @Override
                    public VOSubscriptionDetails call() {
                        checkSubscribeToProduct(false, id, testProducts.get(3),
                                SubscriptionStatus.ACTIVE, 3, null, 1);
                        Subscription subscription = getSubscription(id,
                                testOrganizations.get(0).getKey());
                        subscription.getProduct().getParameterSet()
                                .getParameters().get(0).setConfigurable(true);
                        return SubscriptionAssembler.toVOSubscriptionDetails(
                                subscription, new LocalizerFacade(
                                        new LocalizerServiceStub2(), "en"));
                    }
                });

        final long subKey = subToModify.getKey();
        UserGroup subUnitBefore = runTX(new Callable<UserGroup>() {
            @Override
            public UserGroup call() throws ObjectNotFoundException {
                Subscription sub = mgr.getReference(Subscription.class, subKey);
                return unproxyEntity(sub.getUserGroup());
            }
        });

        assertEquals(unit1.getKey(), subUnitBefore.getKey());

        subToModify.setUnitKey(unit2.getKey());
        subMgmt.modifySubscription(subToModify, null, new ArrayList<VOUda>());

        UserGroup subUnitAfter = runTX(new Callable<UserGroup>() {
            @Override
            public UserGroup call() throws ObjectNotFoundException {
                Subscription sub = mgr.getReference(Subscription.class, subKey);
                return unproxyEntity(sub.getUserGroup());
            }
        });

        assertEquals(unit2.getKey(), subUnitAfter.getKey());
    }

    @Test
    public void testModifySubscriptionUnitAssign() throws Exception {

        Organization customerOrg = runTX(new Callable<Organization>() {
            @Override
            public Organization call() {
                return testOrganizations.get(0);
            }
        });

        UserGroup unit1 = getUnit(UNIT1, customerOrg);

        final String id = prepareSubscriptionForModification(0L);

        VOSubscriptionDetails subToModify = runTX(
                new Callable<VOSubscriptionDetails>() {
                    @Override
                    public VOSubscriptionDetails call() {
                        checkSubscribeToProduct(false, id, testProducts.get(3),
                                SubscriptionStatus.ACTIVE, 3, null, 1);
                        Subscription subscription = getSubscription(id,
                                testOrganizations.get(0).getKey());
                        subscription.getProduct().getParameterSet()
                                .getParameters().get(0).setConfigurable(true);
                        return SubscriptionAssembler.toVOSubscriptionDetails(
                                subscription, new LocalizerFacade(
                                        new LocalizerServiceStub2(), "en"));
                    }
                });

        final long subKey = subToModify.getKey();
        UserGroup subUnitBefore = runTX(new Callable<UserGroup>() {
            @Override
            public UserGroup call() throws ObjectNotFoundException {
                Subscription sub = mgr.getReference(Subscription.class, subKey);
                return sub.getUserGroup();
            }
        });

        assertNull(subUnitBefore);

        subToModify.setUnitKey(unit1.getKey());
        subMgmt.modifySubscription(subToModify, null, new ArrayList<VOUda>());

        UserGroup subUnitAfter = runTX(new Callable<UserGroup>() {
            @Override
            public UserGroup call() throws ObjectNotFoundException {
                Subscription sub = mgr.getReference(Subscription.class, subKey);
                return unproxyEntity(sub.getUserGroup());
            }
        });

        assertEquals(unit1.getKey(), subUnitAfter.getKey());
    }

    @Test
    public void testModifySubscriptionUnitAssignSubManager() throws Exception {

        Organization customerOrg = runTX(new Callable<Organization>() {
            @Override
            public Organization call() {
                return testOrganizations.get(0);
            }
        });

        UserGroup unit1 = getUnit(UNIT1, customerOrg);

        final String id = prepareSubscriptionForModification(0L);

        VOSubscriptionDetails subToModify = runTX(
                new Callable<VOSubscriptionDetails>() {
                    @Override
                    public VOSubscriptionDetails call() {
                        checkSubscribeToProduct(false, id, testProducts.get(3),
                                SubscriptionStatus.ACTIVE, 3, null, 1);
                        Subscription subscription = getSubscription(id,
                                testOrganizations.get(0).getKey());
                        subscription.getProduct().getParameterSet()
                                .getParameters().get(0).setConfigurable(true);
                        return SubscriptionAssembler.toVOSubscriptionDetails(
                                subscription, new LocalizerFacade(
                                        new LocalizerServiceStub2(), "en"));
                    }
                });

        final long subKey = subToModify.getKey();
        UserGroup subUnitBefore = runTX(new Callable<UserGroup>() {
            @Override
            public UserGroup call() throws ObjectNotFoundException {
                Subscription sub = mgr.getReference(Subscription.class, subKey);
                return sub.getUserGroup();
            }
        });

        assertNull(subUnitBefore);

        PlatformUser subManager = runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws ObjectNotFoundException,
                    NumberFormatException, NonUniqueBusinessKeyException {
                final Organization customer = mgr.getReference(
                        Organization.class, testOrganizations.get(0).getKey());
                PlatformUser user = Organizations.createUserForOrg(mgr,
                        customer, false, "subMgr", "en");
                PlatformUsers.grantRoles(mgr, user,
                        UserRoleType.SUBSCRIPTION_MANAGER);
                return user;
            }
        });

        subToModify.setOwnerId(subManager.getUserId());

        subMgmt.modifySubscription(subToModify, null, new ArrayList<VOUda>());

        container.login(subManager.getKey(), ROLE_SUBSCRIPTION_MANAGER);

        subToModify = runTX(new Callable<VOSubscriptionDetails>() {
            @Override
            public VOSubscriptionDetails call() {
                checkSubscribeToProduct(false, id, testProducts.get(3),
                        SubscriptionStatus.ACTIVE, 3, null, 1);
                Subscription subscription = getSubscription(id,
                        testOrganizations.get(0).getKey());
                subscription.getProduct().getParameterSet().getParameters()
                        .get(0).setConfigurable(true);
                return SubscriptionAssembler.toVOSubscriptionDetails(
                        subscription,
                        new LocalizerFacade(new LocalizerServiceStub2(), "en"));
            }
        });

        subToModify.setUnitKey(unit1.getKey());
        subMgmt.modifySubscription(subToModify, null, new ArrayList<VOUda>());

        UserGroup subUnitAfter = runTX(new Callable<UserGroup>() {
            @Override
            public UserGroup call() throws ObjectNotFoundException {
                Subscription sub = mgr.getReference(Subscription.class, subKey);
                return sub.getUserGroup();
            }
        });

        assertNull(subUnitAfter);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testModifySubscriptionUnitAssignNotExisting() throws Exception {

        final String id = prepareSubscriptionForModification(0L);

        VOSubscriptionDetails subToModify = runTX(
                new Callable<VOSubscriptionDetails>() {
                    @Override
                    public VOSubscriptionDetails call() {
                        checkSubscribeToProduct(false, id, testProducts.get(3),
                                SubscriptionStatus.ACTIVE, 3, null, 1);
                        Subscription subscription = getSubscription(id,
                                testOrganizations.get(0).getKey());
                        subscription.getProduct().getParameterSet()
                                .getParameters().get(0).setConfigurable(true);
                        return SubscriptionAssembler.toVOSubscriptionDetails(
                                subscription, new LocalizerFacade(
                                        new LocalizerServiceStub2(), "en"));
                    }
                });

        final long subKey = subToModify.getKey();
        UserGroup subUnitBefore = runTX(new Callable<UserGroup>() {
            @Override
            public UserGroup call() throws ObjectNotFoundException {
                Subscription sub = mgr.getReference(Subscription.class, subKey);
                return sub.getUserGroup();
            }
        });

        assertNull(subUnitBefore);

        subToModify.setUnitKey(UNIT_NON_EXISTING);
        subMgmt.modifySubscription(subToModify, null, new ArrayList<VOUda>());
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testModifySubscriptionUnitAssignOtherOrg() throws Exception {

        Long unitOtherOrgKey = runTX(new Callable<Long>() {
            @Override
            public Long call() throws ObjectNotFoundException {
                UserGroup unit = new UserGroup();
                unit.setName(
                        UNIT_OTHER_ORG + tpAndSupplier.getOrganizationId());
                unit.setOrganization_tkey(tpAndSupplier.getKey());
                unit = (UserGroup) mgr.getReferenceByBusinessKey(unit);
                return Long.valueOf(unit.getKey());
            }
        });

        final String id = prepareSubscriptionForModification(0L);

        VOSubscriptionDetails subToModify = runTX(
                new Callable<VOSubscriptionDetails>() {
                    @Override
                    public VOSubscriptionDetails call() {
                        checkSubscribeToProduct(false, id, testProducts.get(3),
                                SubscriptionStatus.ACTIVE, 3, null, 1);
                        Subscription subscription = getSubscription(id,
                                testOrganizations.get(0).getKey());
                        subscription.getProduct().getParameterSet()
                                .getParameters().get(0).setConfigurable(true);
                        return SubscriptionAssembler.toVOSubscriptionDetails(
                                subscription, new LocalizerFacade(
                                        new LocalizerServiceStub2(), "en"));
                    }
                });

        final long subKey = subToModify.getKey();
        UserGroup subUnitBefore = runTX(new Callable<UserGroup>() {
            @Override
            public UserGroup call() throws ObjectNotFoundException {
                Subscription sub = mgr.getReference(Subscription.class, subKey);
                return sub.getUserGroup();
            }
        });

        assertNull(subUnitBefore);

        subToModify.setUnitKey(unitOtherOrgKey.longValue());
        subMgmt.modifySubscription(subToModify, null, new ArrayList<VOUda>());
    }

    @Test
    public void testModifySubscriptionUnitDeassign() throws Exception {

        Organization customerOrg = runTX(new Callable<Organization>() {
            @Override
            public Organization call() {
                return testOrganizations.get(0);
            }
        });

        final String customerOrgId = customerOrg.getOrganizationId();
        final long customerOrgKey = customerOrg.getKey();
        Long unit1Key = runTX(new Callable<Long>() {
            @Override
            public Long call() throws ObjectNotFoundException {
                UserGroup unit = new UserGroup();
                unit.setName(UNIT1 + customerOrgId);
                unit.setOrganization_tkey(customerOrgKey);
                unit = (UserGroup) mgr.getReferenceByBusinessKey(unit);
                return Long.valueOf(unit.getKey());
            }
        });

        final String id = prepareSubscriptionForModification(
                unit1Key.longValue());

        VOSubscriptionDetails subToModify = runTX(
                new Callable<VOSubscriptionDetails>() {
                    @Override
                    public VOSubscriptionDetails call() {
                        checkSubscribeToProduct(false, id, testProducts.get(3),
                                SubscriptionStatus.ACTIVE, 3, null, 1);
                        Subscription subscription = getSubscription(id,
                                testOrganizations.get(0).getKey());
                        subscription.getProduct().getParameterSet()
                                .getParameters().get(0).setConfigurable(true);
                        return SubscriptionAssembler.toVOSubscriptionDetails(
                                subscription, new LocalizerFacade(
                                        new LocalizerServiceStub2(), "en"));
                    }
                });

        final long subKey = subToModify.getKey();
        UserGroup subUnitBefore = runTX(new Callable<UserGroup>() {
            @Override
            public UserGroup call() throws ObjectNotFoundException {
                Subscription sub = mgr.getReference(Subscription.class, subKey);
                return unproxyEntity(sub.getUserGroup());
            }
        });

        assertEquals(unit1Key.longValue(), subUnitBefore.getKey());

        subToModify.setUnitKey(0L);
        subToModify.setUnitName(null);
        subMgmt.modifySubscription(subToModify, null, new ArrayList<VOUda>());

        UserGroup subUnitAfter = runTX(new Callable<UserGroup>() {
            @Override
            public UserGroup call() throws ObjectNotFoundException {
                Subscription sub = mgr.getReference(Subscription.class, subKey);
                return sub.getUserGroup();
            }
        });

        assertNull(subUnitAfter);
    }

    @Test
    public void testModifySubscriptionUnitAsync() throws Throwable {
        UserGroup unitBefore = getUnit(UNIT1, testOrganizations.get(0));
        UserGroup unitAfter = getUnit(UNIT2, testOrganizations.get(0));
        final String subscriptionIdBefore = "testModifySubscriptionUnitAsyncBefore";
        final String subscriptionIdAfter = "testModifySubscriptionUnitAsyncAfter";

        subscribeAsyncUnit(subscriptionIdBefore, 0, false, unitBefore.getKey());

        String orgId = testOrganizations.get(0).getOrganizationId();

        // login as technology provider
        container.login(String.valueOf(supplierUser.getKey()),
                ROLE_TECHNOLOGY_MANAGER);
        VOInstanceInfo instanceInfo = new VOInstanceInfo();
        instanceInfo.setInstanceId("completionProductInstanceId");
        subMgmt.completeAsyncSubscription(subscriptionIdBefore, orgId,
                instanceInfo);

        VOSubscriptionDetails subToModify = runTX(
                new Callable<VOSubscriptionDetails>() {
                    @Override
                    public VOSubscriptionDetails call() {
                        checkSubscribeToProduct(false, subscriptionIdBefore,
                                asyncTestProducts.get(0),
                                SubscriptionStatus.ACTIVE, 3, null, 1);
                        Subscription subscription = getSubscription(
                                subscriptionIdBefore,
                                testOrganizations.get(0).getKey());
                        subscription.getProduct().getParameterSet()
                                .getParameters().get(0).setConfigurable(true);
                        return SubscriptionAssembler.toVOSubscriptionDetails(
                                subscription, new LocalizerFacade(
                                        new LocalizerServiceStub2(), "en"));
                    }
                });
        assertEquals(unitBefore.getKey(), subToModify.getUnitKey());

        subToModify.setUnitKey(unitAfter.getKey());
        subToModify.setSubscriptionId(subscriptionIdAfter);
        PlatformUser user = runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                Organization organization = mgr.getReference(Organization.class,
                        getSubscription(subscriptionIdBefore,
                                testOrganizations.get(0).getKey())
                                        .getOrganization().getKey());
                return organization.getPlatformUsers().get(0);
            }
        });
        // login as organization administrator
        container.login(user.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails modifiedSubscription = subMgmt
                .modifySubscription(subToModify, null, new ArrayList<VOUda>());
        assertEquals("Subscription must be PENDING",
                SubscriptionStatus.PENDING_UPD,
                modifiedSubscription.getStatus());
        // login as technology manager
        container.login(String.valueOf(supplierUser.getKey()),
                ROLE_TECHNOLOGY_MANAGER);
        subMgmt.completeAsyncModifySubscription(subscriptionIdBefore, orgId,
                instanceInfo);

        // Now check results
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() {
                    checkSubscribeToProduct(false, subscriptionIdAfter,
                            asyncTestProducts.get(0), SubscriptionStatus.ACTIVE,
                            3, subscriptionIdBefore, 1);
                    return null;
                }
            });
        } catch (Exception e) {
            if (e.getCause() != null) {
                throw e.getCause();
            }
            throw e;
        }
        final long subKey = subToModify.getKey();
        UserGroup subscriptionUnit = runTX(new Callable<UserGroup>() {
            @Override
            public UserGroup call() throws ObjectNotFoundException {
                Subscription sub = mgr.getReference(Subscription.class, subKey);
                return unproxyEntity(sub.getUserGroup());
            }
        });
        assertEquals(unitAfter.getKey(), subscriptionUnit.getKey());
    }

    @Test
    public void testModifySubscriptionUnitAsyncAbort() throws Throwable {
        UserGroup unitBefore = getUnit(UNIT1, testOrganizations.get(0));
        UserGroup unitAfter = getUnit(UNIT2, testOrganizations.get(0));
        final String subscriptionIdBefore = "testModifySubscriptionUnitAsyncBefore";
        final String subscriptionIdAfter = "testModifySubscriptionUnitAsyncAfter";

        subscribeAsyncUnit(subscriptionIdBefore, 0, false, unitBefore.getKey());

        String orgId = testOrganizations.get(0).getOrganizationId();

        // login as technology provider
        container.login(String.valueOf(supplierUser.getKey()),
                ROLE_TECHNOLOGY_MANAGER);
        VOInstanceInfo instanceInfo = new VOInstanceInfo();
        instanceInfo.setInstanceId("completionProductInstanceId");
        subMgmt.completeAsyncSubscription(subscriptionIdBefore, orgId,
                instanceInfo);

        VOSubscriptionDetails subToModify = runTX(
                new Callable<VOSubscriptionDetails>() {
                    @Override
                    public VOSubscriptionDetails call() {
                        checkSubscribeToProduct(false, subscriptionIdBefore,
                                asyncTestProducts.get(0),
                                SubscriptionStatus.ACTIVE, 3, null, 1);
                        Subscription subscription = getSubscription(
                                subscriptionIdBefore,
                                testOrganizations.get(0).getKey());
                        subscription.getProduct().getParameterSet()
                                .getParameters().get(0).setConfigurable(true);
                        return SubscriptionAssembler.toVOSubscriptionDetails(
                                subscription, new LocalizerFacade(
                                        new LocalizerServiceStub2(), "en"));
                    }
                });
        assertEquals(unitBefore.getKey(), subToModify.getUnitKey());

        subToModify.setUnitKey(unitAfter.getKey());
        subToModify.setSubscriptionId(subscriptionIdAfter);
        PlatformUser user = runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                Organization organization = mgr.getReference(Organization.class,
                        getSubscription(subscriptionIdBefore,
                                testOrganizations.get(0).getKey())
                                        .getOrganization().getKey());
                return organization.getPlatformUsers().get(0);
            }
        });
        // login as organization administrator
        container.login(user.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails modifiedSubscription = subMgmt
                .modifySubscription(subToModify, null, new ArrayList<VOUda>());
        assertEquals("Subscription must be PENDING",
                SubscriptionStatus.PENDING_UPD,
                modifiedSubscription.getStatus());
        // login as technology manager
        container.login(String.valueOf(supplierUser.getKey()),
                ROLE_TECHNOLOGY_MANAGER);
        subMgmt.abortAsyncModifySubscription(subscriptionIdBefore, orgId, null);

        // Now check results
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() {
                    checkSubscribeToProduct(false, subscriptionIdBefore,
                            asyncTestProducts.get(0), SubscriptionStatus.ACTIVE,
                            3, null, 1);
                    return null;
                }
            });
        } catch (Exception e) {
            if (e.getCause() != null) {
                throw e.getCause();
            }
            throw e;
        }

        final long subKey = subToModify.getKey();
        UserGroup subscriptionUnit = runTX(new Callable<UserGroup>() {
            @Override
            public UserGroup call() throws ObjectNotFoundException {
                Subscription sub = mgr.getReference(Subscription.class, subKey);
                return unproxyEntity(sub.getUserGroup());
            }
        });

        assertEquals(unitBefore.getKey(), subscriptionUnit.getKey());
    }

    private void subscribeAsyncUnit(final String id, final int indexInList,
            boolean noPaymentInfo, long unitKey) throws Throwable {
        assertNotNull(subMgmt);
        appMgmtStub.addedUsers.clear();
        appMgmtStub.deletedUsers.clear();
        VOService product = getProductToSubscribe(
                asyncTestProducts.get(indexInList).getKey());
        VOUser[] users = new VOUser[2];
        VOUser[] admins = new VOUser[1];
        setUsers(users, admins);
        VOPaymentInfo voPaymentInfo = getPaymentInfo(
                tpAndSupplier.getOrganizationId(), testOrganizations.get(0));
        VOBillingContact bc = createBillingContact(testOrganizations.get(0));
        VOSubscription sub = Subscriptions.createVOSubscription(id);
        sub.setUnitKey(unitKey);
        VOSubscription subscription = subMgmt.subscribeToService(sub, product,
                getUsersToAdd(admins, users),
                noPaymentInfo ? null : voPaymentInfo, bc,
                new ArrayList<VOUda>());

        assertEquals("Subscription must be PENDING", subscription.getStatus(),
                SubscriptionStatus.PENDING);

        final long orgKey = testOrganizations.get(0).getKey();

        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {

                    assertProvisioningNotCompleted(id, orgKey);

                    checkSubscribeToProduct(true, id,
                            asyncTestProducts.get(indexInList),
                            SubscriptionStatus.PENDING, 3, null, 1);
                    return null;
                }

            });
        } catch (Exception e) {
            throw e.getCause();
        }
    }

    private void modifySubscriptionIDInt(final String subscriptionId)
            throws Throwable {
        VOSubscription subToModify = getSubscription(subscriptionId);

        final TriggerProcess tp = generateModifyIDTriggerProcess(subToModify);

        callModifyInt(tp);
    }

    private void callModifyInt(final TriggerProcess tp) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                subMgmtLocal.modifySubscriptionInt(tp);
                return null;
            }
        });
    }

    private TriggerProcess generateModifyIDTriggerProcess(
            VOSubscription subToModify) {
        subToModify.setSubscriptionId(UUID.randomUUID().toString());

        final TriggerProcess tp = new TriggerProcess();
        tp.addTriggerProcessParameter(TriggerProcessParameterName.SUBSCRIPTION,
                subToModify);
        tp.addTriggerProcessParameter(TriggerProcessParameterName.PARAMETERS,
                new ArrayList<VOParameter>());
        tp.addTriggerProcessParameter(TriggerProcessParameterName.UDAS,
                new ArrayList<VOUda>());
        return tp;
    }

    private VOSubscription getSubscription(final String subscriptionId)
            throws Exception {
        VOSubscription subToModify = runTX(new Callable<VOSubscription>() {
            @Override
            public VOSubscription call() throws Exception {
                Subscription subscription = getSubscription(subscriptionId,
                        testOrganizations.get(0).getKey());
                return SubscriptionAssembler.toVOSubscription(subscription,
                        new LocalizerFacade(new LocalizerServiceStub2(), "en"));
            }
        });
        return subToModify;
    }

    @Test(expected = SubscriptionStateException.class)
    public void testModifyPendingSubscriptionChangeParams_Bug_9998()
            throws Throwable {
        // given
        String subscriptionId = givenPendingSubscriptionWithThreeUsers();

        // when
        modifySubscriptionParams(subscriptionId);
    }

    @Test(expected = SubscriptionStateException.class)
    public void testModifyPendingSunbscriptionIntChangeParams_Bug_9998()
            throws Throwable {
        // given
        String subscriptionId = givenPendingSubscriptionWithThreeUsers();

        // when
        modifySubscriptionParamsInt(subscriptionId);
    }

    private void modifySubscriptionParamsInt(String subscriptionId)
            throws Exception {
        VOSubscription subToModify = getSubscription(subscriptionId);

        final TriggerProcess tp = generateModifyParamsTriggerProcess(
                subToModify);

        callModifyInt(tp);

    }

    private TriggerProcess generateModifyParamsTriggerProcess(
            final VOSubscription subToModify) throws Exception {

        VOService voService = modifyParams(subToModify.getSubscriptionId());

        final TriggerProcess tp = new TriggerProcess();
        tp.addTriggerProcessParameter(TriggerProcessParameterName.SUBSCRIPTION,
                subToModify);
        tp.addTriggerProcessParameter(TriggerProcessParameterName.PARAMETERS,
                voService.getParameters());
        tp.addTriggerProcessParameter(TriggerProcessParameterName.UDAS,
                new ArrayList<VOUda>());
        return tp;
    }

    private void modifySubscriptionChangeRefNumber(final String subscriptionId,
            String refNumber) throws Throwable {
        VOSubscription subToModify = getSubscription(subscriptionId);

        subToModify.setPurchaseOrderNumber(refNumber);

        subMgmt.modifySubscription(subToModify, new ArrayList<VOParameter>(),
                new ArrayList<VOUda>());
    }

    private void modifySubscriptionParams(final String subscriptionId)
            throws Throwable {

        VOSubscription subToModify = getSubscription(subscriptionId);

        VOService voService = modifyParams(subscriptionId);

        subMgmt.modifySubscription(subToModify, voService.getParameters(),
                new ArrayList<VOUda>());
    }

    private VOService modifyParams(final String subscriptionId)
            throws Exception {
        VOService voService = runTX(new Callable<VOService>() {
            @Override
            public VOService call() throws Exception {
                Subscription subscription = getSubscription(subscriptionId,
                        testOrganizations.get(0).getKey());
                Product subProduct = subscription.getProduct();
                return ProductAssembler.toVOProduct(subProduct,
                        new LocalizerFacade(localizer, "en"));
            }
        });

        voService.getParameters().get(0).setValue("5435897");
        return voService;
    }

    @Test(expected = SubscriptionStateException.class)
    public void testModifyPendingSubscriptionChangeID_Bug_9998()
            throws Throwable {
        // given
        String subscriptionId = givenPendingSubscriptionWithThreeUsers();

        // when
        modifySubscriptionID(subscriptionId);

        // then
        // SaaSSystemException should be thrown
    }

    @Test(expected = SubscriptionStateException.class)
    public void testModifyPendingSubscriptionChangeRefNumber_Bug_9998()
            throws Throwable {
        // given
        String subscriptionId = givenPendingSubscriptionWithThreeUsers();

        // when
        String refNumber = "1234567";
        modifySubscriptionChangeRefNumber(subscriptionId, refNumber);
    }

    /**
     * @param subscriptionId
     * @return
     * @throws Throwable
     */
    private void modifySubscriptionID(final String subscriptionId)
            throws Throwable {
        VOSubscription subToModify = getSubscription(subscriptionId);

        subToModify.setSubscriptionId(UUID.randomUUID().toString());
        subMgmt.modifySubscription(subToModify, null, new ArrayList<VOUda>());
    }

    /**
     * Test for getting subscription identifier for supplier. Testing of this
     * getter is needed for code coverage.
     * 
     * @throws Exception
     */
    @Test
    public void testGetSubscriptionIdentifiersForSupplier() throws Exception {
        container.login(String.valueOf(supplierUser.getKey()),
                ROLE_SERVICE_MANAGER);
        subMgmt.getSubscriptionIdentifiers();
    }

    /**
     * Test for getting customer for supplier. Testing of this getter is needed
     * for code coverage.
     * 
     * @throws Exception
     */
    @Test
    public void testGetCustomersForSupplier() throws Exception {
        createAvailablePayment(testOrganizations.get(0));
        final String id = commonSubscribeToProduct();
        container.login(String.valueOf(supplierUser.getKey()),
                new String[] { ROLE_SERVICE_MANAGER, ROLE_ORGANIZATION_ADMIN });
        subMgmt.getCustomersForSubscriptionId(id);
    }

    /**
     * Test method for getCustomersSubscriptionForSupplier. Testing of this
     * getter is needed for code coverage.
     * 
     * @throws Exception
     */
    @Test
    public void testGetCustomersSubscriptionForSupplier() throws Exception {
        createAvailablePayment(testOrganizations.get(0));
        commonSubscribeToProduct();
        container.login(supplierUser.getKey(), ROLE_SERVICE_MANAGER);
        subMgmt.getCustomerSubscriptions();
    }

    @Test
    public void testGetUpgradeOptionsInactiveTemplate() throws Exception {
        Product[] templates = prepareTemplates();
        templates[0] = activate(templates[0]);
        VOSubscription sub = prepareSubscription(templates[0]);
        List<VOService> upgradeOptions = subMgmt
                .getUpgradeOptions(sub.getSubscriptionId());
        assertTrue(upgradeOptions.isEmpty());
    }

    @Test
    public void testGetUpgradeOptionsActiveTemplateInactiveCustomerProduct()
            throws Exception {
        Product[] templates = prepareTemplates();
        templates[0] = activate(templates[0]);
        templates[1] = activate(templates[1]);
        prepareCustomerProduct(templates[1]);
        VOSubscription sub = prepareSubscription(templates[0]);
        List<VOService> upgradeOptions = subMgmt
                .getUpgradeOptions(sub.getSubscriptionId());
        assertEquals(0, upgradeOptions.size());
    }

    @Test
    public void testGetUpgradeOptionsActiveTemplateActiveCustomerProduct()
            throws Exception {
        Product[] templates = prepareTemplates();
        templates[0] = activate(templates[0]);
        templates[1] = activate(templates[1]);
        Product custProd = prepareCustomerProduct(templates[1]);
        activate(custProd);
        VOSubscription sub = prepareSubscription(templates[0]);
        List<VOService> upgradeOptions = subMgmt
                .getUpgradeOptions(sub.getSubscriptionId());
        assertEquals(1, upgradeOptions.size());
        assertEquals(custProd.getKey(), upgradeOptions.get(0).getKey());
    }

    @Test
    public void testGetUpgradeOptionsInactiveTemplateActiveCustomerProduct()
            throws Exception {
        Product[] templates = prepareTemplates();
        templates[0] = activate(templates[0]);
        Product custProd = prepareCustomerProduct(templates[1]);
        activate(custProd);
        VOSubscription sub = prepareSubscription(templates[0]);
        List<VOService> upgradeOptions = subMgmt
                .getUpgradeOptions(sub.getSubscriptionId());
        assertEquals(1, upgradeOptions.size());
        assertEquals(custProd.getKey(), upgradeOptions.get(0).getKey());
    }

    @Test
    public void testGetUpgradeOptionsInactiveTemplateInactiveCustomerProduct()
            throws Exception {
        Product[] templates = prepareTemplates();
        templates[0] = activate(templates[0]);
        prepareCustomerProduct(templates[1]);
        VOSubscription sub = prepareSubscription(templates[0]);
        List<VOService> upgradeOptions = subMgmt
                .getUpgradeOptions(sub.getSubscriptionId());
        assertTrue(upgradeOptions.isEmpty());
    }

    @Test(expected = ServiceChangedException.class)
    public void testSubscribeToProductInactiveTemplateInactiveCustomerProduct()
            throws Exception {
        Product[] templates = prepareTemplates();
        Product custProd = prepareCustomerProduct(templates[1]);
        prepareSubscription(custProd);
    }

    @Test(expected = ServiceChangedException.class)
    public void testSubscribeToProductActiveTemplateInactiveCustomerProduct()
            throws Exception {
        Product[] templates = prepareTemplates();
        templates[0] = activate(templates[0]);
        Product custProd = prepareCustomerProduct(templates[0]);
        prepareSubscription(custProd);
    }

    @Test(expected = ServiceChangedException.class)
    public void testSubscribeToProductInactiveTemplateActiveCustomerProduct()
            throws Exception {
        Product[] templates = prepareTemplates();
        Product custProd = prepareCustomerProduct(templates[0]);
        activate(custProd);
        prepareSubscription(templates[0]);
    }

    @Test(expected = ServiceChangedException.class)
    public void testSubscribeToProductActiveTemplateActiveCustomerProduct()
            throws Exception {
        Product[] templates = prepareTemplates();
        templates[0] = activate(templates[0]);
        Product custProd = prepareCustomerProduct(templates[0]);
        activate(custProd);
        prepareSubscription(templates[0]);
    }

    @Test
    public void testSubscribeToCustomerSpecificProductActiveTemplate()
            throws Exception {
        Product[] templates = prepareTemplates();
        templates[0] = activate(templates[0]);
        Product custProd = prepareCustomerProduct(templates[0]);
        activate(custProd);
        prepareSubscription(custProd);
    }

    @Test
    public void testSubscribeToCustomerSpecificProductInactiveTemplate()
            throws Exception {
        Product[] templates = prepareTemplates();
        Product custProd = prepareCustomerProduct(templates[0]);
        activate(custProd);
        prepareSubscription(custProd);
    }

    /**
     * Test for subcribeToProduct. Block "if (organization.getKey() !=
     * targetCustomer.getKey())"
     * 
     * @throws Exception
     */
    @Test(expected = OperationNotPermittedException.class)
    public void testSubscribeToProductOrganizationIsNotTargetCustomer()
            throws Exception {
        // set target customer as different organization
        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                initCustomerProduct(1);
                return null;
            }
        });
        Product[] templates = prepareTemplates();
        templates[0] = activate(templates[0]);
        Product custProd = prepareCustomerProduct(templates[0]);
        activate(custProd);
        prepareSubscription(templates[0]);
    }

    private Product initCustomerProduct(int index) {
        Product product = mgr.find(Product.class,
                testProducts.get(10).getKey());
        product.setTargetCustomer(testOrganizations.get(index));
        product.setTemplate(
                mgr.find(Product.class, testProducts.get(8).getKey()));
        return product;
    }

    /**
     * Test for subcribeToProduct. Block "if (priceModel == null)"
     * 
     * @throws Exception
     */
    @Test(expected = PriceModelException.class)
    public void testSubscribeToProductOrganizationMissingPriceModel()
            throws Exception {
        // set target customer the same organization
        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                Product product = initCustomerProduct(0);
                product.setPriceModel(null);
                return null;
            }

        });
        Product[] templates = prepareTemplates();
        templates[0] = activate(templates[0]);
        Product custProd = prepareCustomerProduct(templates[0]);
        activate(custProd);
        prepareSubscription(templates[0]);
    }

    /**
     * Test case for the upgradeSubscription(). No default payment info for
     * organization.
     */
    @Test(expected = SubscriptionMigrationException.class)
    public void testUpgradeSubscriptionSubscriptionMigrationFailed()
            throws Throwable {
        List<VOService> products;
        VOService product = getProductToSubscribe(
                testProducts.get(10).getKey());
        VOUser[] users = new VOUser[2];
        VOUser[] admins = new VOUser[1];
        setUsers(users, admins);
        VOSubscription sub = Subscriptions.createVOSubscription("testUpgrade");
        subMgmt.subscribeToService(sub, product, getUsersToAdd(admins, users),
                null, null, new ArrayList<VOUda>());
        products = servProv.getServicesForMarketplace(mp.getMarketplaceId());
        assertNotNull("Product expected", products);
        assertTrue("Product expected", products.size() > 0);
        subMgmt.upgradeSubscription(sub, products.get(0), null, null,
                new ArrayList<VOUda>());
    }

    /**
     * Creates two marketing products, subscribes to the first one (customer
     * specific pm) and migrates to the second marketing product. Related to bug
     * 5264.
     * 
     * @throws Exception
     */
    @Test
    public void testUpgradeSubscriptionFromCustomerSpecificProduct()
            throws Exception {
        createAvailablePayment(testOrganizations.get(0));
        Product sourceProductTemplate = testProducts.get(10);
        Product targetProductTemplate = testProducts.get(11);
        // product 10 can be migrated to product 11
        // so first create a customer specific version of product 10
        Product sourceProductCustomerSpecific = prepareCustomerProduct(
                sourceProductTemplate);
        sourceProductCustomerSpecific = activate(sourceProductCustomerSpecific);

        // then subscribe to that specific product
        final Organization customer = testOrganizations.get(0);
        PlatformUser customerAdmin = testUsers.get(customer).get(0);

        prepareSubscription(sourceProductCustomerSpecific);
        // remember the subscription
        Subscription storedSubscription = runTX(new Callable<Subscription>() {
            @Override
            public Subscription call() {
                Query query = mgr.createQuery("SELECT s FROM Subscription s");
                Subscription result = (Subscription) query.getSingleResult();
                load(result.getProduct());
                return result;
            }
        });
        final Product oldSubSpecificProduct = storedSubscription.getProduct();
        final List<DomainObject<?>> productRelatedObjects = getProductRelatedObjects(
                oldSubSpecificProduct);

        container.login(String.valueOf(customerAdmin.getKey()),
                ROLE_ORGANIZATION_ADMIN);

        // finally migrate
        List<VOService> products = servProv
                .getServicesForMarketplace(mp.getMarketplaceId());
        VOService targetProduct = null;
        for (VOService product : products) {
            if (product.getKey() == targetProductTemplate.getKey()) {
                targetProduct = product;
                break;
            }
        }
        VOSubscription currentSub = subMgmt
                .getSubscriptionDetails(storedSubscription.getSubscriptionId());
        VOPaymentInfo voPaymentInfo = getPaymentInfo(
                tpAndSupplier.getOrganizationId(), testOrganizations.get(0));
        VOBillingContact bc = createBillingContact(testOrganizations.get(0));

        assertNotNull("no target product found", targetProduct);
        subMgmt.upgradeSubscription(currentSub, targetProduct, voPaymentInfo,
                bc, new ArrayList<VOUda>());

        // check amount of products and their settings
        List<Product> allMyProducts = runTX(new Callable<List<Product>>() {
            @Override
            public List<Product> call() {
                Query query = mgr.createQuery("SELECT p FROM Product p");
                return ParameterizedTypes.list(query.getResultList(),
                        Product.class);
            }
        });
        List<Product> allCustomerCopies = new ArrayList<>();
        for (Product prod : allMyProducts) {
            if (prod.getTemplate() != null
                    && prod.getTargetCustomer() != null) {
                allCustomerCopies.add(prod);
            }
            if (prod.getKey() == oldSubSpecificProduct.getKey()) {
                Assert.fail(
                        "The old subscription specific product must not exist anymore");
            }
        }
        assertEquals("Only one customer specific product must exist", 1,
                allCustomerCopies.size());

        // now verify that the product (old subscription product) has been
        // removed as well as all depending entities, also verify the history
        // entries of them
        verifyDeletion(productRelatedObjects);
    }

    @Test
    public void testSubscribeToProductIntNoUsers() throws Throwable {
        assertNotNull(subMgmtLocal);
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                subMgmtLocal.subscribeToServiceInt(
                        createTriggerProcessForSubscription(
                                "testSubscribeToProduct"));
                return null;
            }
        });
    }

    @Test
    public void testSubscribeToProductInt_OnlyOneSubscriptionPerUser()
            throws Throwable {
        assertNotNull(subMgmtLocal);

        // activate the 'onlyOneSubscriptionPerUser' flag
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                TechnicalProduct techProduct = mgr.getReference(
                        TechnicalProduct.class,
                        testProducts.get(0).getTechnicalProduct().getKey());
                techProduct.setOnlyOneSubscriptionAllowed(true);
                return null;
            }
        });

        // try to subscribe twice
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                subMgmtLocal.subscribeToServiceInt(
                        createTriggerProcessForSubscription(
                                "testSubscribeToProduct1"));
                try {
                    subMgmtLocal.subscribeToServiceInt(
                            createTriggerProcessForSubscription(
                                    "testSubscribeToProduct2"));
                    fail("SubscriptionAlreadyExistsException expected");
                } catch (SubscriptionAlreadyExistsException e) {
                    // expected
                }
                return null;
            }
        });
    }

    private TriggerProcess createTriggerProcessForSubscription(
            String subscriptionId) throws Exception {
        VOService product = getProductToSubscribe(testProducts.get(0).getKey());
        VOUser[] users = new VOUser[0];
        VOUser[] admins = new VOUser[0];
        VOSubscription sub = Subscriptions.createVOSubscription(subscriptionId);
        TriggerProcess tp = new TriggerProcess();
        tp.addTriggerProcessParameter(TriggerProcessParameterName.SUBSCRIPTION,
                sub);
        tp.addTriggerProcessParameter(TriggerProcessParameterName.PRODUCT,
                product);
        tp.addTriggerProcessParameter(TriggerProcessParameterName.USERS,
                getUsersToAdd(admins, users));
        VOPaymentInfo pi = getPaymentInfo(tpAndSupplier.getOrganizationId(),
                testOrganizations.get(0));
        tp.addTriggerProcessParameter(TriggerProcessParameterName.PAYMENTINFO,
                pi);
        VOBillingContact bc = createBillingContact(testOrganizations.get(0));
        tp.addTriggerProcessParameter(
                TriggerProcessParameterName.BILLING_CONTACT, bc);
        tp.addTriggerProcessParameter(TriggerProcessParameterName.UDAS,
                new ArrayList<VOUda>());
        tp.setUser(testUsers.get(testOrganizations.get(0)).get(1));
        return tp;
    }

    @Test
    public void testUnsubscribeNonSuspendingTrigger() throws Exception {
        // setup trigger
        container.login(String.valueOf(supplierUser.getKey()),
                ROLE_SERVICE_MANAGER);
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    createTriggerDefinition(
                            TriggerType.SUBSCRIPTION_TERMINATION, false);
                    return null;
                }
            });
        } finally {
        }
        // setup subscription
        final TriggerProcess tp = new TriggerProcess();
        final Subscription sub = createSubscription();
        String subId = sub.getSubscriptionId();
        tp.addTriggerProcessParameter(TriggerProcessParameterName.SUBSCRIPTION,
                subId);

        PlatformUser user = runTX(new Callable<PlatformUser>() {
            @Override
            public PlatformUser call() throws Exception {
                Organization organization = mgr.getReference(Organization.class,
                        sub.getOrganization().getKey());
                return organization.getPlatformUsers().get(0);
            }
        });
        // when
        container.login(user.getKey(), ROLE_ORGANIZATION_ADMIN);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                subMgmtLocal.unsubscribeFromServiceInt(tp);
                return null;
            }
        });
        // then
        assertTrue(isCorrectSubscriptionIdForMail);
        assertTrue(isTriggerQueueService_sendAllNonSuspendingMessageCalled);
        assertTrue(isMessageSend(TriggerType.SUBSCRIPTION_TERMINATION));
    }

    private void createTriggerDefinition(final TriggerType type,
            final boolean isSuspendable) throws Exception {
        runTX(new Callable<TriggerDefinition>() {
            @Override
            public TriggerDefinition call() throws Exception {
                TriggerDefinition td = new TriggerDefinition();
                td.setOrganization(tpAndSupplier);
                td.setTargetType(TriggerTargetType.WEB_SERVICE);
                td.setTarget("some URL");
                td.setType(type);
                td.setSuspendProcess(isSuspendable);
                td.setName("testTrigger");
                mgr.persist(td);
                mgr.flush();
                return td;
            }
        });
    }

    @Test
    public void testSubscribeToProductInt() throws Throwable {
        assertNotNull(subMgmtLocal);
        VOService product = getProductToSubscribe(testProducts.get(0).getKey());
        VOUser[] users = new VOUser[2];
        VOUser[] admins = new VOUser[1];
        setUsers(users, admins);
        VOSubscription sub = Subscriptions
                .createVOSubscription("testSubscribeToProduct");

        final TriggerProcess tp = new TriggerProcess();
        tp.addTriggerProcessParameter(TriggerProcessParameterName.SUBSCRIPTION,
                sub);
        tp.addTriggerProcessParameter(TriggerProcessParameterName.PRODUCT,
                product);
        tp.addTriggerProcessParameter(TriggerProcessParameterName.USERS,
                getUsersToAdd(admins, users));
        VOPaymentInfo pi = getPaymentInfo(tpAndSupplier.getOrganizationId(),
                testOrganizations.get(0));
        tp.addTriggerProcessParameter(TriggerProcessParameterName.PAYMENTINFO,
                pi);
        VOBillingContact bc = createBillingContact(testOrganizations.get(0));
        tp.addTriggerProcessParameter(
                TriggerProcessParameterName.BILLING_CONTACT, bc);
        tp.addTriggerProcessParameter(TriggerProcessParameterName.UDAS,
                new ArrayList<VOUda>());
        tp.setUser(testUsers.get(testOrganizations.get(0)).get(1));
        final Subscription newSub = runTX(new Callable<Subscription>() {
            @Override
            public Subscription call() throws Exception {
                return subMgmtLocal.subscribeToServiceInt(tp);
            }
        });

        // Now check results
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    checkSubscribeToProduct(false, "testSubscribeToProduct",
                            testProducts.get(0), SubscriptionStatus.ACTIVE, 3,
                            null, 1);

                    long key = testProducts.get(0).getKey();
                    Product product = mgr.getReference(Product.class, key);
                    assertNotNull("Catalog entry for product expected",
                            product.getCatalogEntries().get(0));
                    assertNotNull("New subscription object expected", newSub);
                    Subscription sub = mgr.getReference(Subscription.class,
                            newSub.getKey());
                    assertNotNull("Persisted subscription expected", sub);
                    Product subProduct = sub.getProduct();
                    assertNotNull("Subscription product copy expected",
                            subProduct);
                    assertTrue(
                            "No catalog entry for subscription product copy expected",
                            subProduct.getCatalogEntries().isEmpty());
                    assertNull(subProduct.getConfiguratorUrl());
                    return null;
                }
            });
        } catch (Exception e) {
            throw e.getCause();
        }
        assertNotNull(newSub);
        assertTrue(isTriggerQueueService_sendAllNonSuspendingMessageCalled);
        assertTrue(isMessageSend(TriggerType.SUBSCRIBE_TO_SERVICE));
    }

    @Test
    public void testSubscribeToProductWithTrigger_AutoAssignUser()
            throws Throwable {
        assertNotNull(subMgmtLocal);
        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                Product product = mgr.getReference(Product.class, testProducts
                        .get(0).getKey());
                product.setAutoAssignUserEnabled(Boolean.TRUE);
                return null;
            }
        });
        VOService product = getProductToSubscribe(testProducts.get(0).getKey());
        product.setAutoAssignUserEnabled(Boolean.TRUE);

        VOSubscription sub = Subscriptions
                .createVOSubscription("testSubscribeToProduct");

        final TriggerProcess tp = new TriggerProcess();
        TriggerDefinition tpDef = new TriggerDefinition();
        tpDef.setType(TriggerType.SUBSCRIBE_TO_SERVICE);

        tp.setTriggerDefinition(tpDef);

        tp.addTriggerProcessParameter(TriggerProcessParameterName.SUBSCRIPTION,
                sub);
        tp.addTriggerProcessParameter(TriggerProcessParameterName.PRODUCT,
                product);
        tp.addTriggerProcessParameter(TriggerProcessParameterName.USERS, null);
        VOPaymentInfo pi = getPaymentInfo(tpAndSupplier.getOrganizationId(),
                testOrganizations.get(0));
        tp.addTriggerProcessParameter(TriggerProcessParameterName.PAYMENTINFO,
                pi);
        VOBillingContact bc = createBillingContact(testOrganizations.get(0));
        tp.addTriggerProcessParameter(
                TriggerProcessParameterName.BILLING_CONTACT, bc);
        tp.addTriggerProcessParameter(TriggerProcessParameterName.UDAS,
                new ArrayList<VOUda>());
        tp.setUser(testUsers.get(testOrganizations.get(0)).get(1));

        final Subscription newSub = runTX(new Callable<Subscription>() {
            @Override
            public Subscription call() throws Exception {
                return subMgmtLocal.subscribeToServiceInt(tp);
            }
        });

        // Now check results
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    checkSubscribeToProduct(false, "testSubscribeToProduct",
                            testProducts.get(0), SubscriptionStatus.ACTIVE, 1,
                            null, 1);

                    long key = testProducts.get(0).getKey();
                    Product product = mgr.getReference(Product.class, key);
                    assertNotNull("Catalog entry for product expected",
                            product.getCatalogEntries().get(0));
                    assertNotNull("New subscription object expected", newSub);
                    Subscription sub = mgr.getReference(Subscription.class,
                            newSub.getKey());
                    assertNotNull("Persisted subscription expected", sub);
                    Product subProduct = sub.getProduct();
                    assertNotNull("Subscription product copy expected",
                            subProduct);
                    assertTrue(
                            "No catalog entry for subscription product copy expected",
                            subProduct.getCatalogEntries().isEmpty());
                    assertNull(subProduct.getConfiguratorUrl());
                    return null;
                }
            });
        } catch (Exception e) {
            throw e.getCause();
        }
        assertNotNull(newSub);
        assertTrue(isTriggerQueueService_sendAllNonSuspendingMessageCalled);
        assertTrue(isMessageSend(TriggerType.SUBSCRIBE_TO_SERVICE));
    }

    @Test
    public void testSubscribeToProductWithTrigger_AutoAssignUser2()
            throws Throwable {
        assertNotNull(subMgmtLocal);
        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                Product product = mgr.getReference(Product.class, testProducts
                        .get(0).getKey());
                product.setAutoAssignUserEnabled(Boolean.TRUE);
                return null;
            }
        });
        VOService product = getProductToSubscribe(testProducts.get(0).getKey());
        product.setAutoAssignUserEnabled(Boolean.TRUE);

        VOSubscription sub = Subscriptions
                .createVOSubscription("testSubscribeToProduct");

        final TriggerProcess tp = new TriggerProcess();
        TriggerDefinition tpDef = new TriggerDefinition();
        tpDef.setType(TriggerType.SUBSCRIBE_TO_SERVICE);

        tp.setTriggerDefinition(tpDef);

        tp.addTriggerProcessParameter(TriggerProcessParameterName.SUBSCRIPTION,
                sub);
        tp.addTriggerProcessParameter(TriggerProcessParameterName.PRODUCT,
                product);
        tp.addTriggerProcessParameter(TriggerProcessParameterName.USERS, null);
        VOPaymentInfo pi = getPaymentInfo(tpAndSupplier.getOrganizationId(),
                testOrganizations.get(0));
        tp.addTriggerProcessParameter(TriggerProcessParameterName.PAYMENTINFO,
                pi);
        VOBillingContact bc = createBillingContact(testOrganizations.get(0));
        tp.addTriggerProcessParameter(
                TriggerProcessParameterName.BILLING_CONTACT, bc);
        tp.addTriggerProcessParameter(TriggerProcessParameterName.UDAS,
                new ArrayList<VOUda>());
        tp.setUser(testUsers.get(testOrganizations.get(0)).get(0));

        final Subscription newSub = runTX(new Callable<Subscription>() {
            @Override
            public Subscription call() throws Exception {
                return subMgmtLocal.subscribeToServiceInt(tp);
            }
        });

        // Now check results
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    checkSubscribeToProduct(false, "testSubscribeToProduct",
                            testProducts.get(0), SubscriptionStatus.ACTIVE, 1,
                            null, 0);

                    long key = testProducts.get(0).getKey();
                    Product product = mgr.getReference(Product.class, key);
                    assertNotNull("Catalog entry for product expected",
                            product.getCatalogEntries().get(0));
                    assertNotNull("New subscription object expected", newSub);
                    Subscription sub = mgr.getReference(Subscription.class,
                            newSub.getKey());
                    assertNotNull("Persisted subscription expected", sub);
                    Product subProduct = sub.getProduct();
                    assertNotNull("Subscription product copy expected",
                            subProduct);
                    assertTrue(
                            "No catalog entry for subscription product copy expected",
                            subProduct.getCatalogEntries().isEmpty());
                    assertNull(subProduct.getConfiguratorUrl());
                    return null;
                }
            });
        } catch (Exception e) {
            throw e.getCause();
        }
        assertNotNull(newSub);
        assertTrue(isTriggerQueueService_sendAllNonSuspendingMessageCalled);
        assertTrue(isMessageSend(TriggerType.SUBSCRIBE_TO_SERVICE));
    }

    @Test
    public void testSubscribeToProductWithTrigger_AutoAssignUsers()
            throws Throwable {
        assertNotNull(subMgmtLocal);
        VOService product = getProductToSubscribe(testProducts.get(0).getKey());
        product.setAutoAssignUserEnabled(Boolean.TRUE);

        VOUser[] users = new VOUser[2];
        VOUser[] admins = new VOUser[1];
        setUsers(users, admins);
        VOSubscription sub = Subscriptions
                .createVOSubscription("testSubscribeToProduct");

        final TriggerProcess tp = new TriggerProcess();
        TriggerDefinition tpDef = new TriggerDefinition();
        tpDef.setType(TriggerType.SUBSCRIBE_TO_SERVICE);

        tp.setTriggerDefinition(tpDef);

        tp.addTriggerProcessParameter(TriggerProcessParameterName.SUBSCRIPTION,
                sub);
        tp.addTriggerProcessParameter(TriggerProcessParameterName.PRODUCT,
                product);
        tp.addTriggerProcessParameter(TriggerProcessParameterName.USERS,
                getUsersToAdd(admins, users));
        VOPaymentInfo pi = getPaymentInfo(tpAndSupplier.getOrganizationId(),
                testOrganizations.get(0));
        tp.addTriggerProcessParameter(TriggerProcessParameterName.PAYMENTINFO,
                pi);
        VOBillingContact bc = createBillingContact(testOrganizations.get(0));
        tp.addTriggerProcessParameter(
                TriggerProcessParameterName.BILLING_CONTACT, bc);
        tp.addTriggerProcessParameter(TriggerProcessParameterName.UDAS,
                new ArrayList<VOUda>());
        tp.setUser(testUsers.get(testOrganizations.get(0)).get(1));

        final Subscription newSub = runTX(new Callable<Subscription>() {
            @Override
            public Subscription call() throws Exception {
                return subMgmtLocal.subscribeToServiceInt(tp);
            }
        });

        // Now check results
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    checkSubscribeToProduct(false, "testSubscribeToProduct",
                            testProducts.get(0), SubscriptionStatus.ACTIVE, 3,
                            null, 1);

                    long key = testProducts.get(0).getKey();
                    Product product = mgr.getReference(Product.class, key);
                    assertNotNull("Catalog entry for product expected",
                            product.getCatalogEntries().get(0));
                    assertNotNull("New subscription object expected", newSub);
                    Subscription sub = mgr.getReference(Subscription.class,
                            newSub.getKey());
                    assertNotNull("Persisted subscription expected", sub);
                    Product subProduct = sub.getProduct();
                    assertNotNull("Subscription product copy expected",
                            subProduct);
                    assertTrue(
                            "No catalog entry for subscription product copy expected",
                            subProduct.getCatalogEntries().isEmpty());
                    assertNull(subProduct.getConfiguratorUrl());
                    return null;
                }
            });
        } catch (Exception e) {
            throw e.getCause();
        }
        assertNotNull(newSub);
        assertTrue(isTriggerQueueService_sendAllNonSuspendingMessageCalled);
        assertTrue(isMessageSend(TriggerType.SUBSCRIBE_TO_SERVICE));
    }

    @Test
    public void testSubscribeToProductWithTrigger_AutoAssignUsers2()
            throws Throwable {
        assertNotNull(subMgmtLocal);
        VOService product = getProductToSubscribe(testProducts.get(0).getKey());
        product.setAutoAssignUserEnabled(Boolean.TRUE);

        VOUser[] users = new VOUser[2];
        VOUser[] admins = new VOUser[1];
        setUsers(users, admins);
        VOSubscription sub = Subscriptions
                .createVOSubscription("testSubscribeToProduct");

        final TriggerProcess tp = new TriggerProcess();
        TriggerDefinition tpDef = new TriggerDefinition();
        tpDef.setType(TriggerType.SUBSCRIBE_TO_SERVICE);

        tp.setTriggerDefinition(tpDef);

        tp.addTriggerProcessParameter(TriggerProcessParameterName.SUBSCRIPTION,
                sub);
        tp.addTriggerProcessParameter(TriggerProcessParameterName.PRODUCT,
                product);
        tp.addTriggerProcessParameter(TriggerProcessParameterName.USERS,
                getUsersToAdd(admins, users));
        VOPaymentInfo pi = getPaymentInfo(tpAndSupplier.getOrganizationId(),
                testOrganizations.get(0));
        tp.addTriggerProcessParameter(TriggerProcessParameterName.PAYMENTINFO,
                pi);
        VOBillingContact bc = createBillingContact(testOrganizations.get(0));
        tp.addTriggerProcessParameter(
                TriggerProcessParameterName.BILLING_CONTACT, bc);
        tp.addTriggerProcessParameter(TriggerProcessParameterName.UDAS,
                new ArrayList<VOUda>());
        tp.setUser(testUsers.get(testOrganizations.get(0)).get(2));

        final Subscription newSub = runTX(new Callable<Subscription>() {
            @Override
            public Subscription call() throws Exception {
                return subMgmtLocal.subscribeToServiceInt(tp);
            }
        });

        // Now check results
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    checkSubscribeToProduct(false, "testSubscribeToProduct",
                            testProducts.get(0), SubscriptionStatus.ACTIVE, 3,
                            null, 1);

                    long key = testProducts.get(0).getKey();
                    Product product = mgr.getReference(Product.class, key);
                    assertNotNull("Catalog entry for product expected",
                            product.getCatalogEntries().get(0));
                    assertNotNull("New subscription object expected", newSub);
                    Subscription sub = mgr.getReference(Subscription.class,
                            newSub.getKey());
                    assertNotNull("Persisted subscription expected", sub);
                    Product subProduct = sub.getProduct();
                    assertNotNull("Subscription product copy expected",
                            subProduct);
                    assertTrue(
                            "No catalog entry for subscription product copy expected",
                            subProduct.getCatalogEntries().isEmpty());
                    assertNull(subProduct.getConfiguratorUrl());
                    return null;
                }
            });
        } catch (Exception e) {
            throw e.getCause();
        }
        assertNotNull(newSub);
        assertTrue(isTriggerQueueService_sendAllNonSuspendingMessageCalled);
        assertTrue(isMessageSend(TriggerType.SUBSCRIBE_TO_SERVICE));
    }

    @Test
    public void testCreateProductWithRolePricesAndSubscribe() throws Exception {
        final Organization testCustomer = testOrganizations.get(0);

        final VOPaymentInfo voPaymentInfo = getPaymentInfo(
                tpAndSupplier.getOrganizationId(), testOrganizations.get(0));
        final VOBillingContact bc = createBillingContact(
                testOrganizations.get(0));

        final Product product = runTX(new Callable<Product>() {
            @Override
            public Product call() throws Exception {
                TechnicalProduct tp = TechnicalProducts.createTechnicalProduct(
                        mgr, tpAndSupplier, "TProdWithRolePrices", false,
                        ServiceAccessType.LOGIN);

                RoleDefinition rd = new RoleDefinition();
                rd.setRoleId("TP-ROLE");
                rd.setTechnicalProduct(tp);
                mgr.persist(rd);
                tp.setRoleDefinitions(Collections.singletonList(rd));

                Product prod = Products.createProduct(tpAndSupplier, tp, true,
                        "prodId", null, mgr);

                // now edit the price model and set a price for the role...
                PricedProductRole ppr = new PricedProductRole();
                ppr.setPricePerUser(new BigDecimal(123));
                ppr.setRoleDefinition(rd);
                ppr.setPriceModel(prod.getPriceModel());
                prod.getPriceModel().getRoleSpecificUserPrices().add(ppr);

                // set payment for customer
                Organization customer = mgr.getReference(Organization.class,
                        testCustomer.getKey());
                PaymentType pt = paymentTypes.get(0);

                OrganizationRefToPaymentType otpt = new OrganizationRefToPaymentType();
                otpt.setOrganizationReference(customer.getSources().get(0));
                otpt.setOrganizationRole(customer.getGrantedRoles().iterator()
                        .next().getOrganizationRole());
                otpt.setPaymentType(pt);
                mgr.persist(otpt);

                // allow payment for service
                ProductToPaymentType ptpt = new ProductToPaymentType(prod, pt);
                mgr.persist(ptpt);
                return prod;
            }
        });

        final LocalizerFacade facade = new LocalizerFacade(localizer, "en");
        final VOService voProd = runTX(new Callable<VOService>() {

            @Override
            public VOService call() {
                Product prod = (Product) mgr.find(product);
                return ProductAssembler.toVOProduct(prod, facade);
            }
        });
        VORoleDefinition roleDef = RoleAssembler.toVORoleDefinition(
                product.getTechnicalProduct().getRoleDefinitions().get(0),
                facade);

        // now subscribe and obtain subscription specific product details
        VOUser[] users = new VOUser[2];
        VOUser[] admins = new VOUser[1];
        admins[0] = UserDataAssembler
                .toVOUser(testUsers.get(testCustomer).get(1));
        users[0] = UserDataAssembler
                .toVOUser(testUsers.get(testCustomer).get(2));
        users[1] = UserDataAssembler
                .toVOUser(testUsers.get(testCustomer).get(3));
        VOSubscription subscription = Subscriptions
                .createVOSubscription("smee");
        container.login(
                String.valueOf(testUsers.get(testCustomer).get(1).getKey()),
                ROLE_ORGANIZATION_ADMIN);

        List<VOUsageLicense> usersToAdd = getUsersToAdd(admins, users);
        for (VOUsageLicense voUsageLicense : usersToAdd) {
            voUsageLicense.setRoleDefinition(roleDef);
        }
        VOSubscription createdSub = subMgmt.subscribeToService(subscription,
                voProd, usersToAdd, voPaymentInfo, bc, new ArrayList<VOUda>());

        final long subKey = createdSub.getKey();

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Subscription sub = mgr.getReference(Subscription.class, subKey);
                Product subProd = sub.getProduct();
                Product template = subProd.getTemplate();

                assertEquals(sub.getPaymentInfo().getKey(),
                        voPaymentInfo.getKey());
                List<PricedProductRole> templateRolePrices = template
                        .getPriceModel().getRoleSpecificUserPrices();
                assertEquals(1, templateRolePrices.size());

                List<PricedProductRole> subRolePrices = subProd.getPriceModel()
                        .getRoleSpecificUserPrices();
                assertEquals(1, subRolePrices.size());
                PricedProductRole templateRolePrice = templateRolePrices.get(0);
                PricedProductRole copiedRolePrice = subRolePrices.get(0);
                assertEquals(templateRolePrice.getPricePerUser(),
                        copiedRolePrice.getPricePerUser());
                assertEquals(templateRolePrice.getRoleDefinition(),
                        copiedRolePrice.getRoleDefinition());
                assertFalse(templateRolePrice.equals(copiedRolePrice));
                return null;
            }
        });
    }

    /**
     * Determines all product depending objects and adds them to the returned
     * list.
     * 
     * @param oldSubSpecificProduct
     *            The product to find the objects for.
     * @return The list of product related objects including the product itself.
     * @throws Exception
     */
    private List<DomainObject<?>> getProductRelatedObjects(
            final Product oldSubSpecificProduct) throws Exception {
        final List<DomainObject<?>> productRelatedObjects = new ArrayList<>();
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Product currentProduct = mgr.getReference(Product.class,
                        oldSubSpecificProduct.getKey());
                productRelatedObjects.add(currentProduct);
                // handle price model
                PriceModel priceModel = currentProduct.getPriceModel();
                productRelatedObjects.add(priceModel);

                List<PricedEvent> events = priceModel.getConsideredEvents();
                for (PricedEvent event : events) {
                    productRelatedObjects.add(event);
                }

                List<PricedParameter> selectedParams = priceModel
                        .getSelectedParameters();
                for (PricedParameter pricedParam : selectedParams) {
                    productRelatedObjects.add(pricedParam);
                    List<PricedOption> options = pricedParam
                            .getPricedOptionList();
                    for (PricedOption option : options) {
                        productRelatedObjects.add(option);
                    }
                }

                // handle parameter set
                ParameterSet parameterSet = currentProduct.getParameterSet();
                productRelatedObjects.add(parameterSet);
                List<Parameter> params = parameterSet.getParameters();
                for (Parameter param : params) {
                    productRelatedObjects.add(param);
                }

                // handle product references outgoing
                List<ProductReference> refs = currentProduct
                        .getCompatibleProducts();
                for (ProductReference ref : refs) {
                    productRelatedObjects.add(ref);
                }

                // handle product references incoming
                refs = currentProduct.getCompatibleProductsTarget();
                for (ProductReference ref : refs) {
                    productRelatedObjects.add(ref);
                }
                load(productRelatedObjects);
                return null;
            }
        });
        productRelatedObjects.add(oldSubSpecificProduct);
        return productRelatedObjects;
    }

    /**
     * Tries to retrieve the specified domain object (identified by the key
     * value). If it is found, the test will fail.
     * 
     * @param searchTemplates
     *            The objects that must not exist anymore.
     * @throws Exception
     */
    private void verifyDeletion(final List<DomainObject<?>> searchTemplates)
            throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                for (DomainObject<?> searchTemplate : searchTemplates) {
                    @SuppressWarnings("unchecked")
                    Class<? extends DomainObject<?>> searchedClass = (Class<? extends DomainObject<?>>) DomainObject
                            .getDomainClass(searchTemplate);
                    DomainObject<?> find = mgr.find(searchedClass,
                            searchTemplate.getKey());
                    assertNull("Object of type '" + searchTemplate.getClass()
                            + "' with key '" + searchTemplate.getKey()
                            + "' was not deleted", find);

                    if (searchTemplate.hasHistory()) {
                        List<DomainHistoryObject<?>> historyEntries = mgr
                                .findHistory(searchTemplate);
                        DomainHistoryObject<?> lastHistoryEntry = historyEntries
                                .get(historyEntries.size() - 1);
                        assertEquals(
                                "last entry must indicate deletion, failed for type '"
                                        + searchTemplate.getClass()
                                        + "' with key '"
                                        + searchTemplate.getKey() + "'.",
                                ModificationType.DELETE,
                                lastHistoryEntry.getModtype());
                    }
                }
                return null;
            }
        });
    }

    private VOSubscription prepareSubscription(final Product template)
            throws Exception {
        VOServiceDetails details = runTX(new Callable<VOServiceDetails>() {

            @Override
            public VOServiceDetails call() {
                Product find = mgr.find(Product.class, template.getKey());
                return ProductAssembler.toVOProductDetails(find,
                        new ArrayList<ParameterDefinition>(),
                        new ArrayList<Event>(), false,
                        new LocalizerFacade(localizer, "en"));
            }
        });
        VOUser[] admins = new VOUser[1];
        admins[0] = UserDataAssembler
                .toVOUser(testUsers.get(testOrganizations.get(0)).get(1));
        VOSubscription sub = Subscriptions
                .createVOSubscription(UUID.randomUUID().toString());
        VOPaymentInfo voPaymentInfo = getPaymentInfo(
                tpAndSupplier.getOrganizationId(), testOrganizations.get(0));
        VOBillingContact bc = createBillingContact(testOrganizations.get(0));
        subMgmt.subscribeToService(sub, details,
                getUsersToAdd(admins, new VOUser[0]), voPaymentInfo, bc,
                new ArrayList<VOUda>());
        return subMgmt.getSubscriptionDetails(sub.getSubscriptionId());
    }

    private Product prepareCustomerProduct(final Product template)
            throws Exception {
        return runTX(new Callable<Product>() {
            @Override
            public Product call() throws Exception {
                List<PlatformUser> users = testUsers
                        .get(testOrganizations.get(0));
                testOrganizations.set(0, mgr.find(Organization.class,
                        testOrganizations.get(0).getKey()));
                testUsers.put(testOrganizations.get(0), users);
                Product find = mgr.find(Product.class, template.getKey());
                Product copy = find.copyForCustomer(testOrganizations.get(0));
                mgr.persist(copy);
                return copy;
            }
        });
    }

    private Product[] prepareTemplates() throws Exception {
        return runTX(new Callable<Product[]>() {
            @Override
            public Product[] call() {
                testProducts.set(10,
                        mgr.find(Product.class, testProducts.get(10).getKey()));
                testProducts.set(11,
                        mgr.find(Product.class, testProducts.get(11).getKey()));
                testProducts.set(12,
                        mgr.find(Product.class, testProducts.get(12).getKey()));
                testProducts.get(10).setStatus(ServiceStatus.INACTIVE);
                testProducts.get(11).setStatus(ServiceStatus.INACTIVE);
                testProducts.get(12).setStatus(ServiceStatus.INACTIVE);
                return new Product[] { testProducts.get(10),
                        testProducts.get(11) };
            }
        });
    }

    private Product activate(final Product product) throws Exception {
        return runTX(new Callable<Product>() {
            @Override
            public Product call() {
                Product find = mgr.find(Product.class, product.getKey());
                find.setStatus(ServiceStatus.ACTIVE);
                return find;
            }
        });
    }

    /**
     * Helper method to subscribe.
     * 
     * @return Subscription id.
     * @throws Exception
     */
    private String commonSubscribeToProduct() throws Exception {
        assertNotNull(subMgmt);
        VOService product = getProductToSubscribe(testProducts.get(3).getKey());
        VOUser[] users = new VOUser[2];
        VOUser[] admins = new VOUser[1];
        setUsers(users, admins);
        final String id = "testModifyPon";
        VOPaymentInfo voPaymentInfo = getPaymentInfo(
                tpAndSupplier.getOrganizationId(), testOrganizations.get(0));
        VOBillingContact bc = createBillingContact(testOrganizations.get(0));

        subMgmt.subscribeToService(Subscriptions.createVOSubscription(id),
                product, getUsersToAdd(admins, users), voPaymentInfo, bc,
                new ArrayList<VOUda>());
        return id;
    }

    /**
     * Creates a value object based on the domain object.
     * 
     * @param parameterDefinition
     *            The parameter definition serving as template.
     * @param isCorrectTest
     *            Indicates whether to use the parameter id as defined in the
     *            given parameter definition or not.
     * @return The parameter definition in value object representation.
     */
    private VOParameterDefinition getVOParameterDefinition(
            ParameterDefinition parameterDefinition, boolean isCorrectTest) {
        ParameterType parameterType = parameterDefinition.getParameterType();
        String parameterId = null;
        if (isCorrectTest) {
            parameterId = parameterDefinition.getParameterId();
        } else {
            // wrong id for initiating OperationNotPermitted exception
            parameterId = "123";
        }
        String description = "";
        ParameterValueType valueType = parameterDefinition.getValueType();
        String defaultValue = parameterDefinition.getDefaultValue();
        Long minValue = parameterDefinition.getMinimumValue();
        Long maxValue = parameterDefinition.getMaximumValue();
        boolean mandatory = parameterDefinition.isMandatory();
        boolean configurable = true; // parameterDefinition.isConfigurable();
        List<VOParameterOption> parameterOptions = null;
        VOParameterDefinition parameterDefinitionVO = new VOParameterDefinition(
                parameterType, parameterId, description, valueType,
                defaultValue, minValue, maxValue, mandatory, configurable,
                parameterOptions);
        return parameterDefinitionVO;
    }

    /**
     * Base method for testing subscription modification for product with
     * parameters.
     * 
     * @param isConfigurable
     * @param parameterValue
     * @param isNewValueTheSame
     * @param isParameterExist
     * @throws Throwable
     */
    private void baseTestModifyWithParameters(final boolean isConfigurable,
            final String parameterValue, Boolean isNewValueTheSame,
            boolean isParameterExist) throws Throwable {
        // create a test subscription
        final String id = commonSubscribeToProduct();
        // get just created subscription for getting correct reference to new
        // product instance
        final Subscription subscription = runTX(new Callable<Subscription>() {
            @Override
            public Subscription call() {
                Subscription returnSubscription = getSubscription(id,
                        testOrganizations.get(0).getKey());
                load(returnSubscription.getProduct());
                return returnSubscription;
            }
        });
        // get VOSubscription, which is needed as tested method argument
        VOSubscription subToModify = runTX(new Callable<VOSubscription>() {
            @Override
            public VOSubscription call() {
                Subscription subscription = getSubscription(id,
                        testOrganizations.get(0).getKey());
                return SubscriptionAssembler.toVOSubscription(subscription,
                        new LocalizerFacade(new LocalizerServiceStub2(), "en"));
            }
        });
        // get product id, it will be a new product with ID like
        // <old_product_id>#<generated_number>
        final String productID = subscription.getProduct().getProductId();
        // create parameter for subscription product
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                doSetParameterSetForProduct(productID, isConfigurable,
                        parameterValue);
                return null;
            }
        });
        // get just created parameters for preparation as method argument - but
        // only those for the product
        final Product product = subscription.getProduct();
        List<Parameter> parameters = new ArrayList<>();
        parameters = runTX(new Callable<List<Parameter>>() {
            @Override
            public List<Parameter> call() throws Exception {
                Product currentProduct = mgr.getReference(Product.class,
                        product.getKey());
                List<Parameter> result = currentProduct.getParameterSet()
                        .getParameters();
                for (int i = 0; i < result.size(); i++) {
                    ParameterDefinition paramDef = result.get(i)
                            .getParameterDefinition();
                    result.get(i).getParameterDefinition().getValueType();
                    result.get(i).setParameterDefinition(paramDef);
                }
                // result.size(); // only to avoid lazy initialization problems
                return result;
            }
        });
        final List<VOParameter> modifiedParameters = new ArrayList<>();
        // prepare VOParameter list for using as method argument
        for (final Parameter parameter : parameters) {
            final long parameterDefinitionKey = parameter
                    .getParameterDefinition().getKey();
            // get parameter definition
            ParameterDefinition parameterDefinition = runTX(
                    new Callable<ParameterDefinition>() {
                        @Override
                        public ParameterDefinition call() {
                            ParameterDefinition parameterDefinition = mgr.find(
                                    ParameterDefinition.class,
                                    parameterDefinitionKey);
                            return parameterDefinition;
                        }
                    });
            // prepare arguments for instantiation VOParameterDefinition
            VOParameterDefinition parameterDefinitionVO = getVOParameterDefinition(
                    parameterDefinition, true);
            // prepare VOParameter for argument list
            VOParameter voParam = new VOParameter(parameterDefinitionVO);
            if (isNewValueTheSame == null) {
                // set null value, if configurable
                voParam.setValue(null);
            } else {
                if (isNewValueTheSame.equals(Boolean.TRUE)) {
                    // set the same value
                    voParam.setValue(parameter.getValue());
                } else {
                    String value = parameter.getValue();
                    StringBuffer str = new StringBuffer();
                    if (value != null) {
                        str.append(value);
                    }
                    str.append("12");
                    voParam.setValue(str.toString());
                }
            }
            voParam.setConfigurable(parameter.isConfigurable());
            // list for using as argument
            if (parameter.isConfigurable() || !isConfigurable) {
                modifiedParameters.add(voParam);
            }
            // add not existing parameter for negative test
            if (!isParameterExist) {
                // prepare arguments for instantiation VOParameterDefinition
                VOParameterDefinition parameterDefinitionVO2 = getVOParameterDefinition(
                        parameterDefinition, false);
                // prepare VOParameter for argument list
                VOParameter voParam2 = new VOParameter(parameterDefinitionVO2);
                modifiedParameters.add(voParam2);
            } else {
                voParam.setKey(parameter.getKey());
                voParam.setVersion(parameter.getVersion());
            }
        }
        // this call will go to checkIfValidParametersAreModified method of
        // Subscription Manager.

        subMgmt.modifySubscription(subToModify, modifiedParameters,
                new ArrayList<VOUda>());
        // check changes
        if (modifiedParameters.size() > 0) {
            runTX(new Callable<Void>() {
                @Override
                public Void call() {
                    // test parameters values
                    Subscription sub = mgr.find(Subscription.class,
                            subscription.getKey());
                    Product currentProduct = sub.getProduct();
                    List<Parameter> productParams = currentProduct
                            .getParameterSet().getParameters();
                    boolean isValueAsserted = false;
                    for (Parameter parameter : productParams) {
                        String value = parameter.getValue();
                        // all test input values are the same, can be the first
                        // list member used
                        if (parameter.getParameterDefinition().getParameterId()
                                .equals(modifiedParameters.get(0)
                                        .getParameterDefinition()
                                        .getParameterId())) {
                            assertEquals("Wrong parameter value",
                                    modifiedParameters.get(0).getValue(),
                                    value);
                            isValueAsserted = true;
                        }
                    }
                    assertTrue("Value has not been asserted", isValueAsserted);
                    return null;
                }
            });
        }
    }

    private void assertUsageLicenseHistory(List<DomainHistoryObject<?>> hist) {
        assertEquals("Wrong history entry", ModificationType.ADD,
                hist.get(hist.size() - 2).getModtype());
        assertEquals("Wrong history entry", ModificationType.MODIFY,
                hist.get(hist.size() - 1).getModtype());
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

    /**
     * Returns the parameter of the subscription related product with the given
     * id, null if none exists.
     * 
     * @param subscription
     *            The subscription the parameter has to belong to.
     * @param parameterId
     *            The identifier of the parameter.
     * @return The parameter.
     * @throws ObjectNotFoundException
     * @throws OperationNotPermittedException
     */
    private VOParameter getParameterById(VOSubscription subscription,
            String parameterId)
            throws ObjectNotFoundException, OperationNotPermittedException {
        List<VOParameter> parameters = subMgmt
                .getSubscriptionDetails(subscription.getSubscriptionId())
                .getSubscribedService().getParameters();
        VOParameter param = null;
        for (VOParameter currentParam : parameters) {
            if (currentParam.getParameterDefinition().getParameterId()
                    .equals(parameterId)) {
                param = currentParam;
            }
        }
        return param;
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
                    Organization reloadedOrg = mgr
                            .getReference(Organization.class, org.getKey());
                    apt.setOrganizationReference(
                            reloadedOrg.getSources().get(0));
                    apt.setPaymentType(type);
                    apt.setOrganizationRole(role);
                    apt.setUsedAsDefault(false);
                    mgr.persist(apt);
                }
                return null;
            }
        });
    }

    private Subscription createSubscription() throws Exception {
        Subscription sub = runTX(new Callable<Subscription>() {
            @Override
            public Subscription call() throws Exception {
                org = Organizations.createOrganization(mgr,
                        OrganizationRoleType.TECHNOLOGY_PROVIDER,
                        OrganizationRoleType.SUPPLIER);
                org.addPlatformUser(Organizations.createUserForOrg(mgr, org,
                        true, "admin"));
                TechnicalProduct techProd = TechnicalProducts
                        .createTechnicalProduct(mgr, org, "techProd", false,
                                ServiceAccessType.DIRECT);
                Products.createProduct(org, techProd, false, "prodId", null,
                        mgr);

                Organization customer = Organizations.createCustomer(mgr, org);
                customer.addPlatformUser(Organizations.createUserForOrg(mgr,
                        customer, true, "admin"));
                Subscription sub = Subscriptions.createSubscription(mgr,
                        customer.getOrganizationId(), "prodId", SUBSCRIPTION_ID,
                        org);
                return sub;
            }
        });
        return sub;
    }

    private VOUsageLicense mapUserToRole(PlatformUser user) {
        VOUsageLicense lic = new VOUsageLicense();
        lic.setUser(UserDataAssembler.toVOUser(user));
        return lic;
    }

    private void createMarketingPermission(final long tpKey,
            final long orgRefKey) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                TechnicalProduct technicalProduct = mgr
                        .find(TechnicalProduct.class, tpKey);
                OrganizationReference reference = mgr
                        .find(OrganizationReference.class, orgRefKey);

                MarketingPermission permission = new MarketingPermission();
                permission.setOrganizationReference(reference);
                permission.setTechnicalProduct(technicalProduct);
                try {
                    mgr.persist(permission);
                } catch (NonUniqueBusinessKeyException e) {
                    // ignore
                }
                return null;
            }
        });
    }

    private OrganizationReference createOrgRef(final long orgKey)
            throws Exception {
        return runTX(new Callable<OrganizationReference>() {
            @Override
            public OrganizationReference call() {
                Organization organization = mgr.find(Organization.class,
                        orgKey);
                OrganizationReference orgRef = new OrganizationReference(
                        organization, organization,
                        OrganizationReferenceType.TECHNOLOGY_PROVIDER_TO_SUPPLIER);
                try {
                    mgr.persist(orgRef);
                } catch (NonUniqueBusinessKeyException e) {
                    // ignore
                    orgRef = (OrganizationReference) mgr.find(orgRef);
                }
                return orgRef;
            }
        });
    }

    private VOServiceDetails createProductWithRoles(String tpId)
            throws Exception {
        OrganizationReference orgRef = createOrgRef(
                supplierUser.getOrganization().getKey());

        servProv.importTechnicalServices(
                TECHNICAL_SERVICES_XML.getBytes("UTF-8"));
        List<VOTechnicalService> technicalProducts = servProv
                .getTechnicalServices(OrganizationRoleType.TECHNOLOGY_PROVIDER);
        VOTechnicalService tp = null;
        for (VOTechnicalService voTechnicalProduct : technicalProducts) {
            createMarketingPermission(voTechnicalProduct.getKey(),
                    orgRef.getKey());
            if (tpId.equals(voTechnicalProduct.getTechnicalServiceId())) {
                tp = voTechnicalProduct;
            }
        }
        if (tp == null) {
            Assert.fail("TP " + tpId + " not found");
            return null;
        }
        VOService voProduct = new VOService();
        voProduct.setServiceId(tpId + "_prod");
        VOServiceDetails prod = servProv.createService(tp, voProduct, null);
        VOPriceModel voPriceModel = new VOPriceModel();
        voPriceModel.setType(PriceModelType.FREE_OF_CHARGE);
        voPriceModel.setCurrencyISOCode("EUR");
        prod = servProv.savePriceModel(prod, voPriceModel);

        VOMarketplace localMp = new VOMarketplace();
        localMp.setMarketplaceId(mp.getMarketplaceId());
        VOCatalogEntry entry = new VOCatalogEntry();
        entry.setMarketplace(localMp);
        mpSvc.publishService(prod, Arrays.asList(entry));

        servProv.activateService(prod);
        prod = servProv.getServiceDetails(prod);
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        return prod;
    }

    @Test
    public void testSubscribeToProductWithServiceRoles() throws Exception {
        container.login(String.valueOf(supplierUser.getKey()),
                new String[] { ROLE_ORGANIZATION_ADMIN, ROLE_TECHNOLOGY_MANAGER,
                        ROLE_SERVICE_MANAGER });
        VOServiceDetails prod = createProductWithRoles("example");
        List<VORoleDefinition> roles = prod.getTechnicalService()
                .getRoleDefinitions();
        assertEquals(3, roles.size());
        VOSubscription sub = Subscriptions.createVOSubscription("sub");
        VORoleDefinition role = roles.get(0);
        subMgmt.subscribeToService(sub, prod, createUsers(role, 2), null, null,
                new ArrayList<VOUda>());
        VOSubscriptionDetails subDetails = subMgmt
                .getSubscriptionDetails(sub.getSubscriptionId());
        List<VOUsageLicense> licenses = subDetails.getUsageLicenses();
        assertEquals(2, licenses.size());
        for (VOUsageLicense lic : licenses) {
            VORoleDefinition assignedRole = lic.getRoleDefinition();
            assertNotNull(assignedRole);
            assertEquals(role.getKey(), assignedRole.getKey());
        }
    }

    @Test
    public void testSubscribeToProductWithoutServiceRolesSetServiceRole()
            throws Exception {
        container.login(String.valueOf(supplierUser.getKey()),
                new String[] { ROLE_ORGANIZATION_ADMIN, ROLE_TECHNOLOGY_MANAGER,
                        ROLE_SERVICE_MANAGER });
        VORoleDefinition role = createOtherRole();
        VOServiceDetails prod = createProductWithRoles("ssh");
        assertTrue(prod.getTechnicalService().getRoleDefinitions().isEmpty());
        VOSubscription sub = Subscriptions.createVOSubscription("sub");
        subMgmt.subscribeToService(sub, prod, createUsers(role, 2), null, null,
                new ArrayList<VOUda>());
        VOSubscriptionDetails subDetails = subMgmt
                .getSubscriptionDetails(sub.getSubscriptionId());
        List<VOUsageLicense> licenses = subDetails.getUsageLicenses();
        assertEquals(2, licenses.size());
        for (VOUsageLicense lic : licenses) {
            assertNull(lic.getRoleDefinition());
        }
    }

    @Test
    public void testSubscribeToProductWithServiceRolesSetAdmin()
            throws Exception {
        container.login(String.valueOf(supplierUser.getKey()),
                new String[] { ROLE_ORGANIZATION_ADMIN, ROLE_TECHNOLOGY_MANAGER,
                        ROLE_SERVICE_MANAGER });
        VOServiceDetails prod = createProductWithRoles("example");
        List<VORoleDefinition> roles = prod.getTechnicalService()
                .getRoleDefinitions();
        assertEquals(3, roles.size());
        VOSubscription sub = Subscriptions.createVOSubscription("sub");
        VORoleDefinition role = roles.get(0);
        subMgmt.subscribeToService(sub, prod, createUsers(role, 2), null, null,
                new ArrayList<VOUda>());
        VOSubscriptionDetails subDetails = subMgmt
                .getSubscriptionDetails(sub.getSubscriptionId());
        List<VOUsageLicense> licenses = subDetails.getUsageLicenses();
        assertEquals(2, licenses.size());
        for (VOUsageLicense lic : licenses) {
            VORoleDefinition assignedRole = lic.getRoleDefinition();
            assertNotNull(assignedRole);
            assertEquals(role.getKey(), assignedRole.getKey());
        }
    }

    @Test(expected = OperationNotPermittedException.class)
    public void testSubscribeToProductWithServiceRolesNoRolesSet()
            throws Exception {
        container.login(String.valueOf(supplierUser.getKey()),
                new String[] { ROLE_ORGANIZATION_ADMIN, ROLE_TECHNOLOGY_MANAGER,
                        ROLE_SERVICE_MANAGER });
        VOServiceDetails prod = createProductWithRoles("example");
        VOSubscription sub = Subscriptions.createVOSubscription("sub");
        subMgmt.subscribeToService(sub, prod, createUsers(null, 2), null, null,
                new ArrayList<VOUda>());
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testSubscribeToProductWithServiceRolesNotExistingRolesSet()
            throws Exception {
        container.login(String.valueOf(supplierUser.getKey()),
                new String[] { ROLE_ORGANIZATION_ADMIN, ROLE_TECHNOLOGY_MANAGER,
                        ROLE_SERVICE_MANAGER });
        VOServiceDetails prod = createProductWithRoles("example");
        VOSubscription sub = Subscriptions.createVOSubscription("sub");
        subMgmt.subscribeToService(sub, prod,
                createUsers(new VORoleDefinition(), 2), null, null,
                new ArrayList<VOUda>());
    }

    @Test(expected = javax.ejb.EJBException.class)
    public void testSubscribeToProductWithServiceRolesRoleOfDifferentTPSet()
            throws Exception {
        container.login(String.valueOf(supplierUser.getKey()),
                new String[] { ROLE_ORGANIZATION_ADMIN });
        VORoleDefinition role = createOtherRole();
        VOServiceDetails prod = createProductWithRoles("example");
        VOSubscription sub = Subscriptions.createVOSubscription("sub");
        subMgmt.subscribeToService(sub, prod, createUsers(role, 2), null, null,
                new ArrayList<VOUda>());
    }

    @Test(expected = OperationNotPermittedException.class)
    public void testSubscribeToProductForOtherUserNotPermitted()
            throws Exception {
        VOService prod = getProductToSubscribe(testProducts.get(0).getKey());
        VOSubscription sub = Subscriptions.createVOSubscription("sub");

        VOUser[] admins = new VOUser[1];
        admins[0] = UserDataAssembler
                .toVOUser(testUsers.get(testOrganizations.get(1)).get(1)); // wrong
                                                                           // organization!

        subMgmt.subscribeToService(sub, prod, getUsersToAdd(admins, null), null,
                null, new ArrayList<VOUda>());
    }

    private List<VOUsageLicense> createUsers(VORoleDefinition role,
            int numOfUsers) {
        List<VOUsageLicense> result = new ArrayList<>();
        for (int i = 0; i < numOfUsers; i++) {
            VOUsageLicense lic = new VOUsageLicense();
            lic.setRoleDefinition(role);
            lic.setUser(UserDataAssembler
                    .toVOUser(testUsers.get(testOrganizations.get(0)).get(i)));
            result.add(lic);
        }
        return result;
    }

    private VORoleDefinition createOtherRole() throws Exception {
        servProv.importTechnicalServices(TECHNICAL_SERVICES_XML
                .replaceAll("example", "example_other").getBytes("UTF-8"));

        List<VOTechnicalService> technicalProducts = servProv
                .getTechnicalServices(OrganizationRoleType.TECHNOLOGY_PROVIDER);
        VOTechnicalService tp = null;
        for (VOTechnicalService voTechnicalProduct : technicalProducts) {
            if ("example_other"
                    .equals(voTechnicalProduct.getTechnicalServiceId())) {
                tp = voTechnicalProduct;
            }
        }
        if (tp == null) {
            Assert.fail("TP example_other not found");
            return null;
        }
        return tp.getRoleDefinitions().get(0);
    }

    @Test
    public void testAddRevokeUsersWithServiceRoles() throws Exception {
        container.login(String.valueOf(supplierUser.getKey()),
                new String[] { ROLE_ORGANIZATION_ADMIN, ROLE_TECHNOLOGY_MANAGER,
                        ROLE_SERVICE_MANAGER });
        VOServiceDetails prod = createProductWithRoles("example");
        List<VORoleDefinition> roles = prod.getTechnicalService()
                .getRoleDefinitions();
        assertEquals(3, roles.size());
        VOSubscription sub = Subscriptions.createVOSubscription("sub");
        sub = subMgmt.subscribeToService(sub, prod,
                createUsers(roles.get(0), 1), null, null,
                new ArrayList<VOUda>());
        List<VOUsageLicense> users = new ArrayList<>();
        users.add(createUsers(roles.get(1), 2).get(1));
        users.add(createUsers(roles.get(2), 3).get(2));
        subMgmt.addRevokeUser(sub.getSubscriptionId(), users, null);

        VOSubscriptionDetails subDetails = subMgmt
                .getSubscriptionDetails(sub.getSubscriptionId());
        List<VOUsageLicense> licenses = subDetails.getUsageLicenses();
        assertEquals(3, licenses.size());
        for (int i = 0; i < 3; i++) {
            VOUsageLicense lic = licenses.get(i);
            VORoleDefinition assignedRole = lic.getRoleDefinition();
            assertNotNull(assignedRole);
            assertEquals(roles.get(i).getKey(), assignedRole.getKey());
        }
    }

    @Test(expected = OperationNotPermittedException.class)
    public void testAddRevokeUsersWithServiceRolesNoRolesSet()
            throws Exception {
        container.login(String.valueOf(supplierUser.getKey()),
                new String[] { ROLE_ORGANIZATION_ADMIN, ROLE_TECHNOLOGY_MANAGER,
                        ROLE_SERVICE_MANAGER });
        VOServiceDetails prod = createProductWithRoles("example");
        List<VORoleDefinition> roles = prod.getTechnicalService()
                .getRoleDefinitions();
        assertEquals(3, roles.size());
        VOSubscription sub = Subscriptions.createVOSubscription("sub");
        sub = subMgmt.subscribeToService(sub, prod,
                createUsers(roles.get(0), 1), null, null,
                new ArrayList<VOUda>());
        subMgmt.addRevokeUser(sub.getSubscriptionId(), createUsers(null, 3),
                null);
    }

    @Test(expected = OperationNotPermittedException.class)
    public void testAddRevokeUsersWithServiceRolesRoleOfDifferentTPSet()
            throws Exception {
        container.login(String.valueOf(supplierUser.getKey()),
                new String[] { ROLE_ORGANIZATION_ADMIN, ROLE_TECHNOLOGY_MANAGER,
                        ROLE_SERVICE_MANAGER });
        VORoleDefinition role = createOtherRole();
        VOServiceDetails prod = createProductWithRoles("example");
        List<VORoleDefinition> roles = prod.getTechnicalService()
                .getRoleDefinitions();
        assertEquals(3, roles.size());
        VOSubscription sub = Subscriptions.createVOSubscription("sub");
        sub = subMgmt.subscribeToService(sub, prod,
                createUsers(roles.get(0), 1), null, null,
                new ArrayList<VOUda>());
        subMgmt.addRevokeUser(sub.getSubscriptionId(), createUsers(role, 3),
                null);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testAddRevokeUsersWithServiceRolesNotExistingRoleSet()
            throws Exception {
        container.login(String.valueOf(supplierUser.getKey()),
                new String[] { ROLE_ORGANIZATION_ADMIN, ROLE_TECHNOLOGY_MANAGER,
                        ROLE_SERVICE_MANAGER });
        VOServiceDetails prod = createProductWithRoles("example");
        List<VORoleDefinition> roles = prod.getTechnicalService()
                .getRoleDefinitions();
        assertEquals(3, roles.size());
        VOSubscription sub = Subscriptions.createVOSubscription("sub");
        sub = subMgmt.subscribeToService(sub, prod,
                createUsers(roles.get(0), 1), null, null,
                new ArrayList<VOUda>());
        subMgmt.addRevokeUser(sub.getSubscriptionId(),
                createUsers(new VORoleDefinition(), 3), null);
    }

    @Test
    public void testAddRevokeUsersWithoutServiceRolesSetServiceRole()
            throws Exception {
        container.login(String.valueOf(supplierUser.getKey()),
                new String[] { ROLE_ORGANIZATION_ADMIN, ROLE_TECHNOLOGY_MANAGER,
                        ROLE_SERVICE_MANAGER });
        VORoleDefinition role = createOtherRole();
        VOServiceDetails prod = createProductWithRoles("ssh");

        assertTrue(prod.getTechnicalService().getRoleDefinitions().isEmpty());
        VOSubscription sub = Subscriptions.createVOSubscription("sub");
        sub = subMgmt.subscribeToService(sub, prod, createUsers(null, 1), null,
                null, new ArrayList<VOUda>());

        VOSubscriptionDetails details = subMgmt
                .getSubscriptionDetails(sub.getSubscriptionId());

        final VOUsageLicense user = details.getUsageLicenses().get(0);
        user.setRoleDefinition(role);
        List<VOUsageLicense> usersToBeAdded = new ArrayList<>();
        usersToBeAdded.add(user);

        subMgmt.addRevokeUser(sub.getSubscriptionId(), usersToBeAdded, null);

        details = subMgmt.getSubscriptionDetails(sub.getSubscriptionId());
        List<VOUsageLicense> licenses = details.getUsageLicenses();
        assertEquals(1, licenses.size());
        VOUsageLicense lic = licenses.get(0);
        assertNull(lic.getRoleDefinition());
    }

    @Test
    public void testGetServiceRolesForSubscription() throws Exception {
        container.login(String.valueOf(supplierUser.getKey()),
                new String[] { ROLE_ORGANIZATION_ADMIN, ROLE_TECHNOLOGY_MANAGER,
                        ROLE_SERVICE_MANAGER });
        VOServiceDetails prod = createProductWithRoles("example");
        List<VORoleDefinition> roles = prod.getTechnicalService()
                .getRoleDefinitions();
        VOSubscription sub = Subscriptions.createVOSubscription("sub");
        sub = subMgmt.subscribeToService(sub, prod,
                createUsers(roles.get(0), 1), null, null,
                new ArrayList<VOUda>());
        List<VORoleDefinition> rolesForSub = subMgmt
                .getServiceRolesForSubscription(sub.getSubscriptionId());
        compareRoles(roles, rolesForSub);
    }

    /**
     * Subscribe to a service which not free; pass no payment info when
     * subscribing. this should trigger a exception.
     */
    @Test(expected = PaymentInformationException.class)
    public void testSubscribeToProductNonFreePaymentInfoNull()
            throws Throwable {

        // make sure the service has a chargeable price model
        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                Product product = initCustomerProduct(0);

                PriceModel priceModel = new PriceModel();
                priceModel.setType(PriceModelType.PRO_RATA);
                Query query = mgr.createNamedQuery(
                        "SupportedCurrency.findByBusinessKey");
                query.setParameter("currencyISOCode", "EUR");
                SupportedCurrency currency = (SupportedCurrency) query
                        .getSingleResult();
                priceModel.setCurrency(currency);
                product.setPriceModel(priceModel);
                return null;
            }
        });

        VOService product = getProductToSubscribe(
                testProducts.get(10).getKey());
        VOUser[] users = new VOUser[2];
        VOUser[] admins = new VOUser[1];
        setUsers(users, admins);
        VOSubscription subscription = Subscriptions
                .createVOSubscription("testSubscribeToProduct");

        assertTrue(product.getPriceModel().isChargeable());

        subMgmt.subscribeToService(subscription, product,
                getUsersToAdd(admins, users), null, null,
                new ArrayList<VOUda>());
    }

    /**
     * Subscribe to a service which; pass a paymentinfo with the supplier key
     * which is not the key of the service the user wants to subscribe to.
     */
    @Test(expected = PaymentInformationException.class)
    public void testSubscribeToProductForeignSupplierId() throws Throwable {

        // Create a
        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                Product product = initCustomerProduct(0);
                PriceModel priceModel = new PriceModel();
                priceModel.setType(PriceModelType.PRO_RATA);
                Query query = mgr.createNamedQuery(
                        "SupportedCurrency.findByBusinessKey");
                query.setParameter("currencyISOCode", "EUR");
                SupportedCurrency currency = (SupportedCurrency) query
                        .getSingleResult();
                priceModel.setCurrency(currency);
                product.setPriceModel(priceModel);
                return null;
            }
        });

        VOService product = getProductToSubscribe(
                testProducts.get(10).getKey());
        VOUser[] users = new VOUser[2];
        VOUser[] admins = new VOUser[1];
        setUsers(users, admins);
        VOSubscription subscription = Subscriptions
                .createVOSubscription("testSubscribeToProduct");

        assertTrue(product.getPriceModel().isChargeable());

        String voSupplierId = "0012300";
        // Create a VO with a different supplier key than the subscription.
        final VOPaymentInfo voPaymentInfo = getPaymentInfo(voSupplierId,
                testOrganizations.get(0));
        VOBillingContact bc = createBillingContact(testOrganizations.get(0));
        subMgmt.subscribeToService(subscription, product,
                getUsersToAdd(admins, users), voPaymentInfo, bc,
                new ArrayList<VOUda>());
    }

    /**
     * Subscribe to a service which; pass a "Foreign" paymentinfo (a paymentinfo
     * which was created for another supplier)
     */
    @Test(expected = PaymentInformationException.class)
    public void testSubscribeToProduct_NotSupported() throws Throwable {

        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                Product product = initCustomerProduct(0);
                PriceModel priceModel = new PriceModel();
                priceModel.setType(PriceModelType.PRO_RATA);
                Query query = mgr.createNamedQuery(
                        "SupportedCurrency.findByBusinessKey");
                query.setParameter("currencyISOCode", "EUR");
                SupportedCurrency currency = (SupportedCurrency) query
                        .getSingleResult();
                priceModel.setCurrency(currency);
                product.setPriceModel(priceModel);
                return null;
            }
        });
        final Organization tpAndSupplier2 = runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                return Organizations.createOrganization(mgr,
                        OrganizationRoleType.SUPPLIER,
                        OrganizationRoleType.TECHNOLOGY_PROVIDER);
            }
        });
        VOService product = getProductToSubscribe(
                testProducts.get(10).getKey());
        VOUser[] users = new VOUser[2];
        VOUser[] admins = new VOUser[1];
        setUsers(users, admins);
        VOSubscription subscription = Subscriptions
                .createVOSubscription("testSubscribeToProduct");

        assertTrue(product.getPriceModel().isChargeable());

        // Create a VO with a different supplier key than the subscription.
        final VOPaymentInfo voPaymentInfo = getPaymentInfo(
                tpAndSupplier2.getOrganizationId(), testOrganizations.get(0));
        VOBillingContact bc = createBillingContact(testOrganizations.get(0));
        subMgmt.subscribeToService(subscription, product,
                getUsersToAdd(admins, users), voPaymentInfo, bc,
                new ArrayList<VOUda>());
    }

    /**
     * Subscribe to a service - pass a "Foreign" paymentinfo (a paymentinfo
     * which was created by another organization).
     */
    @Test(expected = OperationNotPermittedException.class)
    public void testSubscribeToProductForeignPaymentInfo() throws Throwable {

        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                Product product = initCustomerProduct(0);
                PriceModel priceModel = new PriceModel();
                priceModel.setType(PriceModelType.PRO_RATA);
                Query query = mgr.createNamedQuery(
                        "SupportedCurrency.findByBusinessKey");
                query.setParameter("currencyISOCode", "EUR");
                SupportedCurrency currency = (SupportedCurrency) query
                        .getSingleResult();
                priceModel.setCurrency(currency);
                product.setPriceModel(priceModel);
                return null;
            }
        });
        final Organization tpAndSupplier2 = runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization newOrg = Organizations.createOrganization(mgr,
                        OrganizationRoleType.SUPPLIER,
                        OrganizationRoleType.TECHNOLOGY_PROVIDER);
                return newOrg;
            }
        });
        createAvailablePayment(tpAndSupplier2);

        VOService product = getProductToSubscribe(
                testProducts.get(10).getKey());
        VOUser[] users = new VOUser[2];
        VOUser[] admins = new VOUser[1];
        setUsers(users, admins);
        VOSubscription subscription = Subscriptions
                .createVOSubscription("testSubscribeToProduct");

        assertTrue(product.getPriceModel().isChargeable());

        // Create a VO with a different supplier key than the subscription.
        final VOPaymentInfo voPaymentInfo = getPaymentInfo(
                tpAndSupplier2.getOrganizationId(), tpAndSupplier2);
        VOBillingContact bc = createBillingContact(tpAndSupplier2);
        // use the supplied id of the service supplier
        subMgmt.subscribeToService(subscription, product,
                getUsersToAdd(admins, users), voPaymentInfo, bc,
                new ArrayList<VOUda>());
    }

    /**
     * Check the successful subscription and check if the payment inform was
     * stored correct
     */
    @Test
    public void testSubscribeToProductNonFreeWithPaymentInfo()
            throws Throwable {

        // make sure the service has a chargeable price model
        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                Product product = initCustomerProduct(0);
                PriceModel priceModel = new PriceModel();
                priceModel.setType(PriceModelType.PRO_RATA);
                Query query = mgr.createNamedQuery(
                        "SupportedCurrency.findByBusinessKey");
                query.setParameter("currencyISOCode", "EUR");
                SupportedCurrency currency = (SupportedCurrency) query
                        .getSingleResult();
                priceModel.setCurrency(currency);
                product.setPriceModel(priceModel);
                return null;
            }
        });

        createAvailablePayment(testOrganizations.get(0));

        VOService product = getProductToSubscribe(
                testProducts.get(10).getKey());
        VOUser[] users = new VOUser[2];
        VOUser[] admins = new VOUser[1];
        setUsers(users, admins);
        VOSubscription subscription = Subscriptions
                .createVOSubscription("testSubscribeToProduct");
        final VOPaymentInfo voPaymentInfo = getPaymentInfo(
                tpAndSupplier.getOrganizationId(), testOrganizations.get(0));
        VOBillingContact bc = createBillingContact(testOrganizations.get(0));

        assertTrue(product.getPriceModel().isChargeable());

        final VOSubscription voSubscription = subMgmt.subscribeToService(
                subscription, product, getUsersToAdd(admins, users),
                voPaymentInfo, bc, new ArrayList<VOUda>());

        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                Subscription subscription = mgr.getReference(Subscription.class,
                        voSubscription.getKey());
                PaymentInfo paymentInfo = subscription.getPaymentInfo();
                assertEquals(voPaymentInfo.getKey(), paymentInfo.getKey());
                PaymentInfo pi = mgr.getReference(PaymentInfo.class,
                        voPaymentInfo.getKey());
                assertSame(pi, paymentInfo);
                return null;
            }
        });

    }

    /**
     * Subscribe to a service which is free; pass no payment info when
     * subscribing, which should work.
     */
    @Test
    public void testSubscribeToProductFreePaymentInfoNull() throws Throwable {
        // make sure the service has a non chargeable price model
        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                Product product = mgr.find(Product.class,
                        testProducts.get(10).getKey());
                product.setTargetCustomer(testOrganizations.get(0));
                product.setTemplate(
                        mgr.find(Product.class, testProducts.get(10).getKey()));
                PriceModel priceModel = new PriceModel();
                priceModel.setType(PriceModelType.FREE_OF_CHARGE);
                load(product.getPriceModel());
                return null;
            }
        });

        VOService product = getProductToSubscribe(
                testProducts.get(10).getKey());
        VOUser[] users = new VOUser[2];
        VOUser[] admins = new VOUser[1];
        setUsers(users, admins);
        VOSubscription subscription = Subscriptions
                .createVOSubscription("testSubscribeToProduct");

        assertFalse(product.getPriceModel().isChargeable());

        final VOSubscription voSubscription = subMgmt.subscribeToService(
                subscription, product, getUsersToAdd(admins, users), null, null,
                new ArrayList<VOUda>());

        assertNotNull(voSubscription);

        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                checkSubscribeToProduct(false, "testSubscribeToProduct",
                        testProducts.get(10), SubscriptionStatus.ACTIVE, 3,
                        null, 1);
                return null;
            }
        });
    }

    /**
     * Subscribe to a service which is not free and requires a payment info obj.
     * The reference to the billing contact is null -> a exception is ecepted.
     */
    @Test(expected = PaymentInformationException.class)
    public void testSubscribeToProductPaymentInfoNullBillingContact()
            throws Throwable {
        // make sure the service has a non chargeable price model
        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                Product product = initCustomerProduct(0);
                PriceModel priceModel = new PriceModel();
                priceModel.setType(PriceModelType.PRO_RATA);
                Query query = mgr.createNamedQuery(
                        "SupportedCurrency.findByBusinessKey");
                query.setParameter("currencyISOCode", "EUR");
                SupportedCurrency currency = (SupportedCurrency) query
                        .getSingleResult();
                priceModel.setCurrency(currency);
                product.setPriceModel(priceModel);
                return null;
            }
        });

        VOService product = getProductToSubscribe(
                testProducts.get(10).getKey());
        VOUser[] users = new VOUser[2];
        VOUser[] admins = new VOUser[1];
        setUsers(users, admins);
        VOSubscription subscription = Subscriptions
                .createVOSubscription("testSubscribeToProduct");

        assertTrue(product.getPriceModel().isChargeable());

        VOPaymentInfo paymentInfo = getPaymentInfo(
                tpAndSupplier.getOrganizationId(), testOrganizations.get(0));
        subMgmt.subscribeToService(subscription, product,
                getUsersToAdd(admins, users), paymentInfo, null,
                new ArrayList<VOUda>());

        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                checkSubscribeToProduct(false, "testSubscribeToProduct",
                        testProducts.get(10), SubscriptionStatus.ACTIVE, 3,
                        null, 1);
                return null;
            }
        });
    }

    /**
     * Upgrade from a free service to another free service
     */
    @Test
    public void testUpgradeToProductFreeToFreeOk() throws Throwable {
        createAvailablePayment(testOrganizations.get(0));
        VOService product = getProductToSubscribe(
                testProducts.get(10).getKey());
        VOUser[] users = new VOUser[2];
        VOUser[] admins = new VOUser[1];
        setUsers(users, admins);
        VOSubscription sub = Subscriptions.createVOSubscription("testUpgrade");
        assertFalse(product.getPriceModel().isChargeable());
        // Free service
        subMgmt.subscribeToService(sub, product, getUsersToAdd(admins, users),
                null, null, new ArrayList<VOUda>());

        List<VOService> products = servProv
                .getServicesForMarketplace(supplierOrgId);
        int numOfProducts = products.size();

        sub = subMgmt.getSubscriptionDetails(sub.getSubscriptionId());
        products = subMgmt.getUpgradeOptions(sub.getSubscriptionId());
        assertEquals(2, products.size());
        assertFalse(products.get(1).getPriceModel().isChargeable());

        final VOSubscription upgradedSubscription = subMgmt.upgradeSubscription(
                sub, products.get(1), null, null, new ArrayList<VOUda>());

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Subscription sub = mgr.getReference(Subscription.class,
                        upgradedSubscription.getKey());
                assertNull(sub.getPaymentInfo());
                return null;
            }
        });

        assertNotNull(upgradedSubscription);
        assertEquals(numOfProducts,
                servProv.getServicesForMarketplace(supplierOrgId).size());
        assertEquals(products.get(1).getServiceId(),
                upgradedSubscription.getServiceId());
        assertTrue(isTriggerQueueService_sendSuspendingMessageCalled);
        assertTrue(isMessageSend(TriggerType.UPGRADE_SUBSCRIPTION));
    }

    /**
     * Upgrade from a free service to a chargeable.
     */
    @Test
    public void testUpgradeToProductFreeToChargeableOk() throws Throwable {
        createAvailablePayment(testOrganizations.get(0));
        VOService product = getProductToSubscribe(
                testProducts.get(10).getKey());
        VOUser[] users = new VOUser[2];
        VOUser[] admins = new VOUser[1];
        setUsers(users, admins);
        VOSubscription sub = Subscriptions.createVOSubscription("testUpgrade");
        assertFalse(product.getPriceModel().isChargeable());
        // Free service
        subMgmt.subscribeToService(sub, product, getUsersToAdd(admins, users),
                null, null, new ArrayList<VOUda>());

        List<VOService> products = servProv
                .getServicesForMarketplace(supplierOrgId);
        int numOfProducts = products.size();

        sub = subMgmt.getSubscriptionDetails(sub.getSubscriptionId());
        products = subMgmt.getUpgradeOptions(sub.getSubscriptionId());
        assertEquals(2, products.size());
        assertTrue(products.get(0).getPriceModel().isChargeable());

        final VOPaymentInfo paymentInfo = getPaymentInfo(
                tpAndSupplier.getOrganizationId(), testOrganizations.get(0));
        VOBillingContact bc = createBillingContact(testOrganizations.get(0));

        final VOSubscription upgradedSubscription = subMgmt.upgradeSubscription(
                sub, products.get(0), paymentInfo, bc, new ArrayList<VOUda>());

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Subscription sub = mgr.getReference(Subscription.class,
                        upgradedSubscription.getKey());
                assertEquals(paymentInfo.getKey(),
                        sub.getPaymentInfo().getKey());
                return null;
            }
        });

        assertNotNull(upgradedSubscription);
        assertEquals(numOfProducts,
                servProv.getServicesForMarketplace(supplierOrgId).size());
        assertEquals(products.get(0).getServiceId(),
                upgradedSubscription.getServiceId());
        assertTrue(isTriggerQueueService_sendSuspendingMessageCalled);
        assertTrue(isMessageSend(TriggerType.UPGRADE_SUBSCRIPTION));
    }

    /**
     * Upgrade from a chargeable service to a free service.
     */
    @Test
    public void testUpgradeToProductChargeableToFreeOk() throws Throwable {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Product product = mgr.getReference(Product.class,
                        testProducts.get(10).getKey());
                SupportedCurrency sc = new SupportedCurrency();
                sc.setCurrency(Currency.getInstance("USD"));
                mgr.persist(sc);
                product.getPriceModel().setCurrency(sc);
                product.getPriceModel().setType(PriceModelType.PRO_RATA);

                createAvailablePayment(mgr.getReference(Organization.class,
                        testOrganizations.get(0).getKey()));
                return null;
            }
        });
        VOService product = getProductToSubscribe(
                testProducts.get(10).getKey());
        VOUser[] users = new VOUser[2];
        VOUser[] admins = new VOUser[1];
        setUsers(users, admins);
        VOSubscription sub = Subscriptions.createVOSubscription("testUpgrade");
        assertTrue(product.getPriceModel().isChargeable());
        VOPaymentInfo paymentInfo = getPaymentInfo(
                tpAndSupplier.getOrganizationId(), testOrganizations.get(0));
        VOBillingContact bc = createBillingContact(testOrganizations.get(0));
        assertTrue(product.getPriceModel().isChargeable());
        subMgmt.subscribeToService(sub, product, getUsersToAdd(admins, users),
                paymentInfo, bc, new ArrayList<VOUda>());

        List<VOService> products = servProv
                .getServicesForMarketplace(supplierOrgId);
        int numOfProducts = products.size();

        sub = subMgmt.getSubscriptionDetails(sub.getSubscriptionId());
        products = subMgmt.getUpgradeOptions(sub.getSubscriptionId());
        assertEquals(2, products.size());
        assertFalse(products.get(1).getPriceModel().isChargeable());

        final VOSubscription upgradedSubscription = subMgmt.upgradeSubscription(
                sub, products.get(1), null, null, new ArrayList<VOUda>());

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Subscription sub = mgr.getReference(Subscription.class,
                        upgradedSubscription.getKey());
                assertNull(sub.getPaymentInfo());
                assertNull(sub.getBillingContact());
                return null;
            }
        });

        assertNotNull(upgradedSubscription);
        assertEquals(numOfProducts,
                servProv.getServicesForMarketplace(supplierOrgId).size());
        assertEquals(products.get(1).getServiceId(),
                upgradedSubscription.getServiceId());
        assertTrue(isTriggerQueueService_sendSuspendingMessageCalled);
        assertTrue(isMessageSend(TriggerType.UPGRADE_SUBSCRIPTION));
    }

    /**
     * Upgrade from a charge able service to another chargeable service. The
     * payment info was set for the first service and should be reused.
     */
    @Test
    public void testUpgradeToProductChargeableToChargeableOk()
            throws Throwable {
        createAvailablePayment(testOrganizations.get(0));

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Product product = mgr.getReference(Product.class,
                        testProducts.get(10).getKey());
                SupportedCurrency sc = new SupportedCurrency();
                sc.setCurrency(Currency.getInstance("USD"));
                mgr.persist(sc);
                product.getPriceModel().setCurrency(sc);
                product.getPriceModel().setType(PriceModelType.PRO_RATA);
                // product.setPriceModel(null);
                return null;
            }
        });

        VOService product = getProductToSubscribe(
                testProducts.get(10).getKey());
        VOUser[] users = new VOUser[2];
        VOUser[] admins = new VOUser[1];
        setUsers(users, admins);
        VOSubscription sub = Subscriptions.createVOSubscription("testUpgrade");
        assertTrue(product.getPriceModel().isChargeable());
        final VOPaymentInfo paymentInfo = getPaymentInfo(
                tpAndSupplier.getOrganizationId(), testOrganizations.get(0));
        VOBillingContact bc = createBillingContact(testOrganizations.get(0));

        subMgmt.subscribeToService(sub, product, getUsersToAdd(admins, users),
                paymentInfo, bc, new ArrayList<VOUda>());

        List<VOService> products = servProv
                .getServicesForMarketplace(supplierOrgId);
        int numOfProducts = products.size();

        sub = subMgmt.getSubscriptionDetails(sub.getSubscriptionId());
        products = subMgmt.getUpgradeOptions(sub.getSubscriptionId());
        assertEquals(2, products.size());
        assertTrue(products.get(0).getPriceModel().isChargeable());

        final VOSubscription upgradedSubscription = subMgmt.upgradeSubscription(
                sub, products.get(0), paymentInfo, bc, new ArrayList<VOUda>());

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Subscription sub = mgr.getReference(Subscription.class,
                        upgradedSubscription.getKey());
                assertEquals(paymentInfo.getKey(),
                        sub.getPaymentInfo().getKey());
                return null;
            }
        });

        assertNotNull(upgradedSubscription);
        assertEquals(numOfProducts,
                servProv.getServicesForMarketplace(supplierOrgId).size());
        assertEquals(products.get(0).getServiceId(),
                upgradedSubscription.getServiceId());
        assertTrue(isTriggerQueueService_sendSuspendingMessageCalled);
        assertTrue(isMessageSend(TriggerType.UPGRADE_SUBSCRIPTION));
    }

    @Test
    public void testUpgradeToProductChargeableSuspendedToChargeableOk()
            throws Throwable {
        createAvailablePayment(testOrganizations.get(0));

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Product product = mgr.getReference(Product.class,
                        testProducts.get(10).getKey());
                SupportedCurrency sc = new SupportedCurrency();
                sc.setCurrency(Currency.getInstance("USD"));
                mgr.persist(sc);
                product.getPriceModel().setCurrency(sc);
                product.getPriceModel().setType(PriceModelType.PRO_RATA);
                // product.setPriceModel(null);
                return null;
            }
        });

        VOService product = getProductToSubscribe(
                testProducts.get(10).getKey());
        VOSubscription sub = Subscriptions.createVOSubscription("testUpgrade");
        assertTrue(product.getPriceModel().isChargeable());
        final VOPaymentInfo paymentInfo = getPaymentInfo(
                tpAndSupplier.getOrganizationId(), testOrganizations.get(0));
        final VOBillingContact bc = createBillingContact(
                testOrganizations.get(0));

        final VOSubscription created = subMgmt.subscribeToService(sub, product,
                new ArrayList<VOUsageLicense>(), paymentInfo, bc,
                new ArrayList<VOUda>());

        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                Subscription s = mgr.getReference(Subscription.class,
                        created.getKey());
                BillingContact contact = s.getBillingContact();
                s.setBillingContact(null);
                s.setStatus(SubscriptionStatus.SUSPENDED);
                appMgmtStub.activated = false;
                mgr.remove(contact);
                return null;
            }
        });

        sub = subMgmt.getSubscriptionDetails(sub.getSubscriptionId());
        assertEquals(SubscriptionStatus.SUSPENDED, sub.getStatus());

        final VOBillingContact bc2 = createBillingContact(
                testOrganizations.get(0));

        List<VOService> products = subMgmt
                .getUpgradeOptions(sub.getSubscriptionId());
        assertEquals(2, products.size());
        assertTrue(products.get(0).getPriceModel().isChargeable());

        final VOSubscription upgradedSubscription = subMgmt.upgradeSubscription(
                sub, products.get(0), paymentInfo, bc2, new ArrayList<VOUda>());

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Subscription sub = mgr.getReference(Subscription.class,
                        upgradedSubscription.getKey());
                assertEquals(paymentInfo.getKey(),
                        sub.getPaymentInfo().getKey());
                assertEquals(bc2.getKey(), sub.getBillingContact().getKey());
                return null;
            }
        });
        assertTrue(appMgmtStub.activated);
        assertEquals(SubscriptionStatus.ACTIVE,
                upgradedSubscription.getStatus());
        assertNotNull(upgradedSubscription);
        assertEquals(products.get(0).getServiceId(),
                upgradedSubscription.getServiceId());
        assertTrue(isTriggerQueueService_sendSuspendingMessageCalled);
        assertTrue(isMessageSend(TriggerType.UPGRADE_SUBSCRIPTION));
    }

    /**
     * Upgrade from a free service to a chargeable but do not pass paymentinfo.
     */
    @Test(expected = PaymentInformationException.class)
    public void testUpgradeToProductFreeToChargeableNok() throws Throwable {
        createAvailablePayment(testOrganizations.get(0));
        VOService product = getProductToSubscribe(
                testProducts.get(10).getKey());
        VOUser[] users = new VOUser[2];
        VOUser[] admins = new VOUser[1];
        setUsers(users, admins);
        VOSubscription sub = Subscriptions.createVOSubscription("testUpgrade");

        // Free service
        subMgmt.subscribeToService(sub, product, getUsersToAdd(admins, users),
                null, null, new ArrayList<VOUda>());

        List<VOService> products = servProv
                .getServicesForMarketplace(supplierOrgId);

        sub = subMgmt.getSubscriptionDetails(sub.getSubscriptionId());
        products = subMgmt.getUpgradeOptions(sub.getSubscriptionId());
        assertEquals(2, products.size());
        assertTrue(products.get(0).getPriceModel().isChargeable());

        subMgmt.upgradeSubscription(sub, products.get(0), null, null,
                new ArrayList<VOUda>());
    }

    /**
     * Upgrade from a free service to a chargeable without valid payment types.
     */
    @Test(expected = PaymentInformationException.class)
    public void testUpgradeToProductFreeToChargeableNoProductPayment()
            throws Throwable {
        createAvailablePayment(testOrganizations.get(0));
        final VOService product = getProductToSubscribe(
                testProducts.get(10).getKey());
        VOUser[] users = new VOUser[2];
        VOUser[] admins = new VOUser[1];
        setUsers(users, admins);
        VOSubscription sub = Subscriptions.createVOSubscription("testUpgrade");

        // Free service
        subMgmt.subscribeToService(sub, product, getUsersToAdd(admins, users),
                null, null, new ArrayList<VOUda>());

        sub = subMgmt.getSubscriptionDetails(sub.getSubscriptionId());
        final List<VOService> products = subMgmt
                .getUpgradeOptions(sub.getSubscriptionId());
        assertEquals(2, products.size());
        assertTrue(products.get(0).getPriceModel().isChargeable());

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                VOService product = products.get(0);
                Product p = mgr.getReference(Product.class, product.getKey());
                for (ProductToPaymentType ptpt : p.getPaymentTypes()) {
                    mgr.remove(ptpt);
                }
                return null;
            }
        });

        final VOPaymentInfo paymentInfo = getPaymentInfo(
                tpAndSupplier.getOrganizationId(), testOrganizations.get(0));
        final VOBillingContact bc = createBillingContact(
                testOrganizations.get(0));

        subMgmt.upgradeSubscription(sub, products.get(0), paymentInfo, bc,
                new ArrayList<VOUda>());
    }

    /**
     * Upgrade form a non free service to another non free service. But pass no
     * payment info for the upgrade.
     */
    @Test(expected = PaymentInformationException.class)
    public void testUpgradeToProductChargeableToChargeableNok()
            throws Throwable {
        createAvailablePayment(testOrganizations.get(0));

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Product product = mgr.getReference(Product.class,
                        testProducts.get(10).getKey());
                SupportedCurrency sc = new SupportedCurrency();
                sc.setCurrency(Currency.getInstance("USD"));
                mgr.persist(sc);
                product.getPriceModel().setCurrency(sc);
                product.getPriceModel().setType(PriceModelType.PRO_RATA);
                // product.setPriceModel(null);
                return null;
            }
        });

        VOService product = getProductToSubscribe(
                testProducts.get(10).getKey());
        VOUser[] users = new VOUser[2];
        VOUser[] admins = new VOUser[1];
        setUsers(users, admins);
        VOSubscription sub = Subscriptions.createVOSubscription("testUpgrade");
        assertTrue(product.getPriceModel().isChargeable());
        final VOPaymentInfo paymentInfo = getPaymentInfo(
                tpAndSupplier.getOrganizationId(), testOrganizations.get(0));
        VOBillingContact bc = createBillingContact(testOrganizations.get(0));

        subMgmt.subscribeToService(sub, product, getUsersToAdd(admins, users),
                paymentInfo, bc, new ArrayList<VOUda>());

        List<VOService> products = servProv
                .getServicesForMarketplace(supplierOrgId);

        sub = subMgmt.getSubscriptionDetails(sub.getSubscriptionId());
        products = subMgmt.getUpgradeOptions(sub.getSubscriptionId());
        assertEquals(2, products.size());
        assertTrue(products.get(0).getPriceModel().isChargeable());

        subMgmt.upgradeSubscription(sub, products.get(0), null, null,
                new ArrayList<VOUda>());
    }

    /**
     * Try to upgrade; pass the payment information of an different supplier.
     */
    @Test(expected = PaymentInformationException.class)
    public void testUpgradeProductForeignPaymentInfo() throws Throwable {
        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                Product product = initCustomerProduct(0);
                PriceModel priceModel = new PriceModel();
                priceModel.setType(PriceModelType.PRO_RATA);
                Query query = mgr.createNamedQuery(
                        "SupportedCurrency.findByBusinessKey");
                query.setParameter("currencyISOCode", "EUR");
                SupportedCurrency currency = (SupportedCurrency) query
                        .getSingleResult();
                priceModel.setCurrency(currency);
                product.setPriceModel(priceModel);
                return null;
            }
        });
        final Organization tpAndSupplier2 = runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                return Organizations.createOrganization(mgr,
                        OrganizationRoleType.SUPPLIER,
                        OrganizationRoleType.TECHNOLOGY_PROVIDER);
            }
        });
        createAvailablePayment(tpAndSupplier2);

        VOService product = getProductToSubscribe(
                testProducts.get(10).getKey());
        VOUser[] users = new VOUser[2];
        VOUser[] admins = new VOUser[1];
        setUsers(users, admins);
        VOSubscription subscription = Subscriptions
                .createVOSubscription("testSubscribeToProduct");

        assertTrue(product.getPriceModel().isChargeable());
        // Free service
        subMgmt.subscribeToService(subscription, product,
                getUsersToAdd(admins, users), null, null,
                new ArrayList<VOUda>());

        List<VOService> products = servProv
                .getServicesForMarketplace(supplierOrgId);

        subscription = subMgmt
                .getSubscriptionDetails(subscription.getSubscriptionId());
        products = subMgmt.getUpgradeOptions(subscription.getSubscriptionId());
        assertEquals(2, products.size());
        assertTrue(products.get(0).getPriceModel().isChargeable());

        final VOPaymentInfo voPaymentInfo = getPaymentInfo(
                tpAndSupplier2.getOrganizationId(), testOrganizations.get(0));
        VOBillingContact bc = createBillingContact(testOrganizations.get(0));
        subMgmt.upgradeSubscription(subscription, products.get(0),
                voPaymentInfo, bc, new ArrayList<VOUda>());
    }

    /**
     * Pass payment information from a different supplier and fake the supplier
     * id (use the id of the supplier which is the provider of the service).
     */
    @Test(expected = PaymentInformationException.class)
    public void testUpgradeProductForeignPaymentInfoAndFakeSupplierId()
            throws Throwable {

        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                Product product = initCustomerProduct(0);
                PriceModel priceModel = new PriceModel();
                priceModel.setType(PriceModelType.PRO_RATA);
                Query query = mgr.createNamedQuery(
                        "SupportedCurrency.findByBusinessKey");
                query.setParameter("currencyISOCode", "EUR");
                SupportedCurrency currency = (SupportedCurrency) query
                        .getSingleResult();
                priceModel.setCurrency(currency);
                product.setPriceModel(priceModel);
                return null;
            }
        });
        final Organization tpAndSupplier2 = runTX(new Callable<Organization>() {
            @Override
            public Organization call() throws Exception {
                Organization newOrg = Organizations.createOrganization(mgr,
                        OrganizationRoleType.SUPPLIER,
                        OrganizationRoleType.TECHNOLOGY_PROVIDER);
                return newOrg;
            }
        });
        createAvailablePayment(tpAndSupplier2);

        VOService product = getProductToSubscribe(
                testProducts.get(10).getKey());
        VOUser[] users = new VOUser[2];
        VOUser[] admins = new VOUser[1];
        setUsers(users, admins);
        VOSubscription subscription = Subscriptions
                .createVOSubscription("testSubscribeToProduct");

        assertTrue(product.getPriceModel().isChargeable());
        subMgmt.subscribeToService(subscription, product,
                getUsersToAdd(admins, users), null, null,
                new ArrayList<VOUda>());

        List<VOService> products = servProv
                .getServicesForMarketplace(supplierOrgId);

        subscription = subMgmt
                .getSubscriptionDetails(subscription.getSubscriptionId());
        products = subMgmt.getUpgradeOptions(subscription.getSubscriptionId());
        assertEquals(2, products.size());
        assertTrue(products.get(0).getPriceModel().isChargeable());

        VOPaymentInfo paymentInfo = getPaymentInfo(
                tpAndSupplier.getOrganizationId(), tpAndSupplier);
        VOBillingContact bc = createBillingContact(testOrganizations.get(0));
        subMgmt.upgradeSubscription(subscription, products.get(0), paymentInfo,
                bc, new ArrayList<VOUda>());
    }

    /**
     * Use a payment information which has no billing contact attached.
     */
    @Test(expected = PaymentInformationException.class)
    public void testUpgradeProductPaymentInfoNullBillingContact()
            throws Throwable {
        createAvailablePayment(testOrganizations.get(0));
        VOService product = getProductToSubscribe(
                testProducts.get(10).getKey());
        VOUser[] users = new VOUser[2];
        VOUser[] admins = new VOUser[1];
        setUsers(users, admins);
        VOSubscription sub = Subscriptions.createVOSubscription("testUpgrade");
        assertFalse(product.getPriceModel().isChargeable());
        // Free service
        subMgmt.subscribeToService(sub, product, getUsersToAdd(admins, users),
                null, null, new ArrayList<VOUda>());

        List<VOService> products = servProv
                .getServicesForMarketplace(supplierOrgId);

        sub = subMgmt.getSubscriptionDetails(sub.getSubscriptionId());
        products = subMgmt.getUpgradeOptions(sub.getSubscriptionId());
        assertEquals(2, products.size());
        assertTrue(products.get(0).getPriceModel().isChargeable());

        final VOPaymentInfo paymentInfo = getPaymentInfo(
                tpAndSupplier.getOrganizationId(), testOrganizations.get(0));
        subMgmt.upgradeSubscription(sub, products.get(0), paymentInfo, null,
                new ArrayList<VOUda>());
    }

    private void compareRoles(List<VORoleDefinition> roles,
            List<VORoleDefinition> rolesForSub) {
        assertEquals(roles.size(), rolesForSub.size());
        assertEquals(roles.get(0).getKey(), rolesForSub.get(0).getKey());
        assertEquals(roles.get(0).getRoleId(), rolesForSub.get(0).getRoleId());
        assertEquals(roles.get(1).getKey(), rolesForSub.get(1).getKey());
        assertEquals(roles.get(1).getRoleId(), rolesForSub.get(1).getRoleId());
        assertEquals(roles.get(2).getKey(), rolesForSub.get(2).getKey());
        assertEquals(roles.get(2).getRoleId(), rolesForSub.get(2).getRoleId());
    }

    @Test
    public void testGetServiceRolesForProduct() throws Exception {
        container.login(String.valueOf(supplierUser.getKey()),
                new String[] { ROLE_ORGANIZATION_ADMIN, ROLE_TECHNOLOGY_MANAGER,
                        ROLE_SERVICE_MANAGER });
        VOServiceDetails prod = createProductWithRoles("example");
        List<VORoleDefinition> roles = prod.getTechnicalService()
                .getRoleDefinitions();
        List<VORoleDefinition> rolesForSub = subMgmt
                .getServiceRolesForService(prod);
        compareRoles(roles, rolesForSub);
    }

    private static VOInstanceInfo createInstanceInfo(String access,
            String baseUrl, String instanceId, String loginPath) {
        VOInstanceInfo info = new VOInstanceInfo();
        info.setAccessInfo(access);
        info.setBaseUrl(baseUrl);
        info.setInstanceId(instanceId);
        info.setLoginPath(loginPath);
        return info;
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

    private VOSubscription prepareSubForGetUpgradeOptions(final String subId,
            final ServiceStatus state) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                // the template used for the subscription
                Product prod = mgr.find(Product.class,
                        testProducts.get(0).getKey());
                prod.setStatus(ServiceStatus.ACTIVE);
                // the inactive template and customer specific product
                prod = mgr.find(Product.class, testProducts.get(1).getKey());
                prod.setStatus(state);
                prod = mgr.find(Product.class, testProducts.get(2).getKey());
                prod.setStatus(state);
                return null;
            }
        });
        VOService product = findProduct(testProducts.get(0).getKey());
        VOUser[] users = new VOUser[1];
        VOUser[] admins = new VOUser[1];
        admins[0] = UserDataAssembler
                .toVOUser(testUsers.get(testOrganizations.get(0)).get(1));
        users[0] = UserDataAssembler
                .toVOUser(testUsers.get(testOrganizations.get(0)).get(2));
        VOSubscription sub = subMgmt.subscribeToService(
                Subscriptions.createVOSubscription(subId), product,
                getUsersToAdd(admins, users), null, null,
                new ArrayList<VOUda>());
        return sub;
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testModifySubscriptionConcurrency_ParameterChanged()
            throws Exception {
        final VOSubscription sub = createNewSubscription();

        VOService voService = runTX(new Callable<VOService>() {
            @Override
            public VOService call() throws Exception {
                Subscription subscriptionToModify = mgr
                        .getReference(Subscription.class, sub.getKey());
                Product subProduct = subscriptionToModify.getProduct();

                VOService voProduct1 = ProductAssembler.toVOProduct(subProduct,
                        new LocalizerFacade(localizer, "en"));

                Parameter p = subProduct.getParameterSet().getParameters()
                        .get(subProduct.getParameterSet().getParameters().size()
                                - 1);
                p.setValue("5435897");
                return voProduct1;
            }
        });

        // Modify subscription. ConcurrentModificationException should
        // be thrown
        subMgmt.modifySubscription(sub, voService.getParameters(),
                new ArrayList<VOUda>());
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testModifySubscriptionConcurrency_ParameterDeleted()
            throws Exception {

        final VOSubscription sub = createNewSubscription();

        VOService voService = runTX(new Callable<VOService>() {
            @Override
            public VOService call() throws Exception {
                // DataService dm = container.get(DataService.class);

                Subscription subscriptionToModify = mgr
                        .getReference(Subscription.class, sub.getKey());
                Product subProduct = subscriptionToModify.getProduct();

                VOService voProduct1 = ProductAssembler.toVOProduct(subProduct,
                        new LocalizerFacade(localizer, "en"));

                removeReferences();

                Parameter p = subProduct.getParameterSet().getParameters()
                        .get(0);
                subProduct.getParameterSet().getParameters().remove(p);
                mgr.remove(p);

                return voProduct1;
            }
        });

        assertEquals(1, voService.getParameters().size());

        // Modify subscription. ConcurrentModificationException should
        // be thrown
        subMgmt.modifySubscription(sub, voService.getParameters(),
                new ArrayList<VOUda>());
    }

    @Test(expected = ServiceChangedException.class)
    public void testUpgradeSubscriptionConcurrency_ParameterChanged()
            throws Exception {
        final VOSubscription sub = createNewSubscription();

        List<VOService> targetProducts = servProv
                .getServicesForMarketplace(supplierOrgId);

        VOSubscriptionDetails subDetails = subMgmt
                .getSubscriptionDetails(sub.getSubscriptionId());
        targetProducts = subMgmt
                .getUpgradeOptions(subDetails.getSubscriptionId());
        final VOService voTargetProduct = targetProducts.get(1);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {

                Product dbTargetProduct = mgr.getReference(Product.class,
                        voTargetProduct.getKey());

                Parameter p = dbTargetProduct.getParameterSet().getParameters()
                        .get(dbTargetProduct.getParameterSet().getParameters()
                                .size() - 1);
                p.setValue("12321");
                return null;
            }
        });

        // Modify subscription. ConcurrentModificationException should
        // be thrown
        subMgmt.upgradeSubscription(sub, voTargetProduct, null, null,
                new ArrayList<VOUda>());
    }

    @Test(expected = ServiceChangedException.class)
    public void testUpgradeSubscriptionConcurrency_ParameterDeleted()
            throws Exception {
        final VOSubscription sub = createNewSubscription();

        List<VOService> targetProducts = servProv
                .getServicesForMarketplace(supplierOrgId);

        VOSubscriptionDetails subDetails = subMgmt
                .getSubscriptionDetails(sub.getSubscriptionId());
        targetProducts = subMgmt
                .getUpgradeOptions(subDetails.getSubscriptionId());

        final VOService voTargetProduct = targetProducts.get(1);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                removeReferences();
                Product dbTargetProduct = mgr.getReference(Product.class,
                        voTargetProduct.getKey());

                Parameter p = dbTargetProduct.getParameterSet().getParameters()
                        .get(0);
                dbTargetProduct.getParameterSet().getParameters().remove(p);
                mgr.remove(p);

                return null;
            }
        });

        // Modify subscription. ConcurrentModificationException should
        // be thrown
        subMgmt.upgradeSubscription(sub, voTargetProduct, null, null,
                new ArrayList<VOUda>());
    }

    @Test
    public void testUpgradeSubscriptionConcurrency_ParameterNotAvailable()
            throws Exception {
        final VOSubscription sub = createNewSubscription();

        List<VOService> targetProducts = servProv
                .getServicesForMarketplace(supplierOrgId);

        VOSubscriptionDetails subDetails = subMgmt
                .getSubscriptionDetails(sub.getSubscriptionId());
        targetProducts = subMgmt
                .getUpgradeOptions(subDetails.getSubscriptionId());

        // final VOService voTargetProduct = targetProducts.get(1);
        final long targetProductKey = targetProducts.get(1).getKey();

        VOService voTargetProduct = runTX(new Callable<VOService>() {
            @Override
            public VOService call() throws Exception {
                removeReferences();
                Product dbTargetProduct = mgr.getReference(Product.class,
                        targetProductKey);

                // delete all parameters
                dbTargetProduct.getParameterSet().getParameters().clear();
                dbTargetProduct.getPriceModel().setSelectedParameters(
                        new LinkedList<PricedParameter>());
                mgr.createQuery("DELETE FROM Parameter").executeUpdate();
                mgr.flush();

                return ProductAssembler.toVOProduct(dbTargetProduct,
                        new LocalizerFacade(localizer, "en"));
            }
        });

        // Modify subscription. ConcurrentModificationException should
        // be thrown
        subMgmt.upgradeSubscription(sub, voTargetProduct, null, null,
                new ArrayList<VOUda>());
    }

    @Test
    public void testSubscriptionUpgradeWithParamMigrationRule()
            throws Exception {

        upgradeSubscription("1", true, "1", true, false);

        upgradeSubscription("1", true, "2", true, false);

        upgradeSubscription(null, false, null, false, false);

        upgradeSubscription(null, false, "", false, false);

        upgradeSubscription("", false, null, false, false);

        upgradeSubscription("", false, "", false, false);

        upgradeSubscription("1", false, "1", false, false);

        upgradeSubscription("1", false, "2", false, true);

        upgradeSubscription("1", true, "1", false, false);

        upgradeSubscription("500", true, "600", false, false);

        upgradeSubscription("1", false, "1", true, false);

        upgradeSubscription("1", false, "2", true, true);
    }

    @Test
    public void testSubscribeToProduct_https() throws Throwable {
        assertNotNull(subMgmt);

        Product prod = runTX(new Callable<Product>() {
            @Override
            public Product call() throws Exception {
                TechnicalProduct tProd = TechnicalProducts
                        .createTechnicalProduct(mgr, tpAndSupplier,
                                "TP_PLATFORM_HTTPS", false,
                                ServiceAccessType.LOGIN);
                tProd.setBaseURL(BASE_URL_SERVICE_HTTPS);

                Product p1 = Products.createProduct(tpAndSupplier, tProd, false,
                        "mprodtc", null, mgr);
                p1.setStatus(ServiceStatus.ACTIVE);
                return p1;
            }
        });

        VOService product = getProductToSubscribe(prod.getKey());

        VOUser[] users = new VOUser[2];
        VOUser[] admins = new VOUser[1];
        setUsers(users, admins);
        VOSubscription sub = Subscriptions
                .createVOSubscription("testSubscribeToProductHttps");
        sub.setPurchaseOrderNumber("PON");
        messagesOfTaskQueue = new ArrayList<>();
        VOSubscription newSub = subMgmt.subscribeToService(sub, product,
                getUsersToAdd(admins, users), null, null,
                new ArrayList<VOUda>());

        // Now check results
        assertNotNull(newSub);
        assertEquals(2, receivedSendMailPayload.size());
        assertEquals(3, receivedSendMailPayload.get(0).getMailObjects().size());
        assertEquals(1, receivedSendMailPayload.get(1).getMailObjects().size());

        // Check if the URL in the mail contains the correct base URL
        assertTrue(((String) receivedSendMailPayload.get(0).getMailObjects()
                .get(0).getParams()[1]).startsWith(BASE_URL_BES_HTTPS));
    }

    @Test
    public void testSubscribeToProductWithExternalBilling() throws Throwable {
        assertNotNull(subMgmt);

        Product prod = runTX(new Callable<Product>() {
            @Override
            public Product call() throws Exception {
                TechnicalProduct tProd = TechnicalProducts
                        .createTechnicalProduct(mgr, tpAndSupplier,
                                "TP_PLATFORM_EXT_BILLING", false,
                                ServiceAccessType.LOGIN, false, true);
                tProd.setBaseURL(BASE_URL_SERVICE_HTTPS);

                Product p1 = Products.createProduct(tpAndSupplier, tProd, false,
                        "extBillProd", null, mgr);
                p1.setStatus(ServiceStatus.ACTIVE);
                return p1;
            }
        });

        VOService product = getProductToSubscribe(prod.getKey());

        VOUser[] users = new VOUser[2];
        VOUser[] admins = new VOUser[1];
        setUsers(users, admins);
        VOSubscription sub = Subscriptions
                .createVOSubscription("testSubscribeToProductWithExtlBilling");
        sub.setPurchaseOrderNumber("3434-54545");
        messagesOfTaskQueue = new ArrayList<>();
        final VOSubscription newSub = subMgmt.subscribeToService(sub, product,
                getUsersToAdd(admins, users), null, null,
                new ArrayList<VOUda>());

        Subscription createdSub = runTX(new Callable<Subscription>() {
            @Override
            public Subscription call() throws ObjectNotFoundException {
                return mgr.find(Subscription.class, newSub.getKey());
            }
        });

        assertEquals(true, createdSub.isExternal());
    }

    @Test
    public void checkIPAddressChangedAndSendMailToUsers_sendMail()
            throws Throwable {
        // given
        final SubscriptionServiceBean sBean = spy(
                new SubscriptionServiceBean());
        doReturn(new UsageLicenseDao(mgr)).when(sBean).getUsageLicenseDao();
        doReturn(new SubscriptionHistoryDao(mgr)).when(sBean)
                .getSubscriptionHistoryDao();
        sBean.tqs = tqs;
        ManageSubscriptionBean manageBean = spy(new ManageSubscriptionBean());
        sBean.manageBean = manageBean;

        final String id = "asyncSubscriptionId";
        subscribeAsync(id, 0, false);
        String orgId = testOrganizations.get(0).getOrganizationId();
        container.login(String.valueOf(supplierUser.getKey()),
                ROLE_TECHNOLOGY_MANAGER);
        final VOInstanceInfo instanceInfo = new VOInstanceInfo();
        instanceInfo.setInstanceId("completionProductInstanceId");
        instanceInfo.setAccessInfo(
                "Public DNS for EC2 instance: ec2-66-66-66-66.compute-1.amazonaws.com  Key pair name: us-east-1");
        receivedSendMailPayload = new ArrayList<>();
        subMgmt.completeAsyncSubscription(id, orgId, instanceInfo);
        instanceInfo.setAccessInfo(getAccessInfo());

        // when
        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                Subscription qryObj = new Subscription();
                qryObj.setOrganizationKey(testOrganizations.get(0).getKey());
                qryObj.setSubscriptionId(id);
                Subscription subscription = (Subscription) mgr.find(qryObj);
                isSendAllMessagesCalled = false;
                sBean.checkIPAddressChangedAndSendMailToUsers(subscription,
                        instanceInfo);
                return null;
            }
        });
        // then
        assertEquals(Boolean.TRUE, Boolean.valueOf(isSendAllMessagesCalled));
    }

    @Test
    public void checkIPAddressChangedAndSendMailToUsers_noSendMail()
            throws Throwable {
        // given
        final SubscriptionServiceBean sBean = spy(
                new SubscriptionServiceBean());
        doReturn(new UsageLicenseDao(mgr)).when(sBean).getUsageLicenseDao();
        doReturn(new SubscriptionHistoryDao(mgr)).when(sBean)
                .getSubscriptionHistoryDao();
        sBean.tqs = tqs;
        ManageSubscriptionBean manageBean = spy(new ManageSubscriptionBean());
        sBean.manageBean = manageBean;

        final String id = "subscriptionId";
        subscribeAsync(id, 0, false);
        container.login(String.valueOf(supplierUser.getKey()),
                ROLE_TECHNOLOGY_MANAGER);
        final VOInstanceInfo instanceInfo = new VOInstanceInfo();
        instanceInfo.setInstanceId("instanceId");
        instanceInfo.setAccessInfo(
                "Public DNS for EC2 instance: ec2-54-165-81-61.compute-1.amazonaws.com");

        // when
        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                Subscription qryObj = new Subscription();
                qryObj.setOrganizationKey(testOrganizations.get(0).getKey());
                qryObj.setSubscriptionId(id);
                Subscription subscription = (Subscription) mgr.find(qryObj);
                isSendAllMessagesCalled = false;
                sBean.checkIPAddressChangedAndSendMailToUsers(subscription,
                        instanceInfo);
                return null;
            }
        });

        // then
        verify(sBean, times(0)).checkIPAddressChanged(any(String.class),
                any(String.class));
        assertEquals(Boolean.FALSE, Boolean.valueOf(isSendAllMessagesCalled));
    }

    @Test
    public void getPublicDNS_NonEmpty() {
        // given
        String accessInfo = getAccessInfo();
        SubscriptionServiceBean sBean = spy(new SubscriptionServiceBean());
        // when
        String result = sBean.getPublicDNS(accessInfo);
        // then
        assertEquals(
                "Public DNS for EC2 instance: ec2-54-174-206-114.compute-1.amazonaws.com  ",
                result);
    }

    @Test
    public void getIPAddress_NonEmpty() {
        // given
        String accessInfo = getAccessInfo();
        SubscriptionServiceBean sBean = spy(new SubscriptionServiceBean());
        // when
        String result = sBean.getIPAddress(accessInfo);
        // then
        assertEquals("54.174.206.114", result);
    }

    @Test
    public void getPublicDNS_Empty() {
        // given
        String accessInfo = getUnableAccessInfo();
        SubscriptionServiceBean sBean = spy(new SubscriptionServiceBean());
        // when
        String result = sBean.getPublicDNS(accessInfo);
        // then
        assertEquals("", result);
    }

    @Test
    public void getIPAddress_Empty() {
        // given
        String accessInfo = getUnableAccessInfo();
        SubscriptionServiceBean sBean = spy(new SubscriptionServiceBean());
        // when
        String result = sBean.getIPAddress(accessInfo);
        // then
        assertEquals("", result);
    }

    @Test
    public void getKeyPairName_Empty() {
        // given
        String accessInfo = getUnableAccessInfo();
        SubscriptionServiceBean sBean = spy(new SubscriptionServiceBean());
        // when
        String result = sBean.getKeyPairName(accessInfo);
        // then
        assertEquals("", result);
    }

    @Test
    public void hasNullAccessInfo_False() {
        // given
        String accessInfo = getAccessInfo();
        SubscriptionServiceBean sBean = spy(new SubscriptionServiceBean());
        // when
        boolean result = sBean.hasNullAccessInfo(accessInfo);
        // then
        assertEquals(Boolean.FALSE, Boolean.valueOf(result));
    }

    @Test
    public void hasNullAccessInfo_True() {
        // given
        String accessInfo = getUnableAccessInfo();
        SubscriptionServiceBean sBean = spy(new SubscriptionServiceBean());
        // when
        boolean result = sBean.hasNullAccessInfo(accessInfo);
        // then
        assertEquals(Boolean.TRUE, Boolean.valueOf(result));
    }

    @Test
    public void isUsableAWSAccessInfo_False() {
        // given
        String accessInfo = getUnableAccessInfo();
        SubscriptionServiceBean sBean = spy(new SubscriptionServiceBean());
        // when
        boolean result = sBean.isUsableAWSAccessInfo(accessInfo);
        // then
        assertEquals(Boolean.FALSE, Boolean.valueOf(result));
    }

    @Test
    public void isUsableAWSAccessInfo_True() {
        SubscriptionServiceBean sBean = spy(new SubscriptionServiceBean());
        // given
        String accessInfo = getAccessInfo();
        // when
        boolean result = sBean.isUsableAWSAccessInfo(accessInfo);
        // then
        assertEquals(Boolean.TRUE, Boolean.valueOf(result));
    }

    @Test
    public void isUsableAWSAccessInfo_NullAccessInfo() {
        // given
        String accessInfo = "";
        SubscriptionServiceBean sBean = spy(new SubscriptionServiceBean());
        // when
        boolean result = sBean.isUsableAWSAccessInfo(accessInfo);
        // then
        assertEquals(Boolean.FALSE, Boolean.valueOf(result));
    }

    @Test
    public void isAWSAccessInfo_False() {
        // given
        String accessInfo = "Key pair name besdev3";
        SubscriptionServiceBean sBean = spy(new SubscriptionServiceBean());
        // when
        boolean result = sBean.isAWSAccessInfo(accessInfo);
        // then
        assertEquals(Boolean.FALSE, Boolean.valueOf(result));
    }

    @Test
    public void isAWSAccessInfo_True() {
        // given
        String accessInfo = getAccessInfo();
        SubscriptionServiceBean sBean = spy(new SubscriptionServiceBean());
        // when
        boolean result = sBean.isAWSAccessInfo(accessInfo);
        // then
        assertEquals(Boolean.TRUE, Boolean.valueOf(result));
    }

    @Test
    public void getKeyPairName_NonEmpty() {
        // given
        String accessInfo = getAccessInfo();
        SubscriptionServiceBean sBean = spy(new SubscriptionServiceBean());
        // when
        String result = sBean.getKeyPairName(accessInfo);
        // then
        assertEquals("Key pair name: us-east-1", result);
    }

    private String getAccessInfo() {
        String accessInfo = "Public DNS for EC2 instance: ec2-54-174-206-114.compute-1.amazonaws.com  Key pair name: us-east-1";
        return accessInfo;
    }

    private String getUnableAccessInfo() {
        String accessInfo = "amazonaws.com";
        return accessInfo;
    }

    private VOSubscription createNewSubscription() throws Exception {
        final Product product = testProducts.get(10);

        final VOSubscription sub = runTX(new Callable<VOSubscription>() {
            @Override
            public VOSubscription call() throws Exception {
                Product dbProduct = mgr.getReference(Product.class,
                        product.getKey());

                VOService voProduct = ProductAssembler.toVOProduct(dbProduct,
                        new LocalizerFacade(localizer, "en"));
                assertTrue(voProduct.getParameters().size() == 1);

                // subscribe service
                final VOSubscription newSub = Subscriptions
                        .createVOSubscription("" + UUID.randomUUID());
                VOSubscription sub = subMgmt.subscribeToService(newSub,
                        voProduct, null, null, null, new ArrayList<VOUda>());
                return sub;
            }
        });
        return sub;
    }

    private VOSubscription upgradeSubscription(final String targetDbValue,
            final boolean targetDbConfig, final String targetInputValue,
            final boolean targetInputConfig, boolean upgradeErrorExpected)
            throws Exception {

        final Product sourceProduct = testProducts.get(10);

        final VOService voSourceProduct = runTX(new Callable<VOService>() {
            @Override
            public VOService call() throws Exception {
                Product product = mgr.getReference(Product.class,
                        sourceProduct.getKey());
                return ProductAssembler.toVOProduct(product,
                        new LocalizerFacade(localizer, "en"));
            }
        });

        VOSubscription sub = createNewSubscription();

        // Upgrade to service 2
        List<VOService> targetProducts = servProv
                .getServicesForMarketplace(supplierOrgId);

        sub = subMgmt.getSubscriptionDetails(sub.getSubscriptionId());
        targetProducts = subMgmt.getUpgradeOptions(sub.getSubscriptionId());

        // check target parameters
        final VOService voTargetProduct = targetProducts.get(1);
        assertTrue(voSourceProduct.getKey() != voTargetProduct.getKey());
        assertEquals(1, voTargetProduct.getParameters().size());

        VOService voTargetProduct2 = runTX(new Callable<VOService>() {
            @Override
            public VOService call() throws Exception {
                DataService dm = container.get(DataService.class);

                Product targetProduct = mgr.getReference(Product.class,
                        voTargetProduct.getKey());

                for (Parameter targetParameter : targetProduct.getParameterSet()
                        .getParameters()) {
                    targetParameter.setConfigurable(targetDbConfig);
                    targetParameter.setValue(targetDbValue);
                }
                dm.flush();
                return ProductAssembler.toVOProduct(targetProduct,
                        new LocalizerFacade(localizer, "en"));
            }
        });

        // set target parameters with test values
        assertEquals(1, voTargetProduct2.getParameters().size());
        for (VOParameter voTargetParam : voTargetProduct2.getParameters()) {
            voTargetParam.setConfigurable(targetInputConfig);
            voTargetParam.setValue(targetInputValue);
        }

        // upgrade and check error state
        VOSubscription result = null;
        Exception error = null;
        try {
            result = subMgmt.upgradeSubscription(sub, voTargetProduct2, null,
                    null, new ArrayList<VOUda>());
        } catch (SubscriptionMigrationException e) {
            if (upgradeErrorExpected && e
                    .getReason() == SubscriptionMigrationException.Reason.INCOMPATIBLE_PARAMETER) {
                error = e;
            } else {
                throw e;
            }
        }

        if (upgradeErrorExpected) {
            assertNotNull(error);
        }
        return result;
    }

    /**
     * Removes references from Parameter to PricedParameter. This method is
     * called before a parameter should be deleted.
     */
    private void removeReferences() {
        mgr.createQuery("UPDATE PricedParameter pp SET parameter = NULL")
                .executeUpdate();
    }

    /**
     * Verify if message is send with given trigger type.
     * 
     * @param type
     *            trigger type
     * @return true if send otherwise false
     */
    private boolean isMessageSend(TriggerType type) {
        for (TriggerType usedType : usedTriggersTypes) {
            if (type == usedType) {
                return true;
            }
        }
        return false;
    }

    private void assertInvalidStateException(SubscriptionStateException e,
            SubscriptionStatus s) {
        assertEquals("ex.SubscriptionStateException.SUBSCRIPTION_INVALID_STATE",
                e.getMessageKey());
        assertEquals("enum.SubscriptionStatus." + s.name(),
                e.getMessageParams()[0]);
    }
}
