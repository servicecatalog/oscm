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
import org.oscm.billingservice.setup.VOPriceModelFactory;
import org.oscm.billingservice.setup.VOPriceModelFactory.TestPriceModel;
import org.oscm.billingservice.setup.VOServiceFactory;
import org.oscm.billingservice.setup.VOServiceFactory.TestService;
import org.oscm.billingservice.setup.VendorData;
import org.oscm.test.DateTimeHandling;
import org.oscm.internal.types.enumtypes.PriceModelType;
import org.oscm.internal.types.enumtypes.PricingPeriod;
import org.oscm.internal.vo.VOPriceModel;
import org.oscm.internal.vo.VORoleDefinition;
import org.oscm.internal.vo.VOServiceDetails;
import org.oscm.internal.vo.VOSubscriptionDetails;

/**
 * @author malhotra
 * 
 */
public class PeriodMonthSetup extends IntegrationTestSetup {

    /**
     * Creates the subscription test data for long-lasting scenario based on the
     * calculation of billing period. Usage started before the billing period
     * and ended after the billing period (long-lasting usages).
     * 
     * usage started before Billing Period Start Time
     * 
     * usage ended after Billing period End Time
     */
    public void createSubUsageScenario01() throws Exception {
        long usageStartTime = DateTimeHandling
                .calculateMillis("2012-12-01 00:00:00")
                - DateTimeHandling.daysToMillis(15.5);
        BillingIntegrationTestBase.setDateFactoryInstance(usageStartTime);

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "SCENARIO01_PERUNIT_MONTH", TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_MONTH_ROLES,
                        technicalService, supplierMarketplace);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "SCENARIO01_PERUNIT_MONTH", serviceDetails,
                basicSetup.getCustomerUser1(), role);

        long usageEndTime = DateTimeHandling
                .calculateMillis("2013-01-01 00:00:00")
                + DateTimeHandling.daysToMillis(15.5);
        BillingIntegrationTestBase.setDateFactoryInstance(usageEndTime);
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO01_PERUNIT_MONTH", subDetails);
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
                - DateTimeHandling.daysToMillis(20.5);
        BillingIntegrationTestBase.setDateFactoryInstance(usageStartTime);

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "SCENARIO02_PERUNIT_MONTH", TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_MONTH_ROLES,
                        technicalService, supplierMarketplace);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "SCENARIO02_PERUNIT_MONTH", serviceDetails,
                basicSetup.getCustomerUser2(), role);

        long usageEndTime = DateTimeHandling
                .calculateMillis("2012-12-01 00:00:00")
                - DateTimeHandling.daysToMillis(5);
        BillingIntegrationTestBase.setDateFactoryInstance(usageEndTime);
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO02_PERUNIT_MONTH", subDetails);
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
                - DateTimeHandling.daysToMillis(6.5);
        BillingIntegrationTestBase.setDateFactoryInstance(usageStartTime);

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "SCENARIO03_PERUNIT_MONTH", TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_MONTH_ROLES,
                        technicalService, supplierMarketplace);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "SCENARIO03_PERUNIT_MONTH", serviceDetails,
                basicSetup.getCustomerUser1(), role);

        long usageEndTime = DateTimeHandling
                .calculateMillis("2012-12-01 00:00:00")
                + DateTimeHandling.daysToMillis(10);
        BillingIntegrationTestBase.setDateFactoryInstance(usageEndTime);
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO03_PERUNIT_MONTH", subDetails);
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
                + DateTimeHandling.daysToMillis(5);
        BillingIntegrationTestBase.setDateFactoryInstance(usageStartTime);

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "SCENARIO04_PERUNIT_MONTH", TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_MONTH_ROLES,
                        technicalService, supplierMarketplace);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "SCENARIO04_PERUNIT_MONTH", serviceDetails,
                basicSetup.getCustomerUser2(), role);

        long usageEndTime = DateTimeHandling
                .calculateMillis("2013-01-01 00:00:00")
                - DateTimeHandling.daysToMillis(5);
        BillingIntegrationTestBase.setDateFactoryInstance(usageEndTime);
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO04_PERUNIT_MONTH", subDetails);
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
                        "SCENARIO05_PERUNIT_MONTH", TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_MONTH_ROLES,
                        technicalService, supplierMarketplace);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "SCENARIO05_PERUNIT_MONTH", serviceDetails,
                basicSetup.getCustomerUser1(), role);

        long usageEndTime = DateTimeHandling
                .calculateMillis("2013-01-01 00:00:00")
                + DateTimeHandling.daysToMillis(5);
        BillingIntegrationTestBase.setDateFactoryInstance(usageEndTime);
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO05_PERUNIT_MONTH", subDetails);
    }

    /**
     * Creates the subscription test data for the scenario after the billing
     * period end time. Usage started after the billing period end time and
     * ended after the billing period end time.
     * 
     * usage started 5 days after Billing Period End Time
     * 
     * usage ended 15 days after Billing Period End Time
     */
    public void createSubUsageScenario06() throws Exception {
        long usageStartTime = DateTimeHandling
                .calculateMillis("2013-01-01 00:00:00")
                + DateTimeHandling.daysToMillis(5);
        BillingIntegrationTestBase.setDateFactoryInstance(usageStartTime);

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "SCENARIO06_PERUNIT_MONTH", TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_MONTH_ROLES,
                        technicalService, supplierMarketplace);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "SCENARIO06_PERUNIT_MONTH", serviceDetails,
                basicSetup.getCustomerUser2(), role);

        long usageEndTime = DateTimeHandling
                .calculateMillis("2013-01-01 00:00:00")
                + DateTimeHandling.daysToMillis(15);
        BillingIntegrationTestBase.setDateFactoryInstance(usageEndTime);
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO06_PERUNIT_MONTH", subDetails);
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
                - DateTimeHandling.daysToMillis(15);
        BillingIntegrationTestBase.setDateFactoryInstance(usageStartTime);

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "SCENARIO07_PERUNIT_MONTH", TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_MONTH_ROLES,
                        technicalService, supplierMarketplace);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "SCENARIO07_PERUNIT_MONTH", serviceDetails,
                basicSetup.getCustomerUser2(), role);

        // Parameters modification 9 days before Billing Period Start Time
        long usageModificationTime = DateTimeHandling
                .calculateMillis("2012-12-01 00:00:00")
                - DateTimeHandling.daysToMillis(9);
        subDetails = subscrSetup.modifyParameterForSubscription(subDetails,
                usageModificationTime, "MAX_FOLDER_NUMBER", "13");
        // Parameters modification 3 days after Billing Period Start Time
        usageModificationTime = DateTimeHandling
                .calculateMillis("2012-12-01 00:00:00")
                + DateTimeHandling.daysToMillis(3);
        subDetails = subscrSetup.modifyParameterForSubscription(subDetails,
                usageModificationTime, "MAX_FOLDER_NUMBER", "23");
        // Parameters modification 5 days after Billing Period Start Time
        usageModificationTime = DateTimeHandling
                .calculateMillis("2012-12-01 00:00:00")
                + DateTimeHandling.daysToMillis(5);
        subDetails = subscrSetup.modifyParameterForSubscription(subDetails,
                usageModificationTime, "MAX_FOLDER_NUMBER", "33");

        long usageEndTime = DateTimeHandling
                .calculateMillis("2013-01-01 00:00:00")
                - DateTimeHandling.daysToMillis(3);
        BillingIntegrationTestBase.setDateFactoryInstance(usageEndTime);
        subscrSetup.terminateSubscription(basicSetup.getSupplierAdminKey(),
                subDetails, "");

        resetCutOffDay(basicSetup.getSupplierAdminKey());
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO07_PERUNIT_MONTH", subDetails);
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
                - DateTimeHandling.daysToMillis(15);
        BillingIntegrationTestBase.setDateFactoryInstance(usageStartTime);

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "SCENARIO08_PERUNIT_MONTH", TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_MONTH_ROLES,
                        technicalService, supplierMarketplace);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        // create subscription without user assignment
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "SCENARIO08_PERUNIT_MONTH", serviceDetails, null, role);

        long usageEndTime = DateTimeHandling
                .calculateMillis("2013-01-01 00:00:00")
                - DateTimeHandling.daysToMillis(3);
        BillingIntegrationTestBase.setDateFactoryInstance(usageEndTime);
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO08_PERUNIT_MONTH", subDetails);
    }

    /**
     * Creates the subscription test data for the usage with multiple user
     * assignment for same and different users. Usage started before the billing
     * period start time and terminated after the Billing Period End Time.
     * 
     * usage started before Billing Period Start Time
     * 
     * usage terminated after Billing Period End Time
     * 
     * multiple user assignment for same and multiple users
     * 
     * role definitions are changed.
     */
    public void createSubUsageScenario09() throws Exception {
        long usageStartTime = DateTimeHandling
                .calculateMillis("2012-12-01 00:00:00")
                - DateTimeHandling.daysToMillis(15.5);
        BillingIntegrationTestBase.setDateFactoryInstance(usageStartTime);

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "SCENARIO09_PERUNIT_MONTH", TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_MONTH_ROLES,
                        technicalService, supplierMarketplace);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "SCENARIO09_PERUNIT_MONTH", serviceDetails,
                basicSetup.getCustomerUser1(), role);

        // user is deassigned 2 days after Billing Period Start Time
        long userDeassignedTime = DateTimeHandling
                .calculateMillis("2012-12-01 00:00:00")
                + DateTimeHandling.daysToMillis(2);
        BillingIntegrationTestBase.setDateFactoryInstance(userDeassignedTime);
        subscrSetup.revokeUser(basicSetup.getCustomerUser1(),
                subDetails.getSubscriptionId());

        // No user is assigned for 2 days
        // same user is assigned again 4 days after Billing Period Start Time
        long userAssignedTime = DateTimeHandling
                .calculateMillis("2012-12-01 00:00:00")
                + DateTimeHandling.daysToMillis(4);
        BillingIntegrationTestBase.setDateFactoryInstance(userAssignedTime);
        VORoleDefinition changedRole = VOServiceFactory.getRole(serviceDetails,
                "USER");
        subscrSetup.addUser(basicSetup.getCustomerUser1(), changedRole,
                subDetails.getSubscriptionId());

        long usageEndTime = DateTimeHandling
                .calculateMillis("2013-01-01 00:00:00")
                + DateTimeHandling.daysToMillis(15.5);
        BillingIntegrationTestBase.setDateFactoryInstance(usageEndTime);
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO09_PERUNIT_MONTH", subDetails);
    }

    /**
     * Creates the subscription data with upgrade options. Usage started before
     * the billing period start time and terminated after the Billing Period End
     * Time.
     * 
     * Subscription is upgraded to another service with different price models.
     */
    public void createSubUsageScenario10() throws Exception {
        long usageStartTime = DateTimeHandling
                .calculateMillis("2012-12-01 00:00:00")
                - DateTimeHandling.daysToMillis(15.5);
        BillingIntegrationTestBase.setDateFactoryInstance(usageStartTime);

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "SCENARIO10_PERUNIT_MONTH", TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_MONTH_ROLES,
                        technicalService, supplierMarketplace);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "SCENARIO10_PERUNIT_MONTH", serviceDetails,
                basicSetup.getCustomerUser1(), role);

        // create upgraded service
        VOServiceDetails upgradeService = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "SCENARIO10_PERUNIT_MONTH_UPGRADE_SERVICE",
                        TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_MONTH_ROLES_2,
                        technicalService, supplierMarketplace);

        serviceSetup.registerCompatibleServices(
                basicSetup.getSupplierAdminKey(), serviceDetails,
                upgradeService);

        // upgrade subscription 10 days after Billing period Start Time
        long usageUpgradeTime = DateTimeHandling
                .calculateMillis("2012-12-01 00:00:00")
                + DateTimeHandling.daysToMillis(10);
        BillingIntegrationTestBase.setDateFactoryInstance(usageUpgradeTime);
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(subDetails, upgradeService);

        long usageEndTime = DateTimeHandling
                .calculateMillis("2013-01-01 00:00:00")
                + DateTimeHandling.daysToMillis(15.5);
        BillingIntegrationTestBase.setDateFactoryInstance(usageEndTime);
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO10_PERUNIT_MONTH", subDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO10_PERUNIT_MONTH", upgradedSubDetails);
    }

    /**
     * Creates the subscription data with different price model types. Usage
     * started before the billing period start time and terminated after the
     * Billing Period End Time. The subscription is upgraded from PER_UNIT to
     * PRO_RATA.
     */
    public void createSubUsageScenario12() throws Exception {
        long usageStartTime = DateTimeHandling
                .calculateMillis("2012-12-01 00:00:00")
                - DateTimeHandling.daysToMillis(5.5);
        BillingIntegrationTestBase.setDateFactoryInstance(usageStartTime);

        VOServiceDetails perUnitServiceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "SCENARIO12_PERUNIT_MONTH_SERVICE",
                        TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_MONTH_ROLES,
                        technicalService, supplierMarketplace);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);

        VORoleDefinition role = VOServiceFactory.getRole(perUnitServiceDetails,
                "ADMIN");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "SCENARIO12_PRORATA_MONTH", perUnitServiceDetails,
                basicSetup.getCustomerUser1(), role);

        // *****create upgraded service with PRO RATA price model type***
        VOServiceDetails proRataService = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "SCENARIO12_PRORATA_MONTH", TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PRORATA_MONTH_ROLES,
                        technicalService, supplierMarketplace);
        serviceSetup.registerCompatibleServices(
                basicSetup.getSupplierAdminKey(), perUnitServiceDetails,
                proRataService);

        // upgrade subscription 10 days after Billing period Start Time
        long usageUpgradeTime = DateTimeHandling
                .calculateMillis("2012-12-01 00:00:00")
                + DateTimeHandling.daysToMillis(10);
        BillingIntegrationTestBase.setDateFactoryInstance(usageUpgradeTime);
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(subDetails, proRataService);

        long usageEndTime = DateTimeHandling
                .calculateMillis("2013-01-01 00:00:00")
                + DateTimeHandling.daysToMillis(5.5);
        BillingIntegrationTestBase.setDateFactoryInstance(usageEndTime);
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO12_PRORATA_MONTH", subDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO12_PRORATA_MONTH", upgradedSubDetails);
    }

    /**
     * Creates the subscription data with different price model. The price model
     * user assignment cost and the period fee of the subscription price model
     * are changed during the billing period.
     * 
     * Usage started before the billing period start time and terminated after
     * the Billing Period End Time.
     */
    public void createSubUsageScenario13() throws Exception {
        long usageStartTime = DateTimeHandling
                .calculateMillis("2012-12-01 00:00:00")
                - DateTimeHandling.daysToMillis(15);
        BillingIntegrationTestBase.setDateFactoryInstance(usageStartTime);

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "SCENARIO13_PERUNIT_MONTH", TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_MONTH_ROLES,
                        technicalService, supplierMarketplace);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "SCENARIO13_PERUNIT_MONTH", serviceDetails,
                basicSetup.getCustomerUser2(), role);

        // price model modification 3 days after Billing Period Start Time
        long usageModificationTime = DateTimeHandling
                .calculateMillis("2012-12-01 00:00:00")
                + DateTimeHandling.daysToMillis(3);
        BillingIntegrationTestBase
                .setDateFactoryInstance(usageModificationTime);

        VOPriceModel newSubPriceModel = VOPriceModelFactory.modifyPriceModel(
                subDetails.getPriceModel(), PriceModelType.PER_UNIT,
                PricingPeriod.MONTH, "EUR", new BigDecimal("123.00"),
                new BigDecimal("778.00"), new BigDecimal("445.00"), 0);
        subscrSetup.savePriceModelForSubscription(
                basicSetup.getSupplierAdminKey(), subDetails, newSubPriceModel,
                basicSetup.getCustomer());

        // assign another user
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VORoleDefinition changedRole = VOServiceFactory.getRole(serviceDetails,
                "USER");
        subscrSetup.addUser(basicSetup.getCustomerUser1(), changedRole,
                subDetails.getSubscriptionId());

        // Usage ended 5.5 days after Billing period End Time
        long usageEndTime = DateTimeHandling
                .calculateMillis("2013-01-01 00:00:00")
                + DateTimeHandling.daysToMillis(5.5);
        BillingIntegrationTestBase.setDateFactoryInstance(usageEndTime);
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO13_PERUNIT_MONTH", subDetails);
    }

    /**
     * Starts a subscription with one user. The subscription is not terminated.
     */
    public void createSubUsageScenario14() throws Exception {
        long usageStartTime = DateTimeHandling
                .calculateMillis("2012-11-28 21:36:00");
        BillingIntegrationTestBase.setDateFactoryInstance(usageStartTime);

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "SCENARIO14_PERUNIT_MONTH", TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_MONTH_ROLES,
                        technicalService, supplierMarketplace);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "SCENARIO14_PERUNIT_MONTH", serviceDetails,
                basicSetup.getCustomerUser1(), role);

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO14_PERUNIT_MONTH", subDetails);
    }

    /**
     * Creates 2 subscriptions with free trial period. Terminates the first
     * before the free trail period is over, the other after that. The first one
     * must not create overall costs.
     */
    public void createSubUsageScenario15() throws Exception {
        final long usageStartTime = DateTimeHandling
                .calculateMillis("2012-12-01 00:00:00")
                + DateTimeHandling.daysToMillis(1);
        BillingIntegrationTestBase.setDateFactoryInstance(usageStartTime);

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "SCENARIO15_PERUNIT_MONTH", TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_MONTH_ROLES_PARS_FREEP,
                        technicalService, supplierMarketplace);

        VOServiceDetails serviceDetails2 = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "SCENARIO15_PERUNIT_MONTH_SERVICE_2",
                        TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_MONTH_ROLES_PARS_FREEP,
                        technicalService, supplierMarketplace);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "SCENARIO15_PERUNIT_MONTH", serviceDetails,
                basicSetup.getCustomerUser1(), role);
        VOSubscriptionDetails subDetails2 = subscrSetup.subscribeToService(
                "SCENARIO15_PERUNIT_MONTH_2", serviceDetails2,
                basicSetup.getCustomerUser1(), role);

        // First subscription ends 2 days after Billing Period Start Time (in
        // the free trial period)
        long usageEndTime = DateTimeHandling
                .calculateMillis("2012-12-01 00:00:00")
                + DateTimeHandling.daysToMillis(2);
        BillingIntegrationTestBase.setDateFactoryInstance(usageEndTime);
        subscrSetup.terminateSubscription(basicSetup.getSupplierAdminKey(),
                subDetails, "alles quatsch");
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO15_PERUNIT_MONTH", subDetails);

        // Second subscription ends 9 days after Billing Period Start Time
        // (after the free trial period)
        usageEndTime = DateTimeHandling.calculateMillis("2012-12-01 00:00:00")
                + DateTimeHandling.daysToMillis(9);
        BillingIntegrationTestBase.setDateFactoryInstance(usageEndTime);
        subscrSetup.terminateSubscription(basicSetup.getSupplierAdminKey(),
                subDetails2, "alles käse");

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO15_PERUNIT_MONTH_2", subDetails2);
    }

    /**
     * Creates the subscription test data for the scenario with flexible billing
     * offset,invocation time and cut off days.
     */
    public void createSubUsageScenario18() throws Exception {
        long usageStartTime = DateTimeHandling
                .calculateMillis("2013-02-05 00:00:00")
                - DateTimeHandling.daysToMillis(5.5);
        BillingIntegrationTestBase.setDateFactoryInstance(usageStartTime);

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "SCENARIO18_PERUNIT_MONTH", TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_MONTH_ROLES_PARS_FREEP,
                        technicalService, supplierMarketplace);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 5);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "SCENARIO18_PERUNIT_MONTH", serviceDetails,
                basicSetup.getCustomerUser1(), role);

        long usageEndTime = DateTimeHandling
                .calculateMillis("2013-02-05 00:00:00")
                + DateTimeHandling.daysToMillis(11);
        BillingIntegrationTestBase.setDateFactoryInstance(usageEndTime);
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO18_PERUNIT_MONTH", subDetails);
    }

    /**
     * Creates the subscription with events.
     */
    public void createSubUsageScenario19() throws Exception {
        long usageStartTime = DateTimeHandling
                .calculateMillis("2012-12-01 00:00:00")
                - DateTimeHandling.daysToMillis(6.5);
        BillingIntegrationTestBase.setDateFactoryInstance(usageStartTime);

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "SCENARIO19_PERUNIT_MONTH",
                        TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_MONTH_ROLES_PARS_EVENTS_FREEP,
                        technicalService, supplierMarketplace);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "SCENARIO19_PERUNIT_MONTH", serviceDetails,
                basicSetup.getCustomerUser1(), role);

        // record one event after 8 days
        container.login(basicSetup.getSupplierAdminKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        subscrSetup.recordEventForSubscription(subDetails, usageStartTime
                + DateTimeHandling.daysToMillis(8), "FILE_DOWNLOAD", 2);

        // record other event after 10 days
        subscrSetup.recordEventForSubscription(subDetails, usageStartTime
                + DateTimeHandling.daysToMillis(10), "FILE_UPLOAD", 2);

        long usageEndTime = DateTimeHandling
                .calculateMillis("2012-12-01 00:00:00")
                + DateTimeHandling.daysToMillis(10);
        BillingIntegrationTestBase.setDateFactoryInstance(usageEndTime);
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO19_PERUNIT_MONTH", subDetails);
    }

    /**
     * Subscribe to a service from November 2012 until January 2013 (cutoff day
     * is 1, so the subscription goes over 3 billing periods). A specific price
     * model is created for the customer, who has a discount.
     */
    public void createMonthScenario01CustomerPriceModel() throws Exception {

        VendorData supplierData = setupNewSupplier("2012-11-01 12:00:00");
        setCutOffDay(supplierData.getAdminKey(), 1);

        CustomerData customerData = registerCustomerWithDiscount(supplierData,
                new BigDecimal("25.00"),
                DateTimeHandling.calculateMillis("2012-12-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-01-01 01:00:00"));

        VOServiceDetails serviceDetails = createPublishService(supplierData,
                TestPriceModel.EXAMPLE_PERUNIT_MONTH_ROLES,
                "SCENARIO01_PU_MONTH_CUST_PM_SERVICE");

        VOServiceDetails customerServiceDetails = serviceSetup
                .savePriceModelForCustomer(serviceDetails,
                        TestPriceModel.EXAMPLE_PERUNIT_MONTH_ROLES_2,
                        customerData.getOrganization());
        customerServiceDetails = serviceSetup
                .activateMarketableService(customerServiceDetails);

        VOSubscriptionDetails subDetails = subscribe(
                customerData.getAdminUser(), "SCENARIO01_PU_MONTH_CUST_PM",
                customerServiceDetails, "2012-11-15 12:00:00", "ADMIN");

        unsubscribe(customerData.getAdminKey(), subDetails.getSubscriptionId(),
                "2013-01-16 12:00:00");

        resetCutOffDay(supplierData.getAdminKey());

        cacheTestData("SCENARIO01_PU_MONTH_CUST_PM", new TestData(supplierData));
    }

    /**
     * Subscribe to a service from November 2012 until January 2013 (cutoff day
     * is 1, so the subscription goes over 3 billing periods). A specific price
     * model is created for the customer, who has a discount. The ID of the
     * service template is changed in the second billing period.
     */
    public void createMonthScenario01CustomerPriceModel_changeServiceId()
            throws Exception {

        VendorData supplierData = setupNewSupplier("2012-11-01 12:00:00");
        setCutOffDay(supplierData.getAdminKey(), 1);

        CustomerData customerData = registerCustomerWithDiscount(supplierData,
                new BigDecimal("25.00"),
                DateTimeHandling.calculateMillis("2012-12-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-01-01 01:00:00"));

        VOServiceDetails serviceDetails = createPublishService(supplierData,
                TestPriceModel.EXAMPLE_PERUNIT_MONTH_ROLES,
                "SCENARIO01_PU_MONTH_CUST_PM2_SERVICE");

        VOServiceDetails customerServiceDetails = serviceSetup
                .savePriceModelForCustomer(serviceDetails,
                        TestPriceModel.EXAMPLE_PERUNIT_MONTH_ROLES_2,
                        customerData.getOrganization());
        customerServiceDetails = serviceSetup
                .activateMarketableService(customerServiceDetails);

        VOSubscriptionDetails subDetails = subscribe(
                customerData.getAdminUser(), "SCENARIO01_PU_MONTH_CUST_PM2",
                customerServiceDetails, "2012-11-15 12:00:00", "ADMIN");

        // Update ID of service template
        deactivateService(supplierData, customerServiceDetails);
        updateServiceId(supplierData, serviceDetails,
                "SCENARIO01_PU_MONTH_CUST_PM2_NEW_SERVICE",
                "2012-12-15 00:00:00");

        unsubscribe(customerData.getAdminKey(), subDetails.getSubscriptionId(),
                "2013-01-16 12:00:00");

        resetCutOffDay(supplierData.getAdminKey());

        cacheTestData("SCENARIO01_PU_MONTH_CUST_PM2",
                new TestData(supplierData));
    }

}
