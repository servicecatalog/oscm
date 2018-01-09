/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: May 21, 2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.calculation.revenue.setup;

import org.oscm.billingservice.setup.CustomerData;
import org.oscm.billingservice.setup.IntegrationTestSetup;
import org.oscm.billingservice.setup.TestData;
import org.oscm.billingservice.setup.VOPriceModelFactory.TestPriceModel;
import org.oscm.billingservice.setup.VendorData;
import org.oscm.internal.vo.VOServiceDetails;
import org.oscm.internal.vo.VOSubscriptionDetails;

/**
 * @author farmaki
 * 
 */
public class PartnerSetup extends IntegrationTestSetup {

    /**
     * Subscribe to an Synchronous Reseller Service.
     */
    public void subscribeToSyncResellerService() throws Exception {
        VendorData supplierData = setupNewSupplier("2013-01-01 08:00:00");
        // Create supplier Synchronous service
        setDateFactory("2013-01-02 20:00:00");
        VOServiceDetails supplService = createPublishActivateService(
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
        subscribe(resellerCustomerData.getAdminUser(), "resellerSubscription",
                resellerService, "2013-08-18 00:00:00", "ADMIN");

        resetCutOffDay(supplierData.getAdminKey());
        resetCutOffDay(resellerData.getAdminKey());

        cacheTestData("subscribeToSyncResellerService", new TestData(
                supplierData, resellerData));
    }

    /**
     * Subscribe to an Synchronous Reseller Service. Change the ID of the
     * service template after the billing period.
     */
    public void subscribeToSyncResellerService_changeServiceIdAfterBP()
            throws Exception {
        VendorData supplierData = setupNewSupplier("2013-01-01 08:00:00");
        // Create supplier Synchronous service
        setDateFactory("2013-01-02 20:00:00");
        VOServiceDetails supplService = createPublishActivateService(
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
        subscribe(resellerCustomerData.getAdminUser(), "resellerSubscription2",
                resellerService, "2013-08-18 00:00:00", "ADMIN");

        resetCutOffDay(supplierData.getAdminKey());
        resetCutOffDay(resellerData.getAdminKey());

        // Change the ID of the service template
        updateServiceId(supplierData, supplService, "supplSrvForReseller_new",
                "2013-09-15 08:00:00");

        cacheTestData("subscribeToSyncResellerService_changeServiceIdAfterBP",
                new TestData(supplierData, resellerData));
    }

    /**
     * Subscribe to Synchronous Broker Service.
     */
    public void subscribeToSyncBrokerService() throws Exception {
        VendorData supplierData = setupNewSupplier("2013-01-01 08:00:00");
        // Create supplier Synchronous service
        setDateFactory("2013-01-02 20:00:00");
        VOServiceDetails supplService = createPublishActivateService(
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
        subscribe(brokerCustomerData.getAdminUser(), "brokerSubscription",
                brokerService, "2013-08-18 00:00:00", "ADMIN");

        resetCutOffDay(supplierData.getAdminKey());

        cacheTestData("subscribeToSyncBrokerService", new TestData(
                supplierData, brokerData));
    }

    /**
     * Subscribe to Synchronous Broker Service. Change the ID of the service
     * template in the billing period.
     */
    public void subscribeToSyncBrokerService_changeServiceIdInBP()
            throws Exception {
        VendorData supplierData = setupNewSupplier("2013-01-01 08:00:00");
        // Create supplier Synchronous service
        setDateFactory("2013-01-02 20:00:00");
        VOServiceDetails supplService = createPublishActivateService(
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
        subscribe(brokerCustomerData.getAdminUser(), "brokerSubscription2",
                brokerService, "2013-08-18 00:00:00", "ADMIN");

        // Change the ID of the service template
        updateServiceId(supplierData, supplService, "supplSrvForBroker_new",
                "2013-08-30 10:00:00");

        resetCutOffDay(supplierData.getAdminKey());

        cacheTestData("subscribeToSyncBrokerService_changeServiceIdInBP",
                new TestData(supplierData, brokerData));
    }

    public void createBrokerScenario1() throws Exception {
        VendorData supplierData = setupNewSupplier("2013-01-01 08:00:00");
        // Create supplier Async service
        setDateFactory("2013-01-02 20:00:00");
        VOServiceDetails supplService = createPublishActivateService(
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
                "2013-08-18 00:00:00", "ADMIN");

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
        VOServiceDetails supplService = createPublishActivateService(
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
                "2013-08-18 00:00:00", "ADMIN");

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

    public void createResellerScenario1() throws Exception {
        VendorData supplierData = setupNewSupplier("2013-01-01 08:00:00");
        // Create supplier Async service
        setDateFactory("2013-01-02 20:00:00");
        VOServiceDetails supplService = createPublishActivateService(
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
                resellerService, "2013-08-18 00:00:00", "ADMIN");

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
        VOServiceDetails supplService = createPublishActivateService(
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
                resellerService, "2013-08-18 00:00:00", "ADMIN");

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

}
