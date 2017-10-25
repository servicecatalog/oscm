/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: kulle                                       
 *                                                                              
 *  Creation Date: 18.08.2011                                                      
 *                                                                              
 *  Completion Time: 18.08.2011                                     
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
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import org.oscm.converter.PriceConverter;
import org.oscm.converter.XMLConverter;
import org.oscm.types.constants.BillingResultXMLTags;

/**
 * @author kulle
 */
public class MigrationBillingResultSteppedPrices extends DatabaseUpgradeTask {

    private static final String XPATH_STEPPEDPRICES = "//"
            + BillingResultXMLTags.STEPPED_PRICES_NODE_NAME;
    private static final String NULL_STRING = "null";

    private class StepData {
        private long value;
        private BigDecimal additionalPrice = BigDecimal.ZERO;
        private BigDecimal basePrice = BigDecimal.ZERO;
        private long freeAmount;

        public long getValue() {
            return value;
        }

        public void setValue(long valueFactor) {
            this.value = valueFactor;
        }

        public BigDecimal getAdditionalPrice() {
            return additionalPrice;
        }

        public void setAdditionalPrice(BigDecimal additionalPrice) {
            this.additionalPrice = additionalPrice;
        }

        public BigDecimal getBasePrice() {
            return basePrice;
        }

        public void setBasePrice(BigDecimal basePrice) {
            this.basePrice = basePrice;
        }

        public long getFreeAmount() {
            return freeAmount;
        }

        public void setFreeAmount(long limit) {
            this.freeAmount = limit;
        }

    }

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
            XPathExpressionException, TransformerException {

        // create a document from the xml file
        Document document = XMLConverter.convertToDocument(billingXml, false);

        // iterate over all stepped prices elements, if any
        NodeList steppedPrices = XMLConverter.getNodeListByXPath(document,
                XPATH_STEPPEDPRICES);
        for (int i = 0; i < steppedPrices.getLength(); i++) {
            Node steppedPricesElement = steppedPrices.item(i);

            // proceed only if amount attribute is not present
            if (!isAmountAttributeAlreadyPresent(steppedPricesElement)) {
                long value = getValue(steppedPricesElement);
                StepData relevantStep = getStepData(steppedPricesElement, value);
                BigDecimal price = calculateStepPrice(relevantStep);
                createAttribute(steppedPricesElement, price);
            }
        }

        return XMLConverter.convertToString(document, false);
    }

    private boolean isAmountAttributeAlreadyPresent(Node steppedPricesElement) {
        NamedNodeMap attrMap = steppedPricesElement.getAttributes();
        if (attrMap != null) {
            return attrMap
                    .getNamedItem(BillingResultXMLTags.AMOUNT_ATTRIBUTE_NAME) != null;
        }

        return false;
    }

    private long getValue(Node steppedPricesElement) {
        long result = 0;

        Node parent = steppedPricesElement.getParentNode();
        // stepped price of an event?
        if (BillingResultXMLTags.EVENT_NODE_NAME.equals(parent.getNodeName())) {
            Node occurencesNode = XMLConverter.getLastChildNode(parent,
                    BillingResultXMLTags.NUMBER_OF_OCCURRENCE_NODE_NAME);
            result = XMLConverter.getLongAttValue(occurencesNode,
                    BillingResultXMLTags.AMOUNT_ATTRIBUTE_NAME);

        }

        // stepped price of the price model?
        else if (BillingResultXMLTags.USER_ASSIGNMENT_COSTS_NODE_NAME
                .equals(parent.getNodeName())) {
            result = XMLConverter.getLongAttValue(parent,
                    BillingResultXMLTags.NUMBER_OF_USERS_TOTAL_ATTRIBUTE_NAME);
        }

        // stepped price of a parameter?
        else if (BillingResultXMLTags.PARAMETER_NODE_NAME.equals(parent
                .getNodeName())) {
            Node paramValueNode = XMLConverter.getLastChildNode(parent,
                    BillingResultXMLTags.PARAMETER_VALUE_NODE_NAME);
            result = XMLConverter.getLongAttValue(paramValueNode,
                    BillingResultXMLTags.AMOUNT_ATTRIBUTE_NAME);
        }

        return result;
    }

    private StepData getStepData(Node steppedPricesElement, long value) {
        StepData stepData = new StepData();
        stepData.setValue(value);
        long limit = -1;
        long tempLimit;

        // Find the matching step for the provided amount (number of users,
        // number of events or parameter value) – find the step with the
        // smallest limit that is greater than the amount.
        NodeList steppedPriceList = steppedPricesElement.getChildNodes();
        for (int i = 0; i < steppedPriceList.getLength(); i++) {
            Node steppedPrice = steppedPriceList.item(i);

            if (BillingResultXMLTags.STEPPED_PRICE_NODE_NAME
                    .equals(steppedPrice.getNodeName())) {

                if (NULL_STRING.equals(XMLConverter
                        .getStringAttValue(steppedPrice,
                                BillingResultXMLTags.LIMIT_ATTRIBUTE_NAME))) {
                    if (value > XMLConverter.getLongAttValue(steppedPrice,
                            BillingResultXMLTags.FREE_AMOUNT_ATTRIBUTE_NAME)) {
                        updateStepPriceData(stepData, steppedPrice);
                    }
                } else {
                    tempLimit = XMLConverter.getLongAttValue(steppedPrice,
                            BillingResultXMLTags.LIMIT_ATTRIBUTE_NAME);

                    if (value <= tempLimit) {
                        if (limit == -1) {
                            limit = tempLimit;
                        }
                        if (tempLimit <= limit) {
                            limit = tempLimit;
                            updateStepPriceData(stepData, steppedPrice);
                        }
                    }
                }
            }
        }

        return stepData;
    }

    private void updateStepPriceData(StepData stepData, Node steppedPrice) {
        stepData.setAdditionalPrice(XMLConverter.getBigDecimalAttValue(
                steppedPrice,
                BillingResultXMLTags.ADDITIONAL_PRICE_ATTRIBUTE_NAME));
        stepData.setBasePrice(XMLConverter.getBigDecimalAttValue(steppedPrice,
                BillingResultXMLTags.BASE_PRICE_ATTRIBUTE_NAME));
        stepData.setFreeAmount(XMLConverter.getLongAttValue(steppedPrice,
                BillingResultXMLTags.FREE_AMOUNT_ATTRIBUTE_NAME));
    }

    private BigDecimal calculateStepPrice(StepData step) {
        BigDecimal additionalPrice = step.getAdditionalPrice();
        BigDecimal basePrice = step.getBasePrice();
        BigDecimal value = BigDecimal.valueOf(step.getValue()).subtract(
                BigDecimal.valueOf(step.getFreeAmount()));
        return (value.multiply(basePrice)).add(additionalPrice);
    }

    private void createAttribute(Node steppedPricesElement, BigDecimal price) {
        PriceConverter priceFormatConverter = new PriceConverter();
        price = price.setScale(PriceConverter.NORMALIZED_PRICE_SCALING,
                RoundingMode.HALF_UP);
        ((Element) steppedPricesElement).setAttribute(
                BillingResultXMLTags.AMOUNT_ATTRIBUTE_NAME,
                priceFormatConverter.getValueToDisplay(price, false));
    }
}
