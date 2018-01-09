/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2018
 *                                                                                                                                 
 *  Creation Date: 23.09.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.evaluation;

import static junit.framework.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import org.oscm.types.constants.BillingShareResultXmlTags;
import org.oscm.internal.vo.VOService;

/**
 * @author baumann
 * 
 */
public class MarketplaceShareResultEvaluator extends ShareResultEvaluator {

    /**
     * All asserts of this object are performed against the first found currency
     * in the marketplace revenue share document. To change the currency call
     * the setCurrency method.
     */
    public MarketplaceShareResultEvaluator(Document xml) throws Exception {
        super(xml, "MarketplaceOwnerRevenueShareResult");
    }

    /**
     * All asserts of this object are performed against the provided currency
     * iso code. To change the currency used, call the setCurrency method.
     */
    public MarketplaceShareResultEvaluator(Document xml, String currency) {
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

    public void assertOverallSuppliers(String expectedAmount,
            String expectedTotalAmount, String expectedMarketplaceRevenue)
            throws Exception {
        Node suppliersNode = SharesXMLNodeSearch
                .getSuppliersNode(SharesXMLNodeSearch
                        .getRevenuesOverAllMarketplacesNode(document, currency));

        assertRevenues(suppliersNode, expectedAmount, expectedTotalAmount,
                expectedMarketplaceRevenue);

    }

    public void assertOverallSuppliersOrganization(String organizationId,
            String expectedAmount, String expectedTotalAmount,
            String expectedMarketplaceRevenue) throws Exception {

        Node supplierOrgNode = SharesXMLNodeSearch.getSuppliersOrgNode(
                SharesXMLNodeSearch.getRevenuesOverAllMarketplacesNode(
                        document, currency), organizationId);

        assertRevenues(supplierOrgNode, expectedAmount, expectedTotalAmount,
                expectedMarketplaceRevenue);
    }

    public void assertSuppliers(String marketplaceId, String expectedAmount,
            String expectedTotalAmount, String expectedMarketplaceRevenue)
            throws Exception {

        Node suppliersNode = SharesXMLNodeSearch
                .getSuppliersNode(SharesXMLNodeSearch
                        .getRevenuesPerMarketplaceNode(document, currency,
                                marketplaceId));

        assertRevenues(suppliersNode, expectedAmount, expectedTotalAmount,
                expectedMarketplaceRevenue);
    }

    public void assertSuppliersOrganization(String marketplaceId,
            String organizationId, String expectedAmount,
            String expectedTotalAmount, String expectedMarketplaceRevenue)
            throws Exception {

        Node supplierOrgNode = SharesXMLNodeSearch.getSuppliersOrgNode(
                SharesXMLNodeSearch.getRevenuesPerMarketplaceNode(document,
                        currency, marketplaceId), organizationId);

        assertRevenues(supplierOrgNode, expectedAmount, expectedTotalAmount,
                expectedMarketplaceRevenue);
    }

    public void assertOverallBrokers(String expectedAmount,
            String expectedTotalAmount, String expectedMarketplaceRevenue)
            throws Exception {

        Node brokersNode = SharesXMLNodeSearch
                .getBrokersNode(SharesXMLNodeSearch
                        .getRevenuesOverAllMarketplacesNode(document, currency));

        assertRevenues(brokersNode, expectedAmount, expectedTotalAmount,
                expectedMarketplaceRevenue);
    }

    public void assertOverallBrokersOrganization(String organizationId,
            String expectedAmount, String expectedTotalAmount,
            String expectedMarketplaceRevenue) throws Exception {

        Node brokerOrgNode = SharesXMLNodeSearch.getBrokersOrgNode(
                SharesXMLNodeSearch.getRevenuesOverAllMarketplacesNode(
                        document, currency), organizationId);

        assertRevenues(brokerOrgNode, expectedAmount, expectedTotalAmount,
                expectedMarketplaceRevenue);
    }

    public void assertBrokers(String marketplaceId, String expectedAmount,
            String expectedTotalAmount, String expectedMarketplaceRevenue)
            throws Exception {

        Node brokersNode = SharesXMLNodeSearch
                .getBrokersNode(SharesXMLNodeSearch
                        .getRevenuesPerMarketplaceNode(document, currency,
                                marketplaceId));

        assertRevenues(brokersNode, expectedAmount, expectedTotalAmount,
                expectedMarketplaceRevenue);
    }

    public void assertBrokersOrganization(String marketplaceId,
            String organizationId, String expectedAmount,
            String expectedTotalAmount, String expectedMarketplaceRevenue)
            throws Exception {

        Node brokerOrgNode = SharesXMLNodeSearch.getBrokersOrgNode(
                SharesXMLNodeSearch.getRevenuesPerMarketplaceNode(document,
                        currency, marketplaceId), organizationId);

        assertRevenues(brokerOrgNode, expectedAmount, expectedTotalAmount,
                expectedMarketplaceRevenue);
    }

    public void assertOverallResellers(String expectedAmount,
            String expectedTotalAmount, String expectedMarketplaceRevenue)
            throws Exception {

        Node resellersNode = SharesXMLNodeSearch
                .getResellersNode(SharesXMLNodeSearch
                        .getRevenuesOverAllMarketplacesNode(document, currency));

        assertRevenues(resellersNode, expectedAmount, expectedTotalAmount,
                expectedMarketplaceRevenue);
    }

    public void assertOverallResellersOrganization(String organizationId,
            String expectedAmount, String expectedTotalAmount,
            String expectedMarketplaceRevenue) throws Exception {

        Node resellerOrgNode = SharesXMLNodeSearch.getResellersOrgNode(
                SharesXMLNodeSearch.getRevenuesOverAllMarketplacesNode(
                        document, currency), organizationId);

        assertRevenues(resellerOrgNode, expectedAmount, expectedTotalAmount,
                expectedMarketplaceRevenue);
    }

    public void assertResellers(String marketplaceId, String expectedAmount,
            String expectedTotalAmount, String expectedMarketplaceRevenue)
            throws Exception {

        Node resellersNode = SharesXMLNodeSearch
                .getResellersNode(SharesXMLNodeSearch
                        .getRevenuesPerMarketplaceNode(document, currency,
                                marketplaceId));

        assertRevenues(resellersNode, expectedAmount, expectedTotalAmount,
                expectedMarketplaceRevenue);
    }

    public void assertResellersOrganization(String marketplaceId,
            String organizationId, String expectedAmount,
            String expectedTotalAmount, String expectedMarketplaceRevenue)
            throws Exception {

        Node resellerOrgNode = SharesXMLNodeSearch.getResellersOrgNode(
                SharesXMLNodeSearch.getRevenuesPerMarketplaceNode(document,
                        currency, marketplaceId), organizationId);

        assertRevenues(resellerOrgNode, expectedAmount, expectedTotalAmount,
                expectedMarketplaceRevenue);
    }

    /**
     * Asserts the revenues for the given node
     * 
     * @param node
     *            a Suppliers-, Brokers, Resellers- or Organization-node
     * @param expectedAmount
     *            the expected amount
     * @param expectedTotalAmount
     *            the expected total amount
     * @param expectedMarketplaceRevenue
     *            the expected marketplace revenue
     * @throws Exception
     */
    private void assertRevenues(Node node, String expectedAmount,
            String expectedTotalAmount, String expectedMarketplaceRevenue)
            throws Exception {

        assertNotNull(node);

        assertAttribute(node, BillingShareResultXmlTags.ATTRIBUTE_NAME_AMOUNT,
                expectedAmount);

        assertAttribute(node,
                BillingShareResultXmlTags.ATTRIBUTE_NAME_TOTAL_AMOUNT,
                expectedTotalAmount);

        assertAttribute(node,
                BillingShareResultXmlTags.ATTRIBUTE_NAME_MARKETPLACE_REVENUE,
                expectedMarketplaceRevenue);
    }

    public void assertMarketplaceOwner(String marketplaceId,
            String expectedAmount) throws Exception {

        Node marketplaceOwnerNode = SharesXMLNodeSearch
                .getMarketplaceOwnerNode(SharesXMLNodeSearch
                        .getRevenuesPerMarketplaceNode(document, currency,
                                marketplaceId));
        assertNotNull(marketplaceOwnerNode);

        assertAttribute(marketplaceOwnerNode,
                BillingShareResultXmlTags.ATTRIBUTE_NAME_AMOUNT, expectedAmount);
    }

}
