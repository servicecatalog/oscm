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
import org.oscm.types.constants.BillingResultXMLTags;

/**
 * Migrates the billing result xmls within the database table
 * 'billingsharesresult'.
 * 
 * @author stavreva
 */
public class MigrationBillingSharesResultSubscriptionPeriod extends
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
                    if (billingXml != null && billingXml.length() > 0) {
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
        Node billingPeriod = XMLConverter.getNodeByXPath(document, "//"
                + BillingResultXMLTags.PERIOD_NODE_NAME);

        NodeList nodeList = XMLConverter.getNodeListByXPath(document, "//"
                + BillingResultXMLTags.SUBSCRIPTION_NODE_NAME);

        for (int i = 0; i < nodeList.getLength(); i++) {
            Node subscription = nodeList.item(i);
            if (!isSubscriptionPeriodAlreadyPresent(subscription)) {
                Element subscriptionPeriod = createNodeSubscriptionPeriod(
                        billingPeriod, subscription);
                subscription.appendChild(subscriptionPeriod);
            }
        }

        // append xml header
        final StringBuilder buffer = new StringBuilder();
        buffer.append(XMLConverter.HEADER);
        buffer.append(String.format("%s%n",
                XMLConverter.convertToString(document, false)));
        return buffer.toString();
    }

    private boolean isSubscriptionPeriodAlreadyPresent(Node subscription) {
        return XMLConverter.getLastChildNode(subscription,
                BillingResultXMLTags.PERIOD_NODE_NAME) != null;
    }

    private Element createNodeSubscriptionPeriod(Node billingPeriod,
            Node subscription) {

        Element subscriptionPeriodElement = XMLConverter.newElement(
                BillingResultXMLTags.PERIOD_NODE_NAME, (Element) subscription);

        subscriptionPeriodElement.setAttribute(
                BillingResultXMLTags.END_DATE_ISO_ATTRIBUTE_NAME,
                XMLConverter.getStringAttValue(billingPeriod,
                        BillingResultXMLTags.END_DATE_ISO_ATTRIBUTE_NAME));

        subscriptionPeriodElement.setAttribute(
                BillingResultXMLTags.END_DATE_ATTRIBUTE_NAME, XMLConverter
                        .getStringAttValue(billingPeriod,
                                BillingResultXMLTags.END_DATE_ATTRIBUTE_NAME));

        subscriptionPeriodElement.setAttribute(
                BillingResultXMLTags.START_DATE_ISO_ATTRIBUTE_NAME,
                XMLConverter.getStringAttValue(billingPeriod,
                        BillingResultXMLTags.START_DATE_ISO_ATTRIBUTE_NAME));

        subscriptionPeriodElement
                .setAttribute(BillingResultXMLTags.START_DATE_ATTRIBUTE_NAME,
                        XMLConverter.getStringAttValue(billingPeriod,
                                BillingResultXMLTags.START_DATE_ATTRIBUTE_NAME));

        return subscriptionPeriodElement;
    }
}
