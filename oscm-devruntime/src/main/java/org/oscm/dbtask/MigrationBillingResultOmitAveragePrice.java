/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: cheld                                                    
 *                                                                              
 *  Creation Date: 20.09.2011                                                      
 *                                                                              
 *  Completion Time: 20.09.2011                                               
 *                                                                              
 *******************************************************************************/

package org.oscm.dbtask;

import java.sql.ResultSet;
import java.util.Locale;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.oscm.converter.XMLConverter;
import org.oscm.types.constants.BillingResultXMLTags;

/**
 * Removes the average base price from the billing result in case the price is
 * defined by 'stepped prices'. The attribute did not make much sense and was
 * not used.
 * 
 * @author cheld
 * 
 */
public class MigrationBillingResultOmitAveragePrice extends DatabaseUpgradeTask {

    /*
     * (non-Javadoc)
     * 
     * @see org.oscm.dbtask.DatabaseUpgradeTask#execute()
     */
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
            throws Exception {
        Document document = XMLConverter.convertToDocument(billingXml, false);
        NodeList parentsOfSteppedPrices = findParentsOfSteppedPrices(document);
        for (int i = 0; i < parentsOfSteppedPrices.getLength(); i++) {
            Node parent = parentsOfSteppedPrices.item(i);
            if (isBasePriceAttributeAlreadyPresent(parent)) {
                removeBasePriceAttribute(parent);
            }
        }
        return XMLConverter.convertToString(document, false);
    }

    private NodeList findParentsOfSteppedPrices(Document document)
            throws XPathExpressionException {
        NodeList parentsOfSteppedPrices = XMLConverter.getNodeListByXPath(
                document, "//" + BillingResultXMLTags.STEPPED_PRICES_NODE_NAME
                        + "/..");
        return parentsOfSteppedPrices;
    }

    private boolean isBasePriceAttributeAlreadyPresent(Node node) {
        NamedNodeMap attrMap = node.getAttributes();
        if (attrMap != null) {
            return attrMap
                    .getNamedItem(BillingResultXMLTags.BASE_PRICE_ATTRIBUTE_NAME) != null;
        }
        return false;
    }

    private void removeBasePriceAttribute(Node node) {
        node.getAttributes().removeNamedItem(
                BillingResultXMLTags.BASE_PRICE_ATTRIBUTE_NAME);
    }
}
