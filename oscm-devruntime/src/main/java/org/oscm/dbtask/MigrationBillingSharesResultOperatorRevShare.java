/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: stavreva                                                       
 *                                                                              
 *  Creation Date: 07.01.2013                                                      
 *                                                                              
 *  Completion Time: 08.01.2013                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.dbtask;

import java.io.IOException;
import java.sql.ResultSet;
import java.util.Locale;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import org.oscm.converter.XMLConverter;
import org.oscm.types.constants.BillingShareResultXmlTags;
import org.oscm.internal.types.enumtypes.BillingSharesResultType;

/**
 * Migrates the billing result xmls within the database table
 * 'billingsharesresult'.
 * 
 * @author baumann
 */
public class MigrationBillingSharesResultOperatorRevShare extends
        DatabaseUpgradeTask {

    @Override
    public void execute() throws Exception {
        Locale backup = Locale.getDefault();
        Locale.setDefault(Locale.ENGLISH);
        try {
            ResultSet billingSharesResults = getRecordsByTable(TABLE_BILLINGSHARESRESULT);
            if (billingSharesResults != null) {
                while (billingSharesResults.next()) {
                    String billingXml = billingSharesResults
                            .getString(COLUMN_RESULTXML);
                    String resulttype = billingSharesResults
                            .getString(COLUMN_RESULTTYPE);
                    if (BillingSharesResultType.SUPPLIER.name().equals(
                            resulttype)
                            && billingXml != null && billingXml.length() > 0) {
                        String migratedXml = migrateBillingSharesResultXml(billingXml);
                        updateBillingSharesResultTable(
                                billingSharesResults.getString(COLUMN_TKEY),
                                migratedXml);
                    }
                }
            }
        } finally {
            Locale.setDefault(backup);
        }
    }

    protected String migrateBillingSharesResultXml(String billingXml)
            throws ParserConfigurationException, SAXException, IOException,
            TransformerException, XPathExpressionException {
        Document document = XMLConverter.convertToDocument(billingXml, false);

        migrateRevenuePerMarketplace(document);
        migrateRevenueShareDetails(document);
        migrateSupplierRevenue(document);

        return XMLConverter.convertToString(document, false);
    }

    private void migrateRevenuePerMarketplace(Document document)
            throws XPathExpressionException {
        NodeList nodeList = XMLConverter.getNodeListByXPath(document, "//"
                + BillingShareResultXmlTags.NODE_NAME_REVENUE_PER_MARKETPLACE);
        for (int i = 0; i < nodeList.getLength(); i++) {
            Element revPerMp = (Element) nodeList.item(i);
            migrateOperatorRevenueAttribute(revPerMp);
        }
    }

    private void migrateRevenueShareDetails(Document document)
            throws XPathExpressionException {
        NodeList nodeList = XMLConverter.getNodeListByXPath(document, "//"
                + BillingShareResultXmlTags.NODE_NAME_REVENUE_SHARE_DETAILS);
        for (int i = 0; i < nodeList.getLength(); i++) {
            Element revShareDetails = (Element) nodeList.item(i);
            migrateOperatorRevenueAttribute(revShareDetails);
            migrateOpRevSharePercentageAttribute(revShareDetails);
            migrateCustomerRevenueShareDetails(revShareDetails.getChildNodes());
        }
    }

    private void migrateCustomerRevenueShareDetails(
            NodeList revShareDetailsChilds) {
        for (int index = 0; index < revShareDetailsChilds.getLength(); index++) {
            Node childNode = revShareDetailsChilds.item(index);

            if (BillingShareResultXmlTags.NODE_NAME_CUSTOMER_REVENUE_SHARE_DETAILS
                    .equals(childNode.getNodeName())) {
                Element custRevShareDetails = (Element) childNode;
                migrateOperatorRevenueAttribute(custRevShareDetails);
                removePercentageAttributes(custRevShareDetails);
            }
        }
    }

    private void migrateSupplierRevenue(Document document)
            throws XPathExpressionException {
        NodeList supplRevenueNodes = XMLConverter.getNodeListByXPath(document,
                "//" + BillingShareResultXmlTags.NODE_NAME_SUPPLIER_REVENUE);
        for (int i = 0; i < supplRevenueNodes.getLength(); i++) {
            NodeList supplRevenueChilds = supplRevenueNodes.item(i)
                    .getChildNodes();
            for (int index = 0; index < supplRevenueChilds.getLength(); index++) {
                Node childNode = supplRevenueChilds.item(index);

                if (BillingShareResultXmlTags.NODE_NAME_DIRECT_REVENUE
                        .equals(childNode.getNodeName())
                        || BillingShareResultXmlTags.NODE_NAME_BROKER_REVENUE
                                .equals(childNode.getNodeName())
                        || BillingShareResultXmlTags.NODE_NAME_RESELLER_REVENUE
                                .equals(childNode.getNodeName())

                ) {
                    migrateOperatorRevenueAttribute((Element) childNode);
                }
            }
        }
    }

    private void migrateOperatorRevenueAttribute(Element element) {
        if (!element
                .hasAttribute(BillingShareResultXmlTags.ATTRIBUTE_NAME_OPERATOR_REVENUE)) {
            element.setAttribute(
                    BillingShareResultXmlTags.ATTRIBUTE_NAME_OPERATOR_REVENUE,
                    "0.00");
        }
    }

    private void migrateOpRevSharePercentageAttribute(Element element) {
        if (!element
                .hasAttribute(BillingShareResultXmlTags.ATTRIBUTE_NAME_OPERATOR_REVENUE_SHARE_PERCENTAGE)) {
            element.setAttribute(
                    BillingShareResultXmlTags.ATTRIBUTE_NAME_OPERATOR_REVENUE_SHARE_PERCENTAGE,
                    "0.00");
        }
    }

    private void removePercentageAttributes(Element element) {
        element.removeAttribute(BillingShareResultXmlTags.ATTRIBUTE_NAME_BROKER_REVENUE_SHARE_PERCENTAGE);
        element.removeAttribute(BillingShareResultXmlTags.ATTRIBUTE_NAME_RESELLER_REVENUE_SHARE_PERCENTAGE);
        element.removeAttribute(BillingShareResultXmlTags.ATTRIBUTE_NAME_MARKETPLACE_REVENUE_SHARE_PERCENTAGE);
    }

}
