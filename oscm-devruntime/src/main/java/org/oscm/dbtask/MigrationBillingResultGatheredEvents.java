/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: kulle                                                       
 *                                                                              
 *  Creation Date: 17.08.2011                                                      
 *                                                                              
 *  Completion Time: 17.08.2011                                              
 *                                                                              
 *******************************************************************************/

package org.oscm.dbtask;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
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

import org.oscm.converter.PriceConverter;
import org.oscm.converter.XMLConverter;
import org.oscm.types.constants.BillingResultXMLTags;

/**
 * Migrates the billing result xmls within the database table 'billingresult'.
 * 
 * @author kulle
 */
public class MigrationBillingResultGatheredEvents extends DatabaseUpgradeTask {

    private static final String XPATH_GATHEREDEVENTS = "//GatheredEvents";

    @Override
    public void execute() throws Exception {
        Locale backup = Locale.getDefault();
        Locale.setDefault(Locale.ENGLISH);
        try {
            ResultSet billingResults = getRecordsByTable(TABLE_BILLINGRESULT);
            if (billingResults != null) {
                while (billingResults.next()) {
                    String billingXml = billingResults
                            .getString(COLUMN_RESULTXML);
                    if (billingXml != null && billingXml.length() > 0) {
                        String migratedXml = migrateBillingResultXml(billingXml);
                        updateBillingResultTable(
                                billingResults.getString(COLUMN_TKEY),
                                migratedXml);
                    }
                }
            }
        } finally {
            Locale.setDefault(backup);
        }
    }

    protected String migrateBillingResultXml(String billingXml)
            throws ParserConfigurationException, SAXException, IOException,
            TransformerException, XPathExpressionException {
        Document document = XMLConverter.convertToDocument(billingXml, false);
        NodeList nodeList = XMLConverter.getNodeListByXPath(document,
                XPATH_GATHEREDEVENTS);
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node gatheredEvents = nodeList.item(i);
            if (!istotalEventCostsAlreadyPresent(gatheredEvents)
                    && gatheredEvents.getChildNodes().getLength() > 0) {
                Element totalCosts = createNodeTotalEventCosts(gatheredEvents);
                gatheredEvents.appendChild(totalCosts);
            }
        }
        return XMLConverter.convertToString(document, false);
    }

    private boolean istotalEventCostsAlreadyPresent(Node gatheredEvents) {
        return XMLConverter.getLastChildNode(gatheredEvents,
                BillingResultXMLTags.GATHERED_EVENTS_COSTS_NODE_NAME) != null;
    }

    private Element createNodeTotalEventCosts(Node gatheredEvents) {
        PriceConverter priceFormatConverter = new PriceConverter();
        BigDecimal totalCosts = BigDecimal.ZERO;
        NodeList childNodes = gatheredEvents.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            if ("Event".equals(childNodes.item(i).getNodeName())) {
                Node costNode = XMLConverter.getLastChildNode(childNodes.item(i),
                        BillingResultXMLTags.COST_FOR_EVENT_TYPE_NODE_NAME);
                BigDecimal amount = XMLConverter.getBigDecimalAttValue(
                        costNode, BillingResultXMLTags.AMOUNT_ATTRIBUTE_NAME);
                totalCosts = totalCosts.add(amount);
            }
        }

        Element gatheredEventsCostElement = XMLConverter.newElement(
                BillingResultXMLTags.GATHERED_EVENTS_COSTS_NODE_NAME,
                (Element) gatheredEvents);

        // round total costs before setting the attribute value
        totalCosts = totalCosts.setScale(
                PriceConverter.NORMALIZED_PRICE_SCALING, RoundingMode.HALF_UP);
        gatheredEventsCostElement.setAttribute(
                BillingResultXMLTags.AMOUNT_ATTRIBUTE_NAME,
                priceFormatConverter.getValueToDisplay(totalCosts, false));

        return gatheredEventsCostElement;
    }

}
