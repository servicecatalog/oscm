/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 10.09.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.evaluation;

import static junit.framework.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import org.oscm.types.constants.BillingShareResultXmlTags;
import org.oscm.internal.vo.VOService;

/**
 * @author baumann
 * 
 */
public class SupplierShareResultEvaluator extends ShareResultEvaluator {

    /**
     * All asserts of this object are performed against the first found currency
     * in the supplier revenue share document. To change the currency call the
     * setCurrency method.
     */
    public SupplierShareResultEvaluator(Document xml)
            throws XPathExpressionException {
        super(xml, "SupplierRevenueShareResult");
    }

    /**
     * All asserts of this object are performed against the provided currency
     * iso code. To change the currency used, call the setCurrency method.
     */
    public SupplierShareResultEvaluator(Document xml, String currency) {
        super(xml);
        setCurrency(currency);
    }

    @Override
    public Map<String, String> getServiceAttributes(VOService service) {
        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put("id", service.getServiceId());
        attributes.put("key", Long.toString(service.getKey()));
        attributes.put("model", service.getOfferingType().name());
        return attributes;
    }

    public void assertSubscription(String marketplaceId, String subscrId,
            VOService service, String expectedRevenue) throws Exception {

        Node subscriptionNode = SharesXMLNodeSearch.getSubscriptionNode(
                document, currency, marketplaceId,
                getServiceAttributes(service), subscrId);
        assertNotNull(subscriptionNode);

        assertAttribute(subscriptionNode,
                BillingShareResultXmlTags.ATTRIBUTE_NAME_REVENUE,
                expectedRevenue);
    }

    public void assertSubscriptionsRevenue(String marketplaceId,
            VOService service, String expectedAmount) throws Exception {

        Node subscriptionNode = SharesXMLNodeSearch
                .getSubscriptionsRevenueNode(document, currency, marketplaceId,
                        getServiceAttributes(service));
        assertNotNull(subscriptionNode);

        assertAttribute(subscriptionNode,
                BillingShareResultXmlTags.ATTRIBUTE_NAME_AMOUNT, expectedAmount);
    }

    public Node assertCustomerRevenueShareDetails(String marketplaceId,
            VOService service, String customerId,
            String expectedServiceRevenue, String expectedMarketplaceRevenue,
            String expectedOperatorRevenue, String expectedSupplierAmount)
            throws Exception {
        Node customerRevenueShareDetails = SharesXMLNodeSearch
                .getCustomerRevenueShareDetailsNode(document, currency,
                        marketplaceId, getServiceAttributes(service),
                        customerId);

        assertNotNull(customerRevenueShareDetails);

        assertAttribute(customerRevenueShareDetails,
                BillingShareResultXmlTags.ATTRIBUTE_NAME_SERVICE_REVENUE,
                expectedServiceRevenue);

        assertAttribute(customerRevenueShareDetails,
                BillingShareResultXmlTags.ATTRIBUTE_NAME_MARKETPLACE_REVENUE,
                expectedMarketplaceRevenue);

        assertAttribute(customerRevenueShareDetails,
                BillingShareResultXmlTags.ATTRIBUTE_NAME_OPERATOR_REVENUE,
                expectedOperatorRevenue);

        assertAttribute(customerRevenueShareDetails,
                BillingShareResultXmlTags.ATTRIBUTE_NAME_AMOUNT_FOR_SUPPLIER,
                expectedSupplierAmount);

        return customerRevenueShareDetails;
    }

    public void assertCustomerRevenueShareDetails_BrokerService(
            String marketplaceId, VOService service, String customerId,
            String expectedServiceRevenue, String expectedMarketplaceRevenue,
            String expectedOperatorRevenue, String expectedSupplierAmount,
            String expectedBrokerRevenue) throws Exception {
        Node customerRevenueShareDetails = assertCustomerRevenueShareDetails(
                marketplaceId, service, customerId, expectedServiceRevenue,
                expectedMarketplaceRevenue, expectedOperatorRevenue,
                expectedSupplierAmount);

        assertAttribute(customerRevenueShareDetails,
                BillingShareResultXmlTags.ATTRIBUTE_NAME_BROKER_REVENUE,
                expectedBrokerRevenue);
    }

    public void assertCustomerRevenueShareDetails_ResellerService(
            String marketplaceId, VOService service, String customerId,
            String expectedServiceRevenue, String expectedMarketplaceRevenue,
            String expectedOperatorRevenue, String expectedSupplierAmount,
            String expectedResellerRevenue) throws Exception {
        Node customerRevenueShareDetails = assertCustomerRevenueShareDetails(
                marketplaceId, service, customerId, expectedServiceRevenue,
                expectedMarketplaceRevenue, expectedOperatorRevenue,
                expectedSupplierAmount);

        assertAttribute(customerRevenueShareDetails,
                BillingShareResultXmlTags.ATTRIBUTE_NAME_RESELLER_REVENUE,
                expectedResellerRevenue);
    }

    public void assertRevenuePerMarketplace(String marketplaceId,
            String expectedServiceRevenue, String expectedMarketplaceRevenue,
            String expectedOperatorRevenue, String expectedResellerRevenue,
            String expectedBrokerRevenue, String expectedOverallRevenue)
            throws Exception {

        Node revenuePerMarketplaceNode = SharesXMLNodeSearch
                .getRevenuePerMarketplaceNode(document, currency, marketplaceId);
        assertNotNull(revenuePerMarketplaceNode);

        assertAttribute(revenuePerMarketplaceNode,
                BillingShareResultXmlTags.ATTRIBUTE_NAME_SERVICE_REVENUE,
                expectedServiceRevenue);

        assertAttribute(revenuePerMarketplaceNode,
                BillingShareResultXmlTags.ATTRIBUTE_NAME_MARKETPLACE_REVENUE,
                expectedMarketplaceRevenue);

        assertAttribute(revenuePerMarketplaceNode,
                BillingShareResultXmlTags.ATTRIBUTE_NAME_OPERATOR_REVENUE,
                expectedOperatorRevenue);

        assertAttribute(revenuePerMarketplaceNode,
                BillingShareResultXmlTags.ATTRIBUTE_NAME_RESELLER_REVENUE,
                expectedResellerRevenue);

        assertAttribute(revenuePerMarketplaceNode,
                BillingShareResultXmlTags.ATTRIBUTE_NAME_BROKER_REVENUE,
                expectedBrokerRevenue);

        assertAttribute(revenuePerMarketplaceNode,
                BillingShareResultXmlTags.ATTRIBUTE_NAME_OVERALLREVENUE,
                expectedOverallRevenue);
    }

    public void assertSupplierRevenue(String expectedAmount) throws Exception {
        Node supplierRevenueNode = SharesXMLNodeSearch.getSupplierRevenueNode(
                document, currency);
        assertNotNull(supplierRevenueNode);

        assertAttribute(supplierRevenueNode,
                BillingShareResultXmlTags.ATTRIBUTE_NAME_AMOUNT, expectedAmount);
    }

    public void assertDirectRevenue(String expectedServiceRevenue,
            String expectedMarketplaceRevenue, String expectedOperatorRevenue)
            throws Exception {
        Node directRevenueNode = SharesXMLNodeSearch
                .getDirectRevenueNode(SharesXMLNodeSearch
                        .getSupplierRevenueNode(document, currency));
        assertNotNull(directRevenueNode);

        assertAttribute(directRevenueNode,
                BillingShareResultXmlTags.ATTRIBUTE_NAME_SERVICE_REVENUE,
                expectedServiceRevenue);

        assertAttribute(directRevenueNode,
                BillingShareResultXmlTags.ATTRIBUTE_NAME_MARKETPLACE_REVENUE,
                expectedMarketplaceRevenue);

        assertAttribute(directRevenueNode,
                BillingShareResultXmlTags.ATTRIBUTE_NAME_OPERATOR_REVENUE,
                expectedOperatorRevenue);
    }

    public void assertBrokerRevenue(String expectedServiceRevenue,
            String expectedMarketplaceRevenue, String expectedOperatorRevenue,
            String expectedBrokerRevenue) throws Exception {
        Node brokerRevenueNode = SharesXMLNodeSearch
                .getBrokerRevenueNode(SharesXMLNodeSearch
                        .getSupplierRevenueNode(document, currency));
        assertNotNull(brokerRevenueNode);

        assertAttribute(brokerRevenueNode,
                BillingShareResultXmlTags.ATTRIBUTE_NAME_SERVICE_REVENUE,
                expectedServiceRevenue);

        assertAttribute(brokerRevenueNode,
                BillingShareResultXmlTags.ATTRIBUTE_NAME_MARKETPLACE_REVENUE,
                expectedMarketplaceRevenue);

        assertAttribute(brokerRevenueNode,
                BillingShareResultXmlTags.ATTRIBUTE_NAME_OPERATOR_REVENUE,
                expectedOperatorRevenue);

        assertAttribute(brokerRevenueNode,
                BillingShareResultXmlTags.ATTRIBUTE_NAME_BROKER_REVENUE,
                expectedBrokerRevenue);
    }

    public void assertResellerRevenue(String expectedServiceRevenue,
            String expectedMarketplaceRevenue, String expectedOperatorRevenue,
            String expectedResellerRevenue, String expectedOverallRevenue)
            throws Exception {
        Node resellerRevenueNode = SharesXMLNodeSearch
                .getResellerRevenueNode(SharesXMLNodeSearch
                        .getSupplierRevenueNode(document, currency));
        assertNotNull(resellerRevenueNode);

        assertAttribute(resellerRevenueNode,
                BillingShareResultXmlTags.ATTRIBUTE_NAME_SERVICE_REVENUE,
                expectedServiceRevenue);

        assertAttribute(resellerRevenueNode,
                BillingShareResultXmlTags.ATTRIBUTE_NAME_MARKETPLACE_REVENUE,
                expectedMarketplaceRevenue);

        assertAttribute(resellerRevenueNode,
                BillingShareResultXmlTags.ATTRIBUTE_NAME_OPERATOR_REVENUE,
                expectedOperatorRevenue);

        assertAttribute(resellerRevenueNode,
                BillingShareResultXmlTags.ATTRIBUTE_NAME_RESELLER_REVENUE,
                expectedResellerRevenue);

        assertAttribute(resellerRevenueNode,
                BillingShareResultXmlTags.ATTRIBUTE_NAME_OVERALLREVENUE,
                expectedOverallRevenue);
    }

}
