/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: Apr 15, 2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.calculation.share;

import org.junit.BeforeClass;
import org.junit.Test;

import org.oscm.billingservice.business.calculation.share.setup.SharesAllSellersSetup;
import org.oscm.billingservice.evaluation.BrokerShareResultEvaluator;
import org.oscm.billingservice.evaluation.MarketplaceShareResultEvaluator;
import org.oscm.billingservice.evaluation.ResellerShareResultEvaluator;
import org.oscm.billingservice.evaluation.SupplierShareResultEvaluator;
import org.oscm.billingservice.setup.BillingIntegrationTestBase;
import org.oscm.billingservice.setup.CustomerData;
import org.oscm.billingservice.setup.TestData;
import org.oscm.billingservice.setup.VendorData;
import org.oscm.internal.vo.VOServiceDetails;

/**
 * @author farmaki
 * 
 */
public class SharesAllSellersIT extends BillingIntegrationTestBase {

    private SharesAllSellersSetup testSetup = new SharesAllSellersSetup();

    @BeforeClass
    public static void setupOnce() throws Exception {
        BillingIntegrationTestBase.initialize();
        BillingIntegrationTestBase.createBasicTestData(true);
    }

    /**
     * Test the revenue share results for all sellers (supplier, broker
     * reseller, mp owner)
     */
    @Test
    public void sharesForAllSellers() throws Exception {
        // given
        testSetup.setupAllSellers("testSharesForAllSellers");
        TestData testData = getTestData("testSharesForAllSellers");

        VendorData supplier = testData.getVendor(0);
        VendorData broker = testData.getVendor(1);
        VendorData reseller = testData.getVendor(2);

        VOServiceDetails supplierService = supplier.getService(0);
        VOServiceDetails brokerService = broker.getService(0);
        VOServiceDetails resellerService = reseller.getService(0);

        String supplierMpId = supplier.getMarketplaceId(0);
        String brokerMpId = broker.getMarketplaceId(0);
        String resellerMpId = reseller.getMarketplaceId(0);

        // when
        performBillingRun(0, "2013-02-05 00:00:00");

        // then verify all Revenue Share Results
        assertSupplierRevenueShareResult_sharesForAllSellers(supplier, broker,
                reseller, "2013-01-01 00:00:00", "2013-02-01 00:00:00");
        assertBrokerRevenueShareResult_sharesForAllSellers(supplier, broker,
                "2013-01-01 00:00:00", "2013-02-01 00:00:00");
        assertResellerRevenueShareResult_sharesForAllSellers(supplier,
                reseller, "2013-01-01 00:00:00", "2013-02-01 00:00:00");

        // verify MarketplaceOwnerRevenueShareResult for supplier
        MarketplaceShareResultEvaluator mpEvaSupplier = newMarketplaceShareResultEvaluator(
                supplier.getOrganizationKey(), "2013-01-01 00:00:00",
                "2013-02-01 00:00:00");

        mpEvaSupplier.assertRevenueShareDetails(supplierMpId, supplierService,
                "4950.00", "0.00", "0.00", "5.00", "247.50", "4702.50");
        mpEvaSupplier.assertSuppliers(supplierMpId, "4702.50", "4950.00",
                "0.00");
        mpEvaSupplier.assertSuppliersOrganization(supplierMpId,
                supplier.getOrganizationId(), "4702.50", "4950.00", "0.00");
        mpEvaSupplier.assertBrokers(supplierMpId, "0.00", "0.00", "0.00");
        mpEvaSupplier.assertResellers(supplierMpId, "0.00", "0.00", "0.00");
        mpEvaSupplier.assertMarketplaceOwner(supplierMpId, "0.00");
        mpEvaSupplier.assertOverallSuppliers("4702.50", "4950.00", "0.00");
        mpEvaSupplier.assertOverallSuppliersOrganization(
                supplier.getOrganizationId(), "4702.50", "4950.00", "0.00");
        mpEvaSupplier.assertOverallBrokers("0.00", "0.00", "0.00");
        mpEvaSupplier.assertOverallResellers("0.00", "0.00", "0.00");

        // verify MarketplaceOwnerRevenueShareResult for broker
        MarketplaceShareResultEvaluator mpEvaBroker = newMarketplaceShareResultEvaluator(
                broker.getOrganizationKey(), "2013-01-01 00:00:00",
                "2013-02-01 00:00:00");

        mpEvaBroker.assertRevenueShareDetails_BrokerService(brokerMpId,
                brokerService, "4950.00", "10.00", "495.00", "5.00", "247.50",
                "15.00", "742.50", "3465.00");
        mpEvaBroker.assertSuppliers(brokerMpId, "3465.00", "0.00", "0.00");
        mpEvaBroker.assertSuppliersOrganization(brokerMpId,
                supplier.getOrganizationId(), "3465.00", "0.00", "0.00");
        mpEvaBroker.assertBrokers(brokerMpId, "742.50", "4950.00", "495.00");
        mpEvaBroker.assertBrokersOrganization(brokerMpId,
                broker.getOrganizationId(), "742.50", "4950.00", "495.00");
        mpEvaBroker.assertResellers(brokerMpId, "0.00", "0.00", "0.00");
        mpEvaBroker.assertMarketplaceOwner(brokerMpId, "495.00");
        mpEvaBroker.assertOverallSuppliers("3465.00", "0.00", "0.00");
        mpEvaBroker.assertOverallSuppliersOrganization(
                supplier.getOrganizationId(), "3465.00", "0.00", "0.00");
        mpEvaBroker.assertOverallBrokers("742.50", "4950.00", "495.00");
        mpEvaBroker.assertOverallBrokersOrganization(
                broker.getOrganizationId(), "742.50", "4950.00", "495.00");
        mpEvaBroker.assertOverallResellers("0.00", "0.00", "0.00");

        // verify MarketplaceOwnerRevenueShareResult for reseller
        MarketplaceShareResultEvaluator mpEvaReseller = newMarketplaceShareResultEvaluator(
                reseller.getOrganizationKey(), "2013-01-01 00:00:00",
                "2013-02-01 00:00:00");

        mpEvaReseller.assertRevenueShareDetails_ResellerService(resellerMpId,
                resellerService, "4950.00", "10.00", "495.00", "5.00",
                "247.50", "20.00", "990.00", "3217.50");
        mpEvaReseller.assertSuppliers(resellerMpId, "3217.50", "0.00", "0.00");
        mpEvaReseller.assertSuppliersOrganization(resellerMpId,
                supplier.getOrganizationId(), "3217.50", "0.00", "0.00");
        mpEvaReseller.assertResellers(resellerMpId, "990.00", "4950.00",
                "495.00");
        mpEvaReseller.assertResellersOrganization(resellerMpId,
                reseller.getOrganizationId(), "990.00", "4950.00", "495.00");
        mpEvaReseller.assertBrokers(resellerMpId, "0.00", "0.00", "0.00");
        mpEvaReseller.assertMarketplaceOwner(resellerMpId, "495.00");
        mpEvaReseller.assertOverallSuppliers("3217.50", "0.00", "0.00");
        mpEvaReseller.assertOverallSuppliersOrganization(
                supplier.getOrganizationId(), "3217.50", "0.00", "0.00");
        mpEvaReseller.assertOverallResellers("990.00", "4950.00", "495.00");
        mpEvaReseller.assertOverallResellersOrganization(
                reseller.getOrganizationId(), "990.00", "4950.00", "495.00");
        mpEvaReseller.assertOverallBrokers("0.00", "0.00", "0.00");

    }

    private void assertResellerRevenueShareResult_sharesForAllSellers(
            VendorData supplier, VendorData reseller, String periodStart,
            String periodEnd) throws Exception {
        CustomerData resellerCustomer = reseller.getCustomer(0);
        VOServiceDetails resellerService = reseller.getService(0);

        ResellerShareResultEvaluator resellerEva = newResellerShareResultEvaluator(
                reseller.getOrganizationKey(), periodStart, periodEnd);

        resellerEva.assertSubscription(supplier.getOrganizationId(),
                "resellerSubscr1", resellerService, "4950.00");
        resellerEva.assertServiceRevenue(supplier.getOrganizationId(),
                resellerService, "4950.00", "20.00", "990.00");
        resellerEva.assertServiceCustomerRevenue(supplier.getOrganizationId(),
                resellerService, resellerCustomer.getOrganizationId(),
                "4950.00", "20.00", "990.00");
        resellerEva.assertResellerRevenuePerSupplier(
                supplier.getOrganizationId(), "4950.00", "990.00", "3960.00");
        resellerEva.assertResellerRevenue("4950.00", "990.00", "3960.00");
    }

    private void assertBrokerRevenueShareResult_sharesForAllSellers(
            VendorData supplier, VendorData broker, String periodStart,
            String periodEnd) throws Exception {
        CustomerData brokerCustomer = broker.getCustomer(0);
        VOServiceDetails brokerService = broker.getService(0);

        BrokerShareResultEvaluator brokerEva = newBrokerShareResultEvaluator(
                broker.getOrganizationKey(), periodStart, periodEnd);

        brokerEva.assertServiceRevenue(supplier.getOrganizationId(),
                brokerService, "4950.00", "15.00", "742.50");
        brokerEva.assertServiceCustomerRevenue(supplier.getOrganizationId(),
                brokerService, brokerCustomer.getOrganizationId(), "4950.00",
                "15.00", "742.50");
        brokerEva.assertBrokerRevenuePerSupplier(supplier.getOrganizationId(),
                "4950.00", "742.50");
        brokerEva.assertBrokerRevenue("4950.00", "742.50");
    }

    private void assertSupplierRevenueShareResult_sharesForAllSellers(
            VendorData supplier, VendorData broker, VendorData reseller,
            String periodStart, String periodEnd) throws Exception {
        CustomerData supplierCustomer = supplier.getCustomer(0);
        CustomerData brokerCustomer = broker.getCustomer(0);

        VOServiceDetails supplierService = supplier.getService(0);
        VOServiceDetails brokerService = broker.getService(0);
        VOServiceDetails resellerService = reseller.getService(0);

        String supplierMpId = supplier.getMarketplaceId(0);
        String brokerMpId = broker.getMarketplaceId(0);
        String resellerMpId = reseller.getMarketplaceId(0);

        SupplierShareResultEvaluator supplEva = newSupplierShareResultEvaluator(
                supplier.getOrganizationKey(), periodStart, periodEnd);

        supplEva.assertRevenuePerMarketplace(supplierMpId, "4950.00", "0.00",
                "247.50", "0.00", "0.00", "4702.50");
        supplEva.assertRevenuePerMarketplace(brokerMpId, "4950.00", "495.00",
                "247.50", "0.00", "742.50", "3465.00");
        supplEva.assertRevenuePerMarketplace(resellerMpId, "4950.00", "495.00",
                "247.50", "990.00", "0.00", "3217.50");

        supplEva.assertRevenueShareDetails(supplierMpId, supplierService,
                "4950.00", "0.00", "0.00", "5.00", "247.50", "4702.50");
        supplEva.assertRevenueShareDetails_BrokerService(brokerMpId,
                brokerService, "4950.00", "10.00", "495.00", "5.00", "247.50",
                "15.00", "742.50", "3465.00");

        supplEva.assertCustomerRevenueShareDetails(supplierMpId,
                supplierService, supplierCustomer.getOrganization()
                        .getOrganizationId(), "4950.00", "0.00", "247.50",
                "4702.50");
        supplEva.assertCustomerRevenueShareDetails_BrokerService(brokerMpId,
                brokerService, brokerCustomer.getOrganizationId(), "4950.00",
                "495.00", "247.50", "3465.00", "742.50");

        supplEva.assertSubscription(supplierMpId, "supplierSubscr1",
                supplierService, "4950.00");
        supplEva.assertSubscription(brokerMpId, "brokerSubscr1", brokerService,
                "4950.00");
        supplEva.assertSubscriptionsRevenue(resellerMpId, resellerService,
                "4950.00");

        supplEva.assertSupplierRevenue("11385.00");
        supplEva.assertDirectRevenue("4950.00", "0.00", "247.50");
        supplEva.assertBrokerRevenue("4950.00", "495.00", "247.50", "742.50");
        supplEva.assertResellerRevenue("4950.00", "495.00", "247.50", "990.00",
                "3217.50");
    }

}
