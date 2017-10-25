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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.oscm.converter.XMLConverter;
import org.oscm.internal.types.enumtypes.ConfigurationKey;

/**
 * Migrates the billing result xml in the billingresult table for any decimal
 * places support. This task is used in upd_postgresql_02_00_35.sql.
 * 
 * @author tokoda
 * 
 */
public class MigrationBillingResultDecimalPlace extends DatabaseUpgradeTask {

    private static final int UPPER_LIMIT_DECIMAL_PLACES = 6;
    private static final int LOWER_LIMIT_DECIMAL_PLACES = 2;

    private static final String[] targetAttributes = { "amount", "baseAmount",
            "grossAmount", "netAmount", "discountNetAmount", "price",
            "basePrice", "total", "netAmountAfterDiscount",
            "netAmountBeforeDiscount" };

    @Override
    public void execute() throws Exception {

        int decimalPlacesSetting = readDecimalPlacesSetting();

        ResultSet billingResults = getRecordsByTable(TABLE_BILLINGRESULT);
        if (billingResults != null) {
            while (billingResults.next()) {
                String tkey = billingResults.getString("tkey");
                String billingResultXml = billingResults.getString("resultxml");
                if (billingResultXml != null && billingResultXml.length() > 0) {
                    billingResultXml = processXml(billingResultXml,
                            decimalPlacesSetting);
                    updateBillingResult(tkey, billingResultXml);
                }
            }
        }

    }

    /**
     * Returns the integer value of the configuration setting for decimal
     * places. The fallback value is returned in case no valid value is found.
     * 
     * @return The value of the configuration setting or the fallback.
     */
    private int readDecimalPlacesSetting() throws Exception {
        String key = ConfigurationKey.DECIMAL_PLACES.getKeyName();
        String decimalConfigSetting = getConfigSettingValue(key);
        int decimalSettingIntValue = getDecimalPlacesSettingValue(decimalConfigSetting);
        return decimalSettingIntValue;
    }

    private int getDecimalPlacesSettingValue(String decimalPlacesSetting) {
        int result = Integer.parseInt(ConfigurationKey.DECIMAL_PLACES
                .getFallBackValue());
        if (decimalPlacesSetting == null) {
            // use default value
        } else {
            try {
                int settingValue = Integer.parseInt(decimalPlacesSetting);
                if (settingValue < LOWER_LIMIT_DECIMAL_PLACES
                        || settingValue > UPPER_LIMIT_DECIMAL_PLACES) {
                    // use default value
                } else {
                    result = settingValue;
                }
            } catch (NumberFormatException e) {
                // use default value
            }
        }
        return result;
    }

    /**
     * Returns the billing result xml which the price values are converted to
     * containing the decimal places.
     * 
     * @param sourceXml
     *            the billing result xml which not contain the the decimal place
     *            consideration.
     * @param decimalPlacesSetting
     *            the value of the configuration setting for decimal places.
     * @return the modified billing result xml which contain the the decimal
     *         place consideration.
     * @throws Exception
     */
    private String processXml(String sourceXml, int decimalPlacesSetting)
            throws Exception {
        String resultXml = sourceXml;
        if (resultXml != null && resultXml.length() > 0
                && decimalPlacesSetting > 0) {
            Document doc = XMLConverter.convertToDocument(sourceXml, false);
            if (doc == null) {
                return resultXml;
            }
            updatePrices(doc, decimalPlacesSetting);
            resultXml = XMLConverter.convertToString(doc, false);
        }
        return resultXml;
    }

    private void updatePrices(Document doc, int decimalPlacesSetting)
            throws Exception {
        parseNodes(doc.getFirstChild(), decimalPlacesSetting);
    }

    private void parseNodes(Node node, int decimalPlacesSetting) {
        if (node.hasChildNodes()) {
            NodeList childs = node.getChildNodes();
            for (int i = 0; i < childs.getLength(); i++) {
                parseNodes(childs.item(i), decimalPlacesSetting);
            }
        }
        for (String targetAttribute : targetAttributes) {
            updateAttributeValue(node, targetAttribute, decimalPlacesSetting);
        }
    }

    private void updateAttributeValue(Node node, String attName,
            int decimalPlacesSetting) {
        if (node.hasAttributes()
                && !"NumberOfOccurrence".equals(node.getNodeName())
                && !"ParameterValue".equals(node.getNodeName())) {
            Node attribute = node.getAttributes().getNamedItem(attName);
            if (attribute != null) {
                String priceValue = attribute.getNodeValue();
                if (priceValue != null && priceValue.length() > 0) {
                    attribute.setNodeValue(getConvertedPrice(priceValue,
                            decimalPlacesSetting));
                }
            }
        }
    }

    private String getConvertedPrice(String input, int decimalPlacesSetting) {
        String convertedPrice = input;
        if (convertedPrice != null && convertedPrice.length() > 0) {
            BigDecimal result = BigDecimal.valueOf(Long.parseLong(input),
                    decimalPlacesSetting);
            convertedPrice = result.toPlainString();
        }
        return convertedPrice;
    }

    /**
     * Updates the billing result data by the converted xml.
     * 
     * @param billingResultXml
     */
    private void updateBillingResult(String tkey, String billingResultXml)
            throws Exception {
        if (updateBillingResultByTkey(TABLE_BILLINGRESULT, tkey, "resultxml",
                billingResultXml) < 1) {
            String message = "The update billingresult with tkey '%s' failed.";
            throw new Exception(String.format(message, tkey));
        }
    }

    private int updateBillingResultByTkey(String tableName, String tkey,
            String columnName, String newValue) throws Exception {
        String sql = String.format("UPDATE %s SET %s=? WHERE tkey=?;",
                tableName, columnName);
        PreparedStatement pStatement = getPreparedStatement(sql);
        pStatement.setString(1, newValue);
        pStatement.setLong(2, Long.parseLong(tkey));
        int result = pStatement.executeUpdate();
        pStatement.close();
        pStatement = null;
        return result;
    }

}
