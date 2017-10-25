/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: tokoda                                                     
 *                                                                              
 *  Creation Date: Jul 21, 2011                                                      
 *                                                                              
 *  Completion Time: Jul 21, 2011                                       
 *                                                                              
 *******************************************************************************/

package org.oscm.dbtask;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.w3c.dom.Document;

import org.oscm.converter.XMLConverter;
import org.oscm.types.constants.BillingResultXMLTags;

/**
 * Migrations for the BillingResult table, used for RQ 'FTS Partner-Model -
 * Billing' (upd_postgresql_02_05_00.sql)
 * 
 * @author Enes Sejfi
 */
public class MigrationBillingResultNetAmount extends DatabaseUpgradeTask {

    @Override
    public void execute() throws Exception {
        ResultSet billingResults = getRecordsByTable(TABLE_BILLINGRESULT);
        if (billingResults != null) {
            while (billingResults.next()) {
                Long tkey = Long.valueOf(billingResults
                        .getLong(DatabaseUpgradeTask.COLUMN_TKEY));
                String resultXml = billingResults
                        .getString(DatabaseUpgradeTask.COLUMN_RESULTXML);
                BigDecimal netAmount = BigDecimal.ZERO;
                Document document = getDocument(resultXml);
                if (document != null) {
                    netAmount = getNetAmount(document);
                }
                updateBillingResult(tkey, netAmount);
            }
            billingResults.close();
        }
    }

    /**
     * Returns the billing result XML.
     * 
     * @param resultXml
     *            the billing result XML
     * @return XML document
     */
    private Document getDocument(String resultXml) throws Exception {
        if (resultXml != null && resultXml.trim().length() > 0) {
            return XMLConverter.convertToDocument(resultXml, false);
        }
        return null;
    }

    /**
     * Returns the net amount of the billing result.
     * 
     * @return net amount of the billing result.
     */
    public BigDecimal getNetAmount(Document document) throws Exception {
        String netAmount = XMLConverter.getNodeTextContentByXPath(document,
                "/BillingDetails/OverallCosts/@"
                        + BillingResultXMLTags.NET_AMOUNT_ATTRIBUTE_NAME);
        if (netAmount != null) {
            return new BigDecimal(netAmount);
        } else {
            return BigDecimal.ZERO;
        }
    }

    /**
     * Updates the billing result data with net amount.
     * 
     * @param tkey
     *            billing result key
     * @param netAmount
     *            billing result net amount
     */
    void updateBillingResult(Long tkey, BigDecimal netAmount) throws Exception {
        if (updateBillingResultByTkey(tkey, netAmount) < 1) {
            String message = "The update of billingResult.netAmount with tkey '%s' failed.";
            throw new Exception(String.format(message, tkey));
        }
    }

    /**
     * Updates the billing result data with net amount.
     * 
     * @param tkey
     *            billing result key
     * @param netAmount
     *            billing result net amount
     */
    int updateBillingResultByTkey(Long tkey, BigDecimal netAmount)
            throws Exception {
        String sql = String.format("UPDATE %s SET netamount=? WHERE tkey=?;",
                TABLE_BILLINGRESULT);
        PreparedStatement stmt = getPreparedStatement(sql);
        stmt.setBigDecimal(1, netAmount);
        stmt.setLong(2, tkey.longValue());
        return stmt.executeUpdate();
    }
}
