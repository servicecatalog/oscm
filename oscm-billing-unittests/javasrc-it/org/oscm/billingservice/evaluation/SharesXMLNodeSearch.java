/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 24.03.2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.evaluation;

import java.util.Map;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;

import org.oscm.converter.XMLConverter;

/**
 * @author baumann
 * 
 */
public class SharesXMLNodeSearch {

    private static String currencyPathPrefix(String currency) {
        return "//Currency[@id=\"" + currency + "\"]";
    }

    private static String getServiceAttributeXPath(
            Map<String, String> serviceAttributes) {
        StringBuffer attributeXPath = new StringBuffer();
        for (String attributeName : serviceAttributes.keySet()) {
            if (attributeXPath.length() > 0) {
                attributeXPath.append(" and ");
            }
            attributeXPath.append("@").append(attributeName).append("='")
                    .append(serviceAttributes.get(attributeName)).append("'");
        }
        return attributeXPath.toString();
    }

    public static Node getSubscriptionNode(Node document, String currency,
            String marketplaceId, Map<String, String> serviceAttributes,
            String subscrId) throws Exception {

        String xpath = String.format(
                "/Marketplace[@id='%s']/Service[%s]/Subscription[@id='%s']",
                marketplaceId, getServiceAttributeXPath(serviceAttributes),
                subscrId);

        return XMLConverter.getNodeByXPath(document,
                currencyPathPrefix(currency) + xpath);
    }

    public static Node getSubscriptionsRevenueNode(Node document,
            String currency, String marketplaceId,
            Map<String, String> serviceAttributes) throws Exception {

        String xpath = String.format(
                "/Marketplace[@id='%s']/Service[%s]/SubscriptionsRevenue",
                marketplaceId, getServiceAttributeXPath(serviceAttributes));

        return XMLConverter.getNodeByXPath(document,
                currencyPathPrefix(currency) + xpath);
    }

    public static Node getRevenueShareDetailsNode(Node document,
            String currency, String marketplaceId,
            Map<String, String> serviceAttributes)
            throws XPathExpressionException {

        String xpath = String.format(
                "/Marketplace[@id='%s']/Service[%s]/RevenueShareDetails",
                marketplaceId, getServiceAttributeXPath(serviceAttributes));

        return XMLConverter.getNodeByXPath(document,
                currencyPathPrefix(currency) + xpath);
    }

    public static Node getCustomerRevenueShareDetailsNode(Node document,
            String currency, String marketplaceId,
            Map<String, String> serviceAttributes, String customerId)
            throws XPathExpressionException {

        String xpath = String
                .format("/Marketplace[@id='%s']/Service[%s]/RevenueShareDetails/CustomerRevenueShareDetails[@customerId='%s']",
                        marketplaceId,
                        getServiceAttributeXPath(serviceAttributes), customerId);

        return XMLConverter.getNodeByXPath(document,
                currencyPathPrefix(currency) + xpath);
    }

    public static Node getRevenuePerMarketplaceNode(Node document,
            String currency, String marketplaceId)
            throws XPathExpressionException {
        String xpath = String.format(
                "/Marketplace[@id='%s']/RevenuePerMarketplace", marketplaceId);

        return XMLConverter.getNodeByXPath(document,
                currencyPathPrefix(currency) + xpath);
    }

    public static Node getSupplierRevenueNode(Node document, String currency)
            throws XPathExpressionException {
        return XMLConverter.getNodeByXPath(document,
                currencyPathPrefix(currency) + "/SupplierRevenue");
    }

    public static Node getDirectRevenueNode(Node parent)
            throws XPathExpressionException {
        return XMLConverter.getNodeByXPath(parent, ".//DirectRevenue");
    }

    public static Node getBrokerRevenueNode(Node parent)
            throws XPathExpressionException {
        return XMLConverter.getNodeByXPath(parent, ".//BrokerRevenue");
    }

    public static Node getResellerRevenueNode(Node parent)
            throws XPathExpressionException {
        return XMLConverter.getNodeByXPath(parent, ".//ResellerRevenue");
    }

    public static Node getRevenuesOverAllMarketplacesNode(Node document,
            String currency) throws XPathExpressionException {
        return XMLConverter.getNodeByXPath(document,
                currencyPathPrefix(currency) + "/RevenuesOverAllMarketplaces");
    }

    public static Node getRevenuesPerMarketplaceNode(Node document,
            String currency, String marketplaceId)
            throws XPathExpressionException {
        return XMLConverter.getNodeByXPath(document,
                currencyPathPrefix(currency) + "/Marketplace[@id=\""
                        + marketplaceId + "\"]/RevenuesPerMarketplace");
    }

    public static Node getSuppliersNode(Node parent)
            throws XPathExpressionException {
        return XMLConverter.getNodeByXPath(parent, ".//Suppliers");
    }

    public static Node getSuppliersOrgNode(Node parent, String organizationId)
            throws XPathExpressionException {
        String xpath = String.format(
                ".//Suppliers/Organization[@identifier='%s']", organizationId);

        return XMLConverter.getNodeByXPath(parent, xpath);
    }

    public static Node getBrokersNode(Node parent)
            throws XPathExpressionException {
        return XMLConverter.getNodeByXPath(parent, ".//Brokers");
    }

    public static Node getBrokersOrgNode(Node parent, String organizationId)
            throws XPathExpressionException {
        String xpath = String.format(
                ".//Brokers/Organization[@identifier='%s']", organizationId);

        return XMLConverter.getNodeByXPath(parent, xpath);
    }

    public static Node getResellersNode(Node parent)
            throws XPathExpressionException {
        return XMLConverter.getNodeByXPath(parent, ".//Resellers");
    }

    public static Node getResellersOrgNode(Node parent, String organizationId)
            throws XPathExpressionException {
        String xpath = String.format(
                ".//Resellers/Organization[@identifier='%s']", organizationId);

        return XMLConverter.getNodeByXPath(parent, xpath);
    }

    public static Node getMarketplaceOwnerNode(Node parent)
            throws XPathExpressionException {
        return XMLConverter.getNodeByXPath(parent, ".//MarketplaceOwner");
    }

    public static Node getServiceRevenueNode(Node document, String currency,
            String supplierId, Map<String, String> serviceAttributes)
            throws XPathExpressionException {

        String xpath = String
                .format("/Supplier[.//OrganizationData[@id='%s']]/Service[%s]/ServiceRevenue",
                        supplierId, getServiceAttributeXPath(serviceAttributes));

        return XMLConverter.getNodeByXPath(document,
                currencyPathPrefix(currency) + xpath);
    }

    public static Node getServiceCustomerRevenueNode(Node document,
            String currency, String supplierId,
            Map<String, String> serviceAttributes, String customerId)
            throws XPathExpressionException {

        String xpath = String
                .format("/Supplier[.//OrganizationData[@id='%s']]/Service[%s]/ServiceRevenue/ServiceCustomerRevenue[@customerId='%s']",
                        supplierId,
                        getServiceAttributeXPath(serviceAttributes), customerId);

        return XMLConverter.getNodeByXPath(document,
                currencyPathPrefix(currency) + xpath);
    }

    public static Node getBrokerRevenuePerSupplierNode(Node document,
            String currency, String supplierId) throws XPathExpressionException {

        String xpath = String
                .format("/Supplier[.//OrganizationData[@id='%s']]/BrokerRevenuePerSupplier",
                        supplierId);

        return XMLConverter.getNodeByXPath(document,
                currencyPathPrefix(currency) + xpath);
    }

    public static Node getBrokerRevenueNode(Node document, String currency)
            throws XPathExpressionException {

        return XMLConverter.getNodeByXPath(document,
                currencyPathPrefix(currency) + "/BrokerRevenue");
    }

    public static Node getResellerRevenuePerSupplierNode(Node document,
            String currency, String supplierId) throws XPathExpressionException {

        String xpath = String
                .format("/Supplier[.//OrganizationData[@id='%s']]/ResellerRevenuePerSupplier",
                        supplierId);

        return XMLConverter.getNodeByXPath(document,
                currencyPathPrefix(currency) + xpath);
    }

    public static Node getResellerRevenueNode(Node document, String currency)
            throws XPathExpressionException {

        return XMLConverter.getNodeByXPath(document,
                currencyPathPrefix(currency) + "/ResellerRevenue");
    }

    public static Node getSubscriptionNodeForSupplierService(Node document,
            String currency, String supplierId,
            Map<String, String> serviceAttributes, String subscrId)
            throws XPathExpressionException {

        String xpath = String
                .format("/Supplier[.//OrganizationData[@id='%s']]/Service[%s]/Subscription[@id='%s']",
                        supplierId,
                        getServiceAttributeXPath(serviceAttributes), subscrId);

        return XMLConverter.getNodeByXPath(document,
                currencyPathPrefix(currency) + xpath);
    }

}
