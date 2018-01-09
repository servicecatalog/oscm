/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: Dec 20, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.calculation.revenue.setup;

import java.math.BigDecimal;

import org.oscm.billingservice.setup.BillingIntegrationTestBase;
import org.oscm.billingservice.setup.CustomerData;
import org.oscm.billingservice.setup.IntegrationTestSetup;
import org.oscm.billingservice.setup.TestData;
import org.oscm.billingservice.setup.TestOrganizationSetup;
import org.oscm.billingservice.setup.VOPriceModelFactory;
import org.oscm.billingservice.setup.VOPriceModelFactory.TestPriceModel;
import org.oscm.billingservice.setup.VOServiceFactory;
import org.oscm.billingservice.setup.VOServiceFactory.TestService;
import org.oscm.billingservice.setup.VendorData;
import org.oscm.test.DateTimeHandling;
import org.oscm.test.ReflectiveClone;
import org.oscm.internal.types.enumtypes.UserRoleType;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.internal.vo.VOPriceModel;
import org.oscm.internal.vo.VORoleDefinition;
import org.oscm.internal.vo.VOServiceDetails;
import org.oscm.internal.vo.VOSubscriptionDetails;
import org.oscm.internal.vo.VOUser;
import org.oscm.internal.vo.VOUserDetails;

/**
 * @author baumann
 */
public class BugSetup extends IntegrationTestSetup {

    public void createBug10339() throws Exception {
        long supplierKey = basicSetup.getSupplierAdminKey();
        long customerAdminKey = basicSetup.getCustomerAdminKey();
        VOUser customerAdmin = basicSetup.getCustomerAdmin();
        VOUserDetails user = basicSetup.getCustomerUser1();

        BillingIntegrationTestBase
                .setDateFactoryInstance("2013-05-01 12:00:00");
        setCutOffDay(supplierKey, 1);
        VOServiceDetails service = serviceSetup
                .createPublishAndActivateMarketableService(supplierKey,
                        "Bug10339", TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_MONTH_ROLES_PARS,
                        technicalService, supplierMarketplace);

        login("2013-06-01 08:00:00", customerAdminKey, ROLE_ORGANIZATION_ADMIN);
        VORoleDefinition role = VOServiceFactory.getRole(service, "USER");
        VOSubscriptionDetails subscription = subscrSetup.subscribeToService(
                "Bug10339", service, customerAdmin, role);

        subscription = subscrSetup.addUser("2013-06-10 20:00:00",
                subscription.getSubscriptionId(), user,
                VOServiceFactory.getRole(service, "GUEST"));

        subscription = subscrSetup.modifyParameter("2013-06-15 12:00:00",
                subscription, "HAS_OPTIONS", "2");

        BillingIntegrationTestBase.addToCache(subscription);
        resetCutOffDay(supplierKey);
    }

    public void createBug10267_free() throws Exception {
        // create subscription
        BillingIntegrationTestBase
                .setDateFactoryInstance("2013-03-07 07:00:00");
        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(), "Bug10267_free",
                        TestService.EXAMPLE, TestPriceModel.FREE,
                        technicalService, supplierMarketplace);
        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);
        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "Bug10267_free", serviceDetails, basicSetup.getCustomerUser1(),
                role);
        BillingIntegrationTestBase.addToCache(subDetails);

        resetCutOffDay(basicSetup.getSupplierAdminKey());
    }

    public void createBug10267() throws Exception {
        // create subscription
        BillingIntegrationTestBase
                .setDateFactoryInstance("2013-03-07 07:00:00");
        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(), "createBug10267_1",
                        TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_MONTH_ROLES,
                        technicalService, supplierMarketplace);
        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);
        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup
                .subscribeToService("Bug10267", serviceDetails,
                        basicSetup.getCustomerUser1(), role);
        BillingIntegrationTestBase.addToCache(subDetails);

        // upgrade to free of charge
        VOServiceDetails freeService = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(), "createBug10267_2",
                        TestService.EXAMPLE, TestPriceModel.FREE,
                        technicalService, supplierMarketplace);
        serviceSetup.registerCompatibleServices(
                basicSetup.getSupplierAdminKey(), serviceDetails, freeService);
        BillingIntegrationTestBase
                .setDateFactoryInstance("2013-03-10 07:00:00");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(subDetails, freeService);
        BillingIntegrationTestBase.addToCache(upgradedSubDetails);

        resetCutOffDay(basicSetup.getSupplierAdminKey());
    }

    public void createMonthScenarioBug10091() throws Exception {
        long usageStartTime = DateTimeHandling
                .calculateMillis("2013-03-07 07:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(usageStartTime);

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "BUG10091_PERUNIT_MONTH", TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_MONTH_ROLES,
                        technicalService, supplierMarketplace);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 10);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "BUG10091_PERUNIT_MONTH", serviceDetails,
                basicSetup.getCustomerUser1(), role);

        // create upgraded service
        VOServiceDetails freeService = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "BUG10091_MONTH_FREE_SERVICE", TestService.EXAMPLE,
                        TestPriceModel.FREE, technicalService,
                        supplierMarketplace);

        serviceSetup.registerCompatibleServices(
                basicSetup.getSupplierAdminKey(), serviceDetails, freeService);

        long usageUpgradeTime = DateTimeHandling
                .calculateMillis("2013-03-09 07:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(usageUpgradeTime);
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(subDetails, freeService);

        long usageEndTime = DateTimeHandling
                .calculateMillis("2013-03-11 07:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(usageEndTime);
        subscrSetup
                .unsubscribeToService(upgradedSubDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "BUG10091_PERUNIT_MONTH", subDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "BUG10091_PERUNIT_MONTH", upgradedSubDetails);
    }

    public void createWeekScenarioBug10091() throws Exception {
        long usageStartTime = DateTimeHandling
                .calculateMillis("2013-04-08 07:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(usageStartTime);

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "BUG10091_PERUNIT_WEEK", TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_WEEK_ROLES_PARS,
                        technicalService, supplierMarketplace);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 10);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "BUG10091_PERUNIT_WEEK", serviceDetails,
                basicSetup.getCustomerUser1(), role);

        // create upgraded service
        VOServiceDetails freeService = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "BUG10091_WEEK_FREE_SERVICE", TestService.EXAMPLE,
                        TestPriceModel.FREE, technicalService,
                        supplierMarketplace);

        serviceSetup.registerCompatibleServices(
                basicSetup.getSupplierAdminKey(), serviceDetails, freeService);

        long usageUpgradeTime = DateTimeHandling
                .calculateMillis("2013-04-09 07:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(usageUpgradeTime);
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(subDetails, freeService);

        long usageEndTime = DateTimeHandling
                .calculateMillis("2013-04-15 10:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(usageEndTime);
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "BUG10091_PERUNIT_WEEK", subDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "BUG10091_PERUNIT_WEEK", upgradedSubDetails);
    }

    public void createWeekScenarioBug10091_Freep() throws Exception {
        long usageStartTime = DateTimeHandling
                .calculateMillis("2013-04-08 07:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(usageStartTime);

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "BUG10091_PU_WEEK_FREEP", TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_WEEK_ROLES_PARS_FREEP_2,
                        technicalService, supplierMarketplace);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 10);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "BUG10091_PU_WEEK_FREEP", serviceDetails,
                basicSetup.getCustomerUser1(), role);

        // create upgraded service
        VOServiceDetails freeService = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "BUG10091_WEEK_FREE_SERVIC", TestService.EXAMPLE,
                        TestPriceModel.FREE, technicalService,
                        supplierMarketplace);

        serviceSetup.registerCompatibleServices(
                basicSetup.getSupplierAdminKey(), serviceDetails, freeService);

        long usageUpgradeTime = DateTimeHandling
                .calculateMillis("2013-04-09 20:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(usageUpgradeTime);
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(subDetails, freeService);

        long usageEndTime = DateTimeHandling
                .calculateMillis("2013-04-15 10:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(usageEndTime);
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "BUG10091_PU_WEEK_FREEP", subDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "BUG10091_PU_WEEK_FREEP", upgradedSubDetails);
    }

    public void createWeekScenarioBug10269_Freep() throws Exception {
        long usageStartTime = DateTimeHandling
                .calculateMillis("2013-04-08 07:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(usageStartTime);

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "BUG10269_PERUNIT_WEEK_FREEP", TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_WEEK_ROLES_PARS_FREEP,
                        technicalService, supplierMarketplace);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 11);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "BUG10269_PERUNIT_WEEK_FREEP", serviceDetails,
                basicSetup.getCustomerUser1(), role);

        // create upgraded service
        VOServiceDetails paidService = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "BUG10269_2_WEEK_FREE_SERVICE", TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_WEEK_ROLES,
                        technicalService, supplierMarketplace);

        serviceSetup.registerCompatibleServices(
                basicSetup.getSupplierAdminKey(), serviceDetails, paidService);

        long usageUpgradeTime = DateTimeHandling
                .calculateMillis("2013-04-09 20:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(usageUpgradeTime);
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(subDetails, paidService);

        long usageEndTime = DateTimeHandling
                .calculateMillis("2013-04-15 10:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(usageEndTime);
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "BUG10269_PERUNIT_WEEK_FREEP", subDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "BUG10269_PERUNIT_WEEK_FREEP", upgradedSubDetails);
    }

    public void createWeekScenarioBug10269_Rata_Freep() throws Exception {
        long usageStartTime = DateTimeHandling
                .calculateMillis("2013-04-08 07:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(usageStartTime);

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "BUG10269_RATA_WEEK_FREEP", TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_WEEK_ROLES_PARS_FREEP,
                        technicalService, supplierMarketplace);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 11);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "BUG10269_RATA_WEEK_FREEP", serviceDetails,
                basicSetup.getCustomerUser1(), role);

        // create upgraded service
        VOServiceDetails paidService = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "BUG10269_3_WEEK_FREE_SERVICE", TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_RATA_WEEK_ROLES,
                        technicalService, supplierMarketplace);

        serviceSetup.registerCompatibleServices(
                basicSetup.getSupplierAdminKey(), serviceDetails, paidService);

        long usageUpgradeTime = DateTimeHandling
                .calculateMillis("2013-04-09 20:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(usageUpgradeTime);
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(subDetails, paidService);

        long usageEndTime = DateTimeHandling
                .calculateMillis("2013-04-15 10:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(usageEndTime);
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "BUG10269_RATA_WEEK_FREEP", subDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "BUG10269_RATA_WEEK_FREEP", upgradedSubDetails);
    }

    public void createWeekScenarioBug10269_2_Rata_Freep() throws Exception {
        long usageStartTime = DateTimeHandling
                .calculateMillis("2013-04-08 07:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(usageStartTime);

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "BUG10269_2_RATA_WEEK_FREEP", TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_WEEK_ROLES_PARS_FREEP,
                        technicalService, supplierMarketplace);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "BUG10269_2_RATA_WEEK_FREEP", serviceDetails,
                basicSetup.getCustomerUser1(), role);

        // create upgraded service
        VOServiceDetails paidService = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "BUG10269_2-2_WEEK_FREE_SERVICE", TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_RATA_WEEK_ROLES,
                        technicalService, supplierMarketplace);

        serviceSetup.registerCompatibleServices(
                basicSetup.getSupplierAdminKey(), serviceDetails, paidService);

        long usageUpgradeTime = DateTimeHandling
                .calculateMillis("2013-04-10 20:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(usageUpgradeTime);
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(subDetails, paidService);

        long usageEndTime = DateTimeHandling
                .calculateMillis("2013-04-14 10:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(usageEndTime);
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "BUG10269_2_RATA_WEEK_FREEP", subDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "BUG10269_2_RATA_WEEK_FREEP", upgradedSubDetails);
    }

    public void createWeekScenarioBug10091_Rata() throws Exception {
        long usageStartTime = DateTimeHandling
                .calculateMillis("2013-04-08 07:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(usageStartTime);

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(), "BUG10091_RATA_WEEK",
                        TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_RATA_WEEK_ROLES_PARS,
                        technicalService, supplierMarketplace);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 10);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "BUG10091_RATA_WEEK", serviceDetails,
                basicSetup.getCustomerUser1(), role);

        // create upgraded service
        VOServiceDetails freeService = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "BUG10091_WEEK_FREE_SERVICE_Rata", TestService.EXAMPLE,
                        TestPriceModel.FREE, technicalService,
                        supplierMarketplace);

        serviceSetup.registerCompatibleServices(
                basicSetup.getSupplierAdminKey(), serviceDetails, freeService);

        long usageUpgradeTime = DateTimeHandling
                .calculateMillis("2013-04-09 07:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(usageUpgradeTime);
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(subDetails, freeService);

        long usageEndTime = DateTimeHandling
                .calculateMillis("2013-04-15 10:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(usageEndTime);
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "BUG10091_RATA_WEEK", subDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "BUG10091_RATA_WEEK", upgradedSubDetails);
    }

    public void createWeekScenarioBug10133() throws Exception {
        long usageStartTime = DateTimeHandling
                .calculateMillis("2013-04-30 07:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(usageStartTime);

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "BUG10133_PERUNIT_WEEK", TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_WEEK_ROLES_PARS,
                        technicalService, supplierMarketplace);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 3);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "BUG10133_PERUNIT_WEEK", serviceDetails,
                basicSetup.getCustomerUser1(), role);

        long usageEndTime = DateTimeHandling
                .calculateMillis("2013-05-01 10:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(usageEndTime);
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "BUG10133_PERUNIT_WEEK", subDetails);
    }

    public void createWeekScenarioBug10133_Rata() throws Exception {
        long usageStartTime = DateTimeHandling
                .calculateMillis("2013-04-30 07:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(usageStartTime);

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(), "BUG10133_RATA_WEEK",
                        TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_RATA_WEEK_ROLES_PARS,
                        technicalService, supplierMarketplace);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 3);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "BUG10133_RATA_WEEK", serviceDetails,
                basicSetup.getCustomerUser1(), role);

        long usageEndTime = DateTimeHandling
                .calculateMillis("2013-05-01 10:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(usageEndTime);
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "BUG10133_RATA_WEEK", subDetails);
    }

    public void createWeekScenarioBug10133_2() throws Exception {
        long usageStartTime = DateTimeHandling
                .calculateMillis("2013-04-28 07:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(usageStartTime);

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "BUG10133_2_PERUNIT_WEEK", TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_WEEK_ROLES_PARS,
                        technicalService, supplierMarketplace);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 3);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "BUG10133_2_PERUNIT_WEEK", serviceDetails,
                basicSetup.getCustomerUser1(), role);

        long usageEndTime = DateTimeHandling
                .calculateMillis("2013-05-01 10:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(usageEndTime);
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "BUG10133_2_PERUNIT_WEEK", subDetails);
    }

    public void createWeekScenarioBug10221_with_free_period() throws Exception {
        long usageStartTime = DateTimeHandling
                .calculateMillis("2013-04-28 07:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(usageStartTime);

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "BUG10221_FREE_PERIOD_UNIT_WEEK", TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_WEEK_FREEP,
                        technicalService, supplierMarketplace);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 3);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "BUG10221_FREE_PERIOD_UNIT_WEEK", serviceDetails,
                basicSetup.getCustomerUser1(), role);

        long usageEndTime = DateTimeHandling
                .calculateMillis("2013-05-01 10:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(usageEndTime);
        // subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "BUG10221_FREE_PERIOD_UNIT_WEEK", subDetails);
    }

    public void createWeekBug10221_free_period_and_event() throws Exception {
        long usageStartTime = DateTimeHandling
                .calculateMillis("2013-04-28 07:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(usageStartTime);

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "BUG10221_FREE_UNIT_WEEK_EVENT", TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_WEEK_ROLES_EVENTS_FREEP,
                        technicalService, supplierMarketplace);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 3);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "BUG10221_FREE_UNIT_WEEK_EVENT", serviceDetails,
                basicSetup.getCustomerUser1(), role);

        // record one event after 3 days
        container.login(basicSetup.getSupplierAdminKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        subscrSetup.recordEventForSubscription(subDetails, usageStartTime
                + DateTimeHandling.daysToMillis(3), "FILE_DOWNLOAD", 2);

        // record other event after 6 days
        subscrSetup.recordEventForSubscription(subDetails, usageStartTime
                + DateTimeHandling.daysToMillis(6), "FILE_UPLOAD", 2);

        long usageEndTime = DateTimeHandling
                .calculateMillis("2013-05-04 10:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(usageEndTime);
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "BUG10221_FREE_UNIT_WEEK_EVENT", subDetails);
    }

    public void createWeekScenarioBug10221_with_free_period_Rata()
            throws Exception {
        long usageStartTime = DateTimeHandling
                .calculateMillis("2013-04-28 07:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(usageStartTime);

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "BUG10221_FREE_PER_RATA_WEEK", TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_RATA_WEEK_ROLES_PARS_FREEP,
                        technicalService, supplierMarketplace);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 3);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "BUG10221_FREE_PER_RATA_WEEK", serviceDetails,
                basicSetup.getCustomerUser1(), role);

        long usageEndTime = DateTimeHandling
                .calculateMillis("2013-05-01 10:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(usageEndTime);
        // subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "BUG10221_FREE_PER_RATA_WEEK", subDetails);
    }

    public void createWeekScenarioBug10221_with_free_period_2()
            throws Exception {
        long usageStartTime = DateTimeHandling
                .calculateMillis("2013-04-28 07:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(usageStartTime);

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "BUG10221_FREE_PERIOD_UNIT_WEEK_2",
                        TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_WEEK_FREEP,
                        technicalService, supplierMarketplace);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 4);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "BUG10221_FREE_PERIOD_UNIT_WEEK_2", serviceDetails,
                basicSetup.getCustomerUser1(), role);

        long usageEndTime = DateTimeHandling
                .calculateMillis("2013-05-01 10:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(usageEndTime);
        // subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "BUG10221_FREE_PERIOD_UNIT_WEEK_2", subDetails);
    }

    public void createWeekScenarioBug10221_with_free_period_3()
            throws Exception {
        long usageStartTime = DateTimeHandling
                .calculateMillis("2013-04-29 07:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(usageStartTime);

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "BUG10221_FREE_PERIOD_UNIT_WEEK_3",
                        TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_WEEK_FREEP,
                        technicalService, supplierMarketplace);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 4);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "BUG10221_FREE_PERIOD_UNIT_WEEK_3", serviceDetails,
                basicSetup.getCustomerUser1(), role);

        long usageEndTime = DateTimeHandling
                .calculateMillis("2013-05-01 10:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(usageEndTime);
        // subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "BUG10221_FREE_PERIOD_UNIT_WEEK_3", subDetails);
    }

    public void createWeekScenarioBug10235_with_free_period() throws Exception {
        long usageStartTime = DateTimeHandling
                .calculateMillis("2013-04-29 07:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(usageStartTime);

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "BUG10235_FREE_PERIOD_UNIT_WEEK", TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_WEEK_ROLES_FREEP,
                        technicalService, supplierMarketplace);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 4);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "BUG10235_FREE_PERIOD_UNIT_WEEK", serviceDetails,
                basicSetup.getCustomerUser1(), role);

        // user is assigned
        long userAssignedTime = DateTimeHandling
                .calculateMillis("2013-04-30 10:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(userAssignedTime);
        VORoleDefinition changedRole = VOServiceFactory.getRole(serviceDetails,
                "USER");
        subscrSetup.addUser(basicSetup.getSecondCustomerUser1(), changedRole,
                subDetails.getSubscriptionId());

        // user is deassigned
        long userDeassignedTime = DateTimeHandling
                .calculateMillis("2013-05-01 10:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(userDeassignedTime);
        subscrSetup.revokeUser(basicSetup.getSecondCustomerUser1(),
                subDetails.getSubscriptionId());

        long usageEndTime = DateTimeHandling
                .calculateMillis("2013-05-01 10:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(usageEndTime);
        // subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "BUG10235_FREE_PERIOD_UNIT_WEEK", subDetails);

    }

    public void createWeekScenarioBug10235_with_free_period_2()
            throws Exception {
        long usageStartTime = DateTimeHandling
                .calculateMillis("2013-04-29 07:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(usageStartTime);

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "BUG10235_FREE_PERIOD_UNIT_WEEK_2",
                        TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_WEEK_ROLES_FREEP_3,
                        technicalService, supplierMarketplace);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 4);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "BUG10235_FREE_PERIOD_UNIT_WEEK_2", serviceDetails,
                basicSetup.getCustomerUser1(), role);

        // user is assigned
        long userAssignedTime = DateTimeHandling
                .calculateMillis("2013-04-30 10:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(userAssignedTime);
        VORoleDefinition changedRole = VOServiceFactory.getRole(serviceDetails,
                "USER");
        subscrSetup.addUser(basicSetup.getSecondCustomerUser1(), changedRole,
                subDetails.getSubscriptionId());

        // user is deassigned
        long userDeassignedTime = DateTimeHandling
                .calculateMillis("2013-05-02 10:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(userDeassignedTime);
        subscrSetup.revokeUser(basicSetup.getSecondCustomerUser1(),
                subDetails.getSubscriptionId());

        long usageEndTime = DateTimeHandling
                .calculateMillis("2013-05-02 10:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(usageEndTime);
        // subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "BUG10235_FREE_PERIOD_UNIT_WEEK_2", subDetails);

    }

    public void createWeekScenarioBug10235_with_free_period_Rata()
            throws Exception {
        long usageStartTime = DateTimeHandling
                .calculateMillis("2013-04-29 07:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(usageStartTime);

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "BUG10235_FREE_PERIOD_RATA_WEEK", TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_RATA_WEEK_ROLES_PARS_FREEP,
                        technicalService, supplierMarketplace);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 4);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "BUG10235_FREE_PERIOD_RATA_WEEK", serviceDetails,
                basicSetup.getCustomerUser1(), role);

        // user is assigned
        long userAssignedTime = DateTimeHandling
                .calculateMillis("2013-04-30 10:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(userAssignedTime);
        VORoleDefinition changedRole = VOServiceFactory.getRole(serviceDetails,
                "USER");
        subscrSetup.addUser(basicSetup.getSecondCustomerUser1(), changedRole,
                subDetails.getSubscriptionId());

        // user is deassigned
        long userDeassignedTime = DateTimeHandling
                .calculateMillis("2013-05-01 10:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(userDeassignedTime);
        subscrSetup.revokeUser(basicSetup.getSecondCustomerUser1(),
                subDetails.getSubscriptionId());

        long usageEndTime = DateTimeHandling
                .calculateMillis("2013-05-01 10:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(usageEndTime);
        // subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "BUG10235_FREE_PERIOD_RATA_WEEK", subDetails);

    }

    public void createWeek_free_period_stepPriceUser() throws Exception {
        long usageStartTime = DateTimeHandling
                .calculateMillis("2013-04-28 07:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(usageStartTime);

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "FREE_STEPPED_USER_WEEK",
                        TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_WEEK_ROLES_STEPPED_FREEP,
                        technicalService, supplierMarketplace);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 3);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "FREE_STEPPED_USER_WEEK", serviceDetails,
                basicSetup.getCustomerUser1(), role);

        // 2. user is assigned
        long userAssignedTime = DateTimeHandling
                .calculateMillis("2013-04-28 10:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(userAssignedTime);
        VORoleDefinition changedRole = VOServiceFactory.getRole(serviceDetails,
                "USER");
        subscrSetup.addUser(basicSetup.getCustomerUser2(), changedRole,
                subDetails.getSubscriptionId());

        // 3. user is assigned
        userAssignedTime = DateTimeHandling
                .calculateMillis("2013-04-28 10:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(userAssignedTime);
        changedRole = VOServiceFactory.getRole(serviceDetails, "USER");
        subscrSetup.addUser(basicSetup.getCustomerUser3(), changedRole,
                subDetails.getSubscriptionId());

        // 4. user is assigned
        userAssignedTime = DateTimeHandling
                .calculateMillis("2013-04-28 10:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(userAssignedTime);
        changedRole = VOServiceFactory.getRole(serviceDetails, "USER");
        subscrSetup.addUser(basicSetup.getCustomerUser4(), changedRole,
                subDetails.getSubscriptionId());

        // 5. user is assigned
        userAssignedTime = DateTimeHandling
                .calculateMillis("2013-04-28 10:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(userAssignedTime);
        changedRole = VOServiceFactory.getRole(serviceDetails, "USER");
        subscrSetup.addUser(basicSetup.getCustomerUser5(), changedRole,
                subDetails.getSubscriptionId());

        // 6. user is assigned
        userAssignedTime = DateTimeHandling
                .calculateMillis("2013-04-28 10:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(userAssignedTime);
        changedRole = VOServiceFactory.getRole(serviceDetails, "USER");
        subscrSetup.addUser(basicSetup.getCustomerUser6(), changedRole,
                subDetails.getSubscriptionId());

        // 2. user is deassigned
        long userDeassignedTime = DateTimeHandling
                .calculateMillis("2013-04-29 10:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(userDeassignedTime);
        subscrSetup.revokeUser(basicSetup.getCustomerUser2(),
                subDetails.getSubscriptionId());

        long usageEndTime = DateTimeHandling
                .calculateMillis("2013-05-04 10:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(usageEndTime);
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "FREE_STEPPED_USER_WEEK", subDetails);

    }

    /**
     * Bug 10249: Subscribe to a service with priced events. One event has
     * stepped prices.
     */
    public void createMonthScenarioBug10249_perUnit_steppedEvents()
            throws Exception {
        long usageStartTime = DateTimeHandling
                .calculateMillis("2012-12-01 00:00:00")
                - DateTimeHandling.daysToMillis(3.5);
        BillingIntegrationTestBase.setDateFactoryInstance(usageStartTime);

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "BUG10249_PER_UNIT_MONTH_EVENTS", TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_MONTH_STEPPED_EVENTS,
                        technicalService, supplierMarketplace);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "BUG10249_PER_UNIT_MONTH_EVENTS", serviceDetails,
                basicSetup.getCustomerUser1(), role);

        // record an event after 8 days
        container.login(basicSetup.getSupplierAdminKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        subscrSetup.recordEventForSubscription(subDetails, usageStartTime
                + DateTimeHandling.daysToMillis(8), "FILE_DOWNLOAD", 75);

        // record another event after 10 days
        subscrSetup.recordEventForSubscription(subDetails, usageStartTime
                + DateTimeHandling.daysToMillis(10), "FILE_UPLOAD", 13);

        // record another event after 12 days
        subscrSetup.recordEventForSubscription(subDetails, usageStartTime
                + DateTimeHandling.daysToMillis(10), "FOLDER_NEW", 1);

        long usageEndTime = DateTimeHandling
                .calculateMillis("2012-12-01 00:00:00")
                + DateTimeHandling.daysToMillis(10);
        BillingIntegrationTestBase.setDateFactoryInstance(usageEndTime);
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "BUG10249_PER_UNIT_MONTH_EVENTS", subDetails);
    }

    public void createWeekScenarioRolChangeWithFreeP() throws Exception {
        long usageStartTime = DateTimeHandling
                .calculateMillis("2013-02-28 07:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(usageStartTime);

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "ROLCHANGE_WEEK_FREEP", TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_WEEK_ROLES_FREEP_2,
                        technicalService, supplierMarketplace);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");

        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "ROLCHANGE_WEEK_FREEP", serviceDetails,
                basicSetup.getCustomerUser1(), role);

        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-03-01 00:00:00"));
        subDetails = subscrSetup.modifyUserRole(subDetails.getUsageLicenses()
                .get(0), VOServiceFactory.getRole(serviceDetails, "USER"),
                subDetails.getSubscriptionId());

        long usageEndTime = DateTimeHandling
                .calculateMillis("2013-03-03 07:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(usageEndTime);
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "ROLCHANGE_WEEK_FREEP", subDetails);
    }

    /**
     * Change parameter two times in free period. Price model is per unit/week,
     * charged week overlaps billing period.
     */
    public void createWeekScenarioBug10265_ParChangeWithFreeP()
            throws Exception {
        long usageStartTime = DateTimeHandling
                .calculateMillis("2013-02-28 07:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(usageStartTime);

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "RARCHANGE_WEEK_FREEP", TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_WEEK_ROLES_PARS_FREEP,
                        technicalService, supplierMarketplace);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");

        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "RARCHANGE_WEEK_FREEP", serviceDetails,
                basicSetup.getCustomerUser1(), role);

        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-03-01 00:00:00"));
        subDetails = subscrSetup.modifyParameterForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00"),
                "MAX_FOLDER_NUMBER", "7");
        subDetails = subscrSetup.modifyParameterForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-03-01 01:00:00"),
                "MAX_FOLDER_NUMBER", "3");

        long usageEndTime = DateTimeHandling
                .calculateMillis("2013-03-03 07:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(usageEndTime);
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "RARCHANGE_WEEK_FREEP", subDetails);
    }

    /**
     * Upgrade a service with a free period to another service with a free
     * period. Both services have a per unit/week price model. Change a
     * parameter in both free periods. Charged week overlaps billing period.
     */
    public void createWeekScenarioBug10265_UpgradeAndParChange()
            throws Exception {
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-02-23 00:00:00"));

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "BUG10265_UPG_PARCHG", TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_WEEK_ROLES_PARS2, 3,
                        technicalService, supplierMarketplace);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);

        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                basicSetup.getCustomerAdminKey(), "BUG10265_UPG_PARCHG",
                serviceDetails, basicSetup.getCustomerUser1(),
                VOServiceFactory.getRole(serviceDetails, "ADMIN"));

        subDetails = subscrSetup.modifyParameterForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-02-25 00:00:00"),
                "MAX_FOLDER_NUMBER", "3");

        // Upgrade the subscription
        VOServiceDetails perUnitService = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "BUG10265_UPG_PARCHG_SERVICE2", TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_WEEK_ROLES_PARS3, 2,
                        technicalService, supplierMarketplace);

        serviceSetup.registerCompatibleServices(
                basicSetup.getSupplierAdminKey(), serviceDetails,
                perUnitService);

        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-02-28 00:00:00"));
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .copyParametersAndUpgradeSubscription(
                        basicSetup.getCustomerAdminKey(), subDetails,
                        perUnitService);

        upgradedSubDetails = subscrSetup.modifyParameterForSubscription(
                upgradedSubDetails,
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00"),
                "MAX_FOLDER_NUMBER", "5");

        upgradedSubDetails = subscrSetup.modifyParameterForSubscription(
                upgradedSubDetails,
                DateTimeHandling.calculateMillis("2013-03-01 07:00:00"),
                "MAX_FOLDER_NUMBER", "7");

        upgradedSubDetails = subscrSetup.modifyParameterForSubscription(
                upgradedSubDetails,
                DateTimeHandling.calculateMillis("2013-03-02 12:00:00"),
                "MAX_FOLDER_NUMBER", "4");

        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-03-03 07:00:00"));
        subscrSetup
                .unsubscribeToService(upgradedSubDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "BUG10265_UPG_PARCHG", subDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "BUG10265_UPG_PARCHG", upgradedSubDetails);
    }

    /**
     * Upgrade a service with a free period to another service without a free
     * period. Both services have a per unit/week price model. Change a
     * parameter in the free period and after it. Charged week overlaps billing
     * period.
     */
    public void createWeekScenarioBug10265_UpgradeAndParChange2()
            throws Exception {
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-02-23 00:00:00"));

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "BUG10265_UPG_PARCHG2", TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_WEEK_ROLES_PARS2, 3,
                        technicalService, supplierMarketplace);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);

        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                basicSetup.getCustomerAdminKey(), "BUG10265_UPG_PARCHG2",
                serviceDetails, basicSetup.getCustomerUser1(),
                VOServiceFactory.getRole(serviceDetails, "ADMIN"));

        subDetails = subscrSetup.modifyParameterForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-02-25 00:00:00"),
                "MAX_FOLDER_NUMBER", "3");

        // Upgrade the subscription
        VOServiceDetails perUnitService = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "BUG10265_UPG_PARCHG2_SERVICE2", TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_WEEK_ROLES_PARS3, 0,
                        technicalService, supplierMarketplace);

        serviceSetup.registerCompatibleServices(
                basicSetup.getSupplierAdminKey(), serviceDetails,
                perUnitService);

        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-02-28 00:00:00"));
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .copyParametersAndUpgradeSubscription(
                        basicSetup.getCustomerAdminKey(), subDetails,
                        perUnitService);

        upgradedSubDetails = subscrSetup.modifyParameterForSubscription(
                upgradedSubDetails,
                DateTimeHandling.calculateMillis("2013-03-01 00:00:00"),
                "MAX_FOLDER_NUMBER", "5");

        upgradedSubDetails = subscrSetup.modifyParameterForSubscription(
                upgradedSubDetails,
                DateTimeHandling.calculateMillis("2013-03-01 07:00:00"),
                "MAX_FOLDER_NUMBER", "7");

        upgradedSubDetails = subscrSetup.modifyParameterForSubscription(
                upgradedSubDetails,
                DateTimeHandling.calculateMillis("2013-03-02 12:00:00"),
                "MAX_FOLDER_NUMBER", "4");

        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-03-03 07:00:00"));
        subscrSetup
                .unsubscribeToService(upgradedSubDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "BUG10265_UPG_PARCHG2", subDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "BUG10265_UPG_PARCHG2", upgradedSubDetails);
    }

    public void createWeekSuspend() throws Exception {
        long usageStartTime = DateTimeHandling
                .calculateMillis("2013-06-08 07:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(usageStartTime);

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(), "SCENARIO_WEEK_SUSP",
                        TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_WEEK_ROLES,
                        technicalService, supplierMarketplace);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);

        // Create own customer, because the customer's payment types are changed
        // later on
        String customerAdminId = "ScenarioSuspWeekCustomerAdmin";
        VOOrganization customer = orgSetup.registerCustomer(
                "ScenarioWeekSuspCustomer",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                customerAdminId, supplierMarketplace.getMarketplaceId(),
                basicSetup.getSupplierOrgID());
        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "SCENARIO_WEEK_SUSP", serviceDetails, customerAdmin, role);

        // suspend/resume subscription several times by removing/restoring the
        // customer's payment types
        long suspResTime = DateTimeHandling
                .calculateMillis("2013-06-09 07:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(suspResTime);

        container.login(basicSetup.getSupplierAdminKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);

        paymentSetup.deleteCustomerPaymentTypes(customer);

        long reassignResTime = DateTimeHandling
                .calculateMillis("2013-06-17 07:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(reassignResTime);
        paymentSetup.reassignCustomerPaymentTypes(customer);

        // price model change!!
        VOPriceModel newSubPriceModel = VOPriceModelFactory
                .modifyPriceModelPeriodFee(subDetails.getPriceModel(),
                        new BigDecimal("200.00"));
        subscrSetup.savePriceModelForSubscription(
                basicSetup.getSupplierAdminKey(), subDetails, newSubPriceModel,
                customer);

        long usageEndTime = DateTimeHandling
                .calculateMillis("2013-06-18 10:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(usageEndTime);

        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO_WEEK_SUSP", subDetails);
        BillingIntegrationTestBase.updateCustomerListForTests(
                "SCENARIO_WEEK_SUSP", customer);
    }

    public void createBug10301() throws Exception {
        long usageStartTime = DateTimeHandling
                .calculateMillis("2013-05-05 20:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(usageStartTime);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);

        // Create own customer
        String customerAdminId = "BUGxxCustomer";
        VOOrganization customer = orgSetup.registerCustomer("BUGxxCustomerOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                customerAdminId, supplierMarketplace.getMarketplaceId(),
                basicSetup.getSupplierOrgID());

        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(), "BUG10301",
                        TestService.EXAMPLE, TestPriceModel.FREE,
                        technicalService, supplierMarketplace);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");

        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "BUG10301", serviceDetails, customerAdmin, role);

        // role change

        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-05-14 10:00:00"));
        subDetails = subscrSetup.modifyUserRole(subDetails.getUsageLicenses()
                .get(0), VOServiceFactory.getRole(serviceDetails, "USER"),
                subDetails.getSubscriptionId());

        // create upgraded service
        VOServiceDetails paidService = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(), "BUG10301_Upgrade",
                        TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_MONTH_ROLES,
                        technicalService, supplierMarketplace);

        serviceSetup.registerCompatibleServices(
                basicSetup.getSupplierAdminKey(), serviceDetails, paidService);

        long usageUpgradeTime = DateTimeHandling
                .calculateMillis("2013-05-15 20:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(usageUpgradeTime);
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(subDetails, paidService);

        serviceSetup.registerCompatibleServices(
                basicSetup.getSupplierAdminKey(), serviceDetails, paidService);

        // change sunbscriptionID
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-05-16 00:00:00"));
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        upgradedSubDetails.setSubscriptionId("BUG10301" + "_SubID2");
        upgradedSubDetails = subscrSetup.modifySubscription(upgradedSubDetails,
                null);

        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests("BUG10301",
                subDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests("BUG10301",
                upgradedSubDetails);

        BillingIntegrationTestBase.updateCustomerListForTests("BUG10301",
                customer);

    }

    public void createPerUnitDayBug10302() throws Exception {
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-03-01 00:00:00"));

        setCutOffDay(basicSetup.getSupplierAdminKey(), 7);

        // Create an own customer
        String customerAdminId = "Bug10302PUnitDayCustomer";
        VOOrganization customer = orgSetup.registerCustomer(
                "Bug10302PUnitDayCustomerOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_UK,
                customerAdminId, supplierMarketplace.getMarketplaceId(),
                basicSetup.getSupplierOrgID());
        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "BUG10302_PERUNIT_DAY", TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PICT04_PERUNIT_DAY,
                        technicalService, supplierMarketplace);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "GUEST");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "BUG10302_PERUNIT_DAY", serviceDetails, customerAdmin, role);

        // Suspend and resume the subscription by deleting the customer's
        // payment types
        container.login(basicSetup.getSupplierAdminKey(),
                UserRoleType.SERVICE_MANAGER.name());
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-03-07 12:00:00"));
        paymentSetup.deleteCustomerPaymentTypes(customer);
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-03-07 18:00:00"));
        paymentSetup.reassignCustomerPaymentTypes(customer);
        subDetails = subscrSetup.getSubscriptionDetails(customerAdmin.getKey(),
                subDetails.getSubscriptionId());

        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-03-11 00:00:00"));
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "BUG10302_PERUNIT_DAY", subDetails);
        BillingIntegrationTestBase.updateCustomerListForTests(
                "BUG10302_PERUNIT_DAY", customer);
    }

    public void createBug10303() throws Exception {
        long usageStartTime = DateTimeHandling
                .calculateMillis("2013-05-05 20:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(usageStartTime);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);

        // Create own customer
        String customerAdminId = "BUG10303Customer";
        VOOrganization customer = orgSetup.registerCustomer("BUGxxCustomerOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                customerAdminId, supplierMarketplace.getMarketplaceId(),
                basicSetup.getSupplierOrgID());

        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(), "BUG10303",
                        TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_MONTH_ROLES,
                        technicalService, supplierMarketplace);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");

        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "BUG10303", serviceDetails, customerAdmin, role);
        // create upgraded service
        VOServiceDetails paidService = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(), "BUG10303_Upgrade",
                        TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_MONTH_ROLES,
                        technicalService, supplierMarketplace);

        serviceSetup.registerCompatibleServices(
                basicSetup.getSupplierAdminKey(), serviceDetails, paidService);

        long usageUpgradeTime = DateTimeHandling
                .calculateMillis("2013-05-8 20:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(usageUpgradeTime);
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(subDetails, paidService);

        serviceSetup.registerCompatibleServices(
                basicSetup.getSupplierAdminKey(), serviceDetails, paidService);

        // // suspend/resume subscription several times by removing/restoring
        // // customer's payment types
        long suspResTime = DateTimeHandling
                .calculateMillis("2013-05-11 07:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(suspResTime);

        container.login(basicSetup.getSupplierAdminKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);

        paymentSetup.deleteCustomerPaymentTypes(customer);

        long reassignResTime = DateTimeHandling
                .calculateMillis("2013-05-12 07:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(reassignResTime);
        paymentSetup.reassignCustomerPaymentTypes(customer);

        upgradedSubDetails = subscrSetup.getSubscriptionDetails(
                customerAdmin.getKey(), upgradedSubDetails.getSubscriptionId());

        // change sunbscriptionID
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-05-14 00:00:00"));
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        upgradedSubDetails.setSubscriptionId("BUG10301" + "_SubID2");
        upgradedSubDetails = subscrSetup.modifySubscription(upgradedSubDetails,
                null);

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests("BUG10303",
                subDetails);

        BillingIntegrationTestBase.updateSubscriptionListForTests("BUG10303",
                upgradedSubDetails);

        BillingIntegrationTestBase.updateCustomerListForTests("BUG10303",
                customer);

    }

    public void createDayBug10361_DaylightSavingTime() throws Exception {
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-03-15 00:00:00"));

        setCutOffDay(basicSetup.getSupplierAdminKey(), 7);

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(), "BUG10361_DAYLSAV",
                        TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_FP_PUDAY_ROLES_PAR_EVENTS,
                        technicalService, supplierMarketplace);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "BUG10361_DAYLSAV", serviceDetails,
                basicSetup.getCustomerUser1(), role);

        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-03-31 00:00:00"));
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "BUG10361_DAYLSAV", subDetails);
    }

    public void createDayBug10361_DaylSav_ParChange() throws Exception {
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-03-16 00:00:00"));

        setCutOffDay(basicSetup.getSupplierAdminKey(), 7);

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "BUG10361_DLS_PAR_CHANGE", TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_FP_PUDAY_ROLES_PAR_EVENTS,
                        technicalService, supplierMarketplace);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "BUG10361_DLS_PAR_CHANGE", serviceDetails,
                basicSetup.getCustomerUser1(), role);

        subDetails = subscrSetup.modifyParameterForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-03-31 01:00:00"),
                "MAX_FOLDER_NUMBER", "4");

        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-04-01 00:00:00"));
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "BUG10361_DLS_PAR_CHANGE", subDetails);
    }

    public void createBug10404_suspendUpgradedProRataService() throws Exception {
        setDateFactory("2013-08-07 10:00:00");

        setCutOffDay(basicSetup.getSupplierAdminKey(), 10);

        // Create an own customer, because the customer's payment types
        // are changed later on
        String customerAdminId = "Bug10404Customer";
        VOOrganization customer = orgSetup.registerCustomer(
                "Bug10404CustomerOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                customerAdminId, supplierMarketplace.getMarketplaceId(),
                basicSetup.getSupplierOrgID());
        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "BUG10404_UPGR_SUS_SERVICE", TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_WEEK_ROLES,
                        technicalService, supplierMarketplace);

        // subscribe
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "BUG10404_UPGR_SUS", serviceDetails, customerAdmin,
                VOServiceFactory.getRole(serviceDetails, "GUEST"));

        // upgrade service
        VOServiceDetails upgradedServiceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "Bug10404_Upgrade_Service", TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_RATA_WEEK_ROLES,
                        technicalService, supplierMarketplace);
        serviceSetup.registerCompatibleServices(
                basicSetup.getSupplierAdminKey(), serviceDetails,
                upgradedServiceDetails);

        setDateFactory("2013-08-08 13:00:00");
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(customerAdmin.getKey(), subDetails,
                        upgradedServiceDetails);

        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-08-09 00:00:00"));
        upgradedSubDetails = subscrSetup.modifyUserRole(upgradedSubDetails
                .getUsageLicenses().get(0), VOServiceFactory.getRole(
                upgradedServiceDetails, "ADMIN"), upgradedSubDetails
                .getSubscriptionId());

        // Suspend and resume the subscription by deleting the customer's
        // payment types
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-08-09 18:00:00"));
        paymentSetup.deleteCustomerPaymentTypes(
                basicSetup.getSupplierAdminKey(), customer);
        upgradedSubDetails = subscrSetup.getSubscriptionDetails(
                customerAdmin.getKey(), subDetails.getSubscriptionId());

        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-08-14 20:00:00"));
        paymentSetup.reassignCustomerPaymentTypes(
                basicSetup.getSupplierAdminKey(), customer);
        upgradedSubDetails = subscrSetup.getSubscriptionDetails(
                customerAdmin.getKey(), subDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "BUG10404_UPGR_SUS", subDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "BUG10404_UPGR_SUS", upgradedSubDetails);

        BillingIntegrationTestBase.updateCustomerListForTests(
                "BUG10404_UPGR_SUS", customer);
    }

    public void createBug10404_upgradeExpiredSubscription() throws Exception {
        setDateFactory("2013-08-07 10:00:00");

        setCutOffDay(basicSetup.getSupplierAdminKey(), 10);

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "BUG10404_UPGR_EXP_SUB", TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_WEEK_ROLES,
                        technicalService, supplierMarketplace);

        // subscribe
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                basicSetup.getCustomerAdminKey(), "BUG10404_UPGR_EXP_SUB",
                serviceDetails, basicSetup.getCustomerUser1(),
                VOServiceFactory.getRole(serviceDetails, "ADMIN"));

        // expire the subscription
        setDateFactory("2013-08-12 13:00:00");
        subscrSetup.expireSubscription(subDetails, basicSetup.getCustomer());
        subDetails = subscrSetup.getSubscriptionDetails(
                basicSetup.getCustomerAdminKey(),
                subDetails.getSubscriptionId());

        // upgrade service
        VOServiceDetails upgradedServiceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "Bug10404_UpgradeExp_Service", TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_RATA_WEEK_ROLES,
                        technicalService, supplierMarketplace);
        serviceSetup.registerCompatibleServices(
                basicSetup.getSupplierAdminKey(), serviceDetails,
                upgradedServiceDetails);

        setDateFactory("2013-08-20 10:00:00");
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(basicSetup.getCustomerAdminKey(),
                        subDetails, upgradedServiceDetails);

        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-08-30 15:20:00"));
        subscrSetup.unsubscribeToService(basicSetup.getCustomerAdminKey(),
                upgradedSubDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "BUG10404_UPGR_EXP_SUB", subDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "BUG10404_UPGR_EXP_SUB", upgradedSubDetails);
    }

    public void createBug10404_expireSuspendedSubscription() throws Exception {
        setDateFactory("2013-08-07 10:00:00");

        setCutOffDay(basicSetup.getSupplierAdminKey(), 10);

        // Create an own customer, because the customer's payment types
        // are changed later on
        String customerAdminId = "Bug10404ExpSusCustomer";
        VOOrganization customer = orgSetup.registerCustomer(
                "Bug10404ExpSusCustomerOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                customerAdminId, supplierMarketplace.getMarketplaceId(),
                basicSetup.getSupplierOrgID());
        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "BUG10404_EXP_SUS_SUB_SERVICE", TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_WEEK_ROLES,
                        technicalService, supplierMarketplace);

        // subscribe
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                customerAdmin.getKey(), "BUG10404_EXP_SUS_SUB", serviceDetails,
                customerAdmin,
                VOServiceFactory.getRole(serviceDetails, "ADMIN"));

        // Suspend the subscription
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-08-12 13:00:00"));
        paymentSetup.deleteCustomerPaymentTypes(
                basicSetup.getSupplierAdminKey(), customer);
        subDetails = subscrSetup.getSubscriptionDetails(customerAdmin.getKey(),
                subDetails.getSubscriptionId());

        // expire the subscription
        setDateFactory("2013-08-13 10:00:00");
        subscrSetup.expireSubscription(subDetails, customer);
        subDetails = subscrSetup.getSubscriptionDetails(customerAdmin.getKey(),
                subDetails.getSubscriptionId());

        // Reassign the customer's payment types
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-08-18 18:00:00"));
        paymentSetup.reassignCustomerPaymentTypes(
                basicSetup.getSupplierAdminKey(), customer);
        subDetails = subscrSetup.getSubscriptionDetails(customerAdmin.getKey(),
                subDetails.getSubscriptionId());

        // upgrade service
        VOServiceDetails upgradedServiceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "Bug10404_SusExpUpgr_Service", TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_RATA_WEEK_ROLES,
                        technicalService, supplierMarketplace);
        serviceSetup.registerCompatibleServices(
                basicSetup.getSupplierAdminKey(), serviceDetails,
                upgradedServiceDetails);

        setDateFactory("2013-08-20 10:00:00");
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(customerAdmin.getKey(), subDetails,
                        upgradedServiceDetails);

        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-08-30 15:20:00"));
        subscrSetup.unsubscribeToService(customerAdmin.getKey(),
                upgradedSubDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "BUG10404_EXP_SUS_SUB", subDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "BUG10404_EXP_SUS_SUB", upgradedSubDetails);
    }

    /**
     * Upgrade a suspended subscription to a free service
     */
    public void createBug10476_upgradeSuspendedSubscription() throws Exception {
        setDateFactory("2013-08-02 10:00:00");

        // Create own customer, because the customer's payment types
        // are changed later on
        String customerAdminId = "Bug10476UpgrSusCustomerAdmin";
        VOOrganization customer = orgSetup.registerCustomer(
                "Bug10476UpgrSusCustomer",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                customerAdminId, supplierMarketplace.getMarketplaceId(),
                basicSetup.getSupplierOrgID());
        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "BUG10476_UPGRSUS_SERVICE", TestService.EXAMPLE2,
                        TestPriceModel.EXAMPLE_PERUNIT_WEEK_ROLES,
                        technicalService, supplierMarketplace);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 3);

        // subscribe
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                customerAdmin.getKey(), "BUG10476_UPGRSUS", serviceDetails,
                customerAdmin,
                VOServiceFactory.getRole(serviceDetails, "GUEST"));

        // Delete customer payment types
        setDateFactory("2013-08-03 00:00:00");
        paymentSetup.deleteCustomerPaymentTypes(
                basicSetup.getSupplierAdminKey(), customer);
        subDetails = subscrSetup.getSubscriptionDetails(customerAdmin.getKey(),
                subDetails.getSubscriptionId());

        // Create upgraded service
        setDateFactory("2013-08-04 12:00:00");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOServiceDetails upgradedServiceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "BUG10476_UPGRSUS_FREE_SERVICE", TestService.EXAMPLE2,
                        TestPriceModel.FREE, technicalService,
                        supplierMarketplace);

        serviceSetup.registerCompatibleServices(
                basicSetup.getSupplierAdminKey(), serviceDetails,
                upgradedServiceDetails);

        // Upgrade subscription -> subscription is activated because
        // it is free!!
        setDateFactory("2013-08-10 23:10:00");
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(customerAdmin.getKey(), subDetails,
                        upgradedServiceDetails);

        setDateFactory("2013-08-11 15:00:00");
        paymentSetup.reassignCustomerPaymentTypes(
                basicSetup.getSupplierAdminKey(), customer);
        upgradedSubDetails = subscrSetup.getSubscriptionDetails(
                customerAdmin.getKey(), upgradedSubDetails.getSubscriptionId());

        // Terminate subscription
        setDateFactory("2013-08-15 08:12:00");
        subscrSetup
                .unsubscribeToService(upgradedSubDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "BUG10476_UPGRSUS", subDetails);

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "BUG10476_UPGRSUS", upgradedSubDetails);

        BillingIntegrationTestBase.updateCustomerListForTests(
                "BUG10476_UPGRSUS", customer);
    }

    /**
     * Same as createBug10476_upgradeSuspendedSubscription(), but suspend and
     * upgrade happens in the same time unit.
     */
    public void createBug10476_upgradeSuspendedSubscription2() throws Exception {
        setDateFactory("2013-08-02 10:00:00");

        // Create own customer, because the customer's payment types
        // are changed later on
        String customerAdminId = "Bug10476UpgrSus2CustomerAdmin";
        VOOrganization customer = orgSetup.registerCustomer(
                "Bug10476UpgrSus2Customer",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                customerAdminId, supplierMarketplace.getMarketplaceId(),
                basicSetup.getSupplierOrgID());
        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "BUG10476_UPGRSUS2_SERVICE", TestService.EXAMPLE2,
                        TestPriceModel.EXAMPLE_PERUNIT_WEEK_ROLES,
                        technicalService, supplierMarketplace);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 3);

        // subscribe
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                customerAdmin.getKey(), "BUG10476_UPGRSUS2", serviceDetails,
                customerAdmin,
                VOServiceFactory.getRole(serviceDetails, "GUEST"));

        // Delete customer payment types
        setDateFactory("2013-08-03 00:00:00");
        paymentSetup.deleteCustomerPaymentTypes(
                basicSetup.getSupplierAdminKey(), customer);
        subDetails = subscrSetup.getSubscriptionDetails(customerAdmin.getKey(),
                subDetails.getSubscriptionId());

        // Create upgraded service
        setDateFactory("2013-08-04 12:00:00");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOServiceDetails upgradedServiceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "BUG10476_UPGRSUS2_FREE_SERVICE", TestService.EXAMPLE2,
                        TestPriceModel.FREE, technicalService,
                        supplierMarketplace);

        serviceSetup.registerCompatibleServices(
                basicSetup.getSupplierAdminKey(), serviceDetails,
                upgradedServiceDetails);

        // Upgrade subscription -> subscription is activated because
        // it is free!!
        setDateFactory("2013-08-04 23:10:00");
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(customerAdmin.getKey(), subDetails,
                        upgradedServiceDetails);

        setDateFactory("2013-08-11 15:00:00");
        paymentSetup.reassignCustomerPaymentTypes(
                basicSetup.getSupplierAdminKey(), customer);
        upgradedSubDetails = subscrSetup.getSubscriptionDetails(
                customerAdmin.getKey(), upgradedSubDetails.getSubscriptionId());

        // Terminate subscription
        setDateFactory("2013-08-15 08:12:00");
        subscrSetup
                .unsubscribeToService(upgradedSubDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "BUG10476_UPGRSUS2", subDetails);

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "BUG10476_UPGRSUS2", upgradedSubDetails);

        BillingIntegrationTestBase.updateCustomerListForTests(
                "BUG10476_UPGRSUS2", customer);
    }

    /**
     * Upgrade a suspended subscription to a free service. The subscription is
     * suspended by deleting the billing contact of the customer.
     */
    public void createBug11021_upgradeSuspendedSubscription() throws Exception {
        setDateFactory("2013-08-02 10:00:00");

        // Create own customer, because the customer's payment types
        // are changed later on
        String customerAdminId = "Bug11021UpgrSusCustomerAdmin";
        VOOrganization customer = orgSetup.registerCustomer(
                "Bug11021UpgrSusCustomer",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                customerAdminId, supplierMarketplace.getMarketplaceId(),
                basicSetup.getSupplierOrgID());
        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "BUG11021_UPGRSUS_SERVICE", TestService.EXAMPLE2,
                        TestPriceModel.EXAMPLE_PERUNIT_WEEK_ROLES,
                        technicalService, supplierMarketplace);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 3);

        // subscribe
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                customerAdmin.getKey(), "BUG11021_UPGRSUS", serviceDetails,
                customerAdmin,
                VOServiceFactory.getRole(serviceDetails, "GUEST"));

        // Delete the customer's billing contacts -> subscription is suspended
        setDateFactory("2013-08-03 00:00:00");
        subDetails = deleteBillingContactsAndUpdateSub(customerAdmin.getKey(),
                subDetails);

        // Create upgraded service
        setDateFactory("2013-08-04 12:00:00");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOServiceDetails upgradedServiceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "BUG11021_UPGRSUS_FREE_SERVICE", TestService.EXAMPLE2,
                        TestPriceModel.FREE, technicalService,
                        supplierMarketplace);

        serviceSetup.registerCompatibleServices(
                basicSetup.getSupplierAdminKey(), serviceDetails,
                upgradedServiceDetails);

        // Upgrade subscription -> subscription is activated because
        // it is free!!
        setDateFactory("2013-08-10 23:10:00");
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(customerAdmin.getKey(), subDetails,
                        upgradedServiceDetails);

        // Terminate subscription
        setDateFactory("2013-08-15 08:12:00");
        subscrSetup
                .unsubscribeToService(upgradedSubDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "BUG11021_UPGRSUS", subDetails);

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "BUG11021_UPGRSUS", upgradedSubDetails);

        BillingIntegrationTestBase.updateCustomerListForTests(
                "BUG11021_UPGRSUS", customer);
    }

    public void bug11822_changePurchaseOrderNumber() throws Exception {
        VendorData supplierData = setupNewSupplier("2015-05-01 08:00:00");
        setCutOffDay(supplierData.getAdminKey(), 1);
        CustomerData customerData = registerCustomer(supplierData);

        VOServiceDetails serviceDetails = createPublishActivateService(
                supplierData, TestPriceModel.EXAMPLE_PERUNIT_MONTH_ROLES,
                "BUG11822_PERUNIT_MONTH_SERVICE");

        VOSubscriptionDetails subDetails = subscribe(
                customerData.getAdminUser(), "BUG11822_CHANGE_PON",
                "My subscription 4711", serviceDetails,
                DateTimeHandling.calculateMillis("2015-06-01 00:00:00"),
                "ADMIN");

        // Change Purchase Order Number
        VOSubscriptionDetails modifiedSubDetails = (VOSubscriptionDetails) (ReflectiveClone
                .clone(subDetails));
        modifiedSubDetails
                .setPurchaseOrderNumber("My new subscription reference 8888");
        modifiedSubDetails = modifySubscription(customerData.getAdminUser(),
                modifiedSubDetails, null,
                DateTimeHandling.calculateMillis("2015-06-30 00:00:00"));

        unsubscribe(customerData.getAdminUser(),
                modifiedSubDetails.getSubscriptionId(), "2015-07-01 00:00:00");

        resetCutOffDay(supplierData.getAdminKey());
        cacheTestData("BUG11822_CHANGE_PON", new TestData(supplierData));
    }

    public void bug11822_changePurchaseOrderNumber_afterUpgrade()
            throws Exception {
        VendorData supplierData = setupNewSupplier("2015-05-01 08:00:00");
        setCutOffDay(supplierData.getAdminKey(), 1);
        CustomerData customerData = registerCustomer(supplierData);

        VOServiceDetails serviceDetails = createPublishActivateService(
                supplierData, TestPriceModel.EXAMPLE_PERUNIT_MONTH_ROLES,
                "BUG11822_PERUNIT_MONTH_SERVICE2");

        VOSubscriptionDetails subDetails = subscribe(
                customerData.getAdminUser(),
                "BUG11822_CHANGE_PON_AFTER_UPGRADE", "My subscription 4712",
                serviceDetails,
                DateTimeHandling.calculateMillis("2015-06-01 00:00:00"),
                "ADMIN");

        // create upgrade service
        VOServiceDetails upgradeService = createAndRegisterCompatibleService(
                supplierData, TestPriceModel.EXAMPLE_PERUNIT_MONTH_ROLES,
                serviceDetails, "BUG11822_PERUNIT_MONTH_UPGRADE_SERVICE");

        // upgrade subscription
        VOSubscriptionDetails upgradedSubDetails = upgrade(
                customerData.getAdminUser(), subDetails, upgradeService,
                DateTimeHandling.calculateMillis("2015-06-16 00:00:00"));

        // Change Purchase Order Number
        VOSubscriptionDetails modifiedSubDetails = (VOSubscriptionDetails) (ReflectiveClone
                .clone(upgradedSubDetails));
        modifiedSubDetails
                .setPurchaseOrderNumber("My new subscription reference 1313");
        modifySubscription(customerData.getAdminUser(), modifiedSubDetails,
                null, DateTimeHandling.calculateMillis("2015-06-30 00:00:00"));

        resetCutOffDay(supplierData.getAdminKey());
        cacheTestData("BUG11822_CHANGE_PON_AFTER_UPGRADE", new TestData(
                supplierData));
    }

}
