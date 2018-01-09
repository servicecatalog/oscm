/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 30.04.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.evaluation;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import org.oscm.converter.XMLConverter;
import org.oscm.test.DateTimeHandling;
import org.oscm.types.constants.BillingResultXMLTags;

/**
 * @author baumann
 * 
 */
public class BillingXMLNodeSearch {

    public static Node getParameterNode(Node priceModel, String parameterId,
            String startDate, String endDate) throws XPathExpressionException {

        long start = DateTimeHandling.calculateMillis(startDate);
        long end = DateTimeHandling.calculateMillis(endDate);
        return XMLConverter.getNodeByXPath(priceModel,
                "Parameters/Parameter[@id='" + parameterId
                        + "' and ParameterUsagePeriod[@startDate='" + start
                        + "' and @endDate='" + end + "']]");
    }

    public static Node getParameterNode(Node priceModel, String parameterId,
            long startDate, long endDate) throws XPathExpressionException {
        return getParameterNode(priceModel, parameterId, startDate + "",
                endDate + "");
    }

    public static Node getParameterNode(Node priceModel, String parameterId,
            String startDate, String endDate, String value, String type)
            throws XPathExpressionException {

        return XMLConverter.getNodeByXPath(priceModel,
                "Parameters/Parameter[@id='" + parameterId
                        + "' and ParameterUsagePeriod[@startDate='" + startDate
                        + "' and @endDate='" + endDate
                        + "'] and ParameterValue[@type='" + type
                        + "' and @amount='" + value + "']]");
    }

    public static Node getParameterNode(Node priceModel, String parameterId,
            long startDate, long endDate, String value, String type)
            throws XPathExpressionException {
        return getParameterNode(priceModel, parameterId, startDate + "",
                endDate + "", value, type);
    }

    public static Node getSubscriptionNode(Document billingResult,
            String subscriptionId) throws XPathExpressionException {
        return XMLConverter.getNodeByXPath(billingResult,
                "//Subscription[@id='" + subscriptionId + "']");
    }

    public static Node getSubscriptionNode(Document billingResult,
            String subscriptionId, String purchaseOrderNumber)
            throws XPathExpressionException {
        return XMLConverter.getNodeByXPath(billingResult,
                "//Subscription[@id='" + subscriptionId
                        + "' and @purchaseOrderNumber='" + purchaseOrderNumber
                        + "']");
    }

    public static Node getSubscriptionNode(Document billingResult,
            String subscriptionId, long priceModelKey)
            throws XPathExpressionException {
        return XMLConverter.getNodeByXPath(billingResult,
                "//Subscription[@id='" + subscriptionId
                        + "' and PriceModels/PriceModel[@id='" + priceModelKey
                        + "']]");
    }

    public static Node getSubscriptionNode(Document billingResult,
            String subscriptionId, long priceModelKey,
            long priceModelUsageStart, long priceModelUsageEnd)
            throws XPathExpressionException {
        return XMLConverter.getNodeByXPath(billingResult,
                "//Subscription[@id='" + subscriptionId
                        + "' and PriceModels/PriceModel[@id='" + priceModelKey
                        + "']/UsagePeriod[@startDate='" + priceModelUsageStart
                        + "' and @endDate='" + priceModelUsageEnd + "']]");
    }

    public static Node getPriceModelNode(Node subscription, long priceModelKey)
            throws XPathExpressionException {
        return XMLConverter.getNodeByXPath(subscription,
                "PriceModels/PriceModel[@id='" + priceModelKey + "']");
    }

    public static Node getPriceModelNode(Node subscription, String serviceId,
            long priceModelKey, String calculationMode)
            throws XPathExpressionException {
        return XMLConverter.getNodeByXPath(subscription,
                "PriceModels/PriceModel[@id='" + priceModelKey
                        + "' and @calculationMode='" + calculationMode
                        + "' and @serviceId='" + serviceId + "']");
    }

    public static Node getPriceModelNode(Document billingResult,
            long priceModelKey) throws XPathExpressionException {
        return XMLConverter.getNodeByXPath(billingResult, "//PriceModel[@id='"
                + priceModelKey + "']");
    }

    public static Node getPriceModelNode(Document billingResult,
            long startDate, long endDate) throws XPathExpressionException {
        Node n = XMLConverter.getNodeByXPath(billingResult,
                "//PriceModel/UsagePeriod[@startDate='" + startDate
                        + "' and @endDate='" + endDate + "']");
        if (n != null) {
            return n.getParentNode();
        } else {
            return null;
        }
    }

    public static Node getPriceModelNode(Document billingResult,
            long priceModelKey, long startDate, long endDate)
            throws XPathExpressionException {
        Node n = XMLConverter.getNodeByXPath(billingResult,
                "//PriceModel[@id='" + priceModelKey
                        + "']/UsagePeriod[@startDate='" + startDate
                        + "' and @endDate='" + endDate + "']");
        if (n != null) {
            return n.getParentNode();
        } else {
            return null;
        }
    }

    public static Node getPriceModelNode(Document billingResult,
            long priceModelKey, String startDate, String endDate)
            throws XPathExpressionException {

        long start = DateTimeHandling.calculateMillis(startDate);
        long end = DateTimeHandling.calculateMillis(endDate);
        Node n = XMLConverter.getNodeByXPath(billingResult,
                "//PriceModel[@id='" + priceModelKey
                        + "']/UsagePeriod[@startDate='" + start
                        + "' and @endDate='" + end + "']");
        return n.getParentNode();
    }

    public static Node getPriceModelNode(Document billingResult,
            String serviceId, long priceModelKey, String calculationMode)
            throws XPathExpressionException {
        return XMLConverter.getNodeByXPath(billingResult, "//PriceModel[@id='"
                + priceModelKey + "' and @calculationMode='" + calculationMode
                + "' and @serviceId='" + serviceId + "']");
    }

    public static Node getParameterOptionNode(Node parameter) {
        Node options = XMLConverter.getLastChildNode(parameter,
                BillingResultXMLTags.OPTIONS_NODE_NAME);
        Node option = XMLConverter.getLastChildNode(options,
                BillingResultXMLTags.OPTION_NODE_NAME);
        return option;
    }

    public static Node getRoleCostsNode(Node parent) {
        Node userAssignmentCosts = XMLConverter.getLastChildNode(parent,
                BillingResultXMLTags.USER_ASSIGNMENT_COSTS_NODE_NAME);
        return XMLConverter.getLastChildNode(userAssignmentCosts,
                BillingResultXMLTags.ROLE_COSTS_NODE_NAME);
    }

    public static Node getUserAssignmentSteppedPricesNode(Node priceModelNode) {
        Node userAssignmentCosts = XMLConverter.getLastChildNode(
                priceModelNode,
                BillingResultXMLTags.USER_ASSIGNMENT_COSTS_NODE_NAME);
        return XMLConverter.getLastChildNode(userAssignmentCosts,
                BillingResultXMLTags.STEPPED_PRICES_NODE_NAME);
    }

    public static Node getParameterSteppedPricesNode(Node parameterNode) {
        Node periodFee = XMLConverter.getLastChildNode(parameterNode,
                BillingResultXMLTags.PERIOD_FEE_NODE_NAME);
        return XMLConverter.getLastChildNode(periodFee,
                BillingResultXMLTags.STEPPED_PRICES_NODE_NAME);
    }

    public static Node getGatheredEventsNode(Node parent) {
        return XMLConverter.getLastChildNode(parent,
                BillingResultXMLTags.GATHERED_EVENTS_NODE_NAME);
    }

    public static Node getEventNode(Node gatheredEventsNode, String eventId)
            throws XPathExpressionException {
        return XMLConverter.getNodeByXPath(gatheredEventsNode, "Event[@id='"
                + eventId + "']");
    }

    public static Node getSteppedEventPricesNode(Node eventNode) {
        return XMLConverter.getLastChildNode(eventNode,
                BillingResultXMLTags.STEPPED_PRICES_NODE_NAME);
    }

    public static Node getOrganizationalUnitNodeForSubscription(
            Document billingResult, String subscriptionId, String unitName)
            throws XPathExpressionException {
        return XMLConverter.getNodeByXPath(billingResult,
                "//Subscription[@id='" + subscriptionId
                        + "']/OrganizationalUnit[@name='" + unitName + "']");
    }

    public static Node getOrganizationalUnitNodeForSubscription(
            Document billingResult, String subscriptionId, String unitName,
            String unitReferenceID) throws XPathExpressionException {
        return XMLConverter.getNodeByXPath(billingResult,
                "//Subscription[@id='" + subscriptionId
                        + "']/OrganizationalUnit[@name='" + unitName
                        + "' and @referenceID='" + unitReferenceID + "']");
    }

    public static Node getOrganizationalUnitNode(Document billingResult)
            throws XPathExpressionException {
        return XMLConverter.getNodeByXPath(billingResult,
                "//OrganizationalUnit");
    }

}
