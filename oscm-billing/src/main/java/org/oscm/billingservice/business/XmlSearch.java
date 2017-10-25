/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                                                                                 
 *  Creation Date: 10.09.2013                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.billingservice.business;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import org.oscm.converter.XMLConverter;
import org.oscm.domobjects.BillingResult;
import org.oscm.types.exceptions.BillingRunFailed;

/**
 * Wrapper for the XMLConverter
 * 
 * @author kulle
 * 
 */
public class XmlSearch {

    private Document billingResult;

    public XmlSearch(BillingResult billingResult) {
        try {
            this.billingResult = XMLConverter.convertToDocument(
                    billingResult.getResultXML(), true);
        } catch (IOException | ParserConfigurationException | SAXException e) {
            throw new BillingRunFailed(e);
        }
    }

    public Set<Long> findPriceModelKeys() {
        try {
            Set<Long> result = new HashSet<Long>();
            NodeList nodeList = XMLConverter.getNodeListByXPath(billingResult,
                    "//PriceModel/@id");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node n = nodeList.item(i);
                String idValue = n.getTextContent();
                result.add(Long.valueOf(idValue));
            }
            return result;
        } catch (XPathExpressionException e) {
            throw new BillingRunFailed(e);
        }
    }

    public List<BigDecimal> retrieveNetAmounts(Long pmKey) {
        try {
            List<BigDecimal> result = new ArrayList<BigDecimal>();
            String xpath = String.format(
                    "//PriceModel[@id='%d']/PriceModelCosts/@amount", pmKey);
            NodeList nodeList = XMLConverter.getNodeListByXPath(billingResult,
                    xpath);

            for (int i = 0; i < nodeList.getLength(); i++) {
                Node n = nodeList.item(i);
                String amountValue = n.getTextContent();
                result.add(new BigDecimal(amountValue));
            }
            return result;
        } catch (XPathExpressionException e) {
            throw new BillingRunFailed(e);
        }

    }

    public BigDecimal retrieveDiscountPercent() {

        try {
            String xpath = "//BillingDetails/OverallCosts/Discount/@percent";

            String value = XMLConverter.getNodeTextContentByXPath(
                    billingResult, xpath);
            if (value != null) {
                return new BigDecimal(value);
            }
            return null;
        } catch (XPathExpressionException e) {
            throw new BillingRunFailed(e);
        }
    }

}
