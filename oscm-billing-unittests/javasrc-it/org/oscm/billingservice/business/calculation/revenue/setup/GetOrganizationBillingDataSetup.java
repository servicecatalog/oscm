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
import org.oscm.billingservice.setup.VOServiceFactory;
import org.oscm.billingservice.setup.VOPriceModelFactory.TestPriceModel;
import org.oscm.billingservice.setup.VOServiceFactory.TestService;
import org.oscm.test.DateTimeHandling;
import org.oscm.internal.vo.VORoleDefinition;
import org.oscm.internal.vo.VOServiceDetails;
import org.oscm.internal.vo.VOSubscriptionDetails;

/**
 * @author malhotra
 * 
 */
public class GetOrganizationBillingDataSetup extends IntegrationTestSetup {

    /**
     * Period: March 2013, cutoff day: 20, the subscription starts at day 7
     */
    public void createDayScenario17() throws Exception {
        // Subscription activation time
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-03-07 07:00:00"));

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "SCENARIO17_PERUNIT_DAY", TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_DAY_ROLES,
                        technicalService, supplierMarketplace);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 20);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(basicSetup.getSecondCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subscriptionDetails = subscrSetup
                .subscribeToService("SCENARIO17_PERUNIT_DAY", serviceDetails,
                        basicSetup.getSecondCustomerUser1(), role);

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO17_PERUNIT_DAY", subscriptionDetails);
    }

    /**
     * Period: March 2013, cutoff day: 20, the subscription starts at day 7
     */
    public void createHourScenario17() throws Exception {
        BillingIntegrationTestBase.setDateFactoryInstance(DateTimeHandling
                .calculateMillis("2013-03-07 07:00:00"));

        VOServiceDetails serviceDetails = serviceSetup
                .createPublishAndActivateMarketableService(
                        basicSetup.getSupplierAdminKey(),
                        "SCENARIO17_PERUNIT_HOUR", TestService.EXAMPLE,
                        TestPriceModel.EXAMPLE_PERUNIT_HOUR_ROLES,
                        technicalService, supplierMarketplace);

        setCutOffDay(basicSetup.getSupplierAdminKey(), 20);

        VORoleDefinition role = VOServiceFactory.getRole(serviceDetails,
                "ADMIN");
        container.login(basicSetup.getSecondCustomerAdminKey(),
                ROLE_ORGANIZATION_ADMIN);
        VOSubscriptionDetails subscriptionDetails = subscrSetup
                .subscribeToService("SCENARIO17_PERUNIT_HOUR", serviceDetails,
                        basicSetup.getSecondCustomerUser1(), role);

        resetCutOffDay(basicSetup.getSupplierAdminKey());

        BillingIntegrationTestBase.updateSubscriptionListForTests(
                "SCENARIO17_PERUNIT_HOUR", subscriptionDetails);
    }

}
