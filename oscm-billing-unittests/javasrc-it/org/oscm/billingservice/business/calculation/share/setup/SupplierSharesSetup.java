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
 * @author kulle
 * 
 */
public class SupplierSharesSetup extends SharesSetup {

    /**
     * Creates test data for test case: oneUpgrade
     */
    public void oneUpgrade() throws Exception {
        VendorData supplierData = setupNewSupplier("2013-01-01 08:00:00");
        setDateFactory("2013-07-10 20:00:00");
        setCutOffDay(supplierData.getAdminKey(), 1);
        CustomerData customerData = registerCustomer(supplierData);

        VOServiceDetails service = createPublishActivateService(supplierData,
                TestPriceModel.EXAMPLE_RATA_WEEK_ROLES, "oneUpgrade");
        updateOperatorRevenueShare(10.0D, service.getKey());

        VOSubscriptionDetails subscr = subscribe(customerData.getAdminUser(),
                "oneUpgrade", service, "2013-08-01 12:00:00", "ADMIN");
        subscr = addSecondCustomerUser(subscr, service, "2013-08-05 10:00:00");

        VOServiceDetails upgrService = createAndRegisterCompatibleService(
                supplierData, TestPriceModel.EXAMPLE_PRORATA_MONTH_ROLES,
                service, "oneUpgrade_upgr");
        updateOperatorRevenueShare(15.0D, upgrService.getKey());
        upgrade(customerData.getAdminUser(), subscr, upgrService,
                "2013-08-08 15:00:00");

        resetCutOffDay(supplierData.getAdminKey());
        cacheTestData("oneUpgrade", new TestData(supplierData));
    }

    /**
     * Creates a subscription for a customer with a discount
     */
    public void setupCustomerDiscount(String testName, BigDecimal discount,
            double operatorShare) throws Exception {
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

        resetCutOffDay(supplierData.getAdminKey());
        cacheTestData(testName, new TestData(supplierData));
    }

    /**
     * One subscription with upgrade, Customer discount and following
     * shares:</br> Operator Share: and</br> Marketplace Share: 0%</br>
     */
    public void setupUpgradeWithCustomerDiscount(String testName,
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
                TestPriceModel.EXAMPLE_RATA_WEEK_ROLES, "srvDiscount");

        VOSubscriptionDetails subscr = subscribe(customerData.getAdminUser(),
                "srvDiscount_subscr", service, "2013-08-01 12:00:00", "ADMIN");

        VOServiceDetails upgrService = createAndRegisterCompatibleService(
                supplierData, TestPriceModel.EXAMPLE_PRORATA_MONTH_ROLES,
                service, "srvDiscount" + "_upgr");
        upgrade(customerData.getAdminUser(), subscr, upgrService,
                "2013-08-08 15:00:00");

        resetCutOffDay(supplierData.getAdminKey());
        cacheTestData(testName, new TestData(supplierData));
    }

    /**
     * One subscription with upgrade and following downgrade, Customer discount
     * and following shares:</br> Operator Share: and</br> Marketplace Share:
     * 0%</br>
     */
    public void setupUpDowngradeWithCustomerDiscount(String testName,
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
                TestPriceModel.EXAMPLE_RATA_WEEK_ROLES, "srvDiscount");

        VOSubscriptionDetails subscr = subscribe(customerData.getAdminUser(),
                "srvDiscount_subscr", service, "2013-08-01 12:00:00", "ADMIN");

        VOServiceDetails upgrService = createPublishActivateService(
                supplierData, TestPriceModel.EXAMPLE_PRORATA_MONTH_ROLES,
                "srvDiscount" + "_upgr");

        service = serviceSetup.registerCompatibleServices(
                supplierData.getAdminKey(), service, upgrService);
        upgrService = serviceSetup.registerCompatibleServices(
                supplierData.getAdminKey(), upgrService, service);

        // Upgrade to new service
        VOSubscriptionDetails upgradedSubscr = upgrade(
                customerData.getAdminUser(), subscr, upgrService,
                "2013-08-08 15:00:00");

        // Downgrade to original service
        upgrade(customerData.getAdminUser(), upgradedSubscr, service,
                "2013-08-18 15:00:00");

        resetCutOffDay(supplierData.getAdminKey());
        cacheTestData(testName, new TestData(supplierData));
    }

    /**
     * Creates two subscriptions based on different services. One subscription
     * gets upgraded.
     */
    public void test1() throws Exception {
        VendorData supplierData = setupNewSupplier("2013-01-01 08:00:00");
        setDateFactory("2013-07-10 20:00:00");
        setCutOffDay(supplierData.getAdminKey(), 1);
        CustomerData customerData = registerCustomer(supplierData);

        VOServiceDetails srv1 = createPublishActivateService(supplierData,
                TestPriceModel.EXAMPLE_RATA_WEEK_ROLES, "srv1");
        VOServiceDetails srv2 = createPublishActivateService(supplierData,
                TestPriceModel.EXAMPLE_RATA_WEEK_ROLES, "srv2");

        updateOperatorRevenueShare(10.0D, srv1.getKey());
        updateOperatorRevenueShare(20.0D, srv2.getKey());

        VOSubscriptionDetails subscr = subscribe(customerData.getAdminUser(),
                "subscr1", srv1, "2013-08-01 12:00:00", "ADMIN");
        subscribe(customerData.getAdminUser(), "subscr2", srv2,
                "2013-08-06 16:00:00", "ADMIN");

        VOServiceDetails upgrService = createAndRegisterCompatibleService(
                supplierData, TestPriceModel.EXAMPLE_PRORATA_MONTH_ROLES, srv1,
                "srv1_upgr");
        updateOperatorRevenueShare(15.0D, upgrService.getKey());
        upgrade(customerData.getAdminUser(), subscr, upgrService,
                "2013-08-08 15:00:00");

        resetCutOffDay(supplierData.getAdminKey());
        cacheTestData("test1", new TestData(supplierData));
    }

    /**
     * Creates 3 services: srv1, srv2, srv2_upgr. Customer1 subscribes to srv1
     * and srv2, customer2 subscribes to srv1 only.
     */
    public void test2() throws Exception {
        VendorData supplierData = setupNewSupplier("2013-01-01 08:00:00");
        setDateFactory("2013-07-10 20:00:00");
        setCutOffDay(supplierData.getAdminKey(), 1);
        CustomerData customerData1 = registerCustomer(supplierData);
        CustomerData customerData2 = registerCustomer(supplierData);

        VOServiceDetails srv1 = createPublishActivateService(supplierData,
                TestPriceModel.EXAMPLE_RATA_WEEK_ROLES, "srv1");
        VOServiceDetails srv2 = createPublishActivateService(supplierData,
                TestPriceModel.EXAMPLE_RATA_WEEK_ROLES, "srv2");

        updateOperatorRevenueShare(10.0D, srv1.getKey());
        updateOperatorRevenueShare(20.0D, srv2.getKey());

        subscribe(customerData1.getAdminUser(), "srv1_subscr1", srv1,
                "2013-08-01 12:00:00", "ADMIN");
        subscribe(customerData2.getAdminUser(), "srv1_subscr2", srv1,
                "2013-08-02 12:00:00", "ADMIN");
        VOSubscriptionDetails subscr3 = subscribe(customerData1.getAdminUser(),
                "srv2_subscr1", srv2, "2013-08-06 16:00:00", "ADMIN");

        VOServiceDetails upgrService = createAndRegisterCompatibleService(
                supplierData, TestPriceModel.EXAMPLE_PRORATA_MONTH_ROLES, srv2,
                "srv2" + "_upgr");
        updateOperatorRevenueShare(15.0D, upgrService.getKey());
        upgrade(customerData1.getAdminUser(), subscr3, upgrService,
                "2013-08-08 15:00:00");

        resetCutOffDay(supplierData.getAdminKey());
        cacheTestData("test2", new TestData(supplierData));
    }

    /**
     * Customer 1 subscribes 2 times to srv1 and to srv2. Second subscription is
     * upgraded to srv2_upgr.
     */
    public void test3() throws Exception {
        VendorData supplierData = setupNewSupplier("2013-01-01 08:00:00");
        setDateFactory("2013-07-10 20:00:00");
        setCutOffDay(supplierData.getAdminKey(), 1);
        CustomerData customerData1 = registerCustomer(supplierData);

        VOServiceDetails srv1 = createPublishActivateService(supplierData,
                TestPriceModel.EXAMPLE_RATA_WEEK_ROLES, "srv1");
        VOServiceDetails srv2 = createPublishActivateService(supplierData,
                TestPriceModel.EXAMPLE_RATA_WEEK_ROLES, "srv2");

        updateOperatorRevenueShare(10.0D, srv1.getKey());
        updateOperatorRevenueShare(20.0D, srv2.getKey());

        subscribe(customerData1.getAdminUser(), "srv1_subscr1", srv1,
                "2013-08-01 12:00:00", "ADMIN");
        subscribe(customerData1.getAdminUser(), "srv1_subscr2", srv1,
                "2013-08-02 12:00:00", "ADMIN");
        VOSubscriptionDetails subscr3 = subscribe(customerData1.getAdminUser(),
                "srv2_subscr1", srv2, "2013-08-06 16:00:00", "ADMIN");

        VOServiceDetails upgrService = createAndRegisterCompatibleService(
                supplierData, TestPriceModel.EXAMPLE_PRORATA_MONTH_ROLES, srv2,
                "srv2" + "_upgr");
        updateOperatorRevenueShare(15.0D, upgrService.getKey());
        upgrade(customerData1.getAdminUser(), subscr3, upgrService,
                "2013-08-08 15:00:00");

        resetCutOffDay(supplierData.getAdminKey());
        cacheTestData("test3", new TestData(supplierData));
    }

    /**
     * Subscribe to two services which as published in two different
     * marketplaces
     */
    public void subscribeToServicesOfTwoMarketplaces(String testName,
            BigDecimal discount, double operatorShare1, double operatorShare2,
            double marketplaceShare1, double marketplaceShare2)
            throws Exception {
        VendorData supplierData = setupNewSupplier("2013-01-01 08:00:00");
        // create a second marketplace for the supplier
        createMarketplace(supplierData);

        setDateFactory("2013-07-10 20:00:00");
        setCutOffDay(supplierData.getAdminKey(), 1);
        CustomerData customerData = registerCustomerWithDiscount(supplierData,
                discount,
                DateTimeHandling.calculateMillis("2013-01-01 08:00:00"),
                DateTimeHandling.calculateMillis("2014-01-01 08:00:00"));

        VOServiceDetails service1 = createPublishActivateService(supplierData,
                TestPriceModel.EXAMPLE_RATA_WEEK_ROLES, "srv1",
                supplierData.getMarketplace(0));

        VOServiceDetails service2 = createPublishActivateService(supplierData,
                TestPriceModel.EXAMPLE_RATA_WEEK_ROLES, "srv2",
                supplierData.getMarketplace(1));

        updateMarketplaceRevenueShare(marketplaceShare1,
                supplierData.getMarketplaceId(0));
        updateMarketplaceRevenueShare(marketplaceShare2,
                supplierData.getMarketplaceId(1));

        updateOperatorRevenueShare(operatorShare1, service1.getKey());
        updateOperatorRevenueShare(operatorShare2, service2.getKey());

        subscribe(customerData.getAdminUser(), "srv1_subscr", service1,
                "2013-08-01 12:00:00", "ADMIN");

        subscribe(customerData.getAdminUser(), "srv2_subscr", service2,
                "2013-08-01 12:00:00", "ADMIN");

        resetCutOffDay(supplierData.getAdminKey());
        cacheTestData(testName, new TestData(supplierData));
    }

}
