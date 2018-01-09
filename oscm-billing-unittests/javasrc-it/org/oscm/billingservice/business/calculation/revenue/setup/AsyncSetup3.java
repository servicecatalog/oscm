/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: Dec 20, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.calculation.revenue.setup;

import org.oscm.billingservice.setup.BillingIntegrationTestBase;
import org.oscm.billingservice.setup.IntegrationTestSetup;
import org.oscm.billingservice.setup.TestOrganizationSetup;
import org.oscm.billingservice.setup.VOPriceModelFactory.TestPriceModel;
import org.oscm.billingservice.setup.VOServiceFactory;
import org.oscm.billingservice.setup.VOServiceFactory.TestService;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.internal.vo.VORoleDefinition;
import org.oscm.internal.vo.VOServiceDetails;
import org.oscm.internal.vo.VOSubscriptionDetails;
import org.oscm.internal.vo.VOUser;

/**
 * @author baumann
 */
public class AsyncSetup3 extends IntegrationTestSetup {

    public void createAsyncScenario29() throws Exception {
        // create subscription
        setDateFactory("2014-03-10 10:15:00");

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(), "SCENARIO_29",
                        TestService.EXAMPLE_ASYNC,
                        TestPriceModel.EXAMPLE_RATA_WEEK_ROLES,
                        technicalServiceAsync, supplierMarketplace);
        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);
        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "SCENARIO_29", serviceDetails, basicSetup.getCustomerUser1(),
                role);
        // ASYNC
        setDateFactory("2014-03-15 10:20:00");
        subDetails = subscrSetup.completeAsyncSubscription(
                basicSetup.getSupplierAdminKey(),
                basicSetup.getCustomerAdmin(), subDetails);

        // upgrade to pro_rata of charge
        VOServiceDetails subDetails2 = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(), "SCENARIO_29_2",
                        TestService.EXAMPLE_ASYNC,
                        TestPriceModel.EXAMPLE_PERUNIT_WEEK_ROLES,
                        technicalServiceAsync, supplierMarketplace);
        serviceSetup.registerCompatibleServices(
                basicSetup.getSupplierAdminKey(), serviceDetails, subDetails2);
        setDateFactory("2014-03-29 10:20:00");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(subDetails, subDetails2);
        // ASYNC
        setDateFactory("2014-03-31 10:20:00");
        upgradedSubDetails = subscrSetup.completeAsyncUpgradeSubscription(
                basicSetup.getSupplierAdminKey(),
                basicSetup.getCustomerAdmin(), upgradedSubDetails);

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO_29", subDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO_29", upgradedSubDetails);

    }

    public void createAsyncScenario30() throws Exception {
        // create subscription
        setDateFactory("2013-03-10 10:15:00");

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(), "SCENARIO_30",
                        TestService.EXAMPLE_ASYNC,
                        TestPriceModel.EXAMPLE_PERUNIT_WEEK_ROLES,
                        technicalServiceAsync, supplierMarketplace);
        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);
        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "SCENARIO_30", serviceDetails, basicSetup.getCustomerUser1(),
                role);
        // ASYNC
        setDateFactory("2013-03-15 10:20:00");
        subDetails = subscrSetup.completeAsyncSubscription(
                basicSetup.getSupplierAdminKey(),
                basicSetup.getCustomerAdmin(), subDetails);

        // upgrade to pro_rata of charge
        VOServiceDetails subDetails2 = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(), "SCENARIO_30_2",
                        TestService.EXAMPLE_ASYNC,
                        TestPriceModel.EXAMPLE_RATA_WEEK_ROLES,
                        technicalServiceAsync, supplierMarketplace);
        serviceSetup.registerCompatibleServices(
                basicSetup.getSupplierAdminKey(), serviceDetails, subDetails2);
        setDateFactory("2013-03-30 10:20:00");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(subDetails, subDetails2);
        // ASYNC
        setDateFactory("2013-04-10 10:20:00");
        upgradedSubDetails = subscrSetup.completeAsyncUpgradeSubscription(
                basicSetup.getSupplierAdminKey(),
                basicSetup.getCustomerAdmin(), upgradedSubDetails);

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO_30", subDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO_30", upgradedSubDetails);

    }

    public void createAsyncScenario31() throws Exception {
        // create subscription
        setDateFactory("2013-03-28 10:15:00");

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(), "SCENARIO_31",
                        TestService.EXAMPLE_ASYNC,
                        TestPriceModel.EXAMPLE_PERUNIT_MONTH_ROLES_1,
                        technicalServiceAsync, supplierMarketplace);
        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);
        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "SCENARIO_31", serviceDetails, basicSetup.getCustomerUser1(),
                role);
        // ASYNC
        setDateFactory("2013-03-28 10:20:00");
        subDetails = subscrSetup.completeAsyncSubscription(
                basicSetup.getSupplierAdminKey(),
                basicSetup.getCustomerAdmin(), subDetails);

        // upgrade to pro_rata of charge
        VOServiceDetails subDetails2 = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(), "SCENARIO_31_2",
                        TestService.EXAMPLE_ASYNC,
                        TestPriceModel.EXAMPLE_PRORATA_MONTH_ROLES,
                        technicalServiceAsync, supplierMarketplace);
        serviceSetup.registerCompatibleServices(
                basicSetup.getSupplierAdminKey(), serviceDetails, subDetails2);
        setDateFactory("2013-03-30 10:20:00");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(subDetails, subDetails2);
        // ASYNC
        setDateFactory("2013-04-02 10:20:00");
        subscrSetup.abortAsyncUpgradeSubscription(basicSetup
                .getSupplierAdminKey(), upgradedSubDetails.getSubscriptionId(),
                basicSetup.getCustomerAdmin().getOrganizationId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO_31", subDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO_31", upgradedSubDetails);

    }

    public void createAsyncScenario31_V2() throws Exception {
        // create subscription
        // with cut of day 15
        setDateFactory("2013-03-10 10:15:00");

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(), "SCENARIO_31_V2",
                        TestService.EXAMPLE_ASYNC,
                        TestPriceModel.EXAMPLE_PERUNIT_MONTH_ROLES_2,
                        technicalServiceAsync, supplierMarketplace);
        setCutOffDay(basicSetup.getSupplierAdminKey(), 15);
        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "SCENARIO_31_V2", serviceDetails,
                basicSetup.getCustomerUser1(), role);
        // ASYNC
        setDateFactory("2013-03-10 10:20:00");
        subDetails = subscrSetup.completeAsyncSubscription(
                basicSetup.getSupplierAdminKey(),
                basicSetup.getCustomerAdmin(), subDetails);

        // upgrade to pro_rata of charge
        VOServiceDetails subDetails2 = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(), "SCENARIO_31_V2_2",
                        TestService.EXAMPLE_ASYNC,
                        TestPriceModel.EXAMPLE_PRORATA_MONTH_ROLES,
                        technicalServiceAsync, supplierMarketplace);
        serviceSetup.registerCompatibleServices(
                basicSetup.getSupplierAdminKey(), serviceDetails, subDetails2);
        setDateFactory("2013-03-30 10:20:00");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(subDetails, subDetails2);
        // ASYNC
        setDateFactory("2013-04-02 10:20:00");
        subscrSetup.abortAsyncUpgradeSubscription(basicSetup
                .getSupplierAdminKey(), upgradedSubDetails.getSubscriptionId(),
                basicSetup.getCustomerAdmin().getOrganizationId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO_31_V2", subDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO_31_V2", upgradedSubDetails);

    }

    public void createAsyncScenario32() throws Exception {
        // create subscription
        setDateFactory("2013-03-28 10:15:00");

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(), "SCENARIO_32",
                        TestService.EXAMPLE_ASYNC,
                        TestPriceModel.EXAMPLE_PERUNIT_MONTH_ROLES_1,
                        technicalServiceAsync, supplierMarketplace);
        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);
        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "SCENARIO_32", serviceDetails, basicSetup.getCustomerUser1(),
                role);
        // ASYNC
        setDateFactory("2013-03-28 10:20:00");
        subDetails = subscrSetup.completeAsyncSubscription(
                basicSetup.getSupplierAdminKey(),
                basicSetup.getCustomerAdmin(), subDetails);

        // upgrade to pro_rata of charge
        VOServiceDetails subDetails2 = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(), "SCENARIO_32_2",
                        TestService.EXAMPLE_ASYNC,
                        TestPriceModel.EXAMPLE_PRORATA_MONTH_ROLES,
                        technicalServiceAsync, supplierMarketplace);
        serviceSetup.registerCompatibleServices(
                basicSetup.getSupplierAdminKey(), serviceDetails, subDetails2);
        setDateFactory("2013-03-30 10:20:00");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(subDetails, subDetails2);
        // terminate Sub
        setDateFactory("2013-04-02 10:20:00");
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO_32", subDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO_32", upgradedSubDetails);

    }

    public void createAsyncScenario33() throws Exception {
        // create subscription
        setDateFactory("2013-03-28 10:15:00");

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(), "SCENARIO_33",
                        TestService.EXAMPLE_ASYNC,
                        TestPriceModel.EXAMPLE_PERUNIT_MONTH_ROLES_1,
                        technicalServiceAsync, supplierMarketplace);
        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);
        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "SCENARIO_33", serviceDetails, basicSetup.getCustomerUser1(),
                role);
        // ASYNC
        setDateFactory("2013-03-28 10:20:00");
        subDetails = subscrSetup.completeAsyncSubscription(
                basicSetup.getSupplierAdminKey(),
                basicSetup.getCustomerAdmin(), subDetails);

        // upgrade to pro_rata of charge
        VOServiceDetails subDetails2 = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(), "SCENARIO_33_2",
                        TestService.EXAMPLE_ASYNC,
                        TestPriceModel.EXAMPLE_PRORATA_MONTH_ROLES_2,
                        technicalServiceAsync, supplierMarketplace);
        serviceSetup.registerCompatibleServices(
                basicSetup.getSupplierAdminKey(), serviceDetails, subDetails2);
        setDateFactory("2013-03-30 10:20:00");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(subDetails, subDetails2);

        // delete paymenttypes
        container.login(basicSetup.getSupplierAdminKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        setDateFactory("2013-04-01 10:20:00");
        paymentSetup.deleteServicePaymentTypes(serviceDetails);

        // ASYNC
        setDateFactory("2013-04-15 10:20:00");
        upgradedSubDetails = subscrSetup.completeAsyncUpgradeSubscription(
                basicSetup.getSupplierAdminKey(),
                basicSetup.getCustomerAdmin(), upgradedSubDetails);

        // terminate Sub
        setDateFactory("2013-04-30 10:20:00");
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO_33", subDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO_33", upgradedSubDetails);

    }

    public void createAsyncScenario34() throws Exception {
        // create subscription
        setDateFactory("2013-03-28 10:15:00");

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(), "SCENARIO_34",
                        TestService.EXAMPLE_ASYNC,
                        TestPriceModel.EXAMPLE_PERUNIT_MONTH_ROLES_1,
                        technicalServiceAsync, supplierMarketplace);
        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);
        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "SCENARIO_34", serviceDetails, basicSetup.getCustomerUser1(),
                role);
        // ASYNC
        setDateFactory("2013-03-28 10:20:00");
        subDetails = subscrSetup.completeAsyncSubscription(
                basicSetup.getSupplierAdminKey(),
                basicSetup.getCustomerAdmin(), subDetails);

        // upgrade to pro_rata of charge
        VOServiceDetails subDetails2 = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "SCENARIO_34_UPGRADE", TestService.EXAMPLE_ASYNC,
                        TestPriceModel.EXAMPLE_PRORATA_MONTH_ROLES_2,
                        technicalServiceAsync, supplierMarketplace);
        serviceSetup.registerCompatibleServices(
                basicSetup.getSupplierAdminKey(), serviceDetails, subDetails2);
        setDateFactory("2013-03-30 10:20:00");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(subDetails, subDetails2);

        // delete paymenttypes
        container.login(basicSetup.getSupplierAdminKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        setDateFactory("2013-04-01 10:20:00");
        paymentSetup.deleteServicePaymentTypes(subDetails2);

        // ASYNC
        setDateFactory("2013-04-15 10:20:00");
        upgradedSubDetails = subscrSetup.completeAsyncUpgradeSubscription(
                basicSetup.getSupplierAdminKey(),
                basicSetup.getCustomerAdmin(), upgradedSubDetails);

        // terminate Sub
        setDateFactory("2013-04-30 10:20:00");
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO_34", subDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO_34", upgradedSubDetails);

    }

    public void createAsyncScenario34_1() throws Exception {
        // create subscription
        setDateFactory("2013-03-28 10:15:00");

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(), "SCENARIO_34_1",
                        TestService.EXAMPLE_ASYNC,
                        TestPriceModel.EXAMPLE_PERUNIT_MONTH_ROLES_2,
                        technicalServiceAsync, supplierMarketplace);
        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);
        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "SCENARIO_34_1", serviceDetails, basicSetup.getCustomerUser1(),
                role);
        // ASYNC
        setDateFactory("2013-03-28 10:20:00");
        subDetails = subscrSetup.completeAsyncSubscription(
                basicSetup.getSupplierAdminKey(),
                basicSetup.getCustomerAdmin(), subDetails);

        // upgrade to pro_rata of charge
        VOServiceDetails subDetails2 = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(), "SCENARIO_34_1_2",
                        TestService.EXAMPLE_ASYNC,
                        TestPriceModel.EXAMPLE_PRORATA_MONTH_ROLES_3,
                        technicalServiceAsync, supplierMarketplace);
        serviceSetup.registerCompatibleServices(
                basicSetup.getSupplierAdminKey(), serviceDetails, subDetails2);
        setDateFactory("2013-03-30 10:20:00");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(subDetails, subDetails2);

        // delete paymenttypes
        container.login(basicSetup.getSupplierAdminKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        setDateFactory("2013-04-01 10:20:00");
        paymentSetup.deleteServicePaymentTypes(subDetails2);

        // ASYNC
        setDateFactory("2013-04-15 10:20:00");
        upgradedSubDetails = subscrSetup.completeAsyncUpgradeSubscription(
                basicSetup.getSupplierAdminKey(),
                basicSetup.getCustomerAdmin(), upgradedSubDetails);

        setDateFactory("2013-04-20 10:20:00");
        container.login(basicSetup.getSupplierAdminKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);

        paymentSetup.reassignServicePaymentTypes(subDetails2);

        // terminate Sub
        setDateFactory("2013-04-30 10:20:00");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO_34_1", subDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO_34_1", upgradedSubDetails);

    }

    public void createAsyncScenario34_2() throws Exception {
        // create subscription
        setDateFactory("2014-03-28 10:15:00");

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(), "SCENARIO_34_2",
                        TestService.EXAMPLE_ASYNC,
                        TestPriceModel.EXAMPLE_PERUNIT_WEEK_ROLES,
                        technicalServiceAsync, supplierMarketplace);
        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);
        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "SCENARIO_34_2", serviceDetails, basicSetup.getCustomerUser1(),
                role);
        // ASYNC
        setDateFactory("2014-03-28 10:20:00");
        subDetails = subscrSetup.completeAsyncSubscription(
                basicSetup.getSupplierAdminKey(),
                basicSetup.getCustomerAdmin(), subDetails);

        // upgrade to pro_rata of charge
        VOServiceDetails subDetails2 = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(), "SCENARIO_34_2_2",
                        TestService.EXAMPLE_ASYNC,
                        TestPriceModel.EXAMPLE_RATA_WEEK_ROLES,
                        technicalServiceAsync, supplierMarketplace);
        serviceSetup.registerCompatibleServices(
                basicSetup.getSupplierAdminKey(), serviceDetails, subDetails2);
        setDateFactory("2014-03-30 10:20:00");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(subDetails, subDetails2);

        // delete payment types
        container.login(basicSetup.getSupplierAdminKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        setDateFactory("2014-04-01 10:20:00");
        paymentSetup.deleteServicePaymentTypes(subDetails2);

        // ASYNC
        setDateFactory("2014-04-15 10:20:00");
        upgradedSubDetails = subscrSetup.completeAsyncUpgradeSubscription(
                basicSetup.getSupplierAdminKey(),
                basicSetup.getCustomerAdmin(), upgradedSubDetails);

        setDateFactory("2014-04-20 10:20:00");
        container.login(basicSetup.getSupplierAdminKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);

        paymentSetup.reassignServicePaymentTypes(subDetails2);

        // terminate Sub
        setDateFactory("2014-04-30 10:20:00");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO_34_2", subDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO_34_2", upgradedSubDetails);

    }

    public void createAsyncScenario34_8() throws Exception {
        // create subscription
        setDateFactory("2014-03-23 10:15:00");

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(), "SCENARIO_34_8",
                        TestService.EXAMPLE_ASYNC,
                        TestPriceModel.EXAMPLE_PERUNIT_WEEK_FREEP_2,
                        technicalServiceAsync, supplierMarketplace);
        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);
        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "SCENARIO_34_8", serviceDetails, basicSetup.getCustomerUser1(),
                role);
        // ASYNC
        setDateFactory("2014-03-23 10:20:00");
        subDetails = subscrSetup.completeAsyncSubscription(
                basicSetup.getSupplierAdminKey(),
                basicSetup.getCustomerAdmin(), subDetails);

        // terminate Sub
        setDateFactory("2014-04-30 10:20:00");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO_34_8", subDetails);

    }

    public void createAsyncScenario34_9() throws Exception {
        // create subscription
        setDateFactory("2014-03-23 10:15:00");

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(), "SCENARIO_34_9",
                        TestService.EXAMPLE_ASYNC,
                        TestPriceModel.EXAMPLE_PERUNIT_WEEK_FREEP_3,
                        technicalServiceAsync, supplierMarketplace);
        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);
        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "SCENARIO_34_9", serviceDetails, basicSetup.getCustomerUser1(),
                role);
        // ASYNC
        setDateFactory("2014-03-23 10:20:00");
        subDetails = subscrSetup.completeAsyncSubscription(
                basicSetup.getSupplierAdminKey(),
                basicSetup.getCustomerAdmin(), subDetails);

        // terminate Sub
        setDateFactory("2014-04-30 10:20:00");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO_34_9", subDetails);

    }

    public void createAsyncScenario34_7() throws Exception {
        // create subscription
        setDateFactory("2014-03-28 10:15:00");

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(), "SCENARIO_34_7",
                        TestService.EXAMPLE_ASYNC,
                        TestPriceModel.EXAMPLE_PERUNIT_WEEK_ROLES,
                        technicalServiceAsync, supplierMarketplace);
        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);
        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "SCENARIO_34_7", serviceDetails, basicSetup.getCustomerUser1(),
                role);
        // ASYNC
        setDateFactory("2014-03-28 10:20:00");
        subDetails = subscrSetup.completeAsyncSubscription(
                basicSetup.getSupplierAdminKey(),
                basicSetup.getCustomerAdmin(), subDetails);

        // upgrade to pro_rata of charge
        VOServiceDetails subDetails2 = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(), "SCENARIO_34_7_2",
                        TestService.EXAMPLE_ASYNC,
                        TestPriceModel.EXAMPLE_RATA_WEEK_ROLES,
                        technicalServiceAsync, supplierMarketplace);
        serviceSetup.registerCompatibleServices(
                basicSetup.getSupplierAdminKey(), serviceDetails, subDetails2);
        setDateFactory("2014-03-30 10:20:00");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(subDetails, subDetails2);

        // delete paymenttypes
        container.login(basicSetup.getSupplierAdminKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        setDateFactory("2014-04-01 10:20:00");
        paymentSetup.deleteServicePaymentTypes(subDetails2);

        // ASYNC
        setDateFactory("2014-04-15 10:20:00");
        upgradedSubDetails = subscrSetup.completeAsyncUpgradeSubscription(
                basicSetup.getSupplierAdminKey(),
                basicSetup.getCustomerAdmin(), upgradedSubDetails);

        setDateFactory("2014-04-20 10:20:00");
        container.login(basicSetup.getSupplierAdminKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        // 2. upgrade to unit of charge
        VOServiceDetails subDetails3 = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(), "SCENARIO_34_7_3",
                        TestService.EXAMPLE_ASYNC,
                        TestPriceModel.EXAMPLE_PERUNIT_WEEK_ROLES,
                        technicalServiceAsync, supplierMarketplace);
        serviceSetup.registerCompatibleServices(
                basicSetup.getSupplierAdminKey(), subDetails2, subDetails3);
        setDateFactory("2014-04-20 10:22:00");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetails2 = subscrSetup
                .upgradeSubscription(upgradedSubDetails, subDetails3);

        // delete payment types
        container.login(basicSetup.getSupplierAdminKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        setDateFactory("2014-04-20 10:23:00");
        paymentSetup.deleteServicePaymentTypes(subDetails3);

        // ASYNC
        setDateFactory("2014-04-20 10:24:00");
        upgradedSubDetails = subscrSetup.completeAsyncUpgradeSubscription(
                basicSetup.getSupplierAdminKey(),
                basicSetup.getCustomerAdmin(), upgradedSubDetails2);

        setDateFactory("2014-04-25 10:24:00");
        container.login(basicSetup.getSupplierAdminKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        // reassign payment types
        paymentSetup.reassignServicePaymentTypes(subDetails3);

        // terminate Sub
        setDateFactory("2014-04-30 10:20:00");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO_34_7", subDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO_34_7", upgradedSubDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO_34_7", upgradedSubDetails2);

    }

    public void createAsyncScenario34_3() throws Exception {
        // create subscription
        setDateFactory("2014-03-28 10:15:00");

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(), "SCENARIO_34_3",
                        TestService.EXAMPLE_ASYNC,
                        TestPriceModel.EXAMPLE_RATA_WEEK_ROLES,
                        technicalServiceAsync, supplierMarketplace);
        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);
        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "SCENARIO_34_3", serviceDetails, basicSetup.getCustomerUser1(),
                role);
        // ASYNC
        setDateFactory("2014-03-28 10:20:00");
        subDetails = subscrSetup.completeAsyncSubscription(
                basicSetup.getSupplierAdminKey(),
                basicSetup.getCustomerAdmin(), subDetails);

        // upgrade to pro_rata of charge
        VOServiceDetails subDetails2 = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(), "SCENARIO_34_3_2",
                        TestService.EXAMPLE_ASYNC,
                        TestPriceModel.EXAMPLE_PERUNIT_WEEK_ROLES,
                        technicalServiceAsync, supplierMarketplace);
        serviceSetup.registerCompatibleServices(
                basicSetup.getSupplierAdminKey(), serviceDetails, subDetails2);
        setDateFactory("2014-03-30 10:20:00");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(subDetails, subDetails2);

        // delete paymenttypes
        container.login(basicSetup.getSupplierAdminKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        setDateFactory("2014-03-30 10:21:00");
        paymentSetup.deleteServicePaymentTypes(subDetails2);

        // ASYNC
        setDateFactory("2014-03-30 10:22:00");
        upgradedSubDetails = subscrSetup.completeAsyncUpgradeSubscription(
                basicSetup.getSupplierAdminKey(),
                basicSetup.getCustomerAdmin(), upgradedSubDetails);

        setDateFactory("2014-03-31 10:22:00");
        container.login(basicSetup.getSupplierAdminKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);

        paymentSetup.reassignServicePaymentTypes(subDetails2);

        // terminate Sub
        setDateFactory("2014-04-04 10:20:00");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO_34_3", subDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO_34_3", upgradedSubDetails);

    }

    public void createAsyncScenario34_5() throws Exception {
        // create subscription
        setDateFactory("2014-03-28 10:15:00");

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(), "SCENARIO_34_5",
                        TestService.EXAMPLE_ASYNC,
                        TestPriceModel.EXAMPLE_RATA_WEEK_ROLES,
                        technicalServiceAsync, supplierMarketplace);
        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);
        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "SCENARIO_34_5", serviceDetails, basicSetup.getCustomerUser1(),
                role);
        // ASYNC
        setDateFactory("2014-03-28 10:20:00");
        subDetails = subscrSetup.completeAsyncSubscription(
                basicSetup.getSupplierAdminKey(),
                basicSetup.getCustomerAdmin(), subDetails);

        // upgrade to pro_rata of charge
        VOServiceDetails subDetails2 = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(), "SCENARIO_34_5_2",
                        TestService.EXAMPLE_ASYNC,
                        TestPriceModel.EXAMPLE_PERUNIT_WEEK_ROLES,
                        technicalServiceAsync, supplierMarketplace);
        serviceSetup.registerCompatibleServices(
                basicSetup.getSupplierAdminKey(), serviceDetails, subDetails2);
        setDateFactory("2014-03-30 10:20:00");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(subDetails, subDetails2);

        // delete paymenttypes
        container.login(basicSetup.getSupplierAdminKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        setDateFactory("2014-03-29 10:21:00");
        paymentSetup.deleteServicePaymentTypes(subDetails2);

        // ASYNC
        setDateFactory("2014-03-30 10:22:00");
        upgradedSubDetails = subscrSetup.completeAsyncUpgradeSubscription(
                basicSetup.getSupplierAdminKey(),
                basicSetup.getCustomerAdmin(), upgradedSubDetails);

        setDateFactory("2014-03-31 10:22:00");
        container.login(basicSetup.getSupplierAdminKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);

        paymentSetup.reassignServicePaymentTypes(subDetails2);

        // terminate Sub
        setDateFactory("2014-04-30 10:20:00");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO_34_5", subDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO_34_5", upgradedSubDetails);

    }

    public void createAsyncScenario34_11() throws Exception {
        // create subscription
        setDateFactory("2014-03-28 10:15:00");

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(), "SCENARIO_34_11",
                        TestService.EXAMPLE_ASYNC,
                        TestPriceModel.EXAMPLE_RATA_WEEK_ROLES,
                        technicalServiceAsync, supplierMarketplace);
        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);
        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "SCENARIO_34_11", serviceDetails,
                basicSetup.getCustomerUser1(), role);
        // ASYNC
        setDateFactory("2014-03-28 10:20:00");
        subDetails = subscrSetup.completeAsyncSubscription(
                basicSetup.getSupplierAdminKey(),
                basicSetup.getCustomerAdmin(), subDetails);

        // upgrade to pro_rata of charge
        VOServiceDetails subDetails2 = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(), "SCENARIO_34_11_2",
                        TestService.EXAMPLE_ASYNC,
                        TestPriceModel.EXAMPLE_PERUNIT_WEEK_ROLES,
                        technicalServiceAsync, supplierMarketplace);
        serviceSetup.registerCompatibleServices(
                basicSetup.getSupplierAdminKey(), serviceDetails, subDetails2);
        setDateFactory("2014-03-30 10:20:00");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(subDetails, subDetails2);

        // delete paymenttypes
        container.login(basicSetup.getSupplierAdminKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        setDateFactory("2014-03-29 10:21:00");
        paymentSetup.deleteServicePaymentTypes(subDetails2);

        // ASYNC
        setDateFactory("2014-03-30 10:22:00");
        upgradedSubDetails = subscrSetup.completeAsyncUpgradeSubscription(
                basicSetup.getSupplierAdminKey(),
                basicSetup.getCustomerAdmin(), upgradedSubDetails);

        setDateFactory("2014-04-15 10:22:00");
        container.login(basicSetup.getSupplierAdminKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);

        paymentSetup.reassignServicePaymentTypes(subDetails2);

        // terminate Sub
        setDateFactory("2014-04-30 10:20:00");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO_34_11", subDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO_34_11", upgradedSubDetails);

    }

    public void createAsyncScenario34_10() throws Exception {
        // create subscription
        setDateFactory("2014-03-28 10:15:00");

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(), "SCENARIO_34_10",
                        TestService.EXAMPLE_ASYNC,
                        TestPriceModel.EXAMPLE_RATA_WEEK_ROLES,
                        technicalServiceAsync, supplierMarketplace);
        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);
        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "SCENARIO_34_10", serviceDetails,
                basicSetup.getCustomerUser1(), role);
        // ASYNC
        setDateFactory("2014-03-28 10:20:00");
        subDetails = subscrSetup.completeAsyncSubscription(
                basicSetup.getSupplierAdminKey(),
                basicSetup.getCustomerAdmin(), subDetails);

        // upgrade to pro_rata of charge
        VOServiceDetails subDetails2 = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(), "SCENARIO_34_10_2",
                        TestService.EXAMPLE_ASYNC,
                        TestPriceModel.EXAMPLE_PERUNIT_WEEK_ROLES,
                        technicalServiceAsync, supplierMarketplace);
        serviceSetup.registerCompatibleServices(
                basicSetup.getSupplierAdminKey(), serviceDetails, subDetails2);
        setDateFactory("2014-03-30 10:20:00");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(subDetails, subDetails2);

        // delete paymenttypes
        container.login(basicSetup.getSupplierAdminKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        setDateFactory("2014-03-30 10:21:00");
        paymentSetup.deleteServicePaymentTypes(subDetails2);

        // ASYNC
        setDateFactory("2014-03-30 10:22:00");
        upgradedSubDetails = subscrSetup.completeAsyncUpgradeSubscription(
                basicSetup.getSupplierAdminKey(),
                basicSetup.getCustomerAdmin(), upgradedSubDetails);

        setDateFactory("2014-03-31 10:22:00");
        container.login(basicSetup.getSupplierAdminKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);

        paymentSetup.reassignServicePaymentTypes(subDetails2);

        // terminate Sub
        setDateFactory("2014-04-30 10:20:00");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO_34_10", subDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO_34_10", upgradedSubDetails);

    }

    public void createAsyncScenario34_6() throws Exception {
        // create subscription
        setDateFactory("2014-03-28 10:15:00");

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(), "SCENARIO_34_6",
                        TestService.EXAMPLE_ASYNC,
                        TestPriceModel.EXAMPLE_PERUNIT_WEEK_ROLES,
                        technicalServiceAsync, supplierMarketplace);
        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);
        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "SCENARIO_34_6", serviceDetails, basicSetup.getCustomerUser1(),
                role);
        // ASYNC
        setDateFactory("2014-03-28 10:20:00");
        subDetails = subscrSetup.completeAsyncSubscription(
                basicSetup.getSupplierAdminKey(),
                basicSetup.getCustomerAdmin(), subDetails);

        // upgrade to pro_rata of charge
        VOServiceDetails subDetails2 = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(), "SCENARIO_34_6_2",
                        TestService.EXAMPLE_ASYNC,
                        TestPriceModel.EXAMPLE_RATA_WEEK_ROLES,
                        technicalServiceAsync, supplierMarketplace);
        serviceSetup.registerCompatibleServices(
                basicSetup.getSupplierAdminKey(), serviceDetails, subDetails2);
        setDateFactory("2014-03-30 10:20:00");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(subDetails, subDetails2);

        // delete paymenttypes
        container.login(basicSetup.getSupplierAdminKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        setDateFactory("2014-03-30 10:21:00");
        paymentSetup.deleteServicePaymentTypes(serviceDetails);

        // ASYNC
        setDateFactory("2014-03-30 10:22:00");
        subscrSetup.abortAsyncUpgradeSubscription(basicSetup
                .getSupplierAdminKey(), upgradedSubDetails.getSubscriptionId(),
                basicSetup.getCustomerAdmin().getOrganizationId());

        setDateFactory("2014-03-31 10:22:00");
        container.login(basicSetup.getSupplierAdminKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);

        paymentSetup.reassignServicePaymentTypes(serviceDetails);

        // terminate Sub
        setDateFactory("2014-04-04 10:20:00");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO_34_6", subDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO_34_6", upgradedSubDetails);

    }

    public void createAsyncScenario34_4() throws Exception {
        // create subscription
        setDateFactory("2014-03-28 10:15:00");

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(), "SCENARIO_34_4",
                        TestService.EXAMPLE_ASYNC,
                        TestPriceModel.EXAMPLE_RATA_WEEK_ROLES,
                        technicalServiceAsync, supplierMarketplace);
        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);
        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "SCENARIO_34_4", serviceDetails, basicSetup.getCustomerUser1(),
                role);
        // ASYNC
        setDateFactory("2014-03-28 10:20:00");
        subDetails = subscrSetup.completeAsyncSubscription(
                basicSetup.getSupplierAdminKey(),
                basicSetup.getCustomerAdmin(), subDetails);

        // upgrade to pro_rata of charge
        VOServiceDetails subDetails2 = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(), "SCENARIO_34_4_2",
                        TestService.EXAMPLE_ASYNC,
                        TestPriceModel.EXAMPLE_PERUNIT_WEEK_ROLES,
                        technicalServiceAsync, supplierMarketplace);
        serviceSetup.registerCompatibleServices(
                basicSetup.getSupplierAdminKey(), serviceDetails, subDetails2);
        setDateFactory("2014-03-30 10:20:00");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(subDetails, subDetails2);

        // delete paymenttypes
        container.login(basicSetup.getSupplierAdminKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        setDateFactory("2014-03-30 10:21:00");
        paymentSetup.deleteServicePaymentTypes(subDetails2);

        // delete paymenttypes
        container.login(basicSetup.getSupplierAdminKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        setDateFactory("2014-03-30 10:22:00");
        paymentSetup.deleteServicePaymentTypes(serviceDetails);
        // ASYNC
        setDateFactory("2014-03-30 10:22:00");
        upgradedSubDetails = subscrSetup.completeAsyncUpgradeSubscription(
                basicSetup.getSupplierAdminKey(),
                basicSetup.getCustomerAdmin(), upgradedSubDetails);

        setDateFactory("2014-03-31 10:22:00");
        container.login(basicSetup.getSupplierAdminKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);

        paymentSetup.reassignServicePaymentTypes(subDetails2);

        // terminate Sub
        setDateFactory("2014-04-04 10:20:00");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO_34_4", subDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO_34_4", upgradedSubDetails);

    }

    public void createAsyncScenario35() throws Exception {
        // create subscription
        setDateFactory("2013-03-28 10:15:00");

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(), "SCENARIO_35",
                        TestService.EXAMPLE_ASYNC,
                        TestPriceModel.EXAMPLE_PERUNIT_MONTH_ROLES_1,
                        technicalServiceAsync, supplierMarketplace);
        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);
        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "SCENARIO_35", serviceDetails, basicSetup.getCustomerUser1(),
                role);
        // ASYNC
        setDateFactory("2013-03-28 10:20:00");
        subDetails = subscrSetup.completeAsyncSubscription(
                basicSetup.getSupplierAdminKey(),
                basicSetup.getCustomerAdmin(), subDetails);

        // upgrade to pro_rata of charge
        VOServiceDetails subDetails2 = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(), "SCENARIO_35_2",
                        TestService.EXAMPLE_ASYNC,
                        TestPriceModel.EXAMPLE_PRORATA_MONTH_ROLES_2,
                        technicalServiceAsync, supplierMarketplace);
        serviceSetup.registerCompatibleServices(
                basicSetup.getSupplierAdminKey(), serviceDetails, subDetails2);
        setDateFactory("2013-03-30 10:20:00");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(subDetails, subDetails2);

        // delete paymenttypes
        container.login(basicSetup.getSupplierAdminKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        setDateFactory("2013-03-30 20:20:00");
        paymentSetup.deleteServicePaymentTypes(serviceDetails);
        setDateFactory("2013-04-01 10:20:00");

        paymentSetup.reassignServicePaymentTypes(serviceDetails);

        // ASYNC
        setDateFactory("2013-04-15 10:20:00");
        upgradedSubDetails = subscrSetup.completeAsyncUpgradeSubscription(
                basicSetup.getSupplierAdminKey(),
                basicSetup.getCustomerAdmin(), upgradedSubDetails);

        // terminate Sub
        setDateFactory("2013-04-30 10:20:00");
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO_35", subDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO_35", upgradedSubDetails);

    }

    public void createAsyncScenario36() throws Exception {
        // create subscription
        setDateFactory("2013-03-28 10:15:00");

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(), "SCENARIO_36",
                        TestService.EXAMPLE_ASYNC,
                        TestPriceModel.EXAMPLE_PERUNIT_MONTH_ROLES_1,
                        technicalServiceAsync, supplierMarketplace);
        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);

        // Create own customer
        String customerAdminId = "SCENARIO_36Customer";
        VOOrganization customer = orgSetup.registerCustomer(
                "SCENARIO_36CustomerOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                customerAdminId, supplierMarketplace.getMarketplaceId(),
                basicSetup.getSupplierOrgID());
        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "SCENARIO_36", serviceDetails, basicSetup.getCustomerUser1(),
                role);

        // ASYNC
        setDateFactory("2013-03-28 10:20:00");
        subDetails = subscrSetup.completeAsyncSubscription(
                basicSetup.getSupplierAdminKey(), customerAdmin, subDetails);

        // upgrade to pro_rata of charge
        VOServiceDetails subDetails2 = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(), "SCENARIO_36_2",
                        TestService.EXAMPLE_ASYNC,
                        TestPriceModel.EXAMPLE_PRORATA_MONTH_ROLES_2,
                        technicalServiceAsync, supplierMarketplace);
        serviceSetup.registerCompatibleServices(
                basicSetup.getSupplierAdminKey(), serviceDetails, subDetails2);
        setDateFactory("2013-03-30 10:20:00");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(subDetails, subDetails2);

        // delete paymenttypes
        container.login(basicSetup.getSupplierAdminKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        setDateFactory("2013-03-30 20:20:00");
        paymentSetup.deleteCustomerPaymentTypes(customer);

        // ASYNC
        setDateFactory("2013-04-10 10:20:00");
        upgradedSubDetails = subscrSetup.completeAsyncUpgradeSubscription(
                basicSetup.getSupplierAdminKey(), customerAdmin,
                upgradedSubDetails);

        setDateFactory("2013-04-15 10:20:00");
        container.login(basicSetup.getSupplierAdminKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);

        paymentSetup.reassignCustomerPaymentTypes(customer);

        // terminate Sub
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        setDateFactory("2013-04-30 10:20:00");
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO_36", subDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO_36", upgradedSubDetails);

    }

    /**
     * Suspend a subscription by deleting the customer's billing contact while
     * an asynchronous upgrade is running. After the async. upgrade is finished,
     * resume the subscription by creating a new billing contact and assigning
     * it to the subscription.
     */
    public void createAsyncScenario36a() throws Exception {
        // create subscription
        setDateFactory("2013-03-28 10:15:00");

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(), "SCENARIO_36a",
                        TestService.EXAMPLE_ASYNC,
                        TestPriceModel.EXAMPLE_PERUNIT_MONTH_ROLES_1,
                        technicalServiceAsync, supplierMarketplace);
        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);

        // create own customer
        String customerAdminId = "SCENARIO_36aCustomer";
        orgSetup.registerCustomer("SCENARIO_36aCustomerOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                customerAdminId, supplierMarketplace.getMarketplaceId(),
                basicSetup.getSupplierOrgID());
        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "SCENARIO_36a", serviceDetails, basicSetup.getCustomerUser1(),
                role);

        // ASYNC
        setDateFactory("2013-03-28 10:20:00");
        subDetails = subscrSetup.completeAsyncSubscription(
                basicSetup.getSupplierAdminKey(), customerAdmin, subDetails);

        // upgrade to pro_rata of charge
        VOServiceDetails serviceDetails2 = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(), "SCENARIO_36a_2",
                        TestService.EXAMPLE_ASYNC,
                        TestPriceModel.EXAMPLE_PRORATA_MONTH_ROLES_2,
                        technicalServiceAsync, supplierMarketplace);
        serviceSetup.registerCompatibleServices(
                basicSetup.getSupplierAdminKey(), serviceDetails,
                serviceDetails2);
        setDateFactory("2013-03-30 10:20:00");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(subDetails, serviceDetails2);

        // delete the customer's billing contact -> subscription is suspended
        setDateFactory("2013-03-30 20:20:00");
        upgradedSubDetails = deleteBillingContactsAndUpdateSub(
                customerAdmin.getKey(), upgradedSubDetails);

        // ASYNC
        setDateFactory("2013-04-10 10:20:00");
        upgradedSubDetails = subscrSetup.completeAsyncUpgradeSubscription(
                basicSetup.getSupplierAdminKey(), customerAdmin,
                upgradedSubDetails);

        // Resume the subscription by creating a new billing contact for the
        // customer and assigning this billing contact to the subscription
        setDateFactory("2013-04-15 10:20:00");
        upgradedSubDetails = restoreBillingContactForSubscription(
                customerAdmin.getKey(), upgradedSubDetails);

        // terminate Sub
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        setDateFactory("2013-04-30 10:20:00");
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO_36a", subDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO_36a", upgradedSubDetails);
    }

    public void createAsyncScenario37() throws Exception {
        // create subscription
        setDateFactory("2013-03-28 10:15:00");

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(), "SCENARIO_37",
                        TestService.EXAMPLE_ASYNC,
                        TestPriceModel.EXAMPLE_PERUNIT_MONTH_ROLES_1,
                        technicalServiceAsync, supplierMarketplace);
        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);
        // Create own customer
        String customerAdminId = "SCENARIO_37Customer";
        VOOrganization customer = orgSetup.registerCustomer(
                "SCENARIO_37CustomerOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                customerAdminId, supplierMarketplace.getMarketplaceId(),
                basicSetup.getSupplierOrgID());

        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "SCENARIO_37", serviceDetails, basicSetup.getCustomerUser1(),
                role);
        // ASYNC
        setDateFactory("2013-03-28 10:20:00");
        subDetails = subscrSetup.completeAsyncSubscription(
                basicSetup.getSupplierAdminKey(), customerAdmin, subDetails);

        // upgrade to pro_rata of charge
        VOServiceDetails subDetails2 = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(), "SCENARIO_37_2",
                        TestService.EXAMPLE_ASYNC,
                        TestPriceModel.EXAMPLE_PRORATA_MONTH_ROLES_2,
                        technicalServiceAsync, supplierMarketplace);
        serviceSetup.registerCompatibleServices(
                basicSetup.getSupplierAdminKey(), serviceDetails, subDetails2);
        setDateFactory("2013-04-01 10:10:00");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(subDetails, subDetails2);

        // delete paymenttypes
        container.login(basicSetup.getSupplierAdminKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        setDateFactory("2013-04-01 10:20:00");
        paymentSetup.deleteServicePaymentTypes(serviceDetails);
        subDetails = subscrSetup.getSubscriptionDetails(customerAdmin.getKey(),
                subDetails.getSubscriptionId());

        // ASYNC
        setDateFactory("2013-04-10 10:20:00");
        subscrSetup.abortAsyncUpgradeSubscription(
                basicSetup.getSupplierAdminKey(),
                upgradedSubDetails.getSubscriptionId(),
                customer.getOrganizationId());

        setDateFactory("2013-04-15 10:20:00");
        container.login(basicSetup.getSupplierAdminKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);

        paymentSetup.reassignServicePaymentTypes(serviceDetails);

        // terminate Sub
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        setDateFactory("2013-04-30 10:20:00");
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO_37", subDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO_37", upgradedSubDetails);

    }

    public void createAsyncScenario38() throws Exception {
        // create subscription
        setDateFactory("2013-03-28 10:15:00");

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(), "SCENARIO_38",
                        TestService.EXAMPLE_ASYNC,
                        TestPriceModel.EXAMPLE_PRORATA_MONTH_ROLES_2,
                        technicalServiceAsync, supplierMarketplace);
        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);
        // Create own customer
        String customerAdminId = "SCENARIO_38Customer";
        orgSetup.registerCustomer("SCENARIO_38CustomerOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                customerAdminId, supplierMarketplace.getMarketplaceId(),
                basicSetup.getSupplierOrgID());

        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "SCENARIO_38", serviceDetails, basicSetup.getCustomerUser1(),
                role);
        // ASYNC
        setDateFactory("2013-03-28 10:20:00");
        subDetails = subscrSetup.completeAsyncSubscription(
                basicSetup.getSupplierAdminKey(), customerAdmin, subDetails);

        // upgrade to pro_rata of charge
        VOServiceDetails subDetails2 = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(), "SCENARIO_38_2",
                        TestService.EXAMPLE_ASYNC,
                        TestPriceModel.EXAMPLE_PERUNIT_MONTH_ROLES_1,
                        technicalServiceAsync, supplierMarketplace);
        serviceSetup.registerCompatibleServices(
                basicSetup.getSupplierAdminKey(), serviceDetails, subDetails2);

        // delete paymenttypes -> subscription goes to suspended
        container.login(basicSetup.getSupplierAdminKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        setDateFactory("2013-03-30 09:20:00");
        paymentSetup.deleteServicePaymentTypes(serviceDetails);
        subDetails = subscrSetup.getSubscriptionDetails(customerAdmin.getKey(),
                subDetails.getSubscriptionId());

        setDateFactory("2013-03-30 10:20:00");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(subDetails, subDetails2);

        // ASYNC
        setDateFactory("2013-04-15 10:20:00");
        upgradedSubDetails = subscrSetup.completeAsyncUpgradeSubscription(
                basicSetup.getSupplierAdminKey(), customerAdmin,
                upgradedSubDetails);

        // terminate Sub
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        setDateFactory("2013-04-30 10:20:00");
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO_38", subDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO_38", upgradedSubDetails);

    }

    public void createAsyncScenario39() throws Exception {
        // create subscription
        setDateFactory("2013-03-28 10:15:00");

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(), "SCENARIO_39",
                        TestService.EXAMPLE_ASYNC,
                        TestPriceModel.EXAMPLE_PERUNIT_MONTH_ROLES_1,
                        technicalServiceAsync, supplierMarketplace);
        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);
        // Create own customer
        String customerAdminId = "SCENARIO_39Customer";
        VOOrganization customer = orgSetup.registerCustomer(
                "SCENARIO_39CustomerOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                customerAdminId, supplierMarketplace.getMarketplaceId(),
                basicSetup.getSupplierOrgID());

        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "SCENARIO_39", serviceDetails, basicSetup.getCustomerUser1(),
                role);
        // ASYNC
        setDateFactory("2013-03-28 10:20:00");
        subDetails = subscrSetup.completeAsyncSubscription(
                basicSetup.getSupplierAdminKey(), customerAdmin, subDetails);

        // delete paymenttypes
        container.login(basicSetup.getSupplierAdminKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        setDateFactory("2013-03-30 09:20:00");
        paymentSetup.deleteServicePaymentTypes(serviceDetails);
        subDetails = subscrSetup.getSubscriptionDetails(customerAdmin.getKey(),
                subDetails.getSubscriptionId());

        // upgrade to pro_rata of charge
        VOServiceDetails subDetails2 = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(), "SCENARIO_39_2",
                        TestService.EXAMPLE_ASYNC,
                        TestPriceModel.EXAMPLE_PRORATA_MONTH_ROLES_2,
                        technicalServiceAsync, supplierMarketplace);
        serviceSetup.registerCompatibleServices(
                basicSetup.getSupplierAdminKey(), serviceDetails, subDetails2);
        setDateFactory("2013-03-30 10:20:00");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(subDetails, subDetails2);

        // ASYNC
        setDateFactory("2013-04-15 10:20:00");
        subscrSetup.abortAsyncUpgradeSubscription(
                basicSetup.getSupplierAdminKey(),
                upgradedSubDetails.getSubscriptionId(),
                customer.getOrganizationId());

        // terminate Sub
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        setDateFactory("2013-04-30 10:20:00");
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO_39", subDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO_39", upgradedSubDetails);

    }

    public void createAsyncScenario40() throws Exception {
        // create subscription
        setDateFactory("2013-03-28 10:15:00");

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(), "SCENARIO_40",
                        TestService.EXAMPLE_ASYNC,
                        TestPriceModel.EXAMPLE_PRORATA_MONTH_ROLES_2,
                        technicalServiceAsync, supplierMarketplace);
        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);
        // Create own customer
        String customerAdminId = "SCENARIO_40Customer";
        orgSetup.registerCustomer("SCENARIO_40CustomerOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                customerAdminId, supplierMarketplace.getMarketplaceId(),
                basicSetup.getSupplierOrgID());

        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "SCENARIO_40", serviceDetails, basicSetup.getCustomerUser1(),
                role);
        // ASYNC
        setDateFactory("2013-03-28 10:20:00");
        subDetails = subscrSetup.completeAsyncSubscription(
                basicSetup.getSupplierAdminKey(), customerAdmin, subDetails);

        // delete paymenttypes
        container.login(basicSetup.getSupplierAdminKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        setDateFactory("2013-03-30 09:20:00");
        paymentSetup.deleteServicePaymentTypes(serviceDetails);
        subDetails = subscrSetup.getSubscriptionDetails(customerAdmin.getKey(),
                subDetails.getSubscriptionId());

        // upgrade to pro_rata of charge
        VOServiceDetails subDetails2 = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(), "SCENARIO_40_2",
                        TestService.EXAMPLE_ASYNC,
                        TestPriceModel.EXAMPLE_PERUNIT_MONTH_ROLES_1,
                        technicalServiceAsync, supplierMarketplace);
        serviceSetup.registerCompatibleServices(
                basicSetup.getSupplierAdminKey(), serviceDetails, subDetails2);
        setDateFactory("2013-03-30 10:20:00");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(subDetails, subDetails2);

        setDateFactory("2013-04-10 10:20:00");
        container.login(basicSetup.getSupplierAdminKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);

        paymentSetup.reassignServicePaymentTypes(serviceDetails);

        // ASYNC
        setDateFactory("2013-04-15 10:20:00");
        upgradedSubDetails = subscrSetup.completeAsyncUpgradeSubscription(
                basicSetup.getSupplierAdminKey(), customerAdmin,
                upgradedSubDetails);

        // terminate Sub
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        setDateFactory("2013-04-30 10:20:00");
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO_40", subDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO_40", upgradedSubDetails);

    }

    public void createAsyncScenario40_1() throws Exception {
        // create subscription
        setDateFactory("2014-03-28 10:15:00");

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(), "SCENARIO_40_1",
                        TestService.EXAMPLE_ASYNC,
                        TestPriceModel.EXAMPLE_RATA_WEEK_ROLES,
                        technicalServiceAsync, supplierMarketplace);
        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);
        // Create own customer
        String customerAdminId = "SCENARIO_40_1Customer";
        orgSetup.registerCustomer("SCENARIO_40_1CustomerOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                customerAdminId, supplierMarketplace.getMarketplaceId(),
                basicSetup.getSupplierOrgID());

        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "SCENARIO_40_1", serviceDetails, basicSetup.getCustomerUser1(),
                role);
        // ASYNC
        setDateFactory("2014-03-28 10:20:00");
        subDetails = subscrSetup.completeAsyncSubscription(
                basicSetup.getSupplierAdminKey(), customerAdmin, subDetails);

        // delete paymenttypes
        container.login(basicSetup.getSupplierAdminKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        setDateFactory("2014-03-30 09:20:00");
        paymentSetup.deleteServicePaymentTypes(serviceDetails);
        subDetails = subscrSetup.getSubscriptionDetails(customerAdmin.getKey(),
                subDetails.getSubscriptionId());

        // upgrade to pro_rata of charge
        VOServiceDetails subDetails2 = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(), "SCENARIO_40_1_2",
                        TestService.EXAMPLE_ASYNC,
                        TestPriceModel.EXAMPLE_PERUNIT_WEEK_ROLES,
                        technicalServiceAsync, supplierMarketplace);
        serviceSetup.registerCompatibleServices(
                basicSetup.getSupplierAdminKey(), serviceDetails, subDetails2);
        setDateFactory("2014-03-30 10:20:00");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(subDetails, subDetails2);

        // ASYNC
        setDateFactory("2014-03-31 10:20:00");
        upgradedSubDetails = subscrSetup.completeAsyncUpgradeSubscription(
                basicSetup.getSupplierAdminKey(), customerAdmin,
                upgradedSubDetails);

        // terminate Sub
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        setDateFactory("2014-04-04 10:20:00");
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO_40_1", subDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO_40_1", upgradedSubDetails);

    }

    public void createAsyncScenario40_2V() throws Exception {
        // create subscription
        setDateFactory("2014-03-28 10:15:00");

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(), "SCENARIO_40_2V",
                        TestService.EXAMPLE_ASYNC,
                        TestPriceModel.EXAMPLE_RATA_WEEK_ROLES,
                        technicalServiceAsync, supplierMarketplace);
        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);
        // Create own customer
        String customerAdminId = "SCENARIO_40_2VCustomer";
        orgSetup.registerCustomer("SCENARIO_40_2VCustomerOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                customerAdminId, supplierMarketplace.getMarketplaceId(),
                basicSetup.getSupplierOrgID());

        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "SCENARIO_40_2V", serviceDetails,
                basicSetup.getCustomerUser1(), role);

        // delete paymenttypes
        container.login(basicSetup.getSupplierAdminKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        setDateFactory("2014-03-30 09:20:00");
        paymentSetup.deleteServicePaymentTypes(serviceDetails);
        subDetails = subscrSetup.getSubscriptionDetails(customerAdmin.getKey(),
                subDetails.getSubscriptionId());

        // ASYNC
        setDateFactory("2014-03-30 09:20:00");
        subDetails = subscrSetup.completeAsyncSubscription(
                basicSetup.getSupplierAdminKey(), customerAdmin, subDetails);

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO_40_2V", subDetails);

    }

}
