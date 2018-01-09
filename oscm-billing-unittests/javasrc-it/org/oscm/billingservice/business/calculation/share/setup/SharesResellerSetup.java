/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 14.04.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.calculation.share.setup;

import org.oscm.billingservice.setup.CustomerData;
import org.oscm.billingservice.setup.TestData;
import org.oscm.billingservice.setup.VOPriceModelFactory.TestPriceModel;
import org.oscm.billingservice.setup.VendorData;
import org.oscm.internal.vo.VOServiceDetails;
import org.oscm.internal.vo.VOSubscriptionDetails;

/**
 * @author baumann
 * 
 */
public class SharesResellerSetup extends SharesSetup {

    /**
     * Subscribe to reseller service. Customer has no discount.
     */
    public void subscribeToResellerServiceNoDiscount() throws Exception {
        VendorData supplierData = setupNewSupplier("2013-01-01 08:00:00");
        setDateFactory("2013-01-02 20:00:00");
        VOServiceDetails supplService = createPublishActivateService(
                supplierData, TestPriceModel.EXAMPLE_RATA_WEEK_ROLES,
                "supplSrvForReseller");

        // The operator revenue share is defined for suppliers only!
        updateOperatorRevenueShare(5.0D, supplService.getKey());
        updatePartnerRevenueShares(12.0D, 15.0D, supplService);

        VendorData resellerData = setupNewReseller("2013-01-03 08:10:00");
        setCutOffDay(resellerData.getAdminKey(), 2);
        CustomerData resellerCustomerData = registerCustomer(resellerData,
                "resellerCustomer");
        updateMarketplaceRevenueShare(10.0D, resellerData.getMarketplaceId(0));

        VOServiceDetails resellerService = grantResalePermission(supplierData,
                supplService, resellerData);
        resellerService = publishActivateService(resellerData, resellerService);

        subscribe(resellerCustomerData.getAdminUser(), "resellerSubscr1",
                resellerService, "2013-01-05 00:00:00", "ADMIN");

        resetCutOffDay(resellerData.getAdminKey());

        cacheTestData("subscribeToResellerServiceNoDiscount", new TestData(
                supplierData, resellerData));
    }

    /**
     * Subscribe to reseller service. Terminate the subscription.
     */
    public void subscribeToResellerServiceTerminate() throws Exception {
        VendorData supplierData = setupNewSupplier("2013-03-01 08:00:00");
        setDateFactory("2013-03-02 20:00:00");
        VOServiceDetails supplService = createPublishActivateService(
                supplierData, TestPriceModel.EXAMPLE_RATA_WEEK_ROLES,
                "supplSrvForReseller2");

        // The operator revenue share is defined for suppliers only!
        updateOperatorRevenueShare(5.0D, supplService.getKey());
        updatePartnerRevenueShares(12.0D, 15.0D, supplService);

        VendorData resellerData = setupNewReseller("2013-03-03 08:10:00");
        setCutOffDay(resellerData.getAdminKey(), 2);
        CustomerData resellerCustomerData = registerCustomer(resellerData,
                "resellerCustomer");
        updateMarketplaceRevenueShare(10.0D, resellerData.getMarketplaceId(0));

        VOServiceDetails resellerService = grantResalePermission(supplierData,
                supplService, resellerData);
        resellerService = publishActivateService(resellerData, resellerService);

        VOSubscriptionDetails resellerSub = subscribe(
                resellerCustomerData.getAdminUser(),
                "resellerSubWithTerminate", resellerService,
                "2013-04-02 00:00:00", "ADMIN");

        unsubscribe(resellerCustomerData.getAdminKey(),
                resellerSub.getSubscriptionId(), "2013-04-30 00:00:00");

        resetCutOffDay(resellerData.getAdminKey());

        cacheTestData("subscribeToResellerServiceTerminate", new TestData(
                supplierData, resellerData));
    }

    /**
     * Suspend and resume a reseller service.
     */
    public void suspendResumeResellerService(String testName,
            double operatorShare) throws Exception {
        VendorData supplierData = setupNewSupplier("2013-01-01 08:00:00");
        setCutOffDay(supplierData.getAdminKey(), 1);

        setDateFactory("2013-01-02 20:00:00");
        VOServiceDetails supplService = createPublishActivateService(
                supplierData, TestPriceModel.EXAMPLE_RATA_WEEK_ROLES,
                "supplSrvForReseller");

        // The operator revenue share is defined for suppliers only!
        updateOperatorRevenueShare(operatorShare, supplService.getKey());
        updatePartnerRevenueShares(15.0D, 20.0D, supplService);

        // Subscribe to the broker service
        VendorData resellerData = setupNewReseller("2013-01-03 08:00:00");
        setCutOffDay(resellerData.getAdminKey(), 1);
        CustomerData resellerCustomerData = registerCustomer(resellerData,
                "resellerCustomer");
        updateMarketplaceRevenueShare(10.0D, resellerData.getMarketplaceId(0));

        setDateFactory("2013-07-10 20:00:00");
        VOServiceDetails resellerService = grantResalePermission(supplierData,
                supplService, resellerData);
        resellerService = publishActivateService(resellerData, resellerService);

        subscribe(resellerCustomerData.getAdminUser(), "resellerSubscr",
                resellerService, "2013-08-01 12:00:00", "ADMIN");

        // Suspend and resume the subscription
        setDateFactory("2013-08-10 07:00:00");
        paymentSetup.deleteCustomerPaymentTypes(resellerData, resellerCustomerData
                .getOrganization());

        setDateFactory("2013-08-27 12:00:00");
        paymentSetup.reassignCustomerPaymentTypes(resellerData, resellerCustomerData
                .getOrganization());

        resetCutOffDay(supplierData.getAdminKey());
        resetCutOffDay(resellerData.getAdminKey());

        cacheTestData(testName, new TestData(supplierData, resellerData));
    }
}
