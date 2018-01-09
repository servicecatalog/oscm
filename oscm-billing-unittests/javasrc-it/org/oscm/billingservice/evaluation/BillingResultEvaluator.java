/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: Jan 3, 2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.evaluation;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Date;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.oscm.converter.XMLConverter;
import org.oscm.test.DateTimeHandling;
import org.oscm.types.constants.BillingResultXMLTags;

/**
 * @author baumann
 * 
 */
public class BillingResultEvaluator {

    private final Document billingResult;

    // Java double has a precision of 15 significant digits. The test prices
    // and factors don't have more than 5 digits on the left of the comma.
    private final static double ASSERT_DOUBLE_DELTA = 0.0000000009D;

    public BillingResultEvaluator(Document billingResult) {
        this.billingResult = billingResult;
    }

    public Document getBillingResult() {
        return billingResult;
    }

    public void assertPriceModelUsagePeriod(Node priceModel,
            String expectedStartDate, String expectedEndDate)
            throws XPathExpressionException {
        assertEquals("Wrong start date in usage period", expectedStartDate,
                XMLConverter.getNodeTextContentByXPath(priceModel,
                        "UsagePeriod/@startDate"));
        assertEquals("Wrong end date in usage period", expectedEndDate,
                XMLConverter.getNodeTextContentByXPath(priceModel,
                        "UsagePeriod/@endDate"));
    }

    public void assertPriceModelUsagePeriod(Node priceModel,
            long expectedStartDate, long expectedEndDate)
            throws XPathExpressionException {
        assertPriceModelUsagePeriod(priceModel, expectedStartDate + "",
                expectedEndDate + "");
    }

    public void assertOneTimeFee(Node priceModel, String expected)
            throws XPathExpressionException {
        assertEquals(expected, XMLConverter.getNodeTextContentByXPath(
                priceModel, "OneTimeFee/@amount"));
        assertEquals(expected, XMLConverter.getNodeTextContentByXPath(
                priceModel, "OneTimeFee/@baseAmount"));
        assertEquals("1", XMLConverter.getNodeTextContentByXPath(priceModel,
                "OneTimeFee/@factor"));
    }

    public void assertOneTimeFee(long priceModelKey, String expected)
            throws XPathExpressionException {
        assertEquals(expected, XMLConverter.getNodeTextContentByXPath(
                billingResult, "//PriceModel[@id='" + priceModelKey
                        + "']/OneTimeFee/@amount"));
        assertEquals(expected, XMLConverter.getNodeTextContentByXPath(
                billingResult, "//PriceModel[@id='" + priceModelKey
                        + "']/OneTimeFee/@baseAmount"));
        assertEquals("1", XMLConverter.getNodeTextContentByXPath(billingResult,
                "//PriceModel[@id='" + priceModelKey + "']/OneTimeFee/@factor"));
    }

    public void assertZeroOneTimeFee(Node priceModel, String baseAmount)
            throws XPathExpressionException {
        assertEquals("0.00", XMLConverter.getNodeTextContentByXPath(priceModel,
                "OneTimeFee/@amount"));
        assertEquals(baseAmount, XMLConverter.getNodeTextContentByXPath(
                priceModel, "OneTimeFee/@baseAmount"));
        assertEquals("0", XMLConverter.getNodeTextContentByXPath(priceModel,
                "OneTimeFee/@factor"));
    }

    public void assertPriceModelPeriodFee(Node priceModel,
            String expectedBasePeriod, String expectedBasePrice,
            String expectedFactor, String price)
            throws XPathExpressionException {
        assertEquals("Wrong base period in period fee", expectedBasePeriod,
                XMLConverter.getNodeTextContentByXPath(priceModel,
                        "PeriodFee/@basePeriod"));
        assertEquals("Wrong base price in period fee", expectedBasePrice,
                XMLConverter.getNodeTextContentByXPath(priceModel,
                        "PeriodFee/@basePrice"));
        assertEquals("Wrong factor in period fee", expectedFactor,
                XMLConverter.getNodeTextContentByXPath(priceModel,
                        "PeriodFee/@factor"));
        assertEquals("Wrong price in period fee", price,
                XMLConverter.getNodeTextContentByXPath(priceModel,
                        "PeriodFee/@price"));
    }

    public void assertPriceModelPeriodFee(Node priceModel,
            String expectedBasePeriod, String expectedBasePrice,
            double expectedFactor, String price)
            throws XPathExpressionException {
        assertEquals("Wrong base period in period fee", expectedBasePeriod,
                XMLConverter.getNodeTextContentByXPath(priceModel,
                        "PeriodFee/@basePeriod"));
        assertEquals("Wrong base price in period fee", expectedBasePrice,
                XMLConverter.getNodeTextContentByXPath(priceModel,
                        "PeriodFee/@basePrice"));
        assertEquals("Wrong price in period fee", price,
                XMLConverter.getNodeTextContentByXPath(priceModel,
                        "PeriodFee/@price"));
        assertEquals("factor wrong", expectedFactor,
                XMLConverter.getDoubleAttValue(XMLConverter.getLastChildNode(
                        priceModel, BillingResultXMLTags.PERIOD_FEE_NODE_NAME),
                        BillingResultXMLTags.FACTOR_ATTRIBUTE_NAME),
                ASSERT_DOUBLE_DELTA);
    }

    public void assertPriceModelUserAssignmentCosts(Node priceModel,
            String expectedBasePeriod, String expectedBasePrice,
            String expectedFactor, String expectedUserNumber, String price)
            throws XPathExpressionException {

        assertEquals("Wrong base price in user assignment costs",
                expectedBasePrice, XMLConverter.getNodeTextContentByXPath(
                        priceModel, "UserAssignmentCosts/@basePrice"));

        assertPriceModelUserAssignmentCosts(priceModel, expectedBasePeriod,
                expectedFactor, expectedUserNumber, price);
    }

    public void assertPriceModelUserAssignmentCosts(Node priceModel,
            String expectedBasePeriod, String expectedBasePrice,
            double expectedFactor, String expectedUserNumber, String price)
            throws XPathExpressionException {

        assertEquals("Wrong base price in user assignment costs",
                expectedBasePrice, XMLConverter.getNodeTextContentByXPath(
                        priceModel, "UserAssignmentCosts/@basePrice"));

        assertPriceModelUserAssignmentCosts(priceModel, expectedBasePeriod,
                expectedFactor, expectedUserNumber, price);
    }

    public void assertPriceModelUserAssignmentCosts(Node priceModel,
            String expectedBasePeriod, String expectedBasePrice,
            double expectedFactor, String expectedUserNumber, String price,
            String total) throws XPathExpressionException {
        assertPriceModelUserAssignmentCosts(priceModel, expectedBasePeriod,
                expectedBasePrice, expectedFactor, expectedUserNumber, price);

        assertEquals("Wrong total price in user assignment costs", total,
                XMLConverter.getNodeTextContentByXPath(priceModel,
                        "UserAssignmentCosts/@total"));
    }

    public void assertPriceModelUserAssignmentCosts(Node priceModel,
            String expectedBasePeriod, String expectedFactor,
            String expectedUserNumber, String price)
            throws XPathExpressionException {

        assertEquals("Wrong base period in user assignment costs",
                expectedBasePeriod, XMLConverter.getNodeTextContentByXPath(
                        priceModel, "UserAssignmentCosts/@basePeriod"));
        assertEquals("Wrong factor in user assignment costs", expectedFactor,
                XMLConverter.getNodeTextContentByXPath(priceModel,
                        "UserAssignmentCosts/@factor"));
        assertEquals("Wrong user number in user assignment costs",
                String.valueOf(expectedUserNumber),
                XMLConverter.getNodeTextContentByXPath(priceModel,
                        "UserAssignmentCosts/@numberOfUsersTotal"));
        assertEquals("Wrong price in user assignment costs", price,
                XMLConverter.getNodeTextContentByXPath(priceModel,
                        "UserAssignmentCosts/@price"));
    }

    public void assertPriceModelUserAssignmentCosts(Node priceModel,
            String expectedBasePeriod, double expectedFactor,
            String expectedUserNumber, String price)
            throws XPathExpressionException {

        assertEquals("Wrong base period in user assignment costs",
                expectedBasePeriod, XMLConverter.getNodeTextContentByXPath(
                        priceModel, "UserAssignmentCosts/@basePeriod"));
        assertEquals("Wrong factor in user assignment costs", expectedFactor,
                XMLConverter.getDoubleAttValue(XMLConverter.getLastChildNode(
                        priceModel,
                        BillingResultXMLTags.USER_ASSIGNMENT_COSTS_NODE_NAME),
                        BillingResultXMLTags.FACTOR_ATTRIBUTE_NAME),
                ASSERT_DOUBLE_DELTA);
        assertEquals("Wrong user number in user assignment costs",
                String.valueOf(expectedUserNumber),
                XMLConverter.getNodeTextContentByXPath(priceModel,
                        "UserAssignmentCosts/@numberOfUsersTotal"));
        assertEquals("Wrong price in user assignment costs", price,
                XMLConverter.getNodeTextContentByXPath(priceModel,
                        "UserAssignmentCosts/@price"));
    }

    public void assertPriceModelUserAssignmentCosts(Node priceModel,
            String expectedBasePeriod, double expectedFactor,
            String expectedUserNumber, String price, String total)
            throws XPathExpressionException {
        assertPriceModelUserAssignmentCosts(priceModel, expectedBasePeriod,
                expectedFactor, expectedUserNumber, price);

        assertEquals("Wrong total price in user assignment costs", total,
                XMLConverter.getNodeTextContentByXPath(priceModel,
                        "UserAssignmentCosts/@total"));
    }

    public void assertTotalRoleCosts(Node roleCosts, String expectedTotal)
            throws XPathExpressionException {
        assertEquals("Wrong total in role costs", expectedTotal,
                XMLConverter.getNodeTextContentByXPath(roleCosts, "@total"));
    }

    public void assertUserAssignmentCostsFactor(long priceModelKey,
            String userId, String factor) throws XPathExpressionException {

        assertEquals(
                "Wrong factor in user assignment costs by user",
                factor,
                XMLConverter
                        .getNodeTextContentByXPath(
                                billingResult,
                                "//PriceModel[@id='"
                                        + priceModelKey
                                        + "']/UserAssignmentCosts/UserAssignmentCostsByUser[@userId='"
                                        + userId + "']/@factor"));
    }

    public void assertUserAssignmentCostsFactor(Node priceModel, String userId,
            String factor) throws XPathExpressionException {
        assertEquals("Wrong factor in user assignment costs by user", factor,
                XMLConverter.getNodeTextContentByXPath(priceModel,
                        "UserAssignmentCosts/UserAssignmentCostsByUser[@userId='"
                                + userId + "']/@factor"));
    }

    public void assertUserAssignmentCostsByUser(Node priceModel, String userId,
            double factor) throws XPathExpressionException {
        Node userAssCostsByUserNode = XMLConverter.getNodeByXPath(priceModel,
                "UserAssignmentCosts/UserAssignmentCostsByUser[@userId='"
                        + userId + "']");
        assertNotNull("UserAssignmentCostsByUser with given user id not found",
                userAssCostsByUserNode);
        assertEquals("Wrong factor in user assignment costs by user", factor,
                XMLConverter.getDoubleAttValue(userAssCostsByUserNode,
                        BillingResultXMLTags.FACTOR_ATTRIBUTE_NAME),
                ASSERT_DOUBLE_DELTA);
    }

    public void assertParametersCosts(Node priceModel, String expectedAmount)
            throws XPathExpressionException {
        assertEquals("Wrong amount in parameter costs", expectedAmount,
                XMLConverter.getNodeTextContentByXPath(priceModel,
                        "Parameters/ParametersCosts/@amount"));
    }

    public void assertParameterPeriodFee(Node parameter, String period,
            String basePrice, double factor, String price)
            throws XPathExpressionException {
        assertParameterPeriodFee(parameter, period, factor, price, null);

        assertEquals("Wrong base price in period fee", basePrice,
                XMLConverter.getNodeTextContentByXPath(parameter,
                        "PeriodFee/@basePrice"));
    }

    public void assertParameterPeriodFee(Node parameter, String period,
            String basePrice, double factor, String price, String valueFactor)
            throws XPathExpressionException {
        assertParameterPeriodFee(parameter, period, factor, price, valueFactor);

        assertEquals("Wrong base price in period fee", basePrice,
                XMLConverter.getNodeTextContentByXPath(parameter,
                        "PeriodFee/@basePrice"));
    }

    public void assertParameterValue(Node parameter, String expectedAmount,
            String expectedType) {
        Node nodeValue = XMLConverter.getLastChildNode(parameter,
                BillingResultXMLTags.PARAMETER_VALUE_NODE_NAME);

        String actualAmount = XMLConverter.getStringAttValue(nodeValue,
                BillingResultXMLTags.AMOUNT_ATTRIBUTE_NAME);
        assertEquals(expectedAmount, actualAmount);

        String actualType = XMLConverter.getStringAttValue(nodeValue,
                BillingResultXMLTags.TYPE_ATTRIBUTE_NAME);
        assertEquals(expectedType, actualType);
    }

    public void assertParameterPeriodFee(Node parameter, String period,
            double factor, String price, String valueFactor) {
        assertNotNull("Parameter not found", parameter);
        Node nodePeriodFee = XMLConverter.getLastChildNode(parameter,
                BillingResultXMLTags.PERIOD_FEE_NODE_NAME);
        assertEquals("period wrong", period, XMLConverter.getStringAttValue(
                nodePeriodFee, BillingResultXMLTags.BASE_PERIOD_ATTRIBUTE_NAME));
        assertEquals("factor wrong", factor, XMLConverter.getDoubleAttValue(
                nodePeriodFee, BillingResultXMLTags.FACTOR_ATTRIBUTE_NAME),
                ASSERT_DOUBLE_DELTA);
        assertEquals("price wrong", price, XMLConverter.getStringAttValue(
                nodePeriodFee, BillingResultXMLTags.PRICE_ATTRIBUTE_NAME));
        assertEquals("value factor wrong", valueFactor,
                XMLConverter.getStringAttValue(nodePeriodFee,
                        BillingResultXMLTags.VALUE_FACTOR_ATTRIBUTE_NAME));
    }

    public void assertUserAssignmentCosts(Node parent, String basePeriod,
            String basePrice, double factor, String price, String total) {
        assertUserAssignmentCosts(parent, basePeriod, basePrice, factor, price,
                total, null);
    }

    /**
     * @param parent
     *            The parent node of the user assignment costs node, e.g.
     *            Parameter or Option
     */
    public void assertUserAssignmentCosts(Node parent, String basePeriod,
            String basePrice, double factor, String price, String total,
            String valueFactor) {
        assertNotNull("Parameter not found", parent);
        Node nodePeriodFee = XMLConverter.getLastChildNode(parent,
                BillingResultXMLTags.USER_ASSIGNMENT_COSTS_NODE_NAME);
        assertEquals("base period wrong", basePeriod,
                XMLConverter.getStringAttValue(nodePeriodFee,
                        BillingResultXMLTags.BASE_PERIOD_ATTRIBUTE_NAME));
        assertEquals("base price wrong", basePrice,
                XMLConverter.getStringAttValue(nodePeriodFee,
                        BillingResultXMLTags.BASE_PRICE_ATTRIBUTE_NAME));

        assertEquals("factor wrong", factor, XMLConverter.getDoubleAttValue(
                nodePeriodFee, BillingResultXMLTags.FACTOR_ATTRIBUTE_NAME),
                ASSERT_DOUBLE_DELTA);

        assertEquals("price wrong", price, XMLConverter.getStringAttValue(
                nodePeriodFee, BillingResultXMLTags.PRICE_ATTRIBUTE_NAME));
        assertEquals("total wrong", total, XMLConverter.getStringAttValue(
                nodePeriodFee, BillingResultXMLTags.TOTAL_ATTRIBUTE_NAME));
        assertEquals("value factor wrong", valueFactor,
                XMLConverter.getStringAttValue(nodePeriodFee,
                        BillingResultXMLTags.VALUE_FACTOR_ATTRIBUTE_NAME));
    }

    public void assertParameterCosts(Node parameter, String amount) {
        assertNotNull("Parameter not found", parameter);
        Node nodePeriodFee = XMLConverter.getLastChildNode(parameter,
                BillingResultXMLTags.PARAMETER_COSTS_NODE_NAME);
        assertEquals(amount, XMLConverter.getStringAttValue(nodePeriodFee,
                BillingResultXMLTags.AMOUNT_ATTRIBUTE_NAME));
    }

    public void assertRoleCost(Node roleCosts, String roleId, String basePrice,
            double factor, String price) {
        assertNotNull("RoleCosts node not found", roleCosts);

        NodeList roleCostList = roleCosts.getChildNodes();
        Node roleCost = null;
        for (int index = 0; index < roleCostList.getLength(); index++) {
            Node rc = roleCostList.item(index);
            if (BillingResultXMLTags.ROLE_COST_NODE_NAME.equals(rc
                    .getNodeName())
                    && XMLConverter.getStringAttValue(rc,
                            BillingResultXMLTags.ID_ATTRIBUTE_NAME).equals(
                            roleId)) {
                roleCost = rc;
                break;
            }
        }
        assertNotNull("RoleCost node not found", roleCost);

        assertEquals("basePrice wrong", basePrice,
                XMLConverter.getStringAttValue(roleCost,
                        BillingResultXMLTags.BASE_PRICE_ATTRIBUTE_NAME));
        assertEquals("factor wrong", factor, XMLConverter.getDoubleAttValue(
                roleCost, BillingResultXMLTags.FACTOR_ATTRIBUTE_NAME), 0.001D);
        assertEquals("price wrong", price, XMLConverter.getStringAttValue(
                roleCost, BillingResultXMLTags.PRICE_ATTRIBUTE_NAME));
    }

    public void assertPriceModelCosts(Node priceModel, String expectedCurrency,
            String expectedAmount) throws XPathExpressionException {
        assertEquals("Wrong currency in price model costs", expectedCurrency,
                XMLConverter.getNodeTextContentByXPath(priceModel,
                        "PriceModelCosts/@currency"));
        assertEquals("Wrong amount in price model costs", expectedAmount,
                XMLConverter.getNodeTextContentByXPath(priceModel,
                        "PriceModelCosts/@amount"));
    }

    public void assertPriceModelDiscount(Node priceModel, String percent,
            String discountNetAmount, String netAmountAfterDiscount,
            String netAmountBeforeDiscount) throws XPathExpressionException {
        Node discount = XMLConverter.getNodeByXPath(priceModel,
                "PriceModelCosts/Discount");

        assertDiscount(discount, percent, discountNetAmount,
                netAmountAfterDiscount, netAmountBeforeDiscount);
    }

    public void assertNullPriceModelDiscount(Node priceModel)
            throws XPathExpressionException {
        assertNull(XMLConverter.getNodeByXPath(priceModel,
                "PriceModelCosts/Discount"));
    }

    public void assertPeriodFeeFactor(long priceModelKey, double expectedFactor)
            throws XPathExpressionException {

        Node priceModel = XMLConverter.getNodeByXPath(billingResult,
                "//PriceModel[@id='" + priceModelKey + "']");

        assertPeriodFeeFactor(priceModel, expectedFactor);
    }

    public void assertPeriodFeeFactor(Node priceModel, double expectedFactor)
            throws XPathExpressionException {

        double factor = Double.parseDouble(XMLConverter
                .getNodeTextContentByXPath(priceModel, "PeriodFee/@factor"));
        assertEquals("Wrong factor attribute in price model node.",
                expectedFactor, factor, ASSERT_DOUBLE_DELTA);
    }

    public void assertPriceModelNodeNull(long priceModelKey)
            throws XPathExpressionException {

        Node priceModel = XMLConverter.getNodeByXPath(billingResult,
                "//PriceModel[@id='" + priceModelKey + "']");

        assertNull(
                "Price model node in billing document is expected to be null",
                priceModel);
    }

    public void assertSubscriptionId(String subscriptionId)
            throws XPathExpressionException {
        final Node n = XMLConverter.getNodeByXPath(billingResult,
                "/BillingDetails/Subscriptions/Subscription");
        assertEquals("Wrong subscription id", subscriptionId,
                XMLConverter.getStringAttValue(n,
                        BillingResultXMLTags.ID_ATTRIBUTE_NAME));
    }

    public void assertPeriod(String start, String end)
            throws XPathExpressionException {
        final Node n = XMLConverter.getNodeByXPath(billingResult,
                "/BillingDetails/Period");
        assertEquals(
                "Wrong start date in period",
                new Date(DateTimeHandling.calculateMillis(start)),
                new Date(
                        Long.valueOf(
                                XMLConverter
                                        .getStringAttValue(
                                                n,
                                                BillingResultXMLTags.START_DATE_ATTRIBUTE_NAME))
                                .longValue()));
        assertEquals(
                "Wrong end date in period",
                new Date(DateTimeHandling.calculateMillis(end)),
                new Date(Long.valueOf(
                        XMLConverter.getStringAttValue(n,
                                BillingResultXMLTags.END_DATE_ATTRIBUTE_NAME))
                        .longValue()));
    }

    public void assertOverallCosts(String expectedCurrency,
            String expectedAmounts) throws XPathExpressionException {
        assertOverallCosts(expectedAmounts, expectedCurrency, expectedAmounts);
    }

    public void assertOverallCosts(String expectedNetAmount,
            String expectedCurrency, String expectedGrossAmount)
            throws XPathExpressionException {
        Node nodeOverallCosts = XMLConverter.getNodeByXPath(billingResult,
                "/BillingDetails/OverallCosts");
        assertEquals("Wrong net amount in overall costs", expectedNetAmount,
                XMLConverter.getStringAttValue(nodeOverallCosts,
                        BillingResultXMLTags.NET_AMOUNT_ATTRIBUTE_NAME));
        assertEquals("Wrong currency in overall costs", expectedCurrency,
                XMLConverter.getStringAttValue(nodeOverallCosts,
                        BillingResultXMLTags.CURRENCY_ATTRIBUTE_NAME));
        assertEquals("Wrong gross amount in overall costs",
                expectedGrossAmount, XMLConverter.getStringAttValue(
                        nodeOverallCosts,
                        BillingResultXMLTags.GROSS_AMOUNT_ATTRIBUTE_NAME));
    }

    public void assertOverallDiscount(String percent, String discountNetAmount,
            String netAmountAfterDiscount, String netAmountBeforeDiscount)
            throws XPathExpressionException {
        Node discount = XMLConverter.getNodeByXPath(billingResult,
                "/BillingDetails/OverallCosts/Discount");

        assertDiscount(discount, percent, discountNetAmount,
                netAmountAfterDiscount, netAmountBeforeDiscount);
    }

    public void assertNullOverallDiscount() throws XPathExpressionException {
        assertNull(XMLConverter.getNodeByXPath(billingResult,
                "/BillingDetails/OverallCosts/Discount"));
    }

    public void assertOverallVAT(String percent, String amount)
            throws XPathExpressionException {
        Node vat = XMLConverter.getNodeByXPath(billingResult,
                "/BillingDetails/OverallCosts/VAT");
        assertEquals("Wrong percentage in VAT", percent,
                XMLConverter.getStringAttValue(vat,
                        BillingResultXMLTags.PERCENT_ATTRIBUTE_NAME));
        assertEquals("Wrong amount in VAT", amount,
                XMLConverter.getStringAttValue(vat,
                        BillingResultXMLTags.AMOUNT_ATTRIBUTE_NAME));
    }

    public void assertDiscount(Node discount, String percent,
            String discountNetAmount, String netAmountAfterDiscount,
            String netAmountBeforeDiscount) {

        assertEquals("Wrong percentage in discount", percent,
                XMLConverter.getStringAttValue(discount,
                        BillingResultXMLTags.PERCENT_ATTRIBUTE_NAME));
        assertEquals(
                "Wrong discount net amount",
                discountNetAmount,
                XMLConverter
                        .getStringAttValue(
                                discount,
                                BillingResultXMLTags.DISCOUNT_NET_AMOUNT_ATTRIBUTE_NAME));
        assertEquals("Wrong net amount before discount",
                netAmountBeforeDiscount, XMLConverter.getStringAttValue(
                        discount, BillingResultXMLTags.AMOUNT_BEFORE_DISCOUNT));
        assertEquals("Wrong net amount after discount", netAmountAfterDiscount,
                XMLConverter.getStringAttValue(discount,
                        BillingResultXMLTags.AMOUNT_AFTER_DISCOUNT));
    }

    public void assertNullOneTimeFee() throws XPathExpressionException {
        assertNull(XMLConverter.getNodeByXPath(billingResult, "//OneTimeFee"));
    }

    public void assertNullOneTimeFee(Node priceModel)
            throws XPathExpressionException {
        assertNull(XMLConverter.getNodeByXPath(priceModel, "./OneTimeFee"));
    }

    public void assertNullOneTimeFee(long priceModelKey)
            throws XPathExpressionException {
        assertNull(XMLConverter.getNodeByXPath(billingResult,
                "//PriceModel[@id='" + priceModelKey + "']/OneTimeFee"));
    }

    public void assertNullPriceModel(long priceModelKey)
            throws XPathExpressionException {
        assertNull(XMLConverter.getNodeByXPath(billingResult,
                "//PriceModel[@id='" + priceModelKey + "']"));
    }

    public void assertNullPriceModel(long startDate, long endDate)
            throws XPathExpressionException {
        assertNull("Price model shouldn't exist",
                BillingXMLNodeSearch.getPriceModelNode(billingResult,
                        startDate, endDate));
    }

    public void assertNullPriceModel(long priceModelKey, long startDate,
            long endDate) throws XPathExpressionException {
        assertNull("Price model shouldn't exist",
                BillingXMLNodeSearch.getPriceModelNode(billingResult,
                        priceModelKey, startDate, endDate));
    }

    /**
     * 
     * @param option
     *            must be an option node
     * @param expected
     *            result
     */
    public void assertOptionCosts(Node option, String expected) {
        Node nodeOptionCosts = XMLConverter.getLastChildNode(option,
                BillingResultXMLTags.OPTION_COSTS_NODE_NAME);
        assertEquals("wrong option costs", expected,
                XMLConverter.getStringAttValue(nodeOptionCosts,
                        BillingResultXMLTags.AMOUNT_ATTRIBUTE_NAME));
    }

    public void assertNodeCount(String relativePath, double expectedCount)
            throws XPathExpressionException {
        assertNodeCount(billingResult, relativePath, expectedCount);
    }

    public void assertNodeCount(Node priceModel, String relativePath,
            double expectedCount) throws XPathExpressionException {
        assertEquals(
                expectedCount,
                XMLConverter.countNodes(priceModel, relativePath).doubleValue(),
                0);
    }

    public void assertEventCosts(Node gatheredEvents, String eventId,
            String singleCost, String numberOfOccurrence,
            String costForEventType) throws XPathExpressionException {
        assertNotNull("GatheredEvents node not found", gatheredEvents);
        Node event = BillingXMLNodeSearch.getEventNode(gatheredEvents, eventId);
        assertNotNull("event node not found", event);
        assertEquals("SingleCost wrong", singleCost,
                XMLConverter.getNodeTextContentByXPath(event,
                        "SingleCost/@amount"));
        assertEventCosts(event, numberOfOccurrence, costForEventType);
    }

    public void assertEventCosts(Node event, String numberOfOccurrence,
            String costForEventType) throws XPathExpressionException {
        assertNotNull("event node not found", event);
        assertEquals("NumberOfOccurrence wrong", numberOfOccurrence,
                XMLConverter.getNodeTextContentByXPath(event,
                        "NumberOfOccurrence/@amount"));
        assertEquals("CostForEventType wrong", costForEventType,
                XMLConverter.getNodeTextContentByXPath(event,
                        "CostForEventType/@amount"));
    }

    public void assertGatheredEventsCosts(Node priceModel, String amount)
            throws XPathExpressionException {
        assertEquals("Wrong amount in gatthered events costs", amount,
                XMLConverter.getNodeTextContentByXPath(priceModel,
                        "GatheredEvents/GatheredEventsCosts/@amount"));
    }

    public void assertSteppedPrice(Node steppedPrices, String additionalPrice,
            String basePrice, String freeAmount, String limit,
            String stepAmount, String stepEntityCount) {
        assertNotNull("SteppedPrices node doesn't exist", steppedPrices);

        NodeList steppedPricesChilds = steppedPrices.getChildNodes();
        Node steppedPrice = null;
        for (int index = 0; index < steppedPricesChilds.getLength(); index++) {
            Node child = steppedPricesChilds.item(index);
            if (BillingResultXMLTags.STEPPED_PRICE_NODE_NAME.equals(child
                    .getNodeName())
                    && XMLConverter.getStringAttValue(child,
                            BillingResultXMLTags.LIMIT_ATTRIBUTE_NAME).equals(
                            limit)) {
                steppedPrice = child;
                break;
            }
        }
        assertNotNull("SteppedPrice node not found", steppedPrice);

        assertEquals("SteppedPrice: additional price wrong", additionalPrice,
                XMLConverter.getStringAttValue(steppedPrice,
                        BillingResultXMLTags.ADDITIONAL_PRICE_ATTRIBUTE_NAME));
        assertEquals("SteppedPrice: base price wrong", basePrice,
                XMLConverter.getStringAttValue(steppedPrice,
                        BillingResultXMLTags.BASE_PRICE_ATTRIBUTE_NAME));
        assertEquals("SteppedPrice: free amount wrong", freeAmount,
                XMLConverter.getStringAttValue(steppedPrice,
                        BillingResultXMLTags.FREE_AMOUNT_ATTRIBUTE_NAME));
        assertEquals("SteppedPrice: step amount wrong", stepAmount,
                XMLConverter.getStringAttValue(steppedPrice,
                        BillingResultXMLTags.STEP_AMOUNT_ATTRIBUTE_NAME));
        assertEquals("SteppedPrice: step entity count wrong", stepEntityCount,
                XMLConverter.getStringAttValue(steppedPrice,
                        BillingResultXMLTags.STEP_ENTITY_COUNT_ATTRIBUTE_NAME));
    }

    public void assertSteppedPricesAmount(Node steppedPrices,
            String expectedAmount) throws XPathExpressionException {
        assertEquals("Wrong amount in stepped prices", expectedAmount,
                XMLConverter
                        .getNodeTextContentByXPath(steppedPrices, "@amount"));
    }

    public void assertOrganizationDetails(String orgId, String orgName,
            String address, String email, String paymenttype)
            throws XPathExpressionException {
        final Node orgDetails = XMLConverter.getNodeByXPath(billingResult,
                "/BillingDetails/OrganizationDetails");
        assertTextContent(orgDetails, "Id", orgId);
        assertTextContent(orgDetails, "Name", orgName);
        assertTextContent(orgDetails, "Address", address);
        assertTextContent(orgDetails, "Email", email);
        assertTextContent(orgDetails, "Paymenttype", paymenttype);
    }

    private void assertTextContent(Node parentNode, String childName,
            String value) throws XPathExpressionException {
        assertEquals("Wrong content of XML node " + childName, value,
                XMLConverter.getNodeTextContentByXPath(parentNode, childName));
    }

}
