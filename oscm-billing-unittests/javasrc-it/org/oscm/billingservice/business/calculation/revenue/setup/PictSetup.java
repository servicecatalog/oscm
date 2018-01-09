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
import org.oscm.billingservice.setup.VOPriceModelFactory.TestPriceModel;
import org.oscm.billingservice.setup.VOServiceFactory;
import org.oscm.billingservice.setup.VOServiceFactory.TestService;
import org.oscm.billingservice.setup.VOTechServiceFactory;
import org.oscm.billingservice.setup.VOVatRateFactory;
import org.oscm.test.BaseAdmUmTest;
import org.oscm.test.DateTimeHandling;
import org.oscm.internal.types.enumtypes.OrganizationRoleType;
import org.oscm.internal.types.enumtypes.PriceModelType;
import org.oscm.internal.types.enumtypes.PricingPeriod;
import org.oscm.internal.vo.VOMarketplace;
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
public class PictSetup extends IntegrationTestSetup {

    /**
     * See testcase #1 in BESBillingFactorCombinations.xlsx
     */
    public void createPictScenario01() throws Exception {
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-05-01 00:00:00"));

        // Create a supplier with a marketplace
        String supplierAdminId = "Pict01Supplier";
        VOOrganization supplier = orgSetup.createOrganization(
                basicSetup.getPlatformOperatorUserKey(), supplierAdminId,
                "Pict01SupplierOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                OrganizationRoleType.TECHNOLOGY_PROVIDER,
                OrganizationRoleType.SUPPLIER);
        VOUser supplierAdmin = orgSetup.getUser(supplierAdminId, true);
        VOMarketplace supplMarketplace = orgSetup.createMarketplace(
                "Pict01Supplier_MP", false, supplier);
        paymentSetup.createPaymentForSupplier(
                basicSetup.getPlatformOperatorUserKey(),
                supplierAdmin.getKey(), supplier);

        setCutOffDay(supplierAdmin.getKey(), 5);

        // Create a customer with a discount
        String customerAdminId = "Pict01DiscountedCustomer";
        VOOrganization customer = orgSetup.registerCustomer(
                "Pict01DiscountedCustomerOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_UK,
                customerAdminId, supplMarketplace.getMarketplaceId(),
                supplier.getOrganizationId());
        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);
        orgSetup.updateCustomerDiscount(customer, new BigDecimal("15.00"),
                DateTimeHandling.calculateMillis("2013-05-06 00:00:00"),
                DateTimeHandling.calculateMillis("2013-05-16 23:59:59"));

        orgSetup.saveAllVats(supplierAdmin.getKey(),
                VOVatRateFactory.newVOVatRate(new BigDecimal("13.00")), //
                null, null);

        orgSetup.createMarketingPermission(basicSetup.getSupplierAdminKey(),
                supplier.getOrganizationId(), technicalService);
        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        supplierAdmin.getKey(), "PICT_TEST_01",
                        TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PICT01_RATA_DAY,
                        technicalService, supplMarketplace);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "PICT_TEST_01", serviceDetails, customerAdmin, role);

        // Upgrade the subscription to a per unit service
        VOServiceDetails perUnitService = serviceSetup
                .createPublishAndActivateMarketableService(
                        supplierAdmin.getKey(), "PICT01_PERUNIT_SERVICE",
                        TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_WEEK_ROLES,
                        technicalService, supplMarketplace);

        serviceSetup.registerCompatibleServices(supplierAdmin.getKey(),
                serviceDetails, perUnitService);

        long usageUpgradeTime = DateTimeHandling
                .calculateMillis("2013-05-21 13:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(usageUpgradeTime);
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(subDetails, perUnitService);

        // reset old cut off day
        resetCutOffDay(supplierAdmin.getKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_01", subDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_01", upgradedSubDetails);
        BillingIntegrationTestBase.updateCustomerListForTests("PICT_TEST_01",
                customer);
    }

    /**
     * See testcase #2 in BESBillingFactorCombinations.xlsx
     */
    public void createPictScenario02() throws Exception {
        long usageStartTime = DateTimeHandling
                .calculateMillis("2013-06-03 20:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(usageStartTime);

        // Create a supplier with a marketplace
        String supplierAdminId = "Pict02Supplier";
        VOOrganization supplier = orgSetup.createOrganization(
                basicSetup.getPlatformOperatorUserKey(), supplierAdminId,
                "Pict02SupplierOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                OrganizationRoleType.TECHNOLOGY_PROVIDER,
                OrganizationRoleType.SUPPLIER);
        VOUser supplierAdmin = orgSetup.getUser(supplierAdminId, true);
        VOMarketplace supplMarketplace = orgSetup.createMarketplace(
                "Pict02Supplier_MP", false, supplier);
        paymentSetup.createPaymentForSupplier(
                basicSetup.getPlatformOperatorUserKey(),
                supplierAdmin.getKey(), supplier);

        setCutOffDay(supplierAdmin.getKey(), 1);

        // Create own customer
        String customerAdminId = "PIC02Customer";
        VOOrganization customer = orgSetup.registerCustomer("PIC02CustomerOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_UK,
                customerAdminId, supplMarketplace.getMarketplaceId(),
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

        // setup free service with customer price model
        VOServiceDetails serviceFreeTempl = serviceSetup
                .createAndPublishMarketableService(supplierAdmin.getKey(),
                        "PICT_TEST_02", TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_RATA_WEEK_ROLES_PARS,
                        technicalService, supplMarketplace);
        VOServiceDetails serviceFree = serviceSetup.savePriceModelForCustomer(
                serviceFreeTempl, TestPriceModel.FREE, customer);
        serviceFree = serviceSetup.activateMarketableService(serviceFree);

        // create upgraded pro rata service with customer price model
        VOServiceDetails serviceProRataTempl = serviceSetup
                .createAndPublishMarketableService(supplierAdmin.getKey(),
                        "PICT_TEST_02_Upgrade", TestService.EXAMPLE,
                        TestPriceModel.FREE, technicalService, supplMarketplace);
        VOServiceDetails serviceProRata = serviceSetup
                .savePriceModelForCustomer(serviceProRataTempl,
                        TestPriceModel.EXAMPLE_PICT02_RATA_WEEK, customer);
        serviceProRata = serviceSetup.activateMarketableService(serviceProRata);

        // define upgrade path
        serviceSetup.registerCompatibleServices(supplierAdmin.getKey(),
                serviceFreeTempl, serviceProRataTempl);

        // create 2. upgraded pro rata service with customer price model
        VOServiceDetails serviceProRataTempl2 = serviceSetup
                .createAndPublishMarketableService(supplierAdmin.getKey(),
                        "PICT_TEST_02_Upgrade2", TestService.EXAMPLE,
                        TestPriceModel.FREE, technicalService, supplMarketplace);
        VOServiceDetails serviceProRata2 = serviceSetup
                .savePriceModelForCustomer(serviceProRataTempl2,
                        TestPriceModel.EXAMPLE_PICT02_RATA_WEEK2, customer);
        serviceProRata2 = serviceSetup
                .activateMarketableService(serviceProRata2);

        // define upgrade path
        serviceSetup.registerCompatibleServices(supplierAdmin.getKey(),
                serviceProRataTempl, serviceProRataTempl2);

        // subscribe to free service as customer
        VORoleDefinition role = VOServiceFactory.getRole(serviceFree, "ADMIN");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "PICT_TEST_02", serviceFree, customerAdmin, role);

        // user is deassigned in free period
        long userDeassignedTime = DateTimeHandling
                .calculateMillis("2013-06-18 10:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(userDeassignedTime);
        subscrSetup.revokeUser(customerAdmin, subDetails.getSubscriptionId());
        // user is assigned in free period
        long userAssignedTime = DateTimeHandling
                .calculateMillis("2013-06-19 10:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(userAssignedTime);
        subDetails = subscrSetup.addUser(customerAdmin, role,
                subDetails.getSubscriptionId());

        // upgrade to pro rata service
        long usageUpgradeTime = DateTimeHandling
                .calculateMillis("2013-07-01 00:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(usageUpgradeTime);
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(subDetails, serviceProRata);

        // delete free Marketable service
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-07-03 09:00:00"));
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER);
        serviceSetup.deleteMarketableService(serviceFree);

        // user is deassigned in 1st pro rata period
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        userDeassignedTime = DateTimeHandling
                .calculateMillis("2013-07-03 10:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(userDeassignedTime);
        upgradedSubDetails = subscrSetup.revokeUser(customerAdmin,
                upgradedSubDetails.getSubscriptionId());

        // change subscriptionID
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-07-03 14:00:00"));
        // container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        upgradedSubDetails.setSubscriptionId("PICT_TEST_02" + "_SubID2");
        upgradedSubDetails = subscrSetup.modifySubscription(upgradedSubDetails,
                null);

        // user is assigned in 1st pro rata period
        userAssignedTime = DateTimeHandling
                .calculateMillis("2013-07-10 10:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(userAssignedTime);
        upgradedSubDetails = subscrSetup.addUser(customerAdmin, role,
                upgradedSubDetails.getSubscriptionId());

        // role change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        long roleChangeTime = DateTimeHandling
                .calculateMillis("2013-07-15 00:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(roleChangeTime);
        upgradedSubDetails = subscrSetup
                .modifyUserRole(upgradedSubDetails.getUsageLicenses().get(0),
                        VOServiceFactory.getRole(serviceProRata, "USER"),
                        upgradedSubDetails.getSubscriptionId());

        // parameter change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        subDetails = subscrSetup.modifyParameterForSubscription(
                upgradedSubDetails,
                DateTimeHandling.calculateMillis("2013-07-15 00:00:00"),
                "MAX_FOLDER_NUMBER", "5");

        // upgrade to 2. pro rata service
        usageUpgradeTime = DateTimeHandling
                .calculateMillis("2013-07-22 00:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(usageUpgradeTime);
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetails2 = subscrSetup
                .upgradeSubscription(upgradedSubDetails, serviceProRata2);

        // delete pro rata marketable services
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-07-22 13:10:00"));
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER);
        serviceSetup.deleteMarketableService(serviceProRata);

        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-07-22 13:20:00"));
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER);
        serviceSetup.deleteMarketableService(serviceProRata2);

        // suspend/resume subscription by removing/restoring the customer's
        // payment types
        long suspResTime = DateTimeHandling
                .calculateMillis("2013-07-23 00:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(suspResTime);

        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);

        paymentSetup.deleteCustomerPaymentTypes(customer);

        long reassignResTime = DateTimeHandling
                .calculateMillis("2013-07-24 00:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(reassignResTime);
        paymentSetup.reassignCustomerPaymentTypes(customer);

        // terminate sub
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        long userterminateTime = DateTimeHandling
                .calculateMillis("2013-07-31 00:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(userterminateTime);
        subscrSetup
                .unsubscribeToService(upgradedSubDetails.getSubscriptionId());

        resetCutOffDay(supplierAdmin.getKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_02", subDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_02", upgradedSubDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_02", upgradedSubDetails2);
        BillingIntegrationTestBase.updateCustomerListForTests("PICT_TEST_02",
                customer);

    }

    /**
     * See testcase #3 (role costs = 0, no daylight savings change) in
     * BESBillingFactorCombinations.xlsx
     */
    public void createPictScenario03() throws Exception {
        long usageStartTime = DateTimeHandling
                .calculateMillis("2013-07-01 00:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(usageStartTime);

        // Create a supplier with a marketplace
        String supplierAdminId = "Pict03Supplier";
        VOOrganization supplier = orgSetup.createOrganization(
                basicSetup.getPlatformOperatorUserKey(), supplierAdminId,
                "Pict03SupplierOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                OrganizationRoleType.TECHNOLOGY_PROVIDER,
                OrganizationRoleType.SUPPLIER);
        VOUser supplierAdmin = orgSetup.getUser(supplierAdminId, true);
        VOMarketplace supplMarketplace = orgSetup.createMarketplace(
                "Pict03Supplier_MP", false, supplier);
        paymentSetup.createPaymentForSupplier(
                basicSetup.getPlatformOperatorUserKey(),
                supplierAdmin.getKey(), supplier);

        // Create own technical service
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        serviceSetup
                .importTechnicalServices(BaseAdmUmTest.TECHNICAL_SERVICE_EXAMPLE2_XML);
        VOTechnicalService example2TechService = serviceSetup
                .getTechnicalService(VOTechServiceFactory.TECH_SERVICE_EXAMPLE2_ID);

        setCutOffDay(supplierAdmin.getKey(), 1);

        // Create own customer
        String customerAdminId = "PIC03Customer";
        VOOrganization customer = orgSetup.registerCustomer("PIC03CustomerOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_UK,
                customerAdminId, supplMarketplace.getMarketplaceId(),
                supplier.getOrganizationId());

        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);

        // setup up time unit hour service
        VOServiceDetails serviceDetailsTemp = serviceSetup
                .createAndPublishMarketableService(supplierAdmin.getKey(),
                        "PICT_TEST_03", TestService.EXAMPLE2,
                        TestPriceModel.FREE, example2TechService,
                        supplMarketplace);
        VOServiceDetails serviceDetails = serviceSetup
                .savePriceModelForCustomer(serviceDetailsTemp,
                        TestPriceModel.EXAMPLE_PICT03_UNIT_HOUR, customer);
        serviceDetails = serviceSetup.activateMarketableService(serviceDetails);

        // subscribe to time unit hour service as customer
        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "PICT_TEST_03", serviceDetails, customerAdmin, role);

        // user is de-assigned and re-assigned in sub free period (first 20 days
        // are without charge)
        BillingIntegrationTestBase
                .setDateFactoryInstance("2013-07-02 10:00:00");
        subscrSetup.revokeUser(customerAdmin, subDetails.getSubscriptionId());
        BillingIntegrationTestBase
                .setDateFactoryInstance("2013-07-02 12:00:00");
        role = VOServiceFactory.getRole(serviceDetails, "USER");
        subDetails = subscrSetup.addUser(customerAdmin, role,
                subDetails.getSubscriptionId());
        BillingIntegrationTestBase
                .setDateFactoryInstance("2013-07-02 13:00:00");
        subscrSetup.revokeUser(customerAdmin, subDetails.getSubscriptionId());
        BillingIntegrationTestBase
                .setDateFactoryInstance("2013-07-02 14:00:00");
        subDetails = subscrSetup.addUser(customerAdmin, role,
                subDetails.getSubscriptionId());

        // user is de-assigned and re-assigned in charged period (from Jul 21
        // 0:00)
        // de-assign at 10:00:00 is within new time unit hr!!! (incl)
        BillingIntegrationTestBase
                .setDateFactoryInstance("2013-07-22 10:00:00");
        subscrSetup.revokeUser(customerAdmin, subDetails.getSubscriptionId());
        BillingIntegrationTestBase
                .setDateFactoryInstance("2013-07-22 10:10:00");
        role = VOServiceFactory.getRole(serviceDetails, "ADMIN");
        subDetails = subscrSetup.addUser(customerAdmin, role,
                subDetails.getSubscriptionId());

        // terminate sub
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        long userterminateTime = DateTimeHandling
                .calculateMillis("2013-08-01 10:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(userterminateTime);
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        // Delete marketable service
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-08-01 13:00:01"));
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER);
        serviceSetup.deleteMarketableService(serviceDetails);

        // Delete marketable service template
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-11-01 13:00:02"));
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER);
        serviceSetup.deleteMarketableService(serviceDetailsTemp);

        // Delete technical service
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-08-01 13:00:03"));
        container.login(supplierAdmin.getKey(), ROLE_TECHNOLOGY_MANAGER);
        serviceSetup.deleteTechnicalService(example2TechService);

        resetCutOffDay(supplierAdmin.getKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_03", subDetails);
        BillingIntegrationTestBase.updateCustomerListForTests("PICT_TEST_03",
                customer);
    }

    /**
     * See testcase #3 variation with role change before begin of time-unit hour
     * * BESBillingFactorCombinations.xlsx
     */
    public void createPictScenario03_1() throws Exception {
        long usageStartTime = DateTimeHandling
                .calculateMillis("2013-07-01 00:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(usageStartTime);

        // Create a supplier with a marketplace
        String supplierAdminId = "Pict03_1Supplier";
        VOOrganization supplier = orgSetup.createOrganization(
                basicSetup.getPlatformOperatorUserKey(), supplierAdminId,
                "Pict03_1SupplierOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                OrganizationRoleType.TECHNOLOGY_PROVIDER,
                OrganizationRoleType.SUPPLIER);
        VOUser supplierAdmin = orgSetup.getUser(supplierAdminId, true);
        VOMarketplace supplMarketplace = orgSetup.createMarketplace(
                "Pict03_1Supplier_MP", false, supplier);
        paymentSetup.createPaymentForSupplier(
                basicSetup.getPlatformOperatorUserKey(),
                supplierAdmin.getKey(), supplier);

        // Create own technical service
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        serviceSetup
                .importTechnicalServices(BaseAdmUmTest.TECHNICAL_SERVICE_EXAMPLE2_XML);
        VOTechnicalService example2TechService = serviceSetup
                .getTechnicalService(VOTechServiceFactory.TECH_SERVICE_EXAMPLE2_ID);

        setCutOffDay(supplierAdmin.getKey(), 1);

        // Create own customer
        String customerAdminId = "PIC03_1Customer";
        VOOrganization customer = orgSetup.registerCustomer(
                "PIC03_1CustomerOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_UK,
                customerAdminId, supplMarketplace.getMarketplaceId(),
                supplier.getOrganizationId());

        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);

        // setup up time unit hour service
        VOServiceDetails serviceDetailsTemp = serviceSetup
                .createAndPublishMarketableService(supplierAdmin.getKey(),
                        "PICT_TEST_03_1", TestService.EXAMPLE2,
                        TestPriceModel.FREE, example2TechService,
                        supplMarketplace);
        VOServiceDetails serviceDetails = serviceSetup
                .savePriceModelForCustomer(serviceDetailsTemp,
                        TestPriceModel.EXAMPLE_PICT03_UNIT_HOUR, customer);
        serviceDetails = serviceSetup.activateMarketableService(serviceDetails);

        // subscribe to time unit hour service as customer
        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "PICT_TEST_03_1", serviceDetails, customerAdmin, role);

        // user is de-assigned and re-assigned in sub free period (first 20 days
        // are without charge)
        long userDeassignedTime = DateTimeHandling
                .calculateMillis("2013-07-02 10:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(userDeassignedTime);
        subscrSetup.revokeUser(customerAdmin, subDetails.getSubscriptionId());
        // user is re-assigned in free period
        long userAssignedTime = DateTimeHandling
                .calculateMillis("2013-07-02 12:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(userAssignedTime);
        // reassign with role USER
        role = VOServiceFactory.getRole(serviceDetails, "USER");
        subDetails = subscrSetup.addUser(customerAdmin, role,
                subDetails.getSubscriptionId());
        userDeassignedTime = DateTimeHandling
                .calculateMillis("2013-07-02 13:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(userDeassignedTime);
        subscrSetup.revokeUser(customerAdmin, subDetails.getSubscriptionId());
        // user is re-assigned in free period
        userAssignedTime = DateTimeHandling
                .calculateMillis("2013-07-02 14:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(userAssignedTime);
        subDetails = subscrSetup.addUser(customerAdmin, role,
                subDetails.getSubscriptionId());

        // user is de-assigned and re-assigned in charged period (from Jul 21
        // 0:00)
        // de-assign at 10:00:00 is just BEFORE new time unit hr!!!
        userDeassignedTime = DateTimeHandling
                .calculateMillis("2013-07-22 09:59:59");
        BillingIntegrationTestBase.setDateFactoryInstance(userDeassignedTime);
        subscrSetup.revokeUser(customerAdmin, subDetails.getSubscriptionId());

        userAssignedTime = DateTimeHandling
                .calculateMillis("2013-07-22 10:10:00");
        BillingIntegrationTestBase.setDateFactoryInstance(userAssignedTime);
        // reassign with role ADMIN
        role = VOServiceFactory.getRole(serviceDetails, "ADMIN");
        subDetails = subscrSetup.addUser(customerAdmin, role,
                subDetails.getSubscriptionId());

        // terminate sub
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        long userterminateTime = DateTimeHandling
                .calculateMillis("2013-08-01 10:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(userterminateTime);
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        // Delete the marketable service
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-08-01 13:00:01"));
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER);
        serviceSetup.deleteMarketableService(serviceDetails);

        // Delete marketable service template
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-08-01 13:00:02"));
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER);
        serviceSetup.deleteMarketableService(serviceDetailsTemp);

        // Delete also the technical service
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-08-01 13:00:03"));
        container.login(supplierAdmin.getKey(), ROLE_TECHNOLOGY_MANAGER);
        serviceSetup.deleteTechnicalService(example2TechService);

        resetCutOffDay(supplierAdmin.getKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_03_1", subDetails);
        BillingIntegrationTestBase.updateCustomerListForTests("PICT_TEST_03_1",
                customer);
    }

    /**
     * See testcase #3 of BESBillingFactorCombinations.xlsx variation of
     * testcase #3 with role change within time-unit hour
     */
    public void createPictScenario03_2() throws Exception {
        long usageStartTime = DateTimeHandling
                .calculateMillis("2013-07-01 00:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(usageStartTime);

        // Create a supplier with a marketplace
        String supplierAdminId = "Pict03_2Supplier";
        VOOrganization supplier = orgSetup.createOrganization(
                basicSetup.getPlatformOperatorUserKey(), supplierAdminId,
                "Pict03_2SupplierOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                OrganizationRoleType.TECHNOLOGY_PROVIDER,
                OrganizationRoleType.SUPPLIER);
        VOUser supplierAdmin = orgSetup.getUser(supplierAdminId, true);
        VOMarketplace supplMarketplace = orgSetup.createMarketplace(
                "Pict03_2Supplier_MP", false, supplier);
        paymentSetup.createPaymentForSupplier(
                basicSetup.getPlatformOperatorUserKey(),
                supplierAdmin.getKey(), supplier);

        // Create own technical service
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        serviceSetup
                .importTechnicalServices(BaseAdmUmTest.TECHNICAL_SERVICE_EXAMPLE2_XML);
        VOTechnicalService example2TechService = serviceSetup
                .getTechnicalService(VOTechServiceFactory.TECH_SERVICE_EXAMPLE2_ID);

        setCutOffDay(supplierAdmin.getKey(), 1);

        // Create own customer
        String customerAdminId = "PIC03_2Customer";
        VOOrganization customer = orgSetup.registerCustomer(
                "PIC03_2CustomerOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_UK,
                customerAdminId, supplMarketplace.getMarketplaceId(),
                supplier.getOrganizationId());

        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);

        // setup up time unit hour service
        VOServiceDetails serviceDetailsTemp = serviceSetup
                .createAndPublishMarketableService(supplierAdmin.getKey(),
                        "PICT_TEST_03_2", TestService.EXAMPLE2,
                        TestPriceModel.FREE, example2TechService,
                        supplMarketplace);
        VOServiceDetails serviceDetails = serviceSetup
                .savePriceModelForCustomer(serviceDetailsTemp,
                        TestPriceModel.EXAMPLE_PICT03_UNIT_HOUR, customer);
        serviceDetails = serviceSetup.activateMarketableService(serviceDetails);

        // subscribe to time unit hour service as customer
        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "PICT_TEST_03_2", serviceDetails, customerAdmin, role);

        // user is de-assigned and re-assigned in sub free period (first 20 days
        // are without charge)
        long userDeassignedTime = DateTimeHandling
                .calculateMillis("2013-07-02 10:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(userDeassignedTime);
        subscrSetup.revokeUser(customerAdmin, subDetails.getSubscriptionId());
        // user is re-assigned in free period
        long userAssignedTime = DateTimeHandling
                .calculateMillis("2013-07-02 12:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(userAssignedTime);
        // reassign with role USER
        role = VOServiceFactory.getRole(serviceDetails, "USER");
        subDetails = subscrSetup.addUser(customerAdmin, role,
                subDetails.getSubscriptionId());
        userDeassignedTime = DateTimeHandling
                .calculateMillis("2013-07-02 13:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(userDeassignedTime);
        subscrSetup.revokeUser(customerAdmin, subDetails.getSubscriptionId());
        // user is re-assigned in free period
        userAssignedTime = DateTimeHandling
                .calculateMillis("2013-07-02 14:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(userAssignedTime);
        subDetails = subscrSetup.addUser(customerAdmin, role,
                subDetails.getSubscriptionId());

        // user is de-assigned and re-assigned in charged period (from Jul 21
        // 0:00)
        userDeassignedTime = DateTimeHandling
                .calculateMillis("2013-07-22 10:10:00");
        BillingIntegrationTestBase.setDateFactoryInstance(userDeassignedTime);
        subscrSetup.revokeUser(customerAdmin, subDetails.getSubscriptionId());

        userAssignedTime = DateTimeHandling
                .calculateMillis("2013-07-22 10:20:00");
        BillingIntegrationTestBase.setDateFactoryInstance(userAssignedTime);
        // reassign with role ADMIN
        role = VOServiceFactory.getRole(serviceDetails, "ADMIN");
        subDetails = subscrSetup.addUser(customerAdmin, role,
                subDetails.getSubscriptionId());

        // terminate sub
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        long userterminateTime = DateTimeHandling
                .calculateMillis("2013-08-01 10:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(userterminateTime);
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        // Delete the marketable service
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-08-01 13:00:01"));
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER);
        serviceSetup.deleteMarketableService(serviceDetails);

        // Delete marketable service template
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-11-01 13:00:02"));
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER);
        serviceSetup.deleteMarketableService(serviceDetailsTemp);

        // Delete also technical service
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-08-01 13:00:03"));
        container.login(supplierAdmin.getKey(), ROLE_TECHNOLOGY_MANAGER);
        serviceSetup.deleteTechnicalService(example2TechService);

        resetCutOffDay(supplierAdmin.getKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_03_2", subDetails);
        BillingIntegrationTestBase.updateCustomerListForTests("PICT_TEST_03_2",
                customer);
    }

    /**
     * See testcase #3 of BESBillingFactorCombinations.xlsx variation of
     * testcase #3 with role change within time-unit hour w daylight savings
     * change in same month March (simple version)
     */
    public void createPictScenario03_3() throws Exception {
        long usageStartTime = DateTimeHandling
                .calculateMillis("2013-03-01 00:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(usageStartTime);

        // Create a supplier with a marketplace
        String supplierAdminId = "Pict03_3Supplier";
        VOOrganization supplier = orgSetup.createOrganization(
                basicSetup.getPlatformOperatorUserKey(), supplierAdminId,
                "Pict03_3SupplierOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                OrganizationRoleType.TECHNOLOGY_PROVIDER,
                OrganizationRoleType.SUPPLIER);
        VOUser supplierAdmin = orgSetup.getUser(supplierAdminId, true);
        VOMarketplace supplMarketplace = orgSetup.createMarketplace(
                "Pict03_3Supplier_MP", false, supplier);
        paymentSetup.createPaymentForSupplier(
                basicSetup.getPlatformOperatorUserKey(),
                supplierAdmin.getKey(), supplier);

        // Create own technical service
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        serviceSetup
                .importTechnicalServices(BaseAdmUmTest.TECHNICAL_SERVICE_EXAMPLE2_XML);
        VOTechnicalService example2TechService = serviceSetup
                .getTechnicalService(VOTechServiceFactory.TECH_SERVICE_EXAMPLE2_ID);

        setCutOffDay(supplierAdmin.getKey(), 1);

        // Create own customer
        String customerAdminId = "PIC03_3Customer";
        VOOrganization customer = orgSetup.registerCustomer(
                "PIC03_3CustomerOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_UK,
                customerAdminId, supplMarketplace.getMarketplaceId(),
                supplier.getOrganizationId());

        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);

        // setup up time unit hour service
        VOServiceDetails serviceDetailsTemp = serviceSetup
                .createAndPublishMarketableService(supplierAdmin.getKey(),
                        "PICT_TEST_03_3", TestService.EXAMPLE2,
                        TestPriceModel.FREE, example2TechService,
                        supplMarketplace);
        VOServiceDetails serviceDetails = serviceSetup
                .savePriceModelForCustomer(serviceDetailsTemp,
                        TestPriceModel.EXAMPLE_PICT03_UNIT_HOUR, customer);
        serviceDetails = serviceSetup.activateMarketableService(serviceDetails);

        // subscribe to time unit hour service as customer
        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "PICT_TEST_03_3", serviceDetails, customerAdmin, role);

        // user is de-assigned and re-assigned in sub free period (first 20 days
        // are without charge)
        long userDeassignedTime = DateTimeHandling
                .calculateMillis("2013-03-02 10:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(userDeassignedTime);
        subscrSetup.revokeUser(customerAdmin, subDetails.getSubscriptionId());
        // user is re-assigned in free period
        long userAssignedTime = DateTimeHandling
                .calculateMillis("2013-03-02 12:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(userAssignedTime);
        // reassign with role USER
        role = VOServiceFactory.getRole(serviceDetails, "USER");
        subDetails = subscrSetup.addUser(customerAdmin, role,
                subDetails.getSubscriptionId());
        userDeassignedTime = DateTimeHandling
                .calculateMillis("2013-03-02 13:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(userDeassignedTime);
        subscrSetup.revokeUser(customerAdmin, subDetails.getSubscriptionId());
        // user is re-assigned in free period
        userAssignedTime = DateTimeHandling
                .calculateMillis("2013-03-02 14:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(userAssignedTime);
        subDetails = subscrSetup.addUser(customerAdmin, role,
                subDetails.getSubscriptionId());

        // user is de-assigned and re-assigned in charged period (from Oct 21
        // 0:00)
        userDeassignedTime = DateTimeHandling
                .calculateMillis("2013-03-22 10:10:00");
        BillingIntegrationTestBase.setDateFactoryInstance(userDeassignedTime);
        subscrSetup.revokeUser(customerAdmin, subDetails.getSubscriptionId());

        userAssignedTime = DateTimeHandling
                .calculateMillis("2013-03-22 10:20:00");
        BillingIntegrationTestBase.setDateFactoryInstance(userAssignedTime);
        // reassign with role ADMIN
        role = VOServiceFactory.getRole(serviceDetails, "ADMIN");
        subDetails = subscrSetup.addUser(customerAdmin, role,
                subDetails.getSubscriptionId());

        // terminate sub
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        long userterminateTime = DateTimeHandling
                .calculateMillis("2013-04-01 10:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(userterminateTime);
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        // Delete marketable service
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-04-01 13:00:01"));
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER);
        serviceSetup.deleteMarketableService(serviceDetails);

        // Delete marketable service template
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-11-01 13:00:02"));
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER);
        serviceSetup.deleteMarketableService(serviceDetailsTemp);

        // Delete technical service
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-04-01 13:00:03"));
        container.login(supplierAdmin.getKey(), ROLE_TECHNOLOGY_MANAGER);
        serviceSetup.deleteTechnicalService(example2TechService);

        resetCutOffDay(supplierAdmin.getKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_03_3", subDetails);
        BillingIntegrationTestBase.updateCustomerListForTests("PICT_TEST_03_3",
                customer);
    }

    /**
     * See testcase #3 of BESBillingFactorCombinations.xlsx variation of
     * testcase #3 with role change within time-unit hour - for October incl
     * daylight savings time change
     */
    public void createPictScenario03_4() throws Exception {
        long usageStartTime = DateTimeHandling
                .calculateMillis("2013-10-01 00:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(usageStartTime);

        // Create a supplier with a marketplace
        String supplierAdminId = "Pict03_4Supplier";
        VOOrganization supplier = orgSetup.createOrganization(
                basicSetup.getPlatformOperatorUserKey(), supplierAdminId,
                "Pict03_4SupplierOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                OrganizationRoleType.TECHNOLOGY_PROVIDER,
                OrganizationRoleType.SUPPLIER);
        VOUser supplierAdmin = orgSetup.getUser(supplierAdminId, true);
        VOMarketplace supplMarketplace = orgSetup.createMarketplace(
                "Pict03_4Supplier_MP", false, supplier);
        paymentSetup.createPaymentForSupplier(
                basicSetup.getPlatformOperatorUserKey(),
                supplierAdmin.getKey(), supplier);

        // Create own technical service
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        serviceSetup
                .importTechnicalServices(BaseAdmUmTest.TECHNICAL_SERVICE_EXAMPLE2_XML);
        VOTechnicalService example2TechService = serviceSetup
                .getTechnicalService(VOTechServiceFactory.TECH_SERVICE_EXAMPLE2_ID);

        setCutOffDay(supplierAdmin.getKey(), 1);

        // Create own customer
        String customerAdminId = "PIC03_4Customer";
        VOOrganization customer = orgSetup.registerCustomer(
                "PIC03_4CustomerOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_UK,
                customerAdminId, supplMarketplace.getMarketplaceId(),
                supplier.getOrganizationId());

        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);

        // setup up time unit hour service
        VOServiceDetails serviceDetailsTemp = serviceSetup
                .createAndPublishMarketableService(supplierAdmin.getKey(),
                        "PICT_TEST_03_4", TestService.EXAMPLE2,
                        TestPriceModel.FREE, example2TechService,
                        supplMarketplace);
        VOServiceDetails serviceDetails = serviceSetup
                .savePriceModelForCustomer(serviceDetailsTemp,
                        TestPriceModel.EXAMPLE_PICT03_UNIT_HOUR, customer);
        serviceDetails = serviceSetup.activateMarketableService(serviceDetails);

        // subscribe to time unit hour service as customer
        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "PICT_TEST_03_4", serviceDetails, customerAdmin, role);

        // user is de-assigned and re-assigned in sub free period (first 20 days
        // are without charge)
        long userDeassignedTime = DateTimeHandling
                .calculateMillis("2013-10-02 10:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(userDeassignedTime);
        subscrSetup.revokeUser(customerAdmin, subDetails.getSubscriptionId());
        // user is re-assigned in free period
        long userAssignedTime = DateTimeHandling
                .calculateMillis("2013-10-02 12:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(userAssignedTime);
        // reassign with role USER
        role = VOServiceFactory.getRole(serviceDetails, "USER");
        subDetails = subscrSetup.addUser(customerAdmin, role,
                subDetails.getSubscriptionId());
        userDeassignedTime = DateTimeHandling
                .calculateMillis("2013-10-02 13:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(userDeassignedTime);
        subscrSetup.revokeUser(customerAdmin, subDetails.getSubscriptionId());
        // user is re-assigned in free period
        userAssignedTime = DateTimeHandling
                .calculateMillis("2013-10-02 14:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(userAssignedTime);
        subDetails = subscrSetup.addUser(customerAdmin, role,
                subDetails.getSubscriptionId());

        // user is de-assigned and re-assigned in charged period (from Oct 21
        // 0:00)
        userDeassignedTime = DateTimeHandling
                .calculateMillis("2013-10-22 10:10:00");
        BillingIntegrationTestBase.setDateFactoryInstance(userDeassignedTime);
        subscrSetup.revokeUser(customerAdmin, subDetails.getSubscriptionId());

        userAssignedTime = DateTimeHandling
                .calculateMillis("2013-10-22 10:20:00");
        BillingIntegrationTestBase.setDateFactoryInstance(userAssignedTime);
        // reassign with role ADMIN
        role = VOServiceFactory.getRole(serviceDetails, "ADMIN");
        subDetails = subscrSetup.addUser(customerAdmin, role,
                subDetails.getSubscriptionId());

        // terminate sub
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        long userterminateTime = DateTimeHandling
                .calculateMillis("2013-11-01 10:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(userterminateTime);
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        // Delete the marketable service
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-11-01 13:00:01"));
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER);
        serviceSetup.deleteMarketableService(serviceDetails);

        // Delete the marketable service template
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-11-01 13:00:02"));
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER);
        serviceSetup.deleteMarketableService(serviceDetailsTemp);

        // Delete technical service
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-11-01 13:00:03"));
        container.login(supplierAdmin.getKey(), ROLE_TECHNOLOGY_MANAGER);
        serviceSetup.deleteTechnicalService(example2TechService);

        resetCutOffDay(supplierAdmin.getKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_03_4", subDetails);
        BillingIntegrationTestBase.updateCustomerListForTests("PICT_TEST_03_4",
                customer);
    }

    /**
     * See testcase #3 of BESBillingFactorCombinations.xlsx variation of
     * testcase #3 with role change within time-unit hour w daylight savings
     * change in same month March - incl parameter change, events
     */
    public void createPictScenario03_5() throws Exception {
        long usageStartTime = DateTimeHandling
                .calculateMillis("2013-03-01 00:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(usageStartTime);

        // Create a supplier with a marketplace
        String supplierAdminId = "Pict03_5Supplier";
        VOOrganization supplier = orgSetup.createOrganization(
                basicSetup.getPlatformOperatorUserKey(), supplierAdminId,
                "Pict03_5SupplierOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                OrganizationRoleType.TECHNOLOGY_PROVIDER,
                OrganizationRoleType.SUPPLIER);
        VOUser supplierAdmin = orgSetup.getUser(supplierAdminId, true);
        VOMarketplace supplMarketplace = orgSetup.createMarketplace(
                "Pict03_5Supplier_MP", false, supplier);
        paymentSetup.createPaymentForSupplier(
                basicSetup.getPlatformOperatorUserKey(),
                supplierAdmin.getKey(), supplier);

        // Create own technical service
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        serviceSetup
                .importTechnicalServices(BaseAdmUmTest.TECHNICAL_SERVICE_EXAMPLE2_XML);
        VOTechnicalService example2TechService = serviceSetup
                .getTechnicalService(VOTechServiceFactory.TECH_SERVICE_EXAMPLE2_ID);

        setCutOffDay(supplierAdmin.getKey(), 1);

        // Create own customer
        String customerAdminId = "PIC03_5Customer";
        VOOrganization customer = orgSetup.registerCustomer(
                "PIC03_5CustomerOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_UK,
                customerAdminId, supplMarketplace.getMarketplaceId(),
                supplier.getOrganizationId());

        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);

        // setup up time unit hour service
        VOServiceDetails serviceDetailsTemp = serviceSetup
                .createAndPublishMarketableService(supplierAdmin.getKey(),
                        "PICT_TEST_03_5", TestService.EXAMPLE2,
                        TestPriceModel.FREE, example2TechService,
                        supplMarketplace);
        VOServiceDetails serviceDetails = serviceSetup
                .savePriceModelForCustomer(serviceDetailsTemp,
                        TestPriceModel.EXAMPLE_PICT03_5_UNIT_HOUR, customer);
        serviceDetails = serviceSetup.activateMarketableService(serviceDetails);

        // subscribe to time unit hour service as customer
        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "PICT_TEST_03_5", serviceDetails, customerAdmin, role);

        // user is de-assigned and re-assigned in sub free period (first 20 days
        // are without charge)
        long userDeassignedTime = DateTimeHandling
                .calculateMillis("2013-03-02 10:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(userDeassignedTime);
        subscrSetup.revokeUser(customerAdmin, subDetails.getSubscriptionId());
        // user is re-assigned in free period
        long userAssignedTime = DateTimeHandling
                .calculateMillis("2013-03-02 12:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(userAssignedTime);
        // reassign with role USER
        role = VOServiceFactory.getRole(serviceDetails, "USER");
        subDetails = subscrSetup.addUser(customerAdmin, role,
                subDetails.getSubscriptionId());
        userDeassignedTime = DateTimeHandling
                .calculateMillis("2013-03-02 13:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(userDeassignedTime);
        subscrSetup.revokeUser(customerAdmin, subDetails.getSubscriptionId());
        // user is re-assigned in free period
        userAssignedTime = DateTimeHandling
                .calculateMillis("2013-03-02 14:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(userAssignedTime);
        subDetails = subscrSetup.addUser(customerAdmin, role,
                subDetails.getSubscriptionId());

        // Event in free period
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        subscrSetup.recordEventForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-03-03 05:00:00"),
                "FILE_DOWNLOAD", 1);
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);

        // user is de-assigned and re-assigned in charged period (from Oct 21
        // 0:00)
        userDeassignedTime = DateTimeHandling
                .calculateMillis("2013-03-22 10:10:00");
        BillingIntegrationTestBase.setDateFactoryInstance(userDeassignedTime);
        subDetails = subscrSetup.revokeUser(customerAdmin,
                subDetails.getSubscriptionId());

        userAssignedTime = DateTimeHandling
                .calculateMillis("2013-03-22 10:20:00");
        BillingIntegrationTestBase.setDateFactoryInstance(userAssignedTime);
        // at same time as role change, change parameter value too.
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        subDetails = subscrSetup.modifyParameterForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-03-22 10:20:00"),
                "PERIOD", DateTimeHandling.daysToMillis(200) + "");
        // reassign with role ADMIN
        role = VOServiceFactory.getRole(serviceDetails, "ADMIN");
        subDetails = subscrSetup.addUser(customerAdmin, role,
                subDetails.getSubscriptionId());

        // Event in charged period
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        subscrSetup.recordEventForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-03-23 05:00:00"),
                "FILE_UPLOAD", 1);

        // terminate sub
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        long userterminateTime = DateTimeHandling
                .calculateMillis("2013-04-01 10:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(userterminateTime);
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        // Delete marketable service
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-04-01 13:00:01"));
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER);
        serviceSetup.deleteMarketableService(serviceDetails);

        // Delete marketable service template
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-04-01 13:00:02"));
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER);
        serviceSetup.deleteMarketableService(serviceDetailsTemp);

        // Delete technical service
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-04-01 13:00:03"));
        container.login(supplierAdmin.getKey(), ROLE_TECHNOLOGY_MANAGER);
        serviceSetup.deleteTechnicalService(example2TechService);

        resetCutOffDay(supplierAdmin.getKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_03_5", subDetails);
        BillingIntegrationTestBase.updateCustomerListForTests("PICT_TEST_03_5",
                customer);
    }

    /**
     * See testcase #4 in BESBillingFactorCombinations.xlsx
     */
    public void createPictScenario04() throws Exception {
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-03-01 00:00:00"));

        // Create a supplier
        String supplierAdminId = "Pict04Supplier";
        VOOrganization supplier = orgSetup.createOrganization(
                basicSetup.getPlatformOperatorUserKey(), supplierAdminId,
                "Pict04SupplierOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                OrganizationRoleType.TECHNOLOGY_PROVIDER,
                OrganizationRoleType.SUPPLIER);
        VOUser supplierAdmin = orgSetup.getUser(supplierAdminId, true);
        paymentSetup.createPaymentForSupplier(
                basicSetup.getPlatformOperatorUserKey(),
                supplierAdmin.getKey(), supplier);

        setCutOffDay(supplierAdmin.getKey(), 7);

        // Create a customer with a discount
        String customerAdminId = "Pict04DiscountedCustomer";
        VOOrganization customer = orgSetup.registerCustomer(
                "Pict04DiscountedCustomerOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_UK,
                customerAdminId, supplierMarketplace.getMarketplaceId(),
                supplier.getOrganizationId());
        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);
        orgSetup.updateCustomerDiscount(customer, new BigDecimal("10.00"),
                DateTimeHandling.calculateMillis("2013-03-04 00:00:00"),
                DateTimeHandling.calculateMillis("2013-03-16 23:59:59"));

        orgSetup.saveAllVats(supplierAdmin.getKey(), VOVatRateFactory
                .newVOVatRate(new BigDecimal("13.00")), Arrays.asList(
                VOVatRateFactory.newVOCountryVatRate(new BigDecimal("20.00"),
                        TestOrganizationSetup.ORGANIZATION_DOMICILE_UK),
                VOVatRateFactory.newVOCountryVatRate(new BigDecimal("19.00"),
                        TestOrganizationSetup.ORGANIZATION_DOMICILE_DE)),
                Arrays.asList(VOVatRateFactory.newVOOrganizationVatRate(
                        new BigDecimal("17.00"), customer)));

        orgSetup.createMarketingPermission(basicSetup.getSupplierAdminKey(),
                supplier.getOrganizationId(), technicalService);
        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        supplierAdmin.getKey(), "PICT_TEST_04",
                        TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PICT04_PERUNIT_DAY,
                        technicalService, supplierMarketplace);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "GUEST");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "PICT_TEST_04", serviceDetails, customerAdmin, role);

        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-03-05 00:00:00"));
        subDetails = subscrSetup.modifyUserRole(subDetails.getUsageLicenses()
                .get(0), VOServiceFactory.getRole(serviceDetails, "USER"),
                subDetails.getSubscriptionId());

        subDetails = subscrSetup.modifyParameterForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-03-06 00:00:00"),
                "PERIOD", DateTimeHandling.daysToMillis(100) + "");

        // Change price model of subscription
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-03-07 06:00:00"));
        VOPriceModel newSubPriceModel = VOPriceModelFactory.modifyPriceModel(
                subDetails.getPriceModel(), PriceModelType.PER_UNIT,
                PricingPeriod.DAY, "EUR", new BigDecimal("0.00"),
                new BigDecimal("7.00"), new BigDecimal("8.00"), 4);
        subscrSetup.savePriceModelForSubscription(supplierAdmin.getKey(),
                subDetails, newSubPriceModel, customer);

        // Suspend and resume the subscription by deleting the customer's
        // payment types
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-03-07 12:00:00"));
        paymentSetup.deleteCustomerPaymentTypes(customer);
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-03-07 18:00:00"));
        paymentSetup.reassignCustomerPaymentTypes(customer);
        subDetails = subscrSetup.getSubscriptionDetails(customerAdmin.getKey(),
                subDetails.getSubscriptionId());

        container.login(basicSetup.getSupplierAdminKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        subscrSetup.recordEventForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-03-09 12:00:00"),
                "FILE_DOWNLOAD", 15);
        subscrSetup.recordEventForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-03-09 18:00:00"),
                "FILE_UPLOAD", 3);

        // Upgrade the subscription to a free service
        VOServiceDetails freeService = serviceSetup
                .createPublishAndActivateMarketableService(
                        supplierAdmin.getKey(), "PICT04_FREE_SERVICE",
                        TestService.EXAMPLE, TestPriceModel.FREE,
                        technicalService, supplierMarketplace);
        serviceSetup.registerCompatibleServices(supplierAdmin.getKey(),
                serviceDetails, freeService);

        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-03-11 00:00:00"));
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(subDetails, freeService);

        upgradedSubDetails = subscrSetup.modifyParameterForSubscription(
                upgradedSubDetails,
                DateTimeHandling.calculateMillis("2013-03-12 00:00:00"),
                "PERIOD", DateTimeHandling.daysToMillis(110) + "");

        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-03-14 00:00:00"));
        upgradedSubDetails.setSubscriptionId("PICT_TEST_04" + "_SubID2");
        upgradedSubDetails = subscrSetup.modifySubscription(upgradedSubDetails,
                null);

        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-03-15 12:00:00"));
        upgradedSubDetails = subscrSetup.revokeUser(customerAdmin,
                upgradedSubDetails.getSubscriptionId());

        // Upgrade the subscription to a pro rata service
        VOServiceDetails proRataService = serviceSetup
                .createPublishAndActivateMarketableService(
                        supplierAdmin.getKey(), "PICT04_PRORATA_SERVICE",
                        TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PRORATA_MONTH_ROLES,
                        technicalService, supplierMarketplace);
        serviceSetup.registerCompatibleServices(supplierAdmin.getKey(),
                freeService, proRataService);

        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-03-17 00:00:00"));
        VOSubscriptionDetails upgradedSubDetails2 = subscrSetup
                .upgradeSubscription(upgradedSubDetails, proRataService);

        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-03-18 12:00:00"));
        upgradedSubDetails2 = subscrSetup.addUser(customerAdmin,
                VOServiceFactory.getRole(serviceDetails, "USER"),
                upgradedSubDetails2.getSubscriptionId());

        upgradedSubDetails2 = subscrSetup.modifyParameterForSubscription(
                upgradedSubDetails2,
                DateTimeHandling.calculateMillis("2013-03-24 00:00:00"),
                "PERIOD", DateTimeHandling.daysToMillis(90) + "");

        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-03-28 12:00:00"));
        upgradedSubDetails2 = subscrSetup.modifyUserRole(upgradedSubDetails2
                .getUsageLicenses().get(0), VOServiceFactory.getRole(
                proRataService, "ADMIN"), upgradedSubDetails2
                .getSubscriptionId());

        // reset old cut off day
        resetCutOffDay(supplierAdmin.getKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_04", subDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_04", upgradedSubDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_04", upgradedSubDetails2);
        BillingIntegrationTestBase.updateCustomerListForTests("PICT_TEST_04",
                customer);
    }

    /**
     * A subscription is suspended and resumed in the same billing period, but
     * in different time units (per unit/Day price model). The subscription ID
     * is changed after the subscription resume. The new subscription ID (last
     * ID in the billing period) must be used for the whole billing result.
     */
    public void createPictScenario04_1() throws Exception {
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-03-01 00:00:00"));

        // Create a supplier
        String supplierAdminId = "Pict04_1Supplier";
        VOOrganization supplier = orgSetup.createOrganization(
                basicSetup.getPlatformOperatorUserKey(), supplierAdminId,
                "Pict04_1SupplierOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                OrganizationRoleType.TECHNOLOGY_PROVIDER,
                OrganizationRoleType.SUPPLIER);
        VOUser supplierAdmin = orgSetup.getUser(supplierAdminId, true);
        paymentSetup.createPaymentForSupplier(
                basicSetup.getPlatformOperatorUserKey(),
                supplierAdmin.getKey(), supplier);

        setCutOffDay(supplierAdmin.getKey(), 1);

        // Create a customer with a discount
        String customerAdminId = "Pict04_1DiscountedCustomer";
        VOOrganization customer = orgSetup.registerCustomer(
                "Pict04_1DiscountedCustomerOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_UK,
                customerAdminId, supplierMarketplace.getMarketplaceId(),
                supplier.getOrganizationId());
        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);
        orgSetup.updateCustomerDiscount(customer, new BigDecimal("10.00"),
                DateTimeHandling.calculateMillis("2013-03-04 00:00:00"),
                DateTimeHandling.calculateMillis("2013-03-16 23:59:59"));

        orgSetup.saveAllVats(supplierAdmin.getKey(), VOVatRateFactory
                .newVOVatRate(new BigDecimal("13.00")), Arrays.asList(
                VOVatRateFactory.newVOCountryVatRate(new BigDecimal("20.00"),
                        TestOrganizationSetup.ORGANIZATION_DOMICILE_UK),
                VOVatRateFactory.newVOCountryVatRate(new BigDecimal("19.00"),
                        TestOrganizationSetup.ORGANIZATION_DOMICILE_DE)),
                Arrays.asList(VOVatRateFactory.newVOOrganizationVatRate(
                        new BigDecimal("17.00"), customer)));

        orgSetup.createMarketingPermission(basicSetup.getSupplierAdminKey(),
                supplier.getOrganizationId(), technicalService);
        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        supplierAdmin.getKey(), "PICT_TEST_04_1",
                        TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PICT04_PERUNIT_DAY,
                        technicalService, supplierMarketplace);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "GUEST");
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                customerAdmin.getKey(), "PICT_TEST_04_1", serviceDetails,
                customerAdmin, role);

        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-03-05 00:00:00"));
        subDetails = subscrSetup.modifyUserRole(subDetails.getUsageLicenses()
                .get(0), VOServiceFactory.getRole(serviceDetails, "USER"),
                subDetails.getSubscriptionId());

        subDetails = subscrSetup.modifyParameterForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-03-06 00:00:00"),
                "PERIOD", DateTimeHandling.daysToMillis(100) + "");

        // Change price model of subscription
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-03-07 06:00:00"));
        VOPriceModel newSubPriceModel = VOPriceModelFactory.modifyPriceModel(
                subDetails.getPriceModel(), PriceModelType.PER_UNIT,
                PricingPeriod.DAY, "EUR", new BigDecimal("0.00"),
                new BigDecimal("7.00"), new BigDecimal("8.00"), 4);
        subscrSetup.savePriceModelForSubscription(supplierAdmin.getKey(),
                subDetails, newSubPriceModel, customer);

        // Suspend and resume the subscription by deleting the customer's
        // payment types
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-03-07 12:00:00"));
        paymentSetup.deleteCustomerPaymentTypes(customer);
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-03-08 18:00:00"));
        paymentSetup.reassignCustomerPaymentTypes(customer);
        subDetails = subscrSetup.getSubscriptionDetails(customerAdmin.getKey(),
                subDetails.getSubscriptionId());

        subscrSetup.recordEventForSubscription(
                basicSetup.getSupplierAdminKey(), subDetails,
                DateTimeHandling.calculateMillis("2013-03-09 12:00:00"),
                "FILE_DOWNLOAD", 15);
        subscrSetup.recordEventForSubscription(
                basicSetup.getSupplierAdminKey(), subDetails,
                DateTimeHandling.calculateMillis("2013-03-09 18:00:00"),
                "FILE_UPLOAD", 3);

        // Upgrade the subscription to a free service
        VOServiceDetails freeService = serviceSetup
                .createPublishAndActivateMarketableService(
                        supplierAdmin.getKey(), "PICT04_1_FREE_SERVICE",
                        TestService.EXAMPLE, TestPriceModel.FREE,
                        technicalService, supplierMarketplace);
        serviceSetup.registerCompatibleServices(supplierAdmin.getKey(),
                serviceDetails, freeService);

        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-03-11 00:00:00"));
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(customerAdmin.getKey(), subDetails,
                        freeService);

        upgradedSubDetails = subscrSetup.modifyParameterForSubscription(
                upgradedSubDetails,
                DateTimeHandling.calculateMillis("2013-03-12 00:00:00"),
                "PERIOD", DateTimeHandling.daysToMillis(110) + "");

        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-03-14 00:00:00"));
        upgradedSubDetails.setSubscriptionId("PICT_TEST_04_1" + "_SubID2");
        upgradedSubDetails = subscrSetup.modifySubscription(upgradedSubDetails,
                null);

        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-03-15 12:00:00"));
        upgradedSubDetails = subscrSetup.revokeUser(customerAdmin,
                upgradedSubDetails.getSubscriptionId());

        // Upgrade the subscription to a pro rata service
        VOServiceDetails proRataService = serviceSetup
                .createPublishAndActivateMarketableService(
                        supplierAdmin.getKey(), "PICT04_1_PRORATA_SERVICE",
                        TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PRORATA_MONTH_ROLES,
                        technicalService, supplierMarketplace);
        serviceSetup.registerCompatibleServices(supplierAdmin.getKey(),
                freeService, proRataService);

        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-03-17 00:00:00"));
        VOSubscriptionDetails upgradedSubDetails2 = subscrSetup
                .upgradeSubscription(customerAdmin.getKey(),
                        upgradedSubDetails, proRataService);

        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-03-18 12:00:00"));
        upgradedSubDetails2 = subscrSetup.addUser(customerAdmin,
                VOServiceFactory.getRole(serviceDetails, "USER"),
                upgradedSubDetails2.getSubscriptionId());

        upgradedSubDetails2 = subscrSetup.modifyParameterForSubscription(
                upgradedSubDetails2,
                DateTimeHandling.calculateMillis("2013-03-24 00:00:00"),
                "PERIOD", DateTimeHandling.daysToMillis(90) + "");

        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-03-28 12:00:00"));
        upgradedSubDetails2 = subscrSetup.modifyUserRole(upgradedSubDetails2
                .getUsageLicenses().get(0), VOServiceFactory.getRole(
                proRataService, "ADMIN"), upgradedSubDetails2
                .getSubscriptionId());

        // reset old cut off day
        resetCutOffDay(supplierAdmin.getKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_04_1", subDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_04_1", upgradedSubDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_04_1", upgradedSubDetails2);
        BillingIntegrationTestBase.updateCustomerListForTests("PICT_TEST_04_1",
                customer);
    }

    /**
     * See testcase #5 in BESBillingFactorCombinations.xlsx
     */
    public void createPictScenario05() throws Exception {
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-01-30 10:00:00"));

        // Create a supplier
        String supplierAdminId = "Pict05Supplier";
        VOOrganization supplier = orgSetup.createOrganization(
                basicSetup.getPlatformOperatorUserKey(), supplierAdminId,
                "Pict05SupplierOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                OrganizationRoleType.TECHNOLOGY_PROVIDER,
                OrganizationRoleType.SUPPLIER);
        VOUser supplierAdmin = orgSetup.getUser(supplierAdminId, true);
        paymentSetup.createPaymentForSupplier(
                basicSetup.getPlatformOperatorUserKey(),
                supplierAdmin.getKey(), supplier);

        setCutOffDay(supplierAdmin.getKey(), 2);

        // Create a customer with a discount
        String customerAdminId = "Pict05DiscountedCustomer";
        VOOrganization customer = orgSetup.registerCustomer(
                "Pict05DiscountedCustomerOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_UK,
                customerAdminId, supplierMarketplace.getMarketplaceId(),
                supplier.getOrganizationId());
        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);
        orgSetup.updateCustomerDiscount(customer, new BigDecimal("10.00"),
                DateTimeHandling.calculateMillis("2013-02-01 00:00:00"),
                DateTimeHandling.calculateMillis("2013-02-16 23:59:59"));

        orgSetup.createMarketingPermission(basicSetup.getSupplierAdminKey(),
                supplier.getOrganizationId(), technicalService);
        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        supplierAdmin.getKey(), "PICT_TEST_05",
                        TestService.EXAMPLE2,
                        TestPriceModel.EXAMPLE_PICT05_UNIT_WEEK,
                        technicalService, supplierMarketplace);

        // subscribe to service w role ADMIN 30.1
        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "PICT_TEST_05", serviceDetails, customerAdmin, role);

        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-02-01 00:00:00"));
        subDetails = subscrSetup.modifyUserRole(subDetails.getUsageLicenses()
                .get(0), VOServiceFactory.getRole(serviceDetails, "USER"),
                subDetails.getSubscriptionId());

        // Suspend and resume the subscription by deleting the customer's
        // payment types; this should change anything because TU price model
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-02-01 02:00:00"));
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER);
        paymentSetup.deleteCustomerPaymentTypes(customer);
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-02-01 03:00:00"));
        paymentSetup.reassignCustomerPaymentTypes(customer);
        subDetails = subscrSetup.getSubscriptionDetails(customerAdmin.getKey(),
                subDetails.getSubscriptionId());

        // user is deassigned in TU price model period; should not change
        // anything in result
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-02-01 04:00:00"));
        subscrSetup.revokeUser(customerAdmin, subDetails.getSubscriptionId());
        // reassign
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-02-01 05:00:00"));
        role = VOServiceFactory.getRole(serviceDetails, "USER");
        subDetails = subscrSetup.addUser(customerAdmin, role,
                subDetails.getSubscriptionId());

        // Upgrade the subscription to pro-rata week
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOServiceDetails proRataService = serviceSetup
                .createPublishAndActivateMarketableService(
                        supplierAdmin.getKey(), "PICT05_RATA_WEEK_SERVICE",
                        TestService.EXAMPLE2,
                        TestPriceModel.EXAMPLE_PICT05_RATA_WEEK,
                        technicalService, supplierMarketplace);
        serviceSetup.registerCompatibleServices(supplierAdmin.getKey(),
                serviceDetails, proRataService);

        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-02-01 10:00:00"));
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(subDetails, proRataService);

        // change subscriptionID
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-02-01 10:10:00"));
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        upgradedSubDetails.setSubscriptionId("PICT_TEST_05" + "_SubID2");
        upgradedSubDetails = subscrSetup.modifySubscription(upgradedSubDetails,
                null);

        // pro rata service2
        VOServiceDetails proRataService2 = serviceSetup
                .createPublishAndActivateMarketableService(
                        supplierAdmin.getKey(), "PICT05_RATA_WEEK2_SERVICE",
                        TestService.EXAMPLE2,
                        TestPriceModel.EXAMPLE_PICT05_RATA_WEEK2,
                        technicalService, supplierMarketplace);
        serviceSetup.registerCompatibleServices(supplierAdmin.getKey(),
                proRataService, proRataService2);

        // upgrade to pro-rata week service 2
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-02-03 00:00:00"));
        VOSubscriptionDetails upgradedSubDetails2 = subscrSetup
                .upgradeSubscription(upgradedSubDetails, proRataService2);

        upgradedSubDetails2 = subscrSetup.modifyParameterForSubscription(
                upgradedSubDetails2,
                DateTimeHandling.calculateMillis("2013-02-03 02:00:00"),
                "HAS_OPTIONS", "1");

        // supplier modifies price model for customer
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER);
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-02-03 05:00:00"));
        VOPriceModel newSubPriceModel = VOPriceModelFactory
                .modifyPriceModelPeriodFee(upgradedSubDetails2.getPriceModel(),
                        new BigDecimal("1000.00"));
        subscrSetup.savePriceModelForSubscription(supplierAdmin.getKey(),
                upgradedSubDetails2, newSubPriceModel, customer);

        // terminate sub
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        long userterminateTime = DateTimeHandling
                .calculateMillis("2013-02-03 10:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(userterminateTime);
        subscrSetup.unsubscribeToService(upgradedSubDetails2
                .getSubscriptionId());

        // reset old cut off day
        resetCutOffDay(supplierAdmin.getKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_05", subDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_05", upgradedSubDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_05", upgradedSubDetails2);
        BillingIntegrationTestBase.updateCustomerListForTests("PICT_TEST_05",
                customer);
    }

    /**
     * simplified version of testcase #5 in BESBillingFactorCombinations.xlsx to
     * document bug#
     */
    public void createPictScenario05_1() throws Exception {
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-01-30 10:00:00"));

        // Create a supplier
        String supplierAdminId = "Pict05_1_Supplier";
        VOOrganization supplier = orgSetup.createOrganization(
                basicSetup.getPlatformOperatorUserKey(), supplierAdminId,
                "Pict05_1_SupplierOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                OrganizationRoleType.TECHNOLOGY_PROVIDER,
                OrganizationRoleType.SUPPLIER);
        VOUser supplierAdmin = orgSetup.getUser(supplierAdminId, true);
        paymentSetup.createPaymentForSupplier(
                basicSetup.getPlatformOperatorUserKey(),
                supplierAdmin.getKey(), supplier);

        setCutOffDay(supplierAdmin.getKey(), 2);

        // Create a customer
        String customerAdminId = "Pict05_1_Customer";
        VOOrganization customer = orgSetup.registerCustomer(
                "Pict05_1_CustomerOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_UK,
                customerAdminId, supplierMarketplace.getMarketplaceId(),
                supplier.getOrganizationId());
        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);

        orgSetup.createMarketingPermission(basicSetup.getSupplierAdminKey(),
                supplier.getOrganizationId(), technicalService);
        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        supplierAdmin.getKey(), "PICT_TEST_05_1",
                        TestService.EXAMPLE2,
                        TestPriceModel.EXAMPLE_PICT05_1_UNIT_WEEK,
                        technicalService, supplierMarketplace);

        // subscribe to service w role ADMIN 30.1
        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "PICT_TEST_05_1", serviceDetails, customerAdmin, role);

        VOServiceDetails proRataService = serviceSetup
                .createPublishAndActivateMarketableService(
                        supplierAdmin.getKey(), "PICT05_1_RATA_WEEK_SERVICE",
                        TestService.EXAMPLE2,
                        TestPriceModel.EXAMPLE_PICT05_1_RATA_WEEK,
                        technicalService, supplierMarketplace);
        serviceSetup.registerCompatibleServices(supplierAdmin.getKey(),
                serviceDetails, proRataService);

        // upgrade to pro-rata service
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-02-01 10:00:00"));
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(subDetails, proRataService);

        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-02-01 11:00:00"));
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        upgradedSubDetails.setSubscriptionId("PICT_TEST_05_1" + "_SubID2");
        upgradedSubDetails = subscrSetup.modifySubscription(upgradedSubDetails,
                null);

        // terminate sub
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        long userterminateTime = DateTimeHandling
                .calculateMillis("2013-02-03 10:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(userterminateTime);
        subscrSetup
                .unsubscribeToService(upgradedSubDetails.getSubscriptionId());

        // reset old cut off day
        resetCutOffDay(supplierAdmin.getKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_05_1", subDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_05_1", upgradedSubDetails);
        BillingIntegrationTestBase.updateCustomerListForTests("PICT_TEST_05_1",
                customer);
    }

    /**
     * See testcase #6 in BESBillingFactorCombinations.xlsx
     */
    public void createPictScenario06() throws Exception {
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-010-23 10:00:00"));

        // Create a supplier
        String supplierAdminId = "Pict06Supplier";
        VOOrganization supplier = orgSetup.createOrganization(
                basicSetup.getPlatformOperatorUserKey(), supplierAdminId,
                "Pict06SupplierOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                OrganizationRoleType.TECHNOLOGY_PROVIDER,
                OrganizationRoleType.SUPPLIER);
        VOUser supplierAdmin = orgSetup.getUser(supplierAdminId, true);
        paymentSetup.createPaymentForSupplier(
                basicSetup.getPlatformOperatorUserKey(),
                supplierAdmin.getKey(), supplier);

        setCutOffDay(supplierAdmin.getKey(), 1);

        // country specific VAT rates
        orgSetup.saveAllVats(supplierAdmin.getKey(), VOVatRateFactory
                .newVOVatRate(new BigDecimal("0.00")), Arrays.asList(
                VOVatRateFactory.newVOCountryVatRate(new BigDecimal("20.00"),
                        TestOrganizationSetup.ORGANIZATION_DOMICILE_UK),
                VOVatRateFactory.newVOCountryVatRate(new BigDecimal("10.00"),
                        TestOrganizationSetup.ORGANIZATION_DOMICILE_DE)), null);

        // Create a customer
        String customerAdminId = "Pict06Customer";
        VOOrganization customer = orgSetup.registerCustomer(
                "Pict06CustomerOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_UK,
                customerAdminId, supplierMarketplace.getMarketplaceId(),
                supplier.getOrganizationId());
        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);

        orgSetup.createMarketingPermission(basicSetup.getSupplierAdminKey(),
                supplier.getOrganizationId(), technicalService);

        // setup time unit hour service with customer price model
        // start with free and make customer specific price model
        // with costs
        VOServiceDetails serviceUnitTempl = serviceSetup
                .createAndPublishMarketableService(supplierAdmin.getKey(),
                        "PICT_TEST_06", TestService.EXAMPLE2,
                        TestPriceModel.FREE, technicalService,
                        supplierMarketplace);
        VOServiceDetails serviceDetails = serviceSetup
                .savePriceModelForCustomer(serviceUnitTempl,
                        TestPriceModel.EXAMPLE_PICT06_UNIT_HOUR, customer);
        serviceDetails = serviceSetup.activateMarketableService(serviceDetails);

        // create upgraded pro rata service with customer price model
        VOServiceDetails serviceProRataTempl = serviceSetup
                .createAndPublishMarketableService(supplierAdmin.getKey(),
                        "PICT_TEST_06_Upgrade", TestService.EXAMPLE2,
                        TestPriceModel.FREE, technicalService,
                        supplierMarketplace);
        VOServiceDetails serviceProRata = serviceSetup
                .savePriceModelForCustomer(serviceProRataTempl,
                        TestPriceModel.EXAMPLE_PICT06_RATA_HOUR, customer);
        serviceProRata = serviceSetup.activateMarketableService(serviceProRata);

        // define upgrade path
        serviceSetup.registerCompatibleServices(supplierAdmin.getKey(),
                serviceUnitTempl, serviceProRataTempl);

        // subscribe to time unit hour service w role ADMIN 30.1
        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "PICT_TEST_06", serviceDetails, customerAdmin, role);

        // Suspend and resume the subscription by deleting the customer's
        // payment types; this should change anything because TU price model
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-10-26 08:00:00"));
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER);
        paymentSetup.deleteCustomerPaymentTypes(customer);
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-10-26 09:00:00"));
        paymentSetup.reassignCustomerPaymentTypes(customer);
        subDetails = subscrSetup.getSubscriptionDetails(customerAdmin.getKey(),
                subDetails.getSubscriptionId());

        // upgrade to pro-rata hour service
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-10-26 10:00:00"));
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(subDetails, serviceProRata);

        // supplier modifies price model for customer
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER);
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-10-26 11:00:00"));
        VOPriceModel newSubPriceModel = VOPriceModelFactory
                .modifyPriceModelPeriodFee(upgradedSubDetails.getPriceModel(),
                        new BigDecimal("1000.00"));
        subscrSetup.savePriceModelForSubscription(supplierAdmin.getKey(),
                upgradedSubDetails, newSubPriceModel, customer);

        // de-assign user
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-10-26 22:00:00"));
        subscrSetup.revokeUser(customerAdmin,
                upgradedSubDetails.getSubscriptionId());

        // delete pro rata marketable service - should make no difference to
        // billing
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-10-27 02:30:00"));
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER);
        serviceSetup.deleteMarketableService(serviceProRata);

        // event during time change
        container.login(basicSetup.getSupplierAdminKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        subscrSetup.recordEventForSubscription(upgradedSubDetails,
                DateTimeHandling.calculateMillis("2013-10-27 02:40:00"),
                "FILE_DOWNLOAD", 1);

        // reassign
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-10-27 22:00:00"));
        role = VOServiceFactory.getRole(serviceDetails, "ADMIN");
        upgradedSubDetails = subscrSetup.addUser(customerAdmin, role,
                upgradedSubDetails.getSubscriptionId());

        // terminate sub
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        long userterminateTime = DateTimeHandling
                .calculateMillis("2013-10-29 10:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(userterminateTime);
        subscrSetup
                .unsubscribeToService(upgradedSubDetails.getSubscriptionId());

        // reset old cut off day
        resetCutOffDay(supplierAdmin.getKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_06", subDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_06", upgradedSubDetails);
        BillingIntegrationTestBase.updateCustomerListForTests("PICT_TEST_06",
                customer);
    }

    /**
     * See testcase #7 in BESBillingFactorCombinations.xlsx
     */
    public void createPictScenario07() throws Exception {
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-07-01 12:00:00"));

        // Create a supplier
        String supplierAdminId = "Pict07Supplier";
        VOOrganization supplier = orgSetup.createOrganization(
                basicSetup.getPlatformOperatorUserKey(), supplierAdminId,
                "Pict07SupplierOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                OrganizationRoleType.TECHNOLOGY_PROVIDER,
                OrganizationRoleType.SUPPLIER);
        VOUser supplierAdmin = orgSetup.getUser(supplierAdminId, true);
        paymentSetup.createPaymentForSupplier(
                basicSetup.getPlatformOperatorUserKey(),
                supplierAdmin.getKey(), supplier);

        setCutOffDay(supplierAdmin.getKey(), 15);

        // Create a customer
        String customerAdminId = "Pict07Customer";
        VOOrganization customer = orgSetup.registerCustomer(
                "Pict07CustomerOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_UK,
                customerAdminId, supplierMarketplace.getMarketplaceId(),
                supplier.getOrganizationId());
        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);

        orgSetup.createMarketingPermission(basicSetup.getSupplierAdminKey(),
                supplier.getOrganizationId(), technicalService);

        orgSetup.saveAllVats(supplierAdmin.getKey(), VOVatRateFactory
                .newVOVatRate(new BigDecimal("10.00")), Arrays.asList(
                VOVatRateFactory.newVOCountryVatRate(new BigDecimal("20.00"),
                        TestOrganizationSetup.ORGANIZATION_DOMICILE_UK),
                VOVatRateFactory.newVOCountryVatRate(new BigDecimal("30.00"),
                        TestOrganizationSetup.ORGANIZATION_DOMICILE_DE)),
                Arrays.asList(VOVatRateFactory.newVOOrganizationVatRate(
                        new BigDecimal("50.00"), customer)));

        // setup time pro rata month service with customer price model
        // start with free and make customer specific price model
        // with costs
        VOServiceDetails serviceUnitTempl = serviceSetup
                .createAndPublishMarketableService(supplierAdmin.getKey(),
                        "PICT_TEST_07", TestService.EXAMPLE2,
                        TestPriceModel.FREE, technicalService,
                        supplierMarketplace);
        VOServiceDetails serviceDetails = serviceSetup
                .savePriceModelForCustomer(serviceUnitTempl,
                        TestPriceModel.EXAMPLE_PICT07_RATA_MONTH, customer);
        serviceDetails = serviceSetup.activateMarketableService(serviceDetails);

        // create upgraded free service with customer price model
        VOServiceDetails serviceFreeTempl = serviceSetup
                .createAndPublishMarketableService(supplierAdmin.getKey(),
                        "PICT_TEST_07_Upgrade", TestService.EXAMPLE2,
                        TestPriceModel.EXAMPLE_PICT07_RATA_MONTH,
                        technicalService, supplierMarketplace);
        VOServiceDetails serviceFreeDetails = serviceSetup
                .savePriceModelForCustomer(serviceFreeTempl,
                        TestPriceModel.FREE, customer);
        serviceFreeDetails = serviceSetup
                .activateMarketableService(serviceFreeDetails);

        // define upgrade path
        serviceSetup.registerCompatibleServices(supplierAdmin.getKey(),
                serviceUnitTempl, serviceFreeTempl);

        // subscribe to pro rata month service w role ADMIN 30.1
        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "PICT_TEST_07", serviceDetails, customerAdmin, role);

        // events - with free service (no change in billing result)
        container.login(basicSetup.getSupplierAdminKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        subscrSetup.recordEventForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-08-02 05:00:00"),
                "FILE_UPLOAD", 10);

        // role change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        long roleChangeTime = DateTimeHandling
                .calculateMillis("2013-08-02 06:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(roleChangeTime);
        subDetails = subscrSetup.modifyUserRole(subDetails.getUsageLicenses()
                .get(0), VOServiceFactory.getRole(serviceDetails, "USER"),
                subDetails.getSubscriptionId());

        // de-assign user (and no re-assign)
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-08-02 12:00:00"));
        subDetails = subscrSetup.revokeUser(customerAdmin,
                subDetails.getSubscriptionId());

        // Suspend and resume the subscription by deleting the customer's
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-08-03 12:00:00"));
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER);
        paymentSetup.deleteCustomerPaymentTypes(customer);
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-08-06 12:00:00"));
        paymentSetup.reassignCustomerPaymentTypes(customer);
        subDetails = subscrSetup.getSubscriptionDetails(customerAdmin.getKey(),
                subDetails.getSubscriptionId());

        // parameter change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        subDetails = subscrSetup.modifyParameterForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-08-08 12:00:00"),
                "LONG_NUMBER", "500");

        // upgrade to free service
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-08-15 00:00:00"));
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(subDetails, serviceFreeDetails);

        // events - with free service (no change in billing result)
        container.login(basicSetup.getSupplierAdminKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        subscrSetup.recordEventForSubscription(upgradedSubDetails,
                DateTimeHandling.calculateMillis("2013-08-15 12:10:00"),
                "FILE_UPLOAD", 10);

        // delete pro rata marketable service - should make no difference to
        // billing
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-08-16 12:30:00"));
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER);
        serviceSetup.deleteMarketableService(serviceDetails);

        // terminate sub
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        long userterminateTime = DateTimeHandling
                .calculateMillis("2013-08-20 12:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(userterminateTime);
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        // reset old cut off day
        resetCutOffDay(supplierAdmin.getKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_07", subDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_07", upgradedSubDetails);
        BillingIntegrationTestBase.updateCustomerListForTests("PICT_TEST_07",
                customer);
    }

    /**
     * See testcase #8 in BESBillingFactorCombinations.xlsx
     */
    public void createPictScenario08() throws Exception {
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-06-30 10:00:00"));

        // Create a supplier
        String supplierAdminId = "Pict08Supplier";
        VOOrganization supplier = orgSetup.createOrganization(
                basicSetup.getPlatformOperatorUserKey(), supplierAdminId,
                "Pict08SupplierOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                OrganizationRoleType.TECHNOLOGY_PROVIDER,
                OrganizationRoleType.SUPPLIER);
        VOUser supplierAdmin = orgSetup.getUser(supplierAdminId, true);
        paymentSetup.createPaymentForSupplier(
                basicSetup.getPlatformOperatorUserKey(),
                supplierAdmin.getKey(), supplier);

        setCutOffDay(supplierAdmin.getKey(), 2);

        // Create a customer with a discount
        String customerAdminId = "Pict08DiscountedCustomer";
        VOOrganization customer = orgSetup.registerCustomer(
                "Pict08DiscountedCustomerOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_UK,
                customerAdminId, supplierMarketplace.getMarketplaceId(),
                supplier.getOrganizationId());
        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);
        orgSetup.updateCustomerDiscount(customer, new BigDecimal("10.00"),
                DateTimeHandling.calculateMillis("2013-06-30 00:00:00"),
                DateTimeHandling.calculateMillis("2013-08-30 00:00:00"));

        // free service
        orgSetup.createMarketingPermission(basicSetup.getSupplierAdminKey(),
                supplier.getOrganizationId(), technicalService);
        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        supplierAdmin.getKey(), "PICT_TEST_08",
                        TestService.EXAMPLE2, TestPriceModel.FREE,
                        technicalService, supplierMarketplace);

        // subscribe to service w role ADMIN 30.1
        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "PICT_TEST_08", serviceDetails, customerAdmin, role);

        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-07-10 00:00:00"));
        subDetails = subscrSetup.modifyUserRole(subDetails.getUsageLicenses()
                .get(0), VOServiceFactory.getRole(serviceDetails, "USER"),
                subDetails.getSubscriptionId());

        // Suspend and resume the subscription by deleting the customer's
        // payment types; this should change anything because free price model
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-07-11 02:00:00"));
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER);
        paymentSetup.deleteCustomerPaymentTypes(customer);
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-07-12 02:00:00"));
        paymentSetup.reassignCustomerPaymentTypes(customer);
        subDetails = subscrSetup.getSubscriptionDetails(customerAdmin.getKey(),
                subDetails.getSubscriptionId());

        // user is deassigned in free price model period
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-07-13 00:00:00"));
        subDetails = subscrSetup.revokeUser(customerAdmin,
                subDetails.getSubscriptionId());

        // define upgrade service pro-rata week
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOServiceDetails proRataService = serviceSetup
                .createPublishAndActivateMarketableService(
                        supplierAdmin.getKey(), "PICT08_RATA_WEEK_SERVICE",
                        TestService.EXAMPLE2,
                        TestPriceModel.EXAMPLE_PICT08_RATA_WEEK,
                        technicalService, supplierMarketplace);
        serviceSetup.registerCompatibleServices(supplierAdmin.getKey(),
                serviceDetails, proRataService);

        // Upgrade the subscription from free to pro-rata week
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-07-14 00:00:00"));
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(subDetails, proRataService);

        // reset old cut off day
        resetCutOffDay(supplierAdmin.getKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_08", subDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_08", upgradedSubDetails);
        BillingIntegrationTestBase.updateCustomerListForTests("PICT_TEST_08",
                customer);
    }

    /**
     * See testcase #9 of BESBillingFactorCombinations.xlsx
     */
    public void createPictScenario09() throws Exception {
        long usageStartTime = DateTimeHandling
                .calculateMillis("2013-03-01 00:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(usageStartTime);

        // Create a supplier with a marketplace
        String supplierAdminId = "Pict09Supplier";
        VOOrganization supplier = orgSetup.createOrganization(
                basicSetup.getPlatformOperatorUserKey(), supplierAdminId,
                "Pict09SupplierOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_DE,
                OrganizationRoleType.TECHNOLOGY_PROVIDER,
                OrganizationRoleType.SUPPLIER);
        VOUser supplierAdmin = orgSetup.getUser(supplierAdminId, true);
        VOMarketplace supplMarketplace = orgSetup.createMarketplace(
                "Pict09Supplier_MP", false, supplier);
        paymentSetup.createPaymentForSupplier(
                basicSetup.getPlatformOperatorUserKey(),
                supplierAdmin.getKey(), supplier);

        // Create own technical service
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        serviceSetup
                .importTechnicalServices(BaseAdmUmTest.TECHNICAL_SERVICE_EXAMPLE2_XML);
        VOTechnicalService example2TechService = serviceSetup
                .getTechnicalService(VOTechServiceFactory.TECH_SERVICE_EXAMPLE2_ID);

        setCutOffDay(supplierAdmin.getKey(), 1);

        // Create own customer
        String customerAdminId = "PIC09Customer";
        VOOrganization customer = orgSetup.registerCustomer("PIC09CustomerOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_UK,
                customerAdminId, supplMarketplace.getMarketplaceId(),
                supplier.getOrganizationId());

        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);
        orgSetup.createMarketingPermission(supplierAdmin.getKey(),
                supplier.getOrganizationId(), example2TechService);

        // setup up time unit hour service
        VOServiceDetails unitServTemplate = serviceSetup
                .createAndPublishMarketableService(supplierAdmin.getKey(),
                        "PICT09_PERUNIT_SERVICE", TestService.EXAMPLE2,
                        TestPriceModel.EXAMPLE_PICT09_UNIT_HOUR,
                        example2TechService, supplierMarketplace);

        VOServiceDetails freeTemplate = serviceSetup
                .createAndPublishMarketableService(supplierAdmin.getKey(),
                        "PICT09_FREE", TestService.EXAMPLE2,
                        TestPriceModel.FREE, example2TechService,
                        supplierMarketplace);
        unitServTemplate = serviceSetup.registerCompatibleServices(
                supplierAdmin.getKey(), unitServTemplate, freeTemplate);
        freeTemplate = serviceSetup.registerCompatibleServices(
                supplierAdmin.getKey(), freeTemplate, unitServTemplate);

        // activate services
        VOServiceDetails serviceDetails = serviceSetup
                .activateMarketableService(unitServTemplate);
        VOServiceDetails serviceFreeDetails = serviceSetup
                .activateMarketableService(freeTemplate);

        // subscribe to time unit hour service as customer
        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "PICT_TEST_09", serviceDetails, customerAdmin, role);

        // change subscriptionID
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-03-10 14:00:00"));
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        subDetails.setSubscriptionId("PICT_TEST_09" + "_SubID2");
        subDetails = subscrSetup.modifySubscription(subDetails, null);

        // suspend/resume subscription by removing/restoring the customer's
        // payment types
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-03-22 14:10:00"));
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        paymentSetup.deleteCustomerPaymentTypes(customer);
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-03-22 14:20:00"));
        paymentSetup.reassignCustomerPaymentTypes(customer);
        subDetails = subscrSetup.getSubscriptionDetails(customerAdmin.getKey(),
                subDetails.getSubscriptionId());

        // user is de-assigned in TU price model period within charged period
        // hour should not change anything in result
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-03-22 14:30:00"));
        subscrSetup.revokeUser(customerAdmin, subDetails.getSubscriptionId());
        // reassign
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-03-22 14:40:00"));
        role = VOServiceFactory.getRole(serviceDetails, "ADMIN");
        subDetails = subscrSetup.addUser(customerAdmin, role,
                subDetails.getSubscriptionId());

        // events during per unit hour charged time
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        subscrSetup.recordEventForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-03-22 14:41:00"),
                "FILE_DOWNLOAD", 100);
        subscrSetup.recordEventForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-03-22 14:42:00"),
                "FILE_UPLOAD", 100);

        // upgrade to free after dst change
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-03-31 11:59:59"));
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(subDetails, serviceFreeDetails);

        // and back to original per unit hour service
        // free period is defined (again)
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-03-31 15:00:00"));
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails upgradedSubDetails2 = subscrSetup
                .upgradeSubscription(upgradedSubDetails, serviceDetails);

        // events during free period
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        subscrSetup.recordEventForSubscription(upgradedSubDetails2,
                DateTimeHandling.calculateMillis("2013-03-31 15:05:00"),
                "FILE_DOWNLOAD", 100);
        subscrSetup.recordEventForSubscription(upgradedSubDetails2,
                DateTimeHandling.calculateMillis("2013-03-31 15:06:00"),
                "FILE_UPLOAD", 100);

        // Delete marketable service
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-03-31 16:00:00"));
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER);
        serviceSetup.deleteMarketableService(serviceDetails);

        // terminate sub (within free period) in next billing period
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-04-10 10:00:00"));
        subscrSetup.unsubscribeToService(upgradedSubDetails2
                .getSubscriptionId());

        resetCutOffDay(supplierAdmin.getKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_09", subDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_09", upgradedSubDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_09", upgradedSubDetails2);
        BillingIntegrationTestBase.updateCustomerListForTests("PICT_TEST_09",
                customer);
    }

    /**
     * See testcase #10 in BESBillingFactorCombinations.xlsx
     */
    public void createPictScenario10() throws Exception {
        // below is NOT time of subscription start
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-06-20 10:00:00"));

        // Create a supplier
        String supplierAdminId = "Pict10Supplier";
        VOOrganization supplier = orgSetup.createOrganization(
                basicSetup.getPlatformOperatorUserKey(), supplierAdminId,
                "Pict10SupplierOrg",
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
        VOTechnicalService example2TechService = serviceSetup
                .getTechnicalService(VOTechServiceFactory.TECH_SERVICE_EXAMPLE2_ID);

        setCutOffDay(supplierAdmin.getKey(), 1);

        // setup customer with discount
        String customerAdminId = "Pict10DiscountedCustomer";
        VOOrganization customer = orgSetup.registerCustomer(
                "Pict10DiscountedCustomerOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_UK,
                customerAdminId, supplierMarketplace.getMarketplaceId(),
                supplier.getOrganizationId());
        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);
        orgSetup.updateCustomerDiscount(customer, new BigDecimal("10.00"),
                DateTimeHandling.calculateMillis("2013-06-30 00:00:00"),
                DateTimeHandling.calculateMillis("2013-08-30 00:00:00"));

        orgSetup.saveAllVats(supplierAdmin.getKey(), VOVatRateFactory
                .newVOVatRate(new BigDecimal("10.00")), Arrays.asList(
                VOVatRateFactory.newVOCountryVatRate(new BigDecimal("20.00"),
                        TestOrganizationSetup.ORGANIZATION_DOMICILE_UK),
                VOVatRateFactory.newVOCountryVatRate(new BigDecimal("30.00"),
                        TestOrganizationSetup.ORGANIZATION_DOMICILE_DE)),
                Arrays.asList(VOVatRateFactory.newVOOrganizationVatRate(
                        new BigDecimal("50.00"), customer)));

        orgSetup.createMarketingPermission(supplierAdmin.getKey(),
                supplier.getOrganizationId(), example2TechService);

        // setup free service
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-06-22 10:00:00"));
        VOServiceDetails freeTemplate = serviceSetup
                .createAndPublishMarketableService(supplierAdmin.getKey(),
                        "PICT10_FREE", TestService.EXAMPLE2,
                        TestPriceModel.FREE, example2TechService,
                        supplierMarketplace);

        // setup time unit month service
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-06-23 10:00:00"));
        VOServiceDetails unitServTemplate = serviceSetup
                .createAndPublishMarketableService(supplierAdmin.getKey(),
                        "PICT10_UNIT_MONTH_SERVICE", TestService.EXAMPLE2,
                        TestPriceModel.EXAMPLE_PICT10_UNIT_MONTH,
                        example2TechService, supplierMarketplace);

        // setup time unit month service #2
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-06-24 10:00:00"));
        VOServiceDetails unitServTemplate2 = serviceSetup
                .createAndPublishMarketableService(supplierAdmin.getKey(),
                        "PICT10_2_UNIT_MONTH_SERVICE", TestService.EXAMPLE2,
                        TestPriceModel.EXAMPLE_PICT10_2_UNIT_MONTH,
                        example2TechService, supplierMarketplace);

        // define upgrade path
        freeTemplate = serviceSetup.registerCompatibleServices(
                supplierAdmin.getKey(), freeTemplate, unitServTemplate);
        unitServTemplate = serviceSetup.registerCompatibleServices(
                supplierAdmin.getKey(), unitServTemplate, unitServTemplate2);

        // activate services
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-06-25 10:00:00"));
        VOServiceDetails serviceFreeDetails = serviceSetup
                .activateMarketableService(freeTemplate);
        VOServiceDetails serviceUnitDetails = serviceSetup
                .activateMarketableService(unitServTemplate);
        VOServiceDetails serviceUnitDetails2 = serviceSetup
                .activateMarketableService(unitServTemplate2);

        // subscribe to service w role ADMIN
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-06-30 10:00:00"));
        VORoleDefinition role = VOServiceFactory.getRole(serviceFreeDetails,
                "ADMIN");
        serviceFreeDetails = serviceSetup.getServiceDetails(
                supplierAdmin.getKey(), serviceFreeDetails);
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "PICT_TEST_10", serviceFreeDetails, customerAdmin, role);

        // user is deassigned and reassigned in free price model period
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-07-05 12:00:00"));
        subDetails = subscrSetup.revokeUser(customerAdmin,
                subDetails.getSubscriptionId());
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-07-10 12:00:00"));
        role = VOServiceFactory.getRole(serviceFreeDetails, "ADMIN");
        subDetails = subscrSetup.addUser(customerAdmin, role,
                subDetails.getSubscriptionId());

        // Upgrade the subscription from free to time-unit month
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-07-14 00:00:00"));
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(subDetails, serviceUnitDetails);

        // parameter change - will be valid at start of next unit
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        upgradedSubDetails = subscrSetup.modifyParameterForSubscription(
                upgradedSubDetails,
                DateTimeHandling.calculateMillis("2013-07-14 10:00:00"),
                "BOOLEAN_PARAMETER", "true");

        // Upgrade the subscription to time-unit month #2
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-08-14 00:00:00"));
        VOSubscriptionDetails upgradedSubDetails2 = subscrSetup
                .upgradeSubscription(upgradedSubDetails, serviceUnitDetails2);

        // terminate sub
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        long userterminateTime = DateTimeHandling
                .calculateMillis("2013-08-20 12:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(userterminateTime);
        subscrSetup.unsubscribeToService(upgradedSubDetails2
                .getSubscriptionId());

        // reset old cut off day
        resetCutOffDay(supplierAdmin.getKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_10", subDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_10", upgradedSubDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_10", upgradedSubDetails2);
        BillingIntegrationTestBase.updateCustomerListForTests("PICT_TEST_10",
                customer);
    }

    /**
     * See testcase #11 in BESBillingFactorCombinations.xlsx
     */
    public void createPictScenario11() throws Exception {

        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-08-01 00:00:00"));

        // Create a supplier
        String supplierAdminId = "Pict11Supplier";
        VOOrganization supplier = orgSetup.createOrganization(
                basicSetup.getPlatformOperatorUserKey(), supplierAdminId,
                "Pict11SupplierOrg",
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
        VOTechnicalService example2TechService = serviceSetup
                .getTechnicalService(VOTechServiceFactory.TECH_SERVICE_EXAMPLE2_ID);

        setCutOffDay(supplierAdmin.getKey(), 1);

        // setup customer with discount
        String customerAdminId = "Pict11DiscountedCustomer";
        VOOrganization customer = orgSetup.registerCustomer(
                "Pict11DiscountedCustomerOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_UK,
                customerAdminId, supplierMarketplace.getMarketplaceId(),
                supplier.getOrganizationId());
        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);
        orgSetup.updateCustomerDiscount(customer, new BigDecimal("10.00"),
                DateTimeHandling.calculateMillis("2013-08-02 00:00:00"),
                DateTimeHandling.calculateMillis("2013-08-12 00:00:00"));

        orgSetup.saveAllVats(supplierAdmin.getKey(), VOVatRateFactory
                .newVOVatRate(new BigDecimal("13.00")), Arrays.asList(
                VOVatRateFactory.newVOCountryVatRate(new BigDecimal("20.00"),
                        TestOrganizationSetup.ORGANIZATION_DOMICILE_UK),
                VOVatRateFactory.newVOCountryVatRate(new BigDecimal("19.00"),
                        TestOrganizationSetup.ORGANIZATION_DOMICILE_DE)), null);

        orgSetup.createMarketingPermission(supplierAdmin.getKey(),
                supplier.getOrganizationId(), example2TechService);

        // setup time unit week service
        VOServiceDetails unitServTemplate = serviceSetup
                .createAndPublishMarketableService(supplierAdmin.getKey(),
                        "PICT11_UNIT_WEEK_SERVICE", TestService.EXAMPLE2,
                        TestPriceModel.FREE, example2TechService,
                        supplierMarketplace);
        VOServiceDetails serviceUnitDetails = serviceSetup
                .savePriceModelForCustomer(unitServTemplate,
                        TestPriceModel.EXAMPLE_PICT11_UNIT_WEEK, customer);

        // setup free service (only customer price Model)
        VOServiceDetails freeTemplate = serviceSetup
                .createAndPublishMarketableService(supplierAdmin.getKey(),
                        "PICT11_FREE", TestService.EXAMPLE2,
                        TestPriceModel.EXAMPLE_PICT11_UNIT_WEEK,
                        example2TechService, supplierMarketplace);
        VOServiceDetails serviceFreeDetails = serviceSetup
                .savePriceModelForCustomer(freeTemplate, TestPriceModel.FREE,
                        customer);

        // setup time unit week service #2 (only customer price model)
        VOServiceDetails unitServTemplate2 = serviceSetup
                .createAndPublishMarketableService(supplierAdmin.getKey(),
                        "PICT11_2_UNIT_MONTH_SERVICE", TestService.EXAMPLE2,
                        TestPriceModel.FREE, example2TechService,
                        supplierMarketplace);
        VOServiceDetails serviceUnitDetails2 = serviceSetup
                .savePriceModelForCustomer(unitServTemplate2,
                        TestPriceModel.EXAMPLE_PICT11_2_UNIT_WEEK, customer);

        // define upgrade path
        unitServTemplate = serviceSetup.registerCompatibleServices(
                supplierAdmin.getKey(), unitServTemplate, freeTemplate);
        freeTemplate = serviceSetup.registerCompatibleServices(
                supplierAdmin.getKey(), freeTemplate, unitServTemplate2);

        // activate services
        serviceUnitDetails = serviceSetup
                .activateMarketableService(serviceUnitDetails);
        serviceFreeDetails = serviceSetup
                .activateMarketableService(serviceFreeDetails);
        serviceUnitDetails2 = serviceSetup
                .activateMarketableService(serviceUnitDetails2);

        // subscribe to service w role USER
        VORoleDefinition role = VOServiceFactory.getRole(serviceUnitDetails,
                "USER");
        serviceUnitDetails = serviceSetup.getServiceDetails(
                supplierAdmin.getKey(), serviceUnitDetails);
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "PICT_TEST_11", serviceUnitDetails, customerAdmin, role);

        // parameter changed to true
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        subDetails = subscrSetup.modifyParameterForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-08-01 01:00:00"),
                "BOOLEAN_PARAMETER", "true");

        // role change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        long roleChangeTime = DateTimeHandling
                .calculateMillis("2013-08-01 02:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(roleChangeTime);
        subDetails = subscrSetup.modifyUserRole(subDetails.getUsageLicenses()
                .get(0), VOServiceFactory.getRole(serviceUnitDetails, "ADMIN"),
                subDetails.getSubscriptionId());

        // change subscriptionID
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-08-01 01:00:00"));
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        subDetails.setSubscriptionId("PICT_TEST_11" + "_SubID2");
        subDetails = subscrSetup.modifySubscription(subDetails, null);

        // Suspend and resume the subscription by deleting the customer's
        // payment types; this should change anything because free period
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-08-01 02:00:00"));
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER);
        paymentSetup.deleteCustomerPaymentTypes(customer);
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-08-01 03:00:00"));
        paymentSetup.reassignCustomerPaymentTypes(customer);
        subDetails = subscrSetup.getSubscriptionDetails(customerAdmin.getKey(),
                subDetails.getSubscriptionId());
        // reactivate subscription
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER);
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-08-12 12:00:00"));
        paymentSetup.reassignCustomerPaymentTypes(customer);
        subDetails = subscrSetup.getSubscriptionDetails(customerAdmin.getKey(),
                subDetails.getSubscriptionId());

        // events after free period
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        subscrSetup.recordEventForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-08-12 12:30:00"),
                "FILE_UPLOAD", 10);

        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        subDetails = subscrSetup.modifyParameterForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-08-13 00:00:00"),
                "BOOLEAN_PARAMETER", "false");

        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        subDetails = subscrSetup.modifyParameterForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-08-14 00:00:00"),
                "BOOLEAN_PARAMETER", "true");

        // Change price model of subscription
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER);
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-08-14 01:00:00"));
        VOPriceModel newSubPriceModel = VOPriceModelFactory
                .modifyPriceModelPeriodFee(subDetails.getPriceModel(),
                        new BigDecimal("4.00"));
        subscrSetup.savePriceModelForSubscription(supplierAdmin.getKey(),
                subDetails, newSubPriceModel, customer);

        // role change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-08-14 02:00:00"));
        subDetails = subscrSetup.modifyUserRole(subDetails.getUsageLicenses()
                .get(0), VOServiceFactory.getRole(serviceUnitDetails, "USER"),
                subDetails.getSubscriptionId());

        // Upgrade the subscription from time-unit week to free
        serviceFreeDetails = serviceSetup.getServiceDetails(
                supplierAdmin.getKey(), serviceFreeDetails);
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-08-15 00:00:00"));
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(subDetails, serviceFreeDetails);

        // Upgrade the subscription to time-unit month #2
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-08-20 00:00:00"));
        VOSubscriptionDetails upgradedSubDetails2 = subscrSetup
                .upgradeSubscription(upgradedSubDetails, serviceUnitDetails2);

        // terminate sub
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        long userterminateTime = DateTimeHandling
                .calculateMillis("2013-08-22 00:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(userterminateTime);
        subscrSetup.unsubscribeToService(upgradedSubDetails2
                .getSubscriptionId());

        // reset old cut off day
        resetCutOffDay(supplierAdmin.getKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_11", subDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_11", upgradedSubDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_11", upgradedSubDetails2);
        BillingIntegrationTestBase.updateCustomerListForTests("PICT_TEST_11",
                customer);
    }

    /**
     * See testcase #12 in BESBillingFactorCombinations.xlsx
     */
    public void createPictScenario12() throws Exception {

        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-08-06 00:00:00"));

        // Create a supplier
        String supplierAdminId = "Pict12Supplier";
        VOOrganization supplier = orgSetup.createOrganization(
                basicSetup.getPlatformOperatorUserKey(), supplierAdminId,
                "Pict12SupplierOrg",
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
        VOTechnicalService example2TechService = serviceSetup
                .getTechnicalService(VOTechServiceFactory.TECH_SERVICE_EXAMPLE2_ID);

        setCutOffDay(supplierAdmin.getKey(), 5);

        // setup customer with discount for first period only
        String customerAdminId = "Pict12DiscountedCustomer";
        VOOrganization customer = orgSetup.registerCustomer(
                "Pict12DiscountedCustomerOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_UK,
                customerAdminId, supplierMarketplace.getMarketplaceId(),
                supplier.getOrganizationId());
        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);
        orgSetup.updateCustomerDiscount(customer, new BigDecimal("10.00"),
                DateTimeHandling.calculateMillis("2013-08-02 00:00:00"),
                DateTimeHandling.calculateMillis("2013-09-04 23:59:59"));

        // default vat
        orgSetup.saveAllVats(supplierAdmin.getKey(),
                VOVatRateFactory.newVOVatRate(new BigDecimal("10.00")), null,
                null);

        orgSetup.createMarketingPermission(supplierAdmin.getKey(),
                supplier.getOrganizationId(), example2TechService);

        // setup pro-rata month service
        VOServiceDetails rataServiceTemplate = serviceSetup
                .createAndPublishMarketableService(supplierAdmin.getKey(),
                        "PICT12_RATA_MONTH_SERVICE", TestService.EXAMPLE2,
                        TestPriceModel.EXAMPLE_PICT12_RATA_MONTH,
                        example2TechService, supplierMarketplace);

        // activate services
        VOServiceDetails serviceRataDetails = serviceSetup
                .activateMarketableService(rataServiceTemplate);

        // subscribe to service w role ADMIN
        VORoleDefinition role = VOServiceFactory.getRole(serviceRataDetails,
                "ADMIN");
        serviceRataDetails = serviceSetup.getServiceDetails(
                supplierAdmin.getKey(), serviceRataDetails);
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "PICT_TEST_12", serviceRataDetails, customerAdmin, role);

        // 2nd user is assigned
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-08-07 00:00:00"));
        role = VOServiceFactory.getRole(serviceRataDetails, "USER");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        subDetails = subscrSetup.addUser(basicSetup.getSecondCustomerUser1(),
                role, subDetails.getSubscriptionId());

        // 2nd user is deassigned
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-08-08 00:00:00"));
        subDetails = subscrSetup.revokeUser(
                basicSetup.getSecondCustomerUser1(),
                subDetails.getSubscriptionId());

        // parameter changed to true
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        subDetails = subscrSetup.modifyParameterForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-08-10 00:00:00"),
                "BOOLEAN_PARAMETER", "true");

        // Delete marketable service
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-08-15 13:00:01"));
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER);
        serviceSetup.deleteMarketableService(serviceRataDetails);

        // parameter changed to false in next charged period (month)
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        subDetails = subscrSetup.modifyParameterForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-09-07 00:00:00"),
                "BOOLEAN_PARAMETER", "false");

        // 2nd user is re-assigned
        role = VOServiceFactory.getRole(serviceRataDetails, "USER");
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-09-08 00:00:00"));
        subDetails = subscrSetup.addUser(basicSetup.getSecondCustomerUser1(),
                role, subDetails.getSubscriptionId());

        // terminate sub
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-09-10 00:00:00"));
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        // Delete technical service
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-09-10 13:00:00"));
        container.login(supplierAdmin.getKey(), ROLE_TECHNOLOGY_MANAGER);
        serviceSetup.deleteTechnicalService(example2TechService);

        // reset old cut off day
        resetCutOffDay(supplierAdmin.getKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_12", subDetails);
        BillingIntegrationTestBase.updateCustomerListForTests("PICT_TEST_12",
                customer);
    }

    /**
     * See testcase #13 in BESBillingFactorCombinations.xlsx BUT with time unit
     * HOUR not DAY
     */
    public void createPictScenario13() throws Exception {

        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-10-03 00:00:00"));

        // Create a supplier
        String supplierAdminId = "Pict13Supplier";
        VOOrganization supplier = orgSetup.createOrganization(
                basicSetup.getPlatformOperatorUserKey(), supplierAdminId,
                "Pict13SupplierOrg",
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
        VOTechnicalService example2TechService = serviceSetup
                .getTechnicalService(VOTechServiceFactory.TECH_SERVICE_EXAMPLE2_ID);

        setCutOffDay(supplierAdmin.getKey(), 1);

        // setup customer
        String customerAdminId = "Pict13Customer";
        VOOrganization customer = orgSetup.registerCustomer(
                "Pict13CustomerOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_UK,
                customerAdminId, supplierMarketplace.getMarketplaceId(),
                supplier.getOrganizationId());
        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);

        orgSetup.createMarketingPermission(supplierAdmin.getKey(),
                supplier.getOrganizationId(), example2TechService);

        // setup time unit hour service
        VOServiceDetails unitServTemplate = serviceSetup
                .createAndPublishMarketableService(supplierAdmin.getKey(),
                        "PICT13_UNIT_HOUR_SERVICE", TestService.EXAMPLE2,
                        TestPriceModel.FREE, example2TechService,
                        supplierMarketplace);
        VOServiceDetails serviceUnitDetails = serviceSetup
                .savePriceModelForCustomer(unitServTemplate,
                        TestPriceModel.EXAMPLE_PICT13_UNIT_HOUR, customer);

        // setup free service (only customer price Model)
        VOServiceDetails freeTemplate = serviceSetup
                .createAndPublishMarketableService(supplierAdmin.getKey(),
                        "PICT13_FREE", TestService.EXAMPLE2,
                        TestPriceModel.EXAMPLE_PICT13_UNIT_HOUR,
                        example2TechService, supplierMarketplace);
        VOServiceDetails serviceFreeDetails = serviceSetup
                .savePriceModelForCustomer(freeTemplate, TestPriceModel.FREE,
                        customer);

        // define upgrade path
        unitServTemplate = serviceSetup.registerCompatibleServices(
                supplierAdmin.getKey(), unitServTemplate, freeTemplate);

        // activate services
        serviceUnitDetails = serviceSetup
                .activateMarketableService(serviceUnitDetails);
        serviceFreeDetails = serviceSetup
                .activateMarketableService(serviceFreeDetails);

        // subscribe to service w role ADMIN
        VORoleDefinition role = VOServiceFactory.getRole(serviceUnitDetails,
                "ADMIN");
        serviceUnitDetails = serviceSetup.getServiceDetails(
                supplierAdmin.getKey(), serviceUnitDetails);
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "PICT_TEST_13", serviceUnitDetails, customerAdmin, role);

        // 2nd user is assigned
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-10-03 00:00:00"));
        role = VOServiceFactory.getRole(serviceUnitDetails, "USER");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        subDetails = subscrSetup.addUser(basicSetup.getSecondCustomerUser1(),
                role, subDetails.getSubscriptionId());

        // role change user 2 to ADMIN
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-10-04 10:20:00"));
        subDetails = subscrSetup.modifyUserRole(subDetails.getUsageLicenses()
                .get(1), VOServiceFactory.getRole(serviceUnitDetails, "ADMIN"),
                subDetails.getSubscriptionId());

        // parameter change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        subDetails = subscrSetup.modifyParameterForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-10-04 10:20:00"),
                "MAX_FOLDER_NUMBER", "5");

        // role change user 2 back to USER
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-10-04 10:30:00"));
        subDetails = subscrSetup.modifyUserRole(subDetails.getUsageLicenses()
                .get(1), VOServiceFactory.getRole(serviceUnitDetails, "USER"),
                subDetails.getSubscriptionId());

        // 2nd user is deassigned
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-10-06 00:00:00"));
        subDetails = subscrSetup.revokeUser(
                basicSetup.getSecondCustomerUser1(),
                subDetails.getSubscriptionId());

        // Suspend and resume the subscription by deleting the customer's
        // payment types; within one hour unit (so no difference in billing)
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-10-07 02:00:00"));
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER);
        paymentSetup.deleteCustomerPaymentTypes(customer);
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-10-07 02:10:00"));
        paymentSetup.reassignCustomerPaymentTypes(customer);
        subDetails = subscrSetup.getSubscriptionDetails(customerAdmin.getKey(),
                subDetails.getSubscriptionId());
        // reactivate subscription
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER);
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-10-07 02:20:00"));
        paymentSetup.reassignCustomerPaymentTypes(customer);
        subDetails = subscrSetup.getSubscriptionDetails(customerAdmin.getKey(),
                subDetails.getSubscriptionId());

        // 2nd user is re-assigned
        role = VOServiceFactory.getRole(serviceUnitDetails, "USER");
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-10-08 00:00:00"));
        subDetails = subscrSetup.addUser(basicSetup.getSecondCustomerUser1(),
                role, subDetails.getSubscriptionId());

        // Change price model of subscription
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER);
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-10-12 00:00:00"));
        VOPriceModel newSubPriceModel = VOPriceModelFactory
                .modifyPriceModelPeriodFee(subDetails.getPriceModel(),
                        new BigDecimal("1.00"));
        subscrSetup.savePriceModelForSubscription(supplierAdmin.getKey(),
                subDetails, newSubPriceModel, customer);

        // Upgrade the subscription from time-unit week to free
        serviceFreeDetails = serviceSetup.getServiceDetails(
                supplierAdmin.getKey(), serviceFreeDetails);
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-10-30 00:00:00"));
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(subDetails, serviceFreeDetails);

        // Delete marketable service
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-10-15 13:00:01"));
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER);
        serviceSetup.deleteMarketableService(serviceUnitDetails);

        // never terminate sub
        // reset old cut off day
        resetCutOffDay(supplierAdmin.getKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_13", subDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_13", upgradedSubDetails);
        BillingIntegrationTestBase.updateCustomerListForTests("PICT_TEST_13",
                customer);
    }

    /**
     * See testcase #13 in BESBillingFactorCombinations.xlsx
     */
    public void createPictScenario13_1() throws Exception {

        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-10-03 00:00:00"));

        // Create a supplier
        String supplierAdminId = "Pict13_1Supplier";
        VOOrganization supplier = orgSetup.createOrganization(
                basicSetup.getPlatformOperatorUserKey(), supplierAdminId,
                "Pict13_1SupplierOrg",
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
        VOTechnicalService example2TechService = serviceSetup
                .getTechnicalService(VOTechServiceFactory.TECH_SERVICE_EXAMPLE2_ID);

        setCutOffDay(supplierAdmin.getKey(), 1);

        // setup customer
        String customerAdminId = "Pict13_1Customer";
        VOOrganization customer = orgSetup.registerCustomer(
                "Pict13_1CustomerOrg",
                TestOrganizationSetup.ORGANIZATION_DOMICILE_UK,
                customerAdminId, supplierMarketplace.getMarketplaceId(),
                supplier.getOrganizationId());
        VOUser customerAdmin = orgSetup.getUser(customerAdminId, true);

        orgSetup.createMarketingPermission(supplierAdmin.getKey(),
                supplier.getOrganizationId(), example2TechService);

        // setup time unit day service
        VOServiceDetails unitServTemplate = serviceSetup
                .createAndPublishMarketableService(supplierAdmin.getKey(),
                        "PICT13_1_UNIT_DAY_SERVICE", TestService.EXAMPLE2,
                        TestPriceModel.FREE, example2TechService,
                        supplierMarketplace);
        VOServiceDetails serviceUnitDetails = serviceSetup
                .savePriceModelForCustomer(unitServTemplate,
                        TestPriceModel.EXAMPLE_PICT13_UNIT_DAY, customer);

        // setup free service (only customer price Model)
        VOServiceDetails freeTemplate = serviceSetup
                .createAndPublishMarketableService(supplierAdmin.getKey(),
                        "PICT13_1_FREE", TestService.EXAMPLE2,
                        TestPriceModel.EXAMPLE_PICT13_UNIT_HOUR,
                        example2TechService, supplierMarketplace);
        VOServiceDetails serviceFreeDetails = serviceSetup
                .savePriceModelForCustomer(freeTemplate, TestPriceModel.FREE,
                        customer);

        // define upgrade path
        unitServTemplate = serviceSetup.registerCompatibleServices(
                supplierAdmin.getKey(), unitServTemplate, freeTemplate);

        // activate services
        serviceUnitDetails = serviceSetup
                .activateMarketableService(serviceUnitDetails);
        serviceFreeDetails = serviceSetup
                .activateMarketableService(serviceFreeDetails);

        // subscribe to service w role ADMIN
        VORoleDefinition role = VOServiceFactory.getRole(serviceUnitDetails,
                "ADMIN");
        serviceUnitDetails = serviceSetup.getServiceDetails(
                supplierAdmin.getKey(), serviceUnitDetails);
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "PICT_TEST_13_1", serviceUnitDetails, customerAdmin, role);

        // 2nd user is assigned
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-10-03 00:00:00"));
        role = VOServiceFactory.getRole(serviceUnitDetails, "USER");
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        subDetails = subscrSetup.addUser(basicSetup.getSecondCustomerUser1(),
                role, subDetails.getSubscriptionId());

        // role change user 2 to ADMIN
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-10-04 10:00:00"));
        subDetails = subscrSetup.modifyUserRole(subDetails.getUsageLicenses()
                .get(1), VOServiceFactory.getRole(serviceUnitDetails, "ADMIN"),
                subDetails.getSubscriptionId());

        // parameter change
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        subDetails = subscrSetup.modifyParameterForSubscription(subDetails,
                DateTimeHandling.calculateMillis("2013-10-04 10:00:00"),
                "MAX_FOLDER_NUMBER", "5");

        // role change user 2 back to USER
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-10-04 11:00:00"));
        subDetails = subscrSetup.modifyUserRole(subDetails.getUsageLicenses()
                .get(1), VOServiceFactory.getRole(serviceUnitDetails, "USER"),
                subDetails.getSubscriptionId());

        // 2nd user is deassigned
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-10-06 00:00:00"));
        subDetails = subscrSetup.revokeUser(
                basicSetup.getSecondCustomerUser1(),
                subDetails.getSubscriptionId());

        // Suspend and resume the subscription by deleting the customer's
        // payment types; within one day (so no difference in billing)
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-10-07 02:00:00"));
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER);
        paymentSetup.deleteCustomerPaymentTypes(customer);
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-10-07 05:00:00"));
        paymentSetup.reassignCustomerPaymentTypes(customer);
        subDetails = subscrSetup.getSubscriptionDetails(customerAdmin.getKey(),
                subDetails.getSubscriptionId());
        // reactivate subscription
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER);
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-10-07 10:00:00"));
        paymentSetup.reassignCustomerPaymentTypes(customer);
        subDetails = subscrSetup.getSubscriptionDetails(customerAdmin.getKey(),
                subDetails.getSubscriptionId());

        // 2nd user is re-assigned
        role = VOServiceFactory.getRole(serviceUnitDetails, "USER");
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-10-08 00:00:00"));
        subDetails = subscrSetup.addUser(basicSetup.getSecondCustomerUser1(),
                role, subDetails.getSubscriptionId());

        // Change price model of subscription
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER);
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-10-12 00:00:00"));
        VOPriceModel newSubPriceModel = VOPriceModelFactory
                .modifyPriceModelPeriodFee(subDetails.getPriceModel(),
                        new BigDecimal("1.00"));
        subscrSetup.savePriceModelForSubscription(supplierAdmin.getKey(),
                subDetails, newSubPriceModel, customer);

        // Upgrade the subscription from time-unit week to free
        serviceFreeDetails = serviceSetup.getServiceDetails(
                supplierAdmin.getKey(), serviceFreeDetails);
        container.login(customerAdmin.getKey(), ROLE_ORGANIZATION_ADMIN);
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-10-30 00:00:00"));
        VOSubscriptionDetails upgradedSubDetails = subscrSetup
                .upgradeSubscription(subDetails, serviceFreeDetails);

        // Delete marketable service
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-10-15 13:00:01"));
        container.login(supplierAdmin.getKey(), ROLE_SERVICE_MANAGER);
        serviceSetup.deleteMarketableService(serviceUnitDetails);

        // never terminate sub
        // reset old cut off day
        resetCutOffDay(supplierAdmin.getKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_13_1", subDetails);
        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PICT_TEST_13_1", upgradedSubDetails);
        BillingIntegrationTestBase.updateCustomerListForTests("PICT_TEST_13_1",
                customer);
    }
}
