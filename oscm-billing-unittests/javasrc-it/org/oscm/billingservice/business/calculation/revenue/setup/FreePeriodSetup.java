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
import org.oscm.billingservice.setup.VOServiceFactory;
import org.oscm.billingservice.setup.VOPriceModelFactory.TestPriceModel;
import org.oscm.billingservice.setup.VOServiceFactory.TestService;
import org.oscm.test.DateTimeHandling;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.internal.vo.VORoleDefinition;
import org.oscm.internal.vo.VOServiceDetails;
import org.oscm.internal.vo.VOSubscriptionDetails;
import org.oscm.internal.vo.VOUser;

/**
 * @author baumann
 */
public class FreePeriodSetup extends IntegrationTestSetup {

    public void createFP01_FreePeriodEndsBeforeBillingPeriod() throws Exception {
        freePeriodBasicScenario("FREEP_01", "Freep01Customer",
                "Freep01CustomerOrg", 3, false, false, null);
    }

    public void createFP02_FreePeriodEndsBeforeSuspend() throws Exception {
        freePeriodBasicScenario("FREEP_02", "Freep02Customer",
                "Freep02CustomerOrg", 7, true, false, null);
    }

    public void createFP03_FreePeriodEndsBetweenSusRes() throws Exception {
        freePeriodBasicScenario("FREEP_03", "Freep03Customer",
                "Freep03CustomerOrg", 9, false, false, null);
    }

    public void createFP04_FreePeriodEndsAfterRes() throws Exception {
        freePeriodBasicScenario("FREEP_04", "Freep04Customer",
                "Freep04CustomerOrg", 15, false, false, null);
    }

    public void createFP05_FreePeriodEndsAfterBillingPeriod() throws Exception {
        freePeriodBasicScenario("FREEP_05", "Freep05Customer",
                "Freep05CustomerOrg", 40, true, false, null);
    }

    public void createFP06_SubscriptionTerminatesInFreePeriod()
            throws Exception {
        freePeriodBasicScenario("FREEP_06", "Freep06Customer",
                "Freep06CustomerOrg", 30, false, true, "2013-06-25 12:51:00");
    }

    public void createFP07_UpgradeServiceWithFreePeriod() throws Exception {
        freePeriodUpgradeScenario("FREEP_07", "Freep07Customer",
                "Freep07CustomerOrg", 17, 3, true);
    }

    public void createFP08_UpgradeInFreePeriod() throws Exception {
        freePeriodUpgradeScenario("FREEP_08", "Freep08Customer",
                "Freep08CustomerOrg", 27, 3, false);
    }

    public void createFP09_FreePeriodEndsAtBeginOfBillingPeriod()
            throws Exception {
        freePeriodBasicScenario("FREEP_09", "Freep09Customer",
                "Freep09CustomerOrg", 36, false, false, null);
    }

    public void createFP10_FreePeriodEndsAtTerminationTime() throws Exception {
        freePeriodBasicScenario("FREEP_10", "Freep10Customer",
                "Freep10CustomerOrg", 36, false, true, "2013-07-07 00:00:00");
    }

    public void createFP11_FreePeriodEndsAtUpgradeTime() throws Exception {
        freePeriodUpgradeScenario("FREEP_11", "Freep11Customer",
                "Freep11CustomerOrg", 20, 3, true);
    }

    private void freePeriodBasicScenario(String scenarioId,
            String customerAdminId, String customerOrgName, int freePeriod,
            boolean recordEvents, boolean terminateSubscription,
            String terminationDate) throws Exception {
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-06-01 00:00:00"));

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
                        TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_FP_PUDAY_ROLES_PAR_EVENTS,
                        freePeriod, technicalService, supplierMarketplace);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                customerAdmin.getKey(), scenarioId, serviceDetails,
                customerAdmin, role);

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

    private void freePeriodUpgradeScenario(String scenarioId,
            String customerAdminId, String customerOrgName, int freePeriod,
            int freePeriodOfUpgrServ, boolean recordEvents) throws Exception {
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-06-01 00:00:00"));

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
                        TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_FP_PUDAY_ROLES_PAR_EVENTS,
                        freePeriod, technicalService, supplierMarketplace);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                customerAdmin.getKey(), scenarioId, serviceDetails,
                customerAdmin, role);

        // Suspend and resume the subscription by deleting the customer's
        // payment types
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-06-09 00:00:00"));
        paymentSetup.deleteCustomerPaymentTypes(
                basicSetup.getSupplierAdminKey(), customer);
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-06-11 00:00:00"));
        paymentSetup.reassignCustomerPaymentTypes(
                basicSetup.getSupplierAdminKey(), customer);
        subDetails = subscrSetup.getSubscriptionDetails(customerAdmin.getKey(),
                subDetails.getSubscriptionId());

        if (recordEvents) {
            subscrSetup.recordEventForSubscription(
                    basicSetup.getSupplierAdminKey(), subDetails,
                    DateTimeHandling.calculateMillis("2013-06-13 01:45:00"),
                    "FILE_DOWNLOAD", 25);
        }

        // Upgrade the subscription
        VOServiceDetails perUnitService = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(), scenarioId
                                + "_UPGRADED_SERVICE", TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_FP_PUDAY_ROLES_PAR_EVENTS2,
                        freePeriodOfUpgrServ, technicalService,
                        supplierMarketplace);

        serviceSetup.registerCompatibleServices(
                basicSetup.getSupplierAdminKey(), serviceDetails,
                perUnitService);

        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-06-21 00:00:00"));
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(customerAdmin.getKey(), subDetails,
                        perUnitService);

        if (recordEvents) {
            subscrSetup.recordEventForSubscription(
                    basicSetup.getSupplierAdminKey(), upgradedSubDetails,
                    DateTimeHandling.calculateMillis("2013-06-22 11:25:00"),
                    "FILE_UPLOAD", 15);
            subscrSetup.recordEventForSubscription(
                    basicSetup.getSupplierAdminKey(), upgradedSubDetails,
                    DateTimeHandling.calculateMillis("2013-06-26 13:13:13"),
                    "FOLDER_NEW", 8);
        }

        // reset old cut off day
        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(scenarioId,
                subDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(scenarioId,
                upgradedSubDetails);
        BillingIntegrationTestBase.updateCustomerListForTests(scenarioId,
                customer);
    }

}
