/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 09.04.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.calculation.share;

import java.math.BigDecimal;

import org.junit.BeforeClass;
import org.junit.Test;

import org.oscm.billingservice.business.calculation.share.setup.SharesBrokerSetup;
import org.oscm.billingservice.evaluation.BrokerShareResultEvaluator;
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
public class SharesBrokerIT extends BillingIntegrationTestBase {

    private SharesBrokerSetup testSetup = new SharesBrokerSetup();

    @BeforeClass
    public static void setupOnce() throws Exception {
        BillingIntegrationTestBase.initialize();
        BillingIntegrationTestBase.createBasicTestData(true);
    }

    /**
     * One subscription to a broker service without customer discount
     */
    @Test
    public void subscribeToBrokerServiceNoDiscount() throws Exception {
        // given
        testSetup.subscribeToBrokerServiceNoDiscount();
        TestData testData = getTestData("subscribeToBrokerServiceNoDiscount");
        VendorData supplier = testData.getVendor(0);
        VendorData broker = testData.getVendor(1);
        CustomerData brokerCustomer = broker.getCustomer(0);
        VOServiceDetails brokerService = broker.getService(0);
        String brokerMpId = broker.getMarketplaceId(0);

        // when
        performBillingRun(0, "2013-02-05 00:00:00");

        // then
        SupplierShareResultEvaluator supplEva = newSupplierShareResultEvaluator(
                supplier.getOrganizationKey(), "2013-01-01 00:00:00",
                "2013-02-01 00:00:00");

        supplEva.assertSubscription(brokerMpId, "brokerSubscr1", brokerService,
                "4950.00");
        supplEva.assertRevenueShareDetails_BrokerService(brokerMpId,
                brokerService, "4950.00", "10.00", "495.00", "5.00", "247.50",
                "15.00", "742.50", "3465.00");
        supplEva.assertCustomerRevenueShareDetails_BrokerService(brokerMpId,
                brokerService, brokerCustomer.getOrganizationId(), "4950.00",
                "495.00", "247.50", "3465.00", "742.50");

        supplEva.assertRevenuePerMarketplace(brokerMpId, "4950.00", "495.00",
                "247.50", "0.00", "742.50", "3465.00");

        supplEva.assertSupplierRevenue("3465.00");
        supplEva.assertBrokerRevenue("4950.00", "495.00", "247.50", "742.50");

        // then
        BrokerShareResultEvaluator brokerEva = newBrokerShareResultEvaluator(
                broker.getOrganizationKey(), "2013-01-01 00:00:00",
                "2013-02-01 00:00:00");
        brokerEva.assertServiceRevenue(supplier.getOrganizationId(),
                brokerService, "4950.00", "15.00", "742.50");
        brokerEva.assertServiceCustomerRevenue(supplier.getOrganizationId(),
                brokerService, brokerCustomer.getOrganizationId(), "4950.00",
                "15.00", "742.50");
        brokerEva.assertBrokerRevenuePerSupplier(supplier.getOrganizationId(),
                "4950.00", "742.50");
        brokerEva.assertBrokerRevenue("4950.00", "742.50");

        MarketplaceShareResultEvaluator mpEva = newMarketplaceShareResultEvaluator(
                broker.getOrganizationKey(), "2013-01-01 00:00:00",
                "2013-02-01 00:00:00");

        mpEva.assertRevenueShareDetails_BrokerService(brokerMpId,
                brokerService, "4950.00", "10.00", "495.00", "5.00", "247.50",
                "15.00", "742.50", "3465.00");

        mpEva.assertSuppliers(brokerMpId, "3465.00", "0.00", "0.00");
        mpEva.assertSuppliersOrganization(brokerMpId,
                supplier.getOrganizationId(), "3465.00", "0.00", "0.00");
        mpEva.assertBrokers(brokerMpId, "742.50", "4950.00", "495.00");
        mpEva.assertBrokersOrganization(brokerMpId, broker.getOrganizationId(),
                "742.50", "4950.00", "495.00");
        mpEva.assertResellers(brokerMpId, "0.00", "0.00", "0.00");
        mpEva.assertMarketplaceOwner(brokerMpId, "495.00");
        mpEva.assertOverallSuppliers("3465.00", "0.00", "0.00");
        mpEva.assertOverallSuppliersOrganization(supplier.getOrganizationId(),
                "3465.00", "0.00", "0.00");
        mpEva.assertOverallBrokers("742.50", "4950.00", "495.00");
        mpEva.assertOverallBrokersOrganization(broker.getOrganizationId(),
                "742.50", "4950.00", "495.00");
        mpEva.assertOverallResellers("0.00", "0.00", "0.00");
    }

    /**
     * One subscription to a broker service with discount for a broker customer
     */
    @Test
    public void subscribeToBrokerServiceDiscount() throws Exception {
        // given
        testSetup.subscribeToBrokerServiceDiscount();
        TestData testData = getTestData("subscribeToBrokerServiceDiscount");
        VendorData supplier = testData.getVendor(0);
        VendorData broker = testData.getVendor(1);
        CustomerData brokerCustomer = broker.getCustomer(0);
        VOServiceDetails brokerService = broker.getService(0);
        String brokerMpId = broker.getMarketplaceId(0);

        // when
        performBillingRun(0, "2013-07-02 00:00:00");

        // then
        SupplierShareResultEvaluator supplEva = newSupplierShareResultEvaluator(
                supplier.getOrganizationKey(), "2013-06-01 00:00:00",
                "2013-07-01 00:00:00");

        supplEva.assertSubscription(brokerMpId, "brokerSubscrDiscount",
                brokerService, "3960.00");
        supplEva.assertRevenueShareDetails_BrokerService(brokerMpId,
                brokerService, "3960.00", "10.00", "396.00", "5.00", "198.00",
                "15.00", "594.00", "2772.00");
        supplEva.assertCustomerRevenueShareDetails_BrokerService(brokerMpId,
                brokerService, brokerCustomer.getOrganizationId(), "3960.00",
                "396.00", "198.00", "2772.00", "594.00");

        supplEva.assertRevenuePerMarketplace(brokerMpId, "3960.00", "396.00",
                "198.00", "0.00", "594.00", "2772.00");

        supplEva.assertSupplierRevenue("2772.00");
        supplEva.assertBrokerRevenue("3960.00", "396.00", "198.00", "594.00");

        // then
        BrokerShareResultEvaluator brokerEva = newBrokerShareResultEvaluator(
                broker.getOrganizationKey(), "2013-06-01 00:00:00",
                "2013-07-01 00:00:00");
        brokerEva.assertServiceRevenue(supplier.getOrganizationId(),
                brokerService, "3960.00", "15.00", "594.00");
        brokerEva.assertServiceCustomerRevenue(supplier.getOrganizationId(),
                brokerService, brokerCustomer.getOrganizationId(), "3960.00",
                "15.00", "594.00");
        brokerEva.assertBrokerRevenuePerSupplier(supplier.getOrganizationId(),
                "3960.00", "594.00");
        brokerEva.assertBrokerRevenue("3960.00", "594.00");

        MarketplaceShareResultEvaluator mpEva = newMarketplaceShareResultEvaluator(
                broker.getOrganizationKey(), "2013-06-01 00:00:00",
                "2013-07-01 00:00:00");

        mpEva.assertRevenueShareDetails_BrokerService(brokerMpId,
                brokerService, "3960.00", "10.00", "396.00", "5.00", "198.00",
                "15.00", "594.00", "2772.00");

        mpEva.assertSuppliers(brokerMpId, "2772.00", "0.00", "0.00");
        mpEva.assertSuppliersOrganization(brokerMpId,
                supplier.getOrganizationId(), "2772.00", "0.00", "0.00");
        mpEva.assertBrokers(brokerMpId, "594.00", "3960.00", "396.00");
        mpEva.assertBrokersOrganization(brokerMpId, broker.getOrganizationId(),
                "594.00", "3960.00", "396.00");
        mpEva.assertResellers(brokerMpId, "0.00", "0.00", "0.00");
        mpEva.assertMarketplaceOwner(brokerMpId, "396.00");
        mpEva.assertOverallSuppliers("2772.00", "0.00", "0.00");
        mpEva.assertOverallSuppliersOrganization(supplier.getOrganizationId(),
                "2772.00", "0.00", "0.00");
        mpEva.assertOverallBrokers("594.00", "3960.00", "396.00");
        mpEva.assertOverallBrokersOrganization(broker.getOrganizationId(),
                "594.00", "3960.00", "396.00");
        mpEva.assertOverallResellers("0.00", "0.00", "0.00");
    }

    @Test
    public void suspendResumeDiscount() throws Exception {
        testSetup.suspendResumeBrokerService(
                "testSuspendResumeBrokerServiceDiscount", new BigDecimal(
                        "50.00"), 2.0D);

        TestData testData = getTestData("testSuspendResumeBrokerServiceDiscount");
        VendorData supplier = testData.getVendor(0);

        VendorData broker = testData.getVendor(1);
        VOServiceDetails brokerService = broker.getService(0);
        CustomerData brokerCustomer = broker.getCustomer(0);
        String brokerMpId = broker.getMarketplaceId(0);

        // when
        performBillingRun(0, "2013-09-10 00:00:00");

        // then
        SupplierShareResultEvaluator supplEva = newSupplierShareResultEvaluator(
                supplier.getOrganizationKey(), "2013-08-01 00:00:00",
                "2013-09-01 00:00:00");

        supplEva.assertSupplierRevenue("906.37");
        supplEva.assertBrokerRevenue("1241.60", "124.16", "24.83", "186.24");

        supplEva.assertRevenuePerMarketplace(brokerMpId, "1241.60", "124.16",
                "24.83", "0.00", "186.24", "906.37");

        supplEva.assertSubscription(brokerMpId, "brokerSubscrDiscount",
                brokerService, "1241.60");
        supplEva.assertRevenueShareDetails_BrokerService(brokerMpId,
                brokerService, "1241.60", "10.00", "124.16", "2.00", "24.83",
                "15.00", "186.24", "906.37");
        supplEva.assertCustomerRevenueShareDetails_BrokerService(brokerMpId,
                brokerService, brokerCustomer.getOrganizationId(), "1241.60",
                "124.16", "24.83", "906.37", "186.24");

        // then
        BrokerShareResultEvaluator brokerEva = newBrokerShareResultEvaluator(
                broker.getOrganizationKey(), "2013-08-01 00:00:00",
                "2013-09-01 00:00:00");

        brokerEva.assertServiceRevenue(supplier.getOrganizationId(),
                brokerService, "1241.60", "15.00", "186.24");
        brokerEva.assertServiceCustomerRevenue(supplier.getOrganizationId(),
                brokerService, brokerCustomer.getOrganizationId(), "1241.60",
                "15.00", "186.24");
        brokerEva.assertBrokerRevenuePerSupplier(supplier.getOrganizationId(),
                "1241.60", "186.24");
        brokerEva.assertBrokerRevenue("1241.60", "186.24");

        MarketplaceShareResultEvaluator mpEva = newMarketplaceShareResultEvaluator(
                broker.getOrganizationKey(), "2013-08-01 00:00:00",
                "2013-09-01 00:00:00");

        // then
        mpEva.assertRevenueShareDetails_BrokerService(brokerMpId,
                brokerService, "1241.60", "10.00", "124.16", "2.00", "24.83",
                "15.00", "186.24", "906.37");
        mpEva.assertSuppliers(brokerMpId, "906.37", "0.00", "0.00");
        mpEva.assertSuppliersOrganization(brokerMpId,
                supplier.getOrganizationId(), "906.37", "0.00", "0.00");
        mpEva.assertBrokers(brokerMpId, "186.24", "1241.60", "124.16");
        mpEva.assertBrokersOrganization(brokerMpId, broker.getOrganizationId(),
                "186.24", "1241.60", "124.16");
        mpEva.assertResellers(brokerMpId, "0.00", "0.00", "0.00");
        mpEva.assertMarketplaceOwner(brokerMpId, "124.16");
        mpEva.assertOverallSuppliers("906.37", "0.00", "0.00");
        mpEva.assertOverallSuppliersOrganization(supplier.getOrganizationId(),
                "906.37", "0.00", "0.00");
        mpEva.assertOverallBrokers("186.24", "1241.60", "124.16");
        mpEva.assertOverallBrokersOrganization(broker.getOrganizationId(),
                "186.24", "1241.60", "124.16");
        mpEva.assertOverallResellers("0.00", "0.00", "0.00");
    }

}
