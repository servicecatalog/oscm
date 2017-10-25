/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2017
 *******************************************************************************/

package org.oscm.operatorservice.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.persistence.Query;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.converter.LocaleHandler;
import org.oscm.dataservice.local.DataService;
import org.oscm.domobjects.enums.LocalizedObjectTypes;
import org.oscm.string.Strings;
import org.oscm.types.enumtypes.LogMessageIdentifier;
import org.oscm.internal.types.exception.SaaSSystemException;

/**
 * Storage of the SQL query result for the supplier revenue list.
 * 
 * @author tokoda
 * 
 */
class SupplierRevenueSqlResult {

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(SupplierRevenueSqlResult.class);

    static final SupplierRevenueSqlResult EMPTY = new SupplierRevenueSqlResult();

    /**
     * Inner class to store the result of the SQL query for supplier revenue
     * list.
     * 
     * @author tokoda
     */
    static class RowData {

        RowData(String fromDate, String toDate, String supplierName,
                String supplierID, String amount, String currency,
                String marketplace) {
            this.fromDate = fromDate;
            this.toDate = toDate;
            this.supplierName = supplierName;
            this.supplierId = supplierID;
            this.amount = amount;
            this.currency = currency;
            if (Strings.isEmpty(marketplace)
                    || "null".equalsIgnoreCase(marketplace)) {
                this.marketplace = "";
            } else {
                this.marketplace = marketplace;
            }
        }

        String fromDate;

        String toDate;

        String supplierName;

        String supplierId;

        String amount;

        String currency;

        String marketplace;
    }

    List<RowData> rowData = new ArrayList<RowData>();

    List<RowData> getRowData() {
        return rowData;
    }

    /**
     * Executes the query and returns the result object.
     * 
     * @param dm
     *            Data Manager to be used
     * @return PlatformRevenueSqlResult result of the query
     */
    public static SupplierRevenueSqlResult executeQuery(DataService dm,
            long month) {

        try {
            String locale = dm.getCurrentUser().getLocale();
            String sql = createSqlQuery(month,
                    LocaleHandler.getLocaleFromString(locale));
            Query query = dm.createNativeQuery(sql);
            @SuppressWarnings("unchecked")
            List<Object[]> resultList = query.getResultList();
            return readData(resultList);
        } catch (Exception e) {
            SaaSSystemException sse = new SaaSSystemException(e);
            logger.logError(
                    Log4jLogger.SYSTEM_LOG,
                    sse,
                    LogMessageIdentifier.ERROR_READ_DATA_FOR_REVENUE_REPORT_FAILED);
            throw sse;
        }
    }

    /**
     * Return the SQL query to get supplier revenue list. The billing result of
     * all customers for each supplier are listed. The result is for only one
     * month which is specified as a parameter, and amount is grouped by
     * supplier and currency.
     * 
     * @param month
     *            the long value of the target month which was converted from
     *            'yyyy-MM'
     * @return the SQL query
     */
    static String createSqlQuery(long month, Locale locale) {
        StringBuffer sb = new StringBuffer();

        sb.append("SELECT ");
        sb.append("to_timestamp(billingresult.periodstarttime/1000) AS startdate, ");
        sb.append("to_timestamp(billingresult.periodendtime/1000) AS enddate, ");
        sb.append("supplier.organizationid AS organizationid, ");
        sb.append("supplier.name AS name, ");
        sb.append("sum(cast(XMLSERIALIZE(CONTENT (xpath('//OverallCosts/@netAmount', XMLPARSE (DOCUMENT resultxml)))[1] AS text) AS NUMERIC)) AS amount, ");
        sb.append("XMLSERIALIZE(CONTENT (xpath('//OverallCosts/@currency', XMLPARSE (DOCUMENT billingresult.resultxml)))[1] AS text) AS currency, ");
        sb.append("CASE WHEN res.value IS NULL THEN (SELECT marketplaceid FROM marketplace WHERE tkey = sub.marketplace_tkey) ELSE res.value END AS mp ");
        sb.append("FROM billingresult, organization AS supplier, subscription AS sub LEFT JOIN localizedresource AS res ");
        sb.append("ON res.objectkey = sub.marketplace_tkey ");
        sb.append("AND res.objecttype = '"
                + LocalizedObjectTypes.MARKETPLACE_NAME.name() + "' ");
        sb.append("AND res.locale = '");
        sb.append(locale.getLanguage());
        sb.append("' ");
        sb.append("WHERE ");
        sb.append("to_timestamp(billingresult.periodstarttime/1000) = to_timestamp("
                + month + "/1000) AND ");
        sb.append("to_timestamp(billingresult.periodendtime/1000) = to_timestamp("
                + month + "/1000) + interval '1 month' AND ");
        sb.append("billingresult.resultxml <> '' AND ");
        sb.append("billingresult.chargingorgkey = supplier.tkey AND ");
        sb.append("sub.tkey = billingresult.subscriptionkey ");
        sb.append("GROUP BY periodstarttime, periodendtime, supplier.tkey, supplier.organizationid, supplier.name, currency, mp ");
        sb.append("ORDER BY organizationid, currency, mp");
        System.out.println(sb.toString());
        return sb.toString();
    }

    static SupplierRevenueSqlResult readData(List<Object[]> resultDataList) {
        SupplierRevenueSqlResult result = new SupplierRevenueSqlResult();
        for (Object[] resultData : resultDataList) {
            String fromDate = String.valueOf(resultData[0]);
            String toDate = String.valueOf(resultData[1]);
            String supplierId = String.valueOf(resultData[2]);
            String supplierName = String.valueOf(resultData[3]);
            String amount = String.valueOf(resultData[4]);
            String currency = String.valueOf(resultData[5]);
            String marketplace = String.valueOf(resultData[6]);

            RowData data = new RowData(fromDate, toDate, supplierName,
                    supplierId, amount, currency, marketplace);
            result.getRowData().add(data);
        }
        return result;
    }

}
