/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 27.03.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.calculation.revenue.setup;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.mockito.Mockito;
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
import org.oscm.billingservice.service.BillingServiceBean;
import org.oscm.communicationservice.local.CommunicationServiceLocal;
import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.dataservice.bean.DataServiceBean;
import org.oscm.dataservice.bean.HibernateIndexer;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.CatalogEntry;
import org.oscm.domobjects.ConfigurationSetting;
import org.oscm.domobjects.Marketplace;
import org.oscm.domobjects.Organization;
import org.oscm.domobjects.PlatformUser;
import org.oscm.domobjects.RoleAssignment;
import org.oscm.domobjects.Subscription;
import org.oscm.domobjects.TriggerProcess;
import org.oscm.domobjects.UserRole;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.encrypter.AESEncrypter;
import org.oscm.eventservice.bean.EventServiceBean;
import org.oscm.i18nservice.bean.ImageResourceServiceBean;
import org.oscm.i18nservice.bean.LocalizerServiceBean;
import org.oscm.i18nservice.local.ImageResourceServiceLocal;
import org.oscm.i18nservice.local.LocalizerServiceLocal;
import org.oscm.identityservice.bean.IdentityServiceBean;
import org.oscm.identityservice.bean.OidcSynchronizationBean;
import org.oscm.identityservice.ldap.LdapAccessStub;
import org.oscm.identityservice.ldap.LdapSettingsManagementServiceBean;
import org.oscm.identityservice.local.IdentityServiceLocal;
import org.oscm.internal.accountmgmt.AccountServiceManagement;
import org.oscm.internal.accountmgmt.AccountServiceManagementBean;
import org.oscm.internal.intf.AccountService;
import org.oscm.internal.intf.MarketplaceCacheService;
import org.oscm.internal.intf.MarketplaceService;
import org.oscm.internal.intf.OperatorService;
import org.oscm.internal.intf.ServiceProvisioningService;
import org.oscm.internal.intf.SubscriptionSearchService;
import org.oscm.internal.types.enumtypes.ConfigurationKey;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.PaymentInfoType;
import org.oscm.internal.types.enumtypes.PriceModelType;
import org.oscm.internal.types.enumtypes.PricingPeriod;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.NonUniqueBusinessKeyException;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.types.exception.OrganizationAuthoritiesException;
import org.oscm.internal.types.exception.ValidationException;
import org.oscm.internal.vo.VOCatalogEntry;
import org.oscm.internal.vo.VOCategory;
import org.oscm.internal.vo.VOMarketplace;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.internal.vo.VOPaymentType;
import org.oscm.internal.vo.VOPriceModel;
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOServiceDetails;
import org.oscm.internal.vo.VOTechnicalService;
import org.oscm.internal.vo.VOUserDetails;
import org.oscm.kafka.service.Producer;
import org.oscm.marketplace.auditlog.MarketplaceAuditLogCollector;
import org.oscm.marketplace.bean.LandingpageServiceBeanLocal;
import org.oscm.marketplace.bean.MarketplaceServiceBean;
import org.oscm.marketplace.bean.MarketplaceServiceLocalBean;
import org.oscm.marketplace.cache.MarketplaceCacheServiceBean;
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
import org.oscm.serviceprovisioningservice.bean.ServiceProvisioningServiceBean;
import org.oscm.serviceprovisioningservice.bean.ServiceProvisioningServiceLocalizationBean;
import org.oscm.serviceprovisioningservice.bean.TagServiceBean;
import org.oscm.serviceprovisioningservice.local.ServiceProvisioningPartnerServiceLocal;
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
import org.oscm.test.EJBTestBase;
import org.oscm.test.data.Organizations;
import org.oscm.test.data.PlatformUsers;
import org.oscm.test.data.SupportedCountries;
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

/**
 * @author kulle
 * 
 */
@SuppressWarnings("boxing")
public class SubscriptionUpgradeSetup {
    private static PlatformUser adminUser;

    public static void setup(TestContainer container) throws Exception {
        AESEncrypter.generateKey();
        container.addBean(new AuditLogDao());
        addConfigurationServiceStub(container);
        container.addBean(new AuditLogServiceBean());
        container.addBean(mockLocalizer());
        container.addBean(mock(SubscriptionAuditLogCollector.class));
        container.addBean(mock(PriceModelAuditLogCollector.class));
        container.addBean(mock(ServiceAuditLogCollector.class));
        container.addBean(mock(MarketplaceAuditLogCollector.class));
        container.addBean(mock(ServiceProvisioningPartnerServiceLocal.class));
        container.addBean(new DataServiceBean());
        container.addBean(new SubscriptionListServiceBean());
        container.addBean(new PaymentTypeDao());
        container.addBean(new LocalizerServiceBean());
        container.addBean(mock(SessionServiceLocal.class));
        container.addBean(mock(ApplicationServiceLocal.class));
        addIdentityServiceStub(container);
        addTenantProvisioningServiceStub(container);
        container.addBean(mock(HibernateIndexer.class));
        container.addBean(mock(CommunicationServiceLocal.class));
        container.addBean(mock(ImageResourceServiceLocal.class));
        container.addBean(mock(TaskQueueServiceLocal.class));
        container.addBean(mock(SubscriptionServiceLocal.class));
        container.addBean(mock(Producer.class));
        container.addBean(new TriggerQueueServiceStub() {
            @Override
            public List<TriggerProcessMessageData> sendSuspendingMessages(
                    List<TriggerMessage> messageData) {
                List<TriggerProcessMessageData> result = new ArrayList<>();
                for (TriggerMessage m : messageData) {
                    TriggerProcess tp = new TriggerProcess();
                    tp.setUser(adminUser);
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
        container.addBean(new MarketplaceCacheServiceBean());
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
        container.addBean(new SubscriptionListServiceBean());
        container.addBean(new SubscriptionUtilBean());
        container.addBean(new ModifyAndUpgradeSubscriptionBean());
        container.addBean(new ManageSubscriptionBean());
        container.addBean(new TerminateSubscriptionBean());
        container.addBean(new ValidateSubscriptionStateBean());
        container.addBean(new OperationRecordDao());
        container.addBean(new OperationRecordServiceLocalBean());
        container.addBean(Mockito.mock(SubscriptionSearchService.class));
        container.addBean(Mockito.mock(org.oscm.internal.intf.TriggerService.class));
        container.addBean(new SubscriptionServiceBean());
        container.addBean(new ServiceProvisioningServiceBean());
        container.addBean(mock(MarketplaceCacheService.class));
        container.addBean(new MarketplaceAccessDao());
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
        container.addBean(mock(TimerServiceBean.class));
        container.addBean(mock(TriggerServiceLocal.class));
        container.addBean(new OperatorServiceBean());
        container.addBean(new LandingpageServiceBeanLocal());
        container.addBean(new MarketplaceServiceLocalBean());
        container.addBean(new MarketplaceServiceBean());
        container.addBean(new ApplicationServiceBean());
        container.addBean(new AccountServiceManagementBean());
        container.addBean(new EventServiceBean());

    }

    private static LocalizerServiceLocal mockLocalizer() {
        LocalizerServiceLocal localizerMock = mock(LocalizerServiceLocal.class);
        final String EMPTY_STRING = "";
        doReturn(EMPTY_STRING).when(localizerMock).getLocalizedTextFromBundle(
                any(LocalizedObjectTypes.class), any(Marketplace.class),
                any(String.class), any(String.class));
        doReturn(EMPTY_STRING).when(localizerMock).getLocalizedTextFromDatabase(
                any(String.class), anyLong(), any(LocalizedObjectTypes.class));
        return localizerMock;
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
                    user = container.get(IdentityServiceLocal.class)
                            .getPlatformUser(userId, false);
                } catch (ObjectNotFoundException
                        | OperationNotPermittedException exception) {
                    throw new UnsupportedOperationException();
                }
                return user;
            }

            @Override
            public PlatformUser getPlatformUser(String userId, String tenantId,
                    boolean validateOrganization) {
                PlatformUser user = null;
                try {
                    user = container.get(IdentityServiceLocal.class)
                            .getPlatformUser(userId, tenantId, false);
                } catch (ObjectNotFoundException
                        | OperationNotPermittedException exception) {
                    throw new UnsupportedOperationException();
                }
                return user;
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
                            Configuration.GLOBAL_CONTEXT, "true"));
                }
                return super.getConfigurationSetting(informationId, contextId);
            }
        });
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

        });
    }

    private static void addIdentityServiceBean(final TestContainer container)
            throws Exception {
        container.addBean(new OidcSynchronizationBean());
        container.addBean(new IdentityServiceBean() {
            @Override
            public void sendMailToCreatedUser(String password,
                    boolean userLocalLdap, Marketplace marketplace,
                    PlatformUser pu) {
            }
        });
    }

    public static void baseSetup(TestContainer container)
            throws NonUniqueBusinessKeyException {
        DataService dataService = container.get(DataService.class);
        EJBTestBase.createOrganizationRoles(dataService);
        EJBTestBase.createPaymentTypes(dataService);
        SupportedCountries.createSomeSupportedCountries(dataService);
        EJBTestBase.createUserRoles(dataService);
    }

    public static void addCurrencies(TestContainer container)
            throws OrganizationAuthoritiesException, ValidationException {
        OperatorService operatorService = container.get(OperatorService.class);
        operatorService.addCurrency("EUR");
        operatorService.addCurrency("USD");
        operatorService.addCurrency("JPY");
    }

    public static long registerOperatorOrganisation(TestContainer container)
            throws Exception {
        DataService dataService = container.get(DataService.class);
        Organization organization = Organizations
                .createPlatformOperator(dataService);
        adminUser = Organizations.createUserForOrg(dataService, organization,
                true, "AdminPlatformOperator");
        PlatformUsers.grantRoles(dataService, adminUser,
                UserRoleType.PLATFORM_OPERATOR);

        RoleAssignment roleAssignment = new RoleAssignment();
        roleAssignment.setRole(new UserRole(UserRoleType.PLATFORM_OPERATOR));
        adminUser.getAssignedRoles().add(roleAssignment);
        return adminUser.getKey();
    }

    public static long registerSupplierAndTechnologyProvider(
            TestContainer container) throws Exception {

        VOUserDetails userDetails = new VOUserDetails();
        userDetails.setUserId("Supplier_" + System.currentTimeMillis());
        userDetails.setEMail("test@est.fujitsu.de");
        userDetails.setLocale("en");

        VOOrganization organization = new VOOrganization();
        organization.setEmail("info@est.fujitsu.com");
        organization.setAddress("address");
        organization.setLocale("en");
        organization.setName("Supplier and TechProv Organization");
        organization.setPhone("+49 89 000000");
        organization.setUrl("http://www.fujitsu.de");
        organization.setSupportEmail("info@est.fujitsu.com");
        organization.setDomicileCountry("DE");
        organization.setOperatorRevenueShare(BigDecimal.ZERO);

        VOOrganization supplier = container.get(OperatorService.class)
                .registerOrganization(organization, null, userDetails, null,
                        null, OrganizationRoleType.TECHNOLOGY_PROVIDER,
                        OrganizationRoleType.SUPPLIER);
        return supplier.getKey();
    }

    public static long registerCustomerOrganization(TestContainer container,
            VOMarketplace marketplace) throws Exception {

        VOUserDetails userDetails = new VOUserDetails();
        userDetails.setUserId("Customer_" + System.currentTimeMillis());
        userDetails.setEMail("test@est.fujitsu.de");
        userDetails.setLocale("en");

        VOOrganization organization = new VOOrganization();
        organization.setName("Customer Organization");
        organization.setAddress("address");
        organization.setEmail("customer@est.fujitsu.com");
        organization.setDomicileCountry("DE");
        organization.setLocale("en");

        VOOrganization customer = container.get(OperatorService.class)
                .registerOrganization(organization, null, userDetails, null,
                        marketplace.getMarketplaceId());
        return customer.getKey();
    }

    public static VOMarketplace createMarketplace(TestContainer container,
            String name, String ownerId) throws Exception {
        VOMarketplace marketplace = new VOMarketplace();
        marketplace.setName(name);
        marketplace.setOwningOrganizationId(ownerId);
        marketplace.setOpen(true);
        return container.get(MarketplaceService.class)
                .createMarketplace(marketplace);
    }

    public static void importTechnicalService(TestContainer container,
            String technicalService) throws Exception {
        ServiceProvisioningService provisioningService = container
                .get(ServiceProvisioningService.class);
        provisioningService
                .importTechnicalServices(technicalService.getBytes("UTF-8"));
    }

    public static VOService createAndPublishFreeProduct(TestContainer container,
            VOTechnicalService technicalService, VOMarketplace marketplace)
            throws Exception {

        double oneTimeFee = 0D;
        double pricePerPeriod = 0D;
        int freePeriod = 0;
        return createAndPublishMarketableService(container, technicalService,
                marketplace, "product.free" + System.currentTimeMillis(),
                PriceModelType.FREE_OF_CHARGE, PricingPeriod.WEEK, oneTimeFee,
                pricePerPeriod, freePeriod);
    }

    public static VOService createAndPublishProRataProduct(
            TestContainer container, VOTechnicalService technicalService,
            VOMarketplace marketplace, int freePeriod, double oneTimeFee)
            throws Exception {

        double pricePerPeriod = 10D;
        return createAndPublishMarketableService(container, technicalService,
                marketplace, "product.pro_rata" + System.currentTimeMillis(),
                PriceModelType.PRO_RATA, PricingPeriod.WEEK, oneTimeFee,
                pricePerPeriod, freePeriod);
    }

    public static VOService createAndPublishProRataProduct(
            TestContainer container, VOTechnicalService technicalService,
            VOMarketplace marketplace, int freePeriod) throws Exception {

        double oneTimeFee = 100D;
        double pricePerPeriod = 10D;
        return createAndPublishMarketableService(container, technicalService,
                marketplace, "product.pro_rata" + System.currentTimeMillis(),
                PriceModelType.PRO_RATA, PricingPeriod.WEEK, oneTimeFee,
                pricePerPeriod, freePeriod);
    }

    public static VOService createAndPublishProRataProduct(
            TestContainer container, VOTechnicalService technicalService,
            VOMarketplace marketplace) throws Exception {

        double oneTimeFee = 100D;
        double pricePerPeriod = 10D;
        int freePeriod = 0;
        return createAndPublishMarketableService(container, technicalService,
                marketplace, "product.pro_rata" + System.currentTimeMillis(),
                PriceModelType.PRO_RATA, PricingPeriod.WEEK, oneTimeFee,
                pricePerPeriod, freePeriod);
    }

    public static VOService createAndPublishPerUnitWeekProduct(
            TestContainer container, VOTechnicalService technicalService,
            VOMarketplace marketplace) throws Exception {

        double oneTimeFee = 200D;
        double pricePerPeriod = 20D;
        int freePeriod = 0;
        return createAndPublishMarketableService(container, technicalService,
                marketplace,
                "product.per_unit.week" + System.currentTimeMillis(),
                PriceModelType.PER_UNIT, PricingPeriod.WEEK, oneTimeFee,
                pricePerPeriod, freePeriod);
    }

    public static VOService createAndPublishPerUnitWeekProduct(
            TestContainer container, VOTechnicalService technicalService,
            VOMarketplace marketplace, int freePeriod) throws Exception {

        double oneTimeFee = 200D;
        double pricePerPeriod = 20D;
        return createAndPublishMarketableService(container, technicalService,
                marketplace,
                "product.per_unit.week" + System.currentTimeMillis(),
                PriceModelType.PER_UNIT, PricingPeriod.WEEK, oneTimeFee,
                pricePerPeriod, freePeriod);
    }

    public static VOService createAndPublishPerUnitMonthProduct(
            TestContainer container, VOTechnicalService technicalService,
            VOMarketplace marketplace) throws Exception {

        double oneTimeFee = 150D;
        double pricePerPeriod = 50D;
        int freePeriod = 0;
        return createAndPublishMarketableService(container, technicalService,
                marketplace, "product.per_unit.month", PriceModelType.PER_UNIT,
                PricingPeriod.MONTH, oneTimeFee, pricePerPeriod, freePeriod);
    }

    public static VOService createAndPublishPerUnitMonthProduct(
            TestContainer container, VOTechnicalService technicalService,
            VOMarketplace marketplace, int freePeriod) throws Exception {

        double oneTimeFee = 150D;
        double pricePerPeriod = 50D;
        return createAndPublishMarketableService(container, technicalService,
                marketplace,
                "product.per_unit.month" + System.currentTimeMillis(),
                PriceModelType.PER_UNIT, PricingPeriod.MONTH, oneTimeFee,
                pricePerPeriod, freePeriod);
    }

    private static VOService createAndPublishMarketableService(
            TestContainer container, VOTechnicalService technicalService,
            VOMarketplace marketplace, String serviceId,
            PriceModelType priceModelType, PricingPeriod pricingPeriod,
            double oneTimeFee, double pricePerPeriod, int freePeriod)
            throws Exception {

        ServiceProvisioningService provisioningService = container
                .get(ServiceProvisioningService.class);
        MarketplaceService marketplaceService = container
                .get(MarketplaceService.class);

        VOServiceDetails voServiceDetails = new VOServiceDetails();
        voServiceDetails.setServiceId(serviceId);
        VOServiceDetails serviceDetails = provisioningService
                .createService(technicalService, voServiceDetails, null);

        VOPriceModel priceModel = new VOPriceModel();
        priceModel.setType(priceModelType);
        priceModel.setFreePeriod(freePeriod);
        priceModel.setPeriod(pricingPeriod);
        priceModel.setCurrencyISOCode("EUR");
        priceModel.setOneTimeFee(BigDecimal.valueOf(oneTimeFee).setScale(2));
        priceModel.setPricePerPeriod(BigDecimal.valueOf(pricePerPeriod));
        serviceDetails = provisioningService.savePriceModel(serviceDetails,
                priceModel);

        VOCatalogEntry voCE = new VOCatalogEntry();
        voCE.setAnonymousVisible(true);
        voCE.setMarketplace(marketplace);
        marketplaceService.publishService(serviceDetails, Arrays.asList(voCE));

        return serviceDetails;
    }

    public static VOService activateService(TestContainer container,
            VOService service) throws Exception {
        return container.get(ServiceProvisioningService.class)
                .activateService(service);
    }

    public static void defineUpgradePath(TestContainer container,
            VOService... services) throws Exception {
        ServiceProvisioningService provisioningService = container
                .get(ServiceProvisioningService.class);

        List<VOService> allServices = new ArrayList<>();
        allServices.addAll(Arrays.asList(services));

        for (VOService srv : services) {
            provisioningService.setCompatibleServices(
                    getServiceDetails(container, srv),
                    getServices(container, allServices));
        }
    }

    public static List<VOService> getServices(TestContainer container,
            List<VOService> services) throws Exception {
        List<VOService> serviceList = new ArrayList<>();
        for (VOService service : services) {
            serviceList.add(getServiceDetails(container, service));
        }
        return serviceList;
    }

    public static VOServiceDetails getServiceDetails(TestContainer container,
            VOService service) throws Exception {
        ServiceProvisioningService provisioningService = container
                .get(ServiceProvisioningService.class);
        return provisioningService.getServiceDetails(service);
    }

    public static int updateCutoffDay(TestContainer container,
            final int cutOffDay) throws Exception {
        VOOrganization organizationData = container.get(AccountService.class)
                .getOrganizationData();
        AccountServiceManagement accountServiceManagement = container
                .get(AccountServiceManagement.class);
        int oldCutoffDay = accountServiceManagement
                .getCutOffDayOfOrganization();
        accountServiceManagement.setCutOffDayOfOrganization(cutOffDay,
                organizationData);
        return Integer.valueOf(oldCutoffDay);
    }

    public static void savePaymentConfiguration(TestContainer container)
            throws Exception {
        AccountService accountService = container.get(AccountService.class);
        Set<VOPaymentType> defaultPaymentTypes = accountService
                .getDefaultPaymentConfiguration();
        VOPaymentType voPaymentType = getPaymentTypeVO(
                accountService.getAvailablePaymentTypesForOrganization(),
                PaymentInfoType.INVOICE);
        defaultPaymentTypes.add(voPaymentType);
        accountService.savePaymentConfiguration(defaultPaymentTypes, null,
                defaultPaymentTypes, null);
    }

    private static VOPaymentType getPaymentTypeVO(
            Set<VOPaymentType> defaultPaymentTypes,
            PaymentInfoType paymentInfoType) {
        for (VOPaymentType voPaymentType : defaultPaymentTypes) {
            if (voPaymentType.getPaymentTypeId()
                    .equals(paymentInfoType.name())) {
                return voPaymentType;
            }
        }
        return null;
    }

    public static void setBillingRunOffset(TestContainer container,
            final int offsetInDays) throws Exception {
        Long offsetInMs = new Long(offsetInDays * 24 * 3600 * 1000L);
        ConfigurationServiceLocal configurationService = container
                .get(ConfigurationServiceLocal.class);

        ConfigurationSetting config = new ConfigurationSetting(
                ConfigurationKey.TIMER_INTERVAL_BILLING_OFFSET,
                Configuration.GLOBAL_CONTEXT, offsetInMs.toString());
        configurationService.setConfigurationSetting(config);
    }

}
