/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: May 19, 2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.calculation.revenue.setup;

import org.oscm.billingservice.setup.CustomerData;
import org.oscm.billingservice.setup.IntegrationTestSetup;
import org.oscm.billingservice.setup.VOPriceModelFactory.TestPriceModel;
import org.oscm.billingservice.setup.VendorData;
import org.oscm.internal.vo.VOServiceDetails;
import org.oscm.internal.vo.VOSubscriptionDetails;

/**
 * @author farmaki
 * 
 */
public class PartnerAsyncSetup extends IntegrationTestSetup {

    /**
     * Subscribe to an Async Reseller Service.
     */
    public void subscribeToAsyncResellerService() throws Exception {
        VendorData supplierData = setupNewSupplier("2013-01-01 08:00:00");
        // Create supplier Async service
        setDateFactory("2013-01-02 20:00:00");
        VOServiceDetails supplService = createPublishActivateAsyncService(
                supplierData, TestPriceModel.EXAMPLE_RATA_WEEK_ROLES,
                "supplSrvForReseller");

        // Setup reseller and reseller customer
        VendorData resellerData = setupNewReseller("2013-01-03 08:10:00");

        setCutOffDay(resellerData.getAdminKey(), 1);
        CustomerData resellerCustomerData = registerCustomer(resellerData,
                "resellerCustomer");

        // Create reseller service
        setDateFactory("2013-07-10 20:00:00");
        VOServiceDetails resellerService = grantResalePermission(supplierData,
                supplService, resellerData);
        resellerService = publishActivateService(resellerData, resellerService);

        // Subscribe to reseller service
        VOSubscriptionDetails subDetails = subscribe(
                resellerCustomerData.getAdminUser(), "resellerSubscr",
                resellerService, "2013-08-01 12:00:00", "ADMIN");

        // Complete the subscription
        completeAsyncSubscription(basicSetup.getSupplierAdminKey(),
                resellerCustomerData.getAdminUser(), subDetails,
                "2013-08-18 00:00:00");

        resetCutOffDay(supplierData.getAdminKey());
        resetCutOffDay(resellerData.getAdminKey());
    }

    /**
     * Subscribe to an Async Broker Service.
     */
    public void subscribeToAsyncBrokerService() throws Exception {
        VendorData supplierData = setupNewSupplier("2013-01-01 08:00:00");
        // Create supplier Async service
        setDateFactory("2013-01-02 20:00:00");
        VOServiceDetails supplService = createPublishActivateAsyncService(
                supplierData, TestPriceModel.EXAMPLE_RATA_WEEK_ROLES,
                "supplSrvForBroker");

        // Setup broker and broker customer
        VendorData brokerData = setupNewBroker("2013-01-03 08:10:00");

        setCutOffDay(brokerData.getAdminKey(), 1);
        CustomerData brokerCustomerData = registerCustomer(brokerData,
                "brokerCustomer");

        // Create broker service
        setDateFactory("2013-07-10 20:00:00");
        VOServiceDetails brokerService = grantResalePermission(supplierData,
                supplService, brokerData);
        brokerService = publishActivateService(brokerData, brokerService);

        // Subscribe to broker service
        VOSubscriptionDetails subDetails = subscribe(
                brokerCustomerData.getAdminUser(), "brokerSubscr",
                brokerService, "2013-08-01 12:00:00", "ADMIN");

        // Complete the subscription
        completeAsyncSubscription(basicSetup.getSupplierAdminKey(),
                brokerCustomerData.getAdminUser(), subDetails,
                "2013-08-18 00:00:00");

        resetCutOffDay(supplierData.getAdminKey());
    }

    public void createBrokerScenario1() throws Exception {
        VendorData supplierData = setupNewSupplier("2013-01-01 08:00:00");
        // Create supplier Async service
        setDateFactory("2013-01-02 20:00:00");
        VOServiceDetails supplService = createPublishActivateAsyncService(
                supplierData, TestPriceModel.EXAMPLE_PERUNIT_WEEK_ROLES,
                "Scenario1");

        // Setup broker and broker customer
        VendorData brokerData = setupNewBroker("2013-01-03 08:10:00");

        setCutOffDay(brokerData.getAdminKey(), 1);
        CustomerData brokerCustomerData = registerCustomer(brokerData,
                "brokerCustomer1");

        // Create broker service
        setDateFactory("2013-07-10 20:00:00");
        VOServiceDetails brokerService = grantResalePermission(supplierData,
                supplService, brokerData);
        brokerService = publishActivateService(brokerData, brokerService);

        // Subscribe to broker service
        VOSubscriptionDetails subDetails = subscribe(
                brokerCustomerData.getAdminUser(), "Scenario1", brokerService,
                "2013-08-01 12:00:00", "ADMIN");

        // Complete the subscription
        completeAsyncSubscription(basicSetup.getSupplierAdminKey(),
                brokerCustomerData.getAdminUser(), subDetails,
                "2013-08-18 00:00:00");

        // terminate Sub
        container.login(brokerCustomerData.getAdminUser().getKey(),
                ROLE_ORGANIZATION_ADMIN);
        setDateFactory("2013-08-18 10:00:00");
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(supplierData.getAdminKey());
    }

    public void createBrokerScenario2() throws Exception {
        VendorData supplierData = setupNewSupplier("2013-01-01 08:00:00");
        // Create supplier Async service
        setDateFactory("2013-01-02 20:00:00");
        VOServiceDetails supplService = createPublishActivateAsyncService(
                supplierData, TestPriceModel.EXAMPLE_PERUNIT_WEEK_ROLES,
                "Scenario2");

        // Setup broker and broker customer
        VendorData brokerData = setupNewBroker("2013-01-03 08:10:00");

        setCutOffDay(brokerData.getAdminKey(), 1);
        CustomerData brokerCustomerData = registerCustomer(brokerData,
                "brokerCustomer2");

        // Create broker service
        setDateFactory("2013-07-10 20:00:00");
        VOServiceDetails brokerService = grantResalePermission(supplierData,
                supplService, brokerData);
        brokerService = publishActivateService(brokerData, brokerService);

        // Subscribe to broker service
        VOSubscriptionDetails subDetails = subscribe(
                brokerCustomerData.getAdminUser(), "Scenario2", brokerService,
                "2013-08-01 12:00:00", "ADMIN");

        // Complete the subscription
        completeAsyncSubscription(basicSetup.getSupplierAdminKey(),
                brokerCustomerData.getAdminUser(), subDetails,
                "2013-08-18 00:00:00");

        // Suspend and resume the subscription
        setDateFactory("2013-08-18 10:00:00");
        paymentSetup.deleteCustomerPaymentTypes(supplierData,
                brokerCustomerData.getOrganization());

        setDateFactory("2013-08-18 20:00:00");
        paymentSetup.reassignCustomerPaymentTypes(supplierData,
                brokerCustomerData.getOrganization());

        // terminate Sub
        container.login(brokerCustomerData.getAdminUser().getKey(),
                ROLE_ORGANIZATION_ADMIN);
        setDateFactory("2013-08-20 10:00:00");
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(supplierData.getAdminKey());
    }

    public void createBrokerScenario3() throws Exception {
        VendorData supplierData = setupNewSupplier("2013-01-01 08:00:00");
        // Create supplier Async service
        setDateFactory("2013-01-02 20:00:00");
        VOServiceDetails supplService = createPublishActivateAsyncService(
                supplierData, TestPriceModel.EXAMPLE_PERUNIT_WEEK_ROLES,
                "Scenario2");

        // Setup broker and broker customer
        VendorData brokerData = setupNewBroker("2013-01-03 08:10:00");

        setCutOffDay(brokerData.getAdminKey(), 1);
        CustomerData brokerCustomerData = registerCustomer(brokerData,
                "brokerCustomer3");

        // Create broker service
        setDateFactory("2013-07-10 20:00:00");
        VOServiceDetails brokerService = grantResalePermission(supplierData,
                supplService, brokerData);
        brokerService = publishActivateService(brokerData, brokerService);

        // Subscribe to broker service
        VOSubscriptionDetails subDetails = subscribe(
                brokerCustomerData.getAdminUser(), "Scenario3", brokerService,
                "2013-08-01 12:00:00", "ADMIN");

        // Suspend the subscription
        setDateFactory("2013-08-18 13:00:00");
        // delete paymenttypes
        container.login(supplierData.getAdminKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        paymentSetup.deleteServicePaymentTypes(supplService);

        // Complete the subscription
        container.login(brokerCustomerData.getAdminUser().getKey(),
                ROLE_SERVICE_MANAGER, ROLE_TECHNOLOGY_MANAGER);
        completeAsyncSubscription(basicSetup.getSupplierAdminKey(),
                brokerCustomerData.getAdminUser(), subDetails,
                "2013-08-19 00:00:00");

        // resume the subscription
        setDateFactory("2013-08-19 20:00:00");
        container.login(supplierData.getAdminKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        paymentSetup.reassignServicePaymentTypes(supplService);

        // terminate Sub
        container.login(brokerCustomerData.getAdminUser().getKey(),
                ROLE_ORGANIZATION_ADMIN);
        setDateFactory("2013-08-20 10:00:00");
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(supplierData.getAdminKey());
    }

    public void createBrokerScenario4() throws Exception {
        VendorData supplierData = setupNewSupplier("2013-01-01 08:00:00");
        // Create supplier Async service
        setDateFactory("2013-01-02 20:00:00");
        VOServiceDetails supplService = createPublishActivateAsyncService(
                supplierData, TestPriceModel.EXAMPLE_PERUNIT_WEEK_ROLES,
                "Scenario2");

        // Setup broker and broker customer
        VendorData brokerData = setupNewBroker("2013-01-03 08:10:00");

        setCutOffDay(brokerData.getAdminKey(), 1);
        CustomerData brokerCustomerData = registerCustomer(brokerData,
                "brokerCustomer4");

        // Create broker service
        setDateFactory("2013-07-10 20:00:00");
        VOServiceDetails brokerService = grantResalePermission(supplierData,
                supplService, brokerData);
        brokerService = publishActivateService(brokerData, brokerService);

        // Subscribe to broker service
        VOSubscriptionDetails subDetails = subscribe(
                brokerCustomerData.getAdminUser(), "Scenario4", brokerService,
                "2013-08-01 12:00:00", "ADMIN");

        // Suspend the subscription
        setDateFactory("2013-08-18 13:00:00");
        // delete paymenttypes
        container.login(supplierData.getAdminKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        paymentSetup.deleteServicePaymentTypes(supplService);

        // resume the subscription
        setDateFactory("2013-08-19 19:00:00");
        container.login(supplierData.getAdminKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        paymentSetup.reassignServicePaymentTypes(supplService);

        // Complete the subscription
        container.login(brokerCustomerData.getAdminUser().getKey(),
                ROLE_SERVICE_MANAGER, ROLE_TECHNOLOGY_MANAGER);
        completeAsyncSubscription(basicSetup.getSupplierAdminKey(),
                brokerCustomerData.getAdminUser(), subDetails,
                "2013-08-19 20:00:00");

        // resume the subscription
        setDateFactory("2013-08-19 20:00:00");
        container.login(supplierData.getAdminKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        paymentSetup.reassignServicePaymentTypes(supplService);

        // terminate Sub
        container.login(brokerCustomerData.getAdminUser().getKey(),
                ROLE_ORGANIZATION_ADMIN);
        setDateFactory("2013-08-20 10:00:00");
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(supplierData.getAdminKey());
    }

    public void createResellerScenario1() throws Exception {
        VendorData supplierData = setupNewSupplier("2013-01-01 08:00:00");
        // Create supplier Async service
        setDateFactory("2013-01-02 20:00:00");
        VOServiceDetails supplService = createPublishActivateAsyncService(
                supplierData, TestPriceModel.EXAMPLE_PERUNIT_WEEK_ROLES,
                "ResellerScenario1");

        // Setup reseller and reseller customer
        VendorData resellerData = setupNewReseller("2013-01-03 08:10:00");

        setCutOffDay(resellerData.getAdminKey(), 1);
        CustomerData resellerCustomerData = registerCustomer(resellerData,
                "resellerCustomer1");

        // Create reseller service
        setDateFactory("2013-07-10 20:00:00");
        VOServiceDetails resellerService = grantResalePermission(supplierData,
                supplService, resellerData);
        resellerService = publishActivateService(resellerData, resellerService);

        // Subscribe to reseller service
        VOSubscriptionDetails subDetails = subscribe(
                resellerCustomerData.getAdminUser(), "ResellerScenario1",
                resellerService, "2013-08-01 12:00:00", "ADMIN");

        // Complete the subscription
        completeAsyncSubscription(basicSetup.getSupplierAdminKey(),
                resellerCustomerData.getAdminUser(), subDetails,
                "2013-08-18 00:00:00");
        // terminate Sub
        container.login(resellerCustomerData.getAdminUser().getKey(),
                ROLE_ORGANIZATION_ADMIN);
        setDateFactory("2013-08-18 10:00:00");
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(supplierData.getAdminKey());
    }

    public void createResellerScenario2() throws Exception {
        VendorData supplierData = setupNewSupplier("2013-01-01 08:00:00");
        // Create supplier Async service
        setDateFactory("2013-01-02 20:00:00");
        VOServiceDetails supplService = createPublishActivateAsyncService(
                supplierData, TestPriceModel.EXAMPLE_PERUNIT_WEEK_ROLES,
                "ResellerScenario2");

        // Setup reseller and reseller customer
        VendorData resellerData = setupNewReseller("2013-01-03 08:10:00");

        setCutOffDay(resellerData.getAdminKey(), 1);
        CustomerData resellerCustomerData = registerCustomer(resellerData,
                "resellerCustomer2");

        // Create reseller service
        setDateFactory("2013-07-10 20:00:00");
        VOServiceDetails resellerService = grantResalePermission(supplierData,
                supplService, resellerData);
        resellerService = publishActivateService(resellerData, resellerService);

        // Subscribe to reseller service
        VOSubscriptionDetails subDetails = subscribe(
                resellerCustomerData.getAdminUser(), "ResellerScenario2",
                resellerService, "2013-08-01 12:00:00", "ADMIN");

        // Complete the subscription
        completeAsyncSubscription(basicSetup.getSupplierAdminKey(),
                resellerCustomerData.getAdminUser(), subDetails,
                "2013-08-18 00:00:00");

        // Suspend and resume the subscription
        setDateFactory("2013-08-18 10:00:00");
        paymentSetup.deleteCustomerPaymentTypes(resellerData,
                resellerCustomerData.getOrganization());

        setDateFactory("2013-08-18 20:00:00");
        paymentSetup.reassignCustomerPaymentTypes(resellerData,
                resellerCustomerData.getOrganization());

        // terminate Sub
        container.login(resellerCustomerData.getAdminUser().getKey(),
                ROLE_ORGANIZATION_ADMIN);
        setDateFactory("2013-08-20 10:00:00");
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(supplierData.getAdminKey());
    }

    public void createResellerScenario3() throws Exception {
        VendorData supplierData = setupNewSupplier("2013-01-01 08:00:00");
        // Create supplier Async service
        setDateFactory("2013-01-02 20:00:00");
        VOServiceDetails supplService = createPublishActivateAsyncService(
                supplierData, TestPriceModel.EXAMPLE_PERUNIT_WEEK_ROLES,
                "ResellerScenario2");

        // Setup reseller and reseller customer
        VendorData resellerData = setupNewReseller("2013-01-03 08:10:00");

        setCutOffDay(resellerData.getAdminKey(), 1);
        CustomerData resellerCustomerData = registerCustomer(resellerData,
                "resellerCustomer3");

        // Create reseller service
        setDateFactory("2013-07-10 20:00:00");
        VOServiceDetails resellerService = grantResalePermission(supplierData,
                supplService, resellerData);
        resellerService = publishActivateService(resellerData, resellerService);

        // Subscribe to reseller service
        VOSubscriptionDetails subDetails = subscribe(
                resellerCustomerData.getAdminUser(), "ResellerScenario3",
                resellerService, "2013-08-01 12:00:00", "ADMIN");

        // Suspend the subscription
        setDateFactory("2013-08-18 13:00:00");
        // delete paymenttypes
        container.login(resellerData.getAdminKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        paymentSetup.deleteServicePaymentTypes(resellerService);

        // Complete the subscription
        container.login(resellerCustomerData.getAdminUser().getKey(),
                ROLE_SERVICE_MANAGER, ROLE_TECHNOLOGY_MANAGER);
        completeAsyncSubscription(basicSetup.getSupplierAdminKey(),
                resellerCustomerData.getAdminUser(), subDetails,
                "2013-08-19 00:00:00");

        // resume the subscription
        setDateFactory("2013-08-19 20:00:00");
        container.login(resellerData.getAdminKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        paymentSetup.reassignServicePaymentTypes(resellerService);

        // terminate Sub
        container.login(resellerCustomerData.getAdminUser().getKey(),
                ROLE_ORGANIZATION_ADMIN);
        setDateFactory("2013-08-20 10:00:00");
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(supplierData.getAdminKey());
    }

    public void createResellerScenario4() throws Exception {
        VendorData supplierData = setupNewSupplier("2013-01-01 08:00:00");
        // Create supplier Async service
        setDateFactory("2013-01-02 20:00:00");
        VOServiceDetails supplService = createPublishActivateAsyncService(
                supplierData, TestPriceModel.EXAMPLE_PERUNIT_WEEK_ROLES,
                "ResellerScenario2");

        // Setup reseller and reseller customer
        VendorData resellerData = setupNewReseller("2013-01-03 08:10:00");

        setCutOffDay(resellerData.getAdminKey(), 1);
        CustomerData resellerCustomerData = registerCustomer(resellerData,
                "resellerCustomer4");

        // Create reseller service
        setDateFactory("2013-07-10 20:00:00");
        VOServiceDetails resellerService = grantResalePermission(supplierData,
                supplService, resellerData);
        resellerService = publishActivateService(resellerData, resellerService);

        // Subscribe to reseller service
        VOSubscriptionDetails subDetails = subscribe(
                resellerCustomerData.getAdminUser(), "ResellerScenario4",
                resellerService, "2013-08-01 12:00:00", "ADMIN");

        // Suspend the subscription
        setDateFactory("2013-08-18 13:00:00");
        // delete paymenttypes
        container.login(resellerData.getAdminKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        paymentSetup.deleteServicePaymentTypes(resellerService);

        // resume the subscription
        setDateFactory("2013-08-19 19:00:00");
        container.login(resellerData.getAdminKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        paymentSetup.reassignServicePaymentTypes(resellerService);

        // Complete the subscription
        container.login(resellerCustomerData.getAdminUser().getKey(),
                ROLE_SERVICE_MANAGER, ROLE_TECHNOLOGY_MANAGER);
        completeAsyncSubscription(basicSetup.getSupplierAdminKey(),
                resellerCustomerData.getAdminUser(), subDetails,
                "2013-08-19 20:00:00");

        // resume the subscription
        setDateFactory("2013-08-19 20:00:00");
        container.login(supplierData.getAdminKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        paymentSetup.reassignServicePaymentTypes(supplService);

        // terminate Sub
        container.login(resellerCustomerData.getAdminUser().getKey(),
                ROLE_ORGANIZATION_ADMIN);
        setDateFactory("2013-08-20 10:00:00");
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(supplierData.getAdminKey());
    }

}
