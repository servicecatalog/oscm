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
 * @author malhotra
 * 
 */
public class PeriodDaySetup extends IntegrationTestSetup {

    /**
     * Creates the subscription test data for long-lasting scenario based on the
     * calculation of billing period. Usage started before the billing period
     * and ended after the billing period (long-lasting usages).
     */
    public void createSubUsageScenario01() throws Exception {
        long usageStartTime = DateTimeHandling
                .calculateMillis("2012-12-01 00:00:00")
                - DateTimeHandling.daysToMillis(17.5);
        BillingIntegrationTestBase.setDateFactoryInstance(usageStartTime);

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "SCENARIO01_PERUNIT_DAY", TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_DAY_ROLES,
                        technicalService, supplierMarketplace);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "SCENARIO01_PERUNIT_DAY", serviceDetails,
                basicSetup.getCustomerUser1(), role);

        long usageEndTime = DateTimeHandling
                .calculateMillis("2013-01-01 00:00:00")
                + DateTimeHandling.daysToMillis(15.5);
        BillingIntegrationTestBase.setDateFactoryInstance(usageEndTime);
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO01_PERUNIT_DAY", subDetails);
    }

    /**
     * Creates the subscription test data for long-lasting scenario based on the
     * calculation of billing period. Usage started before the billing period
     * and ended after the billing period (long-lasting usages).
     */
    public void createAsyncSubUsageScenario01() throws Exception {
        long subscribeTime = DateTimeHandling
                .calculateMillis("2012-11-30 00:00:00")
                - DateTimeHandling.daysToMillis(17.5);
        BillingIntegrationTestBase.setDateFactoryInstance(subscribeTime);

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "SCENARIO01_ASYNC_PERUNIT_DAY",
                        TestService.EXAMPLE_ASYNC,
                        TestPriceModel.EXAMPLE_PERUNIT_DAY_ROLES,
                        technicalServiceAsync, supplierMarketplace);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                basicSetup.getCustomerAdminKey(),
                "SCENARIO01_ASYNC_PERUNIT_DAY", serviceDetails,
                basicSetup.getCustomerUser1(), role);

        long usageStartTime = DateTimeHandling
                .calculateMillis("2012-12-01 00:00:00")
                - DateTimeHandling.daysToMillis(17.5);
        BillingIntegrationTestBase.setDateFactoryInstance(usageStartTime);
        subscrSetup.completeAsyncSubscription(basicSetup.getSupplierAdminKey(),
                basicSetup.getCustomerAdmin(), subDetails);

        long usageEndTime = DateTimeHandling
                .calculateMillis("2013-01-01 00:00:00")
                + DateTimeHandling.daysToMillis(15.5);
        BillingIntegrationTestBase.setDateFactoryInstance(usageEndTime);
        subscrSetup.unsubscribeToService(basicSetup.getCustomerAdminKey(),
                subDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO01_ASYNC_PERUNIT_DAY", subDetails);
    }

    /**
     * Creates the subscription test data for the scenario before the billing
     * period start time. Usage started before the billing period and ended
     * before the billing period.
     */
    public void createSubUsageScenario02() throws Exception {
        long usageStartTime = DateTimeHandling
                .calculateMillis("2012-12-01 00:00:00")
                - DateTimeHandling.daysToMillis(22.5);
        BillingIntegrationTestBase.setDateFactoryInstance(usageStartTime);

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "SCENARIO02_PERUNIT_DAY", TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_DAY_ROLES,
                        technicalService, supplierMarketplace);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "SCENARIO02_PERUNIT_DAY", serviceDetails,
                basicSetup.getCustomerUser2(), role);

        long usageEndTime = DateTimeHandling
                .calculateMillis("2012-12-01 00:00:00")
                - DateTimeHandling.daysToMillis(5.5);
        BillingIntegrationTestBase.setDateFactoryInstance(usageEndTime);
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO02_PERUNIT_DAY", subDetails);
    }

    /**
     * Creates the subscription test data for the overlapping scenario with
     * billing period start time. Usage started before the billing period and
     * ended after the Billing Period Start Time.
     */
    public void createSubUsageScenario03() throws Exception {
        long usageStartTime = DateTimeHandling
                .calculateMillis("2012-12-01 00:00:00")
                - DateTimeHandling.daysToMillis(6.5);
        BillingIntegrationTestBase.setDateFactoryInstance(usageStartTime);

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "SCENARIO03_PERUNIT_DAY", TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_DAY_ROLES,
                        technicalService, supplierMarketplace);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "SCENARIO03_PERUNIT_DAY", serviceDetails,
                basicSetup.getCustomerUser1(), role);

        long usageEndTime = DateTimeHandling
                .calculateMillis("2012-12-01 00:00:00")
                + DateTimeHandling.daysToMillis(5);
        BillingIntegrationTestBase.setDateFactoryInstance(usageEndTime);
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO03_PERUNIT_DAY", subDetails);
    }

    /**
     * Creates the subscription test data for the usage scenario with in billing
     * period . Usage started after the billing period start time and ended
     * before the Billing Period End Time.
     */
    public void createSubUsageScenario04() throws Exception {
        long usageStartTime = DateTimeHandling
                .calculateMillis("2012-12-01 00:00:00")
                + DateTimeHandling.daysToMillis(5);
        BillingIntegrationTestBase.setDateFactoryInstance(usageStartTime);

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "SCENARIO04_PERUNIT_DAY", TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_DAY_ROLES,
                        technicalService, supplierMarketplace);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "SCENARIO04_PERUNIT_DAY", serviceDetails,
                basicSetup.getCustomerUser2(), role);

        long usageEndTime = DateTimeHandling
                .calculateMillis("2013-01-01 00:00:00")
                - DateTimeHandling.daysToMillis(7.5);
        BillingIntegrationTestBase.setDateFactoryInstance(usageEndTime);
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO04_PERUNIT_DAY", subDetails);
    }

    /**
     * Creates the subscription test data for the overlapping scenario with
     * billing period end time. Usage started before the billing period end time
     * and ended after the Billing Period End Time.
     */
    public void createSubUsageScenario05() throws Exception {
        long usageStartTime = DateTimeHandling
                .calculateMillis("2013-01-01 00:00:00")
                - DateTimeHandling.daysToMillis(5);
        BillingIntegrationTestBase.setDateFactoryInstance(usageStartTime);

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "SCENARIO05_PERUNIT_DAY", TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_DAY_ROLES,
                        technicalService, supplierMarketplace);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "SCENARIO05_PERUNIT_DAY", serviceDetails,
                basicSetup.getCustomerUser1(), role);

        long usageEndTime = DateTimeHandling
                .calculateMillis("2013-01-01 00:00:00")
                + DateTimeHandling.daysToMillis(8);
        BillingIntegrationTestBase.setDateFactoryInstance(usageEndTime);
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO05_PERUNIT_DAY", subDetails);
    }

    /**
     * Creates the subscription test data for the scenario after the billing
     * period end time. Usage started after the billing period end time and
     * ended after the billing period end time.
     */
    public void createSubUsageScenario06() throws Exception {
        long usageStartTime = DateTimeHandling
                .calculateMillis("2013-01-01 00:00:00")
                + DateTimeHandling.daysToMillis(5);
        BillingIntegrationTestBase.setDateFactoryInstance(usageStartTime);

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "SCENARIO06_PERUNIT_DAY", TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_DAY_ROLES,
                        technicalService, supplierMarketplace);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "SCENARIO06_PERUNIT_DAY", serviceDetails,
                basicSetup.getCustomerUser2(), role);

        long usageEndTime = DateTimeHandling
                .calculateMillis("2013-01-01 00:00:00")
                + DateTimeHandling.daysToMillis(15);
        BillingIntegrationTestBase.setDateFactoryInstance(usageEndTime);
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO06_PERUNIT_DAY", subDetails);
    }

    /**
     * Creates and terminates the subscription test data for the usage scenario
     * 7. </br> The subscription is started before the billing period start time
     * and is terminated before the billing period end time. The billing period
     * is as follows:
     * <ul>
     * <li>billing period start: 1354316400000 Sat Dec 01 2012 00:00:00 GMT+1
     * <li>billing period end: 1356994800000 Tue Jan 01 2013 00:00:00 GMT+1
     * <li>termination: 1356208560000 Sat Dec 22 2012 21:36:00 GMT+1
     * </ul>
     * 
     * The subscription is based on the following <b>price model</b>:
     * <ul>
     * <li>one time fee: 25.00, not billed
     * <li>price per period: 85.00
     * <li>price per user assignment: 150.00
     * <li>BOOLEAN_PARAMETER parameter, price per subscription: 3.00
     * <li>BOOLEAN_PARAMETER parameter, price per user: 30.00
     * <li>MAX_FOLDER_NUMBER, price per subscription: 2.00
     * <li>MAX_FOLDER_NUMBER, price per user: 20.00
     * </ul>
     * Stepped prices are not used!</br></br>
     * 
     * Furthermore, the subscription is based on the following <b>technical
     * service</b>:</br> PARAMETERS:
     * <ul>
     * <li>MAX_FILE_NUMBER (integer), not configurable by user
     * <li>FOLDER_NEW (integer), not configurable by user
     * <li>STRING_PARAMETER (string), not configurable by user
     * <li>HAS_OPTIONS (enumeration), configurable by user
     * <li>MAX_FOLDER_NUMBER (integer), configurable by user
     * <li>BOOLEAN_PARAMETER (boolean), configurable by user
     * </ul>
     * </br>EVENTS:
     * <ul>
     * <li>FILE_DOWNLOAD, not used
     * <li>FILE_UPLOAD, not used
     * <li>FOLDER_NEW, not used
     * </ul>
     * </br>ROLES:
     * <ul>
     * <li>ADMIN
     * <li>USER
     * <li>GUEST
     * </ul>
     * </br> </br> <b>Subscription Changes:</b> </br>User assignments:
     * <ul>
     * <li>one user assignment for the whole period
     * (GreenPeaceSecondCustomerUser2)
     * </ul>
     * </br> Parameter changes:
     * <ul>
     * <li>BOOLEAN_PARAMETER: same value for the whole period
     * <li>HAS_OPTIONS: same option for the whole period
     * <li>MAX_FOLDER_NUMBER: 3 relevant value changes for the period
     * </ul>
     */
    public void createSubUsageScenario07() throws Exception {
        long usageStartTime = DateTimeHandling
                .calculateMillis("2012-12-01 00:00:00")
                - DateTimeHandling.hoursToMillis(3.2);
        BillingIntegrationTestBase.setDateFactoryInstance(usageStartTime);

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "SCENARIO07_PERUNIT_DAY", TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_DAY_ROLES,
                        technicalService, supplierMarketplace);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "SCENARIO07_PERUNIT_DAY", serviceDetails,
                basicSetup.getCustomerUser2(), role);

        // Parameters modification 2.31 days before Billing Period Start Time
        long usageModificationTime = DateTimeHandling
                .calculateMillis("2012-12-01 00:00:00")
                - DateTimeHandling.daysToMillis(2.31);
        subDetails = subscrSetup.modifyParameterForSubscription(subDetails,
                usageModificationTime, "MAX_FOLDER_NUMBER", "14");
        // Parameters modification 2.31 days after Billing Period Start Time
        usageModificationTime = DateTimeHandling
                .calculateMillis("2012-12-01 00:00:00")
                + DateTimeHandling.daysToMillis(2.31);
        subDetails = subscrSetup.modifyParameterForSubscription(subDetails,
                usageModificationTime, "MAX_FOLDER_NUMBER", "25");
        // Parameters modification 2.9 days after Billing Period Start Time
        usageModificationTime = DateTimeHandling
                .calculateMillis("2012-12-01 00:00:00")
                + DateTimeHandling.daysToMillis(2.9);
        subDetails = subscrSetup.modifyParameterForSubscription(subDetails,
                usageModificationTime, "MAX_FOLDER_NUMBER", "35");

        long usageEndTime = DateTimeHandling
                .calculateMillis("2013-01-01 00:00:00")
                - DateTimeHandling.daysToMillis(4.5);
        BillingIntegrationTestBase.setDateFactoryInstance(usageEndTime);
        subscrSetup.terminateSubscription(basicSetup.getSupplierAdminKey(),
                subDetails, "");

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO07_PERUNIT_DAY", subDetails);
    }

    /**
     * Creates the subscription test data for the usage without user assignment.
     * Usage started before the billing period start time and terminated before
     * the Billing Period End Time.
     */
    public void createSubUsageScenario08() throws Exception {
        long usageStartTime = DateTimeHandling
                .calculateMillis("2012-12-01 00:00:00")
                - DateTimeHandling.hoursToMillis(3.2);
        BillingIntegrationTestBase.setDateFactoryInstance(usageStartTime);

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "SCENARIO08_PERUNIT_DAY", TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_DAY_ROLES,
                        technicalService, supplierMarketplace);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        // create subscription without user assignment
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "SCENARIO08_PERUNIT_DAY", serviceDetails, null, role);

        long usageEndTime = DateTimeHandling
                .calculateMillis("2013-01-01 00:00:00")
                - DateTimeHandling.hoursToMillis(4.5);
        BillingIntegrationTestBase.setDateFactoryInstance(usageEndTime);
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO08_PERUNIT_DAY", subDetails);
    }

    /**
     * Creates the subscription test data for the usage with multiple user
     * assignment for same and different users. Usage started before the billing
     * period start time and terminated after the Billing Period End Time.
     * 
     * role definitions are changed.
     */
    public void createSubUsageScenario09() throws Exception {
        long usageStartTime = DateTimeHandling
                .calculateMillis("2012-12-01 00:00:00")
                - DateTimeHandling.daysToMillis(17.5);
        BillingIntegrationTestBase.setDateFactoryInstance(usageStartTime);

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "SCENARIO09_PERUNIT_DAY", TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_DAY_ROLES,
                        technicalService, supplierMarketplace);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "SCENARIO09_PERUNIT_DAY", serviceDetails,
                basicSetup.getCustomerUser1(), role);

        // user is deassigned 15.1 days after Billing Period Start Time
        long userDeassignedTime = DateTimeHandling
                .calculateMillis("2012-12-01 00:00:00")
                + DateTimeHandling.daysToMillis(15.1);
        BillingIntegrationTestBase.setDateFactoryInstance(userDeassignedTime);
        subscrSetup.revokeUser(basicSetup.getCustomerUser1(),
                subDetails.getSubscriptionId());
        // No user is assigned for .4 days
        // another user is assigned again 15.5 days after Billing Period Start
        long userAssignedTime = DateTimeHandling
                .calculateMillis("2012-12-01 00:00:00")
                + DateTimeHandling.daysToMillis(15.5);
        BillingIntegrationTestBase.setDateFactoryInstance(userAssignedTime);
        VORoleDefinition changedRole = VOServiceFactory.getRole(serviceDetails,
                "USER");
        subDetails = subscrSetup.addUser(basicSetup.getCustomerUser2(),
                changedRole, subDetails.getSubscriptionId());
        // user is deassigned 15.7 days after Billing Period Start Time
        userDeassignedTime = DateTimeHandling
                .calculateMillis("2012-12-01 00:00:00")
                + DateTimeHandling.daysToMillis(15.7);
        BillingIntegrationTestBase.setDateFactoryInstance(userDeassignedTime);
        subscrSetup.revokeUser(basicSetup.getCustomerUser2(),
                subDetails.getSubscriptionId());
        // previous user is assigned again
        subDetails = subscrSetup.addUser(basicSetup.getCustomerUser1(), role,
                subDetails.getSubscriptionId());

        long usageEndTime = DateTimeHandling
                .calculateMillis("2013-01-01 00:00:00")
                + DateTimeHandling.daysToMillis(15.5);
        BillingIntegrationTestBase.setDateFactoryInstance(usageEndTime);
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO09_PERUNIT_DAY", subDetails);
    }

    /**
     * Test for per unit/day price model with stepped event prices, stepped user
     * assignment costs and stepped parameter period fees
     */
    public void createPerUnitDaySteppedScenario() throws Exception {
        setDateFactory("2013-04-01 00:00:00");

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "PER_UNIT_DAY_STEPPED_SERVICE", TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_DAY_EVENTS_PARS_STEPPED,
                        technicalService, supplierMarketplace);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);

        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                basicSetup.getCustomerAdminKey(), "PER_UNIT_DAY_STEPPED",
                serviceDetails, basicSetup.getSecondCustomerUser1(),
                VOServiceFactory.getRole(serviceDetails, "ADMIN"));

        container.login(basicSetup.getSupplierAdminKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        subscrSetup.recordEventForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-04-02 00:00:00"),
                "FILE_DOWNLOAD", 100);

        setDateFactory("2013-04-04 12:00:00");
        subscrSetup.unsubscribeToService(basicSetup.getCustomerAdminKey(),
                subDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PER_UNIT_DAY_STEPPED", subDetails);
    }

    /**
     * Test for per unit/day price model with stepped event prices, stepped user
     * assignment costs and stepped parameter period fees
     */
    public void createProRataDaySteppedScenario() throws Exception {
        setDateFactory("2013-04-01 00:00:00");

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "PRO_RATA_DAY_STEPPED_SERVICE", TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_RATA_DAY_EVENTS_PARS_STEPPED,
                        technicalService, supplierMarketplace);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);

        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                basicSetup.getCustomerAdminKey(), "PRO_RATA_DAY_STEPPED",
                serviceDetails, basicSetup.getSecondCustomerUser1(),
                VOServiceFactory.getRole(serviceDetails, "ADMIN"));

        container.login(basicSetup.getSupplierAdminKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        subscrSetup.recordEventForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-04-02 00:00:00"),
                "FILE_DOWNLOAD", 100);

        setDateFactory("2013-04-05 00:00:00");
        subscrSetup.unsubscribeToService(basicSetup.getCustomerAdminKey(),
                subDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PRO_RATA_DAY_STEPPED", subDetails);
    }

}
