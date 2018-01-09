/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 2014-4-29                                                      
 *                                                                              
 *******************************************************************************/
package org.oscm.subscriptionservice.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Currency;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.junit.Test;
import org.oscm.accountservice.bean.MarketingPermissionServiceBean;
import org.oscm.applicationservice.bean.ApplicationServiceStub;
import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.converter.ParameterizedTypes;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.BillingContact;
import org.oscm.domobjects.CatalogEntry;
import org.oscm.domobjects.ConfigurationSetting;
import org.oscm.domobjects.DomainHistoryObject;
import org.oscm.domobjects.ImageResource;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.OrganizationRefToPaymentType;
import org.oscm.domobjects.OrganizationReference;
import org.oscm.domobjects.OrganizationRole;
import org.oscm.domobjects.PSP;
import org.oscm.domobjects.Parameter;
import org.oscm.domobjects.ParameterDefinition;
import org.oscm.domobjects.ParameterOption;
import org.oscm.domobjects.ParameterSet;
import org.oscm.domobjects.PaymentInfo;
import org.oscm.domobjects.PaymentType;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.PriceModel;
import org.oscm.domobjects.PricedEvent;
import org.oscm.domobjects.PricedOption;
import org.oscm.domobjects.PricedParameter;
import org.oscm.domobjects.Product;
import org.oscm.domobjects.ProductReference;
import org.oscm.domobjects.ProductToPaymentType;
import org.oscm.domobjects.Session;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.SubscriptionData;
import org.oscm.domobjects.SubscriptionHistory;
import org.oscm.domobjects.SupportedCurrency;
import org.oscm.domobjects.TechnicalProduct;
import org.oscm.domobjects.TriggerDefinition;
import org.oscm.domobjects.TriggerProcess;
import org.oscm.domobjects.UsageLicense;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.domobjects.enums.ModificationType;
import org.oscm.domobjects.enums.OrganizationReferenceType;
import org.oscm.encrypter.AESEncrypter;
import org.oscm.i18nservice.bean.LocalizerFacade;
import org.oscm.i18nservice.bean.LocalizerServiceStub2;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.identityservice.assembler.UserDataAssembler;
import org.oscm.identityservice.bean.IdManagementStub;
import org.oscm.internal.intf.IdentityService;
import org.oscm.internal.intf.ServiceProvisioningService;
import org.oscm.internal.intf.SubscriptionService;
import org.oscm.internal.types.enumtypes.ConfigurationKey;
import org.oscm.internal.types.enumtypes.EventType;
import org.oscm.internal.types.enumtypes.ImageType;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.ParameterType;
import org.oscm.internal.types.enumtypes.ParameterValueType;
import org.oscm.internal.types.enumtypes.PaymentCollectionType;
import org.oscm.internal.types.enumtypes.PriceModelType;
import org.oscm.internal.types.enumtypes.ServiceAccessType;
import org.oscm.internal.types.enumtypes.ServiceStatus;
import org.oscm.internal.types.enumtypes.SessionType;
import org.oscm.internal.types.enumtypes.SubscriptionStatus;
import org.oscm.internal.types.enumtypes.TriggerType;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.PaymentInformationException;
import org.oscm.internal.types.exception.TechnicalServiceNotAliveException;
import org.oscm.internal.types.exception.TechnicalServiceOperationException;
import org.oscm.internal.vo.VOBillingContact;
import org.oscm.internal.vo.VOCategory;
import org.oscm.internal.vo.VOInstanceInfo;
import org.oscm.internal.vo.VOLocalizedText;
import org.oscm.internal.vo.VOPaymentInfo;
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOSubscription;
import org.oscm.internal.vo.VOSubscriptionDetails;
import org.oscm.internal.vo.VOUda;
import org.oscm.internal.vo.VOUsageLicense;
import org.oscm.internal.vo.VOUser;
import org.oscm.marketplace.bean.LandingpageServiceBeanLocal;
import org.oscm.marketplace.bean.MarketplaceServiceBean;
import org.oscm.marketplace.bean.MarketplaceServiceLocalBean;
import org.oscm.serviceprovisioningservice.assembler.ProductAssembler;
import org.oscm.serviceprovisioningservice.bean.ServiceProvisioningServiceBean;
import org.oscm.serviceprovisioningservice.bean.ServiceProvisioningServiceLocalizationBean;
import org.oscm.serviceprovisioningservice.bean.TagServiceBean;
import org.oscm.sessionservice.bean.SessionManagementStub2;
import org.oscm.subscriptionservice.assembler.SubscriptionAssembler;
import org.oscm.subscriptionservice.local.SubscriptionServiceLocal;
import org.oscm.taskhandling.local.TaskMessage;
import org.oscm.taskhandling.operations.NotifyProvisioningServiceHandler;
import org.oscm.taskhandling.operations.SendMailHandler;
import org.oscm.taskhandling.payloads.NotifyProvisioningServicePayload;
import org.oscm.taskhandling.payloads.SendMailPayload;
import org.oscm.tenantprovisioningservice.bean.TenantProvisioningServiceBean;
import org.oscm.test.EJBTestBase;
import org.oscm.test.ReflectiveClone;
import org.oscm.test.data.Marketplaces;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.PaymentInfos;
import org.oscm.test.data.PaymentTypes;
import org.oscm.test.data.Products;
import org.oscm.test.data.Subscriptions;
import org.oscm.test.data.SupportedCountries;
import org.oscm.test.data.TechnicalProducts;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.AccountServiceStub;
import org.oscm.test.stubs.CategorizationServiceStub;
import org.oscm.test.stubs.CommunicationServiceStub;
import org.oscm.test.stubs.ConfigurationServiceStub;
import org.oscm.test.stubs.ImageResourceServiceStub;
import org.oscm.test.stubs.MarketplaceServiceStub;
import org.oscm.test.stubs.TaskQueueServiceStub;
import org.oscm.test.stubs.TriggerQueueServiceStub;
import org.oscm.triggerservice.local.TriggerMessage;
import org.oscm.triggerservice.local.TriggerProcessMessageData;
import org.oscm.types.constants.Configuration;
import org.oscm.types.enumtypes.EmailType;
import org.oscm.types.enumtypes.PlatformParameterIdentifiers;
import org.oscm.types.enumtypes.ProvisioningType;
import org.oscm.types.enumtypes.TriggerProcessParameterName;

/**
 * @author qiu
 * 
 */

public class SubscriptionServiceBeanWithoutHeidelpayIT extends EJBTestBase {

    private static final String BASE_URL_BES_HTTP = "http://localhost:8080/oscm-portal";
    private static final String BASE_URL_BES_HTTPS = "https://localhost:8181/oscm-portal";

    private static final String parameterValueConstant = "2000";
    private static final String SUBSCRIPTION_ID = "subId";
    protected DataService mgr;
    protected ApplicationServiceStub appMgmtStub;
    protected SubscriptionService subMgmt;
    protected SubscriptionServiceLocal subMgmtLocal;
    protected IdentityService idMgmt;
    protected ServiceProvisioningService servProv;
    protected LocalizerServiceLocal localizer;
    private List<PaymentType> paymentTypes;
    private final Map<String, VOPaymentInfo> voPaymentInfos = new HashMap<>();

    private final List<Product> testProducts = new ArrayList<>();
    private final List<Product> asyncTestProducts = new ArrayList<>();
    private final List<Organization> testOrganizations = new ArrayList<>();
    private final Map<Organization, ArrayList<PlatformUser>> testUsers = new HashMap<>();

    private PlatformUser supplierUser;

    private List<TaskMessage> messagesOfTaskQueue = new ArrayList<>();

    private boolean isTriggerQueueService_sendSuspendingMessageCalled = false;
    private boolean isTriggerQueueService_sendAllNonSuspendingMessageCalled = false;

    private List<TriggerType> usedTriggersTypes;
    private TriggerDefinition td;
    private Organization org;
    private Organization tpAndSupplier;
    private String supplierOrgId;

    private String customerUserKey;

    private boolean isCorrectSubscriptionIdForMail = false;

    private EmailType mailType = null;
    private Object[] receivedParams = null;

    private final List<SendMailPayload> receivedSendMailPayload = new ArrayList<>();
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
                    if (message.getHandlerClass() == SendMailHandler.class) {
                        SendMailPayload payload = (SendMailPayload) message
                                .getPayload();
                        receivedSendMailPayload.add(payload);
                    }

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

                        } catch (ObjectNotFoundException e) {
                            e.printStackTrace();
                        } catch (TechnicalServiceNotAliveException e) {
                            e.printStackTrace();
                        } catch (TechnicalServiceOperationException e) {
                            e.printStackTrace();
                        }
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
                try {
                    tp.setUser(mgr.getReference(PlatformUser.class,
                            Long.valueOf(customerUserKey).longValue()));
                } catch (ObjectNotFoundException | NumberFormatException e) {
                    e.printStackTrace();
                }
                tp.setTriggerDefinition(td);
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

        mgr = container.get(DataService.class);
        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                paymentTypes = createPaymentTypesWithoutHeidelpay(mgr);
                createOrganizationRoles(mgr);
                SupportedCountries.createSomeSupportedCountries(mgr);
                Organization operator = Organizations.createOrganization(mgr,
                        OrganizationRoleType.PLATFORM_OPERATOR);
                operator.setOrganizationId(
                        OrganizationRoleType.PLATFORM_OPERATOR.name());
                Marketplaces.createGlobalMarketplace(operator,
                        GLOBAL_MARKETPLACE_NAME, mgr);
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

        testProducts.get(0).getTechnicalProduct()
                .setAccessType(ServiceAccessType.LOGIN);

        usedTriggersTypes = new LinkedList<>();
    }

    private static List<PaymentType> createPaymentTypesWithoutHeidelpay(
            DataService mgr) throws NonUniqueBusinessKeyException {
        PSP psp = new PSP();
        psp.setIdentifier("Invoice");
        psp.setWsdlUrl("");
        mgr.persist(psp);
        List<PaymentType> result = new ArrayList<>();
        PaymentType pt = new PaymentType();
        pt.setCollectionType(PaymentCollectionType.ORGANIZATION);
        pt.setPaymentTypeId(INVOICE);
        pt.setPsp(psp);
        mgr.persist(pt);
        result.add(pt);
        return result;
    }

    @Test
    public void terminateSubscription() throws Throwable {
        final String reason = "";
        final String subscriptionId = "testSubscribeToProduct";
        final VOSubscription subscription = runTX(
                new Callable<VOSubscription>() {
                    @Override
                    public VOSubscription call() throws Exception {
                        VOService product = getProductToSubscribe(
                                testProducts.get(0).getKey());
                        VOUser[] admins = getAdminUser();
                        VOUser[] users = getUsers();
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

        container.login(String.valueOf(supplierUser.getKey()),
                ROLE_SERVICE_MANAGER);
        VOSubscriptionDetails sub = subMgmt.getSubscriptionForCustomer(
                testOrganizations.get(0).getOrganizationId(), subscriptionId);
        subMgmt.terminateSubscription(sub, reason);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Subscription subscriptionAfter = mgr.getReference(
                        Subscription.class, subscription.getKey());
                assertEquals("Check subscription status",
                        subscriptionAfter.getStatus(),
                        SubscriptionStatus.DEACTIVATED);

                List<UsageLicense> licenses = subscriptionAfter
                        .getUsageLicenses();
                assertEquals("Check number of usage license", 0,
                        licenses.size());

                Product product = subscriptionAfter.getProduct();
                assertEquals("Check service status", ServiceStatus.DELETED,
                        product.getStatus());

                Query query = mgr.createQuery("SELECT s FROM Session s");
                List<Session> resultList = query.getResultList();
                assertEquals("Check number of sessions", 0, resultList.size());

                return null;
            }
        });

    }

    @Test
    public void subscribeToService() throws Throwable {
        VOService product = getProductToSubscribe(testProducts.get(0).getKey());
        VOUser[] admins = getAdminUser();
        VOUser[] users = getUsers();
        VOSubscription sub = Subscriptions
                .createVOSubscription("testSubscribeToProduct");
        sub.setPurchaseOrderNumber(TOO_LONG_NAME);
        messagesOfTaskQueue = new ArrayList<>();
        VOSubscription newSub = subMgmt.subscribeToService(sub, product,
                getUsersToAdd(admins, users), null, null,
                new ArrayList<VOUda>());

        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() {
                    checkSubscribeToProduct(false, "testSubscribeToProduct",
                            testProducts.get(0), SubscriptionStatus.ACTIVE, 3,
                            TOO_LONG_NAME);
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
        assertTrue(isMessageSend(TriggerType.SUBSCRIPTION_CREATION));

        assertEquals(2, messagesOfTaskQueue.size());
        assertEquals(2, receivedSendMailPayload.size());
        assertEquals(3, receivedSendMailPayload.get(0).getMailObjects().size());
        assertEquals(1, receivedSendMailPayload.get(1).getMailObjects().size());
        assertTrue(((String) receivedSendMailPayload.get(0).getMailObjects()
                .get(0).getParams()[1]).startsWith(BASE_URL_BES_HTTP));
    }

    @Test
    public void subscribeToService_NewCustomerCopyEnabledPaymentTypes()
            throws Throwable {
        VOService product = getProductToSubscribe(testProducts.get(1).getKey());
        VOUser[] admins = getAdminUser();
        VOUser[] users = getUsers();
        final VOPaymentInfo voPaymentInfo = getPaymentInfo(
                tpAndSupplier.getOrganizationId(), testOrganizations.get(0));

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
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
                Organization po = new Organization();
                po.setOrganizationId(
                        OrganizationRoleType.PLATFORM_OPERATOR.name());
                po = Organization.class.cast(mgr.getReferenceByBusinessKey(po));
                OrganizationReference ref = new OrganizationReference(po, sup,
                        OrganizationReferenceType.PLATFORM_OPERATOR_TO_SUPPLIER);
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
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    checkSubscribeToProduct(false, "testSubscribeToProduct",
                            testProducts.get(1), SubscriptionStatus.ACTIVE, 3,
                            null);

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

    @Test(expected = PaymentInformationException.class)
    public void subscribeToService_ChargeableNoPaymentTypeEnabled()
            throws Throwable {
        assertNotNull(subMgmt);
        VOService product = getProductToSubscribe(testProducts.get(1).getKey());
        VOUser[] admins = getAdminUser();
        VOUser[] users = getUsers();
        final VOPaymentInfo voPaymentInfo = getPaymentInfo(
                tpAndSupplier.getOrganizationId(), testOrganizations.get(0));

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                PaymentInfo pi = mgr.getReference(PaymentInfo.class,
                        voPaymentInfo.getKey());

                PaymentType pt = mgr.getReference(PaymentType.class,
                        paymentTypes.get(0).getKey());
                pi.setPaymentType(pt);
                return null;
            }
        });
        VOBillingContact bc = createBillingContact(testOrganizations.get(0));
        voPaymentInfo.setVersion(1);

        subMgmt.subscribeToService(
                Subscriptions.createVOSubscription("testSubscribeToProduct"),
                product, getUsersToAdd(admins, users), voPaymentInfo, bc,
                new ArrayList<VOUda>());
    }

    @Test(expected = PaymentInformationException.class)
    public void subscribeToService_ChargeableNoPaymentTypeForProduct()
            throws Throwable {
        assertNotNull(subMgmt);
        VOService product = getProductToSubscribe(testProducts.get(1).getKey());
        VOUser[] admins = getAdminUser();
        VOUser[] users = getUsers();
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

    private void checkSubscribeToProduct(boolean checkNotActive,
            String subscriptionId, Product product, SubscriptionStatus status,
            int usersAdded, String pon) {
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
        assertEquals(
                new SimpleDateFormat("yyyy-MM-dd")
                        .format(GregorianCalendar.getInstance().getTime()),
                new SimpleDateFormat("yyyy-MM-dd")
                        .format(subscription.getCreationDate()));
        assertEquals(pon, subscription.getPurchaseOrderNumber());
        assertNotNull("No product assigned to subscription",
                subscription.getProduct());
        assertTrue("Wrong ProductId in subscription", subscription.getProduct()
                .getProductId().startsWith(product.getProductId() + "#"));
        assertNotNull("No priceModel assigned to subscription",
                subscription.getPriceModel());
        assertEquals(
                "PriceModel of subscription is not the same instance as for the product",
                product.getPriceModel().getPricePerPeriod(),
                subscription.getPriceModel().getPricePerPeriod());
        assertEquals(
                "PriceModel of subscription is not the same instance as for the product",
                product.getPriceModel().getPricePerUserAssignment(),
                subscription.getPriceModel().getPricePerUserAssignment());
        assertEquals("Wrong number of UsageLicenses", usersAdded,
                subscription.getUsageLicenses().size());
        Collections.sort(subscription.getUsageLicenses(),
                new Comparator<UsageLicense>() {
                    @Override
                    public int compare(UsageLicense b1, UsageLicense b2) {
                        if (b1 == null || b1.getUser() == null)
                            return -1;
                        if (b2 == null || b2.getUser() == null)
                            return 1;
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
        }
        if (checkNotActive) {
            assertEquals(
                    "Product has been informed about creation of all users", 0,
                    appMgmtStub.addedUsers.size());
            assertNull(subscription.getActivationDate());
            assertNull(subscription.getProductInstanceId());
        } else {
            assertNotNull(subscription.getProductInstanceId());
            int idx = 1;
            assertEquals(
                    "Product has not been informed about creation of all users",
                    subscription.getUsageLicenses().size(),
                    appMgmtStub.addedUsers.size());
            for (UsageLicense lic : subscription.getUsageLicenses()) {
                PlatformUser platformUser = appMgmtStub.addedUsers
                        .get(lic.getUser().getUserId());
                assertNotNull(platformUser);
                assertEquals("Wrong user assigned to product",
                        lic.getUser().getUserId(), platformUser.getUserId());
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

    @Test
    public void upgradeSubscription() throws Throwable {
        createAvailablePayment(testOrganizations.get(0));
        VOService product = getProductToSubscribe(
                testProducts.get(10).getKey());
        VOUser[] admins = getAdminUser();
        VOUser[] users = getUsers();
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
    public void upgradeSubscriptionInt() throws Exception {
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

    @Test(expected = PaymentInformationException.class)
    public void upgradeSubscription_InvalidPaymentInfo() throws Throwable {
        createAvailablePayment(testOrganizations.get(0));
        VOService product = getProductToSubscribe(
                testProducts.get(10).getKey());
        VOUser[] admins = getAdminUser();
        VOUser[] users = getUsers();
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

    @Test
    public void unsubscribeFromService() throws Throwable {
        createAvailablePayment(testOrganizations.get(0));
        VOService product = getProductToSubscribe(testProducts.get(1).getKey());
        VOUser[] admins = getAdminUser();
        VOUser[] users = getUsers();
        String subscriptionId = "testUnsubscribeFromProductOK";
        appMgmtStub.addedUsers.clear();
        appMgmtStub.deletedUsers.clear();

        VOPaymentInfo voPaymentInfo = getPaymentInfo(
                tpAndSupplier.getOrganizationId(), testOrganizations.get(0));
        VOBillingContact bc = createBillingContact(testOrganizations.get(0));

        subMgmt.subscribeToService(
                Subscriptions.createVOSubscription(subscriptionId), product,
                getUsersToAdd(admins, users), voPaymentInfo, bc,
                new ArrayList<VOUda>());

        final VOSubscriptionDetails sub = subMgmt
                .getSubscriptionDetails(subscriptionId);
        assertNotNull(sub);
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

    @Test
    public void unsubscribeFromServiceInt() throws Exception {
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
            PlatformUser admin = Organizations.createUserForOrg(mgr, cust, true,
                    "admin");
            if (initialCustomerAdminKey == null) {
                initialCustomerAdminKey = Long.valueOf(admin.getKey());
            }
            userlist.add((PlatformUser) ReflectiveClone.clone(admin));
            for (int j = 1; j <= 5; j++) {
                PlatformUser user = Organizations.createUserForOrg(mgr, cust,
                        false, "user" + j);
                userlist.add((PlatformUser) ReflectiveClone.clone(user));
            }
        }
        supplierUser = Organizations.createUserForOrg(mgr, tpAndSupplier, true,
                "admin");
        return initialCustomerAdminKey;
    }

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

            PaymentTypes.enableForProduct(prod, mgr, PaymentType.INVOICE);

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
    public void expireSubscription() throws Exception {
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
        VOUser[] admins = getAdminUser();
        VOUser[] users = getUsers();
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

        assertFalse("Product has been deleted when the subscription expired",
                appMgmtStub.isProductDeleted);
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

    @Test
    public void completeAsyncSubscription_WithInvalidPaymentInfo()
            throws Throwable {
        final String id = "testCompletion";
        createAvailablePayment(testOrganizations.get(0));

        subscribeAsync(id, 1, false);

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {

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
                template.setIdentifier("Invoice");
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
                            SubscriptionStatus.SUSPENDED, 3, null);
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
    public void completeAsyncSubscription_WithPaymentTypeForProduct()
            throws Throwable {
        final String id = "testCompletion2";
        createAvailablePayment(testOrganizations.get(0));

        subscribeAsync(id, 1, false);

        String orgId = testOrganizations.get(0).getOrganizationId();

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
                            SubscriptionStatus.ACTIVE, 3, null);
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
    public void completeAsyncSubscription_WithWrongPaymentTypeForProduct()
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
                            SubscriptionStatus.SUSPENDED, 3, null);
                    return null;
                }
            });
        } catch (Exception e) {
            throw e.getCause();
        }
        assertTrue(appMgmtStub.deactivated);
        assertFalse(appMgmtStub.activated);
    }

    private void subscribeAsync(final String id, final int indexInList,
            boolean noPaymentInfo) throws Throwable {
        assertNotNull(subMgmt);
        appMgmtStub.addedUsers.clear();
        appMgmtStub.deletedUsers.clear();
        VOService product = getProductToSubscribe(
                asyncTestProducts.get(indexInList).getKey());
        VOUser[] admins = getAdminUser();
        VOUser[] users = getUsers();
        VOPaymentInfo voPaymentInfo = getPaymentInfo(
                tpAndSupplier.getOrganizationId(), testOrganizations.get(0));
        VOBillingContact bc = createBillingContact(testOrganizations.get(0));
        boolean async = subMgmt
                .subscribeToService(Subscriptions.createVOSubscription(id),
                        product, getUsersToAdd(admins, users),
                        noPaymentInfo ? null : voPaymentInfo, bc,
                        new ArrayList<VOUda>())
                .getStatus() == SubscriptionStatus.PENDING;
        assertTrue(async);
        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() {
                    checkSubscribeToProduct(true, id,
                            asyncTestProducts.get(indexInList),
                            SubscriptionStatus.PENDING, 3, null);
                    return null;
                }
            });
        } catch (Exception e) {
            throw e.getCause();
        }
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

    private Product initCustomerProduct(int index) {
        Product product = mgr.find(Product.class,
                testProducts.get(10).getKey());
        product.setTargetCustomer(testOrganizations.get(index));
        product.setTemplate(
                mgr.find(Product.class, testProducts.get(8).getKey()));
        return product;
    }

    @Test
    public void subscribeToServiceInt() throws Throwable {
        assertNotNull(subMgmtLocal);
        VOService product = getProductToSubscribe(testProducts.get(0).getKey());
        VOUser[] admins = getAdminUser();
        VOUser[] users = getUsers();
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
        tp.setUser(supplierUser);
        final Subscription newSub = runTX(new Callable<Subscription>() {
            @Override
            public Subscription call() throws Exception {
                return subMgmtLocal.subscribeToServiceInt(tp);
            }
        });

        try {
            runTX(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    checkSubscribeToProduct(false, "testSubscribeToProduct",
                            testProducts.get(0), SubscriptionStatus.ACTIVE, 3,
                            null);

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

    @Test
    public void subscribeToService_NonFreeWithPaymentInfo() throws Throwable {

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
        VOUser[] admins = getAdminUser();
        VOUser[] users = getUsers();
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

    @Test
    public void upgradeSubscription_FreeToChargeableOk() throws Throwable {
        createAvailablePayment(testOrganizations.get(0));
        VOService product = getProductToSubscribe(
                testProducts.get(10).getKey());
        VOUser[] admins = getAdminUser();
        VOUser[] users = getUsers();
        VOSubscription sub = Subscriptions.createVOSubscription("testUpgrade");
        assertFalse(product.getPriceModel().isChargeable());
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

    @Test
    public void upgradeSubscription_ChargeableToFreeOk() throws Throwable {
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
        VOUser[] admins = getAdminUser();
        VOUser[] users = getUsers();
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

    @Test
    public void upgradeSubscription_ChargeableToChargeableOk()
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
                return null;
            }
        });

        VOService product = getProductToSubscribe(
                testProducts.get(10).getKey());
        VOUser[] admins = getAdminUser();
        VOUser[] users = getUsers();
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
    public void upgradeSubscription_ChargeableSuspendedToChargeableOk()
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

    private VOUser[] getUsers() {
        VOUser[] users = new VOUser[2];
        users[0] = UserDataAssembler
                .toVOUser(testUsers.get(testOrganizations.get(0)).get(2));
        users[1] = UserDataAssembler
                .toVOUser(testUsers.get(testOrganizations.get(0)).get(3));
        return users;
    }

    private VOUser[] getAdminUser() {
        VOUser[] admins = new VOUser[1];
        admins[0] = UserDataAssembler
                .toVOUser(testUsers.get(testOrganizations.get(0)).get(1));
        return admins;
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

    private VOBillingContact createBillingContact(
            final Organization organization) throws Exception {
        return runTX(new Callable<VOBillingContact>() {

            @Override
            public VOBillingContact call() throws Exception {
                return PaymentInfos.createBillingContact(organization, mgr);
            }
        });
    }

    private boolean isMessageSend(TriggerType type) {
        for (TriggerType usedType : usedTriggersTypes) {
            if (type == usedType) {
                return true;
            }
        }
        return false;
    }

}
