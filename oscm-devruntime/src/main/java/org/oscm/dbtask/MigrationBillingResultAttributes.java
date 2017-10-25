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
import java.sql.SQLException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import org.oscm.converter.XMLConverter;
import org.oscm.types.constants.BillingResultXMLTags;

/**
 * Migrations for the BillingResult table, used for RQ 'FTS Partner-Model -
 * Billing' (upd_postgresql_02_02_09.sql)
 * 
 * @author Enes Sejfi
 */
public class MigrationBillingResultAttributes extends DatabaseUpgradeTask {

    @Override
    public void execute() throws Exception {
        ResultSet billingResults = getRecordsByTable(TABLE_BILLINGRESULT);
        if (billingResults != null) {
            String defaultCurrency = getAnyCurrency();
            while (billingResults.next()) {
                Long tkey = Long.valueOf(billingResults
                        .getLong(DatabaseUpgradeTask.COLUMN_TKEY));
                String resultXml = billingResults
                        .getString(DatabaseUpgradeTask.COLUMN_RESULTXML);
                BigDecimal grossAmount = BigDecimal.ZERO;
                String currencyCode = defaultCurrency;
                Document document = getDocument(resultXml);
                if (document != null) {
                    updateSubscriptionKey(document, billingResults, tkey);
                    currencyCode = getCurrencyCode(document);
                    grossAmount = getGrossAmount(document);
                }
                updateBillingResult(tkey, currencyCode, grossAmount);
            }
        }
    }

    void updateSubscriptionKey(final Document document,
            final ResultSet billingResults, final Long tkey)
            throws SQLException, Exception {
        if (Long.valueOf(billingResults.getLong("subscriptionkey")) == null
                || Long.valueOf(billingResults.getLong("subscriptionkey"))
                        .longValue() == 0) {
            String subscriptionId = getSubscription(document);
            Long pmKey = getPriceModel(document, subscriptionId);
            Long organizationObjKey = Long.valueOf(billingResults
                    .getLong("organizationtkey"));
            if (subscriptionId != null && organizationObjKey != null
                    && pmKey != null) {
                updateSubscriptionKey(tkey.longValue(), subscriptionId,
                        organizationObjKey.longValue(), pmKey.longValue());
            } else {
                updateSubscriptionKeyWithBillingResultKey(tkey.longValue());
            }
        }
    }

    private void updateSubscriptionKey(long tkey, String subscriptionId,
            long organizationobjkey, long pmKey) throws Exception {
        StringBuffer sb = new StringBuffer();
        sb.append("UPDATE billingresult SET subscriptionkey=");
        sb.append("(SELECT DISTINCT objkey FROM subscriptionhistory sh WHERE ");
        sb.append("sh.subscriptionid=? AND organizationobjkey=?");
        sb.append(" AND (select distinct pricemodelobjkey from producthistory prdh where prdh.objkey=sh.productobjkey and ?=prdh.pricemodelobjkey) is not null) ");
        sb.append("WHERE tkey=?");

        PreparedStatement pStatement = null;
        try {
            pStatement = getPreparedStatement(sb.toString());
            pStatement.setString(1, subscriptionId);
            pStatement.setLong(2, organizationobjkey);
            pStatement.setLong(3, pmKey);
            pStatement.setLong(4, tkey);
            pStatement.executeUpdate();
        } finally {
            closeStatement(pStatement, null);
        }
    }

    private void updateSubscriptionKeyWithBillingResultKey(long tkey)
            throws Exception {
        StringBuffer sb = new StringBuffer();
        sb.append("UPDATE billingresult SET subscriptionkey=? WHERE tkey=?");
        PreparedStatement pStatement = null;
        try {
            pStatement = getPreparedStatement(sb.toString());
            pStatement.setLong(1, tkey);
            pStatement.setLong(2, tkey);
            pStatement.executeUpdate();
        } finally {
            closeStatement(pStatement, null);
        }
    }

    String getAnyCurrency() throws Exception {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = getPreparedStatement("SELECT currencyisocode FROM supportedcurrency limit 1;");
            rs = ps.executeQuery();
            String code = null;
            if (rs.next()) {
                code = rs.getString("currencyisocode");
            }
            return code;
        } finally {
            closeStatement(ps, rs);
        }
    }

    /**
     * Returns the billing result XML which the price values are converted to
     * containing the decimal places.
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
     * Returns the total costs of all price models. The XML may contain multiple
     * price models with multiple gross amounts in different currencies.
     * Currently, only the first is returned. sense for test code.
     * 
     * @return gross amount of the billing result.
     */
    public BigDecimal getGrossAmount(Document document) throws Exception {
        String grossAmount = XMLConverter.getNodeTextContentByXPath(document,
                "/BillingDetails/OverallCosts/@"
                        + BillingResultXMLTags.GROSS_AMOUNT_ATTRIBUTE_NAME);
        BigDecimal result = null;
        if (grossAmount != null) {
            result = new BigDecimal(grossAmount);
        } else {
            String netAmount = XMLConverter.getNodeTextContentByXPath(document,
                    "/BillingDetails/OverallCosts/@"
                            + BillingResultXMLTags.NET_AMOUNT_ATTRIBUTE_NAME);
            if (netAmount != null) {
                result = new BigDecimal(netAmount);
            } else {
                result = BigDecimal.ZERO;
            }
        }
        return result;
    }

    String getSubscription(Document document) throws Exception {
        String result = null;
        Node node = XMLConverter.getNodeByXPath(document,
                "//Subscription[1]/@id");
        if (node != null) {
            result = node.getTextContent();
        }
        return result;
    }

    Long getPriceModel(Document document, String subscriptionId)
            throws Exception {
        Long result = null;
        if (subscriptionId != null) {
            Node node = XMLConverter.getNodeByXPath(document,
                    "//Subscription[@id='" + subscriptionId
                            + "']/PriceModels/PriceModel[1]/@id");
            if (node != null) {
                result = Long.valueOf(node.getTextContent());
            }
        }
        return result;
    }

    /**
     * Returns the currency of the billing result in String representation
     * according to ISO code 4217 by parsing the result XML. The XML may contain
     * multiple price models with different currencies. The first found currency
     * is returned.
     * 
     * @return The currency ISO code. <code>null</code> in case the result XML
     *         is not initialized or does not contain the currency information.
     */
    String getCurrencyCode(Document document) throws Exception {
        String result = null;
        Node currencyAttribue = XMLConverter.getNodeByXPath(document,
                "/BillingDetails/OverallCosts/@currency");
        if (currencyAttribue != null) {
            result = currencyAttribue.getTextContent();
        }
        return result;
    }

    /**
     * Updates the billing result data with currency code and gross amount.
     * 
     * @param billingResultXml
     */
    void updateBillingResult(Long tkey, String currencyCode,
            BigDecimal grossAmount) throws Exception {
        if (updateBillingResultByTkey(tkey, currencyCode, grossAmount) < 1) {
            String message = "The update of billingresult.currencyCode and billingResult.grossAmount with tkey '%s' failed.";
            throw new Exception(String.format(message, tkey));
        }
    }

    /**
     * Updates the billing result data with currency code and gross amount.
     * 
     * @param billingResultXml
     */
    int updateBillingResultByTkey(Long tkey, String currencyCode,
            BigDecimal valueGrossAmount) throws Exception {
        long currencyKey = getCurrencyKey(currencyCode);

        String sql = String.format(
                "UPDATE %s SET currency_tkey=?, grossamount=? WHERE tkey=?;",
                TABLE_BILLINGRESULT);
        PreparedStatement pStatement = null;
        try {
            pStatement = getPreparedStatement(sql);
            pStatement.setLong(1, currencyKey);
            pStatement.setBigDecimal(2, valueGrossAmount);
            pStatement.setLong(3, tkey.longValue());
            int result = pStatement.executeUpdate();
            return result;
        } finally {
            closeStatement(pStatement, null);
        }
    }

    private long getCurrencyKey(String currencyCode) throws Exception {
        String sql = String
                .format("SELECT tkey FROM supportedcurrency WHERE currencyisocode=?;");
        PreparedStatement pStatement = null;
        ResultSet rs = null;
        try {
            pStatement = getPreparedStatement(sql);
            pStatement.setString(1, currencyCode);
            rs = pStatement.executeQuery();
            rs.next();
            long result = rs.getLong("tkey");
            return result;
        } finally {
            closeStatement(pStatement, rs);
        }
    }

    private void closeStatement(java.sql.Statement statement,
            ResultSet resultSet) throws SQLException {
        if (resultSet != null) {
            resultSet.close();
        }
        if (statement != null) {
            statement.close();
        }
    }
}
