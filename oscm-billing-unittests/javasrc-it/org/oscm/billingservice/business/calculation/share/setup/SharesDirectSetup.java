/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 03.09.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.calculation.share.setup;

import java.math.BigDecimal;

import org.oscm.billingservice.setup.CustomerData;
import org.oscm.billingservice.setup.TestData;
import org.oscm.billingservice.setup.VOPriceModelFactory.TestPriceModel;
import org.oscm.billingservice.setup.VendorData;
import org.oscm.test.DateTimeHandling;
import org.oscm.internal.vo.VOServiceDetails;
import org.oscm.internal.vo.VOSubscriptionDetails;

/**
 * @author baumann
 * 
 */
public class SharesDirectSetup extends SharesSetup {

    /**
     * Creates a subscription which is suspended and resumed in the same billing
     * period. The customer has no discount.
     */
    public void setupSuspendResume(String testName) throws Exception {
        VendorData supplierData = setupNewSupplier("2014-01-01 08:00:00");
        setDateFactory("2014-01-04 20:00:00");
        setCutOffDay(supplierData.getAdminKey(), 1);
        CustomerData customerData = registerCustomer(supplierData);

        VOServiceDetails service = createPublishActivateService(supplierData,
                TestPriceModel.EXAMPLE_RATA_WEEK_ROLES, "susresService");
        updateOperatorRevenueShare(10.0D, service.getKey());
        updateMarketplaceRevenueShare(20.0D, supplierData.getMarketplaceId(0));

        subscribe(customerData.getAdminUser(), "susresService_subscr", service,
                "2014-02-01 00:00:00", "ADMIN");

        // suspend and resume the subscription
        setDateFactory("2014-02-15 00:00:00");
        paymentSetup.deleteCustomerPaymentTypes(supplierData,
                customerData.getOrganization());

        setDateFactory("2014-02-22 00:00:00");
        paymentSetup.reassignCustomerPaymentTypes(supplierData,
                customerData.getOrganization());

        resetCutOffDay(supplierData.getAdminKey());
        cacheTestData(testName, new TestData(supplierData));
    }

    /**
     * Creates a subscription which is suspended and resumed for a customer with
     * a discount.
     */
    public void setupSuspendAndResumeWithCustomerDiscount(String testName,
            BigDecimal discount, double operatorShare) throws Exception {
        VendorData supplierData = setupNewSupplier("2013-01-01 08:00:00");
        setDateFactory("2013-07-10 20:00:00");
        setCutOffDay(supplierData.getAdminKey(), 1);
        CustomerData customerData = registerCustomerWithDiscount(supplierData,
                discount,
                DateTimeHandling.calculateMillis("2013-01-01 08:00:00"),
                DateTimeHandling.calculateMillis("2014-01-01 08:00:00"));

        VOServiceDetails service = createPublishActivateService(supplierData,
                TestPriceModel.EXAMPLE_RATA_WEEK_ROLES, "srvDiscount");
        updateOperatorRevenueShare(operatorShare, service.getKey());

        subscribe(customerData.getAdminUser(), "srvDiscount_subscr", service,
                "2013-08-01 12:00:00", "ADMIN");

        // suspend and resume the subscription
        setDateFactory("2013-08-10 07:00:00");
        paymentSetup.deleteCustomerPaymentTypes(supplierData,
                customerData.getOrganization());

        setDateFactory("2013-08-27 12:00:00");
        paymentSetup.reassignCustomerPaymentTypes(supplierData,
                customerData.getOrganization());

        resetCutOffDay(supplierData.getAdminKey());
        cacheTestData(testName, new TestData(supplierData));
    }

    /**
     * Two customers subscribe to the same service, and a third customer to a
     * second service. The customers have different discounts and their
     * subscriptions are suspended and resumed in different periods for each
     * customer.
     */
    public void setupSuspendAndResumeMultipleCustomerSubscriptions(
            String testName, double operatorShare1, double operatorShare2,
            double marketplaceShare) throws Exception {
        VendorData supplierData = setupNewSupplier("2013-01-01 08:00:00");
        setDateFactory("2013-07-10 20:00:00");
        setCutOffDay(supplierData.getAdminKey(), 1);

        // Register three customers with different discounts.
        CustomerData customerData1 = registerCustomerWithDiscount(supplierData,
                new BigDecimal("50.00"),
                DateTimeHandling.calculateMillis("2013-01-01 08:00:00"),
                DateTimeHandling.calculateMillis("2014-01-01 08:00:00"));

        CustomerData customerData2 = registerCustomerWithDiscount(supplierData,
                new BigDecimal("25.00"),
                DateTimeHandling.calculateMillis("2013-01-01 08:00:00"),
                DateTimeHandling.calculateMillis("2014-01-01 08:00:00"));

        CustomerData customerData3 = registerCustomerWithDiscount(supplierData,
                new BigDecimal("10.00"),
                DateTimeHandling.calculateMillis("2013-01-01 08:00:00"),
                DateTimeHandling.calculateMillis("2014-01-01 08:00:00"));

        VOServiceDetails service1 = createPublishActivateService(supplierData,
                TestPriceModel.EXAMPLE_RATA_DAY_ROLES, "srv");

        VOServiceDetails service2 = createPublishActivateService(supplierData,
                TestPriceModel.EXAMPLE_RATA_DAY_ROLES, "srv2");

        // Update the operator shares of the two different services.
        updateOperatorRevenueShare(operatorShare1, service1.getKey());
        updateOperatorRevenueShare(operatorShare2, service2.getKey());

        updateMarketplaceRevenueShare(marketplaceShare,
                supplierData.getMarketplaceId(0));

        // The two first customers subscribe to the same service
        subscribe(customerData1.getAdminUser(), "srv_subscr1", service1,
                "2013-08-01 12:00:00", "ADMIN");

        subscribe(customerData2.getAdminUser(), "srv_subscr2", service1,
                "2013-08-01 12:00:00", "ADMIN");

        // The third customer subscribes to another service
        subscribe(customerData3.getAdminUser(), "srv2_subscr3", service2,
                "2013-08-01 12:00:00", "ADMIN");

        // Suspend and resume the subscription of the first customer
        setDateFactory("2013-08-04 12:00:00");
        paymentSetup.deleteCustomerPaymentTypes(supplierData,
                customerData1.getOrganization());

        setDateFactory("2013-08-06 12:00:00");
        paymentSetup.reassignCustomerPaymentTypes(supplierData,
                customerData1.getOrganization());

        // Suspend and resume the subscription of the second customer
        setDateFactory("2013-08-05 12:00:00");
        paymentSetup.deleteCustomerPaymentTypes(supplierData,
                customerData2.getOrganization());

        setDateFactory("2013-08-10 12:00:00");
        paymentSetup.reassignCustomerPaymentTypes(supplierData,
                customerData2.getOrganization());

        // Suspend and resume the subscription of the third customer
        setDateFactory("2013-08-07 12:00:00");
        paymentSetup.deleteCustomerPaymentTypes(supplierData,
                customerData3.getOrganization());

        setDateFactory("2013-08-14 12:00:00");
        paymentSetup.reassignCustomerPaymentTypes(supplierData,
                customerData3.getOrganization());

        resetCutOffDay(supplierData.getAdminKey());
        cacheTestData(testName, new TestData(supplierData));
    }

    /**
     * Creates a subscription which is suspended and resumed, then upgraded,
     * then again suspended and resumed and then again upgraded
     */
    public void setupSuspendUpgradeSuspendUpgrade(String testName,
            BigDecimal discount, double operatorShare) throws Exception {
        VendorData supplierData = setupNewSupplier("2013-01-01 08:00:00",
                new BigDecimal(operatorShare));
        setDateFactory("2013-07-10 20:00:00");
        setCutOffDay(supplierData.getAdminKey(), 1);
        CustomerData customerData = registerCustomerWithDiscount(supplierData,
                discount,
                DateTimeHandling.calculateMillis("2013-01-01 08:00:00"),
                DateTimeHandling.calculateMillis("2014-01-01 08:00:00"));

        VOServiceDetails service = createPublishActivateService(supplierData,
                TestPriceModel.EXAMPLE_RATA_DAY_ROLES, "srvDiscount");

        VOSubscriptionDetails subscr = subscribe(customerData.getAdminUser(),
                "srvDiscount_subscr", service, "2013-08-01 12:00:00", "ADMIN");

        // First suspend and resume the subscription
        setDateFactory("2013-08-04 12:00:00");
        container.login(supplierData.getAdminKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        paymentSetup.deleteCustomerPaymentTypes(customerData.getOrganization());

        setDateFactory("2013-08-06 12:00:00");
        paymentSetup.reassignCustomerPaymentTypes(customerData
                .getOrganization());

        // Then upgrade the subscription
        subscr = subscrSetup.getSubscriptionDetails(customerData.getAdminKey(),
                subscr.getSubscriptionId());
        VOServiceDetails upgrService = createAndRegisterCompatibleService(
                supplierData, TestPriceModel.EXAMPLE_RATA_WEEK_ROLES, service,
                "srvDiscount_upgr");
        upgrade(customerData.getAdminUser(), subscr, upgrService,
                "2013-08-08 12:00:00");

        // Suspend and resume again
        setDateFactory("2013-08-15 12:00:00");
        container.login(supplierData.getAdminKey(), ROLE_SERVICE_MANAGER,
                ROLE_TECHNOLOGY_MANAGER);
        paymentSetup.deleteCustomerPaymentTypes(customerData.getOrganization());

        setDateFactory("2013-08-17 12:00:00");
        paymentSetup.reassignCustomerPaymentTypes(customerData
                .getOrganization());

        // Upgrade again
        subscr = subscrSetup.getSubscriptionDetails(customerData.getAdminKey(),
                subscr.getSubscriptionId());
        VOServiceDetails upgrService2 = createAndRegisterCompatibleService(
                supplierData, TestPriceModel.EXAMPLE_RATA_DAY_ROLES,
                upgrService, "srvDiscount_upgr2");
        upgrade(customerData.getAdminUser(), subscr, upgrService2,
                "2013-08-24 12:00:00");

        resetCutOffDay(supplierData.getAdminKey());
        cacheTestData(testName, new TestData(supplierData));
    }

    /**
     * Creates two customer subscriptions with different discounts for the same
     * service.
     */
    public void setupTwoCustomersWithDiscount(String testName,
            BigDecimal discount1, BigDecimal discount2, double operatorShare,
            double marketplaceShare) throws Exception {
        VendorData supplierData = setupNewSupplier("2013-01-01 08:00:00");
        setDateFactory("2013-07-10 20:00:00");
        setCutOffDay(supplierData.getAdminKey(), 1);

        CustomerData customer1Data = registerCustomerWithDiscount(supplierData,
                discount1,
                DateTimeHandling.calculateMillis("2013-01-01 08:00:00"),
                DateTimeHandling.calculateMillis("2014-01-01 08:00:00"));

        CustomerData customer2Data = registerCustomerWithDiscount(supplierData,
                discount2,
                DateTimeHandling.calculateMillis("2013-01-01 08:00:00"),
                DateTimeHandling.calculateMillis("2014-01-01 08:00:00"));

        VOServiceDetails service = createPublishActivateService(supplierData,
                TestPriceModel.EXAMPLE_RATA_WEEK_ROLES, "srvDiscount");
        updateOperatorRevenueShare(operatorShare, service.getKey());

        updateMarketplaceRevenueShare(marketplaceShare,
                supplierData.getMarketplaceId(0));

        // Two customers with different discounts subscribe to the same service
        subscribe(customer1Data.getAdminUser(), "srvDiscount_subscr1", service,
                "2013-08-01 12:00:00", "ADMIN");

        subscribe(customer2Data.getAdminUser(), "srvDiscount_subscr2", service,
                "2013-08-01 12:00:00", "ADMIN");

        resetCutOffDay(supplierData.getAdminKey());

        cacheTestData(testName, new TestData(supplierData));
    }

    /**
     * Creates two customer subscriptions with different discounts for the same
     * service. The subscriptions are terminated before billing period end.
     */
    public void setupTwoCustomersWithDiscount_Terminate(String testName,
            BigDecimal discount1, BigDecimal discount2, double operatorShare,
            double marketplaceShare) throws Exception {
        VendorData supplierData = setupNewSupplier("2013-01-01 08:00:00");
        setDateFactory("2013-07-10 20:00:00");
        setCutOffDay(supplierData.getAdminKey(), 1);

        CustomerData customer1Data = registerCustomerWithDiscount(supplierData,
                discount1,
                DateTimeHandling.calculateMillis("2013-01-01 08:00:00"),
                DateTimeHandling.calculateMillis("2014-01-01 08:00:00"));

        CustomerData customer2Data = registerCustomerWithDiscount(supplierData,
                discount2,
                DateTimeHandling.calculateMillis("2013-01-01 08:00:00"),
                DateTimeHandling.calculateMillis("2014-01-01 08:00:00"));

        VOServiceDetails service = createPublishActivateService(supplierData,
                TestPriceModel.EXAMPLE_RATA_WEEK_ROLES, "srvDiscount2");
        updateOperatorRevenueShare(operatorShare, service.getKey());

        updateMarketplaceRevenueShare(marketplaceShare,
                supplierData.getMarketplaceId(0));

        // Two customers with different discounts subscribe to the same service
        VOSubscriptionDetails sub1 = subscribe(customer1Data.getAdminUser(),
                "srvDiscount2_subscr1", service, "2013-08-01 06:00:00", "ADMIN");

        VOSubscriptionDetails sub2 = subscribe(customer2Data.getAdminUser(),
                "srvDiscount2_subscr2", service, "2013-08-01 06:00:00", "ADMIN");

        unsubscribe(customer1Data.getAdminKey(), sub1.getSubscriptionId(),
                "2013-08-31 18:00:00");
        unsubscribe(customer2Data.getAdminKey(), sub2.getSubscriptionId(),
                "2013-08-31 18:00:00");

        resetCutOffDay(supplierData.getAdminKey());

        cacheTestData(testName, new TestData(supplierData));
    }

    /**
     * One customer subscribes to two services provided by two different
     * suppliers, for which he has two different discounts.
     */
    public void setupCustomerWithTwoDiscounts(String testName,
            BigDecimal discount1, BigDecimal discount2, double operatorShare1,
            double operatorShare2, double marketplaceShare1,
            double marketplaceShare2) throws Exception {

        VendorData supplierData = setupNewSupplier("2013-01-01 08:00:00");
        VendorData supplierData2 = setupNewSupplier("2013-01-01 08:00:00");
        setCutOffDay(supplierData.getAdminKey(), 1);
        setCutOffDay(supplierData2.getAdminKey(), 1);

        setDateFactory("2013-01-02 20:00:00");

        VOServiceDetails service1 = createPublishActivateService(supplierData,
                TestPriceModel.EXAMPLE_RATA_DAY_ROLES, "srv1");

        VOServiceDetails service2 = createPublishActivateService(supplierData2,
                TestPriceModel.EXAMPLE_RATA_DAY_ROLES, "srv2");

        // Update the operator shares of the two different services.
        updateOperatorRevenueShare(operatorShare1, service1.getKey());
        updateOperatorRevenueShare(operatorShare2, service2.getKey());

        // Update the marketplace shares for the two different supplier
        // marketplaces.
        updateMarketplaceRevenueShare(marketplaceShare1,
                supplierData.getMarketplaceId(0));
        updateMarketplaceRevenueShare(marketplaceShare2,
                supplierData2.getMarketplaceId(0));

        CustomerData customerData = registerCustomerWithDiscount(supplierData,
                discount1,
                DateTimeHandling.calculateMillis("2013-01-01 08:00:00"),
                DateTimeHandling.calculateMillis("2014-01-01 08:00:00"));

        // A customer subscribes to two services
        // provided by two different suppliers
        subscribe(customerData.getAdminUser(), "srv1_subscr", service1,
                "2013-08-01 12:00:00", "ADMIN");

        subscribe(customerData.getAdminUser(), "srv2_subscr", service2,
                "2013-08-01 12:00:00", "ADMIN");

        updateCustomerDiscount(supplierData2.getAdminKey(), customerData,
                discount2,
                DateTimeHandling.calculateMillis("2013-01-01 08:00:00"),
                DateTimeHandling.calculateMillis("2014-01-01 08:00:00"));

        resetCutOffDay(supplierData.getAdminKey());
        resetCutOffDay(supplierData2.getAdminKey());
        cacheTestData(testName, new TestData(supplierData, supplierData2));
    }

    /**
     * Customer1 subscribes to service1 twice. First subscription is upgraded to
     * free service and downgraded again to original service in the same billing
     * period. Customer2 also subscribes to service1.
     */
    public void setupMultipleSubscriptionsUpDowngrade(String testName,
            double operatorShare, double marketplaceShare) throws Exception {
        VendorData supplierData = setupNewSupplier("2013-01-01 08:00:00",
                new BigDecimal(operatorShare));
        setCutOffDay(supplierData.getAdminKey(), 1);
        updateMarketplaceRevenueShare(marketplaceShare,
                supplierData.getMarketplaceId(0));

        CustomerData customer1 = registerCustomer(supplierData);
        CustomerData customer2 = registerCustomer(supplierData);

        VOServiceDetails service1 = createPublishActivateService(supplierData,
                TestPriceModel.EXAMPLE_RATA_WEEK_ROLES, "service1");

        VOSubscriptionDetails subscr1 = subscribe(customer1.getAdminUser(),
                "customer1_subscr1", service1, "2013-08-01 12:00:00", "ADMIN");

        subscribe(customer1.getAdminUser(), "customer1_subscr2", service1,
                "2013-08-02 12:00:00", "ADMIN");

        subscribe(customer2.getAdminUser(), "customer2_subscr", service1,
                "2013-08-03 12:00:00", "ADMIN");

        VOServiceDetails service2 = createPublishActivateService(supplierData,
                TestPriceModel.FREE, "service2");

        service1 = serviceSetup.registerCompatibleServices(
                supplierData.getAdminKey(), service1, service2);
        service2 = serviceSetup.registerCompatibleServices(
                supplierData.getAdminKey(), service2, service1);

        // Upgrade to new service
        subscr1 = upgrade(customer1.getAdminUser(), subscr1, service2,
                "2013-08-08 15:00:00");

        // Downgrade to original service
        upgrade(customer1.getAdminUser(), subscr1, service1,
                "2013-08-18 15:00:00");

        resetCutOffDay(supplierData.getAdminKey());
        cacheTestData(testName, new TestData(supplierData));
    }

}
