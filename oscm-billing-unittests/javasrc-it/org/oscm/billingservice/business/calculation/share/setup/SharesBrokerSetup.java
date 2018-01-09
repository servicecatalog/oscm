/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 21.03.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.calculation.share.setup;

import java.math.BigDecimal;

import org.oscm.billingservice.setup.CustomerData;
import org.oscm.billingservice.setup.TestData;
import org.oscm.billingservice.setup.VOPriceModelFactory.TestPriceModel;
import org.oscm.billingservice.setup.VOVatRateFactory;
import org.oscm.billingservice.setup.VendorData;
import org.oscm.test.DateTimeHandling;
import org.oscm.internal.vo.VOServiceDetails;

/**
 * @author baumann
 * 
 */
public class SharesBrokerSetup extends SharesSetup {

    /**
     * Subscribe to broker service. Customer has no discount.
     */
    public void subscribeToBrokerServiceNoDiscount() throws Exception {
        VendorData supplierData = setupNewSupplier("2013-01-01 08:00:00");
        // The subscription contains the cutoff day of the product's
        // charging organization (supplier or reseller), see
        // Product.determineChargingOrganization()!
        setCutOffDay(supplierData.getAdminKey(), 1);

        setDateFactory("2013-01-02 20:00:00");
        VOServiceDetails supplService = createPublishActivateService(
                supplierData, TestPriceModel.EXAMPLE_RATA_WEEK_ROLES,
                "supplSrvForBroker");

        // The operator revenue share is defined for suppliers only!
        updateOperatorRevenueShare(5.0D, supplService.getKey());
        updatePartnerRevenueShares(15.0D, 20.0D, supplService);

        VendorData brokerData = setupNewBroker("2013-01-03 08:10:00");
        CustomerData brokerCustomerData = registerCustomer(brokerData,
                "brokerCustomer");
        updateMarketplaceRevenueShare(10.0D, brokerData.getMarketplaceId(0));

        setDateFactory("2013-01-03 10:00:00");
        VOServiceDetails brokerService = grantResalePermission(supplierData,
                supplService, brokerData);
        brokerService = publishActivateService(brokerData, brokerService);

        subscribe(brokerCustomerData.getAdminUser(), "brokerSubscr1",
                brokerService, "2013-01-04 00:00:00", "ADMIN");

        resetCutOffDay(supplierData.getAdminKey());

        cacheTestData("subscribeToBrokerServiceNoDiscount", new TestData(
                supplierData, brokerData));
    }

    /**
     * Subscribe to broker service. Customer has a discount.
     */
    public void subscribeToBrokerServiceDiscount() throws Exception {
        VendorData supplierData = setupNewSupplier("2013-05-01 08:00:00");
        // The subscription contains the cutoff day of the product's
        // charging organization (supplier or reseller), see
        // Product.determineChargingOrganization()!
        setCutOffDay(supplierData.getAdminKey(), 1);
        orgSetup.saveAllVats(supplierData.getAdminKey(),
                VOVatRateFactory.newVOVatRate(new BigDecimal("13.00")), //
                null, null);

        setDateFactory("2013-05-02 20:00:00");
        VOServiceDetails supplService = createPublishActivateService(
                supplierData, TestPriceModel.EXAMPLE_RATA_WEEK_ROLES,
                "supplSrvForBroker2");

        // The operator revenue share is defined for suppliers only!
        updateOperatorRevenueShare(5.0D, supplService.getKey());
        updatePartnerRevenueShares(15.0D, 20.0D, supplService);

        VendorData brokerData = setupNewBroker("2013-05-03 08:10:00");
        CustomerData brokerCustomerData = registerCustomer(brokerData,
                "brokerCustomer");
        updateMarketplaceRevenueShare(10.0D, brokerData.getMarketplaceId(0));

        setDateFactory("2013-05-03 10:00:00");
        VOServiceDetails brokerService = grantResalePermission(supplierData,
                supplService, brokerData);
        brokerService = publishActivateService(brokerData, brokerService);

        subscribe(brokerCustomerData.getAdminUser(), "brokerSubscrDiscount",
                brokerService, "2013-06-03 00:00:00", "ADMIN");

        updateCustomerDiscount(supplierData.getAdminKey(), brokerCustomerData,
                new BigDecimal("20.00"),
                DateTimeHandling.calculateMillis("2013-06-01 08:00:00"),
                DateTimeHandling.calculateMillis("2013-06-30 23:00:00"));

        resetCutOffDay(supplierData.getAdminKey());

        cacheTestData("subscribeToBrokerServiceDiscount", new TestData(
                supplierData, brokerData));
    }

    /**
     * Suspend and resume a broker service.
     */
    public void suspendResumeBrokerService(String testName,
            BigDecimal discount, double operatorShare) throws Exception {
        VendorData supplierData = setupNewSupplier("2013-01-01 08:00:00");
        setCutOffDay(supplierData.getAdminKey(), 1);

        setDateFactory("2013-01-02 20:00:00");
        VOServiceDetails supplService = createPublishActivateService(
                supplierData, TestPriceModel.EXAMPLE_RATA_WEEK_ROLES,
                "supplSrvForBroker");

        // The operator revenue share is defined for suppliers only!
        updateOperatorRevenueShare(operatorShare, supplService.getKey());
        updatePartnerRevenueShares(15.0D, 20.0D, supplService);

        // Subscribe to the broker service
        VendorData brokerData = setupNewBroker("2013-01-03 08:00:00");
        CustomerData brokerCustomerData = registerCustomer(brokerData,
                "brokerCustomer");
        updateMarketplaceRevenueShare(10.0D, brokerData.getMarketplaceId(0));

        setDateFactory("2013-07-10 20:00:00");
        VOServiceDetails brokerService = grantResalePermission(supplierData,
                supplService, brokerData);
        brokerService = publishActivateService(brokerData, brokerService);

        subscribe(brokerCustomerData.getAdminUser(), "brokerSubscrDiscount",
                brokerService, "2013-08-01 12:00:00", "ADMIN");

        updateCustomerDiscount(supplierData.getAdminKey(), brokerCustomerData,
                discount,
                DateTimeHandling.calculateMillis("2013-08-01 08:00:00"),
                DateTimeHandling.calculateMillis("2013-08-31 23:00:00"));

        // Suspend and resume the subscription
        setDateFactory("2013-08-10 07:00:00");
        paymentSetup.deleteCustomerPaymentTypes(supplierData,
                brokerCustomerData.getOrganization());

        setDateFactory("2013-08-27 12:00:00");
        paymentSetup.reassignCustomerPaymentTypes(supplierData,
                brokerCustomerData.getOrganization());

        resetCutOffDay(supplierData.getAdminKey());
        cacheTestData(testName, new TestData(supplierData, brokerData));
    }
}
