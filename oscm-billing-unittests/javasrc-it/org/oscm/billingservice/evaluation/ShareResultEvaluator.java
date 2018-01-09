/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: Mar 19, 2014                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.evaluation;

import static junit.framework.Assert.assertEquals;

import java.util.Map;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import org.oscm.converter.XMLConverter;
import org.oscm.types.constants.BillingShareResultXmlTags;
import org.oscm.internal.vo.VOService;

/**
 * @author farmaki
 * 
 */
public abstract class ShareResultEvaluator {

    protected final Document document;
    protected String currency;

    public ShareResultEvaluator(Document xml) {
        this.document = xml;
    }

    public ShareResultEvaluator(Document xml, String rootElement)
            throws XPathExpressionException {
        this.document = xml;
        this.currency = XMLConverter.getNodeTextContentByXPath(document, "/"
                + rootElement + "/Currency[1]/@id");
    }

    public abstract Map<String, String> getServiceAttributes(VOService service);

    public void setCurrency(String currency) {
        this.currency = currency.trim().toUpperCase();
    }

    public Document getDocument() {
        return document;
    }

    protected void assertAttribute(Node node, String attributeName,
            String expectedAttributeValue) {
        String actualAttributeValue = XMLConverter.getStringAttValue(node,
                attributeName);
        assertEquals(expectedAttributeValue, actualAttributeValue);
    }

    public Node assertRevenueShareDetails(String marketplaceId,
            VOService service, String expectedServiceRevenue,
            String expectedMarketplacePercentage,
            String expectedMarketplaceRevenue,
            String expectedOperatorPercentage, String expectedOperatorRevenue,
            String expectedSupplierAmount) throws Exception {

        Node revenueShareDetails = SharesXMLNodeSearch
                .getRevenueShareDetailsNode(document, currency, marketplaceId,
                        getServiceAttributes(service));

        assertAttribute(revenueShareDetails,
                BillingShareResultXmlTags.ATTRIBUTE_NAME_SERVICE_REVENUE,
                expectedServiceRevenue);

        assertAttribute(
                revenueShareDetails,
                BillingShareResultXmlTags.ATTRIBUTE_NAME_MARKETPLACE_REVENUE_SHARE_PERCENTAGE,
                expectedMarketplacePercentage);

        assertAttribute(revenueShareDetails,
                BillingShareResultXmlTags.ATTRIBUTE_NAME_MARKETPLACE_REVENUE,
                expectedMarketplaceRevenue);

        assertAttribute(
                revenueShareDetails,
                BillingShareResultXmlTags.ATTRIBUTE_NAME_OPERATOR_REVENUE_SHARE_PERCENTAGE,
                expectedOperatorPercentage);

        assertAttribute(revenueShareDetails,
                BillingShareResultXmlTags.ATTRIBUTE_NAME_OPERATOR_REVENUE,
                expectedOperatorRevenue);

        assertAttribute(revenueShareDetails,
                BillingShareResultXmlTags.ATTRIBUTE_NAME_AMOUNT_FOR_SUPPLIER,
                expectedSupplierAmount);

        return revenueShareDetails;
    }

    public void assertRevenueShareDetails_BrokerService(String marketplaceId,
            VOService service, String expectedServiceRevenue,
            String expectedMarketplacePercentage,
            String expectedMarketplaceRevenue,
            String expectedOperatorPercentage, String expectedOperatorRevenue,
            String expectedBrokerPercentage, String expectedBrokerRevenue,
            String expectedSupplierAmount) throws Exception {

        Node revenueShareDetails = assertRevenueShareDetails(marketplaceId,
                service, expectedServiceRevenue, expectedMarketplacePercentage,
                expectedMarketplaceRevenue, expectedOperatorPercentage,
                expectedOperatorRevenue, expectedSupplierAmount);

        assertAttribute(
                revenueShareDetails,
                BillingShareResultXmlTags.ATTRIBUTE_NAME_BROKER_REVENUE_SHARE_PERCENTAGE,
                expectedBrokerPercentage);

        assertAttribute(revenueShareDetails,
                BillingShareResultXmlTags.ATTRIBUTE_NAME_BROKER_REVENUE,
                expectedBrokerRevenue);
    }

    public void assertRevenueShareDetails_ResellerService(String marketplaceId,
            VOService service, String expectedServiceRevenue,
            String expectedMarketplacePercentage,
            String expectedMarketplaceRevenue,
            String expectedOperatorPercentage, String expectedOperatorRevenue,
            String expectedResellerPercentage, String expectedResellerRevenue,
            String expectedSupplierAmount) throws Exception {

        Node revenueShareDetails = assertRevenueShareDetails(marketplaceId,
                service, expectedServiceRevenue, expectedMarketplacePercentage,
                expectedMarketplaceRevenue, expectedOperatorPercentage,
                expectedOperatorRevenue, expectedSupplierAmount);

        assertAttribute(
                revenueShareDetails,
                BillingShareResultXmlTags.ATTRIBUTE_NAME_RESELLER_REVENUE_SHARE_PERCENTAGE,
                expectedResellerPercentage);

        assertAttribute(revenueShareDetails,
                BillingShareResultXmlTags.ATTRIBUTE_NAME_RESELLER_REVENUE,
                expectedResellerRevenue);
    }
}
