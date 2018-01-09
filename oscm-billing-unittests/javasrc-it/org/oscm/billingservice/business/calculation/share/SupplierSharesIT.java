/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 03.09.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.calculation.share;

import java.math.BigDecimal;

import org.junit.BeforeClass;
import org.junit.Test;

import org.oscm.billingservice.business.calculation.share.setup.SupplierSharesSetup;
import org.oscm.billingservice.evaluation.SupplierShareResultEvaluator;
import org.oscm.billingservice.setup.BillingIntegrationTestBase;
import org.oscm.billingservice.setup.CustomerData;
import org.oscm.billingservice.setup.TestData;
import org.oscm.billingservice.setup.VendorData;
import org.oscm.internal.vo.VOServiceDetails;

/**
 * @author kulle
 * 
 */
public class SupplierSharesIT extends BillingIntegrationTestBase {

    private SupplierSharesSetup testSetup = new SupplierSharesSetup();

    @BeforeClass
    public static void setupOnce() throws Exception {
        BillingIntegrationTestBase.initialize();
        BillingIntegrationTestBase.createBasicTestData(true);
    }

    /**
     * One subscription only with customer discount of 50%
     */

    @Test
    public void testSupplierShareResultForCustomerDiscount() throws Exception {
        // given
        testSetup.setupCustomerDiscount("testShareResultForCustomerDiscount",
                new BigDecimal("50.00"), 2.0D);
        TestData testData = getTestData("testShareResultForCustomerDiscount");
        VendorData supplier = testData.getVendor(0);
        VOServiceDetails srvDiscount = supplier.getService(0);
        String mpId = supplier.getMarketplaceId(srvDiscount);
        CustomerData customer = supplier.getCustomer(0);

        // when
        performBillingRun(0, "2013-09-10 00:00:00");

        // then
        SupplierShareResultEvaluator eva = newSupplierShareResultEvaluator(
                supplier.getOrganizationKey(), "2013-08-01 00:00:00",
                "2013-09-01 00:00:00");

        eva.assertSupplierRevenue("2630.95");
        eva.assertDirectRevenue("2684.64", "0.00", "53.69");
        eva.assertRevenuePerMarketplace(mpId, "2684.64", "0.00", "53.69",
                "0.00", "0.00", "2630.95");

        eva.assertRevenueShareDetails(mpId, srvDiscount, "2684.64", "0.00",
                "0.00", "2.00", "53.69", "2630.95");

        eva.assertSubscription(mpId, "srvDiscount_subscr", srvDiscount,
                "2684.64");

        eva.assertCustomerRevenueShareDetails(mpId, srvDiscount, customer
                .getOrganization().getOrganizationId(), "2684.64", "0.00",
                "53.69", "2630.95");
    }

    @Test
    public void testUpgradeForCustomerDiscount() throws Exception {
        // given
        testSetup
                .setupUpgradeWithCustomerDiscount(
                        "testUpgradeForCustomerDiscount", new BigDecimal(
                                "50.00"), 2.0D);
        TestData testData = getTestData("testUpgradeForCustomerDiscount");
        VendorData supplier = testData.getVendor(0);
        VOServiceDetails srvDiscount = supplier.getService(0);
        VOServiceDetails srvDiscountUpgr = supplier.getService(1);
        String mpId = supplier.getMarketplaceId(0);
        CustomerData customer = supplier.getCustomer(0);

        // when
        performBillingRun(0, "2013-09-10 00:00:00");

        // then
        SupplierShareResultEvaluator eva = newSupplierShareResultEvaluator(
                supplier.getOrganizationKey(), "2013-08-01 00:00:00",
                "2013-09-01 00:00:00");

        eva.assertSupplierRevenue("1268.21");
        eva.assertDirectRevenue("1294.09", "0.00", "25.88");
        eva.assertRevenuePerMarketplace(mpId, "1294.09", "0.00", "25.88",
                "0.00", "0.00", "1268.21");

        eva.assertSubscription(mpId, "srvDiscount_subscr", srvDiscount,
                "724.48");
        eva.assertRevenueShareDetails(mpId, srvDiscount, "724.48", "0.00",
                "0.00", "2.00", "14.49", "709.99");

        eva.assertCustomerRevenueShareDetails(mpId, srvDiscount, customer
                .getOrganization().getOrganizationId(), "724.48", "0.00",
                "14.49", "709.99");

        eva.assertSubscription(mpId, "srvDiscount_subscr", srvDiscountUpgr,
                "569.61");
        eva.assertRevenueShareDetails(mpId, srvDiscountUpgr, "569.61", "0.00",
                "0.00", "2.00", "11.39", "558.22");
        eva.assertCustomerRevenueShareDetails(mpId, srvDiscountUpgr, customer
                .getOrganization().getOrganizationId(), "569.61", "0.00",
                "11.39", "558.22");
    }

    /**
     * One subscription with upgrade and following downgrade. Customer has a
     * discount.
     */
    @Test
    public void testUpDowngradeWithCustomerDiscount() throws Exception {
        // given
        testSetup
                .setupUpDowngradeWithCustomerDiscount(
                        "testUpgradeForCustomerDiscount", new BigDecimal(
                                "50.00"), 2.0D);
        TestData testData = getTestData("testUpgradeForCustomerDiscount");
        VendorData supplier = testData.getVendor(0);
        VOServiceDetails srvDiscount = supplier.getService(0);
        VOServiceDetails srvDiscountUpgr = supplier.getService(1);
        String mpId = supplier.getMarketplaceId(0);
        CustomerData customer = supplier.getCustomer(0);

        // when
        performBillingRun(0, "2013-09-10 00:00:00");

        // then
        SupplierShareResultEvaluator eva = newSupplierShareResultEvaluator(
                supplier.getOrganizationKey(), "2013-08-01 00:00:00",
                "2013-09-01 00:00:00");

        eva.assertSupplierRevenue("2243.62");
        eva.assertDirectRevenue("2289.41", "0.00", "45.79");
        eva.assertRevenuePerMarketplace(mpId, "2289.41", "0.00", "45.79",
                "0.00", "0.00", "2243.62");

        eva.assertSubscription(mpId, "srvDiscount_subscr", srvDiscount,
                "1973.06");
        eva.assertRevenueShareDetails(mpId, srvDiscount, "1973.06", "0.00",
                "0.00", "2.00", "39.46", "1933.60");
        eva.assertCustomerRevenueShareDetails(mpId, srvDiscount, customer
                .getOrganization().getOrganizationId(), "1973.06", "0.00",
                "39.46", "1933.60");

        eva.assertSubscription(mpId, "srvDiscount_subscr", srvDiscountUpgr,
                "316.35");
        eva.assertRevenueShareDetails(mpId, srvDiscountUpgr, "316.35", "0.00",
                "0.00", "2.00", "6.33", "310.02");
        eva.assertCustomerRevenueShareDetails(mpId, srvDiscountUpgr, customer
                .getOrganization().getOrganizationId(), "316.35", "0.00",
                "6.33", "310.02");
    }

    /**
     * Test the supplier revenue for services of two different marketplaces with
     * 0% marketplace revenue shares
     */

    @Test
    public void testSupplierRevenueForTwoMarketplaces_ZeroMplShares()
            throws Exception {
        // given
        testSetup.subscribeToServicesOfTwoMarketplaces(
                "testSupplierRevenueForTwoMarketplaces",
                new BigDecimal("50.00"), 2.0D, 2.0D, 0.0D, 0.0D);
        TestData testData = getTestData("testSupplierRevenueForTwoMarketplaces");
        VendorData supplier = testData.getVendor(0);
        CustomerData customer = supplier.getCustomer(0);

        VOServiceDetails service1 = supplier.getService(0);
        VOServiceDetails service2 = supplier.getService(1);

        String mpId1 = supplier.getMarketplaceId(service1);
        String mpId2 = supplier.getMarketplaceId(service2);

        // when
        performBillingRun(0, "2013-09-10 00:00:00");

        // then
        SupplierShareResultEvaluator eva = newSupplierShareResultEvaluator(
                supplier.getOrganizationKey(), "2013-08-01 00:00:00",
                "2013-09-01 00:00:00");

        eva.assertSupplierRevenue("5261.90");
        eva.assertDirectRevenue("5369.28", "0.00", "107.38");

        eva.assertRevenuePerMarketplace(mpId1, "2684.64", "0.00", "53.69",
                "0.00", "0.00", "2630.95");
        eva.assertRevenueShareDetails(mpId1, service1, "2684.64", "0.00",
                "0.00", "2.00", "53.69", "2630.95");
        eva.assertSubscription(mpId1, "srv1_subscr", service1, "2684.64");
        eva.assertCustomerRevenueShareDetails(mpId1, service1, customer
                .getOrganization().getOrganizationId(), "2684.64", "0.00",
                "53.69", "2630.95");

        eva.assertRevenuePerMarketplace(mpId2, "2684.64", "0.00", "53.69",
                "0.00", "0.00", "2630.95");
        eva.assertRevenueShareDetails(mpId2, service2, "2684.64", "0.00",
                "0.00", "2.00", "53.69", "2630.95");
        eva.assertSubscription(mpId2, "srv2_subscr", service2, "2684.64");
        eva.assertCustomerRevenueShareDetails(mpId2, service2, customer
                .getOrganization().getOrganizationId(), "2684.64", "0.00",
                "53.69", "2630.95");
    }

    @Test
    public void testSupplierRevenueForTwoMarketplaces() throws Exception {
        // given
        testSetup.subscribeToServicesOfTwoMarketplaces(
                "testSupplierRevenueForTwoMarketplaces",
                new BigDecimal("50.00"), 2.0D, 4.0D, 10.0D, 20.0D);
        TestData testData = getTestData("testSupplierRevenueForTwoMarketplaces");
        VendorData supplier = testData.getVendor(0);
        CustomerData customer = supplier.getCustomer(0);

        VOServiceDetails service1 = supplier.getService(0);
        VOServiceDetails service2 = supplier.getService(1);

        String mpId1 = supplier.getMarketplaceId(service1);
        String mpId2 = supplier.getMarketplaceId(service2);

        // when
        performBillingRun(0, "2013-09-10 00:00:00");

        // then
        SupplierShareResultEvaluator eva = newSupplierShareResultEvaluator(
                supplier.getOrganizationKey(), "2013-08-01 00:00:00",
                "2013-09-01 00:00:00");

        eva.assertSupplierRevenue("4402.81");
        eva.assertDirectRevenue("5369.28", "805.39", "161.08");

        eva.assertRevenuePerMarketplace(mpId1, "2684.64", "268.46", "53.69",
                "0.00", "0.00", "2362.49");
        eva.assertRevenueShareDetails(mpId1, service1, "2684.64", "10.00",
                "268.46", "2.00", "53.69", "2362.49");
        eva.assertSubscription(mpId1, "srv1_subscr", service1, "2684.64");
        eva.assertCustomerRevenueShareDetails(mpId1, service1, customer
                .getOrganization().getOrganizationId(), "2684.64", "268.46",
                "53.69", "2362.49");

        eva.assertRevenuePerMarketplace(mpId2, "2684.64", "536.93", "107.39",
                "0.00", "0.00", "2040.32");
        eva.assertRevenueShareDetails(mpId2, service2, "2684.64", "20.00",
                "536.93", "4.00", "107.39", "2040.32");
        eva.assertSubscription(mpId2, "srv2_subscr", service2, "2684.64");
        eva.assertCustomerRevenueShareDetails(mpId2, service2, customer
                .getOrganization().getOrganizationId(), "2684.64", "536.93",
                "107.39", "2040.32");
    }

    @Test
    public void oneUpgrade_operatorShare() throws Exception {
        // given
        testSetup.oneUpgrade();
        TestData testData = getTestData("oneUpgrade");
        VendorData supplier = testData.getVendor(0);
        VOServiceDetails oneUpgrade = supplier.getService(0);
        VOServiceDetails oneUpgradeUpgr = supplier.getService(1);
        String mpId = supplier.getMarketplaceId(0);

        // when
        performBillingRun(0, "2013-09-10 00:00:00");

        // then
        SupplierShareResultEvaluator eva = newSupplierShareResultEvaluator(
                supplier.getOrganizationKey(), "2013-08-01 00:00:00",
                "2013-09-01 00:00:00");

        eva.assertSubscription(mpId, "oneUpgrade", oneUpgrade, "1651.10");
        eva.assertSubscription(mpId, "oneUpgrade", oneUpgradeUpgr, "1471.76");

        eva.assertSupplierRevenue("2736.99");
    }

    /**
     * Creates two subscriptions based on different services. One subscription
     * gets upgraded.
     */
    @Test
    public void test1() throws Exception {
        // given
        testSetup.test1();
        TestData testData = getTestData("test1");
        VendorData supplier = testData.getVendor(0);
        VOServiceDetails srv1 = supplier.getService(0);
        VOServiceDetails srv2 = supplier.getService(1);
        VOServiceDetails srv1Upgr = supplier.getService(2);
        String mpId = supplier.getMarketplaceId(0);

        // when
        performBillingRun(0, "2013-09-10 00:00:00");

        // then
        SupplierShareResultEvaluator eva = newSupplierShareResultEvaluator(
                supplier.getOrganizationKey(), "2013-08-01 00:00:00",
                "2013-09-01 00:00:00");

        eva.assertSubscription(mpId, "subscr1", srv1, "1448.97");
        eva.assertSubscription(mpId, "subscr1", srv1Upgr, "1139.23");
        eva.assertSubscription(mpId, "subscr2", srv2, "4502.76");
    }

    /**
     * Creates 3 services: srv1, srv2, srv2_upgr. Customer1 subscribes to srv1
     * and srv2, customer2 subscribes to srv1 only.
     */
    @Test
    public void test2() throws Exception {
        // given
        testSetup.test2();
        TestData testData = getTestData("test2");
        VendorData supplier = testData.getVendor(0);
        VOServiceDetails srv1 = supplier.getService(0);
        VOServiceDetails srv2 = supplier.getService(1);
        VOServiceDetails srv2Upgr = supplier.getService(2);
        String mpId = supplier.getMarketplaceId(0);

        // when
        performBillingRun(0, "2013-09-10 00:00:00");

        // then
        SupplierShareResultEvaluator eva = newSupplierShareResultEvaluator(
                supplier.getOrganizationKey(), "2013-08-01 00:00:00",
                "2013-09-01 00:00:00");

        eva.assertSubscription(mpId, "srv1_subscr1", srv1, "5369.28");
        eva.assertSubscription(mpId, "srv1_subscr2", srv1, "5201.58");
        eva.assertSubscription(mpId, "srv2_subscr1", srv2, "582.45");
        eva.assertSubscription(mpId, "srv2_subscr1", srv2Upgr, "1139.23");
    }

    /**
     * Customer 1 subscribes 2 times to srv1 and to srv2. Second subscription is
     * upgraded to srv2_upgr.
     */
    @Test
    public void test3() throws Exception {
        // given
        testSetup.test3();
        TestData testData = getTestData("test3");
        VendorData supplier = testData.getVendor(0);
        CustomerData customer = supplier.getCustomer(0);
        VOServiceDetails srv1 = supplier.getService(0);
        VOServiceDetails srv2 = supplier.getService(1);
        VOServiceDetails srv2Upgr = supplier.getService(2);
        String mpId = supplier.getMarketplaceId(0);

        // when
        performBillingRun(0, "2013-09-10 00:00:00");

        // then
        SupplierShareResultEvaluator eva = newSupplierShareResultEvaluator(
                supplier.getOrganizationKey(), "2013-08-01 00:00:00",
                "2013-09-01 00:00:00");

        eva.assertSubscription(mpId, "srv1_subscr1", srv1, "5369.28");
        eva.assertSubscription(mpId, "srv1_subscr2", srv1, "5201.58");
        eva.assertRevenueShareDetails(mpId, srv1, "10570.86", "0.00", "0.00",
                "10.00", "1057.09", "9513.77");
        eva.assertCustomerRevenueShareDetails(mpId, srv1, customer
                .getOrganization().getOrganizationId(), "10570.86", "0.00",
                "1057.09", "9513.77");

        eva.assertSubscription(mpId, "srv2_subscr1", srv2, "582.45");
        eva.assertRevenueShareDetails(mpId, srv2, "582.45", "0.00", "0.00",
                "20.00", "116.49", "465.96");
        eva.assertCustomerRevenueShareDetails(mpId, srv2, customer
                .getOrganization().getOrganizationId(), "582.45", "0.00",
                "116.49", "465.96");

        eva.assertSubscription(mpId, "srv2_subscr1", srv2Upgr, "1139.23");
        eva.assertRevenueShareDetails(mpId, srv2Upgr, "1139.23", "0.00",
                "0.00", "15.00", "170.88", "968.35");
        eva.assertCustomerRevenueShareDetails(mpId, srv2Upgr, customer
                .getOrganization().getOrganizationId(), "1139.23", "0.00",
                "170.88", "968.35");
    }
}
