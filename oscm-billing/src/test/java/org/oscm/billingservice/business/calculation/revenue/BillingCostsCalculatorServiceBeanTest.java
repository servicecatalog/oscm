/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: Mike J&auml;ger                                                      
 *                                                                              
 *  Creation Date: 11.05.2010                                                      
 *                                                                              
 *  Completion Time: 19.09.2011                                          
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business.calculation.revenue;

import static org.oscm.test.BigDecimalAsserts.checkEquals;
import static org.oscm.test.Numbers.BD10;
import static org.oscm.test.Numbers.BD100;
import static org.oscm.test.Numbers.BD1000;
import static org.oscm.test.Numbers.BD1900;
import static org.oscm.test.Numbers.BD2;
import static org.oscm.test.Numbers.BD20;
import static org.oscm.test.Numbers.BD200;
import static org.oscm.test.Numbers.BD40;
import static org.oscm.test.Numbers.BD5;
import static org.oscm.test.Numbers.BD50;
import static org.oscm.test.Numbers.BD6;
import static org.oscm.test.Numbers.BD7;
import static org.oscm.test.Numbers.BD8;
import static org.oscm.test.Numbers.BD80;
import static org.oscm.test.Numbers.BD9;
import static org.oscm.test.Numbers.BD90;
import static org.oscm.test.Numbers.L0;
import static org.oscm.test.Numbers.L1;
import static org.oscm.test.Numbers.L10;
import static org.oscm.test.Numbers.L15;
import static org.oscm.test.Numbers.L2;
import static org.oscm.test.Numbers.L20;
import static org.oscm.test.Numbers.L3;
import static org.oscm.test.Numbers.L4;
import static org.oscm.test.Numbers.L5;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import org.oscm.billingservice.business.calculation.revenue.CostCalculator;
import org.oscm.billingservice.business.calculation.revenue.model.EventCosts;
import org.oscm.billingservice.dao.model.EventCount;
import org.oscm.billingservice.dao.model.EventPricingData;
import org.oscm.billingservice.dao.model.RolePricingDetails;
import org.oscm.billingservice.dao.model.SteppedPriceData;
import org.oscm.billingservice.dao.model.SteppedPriceDetail;
import org.oscm.billingservice.dao.model.VatRateDetails;
import org.oscm.domobjects.PricedProductRoleHistory;
import org.oscm.domobjects.SteppedPriceHistory;
import org.oscm.test.Numbers;
import org.oscm.internal.types.exception.IllegalArgumentException;

/**
 * @author Mike J&auml;ger
 * 
 */
public class BillingCostsCalculatorServiceBeanTest {

    CostCalculator calculator = mock(CostCalculator.class,
            Mockito.CALLS_REAL_METHODS);

    /**
     * Test for cost calculation for events. Exception is expected.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testDetermineCostsForGatheredEventsInPeriodNullInput() {
        Map<String, EventPricingData> eventPrices = null;
        List<EventCount> eventStatistics = null;

        calculator.calculateCostsForGatheredEventsInPeriod(eventPrices,
                eventStatistics);
    }

    /**
     * Test for cost calculation for events. Exception is expected.
     */
    @Test
    public void testDetermineCostsForGatheredEventsInPeriod() {
        String strEventId = "event1";
        int numberOfOccurrences = 100;
        BigDecimal totalCosts = BD200;
        BigDecimal totalCostsForOneEvent = BD200;
        BigDecimal priceForOneEvent = BD2;

        EventPricingData eventPricingData = new EventPricingData();
        eventPricingData.setPrice(priceForOneEvent);
        // Set the event key for the eventPricingData.
        eventPricingData.setEventKey(1);

        EventCount eventCount = new EventCount();
        eventCount.setEventIdentifier(strEventId);
        eventCount.setNumberOfOccurrences(numberOfOccurrences);

        Map<String, EventPricingData> eventPrices = new HashMap<String, EventPricingData>();
        eventPrices.put(strEventId, eventPricingData);

        List<EventCount> eventStatistics = new ArrayList<EventCount>();
        eventStatistics.add(eventCount);

        EventCosts eventCosts = calculator
                .calculateCostsForGatheredEventsInPeriod(eventPrices,
                        eventStatistics);

        List<EventCount> actualEvents = eventCosts.getEventCountList();

        // Check if the correct event key is set in eventPricingData.
        Assert.assertEquals("The event key is wrong.", eventPricingData
                .getEventKey(), actualEvents.get(0).getEventKey());

        // total costs
        checkEquals("Total costs is wrong.", totalCosts,
                eventCosts.getTotalCosts(), 2);

        // number of events
        Assert.assertEquals("Number of events is wrong.", 1,
                actualEvents.size());

        // number of occurrences
        Assert.assertEquals("Number of occurrences is wrong.",
                numberOfOccurrences, actualEvents.get(0)
                        .getNumberOfOccurrences());

        // price for one event
        Assert.assertEquals("Price for one event is wrong.", priceForOneEvent,
                actualEvents.get(0).getPriceForOneEvent());

        // total price for one event
        Assert.assertEquals("Price for one event is wrong.",
                totalCostsForOneEvent, actualEvents.get(0)
                        .getPriceForEventsWithEventIdentifier());

    }

    /**
     * Test for cost calculation for events.
     */
    @Test
    public void testDetermineCostsForGatheredEventsInPeriod_fraction() {
        String strEventId = "event1";
        int numberOfOccurrences = 100;
        BigDecimal totalCosts = new BigDecimal("220.00");
        BigDecimal totalCostsForOneEvent = new BigDecimal("220.00");
        BigDecimal priceForOneEvent = new BigDecimal("2.2");

        EventPricingData eventPricingData = new EventPricingData();
        eventPricingData.setPrice(priceForOneEvent);
        // Set the event key for the eventPricingData.
        eventPricingData.setEventKey(1);

        EventCount eventCount = new EventCount();
        eventCount.setEventIdentifier(strEventId);
        eventCount.setNumberOfOccurrences(numberOfOccurrences);

        Map<String, EventPricingData> eventPrices = new HashMap<String, EventPricingData>();
        eventPrices.put(strEventId, eventPricingData);

        List<EventCount> eventStatistics = new ArrayList<EventCount>();
        eventStatistics.add(eventCount);

        EventCosts eventCosts = calculator
                .calculateCostsForGatheredEventsInPeriod(eventPrices,
                        eventStatistics);

        List<EventCount> actualEvents = eventCosts.getEventCountList();

        // Check if the correct event key is set in eventPricingData.
        Assert.assertEquals("The event key is wrong.", eventPricingData
                .getEventKey(), actualEvents.get(0).getEventKey());

        // total costs
        checkEquals("Total costs is wrong.", totalCosts,
                eventCosts.getTotalCosts(), 2);

        // number of events
        Assert.assertEquals("Number of events is wrong.", 1,
                actualEvents.size());

        // number of occurrences
        Assert.assertEquals("Number of occurrences is wrong.",
                numberOfOccurrences, actualEvents.get(0)
                        .getNumberOfOccurrences());

        // price for one event
        Assert.assertEquals("Price for one event is wrong.", priceForOneEvent,
                actualEvents.get(0).getPriceForOneEvent());

        // total price for one event
        checkEquals("Price for one event is wrong.", totalCostsForOneEvent,
                actualEvents.get(0).getPriceForEventsWithEventIdentifier());

    }

    /**
     * Test for cost calculation for events. Exception is expected.
     */
    @Test
    public void testDetermineCostsForGatheredEventsInPeriodManyEvents() {
        String strEventId = "event1";
        BigDecimal totalCosts = new BigDecimal("300.00");

        int numberOfOccurrences = 100;
        long totalCostsForOneEvent = 200;
        long priceForOneEvent = 2;

        String strEventId2 = "event2";
        int numberOfOccurrences2 = 100;
        long totalCostsForOneEvent2 = 100;
        long priceForOneEvent2 = 1;

        EventPricingData eventPricingData = new EventPricingData();
        eventPricingData.setPrice(BigDecimal.valueOf(priceForOneEvent));

        EventPricingData eventPricingData2 = new EventPricingData();
        eventPricingData2.setPrice(BigDecimal.valueOf(priceForOneEvent2));

        Map<String, EventPricingData> eventPrices = new HashMap<String, EventPricingData>();
        eventPrices.put(strEventId, eventPricingData);
        eventPrices.put(strEventId2, eventPricingData2);

        EventCount eventCount = new EventCount();
        eventCount.setEventIdentifier(strEventId);
        eventCount.setNumberOfOccurrences(numberOfOccurrences);

        EventCount eventCount2 = new EventCount();
        eventCount2.setEventIdentifier(strEventId2);
        eventCount2.setNumberOfOccurrences(numberOfOccurrences2);

        List<EventCount> eventStatistics = new ArrayList<EventCount>();
        eventStatistics.add(eventCount);
        eventStatistics.add(eventCount2);

        EventCosts eventCosts = calculator
                .calculateCostsForGatheredEventsInPeriod(eventPrices,
                        eventStatistics);

        List<EventCount> actualEvents = eventCosts.getEventCountList();

        // total costs
        checkEquals(totalCosts, eventCosts.getTotalCosts());

        // number of events
        Assert.assertEquals("Number of events is wrong.", 2,
                actualEvents.size());

        // number of occurrences
        Assert.assertEquals("Number of occurrences is wrong.",
                numberOfOccurrences, actualEvents.get(0)
                        .getNumberOfOccurrences());

        // price for one event
        Assert.assertEquals("Price for one event is wrong.", BigDecimal
                .valueOf(priceForOneEvent), actualEvents.get(0)
                .getPriceForOneEvent());

        // total price for one event
        Assert.assertEquals("Price for one event is wrong.", BigDecimal
                .valueOf(totalCostsForOneEvent), actualEvents.get(0)
                .getPriceForEventsWithEventIdentifier());

        // number of occurrences Event#2
        Assert.assertEquals("Number of occurrences is wrong. Event#2",
                numberOfOccurrences2, actualEvents.get(1)
                        .getNumberOfOccurrences());

        // price for one event Event#2
        Assert.assertEquals("Price for one event is wrong. Event#2", BigDecimal
                .valueOf(priceForOneEvent2), actualEvents.get(1)
                .getPriceForOneEvent());

        // total price for one event Event#2
        Assert.assertEquals("Price for one event is wrong. Event#2", BigDecimal
                .valueOf(totalCostsForOneEvent2), actualEvents.get(1)
                .getPriceForEventsWithEventIdentifier());
    }

    /**
     * Test definition price for step.
     */
    @Test
    public void testGetStepCost_1() {
        Long[] limitArray = { L10, L20, null };
        BigDecimal[] priceArray = { BD100, BD90, BD80 };
        Long[] freeEntityCountArray = { L0, L1, L2 };
        BigDecimal[] additionalPriceArray = { BigDecimal.ZERO, BD1000,
                new BigDecimal(1900) };

        List<SteppedPriceData> steppedPriceList = getSteppedPricesList(
                limitArray, priceArray, freeEntityCountArray,
                additionalPriceArray);

        SteppedPriceDetail steppedpriceDetail = initSteppedPriceDetail(steppedPriceList);
        // calculation is next:
        // Math.round((limit - freeAmount) * price + additionalPrice);

        BigDecimal value = BigDecimal.ONE;
        BigDecimal actualCost = calculator.calculateStepCost(steppedpriceDetail,
                value).getNormalizedCost();
        checkEquals(BD100, actualCost, Numbers.BIGDECIMAL_SCALE);

        steppedpriceDetail = initSteppedPriceDetail(steppedPriceList);
        value = BigDecimal.valueOf(11L);
        actualCost = calculator.calculateStepCost(steppedpriceDetail, value)
                .getNormalizedCost();
        checkEquals(BD1900, actualCost, Numbers.BIGDECIMAL_SCALE);

        steppedpriceDetail = initSteppedPriceDetail(steppedPriceList);
        value = BD20;
        actualCost = calculator.calculateStepCost(steppedpriceDetail, value)
                .getNormalizedCost();
        checkEquals(new BigDecimal(2710), actualCost, Numbers.BIGDECIMAL_SCALE);

        steppedpriceDetail = initSteppedPriceDetail(steppedPriceList);
        value = BigDecimal.valueOf(22L);
        actualCost = calculator.calculateStepCost(steppedpriceDetail, value)
                .getNormalizedCost();
        checkEquals(new BigDecimal(3500), actualCost, Numbers.BIGDECIMAL_SCALE);

        steppedpriceDetail = initSteppedPriceDetail(steppedPriceList);
        value = BD1000;
        actualCost = calculator.calculateStepCost(steppedpriceDetail, value)
                .getNormalizedCost();
        checkEquals(new BigDecimal(81740), actualCost, Numbers.BIGDECIMAL_SCALE);
        SteppedPriceDetail stepCost = calculator.calculateStepCost(
                steppedpriceDetail, value);
        List<SteppedPriceData> priceData = stepCost.getPriceData();
        BigDecimal[] entityCount = { BD10, BigDecimal.valueOf(19),
                BigDecimal.valueOf(998) };
        BigDecimal[] stepAmount = { BD1000, BigDecimal.valueOf(1710),
                BigDecimal.valueOf(79840) };
        for (int i = 0; i < limitArray.length; i++) {
            SteppedPriceData data = priceData.get(i);
            checkEquals(additionalPriceArray[i], data.getAdditionalPrice(),
                    Numbers.BIGDECIMAL_SCALE);
            Long limit = data.getLimit();
            assertEquals(limitArray[i], limit);
            assertEquals(freeEntityCountArray[i].longValue(),
                    data.getFreeEntityCount());
            checkEquals(stepAmount[i], data.getStepAmount(),
                    Numbers.BIGDECIMAL_SCALE);
            checkEquals(priceArray[i], data.getBasePrice(),
                    Numbers.BIGDECIMAL_SCALE);
            checkEquals(entityCount[i], data.getStepEntityCount(),
                    Numbers.BIGDECIMAL_SCALE);
        }
    }

    private List<SteppedPriceData> getSteppedPricesList(Long[] limitArray,
            BigDecimal[] priceArray, Long[] freeEntityCountArray,
            BigDecimal[] additionalPriceArray) {
        List<SteppedPriceData> steppedPriceList = new ArrayList<SteppedPriceData>();
        for (int i = 0; i < additionalPriceArray.length; i++) {
            SteppedPriceHistory step = new SteppedPriceHistory();
            step.getDataContainer().setLimit(limitArray[i]);
            step.getDataContainer().setPrice(priceArray[i]);
            step.getDataContainer().setFreeEntityCount(
                    freeEntityCountArray[i].longValue());
            step.getDataContainer().setAdditionalPrice(additionalPriceArray[i]);
            steppedPriceList.add(new SteppedPriceData(step));
        }
        return steppedPriceList;
    }

    // see bug 8924
    @Test
    public void testGetStepCost_3() {
        Long[] limitArray = { L1, L2, L3, L4, L5, null };
        BigDecimal[] priceArray = { BD10, BD9, BD8, BD7, BD6, BD5 };
        Long[] freeEntityCountArray = { L0, L1, L2, L3, L4, L5 };
        BigDecimal[] additionalPriceArray = { BigDecimal.ZERO, BD10,
                BigDecimal.valueOf(19), BigDecimal.valueOf(27),
                BigDecimal.valueOf(34), BigDecimal.valueOf(40) };

        List<SteppedPriceData> steppedPricesList = getSteppedPricesList(
                limitArray, priceArray, freeEntityCountArray,
                additionalPriceArray);
        SteppedPriceDetail steppedpriceDetail = initSteppedPriceDetail(steppedPricesList);

        BigDecimal value = new BigDecimal(10);
        SteppedPriceDetail stepCost = calculator.calculateStepCost(
                steppedpriceDetail, value);
        BigDecimal actualCost = stepCost.getNormalizedCost();
        checkEquals(new BigDecimal(65), actualCost, Numbers.BIGDECIMAL_SCALE);
        List<SteppedPriceData> priceData = stepCost.getPriceData();
        BigDecimal[] entityCount = { BigDecimal.ONE, BigDecimal.ONE,
                BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, BD5 };
        BigDecimal[] stepAmount = { BD10, BD9, BD8, BD7, BD6,
                BigDecimal.valueOf(25) };
        for (int i = 0; i < limitArray.length; i++) {
            SteppedPriceData data = priceData.get(i);
            checkEquals(additionalPriceArray[i], data.getAdditionalPrice(),
                    Numbers.BIGDECIMAL_SCALE);
            Long limit = data.getLimit();
            assertEquals(limitArray[i], limit);
            assertEquals(freeEntityCountArray[i].longValue(),
                    data.getFreeEntityCount());
            checkEquals(stepAmount[i], data.getStepAmount(),
                    Numbers.BIGDECIMAL_SCALE);
            checkEquals(priceArray[i], data.getBasePrice(),
                    Numbers.BIGDECIMAL_SCALE);
            checkEquals(entityCount[i], data.getStepEntityCount(),
                    Numbers.BIGDECIMAL_SCALE);
        }
    }

    // see bug 8924
    @Test
    public void testGetStepCost_4() {
        // This test evaluates if the step price bigger than the actual count
        // are really set to zero and not to negative values. E.g. you have
        // steps from 1-5 and you calculate for 3. The steps 4 and 5 must have
        // an entity count and amount of 0.
        Long[] limitArray = { L1, L2, L3, L4, L5, null };
        BigDecimal[] priceArray = { BD10, BD9, BD8, BD7, BD6, BD5 };
        Long[] freeEntityCountArray = { L0, L1, L2, L3, L4, L5 };
        BigDecimal[] additionalPriceArray = { BigDecimal.ZERO, BD10,
                BigDecimal.valueOf(19), BigDecimal.valueOf(27),
                BigDecimal.valueOf(34), BigDecimal.valueOf(40) };

        List<SteppedPriceData> steppedPricesList = getSteppedPricesList(
                limitArray, priceArray, freeEntityCountArray,
                additionalPriceArray);
        SteppedPriceDetail steppedpriceDetail = initSteppedPriceDetail(steppedPricesList);

        BigDecimal value = new BigDecimal(3);
        SteppedPriceDetail stepCost = calculator.calculateStepCost(
                steppedpriceDetail, value);
        BigDecimal actualCost = stepCost.getNormalizedCost();
        checkEquals(new BigDecimal(27), actualCost, Numbers.BIGDECIMAL_SCALE);
        List<SteppedPriceData> priceData = stepCost.getPriceData();
        BigDecimal[] entityCount = { BigDecimal.ONE, BigDecimal.ONE,
                BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO };
        BigDecimal[] stepAmount = { BD10, BD9, BD8, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO };
        for (int i = 0; i < limitArray.length; i++) {
            SteppedPriceData data = priceData.get(i);
            checkEquals(additionalPriceArray[i], data.getAdditionalPrice(),
                    Numbers.BIGDECIMAL_SCALE);
            Long limit = data.getLimit();
            assertEquals(limitArray[i], limit);
            assertEquals(freeEntityCountArray[i].longValue(),
                    data.getFreeEntityCount());
            checkEquals(stepAmount[i], data.getStepAmount(),
                    Numbers.BIGDECIMAL_SCALE);
            checkEquals(priceArray[i], data.getBasePrice(),
                    Numbers.BIGDECIMAL_SCALE);
            checkEquals(entityCount[i], data.getStepEntityCount(),
                    Numbers.BIGDECIMAL_SCALE);
        }
    }

    @Test
    public void testGetStepCost_2() {
        Long[] limitArray = { L5, L15, L20, null };
        BigDecimal[] priceArray = { BD10, BD9, BD8, BD7 };
        Long[] freeEntityCountArray = { L0, L5, L15, L20 };
        BigDecimal[] additionalPriceArray = { BigDecimal.ZERO, BD50,
                new BigDecimal(140), new BigDecimal(180) };

        List<SteppedPriceData> steppedPricesList = getSteppedPricesList(
                limitArray, priceArray, freeEntityCountArray,
                additionalPriceArray);
        SteppedPriceDetail steppedpriceDetail = initSteppedPriceDetail(steppedPricesList);

        BigDecimal value = new BigDecimal(55);
        SteppedPriceDetail stepCost = calculator.calculateStepCost(
                steppedpriceDetail, value);
        BigDecimal actualCost = stepCost.getNormalizedCost();
        checkEquals(new BigDecimal(425), actualCost, Numbers.BIGDECIMAL_SCALE);
        List<SteppedPriceData> priceData = stepCost.getPriceData();
        BigDecimal[] entityCount = { BD5, BD10, BD5, BigDecimal.valueOf(35) };
        BigDecimal[] stepAmount = { BD50, BD90, BD40, BigDecimal.valueOf(245) };
        for (int i = 0; i < limitArray.length; i++) {
            SteppedPriceData data = priceData.get(i);
            checkEquals(additionalPriceArray[i], data.getAdditionalPrice(),
                    Numbers.BIGDECIMAL_SCALE);
            Long limit = data.getLimit();
            assertEquals(limitArray[i], limit);
            assertEquals(freeEntityCountArray[i].longValue(),
                    data.getFreeEntityCount());
            checkEquals(stepAmount[i], data.getStepAmount(),
                    Numbers.BIGDECIMAL_SCALE);
            checkEquals(priceArray[i], data.getBasePrice(),
                    Numbers.BIGDECIMAL_SCALE);
            checkEquals(entityCount[i], data.getStepEntityCount(),
                    Numbers.BIGDECIMAL_SCALE);
        }
    }

    private SteppedPriceDetail initSteppedPriceDetail(
            List<SteppedPriceData> steppedPriceList) {
        SteppedPriceDetail steppedpriceDetail;
        steppedpriceDetail = new SteppedPriceDetail();
        steppedpriceDetail.setPriceData(steppedPriceList);
        return steppedpriceDetail;
    }

    /**
     * Test for calculation role costs.
     */
    @Test
    public void testDetermineRoleRelatedCostsForPriceModel() {

        Map<Long, RolePricingDetails> rolePrices = new HashMap<Long, RolePricingDetails>();
        Map<Long, Double> factors = new HashMap<Long, Double>();

        Long key1 = L1;
        Long key2 = L2;

        PricedProductRoleHistory role1 = new PricedProductRoleHistory();
        role1.setPricePerUser(new BigDecimal(100L));
        RolePricingDetails role1Detail = new RolePricingDetails();
        role1Detail.setPricedProductRoleHistory(role1);
        PricedProductRoleHistory role2 = new PricedProductRoleHistory();
        role2.setPricePerUser(new BigDecimal(200L));
        RolePricingDetails role2Detail = new RolePricingDetails();
        role2Detail.setPricedProductRoleHistory(role2);

        rolePrices.put(key1, role1Detail);
        rolePrices.put(key2, role2Detail);

        Long keyFactor1 = L1;
        Long keyFactor2 = L2;

        Double factor1 = Double.valueOf(1.0);
        Double factor2 = Double.valueOf(2.0);

        factors.put(keyFactor1, factor1);
        factors.put(keyFactor2, factor2);

        Map<Long, RolePricingDetails> result = calculator
                .calculateRoleRelatedCostsForPriceModel(rolePrices, factors,
                        null);

        BigDecimal cost = result.get(key1).getCost();
        checkEquals("100.0", cost);
        cost = result.get(key2).getCost();
        checkEquals("400.0", cost);

    }

    @Test
    public void testDetermineRoleRelatedCostsForPriceModel_fraction() {

        Map<Long, RolePricingDetails> rolePrices = new HashMap<Long, RolePricingDetails>();
        Map<Long, Double> factors = new HashMap<Long, Double>();

        Long key1 = L1;
        Long key2 = L2;

        PricedProductRoleHistory role1 = new PricedProductRoleHistory();
        role1.setPricePerUser(new BigDecimal("100.50"));
        RolePricingDetails role1Detail = new RolePricingDetails();
        role1Detail.setPricedProductRoleHistory(role1);
        PricedProductRoleHistory role2 = new PricedProductRoleHistory();
        role2.setPricePerUser(new BigDecimal("200.25"));
        RolePricingDetails role2Detail = new RolePricingDetails();
        role2Detail.setPricedProductRoleHistory(role2);

        rolePrices.put(key1, role1Detail);
        rolePrices.put(key2, role2Detail);

        Long keyFactor1 = L1;
        Long keyFactor2 = L2;

        Double factor1 = Double.valueOf(1.0);
        Double factor2 = Double.valueOf(2.0);

        factors.put(keyFactor1, factor1);
        factors.put(keyFactor2, factor2);

        Map<Long, RolePricingDetails> result = calculator
                .calculateRoleRelatedCostsForPriceModel(rolePrices, factors,
                        null);

        BigDecimal cost = result.get(key1).getCost();
        checkEquals("100.50", cost);
        cost = result.get(key2).getCost();
        checkEquals("400.50", cost);

    }

    @Test
    /**
     * Test for calculation costs with discount.
     */
    public void testGetDiscountedCosts() {
        BigDecimal costExpected = BigDecimal.valueOf(95L).setScale(
                Numbers.BIGDECIMAL_SCALE);
        BigDecimal costsBefore = BigDecimal.valueOf(100L).setScale(
                Numbers.BIGDECIMAL_SCALE);
        BigDecimal discountValue = new BigDecimal("5.00");

        BigDecimal costsActual = CostCalculator.calculateDiscountedCosts(
                costsBefore, discountValue);

        Assert.assertEquals(costExpected, costsActual);
    }

    /**
     * Discount with fractional costs
     */
    @Test
    public void testGetDiscountedCosts_fraction() {
        BigDecimal costsBefore = new BigDecimal("100.50");
        BigDecimal discountValue = new BigDecimal("10.00");
        BigDecimal costsActual = CostCalculator.calculateDiscountedCosts(
                costsBefore, discountValue);
        checkEquals("90.45", costsActual);
    }

    @Test(expected = IllegalArgumentException.class)
    /**
     * Test for calculation costs with discount.
     * Null discount. Expected IlleagalArgumentException (Exception)
     */
    public void testGetDiscountedCostsNullDiscount() {
        BigDecimal costsBefore = BigDecimal.valueOf(100L).setScale(
                Numbers.BIGDECIMAL_SCALE);
        BigDecimal discountValue = null;

        CostCalculator.calculateDiscountedCosts(costsBefore, discountValue);
    }

    @Test(expected = IllegalArgumentException.class)
    /**
     * Test for calculation costs with discount.
     * Null discount. Expected IlleagalArgumentException (Exception)
     */
    public void testGetNullDiscountedCostsDiscount() {
        BigDecimal costsBefore = null;
        BigDecimal discountValue = new BigDecimal("10.00");

        CostCalculator.calculateDiscountedCosts(costsBefore, discountValue);
    }

    @Test
    public void testDetermineVATCosts_NoVAT() throws Exception {
        VatRateDetails vrd = new VatRateDetails();
        vrd.setNetCosts(BigDecimal.valueOf(150));
        vrd = CostCalculator.calculateVATCosts(vrd);
        assertNotNull(vrd);
        assertNotNull(vrd.getTotalCosts());
        assertEquals(150, vrd.getTotalCosts().longValue());
        checkEquals(0, vrd.getVatAmount());
    }

    @Test
    public void testDetermineVATCosts_fraction() throws Exception {
        VatRateDetails vrd = new VatRateDetails();
        vrd.setDefaultVatRate(BigDecimal.valueOf(10));
        vrd.setNetCosts(BigDecimal.valueOf(10.50));
        vrd = CostCalculator.calculateVATCosts(vrd);
        assertNotNull(vrd);
        assertNotNull(vrd.getTotalCosts());
        checkEquals("11.55", vrd.getTotalCosts());
        checkEquals("1.05", vrd.getVatAmount());
    }

    @Test
    public void testDetermineVATCosts_UseVAT() throws Exception {
        VatRateDetails vrd = new VatRateDetails();
        vrd.setDefaultVatRate(BigDecimal.valueOf(12.5F));
        vrd.setNetCosts(BigDecimal.valueOf(1000));
        vrd = CostCalculator.calculateVATCosts(vrd);
        assertNotNull(vrd);
        assertNotNull(vrd.getTotalCosts());
        assertEquals(1125, vrd.getTotalCosts().longValue());
        checkEquals("125.00", vrd.getVatAmount());
    }

    @Test
    public void testDetermineVATCosts_UseVATLargeValues() throws Exception {
        VatRateDetails vrd = new VatRateDetails();
        vrd.setDefaultVatRate(new BigDecimal("1.33"));
        vrd.setNetCosts(BigDecimal.valueOf(Long.MAX_VALUE / 2));
        vrd = CostCalculator.calculateVATCosts(vrd);
        assertNotNull(vrd);
        assertNotNull(vrd.getTotalCosts());
        checkEquals("4673021442472472162.1100", vrd.getTotalCosts());
        checkEquals("61335424045084259.1100", vrd.getVatAmount());
    }

}
