/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 14.04.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.calculation.share;

import org.junit.BeforeClass;
import org.junit.Test;

import org.oscm.billingservice.business.calculation.share.setup.SharesResellerSetup;
import org.oscm.billingservice.evaluation.MarketplaceShareResultEvaluator;
import org.oscm.billingservice.evaluation.ResellerShareResultEvaluator;
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
public class SharesResellerIT extends BillingIntegrationTestBase {

    private SharesResellerSetup testSetup = new SharesResellerSetup();

    @BeforeClass
    public static void setupOnce() throws Exception {
        BillingIntegrationTestBase.initialize();
        BillingIntegrationTestBase.createBasicTestData(true);
    }

    /**
     * One subscription to a reseller service without customer discount.
     * Reseller has cutoff day 2!
     */
    @Test
    public void subscribeToResellerServiceNoDiscount() throws Exception {
        // given
        testSetup.subscribeToResellerServiceNoDiscount();
        TestData testData = getTestData("subscribeToResellerServiceNoDiscount");
        VendorData supplier = testData.getVendor(0);
        VendorData reseller = testData.getVendor(1);
        CustomerData resellerCustomer = reseller.getCustomer(0);
        VOServiceDetails resellerService = reseller.getService(0);
        String resellerMpId = reseller.getMarketplaceId(0);

        // when
        performBillingRun(0, "2013-02-05 00:00:00"); // for billing result
                                                     // (2.1.-2.2. 2013)
        performBillingRun(0, "2013-03-05 00:00:00"); // for share results
                                                     // (period 1.2.-1.3.2013)

        // then
        SupplierShareResultEvaluator supplEva = newSupplierShareResultEvaluator(
                supplier.getOrganizationKey(), "2013-01-02 00:00:00",
                "2013-02-02 00:00:00");

        supplEva.assertSubscriptionsRevenue(resellerMpId, resellerService,
                "4950.00");
        supplEva.assertRevenueShareDetails_ResellerService(resellerMpId,
                resellerService, "4950.00", "10.00", "495.00", "5.00",
                "247.50", "15.00", "742.50", "3465.00");
        supplEva.assertCustomerRevenueShareDetails_ResellerService(
                resellerMpId, resellerService,
                resellerCustomer.getOrganizationId(), "4950.00", "495.00",
                "247.50", "3465.00", "742.50");

        supplEva.assertRevenuePerMarketplace(resellerMpId, "4950.00", "495.00",
                "247.50", "742.50", "0.00", "3465.00");

        supplEva.assertSupplierRevenue("3465.00");
        supplEva.assertResellerRevenue("4950.00", "495.00", "247.50", "742.50",
                "3465.00");

        // then
        ResellerShareResultEvaluator resellerEva = newResellerShareResultEvaluator(
                reseller.getOrganizationKey(), "2013-01-02 00:00:00",
                "2013-02-02 00:00:00");
        resellerEva.assertSubscription(supplier.getOrganizationId(),
                "resellerSubscr1", resellerService, "4950.00");
        resellerEva.assertServiceRevenue(supplier.getOrganizationId(),
                resellerService, "4950.00", "15.00", "742.50");
        resellerEva.assertServiceCustomerRevenue(supplier.getOrganizationId(),
                resellerService, resellerCustomer.getOrganizationId(),
                "4950.00", "15.00", "742.50");
        resellerEva.assertResellerRevenuePerSupplier(
                supplier.getOrganizationId(), "4950.00", "742.50", "4207.50");
        resellerEva.assertResellerRevenue("4950.00", "742.50", "4207.50");

        MarketplaceShareResultEvaluator mpEva = newMarketplaceShareResultEvaluator(
                reseller.getOrganizationKey(), "2013-01-02 00:00:00",
                "2013-02-02 00:00:00");

        mpEva.assertRevenueShareDetails_ResellerService(resellerMpId,
                resellerService, "4950.00", "10.00", "495.00", "5.00",
                "247.50", "15.00", "742.50", "3465.00");
        mpEva.assertSuppliers(resellerMpId, "3465.00", "0.00", "0.00");
        mpEva.assertSuppliersOrganization(resellerMpId,
                supplier.getOrganizationId(), "3465.00", "0.00", "0.00");
        mpEva.assertResellers(resellerMpId, "742.50", "4950.00", "495.00");
        mpEva.assertResellersOrganization(resellerMpId,
                reseller.getOrganizationId(), "742.50", "4950.00", "495.00");
        mpEva.assertBrokers(resellerMpId, "0.00", "0.00", "0.00");
        mpEva.assertMarketplaceOwner(resellerMpId, "495.00");
        mpEva.assertOverallSuppliers("3465.00", "0.00", "0.00");
        mpEva.assertOverallSuppliersOrganization(supplier.getOrganizationId(),
                "3465.00", "0.00", "0.00");
        mpEva.assertOverallResellers("742.50", "4950.00", "495.00");
        mpEva.assertOverallResellersOrganization(reseller.getOrganizationId(),
                "742.50", "4950.00", "495.00");
        mpEva.assertOverallBrokers("0.00", "0.00", "0.00");

    }

    /**
     * One subscription to a reseller service without customer discount.
     * Reseller has cutoff day 2! The subscription is terminated after 4 weeks.
     * See BUG 10933
     */
    @Test
    public void subscribeToResellerServiceTerminate() throws Exception {
        // given
        testSetup.subscribeToResellerServiceTerminate();
        TestData testData = getTestData("subscribeToResellerServiceTerminate");
        VendorData supplier = testData.getVendor(0);
        VendorData reseller = testData.getVendor(1);
        CustomerData resellerCustomer = reseller.getCustomer(0);
        VOServiceDetails resellerService = reseller.getService(0);
        String resellerMpId = reseller.getMarketplaceId(0);

        // when
        performBillingRun(0, "2013-05-05 00:00:00"); // for billing result
                                                     // (2.4.-2.5.2013)
        performBillingRun(0, "2013-06-05 00:00:00"); // for share results
                                                     // (period 1.5.-1.6.2013)

        // then
        SupplierShareResultEvaluator supplEva = newSupplierShareResultEvaluator(
                supplier.getOrganizationKey(), "2013-04-02 00:00:00",
                "2013-05-02 00:00:00");

        supplEva.assertSubscriptionsRevenue(resellerMpId, resellerService,
                "4950.00");
        supplEva.assertRevenueShareDetails_ResellerService(resellerMpId,
                resellerService, "4950.00", "10.00", "495.00", "5.00",
                "247.50", "15.00", "742.50", "3465.00");
        supplEva.assertCustomerRevenueShareDetails_ResellerService(
                resellerMpId, resellerService,
                resellerCustomer.getOrganizationId(), "4950.00", "495.00",
                "247.50", "3465.00", "742.50");

        supplEva.assertRevenuePerMarketplace(resellerMpId, "4950.00", "495.00",
                "247.50", "742.50", "0.00", "3465.00");

        supplEva.assertSupplierRevenue("3465.00");
        supplEva.assertResellerRevenue("4950.00", "495.00", "247.50", "742.50",
                "3465.00");

        // then
        ResellerShareResultEvaluator resellerEva = newResellerShareResultEvaluator(
                reseller.getOrganizationKey(), "2013-04-02 00:00:00",
                "2013-05-02 00:00:00");
        resellerEva.assertSubscription(supplier.getOrganizationId(),
                "resellerSubWithTerminate", resellerService, "4950.00");
        resellerEva.assertServiceRevenue(supplier.getOrganizationId(),
                resellerService, "4950.00", "15.00", "742.50");
        resellerEva.assertServiceCustomerRevenue(supplier.getOrganizationId(),
                resellerService, resellerCustomer.getOrganizationId(),
                "4950.00", "15.00", "742.50");
        resellerEva.assertResellerRevenuePerSupplier(
                supplier.getOrganizationId(), "4950.00", "742.50", "4207.50");
        resellerEva.assertResellerRevenue("4950.00", "742.50", "4207.50");

        MarketplaceShareResultEvaluator mpEva = newMarketplaceShareResultEvaluator(
                reseller.getOrganizationKey(), "2013-04-02 00:00:00",
                "2013-05-02 00:00:00");

        mpEva.assertRevenueShareDetails_ResellerService(resellerMpId,
                resellerService, "4950.00", "10.00", "495.00", "5.00",
                "247.50", "15.00", "742.50", "3465.00");
        mpEva.assertSuppliers(resellerMpId, "3465.00", "0.00", "0.00");
        mpEva.assertSuppliersOrganization(resellerMpId,
                supplier.getOrganizationId(), "3465.00", "0.00", "0.00");
        mpEva.assertResellers(resellerMpId, "742.50", "4950.00", "495.00");
        mpEva.assertResellersOrganization(resellerMpId,
                reseller.getOrganizationId(), "742.50", "4950.00", "495.00");
        mpEva.assertBrokers(resellerMpId, "0.00", "0.00", "0.00");
        mpEva.assertMarketplaceOwner(resellerMpId, "495.00");
        mpEva.assertOverallSuppliers("3465.00", "0.00", "0.00");
        mpEva.assertOverallSuppliersOrganization(supplier.getOrganizationId(),
                "3465.00", "0.00", "0.00");
        mpEva.assertOverallResellers("742.50", "4950.00", "495.00");
        mpEva.assertOverallResellersOrganization(reseller.getOrganizationId(),
                "742.50", "4950.00", "495.00");
        mpEva.assertOverallBrokers("0.00", "0.00", "0.00");

    }

    @Test
    public void suspendResume() throws Exception {
        testSetup.suspendResumeResellerService(
                "testSuspendResumeResellerService", 2.0D);
        TestData testData = getTestData("testSuspendResumeResellerService");
        VendorData supplier = testData.getVendor(0);

        VendorData reseller = testData.getVendor(1);
        VOServiceDetails resellerService = reseller.getService(0);
        CustomerData resellerCustomer = reseller.getCustomer(0);
        String resellerMpId = reseller.getMarketplaceId(0);

        // when
        performBillingRun(0, "2013-09-10 00:00:00");

        // then
        SupplierShareResultEvaluator supplEva = newSupplierShareResultEvaluator(
                supplier.getOrganizationKey(), "2013-08-01 00:00:00",
                "2013-09-01 00:00:00");

        supplEva.assertRevenuePerMarketplace(resellerMpId, "2483.21", "248.32",
                "49.66", "496.64", "0.00", "1688.59");

        supplEva.assertSubscriptionsRevenue(resellerMpId, resellerService,
                "2483.21");

        supplEva.assertRevenueShareDetails_ResellerService(resellerMpId,
                resellerService, "2483.21", "10.00", "248.32", "2.00", "49.66",
                "20.00", "496.64", "1688.59");
        supplEva.assertCustomerRevenueShareDetails_ResellerService(
                resellerMpId, resellerService,
                resellerCustomer.getOrganizationId(), "2483.21", "248.32",
                "49.66", "1688.59", "496.64");

        supplEva.assertRevenuePerMarketplace(resellerMpId, "2483.21", "248.32",
                "49.66", "496.64", "0.00", "1688.59");

        supplEva.assertSupplierRevenue("1688.59");
        supplEva.assertResellerRevenue("2483.21", "248.32", "49.66", "496.64",
                "1688.59");

        // then
        ResellerShareResultEvaluator resellerEva = newResellerShareResultEvaluator(
                reseller.getOrganizationKey(), "2013-08-01 00:00:00",
                "2013-09-01 00:00:00");
        resellerEva.assertSubscription(supplier.getOrganizationId(),
                "resellerSubscr", resellerService, "2483.21");
        resellerEva.assertServiceRevenue(supplier.getOrganizationId(),
                resellerService, "2483.21", "20.00", "496.64");
        resellerEva.assertServiceCustomerRevenue(supplier.getOrganizationId(),
                resellerService, resellerCustomer.getOrganizationId(),
                "2483.21", "20.00", "496.64");
        resellerEva.assertResellerRevenuePerSupplier(
                supplier.getOrganizationId(), "2483.21", "496.64", "1986.57");
        resellerEva.assertResellerRevenue("2483.21", "496.64", "1986.57");

        // then
        MarketplaceShareResultEvaluator mpEva = newMarketplaceShareResultEvaluator(
                reseller.getOrganizationKey(), "2013-08-01 00:00:00",
                "2013-09-01 00:00:00");
        mpEva.assertRevenueShareDetails_ResellerService(resellerMpId,
                resellerService, "2483.21", "10.00", "248.32", "2.00", "49.66",
                "20.00", "496.64", "1688.59");
        mpEva.assertSuppliers(resellerMpId, "1688.59", "0.00", "0.00");
        mpEva.assertSuppliersOrganization(resellerMpId,
                supplier.getOrganizationId(), "1688.59", "0.00", "0.00");
        mpEva.assertResellers(resellerMpId, "496.64", "2483.21", "248.32");
        mpEva.assertResellersOrganization(resellerMpId,
                reseller.getOrganizationId(), "496.64", "2483.21", "248.32");
        mpEva.assertBrokers(resellerMpId, "0.00", "0.00", "0.00");
        mpEva.assertMarketplaceOwner(resellerMpId, "248.32");
        mpEva.assertOverallSuppliers("1688.59", "0.00", "0.00");
        mpEva.assertOverallSuppliersOrganization(supplier.getOrganizationId(),
                "1688.59", "0.00", "0.00");
        mpEva.assertOverallResellers("496.64", "2483.21", "248.32");
        mpEva.assertOverallResellersOrganization(reseller.getOrganizationId(),
                "496.64", "2483.21", "248.32");
        mpEva.assertOverallBrokers("0.00", "0.00", "0.00");
    }

}
