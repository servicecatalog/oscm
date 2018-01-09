/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 24.03.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.calculation.share;

import java.math.BigDecimal;

import org.junit.BeforeClass;
import org.junit.Test;

import org.oscm.billingservice.business.calculation.share.setup.SharesDirectSetup;
import org.oscm.billingservice.evaluation.MarketplaceShareResultEvaluator;
import org.oscm.billingservice.evaluation.SupplierShareResultEvaluator;
import org.oscm.billingservice.setup.BillingIntegrationTestBase;
import org.oscm.billingservice.setup.CustomerData;
import org.oscm.billingservice.setup.TestData;
import org.oscm.billingservice.setup.VendorData;
import org.oscm.internal.vo.VOServiceDetails;

/**
 * @author baumann
 * 
 */
public class SharesDirectIT extends BillingIntegrationTestBase {

    private SharesDirectSetup testSetup = new SharesDirectSetup();

    @BeforeClass
    public static void setupOnce() throws Exception {
        BillingIntegrationTestBase.initialize();
        BillingIntegrationTestBase.createBasicTestData(true);
    }

    /**
     * Suspend a subscription and resume it again in the same billing period.
     */
    @Test
    public void suspendResume() throws Exception {
        // given
        testSetup.setupSuspendResume("testSuspendResume");
        TestData testData = getTestData("testSuspendResume");
        VendorData supplier = testData.getVendor(0);
        CustomerData customer = supplier.getCustomer(0);
        VOServiceDetails service = supplier.getService(0);
        String mpId = supplier.getMarketplaceId(service);

        // when
        performBillingRun(0, "2014-03-02 10:00:00");

        // then
        SupplierShareResultEvaluator supplEva = newSupplierShareResultEvaluator(
                supplier.getOrganizationKey(), "2014-02-01 00:00:00",
                "2014-03-01 00:00:00");

        supplEva.assertSupplierRevenue("2643.20");
        supplEva.assertDirectRevenue("3776.00", "755.20", "377.60");

        supplEva.assertRevenuePerMarketplace(mpId, "3776.00", "755.20",
                "377.60", "0.00", "0.00", "2643.20");

        supplEva.assertSubscription(mpId, "susresService_subscr", service,
                "3776.00");
        supplEva.assertRevenueShareDetails(mpId, service, "3776.00", "20.00",
                "755.20", "10.00", "377.60", "2643.20");
        supplEva.assertCustomerRevenueShareDetails(mpId, service, customer
                .getOrganization().getOrganizationId(), "3776.00", "755.20",
                "377.60", "2643.20");

        MarketplaceShareResultEvaluator mpEva = newMarketplaceShareResultEvaluator(
                supplier.getOrganizationKey(), "2014-02-01 00:00:00",
                "2014-03-01 00:00:00");

        mpEva.assertRevenueShareDetails(mpId, service, "3776.00", "20.00",
                "755.20", "10.00", "377.60", "2643.20");

        mpEva.assertSuppliers(mpId, "2643.20", "3776.00", "755.20");

        mpEva.assertSuppliersOrganization(mpId, supplier.getOrganizationId(),
                "2643.20", "3776.00", "755.20");
        mpEva.assertBrokers(mpId, "0.00", "0.00", "0.00");
        mpEva.assertResellers(mpId, "0.00", "0.00", "0.00");
        mpEva.assertMarketplaceOwner(mpId, "755.20");

        mpEva.assertOverallSuppliers("2643.20", "3776.00", "755.20");
        mpEva.assertOverallSuppliersOrganization(supplier.getOrganizationId(),
                "2643.20", "3776.00", "755.20");
        mpEva.assertOverallBrokers("0.00", "0.00", "0.00");
        mpEva.assertOverallResellers("0.00", "0.00", "0.00");
    }

    @Test
    public void suspendResumeForCustomerDiscount() throws Exception {
        // given
        testSetup.setupSuspendAndResumeWithCustomerDiscount(
                "testSuspendResumeForCustomerDiscount",
                new BigDecimal("50.00"), 2.0D);
        TestData testData = getTestData("testSuspendResumeForCustomerDiscount");
        VendorData supplier = testData.getVendor(0);
        CustomerData customer = supplier.getCustomer(0);
        VOServiceDetails srvDiscount = supplier.getService(0);
        String mpId = supplier.getMarketplaceId(srvDiscount);

        // when
        performBillingRun(0, "2013-09-10 00:00:00");

        // then
        SupplierShareResultEvaluator supplEva = newSupplierShareResultEvaluator(
                supplier.getOrganizationKey(), "2013-08-01 00:00:00",
                "2013-09-01 00:00:00");

        supplEva.assertSupplierRevenue("1216.77");
        supplEva.assertDirectRevenue("1241.60", "0.00", "24.83");
        supplEva.assertRevenuePerMarketplace(mpId, "1241.60", "0.00", "24.83",
                "0.00", "0.00", "1216.77");

        supplEva.assertSubscription(mpId, "srvDiscount_subscr", srvDiscount,
                "1241.60");
        supplEva.assertRevenueShareDetails(mpId, srvDiscount, "1241.60",
                "0.00", "0.00", "2.00", "24.83", "1216.77");
        supplEva.assertCustomerRevenueShareDetails(mpId, srvDiscount, customer
                .getOrganization().getOrganizationId(), "1241.60", "0.00",
                "24.83", "1216.77");

        MarketplaceShareResultEvaluator mpEva = newMarketplaceShareResultEvaluator(
                supplier.getOrganizationKey(), "2013-08-01 00:00:00",
                "2013-09-01 00:00:00");
        mpEva.assertRevenueShareDetails(mpId, srvDiscount, "1241.60", "0.00",
                "0.00", "2.00", "24.83", "1216.77");

        mpEva.assertSuppliers(mpId, "1216.77", "1241.60", "0.00");
        mpEva.assertSuppliersOrganization(mpId, supplier.getOrganizationId(),
                "1216.77", "1241.60", "0.00");
        mpEva.assertBrokers(mpId, "0.00", "0.00", "0.00");
        mpEva.assertResellers(mpId, "0.00", "0.00", "0.00");
        mpEva.assertMarketplaceOwner(mpId, "0.00");

        mpEva.assertOverallSuppliers("1216.77", "1241.60", "0.00");
        mpEva.assertOverallSuppliersOrganization(supplier.getOrganizationId(),
                "1216.77", "1241.60", "0.00");
        mpEva.assertOverallBrokers("0.00", "0.00", "0.00");
        mpEva.assertOverallResellers("0.00", "0.00", "0.00");

    }

    @Test
    public void suspendResumeMultipleCustomerSubscriptions() throws Exception {
        // given
        testSetup.setupSuspendAndResumeMultipleCustomerSubscriptions(
                "testSuspendResumeMultipleCustomerSubscriptions", 2.0D, 4.0D,
                10.0D);
        TestData testData = getTestData("testSuspendResumeMultipleCustomerSubscriptions");
        VendorData supplier = testData.getVendor(0);
        VOServiceDetails service1 = supplier.getService(0);
        VOServiceDetails service2 = supplier.getService(1);
        String mpId = supplier.getMarketplaceId(service1);

        CustomerData customer1 = supplier.getCustomer(0);
        CustomerData customer2 = supplier.getCustomer(1);
        CustomerData customer3 = supplier.getCustomer(2);

        // when
        performBillingRun(0, "2013-09-10 00:00:00");

        // then
        SupplierShareResultEvaluator supplEva = newSupplierShareResultEvaluator(
                supplier.getOrganizationKey(), "2013-08-01 00:00:00",
                "2013-09-01 00:00:00");

        supplEva.assertSupplierRevenue("8883.44");
        supplEva.assertDirectRevenue("10184.65", "1018.47", "282.74");
        supplEva.assertRevenuePerMarketplace(mpId, "10184.65", "1018.47",
                "282.74", "0.00", "0.00", "8883.44");

        supplEva.assertSubscription(mpId, "srv_subscr1", service1, "2660.50");
        supplEva.assertSubscription(mpId, "srv_subscr2", service1, "3572.25");
        supplEva.assertSubscription(mpId, "srv2_subscr3", service2, "3951.90");

        supplEva.assertRevenueShareDetails(mpId, service1, "6232.75", "10.00",
                "623.28", "2.00", "124.66", "5484.81");

        supplEva.assertRevenueShareDetails(mpId, service2, "3951.90", "10.00",
                "395.19", "4.00", "158.08", "3398.63");

        supplEva.assertCustomerRevenueShareDetails(mpId, service1, customer1
                .getOrganization().getOrganizationId(), "2660.50", "266.05",
                "53.21", "2341.24");
        supplEva.assertCustomerRevenueShareDetails(mpId, service1, customer2
                .getOrganization().getOrganizationId(), "3572.25", "357.23",
                "71.45", "3143.57");
        supplEva.assertCustomerRevenueShareDetails(mpId, service2, customer3
                .getOrganization().getOrganizationId(), "3951.90", "395.19",
                "158.08", "3398.63");

        MarketplaceShareResultEvaluator mpEva = newMarketplaceShareResultEvaluator(
                supplier.getOrganizationKey(), "2013-08-01 00:00:00",
                "2013-09-01 00:00:00");

        mpEva.assertRevenueShareDetails(mpId, service1, "6232.75", "10.00",
                "623.28", "2.00", "124.66", "5484.81");
        mpEva.assertRevenueShareDetails(mpId, service2, "3951.90", "10.00",
                "395.19", "4.00", "158.08", "3398.63");

        mpEva.assertSuppliers(mpId, "8883.44", "10184.65", "1018.47");
        mpEva.assertSuppliersOrganization(mpId, supplier.getOrganizationId(),
                "8883.44", "10184.65", "1018.47");
        mpEva.assertBrokers(mpId, "0.00", "0.00", "0.00");
        mpEva.assertResellers(mpId, "0.00", "0.00", "0.00");
        mpEva.assertMarketplaceOwner(mpId, "1018.47");

        mpEva.assertOverallSuppliers("8883.44", "10184.65", "1018.47");
        mpEva.assertOverallSuppliersOrganization(supplier.getOrganizationId(),
                "8883.44", "10184.65", "1018.47");
        mpEva.assertOverallBrokers("0.00", "0.00", "0.00");
        mpEva.assertOverallResellers("0.00", "0.00", "0.00");
    }

    @Test
    public void suspendUpgradeSuspendUpgrade() throws Exception {
        // given
        testSetup.setupSuspendUpgradeSuspendUpgrade(
                "testSuspendUpgradeSuspend", new BigDecimal("50.00"), 2.0D);
        TestData testData = getTestData("testSuspendUpgradeSuspend");
        VendorData supplier = testData.getVendor(0);
        CustomerData customer = supplier.getCustomer(0);
        VOServiceDetails srvDiscount = supplier.getService(0);
        VOServiceDetails srvDiscountUpgr = supplier.getService(1);
        VOServiceDetails srvDiscountUpgr2 = supplier.getService(2);

        String mpId = supplier.getMarketplaceId(srvDiscount);

        // when
        performBillingRun(0, "2013-09-10 00:00:00");

        // then
        SupplierShareResultEvaluator supplEva = newSupplierShareResultEvaluator(
                supplier.getOrganizationKey(), "2013-08-01 00:00:00",
                "2013-09-01 00:00:00");

        supplEva.assertSupplierRevenue("2433.83");
        supplEva.assertDirectRevenue("2483.50", "0.00", "49.67");
        supplEva.assertRevenuePerMarketplace(mpId, "2483.50", "0.00", "49.67",
                "0.00", "0.00", "2433.83");

        supplEva.assertSubscription(mpId, "srvDiscount_subscr", srvDiscount,
                "475.00");
        supplEva.assertRevenueShareDetails(mpId, srvDiscount, "475.00", "0.00",
                "0.00", "2.00", "9.50", "465.50");
        supplEva.assertCustomerRevenueShareDetails(mpId, srvDiscount, customer
                .getOrganization().getOrganizationId(), "475.00", "0.00",
                "9.50", "465.50");

        supplEva.assertSubscription(mpId, "srvDiscount_subscr",
                srvDiscountUpgr, "1301.00");
        supplEva.assertRevenueShareDetails(mpId, srvDiscountUpgr, "1301.00",
                "0.00", "0.00", "2.00", "26.02", "1274.98");
        supplEva.assertCustomerRevenueShareDetails(mpId, srvDiscountUpgr,
                customer.getOrganization().getOrganizationId(), "1301.00",
                "0.00", "26.02", "1274.98");

        supplEva.assertSubscription(mpId, "srvDiscount_subscr",
                srvDiscountUpgr2, "707.50");
        supplEva.assertRevenueShareDetails(mpId, srvDiscountUpgr2, "707.50",
                "0.00", "0.00", "2.00", "14.15", "693.35");
        supplEva.assertCustomerRevenueShareDetails(mpId, srvDiscountUpgr2,
                customer.getOrganization().getOrganizationId(), "707.50",
                "0.00", "14.15", "693.35");

        MarketplaceShareResultEvaluator mpEva = newMarketplaceShareResultEvaluator(
                supplier.getOrganizationKey(), "2013-08-01 00:00:00",
                "2013-09-01 00:00:00");

        mpEva.assertRevenueShareDetails(mpId, srvDiscount, "475.00", "0.00",
                "0.00", "2.00", "9.50", "465.50");
        mpEva.assertRevenueShareDetails(mpId, srvDiscountUpgr, "1301.00",
                "0.00", "0.00", "2.00", "26.02", "1274.98");
        mpEva.assertRevenueShareDetails(mpId, srvDiscountUpgr2, "707.50",
                "0.00", "0.00", "2.00", "14.15", "693.35");

        mpEva.assertSuppliers(mpId, "2433.83", "2483.50", "0.00");
        mpEva.assertSuppliersOrganization(mpId, supplier.getOrganizationId(),
                "2433.83", "2483.50", "0.00");
        mpEva.assertBrokers(mpId, "0.00", "0.00", "0.00");
        mpEva.assertResellers(mpId, "0.00", "0.00", "0.00");
        mpEva.assertMarketplaceOwner(mpId, "0.00");

        mpEva.assertOverallSuppliers("2433.83", "2483.50", "0.00");
        mpEva.assertOverallSuppliersOrganization(supplier.getOrganizationId(),
                "2433.83", "2483.50", "0.00");
        mpEva.assertOverallBrokers("0.00", "0.00", "0.00");
        mpEva.assertOverallResellers("0.00", "0.00", "0.00");

    }

    /**
     * Two customer subscriptions to the same service with customer discounts
     * of: 50% and 25%
     */
    @Test
    public void testTwoCustomersWithDiscounts_ZeroMplShare() throws Exception {
        // given
        testSetup.setupTwoCustomersWithDiscount(
                "testTwoCustomersWithDiscounts", new BigDecimal("50.00"),
                new BigDecimal("25.00"), 2.0D, 0.0D);

        TestData testData = getTestData("testTwoCustomersWithDiscounts");
        VendorData supplier = testData.getVendor(0);
        VOServiceDetails srvDiscount = supplier.getService(0);
        String mpId = supplier.getMarketplaceId(srvDiscount);
        CustomerData customer1 = supplier.getCustomer(0);
        CustomerData customer2 = supplier.getCustomer(1);

        // when
        performBillingRun(0, "2013-09-10 00:00:00");

        // then evaluate the two subscription costs, the supplier revenue shares
        // (including customer revenue share details)
        // and the marketplace revenue share details.
        SupplierShareResultEvaluator supplEva = newSupplierShareResultEvaluator(
                supplier.getOrganizationKey(), "2013-08-01 00:00:00",
                "2013-09-01 00:00:00");

        supplEva.assertSupplierRevenue("6577.37");
        supplEva.assertDirectRevenue("6711.60", "0.00", "134.23");
        supplEva.assertRevenuePerMarketplace(mpId, "6711.60", "0.00", "134.23",
                "0.00", "0.00", "6577.37");

        supplEva.assertSubscription(mpId, "srvDiscount_subscr1", srvDiscount,
                "2684.64");

        supplEva.assertSubscription(mpId, "srvDiscount_subscr2", srvDiscount,
                "4026.96");

        supplEva.assertRevenueShareDetails(mpId, srvDiscount, "6711.60",
                "0.00", "0.00", "2.00", "134.23", "6577.37");

        supplEva.assertCustomerRevenueShareDetails(mpId, srvDiscount, customer1
                .getOrganization().getOrganizationId(), "2684.64", "0.00",
                "53.69", "2630.95");
        supplEva.assertCustomerRevenueShareDetails(mpId, srvDiscount, customer2
                .getOrganization().getOrganizationId(), "4026.96", "0.00",
                "80.54", "3946.42");

        MarketplaceShareResultEvaluator mpEva = newMarketplaceShareResultEvaluator(
                supplier.getOrganizationKey(), "2013-08-01 00:00:00",
                "2013-09-01 00:00:00");

        mpEva.assertRevenueShareDetails(mpId, srvDiscount, "6711.60", "0.00",
                "0.00", "2.00", "134.23", "6577.37");
        mpEva.assertSuppliers(mpId, "6577.37", "6711.60", "0.00");
        mpEva.assertSuppliersOrganization(mpId, supplier.getOrganizationId(),
                "6577.37", "6711.60", "0.00");
        mpEva.assertBrokers(mpId, "0.00", "0.00", "0.00");
        mpEva.assertResellers(mpId, "0.00", "0.00", "0.00");
        mpEva.assertMarketplaceOwner(mpId, "0.00");
        mpEva.assertOverallSuppliers("6577.37", "6711.60", "0.00");
        mpEva.assertOverallSuppliersOrganization(supplier.getOrganizationId(),
                "6577.37", "6711.60", "0.00");
        mpEva.assertOverallBrokers("0.00", "0.00", "0.00");
        mpEva.assertOverallResellers("0.00", "0.00", "0.00");
    }

    /**
     * Two customer subscriptions to the same service with customer discounts of
     * 50% and 25%. The subscriptions are terminated before billing period end.
     * See Bug 10933.
     */
    @Test
    public void testTwoCustomersWithDiscounts_ZeroMplShare_Terminate()
            throws Exception {
        // given
        testSetup.setupTwoCustomersWithDiscount_Terminate(
                "testTwoCustomersWithDiscounts_Terminate", new BigDecimal(
                        "50.00"), new BigDecimal("25.00"), 2.0D, 0.0D);

        TestData testData = getTestData("testTwoCustomersWithDiscounts_Terminate");
        VendorData supplier = testData.getVendor(0);
        VOServiceDetails srvDiscount = supplier.getService(0);
        String mpId = supplier.getMarketplaceId(srvDiscount);
        CustomerData customer1 = supplier.getCustomer(0);
        CustomerData customer2 = supplier.getCustomer(1);

        // when
        performBillingRun(0, "2013-09-10 00:00:00");

        // then evaluate the two subscription costs, the supplier revenue shares
        // (including customer revenue share details)
        // and the marketplace revenue share details.
        SupplierShareResultEvaluator supplEva = newSupplierShareResultEvaluator(
                supplier.getOrganizationKey(), "2013-08-01 00:00:00",
                "2013-09-01 00:00:00");

        supplEva.assertSupplierRevenue("6577.37");
        supplEva.assertDirectRevenue("6711.60", "0.00", "134.23");
        supplEva.assertRevenuePerMarketplace(mpId, "6711.60", "0.00", "134.23",
                "0.00", "0.00", "6577.37");

        supplEva.assertSubscription(mpId, "srvDiscount2_subscr1", srvDiscount,
                "2684.64");

        supplEva.assertSubscription(mpId, "srvDiscount2_subscr2", srvDiscount,
                "4026.96");

        supplEva.assertRevenueShareDetails(mpId, srvDiscount, "6711.60",
                "0.00", "0.00", "2.00", "134.23", "6577.37");

        supplEva.assertCustomerRevenueShareDetails(mpId, srvDiscount, customer1
                .getOrganization().getOrganizationId(), "2684.64", "0.00",
                "53.69", "2630.95");
        supplEva.assertCustomerRevenueShareDetails(mpId, srvDiscount, customer2
                .getOrganization().getOrganizationId(), "4026.96", "0.00",
                "80.54", "3946.42");

        MarketplaceShareResultEvaluator mpEva = newMarketplaceShareResultEvaluator(
                supplier.getOrganizationKey(), "2013-08-01 00:00:00",
                "2013-09-01 00:00:00");

        mpEva.assertRevenueShareDetails(mpId, srvDiscount, "6711.60", "0.00",
                "0.00", "2.00", "134.23", "6577.37");
        mpEva.assertSuppliers(mpId, "6577.37", "6711.60", "0.00");
        mpEva.assertSuppliersOrganization(mpId, supplier.getOrganizationId(),
                "6577.37", "6711.60", "0.00");
        mpEva.assertBrokers(mpId, "0.00", "0.00", "0.00");
        mpEva.assertResellers(mpId, "0.00", "0.00", "0.00");
        mpEva.assertMarketplaceOwner(mpId, "0.00");
        mpEva.assertOverallSuppliers("6577.37", "6711.60", "0.00");
        mpEva.assertOverallSuppliersOrganization(supplier.getOrganizationId(),
                "6577.37", "6711.60", "0.00");
        mpEva.assertOverallBrokers("0.00", "0.00", "0.00");
        mpEva.assertOverallResellers("0.00", "0.00", "0.00");
    }

    /**
     * One customer subscribes to two services provided by two different
     * suppliers, for which he has two different discounts.
     */
    @Test
    public void testCustomerWithTwoDiscounts() throws Exception {

        // given
        testSetup.setupCustomerWithTwoDiscounts("testCustomerWithTwoDiscounts",
                new BigDecimal("50.00"), new BigDecimal("25.00"), 2.0D, 4.0D,
                10.0D, 20.0D);

        TestData testData = getTestData("testCustomerWithTwoDiscounts");
        VendorData supplier1 = testData.getVendor(0);
        VendorData supplier2 = testData.getVendor(1);
        VOServiceDetails service1 = supplier1.getService(0);
        VOServiceDetails service2 = supplier2.getService(0);
        String mpId1 = supplier1.getMarketplaceId(service1);
        String mpId2 = supplier2.getMarketplaceId(service2);
        CustomerData customer = supplier1.getCustomer(0);

        // when
        performBillingRun(0, "2013-09-10 00:00:00");

        // then
        SupplierShareResultEvaluator supplEva1 = newSupplierShareResultEvaluator(
                supplier1.getOrganizationKey(), "2013-08-01 00:00:00",
                "2013-09-01 00:00:00");

        supplEva1.assertSupplierRevenue("2504.92");
        supplEva1.assertDirectRevenue("2846.50", "284.65", "56.93");
        supplEva1.assertRevenuePerMarketplace(mpId1, "2846.50", "284.65",
                "56.93", "0.00", "0.00", "2504.92");

        supplEva1.assertSubscription(mpId1, "srv1_subscr", service1, "2846.50");
        supplEva1.assertRevenueShareDetails(mpId1, service1, "2846.50",
                "10.00", "284.65", "2.00", "56.93", "2504.92");
        supplEva1.assertCustomerRevenueShareDetails(mpId1, service1, customer
                .getOrganization().getOrganizationId(), "2846.50", "284.65",
                "56.93", "2504.92");

        MarketplaceShareResultEvaluator mpEva1 = newMarketplaceShareResultEvaluator(
                supplier1.getOrganizationKey(), "2013-08-01 00:00:00",
                "2013-09-01 00:00:00");

        mpEva1.assertRevenueShareDetails(mpId1, service1, "2846.50", "10.00",
                "284.65", "2.00", "56.93", "2504.92");

        mpEva1.assertSuppliers(mpId1, "2504.92", "2846.50", "284.65");
        mpEva1.assertSuppliersOrganization(mpId1,
                supplier1.getOrganizationId(), "2504.92", "2846.50", "284.65");
        mpEva1.assertBrokers(mpId1, "0.00", "0.00", "0.00");
        mpEva1.assertResellers(mpId1, "0.00", "0.00", "0.00");
        mpEva1.assertMarketplaceOwner(mpId1, "284.65");

        mpEva1.assertOverallSuppliers("2504.92", "2846.50", "284.65");
        mpEva1.assertOverallSuppliersOrganization(
                supplier1.getOrganizationId(), "2504.92", "2846.50", "284.65");
        mpEva1.assertOverallBrokers("0.00", "0.00", "0.00");
        mpEva1.assertOverallResellers("0.00", "0.00", "0.00");

        // then
        SupplierShareResultEvaluator supplEva2 = newSupplierShareResultEvaluator(
                supplier2.getOrganizationKey(), "2013-08-01 00:00:00",
                "2013-09-01 00:00:00");

        supplEva2.assertSupplierRevenue("3245.01");
        supplEva2.assertDirectRevenue("4269.75", "853.95", "170.79");
        supplEva2.assertRevenuePerMarketplace(mpId2, "4269.75", "853.95",
                "170.79", "0.00", "0.00", "3245.01");

        supplEva2.assertSubscription(mpId2, "srv2_subscr", service2, "4269.75");
        supplEva2.assertRevenueShareDetails(mpId2, service2, "4269.75",
                "20.00", "853.95", "4.00", "170.79", "3245.01");
        supplEva2.assertCustomerRevenueShareDetails(mpId2, service2, customer
                .getOrganization().getOrganizationId(), "4269.75", "853.95",
                "170.79", "3245.01");

        MarketplaceShareResultEvaluator mpEva2 = newMarketplaceShareResultEvaluator(
                supplier2.getOrganizationKey(), "2013-08-01 00:00:00",
                "2013-09-01 00:00:00");

        mpEva2.assertRevenueShareDetails(mpId2, service2, "4269.75", "20.00",
                "853.95", "4.00", "170.79", "3245.01");

        mpEva2.assertSuppliers(mpId2, "3245.01", "4269.75", "853.95");
        mpEva2.assertSuppliersOrganization(mpId2,
                supplier2.getOrganizationId(), "3245.01", "4269.75", "853.95");
        mpEva2.assertBrokers(mpId2, "0.00", "0.00", "0.00");
        mpEva2.assertResellers(mpId2, "0.00", "0.00", "0.00");
        mpEva2.assertMarketplaceOwner(mpId2, "853.95");

        mpEva2.assertOverallSuppliers("3245.01", "4269.75", "853.95");
        mpEva2.assertOverallSuppliersOrganization(
                supplier2.getOrganizationId(), "3245.01", "4269.75", "853.95");
        mpEva2.assertOverallBrokers("0.00", "0.00", "0.00");
        mpEva2.assertOverallResellers("0.00", "0.00", "0.00");

    }

    /**
     * Customer1 subscribes to service1 twice. First subscription is upgraded to
     * free service and downgraded again to original service in the same billing
     * period. Customer2 also subscribes to service1.
     */
    @Test
    public void multipleSubscriptionsUpDowngrade() throws Exception {

        // given
        testSetup.setupMultipleSubscriptionsUpDowngrade(
                "multipleSubscriptionsUpDowngrade", 20.0D, 10.0D);

        TestData testData = getTestData("multipleSubscriptionsUpDowngrade");
        VendorData supplier = testData.getVendor(0);
        VOServiceDetails service1 = supplier.getService(0);
        String mpId = supplier.getMarketplaceId(0);
        CustomerData customer1 = supplier.getCustomer(0);
        CustomerData customer2 = supplier.getCustomer(1);

        // when
        performBillingRun(0, "2013-09-10 00:00:00");

        // then
        SupplierShareResultEvaluator supplEva1 = newSupplierShareResultEvaluator(
                supplier.getOrganizationKey(), "2013-08-01 00:00:00",
                "2013-09-01 00:00:00");

        supplEva1.assertSupplierRevenue("9927.10");
        supplEva1.assertDirectRevenue("14181.58", "1418.16", "2836.32");
        supplEva1.assertRevenuePerMarketplace(mpId, "14181.58", "1418.16",
                "2836.32", "0.00", "0.00", "9927.10");

        supplEva1.assertSubscription(mpId, "customer1_subscr1", service1,
                "3946.14");
        supplEva1.assertSubscription(mpId, "customer1_subscr2", service1,
                "5201.58");
        supplEva1.assertSubscription(mpId, "customer2_subscr", service1,
                "5033.86");

        supplEva1.assertRevenueShareDetails(mpId, service1, "14181.58",
                "10.00", "1418.16", "20.00", "2836.32", "9927.10");
        supplEva1.assertCustomerRevenueShareDetails(mpId, service1, customer1
                .getOrganization().getOrganizationId(), "9147.72", "914.77",
                "1829.54", "6403.41");
        supplEva1.assertCustomerRevenueShareDetails(mpId, service1, customer2
                .getOrganization().getOrganizationId(), "5033.86", "503.39",
                "1006.77", "3523.70");

        MarketplaceShareResultEvaluator mpEva = newMarketplaceShareResultEvaluator(
                supplier.getOrganizationKey(), "2013-08-01 00:00:00",
                "2013-09-01 00:00:00");

        mpEva.assertRevenueShareDetails(mpId, service1, "14181.58", "10.00",
                "1418.16", "20.00", "2836.32", "9927.10");

        mpEva.assertSuppliers(mpId, "9927.10", "14181.58", "1418.17");
        mpEva.assertSuppliersOrganization(mpId, supplier.getOrganizationId(),
                "9927.10", "14181.58", "1418.17");
        mpEva.assertBrokers(mpId, "0.00", "0.00", "0.00");
        mpEva.assertResellers(mpId, "0.00", "0.00", "0.00");
        mpEva.assertMarketplaceOwner(mpId, "1418.17");

        mpEva.assertOverallSuppliers("9927.10", "14181.58", "1418.17");
        mpEva.assertOverallSuppliersOrganization(supplier.getOrganizationId(),
                "9927.10", "14181.58", "1418.17");
        mpEva.assertOverallBrokers("0.00", "0.00", "0.00");
        mpEva.assertOverallResellers("0.00", "0.00", "0.00");
    }

}
