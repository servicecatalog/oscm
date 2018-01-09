/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: Dec 20, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.calculation.revenue.setup;

import org.oscm.billingservice.setup.BillingIntegrationTestBase;
import org.oscm.billingservice.setup.CustomerData;
import org.oscm.billingservice.setup.IntegrationTestSetup;
import org.oscm.billingservice.setup.TestData;
import org.oscm.billingservice.setup.TestOrganizationSetup;
import org.oscm.billingservice.setup.VOPriceModelFactory.TestPriceModel;
import org.oscm.billingservice.setup.VOServiceFactory;
import org.oscm.billingservice.setup.VOServiceFactory.TestService;
import org.oscm.billingservice.setup.VOTechServiceFactory;
import org.oscm.billingservice.setup.VendorData;
import org.oscm.domobjects.UserGroup;
import org.oscm.test.BaseAdmUmTest;
import org.oscm.test.DateTimeHandling;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.internal.vo.VORoleDefinition;
import org.oscm.internal.vo.VOServiceDetails;
import org.oscm.internal.vo.VOSubscriptionDetails;
import org.oscm.internal.vo.VOTechnicalService;
import org.oscm.internal.vo.VOUser;

/**
 * @author malhotra
 * 
 */
public class PeriodWeekSetup extends IntegrationTestSetup {

    /**
     * Creates the subscription test data for long-lasting scenario based on the
     * calculation of billing period. Usage started before the billing period
     * and ended after the billing period (long-lasting usages).
     */
    public void createSubUsageScenario01() throws Exception {
        long usageStartTime = DateTimeHandling
                .calculateMillis("2012-12-01 00:00:00")
                - DateTimeHandling.weeksToMillis(2.5);
        BillingIntegrationTestBase.setDateFactoryInstance(usageStartTime);

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "SCENARIO01_PERUNIT_WEEK", TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_WEEK_ROLES_PARS,
                        technicalService, supplierMarketplace);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "SCENARIO01_PERUNIT_WEEK", serviceDetails,
                basicSetup.getSecondCustomerUser1(), role);

        long usageEndTime = DateTimeHandling
                .calculateMillis("2013-01-01 00:00:00")
                + DateTimeHandling.weeksToMillis(1.5);
        BillingIntegrationTestBase.setDateFactoryInstance(usageEndTime);
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO01_PERUNIT_WEEK", subDetails);
    }

    /**
     * Creates the subscription test data for the scenario before the billing
     * period start time. Usage started before the billing period and ended
     * before the billing period.
     * 
     * usage started before Billing Period Start Time
     * 
     * usage ended before Billing Period Start Time
     */
    public void createSubUsageScenario02() throws Exception {
        long usageStartTime = DateTimeHandling
                .calculateMillis("2012-12-01 00:00:00")
                - DateTimeHandling.weeksToMillis(2);
        BillingIntegrationTestBase.setDateFactoryInstance(usageStartTime);

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "SCENARIO02_PERUNIT_WEEK", TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_WEEK_ROLES_PARS,
                        technicalService, supplierMarketplace);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "SCENARIO02_PERUNIT_WEEK", serviceDetails,
                basicSetup.getSecondCustomerUser2(), role);

        long usageEndTime = DateTimeHandling
                .calculateMillis("2012-12-01 00:00:00")
                - DateTimeHandling.weeksToMillis(1.3);
        BillingIntegrationTestBase.setDateFactoryInstance(usageEndTime);
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO02_PERUNIT_WEEK", subDetails);
    }

    /**
     * Creates the subscription test data for the overlapping scenario with
     * billing period start time. Usage started before the billing period and
     * ended after the Billing Period Start Time.
     * 
     * usage started before Billing Period Start Time
     * 
     * usage ended after Billing Period Start Time
     */
    public void createSubUsageScenario03() throws Exception {
        long usageStartTime = DateTimeHandling
                .calculateMillis("2012-12-01 00:00:00")
                - DateTimeHandling.weeksToMillis(0.5);
        BillingIntegrationTestBase.setDateFactoryInstance(usageStartTime);

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "SCENARIO03_PERUNIT_WEEK", TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_WEEK_ROLES_PARS,
                        technicalService, supplierMarketplace);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "SCENARIO03_PERUNIT_WEEK", serviceDetails,
                basicSetup.getSecondCustomerUser1(), role);

        long usageEndTime = DateTimeHandling
                .calculateMillis("2012-12-01 00:00:00")
                + DateTimeHandling.weeksToMillis(2.5);
        BillingIntegrationTestBase.setDateFactoryInstance(usageEndTime);
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO03_PERUNIT_WEEK", subDetails);
    }

    /**
     * Creates the subscription test data for the usage scenario with in billing
     * period . Usage started after the billing period start time and ended
     * before the Billing Period End Time.
     * 
     * usage started after Billing Period Start Time
     * 
     * usage ended before Billing Period End Time
     */
    public void createSubUsageScenario04() throws Exception {
        long usageStartTime = DateTimeHandling
                .calculateMillis("2012-12-01 00:00:00")
                + DateTimeHandling.weeksToMillis(0.3);
        BillingIntegrationTestBase.setDateFactoryInstance(usageStartTime);

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "SCENARIO04_PERUNIT_WEEK", TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_WEEK_ROLES_PARS,
                        technicalService, supplierMarketplace);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "SCENARIO04_PERUNIT_WEEK", serviceDetails,
                basicSetup.getSecondCustomerUser2(), role);

        long usageEndTime = DateTimeHandling
                .calculateMillis("2013-01-01 00:00:00")
                - DateTimeHandling.weeksToMillis(2.1);
        BillingIntegrationTestBase.setDateFactoryInstance(usageEndTime);
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO04_PERUNIT_WEEK", subDetails);
    }

    /**
     * Creates the subscription test data for the overlapping scenario with
     * billing period end time. Usage started before the billing period end time
     * and ended after the Billing Period End Time.
     */
    public void createSubUsageScenario05() throws Exception {
        long usageStartTime = DateTimeHandling
                .calculateMillis("2013-01-01 00:00:00")
                - DateTimeHandling.weeksToMillis(1.5);
        BillingIntegrationTestBase.setDateFactoryInstance(usageStartTime);

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "SCENARIO05_PERUNIT_WEEK", TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_WEEK_ROLES_PARS,
                        technicalService, supplierMarketplace);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "SCENARIO05_PERUNIT_WEEK", serviceDetails,
                basicSetup.getSecondCustomerUser1(), role);

        long usageEndTime = DateTimeHandling
                .calculateMillis("2013-01-01 00:00:00")
                + DateTimeHandling.weeksToMillis(2);
        BillingIntegrationTestBase.setDateFactoryInstance(usageEndTime);
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO05_PERUNIT_WEEK", subDetails);
    }

    /**
     * Creates the subscription test data for the scenario after the billing
     * period end time. Usage started after the billing period end time and
     * ended after the billing period end time.
     */
    public void createSubUsageScenario06() throws Exception {
        long usageStartTime = DateTimeHandling
                .calculateMillis("2013-01-01 00:00:00")
                + DateTimeHandling.weeksToMillis(2);
        BillingIntegrationTestBase.setDateFactoryInstance(usageStartTime);

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "SCENARIO06_PERUNIT_WEEK", TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_WEEK_ROLES_PARS,
                        technicalService, supplierMarketplace);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "SCENARIO06_PERUNIT_WEEK", serviceDetails,
                basicSetup.getSecondCustomerUser2(), role);

        // Usage ended 4 weeks after Billing Period End Time
        long usageEndTime = DateTimeHandling
                .calculateMillis("2013-01-01 00:00:00")
                + DateTimeHandling.weeksToMillis(4);
        BillingIntegrationTestBase.setDateFactoryInstance(usageEndTime);
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO06_PERUNIT_WEEK", subDetails);
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
                - DateTimeHandling.weeksToMillis(2.3);
        BillingIntegrationTestBase.setDateFactoryInstance(usageStartTime);

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "SCENARIO07_PERUNIT_WEEK", TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_WEEK_ROLES_PARS,
                        technicalService, supplierMarketplace);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "SCENARIO07_PERUNIT_WEEK", serviceDetails,
                basicSetup.getSecondCustomerUser2(), role);

        // Parameters modification 1.3 weeks before Billing Period Start Time
        long usageModificationTime = DateTimeHandling
                .calculateMillis("2012-12-01 00:00:00")
                - DateTimeHandling.weeksToMillis(1.3);
        subDetails = subscrSetup.modifyParameterForSubscription(subDetails,
                usageModificationTime, "MAX_FOLDER_NUMBER", "12");

        // Parameters modification .3 weeks before Billing Period Start Time
        usageModificationTime = DateTimeHandling
                .calculateMillis("2012-12-01 00:00:00")
                - DateTimeHandling.weeksToMillis(0.3);
        subDetails = subscrSetup.modifyParameterForSubscription(subDetails,
                usageModificationTime, "MAX_FOLDER_NUMBER", "21");

        // Parameters modification .3 weeks after Billing Period Start Time
        usageModificationTime = DateTimeHandling
                .calculateMillis("2012-12-01 00:00:00")
                + DateTimeHandling.weeksToMillis(0.3);
        subDetails = subscrSetup.modifyParameterForSubscription(subDetails,
                usageModificationTime, "MAX_FOLDER_NUMBER", "31");

        // Parameters modification .6 weeks after Billing Period Start Time
        usageModificationTime = DateTimeHandling
                .calculateMillis("2012-12-01 00:00:00")
                + DateTimeHandling.weeksToMillis(0.6);
        subDetails = subscrSetup.modifyParameterForSubscription(subDetails,
                usageModificationTime, "MAX_FOLDER_NUMBER", "41");

        long usageEndTime = DateTimeHandling
                .calculateMillis("2013-01-01 00:00:00")
                - DateTimeHandling.weeksToMillis(1.3);
        BillingIntegrationTestBase.setDateFactoryInstance(usageEndTime);
        subscrSetup.terminateSubscription(basicSetup.getSupplierAdminKey(),
                subDetails, "");

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO07_PERUNIT_WEEK", subDetails);
    }

    /**
     * Creates the subscription test data for the usage without user assignment.
     * Usage started before the billing period start time and terminated before
     * the Billing Period End Time.
     * 
     * usage started before Billing Period Start Time
     * 
     * usage terminated before Billing Period End Time
     */
    public void createSubUsageScenario08() throws Exception {
        long usageStartTime = DateTimeHandling
                .calculateMillis("2012-12-01 00:00:00")
                - DateTimeHandling.weeksToMillis(2.3);
        BillingIntegrationTestBase.setDateFactoryInstance(usageStartTime);

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "SCENARIO08_PERUNIT_WEEK", TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_WEEK_ROLES_PARS,
                        technicalService, supplierMarketplace);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        // create subscription without user assignment
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "SCENARIO08_PERUNIT_WEEK", serviceDetails, null, role);

        // Parameters modification 0.3 weeks after Billing Period Start Time
        long usageModificationTime = DateTimeHandling
                .calculateMillis("2012-12-01 00:00:00")
                + DateTimeHandling.weeksToMillis(0.3);
        subDetails = subscrSetup.modifyParameterForSubscription(subDetails,
                usageModificationTime, "BOOLEAN_PARAMETER", "");
        usageModificationTime += 500000;
        subDetails = subscrSetup.modifyParameterForSubscription(subDetails,
                usageModificationTime, "BOOLEAN_PARAMETER", "" + Boolean.TRUE);

        long usageEndTime = DateTimeHandling
                .calculateMillis("2013-01-01 00:00:00")
                - DateTimeHandling.weeksToMillis(1.3);
        BillingIntegrationTestBase.setDateFactoryInstance(usageEndTime);
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO08_PERUNIT_WEEK", subDetails);
    }

    /**
     * Creates the subscription test data for the usage with multiple user
     * assignment for same and different users. Usage started before the billing
     * period start time and terminated after the Billing Period End Time. Role
     * definitions are changed.
     */
    public void createSubUsageScenario09() throws Exception {
        long usageStartTime = DateTimeHandling
                .calculateMillis("2012-12-01 00:00:00")
                + DateTimeHandling.weeksToMillis(0.1);
        BillingIntegrationTestBase.setDateFactoryInstance(usageStartTime);

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "SCENARIO09_PERUNIT_WEEK", TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_WEEK_ROLES_PARS,
                        technicalService, supplierMarketplace);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        // Subscription created without user .1 weeks after Billing Period Start
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "SCENARIO09_PERUNIT_WEEK", serviceDetails, null, role);

        // user is assigned .3 weeks after Billing Period Start Time
        // (beginning of next week after the overlapping week)
        long userAssignedTime = DateTimeHandling
                .calculateMillis("2012-12-01 00:00:00")
                + DateTimeHandling.weeksToMillis(0.3);
        BillingIntegrationTestBase.setDateFactoryInstance(userAssignedTime);
        VORoleDefinition changedRole = VOServiceFactory.getRole(serviceDetails,
                "USER");
        subscrSetup.addUser(basicSetup.getSecondCustomerUser2(), changedRole,
                subDetails.getSubscriptionId());
        // user is deassigned .5 weeks after Billing Period Start Time
        long userDeassignedTime = DateTimeHandling
                .calculateMillis("2012-12-01 00:00:00")
                + DateTimeHandling.weeksToMillis(0.5);
        BillingIntegrationTestBase.setDateFactoryInstance(userDeassignedTime);
        subscrSetup.revokeUser(basicSetup.getSecondCustomerUser2(),
                subDetails.getSubscriptionId());
        // same user is assigned again .7 weeks after Billing Period Start Time
        userAssignedTime = DateTimeHandling
                .calculateMillis("2012-12-01 00:00:00")
                + DateTimeHandling.weeksToMillis(0.7);
        BillingIntegrationTestBase.setDateFactoryInstance(userAssignedTime);
        changedRole = VOServiceFactory.getRole(serviceDetails, "GUEST");
        subscrSetup.addUser(basicSetup.getSecondCustomerUser2(), changedRole,
                subDetails.getSubscriptionId());
        // for overlapping week at Billing Period End Time
        // user is deassigned .7 weeks before Billing period End Time
        userDeassignedTime = DateTimeHandling
                .calculateMillis("2013-01-01 00:00:00")
                - DateTimeHandling.weeksToMillis(0.7);
        BillingIntegrationTestBase.setDateFactoryInstance(userDeassignedTime);
        subscrSetup.revokeUser(basicSetup.getSecondCustomerUser2(),
                subDetails.getSubscriptionId());
        // another user is assigned again .4 weeks before Billing period End
        userAssignedTime = DateTimeHandling
                .calculateMillis("2013-01-01 00:00:00")
                - DateTimeHandling.weeksToMillis(0.4);
        BillingIntegrationTestBase.setDateFactoryInstance(userAssignedTime);
        subscrSetup.addUser(basicSetup.getSecondCustomerUser1(), role,
                subDetails.getSubscriptionId());

        // Usage ended .2 weeks before Billing period End Time
        long usageEndTime = DateTimeHandling
                .calculateMillis("2013-01-01 00:00:00")
                - DateTimeHandling.weeksToMillis(0.2);
        BillingIntegrationTestBase.setDateFactoryInstance(usageEndTime);
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO09_PERUNIT_WEEK", subDetails);
    }

    /**
     * Creates the subscription data with upgrade options. Usage started before
     * the billing period start time and terminated after the Billing Period End
     * Time.
     * 
     * Subscription is upgraded to another service with different price models.
     * 
     * Usage terminated after Billing Period End Time
     */
    public void createSubUsageScenario10() throws Exception {
        VendorData supplierData = setupNewSupplier("2012-11-01 08:00:00");
        setCutOffDay(supplierData.getAdminKey(), 1);
        CustomerData customerData = registerCustomer(supplierData);

        VOServiceDetails serviceDetails = createPublishActivateService(
                supplierData, TestPriceModel.EXAMPLE_PERUNIT_WEEK_ROLES_PARS,
                "SCENARIO10_PERUNIT_WEEK_SERVICE");

        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2012-12-01 00:00:00")
                - DateTimeHandling.weeksToMillis(0.3));

        VOSubscriptionDetails subDetails = subscribe(
                customerData.getAdminUser(), "SCENARIO10_PERUNIT_WEEK",
                serviceDetails,
                DateTimeHandling.calculateMillis("2012-12-01 00:00:00")
                        - DateTimeHandling.weeksToMillis(0.3), "ADMIN");

        // create upgrade service
        VOServiceDetails upgradeService = createAndRegisterCompatibleService(
                supplierData, TestPriceModel.EXAMPLE_PERUNIT_WEEK_ROLES,
                serviceDetails, "SCENARIO10_PERUNIT_WEEK_UPGRADE_SERVICE");

        // upgrade subscription .3 weeks after Billing period Start Time
        VOSubscriptionDetails upgradedSubDetails = upgrade(
                customerData.getAdminUser(), subDetails, upgradeService,
                DateTimeHandling.calculateMillis("2012-12-01 00:00:00")
                        + DateTimeHandling.weeksToMillis(0.3));

        unsubscribe(customerData.getAdminKey(),
                upgradedSubDetails.getSubscriptionId(),
                DateTimeHandling.calculateMillis("2013-01-01 00:00:00")
                        + DateTimeHandling.weeksToMillis(1.5));

        resetCutOffDay(supplierData.getAdminKey());
        cacheTestData("SCENARIO10_PERUNIT_WEEK", new TestData(supplierData));
    }

    /**
     * Creates the subscription data with upgrade options. Usage started before
     * the billing period start time and terminated after the Billing Period End
     * Time.
     * 
     * Subscription is upgraded to another service with different price models.
     * 
     * Usage terminated after Billing Period End Time
     */
    public void createSubUsageScenario10_changeServiceIdAndUnitInBP()
            throws Exception {
        VendorData supplierData = setupNewSupplier("2012-11-01 08:00:00");
        setCutOffDay(supplierData.getAdminKey(), 1);
        CustomerData customerData = registerCustomer(supplierData);

        VOServiceDetails serviceDetails = createPublishActivateService(
                supplierData, TestPriceModel.EXAMPLE_PERUNIT_WEEK_ROLES_PARS,
                "SCENARIO10_PERUNIT_WEEK_SERVICE");

        UserGroup unit1 = createUnitAndAssignVisibleService(customerData,
                "Unit1", "Unit1 reference", serviceDetails,
                supplierData.getMarketplace(0));

        UserGroup unit2 = createUnitAndAssignVisibleService(customerData,
                "Unit2", "Unit2 reference", serviceDetails,
                supplierData.getMarketplace(0));

        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2012-12-01 00:00:00")
                - DateTimeHandling.weeksToMillis(0.3));

        VOSubscriptionDetails subDetails = subscribeWithAssignToUnit(
                customerData.getAdminUser(),
                "SCENARIO10_PERUNIT_WEEK_CHSRVINBP", unit1.getKey(),
                serviceDetails,
                DateTimeHandling.calculateMillis("2012-12-01 00:00:00")
                        - DateTimeHandling.weeksToMillis(0.3), "ADMIN");

        // create upgrade service
        VOServiceDetails upgradeService = createAndRegisterCompatibleService(
                supplierData, TestPriceModel.EXAMPLE_PERUNIT_WEEK_ROLES,
                serviceDetails, "SCENARIO10_PERUNIT_WEEK_UPGRADE_SERVICE");

        // upgrade subscription .3 weeks after Billing period Start Time
        VOSubscriptionDetails upgradedSubDetails = upgrade(
                customerData.getAdminUser(), subDetails, upgradeService,
                DateTimeHandling.calculateMillis("2012-12-01 00:00:00")
                        + DateTimeHandling.weeksToMillis(0.3));

        // change the service ID
        serviceDetails = updateServiceId(supplierData, serviceDetails,
                "SCENARIO10_PERUNIT_WEEK_NEW_SERVICE", "2012-12-20 00:00:00");

        // change subscription unit
        upgradedSubDetails = changeSubscriptionUnit(customerData.getAdminKey(),
                upgradedSubDetails, unit2.getKey(), "2012-12-20 00:00:00");

        unsubscribe(customerData.getAdminKey(),
                upgradedSubDetails.getSubscriptionId(),
                DateTimeHandling.calculateMillis("2013-01-01 00:00:00")
                        + DateTimeHandling.weeksToMillis(1.5));

        resetCutOffDay(supplierData.getAdminKey());
        cacheTestData("SCENARIO10_PERUNIT_WEEK_CHSRVINBP", new TestData(
                supplierData));
    }

    /**
     * Creates the subscription data with upgrade options. Usage started before
     * the billing period start time.
     * 
     * Subscription is upgraded to another service with different price models.
     * Service ID is changed and unit is removed afterwards.
     */
    public void createSubUsageScenario10_changeServiceIdAndRemoveUnitInBP()
            throws Exception {
        VendorData supplierData = setupNewSupplier("2012-11-01 08:00:00");
        setCutOffDay(supplierData.getAdminKey(), 1);
        CustomerData customerData = registerCustomer(supplierData);

        VOServiceDetails serviceDetails = createPublishActivateService(
                supplierData, TestPriceModel.EXAMPLE_PERUNIT_WEEK_ROLES_PARS,
                "SCENARIO10_RUCSINBP_PU_WEEK_SERVICE");

        UserGroup unit1 = createUnitAndAssignVisibleService(customerData,
                "Unit1", "Unit1 reference", serviceDetails,
                supplierData.getMarketplace(0));

        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2012-12-01 00:00:00")
                - DateTimeHandling.weeksToMillis(0.3));

        VOSubscriptionDetails subDetails = subscribeWithAssignToUnit(
                customerData.getAdminUser(),
                "SCENARIO10_PERUNIT_WEEK_RUCSINBP", unit1.getKey(),
                serviceDetails,
                DateTimeHandling.calculateMillis("2012-12-01 00:00:00")
                        - DateTimeHandling.weeksToMillis(0.3), "ADMIN");

        // create upgrade service
        VOServiceDetails upgradeService = createAndRegisterCompatibleService(
                supplierData, TestPriceModel.EXAMPLE_PERUNIT_WEEK_ROLES,
                serviceDetails, "SCENARIO10_RUCSINBP_PU_WEEK_UPGSERVICE");

        // upgrade subscription .3 weeks after Billing period Start Time
        VOSubscriptionDetails upgradedSubDetails = upgrade(
                customerData.getAdminUser(), subDetails, upgradeService,
                DateTimeHandling.calculateMillis("2012-12-01 00:00:00")
                        + DateTimeHandling.weeksToMillis(0.3));

        // change the service ID
        serviceDetails = updateServiceId(supplierData, serviceDetails,
                "SCENARIO10_RUCSINBP_PU_WEEK_NEW_SERVICE",
                "2012-12-20 00:00:00");

        // Remove subscription unit from subscription
        upgradedSubDetails = changeSubscriptionUnit(customerData.getAdminKey(),
                upgradedSubDetails, 0, "2012-12-20 00:00:00");

        resetCutOffDay(supplierData.getAdminKey());
        cacheTestData("SCENARIO10_PERUNIT_WEEK_RUCSINBP", new TestData(
                supplierData));
    }

    /**
     * Creates the subscription data with upgrade options. Usage started before
     * the billing period start time and terminated before the Billing Period
     * End Time.
     * 
     * Subscription is upgraded to another service with different price models.
     * 
     * Usage terminated after Billing Period End Time
     */
    public void createSubUsageScenario10_changeServiceIdAndUnitInBP_2()
            throws Exception {
        VendorData supplierData = setupNewSupplier("2012-11-01 08:00:00");
        setCutOffDay(supplierData.getAdminKey(), 1);
        CustomerData customerData = registerCustomer(supplierData);

        VOServiceDetails serviceDetails = createPublishActivateService(
                supplierData, TestPriceModel.EXAMPLE_PERUNIT_WEEK_ROLES_PARS,
                "SCENARIO10_PERUNIT_WEEK_SERVICE_2");

        UserGroup unit1 = createUnitAndAssignVisibleService(customerData,
                "Unit1_2", "Unit1_2 reference", serviceDetails,
                supplierData.getMarketplace(0));

        UserGroup unit2 = createUnitAndAssignVisibleService(customerData,
                "Unit2_2", "Unit2_2 reference", serviceDetails,
                supplierData.getMarketplace(0));

        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2012-12-01 00:00:00")
                - DateTimeHandling.weeksToMillis(0.3));

        VOSubscriptionDetails subDetails = subscribeWithAssignToUnit(
                customerData.getAdminUser(),
                "SCENARIO10_PERUNIT_WEEK_CHSRVINBP_2", unit1.getKey(),
                serviceDetails,
                DateTimeHandling.calculateMillis("2012-12-01 00:00:00")
                        - DateTimeHandling.weeksToMillis(0.3), "ADMIN");

        // create upgrade service
        VOServiceDetails upgradeService = createAndRegisterCompatibleService(
                supplierData, TestPriceModel.EXAMPLE_PERUNIT_WEEK_ROLES,
                serviceDetails, "SCENARIO10_PERUNIT_WEEK_UPGR_SERVICE_2");

        // upgrade subscription .3 weeks after Billing period Start Time
        VOSubscriptionDetails upgradedSubDetails = upgrade(
                customerData.getAdminUser(), subDetails, upgradeService,
                DateTimeHandling.calculateMillis("2012-12-01 00:00:00")
                        + DateTimeHandling.weeksToMillis(0.3));

        // change the service ID
        serviceDetails = updateServiceId(supplierData, serviceDetails,
                "SCENARIO10_PERUNIT_WEEK_NEW_SERVICE_2", "2012-12-20 00:00:00");

        // change subscription unit
        upgradedSubDetails = changeSubscriptionUnit(customerData.getAdminKey(),
                upgradedSubDetails, unit2.getKey(), "2012-12-20 00:00:00");

        unsubscribe(customerData.getAdminKey(),
                upgradedSubDetails.getSubscriptionId(),
                DateTimeHandling.calculateMillis("2012-12-31 13:13:13"));

        resetCutOffDay(supplierData.getAdminKey());
        cacheTestData("SCENARIO10_PERUNIT_WEEK_CHSRVINBP_2", new TestData(
                supplierData));
    }

    /**
     * Creates the subscription data with upgrade options. Usage started before
     * the billing period start time and terminated after the Billing Period End
     * Time.
     * 
     * Subscription is upgraded to another service with different price models.
     * 
     * Usage terminated after Billing Period End Time
     */
    public void createSubUsageScenario10_changeServiceIdAndUnitAfterBP()
            throws Exception {
        VendorData supplierData = setupNewSupplier("2012-11-01 08:00:00");
        setCutOffDay(supplierData.getAdminKey(), 1);
        CustomerData customerData = registerCustomer(supplierData);

        VOServiceDetails serviceDetails = createPublishActivateService(
                supplierData, TestPriceModel.EXAMPLE_PERUNIT_WEEK_ROLES_PARS,
                "SCENARIO10_PERUNIT_WEEK_SERVICE");

        UserGroup unit1 = createUnitAndAssignVisibleService(customerData,
                "Unit1", "Unit1 reference", serviceDetails,
                supplierData.getMarketplace(0));

        UserGroup unit2 = createUnitAndAssignVisibleService(customerData,
                "Unit2", "Unit2 reference", serviceDetails,
                supplierData.getMarketplace(0));

        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2012-12-01 00:00:00")
                - DateTimeHandling.weeksToMillis(0.3));

        VOSubscriptionDetails subDetails = subscribeWithAssignToUnit(
                customerData.getAdminUser(),
                "SCENARIO10_PERUNIT_WEEK_CHSRVAFBP", unit1.getKey(),
                serviceDetails,
                DateTimeHandling.calculateMillis("2012-12-01 00:00:00")
                        - DateTimeHandling.weeksToMillis(0.3), "ADMIN");

        // create upgrade service
        VOServiceDetails upgradeService = createAndRegisterCompatibleService(
                supplierData, TestPriceModel.EXAMPLE_PERUNIT_WEEK_ROLES,
                serviceDetails, "SCENARIO10_PERUNIT_WEEK_UPGRADE_SERVICE");

        // upgrade subscription .3 weeks after Billing period Start Time
        VOSubscriptionDetails upgradedSubDetails = upgrade(
                customerData.getAdminUser(), subDetails, upgradeService,
                DateTimeHandling.calculateMillis("2012-12-01 00:00:00")
                        + DateTimeHandling.weeksToMillis(0.3));

        // change the service ID
        serviceDetails = updateServiceId(supplierData, serviceDetails,
                "SCENARIO10_PERUNIT_WEEK_NEW_SERVICE", "2013-01-03 00:00:00");

        // change the subscription unit
        upgradedSubDetails = changeSubscriptionUnit(customerData.getAdminKey(),
                upgradedSubDetails, unit2.getKey(), "2013-01-03 00:00:00");

        resetCutOffDay(supplierData.getAdminKey());
        cacheTestData("SCENARIO10_PERUNIT_WEEK_CHSRVAFBP", new TestData(
                supplierData));
    }

    /**
     * Creates the subscription data with multiple suspended and resumed
     * periods. Usage started before the billing period start time and
     * terminated after the Billing Period End Time. Unit in which the
     * subscription was suspended and resumed multiple times must only be
     * charged once.
     * 
     * Usage terminated after Billing Period End Time
     */
    public void createSubUsageScenario11() throws Exception {
        long usageStartTime = DateTimeHandling
                .calculateMillis("2012-12-01 00:00:00")
                - DateTimeHandling.weeksToMillis(0.3);
        BillingIntegrationTestBase.setDateFactoryInstance(usageStartTime);

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "SCENARIO11_PERUNIT_WEEK", TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_WEEK_ROLES_PARS,
                        technicalService, supplierMarketplace);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);

        // Create own customer, because the customer's payment types are changed
        // later on
        String customerAdminId = "Scenario011PUWeekCustomerAdmin";
        VOOrganization customer = orgSetup.registerCustomer(
                "Scenario011PUWeekCustomer",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                customerAdminId, supplierMarketplace.getMarketplaceId(),
                basicSetup.getSupplierOrgID());
        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "SCENARIO11_PERUNIT_WEEK", serviceDetails, customerAdmin, role);

        // suspend/resume subscription several times by removing/restoring the
        // customer's payment types
        long suspResTime = DateTimeHandling
                .calculateMillis("2012-12-01 00:00:00")
                + DateTimeHandling.weeksToMillis(0.3);
        BillingIntegrationTestBase.setDateFactoryInstance(suspResTime);

        container.login(basicSetup.getSupplierAdminKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);

        // Mon 2012-12-03 02:24:00
        paymentSetup.deleteCustomerPaymentTypes(customer);
        suspResTime += 86400000;
        BillingIntegrationTestBase.setDateFactoryInstance(suspResTime);
        // Tue 2012-12-04 02:24:00
        paymentSetup.reassignCustomerPaymentTypes(customer);
        suspResTime += 864000;
        BillingIntegrationTestBase.setDateFactoryInstance(suspResTime);
        // Tue 2012-12-04 02:38:24
        paymentSetup.deleteCustomerPaymentTypes(customer);
        suspResTime += 864000;
        BillingIntegrationTestBase.setDateFactoryInstance(suspResTime);
        // Tue 2012-12-04 02:52:48
        paymentSetup.reassignCustomerPaymentTypes(customer);
        suspResTime += 604800000;
        BillingIntegrationTestBase.setDateFactoryInstance(suspResTime);
        // Tue 2012-12-11 02:52:48
        paymentSetup.deleteCustomerPaymentTypes(customer);
        suspResTime += 950400000;
        BillingIntegrationTestBase.setDateFactoryInstance(suspResTime);
        // Sat 2012-12-22 02:52:48
        paymentSetup.reassignCustomerPaymentTypes(customer);

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO11_PERUNIT_WEEK", subDetails);
        BillingIntegrationTestBase.updateCustomerListForTests(
                "SCENARIO11_PERUNIT_WEEK", customer);
    }

    /**
     * Creates the subscription data with multiple suspended and resumed
     * periods. Usage started before the billing period start time and
     * terminated after the Billing Period End Time. Unit in which the
     * subscription was suspended and resumed multiple times must only be
     * charged once. The subscription is suspended by deleting the billing
     * contact of the customer.
     * 
     * Usage terminated after Billing Period End Time
     */
    public void createSubUsageScenario11a() throws Exception {
        long usageStartTime = DateTimeHandling
                .calculateMillis("2012-12-01 00:00:00")
                - DateTimeHandling.weeksToMillis(0.3);
        BillingIntegrationTestBase.setDateFactoryInstance(usageStartTime);

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "SCENARIO11a_PERUNIT_WEEK", TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_WEEK_ROLES_PARS,
                        technicalService, supplierMarketplace);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);

        // Create own customer, because the customer's payment types are changed
        // later on
        String customerAdminId = "Scenario011aPUWeekCustomerAdmin";
        VOOrganization customer = orgSetup.registerCustomer(
                "Scenario011aPUWeekCustomer",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                customerAdminId, supplierMarketplace.getMarketplaceId(),
                basicSetup.getSupplierOrgID());
        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup
                .subscribeToService("SCENARIO11a_PERUNIT_WEEK", serviceDetails,
                        customerAdmin, role);

        // suspend/resume subscription several times by removing/restoring the
        // customer's billing contact
        long suspResTime = DateTimeHandling
                .calculateMillis("2012-12-01 00:00:00")
                + DateTimeHandling.weeksToMillis(0.3);
        BillingIntegrationTestBase.setDateFactoryInstance(suspResTime);

        // Mon 2012-12-03 02:24:00
        subDetails = deleteBillingContactsAndUpdateSub(customerAdmin.getKey(),
                subDetails);

        suspResTime += 86400000;
        BillingIntegrationTestBase.setDateFactoryInstance(suspResTime);
        // Tue 2012-12-04 02:24:00
        subDetails = restoreBillingContactForSubscription(
                customerAdmin.getKey(), subDetails);

        suspResTime += 864000;
        BillingIntegrationTestBase.setDateFactoryInstance(suspResTime);
        // Tue 2012-12-04 02:38:24
        subDetails = deleteBillingContactsAndUpdateSub(customerAdmin.getKey(),
                subDetails);

        suspResTime += 864000;
        BillingIntegrationTestBase.setDateFactoryInstance(suspResTime);
        // Tue 2012-12-04 02:52:48
        subDetails = restoreBillingContactForSubscription(
                customerAdmin.getKey(), subDetails);

        suspResTime += 604800000;
        BillingIntegrationTestBase.setDateFactoryInstance(suspResTime);
        // Tue 2012-12-11 02:52:48
        subDetails = deleteBillingContactsAndUpdateSub(customerAdmin.getKey(),
                subDetails);

        suspResTime += 950400000;
        BillingIntegrationTestBase.setDateFactoryInstance(suspResTime);
        // Sat 2012-12-22 02:52:48
        subDetails = restoreBillingContactForSubscription(
                customerAdmin.getKey(), subDetails);

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO11a_PERUNIT_WEEK", subDetails);
        BillingIntegrationTestBase.updateCustomerListForTests(
                "SCENARIO11a_PERUNIT_WEEK", customer);
    }

    /**
     * Creates one subscription with severeal parameter modifications. The usage
     * start and end date in the billing result must be correct, that means not
     * the start of the time unit, but the actual usage time.
     */
    public void createSubUsageScenario16() throws Exception {
        long usageStartTime = DateTimeHandling
                .calculateMillis("2012-12-01 00:00:00")
                - DateTimeHandling.daysToMillis(15);
        BillingIntegrationTestBase.setDateFactoryInstance(usageStartTime);

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "SCENARIO16_PERUNIT_WEEK", TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_WEEK_ROLES_PARS,
                        technicalService, supplierMarketplace);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "SCENARIO16_PERUNIT_WEEK", serviceDetails,
                basicSetup.getCustomerUser1(), role);

        // Parameters modification 9 days before Billing Period Start Time
        long usageModificationTime = DateTimeHandling
                .calculateMillis("2012-12-01 00:00:00")
                - DateTimeHandling.daysToMillis(9);
        subDetails = subscrSetup.modifyParameterForSubscription(subDetails,
                usageModificationTime, "BOOLEAN_PARAMETER", "false");
        subDetails = subscrSetup.modifyParameterForSubscription(subDetails,
                usageModificationTime, "MAX_FOLDER_NUMBER", "13");
        // Parameters modification 3 days after Billing Period Start Time
        usageModificationTime = DateTimeHandling
                .calculateMillis("2012-12-01 00:00:00")
                + DateTimeHandling.daysToMillis(3);
        subDetails = subscrSetup.modifyParameterForSubscription(subDetails,
                usageModificationTime, "HAS_OPTIONS", "3");
        // Parameters modification 10 days after Billing Period Start Time
        usageModificationTime = DateTimeHandling
                .calculateMillis("2012-12-01 00:00:00")
                + DateTimeHandling.daysToMillis(10);
        subDetails = subscrSetup.modifyParameterForSubscription(subDetails,
                usageModificationTime, "BOOLEAN_PARAMETER", "true");

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO16_PERUNIT_WEEK", subDetails);
    }

    /**
     * Subscribe to a service with a per unit price model (charged period week)
     * in February 2013. The service is deleted before the subscription ends.
     * The technical service is deleted after the subscription has been
     * terminated, but before the end of the month. Nevertheless all weeks of
     * February must be charged. See also Bug 10268.
     */
    public void createWeekScenarioServiceDeletion() throws Exception {
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-02-02 05:00:00"));

        // Create own technical service
        container.login(basicSetup.getSupplierAdminKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        serviceSetup
                .importTechnicalServices(BaseAdmUmTest.TECHNICAL_SERVICE_EXAMPLE2_XML);
        VOTechnicalService example2Service = serviceSetup
                .getTechnicalService(VOTechServiceFactory.TECH_SERVICE_EXAMPLE2_ID);

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(), "SERVICEDEL_PU_WEEK",
                        TestService.EXAMPLE2,
                        TestPriceModel.EXAMPLE_PERUNIT_WEEK_ROLES_PARS,
                        example2Service, supplierMarketplace);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "SERVICEDEL_PU_WEEK", serviceDetails,
                basicSetup.getCustomerUser2(), role);

        // Delete the marketable service -> the subscription must not be
        // affected
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-02-14 13:00:00"));
        container.login(basicSetup.getSupplierAdminKey(), ROLE_SERVICE_MANAGER);
        serviceSetup.deleteMarketableService(serviceDetails);

        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-02-25 15:00:00"));
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        // Delete also the technical service
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-02-27 21:00:00"));
        container.login(basicSetup.getSupplierAdminKey(),
                ROLE_TECHNOLOGY_MANAGER);
        serviceSetup.deleteTechnicalService(example2Service);

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SERVICEDEL_PU_WEEK", subDetails);
    }

    /**
     * Creates the subscription test data for long-lasting scenario.
     */
    public void createWeekScenarioLongUsage() throws Exception {
        BillingIntegrationTestBase
                .setDateFactoryInstance("2013-01-05 12:00:00");

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "LONG_USAGE_PUWEEK_SERVICE", TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_WEEK_ROLES,
                        technicalService, supplierMarketplace);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                basicSetup.getCustomerAdminKey(), "LONG_USAGE_PUWEEK",
                serviceDetails, basicSetup.getSecondCustomerUser1(), role);

        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-04-29 08:12:00"));
        subDetails.setSubscriptionId("LONG_USAGE_PUWEEK_SubID2");
        subDetails = subscrSetup.modifySubscription(
                basicSetup.getCustomerAdminKey(), subDetails, null);

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "LONG_USAGE_PUWEEK", subDetails);
    }

    /**
     * Test for per unit/week price model with stepped event prices, stepped
     * user assignment costs and stepped parameter period fees
     */
    public void createPerUnitWeekSteppedScenario() throws Exception {
        setDateFactory("2013-04-01 00:00:00");

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "PER_UNIT_WEEK_STEPPED_SERVICE",
                        TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_WEEK_EVENTS_PARS_STEPPED,
                        technicalService, supplierMarketplace);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);

        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                basicSetup.getCustomerAdminKey(), "PER_UNIT_WEEK_STEPPED",
                serviceDetails, basicSetup.getSecondCustomerUser1(),
                VOServiceFactory.getRole(serviceDetails, "ADMIN"));

        container.login(basicSetup.getSupplierAdminKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        subscrSetup.recordEventForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-04-15 00:00:00"),
                "FILE_DOWNLOAD", 100);

        setDateFactory("2013-04-29 00:00:00");
        subscrSetup.unsubscribeToService(basicSetup.getCustomerAdminKey(),
                subDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PER_UNIT_WEEK_STEPPED", subDetails);
    }

    /**
     * Test for pro rata/week price model with stepped event prices, stepped
     * user assignment costs and stepped parameter period fees
     */
    public void createProRataWeekSteppedScenario() throws Exception {
        setDateFactory("2013-04-01 00:00:00");

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "PRO_RATA_WEEK_STEPPED_SERVICE", TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_RATA_WEEK_EVENTS_PARS_STEPPED,
                        technicalService, supplierMarketplace);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);

        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                basicSetup.getCustomerAdminKey(), "PRO_RATA_WEEK_STEPPED",
                serviceDetails, basicSetup.getSecondCustomerUser1(),
                VOServiceFactory.getRole(serviceDetails, "ADMIN"));

        container.login(basicSetup.getSupplierAdminKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        subscrSetup.recordEventForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-04-15 00:00:00"),
                "FILE_DOWNLOAD", 100);

        setDateFactory("2013-04-29 00:00:00");
        subscrSetup.unsubscribeToService(basicSetup.getCustomerAdminKey(),
                subDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PRO_RATA_WEEK_STEPPED", subDetails);
    }
}
