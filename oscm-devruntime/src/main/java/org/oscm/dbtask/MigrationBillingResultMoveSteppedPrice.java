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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.oscm.converter.XMLConverter;
import org.oscm.types.constants.BillingResultXMLTags;

/**
 * Moves the stepped price from a sibling of period fee to a child of period
 * fee.
 * 
 * @author cheld
 * 
 */
public class MigrationBillingResultMoveSteppedPrice extends DatabaseUpgradeTask {

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
        NodeList periodFees = findPeriodFee(document);
        for (int i = 0; i < periodFees.getLength(); i++) {
            Node periodFee = periodFees.item(i);
            Node steppedPrice = findSiblingSteppedPrice(periodFee);
            if (steppedPrice != null) {
                move(periodFee, steppedPrice);
            }
        }
        return XMLConverter.convertToString(document, false);
    }

    private void move(Node periodFee, Node steppedPrice) {
        periodFee.appendChild(steppedPrice);
    }

    private NodeList findPeriodFee(Document document)
            throws XPathExpressionException {
        NodeList periodFees = XMLConverter.getNodeListByXPath(document, "//"
                + BillingResultXMLTags.PERIOD_FEE_NODE_NAME);
        return periodFees;
    }

    private Node findSiblingSteppedPrice(Node periodFee) {
        Node sibling = periodFee;
        while ((sibling = sibling.getNextSibling()) != null) {
            if (sibling.getNodeName().equals(
                    BillingResultXMLTags.STEPPED_PRICES_NODE_NAME)) {
                return sibling;
            }
        }
        return null;
    }

}
