/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: Dec 20, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.calculation.revenue.setup;

import java.math.BigDecimal;
import java.util.Arrays;

import org.oscm.billingservice.setup.BillingIntegrationTestBase;
import org.oscm.billingservice.setup.IntegrationTestSetup;
import org.oscm.billingservice.setup.TestOrganizationSetup;
import org.oscm.billingservice.setup.VOPriceModelFactory;
import org.oscm.billingservice.setup.VOServiceFactory;
import org.oscm.billingservice.setup.VOTechServiceFactory;
import org.oscm.billingservice.setup.VOVatRateFactory;
import org.oscm.billingservice.setup.VOPriceModelFactory.TestPriceModel;
import org.oscm.billingservice.setup.VOServiceFactory.TestService;
import org.oscm.test.BaseAdmUmTest;
import org.oscm.test.DateTimeHandling;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.vo.VOOrganization;
import org.oscm.internal.vo.VOPriceModel;
import org.oscm.internal.vo.VORoleDefinition;
import org.oscm.internal.vo.VOServiceDetails;
import org.oscm.internal.vo.VOSubscriptionDetails;
import org.oscm.internal.vo.VOTechnicalService;
import org.oscm.internal.vo.VOUser;

/**
 * @author baumann
 */
public class PictSetup2 extends IntegrationTestSetup {

    public void createPictScenario52() throws Exception {
        long usageStartTime = DateTimeHandling
                .calculateMillis("2013-06-03 20:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(usageStartTime);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);

        // Create own customer
        String customerAdminId = "PIC52Customer";
        VOOrganization customer = orgSetup.registerCustomer("PIC52CustomerOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                customerAdminId, supplierMarketplace.getMarketplaceId(),
                basicSetup.getSupplierOrgID());

        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);

        orgSetup.updateCustomerDiscount(customer, new BigDecimal("25.00"),
                DateTimeHandling.calculateMillis("2013-06-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-07-01 01:00:00"));

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(), "PICT_TEST_52",
                        TestService.EXAMPLE, TestPriceModel.FREE,
                        technicalService, supplierMarketplace);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");

        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "PICT_TEST_52", serviceDetails, customerAdmin, role);

        // suspend/resume subscription several times by removing/restoring the
        // customer's payment types
        long suspResTime = DateTimeHandling
                .calculateMillis("2013-06-04 07:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(suspResTime);

        container.login(basicSetup.getSupplierAdminKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);

        paymentSetup.deleteCustomerPaymentTypes(customer);

        long reassignResTime = DateTimeHandling
                .calculateMillis("2013-06-05 07:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(reassignResTime);
        paymentSetup.reassignCustomerPaymentTypes(customer);

        // 1. create upgraded service
        VOServiceDetails paidService = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(), "PAIRWISE52_Upgrade",
                        TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_RATA_WEEK_ROLES,
                        technicalService, supplierMarketplace);

        serviceSetup.registerCompatibleServices(
                basicSetup.getSupplierAdminKey(), serviceDetails, paidService);

        long usageUpgradeTime = DateTimeHandling
                .calculateMillis("2013-06-09 20:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(usageUpgradeTime);
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(subDetails, paidService);

        // 2.create upgraded service
        VOServiceDetails paidService2 = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "PAIRWISE52_Upgrade2", TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_RATA_WEEK_ROLES_PAR_B,
                        technicalService, supplierMarketplace);

        serviceSetup.registerCompatibleServices(
                basicSetup.getSupplierAdminKey(), paidService, paidService2);

        usageUpgradeTime = DateTimeHandling
                .calculateMillis("2013-06-10 20:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(usageUpgradeTime);
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetails2 = subscrSetup
                .upgradeSubscription(upgradedSubDetails, paidService2);

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_52", subDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_52", upgradedSubDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_52", upgradedSubDetails2);
        BillingIntegrationTestBase.updateCustomerListForTests("PICT_TEST_52",
                customer);

    }

    public void createPictScenario52_2() throws Exception {
        long usageStartTime = DateTimeHandling
                .calculateMillis("2013-06-03 20:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(usageStartTime);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);

        // Create own customer
        String customerAdminId = "PIC52Customer_2";
        VOOrganization customer = orgSetup.registerCustomer("PIC52CustomerOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                customerAdminId, supplierMarketplace.getMarketplaceId(),
                basicSetup.getSupplierOrgID());

        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);

        orgSetup.updateCustomerDiscount(customer, new BigDecimal("25.00"),
                DateTimeHandling.calculateMillis("2013-06-02 00:00:00"),
                DateTimeHandling.calculateMillis("2013-06-03 01:00:00"));

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(), "PICT_TEST_52_2",
                        TestService.EXAMPLE, TestPriceModel.FREE,
                        technicalService, supplierMarketplace);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");

        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "PICT_TEST_52_2", serviceDetails, customerAdmin, role);

        // suspend/resume subscription several times by removing/restoring the
        // customer's payment types
        long suspResTime = DateTimeHandling
                .calculateMillis("2013-06-04 07:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(suspResTime);

        container.login(basicSetup.getSupplierAdminKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);

        paymentSetup.deleteCustomerPaymentTypes(customer);

        long reassignResTime = DateTimeHandling
                .calculateMillis("2013-06-05 07:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(reassignResTime);
        paymentSetup.reassignCustomerPaymentTypes(customer);
        //

        // 1. create upgraded service
        VOServiceDetails paidService = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "PAIRWISE52_2_Upgrade", TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_RATA_WEEK_ROLES,
                        technicalService, supplierMarketplace);

        serviceSetup.registerCompatibleServices(
                basicSetup.getSupplierAdminKey(), serviceDetails, paidService);

        long usageUpgradeTime = DateTimeHandling
                .calculateMillis("2013-06-09 20:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(usageUpgradeTime);
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(subDetails, paidService);

        // 2.create upgraded service
        VOServiceDetails paidService2 = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "PAIRWISE52_2_Upgrade2", TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_RATA_WEEK_ROLES_PAR_B,
                        technicalService, supplierMarketplace);

        serviceSetup.registerCompatibleServices(
                basicSetup.getSupplierAdminKey(), paidService, paidService2);

        usageUpgradeTime = DateTimeHandling
                .calculateMillis("2013-06-10 20:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(usageUpgradeTime);
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetails2 = subscrSetup
                .upgradeSubscription(upgradedSubDetails, paidService2);

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_52_2", subDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_52_2", upgradedSubDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_52_2", upgradedSubDetails2);
        BillingIntegrationTestBase.updateCustomerListForTests("PICT_TEST_52_2",
                customer);

    }

    public void createPictScenario51() throws Exception {
        long usageStartTime = DateTimeHandling
                .calculateMillis("2013-06-05 20:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(usageStartTime);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 2);

        // Create own customer
        String customerAdminId = "PICT51Customer";
        VOOrganization customer = orgSetup.registerCustomer(
                "PICT51CustomerOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                customerAdminId, supplierMarketplace.getMarketplaceId(),
                basicSetup.getSupplierOrgID());

        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);

        orgSetup.updateCustomerDiscount(customer, new BigDecimal("25.00"),
                DateTimeHandling.calculateMillis("2013-06-02 00:00:00"),
                DateTimeHandling.calculateMillis("2013-07-03 01:00:00"));

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "PICT_TEST_51",
                        TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_MONTH_USER_STEPPS_ROLES_PARS_FREEP,
                        technicalService, supplierMarketplace);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");

        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "PICT_TEST_51", serviceDetails, customerAdmin, role);

        // 1. create upgraded service
        VOServiceDetails freeService = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(), "P51_Upgrade",
                        TestService.EXAMPLE, TestPriceModel.FREE,
                        technicalService, supplierMarketplace);

        serviceSetup.registerCompatibleServices(
                basicSetup.getSupplierAdminKey(), serviceDetails, freeService);

        long usageUpgradeTime = DateTimeHandling
                .calculateMillis("2013-06-09 20:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(usageUpgradeTime);
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(subDetails, freeService);

        // role change

        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-06-10 10:00:00"));
        upgradedSubDetails = subscrSetup.modifyUserRole(upgradedSubDetails
                .getUsageLicenses().get(0), VOServiceFactory.getRole(
                freeService, "USER"), upgradedSubDetails.getSubscriptionId());

        // role change

        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-06-10 10:00:01"));
        upgradedSubDetails = subscrSetup.modifyUserRole(upgradedSubDetails
                .getUsageLicenses().get(0), VOServiceFactory.getRole(
                freeService, "ADMIN"), upgradedSubDetails.getSubscriptionId());

        // role change

        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-06-10 10:00:02"));
        upgradedSubDetails = subscrSetup.modifyUserRole(upgradedSubDetails
                .getUsageLicenses().get(0), VOServiceFactory.getRole(
                freeService, "USER"), upgradedSubDetails.getSubscriptionId());

        // 2.create upgraded service
        VOServiceDetails paidService2 = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "P51_Upgrade2",
                        TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_MONTH_USER_STEPPS_ROLES_PARS_FREEP,
                        technicalService, supplierMarketplace);

        serviceSetup.registerCompatibleServices(
                basicSetup.getSupplierAdminKey(), freeService, paidService2);

        usageUpgradeTime = DateTimeHandling
                .calculateMillis("2013-06-10 20:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(usageUpgradeTime);
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetails2 = subscrSetup
                .upgradeSubscription(upgradedSubDetails, paidService2);

        // // suspend/resume subscription several times by removing/restoring
        // // customer's payment types
        long suspResTime = DateTimeHandling
                .calculateMillis("2013-06-11 07:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(suspResTime);

        container.login(basicSetup.getSupplierAdminKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);

        paymentSetup.deleteCustomerPaymentTypes(customer);

        long reassignResTime = DateTimeHandling
                .calculateMillis("2013-06-12 07:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(reassignResTime);
        paymentSetup.reassignCustomerPaymentTypes(customer);

        upgradedSubDetails2 = subscrSetup
                .getSubscriptionDetails(customerAdmin.getKey(),
                        upgradedSubDetails2.getSubscriptionId());

        // price model change

        long priceModelChangeTime = DateTimeHandling
                .calculateMillis("2013-06-12 08:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(priceModelChangeTime);
        VOPriceModel newSubPriceModel = VOPriceModelFactory
                .modifyPriceModelPeriodFee(upgradedSubDetails2.getPriceModel(),
                        new BigDecimal("445.00"));

        subscrSetup.savePriceModelForSubscription(
                basicSetup.getSupplierAdminKey(), upgradedSubDetails2,
                newSubPriceModel, customer);

        // delete market service
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-06-13 13:00:00"));
        container.login(basicSetup.getSupplierAdminKey(), ROLE_SERVICE_MANAGER);
        serviceSetup.deleteMarketableService(serviceDetails);

        // change sunbscriptionID
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-06-14 00:00:00"));
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        upgradedSubDetails2.setSubscriptionId("PICT_TEST_51" + "_SubID2");
        upgradedSubDetails2 = subscrSetup.modifySubscription(
                upgradedSubDetails2, null);

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_51", subDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_51", upgradedSubDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_51", upgradedSubDetails2);
        BillingIntegrationTestBase.updateCustomerListForTests("PICT_TEST_51",
                customer);

    }

    public void createPictScenario50() throws Exception {
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-06-05 20:00:00"));

        // Create a supplier with a marketplace
        String supplierAdminId = "Pict50Supplier";
        VOOrganization supplier = orgSetup.createOrganization(
                basicSetup.getPlatformOperatorUserKey(), supplierAdminId,
                "Pict50SupplierOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                OrganizationRoleType.TECHNOLOGY_PROVIDER,
                OrganizationRoleType.SUPPLIER);
        VOUser supplierAdmin = orgSetup.getUser(supplierAdminId, true);
        paymentSetup.createPaymentForSupplier(
                basicSetup.getPlatformOperatorUserKey(),
                supplierAdmin.getKey(), supplier);

        setCutOffDay(supplierAdmin.getKey(), 2);

        // Create a customer
        String customerAdminId = "Pict50Customer";
        VOOrganization customer = orgSetup.registerCustomer(
                "Pict50CustomerOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                customerAdminId, supplierMarketplace.getMarketplaceId(),
                supplier.getOrganizationId());
        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);

        orgSetup.saveAllVats(supplierAdmin.getKey(), VOVatRateFactory
                .newVOVatRate(new BigDecimal("13.00")), Arrays.asList(
                VOVatRateFactory.newVOCountryVatRate(new BigDecimal("20.00"),
                        TestOrganizationSetup.ORGANIZATION_DOMICILE_UK),
                VOVatRateFactory.newVOCountryVatRate(new BigDecimal("19.00"),
                        TestOrganizationSetup.ORGANIZATION_DOMICILE_DE)), null);

        orgSetup.createMarketingPermission(basicSetup.getSupplierAdminKey(),
                supplier.getOrganizationId(), technicalService);
        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        supplierAdmin.getKey(), "PICT_TEST_50",
                        TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PICT50_RATA_HOUR,
                        technicalService, supplierMarketplace);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "GUEST");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "PICT_TEST_50", serviceDetails, customerAdmin, role);
        // Events
        container.login(basicSetup.getSupplierAdminKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        subscrSetup.recordEventForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-06-09 12:00:00"),
                "FILE_DOWNLOAD", 15);

        // parameter change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        subDetails = subscrSetup.modifyParameterForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-06-09 20:00:00"),
                "MAX_FOLDER_NUMBER", "4");

        // Upgrade the subscription to a free service
        VOServiceDetails freeService = serviceSetup
                .createPublishAndActivateMarketableService(
                        supplierAdmin.getKey(), "PIC50_FREE_SERVICE",
                        TestService.EXAMPLE, TestPriceModel.FREE,
                        technicalService, supplierMarketplace);
        serviceSetup.registerCompatibleServices(supplierAdmin.getKey(),
                serviceDetails, freeService);

        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-06-11 20:00:00"));
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(subDetails, freeService);

        // Events
        container.login(basicSetup.getSupplierAdminKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        subscrSetup.recordEventForSubscription(upgradedSubDetails,
                DateTimeHandling.calculateMillis("2013-06-12 12:00:00"),
                "FILE_UPLOAD", 15);

        // Upgrade the subscription
        VOServiceDetails proRataService = serviceSetup
                .createPublishAndActivateMarketableService(
                        supplierAdmin.getKey(), "PICT50_PRORATA_SERVICE",
                        TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PICT50_RATA_HOUR,
                        technicalService, supplierMarketplace);
        serviceSetup.registerCompatibleServices(supplierAdmin.getKey(),
                freeService, proRataService);

        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-06-17 20:00:00"));
        VOSubscriptionDetails upgradedSubDetails2 = subscrSetup
                .upgradeSubscription(upgradedSubDetails, proRataService);

        // user is assigned

        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        setDateFactory("2013-06-18 10:00:00");

        VORoleDefinition changedRole = VOServiceFactory.getRole(proRataService,
                "ADMIN");
        upgradedSubDetails2 = subscrSetup.addUser(
                basicSetup.getSecondCustomerUser1(), changedRole,
                upgradedSubDetails2.getSubscriptionId());

        // user is deassigned
        setDateFactory("2013-06-18 10:30:00");

        subscrSetup.revokeUser(basicSetup.getSecondCustomerUser1(),
                upgradedSubDetails2.getSubscriptionId());
        // user is assigned
        setDateFactory("2013-06-18 10:35:00");

        upgradedSubDetails2 = subscrSetup.addUser(
                basicSetup.getSecondCustomerUser1(), changedRole,
                upgradedSubDetails2.getSubscriptionId());
        // user is deassigned
        setDateFactory("2013-06-18 10:40:00");

        subscrSetup.revokeUser(basicSetup.getSecondCustomerUser1(),
                upgradedSubDetails2.getSubscriptionId());

        // price model change

        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);

        long priceModelChangeTime = DateTimeHandling
                .calculateMillis("2013-06-19 08:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(priceModelChangeTime);
        VOPriceModel newSubPriceModel = VOPriceModelFactory
                .modifyPriceModelPeriodFee(upgradedSubDetails2.getPriceModel(),
                        new BigDecimal("20.00"));

        subscrSetup.savePriceModelForSubscription(supplierAdmin.getKey(),
                upgradedSubDetails2, newSubPriceModel, customer);

        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        // reset old cut off day
        resetCutOffDay(supplierAdmin.getKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_50", subDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_50", upgradedSubDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_50", upgradedSubDetails2);
        BillingIntegrationTestBase.updateCustomerListForTests("PICT_TEST_50",
                customer);
    }

    public void createPictScenario49() throws Exception {
        // ////////////////////////////////
        // NO FREE PERIOD//////////////////
        // /////////////////////////////////
        setDateFactory("2013-06-09 20:00:00");

        // Create a supplier with a marketplace
        String supplierAdminId = "Pict49Supplier";
        VOOrganization supplier = orgSetup.createOrganization(
                basicSetup.getPlatformOperatorUserKey(), supplierAdminId,
                "Pict49SupplierOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                OrganizationRoleType.TECHNOLOGY_PROVIDER,
                OrganizationRoleType.SUPPLIER);
        VOUser supplierAdmin = orgSetup.getUser(supplierAdminId, true);
        paymentSetup.createPaymentForSupplier(
                basicSetup.getPlatformOperatorUserKey(),
                supplierAdmin.getKey(), supplier);

        // Create own technical service
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        serviceSetup
                .importTechnicalServices(BaseAdmUmTest.TECHNICAL_SERVICE_EXAMPLE2_XML);
        VOTechnicalService example2Service = serviceSetup
                .getTechnicalService(VOTechServiceFactory.TECH_SERVICE_EXAMPLE2_ID);

        setCutOffDay(supplierAdmin.getKey(), 2);

        // Create a customer
        String customerAdminId = "Pict49Customer";
        VOOrganization customer = orgSetup.registerCustomer(
                "Pict49CustomerOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                customerAdminId, supplierMarketplace.getMarketplaceId(),
                supplier.getOrganizationId());
        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);

        orgSetup.saveAllVats(supplierAdmin.getKey(), VOVatRateFactory
                .newVOVatRate(new BigDecimal("13.00")), Arrays.asList(
                VOVatRateFactory.newVOCountryVatRate(new BigDecimal("20.00"),
                        TestOrganizationSetup.ORGANIZATION_DOMICILE_UK),
                VOVatRateFactory.newVOCountryVatRate(new BigDecimal("19.00"),
                        TestOrganizationSetup.ORGANIZATION_DOMICILE_DE)), null);

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        supplierAdmin.getKey(), "PICT_TEST_49",
                        TestService.EXAMPLE2,
                        TestPriceModel.EXAMPLE_PICT49_HOUR_ROLES,
                        example2Service, supplierMarketplace);

        orgSetup.createMarketingPermission(supplierAdmin.getKey(),
                supplier.getOrganizationId(), example2Service);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "GUEST");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "PICT_TEST_49", serviceDetails, customerAdmin, role);
        // Events
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        subscrSetup.recordEventForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-06-09 12:00:00"),
                "FILE_DOWNLOAD", 15);

        // user is assigned

        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        setDateFactory("2013-06-10 10:00:00");
        VORoleDefinition changedRole = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        subDetails = subscrSetup.addUser(basicSetup.getSecondCustomerUser1(),
                changedRole, subDetails.getSubscriptionId());

        // user is deassigned
        setDateFactory("2013-06-10 11:00:00");
        subDetails = subscrSetup.revokeUser(
                basicSetup.getSecondCustomerUser1(),
                subDetails.getSubscriptionId());

        // user is assigned
        setDateFactory("2013-06-12 10:00:00");
        subDetails = subscrSetup.addUser(basicSetup.getSecondCustomerUser1(),
                changedRole, subDetails.getSubscriptionId());

        // user is deassigned
        setDateFactory("2013-06-12 12:00:00");
        subDetails = subscrSetup.revokeUser(
                basicSetup.getSecondCustomerUser1(),
                subDetails.getSubscriptionId());

        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);

        // terminate sub
        long userterminateTime = DateTimeHandling
                .calculateMillis("2013-06-12 20:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(userterminateTime);
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        // Delete the marketable service
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-06-13 13:00:01"));
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER);
        serviceSetup.deleteMarketableService(serviceDetails);

        // Delete also the technical service
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-06-13 13:00:00"));
        container.login(supplierAdmin.getKey(), ROLE_TECHNOLOGY_MANAGER);
        serviceSetup.deleteTechnicalService(example2Service);

        // reset old cut off day
        resetCutOffDay(supplierAdmin.getKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_49", subDetails);

        BillingIntegrationTestBase.updateCustomerListForTests("PICT_TEST_49",
                customer);
    }

    public void createPictScenario49_1() throws Exception {

        // ////////////////////////////////
        // FREE PERIOD 1 day//////////////
        // /////////////////////////////////
        setDateFactory("2013-06-08 20:00:00");

        // Create a supplier with a marketplace
        String supplierAdminId = "Pict49_1Supplier";
        VOOrganization supplier = orgSetup.createOrganization(
                basicSetup.getPlatformOperatorUserKey(), supplierAdminId,
                "Pict49_1SupplierOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                OrganizationRoleType.TECHNOLOGY_PROVIDER,
                OrganizationRoleType.SUPPLIER);
        VOUser supplierAdmin = orgSetup.getUser(supplierAdminId, true);
        paymentSetup.createPaymentForSupplier(
                basicSetup.getPlatformOperatorUserKey(),
                supplierAdmin.getKey(), supplier);

        // Create own technical service
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        serviceSetup
                .importTechnicalServices(BaseAdmUmTest.TECHNICAL_SERVICE_EXAMPLE2_XML);
        VOTechnicalService example2Service = serviceSetup
                .getTechnicalService(VOTechServiceFactory.TECH_SERVICE_EXAMPLE2_ID);

        setCutOffDay(supplierAdmin.getKey(), 2);

        // Create a customer
        String customerAdminId = "Pict49_1Customer";
        VOOrganization customer = orgSetup.registerCustomer(
                "Pict49_1CustomerOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                customerAdminId, supplierMarketplace.getMarketplaceId(),
                supplier.getOrganizationId());
        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);

        orgSetup.saveAllVats(supplierAdmin.getKey(), VOVatRateFactory
                .newVOVatRate(new BigDecimal("13.00")), Arrays.asList(
                VOVatRateFactory.newVOCountryVatRate(new BigDecimal("20.00"),
                        TestOrganizationSetup.ORGANIZATION_DOMICILE_UK),
                VOVatRateFactory.newVOCountryVatRate(new BigDecimal("19.00"),
                        TestOrganizationSetup.ORGANIZATION_DOMICILE_DE)), null);

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        supplierAdmin.getKey(), "PICT_TEST_49_1",
                        TestService.EXAMPLE2,
                        TestPriceModel.EXAMPLE_PICT49_1_HOUR_ROLES,
                        example2Service, supplierMarketplace);

        orgSetup.createMarketingPermission(supplierAdmin.getKey(),
                supplier.getOrganizationId(), example2Service);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "GUEST");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "PICT_TEST_49_1", serviceDetails, customerAdmin, role);
        // Events
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        subscrSetup.recordEventForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-06-09 12:00:00"),
                "FILE_DOWNLOAD", 15);

        // user is assigned
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        setDateFactory("2013-06-10 10:00:00");
        VORoleDefinition changedRole = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        subDetails = subscrSetup.addUser(basicSetup.getSecondCustomerUser1(),
                changedRole, subDetails.getSubscriptionId());

        // user is deassigned
        setDateFactory("2013-06-10 10:59:59");
        subDetails = subscrSetup.revokeUser(
                basicSetup.getSecondCustomerUser1(),
                subDetails.getSubscriptionId());

        // role change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        setDateFactory("2013-06-10 20:00:00");
        subDetails = subscrSetup.modifyUserRole(subDetails.getUsageLicenses()
                .get(0), VOServiceFactory.getRole(serviceDetails, "USER"),
                subDetails.getSubscriptionId());

        // user is assigned
        setDateFactory("2013-06-12 10:00:00");
        subDetails = subscrSetup.addUser(basicSetup.getSecondCustomerUser1(),
                changedRole, subDetails.getSubscriptionId());

        // user is deassigned
        setDateFactory("2013-06-12 11:59:59");
        subDetails = subscrSetup.revokeUser(
                basicSetup.getSecondCustomerUser1(),
                subDetails.getSubscriptionId());

        // terminate sub
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        long userterminateTime = DateTimeHandling
                .calculateMillis("2013-06-12 20:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(userterminateTime);
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        // Delete the marketable service
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-06-13 13:00:01"));
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER);
        serviceSetup.deleteMarketableService(serviceDetails);

        // Delete also the technical service
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-06-13 13:00:00"));
        container.login(supplierAdmin.getKey(), ROLE_TECHNOLOGY_MANAGER);
        serviceSetup.deleteTechnicalService(example2Service);

        // reset old cut off day
        resetCutOffDay(supplierAdmin.getKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_49_1", subDetails);

        BillingIntegrationTestBase.updateCustomerListForTests("PICT_TEST_49_1",
                customer);
    }

    public void createPictScenario49_2() throws Exception {
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-06-09 20:00:00"));

        // Create a supplier with a marketplace
        String supplierAdminId = "Pict49_2Supplier";
        VOOrganization supplier = orgSetup.createOrganization(
                basicSetup.getPlatformOperatorUserKey(), supplierAdminId,
                "Pict49_2SupplierOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                OrganizationRoleType.TECHNOLOGY_PROVIDER,
                OrganizationRoleType.SUPPLIER);
        VOUser supplierAdmin = orgSetup.getUser(supplierAdminId, true);
        paymentSetup.createPaymentForSupplier(
                basicSetup.getPlatformOperatorUserKey(),
                supplierAdmin.getKey(), supplier);

        // Create own technical service
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        serviceSetup
                .importTechnicalServices(BaseAdmUmTest.TECHNICAL_SERVICE_EXAMPLE2_XML);
        VOTechnicalService example2Service = serviceSetup
                .getTechnicalService(VOTechServiceFactory.TECH_SERVICE_EXAMPLE2_ID);

        setCutOffDay(supplierAdmin.getKey(), 2);

        // Create a customer
        String customerAdminId = "Pict49_2Customer";
        VOOrganization customer = orgSetup.registerCustomer(
                "Pict49_2CustomerOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                customerAdminId, supplierMarketplace.getMarketplaceId(),
                supplier.getOrganizationId());
        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);

        orgSetup.saveAllVats(supplierAdmin.getKey(), VOVatRateFactory
                .newVOVatRate(new BigDecimal("13.00")), Arrays.asList(
                VOVatRateFactory.newVOCountryVatRate(new BigDecimal("20.00"),
                        TestOrganizationSetup.ORGANIZATION_DOMICILE_UK),
                VOVatRateFactory.newVOCountryVatRate(new BigDecimal("19.00"),
                        TestOrganizationSetup.ORGANIZATION_DOMICILE_DE)), null);

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        supplierAdmin.getKey(), "PICT_TEST_49_2",
                        TestService.EXAMPLE2,
                        TestPriceModel.EXAMPLE_PICT49_HOUR_ROLES,
                        example2Service, supplierMarketplace);

        orgSetup.createMarketingPermission(supplierAdmin.getKey(),
                supplier.getOrganizationId(), example2Service);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "GUEST");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "PICT_TEST_49_2", serviceDetails, customerAdmin, role);

        // user is assigned
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        long userAssignedTime = DateTimeHandling
                .calculateMillis("2013-06-10 10:00:01");
        BillingIntegrationTestBase.setDateFactoryInstance(userAssignedTime);
        VORoleDefinition changedRole = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        subDetails = subscrSetup.addUser(basicSetup.getSecondCustomerUser1(),
                changedRole, subDetails.getSubscriptionId());

        // user is deassigned
        long userDeassignedTime = DateTimeHandling
                .calculateMillis("2013-06-10 10:59:58");
        BillingIntegrationTestBase.setDateFactoryInstance(userDeassignedTime);
        subscrSetup.revokeUser(basicSetup.getSecondCustomerUser1(),
                subDetails.getSubscriptionId());

        // same user is assigned in same charged period
        userAssignedTime = DateTimeHandling
                .calculateMillis("2013-06-10 10:59:59");
        BillingIntegrationTestBase.setDateFactoryInstance(userAssignedTime);
        subDetails = subscrSetup.addUser(basicSetup.getSecondCustomerUser1(),
                changedRole, subDetails.getSubscriptionId());

        // user is deassigned
        userDeassignedTime = DateTimeHandling
                .calculateMillis("2013-06-10 11:59:59");
        BillingIntegrationTestBase.setDateFactoryInstance(userDeassignedTime);
        subscrSetup.revokeUser(basicSetup.getSecondCustomerUser1(),
                subDetails.getSubscriptionId());

        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);

        // terminate sub
        long userterminateTime = DateTimeHandling
                .calculateMillis("2013-06-12 19:59:00");
        BillingIntegrationTestBase.setDateFactoryInstance(userterminateTime);
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        // reset old cut off day
        resetCutOffDay(supplierAdmin.getKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_49_2", subDetails);

        BillingIntegrationTestBase.updateCustomerListForTests("PICT_TEST_49_2",
                customer);
    }

    public void createPictScenario49_3() throws Exception {
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-06-09 20:00:00"));

        // Create a supplier with a marketplace
        String supplierAdminId = "Pict49_3Supplier";
        VOOrganization supplier = orgSetup.createOrganization(
                basicSetup.getPlatformOperatorUserKey(), supplierAdminId,
                "Pict49_3SupplierOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                OrganizationRoleType.TECHNOLOGY_PROVIDER,
                OrganizationRoleType.SUPPLIER);
        VOUser supplierAdmin = orgSetup.getUser(supplierAdminId, true);
        paymentSetup.createPaymentForSupplier(
                basicSetup.getPlatformOperatorUserKey(),
                supplierAdmin.getKey(), supplier);

        // Create own technical service
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        serviceSetup
                .importTechnicalServices(BaseAdmUmTest.TECHNICAL_SERVICE_EXAMPLE2_XML);
        VOTechnicalService example2Service = serviceSetup
                .getTechnicalService(VOTechServiceFactory.TECH_SERVICE_EXAMPLE2_ID);

        setCutOffDay(supplierAdmin.getKey(), 2);

        // Create a customer
        String customerAdminId = "Pict49_3Customer";
        VOOrganization customer = orgSetup.registerCustomer(
                "Pict49_3CustomerOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                customerAdminId, supplierMarketplace.getMarketplaceId(),
                supplier.getOrganizationId());
        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);

        orgSetup.saveAllVats(supplierAdmin.getKey(), VOVatRateFactory
                .newVOVatRate(new BigDecimal("13.00")), Arrays.asList(
                VOVatRateFactory.newVOCountryVatRate(new BigDecimal("20.00"),
                        TestOrganizationSetup.ORGANIZATION_DOMICILE_UK),
                VOVatRateFactory.newVOCountryVatRate(new BigDecimal("19.00"),
                        TestOrganizationSetup.ORGANIZATION_DOMICILE_DE)), null);

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        supplierAdmin.getKey(), "PICT_TEST_49_3",
                        TestService.EXAMPLE2,
                        TestPriceModel.EXAMPLE_PICT49_HOUR_ROLES,
                        example2Service, supplierMarketplace);

        orgSetup.createMarketingPermission(supplierAdmin.getKey(),
                supplier.getOrganizationId(), example2Service);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "GUEST");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "PICT_TEST_49_3", serviceDetails, customerAdmin, role);

        // user is assigned
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        long userAssignedTime = DateTimeHandling
                .calculateMillis("2013-06-10 10:00:01");
        BillingIntegrationTestBase.setDateFactoryInstance(userAssignedTime);
        VORoleDefinition changedRole = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        subDetails = subscrSetup.addUser(basicSetup.getSecondCustomerUser1(),
                changedRole, subDetails.getSubscriptionId());

        // user is deassigned
        long userDeassignedTime = DateTimeHandling
                .calculateMillis("2013-06-10 10:59:58");
        BillingIntegrationTestBase.setDateFactoryInstance(userDeassignedTime);
        subscrSetup.revokeUser(basicSetup.getSecondCustomerUser1(),
                subDetails.getSubscriptionId());

        // other user is assigned in same charged period
        userAssignedTime = DateTimeHandling
                .calculateMillis("2013-06-10 10:59:59");
        BillingIntegrationTestBase.setDateFactoryInstance(userAssignedTime);
        changedRole = VOServiceFactory.getRole(serviceDetails, "USER");
        subDetails = subscrSetup.addUser(basicSetup.getSecondCustomerUser2(),
                changedRole, subDetails.getSubscriptionId());

        // user is deassigned
        userDeassignedTime = DateTimeHandling
                .calculateMillis("2013-06-10 11:59:59");
        BillingIntegrationTestBase.setDateFactoryInstance(userDeassignedTime);
        subscrSetup.revokeUser(basicSetup.getSecondCustomerUser2(),
                subDetails.getSubscriptionId());

        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);

        // terminate sub
        long userterminateTime = DateTimeHandling
                .calculateMillis("2013-06-12 19:59:00");
        BillingIntegrationTestBase.setDateFactoryInstance(userterminateTime);
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        // reset old cut off day
        resetCutOffDay(supplierAdmin.getKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_49_3", subDetails);

        BillingIntegrationTestBase.updateCustomerListForTests("PICT_TEST_49_3",
                customer);
    }

    public void createPictScenario49_4() throws Exception {
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-06-09 20:00:00"));

        // Create a supplier with a marketplace
        String supplierAdminId = "Pict49_4Supplier";
        VOOrganization supplier = orgSetup.createOrganization(
                basicSetup.getPlatformOperatorUserKey(), supplierAdminId,
                "Pict49_4SupplierOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                OrganizationRoleType.TECHNOLOGY_PROVIDER,
                OrganizationRoleType.SUPPLIER);
        VOUser supplierAdmin = orgSetup.getUser(supplierAdminId, true);
        paymentSetup.createPaymentForSupplier(
                basicSetup.getPlatformOperatorUserKey(),
                supplierAdmin.getKey(), supplier);

        // Create own technical service
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        serviceSetup
                .importTechnicalServices(BaseAdmUmTest.TECHNICAL_SERVICE_EXAMPLE2_XML);
        VOTechnicalService example2Service = serviceSetup
                .getTechnicalService(VOTechServiceFactory.TECH_SERVICE_EXAMPLE2_ID);

        setCutOffDay(supplierAdmin.getKey(), 2);

        // Create a customer
        String customerAdminId = "Pict49_4Customer";
        VOOrganization customer = orgSetup.registerCustomer(
                "Pict49_4CustomerOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                customerAdminId, supplierMarketplace.getMarketplaceId(),
                supplier.getOrganizationId());
        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);

        orgSetup.saveAllVats(supplierAdmin.getKey(), VOVatRateFactory
                .newVOVatRate(new BigDecimal("13.00")), Arrays.asList(
                VOVatRateFactory.newVOCountryVatRate(new BigDecimal("20.00"),
                        TestOrganizationSetup.ORGANIZATION_DOMICILE_UK),
                VOVatRateFactory.newVOCountryVatRate(new BigDecimal("19.00"),
                        TestOrganizationSetup.ORGANIZATION_DOMICILE_DE)), null);

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        supplierAdmin.getKey(), "PICT_TEST_49_4",
                        TestService.EXAMPLE2,
                        TestPriceModel.EXAMPLE_PICT49_HOUR_ROLES,
                        example2Service, supplierMarketplace);

        orgSetup.createMarketingPermission(supplierAdmin.getKey(),
                supplier.getOrganizationId(), example2Service);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "GUEST");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "PICT_TEST_49_4", serviceDetails, customerAdmin, role);

        // user is assigned
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        long userAssignedTime = DateTimeHandling
                .calculateMillis("2013-06-10 10:00:01");
        BillingIntegrationTestBase.setDateFactoryInstance(userAssignedTime);
        VORoleDefinition changedRole = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        subDetails = subscrSetup.addUser(basicSetup.getSecondCustomerUser1(),
                changedRole, subDetails.getSubscriptionId());

        // user is deassigned
        long userDeassignedTime = DateTimeHandling
                .calculateMillis("2013-06-10 10:59:58");
        BillingIntegrationTestBase.setDateFactoryInstance(userDeassignedTime);
        subscrSetup.revokeUser(basicSetup.getSecondCustomerUser1(),
                subDetails.getSubscriptionId());

        // same user is assigned in other charged period

        userAssignedTime = DateTimeHandling
                .calculateMillis("2013-06-10 11:59:58");
        BillingIntegrationTestBase.setDateFactoryInstance(userAssignedTime);
        subDetails = subscrSetup.addUser(basicSetup.getSecondCustomerUser1(),
                changedRole, subDetails.getSubscriptionId());

        // user is deassigned
        userDeassignedTime = DateTimeHandling
                .calculateMillis("2013-06-10 11:59:59");
        BillingIntegrationTestBase.setDateFactoryInstance(userDeassignedTime);
        subscrSetup.revokeUser(basicSetup.getSecondCustomerUser1(),
                subDetails.getSubscriptionId());

        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);

        // terminate sub
        long userterminateTime = DateTimeHandling
                .calculateMillis("2013-06-12 19:59:00");
        BillingIntegrationTestBase.setDateFactoryInstance(userterminateTime);
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        // reset old cut off day
        resetCutOffDay(supplierAdmin.getKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_49_4", subDetails);

        BillingIntegrationTestBase.updateCustomerListForTests("PICT_TEST_49_4",
                customer);
    }

    public void createPictScenario48() throws Exception {
        setDateFactory("2013-05-28 20:00:00");

        // Create a supplier with a marketplace
        String supplierAdminId = "Pict48Supplier";
        VOOrganization supplier = orgSetup.createOrganization(
                basicSetup.getPlatformOperatorUserKey(), supplierAdminId,
                "Pict48SupplierOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                OrganizationRoleType.TECHNOLOGY_PROVIDER,
                OrganizationRoleType.SUPPLIER);
        VOUser supplierAdmin = orgSetup.getUser(supplierAdminId, true);
        paymentSetup.createPaymentForSupplier(
                basicSetup.getPlatformOperatorUserKey(),
                supplierAdmin.getKey(), supplier);

        setCutOffDay(supplierAdmin.getKey(), 1);

        // Create a customer
        String customerAdminId = "Pict48Customer";
        VOOrganization customer = orgSetup.registerCustomer(
                "Pict48CustomerOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                customerAdminId, supplierMarketplace.getMarketplaceId(),
                supplier.getOrganizationId());
        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);

        orgSetup.saveAllVats(supplierAdmin.getKey(), VOVatRateFactory
                .newVOVatRate(new BigDecimal("13.00")), Arrays.asList(
                VOVatRateFactory.newVOCountryVatRate(new BigDecimal("20.00"),
                        TestOrganizationSetup.ORGANIZATION_DOMICILE_UK),
                VOVatRateFactory.newVOCountryVatRate(new BigDecimal("19.00"),
                        TestOrganizationSetup.ORGANIZATION_DOMICILE_DE)), null);

        orgSetup.createMarketingPermission(basicSetup.getSupplierAdminKey(),
                supplier.getOrganizationId(), technicalService);

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        supplierAdmin.getKey(), "PICT_TEST_48",
                        TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PICT48_RATA_WEEKS,
                        technicalService, supplierMarketplace);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "GUEST");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "PICT_TEST_48", serviceDetails, customerAdmin, role);

        // // suspend/resume subscription several times by removing/restoring
        // // customer's payment types
        setDateFactory("2013-05-29 07:00:00");

        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);

        paymentSetup.deleteCustomerPaymentTypes(customer);

        setDateFactory("2013-06-12 07:00:00");

        paymentSetup.reassignCustomerPaymentTypes(customer);

        subDetails = subscrSetup.getSubscriptionDetails(customerAdmin.getKey(),
                subDetails.getSubscriptionId());

        // 1. create upgraded service
        VOServiceDetails freeService = serviceSetup
                .createPublishAndActivateMarketableService(
                        supplierAdmin.getKey(), "P48_Upgrade",
                        TestService.EXAMPLE, TestPriceModel.FREE,
                        technicalService, supplierMarketplace);

        serviceSetup.registerCompatibleServices(supplierAdmin.getKey(),
                serviceDetails, freeService);

        setDateFactory("2013-06-14 20:00:00");

        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(subDetails, freeService);

        // delete market service
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-06-13 13:00:00"));
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER);
        serviceSetup.deleteMarketableService(serviceDetails);

        // change sunbscriptionID
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-06-15 00:00:00"));
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        upgradedSubDetails.setSubscriptionId("PICT_TEST_48" + "_SubID2");
        upgradedSubDetails = subscrSetup.modifySubscription(upgradedSubDetails,
                null);

        setDateFactory("2013-06-20 20:00:00");
        subscrSetup
                .unsubscribeToService(upgradedSubDetails.getSubscriptionId());

        resetCutOffDay(supplierAdmin.getKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_48", subDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_48", upgradedSubDetails);

        BillingIntegrationTestBase.updateCustomerListForTests("PICT_TEST_48",
                customer);

    }

    public void createPictScenario47() throws Exception {
        setDateFactory("2013-04-30 20:00:00");

        // Create a supplier with a marketplace
        String supplierAdminId = "Pict47Supplier";
        VOOrganization supplier = orgSetup.createOrganization(
                basicSetup.getPlatformOperatorUserKey(), supplierAdminId,
                "Pict47SupplierOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                OrganizationRoleType.TECHNOLOGY_PROVIDER,
                OrganizationRoleType.SUPPLIER);
        VOUser supplierAdmin = orgSetup.getUser(supplierAdminId, true);
        paymentSetup.createPaymentForSupplier(
                basicSetup.getPlatformOperatorUserKey(),
                supplierAdmin.getKey(), supplier);

        // Create own technical service
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        serviceSetup
                .importTechnicalServices(BaseAdmUmTest.TECHNICAL_SERVICE_EXAMPLE2_XML);
        VOTechnicalService example2Service = serviceSetup
                .getTechnicalService(VOTechServiceFactory.TECH_SERVICE_EXAMPLE2_ID);

        setCutOffDay(supplierAdmin.getKey(), 3);

        // Create a customer
        String customerAdminId = "Pict47Customer";
        VOOrganization customer = orgSetup.registerCustomer(
                "Pict47CustomerOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                customerAdminId, supplierMarketplace.getMarketplaceId(),
                supplier.getOrganizationId());
        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);

        orgSetup.saveAllVats(supplierAdmin.getKey(), VOVatRateFactory
                .newVOVatRate(new BigDecimal("13.00")), Arrays.asList(
                VOVatRateFactory.newVOCountryVatRate(new BigDecimal("20.00"),
                        TestOrganizationSetup.ORGANIZATION_DOMICILE_UK),
                VOVatRateFactory.newVOCountryVatRate(new BigDecimal("19.00"),
                        TestOrganizationSetup.ORGANIZATION_DOMICILE_DE)),
                Arrays.asList(VOVatRateFactory.newVOOrganizationVatRate(
                        new BigDecimal("19.00"), customer)));

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        supplierAdmin.getKey(), "PICT_TEST_47",
                        TestService.EXAMPLE2,
                        TestPriceModel.EXAMPLE_PICT47_UNIT_WEEKS,
                        example2Service, supplierMarketplace);

        orgSetup.createMarketingPermission(supplierAdmin.getKey(),
                supplier.getOrganizationId(), example2Service);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "GUEST");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "PICT_TEST_47", serviceDetails, customerAdmin, role);

        // Events
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        subscrSetup.recordEventForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-05-15 12:00:00"),
                "FILE_DOWNLOAD", 35);

        // user is assigned

        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        setDateFactory("2013-05-30 07:00:00");
        VORoleDefinition changedRole = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        subDetails = subscrSetup.addUser(basicSetup.getSecondCustomerUser1(),
                changedRole, subDetails.getSubscriptionId());

        // user is deassigned
        setDateFactory("2013-05-30 11:00:00");
        subscrSetup.revokeUser(basicSetup.getSecondCustomerUser1(),
                subDetails.getSubscriptionId());

        // user is assigned
        setDateFactory("2013-05-30 12:00:00");
        subDetails = subscrSetup.addUser(basicSetup.getSecondCustomerUser1(),
                changedRole, subDetails.getSubscriptionId());

        // delete market service
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-05-13 13:00:00"));
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER);
        serviceSetup.deleteMarketableService(serviceDetails);

        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);

        setDateFactory("2013-05-30 20:00:00");
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        // Delete also the technical service
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-06-13 13:00:00"));
        container.login(supplierAdmin.getKey(), ROLE_TECHNOLOGY_MANAGER);
        serviceSetup.deleteTechnicalService(example2Service);

        resetCutOffDay(supplierAdmin.getKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_47", subDetails);

        BillingIntegrationTestBase.updateCustomerListForTests("PICT_TEST_47",
                customer);

    }

    public void createPictScenario46() throws Exception {
        setDateFactory("2013-04-07 20:00:00");

        // Create a supplier with a marketplace
        String supplierAdminId = "Pict46Supplier";
        VOOrganization supplier = orgSetup.createOrganization(
                basicSetup.getPlatformOperatorUserKey(), supplierAdminId,
                "Pict46SupplierOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                OrganizationRoleType.TECHNOLOGY_PROVIDER,
                OrganizationRoleType.SUPPLIER);
        VOUser supplierAdmin = orgSetup.getUser(supplierAdminId, true);
        paymentSetup.createPaymentForSupplier(
                basicSetup.getPlatformOperatorUserKey(),
                supplierAdmin.getKey(), supplier);

        // Create own technical service
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        serviceSetup
                .importTechnicalServices(BaseAdmUmTest.TECHNICAL_SERVICE_EXAMPLE2_XML);
        VOTechnicalService example2Service = serviceSetup
                .getTechnicalService(VOTechServiceFactory.TECH_SERVICE_EXAMPLE2_ID);

        setCutOffDay(supplierAdmin.getKey(), 1);

        // Create a customer
        String customerAdminId = "Pict46Customer";
        VOOrganization customer = orgSetup.registerCustomer(
                "Pict46CustomerOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                customerAdminId, supplierMarketplace.getMarketplaceId(),
                supplier.getOrganizationId());
        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);

        orgSetup.saveAllVats(supplierAdmin.getKey(), VOVatRateFactory
                .newVOVatRate(new BigDecimal("13.00")), Arrays.asList(
                VOVatRateFactory.newVOCountryVatRate(new BigDecimal("20.00"),
                        TestOrganizationSetup.ORGANIZATION_DOMICILE_UK),
                VOVatRateFactory.newVOCountryVatRate(new BigDecimal("19.00"),
                        TestOrganizationSetup.ORGANIZATION_DOMICILE_DE)),
                Arrays.asList(VOVatRateFactory.newVOOrganizationVatRate(
                        new BigDecimal("5.00"), customer)));

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        supplierAdmin.getKey(), "PICT_TEST_46",
                        TestService.EXAMPLE2,
                        TestPriceModel.EXAMPLE_PICT46_UNIT_WEEKS,
                        example2Service, supplierMarketplace);

        orgSetup.createMarketingPermission(supplierAdmin.getKey(),
                supplier.getOrganizationId(), example2Service);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "GUEST");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "PICT_TEST_46", serviceDetails, customerAdmin, role);

        // Events
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        subscrSetup.recordEventForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-04-15 12:00:00"),
                "FILE_DOWNLOAD", 35);

        // // suspend/resume subscription several times by removing/restoring
        // // customer's payment types
        setDateFactory("2013-04-10 07:00:00");

        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);

        paymentSetup.deleteCustomerPaymentTypes(customer);

        setDateFactory("2013-04-17 07:00:00");

        paymentSetup.reassignCustomerPaymentTypes(customer);

        subDetails = subscrSetup.getSubscriptionDetails(customerAdmin.getKey(),
                subDetails.getSubscriptionId());

        // role change

        setDateFactory("2013-04-17 10:00:00");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        subDetails = subscrSetup.modifyUserRole(subDetails.getUsageLicenses()
                .get(0), VOServiceFactory.getRole(serviceDetails, "USER"),
                subDetails.getSubscriptionId());
        // parameter change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        subDetails = subscrSetup.modifyParameterForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-04-17 11:00:00"),
                "MAX_FOLDER_NUMBER", "4");

        setDateFactory("2013-04-22 10:00:00");
        subDetails = subscrSetup.modifyUserRole(subDetails.getUsageLicenses()
                .get(0), VOServiceFactory.getRole(serviceDetails, "ADMIN"),
                subDetails.getSubscriptionId());
        subDetails = subscrSetup.modifyParameterForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-04-22 11:00:00"),
                "MAX_FOLDER_NUMBER", "10");

        // price model change
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        setDateFactory("2013-04-20 08:00:00");

        VOPriceModel newSubPriceModel = VOPriceModelFactory
                .modifyPriceModelPeriodFee(subDetails.getPriceModel(),
                        new BigDecimal("445.00"));

        subscrSetup.savePriceModelForSubscription(supplierAdmin.getKey(),
                subDetails, newSubPriceModel, customer);

        // // delete market service
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-04-23 13:00:00"));
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER);
        serviceSetup.deleteMarketableService(serviceDetails);

        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);

        setDateFactory("2013-04-30 20:00:00");
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(supplierAdmin.getKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_46", subDetails);

        BillingIntegrationTestBase.updateCustomerListForTests("PICT_TEST_46",
                customer);

    }

    public void createPictScenario45_1() throws Exception {
        setDateFactory("2013-05-07 20:00:00");

        // Create a supplier with a marketplace
        String supplierAdminId = "Pict45_1Supplier";
        VOOrganization supplier = orgSetup.createOrganization(
                basicSetup.getPlatformOperatorUserKey(), supplierAdminId,
                "Pict45_1SupplierOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                OrganizationRoleType.TECHNOLOGY_PROVIDER,
                OrganizationRoleType.SUPPLIER);
        VOUser supplierAdmin = orgSetup.getUser(supplierAdminId, true);
        paymentSetup.createPaymentForSupplier(
                basicSetup.getPlatformOperatorUserKey(),
                supplierAdmin.getKey(), supplier);

        // Create own technical service
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        serviceSetup
                .importTechnicalServices(BaseAdmUmTest.TECHNICAL_SERVICE_EXAMPLE2_XML);
        VOTechnicalService example2Service = serviceSetup
                .getTechnicalService(VOTechServiceFactory.TECH_SERVICE_EXAMPLE2_ID);

        setCutOffDay(supplierAdmin.getKey(), 15);

        // Create a customer
        String customerAdminId = "Pict45_1Customer";
        VOOrganization customer = orgSetup.registerCustomer(
                "Pict45_1CustomerOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                customerAdminId, supplierMarketplace.getMarketplaceId(),
                supplier.getOrganizationId());
        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);

        orgSetup.updateCustomerDiscount(customer, new BigDecimal("25.00"),
                DateTimeHandling.calculateMillis("2013-05-06 00:00:00"),
                DateTimeHandling.calculateMillis("2013-05-06 01:00:00"));

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        supplierAdmin.getKey(), "PICT_TEST_45_1",
                        TestService.EXAMPLE2, TestPriceModel.FREE,
                        example2Service, supplierMarketplace);

        orgSetup.createMarketingPermission(supplierAdmin.getKey(),
                supplier.getOrganizationId(), example2Service);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "GUEST");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "PICT_TEST_45_1", serviceDetails, customerAdmin, role);

        // Events
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        subscrSetup.recordEventForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-05-08 12:00:00"),
                "FILE_DOWNLOAD", 35);

        // // suspend/resume subscription several times by removing/restoring
        // // customer's payment types
        setDateFactory("2013-05-10 07:00:00");

        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);

        paymentSetup.deleteCustomerPaymentTypes(customer);

        setDateFactory("2013-05-10 08:00:00");

        paymentSetup.reassignCustomerPaymentTypes(customer);

        subDetails = subscrSetup.getSubscriptionDetails(customerAdmin.getKey(),
                subDetails.getSubscriptionId());

        // role change

        setDateFactory("2013-05-11 10:00:00");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        subDetails = subscrSetup.modifyUserRole(subDetails.getUsageLicenses()
                .get(0), VOServiceFactory.getRole(serviceDetails, "USER"),
                subDetails.getSubscriptionId());
        // parameter change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        subDetails = subscrSetup.modifyParameterForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-05-11 12:00:00"),
                "MAX_FOLDER_NUMBER", "4");

        setDateFactory("2013-05-12 10:00:00");
        subDetails = subscrSetup.modifyUserRole(subDetails.getUsageLicenses()
                .get(0), VOServiceFactory.getRole(serviceDetails, "ADMIN"),
                subDetails.getSubscriptionId());
        subDetails = subscrSetup.modifyParameterForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-05-12 12:00:00"),
                "MAX_FOLDER_NUMBER", "10");

        // 1. create upgraded service
        VOServiceDetails paidService = serviceSetup
                .createPublishAndActivateMarketableService(
                        supplierAdmin.getKey(), "PAIRWISE45_1_Upgrade",
                        TestService.EXAMPLE2,
                        TestPriceModel.EXAMPLE_PERUNIT_WEEK_FREEP,
                        example2Service, supplierMarketplace);

        serviceSetup.registerCompatibleServices(supplierAdmin.getKey(),
                serviceDetails, paidService);

        setDateFactory("2013-05-13 20:00:00");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(subDetails, paidService);

        // 2.create upgraded service
        VOServiceDetails paidService2 = serviceSetup
                .createPublishAndActivateMarketableService(
                        supplierAdmin.getKey(), "PAIRWISE45_1_Upgrade2",
                        TestService.EXAMPLE2,
                        TestPriceModel.EXAMPLE_PERUNIT_WEEK_FREEP,
                        example2Service, supplierMarketplace);

        serviceSetup.registerCompatibleServices(supplierAdmin.getKey(),
                paidService, paidService2);

        setDateFactory("2013-05-14 20:00:00");

        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetails2 = subscrSetup
                .upgradeSubscription(upgradedSubDetails, paidService2);

        setDateFactory("2013-05-15 20:00:00");
        subscrSetup
                .unsubscribeToService(upgradedSubDetails.getSubscriptionId());

        resetCutOffDay(supplierAdmin.getKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_45_1", subDetails);

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_45_1", upgradedSubDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_45_1", upgradedSubDetails2);

        BillingIntegrationTestBase.updateCustomerListForTests("PICT_TEST_45_1",
                customer);

    }

    public void createPictScenario45() throws Exception {
        setDateFactory("2013-05-07 20:00:00");

        // Create a supplier with a marketplace
        String supplierAdminId = "Pict45Supplier";
        VOOrganization supplier = orgSetup.createOrganization(
                basicSetup.getPlatformOperatorUserKey(), supplierAdminId,
                "Pict45SupplierOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                OrganizationRoleType.TECHNOLOGY_PROVIDER,
                OrganizationRoleType.SUPPLIER);
        VOUser supplierAdmin = orgSetup.getUser(supplierAdminId, true);
        paymentSetup.createPaymentForSupplier(
                basicSetup.getPlatformOperatorUserKey(),
                supplierAdmin.getKey(), supplier);

        // Create own technical service
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        serviceSetup
                .importTechnicalServices(BaseAdmUmTest.TECHNICAL_SERVICE_EXAMPLE2_XML);
        VOTechnicalService example2Service = serviceSetup
                .getTechnicalService(VOTechServiceFactory.TECH_SERVICE_EXAMPLE2_ID);

        setCutOffDay(supplierAdmin.getKey(), 15);

        // Create a customer
        String customerAdminId = "Pict45Customer";
        VOOrganization customer = orgSetup.registerCustomer(
                "Pict45CustomerOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                customerAdminId, supplierMarketplace.getMarketplaceId(),
                supplier.getOrganizationId());
        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        supplierAdmin.getKey(), "PICT_TEST_45",
                        TestService.EXAMPLE2,
                        TestPriceModel.EXAMPLE_PERUNIT_DAY_ROLES,
                        example2Service, supplierMarketplace);

        orgSetup.createMarketingPermission(supplierAdmin.getKey(),
                supplier.getOrganizationId(), example2Service);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "GUEST");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "PICT_TEST_45", serviceDetails, customerAdmin, role);

        // // 1. create upgraded service
        VOServiceDetails paidService = serviceSetup
                .createPublishAndActivateMarketableService(
                        supplierAdmin.getKey(), "PAIRWISE45_Upgrade",
                        TestService.EXAMPLE2,
                        TestPriceModel.EXAMPLE_PERUNIT_WEEK_FREEP,
                        example2Service, supplierMarketplace);

        serviceSetup.registerCompatibleServices(supplierAdmin.getKey(),
                serviceDetails, paidService);

        setDateFactory("2013-05-13 20:00:00");

        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(subDetails, paidService);

        // // // 2. create upgraded service
        VOServiceDetails paidService2 = serviceSetup
                .createPublishAndActivateMarketableService(
                        supplierAdmin.getKey(), "PAIRWISE45_Upgrade2",
                        TestService.EXAMPLE2,
                        TestPriceModel.EXAMPLE_PERUNIT_WEEK_ROLES,
                        example2Service, supplierMarketplace);

        serviceSetup.registerCompatibleServices(supplierAdmin.getKey(),
                paidService, paidService2);

        setDateFactory("2013-05-14 20:00:00");

        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetails2 = subscrSetup
                .upgradeSubscription(upgradedSubDetails, paidService2);

        // user is assigned

        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        setDateFactory("2013-05-14 21:00:00");
        VORoleDefinition changedRole = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        upgradedSubDetails2 = subscrSetup.addUser(
                basicSetup.getSecondCustomerUser1(), changedRole,
                upgradedSubDetails2.getSubscriptionId());

        setDateFactory("2013-05-15 20:00:00");
        subscrSetup
                .unsubscribeToService(upgradedSubDetails.getSubscriptionId());

        resetCutOffDay(supplierAdmin.getKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_45", subDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_45", upgradedSubDetails2);

        BillingIntegrationTestBase.updateCustomerListForTests("PICT_TEST_45",
                customer);

    }

    public void createPictScenario44() throws Exception {

        setDateFactory("2013-05-07 20:00:00");

        // Create a supplier with a marketplace
        String supplierAdminId = "Pict44Supplier";
        VOOrganization supplier = orgSetup.createOrganization(
                basicSetup.getPlatformOperatorUserKey(), supplierAdminId,
                "Pict44SupplierOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                OrganizationRoleType.TECHNOLOGY_PROVIDER,
                OrganizationRoleType.SUPPLIER);
        VOUser supplierAdmin = orgSetup.getUser(supplierAdminId, true);
        paymentSetup.createPaymentForSupplier(
                basicSetup.getPlatformOperatorUserKey(),
                supplierAdmin.getKey(), supplier);

        // Create own technical service
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        serviceSetup
                .importTechnicalServices(BaseAdmUmTest.TECHNICAL_SERVICE_EXAMPLE2_XML);
        VOTechnicalService example2Service = serviceSetup
                .getTechnicalService(VOTechServiceFactory.TECH_SERVICE_EXAMPLE2_ID);

        setCutOffDay(supplierAdmin.getKey(), 2);

        // Create a customer
        String customerAdminId = "Pict44Customer";
        VOOrganization customer = orgSetup.registerCustomer(
                "Pict44CustomerOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                customerAdminId, supplierMarketplace.getMarketplaceId(),
                supplier.getOrganizationId());
        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);

        orgSetup.saveAllVats(supplierAdmin.getKey(),
                VOVatRateFactory.newVOVatRate(new BigDecimal("13.00")), //
                null, null);

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        supplierAdmin.getKey(), "PICT_TEST_44",
                        TestService.EXAMPLE2,
                        TestPriceModel.EXAMPLE_PICT44_UNIT_MONTH,
                        example2Service, supplierMarketplace);

        orgSetup.createMarketingPermission(supplierAdmin.getKey(),
                supplier.getOrganizationId(), example2Service);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "GUEST");

        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "PICT_TEST_44", serviceDetails, customerAdmin, role);

        // // price model change
        //
        setDateFactory("2013-05-10 09:30:00");
        VOPriceModel newSubPriceModel = VOPriceModelFactory
                .modifyPriceModelPeriodFee(subDetails.getPriceModel(),
                        new BigDecimal("445.00"));

        subscrSetup.savePriceModelForSubscription(supplierAdmin.getKey(),
                subDetails, newSubPriceModel, customer);

        // user is assigned

        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        setDateFactory("2013-05-10 10:00:00");
        VORoleDefinition changedRole = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        subDetails = subscrSetup.addUser(basicSetup.getSecondCustomerUser2(),
                changedRole, subDetails.getSubscriptionId());

        // Events
        container.login(supplierAdmin.getKey(), ROLE_TECHNOLOGY_MANAGER);
        subscrSetup.recordEventForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-05-15 12:00:00"),
                "FILE_DOWNLOAD", 35);

        // 1. create upgraded service
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOServiceDetails paidService = serviceSetup
                .createPublishAndActivateMarketableService(
                        supplierAdmin.getKey(), "PAIRWISE44_Upgrade",
                        TestService.EXAMPLE2,
                        TestPriceModel.EXAMPLE_RATA_WEEK_ROLES,
                        example2Service, supplierMarketplace);

        serviceSetup.registerCompatibleServices(supplierAdmin.getKey(),
                serviceDetails, paidService);

        setDateFactory("2013-05-16 20:00:00");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(subDetails, paidService);

        // user is assigned

        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        setDateFactory("2013-05-16 21:00:00");
        changedRole = VOServiceFactory.getRole(serviceDetails, "ADMIN");
        upgradedSubDetails = subscrSetup.addUser(
                basicSetup.getSecondCustomerUser1(), changedRole,
                upgradedSubDetails.getSubscriptionId());

        // // suspend/resume subscription several times by removing/restoring
        // // customer's payment types
        setDateFactory("2013-05-17 20:00:00");

        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);

        paymentSetup.deleteCustomerPaymentTypes(customer);

        setDateFactory("2013-05-27 08:00:00");

        paymentSetup.reassignCustomerPaymentTypes(customer);

        upgradedSubDetails = subscrSetup.getSubscriptionDetails(
                customerAdmin.getKey(), upgradedSubDetails.getSubscriptionId());

        // role change

        setDateFactory("2013-05-27 10:00:00");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        upgradedSubDetails = subscrSetup.modifyUserRole(upgradedSubDetails
                .getUsageLicenses().get(0), VOServiceFactory.getRole(
                paidService, "USER"), upgradedSubDetails.getSubscriptionId());

        setDateFactory("2013-05-27 11:00:00");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        upgradedSubDetails = subscrSetup.modifyUserRole(upgradedSubDetails
                .getUsageLicenses().get(0), VOServiceFactory.getRole(
                paidService, "ADMIN"), upgradedSubDetails.getSubscriptionId());

        // parameter change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        upgradedSubDetails = subscrSetup.modifyParameterForSubscription(
                upgradedSubDetails,
                DateTimeHandling.calculateMillis("2013-06-08 12:00:00"),
                "HAS_OPTIONS", "9");

        // parameter change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        upgradedSubDetails = subscrSetup.modifyParameterForSubscription(
                upgradedSubDetails,
                DateTimeHandling.calculateMillis("2013-06-09 12:00:00"),
                "HAS_OPTIONS", "10");

        // user is deassign
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);

        setDateFactory("2013-06-10 12:00:00");
        subscrSetup.revokeUser(basicSetup.getSecondCustomerUser1(),
                upgradedSubDetails.getSubscriptionId());

        // delete market service
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-05-17 13:00:00"));
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER);
        serviceSetup.deleteMarketableService(serviceDetails);

        resetCutOffDay(supplierAdmin.getKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_44", subDetails);

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_44", upgradedSubDetails);

        BillingIntegrationTestBase.updateCustomerListForTests("PICT_TEST_44",
                customer);

    }

    public void createPictScenario43() throws Exception {
        setDateFactory("2013-05-07 20:00:00");

        // Create a supplier with a marketplace
        String supplierAdminId = "Pict43Supplier";
        VOOrganization supplier = orgSetup.createOrganization(
                basicSetup.getPlatformOperatorUserKey(), supplierAdminId,
                "Pict43SupplierOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                OrganizationRoleType.TECHNOLOGY_PROVIDER,
                OrganizationRoleType.SUPPLIER);
        VOUser supplierAdmin = orgSetup.getUser(supplierAdminId, true);
        paymentSetup.createPaymentForSupplier(
                basicSetup.getPlatformOperatorUserKey(),
                supplierAdmin.getKey(), supplier);

        // Create own technical service
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        serviceSetup
                .importTechnicalServices(BaseAdmUmTest.TECHNICAL_SERVICE_EXAMPLE2_XML);
        VOTechnicalService example2Service = serviceSetup
                .getTechnicalService(VOTechServiceFactory.TECH_SERVICE_EXAMPLE2_ID);

        setCutOffDay(supplierAdmin.getKey(), 2);

        // Create a customer
        String customerAdminId = "Pict43Customer";
        VOOrganization customer = orgSetup.registerCustomer(
                "Pict43CustomerOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                customerAdminId, supplierMarketplace.getMarketplaceId(),
                supplier.getOrganizationId());
        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);

        orgSetup.saveAllVats(supplierAdmin.getKey(),
                VOVatRateFactory.newVOVatRate(new BigDecimal("13.00")), //
                null, null);

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        supplierAdmin.getKey(), "PICT_TEST_43",
                        TestService.EXAMPLE2, TestPriceModel.FREE,
                        example2Service, supplierMarketplace);

        orgSetup.createMarketingPermission(supplierAdmin.getKey(),
                supplier.getOrganizationId(), example2Service);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "GUEST");

        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "PICT_TEST_43", serviceDetails, customerAdmin, role);

        // user is assigned

        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        setDateFactory("2013-05-10 10:00:00");
        VORoleDefinition changedRole = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        subDetails = subscrSetup.addUser(basicSetup.getSecondCustomerUser2(),
                changedRole, subDetails.getSubscriptionId());

        // Events
        container.login(supplierAdmin.getKey(), ROLE_TECHNOLOGY_MANAGER);
        subscrSetup.recordEventForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-05-15 12:00:00"),
                "FILE_DOWNLOAD", 35);

        // 1. create upgraded service
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOServiceDetails paidService = serviceSetup
                .createPublishAndActivateMarketableService(
                        supplierAdmin.getKey(), "PAIRWISE43_Upgrade",
                        TestService.EXAMPLE2,
                        TestPriceModel.EXAMPLE_RATA_WEEK_ROLES,
                        example2Service, supplierMarketplace);

        serviceSetup.registerCompatibleServices(supplierAdmin.getKey(),
                serviceDetails, paidService);

        setDateFactory("2013-05-16 20:00:00");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(subDetails, paidService);

        // user is assigned

        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        setDateFactory("2013-05-16 21:00:00");
        changedRole = VOServiceFactory.getRole(serviceDetails, "ADMIN");
        upgradedSubDetails = subscrSetup.addUser(
                basicSetup.getSecondCustomerUser1(), changedRole,
                upgradedSubDetails.getSubscriptionId());

        // parameter change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        upgradedSubDetails = subscrSetup.modifyParameterForSubscription(
                upgradedSubDetails,
                DateTimeHandling.calculateMillis("2013-05-16 22:00:00"),
                "HAS_OPTIONS", "9");

        // 2. create upgraded service
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOServiceDetails paidServiceUnit = serviceSetup
                .createPublishAndActivateMarketableService(
                        supplierAdmin.getKey(), "PAIRWISE43_Upgrade2",
                        TestService.EXAMPLE2,
                        TestPriceModel.EXAMPLE_PERUNIT_WEEK_ROLES_EVENTS_FREEP,
                        example2Service, supplierMarketplace);

        serviceSetup.registerCompatibleServices(supplierAdmin.getKey(),
                paidService, paidServiceUnit);

        setDateFactory("2013-05-16 23:00:00");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetailsUnit = subscrSetup
                .upgradeSubscription(upgradedSubDetails, paidServiceUnit);

        // parameter change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        upgradedSubDetailsUnit = subscrSetup.modifyParameterForSubscription(
                upgradedSubDetailsUnit,
                DateTimeHandling.calculateMillis("2013-05-16 23:00:00"),
                "HAS_OPTIONS", "10");

        // user is deassign
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);

        setDateFactory("2013-05-16 23:10:00");
        subscrSetup.revokeUser(basicSetup.getSecondCustomerUser1(),
                upgradedSubDetailsUnit.getSubscriptionId());

        // user is assigned

        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        setDateFactory("2013-05-26 21:00:00");
        changedRole = VOServiceFactory.getRole(paidServiceUnit, "ADMIN");
        upgradedSubDetailsUnit = subscrSetup.addUser(
                basicSetup.getSecondCustomerUser1(), changedRole,
                upgradedSubDetailsUnit.getSubscriptionId());

        resetCutOffDay(supplierAdmin.getKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_43", subDetails);

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_43", upgradedSubDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_43", upgradedSubDetailsUnit);

        BillingIntegrationTestBase.updateCustomerListForTests("PICT_TEST_43",
                customer);

    }

    public void createPictScenario42() throws Exception {
        setDateFactory("2013-05-01 20:00:00");

        // Create a supplier with a marketplace
        String supplierAdminId = "Pict42Supplier";
        VOOrganization supplier = orgSetup.createOrganization(
                basicSetup.getPlatformOperatorUserKey(), supplierAdminId,
                "Pict42SupplierOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                OrganizationRoleType.TECHNOLOGY_PROVIDER,
                OrganizationRoleType.SUPPLIER);
        VOUser supplierAdmin = orgSetup.getUser(supplierAdminId, true);
        paymentSetup.createPaymentForSupplier(
                basicSetup.getPlatformOperatorUserKey(),
                supplierAdmin.getKey(), supplier);

        // Create own technical service
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        serviceSetup
                .importTechnicalServices(BaseAdmUmTest.TECHNICAL_SERVICE_EXAMPLE2_XML);
        VOTechnicalService example2Service = serviceSetup
                .getTechnicalService(VOTechServiceFactory.TECH_SERVICE_EXAMPLE2_ID);

        setCutOffDay(supplierAdmin.getKey(), 2);

        // Create a customer
        String customerAdminId = "Pict42Customer";
        VOOrganization customer = orgSetup.registerCustomer(
                "Pict42CustomerOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                customerAdminId, supplierMarketplace.getMarketplaceId(),
                supplier.getOrganizationId());
        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);

        orgSetup.saveAllVats(supplierAdmin.getKey(), VOVatRateFactory
                .newVOVatRate(new BigDecimal("13.00")), Arrays.asList(
                VOVatRateFactory.newVOCountryVatRate(new BigDecimal("20.00"),
                        TestOrganizationSetup.ORGANIZATION_DOMICILE_UK),
                VOVatRateFactory.newVOCountryVatRate(new BigDecimal("19.00"),
                        TestOrganizationSetup.ORGANIZATION_DOMICILE_DE)),
                Arrays.asList(VOVatRateFactory.newVOOrganizationVatRate(
                        new BigDecimal("5.00"), customer)));

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        supplierAdmin.getKey(), "PICT_TEST_42",
                        TestService.EXAMPLE2,
                        TestPriceModel.EXAMPLE_PICT42_UNIT_MONTH,
                        example2Service, supplierMarketplace);

        orgSetup.createMarketingPermission(supplierAdmin.getKey(),
                supplier.getOrganizationId(), example2Service);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "GUEST");

        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "PICT_TEST_42", serviceDetails, customerAdmin, role);

        // user is assigned

        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        setDateFactory("2013-05-10 10:00:00");
        VORoleDefinition changedRole = VOServiceFactory.getRole(serviceDetails,
                "GUEST");
        subDetails = subscrSetup.addUser(basicSetup.getSecondCustomerUser2(),
                changedRole, subDetails.getSubscriptionId());

        setDateFactory("2013-05-10 11:00:00");
        subDetails = subscrSetup.modifyUserRole(subDetails.getUsageLicenses()
                .get(0), VOServiceFactory.getRole(serviceDetails, "ADMIN"),
                subDetails.getSubscriptionId());

        setDateFactory("2013-05-10 12:00:00");
        subDetails = subscrSetup.modifyUserRole(subDetails.getUsageLicenses()
                .get(0), VOServiceFactory.getRole(serviceDetails, "USER"),
                subDetails.getSubscriptionId());

        // Events
        container.login(supplierAdmin.getKey(), ROLE_TECHNOLOGY_MANAGER);
        subscrSetup.recordEventForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-05-15 12:00:00"),
                "FILE_DOWNLOAD", 35);

        // 1. create upgraded service
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOServiceDetails free = serviceSetup
                .createPublishAndActivateMarketableService(
                        supplierAdmin.getKey(), "PAIRWISE42_Upgrade",
                        TestService.EXAMPLE2, TestPriceModel.FREE,
                        example2Service, supplierMarketplace);

        serviceSetup.registerCompatibleServices(supplierAdmin.getKey(),
                serviceDetails, free);

        setDateFactory("2013-05-16 20:00:00");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(subDetails, free);

        // user is assigned

        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        setDateFactory("2013-05-16 21:00:00");
        changedRole = VOServiceFactory.getRole(serviceDetails, "USER");
        upgradedSubDetails = subscrSetup.addUser(
                basicSetup.getSecondCustomerUser1(), changedRole,
                upgradedSubDetails.getSubscriptionId());

        // parameter change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        upgradedSubDetails = subscrSetup.modifyParameterForSubscription(
                upgradedSubDetails,
                DateTimeHandling.calculateMillis("2013-05-16 22:00:00"),
                "MAX_FOLDER_NUMBER", "220");

        // parameter change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        upgradedSubDetails = subscrSetup.modifyParameterForSubscription(
                upgradedSubDetails,
                DateTimeHandling.calculateMillis("2013-05-16 23:00:00"),
                "MAX_FOLDER_NUMBER", "300");

        // user is deassign
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);

        setDateFactory("2013-05-16 23:10:00");
        subscrSetup.revokeUser(basicSetup.getSecondCustomerUser1(),
                upgradedSubDetails.getSubscriptionId());

        // user is assigned

        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        setDateFactory("2013-05-26 21:00:00");
        changedRole = VOServiceFactory.getRole(free, "ADMIN");
        upgradedSubDetails = subscrSetup.addUser(
                basicSetup.getSecondCustomerUser1(), changedRole,
                upgradedSubDetails.getSubscriptionId());

        upgradedSubDetails = subscrSetup.getSubscriptionDetails(
                customerAdmin.getKey(), upgradedSubDetails.getSubscriptionId());

        // // suspend/resume subscription several times by removing/restoring
        // // customer's payment types
        setDateFactory("2013-05-26 22:00:00");

        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);

        paymentSetup.deleteCustomerPaymentTypes(customer);

        setDateFactory("2013-06-09 08:00:00");

        paymentSetup.reassignCustomerPaymentTypes(customer);

        resetCutOffDay(supplierAdmin.getKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_42", subDetails);

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_42", upgradedSubDetails);

        BillingIntegrationTestBase.updateCustomerListForTests("PICT_TEST_42",
                customer);

    }

    public void createPictScenario41() throws Exception {
        setDateFactory("2013-07-10 20:00:00");

        // Create a supplier with a marketplace
        String supplierAdminId = "Pict41Supplier";
        VOOrganization supplier = orgSetup.createOrganization(
                basicSetup.getPlatformOperatorUserKey(), supplierAdminId,
                "Pict41SupplierOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                OrganizationRoleType.TECHNOLOGY_PROVIDER,
                OrganizationRoleType.SUPPLIER);
        VOUser supplierAdmin = orgSetup.getUser(supplierAdminId, true);
        paymentSetup.createPaymentForSupplier(
                basicSetup.getPlatformOperatorUserKey(),
                supplierAdmin.getKey(), supplier);

        // Create own technical service
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        serviceSetup
                .importTechnicalServices(BaseAdmUmTest.TECHNICAL_SERVICE_EXAMPLE2_XML);
        VOTechnicalService example2Service = serviceSetup
                .getTechnicalService(VOTechServiceFactory.TECH_SERVICE_EXAMPLE2_ID);

        setCutOffDay(supplierAdmin.getKey(), 3);

        // Create a customer
        String customerAdminId = "Pict41Customer";
        VOOrganization customer = orgSetup.registerCustomer(
                "Pict41CustomerOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                customerAdminId, supplierMarketplace.getMarketplaceId(),
                supplier.getOrganizationId());
        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);

        orgSetup.updateCustomerDiscount(customer, new BigDecimal("25.00"),
                DateTimeHandling.calculateMillis("2013-07-08 00:00:00"),
                DateTimeHandling.calculateMillis("2013-07-09 01:00:00"));
        //
        orgSetup.saveAllVats(supplierAdmin.getKey(), VOVatRateFactory
                .newVOVatRate(new BigDecimal("13.00")), Arrays.asList(
                VOVatRateFactory.newVOCountryVatRate(new BigDecimal("20.00"),
                        TestOrganizationSetup.ORGANIZATION_DOMICILE_UK),
                VOVatRateFactory.newVOCountryVatRate(new BigDecimal("19.00"),
                        TestOrganizationSetup.ORGANIZATION_DOMICILE_DE)),
                Arrays.asList(VOVatRateFactory.newVOOrganizationVatRate(
                        new BigDecimal("5.00"), customer)));

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        supplierAdmin.getKey(), "PICT_TEST_41",
                        TestService.EXAMPLE2, TestPriceModel.FREE,
                        example2Service, supplierMarketplace);

        orgSetup.createMarketingPermission(supplierAdmin.getKey(),
                supplier.getOrganizationId(), example2Service);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "GUEST");

        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "PICT_TEST_41", serviceDetails, customerAdmin, role);

        // role change

        setDateFactory("2013-07-10 21:00:00");
        subDetails = subscrSetup.modifyUserRole(subDetails.getUsageLicenses()
                .get(0), VOServiceFactory.getRole(serviceDetails, "ADMIN"),
                subDetails.getSubscriptionId());

        // 1. create upgraded service
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOServiceDetails paidService = serviceSetup
                .createPublishAndActivateMarketableService(
                        supplierAdmin.getKey(), "PAIRWISE41_Upgrade",
                        TestService.EXAMPLE2,
                        TestPriceModel.EXAMPLE_PICT41_RATA_HOUR,
                        example2Service, supplierMarketplace);

        serviceSetup.registerCompatibleServices(supplierAdmin.getKey(),
                serviceDetails, paidService);

        setDateFactory("2013-07-10 22:00:00");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(subDetails, paidService);

        // parameter change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        upgradedSubDetails = subscrSetup.modifyParameterForSubscription(
                upgradedSubDetails,
                DateTimeHandling.calculateMillis("2013-07-11 22:00:00"),
                "LONG_NUMBER", "500");

        // // suspend/resume subscription several times by removing/restoring
        // // customer's payment types
        setDateFactory("2013-07-12 22:00:00");

        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);

        paymentSetup.deleteCustomerPaymentTypes(customer);

        setDateFactory("2013-07-12 22:05:00");

        paymentSetup.reassignCustomerPaymentTypes(customer);

        upgradedSubDetails = subscrSetup.getSubscriptionDetails(
                customerAdmin.getKey(), upgradedSubDetails.getSubscriptionId());

        setDateFactory("2013-07-12 23:00:00");
        subscrSetup
                .unsubscribeToService(upgradedSubDetails.getSubscriptionId());

        resetCutOffDay(supplierAdmin.getKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_41", subDetails);

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_41", upgradedSubDetails);

        BillingIntegrationTestBase.updateCustomerListForTests("PICT_TEST_41",
                customer);

    }

    public void createPictScenario40() throws Exception {
        setDateFactory("2013-08-02 10:00:00");

        // Create a supplier with a marketplace
        String supplierAdminId = "Pict40Supplier";
        VOOrganization supplier = orgSetup.createOrganization(
                basicSetup.getPlatformOperatorUserKey(), supplierAdminId,
                "Pict40SupplierOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                OrganizationRoleType.TECHNOLOGY_PROVIDER,
                OrganizationRoleType.SUPPLIER);
        VOUser supplierAdmin = orgSetup.getUser(supplierAdminId, true);
        paymentSetup.createPaymentForSupplier(
                basicSetup.getPlatformOperatorUserKey(),
                supplierAdmin.getKey(), supplier);

        // Create own technical service
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        serviceSetup
                .importTechnicalServices(BaseAdmUmTest.TECHNICAL_SERVICE_EXAMPLE2_XML);
        VOTechnicalService example2Service = serviceSetup
                .getTechnicalService(VOTechServiceFactory.TECH_SERVICE_EXAMPLE2_ID);

        setCutOffDay(supplierAdmin.getKey(), 3);

        // Create a customer
        String customerAdminId = "Pict40Customer";
        VOOrganization customer = orgSetup.registerCustomer(
                "Pict40CustomerOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                customerAdminId, supplierMarketplace.getMarketplaceId(),
                supplier.getOrganizationId());
        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);

        // Vat
        orgSetup.saveAllVats(supplierAdmin.getKey(), VOVatRateFactory
                .newVOVatRate(new BigDecimal("13.00")), Arrays.asList(
                VOVatRateFactory.newVOCountryVatRate(new BigDecimal("20.00"),
                        TestOrganizationSetup.ORGANIZATION_DOMICILE_UK),
                VOVatRateFactory.newVOCountryVatRate(new BigDecimal("19.00"),
                        TestOrganizationSetup.ORGANIZATION_DOMICILE_DE)),
                Arrays.asList(VOVatRateFactory.newVOOrganizationVatRate(
                        new BigDecimal("5.00"), customer)));

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        supplierAdmin.getKey(), "PICT_TEST_40",
                        TestService.EXAMPLE2,
                        TestPriceModel.EXAMPLE_PICT40_UNIT_WEEKS,
                        example2Service, supplierMarketplace);

        orgSetup.createMarketingPermission(supplierAdmin.getKey(),
                supplier.getOrganizationId(), example2Service);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "GUEST");
        // subscribe
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "PICT_TEST_40", serviceDetails, customerAdmin, role);

        // user is assigned
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        setDateFactory("2013-08-04 10:00:00");
        VORoleDefinition changedRole = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        subDetails = subscrSetup.addUser(basicSetup.getSecondCustomerUser1(),
                changedRole, subDetails.getSubscriptionId());

        // user is deassigned
        setDateFactory("2013-08-04 10:59:59");
        subDetails = subscrSetup.revokeUser(
                basicSetup.getSecondCustomerUser1(),
                subDetails.getSubscriptionId());

        // role change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        setDateFactory("2013-08-04 20:00:00");
        subDetails = subscrSetup.modifyUserRole(subDetails.getUsageLicenses()
                .get(0), VOServiceFactory.getRole(serviceDetails, "USER"),
                subDetails.getSubscriptionId());

        // role change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        setDateFactory("2013-08-04 21:00:00");
        subDetails = subscrSetup.modifyUserRole(subDetails.getUsageLicenses()
                .get(0), VOServiceFactory.getRole(serviceDetails, "GUEST"),
                subDetails.getSubscriptionId());

        // user is assigned
        setDateFactory("2013-08-04 23:00:00");
        subDetails = subscrSetup.addUser(basicSetup.getSecondCustomerUser1(),
                changedRole, subDetails.getSubscriptionId());

        // // suspend/resume subscription several times by removing/restoring
        // // customer's payment types
        setDateFactory("2013-08-04 23:10:00");

        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);

        paymentSetup.deleteCustomerPaymentTypes(customer);

        setDateFactory("2013-08-05 08:05:00");

        paymentSetup.reassignCustomerPaymentTypes(customer);

        subDetails = subscrSetup.getSubscriptionDetails(customerAdmin.getKey(),
                subDetails.getSubscriptionId());

        // price model change

        long priceModelChangeTime = DateTimeHandling
                .calculateMillis("2013-08-05 08:10:00");
        BillingIntegrationTestBase.setDateFactoryInstance(priceModelChangeTime);
        VOPriceModel newSubPriceModel = VOPriceModelFactory
                .modifyPriceModelPeriodFee(subDetails.getPriceModel(),
                        new BigDecimal("445.00"));

        subscrSetup.savePriceModelForSubscription(supplierAdmin.getKey(),
                subDetails, newSubPriceModel, customer);

        // 1. create upgraded service
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOServiceDetails free = serviceSetup
                .createPublishAndActivateMarketableService(
                        supplierAdmin.getKey(), "PAIRWISE40_Upgrade",
                        TestService.EXAMPLE2, TestPriceModel.FREE,
                        example2Service, supplierMarketplace);

        serviceSetup.registerCompatibleServices(supplierAdmin.getKey(),
                serviceDetails, free);

        setDateFactory("2013-08-10 10:00:00");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(subDetails, free);

        // delete market service
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-08-10 21:10:00"));
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER);
        serviceSetup.deleteMarketableService(serviceDetails);

        // parameter change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        upgradedSubDetails = subscrSetup.modifyParameterForSubscription(
                upgradedSubDetails,
                DateTimeHandling.calculateMillis("2013-08-10 22:00:00"),
                "LONG_NUMBER", "500");

        resetCutOffDay(supplierAdmin.getKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_40", subDetails);

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_40", upgradedSubDetails);

        BillingIntegrationTestBase.updateCustomerListForTests("PICT_TEST_40",
                customer);

    }

    public void createPictScenario39() throws Exception {
        setDateFactory("2013-08-02 10:00:00");

        // Create a supplier with a marketplace
        String supplierAdminId = "Pict39Supplier";
        VOOrganization supplier = orgSetup.createOrganization(
                basicSetup.getPlatformOperatorUserKey(), supplierAdminId,
                "Pict39SupplierOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                OrganizationRoleType.TECHNOLOGY_PROVIDER,
                OrganizationRoleType.SUPPLIER);
        VOUser supplierAdmin = orgSetup.getUser(supplierAdminId, true);
        paymentSetup.createPaymentForSupplier(
                basicSetup.getPlatformOperatorUserKey(),
                supplierAdmin.getKey(), supplier);

        // Create own technical service
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        serviceSetup
                .importTechnicalServices(BaseAdmUmTest.TECHNICAL_SERVICE_EXAMPLE2_XML);
        VOTechnicalService example2Service = serviceSetup
                .getTechnicalService(VOTechServiceFactory.TECH_SERVICE_EXAMPLE2_ID);

        setCutOffDay(supplierAdmin.getKey(), 1);

        // Create a customer
        String customerAdminId = "Pict39Customer";
        VOOrganization customer = orgSetup.registerCustomer(
                "Pict39CustomerOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                customerAdminId, supplierMarketplace.getMarketplaceId(),
                supplier.getOrganizationId());
        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);

        orgSetup.saveAllVats(supplierAdmin.getKey(),
                VOVatRateFactory.newVOVatRate(new BigDecimal("13.00")), //
                null, null);

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        supplierAdmin.getKey(), "PICT_TEST_39",
                        TestService.EXAMPLE2, TestPriceModel.FREE,
                        example2Service, supplierMarketplace);

        orgSetup.createMarketingPermission(supplierAdmin.getKey(),
                supplier.getOrganizationId(), example2Service);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "GUEST");
        // subscribe
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "PICT_TEST_39", serviceDetails, customerAdmin, role);

        // change sunbscriptionID
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-08-04 09:00:00"));
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        subDetails.setSubscriptionId("PICT_TEST_39" + "_SubID2");
        subDetails = subscrSetup.modifySubscription(subDetails, null);

        // user is assigned
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        setDateFactory("2013-08-04 10:00:00");
        VORoleDefinition changedRole = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        subDetails = subscrSetup.addUser(basicSetup.getSecondCustomerUser1(),
                changedRole, subDetails.getSubscriptionId());

        // user is deassigned
        setDateFactory("2013-08-04 10:59:59");
        subDetails = subscrSetup.revokeUser(
                basicSetup.getSecondCustomerUser1(),
                subDetails.getSubscriptionId());

        // role change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        setDateFactory("2013-08-04 20:00:00");
        subDetails = subscrSetup.modifyUserRole(subDetails.getUsageLicenses()
                .get(0), VOServiceFactory.getRole(serviceDetails, "USER"),
                subDetails.getSubscriptionId());

        // role change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        setDateFactory("2013-08-04 21:00:00");
        subDetails = subscrSetup.modifyUserRole(subDetails.getUsageLicenses()
                .get(0), VOServiceFactory.getRole(serviceDetails, "GUEST"),
                subDetails.getSubscriptionId());

        // user is assigned
        setDateFactory("2013-08-04 23:00:00");
        subDetails = subscrSetup.addUser(basicSetup.getSecondCustomerUser1(),
                changedRole, subDetails.getSubscriptionId());

        // // suspend/resume subscription several times by removing/restoring
        // // customer's payment types
        setDateFactory("2013-08-04 23:10:00");

        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);

        paymentSetup.deleteCustomerPaymentTypes(customer);

        setDateFactory("2013-08-05 08:05:00");

        paymentSetup.reassignCustomerPaymentTypes(customer);

        subDetails = subscrSetup.getSubscriptionDetails(customerAdmin.getKey(),
                subDetails.getSubscriptionId());

        // parameter change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        subDetails = subscrSetup.modifyParameterForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-08-10 22:00:00"),
                "LONG_NUMBER", "500");

        resetCutOffDay(supplierAdmin.getKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_39", subDetails);

        BillingIntegrationTestBase.updateCustomerListForTests("PICT_TEST_39",
                customer);

    }

    public void createPictScenario38() throws Exception {
        setDateFactory("2013-10-02 10:00:00");

        // Create a supplier with a marketplace
        String supplierAdminId = "Pict38Supplier";
        VOOrganization supplier = orgSetup.createOrganization(
                basicSetup.getPlatformOperatorUserKey(), supplierAdminId,
                "Pict38SupplierOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                OrganizationRoleType.TECHNOLOGY_PROVIDER,
                OrganizationRoleType.SUPPLIER);
        VOUser supplierAdmin = orgSetup.getUser(supplierAdminId, true);
        paymentSetup.createPaymentForSupplier(
                basicSetup.getPlatformOperatorUserKey(),
                supplierAdmin.getKey(), supplier);

        // Create own technical service
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        serviceSetup
                .importTechnicalServices(BaseAdmUmTest.TECHNICAL_SERVICE_EXAMPLE2_XML);
        VOTechnicalService example2Service = serviceSetup
                .getTechnicalService(VOTechServiceFactory.TECH_SERVICE_EXAMPLE2_ID);

        setCutOffDay(supplierAdmin.getKey(), 1);

        // Create a customer
        String customerAdminId = "Pict38Customer";
        VOOrganization customer = orgSetup.registerCustomer(
                "Pict38CustomerOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                customerAdminId, supplierMarketplace.getMarketplaceId(),
                supplier.getOrganizationId());
        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);
        // vat
        orgSetup.saveAllVats(supplierAdmin.getKey(),
                VOVatRateFactory.newVOVatRate(new BigDecimal("13.00")), //
                null, null);

        orgSetup.updateCustomerDiscount(customer, new BigDecimal("25.00"),
                DateTimeHandling.calculateMillis("2013-10-02 00:00:00"),
                DateTimeHandling.calculateMillis("2013-11-10 01:00:00"));

        orgSetup.createMarketingPermission(supplierAdmin.getKey(),
                supplier.getOrganizationId(), example2Service);

        // price model for customer
        VOServiceDetails serviceTemplate = serviceSetup
                .createAndPublishMarketableService(supplierAdmin.getKey(),
                        "PICT_TEST_38", TestService.EXAMPLE2,
                        TestPriceModel.EXAMPLE_PICT38_UNIT_DAYS,
                        example2Service, supplierMarketplace);
        VOServiceDetails serviceDetails = serviceSetup
                .savePriceModelForCustomer(serviceTemplate,
                        TestPriceModel.EXAMPLE_PICT38_UNIT_DAYS, customer);
        serviceDetails = serviceSetup.activateMarketableService(serviceDetails);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "GUEST");
        // subscribe
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "PICT_TEST_38", serviceDetails, customerAdmin, role);

        // Events during free
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        subscrSetup.recordEventForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-10-03 10:00:30"),
                "FILE_DOWNLOAD", 15);
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);

        // user is assigned during free period
        setDateFactory("2013-10-03 10:00:00");
        subDetails = subscrSetup.addUser(basicSetup.getSecondCustomerUser1(),
                role, subDetails.getSubscriptionId());

        // user is revoked during free period
        setDateFactory("2013-10-03 10:10:00");
        subscrSetup.revokeUser(basicSetup.getSecondCustomerUser1(),
                subDetails.getSubscriptionId());

        // role change during free period
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        setDateFactory("2013-10-04 20:00:00");
        subDetails = subscrSetup.modifyUserRole(subDetails.getUsageLicenses()
                .get(0), VOServiceFactory.getRole(serviceDetails, "USER"),
                subDetails.getSubscriptionId());

        // role change during free period
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        setDateFactory("2013-10-05 21:00:00");
        subDetails = subscrSetup.modifyUserRole(subDetails.getUsageLicenses()
                .get(0), VOServiceFactory.getRole(serviceDetails, "GUEST"),
                subDetails.getSubscriptionId());

        // Events
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        subscrSetup.recordEventForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-10-10 09:00:00"),
                "FILE_UPLOAD", 15);
        // 1. upgrade
        VOServiceDetails paidTemplate = serviceSetup
                .createAndPublishMarketableService(supplierAdmin.getKey(),
                        "PAIRWISE38_Upgrade", TestService.EXAMPLE2,
                        TestPriceModel.EXAMPLE_PICT38_RATA_HOUR,
                        example2Service, supplierMarketplace);
        serviceSetup.registerCompatibleServices(supplierAdmin.getKey(),
                serviceTemplate, paidTemplate);
        VOServiceDetails paid = serviceSetup
                .savePriceModelForCustomer(paidTemplate,
                        TestPriceModel.EXAMPLE_PICT38_RATA_HOUR, customer);
        paid = serviceSetup.activateMarketableService(paid);

        setDateFactory("2013-10-10 10:00:00");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(subDetails, paid);

        // Events
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        subscrSetup.recordEventForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-10-10 10:20:00"),
                "FOLDER_NEW", 15);
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);

        // // suspend/resume subscription several times by removing/restoring
        // // customer's payment types
        setDateFactory("2013-10-14 23:10:00");

        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);

        paymentSetup.deleteCustomerPaymentTypes(customer);

        setDateFactory("2013-10-14 23:15:00");

        paymentSetup.reassignCustomerPaymentTypes(customer);

        upgradedSubDetails = subscrSetup.getSubscriptionDetails(
                customerAdmin.getKey(), upgradedSubDetails.getSubscriptionId());

        // parameter change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        upgradedSubDetails = subscrSetup.modifyParameterForSubscription(
                upgradedSubDetails,
                DateTimeHandling.calculateMillis("2013-10-27 01:10:00"),
                "LONG_NUMBER", "500");

        // parameter change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        upgradedSubDetails = subscrSetup.modifyParameterForSubscription(
                upgradedSubDetails,
                DateTimeHandling.calculateMillis("2013-10-27 07:55:00"),
                "LONG_NUMBER", "400");

        // price model change

        setDateFactory("2013-10-27 07:56:00");
        VOPriceModel newSubPriceModel = VOPriceModelFactory
                .modifyPriceModelPeriodFee(upgradedSubDetails.getPriceModel(),
                        new BigDecimal("0.02"));

        subscrSetup.savePriceModelForSubscription(supplierAdmin.getKey(),
                upgradedSubDetails, newSubPriceModel, customer);

        setDateFactory("2013-10-27 20:00:00");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        subscrSetup
                .unsubscribeToService(upgradedSubDetails.getSubscriptionId());

        // delete market service
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-10-27 22:10:00"));
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER);
        serviceSetup.deleteMarketableService(paid);

        resetCutOffDay(supplierAdmin.getKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_38", subDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_38", upgradedSubDetails);

        BillingIntegrationTestBase.updateCustomerListForTests("PICT_TEST_38",
                customer);

    }

    public void createPictScenario37() throws Exception {
        setDateFactory("2013-10-02 10:00:00");

        // Create a supplier with a marketplace
        String supplierAdminId = "Pict37Supplier";
        VOOrganization supplier = orgSetup.createOrganization(
                basicSetup.getPlatformOperatorUserKey(), supplierAdminId,
                "Pict37SupplierOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                OrganizationRoleType.TECHNOLOGY_PROVIDER,
                OrganizationRoleType.SUPPLIER);
        VOUser supplierAdmin = orgSetup.getUser(supplierAdminId, true);
        paymentSetup.createPaymentForSupplier(
                basicSetup.getPlatformOperatorUserKey(),
                supplierAdmin.getKey(), supplier);

        // Create own technical service
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        serviceSetup
                .importTechnicalServices(BaseAdmUmTest.TECHNICAL_SERVICE_EXAMPLE2_XML);
        VOTechnicalService example2Service = serviceSetup
                .getTechnicalService(VOTechServiceFactory.TECH_SERVICE_EXAMPLE2_ID);

        setCutOffDay(supplierAdmin.getKey(), 1);

        // Create a customer
        String customerAdminId = "Pict37Customer";
        VOOrganization customer = orgSetup.registerCustomer(
                "Pict37CustomerOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                customerAdminId, supplierMarketplace.getMarketplaceId(),
                supplier.getOrganizationId());
        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);

        orgSetup.updateCustomerDiscount(customer, new BigDecimal("25.00"),
                DateTimeHandling.calculateMillis("2013-10-02 00:00:00"),
                DateTimeHandling.calculateMillis("2013-11-10 01:00:00"));

        orgSetup.createMarketingPermission(supplierAdmin.getKey(),
                supplier.getOrganizationId(), example2Service);

        // price model for customer
        VOServiceDetails serviceTemplate = serviceSetup
                .createAndPublishMarketableService(supplierAdmin.getKey(),
                        "PICT_TEST_37", TestService.EXAMPLE2,
                        TestPriceModel.EXAMPLE_PICT37_RATA_MONTH,
                        example2Service, supplierMarketplace);
        VOServiceDetails serviceDetails = serviceSetup
                .savePriceModelForCustomer(serviceTemplate,
                        TestPriceModel.EXAMPLE_PICT37_RATA_MONTH, customer);
        serviceDetails = serviceSetup.activateMarketableService(serviceDetails);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "GUEST");
        // subscribe
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "PICT_TEST_37", serviceDetails, customerAdmin, role);

        // change sunbscriptionID
        setDateFactory("2013-10-03 07:30:00");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        subDetails.setSubscriptionId("PICT_TEST_37" + "_SubID2");
        subDetails = subscrSetup.modifySubscription(subDetails, null);

        // price model change

        setDateFactory("2013-10-03 08:00:30");

        VOPriceModel newSubPriceModel = VOPriceModelFactory
                .modifyPriceModelPeriodFee(subDetails.getPriceModel(),
                        new BigDecimal("50.00"));

        subscrSetup.savePriceModelForSubscription(supplierAdmin.getKey(),
                subDetails, newSubPriceModel, customer);

        // Events
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        subscrSetup.recordEventForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-10-03 10:00:30"),
                "FILE_DOWNLOAD", 15);
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);

        // user is assigned
        setDateFactory("2013-10-03 10:00:00");
        subDetails = subscrSetup.addUser(basicSetup.getSecondCustomerUser1(),
                role, subDetails.getSubscriptionId());

        // user is revoked
        setDateFactory("2013-10-03 10:10:00");
        subscrSetup.revokeUser(basicSetup.getSecondCustomerUser1(),
                subDetails.getSubscriptionId());

        // role change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        setDateFactory("2013-10-04 20:00:00");
        subDetails = subscrSetup.modifyUserRole(subDetails.getUsageLicenses()
                .get(0), VOServiceFactory.getRole(serviceDetails, "USER"),
                subDetails.getSubscriptionId());

        // role change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        setDateFactory("2013-10-05 21:00:00");
        subDetails = subscrSetup.modifyUserRole(subDetails.getUsageLicenses()
                .get(0), VOServiceFactory.getRole(serviceDetails, "GUEST"),
                subDetails.getSubscriptionId());

        // Events
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        subscrSetup.recordEventForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-10-10 09:00:00"),
                "FILE_UPLOAD", 15);

        // upgrade service
        // /////////////////
        VOServiceDetails freeTemplate = serviceSetup
                .createAndPublishMarketableService(supplierAdmin.getKey(),
                        "PAIRWISE37_Upgrade", TestService.EXAMPLE2,
                        TestPriceModel.FREE, example2Service,
                        supplierMarketplace);
        serviceSetup.registerCompatibleServices(supplierAdmin.getKey(),
                serviceTemplate, freeTemplate);
        VOServiceDetails free = serviceSetup.savePriceModelForCustomer(
                freeTemplate, TestPriceModel.FREE, customer);
        free = serviceSetup.activateMarketableService(free);

        setDateFactory("2013-10-10 10:00:00");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(subDetails, free);

        // Events
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        subscrSetup.recordEventForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-10-10 10:20:00"),
                "FOLDER_NEW", 15);
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);

        // parameter change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        upgradedSubDetails = subscrSetup.modifyParameterForSubscription(
                upgradedSubDetails,
                DateTimeHandling.calculateMillis("2013-10-27 01:10:00"),
                "LONG_NUMBER", "500");

        // parameter change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        upgradedSubDetails = subscrSetup.modifyParameterForSubscription(
                upgradedSubDetails,
                DateTimeHandling.calculateMillis("2013-10-27 07:55:00"),
                "LONG_NUMBER", "400");

        // role change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        setDateFactory("2013-11-05 21:00:00");
        subDetails = subscrSetup.modifyUserRole(subDetails.getUsageLicenses()
                .get(0), VOServiceFactory.getRole(serviceDetails, "ADMIN"),
                subDetails.getSubscriptionId());

        // unsubscribe
        setDateFactory("2013-11-27 20:00:00");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        subscrSetup
                .unsubscribeToService(upgradedSubDetails.getSubscriptionId());

        // delete market service
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-10-27 22:10:00"));
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER);
        serviceSetup.deleteMarketableService(free);

        resetCutOffDay(supplierAdmin.getKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_37", subDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_37", upgradedSubDetails);

        BillingIntegrationTestBase.updateCustomerListForTests("PICT_TEST_37",
                customer);

    }

    public void createPictScenario36() throws Exception {
        setDateFactory("2013-07-08 10:00:00");

        // Create a supplier with a marketplace
        String supplierAdminId = "Pict36Supplier";
        VOOrganization supplier = orgSetup.createOrganization(
                basicSetup.getPlatformOperatorUserKey(), supplierAdminId,
                "Pict36SupplierOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                OrganizationRoleType.TECHNOLOGY_PROVIDER,
                OrganizationRoleType.SUPPLIER);
        VOUser supplierAdmin = orgSetup.getUser(supplierAdminId, true);
        paymentSetup.createPaymentForSupplier(
                basicSetup.getPlatformOperatorUserKey(),
                supplierAdmin.getKey(), supplier);

        // Create own technical service
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        serviceSetup
                .importTechnicalServices(BaseAdmUmTest.TECHNICAL_SERVICE_EXAMPLE2_XML);
        VOTechnicalService example2Service = serviceSetup
                .getTechnicalService(VOTechServiceFactory.TECH_SERVICE_EXAMPLE2_ID);

        setCutOffDay(supplierAdmin.getKey(), 1);

        // Create a customer
        String customerAdminId = "Pict36Customer";
        VOOrganization customer = orgSetup.registerCustomer(
                "Pict36CustomerOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                customerAdminId, supplierMarketplace.getMarketplaceId(),
                supplier.getOrganizationId());
        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);

        orgSetup.createMarketingPermission(supplierAdmin.getKey(),
                supplier.getOrganizationId(), example2Service);

        // price model for customer
        VOServiceDetails serviceTemplate = serviceSetup
                .createAndPublishMarketableService(supplierAdmin.getKey(),
                        "PICT_TEST_36", TestService.EXAMPLE2,
                        TestPriceModel.EXAMPLE_PICT36_RATA_WEEKS,
                        example2Service, supplierMarketplace);
        VOServiceDetails serviceDetails = serviceSetup
                .savePriceModelForCustomer(serviceTemplate,
                        TestPriceModel.EXAMPLE_PICT36_RATA_WEEKS, customer);
        serviceDetails = serviceSetup.activateMarketableService(serviceDetails);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "GUEST");
        // subscribe
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "PICT_TEST_36", serviceDetails, customerAdmin, role);

        // change subscriptionID
        setDateFactory("2013-07-08 10:10:00");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        subDetails.setSubscriptionId("PICT_TEST_36" + "_SubID2");
        subDetails = subscrSetup.modifySubscription(subDetails, null);

        // Events
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        subscrSetup.recordEventForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-07-08 10:20:00"),
                "FILE_DOWNLOAD", 15);
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);

        // upgrade service
        // /////////////////
        VOServiceDetails unitTemplate = serviceSetup
                .createAndPublishMarketableService(supplierAdmin.getKey(),
                        "PAIRWISE36_Upgrade", TestService.EXAMPLE2,
                        TestPriceModel.EXAMPLE_PICT36_UNIT_WEEKS,
                        example2Service, supplierMarketplace);
        serviceSetup.registerCompatibleServices(supplierAdmin.getKey(),
                serviceTemplate, unitTemplate);
        VOServiceDetails unitW = serviceSetup.savePriceModelForCustomer(
                unitTemplate, TestPriceModel.EXAMPLE_PICT36_UNIT_WEEKS,
                customer);
        unitW = serviceSetup.activateMarketableService(unitW);

        setDateFactory("2013-07-15 02:00:00");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(subDetails, unitW);

        // parameter change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        upgradedSubDetails = subscrSetup.modifyParameterForSubscription(
                upgradedSubDetails,
                DateTimeHandling.calculateMillis("2013-07-15 02:10:00"),
                "MAX_FOLDER_NUMBER", "50");

        // parameter change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        upgradedSubDetails = subscrSetup.modifyParameterForSubscription(
                upgradedSubDetails,
                DateTimeHandling.calculateMillis("2013-07-16 02:10:00"),
                "MAX_FOLDER_NUMBER", "60");

        // Events
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        subscrSetup.recordEventForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-07-28 01:00:00"),
                "FOLDER_NEW", 15);
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);

        // suspend/resume subscription several times by removing/restoring
        // customer's payment types
        long suspResTime = DateTimeHandling
                .calculateMillis("2013-07-27 22:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(suspResTime);

        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);

        paymentSetup.deleteCustomerPaymentTypes(customer);

        long reassignResTime = DateTimeHandling
                .calculateMillis("2013-07-27 22:01:00");
        BillingIntegrationTestBase.setDateFactoryInstance(reassignResTime);
        paymentSetup.reassignCustomerPaymentTypes(customer);

        role = VOServiceFactory.getRole(serviceDetails, "USER");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        // user is assigned
        setDateFactory("2013-07-27 22:02:00");
        subDetails = subscrSetup.addUser(basicSetup.getSecondCustomerUser1(),
                role, subDetails.getSubscriptionId());

        // role change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        setDateFactory("2013-07-27 22:03:00");
        subDetails = subscrSetup.modifyUserRole(subDetails.getUsageLicenses()
                .get(0), VOServiceFactory.getRole(serviceDetails, "ADMIN"),
                subDetails.getSubscriptionId());

        // user is revoked
        setDateFactory("2013-07-27 22:10:00");
        subscrSetup.revokeUser(basicSetup.getSecondCustomerUser1(),
                subDetails.getSubscriptionId());

        // role change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        setDateFactory("2013-07-27 23:01:00");
        subDetails = subscrSetup.modifyUserRole(subDetails.getUsageLicenses()
                .get(0), VOServiceFactory.getRole(serviceDetails, "GUEST"),
                subDetails.getSubscriptionId());

        // unsubscribe

        setDateFactory("2013-07-29 02:00:00");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        subscrSetup
                .unsubscribeToService(upgradedSubDetails.getSubscriptionId());

        resetCutOffDay(supplierAdmin.getKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_36", subDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_36", upgradedSubDetails);

        BillingIntegrationTestBase.updateCustomerListForTests("PICT_TEST_36",
                customer);

    }

    public void createPictScenario49_5() throws Exception {
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-06-05 20:00:00"));

        // Create a supplier with a marketplace
        String supplierAdminId = "Pict49_5Supplier";
        VOOrganization supplier = orgSetup.createOrganization(
                basicSetup.getPlatformOperatorUserKey(), supplierAdminId,
                "Pict49_5SupplierOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                OrganizationRoleType.TECHNOLOGY_PROVIDER,
                OrganizationRoleType.SUPPLIER);
        VOUser supplierAdmin = orgSetup.getUser(supplierAdminId, true);
        paymentSetup.createPaymentForSupplier(
                basicSetup.getPlatformOperatorUserKey(),
                supplierAdmin.getKey(), supplier);

        setCutOffDay(supplierAdmin.getKey(), 2);

        // Create a customer
        String customerAdminId = "Pict49_5Customer";
        VOOrganization customer = orgSetup.registerCustomer(
                "Pict49_5CustomerOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                customerAdminId, supplierMarketplace.getMarketplaceId(),
                supplier.getOrganizationId());
        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);

        orgSetup.saveAllVats(supplierAdmin.getKey(), VOVatRateFactory
                .newVOVatRate(new BigDecimal("13.00")), Arrays.asList(
                VOVatRateFactory.newVOCountryVatRate(new BigDecimal("20.00"),
                        TestOrganizationSetup.ORGANIZATION_DOMICILE_UK),
                VOVatRateFactory.newVOCountryVatRate(new BigDecimal("19.00"),
                        TestOrganizationSetup.ORGANIZATION_DOMICILE_DE)), null);

        orgSetup.createMarketingPermission(basicSetup.getSupplierAdminKey(),
                supplier.getOrganizationId(), technicalService);
        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        supplierAdmin.getKey(), "PICT_TEST_49_5",
                        TestService.EXAMPLE, TestPriceModel.EXAMPLE_PICT49_5,
                        technicalService, supplierMarketplace);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "GUEST");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "PICT_TEST_49_5", serviceDetails, customerAdmin, role);

        // user is assigned

        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        setDateFactory("2013-06-06 20:00:00");

        VORoleDefinition changedRole = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        subDetails = subscrSetup.addUser(basicSetup.getSecondCustomerUser1(),
                changedRole, subDetails.getSubscriptionId());

        // parameter change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        subDetails = subscrSetup.modifyParameterForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-06-09 20:00:00"),
                "MAX_FOLDER_NUMBER", "20");

        // user is assigned

        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        setDateFactory("2013-06-11 20:00:00");

        changedRole = VOServiceFactory.getRole(serviceDetails, "ADMIN");
        subDetails = subscrSetup.addUser(basicSetup.getSecondCustomerUser2(),
                changedRole, subDetails.getSubscriptionId());

        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        // reset old cut off day
        resetCutOffDay(supplierAdmin.getKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_49_5", subDetails);

        BillingIntegrationTestBase.updateCustomerListForTests("PICT_TEST_49_5",
                customer);
    }

    public void createPictScenario35() throws Exception {
        setDateFactory("2013-07-08 10:00:00");

        // Create a supplier with a marketplace
        String supplierAdminId = "Pict35Supplier";
        VOOrganization supplier = orgSetup.createOrganization(
                basicSetup.getPlatformOperatorUserKey(), supplierAdminId,
                "Pict35SupplierOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                OrganizationRoleType.TECHNOLOGY_PROVIDER,
                OrganizationRoleType.SUPPLIER);
        VOUser supplierAdmin = orgSetup.getUser(supplierAdminId, true);
        paymentSetup.createPaymentForSupplier(
                basicSetup.getPlatformOperatorUserKey(),
                supplierAdmin.getKey(), supplier);

        // Create own technical service
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        serviceSetup
                .importTechnicalServices(BaseAdmUmTest.TECHNICAL_SERVICE_EXAMPLE2_XML);
        VOTechnicalService example2Service = serviceSetup
                .getTechnicalService(VOTechServiceFactory.TECH_SERVICE_EXAMPLE2_ID);

        setCutOffDay(supplierAdmin.getKey(), 2);

        // Create a customer
        String customerAdminId = "Pict35Customer";
        VOOrganization customer = orgSetup.registerCustomer(
                "Pict35CustomerOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                customerAdminId, supplierMarketplace.getMarketplaceId(),
                supplier.getOrganizationId());
        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);

        orgSetup.createMarketingPermission(supplierAdmin.getKey(),
                supplier.getOrganizationId(), example2Service);

        // price model for customer
        VOServiceDetails serviceTemplate = serviceSetup
                .createAndPublishMarketableService(supplierAdmin.getKey(),
                        "PICT_TEST_35", TestService.EXAMPLE2,
                        TestPriceModel.EXAMPLE_PICT35, example2Service,
                        supplierMarketplace);
        VOServiceDetails serviceDetails = serviceSetup
                .savePriceModelForCustomer(serviceTemplate,
                        TestPriceModel.EXAMPLE_PICT35, customer);
        serviceDetails = serviceSetup.activateMarketableService(serviceDetails);

        orgSetup.saveAllVats(supplierAdmin.getKey(), VOVatRateFactory
                .newVOVatRate(new BigDecimal("13.00")), Arrays.asList(
                VOVatRateFactory.newVOCountryVatRate(new BigDecimal("20.00"),
                        TestOrganizationSetup.ORGANIZATION_DOMICILE_UK),
                VOVatRateFactory.newVOCountryVatRate(new BigDecimal("19.00"),
                        TestOrganizationSetup.ORGANIZATION_DOMICILE_DE)), null);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "GUEST");
        // subscribe
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "PICT_TEST_35", serviceDetails, customerAdmin, role);

        // Events
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        subscrSetup.recordEventForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-07-08 10:20:00"),
                "FILE_DOWNLOAD", 15);
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);

        // parameter change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        subDetails = subscrSetup.modifyParameterForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-07-15 02:10:00"),
                "LONG_NUMBER", "100");

        role = VOServiceFactory.getRole(serviceDetails, "USER");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        // user is assigned
        setDateFactory("2013-07-27 22:01:00");
        subDetails = subscrSetup.addUser(basicSetup.getSecondCustomerUser1(),
                role, subDetails.getSubscriptionId());

        // role change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        setDateFactory("2013-07-27 23:00:00");
        subDetails = subscrSetup.modifyUserRole(subDetails.getUsageLicenses()
                .get(0), VOServiceFactory.getRole(serviceDetails, "ADMIN"),
                subDetails.getSubscriptionId());

        // parameter change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        subDetails = subscrSetup.modifyParameterForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-07-27 23:10:00"),
                "LONG_NUMBER", "200");

        // Events
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        subscrSetup.recordEventForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-07-28 01:00:00"),
                "FOLDER_NEW", 15);
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);

        // user is revoked
        setDateFactory("2013-07-28 22:10:00");
        subscrSetup.revokeUser(basicSetup.getSecondCustomerUser1(),
                subDetails.getSubscriptionId());

        // unsubscribe

        setDateFactory("2013-07-29 02:00:00");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        // delete market service
        setDateFactory("2013-07-29 02:01:00");
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER);
        serviceSetup.deleteMarketableService(serviceDetails);

        // delete market service
        setDateFactory("2013-07-29 02:02:00");
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER);
        serviceSetup.deleteMarketableService(serviceTemplate);

        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);

        // Delete also the technical service
        setDateFactory("2013-07-29 02:03:00");
        container.login(supplierAdmin.getKey(), ROLE_TECHNOLOGY_MANAGER);
        serviceSetup.deleteTechnicalService(example2Service);

        resetCutOffDay(supplierAdmin.getKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_35", subDetails);

        BillingIntegrationTestBase.updateCustomerListForTests("PICT_TEST_35",
                customer);

    }

    public void createPictScenario34_1() throws Exception {
        setDateFactory("2013-07-08 10:00:00");

        // Create a supplier with a marketplace
        String supplierAdminId = "Pict34_1Supplier";
        VOOrganization supplier = orgSetup.createOrganization(
                basicSetup.getPlatformOperatorUserKey(), supplierAdminId,
                "Pict34_1SupplierOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                OrganizationRoleType.TECHNOLOGY_PROVIDER,
                OrganizationRoleType.SUPPLIER);
        VOUser supplierAdmin = orgSetup.getUser(supplierAdminId, true);
        paymentSetup.createPaymentForSupplier(
                basicSetup.getPlatformOperatorUserKey(),
                supplierAdmin.getKey(), supplier);

        // Create own technical service
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        serviceSetup
                .importTechnicalServices(BaseAdmUmTest.TECHNICAL_SERVICE_EXAMPLE2_XML);
        VOTechnicalService example2Service = serviceSetup
                .getTechnicalService(VOTechServiceFactory.TECH_SERVICE_EXAMPLE2_ID);

        setCutOffDay(supplierAdmin.getKey(), 1);

        // Create a customer
        String customerAdminId = "Pict34_1Customer";
        VOOrganization customer = orgSetup.registerCustomer(
                "Pict34_1CustomerOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                customerAdminId, supplierMarketplace.getMarketplaceId(),
                supplier.getOrganizationId());
        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);

        orgSetup.createMarketingPermission(supplierAdmin.getKey(),
                supplier.getOrganizationId(), example2Service);

        // price model for customer
        VOServiceDetails serviceTemplate = serviceSetup
                .createAndPublishMarketableService(supplierAdmin.getKey(),
                        "PICT_TEST_34_1", TestService.EXAMPLE2,
                        TestPriceModel.FREE, example2Service,
                        supplierMarketplace);
        VOServiceDetails serviceDetails = serviceSetup
                .savePriceModelForCustomer(serviceTemplate,
                        TestPriceModel.FREE, customer);
        serviceDetails = serviceSetup.activateMarketableService(serviceDetails);

        orgSetup.saveAllVats(supplierAdmin.getKey(), VOVatRateFactory
                .newVOVatRate(new BigDecimal("13.00")), Arrays.asList(
                VOVatRateFactory.newVOCountryVatRate(new BigDecimal("20.00"),
                        TestOrganizationSetup.ORGANIZATION_DOMICILE_UK),
                VOVatRateFactory.newVOCountryVatRate(new BigDecimal("19.00"),
                        TestOrganizationSetup.ORGANIZATION_DOMICILE_DE)), null);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "GUEST");
        // subscribe
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "PICT_TEST_34_1", serviceDetails, customerAdmin, role);

        // Events
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        subscrSetup.recordEventForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-07-08 10:20:00"),
                "FILE_DOWNLOAD", 15);
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);

        // upgrade service
        // /////////////////
        VOServiceDetails unitTemplate = serviceSetup
                .createAndPublishMarketableService(supplierAdmin.getKey(),
                        "PAIRWISE34_1_Upgrade", TestService.EXAMPLE2,
                        TestPriceModel.EXAMPLE_PICT34_UNIT_WEEKS,
                        example2Service, supplierMarketplace);
        serviceSetup.registerCompatibleServices(supplierAdmin.getKey(),
                serviceTemplate, unitTemplate);
        VOServiceDetails unitW = serviceSetup.savePriceModelForCustomer(
                unitTemplate, TestPriceModel.EXAMPLE_PICT34_UNIT_WEEKS,
                customer);
        unitW = serviceSetup.activateMarketableService(unitW);

        setDateFactory("2013-07-09 10:00:00");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(subDetails, unitW);

        // parameter change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        upgradedSubDetails = subscrSetup.modifyParameterForSubscription(
                upgradedSubDetails,
                DateTimeHandling.calculateMillis("2013-07-10 10:00:00"),
                "LONG_NUMBER", "100");

        role = VOServiceFactory.getRole(serviceDetails, "USER");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        // user is assigned
        setDateFactory("2013-07-10 11:00:00");
        upgradedSubDetails = subscrSetup.addUser(
                basicSetup.getSecondCustomerUser1(), role,
                upgradedSubDetails.getSubscriptionId());

        // role change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        setDateFactory("2013-07-10 20:00:00");
        upgradedSubDetails = subscrSetup.modifyUserRole(subDetails
                .getUsageLicenses().get(0), VOServiceFactory.getRole(unitW,
                "ADMIN"), upgradedSubDetails.getSubscriptionId());

        // parameter change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        upgradedSubDetails = subscrSetup.modifyParameterForSubscription(
                upgradedSubDetails,
                DateTimeHandling.calculateMillis("2013-07-31 20:00:00"),
                "LONG_NUMBER", "200");

        // Events
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        subscrSetup.recordEventForSubscription(upgradedSubDetails,
                DateTimeHandling.calculateMillis("2013-07-31 20:00:00"),
                "FOLDER_NEW", 15);
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);

        // user is revoked
        setDateFactory("2013-08-01 20:00:00");
        subscrSetup.revokeUser(basicSetup.getSecondCustomerUser1(),
                upgradedSubDetails.getSubscriptionId());

        // unsubscribe

        setDateFactory("2013-08-01 21:00:00");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        subscrSetup
                .unsubscribeToService(upgradedSubDetails.getSubscriptionId());

        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);

        resetCutOffDay(supplierAdmin.getKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_34_1", subDetails);

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_34_1", upgradedSubDetails);

        BillingIntegrationTestBase.updateCustomerListForTests("PICT_TEST_34_1",
                customer);

    }

    public void createPictScenario33() throws Exception {
        setDateFactory("2013-07-01 10:00:00");

        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);

        // Create own customer
        String customerAdminId = "PIC33Customer";
        VOOrganization customer = orgSetup.registerCustomer("PIC33CustomerOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                customerAdminId, supplierMarketplace.getMarketplaceId(),
                basicSetup.getSupplierOrgID());

        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(), "PICT_TEST_33",
                        TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PICT33_UNIT_WEEKS,
                        technicalService, supplierMarketplace);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");

        // subscribe
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "PICT_TEST_33", serviceDetails, customerAdmin, role);
        // price model change

        container.login(basicSetup.getSupplierAdminKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);

        long priceModelChangeTime = DateTimeHandling
                .calculateMillis("2013-07-01 11:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(priceModelChangeTime);
        VOPriceModel newSubPriceModel = VOPriceModelFactory
                .modifyPriceModelPeriodFee(subDetails.getPriceModel(),
                        new BigDecimal("20.00"));
        subscrSetup.savePriceModelForSubscription(
                basicSetup.getSupplierAdminKey(), subDetails, newSubPriceModel,
                customer);

        // Events
        container.login(basicSetup.getSupplierAdminKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        subscrSetup.recordEventForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-07-08 10:20:00"),
                "FILE_DOWNLOAD", 15);
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);

        // // 1. upgrade
        setDateFactory("2013-07-09 20:00:00");
        VOServiceDetails paidService = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(), "PAIRWISE33_Upgrade",
                        TestService.EXAMPLE, TestPriceModel.FREE,
                        technicalService, supplierMarketplace);
        serviceSetup.registerCompatibleServices(
                basicSetup.getSupplierAdminKey(), serviceDetails, paidService);
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(subDetails, paidService);

        // parameter change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        upgradedSubDetails = subscrSetup.modifyParameterForSubscription(
                upgradedSubDetails,
                DateTimeHandling.calculateMillis("2013-07-10 10:00:00"),
                "MAX_FOLDER_NUMBER", "10");

        // 2. upgrade
        setDateFactory("2013-07-13 20:00:00");
        VOServiceDetails paidService2 = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "PAIRWISE33_Upgrade2", TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PICT33_UNIT_WEEKS,
                        technicalService, supplierMarketplace);
        serviceSetup.registerCompatibleServices(
                basicSetup.getSupplierAdminKey(), paidService, paidService2);
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetails2 = subscrSetup
                .upgradeSubscription(upgradedSubDetails, paidService2);

        // Events
        container.login(basicSetup.getSupplierAdminKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        subscrSetup.recordEventForSubscription(upgradedSubDetails2,
                DateTimeHandling.calculateMillis("2013-07-13 20:02:00"),
                "FILE_UPLOAD", 15);
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);

        role = VOServiceFactory.getRole(serviceDetails, "USER");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);

        // user is assigned
        setDateFactory("2013-07-13 21:00:00");
        upgradedSubDetails2 = subscrSetup.addUser(
                basicSetup.getSecondCustomerUser1(), role,
                upgradedSubDetails2.getSubscriptionId());

        // role change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        setDateFactory("2013-07-13 22:00:00");
        upgradedSubDetails2 = subscrSetup.modifyUserRole(subDetails
                .getUsageLicenses().get(0), VOServiceFactory.getRole(
                paidService, "ADMIN"), upgradedSubDetails2.getSubscriptionId());

        // parameter change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        upgradedSubDetails2 = subscrSetup.modifyParameterForSubscription(
                upgradedSubDetails2,
                DateTimeHandling.calculateMillis("2013-07-23 22:00:00"),
                "MAX_FOLDER_NUMBER", "20");

        // Events
        container.login(basicSetup.getSupplierAdminKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        subscrSetup.recordEventForSubscription(upgradedSubDetails2,
                DateTimeHandling.calculateMillis("2013-07-27 20:00:00"),
                "FOLDER_NEW", 15);
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);

        // user is deassigned
        setDateFactory("2013-07-28 11:00:00");
        subscrSetup.revokeUser(customerAdmin,
                upgradedSubDetails2.getSubscriptionId());

        // user is assigned
        setDateFactory("2013-07-28 12:00:00");
        upgradedSubDetails2 = subscrSetup.addUser(customerAdmin, role,
                upgradedSubDetails2.getSubscriptionId());

        // delete market service
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-08-01 13:00:00"));
        container.login(basicSetup.getSupplierAdminKey(), ROLE_SERVICE_MANAGER);
        serviceSetup.deleteMarketableService(serviceDetails);

        // change sunbscriptionID
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-08-01 00:00:00"));
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        upgradedSubDetails2.setSubscriptionId("PICT_TEST_33" + "_SubID2");
        upgradedSubDetails2 = subscrSetup.modifySubscription(
                upgradedSubDetails2, null);

        // unsubscribe

        setDateFactory("2013-08-01 21:00:00");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        subscrSetup.unsubscribeToService(upgradedSubDetails2
                .getSubscriptionId());

        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);

        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_33", subDetails);

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_33", upgradedSubDetails);

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_33", upgradedSubDetails2);

        BillingIntegrationTestBase.updateCustomerListForTests("PICT_TEST_33",
                customer);

    }

    public void createPictScenario32() throws Exception {
        setDateFactory("2013-07-01 10:00:00");

        setCutOffDay(basicSetup.getSupplierAdminKey(), 1);

        // Create own customer
        String customerAdminId = "PIC32Customer";
        VOOrganization customer = orgSetup.registerCustomer("PIC32CustomerOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                customerAdminId, supplierMarketplace.getMarketplaceId(),
                basicSetup.getSupplierOrgID());

        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);

        orgSetup.updateCustomerDiscount(customer, new BigDecimal("25.00"),
                DateTimeHandling.calculateMillis("2013-07-02 00:00:00"),
                DateTimeHandling.calculateMillis("2013-08-03 01:00:00"));

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(), "PICT_TEST_32",
                        TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PICT32_UNIT_WEEKS,
                        technicalService, supplierMarketplace);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");

        // subscribe
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "PICT_TEST_32", serviceDetails, customerAdmin, role);

        // change subscriptionID
        setDateFactory("2013-07-02 00:00:00");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        subDetails.setSubscriptionId("PICT_TEST_32" + "_SubID2");
        subDetails = subscrSetup.modifySubscription(subDetails, null);

        // parameter change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        subDetails = subscrSetup.modifyParameterForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-07-10 10:00:00"),
                "LONG_NUMBER", "900");

        // parameter change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        subDetails = subscrSetup.modifyParameterForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-07-10 11:00:00"),
                "LONG_NUMBER", "500");

        // price model change

        container.login(basicSetup.getSupplierAdminKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);

        long priceModelChangeTime = DateTimeHandling
                .calculateMillis("2013-07-03 11:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(priceModelChangeTime);
        VOPriceModel newSubPriceModel = VOPriceModelFactory
                .modifyPriceModelPeriodFee(subDetails.getPriceModel(),
                        new BigDecimal("20.00"));
        subscrSetup.savePriceModelForSubscription(
                basicSetup.getSupplierAdminKey(), subDetails, newSubPriceModel,
                customer);

        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        // user is assigned
        setDateFactory("2013-07-13 21:00:00");
        subDetails = subscrSetup.addUser(basicSetup.getSecondCustomerUser1(),
                role, subDetails.getSubscriptionId());

        // user is deassigned
        setDateFactory("2013-07-13 21:10:01");
        subscrSetup.revokeUser(basicSetup.getSecondCustomerUser1(),
                subDetails.getSubscriptionId());

        // role change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        setDateFactory("2013-07-13 22:00:00");
        subDetails = subscrSetup.modifyUserRole(subDetails.getUsageLicenses()
                .get(0), VOServiceFactory.getRole(serviceDetails, "GUEST"),
                subDetails.getSubscriptionId());

        // Events
        container.login(basicSetup.getSupplierAdminKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        subscrSetup.recordEventForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-07-14 10:20:00"),
                "FILE_DOWNLOAD", 15);
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);

        // // 1. upgrade
        setDateFactory("2013-07-29 20:00:00");
        VOServiceDetails paidService = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(), "PAIRWISE32_Upgrade",
                        TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_RATA_WEEK_32, technicalService,
                        supplierMarketplace);
        serviceSetup.registerCompatibleServices(
                basicSetup.getSupplierAdminKey(), serviceDetails, paidService);
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(subDetails, paidService);

        // 2. upgrade
        setDateFactory("2013-07-30 20:00:00");
        VOServiceDetails paidService2 = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "PAIRWISE32_Upgrade2", TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PICT32_UNIT_WEEKS_2,
                        technicalService, supplierMarketplace);
        serviceSetup.registerCompatibleServices(
                basicSetup.getSupplierAdminKey(), paidService, paidService2);
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetails2 = subscrSetup
                .upgradeSubscription(upgradedSubDetails, paidService2);

        role = VOServiceFactory.getRole(serviceDetails, "USER");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);

        // parameter change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        upgradedSubDetails2 = subscrSetup.modifyParameterForSubscription(
                upgradedSubDetails2,
                DateTimeHandling.calculateMillis("2013-07-30 21:00:00"),
                "LONG_NUMBER", "500");

        // role change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        setDateFactory("2013-08-01 00:00:00");
        upgradedSubDetails2 = subscrSetup
                .modifyUserRole(subDetails.getUsageLicenses().get(0),
                        VOServiceFactory.getRole(paidService2, "ADMIN"),
                        upgradedSubDetails2.getSubscriptionId());

        // unsubscribe

        setDateFactory("2013-08-01 21:00:00");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        subscrSetup.unsubscribeToService(upgradedSubDetails2
                .getSubscriptionId());

        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);

        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_32", subDetails);

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_32", upgradedSubDetails);

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_32", upgradedSubDetails2);

        BillingIntegrationTestBase.updateCustomerListForTests("PICT_TEST_32",
                customer);

    }

    public void createPictScenario34() throws Exception {
        setDateFactory("2013-07-08 10:00:00");

        // Create a supplier with a marketplace
        String supplierAdminId = "Pict34Supplier";
        VOOrganization supplier = orgSetup.createOrganization(
                basicSetup.getPlatformOperatorUserKey(), supplierAdminId,
                "Pict34SupplierOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                OrganizationRoleType.TECHNOLOGY_PROVIDER,
                OrganizationRoleType.SUPPLIER);
        VOUser supplierAdmin = orgSetup.getUser(supplierAdminId, true);
        paymentSetup.createPaymentForSupplier(
                basicSetup.getPlatformOperatorUserKey(),
                supplierAdmin.getKey(), supplier);

        // Create own technical service
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        serviceSetup
                .importTechnicalServices(BaseAdmUmTest.TECHNICAL_SERVICE_EXAMPLE2_XML);
        VOTechnicalService example2Service = serviceSetup
                .getTechnicalService(VOTechServiceFactory.TECH_SERVICE_EXAMPLE2_ID);

        setCutOffDay(supplierAdmin.getKey(), 1);

        // Create a customer
        String customerAdminId = "Pict34Customer";
        VOOrganization customer = orgSetup.registerCustomer(
                "Pict34CustomerOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                customerAdminId, supplierMarketplace.getMarketplaceId(),
                supplier.getOrganizationId());
        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);

        orgSetup.createMarketingPermission(supplierAdmin.getKey(),
                supplier.getOrganizationId(), example2Service);

        // price model for customer
        VOServiceDetails serviceTemplate = serviceSetup
                .createAndPublishMarketableService(supplierAdmin.getKey(),
                        "PICT_TEST_34", TestService.EXAMPLE2,
                        TestPriceModel.FREE, example2Service,
                        supplierMarketplace);
        VOServiceDetails serviceDetails = serviceSetup
                .savePriceModelForCustomer(serviceTemplate,
                        TestPriceModel.FREE, customer);
        serviceDetails = serviceSetup.activateMarketableService(serviceDetails);

        orgSetup.saveAllVats(supplierAdmin.getKey(), VOVatRateFactory
                .newVOVatRate(new BigDecimal("13.00")), Arrays.asList(
                VOVatRateFactory.newVOCountryVatRate(new BigDecimal("20.00"),
                        TestOrganizationSetup.ORGANIZATION_DOMICILE_UK),
                VOVatRateFactory.newVOCountryVatRate(new BigDecimal("19.00"),
                        TestOrganizationSetup.ORGANIZATION_DOMICILE_DE)), null);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "GUEST");
        // subscribe
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "PICT_TEST_34", serviceDetails, customerAdmin, role);

        // Events
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        subscrSetup.recordEventForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-07-08 10:20:00"),
                "FILE_DOWNLOAD", 15);
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);

        // upgrade service
        // /////////////////
        VOServiceDetails unitTemplate = serviceSetup
                .createAndPublishMarketableService(supplierAdmin.getKey(),
                        "PAIRWISE34_Upgrade", TestService.EXAMPLE2,
                        TestPriceModel.EXAMPLE_PICT34_UNIT_WEEKS,
                        example2Service, supplierMarketplace);
        serviceSetup.registerCompatibleServices(supplierAdmin.getKey(),
                serviceTemplate, unitTemplate);
        VOServiceDetails unitW = serviceSetup.savePriceModelForCustomer(
                unitTemplate, TestPriceModel.EXAMPLE_PICT34_UNIT_WEEKS,
                customer);
        unitW = serviceSetup.activateMarketableService(unitW);

        setDateFactory("2013-07-09 10:00:00");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(subDetails, unitW);

        // parameter change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        upgradedSubDetails = subscrSetup.modifyParameterForSubscription(
                upgradedSubDetails,
                DateTimeHandling.calculateMillis("2013-07-10 10:00:00"),
                "LONG_NUMBER", "100");

        role = VOServiceFactory.getRole(serviceDetails, "USER");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        // user is assigned
        setDateFactory("2013-07-10 11:00:00");
        upgradedSubDetails = subscrSetup.addUser(
                basicSetup.getSecondCustomerUser1(), role,
                upgradedSubDetails.getSubscriptionId());

        // role change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        setDateFactory("2013-07-10 20:00:00");
        upgradedSubDetails = subscrSetup.modifyUserRole(subDetails
                .getUsageLicenses().get(0), VOServiceFactory.getRole(unitW,
                "ADMIN"), upgradedSubDetails.getSubscriptionId());

        // parameter change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        upgradedSubDetails = subscrSetup.modifyParameterForSubscription(
                upgradedSubDetails,
                DateTimeHandling.calculateMillis("2013-07-31 20:00:00"),
                "LONG_NUMBER", "200");

        // Events
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        subscrSetup.recordEventForSubscription(upgradedSubDetails,
                DateTimeHandling.calculateMillis("2013-07-31 20:00:00"),
                "FOLDER_NEW", 15);
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);

        // user is revoked
        setDateFactory("2013-08-01 20:00:00");
        subscrSetup.revokeUser(basicSetup.getSecondCustomerUser1(),
                upgradedSubDetails.getSubscriptionId());

        // delete market service
        setDateFactory("2013-08-02 20:00:00");
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER);
        serviceSetup.deleteMarketableService(unitW);

        // // suspend/resume subscription several times by removing/restoring
        // // customer's payment types
        setDateFactory("2013-08-01 21:00:00");

        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);

        paymentSetup.deleteCustomerPaymentTypes(customer);

        setDateFactory("2013-08-11 21:00:00");

        paymentSetup.reassignCustomerPaymentTypes(customer);

        upgradedSubDetails = subscrSetup.getSubscriptionDetails(
                customerAdmin.getKey(), upgradedSubDetails.getSubscriptionId());

        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);

        resetCutOffDay(supplierAdmin.getKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_34", subDetails);

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_34", upgradedSubDetails);

        BillingIntegrationTestBase.updateCustomerListForTests("PICT_TEST_34",
                customer);

    }

    public void createPictScenario31() throws Exception {
        setDateFactory("2013-07-08 10:00:00");

        // Create a supplier with a marketplace
        String supplierAdminId = "Pict31Supplier";
        VOOrganization supplier = orgSetup.createOrganization(
                basicSetup.getPlatformOperatorUserKey(), supplierAdminId,
                "Pict31SupplierOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                OrganizationRoleType.TECHNOLOGY_PROVIDER,
                OrganizationRoleType.SUPPLIER);
        VOUser supplierAdmin = orgSetup.getUser(supplierAdminId, true);
        paymentSetup.createPaymentForSupplier(
                basicSetup.getPlatformOperatorUserKey(),
                supplierAdmin.getKey(), supplier);

        // Create own technical service
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        serviceSetup
                .importTechnicalServices(BaseAdmUmTest.TECHNICAL_SERVICE_EXAMPLE2_XML);
        VOTechnicalService example2Service = serviceSetup
                .getTechnicalService(VOTechServiceFactory.TECH_SERVICE_EXAMPLE2_ID);

        setCutOffDay(supplierAdmin.getKey(), 1);

        // Create a customer
        String customerAdminId = "Pict31Customer";
        VOOrganization customer = orgSetup.registerCustomer(
                "Pict31CustomerOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                customerAdminId, supplierMarketplace.getMarketplaceId(),
                supplier.getOrganizationId());
        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);

        orgSetup.createMarketingPermission(supplierAdmin.getKey(),
                supplier.getOrganizationId(), example2Service);

        // price model for customer
        VOServiceDetails serviceTemplate = serviceSetup
                .createAndPublishMarketableService(supplierAdmin.getKey(),
                        "PICT_TEST_31", TestService.EXAMPLE2,
                        TestPriceModel.FREE, example2Service,
                        supplierMarketplace);
        VOServiceDetails serviceDetails = serviceSetup
                .savePriceModelForCustomer(serviceTemplate,
                        TestPriceModel.FREE, customer);
        serviceDetails = serviceSetup.activateMarketableService(serviceDetails);

        // Vat
        orgSetup.saveAllVats(supplierAdmin.getKey(), VOVatRateFactory
                .newVOVatRate(new BigDecimal("13.00")), Arrays.asList(
                VOVatRateFactory.newVOCountryVatRate(new BigDecimal("20.00"),
                        TestOrganizationSetup.ORGANIZATION_DOMICILE_UK),
                VOVatRateFactory.newVOCountryVatRate(new BigDecimal("19.00"),
                        TestOrganizationSetup.ORGANIZATION_DOMICILE_DE)),
                Arrays.asList(VOVatRateFactory.newVOOrganizationVatRate(
                        new BigDecimal("5.00"), customer)));

        orgSetup.updateCustomerDiscount(customer, new BigDecimal("25.00"),
                DateTimeHandling.calculateMillis("2013-07-08 00:00:00"),
                DateTimeHandling.calculateMillis("2013-08-09 01:00:00"));

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "GUEST");
        // subscribe
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "PICT_TEST_31", serviceDetails, customerAdmin, role);

        // Events
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        subscrSetup.recordEventForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-07-08 10:20:00"),
                "FILE_DOWNLOAD", 15);
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);

        // parameter change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        subDetails = subscrSetup.modifyParameterForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-07-15 02:10:00"),
                "HAS_OPTIONS", "1");

        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);

        // role change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        setDateFactory("2013-07-27 23:00:00");
        subDetails = subscrSetup.modifyUserRole(subDetails.getUsageLicenses()
                .get(0), VOServiceFactory.getRole(serviceDetails, "ADMIN"),
                subDetails.getSubscriptionId());

        // role change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        setDateFactory("2013-07-27 28:00:00");
        subDetails = subscrSetup.modifyUserRole(subDetails.getUsageLicenses()
                .get(0), VOServiceFactory.getRole(serviceDetails, "GUEST"),
                subDetails.getSubscriptionId());

        // Events
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        subscrSetup.recordEventForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-07-28 01:00:00"),
                "FOLDER_NEW", 15);
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);

        // unsubscribe

        setDateFactory("2013-07-29 02:00:00");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        // delete market service
        setDateFactory("2013-07-29 02:01:00");
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER);
        serviceSetup.deleteMarketableService(serviceDetails);

        // delete market service
        setDateFactory("2013-07-29 02:02:00");
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER);
        serviceSetup.deleteMarketableService(serviceTemplate);

        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);

        // Delete also the technical service
        setDateFactory("2013-07-29 02:03:00");
        container.login(supplierAdmin.getKey(), ROLE_TECHNOLOGY_MANAGER);
        serviceSetup.deleteTechnicalService(example2Service);

        resetCutOffDay(supplierAdmin.getKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_31", subDetails);

        BillingIntegrationTestBase.updateCustomerListForTests("PICT_TEST_31",
                customer);

    }

    public void createPictScenario30() throws Exception {
        setDateFactory("2013-07-11 10:00:00");

        // Create a supplier with a marketplace
        String supplierAdminId = "Pict30Supplier";
        VOOrganization supplier = orgSetup.createOrganization(
                basicSetup.getPlatformOperatorUserKey(), supplierAdminId,
                "Pict30SupplierOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                OrganizationRoleType.TECHNOLOGY_PROVIDER,
                OrganizationRoleType.SUPPLIER);
        VOUser supplierAdmin = orgSetup.getUser(supplierAdminId, true);
        paymentSetup.createPaymentForSupplier(
                basicSetup.getPlatformOperatorUserKey(),
                supplierAdmin.getKey(), supplier);

        // Create own technical service
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        serviceSetup
                .importTechnicalServices(BaseAdmUmTest.TECHNICAL_SERVICE_EXAMPLE2_XML);
        VOTechnicalService example2Service = serviceSetup
                .getTechnicalService(VOTechServiceFactory.TECH_SERVICE_EXAMPLE2_ID);

        setCutOffDay(supplierAdmin.getKey(), 13);

        // Create a customer
        String customerAdminId = "Pict30Customer";
        VOOrganization customer = orgSetup.registerCustomer(
                "Pict30CustomerOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                customerAdminId, supplierMarketplace.getMarketplaceId(),
                supplier.getOrganizationId());
        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);

        orgSetup.createMarketingPermission(supplierAdmin.getKey(),
                supplier.getOrganizationId(), example2Service);

        // price model for customer
        VOServiceDetails serviceTemplate = serviceSetup
                .createAndPublishMarketableService(supplierAdmin.getKey(),
                        "PICT_TEST_30", TestService.EXAMPLE2,
                        TestPriceModel.EXAMPLE_PICT30, example2Service,
                        supplierMarketplace);
        VOServiceDetails serviceDetails = serviceSetup
                .savePriceModelForCustomer(serviceTemplate,
                        TestPriceModel.EXAMPLE_PICT30, customer);
        serviceDetails = serviceSetup.activateMarketableService(serviceDetails);

        orgSetup.saveAllVats(supplierAdmin.getKey(), VOVatRateFactory
                .newVOVatRate(new BigDecimal("13.00")), Arrays.asList(
                VOVatRateFactory.newVOCountryVatRate(new BigDecimal("20.00"),
                        TestOrganizationSetup.ORGANIZATION_DOMICILE_UK),
                VOVatRateFactory.newVOCountryVatRate(new BigDecimal("19.00"),
                        TestOrganizationSetup.ORGANIZATION_DOMICILE_DE)), null);
        orgSetup.updateCustomerDiscount(customer, new BigDecimal("25.00"),
                DateTimeHandling.calculateMillis("2013-07-08 00:00:00"),
                DateTimeHandling.calculateMillis("2013-07-08 01:00:00"));
        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "GUEST");
        // subscribe
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "PICT_TEST_30", serviceDetails, customerAdmin, role);

        // // suspend/resume subscription several times by removing/restoring
        // // customer's payment types
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        setDateFactory("2013-07-11 12:00:00");
        paymentSetup.deleteCustomerPaymentTypes(customer);

        setDateFactory("2013-07-13 13:00:00");
        paymentSetup.reassignCustomerPaymentTypes(customer);

        subDetails = subscrSetup.getSubscriptionDetails(customerAdmin.getKey(),
                subDetails.getSubscriptionId());

        // Events
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        subscrSetup.recordEventForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-07-14 00:20:00"),
                "FILE_DOWNLOAD", 15);

        // price model change
        setDateFactory("2013-07-14 08:00:00");

        VOPriceModel newSubPriceModel = VOPriceModelFactory
                .modifyPriceModelPeriodFee(subDetails.getPriceModel(),
                        new BigDecimal("2.00"));

        subscrSetup.savePriceModelForSubscription(supplierAdmin.getKey(),
                subDetails, newSubPriceModel, customer);

        // change subscriptionID
        setDateFactory("2013-07-14 09:00:00");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        subDetails.setSubscriptionId("PICT_TEST_30" + "_SubID2");
        subDetails = subscrSetup.modifySubscription(subDetails, null);

        // role change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        setDateFactory("2013-07-14 10:00:00");
        subDetails = subscrSetup.modifyUserRole(subDetails.getUsageLicenses()
                .get(0), VOServiceFactory.getRole(serviceDetails, "ADMIN"),
                subDetails.getSubscriptionId());

        // parameter change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        subDetails = subscrSetup.modifyParameterForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-07-14 12:00:00"),
                "MAX_FOLDER_NUMBER", "50");

        // role change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        setDateFactory("2013-08-12 01:00:00");
        subDetails = subscrSetup.modifyUserRole(subDetails.getUsageLicenses()
                .get(0), VOServiceFactory.getRole(serviceDetails, "GUEST"),
                subDetails.getSubscriptionId());

        // parameter change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        subDetails = subscrSetup.modifyParameterForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-08-12 10:00:00"),
                "MAX_FOLDER_NUMBER", "100");
        // parameter change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        subDetails = subscrSetup.modifyParameterForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-08-12 12:00:00"),
                "MAX_FOLDER_NUMBER", "50");

        // Events
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        subscrSetup.recordEventForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-08-12 20:00:00"),
                "FOLDER_NEW", 15);

        resetCutOffDay(supplierAdmin.getKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_30", subDetails);

        BillingIntegrationTestBase.updateCustomerListForTests("PICT_TEST_30",
                customer);

    }

    public void createPictScenario29() throws Exception {
        setDateFactory("2013-07-04 10:00:00");

        // Create a supplier with a marketplace
        String supplierAdminId = "Pict29Supplier";
        VOOrganization supplier = orgSetup.createOrganization(
                basicSetup.getPlatformOperatorUserKey(), supplierAdminId,
                "Pict29SupplierOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                OrganizationRoleType.TECHNOLOGY_PROVIDER,
                OrganizationRoleType.SUPPLIER);
        VOUser supplierAdmin = orgSetup.getUser(supplierAdminId, true);
        paymentSetup.createPaymentForSupplier(
                basicSetup.getPlatformOperatorUserKey(),
                supplierAdmin.getKey(), supplier);

        // Create own technical service
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        serviceSetup
                .importTechnicalServices(BaseAdmUmTest.TECHNICAL_SERVICE_EXAMPLE2_XML);
        VOTechnicalService example2Service = serviceSetup
                .getTechnicalService(VOTechServiceFactory.TECH_SERVICE_EXAMPLE2_ID);

        setCutOffDay(supplierAdmin.getKey(), 5);

        // Create a customer
        String customerAdminId = "Pict29Customer";
        VOOrganization customer = orgSetup.registerCustomer(
                "Pict29CustomerOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                customerAdminId, supplierMarketplace.getMarketplaceId(),
                supplier.getOrganizationId());
        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);

        orgSetup.createMarketingPermission(supplierAdmin.getKey(),
                supplier.getOrganizationId(), example2Service);

        // price model for customer
        VOServiceDetails serviceTemplate = serviceSetup
                .createAndPublishMarketableService(supplierAdmin.getKey(),
                        "PICT_TEST_29", TestService.EXAMPLE2,
                        TestPriceModel.EXAMPLE_PICT29_UNIT_WEEKS,
                        example2Service, supplierMarketplace);
        VOServiceDetails serviceDetails = serviceSetup
                .savePriceModelForCustomer(serviceTemplate,
                        TestPriceModel.EXAMPLE_PICT29_UNIT_WEEKS, customer);
        serviceDetails = serviceSetup.activateMarketableService(serviceDetails);

        orgSetup.saveAllVats(supplierAdmin.getKey(),
                VOVatRateFactory.newVOVatRate(new BigDecimal("13.00")), null,
                null);

        orgSetup.updateCustomerDiscount(customer, new BigDecimal("25.00"),
                DateTimeHandling.calculateMillis("2013-07-08 00:00:00"),
                DateTimeHandling.calculateMillis("2013-07-08 01:00:00"));
        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "GUEST");
        // subscribe
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "PICT_TEST_29", serviceDetails, customerAdmin, role);

        // Events
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        subscrSetup.recordEventForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-07-04 10:00:01"),
                "FILE_DOWNLOAD", 15);
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);

        // user is deassigned
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        setDateFactory("2013-07-04 11:00:00");
        subscrSetup.revokeUser(customerAdmin, subDetails.getSubscriptionId());

        // user is assigned
        setDateFactory("2013-07-04 11:00:01");
        subDetails = subscrSetup.addUser(customerAdmin, role,
                subDetails.getSubscriptionId());

        // upgrade service

        VOServiceDetails rataTemplate = serviceSetup
                .createAndPublishMarketableService(supplierAdmin.getKey(),
                        "PAIRWISE34_Upgrade", TestService.EXAMPLE2,
                        TestPriceModel.EXAMPLE_PICT38_RATA_HOUR,
                        example2Service, supplierMarketplace);
        serviceSetup.registerCompatibleServices(supplierAdmin.getKey(),
                serviceTemplate, rataTemplate);
        VOServiceDetails rataH = serviceSetup
                .savePriceModelForCustomer(rataTemplate,
                        TestPriceModel.EXAMPLE_PICT38_RATA_HOUR, customer);
        rataH = serviceSetup.activateMarketableService(rataH);

        setDateFactory("2013-07-09 10:00:00");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(subDetails, rataH);

        // change sunbscriptionID
        setDateFactory("2013-07-09 10:00:02");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        upgradedSubDetails.setSubscriptionId("PICT_TEST_29" + "_SubID2");
        upgradedSubDetails = subscrSetup.modifySubscription(upgradedSubDetails,
                null);

        // role change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        setDateFactory("2013-07-09 11:30:00");
        upgradedSubDetails = subscrSetup.modifyUserRole(subDetails
                .getUsageLicenses().get(0), VOServiceFactory.getRole(rataH,
                "ADMIN"), upgradedSubDetails.getSubscriptionId());

        // parameter change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        upgradedSubDetails = subscrSetup.modifyParameterForSubscription(
                upgradedSubDetails,
                DateTimeHandling.calculateMillis("2013-07-09 11:30:01"),
                "LONG_NUMBER", "100");

        // role change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        setDateFactory("2013-07-09 11:45:00");
        upgradedSubDetails = subscrSetup.modifyUserRole(upgradedSubDetails
                .getUsageLicenses().get(0), VOServiceFactory.getRole(rataH,
                "GUEST"), upgradedSubDetails.getSubscriptionId());

        // parameter change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        upgradedSubDetails = subscrSetup.modifyParameterForSubscription(
                upgradedSubDetails,
                DateTimeHandling.calculateMillis("2013-07-09 11:45:01"),
                "LONG_NUMBER", "200");

        // // suspend/resume subscription several times by removing/restoring
        // // customer's payment types
        setDateFactory("2013-07-10 12:00:00");

        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);

        paymentSetup.deleteCustomerPaymentTypes(customer);

        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);

        resetCutOffDay(supplierAdmin.getKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_29", subDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_29", upgradedSubDetails);

        BillingIntegrationTestBase.updateCustomerListForTests("PICT_TEST_29",
                customer);

    }

    public void createPictScenario28() throws Exception {
        setDateFactory("2013-08-12 10:00:00");

        // Create a supplier with a marketplace
        String supplierAdminId = "Pict28Supplier";
        VOOrganization supplier = orgSetup.createOrganization(
                basicSetup.getPlatformOperatorUserKey(), supplierAdminId,
                "Pict28SupplierOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                OrganizationRoleType.TECHNOLOGY_PROVIDER,
                OrganizationRoleType.SUPPLIER);
        VOUser supplierAdmin = orgSetup.getUser(supplierAdminId, true);
        paymentSetup.createPaymentForSupplier(
                basicSetup.getPlatformOperatorUserKey(),
                supplierAdmin.getKey(), supplier);

        // Create own technical service
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        serviceSetup
                .importTechnicalServices(BaseAdmUmTest.TECHNICAL_SERVICE_EXAMPLE2_XML);
        VOTechnicalService example2Service = serviceSetup
                .getTechnicalService(VOTechServiceFactory.TECH_SERVICE_EXAMPLE2_ID);

        setCutOffDay(supplierAdmin.getKey(), 10);

        // Create a customer
        String customerAdminId = "Pict28Customer";
        VOOrganization customer = orgSetup.registerCustomer(
                "Pict28CustomerOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                customerAdminId, supplierMarketplace.getMarketplaceId(),
                supplier.getOrganizationId());
        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);

        orgSetup.createMarketingPermission(supplierAdmin.getKey(),
                supplier.getOrganizationId(), example2Service);

        // price model for customer
        VOServiceDetails serviceTemplate = serviceSetup
                .createAndPublishMarketableService(supplierAdmin.getKey(),
                        "PICT_TEST_28", TestService.EXAMPLE2,
                        TestPriceModel.EXAMPLE_PICT28_UNIT_WEEKS,
                        example2Service, supplierMarketplace);
        VOServiceDetails serviceDetails = serviceSetup
                .savePriceModelForCustomer(serviceTemplate,
                        TestPriceModel.EXAMPLE_PICT28_UNIT_WEEKS, customer);
        serviceDetails = serviceSetup.activateMarketableService(serviceDetails);

        // Vat
        orgSetup.saveAllVats(supplierAdmin.getKey(), VOVatRateFactory
                .newVOVatRate(new BigDecimal("13.00")), Arrays.asList(
                VOVatRateFactory.newVOCountryVatRate(new BigDecimal("20.00"),
                        TestOrganizationSetup.ORGANIZATION_DOMICILE_UK),
                VOVatRateFactory.newVOCountryVatRate(new BigDecimal("19.00"),
                        TestOrganizationSetup.ORGANIZATION_DOMICILE_DE)),
                Arrays.asList(VOVatRateFactory.newVOOrganizationVatRate(
                        new BigDecimal("5.00"), customer)));

        orgSetup.updateCustomerDiscount(customer, new BigDecimal("3.00"),
                DateTimeHandling.calculateMillis("2013-07-08 00:00:00"),
                DateTimeHandling.calculateMillis("2013-08-09 01:00:00"));

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "GUEST");
        // subscribe
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "PICT_TEST_28", serviceDetails, customerAdmin, role);

        // Events
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        subscrSetup.recordEventForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-08-13 10:20:00"),
                "FILE_DOWNLOAD", 15);
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);

        // price model change
        setDateFactory("2013-08-13 10:21:00");

        VOPriceModel newSubPriceModel = VOPriceModelFactory
                .modifyPriceModelPeriodFee(subDetails.getPriceModel(),
                        new BigDecimal("2.00"));

        subscrSetup.savePriceModelForSubscription(supplierAdmin.getKey(),
                subDetails, newSubPriceModel, customer);

        // change sunbscriptionID
        setDateFactory("2013-08-13 10:22:00");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        subDetails.setSubscriptionId("PICT_TEST_28" + "_SubID2");
        subDetails = subscrSetup.modifySubscription(subDetails, null);

        // 1. upgrade service
        VOServiceDetails unitTemplate = serviceSetup
                .createAndPublishMarketableService(supplierAdmin.getKey(),
                        "PAIRWISE28_Upgrade", TestService.EXAMPLE2,
                        TestPriceModel.EXAMPLE_PICT28_RATA_WEEKS,
                        example2Service, supplierMarketplace);
        serviceSetup.registerCompatibleServices(supplierAdmin.getKey(),
                serviceTemplate, unitTemplate);
        VOServiceDetails rataW = serviceSetup.savePriceModelForCustomer(
                unitTemplate, TestPriceModel.EXAMPLE_PICT28_RATA_WEEKS,
                customer);
        rataW = serviceSetup.activateMarketableService(rataW);

        setDateFactory("2013-08-31 10:00:00");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(subDetails, rataW);

        // user is deassigned
        setDateFactory("2013-08-31 11:00:00");
        subscrSetup.revokeUser(customerAdmin,
                upgradedSubDetails.getSubscriptionId());

        // suspend/resume subscription several times by removing/restoring
        // customer's payment types
        setDateFactory("2013-09-01 12:00:00");

        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);

        paymentSetup.deleteCustomerPaymentTypes(customer);

        setDateFactory("2013-09-01 13:00:00");

        paymentSetup.reassignCustomerPaymentTypes(customer);

        upgradedSubDetails = subscrSetup.getSubscriptionDetails(
                customerAdmin.getKey(), upgradedSubDetails.getSubscriptionId());

        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);

        // Events
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        subscrSetup.recordEventForSubscription(upgradedSubDetails,
                DateTimeHandling.calculateMillis("2013-09-02 01:00:00"),
                "FOLDER_NEW", 15);
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);

        // unsubscribe
        setDateFactory("2013-09-02 02:00:00");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        subscrSetup
                .unsubscribeToService(upgradedSubDetails.getSubscriptionId());

        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);

        resetCutOffDay(supplierAdmin.getKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_28", subDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_28", upgradedSubDetails);

        BillingIntegrationTestBase.updateCustomerListForTests("PICT_TEST_28",
                customer);

    }

    public void createPictScenario28_1() throws Exception {
        setDateFactory("2013-08-07 10:00:00");

        // Create a supplier with a marketplace
        String supplierAdminId = "Pict28_1Supplier";
        VOOrganization supplier = orgSetup.createOrganization(
                basicSetup.getPlatformOperatorUserKey(), supplierAdminId,
                "Pict28_1SupplierOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                OrganizationRoleType.TECHNOLOGY_PROVIDER,
                OrganizationRoleType.SUPPLIER);
        VOUser supplierAdmin = orgSetup.getUser(supplierAdminId, true);
        paymentSetup.createPaymentForSupplier(
                basicSetup.getPlatformOperatorUserKey(),
                supplierAdmin.getKey(), supplier);

        // Create own technical service
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        serviceSetup
                .importTechnicalServices(BaseAdmUmTest.TECHNICAL_SERVICE_EXAMPLE2_XML);
        VOTechnicalService example2Service = serviceSetup
                .getTechnicalService(VOTechServiceFactory.TECH_SERVICE_EXAMPLE2_ID);

        setCutOffDay(supplierAdmin.getKey(), 10);

        // Create a customer
        String customerAdminId = "Pict28_1Customer";
        VOOrganization customer = orgSetup.registerCustomer(
                "Pict28_1CustomerOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                customerAdminId, supplierMarketplace.getMarketplaceId(),
                supplier.getOrganizationId());
        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);

        orgSetup.createMarketingPermission(supplierAdmin.getKey(),
                supplier.getOrganizationId(), example2Service);

        // price model for customer
        VOServiceDetails serviceTemplate = serviceSetup
                .createAndPublishMarketableService(supplierAdmin.getKey(),
                        "PICT_TEST_28_1", TestService.EXAMPLE2,
                        TestPriceModel.EXAMPLE_PICT28_1_UNIT_WEEKS,
                        example2Service, supplierMarketplace);
        VOServiceDetails serviceDetails = serviceSetup
                .savePriceModelForCustomer(serviceTemplate,
                        TestPriceModel.EXAMPLE_PICT28_1_UNIT_WEEKS, customer);
        serviceDetails = serviceSetup.activateMarketableService(serviceDetails);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "GUEST");
        // subscribe
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "PICT_TEST_28_1", serviceDetails, customerAdmin, role);

        // Events
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        subscrSetup.recordEventForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-08-08 11:00:00"),
                "FILE_DOWNLOAD", 15);
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);

        // 1. upgrade service
        VOServiceDetails unitTemplate = serviceSetup
                .createAndPublishMarketableService(supplierAdmin.getKey(),
                        "PAIRWISE28_1_Upgrade", TestService.EXAMPLE2,
                        TestPriceModel.EXAMPLE_PICT28_1_RATA_WEEKS,
                        example2Service, supplierMarketplace);
        serviceSetup.registerCompatibleServices(supplierAdmin.getKey(),
                serviceTemplate, unitTemplate);
        VOServiceDetails rataW = serviceSetup.savePriceModelForCustomer(
                unitTemplate, TestPriceModel.EXAMPLE_PICT28_1_RATA_WEEKS,
                customer);
        rataW = serviceSetup.activateMarketableService(rataW);

        setDateFactory("2013-08-08 13:00:00");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(subDetails, rataW);

        // Events
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        subscrSetup.recordEventForSubscription(upgradedSubDetails,
                DateTimeHandling.calculateMillis("2013-09-02 01:00:00"),
                "FOLDER_NEW", 15);

        resetCutOffDay(supplierAdmin.getKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_28_1", subDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_28_1", upgradedSubDetails);

        BillingIntegrationTestBase.updateCustomerListForTests("PICT_TEST_28_1",
                customer);

    }

    public void createPictScenario27() throws Exception {
        setDateFactory("2013-08-01 10:00:00");

        // Create a supplier with a marketplace
        String supplierAdminId = "Pict27Supplier";
        VOOrganization supplier = orgSetup.createOrganization(
                basicSetup.getPlatformOperatorUserKey(), supplierAdminId,
                "Pict27SupplierOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                OrganizationRoleType.TECHNOLOGY_PROVIDER,
                OrganizationRoleType.SUPPLIER);
        VOUser supplierAdmin = orgSetup.getUser(supplierAdminId, true);
        paymentSetup.createPaymentForSupplier(
                basicSetup.getPlatformOperatorUserKey(),
                supplierAdmin.getKey(), supplier);

        // Create own technical service
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        serviceSetup
                .importTechnicalServices(BaseAdmUmTest.TECHNICAL_SERVICE_EXAMPLE2_XML);
        VOTechnicalService example2Service = serviceSetup
                .getTechnicalService(VOTechServiceFactory.TECH_SERVICE_EXAMPLE2_ID);

        setCutOffDay(supplierAdmin.getKey(), 1);

        // Create a customer
        String customerAdminId = "Pict27Customer";
        VOOrganization customer = orgSetup.registerCustomer(
                "Pict27CustomerOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                customerAdminId, supplierMarketplace.getMarketplaceId(),
                supplier.getOrganizationId());
        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);

        orgSetup.createMarketingPermission(supplierAdmin.getKey(),
                supplier.getOrganizationId(), example2Service);

        // price model for service
        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        supplierAdmin.getKey(), "PICT_TEST_27",
                        TestService.EXAMPLE2, TestPriceModel.FREE,
                        example2Service, supplierMarketplace);

        orgSetup.updateCustomerDiscount(customer, new BigDecimal("3.00"),
                DateTimeHandling.calculateMillis("2013-07-08 00:00:00"),
                DateTimeHandling.calculateMillis("2013-08-09 01:00:00"));

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "GUEST");
        // subscribe
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "PICT_TEST_27", serviceDetails, customerAdmin, role);

        // change sunbscriptionID
        setDateFactory("2013-08-02 10:00:00");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        subDetails.setSubscriptionId("PICT_TEST_27" + "_SubID2");
        subDetails = subscrSetup.modifySubscription(subDetails, null);

        // 1. upgrade service
        VOServiceDetails rataW = serviceSetup
                .createPublishAndActivateMarketableService(
                        supplierAdmin.getKey(), "PAIRWISE27_Upgrade",
                        TestService.EXAMPLE2,
                        TestPriceModel.EXAMPLE_PICT27_RATA_WEEKS,
                        example2Service, supplierMarketplace);
        serviceSetup.registerCompatibleServices(supplierAdmin.getKey(),
                serviceDetails, rataW);

        setDateFactory("2013-08-02 11:00:00");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(subDetails, rataW);

        // parameter change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        upgradedSubDetails = subscrSetup.modifyParameterForSubscription(
                upgradedSubDetails,
                DateTimeHandling.calculateMillis("2013-08-02 12:00:00"),
                "MAX_FOLDER_NUMBER", "50");

        // role change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        setDateFactory("2013-08-02 13:00:00");
        upgradedSubDetails = subscrSetup.modifyUserRole(subDetails
                .getUsageLicenses().get(0), VOServiceFactory.getRole(rataW,
                "USER"), upgradedSubDetails.getSubscriptionId());

        // 2. upgrade service
        VOServiceDetails rataW2 = serviceSetup
                .createPublishAndActivateMarketableService(
                        supplierAdmin.getKey(), "PAIRWISE27_Upgrade_2",
                        TestService.EXAMPLE2,
                        TestPriceModel.EXAMPLE_PICT27_RATA_WEEKS,
                        example2Service, supplierMarketplace);
        serviceSetup.registerCompatibleServices(supplierAdmin.getKey(), rataW,
                rataW2);

        setDateFactory("2013-08-03 11:00:00");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetails2 = subscrSetup
                .upgradeSubscription(upgradedSubDetails, rataW2);

        // suspend/resume subscription several times by removing/restoring
        // customer's payment types
        setDateFactory("2013-08-31 11:00:00");

        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);

        paymentSetup.deleteCustomerPaymentTypes(customer);

        // unsubscribe
        setDateFactory("2013-09-02 02:00:00");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        subscrSetup.unsubscribeToService(upgradedSubDetails2
                .getSubscriptionId());

        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);

        resetCutOffDay(supplierAdmin.getKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_27", subDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_27", upgradedSubDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_27", upgradedSubDetails2);

        BillingIntegrationTestBase.updateCustomerListForTests("PICT_TEST_27",
                customer);

    }

    public void createPictScenario26() throws Exception {
        setDateFactory("2013-08-01 10:00:00");

        // Create a supplier with a marketplace
        String supplierAdminId = "Pict26Supplier";
        VOOrganization supplier = orgSetup.createOrganization(
                basicSetup.getPlatformOperatorUserKey(), supplierAdminId,
                "Pict26SupplierOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                OrganizationRoleType.TECHNOLOGY_PROVIDER,
                OrganizationRoleType.SUPPLIER);
        VOUser supplierAdmin = orgSetup.getUser(supplierAdminId, true);
        paymentSetup.createPaymentForSupplier(
                basicSetup.getPlatformOperatorUserKey(),
                supplierAdmin.getKey(), supplier);

        // Create own technical service
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        serviceSetup
                .importTechnicalServices(BaseAdmUmTest.TECHNICAL_SERVICE_EXAMPLE2_XML);
        VOTechnicalService example2Service = serviceSetup
                .getTechnicalService(VOTechServiceFactory.TECH_SERVICE_EXAMPLE2_ID);

        setCutOffDay(supplierAdmin.getKey(), 10);

        // Create a customer
        String customerAdminId = "Pict26Customer";
        VOOrganization customer = orgSetup.registerCustomer(
                "Pict26CustomerOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                customerAdminId, supplierMarketplace.getMarketplaceId(),
                supplier.getOrganizationId());
        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);

        orgSetup.createMarketingPermission(supplierAdmin.getKey(),
                supplier.getOrganizationId(), example2Service);

        // price model for customer
        VOServiceDetails serviceTemplate = serviceSetup
                .createAndPublishMarketableService(supplierAdmin.getKey(),
                        "PICT_TEST_26", TestService.EXAMPLE2,
                        TestPriceModel.EXAMPLE_PICT26_UNIT_WEEKS,
                        example2Service, supplierMarketplace);
        VOServiceDetails serviceDetails = serviceSetup
                .savePriceModelForCustomer(serviceTemplate,
                        TestPriceModel.EXAMPLE_PICT26_UNIT_WEEKS, customer);
        serviceDetails = serviceSetup.activateMarketableService(serviceDetails);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "GUEST");
        // subscribe
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "PICT_TEST_26", serviceDetails, customerAdmin, role);

        // suspend/resume subscription several times by removing/restoring
        // customer's payment types
        setDateFactory("2013-08-02 12:00:00");

        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);

        paymentSetup.deleteCustomerPaymentTypes(customer);

        setDateFactory("2013-08-05 13:00:00");

        paymentSetup.reassignCustomerPaymentTypes(customer);

        subDetails = subscrSetup.getSubscriptionDetails(customerAdmin.getKey(),
                subDetails.getSubscriptionId());

        // Events
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        subscrSetup.recordEventForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-08-05 14:00:00"),
                "FILE_DOWNLOAD", 15);

        // parameter change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        subDetails = subscrSetup.modifyParameterForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-08-06 14:00:00"),
                "MAX_FOLDER_NUMBER", "50");

        // role change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        setDateFactory("2013-08-08 14:00:00");
        subDetails = subscrSetup.modifyUserRole(subDetails.getUsageLicenses()
                .get(0), VOServiceFactory.getRole(serviceDetails, "USER"),
                subDetails.getSubscriptionId());

        // 1. upgrade service
        VOServiceDetails unitTemplate = serviceSetup
                .createAndPublishMarketableService(supplierAdmin.getKey(),
                        "PAIRWISE26_Upgrade", TestService.EXAMPLE2,
                        TestPriceModel.EXAMPLE_PICT26_RATA_WEEKS,
                        example2Service, supplierMarketplace);
        serviceSetup.registerCompatibleServices(supplierAdmin.getKey(),
                serviceTemplate, unitTemplate);
        VOServiceDetails rataW = serviceSetup.savePriceModelForCustomer(
                unitTemplate, TestPriceModel.EXAMPLE_PICT26_RATA_WEEKS,
                customer);
        rataW = serviceSetup.activateMarketableService(rataW);

        setDateFactory("2013-08-08 15:00:00");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(subDetails, rataW);

        // parameter change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        upgradedSubDetails = subscrSetup.modifyParameterForSubscription(
                upgradedSubDetails,
                DateTimeHandling.calculateMillis("2013-09-01 14:00:00"),
                "MAX_FOLDER_NUMBER", "150");

        // role change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        setDateFactory("2013-09-01 15:00:00");
        upgradedSubDetails = subscrSetup.modifyUserRole(upgradedSubDetails
                .getUsageLicenses().get(0), VOServiceFactory.getRole(
                serviceDetails, "ADMIN"), upgradedSubDetails
                .getSubscriptionId());

        // change sunbscriptionID
        setDateFactory("2013-09-02 01:00:00");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        upgradedSubDetails.setSubscriptionId("PICT_TEST_26" + "_SubID2");
        upgradedSubDetails = subscrSetup.modifySubscription(upgradedSubDetails,
                null);

        // unsubscribe
        setDateFactory("2013-09-09 02:00:00");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        subscrSetup
                .unsubscribeToService(upgradedSubDetails.getSubscriptionId());

        resetCutOffDay(supplierAdmin.getKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_26", subDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_26", upgradedSubDetails);

        BillingIntegrationTestBase.updateCustomerListForTests("PICT_TEST_26",
                customer);

    }

    public void createPictScenario25() throws Exception {
        setDateFactory("2013-07-31 10:00:00");

        // Create a supplier with a marketplace
        String supplierAdminId = "Pict25Supplier";
        VOOrganization supplier = orgSetup.createOrganization(
                basicSetup.getPlatformOperatorUserKey(), supplierAdminId,
                "Pict25SupplierOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                OrganizationRoleType.TECHNOLOGY_PROVIDER,
                OrganizationRoleType.SUPPLIER);
        VOUser supplierAdmin = orgSetup.getUser(supplierAdminId, true);
        paymentSetup.createPaymentForSupplier(
                basicSetup.getPlatformOperatorUserKey(),
                supplierAdmin.getKey(), supplier);

        // Create own technical service
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        serviceSetup
                .importTechnicalServices(BaseAdmUmTest.TECHNICAL_SERVICE_EXAMPLE2_XML);
        VOTechnicalService example2Service = serviceSetup
                .getTechnicalService(VOTechServiceFactory.TECH_SERVICE_EXAMPLE2_ID);

        setCutOffDay(supplierAdmin.getKey(), 1);

        // Create a customer
        String customerAdminId = "Pict25Customer";
        VOOrganization customer = orgSetup.registerCustomer(
                "Pict25CustomerOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                customerAdminId, supplierMarketplace.getMarketplaceId(),
                supplier.getOrganizationId());
        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);

        orgSetup.createMarketingPermission(supplierAdmin.getKey(),
                supplier.getOrganizationId(), example2Service);

        // price model for customer
        VOServiceDetails serviceTemplate = serviceSetup
                .createAndPublishMarketableService(supplierAdmin.getKey(),
                        "PICT_TEST_25", TestService.EXAMPLE2,
                        TestPriceModel.EXAMPLE_PICT25_RATA_WEEKS,
                        example2Service, supplierMarketplace);
        VOServiceDetails serviceDetails = serviceSetup
                .savePriceModelForCustomer(serviceTemplate,
                        TestPriceModel.EXAMPLE_PICT25_RATA_WEEKS, customer);
        serviceDetails = serviceSetup.activateMarketableService(serviceDetails);

        // Vat
        orgSetup.saveAllVats(supplierAdmin.getKey(), VOVatRateFactory
                .newVOVatRate(new BigDecimal("13.00")), Arrays.asList(
                VOVatRateFactory.newVOCountryVatRate(new BigDecimal("20.00"),
                        TestOrganizationSetup.ORGANIZATION_DOMICILE_UK),
                VOVatRateFactory.newVOCountryVatRate(new BigDecimal("19.00"),
                        TestOrganizationSetup.ORGANIZATION_DOMICILE_DE)),
                Arrays.asList(VOVatRateFactory.newVOOrganizationVatRate(
                        new BigDecimal("5.00"), customer)));

        orgSetup.updateCustomerDiscount(customer, new BigDecimal("3.00"),
                DateTimeHandling.calculateMillis("2013-07-08 00:00:00"),
                DateTimeHandling.calculateMillis("2013-08-09 01:00:00"));

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "GUEST");
        // subscribe
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "PICT_TEST_25", serviceDetails, customerAdmin, role);

        // suspend/resume subscription several times by removing/restoring
        // customer's payment types
        setDateFactory("2013-07-31 23:00:00");

        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);

        paymentSetup.deleteCustomerPaymentTypes(customer);

        setDateFactory("2013-08-01 01:00:00");

        paymentSetup.reassignCustomerPaymentTypes(customer);

        subDetails = subscrSetup.getSubscriptionDetails(customerAdmin.getKey(),
                subDetails.getSubscriptionId());

        // parameter change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        subDetails = subscrSetup.modifyParameterForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-08-01 02:00:00"),
                "MAX_FOLDER_NUMBER", "50");
        // parameter change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        subDetails = subscrSetup.modifyParameterForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-08-01 03:00:00"),
                "MAX_FOLDER_NUMBER", "60");

        // role change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        setDateFactory("2013-08-01 04:00:00");
        subDetails = subscrSetup.modifyUserRole(subDetails.getUsageLicenses()
                .get(0), VOServiceFactory.getRole(serviceDetails, "ADMIN"),
                subDetails.getSubscriptionId());
        // role change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        setDateFactory("2013-08-01 05:00:00");
        subDetails = subscrSetup.modifyUserRole(subDetails.getUsageLicenses()
                .get(0), VOServiceFactory.getRole(serviceDetails, "USER"),
                subDetails.getSubscriptionId());

        // 1. upgrade service
        VOServiceDetails unitTemplate = serviceSetup
                .createAndPublishMarketableService(supplierAdmin.getKey(),
                        "PAIRWISE25_Upgrade", TestService.EXAMPLE2,
                        TestPriceModel.EXAMPLE_PICT26_UNIT_WEEKS,
                        example2Service, supplierMarketplace);
        serviceSetup.registerCompatibleServices(supplierAdmin.getKey(),
                serviceTemplate, unitTemplate);
        VOServiceDetails unitW1 = serviceSetup.savePriceModelForCustomer(
                unitTemplate, TestPriceModel.EXAMPLE_PICT26_UNIT_WEEKS,
                customer);
        unitW1 = serviceSetup.activateMarketableService(unitW1);

        setDateFactory("2013-08-01 15:00:00");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(subDetails, unitW1);

        // 2. upgrade service
        VOServiceDetails unitTemplate2 = serviceSetup
                .createAndPublishMarketableService(supplierAdmin.getKey(),
                        "PAIRWISE25_Upgrade2", TestService.EXAMPLE2,
                        TestPriceModel.EXAMPLE_PICT26_UNIT_WEEKS,
                        example2Service, supplierMarketplace);
        serviceSetup.registerCompatibleServices(supplierAdmin.getKey(),
                unitTemplate, unitTemplate2);
        VOServiceDetails unitw2 = serviceSetup.savePriceModelForCustomer(
                unitTemplate2, TestPriceModel.EXAMPLE_PICT26_UNIT_WEEKS,
                customer);
        unitw2 = serviceSetup.activateMarketableService(unitw2);

        setDateFactory("2013-08-02 15:00:00");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetails2 = subscrSetup
                .upgradeSubscription(upgradedSubDetails, unitw2);

        // suspend/resume subscription several times by removing/restoring
        // customer's payment types
        setDateFactory("2013-08-10 16:00:00");

        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);

        paymentSetup.deleteCustomerPaymentTypes(customer);

        setDateFactory("2013-08-20 13:00:00");

        paymentSetup.reassignCustomerPaymentTypes(customer);

        upgradedSubDetails2 = subscrSetup
                .getSubscriptionDetails(customerAdmin.getKey(),
                        upgradedSubDetails2.getSubscriptionId());

        // parameter change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        upgradedSubDetails2 = subscrSetup.modifyParameterForSubscription(
                upgradedSubDetails2,
                DateTimeHandling.calculateMillis("2013-09-01 14:00:00"),
                "MAX_FOLDER_NUMBER", "150");

        // role change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        setDateFactory("2013-09-01 15:00:00");
        upgradedSubDetails2 = subscrSetup.modifyUserRole(upgradedSubDetails
                .getUsageLicenses().get(0), VOServiceFactory.getRole(unitw2,
                "ADMIN"), upgradedSubDetails2.getSubscriptionId());

        // unsubscribe
        setDateFactory("2013-09-30 02:00:00");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        subscrSetup.unsubscribeToService(upgradedSubDetails2
                .getSubscriptionId());

        resetCutOffDay(supplierAdmin.getKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_25", subDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_25", upgradedSubDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_25", upgradedSubDetails2);

        BillingIntegrationTestBase.updateCustomerListForTests("PICT_TEST_25",
                customer);

    }

    public void createPictScenario25_1() throws Exception {
        setDateFactory("2013-07-31 10:00:00");

        // Create a supplier with a marketplace
        String supplierAdminId = "Pict25_1Supplier";
        VOOrganization supplier = orgSetup.createOrganization(
                basicSetup.getPlatformOperatorUserKey(), supplierAdminId,
                "Pict25_1SupplierOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                OrganizationRoleType.TECHNOLOGY_PROVIDER,
                OrganizationRoleType.SUPPLIER);
        VOUser supplierAdmin = orgSetup.getUser(supplierAdminId, true);
        paymentSetup.createPaymentForSupplier(
                basicSetup.getPlatformOperatorUserKey(),
                supplierAdmin.getKey(), supplier);

        // Create own technical service
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        serviceSetup
                .importTechnicalServices(BaseAdmUmTest.TECHNICAL_SERVICE_EXAMPLE2_XML);
        VOTechnicalService example2Service = serviceSetup
                .getTechnicalService(VOTechServiceFactory.TECH_SERVICE_EXAMPLE2_ID);

        setCutOffDay(supplierAdmin.getKey(), 1);

        // Create a customer
        String customerAdminId = "Pict25_1Customer";
        VOOrganization customer = orgSetup.registerCustomer(
                "Pict25_1CustomerOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                customerAdminId, supplierMarketplace.getMarketplaceId(),
                supplier.getOrganizationId());
        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);

        orgSetup.createMarketingPermission(supplierAdmin.getKey(),
                supplier.getOrganizationId(), example2Service);

        // price model for customer
        VOServiceDetails serviceTemplate = serviceSetup
                .createAndPublishMarketableService(supplierAdmin.getKey(),
                        "PICT_TEST_25_1", TestService.EXAMPLE2,
                        TestPriceModel.EXAMPLE_PICT25_RATA_WEEKS,
                        example2Service, supplierMarketplace);
        VOServiceDetails serviceDetails = serviceSetup
                .savePriceModelForCustomer(serviceTemplate,
                        TestPriceModel.EXAMPLE_PICT25_RATA_WEEKS, customer);
        serviceDetails = serviceSetup.activateMarketableService(serviceDetails);

        // Vat
        orgSetup.saveAllVats(supplierAdmin.getKey(), VOVatRateFactory
                .newVOVatRate(new BigDecimal("13.00")), Arrays.asList(
                VOVatRateFactory.newVOCountryVatRate(new BigDecimal("20.00"),
                        TestOrganizationSetup.ORGANIZATION_DOMICILE_UK),
                VOVatRateFactory.newVOCountryVatRate(new BigDecimal("19.00"),
                        TestOrganizationSetup.ORGANIZATION_DOMICILE_DE)),
                Arrays.asList(VOVatRateFactory.newVOOrganizationVatRate(
                        new BigDecimal("5.00"), customer)));

        orgSetup.updateCustomerDiscount(customer, new BigDecimal("3.00"),
                DateTimeHandling.calculateMillis("2013-07-08 00:00:00"),
                DateTimeHandling.calculateMillis("2013-08-09 01:00:00"));

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "GUEST");
        // subscribe
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "PICT_TEST_25_1", serviceDetails, customerAdmin, role);

        // suspend/resume subscription several times by removing/restoring
        // customer's payment types
        setDateFactory("2013-07-31 23:00:00");

        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);

        paymentSetup.deleteCustomerPaymentTypes(customer);

        setDateFactory("2013-08-01 01:00:00");

        paymentSetup.reassignCustomerPaymentTypes(customer);

        subDetails = subscrSetup.getSubscriptionDetails(customerAdmin.getKey(),
                subDetails.getSubscriptionId());

        // parameter change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        subDetails = subscrSetup.modifyParameterForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-08-01 02:00:00"),
                "MAX_FOLDER_NUMBER", "50");
        // parameter change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        subDetails = subscrSetup.modifyParameterForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-08-01 03:00:00"),
                "MAX_FOLDER_NUMBER", "60");

        // role change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        setDateFactory("2013-08-01 04:00:00");
        subDetails = subscrSetup.modifyUserRole(subDetails.getUsageLicenses()
                .get(0), VOServiceFactory.getRole(serviceDetails, "ADMIN"),
                subDetails.getSubscriptionId());
        // role change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        setDateFactory("2013-08-01 05:00:00");
        subDetails = subscrSetup.modifyUserRole(subDetails.getUsageLicenses()
                .get(0), VOServiceFactory.getRole(serviceDetails, "USER"),
                subDetails.getSubscriptionId());

        // 1. upgrade service
        VOServiceDetails unitTemplate = serviceSetup
                .createAndPublishMarketableService(supplierAdmin.getKey(),
                        "PAIRWISE25_1_Upgrade", TestService.EXAMPLE2,
                        TestPriceModel.EXAMPLE_PICT26_UNIT_WEEKS,
                        example2Service, supplierMarketplace);
        serviceSetup.registerCompatibleServices(supplierAdmin.getKey(),
                serviceTemplate, unitTemplate);
        VOServiceDetails unitW1 = serviceSetup.savePriceModelForCustomer(
                unitTemplate, TestPriceModel.EXAMPLE_PICT26_UNIT_WEEKS,
                customer);
        unitW1 = serviceSetup.activateMarketableService(unitW1);

        setDateFactory("2013-08-01 15:00:00");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(subDetails, unitW1);

        // 2. upgrade service
        VOServiceDetails unitTemplate2 = serviceSetup
                .createAndPublishMarketableService(supplierAdmin.getKey(),
                        "PAIRWISE25_1_Upgrade2", TestService.EXAMPLE2,
                        TestPriceModel.EXAMPLE_PICT25_RATA_WEEKS,
                        example2Service, supplierMarketplace);
        serviceSetup.registerCompatibleServices(supplierAdmin.getKey(),
                unitTemplate, unitTemplate2);
        VOServiceDetails unitw2 = serviceSetup.savePriceModelForCustomer(
                unitTemplate2, TestPriceModel.EXAMPLE_PICT25_RATA_WEEKS,
                customer);
        unitw2 = serviceSetup.activateMarketableService(unitw2);

        setDateFactory("2013-08-02 15:00:00");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetails2 = subscrSetup
                .upgradeSubscription(upgradedSubDetails, unitw2);

        // suspend/resume subscription several times by removing/restoring
        // customer's payment types
        setDateFactory("2013-08-10 16:00:00");

        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);

        paymentSetup.deleteCustomerPaymentTypes(customer);

        setDateFactory("2013-08-20 13:00:00");

        paymentSetup.reassignCustomerPaymentTypes(customer);

        upgradedSubDetails2 = subscrSetup
                .getSubscriptionDetails(customerAdmin.getKey(),
                        upgradedSubDetails2.getSubscriptionId());

        // parameter change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        upgradedSubDetails2 = subscrSetup.modifyParameterForSubscription(
                upgradedSubDetails2,
                DateTimeHandling.calculateMillis("2013-09-01 14:00:00"),
                "MAX_FOLDER_NUMBER", "150");

        // role change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        setDateFactory("2013-09-01 15:00:00");
        upgradedSubDetails2 = subscrSetup.modifyUserRole(upgradedSubDetails
                .getUsageLicenses().get(0), VOServiceFactory.getRole(unitw2,
                "ADMIN"), upgradedSubDetails2.getSubscriptionId());

        // unsubscribe
        setDateFactory("2013-09-30 02:00:00");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        subscrSetup.unsubscribeToService(upgradedSubDetails2
                .getSubscriptionId());

        resetCutOffDay(supplierAdmin.getKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_25_1", subDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_25_1", upgradedSubDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_25_1", upgradedSubDetails2);

        BillingIntegrationTestBase.updateCustomerListForTests("PICT_TEST_25_1",
                customer);

    }

    public void createPictScenario25_2() throws Exception {
        setDateFactory("2013-07-31 10:00:00");

        // Create a supplier with a marketplace
        String supplierAdminId = "Pict25_2Supplier";
        VOOrganization supplier = orgSetup.createOrganization(
                basicSetup.getPlatformOperatorUserKey(), supplierAdminId,
                "Pict25_2SupplierOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                OrganizationRoleType.TECHNOLOGY_PROVIDER,
                OrganizationRoleType.SUPPLIER);
        VOUser supplierAdmin = orgSetup.getUser(supplierAdminId, true);
        paymentSetup.createPaymentForSupplier(
                basicSetup.getPlatformOperatorUserKey(),
                supplierAdmin.getKey(), supplier);

        // Create own technical service
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        serviceSetup
                .importTechnicalServices(BaseAdmUmTest.TECHNICAL_SERVICE_EXAMPLE2_XML);
        VOTechnicalService example2Service = serviceSetup
                .getTechnicalService(VOTechServiceFactory.TECH_SERVICE_EXAMPLE2_ID);

        setCutOffDay(supplierAdmin.getKey(), 1);

        // Create a customer
        String customerAdminId = "Pict25_2Customer";
        VOOrganization customer = orgSetup.registerCustomer(
                "Pict25_2CustomerOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                customerAdminId, supplierMarketplace.getMarketplaceId(),
                supplier.getOrganizationId());
        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);

        orgSetup.createMarketingPermission(supplierAdmin.getKey(),
                supplier.getOrganizationId(), example2Service);

        // price model for customer
        VOServiceDetails serviceTemplate = serviceSetup
                .createAndPublishMarketableService(supplierAdmin.getKey(),
                        "PICT_TEST_25_2", TestService.EXAMPLE2,
                        TestPriceModel.EXAMPLE_PICT25_RATA_WEEKS,
                        example2Service, supplierMarketplace);
        VOServiceDetails serviceDetails = serviceSetup
                .savePriceModelForCustomer(serviceTemplate,
                        TestPriceModel.EXAMPLE_PICT25_RATA_WEEKS, customer);
        serviceDetails = serviceSetup.activateMarketableService(serviceDetails);

        // Vat
        orgSetup.saveAllVats(supplierAdmin.getKey(), VOVatRateFactory
                .newVOVatRate(new BigDecimal("13.00")), Arrays.asList(
                VOVatRateFactory.newVOCountryVatRate(new BigDecimal("20.00"),
                        TestOrganizationSetup.ORGANIZATION_DOMICILE_UK),
                VOVatRateFactory.newVOCountryVatRate(new BigDecimal("19.00"),
                        TestOrganizationSetup.ORGANIZATION_DOMICILE_DE)),
                Arrays.asList(VOVatRateFactory.newVOOrganizationVatRate(
                        new BigDecimal("5.00"), customer)));

        orgSetup.updateCustomerDiscount(customer, new BigDecimal("3.00"),
                DateTimeHandling.calculateMillis("2013-07-08 00:00:00"),
                DateTimeHandling.calculateMillis("2013-08-09 01:00:00"));

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "GUEST");
        // subscribe
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "PICT_TEST_25_2", serviceDetails, customerAdmin, role);

        // 1. upgrade service
        VOServiceDetails unitTemplate = serviceSetup
                .createAndPublishMarketableService(supplierAdmin.getKey(),
                        "PAIRWISE25_2_Upgrade", TestService.EXAMPLE2,
                        TestPriceModel.EXAMPLE_PICT25_RATA_WEEKS,
                        example2Service, supplierMarketplace);
        serviceSetup.registerCompatibleServices(supplierAdmin.getKey(),
                serviceTemplate, unitTemplate);
        VOServiceDetails unitW1 = serviceSetup.savePriceModelForCustomer(
                unitTemplate, TestPriceModel.EXAMPLE_PICT25_RATA_WEEKS,
                customer);
        unitW1 = serviceSetup.activateMarketableService(unitW1);

        setDateFactory("2013-08-10 15:00:00");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(subDetails, unitW1);

        resetCutOffDay(supplierAdmin.getKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_25_2", subDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_25_2", upgradedSubDetails);

        BillingIntegrationTestBase.updateCustomerListForTests("PICT_TEST_25_2",
                customer);

    }

    public void createPictScenario25_3() throws Exception {
        setDateFactory("2013-07-31 10:00:00");

        // Create a supplier with a marketplace
        String supplierAdminId = "Pict25_3Supplier";
        VOOrganization supplier = orgSetup.createOrganization(
                basicSetup.getPlatformOperatorUserKey(), supplierAdminId,
                "Pict25_3SupplierOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                OrganizationRoleType.TECHNOLOGY_PROVIDER,
                OrganizationRoleType.SUPPLIER);
        VOUser supplierAdmin = orgSetup.getUser(supplierAdminId, true);
        paymentSetup.createPaymentForSupplier(
                basicSetup.getPlatformOperatorUserKey(),
                supplierAdmin.getKey(), supplier);

        // Create own technical service
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        serviceSetup
                .importTechnicalServices(BaseAdmUmTest.TECHNICAL_SERVICE_EXAMPLE2_XML);
        VOTechnicalService example2Service = serviceSetup
                .getTechnicalService(VOTechServiceFactory.TECH_SERVICE_EXAMPLE2_ID);

        setCutOffDay(supplierAdmin.getKey(), 1);

        // Create a customer
        String customerAdminId = "Pict25_3Customer";
        VOOrganization customer = orgSetup.registerCustomer(
                "Pict25_3CustomerOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                customerAdminId, supplierMarketplace.getMarketplaceId(),
                supplier.getOrganizationId());
        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);

        orgSetup.createMarketingPermission(supplierAdmin.getKey(),
                supplier.getOrganizationId(), example2Service);

        // price model for customer
        VOServiceDetails serviceTemplate = serviceSetup
                .createAndPublishMarketableService(supplierAdmin.getKey(),
                        "PICT_TEST_25_3", TestService.EXAMPLE2,
                        TestPriceModel.EXAMPLE_PICT25_3_RATA_WEEKS,
                        example2Service, supplierMarketplace);
        VOServiceDetails serviceDetails = serviceSetup
                .savePriceModelForCustomer(serviceTemplate,
                        TestPriceModel.EXAMPLE_PICT25_3_RATA_WEEKS, customer);
        serviceDetails = serviceSetup.activateMarketableService(serviceDetails);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "GUEST");
        // subscribe
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "PICT_TEST_25_3", serviceDetails, customerAdmin, role);

        resetCutOffDay(supplierAdmin.getKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_25_3", subDetails);

        BillingIntegrationTestBase.updateCustomerListForTests("PICT_TEST_25_3",
                customer);

    }

    public void createPictScenario24() throws Exception {
        setDateFactory("2013-10-01 10:00:00");

        // Create a supplier with a marketplace
        String supplierAdminId = "Pict24Supplier";
        VOOrganization supplier = orgSetup.createOrganization(
                basicSetup.getPlatformOperatorUserKey(), supplierAdminId,
                "Pict24SupplierOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                OrganizationRoleType.TECHNOLOGY_PROVIDER,
                OrganizationRoleType.SUPPLIER);
        VOUser supplierAdmin = orgSetup.getUser(supplierAdminId, true);
        paymentSetup.createPaymentForSupplier(
                basicSetup.getPlatformOperatorUserKey(),
                supplierAdmin.getKey(), supplier);

        // Create own technical service
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        serviceSetup
                .importTechnicalServices(BaseAdmUmTest.TECHNICAL_SERVICE_EXAMPLE2_XML);
        VOTechnicalService example2Service = serviceSetup
                .getTechnicalService(VOTechServiceFactory.TECH_SERVICE_EXAMPLE2_ID);

        setCutOffDay(supplierAdmin.getKey(), 2);

        // Create a customer
        String customerAdminId = "Pict24Customer";
        VOOrganization customer = orgSetup.registerCustomer(
                "Pict24CustomerOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                customerAdminId, supplierMarketplace.getMarketplaceId(),
                supplier.getOrganizationId());
        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);

        orgSetup.createMarketingPermission(supplierAdmin.getKey(),
                supplier.getOrganizationId(), example2Service);

        // price model for customer
        VOServiceDetails serviceTemplate = serviceSetup
                .createAndPublishMarketableService(supplierAdmin.getKey(),
                        "PICT_TEST_24", TestService.EXAMPLE2,
                        TestPriceModel.EXAMPLE_PICT24_RATA_MONTH,
                        example2Service, supplierMarketplace);
        VOServiceDetails serviceDetails = serviceSetup
                .savePriceModelForCustomer(serviceTemplate,
                        TestPriceModel.EXAMPLE_PICT24_RATA_MONTH, customer);
        serviceDetails = serviceSetup.activateMarketableService(serviceDetails);

        // Vat
        orgSetup.saveAllVats(supplierAdmin.getKey(), VOVatRateFactory
                .newVOVatRate(new BigDecimal("13.00")), Arrays.asList(
                VOVatRateFactory.newVOCountryVatRate(new BigDecimal("20.00"),
                        TestOrganizationSetup.ORGANIZATION_DOMICILE_UK),
                VOVatRateFactory.newVOCountryVatRate(new BigDecimal("19.00"),
                        TestOrganizationSetup.ORGANIZATION_DOMICILE_DE)), null);

        orgSetup.updateCustomerDiscount(customer, new BigDecimal("3.00"),
                DateTimeHandling.calculateMillis("2013-09-08 00:00:00"),
                DateTimeHandling.calculateMillis("2013-11-09 01:00:00"));

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "GUEST");
        // subscribe
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "PICT_TEST_24", serviceDetails, customerAdmin, role);

        // 1. upgrade service
        VOServiceDetails unitTemplate = serviceSetup
                .createAndPublishMarketableService(supplierAdmin.getKey(),
                        "PAIRWISE24_Upgrade", TestService.EXAMPLE2,
                        TestPriceModel.EXAMPLE_PICT24_UNIT_HOUR,
                        example2Service, supplierMarketplace);
        serviceSetup.registerCompatibleServices(supplierAdmin.getKey(),
                serviceTemplate, unitTemplate);
        VOServiceDetails unitH1 = serviceSetup
                .savePriceModelForCustomer(unitTemplate,
                        TestPriceModel.EXAMPLE_PICT24_UNIT_HOUR, customer);
        unitH1 = serviceSetup.activateMarketableService(unitH1);

        setDateFactory("2013-10-06 00:00:00");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(subDetails, unitH1);

        // role change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        setDateFactory("2013-10-06 00:00:01");
        subDetails = subscrSetup.modifyUserRole(subDetails.getUsageLicenses()
                .get(0), VOServiceFactory.getRole(serviceDetails, "ADMIN"),
                subDetails.getSubscriptionId());
        // role change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        setDateFactory("2013-10-06 00:01:01");
        subDetails = subscrSetup.modifyUserRole(subDetails.getUsageLicenses()
                .get(0), VOServiceFactory.getRole(serviceDetails, "USER"),
                subDetails.getSubscriptionId());

        // 2. upgrade service
        VOServiceDetails unitTemplate2 = serviceSetup
                .createAndPublishMarketableService(supplierAdmin.getKey(),
                        "PAIRWISE24_Upgrade2", TestService.EXAMPLE2,
                        TestPriceModel.EXAMPLE_PICT24_UNIT_HOUR,
                        example2Service, supplierMarketplace);
        serviceSetup.registerCompatibleServices(supplierAdmin.getKey(),
                unitTemplate, unitTemplate2);
        VOServiceDetails unitH2 = serviceSetup.savePriceModelForCustomer(
                unitTemplate2, TestPriceModel.EXAMPLE_PICT24_UNIT_HOUR,
                customer);
        unitH2 = serviceSetup.activateMarketableService(unitH2);

        setDateFactory("2013-10-06 01:15:00");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetails2 = subscrSetup
                .upgradeSubscription(upgradedSubDetails, unitH2);

        // suspend/resume subscription several times by removing/restoring
        // customer's payment types
        setDateFactory("2013-10-06 01:15:01");

        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);

        paymentSetup.deleteCustomerPaymentTypes(customer);

        setDateFactory("2013-10-06 01:15:11");

        paymentSetup.reassignCustomerPaymentTypes(customer);

        upgradedSubDetails2 = subscrSetup
                .getSubscriptionDetails(customerAdmin.getKey(),
                        upgradedSubDetails2.getSubscriptionId());

        // change sunbscriptionID
        setDateFactory("2013-10-06 01:15:12");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        upgradedSubDetails2.setSubscriptionId("PICT_TEST_24" + "_SubID2");
        upgradedSubDetails2 = subscrSetup.modifySubscription(
                upgradedSubDetails2, null);

        // price model change

        long priceModelChangeTime = DateTimeHandling
                .calculateMillis("2013-10-06 01:15:13");
        BillingIntegrationTestBase.setDateFactoryInstance(priceModelChangeTime);
        VOPriceModel newSubPriceModel = VOPriceModelFactory
                .modifyPriceModelPeriodFee(upgradedSubDetails2.getPriceModel(),
                        new BigDecimal("2.00"));

        subscrSetup.savePriceModelForSubscription(supplierAdmin.getKey(),
                upgradedSubDetails2, newSubPriceModel, customer);

        // user is assigned
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VORoleDefinition changedRole = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        setDateFactory("2013-10-06 01:15:14");
        upgradedSubDetails2 = subscrSetup.addUser(
                basicSetup.getSecondCustomerUser1(), changedRole,
                upgradedSubDetails2.getSubscriptionId());

        // user is deassigned
        setDateFactory("2013-10-06 01:15:15");
        subscrSetup.revokeUser(basicSetup.getSecondCustomerUser1(),
                upgradedSubDetails2.getSubscriptionId());

        // unsubscribe
        setDateFactory("2013-10-29 02:15:00");
        subscrSetup.unsubscribeToService(upgradedSubDetails2
                .getSubscriptionId());

        resetCutOffDay(supplierAdmin.getKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_24", subDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_24", upgradedSubDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_24", upgradedSubDetails2);

        BillingIntegrationTestBase.updateCustomerListForTests("PICT_TEST_24",
                customer);

    }

    public void createPictScenario24_1() throws Exception {
        setDateFactory("2013-10-01 10:00:00");

        // Create a supplier with a marketplace
        String supplierAdminId = "Pict24_1Supplier";
        VOOrganization supplier = orgSetup.createOrganization(
                basicSetup.getPlatformOperatorUserKey(), supplierAdminId,
                "Pict24_1SupplierOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                OrganizationRoleType.TECHNOLOGY_PROVIDER,
                OrganizationRoleType.SUPPLIER);
        VOUser supplierAdmin = orgSetup.getUser(supplierAdminId, true);
        paymentSetup.createPaymentForSupplier(
                basicSetup.getPlatformOperatorUserKey(),
                supplierAdmin.getKey(), supplier);

        // Create own technical service
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        serviceSetup
                .importTechnicalServices(BaseAdmUmTest.TECHNICAL_SERVICE_EXAMPLE2_XML);
        VOTechnicalService example2Service = serviceSetup
                .getTechnicalService(VOTechServiceFactory.TECH_SERVICE_EXAMPLE2_ID);

        setCutOffDay(supplierAdmin.getKey(), 2);

        // Create a customer
        String customerAdminId = "Pict24_1Customer";
        VOOrganization customer = orgSetup.registerCustomer(
                "Pict24_1CustomerOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                customerAdminId, supplierMarketplace.getMarketplaceId(),
                supplier.getOrganizationId());
        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);

        orgSetup.createMarketingPermission(supplierAdmin.getKey(),
                supplier.getOrganizationId(), example2Service);

        // price model for customer
        VOServiceDetails serviceTemplate = serviceSetup
                .createAndPublishMarketableService(supplierAdmin.getKey(),
                        "PICT_TEST_24_1", TestService.EXAMPLE2,
                        TestPriceModel.EXAMPLE_PICT24_RATA_MONTH,
                        example2Service, supplierMarketplace);
        VOServiceDetails serviceDetails = serviceSetup
                .savePriceModelForCustomer(serviceTemplate,
                        TestPriceModel.EXAMPLE_PICT24_RATA_MONTH, customer);
        serviceDetails = serviceSetup.activateMarketableService(serviceDetails);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "GUEST");
        // subscribe
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "PICT_TEST_24_1", serviceDetails, customerAdmin, role);

        // 1. upgrade service
        VOServiceDetails unitTemplate = serviceSetup
                .createAndPublishMarketableService(supplierAdmin.getKey(),
                        "PAIRWISE24_1_Upgrade", TestService.EXAMPLE2,
                        TestPriceModel.EXAMPLE_PICT24_UNIT_HOUR,
                        example2Service, supplierMarketplace);
        serviceSetup.registerCompatibleServices(supplierAdmin.getKey(),
                serviceTemplate, unitTemplate);
        VOServiceDetails unitH1 = serviceSetup
                .savePriceModelForCustomer(unitTemplate,
                        TestPriceModel.EXAMPLE_PICT24_UNIT_HOUR, customer);
        unitH1 = serviceSetup.activateMarketableService(unitH1);

        setDateFactory("2013-10-06 00:00:00");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(subDetails, unitH1);

        // role change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        setDateFactory("2013-10-06 00:00:01");
        subDetails = subscrSetup.modifyUserRole(subDetails.getUsageLicenses()
                .get(0), VOServiceFactory.getRole(serviceDetails, "ADMIN"),
                subDetails.getSubscriptionId());
        // role change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        setDateFactory("2013-10-06 00:01:01");
        subDetails = subscrSetup.modifyUserRole(subDetails.getUsageLicenses()
                .get(0), VOServiceFactory.getRole(serviceDetails, "USER"),
                subDetails.getSubscriptionId());

        // 2. upgrade service
        VOServiceDetails unitTemplate2 = serviceSetup
                .createAndPublishMarketableService(supplierAdmin.getKey(),
                        "PAIRWISE24_1_Upgrade2", TestService.EXAMPLE2,
                        TestPriceModel.EXAMPLE_PICT24_UNIT_HOUR,
                        example2Service, supplierMarketplace);
        serviceSetup.registerCompatibleServices(supplierAdmin.getKey(),
                unitTemplate, unitTemplate2);
        VOServiceDetails unitH2 = serviceSetup.savePriceModelForCustomer(
                unitTemplate2, TestPriceModel.EXAMPLE_PICT24_UNIT_HOUR,
                customer);
        unitH2 = serviceSetup.activateMarketableService(unitH2);

        setDateFactory("2013-10-08 01:15:00");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetails2 = subscrSetup
                .upgradeSubscription(upgradedSubDetails, unitH2);

        // change sunbscriptionID
        setDateFactory("2013-10-08 01:15:12");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        upgradedSubDetails2.setSubscriptionId("PICT_TEST_24_1" + "_SubID2");
        upgradedSubDetails2 = subscrSetup.modifySubscription(
                upgradedSubDetails2, null);

        // price model change

        long priceModelChangeTime = DateTimeHandling
                .calculateMillis("2013-10-08 01:15:13");
        BillingIntegrationTestBase.setDateFactoryInstance(priceModelChangeTime);
        VOPriceModel newSubPriceModel = VOPriceModelFactory
                .modifyPriceModelPeriodFee(upgradedSubDetails2.getPriceModel(),
                        new BigDecimal("2.00"));

        subscrSetup.savePriceModelForSubscription(supplierAdmin.getKey(),
                upgradedSubDetails2, newSubPriceModel, customer);

        // user is assigned
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VORoleDefinition changedRole = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        setDateFactory("2013-10-08 01:15:14");
        upgradedSubDetails2 = subscrSetup.addUser(
                basicSetup.getSecondCustomerUser1(), changedRole,
                upgradedSubDetails2.getSubscriptionId());

        // user is deassigned
        setDateFactory("2013-10-08 01:15:15");
        subscrSetup.revokeUser(basicSetup.getSecondCustomerUser1(),
                upgradedSubDetails2.getSubscriptionId());

        // unsubscribe
        setDateFactory("2013-10-29 02:15:00");
        subscrSetup.unsubscribeToService(upgradedSubDetails2
                .getSubscriptionId());

        resetCutOffDay(supplierAdmin.getKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_24_1", subDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_24_1", upgradedSubDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_24_1", upgradedSubDetails2);

        BillingIntegrationTestBase.updateCustomerListForTests("PICT_TEST_24_1",
                customer);

    }

    public void createPictScenario23() throws Exception {
        setDateFactory("2013-09-29 10:00:00");

        // Create a supplier with a marketplace
        String supplierAdminId = "Pict23Supplier";
        VOOrganization supplier = orgSetup.createOrganization(
                basicSetup.getPlatformOperatorUserKey(), supplierAdminId,
                "Pict23SupplierOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                OrganizationRoleType.TECHNOLOGY_PROVIDER,
                OrganizationRoleType.SUPPLIER);
        VOUser supplierAdmin = orgSetup.getUser(supplierAdminId, true);
        paymentSetup.createPaymentForSupplier(
                basicSetup.getPlatformOperatorUserKey(),
                supplierAdmin.getKey(), supplier);

        // Create own technical service
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        serviceSetup
                .importTechnicalServices(BaseAdmUmTest.TECHNICAL_SERVICE_EXAMPLE2_XML);
        VOTechnicalService example2Service = serviceSetup
                .getTechnicalService(VOTechServiceFactory.TECH_SERVICE_EXAMPLE2_ID);

        setCutOffDay(supplierAdmin.getKey(), 1);

        // Create a customer
        String customerAdminId = "Pict23Customer";
        VOOrganization customer = orgSetup.registerCustomer(
                "Pict23CustomerOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                customerAdminId, supplierMarketplace.getMarketplaceId(),
                supplier.getOrganizationId());
        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);

        orgSetup.createMarketingPermission(supplierAdmin.getKey(),
                supplier.getOrganizationId(), example2Service);

        // price model for customer
        VOServiceDetails serviceTemplate = serviceSetup
                .createAndPublishMarketableService(supplierAdmin.getKey(),
                        "PICT_TEST_23", TestService.EXAMPLE2,
                        TestPriceModel.FREE, example2Service,
                        supplierMarketplace);
        VOServiceDetails serviceDetails = serviceSetup
                .savePriceModelForCustomer(serviceTemplate,
                        TestPriceModel.FREE, customer);
        serviceDetails = serviceSetup.activateMarketableService(serviceDetails);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "GUEST");
        // subscribe
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "PICT_TEST_23", serviceDetails, customerAdmin, role);

        // 1. upgrade service
        VOServiceDetails unitTemplate = serviceSetup
                .createAndPublishMarketableService(supplierAdmin.getKey(),
                        "PAIRWISE23_Upgrade", TestService.EXAMPLE2,
                        TestPriceModel.EXAMPLE_PICT24_UNIT_HOUR,
                        example2Service, supplierMarketplace);
        serviceSetup.registerCompatibleServices(supplierAdmin.getKey(),
                serviceTemplate, unitTemplate);
        VOServiceDetails unitH1 = serviceSetup
                .savePriceModelForCustomer(unitTemplate,
                        TestPriceModel.EXAMPLE_PICT24_UNIT_HOUR, customer);
        unitH1 = serviceSetup.activateMarketableService(unitH1);

        setDateFactory("2013-10-09 00:00:00");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(subDetails, unitH1);

        // role change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        setDateFactory("2013-10-09 00:00:01");
        upgradedSubDetails = subscrSetup.modifyUserRole(upgradedSubDetails
                .getUsageLicenses().get(0), VOServiceFactory.getRole(unitH1,
                "ADMIN"), subDetails.getSubscriptionId());
        // role change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        setDateFactory("2013-10-16 00:01:01");
        upgradedSubDetails = subscrSetup.modifyUserRole(upgradedSubDetails
                .getUsageLicenses().get(0), VOServiceFactory.getRole(unitH1,
                "USER"), upgradedSubDetails.getSubscriptionId());

        // 2. upgrade service
        VOServiceDetails unitTemplate2 = serviceSetup
                .createAndPublishMarketableService(supplierAdmin.getKey(),
                        "PAIRWISE23_Upgrade2", TestService.EXAMPLE2,
                        TestPriceModel.FREE, example2Service,
                        supplierMarketplace);
        serviceSetup.registerCompatibleServices(supplierAdmin.getKey(),
                unitTemplate, unitTemplate2);
        VOServiceDetails unitH2 = serviceSetup.savePriceModelForCustomer(
                unitTemplate2, TestPriceModel.FREE, customer);
        unitH2 = serviceSetup.activateMarketableService(unitH2);

        setDateFactory("2013-10-28 00:00:00");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetails2 = subscrSetup
                .upgradeSubscription(upgradedSubDetails, unitH2);

        // suspend/resume subscription several times by removing/restoring
        // customer's payment types
        setDateFactory("2013-10-28 01:15:01");

        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);

        paymentSetup.deleteCustomerPaymentTypes(customer);

        setDateFactory("2013-10-28 01:15:11");

        paymentSetup.reassignCustomerPaymentTypes(customer);

        upgradedSubDetails2 = subscrSetup
                .getSubscriptionDetails(customerAdmin.getKey(),
                        upgradedSubDetails2.getSubscriptionId());

        // change sunbscriptionID
        setDateFactory("2013-10-28 01:15:12");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        upgradedSubDetails2.setSubscriptionId("PICT_TEST_23" + "_SubID2");
        upgradedSubDetails2 = subscrSetup.modifySubscription(
                upgradedSubDetails2, null);

        resetCutOffDay(supplierAdmin.getKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_23", subDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_23", upgradedSubDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_23", upgradedSubDetails2);

        BillingIntegrationTestBase.updateCustomerListForTests("PICT_TEST_23",
                customer);

    }

    public void createPictScenario22() throws Exception {
        setDateFactory("2013-09-29 10:00:00");

        // Create a supplier with a marketplace
        String supplierAdminId = "Pict22Supplier";
        VOOrganization supplier = orgSetup.createOrganization(
                basicSetup.getPlatformOperatorUserKey(), supplierAdminId,
                "Pict22SupplierOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                OrganizationRoleType.TECHNOLOGY_PROVIDER,
                OrganizationRoleType.SUPPLIER);
        VOUser supplierAdmin = orgSetup.getUser(supplierAdminId, true);
        paymentSetup.createPaymentForSupplier(
                basicSetup.getPlatformOperatorUserKey(),
                supplierAdmin.getKey(), supplier);

        // Create own technical service
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        serviceSetup
                .importTechnicalServices(BaseAdmUmTest.TECHNICAL_SERVICE_EXAMPLE2_XML);
        VOTechnicalService example2Service = serviceSetup
                .getTechnicalService(VOTechServiceFactory.TECH_SERVICE_EXAMPLE2_ID);

        setCutOffDay(supplierAdmin.getKey(), 15);

        // Create a customer
        String customerAdminId = "Pict22Customer";
        VOOrganization customer = orgSetup.registerCustomer(
                "Pict22CustomerOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                customerAdminId, supplierMarketplace.getMarketplaceId(),
                supplier.getOrganizationId());
        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);

        orgSetup.createMarketingPermission(supplierAdmin.getKey(),
                supplier.getOrganizationId(), example2Service);

        // price model for customer
        VOServiceDetails serviceTemplate = serviceSetup
                .createAndPublishMarketableService(supplierAdmin.getKey(),
                        "PICT_TEST_22", TestService.EXAMPLE2,
                        TestPriceModel.FREE, example2Service,
                        supplierMarketplace);
        VOServiceDetails serviceDetails = serviceSetup
                .savePriceModelForCustomer(serviceTemplate,
                        TestPriceModel.FREE, customer);
        serviceDetails = serviceSetup.activateMarketableService(serviceDetails);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "GUEST");
        // subscribe
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "PICT_TEST_22", serviceDetails, customerAdmin, role);

        // 1. upgrade service
        VOServiceDetails unitTemplate = serviceSetup
                .createAndPublishMarketableService(supplierAdmin.getKey(),
                        "PAIRWISE22_Upgrade", TestService.EXAMPLE2,
                        TestPriceModel.EXAMPLE_PICT22_RATA_HOUR,
                        example2Service, supplierMarketplace);
        serviceSetup.registerCompatibleServices(supplierAdmin.getKey(),
                serviceTemplate, unitTemplate);
        VOServiceDetails unitH1 = serviceSetup
                .savePriceModelForCustomer(unitTemplate,
                        TestPriceModel.EXAMPLE_PICT22_RATA_HOUR, customer);
        unitH1 = serviceSetup.activateMarketableService(unitH1);

        setDateFactory("2013-10-27 00:00:00");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(subDetails, unitH1);

        // suspend/resume subscription several times by removing/restoring
        // customer's payment types
        setDateFactory("2013-10-27 01:15:01");

        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);

        paymentSetup.deleteCustomerPaymentTypes(customer);

        setDateFactory("2013-10-27 01:15:11");

        paymentSetup.reassignCustomerPaymentTypes(customer);

        upgradedSubDetails = subscrSetup.getSubscriptionDetails(
                customerAdmin.getKey(), upgradedSubDetails.getSubscriptionId());

        // parameter change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        upgradedSubDetails = subscrSetup.modifyParameterForSubscription(
                upgradedSubDetails,
                DateTimeHandling.calculateMillis("2013-10-27 03:00:01"),
                "HAS_OPTIONS", "1");

        // user is assigned

        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        setDateFactory("2013-10-27 03:15:00");
        VORoleDefinition changedRole = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        upgradedSubDetails = subscrSetup.addUser(
                basicSetup.getSecondCustomerUser1(), changedRole,
                upgradedSubDetails.getSubscriptionId());

        // user is deassigned
        setDateFactory("2013-10-27 03:30:00");
        upgradedSubDetails = subscrSetup.revokeUser(
                basicSetup.getSecondCustomerUser1(),
                upgradedSubDetails.getSubscriptionId());

        // change sunbscriptionID
        setDateFactory("2013-10-28 01:15:12");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        upgradedSubDetails.setSubscriptionId("PICT_TEST_22" + "_SubID2");
        upgradedSubDetails = subscrSetup.modifySubscription(upgradedSubDetails,
                null);

        resetCutOffDay(supplierAdmin.getKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_22", subDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_22", upgradedSubDetails);

        BillingIntegrationTestBase.updateCustomerListForTests("PICT_TEST_22",
                customer);

    }

    public void createPictScenario21() throws Exception {
        setDateFactory("2013-10-01 08:00:00");

        setCutOffDay(basicSetup.getSupplierAdminKey(), 3);

        // Create own customer
        String customerAdminId = "PICT21Customer";
        VOOrganization customer = orgSetup.registerCustomer(
                "PICT21CustomerOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                customerAdminId, supplierMarketplace.getMarketplaceId(),
                basicSetup.getSupplierOrgID());

        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(), "PICT_TEST_21",
                        TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PICT21_RATA_WEEK,
                        technicalService, supplierMarketplace);

        orgSetup.saveAllVats(basicSetup.getSupplierAdminKey(), VOVatRateFactory
                .newVOVatRate(new BigDecimal("13.00")), Arrays.asList(
                VOVatRateFactory.newVOCountryVatRate(new BigDecimal("20.00"),
                        TestOrganizationSetup.ORGANIZATION_DOMICILE_UK),
                VOVatRateFactory.newVOCountryVatRate(new BigDecimal("19.00"),
                        TestOrganizationSetup.ORGANIZATION_DOMICILE_DE)), null);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");

        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "PICT_TEST_21", serviceDetails, customerAdmin, role);

        // price model change

        setDateFactory("2013-10-01 09:00:00");

        VOPriceModel newSubPriceModel = VOPriceModelFactory
                .modifyPriceModelPeriodFee(subDetails.getPriceModel(),
                        new BigDecimal("2.00"));

        subscrSetup.savePriceModelForSubscription(
                basicSetup.getSupplierAdminKey(), subDetails, newSubPriceModel,
                customer);

        // parameter change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        subDetails = subscrSetup.modifyParameterForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-10-01 11:00:00"),
                "HAS_OPTIONS", "2");

        // parameter change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        subDetails = subscrSetup.modifyParameterForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-10-01 12:00:00"),
                "HAS_OPTIONS", "1");

        // // suspend/resume subscription several times by removing/restoring
        // // customer's payment types
        setDateFactory("2013-10-06 08:00:00");
        container.login(basicSetup.getSupplierAdminKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        paymentSetup.deleteCustomerPaymentTypes(customer);
        setDateFactory("2013-10-07 08:00:00");
        paymentSetup.reassignCustomerPaymentTypes(customer);

        subDetails = subscrSetup.getSubscriptionDetails(customerAdmin.getKey(),
                subDetails.getSubscriptionId());

        // user is assigned

        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        setDateFactory("2013-10-07 10:00:00");
        VORoleDefinition changedRole = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        subDetails = subscrSetup.addUser(basicSetup.getSecondCustomerUser1(),
                changedRole, subDetails.getSubscriptionId());

        // user is deassigned
        setDateFactory("2013-10-17 10:00:00");
        subDetails = subscrSetup.revokeUser(
                basicSetup.getSecondCustomerUser1(),
                subDetails.getSubscriptionId());

        // Events
        container.login(basicSetup.getSupplierAdminKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        subscrSetup.recordEventForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-10-17 10:00:10"),
                "FILE_DOWNLOAD", 15);

        // Events
        container.login(basicSetup.getSupplierAdminKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        subscrSetup.recordEventForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-10-17 10:00:20"),
                "FILE_DOWNLOAD", 15);

        // 1. create upgraded service
        VOServiceDetails freeService = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(), "P21_Upgrade",
                        TestService.EXAMPLE, TestPriceModel.FREE,
                        technicalService, supplierMarketplace);

        serviceSetup.registerCompatibleServices(
                basicSetup.getSupplierAdminKey(), serviceDetails, freeService);

        setDateFactory("2013-11-02 20:00:00");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(subDetails, freeService);

        // change sunbscriptionID
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-11-02 21:00:00"));
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        upgradedSubDetails.setSubscriptionId("PICT_TEST_21" + "_SubID2");
        upgradedSubDetails = subscrSetup.modifySubscription(upgradedSubDetails,
                null);

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_21", subDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_21", upgradedSubDetails);
        BillingIntegrationTestBase.updateCustomerListForTests("PICT_TEST_21",
                customer);

    }

    public void createPictScenario20() throws Exception {
        setDateFactory("2013-09-29 10:00:00");

        // Create a supplier with a marketplace
        String supplierAdminId = "Pict20Supplier";
        VOOrganization supplier = orgSetup.createOrganization(
                basicSetup.getPlatformOperatorUserKey(), supplierAdminId,
                "Pict20SupplierOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                OrganizationRoleType.TECHNOLOGY_PROVIDER,
                OrganizationRoleType.SUPPLIER);
        VOUser supplierAdmin = orgSetup.getUser(supplierAdminId, true);
        paymentSetup.createPaymentForSupplier(
                basicSetup.getPlatformOperatorUserKey(),
                supplierAdmin.getKey(), supplier);

        // Create own technical service
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        serviceSetup
                .importTechnicalServices(BaseAdmUmTest.TECHNICAL_SERVICE_EXAMPLE2_XML);
        VOTechnicalService example2Service = serviceSetup
                .getTechnicalService(VOTechServiceFactory.TECH_SERVICE_EXAMPLE2_ID);

        setCutOffDay(supplierAdmin.getKey(), 2);

        // Create a customer
        String customerAdminId = "Pict20Customer";
        VOOrganization customer = orgSetup.registerCustomer(
                "Pict20CustomerOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                customerAdminId, supplierMarketplace.getMarketplaceId(),
                supplier.getOrganizationId());
        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);

        orgSetup.createMarketingPermission(supplierAdmin.getKey(),
                supplier.getOrganizationId(), example2Service);

        // price model for customer
        VOServiceDetails serviceTemplate = serviceSetup
                .createAndPublishMarketableService(supplierAdmin.getKey(),
                        "PICT_TEST_20", TestService.EXAMPLE2,
                        TestPriceModel.FREE, example2Service,
                        supplierMarketplace);
        VOServiceDetails serviceDetails = serviceSetup
                .savePriceModelForCustomer(serviceTemplate,
                        TestPriceModel.FREE, customer);
        serviceDetails = serviceSetup.activateMarketableService(serviceDetails);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "GUEST");

        orgSetup.saveAllVats(supplierAdmin.getKey(),
                VOVatRateFactory.newVOVatRate(new BigDecimal("10.00")), //
                null, null);

        // subscribe
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "PICT_TEST_20", serviceDetails, customerAdmin, role);

        // 1. upgrade service
        VOServiceDetails unitTemplate = serviceSetup
                .createAndPublishMarketableService(supplierAdmin.getKey(),
                        "PAIRWISE20_Upgrade", TestService.EXAMPLE2,
                        TestPriceModel.EXAMPLE_PICT20_UNIT_MONTH,
                        example2Service, supplierMarketplace);
        serviceSetup.registerCompatibleServices(supplierAdmin.getKey(),
                serviceTemplate, unitTemplate);
        VOServiceDetails unitH1 = serviceSetup.savePriceModelForCustomer(
                unitTemplate, TestPriceModel.EXAMPLE_PICT20_UNIT_MONTH,
                customer);
        unitH1 = serviceSetup.activateMarketableService(unitH1);

        setDateFactory("2013-10-27 00:00:00");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(subDetails, unitH1);

        // suspend/resume subscription several times by removing/restoring
        // customer's payment types
        setDateFactory("2013-10-27 01:15:01");

        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);

        paymentSetup.deleteCustomerPaymentTypes(customer);

        setDateFactory("2013-10-27 01:15:11");

        paymentSetup.reassignCustomerPaymentTypes(customer);

        upgradedSubDetails = subscrSetup.getSubscriptionDetails(
                customerAdmin.getKey(), upgradedSubDetails.getSubscriptionId());

        // delete market service
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-10-28 00:15:12"));
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER);
        serviceSetup.deleteMarketableService(serviceDetails);

        // change sunbscriptionID
        setDateFactory("2013-10-28 01:15:12");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        upgradedSubDetails.setSubscriptionId("PICT_TEST_20" + "_SubID2");
        upgradedSubDetails = subscrSetup.modifySubscription(upgradedSubDetails,
                null);
        // role change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        setDateFactory("2013-10-29 00:00:00");
        upgradedSubDetails = subscrSetup.modifyUserRole(upgradedSubDetails
                .getUsageLicenses().get(0), VOServiceFactory.getRole(unitH1,
                "ADMIN"), upgradedSubDetails.getSubscriptionId());

        // Events
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        subscrSetup.recordEventForSubscription(upgradedSubDetails,
                DateTimeHandling.calculateMillis("2013-11-02 22:00:00"),
                "FILE_UPLOAD", 10);

        subscrSetup.recordEventForSubscription(upgradedSubDetails,
                DateTimeHandling.calculateMillis("2013-11-10 01:15:00"),
                "FILE_UPLOAD", 10);
        // unsubscribe
        setDateFactory("2013-11-10 02:15:00");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        subscrSetup
                .unsubscribeToService(upgradedSubDetails.getSubscriptionId());

        resetCutOffDay(supplierAdmin.getKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_20", subDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_20", upgradedSubDetails);

        BillingIntegrationTestBase.updateCustomerListForTests("PICT_TEST_20",
                customer);

    }

    public void createPictScenario19() throws Exception {
        setDateFactory("2013-03-30 01:00:00");

        // Create a supplier with a marketplace
        String supplierAdminId = "Pict19Supplier";
        VOOrganization supplier = orgSetup.createOrganization(
                basicSetup.getPlatformOperatorUserKey(), supplierAdminId,
                "Pict19SupplierOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                OrganizationRoleType.TECHNOLOGY_PROVIDER,
                OrganizationRoleType.SUPPLIER);
        VOUser supplierAdmin = orgSetup.getUser(supplierAdminId, true);
        paymentSetup.createPaymentForSupplier(
                basicSetup.getPlatformOperatorUserKey(),
                supplierAdmin.getKey(), supplier);

        // Create own technical service
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        serviceSetup
                .importTechnicalServices(BaseAdmUmTest.TECHNICAL_SERVICE_EXAMPLE2_XML);
        VOTechnicalService example2Service = serviceSetup
                .getTechnicalService(VOTechServiceFactory.TECH_SERVICE_EXAMPLE2_ID);

        setCutOffDay(supplierAdmin.getKey(), 1);

        // Create a customer
        String customerAdminId = "Pict19Customer";
        VOOrganization customer = orgSetup.registerCustomer(
                "Pict19CustomerOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                customerAdminId, supplierMarketplace.getMarketplaceId(),
                supplier.getOrganizationId());
        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);

        orgSetup.createMarketingPermission(supplierAdmin.getKey(),
                supplier.getOrganizationId(), example2Service);

        // price model for customer
        VOServiceDetails serviceTemplate = serviceSetup
                .createAndPublishMarketableService(supplierAdmin.getKey(),
                        "PICT_TEST_19", TestService.EXAMPLE2,
                        TestPriceModel.EXAMPLE_PICT19_UNIT_HOUR,
                        example2Service, supplierMarketplace);
        VOServiceDetails serviceDetails = serviceSetup
                .savePriceModelForCustomer(serviceTemplate,
                        TestPriceModel.EXAMPLE_PICT19_UNIT_HOUR, customer);
        serviceDetails = serviceSetup.activateMarketableService(serviceDetails);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "GUEST");

        // subscribe
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "PICT_TEST_19", serviceDetails, customerAdmin, role);

        // price model change

        setDateFactory("2013-03-31 01:00:01");

        VOPriceModel newSubPriceModel = VOPriceModelFactory
                .modifyPriceModelPeriodFee(subDetails.getPriceModel(),
                        new BigDecimal("2.00"));

        subscrSetup.savePriceModelForSubscription(supplierAdmin.getKey(),
                subDetails, newSubPriceModel, customer);

        // 1. upgrade service
        VOServiceDetails unitTemplate = serviceSetup
                .createAndPublishMarketableService(supplierAdmin.getKey(),
                        "PAIRWISE19_Upgrade", TestService.EXAMPLE2,
                        TestPriceModel.FREE, example2Service,
                        supplierMarketplace);
        serviceSetup.registerCompatibleServices(supplierAdmin.getKey(),
                serviceTemplate, unitTemplate);
        VOServiceDetails unitH1 = serviceSetup.savePriceModelForCustomer(
                unitTemplate, TestPriceModel.FREE, customer);
        unitH1 = serviceSetup.activateMarketableService(unitH1);

        setDateFactory("2013-03-31 03:59:59");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(subDetails, unitH1);

        // suspend/resume subscription several times by removing/restoring
        // customer's payment types
        setDateFactory("2013-03-31 04:00:01");

        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);

        paymentSetup.deleteCustomerPaymentTypes(customer);

        setDateFactory("2013-03-31 04:00:02");

        paymentSetup.reassignCustomerPaymentTypes(customer);

        upgradedSubDetails = subscrSetup.getSubscriptionDetails(
                customerAdmin.getKey(), upgradedSubDetails.getSubscriptionId());

        // change sunbscriptionID
        setDateFactory("2013-03-31 04:00:03");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        upgradedSubDetails.setSubscriptionId("PICT_TEST_19" + "_SubID2");
        upgradedSubDetails = subscrSetup.modifySubscription(upgradedSubDetails,
                null);
        // role change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        setDateFactory("2013-03-31 04:00:04");
        upgradedSubDetails = subscrSetup.modifyUserRole(upgradedSubDetails
                .getUsageLicenses().get(0), VOServiceFactory.getRole(unitH1,
                "ADMIN"), upgradedSubDetails.getSubscriptionId());

        // role change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        setDateFactory("2013-03-31 04:00:05");
        upgradedSubDetails = subscrSetup.modifyUserRole(upgradedSubDetails
                .getUsageLicenses().get(0), VOServiceFactory.getRole(unitH1,
                "GUEST"), upgradedSubDetails.getSubscriptionId());

        // Events
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        subscrSetup.recordEventForSubscription(upgradedSubDetails,
                DateTimeHandling.calculateMillis("2013-04-01 02:14:00"),
                "FILE_UPLOAD", 10);

        subscrSetup.recordEventForSubscription(upgradedSubDetails,
                DateTimeHandling.calculateMillis("2013-04-01 02:14:01"),
                "FILE_UPLOAD", 10);
        // unsubscribe
        setDateFactory("2013-04-01 02:15:00");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        subscrSetup
                .unsubscribeToService(upgradedSubDetails.getSubscriptionId());

        resetCutOffDay(supplierAdmin.getKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_19", subDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_19", upgradedSubDetails);

        BillingIntegrationTestBase.updateCustomerListForTests("PICT_TEST_19",
                customer);

    }

    public void createPictScenario18() throws Exception {
        setDateFactory("2013-03-30 01:00:00");

        // Create a supplier with a marketplace
        String supplierAdminId = "Pict18Supplier";
        VOOrganization supplier = orgSetup.createOrganization(
                basicSetup.getPlatformOperatorUserKey(), supplierAdminId,
                "Pict18SupplierOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                OrganizationRoleType.TECHNOLOGY_PROVIDER,
                OrganizationRoleType.SUPPLIER);
        VOUser supplierAdmin = orgSetup.getUser(supplierAdminId, true);
        paymentSetup.createPaymentForSupplier(
                basicSetup.getPlatformOperatorUserKey(),
                supplierAdmin.getKey(), supplier);

        // Create own technical service
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        serviceSetup
                .importTechnicalServices(BaseAdmUmTest.TECHNICAL_SERVICE_EXAMPLE2_XML);
        VOTechnicalService example2Service = serviceSetup
                .getTechnicalService(VOTechServiceFactory.TECH_SERVICE_EXAMPLE2_ID);

        setCutOffDay(supplierAdmin.getKey(), 15);

        // Create a customer
        String customerAdminId = "Pict18Customer";
        VOOrganization customer = orgSetup.registerCustomer(
                "Pict18CustomerOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                customerAdminId, supplierMarketplace.getMarketplaceId(),
                supplier.getOrganizationId());
        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);

        orgSetup.createMarketingPermission(supplierAdmin.getKey(),
                supplier.getOrganizationId(), example2Service);

        orgSetup.saveAllVats(supplierAdmin.getKey(),
                VOVatRateFactory.newVOVatRate(new BigDecimal("10.00")), //
                null, null);

        // price model for customer
        VOServiceDetails serviceTemplate = serviceSetup
                .createAndPublishMarketableService(supplierAdmin.getKey(),
                        "PICT_TEST_18", TestService.EXAMPLE2,
                        TestPriceModel.FREE, example2Service,
                        supplierMarketplace);
        VOServiceDetails serviceDetails = serviceSetup
                .savePriceModelForCustomer(serviceTemplate,
                        TestPriceModel.FREE, customer);
        serviceDetails = serviceSetup.activateMarketableService(serviceDetails);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "GUEST");

        // subscribe
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "PICT_TEST_18", serviceDetails, customerAdmin, role);

        // user is assigned

        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        setDateFactory("2013-03-31 04:00:03");
        VORoleDefinition changedRole = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        subDetails = subscrSetup.addUser(basicSetup.getSecondCustomerUser1(),
                changedRole, subDetails.getSubscriptionId());

        // user is deassigned
        setDateFactory("2013-03-31 04:00:04");
        subscrSetup.revokeUser(basicSetup.getSecondCustomerUser1(),
                subDetails.getSubscriptionId());

        // role change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        setDateFactory("2013-03-31 04:00:05");
        subDetails = subscrSetup.modifyUserRole(subDetails.getUsageLicenses()
                .get(0), VOServiceFactory.getRole(serviceDetails, "GUEST"),
                subDetails.getSubscriptionId());

        // Events
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        subscrSetup.recordEventForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-04-01 02:14:00"),
                "FILE_UPLOAD", 10);

        subscrSetup.recordEventForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-04-01 02:14:01"),
                "FILE_UPLOAD", 10);
        // unsubscribe
        setDateFactory("2013-04-01 02:15:00");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        // delete market service
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-04-01 02:15:01"));
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER);
        serviceSetup.deleteMarketableService(serviceDetails);

        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-04-01 02:15:01"));
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER);
        serviceSetup.deleteMarketableService(serviceTemplate);

        // Delete also the technical service
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-04-01 02:15:02"));
        container.login(supplierAdmin.getKey(), ROLE_TECHNOLOGY_MANAGER);
        serviceSetup.deleteTechnicalService(example2Service);

        resetCutOffDay(supplierAdmin.getKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_18", subDetails);

        BillingIntegrationTestBase.updateCustomerListForTests("PICT_TEST_18",
                customer);

    }

    public void createPictScenario17() throws Exception {
        setDateFactory("2013-07-01 10:00:00");

        // Create a supplier with a marketplace
        String supplierAdminId = "Pict17Supplier";
        VOOrganization supplier = orgSetup.createOrganization(
                basicSetup.getPlatformOperatorUserKey(), supplierAdminId,
                "Pict17SupplierOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                OrganizationRoleType.TECHNOLOGY_PROVIDER,
                OrganizationRoleType.SUPPLIER);
        VOUser supplierAdmin = orgSetup.getUser(supplierAdminId, true);
        paymentSetup.createPaymentForSupplier(
                basicSetup.getPlatformOperatorUserKey(),
                supplierAdmin.getKey(), supplier);

        // Create own technical service
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        serviceSetup
                .importTechnicalServices(BaseAdmUmTest.TECHNICAL_SERVICE_EXAMPLE2_XML);
        VOTechnicalService example2Service = serviceSetup
                .getTechnicalService(VOTechServiceFactory.TECH_SERVICE_EXAMPLE2_ID);

        setCutOffDay(supplierAdmin.getKey(), 1);

        // Create a customer
        String customerAdminId = "Pict17Customer";
        VOOrganization customer = orgSetup.registerCustomer(
                "Pict17CustomerOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                customerAdminId, supplierMarketplace.getMarketplaceId(),
                supplier.getOrganizationId());
        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);

        orgSetup.createMarketingPermission(supplierAdmin.getKey(),
                supplier.getOrganizationId(), example2Service);

        // price model for customer
        VOServiceDetails serviceTemplate = serviceSetup
                .createAndPublishMarketableService(supplierAdmin.getKey(),
                        "PICT_TEST_17", TestService.EXAMPLE2,
                        TestPriceModel.EXAMPLE_PICT17_RATA_WEEKS,
                        example2Service, supplierMarketplace);
        VOServiceDetails serviceDetails = serviceSetup
                .savePriceModelForCustomer(serviceTemplate,
                        TestPriceModel.EXAMPLE_PICT17_RATA_WEEKS, customer);
        serviceDetails = serviceSetup.activateMarketableService(serviceDetails);

        // Vat
        orgSetup.saveAllVats(supplierAdmin.getKey(),
                VOVatRateFactory.newVOVatRate(new BigDecimal("10.00")), null,
                null);

        orgSetup.updateCustomerDiscount(customer, new BigDecimal("3.00"),
                DateTimeHandling.calculateMillis("2013-07-08 00:00:00"),
                DateTimeHandling.calculateMillis("2013-07-09 01:00:00"));

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "GUEST");
        // subscribe
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "PICT_TEST_17", serviceDetails, customerAdmin, role);

        // role change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        setDateFactory("2013-07-01 11:00:00");
        subDetails = subscrSetup.modifyUserRole(subDetails.getUsageLicenses()
                .get(0), VOServiceFactory.getRole(serviceDetails, "ADMIN"),
                subDetails.getSubscriptionId());

        // parameter change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        subDetails = subscrSetup.modifyParameterForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-07-01 12:00:00"),
                "MAX_FOLDER_NUMBER", "100");

        // role change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        setDateFactory("2013-07-17 11:00:00");
        subDetails = subscrSetup.modifyUserRole(subDetails.getUsageLicenses()
                .get(0), VOServiceFactory.getRole(serviceDetails, "USER"),
                subDetails.getSubscriptionId());

        // 1. upgrade service
        VOServiceDetails unitTemplate = serviceSetup
                .createAndPublishMarketableService(supplierAdmin.getKey(),
                        "PAIRWISE17_Upgrade", TestService.EXAMPLE2,
                        TestPriceModel.FREE, example2Service,
                        supplierMarketplace);
        serviceSetup.registerCompatibleServices(supplierAdmin.getKey(),
                serviceTemplate, unitTemplate);
        VOServiceDetails unitH1 = serviceSetup.savePriceModelForCustomer(
                unitTemplate, TestPriceModel.FREE, customer);
        unitH1 = serviceSetup.activateMarketableService(unitH1);

        setDateFactory("2013-07-28 10:00:00");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(subDetails, unitH1);

        // 2. upgrade service
        VOServiceDetails unitTemplate2 = serviceSetup
                .createAndPublishMarketableService(supplierAdmin.getKey(),
                        "PAIRWISE17_Upgrade2", TestService.EXAMPLE2,
                        TestPriceModel.EXAMPLE_PICT36_UNIT_WEEKS,
                        example2Service, supplierMarketplace);
        serviceSetup.registerCompatibleServices(supplierAdmin.getKey(),
                unitTemplate, unitTemplate2);
        VOServiceDetails unitH2 = serviceSetup.savePriceModelForCustomer(
                unitTemplate2, TestPriceModel.EXAMPLE_PICT36_UNIT_WEEKS,
                customer);
        unitH2 = serviceSetup.activateMarketableService(unitH2);

        setDateFactory("2013-07-28 10:00:01");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetails2 = subscrSetup
                .upgradeSubscription(upgradedSubDetails, unitH2);

        // parameter change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        upgradedSubDetails2 = subscrSetup.modifyParameterForSubscription(
                upgradedSubDetails2,
                DateTimeHandling.calculateMillis("2013-07-28 10:30:00"),
                "MAX_FOLDER_NUMBER", "150");

        // user is assigned
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VORoleDefinition changedRole = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        setDateFactory("2013-07-28 11:00:01");
        upgradedSubDetails2 = subscrSetup.addUser(
                basicSetup.getSecondCustomerUser1(), changedRole,
                upgradedSubDetails2.getSubscriptionId());

        // user is deassigned
        setDateFactory("2013-07-28 12:00:01");
        subscrSetup.revokeUser(basicSetup.getSecondCustomerUser1(),
                upgradedSubDetails2.getSubscriptionId());
        upgradedSubDetails2 = subscrSetup.addUser(
                basicSetup.getSecondCustomerUser1(), changedRole,
                upgradedSubDetails2.getSubscriptionId());

        // price model change

        long priceModelChangeTime = DateTimeHandling
                .calculateMillis("2013-07-31 08:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(priceModelChangeTime);
        VOPriceModel newSubPriceModel = VOPriceModelFactory
                .modifyPriceModelPeriodFee(upgradedSubDetails2.getPriceModel(),
                        new BigDecimal("2.00"));

        subscrSetup.savePriceModelForSubscription(supplierAdmin.getKey(),
                upgradedSubDetails2, newSubPriceModel, customer);

        // change sunbscriptionID
        setDateFactory("2013-07-31 09:00:00");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        upgradedSubDetails2.setSubscriptionId("PICT_TEST_17" + "_SubID2");
        upgradedSubDetails2 = subscrSetup.modifySubscription(
                upgradedSubDetails2, null);

        // suspend/resume subscription several times by removing/restoring
        // customer's payment types
        setDateFactory("2013-07-31 10:00:00");

        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);

        paymentSetup.deleteCustomerPaymentTypes(customer);

        upgradedSubDetails2 = subscrSetup
                .getSubscriptionDetails(customerAdmin.getKey(),
                        upgradedSubDetails2.getSubscriptionId());

        // unsubscribe
        setDateFactory("2013-08-10 10:00:00");
        subscrSetup.unsubscribeToService(upgradedSubDetails2
                .getSubscriptionId());

        // delete market service
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-04-01 02:15:01"));
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER);
        serviceSetup.deleteMarketableService(serviceDetails);

        resetCutOffDay(supplierAdmin.getKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_17", subDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_17", upgradedSubDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_17", upgradedSubDetails2);

        BillingIntegrationTestBase.updateCustomerListForTests("PICT_TEST_17",
                customer);

    }

    public void createPictScenario16() throws Exception {
        setDateFactory("2013-03-28 10:00:00");

        // Create a supplier with a marketplace
        String supplierAdminId = "Pict16Supplier";
        VOOrganization supplier = orgSetup.createOrganization(
                basicSetup.getPlatformOperatorUserKey(), supplierAdminId,
                "Pict16SupplierOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                OrganizationRoleType.TECHNOLOGY_PROVIDER,
                OrganizationRoleType.SUPPLIER);
        VOUser supplierAdmin = orgSetup.getUser(supplierAdminId, true);
        paymentSetup.createPaymentForSupplier(
                basicSetup.getPlatformOperatorUserKey(),
                supplierAdmin.getKey(), supplier);

        // Create own technical service
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        serviceSetup
                .importTechnicalServices(BaseAdmUmTest.TECHNICAL_SERVICE_EXAMPLE2_XML);
        VOTechnicalService example2Service = serviceSetup
                .getTechnicalService(VOTechServiceFactory.TECH_SERVICE_EXAMPLE2_ID);

        setCutOffDay(supplierAdmin.getKey(), 1);

        // Create a customer
        String customerAdminId = "Pict16Customer";
        VOOrganization customer = orgSetup.registerCustomer(
                "Pict16CustomerOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                customerAdminId, supplierMarketplace.getMarketplaceId(),
                supplier.getOrganizationId());
        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);

        orgSetup.createMarketingPermission(supplierAdmin.getKey(),
                supplier.getOrganizationId(), example2Service);

        // price model for customer
        VOServiceDetails serviceTemplate = serviceSetup
                .createAndPublishMarketableService(supplierAdmin.getKey(),
                        "PICT_TEST_16", TestService.EXAMPLE2,
                        TestPriceModel.EXAMPLE_PICT16_RATA_DAYS,
                        example2Service, supplierMarketplace);
        VOServiceDetails serviceDetails = serviceSetup
                .savePriceModelForCustomer(serviceTemplate,
                        TestPriceModel.EXAMPLE_PICT16_RATA_DAYS, customer);
        serviceDetails = serviceSetup.activateMarketableService(serviceDetails);

        // Vat
        orgSetup.saveAllVats(supplierAdmin.getKey(), VOVatRateFactory
                .newVOVatRate(new BigDecimal("13.00")), Arrays.asList(
                VOVatRateFactory.newVOCountryVatRate(new BigDecimal("20.00"),
                        TestOrganizationSetup.ORGANIZATION_DOMICILE_UK),
                VOVatRateFactory.newVOCountryVatRate(new BigDecimal("19.00"),
                        TestOrganizationSetup.ORGANIZATION_DOMICILE_DE)),
                Arrays.asList(VOVatRateFactory.newVOOrganizationVatRate(
                        new BigDecimal("19.00"), customer)));

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "GUEST");
        // subscribe
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "PICT_TEST_16", serviceDetails, customerAdmin, role);

        // user is assigned
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VORoleDefinition changedRole = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        setDateFactory("2013-03-28 11:00:00");
        subDetails = subscrSetup.addUser(basicSetup.getSecondCustomerUser1(),
                changedRole, subDetails.getSubscriptionId());

        // user is deassigned
        setDateFactory("2013-03-28 12:00:00");
        subscrSetup.revokeUser(basicSetup.getSecondCustomerUser1(),
                subDetails.getSubscriptionId());

        // suspend/resume subscription several times by removing/restoring
        // customer's payment types
        long suspResTime = DateTimeHandling
                .calculateMillis("2013-03-29 11:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(suspResTime);

        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);

        paymentSetup.deleteCustomerPaymentTypes(customer);

        long reassignResTime = DateTimeHandling
                .calculateMillis("2013-03-31 00:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(reassignResTime);
        paymentSetup.reassignCustomerPaymentTypes(customer);

        // price model change

        long priceModelChangeTime = DateTimeHandling
                .calculateMillis("2013-03-31 11:00:01");
        BillingIntegrationTestBase.setDateFactoryInstance(priceModelChangeTime);
        VOPriceModel newSubPriceModel = VOPriceModelFactory
                .modifyPriceModelPeriodFee(subDetails.getPriceModel(),
                        new BigDecimal("2.00"));

        subscrSetup.savePriceModelForSubscription(supplierAdmin.getKey(),
                subDetails, newSubPriceModel, customer);

        // role change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        setDateFactory("2013-03-31 12:00:00");
        subDetails = subscrSetup.modifyUserRole(subDetails.getUsageLicenses()
                .get(0), VOServiceFactory.getRole(serviceDetails, "USER"),
                subDetails.getSubscriptionId());

        // 1. upgrade service
        VOServiceDetails unitTemplate = serviceSetup
                .createAndPublishMarketableService(supplierAdmin.getKey(),
                        "PAIRWISE16_Upgrade", TestService.EXAMPLE2,
                        TestPriceModel.FREE, example2Service,
                        supplierMarketplace);
        serviceSetup.registerCompatibleServices(supplierAdmin.getKey(),
                serviceTemplate, unitTemplate);
        VOServiceDetails unitH1 = serviceSetup.savePriceModelForCustomer(
                unitTemplate, TestPriceModel.FREE, customer);
        unitH1 = serviceSetup.activateMarketableService(unitH1);

        setDateFactory("2013-04-01 11:00:00");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(subDetails, unitH1);

        // change sunbscriptionID
        setDateFactory("2013-04-03 12:00:00");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        upgradedSubDetails.setSubscriptionId("PICT_TEST_16" + "_SubID2");
        upgradedSubDetails = subscrSetup.modifySubscription(upgradedSubDetails,
                null);

        resetCutOffDay(supplierAdmin.getKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_16", subDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_16", upgradedSubDetails);

        BillingIntegrationTestBase.updateCustomerListForTests("PICT_TEST_16",
                customer);

    }

    public void createPictScenario16_1() throws Exception {
        setDateFactory("2013-03-31 00:00:00");

        // Create a supplier with a marketplace
        String supplierAdminId = "Pict16_1Supplier";
        VOOrganization supplier = orgSetup.createOrganization(
                basicSetup.getPlatformOperatorUserKey(), supplierAdminId,
                "Pict16_1SupplierOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                OrganizationRoleType.TECHNOLOGY_PROVIDER,
                OrganizationRoleType.SUPPLIER);
        VOUser supplierAdmin = orgSetup.getUser(supplierAdminId, true);
        paymentSetup.createPaymentForSupplier(
                basicSetup.getPlatformOperatorUserKey(),
                supplierAdmin.getKey(), supplier);

        // Create own technical service
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        serviceSetup
                .importTechnicalServices(BaseAdmUmTest.TECHNICAL_SERVICE_EXAMPLE2_XML);
        VOTechnicalService example2Service = serviceSetup
                .getTechnicalService(VOTechServiceFactory.TECH_SERVICE_EXAMPLE2_ID);

        setCutOffDay(supplierAdmin.getKey(), 1);

        // Create a customer
        String customerAdminId = "Pict16_1Customer";
        VOOrganization customer = orgSetup.registerCustomer(
                "Pict16_1CustomerOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                customerAdminId, supplierMarketplace.getMarketplaceId(),
                supplier.getOrganizationId());
        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);

        orgSetup.createMarketingPermission(supplierAdmin.getKey(),
                supplier.getOrganizationId(), example2Service);

        // price model for customer
        VOServiceDetails serviceTemplate = serviceSetup
                .createAndPublishMarketableService(supplierAdmin.getKey(),
                        "PICT_TEST_16_1", TestService.EXAMPLE2,
                        TestPriceModel.EXAMPLE_PICT16_1_RATA_DAYS,
                        example2Service, supplierMarketplace);
        VOServiceDetails serviceDetails = serviceSetup
                .savePriceModelForCustomer(serviceTemplate,
                        TestPriceModel.EXAMPLE_PICT16_1_RATA_DAYS, customer);
        serviceDetails = serviceSetup.activateMarketableService(serviceDetails);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "GUEST");

        // subscribe
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "PICT_TEST_16_1", serviceDetails, customerAdmin, role);
        // unsubscribe
        setDateFactory("2013-04-01 00:00:00");
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(supplierAdmin.getKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_16_1", subDetails);
        BillingIntegrationTestBase.updateCustomerListForTests("PICT_TEST_16_1",
                customer);

    }

    public void createPictScenario16_2() throws Exception {
        setDateFactory("2013-03-30 10:00:00");

        // Create a supplier with a marketplace
        String supplierAdminId = "Pict16_2Supplier";
        VOOrganization supplier = orgSetup.createOrganization(
                basicSetup.getPlatformOperatorUserKey(), supplierAdminId,
                "Pict16_2SupplierOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                OrganizationRoleType.TECHNOLOGY_PROVIDER,
                OrganizationRoleType.SUPPLIER);
        VOUser supplierAdmin = orgSetup.getUser(supplierAdminId, true);
        paymentSetup.createPaymentForSupplier(
                basicSetup.getPlatformOperatorUserKey(),
                supplierAdmin.getKey(), supplier);

        // Create own technical service
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        serviceSetup
                .importTechnicalServices(BaseAdmUmTest.TECHNICAL_SERVICE_EXAMPLE2_XML);
        VOTechnicalService example2Service = serviceSetup
                .getTechnicalService(VOTechServiceFactory.TECH_SERVICE_EXAMPLE2_ID);

        setCutOffDay(supplierAdmin.getKey(), 1);

        // Create a customer
        String customerAdminId = "Pict16_2Customer";
        VOOrganization customer = orgSetup.registerCustomer(
                "Pict16_2CustomerOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                customerAdminId, supplierMarketplace.getMarketplaceId(),
                supplier.getOrganizationId());
        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);

        orgSetup.createMarketingPermission(supplierAdmin.getKey(),
                supplier.getOrganizationId(), example2Service);

        // price model for customer
        VOServiceDetails serviceTemplate = serviceSetup
                .createAndPublishMarketableService(supplierAdmin.getKey(),
                        "PICT_TEST_16_2", TestService.EXAMPLE2,
                        TestPriceModel.EXAMPLE_PICT16_2_RATA_DAYS,
                        example2Service, supplierMarketplace);
        VOServiceDetails serviceDetails = serviceSetup
                .savePriceModelForCustomer(serviceTemplate,
                        TestPriceModel.EXAMPLE_PICT16_2_RATA_DAYS, customer);
        serviceDetails = serviceSetup.activateMarketableService(serviceDetails);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "GUEST");

        // subscribe
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "PICT_TEST_16_2", serviceDetails, customerAdmin, role);
        // unsubscribe
        setDateFactory("2013-03-31 12:00:00");
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(supplierAdmin.getKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_16_2", subDetails);
        BillingIntegrationTestBase.updateCustomerListForTests("PICT_TEST_16_2",
                customer);

    }

    public void createPictScenario16_3() throws Exception {
        setDateFactory("2013-10-27 00:00:00");

        // Create a supplier with a marketplace
        String supplierAdminId = "Pict16_3Supplier";
        VOOrganization supplier = orgSetup.createOrganization(
                basicSetup.getPlatformOperatorUserKey(), supplierAdminId,
                "Pict16_3SupplierOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                OrganizationRoleType.TECHNOLOGY_PROVIDER,
                OrganizationRoleType.SUPPLIER);
        VOUser supplierAdmin = orgSetup.getUser(supplierAdminId, true);
        paymentSetup.createPaymentForSupplier(
                basicSetup.getPlatformOperatorUserKey(),
                supplierAdmin.getKey(), supplier);

        // Create own technical service
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        serviceSetup
                .importTechnicalServices(BaseAdmUmTest.TECHNICAL_SERVICE_EXAMPLE2_XML);
        VOTechnicalService example2Service = serviceSetup
                .getTechnicalService(VOTechServiceFactory.TECH_SERVICE_EXAMPLE2_ID);

        setCutOffDay(supplierAdmin.getKey(), 1);

        // Create a customer
        String customerAdminId = "Pict16_3Customer";
        VOOrganization customer = orgSetup.registerCustomer(
                "Pict16_3CustomerOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                customerAdminId, supplierMarketplace.getMarketplaceId(),
                supplier.getOrganizationId());
        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);

        orgSetup.createMarketingPermission(supplierAdmin.getKey(),
                supplier.getOrganizationId(), example2Service);

        // price model for customer
        VOServiceDetails serviceTemplate = serviceSetup
                .createAndPublishMarketableService(supplierAdmin.getKey(),
                        "PICT_TEST_16_3", TestService.EXAMPLE2,
                        TestPriceModel.EXAMPLE_PICT16_1_RATA_DAYS,
                        example2Service, supplierMarketplace);
        VOServiceDetails serviceDetails = serviceSetup
                .savePriceModelForCustomer(serviceTemplate,
                        TestPriceModel.EXAMPLE_PICT16_1_RATA_DAYS, customer);
        serviceDetails = serviceSetup.activateMarketableService(serviceDetails);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "GUEST");

        // subscribe
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "PICT_TEST_16_3", serviceDetails, customerAdmin, role);
        // unsubscribe
        setDateFactory("2013-10-28 00:00:00");
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(supplierAdmin.getKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_16_3", subDetails);
        BillingIntegrationTestBase.updateCustomerListForTests("PICT_TEST_16_3",
                customer);

    }

    public void createPictScenario16_4() throws Exception {
        setDateFactory("2013-10-27 10:00:00");

        // Create a supplier with a marketplace
        String supplierAdminId = "Pict16_4Supplier";
        VOOrganization supplier = orgSetup.createOrganization(
                basicSetup.getPlatformOperatorUserKey(), supplierAdminId,
                "Pict16_4SupplierOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                OrganizationRoleType.TECHNOLOGY_PROVIDER,
                OrganizationRoleType.SUPPLIER);
        VOUser supplierAdmin = orgSetup.getUser(supplierAdminId, true);
        paymentSetup.createPaymentForSupplier(
                basicSetup.getPlatformOperatorUserKey(),
                supplierAdmin.getKey(), supplier);

        // Create own technical service
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        serviceSetup
                .importTechnicalServices(BaseAdmUmTest.TECHNICAL_SERVICE_EXAMPLE2_XML);
        VOTechnicalService example2Service = serviceSetup
                .getTechnicalService(VOTechServiceFactory.TECH_SERVICE_EXAMPLE2_ID);

        setCutOffDay(supplierAdmin.getKey(), 1);

        // Create a customer
        String customerAdminId = "Pict16_4Customer";
        VOOrganization customer = orgSetup.registerCustomer(
                "Pict16_4CustomerOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                customerAdminId, supplierMarketplace.getMarketplaceId(),
                supplier.getOrganizationId());
        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);

        orgSetup.createMarketingPermission(supplierAdmin.getKey(),
                supplier.getOrganizationId(), example2Service);

        // price model for customer
        VOServiceDetails serviceTemplate = serviceSetup
                .createAndPublishMarketableService(supplierAdmin.getKey(),
                        "PICT_TEST_16_4", TestService.EXAMPLE2,
                        TestPriceModel.EXAMPLE_PICT16_1_RATA_DAYS,
                        example2Service, supplierMarketplace);
        VOServiceDetails serviceDetails = serviceSetup
                .savePriceModelForCustomer(serviceTemplate,
                        TestPriceModel.EXAMPLE_PICT16_1_RATA_DAYS, customer);
        serviceDetails = serviceSetup.activateMarketableService(serviceDetails);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "GUEST");

        // subscribe
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "PICT_TEST_16_4", serviceDetails, customerAdmin, role);
        // unsubscribe
        setDateFactory("2013-10-27 11:00:00");
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(supplierAdmin.getKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_16_4", subDetails);
        BillingIntegrationTestBase.updateCustomerListForTests("PICT_TEST_16_4",
                customer);

    }

    public void createPictScenario15() throws Exception {
        setDateFactory("2013-03-28 10:15:00");

        // Create a supplier with a marketplace
        String supplierAdminId = "Pict15Supplier";
        VOOrganization supplier = orgSetup.createOrganization(
                basicSetup.getPlatformOperatorUserKey(), supplierAdminId,
                "Pict15SupplierOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                OrganizationRoleType.TECHNOLOGY_PROVIDER,
                OrganizationRoleType.SUPPLIER);
        VOUser supplierAdmin = orgSetup.getUser(supplierAdminId, true);
        paymentSetup.createPaymentForSupplier(
                basicSetup.getPlatformOperatorUserKey(),
                supplierAdmin.getKey(), supplier);

        // Create own technical service
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        serviceSetup
                .importTechnicalServices(BaseAdmUmTest.TECHNICAL_SERVICE_EXAMPLE2_XML);
        VOTechnicalService example2Service = serviceSetup
                .getTechnicalService(VOTechServiceFactory.TECH_SERVICE_EXAMPLE2_ID);

        setCutOffDay(supplierAdmin.getKey(), 2);

        // Create a customer
        String customerAdminId = "Pict15Customer";
        VOOrganization customer = orgSetup.registerCustomer(
                "Pict15CustomerOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                customerAdminId, supplierMarketplace.getMarketplaceId(),
                supplier.getOrganizationId());
        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);

        orgSetup.createMarketingPermission(supplierAdmin.getKey(),
                supplier.getOrganizationId(), example2Service);

        // price model for customer
        VOServiceDetails serviceTemplate = serviceSetup
                .createAndPublishMarketableService(supplierAdmin.getKey(),
                        "PICT_TEST_15", TestService.EXAMPLE2,
                        TestPriceModel.EXAMPLE_PICT15_RATA_HOURS,
                        example2Service, supplierMarketplace);
        VOServiceDetails serviceDetails = serviceSetup
                .savePriceModelForCustomer(serviceTemplate,
                        TestPriceModel.EXAMPLE_PICT15_RATA_HOURS, customer);
        serviceDetails = serviceSetup.activateMarketableService(serviceDetails);
        // Discount
        orgSetup.updateCustomerDiscount(customer, new BigDecimal("3.00"),
                DateTimeHandling.calculateMillis("2013-07-08 00:00:00"),
                DateTimeHandling.calculateMillis("2013-07-09 01:00:00"));

        // Vat
        orgSetup.saveAllVats(supplierAdmin.getKey(),
                VOVatRateFactory.newVOVatRate(new BigDecimal("10.00")), null,
                null);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "GUEST");
        // subscribe
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "PICT_TEST_15", serviceDetails, customerAdmin, role);

        // 1. upgrade service
        VOServiceDetails unitTemplate = serviceSetup
                .createAndPublishMarketableService(supplierAdmin.getKey(),
                        "PAIRWISE15_Upgrade", TestService.EXAMPLE2,
                        TestPriceModel.EXAMPLE_PICT15_UNIT_HOURS,
                        example2Service, supplierMarketplace);
        serviceSetup.registerCompatibleServices(supplierAdmin.getKey(),
                serviceTemplate, unitTemplate);
        VOServiceDetails unitH1 = serviceSetup.savePriceModelForCustomer(
                unitTemplate, TestPriceModel.EXAMPLE_PICT15_UNIT_HOURS,
                customer);
        unitH1 = serviceSetup.activateMarketableService(unitH1);

        setDateFactory("2013-03-31 00:00:00");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(subDetails, unitH1);

        // change sunbscriptionID
        setDateFactory("2013-03-31 00:00:01");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        upgradedSubDetails.setSubscriptionId("PICT_TEST_15" + "_SubID2");
        upgradedSubDetails = subscrSetup.modifySubscription(upgradedSubDetails,
                null);

        // change sunbscriptionID
        setDateFactory("2013-03-31 00:00:02");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        upgradedSubDetails.setSubscriptionId("PICT_TEST_15" + "_SubID3");
        upgradedSubDetails = subscrSetup.modifySubscription(upgradedSubDetails,
                null);

        // price model change

        long priceModelChangeTime = DateTimeHandling
                .calculateMillis("2013-03-31 00:00:02");
        BillingIntegrationTestBase.setDateFactoryInstance(priceModelChangeTime);
        VOPriceModel newSubPriceModel = VOPriceModelFactory
                .modifyPriceModelPeriodFee(upgradedSubDetails.getPriceModel(),
                        new BigDecimal("2.00"));

        subscrSetup.savePriceModelForSubscription(supplierAdmin.getKey(),
                upgradedSubDetails, newSubPriceModel, customer);

        // user is deassigned
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        setDateFactory("2013-03-31 00:00:03");
        upgradedSubDetails = subscrSetup.revokeUser(customerAdmin,
                upgradedSubDetails.getSubscriptionId());

        // parameter change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        upgradedSubDetails = subscrSetup.modifyParameterForSubscription(
                upgradedSubDetails,
                DateTimeHandling.calculateMillis("2013-03-31 00:15:00"),
                "LONG_NUMBER", "100");

        // user is assigned
        VORoleDefinition changedRole = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        setDateFactory("2013-03-31 00:30:00");
        upgradedSubDetails = subscrSetup.addUser(customerAdmin, changedRole,
                upgradedSubDetails.getSubscriptionId());

        // 2. upgrade service
        VOServiceDetails unitTemplate2 = serviceSetup
                .createAndPublishMarketableService(supplierAdmin.getKey(),
                        "PAIRWISE15_Upgrade2", TestService.EXAMPLE2,
                        TestPriceModel.EXAMPLE_PICT15_UNIT_HOURS,
                        example2Service, supplierMarketplace);
        serviceSetup.registerCompatibleServices(supplierAdmin.getKey(),
                unitTemplate, unitTemplate2);
        VOServiceDetails unitH2 = serviceSetup.savePriceModelForCustomer(
                unitTemplate2, TestPriceModel.EXAMPLE_PICT15_UNIT_HOURS,
                customer);
        unitH2 = serviceSetup.activateMarketableService(unitH2);

        setDateFactory("2013-03-31 01:00:00");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetails2 = subscrSetup
                .upgradeSubscription(upgradedSubDetails, unitH2);

        // suspend/resume subscription several times by removing/restoring
        // customer's payment types
        setDateFactory("2013-03-31 01:59:59");
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);

        paymentSetup.deleteCustomerPaymentTypes(customer);

        setDateFactory("2013-03-31 03:00:01");
        paymentSetup.reassignCustomerPaymentTypes(customer);
        upgradedSubDetails2 = subscrSetup
                .getSubscriptionDetails(customerAdmin.getKey(),
                        upgradedSubDetails2.getSubscriptionId());

        // change sunbscriptionID
        setDateFactory("2013-03-31 03:00:01");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        upgradedSubDetails2.setSubscriptionId("PICT_TEST_15" + "_SubID4");
        upgradedSubDetails2 = subscrSetup.modifySubscription(
                upgradedSubDetails2, null);

        resetCutOffDay(supplierAdmin.getKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_15", subDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_15", upgradedSubDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_15", upgradedSubDetails2);

        BillingIntegrationTestBase.updateCustomerListForTests("PICT_TEST_15",
                customer);

    }

    public void createPictScenario14() throws Exception {
        setDateFactory("2013-03-30 10:15:00");

        // Create a supplier with a marketplace
        String supplierAdminId = "Pict14Supplier";
        VOOrganization supplier = orgSetup.createOrganization(
                basicSetup.getPlatformOperatorUserKey(), supplierAdminId,
                "Pict14SupplierOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                OrganizationRoleType.TECHNOLOGY_PROVIDER,
                OrganizationRoleType.SUPPLIER);
        VOUser supplierAdmin = orgSetup.getUser(supplierAdminId, true);
        paymentSetup.createPaymentForSupplier(
                basicSetup.getPlatformOperatorUserKey(),
                supplierAdmin.getKey(), supplier);

        // Create own technical service
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        serviceSetup
                .importTechnicalServices(BaseAdmUmTest.TECHNICAL_SERVICE_EXAMPLE2_XML);
        VOTechnicalService example2Service = serviceSetup
                .getTechnicalService(VOTechServiceFactory.TECH_SERVICE_EXAMPLE2_ID);

        setCutOffDay(supplierAdmin.getKey(), 1);

        // Create a customer
        String customerAdminId = "Pict14Customer";
        VOOrganization customer = orgSetup.registerCustomer(
                "Pict14CustomerOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                customerAdminId, supplierMarketplace.getMarketplaceId(),
                supplier.getOrganizationId());
        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);

        orgSetup.createMarketingPermission(supplierAdmin.getKey(),
                supplier.getOrganizationId(), example2Service);

        // price model for customer
        VOServiceDetails serviceTemplate = serviceSetup
                .createAndPublishMarketableService(supplierAdmin.getKey(),
                        "PICT_TEST_14", TestService.EXAMPLE2,
                        TestPriceModel.EXAMPLE_PICT14_RATA_HOURS,
                        example2Service, supplierMarketplace);
        VOServiceDetails serviceDetails = serviceSetup
                .savePriceModelForCustomer(serviceTemplate,
                        TestPriceModel.EXAMPLE_PICT14_RATA_HOURS, customer);
        serviceDetails = serviceSetup.activateMarketableService(serviceDetails);

        // Vat
        orgSetup.saveAllVats(supplierAdmin.getKey(),
                VOVatRateFactory.newVOVatRate(new BigDecimal("10.00")), null,
                null);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "GUEST");
        // subscribe
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "PICT_TEST_14", serviceDetails, customerAdmin, role);

        // price model change

        long priceModelChangeTime = DateTimeHandling
                .calculateMillis("2013-03-30 10:15:01");
        BillingIntegrationTestBase.setDateFactoryInstance(priceModelChangeTime);
        VOPriceModel newSubPriceModel = VOPriceModelFactory
                .modifyPriceModelPeriodFee(subDetails.getPriceModel(),
                        new BigDecimal("2.00"));

        subscrSetup.savePriceModelForSubscription(supplierAdmin.getKey(),
                subDetails, newSubPriceModel, customer);

        // parameter change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        subDetails = subscrSetup.modifyParameterForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-03-30 11:15:00"),
                "HAS_OPTIONS", "1");
        // parameter change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        subDetails = subscrSetup.modifyParameterForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-03-30 12:15:00"),
                "HAS_OPTIONS", "2");

        // 1. upgrade service
        VOServiceDetails unitTemplate = serviceSetup
                .createAndPublishMarketableService(supplierAdmin.getKey(),
                        "PAIRWISE14_Upgrade", TestService.EXAMPLE2,
                        TestPriceModel.EXAMPLE_PICT15_UNIT_HOURS,
                        example2Service, supplierMarketplace);
        serviceSetup.registerCompatibleServices(supplierAdmin.getKey(),
                serviceTemplate, unitTemplate);
        VOServiceDetails unitH1 = serviceSetup.savePriceModelForCustomer(
                unitTemplate, TestPriceModel.EXAMPLE_PICT15_UNIT_HOURS,
                customer);
        unitH1 = serviceSetup.activateMarketableService(unitH1);

        setDateFactory("2013-04-01 10:15:00");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(subDetails, unitH1);

        // user is deassigned
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        setDateFactory("2013-04-01 10:15:01");
        upgradedSubDetails = subscrSetup.revokeUser(customerAdmin,
                upgradedSubDetails.getSubscriptionId());

        // user is assigned
        VORoleDefinition changedRole = VOServiceFactory.getRole(serviceDetails,
                "GUEST");
        setDateFactory("2013-04-01 10:15:02");
        upgradedSubDetails = subscrSetup.addUser(customerAdmin, changedRole,
                upgradedSubDetails.getSubscriptionId());

        // 2. upgrade service
        VOServiceDetails unitTemplate2 = serviceSetup
                .createAndPublishMarketableService(supplierAdmin.getKey(),
                        "PAIRWISE14_Upgrade2", TestService.EXAMPLE2,
                        TestPriceModel.FREE, example2Service,
                        supplierMarketplace);
        serviceSetup.registerCompatibleServices(supplierAdmin.getKey(),
                unitTemplate, unitTemplate2);
        VOServiceDetails unitH2 = serviceSetup.savePriceModelForCustomer(
                unitTemplate2, TestPriceModel.FREE, customer);
        unitH2 = serviceSetup.activateMarketableService(unitH2);

        setDateFactory("2013-04-01 11:15:00");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetails2 = subscrSetup
                .upgradeSubscription(upgradedSubDetails, unitH2);

        // role change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        setDateFactory("2013-04-01 11:15:01");
        upgradedSubDetails2 = subscrSetup.modifyUserRole(upgradedSubDetails2
                .getUsageLicenses().get(0), VOServiceFactory.getRole(
                serviceDetails, "USER"), upgradedSubDetails2
                .getSubscriptionId());

        // role change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        setDateFactory("2013-04-01 11:15:02");
        upgradedSubDetails2 = subscrSetup.modifyUserRole(upgradedSubDetails2
                .getUsageLicenses().get(0), VOServiceFactory.getRole(
                serviceDetails, "ADMIN"), upgradedSubDetails2
                .getSubscriptionId());

        // change sunbscriptionID
        setDateFactory("2013-04-01 11:16:00");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        upgradedSubDetails2.setSubscriptionId("PICT_TEST_14" + "_SubID2");
        upgradedSubDetails2 = subscrSetup.modifySubscription(
                upgradedSubDetails2, null);

        // unsubscribe
        setDateFactory("2013-04-03 10:15:00");
        subscrSetup.unsubscribeToService(upgradedSubDetails2
                .getSubscriptionId());

        // delete market service
        setDateFactory("2013-04-03 10:16:00");
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER);
        serviceSetup.deleteMarketableService(serviceDetails);

        resetCutOffDay(supplierAdmin.getKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_14", subDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_14", upgradedSubDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_14", upgradedSubDetails2);

        BillingIntegrationTestBase.updateCustomerListForTests("PICT_TEST_14",
                customer);

    }

}
