/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 16.05.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.calculation.revenue;

import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.persistence.Query;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.oscm.encrypter.AESEncrypter;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import org.oscm.accountservice.local.AccountServiceLocal;
import org.oscm.billingservice.business.calculation.revenue.setup.SubscriptionUpgradeSetup;
import org.oscm.billingservice.evaluation.BillingResultEvaluator;
import org.oscm.billingservice.evaluation.BillingXMLNodeSearch;
import org.oscm.billingservice.service.BillingServiceLocal;
import org.oscm.billingservice.setup.TestPaymentSetup;
import org.oscm.converter.XMLConverter;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.BillingResult;
import org.oscm.domobjects.enums.BillingAdapterIdentifier;
import org.oscm.interceptor.DateFactory;
import org.oscm.test.DateTimeHandling;
import org.oscm.test.StaticEJBTestBase;
import org.oscm.test.TestDateFactory;
import org.oscm.test.data.BillingAdapters;
import org.oscm.internal.intf.AccountService;
import org.oscm.internal.intf.ServiceProvisioningService;
import org.oscm.internal.intf.SubscriptionService;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.PriceModelType;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.vo.VOBillingContact;
import org.oscm.internal.vo.VOInstanceInfo;
import org.oscm.internal.vo.VOMarketplace;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.internal.vo.VOPaymentInfo;
import org.oscm.internal.vo.VORoleDefinition;
import org.oscm.internal.vo.VOService;
import org.oscm.internal.vo.VOSubscription;
import org.oscm.internal.vo.VOSubscriptionDetails;
import org.oscm.internal.vo.VOTechnicalService;
import org.oscm.internal.vo.VOUda;
import org.oscm.internal.vo.VOUsageLicense;
import org.oscm.internal.vo.VOUser;

/**
 * @author farmaki
 * 
 */
public class OneTimeFeeAsyncIT extends StaticEJBTestBase {

    private static long operatorUserKey;

    private static String supplierOrgId;
    private static long supplierOrgKey;
    private static long supplierUserKey;
    private static long customerUserKey;
    private static String customerUserId;
    private static VOMarketplace marketplace;
    private static VOTechnicalService technicalServiceAsync;
    private static VOService perUnitWeekService;
    private static VOService perUnitMonthService;
    private static VOService proRataService;
    private static VOService proRataService2;
    private final static int defaultBillingOffset = 0;
    private static TestPaymentSetup payment;

    private final Set<String> subscrIds = new HashSet<String>();

    @BeforeClass
    public static void setupOnce() throws Exception {
        SubscriptionUpgradeSetup.setup(container);
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                dateFactorySetTime("2013-01-01 08:00:00");
                DataService ds = container.get(DataService.class);
                BillingAdapters.createBillingAdapter(ds,
                        BillingAdapterIdentifier.NATIVE_BILLING.toString(),
                        true);

                SubscriptionUpgradeSetup.baseSetup(container);

                operatorUserKey = SubscriptionUpgradeSetup
                        .registerOperatorOrganisation(container);
                container.login(operatorUserKey, ROLE_PLATFORM_OPERATOR);
                SubscriptionUpgradeSetup.addCurrencies(container);

                supplierOrgKey = SubscriptionUpgradeSetup
                        .registerSupplierAndTechnologyProvider(container);
                supplierUserKey = container.get(AccountServiceLocal.class)
                        .getOrganizationAdmins(supplierOrgKey).get(0).getKey();

                container.login(supplierUserKey, ROLE_ORGANIZATION_ADMIN,
                        ROLE_SERVICE_MANAGER, ROLE_TECHNOLOGY_MANAGER);
                supplierOrgId = container.get(AccountService.class)
                        .getOrganizationData().getOrganizationId();

                SubscriptionUpgradeSetup.importTechnicalService(container,
                        TECHNICAL_SERVICES_ASYNC_XML);

                technicalServiceAsync = container
                        .get(ServiceProvisioningService.class)
                        .getTechnicalServices(
                                OrganizationRoleType.TECHNOLOGY_PROVIDER)
                        .get(0);

                container.login(operatorUserKey, ROLE_PLATFORM_OPERATOR);
                marketplace = SubscriptionUpgradeSetup.createMarketplace(
                        container, "m_supplier", supplierOrgId);

                container.login(supplierUserKey, ROLE_ORGANIZATION_ADMIN,
                        ROLE_SERVICE_MANAGER, ROLE_TECHNOLOGY_MANAGER);
                SubscriptionUpgradeSetup.updateCutoffDay(container, 1);
                SubscriptionUpgradeSetup.savePaymentConfiguration(container);

                payment = new TestPaymentSetup(container);
                return null;
            }
        });
    }

    private static void dateFactorySetTime(String timeToSet) {
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.setTime(new Date(DateTimeHandling.calculateMillis(timeToSet)));
        DateFactory.setInstance(new TestDateFactory(cal.getTime()));
    }

    private VOSubscription subscribeToService(
            final String subscriptionNamePrefix, final VOService service,
            final String userId, final long userKey) throws Exception {

        VOSubscription subscription = createSubscriptionVO(subscriptionNamePrefix);

        VORoleDefinition roleDefinition = technicalServiceAsync
                .getRoleDefinitions().get(0);
        List<VOUsageLicense> users = null;
        if (userId.length() != 0) {
            users = createUsageLicenceVO(roleDefinition, userId, userKey);
        }

        AccountService accountService = container.get(AccountService.class);
        SubscriptionService subscriptionService = container
                .get(SubscriptionService.class);

        VOBillingContact billingContact = accountService
                .saveBillingContact(createBillingContactVO());
        VOPaymentInfo paymentInfo = null;
        if (service.getPriceModel().getType() != PriceModelType.FREE_OF_CHARGE) {
            paymentInfo = accountService.getPaymentInfos().get(0);
        } else {
            billingContact = null;
        }

        subscription = subscriptionService.subscribeToService(subscription,
                service, users, paymentInfo, billingContact,
                new ArrayList<VOUda>());

        return subscriptionService.getSubscriptionDetails(subscription
                .getSubscriptionId());
    }

    private VOSubscription completeAsyncSubscription(
            final long supplierAdminKey, final String customerOrgId,
            final long customerAdminKey, final VOSubscription subscription)
            throws Exception {
        container.login(supplierAdminKey,
                UserRoleType.TECHNOLOGY_MANAGER.name());
        VOInstanceInfo instance = getDefaultInstance(
                subscription.getSubscriptionId(),
                subscription.getServiceBaseURL());

        SubscriptionService subscriptionService = container
                .get(SubscriptionService.class);
        subscriptionService.completeAsyncSubscription(
                subscription.getSubscriptionId(), customerOrgId, instance);

        container.login(customerAdminKey,
                UserRoleType.ORGANIZATION_ADMIN.name());
        return subscriptionService.getSubscriptionDetails(subscription
                .getSubscriptionId());
    }

    protected VOInstanceInfo getDefaultInstance(String subscriptionId,
            String baseUrl) {
        VOInstanceInfo instance = new VOInstanceInfo();
        instance.setInstanceId(subscriptionId);
        instance.setBaseUrl(baseUrl);
        instance.setAccessInfo("");
        instance.setLoginPath("/login");
        return instance;
    }

    private VOSubscription loadSubscriptionDetails(final String subscriptionId)
            throws Exception {
        SubscriptionService subscriptionService = container
                .get(SubscriptionService.class);
        return subscriptionService.getSubscriptionDetails(subscriptionId);
    }

    private static VOBillingContact createBillingContactVO() {
        VOBillingContact voBillingContact = new VOBillingContact();
        voBillingContact.setAddress("Customer Road");
        voBillingContact.setCompanyName("Customer Company");
        voBillingContact.setEmail("customer@fujitsu.com");
        voBillingContact.setOrgAddressUsed(true);
        voBillingContact.setId("billingId_" + System.nanoTime());
        return voBillingContact;
    }

    private static List<VOUsageLicense> createUsageLicenceVO(
            VORoleDefinition role, String userId, long userKey) {
        List<VOUsageLicense> users = new ArrayList<VOUsageLicense>();
        users.add(createUsageLicenceVO(userId, role, userKey));
        return users;
    }

    private static VOUsageLicense createUsageLicenceVO(String userId,
            VORoleDefinition role, long userKey) {
        VOUser user = new VOUser();
        user.setUserId(userId);
        user.setKey(userKey);
        VOUsageLicense usageLicence = new VOUsageLicense();
        usageLicence.setUser(user);
        usageLicence.setRoleDefinition(role);
        return usageLicence;
    }

    private static VOSubscription createSubscriptionVO(String namePrefix) {
        VOSubscription subscription = new VOSubscription();
        subscription.setSubscriptionId(namePrefix);
        return subscription;
    }

    private VOSubscriptionDetails getSubscriptionDetails(
            VOSubscription subscription) throws ObjectNotFoundException,
            OperationNotPermittedException {
        return container.get(SubscriptionService.class).getSubscriptionDetails(
                subscription.getSubscriptionId());
    }

    private static void performBillingRun(final int billingOffset,
            final long invocationTime) throws Exception {

        SubscriptionUpgradeSetup.setBillingRunOffset(container, billingOffset);

        BillingServiceLocal billingService = container
                .get(BillingServiceLocal.class);
        billingService.startBillingRun(invocationTime);
    }

    private BillingResultEvaluator billingResultEvaluatorFactory(
            VOSubscription subscription, String billingPeriodStart,
            String billingPeriodEnd) {

        long billingPeriodStart_long = DateTimeHandling
                .calculateMillis(billingPeriodStart);
        long billingPeriodEnd_long = DateTimeHandling
                .calculateMillis(billingPeriodEnd);

        try {
            Document billingResult = getBillingResult(subscription.getKey(),
                    billingPeriodStart_long, billingPeriodEnd_long);
            return new BillingResultEvaluator(billingResult);
        } catch (Exception e) {
            return null;
        }
    }

    private static Document getBillingResult(final long subscriptionKey,
            final long billingPeriodStart, final long billingPeriodEnd)
            throws Exception {
        return runTX(new Callable<Document>() {
            @Override
            public Document call() throws Exception {

                DataService dataService = container.get(DataService.class);
                Query query = dataService
                        .createNamedQuery("BillingResult.findBillingResult");
                query.setParameter("subscriptionKey",
                        Long.valueOf(subscriptionKey));
                query.setParameter("startPeriod",
                        Long.valueOf(billingPeriodStart));
                query.setParameter("endPeriod", Long.valueOf(billingPeriodEnd));
                BillingResult billingResult = (BillingResult) query
                        .getSingleResult();
                Document doc = XMLConverter.convertToDocument(
                        billingResult.getResultXML(), true);
                return doc;
            }
        });
    }

    private VOSubscription upgradeSubscription(
            final VOSubscription subscription, final VOService serviceToUpgrade)
            throws Exception {

        AccountService accountService = container.get(AccountService.class);
        SubscriptionService subscriptionService = container
                .get(SubscriptionService.class);

        VOPaymentInfo paymentInfo = null;
        VOBillingContact billingContact = null;
        if (serviceToUpgrade.getPriceModel().getType() != PriceModelType.FREE_OF_CHARGE) {
            paymentInfo = accountService.getPaymentInfos().get(0);
            billingContact = accountService.getBillingContacts().get(0);
        }

        VOSubscription newSubscription = subscriptionService
                .upgradeSubscription(subscription, serviceToUpgrade,
                        paymentInfo, billingContact, new ArrayList<VOUda>());
        return subscriptionService.getSubscriptionDetails(newSubscription
                .getSubscriptionId());
    }

    private VOSubscription completeAsyncUpgradeSubscription(
            final long supplierAdminKey, final long customerAdminKey,
            final String customerOrgId, final VOSubscription subscription)
            throws Exception {
        container.login(supplierAdminKey,
                UserRoleType.TECHNOLOGY_MANAGER.name());
        VOInstanceInfo instance = getDefaultInstance(
                subscription.getSubscriptionId(),
                subscription.getServiceBaseURL());

        SubscriptionService subscriptionService = container
                .get(SubscriptionService.class);
        subscriptionService.completeAsyncUpgradeSubscription(
                subscription.getSubscriptionId(), customerOrgId, instance);

        container.login(customerAdminKey,
                UserRoleType.ORGANIZATION_ADMIN.name());
        return subscriptionService.getSubscriptionDetails(subscription
                .getSubscriptionId());

    }

    private static void terminateSubscription(final String subscriptionId)
            throws Exception {
        container.get(SubscriptionService.class).unsubscribeFromService(
                subscriptionId);
    }

    private static VOSubscriptionDetails modifySubscription(
            final String subscriptionId, final String pon) throws Exception {
        VOSubscriptionDetails subscription = container.get(
                SubscriptionService.class).getSubscriptionDetails(
                subscriptionId);
        subscription.setPurchaseOrderNumber(pon);
        return container.get(SubscriptionService.class).modifySubscription(
                subscription, null, null);
    }

    private void deletePaymentTypes() throws Exception {
        AccountService accountService = container.get(AccountService.class);
        VOOrganization organizationData = accountService.getOrganizationData();
        container.login(supplierUserKey, ROLE_ORGANIZATION_ADMIN,
                ROLE_SERVICE_MANAGER);
        payment.deleteCustomerPaymentTypes(organizationData);
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
    }

    private void addPaymentTypes() throws Exception {
        AccountService accountService = container.get(AccountService.class);
        VOOrganization organizationData = accountService.getOrganizationData();
        container.login(supplierUserKey, ROLE_ORGANIZATION_ADMIN,
                ROLE_SERVICE_MANAGER);
        payment.reassignCustomerPaymentTypes(organizationData);
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
    }

    private String generateSubscriptionId() {
        String result = String.valueOf(System.currentTimeMillis());
        if (subscrIds.contains(result)) {
            throw new IllegalStateException("Generated subscription id twice");
        }
        subscrIds.add(result);
        return result;
    }

    @Before
    public void setup() throws Exception {
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                container.login(operatorUserKey, ROLE_PLATFORM_OPERATOR);
                long customerOrgKey = SubscriptionUpgradeSetup
                        .registerCustomerOrganization(container, marketplace);

                container.get(DataService.class).flush();
                customerUserKey = container.get(AccountServiceLocal.class)
                        .getOrganizationAdmins(customerOrgKey).get(0).getKey();
                customerUserId = container.get(AccountServiceLocal.class)
                        .getOrganizationAdmins(customerOrgKey).get(0)
                        .getUserId();

                container.login(supplierUserKey, ROLE_ORGANIZATION_ADMIN,
                        ROLE_SERVICE_MANAGER, ROLE_TECHNOLOGY_MANAGER);
                return null;
            }
        });

    }

    /**
     * Per unit price model. Subscription created in overlapping unit BEFORE the
     * billing period start time.
     */
    @Test
    public void overlapping_subStartBeforeBs() throws Exception {
        // given
        dateFactorySetTime("2013-01-01 08:00:00");
        container.login(supplierUserKey, ROLE_ORGANIZATION_ADMIN,
                ROLE_SERVICE_MANAGER, ROLE_TECHNOLOGY_MANAGER);
        perUnitWeekService = SubscriptionUpgradeSetup
                .createAndPublishPerUnitWeekProduct(container,
                        technicalServiceAsync, marketplace);
        perUnitWeekService = SubscriptionUpgradeSetup.activateService(
                container, perUnitWeekService);

        dateFactorySetTime("2013-07-30 13:00:00");
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        VOSubscription subscription = subscribeToService(
                generateSubscriptionId(), perUnitWeekService, customerUserId,
                customerUserKey);

        String customerOrgId = container.get(AccountService.class)
                .getOrganizationData().getOrganizationId();

        // complete async subscription
        dateFactorySetTime("2013-07-30 13:00:00");
        subscription = completeAsyncSubscription(supplierUserKey,
                customerOrgId, customerUserKey, subscription);

        long priceModelKey = getSubscriptionDetails(subscription)
                .getPriceModel().getKey();

        // when
        dateFactorySetTime("2013-09-10 00:00:00");
        performBillingRun(defaultBillingOffset,
                DateTimeHandling.calculateMillis("2013-09-10 00:00:00"));

        // then
        BillingResultEvaluator eva = billingResultEvaluatorFactory(
                subscription, "2013-07-01 00:00:00", "2013-08-01 00:00:00");
        eva.assertOneTimeFee(priceModelKey, "200.00");

        eva = billingResultEvaluatorFactory(subscription,
                "2013-08-01 00:00:00", "2013-09-01 00:00:00");
        eva.assertNullOneTimeFee();
    }

    /**
     * Per unit price model. Subscription created in overlapping unit before the
     * billing period start time. Free period ends in overlapping unit BEFORE
     * billing period start.
     */
    @Test
    public void overlapping_subStartBeforeBs_freePeriodEndsBeforeBs()
            throws Exception {
        // given
        final int freePeriod = 1;
        dateFactorySetTime("2013-01-01 08:00:00");
        container.login(supplierUserKey, ROLE_ORGANIZATION_ADMIN,
                ROLE_SERVICE_MANAGER, ROLE_TECHNOLOGY_MANAGER);
        perUnitWeekService = SubscriptionUpgradeSetup
                .createAndPublishPerUnitWeekProduct(container,
                        technicalServiceAsync, marketplace, freePeriod);
        perUnitWeekService = SubscriptionUpgradeSetup.activateService(
                container, perUnitWeekService);

        dateFactorySetTime("2013-07-30 13:00:00");
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        VOSubscription subscription = subscribeToService(
                generateSubscriptionId(), perUnitWeekService, customerUserId,
                customerUserKey);

        String customerOrgId = container.get(AccountService.class)
                .getOrganizationData().getOrganizationId();

        // complete async subscription
        dateFactorySetTime("2013-07-30 13:00:00");
        subscription = completeAsyncSubscription(supplierUserKey,
                customerOrgId, customerUserKey, subscription);

        long priceModelKey = getSubscriptionDetails(subscription)
                .getPriceModel().getKey();

        // when
        dateFactorySetTime("2013-09-10 00:00:00");
        performBillingRun(defaultBillingOffset,
                DateTimeHandling.calculateMillis("2013-09-10 00:00:00"));

        // then
        BillingResultEvaluator eva = billingResultEvaluatorFactory(
                subscription, "2013-07-01 00:00:00", "2013-08-01 00:00:00");
        eva.assertOneTimeFee(priceModelKey, "200.00");

        eva = billingResultEvaluatorFactory(subscription,
                "2013-08-01 00:00:00", "2013-09-01 00:00:00");
        eva.assertNullOneTimeFee();
    }

    /**
     * Per unit price model. Subscription created in overlapping unit before the
     * billing period start time. Free period ends in overlapping unit AFTER
     * billing period start.
     */
    @Test
    public void overlapping_subStartBeforeBs_freePeriodEndsAfterBs()
            throws Exception {
        // given
        final int freePeriod = 3;
        dateFactorySetTime("2013-01-01 08:00:00");
        container.login(supplierUserKey, ROLE_ORGANIZATION_ADMIN,
                ROLE_SERVICE_MANAGER, ROLE_TECHNOLOGY_MANAGER);
        perUnitWeekService = SubscriptionUpgradeSetup
                .createAndPublishPerUnitWeekProduct(container,
                        technicalServiceAsync, marketplace, freePeriod);
        perUnitWeekService = SubscriptionUpgradeSetup.activateService(
                container, perUnitWeekService);

        dateFactorySetTime("2013-07-30 13:00:00");
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        VOSubscription subscription = subscribeToService(
                generateSubscriptionId(), perUnitWeekService, customerUserId,
                customerUserKey);

        String customerOrgId = container.get(AccountService.class)
                .getOrganizationData().getOrganizationId();

        // complete async subscription
        dateFactorySetTime("2013-07-30 13:00:00");
        subscription = completeAsyncSubscription(supplierUserKey,
                customerOrgId, customerUserKey, subscription);

        long priceModelKey = getSubscriptionDetails(subscription)
                .getPriceModel().getKey();

        // when
        dateFactorySetTime("2013-09-10 00:00:00");
        performBillingRun(defaultBillingOffset,
                DateTimeHandling.calculateMillis("2013-09-10 00:00:00"));

        // then
        BillingResultEvaluator eva = billingResultEvaluatorFactory(
                subscription, "2013-07-01 00:00:00", "2013-08-01 00:00:00");
        assertNull("No result expected", eva);

        eva = billingResultEvaluatorFactory(subscription,
                "2013-08-01 00:00:00", "2013-09-01 00:00:00");
        eva.assertOneTimeFee(priceModelKey, "200.00");
    }

    @Test
    public void per_unit_not_overlapping() throws Exception {
        // given
        final int freePeriod = 0;
        dateFactorySetTime("2013-01-01 08:00:00");
        container.login(supplierUserKey, ROLE_ORGANIZATION_ADMIN,
                ROLE_SERVICE_MANAGER, ROLE_TECHNOLOGY_MANAGER);
        perUnitWeekService = SubscriptionUpgradeSetup
                .createAndPublishPerUnitWeekProduct(container,
                        technicalServiceAsync, marketplace, freePeriod);
        perUnitWeekService = SubscriptionUpgradeSetup.activateService(
                container, perUnitWeekService);

        dateFactorySetTime("2013-07-08 13:00:00");
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        VOSubscription subscription = subscribeToService(
                generateSubscriptionId(), perUnitWeekService, customerUserId,
                customerUserKey);

        String customerOrgId = container.get(AccountService.class)
                .getOrganizationData().getOrganizationId();

        // complete async subscription
        dateFactorySetTime("2013-07-08 13:00:00");
        subscription = completeAsyncSubscription(supplierUserKey,
                customerOrgId, customerUserKey, subscription);

        long priceModelKey = getSubscriptionDetails(subscription)
                .getPriceModel().getKey();

        // when
        dateFactorySetTime("2013-09-10 00:00:00");
        performBillingRun(defaultBillingOffset,
                DateTimeHandling.calculateMillis("2013-09-10 00:00:00"));

        // then
        BillingResultEvaluator eva = billingResultEvaluatorFactory(
                subscription, "2013-07-01 00:00:00", "2013-08-01 00:00:00");
        eva.assertOneTimeFee(priceModelKey, "200.00");

        eva = billingResultEvaluatorFactory(subscription,
                "2013-08-01 00:00:00", "2013-09-01 00:00:00");
        eva.assertNullOneTimeFee();
    }

    /**
     * Per unit price model. Subscription created in overlapping unit AFTER the
     * billing period start time.
     */
    @Test
    public void overlapping_subStartAfterBs() throws Exception {
        // given
        final int freePeriod = 0;
        dateFactorySetTime("2013-01-01 08:00:00");
        container.login(supplierUserKey, ROLE_ORGANIZATION_ADMIN,
                ROLE_SERVICE_MANAGER, ROLE_TECHNOLOGY_MANAGER);
        perUnitWeekService = SubscriptionUpgradeSetup
                .createAndPublishPerUnitWeekProduct(container,
                        technicalServiceAsync, marketplace, freePeriod);
        perUnitWeekService = SubscriptionUpgradeSetup.activateService(
                container, perUnitWeekService);

        dateFactorySetTime("2013-08-02 13:00:00");
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        VOSubscription subscription = subscribeToService(
                generateSubscriptionId(), perUnitWeekService, customerUserId,
                customerUserKey);

        String customerOrgId = container.get(AccountService.class)
                .getOrganizationData().getOrganizationId();

        // complete async subscription
        dateFactorySetTime("2013-08-02 13:00:00");
        subscription = completeAsyncSubscription(supplierUserKey,
                customerOrgId, customerUserKey, subscription);

        long priceModelKey = getSubscriptionDetails(subscription)
                .getPriceModel().getKey();

        // when
        dateFactorySetTime("2013-09-10 00:00:00");
        performBillingRun(defaultBillingOffset,
                DateTimeHandling.calculateMillis("2013-09-10 00:00:00"));

        // then
        BillingResultEvaluator eva = billingResultEvaluatorFactory(
                subscription, "2013-08-01 00:00:00", "2013-09-01 00:00:00");
        eva.assertOneTimeFee(priceModelKey, "200.00");
    }

    /**
     * Per unit price model. Subscription created in overlapping unit before the
     * billing period start time. Also an upgraded before bs was done.
     */
    @Test
    public void overlapping_subStartBeforeBs_upgradeBeforeBs() throws Exception {
        // given
        final int freePeriod = 0;
        dateFactorySetTime("2013-01-01 08:00:00");
        container.login(supplierUserKey, ROLE_ORGANIZATION_ADMIN,
                ROLE_SERVICE_MANAGER, ROLE_TECHNOLOGY_MANAGER);
        perUnitWeekService = SubscriptionUpgradeSetup
                .createAndPublishPerUnitWeekProduct(container,
                        technicalServiceAsync, marketplace, freePeriod);
        perUnitMonthService = SubscriptionUpgradeSetup
                .createAndPublishPerUnitMonthProduct(container,
                        technicalServiceAsync, marketplace, freePeriod);
        SubscriptionUpgradeSetup.defineUpgradePath(container,
                perUnitWeekService, perUnitMonthService);
        perUnitWeekService = SubscriptionUpgradeSetup.activateService(
                container, perUnitWeekService);
        perUnitMonthService = SubscriptionUpgradeSetup.activateService(
                container, perUnitMonthService);

        dateFactorySetTime("2013-07-30 13:00:00");
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        VOSubscription subscription = subscribeToService(
                generateSubscriptionId(), perUnitWeekService, customerUserId,
                customerUserKey);

        String customerOrgId = container.get(AccountService.class)
                .getOrganizationData().getOrganizationId();

        // complete async subscription
        dateFactorySetTime("2013-07-30 13:00:00");
        subscription = completeAsyncSubscription(supplierUserKey,
                customerOrgId, customerUserKey, subscription);

        long priceModelKey1 = getSubscriptionDetails(subscription)
                .getPriceModel().getKey();

        dateFactorySetTime("2013-07-31 13:00:00");
        subscription = upgradeSubscription(subscription, perUnitMonthService);

        dateFactorySetTime("2013-07-31 13:00:00");
        subscription = completeAsyncUpgradeSubscription(supplierUserKey,
                customerUserKey, customerOrgId, subscription);

        long priceModelKey2 = getSubscriptionDetails(subscription)
                .getPriceModel().getKey();

        // when
        dateFactorySetTime("2013-09-10 00:00:00");
        performBillingRun(defaultBillingOffset,
                DateTimeHandling.calculateMillis("2013-09-10 00:00:00"));

        // then
        BillingResultEvaluator eva = billingResultEvaluatorFactory(
                subscription, "2013-07-01 00:00:00", "2013-08-01 00:00:00");
        eva.assertOneTimeFee(priceModelKey1, "200.00");

        eva = billingResultEvaluatorFactory(subscription,
                "2013-07-01 00:00:00", "2013-08-01 00:00:00");
        eva.assertOneTimeFee(priceModelKey2, "150.00");

        eva = billingResultEvaluatorFactory(subscription,
                "2013-08-01 00:00:00", "2013-09-01 00:00:00");
        eva.assertNullOneTimeFee();
    }

    /**
     * Per unit price model. Subscription created in overlapping unit before the
     * billing period start time. An upgraded after bs was done.
     */

    @Test
    public void overlapping_subStartBeforeBs_upgradeAfterBs() throws Exception {
        // given
        final int freePeriod = 0;

        dateFactorySetTime("2013-01-01 08:00:00");
        container.login(supplierUserKey, ROLE_ORGANIZATION_ADMIN,
                ROLE_SERVICE_MANAGER, ROLE_TECHNOLOGY_MANAGER);
        perUnitWeekService = SubscriptionUpgradeSetup
                .createAndPublishPerUnitWeekProduct(container,
                        technicalServiceAsync, marketplace, freePeriod);
        perUnitMonthService = SubscriptionUpgradeSetup
                .createAndPublishPerUnitMonthProduct(container,
                        technicalServiceAsync, marketplace, freePeriod);
        SubscriptionUpgradeSetup.defineUpgradePath(container,
                perUnitWeekService, perUnitMonthService);

        perUnitWeekService = container.get(ServiceProvisioningService.class)
                .activateService(perUnitWeekService);
        perUnitMonthService = container.get(ServiceProvisioningService.class)
                .activateService(perUnitMonthService);

        dateFactorySetTime("2013-07-29 13:00:00");
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        VOSubscription subscription = subscribeToService(
                generateSubscriptionId(), perUnitWeekService, customerUserId,
                customerUserKey);

        // complete subscribe
        String customerOrgId = container.get(AccountService.class)
                .getOrganizationData().getOrganizationId();
        dateFactorySetTime("2013-07-30 13:00:00");
        subscription = completeAsyncSubscription(supplierUserKey,
                customerOrgId, customerUserKey, subscription);

        long priceModelKey1 = getSubscriptionDetails(subscription)
                .getPriceModel().getKey();

        dateFactorySetTime("2013-08-02 13:00:00");
        subscription = upgradeSubscription(subscription, perUnitMonthService);

        // complete upgrade1
        dateFactorySetTime("2013-08-03 13:00:00");
        subscription = completeAsyncUpgradeSubscription(supplierUserKey,
                customerUserKey, customerOrgId, subscription);
        long priceModelKey2 = getSubscriptionDetails(subscription)
                .getPriceModel().getKey();

        // when
        dateFactorySetTime("2013-09-10 00:00:00");
        performBillingRun(defaultBillingOffset,
                DateTimeHandling.calculateMillis("2013-09-10 00:00:00"));

        // then
        BillingResultEvaluator eva = billingResultEvaluatorFactory(
                subscription, "2013-07-01 00:00:00", "2013-08-01 00:00:00");
        eva.assertOneTimeFee(priceModelKey1, "200.00");

        eva = billingResultEvaluatorFactory(subscription,
                "2013-08-01 00:00:00", "2013-09-01 00:00:00");
        eva.assertNullOneTimeFee(priceModelKey1);
        eva.assertOneTimeFee(priceModelKey2, "150.00");
    }

    @Test
    public void overlapping_subStartBeforeBs_freePeriodsEndsAfterBs_upgradeAfterFreePeriod()
            throws Exception {
        // GIVEN
        final int freePeriod = 3;

        dateFactorySetTime("2013-01-01 08:00:00");
        container.login(supplierUserKey, ROLE_ORGANIZATION_ADMIN,
                ROLE_SERVICE_MANAGER, ROLE_TECHNOLOGY_MANAGER);
        perUnitWeekService = SubscriptionUpgradeSetup
                .createAndPublishPerUnitWeekProduct(container,
                        technicalServiceAsync, marketplace, freePeriod);
        perUnitMonthService = SubscriptionUpgradeSetup
                .createAndPublishPerUnitMonthProduct(container,
                        technicalServiceAsync, marketplace, freePeriod);
        SubscriptionUpgradeSetup.defineUpgradePath(container,
                perUnitWeekService, perUnitMonthService);

        perUnitWeekService = container.get(ServiceProvisioningService.class)
                .activateService(perUnitWeekService);
        perUnitMonthService = container.get(ServiceProvisioningService.class)
                .activateService(perUnitMonthService);

        dateFactorySetTime("2013-07-30 10:00:00");
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        VOSubscription subscription = subscribeToService(
                generateSubscriptionId(), perUnitWeekService, customerUserId,
                customerUserKey);

        // complete subscribe
        String customerOrgId = container.get(AccountService.class)
                .getOrganizationData().getOrganizationId();
        dateFactorySetTime("2013-07-30 13:00:00");
        subscription = completeAsyncSubscription(supplierUserKey,
                customerOrgId, customerUserKey, subscription);
        long priceModelKey1 = getSubscriptionDetails(subscription)
                .getPriceModel().getKey();

        dateFactorySetTime("2013-08-02 20:00:00");
        subscription = upgradeSubscription(subscription, perUnitMonthService);

        // complete upgrade1
        dateFactorySetTime("2013-08-03 20:00:00");
        subscription = completeAsyncUpgradeSubscription(supplierUserKey,
                customerUserKey, customerOrgId, subscription);
        long priceModelKey2 = getSubscriptionDetails(subscription)
                .getPriceModel().getKey();

        // WHEN
        dateFactorySetTime("2013-09-10 00:00:00");
        performBillingRun(defaultBillingOffset,
                DateTimeHandling.calculateMillis("2013-09-10 00:00:00"));

        // THEN

        BillingResultEvaluator eva = billingResultEvaluatorFactory(
                subscription, "2013-08-01 00:00:00", "2013-09-01 00:00:00");
        eva.assertOneTimeFee(priceModelKey1, "200.00");
        eva.assertOneTimeFee(priceModelKey2, "150.00");
    }

    /**
     * overlap, upgrade in free period before bs
     */

    @Test
    public void overlapping_subStartBeforeBs_freePeriodEndsBeforeBS_upgradeInFreePeriod()
            throws Exception {
        // GIVEN

        dateFactorySetTime("2013-01-01 08:00:00");
        container.login(supplierUserKey, ROLE_ORGANIZATION_ADMIN,
                ROLE_SERVICE_MANAGER, ROLE_TECHNOLOGY_MANAGER);
        perUnitWeekService = SubscriptionUpgradeSetup
                .createAndPublishPerUnitWeekProduct(container,
                        technicalServiceAsync, marketplace, 2);
        perUnitMonthService = SubscriptionUpgradeSetup
                .createAndPublishPerUnitMonthProduct(container,
                        technicalServiceAsync, marketplace, 0);
        SubscriptionUpgradeSetup.defineUpgradePath(container,
                perUnitWeekService, perUnitMonthService);

        perUnitWeekService = container.get(ServiceProvisioningService.class)
                .activateService(perUnitWeekService);
        perUnitMonthService = container.get(ServiceProvisioningService.class)
                .activateService(perUnitMonthService);

        dateFactorySetTime("2013-07-28 13:00:00");
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        VOSubscription subscription = subscribeToService(
                generateSubscriptionId(), perUnitWeekService, customerUserId,
                customerUserKey);

        // complete subscribe
        String customerOrgId = container.get(AccountService.class)
                .getOrganizationData().getOrganizationId();
        dateFactorySetTime("2013-07-29 13:00:00");
        subscription = completeAsyncSubscription(supplierUserKey,
                customerOrgId, customerUserKey, subscription);
        long priceModelKey1 = getSubscriptionDetails(subscription)
                .getPriceModel().getKey();

        dateFactorySetTime("2013-07-30 08:00:00");
        subscription = upgradeSubscription(subscription, perUnitMonthService);
        // complete upgrade1
        dateFactorySetTime("2013-07-30 14:00:00");
        subscription = completeAsyncUpgradeSubscription(supplierUserKey,
                customerUserKey, customerOrgId, subscription);
        long priceModelKey2 = getSubscriptionDetails(subscription)
                .getPriceModel().getKey();

        // WHEN
        dateFactorySetTime("2013-09-10 00:00:00");
        performBillingRun(defaultBillingOffset,
                DateTimeHandling.calculateMillis("2013-09-10 00:00:00"));

        // THEN
        BillingResultEvaluator eva = billingResultEvaluatorFactory(
                subscription, "2013-07-01 00:00:00", "2013-08-01 00:00:00");
        eva.assertNullOneTimeFee(priceModelKey1);
        eva.assertOneTimeFee(priceModelKey2, "150.00");

        eva = billingResultEvaluatorFactory(subscription,
                "2013-08-01 00:00:00", "2013-09-01 00:00:00");
        eva.assertNullOneTimeFee(priceModelKey1);
        eva.assertNullOneTimeFee(priceModelKey2);
    }

    /**
     * overlap, upgrade in free period after bs
     */

    @Test
    public void overlapping_subStartBeforeBs_freePeriodEndsAfterBS_upgradeInFreePeriodAfterBs()
            throws Exception {
        // GIVEN

        dateFactorySetTime("2013-01-01 08:00:00");
        container.login(supplierUserKey, ROLE_ORGANIZATION_ADMIN,
                ROLE_SERVICE_MANAGER, ROLE_TECHNOLOGY_MANAGER);
        perUnitWeekService = SubscriptionUpgradeSetup
                .createAndPublishPerUnitWeekProduct(container,
                        technicalServiceAsync, marketplace, 5);
        perUnitMonthService = SubscriptionUpgradeSetup
                .createAndPublishPerUnitMonthProduct(container,
                        technicalServiceAsync, marketplace, 0);
        SubscriptionUpgradeSetup.defineUpgradePath(container,
                perUnitWeekService, perUnitMonthService);

        perUnitWeekService = container.get(ServiceProvisioningService.class)
                .activateService(perUnitWeekService);
        perUnitMonthService = container.get(ServiceProvisioningService.class)
                .activateService(perUnitMonthService);

        dateFactorySetTime("2013-07-28 13:00:00");
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        VOSubscription subscription = subscribeToService(
                generateSubscriptionId(), perUnitWeekService, customerUserId,
                customerUserKey);
        // complete subscribe
        String customerOrgId = container.get(AccountService.class)
                .getOrganizationData().getOrganizationId();
        dateFactorySetTime("2013-07-29 13:00:00");
        subscription = completeAsyncSubscription(supplierUserKey,
                customerOrgId, customerUserKey, subscription);

        long priceModelKey1 = getSubscriptionDetails(subscription)
                .getPriceModel().getKey();

        dateFactorySetTime("2013-08-01 13:00:00");
        subscription = upgradeSubscription(subscription, perUnitMonthService);
        // complete upgrade1
        dateFactorySetTime("2013-08-01 14:00:00");
        subscription = completeAsyncUpgradeSubscription(supplierUserKey,
                customerUserKey, customerOrgId, subscription);

        long priceModelKey2 = getSubscriptionDetails(subscription)
                .getPriceModel().getKey();

        // WHEN
        dateFactorySetTime("2013-09-10 00:00:00");
        performBillingRun(defaultBillingOffset,
                DateTimeHandling.calculateMillis("2013-09-10 00:00:00"));

        // THEN
        BillingResultEvaluator eva = billingResultEvaluatorFactory(
                subscription, "2013-07-01 00:00:00", "2013-08-01 00:00:00");
        assertNull("No result expected", eva);

        eva = billingResultEvaluatorFactory(subscription,
                "2013-08-01 00:00:00", "2013-09-01 00:00:00");
        eva.assertNullPriceModel(priceModelKey1);
        eva.assertOneTimeFee(priceModelKey2, "150.00");
    }

    /**
     * overlap, upgrade in free period before bs, upgrade price model has also a
     * free period which reaches into the next billing period
     */

    @Test
    public void overlapping_subStartBeforeBs_freePeriodEndsBeforeBS_upgradeInFreePeriodWithOwnFreePeriod()
            throws Exception {
        // GIVEN

        dateFactorySetTime("2013-01-01 08:00:00");
        container.login(supplierUserKey, ROLE_ORGANIZATION_ADMIN,
                ROLE_SERVICE_MANAGER, ROLE_TECHNOLOGY_MANAGER);
        perUnitWeekService = SubscriptionUpgradeSetup
                .createAndPublishPerUnitWeekProduct(container,
                        technicalServiceAsync, marketplace, 2);
        perUnitMonthService = SubscriptionUpgradeSetup
                .createAndPublishPerUnitMonthProduct(container,
                        technicalServiceAsync, marketplace, 3);
        SubscriptionUpgradeSetup.defineUpgradePath(container,
                perUnitWeekService, perUnitMonthService);

        perUnitWeekService = container.get(ServiceProvisioningService.class)
                .activateService(perUnitWeekService);
        perUnitMonthService = container.get(ServiceProvisioningService.class)
                .activateService(perUnitMonthService);

        dateFactorySetTime("2013-06-29 13:00:00");
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        VOSubscription subscription = subscribeToService(
                generateSubscriptionId(), perUnitWeekService, customerUserId,
                customerUserKey);

        // complete subscribe
        String customerOrgId = container.get(AccountService.class)
                .getOrganizationData().getOrganizationId();
        dateFactorySetTime("2013-07-29 13:00:00");
        subscription = completeAsyncSubscription(supplierUserKey,
                customerOrgId, customerUserKey, subscription);

        long priceModelKey1 = getSubscriptionDetails(subscription)
                .getPriceModel().getKey();

        dateFactorySetTime("2013-07-30 10:00:00");
        subscription = upgradeSubscription(subscription, perUnitMonthService);

        // complete upgrade1
        dateFactorySetTime("2013-07-30 12:00:00");
        subscription = completeAsyncUpgradeSubscription(supplierUserKey,
                customerUserKey, customerOrgId, subscription);
        long priceModelKey2 = getSubscriptionDetails(subscription)
                .getPriceModel().getKey();

        // WHEN
        dateFactorySetTime("2013-09-10 00:00:00");
        performBillingRun(defaultBillingOffset,
                DateTimeHandling.calculateMillis("2013-09-10 00:00:00"));

        // THEN
        BillingResultEvaluator eva = billingResultEvaluatorFactory(
                subscription, "2013-07-01 00:00:00", "2013-08-01 00:00:00");
        assertNull("No result expected", eva);

        eva = billingResultEvaluatorFactory(subscription,
                "2013-08-01 00:00:00", "2013-09-01 00:00:00");
        eva.assertNullPriceModel(priceModelKey1);
        eva.assertOneTimeFee(priceModelKey2, "150.00");
    }

    /**
     * overlapping_subStartBeforeBs_freePeriodEndsBeforeBS_terminate
     */

    @Test
    public void overlapping_subStartBeforeBs_freePeriodEndsBeforeBS_terminate()
            throws Exception {
        // GIVEN
        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                dateFactorySetTime("2013-01-01 08:00:00");
                container.login(supplierUserKey, ROLE_ORGANIZATION_ADMIN,
                        ROLE_SERVICE_MANAGER, ROLE_TECHNOLOGY_MANAGER);
                perUnitWeekService = SubscriptionUpgradeSetup
                        .createAndPublishPerUnitWeekProduct(container,
                                technicalServiceAsync, marketplace, 2);
                container.get(DataService.class).flush();

                perUnitWeekService = container.get(
                        ServiceProvisioningService.class).activateService(
                        perUnitWeekService);
                container.logout();
                return null;
            }
        });

        dateFactorySetTime("2013-07-29 13:00:00");
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        VOSubscription subscription = subscribeToService(
                generateSubscriptionId(), perUnitWeekService, customerUserId,
                customerUserKey);

        dateFactorySetTime("2013-07-30 12:00:00");
        terminateSubscription(subscription.getSubscriptionId());

        // WHEN
        dateFactorySetTime("2013-09-10 00:00:00");
        performBillingRun(defaultBillingOffset,
                DateTimeHandling.calculateMillis("2013-09-10 00:00:00"));

        // THEN

        BillingResultEvaluator eva = billingResultEvaluatorFactory(
                subscription, "2013-08-01 00:00:00", "2013-09-01 00:00:00");
        assertNull("No result expected", eva);
    }

    @Test
    public void pro_rata_terminateInFreePeriod() throws Exception {
        // GIVEN
        dateFactorySetTime("2013-01-01 08:00:00");
        container.login(supplierUserKey, ROLE_ORGANIZATION_ADMIN,
                ROLE_SERVICE_MANAGER, ROLE_TECHNOLOGY_MANAGER);
        proRataService = SubscriptionUpgradeSetup
                .createAndPublishProRataProduct(container,
                        technicalServiceAsync, marketplace, 2);
        proRataService = container.get(ServiceProvisioningService.class)
                .activateService(proRataService);

        dateFactorySetTime("2013-07-29 11:00:00");
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        VOSubscription subscription = subscribeToService(
                generateSubscriptionId(), proRataService, customerUserId,
                customerUserKey);

        // complete subscribe
        String customerOrgId = container.get(AccountService.class)
                .getOrganizationData().getOrganizationId();
        dateFactorySetTime("2013-07-29 13:00:00");
        subscription = completeAsyncSubscription(supplierUserKey,
                customerOrgId, customerUserKey, subscription);

        dateFactorySetTime("2013-07-30 12:00:00");
        terminateSubscription(subscription.getSubscriptionId());

        // WHEN
        dateFactorySetTime("2013-09-10 00:00:00");
        performBillingRun(defaultBillingOffset,
                DateTimeHandling.calculateMillis("2013-09-10 00:00:00"));

        // THEN
        BillingResultEvaluator eva = billingResultEvaluatorFactory(
                subscription, "2013-07-01 00:00:00", "2013-08-01 00:00:00");
        assertNull("no result should exist", eva);
    }

    @Test
    public void pro_rata() throws Exception {
        // GIVEN

        dateFactorySetTime("2013-01-01 08:00:00");
        container.login(supplierUserKey, ROLE_ORGANIZATION_ADMIN,
                ROLE_SERVICE_MANAGER, ROLE_TECHNOLOGY_MANAGER);
        proRataService = SubscriptionUpgradeSetup
                .createAndPublishProRataProduct(container,
                        technicalServiceAsync, marketplace, 2);

        proRataService = container.get(ServiceProvisioningService.class)
                .activateService(proRataService);

        dateFactorySetTime("2013-07-28 13:00:00");
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        VOSubscription subscription = subscribeToService(
                generateSubscriptionId(), proRataService, customerUserId,
                customerUserKey);

        // complete subscribe
        String customerOrgId = container.get(AccountService.class)
                .getOrganizationData().getOrganizationId();
        dateFactorySetTime("2013-07-29 13:00:00");
        subscription = completeAsyncSubscription(supplierUserKey,
                customerOrgId, customerUserKey, subscription);
        long priceModelKey1 = getSubscriptionDetails(subscription)
                .getPriceModel().getKey();

        // WHEN
        dateFactorySetTime("2013-09-10 00:00:00");
        performBillingRun(defaultBillingOffset,
                DateTimeHandling.calculateMillis("2013-09-10 00:00:00"));

        // THEN
        BillingResultEvaluator eva = billingResultEvaluatorFactory(
                subscription, "2013-07-01 00:00:00", "2013-08-01 00:00:00");
        eva.assertOneTimeFee(priceModelKey1, "100.00");

        eva = billingResultEvaluatorFactory(subscription,
                "2013-08-01 00:00:00", "2013-09-01 00:00:00");
        eva.assertNullOneTimeFee(priceModelKey1);
    }

    @Test
    public void multipleUpgrades() throws Exception {
        // GIVEN
        dateFactorySetTime("2013-01-01 08:00:00");
        container.login(supplierUserKey, ROLE_ORGANIZATION_ADMIN,
                ROLE_SERVICE_MANAGER, ROLE_TECHNOLOGY_MANAGER);
        proRataService = SubscriptionUpgradeSetup
                .createAndPublishProRataProduct(container,
                        technicalServiceAsync, marketplace, 2);
        perUnitWeekService = SubscriptionUpgradeSetup
                .createAndPublishPerUnitWeekProduct(container,
                        technicalServiceAsync, marketplace, 2);
        perUnitMonthService = SubscriptionUpgradeSetup
                .createAndPublishPerUnitMonthProduct(container,
                        technicalServiceAsync, marketplace, 2);
        SubscriptionUpgradeSetup.defineUpgradePath(container,
                perUnitWeekService, perUnitMonthService, proRataService);
        proRataService = SubscriptionUpgradeSetup.activateService(container,
                proRataService);
        perUnitWeekService = SubscriptionUpgradeSetup.activateService(
                container, perUnitWeekService);
        perUnitMonthService = SubscriptionUpgradeSetup.activateService(
                container, perUnitMonthService);

        // initial subscribe
        dateFactorySetTime("2013-06-30 13:00:00");
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);

        VOSubscription subscription = subscribeToService(
                generateSubscriptionId(), proRataService, customerUserId,
                customerUserKey);

        String customerOrgId = container.get(AccountService.class)
                .getOrganizationData().getOrganizationId();

        dateFactorySetTime("2013-07-01 13:00:00");
        subscription = completeAsyncSubscription(supplierUserKey,
                customerOrgId, customerUserKey, subscription);

        long priceModelKey1 = getSubscriptionDetails(subscription)
                .getPriceModel().getKey();

        // // upgrade 1
        dateFactorySetTime("2013-07-03 13:00:00");
        subscription = upgradeSubscription(subscription, perUnitWeekService);

        // complete upgrade1
        dateFactorySetTime("2013-07-05 13:00:00");
        subscription = completeAsyncUpgradeSubscription(supplierUserKey,
                customerUserKey, customerOrgId, subscription);

        long priceModelKey2 = getSubscriptionDetails(subscription)
                .getPriceModel().getKey();

        // upgrade 2
        dateFactorySetTime("2013-07-04 13:00:00");
        subscription = upgradeSubscription(subscription, perUnitMonthService);

        // complete upgrade 2
        dateFactorySetTime("2013-07-06 13:00:00");
        subscription = completeAsyncUpgradeSubscription(supplierUserKey,
                customerUserKey, customerOrgId, subscription);

        long priceModelKey3 = getSubscriptionDetails(subscription)
                .getPriceModel().getKey();

        // the purchase order number triggers provisioning now - avoid this
        // command, has nothing to do with the test
        // subscription = modifySubscription(subscription.getSubscriptionId(),
        // "new pon");

        // upgrade 3
        dateFactorySetTime("2013-07-27 13:00:00");
        subscription = upgradeSubscription(subscription, perUnitWeekService);

        // complete upgrade 3
        dateFactorySetTime("2013-07-29 13:00:00");
        subscription = completeAsyncUpgradeSubscription(supplierUserKey,
                customerUserKey, customerOrgId, subscription);

        long priceModelKey4 = getSubscriptionDetails(subscription)
                .getPriceModel().getKey();

        // WHEN
        dateFactorySetTime("2013-09-10 00:00:00");
        performBillingRun(defaultBillingOffset,
                DateTimeHandling.calculateMillis("2013-09-10 00:00:00"));

        // THEN
        BillingResultEvaluator eva = billingResultEvaluatorFactory(
                subscription, "2013-07-01 00:00:00", "2013-08-01 00:00:00");
        eva.assertOneTimeFee(priceModelKey1, "100.00");
        eva.assertNullOneTimeFee(priceModelKey2);
        eva.assertOneTimeFee(priceModelKey3, "150.00");
        eva.assertOneTimeFee(priceModelKey4, "200.00");

        eva = billingResultEvaluatorFactory(subscription,
                "2013-08-01 00:00:00", "2013-09-01 00:00:00");
        eva.assertNullPriceModel(priceModelKey1);
        eva.assertNullPriceModel(priceModelKey2);
        eva.assertNullPriceModel(priceModelKey3);
        eva.assertNullOneTimeFee(priceModelKey4);
    }

    @Test
    public void prorata_upgradeInFreePeriod_afterBE() throws Exception {
        // GIVEN
        dateFactorySetTime("2013-01-01 08:00:00");
        container.login(supplierUserKey, ROLE_ORGANIZATION_ADMIN,
                ROLE_SERVICE_MANAGER, ROLE_TECHNOLOGY_MANAGER);
        proRataService = SubscriptionUpgradeSetup
                .createAndPublishProRataProduct(container,
                        technicalServiceAsync, marketplace, 3);
        proRataService2 = SubscriptionUpgradeSetup
                .createAndPublishProRataProduct(container,
                        technicalServiceAsync, marketplace, 0, 101D);
        SubscriptionUpgradeSetup.defineUpgradePath(container, proRataService,
                proRataService2);
        proRataService = SubscriptionUpgradeSetup.activateService(container,
                proRataService);
        proRataService2 = SubscriptionUpgradeSetup.activateService(container,
                proRataService2);

        // initial subscribe
        dateFactorySetTime("2013-07-29 13:00:00");
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        VOSubscription subscription = subscribeToService(
                generateSubscriptionId(), proRataService, customerUserId,
                customerUserKey);

        String customerOrgId = container.get(AccountService.class)
                .getOrganizationData().getOrganizationId();
        // complete async subscription
        dateFactorySetTime("2013-07-31 13:00:00");
        subscription = completeAsyncSubscription(supplierUserKey,
                customerOrgId, customerUserKey, subscription);

        long priceModelKey1 = getSubscriptionDetails(subscription)
                .getPriceModel().getKey();

        // upgrade
        dateFactorySetTime("2013-07-31 13:00:00");
        subscription = upgradeSubscription(subscription, proRataService2);

        // complete upgrade
        dateFactorySetTime("2013-08-01 13:00:00");
        subscription = completeAsyncUpgradeSubscription(supplierUserKey,
                customerUserKey, customerOrgId, subscription);

        long priceModelKey2 = getSubscriptionDetails(subscription)
                .getPriceModel().getKey();

        // WHEN
        dateFactorySetTime("2013-09-10 00:00:00");
        performBillingRun(defaultBillingOffset,
                DateTimeHandling.calculateMillis("2013-09-10 00:00:00"));

        // THEN
        BillingResultEvaluator eva = billingResultEvaluatorFactory(
                subscription, "2013-07-01 00:00:00", "2013-08-01 00:00:00");
        assertNull(eva);

        eva = billingResultEvaluatorFactory(subscription,
                "2013-08-01 00:00:00", "2013-09-01 00:00:00");
        eva.assertNullOneTimeFee(priceModelKey1);
        eva.assertOneTimeFee(priceModelKey2, "101.00");
    }

    @Test
    public void prorata_upgradeInFreePeriod() throws Exception {
        // GIVEN
        dateFactorySetTime("2013-01-01 08:00:00");
        container.login(supplierUserKey, ROLE_ORGANIZATION_ADMIN,
                ROLE_SERVICE_MANAGER, ROLE_TECHNOLOGY_MANAGER);
        proRataService = SubscriptionUpgradeSetup
                .createAndPublishProRataProduct(container,
                        technicalServiceAsync, marketplace, 3);
        proRataService2 = SubscriptionUpgradeSetup
                .createAndPublishProRataProduct(container,
                        technicalServiceAsync, marketplace, 0, 101D);
        SubscriptionUpgradeSetup.defineUpgradePath(container, proRataService,
                proRataService2);
        proRataService = SubscriptionUpgradeSetup.activateService(container,
                proRataService);
        proRataService2 = SubscriptionUpgradeSetup.activateService(container,
                proRataService2);

        // initial subscribe
        dateFactorySetTime("2013-06-30 13:00:00");
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        VOSubscription subscription = subscribeToService(
                generateSubscriptionId(), proRataService, customerUserId,
                customerUserKey);
        String customerOrgId = container.get(AccountService.class)
                .getOrganizationData().getOrganizationId();

        // complete async subscription
        dateFactorySetTime("2013-07-01 13:00:00");
        subscription = completeAsyncSubscription(supplierUserKey,
                customerOrgId, customerUserKey, subscription);

        long priceModelKey1 = getSubscriptionDetails(subscription)
                .getPriceModel().getKey();

        // upgrade
        dateFactorySetTime("2013-07-01 13:00:00");
        subscription = upgradeSubscription(subscription, proRataService2);

        // complete upgrade
        dateFactorySetTime("2013-07-02 13:00:00");
        subscription = completeAsyncUpgradeSubscription(supplierUserKey,
                customerUserKey, customerOrgId, subscription);

        long priceModelKey2 = getSubscriptionDetails(subscription)
                .getPriceModel().getKey();

        // WHEN
        dateFactorySetTime("2013-09-10 00:00:00");
        performBillingRun(defaultBillingOffset,
                DateTimeHandling.calculateMillis("2013-09-10 00:00:00"));

        // THEN
        BillingResultEvaluator eva = billingResultEvaluatorFactory(
                subscription, "2013-07-01 00:00:00", "2013-08-01 00:00:00");
        eva.assertNullOneTimeFee(priceModelKey1);
        eva.assertOneTimeFee(priceModelKey2, "101.00");
    }

    @Test
    public void prorata_upgrade_suspend() throws Exception {
        // GIVEN

        dateFactorySetTime("2013-01-01 08:00:00");
        container.login(supplierUserKey, ROLE_ORGANIZATION_ADMIN,
                ROLE_SERVICE_MANAGER, ROLE_TECHNOLOGY_MANAGER);
        proRataService = SubscriptionUpgradeSetup
                .createAndPublishProRataProduct(container,
                        technicalServiceAsync, marketplace, 0);
        proRataService2 = SubscriptionUpgradeSetup
                .createAndPublishProRataProduct(container,
                        technicalServiceAsync, marketplace, 0, 101D);
        SubscriptionUpgradeSetup.defineUpgradePath(container, proRataService,
                proRataService2);

        proRataService = container.get(ServiceProvisioningService.class)
                .activateService(proRataService);
        proRataService2 = container.get(ServiceProvisioningService.class)
                .activateService(proRataService2);

        // initial subscribe
        dateFactorySetTime("2013-07-01 12:00:00");
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        VOSubscription subscription = subscribeToService(
                generateSubscriptionId(), proRataService, customerUserId,
                customerUserKey);

        // complete subscribe
        String customerOrgId = container.get(AccountService.class)
                .getOrganizationData().getOrganizationId();
        dateFactorySetTime("2013-07-01 13:00:00");
        subscription = completeAsyncSubscription(supplierUserKey,
                customerOrgId, customerUserKey, subscription);

        // upgrade
        dateFactorySetTime("2013-07-09 13:00:00");
        subscription = upgradeSubscription(subscription, proRataService2);

        // complete upgrade1
        dateFactorySetTime("2013-07-10 13:00:00");
        subscription = completeAsyncUpgradeSubscription(supplierUserKey,
                customerUserKey, customerOrgId, subscription);

        // suspend & resume
        dateFactorySetTime("2013-07-12 13:00:00");
        deletePaymentTypes();
        dateFactorySetTime("2013-07-14 13:00:00");
        addPaymentTypes();

        // WHEN
        dateFactorySetTime("2013-09-10 00:00:00");
        performBillingRun(defaultBillingOffset,
                DateTimeHandling.calculateMillis("2013-09-10 00:00:00"));

        // THEN
        BillingResultEvaluator eva = billingResultEvaluatorFactory(
                subscription, "2013-07-01 00:00:00", "2013-08-01 00:00:00");

        Node pm = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(),
                DateTimeHandling.calculateMillis("2013-07-14 13:00:00"),
                DateTimeHandling.calculateMillis("2013-08-01 00:00:00"));
        eva.assertNullOneTimeFee(pm);

        pm = BillingXMLNodeSearch.getPriceModelNode(eva.getBillingResult(),
                DateTimeHandling.calculateMillis("2013-07-10 13:00:00"),
                DateTimeHandling.calculateMillis("2013-07-12 13:00:00"));
        eva.assertOneTimeFee(pm, "101.00");

        pm = BillingXMLNodeSearch.getPriceModelNode(eva.getBillingResult(),
                DateTimeHandling.calculateMillis("2013-07-01 13:00:00"),
                DateTimeHandling.calculateMillis("2013-07-10 13:00:00"));
        eva.assertOneTimeFee(pm, "100.00");
    }

    @Test
    public void prorata_suspend_freePeriodEndsWithinSuspendBlock()
            throws Exception {
        // GIVEN

        dateFactorySetTime("2013-01-01 08:00:00");
        container.login(supplierUserKey, ROLE_ORGANIZATION_ADMIN,
                ROLE_SERVICE_MANAGER, ROLE_TECHNOLOGY_MANAGER);
        proRataService = SubscriptionUpgradeSetup
                .createAndPublishProRataProduct(container,
                        technicalServiceAsync, marketplace, 0);
        proRataService2 = SubscriptionUpgradeSetup
                .createAndPublishProRataProduct(container,
                        technicalServiceAsync, marketplace, 5, 101D);
        SubscriptionUpgradeSetup.defineUpgradePath(container, proRataService,
                proRataService2);

        proRataService = container.get(ServiceProvisioningService.class)
                .activateService(proRataService);
        proRataService2 = container.get(ServiceProvisioningService.class)
                .activateService(proRataService2);

        // initial subscribe
        dateFactorySetTime("2013-06-01 13:00:00");
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        VOSubscription subscription = subscribeToService(
                generateSubscriptionId(), proRataService, customerUserId,
                customerUserKey);
        // complete subscribe
        String customerOrgId = container.get(AccountService.class)
                .getOrganizationData().getOrganizationId();
        dateFactorySetTime("2013-07-01 13:00:00");
        subscription = completeAsyncSubscription(supplierUserKey,
                customerOrgId, customerUserKey, subscription);

        // upgrade
        dateFactorySetTime("2013-07-05 13:00:00");
        subscription = upgradeSubscription(subscription, proRataService2);
        // complete upgrade1
        dateFactorySetTime("2013-07-10 13:00:00");
        subscription = completeAsyncUpgradeSubscription(supplierUserKey,
                customerUserKey, customerOrgId, subscription);

        // suspend & resume
        dateFactorySetTime("2013-07-14 13:00:00");
        deletePaymentTypes();
        dateFactorySetTime("2013-07-20 13:00:00");
        addPaymentTypes();

        // WHEN
        dateFactorySetTime("2013-09-10 00:00:00");
        performBillingRun(defaultBillingOffset,
                DateTimeHandling.calculateMillis("2013-09-10 00:00:00"));

        // THEN
        BillingResultEvaluator eva = billingResultEvaluatorFactory(
                subscription, "2013-07-01 00:00:00", "2013-08-01 00:00:00");

        Node pm = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(),
                DateTimeHandling.calculateMillis("2013-07-20 13:00:00"),
                DateTimeHandling.calculateMillis("2013-08-01 00:00:00"));
        eva.assertOneTimeFee(pm, "101.00");

        pm = BillingXMLNodeSearch.getPriceModelNode(eva.getBillingResult(),
                DateTimeHandling.calculateMillis("2013-07-01 13:00:00"),
                DateTimeHandling.calculateMillis("2013-07-10 13:00:00"));
        eva.assertOneTimeFee(pm, "100.00");
    }

    @Test
    public void proRata_suspend_freePeriodEndsWithinSuspendBlock_resumeInNextPeriod()
            throws Exception {
        // GIVEN

        dateFactorySetTime("2013-01-01 08:00:00");
        container.login(supplierUserKey, ROLE_ORGANIZATION_ADMIN,
                ROLE_SERVICE_MANAGER, ROLE_TECHNOLOGY_MANAGER);
        proRataService = SubscriptionUpgradeSetup
                .createAndPublishProRataProduct(container,
                        technicalServiceAsync, marketplace, 0);
        proRataService2 = SubscriptionUpgradeSetup
                .createAndPublishProRataProduct(container,
                        technicalServiceAsync, marketplace, 10, 101D);
        SubscriptionUpgradeSetup.defineUpgradePath(container, proRataService,
                proRataService2);

        proRataService = container.get(ServiceProvisioningService.class)
                .activateService(proRataService);
        proRataService2 = container.get(ServiceProvisioningService.class)
                .activateService(proRataService2);

        // initial subscribe
        dateFactorySetTime("2013-06-01 13:00:00");
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        VOSubscription subscription = subscribeToService(
                generateSubscriptionId(), proRataService, customerUserId,
                customerUserKey);

        // complete subscribe
        String customerOrgId = container.get(AccountService.class)
                .getOrganizationData().getOrganizationId();
        dateFactorySetTime("2013-07-01 13:00:00");
        subscription = completeAsyncSubscription(supplierUserKey,
                customerOrgId, customerUserKey, subscription);

        // upgrade
        dateFactorySetTime("2013-07-09 13:00:00");
        subscription = upgradeSubscription(subscription, proRataService2);
        // complete upgrade1
        dateFactorySetTime("2013-07-10 13:00:00");
        subscription = completeAsyncUpgradeSubscription(supplierUserKey,
                customerUserKey, customerOrgId, subscription);
        // suspend & resume
        dateFactorySetTime("2013-07-14 13:00:00");
        deletePaymentTypes();
        dateFactorySetTime("2013-08-04 13:00:00");
        addPaymentTypes();

        // WHEN
        dateFactorySetTime("2013-09-10 00:00:00");
        performBillingRun(defaultBillingOffset,
                DateTimeHandling.calculateMillis("2013-09-10 00:00:00"));

        // THEN
        BillingResultEvaluator eva = billingResultEvaluatorFactory(
                subscription, "2013-07-01 00:00:00", "2013-08-01 00:00:00");

        Node pm = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(),
                DateTimeHandling.calculateMillis("2013-07-01 13:00:00"),
                DateTimeHandling.calculateMillis("2013-07-10 13:00:00"));
        eva.assertOneTimeFee(pm, "100.00");

        eva = billingResultEvaluatorFactory(subscription,
                "2013-08-01 00:00:00", "2013-09-01 00:00:00");
        pm = BillingXMLNodeSearch.getPriceModelNode(eva.getBillingResult(),
                DateTimeHandling.calculateMillis("2013-08-04 13:00:00"),
                DateTimeHandling.calculateMillis("2013-09-01 00:00:00"));
        eva.assertOneTimeFee(pm, "101.00");
    }

    @Test
    public void prorata_suspend_freePeriodEndAfterResume() throws Exception {
        // GIVEN

        dateFactorySetTime("2013-01-01 08:00:00");
        container.login(supplierUserKey, ROLE_ORGANIZATION_ADMIN,
                ROLE_SERVICE_MANAGER, ROLE_TECHNOLOGY_MANAGER);
        proRataService = SubscriptionUpgradeSetup
                .createAndPublishProRataProduct(container,
                        technicalServiceAsync, marketplace, 0);
        proRataService2 = SubscriptionUpgradeSetup
                .createAndPublishProRataProduct(container,
                        technicalServiceAsync, marketplace, 5, 101D);
        SubscriptionUpgradeSetup.defineUpgradePath(container, proRataService,
                proRataService2);

        proRataService = container.get(ServiceProvisioningService.class)
                .activateService(proRataService);
        proRataService2 = container.get(ServiceProvisioningService.class)
                .activateService(proRataService2);

        // initial subscribe
        dateFactorySetTime("2013-06-01 13:00:00");
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        VOSubscription subscription = subscribeToService(
                generateSubscriptionId(), proRataService, customerUserId,
                customerUserKey);
        // complete subscribe
        String customerOrgId = container.get(AccountService.class)
                .getOrganizationData().getOrganizationId();
        dateFactorySetTime("2013-07-01 13:00:00");
        subscription = completeAsyncSubscription(supplierUserKey,
                customerOrgId, customerUserKey, subscription);

        // upgrade
        dateFactorySetTime("2013-07-05 13:00:00");
        subscription = upgradeSubscription(subscription, proRataService2);
        // complete upgrade1
        dateFactorySetTime("2013-07-10 13:00:00");
        subscription = completeAsyncUpgradeSubscription(supplierUserKey,
                customerUserKey, customerOrgId, subscription);

        // suspend & resume
        dateFactorySetTime("2013-07-11 13:00:00");
        deletePaymentTypes();
        dateFactorySetTime("2013-07-12 13:00:00");
        addPaymentTypes();

        // WHEN
        dateFactorySetTime("2013-09-10 00:00:00");
        performBillingRun(defaultBillingOffset,
                DateTimeHandling.calculateMillis("2013-09-10 00:00:00"));

        // THEN
        BillingResultEvaluator eva = billingResultEvaluatorFactory(
                subscription, "2013-07-01 00:00:00", "2013-08-01 00:00:00");

        Node pm = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(),
                DateTimeHandling.calculateMillis("2013-07-15 13:00:00"),
                DateTimeHandling.calculateMillis("2013-08-01 00:00:00"));
        eva.assertOneTimeFee(pm, "101.00");

        pm = BillingXMLNodeSearch.getPriceModelNode(eva.getBillingResult(),
                DateTimeHandling.calculateMillis("2013-07-01 13:00:00"),
                DateTimeHandling.calculateMillis("2013-07-10 13:00:00"));
        eva.assertOneTimeFee(pm, "100.00");
    }

    @Test
    public void prorata_suspend_billingPeriodWithinSuspendBlock()
            throws Exception {
        // GIVEN

        dateFactorySetTime("2013-01-01 08:00:00");
        container.login(supplierUserKey, ROLE_ORGANIZATION_ADMIN,
                ROLE_SERVICE_MANAGER, ROLE_TECHNOLOGY_MANAGER);
        proRataService = SubscriptionUpgradeSetup
                .createAndPublishProRataProduct(container,
                        technicalServiceAsync, marketplace, 0);
        proRataService2 = SubscriptionUpgradeSetup
                .createAndPublishProRataProduct(container,
                        technicalServiceAsync, marketplace, 5, 101D);
        SubscriptionUpgradeSetup.defineUpgradePath(container, proRataService,
                proRataService2);

        proRataService = container.get(ServiceProvisioningService.class)
                .activateService(proRataService);
        proRataService2 = container.get(ServiceProvisioningService.class)
                .activateService(proRataService2);

        // initial subscribe
        dateFactorySetTime("2013-04-01 13:00:00");
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        VOSubscription subscription = subscribeToService(
                generateSubscriptionId(), proRataService, customerUserId,
                customerUserKey);
        // complete subscribe
        String customerOrgId = container.get(AccountService.class)
                .getOrganizationData().getOrganizationId();
        dateFactorySetTime("2013-05-01 13:00:00");
        subscription = completeAsyncSubscription(supplierUserKey,
                customerOrgId, customerUserKey, subscription);

        // upgrade
        dateFactorySetTime("2013-05-09 13:00:00");
        subscription = upgradeSubscription(subscription, proRataService2);
        // complete upgrade1
        dateFactorySetTime("2013-05-10 13:00:00");
        subscription = completeAsyncUpgradeSubscription(supplierUserKey,
                customerUserKey, customerOrgId, subscription);

        // suspend & resume
        dateFactorySetTime("2013-05-11 13:00:00");
        deletePaymentTypes();
        dateFactorySetTime("2013-07-10 13:00:00");
        addPaymentTypes();

        // WHEN
        dateFactorySetTime("2013-07-10 00:00:00");
        performBillingRun(defaultBillingOffset,
                DateTimeHandling.calculateMillis("2013-07-10 00:00:00"));
        dateFactorySetTime("2013-09-10 00:00:00");
        performBillingRun(defaultBillingOffset,
                DateTimeHandling.calculateMillis("2013-09-10 00:00:00"));

        // THEN
        BillingResultEvaluator eva = billingResultEvaluatorFactory(
                subscription, "2013-07-01 00:00:00", "2013-08-01 00:00:00");
        Node pm = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(),
                DateTimeHandling.calculateMillis("2013-07-10 13:00:00"),
                DateTimeHandling.calculateMillis("2013-08-01 00:00:00"));
        eva.assertOneTimeFee(pm, "101.00");

        eva = billingResultEvaluatorFactory(subscription,
                "2013-05-01 00:00:00", "2013-06-01 00:00:00");

        pm = BillingXMLNodeSearch.getPriceModelNode(eva.getBillingResult(),
                DateTimeHandling.calculateMillis("2013-05-01 13:00:00"),
                DateTimeHandling.calculateMillis("2013-05-10 13:00:00"));
        eva.assertOneTimeFee(pm, "100.00");
    }

    /**
     * Overlapping week, resumed in overlapping week, upgraded to per unit week
     */

    @Test
    public void overlapping_subStartBeforeBs_resumed_upgrade() throws Exception {
        // GIVEN

        dateFactorySetTime("2013-01-01 08:00:00");
        container.login(supplierUserKey, ROLE_ORGANIZATION_ADMIN,
                ROLE_SERVICE_MANAGER, ROLE_TECHNOLOGY_MANAGER);
        perUnitWeekService = SubscriptionUpgradeSetup
                .createAndPublishPerUnitWeekProduct(container,
                        technicalServiceAsync, marketplace, 0);
        perUnitMonthService = SubscriptionUpgradeSetup
                .createAndPublishPerUnitMonthProduct(container,
                        technicalServiceAsync, marketplace, 0);
        SubscriptionUpgradeSetup.defineUpgradePath(container,
                perUnitWeekService, perUnitMonthService);

        perUnitWeekService = container.get(ServiceProvisioningService.class)
                .activateService(perUnitWeekService);
        perUnitMonthService = container.get(ServiceProvisioningService.class)
                .activateService(perUnitMonthService);

        // subscribe
        dateFactorySetTime("2013-07-09 13:00:00");
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        VOSubscription subscription = subscribeToService(
                generateSubscriptionId(), perUnitWeekService, customerUserId,
                customerUserKey);

        // complete subscribe
        String customerOrgId = container.get(AccountService.class)
                .getOrganizationData().getOrganizationId();
        dateFactorySetTime("2013-07-10 13:00:00");
        subscription = completeAsyncSubscription(supplierUserKey,
                customerOrgId, customerUserKey, subscription);
        long priceModelKey1 = getSubscriptionDetails(subscription)
                .getPriceModel().getKey();

        // suspend & resume
        dateFactorySetTime("2013-07-15 13:00:00");
        deletePaymentTypes();
        dateFactorySetTime("2013-07-29 13:00:00");
        addPaymentTypes();

        // upgrade
        dateFactorySetTime("2013-07-30 13:00:00");
        subscription = loadSubscriptionDetails(subscription.getSubscriptionId());
        subscription = upgradeSubscription(subscription, perUnitMonthService);

        // complete upgrade1
        dateFactorySetTime("2013-07-30 14:00:00");
        subscription = completeAsyncUpgradeSubscription(supplierUserKey,
                customerUserKey, customerOrgId, subscription);

        long priceModelKey2 = getSubscriptionDetails(subscription)
                .getPriceModel().getKey();

        // WHEN
        dateFactorySetTime("2013-09-10 00:00:00");
        performBillingRun(defaultBillingOffset,
                DateTimeHandling.calculateMillis("2013-09-10 00:00:00"));

        // THEN
        BillingResultEvaluator eva = billingResultEvaluatorFactory(
                subscription, "2013-07-01 00:00:00", "2013-08-01 00:00:00");
        eva.assertOneTimeFee(priceModelKey1, "200.00");
        eva.assertOneTimeFee(priceModelKey2, "150.00");

        eva = billingResultEvaluatorFactory(subscription,
                "2013-08-01 00:00:00", "2013-09-01 00:00:00");
        eva.assertNullOneTimeFee(priceModelKey1);
        eva.assertNullOneTimeFee(priceModelKey2);
    }

    /**
     * Overlapping week, resumed in overlapping week
     */

    @Test
    public void overlapping_subStartBeforeBs_resumed() throws Exception {
        // GIVEN

        dateFactorySetTime("2013-01-01 08:00:00");
        container.login(supplierUserKey, ROLE_ORGANIZATION_ADMIN,
                ROLE_SERVICE_MANAGER, ROLE_TECHNOLOGY_MANAGER);
        perUnitWeekService = SubscriptionUpgradeSetup
                .createAndPublishPerUnitWeekProduct(container,
                        technicalServiceAsync, marketplace, 19);

        perUnitWeekService = container.get(ServiceProvisioningService.class)
                .activateService(perUnitWeekService);

        // subscribe
        dateFactorySetTime("2013-07-09 11:00:00");
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        VOSubscription subscription = subscribeToService(
                generateSubscriptionId(), perUnitWeekService, customerUserId,
                customerUserKey);

        // complete subscribe
        String customerOrgId = container.get(AccountService.class)
                .getOrganizationData().getOrganizationId();
        dateFactorySetTime("2013-07-10 11:00:00");
        subscription = completeAsyncSubscription(supplierUserKey,
                customerOrgId, customerUserKey, subscription);
        long priceModelKey1 = getSubscriptionDetails(subscription)
                .getPriceModel().getKey();

        // suspend & resume
        dateFactorySetTime("2013-07-15 13:00:00");
        deletePaymentTypes();
        dateFactorySetTime("2013-07-29 13:00:00");
        addPaymentTypes();

        // WHEN
        dateFactorySetTime("2013-09-10 00:00:00");
        performBillingRun(defaultBillingOffset,
                DateTimeHandling.calculateMillis("2013-09-10 00:00:00"));

        // THEN
        BillingResultEvaluator eva = billingResultEvaluatorFactory(
                subscription, "2013-07-01 00:00:00", "2013-08-01 00:00:00");
        eva.assertOneTimeFee(priceModelKey1, "200.00");

        eva = billingResultEvaluatorFactory(subscription,
                "2013-08-01 00:00:00", "2013-09-01 00:00:00");
        eva.assertNullOneTimeFee(priceModelKey1);
    }

    @Test
    public void pro_rata_longFreePeriod() throws Exception {
        // GIVEN
        dateFactorySetTime("2013-01-01 08:00:00");
        container.login(supplierUserKey, ROLE_ORGANIZATION_ADMIN,
                ROLE_SERVICE_MANAGER, ROLE_TECHNOLOGY_MANAGER);
        proRataService = SubscriptionUpgradeSetup
                .createAndPublishProRataProduct(container,
                        technicalServiceAsync, marketplace, 32);
        proRataService = SubscriptionUpgradeSetup.activateService(container,
                proRataService);

        dateFactorySetTime("2013-07-01 13:00:00");
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        VOSubscription subscription = subscribeToService(
                generateSubscriptionId(), proRataService, customerUserId,
                customerUserKey);

        String customerOrgId = container.get(AccountService.class)
                .getOrganizationData().getOrganizationId();

        // complete async subscription
        dateFactorySetTime("2013-07-01 13:00:00");
        subscription = completeAsyncSubscription(supplierUserKey,
                customerOrgId, customerUserKey, subscription);

        long priceModelKey1 = getSubscriptionDetails(subscription)
                .getPriceModel().getKey();

        dateFactorySetTime("2013-09-15 12:00:00");
        terminateSubscription(subscription.getSubscriptionId());

        // WHEN
        dateFactorySetTime("2013-09-10 00:00:00");
        performBillingRun(defaultBillingOffset,
                DateTimeHandling.calculateMillis("2013-09-10 00:00:00"));

        // THEN
        BillingResultEvaluator eva = billingResultEvaluatorFactory(
                subscription, "2013-07-01 00:00:00", "2013-08-01 00:00:00");
        assertNull("no result should exist", eva);

        eva = billingResultEvaluatorFactory(subscription,
                "2013-08-01 00:00:00", "2013-09-01 00:00:00");
        eva.assertOneTimeFee(priceModelKey1, "100.00");
    }

    @Test
    public void pro_rata_freePeriodEndsInSamePeriodAfterResume()
            throws Exception {
        // GIVEN
        dateFactorySetTime("2013-01-01 08:00:00");
        container.login(supplierUserKey, ROLE_ORGANIZATION_ADMIN,
                ROLE_SERVICE_MANAGER, ROLE_TECHNOLOGY_MANAGER);
        proRataService = SubscriptionUpgradeSetup
                .createAndPublishProRataProduct(container,
                        technicalServiceAsync, marketplace, 10);
        proRataService = SubscriptionUpgradeSetup.activateService(container,
                proRataService);

        // subscribe
        dateFactorySetTime("2013-07-01 13:00:00");
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        VOSubscription subscription = subscribeToService(
                generateSubscriptionId(), proRataService, customerUserId,
                customerUserKey);

        String customerOrgId = container.get(AccountService.class)
                .getOrganizationData().getOrganizationId();

        // complete async subscription
        dateFactorySetTime("2013-07-01 13:00:00");
        subscription = completeAsyncSubscription(supplierUserKey,
                customerOrgId, customerUserKey, subscription);

        long priceModelKey1 = getSubscriptionDetails(subscription)
                .getPriceModel().getKey();

        // suspend & resume
        dateFactorySetTime("2013-07-08 13:00:00");
        deletePaymentTypes();
        dateFactorySetTime("2013-07-20 13:00:00");
        addPaymentTypes();

        // terminate
        dateFactorySetTime("2013-07-25 12:00:00");
        terminateSubscription(subscription.getSubscriptionId());

        // WHEN
        dateFactorySetTime("2013-09-10 00:00:00");
        performBillingRun(defaultBillingOffset,
                DateTimeHandling.calculateMillis("2013-09-10 00:00:00"));

        // THEN
        BillingResultEvaluator eva = billingResultEvaluatorFactory(
                subscription, "2013-07-01 00:00:00", "2013-08-01 00:00:00");
        eva.assertOneTimeFee(priceModelKey1, "100.00");
    }

    @Test
    public void prorata_suspend_multipleBillingPeriodsWithinSuspendBlock()
            throws Exception {
        // GIVEN
        dateFactorySetTime("2013-01-01 08:00:00");
        container.login(supplierUserKey, ROLE_ORGANIZATION_ADMIN,
                ROLE_SERVICE_MANAGER, ROLE_TECHNOLOGY_MANAGER);
        proRataService = SubscriptionUpgradeSetup
                .createAndPublishProRataProduct(container,
                        technicalServiceAsync, marketplace, 0);
        proRataService = SubscriptionUpgradeSetup.activateService(container,
                proRataService);

        // initial subscribe
        dateFactorySetTime("2013-05-01 13:00:00");
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        VOSubscription subscription = subscribeToService(
                generateSubscriptionId(), proRataService, customerUserId,
                customerUserKey);

        String customerOrgId = container.get(AccountService.class)
                .getOrganizationData().getOrganizationId();

        // complete async subscription
        dateFactorySetTime("2013-05-01 13:00:00");
        subscription = completeAsyncSubscription(supplierUserKey,
                customerOrgId, customerUserKey, subscription);

        // suspend & resume
        dateFactorySetTime("2013-05-11 13:00:00");
        deletePaymentTypes();
        dateFactorySetTime("2013-11-10 13:00:00");
        addPaymentTypes();

        // WHEN
        dateFactorySetTime("2013-06-10 00:00:00");
        performBillingRun(defaultBillingOffset,
                DateTimeHandling.calculateMillis("2013-06-10 00:00:00"));
        dateFactorySetTime("2013-12-10 00:00:00");
        performBillingRun(defaultBillingOffset,
                DateTimeHandling.calculateMillis("2013-12-10 00:00:00"));

        // THEN
        BillingResultEvaluator eva = billingResultEvaluatorFactory(
                subscription, "2013-11-01 00:00:00", "2013-12-01 00:00:00");
        Node pm = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(),
                DateTimeHandling.calculateMillis("2013-11-10 13:00:00"),
                DateTimeHandling.calculateMillis("2013-12-01 00:00:00"));
        eva.assertNullOneTimeFee(pm);

        eva = billingResultEvaluatorFactory(subscription,
                "2013-05-01 00:00:00", "2013-06-01 00:00:00");
        pm = BillingXMLNodeSearch.getPriceModelNode(eva.getBillingResult(),
                DateTimeHandling.calculateMillis("2013-05-01 13:00:00"),
                DateTimeHandling.calculateMillis("2013-05-11 13:00:00"));
        eva.assertOneTimeFee(pm, "100.00");
    }

    @Test
    public void prorata_suspend_multipleBillingPeriodsWithinSuspendBlock_fp()
            throws Exception {
        // GIVEN

        dateFactorySetTime("2013-01-01 08:00:00");
        container.login(supplierUserKey, ROLE_ORGANIZATION_ADMIN,
                ROLE_SERVICE_MANAGER, ROLE_TECHNOLOGY_MANAGER);
        proRataService = SubscriptionUpgradeSetup
                .createAndPublishProRataProduct(container,
                        technicalServiceAsync, marketplace, 20);

        proRataService = container.get(ServiceProvisioningService.class)
                .activateService(proRataService);

        // initial subscribe
        dateFactorySetTime("2013-05-01 10:00:00");
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        VOSubscription subscription = subscribeToService(
                generateSubscriptionId(), proRataService, customerUserId,
                customerUserKey);

        // complete subscribe
        String customerOrgId = container.get(AccountService.class)
                .getOrganizationData().getOrganizationId();
        dateFactorySetTime("2013-05-01 13:00:00");
        subscription = completeAsyncSubscription(supplierUserKey,
                customerOrgId, customerUserKey, subscription);

        // suspend & resume
        dateFactorySetTime("2013-05-11 13:00:00");
        deletePaymentTypes();
        dateFactorySetTime("2013-11-10 13:00:00");
        addPaymentTypes();

        // WHEN
        dateFactorySetTime("2013-06-10 00:00:00");
        performBillingRun(defaultBillingOffset,
                DateTimeHandling.calculateMillis("2013-06-10 00:00:00"));
        dateFactorySetTime("2013-12-10 00:00:00");
        performBillingRun(defaultBillingOffset,
                DateTimeHandling.calculateMillis("2013-12-10 00:00:00"));

        // THEN
        BillingResultEvaluator eva = billingResultEvaluatorFactory(
                subscription, "2013-11-01 00:00:00", "2013-12-01 00:00:00");
        Node pm = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(),
                DateTimeHandling.calculateMillis("2013-11-10 13:00:00"),
                DateTimeHandling.calculateMillis("2013-12-01 00:00:00"));
        eva.assertOneTimeFee(pm, "100.00");
    }

    /**
     * Subscription to per unit service is suspended and resumed multiple times.
     * Free period ends exactly at a suspend time.
     * 
     * @throws Exception
     */

    @Test
    public void perunit_multiple_suspend() throws Exception {
        // GIVEN

        dateFactorySetTime("2013-02-15 08:00:00");
        container.login(supplierUserKey, ROLE_ORGANIZATION_ADMIN,
                ROLE_SERVICE_MANAGER, ROLE_TECHNOLOGY_MANAGER);
        perUnitWeekService = SubscriptionUpgradeSetup
                .createAndPublishPerUnitWeekProduct(container,
                        technicalServiceAsync, marketplace, 17);

        perUnitWeekService = container.get(ServiceProvisioningService.class)
                .activateService(perUnitWeekService);

        // initial subscribe
        dateFactorySetTime("2013-02-02 00:00:00");
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        VOSubscription subscription = subscribeToService(
                generateSubscriptionId(), perUnitWeekService, customerUserId,
                customerUserKey);

        // complete subscribe
        String customerOrgId = container.get(AccountService.class)
                .getOrganizationData().getOrganizationId();
        dateFactorySetTime("2013-03-02 00:00:00");
        subscription = completeAsyncSubscription(supplierUserKey,
                customerOrgId, customerUserKey, subscription);

        // suspend & resume
        dateFactorySetTime("2013-03-04 00:00:00");
        deletePaymentTypes();
        dateFactorySetTime("2013-03-15 00:00:00");
        addPaymentTypes();
        dateFactorySetTime("2013-03-19 00:00:00");
        deletePaymentTypes();
        dateFactorySetTime("2013-03-27 00:00:00");
        addPaymentTypes();

        // terminate
        dateFactorySetTime("2013-03-29 00:00:00");
        terminateSubscription(subscription.getSubscriptionId());

        // WHEN
        dateFactorySetTime("2013-04-10 00:00:00");
        performBillingRun(defaultBillingOffset,
                DateTimeHandling.calculateMillis("2013-04-10 00:00:00"));

        // THEN
        BillingResultEvaluator eva = billingResultEvaluatorFactory(
                subscription, "2013-03-01 00:00:00", "2013-04-01 00:00:00");

        Node pm = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(),
                DateTimeHandling.calculateMillis("2013-03-19 00:00:00"),
                DateTimeHandling.calculateMillis("2013-03-19 00:00:00"));
        eva.assertOneTimeFee(pm, "200.00");

        pm = BillingXMLNodeSearch.getPriceModelNode(eva.getBillingResult(),
                DateTimeHandling.calculateMillis("2013-03-27 00:00:00"),
                DateTimeHandling.calculateMillis("2013-03-29 00:00:00"));
        eva.assertNullOneTimeFee(pm);
    }

    /**
     * Subscription to pro rata service is suspended and resumed multiple times.
     * Free period ends exactly at a suspend time.
     * 
     * @throws Exception
     */

    @Test
    public void prorata_multiple_suspend() throws Exception {
        // GIVEN

        dateFactorySetTime("2013-02-15 08:00:00");
        container.login(supplierUserKey, ROLE_ORGANIZATION_ADMIN,
                ROLE_SERVICE_MANAGER, ROLE_TECHNOLOGY_MANAGER);
        proRataService = SubscriptionUpgradeSetup
                .createAndPublishProRataProduct(container,
                        technicalServiceAsync, marketplace, 17);

        proRataService = container.get(ServiceProvisioningService.class)
                .activateService(proRataService);

        // initial subscribe
        dateFactorySetTime("2013-03-01 00:00:00");
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        VOSubscription subscription = subscribeToService(
                generateSubscriptionId(), proRataService, customerUserId,
                customerUserKey);
        // complete subscribe
        String customerOrgId = container.get(AccountService.class)
                .getOrganizationData().getOrganizationId();
        dateFactorySetTime("2013-03-02 00:00:00");
        subscription = completeAsyncSubscription(supplierUserKey,
                customerOrgId, customerUserKey, subscription);
        // suspend & resume
        dateFactorySetTime("2013-03-04 00:00:00");
        deletePaymentTypes();
        dateFactorySetTime("2013-03-15 00:00:00");
        addPaymentTypes();
        dateFactorySetTime("2013-03-19 00:00:00");
        deletePaymentTypes();
        dateFactorySetTime("2013-03-27 00:00:00");
        addPaymentTypes();

        // terminate
        dateFactorySetTime("2013-03-29 00:00:00");
        terminateSubscription(subscription.getSubscriptionId());

        // WHEN
        dateFactorySetTime("2013-04-10 00:00:00");
        performBillingRun(defaultBillingOffset,
                DateTimeHandling.calculateMillis("2013-04-10 00:00:00"));

        // THEN
        BillingResultEvaluator eva = billingResultEvaluatorFactory(
                subscription, "2013-03-01 00:00:00", "2013-04-01 00:00:00");

        Node pm = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(),
                DateTimeHandling.calculateMillis("2013-03-19 00:00:00"),
                DateTimeHandling.calculateMillis("2013-03-19 00:00:00"));
        eva.assertOneTimeFee(pm, "100.00");

        pm = BillingXMLNodeSearch.getPriceModelNode(eva.getBillingResult(),
                DateTimeHandling.calculateMillis("2013-03-27 00:00:00"),
                DateTimeHandling.calculateMillis("2013-03-29 00:00:00"));
        eva.assertNullOneTimeFee(pm);
    }
}
