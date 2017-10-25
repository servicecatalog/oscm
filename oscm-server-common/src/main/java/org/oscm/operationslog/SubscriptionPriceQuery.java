/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2017
 *                                                                              
 *  Author: tokoda                                                      
 *                                                                              
 *  Creation Date: Oct 13, 2011                                                      
 *                                                                              
 *  Completion Time: Oct 13, 2011                                               
 *                                                                              
 *******************************************************************************/

package org.oscm.operationslog;

import org.oscm.types.enumtypes.LogMessageIdentifier;

/**
 * @author tokoda
 */
public class SubscriptionPriceQuery extends UserOperationLogQuery {

    private static final String SUBSCR_PRICE = "SELECT pm.modDate, pm.modType, (CASE WHEN mu.userId IS NOT NULL THEN mu.userId ELSE pm.modUser END) AS userId, pm.objVersion"
            + SubscriptionQuery.SUBSCR_COMMON_SELECT
            + ", pm.type='FREE_OF_CHARGE' AS free, pm.oneTimeFee, pm.period, pm.pricePerPeriod, pm.pricePerUserAssignment, sc.currencyISOCode"
            + " FROM PriceModelHistory pm"
            + " JOIN ProductHistory p ON pm.productObjKey = p.objKey"
            + " LEFT JOIN SubscriptionHistory s ON p.objKey = s.productObjKey"
            + " LEFT JOIN OrganizationHistory o ON s.organizationObjKey = o.objKey"
            + " LEFT JOIN SupportedCurrency sc ON sc.tkey = pm.currencyObjKey"
            + " LEFT JOIN PlatformUserHistory mu ON pm.modUser = CAST(mu.objKey AS VARCHAR(255))"
            + " WHERE pm.modDate >= :startDate AND pm.modDate <= :endDate"
            + " AND p.objVersion = (SELECT MAX(p2.objVersion) FROM ProductHistory p2 WHERE pm.productObjKey = p2.objKey AND p2.modDate <= pm.modDate)"
            + " AND (s.objVersion IS NULL OR s.objVersion = (SELECT MAX(s2.objVersion) FROM SubscriptionHistory s2 WHERE s2.productObjKey = p.objKey AND s2.modDate <= pm.modDate))"
            + " AND (o.objVersion IS NULL OR o.objVersion = (SELECT MAX(o2.objVersion) FROM OrganizationHistory o2 WHERE s.organizationObjKey = o2.objKey AND o2.modDate <= pm.modDate))"
            + " AND (mu.objVersion IS NULL OR mu.objVersion = (SELECT MAX(mu2.objVersion) FROM PlatformUserHistory mu2 WHERE pm.modUser = CAST(mu2.objKey AS VARCHAR(255)) AND mu2.modDate <= pm.modDate))"
            + SubscriptionQuery.SUBSCR_COMMON_ORDERBY;

    private static final String[] fieldNames = new String[] {
            COMMON_COLUMN_MODDATE, "op", "user", COMMON_COLUMN_OBJVERSION,
            "subscription", "customer", "customer id", "free", "one time fee",
            "period", "price", "price per user", "currency" };

    @Override
    public LogMessageIdentifier getLogMessageIdentifier() {
        return LogMessageIdentifier.INFO_OPERATION_LOG_SUBSCR_PRICE;
    }

    @Override
    public String getQuery() {
        return SUBSCR_PRICE;
    }

    @Override
    public String[] getFieldNames() {
        return fieldNames;
    }

    @Override
    public String getLogType() {
        return "SUBSCR_PRICE";
    }

    @Override
    protected void formatRow(Object[] row) {
        // free
        if (row[7] instanceof Boolean) {
            row[7] = formatYesNo((Boolean) row[7]);
        }
    }

}
