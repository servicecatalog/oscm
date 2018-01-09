/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: Apr 15, 2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.calculation.share.setup;

import org.oscm.billingservice.setup.CustomerData;
import org.oscm.billingservice.setup.TestData;
import org.oscm.billingservice.setup.VOPriceModelFactory.TestPriceModel;
import org.oscm.billingservice.setup.VendorData;
import org.oscm.internal.vo.VOServiceDetails;

/**
 * @author farmaki
 * 
 */
public class SharesAllSellersSetup extends SharesSetup {

    /**
     * Create three subscriptions to a supplier service, a broker service and a
     * reseller service
     */
    public void setupAllSellers(String testName) throws Exception {
        VendorData supplierData = setupNewSupplier("2013-01-01 08:00:00");
        setCutOffDay(supplierData.getAdminKey(), 1);
        setDateFactory("2013-01-02 20:00:00");

        CustomerData customerData = registerCustomer(supplierData,
                "supplierCustomer");
        VOServiceDetails supplService = createPublishActivateService(
                supplierData, TestPriceModel.EXAMPLE_RATA_WEEK_ROLES,
                "supplSrv1");
        // The operator revenue share is defined for suppliers only!
        updateOperatorRevenueShare(5.0D, supplService.getKey());
        updatePartnerRevenueShares(15.0D, 20.0D, supplService);

        // Create a broker, a broker customer and a broker service
        VendorData brokerData = setupNewBroker("2013-01-03 08:10:00");
        CustomerData brokerCustomerData = registerCustomer(brokerData,
                "brokerCustomer");
        updateMarketplaceRevenueShare(10.0D, brokerData.getMarketplaceId(0));

        VOServiceDetails brokerService = grantResalePermission(supplierData,
                supplService, brokerData);
        brokerService = publishActivateService(brokerData, brokerService);

        // Create a reseller, a reseller customer and a reseller service
        VendorData resellerData = setupNewReseller("2013-01-03 08:10:00");
        setCutOffDay(resellerData.getAdminKey(), 1);
        CustomerData resellerCustomerData = registerCustomer(resellerData,
                "resellerCustomer");
        updateMarketplaceRevenueShare(10.0D, resellerData.getMarketplaceId(0));

        VOServiceDetails resellerService = grantResalePermission(supplierData,
                supplService, resellerData);
        resellerService = publishActivateService(resellerData, resellerService);

        // Create three subscriptions to the supplier service, the broker
        // service and the reseller service
        subscribe(customerData.getAdminUser(), "supplierSubscr1", supplService,
                "2013-01-04 00:00:00", "ADMIN");
        subscribe(brokerCustomerData.getAdminUser(), "brokerSubscr1",
                brokerService, "2013-01-04 00:00:00", "ADMIN");
        subscribe(resellerCustomerData.getAdminUser(), "resellerSubscr1",
                resellerService, "2013-01-04 00:00:00", "ADMIN");

        resetCutOffDay(supplierData.getAdminKey());
        resetCutOffDay(resellerData.getAdminKey());

        cacheTestData(testName, new TestData(supplierData, brokerData,
                resellerData));
    }

}
