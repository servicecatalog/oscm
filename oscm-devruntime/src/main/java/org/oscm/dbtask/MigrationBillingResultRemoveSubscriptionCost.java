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

import java.sql.ResultSet;
import java.util.Locale;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.oscm.converter.XMLConverter;

/**
 * Removes the tag subscriptioncosts
 * 
 * @author held
 */
public class MigrationBillingResultRemoveSubscriptionCost extends
        DatabaseUpgradeTask {
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
        NodeList subscriptionCosts = findSubscriptionCost(document);
        for (int i = 0; i < subscriptionCosts.getLength(); i++) {
            Node subscriptionCost = subscriptionCosts.item(i);
            Node parent = subscriptionCost.getParentNode();
            parent.removeChild(subscriptionCost);
        }
        return XMLConverter.convertToString(document, false);
    }

    private NodeList findSubscriptionCost(Document document)
            throws XPathExpressionException {
        NodeList periodFees = XMLConverter.getNodeListByXPath(document,
                "//SubscriptionCosts");
        return periodFees;
    }

}
