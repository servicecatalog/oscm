/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 20.09.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.calculation.share;

import java.math.BigDecimal;

import org.junit.BeforeClass;
import org.junit.Test;

import org.oscm.billingservice.business.calculation.share.setup.MarketplaceSharesSetup;
import org.oscm.billingservice.evaluation.MarketplaceShareResultEvaluator;
import org.oscm.billingservice.setup.BillingIntegrationTestBase;
import org.oscm.billingservice.setup.TestData;
import org.oscm.billingservice.setup.VendorData;
import org.oscm.internal.vo.VOServiceDetails;

/**
 * @author kulle
 * 
 */
public class MarketplaceSharesIT extends BillingIntegrationTestBase {

    private MarketplaceSharesSetup testSetup = new MarketplaceSharesSetup();

    @BeforeClass
    public static void setupOnce() throws Exception {
        BillingIntegrationTestBase.initialize();
        BillingIntegrationTestBase.createBasicTestData(true);
    }

    /**
     * One subscription only with:</br> Operator Share: 2%</br> Marketplace
     * Share: 10%</br>
     */
    @Test
    public void test1() throws Exception {
        // given
        testSetup.test1();
        TestData testData = getTestData("test1");
        VendorData supplier = testData.getVendor(0);
        VOServiceDetails srv1 = supplier.getService(0);
        String mpId = supplier.getMarketplaceId(srv1);

        // when
        performBillingRun(0, "2013-09-10 00:00:00");

        // then
        MarketplaceShareResultEvaluator eva = newMarketplaceShareResultEvaluator(
                supplier.getOrganizationKey(), "2013-08-01 00:00:00",
                "2013-09-01 00:00:00");
        eva.assertRevenueShareDetails(mpId, srv1, "5369.28", "10.00", "536.93",
                "2.00", "107.39", "4724.96");
        eva.assertSuppliers(mpId, "4724.96", "5369.28", "536.93");
        eva.assertSuppliersOrganization(mpId, supplier.getOrganizationId(),
                "4724.96", "5369.28", "536.93");
        eva.assertBrokers(mpId, "0.00", "0.00", "0.00");
        eva.assertResellers(mpId, "0.00", "0.00", "0.00");
        eva.assertMarketplaceOwner(mpId, "536.93");
        eva.assertOverallSuppliers("4724.96", "5369.28", "536.93");
        eva.assertOverallSuppliersOrganization(supplier.getOrganizationId(),
                "4724.96", "5369.28", "536.93");
        eva.assertOverallBrokers("0.00", "0.00", "0.00");
        eva.assertOverallResellers("0.00", "0.00", "0.00");
    }

    /**
     * One subscription only with:</br> Customer Discount: 50%</br> Operator
     * Share: 0%</br> Marketplace Share: 0%</br>
     */
    @Test
    public void testMarketplaceShareResultForCustomerDiscount_ZeroShares()
            throws Exception {
        // given
        testSetup.setupSubscriptionWithCustomerDiscount(
                "testZeroMPShareCustomerDiscount", new BigDecimal("50.00"),
                0.0D, 0.0D);
        TestData testData = getTestData("testZeroMPShareCustomerDiscount");
        VendorData supplier = testData.getVendor(0);
        VOServiceDetails srvDiscount = supplier.getService(0);
        String mpId = supplier.getMarketplaceId(srvDiscount);

        // when
        performBillingRun(0, "2013-09-10 00:00:00");

        // then
        MarketplaceShareResultEvaluator eva = newMarketplaceShareResultEvaluator(
                supplier.getOrganizationKey(), "2013-08-01 00:00:00",
                "2013-09-01 00:00:00");

        eva.assertRevenueShareDetails(mpId, srvDiscount, "2684.64", "0.00",
                "0.00", "0.00", "0.00", "2684.64");
        eva.assertSuppliers(mpId, "2684.64", "2684.64", "0.00");
        eva.assertSuppliersOrganization(mpId, supplier.getOrganizationId(),
                "2684.64", "2684.64", "0.00");
        eva.assertBrokers(mpId, "0.00", "0.00", "0.00");
        eva.assertResellers(mpId, "0.00", "0.00", "0.00");
        eva.assertMarketplaceOwner(mpId, "0.00");
        eva.assertOverallSuppliers("2684.64", "2684.64", "0.00");
        eva.assertOverallSuppliersOrganization(supplier.getOrganizationId(),
                "2684.64", "2684.64", "0.00");
        eva.assertOverallBrokers("0.00", "0.00", "0.00");
        eva.assertOverallResellers("0.00", "0.00", "0.00");
    }

    /**
     * One subscription only with:</br> Customer Discount: 50% </br> Operator
     * Share: 2%</br> Marketplace Share: 10%</br>
     */
    @Test
    public void testMarketplaceShareResultForCustomerDiscount()
            throws Exception {
        // given
        testSetup.setupSubscriptionWithCustomerDiscount(
                "testMPShareCustomerDiscount", new BigDecimal("50.00"), 2.0D,
                10.0D);
        TestData testData = getTestData("testMPShareCustomerDiscount");
        VendorData supplier = testData.getVendor(0);
        VOServiceDetails srvDiscount = supplier.getService(0);
        String mpId = supplier.getMarketplaceId(srvDiscount);

        // when
        performBillingRun(0, "2013-09-10 00:00:00");

        // then
        MarketplaceShareResultEvaluator eva = newMarketplaceShareResultEvaluator(
                supplier.getOrganizationKey(), "2013-08-01 00:00:00",
                "2013-09-01 00:00:00");

        eva.assertRevenueShareDetails(mpId, srvDiscount, "2684.64", "10.00",
                "268.46", "2.00", "53.69", "2362.49");
        eva.assertSuppliers(mpId, "2362.49", "2684.64", "268.46");
        eva.assertSuppliersOrganization(mpId, supplier.getOrganizationId(),
                "2362.49", "2684.64", "268.46");
        eva.assertBrokers(mpId, "0.00", "0.00", "0.00");
        eva.assertResellers(mpId, "0.00", "0.00", "0.00");
        eva.assertMarketplaceOwner(mpId, "268.46");
        eva.assertOverallSuppliers("2362.49", "2684.64", "268.46");
        eva.assertOverallSuppliersOrganization(supplier.getOrganizationId(),
                "2362.49", "2684.64", "268.46");
        eva.assertOverallBrokers("0.00", "0.00", "0.00");
        eva.assertOverallResellers("0.00", "0.00", "0.00");
    }

    /**
     * One subscription with upgrade with Customer Discount of 50% and zero
     * shares.
     */
    @Test
    public void testUpgradeForCustomerDiscount_ZeroShares() throws Exception {
        // given
        testSetup.setupUpgradeWithCustomerDiscount(
                "testZeroMPShareUpgradeWithCustomerDiscount", new BigDecimal(
                        "50.00"), 0.0D, 0.0D);
        TestData testData = getTestData("testZeroMPShareUpgradeWithCustomerDiscount");
        VendorData supplier = testData.getVendor(0);
        VOServiceDetails srvDiscount = supplier.getService(0);
        VOServiceDetails srvDiscountUpgr = supplier.getService(1);
        String mpId = supplier.getMarketplaceId(0);

        // when
        performBillingRun(0, "2013-09-10 00:00:00");

        // then
        MarketplaceShareResultEvaluator eva = newMarketplaceShareResultEvaluator(
                supplier.getOrganizationKey(), "2013-08-01 00:00:00",
                "2013-09-01 00:00:00");

        eva.assertRevenueShareDetails(mpId, srvDiscountUpgr, "569.61", "0.00",
                "0.00", "0.00", "0.00", "569.61");
        eva.assertRevenueShareDetails(mpId, srvDiscount, "724.48", "0.00",
                "0.00", "0.00", "0.00", "724.48");
        eva.assertSuppliers(mpId, "1294.09", "1294.09", "0.00");
        eva.assertSuppliersOrganization(mpId, supplier.getOrganizationId(),
                "1294.09", "1294.09", "0.00");
        eva.assertBrokers(mpId, "0.00", "0.00", "0.00");
        eva.assertResellers(mpId, "0.00", "0.00", "0.00");
        eva.assertMarketplaceOwner(mpId, "0.00");
        eva.assertOverallSuppliers("1294.09", "1294.09", "0.00");
        eva.assertOverallSuppliersOrganization(supplier.getOrganizationId(),
                "1294.09", "1294.09", "0.00");
        eva.assertOverallBrokers("0.00", "0.00", "0.00");
        eva.assertOverallResellers("0.00", "0.00", "0.00");
    }

    /**
     * One subscription with upgrade with:</br> Customer Discount: 50% </br>
     * Operator Share: 2%</br> Marketplace Share: 10%</br>
     */
    @Test
    public void testUpgradeForCustomerDiscount() throws Exception {
        // given

        testSetup.setupUpgradeWithCustomerDiscount(
                "testUpgradeForCustomerDiscount", new BigDecimal("50.00"),
                2.0D, 10.0D);
        TestData testData = getTestData("testUpgradeForCustomerDiscount");
        VendorData supplier = testData.getVendor(0);
        VOServiceDetails srvDiscount = supplier.getService(0);
        VOServiceDetails srvDiscountUpgr = supplier.getService(1);
        String mpId = supplier.getMarketplaceId(0);

        // when
        performBillingRun(0, "2013-09-10 00:00:00");

        // then
        MarketplaceShareResultEvaluator eva = newMarketplaceShareResultEvaluator(
                supplier.getOrganizationKey(), "2013-08-01 00:00:00",
                "2013-09-01 00:00:00");
        eva.assertRevenueShareDetails(mpId, srvDiscountUpgr, "569.61", "10.00",
                "56.96", "2.00", "11.39", "501.26");
        eva.assertRevenueShareDetails(mpId, srvDiscount, "724.48", "10.00",
                "72.45", "2.00", "14.49", "637.54");
        eva.assertSuppliers(mpId, "1138.80", "1294.09", "129.41");
        eva.assertSuppliersOrganization(mpId, supplier.getOrganizationId(),
                "1138.80", "1294.09", "129.41");
        eva.assertBrokers(mpId, "0.00", "0.00", "0.00");
        eva.assertResellers(mpId, "0.00", "0.00", "0.00");
        eva.assertMarketplaceOwner(mpId, "129.41");
        eva.assertOverallSuppliers("1138.80", "1294.09", "129.41");
        eva.assertOverallSuppliersOrganization(supplier.getOrganizationId(),
                "1138.80", "1294.09", "129.41");
        eva.assertOverallBrokers("0.00", "0.00", "0.00");
        eva.assertOverallResellers("0.00", "0.00", "0.00");
    }

    /**
     * One subscription with upgrade and following shares:</br> Operator Share:
     * 2%</br> Marketplace Share: 10%</br>
     */
    @Test
    public void test2() throws Exception {
        // given
        testSetup.test2();
        TestData testData = getTestData("test2");
        VendorData supplier = testData.getVendor(0);
        VOServiceDetails srv1 = supplier.getService(0);
        VOServiceDetails srv1Upgr = supplier.getService(1);
        String mpId = supplier.getMarketplaceId(0);

        // when
        performBillingRun(0, "2013-09-10 00:00:00");

        // then
        MarketplaceShareResultEvaluator eva = newMarketplaceShareResultEvaluator(
                supplier.getOrganizationKey(), "2013-08-01 00:00:00",
                "2013-09-01 00:00:00");
        eva.assertRevenueShareDetails(mpId, srv1Upgr, "1139.23", "10.00",
                "113.92", "2.00", "22.78", "1002.53");
        eva.assertRevenueShareDetails(mpId, srv1, "1448.97", "10.00", "144.90",
                "2.00", "28.98", "1275.09");
        eva.assertSuppliers(mpId, "2277.62", "2588.20", "258.82");
        eva.assertSuppliersOrganization(mpId, supplier.getOrganizationId(),
                "2277.62", "2588.20", "258.82");
        eva.assertBrokers(mpId, "0.00", "0.00", "0.00");
        eva.assertResellers(mpId, "0.00", "0.00", "0.00");
        eva.assertMarketplaceOwner(mpId, "258.82");
        eva.assertOverallSuppliers("2277.62", "2588.20", "258.82");
        eva.assertOverallSuppliersOrganization(supplier.getOrganizationId(),
                "2277.62", "2588.20", "258.82");
        eva.assertOverallBrokers("0.00", "0.00", "0.00");
        eva.assertOverallResellers("0.00", "0.00", "0.00");
    }

    /**
     * One subscription with an upgrade, another subscription without upgrade.
     * Following shares:</br> Operator Share: 2%</br> Marketplace Share:
     * 10%</br>
     */
    @Test
    public void test3() throws Exception {
        // given
        testSetup.test3();
        TestData testData = getTestData("test3");
        VendorData supplier = testData.getVendor(0);
        VOServiceDetails srv1 = supplier.getService(0);
        VOServiceDetails srv2 = supplier.getService(1);
        VOServiceDetails srv1Upgr = supplier.getService(2);
        String mpId = supplier.getMarketplaceId(0);

        // when
        performBillingRun(0, "2013-09-10 00:00:00");

        // then
        MarketplaceShareResultEvaluator eva = newMarketplaceShareResultEvaluator(
                supplier.getOrganizationKey(), "2013-08-01 00:00:00",
                "2013-09-01 00:00:00");
        eva.assertRevenueShareDetails(mpId, srv1Upgr, "1139.23", "10.00",
                "113.92", "2.00", "22.78", "1002.53");
        eva.assertRevenueShareDetails(mpId, srv1, "1448.97", "10.00", "144.90",
                "2.00", "28.98", "1275.09");
        eva.assertRevenueShareDetails(mpId, srv2, "3524.42", "10.00", "352.44",
                "2.00", "70.49", "3101.49");
        eva.assertSuppliers(mpId, "5379.11", "6112.62", "611.26");
        eva.assertSuppliersOrganization(mpId, supplier.getOrganizationId(),
                "5379.11", "6112.62", "611.26");
        eva.assertBrokers(mpId, "0.00", "0.00", "0.00");
        eva.assertResellers(mpId, "0.00", "0.00", "0.00");
        eva.assertMarketplaceOwner(mpId, "611.26");
        eva.assertOverallSuppliers("5379.11", "6112.62", "611.26");
        eva.assertOverallSuppliersOrganization(supplier.getOrganizationId(),
                "5379.11", "6112.62", "611.26");
        eva.assertOverallBrokers("0.00", "0.00", "0.00");
        eva.assertOverallResellers("0.00", "0.00", "0.00");
    }

    /**
     * Two subscriptions, one on mp1 with 10% marketplace share, the other one
     * on mp2 with 30% marketplace share.
     */
    @Test
    public void test4() throws Exception {
        // given
        testSetup.test4();
        TestData testData = getTestData("test4");
        VendorData supplier = testData.getVendor(0);
        VOServiceDetails srv1 = supplier.getService(0);
        VOServiceDetails srv2 = supplier.getService(1);
        String mpId1 = supplier.getMarketplaceId(srv1);
        String mpId2 = supplier.getMarketplaceId(srv2);

        // when
        performBillingRun(0, "2013-09-10 00:00:00");

        // then
        MarketplaceShareResultEvaluator eva = newMarketplaceShareResultEvaluator(
                supplier.getOrganizationKey(), "2013-08-01 00:00:00",
                "2013-09-01 00:00:00");
        eva.assertRevenueShareDetails(mpId1, srv1, "5369.28", "10.00",
                "536.93", "2.00", "107.39", "4724.96");
        eva.assertSuppliers(mpId1, "4724.96", "5369.28", "536.93");
        eva.assertSuppliersOrganization(mpId1, supplier.getOrganizationId(),
                "4724.96", "5369.28", "536.93");
        eva.assertBrokers(mpId1, "0.00", "0.00", "0.00");
        eva.assertResellers(mpId1, "0.00", "0.00", "0.00");
        eva.assertMarketplaceOwner(mpId1, "536.93");
        eva.assertRevenueShareDetails(mpId2, srv2, "3859.86", "30.00",
                "1157.96", "2.00", "77.20", "2624.70");
        eva.assertSuppliers(mpId2, "2624.70", "3859.86", "1157.96");
        eva.assertSuppliersOrganization(mpId2, supplier.getOrganizationId(),
                "2624.70", "3859.86", "1157.96");
        eva.assertBrokers(mpId2, "0.00", "0.00", "0.00");
        eva.assertResellers(mpId2, "0.00", "0.00", "0.00");
        eva.assertMarketplaceOwner(mpId2, "1157.96");
        eva.assertOverallSuppliers("7349.66", "9229.14", "1694.89");
        eva.assertOverallSuppliersOrganization(supplier.getOrganizationId(),
                "7349.66", "9229.14", "1694.89");
        eva.assertOverallBrokers("0.00", "0.00", "0.00");
        eva.assertOverallResellers("0.00", "0.00", "0.00");
    }
}
