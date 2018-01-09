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
import org.oscm.billingservice.setup.VOPriceModelFactory.TestPriceModel;
import org.oscm.billingservice.setup.VOServiceFactory;
import org.oscm.billingservice.setup.VOServiceFactory.TestService;
import org.oscm.test.DateTimeHandling;
import org.oscm.internal.vo.VORoleDefinition;
import org.oscm.internal.vo.VOServiceDetails;
import org.oscm.internal.vo.VOSubscriptionDetails;

/**
 * @author baumann
 */
public class ParameterChargeSetup extends IntegrationTestSetup {

    /**
     * A customer subscribes to a service and terminates the subscription within
     * a time unit. The value of a parameter is changed several times in the
     * time unit. The role of the assigned user is changed too in the time unit.
     * The parameter fees are charged in a "pro rata" way. See example 2 in
     * requirement http://wwwi.est.fujitsu.com/confluence/x/4I7H
     */
    public void createMonthScenarioParAndRoleChange() throws Exception {
        BillingIntegrationTestBase
                .setDateFactoryInstance("2013-02-04 12:00:00");

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "PARCHARGE_PU_MONTH_ROLES", TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_MONTH_ROLES_PARS,
                        technicalService, supplierMarketplace);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);

        VORoleDefinition role = VOServiceFactory
                .getRole(serviceDetails, "USER");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "PARCHARGE_PU_MONTH_ROLES", serviceDetails,
                basicSetup.getCustomerUser1(), role);

        subDetails = subscrSetup.modifyParameterForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-02-11 12:00:00"),
                "MAX_FOLDER_NUMBER", "4");

        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-02-15 00:00:00"));
        subDetails = subscrSetup.modifyUserRole(subDetails.getUsageLicenses()
                .get(0), VOServiceFactory.getRole(serviceDetails, "GUEST"),
                subDetails.getSubscriptionId());

        subDetails = subscrSetup.modifyParameterForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-02-22 00:00:00"),
                "MAX_FOLDER_NUMBER", "88");

        BillingIntegrationTestBase
                .setDateFactoryInstance("2013-02-25 12:00:00");
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PARCHARGE_PU_MONTH_ROLES", subDetails);
    }

    /**
     * A customer subscribes to a service and terminates the subscription within
     * a billing period. The value of a parameter and the user role are changed
     * several times. The parameter fees are charged in a "pro rata" way if
     * parameter- or user role changes occur in a time unit. Similar as example
     * 2 in requirement http://wwwi.est.fujitsu.com/confluence/x/4I7H, but there
     * are several time units charged in the billing period.
     */
    public void createWeekScenarioParAndRoleChange() throws Exception {
        BillingIntegrationTestBase
                .setDateFactoryInstance("2013-02-01 00:00:00");

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "PARCHARGE_PU_WEEK_ROLES", TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_WEEK_ROLES_PARS,
                        technicalService, supplierMarketplace);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "PARCHARGE_PU_WEEK_ROLES", serviceDetails,
                basicSetup.getCustomerUser1(), role);

        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-02-04 00:00:00"));
        subDetails = subscrSetup.modifyUserRole(subDetails.getUsageLicenses()
                .get(0), VOServiceFactory.getRole(serviceDetails, "USER"),
                subDetails.getSubscriptionId());

        subDetails = subscrSetup.modifyParameterForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-02-04 00:00:00"),
                "MAX_FOLDER_NUMBER", "2");

        subDetails = subscrSetup.modifyParameterForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-02-11 00:00:00"),
                "MAX_FOLDER_NUMBER", "7");

        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-02-14 12:00:00"));
        subDetails = subscrSetup.modifyUserRole(subDetails.getUsageLicenses()
                .get(0), VOServiceFactory.getRole(serviceDetails, "GUEST"),
                subDetails.getSubscriptionId());

        subDetails = subscrSetup.modifyParameterForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-02-19 18:00:00"),
                "MAX_FOLDER_NUMBER", "13");

        BillingIntegrationTestBase
                .setDateFactoryInstance("2013-03-01 00:00:00");
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PARCHARGE_PU_WEEK_ROLES", subDetails);
    }

    /**
     * Same as createWeekScenarioParAndRoleChange(), but the subscription is
     * terminated at the beginning of the overlapping week.
     */
    public void createWeekScenarioParAndRoleChange2() throws Exception {
        BillingIntegrationTestBase
                .setDateFactoryInstance("2013-02-01 00:00:00");

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "PARCHARGE_PU_WEEK_ROLES2", TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_WEEK_ROLES_PARS,
                        technicalService, supplierMarketplace);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "PARCHARGE_PU_WEEK_ROLES2", serviceDetails,
                basicSetup.getCustomerUser1(), role);

        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-02-04 00:00:00"));
        subDetails = subscrSetup.modifyUserRole(subDetails.getUsageLicenses()
                .get(0), VOServiceFactory.getRole(serviceDetails, "USER"),
                subDetails.getSubscriptionId());

        subDetails = subscrSetup.modifyParameterForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-02-04 00:00:00"),
                "MAX_FOLDER_NUMBER", "2");

        subDetails = subscrSetup.modifyParameterForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-02-11 00:00:00"),
                "MAX_FOLDER_NUMBER", "7");

        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-02-14 12:00:00"));
        subDetails = subscrSetup.modifyUserRole(subDetails.getUsageLicenses()
                .get(0), VOServiceFactory.getRole(serviceDetails, "GUEST"),
                subDetails.getSubscriptionId());

        subDetails = subscrSetup.modifyParameterForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-02-19 18:00:00"),
                "MAX_FOLDER_NUMBER", "13");

        // Terminate the subscription at the beginning of the overlapping week
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-02-25 00:00:00"));
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PARCHARGE_PU_WEEK_ROLES2", subDetails);
    }

    /**
     * A customer subscribes to a service and terminates the subscription within
     * a time unit. The value of a parameter is changed several times in the
     * time unit. In addition the assigned user is deassigned and reassigned
     * with a different role in the time unit. The parameter fees are charged in
     * a "pro rata" way. See example 3 in requirement
     * http://wwwi.est.fujitsu.com/confluence/x/4I7H
     */
    public void createMonthScenarioParAndUserAssignChange() throws Exception {
        BillingIntegrationTestBase
                .setDateFactoryInstance("2013-02-04 12:00:00");

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "PARCHARGE_PU_MONTH_ASSIGN", TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_MONTH_ROLES_PARS,
                        technicalService, supplierMarketplace);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);

        VORoleDefinition role = VOServiceFactory
                .getRole(serviceDetails, "USER");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "PARCHARGE_PU_MONTH_ASSIGN", serviceDetails,
                basicSetup.getCustomerUser1(), role);

        subDetails = subscrSetup.modifyParameterForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-02-11 12:00:00"),
                "MAX_FOLDER_NUMBER", "4");

        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-02-13 06:00:00"));
        subDetails = subscrSetup.revokeUser(basicSetup.getCustomerUser1(),
                subDetails.getSubscriptionId());

        // Add same user again, but with a different role
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-02-15 00:00:00"));
        subDetails = subscrSetup.addUser(basicSetup.getCustomerUser1(),
                VOServiceFactory.getRole(serviceDetails, "GUEST"),
                subDetails.getSubscriptionId());

        subDetails = subscrSetup.modifyParameterForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-02-22 00:00:00"),
                "MAX_FOLDER_NUMBER", "88");

        BillingIntegrationTestBase
                .setDateFactoryInstance("2013-02-25 12:00:00");
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PARCHARGE_PU_MONTH_ASSIGN", subDetails);
    }

    /**
     * A customer subscribes to a service and terminates the subscription within
     * a billing period. The value of a parameter is changed several times. Also
     * the assigned user is deassigned and reassigned several times using
     * different roles. The parameter fees are charged in a "pro rata" way if
     * parameter- or user role changes occur in a time unit. Similar as example
     * 3 in requirement http://wwwi.est.fujitsu.com/confluence/x/4I7H, but there
     * are several time units charged in the billing period.
     */
    public void createWeekScenarioParAndUserAssignChange() throws Exception {
        BillingIntegrationTestBase
                .setDateFactoryInstance("2013-02-01 00:00:00");

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "PARCHARGE_PU_WEEK_ASSIGN", TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_WEEK_ROLES_PARS,
                        technicalService, supplierMarketplace);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "PARCHARGE_PU_WEEK_ASSIGN", serviceDetails,
                basicSetup.getCustomerUser1(), role);

        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-02-04 00:00:00"));
        subDetails = subscrSetup.modifyUserRole(subDetails.getUsageLicenses()
                .get(0), VOServiceFactory.getRole(serviceDetails, "USER"),
                subDetails.getSubscriptionId());

        subDetails = subscrSetup.modifyParameterForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-02-04 00:00:00"),
                "MAX_FOLDER_NUMBER", "2");

        subDetails = subscrSetup.modifyParameterForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-02-11 00:00:00"),
                "MAX_FOLDER_NUMBER", "7");

        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-02-12 06:00:00"));
        subDetails = subscrSetup.revokeUser(basicSetup.getCustomerUser1(),
                subDetails.getSubscriptionId());

        // Add same user again, but with a different role
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-02-14 12:00:00"));
        subDetails = subscrSetup.addUser(basicSetup.getCustomerUser1(),
                VOServiceFactory.getRole(serviceDetails, "GUEST"),
                subDetails.getSubscriptionId());

        subDetails = subscrSetup.modifyParameterForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-02-19 18:00:00"),
                "MAX_FOLDER_NUMBER", "13");

        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-02-21 12:00:00"));
        subDetails = subscrSetup.revokeUser(basicSetup.getCustomerUser1(),
                subDetails.getSubscriptionId());

        // Add same user again with the same role
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-02-24 00:00:00"));
        subDetails = subscrSetup.addUser(basicSetup.getCustomerUser1(),
                VOServiceFactory.getRole(serviceDetails, "GUEST"),
                subDetails.getSubscriptionId());

        BillingIntegrationTestBase
                .setDateFactoryInstance("2013-03-01 00:00:00");
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PARCHARGE_PU_WEEK_ASSIGN", subDetails);

    }

    /**
     * A customer subscribes to a service and terminates the subscription within
     * a time unit. The value of a parameter is changed several times in the
     * time unit. In addition the assigned user is deassigned and reassigned
     * with a different role in the time unit. One parameter change is done
     * while the user is deassigned. The parameter fees are charged in a
     * "pro rata" way. See example 4 in requirement
     * http://wwwi.est.fujitsu.com/confluence/x/4I7H
     */
    public void createMonthScenarioParAndUserAssignChange2() throws Exception {
        BillingIntegrationTestBase
                .setDateFactoryInstance("2013-02-04 12:00:00");

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "PARCHARGE_PU_MONTH_ASSIGN2", TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_MONTH_ROLES_PARS,
                        technicalService, supplierMarketplace);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);

        VORoleDefinition role = VOServiceFactory
                .getRole(serviceDetails, "USER");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "PARCHARGE_PU_MONTH_ASSIGN2", serviceDetails,
                basicSetup.getCustomerUser1(), role);

        subDetails = subscrSetup.modifyParameterForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-02-11 12:00:00"),
                "MAX_FOLDER_NUMBER", "4");

        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-02-13 06:00:00"));
        subDetails = subscrSetup.revokeUser(basicSetup.getCustomerUser1(),
                subDetails.getSubscriptionId());

        subDetails = subscrSetup.modifyParameterForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-02-22 00:00:00"),
                "MAX_FOLDER_NUMBER", "88");

        // Add same user again, but with a different role
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-02-23 18:00:00"));
        subDetails = subscrSetup.addUser(basicSetup.getCustomerUser1(),
                VOServiceFactory.getRole(serviceDetails, "GUEST"),
                subDetails.getSubscriptionId());

        BillingIntegrationTestBase
                .setDateFactoryInstance("2013-02-25 12:00:00");
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PARCHARGE_PU_MONTH_ASSIGN2", subDetails);
    }

    /**
     * A customer subscribes to a service and terminates the subscription within
     * a billing period. The value of a parameter is changed several times. In
     * addition the assigned user is deassigned in one time unit and reassigned
     * with a different role in another time unit. Two parameter changes are
     * done while the user is deassigned. The parameter fees are charged in a
     * "pro rata" way if parameter- or user role changes occur in a time unit.
     * See examples 5 and 6 in requirement
     * http://wwwi.est.fujitsu.com/confluence/x/4I7H
     */
    public void createWeekScenarioParAndUserAssignChange2() throws Exception {
        BillingIntegrationTestBase
                .setDateFactoryInstance("2013-02-01 00:00:00");

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "PARCHARGE_PU_WEEK_ASSIGN2", TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_WEEK_ROLES_PARS,
                        technicalService, supplierMarketplace);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "PARCHARGE_PU_WEEK_ASSIGN2", serviceDetails,
                basicSetup.getCustomerUser1(), role);

        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-02-04 00:00:00"));
        subDetails = subscrSetup.modifyUserRole(subDetails.getUsageLicenses()
                .get(0), VOServiceFactory.getRole(serviceDetails, "USER"),
                subDetails.getSubscriptionId());

        subDetails = subscrSetup.modifyParameterForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-02-04 00:00:00"),
                "MAX_FOLDER_NUMBER", "2");

        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-02-10 00:00:00"));
        subDetails = subscrSetup.revokeUser(basicSetup.getCustomerUser1(),
                subDetails.getSubscriptionId());

        subDetails = subscrSetup.modifyParameterForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-02-11 00:00:00"),
                "MAX_FOLDER_NUMBER", "7");

        subDetails = subscrSetup.modifyParameterForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-02-19 18:00:00"),
                "MAX_FOLDER_NUMBER", "10");

        // Add same user again, but with a different role
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-02-21 12:00:00"));
        subDetails = subscrSetup.addUser(basicSetup.getCustomerUser1(),
                VOServiceFactory.getRole(serviceDetails, "GUEST"),
                subDetails.getSubscriptionId());

        subDetails = subscrSetup.modifyParameterForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-02-23 06:00:00"),
                "MAX_FOLDER_NUMBER", "13");

        BillingIntegrationTestBase
                .setDateFactoryInstance("2013-03-01 00:00:00");
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PARCHARGE_PU_WEEK_ASSIGN2", subDetails);
    }

    /**
     * A customer subscribes to a service and terminates the subscription within
     * a time unit. After a while the assigned user is deassigned. The value of
     * a parameter is changed several times in the time unit. One parameter
     * change is done before the user is deassigned. The other parameter change
     * is done after the user has been deassigned. The parameter fees are
     * charged in a "pro rata" way. See example 5 in requirement
     * http://wwwi.est.fujitsu.com/confluence/x/4I7H
     */
    public void createMonthScenarioParChangeAndUserDeassign() throws Exception {
        BillingIntegrationTestBase
                .setDateFactoryInstance("2013-02-04 12:00:00");

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "PARCHARGE_PU_MONTH_DEASSIGN", TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_MONTH_ROLES_PARS2,
                        technicalService, supplierMarketplace);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "PARCHARGE_PU_MONTH_DEASSIGN", serviceDetails,
                basicSetup.getCustomerUser1(), role);

        subDetails = subscrSetup.modifyParameterForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-02-11 12:00:00"),
                "LONG_NUMBER", "29");

        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-02-15 00:00:00"));
        subDetails = subscrSetup.revokeUser(basicSetup.getCustomerUser1(),
                subDetails.getSubscriptionId());

        subDetails = subscrSetup.modifyParameterForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-02-22 00:00:00"),
                "LONG_NUMBER", "1588");

        BillingIntegrationTestBase
                .setDateFactoryInstance("2013-02-25 12:00:00");
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PARCHARGE_PU_MONTH_DEASSIGN", subDetails);
    }

    /**
     * A customer subscribes to a service and terminates the subscription within
     * a billing period. The assigned user is deassigned in the middle of the
     * subscription. The value of a parameter is changed several times. Two
     * parameter changes are done before the user is deassigned. Another
     * parameter change is done after the user is deassigned. The parameter fees
     * are charged in a "pro rata" way if parameter- or user role changes occur
     * in a time unit. Similar as example 5 in requirement
     * http://wwwi.est.fujitsu.com/confluence/x/4I7H, but several time units are
     * charged.
     */
    public void createWeekScenarioParChangeAndUserDeassign() throws Exception {
        BillingIntegrationTestBase
                .setDateFactoryInstance("2013-02-01 00:00:00");

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "PARCHARGE_PU_WEEK_DEASSIGN", TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_WEEK_ROLES_PARS_STEPPED,
                        technicalService, supplierMarketplace);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "PARCHARGE_PU_WEEK_DEASSIGN", serviceDetails,
                basicSetup.getCustomerUser1(), role);

        subDetails = subscrSetup.modifyParameterForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-02-04 00:00:00"),
                "LONG_NUMBER", "1523");

        subDetails = subscrSetup.modifyParameterForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-02-12 18:00:00"),
                "LONG_NUMBER", "400");

        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-02-14 12:00:00"));
        subDetails = subscrSetup.revokeUser(basicSetup.getCustomerUser1(),
                subDetails.getSubscriptionId());

        subDetails = subscrSetup.modifyParameterForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-02-16 06:00:00"),
                "LONG_NUMBER", "29");

        BillingIntegrationTestBase
                .setDateFactoryInstance("2013-03-01 00:00:00");
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PARCHARGE_PU_WEEK_DEASSIGN", subDetails);
    }

    /**
     * A subscription starts in an overlapping week. A parameter is changed in
     * that week.
     */
    public void createWeekScenarioParChange() throws Exception {
        BillingIntegrationTestBase
                .setDateFactoryInstance("2013-01-28 02:00:00");

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "PARCHANGE_PU_WEEK_SERVICE", TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_WEEK_ROLES_PARS,
                        technicalService, supplierMarketplace);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);

        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                basicSetup.getCustomerAdminKey(), "PARCHANGE_PU_WEEK",
                serviceDetails, basicSetup.getCustomerUser1(),
                VOServiceFactory.getRole(serviceDetails, "ADMIN"));

        subDetails = subscrSetup.modifyParameterForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-01-30 10:00:00"),
                "MAX_FOLDER_NUMBER", "2");

        subDetails = subscrSetup.modifyParameterForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-02-11 00:00:00"),
                "MAX_FOLDER_NUMBER", "7");

        BillingIntegrationTestBase
                .setDateFactoryInstance("2013-02-25 00:00:00");
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PARCHANGE_PU_WEEK", subDetails);
    }

    /**
     * A subscription starts in an overlapping week. It is upgraded in that week
     * from a per unit/week service to a pro rata/week service. A parameter is
     * changed before the upgrade.
     */
    public void createWeekScenarioParChangeUpgrade() throws Exception {
        BillingIntegrationTestBase
                .setDateFactoryInstance("2013-01-28 12:00:00");

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "PARCHANGE_UPGRADE_PU_WEEK_SERVICE",
                        TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_WEEK_ROLES_PARS2, 1,
                        technicalService, supplierMarketplace);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);

        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                basicSetup.getCustomerAdminKey(), "PARCHANGE_UPGRADE_PU_WEEK",
                serviceDetails, basicSetup.getCustomerUser1(),
                VOServiceFactory.getRole(serviceDetails, "ADMIN"));

        subDetails = subscrSetup.modifyParameterForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-01-30 00:00:00"),
                "MAX_FOLDER_NUMBER", "2");

        // Upgrade the subscription to a pro rata service
        VOServiceDetails proRataService = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "PARCHG_UPGRADE_RATA_PU_WEEK_SERVICE",
                        TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_RATA_WEEK_ROLES_PAR_I,
                        technicalService, supplierMarketplace);
        serviceSetup.registerCompatibleServices(
                basicSetup.getSupplierAdminKey(), serviceDetails,
                proRataService);

        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-01-30 12:00:00"));
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .copyParametersAndUpgradeSubscription(
                        basicSetup.getCustomerAdminKey(), subDetails,
                        proRataService);

        BillingIntegrationTestBase
                .setDateFactoryInstance("2013-02-25 08:00:00");
        subscrSetup
                .unsubscribeToService(upgradedSubDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PARCHANGE_UPGRADE_PU_WEEK", subDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PARCHANGE_UPGRADE_PU_WEEK", upgradedSubDetails);
    }

    public void createRataWeekScenarioParUserChange() throws Exception {
        setDateFactory("2013-04-01 00:00:00");

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "RATA_WEEK_PAR_USER_CHANGE_SERVICE",
                        TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_RATA_WEEK_ROLES_PAR_I,
                        technicalService, supplierMarketplace);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);

        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                basicSetup.getCustomerAdminKey(), "RATA_WEEK_PAR_USER_CHANGE",
                serviceDetails, basicSetup.getCustomerUser1(),
                VOServiceFactory.getRole(serviceDetails, "ADMIN"));

        setDateFactory("2013-04-08 00:00:00");
        subDetails = subscrSetup.addUser(basicSetup.getCustomerUser2(),
                VOServiceFactory.getRole(serviceDetails, "GUEST"),
                subDetails.getSubscriptionId());

        subDetails = subscrSetup.modifyParameterForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-04-08 00:00:00"),
                "MAX_FOLDER_NUMBER", "2");

        setDateFactory("2013-04-15 00:00:00");
        subDetails = subscrSetup.revokeUser(basicSetup.getCustomerUser1(),
                subDetails.getSubscriptionId());

        subDetails = subscrSetup.modifyParameterForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-04-18 12:00:00"),
                "MAX_FOLDER_NUMBER", "3");

        setDateFactory("2013-04-22 00:00:00");
        subDetails = subscrSetup.addUser(basicSetup.getCustomerUser1(),
                VOServiceFactory.getRole(serviceDetails, "GUEST"),
                subDetails.getSubscriptionId());

        setDateFactory("2013-04-29 00:00:00");
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "RATA_WEEK_PAR_USER_CHANGE", subDetails);
    }

}
