/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: Dec 20, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.calculation.revenue.setup;

import org.oscm.billingservice.setup.BillingIntegrationTestBase;
import org.oscm.billingservice.setup.IntegrationTestSetup;
import org.oscm.billingservice.setup.VOPriceModelFactory.TestPriceModel;
import org.oscm.billingservice.setup.VOServiceFactory;
import org.oscm.billingservice.setup.VOServiceFactory.TestService;
import org.oscm.internal.vo.VORoleDefinition;
import org.oscm.internal.vo.VOServiceDetails;
import org.oscm.internal.vo.VOSubscriptionDetails;
import org.oscm.test.DateTimeHandling;

public class PerUnitMonthSetup extends IntegrationTestSetup {

    public void createPerUnitMonthOverlapping() throws Exception {
        long usageStartTime = DateTimeHandling
                .calculateMillis("2015-02-05 00:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(usageStartTime);

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "PERUNIT_MONTH_OV_SERVICE", TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_MONTH, technicalService,
                        supplierMarketplace);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 15);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(basicSetup.getCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subDetails = subscrSetup.subscribeToService(
                "PERUNIT_MONTH_OVERLAPPING", serviceDetails,
                basicSetup.getCustomerUser2(), role);

        long usageEndTime = DateTimeHandling
                .calculateMillis("2015-03-10 00:00:00");
        BillingIntegrationTestBase.setDateFactoryInstance(usageEndTime);
        subscrSetup.unsubscribeToService(subDetails.getSubscriptionId());

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "PERUNIT_MONTH_OVERLAPPING", subDetails);
    }
}
