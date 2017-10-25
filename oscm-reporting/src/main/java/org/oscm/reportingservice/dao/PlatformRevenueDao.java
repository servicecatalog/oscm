/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.reportingservice.dao;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.w3c.dom.Document;

import org.oscm.converter.XMLConverter;
import org.oscm.dataservice.local.DataService;
import org.oscm.dataservice.local.DataSet;
import org.oscm.dataservice.local.SqlQuery;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.internal.types.exception.SaaSSystemException;

/**
 * Storage of the SQL query result for the platform revenue report.
 * 
 * @author cheld
 * 
 */
public class PlatformRevenueDao {

    public class RowData {
        final String supplierName;
        final String supplierID;
        final String supplierCountry;
        final String billingKey;
        final String currency;
        final String marketplace;
        final BigDecimal amount;

        public RowData(String supplierName, String supplierID,
                String supplierCountry, String billingResult, String marketplace) {
            this.supplierName = supplierName;
            this.supplierID = supplierID;
            this.supplierCountry = supplierCountry;
            this.marketplace = marketplace;
            try {
                final Document br = XMLConverter.convertToDocument(billingResult,
                        false);
                currency = XMLConverter.getNodeTextContentByXPath(br,
                        "/BillingDetails/OverallCosts/@currency");
                amount = new BigDecimal(XMLConverter.getNodeTextContentByXPath(
                        br, "/BillingDetails/OverallCosts/@netAmount"));
                billingKey = XMLConverter.getNodeTextContentByXPath(br,
                        "/BillingDetails/@key");
            } catch (Exception ex) {
                throw new SaaSSystemException(ex);
            }
        }

        public String getSupplierName() {
            return supplierName;
        }

        public String getSupplierID() {
            return supplierID;
        }

        public String getSupplierCountry() {
            return supplierCountry;
        }

        public String getBillingKey() {
            return billingKey;
        }

        public String getCurrency() {
            return currency;
        }

        public String getMarketplace() {
            return marketplace;
        }

        public BigDecimal getAmount() {
            return amount;
        }

    }

    static final String QUERY = "SELECT sup.name, sup.organizationid, c.countryisocode, "
            + "CASE WHEN sub.marketplaceobjkey IS NOT NULL THEN (SELECT DISTINCT marketplaceid FROM marketplacehistory WHERE objkey=sub.marketplaceobjkey) END AS marketplaceid, "
            + "CASE WHEN res.value IS NOT NULL THEN res.value END AS value, br.resultxml "
            + "FROM billingresult br, organizationhistory sup, supportedcountry c, subscriptionhistory sub LEFT JOIN localizedresource res ON res.objectkey = sub.marketplaceobjkey AND res.objecttype = ? AND res.locale = ? "
            + "WHERE br.chargingorgkey = sup.objkey AND sup.objversion = (SELECT MAX(supt.objversion) FROM organizationhistory supt WHERE sup.objkey=supt.objkey AND supt.moddate <= ?) AND sub.objkey = br.subscriptionkey AND sub.objversion = (SELECT MAX(subt.objversion) FROM subscriptionhistory subt WHERE sub.objkey=subt.objkey AND subt.moddate <= ?) AND c.tkey = sup.domicilecountryobjkey AND br.periodstarttime >= ? AND br.periodendtime <= ?";

    private final List<RowData> rowData = new ArrayList<RowData>();
    private final DataService ds;
    private final String language;

    public PlatformRevenueDao(DataService ds, String language) {
        this.ds = ds;
        this.language = language;
    }

    public List<RowData> getRowData() {
        return rowData;
    }

    public void executeQuery(Date start, Date end) {
        SqlQuery sqlQuery = new SqlQuery(QUERY);
        sqlQuery.setString(1, LocalizedObjectTypes.MARKETPLACE_NAME.name());
        sqlQuery.setString(2, language);
        sqlQuery.setDate(3, end);
        sqlQuery.setDate(4, end);
        sqlQuery.setLong(5, start.getTime());
        sqlQuery.setLong(6, end.getTime());
        DataSet dataSet = ds.executeQueryForRawData(sqlQuery);
        readData(dataSet);
    }

    private void readData(DataSet rs) {
        rowData.clear();
        String supplierName;
        String supplierID;
        String supplierCountry;
        String billingResult;
        String marketplace;
        while (rs.next()) {
            billingResult = rs.getString("resultxml");
            if (billingResult != null && billingResult.trim().length() > 0) {
                supplierName = rs.getString("name");
                supplierID = rs.getString("organizationid");
                supplierCountry = rs.getString("countryisocode");
                marketplace = buildMarketplaceLabel(rs);
                rowData.add(new RowData(supplierName, supplierID,
                        supplierCountry, billingResult, marketplace));
            }
        }
    }

    private String buildMarketplaceLabel(DataSet rs) {
        String marketplace;
        marketplace = rs.getString("value");
        String marketplaceId = rs.getString("marketplaceid");
        if (marketplaceId == null) {
            marketplace = "[NOT_LISTED_IN_MARKETPLACE]";
        } else {
            if (marketplace == null || marketplace.trim().length() == 0) {
                marketplace = marketplaceId;
            } else {
                marketplace = marketplace.concat(" (").concat(marketplaceId)
                        .concat(")");
            }
        }
        return marketplace;
    }

}
