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
import org.oscm.test.DateTimeHandling;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.internal.vo.VORoleDefinition;
import org.oscm.internal.vo.VOServiceDetails;
import org.oscm.internal.vo.VOSubscriptionDetails;
import org.oscm.internal.vo.VOUser;

/**
 * @author sdehn
 */
public class AsyncSetup extends IntegrationTestSetup {

    /**
     * See testcases in AsyncCombinations.docx
     */
    public void createAsyncScenario01() throws Exception {
        long supplierKey = basicSetup.getSupplierAdminKey();
        long customerAdminKey = basicSetup.getCustomerAdminKey();
        VOUser customerAdmin = basicSetup.getCustomerAdmin();

        BillingIntegrationTestBase
                .setDateFactoryInstance("2013-05-01 12:00:00");
        setCutOffDay(supplierKey, 1);
        VOServiceDetails service = serviceSetup
                .createPublishAndActivateMarketableService(supplierKey,
                        "test_01", TestService.EXAMPLE_ASYNC,
                        TestPriceModel.EXAMPLE_PERUNIT_MONTH_ROLES_PARS,
                        technicalServiceAsync, supplierMarketplace);

        login("2013-06-01 07:00:00", customerAdminKey, ROLE_ORGANIZATION_ADMIN);
        VORoleDefinition role = VOServiceFactory.getRole(service, "USER");
        VOSubscriptionDetails subscription = subscrSetup.subscribeToService(
                "test_01", service, customerAdmin, role);
        // ASYNC
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-06-01 08:00:00"));
        subscrSetup
                .abortAsyncSubscription(supplierKey,
                        subscription.getSubscriptionId(),
                        basicSetup.getCustomerOrgID());

        BillingIntegrationTestBase.addToCache(subscription);
        resetCutOffDay(supplierKey);
    }

    /**
     * See testcase #1, except that async subscription is never aborted in
     * AsyncCombinations.docx
     */
    public void createAsyncScenario01_01() throws Exception {
        long supplierKey = basicSetup.getSupplierAdminKey();
        long customerAdminKey = basicSetup.getCustomerAdminKey();
        VOUser customerAdmin = basicSetup.getCustomerAdmin();

        BillingIntegrationTestBase
                .setDateFactoryInstance("2013-05-01 12:00:00");
        setCutOffDay(supplierKey, 1);
        VOServiceDetails service = serviceSetup
                .createPublishAndActivateMarketableService(supplierKey,
                        "test_01_01", TestService.EXAMPLE_ASYNC,
                        TestPriceModel.EXAMPLE_PERUNIT_MONTH_ROLES_PARS,
                        technicalServiceAsync, supplierMarketplace);

        login("2013-06-01 07:00:00", customerAdminKey, ROLE_ORGANIZATION_ADMIN);
        VORoleDefinition role = VOServiceFactory.getRole(service, "USER");
        VOSubscriptionDetails subscription = subscrSetup.subscribeToService(
                "test_01_01", service, customerAdmin, role);

        BillingIntegrationTestBase.addToCache(subscription);
        resetCutOffDay(supplierKey);
    }

    /**
     * See testcase #4, PR price model in AsyncCombinations.docx
     */
    public void createAsyncScenario04() throws Exception {
        freePeriodBasicScenario("test_04", "Test02Customer",
                "test04CustomerOrg", 7, TestPriceModel.EXAMPLE_RATA_WEEK_PARAM,
                false, false, null);
    }

    /**
     * See testcase #5, PU price model in AsyncCombinations.docx
     */
    public void createAsyncScenario05() throws Exception {
        freePeriodBasicScenario("test_05", "Test05Customer",
                "test05CustomerOrg", 3,
                TestPriceModel.EXAMPLE_FP_PUDAY_ROLES_PAR_EVENTS, false, false,
                null);
    }

    public void createAsyncScenario05_01() throws Exception {
        freePeriodBasicScenario2("test_05_01", "Test05_01Customer",
                "test05_01CustomerOrg", 7,
                TestPriceModel.EXAMPLE_UNIT_WEEK_PARAM, true, false, null,
                null, false, null);
    }

    public void createAsyncScenario_06() throws Exception {
        String updStartDate = "2013-06-11 12:00:00";
        String updComplDate = "2013-06-20 00:00:00";
        freePeriodBasicScenario2("test_06", "Test_06Customer",
                "test_06CustomerOrg", 0,
                TestPriceModel.EXAMPLE_RATA_WEEK_PARAM, false, true,
                updStartDate, updComplDate, false, null);
    }

    public void createAsyncScenario_07() throws Exception {
        String updStartDate = "2013-06-11 12:00:00";
        String updComplDate = "2013-06-28 00:00:00";
        freePeriodBasicScenario2("test_07", "Test_07Customer",
                "test_07CustomerOrg", 0,
                TestPriceModel.EXAMPLE_UNIT_WEEK_PARAM, false, true,
                updStartDate, updComplDate, false, null);
    }

    public void createAsyncScenario_08() throws Exception {
        String updStartDate = "2013-06-11 12:00:00";
        String updComplDate = "2013-07-02 00:00:00";
        freePeriodBasicScenario2("test_08", "Test_08Customer",
                "test_08CustomerOrg", 0,
                TestPriceModel.EXAMPLE_RATA_WEEK_PARAM, false, true,
                updStartDate, updComplDate, false, null);
    }

    public void createAsyncScenario_09() throws Exception {
        String updStartDate = "2013-06-11 12:00:00";
        String updComplDate = "2013-06-28 00:00:00";
        String terminationDate = "2013-06-27 00:00:00";
        freePeriodBasicScenario2("test_09", "Test_09Customer",
                "test_09CustomerOrg", 0,
                TestPriceModel.EXAMPLE_RATA_WEEK_PARAM, false, true,
                updStartDate, updComplDate, true, terminationDate);
    }

    public void createAsyncScenario10() throws Exception {
        String updStartDate = "2013-06-12 00:00:00";
        String updComplDate = "2013-06-26 00:00:00";
        String suspendDate = "2013-06-13 00:00:00";
        String resumeDate = "2013-06-14 00:00:00";
        String terminationDate = "2013-10-23 00:00:00";
        freePeriodBasicScenario3("test_10", "Test10Customer",
                "test10CustomerOrg", 0, // no free period
                TestPriceModel.EXAMPLE_RATA_MONTH_PARAM, //
                true, suspendDate, true, resumeDate, // suspendResume
                true, updStartDate, true, updComplDate, // updParams
                false, terminationDate);
    }

    public void createAsyncScenario11() throws Exception {
        String updStartDate = "2013-06-12 00:00:00";
        String updComplDate = "2013-07-12 00:00:00";
        String suspendDate = "2013-06-13 00:00:00";
        String resumeDate = "2013-06-14 00:00:00";
        String terminationDate = "2013-10-23 00:00:00";
        freePeriodBasicScenario3("test_11", "Test11Customer",
                "test11CustomerOrg", 0, // no free period
                TestPriceModel.EXAMPLE_RATA_MONTH_PARAM, //
                true, suspendDate, true, resumeDate, // suspendResume
                true, updStartDate, true, updComplDate, // updParams
                false, terminationDate);
    }

    public void createAsyncScenario12() throws Exception {
        String updStartDate = "2013-06-12 00:00:00";
        String updComplDate = "2013-06-26 00:00:00";
        String suspendDate = "2013-06-13 00:00:00";
        String resumeDate = "2013-06-14 00:00:00";
        String terminationDate = "2013-06-27 00:00:00";
        freePeriodBasicScenario3("test_12", "Test12Customer",
                "test12CustomerOrg", 0, // no free period
                TestPriceModel.EXAMPLE_RATA_MONTH_PARAM, //
                true, suspendDate, true, resumeDate, // suspendResume
                true, updStartDate, true, updComplDate, // updParams
                true, terminationDate);
    }

    public void createAsyncScenario13() throws Exception {
        String updStartDate = "2013-06-12 00:00:00";
        String updComplDate = "2013-06-16 00:00:00";
        String suspendDate = "2013-06-13 00:00:00";
        String resumeDate = "2013-06-14 00:00:00";
        String terminationDate = "2013-10-23 00:00:00";
        freePeriodBasicScenario3("test_13", "Test13Customer",
                "test13CustomerOrg", 0, // no free period
                TestPriceModel.EXAMPLE_UNIT_MONTH_PARAM, //
                true, suspendDate, true, resumeDate, // suspendResume
                true, updStartDate, true, updComplDate, // updParams
                false, terminationDate);
    }

    public void createAsyncScenario14() throws Exception {
        String updStartDate = "2013-06-12 00:00:00";
        String updComplDate = "2013-06-19 00:00:00";
        String suspendDate = "2013-06-14 00:00:00";
        String resumeDate = "2013-06-22 00:00:00";
        String terminationDate = "2013-10-23 00:00:00";
        freePeriodBasicScenario3("test_14", "Test14Customer",
                "test14CustomerOrg", 0, // no free period
                TestPriceModel.EXAMPLE_RATA_WEEK_PARAM, //
                true, suspendDate, true, resumeDate, // suspendResume
                true, updStartDate, true, updComplDate, // updParams
                false, terminationDate);
    }

    public void createAsyncScenario15() throws Exception {
        String updStartDate = "2013-06-12 00:00:00";
        String updComplDate = "2013-06-19 00:00:00";
        String suspendDate = "2013-06-14 00:00:00";
        String resumeDate = "9999-06-22 00:00:00"; // do not resume
        String terminationDate = "2013-06-23 00:00:00";
        freePeriodBasicScenario3("test_15", "Test15Customer",
                "test15CustomerOrg", 0, // no free period
                TestPriceModel.EXAMPLE_RATA_WEEK_PARAM, //
                true, suspendDate, false, resumeDate, // suspendResume
                true, updStartDate, true, updComplDate, // updParams
                true, terminationDate);
    }

    public void createAsyncScenario16() throws Exception {
        String updStartDate = "2013-06-12 00:00:00";
        String updComplDate = "2013-06-14 00:00:00";
        String suspendDate = "2013-06-13 00:00:00";
        String resumeDate = "2013-06-15 00:00:00";
        freePeriodBasicScenario3("test_16", "Test16Customer",
                "test16CustomerOrg", 0, // no free period
                TestPriceModel.EXAMPLE_UNIT_WEEK_PARAM, //
                true, suspendDate, true, resumeDate, // suspendResume
                true, updStartDate, true, updComplDate, // updParams
                false, null);
    }

    public void createAsyncScenario16_01() throws Exception {
        String updStartDate = "2013-06-12 00:00:00";
        String updComplDate = "2013-06-14 00:00:00";
        String suspendDate = "2013-06-13 00:00:00";
        String resumeDate = "2013-06-13 12:00:00";
        freePeriodBasicScenario3("test_16_01", "Test16_01Customer",
                "test16_01CustomerOrg", 0, // no free period
                TestPriceModel.EXAMPLE_UNIT_WEEK_PARAM, //
                true, suspendDate, true, resumeDate, // suspendResume
                true, updStartDate, true, updComplDate, // updParams
                false, null);
    }

    public void createAsyncScenario17() throws Exception {
        String updStartDate = "2013-06-12 00:00:00";
        String updComplDate = "0000-00-00 00:00:00"; // never completes
        String suspendDate = "2013-06-14 00:00:00";
        // resume is set to false, so date below does not matter
        String resumeDate = "2013-08-22 00:00:00"; // does not resume
        String terminationDate = "2013-06-23 00:00:00";
        freePeriodBasicScenario3("test_17", "Test17Customer",
                "test17CustomerOrg", 0, // no free period
                TestPriceModel.EXAMPLE_RATA_WEEK_PARAM, //
                true, suspendDate, false, resumeDate, // suspend only
                true, updStartDate, false, updComplDate, // updParams
                true, terminationDate);
    }

    public void createAsyncScenario17_01() throws Exception {
        String updStartDate = "2013-06-12 00:00:00";
        String updComplDate = "0000-00-00 00:00:00"; // never completes
        String suspendDate = "2013-06-14 00:00:00";
        String resumeDate = "2013-06-17 00:00:00";
        String terminationDate = "2013-06-24 00:00:00";
        freePeriodBasicScenario3("test_17_01", "Test17_01Customer",
                "test17_01CustomerOrg", 0, // no free period
                TestPriceModel.EXAMPLE_RATA_WEEK_PARAM, //
                true, suspendDate, true, resumeDate, // suspendResume
                true, updStartDate, false, updComplDate, // updParams
                true, terminationDate);
    }

    public void createAsyncScenario18() throws Exception {
        String updStartDate = "2013-06-12 00:00:00";
        String updComplAbortDate = "2013-06-14 00:00:00"; // abort upd
        // suspend is set to false, so dates below do not matter
        String suspendDate = "2013-06-14 00:00:00";
        String resumeDate = "2013-08-22 00:00:00";
        String terminationDate = "2013-06-23 00:00:00";
        freePeriodBasicScenario4("test_18", "Test18Customer",
                "test18CustomerOrg", 0, // no free period
                TestPriceModel.EXAMPLE_RATA_WEEK_PARAM, //
                false, suspendDate, false, resumeDate, // no suspend
                true, updStartDate, true, updComplAbortDate, // abort upd
                false, terminationDate);
    }

    public void createAsyncScenario19() throws Exception {
        String updStartDate = "2013-06-09 00:00:00";
        String updComplAbortDate = "2013-06-14 12:00:00";
        String suspendDate = "2013-06-13 00:00:00";
        String resumeDate = "2013-06-20 00:00:00";
        String terminationDate = "2013-06-23 00:00:00";
        freePeriodBasicScenario4("test_19", "Test19Customer",
                "test19CustomerOrg", 0, // no free period
                TestPriceModel.EXAMPLE_RATA_WEEK_PARAM, //
                true, suspendDate, true, resumeDate, // suspend
                true, updStartDate, true, updComplAbortDate, // abort upd
                false, terminationDate);
    }

    public void createAsyncScenario20() throws Exception {
        String updStartDate = "2013-06-10 12:00:00";
        String updComplAbortDate = "2013-06-15 12:00:00";
        String suspendDate = "2013-06-14 00:00:00";
        String resumeDate = "2013-06-16 00:00:00";
        String terminationDate = "2013-07-23 00:00:00"; // no termination
        freePeriodBasicScenario4("test_20", "Test20Customer",
                "test20CustomerOrg", 0, // no free period
                TestPriceModel.EXAMPLE_UNIT_WEEK_PARAM, //
                true, suspendDate, true, resumeDate, // suspend
                true, updStartDate, true, updComplAbortDate, // abort upd
                false, terminationDate);
    }

    public void createAsyncScenario21() throws Exception {
        String updStartDate = "2013-06-11 12:00:00";
        String updComplDate = "2013-06-14 00:00:00";
        freePeriodBasicScenario2("test_21", "Test21Customer",
                "test21CustomerOrg",
                11, // free period ends "2013-06-12 00:00:00"
                TestPriceModel.EXAMPLE_UNIT_WEEK_PARAM, false, true,
                updStartDate, updComplDate, false, null);
    }

    public void createAsyncScenario22() throws Exception {
        String updStartDate = "2013-06-11 12:00:00";
        String updComplDate = "2013-06-20 00:00:00";
        freePeriodBasicScenario2("test_22", "Test22Customer",
                "test22CustomerOrg",
                18, // free period ends "2013-06-19 00:00:00"
                TestPriceModel.EXAMPLE_UNIT_WEEK_PARAM, false, true,
                updStartDate, updComplDate, false, null);
    }

    public void createAsyncScenario23() throws Exception {
        String updStartDate = "2013-06-12 00:00:00";
        String updComplDate = "2013-06-19 00:00:00";
        String suspendDate = "2013-06-14 00:00:00";
        String resumeDate = "2013-06-20 00:00:00";
        freePeriodBasicScenario3("test_23", "Test23Customer",
                "test23CustomerOrg", 16, // free period ends
                                         // "2013-06-17 00:00:00"
                TestPriceModel.EXAMPLE_RATA_WEEK_PARAM, //
                true, suspendDate, true, resumeDate, // suspendResume
                true, updStartDate, true, updComplDate, // updParams
                false, null);
    }

    public void createAsyncScenario24() throws Exception {
        String updStartDate = "2013-06-12 00:00:00";
        String updComplDate = "2013-06-19 00:00:00";
        String suspendDate = "2013-06-14 00:00:00";
        String resumeDate = "2013-06-20 00:00:00";
        freePeriodBasicScenario3("test_24", "Test24Customer",
                "test24CustomerOrg", 24, // free period ends
                                         // "2013-06-25 00:00:00"
                TestPriceModel.EXAMPLE_UNIT_WEEK_PARAM, //
                true, suspendDate, true, resumeDate, // suspendResume
                true, updStartDate, true, updComplDate, // updParams
                false, null);
    }

    public void createAsyncScenario24_01() throws Exception {
        String updStartDate = "2013-06-12 00:00:00";
        String updComplDate = "2013-06-19 00:00:00";
        String suspendDate = "2013-06-14 00:00:00";
        String resumeDate = "2013-06-20 00:00:00";
        freePeriodBasicScenario3("test_24_01", "Test24_01Customer",
                "test24_01CustomerOrg", 16, // free period ends
                                            // "2013-06-17 00:00:00"
                TestPriceModel.EXAMPLE_UNIT_WEEK_PARAM, //
                true, suspendDate, true, resumeDate, // suspendResume
                true, updStartDate, true, updComplDate, // updParams
                false, null);
    }

    public void createAsyncScenario24_02() throws Exception {
        String updStartDate = "2013-06-12 00:00:00";
        String updComplDate = "2013-06-19 00:00:00";
        String suspendDate = "2013-06-14 00:00:00";
        String resumeDate = "2013-06-20 00:00:00";
        freePeriodBasicScenario3("test_24_02", "Test24_02Customer",
                "test24_02CustomerOrg", 12, // free period ends
                                            // "2013-06-13 00:00:00"
                TestPriceModel.EXAMPLE_UNIT_WEEK_PARAM, //
                true, suspendDate, true, resumeDate, // suspendResume
                true, updStartDate, true, updComplDate, // updParams
                false, null);
    }

    public void createAsyncScenario25() throws Exception {
        String updStartDate = "2013-06-12 00:00:00";
        String updComplDate = "2013-06-19 00:00:00";
        String suspendDate = "2013-06-14 00:00:00";
        String resumeDate = "2013-06-22 00:00:00";
        freePeriodBasicScenario3("test_25", "Test25Customer",
                "test25CustomerOrg", 19, // free period ends
                                         // "2013-06-20 00:00:00"
                TestPriceModel.EXAMPLE_RATA_WEEK_PARAM, //
                true, suspendDate, true, resumeDate, // suspendResume
                true, updStartDate, true, updComplDate, // updParams
                false, null);
    }

    public void createAsyncScenario26() throws Exception {
        String updStartDate = "2013-06-12 00:00:00";
        String updComplDate = "2013-06-14 00:00:00";
        String suspendDate = "2013-06-13 00:00:00";
        String resumeDate = "2013-06-15 00:00:00";
        freePeriodBasicScenario3("test_26", "Test26Customer",
                "test26CustomerOrg", 17, // free period ends
                                         // "2013-06-18 00:00:00"
                TestPriceModel.EXAMPLE_UNIT_WEEK_PARAM, //
                true, suspendDate, true, resumeDate, // suspendResume
                true, updStartDate, true, updComplDate, // updParams
                false, null);
    }

    public void createAsyncScenario27() throws Exception {
        String updStartDate = "2013-06-12 00:00:00";
        String updComplDate = "2013-06-14 00:00:00";
        String suspendDate = "2013-06-13 00:00:00";
        String resumeDate = "2013-06-15 00:00:00";
        freePeriodBasicScenario3("test_27", "Test27Customer",
                "test27CustomerOrg", 15, // free period ends
                                         // "2013-06-16 00:00:00"
                TestPriceModel.EXAMPLE_UNIT_WEEK_PARAM, //
                true, suspendDate, true, resumeDate, // suspendResume
                true, updStartDate, true, updComplDate, // updParams
                false, null);
    }

    public void createAsyncScenario27_01() throws Exception {
        String updStartDate = "2013-06-12 00:00:00";
        String updComplDate = "2013-06-14 12:00:00";
        String suspendDate = "2013-06-13 00:00:00";
        String resumeDate = "2013-06-15 00:00:00";
        freePeriodBasicScenario3("test_27_01", "Test27_01Customer",
                "test27_01CustomerOrg", 13, // free period ends
                                            // "2013-06-14 00:00:00"
                TestPriceModel.EXAMPLE_UNIT_WEEK_PARAM, //
                true, suspendDate, true, resumeDate, // suspendResume
                true, updStartDate, true, updComplDate, // updParams
                false, null);
    }

    private void freePeriodBasicScenario(String scenarioId,
            String customerAdminId, String customerOrgName, int freePeriod,
            TestPriceModel testPriceModel, boolean recordEvents,
            boolean terminateSubscription, String terminationDate)
            throws Exception {
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-05-20 00:00:00"));

        setCutOffDay(basicSetup.getSupplierAdminKey(), 7);

        // Create a customer
        VOOrganization customer = orgSetup.registerCustomer(customerOrgName,
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                customerAdminId, supplierMarketplace.getMarketplaceId(),
                basicSetup.getSupplierOrgID());
        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(), scenarioId,
                        TestService.EXAMPLE, testPriceModel, freePeriod,
                        technicalServiceAsync, supplierMarketplace);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                customerAdmin.getKey(), scenarioId, serviceDetails,
                customerAdmin, role);
        // ASYNC
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-06-01 00:00:00"));
        subDetails = subscrSetup.completeAsyncSubscription(
                basicSetup.getSupplierAdminKey(), customerAdmin, subDetails);

        if (recordEvents) {
            subscrSetup.recordEventForSubscription(
                    basicSetup.getSupplierAdminKey(), subDetails,
                    DateTimeHandling.calculateMillis("2013-06-07 12:00:00"),
                    "FILE_DOWNLOAD", 15);
        }

        // Suspend and resume the subscription by deleting the customer's
        // payment types
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-06-09 00:00:00"));
        paymentSetup.deleteCustomerPaymentTypes(
                basicSetup.getSupplierAdminKey(), customer);
        subDetails = subscrSetup.getSubscriptionDetails(customerAdmin.getKey(),
                subDetails.getSubscriptionId());

        if (recordEvents) {
            subscrSetup.recordEventForSubscription(
                    basicSetup.getSupplierAdminKey(), subDetails,
                    DateTimeHandling.calculateMillis("2013-06-10 01:45:00"),
                    "FILE_UPLOAD", 25);
        }

        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-06-11 00:00:00"));
        paymentSetup.reassignCustomerPaymentTypes(
                basicSetup.getSupplierAdminKey(), customer);
        subDetails = subscrSetup.getSubscriptionDetails(customerAdmin.getKey(),
                subDetails.getSubscriptionId());

        if (recordEvents) {
            subscrSetup.recordEventForSubscription(
                    basicSetup.getSupplierAdminKey(), subDetails,
                    DateTimeHandling.calculateMillis("2013-06-13 13:13:13"),
                    "FOLDER_NEW", 4);
        }

        if (terminateSubscription) {
            BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                    .calculateMillis(terminationDate));
            subscrSetup.unsubscribeToService(customerAdmin.getKey(),
                    subDetails.getSubscriptionId());
        }

        // reset old cut off day
        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(scenarioId,
                subDetails);
        BillingIntegrationTestBase.updateCustomerListForTests(scenarioId,
                customer);
    }

    private void freePeriodBasicScenario2(String scenarioId,
            String customerAdminId, String customerOrgName, int freePeriod,
            TestPriceModel testPriceModel, boolean suspendResume,
            boolean updateParameter, String updStart, String updCompl,
            boolean terminateSubscription, String terminationDate)
            throws Exception {
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-05-20 00:00:00"));
        // .calculateMillis("2013-06-01 00:00:00"));

        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);

        // Create a customer
        VOOrganization customer = orgSetup.registerCustomer(customerOrgName,
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                customerAdminId, supplierMarketplace.getMarketplaceId(),
                basicSetup.getSupplierOrgID());
        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(), scenarioId,
                        TestService.EXAMPLE, testPriceModel, freePeriod,
                        technicalServiceAsync, supplierMarketplace);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                customerAdmin.getKey(), scenarioId, serviceDetails,
                customerAdmin, role);
        // ASYNC
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-06-01 00:00:00"));
        subDetails = subscrSetup.completeAsyncSubscription(
                basicSetup.getSupplierAdminKey(), customerAdmin, subDetails);

        if (updateParameter) {
            // parameter change
            container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
            subDetails = subscrSetup.modifyParameterForSubscription(subDetails,
                    DateTimeHandling.calculateMillis(updStart),
                    "MAX_FOLDER_NUMBER", "5");
            BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                    .calculateMillis(updCompl));
            subDetails = subscrSetup
                    .completeAsyncModifySubscription(
                            basicSetup.getSupplierAdminKey(), customerAdmin,
                            subDetails);
        }

        if (suspendResume) {
            // Suspend and resume the subscription by deleting the customer's
            // payment types
            BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                    .calculateMillis("2013-06-09 00:00:00"));
            paymentSetup.deleteCustomerPaymentTypes(
                    basicSetup.getSupplierAdminKey(), customer);
            subDetails = subscrSetup.getSubscriptionDetails(
                    customerAdmin.getKey(), subDetails.getSubscriptionId());
            BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                    .calculateMillis("2013-06-11 00:00:00"));
            paymentSetup.reassignCustomerPaymentTypes(
                    basicSetup.getSupplierAdminKey(), customer);
            subDetails = subscrSetup.getSubscriptionDetails(
                    customerAdmin.getKey(), subDetails.getSubscriptionId());
        }

        if (terminateSubscription) {
            BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                    .calculateMillis(terminationDate));
            subscrSetup.unsubscribeToService(customerAdmin.getKey(),
                    subDetails.getSubscriptionId());
        }

        // reset old cut off day
        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(scenarioId,
                subDetails);
        BillingIntegrationTestBase.updateCustomerListForTests(scenarioId,
                customer);
    }

    private void freePeriodBasicScenario3(
            String scenarioId,
            String customerAdminId,
            String customerOrgName,
            int freePeriod,
            TestPriceModel testPriceModel, //
            boolean suspendResume, String suspendTime, boolean resume,
            String resumeTime, boolean updateParameter, String updStartDate,
            boolean complUpdate, String updComplDate,
            boolean terminateSubscription, String terminationDate)
            throws Exception {
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-05-20 00:00:00"));

        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);

        // Create a customer
        VOOrganization customer = orgSetup.registerCustomer(customerOrgName,
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                customerAdminId, supplierMarketplace.getMarketplaceId(),
                basicSetup.getSupplierOrgID());
        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(), scenarioId,
                        TestService.EXAMPLE, testPriceModel, freePeriod,
                        technicalServiceAsync, supplierMarketplace);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                customerAdmin.getKey(), scenarioId, serviceDetails,
                customerAdmin, role);
        // ASYNC
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-06-01 00:00:00"));
        subDetails = subscrSetup.completeAsyncSubscription(
                basicSetup.getSupplierAdminKey(), customerAdmin, subDetails);

        if (updateParameter) {
            // parameter change
            container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
            subDetails = subscrSetup.modifyParameterForSubscription(subDetails,
                    DateTimeHandling.calculateMillis(updStartDate),
                    "MAX_FOLDER_NUMBER", "5");
        }

        if (suspendResume) {
            // Suspend the subscription by deleting the customer's
            // payment types
            BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                    .calculateMillis(suspendTime));
            paymentSetup.deleteCustomerPaymentTypes(
                    basicSetup.getSupplierAdminKey(), customer);
            subDetails = subscrSetup.getSubscriptionDetails(
                    customerAdmin.getKey(), subDetails.getSubscriptionId());

        }

        // check if resume is necessary or if update completion is done first
        // or do not resume at all if resume == false
        if ((suspendResume && resume)
                && (complUpdate && (DateTimeHandling
                        .calculateMillis(resumeTime) < DateTimeHandling
                        .calculateMillis(updComplDate)))) {
            BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                    .calculateMillis(resumeTime));
            paymentSetup.reassignCustomerPaymentTypes(
                    basicSetup.getSupplierAdminKey(), customer);
            subDetails = subscrSetup.getSubscriptionDetails(
                    customerAdmin.getKey(), subDetails.getSubscriptionId());
        }

        if (updateParameter && (complUpdate)) {
            // parameter update completion
            container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
            BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                    .calculateMillis(updComplDate));
            subDetails = subscrSetup
                    .completeAsyncModifySubscription(
                            basicSetup.getSupplierAdminKey(), customerAdmin,
                            subDetails);
        }

        // do not resume at all if resume == false
        // otherwise check if resumeTime is after updComplTime
        if ((suspendResume && resume)
                && (!complUpdate || (complUpdate && (DateTimeHandling
                        .calculateMillis(resumeTime) > DateTimeHandling
                        .calculateMillis(updComplDate))))) {

            BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                    .calculateMillis(resumeTime));
            paymentSetup.reassignCustomerPaymentTypes(
                    basicSetup.getSupplierAdminKey(), customer);
            subDetails = subscrSetup.getSubscriptionDetails(
                    customerAdmin.getKey(), subDetails.getSubscriptionId());

        }

        if (terminateSubscription) {
            BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                    .calculateMillis(terminationDate));
            subscrSetup.unsubscribeToService(customerAdmin.getKey(),
                    subDetails.getSubscriptionId());
        }

        // reset old cut off day
        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(scenarioId,
                subDetails);
        BillingIntegrationTestBase.updateCustomerListForTests(scenarioId,
                customer);
    }

    private void freePeriodBasicScenario4(String scenarioId,
            String customerAdminId, String customerOrgName,
            int freePeriod,
            TestPriceModel testPriceModel, //
            boolean suspendResume,
            String suspendTime, //
            boolean resume,
            String resumeTime, //
            boolean updateParameter,
            String updStartDate, //
            boolean abortUpd, String updComplAbortDate,
            boolean terminateSubscription, String terminationDate)
            throws Exception {
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-05-20 00:00:00"));

        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);

        // Create a customer
        VOOrganization customer = orgSetup.registerCustomer(customerOrgName,
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                customerAdminId, supplierMarketplace.getMarketplaceId(),
                basicSetup.getSupplierOrgID());
        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(), scenarioId,
                        TestService.EXAMPLE, testPriceModel, freePeriod,
                        technicalServiceAsync, supplierMarketplace);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                customerAdmin.getKey(), scenarioId, serviceDetails,
                customerAdmin, role);
        // ASYNC
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-06-01 00:00:00"));
        subDetails = subscrSetup.completeAsyncSubscription(
                basicSetup.getSupplierAdminKey(), customerAdmin, subDetails);

        if (updateParameter) {
            // parameter change
            container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
            subDetails = subscrSetup.modifyParameterForSubscription(subDetails,
                    DateTimeHandling.calculateMillis(updStartDate),
                    "MAX_FOLDER_NUMBER", "5");
        }

        if (suspendResume) {
            // Suspend the subscription by deleting the customer's
            // payment types
            BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                    .calculateMillis(suspendTime));
            paymentSetup.deleteCustomerPaymentTypes(
                    basicSetup.getSupplierAdminKey(), customer);
            subDetails = subscrSetup.getSubscriptionDetails(
                    customerAdmin.getKey(), subDetails.getSubscriptionId());

        }

        // check if resume is necessary or if update completion is done first
        // or do not resume at all if resume == false
        if ((suspendResume && resume)
                && (DateTimeHandling.calculateMillis(resumeTime) < DateTimeHandling
                        .calculateMillis(updComplAbortDate))) {
            BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                    .calculateMillis(resumeTime));
            paymentSetup.reassignCustomerPaymentTypes(
                    basicSetup.getSupplierAdminKey(), customer);
            subDetails = subscrSetup.getSubscriptionDetails(
                    customerAdmin.getKey(), subDetails.getSubscriptionId());
        }

        if (updateParameter && !abortUpd) {
            // parameter update completion
            container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
            BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                    .calculateMillis(updComplAbortDate));
            subDetails = subscrSetup
                    .completeAsyncModifySubscription(
                            basicSetup.getSupplierAdminKey(), customerAdmin,
                            subDetails);
        } else if (updateParameter && abortUpd) {
            container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
            BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                    .calculateMillis(updComplAbortDate));
            subscrSetup.abortAsyncModifySubscription(
                    basicSetup.getSupplierAdminKey(),
                    subDetails.getSubscriptionId(),
                    customer.getOrganizationId());
        }

        // do not resume at all if resume == false
        if ((suspendResume && resume)
                && (DateTimeHandling.calculateMillis(resumeTime) >= DateTimeHandling
                        .calculateMillis(updComplAbortDate))) {

            BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                    .calculateMillis(resumeTime));
            paymentSetup.reassignCustomerPaymentTypes(
                    basicSetup.getSupplierAdminKey(), customer);
            subDetails = subscrSetup.getSubscriptionDetails(
                    customerAdmin.getKey(), subDetails.getSubscriptionId());
        }

        if (terminateSubscription) {
            BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                    .calculateMillis(terminationDate));
            subscrSetup.unsubscribeToService(customerAdmin.getKey(),
                    subDetails.getSubscriptionId());
        }

        // reset old cut off day
        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(scenarioId,
                subDetails);
        BillingIntegrationTestBase.updateCustomerListForTests(scenarioId,
                customer);
    }
}
