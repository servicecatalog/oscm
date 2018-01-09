/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 27.03.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.calculation.revenue;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;

import org.oscm.accountservice.local.AccountServiceLocal;
import org.oscm.billingservice.business.calculation.revenue.setup.SubscriptionUpgradeSetup;
import org.oscm.billingservice.evaluation.BillingResultEvaluator;
import org.oscm.billingservice.service.BillingServiceLocal;
import org.oscm.configurationservice.local.ConfigurationServiceLocal;
import org.oscm.converter.XMLConverter;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.BillingResult;
import org.oscm.domobjects.ConfigurationSetting;
import org.oscm.domobjects.enums.BillingAdapterIdentifier;
import org.oscm.interceptor.DateFactory;
import org.oscm.test.DateTimeHandling;
import org.oscm.test.StaticEJBTestBase;
import org.oscm.test.TestDateFactory;
import org.oscm.test.data.BillingAdapters;
import org.oscm.types.constants.Configuration;
import org.oscm.internal.intf.AccountService;
import org.oscm.internal.intf.ServiceProvisioningService;
import org.oscm.internal.intf.SubscriptionService;
import org.oscm.internal.types.enumtypes.ConfigurationKey;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.PriceModelType;
import org.oscm.internal.types.exception.ObjectNotFoundException;
import org.oscm.internal.types.exception.OperationNotPermittedException;
import org.oscm.internal.vo.VOBillingContact;
import org.oscm.internal.vo.VOMarketplace;
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
 * @author kulle
 * 
 */
public class SubscriptionUpgradeIT extends StaticEJBTestBase {

    private static long operatorUserKey;

    private static String supplierOrgId;
    private static long supplierOrgKey;
    private static long supplierUserKey;
    private static String customerUserId;
    private static long customerOrgKey;
    private static long customerUserKey;

    private static VOTechnicalService technicalService;
    private static VOService freeService;
    private static VOService proRataService;
    private static VOService perUnitWeekService;
    private static VOService perUnitMonthService;

    private final static int defaultBillingOffset = 0;

    @BeforeClass
    public static void setupOnce() throws Exception {
        SubscriptionUpgradeSetup.setup(container);

        runTX(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                dateFactorySetTime("2012-12-12 00:00:00");

                SubscriptionUpgradeSetup.baseSetup(container);
                DataService ds = container.get(DataService.class);
                BillingAdapters.createBillingAdapter(ds,
                                BillingAdapterIdentifier.NATIVE_BILLING.toString(),
                                true);
                operatorUserKey = SubscriptionUpgradeSetup
                        .registerOperatorOrganisation(container);

                container.login(operatorUserKey, ROLE_PLATFORM_OPERATOR);
                SubscriptionUpgradeSetup.addCurrencies(container);
                supplierOrgKey = SubscriptionUpgradeSetup
                        .registerSupplierAndTechnologyProvider(container);
                container.get(DataService.class).flush();
                supplierUserKey = container.get(AccountServiceLocal.class)
                        .getOrganizationAdmins(supplierOrgKey).get(0).getKey();

                container.login(supplierUserKey, ROLE_ORGANIZATION_ADMIN,
                        ROLE_SERVICE_MANAGER, ROLE_TECHNOLOGY_MANAGER);
                supplierOrgId = container.get(AccountService.class)
                        .getOrganizationData().getOrganizationId();
                SubscriptionUpgradeSetup.importTechnicalService(container,
                        TECHNICAL_SERVICES_XML);
                technicalService = container
                        .get(ServiceProvisioningService.class)
                        .getTechnicalServices(
                                OrganizationRoleType.TECHNOLOGY_PROVIDER)
                        .get(0);

                container.login(operatorUserKey, ROLE_PLATFORM_OPERATOR);
                VOMarketplace marketplace = SubscriptionUpgradeSetup
                        .createMarketplace(container, "m_supplier",
                                supplierOrgId);

                customerOrgKey = SubscriptionUpgradeSetup
                        .registerCustomerOrganization(container, marketplace);
                container.get(DataService.class).flush();
                customerUserKey = container.get(AccountServiceLocal.class)
                        .getOrganizationAdmins(customerOrgKey).get(0).getKey();
                customerUserId = container.get(AccountServiceLocal.class)
                        .getOrganizationAdmins(customerOrgKey).get(0)
                        .getUserId();

                container.login(supplierUserKey, ROLE_ORGANIZATION_ADMIN,
                        ROLE_SERVICE_MANAGER, ROLE_TECHNOLOGY_MANAGER);

                SubscriptionUpgradeSetup.updateCutoffDay(container, 15);
                SubscriptionUpgradeSetup.savePaymentConfiguration(container);

                freeService = SubscriptionUpgradeSetup
                        .createAndPublishFreeProduct(container,
                                technicalService, marketplace);
                proRataService = SubscriptionUpgradeSetup
                        .createAndPublishProRataProduct(container,
                                technicalService, marketplace);
                perUnitWeekService = SubscriptionUpgradeSetup
                        .createAndPublishPerUnitWeekProduct(container,
                                technicalService, marketplace);
                perUnitMonthService = SubscriptionUpgradeSetup
                        .createAndPublishPerUnitMonthProduct(container,
                                technicalService, marketplace);

                SubscriptionUpgradeSetup.defineUpgradePath(container,
                        freeService, proRataService, perUnitWeekService,
                        perUnitMonthService);
                container.get(DataService.class).flush();

                freeService = container.get(ServiceProvisioningService.class)
                        .activateService(freeService);
                proRataService = container
                        .get(ServiceProvisioningService.class).activateService(
                                proRataService);
                perUnitWeekService = container.get(
                        ServiceProvisioningService.class).activateService(
                        perUnitWeekService);
                perUnitMonthService = container.get(
                        ServiceProvisioningService.class).activateService(
                        perUnitMonthService);

                container.logout();

                return null;
            }
        });
    }

    @Test
    public void upgrade_PerUnitWeek_to_Free_NoOverlap() throws Exception {
        // given
        dateFactorySetTime("2013-01-01 00:00:00");
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        VOSubscription subscription = subscribeToService("PerUnitWeek_to_Free",
                perUnitWeekService, customerUserId, customerUserKey);
        long perUnitPriceModelKey = getSubscriptionDetails(subscription)
                .getPriceModel().getKey();

        dateFactorySetTime("2013-02-05 00:00:00");
        subscription = upgradeSubscription(subscription, freeService);

        // when
        dateFactorySetTime("2013-03-20 00:00:00");
        performBillingRun(defaultBillingOffset,
                DateTimeHandling.calculateMillis("2013-03-20 00:00:00"));

        // then
        // check last billing period
        try {
            billingResultEvaluatorFactory(subscription, "2013-02-15 00:00:00",
                    "2013-03-15 00:00:00");
            fail();
        } catch (NoResultException e) {
            // check second last billing period
            assertPriceModelFactor(subscription, perUnitPriceModelKey, 4.0,
                    "2013-01-15 00:00:00", "2013-02-15 00:00:00");
        } finally {
            container.logout();
        }
    }

    @Test
    public void upgrade_PerUnitWeek_to_Free_Overlap() throws Exception {
        // given
        dateFactorySetTime("2013-01-01 00:00:00");
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        VOSubscription subscription = subscribeToService("PerUnitWeek_to_Free",
                perUnitWeekService, customerUserId, customerUserKey);
        long perUnitPriceModelKey = getSubscriptionDetails(subscription)
                .getPriceModel().getKey();

        dateFactorySetTime("2013-02-12 00:00:00");
        subscription = upgradeSubscription(subscription, freeService);

        // when
        dateFactorySetTime("2013-03-20 00:00:00");
        performBillingRun(defaultBillingOffset,
                DateTimeHandling.calculateMillis("2013-03-20 00:00:00"));

        // then
        // check last billing period
        assertPriceModelFactor(subscription, perUnitPriceModelKey, 1.0,
                "2013-02-15 00:00:00", "2013-03-15 00:00:00");
        // check second last billing period
        assertPriceModelFactor(subscription, perUnitPriceModelKey, 4.0,
                "2013-01-15 00:00:00", "2013-02-15 00:00:00");

        container.logout();
    }

    @Test
    public void upgrade_PerUnitMonth_to_Free_Overlap() throws Exception {
        // given
        dateFactorySetTime("2013-01-01 00:00:00");
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        VOSubscription subscription = subscribeToService(
                "PerUnitMonth_to_Free", perUnitMonthService, customerUserId,
                customerUserKey);
        long perUnitPriceModelKey = getSubscriptionDetails(subscription)
                .getPriceModel().getKey();

        dateFactorySetTime("2013-02-05 00:00:00");
        subscription = upgradeSubscription(subscription, freeService);

        // when
        dateFactorySetTime("2013-03-20 00:00:00");
        performBillingRun(defaultBillingOffset,
                DateTimeHandling.calculateMillis("2013-03-20 00:00:00"));

        // then
        // check last billing period
        assertPriceModelFactor(subscription, perUnitPriceModelKey, 1.0,
                "2013-02-15 00:00:00", "2013-03-15 00:00:00");
        // check second last billing period
        assertPriceModelFactor(subscription, perUnitPriceModelKey, 1.0,
                "2013-01-15 00:00:00", "2013-02-15 00:00:00");

        container.logout();
    }

    @Test
    public void upgrade_PerUnitWeek_to_ProRata_to_Free_NoOverlap()
            throws Exception {
        // given
        dateFactorySetTime("2013-01-01 00:00:00");
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        VOSubscription subscription = subscribeToService("PUW_to_PR_to_FR",
                perUnitWeekService, customerUserId, customerUserKey);
        long perUnitPriceModelKey = getSubscriptionDetails(subscription)
                .getPriceModel().getKey();

        dateFactorySetTime("2013-02-05 00:00:00");
        subscription = upgradeSubscription(subscription, proRataService);

        dateFactorySetTime("2013-02-06 00:00:00");
        subscription = upgradeSubscription(subscription, freeService);

        // when
        dateFactorySetTime("2013-03-20 00:00:00");
        performBillingRun(defaultBillingOffset,
                DateTimeHandling.calculateMillis("2013-03-20 00:00:00"));

        // then
        // check last billing period
        try {
            billingResultEvaluatorFactory(subscription, "2013-02-15 00:00:00",
                    "2013-03-15 00:00:00");
            fail();
        } catch (NoResultException e) {

        }

        // check second last billing period
        assertPriceModelFactor(subscription, perUnitPriceModelKey, 4.0,
                "2013-01-15 00:00:00", "2013-02-15 00:00:00");

        container.logout();
    }

    @Test
    public void upgrade_PerUnitWeek_to_ProRata_to_Free_Overlap()
            throws Exception {
        // given
        dateFactorySetTime("2013-01-01 00:00:00");
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        VOSubscription subscription = subscribeToService("PUW_to_PR_to_FR",
                perUnitWeekService, customerUserId, customerUserKey);
        long perUnitPriceModelKey = getSubscriptionDetails(subscription)
                .getPriceModel().getKey();

        dateFactorySetTime("2013-02-12 00:00:00");
        subscription = upgradeSubscription(subscription, proRataService);

        dateFactorySetTime("2013-02-13 00:00:00");
        subscription = upgradeSubscription(subscription, freeService);

        // when
        dateFactorySetTime("2013-03-20 00:00:00");
        performBillingRun(defaultBillingOffset,
                DateTimeHandling.calculateMillis("2013-03-20 00:00:00"));

        // then
        // check last billing period
        assertPriceModelFactor(subscription, perUnitPriceModelKey, 1.0,
                "2013-02-15 00:00:00", "2013-03-15 00:00:00");
        // check second last billing period
        assertPriceModelFactor(subscription, perUnitPriceModelKey, 4.0,
                "2013-01-15 00:00:00", "2013-02-15 00:00:00");

        container.logout();
    }

    @Test
    public void upgrade_PerUnitMonth_to_ProRata_to_Free_Overlap()
            throws Exception {
        // given
        dateFactorySetTime("2013-01-01 00:00:00");
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        VOSubscription subscription = subscribeToService("PUM_to_PR_to_FR",
                perUnitMonthService, customerUserId, customerUserKey);
        long perUnitPriceModelKey = getSubscriptionDetails(subscription)
                .getPriceModel().getKey();

        dateFactorySetTime("2013-02-05 00:00:00");
        subscription = upgradeSubscription(subscription, proRataService);

        dateFactorySetTime("2013-02-06 00:00:00");
        subscription = upgradeSubscription(subscription, freeService);

        // when
        dateFactorySetTime("2013-03-20 00:00:00");
        performBillingRun(defaultBillingOffset,
                DateTimeHandling.calculateMillis("2013-03-20 00:00:00"));

        // then
        // check last billing period
        assertPriceModelFactor(subscription, perUnitPriceModelKey, 1.0,
                "2013-02-15 00:00:00", "2013-03-15 00:00:00");
        // check second last billing period
        assertPriceModelFactor(subscription, perUnitPriceModelKey, 1.0,
                "2013-01-15 00:00:00", "2013-02-15 00:00:00");

        container.logout();
    }

    @Test
    public void upgrade_PerUnitWeek_to_ProRata_NoOverlap() throws Exception {
        // given
        dateFactorySetTime("2013-01-01 00:00:00");
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        VOSubscription subscription = subscribeToService("PUW_to_PR",
                perUnitWeekService, customerUserId, customerUserKey);
        long perUnitPriceModelKey = getSubscriptionDetails(subscription)
                .getPriceModel().getKey();

        dateFactorySetTime("2013-02-05 00:00:00");
        subscription = upgradeSubscription(subscription, proRataService);

        // when
        dateFactorySetTime("2013-03-20 00:00:00");
        performBillingRun(defaultBillingOffset,
                DateTimeHandling.calculateMillis("2013-03-20 00:00:00"));

        // then
        // check last billing period
        assertPriceModelIsNull(subscription, perUnitPriceModelKey,
                "2013-02-15 00:00:00", "2013-03-15 00:00:00");
        // check second last billing period
        assertPriceModelFactor(subscription, perUnitPriceModelKey, 4.0,
                "2013-01-15 00:00:00", "2013-02-15 00:00:00");

        container.logout();
    }

    @Test
    public void upgrade_PerUnitWeek_to_ProRata_Overlap() throws Exception {
        // given
        dateFactorySetTime("2013-01-01 00:00:00");
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        VOSubscription subscription = subscribeToService("PUW_to_PR",
                perUnitWeekService, customerUserId, customerUserKey);
        long perUnitPriceModelKey = getSubscriptionDetails(subscription)
                .getPriceModel().getKey();

        dateFactorySetTime("2013-02-12 00:00:00");
        subscription = upgradeSubscription(subscription, proRataService);

        // when
        dateFactorySetTime("2013-03-20 00:00:00");
        performBillingRun(defaultBillingOffset,
                DateTimeHandling.calculateMillis("2013-03-20 00:00:00"));

        // then
        // check last billing period
        assertPriceModelFactor(subscription, perUnitPriceModelKey, 1.0,
                "2013-02-15 00:00:00", "2013-03-15 00:00:00");
        // check second last billing period
        assertPriceModelFactor(subscription, perUnitPriceModelKey, 4.0,
                "2013-01-15 00:00:00", "2013-02-15 00:00:00");

        container.logout();
    }

    @Test
    public void upgrade_PerUnitMonth_to_ProRata_Overlap() throws Exception {
        // given
        dateFactorySetTime("2013-01-01 00:00:00");
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        VOSubscription subscription = subscribeToService("PUM_to_PR",
                perUnitMonthService, customerUserId, customerUserKey);
        long perUnitPriceModelKey = getSubscriptionDetails(subscription)
                .getPriceModel().getKey();

        dateFactorySetTime("2013-02-05 00:00:00");
        subscription = upgradeSubscription(subscription, proRataService);

        // when
        dateFactorySetTime("2013-03-20 00:00:00");
        performBillingRun(defaultBillingOffset,
                DateTimeHandling.calculateMillis("2013-03-20 00:00:00"));

        // then
        // check last billing period
        assertPriceModelFactor(subscription, perUnitPriceModelKey, 1.0,
                "2013-02-15 00:00:00", "2013-03-15 00:00:00");
        // check second last billing period
        assertPriceModelFactor(subscription, perUnitPriceModelKey, 1.0,
                "2013-01-15 00:00:00", "2013-02-15 00:00:00");

        container.logout();
    }

    @Test
    public void upgrade_ProRata_to_PerUnitWeek_NoOverlap() throws Exception {
        // given
        dateFactorySetTime("2013-01-01 00:00:00");
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        VOSubscription subscription = subscribeToService("PR_to_PUW",
                proRataService, customerUserId, customerUserKey);

        dateFactorySetTime("2013-02-19 00:00:00");
        subscription = upgradeSubscription(subscription, perUnitWeekService);
        long perUnitPriceModelKey = getSubscriptionDetails(subscription)
                .getPriceModel().getKey();

        // when
        dateFactorySetTime("2013-03-20 00:00:00");
        performBillingRun(defaultBillingOffset,
                DateTimeHandling.calculateMillis("2013-03-20 00:00:00"));

        // then
        // check last billing period
        assertPriceModelFactor(subscription, perUnitPriceModelKey, 3.0,
                "2013-02-15 00:00:00", "2013-03-15 00:00:00");
        // check second last billing period
        assertPriceModelIsNull(subscription, perUnitPriceModelKey,
                "2013-01-15 00:00:00", "2013-02-15 00:00:00");

        container.logout();
    }

    @Test
    public void upgrade_ProRata_to_PerUnitWeek_Overlap1() throws Exception {
        // given
        dateFactorySetTime("2013-01-01 00:00:00");
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        VOSubscription subscription = subscribeToService("PR_to_PUW",
                proRataService, customerUserId, customerUserKey);

        dateFactorySetTime("2013-02-16 00:00:00");
        subscription = upgradeSubscription(subscription, perUnitWeekService);
        long perUnitPriceModelKey = getSubscriptionDetails(subscription)
                .getPriceModel().getKey();

        // when
        dateFactorySetTime("2013-03-20 00:00:00");
        performBillingRun(defaultBillingOffset,
                DateTimeHandling.calculateMillis("2013-03-20 00:00:00"));

        // then
        // check last billing period
        assertPriceModelFactor(subscription, perUnitPriceModelKey, 4.0,
                "2013-02-15 00:00:00", "2013-03-15 00:00:00");
        // check second last billing period
        assertPriceModelIsNull(subscription, perUnitPriceModelKey,
                "2013-01-15 00:00:00", "2013-02-15 00:00:00");

        container.logout();
    }

    @Test
    public void upgrade_ProRata_to_PerUnitWeek_Overlap2() throws Exception {
        // given
        dateFactorySetTime("2013-01-01 00:00:00");
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        VOSubscription subscription = subscribeToService("PR_to_PUW",
                proRataService, customerUserId, customerUserKey);

        dateFactorySetTime("2013-02-12 00:00:00");
        subscription = upgradeSubscription(subscription, perUnitWeekService);
        long perUnitPriceModelKey = getSubscriptionDetails(subscription)
                .getPriceModel().getKey();

        // when
        dateFactorySetTime("2013-03-20 00:00:00");
        performBillingRun(defaultBillingOffset,
                DateTimeHandling.calculateMillis("2013-03-20 00:00:00"));

        // then
        // check last billing period
        assertPriceModelFactor(subscription, perUnitPriceModelKey, 4.0,
                "2013-02-15 00:00:00", "2013-03-15 00:00:00");
        // check second last billing period
        assertPriceModelFactor(subscription, perUnitPriceModelKey, 0.0,
                "2013-01-15 00:00:00", "2013-02-15 00:00:00");

        container.logout();
    }

    @Test
    public void upgrade_ProRata_to_PerUnitWeek_Overlap3() throws Exception {
        // given
        dateFactorySetTime("2013-01-01 00:00:00");
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        VOSubscription subscription = subscribeToService("PR_to_PUW",
                proRataService, customerUserId, customerUserKey);

        dateFactorySetTime("2013-02-05 00:00:00");
        subscription = upgradeSubscription(subscription, perUnitWeekService);
        long perUnitPriceModelKey = getSubscriptionDetails(subscription)
                .getPriceModel().getKey();

        // when
        dateFactorySetTime("2013-03-20 00:00:00");
        performBillingRun(defaultBillingOffset,
                DateTimeHandling.calculateMillis("2013-03-20 00:00:00"));

        // then
        // check last billing period
        assertPriceModelFactor(subscription, perUnitPriceModelKey, 4.0,
                "2013-02-15 00:00:00", "2013-03-15 00:00:00");
        // check second last billing period
        assertPriceModelFactor(subscription, perUnitPriceModelKey, 1.0,
                "2013-01-15 00:00:00", "2013-02-15 00:00:00");

        container.logout();
    }

    @Test
    public void upgrade_ProRata_to_PerUnitMonth_Overlap() throws Exception {
        // given
        dateFactorySetTime("2013-01-01 00:00:00");
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        VOSubscription subscription = subscribeToService("PR_to_PUM",
                proRataService, customerUserId, customerUserKey);

        dateFactorySetTime("2013-02-05 00:00:00");
        subscription = upgradeSubscription(subscription, perUnitMonthService);
        long perUnitPriceModelKey = getSubscriptionDetails(subscription)
                .getPriceModel().getKey();

        // when
        dateFactorySetTime("2013-03-20 00:00:00");
        performBillingRun(defaultBillingOffset,
                DateTimeHandling.calculateMillis("2013-03-20 00:00:00"));

        // then
        // check last billing period
        assertPriceModelFactor(subscription, perUnitPriceModelKey, 1.0,
                "2013-02-15 00:00:00", "2013-03-15 00:00:00");
        // check second last billing period
        assertPriceModelFactor(subscription, perUnitPriceModelKey, 0.0,
                "2013-01-15 00:00:00", "2013-02-15 00:00:00");

        container.logout();
    }

    @Test
    public void upgrade_Free_to_PerUnitWeek_NoOverlap() throws Exception {
        // given
        dateFactorySetTime("2013-01-01 00:00:00");
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        VOSubscription subscription = subscribeToService("FR_to_PUW",
                freeService, customerUserId, customerUserKey);

        dateFactorySetTime("2013-02-19 00:00:00");
        subscription = upgradeSubscription(subscription, perUnitWeekService);
        long perUnitPriceModelKey = getSubscriptionDetails(subscription)
                .getPriceModel().getKey();

        // when
        dateFactorySetTime("2013-03-20 00:00:00");
        performBillingRun(defaultBillingOffset,
                DateTimeHandling.calculateMillis("2013-03-20 00:00:00"));

        // then
        // check last billing period
        assertPriceModelFactor(subscription, perUnitPriceModelKey, 3.0,
                "2013-02-15 00:00:00", "2013-03-15 00:00:00");

        container.logout();
    }

    @Test
    public void upgrade_Free_to_PerUnitWeek_Overlap1() throws Exception {
        // given
        dateFactorySetTime("2013-01-01 00:00:00");
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        VOSubscription subscription = subscribeToService("FR_to_PUW",
                freeService, customerUserId, customerUserKey);

        dateFactorySetTime("2013-02-16 00:00:00");
        subscription = upgradeSubscription(subscription, perUnitWeekService);
        long perUnitPriceModelKey = getSubscriptionDetails(subscription)
                .getPriceModel().getKey();

        // when
        dateFactorySetTime("2013-03-20 00:00:00");
        performBillingRun(defaultBillingOffset,
                DateTimeHandling.calculateMillis("2013-03-20 00:00:00"));

        // then
        // check last billing period
        assertPriceModelFactor(subscription, perUnitPriceModelKey, 4.0,
                "2013-02-15 00:00:00", "2013-03-15 00:00:00");

        container.logout();
    }

    @Test
    public void upgrade_Free_to_PerUnitWeek_Overlap2() throws Exception {
        // given
        dateFactorySetTime("2013-01-01 00:00:00");
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        VOSubscription subscription = subscribeToService("FR_to_PUW",
                freeService, customerUserId, customerUserKey);

        dateFactorySetTime("2013-02-12 00:00:00");
        subscription = upgradeSubscription(subscription, perUnitWeekService);
        long perUnitPriceModelKey = getSubscriptionDetails(subscription)
                .getPriceModel().getKey();

        // when
        dateFactorySetTime("2013-03-20 00:00:00");
        performBillingRun(defaultBillingOffset,
                DateTimeHandling.calculateMillis("2013-03-20 00:00:00"));

        // then
        // check last billing period
        assertPriceModelFactor(subscription, perUnitPriceModelKey, 4.0,
                "2013-02-15 00:00:00", "2013-03-15 00:00:00");
        // check second last billing period
        assertPriceModelFactor(subscription, perUnitPriceModelKey, 0.0,
                "2013-01-15 00:00:00", "2013-02-15 00:00:00");

        container.logout();
    }

    @Test
    public void upgrade_Free_to_PerUnitWeek_Overlap3() throws Exception {
        // given
        dateFactorySetTime("2013-01-01 00:00:00");
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        VOSubscription subscription = subscribeToService("FR_to_PUW",
                freeService, customerUserId, customerUserKey);

        dateFactorySetTime("2013-02-05 00:00:00");
        subscription = upgradeSubscription(subscription, perUnitWeekService);
        long perUnitPriceModelKey = getSubscriptionDetails(subscription)
                .getPriceModel().getKey();

        // when
        dateFactorySetTime("2013-03-20 00:00:00");
        performBillingRun(defaultBillingOffset,
                DateTimeHandling.calculateMillis("2013-03-20 00:00:00"));

        // then
        // check last billing period
        assertPriceModelFactor(subscription, perUnitPriceModelKey, 4.0,
                "2013-02-15 00:00:00", "2013-03-15 00:00:00");
        // check second last billing period
        assertPriceModelFactor(subscription, perUnitPriceModelKey, 1.0,
                "2013-01-15 00:00:00", "2013-02-15 00:00:00");

        container.logout();
    }

    @Test
    public void upgrade_Free_to_PerUnitMonth_Overlap() throws Exception {
        // given
        dateFactorySetTime("2013-01-01 00:00:00");
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        VOSubscription subscription = subscribeToService("FR_to_PUM",
                freeService, customerUserId, customerUserKey);

        dateFactorySetTime("2013-02-05 00:00:00");
        subscription = upgradeSubscription(subscription, perUnitMonthService);
        long perUnitPriceModelKey = getSubscriptionDetails(subscription)
                .getPriceModel().getKey();

        // when
        dateFactorySetTime("2013-03-20 00:00:00");
        performBillingRun(defaultBillingOffset,
                DateTimeHandling.calculateMillis("2013-03-20 00:00:00"));

        // then
        // check last billing period
        assertPriceModelFactor(subscription, perUnitPriceModelKey, 1.0,
                "2013-02-15 00:00:00", "2013-03-15 00:00:00");
        // check second last billing period
        assertPriceModelFactor(subscription, perUnitPriceModelKey, 0.0,
                "2013-01-15 00:00:00", "2013-02-15 00:00:00");

        container.logout();
    }

    @Test
    public void upgrade_Free_to_PerUnitMonth_to_Free_to_PerUnitWeek_Overlap()
            throws Exception {
        // given
        dateFactorySetTime("2013-01-01 00:00:00");
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        VOSubscription subscription = subscribeToService(
                "FR_to_PUM_to_FR_to_PUW", freeService, customerUserId,
                customerUserKey);

        dateFactorySetTime("2013-02-05 00:00:00");
        subscription = upgradeSubscription(subscription, perUnitMonthService);
        long perUnitPriceModelKey1 = getSubscriptionDetails(subscription)
                .getPriceModel().getKey();

        dateFactorySetTime("2013-02-16 00:00:00");
        subscription = upgradeSubscription(subscription, freeService);
        long perUnitPriceModelKey2 = getSubscriptionDetails(subscription)
                .getPriceModel().getKey();

        dateFactorySetTime("2013-02-27 00:00:00");
        subscription = upgradeSubscription(subscription, perUnitWeekService);
        long perUnitPriceModelKey3 = getSubscriptionDetails(subscription)
                .getPriceModel().getKey();
        // when
        dateFactorySetTime("2013-03-20 00:00:00");
        performBillingRun(defaultBillingOffset,
                DateTimeHandling.calculateMillis("2013-03-20 00:00:00"));

        // then
        assertPriceModelFactor(subscription, perUnitPriceModelKey1, 1.0,
                "2013-02-15 00:00:00", "2013-03-15 00:00:00");

        assertPriceModelIsNull(subscription, perUnitPriceModelKey2,
                "2013-02-15 00:00:00", "2013-03-15 00:00:00");

        assertPriceModelFactor(subscription, perUnitPriceModelKey3, 2.0,
                "2013-02-15 00:00:00", "2013-03-15 00:00:00");

        container.logout();
    }

    @Test
    public void upgrade_Free_to_ProRata_to_Free_to_ProRata_Overlap()
            throws Exception {
        // given
        dateFactorySetTime("2013-01-01 00:00:00");
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        VOSubscription subscription = subscribeToService(
                "FR_to_PR_to_FR_to_PR", freeService, customerUserId,
                customerUserKey);

        dateFactorySetTime("2013-02-05 00:00:00");
        subscription = upgradeSubscription(subscription, proRataService);
        long perUnitPriceModelKey1 = getSubscriptionDetails(subscription)
                .getPriceModel().getKey();

        dateFactorySetTime("2013-02-16 00:00:00");
        subscription = upgradeSubscription(subscription, freeService);
        long perUnitPriceModelKey2 = getSubscriptionDetails(subscription)
                .getPriceModel().getKey();

        dateFactorySetTime("2013-02-27 00:00:00");
        subscription = upgradeSubscription(subscription, proRataService);
        long perUnitPriceModelKey3 = getSubscriptionDetails(subscription)
                .getPriceModel().getKey();
        // when
        dateFactorySetTime("2013-03-20 00:00:00");
        performBillingRun(defaultBillingOffset,
                DateTimeHandling.calculateMillis("2013-03-20 00:00:00"));

        // then
        /* Usage 1 day; unit per week => factor = 1 / 7 = 0.142... */
        assertPriceModelFactor(subscription, perUnitPriceModelKey1,
                0.14285714285714285, "2013-02-15 00:00:00",
                "2013-03-15 00:00:00");

        /* Free */
        assertPriceModelIsNull(subscription, perUnitPriceModelKey2,
                "2013-02-15 00:00:00", "2013-03-15 00:00:00");

        /* 15 days; unit per week => factor 15 / 7 = 2.285... */
        assertPriceModelFactor(subscription, perUnitPriceModelKey3,
                2.2857142857142856, "2013-02-15 00:00:00",
                "2013-03-15 00:00:00");

        container.logout();
    }

    @Test
    public void upgrade_PerUnitWeek_to_ProRata_to_PerUnitWeek_NoOverlap()
            throws Exception {
        // given
        dateFactorySetTime("2013-01-01 00:00:00");
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        VOSubscription subscription = subscribeToService("PUW_to_PR_to_PUW",
                perUnitWeekService, customerUserId, customerUserKey);
        long perUnitPriceModelKey1 = getSubscriptionDetails(subscription)
                .getPriceModel().getKey();

        dateFactorySetTime("2013-02-05 00:00:00");
        subscription = upgradeSubscription(subscription, proRataService);

        dateFactorySetTime("2013-02-19 00:00:00");
        subscription = upgradeSubscription(subscription, perUnitWeekService);
        long perUnitPriceModelKey2 = getSubscriptionDetails(subscription)
                .getPriceModel().getKey();

        // when
        dateFactorySetTime("2013-03-20 00:00:00");
        performBillingRun(defaultBillingOffset,
                DateTimeHandling.calculateMillis("2013-03-20 00:00:00"));

        // then
        // check last billing period
        assertPriceModelFactor(subscription, perUnitPriceModelKey2, 3.0,
                "2013-02-15 00:00:00", "2013-03-15 00:00:00");
        // check second last billing period
        assertPriceModelFactor(subscription, perUnitPriceModelKey1, 4.0,
                "2013-01-15 00:00:00", "2013-02-15 00:00:00");

        container.logout();
    }

    @Test
    public void upgrade_PerUnitWeek_to_ProRata_to_PerUnitWeek_Overlap1()
            throws Exception {
        // given
        dateFactorySetTime("2013-01-01 00:00:00");
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        VOSubscription subscription = subscribeToService("PUW_to_PR_to_PUW",
                perUnitWeekService, customerUserId, customerUserKey);
        long perUnitPriceModelKey1 = getSubscriptionDetails(subscription)
                .getPriceModel().getKey();

        dateFactorySetTime("2013-02-05 00:00:00");
        subscription = upgradeSubscription(subscription, proRataService);

        dateFactorySetTime("2013-02-12 00:00:00");
        subscription = upgradeSubscription(subscription, perUnitWeekService);
        long perUnitPriceModelKey2 = getSubscriptionDetails(subscription)
                .getPriceModel().getKey();

        // when
        dateFactorySetTime("2013-03-20 00:00:00");
        performBillingRun(defaultBillingOffset,
                DateTimeHandling.calculateMillis("2013-03-20 00:00:00"));

        // then
        // check last billing period
        assertPriceModelFactor(subscription, perUnitPriceModelKey2, 4.0,
                "2013-02-15 00:00:00", "2013-03-15 00:00:00");
        // check second last billing period
        assertPriceModelFactor(subscription, perUnitPriceModelKey1, 4.0,
                "2013-01-15 00:00:00", "2013-02-15 00:00:00");

        container.logout();
    }

    @Test
    public void upgrade_PerUnitWeek_to_ProRata_to_PerUnitWeek_Overlap2()
            throws Exception {
        // given
        dateFactorySetTime("2013-01-01 00:00:00");
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        VOSubscription subscription = subscribeToService("PUW_to_PR_to_PUW",
                perUnitWeekService, customerUserId, customerUserKey);
        long perUnitPriceModelKey1 = getSubscriptionDetails(subscription)
                .getPriceModel().getKey();

        dateFactorySetTime("2013-02-12 00:00:00");
        subscription = upgradeSubscription(subscription, proRataService);

        dateFactorySetTime("2013-02-13 00:00:00");
        subscription = upgradeSubscription(subscription, perUnitWeekService);
        long perUnitPriceModelKey2 = getSubscriptionDetails(subscription)
                .getPriceModel().getKey();

        // when
        dateFactorySetTime("2013-03-20 00:00:00");
        performBillingRun(defaultBillingOffset,
                DateTimeHandling.calculateMillis("2013-03-20 00:00:00"));

        // then
        // check last billing period
        assertPriceModelFactor(subscription, perUnitPriceModelKey2, 4.0,
                "2013-02-15 00:00:00", "2013-03-15 00:00:00");
        assertPriceModelFactor(subscription, perUnitPriceModelKey1, 1.0,
                "2013-02-15 00:00:00", "2013-03-15 00:00:00");
        // check second last billing period
        assertPriceModelFactor(subscription, perUnitPriceModelKey1, 4.0,
                "2013-01-15 00:00:00", "2013-02-15 00:00:00");

        container.logout();
    }

    @Test
    public void upgrade_PerUnitWeek_to_ProRata_to_PerUnitWeek_Overlap3()
            throws Exception {
        // given
        dateFactorySetTime("2013-01-01 00:00:00");
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        VOSubscription subscription = subscribeToService("PUW_to_PR_to_PUW",
                perUnitWeekService, customerUserId, customerUserKey);
        long perUnitPriceModelKey1 = getSubscriptionDetails(subscription)
                .getPriceModel().getKey();

        dateFactorySetTime("2013-02-12 00:00:00");
        subscription = upgradeSubscription(subscription, proRataService);

        dateFactorySetTime("2013-02-16 00:00:00");
        subscription = upgradeSubscription(subscription, perUnitWeekService);
        long perUnitPriceModelKey2 = getSubscriptionDetails(subscription)
                .getPriceModel().getKey();

        // when
        dateFactorySetTime("2013-03-20 00:00:00");
        performBillingRun(defaultBillingOffset,
                DateTimeHandling.calculateMillis("2013-03-20 00:00:00"));

        // then
        // check last billing period
        assertPriceModelFactor(subscription, perUnitPriceModelKey2, 4.0,
                "2013-02-15 00:00:00", "2013-03-15 00:00:00");
        assertPriceModelFactor(subscription, perUnitPriceModelKey1, 1.0,
                "2013-02-15 00:00:00", "2013-03-15 00:00:00");
        // check second last billing period
        assertPriceModelFactor(subscription, perUnitPriceModelKey1, 4.0,
                "2013-01-15 00:00:00", "2013-02-15 00:00:00");

        container.logout();
    }

    @Test
    public void upgrade_PerUnitWeek_to_ProRata_to_PerUnitWeek_Overlap4()
            throws Exception {
        // given
        dateFactorySetTime("2013-01-01 00:00:00");
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        VOSubscription subscription = subscribeToService("PUW_to_PR_to_PUW",
                perUnitWeekService, customerUserId, customerUserKey);
        long perUnitPriceModelKey1 = getSubscriptionDetails(subscription)
                .getPriceModel().getKey();

        dateFactorySetTime("2013-02-12 00:00:00");
        subscription = upgradeSubscription(subscription, proRataService);

        dateFactorySetTime("2013-02-19 00:00:00");
        subscription = upgradeSubscription(subscription, perUnitWeekService);
        long perUnitPriceModelKey2 = getSubscriptionDetails(subscription)
                .getPriceModel().getKey();

        // when
        dateFactorySetTime("2013-03-20 00:00:00");
        performBillingRun(defaultBillingOffset,
                DateTimeHandling.calculateMillis("2013-03-20 00:00:00"));

        // then
        // check last billing period
        assertPriceModelFactor(subscription, perUnitPriceModelKey2, 3.0,
                "2013-02-15 00:00:00", "2013-03-15 00:00:00");
        assertPriceModelFactor(subscription, perUnitPriceModelKey1, 1.0,
                "2013-02-15 00:00:00", "2013-03-15 00:00:00");
        // check second last billing period
        assertPriceModelFactor(subscription, perUnitPriceModelKey1, 4.0,
                "2013-01-15 00:00:00", "2013-02-15 00:00:00");

        container.logout();
    }

    @Test
    public void upgrade_PerUnitMonth_to_ProRata_to_PerUnitMonth_NoOverlap()
            throws Exception {
        // given
        dateFactorySetTime("2013-01-01 00:00:00");
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        VOSubscription subscription = subscribeToService("PUM_to_PR_to_PUM",
                perUnitMonthService, customerUserId, customerUserKey);
        long perUnitPriceModelKey1 = getSubscriptionDetails(subscription)
                .getPriceModel().getKey();

        dateFactorySetTime("2013-01-29 00:00:00");
        subscription = upgradeSubscription(subscription, proRataService);

        dateFactorySetTime("2013-02-19 00:00:00");
        subscription = upgradeSubscription(subscription, perUnitMonthService);
        long perUnitPriceModelKey2 = getSubscriptionDetails(subscription)
                .getPriceModel().getKey();

        // when
        dateFactorySetTime("2013-03-20 00:00:00");
        performBillingRun(defaultBillingOffset,
                DateTimeHandling.calculateMillis("2013-03-20 00:00:00"));

        // then
        // check last billing period
        assertPriceModelFactor(subscription, perUnitPriceModelKey2, 1.0,
                "2013-02-15 00:00:00", "2013-03-15 00:00:00");
        // check second last billing period
        assertPriceModelFactor(subscription, perUnitPriceModelKey1, 1.0,
                "2013-01-15 00:00:00", "2013-02-15 00:00:00");

        container.logout();
    }

    @Test
    public void upgrade_PerUnitMonth_to_ProRata_to_PerUnitMonth_Overlap()
            throws Exception {
        // given
        dateFactorySetTime("2013-01-01 00:00:00");
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        VOSubscription subscription = subscribeToService("PUM_to_PR_to_PUM",
                perUnitMonthService, customerUserId, customerUserKey);
        long perUnitPriceModelKey1 = getSubscriptionDetails(subscription)
                .getPriceModel().getKey();

        dateFactorySetTime("2013-01-29 00:00:00");
        subscription = upgradeSubscription(subscription, proRataService);

        dateFactorySetTime("2013-02-12 00:00:00");
        subscription = upgradeSubscription(subscription, perUnitMonthService);
        long perUnitPriceModelKey2 = getSubscriptionDetails(subscription)
                .getPriceModel().getKey();

        // when
        dateFactorySetTime("2013-03-20 00:00:00");
        performBillingRun(defaultBillingOffset,
                DateTimeHandling.calculateMillis("2013-03-20 00:00:00"));

        // then
        // check last billing period
        assertPriceModelFactor(subscription, perUnitPriceModelKey2, 1.0,
                "2013-02-15 00:00:00", "2013-03-15 00:00:00");
        // check second last billing period
        assertPriceModelFactor(subscription, perUnitPriceModelKey1, 1.0,
                "2013-01-15 00:00:00", "2013-02-15 00:00:00");

        container.logout();
    }

    @Test
    public void upgrade_PerUnitMonth_to_ProRata_to_PerUnitWeek_Overlap()
            throws Exception {
        // given
        dateFactorySetTime("2013-01-01 00:00:00");
        container.login(customerUserKey, ROLE_ORGANIZATION_ADMIN);
        VOSubscription subscription = subscribeToService("PUM_to_PR_to_PUW",
                perUnitMonthService, customerUserId, customerUserKey);
        long perUnitPriceModelKey1 = getSubscriptionDetails(subscription)
                .getPriceModel().getKey();

        dateFactorySetTime("2013-02-05 00:00:00");
        subscription = upgradeSubscription(subscription, proRataService);

        dateFactorySetTime("2013-02-12 00:00:00");
        subscription = upgradeSubscription(subscription, perUnitWeekService);
        long perUnitPriceModelKey2 = getSubscriptionDetails(subscription)
                .getPriceModel().getKey();

        // when
        dateFactorySetTime("2013-03-20 00:00:00");
        performBillingRun(defaultBillingOffset,
                DateTimeHandling.calculateMillis("2013-03-20 00:00:00"));

        // then
        // check last billing period
        assertPriceModelFactor(subscription, perUnitPriceModelKey2, 4.0,
                "2013-02-15 00:00:00", "2013-03-15 00:00:00");
        assertPriceModelFactor(subscription, perUnitPriceModelKey1, 1.0,
                "2013-02-15 00:00:00", "2013-03-15 00:00:00");
        // check second last billing period
        assertPriceModelFactor(subscription, perUnitPriceModelKey1, 1.0,
                "2013-01-15 00:00:00", "2013-02-15 00:00:00");

        container.logout();
    }

    private void assertPriceModelFactor(VOSubscription subscription,
            long priceModelKey, double factor, String billingPeriodStart,
            String billingPeriodEnd) throws Exception {

        BillingResultEvaluator billingResultEvaluator = billingResultEvaluatorFactory(
                subscription, billingPeriodStart, billingPeriodEnd);
        billingResultEvaluator.assertPeriodFeeFactor(priceModelKey, factor);
    }

    private void assertPriceModelIsNull(VOSubscription subscription,
            long priceModelKey, String billingPeriodStart,
            String billingPeriodEnd) throws Exception {

        BillingResultEvaluator billingResultEvaluator = billingResultEvaluatorFactory(
                subscription, billingPeriodStart, billingPeriodEnd);
        billingResultEvaluator.assertPriceModelNodeNull(priceModelKey);
    }

    private BillingResultEvaluator billingResultEvaluatorFactory(
            VOSubscription subscription, String billingPeriodStart,
            String billingPeriodEnd) throws Exception {

        long billingPeriodStart_long = DateTimeHandling
                .calculateMillis(billingPeriodStart);
        long billingPeriodEnd_long = DateTimeHandling
                .calculateMillis(billingPeriodEnd);

        Document billingResult = getBillingResult(subscription.getKey(),
                billingPeriodStart_long, billingPeriodEnd_long);

        return new BillingResultEvaluator(billingResult);
    }

    private VOSubscriptionDetails getSubscriptionDetails(
            VOSubscription subscription) throws ObjectNotFoundException,
            OperationNotPermittedException {
        return container.get(SubscriptionService.class).getSubscriptionDetails(
                subscription.getSubscriptionId());
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

    private static VOSubscription upgradeSubscription(
            final VOSubscription subscription, final VOService serviceToUpgrade)
            throws Exception {

        return runTX(new Callable<VOSubscription>() {

            @Override
            public VOSubscription call() throws Exception {

                AccountService accountService = container
                        .get(AccountService.class);
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
                                paymentInfo, billingContact,
                                new ArrayList<VOUda>());
                return newSubscription;
            }
        });
    }

    private static VOSubscription subscribeToService(
            final String subscriptionNamePrefix, final VOService service,
            final String userId, final long userKey) throws Exception {

        return runTX(new Callable<VOSubscription>() {

            @Override
            public VOSubscription call() throws Exception {

                AccountService accountService = container
                        .get(AccountService.class);
                SubscriptionService subscriptionService = container
                        .get(SubscriptionService.class);

                VOSubscription subscription = createSubscriptionVO(subscriptionNamePrefix);

                VORoleDefinition roleDefinition = technicalService
                        .getRoleDefinitions().get(0);
                List<VOUsageLicense> users = null;
                if (userId.length() != 0) {
                    users = createUsageLicenceVO(roleDefinition, userId,
                            userKey);
                }

                VOBillingContact billingContact = accountService
                        .saveBillingContact(createBillingContactVO());
                container.get(DataService.class).flush();
                VOPaymentInfo paymentInfo = null;

                if (service.getPriceModel().getType() != PriceModelType.FREE_OF_CHARGE) {
                    paymentInfo = accountService.getPaymentInfos().get(0);
                } else {
                    billingContact = null;
                }

                subscription = subscriptionService.subscribeToService(
                        subscription, service, users, paymentInfo,
                        billingContact, new ArrayList<VOUda>());

                return subscription;
            }
        });
    }

    private static VOBillingContact createBillingContactVO() {
        VOBillingContact voBillingContact = new VOBillingContact();
        voBillingContact.setAddress("Customer Road");
        voBillingContact.setCompanyName("Customer Company");
        voBillingContact.setEmail("customer@fujitsu.com");
        voBillingContact.setOrgAddressUsed(true);
        voBillingContact.setId("billingId_" + System.currentTimeMillis());
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
        subscription.setSubscriptionId(namePrefix + "_"
                + System.currentTimeMillis());
        return subscription;
    }

    private static void dateFactorySetTime(String timeToSet) {
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.setTime(new Date(DateTimeHandling.calculateMillis(timeToSet)));
        DateFactory.setInstance(new TestDateFactory(cal.getTime()));
    }

    private static void setBillingRunOffset(final int offsetInDays)
            throws Exception {
        Long offsetInMs = new Long(offsetInDays * 24 * 3600 * 1000L);
        ConfigurationServiceLocal configurationService = container
                .get(ConfigurationServiceLocal.class);

        ConfigurationSetting config = new ConfigurationSetting(
                ConfigurationKey.TIMER_INTERVAL_BILLING_OFFSET,
                Configuration.GLOBAL_CONTEXT, offsetInMs.toString());
        configurationService.setConfigurationSetting(config);
    }

    private static void performBillingRun(final int billingOffset,
            final long invocationTime) throws Exception {

        runTX(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                setBillingRunOffset(billingOffset);
                container.get(DataService.class).flush();

                BillingServiceLocal billingService = container
                        .get(BillingServiceLocal.class);
                billingService.startBillingRun(invocationTime);
                return null;
            }
        });
    }
}
