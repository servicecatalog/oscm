/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 14.08.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.calculation.revenue;

import static org.junit.Assert.assertNull;

import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Node;

import org.oscm.billingservice.business.calculation.revenue.setup.EventSetup;
import org.oscm.billingservice.evaluation.BillingResultEvaluator;
import org.oscm.billingservice.evaluation.BillingXMLNodeSearch;
import org.oscm.billingservice.setup.BillingIntegrationTestBase;
import org.oscm.test.DateTimeHandling;
import org.oscm.internal.vo.VOSubscriptionDetails;

/**
 * @author kulle
 * 
 */
public class EventIT extends BillingIntegrationTestBase {

    private EventSetup testSetup = new EventSetup();

    @BeforeClass
    public static void setupOnce() throws Exception {
        BillingIntegrationTestBase.initialize();
        BillingIntegrationTestBase.createBasicTestData(true);
    }

    @Test
    public void bug10248_cutoffday_eventAfterStartOfNextMonth()
            throws Exception {

        // given
        testSetup.bug10248_cutoffday_eventAfterStartOfNextMonth();
        String subscriptionId = "bug10248_1";

        // when
        performBillingRun(0, "2013-07-01 00:00:00");

        // then
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                subscriptionId, 0);
        BillingResultEvaluator eva = getEvaluator(
                voSubscriptionDetails.getKey(), "2013-05-03 00:00:00",
                "2013-06-03 00:00:00");
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(eva
                .getBillingResult(), voSubscriptionDetails.getPriceModel()
                .getKey());
        Node roleCosts = BillingXMLNodeSearch.getRoleCostsNode(priceModel);
        eva.assertRoleCost(roleCosts, "ADMIN", "6.00", 5.0, "30.00");
        eva.assertUserAssignmentCostsFactor(priceModel,
                "GreenPeaceCustomerUser1", "5.0");
        Node eventCosts = BillingXMLNodeSearch
                .getGatheredEventsNode(priceModel);
        eva.assertEventCosts(eventCosts, "FILE_DOWNLOAD", "10.00", "2", "20.00");
        eva.assertOverallCosts("EUR", "550.00");
    }

    @Test
    public void billingPerUnitWeekBug10248_free_period_Event_2()
            throws Exception {

        // given
        testSetup.createWeekBug10248_2_free_period_and_event();

        // when
        performBillingRun(0, "2013-06-06 07:00:00");

        // then - assert period 1
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "BUG10248_2_FREE_UNIT_WEEK_EVENT", 0);
        BillingResultEvaluator eva = getEvaluator(
                voSubscriptionDetails.getKey(), "2013-05-03 00:00:00",
                "2013-06-03 00:00:00");
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(eva
                .getBillingResult(), voSubscriptionDetails.getPriceModel()
                .getKey());
        Node eventCosts = BillingXMLNodeSearch
                .getGatheredEventsNode(priceModel);
        eva.assertEventCosts(eventCosts, "FILE_UPLOAD", "10.00", "2", "20.00");
        eva.assertGatheredEventsCosts(priceModel, "20.00");

        // then - assert period 2
        voSubscriptionDetails = getSubscriptionDetails(
                "BUG10248_2_FREE_UNIT_WEEK_EVENT", 0);
        eva = getEvaluator(voSubscriptionDetails.getKey(),
                "2013-04-03 00:00:00", "2013-05-03 00:00:00");
        priceModel = BillingXMLNodeSearch.getPriceModelNode(eva
                .getBillingResult(), voSubscriptionDetails.getPriceModel()
                .getKey());
        eventCosts = BillingXMLNodeSearch.getGatheredEventsNode(priceModel);
        eva.assertEventCosts(eventCosts, "FILE_DOWNLOAD", "10.00", "2", "20.00");
        eva.assertGatheredEventsCosts(priceModel, "20.00");
        eva.assertOneTimeFee(priceModel, "25.00");
    }

    @Test
    public void billingPerRataWeekBug10248_free_period_Event() throws Exception {
        // given
        testSetup.createWeekBug10248_Rata_free_period_and_event();

        // when
        performBillingRun(0, "2013-06-06 07:00:00");

        // then
        VOSubscriptionDetails sub = getSubscriptionDetails(
                "BUG10248_RATA_WEEK_FREE_EVENT", 0);
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2013-05-03 00:00:00", "2013-06-03 00:00:00");
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(), sub.getPriceModel().getKey());
        Node eventCosts = BillingXMLNodeSearch
                .getGatheredEventsNode(priceModel);
        eva.assertEventCosts(eventCosts, "FILE_UPLOAD", "10.00", "2", "20.00");
        eva.assertGatheredEventsCosts(priceModel, "20.00");

        sub = getSubscriptionDetails("BUG10248_RATA_WEEK_FREE_EVENT", 0);
        eva = getEvaluator(sub.getKey(), "2013-04-03 00:00:00",
                "2013-05-03 00:00:00");
        priceModel = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(), sub.getPriceModel().getKey());
        eventCosts = BillingXMLNodeSearch.getGatheredEventsNode(priceModel);
        eva.assertEventCosts(eventCosts, "FILE_DOWNLOAD", "10.00", "2", "20.00");
        eva.assertGatheredEventsCosts(priceModel, "20.00");
        eva.assertOneTimeFee(priceModel, "25.00");
    }

    @Test
    public void billingPerUniMonthBug10248_free_period_Event() throws Exception {

        // given
        testSetup.createWeekBug10248_month_free_period_and_event();

        // when
        performBillingRun(0, "2013-06-06 07:00:00");

        // then - assert period 1
        VOSubscriptionDetails sub = getSubscriptionDetails(
                "BUG10248_FREE_UNIT_MONTH_EVENT", 0);
        BillingResultEvaluator eva = getEvaluator(sub.getKey(),
                "2013-05-03 00:00:00", "2013-06-03 00:00:00");
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(), sub.getPriceModel().getKey());
        Node eventCosts = BillingXMLNodeSearch
                .getGatheredEventsNode(priceModel);
        eva.assertEventCosts(eventCosts, "FILE_UPLOAD", "10.00", "2", "20.00");
        eva.assertGatheredEventsCosts(priceModel, "20.00");

        // then - assert period 2
        sub = getSubscriptionDetails("BUG10248_FREE_UNIT_MONTH_EVENT", 0);
        eva = getEvaluator(sub.getKey(), "2013-04-03 00:00:00",
                "2013-05-03 00:00:00");
        priceModel = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(), sub.getPriceModel().getKey());
        eventCosts = BillingXMLNodeSearch.getGatheredEventsNode(priceModel);
        eva.assertEventCosts(eventCosts, "FILE_DOWNLOAD", "10.00", "2", "20.00");
        eva.assertGatheredEventsCosts(priceModel, "20.00");
        eva.assertOneTimeFee(priceModel, "123.00");
    }

    @Test
    public void billingPerUnitWeekBug10248_free_period_Event_3()
            throws Exception {

        // given
        testSetup.createWeekBug10248_3_free_period_and_event();

        // when
        performBillingRun(0, "2013-06-06 07:00:00");

        // then - period 1
        VOSubscriptionDetails voSubscriptionDetails = getSubscriptionDetails(
                "BUG10248_3_FREE_UNIT_WEEK_EVENT", 0);
        assertNull(getEvaluator(voSubscriptionDetails.getKey(),
                "2013-05-06 00:00:00", "2013-06-06 00:00:00"));

        // then - period 2
        voSubscriptionDetails = getSubscriptionDetails(
                "BUG10248_3_FREE_UNIT_WEEK_EVENT", 0);
        BillingResultEvaluator eva = getEvaluator(
                voSubscriptionDetails.getKey(), "2013-04-06 00:00:00",
                "2013-05-06 00:00:00");
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(eva
                .getBillingResult(), voSubscriptionDetails.getPriceModel()
                .getKey());
        Node eventCosts = BillingXMLNodeSearch
                .getGatheredEventsNode(priceModel);
        eva.assertEventCosts(eventCosts, "FILE_UPLOAD", "10.00", "2", "20.00");
        eva.assertGatheredEventsCosts(priceModel, "20.00");
        eva.assertOneTimeFee(priceModel, "25.00");
    }

    /**
     * This tests ensures that events which occurred within suspend blocks are
     * not used by the billing algorithm to charge a customer. In this case the
     * FILE_UPLOAD event at 2013-07-27 13:00:00 is ignored because it happened
     * within a suspend block.
     */
    @Test
    public void ignoreEventsWithinSuspendBlock() throws Exception {
        // given
        testSetup.ignoreEventsWithinSuspendBlock();

        // when
        performBillingRun(0, "2013-08-08 07:00:00");

        // then - subscription 2 - price model 2
        VOSubscriptionDetails subscr = getSubscriptionDetails("EVENT1", 0);
        BillingResultEvaluator eva = getEvaluator(subscr.getKey(),
                "2013-07-01 00:00:00", "2013-08-01 00:00:00");
        Node priceModel = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(),
                DateTimeHandling.calculateMillis("2013-07-30 13:00:00"),
                DateTimeHandling.calculateMillis("2013-07-30 13:00:00"));
        Node eventCosts = BillingXMLNodeSearch
                .getGatheredEventsNode(priceModel);
        eva.assertEventCosts(eventCosts, "FILE_UPLOAD", "10.00", "1", "10.00");
        eva.assertGatheredEventsCosts(priceModel, "10.00");

        // then - subscription 2 - price model 1
        priceModel = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(),
                DateTimeHandling.calculateMillis("2013-07-29 11:00:00"),
                DateTimeHandling.calculateMillis("2013-07-29 11:00:00"));
        eventCosts = BillingXMLNodeSearch.getGatheredEventsNode(priceModel);
        eva.assertEventCosts(eventCosts, "FOLDER_NEW", "10.00", "1", "10.00");
        eva.assertGatheredEventsCosts(priceModel, "10.00");

        // then - subscription 1 - price model 1
        priceModel = BillingXMLNodeSearch.getPriceModelNode(
                eva.getBillingResult(),
                DateTimeHandling.calculateMillis("2013-07-20 13:00:00"),
                DateTimeHandling.calculateMillis("2013-07-26 13:00:00"));
        eventCosts = BillingXMLNodeSearch.getGatheredEventsNode(priceModel);
        eva.assertEventCosts(eventCosts, "FILE_DOWNLOAD", "10.00", "1", "10.00");
        eva.assertGatheredEventsCosts(priceModel, "10.00");
    }

}
