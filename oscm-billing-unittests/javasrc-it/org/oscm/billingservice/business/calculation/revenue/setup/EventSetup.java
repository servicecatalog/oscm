/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 14.08.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.calculation.revenue.setup;

import java.util.concurrent.Callable;

import org.oscm.billingservice.setup.BillingIntegrationTestBase;
import org.oscm.billingservice.setup.IntegrationTestSetup;
import org.oscm.billingservice.setup.VOPriceModelFactory.TestPriceModel;
import org.oscm.billingservice.setup.VOServiceFactory;
import org.oscm.billingservice.setup.VOServiceFactory.TestService;
import org.oscm.test.DateTimeHandling;
import org.oscm.internal.intf.SubscriptionService;
import org.oscm.internal.vo.VORoleDefinition;
import org.oscm.internal.vo.VOServiceDetails;
import org.oscm.internal.vo.VOSubscriptionDetails;

/**
 * @author kulle
 * 
 */
public class EventSetup extends IntegrationTestSetup {

    public void bug10248_cutoffday_eventAfterStartOfNextMonth()
            throws Exception {

        String subscriptionId = "bug10248_1";
        setCutOffDay(basicSetup.getSupplierAdminKey(), 3);

        long usageStartTime = DateTimeHandling
                .calculateMillis("2013-04-28 07:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(usageStartTime);
        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(), subscriptionId,
                        TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_WEEK_ROLES_EVENTS_FREEP,
                        technicalService, supplierMarketplace);

        // SUBSCRIBE to service
        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                subscriptionId, serviceDetails, basicSetup.getCustomerUser1(),
                role);

        // RECORD one event
        container.login(basicSetup.getSupplierAdminKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        subscrSetup.recordEventForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-06-02 12:00:00"),
                "FILE_DOWNLOAD", 2);

        resetCutOffDay(basicSetup.getSupplierAdminKey());
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                subscriptionId, subDetails);
    }

    public void createWeekBug10248_2_free_period_and_event() throws Exception {
        long usageStartTime = DateTimeHandling
                .calculateMillis("2013-04-28 07:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(usageStartTime);

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "BUG10248_2_FREE_UNIT_WEEK_EVENT",
                        TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_WEEK_ROLES_EVENTS_FREEP2,
                        technicalService, supplierMarketplace);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 3);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "BUG10248_2_FREE_UNIT_WEEK_EVENT", serviceDetails,
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
                "BUG10248_2_FREE_UNIT_WEEK_EVENT", subDetails);
    }

    public void createWeekBug10248_3_free_period_and_event() throws Exception {
        long usageStartTime = DateTimeHandling
                .calculateMillis("2013-04-28 07:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(usageStartTime);

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "BUG10248_3_FREE_UNIT_WEEK_EVENT",
                        TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_WEEK_ROLES_EVENTS_FREEP2,
                        technicalService, supplierMarketplace);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 6);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "BUG10248_3_FREE_UNIT_WEEK_EVENT", serviceDetails,
                basicSetup.getCustomerUser1(), role);

        // record one event after 1 days
        container.login(basicSetup.getSupplierAdminKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        subscrSetup.recordEventForSubscription(subDetails, usageStartTime
                + DateTimeHandling.daysToMillis(1), "FILE_DOWNLOAD", 2);

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
                "BUG10248_3_FREE_UNIT_WEEK_EVENT", subDetails);
    }

    public void createWeekBug10248_Rata_free_period_and_event()
            throws Exception {
        long usageStartTime = DateTimeHandling
                .calculateMillis("2013-04-28 07:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(usageStartTime);

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "BUG10248_RATA_WEEK_FREE_EVENT", TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_RATA_WEEK_ROLES_EVENTS_FREEP,
                        technicalService, supplierMarketplace);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 3);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "BUG10248_RATA_WEEK_FREE_EVENT", serviceDetails,
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
                "BUG10248_RATA_WEEK_FREE_EVENT", subDetails);
    }

    public void createWeekBug10248_month_free_period_and_event()
            throws Exception {
        long usageStartTime = DateTimeHandling
                .calculateMillis("2013-04-28 07:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(usageStartTime);

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "BUG10248_FREE_UNIT_MONTH_EVENT",
                        TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_MONTH_ROLES_PARS_EVENTS_FREEP,
                        technicalService, supplierMarketplace);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 3);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "BUG10248_FREE_UNIT_MONTH_EVENT", serviceDetails,
                basicSetup.getCustomerUser1(), role);

        // record one event after 4 days
        container.login(basicSetup.getSupplierAdminKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        subscrSetup.recordEventForSubscription(subDetails, usageStartTime
                + DateTimeHandling.daysToMillis(4), "FILE_DOWNLOAD", 2);

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
                "BUG10248_FREE_UNIT_MONTH_EVENT", subDetails);
    }

    public void ignoreEventsWithinSuspendBlock() throws Exception {
        BillingIntegrationTestBase
                .setDateFactoryInstance("2013-07-01 08:00:00");
        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(), "EVENT1",
                        TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_WEEK_ROLES_EVENTS_FREEP,
                        0, technicalService, supplierMarketplace);
        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);

        // subscribe
        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        BillingIntegrationTestBase
                .setDateFactoryInstance("2013-07-20 13:00:00");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "EVENT1", serviceDetails, basicSetup.getCustomerUser1(), role);

        // record one event
        subscrSetup.recordEventForSubscription(
                basicSetup.getSupplierAdminKey(), subDetails,
                "2013-07-25 13:00:00", "FILE_DOWNLOAD", 1);

        suspend("2013-07-26 13:00:00");

        // record one event
        subscrSetup.recordEventForSubscription(
                basicSetup.getSupplierAdminKey(), subDetails,
                "2013-07-27 13:00:00", "FILE_UPLOAD", 1);

        resume("2013-07-29 11:00:00");

        // record event
        subscrSetup.recordEventForSubscription(
                basicSetup.getSupplierAdminKey(), subDetails,
                "2013-07-29 13:00:00", "FOLDER_NEW", 1);

        // upgrade
        VOServiceDetails paidService = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "EVENT2",
                        TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_WEEK_ROLES_EVENTS_FREEP2,
                        0, technicalService, supplierMarketplace);
        serviceSetup.registerCompatibleServices(
                basicSetup.getSupplierAdminKey(), serviceDetails, paidService);
        long usageUpgradeTime = DateTimeHandling
                .calculateMillis("2013-07-30 13:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(usageUpgradeTime);
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        subDetails = loadSubscriptionDetails(subDetails.getSubscriptionId());
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(subDetails, paidService);

        // event
        subscrSetup.recordEventForSubscription(
                basicSetup.getSupplierAdminKey(), upgradedSubDetails,
                "2013-07-31 13:00:00", "FILE_UPLOAD", 1);

        long usageEndTime = DateTimeHandling
                .calculateMillis("2013-08-10 13:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(usageEndTime);
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());
        resetCutOffDay(basicSetup.getSupplierAdminKey());
        BillingIntegrationTestBase.updateSubscriptionListForTests("EVENT1",
                subDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests("EVENT2",
                upgradedSubDetails);
    }

    private VOSubscriptionDetails loadSubscriptionDetails(
            final String subscriptionId) throws Exception {
        return BillingIntegrationTestBase
                .runTX(new Callable<VOSubscriptionDetails>() {

                    @Override
                    public VOSubscriptionDetails call() throws Exception {
                        SubscriptionService subscriptionService = container
                                .get(SubscriptionService.class);
                        VOSubscriptionDetails subscription = subscriptionService
                                .getSubscriptionDetails(subscriptionId);
                        return subscription;
                    }
                });
    }

    private void suspend(String suspend) throws Exception {
        long suspResTime = DateTimeHandling.calculateMillis(suspend);
        BillingIntegrationTestBase.setDateFactoryInstance(suspResTime);
        container.login(basicSetup.getSupplierAdminKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        paymentSetup.deleteCustomerPaymentTypes(basicSetup.getCustomer());
    }

    private void resume(String resume) throws Exception {
        long reassignResTime = DateTimeHandling.calculateMillis(resume);
        container.login(basicSetup.getSupplierAdminKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        BillingIntegrationTestBase.setDateFactoryInstance(reassignResTime);
        paymentSetup.reassignCustomerPaymentTypes(basicSetup.getCustomer());
    }

}
