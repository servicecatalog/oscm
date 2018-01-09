/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 20.09.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.calculation.share.setup;

import java.math.BigDecimal;

import org.oscm.billingservice.setup.CustomerData;
import org.oscm.billingservice.setup.TestData;
import org.oscm.billingservice.setup.VOPriceModelFactory.TestPriceModel;
import org.oscm.billingservice.setup.VendorData;
import org.oscm.test.DateTimeHandling;
import org.oscm.internal.vo.VOMarketplace;
import org.oscm.internal.vo.VOServiceDetails;
import org.oscm.internal.vo.VOSubscriptionDetails;

/**
 * @author kulle
 * 
 */
public class MarketplaceSharesSetup extends SharesSetup {

    /**
     * Creates test data for test case: setup customer discount of 50% (for one
     * subscription with Operator Share: 2%, Marketplace Share: 10%)
     */
    public void setupSubscriptionWithCustomerDiscount(String testName,
            BigDecimal discount, double operatorShare, double marketplaceShare)
            throws Exception {
        VendorData supplierData = setupNewSupplier("2013-01-01 08:00:00",
                new BigDecimal(operatorShare));
        VOMarketplace marketplace = supplierData.getMarketplace(0);

        setDateFactory("2013-07-10 20:00:00");
        setCutOffDay(supplierData.getAdminKey(), 1);

        CustomerData customer = registerCustomerWithDiscount(supplierData,
                discount,
                DateTimeHandling.calculateMillis("2013-01-01 08:00:00"),
                DateTimeHandling.calculateMillis("2014-01-01 08:00:00"));

        VOServiceDetails service = createPublishActivateService(supplierData,
                TestPriceModel.EXAMPLE_RATA_WEEK_ROLES, "srvDiscount");

        updateMarketplaceRevenueShare(marketplaceShare,
                marketplace.getMarketplaceId());

        subscribe(customer.getAdminUser(), "srvDiscount_subscr", service,
                "2013-08-01 12:00:00", "ADMIN");

        resetCutOffDay(supplierData.getAdminKey());
        cacheTestData(testName, new TestData(supplierData));
    }

    /**
     * One subscription with upgrade, Customer discount and following
     * shares:</br> Operator Share: and</br> Marketplace Share: 0%</br>
     */
    public void setupUpgradeWithCustomerDiscount(String testName,
            BigDecimal discount, double operatorShare, double marketplaceShare)
            throws Exception {
        VendorData supplierData = setupNewSupplier("2013-01-01 08:00:00",
                new BigDecimal(operatorShare));
        VOMarketplace marketplace = supplierData.getMarketplace(0);

        setDateFactory("2013-07-10 20:00:00");
        setCutOffDay(supplierData.getAdminKey(), 1);

        CustomerData customer = registerCustomerWithDiscount(supplierData,
                discount,
                DateTimeHandling.calculateMillis("2013-01-01 08:00:00"),
                DateTimeHandling.calculateMillis("2014-01-01 08:00:00"));
        VOServiceDetails service = createPublishActivateService(supplierData,
                TestPriceModel.EXAMPLE_RATA_WEEK_ROLES, "srvDiscount");

        updateMarketplaceRevenueShare(marketplaceShare,
                marketplace.getMarketplaceId());

        VOSubscriptionDetails subscr = subscribe(customer.getAdminUser(),
                "srvDiscount_subscr1", service, "2013-08-01 12:00:00", "ADMIN");

        VOServiceDetails upgrService = createAndRegisterCompatibleService(
                supplierData, TestPriceModel.EXAMPLE_PRORATA_MONTH_ROLES,
                service, "srvDiscount" + "_upgr");
        upgrade(customer.getAdminUser(), subscr, upgrService, "2013-08-08 15:00:00");

        resetCutOffDay(supplierData.getAdminKey());
        cacheTestData(testName, new TestData(supplierData));
    }

    /**
     * One subscription only with:</br> Operator Share: 2%</br> Marketplace
     * Share: 10%</br>
     */
    public void test1() throws Exception {
        VendorData supplierData = setupNewSupplier("2013-01-01 08:00:00",
                new BigDecimal(2.0D));
        VOMarketplace marketplace = supplierData.getMarketplace(0);
        setDateFactory("2013-07-10 20:00:00");
        setCutOffDay(supplierData.getAdminKey(), 1);
        CustomerData customer = registerCustomer(supplierData);
        VOServiceDetails service = createPublishActivateService(supplierData,
                TestPriceModel.EXAMPLE_RATA_WEEK_ROLES, "srv1");

        updateMarketplaceRevenueShare(10.0D, marketplace.getMarketplaceId());

        subscribe(customer.getAdminUser(), "srv1_subscr1", service,
                "2013-08-01 12:00:00", "ADMIN");

        resetCutOffDay(supplierData.getAdminKey());
        cacheTestData("test1", new TestData(supplierData));
    }

    /**
     * One subscription with upgrade and following shares:</br> Operator Share:
     * 2%</br> Marketplace Share: 10%</br>
     */
    public void test2() throws Exception {
        VendorData supplierData = setupNewSupplier("2013-01-01 08:00:00",
                new BigDecimal(2.0D));
        VOMarketplace marketplace = supplierData.getMarketplace(0);

        setDateFactory("2013-07-10 20:00:00");
        setCutOffDay(supplierData.getAdminKey(), 1);
        CustomerData customer = registerCustomer(supplierData);
        VOServiceDetails service = createPublishActivateService(supplierData,
                TestPriceModel.EXAMPLE_RATA_WEEK_ROLES, "srv1");

        updateMarketplaceRevenueShare(10.0D, marketplace.getMarketplaceId());

        VOSubscriptionDetails subscr = subscribe(customer.getAdminUser(),
                "srv1_subscr1", service, "2013-08-01 12:00:00", "ADMIN");

        VOServiceDetails upgrService = createAndRegisterCompatibleService(
                supplierData, TestPriceModel.EXAMPLE_PRORATA_MONTH_ROLES,
                service, "srv1" + "_upgr");
        upgrade(customer.getAdminUser(), subscr, upgrService, "2013-08-08 15:00:00");

        resetCutOffDay(supplierData.getAdminKey());
        cacheTestData("test2", new TestData(supplierData));
    }

    /**
     * One subscription with an upgrade, another subscription without upgrade.
     * Following shares:</br> Operator Share: 2%</br> Marketplace Share:
     * 10%</br>
     */
    public void test3() throws Exception {
        VendorData supplierData = setupNewSupplier("2013-01-01 08:00:00",
                new BigDecimal(2.0D));
        VOMarketplace marketplace = supplierData.getMarketplace(0);

        setDateFactory("2013-07-10 20:00:00");
        setCutOffDay(supplierData.getAdminKey(), 1);
        CustomerData customer = registerCustomer(supplierData);
        VOServiceDetails srv1 = createPublishActivateService(supplierData,
                TestPriceModel.EXAMPLE_RATA_WEEK_ROLES, "srv1");
        VOServiceDetails srv2 = createPublishActivateService(supplierData,
                TestPriceModel.EXAMPLE_RATA_WEEK_ROLES, "srv2");

        updateMarketplaceRevenueShare(10.0D, marketplace.getMarketplaceId());

        VOSubscriptionDetails subscr = subscribe(customer.getAdminUser(),
                "srv1_subscr1", srv1, "2013-08-01 12:00:00", "ADMIN");
        VOServiceDetails upgrSrv1 = createAndRegisterCompatibleService(
                supplierData, TestPriceModel.EXAMPLE_PRORATA_MONTH_ROLES, srv1,
                "srv1" + "_upgr");
        upgrade(customer.getAdminUser(), subscr, upgrSrv1, "2013-08-08 15:00:00");
        subscribe(customer.getAdminUser(), "srv2_subscr1", srv2,
                "2013-08-12 12:00:00", "ADMIN");

        resetCutOffDay(supplierData.getAdminKey());
        cacheTestData("test3", new TestData(supplierData));
    }

    /**
     * Two subscriptions, one on mp1 with 10% marketplace share, the other one
     * on mp2 with 30% marketplace share.
     */
    public void test4() throws Exception {
        VendorData supplierData = setupNewSupplier("2013-01-01 08:00:00",
                new BigDecimal(2.0D));
        VOMarketplace mp1 = supplierData.getMarketplace(0);
        VOMarketplace mp2 = createMarketplace(supplierData);

        setDateFactory("2013-07-10 20:00:00");
        setCutOffDay(supplierData.getAdminKey(), 1);
        CustomerData customer = registerCustomer(supplierData);
        VOServiceDetails srv1 = createPublishActivateService(supplierData,
                TestPriceModel.EXAMPLE_RATA_WEEK_ROLES, "srv1", mp1);
        VOServiceDetails srv2 = createPublishActivateService(supplierData,
                TestPriceModel.EXAMPLE_RATA_WEEK_ROLES, "srv2", mp2);

        updateMarketplaceRevenueShare(10.0D, mp1.getMarketplaceId());
        updateMarketplaceRevenueShare(30.0D, mp2.getMarketplaceId());

        subscribe(customer.getAdminUser(), "srv1_subscr1", srv1,
                "2013-08-01 12:00:00", "ADMIN");
        subscribe(customer.getAdminUser(), "srv2_subscr1", srv2,
                "2013-08-10 12:00:00", "ADMIN");

        resetCutOffDay(supplierData.getAdminKey());
        cacheTestData("test4", new TestData(supplierData));
    }

}
