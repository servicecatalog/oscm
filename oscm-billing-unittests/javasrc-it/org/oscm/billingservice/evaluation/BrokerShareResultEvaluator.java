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
public class BrokerShareResultEvaluator extends ShareResultEvaluator {

    /**
     * All asserts of this object are performed against the first found currency
     * in the supplier revenue share document. To change the currency call the
     * setCurrency method.
     */
    public BrokerShareResultEvaluator(Document xml)
            throws XPathExpressionException {
        super(xml, "BrokerRevenueShareResult");
    }

    /**
     * All asserts of this object are performed against the provided currency
     * iso code. To change the currency used, call the setCurrency method.
     */
    public BrokerShareResultEvaluator(Document xml, String currency) {
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

    public void assertServiceRevenue(String supplierId, VOService service,
            String expectedtotalAmount, String expectedBrokerPercentage,
            String expectedBrokerRevenue) throws Exception {

        Node serviceRevenue = SharesXMLNodeSearch.getServiceRevenueNode(
                document, currency, supplierId, getServiceAttributes(service));
        assertNotNull(serviceRevenue);

        assertAttribute(serviceRevenue,
                BillingShareResultXmlTags.ATTRIBUTE_NAME_TOTAL_AMOUNT,
                expectedtotalAmount);

        assertAttribute(
                serviceRevenue,
                BillingShareResultXmlTags.ATTRIBUTE_NAME_BROKER_REVENUE_SHARE_PERCENTAGE,
                expectedBrokerPercentage);

        assertAttribute(serviceRevenue,
                BillingShareResultXmlTags.ATTRIBUTE_NAME_BROKER_REVENUE,
                expectedBrokerRevenue);
    }

    public void assertServiceCustomerRevenue(String supplierId,
            VOService service, String customerId, String expectedtotalAmount,
            String expectedBrokerPercentage, String expectedBrokerRevenue)
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
                BillingShareResultXmlTags.ATTRIBUTE_NAME_BROKER_REVENUE_SHARE_PERCENTAGE,
                expectedBrokerPercentage);

        assertAttribute(serviceCustomerRevenue,
                BillingShareResultXmlTags.ATTRIBUTE_NAME_BROKER_REVENUE,
                expectedBrokerRevenue);
    }

    public void assertBrokerRevenuePerSupplier(String supplierId,
            String expectedtotalAmount, String expectedAmount) throws Exception {
        Node brokerRevenuePerSupplier = SharesXMLNodeSearch
                .getBrokerRevenuePerSupplierNode(document, currency, supplierId);
        assertNotNull(brokerRevenuePerSupplier);

        assertAttribute(brokerRevenuePerSupplier,
                BillingShareResultXmlTags.ATTRIBUTE_NAME_TOTAL_AMOUNT,
                expectedtotalAmount);

        assertAttribute(brokerRevenuePerSupplier,
                BillingShareResultXmlTags.ATTRIBUTE_NAME_AMOUNT, expectedAmount);
    }

    public void assertBrokerRevenue(String expectedtotalAmount,
            String expectedAmount) throws Exception {
        Node brokerRevenue = SharesXMLNodeSearch.getBrokerRevenueNode(document,
                currency);
        assertNotNull(brokerRevenue);

        assertAttribute(brokerRevenue,
                BillingShareResultXmlTags.ATTRIBUTE_NAME_TOTAL_AMOUNT,
                expectedtotalAmount);

        assertAttribute(brokerRevenue,
                BillingShareResultXmlTags.ATTRIBUTE_NAME_AMOUNT, expectedAmount);
    }

}
