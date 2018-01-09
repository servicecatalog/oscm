/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 10.04.2014                                                      
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
public class ResellerShareResultEvaluator extends ShareResultEvaluator {

    /**
     * All asserts of this object are performed against the first found currency
     * in the supplier revenue share document. To change the currency call the
     * setCurrency method.
     */
    public ResellerShareResultEvaluator(Document xml)
            throws XPathExpressionException {
        super(xml, "ResellerRevenueShareResult");
    }

    /**
     * All asserts of this object are performed against the provided currency
     * iso code. To change the currency used, call the setCurrency method.
     */
    public ResellerShareResultEvaluator(Document xml, String currency) {
        super(xml);
        setCurrency(currency);
    }

    @Override
    public Map<String, String> getServiceAttributes(VOService service) {
        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put("id", service.getServiceId());
        attributes.put("key", Long.toString(service.getKey()));
        return attributes;
    }

    public void assertSubscription(String supplierId, String subscrId,
            VOService service, String expectedRevenue) throws Exception {

        Node subscriptionNode = SharesXMLNodeSearch
                .getSubscriptionNodeForSupplierService(document, currency,
                        supplierId, getServiceAttributes(service), subscrId);
        assertNotNull(subscriptionNode);

        assertAttribute(subscriptionNode,
                BillingShareResultXmlTags.ATTRIBUTE_NAME_REVENUE,
                expectedRevenue);
    }

    public void assertServiceRevenue(String supplierId, VOService service,
            String expectedtotalAmount, String expectedResellerPercentage,
            String expectedResellerRevenue) throws Exception {

        Node serviceRevenue = SharesXMLNodeSearch.getServiceRevenueNode(
                document, currency, supplierId, getServiceAttributes(service));
        assertNotNull(serviceRevenue);

        assertAttribute(serviceRevenue,
                BillingShareResultXmlTags.ATTRIBUTE_NAME_TOTAL_AMOUNT,
                expectedtotalAmount);

        assertAttribute(
                serviceRevenue,
                BillingShareResultXmlTags.ATTRIBUTE_NAME_RESELLER_REVENUE_SHARE_PERCENTAGE,
                expectedResellerPercentage);

        assertAttribute(serviceRevenue,
                BillingShareResultXmlTags.ATTRIBUTE_NAME_RESELLER_REVENUE,
                expectedResellerRevenue);
    }

    public void assertServiceCustomerRevenue(String supplierId,
            VOService service, String customerId, String expectedtotalAmount,
            String expectedResellerPercentage, String expectedResellerRevenue)
            throws Exception {
        Node serviceCustomerRevenue = SharesXMLNodeSearch
                .getServiceCustomerRevenueNode(document, currency, supplierId,
                        getServiceAttributes(service), customerId);
        assertNotNull(serviceCustomerRevenue);

        assertAttribute(serviceCustomerRevenue,
                BillingShareResultXmlTags.ATTRIBUTE_NAME_TOTAL_AMOUNT,
                expectedtotalAmount);

        assertAttribute(
                serviceCustomerRevenue,
                BillingShareResultXmlTags.ATTRIBUTE_NAME_RESELLER_REVENUE_SHARE_PERCENTAGE,
                expectedResellerPercentage);

        assertAttribute(serviceCustomerRevenue,
                BillingShareResultXmlTags.ATTRIBUTE_NAME_RESELLER_REVENUE,
                expectedResellerRevenue);
    }

    public void assertResellerRevenuePerSupplier(String supplierId,
            String expectedtotalAmount, String expectedAmount,
            String expectedPurchasePrice) throws Exception {
        Node resellerRevenuePerSupplier = SharesXMLNodeSearch
                .getResellerRevenuePerSupplierNode(document, currency,
                        supplierId);
        assertNotNull(resellerRevenuePerSupplier);

        assertAttribute(resellerRevenuePerSupplier,
                BillingShareResultXmlTags.ATTRIBUTE_NAME_TOTAL_AMOUNT,
                expectedtotalAmount);

        assertAttribute(resellerRevenuePerSupplier,
                BillingShareResultXmlTags.ATTRIBUTE_NAME_AMOUNT, expectedAmount);

        assertAttribute(resellerRevenuePerSupplier,
                BillingShareResultXmlTags.ATTRIBUTE_NAME_PURCHASE_PRICE,
                expectedPurchasePrice);
    }

    public void assertResellerRevenue(String expectedtotalAmount,
            String expectedAmount, String expectedPurchasePrice)
            throws Exception {
        Node resellerRevenue = SharesXMLNodeSearch.getResellerRevenueNode(
                document, currency);
        assertNotNull(resellerRevenue);

        assertAttribute(resellerRevenue,
                BillingShareResultXmlTags.ATTRIBUTE_NAME_TOTAL_AMOUNT,
                expectedtotalAmount);

        assertAttribute(resellerRevenue,
                BillingShareResultXmlTags.ATTRIBUTE_NAME_AMOUNT, expectedAmount);

        assertAttribute(resellerRevenue,
                BillingShareResultXmlTags.ATTRIBUTE_NAME_PURCHASE_PRICE,
                expectedPurchasePrice);
    }

}
