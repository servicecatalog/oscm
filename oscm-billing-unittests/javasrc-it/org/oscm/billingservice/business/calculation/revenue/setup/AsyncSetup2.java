/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: May 7, 2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.calculation.revenue.setup;

import org.oscm.billingservice.setup.IntegrationTestSetup;
import org.oscm.billingservice.setup.VOPriceModelFactory.TestPriceModel;
import org.oscm.internal.vo.VOServiceDetails;
import org.oscm.internal.vo.VOSubscriptionDetails;

/**
 * @author farmaki
 * 
 */
public class AsyncSetup2 extends IntegrationTestSetup {

    public void createAsyncScenario28() throws Exception {
        setDateFactory("2013-07-10 20:00:00");
        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);

        VOServiceDetails service = createPublishAndActivateAsyncService(
                basicSetup.getSupplierAdminKey(),
                TestPriceModel.EXAMPLE_RATA_DAY_ROLES, "SERVICE_SCENARIO_28");

        VOSubscriptionDetails subDetails = subscribe(
                basicSetup.getCustomerAdmin(), "SERVICE_SCENARIO_28_subscr",
                service, "2013-09-01 12:00:00", "ADMIN");

        subDetails = completeAsyncSubscription(
                basicSetup.getSupplierAdminKey(),
                basicSetup.getCustomerAdmin(), subDetails,
                "2013-09-02 12:00:00");

        // Create upgraded service
        setDateFactory("2013-09-03 12:00:00");
        VOServiceDetails upgrService = createPublishAndActivateAsyncService(
                basicSetup.getSupplierAdminKey(),
                TestPriceModel.EXAMPLE_PERUNIT_WEEK_ROLES,
                "UPGR_SERVICE_SCENARIO_28");
        serviceSetup.registerCompatibleServices(
                basicSetup.getSupplierAdminKey(), service, upgrService);

        // Upgrade
        VOSubscriptionDetails upgradedSubDetails = upgrade(
                basicSetup.getCustomerAdmin(), subDetails, upgrService,
                "2013-09-04 12:00:00");

        // Complete the async.upgrade one time unit after the upgrade.
        completeAsyncUpgradeSubscription(basicSetup.getSupplierAdminKey(),
                basicSetup.getCustomerAdmin(), upgradedSubDetails,
                "2013-09-14 12:00:00");

        resetCutOffDay(basicSetup.getSupplierAdminKey());

    }

    public void createAsyncScenario41() throws Exception {
        setDateFactory("2013-07-10 20:00:00");
        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);

        VOServiceDetails service = createPublishAndActivateAsyncService(
                basicSetup.getSupplierAdminKey(),
                TestPriceModel.EXAMPLE_RATA_WEEK_ROLES_PARS_FREEP,
                "SERVICE_SCENARIO_41");

        VOSubscriptionDetails subDetails = subscribe(
                basicSetup.getCustomerAdmin(), "SERVICE_SCENARIO_41_subscr",
                service, "2013-08-01 12:00:00", "ADMIN");

        subDetails = completeAsyncSubscription(
                basicSetup.getSupplierAdminKey(),
                basicSetup.getCustomerAdmin(), subDetails,
                "2013-08-01 13:00:00");

        // Create upgraded service
        setDateFactory("2013-08-02 12:00:00");
        VOServiceDetails upgrService = createPublishAndActivateAsyncService(
                basicSetup.getSupplierAdminKey(),
                TestPriceModel.EXAMPLE_PERUNIT_WEEK_FREEP,
                "UPGR_SERVICE_SCENARIO_41");
        serviceSetup.registerCompatibleServices(
                basicSetup.getSupplierAdminKey(), service, upgrService);

        // Upgrade
        VOSubscriptionDetails upgradedSubDetails = upgrade(
                basicSetup.getCustomerAdmin(), subDetails, upgrService,
                "2013-08-03 12:00:00");

        // Complete the async.upgrade one time unit after the upgrade.
        completeAsyncUpgradeSubscription(basicSetup.getSupplierAdminKey(),
                basicSetup.getCustomerAdmin(), upgradedSubDetails,
                "2013-08-10 12:00:00");

        resetCutOffDay(basicSetup.getSupplierAdminKey());

    }

    public void createAsyncScenario41_1() throws Exception {
        setDateFactory("2013-07-10 20:00:00");
        setCutOffDay(basicSetup.getSupplierAdminKey(), 2);

        VOServiceDetails service = createPublishAndActivateAsyncService(
                basicSetup.getSupplierAdminKey(),
                TestPriceModel.EXAMPLE_RATA_WEEK_ROLES_PARS_FREEP,
                "SERVICE_SCENARIO_41_1");

        VOSubscriptionDetails subDetails = subscribe(
                basicSetup.getCustomerAdmin(), "SERVICE_SCENARIO_41_1_subscr",
                service, "2013-08-01 12:00:00", "ADMIN");

        subDetails = completeAsyncSubscription(
                basicSetup.getSupplierAdminKey(),
                basicSetup.getCustomerAdmin(), subDetails,
                "2013-08-01 13:00:00");

        // Create upgraded service
        setDateFactory("2013-08-02 12:00:00");
        VOServiceDetails upgrService = createPublishAndActivateAsyncService(
                basicSetup.getSupplierAdminKey(),
                TestPriceModel.EXAMPLE_PERUNIT_WEEK_FREEP,
                "UPGR_SERVICE_SCENARIO_41_1");
        serviceSetup.registerCompatibleServices(
                basicSetup.getSupplierAdminKey(), service, upgrService);

        // Upgrade
        VOSubscriptionDetails upgradedSubDetails = upgrade(
                basicSetup.getCustomerAdmin(), subDetails, upgrService,
                "2013-08-03 12:00:00");

        // Complete the async.upgrade one time unit after the upgrade.
        completeAsyncUpgradeSubscription(basicSetup.getSupplierAdminKey(),
                basicSetup.getCustomerAdmin(), upgradedSubDetails,
                "2013-08-10 12:00:00");

        resetCutOffDay(basicSetup.getSupplierAdminKey());

    }

    public void createAsyncScenario42() throws Exception {
        setDateFactory("2013-07-10 20:00:00");
        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);

        VOServiceDetails service = createPublishAndActivateAsyncService(
                basicSetup.getSupplierAdminKey(),
                TestPriceModel.EXAMPLE_RATA_WEEK_ROLES_PARS_FREEP,
                "SERVICE_SCENARIO_42");

        VOSubscriptionDetails subDetails = subscribe(
                basicSetup.getCustomerAdmin(), "SERVICE_SCENARIO_42_subscr",
                service, "2013-08-01 12:00:00", "ADMIN");

        subDetails = completeAsyncSubscription(
                basicSetup.getSupplierAdminKey(),
                basicSetup.getCustomerAdmin(), subDetails,
                "2013-08-01 13:00:00");

        // Create upgraded service
        setDateFactory("2013-08-02 12:00:00");
        VOServiceDetails upgrService = createPublishAndActivateAsyncService(
                basicSetup.getSupplierAdminKey(),
                TestPriceModel.EXAMPLE_PERUNIT_WEEK_FREEP,
                "UPGR_SERVICE_SCENARIO_42");
        serviceSetup.registerCompatibleServices(
                basicSetup.getSupplierAdminKey(), service, upgrService);

        // Upgrade
        VOSubscriptionDetails upgradedSubDetails = upgrade(
                basicSetup.getCustomerAdmin(), subDetails, upgrService,
                "2013-08-03 12:00:00");

        // Complete the async.upgrade one time unit after the upgrade.
        setDateFactory("2013-08-10 12:00:00");
        subscrSetup.abortAsyncUpgradeSubscription(
                basicSetup.getSupplierAdminKey(),
                upgradedSubDetails.getSubscriptionId(),
                basicSetup.getCustomerOrgID());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

    }

    public void createAsyncScenario43() throws Exception {
        setDateFactory("2013-07-10 20:00:00");
        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);

        VOServiceDetails service = createPublishAndActivateAsyncService(
                basicSetup.getSupplierAdminKey(),
                TestPriceModel.EXAMPLE_RATA_WEEK_ROLES_PARS_FREEP,
                "SERVICE_SCENARIO_43");

        VOSubscriptionDetails subDetails = subscribe(
                basicSetup.getCustomerAdmin(), "SERVICE_SCENARIO_43_subscr",
                service, "2013-08-01 12:00:00", "ADMIN");

        subDetails = completeAsyncSubscription(
                basicSetup.getSupplierAdminKey(),
                basicSetup.getCustomerAdmin(), subDetails,
                "2013-08-01 13:00:00");

        // Create upgraded service
        setDateFactory("2013-08-02 12:00:00");
        VOServiceDetails upgrService = createPublishAndActivateAsyncService(
                basicSetup.getSupplierAdminKey(),
                TestPriceModel.EXAMPLE_PERUNIT_WEEK_FREEP,
                "UPGR_SERVICE_SCENARIO_43");
        serviceSetup.registerCompatibleServices(
                basicSetup.getSupplierAdminKey(), service, upgrService);

        // Upgrade
        VOSubscriptionDetails upgradedSubDetails = upgrade(
                basicSetup.getCustomerAdmin(), subDetails, upgrService,
                "2013-08-03 12:00:00");

        // Remove the payment types of the old service before the async.upgrade
        // is finished, but after the free period of the old service has ended.
        setDateFactory("2013-08-08 12:00:00");
        paymentSetup.deleteServicePaymentTypes(
                basicSetup.getSupplierAdminKey(), service);

        // Complete the async.upgrade one time unit after the upgrade.
        completeAsyncUpgradeSubscription(basicSetup.getSupplierAdminKey(),
                basicSetup.getCustomerAdmin(), upgradedSubDetails,
                "2013-08-10 12:00:00");

        resetCutOffDay(basicSetup.getSupplierAdminKey());
    }

    public void createAsyncScenario43_1() throws Exception {
        setDateFactory("2013-07-10 20:00:00");
        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);

        VOServiceDetails service = createPublishAndActivateAsyncService(
                basicSetup.getSupplierAdminKey(),
                TestPriceModel.EXAMPLE_RATA_WEEK_ROLES_PARS_FREEP,
                "SERVICE_SCENARIO_43_1");

        VOSubscriptionDetails subDetails = subscribe(
                basicSetup.getCustomerAdmin(), "SERVICE_SCENARIO_43_1_subscr",
                service, "2013-08-01 12:00:00", "ADMIN");

        subDetails = completeAsyncSubscription(
                basicSetup.getSupplierAdminKey(),
                basicSetup.getCustomerAdmin(), subDetails,
                "2013-08-01 13:00:00");

        // Create upgraded service
        setDateFactory("2013-08-02 12:00:00");
        VOServiceDetails upgrService = createPublishAndActivateAsyncService(
                basicSetup.getSupplierAdminKey(),
                TestPriceModel.EXAMPLE_PERUNIT_WEEK_FREEP,
                "UPGR_SERVICE_SCENARIO_43_1");
        serviceSetup.registerCompatibleServices(
                basicSetup.getSupplierAdminKey(), service, upgrService);

        // Upgrade
        VOSubscriptionDetails upgradedSubDetails = upgrade(
                basicSetup.getCustomerAdmin(), subDetails, upgrService,
                "2013-08-03 12:00:00");

        // Remove the payment types of the old service before the async.upgrade
        // is finished, but before the free period of the old service has ended.
        setDateFactory("2013-08-04 12:00:00");
        paymentSetup.deleteServicePaymentTypes(
                basicSetup.getSupplierAdminKey(), service);

        // Complete the async.upgrade one time unit after the upgrade.
        completeAsyncUpgradeSubscription(basicSetup.getSupplierAdminKey(),
                basicSetup.getCustomerAdmin(), upgradedSubDetails,
                "2013-08-10 12:00:00");

        resetCutOffDay(basicSetup.getSupplierAdminKey());
    }

    public void createAsyncScenario44() throws Exception {
        setDateFactory("2013-07-10 20:00:00");
        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);

        VOServiceDetails service = createPublishAndActivateAsyncService(
                basicSetup.getSupplierAdminKey(),
                TestPriceModel.EXAMPLE_PERUNIT_WEEK_ROLES,
                "SERVICE_SCENARIO_44");

        VOSubscriptionDetails subDetails = subscribe(
                basicSetup.getCustomerAdmin(), "SERVICE_SCENARIO_44_subscr",
                service, "2013-08-01 12:00:00", "ADMIN");

        // Suspend the subscription before the subscription is completed
        setDateFactory("2013-08-07 12:00:00");
        paymentSetup.deleteServicePaymentTypes(
                basicSetup.getSupplierAdminKey(), service);

        // Complete the subscription
        completeAsyncSubscription(basicSetup.getSupplierAdminKey(),
                basicSetup.getCustomerAdmin(), subDetails,
                "2013-08-10 12:00:00");

        // Reassign the payment types one time unit later.
        setDateFactory("2013-08-17 12:00:00");
        paymentSetup.reassignServicePaymentTypes(
                basicSetup.getSupplierAdminKey(), service);

        resetCutOffDay(basicSetup.getSupplierAdminKey());
    }

    public void createAsyncScenario45() throws Exception {
        setDateFactory("2013-07-10 20:00:00");
        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);

        VOServiceDetails service = createPublishAndActivateAsyncService(
                basicSetup.getSupplierAdminKey(),
                TestPriceModel.EXAMPLE_RATA_WEEK_ROLES_PARS_FREEP,
                "SERVICE_SCENARIO_45");

        VOSubscriptionDetails subDetails = subscribe(
                basicSetup.getCustomerAdmin(), "SERVICE_SCENARIO_45_subscr",
                service, "2013-08-01 12:00:00", "ADMIN");

        // Suspend the subscription before the subscription is completed
        setDateFactory("2013-08-07 12:00:00");
        paymentSetup.deleteServicePaymentTypes(
                basicSetup.getSupplierAdminKey(), service);

        // Complete the subscription
        completeAsyncSubscription(basicSetup.getSupplierAdminKey(),
                basicSetup.getCustomerAdmin(), subDetails,
                "2013-08-13 00:00:00");

        // Reassign the payment types before the free period has ended.
        setDateFactory("2013-08-15 12:00:00");
        paymentSetup.reassignServicePaymentTypes(
                basicSetup.getSupplierAdminKey(), service);

        resetCutOffDay(basicSetup.getSupplierAdminKey());

    }

    public void createAsyncScenario45_1() throws Exception {
        setDateFactory("2013-07-10 20:00:00");
        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);

        VOServiceDetails service = createPublishAndActivateAsyncService(
                basicSetup.getSupplierAdminKey(),
                TestPriceModel.EXAMPLE_RATA_WEEK_ROLES_PARS_FREEP,
                "SERVICE_SCENARIO_45_1");

        VOSubscriptionDetails subDetails = subscribe(
                basicSetup.getCustomerAdmin(), "SERVICE_SCENARIO_45_1_subscr",
                service, "2013-08-01 12:00:00", "ADMIN");

        // Suspend the subscription before the subscription is completed
        setDateFactory("2013-08-07 12:00:00");
        paymentSetup.deleteServicePaymentTypes(
                basicSetup.getSupplierAdminKey(), service);

        // Complete the subscription
        completeAsyncSubscription(basicSetup.getSupplierAdminKey(),
                basicSetup.getCustomerAdmin(), subDetails,
                "2013-08-13 00:00:00");

        // Reassign the payment types after the free period has ended.
        setDateFactory("2013-08-20 12:00:00");
        paymentSetup.reassignServicePaymentTypes(
                basicSetup.getSupplierAdminKey(), service);

        resetCutOffDay(basicSetup.getSupplierAdminKey());

    }
}
