/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Creation Date: 03.12.2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.setup;

import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.junit.AfterClass;
import org.oscm.accountservice.bean.AccountServiceBean;
import org.oscm.accountservice.bean.MarketingPermissionServiceBean;
import org.oscm.accountservice.bean.UserLicenseServiceLocalBean;
import org.oscm.accountservice.dao.PaymentTypeDao;
import org.oscm.accountservice.dao.TechnicalProductDao;
import org.oscm.accountservice.dao.UserLicenseDao;
import org.oscm.applicationservice.bean.ApplicationServiceBean;
import org.oscm.applicationservice.local.ApplicationServiceLocal;
import org.oscm.auditlog.bean.AuditLogServiceBean;
import org.oscm.auditlog.dao.AuditLogDao;
import org.oscm.billingservice.business.calculation.revenue.RevenueCalculatorBean;
import org.oscm.billingservice.business.calculation.share.SharesCalculatorBean;
import org.oscm.billingservice.dao.BillingDataRetrievalServiceBean;
import org.oscm.billingservice.dao.SharesDataRetrievalServiceBean;
import org.oscm.billingservice.evaluation.BillingResultEvaluator;
import org.oscm.billingservice.evaluation.BrokerShareResultEvaluator;
import org.oscm.billingservice.evaluation.MarketplaceShareResultEvaluator;
import org.oscm.billingservice.evaluation.ResellerShareResultEvaluator;
import org.oscm.billingservice.evaluation.SupplierShareResultEvaluator;
import org.oscm.billingservice.service.BillingServiceBean;
import org.oscm.billingservice.service.BillingServiceLocal;
import org.oscm.billingservice.service.model.BillingRun;
import org.oscm.communicationservice.local.CommunicationServiceLocal;
import org.oscm.converter.DateConverter;
import org.oscm.converter.XMLConverter;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.bean.HibernateIndexer;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.BillingResult;
import org.oscm.domobjects.BillingSharesResult;
import org.oscm.domobjects.CatalogEntry;
import org.oscm.domobjects.ConfigurationSetting;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.TriggerProcess;
import org.oscm.encrypter.AESEncrypter;
import org.oscm.eventservice.bean.EventServiceBean;
import org.oscm.i18nservice.bean.ImageResourceServiceBean;
import org.oscm.i18nservice.bean.LocalizerServiceBean;
import org.oscm.i18nservice.local.ImageResourceServiceLocal;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.identityservice.bean.IdentityServiceBean;
import org.oscm.identityservice.ldap.LdapAccessStub;
import org.oscm.identityservice.ldap.LdapSettingsManagementServiceBean;
import org.oscm.identityservice.local.IdentityServiceLocal;
import org.oscm.interceptor.DateFactory;
import org.oscm.internal.accountmgmt.AccountServiceManagementBean;
import org.oscm.internal.intf.MarketplaceCacheService;
import org.oscm.internal.intf.SubscriptionSearchService;
import org.oscm.internal.intf.TriggerService;
import org.oscm.internal.marketplace.MarketplaceServiceManagePartnerBean;
import org.oscm.internal.pricing.PricingServiceBean;
import org.oscm.internal.service.PublishServiceBean;
import org.oscm.internal.types.enumtypes.BillingSharesResultType;
import org.oscm.internal.types.enumtypes.ConfigurationKey;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.vo.VOCategory;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.internal.vo.VOSubscriptionDetails;
import org.oscm.kafka.service.Producer;
import org.oscm.marketplace.auditlog.MarketplaceAuditLogCollector;
import org.oscm.marketplace.bean.LandingpageServiceBeanLocal;
import org.oscm.marketplace.bean.MarketplaceServiceBean;
import org.oscm.marketplace.bean.MarketplaceServiceLocalBean;
import org.oscm.marketplace.dao.MarketplaceAccessDao;
import org.oscm.operatorservice.bean.OperatorServiceBean;
import org.oscm.paymentservice.bean.PaymentServiceBean;
import org.oscm.paymentservice.bean.PortLocatorBean;
import org.oscm.reviewservice.bean.ReviewServiceLocalBean;
import org.oscm.reviewservice.dao.ProductReviewDao;
import org.oscm.serviceprovisioningservice.auditlog.PriceModelAuditLogCollector;
import org.oscm.serviceprovisioningservice.auditlog.ServiceAuditLogCollector;
import org.oscm.serviceprovisioningservice.bean.BillingAdapterLocalBean;
import org.oscm.serviceprovisioningservice.bean.SearchServiceBean;
import org.oscm.serviceprovisioningservice.bean.ServiceProvisioningPartnerServiceLocalBean;
import org.oscm.serviceprovisioningservice.bean.ServiceProvisioningServiceBean;
import org.oscm.serviceprovisioningservice.bean.ServiceProvisioningServiceLocalizationBean;
import org.oscm.serviceprovisioningservice.bean.TagServiceBean;
import org.oscm.sessionservice.local.SessionServiceLocal;
import org.oscm.subscriptionservice.auditlog.SubscriptionAuditLogCollector;
import org.oscm.subscriptionservice.bean.ManageSubscriptionBean;
import org.oscm.subscriptionservice.bean.ModifyAndUpgradeSubscriptionBean;
import org.oscm.subscriptionservice.bean.SubscriptionListServiceBean;
import org.oscm.subscriptionservice.bean.SubscriptionServiceBean;
import org.oscm.subscriptionservice.bean.SubscriptionUtilBean;
import org.oscm.subscriptionservice.bean.TerminateSubscriptionBean;
import org.oscm.subscriptionservice.bean.ValidateSubscriptionStateBean;
import org.oscm.subscriptionservice.local.SubscriptionServiceLocal;
import org.oscm.taskhandling.local.TaskQueueServiceLocal;
import org.oscm.techproductoperation.bean.OperationRecordServiceLocalBean;
import org.oscm.techproductoperation.dao.OperationRecordDao;
import org.oscm.tenantprovisioningservice.bean.TenantProvisioningServiceBean;
import org.oscm.tenantprovisioningservice.vo.TenantProvisioningResult;
import org.oscm.test.BillingResultReader;
import org.oscm.test.DateTimeHandling;
import org.oscm.test.StaticEJBTestBase;
import org.oscm.test.TestDateFactory;
import org.oscm.test.ejb.TestContainer;
import org.oscm.test.stubs.AccountServiceStub;
import org.oscm.test.stubs.CategorizationServiceStub;
import org.oscm.test.stubs.ConfigurationServiceStub;
import org.oscm.test.stubs.IdentityServiceStub;
import org.oscm.test.stubs.MarketplaceServiceStub;
import org.oscm.test.stubs.TriggerQueueServiceStub;
import org.oscm.timerservice.bean.TimerServiceBean;
import org.oscm.triggerservice.local.TriggerMessage;
import org.oscm.triggerservice.local.TriggerProcessMessageData;
import org.oscm.triggerservice.local.TriggerServiceLocal;
import org.oscm.types.constants.Configuration;
import org.oscm.types.enumtypes.ProvisioningType;
import org.oscm.usergroupservice.auditlog.UserGroupAuditLogCollector;
import org.oscm.usergroupservice.bean.UserGroupServiceLocalBean;
import org.oscm.usergroupservice.dao.UserGroupDao;
import org.oscm.usergroupservice.dao.UserGroupUsersDao;
import org.oscm.vatservice.bean.VatServiceBean;
import org.w3c.dom.Document;

/**
 * @author baumann
 * 
 */
public class BillingIntegrationTestBase extends StaticEJBTestBase {

    private static Map<String, TestData> testCache;
    private static Map<String, List<VOSubscriptionDetails>> subscriptionCache;
    private static Map<String, List<VOOrganization>> customersPerScenario;
    private static DataService dataService;
    private static BillingServiceLocal billingService;
    private static ConfigurationServiceStub configurationService;
    private static IdentityServiceLocal identityServiceLocal;
    private static boolean setInvoiceAsDefaultPayment = false;
    private static TestBasicSetup basicSetup;

    public static <T> T runTX(Callable<T> callable) throws Exception {
        return StaticEJBTestBase.runTX(callable);
    }

    @AfterClass
    public static void tearDown() {
        dataService = null;
        billingService = null;
        configurationService = null;
        identityServiceLocal = null;
        basicSetup = null;
        customersPerScenario = null;
        subscriptionCache = null;
        testCache = null;
    }

    public static void initialize() throws Exception {
        containerSetup(container);
        basicSetup = new TestBasicSetup(container);

        customersPerScenario = new HashMap<>();
        subscriptionCache = new HashMap<>();
        testCache = new HashMap<>();
    }

    public static TestData getTestData(String testName) {
        return testCache.get(testName);
    }

    public static void addToTestDataCache(String testName, TestData value) {
        testCache.put(testName, value);
    }

    /**
     * Base setup for billing test data.
     * 
     * It adds the test data for organization roles, create payment types,
     * supported countries, Currencies, operator, supplier organizations,
     * payment Configuration,marketplace, customer organizations, and technical
     * service
     */
    public static void createBasicTestData(final boolean basicSetupRequired)
            throws Exception {
        setDateFactoryInstance(
                DateTimeHandling.calculateMillis("2010-01-01 00:00:00"));

        basicDataSetup(basicSetupRequired);

        createOperatorOrganization();
        container.login(basicSetup.getPlatformOperatorUserKey(),
                ROLE_PLATFORM_OPERATOR);

        addCurrencies();
        createSupplierOrganization();
        savePaymentConfiguration();
        createCustomerOrganizations();
        createTechnicalService(container);
        createAsyncTechnicalService(container);
    }

    public static void setDateFactoryInstance(String dateSource) {
        setDateFactoryInstance(DateTimeHandling.calculateMillis(dateSource));
    }

    public static void setDateFactoryInstance(long millis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);
        DateFactory.setInstance(new TestDateFactory(cal.getTime()));
    }

    public static TestContainer getContainer() {
        return container;
    }

    public static TestBasicSetup getBasicSetup() {
        return basicSetup;
    }

    // use stub not bean to remove the cycle dependency while
    // injecting the dependent beans
    private static void containerSetup(final TestContainer container)
            throws Exception {

        AESEncrypter.generateKey();
        container.addBean(mock(LocalizerServiceLocal.class));
        addConfigurationServiceStub(container);
        container.addBean(mock(Producer.class));
        container.addBean(mock(AuditLogDao.class));
        container.addBean(mock(AuditLogServiceBean.class));
        container.addBean(mock(SubscriptionAuditLogCollector.class));
        container.addBean(mock(PriceModelAuditLogCollector.class));
        container.addBean(mock(ServiceAuditLogCollector.class));
        container.addBean(mock(MarketplaceAuditLogCollector.class));

        container.addBean(new DataServiceBean());
        container.addBean(new SubscriptionListServiceBean());
        container.addBean(new PaymentTypeDao());
        container.addBean(mock(MarketplaceAccessDao.class));
        container.addBean(mock(MarketplaceCacheService.class));
        container.addBean(new LocalizerServiceBean());
        container.addBean(mock(SessionServiceLocal.class));
        container.addBean(mock(ApplicationServiceLocal.class));
        addIdentityServiceStub(container);
        addTenantProvisioningServiceStub(container);
        container.addBean(mock(CommunicationServiceLocal.class));
        container.addBean(mock(ImageResourceServiceLocal.class));
        container.addBean(mock(TaskQueueServiceLocal.class));
        container.addBean(mock(SubscriptionServiceLocal.class));
        container.addBean(new TriggerQueueServiceStub() {
            @Override
            public List<TriggerProcessMessageData> sendSuspendingMessages(
                    List<TriggerMessage> messageData) {
                List<TriggerProcessMessageData> result = new ArrayList<>();
                for (TriggerMessage m : messageData) {
                    TriggerProcess tp = new TriggerProcess();
                    PlatformUser user = new PlatformUser();
                    user.setKey(basicSetup.getPlatformOperatorUserKey());
                    tp.setUser(user);
                    TriggerProcessMessageData data = new TriggerProcessMessageData(
                            tp, m);
                    result.add(data);
                }

                return result;
            }

        });
        container.addBean(new TagServiceBean());
        container.addBean(new TechnicalProductDao());
        container.addBean(new MarketingPermissionServiceBean());
        container.addBean(new MarketplaceServiceStub());
        container.addBean(new UserGroupDao());
        container.addBean(new UserGroupUsersDao());
        container.addBean(new UserGroupAuditLogCollector());
        container.addBean(new UserGroupServiceLocalBean());
        container.addBean(new LandingpageServiceBeanLocal());
        container.addBean(new ServiceProvisioningServiceLocalizationBean());
        container.addBean(new BillingAdapterLocalBean());
        container.addBean(new AccountServiceStub());
        container.addBean(new CategorizationServiceStub() {
            @Override
            public boolean updateAssignedCategories(CatalogEntry catalogEntry,
                    List<VOCategory> categories) {
                return true;
            }
        });
        container.addBean(mock(HibernateIndexer.class));
        container.addBean(new SubscriptionListServiceBean());
        container.addBean(new SubscriptionUtilBean());
        container.addBean(new ModifyAndUpgradeSubscriptionBean());
        container.addBean(new ManageSubscriptionBean());
        container.addBean(new TerminateSubscriptionBean());
        container.addBean(new ValidateSubscriptionStateBean());
        container.addBean(new OperationRecordDao());
        container.addBean(new OperationRecordServiceLocalBean());
        container.addBean(mock(SubscriptionSearchService.class));
        container.addBean(mock(TriggerService.class));
        container.addBean(new SubscriptionServiceBean());
        container.addBean(new ServiceProvisioningServiceBean());
        container.addBean(new ServiceProvisioningPartnerServiceLocalBean());
        container.addBean(new MarketplaceServiceLocalBean());
        container.addBean(new MarketplaceServiceBean());
        container.addBean(new ProductReviewDao());
        container.addBean(new ReviewServiceLocalBean());
        container.addBean(new LdapSettingsManagementServiceBean());
        container.addBean(new LdapAccessStub());
        addIdentityServiceBean(container);
        container.addBean(new BillingDataRetrievalServiceBean());
        container.addBean(new PortLocatorBean());
        container.addBean(new PaymentServiceBean());
        container.addBean(mock(UserLicenseDao.class));
        container.addBean(mock(UserLicenseServiceLocalBean.class));
        container.addBean(new AccountServiceBean());
        container.addBean(new SearchServiceBean());
        container.addBean(new ImageResourceServiceBean());
        container.addBean(new SharesDataRetrievalServiceBean());
        container.addBean(new SharesCalculatorBean());
        container.addBean(new RevenueCalculatorBean());
        container.addBean(new BillingServiceBean());
        container.addBean(mock(TriggerServiceLocal.class));
        container.addBean(mock(TimerServiceBean.class));
        container.addBean(new OperatorServiceBean());
        container.addBean(new LandingpageServiceBeanLocal());
        container.addBean(new MarketplaceServiceLocalBean());
        container.addBean(new MarketplaceServiceBean());
        container.addBean(new ApplicationServiceBean());
        container.addBean(new AccountServiceManagementBean());
        container.addBean(new EventServiceBean());
        container.addBean(new VatServiceBean());
        container.addBean(new ServiceProvisioningPartnerServiceLocalBean());
        container.addBean(new PricingServiceBean());
        container.addBean(new MarketplaceServiceManagePartnerBean());
        container.addBean(new PublishServiceBean());


        dataService = container.get(DataService.class);
        billingService = container.get(BillingServiceLocal.class);
        configurationService = container.get(ConfigurationServiceStub.class);
        identityServiceLocal = container.get(IdentityServiceLocal.class);
    }

    private static void addTenantProvisioningServiceStub(
            final TestContainer container) throws Exception {
        container.addBean(new TenantProvisioningServiceBean() {
            @Override
            public TenantProvisioningResult createProductInstance(
                    Subscription subscription) {
                TenantProvisioningResult result = new TenantProvisioningResult();
                ProvisioningType provType = subscription.getProduct()
                        .getTechnicalProduct().getProvisioningType();
                result.setAsyncProvisioning(
                        provType == ProvisioningType.ASYNCHRONOUS);
                return result;
            }

            @Override
            public void deleteProductInstance(Subscription subscription) {
            }
        });
    }

    private static void addConfigurationServiceStub(
            final TestContainer container) throws Exception {
        container.addBean(new ConfigurationServiceStub() {
            @Override
            public ConfigurationSetting getConfigurationSetting(
                    ConfigurationKey informationId, String contextId) {
                if (ConfigurationKey.SUPPLIER_SETS_INVOICE_AS_DEFAULT
                        .equals(informationId)) {

                    super.setConfigurationSetting(new ConfigurationSetting(
                            ConfigurationKey.SUPPLIER_SETS_INVOICE_AS_DEFAULT,
                            Configuration.GLOBAL_CONTEXT,
                            Boolean.valueOf(setInvoiceAsDefaultPayment)
                                    .toString()));
                }
                return super.getConfigurationSetting(informationId, contextId);
            }
        });
    }

    private static void addIdentityServiceBean(final TestContainer container)
            throws Exception {
        container.addBean(new IdentityServiceBean() {
            @Override
            public void sendMailToCreatedUser(String password,
                    boolean userLocalLdap, Marketplace marketplace,
                    PlatformUser pu) {
            }
        });
    }

    private static void addIdentityServiceStub(final TestContainer container)
            throws Exception {
        container.addBean(new IdentityServiceStub() {
            @Override
            public void sendMailToCreatedUser(String password,
                    boolean userLocalLdap, Marketplace marketplace,
                    PlatformUser pu) {
            }

            @Override
            public PlatformUser getPlatformUser(String userId,
                    boolean validateOrganization) {
                PlatformUser user = null;
                try {
                    user = identityServiceLocal.getPlatformUser(userId, false);
                } catch (ObjectNotFoundException e) {
                    throw new UnsupportedOperationException();
                } catch (OperationNotPermittedException e) {
                    throw new UnsupportedOperationException();
                }
                return user;
            }

            @Override
            public PlatformUser getPlatformUser(String userId, String tenantId,
                    boolean validateOrganization) {
                PlatformUser user = null;
                try {
                    user = identityServiceLocal.getPlatformUser(userId,
                            tenantId, false);
                } catch (ObjectNotFoundException e) {
                    throw new UnsupportedOperationException();
                } catch (OperationNotPermittedException e) {
                    throw new UnsupportedOperationException();
                }
                return user;
            }
        });
    }

    private static void basicDataSetup(final boolean basicSetupRequired)
            throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                basicSetup.createBasicData(basicSetupRequired);
                return null;
            }
        });
    }

    private static void createOperatorOrganization() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                basicSetup.registerOperatorOrganisation();
                return null;
            }
        });
    }

    private static void addCurrencies() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                basicSetup.addCurrencies();
                return null;
            }
        });
    }

    public static void createSupplierOrganization() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                basicSetup.registerDefaultSupplierOrganisation();
                return null;
            }
        });

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                container.login(basicSetup.getPlatformOperatorUserKey(),
                        ROLE_PLATFORM_OPERATOR);
                basicSetup.createSupplierMarketplace();
                return null;
            }
        });

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                container.login(basicSetup.getSupplierAdminKey(),
                        ROLE_ORGANIZATION_ADMIN);
                basicSetup.createTechnologyProviderUser();
                return null;
            }
        });
    }

    private static void createCustomerOrganizations() throws Exception {
        // create first customer organization and assign two user, one with
        // admin role
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                container.login(basicSetup.getSupplierAdminKey(),
                        ROLE_ORGANIZATION_ADMIN);
                basicSetup.registerTestCustomer();
                container.login(basicSetup.getCustomerAdminKey(),
                        ROLE_ORGANIZATION_ADMIN);
                basicSetup.registerCustomerUsers();
                return null;
            }
        });

        // create second customer organization and assign two user, one with
        // admin role
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                basicSetup.registerSecondTestCustomer();
                container.login(basicSetup.getSecondCustomerAdminKey(),
                        ROLE_ORGANIZATION_ADMIN);
                basicSetup.registerSecondCustomerUsers();
                return null;
            }
        });
    }

    private static void savePaymentConfiguration() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                container.login(basicSetup.getPlatformOperatorUserKey(),
                        ROLE_PLATFORM_OPERATOR);
                basicSetup.savePSPAccount();
                return null;
            }
        });
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                basicSetup.addAvailablePaymentTypeForSupplier();
                return null;
            }
        });
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                container.login(basicSetup.getSupplierAdminKey(),
                        ROLE_SERVICE_MANAGER);
                basicSetup.savePaymentConfigForSupplier();
                return null;
            }
        });
    }

    private static void createTechnicalService(final TestContainer container)
            throws Exception {

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                container.login(basicSetup.getSupplierAdminKey(),
                        ROLE_TECHNOLOGY_MANAGER);
                basicSetup.createTechnicalService(TECHNICAL_SERVICES_XML);
                return null;
            }
        });

    }

    private static void createAsyncTechnicalService(
            final TestContainer container) throws Exception {

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                container.login(basicSetup.getSupplierAdminKey(),
                        ROLE_TECHNOLOGY_MANAGER);
                basicSetup.createAsyncTechnicalService(
                        TECHNICAL_SERVICES_ASYNC_XML);
                return null;
            }
        });

    }

    public static List<VOSubscriptionDetails> getSubscriptionDetails(
            String key) {
        return subscriptionCache.get(key);
    }

    public static VOSubscriptionDetails getSubscriptionDetails(String key,
            int subscriptionIndex) {
        return getSubscriptionDetails(key).get(subscriptionIndex);
    }

    public static void addToCache(VOSubscriptionDetails subDetails) {
        updateSubscriptionListForTests(subDetails.getSubscriptionId(),
                subDetails);
    }

    public static void updateSubscriptionListForTests(String key,
            VOSubscriptionDetails subDetails) {
        List<VOSubscriptionDetails> subDetailsList = subscriptionCache.get(key);
        if (subDetailsList == null) {
            subDetailsList = new ArrayList<>();
            subscriptionCache.put(key, subDetailsList);
        }
        subDetailsList.add(subDetails);
    }

    public static void updateCustomerListForTests(String scenarioId,
            VOOrganization customer) {
        List<VOOrganization> customerList = customersPerScenario
                .get(scenarioId);
        if (customerList == null) {
            customerList = new ArrayList<>();
            customersPerScenario.put(scenarioId, customerList);
        }
        customerList.add(customer);
    }

    protected void setBillingRunOffset(final long offsetInMs) throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                ConfigurationSetting config = new ConfigurationSetting(
                        ConfigurationKey.TIMER_INTERVAL_BILLING_OFFSET,
                        Configuration.GLOBAL_CONTEXT,
                        Long.valueOf(offsetInMs).toString());
                configurationService.setConfigurationSetting(config);
                return null;
            }
        });
    }

    protected void performBillingRun(final long billingOffsetInDays,
            final String invocationTime) throws Exception {
        performBillingRun(billingOffsetInDays,
                DateTimeHandling.calculateMillis(invocationTime));
    }

    protected void performBillingRun(final long billingOffsetInDays,
            final long invocationTime) throws Exception {
        setDateFactoryInstance(invocationTime);
        setBillingRunOffset(
                billingOffsetInDays * DateConverter.MILLISECONDS_PER_DAY);

        runTX(new Callable<Void>() {
            @Override
            public Void call() {
                billingService.startBillingRun(invocationTime);
                return null;
            }
        });
    }

    protected Document loadBillingResult(final long subscriptionKey,
            final long billingPeriodStart, final long billingPeriodEnd)
            throws Exception {
        return runTX(new Callable<Document>() {
            @Override
            public Document call() throws Exception {

                Document doc = null;
                BillingResult billingResult = BillingResultReader
                        .loadBillingResult(dataService, subscriptionKey,
                                billingPeriodStart, billingPeriodEnd);
                if (billingResult != null) {
                    System.out.println(billingResult.getResultXML());
                    doc = XMLConverter.convertToDocument(
                            billingResult.getResultXML(), true);
                }

                return doc;
            }
        });
    }

    protected SupplierShareResultEvaluator newSupplierShareResultEvaluator(
            long supplierKey, final String periodStart, final String periodEnd)
            throws Exception {
        Document sharesResult = loadSharesResult(supplierKey, periodStart,
                periodEnd, BillingSharesResultType.SUPPLIER);
        return new SupplierShareResultEvaluator(sharesResult);
    }

    protected MarketplaceShareResultEvaluator newMarketplaceShareResultEvaluator(
            long mpOwnerKey, final String periodStart, final String periodEnd)
            throws Exception {
        Document sharesResult = loadSharesResult(mpOwnerKey, periodStart,
                periodEnd, BillingSharesResultType.MARKETPLACE_OWNER);
        return sharesResult == null ? null
                : new MarketplaceShareResultEvaluator(sharesResult);
    }

    protected BrokerShareResultEvaluator newBrokerShareResultEvaluator(
            long brokerKey, final String periodStart, final String periodEnd)
            throws Exception {
        Document sharesResult = loadSharesResult(brokerKey, periodStart,
                periodEnd, BillingSharesResultType.BROKER);
        return new BrokerShareResultEvaluator(sharesResult);
    }

    protected ResellerShareResultEvaluator newResellerShareResultEvaluator(
            long resellerKey, final String periodStart, final String periodEnd)
            throws Exception {
        Document sharesResult = loadSharesResult(resellerKey, periodStart,
                periodEnd, BillingSharesResultType.RESELLER);
        return new ResellerShareResultEvaluator(sharesResult);
    }

    protected Document loadSharesResult(final long organizationKey,
            final String periodStart, final String periodEnd,
            final BillingSharesResultType resultType) throws Exception {

        long start = DateTimeHandling.calculateMillis(periodStart);
        long end = DateTimeHandling.calculateMillis(periodEnd);
        return loadSharesResult(organizationKey, start, end, resultType);
    }

    protected Document loadSharesResult(final long organizationKey,
            final long periodStart, final long periodEnd,
            final BillingSharesResultType resultType) throws Exception {

        return runTX(new Callable<Document>() {
            @Override
            public Document call() throws Exception {
                Query query = dataService.createNamedQuery(
                        "BillingSharesResult.getSharesResult");
                query.setParameter("fromDate", Long.valueOf(periodStart));
                query.setParameter("toDate", Long.valueOf(periodEnd));
                query.setParameter("resultType", resultType);

                try {
                    @SuppressWarnings("unchecked")
                    List<BillingSharesResult> shareDocuments = query
                            .getResultList();
                    BillingSharesResult result = null;
                    for (BillingSharesResult bsr : shareDocuments) {
                        if (bsr.getOrganizationTKey() == organizationKey) {
                            result = bsr;
                        }
                    }
                    if (result == null) {
                        return null;
                    }

                    System.out.println(result.getResultXML());
                    Document doc = XMLConverter
                            .convertToDocument(result.getResultXML(), true);
                    return doc;
                } catch (NoResultException e) {
                    return null;
                }
            }
        });
    }

    protected List<BillingResult> generateBillingForAnyPeriod(
            String periodStart, String periodEnd, long customerKey)
            throws Exception {
        return billingService.generateBillingForAnyPeriod(
                DateTimeHandling.calculateMillis(periodStart),
                DateTimeHandling.calculateMillis(periodEnd), customerKey);
    }

    protected BillingRun generatePaymentPreviewReport(long organizationKey,
            String date) {
        setDateFactoryInstance(date);
        return billingService.generatePaymentPreviewReport(organizationKey);
    }

    /**
     * @return an evaluator for a billing result which is uniquely defined by
     *         start date, end date and a subscription key. Returns null if no
     *         billing result could be found.
     */
    protected BillingResultEvaluator getEvaluator(long subscriptionKey,
            String periodStart, String periodEnd) throws Exception {

        Document billingResult = loadBillingResult(subscriptionKey,
                DateTimeHandling.calculateMillis(periodStart),
                DateTimeHandling.calculateMillis(periodEnd));
        if (billingResult != null) {
            return new BillingResultEvaluator(billingResult);
        }

        return null;
    }

}
